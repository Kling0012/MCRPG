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

    /**
     * コンストラクタ
     *
     * @param repository データリポジトリ
     * @param logger ロガー
     */
    public CacheRepository(PlayerDataRepository repository, Logger logger) {
        this.repository = repository;
        this.logger = logger;

        // L1キャッシュ: オンラインプレイヤー（スレッドセーフなHashMap）
        this.l1Cache = new ConcurrentHashMap<>();

        // L2キャッシュ: CaffeineによるLRUキャッシュ
        this.l2Cache = Caffeine.newBuilder()
                .maximumSize(1000)          // 最大1000エントリ
                .expireAfterWrite(5, TimeUnit.MINUTES)  // 5分TTL
                .recordStats()              // 統計有効化
                .build();
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
     * キャッシュ統計を取得
     *
     * @return 統計情報
     */
    public CacheStatistics getStatistics() {
        CacheStats l2Stats = l2Cache.stats();

        double hitRate = totalRequests > 0
                ? ((double) (l1Hits + l2Hits) / totalRequests) * 100
                : 0.0;

        return new CacheStatistics(
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
