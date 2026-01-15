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
 * BasicCooldownComponentのテストクラス
 */
@DisplayName("BasicCooldownComponent Tests")
class BasicCooldownComponentTest {

    private BasicCooldownComponent component;
    private LivingEntity mockCaster;
    private List<LivingEntity> targets;

    /**
     * テスト用のサブクラス - executeChildrenをオーバーライドして挙動を確認
     */
    private static class TestableBasicCooldownComponent extends BasicCooldownComponent {
        private boolean executeChildrenCalled = false;
        private LivingEntity capturedCaster;
        private int capturedLevel;
        private List<LivingEntity> capturedTargets;

        @Override
        public boolean executeChildren(LivingEntity caster, int level, List<LivingEntity> targets) {
            executeChildrenCalled = true;
            capturedCaster = caster;
            capturedLevel = level;
            capturedTargets = targets;
            return true;
        }

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

    @BeforeEach
    void setUp() {
        component = new TestableBasicCooldownComponent();
        mockCaster = mock(LivingEntity.class);
        targets = new ArrayList<>();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("コンストラクタ - 正しく初期化される")
        void constructorInitializesCorrectly() {
            assertNotNull(component, "Component should be initialized");
        }

        @Test
        @DisplayName("getType() は COOLDOWN を返す")
        void getTypeReturnsCooldown() {
            assertEquals(ComponentType.COOLDOWN, component.getType(),
                    "getType() should return COOLDOWN");
        }
    }

    @Nested
    @DisplayName("execute() Tests")
    class ExecuteTests {

        @Test
        @DisplayName("execute() は executeChildren() を呼び出す")
        void executeCallsExecuteChildren() {
            component.execute(mockCaster, 1, targets);

            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            assertTrue(testable.wasExecuteChildrenCalled(),
                    "execute() should call executeChildren()");
        }

        @Test
        @DisplayName("execute() は正しいパラメータを渡す")
        void executePassesCorrectParameters() {
            int level = 5;
            List<LivingEntity> testTargets = new ArrayList<>();
            testTargets.add(mock(LivingEntity.class));
            testTargets.add(mock(LivingEntity.class));

            component.execute(mockCaster, level, testTargets);

            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            assertEquals(mockCaster, testable.getCapturedCaster(),
                    "Caster should be passed correctly");
            assertEquals(level, testable.getCapturedLevel(),
                    "Level should be passed correctly");
            assertEquals(testTargets, testable.getCapturedTargets(),
                    "Targets should be passed correctly");
        }

        @Test
        @DisplayName("execute() は子コンポーネントの結果を返す")
        void executeReturnsChildrenResult() {
            boolean result = component.execute(mockCaster, 1, targets);

            assertTrue(result, "Should return true from executeChildren");
        }

        @Test
        @DisplayName("execute() - 空のターゲットリスト")
        void executeWithEmptyTargets() {
            List<LivingEntity> emptyTargets = new ArrayList<>();

            boolean result = component.execute(mockCaster, 1, emptyTargets);

            assertTrue(result, "Should handle empty targets list");
        }

        @Test
        @DisplayName("execute() - 空リストとnullリストの区別")
        void executeWithDifferentTargets() {
            // 空リストとnullを渡した場合の挙動を確認
            List<LivingEntity> emptyTargets = new ArrayList<>();

            // 空リストは正常に処理される
            boolean result1 = component.execute(mockCaster, 1, emptyTargets);
            assertTrue(result1, "Should handle empty targets");

            // nullは実装依存だが、呼び出し側はnullを渡さないべき
            // ここではnullを渡してもクラッシュしないことを確認
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.execute(mockCaster, 1, null);
            // nullを渡した場合の結果は実装に依存するが、
            // 少なくともクラッシュしないことを確認
            assertTrue(testable.wasExecuteChildrenCalled(),
                    "Should still call executeChildren even with null targets");
        }
    }

    @Nested
    @DisplayName("Cooldown Calculation Tests")
    class CooldownCalculationTests {

        @Test
        @DisplayName("デフォルト設定 - クールダウンは0")
        void defaultSettingsCooldownZero() {
            assertEquals(0.0, component.getCooldownSeconds(1), 0.001,
                    "Default cooldown should be 0");
            assertEquals(0L, component.getCooldownMillis(1),
                    "Default cooldown millis should be 0");
        }

        @Test
        @DisplayName("base設定 - 正しいクールダウン")
        void baseCooldown() {
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.setSetting("base", 10.0);

            assertEquals(10.0, component.getCooldownSeconds(1), 0.001,
                    "Cooldown with base=10 should be 10 seconds");
            assertEquals(10000L, component.getCooldownMillis(1),
                    "Cooldown millis should be 10000");
        }

        @Test
        @DisplayName("base + per_level設定 - レベルで変化")
        void perLevelCooldown() {
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.setSetting("base", 10.0);
            testable.setSetting("per_level", 2.0);

            assertEquals(10.0, component.getCooldownSeconds(1), 0.001,
                    "Level 1: base only");
            assertEquals(12.0, component.getCooldownSeconds(2), 0.001,
                    "Level 2: base + per_level");
            assertEquals(16.0, component.getCooldownSeconds(4), 0.001,
                    "Level 4: base + 3 * per_level");
        }

        @Test
        @DisplayName("min制約 - 最小値が適用される")
        void minConstraint() {
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.setSetting("base", 5.0);
            testable.setSetting("min", 8.0);

            assertEquals(8.0, component.getCooldownSeconds(1), 0.001,
                    "Min constraint should be applied");
        }

        @Test
        @DisplayName("max制約 - 最大値が適用される")
        void maxConstraint() {
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.setSetting("base", 20.0);
            testable.setSetting("max", 15.0);

            assertEquals(15.0, component.getCooldownSeconds(1), 0.001,
                    "Max constraint should be applied");
        }

        @Test
        @DisplayName("全パラメータ - 複雑な計算")
        void allParameters() {
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.setSetting("base", 10.0);
            testable.setSetting("per_level", 3.0);
            testable.setSetting("min", 12.0);
            testable.setSetting("max", 20.0);

            // Level 1: 10 -> min(12) = 12
            assertEquals(12.0, component.getCooldownSeconds(1), 0.001);

            // Level 2: 13 (in range)
            assertEquals(13.0, component.getCooldownSeconds(2), 0.001);

            // Level 5: 22 -> max(20) = 20
            assertEquals(20.0, component.getCooldownSeconds(5), 0.001);
        }

        @Test
        @DisplayName("負のper_level - レベル上昇で減少")
        void negativePerLevel() {
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.setSetting("base", 20.0);
            testable.setSetting("per_level", -1.0);

            assertEquals(20.0, component.getCooldownSeconds(1), 0.001);
            assertEquals(19.0, component.getCooldownSeconds(2), 0.001);
            assertEquals(16.0, component.getCooldownSeconds(5), 0.001);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("実用的なシナリオ - 固定クールダウン")
        void practicalScenarioFixedCooldown() {
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.setSetting("base", 5.0);

            // 全レベルで同じクールダウン
            for (int level = 1; level <= 10; level++) {
                assertEquals(5.0, component.getCooldownSeconds(level), 0.001,
                        "Level " + level + " should have 5 second cooldown");
            }
        }

        @Test
        @DisplayName("実用的なシナリオ - レベル上昇で減少")
        void practicalScenarioDecreasingCooldown() {
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.setSetting("base", 15.0);
            testable.setSetting("per_level", -0.5);
            testable.setSetting("min", 3.0);

            assertEquals(15.0, component.getCooldownSeconds(1), 0.001);
            assertEquals(14.5, component.getCooldownSeconds(2), 0.001);
            assertEquals(14.0, component.getCooldownSeconds(3), 0.001);
            // ...レベルが上がるほど減少
            assertEquals(3.0, component.getCooldownSeconds(25), 0.001,
                    "Should reach minimum at some level");
        }

        @Test
        @DisplayName("実用的なシナリオ - レベル上昇で増加")
        void practicalScenarioIncreasingCooldown() {
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.setSetting("base", 5.0);
            testable.setSetting("per_level", 1.0);
            testable.setSetting("max", 15.0);

            assertEquals(5.0, component.getCooldownSeconds(1), 0.001);
            assertEquals(6.0, component.getCooldownSeconds(2), 0.001);
            assertEquals(10.0, component.getCooldownSeconds(6), 0.001);
            assertEquals(15.0, component.getCooldownSeconds(11), 0.001,
                    "Should reach maximum at some level");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("非常に大きなクールダウン値")
        void veryLargeCooldown() {
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.setSetting("base", 1000000.0);

            assertEquals(1000000.0, component.getCooldownSeconds(1), 0.001);
            assertEquals(1000000000L, component.getCooldownMillis(1));
        }

        @Test
        @DisplayName("非常に小さなクールダウン値")
        void verySmallCooldown() {
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.setSetting("base", 0.001);

            assertEquals(0.001, component.getCooldownSeconds(1), 0.0001);
            assertEquals(1L, component.getCooldownMillis(1),
                    "Should round to 1 millisecond");
        }

        @Test
        @DisplayName("レベル0での計算")
        void levelZero() {
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.setSetting("base", 10.0);
            testable.setSetting("per_level", 2.0);

            // Level 0: base + per_level * (0 - 1) = 10 - 2 = 8
            assertEquals(8.0, component.getCooldownSeconds(0), 0.001);
        }

        @Test
        @DisplayName("小数のクールダウン値")
        void fractionalCooldown() {
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.setSetting("base", 2.5);
            testable.setSetting("per_level", 0.75);

            assertEquals(2.5, component.getCooldownSeconds(1), 0.001);
            assertEquals(3.25, component.getCooldownSeconds(2), 0.001);
            assertEquals(2500L, component.getCooldownMillis(1));
            assertEquals(3250L, component.getCooldownMillis(2));
        }
    }

    @Nested
    @DisplayName("YAML Configuration Examples")
    class YamlConfigurationTests {

        @Test
        @DisplayName("YAML例1: base: 5.0, per_level: 0.0 - 常に5秒")
        void yamlExample1() {
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.setSetting("base", 5.0);
            testable.setSetting("per_level", 0.0);

            for (int level = 1; level <= 10; level++) {
                assertEquals(5.0, component.getCooldownSeconds(level), 0.001,
                        "Should always be 5 seconds");
            }
        }

        @Test
        @DisplayName("YAML例2: base: 10.0, per_level: -1.0 - レベルで減少")
        void yamlExample2() {
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.setSetting("base", 10.0);
            testable.setSetting("per_level", -1.0);

            assertEquals(10.0, component.getCooldownSeconds(1), 0.001,
                    "Level 1: 10 seconds");
            assertEquals(9.0, component.getCooldownSeconds(2), 0.001,
                    "Level 2: 9 seconds");
            assertEquals(6.0, component.getCooldownSeconds(5), 0.001,
                    "Level 5: 6 seconds");
        }

        @Test
        @DisplayName("YAML例3: base: 3.0, per_level: -0.5, min: 1.0 - 最小値あり")
        void yamlExample3() {
            TestableBasicCooldownComponent testable = (TestableBasicCooldownComponent) component;
            testable.setSetting("base", 3.0);
            testable.setSetting("per_level", -0.5);
            testable.setSetting("min", 1.0);

            assertEquals(3.0, component.getCooldownSeconds(1), 0.001,
                    "Level 1: 3 seconds");
            assertEquals(2.5, component.getCooldownSeconds(2), 0.001,
                    "Level 2: 2.5 seconds");
            assertEquals(2.0, component.getCooldownSeconds(3), 0.001,
                    "Level 3: 2 seconds");
            assertEquals(1.5, component.getCooldownSeconds(4), 0.001,
                    "Level 4: 1.5 seconds");
            assertEquals(1.0, component.getCooldownSeconds(5), 0.001,
                    "Level 5: min reached, 1 second");
            assertEquals(1.0, component.getCooldownSeconds(10), 0.001,
                    "Level 10: still at min, 1 second");
        }
    }
}
