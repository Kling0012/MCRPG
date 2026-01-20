package com.example.rpgplugin.skill.repository;

import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillTree;
import com.example.rpgplugin.skill.SkillTreeRegistry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * スキルリポジトリ
 *
 * <p>全スキルの登録・取得・検索を行います。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキルデータ管理の単一責務</li>
 *   <li>Thread-Safety: ConcurrentHashMapによるスレッドセーフ</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillRepository {

    private static final Logger LOGGER = Logger.getLogger(SkillRepository.class.getName());

    /** 登録されたスキル */
    private final Map<String, Skill> skills;

    /** スキルツリーレジストリ */
    private final SkillTreeRegistry treeRegistry;

    /**
     * コンストラクタ
     */
    public SkillRepository() {
        this.skills = new ConcurrentHashMap<>();
        this.treeRegistry = new SkillTreeRegistry();
    }

    /**
     * スキルを登録します
     *
     * @param skill 登録するスキル
     * @return 重複がある場合はfalse
     */
    public boolean registerSkill(Skill skill) {
        if (skill == null || skill.getId() == null) {
            LOGGER.warning("Cannot register null skill or skill with null ID");
            return false;
        }
        if (skills.containsKey(skill.getId())) {
            LOGGER.warning(() -> "Skill already registered: " + skill.getId());
            return false;
        }
        skills.put(skill.getId(), skill);
        LOGGER.info(() -> "Skill registered: " + skill.getId());

        // スキルツリーレジストリにも登録
        treeRegistry.registerSkill(skill);

        return true;
    }

    /**
     * 既存のスキルを更新します
     *
     * <p>スキルが存在しない場合は登録します。</p>
     *
     * @param skill 更新するスキル
     * @return 成功した場合はtrue
     */
    public boolean updateSkill(Skill skill) {
        if (skill == null || skill.getId() == null) {
            LOGGER.warning("Cannot update null skill or skill with null ID");
            return false;
        }

        boolean wasExisting = skills.containsKey(skill.getId());
        skills.put(skill.getId(), skill);

        // スキルツリーレジストリを更新
        treeRegistry.registerSkill(skill);

        if (wasExisting) {
            LOGGER.info(() -> "Skill updated: " + skill.getId());
        } else {
            LOGGER.info(() -> "Skill registered (via update): " + skill.getId());
        }

        return true;
    }

    /**
     * スキルを取得します
     *
     * @param skillId スキルID
     * @return スキル、見つからない場合はnull
     */
    public Skill getSkill(String skillId) {
        if (skillId == null) {
            return null;
        }
        return skills.get(skillId);
    }

    /**
     * 全スキルを取得します
     *
     * @return 全スキルのマップ（コピー）
     */
    public Map<String, Skill> getAllSkills() {
        return new HashMap<>(skills);
    }

    /**
     * 全スキルIDを取得します
     *
     * @return スキルIDのセット
     */
    public Set<String> getAllSkillIds() {
        return new HashSet<>(skills.keySet());
    }

    /**
     * 指定されたクラスで使用可能なスキルを取得します
     *
     * @param classId クラスID
     * @return 使用可能なスキルリスト
     */
    public List<Skill> getSkillsForClass(String classId) {
        return skills.values().stream()
                .filter(skill -> skill.isAvailableForClass(classId))
                .collect(Collectors.toList());
    }

    /**
     * スキルツリーレジストリを取得します
     *
     * @return スキルツリーレジストリ
     */
    public SkillTreeRegistry getTreeRegistry() {
        return treeRegistry;
    }

    /**
     * 指定されたクラスのスキルツリーを取得します
     *
     * @param classId クラスID
     * @return スキルツリー
     */
    public SkillTree getSkillTree(String classId) {
        return treeRegistry.getTree(classId);
    }

    /**
     * 全スキルデータをクリアします
     */
    public void clearAllSkills() {
        skills.clear();
    }

    /**
     * スキルをリロードし、削除されたスキル情報を返します
     *
     * @param newSkills 新しいスキルマップ
     * @return 削除されたスキルIDのセット
     */
    public ReloadResult reloadSkills(Map<String, Skill> newSkills) {
        Set<String> oldSkillIds = new HashSet<>(skills.keySet());
        Set<String> newSkillIds = new HashSet<>(newSkills.keySet());

        // 削除されたスキルを検出
        Set<String> removedSkills = new HashSet<>(oldSkillIds);
        removedSkills.removeAll(newSkillIds);

        // 新しいスキルマップを適用
        skills.clear();
        skills.putAll(newSkills);

        // スキルツリーキャッシュを全て無効化
        treeRegistry.invalidateAll();

        final int skillsSize = skills.size();
        final int removedSize = removedSkills.size();
        LOGGER.info(() -> "Reloaded " + skillsSize + " skills (removed: " + removedSize + ")");

        return new ReloadResult(newSkills.size(), removedSkills);
    }

    /**
     * スキルが登録されているかチェックします
     *
     * @param skillId スキルID
     * @return 登録されている場合はtrue
     */
    public boolean hasSkill(String skillId) {
        if (skillId == null) {
            return false;
        }
        return skills.containsKey(skillId);
    }

    /**
     * 登録されているスキル数を返します
     *
     * @return スキル数
     */
    public int size() {
        return skills.size();
    }

    /**
     * スキルが空かチェックします
     *
     * @return スキルが登録されていない場合はtrue
     */
    public boolean isEmpty() {
        return skills.isEmpty();
    }

    /**
     * スキルリロード結果
     */
    public static class ReloadResult {
        private final int loadedSkillCount;
        private final Set<String> removedSkills;

        public ReloadResult(int loadedSkillCount, Set<String> removedSkills) {
            this.loadedSkillCount = loadedSkillCount;
            this.removedSkills = new HashSet<>(removedSkills);
        }

        public int getLoadedSkillCount() {
            return loadedSkillCount;
        }

        public Set<String> getRemovedSkills() {
            return new HashSet<>(removedSkills);
        }

        public boolean hasRemovedSkills() {
            return !removedSkills.isEmpty();
        }

        @Override
        public String toString() {
            return "ReloadResult{" +
                    "loaded=" + loadedSkillCount +
                    ", removed=" + removedSkills.size() +
                    '}';
        }
    }
}
