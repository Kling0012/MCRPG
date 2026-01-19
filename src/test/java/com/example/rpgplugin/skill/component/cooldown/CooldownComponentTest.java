package com.example.rpgplugin.skill.component.cooldown;

import com.example.rpgplugin.skill.component.ComponentType;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CooldownComponentのテストクラス
 *
 * <p>CooldownComponentは抽象クラスなので、テスト用の具象実装を使用します。</p>
 */
@DisplayName("CooldownComponent Tests")
class CooldownComponentTest {

    /**
     * テスト用の具象実装
     */
    private static class TestCooldownComponent extends CooldownComponent {
        private boolean executeChildrenCalled = false;
        private LivingEntity capturedCaster;
        private int capturedLevel;
        private List<LivingEntity> capturedTargets;

        public TestCooldownComponent() {
            super("TEST_COOLDOWN");
        }

        @Override
        public boolean executeChildren(LivingEntity caster, int level, List<LivingEntity> targets) {
            executeChildrenCalled = true;
            capturedCaster = caster;
            capturedLevel = level;
            capturedTargets = targets;
            return true;
        }

        // テスト用のヘルパーメソッド
        public boolean wasExecuteChildrenCalled() {
            return executeChildrenCalled;
        }

        public LivingEntity getCapturedCaster() {
            return capturedCaster;
        }

        public int getCapturedLevel() {
            return capturedLevel;
        }

        public List<LivingEntity> getCapturedTargets() {
            return capturedTargets;
        }

        // 設定用ヘルパーメソッド
        public void setSetting(String key, Object value) {
            this.settings.set(key, value);
        }
    }

    private TestCooldownComponent component;
    private LivingEntity mockCaster;
    private List<LivingEntity> targets;

    @BeforeEach
    void setUp() {
        component = new TestCooldownComponent();
        mockCaster = mock(LivingEntity.class);
        targets = new ArrayList<>();
    }

    @Nested
    @DisplayName("getType() Tests")
    class GetTypeTests {

        @Test
        @DisplayName("getType() は COOLDOWN を返す")
        void getTypeReturnsCooldown() {
            assertEquals(ComponentType.COOLDOWN, component.getType());
        }
    }

    @Nested
    @DisplayName("execute() Tests")
    class ExecuteTests {

        @Test
        @DisplayName("execute() は executeChildren() を呼び出す")
        void executeCallsExecuteChildren() {
            component.execute(mockCaster, 1, targets);

            assertTrue(component.wasExecuteChildrenCalled(),
                    "execute() should call executeChildren()");
        }

        @Test
        @DisplayName("execute() は正しいパラメータを渡す")
        void executePassesCorrectParameters() {
            int level = 5;
            List<LivingEntity> testTargets = new ArrayList<>();
            testTargets.add(mock(LivingEntity.class));

            component.execute(mockCaster, level, testTargets);

            assertEquals(mockCaster, component.getCapturedCaster(),
                    "Caster should be passed correctly");
            assertEquals(level, component.getCapturedLevel(),
                    "Level should be passed correctly");
            assertEquals(testTargets, component.getCapturedTargets(),
                    "Targets should be passed correctly");
        }

        @Test
        @DisplayName("execute() は子コンポーネントの結果を返す")
        void executeReturnsChildrenResult() {
            boolean result = component.execute(mockCaster, 1, targets);

            assertTrue(result, "Should return true from executeChildren");
        }
    }

    @Nested
    @DisplayName("calculateCooldown() Tests - Base Value")
    class CalculateCooldownBaseTests {

        @Test
        @DisplayName("base設定のみの場合 - 正しい値を返す")
        void baseOnly() {
            component.setSetting("base", 10.0);

            double cooldown = component.calculateCooldown(1);

            assertEquals(10.0, cooldown, 0.001,
                    "Should return base value when only base is set");
        }

        @Test
        @DisplayName("base未設定の場合 - デフォルト値0を使用")
        void baseDefaultsToZero() {
            double cooldown = component.calculateCooldown(1);

            assertEquals(0.0, cooldown, 0.001,
                    "Should default to 0 when base is not set");
        }

        @Test
        @DisplayName("baseが0の場合 - 0を返す")
        void baseZero() {
            component.setSetting("base", 0.0);

            double cooldown = component.calculateCooldown(1);

            assertEquals(0.0, cooldown, 0.001,
                    "Should return 0 when base is 0");
        }
    }

    @Nested
    @DisplayName("calculateCooldown() Tests - Per Level")
    class CalculateCooldownPerLevelTests {

        @Test
        @DisplayName("base + per_level - レベル1でbaseのみ")
        void perLevelAtLevel1() {
            component.setSetting("base", 10.0);
            component.setSetting("per_level", 2.0);

            double cooldown = component.calculateCooldown(1);

            assertEquals(10.0, cooldown, 0.001,
                    "At level 1, only base should apply (no per_level bonus)");
        }

        @Test
        @DisplayName("base + per_level - レベル2でbase + per_level")
        void perLevelAtLevel2() {
            component.setSetting("base", 10.0);
            component.setSetting("per_level", 2.0);

            double cooldown = component.calculateCooldown(2);

            assertEquals(12.0, cooldown, 0.001,
                    "At level 2, should be base + per_level");
        }

        @Test
        @DisplayName("base + per_level - レベル5でbase + 4*per_level")
        void perLevelAtLevel5() {
            component.setSetting("base", 10.0);
            component.setSetting("per_level", 2.0);

            double cooldown = component.calculateCooldown(5);

            assertEquals(18.0, cooldown, 0.001,
                    "At level 5, should be base + 4 * per_level");
        }

        @Test
        @DisplayName("per_levelが負の値 - レベル上昇で減少")
        void perLevelNegative() {
            component.setSetting("base", 20.0);
            component.setSetting("per_level", -1.0);

            double cooldown = component.calculateCooldown(5);

            assertEquals(16.0, cooldown, 0.001,
                    "Negative per_level should decrease cooldown at higher levels");
        }

        @Test
        @DisplayName("per_level未設定の場合 - 0として扱う")
        void perLevelDefaultsToZero() {
            component.setSetting("base", 10.0);

            double cooldown = component.calculateCooldown(10);

            assertEquals(10.0, cooldown, 0.001,
                    "Unset per_level should default to 0");
        }
    }

    @Nested
    @DisplayName("calculateCooldown() Tests - Min Constraint")
    class CalculateCooldownMinTests {

        @Test
        @DisplayName("min制約 - 計算結果がminより小さい場合minを返す")
        void minConstraintApplied() {
            component.setSetting("base", 5.0);
            component.setSetting("min", 10.0);

            double cooldown = component.calculateCooldown(1);

            assertEquals(10.0, cooldown, 0.001,
                    "Should apply min when calculated value is smaller");
        }

        @Test
        @DisplayName("min制約 - 計算結果がminより大きい場合計算結果を返す")
        void minConstraintNotApplied() {
            component.setSetting("base", 15.0);
            component.setSetting("min", 10.0);

            double cooldown = component.calculateCooldown(1);

            assertEquals(15.0, cooldown, 0.001,
                    "Should not apply min when calculated value is larger");
        }

        @Test
        @DisplayName("min制約 - 計算結果とminが等しい場合その値")
        void minConstraintEqual() {
            component.setSetting("base", 10.0);
            component.setSetting("min", 10.0);

            double cooldown = component.calculateCooldown(1);

            assertEquals(10.0, cooldown, 0.001,
                    "Should return the value when equal to min");
        }

        @Test
        @DisplayName("min未設定の場合 - デフォルト0を使用")
        void minDefaultsToZero() {
            component.setSetting("base", -5.0);

            double cooldown = component.calculateCooldown(1);

            assertEquals(0.0, cooldown, 0.001,
                    "Unset min should default to 0");
        }

        @Test
        @DisplayName("minが負の値 - 負の値も許容")
        void minNegative() {
            component.setSetting("base", 5.0);
            component.setSetting("min", -10.0);

            double cooldown = component.calculateCooldown(1);

            assertEquals(5.0, cooldown, 0.001,
                    "Should allow negative min values");
        }

        @Test
        @DisplayName("min + per_level - レベル上昇でmin未満になる場合")
        void minWithPerLevelDecrease() {
            component.setSetting("base", 20.0);
            component.setSetting("per_level", -5.0);
            component.setSetting("min", 10.0);

            double cooldown = component.calculateCooldown(5);

            assertEquals(10.0, cooldown, 0.001,
                    "Should apply min when per_level decreases value below min");
        }
    }

    @Nested
    @DisplayName("calculateCooldown() Tests - Max Constraint")
    class CalculateCooldownMaxTests {

        @Test
        @DisplayName("max制約 - 計算結果がmaxより大きい場合maxを返す")
        void maxConstraintApplied() {
            component.setSetting("base", 20.0);
            component.setSetting("max", 15.0);

            double cooldown = component.calculateCooldown(1);

            assertEquals(15.0, cooldown, 0.001,
                    "Should apply max when calculated value is larger");
        }

        @Test
        @DisplayName("max制約 - 計算結果がmaxより小さい場合計算結果を返す")
        void maxConstraintNotApplied() {
            component.setSetting("base", 10.0);
            component.setSetting("max", 15.0);

            double cooldown = component.calculateCooldown(1);

            assertEquals(10.0, cooldown, 0.001,
                    "Should not apply max when calculated value is smaller");
        }

        @Test
        @DisplayName("max制約 - 計算結果とmaxが等しい場合その値")
        void maxConstraintEqual() {
            component.setSetting("base", 15.0);
            component.setSetting("max", 15.0);

            double cooldown = component.calculateCooldown(1);

            assertEquals(15.0, cooldown, 0.001,
                    "Should return the value when equal to max");
        }

        @Test
        @DisplayName("max未設定の場合 - デフォルトMAX_VALUEを使用")
        void maxDefaultsToMaxValue() {
            component.setSetting("base", 1000000.0);

            double cooldown = component.calculateCooldown(1);

            assertEquals(1000000.0, cooldown, 0.001,
                    "Unset max should default to MAX_VALUE");
        }
    }

    @Nested
    @DisplayName("calculateCooldown() Tests - Combined Constraints")
    class CalculateCooldownCombinedTests {

        @Test
        @DisplayName("minとmax両方設定 - 範囲内の値はそのまま")
        void bothConstraintsInRange() {
            component.setSetting("base", 12.0);
            component.setSetting("min", 10.0);
            component.setSetting("max", 15.0);

            double cooldown = component.calculateCooldown(1);

            assertEquals(12.0, cooldown, 0.001,
                    "Should return calculated value when within range");
        }

        @Test
        @DisplayName("minとmax両方設定 - 範囲外の小さい値はmin")
        void bothConstraintsBelowMin() {
            component.setSetting("base", 5.0);
            component.setSetting("min", 10.0);
            component.setSetting("max", 15.0);

            double cooldown = component.calculateCooldown(1);

            assertEquals(10.0, cooldown, 0.001,
                    "Should apply min when below range");
        }

        @Test
        @DisplayName("minとmax両方設定 - 範囲外の大きい値はmax")
        void bothConstraintsAboveMax() {
            component.setSetting("base", 20.0);
            component.setSetting("min", 10.0);
            component.setSetting("max", 15.0);

            double cooldown = component.calculateCooldown(1);

            assertEquals(15.0, cooldown, 0.001,
                    "Should apply max when above range");
        }

        @Test
        @DisplayName("全パラメータ設定 - 複雑な計算")
        void allParametersComplexCalculation() {
            component.setSetting("base", 10.0);
            component.setSetting("per_level", 3.0);
            component.setSetting("min", 12.0);
            component.setSetting("max", 20.0);

            // Level 1: 10 + 0 = 10 -> min(12) = 12
            assertEquals(12.0, component.calculateCooldown(1), 0.001);

            // Level 2: 10 + 3 = 13 (in range)
            assertEquals(13.0, component.calculateCooldown(2), 0.001);

            // Level 3: 10 + 6 = 16 (in range)
            assertEquals(16.0, component.calculateCooldown(3), 0.001);

            // Level 5: 10 + 12 = 22 -> max(20) = 20
            assertEquals(20.0, component.calculateCooldown(5), 0.001);
        }

        @Test
        @DisplayName("小数値の計算 - 正確な精度")
        void fractionalValues() {
            component.setSetting("base", 5.5);
            component.setSetting("per_level", 1.25);

            double cooldown = component.calculateCooldown(3);

            assertEquals(5.5 + 1.25 * 2, cooldown, 0.0001,
                    "Should handle fractional values correctly");
        }
    }

    @Nested
    @DisplayName("getCooldownMillis() Tests")
    class GetCooldownMillisTests {

        @Test
        @DisplayName("秒をミリ秒に変換")
        void convertsSecondsToMillis() {
            component.setSetting("base", 5.5);

            long millis = component.getCooldownMillis(1);

            assertEquals(5500L, millis,
                    "Should convert seconds to milliseconds");
        }

        @Test
        @DisplayName("整数の秒 - 正確なミリ秒")
        void integerSeconds() {
            component.setSetting("base", 10.0);

            long millis = component.getCooldownMillis(1);

            assertEquals(10000L, millis,
                    "Should correctly convert integer seconds");
        }

        @Test
        @DisplayName("0秒 - 0ミリ秒")
        void zeroSeconds() {
            long millis = component.getCooldownMillis(1);

            assertEquals(0L, millis,
                    "Zero seconds should be 0 milliseconds");
        }

        @Test
        @DisplayName("小数の丸め - 切り捨て")
        void fractionalRounding() {
            component.setSetting("base", 5.999);

            long millis = component.getCooldownMillis(1);

            assertEquals(5999L, millis,
                    "Should truncate fractional milliseconds");
        }
    }

    @Nested
    @DisplayName("getCooldownSeconds() Tests")
    class GetCooldownSecondsTests {

        @Test
        @DisplayName("calculateCooldownの結果を返す")
        void returnsCalculateCooldownResult() {
            component.setSetting("base", 10.0);

            double seconds = component.getCooldownSeconds(1);

            assertEquals(10.0, seconds, 0.001,
                    "Should return the result of calculateCooldown");
        }

        @Test
        @DisplayName("per_levelを含む計算")
        void withPerLevel() {
            component.setSetting("base", 10.0);
            component.setSetting("per_level", 2.0);

            double seconds = component.getCooldownSeconds(3);

            assertEquals(14.0, seconds, 0.001,
                    "Should include per_level in calculation");
        }

        @Test
        @DisplayName("min制約を適用")
        void appliesMinConstraint() {
            component.setSetting("base", 5.0);
            component.setSetting("min", 8.0);

            double seconds = component.getCooldownSeconds(1);

            assertEquals(8.0, seconds, 0.001,
                    "Should apply min constraint");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("レベル0 - 負のper_levelで計算")
        void levelZero() {
            component.setSetting("base", 10.0);
            component.setSetting("per_level", 2.0);

            double cooldown = component.calculateCooldown(0);

            // Level 0: base + per_level * (0 - 1) = 10 - 2 = 8
            assertEquals(8.0, cooldown, 0.001,
                    "Level 0 should reduce cooldown by per_level");
        }

        @Test
        @DisplayName("負のレベル - 計算が続く")
        void negativeLevel() {
            component.setSetting("base", 10.0);
            component.setSetting("per_level", 2.0);

            double cooldown = component.calculateCooldown(-5);

            // Level -5: base + per_level * (-5 - 1) = 10 - 12 = -2 -> min(0) = 0
            assertEquals(0.0, cooldown, 0.001,
                    "Negative level should still calculate");
        }

        @Test
        @DisplayName("非常に大きなレベル - オーバーフローなし")
        void veryLargeLevel() {
            component.setSetting("base", 10.0);
            component.setSetting("per_level", 0.1);
            component.setSetting("max", 100.0);

            double cooldown = component.calculateCooldown(1000);

            assertEquals(100.0, cooldown, 0.001,
                    "Should handle large levels without overflow");
        }

        @Test
        @DisplayName("非常に小さな値 - 精度維持")
        void verySmallValues() {
            component.setSetting("base", 0.001);
            component.setSetting("per_level", 0.0001);

            double cooldown = component.calculateCooldown(2);

            assertEquals(0.0011, cooldown, 0.00001,
                    "Should maintain precision for very small values");
        }
    }
}
