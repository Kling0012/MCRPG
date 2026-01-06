package com.example.rpgplugin.mythicmobs.drop;

import com.example.rpgplugin.storage.database.ConnectionPool;
import com.example.rpgplugin.storage.repository.IRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * MythicMobsドロップデータリポジトリ
 *
 * <p>ドロップデータのCRUD操作を担当します。
 * データベースとドロップハンドラーの橋渡し役です。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>リポジトリパターン: データアクセスの抽象化</li>
 *   <li>単一責任: ドロップデータの永続化のみ担当</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class DropRepository implements IRepository<DropData> {

    private final ConnectionPool connectionPool;
    private final Logger logger;

    /**
     * コンストラクタ
     *
     * @param connectionPool データベースコネクションプール
     * @param logger ロガー
     */
    public DropRepository(ConnectionPool connectionPool, Logger logger) {
        this.connectionPool = connectionPool;
        this.logger = logger;
    }

    /**
     * ドロップデータを保存します
     *
     * @param drop 保存するドロップデータ
     * @throws SQLException 保存失敗時
     */
    @Override
    public void save(DropData drop) throws SQLException {
        String sql = """
            INSERT INTO mythic_drops (player_uuid, mob_id, item_data, dropped_at, is_claimed, expires_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, drop.getPlayerUuid().toString());
            stmt.setString(2, drop.getMobId());
            stmt.setString(3, drop.getItemData());
            stmt.setLong(4, drop.getDroppedAt());
            stmt.setBoolean(5, drop.isClaimed());
            stmt.setObject(6, drop.getExpiresAt());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Saving drop failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    drop.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Saving drop failed, no ID obtained.");
                }
            }

            logger.fine("Drop saved: " + drop.getId());
        }
    }

    /**
     * IDでドロップデータを取得します
     *
     * @param id ドロップID
     * @return ドロップデータのOptional
     * @throws SQLException 取得失敗時
     */
    @Override
    public Optional<DropData> findById(int id) throws SQLException {
        String sql = "SELECT * FROM mythic_drops WHERE id = ?";

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDrop(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * プレイヤーの全ドロップデータを取得します
     *
     * @param playerUuid プレイヤーUUID
     * @return ドロップデータリスト
     * @throws SQLException 取得失敗時
     */
    public List<DropData> findByPlayer(UUID playerUuid) throws SQLException {
        String sql = "SELECT * FROM mythic_drops WHERE player_uuid = ? ORDER BY dropped_at DESC";
        List<DropData> drops = new ArrayList<>();

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    drops.add(mapResultSetToDrop(rs));
                }
            }
        }

        return drops;
    }

    /**
     * モブIDでドロップデータを取得します
     *
     * @param mobId モブID
     * @return ドロップデータリスト
     * @throws SQLException 取得失敗時
     */
    public List<DropData> findByMob(String mobId) throws SQLException {
        String sql = "SELECT * FROM mythic_drops WHERE mob_id = ? ORDER BY dropped_at DESC";
        List<DropData> drops = new ArrayList<>();

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, mobId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    drops.add(mapResultSetToDrop(rs));
                }
            }
        }

        return drops;
    }

    /**
     * 期限切れのドロップデータを取得します
     *
     * @return 期限切れドロップデータリスト
     * @throws SQLException 取得失敗時
     */
    public List<DropData> findExpired() throws SQLException {
        String sql = "SELECT * FROM mythic_drops WHERE expires_at IS NOT NULL AND expires_at <= ? AND is_claimed = 0";
        List<DropData> drops = new ArrayList<>();
        long currentTime = System.currentTimeMillis() / 1000;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, currentTime);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    drops.add(mapResultSetToDrop(rs));
                }
            }
        }

        return drops;
    }

    /**
     * ドロップデータを更新します
     *
     * @param drop 更新するドロップデータ
     * @throws SQLException 更新失敗時
     */
    @Override
    public void update(DropData drop) throws SQLException {
        String sql = """
            UPDATE mythic_drops
            SET is_claimed = ?, expires_at = ?
            WHERE id = ?
        """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, drop.isClaimed());
            stmt.setObject(2, drop.getExpiresAt());
            stmt.setInt(3, drop.getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Updating drop failed, no rows affected. Drop ID: " + drop.getId());
            }

            logger.fine("Drop updated: " + drop.getId());
        }
    }

    /**
     * ドロップデータを削除します
     *
     * @param drop 削除するドロップデータ
     * @throws SQLException 削除失敗時
     */
    @Override
    public void delete(DropData drop) throws SQLException {
        String sql = "DELETE FROM mythic_drops WHERE id = ?";

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, drop.getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Deleting drop failed, no rows affected. Drop ID: " + drop.getId());
            }

            logger.fine("Drop deleted: " + drop.getId());
        }
    }

    /**
     * プレイヤーの全ドロップデータを削除します
     *
     * @param playerUuid プレイヤーUUID
     * @throws SQLException 削除失敗時
     */
    public void deleteByPlayer(UUID playerUuid) throws SQLException {
        String sql = "DELETE FROM mythic_drops WHERE player_uuid = ?";

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUuid.toString());
            int affectedRows = stmt.executeUpdate();

            logger.fine("Deleted " + affectedRows + " drops for player: " + playerUuid);
        }
    }

    /**
     * 期限切れのドロップデータを一括削除します
     *
     * @return 削除した件数
     * @throws SQLException 削除失敗時
     */
    public int deleteExpired() throws SQLException {
        String sql = """
            DELETE FROM mythic_drops
            WHERE expires_at IS NOT NULL
            AND expires_at <= ?
            AND is_claimed = 1
            AND dropped_at <= ?
        """;
        long currentTime = System.currentTimeMillis() / 1000;
        long weekAgo = currentTime - (7 * 24 * 60 * 60); // 1週間前

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, currentTime);
            stmt.setLong(2, weekAgo);

            int affectedRows = stmt.executeUpdate();
            logger.info("Deleted " + affectedRows + " expired drops");

            return affectedRows;
        }
    }

    /**
     * ResultSetをDropDataオブジェクトにマッピングします
     *
     * @param rs ResultSet
     * @return DropDataオブジェクト
     * @throws SQLException マッピング失敗時
     */
    private DropData mapResultSetToDrop(ResultSet rs) throws SQLException {
        DropData drop = new DropData();
        drop.setId(rs.getInt("id"));
        drop.setPlayerUuid(UUID.fromString(rs.getString("player_uuid")));
        drop.setMobId(rs.getString("mob_id"));
        drop.setItemData(rs.getString("item_data"));
        drop.setDroppedAt(rs.getLong("dropped_at"));
        drop.setClaimed(rs.getBoolean("is_claimed"));

        long expiresAt = rs.getLong("expires_at");
        if (!rs.wasNull()) {
            drop.setExpiresAt(expiresAt);
        }

        return drop;
    }

    /**
     * 統計情報を取得します
     *
     * @return 総ドロップ件数
     * @throws SQLException 取得失敗時
     */
    public int getTotalDrops() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM mythic_drops";

        try (Connection conn = connectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        }

        return 0;
    }
}
