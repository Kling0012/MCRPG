package com.example.rpgplugin.player;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.core.config.YamlConfigManager;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * VanillaExpHandlerのテストクラス
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: VanillaExpHandlerのテストに特化</li>
 *   <li>KISS: シンプルなテストケース</li>
 *   <li>読みやすさ: テスト名で振る舞いを明示</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("VanillaExpHandler テスト")
class VanillaExpHandlerTest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private ExpDiminisher mockExpDiminisher;

    @Mock
    private Player mockPlayer;

    @Mock
    private YamlConfigManager mockConfigManager;

    @Mock
    private RPGPlayer mockRpgPlayer;

    @Mock
    private SkillManager mockSkillManager;

    @Mock
    private SkillManager.PlayerSkillData mockSkillData;

    @Mock
    private Logger mockLogger;

    @Mock
    private StatManager mockStatManager;

    @Mock
    private World mockWorld;

    private UUID testUuid;
    private VanillaExpHandler vanillaExpHandler;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();

        // Plugin setup
        when(mockPlugin.getLogger()).thenReturn(mockLogger);
        when(mockPlugin.getConfigManager()).thenReturn(mockConfigManager);
        when(mockPlugin.getSkillManager()).thenReturn(mockSkillManager);

        // Player setup
        when(mockPlayer.getUniqueId()).thenReturn(testUuid);
        when(mockPlayer.getName()).thenReturn("TestPlayer");
        when(mockPlayer.getLevel()).thenReturn(10);
        when(mockPlayer.isOnline()).thenReturn(true);
        when(mockPlayer.getWorld()).thenReturn(mockWorld);
        when(mockWorld.getName()).thenReturn("world");
        when(mockPlayer.getLocation()).thenReturn(new Location(mockWorld, 0, 0, 0));

        // RPGPlayer setup
        when(mockRpgPlayer.getUuid()).thenReturn(testUuid);
        when(mockRpgPlayer.getUsername()).thenReturn("TestPlayer");
        when(mockRpgPlayer.getAvailablePoints()).thenReturn(8);
        when(mockRpgPlayer.getBukkitPlayer()).thenReturn(mockPlayer);
        when(mockRpgPlayer.getStatManager()).thenReturn(mockStatManager);

        // SkillData setup
        when(mockSkillData.getSkillPoints()).thenReturn(5);

        // SkillManager setup
        when(mockSkillManager.getPlayerSkillData(testUuid)).thenReturn(mockSkillData);

        // ConfigManager setup - default values
        when(mockConfigManager.getBoolean("main", "debug.log_exp_changes", false)).thenReturn(false);
        when(mockConfigManager.getBoolean("main", "level_up.show_title", true)).thenReturn(true);
        when(mockConfigManager.getBoolean("main", "level_up.play_sound", true)).thenReturn(true);

        vanillaExpHandler = new VanillaExpHandler(mockPlugin, mockPlayerManager, mockExpDiminisher);
    }

    // ==================== コンストラクタ ====================

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("正常に初期化される")
        void constructor_InitializesSuccessfully() {
            assertNotNull(vanillaExpHandler);
        }
    }

    // ==================== onPlayerLevelChange ====================

    @Nested
    @DisplayName("onPlayerLevelChange")
    class OnPlayerLevelChangeTests {

        @Test
        @DisplayName("レベルアップ時にステータスを配分する")
        void onPlayerLevelChange_LevelUp_AllocatesStats() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getBaseStat(any())).thenReturn(10);

            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 10, 11);
            vanillaExpHandler.onPlayerLevelChange(event);

            // 全ステータスに+2される
            for (Stat stat : Stat.values()) {
                verify(mockRpgPlayer).setBaseStat(stat, 12);
            }
            // 手動配分ポイント+3
            verify(mockRpgPlayer).addAvailablePoints(3);
            // スキルポイント+1
            verify(mockSkillData).addSkillPoints(1);
            // 総レベル更新
            verify(mockStatManager).setTotalLevel(11);
        }

        @Test
        @DisplayName("レベルアップメッセージを送信する")
        void onPlayerLevelChange_LevelUp_SendsMessage() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getBaseStat(any())).thenReturn(10);

            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 10, 11);
            vanillaExpHandler.onPlayerLevelChange(event);

            // メッセージ送信を検証（8回のsendMessage呼び出し）
            verify(mockPlayer, atLeast(8)).sendMessage(anyString());
        }

        @Test
        @DisplayName("タイトルとサウンドを再生する")
        void onPlayerLevelChange_LevelUp_ShowsTitleAndPlaysSound() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getBaseStat(any())).thenReturn(10);

            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 10, 11);
            vanillaExpHandler.onPlayerLevelChange(event);

            // タイトル表示
            verify(mockPlayer).showTitle(any(Title.class));
            // サウンド再生
            verify(mockPlayer).playSound(any(Location.class), eq(Sound.ENTITY_PLAYER_LEVELUP), eq(1.0f), eq(1.0f));
        }

        @Test
        @DisplayName("タイトル無効時はタイトルを表示しない")
        void onPlayerLevelChange_TitleDisabled_DoesNotShowTitle() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getBaseStat(any())).thenReturn(10);
            when(mockConfigManager.getBoolean("main", "level_up.show_title", true)).thenReturn(false);

            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 10, 11);
            vanillaExpHandler.onPlayerLevelChange(event);

            verify(mockPlayer, never()).showTitle(any(Title.class));
            // サウンドは再生される
            verify(mockPlayer).playSound(any(Location.class), eq(Sound.ENTITY_PLAYER_LEVELUP), eq(1.0f), eq(1.0f));
        }

        @Test
        @DisplayName("サウンド無効時はサウンドを再生しない")
        void onPlayerLevelChange_SoundDisabled_DoesNotPlaySound() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getBaseStat(any())).thenReturn(10);
            when(mockConfigManager.getBoolean("main", "level_up.play_sound", true)).thenReturn(false);

            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 10, 11);
            vanillaExpHandler.onPlayerLevelChange(event);

            // タイトルは表示される
            verify(mockPlayer).showTitle(any(Title.class));
            // サウンドは再生されない
            verify(mockPlayer, never()).playSound(any(Location.class), any(Sound.class), anyFloat(), anyFloat());
        }

        @Test
        @DisplayName("タイトルとサウンド両方無効時は何も再生しない")
        void onPlayerLevelChange_BothDisabled_NoEffects() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getBaseStat(any())).thenReturn(10);
            when(mockConfigManager.getBoolean("main", "level_up.show_title", true)).thenReturn(false);
            when(mockConfigManager.getBoolean("main", "level_up.play_sound", true)).thenReturn(false);

            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 10, 11);
            vanillaExpHandler.onPlayerLevelChange(event);

            verify(mockPlayer, never()).showTitle(any(Title.class));
            verify(mockPlayer, never()).playSound(any(Location.class), any(Sound.class), anyFloat(), anyFloat());
        }

        @Test
        @DisplayName("レベルダウン時は何もしない")
        void onPlayerLevelChange_LevelDown_DoesNothing() {
            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 11, 10);

            assertDoesNotThrow(() -> vanillaExpHandler.onPlayerLevelChange(event));
            verify(mockPlayerManager, never()).getRPGPlayer(any(UUID.class));
            verify(mockPlayer, never()).sendMessage(anyString());
        }

        @Test
        @DisplayName("同じレベルの場合は何もしない")
        void onPlayerLevelChange_SameLevel_DoesNothing() {
            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 10, 10);

            assertDoesNotThrow(() -> vanillaExpHandler.onPlayerLevelChange(event));
            verify(mockPlayerManager, never()).getRPGPlayer(any(UUID.class));
            verify(mockPlayer, never()).sendMessage(anyString());
        }

        @Test
        @DisplayName("RPGPlayer未取得時に警告をログする")
        void onPlayerLevelChange_NoRpgPlayer_LogsWarning() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(null);

            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 10, 11);

            assertDoesNotThrow(() -> vanillaExpHandler.onPlayerLevelChange(event));
            verify(mockLogger).warning(contains("RPGPlayer not found"));
            verify(mockPlayer, never()).sendMessage(anyString());
        }

        @Test
        @DisplayName("例外発生時にエラーをログする")
        void onPlayerLevelChange_Exception_LogsError() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getBaseStat(any())).thenThrow(new RuntimeException("Test error"));

            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 10, 11);

            assertDoesNotThrow(() -> vanillaExpHandler.onPlayerLevelChange(event));
            verify(mockLogger).severe(contains("Failed to handle level up"));
        }
    }

    // ==================== onPlayerExpChange ====================

    @Nested
    @DisplayName("onPlayerExpChange")
    class OnPlayerExpChangeTests {

        @Test
        @DisplayName("経験値減衰を適用する")
        void onPlayerExpChange_AppliesDiminishment() {
            PlayerExpChangeEvent event = new PlayerExpChangeEvent(mockPlayer, 100);

            vanillaExpHandler.onPlayerExpChange(event);

            verify(mockExpDiminisher).applyDiminishment(event);
        }

        @Test
        @DisplayName("デバッグログ無効時はログを出力しない")
        void onPlayerExpChange_DebugDisabled_NoLog() {
            PlayerExpChangeEvent event = new PlayerExpChangeEvent(mockPlayer, 50);

            vanillaExpHandler.onPlayerExpChange(event);

            verify(mockExpDiminisher).applyDiminishment(event);
            verify(mockLogger, never()).fine(anyString());
        }

        @Test
        @DisplayName("デバッグログ有効時にログを出力する")
        void onPlayerExpChange_DebugEnabled_LogsExp() {
            when(mockConfigManager.getBoolean("main", "debug.log_exp_changes", false)).thenReturn(true);
            PlayerExpChangeEvent event = new PlayerExpChangeEvent(mockPlayer, 50);

            vanillaExpHandler.onPlayerExpChange(event);

            verify(mockExpDiminisher).applyDiminishment(event);
            verify(mockLogger).fine(contains("gained"));
        }
    }

    // ==================== handleLevelUp ====================

    @Nested
    @DisplayName("handleLevelUp (経由テスト)")
    class HandleLevelUpTests {

        @Test
        @DisplayName("BukkitPlayerがnullの場合は何もしない")
        void handleLevelUp_NullBukkitPlayer_DoesNothing() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getBukkitPlayer()).thenReturn(null);

            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 10, 11);

            assertDoesNotThrow(() -> vanillaExpHandler.onPlayerLevelChange(event));
            verify(mockPlayer, never()).sendMessage(anyString());
            verify(mockStatManager, never()).setTotalLevel(anyInt());
        }

        @Test
        @DisplayName("SkillManagerがnullの場合でも処理を継続する")
        void handleLevelUp_NoSkillManager_Continues() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockPlugin.getSkillManager()).thenReturn(null);
            when(mockRpgPlayer.getBaseStat(any())).thenReturn(10);

            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 10, 11);

            assertDoesNotThrow(() -> vanillaExpHandler.onPlayerLevelChange(event));

            // ステータス配分は行われる
            verify(mockRpgPlayer, atLeastOnce()).setBaseStat(any(), anyInt());
            verify(mockRpgPlayer).addAvailablePoints(3);
            // メッセージは送信される
            verify(mockPlayer, atLeast(1)).sendMessage(anyString());
        }

        @Test
        @DisplayName("スキルポイントを表示するメッセージを送信する")
        void handleLevelUp_WithSkillManager_ShowsSkillPoints() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getBaseStat(any())).thenReturn(10);

            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 10, 11);
            vanillaExpHandler.onPlayerLevelChange(event);

            // スキルポイント+1
            verify(mockSkillData).addSkillPoints(1);
        }
    }

    // ==================== 定数テスト ====================

    @Nested
    @DisplayName("Constants")
    class ConstantTests {

        @Test
        @DisplayName("自動配分ポイント定数を確認")
        void autoAllocatePoints_IsTwo() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getBaseStat(any())).thenReturn(0);

            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 10, 11);
            vanillaExpHandler.onPlayerLevelChange(event);

            // 全ステータス+2
            for (Stat stat : Stat.values()) {
                verify(mockRpgPlayer).setBaseStat(stat, 2);
            }
        }

        @Test
        @DisplayName("手動配分ポイント定数を確認")
        void manualAllocatePoints_IsThree() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getBaseStat(any())).thenReturn(10);

            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 10, 11);
            vanillaExpHandler.onPlayerLevelChange(event);

            verify(mockRpgPlayer).addAvailablePoints(3);
        }
    }

    // ==================== 複数回レベルアップ ====================

    @Nested
    @DisplayName("Multiple Level Ups")
    class MultipleLevelUpTests {

        @Test
        @DisplayName("複数回レベルアップしても正しく動作する")
        void multipleLevelUps_WorkCorrectly() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getBaseStat(any())).thenReturn(10);

            // 最初のレベルアップ: 10 -> 11
            PlayerLevelChangeEvent event1 = new PlayerLevelChangeEvent(mockPlayer, 10, 11);
            vanillaExpHandler.onPlayerLevelChange(event1);

            // 次のレベルアップ: 11 -> 12
            PlayerLevelChangeEvent event2 = new PlayerLevelChangeEvent(mockPlayer, 11, 12);
            vanillaExpHandler.onPlayerLevelChange(event2);

            // 各レベルアップで+3ポイント
            verify(mockRpgPlayer, times(2)).addAvailablePoints(3);
            // 各レベルアップでスキルポイント+1
            verify(mockSkillData, times(2)).addSkillPoints(1);
        }

        @Test
        @DisplayName("大きなレベル差でも正しく動作する")
        void bigLevelJump_WorkCorrectly() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getBaseStat(any())).thenReturn(10);

            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 10, 20);

            assertDoesNotThrow(() -> vanillaExpHandler.onPlayerLevelChange(event));

            // 1回分の配分のみ（イベント1回につき1回）
            verify(mockRpgPlayer).addAvailablePoints(3);
        }
    }

    // ==================== メッセージ内容テスト ====================

    @Nested
    @DisplayName("Level Up Messages")
    class LevelUpMessageTests {

        @Test
        @DisplayName("レベルアップメッセージの内容を検証する")
        void levelUpMessage_ContainsExpectedContent() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getBaseStat(any())).thenReturn(10);
            when(mockRpgPlayer.getAvailablePoints()).thenReturn(8);

            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(mockPlayer, 10, 11);
            vanillaExpHandler.onPlayerLevelChange(event);

            // メッセージパターンを検証
            verify(mockPlayer).sendMessage(contains("レベルアップ!"));
            verify(mockPlayer).sendMessage(contains("レベル 10"));
            verify(mockPlayer).sendMessage(contains("レベル 11"));
            verify(mockPlayer).sendMessage(contains("自動配分"));
            verify(mockPlayer).sendMessage(contains("手動配分ポイント"));
        }
    }
}
