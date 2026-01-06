package com.example.rpgplugin.storage.repository;

import com.example.rpgplugin.storage.database.DatabaseManager;
import com.example.rpgplugin.storage.models.PlayerData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * プレイヤーデータリポジトリ実装
 * データベースへのCRUD操作を担当
 */
public class PlayerDataRepository implements IRepository<PlayerData, UUID> {

    private final DatabaseManager dbManager;
    private final Logger logger;

    public PlayerDataRepository(DatabaseManager dbManager, Logger logger) {
        this.dbManager = dbManager;
        this.logger = logger;
    }

    @Override
    public void save(PlayerData player) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO player_data (uuid, username, class_id, class_rank, first_join, last_login)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, player.getUuid().toString());
            stmt.setString(2, player.getUsername());
            stmt.setString(3, player.getClassId());
            stmt.setInt(4, player.getClassRank());
            stmt.setLong(5, player.getFirstJoin());
            stmt.setLong(6, player.getLastLogin());

            stmt.executeUpdate();
            logger.fine("Player data saved: " + player.getUuid());
        }
    }

    @Override
    public void saveAsync(PlayerData player) {
        dbManager.executeAsync(() -> {
            try {
                save(player);
            } catch (SQLException e) {
                logger.severe("Failed to save player data asynchronously: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public Optional<PlayerData> findById(UUID uuid) throws SQLException {
        String sql = "SELECT * FROM player_data WHERE uuid = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToPlayerData(rs));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public List<PlayerData> findAll() throws SQLException {
        String sql = "SELECT * FROM player_data";
        List<PlayerData> players = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                players.add(mapRowToPlayerData(rs));
            }
        }

        return players;
    }

    @Override
    public void deleteById(UUID uuid) throws SQLException {
        String sql = "DELETE FROM player_data WHERE uuid = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();

            logger.info("Player data deleted: " + uuid);
        }
    }

    @Override
    public boolean existsById(UUID uuid) throws SQLException {
        return findById(uuid).isPresent();
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM player_data";

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
    public void saveAll(List<PlayerData> players) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO player_data (uuid, username, class_id, class_rank, first_join, last_login)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            try {
                for (PlayerData player : players) {
                    stmt.setString(1, player.getUuid().toString());
                    stmt.setString(2, player.getUsername());
                    stmt.setString(3, player.getClassId());
                    stmt.setInt(4, player.getClassRank());
                    stmt.setLong(5, player.getFirstJoin());
                    stmt.setLong(6, player.getLastLogin());

                    stmt.addBatch();
                }

                stmt.executeBatch();
                conn.commit();

                logger.fine("Batch saved " + players.size() + " players");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public void saveAllAsync(List<PlayerData> players) {
        dbManager.executeAsync(() -> {
            try {
                saveAll(players);
            } catch (SQLException e) {
                logger.severe("Failed to batch save player data asynchronously: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * ユーザー名でプレイヤーを検索
     *
     * @param username ユーザー名
     * @return プレイヤーデータ
     * @throws SQLException 検索失敗時
     */
    public Optional<PlayerData> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM player_data WHERE username = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToPlayerData(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * 特定クラスのプレイヤーをすべて取得
     *
     * @param classId クラスID
     * @return プレイヤーリスト
     * @throws SQLException 取得失敗時
     */
    public List<PlayerData> findByClass(String classId) throws SQLException {
        String sql = "SELECT * FROM player_data WHERE class_id = ?";
        List<PlayerData> players = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, classId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    players.add(mapRowToPlayerData(rs));
                }
            }
        }

        return players;
    }

    /**
     * ResultSetをPlayerDataにマッピング
     */
    private PlayerData mapRowToPlayerData(ResultSet rs) throws SQLException {
        UUID uuid = UUID.fromString(rs.getString("uuid"));
        String username = rs.getString("username");
        String classId = rs.getString("class_id");
        int classRank = rs.getInt("class_rank");
        long firstJoin = rs.getLong("first_join");
        long lastLogin = rs.getLong("last_login");

        return new PlayerData(uuid, username, classId, classRank, firstJoin, lastLogin);
    }
}
