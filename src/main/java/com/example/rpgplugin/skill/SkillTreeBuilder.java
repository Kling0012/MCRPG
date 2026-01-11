package com.example.rpgplugin.skill;

import java.util.*;
import java.util.logging.Logger;

/**
 * スキルツリービルダー
 *
 * <p>スキルからスキルツリーを構築するビルダークラスです。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキルツリーの構築に専念</li>
 *   <li>Builder: 段階的なツリー構築をサポート</li>
 *   <li>KISS: シンプルなAPI設計</li>
 * </ul>
 *
 * <p>機能:</p>
 * <ul>
 *   <li>スキルリストからのツリー自動構築</li>
 *   <li>親子関係の自動解決</li>
 *   <li>循環参照の検出</li>
 *   <li>バリデーション機能</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillTreeBuilder {

    private final Logger logger;
    private final String classId;
    private final Map<String, Skill> skills;
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    /**
     * コンストラクタ
     *
     * @param classId クラスID
     * @param logger ロガー
     */
    public SkillTreeBuilder(String classId, Logger logger) {
        this.classId = classId;
        this.logger = logger;
        this.skills = new HashMap<>();
    }

    /**
     * コンストラクタ（スキルリスト付き）
     *
     * @param classId クラスID
     * @param skills スキルリスト
     * @param logger ロガー
     */
    public SkillTreeBuilder(String classId, List<Skill> skills, Logger logger) {
        this.classId = classId;
        this.logger = logger;
        this.skills = new HashMap<>();
        for (Skill skill : skills) {
            addSkill(skill);
        }
    }

    /**
     * スキルを追加します
     *
     * @param skill スキル
     * @return this（メソッドチェーン用）
     */
    public SkillTreeBuilder addSkill(Skill skill) {
        skills.put(skill.getId(), skill);
        return this;
    }

    /**
     * 複数のスキルを追加します
     *
     * @param skills スキルリスト
     * @return this（メソッドチェーン用）
     */
    public SkillTreeBuilder addSkills(List<Skill> skills) {
        for (Skill skill : skills) {
            addSkill(skill);
        }
        return this;
    }

    /**
     * スキルツリーを構築します
     *
     * @return 構築されたスキルツリー
     * @throws IllegalStateException バリデーションエラーがある場合
     */
    public SkillTree build() {
        errors.clear();
        warnings.clear();

        SkillTree tree = new SkillTree(classId);

        if (skills.isEmpty()) {
            warnings.add("スキルが登録されていません");
            return tree;
        }

        // ノードマップを作成
        Map<String, SkillNode> nodeMap = new HashMap<>();
        for (Skill skill : skills.values()) {
            SkillNode node = new SkillNode(skill, null);
            nodeMap.put(skill.getId(), node);
        }

        // 循環参照検出用セット
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();

        // 親子関係を構築
        for (Skill skill : skills.values()) {
            String skillId = skill.getId();
            if (visited.contains(skillId)) {
                continue;
            }

            // 循環参照チェック
            if (hasCycle(skillId, nodeMap, visiting, new HashSet<>())) {
                errors.add("循環参照が検出されました: " + skillId);
                continue;
            }

            SkillNode node = nodeMap.get(skillId);
            if (node == null) {
                continue;
            }

            // ノードを追加（親子関係はSkillTree側で処理）
            addNodeToTree(tree, node, nodeMap);
            visited.add(skillId);
        }

        // 警告・エラーをログ出力
        logMessages();

        return tree;
    }

    /**
     * ノードをツリーに追加します
     *
     * @param tree ツリー
     * @param node ノード
     * @param nodeMap ノードマップ
     */
    private void addNodeToTree(SkillTree tree, SkillNode node, Map<String, SkillNode> nodeMap) {
        Skill skill = node.getSkill();
        Skill.SkillTreeConfig treeConfig = skill.getSkillTree();

        if (treeConfig == null) {
            // ツリー設定がない場合はルートとして追加
            tree.addNode(node);
            return;
        }

        String parentId = treeConfig.getParent();
        if (parentId == null || "none".equalsIgnoreCase(parentId)) {
            // 親がいない場合はルート
            tree.addNode(node);
            return;
        }

        // 親ノードを探す
        SkillNode parentNode = nodeMap.get(parentId);
        if (parentNode == null) {
            // 親が見つからない場合は警告してルートとして追加
            warnings.add("親スキルが見つかりません: " + parentId + " (スキル: " + skill.getId() + ")");
            tree.addNode(node);
            return;
        }

        // 親ノードを先に追加してから、このノードを追加
        // SkillTree.addNodeで親子関係が設定される
        if (!isNodeInTree(tree, parentId)) {
            addNodeToTree(tree, parentNode, nodeMap);
        }
        tree.addNode(node);
    }

    /**
     * ノードがツリーに含まれているかチェックします
     *
     * @param tree ツリー
     * @param skillId スキルID
     * @return 含まれている場合はtrue
     */
    private boolean isNodeInTree(SkillTree tree, String skillId) {
        return tree.getNode(skillId) != null;
    }

    /**
     * 循環参照を検出します
     *
     * @param skillId 現在のスキルID
     * @param nodeMap ノードマップ
     * @param visiting 訪問中のノード
     * @param path 現在のパス
     * @return 循環参照がある場合はtrue
     */
    private boolean hasCycle(String skillId, Map<String, SkillNode> nodeMap,
                            Set<String> visiting, Set<String> path) {
        if (path.contains(skillId)) {
            return true; // 循環検出
        }

        if (visiting.contains(skillId)) {
            return false; // 既にチェック済み
        }

        visiting.add(skillId);
        path.add(skillId);

        SkillNode node = nodeMap.get(skillId);
        if (node != null) {
            Skill.SkillTreeConfig config = node.getSkill().getSkillTree();
            if (config != null) {
                String parentId = config.getParent();
                if (parentId != null && !"none".equalsIgnoreCase(parentId)) {
                    if (hasCycle(parentId, nodeMap, visiting, path)) {
                        return true;
                    }
                }
            }
        }

        path.remove(skillId);
        return false;
    }

    /**
     * エラーがあるかチェックします
     *
     * @return エラーがある場合はtrue
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * 警告があるかチェックします
     *
     * @return 警告がある場合はtrue
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * エラーリストを取得します
     *
     * @return エラーリスト
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * 警告リストを取得します
     *
     * @return 警告リスト
     */
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }

    /**
     * メッセージをログ出力します
     */
    private void logMessages() {
        for (String warning : warnings) {
            logger.warning("[SkillTreeBuilder] " + warning);
        }
        for (String error : errors) {
            logger.severe("[SkillTreeBuilder] " + error);
        }
    }

    /**
     * クラスごとのスキルをフィルタリングしてビルダーを作成します
     *
     * @param classId クラスID
     * @param allSkills 全スキル
     * @param logger ロガー
     * @return ビルダー
     */
    public static SkillTreeBuilder forClass(String classId, Map<String, Skill> allSkills, Logger logger) {
        SkillTreeBuilder builder = new SkillTreeBuilder(classId, logger);

        for (Skill skill : allSkills.values()) {
            List<String> availableClasses = skill.getAvailableClasses();
            // 利用可能クラスが空（全クラス利用可能）または一致
            if (availableClasses.isEmpty() || availableClasses.contains(classId)) {
                builder.addSkill(skill);
            }
        }

        return builder;
    }
}
