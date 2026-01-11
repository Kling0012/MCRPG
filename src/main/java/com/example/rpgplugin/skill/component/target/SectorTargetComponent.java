package com.example.rpgplugin.skill.component.target;

import org.bukkit.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * 扇形範囲のターゲットを選択するコンポーネント
 *
 * <p>ConeTargetComponentと似ていますが、より細かい設定が可能です。</p>
 *
 * <p>YAML設定例:</p>
 * <pre>
 * components:
 *   - type: SECTOR
 *     angle: 60.0
 *     radius: 8.0
 *     max_targets: 8
 * </pre>
 *
 * <p>設定パラメータ:</p>
 * <ul>
 *   <li>angle: 扇形の角度（度数法、デフォルト: 60.0）</li>
 *   <li>angle_per_level: レベルごとの角度増加量（デフォルト: 0.0）</li>
 *   <li>radius: 半径（デフォルト: 8.0）</li>
 *   <li>radius_per_level: レベルごとの半径増加量（デフォルト: 0.0）</li>
 *   <li>max_targets: 最大ターゲット数（デフォルト: 8）</li>
 *   <li>max_targets_per_level: レベルごとの最大ターゲット増加量（デフォルト: 0）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SectorTargetComponent extends TargetComponent {

    public SectorTargetComponent() {
        super("SECTOR");
    }

    @Override
    protected List<LivingEntity> selectTargets(LivingEntity caster, int level) {
        List<LivingEntity> targets = new ArrayList<>();

        if (caster == null || !caster.isValid()) {
            return targets;
        }

        double angle = getAngle(level, 60.0);
        double radius = getRadius(level, 8.0);
        int maxTargets = getMaxTargets(level, 8);

        // 近くのエンティティを取得
        List<LivingEntity> nearby = getNearbyEntities(caster, radius);

        // 視線方向を取得
        Vector direction = caster.getEyeLocation().getDirection();
        Location casterLoc = caster.getEyeLocation();

        // 扇形内のエンティティをフィルタ
        for (LivingEntity entity : nearby) {
            if (entity.equals(caster)) {
                continue;
            }

            Vector toEntity = entity.getLocation().toVector().subtract(casterLoc.toVector());
            double distance = toEntity.length();

            if (distance > radius) {
                continue;
            }

            // 角度を計算
            toEntity.normalize();
            double angleDiff = Math.toDegrees(Math.acos(
                    Math.max(-1.0, Math.min(1.0, direction.dot(toEntity)))
            ));

            if (angleDiff <= angle / 2.0) {
                targets.add(entity);
            }
        }

        // 最大ターゲット数で制限
        return limitTargets(targets, maxTargets);
    }
}
