package com.example.rpgplugin.rpgclass.growth;

import com.example.rpgplugin.stats.Stat;
import org.bukkit.configuration.ConfigurationSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * StatGrowthのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("StatGrowth Tests")
class StatGrowthTest {

    @Mock
    private ConfigurationSection mockSection;

    @Mock
    private ConfigurationSection mockAutoSection;

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Builder - デフォルト値")
        void builderWithDefaults() {
            StatGrowth growth = new StatGrowth.Builder()
                    .build();

            assertEquals(0, growth.getManualPoints());
            assertEquals(0, growth.getAutoGrowth(Stat.STRENGTH));
            assertTrue(growth.getAllAutoGrowth().isEmpty());
        }

        @Test
        @DisplayName("Builder - 自動成長を設定")
        void builderWithAutoGrowth() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setAutoGrowth(Stat.STRENGTH, 5)
                    .setAutoGrowth(Stat.VITALITY, 3)
                    .build();

            assertEquals(5, growth.getAutoGrowth(Stat.STRENGTH));
            assertEquals(3, growth.getAutoGrowth(Stat.VITALITY));
            assertEquals(0, growth.getAutoGrowth(Stat.INTELLIGENCE));
        }

        @Test
        @DisplayName("Builder - 手動ポイントを設定")
        void builderWithManualPoints() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setManualPoints(10)
                    .build();

            assertEquals(10, growth.getManualPoints());
        }

        @Test
        @DisplayName("Builder - すべての設定")
        void builderWithAllSettings() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setAutoGrowth(Stat.STRENGTH, 5)
                    .setAutoGrowth(Stat.VITALITY, 3)
                    .setAutoGrowth(Stat.INTELLIGENCE, 2)
                    .setManualPoints(8)
                    .build();

            assertEquals(5, growth.getAutoGrowth(Stat.STRENGTH));
            assertEquals(3, growth.getAutoGrowth(Stat.VITALITY));
            assertEquals(2, growth.getAutoGrowth(Stat.INTELLIGENCE));
            assertEquals(8, growth.getManualPoints());
        }

        @Test
        @DisplayName("Builder - 負の値は無視される（自動成長）")
        void builderWithNegativeAutoGrowth() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setAutoGrowth(Stat.STRENGTH, 5)
                    .setAutoGrowth(Stat.VITALITY, -3)
                    .build();

            assertEquals(5, growth.getAutoGrowth(Stat.STRENGTH));
            assertEquals(0, growth.getAutoGrowth(Stat.VITALITY));
        }

        @Test
        @DisplayName("Builder - 0は無視される（自動成長）")
        void builderWithZeroAutoGrowth() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setAutoGrowth(Stat.STRENGTH, 5)
                    .setAutoGrowth(Stat.VITALITY, 0)
                    .build();

            assertEquals(5, growth.getAutoGrowth(Stat.STRENGTH));
            assertEquals(0, growth.getAutoGrowth(Stat.VITALITY));
        }

        @Test
        @DisplayName("Builder - 負の値は0に調整される（手動ポイント）")
        void builderWithNegativeManualPoints() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setManualPoints(-5)
                    .build();

            assertEquals(0, growth.getManualPoints());
        }

        @Test
        @DisplayName("Builder - 0の手動ポイント")
        void builderWithZeroManualPoints() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setManualPoints(0)
                    .build();

            assertEquals(0, growth.getManualPoints());
        }

        @Test
        @DisplayName("Builder - メソッドチェーン")
        void builderMethodChaining() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setAutoGrowth(Stat.STRENGTH, 5)
                    .setManualPoints(10)
                    .setAutoGrowth(Stat.VITALITY, 3)
                    .build();

            assertEquals(5, growth.getAutoGrowth(Stat.STRENGTH));
            assertEquals(3, growth.getAutoGrowth(Stat.VITALITY));
            assertEquals(10, growth.getManualPoints());
        }

        @Test
        @DisplayName("Builder - 複数回同じステータスを設定")
        void builderSetSameStatMultipleTimes() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setAutoGrowth(Stat.STRENGTH, 5)
                    .setAutoGrowth(Stat.STRENGTH, 10)
                    .build();

            assertEquals(10, growth.getAutoGrowth(Stat.STRENGTH));
        }
    }

    @Nested
    @DisplayName("getAutoGrowth() Tests")
    class GetAutoGrowthTests {

        @Test
        @DisplayName("getAutoGrowth - 設定済みのステータス")
        void getAutoGrowthForSetStat() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setAutoGrowth(Stat.STRENGTH, 5)
                    .build();

            assertEquals(5, growth.getAutoGrowth(Stat.STRENGTH));
        }

        @Test
        @DisplayName("getAutoGrowth - 未設定のステータスは0")
        void getAutoGrowthForUnsetStat() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setAutoGrowth(Stat.STRENGTH, 5)
                    .build();

            assertEquals(0, growth.getAutoGrowth(Stat.VITALITY));
            assertEquals(0, growth.getAutoGrowth(Stat.INTELLIGENCE));
            assertEquals(0, growth.getAutoGrowth(Stat.SPIRIT));
            assertEquals(0, growth.getAutoGrowth(Stat.DEXTERITY));
        }

        @Test
        @DisplayName("getAutoGrowth - 全ステータスタイプ")
        void getAutoGrowthForAllStatTypes() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setAutoGrowth(Stat.STRENGTH, 1)
                    .setAutoGrowth(Stat.VITALITY, 2)
                    .setAutoGrowth(Stat.INTELLIGENCE, 3)
                    .setAutoGrowth(Stat.SPIRIT, 4)
                    .setAutoGrowth(Stat.DEXTERITY, 5)
                    .build();

            assertEquals(1, growth.getAutoGrowth(Stat.STRENGTH));
            assertEquals(2, growth.getAutoGrowth(Stat.VITALITY));
            assertEquals(3, growth.getAutoGrowth(Stat.INTELLIGENCE));
            assertEquals(4, growth.getAutoGrowth(Stat.SPIRIT));
            assertEquals(5, growth.getAutoGrowth(Stat.DEXTERITY));
        }
    }

    @Nested
    @DisplayName("getAllAutoGrowth() Tests")
    class GetAllAutoGrowthTests {

        @Test
        @DisplayName("getAllAutoGrowth - 空のマップ")
        void getAllAutoGrowthWhenEmpty() {
            StatGrowth growth = new StatGrowth.Builder().build();

            assertTrue(growth.getAllAutoGrowth().isEmpty());
        }

        @Test
        @DisplayName("getAllAutoGrowth - 設定済みのステータスを含む")
        void getAllAutoGrowthWithSetStats() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setAutoGrowth(Stat.STRENGTH, 5)
                    .setAutoGrowth(Stat.VITALITY, 3)
                    .build();

            Map<Stat, Integer> result = growth.getAllAutoGrowth();

            assertEquals(2, result.size());
            assertEquals(5, result.get(Stat.STRENGTH));
            assertEquals(3, result.get(Stat.VITALITY));
        }

        @Test
        @DisplayName("getAllAutoGrowth - 返されたマップの変更は元に影響しない")
        void getAllAutoGrowthReturnsCopy() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setAutoGrowth(Stat.STRENGTH, 5)
                    .build();

            Map<Stat, Integer> result = growth.getAllAutoGrowth();
            result.put(Stat.VITALITY, 10);

            assertEquals(0, growth.getAutoGrowth(Stat.VITALITY));
            assertEquals(1, growth.getAllAutoGrowth().size());
        }

        @Test
        @DisplayName("getAllAutoGrowth - 全ステータスタイプ")
        void getAllAutoGrowthWithAllStatTypes() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setAutoGrowth(Stat.STRENGTH, 1)
                    .setAutoGrowth(Stat.VITALITY, 2)
                    .setAutoGrowth(Stat.INTELLIGENCE, 3)
                    .setAutoGrowth(Stat.SPIRIT, 4)
                    .setAutoGrowth(Stat.DEXTERITY, 5)
                    .build();

            Map<Stat, Integer> result = growth.getAllAutoGrowth();

            assertEquals(5, result.size());
            assertEquals(1, result.get(Stat.STRENGTH));
            assertEquals(2, result.get(Stat.VITALITY));
            assertEquals(3, result.get(Stat.INTELLIGENCE));
            assertEquals(4, result.get(Stat.SPIRIT));
            assertEquals(5, result.get(Stat.DEXTERITY));
        }
    }

    @Nested
    @DisplayName("getManualPoints() Tests")
    class GetManualPointsTests {

        @Test
        @DisplayName("getManualPoints - デフォルトは0")
        void getManualPointsWithDefault() {
            StatGrowth growth = new StatGrowth.Builder().build();

            assertEquals(0, growth.getManualPoints());
        }

        @Test
        @DisplayName("getManualPoints - 設定済みの値")
        void getManualPointsWithSetValue() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setManualPoints(15)
                    .build();

            assertEquals(15, growth.getManualPoints());
        }

        @Test
        @DisplayName("getManualPoints - 負の値は0に調整")
        void getManualPointsWithNegativeValue() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setManualPoints(-10)
                    .build();

            assertEquals(0, growth.getManualPoints());
        }

        @Test
        @DisplayName("getManualPoints - 大きな値")
        void getManualPointsWithLargeValue() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setManualPoints(1000)
                    .build();

            assertEquals(1000, growth.getManualPoints());
        }
    }

    @Nested
    @DisplayName("parse() Tests")
    class ParseTests {

        @BeforeEach
        void setUp() {
            when(mockSection.getConfigurationSection("auto")).thenReturn(mockAutoSection);
        }

        @Test
        @DisplayName("parse - nullセクションで例外")
        void parseWithNullSection() {
            assertThrows(IllegalArgumentException.class, () -> {
                StatGrowth.parse(null);
            });
        }

        @Test
        @DisplayName("parse - 最小限の設定")
        void parseWithMinimalConfig() {
            when(mockSection.getConfigurationSection("auto")).thenReturn(null);
            when(mockSection.getInt("manual_points", 0)).thenReturn(0);

            StatGrowth growth = StatGrowth.parse(mockSection);

            assertEquals(0, growth.getManualPoints());
            assertTrue(growth.getAllAutoGrowth().isEmpty());
        }

        @Test
        @DisplayName("parse - 自動成長のみ")
        void parseWithAutoGrowthOnly() {
            when(mockAutoSection.getKeys(false)).thenReturn(java.util.Set.of("STRENGTH", "VITALITY"));
            when(mockAutoSection.getInt("STRENGTH", 0)).thenReturn(5);
            when(mockAutoSection.getInt("VITALITY", 0)).thenReturn(3);
            when(mockSection.getInt("manual_points", 0)).thenReturn(0);

            StatGrowth growth = StatGrowth.parse(mockSection);

            assertEquals(5, growth.getAutoGrowth(Stat.STRENGTH));
            assertEquals(3, growth.getAutoGrowth(Stat.VITALITY));
            assertEquals(0, growth.getManualPoints());
        }

        @Test
        @DisplayName("parse - 手動ポイントのみ")
        void parseWithManualPointsOnly() {
            when(mockSection.getConfigurationSection("auto")).thenReturn(null);
            when(mockSection.getInt("manual_points", 0)).thenReturn(10);

            StatGrowth growth = StatGrowth.parse(mockSection);

            assertEquals(10, growth.getManualPoints());
            assertTrue(growth.getAllAutoGrowth().isEmpty());
        }

        @Test
        @DisplayName("parse - 完全な設定")
        void parseWithFullConfig() {
            when(mockAutoSection.getKeys(false)).thenReturn(java.util.Set.of("STRENGTH", "INTELLIGENCE"));
            when(mockAutoSection.getInt("STRENGTH", 0)).thenReturn(5);
            when(mockAutoSection.getInt("INTELLIGENCE", 0)).thenReturn(3);
            when(mockSection.getInt("manual_points", 0)).thenReturn(8);

            StatGrowth growth = StatGrowth.parse(mockSection);

            assertEquals(5, growth.getAutoGrowth(Stat.STRENGTH));
            assertEquals(3, growth.getAutoGrowth(Stat.INTELLIGENCE));
            assertEquals(8, growth.getManualPoints());
        }

        @Test
        @DisplayName("parse - 小文字のステータス名")
        void parseWithLowerCaseStatName() {
            when(mockAutoSection.getKeys(false)).thenReturn(java.util.Set.of("strength"));
            when(mockAutoSection.getInt("strength", 0)).thenReturn(7);
            when(mockSection.getInt("manual_points", 0)).thenReturn(0);

            StatGrowth growth = StatGrowth.parse(mockSection);

            assertEquals(7, growth.getAutoGrowth(Stat.STRENGTH));
        }

        @Test
        @DisplayName("parse - 不明なステータス名はスキップ")
        void parseWithInvalidStatName() {
            when(mockAutoSection.getKeys(false)).thenReturn(java.util.Set.of("STRENGTH", "INVALID_STAT"));
            when(mockAutoSection.getInt("STRENGTH", 0)).thenReturn(5);
            when(mockAutoSection.getInt("INVALID_STAT", 0)).thenReturn(10);
            when(mockSection.getInt("manual_points", 0)).thenReturn(0);

            StatGrowth growth = StatGrowth.parse(mockSection);

            assertEquals(5, growth.getAutoGrowth(Stat.STRENGTH));
            assertEquals(0, growth.getAutoGrowth(Stat.VITALITY)); // INVALID_STAT ignored
        }

        @Test
        @DisplayName("parse - 0と負の値はスキップ")
        void parseWithZeroAndNegativeValues() {
            when(mockAutoSection.getKeys(false)).thenReturn(java.util.Set.of("STRENGTH", "VITALITY", "INTELLIGENCE"));
            when(mockAutoSection.getInt("STRENGTH", 0)).thenReturn(5);
            when(mockAutoSection.getInt("VITALITY", 0)).thenReturn(0);
            when(mockAutoSection.getInt("INTELLIGENCE", 0)).thenReturn(-3);
            when(mockSection.getInt("manual_points", 0)).thenReturn(0);

            StatGrowth growth = StatGrowth.parse(mockSection);

            assertEquals(5, growth.getAutoGrowth(Stat.STRENGTH));
            assertEquals(0, growth.getAutoGrowth(Stat.VITALITY));
            assertEquals(0, growth.getAutoGrowth(Stat.INTELLIGENCE));
        }

        @Test
        @DisplayName("parse - 全ステータスタイプ")
        void parseWithAllStatTypes() {
            when(mockAutoSection.getKeys(false)).thenReturn(java.util.Set.of(
                "STRENGTH", "VITALITY", "INTELLIGENCE", "SPIRIT", "DEXTERITY"));
            when(mockAutoSection.getInt("STRENGTH", 0)).thenReturn(1);
            when(mockAutoSection.getInt("VITALITY", 0)).thenReturn(2);
            when(mockAutoSection.getInt("INTELLIGENCE", 0)).thenReturn(3);
            when(mockAutoSection.getInt("SPIRIT", 0)).thenReturn(4);
            when(mockAutoSection.getInt("DEXTERITY", 0)).thenReturn(5);
            when(mockSection.getInt("manual_points", 0)).thenReturn(10);

            StatGrowth growth = StatGrowth.parse(mockSection);

            assertEquals(1, growth.getAutoGrowth(Stat.STRENGTH));
            assertEquals(2, growth.getAutoGrowth(Stat.VITALITY));
            assertEquals(3, growth.getAutoGrowth(Stat.INTELLIGENCE));
            assertEquals(4, growth.getAutoGrowth(Stat.SPIRIT));
            assertEquals(5, growth.getAutoGrowth(Stat.DEXTERITY));
            assertEquals(10, growth.getManualPoints());
        }

        @Test
        @DisplayName("parse - 負の手動ポイントは0に調整")
        void parseWithNegativeManualPoints() {
            when(mockSection.getConfigurationSection("auto")).thenReturn(null);
            when(mockSection.getInt("manual_points", 0)).thenReturn(-5);

            StatGrowth growth = StatGrowth.parse(mockSection);

            assertEquals(0, growth.getManualPoints());
        }

        @Test
        @DisplayName("parse - 空の自動セクション")
        void parseWithEmptyAutoSection() {
            when(mockAutoSection.getKeys(false)).thenReturn(java.util.Collections.emptySet());
            when(mockSection.getInt("manual_points", 0)).thenReturn(5);

            StatGrowth growth = StatGrowth.parse(mockSection);

            assertTrue(growth.getAllAutoGrowth().isEmpty());
            assertEquals(5, growth.getManualPoints());
        }
    }

    @Nested
    @DisplayName("toString() Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString - 空の設定")
        void toStringWithEmptySettings() {
            StatGrowth growth = new StatGrowth.Builder().build();

            String result = growth.toString();

            assertTrue(result.contains("StatGrowth{"));
            assertTrue(result.contains("autoGrowth={}"));
            assertTrue(result.contains("manualPoints=0"));
        }

        @Test
        @DisplayName("toString - 設定あり")
        void toStringWithSettings() {
            StatGrowth growth = new StatGrowth.Builder()
                    .setAutoGrowth(Stat.STRENGTH, 5)
                    .setManualPoints(10)
                    .build();

            String result = growth.toString();

            assertTrue(result.contains("StatGrowth{"));
            assertTrue(result.contains("autoGrowth="));
            assertTrue(result.contains("manualPoints=10"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("複数インスタンス - 別々の設定")
        void multipleInstancesHaveDifferentSettings() {
            StatGrowth growth1 = new StatGrowth.Builder()
                    .setAutoGrowth(Stat.STRENGTH, 5)
                    .setManualPoints(10)
                    .build();

            StatGrowth growth2 = new StatGrowth.Builder()
                    .setAutoGrowth(Stat.VITALITY, 3)
                    .setManualPoints(15)
                    .build();

            assertEquals(5, growth1.getAutoGrowth(Stat.STRENGTH));
            assertEquals(0, growth1.getAutoGrowth(Stat.VITALITY));
            assertEquals(10, growth1.getManualPoints());

            assertEquals(0, growth2.getAutoGrowth(Stat.STRENGTH));
            assertEquals(3, growth2.getAutoGrowth(Stat.VITALITY));
            assertEquals(15, growth2.getManualPoints());
        }

        @Test
        @DisplayName("Builder - 同じビルダーで複数のインスタンスを作成")
        void sameBuilderMultipleInstances() {
            StatGrowth.Builder builder = new StatGrowth.Builder()
                    .setAutoGrowth(Stat.STRENGTH, 5)
                    .setManualPoints(10);

            StatGrowth growth1 = builder.build();
            builder.setAutoGrowth(Stat.VITALITY, 3);
            StatGrowth growth2 = builder.build();

            assertEquals(5, growth1.getAutoGrowth(Stat.STRENGTH));
            assertEquals(0, growth1.getAutoGrowth(Stat.VITALITY));

            assertEquals(5, growth2.getAutoGrowth(Stat.STRENGTH));
            assertEquals(3, growth2.getAutoGrowth(Stat.VITALITY));
        }
    }
}
