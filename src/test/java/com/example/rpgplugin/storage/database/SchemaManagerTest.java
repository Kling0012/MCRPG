package com.example.rpgplugin.storage.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SchemaManagerの単体テスト
 *
 * <p>データベーススキーマ管理機能のテスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("SchemaManager テスト")
@ExtendWith(MockitoExtension.class)
class SchemaManagerTest {

    @Mock
    private DatabaseManager mockDbManager;

    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    private Logger logger;
    private SchemaManager schemaManager;

    @BeforeEach
    void setUp() throws SQLException {
        logger = Logger.getLogger("TestLogger");
        lenient().when(mockDbManager.getConnection()).thenReturn(mockConnection);
        lenient().when(mockConnection.createStatement()).thenReturn(mockStatement);
        schemaManager = new SchemaManager(mockDbManager, logger);
    }

    // ==================== getSchemaVersion テスト ====================

    @Test
    @DisplayName("getSchemaVersion: 正常にバージョンを取得")
    void testGetSchemaVersion_Success() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        lenient().when(mockStatement.executeQuery("SELECT MAX(version) as version FROM schema_version"))
                .thenReturn(rs);
        lenient().when(rs.next()).thenReturn(true);
        lenient().when(rs.getInt("version")).thenReturn(5);

        int version = schemaManager.getSchemaVersion();

        assertEquals(5, version);
    }

    @Test
    @DisplayName("getSchemaVersion: エラー時に0を返す")
    void testGetSchemaVersion_Error() throws SQLException {
        lenient().when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("DB error"));

        int version = schemaManager.getSchemaVersion();

        assertEquals(0, version);
    }

    @Test
    @DisplayName("getSchemaVersion: 結果がない場合に0を返す")
    void testGetSchemaVersion_NoResults() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        lenient().when(mockStatement.executeQuery("SELECT MAX(version) as version FROM schema_version"))
                .thenReturn(rs);
        lenient().when(rs.next()).thenReturn(false);

        int version = schemaManager.getSchemaVersion();

        assertEquals(0, version);
    }
}
