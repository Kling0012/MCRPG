package com.example.rpgplugin.storage.repository;

import com.example.rpgplugin.storage.database.DatabaseManager;
import com.example.rpgplugin.storage.models.PlayerCurrency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * プレイヤー通貨データリポジトリ実装
 * データベースへのCRUD操作を担当
 */
public class PlayerCurrencyRepository implements IRepository<PlayerCurrency, UUID> {

    private final DatabaseManager dbManager;
    private final Logger logger;

    public PlayerCurrencyRepository(DatabaseManager dbManager, Logger logger) {
        this.dbManager = dbManager;
        this.logger = logger;
    }

    @Override
    public void save(PlayerCurrency currency) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO player_currency (uuid, gold_balance, total_earned, total_spent)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, currency.getUuid().toString());
            stmt.setDouble(2, currency.getGoldBalance());
            stmt.setDouble(3, currency.getTotalEarned());
            stmt.setDouble(4, currency.getTotalSpent());

            stmt.executeUpdate();
            logger.fine("[Currency] Player currency saved: " + currency.getUuid());
        }
    }

    @Override
    public void saveAsync(PlayerCurrency currency) {
        dbManager.executeAsync(() -> {
            try {
                save(currency);
            } catch (SQLException e) {
                logger.severe("[Currency] Failed to save player currency asynchronously: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public Optional<PlayerCurrency> findById(UUID uuid) throws SQLException {
        String sql = "SELECT * FROM player_currency WHERE uuid = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToPlayerCurrency(rs));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public List<PlayerCurrency> findAll() throws SQLException {
        String sql = "SELECT * FROM player_currency";
        List<PlayerCurrency> currencies = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                currencies.add(mapRowToPlayerCurrency(rs));
            }
        }

        return currencies;
    }

    @Override
    public void deleteById(UUID uuid) throws SQLException {
        String sql = "DELETE FROM player_currency WHERE uuid = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();

            logger.info("[Currency] Player currency deleted: " + uuid);
        }
    }

    @Override
    public boolean existsById(UUID uuid) throws SQLException {
        return findById(uuid).isPresent();
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM player_currency";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getLong("count");
            }
        }

        return 0;
    }

    @Override
    public void saveAll(List<PlayerCurrency> currencies) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO player_currency (uuid, gold_balance, total_earned, total_spent)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            try {
                for (PlayerCurrency currency : currencies) {
                    stmt.setString(1, currency.getUuid().toString());
                    stmt.setDouble(2, currency.getGoldBalance());
                    stmt.setDouble(3, currency.getTotalEarned());
                    stmt.setDouble(4, currency.getTotalSpent());

                    stmt.addBatch();
                    stmt.clearParameters();
                }

                stmt.executeBatch();
                conn.commit();

                logger.fine("[Currency] Batch saved " + currencies.size() + " player currencies");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public void saveAllAsync(List<PlayerCurrency> currencies) {
        dbManager.executeAsync(() -> {
            try {
                saveAll(currencies);
            } catch (SQLException e) {
                logger.severe("[Currency] Failed to batch save player currencies asynchronously: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * ResultSetをPlayerCurrencyにマッピング
     */
    private PlayerCurrency mapRowToPlayerCurrency(ResultSet rs) throws SQLException {
        UUID uuid = UUID.fromString(rs.getString("uuid"));
        double goldBalance = rs.getDouble("gold_balance");
        double totalEarned = rs.getDouble("total_earned");
        double totalSpent = rs.getDouble("total_spent");

        return new PlayerCurrency(uuid, goldBalance, totalEarned, totalSpent);
    }

    /**
     * プレイヤーの通貨データが存在しない場合に新規作成
     *
     * @param uuid プレイヤーUUID
     * @return 通貨データ
     * @throws SQLException 取得失敗時
     */
    public PlayerCurrency getOrCreate(UUID uuid) throws SQLException {
        Optional<PlayerCurrency> currency = findById(uuid);
        if (currency.isPresent()) {
            return currency.get();
        }

        // 新規作成
        PlayerCurrency newCurrency = new PlayerCurrency(uuid);
        save(newCurrency);
        return newCurrency;
    }
}
