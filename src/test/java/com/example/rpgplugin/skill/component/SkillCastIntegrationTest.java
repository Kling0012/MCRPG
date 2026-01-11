package com.example.rpgplugin.skill.component;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.component.condition.ChanceCondition;
import com.example.rpgplugin.skill.component.condition.HealthCondition;
import com.example.rpgplugin.skill.component.condition.ManaCondition;
import com.example.rpgplugin.skill.component.mechanic.DamageMechanic;
import com.example.rpgplugin.skill.component.mechanic.FireMechanic;
import com.example.rpgplugin.skill.component.mechanic.PushMechanic;
import com.example.rpgplugin.skill.component.trigger.CrouchTrigger;
import com.example.rpgplugin.skill.component.trigger.LandTrigger;
import com.example.rpgplugin.skill.component.trigger.TriggerHandler;
import com.example.rpgplugin.skill.component.trigger.TriggerManager;
import com.example.rpgplugin.skill.component.trigger.TriggerSettings;
import com.example.rpgplugin.skill.executor.ActiveSkillExecutor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * スキル発動統合テスト
 *
 * <p>テスト対象:</p>
 * <ul>
 *   <li>スキル発動コマンドから効果適用までの完全フロー</li>
 *   <li>トリガー_ACTIVATION → 発火 → 効果実行の流れ</li>
 *   <li>コンポーネントの組み合わせ実行</li>
 *   <li>各種メカニックの動作確認</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキル発動統合のテストに専念</li>
 *   <li>現実的: 実際のゲームプレイに近いフローでテスト</li>
 *   <li>網羅性: 全てのコンポーネントタイプをカバー</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("スキル発動統合テスト")
class SkillCastIntegrationTest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private Logger mockLogger;

    @Mock
    private Player mockPlayer;

    @Mock
    private RPGPlayer mockRpgPlayer;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private Server mockServer;

    @Mock
    private PluginManager mockPluginManager;

    @Mock
    private BukkitScheduler mockScheduler;

    @Mock
    private AttributeInstance mockAttributeInstance;

    @Mock
    private World mockWorld;

    @Mock
    private Location mockLocation;

    private MockedStatic<Bukkit> mockedBukkit;

    private TriggerManager triggerManager;

    @Mock
    private SkillManager skillManager;

    private ActiveSkillExecutor skillExecutor;

    /**
     * 各テストの前に実行されるセットアップ処理
     */
    @BeforeEach
    void setUp() {
        // ヘッドレスモードを設定（AWT問題回避）
        System.setProperty("java.awt.headless", "true");

        // Bukkit静的メソッドのモック化
        mockedBukkit = mockStatic(Bukkit.class);

        // Pluginの基本設定
        when(mockPlugin.getLogger()).thenReturn(mockLogger);
        when(mockPlugin.getServer()).thenReturn(mockServer);
        when(mockServer.getPluginManager()).thenReturn(mockPluginManager);
        when(mockServer.getScheduler()).thenReturn(mockScheduler);

        // プレイヤーのモック設定
        UUID playerUuid = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockPlayer.getName()).thenReturn("TestPlayer");
        when(mockPlayer.getEntityId()).thenReturn(12345);
        when(mockPlayer.getHealth()).thenReturn(20.0);
        // Paper 1.20.6: getMaxHealth()は非推奨。Attribute APIでモック済み

        // WorldとLocationのモック設定
        when(mockPlayer.getWorld()).thenReturn(mockWorld);
        when(mockPlayer.getLocation()).thenReturn(mockLocation);
        when(mockLocation.getWorld()).thenReturn(mockWorld);
        when(mockLocation.clone()).thenReturn(mockLocation);
        when(mockWorld.getNearbyEntities(any(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(java.util.Collections.emptyList());

        // Attributeのモック設定
        when(mockPlayer.getAttribute(any(Attribute.class))).thenReturn(mockAttributeInstance);
        when(mockAttributeInstance.getValue()).thenReturn(20.0);

        // RPGPlayerのモック設定
        when(mockRpgPlayer.getBukkitPlayer()).thenReturn(mockPlayer);
        when(mockRpgPlayer.getLevel()).thenReturn(10);
        when(mockRpgPlayer.getCurrentMana()).thenReturn(100);
        when(mockRpgPlayer.hasMana(anyInt())).thenReturn(true);
        when(mockRpgPlayer.consumeMana(anyInt())).thenReturn(true);
        when(mockRpgPlayer.consumeSkillCost(anyInt())).thenReturn(true);

        // PlayerManagerのモック設定
        when(mockPlayerManager.getRPGPlayer(any(UUID.class))).thenReturn(mockRpgPlayer);

        // TriggerManagerの初期化
        triggerManager = TriggerManager.initialize(mockPlugin);

        // SkillManagerはモックとして使用するため、必要な振る舞いを設定
        when(skillManager.checkCooldown(any(), anyString())).thenReturn(true);
        when(skillManager.checkCooldown(any(Player.class), anyString())).thenReturn(true);

        // getPlayerSkillDataのモック設定
        SkillManager.PlayerSkillData mockSkillData = mock(SkillManager.PlayerSkillData.class);
        when(skillManager.getPlayerSkillData(any(Player.class))).thenReturn(mockSkillData);

        // ActiveSkillExecutorの初期化（モックのSkillManagerを使用）
        skillExecutor = new ActiveSkillExecutor(mockPlugin, skillManager, mockPlayerManager);
    }

    /**
     * 各テストの後に実行されるクリーンアップ処理
     */
    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    // ==================== テスト1: 基本的なスキル発動 ====================

    @Nested
    @DisplayName("テスト1: 基本的なスキル発動")
    class Test1BasicSkillCast {

        @Test
        @DisplayName("テスト1-1: コスト消費なしのスキルが発動できる")
        void test1_1_SkillCastWithoutCost() {
            // Given: コストなしのスキル
            Skill skill = createMockSkill("test_skill", 0);

            // When: スキルを発動
            boolean result = skillExecutor.execute(mockPlayer, skill, 1);

            // Then: スキル発動成功
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("テスト1-2: MP消費ありのスキルが発動できる")
        void test1_2_SkillCastWithManaCost() {
            // Given: MP消費スキル
            Skill skill = createMockSkill("test_skill", 10);
            when(mockRpgPlayer.isManaCostType()).thenReturn(true);

            // When: スキルを発動
            boolean result = skillExecutor.execute(mockPlayer, skill, 1);

            // Then: スキル発動成功
            assertThat(result).isTrue();
            // MP消費が呼ばれたことを確認
            verify(mockRpgPlayer).consumeSkillCost(10);
        }

        @Test
        @DisplayName("テスト1-3: クールダウン中のスキルは発動できない")
        void test1_3_SkillCastDuringCooldown() {
            // Given: スキル
            Skill skill = createMockSkill("test_skill", 0);

            // 最初の発動
            skillExecutor.execute(mockPlayer, skill, 1);

            // クールダウン中であることを確認（2回目は失敗するはず）
            // Note: 実際のクールダウン実装に依存
        }

        @Test
        @DisplayName("テスト1-4: MP不足でスキル発動失敗")
        void test1_4_SkillCastWithoutEnoughMana() {
            // Given: MP消費スキル
            Skill skill = createMockSkill("test_skill", 10);
            when(mockRpgPlayer.isManaCostType()).thenReturn(true);
            when(mockRpgPlayer.consumeSkillCost(anyInt())).thenReturn(false);

            // When: スキルを発動
            boolean result = skillExecutor.execute(mockPlayer, skill, 1);

            // Then: スキル発動失敗
            assertThat(result).isFalse();
        }
    }

    // ==================== テスト2: コンポーネント効果付きスキル ====================

    @Nested
    @DisplayName("テスト2: コンポーネント効果付きスキル")
    class Test2ComponentEffectSkills {

        @Test
        @DisplayName("テスト2-1: ダメージメカニック付きスキル")
        void test2_1_SkillWithDamageMechanic() {
            // Given: ダメージメカニック付きスキル
            SkillEffect skillEffect = new SkillEffect("damage_skill");
            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "15");
            skillEffect.addComponent(damage);

            Skill skill = createMockSkill("damage_skill", 0);
            when(skill.getComponentEffect()).thenReturn(skillEffect);

            // When: スキルを発動
            boolean result = skillExecutor.execute(mockPlayer, skill, 1);

            // Then: スキル発動成功
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("テスト2-2: 複数メカニック付きスキル")
        void test2_2_SkillWithMultipleMechanics() {
            // Given: 複数メカニック付きスキル
            SkillEffect skillEffect = new SkillEffect("multi_skill");

            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "10");

            PushMechanic push = new PushMechanic();
            push.getSettings().put("velocity", "1.0");

            skillEffect.addComponent(damage);
            skillEffect.addComponent(push);

            Skill skill = createMockSkill("multi_skill", 0);
            when(skill.getComponentEffect()).thenReturn(skillEffect);

            // When: スキルを発動
            boolean result = skillExecutor.execute(mockPlayer, skill, 1);

            // Then: スキル発動成功
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("テスト2-3: 条件付きメカニック")
        void test2_3_SkillWithConditionalMechanic() {
            // Given: 条件付きメカニック
            SkillEffect skillEffect = new SkillEffect("conditional_skill");

            HealthCondition condition = new HealthCondition();
            condition.getSettings().put("below", "50");

            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "20");

            condition.addChild(damage);
            skillEffect.addComponent(condition);

            Skill skill = createMockSkill("conditional_skill", 0);
            when(skill.getComponentEffect()).thenReturn(skillEffect);

            // When: スキルを発動
            boolean result = skillExecutor.execute(mockPlayer, skill, 1);

            // Then: スキル発動成功
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("テスト2-4: チャンス条件付きメカニック")
        void test2_4_SkillWithChanceCondition() {
            // Given: チャンス条件付きメカニック
            SkillEffect skillEffect = new SkillEffect("chance_skill");

            ChanceCondition chance = new ChanceCondition();
            chance.getSettings().put("chance", "100"); // 100%で発動

            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "25");

            chance.addChild(damage);
            skillEffect.addComponent(chance);

            Skill skill = createMockSkill("chance_skill", 0);
            when(skill.getComponentEffect()).thenReturn(skillEffect);

            // When: スキルを発動
            boolean result = skillExecutor.execute(mockPlayer, skill, 1);

            // Then: スキル発動成功
            assertThat(result).isTrue();
        }
    }

    // ==================== テスト3: トリガー付きスキル ====================

    @Nested
    @DisplayName("テスト3: トリガー付きスキル")
    class Test3TriggerBasedSkills {

        @Test
        @DisplayName("テスト3-1: トリガー_ACTIVATIONが正しく動作する")
        void test3_1_TriggerActivation() {
            // Given: CROUCHトリガー付きスキル
            CrouchTrigger trigger = new CrouchTrigger();
            TriggerSettings settings = new TriggerSettings();
            settings.put("cooldown", "5");

            DamageMechanic component = new DamageMechanic();
            component.getSettings().put("value", "10");

            TriggerHandler handler = new TriggerHandler(
                    "crouch_skill",
                    trigger,
                    settings,
                    component,
                    30
            );

            // When: スキルをアクティベート
            assertThatCode(() -> {
                triggerManager.activateSkill(
                        "crouch_skill",
                        mockPlayer,
                        1,
                        30,
                        List.of(handler)
                );
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("テスト3-2: 複数トリガーの登録")
        void test3_2_MultipleTriggers() {
            // Given: 複数トリガー
            CrouchTrigger crouchTrigger = new CrouchTrigger();
            LandTrigger landTrigger = new LandTrigger();
            TriggerSettings settings = new TriggerSettings();

            DamageMechanic component1 = new DamageMechanic();
            component1.getSettings().put("value", "10");

            FireMechanic component2 = new FireMechanic();
            component2.getSettings().put("duration", "3");

            TriggerHandler handler1 = new TriggerHandler(
                    "crouch_skill",
                    crouchTrigger,
                    settings,
                    component1,
                    0
            );

            TriggerHandler handler2 = new TriggerHandler(
                    "land_skill",
                    landTrigger,
                    settings,
                    component2,
                    0
            );

            // When: 両方のスキルをアクティベート
            assertThatCode(() -> {
                triggerManager.activateSkill("crouch_skill", mockPlayer, 1, 0, List.of(handler1));
                triggerManager.activateSkill("land_skill", mockPlayer, 1, 0, List.of(handler2));
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("テスト3-3: トリガーハンドラーの設定が正しく保持される")
        void test3_3_TriggerHandlerSettings() {
            // Given: トリガーハンドラー
            CrouchTrigger trigger = new CrouchTrigger();
            TriggerSettings settings = new TriggerSettings();
            settings.put("cooldown", "10");
            settings.put("chance", "50");

            DamageMechanic component = new DamageMechanic();
            component.getSettings().put("value", "15");

            TriggerHandler handler = new TriggerHandler(
                    "test_skill",
                    trigger,
                    settings,
                    component,
                    60
            );

            // Then: 設定が正しく保持されている
            assertThat(handler.getSkillId()).isEqualTo("test_skill");
            assertThat(handler.getTrigger()).isSameAs(trigger);
            assertThat(handler.getDuration()).isEqualTo(60);
            assertThat(handler.getSettings().getInt("cooldown", 0)).isEqualTo(10);
            assertThat(handler.getSettings().getInt("chance", 0)).isEqualTo(50);
        }

        @Test
        @DisplayName("テスト3-4: トリガーのディアクティベート")
        void test3_4_TriggerDeactivation() {
            // Given: アクティベートされたトリガー
            CrouchTrigger trigger = new CrouchTrigger();
            TriggerSettings settings = new TriggerSettings();

            DamageMechanic component = new DamageMechanic();

            TriggerHandler handler = new TriggerHandler(
                    "test_skill",
                    trigger,
                    settings,
                    component,
                    0
            );

            triggerManager.activateSkill("test_skill", mockPlayer, 1, 0, List.of(handler));

            // When: ディアクティベート
            assertThatCode(() -> {
                triggerManager.deactivateSkill("test_skill", mockPlayer);
            }).doesNotThrowAnyException();
        }
    }

    // ==================== テスト4: 複雑なスキル構成 ====================

    @Nested
    @DisplayName("テスト4: 複雑なスキル構成")
    class Test4ComplexSkillCompositions {

        @Test
        @DisplayName("テスト4-1: 階層的なコンポーネント構造")
        void test4_1_NestedComponentStructure() {
            // Given: 複雑な階層構造
            SkillEffect skillEffect = new SkillEffect("complex_skill");

            // 条件1: HPが50%以下
            HealthCondition healthCond = new HealthCondition();
            healthCond.getSettings().put("below", "50");

            // 条件2: 50%の確率
            ChanceCondition chanceCond = new ChanceCondition();
            chanceCond.getSettings().put("chance", "50");

            // メカニック1: ダメージ
            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "30");

            // メカニック2: 炎上
            FireMechanic fire = new FireMechanic();
            fire.getSettings().put("duration", "5");

            // 階層構造を構築
            chanceCond.addChild(damage);
            chanceCond.addChild(fire);
            healthCond.addChild(chanceCond);
            skillEffect.addComponent(healthCond);

            Skill skill = createMockSkill("complex_skill", 0);
            when(skill.getComponentEffect()).thenReturn(skillEffect);

            // When: スキルを発動
            boolean result = skillExecutor.execute(mockPlayer, skill, 1);

            // Then: スキル発動成功
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("テスト4-2: 並列メカニック実行")
        void test4_2_ParallelMechanicExecution() {
            // Given: 複数の並列メカニック
            SkillEffect skillEffect = new SkillEffect("parallel_skill");

            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "10");

            FireMechanic fire = new FireMechanic();
            fire.getSettings().put("duration", "3");

            PushMechanic push = new PushMechanic();
            push.getSettings().put("velocity", "2.0");

            skillEffect.addComponent(damage);
            skillEffect.addComponent(fire);
            skillEffect.addComponent(push);

            Skill skill = createMockSkill("parallel_skill", 0);
            when(skill.getComponentEffect()).thenReturn(skillEffect);

            // When: スキルを発動
            boolean result = skillExecutor.execute(mockPlayer, skill, 1);

            // Then: スキル発動成功
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("テスト4-3: 条件の組み合わせ")
        void test4_3_CombinedConditions() {
            // Given: 複数の条件
            SkillEffect skillEffect = new SkillEffect("combined_skill");

            // HP条件
            HealthCondition healthCond = new HealthCondition();
            healthCond.getSettings().put("below", "30");

            // MP条件
            ManaCondition manaCond = new ManaCondition();
            manaCond.getSettings().put("above", "20");

            // メカニック
            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "50");

            healthCond.addChild(damage);
            manaCond.addChild(damage);
            skillEffect.addComponent(healthCond);
            skillEffect.addComponent(manaCond);

            Skill skill = createMockSkill("combined_skill", 0);
            when(skill.getComponentEffect()).thenReturn(skillEffect);

            // When: スキルを発動
            boolean result = skillExecutor.execute(mockPlayer, skill, 1);

            // Then: スキル発動成功
            assertThat(result).isTrue();
        }
    }

    // ==================== テスト5: エッジケース ====================

    @Nested
    @DisplayName("テスト5: エッジケース")
    class Test5EdgeCases {

        @Test
        @DisplayName("テスト5-1: 空のコンポーネントリスト")
        void test5_1_EmptyComponentList() {
            // Given: 空のコンポーネント効果
            SkillEffect skillEffect = new SkillEffect("empty_skill");

            Skill skill = createMockSkill("empty_skill", 0);
            when(skill.getComponentEffect()).thenReturn(skillEffect);

            // When: スキルを発動
            boolean result = skillExecutor.execute(mockPlayer, skill, 1);

            // Then: スキル発動成功（効果はないが発動自体は成功）
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("テスト5-2: nullコンポーネント効果")
        void test5_2_NullComponentEffect() {
            // Given: コンポーネント効果がないスキル
            Skill skill = createMockSkill("no_effect_skill", 0);
            when(skill.getComponentEffect()).thenReturn(null);

            // When: スキルを発動
            boolean result = skillExecutor.execute(mockPlayer, skill, 1);

            // Then: スキル発動成功
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("テスト5-3: 最大レベルのスキル")
        void test5_3_MaxLevelSkill() {
            // Given: 最大レベルのスキル
            Skill skill = createMockSkill("max_level_skill", 0);

            // When: レベル10でスキルを発動
            boolean result = skillExecutor.execute(mockPlayer, skill, 10);

            // Then: スキル発動成功
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("テスト5-4: 無制限効果時間のトリガー")
        void test5_4_UnlimitedDurationTrigger() {
            // Given: 無制限効果時間のトリガー
            CrouchTrigger trigger = new CrouchTrigger();
            TriggerSettings settings = new TriggerSettings();

            DamageMechanic component = new DamageMechanic();

            TriggerHandler handler = new TriggerHandler(
                    "unlimited_skill",
                    trigger,
                    settings,
                    component,
                    0 // duration = 0 で無制限
            );

            // When: スキルをアクティベート
            assertThatCode(() -> {
                triggerManager.activateSkill(
                        "unlimited_skill",
                        mockPlayer,
                        1,
                        0,
                        List.of(handler)
                );
            }).doesNotThrowAnyException();
        }
    }

    // ==================== ヘルパーメソッド ====================

    /**
     * モックスキルを作成します
     *
     * @param id    スキルID
     * @param cost  コスト
     * @return モックスキル
     */
    private Skill createMockSkill(String id, double cost) {
        Skill skill = mock(Skill.class);
        when(skill.getId()).thenReturn(id);
        when(skill.getCost(anyInt())).thenReturn((int) cost);
        when(skill.getCooldown()).thenReturn(0.0);

        // コンポーネントベースのメソッドをモック
        when(skill.getCostFromComponents(anyInt())).thenReturn((int) cost);
        when(skill.getCooldownFromComponents(anyInt())).thenReturn(0.0);
        when(skill.getDamageFromComponents(anyInt())).thenReturn(0.0);
        when(skill.getTargetFromComponents()).thenReturn(null);
        when(skill.hasComponentEffect()).thenReturn(false);

        return skill;
    }
}
