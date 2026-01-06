package com.example.rpgplugin.trade.repository;

import com.example.rpgplugin.storage.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * トレード履歴リポジトリ
 *
 * <p>トレード履歴のデータ永続化を担当します。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class TradeHistoryRepository {

    private final DatabaseManager dbManager;
    private final Logger logger;

    /**
     * コンストラクタ
     *
     * @param dbManager データベースマネージャー
     * @param logger ロガー
     */
    public TradeHistoryRepository(DatabaseManager dbManager, Logger logger) {
        this.dbManager = dbManager;
        this.logger = logger;
    }

    /**
     * トレード履歴を保存
     *
     * @param record トレード履歴レコード
     * @return 保存成功時true
     */
    public boolean save(TradeHistoryRecord record) {
        String sql = """
            INSERT INTO trade_history (
                player1_uuid, player2_uuid,
                player1_items, player2_items,
                gold_amount1, gold_amount2,
                trade_time
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, record.player1Uuid.toString());
            stmt.setString(2, record.player2Uuid.toString());
            stmt.setString(3, record.player1ItemsJson);
            stmt.setString(4, record.player2ItemsJson);
            stmt.setDouble(5, record.goldAmount1);
            stmt.setDouble(6, record.goldAmount2);
            stmt.setLong(7, record.tradeTime);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            logger.severe("Failed to save trade history: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * トレード履歴レコード
     */
    public static class TradeHistoryRecord {
        public final java.util.UUID player1Uuid;
        public final java.util.UUID player2Uuid;
        public final String player1ItemsJson;
        public final String player2ItemsJson;
        public final double goldAmount1;
        public final double goldAmount2;
        public final long tradeTime;

        public TradeHistoryRecord(
            java.util.UUID player1Uuid,
            java.util.UUID player2Uuid,
            String player1ItemsJson,
            String player2ItemsJson,
            double goldAmount1,
            double goldAmount2,
            long tradeTime
        ) {
            this.player1Uuid = player1Uuid;
            this.player2Uuid = player2Uuid;
            this.player1ItemsJson = player1ItemsJson;
            this.player2ItemsJson = player2ItemsJson;
            this.goldAmount1 = goldAmount1;
            this.goldAmount2 = goldAmount2;
            this.tradeTime = tradeTime;
        }
    }
}
