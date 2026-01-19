package com.example.rpgplugin.skill.component.placement;

/**
 * コンポーネントタイプ
 * <p>配置ルールの定義に使用します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public enum ComponentType {

    // トリガー（CASTのみ）
    TRIGGER("trigger", "トリガー", 0x1),

    // ターゲット
    TARGET("target", "ターゲット", 0x2),

    // フィルター（エンティティタイプ、グループ等）
    FILTER("filter", "フィルター", 0x4),

    // 条件（イベント条件を含む）
    CONDITION("condition", "条件", 0x8),

    // メカニック
    MECHANIC("mechanic", "メカニック", 0x10),

    // コスト
    COST("cost", "コスト", 0x20),

    // クールダウン
    COOLDOWN("cooldown", "クールダウン", 0x40);

    private final String id;
    private final String displayName;
    private final int bitMask;

    ComponentType(String id, String displayName, int bitMask) {
        this.id = id;
        this.displayName = displayName;
        this.bitMask = bitMask;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBitMask() {
        return bitMask;
    }
}
