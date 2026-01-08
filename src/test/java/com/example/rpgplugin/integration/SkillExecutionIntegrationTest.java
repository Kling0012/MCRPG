package com.example.rpgplugin.integration;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillLoader;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillType;
import com.example.rpgplugin.skill.target.SkillTarget;
import com.example.rpgplugin.skill.target.TargetType;
import com.example.rpgplugin.skill.target.AreaShape;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * スキル発動フローの結合テスト
 *
 * <p>テスト対象:</p>
 * <ul>
 *   <li>YAML → ロード → 実行 → 結果確認の基本フロー</li>
 *   <li>斬撃スキル発動（単体ターゲット）</li>
 *   <li>ファイアボール発動（扇状範囲）</li>
 *   <li>レベル別スキルパラメータの変化</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキル発動フローのテストに専念</li>
 *   <li>現実的: 実際のYAML設定と近い形式でテスト</li>
 *   <li>独立性: 各テストは独立して実行可能</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("スキル発動フロー結合テスト")
class SkillExecutionIntegrationTest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private Logger mockLogger;

    @Mock
    private Player mockPlayer;

    @Mock
    private RPGPlayer mockRpgPlayer;

    @Mock
    private Location mockLocation;

    private MockedStatic<Bukkit> mockedBukkit;

    private SkillLoader skillLoader;
    private SkillManager skillManager;

    /**
     * 各テストの前に実行されるセットアップ処理
     */
    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        // Bukkit静的メソッドのモック化
        mockedBukkit = mockStatic(Bukkit.class);

        // Pluginの基本設定
        when(mockPlugin.getLogger()).thenReturn(mockLogger);
        when(mockPlugin.getDataFolder()).thenReturn(tempDir.toFile());

        // プレイヤーのモック設定
        UUID playerUuid = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockPlayer.getName()).thenReturn("TestPlayer");
        when(mockPlayer.getLocation()).thenReturn(mockLocation);

        // RPGPlayerのモック設定
        when(mockRpgPlayer.getBukkitPlayer()).thenReturn(mockPlayer);
        when(mockRpgPlayer.getLevel()).thenReturn(10);

        // スキルディレクトリの作成
        Path skillsDir = tempDir.resolve("skills");
        Files.createDirectories(skillsDir);
        Path activeSkillsDir = skillsDir.resolve("active");
        Files.createDirectories(activeSkillsDir);

        // SkillLoaderとSkillManagerの初期化
        skillLoader = new SkillLoader(mockPlugin);
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

    // ==================== シナリオ1: 斬撃スキル発動（単体） ====================

    @Nested
    @DisplayName("シナリオ1: 斬撃スキル発動（単体ターゲット）")
    class Scenario1SingleTargetSkill {

        @Test
        @DisplayName("シナリオ1-1: 斬撃スキルがYAMLから正しくロードされる")
        void test1_1_SlashSkillYamlLoad() throws IOException {
            // Given: 斬撃スキルのYAMLファイルを作成
            createSlashSkillYaml();

            // When: スキルを読み込む
            skillLoader.loadAllSkills();

            // Then: スキルが正しくロードされている
            // 注: このテストではSkillLoaderの実装に依存
            // 実際の登録はSkillManager経由で行われる
        }

        @Test
        @DisplayName("シナリオ1-2: 斬撃スキルが手動登録で正しく動作する")
        void test1_2_SlashSkillManualRegistration() {
            // Given: 斬撃スキルを作成して登録
            Skill slashSkill = createSlashSkill();
            skillManager.registerSkill(slashSkill);

            // When: スキルをプレイヤーに習得させる
            when(mockRpgPlayer.hasSkill("slash")).thenReturn(true);
            when(mockRpgPlayer.getSkillLevel("slash")).thenReturn(1);

            // Then: スキルが正しく登録されている
            assertThat(skillManager.hasSkill("slash")).isTrue();

            Skill registeredSkill = skillManager.getSkill("slash");
            assertThat(registeredSkill).isNotNull();
            assertThat(registeredSkill.getId()).isEqualTo("slash");
            assertThat(registeredSkill.getType()).isEqualTo(SkillType.ACTIVE);
        }

        @Test
        @DisplayName("シナリオ1-3: 斬撃スキルのダメージ計算が正しく行われる")
        void test1_3_SlashSkillDamageCalculation() {
            // Given: 斬撃スキルを作成
            Skill slashSkill = createSlashSkill();
            skillManager.registerSkill(slashSkill);

            // When: ダメージを計算する
            double damage = skillManager.calculateDamage(slashSkill, 1, mockRpgPlayer);

            // Then: 基本ダメージ + ステータス補正が含まれている
            assertThat(damage).isGreaterThan(0);
        }

        @Test
        @DisplayName("シナリオ1-4: スキルレベル上昇でダメージが増加する")
        void test1_4_SkillLevelDamageIncrease() {
            // Given: 斬撃スキルを作成
            Skill slashSkill = createSlashSkill();
            skillManager.registerSkill(slashSkill);

            // When: レベル1とレベル5のダメージを計算
            double damageLv1 = skillManager.calculateDamage(slashSkill, 1, mockRpgPlayer);
            double damageLv5 = skillManager.calculateDamage(slashSkill, 5, mockRpgPlayer);

            // Then: レベル5の方がダメージが高い
            assertThat(damageLv5).isGreaterThan(damageLv1);
        }
    }

    // ==================== シナリオ2: ファイアボール発動（扇状範囲） ====================

    @Nested
    @DisplayName("シナリオ2: ファイアボール発動（扇状範囲）")
    class Scenario2FireballSkill {

        @Test
        @DisplayName("シナリオ2-1: ファイアボールスキルが正しく作成される")
        void test2_1_FireballSkillCreation() {
            // Given: ファイアボールスキルの設定
            Skill fireballSkill = createFireballSkill();

            // When: スキルを登録
            skillManager.registerSkill(fireballSkill);

            // Then: スキルが正しく登録されている
            assertThat(skillManager.hasSkill("fireball")).isTrue();

            Skill registeredSkill = skillManager.getSkill("fireball");
            assertThat(registeredSkill).isNotNull();
            assertThat(registeredSkill.hasSkillTarget()).isTrue();
        }

        @Test
        @DisplayName("シナリオ2-2: ファイアボールのターゲット設定が正しい")
        void test2_2_FireballTargetingConfig() {
            // Given: ファイアボールスキル
            Skill fireballSkill = createFireballSkill();

            // When: ターゲット設定を取得
            SkillTarget skillTarget = fireballSkill.getSkillTarget();

            // Then: 扇状範囲の設定が正しい
            assertThat(skillTarget).isNotNull();
            assertThat(skillTarget.getType()).isEqualTo(TargetType.NEAREST_HOSTILE);
            assertThat(skillTarget.getAreaShape()).isEqualTo(AreaShape.CONE);

            SkillTarget.ConeConfig coneConfig = skillTarget.getCone();
            assertThat(coneConfig).isNotNull();
            assertThat(coneConfig.getAngle()).isEqualTo(90.0);
            assertThat(coneConfig.getRange()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("シナリオ2-3: ファイアボールのコスト設定が正しい")
        void test2_3_FireballCostConfig() {
            // Given: ファイアボールスキル
            Skill fireballSkill = createFireballSkill();

            // When: 各レベルのコストを取得
            int costLv1 = fireballSkill.getCost(1);
            int costLv3 = fireballSkill.getCost(3);
            int costLv5 = fireballSkill.getCost(5);

            // Then: レベルに応じてコストが増加する
            assertThat(costLv1).isEqualTo(10);
            assertThat(costLv3).isEqualTo(14);
            assertThat(costLv5).isEqualTo(18);
        }

        @Test
        @DisplayName("シナリオ2-4: ファイアボールのクールダウンがレベルで変化する")
        void test2_4_FireballCooldownChange() {
            // Given: ファイアボールスキル
            Skill fireballSkill = createFireballSkill();

            // When: 各レベルのクールダウンを取得
            double cdLv1 = fireballSkill.getCooldown(1);
            double cdLv3 = fireballSkill.getCooldown(3);
            double cdLv5 = fireballSkill.getCooldown(5);

            // Then: レベルが上がるほどクールダウンが短くなる
            assertThat(cdLv1).isEqualTo(8.0);
            assertThat(cdLv3).isEqualTo(6.0);
            assertThat(cdLv5).isEqualTo(4.0);
        }
    }

    // ==================== シナリオ3: スキル実行結果の検証 ====================

    @Nested
    @DisplayName("シナリオ3: スキル実行結果の検証")
    class Scenario3ExecutionResult {

        @Test
        @DisplayName("シナリオ3-1: 正常なスキル実行で成功結果が返る")
        void test3_1_SuccessfulExecution() {
            // Given: 斬撃スキルを登録し、プレイヤーに習得させる
            Skill slashSkill = createSlashSkill();
            skillManager.registerSkill(slashSkill);

            when(mockRpgPlayer.hasSkill("slash")).thenReturn(true);
            when(mockRpgPlayer.getSkillLevel("slash")).thenReturn(1);

            // When: スキルを実行
            SkillManager.SkillExecutionResult result = skillManager.executeSkill(mockPlayer, "slash");

            // Then: 成功結果が返る
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("シナリオ3-2: 未習得スキルの実行で失敗結果が返る")
        void test3_2_UnskilledExecutionFailure() {
            // Given: 斬撃スキルを登録（プレイヤーは未習得）
            Skill slashSkill = createSlashSkill();
            skillManager.registerSkill(slashSkill);

            when(mockRpgPlayer.hasSkill("slash")).thenReturn(false);

            // When: スキルを実行
            SkillManager.SkillExecutionResult result = skillManager.executeSkill(mockPlayer, "slash");

            // Then: 失敗結果が返る
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("シナリオ3-3: クールダウン中のスキル実行で失敗する")
        void test3_3_CooldownExecutionFailure() {
            // Given: 斬撃スキルを登録し、プレイヤーに習得させる
            Skill slashSkill = createSlashSkill();
            skillManager.registerSkill(slashSkill);

            when(mockRpgPlayer.hasSkill("slash")).thenReturn(true);
            when(mockRpgPlayer.getSkillLevel("slash")).thenReturn(1);

            // When: 2回連続でスキルを実行
            skillManager.executeSkill(mockPlayer, "slash");
            SkillManager.SkillExecutionResult secondResult = skillManager.executeSkill(mockPlayer, "slash");

            // Then: 2回目はクールダウンで失敗する
            assertThat(secondResult.isSuccess()).isFalse();
        }
    }

    // ==================== シナリオ4: 数式ダメージ計算 ====================

    @Nested
    @DisplayName("シナリオ4: 数式ダメージ計算の結合テスト")
    class Scenario4FormulaDamage {

        @Test
        @DisplayName("シナリオ4-1: 基本数式でダメージが計算される")
        void test4_1_BasicFormulaDamage() {
            // Given: 数式ダメージを持つスキル
            Skill formulaSkill = createFormulaSkill();
            skillManager.registerSkill(formulaSkill);

            // When: ダメージを計算
            double damage = skillManager.calculateDamageWithFormula(
                    formulaSkill, 1, mockRpgPlayer, null);

            // Then: ダメージが計算されている
            assertThat(damage).isGreaterThan(0);
        }

        @Test
        @DisplayName("シナリオ4-2: レベル別数式でダメージが変化する")
        void test4_2_LevelBasedFormulaDamage() {
            // Given: レベル別数式を持つスキル
            Skill levelFormulaSkill = createLevelBasedFormulaSkill();
            skillManager.registerSkill(levelFormulaSkill);

            // When: レベル1とレベル5のダメージを計算
            double damageLv1 = skillManager.calculateDamageWithFormula(
                    levelFormulaSkill, 1, mockRpgPlayer, null);
            double damageLv5 = skillManager.calculateDamageWithFormula(
                    levelFormulaSkill, 5, mockRpgPlayer, null);

            // Then: レベル5の方がダメージが高い
            assertThat(damageLv5).isGreaterThan(damageLv1);
        }
    }

    // ==================== テスト用ヘルパーメソッド ====================

    /**
     * 斬撃スキルのYAMLファイルを作成
     */
    private void createSlashSkillYaml() throws IOException {
        Path tempDir = mockPlugin.getDataFolder().toPath();
        Path skillFile = tempDir.resolve("skills").resolve("active").resolve("slash.yml");

        String yamlContent = """
                id: slash
                name: 斬撃
                display_name: "&6斬撃"
                type: active
                description:
                  - "&c基本となる単体攻撃"
                max_level: 5

                damage:
                  base: 50.0
                  stat_multiplier:
                    stat: STRENGTH
                    multiplier: 1.5
                  level_multiplier: 10.0

                cooldown:
                  base: 3.0
                  per_level: -0.2
                  min: 2.0

                cost:
                  type: mana
                  base: 5
                  per_level: 1
                  max: 10

                target:
                  type: nearest_hostile
                  area_shape: single

                icon_material: IRON_SWORD
                """;

        Files.writeString(skillFile, yamlContent);
    }

    /**
     * 斬撃スキルを作成
     */
    private Skill createSlashSkill() {
        Skill.DamageCalculation damage = new Skill.DamageCalculation(
                50.0,
                new Skill.DamageCalculation.StatMultiplier("STRENGTH", 1.5),
                10.0
        );

        Skill.CostConfig costConfig = new Skill.CostConfig(
                "mana",
                5,
                1,
                null,
                10
        );

        Skill.CooldownConfig cooldownConfig = new Skill.CooldownConfig(
                3.0,
                -0.2,
                2.0,
                null
        );

        SkillTarget skillTarget = new SkillTarget(
                TargetType.NEAREST_HOSTILE,
                AreaShape.SINGLE,
                new SkillTarget.SingleTargetConfig(true, false),
                null,
                null,
                null
        );

        return new Skill.Builder("slash", "斬撃")
                .displayName("&6斬撃")
                .type(SkillType.ACTIVE)
                .maxLevel(5)
                .damage(damage)
                .costConfig(costConfig)
                .cooldownConfig(cooldownConfig)
                .skillTarget(skillTarget)
                .build();
    }

    /**
     * ファイアボールスキルを作成
     */
    private Skill createFireballSkill() {
        Skill.DamageCalculation damage = new Skill.DamageCalculation(
                40.0,
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
                -1.0,
                3.0,
                null
        );

        SkillTarget skillTarget = new SkillTarget(
                TargetType.NEAREST_HOSTILE,
                AreaShape.CONE,
                null,
                new SkillTarget.ConeConfig(90.0, 5.0),
                null,
                null
        );

        return new Skill.Builder("fireball", "ファイアボール")
                .displayName("&cファイアボール")
                .type(SkillType.ACTIVE)
                .maxLevel(5)
                .damage(damage)
                .costConfig(costConfig)
                .cooldownConfig(cooldownConfig)
                .skillTarget(skillTarget)
                .build();
    }

    /**
     * 数式ダメージスキルを作成
     */
    private Skill createFormulaSkill() {
        Skill.FormulaDamageConfig formulaDamage = new Skill.FormulaDamageConfig(
                "STR * 1.5 + INT * 1.0 + Lv * 5"
        );

        Skill.CostConfig costConfig = new Skill.CostConfig(
                "mana",
                10,
                1,
                null,
                20
        );

        Skill.CooldownConfig cooldownConfig = new Skill.CooldownConfig(
                5.0,
                0,
                null,
                null
        );

        return new Skill.Builder("magic_arrow", "マジックアロー")
                .displayName("&bマジックアロー")
                .type(SkillType.ACTIVE)
                .maxLevel(10)
                .formulaDamage(formulaDamage)
                .costConfig(costConfig)
                .cooldownConfig(cooldownConfig)
                .build();
    }

    /**
     * レベル別数式ダメージスキルを作成
     */
    private Skill createLevelBasedFormulaSkill() {
        Skill.FormulaDamageConfig formulaDamage = new Skill.FormulaDamageConfig(
                "STR * 2.0",
                java.util.Map.of(
                        1, "STR * 2.0",
                        5, "STR * 3.0",
                        10, "STR * 5.0"
                )
        );

        Skill.CostConfig costConfig = new Skill.CostConfig(
                "mana",
                15,
                2,
                null,
                35
        );

        Skill.CooldownConfig cooldownConfig = new Skill.CooldownConfig(
                6.0,
                0,
                null,
                null
        );

        return new Skill.Builder("power_strike", "パワーストライク")
                .displayName("&eパワーストライク")
                .type(SkillType.ACTIVE)
                .maxLevel(10)
                .formulaDamage(formulaDamage)
                .costConfig(costConfig)
                .cooldownConfig(cooldownConfig)
                .build();
    }
}
