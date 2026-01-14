package com.example.rpgplugin.storage.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * データベーススキーマ管理クラス
 * テーブル作成、バージョニング、マイグレーションを担当
 */
public class SchemaManager {

    private static final int CURRENT_SCHEMA_VERSION = 5;

    private final DatabaseManager dbManager;
    private final Logger logger;

    public SchemaManager(DatabaseManager dbManager, Logger logger) {
        this.dbManager = dbManager;
        this.logger = logger;
    }

    /**
     * データベーススキーマを初期化
     *
     * @throws SQLException 初期化失敗時
     */
    public void initializeSchema() throws SQLException {
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // スキーマバージョンテーブルが存在しない場合は作成
            createSchemaVersionTable(stmt);

            // フレッシュインストールかチェック（player_dataテーブルが存在しない）
            boolean isFreshInstall = !tableExists(stmt, "player_data");

            if (isFreshInstall) {
                // フレッシュインストール時は最新スキーマを直接作成
                logger.info("Fresh install detected, creating latest schema (version " + CURRENT_SCHEMA_VERSION + ")");
                createLatestSchema(stmt);
                // バージョンを記録
                recordSchemaVersion(conn, CURRENT_SCHEMA_VERSION);
            } else {
                // 既存データベースの場合はバージョンを取得してマイグレーション
                int currentVersion = getCurrentSchemaVersion(stmt);
                logger.info("Current schema version: " + currentVersion);

                // 必要に応じてマイグレーション実行
                if (currentVersion < CURRENT_SCHEMA_VERSION) {
                    logger.info("Migrating schema from version " + currentVersion + " to " + CURRENT_SCHEMA_VERSION);
                    migrateSchema(conn, stmt, currentVersion, CURRENT_SCHEMA_VERSION);
                } else if (currentVersion > CURRENT_SCHEMA_VERSION) {
                    logger.warning("Database schema version (" + currentVersion + ") is newer than expected (" + CURRENT_SCHEMA_VERSION + ")");
                }

                // すべてのテーブルが存在することを確認
                ensureTablesExist(stmt);
            }
        }
    }

    /**
     * スキーマバージョンを記録
     */
    private void recordSchemaVersion(Connection conn, int version) throws SQLException {
        String sql = "INSERT INTO schema_version (version) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, version);
            pstmt.executeUpdate();
        }
    }

    /**
     * 最新のスキーマ（バージョン5）を直接作成
     * フレッシュインストール時に使用
     */
    private void createLatestSchema(Statement stmt) throws SQLException {
        logger.info("Creating latest schema");

        // player_data テーブル（V5相当のカラムを含む）
        String playerDataSql = """
            CREATE TABLE IF NOT EXISTS player_data (
                uuid TEXT PRIMARY KEY,
                username TEXT NOT NULL,
                class_id TEXT,
                class_rank INTEGER DEFAULT 1,
                class_history TEXT DEFAULT NULL,
                first_join INTEGER DEFAULT (strftime('%s', 'now')),
                last_login INTEGER DEFAULT (strftime('%s', 'now')),
                max_health INTEGER DEFAULT 20,
                max_mana INTEGER DEFAULT 100,
                current_mana INTEGER DEFAULT 100,
                cost_type TEXT DEFAULT 'mana'
            )
        """;
        stmt.execute(playerDataSql);

        // player_stats テーブル
        String playerStatsSql = """
            CREATE TABLE IF NOT EXISTS player_stats (
                uuid TEXT PRIMARY KEY,
                strength_base INTEGER DEFAULT 0,
                intelligence_base INTEGER DEFAULT 0,
                spirit_base INTEGER DEFAULT 0,
                vitality_base INTEGER DEFAULT 0,
                dexterity_base INTEGER DEFAULT 0,
                strength_auto INTEGER DEFAULT 0,
                intelligence_auto INTEGER DEFAULT 0,
                spirit_auto INTEGER DEFAULT 0,
                vitality_auto INTEGER DEFAULT 0,
                dexterity_auto INTEGER DEFAULT 0,
                available_points INTEGER DEFAULT 0,
                FOREIGN KEY (uuid) REFERENCES player_data(uuid) ON DELETE CASCADE
            )
        """;
        stmt.execute(playerStatsSql);

        // インデックス作成
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_stats_uuid ON player_stats(uuid)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_data_username ON player_data(username)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_data_class ON player_data(class_id)");

        // V3: 通貨テーブル
        String playerCurrencySql = """
            CREATE TABLE IF NOT EXISTS player_currency (
                uuid TEXT PRIMARY KEY,
                gold_balance REAL DEFAULT 0.0,
                total_earned REAL DEFAULT 0.0,
                total_spent REAL DEFAULT 0.0,
                FOREIGN KEY (uuid) REFERENCES player_data(uuid) ON DELETE CASCADE
            )
        """;
        stmt.execute(playerCurrencySql);
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_currency_uuid ON player_currency(uuid)");

        // V3: オークションテーブル
        String auctionListingsSql = """
            CREATE TABLE IF NOT EXISTS auction_listings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                seller_uuid TEXT NOT NULL,
                seller_name TEXT NOT NULL,
                item_data TEXT NOT NULL,
                starting_price REAL NOT NULL,
                current_bid REAL DEFAULT 0,
                current_bidder TEXT,
                duration_seconds INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                expires_at INTEGER NOT NULL,
                is_active BOOLEAN DEFAULT TRUE,
                FOREIGN KEY (seller_uuid) REFERENCES player_data(uuid)
            )
        """;
        stmt.execute(auctionListingsSql);

        String auctionBidsSql = """
            CREATE TABLE IF NOT EXISTS auction_bids (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                auction_id INTEGER NOT NULL,
                bidder_uuid TEXT NOT NULL,
                bid_amount REAL NOT NULL,
                bid_time INTEGER DEFAULT (strftime('%s', 'now')),
                FOREIGN KEY (auction_id) REFERENCES auction_listings(id) ON DELETE CASCADE
            )
        """;
        stmt.execute(auctionBidsSql);

        stmt.execute("CREATE INDEX IF NOT EXISTS idx_auction_listings_seller ON auction_listings(seller_uuid)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_auction_listings_expires ON auction_listings(expires_at)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_auction_listings_active ON auction_listings(is_active)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_auction_bids_auction ON auction_bids(auction_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_auction_bids_bidder ON auction_bids(bidder_uuid)");

        // V3: トレード履歴テーブル
        String tradeHistorySql = """
            CREATE TABLE IF NOT EXISTS trade_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player1_uuid TEXT NOT NULL,
                player2_uuid TEXT NOT NULL,
                player1_items TEXT,
                player2_items TEXT,
                gold_amount1 REAL DEFAULT 0.0,
                gold_amount2 REAL DEFAULT 0.0,
                trade_time INTEGER DEFAULT (strftime('%s', 'now')),
                FOREIGN KEY (player1_uuid) REFERENCES player_data(uuid),
                FOREIGN KEY (player2_uuid) REFERENCES player_data(uuid)
            )
        """;
        stmt.execute(tradeHistorySql);

        stmt.execute("CREATE INDEX IF NOT EXISTS idx_trade_history_player1 ON trade_history(player1_uuid)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_trade_history_player2 ON trade_history(player2_uuid)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_trade_history_time ON trade_history(trade_time)");

        // V4: MythicMobsドロップ管理テーブル
        String mythicDropsSql = """
            CREATE TABLE IF NOT EXISTS mythic_drops (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_uuid TEXT NOT NULL,
                mob_id TEXT NOT NULL,
                item_data TEXT NOT NULL,
                dropped_at INTEGER DEFAULT (strftime('%s', 'now')),
                is_claimed BOOLEAN DEFAULT 0,
                expires_at INTEGER,
                FOREIGN KEY (player_uuid) REFERENCES player_data(uuid) ON DELETE CASCADE
            )
        """;
        stmt.execute(mythicDropsSql);

        stmt.execute("CREATE INDEX IF NOT EXISTS idx_mythic_drops_player ON mythic_drops(player_uuid)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_mythic_drops_mob ON mythic_drops(mob_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_mythic_drops_expires ON mythic_drops(expires_at)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_mythic_drops_claimed ON mythic_drops(is_claimed)");

        logger.info("Latest schema created successfully");
    }

    /**
     * スキーマバージョンテーブルを作成
     */
    private void createSchemaVersionTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS schema_version (
                version INTEGER PRIMARY KEY,
                applied_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """;
        stmt.execute(sql);
    }

    /**
     * 現在のスキーマバージョンを取得
     */
    private int getCurrentSchemaVersion(Statement stmt) throws SQLException {
        // テーブルにデータがあるか確認
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM schema_version");
        if (rs.next() && rs.getInt("count") == 0) {
            // 初期バージョンを登録
            stmt.execute("INSERT INTO schema_version (version) VALUES (1)");
            return 1;
        }

        // 最大のバージョンを取得
        rs = stmt.executeQuery("SELECT MAX(version) as version FROM schema_version");
        if (rs.next()) {
            return rs.getInt("version");
        }

        return 1;
    }

    /**
     * スキーママイグレーション実行
     */
    private void migrateSchema(Connection conn, Statement stmt, int fromVersion, int toVersion) throws SQLException {
        String insertVersionSql = "INSERT INTO schema_version (version) VALUES (?)";
        
        try {
            conn.setAutoCommit(false);

            for (int version = fromVersion + 1; version <= toVersion; version++) {
                logger.info("Applying migration for version " + version);
                applyMigration(stmt, version);

                // バージョンを記録（PreparedStatementでSQLインジェクション対策）
                try (PreparedStatement pstmt = conn.prepareStatement(insertVersionSql)) {
                    pstmt.setInt(1, version);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            logger.info("Schema migration completed successfully");
        } catch (SQLException e) {
            conn.rollback();
            logger.severe("Schema migration failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 特定バージョンのマイグレーションを適用
     */
    private void applyMigration(Statement stmt, int version) throws SQLException {
        switch (version) {
            case 1:
                applyMigrationV1(stmt);
                break;
            case 2:
                applyMigrationV2(stmt);
                break;
            case 3:
                applyMigrationV3(stmt);
                break;
            case 4:
                applyMigrationV4(stmt);
                break;
            case 5:
                applyMigrationV5(stmt);
                break;
            default:
                throw new SQLException("Unknown migration version: " + version);
        }
    }

    /**
     * バージョン1のスキーマを作成
     */
    private void applyMigrationV1(Statement stmt) throws SQLException {
        logger.info("Creating version 1 schema");

        // player_data テーブル
        String playerDataSql = """
            CREATE TABLE IF NOT EXISTS player_data (
                uuid TEXT PRIMARY KEY,
                username TEXT NOT NULL,
                class_id TEXT,
                class_rank INTEGER DEFAULT 1,
                first_join INTEGER DEFAULT (strftime('%s', 'now')),
                last_login INTEGER DEFAULT (strftime('%s', 'now'))
            )
        """;
        stmt.execute(playerDataSql);

        // player_stats テーブル
        String playerStatsSql = """
            CREATE TABLE IF NOT EXISTS player_stats (
                uuid TEXT PRIMARY KEY,
                strength_base INTEGER DEFAULT 0,
                intelligence_base INTEGER DEFAULT 0,
                spirit_base INTEGER DEFAULT 0,
                vitality_base INTEGER DEFAULT 0,
                dexterity_base INTEGER DEFAULT 0,
                strength_auto INTEGER DEFAULT 0,
                intelligence_auto INTEGER DEFAULT 0,
                spirit_auto INTEGER DEFAULT 0,
                vitality_auto INTEGER DEFAULT 0,
                dexterity_auto INTEGER DEFAULT 0,
                available_points INTEGER DEFAULT 0,
                FOREIGN KEY (uuid) REFERENCES player_data(uuid) ON DELETE CASCADE
            )
        """;
        stmt.execute(playerStatsSql);

        // インデックス作成
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_stats_uuid ON player_stats(uuid)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_data_username ON player_data(username)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_data_class ON player_data(class_id)");

        logger.info("Version 1 schema created successfully");
    }

    /**
     * テーブルが存在するかチェック
     *
     * @param stmt SQLステートメント
     * @param tableName テーブル名（ホワイトリスト検証済み）
     * @return テーブルが存在する場合true
     * @throws SQLException データベースエラー
     * @throws IllegalArgumentException テーブル名が無効な場合
     */
    private boolean tableExists(Statement stmt, String tableName) throws SQLException {
        // ホワイトリスト方式でテーブル名を検証（SQLインジェクション対策）
        String sanitized = sanitizeTableName(tableName);
        if (!sanitized.equals(tableName)) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }

        ResultSet rs = stmt.executeQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='" + sanitized + "'"
        );
        return rs.next();
    }

    /**
     * テーブル名をサニタイズ（ホワイトリスト方式）
     *
     * <p>許可する文字: アルファベット、数字、アンダースコアのみ</p>
     *
     * @param tableName テーブル名
     * @return サニタイズされたテーブル名
     */
    private String sanitizeTableName(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            return "";
        }
        // アルファベット、数字、アンダースコアのみを許可
        return tableName.replaceAll("[^a-zA-Z0-9_]", "");
    }

    /**
     * バージョン2のマイグレーション: クラス履歴カラムを追加
     */
    private void applyMigrationV2(Statement stmt) throws SQLException {
        logger.info("Applying version 2 migration: adding class_history column");

        // player_dataテーブルが存在しない場合はスキップ（V1で作成される）
        if (!tableExists(stmt, "player_data")) {
            logger.info("player_data table does not exist yet, skipping V2 migration");
            return;
        }

        // class_historyカラムを追加（既存の場合はスキップ）
        String alterSql = """
            ALTER TABLE player_data ADD COLUMN class_history TEXT DEFAULT NULL
        """;
        try {
            stmt.execute(alterSql);
            logger.info("class_history column added successfully");
        } catch (SQLException e) {
            // カラムが既に存在する場合のエラーを無視
            if (!e.getMessage().contains("duplicate column name")) {
                throw e;
            }
            logger.info("class_history column already exists");
        }

        logger.info("Version 2 migration completed successfully");
    }

    /**
     * バージョン3のマイグレーション: 通貨・オークション・トレードシステムテーブルを追加
     */
    private void applyMigrationV3(Statement stmt) throws SQLException {
        logger.info("Applying version 3 migration: adding currency, auction, and trade tables");

        // player_currency テーブル
        String playerCurrencySql = """
            CREATE TABLE IF NOT EXISTS player_currency (
                uuid TEXT PRIMARY KEY,
                gold_balance REAL DEFAULT 0.0,
                total_earned REAL DEFAULT 0.0,
                total_spent REAL DEFAULT 0.0,
                FOREIGN KEY (uuid) REFERENCES player_data(uuid) ON DELETE CASCADE
            )
        """;
        stmt.execute(playerCurrencySql);

        // インデックス作成（通貨）
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_currency_uuid ON player_currency(uuid)");

        // auction_listings テーブル
        String auctionListingsSql = """
            CREATE TABLE IF NOT EXISTS auction_listings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                seller_uuid TEXT NOT NULL,
                seller_name TEXT NOT NULL,
                item_data TEXT NOT NULL,
                starting_price REAL NOT NULL,
                current_bid REAL DEFAULT 0,
                current_bidder TEXT,
                duration_seconds INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                expires_at INTEGER NOT NULL,
                is_active BOOLEAN DEFAULT TRUE,
                FOREIGN KEY (seller_uuid) REFERENCES player_data(uuid)
            )
        """;
        stmt.execute(auctionListingsSql);

        // auction_bids テーブル（入札履歴）
        String auctionBidsSql = """
            CREATE TABLE IF NOT EXISTS auction_bids (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                auction_id INTEGER NOT NULL,
                bidder_uuid TEXT NOT NULL,
                bid_amount REAL NOT NULL,
                bid_time INTEGER DEFAULT (strftime('%s', 'now')),
                FOREIGN KEY (auction_id) REFERENCES auction_listings(id) ON DELETE CASCADE
            )
        """;
        stmt.execute(auctionBidsSql);

        // インデックス作成（オークション）
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_auction_listings_seller ON auction_listings(seller_uuid)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_auction_listings_expires ON auction_listings(expires_at)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_auction_listings_active ON auction_listings(is_active)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_auction_bids_auction ON auction_bids(auction_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_auction_bids_bidder ON auction_bids(bidder_uuid)");

        // trade_history テーブル（トレード履歴）
        String tradeHistorySql = """
            CREATE TABLE IF NOT EXISTS trade_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player1_uuid TEXT NOT NULL,
                player2_uuid TEXT NOT NULL,
                player1_items TEXT,
                player2_items TEXT,
                gold_amount1 REAL DEFAULT 0.0,
                gold_amount2 REAL DEFAULT 0.0,
                trade_time INTEGER DEFAULT (strftime('%s', 'now')),
                FOREIGN KEY (player1_uuid) REFERENCES player_data(uuid),
                FOREIGN KEY (player2_uuid) REFERENCES player_data(uuid)
            )
        """;
        stmt.execute(tradeHistorySql);

        // インデックス作成（トレード）
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_trade_history_player1 ON trade_history(player1_uuid)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_trade_history_player2 ON trade_history(player2_uuid)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_trade_history_time ON trade_history(trade_time)");

        logger.info("Version 3 migration completed successfully");
    }

    /**
     * バージョン4のマイグレーション: MythicMobsドロップ管理テーブルを追加
     */
    private void applyMigrationV4(Statement stmt) throws SQLException {
        logger.info("Applying version 4 migration: adding mythic_drops table");

        // mythic_drops テーブル
        String mythicDropsSql = """
            CREATE TABLE IF NOT EXISTS mythic_drops (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_uuid TEXT NOT NULL,
                mob_id TEXT NOT NULL,
                item_data TEXT NOT NULL,
                dropped_at INTEGER DEFAULT (strftime('%s', 'now')),
                is_claimed BOOLEAN DEFAULT 0,
                expires_at INTEGER,
                FOREIGN KEY (player_uuid) REFERENCES player_data(uuid) ON DELETE CASCADE
            )
        """;
        stmt.execute(mythicDropsSql);

        // インデックス作成
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_mythic_drops_player ON mythic_drops(player_uuid)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_mythic_drops_mob ON mythic_drops(mob_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_mythic_drops_expires ON mythic_drops(expires_at)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_mythic_drops_claimed ON mythic_drops(is_claimed)");

        logger.info("Version 4 migration completed successfully");
    }

    /**
     * バージョン5のマイグレーション: MP/HP関連フィールドを追加
     */
    private void applyMigrationV5(Statement stmt) throws SQLException {
        logger.info("Applying version 5 migration: adding MP/HP fields to player_data");

        // player_dataテーブルが存在しない場合はスキップ（V1で作成される）
        if (!tableExists(stmt, "player_data")) {
            logger.info("player_data table does not exist yet, skipping V5 migration");
            return;
        }

        // player_data テーブルにカラム追加
        stmt.execute("ALTER TABLE player_data ADD COLUMN max_health INTEGER DEFAULT 20");
        stmt.execute("ALTER TABLE player_data ADD COLUMN max_mana INTEGER DEFAULT 100");
        stmt.execute("ALTER TABLE player_data ADD COLUMN current_mana INTEGER DEFAULT 100");
        stmt.execute("ALTER TABLE player_data ADD COLUMN cost_type TEXT DEFAULT 'mana'");

        logger.info("Version 5 migration completed successfully");
    }

    /**
     * すべてのテーブルが存在することを確認
     */
    private void ensureTablesExist(Statement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table'");

        boolean hasPlayerData = false;
        boolean hasPlayerStats = false;
        boolean hasAuctionListings = false;
        boolean hasAuctionBids = false;

        while (rs.next()) {
            String tableName = rs.getString("name");
            if ("player_data".equals(tableName)) {
                hasPlayerData = true;
            } else if ("player_stats".equals(tableName)) {
                hasPlayerStats = true;
            } else if ("auction_listings".equals(tableName)) {
                hasAuctionListings = true;
            } else if ("auction_bids".equals(tableName)) {
                hasAuctionBids = true;
            }
        }

        if (!hasPlayerData || !hasPlayerStats) {
            logger.warning("Some tables are missing, applying version 1 schema");
            applyMigrationV1(stmt);
        }

        if (!hasAuctionListings || !hasAuctionBids) {
            logger.warning("Auction tables are missing, applying version 3 schema");
            applyMigrationV3(stmt);
        }
    }

    /**
     * スキーマバージョンを取得
     *
     * @return 現在のスキーマバージョン
     */
    public int getSchemaVersion() {
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT MAX(version) as version FROM schema_version");
            if (rs.next()) {
                return rs.getInt("version");
            }
        } catch (SQLException e) {
            logger.warning("Failed to get schema version: " + e.getMessage());
        }
        return 0;
    }
}
