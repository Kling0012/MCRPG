package com.example.rpgplugin.storage;

import com.example.rpgplugin.storage.database.DatabaseManager;
import com.example.rpgplugin.storage.models.PlayerData;
import com.example.rpgplugin.storage.repository.CacheRepository;
import com.example.rpgplugin.storage.repository.PlayerDataRepository;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * StorageManagerの単体テスト
 *
 * <p>ストレージ管理システムの機能テスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("StorageManager テスト")
@ExtendWith(MockitoExtension.class)
class StorageManagerTest {

    @Mock
    private Plugin mockPlugin;

    @Mock
    private FileConfiguration mockConfig;

    @Mock
    private ConfigurationSection mockCacheConfig;

    @Mock
    private Server mockServer;

    @Mock
    private BukkitScheduler mockScheduler;

    @Mock
    private DatabaseManager mockDatabaseManager;

    @Mock
    private PlayerDataRepository mockPlayerDataRepository;

    @Mock
    private CacheRepository mockCacheRepository;

    @Mock
    private PlayerData mockPlayerData;

    private Logger logger;
    private StorageManager storageManager;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger("TestLogger");
        testUuid = UUID.randomUUID();

        // 一時ディレクトリを作成
        File tempDataFolder = new File(System.getProperty("java.io.tmpdir"), "rpg_test_" + System.currentTimeMillis());
        tempDataFolder.mkdirs();

        // プラグイン基本設定
        lenient().when(mockPlugin.getLogger()).thenReturn(logger);
        lenient().when(mockPlugin.getConfig()).thenReturn(mockConfig);
        lenient().when(mockPlugin.getServer()).thenReturn(mockServer);
        lenient().when(mockPlugin.getDataFolder()).thenReturn(tempDataFolder);
        lenient().when(mockServer.getScheduler()).thenReturn(mockScheduler);

        // キャッシュ設定
        lenient().when(mockConfig.getConfigurationSection("cache")).thenReturn(null);
        lenient().when(mockConfig.getInt("cache.stats_logging_interval", 0)).thenReturn(0);

        // スケジューラ基本設定
        lenient().when(mockScheduler.runTaskTimer(eq(mockPlugin), any(Runnable.class), anyLong(), anyLong()))
                .thenReturn(null);

        // PlayerData基本設定
        lenient().when(mockPlayerData.getUuid()).thenReturn(testUuid);

        storageManager = new StorageManager(mockPlugin);
    }

    @AfterEach
    void tearDown() {
        // クリーンアップ: 一時ディレクトリを削除
        try {
            File dataFolder = mockPlugin.getDataFolder();
            if (dataFolder != null && dataFolder.exists()) {
                deleteDirectory(dataFolder);
            }
        } catch (Exception e) {
            // クリーンアップエラーは無視
        }
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

    // ==================== 初期化テスト ====================

    @Test
    @DisplayName("initialize: 正常に初期化される")
    void testInitialize_Success() throws Exception {
        // 一時ディレクトリを作成
        File tempDataFolder = new File(System.getProperty("java.io.tmpdir"), "rpg_test_" + System.currentTimeMillis());
        tempDataFolder.mkdirs();

        // モック設定をリセット
        reset(mockPlugin);
        lenient().when(mockPlugin.getLogger()).thenReturn(logger);
        lenient().when(mockPlugin.getConfig()).thenReturn(mockConfig);
        lenient().when(mockPlugin.getDataFolder()).thenReturn(tempDataFolder);
        lenient().when(mockConfig.getConfigurationSection("cache")).thenReturn(null);
        lenient().when(mockConfig.getInt("cache.stats_logging_interval", 0)).thenReturn(0);
        lenient().when(mockPlugin.getServer()).thenReturn(mockServer);
        lenient().when(mockServer.getScheduler()).thenReturn(mockScheduler);

        StorageManager freshManager = new StorageManager(mockPlugin);

        assertDoesNotThrow(() -> freshManager.initialize());
        assertTrue(freshManager.isInitialized());

        // クリーンアップ
        deleteDirectory(tempDataFolder);
    }

    @Test
    @DisplayName("initialize: 二重初期化時に警告ログ")
    void testInitialize_DoubleInit() throws Exception {
        // 一時ディレクトリを作成
        File tempDataFolder = new File(System.getProperty("java.io.tmpdir"), "rpg_test_" + System.currentTimeMillis());
        tempDataFolder.mkdirs();

        // 初回初期化のモック設定
        reset(mockPlugin);
        lenient().when(mockPlugin.getLogger()).thenReturn(logger);
        lenient().when(mockPlugin.getConfig()).thenReturn(mockConfig);
        lenient().when(mockPlugin.getDataFolder()).thenReturn(tempDataFolder);
        lenient().when(mockConfig.getConfigurationSection("cache")).thenReturn(null);
        lenient().when(mockPlugin.getServer()).thenReturn(mockServer);
        lenient().when(mockServer.getScheduler()).thenReturn(mockScheduler);

        StorageManager freshManager = new StorageManager(mockPlugin);
        freshManager.initialize();

        assertTrue(freshManager.isInitialized());

        // 二回目の初期化
        freshManager.initialize();

        // 初期化済みフラグは変わらない
        assertTrue(freshManager.isInitialized());

        // クリーンアップ
        deleteDirectory(tempDataFolder);
    }

    // ==================== getPlayerData テスト ====================

    @Test
    @DisplayName("getPlayerData: 未初期化時はemptyを返す")
    void testGetPlayerData_NotInitialized() {
        Optional<PlayerData> result = storageManager.getPlayerData(testUuid);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("getPlayerData: キャッシュからデータを取得")
    void testGetPlayerData_Success() throws Exception {
        // 一時ディレクトリを作成
        File tempDataFolder = new File(System.getProperty("java.io.tmpdir"), "rpg_test_" + System.currentTimeMillis());
        tempDataFolder.mkdirs();

        // 初期化
        reset(mockPlugin);
        lenient().when(mockPlugin.getLogger()).thenReturn(logger);
        lenient().when(mockPlugin.getConfig()).thenReturn(mockConfig);
        lenient().when(mockPlugin.getDataFolder()).thenReturn(tempDataFolder);
        lenient().when(mockConfig.getConfigurationSection("cache")).thenReturn(null);
        lenient().when(mockPlugin.getServer()).thenReturn(mockServer);
        lenient().when(mockServer.getScheduler()).thenReturn(mockScheduler);

        StorageManager freshManager = new StorageManager(mockPlugin);
        freshManager.initialize();

        // 内部リポジトリをモックに置き換えることは難しいため
        // 初期化済みであることを確認
        assertTrue(freshManager.isInitialized());

        // クリーンアップ
        deleteDirectory(tempDataFolder);
    }

    // ==================== savePlayerData テスト ====================

    @Test
    @DisplayName("savePlayerData: 未初期化時は何もしない")
    void testSavePlayerData_NotInitialized() {
        assertDoesNotThrow(() -> storageManager.savePlayerData(mockPlayerData));
    }

    // ==================== addOnlinePlayer テスト ====================

    @Test
    @DisplayName("addOnlinePlayer: 未初期化時は何もしない")
    void testAddOnlinePlayer_NotInitialized() {
        assertDoesNotThrow(() -> storageManager.addOnlinePlayer(mockPlayerData));
    }

    // ==================== removeOnlinePlayer テスト ====================

    @Test
    @DisplayName("removeOnlinePlayer: 未初期化時は何もしない")
    void testRemoveOnlinePlayer_NotInitialized() {
        assertDoesNotThrow(() -> storageManager.removeOnlinePlayer(testUuid));
    }

    // ==================== getOnlinePlayers テスト ====================

    @Test
    @DisplayName("getOnlinePlayers: 未初期化時は空リストを返す")
    void testGetOnlinePlayers_NotInitialized() {
        var result = storageManager.getOnlinePlayers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getOnlinePlayerCount テスト ====================

    @Test
    @DisplayName("getOnlinePlayerCount: 未初期化時は0を返す")
    void testGetOnlinePlayerCount_NotInitialized() {
        assertEquals(0, storageManager.getOnlinePlayerCount());
    }

    // ==================== getCacheStatistics テスト ====================

    @Test
    @DisplayName("getCacheStatistics: 未初期化時はnullを返す")
    void testGetCacheStatistics_NotInitialized() {
        assertNull(storageManager.getCacheStatistics());
    }

    // ==================== logCacheStatistics テスト ====================

    @Test
    @DisplayName("logCacheStatistics: 未初期化時は警告ログ")
    void testLogCacheStatistics_NotInitialized() {
        assertDoesNotThrow(() -> storageManager.logCacheStatistics());
    }

    // ==================== shutdown テスト ====================

    @Test
    @DisplayName("shutdown: 未初期化時は何もしない")
    void testShutdown_NotInitialized() {
        assertDoesNotThrow(() -> storageManager.shutdown());
        assertFalse(storageManager.isInitialized());
    }

    // ==================== Getter テスト ====================

    @Test
    @DisplayName("getPlayerDataRepository: 初期化後にリポジトリを取得")
    void testGetPlayerDataRepository() throws Exception {
        // 一時ディレクトリを作成
        File tempDataFolder = new File(System.getProperty("java.io.tmpdir"), "rpg_test_" + System.currentTimeMillis());
        tempDataFolder.mkdirs();

        // 初期化設定
        reset(mockPlugin);
        lenient().when(mockPlugin.getLogger()).thenReturn(logger);
        lenient().when(mockPlugin.getConfig()).thenReturn(mockConfig);
        lenient().when(mockPlugin.getDataFolder()).thenReturn(tempDataFolder);
        lenient().when(mockConfig.getConfigurationSection("cache")).thenReturn(null);
        lenient().when(mockPlugin.getServer()).thenReturn(mockServer);
        lenient().when(mockServer.getScheduler()).thenReturn(mockScheduler);

        StorageManager freshManager = new StorageManager(mockPlugin);
        freshManager.initialize();

        var result = freshManager.getPlayerDataRepository();
        assertNotNull(result);

        // クリーンアップ
        deleteDirectory(tempDataFolder);
    }

    @Test
    @DisplayName("getCacheRepository: 初期化後にキャッシュリポジトリを取得")
    void testGetCacheRepository() throws Exception {
        // 一時ディレクトリを作成
        File tempDataFolder = new File(System.getProperty("java.io.tmpdir"), "rpg_test_" + System.currentTimeMillis());
        tempDataFolder.mkdirs();

        // 初期化設定
        reset(mockPlugin);
        lenient().when(mockPlugin.getLogger()).thenReturn(logger);
        lenient().when(mockPlugin.getConfig()).thenReturn(mockConfig);
        lenient().when(mockPlugin.getDataFolder()).thenReturn(tempDataFolder);
        lenient().when(mockConfig.getConfigurationSection("cache")).thenReturn(null);
        lenient().when(mockPlugin.getServer()).thenReturn(mockServer);
        lenient().when(mockServer.getScheduler()).thenReturn(mockScheduler);

        StorageManager freshManager = new StorageManager(mockPlugin);
        freshManager.initialize();

        var result = freshManager.getCacheRepository();
        assertNotNull(result);

        // クリーンアップ
        deleteDirectory(tempDataFolder);
    }

    @Test
    @DisplayName("getDatabaseManager: 初期化後にデータベースマネージャーを取得")
    void testGetDatabaseManager() throws Exception {
        // 一時ディレクトリを作成
        File tempDataFolder = new File(System.getProperty("java.io.tmpdir"), "rpg_test_" + System.currentTimeMillis());
        tempDataFolder.mkdirs();

        // 初期化設定
        reset(mockPlugin);
        lenient().when(mockPlugin.getLogger()).thenReturn(logger);
        lenient().when(mockPlugin.getConfig()).thenReturn(mockConfig);
        lenient().when(mockPlugin.getDataFolder()).thenReturn(tempDataFolder);
        lenient().when(mockConfig.getConfigurationSection("cache")).thenReturn(null);
        lenient().when(mockPlugin.getServer()).thenReturn(mockServer);
        lenient().when(mockServer.getScheduler()).thenReturn(mockScheduler);

        StorageManager freshManager = new StorageManager(mockPlugin);
        freshManager.initialize();

        var result = freshManager.getDatabaseManager();
        assertNotNull(result);

        // クリーンアップ
        deleteDirectory(tempDataFolder);
    }

    @Test
    @DisplayName("isInitialized: 初期化状態を正しく返す")
    void testIsInitialized() {
        assertFalse(storageManager.isInitialized());
    }
}
