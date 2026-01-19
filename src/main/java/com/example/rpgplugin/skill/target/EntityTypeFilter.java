package com.example.rpgplugin.skill.target;

/**
 * スキルのターゲットエンティティタイプフィルタ
 *
 * <p>ターゲットにするエンティティの種類を指定します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: エンティティタイプフィルタの定義に専念</li>
 *   <li>YAGNI: 必要なフィルタのみ定義</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public enum EntityTypeFilter {

    /**
     * プレイヤーのみ
     */
    PLAYER_ONLY("player_only", "プレイヤーのみ"),

    /**
     * Mobのみ（プレイヤー以外）
     */
    MOB_ONLY("mob_only", "Mobのみ"),

    /**
     * 全てのエンティティ（プレイヤーとMob）
     */
    ALL("all", "全て");

    private final String id;
    private final String displayName;

    EntityTypeFilter(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    /**
     * IDから対応するEntityTypeFilterを取得します
     *
     * @param id フィルタID
     * @return 対応するEntityTypeFilter、見つからない場合はALL
     */
    public static EntityTypeFilter fromId(String id) {
        if (id == null) {
            return ALL;
        }
        for (EntityTypeFilter filter : values()) {
            if (filter.id.equalsIgnoreCase(id)) {
                return filter;
            }
        }
        return ALL;
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
}
