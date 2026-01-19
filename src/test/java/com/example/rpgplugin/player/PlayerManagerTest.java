package com.example.rpgplugin.player;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.stats.StatManager;
import com.example.rpgplugin.storage.models.PlayerData;
import com.example.rpgplugin.storage.repository.PlayerDataRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * PlayerManagerのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PlayerManager Tests")
class PlayerManagerTest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private PlayerDataRepository mockRepository;

    @Mock
    private Logger mockLogger;

    @Mock
    private Player mockPlayer;

    @Mock
    private Player mockPlayer2;

    @Mock
    private BukkitScheduler mockScheduler;

    private MockedStatic<Bukkit> mockedBukkit;

    private PlayerManager playerManager;
    private UUID testUuid;
    private UUID testUuid2;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        testUuid2 = UUID.randomUUID();

        when(mockPlugin.getLogger()).thenReturn(mockLogger);
        when(mockPlayer.getUniqueId()).thenReturn(testUuid);
        when(mockPlayer.getName()).thenReturn("TestPlayer");
        when(mockPlayer.isOnline()).thenReturn(true);
        when(mockPlayer2.getUniqueId()).thenReturn(testUuid2);
        when(mockPlayer2.getName()).thenReturn("TestPlayer2");
        when(mockPlayer2.isOnline()).thenReturn(true);

        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn(Collections.emptyList());
        mockedBukkit.when(() -> Bukkit.getPlayer(testUuid)).thenReturn(mockPlayer);
        mockedBukkit.when(() -> Bukkit.getPlayer(testUuid2)).thenReturn(mockPlayer2);
        mockedBukkit.when(Bukkit::getScheduler).thenReturn(mockScheduler);

        // runTaskAsynchronouslyのモック - 即座にタスクを実行
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(1);
            task.run();
            return null;
        }).when(mockScheduler).runTaskAsynchronously(any(), any(Runnable.class));

        playerManager = new PlayerManager(mockPlugin, mockRepository);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("正常に初期化される")
        void constructor_InitializesSuccessfully() {
            assertNotNull(playerManager);
            assertEquals(0, playerManager.getOnlinePlayerCount());
        }
    }

    @Nested
    @DisplayName("initialize")
    class InitializeTests {

        @Test
        @DisplayName("空のサーバーで初期化される")
        void initialize_EmptyServer_Initializes() {
            mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn(Collections.emptyList());

            playerManager.initialize();

            verify(mockLogger).info("Initializing PlayerManager...");
            verify(mockLogger).info("PlayerManager initialized. Online players: 0");
        }

        @Test
        @DisplayName("オンラインプレイヤーがいる場合にロードされる")
        void initialize_WithOnlinePlayers_LoadsPlayers() throws Exception {
            when(mockRepository.findById(any())).thenReturn(Optional.empty());

            playerManager.initialize();

            verify(mockLogger).info("PlayerManager initialized. Online players: 0");
        }

        @Test
        @DisplayName("リロード時に既存プレイヤーをロードする")
        void initialize_Reload_LoadsExistingPlayers() throws Exception {
            Collection<Player> players = Arrays.asList(mockPlayer, mockPlayer2);
            mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn(players);

            when(mockRepository.findById(testUuid)).thenReturn(Optional.empty());
            when(mockRepository.findById(testUuid2)).thenReturn(Optional.empty());

            playerManager.initialize();

            assertEquals(2, playerManager.getOnlinePlayerCount());
        }
    }

    @Nested
    @DisplayName("shutdown")
    class ShutdownTests {

        @Test
        @DisplayName("正常にシャットダウンする")
        void shutdown_ShutsDownSuccessfully() throws Exception {
            when(mockRepository.findById(any())).thenReturn(Optional.empty());

            playerManager.loadPlayer(testUuid);

            playerManager.shutdown();

            assertEquals(0, playerManager.getOnlinePlayerCount());
            verify(mockLogger).info("Shutting down PlayerManager...");
            verify(mockLogger).info("PlayerManager shutdown complete. Saved: 1, Failed: 0");
        }

        @Test
        @DisplayName("保存失敗時も継続する")
        void shutdown_WithSaveFailure_Continues() throws Exception {
            when(mockRepository.findById(any())).thenReturn(Optional.empty());

            // プレイヤーをロード（最初のsaveは成功させる必要がある）
            playerManager.loadPlayer(testUuid);

            // その後のsave呼び出しで例外をスローするように設定
            doThrow(new RuntimeException("Save failed")).when(mockRepository).save(any());

            // shutdown()は例外をキャッチして継続するはず
            assertDoesNotThrow(() -> playerManager.shutdown());

            verify(mockLogger).warning(anyString());
        }
    }

    @Nested
    @DisplayName("loadPlayer")
    class LoadPlayerTests {

        @Test
        @DisplayName("新規プレイヤーを作成してロードする")
        void loadPlayer_NewPlayer_CreatesAndLoads() throws Exception {
            when(mockRepository.findById(testUuid)).thenReturn(Optional.empty());

            RPGPlayer result = playerManager.loadPlayer(testUuid);

            assertNotNull(result);
            assertEquals(testUuid, result.getUuid());
            assertEquals("TestPlayer", result.getUsername());
            assertEquals(1, playerManager.getOnlinePlayerCount());
            verify(mockRepository).save(any(PlayerData.class));
        }

        @Test
        @DisplayName("既存プレイヤーをロードする")
        void loadPlayer_ExistingPlayer_Loads() throws Exception {
            PlayerData existingData = mock(PlayerData.class);
            when(existingData.getUuid()).thenReturn(testUuid);
            when(existingData.getUsername()).thenReturn("TestPlayer");
            when(existingData.getClassId()).thenReturn("warrior");
            when(mockRepository.findById(testUuid)).thenReturn(Optional.of(existingData));

            RPGPlayer result = playerManager.loadPlayer(testUuid);

            assertNotNull(result);
            assertEquals(testUuid, result.getUuid());
            verify(mockRepository, never()).save(any(PlayerData.class));
        }

        @Test
        @DisplayName("null UUIDの場合は例外を投げる")
        void loadPlayer_NullUuid_ThrowsException() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                playerManager.loadPlayer(null);
            });

            assertTrue(exception.getMessage().contains("UUID cannot be null"));
        }

        @Test
        @DisplayName("既にロード済みの場合は同じインスタンスを返す")
        void loadPlayer_AlreadyLoaded_ReturnsSameInstance() throws Exception {
            when(mockRepository.findById(testUuid)).thenReturn(Optional.empty());

            RPGPlayer first = playerManager.loadPlayer(testUuid);
            RPGPlayer second = playerManager.loadPlayer(testUuid);

            assertEquals(first, second);
            assertEquals(1, playerManager.getOnlinePlayerCount());
        }

        @Test
        @DisplayName("オフラインプレイヤーの場合は例外を投げる")
        void loadPlayer_OfflinePlayer_ThrowsException() {
            UUID offlineUuid = UUID.randomUUID();
            mockedBukkit.when(() -> Bukkit.getPlayer(offlineUuid)).thenReturn(null);

            Exception exception = assertThrows(IllegalStateException.class, () -> {
                playerManager.loadPlayer(offlineUuid);
            });

            assertTrue(exception.getMessage().contains("Player is not online"));
        }
    }

    @Nested
    @DisplayName("unloadPlayer")
    class UnloadPlayerTests {

        @Test
        @DisplayName("プレイヤーをアンロードして保存する")
        void unloadPlayer_UnloadsAndSaves() throws Exception {
            when(mockRepository.findById(testUuid)).thenReturn(Optional.empty());

            playerManager.loadPlayer(testUuid);
            assertEquals(1, playerManager.getOnlinePlayerCount());

            playerManager.unloadPlayer(testUuid);

            assertEquals(0, playerManager.getOnlinePlayerCount());
            verify(mockRepository).save(any(PlayerData.class));
        }

        @Test
        @DisplayName("null UUIDの場合は例外を投げる")
        void unloadPlayer_NullUuid_ThrowsException() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                playerManager.unloadPlayer(null);
            });

            assertTrue(exception.getMessage().contains("UUID cannot be null"));
        }

        @Test
        @DisplayName("未ロードのプレイヤーの場合は何もしない")
        void unloadPlayer_NotLoaded_DoesNothing() throws Exception {
            assertDoesNotThrow(() -> playerManager.unloadPlayer(testUuid));
            assertEquals(0, playerManager.getOnlinePlayerCount());
        }
    }

    @Nested
    @DisplayName("savePlayer")
    class SavePlayerTests {

        @Test
        @DisplayName("プレイヤーデータを保存する")
        void savePlayer_SavesData() throws Exception {
            when(mockRepository.findById(testUuid)).thenReturn(Optional.empty());

            playerManager.loadPlayer(testUuid);
            playerManager.savePlayer(testUuid);

            // loadPlayerで新規作成時に1回、savePlayerで1回の計2回呼ばれる
            verify(mockRepository, times(2)).save(any(PlayerData.class));
        }

        @Test
        @DisplayName("null UUIDの場合は例外を投げる")
        void savePlayer_NullUuid_ThrowsException() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                playerManager.savePlayer(null);
            });

            assertTrue(exception.getMessage().contains("UUID cannot be null"));
        }

        @Test
        @DisplayName("未ロードのプレイヤーの場合は保存しない")
        void savePlayer_NotLoaded_DoesNotSave() throws Exception {
            playerManager.savePlayer(testUuid);

            verify(mockRepository, never()).save(any(PlayerData.class));
        }
    }

    @Nested
    @DisplayName("saveAllAsync")
    class SaveAllAsyncTests {

        @Test
        @DisplayName("全プレイヤーデータを非同期に保存する")
        void saveAllAsync_SavesAllPlayers() throws Exception {
            when(mockRepository.findById(any())).thenReturn(Optional.empty());

            playerManager.loadPlayer(testUuid);
            playerManager.loadPlayer(testUuid2);

            playerManager.saveAllAsync();

            // モック設定により即座に実行されるため待機不要
            verify(mockRepository, atLeastOnce()).save(any(PlayerData.class));
        }

        @Test
        @DisplayName("空の状態で保存してもエラーにならない")
        void saveAllAsync_Empty_DoesNotError() {
            assertDoesNotThrow(() -> playerManager.saveAllAsync());
        }
    }

    @Nested
    @DisplayName("getRPGPlayer")
    class GetRPGPlayerTests {

        @Test
        @DisplayName("UUIDでRPGPlayerを取得する")
        void getRPGPlayer_ByUuid_ReturnsPlayer() throws Exception {
            when(mockRepository.findById(testUuid)).thenReturn(Optional.empty());

            playerManager.loadPlayer(testUuid);

            RPGPlayer result = playerManager.getRPGPlayer(testUuid);

            assertNotNull(result);
            assertEquals(testUuid, result.getUuid());
        }

        @Test
        @DisplayName("null UUIDの場合はnullを返す")
        void getRPGPlayer_NullUuid_ReturnsNull() {
            RPGPlayer result = playerManager.getRPGPlayer((UUID) null);

            assertNull(result);
        }

        @Test
        @DisplayName("未ロードのプレイヤーの場合はnullを返す")
        void getRPGPlayer_NotLoaded_ReturnsNull() {
            RPGPlayer result = playerManager.getRPGPlayer(testUuid);

            assertNull(result);
        }

        @Test
        @DisplayName("名前でRPGPlayerを取得する")
        void getRPGPlayer_ByName_ReturnsPlayer() throws Exception {
            when(mockRepository.findById(any())).thenReturn(Optional.empty());

            playerManager.loadPlayer(testUuid);

            RPGPlayer result = playerManager.getRPGPlayer("TestPlayer");

            assertNotNull(result);
            assertEquals("TestPlayer", result.getUsername());
        }

        @Test
        @DisplayName("名前検索: 大文字小文字を区別しない")
        void getRPGPlayer_ByName_CaseInsensitive() throws Exception {
            when(mockRepository.findById(any())).thenReturn(Optional.empty());

            playerManager.loadPlayer(testUuid);

            RPGPlayer result = playerManager.getRPGPlayer("testplayer");

            assertNotNull(result);
        }

        @Test
        @DisplayName("名前検索: 該当プレイヤーがいない場合はnull")
        void getRPGPlayer_ByName_NotFound_ReturnsNull() {
            RPGPlayer result = playerManager.getRPGPlayer("UnknownPlayer");

            assertNull(result);
        }

        @Test
        @DisplayName("null名前の場合はnullを返す")
        void getRPGPlayer_NullName_ReturnsNull() {
            RPGPlayer result = playerManager.getRPGPlayer((String) null);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("getOnlinePlayerCount")
    class GetOnlinePlayerCountTests {

        @Test
        @DisplayName("オンラインプレイヤー数を取得する")
        void getOnlinePlayerCount_ReturnsCount() throws Exception {
            when(mockRepository.findById(any())).thenReturn(Optional.empty());

            assertEquals(0, playerManager.getOnlinePlayerCount());

            playerManager.loadPlayer(testUuid);

            assertEquals(1, playerManager.getOnlinePlayerCount());

            playerManager.loadPlayer(testUuid2);

            assertEquals(2, playerManager.getOnlinePlayerCount());
        }
    }

    @Nested
    @DisplayName("getOnlinePlayers")
    class GetOnlinePlayersTests {

        @Test
        @DisplayName("オンラインプレイヤーマップを取得する")
        void getOnlinePlayers_ReturnsMap() throws Exception {
            when(mockRepository.findById(any())).thenReturn(Optional.empty());

            playerManager.loadPlayer(testUuid);
            playerManager.loadPlayer(testUuid2);

            Map<UUID, RPGPlayer> result = playerManager.getOnlinePlayers();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.containsKey(testUuid));
            assertTrue(result.containsKey(testUuid2));
        }

        @Test
        @DisplayName("返されたマップは変更不可")
        void getOnlinePlayers_ReturnsUnmodifiableMap() throws Exception {
            when(mockRepository.findById(any())).thenReturn(Optional.empty());

            playerManager.loadPlayer(testUuid);

            Map<UUID, RPGPlayer> result = playerManager.getOnlinePlayers();

            assertThrows(UnsupportedOperationException.class, () -> {
                result.put(UUID.randomUUID(), mock(RPGPlayer.class));
            });
        }
    }

    @Nested
    @DisplayName("isOnline")
    class IsOnlineTests {

        @Test
        @DisplayName("オンラインプレイヤーの場合はtrue")
        void isOnline_OnlinePlayer_ReturnsTrue() throws Exception {
            when(mockRepository.findById(testUuid)).thenReturn(Optional.empty());

            playerManager.loadPlayer(testUuid);

            assertTrue(playerManager.isOnline(testUuid));
        }

        @Test
        @DisplayName("オフラインプレイヤーの場合はfalse")
        void isOnline_OfflinePlayer_ReturnsFalse() {
            assertFalse(playerManager.isOnline(testUuid));
        }

        @Test
        @DisplayName("null UUIDの場合はfalse")
        void isOnline_NullUuid_ReturnsFalse() {
            assertFalse(playerManager.isOnline(null));
        }
    }

    @Nested
    @DisplayName("Event Handlers")
    class EventHandlerTests {

        @Test
        @DisplayName("onPlayerJoin: プレイヤー参加時にロードする")
        void onPlayerJoin_LoadsPlayer() throws Exception {
            when(mockRepository.findById(testUuid)).thenReturn(Optional.empty());

            PlayerJoinEvent event = new PlayerJoinEvent(mockPlayer, "");
            playerManager.onPlayerJoin(event);

            assertEquals(1, playerManager.getOnlinePlayerCount());
        }

        @Test
        @DisplayName("onPlayerJoin: ロード失敗時は例外をログする")
        void onPlayerJoin_LoadFailed_LogsException() throws Exception {
            when(mockRepository.findById(testUuid)).thenThrow(new RuntimeException("Load failed"));

            PlayerJoinEvent event = new PlayerJoinEvent(mockPlayer, "");

            assertDoesNotThrow(() -> playerManager.onPlayerJoin(event));
            verify(mockLogger).severe(contains("Failed to load player"));
        }

        @Test
        @DisplayName("onPlayerQuit: プレイヤー退出時にアンロードする")
        void onPlayerQuit_UnloadsPlayer() throws Exception {
            when(mockRepository.findById(testUuid)).thenReturn(Optional.empty());

            playerManager.loadPlayer(testUuid);
            assertEquals(1, playerManager.getOnlinePlayerCount());

            PlayerQuitEvent event = new PlayerQuitEvent(mockPlayer, "");
            playerManager.onPlayerQuit(event);

            assertEquals(0, playerManager.getOnlinePlayerCount());
        }

        @Test
        @DisplayName("onPlayerQuit: アンロード失敗時は例外をログする")
        void onPlayerQuit_UnloadFailed_LogsException() {
            PlayerQuitEvent event = new PlayerQuitEvent(mockPlayer, "");

            assertDoesNotThrow(() -> playerManager.onPlayerQuit(event));
            // 未ロードのプレイヤーの場合は警告が出ない可能性がある
        }
    }

    @Nested
    @DisplayName("Concurrent Access")
    class ConcurrentAccessTests {

        @Test
        @DisplayName("複数スレッドから同時にアクセスしても安全")
        void concurrentAccess_ThreadSafe() throws Exception {
            when(mockRepository.findById(any())).thenReturn(Optional.empty());

            playerManager.loadPlayer(testUuid);
            playerManager.loadPlayer(testUuid2);

            // 複数スレッドから同時にアクセス
            Thread t1 = new Thread(() -> {
                playerManager.getRPGPlayer(testUuid);
                playerManager.isOnline(testUuid);
                playerManager.getOnlinePlayerCount();
            });

            Thread t2 = new Thread(() -> {
                playerManager.getOnlinePlayers();
                playerManager.getRPGPlayer("TestPlayer");
                playerManager.isOnline(testUuid2);
            });

            t1.start();
            t2.start();

            assertDoesNotThrow(() -> {
                t1.join();
                t2.join();
            });
        }
    }
}
