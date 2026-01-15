package com.example.rpgplugin.rpgclass.requirements;

import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * StatRequirementのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("StatRequirement Tests")
class StatRequirementTest {

    @Mock
    private Player mockPlayer;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private RPGPlayer mockRpgPlayer;

    @Mock
    private ConfigurationSection mockConfig;

    private static final UUID TEST_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(mockPlayer.getUniqueId()).thenReturn(TEST_UUID);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("コンストラクタ - 正しい値")
        void constructorWithValidValues() {
            StatRequirement requirement = new StatRequirement(Stat.STRENGTH, 50, mockPlayerManager);
            assertEquals(Stat.STRENGTH, requirement.getRequiredStat());
            assertEquals(50, requirement.getRequiredValue());
        }

        @Test
        @DisplayName("コンストラクタ - 0は0に調整される")
        void constructorWithZeroAdjustsToZero() {
            StatRequirement requirement = new StatRequirement(Stat.VITALITY, 0, mockPlayerManager);
            assertEquals(0, requirement.getRequiredValue());
        }

        @Test
        @DisplayName("コンストラクタ - 負の値は0に調整される")
        void constructorWithNegativeAdjustsToZero() {
            StatRequirement requirement = new StatRequirement(Stat.INTELLIGENCE, -10, mockPlayerManager);
            assertEquals(0, requirement.getRequiredValue());
        }

        @Test
        @DisplayName("コンストラクタ - 大きな値")
        void constructorWithLargeValue() {
            StatRequirement requirement = new StatRequirement(Stat.DEXTERITY, 10000, mockPlayerManager);
            assertEquals(10000, requirement.getRequiredValue());
        }

        @Test
        @DisplayName("コンストラクタ - 全ステータスタイプ")
        void constructorWithAllStatTypes() {
            StatRequirement strReq = new StatRequirement(Stat.STRENGTH, 10, mockPlayerManager);
            StatRequirement vitReq = new StatRequirement(Stat.VITALITY, 10, mockPlayerManager);
            StatRequirement intReq = new StatRequirement(Stat.INTELLIGENCE, 10, mockPlayerManager);
            StatRequirement spiReq = new StatRequirement(Stat.SPIRIT, 10, mockPlayerManager);
            StatRequirement dexReq = new StatRequirement(Stat.DEXTERITY, 10, mockPlayerManager);

            assertEquals(Stat.STRENGTH, strReq.getRequiredStat());
            assertEquals(Stat.VITALITY, vitReq.getRequiredStat());
            assertEquals(Stat.INTELLIGENCE, intReq.getRequiredStat());
            assertEquals(Stat.SPIRIT, spiReq.getRequiredStat());
            assertEquals(Stat.DEXTERITY, dexReq.getRequiredStat());
        }
    }

    @Nested
    @DisplayName("check() Tests")
    class CheckTests {

        @Test
        @DisplayName("check - nullプレイヤーはfalse")
        void checkWithNullPlayer() {
            StatRequirement requirement = new StatRequirement(Stat.STRENGTH, 50, mockPlayerManager);
            assertFalse(requirement.check(null));
        }

        @Test
        @DisplayName("check - RPGPlayerがnullはfalse")
        void checkWithNullRpgPlayer() {
            when(mockPlayerManager.getRPGPlayer(TEST_UUID)).thenReturn(null);
            StatRequirement requirement = new StatRequirement(Stat.STRENGTH, 50, mockPlayerManager);
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - ステータスが要件を満たす")
        void checkWithSufficientStat() {
            when(mockPlayerManager.getRPGPlayer(TEST_UUID)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getFinalStat(Stat.VITALITY)).thenReturn(75);

            StatRequirement requirement = new StatRequirement(Stat.VITALITY, 50, mockPlayerManager);
            assertTrue(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - ステータスが要件と一致")
        void checkWithExactStat() {
            when(mockPlayerManager.getRPGPlayer(TEST_UUID)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getFinalStat(Stat.INTELLIGENCE)).thenReturn(60);

            StatRequirement requirement = new StatRequirement(Stat.INTELLIGENCE, 60, mockPlayerManager);
            assertTrue(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - ステータスが不足")
        void checkWithInsufficientStat() {
            when(mockPlayerManager.getRPGPlayer(TEST_UUID)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getFinalStat(Stat.DEXTERITY)).thenReturn(25);

            StatRequirement requirement = new StatRequirement(Stat.DEXTERITY, 50, mockPlayerManager);
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - 0要件は常に満たす")
        void checkWithZeroRequirement() {
            when(mockPlayerManager.getRPGPlayer(TEST_UUID)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getFinalStat(Stat.SPIRIT)).thenReturn(0);

            StatRequirement requirement = new StatRequirement(Stat.SPIRIT, 0, mockPlayerManager);
            assertTrue(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - 高いステータス要件")
        void checkWithHighStatRequirement() {
            when(mockPlayerManager.getRPGPlayer(TEST_UUID)).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.getFinalStat(Stat.STRENGTH)).thenReturn(999);

            StatRequirement requirement = new StatRequirement(Stat.STRENGTH, 1000, mockPlayerManager);
            assertFalse(requirement.check(mockPlayer));

            when(mockRpgPlayer.getFinalStat(Stat.STRENGTH)).thenReturn(1000);
            assertTrue(requirement.check(mockPlayer));
        }
    }

    @Nested
    @DisplayName("getDescription() Tests")
    class GetDescriptionTests {

        @Test
        @DisplayName("getDescription - 正しいフォーマット")
        void getDescriptionFormat() {
            StatRequirement requirement = new StatRequirement(Stat.STRENGTH, 50, mockPlayerManager);
            assertEquals("筋力 が 50 以上", requirement.getDescription());
        }

        @Test
        @DisplayName("getDescription - 各ステータスの表示名")
        void getDescriptionForEachStat() {
            StatRequirement strReq = new StatRequirement(Stat.STRENGTH, 10, mockPlayerManager);
            StatRequirement vitReq = new StatRequirement(Stat.VITALITY, 10, mockPlayerManager);
            StatRequirement intReq = new StatRequirement(Stat.INTELLIGENCE, 10, mockPlayerManager);
            StatRequirement spiReq = new StatRequirement(Stat.SPIRIT, 10, mockPlayerManager);
            StatRequirement dexReq = new StatRequirement(Stat.DEXTERITY, 10, mockPlayerManager);

            assertEquals("筋力 が 10 以上", strReq.getDescription());
            assertEquals("体力 が 10 以上", vitReq.getDescription());
            assertEquals("知力 が 10 以上", intReq.getDescription());
            assertEquals("精神 が 10 以上", spiReq.getDescription());
            assertEquals("器用さ が 10 以上", dexReq.getDescription());
        }

        @Test
        @DisplayName("getDescription - 0要件")
        void getDescriptionWithZero() {
            StatRequirement requirement = new StatRequirement(Stat.VITALITY, 0, mockPlayerManager);
            assertEquals("体力 が 0 以上", requirement.getDescription());
        }
    }

    @Nested
    @DisplayName("getType() Tests")
    class GetTypeTests {

        @Test
        @DisplayName("getType - statを返す")
        void getTypeReturnsStat() {
            StatRequirement requirement = new StatRequirement(Stat.DEXTERITY, 30, mockPlayerManager);
            assertEquals("stat", requirement.getType());
        }
    }

    @Nested
    @DisplayName("parse() Tests")
    class ParseTests {

        @Test
        @DisplayName("parse - 正常パース")
        void parseWithValidValues() {
            when(mockConfig.getString("stat", "STRENGTH")).thenReturn("STRENGTH");
            when(mockConfig.getInt("value", 0)).thenReturn(50);

            StatRequirement requirement = StatRequirement.parse(mockConfig, mockPlayerManager);
            assertEquals(Stat.STRENGTH, requirement.getRequiredStat());
            assertEquals(50, requirement.getRequiredValue());
        }

        @Test
        @DisplayName("parse - デフォルト値")
        void parseWithDefaults() {
            when(mockConfig.getString("stat", "STRENGTH")).thenReturn("STRENGTH");
            when(mockConfig.getInt("value", 0)).thenReturn(0);

            StatRequirement requirement = StatRequirement.parse(mockConfig, mockPlayerManager);
            assertEquals(Stat.STRENGTH, requirement.getRequiredStat());
            assertEquals(0, requirement.getRequiredValue());
        }

        @Test
        @DisplayName("parse - 小文字ステータス名")
        void parseWithLowerCaseStatName() {
            when(mockConfig.getString("stat", "STRENGTH")).thenReturn("vitality");
            when(mockConfig.getInt("value", 0)).thenReturn(25);

            StatRequirement requirement = StatRequirement.parse(mockConfig, mockPlayerManager);
            assertEquals(Stat.VITALITY, requirement.getRequiredStat());
        }

        @Test
        @DisplayName("parse - 不明なステータス名で例外")
        void parseWithInvalidStatNameThrowsException() {
            when(mockConfig.getString("stat", "STRENGTH")).thenReturn("INVALID_STAT");
            when(mockConfig.getInt("value", 0)).thenReturn(10);

            assertThrows(IllegalArgumentException.class, () -> {
                StatRequirement.parse(mockConfig, mockPlayerManager);
            });
        }

        @Test
        @DisplayName("parse - 全ステータスタイプ")
        void parseAllStatTypes() {
            String[] statNames = {"STRENGTH", "VITALITY", "INTELLIGENCE", "SPIRIT", "DEXTERITY"};
            Stat[] expectedStats = {Stat.STRENGTH, Stat.VITALITY, Stat.INTELLIGENCE, Stat.SPIRIT, Stat.DEXTERITY};

            for (int i = 0; i < statNames.length; i++) {
                when(mockConfig.getString("stat", "STRENGTH")).thenReturn(statNames[i]);
                when(mockConfig.getInt("value", 0)).thenReturn(10);

                StatRequirement requirement = StatRequirement.parse(mockConfig, mockPlayerManager);
                assertEquals(expectedStats[i], requirement.getRequiredStat(),
                    "Stat type should match for " + statNames[i]);
            }
        }
    }

    @Nested
    @DisplayName("Getter Methods Tests")
    class GetterMethodsTests {

        @Test
        @DisplayName("getRequiredStat - 正しく返す")
        void getRequiredStatReturnsCorrect() {
            StatRequirement requirement = new StatRequirement(Stat.SPIRIT, 100, mockPlayerManager);
            assertEquals(Stat.SPIRIT, requirement.getRequiredStat());
        }

        @Test
        @DisplayName("getRequiredValue - 正しく返す")
        void getRequiredValueReturnsCorrect() {
            StatRequirement requirement = new StatRequirement(Stat.INTELLIGENCE, 75, mockPlayerManager);
            assertEquals(75, requirement.getRequiredValue());
        }
    }
}
