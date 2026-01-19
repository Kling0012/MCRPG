package com.example.rpgplugin.skill.component.filter;

import com.example.rpgplugin.skill.target.TargetGroupFilter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * ターゲットグループフィルターコンポーネント
 * <p>ターゲットを敵味方グループ（ENEMY/ALLY/BOTH）でフィルタリングします</p>
 *
 * <p>YAML設定例:</p>
 * <pre>
 * - type: target
 *   component_id: SPHERE
 *   children:
 *     - type: filter
 *       component_id: GROUP
 *       settings:
 *         group: ENEMY  # ENEMY, ALLY, BOTH
 * </pre>
 *
 * <p>サポートするグループ:</p>
 * <ul>
 *   <li>ENEMY - 敵対的エンティティのみ（Mob）</li>
 *   <li>ALLY - 味方のみ（プレイヤー、パーティーメンバー）</li>
 *   <li>BOTH - 敵味方両方</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class TargetGroupFilterComponent extends FilterComponent {

    /**
     * グループフィルター
     */
    private TargetGroupFilter groupFilter;

    public TargetGroupFilterComponent() {
        super("group");
        this.groupFilter = TargetGroupFilter.ENEMY; // デフォルトは敵のみ
    }

    /**
     * グループフィルターを設定します
     *
     * @param groupFilter グループフィルター
     */
    public void setGroupFilter(TargetGroupFilter groupFilter) {
        this.groupFilter = groupFilter;
    }

    /**
     * グループフィルターを取得します
     *
     * @return グループフィルター
     */
    public TargetGroupFilter getGroupFilter() {
        return groupFilter;
    }

    @Override
    protected boolean test(LivingEntity caster, int level, LivingEntity target) {
        // 設定からグループを読み込み
        String groupStr = getString("group", "enemy");
        groupFilter = TargetGroupFilter.fromId(groupStr);

        if (groupFilter == null) {
            groupFilter = TargetGroupFilter.ENEMY;
        }

        switch (groupFilter) {
            case ENEMY:
                // 敵対的 = Mobのみ（プレイヤーは除外、PvP設定による）
                return !(target instanceof Player) && !target.equals(caster);

            case ALLY:
                // 味方 = プレイヤーか自分自身
                return target instanceof Player || target.equals(caster);

            case BOTH:
            default:
                // 敵味方両方 = 全て通す
                return true;
        }
    }

    @Override
    public String toString() {
        return "TargetGroupFilterComponent{" +
                "groupFilter=" + groupFilter +
                ", children=" + getChildren().size() +
                '}';
    }
}
