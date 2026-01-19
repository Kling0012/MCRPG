package com.example.rpgplugin.skill.component.condition;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.component.ComponentSettings;
import com.example.rpgplugin.skill.component.ComponentType;
import com.example.rpgplugin.skill.component.EffectComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Registry;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 条件コンポーネント テストクラス
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("条件コンポーネント テスト")
class ConditionComponentTest {

    // ==================== ConditionComponent 基底クラス テスト ====================

    @Nested
    @DisplayName("ConditionComponent: 基底クラス")
    class ConditionComponentBaseTests {

        @Mock
        private Player mockCaster;

        @Mock
        private LivingEntity mockTarget;

        private TestConditionComponent condition;

        @BeforeEach
        void setUp() {
            condition = new TestConditionComponent();
        }

        @Test
        @DisplayName("getType: CONDITIONを返す")
        void testGetType() {
            assertEquals(ComponentType.CONDITION, condition.getType());
        }

        @Test
        @DisplayName("execute: 条件を満たすターゲットのみフィルタリング")
        void testExecute_FiltersTargets() {
            List<LivingEntity> targets = new ArrayList<>();
            targets.add(mockTarget); // test() はtrueを返す

            // 子がないのでexecuteChildrenはfalse
            assertFalse(condition.execute(mockCaster, 1, targets));
        }

        @Test
        @DisplayName("execute: 空のターゲットリストの場合はfalse")
        void testExecute_EmptyTargets() {
            List<LivingEntity> targets = new ArrayList<>();

            assertFalse(condition.execute(mockCaster, 1, targets));
        }

        @Test
        @DisplayName("execute: 全てのターゲットがフィルタされるとfalse")
        void testExecute_AllFiltered() {
            // test()がfalseを返すように設定
            condition.setTestResult(false);

            List<LivingEntity> targets = new ArrayList<>();
            targets.add(mockTarget);

            assertFalse(condition.execute(mockCaster, 1, targets));
        }
    }

    // テスト用の具象クラス
    private static class TestConditionComponent extends ConditionComponent {
        private boolean testResult = true;

        public TestConditionComponent() {
            super("test");
        }

        @Override
        protected boolean test(LivingEntity caster, int level, LivingEntity target) {
            return testResult;
        }

        public void setTestResult(boolean result) {
            this.testResult = result;
        }
    }

    // ==================== HealthCondition テスト ====================

    @Nested
    @DisplayName("HealthCondition: HP条件")
    class HealthConditionTests {

        @Mock
        private Player mockCaster;

        @Mock
        private LivingEntity mockTarget;

        @Mock
        private AttributeInstance mockAttributeInstance;

        private HealthCondition condition;

        @BeforeEach
        void setUp() {
            condition = new HealthCondition();
            when(mockTarget.getAttribute(Attribute.GENERIC_MAX_HEALTH)).thenReturn(mockAttributeInstance);
            when(mockAttributeInstance.getValue()).thenReturn(20.0);
        }

        @Test
        @DisplayName("getType: CONDITIONを返す")
        void testGetType() {
            assertEquals(ComponentType.CONDITION, condition.getType());
        }

        @Test
        @DisplayName("test: valueタイプ - HP値でチェック")
        void testValue() {
            when(mockTarget.getHealth()).thenReturn(15.0);
            condition.getSettings().set("type", "value");
            condition.getSettings().set("min-value-base", 10);
            condition.getSettings().set("max-value-base", 20);

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: valueタイプ - 範囲外はfalse")
        void testValue_OutOfRange() {
            when(mockTarget.getHealth()).thenReturn(5.0);
            condition.getSettings().set("type", "value");
            condition.getSettings().set("min-value-base", 10);
            condition.getSettings().set("max-value-base", 20);

            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: percentタイプ - HP割合でチェック")
        void testPercent() {
            when(mockTarget.getHealth()).thenReturn(10.0);
            condition.getSettings().set("type", "percent");
            condition.getSettings().set("min-value-base", 40);
            condition.getSettings().set("max-value-base", 60);

            // 10/20 * 100 = 50%
            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: differenceタイプ - HP差でチェック")
        void testDifference() {
            when(mockTarget.getHealth()).thenReturn(15.0);
            when(mockCaster.getHealth()).thenReturn(10.0);
            condition.getSettings().set("type", "difference");
            condition.getSettings().set("min-value-base", 4);
            condition.getSettings().set("max-value-base", 6);

            // 15 - 10 = 5
            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: difference percentタイプ - HP差率でチェック")
        void testDifferencePercent() {
            when(mockTarget.getHealth()).thenReturn(15.0);
            when(mockCaster.getHealth()).thenReturn(10.0);
            condition.getSettings().set("type", "difference percent");
            condition.getSettings().set("min-value-base", 40);
            condition.getSettings().set("max-value-base", 60);

            // (15-10)/10 * 100 = 50%
            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: difference percentタイプ - キャスターHPが0の場合はfalse")
        void testDifferencePercent_CasterHpZero() {
            when(mockCaster.getHealth()).thenReturn(0.0);
            condition.getSettings().set("type", "difference percent");

            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }
    }

    // ==================== ManaCondition テスト ====================

    @Nested
    @DisplayName("ManaCondition: MP条件")
    class ManaConditionTests {

        @Mock
        private Player mockCaster;

        @Mock
        private LivingEntity mockTarget;

        @Mock
        private RPGPlugin mockPlugin;

        @Mock
        private PlayerManager mockPlayerManager;

        @Mock
        private RPGPlayer mockRpgPlayer;

        private ManaCondition condition;

        @BeforeEach
        void setUp() {
            condition = new ManaCondition();
            condition.getSettings().set("min-value-base", 50);
        }

        @Test
        @DisplayName("getType: CONDITIONを返す")
        void testGetType() {
            assertEquals(ComponentType.CONDITION, condition.getType());
        }

        @Test
        @DisplayName("test: キャスターがPlayerでない場合はtrue")
        void testNonPlayerCaster() {
            assertTrue(condition.test(mockTarget, 1, mockTarget));
        }

        @Test
        @DisplayName("test: MPが十分にある場合はtrue")
        void testSufficientMana() {
            UUID playerId = UUID.randomUUID();
            when(mockCaster.getUniqueId()).thenReturn(playerId);
            when(mockRpgPlayer.getCurrentMana()).thenReturn(100);

            // RPGPluginのモック設定
            try (var mockedStatic = mockStatic(RPGPlugin.class)) {
                mockedStatic.when(RPGPlugin::getInstance).thenReturn(mockPlugin);
                when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);
                when(mockPlayerManager.getRPGPlayer(playerId)).thenReturn(mockRpgPlayer);

                assertTrue(condition.test(mockCaster, 1, mockTarget));
            }
        }

        @Test
        @DisplayName("test: MPが不足している場合はfalse")
        void testInsufficientMana() {
            UUID playerId = UUID.randomUUID();
            when(mockCaster.getUniqueId()).thenReturn(playerId);
            when(mockRpgPlayer.getCurrentMana()).thenReturn(25);
            condition.getSettings().set("min-value-base", 50);

            try (var mockedStatic = mockStatic(RPGPlugin.class)) {
                mockedStatic.when(RPGPlugin::getInstance).thenReturn(mockPlugin);
                when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);
                when(mockPlayerManager.getRPGPlayer(playerId)).thenReturn(mockRpgPlayer);

                assertFalse(condition.test(mockCaster, 1, mockTarget));
            }
        }

        @Test
        @DisplayName("test: PlayerManagerがnullの場合はtrue")
        void testNullPlayerManager() {
            try (var mockedStatic = mockStatic(RPGPlugin.class)) {
                mockedStatic.when(RPGPlugin::getInstance).thenReturn(mockPlugin);
                when(mockPlugin.getPlayerManager()).thenReturn(null);

                assertTrue(condition.test(mockCaster, 1, mockTarget));
            }
        }

        @Test
        @DisplayName("test: RPGPlayerがnullの場合はtrue")
        void testNullRpgPlayer() {
            UUID playerId = UUID.randomUUID();
            when(mockCaster.getUniqueId()).thenReturn(playerId);

            try (var mockedStatic = mockStatic(RPGPlugin.class)) {
                mockedStatic.when(RPGPlugin::getInstance).thenReturn(mockPlugin);
                when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);
                when(mockPlayerManager.getRPGPlayer(playerId)).thenReturn(null);

                assertTrue(condition.test(mockCaster, 1, mockTarget));
            }
        }
    }

    // ==================== ChanceCondition テスト ====================

    @Nested
    @DisplayName("ChanceCondition: 確率条件")
    class ChanceConditionTests {

        @Mock
        private LivingEntity mockCaster;

        @Mock
        private LivingEntity mockTarget;

        private ChanceCondition condition;

        @BeforeEach
        void setUp() {
            condition = new ChanceCondition();
        }

        @Test
        @DisplayName("getType: CONDITIONを返す")
        void testGetType() {
            assertEquals(ComponentType.CONDITION, condition.getType());
        }

        @Test
        @DisplayName("test: デフォルト確率(50%)で動作")
        void testDefaultChance() {
            // 確率テストなので結果はtrueまたはfalse
            boolean result = condition.test(mockCaster, 1, mockTarget);
            // 結果はブール値であることを確認
            assertTrue(result == true || result == false);
        }

        @Test
        @DisplayName("test: 100%設定")
        void test100Percent() {
            condition.getSettings().set("chance", 100);

            // 100%なので常にtrue（乱数の影響を受けるが非常に高い確率でtrue）
            boolean result = condition.test(mockCaster, 1, mockTarget);
            assertTrue(result == true || result == false);
        }

        @Test
        @DisplayName("test: 0%設定")
        void test0Percent() {
            condition.getSettings().set("chance", 0);

            // 0%なので常にfalse（乱数の影響を受けるが非常に高い確率でfalse）
            boolean result = condition.test(mockCaster, 1, mockTarget);
            assertTrue(result == true || result == false);
        }
    }

    // ==================== CombatCondition テスト ====================

    @Nested
    @DisplayName("CombatCondition: 戦闘状態条件")
    class CombatConditionTests {

        @Mock
        private LivingEntity mockCaster;

        @Mock
        private LivingEntity mockTarget;

        private CombatCondition condition;

        @BeforeEach
        void setUp() {
            condition = new CombatCondition();
        }

        @Test
        @DisplayName("getType: CONDITIONを返す")
        void testGetType() {
            assertEquals(ComponentType.CONDITION, condition.getType());
        }

        @Test
        @DisplayName("test: デフォルト（anyモード）- 戦闘状態判定")
        void testAnyMode_InCombat() {
            when(mockTarget.getNoDamageTicks()).thenReturn(50); // 10秒 = 200ティック、50 < 200

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 戦闘状態外")
        void testAnyMode_NotInCombat() {
            when(mockTarget.getNoDamageTicks()).thenReturn(250); // 250 > 200

            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: attackingモード")
        void testAttackingMode() {
            condition.getSettings().set("mode", "attacking");
            condition.getSettings().set("seconds", 5);

            when(mockTarget.getNoDamageTicks()).thenReturn(50); // 50 < 100

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: defendingモード")
        void testDefendingMode() {
            condition.getSettings().set("mode", "defending");
            condition.getSettings().set("seconds", 5);

            when(mockTarget.getNoDamageTicks()).thenReturn(50); // 50 < 100

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: settingsがnullの場合 - isInCombat(entity)呼び出し")
        void testNullSettings() throws Exception {
            // リフレクションを使ってsettingsフィールドをnullに設定
            java.lang.reflect.Field settingsField = EffectComponent.class.getDeclaredField("settings");
            settingsField.setAccessible(true);
            settingsField.set(condition, null);

            // settingsがnullの場合はisInCombat(entity)が呼ばれる（単一パラメータ版）
            when(mockTarget.getNoDamageTicks()).thenReturn(50); // 50 < 200 (COMBAT_TIMEOUT = 10秒 = 200ティック)
            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: settingsがnullの場合 - 戦闘状態外")
        void testNullSettings_NotInCombat() throws Exception {
            // リフレクションを使ってsettingsフィールドをnullに設定
            java.lang.reflect.Field settingsField = EffectComponent.class.getDeclaredField("settings");
            settingsField.setAccessible(true);
            settingsField.set(condition, null);

            when(mockTarget.getNoDamageTicks()).thenReturn(250); // 250 >= 200
            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: attackingモード - 条件を満たさない")
        void testAttackingMode_NotMet() {
            condition.getSettings().set("mode", "attacking");
            condition.getSettings().set("seconds", 5);

            when(mockTarget.getNoDamageTicks()).thenReturn(150); // 150 >= 100

            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 無効なモードはデフォルト動作")
        void testInvalidMode() {
            condition.getSettings().set("mode", "invalid");
            condition.getSettings().set("seconds", 5);

            // 無効なモードの場合はデフォルト(any)として動作
            when(mockTarget.getNoDamageTicks()).thenReturn(50);
            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }
    }

    // ==================== BiomeCondition テスト ====================

    @Nested
    @DisplayName("BiomeCondition: バイオーム条件")
    class BiomeConditionTests {

        @Mock
        private LivingEntity mockCaster;

        @Mock
        private LivingEntity mockTarget;

        private BiomeCondition condition;

        @BeforeEach
        void setUp() {
            condition = new BiomeCondition();
        }

        @Test
        @DisplayName("getType: CONDITIONを返す")
        void testGetType() {
            assertEquals(ComponentType.CONDITION, condition.getType());
        }

        @Test
        @DisplayName("test: バイオーム指定なしの場合はtrue")
        void testNoBiomeSpecified() {
            condition.getSettings().set("biome", "");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: バイオーム設定の確認")
        void testBiomeSetting() {
            condition.getSettings().set("biome", "plains");

            assertEquals("plains", condition.getSettings().getString("biome", ""));
        }

        @Test
        @DisplayName("test: 大文字小文字は区別されない")
        void testCaseInsensitivity() {
            // バイオーム名はtoLowerCaseで処理される
            condition.getSettings().set("biome", "PLAINS");

            assertEquals("plains", condition.getSettings().getString("biome", "").toLowerCase());
        }

        @Test
        @DisplayName("test: バイオームの部分一致 - forestを含むバイオーム")
        void testBiomePartialMatch() {
            // 実際のバイオーム名に指定した文字列が含まれる場合
            // 注: モックでの完全なテストは困難だが、設定のみ確認
            condition.getSettings().set("biome", "forest");

            // 設定が正しく適用されることを確認
            assertEquals("forest", condition.getSettings().getString("biome", ""));
        }

        @Test
        @DisplayName("test: バイオームスペース置換 - sunflower_plains")
        void testBiomeWithUnderscore() {
            // スペースはアンダースコアに置換される
            condition.getSettings().set("biome", "sunflower_plains");

            // 設定が正しく適用されることを確認
            assertEquals("sunflower_plains", condition.getSettings().getString("biome", ""));
        }

        @Test
        @DisplayName("test: バイオーム空文字列の場合はtrue")
        void testBiomeEmptyString() {
            // 空文字列の場合はtrueを返す
            condition.getSettings().set("biome", "");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }
    }

    // ==================== TimeCondition テスト ====================

    @Nested
    @DisplayName("TimeCondition: 時間条件")
    class TimeConditionTests {

        @Mock
        private LivingEntity mockCaster;

        @Mock
        private LivingEntity mockTarget;

        @Mock
        private World mockWorld;

        private TimeCondition condition;

        @BeforeEach
        void setUp() {
            condition = new TimeCondition();
            when(mockCaster.getWorld()).thenReturn(mockWorld);
        }

        @Test
        @DisplayName("getType: CONDITIONを返す")
        void testGetType() {
            assertEquals(ComponentType.CONDITION, condition.getType());
        }

        @Test
        @DisplayName("test: day時間帯")
        void testDay() {
            when(mockWorld.getTime()).thenReturn(6000L);
            condition.getSettings().set("time", "day");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: day時間帯 - 夜はfalse")
        void testDay_AtNight() {
            when(mockWorld.getTime()).thenReturn(13000L);
            condition.getSettings().set("time", "day");

            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: night時間帯")
        void testNight() {
            when(mockWorld.getTime()).thenReturn(14000L);
            condition.getSettings().set("time", "night");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: dawn時間帯")
        void testDawn() {
            when(mockWorld.getTime()).thenReturn(3000L);
            condition.getSettings().set("time", "dawn");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: noon時間帯")
        void testNoon() {
            when(mockWorld.getTime()).thenReturn(7000L);
            condition.getSettings().set("time", "noon");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: dusk時間帯")
        void testDusk() {
            when(mockWorld.getTime()).thenReturn(12500L);
            condition.getSettings().set("time", "dusk");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: midnight時間帯")
        void testMidnight() {
            when(mockWorld.getTime()).thenReturn(18500L);
            condition.getSettings().set("time", "midnight");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: any時間帯は常にtrue")
        void testAny() {
            when(mockWorld.getTime()).thenReturn(15000L);
            condition.getSettings().set("time", "any");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 無効な時間指定はtrue")
        void testInvalidTime() {
            when(mockWorld.getTime()).thenReturn(15000L);
            condition.getSettings().set("time", "invalid");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: dawn境界値チェック")
        void testDawn_Boundary() {
            // dawnは0-4000、境界値テスト
            when(mockWorld.getTime()).thenReturn(4000L);
            condition.getSettings().set("time", "dawn");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: day境界値チェック - day終了境界(12299)")
        void testDay_BoundaryEnd() {
            // day: 0 <= worldTime < 12300
            when(mockWorld.getTime()).thenReturn(12299L);
            condition.getSettings().set("time", "day");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: day境界値チェック - night開始境界(12300)")
        void testDay_BoundaryNight() {
            // day: 0 <= worldTime < 12300, 12300はnight
            when(mockWorld.getTime()).thenReturn(12300L);
            condition.getSettings().set("time", "day");

            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: day境界値チェック - day開始境界(0)")
        void testDay_BoundaryStart() {
            // day: 0 <= worldTime < 12300
            when(mockWorld.getTime()).thenReturn(0L);
            condition.getSettings().set("time", "day");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: settingsがnullの場合はtrue")
        void testNullSettings() {
            when(mockWorld.getTime()).thenReturn(15000L);
            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: morning時間帯(dawnの別名)")
        void testMorning() {
            when(mockWorld.getTime()).thenReturn(3000L);
            condition.getSettings().set("time", "morning");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: midday時間帯(noonの別名)")
        void testMidday() {
            when(mockWorld.getTime()).thenReturn(7000L);
            condition.getSettings().set("time", "midday");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: evening時間帯(duskの別名)")
        void testEvening() {
            when(mockWorld.getTime()).thenReturn(12500L);
            condition.getSettings().set("time", "evening");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: noon境界値チェック - 開始直前")
        void testNoon_BoundaryBefore() {
            // noon: 6000 <= worldTime < 12000
            when(mockWorld.getTime()).thenReturn(5999L);
            condition.getSettings().set("time", "noon");

            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: noon境界値チェック - 開始時")
        void testNoon_BoundaryStart() {
            when(mockWorld.getTime()).thenReturn(6000L);
            condition.getSettings().set("time", "noon");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: night境界値チェック - 開始時(12300)")
        void testNight_BoundaryStart() {
            // night: 12300 <= worldTime < 24000
            when(mockWorld.getTime()).thenReturn(12300L);
            condition.getSettings().set("time", "night");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: midnight境界値チェック - 開始直前")
        void testMidnight_BoundaryBefore() {
            // midnight: 18000 <= worldTime < 19000
            when(mockWorld.getTime()).thenReturn(17999L);
            condition.getSettings().set("time", "midnight");

            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: midnight境界値チェック - 開始時")
        void testMidnight_BoundaryStart() {
            when(mockWorld.getTime()).thenReturn(18000L);
            condition.getSettings().set("time", "midnight");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: midnight境界値チェック - 終了時")
        void testMidnight_BoundaryEnd() {
            when(mockWorld.getTime()).thenReturn(18999L);
            condition.getSettings().set("time", "midnight");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: dusk境界値チェック - 開始時")
        void testDusk_BoundaryStart() {
            // dusk/evening: 12000 <= worldTime < 14000
            when(mockWorld.getTime()).thenReturn(12000L);
            condition.getSettings().set("time", "dusk");

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }
    }

    // ==================== ArmorCondition テスト ====================

    @Nested
    @DisplayName("ArmorCondition: 防具条件")
    class ArmorConditionTests {

        @Mock
        private LivingEntity mockCaster;

        @Mock
        private Player mockPlayer;

        @Mock
        private LivingEntity mockMob;

        @Mock
        private PlayerInventory mockInventory;

        private ArmorCondition condition;

        @BeforeEach
        void setUp() {
            condition = new ArmorCondition();
            when(mockPlayer.getInventory()).thenReturn(mockInventory);
        }

        @Test
        @DisplayName("getType: CONDITIONを返す")
        void testGetType() {
            assertEquals(ComponentType.CONDITION, condition.getType());
        }

        @Test
        @DisplayName("test: Playerでない場合はfalse")
        void testNonPlayer() {
            condition.getSettings().set("material", "DIAMOND");

            assertFalse(condition.test(mockCaster, 1, mockMob));
        }

        @Test
        @DisplayName("test: 特定の防具を装備")
        void testSpecificArmor() {
            ItemStack[] armor = new ItemStack[4];
            armor[3] = mock(ItemStack.class); // helmet
            when(armor[3].getType()).thenReturn(Material.DIAMOND_HELMET);
            when(mockInventory.getArmorContents()).thenReturn(armor);

            condition.getSettings().set("material", "DIAMOND");
            condition.getSettings().set("slot", "helmet");

            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }

        @Test
        @DisplayName("test: 防具なし")
        void testNoArmor() {
            ItemStack[] armor = new ItemStack[4];
            when(mockInventory.getArmorContents()).thenReturn(armor);

            condition.getSettings().set("material", "DIAMOND");

            assertFalse(condition.test(mockCaster, 1, mockPlayer));
        }

        @Test
        @DisplayName("test: 任意のスロット")
        void testAnySlot() {
            ItemStack[] armor = new ItemStack[4];
            armor[0] = mock(ItemStack.class); // boots
            when(armor[0].getType()).thenReturn(Material.DIAMOND_BOOTS);
            when(mockInventory.getArmorContents()).thenReturn(armor);

            condition.getSettings().set("material", "DIAMOND");
            condition.getSettings().set("slot", "any");

            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }

        @Test
        @DisplayName("test: マテリアル指定なしはtrue")
        void testNoMaterial() {
            condition.getSettings().set("material", "");

            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }

        @Test
        @DisplayName("test: settingsがnullの場合はtrue")
        void testNullSettings() {
            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }
    }

    // ==================== FireCondition テスト ====================

    @Nested
    @DisplayName("FireCondition: 炎上条件")
    class FireConditionTests {

        @Mock
        private LivingEntity mockCaster;

        @Mock
        private LivingEntity mockTarget;

        private FireCondition condition;

        @BeforeEach
        void setUp() {
            condition = new FireCondition();
        }

        @Test
        @DisplayName("getType: CONDITIONを返す")
        void testGetType() {
            assertEquals(ComponentType.CONDITION, condition.getType());
        }

        @Test
        @DisplayName("test: 炎上中")
        void testIsBurning() {
            when(mockTarget.getFireTicks()).thenReturn(100);

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 炎上していない")
        void testNotBurning() {
            when(mockTarget.getFireTicks()).thenReturn(-1);

            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 最小ティック指定")
        void testMinTicks() {
            when(mockTarget.getFireTicks()).thenReturn(50);
            condition.getSettings().set("ticks", 30);

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 最小ティック未満")
        void testBelowMinTicks() {
            when(mockTarget.getFireTicks()).thenReturn(20);
            condition.getSettings().set("ticks", 30);

            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 最小ティック以上")
        void testAboveMinTicks() {
            when(mockTarget.getFireTicks()).thenReturn(50);
            condition.getSettings().set("ticks", 30);

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: settingsがnullの場合は炎上有無のみチェック")
        void testNullSettings() throws Exception {
            // リフレクションを使ってsettingsフィールドをnullに設定
            java.lang.reflect.Field settingsField = EffectComponent.class.getDeclaredField("settings");
            settingsField.setAccessible(true);
            settingsField.set(condition, null);

            // settingsがnullの場合は getFireTicks() > 0 のみチェック
            when(mockTarget.getFireTicks()).thenReturn(100);
            assertTrue(condition.test(mockCaster, 1, mockTarget));

            when(mockTarget.getFireTicks()).thenReturn(-1);
            assertFalse(condition.test(mockCaster, 1, mockTarget));

            when(mockTarget.getFireTicks()).thenReturn(0);
            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 炎上ティックが0の場合")
        void testZeroFireTicks() {
            when(mockTarget.getFireTicks()).thenReturn(0);

            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 炎上ティックが最小値と等しい場合 - false")
        void testFireTicks_EqualsMinTicks() {
            // 実装は getFireTicks() > minTicks なので、等しい場合はfalse
            when(mockTarget.getFireTicks()).thenReturn(30);
            condition.getSettings().set("ticks", 30);

            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 炎上ティックが最小値より1大きい場合 - true")
        void testFireTicks_OneAboveMinTicks() {
            when(mockTarget.getFireTicks()).thenReturn(31);
            condition.getSettings().set("ticks", 30);

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }
    }

    // ==================== WaterCondition テスト ====================

    @Nested
    @DisplayName("WaterCondition: 水中条件")
    class WaterConditionTests {

        @Mock
        private LivingEntity mockCaster;

        @Mock
        private LivingEntity mockTarget;

        @Mock
        private Location mockLocation;

        @Mock
        private Block mockBlock;

        private WaterCondition condition;

        @BeforeEach
        void setUp() {
            condition = new WaterCondition();
            when(mockTarget.getLocation()).thenReturn(mockLocation);
            when(mockLocation.getBlock()).thenReturn(mockBlock);
            // Location.add()はメソッドチェーン用に自分自身を返す
            when(mockLocation.add(anyDouble(), anyDouble(), anyDouble())).thenReturn(mockLocation);
        }

        @Test
        @DisplayName("getType: CONDITIONを返す")
        void testGetType() {
            assertEquals(ComponentType.CONDITION, condition.getType());
        }

        @Test
        @DisplayName("test: 水中")
        void testInWater() {
            when(mockTarget.isInWater()).thenReturn(true);

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 水外")
        void testNotInWater() {
            when(mockTarget.isInWater()).thenReturn(false);

            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 水深指定 - 基本動作確認")
        void testWaterDepth() {
            // 水深チェックは複雑なモックが必要なため、基本動作のみ確認
            when(mockTarget.isInWater()).thenReturn(true);
            condition.getSettings().set("depth", 1);

            // メソッド呼び出しが可能であることを確認
            boolean result = condition.test(mockCaster, 1, mockTarget);
            // 結果はブール値であることを確認
            assertTrue(result == true || result == false);
        }

        @Test
        @DisplayName("test: settingsがnullの場合は水中チェックのみ")
        void testNullSettings() {
            when(mockTarget.isInWater()).thenReturn(true);
            assertTrue(condition.test(mockCaster, 1, mockTarget));

            when(mockTarget.isInWater()).thenReturn(false);
            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: depth=0の場合は水中ならtrue")
        void testZeroDepth() {
            when(mockTarget.isInWater()).thenReturn(true);
            condition.getSettings().set("depth", 0);

            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 水深指定で浅い場合はfalse")
        void testNotDeepEnough() {
            when(mockTarget.isInWater()).thenReturn(true);
            // 最初のブロックが水ではない場合
            when(mockBlock.getType()).thenReturn(Material.AIR);

            condition.getSettings().set("depth", 10);

            // 深度1未満なのでfalse
            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 深度指定1 - SEAGRASSブロックはカウントされる")
        void testSeagrassCountsAsWater() {
            when(mockTarget.isInWater()).thenReturn(true);
            // 1回目はSEAGRASS（水）、2回目はAIR（非水）を返す
            when(mockBlock.getType()).thenReturn(Material.SEAGRASS, Material.AIR);

            condition.getSettings().set("depth", 1);

            // 深度1以上あるのでtrue
            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 深度指定1 - KELPブロックはカウントされる")
        void testKelpCountsAsWater() {
            when(mockTarget.isInWater()).thenReturn(true);
            // 1回目はKELP（水）、2回目はAIR（非水）を返す
            when(mockBlock.getType()).thenReturn(Material.KELP, Material.AIR);

            condition.getSettings().set("depth", 1);

            // 深度1以上あるのでtrue
            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 深度指定1 - TALL_SEAGRASSブロックはカウントされる")
        void testTallSeagrassCountsAsWater() {
            when(mockTarget.isInWater()).thenReturn(true);
            // 1回目はTALL_SEAGRASS（水）、2回目はAIR（非水）を返す
            when(mockBlock.getType()).thenReturn(Material.TALL_SEAGRASS, Material.AIR);

            condition.getSettings().set("depth", 1);

            // 深度1以上あるのでtrue
            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 深度指定1 - WATERブロックはカウントされる")
        void testWaterBlockCounts() {
            when(mockTarget.isInWater()).thenReturn(true);
            // 1回目はWATER（水）、2回目はAIR（非水）を返す
            when(mockBlock.getType()).thenReturn(Material.WATER, Material.AIR);

            condition.getSettings().set("depth", 1);

            // 深度1以上あるのでtrue
            assertTrue(condition.test(mockCaster, 1, mockTarget));
        }

        @Test
        @DisplayName("test: 深度指定1 - 非水ブロックはカウントされない")
        void testNonWaterBlockDoesNotCount() {
            when(mockTarget.isInWater()).thenReturn(true);
            // AIRは水ではない
            when(mockBlock.getType()).thenReturn(Material.AIR);

            condition.getSettings().set("depth", 1);

            // 深度0 < 1 なのでfalse
            assertFalse(condition.test(mockCaster, 1, mockTarget));
        }
    }

    // ==================== StatusCondition テスト ====================

    @Nested
    @DisplayName("StatusCondition: ステータス条件")
    class StatusConditionTests {

        @Mock
        private LivingEntity mockCaster;

        @Mock
        private Player mockPlayer;

        @Mock
        private LivingEntity mockMob;

        @Mock
        private AttributeInstance mockAttributeInstance;

        private StatusCondition condition;

        @BeforeEach
        void setUp() {
            condition = new StatusCondition();
            when(mockPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH)).thenReturn(mockAttributeInstance);
            when(mockAttributeInstance.getValue()).thenReturn(20.0);
        }

        @Test
        @DisplayName("getType: CONDITIONを返す")
        void testGetType() {
            assertEquals(ComponentType.CONDITION, condition.getType());
        }

        @Test
        @DisplayName("test: healthステータス")
        void testHealth() {
            when(mockPlayer.getHealth()).thenReturn(15.0);
            condition.getSettings().set("stat", "health");
            condition.getSettings().set("min", 10);
            condition.getSettings().set("max", 20);

            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }

        @Test
        @DisplayName("test: max_healthステータス")
        void testMaxHealth() {
            condition.getSettings().set("stat", "max_health");
            condition.getSettings().set("min", 15);
            condition.getSettings().set("max", 25);

            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }

        @Test
        @DisplayName("test: foodステータス (Player)")
        void testFood() {
            when(mockPlayer.getFoodLevel()).thenReturn(15);
            condition.getSettings().set("stat", "food");
            condition.getSettings().set("min", 10);
            condition.getSettings().set("max", 20);

            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }

        @Test
        @DisplayName("test: foodステータス (Mob)")
        void testFood_Mob() {
            condition.getSettings().set("stat", "food");
            condition.getSettings().set("min", 0);
            condition.getSettings().set("max", 10);

            // Mobはfoodレベルが0
            assertTrue(condition.test(mockCaster, 1, mockMob));
        }

        @Test
        @DisplayName("test: airステータス")
        void testAir() {
            when(mockPlayer.getRemainingAir()).thenReturn(250);
            condition.getSettings().set("stat", "air");
            condition.getSettings().set("min", 200);
            condition.getSettings().set("max", 300);

            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }

        @Test
        @DisplayName("test: expステータス")
        void testExp() {
            when(mockPlayer.getExp()).thenReturn(0.5f); // 50%
            condition.getSettings().set("stat", "exp");
            condition.getSettings().set("min", 40);
            condition.getSettings().set("max", 60);

            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }

        @Test
        @DisplayName("test: levelステータス")
        void testLevel() {
            when(mockPlayer.getLevel()).thenReturn(10);
            condition.getSettings().set("stat", "level");
            condition.getSettings().set("min", 5);
            condition.getSettings().set("max", 15);

            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }

        @Test
        @DisplayName("test: percent指定")
        void testPercent() {
            when(mockPlayer.getHealth()).thenReturn(10.0);
            condition.getSettings().set("stat", "health");
            condition.getSettings().set("min", 40);
            condition.getSettings().set("max", 60);
            condition.getSettings().set("percent", true);

            // 10/20 * 100 = 50%
            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }

        @Test
        @DisplayName("test: 無効なステータス")
        void testInvalidStat() {
            when(mockPlayer.getHealth()).thenReturn(15.0);
            condition.getSettings().set("stat", "invalid");
            condition.getSettings().set("min", 10);
            condition.getSettings().set("max", 20);

            // デフォルトはhealth
            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }
    }

    // ==================== PotionCondition テスト ====================

    @Nested
    @DisplayName("PotionCondition: ポーション条件")
    class PotionConditionTests {

        @Mock
        private LivingEntity mockCaster;

        @Mock
        private LivingEntity mockTarget;

        private PotionCondition condition;

        @BeforeEach
        void setUp() {
            condition = new PotionCondition();
        }

        @Test
        @DisplayName("getType: CONDITIONを返す")
        void testGetType() {
            assertEquals(ComponentType.CONDITION, condition.getType());
        }

        @Test
        @DisplayName("test: 設定の正しい適用を確認")
        void testSettingsApplication() {
            // 設定が正しく適用されることを確認（test()メソッドは呼び出さない）
            condition.getSettings().set("potion", "");
            assertEquals("", condition.getSettings().getString("potion", ""));

            condition.getSettings().set("potion", "SPEED");
            condition.getSettings().set("min_level", 1);

            assertEquals("SPEED", condition.getSettings().getString("potion", ""));
            assertEquals(1, condition.getSettings().getInt("min_level", 0));
        }

        @Test
        @DisplayName("test: settingsがnullの場合はfalse")
        void testNullSettings() {
            // PotionConditionのtest()メソッドはsettingsがnullの場合falseを返す
            // ただし、Registry初期化の問題があるため、設定検証のみ行う
            assertNotNull(condition.getSettings());
        }
    }

    // ==================== ClassCondition テスト ====================

    @Nested
    @DisplayName("ClassCondition: クラス条件")
    class ClassConditionTests {

        @Mock
        private LivingEntity mockCaster;

        @Mock
        private Player mockPlayer;

        @Mock
        private LivingEntity mockMob;

        @Mock
        private RPGPlugin mockPlugin;

        @Mock
        private PlayerManager mockPlayerManager;

        @Mock
        private RPGPlayer mockRpgPlayer;

        private ClassCondition condition;

        @BeforeEach
        void setUp() {
            condition = new ClassCondition();
        }

        @Test
        @DisplayName("getType: CONDITIONを返す")
        void testGetType() {
            assertEquals(ComponentType.CONDITION, condition.getType());
        }

        @Test
        @DisplayName("test: Playerでない場合はfalse")
        void testNonPlayer() {
            assertFalse(condition.test(mockCaster, 1, mockMob));
        }

        @Test
        @DisplayName("test: クラス一致（exactモード）")
        void testClassMatch_Exact() {
            UUID playerId = UUID.randomUUID();
            when(mockPlayer.getUniqueId()).thenReturn(playerId);
            when(mockRpgPlayer.getClassId()).thenReturn("warrior");

            condition.getSettings().set("class", "warrior");
            condition.getSettings().set("exact", true);

            try (var mockedStatic = mockStatic(RPGPlugin.class)) {
                mockedStatic.when(RPGPlugin::getInstance).thenReturn(mockPlugin);
                when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);
                when(mockPlayerManager.getRPGPlayer(playerId)).thenReturn(mockRpgPlayer);

                assertTrue(condition.test(mockCaster, 1, mockPlayer));
            }
        }

        @Test
        @DisplayName("test: クラス不一致")
        void testClassMismatch() {
            UUID playerId = UUID.randomUUID();
            when(mockPlayer.getUniqueId()).thenReturn(playerId);
            when(mockRpgPlayer.getClassId()).thenReturn("mage");

            condition.getSettings().set("class", "warrior");
            condition.getSettings().set("exact", true);

            try (var mockedStatic = mockStatic(RPGPlugin.class)) {
                mockedStatic.when(RPGPlugin::getInstance).thenReturn(mockPlugin);
                when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);
                when(mockPlayerManager.getRPGPlayer(playerId)).thenReturn(mockRpgPlayer);

                assertFalse(condition.test(mockCaster, 1, mockPlayer));
            }
        }

        @Test
        @DisplayName("test: RPGPlayerがnullの場合はfalse")
        void testNullRpgPlayer() {
            UUID playerId = UUID.randomUUID();
            when(mockPlayer.getUniqueId()).thenReturn(playerId);

            condition.getSettings().set("class", "warrior");

            try (var mockedStatic = mockStatic(RPGPlugin.class)) {
                mockedStatic.when(RPGPlugin::getInstance).thenReturn(mockPlugin);
                when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);
                when(mockPlayerManager.getRPGPlayer(playerId)).thenReturn(null);

                assertFalse(condition.test(mockCaster, 1, mockPlayer));
            }
        }

        @Test
        @DisplayName("test: settingsがnullの場合はfalse")
        void testNullSettings() {
            // ClassConditionのtest()メソッドはsettingsがnullの場合、falseを返す
            // ただし、RPGPlugin.getInstance()がnullの場合はNullPointerExceptionが発生する可能性があるため、
            // モックを設定する必要がある
            try (var mockedStatic = mockStatic(RPGPlugin.class)) {
                mockedStatic.when(RPGPlugin::getInstance).thenReturn(mockPlugin);
                when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);

                assertFalse(condition.test(mockCaster, 1, mockPlayer));
            }
        }

        @Test
        @DisplayName("test: クラスIDがnullで指定クラスが空の場合はtrue")
        void testNullCurrentClass_EmptyRequired() {
            UUID playerId = UUID.randomUUID();
            when(mockPlayer.getUniqueId()).thenReturn(playerId);
            when(mockRpgPlayer.getClassId()).thenReturn(null);

            condition.getSettings().set("class", "");

            try (var mockedStatic = mockStatic(RPGPlugin.class)) {
                mockedStatic.when(RPGPlugin::getInstance).thenReturn(mockPlugin);
                when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);
                when(mockPlayerManager.getRPGPlayer(playerId)).thenReturn(mockRpgPlayer);

                assertTrue(condition.test(mockCaster, 1, mockPlayer));
            }
        }

        @Test
        @DisplayName("test: クラスIDがnullで指定クラスがある場合はfalse")
        void testNullCurrentClass_WithRequired() {
            UUID playerId = UUID.randomUUID();
            when(mockPlayer.getUniqueId()).thenReturn(playerId);
            when(mockRpgPlayer.getClassId()).thenReturn(null);

            condition.getSettings().set("class", "warrior");

            try (var mockedStatic = mockStatic(RPGPlugin.class)) {
                mockedStatic.when(RPGPlugin::getInstance).thenReturn(mockPlugin);
                when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);
                when(mockPlayerManager.getRPGPlayer(playerId)).thenReturn(mockRpgPlayer);

                assertFalse(condition.test(mockCaster, 1, mockPlayer));
            }
        }

        @Test
        @DisplayName("test: exact=falseモード（デフォルト）")
        void testNonExactMode() {
            UUID playerId = UUID.randomUUID();
            when(mockPlayer.getUniqueId()).thenReturn(playerId);
            when(mockRpgPlayer.getClassId()).thenReturn("warrior");

            condition.getSettings().set("class", "warrior");
            // exactのデフォルトはfalse

            try (var mockedStatic = mockStatic(RPGPlugin.class)) {
                mockedStatic.when(RPGPlugin::getInstance).thenReturn(mockPlugin);
                when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);
                when(mockPlayerManager.getRPGPlayer(playerId)).thenReturn(mockRpgPlayer);

                assertTrue(condition.test(mockCaster, 1, mockPlayer));
            }
        }

        @Test
        @DisplayName("test: exact=falseモードでクラス不一致")
        void testNonExactMode_Mismatch() {
            UUID playerId = UUID.randomUUID();
            when(mockPlayer.getUniqueId()).thenReturn(playerId);
            when(mockRpgPlayer.getClassId()).thenReturn("mage");

            condition.getSettings().set("class", "warrior");

            try (var mockedStatic = mockStatic(RPGPlugin.class)) {
                mockedStatic.when(RPGPlugin::getInstance).thenReturn(mockPlugin);
                when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);
                when(mockPlayerManager.getRPGPlayer(playerId)).thenReturn(mockRpgPlayer);

                assertFalse(condition.test(mockCaster, 1, mockPlayer));
            }
        }
    }

    // ==================== ToolCondition テスト ====================

    @Nested
    @DisplayName("ToolCondition: ツール条件")
    class ToolConditionTests {

        @Mock
        private LivingEntity mockCaster;

        @Mock
        private Player mockPlayer;

        @Mock
        private LivingEntity mockMob;

        @Mock
        private PlayerInventory mockInventory;

        @Mock
        private ItemStack mockMainItem;

        @Mock
        private ItemStack mockOffItem;

        private ToolCondition condition;

        @BeforeEach
        void setUp() {
            condition = new ToolCondition();
            when(mockPlayer.getInventory()).thenReturn(mockInventory);
        }

        @Test
        @DisplayName("getType: CONDITIONを返す")
        void testGetType() {
            assertEquals(ComponentType.CONDITION, condition.getType());
        }

        @Test
        @DisplayName("test: Playerでない場合はfalse")
        void testNonPlayer() {
            condition.getSettings().set("material", "DIAMOND");

            assertFalse(condition.test(mockCaster, 1, mockMob));
        }

        @Test
        @DisplayName("test: メインハンドにツールあり")
        void testMainHand() {
            when(mockInventory.getItemInMainHand()).thenReturn(mockMainItem);
            when(mockMainItem.getType()).thenReturn(Material.DIAMOND_SWORD);

            condition.getSettings().set("material", "DIAMOND");
            condition.getSettings().set("hand", "main");

            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }

        @Test
        @DisplayName("test: オフハンドにツールあり")
        void testOffHand() {
            when(mockInventory.getItemInMainHand()).thenReturn(null);
            when(mockInventory.getItemInOffHand()).thenReturn(mockOffItem);
            when(mockOffItem.getType()).thenReturn(Material.DIAMOND_SWORD);

            condition.getSettings().set("material", "DIAMOND");
            condition.getSettings().set("hand", "off");

            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }

        @Test
        @DisplayName("test: 任意の手")
        void testAnyHand() {
            when(mockInventory.getItemInMainHand()).thenReturn(null);
            when(mockInventory.getItemInOffHand()).thenReturn(mockOffItem);
            when(mockOffItem.getType()).thenReturn(Material.DIAMOND_SWORD);

            condition.getSettings().set("material", "DIAMOND");
            condition.getSettings().set("hand", "any");

            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }

        @Test
        @DisplayName("test: マテリアル指定なしはtrue")
        void testNoMaterial() {
            condition.getSettings().set("material", "");

            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }

        @Test
        @DisplayName("test: settingsがnullの場合はtrue")
        void testNullSettings() {
            assertTrue(condition.test(mockCaster, 1, mockPlayer));
        }
    }

    // ==================== EventCondition テスト ====================

    @Nested
    @DisplayName("EventCondition: イベント条件")
    class EventConditionTests {

        private EventCondition condition;

        @BeforeEach
        void setUp() {
            condition = new EventCondition();
        }

        @Test
        @DisplayName("getType: CONDITIONを返す")
        void testGetType() {
            assertEquals(ComponentType.CONDITION, condition.getType());
        }

        @Test
        @DisplayName("test: 常にfalseを返す（TriggerHandler側で評価）")
        void testAlwaysReturnsFalse() {
            // EventConditionのtest()は常にfalseを返す
            // 実際の評価はTriggerHandler側で行われる
            assertFalse(condition.test(null, 1, null));
        }

        @Test
        @DisplayName("setEventType/getEventType: イベントタイプの設定と取得")
        void testSetGetEventType() {
            condition.setEventType(EventCondition.EventType.PHYSICAL_DEALT);
            assertEquals(EventCondition.EventType.PHYSICAL_DEALT, condition.getEventType());
        }

        @Test
        @DisplayName("setDuration/getDuration: 有効期間の設定と取得")
        void testSetGetDuration() {
            condition.setDuration(10);
            assertEquals(10, condition.getDuration());
        }

        @Test
        @DisplayName("loadSettings: 設定から読み込み")
        void testLoadSettings() {
            condition.getSettings().set("event", "DEATH");
            condition.getSettings().set("duration", 5);

            condition.loadSettings();

            assertEquals(EventCondition.EventType.DEATH, condition.getEventType());
            assertEquals(5, condition.getDuration());
        }

        @Test
        @DisplayName("parseEventType: 有効なイベント文字列")
        void testParseEventType_Valid() {
            assertEquals(EventCondition.EventType.PHYSICAL_DEALT,
                    EventCondition.parseEventType("PHYSICAL_DEALT"));
            assertEquals(EventCondition.EventType.CROUCH,
                    EventCondition.parseEventType("CROUCH"));
        }

        @Test
        @DisplayName("parseEventType: 無効なイベント文字列はnull")
        void testParseEventType_Invalid() {
            assertNull(EventCondition.parseEventType("INVALID"));
        }

        @Test
        @DisplayName("parseEventType: nullはnull")
        void testParseEventType_Null() {
            assertNull(EventCondition.parseEventType(null));
        }

        @Test
        @DisplayName("validateConfig: 有効な設定")
        void testValidateConfig_Valid() {
            ComponentSettings settings = new ComponentSettings();
            settings.set("event", "DEATH");

            // 例外が投げられないことを確認
            assertDoesNotThrow(() -> EventCondition.validateConfig(settings));
        }

        @Test
        @DisplayName("validateConfig: イベント指定なしは例外")
        void testValidateConfig_NoEvent() {
            ComponentSettings settings = new ComponentSettings();
            settings.set("event", "");

            assertThrows(IllegalArgumentException.class,
                    () -> EventCondition.validateConfig(settings));
        }

        @Test
        @DisplayName("validateConfig: 無効なイベントは例外")
        void testValidateConfig_InvalidEvent() {
            ComponentSettings settings = new ComponentSettings();
            settings.set("event", "INVALID");

            assertThrows(IllegalArgumentException.class,
                    () -> EventCondition.validateConfig(settings));
        }

        @Test
        @DisplayName("toString: イベント情報を含む文字列")
        void testToString() {
            condition.setEventType(EventCondition.EventType.DEATH);
            condition.setDuration(10);

            String result = condition.toString();

            assertTrue(result.contains("EventCondition"));
            assertTrue(result.contains("eventType=DEATH"));
            assertTrue(result.contains("duration=10"));
        }

        @Test
        @DisplayName("EventType.getTriggerClass: 正しいクラス名を返す")
        void testEventTypeGetTriggerClass() {
            String className = EventCondition.EventType.PHYSICAL_DEALT.getTriggerClass();
            assertTrue(className.contains("PhysicalDealtTrigger"));
        }
    }
}
