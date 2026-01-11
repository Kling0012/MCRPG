package com.example.rpgplugin.skill.component.target;

import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 単体ターゲットを選択するコンポーネント
 *
 * <p>YAML設定例:</p>
 * <pre>
 * components:
 *   - type: SINGLE
 *     range: 10.0
 *     select_nearest: true
 *     target_self: false
 * </pre>
 *
 * <p>設定パラメータ:</p>
 * <ul>
 *   <li>range: ターゲット検索範囲（デフォルト: 10.0）</li>
 *   <li>range_per_level: レベルごとの範囲増加量（デフォルト: 0.0）</li>
 *   <li>select_nearest: 最も近いターゲットを選択するか（デフォルト: true）</li>
 *   <li>target_self: 自分自身をターゲットにするか（デフォルト: false）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SingleTargetComponent extends TargetComponent {

    public SingleTargetComponent() {
        super("SINGLE");
    }

    @Override
    protected List<LivingEntity> selectTargets(LivingEntity caster, int level) {
        List<LivingEntity> targets = new ArrayList<>();

        if (caster == null || !caster.isValid()) {
            return targets;
        }

        // 自分自身をターゲットにする場合
        boolean targetSelf = settings.getBoolean("target_self", false);
        if (targetSelf) {
            targets.add(caster);
            return targets;
        }

        // 近くのエンティティを取得
        double range = getRange(level, 10.0);
        List<LivingEntity> nearby = getNearbyEntities(caster, range);

        // 敵対的エンティティのみにフィルタ
        boolean hostileOnly = settings.getBoolean("hostile_only", true);
        if (hostileOnly) {
            nearby = filterHostile(caster, nearby);
        }

        if (nearby.isEmpty()) {
            return targets;
        }

        // 最も近いターゲットを選択
        boolean selectNearest = settings.getBoolean("select_nearest", true);
        if (selectNearest) {
            LivingEntity nearest = getNearestEntity(caster, nearby);
            if (nearest != null) {
                targets.add(nearest);
            }
        } else {
            // ランダムに1体選択
            if (!nearby.isEmpty()) {
                int index = (int) (Math.random() * nearby.size());
                targets.add(nearby.get(index));
            }
        }

        return targets;
    }
}
