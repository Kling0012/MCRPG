package com.example.rpgplugin.storage.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * データベーススキーママイグレーション v1.1
 *
 * <p>MP/HP拡張に伴うプレイヤーステータスのマイグレーション</p>
 *
 * <p>変更内容：</p>
 * <ul>
 *   <li>player_dataテーブルにmax_healthカラムを追加（デフォルト20）</li>
 *   <li>player_dataテーブルにmax_manaカラムを追加（デフォルト100）</li>
 *   <li>player_dataテーブルにcurrent_manaカラムを追加（デフォルト100）</li>
 *   <li>player_dataテーブルにcost_typeカラムを追加（デフォルト'mana'）</li>
 * </ul>
 *
 * <p>既存データの互換性：</p>
 * <ul>
 *   <li>既存プレイヤーデータにはデフォルト値が自動設定されます</li>
 *   <li>SPI精神値からのMP計算はオプションとして実装可能</li>
 * </ul>
 */
public class Migration_v1_1_PlayerStats {

    private static final String MIGRATION_NAME = "v1.1_PlayerStats";
    private static final int TARGET_VERSION = 5;

    /**
     * マイグレーションを適用します
     *
     * @param conn データベース接続
     * @param logger ロガー
     * @throws SQLException マイグレーション失敗時
     */
    public static void apply(Connection conn, Logger logger) throws SQLException {
        logger.info("Applying migration " + MIGRATION_NAME);

        try (Statement stmt = conn.createStatement()) {
            // SQLiteではカラムが存在する場合にADD COLUMNが失敗するため、
            // トランザクション内で各ALTER TABLEを実行

            // max_healthカラムの追加
            try {
                stmt.execute("ALTER TABLE player_data ADD COLUMN max_health INTEGER DEFAULT 20");
                logger.info("Added max_health column to player_data");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column name")) {
                    logger.info("max_health column already exists");
                } else {
                    throw e;
                }
            }

            // max_manaカラムの追加
            try {
                stmt.execute("ALTER TABLE player_data ADD COLUMN max_mana INTEGER DEFAULT 100");
                logger.info("Added max_mana column to player_data");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column name")) {
                    logger.info("max_mana column already exists");
                } else {
                    throw e;
                }
            }

            // current_manaカラムの追加
            try {
                stmt.execute("ALTER TABLE player_data ADD COLUMN current_mana INTEGER DEFAULT 100");
                logger.info("Added current_mana column to player_data");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column name")) {
                    logger.info("current_mana column already exists");
                } else {
                    throw e;
                }
            }

            // cost_typeカラムの追加
            try {
                stmt.execute("ALTER TABLE player_data ADD COLUMN cost_type TEXT DEFAULT 'mana'");
                logger.info("Added cost_type column to player_data");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column name")) {
                    logger.info("cost_type column already exists");
                } else {
                    throw e;
                }
            }

            logger.info("Migration " + MIGRATION_NAME + " completed successfully");
        }
    }

    /**
     * マイグレーション名を取得します
     *
     * @return マイグレーション名
     */
    public static String getMigrationName() {
        return MIGRATION_NAME;
    }

    /**
     * 対象バージョンを取得します
     *
     * @return ターゲットバージョン
     */
    public static int getTargetVersion() {
        return TARGET_VERSION;
    }

    /**
     * 既存データのMP値をSPI精神値から計算して更新します（オプション）
     *
     * <p>このメソッドはオプションです。SPI精神値とMPを連動させたい場合に使用します。</p>
     * <p>計算式：MP = SPI * 5 + 100（例）</p>
     *
     * @param conn データベース接続
     * @param logger ロガー
     * @param baseMp 基礎MP値
     * @param spiMultiplier SPI倍率
     * @throwsSQLException 更新失敗時
     */
    public static void updateManaFromSpirit(Connection conn, Logger logger, int baseMp, double spiMultiplier) throws SQLException {
        logger.info("Updating mana values from SPI stats");

        try (Statement stmt = conn.createStatement()) {
            // player_statsテーブルと結合してSPI値を取得
            String updateSql = String.format(
                "UPDATE player_data " +
                "SET max_mana = CAST(%d + (COALESCE(" +
                "  (SELECT spirit_base + spirit_auto FROM player_stats WHERE player_stats.uuid = player_data.uuid)" +
                ") * %f) AS INTEGER), " +
                "current_mana = CAST(%d + (COALESCE(" +
                "  (SELECT spirit_base + spirit_auto FROM player_stats WHERE player_stats.uuid = player_data.uuid)" +
                ") * %f) AS INTEGER) " +
                "WHERE max_mana = 100", // デフォルト値のレコードのみ更新
                baseMp, spiMultiplier, baseMp, spiMultiplier
            );

            int updated = stmt.executeUpdate(updateSql);
            logger.info("Updated " + updated + " players' mana from SPI stats");
        }
    }

    /**
     * ロールバック処理（SQLiteはALTER TABLEのROLLBACKをサポートしていないため、
     * データ移行用の新しいテーブル作成方式で実装する必要があります）
     *
     * @param conn データベース接続
     * @param logger ロガー
     * @throws SQLException ロールバック失敗時
     */
    public static void rollback(Connection conn, Logger logger) throws SQLException {
        logger.warning("Rolling back migration " + MIGRATION_NAME);
        logger.warning("Note: SQLite does not support DROP COLUMN. Manual recreation required.");

        // SQLiteはALTER TABLE ... DROP COLUMNをサポートしていないため、
        // 完全なロールバックにはテーブルの再作成が必要です
        // ここでは警告のみログ出力します
        logger.warning("To rollback, manually recreate player_data table without new columns");
    }
}
