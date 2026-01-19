package com.example.rpgplugin.skill.component.mechanic;

import com.example.rpgplugin.skill.component.ComponentType;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * LaunchMechanicのテストクラス
 *
 * <p>投射物発射メカニックをテストします。</p>
 * <p>注：LaunchMechanicはBukkit APIのWorld.spawn等に依存しており、
 * Mockitoでの完全なモック化は制限されています。このテストでは：</p>
 * <ul>
 *   <li>基本機能テスト（コンストラクタ、getType）</li>
 *   <li>設定管理のテスト</li>
 *   <li>execute()の基本動作（空リスト）</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LaunchMechanic Tests")
class LaunchMechanicTest {

    private LaunchMechanic mechanic;
    private LivingEntity mockCaster;

    @BeforeEach
    void setUp() {
        mechanic = new LaunchMechanic();
        mockCaster = mock(LivingEntity.class);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("コンストラクタ - 正しく初期化される")
        void constructorInitializesCorrectly() {
            assertNotNull(mechanic, "Mechanic should be initialized");
        }

        @Test
        @DisplayName("複数インスタンス - 別々の設定を持つ")
        void multipleInstancesHaveSeparateSettings() {
            LaunchMechanic mechanic1 = new LaunchMechanic();
            LaunchMechanic mechanic2 = new LaunchMechanic();

            mechanic1.getSettings().set("speed", 3.0);
            mechanic2.getSettings().set("speed", 2.0);

            assertNotEquals(mechanic1.getSettings().getDouble("speed", 0),
                    mechanic2.getSettings().getDouble("speed", 0),
                    "Settings should not be shared between instances");
        }
    }

    @Nested
    @DisplayName("getType() Tests")
    class GetTypeTests {

        @Test
        @DisplayName("getType - MECHANIC を返す")
        void getTypeReturnsMechanic() {
            assertEquals(ComponentType.MECHANIC, mechanic.getType(),
                    "Type should be MECHANIC");
        }
    }

    @Nested
    @DisplayName("getKey() Tests")
    class GetKeyTests {

        @Test
        @DisplayName("getKey - launch を返す")
        void getKeyReturnsLaunch() {
            assertEquals("launch", mechanic.getKey(), "Key should be 'launch'");
        }
    }

    @Nested
    @DisplayName("getSettings() Tests")
    class GetSettingsTests {

        @Test
        @DisplayName("getSettings - nullではない設定を返す")
        void getSettingsReturnsNotNull() {
            assertNotNull(mechanic.getSettings(),
                    "Settings should not be null");
        }

        @Test
        @DisplayName("設定の取得と設定")
        void getSettingsSetAndGet() {
            mechanic.getSettings().set("test_key", "test_value");

            assertEquals("test_value",
                    mechanic.getSettings().getString("test_key", ""),
                    "Should be able to set and get values");
        }

        @Test
        @DisplayName("projectile設定 - デフォルトはARROW")
        void projectileDefaultIsArrow() {
            assertEquals("ARROW",
                    mechanic.getSettings().getString("projectile", "ARROW"),
                    "Default projectile should be ARROW");
        }

        @Test
        @DisplayName("projectile設定 - 文字列を設定")
        void projectileSettingString() {
            mechanic.getSettings().set("projectile", "snowball");

            assertEquals("snowball",
                    mechanic.getSettings().getString("projectile", ""),
                    "Should be able to set projectile");
        }

        @Test
        @DisplayName("speed設定 - 数値を設定")
        void speedSettingNumeric() {
            mechanic.getSettings().set("speed", 3.5);

            assertEquals(3.5,
                    mechanic.getSettings().getDouble("speed", 0),
                    "Should be able to set speed");
        }

        @Test
        @DisplayName("spread設定 - 数値を設定")
        void spreadSettingNumeric() {
            mechanic.getSettings().set("spread", 0.2);

            assertEquals(0.2,
                    mechanic.getSettings().getDouble("spread", 0),
                    "Should be able to set spread");
        }

        @Test
        @DisplayName("複数の設定値")
        void multipleSettings() {
            mechanic.getSettings().set("projectile", "egg");
            mechanic.getSettings().set("speed", 1.5);
            mechanic.getSettings().set("spread", 0.05);

            assertEquals("egg",
                    mechanic.getSettings().getString("projectile", ""),
                    "projectile should be 'egg'");
            assertEquals(1.5,
                    mechanic.getSettings().getDouble("speed", 0),
                    "speed should be 1.5");
            assertEquals(0.05,
                    mechanic.getSettings().getDouble("spread", 0),
                    "spread should be 0.05");
        }

        @Test
        @DisplayName("設定の上書き")
        void settingsOverride() {
            mechanic.getSettings().set("speed", 2.0);
            double firstValue = mechanic.getSettings().getDouble("speed", 0);

            mechanic.getSettings().set("speed", 5.0);
            double secondValue = mechanic.getSettings().getDouble("speed", 0);

            assertEquals(2.0, firstValue, "First value should be 2.0");
            assertEquals(5.0, secondValue, "Second value should be 5.0");
            assertNotEquals(firstValue, secondValue, "Values should be different");
        }
    }

    @Nested
    @DisplayName("execute() Tests")
    class ExecuteTests {

        @Test
        @DisplayName("execute - 空のターゲットリスト")
        void executeWithEmptyTargets() {
            boolean result = mechanic.execute(mockCaster, 1, List.of());

            assertFalse(result, "Should return false for empty target list");
        }

        @Test
        @DisplayName("execute - 空のターゲットリストで設定を変更")
        void executeWithEmptyTargetsAndSettings() {
            mechanic.getSettings().set("projectile", "fireball");
            mechanic.getSettings().set("speed", 3.0);
            mechanic.getSettings().set("spread", 0.2);

            boolean result = mechanic.execute(mockCaster, 1, List.of());

            assertFalse(result, "Should return false for empty target list regardless of settings");
        }

        @Test
        @DisplayName("execute - 空のターゲットリストでnull caster")
        void executeWithEmptyTargetsNullCaster() {
            boolean result = mechanic.execute(null, 1, List.of());

            assertFalse(result, "Should handle null caster with empty targets");
        }

        @Test
        @DisplayName("execute - スキルレベル0")
        void executeWithSkillLevel0() {
            boolean result = mechanic.execute(mockCaster, 0, List.of());

            assertFalse(result, "Should handle level 0 with empty targets");
        }

        @Test
        @DisplayName("execute - 負のスキルレベル")
        void executeWithNegativeLevel() {
            boolean result = mechanic.execute(mockCaster, -1, List.of());

            assertFalse(result, "Should handle negative level with empty targets");
        }

        @Test
        @DisplayName("execute - 高いスキルレベル")
        void executeWithHighLevel() {
            boolean result = mechanic.execute(mockCaster, 100, List.of());

            assertFalse(result, "Should handle high level with empty targets");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("speed - 0")
        void speedZero() {
            mechanic.getSettings().set("speed", 0);

            assertEquals(0, mechanic.getSettings().getDouble("speed", 1),
                    "Speed 0 should be preserved");
        }

        @Test
        @DisplayName("speed - 負の値")
        void speedNegative() {
            mechanic.getSettings().set("speed", -1.0);

            assertEquals(-1.0, mechanic.getSettings().getDouble("speed", 1),
                    "Negative speed should be preserved");
        }

        @Test
        @DisplayName("spread - 0")
        void spreadZero() {
            mechanic.getSettings().set("spread", 0);

            assertEquals(0, mechanic.getSettings().getDouble("spread", 1),
                    "Spread 0 should be preserved");
        }

        @Test
        @DisplayName("spread - 大きな値")
        void spreadLarge() {
            mechanic.getSettings().set("spread", 1.0);

            assertEquals(1.0, mechanic.getSettings().getDouble("spread", 0),
                    "Large spread should be preserved");
        }

        @Test
        @DisplayName("projectile - 空文字列")
        void projectileEmptyString() {
            mechanic.getSettings().set("projectile", "");

            assertEquals("", mechanic.getSettings().getString("projectile", "default"),
                    "Empty projectile should be preserved");
        }

        @Test
        @DisplayName("projectile - 大文字小文字")
        void projectileCase() {
            mechanic.getSettings().set("projectile", "Fireball");

            assertEquals("Fireball", mechanic.getSettings().getString("projectile", ""),
                    "Projectile case should be preserved");
        }

        @Test
        @DisplayName("projectile - 特殊文字")
        void projectileSpecialChars() {
            mechanic.getSettings().set("projectile", "custom_projectile_123");

            assertEquals("custom_projectile_123",
                    mechanic.getSettings().getString("projectile", ""),
                    "Special chars should be preserved");
        }
    }

    @Nested
    @DisplayName("Settings Type Conversion Tests")
    class SettingsTypeConversionTests {

        @Test
        @DisplayName("IntegerからDoubleへの変換")
        void integerToDoubleConversion() {
            mechanic.getSettings().set("speed", 3);

            double result = mechanic.getSettings().getDouble("speed", 0);
            assertEquals(3.0, result, 0.001,
                    "Integer should be converted to double");
        }

        @Test
        @DisplayName("DoubleからIntegerへの変換（切り捨て）")
        void doubleToIntegerConversion() {
            mechanic.getSettings().set("spread", 0.5);

            int result = mechanic.getSettings().getInt("spread", 0);
            assertEquals(0, result,
                    "Double should be converted to int (truncated)");
        }

        @Test
        @DisplayName("Stringから数値への変換")
        void stringToNumericConversion() {
            mechanic.getSettings().set("speed", "2.5");

            double result = mechanic.getSettings().getDouble("speed", 0);
            assertEquals(2.5, result, 0.001,
                    "Numeric string should be converted to double");
        }
    }

    @Nested
    @DisplayName("Component Integration Tests")
    class ComponentIntegrationTests {

        @Test
        @DisplayName("getSettingsを通じて設定を変更すると、値が正しく反映される")
        void settingsChangeAffectsState() {
            // speed = 2.0を設定
            mechanic.getSettings().set("speed", 2.0);
            assertEquals(2.0, mechanic.getSettings().getDouble("speed", 0),
                    "Speed should be 2.0 after setting");

            // speed = 5.0に変更
            mechanic.getSettings().set("speed", 5.0);
            assertEquals(5.0, mechanic.getSettings().getDouble("speed", 0),
                    "Speed should be 5.0 after change");
        }

        @Test
        @DisplayName("設定の削除")
        void removeSetting() {
            mechanic.getSettings().set("projectile", "snowball");
            assertEquals("snowball", mechanic.getSettings().getString("projectile", ""));

            mechanic.getSettings().set("projectile", null);
            // nullを設定すると、デフォルト値が返される
            assertEquals("ARROW", mechanic.getSettings().getString("projectile", "ARROW"),
                    "Should return default after setting to null");
        }
    }
}
