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
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * PassiveSkillExecutorの単体テスト
 *
 * <p>パッシブスキル実行エグゼキューターの機能テスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("PassiveSkillExecutor テスト")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PassiveSkillExecutorTest {

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
    private Server mockServer;
    @Mock
    private BukkitScheduler mockScheduler;

    private MockedStatic<Bukkit> mockedBukkit;
    private PassiveSkillExecutor executor;
    private UUID testUuid;
    private Skill testSkill;

    @BeforeEach
    void setUp() {
        // Bukkit.getScheduler()のモック設定
        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(() -> Bukkit.getScheduler()).thenReturn(mockScheduler);

        // モック設定
        lenient().when(mockPlugin.getServer()).thenReturn(mockServer);
        lenient().when(mockServer.getScheduler()).thenReturn(mockScheduler);
        lenient().when(mockScheduler.runTaskTimer(eq(mockPlugin), any(org.bukkit.scheduler.BukkitRunnable.class), anyLong(), anyLong()))
                .thenReturn(mock(org.bukkit.scheduler.BukkitTask.class));

        executor = new PassiveSkillExecutor(mockPlugin, mockSkillManager, mockPlayerManager);
        testUuid = UUID.randomUUID();

        // lenient() - 全てのテストで使用されないスタブ
        lenient().when(mockPlayer.getUniqueId()).thenReturn(testUuid);
        lenient().when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        lenient().when(mockRpgPlayer.getBukkitPlayer()).thenReturn(mockPlayer);
        lenient().when(mockRpgPlayer.getStatManager()).thenReturn(mockStatManager);
        lenient().when(mockStatManager.getFinalStat(any(Stat.class))).thenReturn(10);

        testSkill = createTestSkill("passive_skill", "パッシブスキル");
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    private Skill createTestSkill(String id, String displayName) {
        Skill.DamageCalculation damage = new Skill.DamageCalculation(5.0, Stat.STRENGTH, 1.0, 0.0);
        LevelDependentParameter cooldownParam = new LevelDependentParameter(0.0, 0.0, null, null);
        LevelDependentParameter costParam = new LevelDependentParameter(0.0, 0.0, null, null);

        return new Skill(
                id, id, displayName, SkillType.NORMAL, List.of(),
                10, 0.0, 0, cooldownParam, costParam,
                SkillCostType.MANA, damage, null, null, List.of()
        );
    }

    // ==================== execute テスト ====================

    @Test
    @DisplayName("execute: パッシブスキルは手動発動不可")
    void testExecute_PassiveSkill() {
        boolean result = executor.execute(mockPlayer, testSkill, 1);

        assertFalse(result, "パッシブスキルは手動発動できない");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    // ==================== applyPassive テスト ====================

    @Test
    @DisplayName("applyPassive: 新規適用")
    void testApplyPassive_New() {
        boolean result = executor.applyPassive(mockPlayer, testSkill, 1);

        assertTrue(result, "新規適用は成功すること");
        verify(mockPlayer).sendMessage(any(Component.class));

        Map<UUID, Map<String, PassiveSkillExecutor.PassiveEffect>> effects = executor.getActiveEffects();
        assertTrue(effects.containsKey(testUuid), "プレイヤー効果が登録されている");
        assertTrue(effects.get(testUuid).containsKey("passive_skill"), "スキル効果が適用されている");
    }

    @Test
    @DisplayName("applyPassive: 重複適用")
    void testApplyPassive_AlreadyApplied() {
        executor.applyPassive(mockPlayer, testSkill, 1);

        boolean result = executor.applyPassive(mockPlayer, testSkill, 1);

        assertFalse(result, "重複適用は失敗すること");
    }

    // ==================== removePassive テスト ====================

    @Test
    @DisplayName("removePassive: 削除成功")
    void testRemovePassive_Success() {
        executor.applyPassive(mockPlayer, testSkill, 1);

        executor.removePassive(mockPlayer, "passive_skill");

        Map<UUID, Map<String, PassiveSkillExecutor.PassiveEffect>> effects = executor.getActiveEffects();
        assertTrue(effects.containsKey(testUuid), "プレイヤー効果は残っている");
        assertFalse(effects.get(testUuid).containsKey("passive_skill"), "スキル効果は削除されている");
        verify(mockPlayer, times(2)).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("removePassive: 未適用スキル削除")
    void testRemovePassive_NotApplied() {
        // 削除してもエラーにならない
        executor.removePassive(mockPlayer, "unknown_skill");

        // 何も起きない
        verify(mockPlayer, never()).sendMessage(any(Component.class));
    }

    // ==================== clearAllPassives テスト ====================

    @Test
    @DisplayName("clearAllPassives: 全削除")
    void testClearAllPassives() {
        Skill skill2 = createTestSkill("passive2", "パッシブ2");
        executor.applyPassive(mockPlayer, testSkill, 1);
        executor.applyPassive(mockPlayer, skill2, 1);

        executor.clearAllPassives(mockPlayer);

        Map<UUID, Map<String, PassiveSkillExecutor.PassiveEffect>> effects = executor.getActiveEffects();
        assertFalse(effects.containsKey(testUuid), "プレイヤー効果は削除されている");
    }

    // ==================== getActiveEffects テスト ====================

    @Test
    @DisplayName("getActiveEffects: コピーを返す")
    void testGetActiveEffects_ReturnsCopy() {
        executor.applyPassive(mockPlayer, testSkill, 1);

        Map<UUID, Map<String, PassiveSkillExecutor.PassiveEffect>> effects = executor.getActiveEffects();
        effects.clear();

        // 元のマップには影響しない
        Map<UUID, Map<String, PassiveSkillExecutor.PassiveEffect>> effects2 = executor.getActiveEffects();
        assertTrue(effects2.containsKey(testUuid), "元のマップは影響を受けていない");
    }

    // ==================== PassiveEffect テスト ====================

    @Test
    @DisplayName("PassiveEffect: nullスキル")
    void testPassiveEffect_NullSkill() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PassiveSkillExecutor.PassiveEffect(null, 1, null, null);
        }, "nullスキルは例外");
    }

    @Test
    @DisplayName("PassiveEffect: ゲッター取得")
    void testPassiveEffect_Getters() {
        PassiveSkillExecutor.PassiveEffect effect = new PassiveSkillExecutor.PassiveEffect(
                testSkill, 5, null, null
        );

        assertEquals(testSkill, effect.getSkill());
        assertEquals(5, effect.getLevel());
        assertNull(effect.getModifier());
        assertNull(effect.getPotionEffect());
    }

    // ==================== ゲッター テスト ====================

    @Test
    @DisplayName("getPlugin: プラグイン取得")
    void testGetPlugin() {
        assertEquals(mockPlugin, executor.getPlugin());
    }

    @Test
    @DisplayName("getSkillManager: スキルマネージャー取得")
    void testGetSkillManager() {
        assertEquals(mockSkillManager, executor.getSkillManager());
    }

    @Test
    @DisplayName("getPlayerManager: プレイヤーマネージャー取得")
    void testGetPlayerManager() {
        assertEquals(mockPlayerManager, executor.getPlayerManager());
    }

    @Test
    @DisplayName("コンストラクタ: null引数")
    void testConstructor_NullArguments() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PassiveSkillExecutor(null, mockSkillManager, mockPlayerManager);
        }, "plugin=nullは例外");

        assertThrows(IllegalArgumentException.class, () -> {
            new PassiveSkillExecutor(mockPlugin, null, mockPlayerManager);
        }, "skillManager=nullは例外");

        assertThrows(IllegalArgumentException.class, () -> {
            new PassiveSkillExecutor(mockPlugin, mockSkillManager, null);
        }, "playerManager=nullは例外");
    }
}
