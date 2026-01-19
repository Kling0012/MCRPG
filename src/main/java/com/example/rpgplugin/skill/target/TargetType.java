package com.example.rpgplugin.skill.target;

import java.util.Optional;

/**
 * スキルのターゲット選択方式を表す列挙型
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ターゲット選択方式の定義に専念</li>
 *   <li>YAGNI: 必要な選択方式のみ定義</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public enum TargetType {

    /**
     * 自分自身（Player）
     */
    SELF("self", "自分自身"),

    /**
     * 自分と最も近いターゲット一人
     */
    SELF_PLUS_ONE("self_plus_one", "自分と他人一人"),

    /**
     * 最も近い敵対MOB
     */
    NEAREST_HOSTILE("nearest_hostile", "最も近いMob"),

    /**
     * 最も近いプレイヤー
     */
    NEAREST_PLAYER("nearest_player", "最も近いプレイヤー"),

    /**
     * 最も近いエンティティ（プレイヤー含む）
     */
    NEAREST_ENTITY("nearest_entity", "最も近いエンティティ"),

    /**
     * 範囲内の全エンティティ（自分を含む）
     */
    AREA_SELF("area_self", "自分を含む範囲"),

    /**
     * 範囲内の全エンティティ（自分を含まない）
     */
    AREA_OTHERS("area_others", "自分を含まない範囲"),

    /**
     * 外部から指定されたターゲット
     */
    EXTERNAL("external", "外部指定"),

    /**
     * 直線上のターゲット（SkillAPI参考）
     */
    LINE("line", "直線"),

    /**
     * 扇状範囲のターゲット（SkillAPI参考）
     */
    CONE("cone", "扇状"),

    /**
     * 視線上のターゲット（SkillAPI参考）
     */
    LOOKING("looking", "視線上"),

    /**
     * 球形範囲のターゲット（SkillAPI参考）
     */
    SPHERE("sphere", "球形範囲");

    private final String id;
    private final String displayName;

    TargetType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    /**
     * IDから対応するTargetTypeを取得します
     *
     * @param id ターゲットタイプID
     * @return 対応するTargetType、見つからない場合は空のOptional
     */
    public static Optional<TargetType> fromId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        for (TargetType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    /**
     * IDから対応するTargetTypeを取得します（レガシーメソッド）
     *
     * @param id ターゲットタイプID
     * @return 対応するTargetType、見つからない場合はnull
     * @deprecated {@link #fromId(String)}を使用してください
     */
    @Deprecated
    public static TargetType fromIdOrNull(String id) {
        return fromId(id).orElse(null);
    }

    /**
     * ターゲットタイプIDを取得します
     *
     * @return ターゲットタイプID
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

    /**
     * 範囲系ターゲットタイプか判定します
     *
     * @return 範囲系の場合はtrue
     */
    public boolean isAreaType() {
        return this == AREA_SELF || this == AREA_OTHERS
                || this == CONE || this == SPHERE;
    }

    /**
     * 単体ターゲットタイプか判定します
     *
     * @return 単体ターゲットの場合はtrue
     */
    public boolean isSingleType() {
        return this == SELF || this == SELF_PLUS_ONE || this == NEAREST_HOSTILE
                || this == NEAREST_PLAYER || this == NEAREST_ENTITY;
    }

    /**
     * 複数ターゲットタイプか判定します
     *
     * @return 複数ターゲットの場合はtrue
     */
    public boolean isMultiTargetType() {
        return this == SELF_PLUS_ONE || this == AREA_SELF || this == AREA_OTHERS;
    }
}
