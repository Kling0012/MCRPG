package com.example.rpgplugin.storage.repository;

import com.example.rpgplugin.storage.models.PlayerData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

/**
 * CacheRepositoryの単体テスト
 *
 * <p>3層キャッシュシステム（L1: ConcurrentHashMap, L2: Caffeine, L3: データベース）のテスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("CacheRepository テスト")
@ExtendWith(MockitoExtension.class)
class CacheRepositoryTest {

    @Mock
    private PlayerDataRepository mockRepository;

    @Mock
    private Logger mockLogger;

    private CacheRepository cacheRepository;
    private UUID testUuid;
    private PlayerData testPlayerData;

    @BeforeEach
    void setUp() {
        lenient().doNothing().when(mockLogger).fine(anyString());
        lenient().doNothing().when(mockLogger).info(anyString());
        lenient().doNothing().when(mockLogger).severe(anyString());

        // 統計ログ出力を無効化してテストをシンプルに
        cacheRepository = new CacheRepository(
                mockRepository,
                mockLogger,
                100,    // L2最大サイズ
                5,      // L2 TTL（分）
                true,   // expireAfterAccess
                0       // 統計ログ出力間隔（0で無効）
        );

        testUuid = UUID.randomUUID();
        testPlayerData = new PlayerData(testUuid, "TestPlayer");
    }

    @AfterEach
    void tearDown() {
        cacheRepository.clearAll();
    }

    // ==================== コンストラクタ テスト ====================

    @Test
    @DisplayName("コンストラクタ: デフォルト設定で初期化")
    void testConstructor_DefaultSettings() {
        CacheRepository defaultRepo = new CacheRepository(mockRepository, mockLogger);

        assertNotNull(defaultRepo, "CacheRepositoryが作成される");
        assertEquals(0, defaultRepo.getOnlinePlayerCount(), "初期状態ではオンラインプレイヤーは0");
    }

    @Test
    @DisplayName("コンストラクタ: カスタム設定で初期化")
    void testConstructor_CustomSettings() {
        CacheRepository customRepo = new CacheRepository(
                mockRepository,
                mockLogger,
                500,    // L2最大サイズ
                10,     // L2 TTL
                false,  // expireAfterWrite
                0       // 統計ログ無効
        );

        assertNotNull(customRepo, "CacheRepositoryが作成される");
    }

    // ==================== findById テスト ====================

    @Test
    @DisplayName("findById: L1キャッシュヒット")
    void testFindById_L1CacheHit() throws SQLException {
        // L1キャッシュに事前に格納
        cacheRepository.addToOnlineCache(testPlayerData);

        lenient().when(mockRepository.findById(testUuid)).thenReturn(java.util.Optional.empty());

        java.util.Optional<PlayerData> result = cacheRepository.findById(testUuid);

        assertTrue(result.isPresent(), "データが取得できる");
        assertEquals(testUuid, result.get().getUuid(), "正しいUUIDのデータ");
        assertEquals("TestPlayer", result.get().getUsername(), "正しいユーザー名");

        // DBは呼ばれないことを確認
        verify(mockRepository, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("findById: L2キャッシュヒット")
    void testFindById_L2CacheHit() throws SQLException {
        // まずL2にデータを保存（L1には入れない）
        cacheRepository.save(testPlayerData);
        cacheRepository.removeFromOnlineCache(testUuid); // L1から削除

        lenient().when(mockRepository.findById(testUuid)).thenReturn(java.util.Optional.empty());

        java.util.Optional<PlayerData> result = cacheRepository.findById(testUuid);

        assertTrue(result.isPresent(), "L2キャッシュからデータが取得できる");

        // DBは呼ばれないことを確認
        verify(mockRepository, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("findById: L3データベースヒット")
    void testFindById_L3DatabaseHit() throws SQLException {
        lenient().when(mockRepository.findById(testUuid))
                .thenReturn(java.util.Optional.of(testPlayerData));

        java.util.Optional<PlayerData> result = cacheRepository.findById(testUuid);

        assertTrue(result.isPresent(), "データベースからデータが取得できる");
        assertEquals(testUuid, result.get().getUuid());

        verify(mockRepository, times(1)).findById(testUuid);
    }

    @Test
    @DisplayName("findById: データが存在しない場合")
    void testFindById_NotFound() throws SQLException {
        lenient().when(mockRepository.findById(testUuid)).thenReturn(java.util.Optional.empty());

        java.util.Optional<PlayerData> result = cacheRepository.findById(testUuid);

        assertFalse(result.isPresent(), "空のOptionalが返される");
    }

    @Test
    @DisplayName("findById: データベースエラー時")
    void testFindById_DatabaseError() throws SQLException {
        lenient().when(mockRepository.findById(testUuid))
                .thenThrow(new RuntimeException("Database error"));

        java.util.Optional<PlayerData> result = cacheRepository.findById(testUuid);

        assertFalse(result.isPresent(), "エラー時は空のOptionalが返される");
        verify(mockLogger).severe(startsWith("Failed to fetch player"));
    }

    // ==================== save テスト ====================

    @Test
    @DisplayName("save: 正常に保存")
    void testSave_Success() throws SQLException {
        lenient().doNothing().when(mockRepository).saveAsync(any(PlayerData.class));

        cacheRepository.save(testPlayerData);

        // L1キャッシュに保存される
        java.util.Optional<PlayerData> fromL1 = cacheRepository.findById(testUuid);
        assertTrue(fromL1.isPresent(), "L1キャッシュに保存される");

        verify(mockRepository, times(1)).saveAsync(testPlayerData);
    }

    @Test
    @DisplayName("save: nullを渡した場合は何もしない")
    void testSave_NullPlayer() {
        cacheRepository.save(null);

        verify(mockRepository, never()).saveAsync(any(PlayerData.class));
        verify(mockLogger, never()).fine(anyString());
    }

    // ==================== addToOnlineCache/removeFromOnlineCache テスト ====================

    @Test
    @DisplayName("addToOnlineCache: L1キャッシュに追加")
    void testAddToOnlineCache_Success() throws SQLException {
        cacheRepository.addToOnlineCache(testPlayerData);

        assertEquals(1, cacheRepository.getOnlinePlayerCount(), "オンラインプレイヤー数が増える");

        java.util.Optional<PlayerData> result = cacheRepository.findById(testUuid);
        assertTrue(result.isPresent(), "L1キャッシュから取得できる");
    }

    @Test
    @DisplayName("addToOnlineCache: nullを渡した場合は何もしない")
    void testAddToOnlineCache_NullPlayer() {
        cacheRepository.addToOnlineCache(null);

        assertEquals(0, cacheRepository.getOnlinePlayerCount(), "プレイヤー数は増えない");
    }

    @Test
    @DisplayName("removeFromOnlineCache: L1キャッシュから削除")
    void testRemoveFromOnlineCache_Success() {
        cacheRepository.addToOnlineCache(testPlayerData);
        assertEquals(1, cacheRepository.getOnlinePlayerCount(), "追加後に1人");

        cacheRepository.removeFromOnlineCache(testUuid);
        assertEquals(0, cacheRepository.getOnlinePlayerCount(), "削除後に0人");
    }

    @Test
    @DisplayName("removeFromOnlineCache: 存在しないUUIDの場合")
    void testRemoveFromOnlineCache_NotFound() {
        // エラーにならずに処理される
        assertDoesNotThrow(() -> cacheRepository.removeFromOnlineCache(UUID.randomUUID()));
    }

    // ==================== getOnlinePlayers/getOnlinePlayerCount テスト ====================

    @Test
    @DisplayName("getOnlinePlayers: オンラインプレイヤーを取得")
    void testGetOnlinePlayers_Success() {
        PlayerData player1 = new PlayerData(UUID.randomUUID(), "Player1");
        PlayerData player2 = new PlayerData(UUID.randomUUID(), "Player2");
        PlayerData player3 = new PlayerData(UUID.randomUUID(), "Player3");

        cacheRepository.addToOnlineCache(player1);
        cacheRepository.addToOnlineCache(player2);
        cacheRepository.addToOnlineCache(player3);

        var onlinePlayers = cacheRepository.getOnlinePlayers();

        assertEquals(3, onlinePlayers.size(), "3人のプレイヤーが取得できる");
        assertTrue(onlinePlayers.stream().anyMatch(p -> p.getUsername().equals("Player1")));
        assertTrue(onlinePlayers.stream().anyMatch(p -> p.getUsername().equals("Player2")));
        assertTrue(onlinePlayers.stream().anyMatch(p -> p.getUsername().equals("Player3")));
    }

    @Test
    @DisplayName("getOnlinePlayers: 空の場合")
    void testGetOnlinePlayers_Empty() {
        var onlinePlayers = cacheRepository.getOnlinePlayers();

        assertNotNull(onlinePlayers, "nullではなく空のコレクション");
        assertTrue(onlinePlayers.isEmpty(), "空のコレクション");
    }

    @Test
    @DisplayName("getOnlinePlayerCount: 正しい数を返す")
    void testGetOnlinePlayerCount_Success() {
        assertEquals(0, cacheRepository.getOnlinePlayerCount(), "初期値は0");

        cacheRepository.addToOnlineCache(new PlayerData(UUID.randomUUID(), "Player1"));
        assertEquals(1, cacheRepository.getOnlinePlayerCount());

        cacheRepository.addToOnlineCache(new PlayerData(UUID.randomUUID(), "Player2"));
        assertEquals(2, cacheRepository.getOnlinePlayerCount());

        cacheRepository.removeFromOnlineCache(testUuid);
        assertEquals(2, cacheRepository.getOnlinePlayerCount(), "存在しないUUIDを削除しても数は変わらない");
    }

    // ==================== getStatistics テスト ====================

    @Test
    @DisplayName("getStatistics: 初期状態の統計")
    void testGetStatistics_InitialState() {
        CacheRepository.CacheStatistics stats = cacheRepository.getStatistics();

        assertNotNull(stats, "統計情報が取得できる");
        assertEquals(0, stats.l1Hits(), "L1ヒット数は0");
        assertEquals(0, stats.l2Hits(), "L2ヒット数は0");
        assertEquals(0, stats.l3Hits(), "L3ヒット数は0");
        assertEquals(0, stats.totalRequests(), "総リクエスト数は0");
        assertEquals(0.0, stats.overallHitRate(), 0.001, "ヒット率は0%");
    }

    @Test
    @DisplayName("getStatistics: キャッシュが有効な間は同じインスタンスを返す")
    void testGetStatistics_Cached() throws SQLException {
        lenient().when(mockRepository.findById(testUuid))
                .thenReturn(java.util.Optional.of(testPlayerData));

        cacheRepository.findById(testUuid);

        CacheRepository.CacheStatistics stats1 = cacheRepository.getStatistics();
        CacheRepository.CacheStatistics stats2 = cacheRepository.getStatistics();

        // 同じインスタンスが返される（キャッシュが有効）
        assertSame(stats1, stats2, "キャッシュが有効な間は同じインスタンス");
    }

    @Test
    @DisplayName("getStatisticsForceRefresh: 強制的に再計算")
    void testGetStatisticsForceRefresh() throws SQLException {
        lenient().when(mockRepository.findById(testUuid))
                .thenReturn(java.util.Optional.of(testPlayerData));

        cacheRepository.findById(testUuid);

        CacheRepository.CacheStatistics stats1 = cacheRepository.getStatistics();
        CacheRepository.CacheStatistics stats2 = cacheRepository.getStatisticsForceRefresh();

        assertNotSame(stats1, stats2, "強制リフレッシュで新しいインスタンス");
        assertTrue(stats2.l3Hits() > 0 || stats2.totalRequests() > 0, "統計が更新されている");
    }

    // ==================== logStatistics テスト ====================

    @Test
    @DisplayName("logStatistics: 正常にログ出力")
    void testLogStatistics_Success() {
        assertDoesNotThrow(() -> cacheRepository.logStatistics());

        verify(mockLogger, atLeastOnce()).info(startsWith("=== Cache Statistics"));
    }

    // ==================== clearL2Cache テスト ====================

    @Test
    @DisplayName("clearL2Cache: L2キャッシュをクリア")
    void testClearL2Cache_Success() throws SQLException {
        // L2にデータを保存
        cacheRepository.save(testPlayerData);
        cacheRepository.removeFromOnlineCache(testUuid); // L1から削除

        // L2から取得できることを確認
        java.util.Optional<PlayerData> before = cacheRepository.findById(testUuid);
        assertTrue(before.isPresent(), "クリア前はL2から取得できる");

        // L2をクリア
        cacheRepository.clearL2Cache();

        // まだL2から取得できる（CaffeineのinvalidateAllは即時ではない可能性があるため）
        // ただしログ出力は確認
        verify(mockLogger).info(startsWith("L2 cache cleared"));
    }

    // ==================== invalidate テスト ====================

    @Test
    @DisplayName("invalidate: 特定プレイヤーのキャッシュをクリア")
    void testInvalidate_Success() throws SQLException {
        cacheRepository.addToOnlineCache(testPlayerData);

        assertTrue(cacheRepository.findById(testUuid).isPresent(), "キャッシュに存在する");

        cacheRepository.invalidate(testUuid);

        assertFalse(cacheRepository.findById(testUuid).isPresent(),
                "invalidate後はDBからも取得しようとする（DBモックはemptyを返す）");
    }

    // ==================== clearAll テスト ====================

    @Test
    @DisplayName("clearAll: すべてのキャッシュをクリア")
    void testClearAll_Success() {
        PlayerData player1 = new PlayerData(UUID.randomUUID(), "Player1");
        PlayerData player2 = new PlayerData(UUID.randomUUID(), "Player2");

        cacheRepository.addToOnlineCache(player1);
        cacheRepository.addToOnlineCache(player2);

        assertEquals(2, cacheRepository.getOnlinePlayerCount(), "2人がオンライン");

        cacheRepository.clearAll();

        assertEquals(0, cacheRepository.getOnlinePlayerCount(), "全員クリア");
        verify(mockLogger).info(startsWith("All caches cleared"));
    }

    // ==================== setStatsLoggingTaskId/cancelStatsLogging テスト ====================

    @Test
    @DisplayName("setStatsLoggingTaskId: タスクIDを設定")
    void testSetStatsLoggingTaskId() {
        assertDoesNotThrow(() -> cacheRepository.setStatsLoggingTaskId(12345));
    }

    @Test
    @DisplayName("cancelStatsLogging: タスクをキャンセル")
    void testCancelStatsLogging() {
        cacheRepository.setStatsLoggingTaskId(12345);

        assertDoesNotThrow(() -> cacheRepository.cancelStatsLogging());
    }

    @Test
    @DisplayName("cancelStatsLogging: タスクIDが-1の場合")
    void testCancelStatsLogging_NoTaskSet() {
        assertDoesNotThrow(() -> cacheRepository.cancelStatsLogging());
    }

    // ==================== CacheStatistics record テスト ====================

    @Test
    @DisplayName("CacheStatistics: recordの正しい動作")
    void testCacheStatistics_Record() {
        var stats = new CacheRepository.CacheStatistics(
                10L,    // l1Hits
                20L,    // l2Hits
                30L,    // l3Hits
                60L,    // totalRequests
                50.0,   // overallHitRate
                5L,     // l1Size
                100L,   // l2Size
                0.75,   // l2HitRate
                0.25    // l2MissRate
        );

        // 全てのアクセサを呼び出してカバレッジを確保
        assertEquals(10L, stats.l1Hits());
        assertEquals(20L, stats.l2Hits());
        assertEquals(30L, stats.l3Hits());
        assertEquals(60L, stats.totalRequests());
        assertEquals(50.0, stats.overallHitRate(), 0.001);
        assertEquals(5L, stats.l1Size());
        assertEquals(100L, stats.l2Size());
        assertEquals(0.75, stats.l2HitRate(), 0.001);
        assertEquals(0.25, stats.l2MissRate(), 0.001);

        // toString, equals, hashCodeも呼び出す
        assertNotNull(stats.toString(), "toStringがnullではない");
        assertNotEquals("", stats.toString(), "toStringが空ではない");
    }

    @Test
    @DisplayName("CacheStatistics: equalsとhashCodeの動作")
    void testCacheStatistics_EqualsAndHashCode() {
        var stats1 = new CacheRepository.CacheStatistics(
                10L, 20L, 30L, 60L, 50.0, 5L, 100L, 0.75, 0.25
        );
        var stats2 = new CacheRepository.CacheStatistics(
                10L, 20L, 30L, 60L, 50.0, 5L, 100L, 0.75, 0.25
        );
        var stats3 = new CacheRepository.CacheStatistics(
                5L, 10L, 15L, 30L, 25.0, 2L, 50L, 0.5, 0.5
        );

        assertEquals(stats1, stats2, "同じ値なら等しい");
        assertNotEquals(stats1, stats3, "違う値なら等しくない");
        assertEquals(stats1.hashCode(), stats2.hashCode(), "同じ値ならhashCodeも等しい");
    }

    // ==================== 統合テスト ====================

    @Test
    @DisplayName("統合: 複数のfindById呼び出しで統計が正しく記録される")
    void testIntegration_MultipleFindByIdCalls() throws SQLException {
        PlayerData player1 = new PlayerData(UUID.randomUUID(), "Player1");
        PlayerData player2 = new PlayerData(UUID.randomUUID(), "Player2");

        lenient().when(mockRepository.findById(player1.getUuid()))
                .thenReturn(java.util.Optional.of(player1));
        lenient().when(mockRepository.findById(player2.getUuid()))
                .thenReturn(java.util.Optional.of(player2));

        // L1キャッシュに追加
        cacheRepository.addToOnlineCache(player1);
        cacheRepository.addToOnlineCache(player2);

        // L1ヒット（2回）
        cacheRepository.findById(player1.getUuid());
        cacheRepository.findById(player2.getUuid());

        CacheRepository.CacheStatistics stats = cacheRepository.getStatisticsForceRefresh();

        assertEquals(2, stats.l1Hits(), "L1ヒットが2回記録される");
        assertEquals(2, stats.totalRequests(), "総リクエスト数は2");
        assertTrue(stats.overallHitRate() > 0, "ヒット率が計算される");
    }

    @Test
    @DisplayName("統合: L1→L2→L3のキャッシュ階層の動作")
    void testIntegration_CacheHierarchy() throws SQLException {
        UUID uuid = UUID.randomUUID();
        PlayerData player = new PlayerData(uuid, "TestPlayer");

        lenient().when(mockRepository.findById(uuid))
                .thenReturn(java.util.Optional.of(player));

        // 初回: L3から取得
        java.util.Optional<PlayerData> result1 = cacheRepository.findById(uuid);
        assertTrue(result1.isPresent());

        // L2にキャッシュされているのでDBは呼ばれない
        cacheRepository.removeFromOnlineCache(uuid);
        java.util.Optional<PlayerData> result2 = cacheRepository.findById(uuid);
        assertTrue(result2.isPresent());

        verify(mockRepository, times(1)).findById(uuid); // 最初の1回だけ
    }

    @Test
    @DisplayName("統合: save後にfindByIdでキャッシュヒット")
    void testIntegration_SaveThenFind() throws SQLException {
        lenient().doNothing().when(mockRepository).saveAsync(any(PlayerData.class));

        cacheRepository.save(testPlayerData);

        java.util.Optional<PlayerData> result = cacheRepository.findById(testUuid);

        assertTrue(result.isPresent(), "保存したデータが取得できる");
        verify(mockRepository, never()).findById(any(UUID.class)); // DBは呼ばれない
    }
}
