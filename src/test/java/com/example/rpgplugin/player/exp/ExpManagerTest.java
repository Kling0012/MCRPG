package com.example.rpgplugin.player.exp;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.ExpDiminisher;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.VanillaExpHandler;
import com.example.rpgplugin.rpgclass.ClassManager;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ExpManagerのテストクラス
 */
@DisplayName("ExpManager テスト")
@ExtendWith(MockitoExtension.class)
class ExpManagerTest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private ClassManager mockClassManager;

    @Mock
    private Logger mockLogger;

    private ExpManager expManager;

    @BeforeEach
    void setUp() {
        lenient().when(mockPlugin.getLogger()).thenReturn(mockLogger);
        expManager = new ExpManager(mockPlugin, mockPlayerManager, mockClassManager);
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("正常に初期化される")
        void constructor_InitializesSuccessfully() {
            assertNotNull(expManager);
            assertNotNull(expManager.getExpDiminisher());
            assertNotNull(expManager.getVanillaExpHandler());
        }
    }

    @Nested
    @DisplayName("initialize")
    class InitializeTests {

        @Test
        @DisplayName("初期化メッセージをログ出力する")
        void initialize_LogsMessages() {
            expManager.initialize();

            verify(mockLogger).info("[ExpManager] 経験値システムを初期化中...");
            verify(mockLogger).info("[ExpManager] 経験値システムの初期化が完了しました");
        }
    }

    @Nested
    @DisplayName("registerListeners")
    class RegisterListenersTests {

        @Test
        @DisplayName("VanillaExpHandlerを登録する")
        void registerListeners_RegistersHandler() {
            Server mockServer = mock(Server.class);
            PluginManager mockPluginManager = mock(PluginManager.class);
            lenient().when(mockPlugin.getServer()).thenReturn(mockServer);
            lenient().when(mockServer.getPluginManager()).thenReturn(mockPluginManager);

            expManager.registerListeners();

            verify(mockLogger).info("[ExpManager] VanillaExpHandlerを登録しました");
        }
    }

    @Nested
    @DisplayName("shutdown")
    class ShutdownTests {

        @Test
        @DisplayName("シャットダウンメッセージをログ出力する")
        void shutdown_LogsMessages() {
            expManager.shutdown();

            verify(mockLogger).info("[ExpManager] 経験値システムをシャットダウン中...");
            verify(mockLogger).info("[ExpManager] 経験値システムのシャットダウンが完了しました");
        }
    }

    @Nested
    @DisplayName("Getters")
    class GetterTests {

        @Test
        @DisplayName("getExpDiminisher: ExpDiminisherを返す")
        void getExpDiminisher_ReturnsExpDiminisher() {
            assertNotNull(expManager.getExpDiminisher());
        }

        @Test
        @DisplayName("getVanillaExpHandler: VanillaExpHandlerを返す")
        void getVanillaExpHandler_ReturnsVanillaExpHandler() {
            assertNotNull(expManager.getVanillaExpHandler());
        }
    }
}
