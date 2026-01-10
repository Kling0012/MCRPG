package com.example.rpgplugin.skill;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * スキルツリーレジストリ
 *
 * <p>クラスごとのスキルツリーを管理し、動的な更新を可能にします。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキルツリーのキャッシュと管理に専念</li>
 *   <li>Singleton: クラスIDごとに単一のツリーを管理</li>
 * </ul>
 *
 * <p>機能:</p>
 * <ul>
 *   <li>クラスごとのスキルツリー構築・キャッシュ</li>
 *   <li>スキル追加時の自動ツリー更新</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillTreeRegistry {

    /** クラスIDごとのスキルツリーキャッシュ */
    private final Map<String, SkillTree> treeCache = new ConcurrentHashMap<>();

    /** スキルIDから所属クラスのマッピング */
    private final Map<String, String> skillToClassMap = new ConcurrentHashMap<>();

    /** 登録済みスキル（全スキル） */
    private final Map<String, Skill> registeredSkills = new ConcurrentHashMap<>();

    /**
     * スキルツリーを取得します
     *
     * <p>キャッシュに存在しない場合は自動的に構築します。</p>
     *
     * @param classId クラスID
     * @return スキルツリー、存在しない場合は空のツリー
     */
    public SkillTree getTree(String classId) {
        if (classId == null) {
            // クラスが未設定の場合は空のツリーを返す
            return new SkillTree("default");
        }
        return treeCache.computeIfAbsent(classId, this::buildTreeForClass);
    }

    /**
     * スキルを登録し、ツリーを更新します
     *
     * @param skill 登録するスキル
     * @return 成功した場合はtrue
     */
    public boolean registerSkill(Skill skill) {
        String skillId = skill.getId();

        // 既存スキルの場合は更新
        if (registeredSkills.containsKey(skillId)) {
            registeredSkills.put(skillId, skill);
            // 所属クラスのツリーを再構築
            rebuildTreeForSkill(skillId);
            return true;
        }

        // 新規スキルの登録
        registeredSkills.put(skillId, skill);

        // 利用可能なクラスを判定
        List<String> availableClasses = skill.getAvailableClasses();
        Set<String> targetClasses;

        if (availableClasses.isEmpty()) {
            // 全クラスで利用可能な場合、すべてのキャッシュをクリア
            targetClasses = new HashSet<>(treeCache.keySet());
        } else {
            // 特定クラスのみ利用可能
            targetClasses = new HashSet<>(availableClasses);
        }

        // 各クラスのスキルとしてマッピング
        for (String classId : targetClasses) {
            skillToClassMap.put(skillId, classId);
            invalidateTree(classId);
        }

        return true;
    }

    /**
     * スキルが登録されているかチェックします
     *
     * @param skillId スキルID
     * @return 登録されている場合はtrue
     */
    public boolean isSkillRegistered(String skillId) {
        return registeredSkills.containsKey(skillId);
    }

    /**
     * 登録済みスキルを取得します
     *
     * @param skillId スキルID
     * @return スキル、未登録の場合はnull
     */
    public Skill getRegisteredSkill(String skillId) {
        return registeredSkills.get(skillId);
    }

    /**
     * 登録済みスキルをクラスでフィルタリングして取得します
     *
     * @param classId クラスID
     * @return スキルのリスト
     */
    public List<Skill> getSkillsForClass(String classId) {
        List<Skill> result = new ArrayList<>();
        for (Skill skill : registeredSkills.values()) {
            List<String> availableClasses = skill.getAvailableClasses();
            if (availableClasses.isEmpty() || availableClasses.contains(classId)) {
                result.add(skill);
            }
        }
        return result;
    }

    /**
     * 全スキルを取得します
     *
     * @return 全スキルのマップ（コピー）
     */
    public Map<String, Skill> getAllSkills() {
        return new HashMap<>(registeredSkills);
    }

    /**
     * 登録済みスキル数を取得します
     *
     * @return スキル数
     */
    public int getSkillCount() {
        return registeredSkills.size();
    }



    /**
     * キャッシュをクリアします
     */
    public void clearCache() {
        treeCache.clear();
        skillToClassMap.clear();
    }

    /**
     * 特定クラスのキャッシュを無効化します
     *
     * @param classId クラスID
     */
    public void invalidateTree(String classId) {
        treeCache.remove(classId);
    }

    /**
     * 全キャッシュを無効化します
     */
    public void invalidateAll() {
        treeCache.clear();
    }

    /**
     * スキルの所属クラスのツリーを再構築します
     *
     * @param skillId スキルID
     */
    private void rebuildTreeForSkill(String skillId) {
        String classId = skillToClassMap.get(skillId);
        if (classId != null) {
            invalidateTree(classId);
        }
    }



    /**
     * クラスのスキルツリーを構築します
     *
     * @param classId クラスID
     * @return 構築されたスキルツリー
     */
    private SkillTree buildTreeForClass(String classId) {
        SkillTree tree = new SkillTree(classId);

        // クラスで利用可能なスキルを取得
        List<Skill> classSkills = getSkillsForClass(classId);

        // スキルノードを作成
        Map<String, SkillNode> nodeMap = new HashMap<>();
        for (Skill skill : classSkills) {
            SkillNode node = new SkillNode(skill, null);
            nodeMap.put(skill.getId(), node);
        }

        // 親子関係を構築
        for (Skill skill : classSkills) {
            SkillNode node = nodeMap.get(skill.getId());
            if (node == null) {
                continue;
            }

            Skill.SkillTreeConfig treeConfig = skill.getSkillTree();
            if (treeConfig != null) {
                String parentId = treeConfig.getParent();
                if (parentId != null && !"none".equalsIgnoreCase(parentId)) {
                    SkillNode parentNode = nodeMap.get(parentId);
                    if (parentNode != null) {
                        // 親ノードに追加（SkillTree.addNode側で処理されるため、ここでは直接関係を設定）
                        // ノードをツリーに追加
                        tree.addNode(node);
                    } else {
                        // 親が見つからない場合はルートとして扱う
                        tree.addNode(node);
                    }
                } else {
                    // ルートノード
                    tree.addNode(node);
                }
            } else {
                // ツリー設定がない場合はルート
                tree.addNode(node);
            }
        }

        return tree;
    }
}
