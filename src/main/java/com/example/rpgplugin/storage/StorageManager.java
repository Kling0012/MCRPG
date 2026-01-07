package com.example.rpgplugin.storage;

import com.example.rpgplugin.storage.database.DatabaseManager;
import com.example.rpgplugin.storage.models.PlayerData;
import com.example.rpgplugin.storage.repository.CacheRepository;
import com.example.rpgplugin.storage.repository.PlayerCurrencyRepository;
import com.example.rpgplugin.storage.repository.PlayerDataRepository;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * ストレージ管理クラス
 *
 * <p>データベース、キャッシュ、リポジトリを統合管理します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ストレージシステム管理に特化</li>
 *   <li>DRY: 設定ロジックを一元管理</li>
 *   <li>KISS: シンプルなAPI設計</li>
 *   <li>OCP: 設定ファイルで拡張可能</li>
 * </ul>
 */
public class StorageManager {

    private final Plugin plugin;
    private final Logger logger;

    private DatabaseManager databaseManager;
    private PlayerDataRepository playerDataRepository;
    private PlayerCurrencyRepository playerCurrencyRepository;
    private CacheRepository cacheRepository;

    private boolean initialized = false;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public StorageManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * ストレージシステムを初期化
     *
     * @throws Exception 初期化失敗時
     */
    public void initialize() throws Exception {
        if (initialized) {
            logger.warning("StorageManager is already initialized");
            return;
        }

        logger.info("Initializing storage system...");

        // データベースマネージャの初期化
        databaseManager = new DatabaseManager(plugin);
        databaseManager.initialize();

        // プレイヤーデータリポジトリの初期化
        playerDataRepository = new PlayerDataRepository(databaseManager, logger);

        // プレイヤー通貨リポジトリの初期化
        playerCurrencyRepository = new PlayerCurrencyRepository(databaseManager, logger);

        // キャッシュ設定を読み込み
        ConfigurationSection cacheConfig = plugin.getConfig().getConfigurationSection("cache");
        if (cacheConfig == null) {
            logger.warning("Cache configuration not found, using defaults");
            cacheRepository = new CacheRepository(playerDataRepository, logger);
        } else {
            // キャッシュ設定を取得
            int l2MaxSize = cacheConfig.getInt("l2_max_size", 2000);
            int l2TtlMinutes = cacheConfig.getInt("l2_ttl_minutes", 10);
            boolean expireAfterAccess = cacheConfig.getBoolean("expire_after_access", true);
            int statsLoggingInterval = cacheConfig.getInt("stats_logging_interval", 0);

            // キャッシュリポジトリの初期化（設定適用）
            cacheRepository = new CacheRepository(
                    playerDataRepository,
                    logger,
                    l2MaxSize,
                    l2TtlMinutes,
                    expireAfterAccess,
                    statsLoggingInterval
            );

            logger.info("Cache settings applied: max_size=" + l2MaxSize + ", ttl=" + l2TtlMinutes + "min");
        }

        // 統計ログ出力タスクを開始
        int statsLoggingInterval = plugin.getConfig().getInt("cache.stats_logging_interval", 0);
        if (statsLoggingInterval > 0) {
            startStatsLoggingTask(statsLoggingInterval);
        }

        initialized = true;
        logger.info("Storage system initialized successfully");
    }

    /**
     * 統計ログ出力タスクを開始
     *
     * @param intervalSeconds 間隔（秒）
     */
    private void startStatsLoggingTask(int intervalSeconds) {
        long intervalTicks = intervalSeconds * 20L; // 秒をティックに変換

        plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                () -> {
                    if (cacheRepository != null) {
                        cacheRepository.logStatistics();
                    }
                },
                intervalTicks,
                intervalTicks
        );

        logger.info("Stats logging task started: interval=" + intervalSeconds + "s");
    }

    /**
     * ストレージシステムをシャットダウン
     */
    public void shutdown() {
        if (!initialized) {
            return;
        }

        logger.info("Shutting down storage system...");

        // オンラインプレイヤーをすべて保存
        if (cacheRepository != null) {
            logger.info("Saving online players before shutdown...");
            var onlinePlayers = cacheRepository.getOnlinePlayers();
            for (PlayerData player : onlinePlayers) {
                try {
                    playerDataRepository.save(player);
                } catch (Exception e) {
                    logger.severe("Failed to save player data: " + e.getMessage());
                }
            }

            // 統計を出力
            cacheRepository.logStatistics();
        }

        // データベースをシャットダウン
        if (databaseManager != null) {
            databaseManager.shutdown();
        }

        initialized = false;
        logger.info("Storage system shutdown complete");
    }

    /**
     * プレイヤーデータを取得
     *
     * @param uuid プレイヤーUUID
     * @return プレイヤーデータ（存在しない場合は空）
     */
    public Optional<PlayerData> getPlayerData(UUID uuid) {
        if (!initialized) {
            logger.warning("StorageManager is not initialized");
            return Optional.empty();
        }

        return cacheRepository.findById(uuid);
    }

    /**
     * プレイヤーデータを保存
     *
     * @param player プレイヤーデータ
     */
    public void savePlayerData(PlayerData player) {
        if (!initialized) {
            logger.warning("StorageManager is not initialized");
            return;
        }

        cacheRepository.save(player);
    }

    /**
     * プレイヤーをオンラインキャッシュに追加
     *
     * @param player プレイヤーデータ
     */
    public void addOnlinePlayer(PlayerData player) {
        if (!initialized) {
            logger.warning("StorageManager is not initialized");
            return;
        }

        cacheRepository.addToOnlineCache(player);
        logger.fine("Player added to online cache: " + player.getUuid());
    }

    /**
     * プレイヤーをオフラインにする
     *
     * @param uuid プレイヤーUUID
     */
    public void removeOnlinePlayer(UUID uuid) {
        if (!initialized) {
            logger.warning("StorageManager is not initialized");
            return;
        }

        cacheRepository.removeFromOnlineCache(uuid);
        logger.fine("Player removed from online cache: " + uuid);
    }

    /**
     * オンラインプレイヤーをすべて取得
     *
     * @return オンラインプレイヤーコレクション
     */
    public java.util.Collection<PlayerData> getOnlinePlayers() {
        if (!initialized) {
            logger.warning("StorageManager is not initialized");
            return java.util.Collections.emptyList();
        }

        return cacheRepository.getOnlinePlayers();
    }

    /**
     * オンラインプレイヤー数を取得
     *
     * @return オンラインプレイヤー数
     */
    public int getOnlinePlayerCount() {
        if (!initialized) {
            return 0;
        }

        return cacheRepository.getOnlinePlayerCount();
    }

    /**
     * キャッシュ統計を取得
     *
     * @return キャッシュ統計
     */
    public CacheRepository.CacheStatistics getCacheStatistics() {
        if (!initialized) {
            logger.warning("StorageManager is not initialized");
            return null;
        }

        return cacheRepository.getStatistics();
    }

    /**
     * キャッシュ統計をログに出力
     */
    public void logCacheStatistics() {
        if (!initialized) {
            logger.warning("StorageManager is not initialized");
            return;
        }

        cacheRepository.logStatistics();
    }

    /**
     * プレイヤーデータリポジトリを取得
     *
     * @return プレイヤーデータリポジトリ
     */
    public PlayerDataRepository getPlayerDataRepository() {
        return playerDataRepository;
    }

    /**
     * プレイヤー通貨リポジトリを取得
     *
     * @return プレイヤー通貨リポジトリ
     */
    public PlayerCurrencyRepository getPlayerCurrencyRepository() {
        return playerCurrencyRepository;
    }

    /**
     * キャッシュリポジトリを取得
     *
     * @return キャッシュリポジトリ
     */
    public CacheRepository getCacheRepository() {
        return cacheRepository;
    }

    /**
     * データベースマネージャーを取得
     *
     * @return データベースマネージャー
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * 初期化済みか確認
     *
     * @return 初期化済みの場合true
     */
    public boolean isInitialized() {
        return initialized;
    }
}
