package com.example.rpgplugin.storage.database;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * データベース接続マネージャー
 * SQLite接続プールを管理
 */
public class DatabaseManager {

    private final Logger logger;
    private final Plugin plugin;
    private final File dataFolder;
    private ConnectionPool connectionPool;
    private SchemaManager schemaManager;

    /**
     * コンストラクタ
     *
     * @param plugin Bukkitプラグインインスタンス
     */
    public DatabaseManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dataFolder = plugin.getDataFolder();
    }

    /**
     * データベースを初期化
     *
     * @throws SQLException 初期化失敗時
     */
    public void initialize() throws SQLException {
        // データフォルダが存在しない場合は作成
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // dataサブディレクトリを作成
        File dataSubDir = new File(dataFolder, "data");
        if (!dataSubDir.exists()) {
            dataSubDir.mkdirs();
        }

        // データベースファイルのパス
        File dbFile = new File(dataSubDir, "database.db");

        logger.info("Initializing database: " + dbFile.getAbsolutePath());

        // JDBC URL作成
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        // コネクションプールの初期化
        connectionPool = new ConnectionPool(() -> {
            try {
                return java.sql.DriverManager.getConnection(url);
            } catch (SQLException e) {
                logger.severe("Failed to create database connection: " + e.getMessage());
                throw e;
            }
        }, 10, 30);

        // スキーママネージャの初期化
        schemaManager = new SchemaManager(this, logger);

        // スキーマの初期化
        schemaManager.initializeSchema();

        logger.info("Database initialized successfully");
    }

    /**
     * コネクションを取得
     *
     * @return データベースコネクション
     * @throws SQLException 取得失敗時
     */
    public Connection getConnection() throws SQLException {
        if (connectionPool == null) {
            throw new SQLException("DatabaseManager is not initialized");
        }
        return connectionPool.getConnection();
    }

    /**
     * 非同期クエリを実行
     *
     * @param query 実行するクエリ
     * @param callback 結果コールバック
     */
    public void executeAsync(Runnable query) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                query.run();
            } catch (Exception e) {
                logger.severe("Async query failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * データベースをシャットダウン
     */
    public void shutdown() {
        if (connectionPool != null) {
            connectionPool.shutdown();
            connectionPool = null;
            logger.info("Database connection pool shutdown");
        }
    }

    /**
     * コネクションプールを取得
     *
     * @return コネクションプール
     */
    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    /**
     * データベースファイルを削除（テスト用）
     *
     * @return 削除成功時true
     */
    public boolean deleteDatabase() {
        shutdown();
        File dataSubDir = new File(dataFolder, "data");
        File dbFile = new File(dataSubDir, "database.db");
        if (dbFile.exists()) {
            return dbFile.delete();
        }
        return true;
    }
}
