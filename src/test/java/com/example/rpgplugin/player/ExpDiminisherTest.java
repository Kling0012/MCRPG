package com.example.rpgplugin.player;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.core.config.YamlConfigManager;
import com.example.rpgplugin.player.config.DiminishConfig;
import com.example.rpgplugin.rpgclass.ClassManager;
import com.example.rpgplugin.rpgclass.RPGClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ExpDiminisherのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ExpDiminisher Tests")
class ExpDiminisherTest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private ClassManager mockClassManager;

    @Mock
    private Logger mockLogger;

    @Mock
    private YamlConfigManager mockConfigManager;

    @Mock
    private Player mockPlayer;

    @Mock
    private RPGPlayer mockRpgPlayer;

    @Mock
    private RPGClass mockRpgClass;

    @Mock
    private RPGClass.ExpDiminish mockExpDiminish;

    @Mock
    private PersistentDataContainer mockPdc;

    private MockedStatic<Bukkit> mockedBukkit;

    private ExpDiminisher expDiminisher;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();

        when(mockPlugin.getLogger()).thenReturn(mockLogger);
        when(mockPlugin.getConfigManager()).thenReturn(mockConfigManager);
        when(mockPlayer.getUniqueId()).thenReturn(testUuid);
        when(mockPlayer.getName()).thenReturn("TestPlayer");
        when(mockPlayer.getLevel()).thenReturn(30);
        when(mockPlayer.getPersistentDataContainer()).thenReturn(mockPdc);
        when(mockRpgPlayer.getUsername()).thenReturn("TestPlayer");

        expDiminisher = new ExpDiminisher(mockPlugin, mockPlayerManager, mockClassManager);
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
            assertNotNull(expDiminisher);
            assertNotNull(expDiminisher.getDiminishConfig());
        }

        @Test
        @DisplayName("コンストラクタで設定がロードされる")
        void constructor_LoadsConfig() {
            verify(mockLogger).info(contains("Loaded diminish_config.yml"));
        }
    }

    @Nested
    @DisplayName("ExpSource Enum")
    class ExpSourceEnumTests {

        @Test
        @DisplayName("ExpSourceの全値を確認")
        void expSource_HasAllValues() {
            ExpDiminisher.ExpSource[] values = ExpDiminisher.ExpSource.values();

            assertEquals(4, values.length);
            assertEquals(ExpDiminisher.ExpSource.NORMAL, values[0]);
            assertEquals(ExpDiminisher.ExpSource.PLAYER_KILL, values[1]);
            assertEquals(ExpDiminisher.ExpSource.BOSS_MOB, values[2]);
            assertEquals(ExpDiminisher.ExpSource.EVENT_REWARD, values[3]);
        }

        @Test
        @DisplayName("ExpSourceのvalueOfが正しく動作する")
        void expSource_ValueOf_WorksCorrectly() {
            assertEquals(ExpDiminisher.ExpSource.NORMAL, ExpDiminisher.ExpSource.valueOf("NORMAL"));
            assertEquals(ExpDiminisher.ExpSource.PLAYER_KILL, ExpDiminisher.ExpSource.valueOf("PLAYER_KILL"));
            assertEquals(ExpDiminisher.ExpSource.BOSS_MOB, ExpDiminisher.ExpSource.valueOf("BOSS_MOB"));
            assertEquals(ExpDiminisher.ExpSource.EVENT_REWARD, ExpDiminisher.ExpSource.valueOf("EVENT_REWARD"));
        }
    }

    @Nested
    @DisplayName("applyDiminishment")
    class ApplyDiminishmentTests {

        @Test
        @DisplayName("減衰無効時は経験値を変更しない")
        void applyDiminishment_Disabled_DoesNotModify() {
            when(mockConfigManager.getBoolean(anyString(), anyString(), anyBoolean())).thenReturn(false);

            PlayerExpChangeEvent event = new PlayerExpChangeEvent(mockPlayer, 100);
            int originalAmount = event.getAmount();

            expDiminisher.applyDiminishment(event);

            assertEquals(originalAmount, event.getAmount());
        }

        @Test
        @DisplayName("RPGPlayer未取得時は経験値を変更しない")
        void applyDiminishment_NoRpgPlayer_DoesNotModify() {
            when(mockConfigManager.getBoolean(anyString(), anyString(), eq(true))).thenReturn(true);
            when(mockConfigManager.getBoolean(anyString(), anyString(), eq(false))).thenReturn(false);
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(null);

            PlayerExpChangeEvent event = new PlayerExpChangeEvent(mockPlayer, 100);

            expDiminisher.applyDiminishment(event);

            assertEquals(100, event.getAmount());
        }

        @Test
        @DisplayName("クラス未設定時は経験値を変更しない")
        void applyDiminishment_NoClass_DoesNotModify() {
            when(mockConfigManager.getBoolean(anyString(), anyString(), eq(true))).thenReturn(true);
            when(mockConfigManager.getBoolean(anyString(), anyString(), eq(false))).thenReturn(false);
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.empty());

            PlayerExpChangeEvent event = new PlayerExpChangeEvent(mockPlayer, 100);

            expDiminisher.applyDiminishment(event);

            assertEquals(100, event.getAmount());
        }
    }

    @Nested
    @DisplayName("calculateDiminishedExp")
    class CalculateDiminishedExpTests {

        @Test
        @DisplayName("0以下の経験値はそのまま返す")
        void calculateDiminishedExp_ZeroOrNegative_ReturnsOriginal() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);

            assertEquals(0, expDiminisher.calculateDiminishedExp(mockPlayer, 0));
            assertEquals(-10, expDiminisher.calculateDiminishedExp(mockPlayer, -10));
        }

        @Test
        @DisplayName("RPGPlayer未取得時は元の経験値を返す")
        void calculateDiminishedExp_NoRpgPlayer_ReturnsOriginal() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(null);

            assertEquals(100, expDiminisher.calculateDiminishedExp(mockPlayer, 100));
            verify(mockLogger).warning(contains("RPGPlayer not found"));
        }

        @Test
        @DisplayName("正常に減衰計算を行う")
        void calculateDiminishedExp_Valid_CalculatesDiminishment() {
            when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.of(mockRpgClass));
            when(mockRpgClass.getExpDiminish()).thenReturn(mockExpDiminish);
            when(mockExpDiminish.getStartLevel()).thenReturn(20);
            when(mockExpDiminish.getReductionRate()).thenReturn(0.5);
            when(mockExpDiminish.applyExp(100, 30)).thenReturn(50L);

            int result = expDiminisher.calculateDiminishedExp(mockPlayer, 100);

            assertEquals(50, result);
        }
    }

    @Nested
    @DisplayName("grantExemptedExp")
    class GrantExemptedExpTests {

        @Test
        @DisplayName("除外経験値を付与する")
        void grantExemptedExp_GrantsExp() {
            when(mockPdc.get(any(), eq(PersistentDataType.STRING))).thenReturn(null);

            ExpDiminisher.grantExemptedExp(mockPlayer, 100, ExpDiminisher.ExpSource.PLAYER_KILL);

            verify(mockPdc).set(any(), eq(PersistentDataType.STRING), eq("PLAYER_KILL"));
            verify(mockPlayer).giveExp(100);
        }

        @Test
        @DisplayName("ソース指定なしでイベント報酬として付与する")
        void grantExemptedExp_NoSource_DefaultsToEventReward() {
            when(mockPdc.get(any(), eq(PersistentDataType.STRING))).thenReturn(null);

            ExpDiminisher.grantExemptedExp(mockPlayer, 100);

            verify(mockPdc).set(any(), eq(PersistentDataType.STRING), eq("EVENT_REWARD"));
            verify(mockPlayer).giveExp(100);
        }
    }

    @Nested
    @DisplayName("getDiminishRate")
    class GetDiminishRateTests {

        @Test
        @DisplayName("減衰率を取得する")
        void getDiminishRate_ReturnsRate() {
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.of(mockRpgClass));
            when(mockRpgClass.getExpDiminish()).thenReturn(mockExpDiminish);
            when(mockExpDiminish.getStartLevel()).thenReturn(20);
            when(mockExpDiminish.getReductionRate()).thenReturn(0.5);
            when(mockPlayer.getLevel()).thenReturn(25);

            double rate = expDiminisher.getDiminishRate(mockPlayer);

            assertEquals(0.5, rate, 0.01);
        }

        @Test
        @DisplayName("クラス未設定時は0を返す")
        void getDiminishRate_NoClass_ReturnsZero() {
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.empty());

            assertEquals(0.0, expDiminisher.getDiminishRate(mockPlayer), 0.01);
        }

        @Test
        @DisplayName("ExpDiminishがnullの場合は0を返す")
        void getDiminishRate_NoExpDiminish_ReturnsZero() {
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.of(mockRpgClass));
            when(mockRpgClass.getExpDiminish()).thenReturn(null);

            assertEquals(0.0, expDiminisher.getDiminishRate(mockPlayer), 0.01);
        }

        @Test
        @DisplayName("開始レベル未満の場合は0を返す")
        void getDiminishRate_BelowStartLevel_ReturnsZero() {
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.of(mockRpgClass));
            when(mockRpgClass.getExpDiminish()).thenReturn(mockExpDiminish);
            when(mockExpDiminish.getStartLevel()).thenReturn(30);
            when(mockPlayer.getLevel()).thenReturn(25);

            assertEquals(0.0, expDiminisher.getDiminishRate(mockPlayer), 0.01);
        }
    }

    @Nested
    @DisplayName("getDiminishStartLevel")
    class GetDiminishStartLevelTests {

        @Test
        @DisplayName("開始レベルを取得する")
        void getDiminishStartLevel_ReturnsStartLevel() {
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.of(mockRpgClass));
            when(mockRpgClass.getExpDiminish()).thenReturn(mockExpDiminish);
            when(mockExpDiminish.getStartLevel()).thenReturn(30);

            int level = expDiminisher.getDiminishStartLevel(mockPlayer);

            assertEquals(30, level);
        }

        @Test
        @DisplayName("クラス未設定時はMAX_VALUEを返す")
        void getDiminishStartLevel_NoClass_ReturnsMaxValue() {
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.empty());

            assertEquals(Integer.MAX_VALUE, expDiminisher.getDiminishStartLevel(mockPlayer));
        }

        @Test
        @DisplayName("ExpDiminishがnullの場合はMAX_VALUEを返す")
        void getDiminishStartLevel_NoExpDiminish_ReturnsMaxValue() {
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.of(mockRpgClass));
            when(mockRpgClass.getExpDiminish()).thenReturn(null);

            assertEquals(Integer.MAX_VALUE, expDiminisher.getDiminishStartLevel(mockPlayer));
        }
    }

    @Nested
    @DisplayName("Exemption Constants")
    class ExemptionConstantsTests {

        @Test
        @DisplayName("除外定数が正しく定義されている")
        void exemptionConstants_AreDefined() {
            assertEquals("player_kill", ExpDiminisher.EXEMPTION_PLAYER_KILL);
            assertEquals("boss_mob", ExpDiminisher.EXEMPTION_BOSS_MOB);
            assertEquals("event_reward", ExpDiminisher.EXEMPTION_EVENT_REWARD);
        }
    }

    @Nested
    @DisplayName("loadConfig")
    class LoadConfigTests {

        @Test
        @DisplayName("設定を再ロードする")
        void loadConfig_ReloadsConfig() {
            expDiminisher.loadConfig();

            // コンストラクタで1回、明示的に呼び出しで1回の計2回
            verify(mockLogger, times(2)).info(contains("Loaded diminish_config.yml"));
        }
    }

    @Nested
    @DisplayName("getDiminishConfig")
    class GetDiminishConfigTests {

        @Test
        @DisplayName("減衰設定を取得する")
        void getDiminishConfig_ReturnsConfig() {
            DiminishConfig config = expDiminisher.getDiminishConfig();

            assertNotNull(config);
        }
    }

    @Nested
    @DisplayName("Static Methods")
    class StaticMethodTests {

        @Test
        @DisplayName("grantExemptedExp: 全てのソース種別で動作する")
        void grantExemptedExp_AllSources_Works() {
            when(mockPdc.get(any(), eq(PersistentDataType.STRING))).thenReturn(null);

            ExpDiminisher.grantExemptedExp(mockPlayer, 100, ExpDiminisher.ExpSource.NORMAL);
            verify(mockPlayer).giveExp(100);

            ExpDiminisher.grantExemptedExp(mockPlayer, 100, ExpDiminisher.ExpSource.PLAYER_KILL);
            verify(mockPlayer, times(2)).giveExp(100);

            ExpDiminisher.grantExemptedExp(mockPlayer, 100, ExpDiminisher.ExpSource.BOSS_MOB);
            verify(mockPlayer, times(3)).giveExp(100);

            ExpDiminisher.grantExemptedExp(mockPlayer, 100, ExpDiminisher.ExpSource.EVENT_REWARD);
            verify(mockPlayer, times(4)).giveExp(100);
        }
    }
}
