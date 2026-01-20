package com.example.rpgplugin.skill;

import com.example.rpgplugin.model.skill.SkillTreeConfig;
import com.example.rpgplugin.model.skill.UnlockRequirement;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * スキルツリー
 *
 * <p>スキルの階層構造を管理し、習得要件チェックやスキルポイント管理を行います。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキルツリーの管理に専念</li>
 *   <li>DRY: 習得要件チェックロジックを一元管理</li>
 *   <li>Composite: ノードの階層構造を管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillTree {

    private final String classId;
    private final Map<String, SkillNode> nodes;
    private final List<SkillNode> rootNodes;

    /**
     * 親スキルがないことを示す定数
     */
    private static final String NO_PARENT = "none";

    /**
     * デフォルトのスキルポイントコスト
     */
    private static final int DEFAULT_COST = 1;

    /**
     * コンストラクタ
     *
     * @param classId クラスID
     */
    public SkillTree(String classId) {
        this.classId = classId;
        this.nodes = new HashMap<>();
        this.rootNodes = new ArrayList<>();
    }

    /**
     * ノードを追加します
     *
     * @param node 追加するノード
     */
    public void addNode(SkillNode node) {
        nodes.put(node.getSkill().getId(), node);

        if (node.isRoot()) {
            rootNodes.add(node);
        } else {
            SkillNode parent = node.getParent();
            if (parent != null) {
                parent.addChild(node);
            }
        }
    }

    /**
     * ノードを取得します
     *
     * @param skillId スキルID
     * @return ノード、見つからない場合はnull
     */
    public SkillNode getNode(String skillId) {
        return nodes.get(skillId);
    }

    /**
     * 全ノードを取得します
     *
     * @return 全ノードのマップ（コピー）
     */
    public Map<String, SkillNode> getAllNodes() {
        return new HashMap<>(nodes);
    }

    /**
     * ルートノードを取得します
     *
     * @return ルートノードのリスト（コピー）
     */
    public List<SkillNode> getRootNodes() {
        return new ArrayList<>(rootNodes);
    }

    /**
     * クラスIDを取得します
     *
     * @return クラスID
     */
    public String getClassId() {
        return classId;
    }

    /**
     * 習得可能かチェックします
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @param playerLevel プレイヤーレベル
     * @param stats ステータス値のマップ
     * @return 習得可能な場合はtrue
     */
    public boolean canAcquire(Player player, String skillId, int playerLevel, Map<com.example.rpgplugin.stats.Stat, Double> stats) {
        SkillNode node = getNode(skillId);
        if (node == null) {
            return false;
        }

        Skill skill = node.getSkill();
        SkillTreeConfig treeConfig = skill.getSkillTree();
        if (treeConfig == null) {
            return true;
        }

        // 親スキルの習得チェック
        if (!NO_PARENT.equalsIgnoreCase(treeConfig.getParent())) {
            SkillNode parent = getNode(treeConfig.getParent());
            if (parent == null) {
                return false;
            }

            // 親スキルが習得されているかチェックは、呼び出し元で行う必要があります
            // ここではプレイヤーデータへのアクセスがないため、親ノードの存在チェックのみ
        }

        // 習得要件チェック
        for (UnlockRequirement requirement : treeConfig.getUnlockRequirements()) {
            if (!meetsRequirement(requirement, playerLevel, stats)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 習得要件を満たしているかチェックします
     *
     * @param requirement 習得要件
     * @param playerLevel プレイヤーレベル
     * @param stats ステータス値のマップ
     * @return 要件を満たしている場合はtrue
     */
    private boolean meetsRequirement(UnlockRequirement requirement, int playerLevel,
                                     Map<com.example.rpgplugin.stats.Stat, Double> stats) {
        String type = requirement.getType();

        switch (type.toLowerCase()) {
            case "level":
                return playerLevel >= (int) requirement.getValue();

            case "stat":
                com.example.rpgplugin.stats.Stat stat = requirement.getStat();
                if (stat == null) {
                    return false;
                }
                double statValue = stats.getOrDefault(stat, 0.0);
                return statValue >= requirement.getValue();

            default:
                return false;
        }
    }

    /**
     * スキル習得にかかるコストを取得します
     *
     * @param skillId スキルID
     * @return スキルポイントコスト
     */
    public int getCost(String skillId) {
        SkillNode node = getNode(skillId);
        if (node == null) {
            return 0;
        }

        SkillTreeConfig treeConfig = node.getSkill().getSkillTree();
        if (treeConfig == null) {
            return DEFAULT_COST;
        }

        return treeConfig.getCost();
    }

    /**
     * 親スキルを取得します
     *
     * @param skillId スキルID
     * @return 親スキルID、親がない場合はnull
     */
    public String getParentSkillId(String skillId) {
        SkillNode node = getNode(skillId);
        if (node == null || node.isRoot()) {
            return null;
        }

        SkillTreeConfig treeConfig = node.getSkill().getSkillTree();
        if (treeConfig == null) {
            return null;
        }

        String parentId = treeConfig.getParent();
        return NO_PARENT.equalsIgnoreCase(parentId) ? null : parentId;
    }

    /**
     * 子スキルのリストを取得します
     *
     * @param skillId スキルID
     * @return 子スキルのノードリスト
     */
    public List<SkillNode> getChildren(String skillId) {
        SkillNode node = getNode(skillId);
        if (node == null) {
            return Collections.emptyList();
        }

        return node.getChildren();
    }

    /**
     * スキルがルートかチェックします
     *
     * @param skillId スキルID
     * @return ルートの場合はtrue
     */
    public boolean isRootSkill(String skillId) {
        SkillNode node = getNode(skillId);
        return node != null && node.isRoot();
    }

    /**
     * ツリーの深さを取得します
     *
     * @return 最大深さ
     */
    public int getMaxDepth() {
        int maxDepth = 0;
        for (SkillNode node : nodes.values()) {
            int depth = node.getDepth();
            if (depth > maxDepth) {
                maxDepth = depth;
            }
        }
        return maxDepth;
    }

    /**
     * ノード数を取得します
     *
     * @return ノード数
     */
    public int getNodeCount() {
        return nodes.size();
    }

    @Override
    public String toString() {
        return "SkillTree{" +
                "classId='" + classId + '\'' +
                ", nodeCount=" + nodes.size() +
                ", rootCount=" + rootNodes.size() +
                '}';
    }
}
