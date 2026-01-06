package com.example.rpgplugin.storage.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 軽量コネクションプール実装
 * HikariCP類似の設計
 */
public class ConnectionPool {

    private static final int MAX_RETRIES = 3;

    private final BlockingQueue<PooledConnection> availableConnections;
    private final ConnectionFactory connectionFactory;
    private final int maxPoolSize;
    private final long connectionTimeoutMillis;
    private int totalConnections;
    private boolean isShutdown;

    /**
     * コンストラクタ
     *
     * @param connectionFactory コネクションファクトリー
     * @param maxPoolSize 最大プールサイズ
     * @param connectionTimeoutSeconds コネクションタイムアウト（秒）
     */
    public ConnectionPool(ConnectionFactory connectionFactory, int maxPoolSize, long connectionTimeoutSeconds) {
        this.connectionFactory = connectionFactory;
        this.maxPoolSize = maxPoolSize;
        this.connectionTimeoutMillis = connectionTimeoutSeconds * 1000;
        this.availableConnections = new ArrayBlockingQueue<>(maxPoolSize);
        this.totalConnections = 0;
        this.isShutdown = false;
    }

    /**
     * デフォルト設定でのコンストラクタ
     *
     * @param connectionFactory コネクションファクトリー
     */
    public ConnectionPool(ConnectionFactory connectionFactory) {
        this(connectionFactory, 10, 30);
    }

    /**
     * コネクションを取得
     *
     * @return コネクション
     * @throws SQLException 取得失敗時
     */
    public Connection getConnection() throws SQLException {
        return getConnectionWithRetry(0);
    }

    /**
     * コネクションを取得（再帰リトライ付き）
     *
     * @param retryCount 現在のリトライ回数
     * @return コネクション
     * @throws SQLException 取得失敗時
     */
    private Connection getConnectionWithRetry(int retryCount) throws SQLException {
        if (isShutdown) {
            throw new SQLException("Connection pool has been shutdown");
        }

        if (retryCount >= MAX_RETRIES) {
            throw new SQLException("Max connection retries exceeded (" + MAX_RETRIES + ")");
        }

        PooledConnection pooledConn = availableConnections.poll();

        if (pooledConn != null) {
            if (pooledConn.isValid()) {
                return pooledConn;
            }
            // 無効なコネクションを削除
            totalConnections--;
        }

        // 新規コネクションを作成
        if (totalConnections < maxPoolSize) {
            synchronized (this) {
                if (totalConnections < maxPoolSize) {
                    Connection conn = connectionFactory.create();
                    totalConnections++;
                    return new PooledConnection(conn);
                }
            }
        }

        // プールが空の場合、待機
        try {
            pooledConn = availableConnections.poll(connectionTimeoutMillis, TimeUnit.MILLISECONDS);
            if (pooledConn == null) {
                throw new SQLException("Connection timeout after " + connectionTimeoutMillis + "ms");
            }
            if (!pooledConn.isValid()) {
                // 無効なコネクションの場合、リトライ
                return getConnectionWithRetry(retryCount + 1);
            }
            return pooledConn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for connection", e);
        }
    }

    /**
     * コネクションをプールに返却
     *
     * @param pooledConn プールされたコネクション
     */
    void releaseConnection(PooledConnection pooledConn) {
        if (isShutdown) {
            closeConnection(pooledConn);
            return;
        }

        if (pooledConn != null && pooledConn.isValid()) {
            if (!availableConnections.offer(pooledConn)) {
                // プールが満杯の場合、クローズ
                closeConnection(pooledConn);
                totalConnections--;
            }
        } else {
            totalConnections--;
        }
    }

    private void closeConnection(PooledConnection pooledConn) {
        try {
            if (pooledConn != null) {
                pooledConn.closeActual();
            }
        } catch (SQLException e) {
            // ログ出力のみ
        }
    }

    /**
     * プールをシャットダウン
     */
    public void shutdown() {
        isShutdown = true;
        PooledConnection conn;
        while ((conn = availableConnections.poll()) != null) {
            closeConnection(conn);
        }
        totalConnections = 0;
    }

    /**
     * プールされたコネクション
     */
    class PooledConnection implements Connection {

        private final Connection delegate;
        private boolean isClosed = false;

        PooledConnection(Connection delegate) {
            this.delegate = delegate;
        }

        @Override
        public void close() throws SQLException {
            if (!isClosed) {
                // 実際にはクローズせず、プールに返却
                releaseConnection(this);
                isClosed = true;
            }
        }

        void closeActual() throws SQLException {
            if (delegate != null && !delegate.isClosed()) {
                delegate.close();
            }
        }

        boolean isValid() {
            try {
                return delegate != null && !delegate.isClosed() && delegate.isValid(1);
            } catch (SQLException e) {
                return false;
            }
        }

        @Override
        public boolean isClosed() throws SQLException {
            return isClosed || delegate.isClosed();
        }

        // 以下、delegateへの委譲メソッド
        @Override
        public java.sql.Statement createStatement() throws SQLException {
            return delegate.createStatement();
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql) throws SQLException {
            return delegate.prepareStatement(sql);
        }

        @Override
        public java.sql.CallableStatement prepareCall(String sql) throws SQLException {
            return delegate.prepareCall(sql);
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            return delegate.nativeSQL(sql);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            delegate.setAutoCommit(autoCommit);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return delegate.getAutoCommit();
        }

        @Override
        public void commit() throws SQLException {
            delegate.commit();
        }

        @Override
        public void rollback() throws SQLException {
            delegate.rollback();
        }

        @Override
        public java.sql.DatabaseMetaData getMetaData() throws SQLException {
            return delegate.getMetaData();
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            delegate.setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return delegate.isReadOnly();
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {
            delegate.setCatalog(catalog);
        }

        @Override
        public String getCatalog() throws SQLException {
            return delegate.getCatalog();
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            delegate.setTransactionIsolation(level);
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return delegate.getTransactionIsolation();
        }

        @Override
        public java.sql.SQLWarning getWarnings() throws SQLException {
            return delegate.getWarnings();
        }

        @Override
        public void clearWarnings() throws SQLException {
            delegate.clearWarnings();
        }

        @Override
        public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return delegate.createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public java.util.Map<String, Class<?>> getTypeMap() throws SQLException {
            return delegate.getTypeMap();
        }

        @Override
        public void setTypeMap(java.util.Map<String, Class<?>> map) throws SQLException {
            delegate.setTypeMap(map);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            delegate.setHoldability(holdability);
        }

        @Override
        public int getHoldability() throws SQLException {
            return delegate.getHoldability();
        }

        @Override
        public java.sql.Savepoint setSavepoint() throws SQLException {
            return delegate.setSavepoint();
        }

        @Override
        public java.sql.Savepoint setSavepoint(String name) throws SQLException {
            return delegate.setSavepoint(name);
        }

        @Override
        public void rollback(java.sql.Savepoint savepoint) throws SQLException {
            delegate.rollback(savepoint);
        }

        @Override
        public void releaseSavepoint(java.sql.Savepoint savepoint) throws SQLException {
            delegate.releaseSavepoint(savepoint);
        }

        @Override
        public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return delegate.prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return delegate.prepareStatement(sql, columnIndexes);
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return delegate.prepareStatement(sql, columnNames);
        }

        @Override
        public java.sql.Clob createClob() throws SQLException {
            return delegate.createClob();
        }

        @Override
        public java.sql.Blob createBlob() throws SQLException {
            return delegate.createBlob();
        }

        @Override
        public java.sql.NClob createNClob() throws SQLException {
            return delegate.createNClob();
        }

        @Override
        public java.sql.SQLXML createSQLXML() throws SQLException {
            return delegate.createSQLXML();
        }

        @Override
        public void setClientInfo(String name, String value) throws java.sql.ClientInfoException {
            delegate.setClientInfo(name, value);
        }

        @Override
        public void setClientInfo(java.util.Properties properties) throws java.sql.ClientInfoException {
            delegate.setClientInfo(properties);
        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return delegate.getClientInfo(name);
        }

        @Override
        public java.util.Properties getClientInfo() throws SQLException {
            return delegate.getClientInfo();
        }

        @Override
        public java.sql.Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return delegate.createArrayOf(typeName, elements);
        }

        @Override
        public java.sql.Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return delegate.createStruct(typeName, attributes);
        }

        @Override
        public void setSchema(String schema) throws SQLException {
            delegate.setSchema(schema);
        }

        @Override
        public String getSchema() throws SQLException {
            return delegate.getSchema();
        }

        @Override
        public void abort(java.util.concurrent.Executor executor) throws SQLException {
            delegate.abort(executor);
        }

        @Override
        public void setNetworkTimeout(java.util.concurrent.Executor executor, int milliseconds) throws SQLException {
            delegate.setNetworkTimeout(executor, milliseconds);
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return delegate.getNetworkTimeout();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return delegate.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return delegate.isWrapperFor(iface);
        }
    }

    /**
     * コネクションファクトリーインターフェース
     */
    public interface ConnectionFactory {
        Connection create() throws SQLException;
    }
}
