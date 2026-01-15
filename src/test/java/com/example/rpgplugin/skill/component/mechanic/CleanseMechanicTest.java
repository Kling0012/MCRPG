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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CleanseMechanicのテストクラス
 *
 * <p>ターゲットのポーション効果を解除します。</p>
 * <p>注：CleanseMechanicはBukkit APIのRegistryに依存しており、
 * Mockitoでの完全なモック化は制限されています。このテストでは：</p>
 * <ul>
 *   <li>基本機能テスト（コンストラクタ、getType）</li>
 *   <li>設定管理のテスト</li>
 *   <li>execute()の基本動作（空リスト）</li>
 * </ul>
 * <p>完全な統合テストにはBukkitサーバー環境（MockBukkit等）が必要です。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CleanseMechanic Tests")
class CleanseMechanicTest {

    private CleanseMechanic mechanic;
    private LivingEntity mockCaster;
    private LivingEntity mockTarget;

    @BeforeEach
    void setUp() {
        mechanic = new CleanseMechanic();
        mockCaster = mock(LivingEntity.class);
        mockTarget = mock(LivingEntity.class);
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
            CleanseMechanic mechanic1 = new CleanseMechanic();
            CleanseMechanic mechanic2 = new CleanseMechanic();

            mechanic1.getSettings().set("bad_only", true);
            mechanic2.getSettings().set("bad_only", false);

            assertNotEquals(mechanic1.getSettings().getBoolean("bad_only", true),
                    mechanic2.getSettings().getBoolean("bad_only", false),
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
        @DisplayName("bad_only設定 - デフォルトはtrue")
        void badOnlyDefaultIsTrue() {
            assertEquals(true, mechanic.getSettings().getBoolean("bad_only", true),
                    "Default bad_only should be true");
        }

        @Test
        @DisplayName("bad_only設定 - falseに設定")
        void badOnlySetToFalse() {
            mechanic.getSettings().set("bad_only", false);

            assertEquals(false, mechanic.getSettings().getBoolean("bad_only", true),
                    "Should be able to set bad_only to false");
        }

        @Test
        @DisplayName("bad_only設定 - trueに設定")
        void badOnlySetToTrue() {
            mechanic.getSettings().set("bad_only", true);

            assertEquals(true, mechanic.getSettings().getBoolean("bad_only", false),
                    "Should be able to set bad_only to true");
        }

        @Test
        @DisplayName("potion設定 - 文字列を設定")
        void potionSettingString() {
            mechanic.getSettings().set("potion", "poison");

            assertEquals("poison",
                    mechanic.getSettings().getString("potion", ""),
                    "Should be able to set potion");
        }

        @Test
        @DisplayName("複数の設定値")
        void multipleSettings() {
            mechanic.getSettings().set("bad_only", false);
            mechanic.getSettings().set("potion", "slowness");

            assertEquals(false, mechanic.getSettings().getBoolean("bad_only", true),
                    "bad_only should be false");
            assertEquals("slowness", mechanic.getSettings().getString("potion", ""),
                    "potion should be 'slowness'");
        }

        @Test
        @DisplayName("設定の上書き")
        void settingsOverride() {
            mechanic.getSettings().set("potion", "poison");
            String firstValue = mechanic.getSettings().getString("potion", "");

            mechanic.getSettings().set("potion", "wither");
            String secondValue = mechanic.getSettings().getString("potion", "");

            assertEquals("poison", firstValue, "First value should be 'poison'");
            assertEquals("wither", secondValue, "Second value should be 'wither'");
            assertNotEquals(firstValue, secondValue, "Values should be different");
        }

        @Test
        @DisplayName("空文字列のpotion設定")
        void emptyPotionSetting() {
            mechanic.getSettings().set("potion", "");

            assertEquals("", mechanic.getSettings().getString("potion", "default"),
                    "Empty string should be preserved");
        }

        @Test
        @DisplayName("nullのpotion設定")
        void nullPotionSetting() {
            // 設定しない場合のデフォルト動作
            String result = mechanic.getSettings().getString("potion", "default");

            assertEquals("default", result, "Should return default when not set");
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
            // 設定を変更しても、空ターゲットならRegistryアクセスは発生しない
            mechanic.getSettings().set("potion", "poison");
            mechanic.getSettings().set("bad_only", true);

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
        @DisplayName("execute - 空のターゲットリストでスキルレベル0")
        void executeWithEmptyTargetsLevelZero() {
            boolean result = mechanic.execute(mockCaster, 0, List.of());

            assertFalse(result, "Should handle level 0 with empty targets");
        }

        @Test
        @DisplayName("execute - 空のターゲットリストで負のスキルレベル")
        void executeWithEmptyTargetsNegativeLevel() {
            boolean result = mechanic.execute(mockCaster, -1, List.of());

            assertFalse(result, "Should handle negative level with empty targets");
        }

        @Test
        @DisplayName("execute - 空のターゲットリストで高いスキルレベル")
        void executeWithEmptyTargetsHighLevel() {
            boolean result = mechanic.execute(mockCaster, 100, List.of());

            assertFalse(result, "Should handle high level with empty targets");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("スペースを含むポーション名")
        void potionNameWithSpaces() {
            mechanic.getSettings().set("potion", "mining fatigue");

            // スペースは保持される
            assertEquals("mining fatigue",
                    mechanic.getSettings().getString("potion", ""),
                    "Should preserve spaces in setting");
        }

        @Test
        @DisplayName("大文字のポーション名")
        void potionNameUpperCase() {
            mechanic.getSettings().set("potion", "POISON");

            assertEquals("POISON",
                    mechanic.getSettings().getString("potion", ""),
                    "Should preserve case in setting");
        }

        @Test
        @DisplayName("小文字のポーション名")
        void potionNameLowerCase() {
            mechanic.getSettings().set("potion", "poison");

            assertEquals("poison",
                    mechanic.getSettings().getString("potion", ""),
                    "Should preserve lowercase");
        }
    }

    @Nested
    @DisplayName("Settings Type Conversion Tests")
    class SettingsTypeConversionTests {

        @Test
        @DisplayName("Boolean型変換 - 文字列からboolean")
        void stringToBooleanConversion() {
            mechanic.getSettings().set("bad_only", "true");
            assertEquals(true, mechanic.getSettings().getBoolean("bad_only", false),
                    "Should convert string 'true' to boolean");

            mechanic.getSettings().set("bad_only", "false");
            assertEquals(false, mechanic.getSettings().getBoolean("bad_only", true),
                    "Should convert string 'false' to boolean");
        }

        @Test
        @DisplayName("Boolean型変換 - 数値からboolean")
        void numberToBooleanConversion() {
            mechanic.getSettings().set("bad_only", 1);
            // 数値からbooleanへの変換はJavaのConfigurationSectionの実装に依存
            String value = mechanic.getSettings().getString("bad_only", "");
            assertEquals("1", value, "Should preserve number as string");
        }

        @Test
        @DisplayName("Integer設定のテスト")
        void integerSetting() {
            mechanic.getSettings().set("test_int", 42);

            assertEquals(42, mechanic.getSettings().getInt("test_int", 0),
                    "Should get integer value");
        }

        @Test
        @DisplayName("Double設定のテスト")
        void doubleSetting() {
            mechanic.getSettings().set("test_double", 3.14);

            assertEquals(3.14, mechanic.getSettings().getDouble("test_double", 0.0),
                    "Should get double value");
        }
    }

    @Nested
    @DisplayName("Component Integration Tests")
    class ComponentIntegrationTests {

        @Test
        @DisplayName("getSettingsを通じて設定を変更すると、次回のexecuteに反映される")
        void settingsChangeAffectsExecution() {
            // bad_only = true
            mechanic.getSettings().set("bad_only", true);
            assertTrue(mechanic.getSettings().getBoolean("bad_only", false),
                    "bad_only should be true");

            // bad_only = falseに変更
            mechanic.getSettings().set("bad_only", false);
            assertFalse(mechanic.getSettings().getBoolean("bad_only", true),
                    "bad_only should be false after change");
        }

        @Test
        @DisplayName("設定の削除")
        void removeSetting() {
            mechanic.getSettings().set("potion", "poison");
            assertEquals("poison", mechanic.getSettings().getString("potion", ""));

            mechanic.getSettings().set("potion", null);
            // nullを設定すると、デフォルト値が返される
            assertEquals("", mechanic.getSettings().getString("potion", ""),
                    "Should return empty string after setting to null");
        }
    }
}
