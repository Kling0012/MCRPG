package com.example.rpgplugin.skill.target;

import java.util.Optional;

/**
 * スキルのターゲットグループフィルタ（敵味方フィルタ）
 *
 * <p>SkillAPIのgroup設定を参考に実装。</p>
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: グループフィルタの定義に専念</li>
 *   <li>YAGNI: 必要なフィルタのみ定義</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public enum TargetGroupFilter {

    /**
     * 敵対的エンティティのみ
     */
    ENEMY("enemy", "敵のみ"),

    /**
     * 味方のみ
     */
    ALLY("ally", "味方のみ"),

    /**
     * 敵味方両方
     */
    BOTH("both", "敵味方両方");

    private final String id;
    private final String displayName;

    TargetGroupFilter(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    /**
     * IDから対応するTargetGroupFilterを取得します
     *
     * @param id グループフィルタID
     * @return 対応するTargetGroupFilter、見つからない場合はデフォルト(ENEMY)
     */
    public static TargetGroupFilter fromId(String id) {
        if (id == null) {
            return ENEMY;
        }
        for (TargetGroupFilter filter : values()) {
            if (filter.id.equalsIgnoreCase(id)) {
                return filter;
            }
        }
        return ENEMY;
    }

    /**
     * IDから対応するTargetGroupFilterを取得します
     *
     * @param id グループフィルタID
     * @return 対応するTargetGroupFilter
     */
    public static Optional<TargetGroupFilter> fromIdOrEmpty(String id) {
        if (id == null) {
            return Optional.empty();
        }
        for (TargetGroupFilter filter : values()) {
            if (filter.id.equalsIgnoreCase(id)) {
                return Optional.of(filter);
            }
        }
        return Optional.empty();
    }

    /**
     * グループフィルタIDを取得します
     *
     * @return グループフィルタID
     */
    public String getId() {
        return id;
    }

    /**
     * 表示名を取得します
     *
     * @return 表示名
     */
    public String getDisplayName() {
        return displayName;
    }
}
