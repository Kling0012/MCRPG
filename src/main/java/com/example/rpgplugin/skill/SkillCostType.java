package com.example.rpgplugin.skill;

/**
 * スキルコストタイプ
 *
 * <p>スキル発動に必要なコストの種類を定義します。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public enum SkillCostType {

    /**
     * マナを消費するスキル
     */
    MANA("mana", "マナ"),

    /**
     * HPを消費するスキル
     */
    HP("hp", "HP");

    private final String id;
    private final String displayName;

    SkillCostType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    /**
     * IDからコストタイプを取得します
     *
     * @param id コストタイプID
     * @return コストタイプ、一致しない場合はnull
     */
    public static SkillCostType fromId(String id) {
        if (id == null) {
            return null;
        }
        for (SkillCostType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}
