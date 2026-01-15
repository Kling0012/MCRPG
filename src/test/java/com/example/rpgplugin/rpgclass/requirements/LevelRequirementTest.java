package com.example.rpgplugin.rpgclass.requirements;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * LevelRequirementのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LevelRequirement Tests")
class LevelRequirementTest {

    @Mock
    private Player mockPlayer;

    @Mock
    private ConfigurationSection mockConfig;

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("コンストラクタ - 正のレベル")
        void constructorWithPositiveLevel() {
            LevelRequirement requirement = new LevelRequirement(10);
            assertEquals(10, requirement.getRequiredLevel());
        }

        @Test
        @DisplayName("コンストラクタ - レベル1")
        void constructorWithLevel1() {
            LevelRequirement requirement = new LevelRequirement(1);
            assertEquals(1, requirement.getRequiredLevel());
        }

        @Test
        @DisplayName("コンストラクタ - 0は1に調整される")
        void constructorWithZeroAdjustsToOne() {
            LevelRequirement requirement = new LevelRequirement(0);
            assertEquals(1, requirement.getRequiredLevel());
        }

        @Test
        @DisplayName("コンストラクタ - 負の値は1に調整される")
        void constructorWithNegativeAdjustsToOne() {
            LevelRequirement requirement = new LevelRequirement(-5);
            assertEquals(1, requirement.getRequiredLevel());
        }

        @Test
        @DisplayName("コンストラクタ - 大きな値")
        void constructorWithLargeValue() {
            LevelRequirement requirement = new LevelRequirement(1000);
            assertEquals(1000, requirement.getRequiredLevel());
        }
    }

    @Nested
    @DisplayName("check() Tests")
    class CheckTests {

        @Test
        @DisplayName("check - nullプレイヤーはfalse")
        void checkWithNullPlayer() {
            LevelRequirement requirement = new LevelRequirement(10);
            assertFalse(requirement.check(null));
        }

        @Test
        @DisplayName("check - レベルが要件を満たす")
        void checkWithSufficientLevel() {
            LevelRequirement requirement = new LevelRequirement(10);
            when(mockPlayer.getLevel()).thenReturn(15);
            assertTrue(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - レベルが要件と一致")
        void checkWithExactLevel() {
            LevelRequirement requirement = new LevelRequirement(10);
            when(mockPlayer.getLevel()).thenReturn(10);
            assertTrue(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - レベルが不足")
        void checkWithInsufficientLevel() {
            LevelRequirement requirement = new LevelRequirement(10);
            when(mockPlayer.getLevel()).thenReturn(5);
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - レベル0で要件1")
        void checkWithLevel0Requirement1() {
            LevelRequirement requirement = new LevelRequirement(1);
            when(mockPlayer.getLevel()).thenReturn(0);
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - 高いレベル要件")
        void checkWithHighLevelRequirement() {
            LevelRequirement requirement = new LevelRequirement(100);
            when(mockPlayer.getLevel()).thenReturn(99);
            assertFalse(requirement.check(mockPlayer));

            when(mockPlayer.getLevel()).thenReturn(100);
            assertTrue(requirement.check(mockPlayer));
        }
    }

    @Nested
    @DisplayName("getDescription() Tests")
    class GetDescriptionTests {

        @Test
        @DisplayName("getDescription - 正しいフォーマット")
        void getDescriptionFormat() {
            LevelRequirement requirement = new LevelRequirement(10);
            assertEquals("レベル 10 以上", requirement.getDescription());
        }

        @Test
        @DisplayName("getDescription - レベル1")
        void getDescriptionWithLevel1() {
            LevelRequirement requirement = new LevelRequirement(1);
            assertEquals("レベル 1 以上", requirement.getDescription());
        }

        @Test
        @DisplayName("getDescription - 大きなレベル")
        void getDescriptionWithLargeLevel() {
            LevelRequirement requirement = new LevelRequirement(999);
            assertEquals("レベル 999 以上", requirement.getDescription());
        }
    }

    @Nested
    @DisplayName("getType() Tests")
    class GetTypeTests {

        @Test
        @DisplayName("getType - levelを返す")
        void getTypeReturnsLevel() {
            LevelRequirement requirement = new LevelRequirement(10);
            assertEquals("level", requirement.getType());
        }
    }

    @Nested
    @DisplayName("getRequiredLevel() Tests")
    class GetRequiredLevelTests {

        @Test
        @DisplayName("getRequiredLevel - 正しく返す")
        void getRequiredLevelReturnsCorrectValue() {
            LevelRequirement requirement = new LevelRequirement(25);
            assertEquals(25, requirement.getRequiredLevel());
        }
    }

    @Nested
    @DisplayName("parse() Tests")
    class ParseTests {

        @Test
        @DisplayName("parse - 通常の値")
        void parseWithNormalValue() {
            when(mockConfig.getInt("value", 1)).thenReturn(15);
            LevelRequirement requirement = LevelRequirement.parse(mockConfig);
            assertEquals(15, requirement.getRequiredLevel());
        }

        @Test
        @DisplayName("parse - デフォルト値（キーがない）")
        void parseWithDefaultValue() {
            when(mockConfig.getInt("value", 1)).thenReturn(1);
            LevelRequirement requirement = LevelRequirement.parse(mockConfig);
            assertEquals(1, requirement.getRequiredLevel());
        }

        @Test
        @DisplayName("parse - 0は1に調整される")
        void parseWithZeroAdjustsToOne() {
            when(mockConfig.getInt("value", 1)).thenReturn(0);
            LevelRequirement requirement = LevelRequirement.parse(mockConfig);
            assertEquals(1, requirement.getRequiredLevel());
        }

        @Test
        @DisplayName("parse - 負の値は1に調整される")
        void parseWithNegativeAdjustsToOne() {
            when(mockConfig.getInt("value", 1)).thenReturn(-10);
            LevelRequirement requirement = LevelRequirement.parse(mockConfig);
            assertEquals(1, requirement.getRequiredLevel());
        }

        @Test
        @DisplayName("parse - 大きな値")
        void parseWithLargeValue() {
            when(mockConfig.getInt("value", 1)).thenReturn(500);
            LevelRequirement requirement = LevelRequirement.parse(mockConfig);
            assertEquals(500, requirement.getRequiredLevel());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("複数インスタンス - 別々の要件レベル")
        void multipleInstancesHaveDifferentLevels() {
            LevelRequirement req1 = new LevelRequirement(5);
            LevelRequirement req2 = new LevelRequirement(20);
            LevelRequirement req3 = new LevelRequirement(50);

            assertEquals(5, req1.getRequiredLevel());
            assertEquals(20, req2.getRequiredLevel());
            assertEquals(50, req3.getRequiredLevel());
        }

        @Test
        @DisplayName("Integer.MAX_VALUE - 正しく処理")
        void maxIntegerValue() {
            LevelRequirement requirement = new LevelRequirement(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, requirement.getRequiredLevel());
        }
    }
}
