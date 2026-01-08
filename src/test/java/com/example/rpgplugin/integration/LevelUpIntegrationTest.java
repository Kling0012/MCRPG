package com.example.rpgplugin.integration;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.LevelDependentParameter;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
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

import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * レベルアップによるパラメータ変化の結合テスト
 *
 * <p>テスト対象:</p>
 * <ul>
 *   <li>スキルLV上昇 → パラメータ変化確認</li>
 *   <li>クールダウン短縮</li>
 *   <li>コスト増加</li>
 *   <li>ダメージ増加</li>
 *   <li>上限/下限の適用</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: レベル依存パラメータのテストに専念</li>
 *   <li>現実的: 実際のスキルレベルアップを模倣</li>
 *   <li>独立性: 各テストは独立して実行可能</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("レベルアップ結合テスト")
class LevelUpIntegrationTest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private Logger mockLogger;

    @Mock
    private Player mockPlayer;

    @Mock
    private RPGPlayer mockRpgPlayer;

    private MockedStatic<Bukkit> mockedBukkit;

    private SkillManager skillManager;

    /**
     * 各テストの前に実行されるセットアップ処理
     */
    @BeforeEach
    void setUp() {
        // Bukkit静的メソッドのモック化
        mockedBukkit = mockStatic(Bukkit.class);

        // Pluginの基本設定
        when(mockPlugin.getLogger()).thenReturn(mockLogger);

        // プレイヤーのモック設定
        UUID playerUuid = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockPlayer.getName()).thenReturn("TestPlayer");

        // RPGPlayerのモック設定
        when(mockRpgPlayer.getBukkitPlayer()).thenReturn(mockPlayer);
        when(mockRpgPlayer.getLevel()).thenReturn(10);

        // SkillManagerの初期化
        skillManager = new SkillManager(mockPlugin);
    }

    /**
     * 各テストの後に実行されるクリーンアップ処理
     */
    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
        skillManager.clearAllSkills();
        skillManager.clearAllPlayerData();
    }

    // ==================== シナリオ1: クールダウン短縮 ====================

    @Nested
    @DisplayName("シナリオ1: レベルアップによるクールダウン短縮")
    class Scenario1CooldownReduction {

        @Test
        @DisplayName("シナリオ1-1: レベルアップでクールダウンが短縮される")
        void test1_1_CooldownDecreasesWithLevel() {
            // Given: レベルでクールダウンが短くなるスキル
            Skill skill = createCooldownReductionSkill();
            skillManager.registerSkill(skill);

            // When: 各レベルのクールダウンを取得
            double cdLv1 = skill.getCooldown(1);
            double cdLv3 = skill.getCooldown(3);
            double cdLv5 = skill.getCooldown(5);

            // Then: レベルが上がるほどクールダウンが短くなる
            assertThat(cdLv1).isEqualTo(10.0);
            assertThat(cdLv3).isEqualTo(8.0);
            assertThat(cdLv5).isEqualTo(6.0);
        }

        @Test
        @DisplayName("シナリオ1-2: クールダウンの下限が正しく適用される")
        void test1_2_CooldownMinLimit() {
            // Given: 下限付きクールダウンスキル
            Skill skill = createCooldownReductionSkill();
            skillManager.registerSkill(skill);

            // When: 高レベルのクールダウンを取得
            double cdLv10 = skill.getCooldown(10);

            // Then: 下限値で止まる
            assertThat(cdLv10).isEqualTo(3.0); // min = 3.0
        }

        @Test
        @DisplayName("シナリオ1-3: 固定クールダウンスキルはレベルで変化しない")
        void test1_3_FixedCooldownDoesNotChange() {
            // Given: 固定クールダウンスキル
            Skill skill = createFixedCooldownSkill();
            skillManager.registerSkill(skill);

            // When: 各レベルのクールダウンを取得
            double cdLv1 = skill.getCooldown(1);
            double cdLv5 = skill.getCooldown(5);
            double cdLv10 = skill.getCooldown(10);

            // Then: 全て同じ値
            assertThat(cdLv1).isEqualTo(5.0);
            assertThat(cdLv5).isEqualTo(5.0);
            assertThat(cdLv10).isEqualTo(5.0);
        }
    }

    // ==================== シナリオ2: コスト増加 ====================

    @Nested
    @DisplayName("シナリオ2: レベルアップによるコスト増加")
    class Scenario2CostIncrease {

        @Test
        @DisplayName("シナリオ2-1: レベルアップでコストが増加する")
        void test2_1_CostIncreasesWithLevel() {
            // Given: レベルでコストが増えるスキル
            Skill skill = createCostIncreaseSkill();
            skillManager.registerSkill(skill);

            // When: 各レベルのコストを取得
            int costLv1 = skill.getCost(1);
            int costLv3 = skill.getCost(3);
            int costLv5 = skill.getCost(5);

            // Then: レベルが上がるほどコストが増える
            assertThat(costLv1).isEqualTo(10);
            assertThat(costLv3).isEqualTo(14);
            assertThat(costLv5).isEqualTo(18);
        }

        @Test
        @DisplayName("シナリオ2-2: コストの上限が正しく適用される")
        void test2_2_CostMaxLimit() {
            // Given: 上限付きコストスキル
            Skill skill = createCostIncreaseSkill();
            skillManager.registerSkill(skill);

            // When: 高レベルのコストを取得
            int costLv10 = skill.getCost(10);

            // Then: 上限値で止まる
            assertThat(costLv10).isEqualTo(30); // max = 30
        }

        @Test
        @DisplayName("シナリオ2-3: 固定コストスキルはレベルで変化しない")
        void test2_3_FixedCostDoesNotChange() {
            // Given: 固定コストスキル
            Skill skill = createFixedCostSkill();
            skillManager.registerSkill(skill);

            // When: 各レベルのコストを取得
            int costLv1 = skill.getCost(1);
            int costLv5 = skill.getCost(5);

            // Then: 全て同じ値
            assertThat(costLv1).isEqualTo(20);
            assertThat(costLv5).isEqualTo(20);
        }
    }

    // ==================== シナリオ3: ダメージ増加 ====================

    @Nested
    @DisplayName("シナリオ3: レベルアップによるダメージ増加")
    class Scenario3DamageIncrease {

        @Test
        @DisplayName("シナリオ3-1: レベルアップでダメージが増加する")
        void test3_1_DamageIncreasesWithLevel() {
            // Given: レベル倍率付きダメージスキル
            Skill skill = createDamageSkill();
            skillManager.registerSkill(skill);

            // When: 各レベルのダメージを計算
            double damageLv1 = skillManager.calculateDamage(skill, 1, mockRpgPlayer);
            double damageLv3 = skillManager.calculateDamage(skill, 3, mockRpgPlayer);
            double damageLv5 = skillManager.calculateDamage(skill, 5, mockRpgPlayer);

            // Then: レベルが上がるほどダメージが増える
            assertThat(damageLv1).isLessThan(damageLv3);
            assertThat(damageLv3).isLessThan(damageLv5);
        }

        @Test
        @DisplayName("シナリオ3-2: レベル倍率が正しく計算される")
        void test3_2_LevelMultiplierCalculation() {
            // Given: 基本ダメージ50、レベル倍率10のスキル
            Skill skill = createDamageSkill();
            skillManager.registerSkill(skill);

            // When: レベル1とレベル5のダメージ差を計算
            double damageLv1 = skillManager.calculateDamage(skill, 1, mockRpgPlayer);
            double damageLv5 = skillManager.calculateDamage(skill, 5, mockRpgPlayer);
            double difference = damageLv5 - damageLv1;

            // Then: 差分はレベル差×倍率（4×10=40）に近い値
            assertThat(difference).isGreaterThan(35); // ステータス補正もあるため許容範囲
        }
    }

    // ==================== シナリオ4: LevelDependentParameterの動作確認 ====================

    @Nested
    @DisplayName("シナリオ4: LevelDependentParameterの動作確認")
    class Scenario4LevelDependentParameter {

        @Test
        @DisplayName("シナリオ4-1: 増加パラメータが正しく計算される")
        void test4_1_IncreasingParameterCalculation() {
            // Given: base=10, per_level=5の増加パラメータ
            LevelDependentParameter ldp = new LevelDependentParameter(10.0, 5.0, null, null);

            // When: 各レベルの値を取得
            double valLv1 = ldp.getValue(1);
            double valLv3 = ldp.getValue(3);
            double valLv5 = ldp.getValue(5);

            // Then: レベルに応じて増加
            assertThat(valLv1).isEqualTo(10.0);
            assertThat(valLv3).isEqualTo(20.0);
            assertThat(valLv5).isEqualTo(30.0);
        }

        @Test
        @DisplayName("シナリオ4-2: 減少パラメータが正しく計算される")
        void test4_2_DecreasingParameterCalculation() {
            // Given: base=10, per_level=-1の減少パラメータ
            LevelDependentParameter ldp = new LevelDependentParameter(10.0, -1.0, 3.0, null);

            // When: 各レベルの値を取得
            double valLv1 = ldp.getValue(1);
            double valLv5 = ldp.getValue(5);
            double valLv10 = ldp.getValue(10);

            // Then: レベルで減少し、下限で止まる
            assertThat(valLv1).isEqualTo(10.0);
            assertThat(valLv5).isEqualTo(6.0);
            assertThat(valLv10).isEqualTo(3.0); // min = 3.0
        }

        @Test
        @DisplayName("シナリオ4-3: 上限が正しく適用される")
        void test4_3_MaxLimitApplied() {
            // Given: base=10, per_level=5, max=30のパラメータ
            LevelDependentParameter ldp = new LevelDependentParameter(10.0, 5.0, null, 30.0);

            // When: 高レベルの値を取得
            double valLv5 = ldp.getValue(5);
            double valLv10 = ldp.getValue(10);

            // Then: 上限で止まる
            assertThat(valLv5).isEqualTo(30.0); // 10 + 5*5 = 35 → 30で上限
            assertThat(valLv10).isEqualTo(30.0);
        }

        @Test
        @DisplayName("シナリオ4-4: 下限が正しく適用される")
        void test4_4_MinLimitApplied() {
            // Given: base=10, per_level=-2, min=2のパラメータ
            LevelDependentParameter ldp = new LevelDependentParameter(10.0, -2.0, 2.0, null);

            // When: 高レベルの値を取得
            double valLv3 = ldp.getValue(3);
            double valLv5 = ldp.getValue(5);
            double valLv10 = ldp.getValue(10);

            // Then: 下限で止まる
            assertThat(valLv3).isEqualTo(6.0);
            assertThat(valLv5).isEqualTo(2.0); // min = 2.0
            assertThat(valLv10).isEqualTo(2.0);
        }
    }

    // ==================== シナリオ5: スキルレベルアップの統合テスト ====================

    @Nested
    @DisplayName("シナリオ5: スキルレベルアップの統合テスト")
    class Scenario5SkillLevelUpIntegration {

        @Test
        @DisplayName("シナリオ5-1: スキル習得からレベルアップまでの流れ")
        void test5_1_SkillAcquisitionToLevelUp() {
            // Given: スキルを登録
            Skill skill = createCooldownReductionSkill();
            skillManager.registerSkill(skill);

            // プレイヤーがスキルを習得
            when(mockRpgPlayer.hasSkill("quick_strike")).thenReturn(true);

            // When: レベル1からレベル3に上げる
            when(mockRpgPlayer.getSkillLevel("quick_strike")).thenReturn(1);
            double cdLv1 = skill.getCooldown(1);

            when(mockRpgPlayer.getSkillLevel("quick_strike")).thenReturn(3);
            double cdLv3 = skill.getCooldown(3);

            // Then: クールダウンが短縮されている
            assertThat(cdLv1).isEqualTo(10.0);
            assertThat(cdLv3).isEqualTo(8.0);
            assertThat(cdLv3).isLessThan(cdLv1);
        }

        @Test
        @DisplayName("シナリオ5-2: 最大レベルでのパラメータ確認")
        void test5_2_MaxLevelParameters() {
            // Given: 最大レベル5のスキル
            Skill skill = createCooldownReductionSkill();

            // When: 最大レベルのパラメータを取得
            double cdMax = skill.getCooldown(5);
            int costMax = skill.getCost(5);

            // Then: 最大レベルの値が正しい
            assertThat(cdMax).isEqualTo(6.0); // 10 - 1*4 = 6
            assertThat(costMax).isEqualTo(18); // 10 + 2*4 = 18
        }

        @Test
        @DisplayName("シナリオ5-3: 最大レベル超過時の挙動")
        void test5_3_OverflowMaxLevelBehavior() {
            // Given: 最大レベル5のスキル
            Skill skill = createCooldownReductionSkill();

            // When: 最大レベルを超える値を指定
            double cdLv10 = skill.getCooldown(10);
            int costLv10 = skill.getCost(10);

            // Then: 限界値（min/max）が適用される
            assertThat(cdLv10).isEqualTo(3.0); // min limit
            assertThat(costLv10).isEqualTo(30); // max limit
        }
    }

    // ==================== シナリオ6: 複数パラメータの同時変化 ====================

    @Nested
    @DisplayName("シナリオ6: 複数パラメータの同時変化")
    class Scenario6MultipleParameterChanges {

        @Test
        @DisplayName("シナリオ6-1: レベルアップで全パラメータが変化する")
        void test6_1_AllParametersChangeWithLevel() {
            // Given: 全パラメータがレベル依存のスキル
            Skill skill = createFullLevelDependentSkill();
            skillManager.registerSkill(skill);

            // When: レベル1とレベル5のパラメータを比較
            double cdLv1 = skill.getCooldown(1);
            double cdLv5 = skill.getCooldown(5);
            int costLv1 = skill.getCost(1);
            int costLv5 = skill.getCost(5);

            // Then: 全てのパラメータが変化している
            assertThat(cdLv5).isLessThan(cdLv1); // CD短縮
            assertThat(costLv5).isGreaterThan(costLv1); // コスト増加
        }

        @Test
        @DisplayName("シナリオ6-2: バランスの取れたパラメータ設計")
        void test6_2_BalancedParameterDesign() {
            // Given: バランス設計されたスキル
            Skill skill = createBalancedSkill();

            // When: レベル1とレベル10を比較
            double cdLv1 = skill.getCooldown(1);
            double cdLv10 = skill.getCooldown(10);
            int costLv1 = skill.getCost(1);
            int costLv10 = skill.getCost(10);
            double damageLv1 = skillManager.calculateDamage(skill, 1, mockRpgPlayer);
            double damageLv10 = skillManager.calculateDamage(skill, 10, mockRpgPlayer);

            // Then: レベルアップで全体的に強化される
            // CDは短縮、コストは増加、ダメージは増加
            assertThat(cdLv10).isLessThan(cdLv1);
            assertThat(costLv10).isGreaterThan(costLv1);
            assertThat(damageLv10).isGreaterThan(damageLv1);

            // コスト増加に対してダメージ増加のバランスが良い
            double costIncreaseRatio = (double) costLv10 / costLv1;
            double damageIncreaseRatio = damageLv10 / damageLv1;
            assertThat(damageIncreaseRatio).isGreaterThan(costIncreaseRatio * 0.8);
        }
    }

    // ==================== テスト用ヘルパーメソッド ====================

    /**
     * クールダウン短縮スキルを作成
     */
    private Skill createCooldownReductionSkill() {
        Skill.DamageCalculation damage = new Skill.DamageCalculation(
                50.0,
                new Skill.DamageCalculation.StatMultiplier("STRENGTH", 1.5),
                10.0
        );

        Skill.CostConfig costConfig = new Skill.CostConfig(
                "mana",
                10,
                2,
                null,
                30
        );

        Skill.CooldownConfig cooldownConfig = new Skill.CooldownConfig(
                10.0,
                -1.0,
                3.0,
                null
        );

        return new Skill.Builder("quick_strike", "クイックストライク")
                .displayName("&eクイックストライク")
                .type(SkillType.ACTIVE)
                .maxLevel(5)
                .damage(damage)
                .costConfig(costConfig)
                .cooldownConfig(cooldownConfig)
                .build();
    }

    /**
     * 固定クールダウンスキルを作成
     */
    private Skill createFixedCooldownSkill() {
        Skill.DamageCalculation damage = new Skill.DamageCalculation(
                40.0,
                new Skill.DamageCalculation.StatMultiplier("STRENGTH", 1.0),
                0.0
        );

        Skill.CostConfig costConfig = new Skill.CostConfig(
                "mana",
                10,
                0,
                null,
                null
        );

        Skill.CooldownConfig cooldownConfig = new Skill.CooldownConfig(
                5.0,
                0,
                null,
                null
        );

        return new Skill.Builder("normal_attack", "ノーマルアタック")
                .displayName("&fノーマルアタック")
                .type(SkillType.ACTIVE)
                .maxLevel(10)
                .damage(damage)
                .costConfig(costConfig)
                .cooldownConfig(cooldownConfig)
                .build();
    }

    /**
     * コスト増加スキルを作成
     */
    private Skill createCostIncreaseSkill() {
        Skill.DamageCalculation damage = new Skill.DamageCalculation(
                60.0,
                new Skill.DamageCalculation.StatMultiplier("INTELLIGENCE", 2.0),
                15.0
        );

        Skill.CostConfig costConfig = new Skill.CostConfig(
                "mana",
                10,
                2,
                null,
                30
        );

        Skill.CooldownConfig cooldownConfig = new Skill.CooldownConfig(
                8.0,
                0,
                null,
                null
        );

        return new Skill.Builder("power_blast", "パワーブラスト")
                .displayName("&cパワーブラスト")
                .type(SkillType.ACTIVE)
                .maxLevel(10)
                .damage(damage)
                .costConfig(costConfig)
                .cooldownConfig(cooldownConfig)
                .build();
    }

    /**
     * 固定コストスキルを作成
     */
    private Skill createFixedCostSkill() {
        Skill.DamageCalculation damage = new Skill.DamageCalculation(
                100.0,
                new Skill.DamageCalculation.StatMultiplier("STRENGTH", 3.0),
                0.0
        );

        Skill.CostConfig costConfig = new Skill.CostConfig(
                "hp",
                20,
                0,
                null,
                null
        );

        Skill.CooldownConfig cooldownConfig = new Skill.CooldownConfig(
                15.0,
                0,
                null,
                null
        );

        return new Skill.Builder("heavy_strike", "ヘヴィストライク")
                .displayName("&8ヘヴィストライク")
                .type(SkillType.ACTIVE)
                .maxLevel(5)
                .damage(damage)
                .costConfig(costConfig)
                .cooldownConfig(cooldownConfig)
                .build();
    }

    /**
     * ダメージスキルを作成
     */
    private Skill createDamageSkill() {
        Skill.DamageCalculation damage = new Skill.DamageCalculation(
                50.0,
                new Skill.DamageCalculation.StatMultiplier("STRENGTH", 1.5),
                10.0
        );

        Skill.CostConfig costConfig = new Skill.CostConfig(
                "mana",
                10,
                1,
                null,
                20
        );

        Skill.CooldownConfig cooldownConfig = new Skill.CooldownConfig(
                6.0,
                0,
                null,
                null
        );

        return new Skill.Builder("slash", "スラッシュ")
                .displayName("&6スラッシュ")
                .type(SkillType.ACTIVE)
                .maxLevel(10)
                .damage(damage)
                .costConfig(costConfig)
                .cooldownConfig(cooldownConfig)
                .build();
    }

    /**
     * 全パラメータレベル依存スキルを作成
     */
    private Skill createFullLevelDependentSkill() {
        Skill.DamageCalculation damage = new Skill.DamageCalculation(
                30.0,
                new Skill.DamageCalculation.StatMultiplier("INTELLIGENCE", 1.5),
                12.0
        );

        Skill.CostConfig costConfig = new Skill.CostConfig(
                "mana",
                8,
                2,
                null,
                25
        );

        Skill.CooldownConfig cooldownConfig = new Skill.CooldownConfig(
                12.0,
                -1.5,
                4.0,
                null
        );

        return new Skill.Builder("chaos_bolt", "カオスボルト")
                .displayName("&dカオスボルト")
                .type(SkillType.ACTIVE)
                .maxLevel(5)
                .damage(damage)
                .costConfig(costConfig)
                .cooldownConfig(cooldownConfig)
                .build();
    }

    /**
     * バランス設計スキルを作成
     */
    private Skill createBalancedSkill() {
        Skill.DamageCalculation damage = new Skill.DamageCalculation(
                40.0,
                new Skill.DamageCalculation.StatMultiplier("STRENGTH", 1.2),
                8.0
        );

        Skill.CostConfig costConfig = new Skill.CostConfig(
                "mana",
                10,
                1.5,
                null,
                25
        );

        Skill.CooldownConfig cooldownConfig = new Skill.CooldownConfig(
                10.0,
                -0.8,
                5.0,
                null
        );

        return new Skill.Builder("balanced_strike", "バランスストライク")
                .displayName("&aバランスストライク")
                .type(SkillType.ACTIVE)
                .maxLevel(10)
                .damage(damage)
                .costConfig(costConfig)
                .cooldownConfig(cooldownConfig)
                .build();
    }
}
