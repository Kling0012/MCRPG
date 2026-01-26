package com.example.rpgplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RPGListenerの単体テスト
 *
 * <p>イベントリスナーの機能テスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("RPGListener テスト")
@ExtendWith(MockitoExtension.class)
class RPGListenerTest {

    @Mock
    private Player mockPlayer;

    @Mock
    private PlayerJoinEvent mockEvent;

    private MockedStatic<RPGPlugin> mockedPlugin;
    private RPGPlugin mockPluginInstance;
    private Logger logger;
    private RPGListener rpgListener;

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger("TestLogger");

        // RPGPluginの静的モック設定
        mockedPlugin = mockStatic(RPGPlugin.class);
        mockPluginInstance = mock(RPGPlugin.class);
        mockedPlugin.when(RPGPlugin::getInstance).thenReturn(mockPluginInstance);
        lenient().when(mockPluginInstance.getLogger()).thenReturn(logger);

        // PlayerJoinEventモック設定
        lenient().when(mockEvent.getPlayer()).thenReturn(mockPlayer);

        rpgListener = new RPGListener();
    }

    @AfterEach
    void tearDown() {
        if (mockedPlugin != null) {
            mockedPlugin.close();
        }
    }

    // ==================== onPlayerJoin テスト ====================

    @Test
    @DisplayName("onPlayerJoin: 正常時にウェルカムメッセージを送信")
    void onPlayerJoin_Normal_SendsWelcomeMessage() {
        rpgListener.onPlayerJoin(mockEvent);

        verify(mockPlayer).sendMessage(contains("ようこそサーバーへ"));
        verify(mockPlayer).sendMessage(contains("RPGPlugin"));
    }

    @Test
    @DisplayName("onPlayerJoin: プレイヤーがnullの場合でも安全に処理")
    void onPlayerJoin_NullPlayer_SafeHandling() {
        when(mockEvent.getPlayer()).thenReturn(null);

        assertDoesNotThrow(() -> rpgListener.onPlayerJoin(mockEvent));
    }

    @Test
    @DisplayName("onPlayerJoin: 例外発生時に警告ログを出力")
    void onPlayerJoin_Exception_LogsWarning() {
        when(mockEvent.getPlayer()).thenThrow(new RuntimeException("Test exception"));

        assertDoesNotThrow(() -> rpgListener.onPlayerJoin(mockEvent));
    }

    @Test
    @DisplayName("onPlayerJoin: NullPointerExceptionもキャッチされる")
    void onPlayerJoin_NullPointerException_LogsWarning() {
        when(mockEvent.getPlayer()).thenThrow(new NullPointerException("Test NPE"));

        assertDoesNotThrow(() -> rpgListener.onPlayerJoin(mockEvent));
    }
}
