package com.example.rpgplugin.storage;

import com.example.rpgplugin.storage.database.DatabaseManager;
import com.example.rpgplugin.storage.models.PlayerData;
import com.example.rpgplugin.storage.repository.CacheRepository;
import com.example.rpgplugin.storage.repository.PlayerDataRepository;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * ストレージ管理クラス
 * データベース、キャッシュ、リポジトリを統合管理
 */
public class StorageManager {

    private final Plugin plugin;
    private final Logger logger;

    private DatabaseManager databaseManager;
    private PlayerDataRepository playerDataRepository;
    private CacheRepository cacheRepository;

    private boolean initialized = false;

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

        // キャッシュリポジトリの初期化
        cacheRepository = new CacheRepository(playerDataRepository, logger);

        initialized = true;
        logger.info("Storage system initialized successfully");
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
    public var getOnlinePlayers() {
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
