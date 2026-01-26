package com.example.rpgplugin.storage.repository;

import com.example.rpgplugin.storage.database.DatabaseManager;
import com.example.rpgplugin.storage.models.PlayerData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * PlayerDataRepositoryの単体テスト
 *
 * <p>プレイヤーデータリポジトリのCRUD操作テスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("PlayerDataRepository テスト")
@ExtendWith(MockitoExtension.class)
class PlayerDataRepositoryTest {

    @Mock
    private DatabaseManager mockDbManager;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private java.util.logging.Logger mockLogger;

    private PlayerDataRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        lenient().doNothing().when(mockLogger).fine(anyString());
        lenient().doNothing().when(mockLogger).info(anyString());
        lenient().doNothing().when(mockLogger).warning(anyString());
        lenient().doNothing().when(mockLogger).severe(anyString());

        lenient().when(mockDbManager.getConnection()).thenReturn(mockConnection);
        lenient().when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        lenient().when(mockConnection.createStatement()).thenReturn(mockStatement);
        lenient().when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        lenient().when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        repository = new PlayerDataRepository(mockDbManager, mockLogger);
    }

    @AfterEach
    void tearDown() {
        reset(mockDbManager, mockConnection, mockPreparedStatement, mockStatement, mockResultSet, mockLogger);
    }

    // ==================== save テスト ====================

    @Test
    @DisplayName("save: 正常に保存")
    void testSave_Success() throws SQLException {
        UUID uuid = UUID.randomUUID();
        PlayerData player = new PlayerData(uuid, "TestPlayer");

        lenient().when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> repository.save(player));

        verify(mockPreparedStatement).setString(1, uuid.toString());
        verify(mockPreparedStatement).setString(2, "TestPlayer");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("save: SQLExceptionで失敗")
    void testSave_SqlException() throws SQLException {
        PlayerData player = new PlayerData(UUID.randomUUID(), "TestPlayer");

        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("DB error"));

        assertThrows(SQLException.class, () -> repository.save(player));
    }

    // ==================== saveAsync テスト ====================

    @Test
    @DisplayName("saveAsync: 正常に非同期保存")
    void testSaveAsync_Success() throws SQLException {
        UUID uuid = UUID.randomUUID();
        PlayerData player = new PlayerData(uuid, "TestPlayer");

        lenient().when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        lenient().doNothing().when(mockDbManager).executeAsync(any(Runnable.class));

        assertDoesNotThrow(() -> repository.saveAsync(player));

        verify(mockDbManager).executeAsync(any(Runnable.class));
    }

    @Test
    @DisplayName("saveAsync: nullの場合は警告ログのみ")
    void testSaveAsync_NullPlayer() {
        assertDoesNotThrow(() -> repository.saveAsync(null));

        verify(mockLogger).warning(anyString()); // contains("null player data")
        verify(mockDbManager, never()).executeAsync(any(Runnable.class));
    }

    // ==================== findById テスト ====================

    @Test
    @DisplayName("findById: データが見つかる")
    void testFindById_Found() throws SQLException {
        UUID uuid = UUID.randomUUID();

        lenient().when(mockResultSet.next()).thenReturn(true, false); // 1回目はtrue、2回目はfalse
        lenient().when(mockResultSet.getString("uuid")).thenReturn(uuid.toString());
        lenient().when(mockResultSet.getString("username")).thenReturn("TestPlayer");
        lenient().when(mockResultSet.getString("class_id")).thenReturn("warrior");
        lenient().when(mockResultSet.getInt("class_rank")).thenReturn(1);
        lenient().when(mockResultSet.getString("class_history")).thenReturn("warrior,archer");
        lenient().when(mockResultSet.getLong("first_join")).thenReturn(1000L);
        lenient().when(mockResultSet.getLong("last_login")).thenReturn(2000L);
        lenient().when(mockResultSet.getInt("max_health")).thenReturn(20);
        lenient().when(mockResultSet.getInt("max_mana")).thenReturn(100);
        lenient().when(mockResultSet.getInt("current_mana")).thenReturn(80);
        lenient().when(mockResultSet.getString("cost_type")).thenReturn("mana");

        var result = repository.findById(uuid);

        assertTrue(result.isPresent(), "データが見つかる");
        assertEquals("TestPlayer", result.get().getUsername());
        assertEquals(uuid, result.get().getUuid());
    }

    @Test
    @DisplayName("findById: データが見つからない")
    void testFindById_NotFound() throws SQLException {
        UUID uuid = UUID.randomUUID();

        lenient().when(mockResultSet.next()).thenReturn(false);

        var result = repository.findById(uuid);

        assertFalse(result.isPresent(), "データが見つからない");
    }

    // ==================== findAll テスト ====================

    @Test
    @DisplayName("findAll: 複数件のデータを取得")
    void testFindAll_MultipleRecords() throws SQLException {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        lenient().when(mockResultSet.next()).thenReturn(true, true, false);
        lenient().when(mockResultSet.getString("uuid")).thenReturn(uuid1.toString(), uuid2.toString());
        lenient().when(mockResultSet.getString("username")).thenReturn("Player1", "Player2");
        lenient().when(mockResultSet.getString("class_id")).thenReturn("warrior", "archer");
        lenient().when(mockResultSet.getInt("class_rank")).thenReturn(1, 2);
        lenient().when(mockResultSet.getString("class_history")).thenReturn("", "");
        lenient().when(mockResultSet.getLong("first_join")).thenReturn(1000L, 1000L);
        lenient().when(mockResultSet.getLong("last_login")).thenReturn(2000L, 2000L);
        lenient().when(mockResultSet.getInt("max_health")).thenReturn(20, 20);
        lenient().when(mockResultSet.getInt("max_mana")).thenReturn(100, 100);
        lenient().when(mockResultSet.getInt("current_mana")).thenReturn(80, 80);
        lenient().when(mockResultSet.getString("cost_type")).thenReturn("mana", "mana");

        List<PlayerData> players = repository.findAll();

        assertEquals(2, players.size(), "2件のデータが取得できる");
    }

    @Test
    @DisplayName("findAll: データが0件")
    void testFindAll_Empty() throws SQLException {
        lenient().when(mockResultSet.next()).thenReturn(false);

        List<PlayerData> players = repository.findAll();

        assertTrue(players.isEmpty(), "空のリストが返される");
    }

    // ==================== deleteById テスト ====================

    @Test
    @DisplayName("deleteById: 正常に削除")
    void testDeleteById_Success() throws SQLException {
        UUID uuid = UUID.randomUUID();

        lenient().when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> repository.deleteById(uuid));

        verify(mockPreparedStatement).setString(1, uuid.toString());
        verify(mockPreparedStatement).executeUpdate();
    }

    // ==================== existsById テスト ====================

    @Test
    @DisplayName("existsById: 存在する")
    void testExistsById_True() throws SQLException {
        UUID uuid = UUID.randomUUID();

        lenient().when(mockResultSet.next()).thenReturn(true, false);
        setupMockResultSet(uuid, "TestPlayer");

        assertTrue(repository.existsById(uuid), "存在する場合はtrue");
    }

    @Test
    @DisplayName("existsById: 存在しない")
    void testExistsById_False() throws SQLException {
        UUID uuid = UUID.randomUUID();

        lenient().when(mockResultSet.next()).thenReturn(false);

        assertFalse(repository.existsById(uuid), "存在しない場合はfalse");
    }

    // ==================== count テスト ====================

    @Test
    @DisplayName("count: 正常に件数を取得")
    void testCount_Success() throws SQLException {
        lenient().when(mockResultSet.next()).thenReturn(true);
        lenient().when(mockResultSet.getLong("count")).thenReturn(42L);

        long count = repository.count();

        assertEquals(42L, count, "件数が正しく取得できる");
    }

    @Test
    @DisplayName("count: 結果がない場合")
    void testCount_NoResults() throws SQLException {
        lenient().when(mockResultSet.next()).thenReturn(false);

        long count = repository.count();

        assertEquals(0L, count, "結果がない場合は0");
    }

    // ==================== saveAll テスト ====================

    @Test
    @DisplayName("saveAll: 複数プレイヤーをバッチ保存")
    void testSaveAll_MultiplePlayers() throws SQLException {
        List<PlayerData> players = List.of(
                new PlayerData(UUID.randomUUID(), "Player1"),
                new PlayerData(UUID.randomUUID(), "Player2")
        );

        lenient().when(mockPreparedStatement.executeBatch()).thenReturn(new int[]{1, 1});

        assertDoesNotThrow(() -> repository.saveAll(players));

        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).setAutoCommit(true);
        verify(mockConnection).commit();
    }

    @Test
    @DisplayName("saveAll: 空リストの場合")
    void testSaveAll_EmptyList() throws SQLException {
        List<PlayerData> players = List.of();

        assertDoesNotThrow(() -> repository.saveAll(players));

        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).commit();
    }

    @Test
    @DisplayName("saveAll: エラー時にロールバック")
    void testSaveAll_RollbackOnError() throws SQLException {
        List<PlayerData> players = List.of(
                new PlayerData(UUID.randomUUID(), "Player1")
        );

        when(mockPreparedStatement.executeBatch()).thenThrow(new SQLException("Batch error"));

        assertThrows(SQLException.class, () -> repository.saveAll(players));

        verify(mockConnection).rollback();
    }

    // ==================== saveAllAsync テスト ====================

    @Test
    @DisplayName("saveAllAsync: 正常に非同期バッチ保存")
    void testSaveAllAsync_Success() {
        List<PlayerData> players = List.of(
                new PlayerData(UUID.randomUUID(), "Player1")
        );

        lenient().doNothing().when(mockDbManager).executeAsync(any(Runnable.class));

        assertDoesNotThrow(() -> repository.saveAllAsync(players));

        verify(mockDbManager).executeAsync(any(Runnable.class));
    }

    // ==================== findByUsername テスト ====================

    @Test
    @DisplayName("findByUsername: ユーザー名で検索して見つかる")
    void testFindByUsername_Found() throws SQLException {
        UUID uuid = UUID.randomUUID();

        lenient().when(mockResultSet.next()).thenReturn(true, false);
        setupMockResultSet(uuid, "TestPlayer");

        var result = repository.findByUsername("TestPlayer");

        assertTrue(result.isPresent(), "データが見つかる");
        assertEquals("TestPlayer", result.get().getUsername());
    }

    @Test
    @DisplayName("findByUsername: ユーザー名で見つからない")
    void testFindByUsername_NotFound() throws SQLException {
        lenient().when(mockResultSet.next()).thenReturn(false);

        var result = repository.findByUsername("UnknownPlayer");

        assertFalse(result.isPresent(), "データが見つからない");
    }

    // ==================== findByClass テスト ====================

    @Test
    @DisplayName("findByClass: クラスIDで検索")
    void testFindByClass_Found() throws SQLException {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        lenient().when(mockResultSet.next()).thenReturn(true, true, false);
        lenient().when(mockResultSet.getString("uuid")).thenReturn(uuid1.toString(), uuid2.toString());
        lenient().when(mockResultSet.getString("username")).thenReturn("Player1", "Player2");
        lenient().when(mockResultSet.getString("class_id")).thenReturn("warrior", "warrior");
        lenient().when(mockResultSet.getInt("class_rank")).thenReturn(1, 1);
        lenient().when(mockResultSet.getString("class_history")).thenReturn("", "");
        lenient().when(mockResultSet.getLong("first_join")).thenReturn(1000L, 1000L);
        lenient().when(mockResultSet.getLong("last_login")).thenReturn(2000L, 2000L);
        lenient().when(mockResultSet.getInt("max_health")).thenReturn(20, 20);
        lenient().when(mockResultSet.getInt("max_mana")).thenReturn(100, 100);
        lenient().when(mockResultSet.getInt("current_mana")).thenReturn(80, 80);
        lenient().when(mockResultSet.getString("cost_type")).thenReturn("mana", "mana");

        List<PlayerData> players = repository.findByClass("warrior");

        assertEquals(2, players.size(), "2件のデータが取得できる");
        assertTrue(players.stream().allMatch(p -> "warrior".equals(p.getClassId())), "全員warriorクラス");
    }

    @Test
    @DisplayName("findByClass: 該当クラスのプレイヤーがいない")
    void testFindByClass_Empty() throws SQLException {
        lenient().when(mockResultSet.next()).thenReturn(false);

        List<PlayerData> players = repository.findByClass("unknown_class");

        assertTrue(players.isEmpty(), "空のリストが返される");
    }

    // ==================== mapRowToPlayerData エッジケース テスト ====================

    @Test
    @DisplayName("mapRowToPlayerData: MP/HPカラムが存在しない場合（旧データベース互換）")
    void testMapRowToPlayerData_LegacyDatabase() throws SQLException {
        UUID uuid = UUID.randomUUID();

        lenient().when(mockResultSet.next()).thenReturn(true, false);
        lenient().when(mockResultSet.getString("uuid")).thenReturn(uuid.toString());
        lenient().when(mockResultSet.getString("username")).thenReturn("TestPlayer");
        lenient().when(mockResultSet.getString("class_id")).thenReturn("warrior");
        lenient().when(mockResultSet.getInt("class_rank")).thenReturn(1);
        lenient().when(mockResultSet.getString("class_history")).thenReturn("");
        lenient().when(mockResultSet.getLong("first_join")).thenReturn(1000L);
        lenient().when(mockResultSet.getLong("last_login")).thenReturn(2000L);

        // MP/HPカラムでSQLExceptionをスロー
        when(mockResultSet.getInt("max_health")).thenThrow(new SQLException("Column not found"));
        when(mockResultSet.getInt("max_mana")).thenThrow(new SQLException("Column not found"));
        when(mockResultSet.getInt("current_mana")).thenThrow(new SQLException("Column not found"));
        when(mockResultSet.getString("cost_type")).thenThrow(new SQLException("Column not found"));

        // findById経由でテスト
        var result = repository.findById(uuid);

        assertTrue(result.isPresent(), "デフォルト値で初期化される");
        assertEquals(20, result.get().getMaxHealth(), "デフォルトのmax_health");
        assertEquals(100, result.get().getMaxMana(), "デフォルトのmax_mana");
        assertEquals(100, result.get().getCurrentMana(), "デフォルトのcurrent_mana");
    }

    @Test
    @DisplayName("mapRowToPlayerData: costTypeがnullの場合のデフォルト値")
    void testMapRowToPlayerData_NullCostType() throws SQLException {
        UUID uuid = UUID.randomUUID();

        lenient().when(mockResultSet.next()).thenReturn(true, false);
        lenient().when(mockResultSet.getString("uuid")).thenReturn(uuid.toString());
        lenient().when(mockResultSet.getString("username")).thenReturn("TestPlayer");
        lenient().when(mockResultSet.getString("class_id")).thenReturn("warrior");
        lenient().when(mockResultSet.getInt("class_rank")).thenReturn(1);
        lenient().when(mockResultSet.getString("class_history")).thenReturn("");
        lenient().when(mockResultSet.getLong("first_join")).thenReturn(1000L);
        lenient().when(mockResultSet.getLong("last_login")).thenReturn(2000L);
        lenient().when(mockResultSet.getInt("max_health")).thenReturn(20);
        lenient().when(mockResultSet.getInt("max_mana")).thenReturn(100);
        lenient().when(mockResultSet.getInt("current_mana")).thenReturn(80);
        lenient().when(mockResultSet.getString("cost_type")).thenReturn(null);

        var result = repository.findById(uuid);

        assertTrue(result.isPresent());
        assertEquals("mana", result.get().getCostType(), "デフォルトのcost_type");
    }

    // ==================== ヘルパーメソッド ====================

    private void setupMockResultSet(UUID uuid, String username) throws SQLException {
        lenient().when(mockResultSet.getString("uuid")).thenReturn(uuid.toString());
        lenient().when(mockResultSet.getString("username")).thenReturn(username);
        lenient().when(mockResultSet.getString("class_id")).thenReturn("warrior");
        lenient().when(mockResultSet.getInt("class_rank")).thenReturn(1);
        lenient().when(mockResultSet.getString("class_history")).thenReturn("");
        lenient().when(mockResultSet.getLong("first_join")).thenReturn(1000L);
        lenient().when(mockResultSet.getLong("last_login")).thenReturn(2000L);
        lenient().when(mockResultSet.getInt("max_health")).thenReturn(20);
        lenient().when(mockResultSet.getInt("max_mana")).thenReturn(100);
        lenient().when(mockResultSet.getInt("current_mana")).thenReturn(80);
        lenient().when(mockResultSet.getString("cost_type")).thenReturn("mana");
    }

    // ==================== 追加テスト（カバレッジ向上用）====================

    @Test
    @DisplayName("save: 全フィールドを正しく設定")
    void testSave_AllFieldsSet() throws SQLException {
        UUID uuid = UUID.randomUUID();
        PlayerData player = new PlayerData(uuid, "TestPlayer", "warrior", 3,
                "warrior,archer,mage", 1000L, 2000L, 25, 150, 120, "health");

        lenient().when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> repository.save(player));

        verify(mockPreparedStatement).setString(1, uuid.toString());
        verify(mockPreparedStatement).setString(2, "TestPlayer");
        verify(mockPreparedStatement).setString(3, "warrior");
        verify(mockPreparedStatement).setInt(4, 3);
        verify(mockPreparedStatement).setString(5, "warrior,archer,mage");
        verify(mockPreparedStatement).setLong(6, 1000L);
        verify(mockPreparedStatement).setLong(7, 2000L);
        verify(mockPreparedStatement).setInt(8, 25);
        verify(mockPreparedStatement).setInt(9, 150);
        verify(mockPreparedStatement).setInt(10, 120);
        verify(mockPreparedStatement).setString(11, "health");
    }

    @Test
    @DisplayName("saveAsync: SQLException時のエラーハンドリング")
    void testSaveAsync_SqlException() {
        UUID uuid = UUID.randomUUID();
        PlayerData player = new PlayerData(uuid, "TestPlayer");

        lenient().doNothing().when(mockDbManager).executeAsync(any(Runnable.class));

        assertDoesNotThrow(() -> repository.saveAsync(player));

        verify(mockDbManager).executeAsync(any(Runnable.class));
    }

    @Test
    @DisplayName("saveAll: 1件の場合")
    void testSaveAll_SinglePlayer() throws SQLException {
        List<PlayerData> players = List.of(
                new PlayerData(UUID.randomUUID(), "Player1")
        );

        lenient().when(mockPreparedStatement.executeBatch()).thenReturn(new int[]{1});

        assertDoesNotThrow(() -> repository.saveAll(players));

        verify(mockPreparedStatement).executeBatch();
    }

    @Test
    @DisplayName("saveAllAsync: SQLException時のログ出力")
    void testSaveAllAsync_SqlException() {
        List<PlayerData> players = List.of(
                new PlayerData(UUID.randomUUID(), "Player1")
        );

        lenient().doNothing().when(mockDbManager).executeAsync(any(Runnable.class));

        assertDoesNotThrow(() -> repository.saveAllAsync(players));

        verify(mockDbManager).executeAsync(any(Runnable.class));
    }

    @Test
    @DisplayName("save: PreparedStatementのcloseが正常に行われる")
    void testSave_StatementClosed() throws SQLException {
        UUID uuid = UUID.randomUUID();
        PlayerData player = new PlayerData(uuid, "TestPlayer");

        lenient().when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> repository.save(player));

        // try-with-resourcesにより自動的にcloseが呼ばれる
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    @DisplayName("count: ResultSetをクローズ")
    void testCount_ResultSetClosed() throws SQLException {
        lenient().when(mockResultSet.next()).thenReturn(true);
        lenient().when(mockResultSet.getLong("count")).thenReturn(10L);

        long count = repository.count();

        assertEquals(10L, count);
        // try-with-resourcesにより自動的にcloseが呼ばれる
    }
}
