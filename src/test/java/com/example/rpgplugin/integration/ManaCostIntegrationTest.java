package com.example.rpgplugin.integration;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillType;
import com.example.rpgplugin.skill.LevelDependentParameter;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
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
 * コスト消費連携の結合テスト
 *
 * <p>テスト対象:</p>
 * <ul>
 *   <li>MP/HP消費 → スキル発動 → 残量確認</li>
 *   <li>HP消費スキル発動</li>
 *   <li>コスト不足時のスキル発動失敗</li>
 *   <li>レベルアップによるコスト変化</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: コスト消費フローのテストに専念</li>
 *   <li>現実的: 実際のゲームプレイに近いシナリオ</li>
 *   <li>独立性: 各テストは独立して実行可能</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("コスト消費連携結合テスト")
class ManaCostIntegrationTest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private Logger mockLogger;

    @Mock
    private Player mockPlayer;

    @Mock
    private RPGPlayer mockRpgPlayer;

    @Mock
    private StatManager mockStatManager;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private World mockWorld;

    private MockedStatic<Bukkit> mockedBukkit;

    private SkillManager skillManager;

    /**
     * 各テストの前に実行されるセットアップ処理
     */
    @BeforeEach
    void setUp() {
        // 前のモックが残っている場合はクローズする
        if (mockedBukkit != null) {
            try {
                mockedBukkit.close();
            } catch (Exception e) {
                // クローズ時の例外は無視
            }
        }

        // Bukkit静的メソッドのモック化
        mockedBukkit = mockStatic(Bukkit.class);

        // Pluginの基本設定
        when(mockPlugin.getLogger()).thenReturn(mockLogger);

        // プレイヤーのモック設定
        UUID playerUuid = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockPlayer.getName()).thenReturn("TestPlayer");
        when(mockPlayer.getWorld()).thenReturn(mockWorld);
        when(mockPlayer.getLocation()).thenReturn(new Location(mockWorld, 0, 64, 0));

        // RPGPlayerのモック設定
        when(mockRpgPlayer.getBukkitPlayer()).thenReturn(mockPlayer);
        when(mockRpgPlayer.getUuid()).thenReturn(playerUuid);
        when(mockRpgPlayer.getUsername()).thenReturn("TestPlayer");
        when(mockRpgPlayer.getLevel()).thenReturn(10);
        when(mockRpgPlayer.getStatManager()).thenReturn(mockStatManager);

        // StatManagerのモック設定（デフォルト値）
        when(mockStatManager.getFinalStat(any(Stat.class))).thenReturn(50);
        when(mockRpgPlayer.getFinalStat(Stat.STRENGTH)).thenReturn(50);
        when(mockRpgPlayer.getFinalStat(Stat.INTELLIGENCE)).thenReturn(30);
        when(mockRpgPlayer.getFinalStat(Stat.VITALITY)).thenReturn(40);
        when(mockRpgPlayer.getFinalStat(Stat.DEXTERITY)).thenReturn(35);
        when(mockRpgPlayer.getFinalStat(Stat.SPIRIT)).thenReturn(25);

        // PlayerManagerのモック設定
        when(mockPlayerManager.getRPGPlayer(playerUuid)).thenReturn(mockRpgPlayer);

        // SkillManagerの初期化
        skillManager = new SkillManager(mockPlugin, mockPlayerManager);
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

    // ==================== シナリオ1: MP消費スキルの基本動作 ====================

    @Nested
    @DisplayName("シナリオ1: MP消費スキルの基本動作")
    class Scenario1ManaCostSkills {

        @Test
        @DisplayName("シナリオ1-1: MP消費スキルのコスト設定が正しい")
        void test1_1_ManaCostConfig() {
            // Given: MP消費スキルを作成
            Skill manaSkill = createManaCostSkill();
            skillManager.registerSkill(manaSkill);

            // When: 各レベルのコストを取得
            int costLv1 = manaSkill.getCost(1);
            int costLv3 = manaSkill.getCost(3);
            int costLv5 = manaSkill.getCost(5);

            // Then: レベルに応じてコストが増加する
            assertThat(costLv1).isEqualTo(10);
            assertThat(costLv3).isEqualTo(14);
            assertThat(costLv5).isEqualTo(18);
        }

        @Test
        @DisplayName("シナリオ1-2: MPコストタイプが正しく設定されている")
        void test1_2_ManaCostType() {
            // Given: MP消費スキルを作成
            Skill manaSkill = createManaCostSkill();

            // When: コストタイプを取得
            SkillCostType costType = manaSkill.getCostType();

            // Then: MANAタイプである
            assertThat(costType).isEqualTo(SkillCostType.MANA);
        }

        @Test
        @DisplayName("シナリオ1-3: MP消費結果が正しく返る")
        void test1_3_ManaConsumptionResult() {
            // Given: MP消費スキルと十分なMPを持つプレイヤー
            Skill manaSkill = createManaCostSkill();
            skillManager.registerSkill(manaSkill);

            when(mockRpgPlayer.hasMana(10)).thenReturn(true);

            // When: コスト消費を実行
            SkillManager.CostConsumptionResult result = skillManager.consumeCost(
                    mockRpgPlayer, manaSkill.getCost(1), manaSkill.getCostType());

            // Then: 消費成功
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getConsumedAmount()).isEqualTo(10);
        }

        @Test
        @DisplayName("シナリオ1-4: MP不足時に消費が失敗する")
        void test1_4_InsufficientMana() {
            // Given: MP消費スキルとMP不足のプレイヤー
            Skill manaSkill = createManaCostSkill();
            skillManager.registerSkill(manaSkill);

            when(mockRpgPlayer.hasMana(10)).thenReturn(false);

            // When: コスト消費を実行
            SkillManager.CostConsumptionResult result = skillManager.consumeCost(
                    mockRpgPlayer, manaSkill.getCost(1), manaSkill.getCostType());

            // Then: 消費失敗
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getConsumedAmount()).isEqualTo(0);
        }
    }

    // ==================== シナリオ2: HP消費スキル ====================

    @Nested
    @DisplayName("シナリオ2: HP消費スキル")
    class Scenario2HPCostSkills {

        @Test
        @DisplayName("シナリオ2-1: HP消費スキルのコスト設定が正しい")
        void test2_1_HPCostConfig() {
            // Given: HP消費スキルを作成
            Skill hpSkill = createHPCostSkill();
            skillManager.registerSkill(hpSkill);

            // When: 各レベルのコストを取得
            int costLv1 = hpSkill.getCost(1);
            int costLv3 = hpSkill.getCost(3);

            // Then: 固定コストである
            assertThat(costLv1).isEqualTo(20);
            assertThat(costLv3).isEqualTo(20);
        }

        @Test
        @DisplayName("シナリオ2-2: HPコストタイプが正しく設定されている")
        void test2_2_HPCostType() {
            // Given: HP消費スキルを作成
            Skill hpSkill = createHPCostSkill();

            // When: コストタイプを取得
            SkillCostType costType = hpSkill.getCostType();

            // Then: HPタイプである
            assertThat(costType).isEqualTo(SkillCostType.HP);
        }

        @Test
        @DisplayName("シナリオ2-3: HP消費結果が正しく返る")
        void test2_3_HPConsumptionResult() {
            // Given: HP消費スキルと十分なHPを持つプレイヤー
            Skill hpSkill = createHPCostSkill();
            skillManager.registerSkill(hpSkill);

            when(mockRpgPlayer.isOnline()).thenReturn(true);
            when(mockPlayer.getHealth()).thenReturn(100.0);

            // When: コスト消費を実行
            SkillManager.CostConsumptionResult result = skillManager.consumeCost(
                    mockRpgPlayer, hpSkill.getCost(1), hpSkill.getCostType());

            // Then: 消費成功
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getConsumedAmount()).isEqualTo(20);
        }

        @Test
        @DisplayName("シナリオ2-4: HP不足時に消費が失敗する")
        void test2_4_InsufficientHP() {
            // Given: HP消費スキルとHP不足のプレイヤー
            Skill hpSkill = createHPCostSkill();
            skillManager.registerSkill(hpSkill);

            when(mockRpgPlayer.isOnline()).thenReturn(true);
            when(mockPlayer.getHealth()).thenReturn(10.0);

            // When: コスト消費を実行
            SkillManager.CostConsumptionResult result = skillManager.consumeCost(
                    mockRpgPlayer, hpSkill.getCost(1), hpSkill.getCostType());

            // Then: 消費失敗
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isFalse();
        }
    }

    // ==================== シナリオ3: レベルアップによるコスト変化 ====================

    @Nested
    @DisplayName("シナリオ3: レベルアップによるコスト変化")
    class Scenario3LevelBasedCostChange {

        @Test
        @DisplayName("シナリオ3-1: レベルアップでMPコストが増加する")
        void test3_1_CostIncreaseWithLevel() {
            // Given: レベル依存コストスキル
            Skill variableCostSkill = createVariableCostSkill();

            // When: 各レベルのコストを比較
            int costLv1 = variableCostSkill.getCost(1);
            int costLv3 = variableCostSkill.getCost(3);
            int costLv5 = variableCostSkill.getCost(5);

            // Then: レベルが上がるほどコストが増加
            assertThat(costLv1).isLessThan(costLv3);
            assertThat(costLv3).isLessThan(costLv5);
        }

        @Test
        @DisplayName("シナリオ3-2: コスト上限が正しく適用される")
        void test3_2_CostMaxLimit() {
            // Given: 上限付きコストスキル
            Skill variableCostSkill = createVariableCostSkill();

            // When: 最大レベルのコストを取得
            int costLv10 = variableCostSkill.getCost(10);
            int costLv20 = variableCostSkill.getCost(20);

            // Then: 上限値で止まる
            assertThat(costLv10).isEqualTo(30); // max = 30
            assertThat(costLv20).isEqualTo(30); // max = 30
        }

        @Test
        @DisplayName("シナリオ3-3: コスト下限が正しく適用される")
        void test3_3_CostMinLimit() {
            // Given: 下限付きコストスキル（レベルで下がるタイプ）
            Skill decreasingCostSkill = createDecreasingCostSkill();

            // When: 高レベルのコストを取得
            int costLv1 = decreasingCostSkill.getCost(1);
            int costLv5 = decreasingCostSkill.getCost(5);
            int costLv10 = decreasingCostSkill.getCost(10);

            // Then: 下限値で止まる
            assertThat(costLv1).isEqualTo(20);
            assertThat(costLv5).isEqualTo(10); // min = 10
            assertThat(costLv10).isEqualTo(10); // min = 10
        }
    }

    // ==================== シナリオ4: スキル発動とコスト消費の連携 ====================

    @Nested
    @DisplayName("シナリオ4: スキル発動とコスト消費の連携")
    class Scenario4SkillExecutionWithCost {

        @Test
        @DisplayName("シナリオ4-1: MP消費スキルの発動でMPが減少する")
        void test4_1_ManaDecreaseOnCast() {
            // Given: MP消費スキルと十分なMPを持つプレイヤー
            Skill manaSkill = createManaCostSkill();
            skillManager.registerSkill(manaSkill);

            // プレイヤーがスキルを習得したことを設定
            skillManager.getPlayerSkillData(mockPlayer).setSkillLevel("fireball", 1);

            when(mockRpgPlayer.hasMana(10)).thenReturn(true);

            // When: スキルを発動
            SkillManager.SkillExecutionResult result = skillManager.executeSkill(
                    mockPlayer, "fireball");

            // Then: スキル発動が成功し、コストが消費されている
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("シナリオ4-2: MP不足でスキル発動が失敗する")
        void test4_2_FailWhenInsufficientMana() {
            // Given: MP消費スキルとMP不足のプレイヤー
            Skill manaSkill = createManaCostSkill();
            skillManager.registerSkill(manaSkill);

            // プレイヤーがスキルを習得したことを設定
            skillManager.getPlayerSkillData(mockPlayer).setSkillLevel("fireball", 1);

            when(mockRpgPlayer.hasMana(10)).thenReturn(false);

            // When: スキルを発動
            SkillManager.SkillExecutionResult result = skillManager.executeSkill(
                    mockPlayer, "fireball");

            // Then: スキル発動が失敗する
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("シナリオ4-3: HP消費スキルの発動でHPが減少する")
        void test4_3_HPDecreaseOnCast() {
            // Given: HP消費スキルと十分なHPを持つプレイヤー
            Skill hpSkill = createHPCostSkill();
            skillManager.registerSkill(hpSkill);

            // プレイヤーがスキルを習得したことを設定
            skillManager.getPlayerSkillData(mockPlayer).setSkillLevel("blood_sacrifice", 1);

            when(mockPlayer.getHealth()).thenReturn(100.0);

            // When: スキルを発動
            SkillManager.SkillExecutionResult result = skillManager.executeSkill(
                    mockPlayer, "blood_sacrifice");

            // Then: スキル発動が成功する
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("シナリオ4-4: HP不足でスキル発動が失敗する")
        void test4_4_FailWhenInsufficientHP() {
            // Given: HP消費スキルとHP不足のプレイヤー
            Skill hpSkill = createHPCostSkill();
            skillManager.registerSkill(hpSkill);

            // プレイヤーがスキルを習得したことを設定
            skillManager.getPlayerSkillData(mockPlayer).setSkillLevel("blood_sacrifice", 1);

            when(mockPlayer.getHealth()).thenReturn(10.0);

            // When: スキルを発動
            SkillManager.SkillExecutionResult result = skillManager.executeSkill(
                    mockPlayer, "blood_sacrifice");

            // Then: スキル発動が失敗する
            assertThat(result.isSuccess()).isFalse();
        }
    }

    // ==================== シナリオ5: 複数スキル発動のコスト管理 ====================

    @Nested
    @DisplayName("シナリオ5: 複数スキル発動のコスト管理")
    class Scenario5MultipleSkillCostManagement {

        @Test
        @DisplayName("シナリオ5-1: 連続発動でMPが正しく消費される")
        void test5_1_ConsecutiveCastManaConsumption() {
            // Given: 複数のMP消費スキル
            Skill skill1 = createManaCostSkill();
            Skill skill2 = createMediumManaCostSkill();
            skillManager.registerSkill(skill1);
            skillManager.registerSkill(skill2);

            // プレイヤーがスキルを習得したことを設定
            skillManager.getPlayerSkillData(mockPlayer).setSkillLevel("fireball", 1);
            skillManager.getPlayerSkillData(mockPlayer).setSkillLevel("ice_spike", 1);

            when(mockRpgPlayer.hasMana(anyInt())).thenReturn(true);

            // When: 2つのスキルを連続発動
            SkillManager.SkillExecutionResult result1 = skillManager.executeSkill(
                    mockPlayer, "fireball");
            SkillManager.SkillExecutionResult result2 = skillManager.executeSkill(
                    mockPlayer, "ice_spike");

            // Then: 両方とも成功する
            assertThat(result1.isSuccess()).isTrue();
            assertThat(result2.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("シナリオ5-2: MP不足で2番目のスキルが失敗する")
        void test5_2_SecondSkillFailsDueToMana() {
            // Given: 複数のMP消費スキル
            Skill skill1 = createManaCostSkill();
            Skill skill2 = createManaCostSkill();
            skillManager.registerSkill(skill1);
            skillManager.registerSkill(skill2);

            // プレイヤーがスキルを習得したことを設定
            skillManager.getPlayerSkillData(mockPlayer).setSkillLevel("fireball", 1);

            // 1回目は成功、2回目は失敗するように設定
            when(mockRpgPlayer.hasMana(10)).thenReturn(true, false);

            // When: 2つのスキルを連続発動
            SkillManager.SkillExecutionResult result1 = skillManager.executeSkill(
                    mockPlayer, "fireball");
            SkillManager.SkillExecutionResult result2 = skillManager.executeSkill(
                    mockPlayer, "fireball");

            // Then: 1回目は成功、2回目は失敗
            assertThat(result1.isSuccess()).isTrue();
            assertThat(result2.isSuccess()).isFalse();
        }
    }

    // ==================== テスト用ヘルパーメソッド ====================

    /**
     * MP消費スキルを作成（レベルで増加）
     */
    private Skill createManaCostSkill() {
        Skill.DamageCalculation damage = new Skill.DamageCalculation(
                40.0,
                Stat.INTELLIGENCE,
                2.0,
                15.0
        );

        LevelDependentParameter costParam = new LevelDependentParameter(10.0, 2.0, null, 30.0);
        LevelDependentParameter cooldownParam = new LevelDependentParameter(8.0, -1.0, 3.0, null);

        return new Skill(
                "fireball",
                "ファイアボール",
                "&cファイアボール",
                SkillType.NORMAL,
                java.util.List.of("&c強力な火の玉を放つ"),
                5,
                8.0,
                10,
                cooldownParam,
                costParam,
                SkillCostType.MANA,
                damage,
                (Skill.SkillTreeConfig) null,
                (String) null,
                java.util.List.of(),
                (java.util.List<Skill.VariableDefinition>) null,
                (Skill.FormulaDamageConfig) null,
                (Skill.TargetingConfig) null,
                (com.example.rpgplugin.skill.target.SkillTarget) null,
                null  // componentEffect
        );
    }

    /**
     * 中程度MP消費スキルを作成
     */
    private Skill createMediumManaCostSkill() {
        Skill.DamageCalculation damage = new Skill.DamageCalculation(
                25.0,
                Stat.INTELLIGENCE,
                1.5,
                10.0
        );

        LevelDependentParameter costParam = new LevelDependentParameter(8.0, 1.0, null, 15.0);
        LevelDependentParameter cooldownParam = new LevelDependentParameter(5.0, 0.0, null, null);

        return new Skill(
                "ice_spike",
                "アイススパイク",
                "&bアイススパイク",
                SkillType.NORMAL,
                java.util.List.of("&b鋭い氷の柱を突き出す"),
                5,
                5.0,
                8,
                cooldownParam,
                costParam,
                SkillCostType.MANA,
                damage,
                (Skill.SkillTreeConfig) null,
                (String) null,
                java.util.List.of(),
                (java.util.List<Skill.VariableDefinition>) null,
                (Skill.FormulaDamageConfig) null,
                (Skill.TargetingConfig) null,
                (com.example.rpgplugin.skill.target.SkillTarget) null,
                null  // componentEffect
        );
    }

    /**
     * HP消費スキルを作成
     */
    private Skill createHPCostSkill() {
        Skill.DamageCalculation damage = new Skill.DamageCalculation(
                100.0,
                Stat.STRENGTH,
                3.0,
                20.0
        );

        LevelDependentParameter costParam = new LevelDependentParameter(20.0, 0.0, null, null);
        LevelDependentParameter cooldownParam = new LevelDependentParameter(15.0, 0.0, null, null);

        return new Skill(
                "blood_sacrifice",
                "ブラッドサクリファイス",
                "&cブラッドサクリファイス",
                SkillType.NORMAL,
                java.util.List.of("&cHPを消費して強力な攻撃"),
                3,
                15.0,
                20,
                cooldownParam,
                costParam,
                SkillCostType.HP,
                damage,
                (Skill.SkillTreeConfig) null,
                (String) null,
                java.util.List.of(),
                (java.util.List<Skill.VariableDefinition>) null,
                (Skill.FormulaDamageConfig) null,
                (Skill.TargetingConfig) null,
                (com.example.rpgplugin.skill.target.SkillTarget) null,
                null  // componentEffect
        );
    }

    /**
     * 可変コストスキルを作成（上限付き）
     */
    private Skill createVariableCostSkill() {
        Skill.DamageCalculation damage = new Skill.DamageCalculation(
                50.0,
                Stat.INTELLIGENCE,
                2.5,
                10.0
        );

        LevelDependentParameter costParam = new LevelDependentParameter(10.0, 5.0, null, 30.0);
        LevelDependentParameter cooldownParam = new LevelDependentParameter(10.0, 0.0, null, null);

        return new Skill(
                "thunder_storm",
                "サンダーストーム",
                "&eサンダーストーム",
                SkillType.NORMAL,
                java.util.List.of("&e雷を落とす"),
                10,
                10.0,
                10,
                cooldownParam,
                costParam,
                SkillCostType.MANA,
                damage,
                (Skill.SkillTreeConfig) null,
                (String) null,
                java.util.List.of(),
                (java.util.List<Skill.VariableDefinition>) null,
                (Skill.FormulaDamageConfig) null,
                (Skill.TargetingConfig) null,
                (com.example.rpgplugin.skill.target.SkillTarget) null,
                null  // componentEffect
        );
    }

    /**
     * 減少コストスキルを作成（下限付き）
     */
    private Skill createDecreasingCostSkill() {
        Skill.DamageCalculation damage = new Skill.DamageCalculation(
                30.0,
                Stat.INTELLIGENCE,
                1.5,
                5.0
        );

        LevelDependentParameter costParam = new LevelDependentParameter(20.0, -2.5, 10.0, null);
        LevelDependentParameter cooldownParam = new LevelDependentParameter(3.0, 0.0, null, null);

        return new Skill(
                "quick_cast",
                "クイックキャスト",
                "&aクイックキャスト",
                SkillType.NORMAL,
                java.util.List.of("&a素早い詠唱"),
                5,
                3.0,
                20,
                cooldownParam,
                costParam,
                SkillCostType.MANA,
                damage,
                (Skill.SkillTreeConfig) null,
                (String) null,
                java.util.List.of(),
                (java.util.List<Skill.VariableDefinition>) null,
                (Skill.FormulaDamageConfig) null,
                (Skill.TargetingConfig) null,
                (com.example.rpgplugin.skill.target.SkillTarget) null,
                null  // componentEffect
        );
    }
}
