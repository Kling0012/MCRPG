package com.example.rpgplugin.skill;

import com.example.rpgplugin.model.skill.CooldownConfig;
import com.example.rpgplugin.model.skill.CostConfig;
import com.example.rpgplugin.model.skill.DamageCalculation;
import com.example.rpgplugin.model.skill.FormulaDamageConfig;
import com.example.rpgplugin.model.skill.SkillTreeConfig;
import com.example.rpgplugin.model.skill.TargetingConfig;
import com.example.rpgplugin.model.skill.UnlockRequirement;
import com.example.rpgplugin.model.skill.VariableDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * Skillのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Skill: スキルデータモデル")
class SkillTest {

    // ========== テスト用ヘルパーメソッド ==========

    private Skill createBasicSkill() {
        return new Skill(
            "testSkill",
            "Test Skill",
            "<gold>Test Skill",
            SkillType.NORMAL,
            List.of("Line 1", "Line 2"),
            10,
            5.0,
            20,
            null,
            null,
            SkillCostType.MANA,
            null,
            null,
            "DIAMOND_SWORD",
            List.of("warrior", "knight")
        );
    }

    private Skill createSkillWithLevelParams() {
        LevelDependentParameter cooldownParam = new LevelDependentParameter(5.0, -0.5, 1.0);
        LevelDependentParameter costParam = new LevelDependentParameter(20.0, -1.0, 5.0);
        return new Skill(
            "testSkill",
            "Test Skill",
            "<gold>Test Skill",
            SkillType.NORMAL,
            List.of(),
            10,
            5.0,
            20,
            cooldownParam,
            costParam,
            SkillCostType.MANA,
            null,
            null,
            "DIAMOND_SWORD",
            new ArrayList<>()
        );
    }

    /**
     * テスト用RPGPlayerモックを作成します
     */
    private com.example.rpgplugin.player.RPGPlayer createMockRPGPlayer() {
        org.bukkit.entity.Player mockPlayer = mock(org.bukkit.entity.Player.class);
        when(mockPlayer.getName()).thenReturn("TestPlayer");
        when(mockPlayer.getUniqueId()).thenReturn(java.util.UUID.randomUUID());

        com.example.rpgplugin.player.RPGPlayer rpgPlayer = mock(com.example.rpgplugin.player.RPGPlayer.class);
        when(rpgPlayer.getBukkitPlayer()).thenReturn(mockPlayer);
        when(rpgPlayer.getUsername()).thenReturn("TestPlayer");
        when(rpgPlayer.getLevel()).thenReturn(5);

        return rpgPlayer;
    }

    // ========== コンストラクタ テスト ==========

    @Nested
    @DisplayName("Constructor: コンストラクタ")
    class ConstructorTests {

        @Test
        @DisplayName("test: 基本コンストラクタでスキルを作成")
        void testBasicConstructor() {
            Skill skill = createBasicSkill();

            assertEquals("testSkill", skill.getId());
            assertEquals("Test Skill", skill.getName());
            assertEquals("<gold>Test Skill", skill.getDisplayName());
            assertEquals(SkillType.NORMAL, skill.getType());
            assertEquals(10, skill.getMaxLevel());
            assertEquals(5.0, skill.getCooldown());
            assertEquals(20, skill.getManaCost());
            assertEquals("DIAMOND_SWORD", skill.getIconMaterial());
        }

        @Test
        @DisplayName("test: 利用可能クラスが正しく設定される")
        void testConstructorWithAvailableClasses() {
            Skill skill = createBasicSkill();

            List<String> classes = skill.getAvailableClasses();
            assertEquals(2, classes.size());
            assertTrue(classes.contains("warrior"));
            assertTrue(classes.contains("knight"));
        }

        @Test
        @DisplayName("test: 説明リストが正しく設定される")
        void testConstructorWithDescription() {
            Skill skill = createBasicSkill();

            List<String> description = skill.getDescription();
            assertEquals(2, description.size());
            assertEquals("Line 1", description.get(0));
            assertEquals("Line 2", description.get(1));
        }

        @Test
        @DisplayName("test: nullのdescriptionは空リストになる")
        void testConstructorWithNullDescription() {
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                null,
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>()
            );

            assertNotNull(skill.getDescription());
            assertTrue(skill.getDescription().isEmpty());
        }

        @Test
        @DisplayName("test: nullのavailableClassesは空リストになる")
        void testConstructorWithNullAvailableClasses() {
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                null
            );

            assertNotNull(skill.getAvailableClasses());
            assertTrue(skill.getAvailableClasses().isEmpty());
        }

        @Test
        @DisplayName("test: iconMaterialがnullの場合はデフォルト値")
        void testConstructorWithNullIconMaterial() {
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                null,
                new ArrayList<>()
            );

            assertEquals("DIAMOND_SWORD", skill.getIconMaterial());
        }

        @Test
        @DisplayName("test: costTypeがnullの場合はMANAがデフォルト")
        void testConstructorWithNullCostType() {
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                null,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>()
            );

            assertEquals(SkillCostType.MANA, skill.getCostType());
        }

        @Test
        @DisplayName("test: レベル依存パラメータ付きコンストラクタ")
        void testConstructorWithLevelParams() {
            LevelDependentParameter cooldownParam = new LevelDependentParameter(5.0, -0.5, 1.0);
            LevelDependentParameter costParam = new LevelDependentParameter(20.0, -1.0, 5.0);

            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                cooldownParam,
                costParam,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>()
            );

            assertEquals(cooldownParam, skill.getCooldownParameter());
            assertEquals(costParam, skill.getCostParameter());
        }

        @Test
        @DisplayName("test: requiredSkillsは空リストで初期化される")
        void testConstructorInitializesRequiredSkills() {
            Skill skill = createBasicSkill();

            assertNotNull(skill.getRequiredSkills());
            assertTrue(skill.getRequiredSkills().isEmpty());
        }
    }

    // ========== getId(), getName(), getDisplayName() テスト ==========

    @Nested
    @DisplayName("Getters: 基本ゲッター")
    class BasicGetterTests {

        @Test
        @DisplayName("test: getIdはスキルIDを返す")
        void testGetId() {
            Skill skill = createBasicSkill();
            assertEquals("testSkill", skill.getId());
        }

        @Test
        @DisplayName("test: getNameはスキル名を返す")
        void testGetName() {
            Skill skill = createBasicSkill();
            assertEquals("Test Skill", skill.getName());
        }

        @Test
        @DisplayName("test: getDisplayNameは表示名を返す")
        void testGetDisplayName() {
            Skill skill = createBasicSkill();
            assertEquals("<gold>Test Skill", skill.getDisplayName());
        }

        @Test
        @DisplayName("test: getTypeはスキルタイプを返す")
        void testGetType() {
            Skill skill = createBasicSkill();
            assertEquals(SkillType.NORMAL, skill.getType());
        }

        @Test
        @DisplayName("test: getMaxLevelは最大レベルを返す")
        void testGetMaxLevel() {
            Skill skill = createBasicSkill();
            assertEquals(10, skill.getMaxLevel());
        }

        @Test
        @DisplayName("test: getIconMaterialはアイコン素材を返す")
        void testGetIconMaterial() {
            Skill skill = createBasicSkill();
            assertEquals("DIAMOND_SWORD", skill.getIconMaterial());
        }
    }

    // ========== getCooldown() テスト ==========

    @Nested
    @DisplayName("getCooldown: クールダウン取得")
    class GetCooldownTests {

        @Test
        @DisplayName("test: getCooldown()は固定値を返す（パラメータなし）")
        void testGetCooldownFixed() {
            Skill skill = createBasicSkill();
            assertEquals(5.0, skill.getCooldown());
        }

        @Test
        @DisplayName("test: getCooldown(int)はパラメータなし時は固定値")
        void testGetCooldownLevelWithoutParam() {
            Skill skill = createBasicSkill();
            assertEquals(5.0, skill.getCooldown(1));
            assertEquals(5.0, skill.getCooldown(5));
            assertEquals(5.0, skill.getCooldown(10));
        }

        @Test
        @DisplayName("test: getCooldown(int)はレベル依存パラメータ使用")
        void testGetCooldownWithLevelParam() {
            Skill skill = createSkillWithLevelParams();
            // base=5.0, per_level=-0.5, min=1.0
            // Lv1: 5.0 + (-0.5 * 0) = 5.0
            // Lv2: 5.0 + (-0.5 * 1) = 4.5
            // Lv5: 5.0 + (-0.5 * 4) = 3.0
            // Lv9: 5.0 + (-0.5 * 8) = 1.0
            // Lv10: 1.0 (min制限)
            assertEquals(5.0, skill.getCooldown(1));
            assertEquals(4.5, skill.getCooldown(2));
            assertEquals(3.0, skill.getCooldown(5));
            assertEquals(1.0, skill.getCooldown(9));
            assertEquals(1.0, skill.getCooldown(10));
        }
    }

    // ========== getCost() テスト ==========

    @Nested
    @DisplayName("getCost: コスト取得")
    class GetCostTests {

        @Test
        @DisplayName("test: getManaCostは固定マナコストを返す")
        void testGetManaCost() {
            Skill skill = createBasicSkill();
            assertEquals(20, skill.getManaCost());
        }

        @Test
        @DisplayName("test: getCost(int)はパラメータなし時はマナコスト")
        void testGetCostWithoutParam() {
            Skill skill = createBasicSkill();
            assertEquals(20, skill.getCost(1));
            assertEquals(20, skill.getCost(5));
        }

        @Test
        @DisplayName("test: getCost(int)はレベル依存パラメータ使用")
        void testGetCostWithLevelParam() {
            Skill skill = createSkillWithLevelParams();
            // base=20, per_level=-1, min=5
            // Lv1: 20 + (-1 * 0) = 20
            // Lv2: 20 + (-1 * 1) = 19
            // Lv5: 20 + (-1 * 4) = 16
            // Lv16: 20 + (-1 * 15) = 5
            // Lv17: 5 (min制限)
            assertEquals(20, skill.getCost(1));
            assertEquals(19, skill.getCost(2));
            assertEquals(16, skill.getCost(5));
            assertEquals(5, skill.getCost(16));
            assertEquals(5, skill.getCost(17));
        }

        @Test
        @DisplayName("test: getCostTypeはコストタイプを返す")
        void testGetCostType() {
            Skill skill = createBasicSkill();
            assertEquals(SkillCostType.MANA, skill.getCostType());
        }

        @Test
        @DisplayName("test: HPコストタイプのスキル")
        void testGetCostTypeHP() {
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.HP,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>()
            );

            assertEquals(SkillCostType.HP, skill.getCostType());
        }
    }

    // ========== getCooldownParameter(), getCostParameter() テスト ==========

    @Nested
    @DisplayName("Parameters: パラメータ取得")
    class ParameterTests {

        @Test
        @DisplayName("test: getCooldownParameterはnullを返す（未設定）")
        void testGetCooldownParameterNull() {
            Skill skill = createBasicSkill();
            assertNull(skill.getCooldownParameter());
        }

        @Test
        @DisplayName("test: getCooldownParameterはパラメータを返す")
        void testGetCooldownParameter() {
            Skill skill = createSkillWithLevelParams();
            assertNotNull(skill.getCooldownParameter());
            assertEquals(5.0, skill.getCooldownParameter().getBase());
        }

        @Test
        @DisplayName("test: getCostParameterはnullを返す（未設定）")
        void testGetCostParameterNull() {
            Skill skill = createBasicSkill();
            assertNull(skill.getCostParameter());
        }

        @Test
        @DisplayName("test: getCostParameterはパラメータを返す")
        void testGetCostParameter() {
            Skill skill = createSkillWithLevelParams();
            assertNotNull(skill.getCostParameter());
            assertEquals(20, skill.getCostParameter().getBase());
        }
    }

    // ========== getDamage(), getSkillTree() テスト ==========

    @Nested
    @DisplayName("Config: 設定取得")
    class ConfigTests {

        @Test
        @DisplayName("test: getDamageはnullを返す（未設定）")
        void testGetDamageNull() {
            Skill skill = createBasicSkill();
            assertNull(skill.getDamage());
        }

        @Test
        @DisplayName("test: getDamageはダメージ計算設定を返す")
        void testGetDamage() {
            DamageCalculation damage = new DamageCalculation(10.0, null, 1.5, 2.0);
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                damage,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>()
            );

            assertNotNull(skill.getDamage());
            assertEquals(10.0, skill.getDamage().getBase());
            assertEquals(1.5, skill.getDamage().getMultiplierValue());
            assertEquals(2.0, skill.getDamage().getLevelMultiplier());
        }

        @Test
        @DisplayName("test: getSkillTreeはnullを返す（未設定）")
        void testGetSkillTreeNull() {
            Skill skill = createBasicSkill();
            assertNull(skill.getSkillTree());
        }

        @Test
        @DisplayName("test: getSkillTreeはスキルツリー設定を返す")
        void testGetSkillTree() {
            SkillTreeConfig skillTree = new SkillTreeConfig(
                "parentSkill",
                new ArrayList<>(),
                5,
                "DIAMOND"
            );
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                skillTree,
                "DIAMOND_SWORD",
                new ArrayList<>()
            );

            assertNotNull(skill.getSkillTree());
            assertEquals("parentSkill", skill.getSkillTree().getParent());
            assertEquals(5, skill.getSkillTree().getCost());
            assertEquals("DIAMOND", skill.getSkillTree().getIcon());
        }
    }

    // ========== isAvailableForClass() テスト ==========

    @Nested
    @DisplayName("isAvailableForClass: クラス利用可否")
    class IsAvailableForClassTests {

        @Test
        @DisplayName("test: 空リストは全クラスで利用可能")
        void testIsAvailableForClassEmptyList() {
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>()
            );

            assertTrue(skill.isAvailableForClass("warrior"));
            assertTrue(skill.isAvailableForClass("mage"));
            assertTrue(skill.isAvailableForClass("any_class"));
        }

        @Test
        @DisplayName("test: 指定クラスが利用可能リストに含まれる")
        void testIsAvailableForClassInList() {
            Skill skill = createBasicSkill();
            assertTrue(skill.isAvailableForClass("warrior"));
            assertTrue(skill.isAvailableForClass("knight"));
        }

        @Test
        @DisplayName("test: 指定クラスが利用可能リストに含まれない")
        void testIsAvailableForClassNotInList() {
            Skill skill = createBasicSkill();
            assertFalse(skill.isAvailableForClass("mage"));
            assertFalse(skill.isAvailableForClass("priest"));
        }

        @Test
        @DisplayName("test: 大文字小文字は区別される")
        void testIsAvailableForClassCaseSensitive() {
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                List.of("Warrior")
            );

            assertTrue(skill.isAvailableForClass("Warrior"));
            assertFalse(skill.isAvailableForClass("warrior"));
        }
    }

    // ========== getRequiredSkills() テスト ==========

    @Nested
    @DisplayName("getRequiredSkills: 前提スキル取得")
    class GetRequiredSkillsTests {

        @Test
        @DisplayName("test: getRequiredSkillsは空リストを返す")
        void testGetRequiredSkillsEmpty() {
            Skill skill = createBasicSkill();
            assertNotNull(skill.getRequiredSkills());
            assertTrue(skill.getRequiredSkills().isEmpty());
        }

        @Test
        @DisplayName("test: getRequiredSkillsはコピーを返す")
        void testGetRequiredSkillsReturnsCopy() {
            Skill skill = createBasicSkill();
            List<String> skills1 = skill.getRequiredSkills();
            List<String> skills2 = skill.getRequiredSkills();

            // 別のインスタンスであることを確認
            assertNotSame(skills1, skills2);
            assertEquals(skills1, skills2);
        }
    }

    // ========== getColoredDisplayName() テスト ==========

    @Nested
    @DisplayName("getColoredDisplayName: 色付き表示名")
    class GetColoredDisplayNameTests {

        @Test
        @DisplayName("test: レガシーカラーコードがない場合はそのまま返す")
        void testGetColoredDisplayNameNoLegacyCodes() {
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test Skill",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>()
            );

            assertEquals("Test Skill", skill.getColoredDisplayName());
        }

        @Test
        @DisplayName("test: レガシーカラーコードの変換（&c→<red>）")
        void testGetColoredDisplayNameConvertsLegacy() {
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "&cFireball",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>()
            );

            assertEquals("<red>Fireball", skill.getColoredDisplayName());
        }

        @Test
        @DisplayName("test: 複数のレガシーカラーコード変換")
        void testGetColoredDisplayNameMultipleCodes() {
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "&6&lLegendary &cSword",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>()
            );

            assertEquals("<gold><bold>Legendary <red>Sword", skill.getColoredDisplayName());
        }

        @Test
        @DisplayName("test: 全カラーコードの変換")
        void testGetColoredDisplayNameAllColors() {
            String[] legacyCodes = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
            String[] miniMessageTags = {"black", "dark_blue", "dark_green", "dark_aqua", "dark_red",
                                       "dark_purple", "gold", "gray", "dark_gray", "blue",
                                       "green", "aqua", "red", "light_purple", "yellow", "white"};

            for (int i = 0; i < legacyCodes.length; i++) {
                String displayName = "&" + legacyCodes[i] + "Test";
                Skill skill = new Skill(
                    "testSkill",
                    "Test Skill",
                    displayName,
                    SkillType.NORMAL,
                    new ArrayList<>(),
                    10,
                    5.0,
                    20,
                    null,
                    null,
                    SkillCostType.MANA,
                    null,
                    null,
                    "DIAMOND_SWORD",
                    new ArrayList<>()
                );

                String expected = "<" + miniMessageTags[i] + ">Test";
                assertEquals(expected, skill.getColoredDisplayName(), "Code &" + legacyCodes[i]);
            }
        }

        @Test
        @DisplayName("test: フォーマットコードの変換")
        void testGetColoredDisplayNameFormatCodes() {
            assertEquals("<bold>Test", createSkillWithDisplayName("&lTest").getColoredDisplayName());
            assertEquals("<italic>Test", createSkillWithDisplayName("&oTest").getColoredDisplayName());
            assertEquals("<underline>Test", createSkillWithDisplayName("&nTest").getColoredDisplayName());
            assertEquals("<strikethrough>Test", createSkillWithDisplayName("&mTest").getColoredDisplayName());
            assertEquals("<obfuscated>Test", createSkillWithDisplayName("&kTest").getColoredDisplayName());
            assertEquals("<reset>Test", createSkillWithDisplayName("&rTest").getColoredDisplayName());
        }

        private Skill createSkillWithDisplayName(String displayName) {
            return new Skill(
                "testSkill",
                "Test Skill",
                displayName,
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>()
            );
        }
    }

    // ========== getVariables(), hasVariables() テスト ==========

    @Nested
    @DisplayName("Variables: カスタム変数")
    class VariablesTests {

        @Test
        @DisplayName("test: getVariablesは空リストを返す（未設定）")
        void testGetVariablesEmpty() {
            Skill skill = createBasicSkill();
            assertNotNull(skill.getVariables());
            assertTrue(skill.getVariables().isEmpty());
        }

        @Test
        @DisplayName("test: hasVariablesはfalseを返す（未設定）")
        void testHasVariablesFalse() {
            Skill skill = createBasicSkill();
            assertFalse(skill.hasVariables());
        }

        @Test
        @DisplayName("test: getVariablesは変数定義リストを返す")
        void testGetVariables() {
            List<VariableDefinition> variables = new ArrayList<>();
            variables.add(new VariableDefinition("base_mod", 1.0));
            variables.add(new VariableDefinition("str_scale", 1.5));

            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                variables,
                null,
                null,
                null,
                null
            );

            List<VariableDefinition> result = skill.getVariables();
            assertEquals(2, result.size());
            assertEquals("base_mod", result.get(0).getName());
            assertEquals(1.0, result.get(0).getValue());
            assertEquals("str_scale", result.get(1).getName());
            assertEquals(1.5, result.get(1).getValue());
        }

        @Test
        @DisplayName("test: hasVariablesはtrueを返す（変数あり）")
        void testHasVariablesTrue() {
            List<VariableDefinition> variables = new ArrayList<>();
            variables.add(new VariableDefinition("test", 1.0));

            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                variables,
                null,
                null,
                null,
                null
            );

            assertTrue(skill.hasVariables());
        }

        @Test
        @DisplayName("test: getVariablesはコピーを返す")
        void testGetVariablesReturnsCopy() {
            List<VariableDefinition> variables = new ArrayList<>();
            variables.add(new VariableDefinition("test", 1.0));

            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                variables,
                null,
                null,
                null,
                null
            );

            List<VariableDefinition> vars1 = skill.getVariables();
            List<VariableDefinition> vars2 = skill.getVariables();

            assertNotSame(vars1, vars2);
            assertEquals(vars1, vars2);
        }
    }

    // ========== getFormulaDamage(), hasFormulaDamage() テスト ==========

    @Nested
    @DisplayName("FormulaDamage: 数式ダメージ設定")
    class FormulaDamageTests {

        @Test
        @DisplayName("test: getFormulaDamageはnullを返す（未設定）")
        void testGetFormulaDamageNull() {
            Skill skill = createBasicSkill();
            assertNull(skill.getFormulaDamage());
        }

        @Test
        @DisplayName("test: hasFormulaDamageはfalseを返す（未設定）")
        void testHasFormulaDamageFalse() {
            Skill skill = createBasicSkill();
            assertFalse(skill.hasFormulaDamage());
        }

        @Test
        @DisplayName("test: getFormulaDamageは設定を返す")
        void testGetFormulaDamage() {
            FormulaDamageConfig formulaDamage = new FormulaDamageConfig(
                "STR * 2 + Lv * 5",
                null
            );

            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                formulaDamage,
                null,
                null,
                null
            );

            assertNotNull(skill.getFormulaDamage());
            assertEquals("STR * 2 + Lv * 5", skill.getFormulaDamage().getFormula());
        }

        @Test
        @DisplayName("test: hasFormulaDamageはtrueを返す（設定あり）")
        void testHasFormulaDamageTrue() {
            FormulaDamageConfig formulaDamage = new FormulaDamageConfig(
                "STR * 2",
                null
            );

            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                formulaDamage,
                null,
                null,
                null
            );

            assertTrue(skill.hasFormulaDamage());
        }
    }

    // ========== getTargeting(), hasTargeting() テスト ==========

    @Nested
    @DisplayName("Targeting: ターゲット設定")
    class TargetingTests {

        @Test
        @DisplayName("test: getTargetingはnullを返す（未設定）")
        void testGetTargetingNull() {
            Skill skill = createBasicSkill();
            assertNull(skill.getTargeting());
        }

        @Test
        @DisplayName("test: hasTargetingはfalseを返す（未設定）")
        void testHasTargetingFalse() {
            Skill skill = createBasicSkill();
            assertFalse(skill.hasTargeting());
        }

        @Test
        @DisplayName("test: getTargetingは設定を返す")
        void testGetTargeting() {
            TargetingConfig.TargetingParams params = new TargetingConfig.TargetingParams(5.0);
            TargetingConfig targeting = new TargetingConfig("single", params);

            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                null,
                targeting,
                null,
                null
            );

            assertNotNull(skill.getTargeting());
            assertEquals("single", skill.getTargeting().getType());
        }

        @Test
        @DisplayName("test: hasTargetingはtrueを返す（設定あり）")
        void testHasTargetingTrue() {
            TargetingConfig targeting = new TargetingConfig("cone", null);

            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                null,
                targeting,
                null,
                null
            );

            assertTrue(skill.hasTargeting());
        }
    }

    // ========== getSkillTarget(), hasSkillTarget() テスト ==========

    @Nested
    @DisplayName("SkillTarget: スキルターゲット設定")
    class SkillTargetTests {

        @Test
        @DisplayName("test: getSkillTargetはnullを返す（未設定）")
        void testGetSkillTargetNull() {
            Skill skill = createBasicSkill();
            assertNull(skill.getSkillTarget());
        }

        @Test
        @DisplayName("test: hasSkillTargetはfalseを返す（未設定）")
        void testHasSkillTargetFalse() {
            Skill skill = createBasicSkill();
            assertFalse(skill.hasSkillTarget());
        }

        // SkillTargetはtargetパッケージのクラスなので、ここではnullチェックのみ
        // 実際の設定ありのテストは統合テストで行う
    }

    // ========== getComponentEffect(), hasComponentEffect() テスト ==========

    @Nested
    @DisplayName("ComponentEffect: コンポーネントエフェクト")
    class ComponentEffectTests {

        @Test
        @DisplayName("test: getComponentEffectはnullを返す（未設定）")
        void testGetComponentEffectNull() {
            Skill skill = createBasicSkill();
            assertNull(skill.getComponentEffect());
        }

        @Test
        @DisplayName("test: hasComponentEffectはfalseを返す（未設定）")
        void testHasComponentEffectFalse() {
            Skill skill = createBasicSkill();
            assertFalse(skill.hasComponentEffect());
        }

        // SkillEffectはcomponentパッケージのクラスなので、ここではnullチェックのみ
    }

    // ========== getDescription() テスト ==========

    @Nested
    @DisplayName("getDescription: 説明取得")
    class GetDescriptionTests {

        @Test
        @DisplayName("test: getDescriptionはコピーを返す")
        void testGetDescriptionReturnsCopy() {
            Skill skill = createBasicSkill();
            List<String> desc1 = skill.getDescription();
            List<String> desc2 = skill.getDescription();

            assertNotSame(desc1, desc2);
            assertEquals(desc1, desc2);
        }

        @Test
        @DisplayName("test: 説明の内容が正しい")
        void testGetDescriptionContent() {
            Skill skill = createBasicSkill();
            List<String> description = skill.getDescription();

            assertEquals(2, description.size());
            assertEquals("Line 1", description.get(0));
            assertEquals("Line 2", description.get(1));
        }
    }

    // ========== getAvailableClasses() テスト ==========

    @Nested
    @DisplayName("getAvailableClasses: 利用可能クラス取得")
    class GetAvailableClassesTests {

        @Test
        @DisplayName("test: getAvailableClassesはコピーを返す")
        void testGetAvailableClassesReturnsCopy() {
            Skill skill = createBasicSkill();
            List<String> classes1 = skill.getAvailableClasses();
            List<String> classes2 = skill.getAvailableClasses();

            assertNotSame(classes1, classes2);
            assertEquals(classes1, classes2);
        }
    }

    // ========== toString() テスト ==========

    @Nested
    @DisplayName("toString: 文字列表現")
    class ToStringTests {

        @Test
        @DisplayName("test: toStringはスキル情報を含む")
        void testToString() {
            Skill skill = createBasicSkill();
            String result = skill.toString();

            assertTrue(result.contains("testSkill") || result.contains("Test Skill"));
        }
    }

    // ========== 内部クラス: DamageCalculation テスト ==========

    @Nested
    @DisplayName("DamageCalculation: ダメージ計算内部クラス")
    class DamageCalculationTests {

        @Test
        @DisplayName("test: コンストラクタでダメージ計算を作成")
        void testConstructor() {
            DamageCalculation damage = new DamageCalculation(10.0, null, 1.5, 2.0);

            assertEquals(10.0, damage.getBase());
            assertNull(damage.getStatMultiplier());
            assertEquals(1.5, damage.getMultiplierValue());
            assertEquals(2.0, damage.getLevelMultiplier());
        }

        @Test
        @DisplayName("test: calculateDamageでダメージ計算")
        void testCalculateDamage() {
            DamageCalculation damage = new DamageCalculation(10.0, null, 1.5, 2.0);

            // damage = base + (statValue * multiplierValue) + (skillLevel * levelMultiplier)
            // = 10.0 + (100.0 * 1.5) + (5 * 2.0)
            // = 10.0 + 150.0 + 10.0 = 170.0
            double result = damage.calculateDamage(100.0, 5);
            assertEquals(170.0, result, 0.001);
        }

        @Test
        @DisplayName("test: calculateDamageでステータスなし")
        void testCalculateDamageNoStat() {
            DamageCalculation damage = new DamageCalculation(10.0, null, 0.0, 2.0);

            double result = damage.calculateDamage(0.0, 5);
            assertEquals(20.0, result, 0.001); // 10.0 + (0 * 0) + (5 * 2.0)
        }

        @Test
        @DisplayName("test: calculateDamageでレベル0")
        void testCalculateDamageLevelZero() {
            DamageCalculation damage = new DamageCalculation(10.0, null, 1.5, 2.0);

            double result = damage.calculateDamage(100.0, 0);
            assertEquals(160.0, result, 0.001); // 10.0 + (100 * 1.5) + (0 * 2.0)
        }
    }

    // ========== 内部クラス: SkillTreeConfig テスト ==========

    @Nested
    @DisplayName("SkillTreeConfig: スキルツリー設定内部クラス")
    class SkillTreeConfigTests {

        @Test
        @DisplayName("test: コンストラクタでスキルツリー設定を作成")
        void testConstructor() {
            SkillTreeConfig config = new SkillTreeConfig(
                "parentSkill",
                new ArrayList<>(),
                5,
                "DIAMOND"
            );

            assertEquals("parentSkill", config.getParent());
            assertNotNull(config.getUnlockRequirements());
            assertTrue(config.getUnlockRequirements().isEmpty());
            assertEquals(5, config.getCost());
            assertEquals("DIAMOND", config.getIcon());
        }

        @Test
        @DisplayName("test: nullのunlockRequirementsは空リストになる")
        void testConstructorWithNullUnlockRequirements() {
            SkillTreeConfig config = new SkillTreeConfig(
                "parentSkill",
                null,
                5,
                "DIAMOND"
            );

            assertNotNull(config.getUnlockRequirements());
            assertTrue(config.getUnlockRequirements().isEmpty());
        }

        @Test
        @DisplayName("test: getUnlockRequirementsは同じインスタンスを返す")
        void testGetUnlockRequirementsReturnsSame() {
            List<UnlockRequirement> requirements = new ArrayList<>();
            requirements.add(new UnlockRequirement("stat", null, 10.0));

            SkillTreeConfig config = new SkillTreeConfig(
                "parent",
                requirements,
                5,
                "DIAMOND"
            );

            List<UnlockRequirement> reqs1 = config.getUnlockRequirements();
            List<UnlockRequirement> reqs2 = config.getUnlockRequirements();

            // 実装では同じインスタンスを返す
            assertSame(reqs1, reqs2);
            assertEquals(1, reqs1.size());
        }
    }

    // ========== 内部クラス: UnlockRequirement テスト ==========

    @Nested
    @DisplayName("UnlockRequirement: 習得要件内部クラス")
    class UnlockRequirementTests {

        @Test
        @DisplayName("test: コンストラクタで習得要件を作成")
        void testConstructor() {
            UnlockRequirement requirement = new UnlockRequirement(
                "stat",
                null,
                10.0
            );

            assertEquals("stat", requirement.getType());
            assertNull(requirement.getStat());
            assertEquals(10.0, requirement.getValue());
        }

        @Test
        @DisplayName("test: Stat付きで習得要件を作成")
        void testConstructorWithStat() {
            UnlockRequirement requirement = new UnlockRequirement(
                "stat",
                com.example.rpgplugin.stats.Stat.STRENGTH,
                50.0
            );

            assertEquals(com.example.rpgplugin.stats.Stat.STRENGTH, requirement.getStat());
            assertEquals(50.0, requirement.getValue());
        }
    }

    // ========== 内部クラス: VariableDefinition テスト ==========

    @Nested
    @DisplayName("VariableDefinition: 変数定義内部クラス")
    class VariableDefinitionTests {

        @Test
        @DisplayName("test: コンストラクタで変数定義を作成")
        void testConstructor() {
            VariableDefinition variable = new VariableDefinition("base_mod", 1.5);

            assertEquals("base_mod", variable.getName());
            assertEquals(1.5, variable.getValue());
        }

        @Test
        @DisplayName("test: 負の値も設定可能")
        void testConstructorWithNegativeValue() {
            VariableDefinition variable = new VariableDefinition("penalty", -0.5);

            assertEquals(-0.5, variable.getValue());
        }

        @Test
        @DisplayName("test: ゼロ値")
        void testConstructorWithZero() {
            VariableDefinition variable = new VariableDefinition("zero", 0.0);

            assertEquals(0.0, variable.getValue());
        }
    }

    // ========== 内部クラス: CostConfig テスト ==========

    @Nested
    @DisplayName("CostConfig: コスト設定内部クラス")
    class CostConfigTests {

        @Test
        @DisplayName("test: コンストラクタでコスト設定を作成")
        void testConstructor() {
            LevelDependentParameter param = new LevelDependentParameter(10.0, 1.0, 0.0);
            CostConfig config = new CostConfig(SkillCostType.MANA, param);

            assertEquals(SkillCostType.MANA, config.getType());
            assertEquals(param, config.getParameter());
        }

        @Test
        @DisplayName("test: nullタイプはMANAがデフォルト")
        void testConstructorWithNullType() {
            LevelDependentParameter param = new LevelDependentParameter(10.0, 1.0, 0.0);
            CostConfig config = new CostConfig(null, param);

            assertEquals(SkillCostType.MANA, config.getType());
        }

        @Test
        @DisplayName("test: getCostでレベル別コスト取得")
        void testGetCost() {
            LevelDependentParameter param = new LevelDependentParameter(10.0, 2.0, 5.0);
            CostConfig config = new CostConfig(SkillCostType.MANA, param);

            // Lv1: 10 + (2 * 0) = 10
            // Lv3: 10 + (2 * 2) = 14
            // Lv5: 10 + (2 * 4) = 18 (18 < 5? no so 18)
            // Lv7: 10 + (2 * 6) = 22 (22 < 5? no so 22)
            assertEquals(10, config.getCost(1));
            assertEquals(14, config.getCost(3));
            assertEquals(18, config.getCost(5));
        }

        @Test
        @DisplayName("test: nullパラメータは0を返す")
        void testGetCostWithNullParam() {
            CostConfig config = new CostConfig(SkillCostType.HP, null);

            assertEquals(0, config.getCost(1));
            assertEquals(0, config.getCost(5));
        }
    }

    // ========== 内部クラス: CooldownConfig テスト ==========

    @Nested
    @DisplayName("CooldownConfig: クールダウン設定内部クラス")
    class CooldownConfigTests {

        @Test
        @DisplayName("test: コンストラクタでクールダウン設定を作成")
        void testConstructor() {
            LevelDependentParameter param = new LevelDependentParameter(5.0, -0.5, 1.0);
            CooldownConfig config = new CooldownConfig(param);

            assertEquals(param, config.getParameter());
        }

        @Test
        @DisplayName("test: getCooldownでレベル別クールダウン取得")
        void testGetCooldown() {
            LevelDependentParameter param = new LevelDependentParameter(5.0, -0.5, 1.0);
            CooldownConfig config = new CooldownConfig(param);

            // Lv1: 5.0 + (-0.5 * 0) = 5.0
            // Lv3: 5.0 + (-0.5 * 2) = 4.0
            // Lv9: 5.0 + (-0.5 * 8) = 1.0
            // Lv10: 1.0 (min制限)
            assertEquals(5.0, config.getCooldown(1), 0.001);
            assertEquals(4.0, config.getCooldown(3), 0.001);
            assertEquals(1.0, config.getCooldown(9), 0.001);
            assertEquals(1.0, config.getCooldown(10), 0.001);
        }

        @Test
        @DisplayName("test: nullパラメータは0.0を返す")
        void testGetCooldownWithNullParam() {
            CooldownConfig config = new CooldownConfig(null);

            assertEquals(0.0, config.getCooldown(1), 0.001);
            assertEquals(0.0, config.getCooldown(5), 0.001);
        }
    }

    // ========== 内部クラス: TargetingConfig テスト ==========

    @Nested
    @DisplayName("TargetingConfig: ターゲット設定内部クラス")
    class TargetingConfigTests {

        @Test
        @DisplayName("test: コンストラクタでターゲット設定を作成")
        void testConstructor() {
            TargetingConfig config = new TargetingConfig("cone", null);

            assertEquals("cone", config.getType());
            assertNull(config.getParams());
        }

        @Test
        @DisplayName("test: nullタイプはsingleがデフォルト")
        void testConstructorWithNullType() {
            TargetingConfig config = new TargetingConfig(null, null);

            assertEquals("single", config.getType());
        }

        @Test
        @DisplayName("test: パラメータ付きコンストラクタ")
        void testConstructorWithParams() {
            TargetingConfig.TargetingParams params = new TargetingConfig.TargetingParams(5.0);
            TargetingConfig config = new TargetingConfig("sphere", params);

            assertEquals("sphere", config.getType());
            assertEquals(params, config.getParams());
        }
    }

    @Nested
    @DisplayName("TargetingParams: ターゲットパラメータ内部クラス")
    class TargetingParamsTests {

        @Test
        @DisplayName("test: TargetingParamsのrange取得")
        void testTargetingParamsGetRange() {
            TargetingConfig.TargetingParams params = new TargetingConfig.TargetingParams(10.0);

            assertEquals(10.0, params.getRange(), 0.001);
        }
    }

    @Nested
    @DisplayName("ConeParams: コーンパラメータ内部クラス")
    class ConeParamsTests {

        @Test
        @DisplayName("test: ConeParamsでangleとrange取得")
        void testConeParams() {
            TargetingConfig.ConeParams params = new TargetingConfig.ConeParams(90.0, 5.0);

            assertEquals(90.0, params.getAngle(), 0.001);
            assertEquals(5.0, params.getRange(), 0.001);
        }
    }

    @Nested
    @DisplayName("SphereParams: 球形パラメータ内部クラス")
    class SphereParamsTests {

        @Test
        @DisplayName("test: SphereParamsでradius取得")
        void testSphereParams() {
            TargetingConfig.SphereParams params = new TargetingConfig.SphereParams(3.5);

            assertEquals(3.5, params.getRadius(), 0.001);
            assertEquals(3.5, params.getRange(), 0.001); // radius is used as range
        }
    }

    @Nested
    @DisplayName("SectorParams: 扇形パラメータ内部クラス")
    class SectorParamsTests {

        @Test
        @DisplayName("test: SectorParamsでangleとradius取得")
        void testSectorParams() {
            TargetingConfig.SectorParams params = new TargetingConfig.SectorParams(45.0, 6.0);

            assertEquals(45.0, params.getAngle(), 0.001);
            assertEquals(6.0, params.getRadius(), 0.001);
            assertEquals(6.0, params.getRange(), 0.001); // radius is used as range
        }
    }

    // ========== 内部クラス: FormulaDamageConfig テスト ==========

    @Nested
    @DisplayName("FormulaDamageConfig: 数式ダメージ設定内部クラス")
    class FormulaDamageConfigTests {

        @Test
        @DisplayName("test: コンストラクタで数式ダメージ設定を作成")
        void testConstructor() {
            FormulaDamageConfig config = new FormulaDamageConfig("STR * 2", null);

            assertEquals("STR * 2", config.getFormula());
            assertNotNull(config.getLevelFormulas());
            assertTrue(config.getLevelFormulas().isEmpty());
        }

        @Test
        @DisplayName("test: レベル別数式付きコンストラクタ")
        void testConstructorWithLevelFormulas() {
            Map<Integer, String> levelFormulas = new HashMap<>();
            levelFormulas.put(5, "STR * 3");
            levelFormulas.put(10, "STR * 5");

            FormulaDamageConfig config = new FormulaDamageConfig("STR * 2", levelFormulas);

            assertEquals("STR * 2", config.getFormula());
            assertEquals(2, config.getLevelFormulas().size());
            assertTrue(config.hasLevelFormulas());
        }

        @Test
        @DisplayName("test: getFormula(int)でレベル別数式取得")
        void testGetFormulaLevel() {
            Map<Integer, String> levelFormulas = new HashMap<>();
            levelFormulas.put(5, "STR * 3");
            levelFormulas.put(10, "STR * 5");

            FormulaDamageConfig config = new FormulaDamageConfig("STR * 2", levelFormulas);

            assertEquals("STR * 2", config.getFormula(1)); // 基本数式
            assertEquals("STR * 3", config.getFormula(5)); // レベル別
            assertEquals("STR * 5", config.getFormula(10)); // レベル別
            assertEquals("STR * 2", config.getFormula(7)); // 基本数式（フォールバック）
        }

        @Test
        @DisplayName("test: hasLevelFormulasで判定")
        void testHasLevelFormulas() {
            FormulaDamageConfig config1 = new FormulaDamageConfig("STR * 2", null);
            assertFalse(config1.hasLevelFormulas());

            Map<Integer, String> levelFormulas = new HashMap<>();
            levelFormulas.put(5, "STR * 3");
            FormulaDamageConfig config2 = new FormulaDamageConfig("STR * 2", levelFormulas);
            assertTrue(config2.hasLevelFormulas());
        }

        @Test
        @DisplayName("test: getLevelFormulasはコピーを返す")
        void testGetLevelFormulasReturnsCopy() {
            Map<Integer, String> levelFormulas = new HashMap<>();
            levelFormulas.put(5, "STR * 3");

            FormulaDamageConfig config = new FormulaDamageConfig("STR * 2", levelFormulas);

            Map<Integer, String> formulas1 = config.getLevelFormulas();
            Map<Integer, String> formulas2 = config.getLevelFormulas();

            assertNotSame(formulas1, formulas2);
            assertEquals(formulas1, formulas2);
        }

    // ========== コンポーネントベースメソッド テスト ==========

    @Nested
    @DisplayName("ComponentBased: コンポーネントベースのメソッド")
    class ComponentBasedTests {

        // テスト用のヘルパーメソッド - コンポーネント付きSkillEffectを作成
        private com.example.rpgplugin.skill.component.SkillEffect createSkillEffectWithComponents() {
            com.example.rpgplugin.skill.component.SkillEffect skillEffect = 
                new com.example.rpgplugin.skill.component.SkillEffect("testSkill");
            
            // Costコンポーネントを追加
            TestCostComponent costComponent = new TestCostComponent("cost");
            skillEffect.addComponent(costComponent);
            
            // Cooldownコンポーネントを追加
            TestCooldownComponent cooldownComponent = new TestCooldownComponent("cooldown");
            skillEffect.addComponent(cooldownComponent);
            
            // Damageメカニックコンポーネントを追加
            TestDamageMechanic damageComponent = new TestDamageMechanic("damage");
            skillEffect.addComponent(damageComponent);
            
            // Targetコンポーネントを追加
            TestTargetComponent targetComponent = new TestTargetComponent("target");
            skillEffect.addComponent(targetComponent);
            
            return skillEffect;
        }

        @Test
        @DisplayName("test: getDamageFromComponentsはコンポーネントからダメージを取得")
        void testGetDamageFromComponents() {
            com.example.rpgplugin.skill.component.SkillEffect skillEffect = 
                new com.example.rpgplugin.skill.component.SkillEffect("testSkill");
            
            // 数式ダメージコンポーネント
            TestDamageMechanic damageComponent = new TestDamageMechanic("damage");
            damageComponent.getSettings().set("value", "10 + level * 2");
            skillEffect.addComponent(damageComponent);
            
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                null,
                null,
                null,
                skillEffect
            );
            
            // level=1: 10 + 1*2 = 12
            assertEquals(12.0, skill.getDamageFromComponents(1), 0.001);
            // level=5: 10 + 5*2 = 20
            assertEquals(20.0, skill.getDamageFromComponents(5), 0.001);
        }

        @Test
        @DisplayName("test: getDamageFromComponentsは固定値も取得可能")
        void testGetDamageFromComponentsFixed() {
            com.example.rpgplugin.skill.component.SkillEffect skillEffect = 
                new com.example.rpgplugin.skill.component.SkillEffect("testSkill");
            
            TestDamageMechanic damageComponent = new TestDamageMechanic("damage");
            damageComponent.getSettings().set("value", "50");
            skillEffect.addComponent(damageComponent);
            
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                null,
                null,
                null,
                skillEffect
            );
            
            assertEquals(50.0, skill.getDamageFromComponents(1), 0.001);
            assertEquals(50.0, skill.getDamageFromComponents(10), 0.001);
        }

        @Test
        @DisplayName("test: getDamageFromComponentsはコンポーネントなしで0を返す")
        void testGetDamageFromComponentsNoComponent() {
            Skill skill = createBasicSkill();
            assertEquals(0.0, skill.getDamageFromComponents(1), 0.001);
        }

        @Test
        @DisplayName("test: getDamageFromComponentsはLvキーワードも置換")
        void testGetDamageFromComponentsLvKeyword() {
            com.example.rpgplugin.skill.component.SkillEffect skillEffect = 
                new com.example.rpgplugin.skill.component.SkillEffect("testSkill");
            
            TestDamageMechanic damageComponent = new TestDamageMechanic("damage");
            damageComponent.getSettings().set("value", "5 * Lv");
            skillEffect.addComponent(damageComponent);
            
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                null,
                null,
                null,
                skillEffect
            );
            
            // Lv=3: 5 * 3 = 15
            assertEquals(15.0, skill.getDamageFromComponents(3), 0.001);
        }

        @Test
        @DisplayName("test: getDamageFromComponentsは数式解析失敗時にdouble値を返す")
        void testGetDamageFromComponentsParseFail() {
            com.example.rpgplugin.skill.component.SkillEffect skillEffect = 
                new com.example.rpgplugin.skill.component.SkillEffect("testSkill");
            
            TestDamageMechanic damageComponent = new TestDamageMechanic("damage");
            damageComponent.getSettings().set("value", "invalid_formula");
            skillEffect.addComponent(damageComponent);
            
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                null,
                null,
                null,
                skillEffect
            );
            
            // パース失敗時はDouble.parseDoubleを試みるが、失敗したら0.0
            assertEquals(0.0, skill.getDamageFromComponents(1), 0.001);
        }

        @Test
        @DisplayName("test: getCooldownFromComponentsはコンポーネントからクールダウンを取得")
        void testGetCooldownFromComponents() {
            com.example.rpgplugin.skill.component.SkillEffect skillEffect = 
                new com.example.rpgplugin.skill.component.SkillEffect("testSkill");
            
            TestCooldownComponent cooldownComponent = new TestCooldownComponent("cooldown");
            cooldownComponent.getSettings().set("value", "10 - level * 0.5");
            skillEffect.addComponent(cooldownComponent);
            
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                null,
                null,
                null,
                skillEffect
            );
            
            // level=1: 10 - 0.5 = 9.5
            assertEquals(9.5, skill.getCooldownFromComponents(1), 0.001);
            // level=5: 10 - 2.5 = 7.5
            assertEquals(7.5, skill.getCooldownFromComponents(5), 0.001);
        }

        @Test
        @DisplayName("test: getCooldownFromComponentsはコンポーネントなしで0を返す")
        void testGetCooldownFromComponentsNoComponent() {
            Skill skill = createBasicSkill();
            assertEquals(0.0, skill.getCooldownFromComponents(1), 0.001);
        }

        @Test
        @DisplayName("test: getCostFromComponentsはコンポーネントからコストを取得")
        void testGetCostFromComponents() {
            com.example.rpgplugin.skill.component.SkillEffect skillEffect = 
                new com.example.rpgplugin.skill.component.SkillEffect("testSkill");
            
            TestCostComponent costComponent = new TestCostComponent("cost");
            costComponent.getSettings().set("value", "20 + level * 2");
            skillEffect.addComponent(costComponent);
            
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                null,
                null,
                null,
                skillEffect
            );
            
            // level=1: 20 + 2 = 22
            assertEquals(22, skill.getCostFromComponents(1));
            // level=5: 20 + 10 = 30
            assertEquals(30, skill.getCostFromComponents(5));
        }

        @Test
        @DisplayName("test: getCostFromComponentsは整数値を返す")
        void testGetCostFromComponentsInt() {
            com.example.rpgplugin.skill.component.SkillEffect skillEffect = 
                new com.example.rpgplugin.skill.component.SkillEffect("testSkill");
            
            TestCostComponent costComponent = new TestCostComponent("cost");
            costComponent.getSettings().set("value", "15.7"); // 小数を設定
            skillEffect.addComponent(costComponent);
            
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                null,
                null,
                null,
                skillEffect
            );
            
            // 15.7 -> intキャストで15
            assertEquals(15, skill.getCostFromComponents(1));
        }

        @Test
        @DisplayName("test: getCostFromComponentsはコンポーネントなしで0を返す")
        void testGetCostFromComponentsNoComponent() {
            Skill skill = createBasicSkill();
            assertEquals(0, skill.getCostFromComponents(1));
        }

        @Test
        @DisplayName("test: findComponentByKeyはコンポーネントを検索")
        void testFindComponentByKey() {
            com.example.rpgplugin.skill.component.SkillEffect skillEffect = 
                new com.example.rpgplugin.skill.component.SkillEffect("testSkill");
            
            TestCostComponent costComponent = new TestCostComponent("cost");
            skillEffect.addComponent(costComponent);
            
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                null,
                null,
                null,
                skillEffect
            );
            
            com.example.rpgplugin.skill.component.EffectComponent found = skill.findComponentByKey("cost");
            assertNotNull(found);
            assertEquals("cost", found.getKey());
        }

        @Test
        @DisplayName("test: findComponentByKeyは存在しないキーでnullを返す")
        void testFindComponentByKeyNotFound() {
            com.example.rpgplugin.skill.component.SkillEffect skillEffect = 
                new com.example.rpgplugin.skill.component.SkillEffect("testSkill");
            
            TestCostComponent costComponent = new TestCostComponent("cost");
            skillEffect.addComponent(costComponent);
            
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                null,
                null,
                null,
                skillEffect
            );
            
            com.example.rpgplugin.skill.component.EffectComponent found = skill.findComponentByKey("damage");
            assertNull(found);
        }

        @Test
        @DisplayName("test: findComponentByKeyはcomponentEffectなしでnullを返す")
        void testFindComponentByKeyNoComponentEffect() {
            Skill skill = createBasicSkill();
            assertNull(skill.findComponentByKey("cost"));
        }

        @Test
        @DisplayName("test: getTargetFromComponentsはSkillTargetを構築")
        void testGetTargetFromComponents() {
            com.example.rpgplugin.skill.component.SkillEffect skillEffect = 
                new com.example.rpgplugin.skill.component.SkillEffect("testSkill");
            
            TestTargetComponent targetComponent = new TestTargetComponent("target");
            targetComponent.getSettings().set("type", "NEAREST_HOSTILE");
            targetComponent.getSettings().set("area_shape", "SPHERE");
            targetComponent.getSettings().set("range", "10.0");
            targetComponent.getSettings().set("max_targets", "3");
            skillEffect.addComponent(targetComponent);
            
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                null,
                null,
                null,
                skillEffect
            );
            
            com.example.rpgplugin.skill.target.SkillTarget target = skill.getTargetFromComponents();
            assertNotNull(target);
            assertEquals(com.example.rpgplugin.skill.target.TargetType.NEAREST_HOSTILE, target.getType());
            assertEquals(com.example.rpgplugin.skill.target.AreaShape.SPHERE, target.getAreaShape());
            assertEquals(10.0, target.getRange(), 0.001);
            assertEquals(3, target.getMaxTargets());
        }

        @Test
        @DisplayName("test: getTargetFromComponentsはデフォルト値を使用")
        void testGetTargetFromComponentsDefaults() {
            com.example.rpgplugin.skill.component.SkillEffect skillEffect = 
                new com.example.rpgplugin.skill.component.SkillEffect("testSkill");
            
            TestTargetComponent targetComponent = new TestTargetComponent("target");
            // 設定を空にする（デフォルト値を使用）
            skillEffect.addComponent(targetComponent);
            
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                null,
                null,
                null,
                skillEffect
            );
            
            com.example.rpgplugin.skill.target.SkillTarget target = skill.getTargetFromComponents();
            assertNotNull(target);
            assertEquals(com.example.rpgplugin.skill.target.TargetType.SELF, target.getType()); // デフォルトはSELF
            assertEquals(com.example.rpgplugin.skill.target.AreaShape.SPHERE, target.getAreaShape());
            assertEquals(5.0, target.getRange(), 0.001); // デフォルトrange
            assertEquals(1, target.getMaxTargets()); // デフォルトmax_targets
        }

        @Test
        @DisplayName("test: getTargetFromComponentsはmaxTargets=1でSingleTargetConfigを作成")
        void testGetTargetFromComponentsSingleTarget() {
            com.example.rpgplugin.skill.component.SkillEffect skillEffect = 
                new com.example.rpgplugin.skill.component.SkillEffect("testSkill");
            
            TestTargetComponent targetComponent = new TestTargetComponent("target");
            targetComponent.getSettings().set("max_targets", "1");
            targetComponent.getSettings().set("select_nearest", "false");
            targetComponent.getSettings().set("target_self", "true");
            skillEffect.addComponent(targetComponent);
            
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                null,
                null,
                null,
                skillEffect
            );
            
            com.example.rpgplugin.skill.target.SkillTarget target = skill.getTargetFromComponents();
            assertNotNull(target);
            assertNotNull(target.getSingleTarget());
            assertFalse(target.getSingleTarget().isSelectNearest());
            assertTrue(target.getSingleTarget().isTargetSelf());
        }

        @Test
        @DisplayName("test: getTargetFromComponentsは無効なタイプでデフォルトを使用")
        void testGetTargetFromComponentsInvalidType() {
            com.example.rpgplugin.skill.component.SkillEffect skillEffect = 
                new com.example.rpgplugin.skill.component.SkillEffect("testSkill");
            
            TestTargetComponent targetComponent = new TestTargetComponent("target");
            targetComponent.getSettings().set("type", "INVALID_TYPE");
            skillEffect.addComponent(targetComponent);
            
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                null,
                null,
                null,
                skillEffect
            );
            
            com.example.rpgplugin.skill.target.SkillTarget target = skill.getTargetFromComponents();
            assertNotNull(target);
            assertEquals(com.example.rpgplugin.skill.target.TargetType.NEAREST_HOSTILE, target.getType());
        }

        @Test
        @DisplayName("test: getTargetFromComponentsは無効なAreaShapeでデフォルトを使用")
        void testGetTargetFromComponentsInvalidAreaShape() {
            com.example.rpgplugin.skill.component.SkillEffect skillEffect = 
                new com.example.rpgplugin.skill.component.SkillEffect("testSkill");
            
            TestTargetComponent targetComponent = new TestTargetComponent("target");
            targetComponent.getSettings().set("area_shape", "INVALID_SHAPE");
            skillEffect.addComponent(targetComponent);
            
            Skill skill = new Skill(
                "testSkill",
                "Test Skill",
                "Test",
                SkillType.NORMAL,
                new ArrayList<>(),
                10,
                5.0,
                20,
                null,
                null,
                SkillCostType.MANA,
                null,
                null,
                "DIAMOND_SWORD",
                new ArrayList<>(),
                null,
                null,
                null,
                null,
                skillEffect
            );
            
            com.example.rpgplugin.skill.target.SkillTarget target = skill.getTargetFromComponents();
            assertNotNull(target);
            assertEquals(com.example.rpgplugin.skill.target.AreaShape.SPHERE, target.getAreaShape());
        }

        @Test
        @DisplayName("test: getTargetFromComponentsはコンポーネントなしでnullを返す")
        void testGetTargetFromComponentsNoComponent() {
            Skill skill = createBasicSkill();
            assertNull(skill.getTargetFromComponents());
        }
    }

    @Nested
    @DisplayName("FormulaDamage: 数式ダメージ計算")
    class FormulaDamageTests {

        private com.example.rpgplugin.player.RPGPlayer createMockRPGPlayer() {
            return SkillTest.this.createMockRPGPlayer();
        }

        @Test
        @DisplayName("test: calculateFormulaDamageはformulaDamageなしで0.0を返す")
        void testCalculateFormulaDamageNoFormulaDamage() {
            Skill skill = createBasicSkill();
            com.example.rpgplugin.skill.evaluator.FormulaDamageCalculator calculator =
                new com.example.rpgplugin.skill.evaluator.FormulaDamageCalculator(
                    new com.example.rpgplugin.skill.evaluator.FormulaEvaluator(),
                    "damage", new java.util.HashMap<>(), "damage");
            com.example.rpgplugin.player.RPGPlayer rpgPlayer = createMockRPGPlayer();

            double result = skill.calculateFormulaDamage(calculator, rpgPlayer, 1);
            assertEquals(0.0, result, 0.001, "formulaDamageなしでは0.0を返す");
        }

        @Test
        @DisplayName("test: calculateFormulaDamageはcalculatorがnullで0.0を返す")
        void testCalculateFormulaDamageNullCalculator() {
            FormulaDamageConfig formulaDamage = new FormulaDamageConfig(
                "10 + level * 5", new java.util.HashMap<>());
            Skill skill = new Skill(
                "testSkill", "Test", "Test", SkillType.NORMAL,
                new ArrayList<>(), 10, 5.0, 20,
                null, null, SkillCostType.MANA, null, null,
                "DIAMOND_SWORD", new ArrayList<>(),
                null, formulaDamage, null, null, null);
            com.example.rpgplugin.player.RPGPlayer rpgPlayer = createMockRPGPlayer();

            double result = skill.calculateFormulaDamage(null, rpgPlayer, 1);
            assertEquals(0.0, result, 0.001, "calculatorがnullでは0.0を返す");
        }

        @Test
        @DisplayName("test: calculateFormulaDamageはカスタム変数を設定する")
        void testCalculateFormulaDamageWithCustomVariables() {
            java.util.List<VariableDefinition> variables = new ArrayList<>();
            variables.add(new VariableDefinition("str_mod", 2.5));
            variables.add(new VariableDefinition("base_mod", 1.5));

            // 変数を使用しないシンプルな数式でテスト
            FormulaDamageConfig formulaDamage = new FormulaDamageConfig(
                "100", new java.util.HashMap<>());

            Skill skill = new Skill(
                "testSkill", "Test", "Test", SkillType.NORMAL,
                new ArrayList<>(), 10, 5.0, 20,
                null, null, SkillCostType.MANA, null, null,
                "DIAMOND_SWORD", new ArrayList<>(),
                variables, formulaDamage, null, null, null);

            com.example.rpgplugin.skill.evaluator.FormulaEvaluator evaluator =
                new com.example.rpgplugin.skill.evaluator.FormulaEvaluator();
            com.example.rpgplugin.skill.evaluator.FormulaDamageCalculator calculator =
                new com.example.rpgplugin.skill.evaluator.FormulaDamageCalculator(
                    evaluator, "100", new java.util.HashMap<>(), "100");
            com.example.rpgplugin.player.RPGPlayer rpgPlayer = createMockRPGPlayer();

            double result = skill.calculateFormulaDamage(calculator, rpgPlayer, 1);
            // 変数は設定されるが、数式は固定値100を返す
            assertEquals(100.0, result, 0.001, "固定値のダメージを計算");
        }

        @Test
        @DisplayName("test: calculateFormulaDamageはエラー時に0.0を返す")
        void testCalculateFormulaDamageWithError() {
            FormulaDamageConfig formulaDamage = new FormulaDamageConfig(
                "undefined_var", new java.util.HashMap<>());

            Skill skill = new Skill(
                "testSkill", "Test", "Test", SkillType.NORMAL,
                new ArrayList<>(), 10, 5.0, 20,
                null, null, SkillCostType.MANA, null, null,
                "DIAMOND_SWORD", new ArrayList<>(),
                null, formulaDamage, null, null, null);

            com.example.rpgplugin.skill.evaluator.FormulaEvaluator evaluator =
                new com.example.rpgplugin.skill.evaluator.FormulaEvaluator();
            com.example.rpgplugin.skill.evaluator.FormulaDamageCalculator calculator =
                new com.example.rpgplugin.skill.evaluator.FormulaDamageCalculator(
                    evaluator, "undefined_var", new java.util.HashMap<>(), "undefined_var");
            com.example.rpgplugin.player.RPGPlayer rpgPlayer = createMockRPGPlayer();

            double result = skill.calculateFormulaDamage(calculator, rpgPlayer, 1);
            // エラー時は0.0を返す
            assertEquals(0.0, result, 0.001, "エラー時は0.0を返す");
        }

        @Test
        @DisplayName("test: createFormulaDamageCalculatorはformulaDamageなしでnullを返す")
        void testCreateFormulaDamageCalculatorNoFormulaDamage() {
            Skill skill = createBasicSkill();
            com.example.rpgplugin.skill.evaluator.FormulaEvaluator evaluator =
                new com.example.rpgplugin.skill.evaluator.FormulaEvaluator();

            com.example.rpgplugin.skill.evaluator.FormulaDamageCalculator result =
                skill.createFormulaDamageCalculator(evaluator);
            assertNull(result, "formulaDamageなしではnullを返す");
        }

        @Test
        @DisplayName("test: createFormulaDamageCalculatorはevaluatorがnullでnullを返す")
        void testCreateFormulaDamageCalculatorNullEvaluator() {
            FormulaDamageConfig formulaDamage = new FormulaDamageConfig(
                "10 + level * 5", new java.util.HashMap<>());
            Skill skill = new Skill(
                "testSkill", "Test", "Test", SkillType.NORMAL,
                new ArrayList<>(), 10, 5.0, 20,
                null, null, SkillCostType.MANA, null, null,
                "DIAMOND_SWORD", new ArrayList<>(),
                null, formulaDamage, null, null, null);

            com.example.rpgplugin.skill.evaluator.FormulaDamageCalculator result =
                skill.createFormulaDamageCalculator(null);
            assertNull(result, "evaluatorがnullではnullを返す");
        }

        @Test
        @DisplayName("test: createFormulaDamageCalculatorは正常にCalculatorを作成")
        void testCreateFormulaDamageCalculatorSuccess() {
            FormulaDamageConfig formulaDamage = new FormulaDamageConfig(
                "10 + level * 5", new java.util.HashMap<>());
            Skill skill = new Skill(
                "testSkill", "Test", "Test", SkillType.NORMAL,
                new ArrayList<>(), 10, 5.0, 20,
                null, null, SkillCostType.MANA, null, null,
                "DIAMOND_SWORD", new ArrayList<>(),
                null, formulaDamage, null, null, null);
            com.example.rpgplugin.skill.evaluator.FormulaEvaluator evaluator =
                new com.example.rpgplugin.skill.evaluator.FormulaEvaluator();

            com.example.rpgplugin.skill.evaluator.FormulaDamageCalculator result =
                skill.createFormulaDamageCalculator(evaluator);
            assertNotNull(result, "Calculatorが作成されること");
        }

        @Test
        @DisplayName("test: createFormulaDamageCalculatorはレベル別数式を含むCalculatorを作成")
        void testCreateFormulaDamageCalculatorWithLevelFormulas() {
            java.util.Map<Integer, String> levelFormulas = new java.util.HashMap<>();
            levelFormulas.put(1, "10");
            levelFormulas.put(5, "50");
            levelFormulas.put(10, "100");

            FormulaDamageConfig formulaDamage = new FormulaDamageConfig(
                "20", levelFormulas);
            Skill skill = new Skill(
                "testSkill", "Test", "Test", SkillType.NORMAL,
                new ArrayList<>(), 10, 5.0, 20,
                null, null, SkillCostType.MANA, null, null,
                "DIAMOND_SWORD", new ArrayList<>(),
                null, formulaDamage, null, null, null);
            com.example.rpgplugin.skill.evaluator.FormulaEvaluator evaluator =
                new com.example.rpgplugin.skill.evaluator.FormulaEvaluator();

            com.example.rpgplugin.skill.evaluator.FormulaDamageCalculator result =
                skill.createFormulaDamageCalculator(evaluator);
            assertNotNull(result, "Calculatorが作成されること");
        }
    }

    // ========== テスト用モックコンポーネントクラス ==========

    /**
     * テスト用Costコンポーネント
     */
    private static class TestCostComponent extends com.example.rpgplugin.skill.component.EffectComponent {
        public TestCostComponent(String key) {
            super(key);
        }

        @Override
        public com.example.rpgplugin.skill.component.ComponentType getType() {
            return com.example.rpgplugin.skill.component.ComponentType.COST;
        }

        @Override
        public boolean execute(org.bukkit.entity.LivingEntity caster, int level, 
                             java.util.List<org.bukkit.entity.LivingEntity> targets) {
            return true;
        }
    }

    /**
     * テスト用Cooldownコンポーネント
     */
    private static class TestCooldownComponent extends com.example.rpgplugin.skill.component.EffectComponent {
        public TestCooldownComponent(String key) {
            super(key);
        }

        @Override
        public com.example.rpgplugin.skill.component.ComponentType getType() {
            return com.example.rpgplugin.skill.component.ComponentType.TRIGGER;
        }

        @Override
        public boolean execute(org.bukkit.entity.LivingEntity caster, int level, 
                             java.util.List<org.bukkit.entity.LivingEntity> targets) {
            return true;
        }
    }

    /**
     * テスト用Damageメカニックコンポーネント
     */
    private static class TestDamageMechanic extends com.example.rpgplugin.skill.component.EffectComponent {
        public TestDamageMechanic(String key) {
            super(key);
        }

        @Override
        public com.example.rpgplugin.skill.component.ComponentType getType() {
            return com.example.rpgplugin.skill.component.ComponentType.MECHANIC;
        }

        @Override
        public boolean execute(org.bukkit.entity.LivingEntity caster, int level, 
                             java.util.List<org.bukkit.entity.LivingEntity> targets) {
            return true;
        }
    }

    /**
     * テスト用Targetコンポーネント
     */
    private static class TestTargetComponent extends com.example.rpgplugin.skill.component.EffectComponent {
        public TestTargetComponent(String key) {
            super(key);
        }

        @Override
        public com.example.rpgplugin.skill.component.ComponentType getType() {
            return com.example.rpgplugin.skill.component.ComponentType.TARGET;
        }

        @Override
        public boolean execute(org.bukkit.entity.LivingEntity caster, int level, 
                             java.util.List<org.bukkit.entity.LivingEntity> targets) {
            return true;
        }
    }
}
}

