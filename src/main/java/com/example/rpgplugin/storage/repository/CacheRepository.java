package com.example.rpgplugin.storage.repository;

import com.example.rpgplugin.storage.models.PlayerData;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 3層キャッシュシステム
 *
 * L1: オンラインプレイヤーキャッシュ（ConcurrentHashMap）
 * L2: 高頻度アクセスキャッシュ（Caffeine）
 * L3: データベース（PlayerDataRepository）
 *
 * キャッシュヒット率目標: 95%以上
 *
 * 設計原則:
 * - SOLID-S: キャッシュ管理に特化
 * - DRY: 設定ロジックを一元管理
 * - KISS: シンプルな3層構造
 * - OCP: 設定ファイルで拡張可能
 */
public class CacheRepository {

    private final Map<UUID, PlayerData> l1Cache;  // オンラインプレイヤー全データ
    private final Cache<UUID, PlayerData> l2Cache;  // 高頻度アクセスデータ
    private final PlayerDataRepository repository;
    private final Logger logger;

    // キャッシュ統計
    private long l1Hits = 0;
    private long l2Hits = 0;
    private long l3Hits = 0;
    private long totalRequests = 0;

    // 統計キャッシュ（1分間キャッシュ）
    private CacheStatistics cachedStats;
    private long lastStatsUpdate = 0;
    private static final long STATS_CACHE_MILLIS = 60000; // 1分

    // 統計ログ出力タスク
    private int statsLoggingTaskId = -1;

    /**
     * コンストラクタ（デフォルト設定）
     *
     * @param repository データリポジトリ
     * @param logger ロガー
     */
    public CacheRepository(PlayerDataRepository repository, Logger logger) {
        this(repository, logger, 2000, 10, true, 0);
    }

    /**
     * コンストラクタ（設定指定）
     *
     * @param repository データリポジトリ
     * @param logger ロガー
     * @param l2MaxSize L2キャッシュ最大サイズ
     * @param l2TtlMinutes L2キャッシュTTL（分）
     * @param expireAfterAccess アクセス時間ベースTTLを使用するか
     * @param statsLoggingInterval 統計ログ出力間隔（秒、0で無効）
     */
    public CacheRepository(
            PlayerDataRepository repository,
            Logger logger,
            int l2MaxSize,
            int l2TtlMinutes,
            boolean expireAfterAccess,
            int statsLoggingInterval) {
        this.repository = repository;
        this.logger = logger;

        // L1キャッシュ: オンラインプレイヤー（スレッドセーフなHashMap）
        this.l1Cache = new ConcurrentHashMap<>();

        // L2キャッシュ: CaffeineによるLRUキャッシュ
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                .maximumSize(l2MaxSize)
                .recordStats();

        // TTLポリシーを設定
        if (expireAfterAccess) {
            caffeineBuilder.expireAfterAccess(l2TtlMinutes, TimeUnit.MINUTES);
            logger.info("L2 cache: Access-based TTL (" + l2TtlMinutes + " minutes)");
        } else {
            caffeineBuilder.expireAfterWrite(l2TtlMinutes, TimeUnit.MINUTES);
            logger.info("L2 cache: Write-based TTL (" + l2TtlMinutes + " minutes)");
        }

        this.l2Cache = caffeineBuilder.build();

        logger.info("L2 cache initialized: max_size=" + l2MaxSize + ", ttl=" + l2TtlMinutes + "min");

        // 統計ログ出力タスクを開始（間隔が0でない場合）
        if (statsLoggingInterval > 0) {
            startStatsLogging(statsLoggingInterval);
        }
    }

    /**
     * 統計ログ出力タスクを開始
     *
     * @param intervalSeconds 間隔（秒）
     */
    private void startStatsLogging(int intervalSeconds) {
        // 実際のスケジューリングはRPGPluginから行うため、ここでは設定のみ
        logger.info("Stats logging interval: " + intervalSeconds + " seconds");
    }

    /**
     * 統計ログ出力タスクを設定
     *
     * @param taskId タスクID
     */
    public void setStatsLoggingTaskId(int taskId) {
        this.statsLoggingTaskId = taskId;
    }

    /**
     * 統計ログ出力タスクをキャンセル
     */
    public void cancelStatsLogging() {
        if (statsLoggingTaskId != -1) {
            // Bukkit.getScheduler().cancelTask(statsLoggingTaskId);
            statsLoggingTaskId = -1;
        }
    }

    /**
     * UUIDでプレイヤーデータを取得
     * 3層キャッシュを順に検索
     *
     * @param uuid プレイヤーUUID
     * @return プレイヤーデータ（存在しない場合は空）
     */
    public Optional<PlayerData> findById(UUID uuid) {
        totalRequests++;

        // L1キャッシュを検索
        PlayerData data = l1Cache.get(uuid);
        if (data != null) {
            l1Hits++;
            logger.fine("L1 cache hit: " + uuid);
            return Optional.of(data);
        }

        // L2キャッシュを検索
        data = l2Cache.getIfPresent(uuid);
        if (data != null) {
            l2Hits++;
            logger.fine("L2 cache hit: " + uuid);
            return Optional.of(data);
        }

        // L3データベースを検索
        try {
            Optional<PlayerData> result = repository.findById(uuid);
            if (result.isPresent()) {
                l3Hits++;
                data = result.get();

                // L2キャッシュに格納
                l2Cache.put(uuid, data);

                logger.fine("L3 database hit: " + uuid);
                return Optional.of(data);
            }
        } catch (Exception e) {
            logger.severe("Failed to fetch player from database: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * プレイヤーデータを保存
     * L1, L2キャッシュを更新し、非同期でデータベースにも保存
     *
     * @param player プレイヤーデータ
     */
    public void save(PlayerData player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUuid();

        // L1キャッシュを更新
        l1Cache.put(uuid, player);

        // L2キャッシュを更新
        l2Cache.put(uuid, player);

        // 非同期でデータベースに保存
        repository.saveAsync(player);

        logger.fine("Player data saved to cache: " + uuid);
    }

    /**
     * オンラインプレイヤーとして登録
     * L1キャッシュに格納
     *
     * @param player プレイヤーデータ
     */
    public void addToOnlineCache(PlayerData player) {
        if (player != null) {
            l1Cache.put(player.getUuid(), player);
            logger.fine("Player added to online cache: " + player.getUuid());
        }
    }

    /**
     * オンラインプレイヤーから削除
     * L1キャッシュから削除（L2には残す）
     *
     * @param uuid プレイヤーUUID
     */
    public void removeFromOnlineCache(UUID uuid) {
        PlayerData removed = l1Cache.remove(uuid);
        if (removed != null) {
            logger.fine("Player removed from online cache: " + uuid);
        }
    }

    /**
     * オンラインプレイヤーをすべて取得
     *
     * @return オンラインプレイヤーリスト
     */
    public Collection<PlayerData> getOnlinePlayers() {
        return new ArrayList<>(l1Cache.values());
    }

    /**
     * オンラインプレイヤー数を取得
     *
     * @return オンラインプレイヤー数
     */
    public int getOnlinePlayerCount() {
        return l1Cache.size();
    }

    /**
     * キャッシュ統計を取得（キャッシュ付き）
     *
     * 統計情報は1分間キャッシュされ、頻繁な呼び出しによるオーバーヘッドを削減します。
     *
     * @return 統計情報
     */
    public CacheStatistics getStatistics() {
        long now = System.currentTimeMillis();

        // キャッシュが有効な場合はキャッシュを返す
        if (cachedStats != null && (now - lastStatsUpdate) < STATS_CACHE_MILLIS) {
            return cachedStats;
        }

        // 統計を再計算
        CacheStats l2Stats = l2Cache.stats();

        double hitRate = totalRequests > 0
                ? ((double) (l1Hits + l2Hits) / totalRequests) * 100
                : 0.0;

        cachedStats = new CacheStatistics(
                l1Hits,
                l2Hits,
                l3Hits,
                totalRequests,
                hitRate,
                l1Cache.size(),
                l2Cache.estimatedSize(),
                l2Stats.hitRate(),
                l2Stats.missRate()
        );

        lastStatsUpdate = now;
        return cachedStats;
    }

    /**
     * キャッシュ統計を強制的に再計算
     *
     * @return 統計情報
     */
    public CacheStatistics getStatisticsForceRefresh() {
        cachedStats = null;
        return getStatistics();
    }

    /**
     * キャッシュ統計をログに出力
     */
    public void logStatistics() {
        CacheStatistics stats = getStatistics();

        logger.info("=== Cache Statistics ===");
        logger.info(String.format("L1 Hits: %d", stats.l1Hits()));
        logger.info(String.format("L2 Hits: %d", stats.l2Hits()));
        logger.info(String.format("L3 Hits: %d", stats.l3Hits()));
        logger.info(String.format("Total Requests: %d", stats.totalRequests()));
        logger.info(String.format("Overall Hit Rate: %.2f%%", stats.overallHitRate()));
        logger.info(String.format("L1 Size: %d (Online players)", stats.l1Size()));
        logger.info(String.format("L2 Size: %d", stats.l2Size()));
        logger.info(String.format("L2 Hit Rate: %.2f%%", stats.l2HitRate() * 100));
        logger.info("========================");
    }

    /**
     * L2キャッシュをクリア
     */
    public void clearL2Cache() {
        l2Cache.invalidateAll();
        logger.info("L2 cache cleared");
    }

    /**
     * 特定プレイヤーのキャッシュをクリア
     *
     * @param uuid プレイヤーUUID
     */
    public void invalidate(UUID uuid) {
        l1Cache.remove(uuid);
        l2Cache.invalidate(uuid);
        logger.fine("Cache invalidated for: " + uuid);
    }

    /**
     * すべてのキャッシュをクリア
     */
    public void clearAll() {
        l1Cache.clear();
        l2Cache.invalidateAll();
        logger.info("All caches cleared");
    }

    /**
     * キャッシュ統計レコード
     */
    public record CacheStatistics(
            long l1Hits,
            long l2Hits,
            long l3Hits,
            long totalRequests,
            double overallHitRate,
            long l1Size,
            long l2Size,
            double l2HitRate,
            double l2MissRate
    ) {
    }
}
