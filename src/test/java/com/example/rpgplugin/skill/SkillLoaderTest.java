package com.example.rpgplugin.skill;

import com.example.rpgplugin.RPGPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SkillLoaderのテストクラス
 *
 * <p>Phase11-6: 新YAMLフォーマット対応の検証</p>
 */
class SkillLoaderTest {

    @TempDir
    Path tempDir;

    @Mock
    private RPGPlugin mockPlugin;

    private SkillLoader loader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // モックプラグインの設定
        when(mockPlugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("SkillLoaderTest"));

        loader = new SkillLoader(mockPlugin);
    }

    /**
     * レガシーYAML形式の後方互換性テスト
     */
    @Test
    void testLegacyYamlFormat() throws IOException {
        // レガシー形式のYAMLファイル作成
        Path skillFile = tempDir.resolve("skills").resolve("test_legacy.yml");
        Files.createDirectories(skillFile.getParent());

        String legacyYaml = """
                id: test_skill
                name: テストスキル
                display_name: "&6テストスキル"
                type: active
                description:
                  - "テスト説明"
                max_level: 5
                cooldown: 8.0
                mana_cost: 10

                damage:
                  base: 50.0
                  stat_multiplier:
                    stat: STRENGTH
                    multiplier: 1.5
                  level_multiplier: 10.0

                skill_tree:
                  parent: none
                  cost: 1

                icon_material: IRON_SWORD
                available_classes:
                  - Warrior
                """;

        Files.writeString(skillFile, legacyYaml);

        // スキル読み込み
        List<Skill> skills = loader.loadAllSkills();

        // 検証
        assertFalse(skills.isEmpty(), "スキルが読み込まれるべき");
        Skill skill = skills.get(0);
        assertEquals("test_skill", skill.getId());
        assertEquals("テストスキル", skill.getName());
        assertEquals(5, skill.getMaxLevel());
        assertEquals(8.0, skill.getCooldown());
        assertEquals(10, skill.getManaCost());
        assertNotNull(skill.getDamage());
    }

    /**
     * 新YAML形式のvariablesセクションテスト
     */
    @Test
    void testNewYamlFormatWithVariables() throws IOException {
        Path skillFile = tempDir.resolve("skills").resolve("test_variables.yml");
        Files.createDirectories(skillFile.getParent());

        String newYaml = """
                id: test_variables
                name: 変数テスト
                display_name: "&e変数テスト"
                type: active
                max_level: 5

                variables:
                  base_mod: 1.0
                  str_scale: 1.5

                cooldown:
                  base: 5.0
                  per_level: -0.5
                  min: 1.0

                cost:
                  type: mana
                  base: 10
                  per_level: -1
                  min: 0
                """;

        Files.writeString(skillFile, newYaml);

        List<Skill> skills = loader.loadAllSkills();

        assertFalse(skills.isEmpty());
        Skill skill = skills.get(0);
        assertTrue(skill.hasVariables(), "カスタム変数が存在すべき");
        
        List<Skill.VariableDefinition> vars = skill.getVariables();
        assertFalse(vars.isEmpty());
        
        // 変数マップの検証
        var varMap = skill.getVariableMap();
        assertEquals(1.0, varMap.get("base_mod"));
        assertEquals(1.5, varMap.get("str_scale"));
    }

    /**
     * 数式ダメージ設定のテスト
     */
    @Test
    void testFormulaDamageConfig() throws IOException {
        Path skillFile = tempDir.resolve("skills").resolve("test_formula.yml");
        Files.createDirectories(skillFile.getParent());

        String formulaYaml = """
                id: test_formula
                name: 数式テスト
                type: active
                max_level: 5

                variables:
                  str_scale: 2.0

                damage:
                  formula: "STR * str_scale + Lv * 5"
                """;

        Files.writeString(skillFile, formulaYaml);

        List<Skill> skills = loader.loadAllSkills();

        assertFalse(skills.isEmpty());
        Skill skill = skills.get(0);
        assertTrue(skill.hasFormulaDamage(), "数式ダメージ設定が存在すべき");

        Skill.FormulaDamageConfig formulaDamage = skill.getFormulaDamage();
        assertNotNull(formulaDamage);
        assertEquals("STR * str_scale + Lv * 5", formulaDamage.getFormula());
        assertEquals("STR * str_scale + Lv * 5", formulaDamage.getFormula(1));
    }

    /**
     * レベル別数式のテスト
     */
    @Test
    void testLevelBasedFormulas() throws IOException {
        Path skillFile = tempDir.resolve("skills").resolve("test_level_formula.yml");
        Files.createDirectories(skillFile.getParent());

        String levelFormulaYaml = """
                id: test_level_formula
                name: レベル別数式テスト
                type: active
                max_level: 10

                damage:
                  formula: "STR * 2"
                  levels:
                    1: "STR * 2"
                    5: "STR * 3"
                    10: "STR * 5"
                """;

        Files.writeString(skillFile, levelFormulaYaml);

        List<Skill> skills = loader.loadAllSkills();

        assertFalse(skills.isEmpty());
        Skill skill = skills.get(0);
        assertTrue(skill.hasFormulaDamage());

        Skill.FormulaDamageConfig formulaDamage = skill.getFormulaDamage();
        assertTrue(formulaDamage.hasLevelFormulas(), "レベル別数式が存在すべき");

        assertEquals("STR * 2", formulaDamage.getFormula(1));
        assertEquals("STR * 3", formulaDamage.getFormula(5));
        assertEquals("STR * 5", formulaDamage.getFormula(10));
        assertEquals("STR * 2", formulaDamage.getFormula(3)); // 未定義レベルは基本数式
    }

    /**
     * ターゲット設定のテスト
     */
    @Test
    void testTargetingConfig() throws IOException {
        Path skillFile = tempDir.resolve("skills").resolve("test_targeting.yml");
        Files.createDirectories(skillFile.getParent());

        String targetingYaml = """
                id: test_targeting
                name: ターゲットテスト
                type: active
                max_level: 5

                targeting:
                  type: cone
                  cone:
                    angle: 90
                    range: 5.0
                """;

        Files.writeString(skillFile, targetingYaml);

        List<Skill> skills = loader.loadAllSkills();

        assertFalse(skills.isEmpty());
        Skill skill = skills.get(0);
        assertTrue(skill.hasTargeting(), "ターゲット設定が存在すべき");

        Skill.TargetingConfig targeting = skill.getTargeting();
        assertEquals("cone", targeting.getType());
        assertNotNull(targeting.getParams());

        assertTrue(targeting.getParams() instanceof Skill.TargetingConfig.ConeParams);
        Skill.TargetingConfig.ConeParams coneParams = 
            (Skill.TargetingConfig.ConeParams) targeting.getParams();
        assertEquals(90.0, coneParams.getAngle());
        assertEquals(5.0, coneParams.getRange());
    }

    /**
     * 球形ターゲットのテスト
     */
    @Test
    void testSphereTargeting() throws IOException {
        Path skillFile = tempDir.resolve("skills").resolve("test_sphere.yml");
        Files.createDirectories(skillFile.getParent());

        String sphereYaml = """
                id: test_sphere
                name: 球形範囲テスト
                type: active
                max_level: 5

                targeting:
                  type: sphere
                  sphere:
                    radius: 3.0
                """;

        Files.writeString(skillFile, sphereYaml);

        List<Skill> skills = loader.loadAllSkills();

        assertFalse(skills.isEmpty());
        Skill skill = skills.get(0);
        assertTrue(skill.hasTargeting());

        Skill.TargetingConfig targeting = skill.getTargeting();
        assertEquals("sphere", targeting.getType());

        assertTrue(targeting.getParams() instanceof Skill.TargetingConfig.SphereParams);
        Skill.TargetingConfig.SphereParams sphereParams = 
            (Skill.TargetingConfig.SphereParams) targeting.getParams();
        assertEquals(3.0, sphereParams.getRadius());
    }

    /**
     * レベル依存パラメータのテスト
     */
    @Test
    void testLevelDependentParameters() throws IOException {
        Path skillFile = tempDir.resolve("skills").resolve("test_ldp.yml");
        Files.createDirectories(skillFile.getParent());

        String ldpYaml = """
                id: test_ldp
                name: レベル依存パラメータテスト
                type: active
                max_level: 5

                cooldown:
                  base: 10.0
                  per_level: -1.0
                  min: 5.0

                cost:
                  type: mana
                  base: 20
                  per_level: 2
                  max: 30
                """;

        Files.writeString(skillFile, ldpYaml);

        List<Skill> skills = loader.loadAllSkills();

        assertFalse(skills.isEmpty());
        Skill skill = skills.get(0);

        // クールダウンの検証
        assertEquals(10.0, skill.getCooldown(1));
        assertEquals(9.0, skill.getCooldown(2));
        assertEquals(6.0, skill.getCooldown(5)); // 10 + (-1) * 4 = 6
        assertEquals(5.0, skill.getCooldown(10)); // min制限: 10 + (-1) * 9 = 1 -> min=5

        // コストの検証
        assertEquals(20, skill.getCost(1));
        assertEquals(22, skill.getCost(2));
        assertEquals(28, skill.getCost(5)); // 20 + 2 * 4 = 28
        assertEquals(30, skill.getCost(10)); // 20 + 2 * 9 = 38 -> max=30で制限
    }

    /**
     * 数式バリデーションのテスト
     */
    @Test
    void testFormulaValidation() throws IOException {
        // 無効な数式（括弧の不一致）
        Path invalidFile = tempDir.resolve("skills").resolve("test_invalid_formula.yml");
        Files.createDirectories(invalidFile.getParent());

        String invalidYaml = """
                id: test_invalid
                name: 無効な数式テスト
                type: active
                max_level: 5

                damage:
                  formula: "STR * (2 + 3"
                """;

        Files.writeString(invalidFile, invalidYaml);

        List<Skill> skills = loader.loadAllSkills();

        // 無効な数式は読み込まれない（またはnullとして扱われる）
        // 実装に応じて適切に検証
    }

    /**
     * 全セクション統合テスト
     */
    @Test
    void testFullNewFormat() throws IOException {
        Path skillFile = tempDir.resolve("skills").resolve("test_full.yml");
        Files.createDirectories(skillFile.getParent());

        String fullYaml = """
                id: test_full
                name: 全機能テスト
                display_name: "&e全機能テスト"
                type: active
                description:
                  - "&c新しいフォーマットの全機能"
                max_level: 10

                variables:
                  base_mod: 1.0
                  str_scale: 1.5
                  int_mod: 2.0

                cost:
                  type: mana
                  base: 15
                  per_level: 2
                  max: 50

                cooldown:
                  base: 6.0
                  per_level: -0.5
                  min: 3.0

                damage:
                  formula: "STR * str_scale + INT * int_mod + (Lv * 5)"
                  levels:
                    5: "STR * 2.0 + INT * 2.5 + 50"
                    10: "STR * 3.0 + INT * 3.0 + 100"

                targeting:
                  type: sector
                  sector:
                    angle: 120
                    radius: 6.0

                skill_tree:
                  parent: none
                  unlock_requirements:
                    - type: level
                      value: 5
                  cost: 2

                icon_material: DIAMOND_SWORD
                available_classes:
                  - Warrior
                  - Samurai
                """;

        Files.writeString(skillFile, fullYaml);

        List<Skill> skills = loader.loadAllSkills();

        assertFalse(skills.isEmpty());
        Skill skill = skills.get(0);

        // 全フィールドの検証
        assertEquals("test_full", skill.getId());
        assertEquals("全機能テスト", skill.getName());
        assertEquals(10, skill.getMaxLevel());
        assertTrue(skill.hasVariables());
        assertTrue(skill.hasFormulaDamage());
        assertTrue(skill.hasTargeting());
        assertNotNull(skill.getSkillTree());

        // 変数の検証
        assertEquals(3, skill.getVariables().size());
        assertEquals(1.5, skill.getVariableMap().get("str_scale"));

        // 数式の検証
        Skill.FormulaDamageConfig formulaDamage = skill.getFormulaDamage();
        assertTrue(formulaDamage.hasLevelFormulas());

        // ターゲットの検証
        Skill.TargetingConfig targeting = skill.getTargeting();
        assertEquals("sector", targeting.getType());
        assertTrue(targeting.getParams() instanceof Skill.TargetingConfig.SectorParams);
    }

    // ===== 追加: 新YAMLフォーマットの拡張テスト =====

    /**
     * セクターターゲットのテスト
     */
    @Test
    void testSectorTargeting() throws IOException {
        Path skillFile = tempDir.resolve("skills").resolve("test_sector.yml");
        Files.createDirectories(skillFile.getParent());

        String sectorYaml = """
                id: test_sector
                name: セクターテスト
                type: active
                max_level: 5

                targeting:
                  type: sector
                  sector:
                    angle: 120
                    radius: 6.0
                """;

        Files.writeString(skillFile, sectorYaml);

        List<Skill> skills = loader.loadAllSkills();

        assertFalse(skills.isEmpty());
        Skill skill = skills.get(0);
        assertTrue(skill.hasTargeting());

        Skill.TargetingConfig targeting = skill.getTargeting();
        assertEquals("sector", targeting.getType());
        assertTrue(targeting.getParams() instanceof Skill.TargetingConfig.SectorParams);

        Skill.TargetingConfig.SectorParams sectorParams =
                (Skill.TargetingConfig.SectorParams) targeting.getParams();
        assertEquals(120.0, sectorParams.getAngle());
        assertEquals(6.0, sectorParams.getRadius());
    }


    /**
     * 代替コストタイプのテスト
     */
    @Test
    void testAlternativeCostType() throws IOException {
        Path skillFile = tempDir.resolve("skills").resolve("test_stamina.yml");
        Files.createDirectories(skillFile.getParent());

        String staminaYaml = """
                id: test_stamina
                name: スタミナ消費テスト
                type: active
                max_level: 5

                cost:
                  type: stamina
                  base: 15
                  per_level: 2
                """;

        Files.writeString(skillFile, staminaYaml);

        List<Skill> skills = loader.loadAllSkills();

        assertFalse(skills.isEmpty());
        Skill skill = skills.get(0);
        // staminaはサポートされていないのでデフォルトのMANAになる
        assertEquals(SkillCostType.MANA, skill.getCostType());
    }

    /**
     * パッシブスキルのテスト
     */
    @Test
    void testPassiveSkill() throws IOException {
        Path skillFile = tempDir.resolve("skills").resolve("test_passive.yml");
        Files.createDirectories(skillFile.getParent());

        String passiveYaml = """
                id: test_passive
                name: パッシブスキル
                type: passive
                max_level: 3
                description:
                  - "常時発動するパッシブ"
                """;

        Files.writeString(skillFile, passiveYaml);

        List<Skill> skills = loader.loadAllSkills();

        assertFalse(skills.isEmpty());
        Skill skill = skills.get(0);
        // パッシブ/アクティブの区別を廃止したため、テストを削除
    }

    /**
     * スキルツリー設定の詳細テスト
     */
    @Test
    void testSkillTreeDetails() throws IOException {
        Path skillFile = tempDir.resolve("skills").resolve("test_skilltree.yml");
        Files.createDirectories(skillFile.getParent());

        String skillTreeYaml = """
                id: test_skilltree
                name: スキルツリー詳細テスト
                type: active
                max_level: 5

                skill_tree:
                  parent: base_skill
                  cost: 3
                  unlock_requirements:
                    - type: level
                      value: 10
                    - type: skill
                      skill_id: base_skill
                      level: 3
                """;

        Files.writeString(skillFile, skillTreeYaml);

        List<Skill> skills = loader.loadAllSkills();

        assertFalse(skills.isEmpty());
        Skill skill = skills.get(0);
        assertNotNull(skill.getSkillTree());
        assertEquals("base_skill", skill.getSkillTree().getParent());
        assertEquals(3, skill.getSkillTree().getCost());
    }

    /**
     * 複数スキルファイルの読み込みテスト
     */
    @Test
    void testMultipleSkillFiles() throws IOException {
        Path skillsDir = tempDir.resolve("skills");
        Files.createDirectories(skillsDir);

        // 複数のスキルファイルを作成
        Files.writeString(skillsDir.resolve("skill1.yml"), """
                id: skill1
                name: スキル1
                type: active
                max_level: 5
                """);

        Files.writeString(skillsDir.resolve("skill2.yml"), """
                id: skill2
                name: スキル2
                type: passive
                max_level: 3
                """);

        List<Skill> skills = loader.loadAllSkills();

        assertEquals(2, skills.size());
        assertTrue(skills.stream().anyMatch(s -> s.getId().equals("skill1")));
        assertTrue(skills.stream().anyMatch(s -> s.getId().equals("skill2")));
    }

    /**
     * 空のスキルディレクトリのテスト
     */
    @Test
    void testEmptySkillDirectory() throws IOException {
        Path skillsDir = tempDir.resolve("skills").resolve("empty");
        Files.createDirectories(skillsDir);

        List<Skill> skills = loader.loadAllSkills();

        assertTrue(skills.isEmpty());
    }

    /**
     * 無効なYAML形式のエラーハンドリングテスト
     */
    @Test
    void testInvalidYamlFormat() throws IOException {
        Path skillFile = tempDir.resolve("skills").resolve("test_invalid.yml");
        Files.createDirectories(skillFile.getParent());

        String invalidYaml = """
                id: test_invalid
                name: 無効なYAML
                type: active
                max_level: [this, is, wrong]
                """;

        Files.writeString(skillFile, invalidYaml);

        // エラーが発生してもクラッシュしない
        List<Skill> skills = loader.loadAllSkills();
        // 実装により、空リストまたは部分的に読み込まれたリスト
        assertNotNull(skills);
    }
}
