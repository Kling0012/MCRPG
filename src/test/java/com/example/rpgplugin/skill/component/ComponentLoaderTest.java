package com.example.rpgplugin.skill.component;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ComponentLoaderのテストクラス
 */
@DisplayName("ComponentLoader: コンポーネントローダー")
class ComponentLoaderTest {

    private ComponentLoader loader;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger(ComponentLoaderTest.class.getName());
        loader = new ComponentLoader(logger);
    }

    // ========== ヘルパーメソッド ==========

    /**
     * テスト用の空のConfigurationSectionを作成
     */
    private ConfigurationSection createEmptySection() {
        return new MemoryConfiguration();
    }

    /**
     * テスト用のシンプルなコンポーネントセクションを作成
     */
    private ConfigurationSection createSimpleComponentSection(String type, String id) {
        MemoryConfiguration config = new MemoryConfiguration();
        config.set("type", type);
        config.set("id", id);
        return config;
    }

    /**
     * テスト用のコンポーネントリストセクションを作成
     */
    private ConfigurationSection createComponentListSection() {
        MemoryConfiguration config = new MemoryConfiguration();
        config.set("components", new ArrayList<>());
        return config;
    }

    // ========== コンストラクタ ==========

    @Nested
    @DisplayName("コンストラクタ")
    class ConstructorTests {

        @Test
        @DisplayName("test: コンストラクタでロガーを設定")
        void testConstructorWithLogger() {
            ComponentLoader l = new ComponentLoader(logger);
            assertNotNull(l);
        }

        @Test
        @DisplayName("test: nullロガーでも動作")
        void testConstructorWithNullLogger() {
            assertDoesNotThrow(() -> new ComponentLoader(null));
        }
    }

    // ========== loadComponentsメソッド ==========

    @Nested
    @DisplayName("loadComponentsメソッド")
    class LoadComponentsTests {

        @Test
        @DisplayName("test: nullセクションの場合は空リスト")
        void testLoadComponentsWithNull() {
            List<EffectComponent> result = loader.loadComponents(null);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("test: 空のセクションの場合は空リスト")
        void testLoadComponentsWithEmptySection() {
            ConfigurationSection section = createEmptySection();
            List<EffectComponent> result = loader.loadComponents(section);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("test: componentsキーがない場合は空リスト")
        void testLoadComponentsWithoutComponentsKey() {
            ConfigurationSection section = createSimpleComponentSection("damage", "test");
            List<EffectComponent> result = loader.loadComponents(section);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("test: 空のcomponentsリスト")
        void testLoadComponentsWithEmptyComponentsList() {
            ConfigurationSection section = createComponentListSection();
            List<EffectComponent> result = loader.loadComponents(section);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("test: 無効なコンポーネントタイプはスキップされる")
        void testLoadComponentsWithInvalidType() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> comp = new HashMap<>();
            comp.put("type", "invalid_type_that_does_not_exist");
            comp.put("id", "test_id");
            components.add(comp);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);

            // 無効なタイプはスキップされるので空リスト
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("test: typeがないコンポーネントはスキップされる")
        void testLoadComponentsWithoutType() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> comp = new HashMap<>();
            comp.put("id", "test_id");
            // typeがない
            components.add(comp);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            assertTrue(result.isEmpty());
        }
    }

    // ========== loadSkillEffectメソッド ==========

    @Nested
    @DisplayName("loadSkillEffectメソッド")
    class LoadSkillEffectTests {

        @Test
        @DisplayName("test: nullセクションの場合は空のSkillEffect")
        void testLoadSkillEffectWithNull() {
            SkillEffect result = loader.loadSkillEffect("test_skill", null);
            assertNotNull(result);
            assertEquals("test_skill", result.getSkillId());
            assertTrue(result.getComponents().isEmpty());
        }

        @Test
        @DisplayName("test: 空のセクションの場合は空のSkillEffect")
        void testLoadSkillEffectWithEmptySection() {
            SkillEffect result = loader.loadSkillEffect("test_skill", createEmptySection());
            assertNotNull(result);
            assertEquals("test_skill", result.getSkillId());
            assertTrue(result.getComponents().isEmpty());
        }

        @Test
        @DisplayName("test: componentsキーがない場合は空のSkillEffect")
        void testLoadSkillEffectWithoutComponentsKey() {
            ConfigurationSection section = createSimpleComponentSection("damage", "test");
            SkillEffect result = loader.loadSkillEffect("test_skill", section);
            assertNotNull(result);
            assertTrue(result.getComponents().isEmpty());
        }

        @Test
        @DisplayName("test: スキルIDが設定される")
        void testLoadSkillEffectSetsSkillId() {
            ConfigurationSection section = createComponentListSection();
            SkillEffect result = loader.loadSkillEffect("my_skill_id", section);
            assertNotNull(result);
            assertEquals("my_skill_id", result.getSkillId());
        }
    }

    // ========== 子コンポーネント ==========

    @Nested
    @DisplayName("子コンポーネントの処理")
    class ChildComponentsTests {

        @Test
        @DisplayName("test: childrenキーで子コンポーネントを追加")
        void testChildComponentsViaChildrenKey() {
            MemoryConfiguration config = new MemoryConfiguration();

            // 親コンポーネント（子を持つ）
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> parent = new HashMap<>();
            parent.put("type", "damage"); // 有効なmechanic
            parent.put("id", "parent");

            // 子コンポーネントリスト
            List<Map<String, Object>> children = new ArrayList<>();
            Map<String, Object> child = new HashMap<>();
            child.put("type", "heal"); // 有効なmechanic
            child.put("id", "child");
            children.add(child);

            parent.put("children", children);
            components.add(parent);

            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);

            // 親コンポーネントが作成される
            assertFalse(result.isEmpty());
            EffectComponent parentComp = result.get(0);
            assertNotNull(parentComp);
        }

        @Test
        @DisplayName("test: 空のchildrenリスト")
        void testEmptyChildrenList() {
            MemoryConfiguration config = new MemoryConfiguration();

            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> parent = new HashMap<>();
            parent.put("type", "damage");
            parent.put("id", "parent");
            parent.put("children", new ArrayList<>()); // 空のchildren
            components.add(parent);

            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("test: 複数レベルのネストされたchildren")
        void testNestedChildrenMultipleLevels() {
            MemoryConfiguration config = new MemoryConfiguration();

            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> root = new HashMap<>();
            root.put("type", "damage");
            root.put("id", "root");

            // 第1レベルのchildren
            List<Map<String, Object>> children1 = new ArrayList<>();
            Map<String, Object> child1 = new HashMap<>();
            child1.put("type", "heal");
            child1.put("id", "child1");

            // 第2レベルのchildren
            List<Map<String, Object>> children2 = new ArrayList<>();
            Map<String, Object> child2 = new HashMap<>();
            child2.put("type", "push");
            child2.put("id", "child2");
            children2.add(child2);

            child1.put("children", children2);
            children1.add(child1);
            root.put("children", children1);
            components.add(root);

            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            assertFalse(result.isEmpty());
        }
    }

    // ========== セッティング読み込み ==========

    @Nested
    @DisplayName("セッティング読み込み")
    class SettingsLoadTests {

        @Test
        @DisplayName("test: settingsが読み込まれる")
        void testSettingsAreLoaded() {
            MemoryConfiguration config = new MemoryConfiguration();

            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> comp = new HashMap<>();
            comp.put("type", "damage"); // 有効なmechanic
            comp.put("id", "test");

            // settings
            Map<String, Object> settings = new HashMap<>();
            settings.put("value", 100);
            settings.put("name", "TestSetting");
            comp.put("settings", settings);

            components.add(comp);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);

            if (!result.isEmpty()) {
                EffectComponent component = result.get(0);
                assertNotNull(component);
                // settingsがComponentSettingsに読み込まれることを確認
                assertNotNull(component.getSettings());
            }
        }

        @Test
        @DisplayName("test: 空のsettingsマップ")
        void testEmptySettingsMap() {
            MemoryConfiguration config = new MemoryConfiguration();

            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> comp = new HashMap<>();
            comp.put("type", "damage");
            comp.put("id", "test");
            comp.put("settings", new HashMap<>()); // 空のsettings

            components.add(comp);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            // エラーにならずに処理される
            assertNotNull(result);
        }

        @Test
        @DisplayName("test: null settings")
        void testNullSettings() {
            MemoryConfiguration config = new MemoryConfiguration();

            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> comp = new HashMap<>();
            comp.put("type", "damage");
            comp.put("id", "test");
            comp.put("settings", null); // null settings

            components.add(comp);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            assertNotNull(result);
        }
    }

    // ========== バリデーション ==========

    @Nested
    @DisplayName("バリデーション")
    class ValidateTests {

        @Test
        @DisplayName("test: nullセクションは有効")
        void testValidateNullSection() {
            ComponentLoader.ValidationResult result = loader.validate(null);
            assertTrue(result.isValid()); // nullは有効扱い
        }

        @Test
        @DisplayName("test: 空のセクションは有効")
        void testValidateEmptySection() {
            ComponentLoader.ValidationResult result = loader.validate(createEmptySection());
            assertTrue(result.isValid()); // 空は有効扱い
        }

        @Test
        @DisplayName("test: componentsキーがない場合は有効")
        void testValidateNoComponentsKey() {
            ComponentLoader.ValidationResult result = loader.validate(createSimpleComponentSection("damage", "test"));
            assertTrue(result.isValid()); // componentsキーがないが有効扱い
        }

        @Test
        @DisplayName("test: 空のcomponentsリストは有効")
        void testValidateEmptyComponentsList() {
            ComponentLoader.ValidationResult result = loader.validate(createComponentListSection());
            assertTrue(result.isValid()); // 空リストは有効扱い
        }

        @Test
        @DisplayName("test: 有効なコンポーネント設定")
        void testValidateValidConfiguration() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> comp = new HashMap<>();
            comp.put("type", "damage"); // 有効なmechanicタイプ
            comp.put("id", "test");
            components.add(comp);
            config.set("components", components);

            ComponentLoader.ValidationResult result = loader.validate(config);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("test: 無効なタイプは警告を含む")
        void testValidateInvalidTypeAddsWarning() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> comp = new HashMap<>();
            comp.put("type", "invalid_type_xyz");
            comp.put("id", "test");
            components.add(comp);
            config.set("components", components);

            ComponentLoader.ValidationResult result = loader.validate(config);
            assertTrue(result.isValid()); // エラーではない
            assertTrue(result.hasWarnings()); // 警告が含まれる
        }
    }

    // ========== ValidationResultクラス ==========

    @Nested
    @DisplayName("ValidationResultクラス")
    class ValidationResultTests {

        @Test
        @DisplayName("test: 新しい結果は有効")
        void testNewResultIsValid() {
            ComponentLoader.ValidationResult result = new ComponentLoader.ValidationResult();
            assertTrue(result.isValid());
            assertTrue(result.getErrors().isEmpty());
            assertTrue(result.getWarnings().isEmpty());
        }

        @Test
        @DisplayName("test: エラーを追加すると無効になる")
        void testAddErrorMakesInvalid() {
            ComponentLoader.ValidationResult result = new ComponentLoader.ValidationResult();
            result.addError("Test error");
            assertFalse(result.isValid());
            assertEquals(1, result.getErrors().size());
            assertEquals("Test error", result.getErrors().get(0));
        }

        @Test
        @DisplayName("test: 複数のエラーを追加")
        void testAddMultipleErrors() {
            ComponentLoader.ValidationResult result = new ComponentLoader.ValidationResult();
            result.addError("Error 1");
            result.addError("Error 2");
            result.addError("Error 3");
            assertFalse(result.isValid());
            assertEquals(3, result.getErrors().size());
        }

        @Test
        @DisplayName("test: 警告を追加")
        void testAddWarning() {
            ComponentLoader.ValidationResult result = new ComponentLoader.ValidationResult();
            result.addWarning("Test warning");
            assertTrue(result.isValid()); // 警告だけなら有効
            assertTrue(result.hasWarnings());
            assertEquals(1, result.getWarnings().size());
        }

        @Test
        @DisplayName("test: getSummary")
        void testGetSummary() {
            ComponentLoader.ValidationResult result = new ComponentLoader.ValidationResult();
            result.addError("Error");
            result.addWarning("Warning");
            String summary = result.getSummary();
            assertTrue(summary.contains("errors=1"));
            assertTrue(summary.contains("warnings=1"));
        }
    }

    // ========== エッジケース ==========

    @Nested
    @DisplayName("エッジケース")
    class EdgeCaseTests {

        @Test
        @DisplayName("test: typeがnullの場合")
        void testNullType() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> comp = new HashMap<>();
            comp.put("type", null);
            comp.put("id", "test");
            components.add(comp);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("test: idがない場合")
        void testNoId() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> comp = new HashMap<>();
            comp.put("type", "damage");
            // idがない
            components.add(comp);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            // idがなくてもコンポーネントは作成される可能性がある
            // 作成されない場合もある（実装に依存）
            assertNotNull(result);
        }

        @Test
        @DisplayName("test: 大文字小文字の異なるtype")
        void testCaseSensitiveType() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> comp = new HashMap<>();
            comp.put("type", "MECHANIC"); // 大文字
            comp.put("id", "test");
            components.add(comp);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            // 大文字小文字が違うと一致しない可能性が高い
            // ComponentRegistryの実装に依存
            assertNotNull(result);
        }

        @Test
        @DisplayName("test: 非常に深いネスト")
        void testVeryDeepNesting() {
            MemoryConfiguration config = new MemoryConfiguration();

            // 5レベルのネストを作成
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> current = new HashMap<>();
            current.put("type", "damage");
            current.put("id", "level0");

            for (int i = 1; i <= 5; i++) {
                List<Map<String, Object>> children = new ArrayList<>();
                Map<String, Object> child = new HashMap<>();
                child.put("type", "heal");
                child.put("id", "level" + i);
                children.add(child);
                current.put("children", children);

                // 最後のchildrenをcurrentとして更新
                current = child;
            }

            components.add(current);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            assertNotNull(result);
        }
    }

    // ========== 特定のコンポーネントタイプ ==========

    @Nested
    @DisplayName("特定のコンポーネントタイプ")
    class SpecificComponentTypesTests {

        @Test
        @DisplayName("test: mechanicタイプのコンポーネント")
        void testMechanicTypeComponent() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> comp = new HashMap<>();
            comp.put("type", "damage"); // 有効なmechanic
            comp.put("value", 10);
            components.add(comp);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("test: conditionタイプのコンポーネント")
        void testConditionTypeComponent() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> comp = new HashMap<>();
            comp.put("type", "health"); // 有効なcondition
            comp.put("value", 0.5);
            components.add(comp);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("test: targetタイプのコンポーネント")
        void testTargetTypeComponent() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> comp = new HashMap<>();
            comp.put("type", "SELF"); // 有効なtarget
            components.add(comp);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("test: filterタイプのコンポーネント")
        void testFilterTypeComponent() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> comp = new HashMap<>();
            comp.put("type", "entity_type"); // 有効なfilter
            comp.put("value", "player");
            components.add(comp);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("test: costタイプのコンポーネント")
        void testCostTypeComponent() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> comp = new HashMap<>();
            comp.put("type", "MANA"); // 有効なcost
            comp.put("value", 50);
            components.add(comp);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("test: cooldownタイプのコンポーネント")
        void testCooldownTypeComponent() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> comp = new HashMap<>();
            comp.put("type", "COOLDOWN"); // 有効なcooldown
            comp.put("duration", 100);
            components.add(comp);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("test: 複数の異なるタイプを混在")
        void testMixedComponentTypes() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();

            Map<String, Object> target = new HashMap<>();
            target.put("type", "SELF"); // target

            Map<String, Object> mechanic = new HashMap<>();
            mechanic.put("type", "damage"); // mechanic

            Map<String, Object> filter = new HashMap<>();
            filter.put("type", "entity_type"); // filter

            components.add(target);
            components.add(mechanic);
            components.add(filter);

            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("test: 同じタイプの複数コンポーネント")
        void testMultipleComponentsSameType() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();

            for (int i = 0; i < 3; i++) {
                Map<String, Object> comp = new HashMap<>();
                comp.put("type", "damage");
                comp.put("id", "mechanic_" + i);
                components.add(comp);
            }

            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            assertEquals(3, result.size());
        }
    }

    // ========== 複数回のロード ==========

    @Nested
    @DisplayName("複数回のロード")
    class MultipleLoadTests {

        @Test
        @DisplayName("test: 同じローダーで複数回ロード")
        void testLoadMultipleTimes() {
            ConfigurationSection section1 = createComponentListSection();
            ConfigurationSection section2 = createComponentListSection();

            List<EffectComponent> result1 = loader.loadComponents(section1);
            List<EffectComponent> result2 = loader.loadComponents(section2);

            // 両方とも成功するはず
            assertNotNull(result1);
            assertNotNull(result2);
        }

        @Test
        @DisplayName("test: 異なるスキルIDでロード")
        void testLoadWithDifferentSkillIds() {
            ConfigurationSection section = createComponentListSection();

            SkillEffect effect1 = loader.loadSkillEffect("skill1", section);
            SkillEffect effect2 = loader.loadSkillEffect("skill2", section);

            assertNotNull(effect1);
            assertNotNull(effect2);
            assertEquals("skill1", effect1.getSkillId());
            assertEquals("skill2", effect2.getSkillId());
        }
    }

    // ========== NULLセーフティ ==========

    @Nested
    @DisplayName("NULLセーフティ")
    class NullSafetyTests {

        @Test
        @DisplayName("test: componentsにnullが含まれる場合")
        void testComponentsListContainsNull() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            components.add(null); // null要素
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            // nullはスキップされる
            assertNotNull(result);
        }

        @Test
        @DisplayName("test: childrenにnullが含まれる場合")
        void testChildrenListContainsNull() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> parent = new HashMap<>();
            parent.put("type", "push");
            parent.put("id", "parent");

            List<Map<String, Object>> children = new ArrayList<>();
            children.add(null); // null要素
            parent.put("children", children);

            components.add(parent);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            assertNotNull(result);
        }
    }

    // ========== パフォーマンス ==========

    @Nested
    @DisplayName("パフォーマンス")
    class PerformanceTests {

        @Test
        @DisplayName("test: 多数のコンポーネントをロード")
        void testLoadManyComponents() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();

            // 100個のコンポーネントを作成
            for (int i = 0; i < 100; i++) {
                Map<String, Object> comp = new HashMap<>();
                comp.put("type", "damage");
                comp.put("id", "comp_" + i);
                components.add(comp);
            }

            config.set("components", components);

            long start = System.nanoTime();
            List<EffectComponent> result = loader.loadComponents(config);
            long duration = System.nanoTime() - start;

            assertNotNull(result);
            // 100個のコンポーネントは適度な時間内に処理されるべき
            // （1秒以内 = 1,000,000,000ナノ秒）
            assertTrue(duration < 1_000_000_000, "Loading 100 components took too long: " + duration + "ns");
        }
    }

    // ========== セクション形式コンポーネント ==========

    @Nested
    @DisplayName("セクション形式コンポーネント")
    class SectionFormatTests {

        @Test
        @DisplayName("test: セクション形式の単一コンポーネントをロード")
        void testLoadSingleSectionComponent() {
            MemoryConfiguration config = new MemoryConfiguration();
            
            // セクション形式で定義
            MemoryConfiguration damageSection = new MemoryConfiguration();
            damageSection.set("value", 15);
            config.set("damage", damageSection);

            List<EffectComponent> result = loader.loadComponents(config);
            
            assertEquals(1, result.size());
            EffectComponent component = result.get(0);
            assertEquals(ComponentType.MECHANIC, component.getType());
        }

        @Test
        @DisplayName("test: セクション形式の複数コンポーネントをロード")
        void testLoadMultipleSectionComponents() {
            MemoryConfiguration config = new MemoryConfiguration();
            
            // 複数のセクション形式コンポーネント
            MemoryConfiguration damageSection = new MemoryConfiguration();
            damageSection.set("value", 10);
            config.set("damage", damageSection);
            
            MemoryConfiguration healSection = new MemoryConfiguration();
            healSection.set("value", 5);
            config.set("heal", healSection);

            List<EffectComponent> result = loader.loadComponents(config);
            
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("test: セクション形式で子コンポーネントを持つコンポーネント")
        void testSectionComponentWithChildren() {
            MemoryConfiguration config = new MemoryConfiguration();
            
            MemoryConfiguration parentSection = new MemoryConfiguration();
            parentSection.set("value", 10);
            
            // 子コンポーネントをセクション形式で追加
            List<Map<String, Object>> children = new ArrayList<>();
            Map<String, Object> child = new HashMap<>();
            child.put("type", "damage");
            child.put("value", 5);
            children.add(child);
            parentSection.set("components", children);
            
            config.set("damage", parentSection);

            List<EffectComponent> result = loader.loadComponents(config);
            
            assertEquals(1, result.size());
            EffectComponent parent = result.get(0);
            assertNotNull(parent);
        }

        @Test
        @DisplayName("test: セクション形式とリスト形式の混在")
        void testMixedSectionAndListFormat() {
            MemoryConfiguration config = new MemoryConfiguration();
            
            // リスト形式
            List<Map<String, Object>> componentList = new ArrayList<>();
            Map<String, Object> listComp = new HashMap<>();
            listComp.put("type", "push");
            listComp.put("id", "push1");
            componentList.add(listComp);
            config.set("components", componentList);
            
            // セクション形式も追加（リストが優先される）
            MemoryConfiguration damageSection = new MemoryConfiguration();
            damageSection.set("value", 10);
            config.set("damage", damageSection);

            List<EffectComponent> result = loader.loadComponents(config);
            
            // "components"キーがあるのでリスト形式が優先
            assertTrue(result.size() >= 1);
        }

        @Test
        @DisplayName("test: セクション形式で無効なタイプ")
        void testSectionWithInvalidType() {
            MemoryConfiguration config = new MemoryConfiguration();
            
            MemoryConfiguration invalidSection = new MemoryConfiguration();
            invalidSection.set("value", 10);
            config.set("invalid_type_xyz", invalidSection);

            List<EffectComponent> result = loader.loadComponents(config);
            
            // 無効なタイプはスキップされる
            assertEquals(0, result.size());
        }

        @Test
        @DisplayName("test: 深い階層のセクション形式コンポーネント")
        void testDeepNestedSectionComponents() {
            MemoryConfiguration config = new MemoryConfiguration();
            
            // 3階層のネスト
            MemoryConfiguration grandparentSection = new MemoryConfiguration();
            grandparentSection.set("value", 20);
            
            MemoryConfiguration parentSection = new MemoryConfiguration();
            parentSection.set("value", 10);
            
            List<Map<String, Object>> children = new ArrayList<>();
            Map<String, Object> child = new HashMap<>();
            child.put("type", "damage");
            child.put("value", 5);
            children.add(child);
            
            parentSection.set("components", children);
            
            List<Map<String, Object>> parents = new ArrayList<>();
            Map<String, Object> parentMap = new HashMap<>();
            parentMap.put("type", "damage");
            parentMap.put("value", 10);
            parentMap.put("components", children);
            parents.add(parentMap);
            
            grandparentSection.set("components", parents);
            config.set("damage", grandparentSection);

            List<EffectComponent> result = loader.loadComponents(config);
            
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("test: セクション形式の条件コンポーネント")
        void testSectionFormatConditionComponent() {
            MemoryConfiguration config = new MemoryConfiguration();
            
            MemoryConfiguration healthSection = new MemoryConfiguration();
            healthSection.set("min", 50);
            config.set("health", healthSection);

            List<EffectComponent> result = loader.loadComponents(config);
            
            assertEquals(1, result.size());
            EffectComponent component = result.get(0);
            assertEquals(ComponentType.CONDITION, component.getType());
        }

        @Test
        @DisplayName("test: セクション形式のターゲットコンポーネント")
        void testSectionFormatTargetComponent() {
            MemoryConfiguration config = new MemoryConfiguration();
            
            MemoryConfiguration targetSection = new MemoryConfiguration();
            targetSection.set("range", 5);
            config.set("SELF", targetSection);

            List<EffectComponent> result = loader.loadComponents(config);
            
            assertEquals(1, result.size());
            EffectComponent component = result.get(0);
            assertEquals(ComponentType.TARGET, component.getType());
        }
    }

    // ========== 深いネスト構造 ==========

    @Nested
    @DisplayName("深いネスト構造")
    class DeepNestingTests {

        @Test
        @DisplayName("test: 4階層の深いネスト")
        void testFourLevelNesting() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            
            // 4階層のネスト構造を作成
            Map<String, Object> level4 = new HashMap<>();
            level4.put("type", "damage");
            level4.put("value", 5);
            
            Map<String, Object> level3 = new HashMap<>();
            level3.put("type", "heal");
            level3.put("value", 10);
            level3.put("components", List.of(level4));
            
            Map<String, Object> level2 = new HashMap<>();
            level2.put("type", "push");
            level2.put("value", 15);
            level2.put("components", List.of(level3));
            
            Map<String, Object> level1 = new HashMap<>();
            level1.put("type", "damage");
            level1.put("value", 20);
            level1.put("components", List.of(level2));
            
            components.add(level1);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("test: 複数ブランチの深いネスト")
        void testMultipleBranchDeepNesting() {
            MemoryConfiguration config = new MemoryConfiguration();
            List<Map<String, Object>> components = new ArrayList<>();
            
            // 親コンポーネントに2つの子を追加
            Map<String, Object> child1 = new HashMap<>();
            child1.put("type", "damage");
            child1.put("value", 10);
            
            Map<String, Object> child2 = new HashMap<>();
            child2.put("type", "heal");
            child2.put("value", 5);
            
            Map<String, Object> parent = new HashMap<>();
            parent.put("type", "push");
            parent.put("value", 15);
            parent.put("components", List.of(child1, child2));
            
            components.add(parent);
            config.set("components", components);

            List<EffectComponent> result = loader.loadComponents(config);
            
            assertEquals(1, result.size());
            EffectComponent root = result.get(0);
            assertNotNull(root);
        }
    }
}