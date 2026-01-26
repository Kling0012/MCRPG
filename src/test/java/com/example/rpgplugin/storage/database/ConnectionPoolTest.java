package com.example.rpgplugin.storage.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * ConnectionPoolの単体テスト
 *
 * <p>コネクションプール機能のテスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("ConnectionPool テスト")
@ExtendWith(MockitoExtension.class)
class ConnectionPoolTest {

    @Mock
    private Connection mockConnection;

    private ConnectionPool connectionPool;
    private ConnectionPool.ConnectionFactory connectionFactory;
    private AtomicInteger connectionCounter;

    @BeforeEach
    void setUp() throws SQLException {
        connectionCounter = new AtomicInteger(0);
        connectionFactory = () -> {
            connectionCounter.incrementAndGet();
            return mockConnection;
        };

        // モックの基本設定
        lenient().when(mockConnection.isClosed()).thenReturn(false);
        lenient().when(mockConnection.isValid(anyInt())).thenReturn(true);
        lenient().when(mockConnection.createStatement()).thenReturn(mock(Statement.class));
    }

    @AfterEach
    void tearDown() {
        if (connectionPool != null) {
            connectionPool.shutdown();
        }
    }

    // ==================== コンストラクタ テスト ====================

    @Test
    @DisplayName("コンストラクタ: デフォルト設定で初期化")
    void testConstructor_DefaultSettings() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory);

        assertNotNull(connectionPool, "ConnectionPoolが作成される");
        assertNotNull(connectionPool.getConnection(), "接続が取得できる");
    }

    @Test
    @DisplayName("コンストラクタ: カスタム設定で初期化")
    void testConstructor_CustomSettings() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 5, 10);

        assertNotNull(connectionPool, "ConnectionPoolが作成される");
        assertNotNull(connectionPool.getConnection(), "接続が取得できる");
    }

    // ==================== getConnection テスト ====================

    @Test
    @DisplayName("getConnection: 新規接続を作成")
    void testGetConnection_NewConnection() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 2, 1);

        Connection conn1 = connectionPool.getConnection();
        Connection conn2 = connectionPool.getConnection();

        assertNotNull(conn1, "1回目の接続が取得できる");
        assertNotNull(conn2, "2回目の接続も取得できる");
        assertEquals(2, connectionCounter.get(), "2つの接続が作成される");
    }

    @Test
    @DisplayName("getConnection: プールから接続を再利用")
    void testGetConnection_ReuseConnection() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 2, 1);

        Connection conn1 = connectionPool.getConnection();
        conn1.close(); // プールに返却

        Connection conn2 = connectionPool.getConnection();

        assertNotNull(conn2, "返却された接続を再利用できる");
        assertEquals(1, connectionCounter.get(), "新しい接続は作成されない");
    }

    @Test
    @DisplayName("getConnection: 無効な接続はスキップされる")
    void testGetConnection_InvalidConnectionSkipped() throws SQLException {
        lenient().when(mockConnection.isValid(anyInt())).thenReturn(false);

        connectionPool = new ConnectionPool(connectionFactory, 2, 1);

        Connection conn = connectionPool.getConnection();

        assertNotNull(conn, "無効な接続がスキップされ、新しい接続が取得できる");
    }

    @Test
    @DisplayName("getConnection: シャットダウン後に呼ぶと例外")
    void testGetConnection_AfterShutdown() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory);
        connectionPool.shutdown();

        SQLException exception = assertThrows(SQLException.class,
                () -> connectionPool.getConnection());

        assertTrue(exception.getMessage().contains("shutdown"), "シャットダウンエラー");
    }

    // ==================== releaseConnection テスト ====================

    @Test
    @DisplayName("releaseConnection: 正常にプールに返却")
    void testReleaseConnection_Success() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn1 = connectionPool.getConnection();
        conn1.close(); // releaseConnectionが呼ばれる

        Connection conn2 = connectionPool.getConnection();

        assertSame(conn1, conn2, "同じ接続が再利用される");
    }

    @Test
    @DisplayName("releaseConnection: シャットダウン後は接続をクローズ")
    void testReleaseConnection_AfterShutdown() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();
        connectionPool.shutdown();
        conn.close(); // エラーにならずに処理される

        verify(mockConnection, atLeastOnce()).close();
    }

    // ==================== shutdown テスト ====================

    @Test
    @DisplayName("shutdown: 複数回呼び出しても安全")
    void testShutdown_MultipleCalls() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory);

        assertDoesNotThrow(() -> {
            connectionPool.shutdown();
            connectionPool.shutdown();
        });
    }

    @Test
    @DisplayName("shutdown: 接続を全てクローズ")
    void testShutdown_ClosesAllConnections() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 3, 1);

        Connection conn1 = connectionPool.getConnection();
        Connection conn2 = connectionPool.getConnection();
        Connection conn3 = connectionPool.getConnection();

        // 接続を返却
        conn1.close();
        conn2.close();
        conn3.close();

        connectionPool.shutdown();

        // 実際のクローズが呼ばれたことを確認
        verify(mockConnection, atLeastOnce()).close();
    }

    // ==================== PooledConnection テスト ====================

    @Test
    @DisplayName("PooledConnection: closeでプールに返却")
    void testPooledConnection_CloseReturnsToPool() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn1 = connectionPool.getConnection();
        conn1.close(); // プールに返却

        Connection conn2 = connectionPool.getConnection();

        assertSame(conn1, conn2, "同じ接続が返される");
    }

    @Test
    @DisplayName("PooledConnection: isClosedはプール状態を反映")
    void testPooledConnection_IsClosed() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertFalse(conn.isClosed(), "使用中はfalse");
        conn.close();
        assertTrue(conn.isClosed(), "返却後はtrue");
    }

    @Test
    @DisplayName("PooledConnection: closeで実際にクローズされない")
    void testPooledConnection_CloseDoesNotCloseActual() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();
        conn.close(); // プールに返却

        // 実際の接続はクローズされていない
        verify(mockConnection, never()).close();
    }

    @Test
    @DisplayName("PooledConnection: shutdown後は実際にクローズされる")
    void testPooledConnection_CloseAfterShutdown() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();
        connectionPool.shutdown();
        conn.close(); // 実際にクローズ

        verify(mockConnection, atLeastOnce()).close();
    }

    @Test
    @DisplayName("PooledConnection: delegateメソッドが正常に動作")
    void testPooledConnection_DelegateMethods() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.createStatement();
            conn.prepareStatement("SELECT 1");
            conn.getAutoCommit();
            conn.getTransactionIsolation();
        });

        verify(mockConnection, atLeastOnce()).createStatement();
        verify(mockConnection, atLeastOnce()).prepareStatement(anyString());
    }

    @Test
    @DisplayName("PooledConnection: isValidで接続検証")
    void testPooledConnection_IsValid() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertTrue(((ConnectionPool.PooledConnection) conn).isValid(), "有効な接続");
    }

    // ==================== タイムアウト/同時実行 テスト ====================

    @Test
    @DisplayName("getConnection: プールが満杯でも待機して接続を取得")
    void testGetConnection_WaitWhenPoolFull() throws SQLException, InterruptedException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 2);

        Connection conn1 = connectionPool.getConnection();

        // 別スレッドで接続を返却
        Thread returnThread = new Thread(() -> {
            try {
                Thread.sleep(100);
                conn1.close();
            } catch (Exception e) {
                // 無視
            }
        });
        returnThread.start();

        // タイムアウト内に接続を取得できる
        long startTime = System.currentTimeMillis();
        Connection conn2 = connectionPool.getConnection();
        long elapsed = System.currentTimeMillis() - startTime;

        assertNotNull(conn2, "接続が取得できる");
        assertTrue(elapsed < 2000, "タイムアウト前に取得できる");

        returnThread.join();
    }

    @Test
    @DisplayName("getConnection: 最大プールサイズまで接続を作成")
    void testGetConnection_MaxPoolSize() throws SQLException {
        int maxPoolSize = 3;
        connectionPool = new ConnectionPool(connectionFactory, maxPoolSize, 1);

        Connection conn1 = connectionPool.getConnection();
        Connection conn2 = connectionPool.getConnection();
        Connection conn3 = connectionPool.getConnection();

        assertEquals(maxPoolSize, connectionCounter.get(), "最大サイズまで接続が作成される");
    }

    // ==================== エッジケース テスト ====================

    @Test
    @DisplayName("PooledConnection: closeActualで実際にクローズ")
    void testPooledConnection_CloseActual() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();
        ConnectionPool.PooledConnection pooledConn = (ConnectionPool.PooledConnection) conn;

        assertDoesNotThrow(() -> pooledConn.closeActual());

        verify(mockConnection, atLeastOnce()).close();
    }

    @Test
    @DisplayName("PooledConnection: resetClosedで状態をリセット")
    void testPooledConnection_ResetClosed() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();
        conn.close();
        assertTrue(conn.isClosed(), "返却後にisClosed");

        // 再取得でリセットされる
        Connection conn2 = connectionPool.getConnection();
        assertSame(conn, conn2, "同じ接続");
        assertFalse(conn.isClosed(), "再取得後にリセットされる");
    }

    // ==================== PooledConnection 追加テスト ====================

    @Test
    @DisplayName("PooledConnection: prepareStatementが正常に動作")
    void testPooledConnection_PrepareStatement() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.prepareStatement("SELECT * FROM test");
            conn.prepareStatement("INSERT INTO test VALUES (?)");
            conn.prepareStatement("UPDATE test SET x = ?", new int[]{1});
        });

        verify(mockConnection).prepareStatement("SELECT * FROM test");
        verify(mockConnection).prepareStatement("INSERT INTO test VALUES (?)");
        verify(mockConnection).prepareStatement("UPDATE test SET x = ?", new int[]{1});
    }

    @Test
    @DisplayName("PooledConnection: createStatementが正常に動作")
    void testPooledConnection_CreateStatement() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.createStatement();
            conn.createStatement(1005, 1007);
            conn.createStatement(1005, 1007, 1);
        });

        verify(mockConnection).createStatement();
        verify(mockConnection).createStatement(1005, 1007);
        verify(mockConnection).createStatement(1005, 1007, 1);
    }

    @Test
    @DisplayName("PooledConnection: setAutoCommit/getAutoCommit")
    void testPooledConnection_AutoCommit() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.setAutoCommit(false);
            conn.getAutoCommit();
            conn.setAutoCommit(true);
        });

        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).getAutoCommit();
        verify(mockConnection).setAutoCommit(true);
    }

    @Test
    @DisplayName("PooledConnection: commit/rollback")
    void testPooledConnection_Transactions() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.commit();
            conn.rollback();
        });

        verify(mockConnection).commit();
        verify(mockConnection).rollback();
    }

    @Test
    @DisplayName("PooledConnection: getMetaData")
    void testPooledConnection_MetaData() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> conn.getMetaData());

        verify(mockConnection).getMetaData();
    }

    @Test
    @DisplayName("PooledConnection: setReadOnly/isReadOnly")
    void testPooledConnection_ReadOnly() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.setReadOnly(true);
            conn.isReadOnly();
            conn.setReadOnly(false);
        });

        verify(mockConnection).setReadOnly(true);
        verify(mockConnection).isReadOnly();
        verify(mockConnection).setReadOnly(false);
    }

    @Test
    @DisplayName("PooledConnection: setCatalog/getCatalog")
    void testPooledConnection_Catalog() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.setCatalog("test_catalog");
            conn.getCatalog();
        });

        verify(mockConnection).setCatalog(anyString());
        verify(mockConnection).getCatalog();
    }

    @Test
    @DisplayName("PooledConnection: setTransactionIsolation/getTransactionIsolation")
    void testPooledConnection_TransactionIsolation() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.getTransactionIsolation();
        });

        verify(mockConnection).setTransactionIsolation(anyInt());
        verify(mockConnection).getTransactionIsolation();
    }

    @Test
    @DisplayName("PooledConnection: getWarnings/clearWarnings")
    void testPooledConnection_Warnings() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.getWarnings();
            conn.clearWarnings();
        });

        verify(mockConnection).getWarnings();
        verify(mockConnection).clearWarnings();
    }

    @Test
    @DisplayName("PooledConnection: setSchema/getSchema")
    void testPooledConnection_Schema() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.setSchema("test_schema");
            conn.getSchema();
        });

        verify(mockConnection).setSchema(anyString());
        verify(mockConnection).getSchema();
    }

    @Test
    @DisplayName("PooledConnection: prepareCall")
    void testPooledConnection_PrepareCall() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.prepareCall("{call test_proc()}");
            conn.prepareCall("{call test_proc()}", 1005, 1007);
        });

        verify(mockConnection).prepareCall("{call test_proc()}");
        verify(mockConnection).prepareCall("{call test_proc()}", 1005, 1007);
    }

    @Test
    @DisplayName("PooledConnection: nativeSQL")
    void testPooledConnection_NativeSQL() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> conn.nativeSQL("SELECT * FROM test"));

        verify(mockConnection).nativeSQL(anyString());
    }

    @Test
    @DisplayName("PooledConnection: unwrap/isWrapperFor")
    void testPooledConnection_Wrapper() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.isWrapperFor(Connection.class);
            conn.unwrap(Connection.class);
        });

        verify(mockConnection).isWrapperFor(any());
        verify(mockConnection).unwrap(any());
    }

    @Test
    @DisplayName("PooledConnection: setHoldability/getHoldability")
    void testPooledConnection_Holdability() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.setHoldability(1);
            conn.getHoldability();
        });

        verify(mockConnection).setHoldability(anyInt());
        verify(mockConnection).getHoldability();
    }

    @Test
    @DisplayName("PooledConnection: createArrayOf/createStruct")
    void testPooledConnection_Factories() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.createArrayOf("VARCHAR", new Object[]{"test"});
            conn.createStruct("TEST_TYPE", new Object[]{"test"});
        });

        verify(mockConnection).createArrayOf(anyString(), any());
        verify(mockConnection).createStruct(anyString(), any());
    }

    @Test
    @DisplayName("PooledConnection: setSavepoint/releaseSavepoint/rollback")
    void testPooledConnection_Savepoints() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.setSavepoint();
            conn.setSavepoint("sp1");
            conn.releaseSavepoint(mock(java.sql.Savepoint.class));
            conn.rollback(mock(java.sql.Savepoint.class));
        });

        verify(mockConnection).setSavepoint();
        verify(mockConnection).setSavepoint("sp1");
        verify(mockConnection).releaseSavepoint(any());
    }

    @Test
    @DisplayName("PooledConnection: getTypeMap/setTypeMap")
    void testPooledConnection_TypeMap() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.getTypeMap();
            conn.setTypeMap(java.util.Collections.emptyMap());
        });

        verify(mockConnection).getTypeMap();
        verify(mockConnection).setTypeMap(any());
    }

    @Test
    @DisplayName("PooledConnection: getClientInfo/setClientInfo")
    void testPooledConnection_ClientInfo() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.getClientInfo();
            conn.getClientInfo("test_key");
            // setClientInfoは空実装（Java 21用）
        });

        verify(mockConnection).getClientInfo();
        verify(mockConnection).getClientInfo("test_key");
    }

    @Test
    @DisplayName("PooledConnection: createClob/createBlob/createNClob/createSQLXML")
    void testPooledConnection_LOBFactories() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.createClob();
            conn.createBlob();
            conn.createNClob();
            conn.createSQLXML();
        });

        verify(mockConnection).createClob();
        verify(mockConnection).createBlob();
        verify(mockConnection).createNClob();
        verify(mockConnection).createSQLXML();
    }

    @Test
    @DisplayName("PooledConnection: setNetworkTimeout/getNetworkTimeout")
    void testPooledConnection_NetworkTimeout() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> {
            conn.setNetworkTimeout(mock(java.util.concurrent.Executor.class), 5000);
            conn.getNetworkTimeout();
        });

        verify(mockConnection).setNetworkTimeout(any(), anyInt());
        verify(mockConnection).getNetworkTimeout();
    }

    @Test
    @DisplayName("PooledConnection: abort")
    void testPooledConnection_Abort() throws SQLException {
        connectionPool = new ConnectionPool(connectionFactory, 1, 1);

        Connection conn = connectionPool.getConnection();

        assertDoesNotThrow(() -> conn.abort(mock(java.util.concurrent.Executor.class)));

        verify(mockConnection).abort(any());
    }
}
