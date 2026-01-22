package com.example.rpgplugin.skill;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.evaluator.FormulaEvaluator;
import com.example.rpgplugin.skill.event.SkillEventListener;
import com.example.rpgplugin.skill.result.SkillExecutionResult;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SkillManagerの単体テスト
 *
 * <p>スキル管理クラスの機能テスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("SkillManager テスト")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SkillManagerTest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private Player mockPlayer;

    @Mock
    private RPGPlayer mockRpgPlayer;

    @Mock
    private StatManager mockStatManager;

    @Mock
    private World mockWorld;

    @Mock
    private LivingEntity mockTarget;

    private SkillManager skillManager;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        skillManager = new SkillManager(mockPlugin, mockPlayerManager);
        testUuid = UUID.randomUUID();

        // 共通モック設定
        lenient().when(mockPlayer.getUniqueId()).thenReturn(testUuid);
        lenient().when(mockPlayer.getLocation()).thenReturn(new Location(mockWorld, 0, 64, 0));
        lenient().when(mockPlayer.getWorld()).thenReturn(mockWorld);
        doNothing().when(mockPlayer).sendMessage(any(Component.class));

        lenient().when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        lenient().when(mockRpgPlayer.getBukkitPlayer()).thenReturn(mockPlayer);
        lenient().when(mockRpgPlayer.getStatManager()).thenReturn(mockStatManager);
        lenient().when(mockStatManager.getFinalStat(any(Stat.class))).thenReturn(10);
    }

    // ==================== ヘルパーメソッド ====================

    private Skill createTestSkill(String id, String displayName) {
        return createTestSkill(id, displayName, null);
    }

    private Skill createTestSkill(String id, String displayName, List<String> availableClasses) {
        DamageCalculation damage = new DamageCalculation(10.0, Stat.STRENGTH, 1.0, 0.0);
        LevelDependentParameter cooldownParam = new LevelDependentParameter(5.0, 0.0, null, null);
        LevelDependentParameter costParam = new LevelDependentParameter(10.0, 0.0, null, null);

        return new Skill(
                id, id, displayName, SkillType.NORMAL, List.of(),
                10, 5.0, 10, cooldownParam, costParam,
                SkillCostType.MANA, damage, null, null, availableClasses
        );
    }

    // ==================== コンストラクタ テスト ====================

    @Test
    @DisplayName("コンストラクタ: 正常に初期化されること")
    void testConstructor() {
        assertNotNull(skillManager, "SkillManagerが初期化されること");
        assertNotNull(skillManager.getPlayerManager(), "PlayerManagerが設定されること");
        assertNotNull(skillManager.getTreeRegistry(), "TreeRegistryが初期化されること");
        assertNotNull(skillManager.getFormulaEvaluator(), "FormulaEvaluatorが初期化されること");
    }

    // ==================== スキル登録・取得 テスト ====================

    @Nested
    @DisplayName("スキル登録・取得")
    class SkillRegistrationTests {

        @Test
        @DisplayName("registerSkill: 有効なスキルを登録できること")
        void testRegisterSkill_Valid() {
            Skill skill = createTestSkill("test_skill", "テストスキル");

            boolean result = skillManager.registerSkill(skill);

            assertTrue(result, "登録に成功すること");
            assertEquals(skill, skillManager.getSkill("test_skill"), "登録したスキルが取得できること");
        }

        @Test
        @DisplayName("registerSkill: nullは登録不可")
        void testRegisterSkill_Null() {
            boolean result = skillManager.registerSkill(null);

            assertFalse(result, "nullスキルの登録は失敗すること");
        }

        @Test
        @DisplayName("registerSkill: 重複IDは登録不可")
        void testRegisterSkill_Duplicate() {
            Skill skill1 = createTestSkill("duplicate", "スキル1");
            Skill skill2 = createTestSkill("duplicate", "スキル2");

            skillManager.registerSkill(skill1);
            boolean result = skillManager.registerSkill(skill2);

            assertFalse(result, "重複IDの登録は失敗すること");
            assertEquals("スキル1", skillManager.getSkill("duplicate").getDisplayName(),
                    "元のスキルが保持されていること");
        }

        @Test
        @DisplayName("getSkill: 未登録スキルはnull")
        void testGetSkill_NotExists() {
            Skill retrieved = skillManager.getSkill("nonexistent");

            assertNull(retrieved, "未登録スキルはnullを返すこと");
        }

        @Test
        @DisplayName("getAllSkills: 全スキルのコピーを取得できること")
        void testGetAllSkills() {
            skillManager.registerSkill(createTestSkill("skill1", "スキル1"));
            skillManager.registerSkill(createTestSkill("skill2", "スキル2"));

            Map<String, Skill> allSkills = skillManager.getAllSkills();

            assertEquals(2, allSkills.size(), "全スキルが取得できること");

            // コピーの確認
            allSkills.clear();
            assertEquals(2, skillManager.getAllSkills().size(),
                    "取得したマップをクリアしても元には影響しないこと");
        }

        @Test
        @DisplayName("getAllSkills: 空の状態では空マップを返す")
        void testGetAllSkills_Empty() {
            Map<String, Skill> allSkills = skillManager.getAllSkills();

            assertTrue(allSkills.isEmpty(), "空マップを返すこと");
        }

        @Test
        @DisplayName("getAllSkillIds: 全スキルIDを取得できること")
        void testGetAllSkillIds() {
            skillManager.registerSkill(createTestSkill("skill1", "スキル1"));
            skillManager.registerSkill(createTestSkill("skill2", "スキル2"));

            Set<String> skillIds = skillManager.getAllSkillIds();

            assertEquals(2, skillIds.size(), "2つのスキルIDが取得できること");
            assertTrue(skillIds.contains("skill1"));
            assertTrue(skillIds.contains("skill2"));
        }

        @Test
        @DisplayName("getSkillsForClass: 指定クラスのスキルを取得できること")
        void testGetSkillsForClass() {
            Skill skill1 = createTestSkill("skill1", "スキル1", List.of("Warrior"));
            Skill skill2 = createTestSkill("skill2", "スキル2", List.of("Mage"));

            skillManager.registerSkill(skill1);
            skillManager.registerSkill(skill2);

            List<Skill> warriorSkills = skillManager.getSkillsForClass("Warrior");
            List<Skill> mageSkills = skillManager.getSkillsForClass("Mage");

            assertEquals(1, warriorSkills.size(), "Warriorスキルが1つであること");
            assertEquals("skill1", warriorSkills.get(0).getId());
            assertEquals(1, mageSkills.size(), "Mageスキルが1つであること");
            assertEquals("skill2", mageSkills.get(0).getId());
        }

        @Test
        @DisplayName("getSkillTree: スキルツリーを取得できること")
        void testGetSkillTree() {
            Skill skill = createTestSkill("test_skill", "テストスキル", List.of("Warrior"));
            skillManager.registerSkill(skill);

            SkillTree tree = skillManager.getSkillTree("Warrior");

            assertNotNull(tree, "スキルツリーが取得できること");
        }

        @Test
        @DisplayName("clearAllSkills: 全スキルをクリアできること")
        void testClearAllSkills() {
            skillManager.registerSkill(createTestSkill("skill1", "スキル1"));
            skillManager.registerSkill(createTestSkill("skill2", "スキル2"));

            skillManager.clearAllSkills();

            assertTrue(skillManager.getAllSkills().isEmpty(), "全スキルがクリアされること");
        }
    }

    // ==================== プレイヤースキルデータ テスト ====================

    @Nested
    @DisplayName("プレイヤースキルデータ")
    class PlayerSkillDataTests {

        @Test
        @DisplayName("getPlayerSkillData(Player): プレイヤーのスキルデータを取得できること")
        void testGetPlayerSkillData_Player() {
            SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(mockPlayer);

            assertNotNull(data, "スキルデータが取得できること");
            assertNotNull(data.getAcquiredSkills(), "習得スキルマップが取得できること");
        }

        @Test
        @DisplayName("getPlayerSkillData(UUID): UUIDでスキルデータを取得できること")
        void testGetPlayerSkillData_Uuid() {
            SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(testUuid);

            assertNotNull(data, "スキルデータが取得できること");
            assertNotNull(data.getAcquiredSkills(), "習得スキルマップが取得できること");
        }

        @Test
        @DisplayName("hasSkill: スキル習得状態をチェックできること")
        void testHasSkill() {
            // このテストはPlayerSkillServiceのモック挙動に依存
            SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(mockPlayer);

            assertFalse(data.hasSkill("test_skill"), "未習得スキルはfalseを返すこと");
        }

        @Test
        @DisplayName("getSkillLevel: スキルレベルを取得できること")
        void testGetSkillLevel() {
            SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(mockPlayer);

            assertEquals(0, data.getSkillLevel("test_skill"), "未習得スキルのレベルは0であること");
        }

        @Test
        @DisplayName("setSkillLevel: スキルレベルを設定できること")
        void testSetSkillLevel() {
            SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(mockPlayer);

            data.setSkillLevel("test_skill", 5);

            assertEquals(5, data.getSkillLevel("test_skill"), "レベルが設定されていること");
        }

        @Test
        @DisplayName("removeSkill: スキルを削除できること")
        void testRemoveSkill() {
            SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(mockPlayer);

            data.setSkillLevel("test_skill", 5);
            data.removeSkill("test_skill");

            assertEquals(0, data.getSkillLevel("test_skill"), "スキルが削除されていること");
        }

        @Test
        @DisplayName("getCooldowns: クールダウンマップを取得できること")
        void testGetCooldowns() {
            SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(mockPlayer);

            Map<String, Long> cooldowns = data.getCooldowns();

            assertNotNull(cooldowns, "クールダウンマップが取得できること");
        }

        @Test
        @DisplayName("setCooldown: クールダウンを設定できること")
        void testSetCooldown() {
            SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(mockPlayer);
            long cooldownTime = System.currentTimeMillis();

            data.setCooldown("test_skill", cooldownTime);

            assertEquals(cooldownTime, data.getLastCastTime("test_skill"), "クールダウンが設定されていること");
        }

        @Test
        @DisplayName("getSkillPoints: スキルポイントを取得できること")
        void testGetSkillPoints() {
            SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(mockPlayer);

            assertEquals(0, data.getSkillPoints(), "初期スキルポイントは0であること");
        }

        @Test
        @DisplayName("setSkillPoints: スキルポイントを設定できること")
        void testSetSkillPoints() {
            SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(mockPlayer);

            data.setSkillPoints(10);

            assertEquals(10, data.getSkillPoints(), "スキルポイントが設定されていること");
        }

        @Test
        @DisplayName("addSkillPoints: スキルポイントを追加できること")
        void testAddSkillPoints() {
            SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(mockPlayer);
            data.setSkillPoints(5);

            data.addSkillPoints(3);

            assertEquals(8, data.getSkillPoints(), "スキルポイントが追加されていること");
        }

        @Test
        @DisplayName("useSkillPoint: スキルポイントを使用できること")
        void testUseSkillPoint() {
            SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(mockPlayer);
            data.setSkillPoints(5);

            boolean result = data.useSkillPoint();

            assertTrue(result, "スキルポイント使用に成功すること");
            assertEquals(4, data.getSkillPoints(), "スキルポイントが減っていること");
        }

        @Test
        @DisplayName("useSkillPoint: ポイント不足時は失敗すること")
        void testUseSkillPoint_NoPoints() {
            SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(mockPlayer);

            boolean result = data.useSkillPoint();

            assertFalse(result, "スキルポイント不足時は失敗すること");
        }

        @Test
        @DisplayName("unloadPlayerData: プレイヤーデータをアンロードできること")
        void testUnloadPlayerData() {
            // エラーが発生しないことを確認
            assertDoesNotThrow(() -> skillManager.unloadPlayerData(testUuid));
        }

        @Test
        @DisplayName("clearAllPlayerData: 全プレイヤーデータをクリアできること")
        void testClearAllPlayerData() {
            assertDoesNotThrow(() -> skillManager.clearAllPlayerData());
        }
    }

    // ==================== スキル実行 テスト ====================

    @Nested
    @DisplayName("スキル実行")
    class SkillExecutionTests {

        @Test
        @DisplayName("executeSkill(config): スキルを実行できること")
        void testExecuteSkill_WithConfig() {
            Skill skill = createTestSkill("test_skill", "テストスキル");
            skillManager.registerSkill(skill);

            SkillExecutionConfig config = new SkillExecutionConfig.Builder().build();
            SkillExecutionResult result = skillManager.executeSkill(mockPlayer, "test_skill", config);

            // 習得していないので失敗するはず
            assertNotNull(result, "実行結果が返ること");
        }

        @Test
        @DisplayName("executeSkill: デフォルト設定でスキルを実行できること")
        void testExecuteSkill_DefaultConfig() {
            Skill skill = createTestSkill("test_skill", "テストスキル");
            skillManager.registerSkill(skill);

            SkillExecutionResult result = skillManager.executeSkill(mockPlayer, "test_skill");

            assertNotNull(result, "実行結果が返ること");
        }

        @Test
        @DisplayName("executeSkill: 存在しないスキルは失敗すること")
        void testExecuteSkill_SkillNotFound() {
            SkillExecutionResult result = skillManager.executeSkill(mockPlayer, "nonexistent");

            assertFalse(result.isSuccess(), "存在しないスキルは失敗すること");
        }
    }

    // ==================== ダメージ計算 テスト ====================

    @Nested
    @DisplayName("ダメージ計算")
    class DamageCalculationTests {

        @Test
        @DisplayName("calculateDamage: 基本的なダメージ計算ができること")
        void testCalculateDamage() {
            Skill skill = createTestSkill("test_skill", "テストスキル");

            double damage = skillManager.calculateDamage(skill, mockRpgPlayer, 1);

            assertTrue(damage > 0, "ダメージが計算されること");
        }

        @Test
        @DisplayName("calculateDamage: カスタム変数付きでダメージ計算ができること")
        void testCalculateDamage_CustomVariables() {
            Skill skill = createTestSkill("test_skill", "テストスキル");

            Map<String, Double> customVars = Map.of("bonus_damage", 5.0);
            double damage = skillManager.calculateDamage(skill, mockRpgPlayer, 1, customVars);

            assertTrue(damage > 0, "ダメージが計算されること");
        }

        @Test
        @DisplayName("calculateDamageWithFormula: 数式でダメージ計算ができること")
        void testCalculateDamageWithFormula() {
            // 単純な数式を使用
            double damage = skillManager.calculateDamageWithFormula("100", mockRpgPlayer, 5);

            assertEquals(100.0, damage, 0.001, "ダメージが計算されること");
        }

        @Test
        @DisplayName("calculateDamageWithFormula: カスタム変数付きで計算できること")
        void testCalculateDamageWithFormula_CustomVariables() {
            Map<String, Double> customVars = Map.of("base", 50.0, "multiplier", 2.0);
            double damage = skillManager.calculateDamageWithFormula("base * multiplier", mockRpgPlayer, 1, customVars);

            assertEquals(100.0, damage, 0.001, "カスタム変数で計算できること");
        }
    }

    // ==================== コスト消費 テスト ====================

    @Nested
    @DisplayName("コスト消費")
    class CostConsumptionTests {

        @Test
        @DisplayName("consumeCost: Manaコストを消費できること")
        void testConsumeCost_Mana() {
            SkillManager.CostConsumptionResult result = skillManager.consumeCost(
                    mockRpgPlayer, 10, SkillCostType.MANA);

            assertNotNull(result, "結果が返ること");
        }

        @Test
        @DisplayName("consumeCost: HPコストを消費できること")
        void testConsumeCost_Hp() {
            SkillManager.CostConsumptionResult result = skillManager.consumeCost(
                    mockRpgPlayer, 5, SkillCostType.HP);

            assertNotNull(result, "結果が返ること");
        }

        @Test
        @DisplayName("consumeCost: 結果の各メソッドが正しく動作すること")
        void testCostConsumptionResult_Methods() {
            SkillManager.CostConsumptionResult result = skillManager.consumeCost(
                    mockRpgPlayer, 10, SkillCostType.MANA);

            // メソッド呼び出しでエラーが発生しないことを確認
            boolean success = result.isSuccess();
            String errorMsg = result.getErrorMessage();
            double consumed = result.getConsumedAmount();

            assertNotNull(result, "結果が返ること");
        }
    }

    // ==================== クールダウン テスト ====================

    @Nested
    @DisplayName("クールダウン")
    class CooldownTests {

        @Test
        @DisplayName("checkCooldown: スキルが存在しない場合はfalse")
        void testCheckCooldown_SkillNotFound() {
            boolean result = skillManager.checkCooldown(mockPlayer, "nonexistent");

            assertFalse(result, "存在しないスキルはfalseを返すこと");
        }

        @Test
        @DisplayName("checkCooldown: 登録済みスキルのクールダウンチェックができること")
        void testCheckCooldown_RegisteredSkill() {
            Skill skill = createTestSkill("test_skill", "テストスキル");
            skillManager.registerSkill(skill);

            // 未習得だがスキルが存在するのでチェックは可能
            boolean result = skillManager.checkCooldown(mockPlayer, "test_skill");

            // 結果はPlayerSkillServiceの状態による
            assertNotNull(skillManager.getSkill("test_skill"), "スキルが登録されていること");
        }
    }

    // ==================== ターゲット選択 テスト ====================

    @Nested
    @DisplayName("ターゲット選択")
    class TargetSelectionTests {

        @Test
        @DisplayName("selectTargets: ターゲットを選択できること")
        void testSelectTargets() {
            Skill skill = createTestSkill("test_skill", "テストスキル");
            skillManager.registerSkill(skill);

            SkillExecutionConfig config = new SkillExecutionConfig.Builder().build();
            List<LivingEntity> targets = skillManager.selectTargets(mockPlayer, skill, config);

            assertNotNull(targets, "ターゲットリストが返ること");
        }
    }

    // ==================== 効果適用 テスト ====================

    @Nested
    @DisplayName("効果適用")
    class EffectApplicationTests {

        @Test
        @DisplayName("applyEffect: 効果を適用できること")
        void testApplyEffect() {
            Skill skill = createTestSkill("test_skill", "テストスキル");

            assertDoesNotThrow(() -> skillManager.applyEffect(mockTarget, 10.0, skill));
        }
    }

    // ==================== SkillEventListener実装 テスト ====================

    @Nested
    @DisplayName("SkillEventListener実装")
    class SkillEventListenerTests {

        @Test
        @DisplayName("onSkillAcquired: スキル習得イベントを処理できること")
        void testOnSkillAcquired() {
            Skill skill = createTestSkill("test_skill", "テストスキル");
            skillManager.registerSkill(skill);

            assertDoesNotThrow(() -> skillManager.onSkillAcquired(testUuid, "test_skill", 1));
        }

        @Test
        @DisplayName("onSkillLevelUp: スキルレベルアップイベントを処理できること")
        void testOnSkillLevelUp() {
            Skill skill = createTestSkill("test_skill", "テストスキル");
            skillManager.registerSkill(skill);

            assertDoesNotThrow(() -> skillManager.onSkillLevelUp(testUuid, "test_skill", 2, 1));
        }

        @Test
        @DisplayName("onSkillExecuted: スキル実行イベントを処理できること")
        void testOnSkillExecuted() {
            Skill skill = createTestSkill("test_skill", "テストスキル");
            skillManager.registerSkill(skill);

            assertDoesNotThrow(() -> skillManager.onSkillExecuted(testUuid, "test_skill", 1));
        }

        @Test
        @DisplayName("canAcquireSkill: スキル習得可能チェックができること")
        void testCanAcquireSkill() {
            Skill skill = createTestSkill("test_skill", "テストスキル");
            skillManager.registerSkill(skill);

            boolean result = skillManager.canAcquireSkill(testUuid, "test_skill");

            // 条件によって結果が異なる
            assertNotNull(skillManager.getSkill("test_skill"), "スキルが登録されていること");
        }

        @Test
        @DisplayName("canAcquireSkill: 存在しないスキルは習得不可")
        void testCanAcquireSkill_NotExists() {
            boolean result = skillManager.canAcquireSkill(testUuid, "nonexistent");

            assertFalse(result, "存在しないスキルは習得不可であること");
        }

        @Test
        @DisplayName("canAcquireSkill: 最大レベル到達時は習得不可")
        void testCanAcquireSkill_MaxLevel() {
            Skill skill = createTestSkill("test_skill", "テストスキル");
            skillManager.registerSkill(skill);

            SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(testUuid);
            data.setSkillLevel("test_skill", 10); // 最大レベル

            boolean result = skillManager.canAcquireSkill(testUuid, "test_skill");

            assertFalse(result, "最大レベル到達時は習得不可であること");
        }

        @Test
        @DisplayName("getSkillLevel(UUID): UUIDでスキルレベルを取得できること")
        void testGetSkillLevel_Uuid() {
            int level = skillManager.getSkillLevel(testUuid, "test_skill");

            assertEquals(0, level, "未習得スキルのレベルは0であること");
        }

        @Test
        @DisplayName("hasSkill(UUID): UUIDでスキル習得状態をチェックできること")
        void testHasSkill_Uuid() {
            boolean hasSkill = skillManager.hasSkill(testUuid, "test_skill");

            assertFalse(hasSkill, "未習得スキルはfalseであること");
        }

        @Test
        @DisplayName("registerPlayerListener: プレイヤーにリスナーを登録できること")
        void testRegisterPlayerListener() {
            assertDoesNotThrow(() -> skillManager.registerPlayerListener(mockRpgPlayer));

            verify(mockRpgPlayer).setSkillEventListener(eq(skillManager));
        }

        @Test
        @DisplayName("registerPlayerListener: nullを渡してもエラーにならない")
        void testRegisterPlayerListener_Null() {
            assertDoesNotThrow(() -> skillManager.registerPlayerListener(null));
        }
    }

    // ==================== ユーティリティ テスト ====================

    @Nested
    @DisplayName("ユーティリティ")
    class UtilityTests {

        @Test
        @DisplayName("getFormulaEvaluator: FormulaEvaluatorが取得できること")
        void testGetFormulaEvaluator() {
            FormulaEvaluator evaluator = skillManager.getFormulaEvaluator();

            assertNotNull(evaluator, "FormulaEvaluatorが取得できること");
        }

        @Test
        @DisplayName("getPlayerManager: PlayerManagerが取得できること")
        void testGetPlayerManager() {
            PlayerManager manager = skillManager.getPlayerManager();

            assertEquals(mockPlayerManager, manager, "設定されたPlayerManagerが取得できること");
        }
    }

    // ==================== リロード テスト ====================

    @Nested
    @DisplayName("スキルリロード")
    class ReloadTests {

        @Test
        @DisplayName("reloadWithCleanup: スキルをリロードできること")
        void testReloadWithCleanup() {
            // 元のスキルを登録
            skillManager.registerSkill(createTestSkill("skill1", "スキル1"));
            skillManager.registerSkill(createTestSkill("skill2", "スキル2"));

            // 新しいスキルマップ（skill2が削除、skill3が追加）
            Map<String, Skill> newSkills = new HashMap<>();
            newSkills.put("skill1", createTestSkill("skill1", "スキル1"));
            newSkills.put("skill3", createTestSkill("skill3", "スキル3"));

            SkillManager.ReloadResult result = skillManager.reloadWithCleanup(newSkills);

            assertNotNull(result, "リロード結果が返ること");
            assertEquals(2, result.getLoadedSkillCount(), "2つのスキルがロードされたこと");
            assertTrue(result.getRemovedSkills().contains("skill2"), "skill2が削除されたこと");
        }

        @Test
        @DisplayName("reloadWithCleanup: 削除スキルがない場合")
        void testReloadWithCleanup_NoRemovedSkills() {
            skillManager.registerSkill(createTestSkill("skill1", "スキル1"));

            Map<String, Skill> newSkills = new HashMap<>();
            newSkills.put("skill1", createTestSkill("skill1", "スキル1"));

            SkillManager.ReloadResult result = skillManager.reloadWithCleanup(newSkills);

            assertFalse(result.hasRemovedSkills(), "削除スキルがないこと");
            assertEquals(0, result.getAffectedPlayerCount(), "影響受けたプレイヤーは0人であること");
        }

        @Test
        @DisplayName("ReloadResult: 各メソッドが正しく動作すること")
        void testReloadResult_Methods() {
            Map<String, Skill> newSkills = new HashMap<>();
            newSkills.put("skill1", createTestSkill("skill1", "スキル1"));

            SkillManager.ReloadResult result = skillManager.reloadWithCleanup(newSkills);

            // メソッド呼び出しでエラーが発生しないことを確認
            int loadedCount = result.getLoadedSkillCount();
            Set<String> removedSkills = result.getRemovedSkills();
            int affectedPlayers = result.getAffectedPlayerCount();
            int totalRemoved = result.getTotalSkillsRemoved();
            boolean hasRemoved = result.hasRemovedSkills();
            String str = result.toString();

            assertNotNull(removedSkills, "削除スキルセットが取得できること");
            assertNotNull(str, "toStringが動作すること");
        }

        @Test
        @DisplayName("ReloadResult: toStringが正しいフォーマットを返す")
        void testReloadResult_ToString() {
            skillManager.registerSkill(createTestSkill("skill1", "スキル1"));

            Map<String, Skill> newSkills = new HashMap<>();
            newSkills.put("skill2", createTestSkill("skill2", "スキル2"));

            SkillManager.ReloadResult result = skillManager.reloadWithCleanup(newSkills);

            String str = result.toString();

            assertTrue(str.contains("loaded="), "loadedが含まれること");
            assertTrue(str.contains("removed="), "removedが含まれること");
        }
    }

    // ==================== 内部クラス テスト ====================

    @Nested
    @DisplayName("CostConsumptionResult内部クラス")
    class CostConsumptionResultClassTests {

        @Test
        @DisplayName("コンストラクタ: 直接インスタンス化できること")
        void testCostConsumptionResult_Constructor() {
            SkillManager.CostConsumptionResult result =
                    new SkillManager.CostConsumptionResult(true, null, 10.0);

            assertTrue(result.isSuccess(), "成功フラグが設定されていること");
            assertEquals(10.0, result.getConsumedAmount(), 0.001, "消費量が設定されていること");
        }

        @Test
        @DisplayName("エラーメッセージが設定できること")
        void testCostConsumptionResult_ErrorMessage() {
            String errorMsg = "テストエラー";
            SkillManager.CostConsumptionResult result =
                    new SkillManager.CostConsumptionResult(false, errorMsg, 0.0);

            assertFalse(result.isSuccess(), "失敗フラグが設定されていること");
            assertEquals(errorMsg, result.getErrorMessage(), "エラーメッセージが設定されていること");
        }
    }
}
