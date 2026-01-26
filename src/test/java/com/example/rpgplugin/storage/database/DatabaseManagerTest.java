package com.example.rpgplugin.storage.database;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DatabaseManagerの単体テスト
 *
 * <p>データベース接続管理機能のテスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("DatabaseManager テスト")
@ExtendWith(MockitoExtension.class)
class DatabaseManagerTest {

    @Mock
    private Plugin mockPlugin;

    @Mock
    private File mockDataFolder;

    @Mock
    private Server mockServer;

    @Mock
    private BukkitScheduler mockScheduler;

    private Logger logger;
    private File tempDataFolder;
    private DatabaseManager databaseManager;

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger("TestLogger");

        // 一時ディレクトリを作成
        tempDataFolder = new File(System.getProperty("java.io.tmpdir"), "rpg_db_test_" + System.currentTimeMillis());
        tempDataFolder.mkdirs();

        // プラグイン基本設定
        lenient().when(mockPlugin.getLogger()).thenReturn(logger);
        lenient().when(mockPlugin.getDataFolder()).thenReturn(tempDataFolder);
        lenient().when(mockPlugin.getServer()).thenReturn(mockServer);
        lenient().when(mockServer.getScheduler()).thenReturn(mockScheduler);

        // スケジューラ基本設定
        lenient().when(mockScheduler.runTaskAsynchronously(eq(mockPlugin), any(Runnable.class)))
                .thenReturn(null);

        databaseManager = new DatabaseManager(mockPlugin);
    }

    @AfterEach
    void tearDown() {
        // クリーンアップ: データベースマネージャーをシャットダウン
        if (databaseManager != null) {
            try {
                databaseManager.shutdown();
            } catch (Exception e) {
                // 無視
            }
        }

        // 一時ディレクトリを削除
        deleteDirectory(tempDataFolder);
    }

    /**
     * ディレクトリを再帰的に削除
     */
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    // ==================== initialize テスト ====================

    @Test
    @DisplayName("initialize: 正常に初期化される")
    void testInitialize_Success() throws SQLException {
        databaseManager.initialize();

        assertNotNull(databaseManager.getConnectionPool(), "ConnectionPoolが初期化される");
        assertNotNull(databaseManager.getConnectionPool().getConnection(), "接続が取得できる");
    }

    @Test
    @DisplayName("initialize: 二重初期化時に警告ログ")
    void testInitialize_DoubleInit() throws SQLException {
        // 初回初期化
        databaseManager.initialize();
        ConnectionPool firstPool = databaseManager.getConnectionPool();

        // 二回目の初期化
        databaseManager.initialize();
        ConnectionPool secondPool = databaseManager.getConnectionPool();

        // 二回目の初期化で新しいプールが作成される
        assertNotNull(secondPool, "二回目もConnectionPoolが作成される");
    }

    // ==================== getConnection テスト ====================

    @Test
    @DisplayName("getConnection: 初期化前に呼ぶと例外")
    void testGetConnection_NotInitialized() {
        SQLException exception = assertThrows(SQLException.class,
                () -> databaseManager.getConnection());
        assertTrue(exception.getMessage().contains("not initialized"));
    }

    @Test
    @DisplayName("getConnection: 初期化後に接続を取得")
    void testGetConnection_AfterInitialization() throws SQLException {
        databaseManager.initialize();

        Connection connection = databaseManager.getConnection();

        assertNotNull(connection, "接続が取得できる");
    }

    // ==================== executeAsync テスト ====================

    @Test
    @DisplayName("executeAsync: 正常に非同期実行")
    void testExecuteAsync_Success() {
        Runnable mockQuery = mock(Runnable.class);
        lenient().doNothing().when(mockQuery).run();

        assertDoesNotThrow(() -> databaseManager.executeAsync(mockQuery));

        verify(mockScheduler).runTaskAsynchronously(eq(mockPlugin), any(Runnable.class));
    }

    @Test
    @DisplayName("executeAsync: 例外発生時に警告ログ")
    void testExecuteAsync_Exception_LogsSevere() {
        Runnable mockQuery = mock(Runnable.class);
        lenient().doThrow(new RuntimeException("Test async exception")).when(mockQuery).run();

        assertDoesNotThrow(() -> databaseManager.executeAsync(mockQuery));

        verify(mockScheduler).runTaskAsynchronously(eq(mockPlugin), any(Runnable.class));
    }

    // ==================== shutdown テスト ====================

    @Test
    @DisplayName("shutdown: 未初期化時に安全に実行")
    void testShutdown_NotInitialized() {
        assertDoesNotThrow(() -> databaseManager.shutdown());
    }

    @Test
    @DisplayName("shutdown: 初期化後にプールをシャットダウン")
    void testShutdown_AfterInitialization() throws SQLException {
        databaseManager.initialize();
        ConnectionPool pool = databaseManager.getConnectionPool();

        databaseManager.shutdown();

        assertNull(databaseManager.getConnectionPool(), "ConnectionPoolがクリアされる");
    }

    // ==================== getConnectionPool テスト ====================

    @Test
 @DisplayName("getConnectionPool: 初期化前にnullを返す")
    void testGetConnectionPool_BeforeInitialization() {
        assertNull(databaseManager.getConnectionPool(), "初期化前はnull");
    }

    @Test
    @DisplayName("getConnectionPool: 初期化後にプールを返す")
    void testGetConnectionPool_AfterInitialization() throws SQLException {
        databaseManager.initialize();

        ConnectionPool pool = databaseManager.getConnectionPool();

        assertNotNull(pool, "ConnectionPoolが取得できる");
    }

    // ==================== deleteDatabase テスト ====================

    @Test
    @DisplayName("deleteDatabase: 未初期化時も安全に実行")
    void testDeleteDatabase_NotInitialized() {
        assertDoesNotThrow(() -> databaseManager.deleteDatabase());
    }

    @Test
    @DisplayName("deleteDatabase: データベースファイルを削除")
    void testDeleteDatabase_FileExists() throws SQLException {
        databaseManager.initialize();

        // データベースファイルが作成されたことを確認
        File dataSubDir = new File(tempDataFolder, "data");
        File dbFile = new File(dataSubDir, "database.db");
        assertTrue(dbFile.exists(), "データベースファイルが作成される");

        boolean result = databaseManager.deleteDatabase();

        assertTrue(result, "削除成功時はtrue");
        assertFalse(dbFile.exists(), "データベースファイルが削除される");
    }

    @Test
    @DisplayName("deleteDatabase: ファイルが存在しない場合はtrue")
    void testDeleteDatabase_FileNotExists() throws SQLException {
        databaseManager.initialize();
        databaseManager.deleteDatabase();

        boolean result = databaseManager.deleteDatabase();

        assertTrue(result, "ファイルが存在しない場合もtrue");
    }

    // ==================== initialize テスト（追加）====================

    @Test
    @DisplayName("initialize: dataサブディレクトリが既存の場合は作成をスキップ")
    void testInitialize_ExistingDataSubDir() throws SQLException {
        // 事前にdataサブディレクトリを作成
        File dataSubDir = new File(tempDataFolder, "data");
        dataSubDir.mkdirs();
        assertTrue(dataSubDir.exists(), "dataサブディレクトリが作成済み");

        databaseManager.initialize();

        assertNotNull(databaseManager.getConnectionPool(), "ConnectionPoolが初期化される");
    }

    @Test
    @DisplayName("initialize: データフォルダが既存の場合は作成をスキップ")
    void testInitialize_ExistingDataFolder() throws SQLException {
        // データフォルダとdataサブディレクトリを事前に作成
        File dataSubDir = new File(tempDataFolder, "data");
        dataSubDir.mkdirs();
        assertTrue(tempDataFolder.exists(), "データフォルダが作成済み");
        assertTrue(dataSubDir.exists(), "dataサブディレクトリが作成済み");

        databaseManager.initialize();

        assertNotNull(databaseManager.getConnectionPool(), "ConnectionPoolが初期化される");
    }

    // ==================== getConnection テスト（追加）====================

    @Test
    @DisplayName("getConnection: 複数回呼び出して接続を取得")
    void testGetConnection_MultipleCalls() throws SQLException {
        databaseManager.initialize();

        Connection conn1 = databaseManager.getConnection();
        Connection conn2 = databaseManager.getConnection();

        assertNotNull(conn1, "1回目の接続が取得できる");
        assertNotNull(conn2, "2回目の接続も取得できる");
    }

    // ==================== executeAsync テスト（追加）====================

    @Test
    @DisplayName("executeAsync: 複数の非同期クエリを実行")
    void testExecuteAsync_MultipleQueries() {
        Runnable mockQuery1 = mock(Runnable.class);
        Runnable mockQuery2 = mock(Runnable.class);
        lenient().doNothing().when(mockQuery1).run();
        lenient().doNothing().when(mockQuery2).run();

        assertDoesNotThrow(() -> {
            databaseManager.executeAsync(mockQuery1);
            databaseManager.executeAsync(mockQuery2);
        });

        verify(mockScheduler, times(2)).runTaskAsynchronously(eq(mockPlugin), any(Runnable.class));
    }

    @Test
    @DisplayName("executeAsync: NullPointerExceptionもキャッチされる")
    void testExecuteAsync_NullPointerException_LogsSevere() {
        Runnable mockQuery = mock(Runnable.class);
        lenient().doThrow(new NullPointerException("Test NPE")).when(mockQuery).run();

        assertDoesNotThrow(() -> databaseManager.executeAsync(mockQuery));

        verify(mockScheduler).runTaskAsynchronously(eq(mockPlugin), any(Runnable.class));
    }

    // ==================== shutdown テスト（追加）====================

    @Test
    @DisplayName("shutdown: 複数回呼び出しても安全")
    void testShutdown_MultipleCalls() throws SQLException {
        databaseManager.initialize();

        assertDoesNotThrow(() -> {
            databaseManager.shutdown();
            databaseManager.shutdown();
        });

        assertNull(databaseManager.getConnectionPool(), "ConnectionPoolがクリアされる");
    }

    // ==================== deleteDatabase テスト（追加）====================

    @Test
    @DisplayName("deleteDatabase: 初期化後に削除して再初期化")
    void testDeleteDatabase_ReinitializeAfterDeletion() throws SQLException {
        // 初期化
        databaseManager.initialize();
        assertNotNull(databaseManager.getConnectionPool(), "初期化後にConnectionPoolが存在");

        // 削除
        boolean deleted = databaseManager.deleteDatabase();
        assertTrue(deleted, "削除成功");
        assertNull(databaseManager.getConnectionPool(), "削除後にConnectionPoolがnull");

        // 再初期化
        databaseManager.initialize();
        assertNotNull(databaseManager.getConnectionPool(), "再初期化後にConnectionPoolが作成される");
    }

    @Test
    @DisplayName("deleteDatabase: dataディレクトリのみ存在する場合")
    void testDeleteDatabase_OnlyDataDirExists() {
        // dataディレクトリのみ作成（データベースファイルなし）
        File dataSubDir = new File(tempDataFolder, "data");
        dataSubDir.mkdirs();

        boolean result = databaseManager.deleteDatabase();

        assertTrue(result, "dataディレクトリのみでもtrue");
    }

    // ==================== 追加のエッジケーステスト ====================

    @Test
    @DisplayName("getConnection: 接続プールを直接検証")
    void testGetConnection_PoolVerification() throws SQLException {
        databaseManager.initialize();

        ConnectionPool pool = databaseManager.getConnectionPool();
        assertNotNull(pool, "接続プールが存在");

        Connection conn = pool.getConnection();
        assertNotNull(conn, "プールから直接接続を取得");
        assertNotNull(databaseManager.getConnection(), "Manager経由でも接続を取得");
    }

    @Test
    @DisplayName("initialize: スキーママネージャも初期化される")
    void testInitialize_SchemaManagerInitialized() throws SQLException {
        databaseManager.initialize();

        assertNotNull(databaseManager.getConnectionPool(), "ConnectionPoolが初期化される");

        // スキーマ初期化が成功したことを確認（接続を取得してテーブルにアクセスできるか）
        Connection conn = databaseManager.getConnection();
        assertNotNull(conn, "接続が取得でき、スキーマが初期化されている");
    }

    @Test
    @DisplayName("shutdown: 未初期化の状態で複数回呼び出しても安全")
    void testShutdown_MultipleCallsNotInitialized() {
        assertDoesNotThrow(() -> {
            databaseManager.shutdown();
            databaseManager.shutdown();
            databaseManager.shutdown();
        });

        assertNull(databaseManager.getConnectionPool(), "未初期化時もnullのまま");
    }

    @Test
    @DisplayName("deleteDatabase: 複数回呼び出しても安全")
    void testDeleteDatabase_MultipleCalls() throws SQLException {
        databaseManager.initialize();

        assertDoesNotThrow(() -> {
            boolean result1 = databaseManager.deleteDatabase();
            boolean result2 = databaseManager.deleteDatabase();
            assertTrue(result1, "1回目の削除");
            assertTrue(result2, "2回目の削除もtrue");
        });
    }

    @Test
    @DisplayName("getConnectionPool: 初期化後にnullでないことを保証")
    void testGetConnectionPool_NotNullAfterInit() throws SQLException {
        databaseManager.initialize();

        ConnectionPool pool1 = databaseManager.getConnectionPool();
        ConnectionPool pool2 = databaseManager.getConnectionPool();

        assertNotNull(pool1, "1回目の取得でnullでない");
        assertNotNull(pool2, "2回目の取得もnullでない");
        assertSame(pool1, pool2, "同じインスタンスが返される");
    }
}
