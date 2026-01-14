package com.example.rpgplugin.skill.repository;

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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        Skill.DamageCalculation damage = new Skill.DamageCalculation(10.0, Stat.STRENGTH, 1.0, 0.0);
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
        Skill.DamageCalculation damageConfig = new Skill.DamageCalculation(10.0, null, 1.0, 0.0);
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
}
