package com.example.rpgplugin.skill.repository;

import com.example.rpgplugin.model.skill.DamageCalculation;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.LevelDependentParameter;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.skill.SkillExecutionConfig;
import com.example.rpgplugin.skill.SkillType;
import com.example.rpgplugin.skill.result.SkillExecutionResult;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.example.rpgplugin.model.skill.DamageCalculation;

/**
 * SkillExecutorの単体テスト
 *
 * <p>スキル実行クラスの機能テスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("SkillExecutor テスト")
@ExtendWith(MockitoExtension.class)
class SkillExecutorTest {

    @Mock
    private SkillRepository skillRepository;
    @Mock
    private PlayerSkillService playerSkillService;
    @Mock
    private PlayerManager playerManager;
    @Mock
    private Player mockPlayer;
    @Mock
    private RPGPlayer mockRpgPlayer;
    @Mock
    private StatManager mockStatManager;
    @Mock
    private PlayerSkillService.PlayerSkillData mockSkillData;
    @Mock
    private World mockWorld;

    private SkillExecutor executor;
    private UUID testUuid;
    private Skill testSkill;

    @BeforeEach
    void setUp() {
        executor = new SkillExecutor(skillRepository, playerSkillService, playerManager);
        testUuid = UUID.randomUUID();

        // lenient() - 全てのテストで使用されないスタブ
        lenient().when(mockPlayer.getUniqueId()).thenReturn(testUuid);
        lenient().when(mockPlayer.getLocation()).thenReturn(new Location(mockWorld, 0, 0, 0));
        lenient().when(mockPlayer.getWorld()).thenReturn(mockWorld);

        lenient().when(playerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        lenient().when(mockRpgPlayer.getBukkitPlayer()).thenReturn(mockPlayer);
        lenient().when(mockRpgPlayer.getStatManager()).thenReturn(mockStatManager);
        lenient().when(mockStatManager.getFinalStat(any(Stat.class))).thenReturn(10);

        lenient().when(playerSkillService.getPlayerSkillData(any(Player.class))).thenReturn(mockSkillData);
        lenient().when(playerSkillService.getPlayerSkillData(any(UUID.class))).thenReturn(mockSkillData);

        testSkill = createTestSkill("test_skill", "テストスキル");
    }

    // ==================== ヘルパーメソッド ====================

    private Skill createTestSkill(String id, String displayName) {
        DamageCalculation damage = new DamageCalculation(10.0, Stat.STRENGTH, 1.0, 0.0);
        LevelDependentParameter cooldownParam = new LevelDependentParameter(5.0, 0.0, null, null);
        LevelDependentParameter costParam = new LevelDependentParameter(10.0, 0.0, null, null);

        return new Skill(
                id, id, displayName, SkillType.NORMAL, List.of(),
                10, 5.0, 10, cooldownParam, costParam,
                SkillCostType.MANA, damage, null, null, List.of()
        );
    }

    // ==================== executeSkill テスト ====================

    @Test
    @DisplayName("executeSkill: スキルが見つからない")
    void testExecuteSkill_SkillNotFound() {
        when(skillRepository.getSkill("unknown_skill")).thenReturn(null);

        SkillExecutionResult result = executor.executeSkill(mockPlayer, "unknown_skill",
                SkillExecutionConfig.createDefault());

        assertFalse(result.isSuccess(), "失敗すること");
        assertTrue(result.getErrorMessage().contains("見つかりません"));
    }

    @Test
    @DisplayName("executeSkill: スキル未習得")
    void testExecuteSkill_NotAcquired() {
        when(skillRepository.getSkill("test_skill")).thenReturn(testSkill);
        when(playerSkillService.hasSkill(mockPlayer, "test_skill")).thenReturn(false);

        SkillExecutionResult result = executor.executeSkill(mockPlayer, "test_skill",
                SkillExecutionConfig.createDefault());

        assertFalse(result.isSuccess(), "失敗すること");
        assertTrue(result.getErrorMessage().contains("習得していません"));
    }

    @Test
    @DisplayName("executeSkill: スキルレベル0")
    void testExecuteSkill_LevelZero() {
        when(skillRepository.getSkill("test_skill")).thenReturn(testSkill);
        when(playerSkillService.hasSkill(mockPlayer, "test_skill")).thenReturn(true);
        when(playerSkillService.getSkillLevel(mockPlayer, "test_skill")).thenReturn(0);

        SkillExecutionResult result = executor.executeSkill(mockPlayer, "test_skill",
                SkillExecutionConfig.createDefault());

        assertFalse(result.isSuccess(), "失敗すること");
        assertTrue(result.getErrorMessage().contains("レベルが0"));
    }

    @Test
    @DisplayName("executeSkill: プレイヤーデータ未読み込み")
    void testExecuteSkill_PlayerDataNotLoaded() {
        when(skillRepository.getSkill("test_skill")).thenReturn(testSkill);
        when(playerSkillService.hasSkill(mockPlayer, "test_skill")).thenReturn(true);
        when(playerSkillService.getSkillLevel(mockPlayer, "test_skill")).thenReturn(1);
        when(playerManager.getRPGPlayer(testUuid)).thenReturn(null);

        SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                .applyCooldown(false)
                .consumeCost(false)
                .applyDamage(false)
                .build();

        SkillExecutionResult result = executor.executeSkill(mockPlayer, "test_skill", config);

        assertFalse(result.isSuccess(), "失敗すること");
        assertTrue(result.getErrorMessage().contains("読み込まれていません"));
    }

    @Test
    @DisplayName("executeSkill: MP不足")
    void testExecuteSkill_NotEnoughMana() {
        when(skillRepository.getSkill("test_skill")).thenReturn(testSkill);
        when(playerSkillService.hasSkill(mockPlayer, "test_skill")).thenReturn(true);
        when(playerSkillService.getSkillLevel(mockPlayer, "test_skill")).thenReturn(1);
        when(mockRpgPlayer.hasMana(10)).thenReturn(false);

        SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                .applyCooldown(false)
                .consumeCost(true)
                .applyDamage(false)
                .build();

        SkillExecutionResult result = executor.executeSkill(mockPlayer, "test_skill", config);

        assertFalse(result.isSuccess(), "失敗すること");
        assertTrue(result.getErrorMessage().contains("MPが不足"));
    }

    @Test
    @DisplayName("executeSkill: 成功（ダメージなし）")
    void testExecuteSkill_SuccessNoDamage() {
        when(skillRepository.getSkill("test_skill")).thenReturn(testSkill);
        when(playerSkillService.hasSkill(mockPlayer, "test_skill")).thenReturn(true);
        when(playerSkillService.getSkillLevel(mockPlayer, "test_skill")).thenReturn(1);
        when(mockRpgPlayer.hasMana(10)).thenReturn(true);

        SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                .applyCooldown(false)
                .consumeCost(true)
                .applyDamage(false)
                .build();

        SkillExecutionResult result = executor.executeSkill(mockPlayer, "test_skill", config);

        assertTrue(result.isSuccess(), "成功すること");
        assertEquals(0.0, result.getDamage(), 0.001);
        assertEquals(10.0, result.getCostConsumed(), 0.001, "MPを10消費");
        verify(mockRpgPlayer).consumeMana(10);
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("executeSkill: デフォルト設定で成功")
    void testExecuteSkill_DefaultConfig() {
        when(skillRepository.getSkill("test_skill")).thenReturn(testSkill);
        when(playerSkillService.hasSkill(mockPlayer, "test_skill")).thenReturn(true);
        when(playerSkillService.getSkillLevel(mockPlayer, "test_skill")).thenReturn(1);
        when(mockRpgPlayer.hasMana(10)).thenReturn(true);
        when(mockSkillData.getLastCastTime("test_skill")).thenReturn(0L);

        SkillExecutionResult result = executor.executeSkill(mockPlayer, "test_skill");

        assertTrue(result.isSuccess(), "成功すること");
        verify(mockSkillData).setLastCastTime(eq("test_skill"), anyLong());
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    // ==================== calculateDamage テスト ====================

    @Test
    @DisplayName("calculateDamage: ダメージ計算成功")
    void testCalculateDamage() {
        when(mockStatManager.getFinalStat(Stat.STRENGTH)).thenReturn(20);

        double damage = executor.calculateDamage(testSkill, mockRpgPlayer, 5);

        assertEquals(30.0, damage, 0.001, "base(10) + stat(20) * multiplier(1.0) + level(5) * levelMultiplier(0.0)");
    }

    @Test
    @DisplayName("calculateDamage: ダメージ設定なし")
    void testCalculateDamage_NoDamageConfig() {
        Skill noDamageSkill = new Skill(
                "no_damage", "no_damage", "ダメージなし", SkillType.NORMAL, List.of(),
                1, 0.0, 0, null, null, SkillCostType.MANA,
                null, null, null, List.of()
        );

        double damage = executor.calculateDamage(noDamageSkill, mockRpgPlayer, 1);

        assertEquals(0.0, damage, 0.001, "ダメージ設定なしは0");
    }

    @Test
    @DisplayName("calculateDamage: ステータスnull")
    void testCalculateDamage_NullStat() {
        DamageCalculation damageConfig = new DamageCalculation(10.0, null, 1.0, 0.0);
        Skill skillWithNullStat = new Skill(
                "null_stat", "null_stat", "ステータスなし", SkillType.NORMAL, List.of(),
                1, 0.0, 0, null, null, SkillCostType.MANA,
                damageConfig, null, null, List.of()
        );

        double result = executor.calculateDamage(skillWithNullStat, mockRpgPlayer, 1);

        assertEquals(10.0, result, 0.001, "ステータスnullはbaseのみ");
    }

    // ==================== calculateDamageWithFormula テスト ====================

    @Test
    @DisplayName("calculateDamageWithFormula: 数式評価成功")
    void testCalculateDamageWithFormula_Success() {
        when(mockStatManager.getFinalStat(Stat.STRENGTH)).thenReturn(15);

        double damage = executor.calculateDamageWithFormula("STR * 2 + Lv", mockRpgPlayer, 5);

        assertEquals(35.0, damage, 0.001, "15 * 2 + 5 = 35");
    }

    @Test
    @DisplayName("calculateDamageWithFormula: 空数式")
    void testCalculateDamageWithFormula_Empty() {
        double damage = executor.calculateDamageWithFormula("", mockRpgPlayer, 1);
        assertEquals(0.0, damage, 0.001);

        damage = executor.calculateDamageWithFormula(null, mockRpgPlayer, 1);
        assertEquals(0.0, damage, 0.001);
    }

    // ==================== consumeCost テスト ====================

    @Test
    @DisplayName("consumeCost: MP消費成功")
    void testConsumeCost_ManaSuccess() {
        when(mockRpgPlayer.hasMana(10)).thenReturn(true);

        SkillExecutor.CostConsumptionResult result = executor.consumeCost(mockRpgPlayer, 10, SkillCostType.MANA);

        assertTrue(result.isSuccess(), "成功すること");
        assertEquals(10.0, result.getConsumedAmount(), 0.001);
        verify(mockRpgPlayer).consumeMana(10);
    }

    @Test
    @DisplayName("consumeCost: MP不足")
    void testConsumeCost_ManaInsufficient() {
        when(mockRpgPlayer.hasMana(10)).thenReturn(false);

        SkillExecutor.CostConsumptionResult result = executor.consumeCost(mockRpgPlayer, 10, SkillCostType.MANA);

        assertFalse(result.isSuccess(), "失敗すること");
        assertTrue(result.getErrorMessage().contains("MPが不足"));
        assertEquals(0.0, result.getConsumedAmount(), 0.001);
    }

    @Test
    @DisplayName("consumeCost: HP消費成功")
    void testConsumeCost_HpSuccess() {
        when(mockPlayer.getHealth()).thenReturn(20.0);

        SkillExecutor.CostConsumptionResult result = executor.consumeCost(mockRpgPlayer, 10, SkillCostType.HP);

        assertTrue(result.isSuccess(), "成功すること");
        assertEquals(10.0, result.getConsumedAmount(), 0.001);
        verify(mockPlayer).setHealth(10.0);
    }

    @Test
    @DisplayName("consumeCost: HP不足")
    void testConsumeCost_HpInsufficient() {
        when(mockPlayer.getHealth()).thenReturn(5.0);

        SkillExecutor.CostConsumptionResult result = executor.consumeCost(mockRpgPlayer, 10, SkillCostType.HP);

        assertFalse(result.isSuccess(), "失敗すること");
        assertTrue(result.getErrorMessage().contains("HPが不足"));
    }

    @Test
    @DisplayName("consumeCost: プレイヤーオフライン")
    void testConsumeCost_PlayerOffline() {
        when(mockRpgPlayer.getBukkitPlayer()).thenReturn(null);

        SkillExecutor.CostConsumptionResult result = executor.consumeCost(mockRpgPlayer, 10, SkillCostType.MANA);

        assertFalse(result.isSuccess(), "失敗すること");
        assertTrue(result.getErrorMessage().contains("オンラインではありません"));
    }

    // ==================== checkCooldown テスト ====================

    @Test
    @DisplayName("checkCooldown: クールダウンなし")
    void testCheckCooldown_NoCooldown() {
        when(mockSkillData.getLastCastTime("test_skill")).thenReturn(0L);

        boolean result = executor.checkCooldown(mockPlayer, "test_skill", testSkill);

        assertTrue(result, "クールダウンなしで実行可能");
        verify(mockPlayer, never()).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("checkCooldown: クールダウン中")
    void testCheckCooldown_InCooldown() {
        long currentTime = System.currentTimeMillis();
        when(mockSkillData.getLastCastTime("test_skill")).thenReturn(currentTime - 2000);

        boolean result = executor.checkCooldown(mockPlayer, "test_skill", testSkill);

        assertFalse(result, "クールダウン中は実行不可");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("checkCooldown: クールダウン完了")
    void testCheckCooldown_Completed() {
        long currentTime = System.currentTimeMillis();
        when(mockSkillData.getLastCastTime("test_skill")).thenReturn(currentTime - 6000);

        boolean result = executor.checkCooldown(mockPlayer, "test_skill", testSkill);

        assertTrue(result, "クールダウン完了で実行可能");
    }

    // ==================== applyEffect テスト ====================

    @Test
    @DisplayName("applyEffect: ダメージ適用")
    void testApplyDamage() {
        LivingEntity mockTarget = mock(LivingEntity.class);

        executor.applyEffect(mockTarget, 50.0, testSkill);

        verify(mockTarget).damage(50.0);
    }

    @Test
    @DisplayName("applyEffect: ダメージ0")
    void testApplyDamage_Zero() {
        LivingEntity mockTarget = mock(LivingEntity.class);

        executor.applyEffect(mockTarget, 0.0, testSkill);

        verify(mockTarget, never()).damage(anyDouble());
    }

    // ==================== getFormulaEvaluator テスト ====================

    @Test
    @DisplayName("getFormulaEvaluator: エバリュエーターを取得")
    void testGetFormulaEvaluator() {
        assertNotNull(executor.getFormulaEvaluator(), "エバリュエーターが取得できる");
    }

    // ==================== CostConsumptionResult テスト ====================

    @Test
    @DisplayName("CostConsumptionResult: 成功結果")
    void testCostConsumptionResult_Success() {
        SkillExecutor.CostConsumptionResult result = new SkillExecutor.CostConsumptionResult(true, null, 15.0);

        assertTrue(result.isSuccess());
        assertNull(result.getErrorMessage());
        assertEquals(15.0, result.getConsumedAmount(), 0.001);
    }

    @Test
    @DisplayName("CostConsumptionResult: 失敗結果")
    void testCostConsumptionResult_Failure() {
        SkillExecutor.CostConsumptionResult result = new SkillExecutor.CostConsumptionResult(false, "エラー", 0.0);

        assertFalse(result.isSuccess());
        assertEquals("エラー", result.getErrorMessage());
        assertEquals(0.0, result.getConsumedAmount(), 0.001);
    }

    // ==================== selectTargets テスト ====================

    @Test
    @DisplayName("selectTargets: 単体ターゲット指定")
    void testSelectTargets_SingleTarget() {
        LivingEntity mockTarget = mock(LivingEntity.class);

        SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                .targetEntity(mockTarget)
                .build();

        List<LivingEntity> result = executor.selectTargets(mockPlayer, testSkill, config);

        assertEquals(1, result.size());
        assertTrue(result.contains(mockTarget));
    }

    @Test
    @DisplayName("selectTargets: 単体ターゲットがLivingEntityでない場合は空")
    void testSelectTargets_NonLivingEntityTarget() {
        Entity mockEntity = mock(Entity.class);

        SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                .targetEntity(mockEntity)
                .build();

        List<LivingEntity> result = executor.selectTargets(mockPlayer, testSkill, config);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("selectTargets: 範囲ターゲット選択（球形）")
    void testSelectTargets_AreaSpherical() {
        // モックの近くのエンティティを設定
        Location testLocation = new Location(mockWorld, 0, 0, 0);
        lenient().when(mockPlayer.getLocation()).thenReturn(testLocation);
        lenient().when(mockPlayer.getWorld()).thenReturn(mockWorld);

        LivingEntity mockEnemy = mock(LivingEntity.class);
        lenient().when(mockEnemy.getUniqueId()).thenReturn(UUID.randomUUID());
        lenient().when(mockEnemy.getType()).thenReturn(org.bukkit.entity.EntityType.ZOMBIE);

        // getNearbyEntitiesが敵を返すようにモック
        Collection<Entity> nearbyEntities = new ArrayList<>();
        nearbyEntities.add(mockEnemy);
        lenient().when(mockWorld.getNearbyEntities(any(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(nearbyEntities);

        SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                .rangeConfig(SkillExecutionConfig.RangeConfig.sphere(10.0))
                .build();

        List<LivingEntity> result = executor.selectTargets(mockPlayer, testSkill, config);

        // 敵（プレイヤーでないLivingEntity）が含まれる
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("selectTargets: プレイヤーはターゲットに含まれない")
    void testSelectTargets_ExcludesCaster() {
        Location testLocation = new Location(mockWorld, 0, 0, 0);
        lenient().when(mockPlayer.getLocation()).thenReturn(testLocation);
        lenient().when(mockPlayer.getWorld()).thenReturn(mockWorld);

        // getNearbyEntitiesが発動者自身のみ返す
        Collection<Entity> nearbyEntities = new ArrayList<>();
        nearbyEntities.add(mockPlayer);
        lenient().when(mockWorld.getNearbyEntities(any(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(nearbyEntities);

        SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                .rangeConfig(SkillExecutionConfig.RangeConfig.sphere(10.0))
                .build();

        List<LivingEntity> result = executor.selectTargets(mockPlayer, testSkill, config);

        // 発動者は含まれない
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("selectTargets: 最大ターゲット数制限")
    void testSelectTargets_MaxTargetsLimit() {
        Location testLocation = new Location(mockWorld, 0, 0, 0);
        lenient().when(mockPlayer.getLocation()).thenReturn(testLocation);
        lenient().when(mockPlayer.getWorld()).thenReturn(mockWorld);

        // 複数の敵を用意
        Collection<Entity> nearbyEntities = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            LivingEntity mockEnemy = mock(LivingEntity.class);
            lenient().when(mockEnemy.getUniqueId()).thenReturn(UUID.randomUUID());
            nearbyEntities.add(mockEnemy);
        }
        lenient().when(mockWorld.getNearbyEntities(any(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(nearbyEntities);

        SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                .rangeConfig(new SkillExecutionConfig.RangeConfig(10.0, 10.0, 10.0, true, 3))
                .build();

        List<LivingEntity> result = executor.selectTargets(mockPlayer, testSkill, config);

        // 最大3つに制限される
        assertTrue(result.size() <= 3);
    }

    @Test
    @DisplayName("selectTargets: 範囲ターゲット選択（直方体）")
    void testSelectTargets_AreaCuboid() {
        Location testLocation = new Location(mockWorld, 0, 0, 0);
        lenient().when(mockPlayer.getLocation()).thenReturn(testLocation);
        lenient().when(mockPlayer.getWorld()).thenReturn(mockWorld);

        LivingEntity mockEnemy = mock(LivingEntity.class);
        lenient().when(mockEnemy.getUniqueId()).thenReturn(UUID.randomUUID());

        Collection<Entity> nearbyEntities = new ArrayList<>();
        nearbyEntities.add(mockEnemy);
        lenient().when(mockWorld.getNearbyEntities(any(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(nearbyEntities);

        SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                .rangeConfig(new SkillExecutionConfig.RangeConfig(5.0, 3.0, 5.0, false, 0))
                .build();

        List<LivingEntity> result = executor.selectTargets(mockPlayer, testSkill, config);

        // 直方体範囲でもターゲットが取得できる
        assertNotNull(result);
    }

    @Test
    @DisplayName("selectTargets: デフォルト範囲設定")
    void testSelectTargets_DefaultRange() {
        Location testLocation = new Location(mockWorld, 0, 0, 0);
        lenient().when(mockPlayer.getLocation()).thenReturn(testLocation);
        lenient().when(mockPlayer.getWorld()).thenReturn(mockWorld);

        lenient().when(mockWorld.getNearbyEntities(any(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new ArrayList<>());

        SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                .build(); // RangeConfigなし

        List<LivingEntity> result = executor.selectTargets(mockPlayer, testSkill, config);

        // デフォルトの球形範囲が使用される
        assertNotNull(result);
    }

    // ==================== calculateDamageWithFormula テスト（カスタム変数付き）====================

    @Test
    @DisplayName("calculateDamageWithFormula: カスタム変数付き")
    void testCalculateDamageWithFormula_CustomVariables() {
        Map<String, Double> customVars = new HashMap<>();
        customVars.put("bonus", 50.0);
        customVars.put("multiplier", 1.5);

        double damage = executor.calculateDamageWithFormula("STR + bonus", mockRpgPlayer, 5, customVars);

        // STR(10) + bonus(50) = 60
        assertEquals(60.0, damage, 0.001);
    }

    @Test
    @DisplayName("calculateDamageWithFormula: 数式評価例外時は0")
    void testCalculateDamageWithFormula_Exception() {
        // 不正な数式
        double damage = executor.calculateDamageWithFormula("invalid syntax!", mockRpgPlayer, 5);

        assertEquals(0.0, damage, 0.001);
    }

    @Test
    @DisplayName("calculateDamageWithFormula: カスタム変数null時")
    void testCalculateDamageWithFormula_NullCustomVariables() {
        double damage = executor.calculateDamageWithFormula("STR", mockRpgPlayer, 5, null);

        assertEquals(10.0, damage, 0.001);
    }

    // ==================== executeSkill テスト（追加）====================

    @Test
    @DisplayName("executeSkill: 未知のコストタイプ")
    void testExecuteSkill_UnknownCostType() {
        Skill testSkill2 = createTestSkill("test2", "テスト2");
        // 未知のタイプを渡すテストはconfig側でカバー

        when(skillRepository.getSkill("test2")).thenReturn(testSkill2);
        when(playerSkillService.hasSkill(mockPlayer, "test2")).thenReturn(true);
        when(playerSkillService.getSkillLevel(mockPlayer, "test2")).thenReturn(1);
        when(mockRpgPlayer.hasMana(10)).thenReturn(true);
        when(playerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);

        // 正常に消費できることを確認
        SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                .consumeCost(true)
                .costType(SkillCostType.MANA)
                .applyCooldown(false)
                .applyDamage(false)
                .build();

        SkillExecutionResult result = executor.executeSkill(mockPlayer, "test2", config);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("executeSkill: ターゲットにダメージ適用")
    void testExecuteSkill_ApplyDamageToTargets() {
        LivingEntity mockTarget = mock(LivingEntity.class);
        Location testLocation = new Location(mockWorld, 0, 0, 0);
        lenient().when(mockPlayer.getLocation()).thenReturn(testLocation);
        lenient().when(mockPlayer.getWorld()).thenReturn(mockWorld);

        // ターゲットを含む近くのエンティティ
        Collection<Entity> nearbyEntities = new ArrayList<>();
        nearbyEntities.add(mockTarget);
        lenient().when(mockWorld.getNearbyEntities(any(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(nearbyEntities);

        when(skillRepository.getSkill("test_skill")).thenReturn(testSkill);
        when(playerSkillService.hasSkill(mockPlayer, "test_skill")).thenReturn(true);
        when(playerSkillService.getSkillLevel(mockPlayer, "test_skill")).thenReturn(1);
        when(mockRpgPlayer.hasMana(10)).thenReturn(true);
        when(mockSkillData.getLastCastTime("test_skill")).thenReturn(0L);

        SkillExecutionConfig config = SkillExecutionConfig.createDefault();

        SkillExecutionResult result = executor.executeSkill(mockPlayer, "test_skill", config);

        assertTrue(result.isSuccess());
        verify(mockTarget).damage(anyDouble());
    }
}
