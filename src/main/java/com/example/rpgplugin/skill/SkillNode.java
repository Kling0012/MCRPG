package com.example.rpgplugin.skill;

import java.util.ArrayList;
import java.util.List;

/**
 * スキルツリーノード
 *
 * <p>スキルツリー内の個々のノードを表します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ノードデータの表現に専念</li>
 *   <li>DRY: 階層構造をシンプルに管理</li>
 *   <li>KISS: シンプルなツリーノード構造</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillNode {

    private final Skill skill;
    private final SkillNode parent;
    private final List<SkillNode> children;

    /**
     * コンストラクタ
     *
     * @param skill スキル
     * @param parent 親ノード
     */
    public SkillNode(Skill skill, SkillNode parent) {
        this.skill = skill;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    /**
     * スキルを取得します
     *
     * @return スキル
     */
    public Skill getSkill() {
        return skill;
    }

    /**
     * 親ノードを取得します
     *
     * @return 親ノード、ルートの場合はnull
     */
    public SkillNode getParent() {
        return parent;
    }

    /**
     * 子ノードのリストを取得します
     *
     * @return 子ノードのリスト（コピー）
     */
    public List<SkillNode> getChildren() {
        return new ArrayList<>(children);
    }

    /**
     * 子ノードを追加します
     *
     * @param child 子ノード
     */
    public void addChild(SkillNode child) {
        if (!children.contains(child)) {
            children.add(child);
        }
    }

    /**
     * ルートノードかチェックします
     *
     * @return ルートの場合はtrue
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * リーフノードかチェックします
     *
     * @return リーフ（子がない）の場合はtrue
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * 深さを取得します
     *
     * @return ルートからの深さ
     */
    public int getDepth() {
        if (isRoot()) {
            return 0;
        }
        return parent.getDepth() + 1;
    }

    @Override
    public String toString() {
        return "SkillNode{" +
                "skill=" + skill.getId() +
                ", children=" + children.size() +
                '}';
    }
}
