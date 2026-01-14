package com.example.rpgplugin.skill.executor;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.LevelDependentParameter;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillType;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    private ActiveSkillExecutor executor;
    private UUID testUuid;
    private Skill testSkill;

    @BeforeEach
    void setUp() {
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
        Skill.DamageCalculation damage = new Skill.DamageCalculation(10.0, Stat.STRENGTH, 1.0, 0.0);
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

    // ==================== ゲッター テスト ====================

    @Test
    @DisplayName("isEnemy: プレイヤーは敵対的でない")
    void testIsEnemy_Player() {
        // isEnemyはprivateメソッドなのでテストできない
        // executeメソッドの挙動を通じて検証
        lenient().when(mockSkillManager.checkCooldown(any(Player.class), eq("test_skill"))).thenReturn(true);
        lenient().when(mockRpgPlayer.consumeSkillCost(0)).thenReturn(true);

        boolean result = executor.execute(mockPlayer, testSkill, 1);

        assertTrue(result, "成功すること（プレイヤーをターゲットにしない）");
    }

    // ==================== ゲッター ====================

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
}
