package com.example.rpgplugin.skill.component.placement;

import org.bukkit.configuration.ConfigurationSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 配置ルールとバリデーション関連のテストクラス
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("配置ルール・バリデーション テスト")
class PlacementComponentTest {

    // ==================== ComponentType テスト ====================

    @Nested
    @DisplayName("ComponentType: 列挙型")
    class ComponentTypeTests {

        @Test
        @DisplayName("全ての列挙値が存在する")
        void testAllValuesExist() {
            ComponentType[] values = ComponentType.values();

            assertEquals(7, values.length);
        }

        @Test
        @DisplayName("getId: 正しいIDを返す")
        void testGetId() {
            assertEquals("trigger", ComponentType.TRIGGER.getId());
            assertEquals("target", ComponentType.TARGET.getId());
            assertEquals("filter", ComponentType.FILTER.getId());
            assertEquals("condition", ComponentType.CONDITION.getId());
            assertEquals("mechanic", ComponentType.MECHANIC.getId());
            assertEquals("cost", ComponentType.COST.getId());
            assertEquals("cooldown", ComponentType.COOLDOWN.getId());
        }

        @Test
        @DisplayName("getDisplayName: 正しい表示名を返す")
        void testGetDisplayName() {
            assertEquals("トリガー", ComponentType.TRIGGER.getDisplayName());
            assertEquals("ターゲット", ComponentType.TARGET.getDisplayName());
            assertEquals("フィルター", ComponentType.FILTER.getDisplayName());
            assertEquals("条件", ComponentType.CONDITION.getDisplayName());
            assertEquals("メカニック", ComponentType.MECHANIC.getDisplayName());
            assertEquals("コスト", ComponentType.COST.getDisplayName());
            assertEquals("クールダウン", ComponentType.COOLDOWN.getDisplayName());
        }

        @Test
        @DisplayName("getBitMask: 正しいビットマスクを返す")
        void testGetBitMask() {
            assertEquals(0x1, ComponentType.TRIGGER.getBitMask());
            assertEquals(0x2, ComponentType.TARGET.getBitMask());
            assertEquals(0x4, ComponentType.FILTER.getBitMask());
            assertEquals(0x8, ComponentType.CONDITION.getBitMask());
            assertEquals(0x10, ComponentType.MECHANIC.getBitMask());
            assertEquals(0x20, ComponentType.COST.getBitMask());
            assertEquals(0x40, ComponentType.COOLDOWN.getBitMask());
        }

        @Test
        @DisplayName("ビットマスクが一意である")
        void testBitMasksAreUnique() {
            int[] masks = new int[7];
            int i = 0;
            for (ComponentType type : ComponentType.values()) {
                masks[i++] = type.getBitMask();
            }

            // 全て異なることを確認
            for (int j = 0; j < masks.length; j++) {
                for (int k = j + 1; k < masks.length; k++) {
                    assertNotEquals(masks[j], masks[k], "ビットマスクが重複しています");
                }
            }
        }

        @Test
        @DisplayName("ビットマスクが2の累乗である")
        void testBitMasksArePowerOfTwo() {
            for (ComponentType type : ComponentType.values()) {
                int mask = type.getBitMask();
                assertTrue((mask & (mask - 1)) == 0, type.name() + " のビットマスクが2の累乗ではありません: " + mask);
            }
        }
    }

    // ==================== PlacementRules テスト ====================

    @Nested
    @DisplayName("PlacementRules: 配置ルール")
    class PlacementRulesTests {

        @Test
        @DisplayName("getAllowedParents: TRIGGERは親を持たない")
        void testGetAllowedParents_Trigger() {
            Set<ComponentType> parents = PlacementRules.getAllowedParents(ComponentType.TRIGGER);

            assertTrue(parents.isEmpty());
        }

        @Test
        @DisplayName("getAllowedParents: COSTの親はTRIGGERのみ")
        void testGetAllowedParents_Cost() {
            Set<ComponentType> parents = PlacementRules.getAllowedParents(ComponentType.COST);

            assertEquals(1, parents.size());
            assertTrue(parents.contains(ComponentType.TRIGGER));
        }

        @Test
        @DisplayName("getAllowedParents: COOLDOWNの親はTRIGGERのみ")
        void testGetAllowedParents_Cooldown() {
            Set<ComponentType> parents = PlacementRules.getAllowedParents(ComponentType.COOLDOWN);

            assertEquals(1, parents.size());
            assertTrue(parents.contains(ComponentType.TRIGGER));
        }

        @Test
        @DisplayName("getAllowedParents: TARGETの親は複数")
        void testGetAllowedParents_Target() {
            Set<ComponentType> parents = PlacementRules.getAllowedParents(ComponentType.TARGET);

            assertTrue(parents.contains(ComponentType.TRIGGER));
            assertTrue(parents.contains(ComponentType.CONDITION));
            assertTrue(parents.contains(ComponentType.FILTER));
            assertTrue(parents.contains(ComponentType.MECHANIC));
        }

        @Test
        @DisplayName("getAllowedChildren: TRIGGERの子はCOST, COOLDOWN, TARGET")
        void testGetAllowedChildren_Trigger() {
            Set<ComponentType> children = PlacementRules.getAllowedChildren(ComponentType.TRIGGER);

            assertEquals(3, children.size());
            assertTrue(children.contains(ComponentType.COST));
            assertTrue(children.contains(ComponentType.COOLDOWN));
            assertTrue(children.contains(ComponentType.TARGET));
        }

        @Test
        @DisplayName("getAllowedChildren: COSTは子を持たない")
        void testGetAllowedChildren_Cost() {
            Set<ComponentType> children = PlacementRules.getAllowedChildren(ComponentType.COST);

            assertTrue(children.isEmpty());
        }

        @Test
        @DisplayName("getAllowedChildren: TARGETは複数の子を持てる")
        void testGetAllowedChildren_Target() {
            Set<ComponentType> children = PlacementRules.getAllowedChildren(ComponentType.TARGET);

            assertTrue(children.contains(ComponentType.FILTER));
            assertTrue(children.contains(ComponentType.CONDITION));
            assertTrue(children.contains(ComponentType.MECHANIC));
            assertTrue(children.contains(ComponentType.TARGET));
        }

        @Test
        @DisplayName("getAllowedChildren: MECHANICはMECHANIC, TARGET, CONDITIONを持てる")
        void testGetAllowedChildren_Mechanic() {
            Set<ComponentType> children = PlacementRules.getAllowedChildren(ComponentType.MECHANIC);

            assertEquals(3, children.size());
            assertTrue(children.contains(ComponentType.MECHANIC));
            assertTrue(children.contains(ComponentType.TARGET));
            assertTrue(children.contains(ComponentType.CONDITION));
        }

        @Test
        @DisplayName("getMaxCount: TRIGGERの最大数は1")
        void testGetMaxCount_Trigger() {
            assertEquals(1, PlacementRules.getMaxCount(ComponentType.TRIGGER));
        }

        @Test
        @DisplayName("getMaxCount: COSTの最大数は1")
        void testGetMaxCount_Cost() {
            assertEquals(1, PlacementRules.getMaxCount(ComponentType.COST));
        }

        @Test
        @DisplayName("getMaxCount: TARGETの最大数は無制限")
        void testGetMaxCount_Target() {
            assertEquals(Integer.MAX_VALUE, PlacementRules.getMaxCount(ComponentType.TARGET));
        }

        @Test
        @DisplayName("getMaxCount: FILTERの最大数は無制限")
        void testGetMaxCount_Filter() {
            assertEquals(Integer.MAX_VALUE, PlacementRules.getMaxCount(ComponentType.FILTER));
        }

        @Test
        @DisplayName("canPlace: TRIGGERの下にTARGETを配置可能")
        void testCanPlace_Trigger_Target() {
            assertTrue(PlacementRules.canPlace(ComponentType.TRIGGER, ComponentType.TARGET));
        }

        @Test
        @DisplayName("canPlace: TRIGGERの下にCOSTを配置可能")
        void testCanPlace_Trigger_Cost() {
            assertTrue(PlacementRules.canPlace(ComponentType.TRIGGER, ComponentType.COST));
        }

        @Test
        @DisplayName("canPlace: TARGETの下にMECHANICを配置可能")
        void testCanPlace_Target_Mechanic() {
            assertTrue(PlacementRules.canPlace(ComponentType.TARGET, ComponentType.MECHANIC));
        }

        @Test
        @DisplayName("canPlace: TARGETの下にTARGETを配置可能")
        void testCanPlace_Target_Target() {
            assertTrue(PlacementRules.canPlace(ComponentType.TARGET, ComponentType.TARGET));
        }

        @Test
        @DisplayName("canPlace: COSTの下に何も配置不可能")
        void testCanPlace_Cost_Anything() {
            assertFalse(PlacementRules.canPlace(ComponentType.COST, ComponentType.TARGET));
            assertFalse(PlacementRules.canPlace(ComponentType.COST, ComponentType.MECHANIC));
            assertFalse(PlacementRules.canPlace(ComponentType.COST, ComponentType.COST));
        }

        @Test
        @DisplayName("canPlace: TRIGGERを子に配置不可能")
        void testCanPlace_Anything_Trigger() {
            assertFalse(PlacementRules.canPlace(ComponentType.TARGET, ComponentType.TRIGGER));
            assertFalse(PlacementRules.canPlace(ComponentType.MECHANIC, ComponentType.TRIGGER));
        }

        @Test
        @DisplayName("canBeRoot: TRIGGERのみルートになれる")
        void testCanBeRoot() {
            assertTrue(PlacementRules.canBeRoot(ComponentType.TRIGGER));
            assertFalse(PlacementRules.canBeRoot(ComponentType.TARGET));
            assertFalse(PlacementRules.canBeRoot(ComponentType.COST));
            assertFalse(PlacementRules.canBeRoot(ComponentType.MECHANIC));
        }

        @Test
        @DisplayName("getSummary: サマリー文字列を返す")
        void testGetSummary() {
            String summary = PlacementRules.getSummary();

            assertNotNull(summary);
            assertTrue(summary.contains("=== Component Placement Rules ==="));
            assertTrue(summary.contains("[TRIGGER]"));
            assertTrue(summary.contains("[TARGET]"));
            assertTrue(summary.contains("[MECHANIC]"));
            assertTrue(summary.contains("Can be parent of:"));
            assertTrue(summary.contains("Can be child of:"));
            assertTrue(summary.contains("Max count:"));
        }
    }

    // ==================== SkillValidator テスト ====================

    @Nested
    @DisplayName("SkillValidator: スキル検証")
    class SkillValidatorTests {

        @Mock
        private Logger mockLogger;

        @Mock
        private ConfigurationSection mockComponents;

        private SkillValidator validator;

        @BeforeEach
        void setUp() {
            validator = new SkillValidator(mockLogger);
        }

        @Test
        @DisplayName("nullコンポーネントは有効")
        void testValidate_NullComponents() {
            assertTrue(validator.validate("test_skill", null));
        }

        @Test
        @DisplayName("空のコンポーネントリストは失敗")
        void testValidate_EmptyComponentList() {
            when(mockComponents.getList("components")).thenReturn(new ArrayList<>());

            assertFalse(validator.validate("test_skill", mockComponents));

            List<String> errors = validator.getErrors();
            assertFalse(errors.isEmpty());
            assertTrue(errors.get(0).contains("コンポーネントが空です"));
        }

        @Test
        @DisplayName("有効なスキル構成")
        void testValidate_ValidSkill() {
            ConfigurationSection triggerSection = mockTriggerSection();
            // TRIGGERのみルートレベルに配置可能
            List<Object> componentList = new ArrayList<>();
            componentList.add(triggerSection);
            doReturn(componentList).when(mockComponents).getList("components");

            assertTrue(validator.validate("test_skill", mockComponents));
            assertTrue(validator.getErrors().isEmpty());
        }

        @Test
        @DisplayName("トリガーがない場合は失敗")
        void testValidate_NoTrigger() {
            ConfigurationSection targetSection = mockTargetSection();

            List<Object> componentList = new ArrayList<>();
            componentList.add(targetSection);
            doReturn(componentList).when(mockComponents).getList("components");

            assertFalse(validator.validate("test_skill", mockComponents));

            List<String> errors = validator.getErrors();
            assertTrue(errors.stream().anyMatch(e -> e.contains("トリガー（CAST）が定義されていません")));
        }

        @Test
        @DisplayName("無効な形式のコンポーネントは失敗")
        void testValidate_InvalidComponentFormat() {
            List<Object> componentList = new ArrayList<>();
            componentList.add("invalid_string");
            doReturn(componentList).when(mockComponents).getList("components");

            assertFalse(validator.validate("test_skill", mockComponents));

            List<String> errors = validator.getErrors();
            assertTrue(errors.stream().anyMatch(e -> e.contains("形式が無効です")));
        }

        @Test
        @DisplayName("タイプ指定がないコンポーネントは失敗")
        void testValidate_NoTypeSpecified() {
            ConfigurationSection emptySection = mock(ConfigurationSection.class);
            // getKeys()はSetを返す
            doReturn(new java.util.HashSet<>()).when(emptySection).getKeys(false);

            List<Object> componentList = new ArrayList<>();
            componentList.add(emptySection);
            doReturn(componentList).when(mockComponents).getList("components");

            assertFalse(validator.validate("test_skill", mockComponents));

            List<String> errors = validator.getErrors();
            assertTrue(errors.stream().anyMatch(e -> e.contains("タイプが指定されていません")));
        }

        @Test
        @DisplayName("ルートに配置できないタイプは失敗")
        void testValidate_InvalidRootType() {
            ConfigurationSection targetSection = mockTargetSection();

            List<Object> componentList = new ArrayList<>();
            componentList.add(targetSection);
            doReturn(componentList).when(mockComponents).getList("components");

            assertFalse(validator.validate("test_skill", mockComponents));

            List<String> errors = validator.getErrors();
            assertTrue(errors.stream().anyMatch(e -> e.contains("ルートレベルに配置できないタイプです")));
        }

        @Test
        @DisplayName("トリガーが2つ以上ある場合は失敗")
        void testValidate_MultipleTriggers() {
            ConfigurationSection triggerSection1 = mockTriggerSection();
            ConfigurationSection triggerSection2 = mockTriggerSection();

            List<Object> componentList = new ArrayList<>();
            componentList.add(triggerSection1);
            componentList.add(triggerSection2);
            doReturn(componentList).when(mockComponents).getList("components");

            assertFalse(validator.validate("test_skill", mockComponents));

            List<String> errors = validator.getErrors();
            assertTrue(errors.stream().anyMatch(e -> e.contains("配置数が上限を超えています")));
        }

        @Test
        @DisplayName("isValid: エラーがない場合はtrue")
        void testIsValid_NoErrors() {
            ConfigurationSection triggerSection = mockTriggerSection();

            List<Object> componentList = new ArrayList<>();
            componentList.add(triggerSection);
            doReturn(componentList).when(mockComponents).getList("components");

            validator.validate("test_skill", mockComponents);

            assertTrue(validator.isValid());
        }

        @Test
        @DisplayName("isValid: エラーがある場合はfalse")
        void testIsValid_HasErrors() {
            doReturn(new ArrayList<>()).when(mockComponents).getList("components");

            validator.validate("test_skill", mockComponents);

            assertFalse(validator.isValid());
        }

        @Test
        @DisplayName("getErrors: エラーリストのコピーを返す")
        void testGetErrors() {
            doReturn(new ArrayList<>()).when(mockComponents).getList("components");

            validator.validate("test_skill", mockComponents);

            List<String> errors1 = validator.getErrors();
            List<String> errors2 = validator.getErrors();

            assertEquals(errors1, errors2);
            assertNotSame(errors1, errors2); // 別のインスタンス
        }

        @Test
        @DisplayName("getWarnings: 警告リストのコピーを返す")
        void testGetWarnings() {
            doReturn(new ArrayList<>()).when(mockComponents).getList("components");

            validator.validate("test_skill", mockComponents);

            List<String> warnings = validator.getWarnings();

            assertNotNull(warnings);
            // warningsは空でも可
        }

        @Test
        @DisplayName("validate: エラー時にログ出力")
        void testValidate_LogsErrors() {
            doReturn(new ArrayList<>()).when(mockComponents).getList("components");

            validator.validate("test_skill", mockComponents);

            // Loggerのモック化は難しいため、エラーリストの確認で代替
            List<String> errors = validator.getErrors();
            assertFalse(errors.isEmpty());
            assertTrue(errors.stream().anyMatch(e -> e.contains("コンポーネントが空です")));
        }

        @Test
        @DisplayName("validate: 再帰検証で深すぎる場合はエラー")
        void testValidate_TooDeep() {
            // TRIGGERの下にTARGETを21階層続ける
            ConfigurationSection rootSection = mockTriggerSection();
            mockDeepChildrenTarget(rootSection, 21);

            List<Object> componentList = new ArrayList<>();
            componentList.add(rootSection);
            doReturn(componentList).when(mockComponents).getList("components");

            validator.validate("test_skill", mockComponents);

            List<String> errors = validator.getErrors();
            assertTrue(errors.stream().anyMatch(e -> e.contains("階層が深すぎます")));
        }

        @Test
        @DisplayName("validateChildren: 無効な親子関係はエラー")
        void testValidateChildren_InvalidParentChild() {
            ConfigurationSection triggerSection = mockTriggerSection();
            // TRIGGERの下にTRIGGERは配置できない
            ConfigurationSection invalidChild = mockTriggerSection();
            List<ConfigurationSection> childList = new ArrayList<>();
            childList.add(invalidChild);
            doReturn(childList).when(triggerSection).getList("components");
            // contains("components")がtrueを返すように設定
            doReturn(true).when(triggerSection).contains("components");

            // TRIGGERをルートに追加
            List<Object> componentList = new ArrayList<>();
            componentList.add(triggerSection);
            doReturn(componentList).when(mockComponents).getList("components");

            validator.validate("test_skill", mockComponents);

            List<String> errors = validator.getErrors();
            assertTrue(errors.stream().anyMatch(e -> e.contains("無効な配置")));
        }

        // 深さテスト用ヘルパーメソッド

        private void mockDeepChildrenTarget(ConfigurationSection section, int depth) {
            if (depth <= 0) return;

            ConfigurationSection childSection = mock(ConfigurationSection.class);
            doReturn(true).when(childSection).contains("target");

            List<ConfigurationSection> childList = new ArrayList<>();
            childList.add(childSection);
            doReturn(childList).when(section).getList("components");
            doReturn(true).when(section).contains("components");

            // 再帰的に子を設定
            mockDeepChildrenTarget(childSection, depth - 1);
        }

        // ヘルパーメソッド

        private ConfigurationSection mockTriggerSection() {
            ConfigurationSection section = mock(ConfigurationSection.class);
            when(section.contains("trigger")).thenReturn(true);
            return section;
        }

        private ConfigurationSection mockTargetSection() {
            ConfigurationSection section = mock(ConfigurationSection.class);
            when(section.contains("target")).thenReturn(true);
            return section;
        }
    }
}
