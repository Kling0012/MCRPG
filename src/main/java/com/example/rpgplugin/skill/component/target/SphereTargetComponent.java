package com.example.rpgplugin.skill.component.target;

import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 球形範囲のターゲットを選択するコンポーネント
 *
 * <p>YAML設定例:</p>
 * <pre>
 * components:
 *   - type: SPHERE
 *     radius: 5.0
 *     max_targets: 10
 * </pre>
 *
 * <p>設定パラメータ:</p>
 * <ul>
 *   <li>radius: 半径（デフォルト: 5.0）</li>
 *   <li>radius_per_level: レベルごとの半径増加量（デフォルト: 0.0）</li>
 *   <li>max_targets: 最大ターゲット数（デフォルト: 10）</li>
 *   <li>max_targets_per_level: レベルごとの最大ターゲット増加量（デフォルト: 0）</li>
 *   <li>include_caster: キャスター自身を含めるか（デフォルト: false）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SphereTargetComponent extends TargetComponent {

    public SphereTargetComponent() {
        super("SPHERE");
    }

    @Override
    protected List<LivingEntity> selectTargets(LivingEntity caster, int level) {
        List<LivingEntity> targets = new ArrayList<>();

        if (caster == null || !caster.isValid()) {
            return targets;
        }

        double radius = getRadius(level, 5.0);
        int maxTargets = getMaxTargets(level, 10);
        boolean includeCaster = settings.getBoolean("include_caster", false);

        // 近くのエンティティを取得
        List<LivingEntity> nearby = getNearbyEntities(caster, radius);

        for (LivingEntity entity : nearby) {
            if (entity.equals(caster) && !includeCaster) {
                continue;
            }
            targets.add(entity);
        }

        // 最大ターゲット数で制限
        return limitTargets(targets, maxTargets);
    }
}
