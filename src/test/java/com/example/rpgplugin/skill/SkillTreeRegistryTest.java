package com.example.rpgplugin.skill;

import com.example.rpgplugin.model.skill.SkillTreeConfig;
import com.example.rpgplugin.model.skill.UnlockRequirement;
import org.junit.jupiter.api.AfterEach;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SkillTreeRegistryのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SkillTreeRegistry: スキルツリーレジストリ")
class SkillTreeRegistryTest {

    @Mock
    private Skill mockSkill;

    @Mock
    private Skill mockSkill2;

    @Mock
    private SkillTreeConfig mockTreeConfig;

    @Mock
    private SkillTreeConfig mockTreeConfig2;

    private SkillTreeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SkillTreeRegistry();

        // mockSkillの基本設定
        when(mockSkill.getId()).thenReturn("testSkill");
        when(mockSkill.getAvailableClasses()).thenReturn(new ArrayList<>());
        when(mockSkill.getSkillTree()).thenReturn(mockTreeConfig);
        when(mockTreeConfig.getParent()).thenReturn(null);

        // mockSkill2の基本設定
        when(mockSkill2.getId()).thenReturn("testSkill2");
        when(mockSkill2.getAvailableClasses()).thenReturn(new ArrayList<>());
        when(mockSkill2.getSkillTree()).thenReturn(mockTreeConfig2);
        when(mockTreeConfig2.getParent()).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        registry.clearCache();
    }

    // ========== getTree() テスト ==========

    @Nested
    @DisplayName("getTree: スキルツリーの取得")
    class GetTreeTests {

        @Test
        @DisplayName("test: nullを渡すとデフォルトツリーを返す")
        void testGetTreeWithNull() {
            SkillTree tree = registry.getTree(null);

            assertNotNull(tree);
            assertEquals("default", tree.getClassId());
        }

        @Test
        @DisplayName("test: 存在しないクラスIDでは空のツリーを返す")
        void testGetTreeWithNonExistentClass() {
            SkillTree tree = registry.getTree("nonexistent");

            assertNotNull(tree);
            assertEquals("nonexistent", tree.getClassId());
            // スキルが登録されていないので空のツリー
            assertTrue(tree.getRootNodes().isEmpty());
        }

        @Test
        @DisplayName("test: キャッシュされたツリーを返す")
        void testGetTreeReturnsCached() {
            // 最初の呼び出しでキャッシュに格納
            SkillTree tree1 = registry.getTree("warrior");
            SkillTree tree2 = registry.getTree("warrior");

            // 同じインスタンスではないが、同じ内容を持つ
            assertNotNull(tree1);
            assertNotNull(tree2);
            assertEquals(tree1.getClassId(), tree2.getClassId());
        }
    }

    // ========== registerSkill() テスト ==========

    @Nested
    @DisplayName("registerSkill: スキルの登録")
    class RegisterSkillTests {

        @Test
        @DisplayName("test: 新規スキルを登録")
        void testRegisterNewSkill() {
            boolean result = registry.registerSkill(mockSkill);

            assertTrue(result);
            assertTrue(registry.isSkillRegistered("testSkill"));
            assertSame(mockSkill, registry.getRegisteredSkill("testSkill"));
        }

        @Test
        @DisplayName("test: 既存スキルを更新")
        void testRegisterExistingSkill() {
            registry.registerSkill(mockSkill);

            // 同じスキルを再登録
            boolean result = registry.registerSkill(mockSkill);

            assertTrue(result);
            assertSame(mockSkill, registry.getRegisteredSkill("testSkill"));
        }

        @Test
        @DisplayName("test: 特定クラスのスキルを登録")
        void testRegisterSkillWithSpecificClass() {
            List<String> classes = new ArrayList<>();
            classes.add("warrior");
            when(mockSkill.getAvailableClasses()).thenReturn(classes);

            registry.registerSkill(mockSkill);

            // クラスでスキルを取得
            List<Skill> warriorSkills = registry.getSkillsForClass("warrior");
            assertEquals(1, warriorSkills.size());
            assertTrue(warriorSkills.contains(mockSkill));
        }

        @Test
        @DisplayName("test: 全クラススキルを登録")
        void testRegisterSkillForAllClasses() {
            // 利用可能なクラスが空の場合は全クラスで利用可能
            when(mockSkill.getAvailableClasses()).thenReturn(new ArrayList<>());

            registry.registerSkill(mockSkill);

            // どのクラスでもスキルを取得可能
            List<Skill> warriorSkills = registry.getSkillsForClass("warrior");
            List<Skill> mageSkills = registry.getSkillsForClass("mage");
            assertEquals(1, warriorSkills.size());
            assertEquals(1, mageSkills.size());
        }
    }

    // ========== isSkillRegistered() テスト ==========

    @Nested
    @DisplayName("isSkillRegistered: スキル登録確認")
    class IsSkillRegisteredTests {

        @Test
        @DisplayName("test: 登録済みスキルはtrue")
        void testIsSkillRegisteredTrue() {
            registry.registerSkill(mockSkill);
            assertTrue(registry.isSkillRegistered("testSkill"));
        }

        @Test
        @DisplayName("test: 未登録スキルはfalse")
        void testIsSkillRegisteredFalse() {
            assertFalse(registry.isSkillRegistered("nonexistent"));
        }
    }

    // ========== getRegisteredSkill() テスト ==========

    @Nested
    @DisplayName("getRegisteredSkill: 登録済みスキルの取得")
    class GetRegisteredSkillTests {

        @Test
        @DisplayName("test: 登録済みスキルを取得")
        void testGetRegisteredSkill() {
            registry.registerSkill(mockSkill);
            assertSame(mockSkill, registry.getRegisteredSkill("testSkill"));
        }

        @Test
        @DisplayName("test: 未登録スキルはnull")
        void testGetRegisteredSkillNotRegistered() {
            assertNull(registry.getRegisteredSkill("nonexistent"));
        }
    }

    // ========== getSkillsForClass() テスト ==========

    @Nested
    @DisplayName("getSkillsForClass: クラス別スキル取得")
    class GetSkillsForClassTests {

        @Test
        @DisplayName("test: 全クラススキルを取得")
        void testGetSkillsForClassWithAllClassSkill() {
            when(mockSkill.getAvailableClasses()).thenReturn(new ArrayList<>());
            registry.registerSkill(mockSkill);

            List<Skill> skills = registry.getSkillsForClass("warrior");
            assertEquals(1, skills.size());
            assertTrue(skills.contains(mockSkill));
        }

        @Test
        @DisplayName("test: 特定クラススキルを取得")
        void testGetSkillsForClassWithSpecificClassSkill() {
            List<String> classes = new ArrayList<>();
            classes.add("warrior");
            when(mockSkill.getAvailableClasses()).thenReturn(classes);
            registry.registerSkill(mockSkill);

            List<Skill> warriorSkills = registry.getSkillsForClass("warrior");
            List<Skill> mageSkills = registry.getSkillsForClass("mage");

            assertEquals(1, warriorSkills.size());
            assertEquals(0, mageSkills.size());
        }

        @Test
        @DisplayName("test: 複数スキルを取得")
        void testGetSkillsForClassWithMultipleSkills() {
            List<String> classes = new ArrayList<>();
            classes.add("warrior");
            when(mockSkill.getAvailableClasses()).thenReturn(classes);
            when(mockSkill2.getAvailableClasses()).thenReturn(classes);

            registry.registerSkill(mockSkill);
            registry.registerSkill(mockSkill2);

            List<Skill> skills = registry.getSkillsForClass("warrior");
            assertEquals(2, skills.size());
        }

        @Test
        @DisplayName("test: 1つのスキルが複数クラスで利用可能（マルチクラス対応）")
        void testSkillAvailableForMultipleClasses() {
            // 1つのスキルが複数クラスで使える設定
            List<String> multiClasses = new ArrayList<>();
            multiClasses.add("warrior");
            multiClasses.add("mage");
            multiClasses.add("rogue");
            when(mockSkill.getAvailableClasses()).thenReturn(multiClasses);

            registry.registerSkill(mockSkill);

            // 全てのクラスでスキルを取得可能
            List<Skill> warriorSkills = registry.getSkillsForClass("warrior");
            List<Skill> mageSkills = registry.getSkillsForClass("mage");
            List<Skill> rogueSkills = registry.getSkillsForClass("rogue");

            assertEquals(1, warriorSkills.size());
            assertEquals(1, mageSkills.size());
            assertEquals(1, rogueSkills.size());

            // 同じスキルインスタンスであることを確認
            assertSame(mockSkill, warriorSkills.get(0));
            assertSame(mockSkill, mageSkills.get(0));
            assertSame(mockSkill, rogueSkills.get(0));
        }

        @Test
        @DisplayName("test: 空のリストを返す（スキル未登録）")
        void testGetSkillsForClassEmpty() {
            List<Skill> skills = registry.getSkillsForClass("warrior");
            assertNotNull(skills);
            assertTrue(skills.isEmpty());
        }
    }

    // ========== getAllSkills() テスト ==========

    @Nested
    @DisplayName("getAllSkills: 全スキル取得")
    class GetAllSkillsTests {

        @Test
        @DisplayName("test: 全スキルのマップを取得")
        void testGetAllSkills() {
            registry.registerSkill(mockSkill);
            registry.registerSkill(mockSkill2);

            Map<String, Skill> allSkills = registry.getAllSkills();

            assertEquals(2, allSkills.size());
            assertTrue(allSkills.containsKey("testSkill"));
            assertTrue(allSkills.containsKey("testSkill2"));
        }

        @Test
        @DisplayName("test: 返されたマップはコピー")
        void testGetAllSkillsReturnsCopy() {
            registry.registerSkill(mockSkill);

            Map<String, Skill> skills1 = registry.getAllSkills();
            Map<String, Skill> skills2 = registry.getAllSkills();

            // 別のインスタンス
            assertNotSame(skills1, skills2);
            // 内容は同じ
            assertEquals(skills1, skills2);
        }

        @Test
        @DisplayName("test: 空のレジストリ")
        void testGetAllSkillsEmpty() {
            Map<String, Skill> allSkills = registry.getAllSkills();
            assertNotNull(allSkills);
            assertTrue(allSkills.isEmpty());
        }
    }

    // ========== getSkillCount() テスト ==========

    @Nested
    @DisplayName("getSkillCount: スキル数取得")
    class GetSkillCountTests {

        @Test
        @DisplayName("test: 登録済みスキル数を取得")
        void testGetSkillCount() {
            assertEquals(0, registry.getSkillCount());

            registry.registerSkill(mockSkill);
            assertEquals(1, registry.getSkillCount());

            registry.registerSkill(mockSkill2);
            assertEquals(2, registry.getSkillCount());
        }

        @Test
        @DisplayName("test: 重複登録でもカウントは増えない")
        void testGetSkillCountWithDuplicateRegistration() {
            registry.registerSkill(mockSkill);
            registry.registerSkill(mockSkill); // 同じスキル

            assertEquals(1, registry.getSkillCount());
        }
    }

    // ========== clearCache() テスト ==========

    @Nested
    @DisplayName("clearCache: キャッシュクリア")
    class ClearCacheTests {

        @Test
        @DisplayName("test: キャッシュをクリア")
        void testClearCache() {
            registry.registerSkill(mockSkill);
            registry.getTree("warrior"); // キャッシュに格納

            registry.clearCache();

            // キャッシュクリア後もスキルは残っている
            assertTrue(registry.isSkillRegistered("testSkill"));
            // ツリーは再構築される
            assertNotNull(registry.getTree("warrior"));
        }
    }

    // ========== invalidateTree() テスト ==========

    @Nested
    @DisplayName("invalidateTree: ツリー無効化")
    class InvalidateTreeTests {

        @Test
        @DisplayName("test: 特定クラスのツリーを無効化")
        void testInvalidateTree() {
            registry.getTree("warrior"); // キャッシュに格納

            registry.invalidateTree("warrior");

            // キャッシュから削除されるが、再取得可能
            SkillTree tree = registry.getTree("warrior");
            assertNotNull(tree);
        }

        @Test
        @DisplayName("test: 存在しないクラスで無効化してもエラーにならない")
        void testInvalidateTreeNonExistent() {
            assertDoesNotThrow(() -> {
                registry.invalidateTree("nonexistent");
            });
        }
    }

    // ========== invalidateAll() テスト ==========

    @Nested
    @DisplayName("invalidateAll: 全キャッシュ無効化")
    class InvalidateAllTests {

        @Test
        @DisplayName("test: 全キャッシュを無効化")
        void testInvalidateAll() {
            registry.getTree("warrior");
            registry.getTree("mage");

            registry.invalidateAll();

            // 再取得可能
            assertNotNull(registry.getTree("warrior"));
            assertNotNull(registry.getTree("mage"));
        }
    }
}
