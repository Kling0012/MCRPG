package com.example.rpgplugin.skill.component.filter;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * エンティティタイプフィルターコンポーネント
 * <p>ターゲットをエンティティタイプ（SELF/PLAYER/MOB）でフィルタリングします</p>
 *
 * <p>YAML設定例:</p>
 * <pre>
 * - type: target
 *   component_id: SPHERE
 *   children:
 *     - type: filter
 *       component_id: ENTITY_TYPE
 *       settings:
 *         type: PLAYER  # SELF, PLAYER, MOB, ALL
 * </pre>
 *
 * <p>サポートするフィルタータイプ:</p>
 * <ul>
 *   <li>SELF - 発動者自身のみ</li>
 *   <li>PLAYER - プレイヤーのみ</li>
 *   <li>MOB - Mobのみ</li>
 *   <li>ALL - 全て（フィルターなし）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class EntityTypeFilter extends FilterComponent {

    /**
     * エンティティタイプフィルター
     */
    public enum EntityType {
        /**
         * 発動者自身のみ
         */
        SELF("self", "発動者のみ"),

        /**
         * プレイヤーのみ
         */
        PLAYER("player", "プレイヤーのみ"),

        /**
         * Mobのみ
         */
        MOB("mob", "Mobのみ"),

        /**
         * 全て（フィルターなし）
         */
        ALL("all", "全て");

        private final String id;
        private final String displayName;

        EntityType(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        /**
         * IDからEntityTypeを取得します
         *
         * @param id ID
         * @return EntityType、見つからない場合はALL
         */
        public static EntityType fromId(String id) {
            if (id == null) {
                return ALL;
            }
            for (EntityType type : values()) {
                if (type.id.equalsIgnoreCase(id)) {
                    return type;
                }
            }
            return ALL;
        }
    }

    /**
     * フィルタータイプ
     */
    private EntityType filterType;

    public EntityTypeFilter() {
        super("entity_type");
        this.filterType = EntityType.ALL; // デフォルトは全て通す
    }

    /**
     * フィルタータイプを設定します
     *
     * @param filterType フィルタータイプ
     */
    public void setFilterType(EntityType filterType) {
        this.filterType = filterType;
    }

    /**
     * フィルタータイプを取得します
     *
     * @return フィルタータイプ
     */
    public EntityType getFilterType() {
        return filterType;
    }

    @Override
    protected boolean test(LivingEntity caster, int level, LivingEntity target) {
        if (filterType == null) {
            filterType = EntityType.fromId(getString("type", "all"));
        }

        switch (filterType) {
            case SELF:
                return target.equals(caster);

            case PLAYER:
                return target instanceof Player;

            case MOB:
                return !(target instanceof Player);

            case ALL:
            default:
                return true;
        }
    }

    @Override
    public String toString() {
        return "EntityTypeFilter{" +
                "filterType=" + filterType +
                ", children=" + getChildren().size() +
                '}';
    }
}
