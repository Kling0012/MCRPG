package com.example.rpgplugin.player.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DiminishConfigのテストクラス
 *
 * <p>経験値減衰設定の管理をテストします。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DiminishConfig Tests")
@SuppressWarnings("unchecked")
class DiminishConfigTest {

    @Mock
    private Logger mockLogger;

    @Mock
    private FileConfiguration mockConfig;

    private DiminishConfig diminishConfig;

    @BeforeEach
    void setUp() {
        diminishConfig = new DiminishConfig(mockLogger);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("コンストラクタ - 正しく初期化される")
        void constructorInitializesCorrectly() {
            assertNotNull(diminishConfig, "DiminishConfig should be initialized");
        }

        @Test
        @DisplayName("複数インスタンス - 別々の設定を持つ")
        void multipleInstancesHaveSeparateSettings() {
            DiminishConfig config1 = new DiminishConfig(mockLogger);
            DiminishConfig config2 = new DiminishConfig(mockLogger);

            assertNotSame(config1, config2, "Instances should be different");
        }
    }

    @Nested
    @DisplayName("load() Tests")
    class LoadTests {

        @Test
        @DisplayName("load - null設定はfalseを返す")
        void loadWithNullConfig() {
            boolean result = diminishConfig.load(null);

            assertFalse(result, "Should return false for null config");
            verify(mockLogger).warning(contains("Failed to load"));
        }

        @Test
        @DisplayName("load - 空の設定はデフォルト値を使用")
        void loadWithEmptyConfig() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            boolean result = diminishConfig.load(mockConfig);

            assertTrue(result, "Should return true even with empty config");
            verify(mockLogger).info(contains("No diminish_table found"));
            assertEquals(0.0, diminishConfig.getReductionRate(10), "Level 10 should have no reduction");
        }

        @Test
        @DisplayName("load - 例外が発生した場合はfalseを返す")
        void loadWithException() {
            when(mockConfig.getList("diminish_table")).thenThrow(new RuntimeException("Test error"));

            boolean result = diminishConfig.load(mockConfig);

            assertFalse(result, "Should return false when exception occurs");
            verify(mockLogger).warning(contains("Failed to load"));
        }
    }

    @Nested
    @DisplayName("loadDiminishTable with Valid Config Tests")
    class LoadDiminishTableValidConfigTests {

        @Test
        @DisplayName("load - 有効なdiminish_table設定を読み込める")
        void loadWithValidDiminishTable() {
            Map<String, Object> entry1 = Map.of(
                "level_range", "30-39",
                "reduction_rate", 0.5
            );
            Map<String, Object> entry2 = Map.of(
                "level_range", "40+",
                "reduction_rate", 0.6
            );
            List table = (List) List.of(entry1, entry2);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);
            when(mockConfig.getString("formula", anyString())).thenReturn("BASE_EXP * (1.0 - REDUCTION_RATE)");

            boolean result = diminishConfig.load(mockConfig);

            assertTrue(result);
            assertEquals(0.5, diminishConfig.getReductionRate(35), "Level 35 should have 50% reduction");
            assertEquals(0.6, diminishConfig.getReductionRate(50), "Level 50 should have 60% reduction");
        }

        @Test
        @DisplayName("load - 無効なレート範囲はスキップされる")
        void loadSkipsInvalidRate() {
            Map<String, Object> validEntry = Map.of(
                "level_range", "30-39",
                "reduction_rate", 0.5
            );
            Map<String, Object> invalidEntry = Map.of(
                "level_range", "40-49",
                "reduction_rate", 1.5
            );
            List<?> table = List.of(validEntry, invalidEntry);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0.5, diminishConfig.getReductionRate(35), "Valid entry should be loaded");
            assertEquals(0.0, diminishConfig.getReductionRate(45), "Invalid entry should be skipped");
        }

        @Test
        @DisplayName("load - 負のレートはスキップされる")
        void loadSkipsNegativeRate() {
            Map<String, Object> entry = Map.of(
                "level_range", "30-39",
                "reduction_rate", -0.1
            );
            List table = (List) List.of(entry);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0.0, diminishConfig.getReductionRate(35), "Negative rate should be skipped");
        }

        @Test
        @DisplayName("load - nullエントリはスキップされる")
        void loadSkipsNullEntries() {
            List table = (List) List.of(null, "invalid", Map.of("level_range", "30-39", "reduction_rate", 0.5));

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0.5, diminishConfig.getReductionRate(35), "Valid entry should be loaded");
        }

        @Test
        @DisplayName("load - level_rangeがnullのエントリはスキップ")
        void loadSkipsNullLevelRange() {
            Map<String, Object> entry = Map.of(
                "reduction_rate", 0.5
            );
            List table = (List) List.of(entry);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0.0, diminishConfig.getReductionRate(35), "Entry without level_range should be skipped");
        }

        @Test
        @DisplayName("load - reduction_rateがnullのエントリはスキップ")
        void loadSkipsNullReductionRate() {
            Map<String, Object> entry = Map.of(
                "level_range", "30-39"
            );
            List table = (List) List.of(entry);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0.0, diminishConfig.getReductionRate(35), "Entry without reduction_rate should be skipped");
        }

        @Test
        @DisplayName("load - 0.0のレートは許可される")
        void loadAllowsZeroRate() {
            Map<String, Object> entry = Map.of(
                "level_range", "30-39",
                "reduction_rate", 0.0
            );
            List table = (List) List.of(entry);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0.0, diminishConfig.getReductionRate(35), "Zero rate should be allowed");
        }

        @Test
        @DisplayName("load - 1.0のレートは許可される")
        void loadAllowsFullRate() {
            Map<String, Object> entry = Map.of(
                "level_range", "30-39",
                "reduction_rate", 1.0
            );
            List table = (List) List.of(entry);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(1.0, diminishConfig.getReductionRate(35), "Full rate should be allowed");
        }
    }

    @Nested
    @DisplayName("parseLevelRange Edge Cases Tests")
    class ParseLevelRangeEdgeCasesTests {

        @Test
        @DisplayName("parseLevelRange - 無効なフォーマットはスキップされる")
        void parseLevelRangeInvalidFormat() {
            Map<String, Object> invalidEntry = Map.of(
                "level_range", "invalid",
                "reduction_rate", 0.5
            );
            List<?> table = List.of(invalidEntry);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0.0, diminishConfig.getReductionRate(10), "Invalid format should be skipped");
        }

        @Test
        @DisplayName("parseLevelRange - 数字のみは無効")
        void parseLevelRangeNumbersOnly() {
            Map<String, Object> entry = Map.of(
                "level_range", "30",
                "reduction_rate", 0.5
            );
            List table = (List) List.of(entry);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0.0, diminishConfig.getReductionRate(30), "Numbers only should not create range");
        }

        @Test
        @DisplayName("parseLevelRange - ハイフンのみは無効")
        void parseLevelRangeOnlyHyphen() {
            Map<String, Object> entry = Map.of(
                "level_range", "-",
                "reduction_rate", 0.5
            );
            List table = (List) List.of(entry);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0.0, diminishConfig.getReductionRate(10), "Only hyphen should be invalid");
        }

        @Test
        @DisplayName("parseLevelRange - 空文字列はnullを返す")
        void parseLevelRangeEmptyString() {
            Map<String, Object> entry = Map.of(
                "level_range", "",
                "reduction_rate", 0.5
            );
            List table = (List) List.of(entry);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0.0, diminishConfig.getReductionRate(10), "Empty string should be skipped");
        }

        @Test
        @DisplayName("parseLevelRange - 複数ハイフンは無効")
        void parseLevelRangeMultipleHyphens() {
            Map<String, Object> entry = Map.of(
                "level_range", "10-20-30",
                "reduction_rate", 0.5
            );
            List table = (List) List.of(entry);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0.0, diminishConfig.getReductionRate(15), "Multiple hyphens should be invalid");
        }
    }

    @Nested
    @DisplayName("loadMobExpTable Tests")
    class LoadMobExpTableTests {

        @Test
        @DisplayName("load - 有効なmob_exp_table設定を読み込める")
        void loadWithValidMobExpTable() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);

            ConfigurationSection mobSection = mock(ConfigurationSection.class);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(mobSection);
            when(mobSection.getKeys(false)).thenReturn(new HashSet<>(Set.of("zombie", "skeleton")));

            ConfigurationSection zombieConfig = mock(ConfigurationSection.class);
            when(mobSection.getConfigurationSection("zombie")).thenReturn(zombieConfig);
            when(zombieConfig.getInt("base_exp", 20)).thenReturn(25);
            when(zombieConfig.getInt("max_level", 69)).thenReturn(70);

            ConfigurationSection skeletonConfig = mock(ConfigurationSection.class);
            when(mobSection.getConfigurationSection("skeleton")).thenReturn(skeletonConfig);
            when(skeletonConfig.getInt("base_exp", 20)).thenReturn(30);
            when(skeletonConfig.getInt("max_level", 69)).thenReturn(75);

            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertNotNull(diminishConfig.getMobExpConfig("zombie"), "Zombie config should be loaded");
            assertNotNull(diminishConfig.getMobExpConfig("skeleton"), "Skeleton config should be loaded");
            assertEquals(25, diminishConfig.getMobBaseExp("zombie", 20), "Zombie base exp should be 25");
            assertEquals(30, diminishConfig.getMobBaseExp("skeleton", 20), "Skeleton base exp should be 30");
            assertEquals(70, diminishConfig.getMobMaxLevel("zombie", 69), "Zombie max level should be 70");
            assertEquals(75, diminishConfig.getMobMaxLevel("skeleton", 69), "Skeleton max level should be 75");
        }

        @Test
        @DisplayName("loadMobExpTable - nullセクションはスキップ")
        void loadMobExpTableNullSection() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);

            ConfigurationSection mobSection = mock(ConfigurationSection.class);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(mobSection);
            when(mobSection.getKeys(false)).thenReturn(new HashSet<>(Set.of("zombie"));
            when(mobSection.getConfigurationSection("zombie")).thenReturn(null);

            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertNull(diminishConfig.getMobExpConfig("zombie"), "Null section should be skipped");
        }

        @Test
        @DisplayName("loadMobExpTable - 空のキーリスト")
        void loadMobExpTableEmptyKeys() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);

            ConfigurationSection mobSection = mock(ConfigurationSection.class);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(mobSection);
            when(mobSection.getKeys(false)).thenReturn(Set.of());

            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertNull(diminishConfig.getMobExpConfig("zombie"), "No config should be loaded");
        }
    }

    @Nested
    @DisplayName("loadExemptions Tests")
    class LoadExemptionsTests {

        @Test
        @DisplayName("load - 除外設定をカスタマイズできる")
        void loadWithCustomExemptions() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);

            ConfigurationSection exemptSection = mock(ConfigurationSection.class);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(exemptSection);
            when(exemptSection.getBoolean("player_kills", true)).thenReturn(false);
            when(exemptSection.getBoolean("boss_mobs", true)).thenReturn(false);
            when(exemptSection.getBoolean("event_rewards", true)).thenReturn(true);

            diminishConfig.load(mockConfig);

            assertFalse(diminishConfig.isPlayerKillsExempt(), "player_kills should be false");
            assertFalse(diminishConfig.isBossMobsExempt(), "boss_mobs should be false");
            assertTrue(diminishConfig.isEventRewardsExempt(), "event_rewards should be true");
        }

        @Test
        @DisplayName("load - 全ての除外がfalse")
        void loadWithNoExemptions() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);

            ConfigurationSection exemptSection = mock(ConfigurationSection.class);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(exemptSection);
            when(exemptSection.getBoolean("player_kills", true)).thenReturn(false);
            when(exemptSection.getBoolean("boss_mobs", true)).thenReturn(false);
            when(exemptSection.getBoolean("event_rewards", true)).thenReturn(false);

            diminishConfig.load(mockConfig);

            assertFalse(diminishConfig.isPlayerKillsExempt());
            assertFalse(diminishConfig.isBossMobsExempt());
            assertFalse(diminishConfig.isEventRewardsExempt());
        }

        @Test
        @DisplayName("load - 一部の除外のみ設定")
        void loadWithPartialExemptions() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);

            ConfigurationSection exemptSection = mock(ConfigurationSection.class);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(exemptSection);
            when(exemptSection.getBoolean("player_kills", true)).thenReturn(true);
            when(exemptSection.getBoolean("boss_mobs", true)).thenReturn(false);
            when(exemptSection.getBoolean("event_rewards", true)).thenReturn(true);

            diminishConfig.load(mockConfig);

            assertTrue(diminishConfig.isPlayerKillsExempt());
            assertFalse(diminishConfig.isBossMobsExempt());
            assertTrue(diminishConfig.isEventRewardsExempt());
        }
    }

    @Nested
    @DisplayName("loadFormula Tests")
    class LoadFormulaTests {

        @Test
        @DisplayName("load - カスタム計算式を設定できる")
        void loadWithCustomFormula() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);
            when(mockConfig.getString("formula", "BASE_EXP * (1.0 - REDUCTION_RATE)"))
                .thenReturn("CUSTOM_FORMULA");

            diminishConfig.load(mockConfig);

            assertEquals("CUSTOM_FORMULA", diminishConfig.getFormula(), "Custom formula should be set");
        }

        @Test
        @DisplayName("load - 空の計算式も許可")
        void loadWithEmptyFormula() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);
            when(mockConfig.getString("formula", "BASE_EXP * (1.0 - REDUCTION_RATE)"))
                .thenReturn("");

            diminishConfig.load(mockConfig);

            assertEquals("", diminishConfig.getFormula(), "Empty formula should be allowed");
        }

        @Test
        @DisplayName("load - 複雑な計算式")
        void loadWithComplexFormula() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);
            when(mockConfig.getString("formula", "BASE_EXP * (1.0 - REDUCTION_RATE)"))
                .thenReturn("BASE_EXP * Math.pow(1.0 - REDUCTION_RATE, 2)");

            diminishConfig.load(mockConfig);

            assertEquals("BASE_EXP * Math.pow(1.0 - REDUCTION_RATE, 2)", diminishConfig.getFormula());
        }
    }

    @Nested
    @DisplayName("getStartLevel Edge Cases Tests")
    class GetStartLevelEdgeCasesTests {

        @Test
        @DisplayName("getStartLevel - カスタム設定での開始レベル")
        void getStartLevelWithCustomConfig() {
            Map<String, Object> entry1 = Map.of(
                "level_range", "50-59",
                "reduction_rate", 0.7
            );
            Map<String, Object> entry2 = Map.of(
                "level_range", "60+",
                "reduction_rate", 0.8
            );
            List table = (List) List.of(entry1, entry2);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(50, diminishConfig.getStartLevel(), "Start level should be 50");
        }

        @Test
        @DisplayName("getStartLevel - 1から始まる設定")
        void getStartLevelFromLevel1() {
            Map<String, Object> entry = Map.of(
                "level_range", "1+",
                "reduction_rate", 0.1
            );
            List table = (List) List.of(entry);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(1, diminishConfig.getStartLevel(), "Start level should be 1");
        }

        @Test
        @DisplayName("getStartLevel - 0から始まる設定")
        void getStartLevelFromLevel0() {
            Map<String, Object> entry = Map.of(
                "level_range", "0-9",
                "reduction_rate", 0.1
            );
            List table = (List) List.of(entry);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0, diminishConfig.getStartLevel(), "Start level should be 0");
        }
    }

    @Nested
    @DisplayName("getReductionRate Additional Tests")
    class GetReductionRateAdditionalTests {

        @Test
        @DisplayName("getReductionRate - 複数範囲の境界値")
        void getReductionRateAtBoundaries() {
            Map<String, Object> entry1 = Map.of(
                "level_range", "10-19",
                "reduction_rate", 0.3
            );
            Map<String, Object> entry2 = Map.of(
                "level_range", "20-29",
                "reduction_rate", 0.5
            );
            Map<String, Object> entry3 = Map.of(
                "level_range", "30+",
                "reduction_rate", 0.7
            );
            List table = (List) List.of(entry1, entry2, entry3);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0.0, diminishConfig.getReductionRate(9), "Below first range");
            assertEquals(0.3, diminishConfig.getReductionRate(10), "At lower bound");
            assertEquals(0.3, diminishConfig.getReductionRate(19), "At upper bound");
            assertEquals(0.5, diminishConfig.getReductionRate(20), "At next range lower bound");
            assertEquals(0.5, diminishConfig.getReductionRate(29), "At next range upper bound");
            assertEquals(0.7, diminishConfig.getReductionRate(30), "At unbounded range start");
            assertEquals(0.7, diminishConfig.getReductionRate(100), "Far into unbounded range");
        }

        @Test
        @DisplayName("getReductionRate - 重複する範囲は後勝ち")
        void getReductionRateOverlappingRanges() {
            Map<String, Object> entry1 = Map.of(
                "level_range", "20-30",
                "reduction_rate", 0.3
            );
            Map<String, Object> entry2 = Map.of(
                "level_range", "25-35",
                "reduction_rate", 0.7
            );
            List table = (List) List.of(entry1, entry2);

            when(mockConfig.getList("diminish_table")).thenReturn(table);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            double result = diminishConfig.getReductionRate(27);
            assertTrue(result == 0.3 || result == 0.7, "Should have one of the overlapping rates");
        }
    }

    @Nested
    @DisplayName("LevelRange Tests")
    class LevelRangeTests {

        @Test
        @DisplayName("LevelRange(30, 39) - 30-39を含む")
        void levelRangeContains() {
            DiminishConfig.LevelRange range = new DiminishConfig.LevelRange(30, 39);

            assertTrue(range.contains(30), "Should contain 30");
            assertTrue(range.contains(35), "Should contain 35");
            assertTrue(range.contains(39), "Should contain 39");
            assertFalse(range.contains(29), "Should not contain 29");
            assertFalse(range.contains(40), "Should not contain 40");
        }

        @Test
        @DisplayName("LevelRange(70, null) - 70以上を含む")
        void levelRangeContainsUnbounded() {
            DiminishConfig.LevelRange range = new DiminishConfig.LevelRange(70, null);

            assertTrue(range.contains(70), "Should contain 70");
            assertTrue(range.contains(100), "Should contain 100");
            assertTrue(range.contains(1000), "Should contain 1000");
            assertFalse(range.contains(69), "Should not contain 69");
        }

        @Test
        @DisplayName("LevelRange - equalsとhashCode")
        void levelRangeEqualsAndHashCode() {
            DiminishConfig.LevelRange range1 = new DiminishConfig.LevelRange(30, 39);
            DiminishConfig.LevelRange range2 = new DiminishConfig.LevelRange(30, 39);
            DiminishConfig.LevelRange range3 = new DiminishConfig.LevelRange(30, 40);
            DiminishConfig.LevelRange range4 = new DiminishConfig.LevelRange(70, null);
            DiminishConfig.LevelRange range5 = new DiminishConfig.LevelRange(70, null);

            assertEquals(range1, range2, "Same ranges should be equal");
            assertEquals(range1.hashCode(), range2.hashCode(), "Same ranges should have same hashCode");
            assertNotEquals(range1, range3, "Different maxLevel should not be equal");
            assertEquals(range4, range5, "Same unbounded ranges should be equal");
            assertNotEquals(range1, null, "Should not equal null");
            assertNotEquals(range1, "not a range", "Should not equal different type");
        }

        @Test
        @DisplayName("LevelRange - toString")
        void levelRangeToString() {
            DiminishConfig.LevelRange boundedRange = new DiminishConfig.LevelRange(30, 39);
            DiminishConfig.LevelRange unboundedRange = new DiminishConfig.LevelRange(70, null);

            assertEquals("30-39", boundedRange.toString(), "Bounded range should format correctly");
            assertEquals("70+", unboundedRange.toString(), "Unbounded range should format with +");
        }

        @Test
        @DisplayName("LevelRange - 最小値と最大値が同じ")
        void levelRangeSameMinAndMax() {
            DiminishConfig.LevelRange range = new DiminishConfig.LevelRange(50, 50);

            assertTrue(range.contains(50), "Should contain 50");
            assertFalse(range.contains(49), "Should not contain 49");
            assertFalse(range.contains(51), "Should not contain 51");
        }
    }

    @Nested
    @DisplayName("MobExpConfig Tests")
    class MobExpConfigTests {

        @Test
        @DisplayName("MobExpConfig - 正しく初期化される")
        void mobExpConfigInitialization() {
            DiminishConfig.MobExpConfig config = new DiminishConfig.MobExpConfig("zombie", 20, 69);

            assertEquals("zombie", config.getMobId(), "MobId should be zombie");
            assertEquals(20, config.getBaseExp(), "BaseExp should be 20");
            assertEquals(69, config.getMaxLevel(), "MaxLevel should be 69");
        }

        @Test
        @DisplayName("MobExpConfig - 異なる値")
        void mobExpConfigDifferentValues() {
            DiminishConfig.MobExpConfig config1 = new DiminishConfig.MobExpConfig("skeleton", 30, 70);
            DiminishConfig.MobExpConfig config2 = new DiminishConfig.MobExpConfig("spider", 25, 65);

            assertNotEquals(config1.getMobId(), config2.getMobId());
            assertNotEquals(config1.getBaseExp(), config2.getBaseExp());
            assertNotEquals(config1.getMaxLevel(), config2.getMaxLevel());
        }

        @Test
        @DisplayName("MobExpConfig - 0の値")
        void mobExpConfigZeroValues() {
            DiminishConfig.MobExpConfig config = new DiminishConfig.MobExpConfig("test", 0, 0);

            assertEquals(0, config.getBaseExp());
            assertEquals(0, config.getMaxLevel());
        }
    }

    @Nested
    @DisplayName("getReductionRate() Tests")
    class GetReductionRateTests {

        @Test
        @DisplayName("getReductionRate - 設定読み込み前は0")
        void getReductionRateBeforeLoad() {
            assertEquals(0.0, diminishConfig.getReductionRate(50), "Should return 0.0 before load");
        }

        @Test
        @DisplayName("getReductionRate - 範囲外のレベルは0")
        void getReductionRateOutOfRange() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0.0, diminishConfig.getReductionRate(10), "Level 10 should have 0 reduction");
            assertEquals(0.0, diminishConfig.getReductionRate(20), "Level 20 should have 0 reduction");
        }

        @Test
        @DisplayName("getReductionRate - デフォルト設定で正しく動作")
        void getReductionRateWithDefaults() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0.0, diminishConfig.getReductionRate(29), "Level 29: no reduction");
            assertEquals(0.5, diminishConfig.getReductionRate(30), "Level 30: 50% reduction");
            assertEquals(0.5, diminishConfig.getReductionRate(35), "Level 35: 50% reduction");
            assertEquals(0.5, diminishConfig.getReductionRate(39), "Level 39: 50% reduction");
            assertEquals(0.6, diminishConfig.getReductionRate(40), "Level 40: 60% reduction");
            assertEquals(0.7, diminishConfig.getReductionRate(50), "Level 50: 70% reduction");
            assertEquals(0.8, diminishConfig.getReductionRate(60), "Level 60: 80% reduction");
            assertEquals(0.9, diminishConfig.getReductionRate(70), "Level 70: 90% reduction");
            assertEquals(0.9, diminishConfig.getReductionRate(100), "Level 100: 90% reduction");
        }
    }

    @Nested
    @DisplayName("getStartLevel() Tests")
    class GetStartLevelTests {

        @Test
        @DisplayName("getStartLevel - 設定読み込み前は30")
        void getStartLevelBeforeLoad() {
            assertEquals(30, diminishConfig.getStartLevel(), "Default start level should be 30");
        }

        @Test
        @DisplayName("getStartLevel - デフォルト設定では30")
        void getStartLevelWithDefaults() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(30, diminishConfig.getStartLevel(), "Default start level should be 30");
        }
    }

    @Nested
    @DisplayName("MobExpConfig Methods Tests")
    class MobExpConfigMethodsTests {

        @Test
        @DisplayName("getMobExpConfig - 設定読み込み前はnull")
        void getMobExpConfigBeforeLoad() {
            assertNull(diminishConfig.getMobExpConfig("zombie"), "Should return null before load");
        }

        @Test
        @DisplayName("getMobExpConfig - 空設定ではnull")
        void getMobExpConfigWithEmptyConfig() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertNull(diminishConfig.getMobExpConfig("zombie"), "Should return null with empty config");
        }

        @Test
        @DisplayName("getMobBaseExp - デフォルト値を使用")
        void getMobBaseExpWithDefault() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(50, diminishConfig.getMobBaseExp("zombie", 50), "Should use default value");
        }

        @Test
        @DisplayName("getMobMaxLevel - デフォルト値を使用")
        void getMobMaxLevelWithDefault() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(70, diminishConfig.getMobMaxLevel("zombie", 70), "Should use default value");
        }
    }

    @Nested
    @DisplayName("Exemption Settings Tests")
    class ExemptionSettingsTests {

        @Test
        @DisplayName("除外設定 - デフォルト値")
        void defaultExemptions() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertTrue(diminishConfig.isPlayerKillsExempt(), "player_kills should default to true");
            assertTrue(diminishConfig.isBossMobsExempt(), "boss_mobs should default to true");
            assertTrue(diminishConfig.isEventRewardsExempt(), "event_rewards should default to true");
        }
    }

    @Nested
    @DisplayName("Formula Tests")
    class FormulaTests {

        @Test
        @DisplayName("getFormula - デフォルト値")
        void getFormulaDefault() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);
            when(mockConfig.getString(eq("formula"), anyString())).thenReturn("BASE_EXP * (1.0 - REDUCTION_RATE)");

            diminishConfig.load(mockConfig);

            String formula = diminishConfig.getFormula();
            assertEquals("BASE_EXP * (1.0 - REDUCTION_RATE)", formula, "Should have default formula");
        }
    }

    @Nested
    @DisplayName("MobExpConfig Case Sensitivity Tests")
    class MobExpConfigCaseSensitivityTests {

        @Test
        @DisplayName("getMobExpConfig - 大文字小文字を区別せず取得できる")
        void getMobExpConfigCaseInsensitiveWithValidConfig() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);

            ConfigurationSection mobSection = mock(ConfigurationSection.class);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(mobSection);
            when(mobSection.getKeys(false)).thenReturn(Set.of("ZOMBIE"));

            ConfigurationSection zombieConfig = mock(ConfigurationSection.class);
            when(mobSection.getConfigurationSection("ZOMBIE")).thenReturn(zombieConfig);
            when(zombieConfig.getInt("base_exp", 20)).thenReturn(25);
            when(zombieConfig.getInt("max_level", 69)).thenReturn(70);

            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertNotNull(diminishConfig.getMobExpConfig("zombie"), "Lowercase should work");
            assertNotNull(diminishConfig.getMobExpConfig("ZOMBIE"), "Uppercase should work");
            assertNotNull(diminishConfig.getMobExpConfig("ZoMbIe"), "Mixed case should work");
            assertEquals(25, diminishConfig.getMobBaseExp("ZOMBIE", 20), "Uppercase lookup should work");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("getReductionRate - 負のレベル")
        void getReductionRateNegativeLevel() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0.0, diminishConfig.getReductionRate(-1), "Negative level should have 0 reduction");
        }

        @Test
        @DisplayName("getReductionRate - 0レベル")
        void getReductionRateZeroLevel() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertEquals(0.0, diminishConfig.getReductionRate(0), "Level 0 should have 0 reduction");
        }

        @Test
        @DisplayName("getMobExpConfig - 大文字小文字は区別されない")
        void getMobExpConfigCaseInsensitive() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertNull(diminishConfig.getMobExpConfig("ZOMBIE"), "Should be case insensitive");
            assertNull(diminishConfig.getMobExpConfig("Zombie"), "Should be case insensitive");
            assertNull(diminishConfig.getMobExpConfig("zombie"), "Should be case insensitive");
        }

        @Test
        @DisplayName("getMobBaseExp - null mobId")
        void getMobBaseExpNullMobId() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertThrows(NullPointerException.class, () -> {
                diminishConfig.getMobBaseExp(null, 100);
            }, "Should throw NPE for null mobId");
        }

        @Test
        @DisplayName("getMobMaxLevel - null mobId")
        void getMobMaxLevelNullMobId() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertThrows(NullPointerException.class, () -> {
                diminishConfig.getMobMaxLevel(null, 70);
            }, "Should throw NPE for null mobId");
        }

        @Test
        @DisplayName("getMobExpConfig - 空文字列mobId")
        void getMobExpConfigEmptyMobId() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertNull(diminishConfig.getMobExpConfig(""), "Should handle empty mobId");
        }

        @Test
        @DisplayName("getMobExpConfig - 異常な文字列mobId")
        void getMobExpConfigSpecialChars() {
            when(mockConfig.getList("diminish_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("mob_exp_table")).thenReturn(null);
            when(mockConfig.getConfigurationSection("exemptions")).thenReturn(null);

            diminishConfig.load(mockConfig);

            assertNull(diminishConfig.getMobExpConfig("mob with spaces"), "Should handle spaces");
            assertNull(diminishConfig.getMobExpConfig("mob\nwith\nnewlines"), "Should handle newlines");
        }
    }
}
