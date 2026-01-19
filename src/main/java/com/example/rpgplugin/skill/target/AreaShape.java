package com.example.rpgplugin.skill.target;

import java.util.Optional;

/**
 * スキルエフェクトの範囲形状を表す列挙型
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: 範囲形状の定義に専念</li>
 *   <li>YAGNI: 必要な形状のみ定義</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public enum AreaShape {

    /**
     * 単体ターゲット（最も近いMOB1体）
     */
    SINGLE("single", "単体"),

    /**
     * 前方扇状範囲
     */
    CONE("cone", "扇状"),

    /**
     * 前方四角形範囲
     */
    RECT("rect", "四角形"),

    /**
     * 周囲円形範囲
     */
    CIRCLE("circle", "円形"),

    /**
     * 球形範囲（SkillAPI参考）
     */
    SPHERE("sphere", "球形");

    private final String id;
    private final String displayName;

    AreaShape(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    /**
     * IDから対応するAreaShapeを取得します
     *
     * @param id 形状ID
     * @return 対応するAreaShape、見つからない場合は空のOptional
     */
    public static Optional<AreaShape> fromId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        for (AreaShape shape : values()) {
            if (shape.id.equalsIgnoreCase(id)) {
                return Optional.of(shape);
            }
        }
        return Optional.empty();
    }

    /**
     * IDから対応するAreaShapeを取得します（レガシーメソッド）
     *
     * @param id 形状ID
     * @return 対応するAreaShape、見つからない場合はnull
     * @deprecated {@link #fromId(String)}を使用してください
     */
    @Deprecated
    public static AreaShape fromIdOrNull(String id) {
        return fromId(id).orElse(null);
    }

    /**
     * 形状IDを取得します
     *
     * @return 形状ID
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
