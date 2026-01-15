package com.example.rpgplugin.skill.component.condition;

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
 * PotionConditionのテストクラス
 *
 * <p>ターゲットが特定のポーション効果を受けている場合のみtrueを返します。</p>
 * <p>注：PotionConditionはBukkit APIのRegistryに強く依存しており、
 * Mockitoでのモック化は制限されています。このテストでは：</p>
 * <ul>
 *   <li>基本機能テスト（コンストラクタ、getType）</li>
 *   <li>設定管理のテスト</li>
 *   <li>execute()の基本動作</li>
 * </ul>
 * <p>test()メソッドの完全なテストにはBukkitサーバー環境（MockBukkit等）が必要です。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PotionCondition Tests")
class PotionConditionTest {

    private PotionCondition condition;
    private LivingEntity mockCaster;
    private LivingEntity mockTarget;

    @BeforeEach
    void setUp() {
        condition = new PotionCondition();
        mockCaster = mock(LivingEntity.class);
        mockTarget = mock(LivingEntity.class);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("コンストラクタ - 正しく初期化される")
        void constructorInitializesCorrectly() {
            assertNotNull(condition, "Condition should be initialized");
        }

        @Test
        @DisplayName("複数インスタンス - 別々の設定を持つ")
        void multipleInstancesHaveSeparateSettings() {
            PotionCondition condition1 = new PotionCondition();
            PotionCondition condition2 = new PotionCondition();

            condition1.getSettings().set("potion", "speed");
            condition2.getSettings().set("potion", "strength");

            assertNotEquals(condition1.getSettings().getString("potion", ""),
                    condition2.getSettings().getString("potion", ""),
                    "Settings should not be shared between instances");
        }
    }

    @Nested
    @DisplayName("getType() Tests")
    class GetTypeTests {

        @Test
        @DisplayName("getType - CONDITION を返す")
        void getTypeReturnsCondition() {
            assertEquals(ComponentType.CONDITION, condition.getType(),
                    "Type should be CONDITION");
        }
    }

    @Nested
    @DisplayName("getSettings() Tests")
    class GetSettingsTests {

        @Test
        @DisplayName("getSettings - nullではない設定を返す")
        void getSettingsReturnsNotNull() {
            assertNotNull(condition.getSettings(),
                    "Settings should not be null");
        }

        @Test
        @DisplayName("設定の取得と設定")
        void getSettingsSetAndGet() {
            condition.getSettings().set("test_key", "test_value");

            assertEquals("test_value",
                    condition.getSettings().getString("test_key", ""),
                    "Should be able to set and get values");
        }

        @Test
        @DisplayName("設定の上書き - 正しく反映される")
        void settingsOverride() {
            condition.getSettings().set("potion", "speed");
            String firstValue = condition.getSettings().getString("potion", "");

            condition.getSettings().set("potion", "strength");
            String secondValue = condition.getSettings().getString("potion", "");

            assertEquals("speed", firstValue, "First value should be 'speed'");
            assertEquals("strength", secondValue, "Second value should be 'strength'");
            assertNotEquals(firstValue, secondValue, "Values should be different");
        }

        @Test
        @DisplayName("複数の設定値")
        void multipleSettings() {
            condition.getSettings().set("potion", "speed");
            condition.getSettings().set("min_level", 3);

            assertEquals("speed", condition.getSettings().getString("potion", ""),
                    "Potion setting should be 'speed'");
            assertEquals(3, condition.getSettings().getInt("min_level", 0),
                    "Min_level setting should be 3");
        }

        @Test
        @DisplayName("空文字列の設定")
        void emptyStringSetting() {
            condition.getSettings().set("potion", "");

            assertEquals("", condition.getSettings().getString("potion", "default"),
                    "Empty string should be preserved");
        }

        @Test
        @DisplayName("min_level=0の設定")
        void minLevelZeroSetting() {
            condition.getSettings().set("min_level", 0);

            assertEquals(0, condition.getSettings().getInt("min_level", 999),
                    "Min_level should be 0");
        }

        @Test
        @DisplayName("負のmin_level設定")
        void negativeMinLevelSetting() {
            condition.getSettings().set("min_level", -1);

            assertEquals(-1, condition.getSettings().getInt("min_level", 0),
                    "Should accept negative min_level");
        }
    }

    @Nested
    @DisplayName("execute() Tests")
    class ExecuteTests {

        @Test
        @DisplayName("execute - 空のターゲットリスト")
        void executeWithEmptyTargets() {
            boolean result = condition.execute(mockCaster, 1, List.of());

            assertFalse(result, "Should return false for empty target list");
        }

        @Test
        @DisplayName("execute - nullターゲットリスト")
        void executeWithNullTargets() {
            // nullを含まないリストだが、potion設定がないので全てのターゲットが条件を満たさない
            boolean result = condition.execute(mockCaster, 1, List.of(mockTarget));

            assertFalse(result, "Should return false when no target matches");
        }

        @Test
        @DisplayName("execute - 複数ターゲット")
        void executeWithMultipleTargets() {
            LivingEntity target1 = mock(LivingEntity.class);
            LivingEntity target2 = mock(LivingEntity.class);

            // potion設定がないので全てのターゲットが条件を満たさない
            boolean result = condition.execute(mockCaster, 1, List.of(target1, target2));

            assertFalse(result, "Should return false when no target matches condition");
        }

        @Test
        @DisplayName("execute - 空のポーション設定")
        void executeWithEmptyPotionSetting() {
            condition.getSettings().set("potion", "");

            boolean result = condition.execute(mockCaster, 1, List.of(mockTarget));

            assertFalse(result, "Should return false with empty potion setting");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("null caster - executeは正常に動作")
        void nullCasterInExecute() {
            condition.getSettings().set("potion", "");

            boolean result = condition.execute(null, 1, List.of(mockTarget));

            assertFalse(result, "Should handle null caster in execute");
        }

        @Test
        @DisplayName("スキルレベル0 - executeは正常に動作")
        void skillLevelZeroInExecute() {
            condition.getSettings().set("potion", "");

            boolean result = condition.execute(mockCaster, 0, List.of(mockTarget));

            assertFalse(result, "Should handle skill level 0");
        }

        @Test
        @DisplayName("負のスキルレベル - executeは正常に動作")
        void negativeSkillLevelInExecute() {
            condition.getSettings().set("potion", "");

            boolean result = condition.execute(mockCaster, -1, List.of(mockTarget));

            assertFalse(result, "Should handle negative skill level");
        }
    }
}
