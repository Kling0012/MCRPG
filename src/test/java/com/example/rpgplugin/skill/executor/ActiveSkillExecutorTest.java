package com.example.rpgplugin.skill.executor;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.model.skill.DamageCalculation;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.LevelDependentParameter;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillType;
import com.example.rpgplugin.skill.component.SkillEffect;
import com.example.rpgplugin.skill.target.TargetType;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import com.example.rpgplugin.model.skill.DamageCalculation;

/**
 * ActiveSkillExecutorの単体テスト
 *
 * <p>アクティブスキル実行エグゼキューターの機能テスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("ActiveSkillExecutor テスト")
@ExtendWith(MockitoExtension.class)
class ActiveSkillExecutorTest {

    @Mock
    private RPGPlugin mockPlugin;
    @Mock
    private SkillManager mockSkillManager;
    @Mock
    private PlayerManager mockPlayerManager;
    @Mock
    private Player mockPlayer;
    @Mock
    private RPGPlayer mockRpgPlayer;
    @Mock
    private StatManager mockStatManager;
    @Mock
    private SkillManager.PlayerSkillData mockSkillData;
    @Mock
    private LivingEntity mockTarget;
    @Mock
    private World mockWorld;
    @Mock
    private Server mockServer;
    @Mock
    private PluginManager mockPluginManager;

    private ActiveSkillExecutor executor;
    private UUID testUuid;
    private Skill testSkill;

    @BeforeEach
    void setUp() {
        // Bukkit.getServer()のモック（TriggerManager.initialize()で必要）
        lenient().when(mockPlugin.getServer()).thenReturn(mockServer);
        lenient().when(mockServer.getPluginManager()).thenReturn(mockPluginManager);

        // TriggerManagerを初期化（server stubの後で呼ぶ必要あり）
        com.example.rpgplugin.skill.component.trigger.TriggerManager.initialize(mockPlugin);

        executor = new ActiveSkillExecutor(mockPlugin, mockSkillManager, mockPlayerManager);
        testUuid = UUID.randomUUID();

        // lenient() - 全てのテストで使用されないスタブ
        lenient().when(mockPlayer.getUniqueId()).thenReturn(testUuid);
        lenient().when(mockPlayer.getLocation()).thenReturn(new Location(mockWorld, 0, 0, 0));
        lenient().when(mockPlayer.getWorld()).thenReturn(mockWorld);

        lenient().when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        lenient().when(mockRpgPlayer.getBukkitPlayer()).thenReturn(mockPlayer);
        lenient().when(mockRpgPlayer.getStatManager()).thenReturn(mockStatManager);
        lenient().when(mockStatManager.getFinalStat(any(Stat.class))).thenReturn(10);

        lenient().when(mockSkillManager.getPlayerSkillData(any(Player.class))).thenReturn(mockSkillData);
        lenient().when(mockSkillManager.getPlayerSkillData(any(UUID.class))).thenReturn(mockSkillData);

        testSkill = createTestSkill("test_skill", "テストスキル");
    }

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

    // ==================== execute テスト ====================

    @Test
    @DisplayName("execute: クールダウン中は失敗")
    void testExecute_InCooldown() {
        lenient().when(mockSkillManager.checkCooldown(any(Player.class), eq("test_skill"))).thenReturn(false);

        boolean result = executor.execute(mockPlayer, testSkill, 1);

        assertFalse(result, "クールダウン中は失敗すること");
        verify(mockSkillManager, never()).getPlayerSkillData(any(Player.class));
    }

    @Test
    @DisplayName("execute: プレイヤーデータ未読み込み")
    void testExecute_PlayerDataNotLoaded() {
        lenient().when(mockSkillManager.checkCooldown(any(Player.class), eq("test_skill"))).thenReturn(true);
        lenient().when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(null);

        boolean result = executor.execute(mockPlayer, testSkill, 1);

        assertFalse(result, "プレイヤーデータ未読み込みは失敗すること");
    }

    @Test
    @DisplayName("execute: MP不足（スキルモック）")
    void testExecute_NotEnoughMana() {
        // コスト付きスキルモックを作成
        Skill costSkill = mock(Skill.class);
        lenient().when(costSkill.getId()).thenReturn("cost_skill");
        lenient().when(costSkill.getCostFromComponents(1)).thenReturn(10);
        lenient().when(costSkill.getTargetFromComponents()).thenReturn(null);
        lenient().when(costSkill.findComponentByKey("damage")).thenReturn(null);
        lenient().when(costSkill.getComponentEffect()).thenReturn(null);
        lenient().when(costSkill.getColoredDisplayName()).thenReturn("Cost Skill");

        lenient().when(mockSkillManager.checkCooldown(any(Player.class), eq("cost_skill"))).thenReturn(true);
        lenient().when(mockRpgPlayer.consumeSkillCost(10)).thenReturn(false);
        lenient().when(mockRpgPlayer.isManaCostType()).thenReturn(true);

        boolean result = executor.execute(mockPlayer, costSkill, 1);

        assertFalse(result, "MP不足は失敗すること");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("execute: HP不足（スキルモック）")
    void testExecute_NotEnoughHealth() {
        // コスト付きスキルモックを作成
        Skill costSkill = mock(Skill.class);
        lenient().when(costSkill.getId()).thenReturn("cost_skill");
        lenient().when(costSkill.getCostFromComponents(1)).thenReturn(10);
        lenient().when(costSkill.getTargetFromComponents()).thenReturn(null);
        lenient().when(costSkill.findComponentByKey("damage")).thenReturn(null);
        lenient().when(costSkill.getComponentEffect()).thenReturn(null);
        lenient().when(costSkill.getColoredDisplayName()).thenReturn("Cost Skill");

        lenient().when(mockSkillManager.checkCooldown(any(Player.class), eq("cost_skill"))).thenReturn(true);
        lenient().when(mockRpgPlayer.consumeSkillCost(10)).thenReturn(false);
        lenient().when(mockRpgPlayer.isManaCostType()).thenReturn(false);

        boolean result = executor.execute(mockPlayer, costSkill, 1);

        assertFalse(result, "HP不足は失敗すること");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("execute: 成功（コスト消費なし）")
    void testExecute_SuccessNoCost() {
        // コスト0のスキル
        Skill noCostSkill = new Skill(
                "no_cost", "no_cost", "コストなし", SkillType.NORMAL, List.of(),
                10, 5.0, 0, null, null, SkillCostType.MANA,
                null, null, null, List.of()
        );

        lenient().when(mockSkillManager.checkCooldown(any(Player.class), eq("no_cost"))).thenReturn(true);

        boolean result = executor.execute(mockPlayer, noCostSkill, 1);

        assertTrue(result, "成功すること");
        verify(mockSkillData).setLastCastTime(eq("no_cost"), anyLong());
    }

    @Test
    @DisplayName("execute: 成功（コスト消費あり）")
    void testExecute_SuccessWithCost() {
        // コスト付きスキルモックを作成
        Skill costSkill = mock(Skill.class);
        lenient().when(costSkill.getId()).thenReturn("cost_skill");
        lenient().when(costSkill.getCostFromComponents(1)).thenReturn(10);
        lenient().when(costSkill.getTargetFromComponents()).thenReturn(null);
        lenient().when(costSkill.findComponentByKey("damage")).thenReturn(null);
        lenient().when(costSkill.getComponentEffect()).thenReturn(null);
        lenient().when(costSkill.getColoredDisplayName()).thenReturn("Cost Skill");

        lenient().when(mockSkillManager.checkCooldown(any(Player.class), eq("cost_skill"))).thenReturn(true);
        lenient().when(mockRpgPlayer.consumeSkillCost(10)).thenReturn(true);
        lenient().when(mockRpgPlayer.isManaCostType()).thenReturn(true);

        boolean result = executor.execute(mockPlayer, costSkill, 1);

        assertTrue(result, "成功すること");
        verify(mockSkillData).setLastCastTime(eq("cost_skill"), anyLong());
    }

    // ==================== executeAt テスト ====================

    @Test
    @DisplayName("executeAt: null引数")
    void testExecuteAt_NullArguments() {
        assertFalse(executor.executeAt(null, testSkill, 1, mockTarget), "player=nullは失敗");
        assertFalse(executor.executeAt(mockPlayer, null, 1, mockTarget), "skill=nullは失敗");
        assertFalse(executor.executeAt(mockPlayer, testSkill, 1, null), "target=nullは失敗");
    }

    @Test
    @DisplayName("executeAt: プレイヤーデータ未読み込み")
    void testExecuteAt_PlayerDataNotLoaded() {
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(null);

        boolean result = executor.executeAt(mockPlayer, testSkill, 1, mockTarget);

        assertFalse(result, "プレイヤーデータ未読み込みは失敗すること");
    }

    @Test
    @DisplayName("executeAt: 無効ターゲット")
    void testExecuteAt_InvalidTarget() {
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        when(mockTarget.isValid()).thenReturn(false);

        boolean result = executor.executeAt(mockPlayer, testSkill, 1, mockTarget);

        assertFalse(result, "無効ターゲットは失敗すること");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("executeAt: クールダウン中")
    void testExecuteAt_InCooldown() {
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        when(mockTarget.isValid()).thenReturn(true);
        when(mockSkillManager.checkCooldown(any(Player.class), eq("test_skill"))).thenReturn(false);

        boolean result = executor.executeAt(mockPlayer, testSkill, 1, mockTarget);

        assertFalse(result, "クールダウン中は失敗すること");
    }

    @Test
    @DisplayName("executeAt: 敵対的ターゲットで成功")
    void testExecuteAt_SuccessWithEnemyTarget() {
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        when(mockTarget.isValid()).thenReturn(true);
        when(mockSkillManager.checkCooldown(any(Player.class), eq("test_skill"))).thenReturn(true);
        // mockTarget is LivingEntity but not Player, so it's an enemy

        boolean result = executor.executeAt(mockPlayer, testSkill, 1, mockTarget);

        assertTrue(result, "敵対的ターゲットで成功すること");
        verify(mockTarget).damage(anyDouble(), eq(mockPlayer));
        verify(mockSkillData).setLastCastTime(eq("test_skill"), anyLong());
    }

    @Test
    @DisplayName("executeAt: 非敵対的ターゲット（プレイヤー）で失敗")
    void testExecuteAt_NonEnemyTarget_Fails() {
        Player mockPlayerTarget = mock(Player.class);
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        when(mockPlayerTarget.isValid()).thenReturn(true);
        when(mockSkillManager.checkCooldown(any(Player.class), eq("test_skill"))).thenReturn(true);

        boolean result = executor.executeAt(mockPlayer, testSkill, 1, mockPlayerTarget);

        assertFalse(result, "非敵対的ターゲットは失敗すること");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("executeAt: LivingEntity以外のターゲット")
    void testExecuteAt_NonLivingEntityTarget() {
        org.bukkit.entity.Entity mockEntity = mock(org.bukkit.entity.Entity.class);
        lenient().when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        lenient().when(mockEntity.isValid()).thenReturn(true);
        lenient().when(mockSkillManager.checkCooldown(any(Player.class), eq("test_skill"))).thenReturn(true);

        boolean result = executor.executeAt(mockPlayer, testSkill, 1, mockEntity);

        assertTrue(result, "LivingEntity以外でも成功すること（ダメージは適用されない）");
        // Entityにはdamageメソッドがないため検証は省略
    }

    // ==================== executeWithCostType テスト ====================

    @Test
    @DisplayName("executeWithCostType: null引数")
    void testExecuteWithCostType_NullArguments() {
        assertFalse(executor.executeWithCostType(null, testSkill, 1, SkillCostType.MANA));
        assertFalse(executor.executeWithCostType(mockPlayer, null, 1, SkillCostType.MANA));
        assertFalse(executor.executeWithCostType(mockPlayer, testSkill, 1, null));
    }

    @Test
    @DisplayName("executeWithCostType: クールダウン中")
    void testExecuteWithCostType_InCooldown() {
        when(mockSkillManager.checkCooldown(any(Player.class), eq("test_skill"))).thenReturn(false);

        boolean result = executor.executeWithCostType(mockPlayer, testSkill, 1, SkillCostType.MANA);

        assertFalse(result, "クールダウン中は失敗すること");
    }

    @Test
    @DisplayName("executeWithCostType: プレイヤーデータ未読み込み")
    void testExecuteWithCostType_PlayerDataNotLoaded() {
        when(mockSkillManager.checkCooldown(any(Player.class), eq("test_skill"))).thenReturn(true);
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(null);

        boolean result = executor.executeWithCostType(mockPlayer, testSkill, 1, SkillCostType.MANA);

        assertFalse(result, "プレイヤーデータ未読み込みは失敗すること");
    }

    @Test
    @DisplayName("executeWithCostType: MANA消費")
    void testExecuteWithCostType_Mana() {
        when(mockSkillManager.checkCooldown(any(Player.class), eq("test_skill"))).thenReturn(true);
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        when(mockRpgPlayer.hasMana(10)).thenReturn(true);
        when(mockRpgPlayer.consumeMana(10)).thenReturn(true);

        boolean result = executor.executeWithCostType(mockPlayer, testSkill, 1, SkillCostType.MANA);

        assertTrue(result, "成功すること");
        verify(mockRpgPlayer).consumeMana(10);
    }

    @Test
    @DisplayName("executeWithCostType: MANA不足")
    void testExecuteWithCostType_NotEnoughMana() {
        when(mockSkillManager.checkCooldown(any(Player.class), eq("test_skill"))).thenReturn(true);
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        when(mockRpgPlayer.hasMana(10)).thenReturn(false);

        boolean result = executor.executeWithCostType(mockPlayer, testSkill, 1, SkillCostType.MANA);

        assertFalse(result, "MANA不足は失敗すること");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("executeWithCostType: HP消費")
    void testExecuteWithCostType_Hp() {
        when(mockSkillManager.checkCooldown(any(Player.class), eq("test_skill"))).thenReturn(true);
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        when(mockRpgPlayer.getBukkitPlayer()).thenReturn(mockPlayer);
        when(mockPlayer.getHealth()).thenReturn(20.0);

        boolean result = executor.executeWithCostType(mockPlayer, testSkill, 1, SkillCostType.HP);

        assertTrue(result, "成功すること");
        verify(mockPlayer).setHealth(10.0);
    }

    @Test
    @DisplayName("executeWithCostType: HP不足で失敗")
    void testExecuteWithCostType_NotEnoughHealth() {
        when(mockSkillManager.checkCooldown(any(Player.class), eq("test_skill"))).thenReturn(true);
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        when(mockRpgPlayer.getBukkitPlayer()).thenReturn(mockPlayer);
        when(mockPlayer.getHealth()).thenReturn(5.0); // HPがコスト(10)より低い

        boolean result = executor.executeWithCostType(mockPlayer, testSkill, 1, SkillCostType.HP);

        assertFalse(result, "HP不足は失敗すること");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("executeWithCostType: HP消費時にBukkitPlayerがnull")
    void testExecuteWithCostType_Hp_NullBukkitPlayer() {
        when(mockSkillManager.checkCooldown(any(Player.class), eq("test_skill"))).thenReturn(true);
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        when(mockRpgPlayer.getBukkitPlayer()).thenReturn(null);

        boolean result = executor.executeWithCostType(mockPlayer, testSkill, 1, SkillCostType.HP);

        assertFalse(result, "BukkitPlayerがnullの場合は失敗すること");
    }

    // ==================== execute ダメージ計算テスト ====================

    @Test
    @DisplayName("execute: コンポーネントベースのダメージ計算")
    void testExecute_ComponentBasedDamage() {
        // ダメージコンポーネント付きスキルモック
        Skill damageSkill = mock(Skill.class);
        when(damageSkill.getId()).thenReturn("damage_skill");
        lenient().when(damageSkill.getCostFromComponents(anyInt())).thenReturn(0);
        lenient().when(damageSkill.getTargetFromComponents()).thenReturn(null);
        lenient().when(damageSkill.getComponentEffect()).thenReturn(null);

        // EffectComponentモック
        com.example.rpgplugin.skill.component.EffectComponent mockDamageComponent =
                mock(com.example.rpgplugin.skill.component.EffectComponent.class);
        com.example.rpgplugin.skill.component.ComponentSettings mockSettings =
                mock(com.example.rpgplugin.skill.component.ComponentSettings.class);

        when(damageSkill.findComponentByKey("damage")).thenReturn(mockDamageComponent);
        when(mockDamageComponent.getSettings()).thenReturn(mockSettings);
        when(mockSettings.has("value")).thenReturn(true);
        when(mockSettings.getString("value", "0")).thenReturn("10 + level * 2");
        lenient().when(mockSettings.has("stat_multiplier")).thenReturn(false);
        lenient().when(mockSettings.has("level_multiplier")).thenReturn(false);

        when(mockSkillManager.checkCooldown(any(Player.class), eq("damage_skill"))).thenReturn(true);
        lenient().when(mockRpgPlayer.consumeSkillCost(0)).thenReturn(true);

        boolean result = executor.execute(mockPlayer, damageSkill, 5);

        assertTrue(result, "ダメージ計算付きで成功すること");
        verify(mockSkillData).setLastCastTime(eq("damage_skill"), anyLong());
    }

    @Test
    @DisplayName("execute: ステータス倍率付きダメージ")
    void testExecute_DamageWithStatMultiplier() {
        Skill damageSkill = mock(Skill.class);
        when(damageSkill.getId()).thenReturn("damage_skill");
        lenient().when(damageSkill.getCostFromComponents(anyInt())).thenReturn(0);
        lenient().when(damageSkill.getTargetFromComponents()).thenReturn(null);
        lenient().when(damageSkill.getComponentEffect()).thenReturn(null);

        com.example.rpgplugin.skill.component.EffectComponent mockDamageComponent =
                mock(com.example.rpgplugin.skill.component.EffectComponent.class);
        com.example.rpgplugin.skill.component.ComponentSettings mockSettings =
                mock(com.example.rpgplugin.skill.component.ComponentSettings.class);

        when(damageSkill.findComponentByKey("damage")).thenReturn(mockDamageComponent);
        when(mockDamageComponent.getSettings()).thenReturn(mockSettings);
        when(mockSettings.has("value")).thenReturn(true);
        when(mockSettings.getString("value", "0")).thenReturn("10");
        when(mockSettings.has("stat_multiplier")).thenReturn(true);
        when(mockSettings.getString("stat_multiplier", "")).thenReturn("strength");
        when(mockSettings.getDouble("multiplier", 1.0)).thenReturn(2.0);
        lenient().when(mockSettings.has("level_multiplier")).thenReturn(false);
        lenient().when(mockStatManager.getFinalStat(Stat.STRENGTH)).thenReturn(15);

        when(mockSkillManager.checkCooldown(any(Player.class), eq("damage_skill"))).thenReturn(true);
        lenient().when(mockRpgPlayer.consumeSkillCost(0)).thenReturn(true);

        boolean result = executor.execute(mockPlayer, damageSkill, 1);

        assertTrue(result, "ステータス倍率付きで成功すること");
    }

    @Test
    @DisplayName("execute: レベル倍率付きダメージ")
    void testExecute_DamageWithLevelMultiplier() {
        Skill damageSkill = mock(Skill.class);
        when(damageSkill.getId()).thenReturn("damage_skill");
        lenient().when(damageSkill.getCostFromComponents(anyInt())).thenReturn(0);
        lenient().when(damageSkill.getTargetFromComponents()).thenReturn(null);
        lenient().when(damageSkill.getComponentEffect()).thenReturn(null);

        com.example.rpgplugin.skill.component.EffectComponent mockDamageComponent =
                mock(com.example.rpgplugin.skill.component.EffectComponent.class);
        com.example.rpgplugin.skill.component.ComponentSettings mockSettings =
                mock(com.example.rpgplugin.skill.component.ComponentSettings.class);

        when(damageSkill.findComponentByKey("damage")).thenReturn(mockDamageComponent);
        when(mockDamageComponent.getSettings()).thenReturn(mockSettings);
        when(mockSettings.has("value")).thenReturn(true);
        when(mockSettings.getString("value", "0")).thenReturn("10");
        lenient().when(mockSettings.has("stat_multiplier")).thenReturn(false);
        when(mockSettings.has("level_multiplier")).thenReturn(true);
        when(mockSettings.getDouble("level_multiplier", 0.0)).thenReturn(5.0);

        when(mockSkillManager.checkCooldown(any(Player.class), eq("damage_skill"))).thenReturn(true);
        lenient().when(mockRpgPlayer.consumeSkillCost(0)).thenReturn(true);

        boolean result = executor.execute(mockPlayer, damageSkill, 3);

        assertTrue(result, "レベル倍率付きで成功すること");
    }

    @Test
    @DisplayName("execute: ステータス変数付き数式")
    void testExecute_DamageWithStatVariables() {
        Skill damageSkill = mock(Skill.class);
        when(damageSkill.getId()).thenReturn("damage_skill");
        lenient().when(damageSkill.getCostFromComponents(anyInt())).thenReturn(0);
        lenient().when(damageSkill.getTargetFromComponents()).thenReturn(null);
        lenient().when(damageSkill.getComponentEffect()).thenReturn(null);

        com.example.rpgplugin.skill.component.EffectComponent mockDamageComponent =
                mock(com.example.rpgplugin.skill.component.EffectComponent.class);
        com.example.rpgplugin.skill.component.ComponentSettings mockSettings =
                mock(com.example.rpgplugin.skill.component.ComponentSettings.class);

        when(damageSkill.findComponentByKey("damage")).thenReturn(mockDamageComponent);
        when(mockDamageComponent.getSettings()).thenReturn(mockSettings);
        when(mockSettings.has("value")).thenReturn(true);
        when(mockSettings.getString("value", "0")).thenReturn("strength + intelligence");
        lenient().when(mockSettings.has("stat_multiplier")).thenReturn(false);
        lenient().when(mockSettings.has("level_multiplier")).thenReturn(false);
        when(mockStatManager.getFinalStat(Stat.STRENGTH)).thenReturn(15);
        when(mockStatManager.getFinalStat(Stat.INTELLIGENCE)).thenReturn(10);

        when(mockSkillManager.checkCooldown(any(Player.class), eq("damage_skill"))).thenReturn(true);
        lenient().when(mockRpgPlayer.consumeSkillCost(0)).thenReturn(true);

        boolean result = executor.execute(mockPlayer, damageSkill, 1);

        assertTrue(result, "ステータス変数付きで成功すること");
    }

    @Test
    @DisplayName("execute: 無効な数式パース時のフォールバック")
    void testExecute_InvalidFormulaFallback() {
        Skill damageSkill = mock(Skill.class);
        when(damageSkill.getId()).thenReturn("damage_skill");
        lenient().when(damageSkill.getCostFromComponents(anyInt())).thenReturn(0);
        lenient().when(damageSkill.getTargetFromComponents()).thenReturn(null);
        lenient().when(damageSkill.getComponentEffect()).thenReturn(null);

        com.example.rpgplugin.skill.component.EffectComponent mockDamageComponent =
                mock(com.example.rpgplugin.skill.component.EffectComponent.class);
        com.example.rpgplugin.skill.component.ComponentSettings mockSettings =
                mock(com.example.rpgplugin.skill.component.ComponentSettings.class);

        when(damageSkill.findComponentByKey("damage")).thenReturn(mockDamageComponent);
        when(mockDamageComponent.getSettings()).thenReturn(mockSettings);
        when(mockSettings.has("value")).thenReturn(true);
        when(mockSettings.getString("value", "0")).thenReturn("invalid formula");
        lenient().when(mockSettings.getDouble("value", 0.0)).thenReturn(25.0);

        when(mockSkillManager.checkCooldown(any(Player.class), eq("damage_skill"))).thenReturn(true);
        lenient().when(mockRpgPlayer.consumeSkillCost(0)).thenReturn(true);

        boolean result = executor.execute(mockPlayer, damageSkill, 1);

        assertTrue(result, "フォールバック値を使用して成功すること");
    }

    // ==================== execute コンポーネント効果テスト ====================

    @Test
    @DisplayName("execute: コンポーネント効果実行")
    void testExecute_WithComponentEffect() {
        Skill effectSkill = mock(Skill.class);
        when(effectSkill.getId()).thenReturn("effect_skill");
        lenient().when(effectSkill.getCostFromComponents(anyInt())).thenReturn(0);
        lenient().when(effectSkill.getTargetFromComponents()).thenReturn(null);
        lenient().when(effectSkill.findComponentByKey("damage")).thenReturn(null);

        // SkillEffectモック
        SkillEffect mockComponentEffect = mock(SkillEffect.class);
        when(effectSkill.getComponentEffect()).thenReturn(mockComponentEffect);

        when(mockSkillManager.checkCooldown(any(Player.class), eq("effect_skill"))).thenReturn(true);
        lenient().when(mockRpgPlayer.consumeSkillCost(0)).thenReturn(true);

        boolean result = executor.execute(mockPlayer, effectSkill, 1);

        assertTrue(result, "コンポーネント効果付きで成功すること");
        verify(mockSkillData).setLastCastTime(eq("effect_skill"), anyLong());
    }

    // ==================== getTargets テスト ====================

    @Test
    @DisplayName("execute: ターゲット選択（SkillTargetあり）")
    void testExecute_WithSkillTarget() {
        Skill targetedSkill = mock(Skill.class);
        when(targetedSkill.getId()).thenReturn("targeted_skill");
        lenient().when(targetedSkill.getCostFromComponents(anyInt())).thenReturn(0);
        lenient().when(targetedSkill.findComponentByKey("damage")).thenReturn(null);
        lenient().when(targetedSkill.getComponentEffect()).thenReturn(null);

        // SkillTargetモック
        com.example.rpgplugin.skill.target.SkillTarget mockSkillTarget =
                mock(com.example.rpgplugin.skill.target.SkillTarget.class);
        when(targetedSkill.getTargetFromComponents()).thenReturn(mockSkillTarget);
        lenient().when(mockSkillTarget.getRange()).thenReturn(10.0);
        lenient().when(mockSkillTarget.getType()).thenReturn(TargetType.SELF);

        when(mockSkillManager.checkCooldown(any(Player.class), eq("targeted_skill"))).thenReturn(true);
        lenient().when(mockRpgPlayer.consumeSkillCost(0)).thenReturn(true);

        boolean result = executor.execute(mockPlayer, targetedSkill, 1);

        assertTrue(result, "ターゲット選択付きで成功すること");
    }

    // ==================== ゲッター テスト ====================

    @Test
    @DisplayName("getPlugin: プラグイン取得")
    void testGetPlugin() {
        assertEquals(mockPlugin, executor.getPlugin(), "プラグインインスタンスが取得できる");
    }

    @Test
    @DisplayName("getSkillManager: スキルマネージャー取得")
    void testGetSkillManager() {
        assertEquals(mockSkillManager, executor.getSkillManager(), "スキルマネージャーが取得できる");
    }

    @Test
    @DisplayName("getPlayerManager: プレイヤーマネージャー取得")
    void testGetPlayerManager() {
        assertEquals(mockPlayerManager, executor.getPlayerManager(), "プレイヤーマネージャーが取得できる");
    }

    // ==================== 追加カバレッジテスト ====================

    @Test
    @DisplayName("execute: コストが0の場合は消費スキップ")
    void testExecute_ZeroCost() {
        Skill zeroCostSkill = mock(Skill.class);
        when(zeroCostSkill.getId()).thenReturn("zero_cost");
        when(zeroCostSkill.getCostFromComponents(anyInt())).thenReturn(0);
        when(zeroCostSkill.getTargetFromComponents()).thenReturn(null);
        when(zeroCostSkill.findComponentByKey("damage")).thenReturn(null);
        when(zeroCostSkill.getComponentEffect()).thenReturn(null);

        when(mockSkillManager.checkCooldown(any(Player.class), eq("zero_cost"))).thenReturn(true);

        boolean result = executor.execute(mockPlayer, zeroCostSkill, 1);

        assertTrue(result, "コスト0は成功すること");
        verify(mockRpgPlayer, never()).consumeSkillCost(anyInt());
    }

    @Test
    @DisplayName("execute: calculateDamage例外時")
    void testExecute_CalculateDamageException() {
        Skill damageSkill = mock(Skill.class);
        when(damageSkill.getId()).thenReturn("damage_skill");
        when(damageSkill.getCostFromComponents(anyInt())).thenReturn(0);
        when(damageSkill.getTargetFromComponents()).thenReturn(null);
        when(damageSkill.getComponentEffect()).thenReturn(null);

        com.example.rpgplugin.skill.component.EffectComponent mockDamageComponent =
                mock(com.example.rpgplugin.skill.component.EffectComponent.class);
        com.example.rpgplugin.skill.component.ComponentSettings mockSettings =
                mock(com.example.rpgplugin.skill.component.ComponentSettings.class);

        when(damageSkill.findComponentByKey("damage")).thenReturn(mockDamageComponent);
        when(mockDamageComponent.getSettings()).thenReturn(mockSettings);
        when(mockSettings.has("value")).thenReturn(false);
        when(mockSettings.getDouble("value", 0.0)).thenReturn(0.0);
        when(mockSettings.has("stat_multiplier")).thenReturn(true);
        when(mockSettings.getString("stat_multiplier", "")).thenReturn("invalid_stat");
        when(mockSettings.getDouble("multiplier", 1.0)).thenReturn(1.0);

        when(mockSkillManager.checkCooldown(any(Player.class), eq("damage_skill"))).thenReturn(true);

        boolean result = executor.execute(mockPlayer, damageSkill, 1);

        assertTrue(result, "例外ハンドリングで成功すること");
    }

    @Test
    @DisplayName("executeWithCostType: コスト0の場合")
    void testExecuteWithCostType_ZeroCost() {
        Skill zeroCostSkill = new Skill(
                "zero_cost", "zero_cost", "コストなし", SkillType.NORMAL, List.of(),
                10, 0.0, 0, null, null, SkillCostType.MANA,
                null, null, null, List.of()
        );

        when(mockSkillManager.checkCooldown(any(Player.class), eq("zero_cost"))).thenReturn(true);
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);

        boolean result = executor.executeWithCostType(mockPlayer, zeroCostSkill, 1, SkillCostType.MANA);

        assertTrue(result, "コスト0は成功すること");
        verify(mockRpgPlayer, never()).consumeMana(anyInt());
    }

    @Test
    @DisplayName("execute: Stat.fromShortNameとfromDisplayName両方失敗")
    void testExecute_StatResolutionFailure() {
        Skill damageSkill = mock(Skill.class);
        when(damageSkill.getId()).thenReturn("damage_skill");
        when(damageSkill.getCostFromComponents(anyInt())).thenReturn(0);
        when(damageSkill.getTargetFromComponents()).thenReturn(null);
        when(damageSkill.getComponentEffect()).thenReturn(null);

        com.example.rpgplugin.skill.component.EffectComponent mockDamageComponent =
                mock(com.example.rpgplugin.skill.component.EffectComponent.class);
        com.example.rpgplugin.skill.component.ComponentSettings mockSettings =
                mock(com.example.rpgplugin.skill.component.ComponentSettings.class);

        when(damageSkill.findComponentByKey("damage")).thenReturn(mockDamageComponent);
        when(mockDamageComponent.getSettings()).thenReturn(mockSettings);
        when(mockSettings.has("value")).thenReturn(true);
        when(mockSettings.getString("value", "0")).thenReturn("10");
        when(mockSettings.has("stat_multiplier")).thenReturn(true);
        when(mockSettings.getString("stat_multiplier", "")).thenReturn("nonexistent");
        when(mockSettings.getDouble("multiplier", 1.0)).thenReturn(2.0);

        when(mockSkillManager.checkCooldown(any(Player.class), eq("damage_skill"))).thenReturn(true);

        boolean result = executor.execute(mockPlayer, damageSkill, 1);

        assertTrue(result, "stat解決失敗でも成功すること");
    }

    @Test
    @DisplayName("parseFormula: カッコ付き数式")
    void testParseFormula_WithParentheses() {
        // 内部メソッドのテストとしてexecute経由で検証
        Skill damageSkill = mock(Skill.class);
        when(damageSkill.getId()).thenReturn("damage_skill");
        when(damageSkill.getCostFromComponents(anyInt())).thenReturn(0);
        when(damageSkill.getTargetFromComponents()).thenReturn(null);
        when(damageSkill.getComponentEffect()).thenReturn(null);

        com.example.rpgplugin.skill.component.EffectComponent mockDamageComponent =
                mock(com.example.rpgplugin.skill.component.EffectComponent.class);
        com.example.rpgplugin.skill.component.ComponentSettings mockSettings =
                mock(com.example.rpgplugin.skill.component.ComponentSettings.class);

        when(damageSkill.findComponentByKey("damage")).thenReturn(mockDamageComponent);
        when(mockDamageComponent.getSettings()).thenReturn(mockSettings);
        when(mockSettings.has("value")).thenReturn(true);
        when(mockSettings.getString("value", "0")).thenReturn("(10 + 5) * 2");
        when(mockSettings.has("stat_multiplier")).thenReturn(false);

        when(mockSkillManager.checkCooldown(any(Player.class), eq("damage_skill"))).thenReturn(true);

        boolean result = executor.execute(mockPlayer, damageSkill, 1);

        assertTrue(result, "カッコ付き数式で成功すること");
    }

    @Test
    @DisplayName("execute: 複数ターゲットにダメージ適用")
    void testExecute_MultipleTargetsDamage() {
        Skill damageSkill = mock(Skill.class);
        when(damageSkill.getId()).thenReturn("damage_skill");
        when(damageSkill.getCostFromComponents(anyInt())).thenReturn(0);
        when(damageSkill.getComponentEffect()).thenReturn(null);

        com.example.rpgplugin.skill.component.EffectComponent mockDamageComponent =
                mock(com.example.rpgplugin.skill.component.EffectComponent.class);
        com.example.rpgplugin.skill.component.ComponentSettings mockSettings =
                mock(com.example.rpgplugin.skill.component.ComponentSettings.class);

        when(damageSkill.findComponentByKey("damage")).thenReturn(mockDamageComponent);
        when(mockDamageComponent.getSettings()).thenReturn(mockSettings);
        when(mockSettings.has("value")).thenReturn(true);
        when(mockSettings.getString("value", "0")).thenReturn("10");
        when(mockSettings.has("stat_multiplier")).thenReturn(false);

        // SkillTargetモック
        com.example.rpgplugin.skill.target.SkillTarget mockSkillTarget =
                mock(com.example.rpgplugin.skill.target.SkillTarget.class);
        when(damageSkill.getTargetFromComponents()).thenReturn(mockSkillTarget);
        when(mockSkillTarget.getRange()).thenReturn(10.0);
        when(mockSkillTarget.getType()).thenReturn(TargetType.NEAREST_HOSTILE);

        when(mockSkillManager.checkCooldown(any(Player.class), eq("damage_skill"))).thenReturn(true);

        boolean result = executor.execute(mockPlayer, damageSkill, 1);

        assertTrue(result, "複数ターゲットで成功すること");
    }

    @Test
    @DisplayName("executeAt: ダメージ設定null")
    void testExecuteAt_NullDamageConfig() {
        Skill noDamageSkill = new Skill(
                "no_damage", "no_damage", "ダメージなし", SkillType.NORMAL, List.of(),
                10, 0.0, 0, null, null, SkillCostType.MANA,
                null, null, null, List.of()
        );

        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        when(mockTarget.isValid()).thenReturn(true);
        when(mockSkillManager.checkCooldown(any(Player.class), eq("no_damage"))).thenReturn(true);

        boolean result = executor.executeAt(mockPlayer, noDamageSkill, 1, mockTarget);

        assertTrue(result, "ダメージ設定nullでも成功すること");
        verify(mockTarget, never()).damage(anyDouble(), any(Player.class));
    }

    @Test
    @DisplayName("executeWithCostType: damageがnull")
    void testExecuteWithCostType_NullDamage() {
        Skill noDamageSkill = new Skill(
                "no_damage", "no_damage", "ダメージなし", SkillType.NORMAL, List.of(),
                10, 0.0, 0, null, null, SkillCostType.MANA,
                null, null, null, List.of()
        );

        when(mockSkillManager.checkCooldown(any(Player.class), eq("no_damage"))).thenReturn(true);
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        // lenientを使用して不要なスタブ警告を回避
        lenient().when(mockRpgPlayer.hasMana(10)).thenReturn(true);
        lenient().when(mockRpgPlayer.consumeMana(10)).thenReturn(true);

        boolean result = executor.executeWithCostType(mockPlayer, noDamageSkill, 1, SkillCostType.MANA);

        assertTrue(result, "damageがnullでも成功すること");
    }
}
