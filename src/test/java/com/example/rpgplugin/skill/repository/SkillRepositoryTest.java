package com.example.rpgplugin.skill.repository;

import com.example.rpgplugin.model.skill.DamageCalculation;
import com.example.rpgplugin.skill.LevelDependentParameter;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.skill.SkillType;
import com.example.rpgplugin.skill.SkillTree;
import com.example.rpgplugin.stats.Stat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import com.example.rpgplugin.model.skill.DamageCalculation;

/**
 * SkillRepositoryの単体テスト
 *
 * <p>スキル登録、取得、検索、リロードのテスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("SkillRepository テスト")
class SkillRepositoryTest {

    private SkillRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SkillRepository();
    }

    // ==================== registerSkill テスト ====================

    @Test
    @DisplayName("registerSkill: 有効なスキルを登録")
    void testRegisterSkill_Valid() {
        Skill skill = createSimpleSkill("test_skill", "テストスキル");

        boolean result = repository.registerSkill(skill);

        assertTrue(result, "登録に成功すること");
        assertTrue(repository.hasSkill("test_skill"), "スキルが登録されていること");
        assertEquals(1, repository.size(), "スキル数が1であること");
    }

    @Test
    @DisplayName("registerSkill: nullスキルは登録不可")
    void testRegisterSkill_Null() {
        boolean result = repository.registerSkill(null);

        assertFalse(result, "nullスキルの登録は失敗すること");
        assertTrue(repository.isEmpty(), "スキルが登録されていないこと");
    }

    @Test
    @DisplayName("registerSkill: 重複IDは登録不可")
    void testRegisterSkill_Duplicate() {
        Skill skill1 = createSimpleSkill("duplicate", "スキル1");
        Skill skill2 = createSimpleSkill("duplicate", "スキル2");

        repository.registerSkill(skill1);
        boolean result = repository.registerSkill(skill2);

        assertFalse(result, "重複IDの登録は失敗すること");
        assertEquals(1, repository.size(), "元のスキルのみが残っていること");
        assertEquals("スキル1", repository.getSkill("duplicate").getDisplayName(), "元のスキルが保持されていること");
    }

    @Test
    @DisplayName("registerSkill: 複数スキルの登録")
    void testRegisterSkill_Multiple() {
        Skill skill1 = createSimpleSkill("skill1", "スキル1");
        Skill skill2 = createSimpleSkill("skill2", "スキル2");
        Skill skill3 = createSimpleSkill("skill3", "スキル3");

        assertTrue(repository.registerSkill(skill1));
        assertTrue(repository.registerSkill(skill2));
        assertTrue(repository.registerSkill(skill3));

        assertEquals(3, repository.size());
        assertTrue(repository.hasSkill("skill1"));
        assertTrue(repository.hasSkill("skill2"));
        assertTrue(repository.hasSkill("skill3"));
    }

    // ==================== getSkill テスト ====================

    @Test
    @DisplayName("getSkill: 登録済みスキルを取得")
    void testGetSkill_Exists() {
        Skill skill = createSimpleSkill("test_skill", "テストスキル");
        repository.registerSkill(skill);

        Skill retrieved = repository.getSkill("test_skill");

        assertNotNull(retrieved, "スキルが取得できること");
        assertEquals("test_skill", retrieved.getId());
        assertEquals("テストスキル", retrieved.getDisplayName());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"nonexistent"})
    @DisplayName("getSkill: 未登録スキルはnull")
    void testGetSkill_NotExists(String skillId) {
        Skill retrieved = repository.getSkill(skillId);
        assertNull(retrieved, "未登録スキルはnullを返すこと");
    }

    // ==================== getAllSkills テスト ====================

    @Test
    @DisplayName("getAllSkills: 全スキルのコピーを取得")
    void testGetAllSkills() {
        repository.registerSkill(createSimpleSkill("skill1", "スキル1"));
        repository.registerSkill(createSimpleSkill("skill2", "スキル2"));

        Map<String, Skill> allSkills = repository.getAllSkills();

        assertEquals(2, allSkills.size(), "全スキルが取得できること");
        assertTrue(allSkills.containsKey("skill1"));
        assertTrue(allSkills.containsKey("skill2"));

        // コピーの確認（元のマップに影響しない）
        allSkills.clear();
        assertEquals(2, repository.size(), "取得したマップをクリアしても元には影響しないこと");
    }

    @Test
    @DisplayName("getAllSkills: 空リポジトリ")
    void testGetAllSkills_Empty() {
        Map<String, Skill> allSkills = repository.getAllSkills();
        assertTrue(allSkills.isEmpty(), "空リポジトリでは空マップを返すこと");
    }

    // ==================== getAllSkillIds テスト ====================

    @Test
    @DisplayName("getAllSkillIds: 全スキルIDを取得")
    void testGetAllSkillIds() {
        repository.registerSkill(createSimpleSkill("skill1", "スキル1"));
        repository.registerSkill(createSimpleSkill("skill2", "スキル2"));

        Set<String> skillIds = repository.getAllSkillIds();

        assertEquals(2, skillIds.size());
        assertTrue(skillIds.contains("skill1"));
        assertTrue(skillIds.contains("skill2"));

        // セットの変更が元に影響しないことを確認
        skillIds.add("fake_id");
        assertFalse(repository.hasSkill("fake_id"), "取得したセットの変更は元に影響しないこと");
    }

    // ==================== getSkillsForClass テスト ====================

    @Test
    @DisplayName("getSkillsForClass: クラス指定でスキルを取得")
    void testGetSkillsForClass() {
        // ウォーリアー専用スキル
        Skill warriorSkill = createSimpleSkill("warrior_skill", "ウォーリアースキル");
        // 全クラス共通スキル（空リストは全クラス利用可能）
        Skill commonSkill = createClassCommonSkill("common_skill", "共通スキル");

        repository.registerSkill(warriorSkill);
        repository.registerSkill(commonSkill);

        List<Skill> warriorSkills = repository.getSkillsForClass("warrior");
        List<Skill> mageSkills = repository.getSkillsForClass("mage");

        // 全クラス共通スキルのみが取得される
        assertTrue(warriorSkills.stream().anyMatch(s -> s.getId().equals("common_skill")),
                "ウォーリアーは共通スキルを使用できること");
        assertTrue(mageSkills.stream().anyMatch(s -> s.getId().equals("common_skill")),
                "メイジも共通スキルを使用できること");
    }

    @Test
    @DisplayName("getSkillsForClass: 空リポジトリ")
    void testGetSkillsForClass_Empty() {
        List<Skill> skills = repository.getSkillsForClass("warrior");
        assertTrue(skills.isEmpty(), "空リポジトリでは空リストを返すこと");
    }

    // ==================== getTreeRegistry / getSkillTree テスト ====================

    @Test
    @DisplayName("getTreeRegistry: レジストリを取得")
    void testGetTreeRegistry() {
        assertNotNull(repository.getTreeRegistry(), "レジストリがnullでないこと");
    }

    @Test
    @DisplayName("getSkillTree: ツリーを取得")
    void testGetSkillTree() {
        Skill skill = createSimpleSkill("test_skill", "テストスキル");
        repository.registerSkill(skill);

        SkillTree tree = repository.getSkillTree("warrior");

        assertNotNull(tree, "ツリーが取得できること");
    }

    // ==================== clearAllSkills テスト ====================

    @Test
    @DisplayName("clearAllSkills: 全スキルをクリア")
    void testClearAllSkills() {
        repository.registerSkill(createSimpleSkill("skill1", "スキル1"));
        repository.registerSkill(createSimpleSkill("skill2", "スキル2"));
        assertEquals(2, repository.size());

        repository.clearAllSkills();

        assertTrue(repository.isEmpty(), "全スキルが削除されていること");
        assertFalse(repository.hasSkill("skill1"));
        assertFalse(repository.hasSkill("skill2"));
    }

    // ==================== reloadSkills テスト ====================

    @Test
    @DisplayName("reloadSkills: スキルをリロード")
    void testReloadSkills() {
        // 初期スキル
        repository.registerSkill(createSimpleSkill("old_skill", "旧スキル"));

        // 新しいスキルセット
        Map<String, Skill> newSkills = Map.of(
                "new_skill", createSimpleSkill("new_skill", "新スキル"),
                "another_skill", createSimpleSkill("another_skill", "別スキル")
        );

        SkillRepository.ReloadResult result = repository.reloadSkills(newSkills);

        assertEquals(2, result.getLoadedSkillCount(), "新しいスキル数が正しいこと");
        assertTrue(result.getRemovedSkills().contains("old_skill"), "削除されたスキルが検出されていること");
        assertEquals(2, repository.size(), "リロード後のスキル数が正しいこと");
        assertTrue(repository.hasSkill("new_skill"));
        assertFalse(repository.hasSkill("old_skill"));
    }

    @Test
    @DisplayName("reloadSkills: 削除なし")
    void testReloadSkills_NoRemovals() {
        repository.registerSkill(createSimpleSkill("skill1", "スキル1"));

        Map<String, Skill> newSkills = Map.of(
                "skill1", createSimpleSkill("skill1", "スキル1"),
                "skill2", createSimpleSkill("skill2", "スキル2")
        );

        SkillRepository.ReloadResult result = repository.reloadSkills(newSkills);

        assertFalse(result.hasRemovedSkills(), "削除されたスキルがないこと");
        assertTrue(result.getRemovedSkills().isEmpty());
    }

    @Test
    @DisplayName("reloadSkills: 空マップでクリア")
    void testReloadSkills_Clear() {
        repository.registerSkill(createSimpleSkill("skill1", "スキル1"));
        repository.registerSkill(createSimpleSkill("skill2", "スキル2"));

        SkillRepository.ReloadResult result = repository.reloadSkills(Map.of());

        assertEquals(0, result.getLoadedSkillCount());
        assertEquals(2, result.getRemovedSkills().size(), "全スキルが削除されていること");
        assertTrue(result.hasRemovedSkills());
        assertTrue(repository.isEmpty());
    }

    @Test
    @DisplayName("ReloadResult: toString")
    void testReloadResult_ToString() {
        SkillRepository.ReloadResult result = new SkillRepository.ReloadResult(
                10, Set.of("removed1", "removed2")
        );

        String str = result.toString();
        assertTrue(str.contains("loaded=10"));
        assertTrue(str.contains("removed=2"));
    }

    // ==================== hasSkill テスト ====================

    @Test
    @DisplayName("hasSkill: 登録済みスキルはtrue")
    void testHasSkill_Exists() {
        repository.registerSkill(createSimpleSkill("test_skill", "テスト"));
        assertTrue(repository.hasSkill("test_skill"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"nonexistent"})
    @DisplayName("hasSkill: 未登録スキルはfalse")
    void testHasSkill_NotExists(String skillId) {
        assertFalse(repository.hasSkill(skillId));
    }

    // ==================== size / isEmpty テスト ====================

    @Test
    @DisplayName("size: スキル数を取得")
    void testSize() {
        assertEquals(0, repository.size(), "初期状態は0");

        repository.registerSkill(createSimpleSkill("skill1", "スキル1"));
        assertEquals(1, repository.size());

        repository.registerSkill(createSimpleSkill("skill2", "スキル2"));
        assertEquals(2, repository.size());
    }

    @Test
    @DisplayName("isEmpty: 空判定")
    void testIsEmpty() {
        assertTrue(repository.isEmpty(), "初期状態は空");

        repository.registerSkill(createSimpleSkill("skill1", "スキル1"));
        assertFalse(repository.isEmpty(), "スキル登録後は空ではない");

        repository.clearAllSkills();
        assertTrue(repository.isEmpty(), "クリア後は空");
    }

    // ==================== スレッドセーフティ テスト ====================

    @Test
    @DisplayName("スレッドセーフティ: 並列登録")
    void testThreadSafety_ParallelRegister() throws InterruptedException {
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    Skill skill = createSimpleSkill(
                            "skill_" + index + "_" + j,
                            "スキル"
                    );
                    repository.registerSkill(skill);
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(threadCount * 10, repository.size(),
                "全スレッドのスキルが正しく登録されていること");
    }

    // ==================== ヘルパーメソッド ====================

    /**
     * シンプルなスキルを作成します
     */
    private Skill createSimpleSkill(String id, String displayName) {
        DamageCalculation damage = new DamageCalculation(
                10.0, Stat.STRENGTH, 1.0, 0.0
        );
        LevelDependentParameter cooldownParam = new LevelDependentParameter(
                5.0, 0.0, null, null
        );
        LevelDependentParameter costParam = new LevelDependentParameter(
                10.0, 0.0, null, null
        );

        return new Skill(
                id,
                id,  // name
                displayName,
                SkillType.NORMAL,
                List.of(),  // description
                1,     // maxLevel
                5.0,   // cooldown
                10,    // manaCost
                cooldownParam,
                costParam,
                SkillCostType.MANA,
                damage,
                null,  // skillTree
                null,  // icon
                List.of("warrior", "mage")  // availableClasses
        );
    }

    /**
     * 全クラス共通スキルを作成します
     */
    private Skill createClassCommonSkill(String id, String displayName) {
        DamageCalculation damage = new DamageCalculation(
                5.0, Stat.STRENGTH, 1.0, 0.0
        );
        LevelDependentParameter cooldownParam = new LevelDependentParameter(
                3.0, 0.0, null, null
        );
        LevelDependentParameter costParam = new LevelDependentParameter(
                5.0, 0.0, null, null
        );

        return new Skill(
                id,
                id,
                displayName,
                SkillType.NORMAL,
                List.of(),
                1,
                3.0,
                5,
                cooldownParam,
                costParam,
                SkillCostType.MANA,
                damage,
                null,
                null,
                List.of()  // 空リスト = 全クラス利用可能
        );
    }
}
