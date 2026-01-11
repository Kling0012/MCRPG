package com.example.rpgplugin.skill.component.target;

import org.bukkit.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * コーン状範囲のターゲットを選択するコンポーネント
 *
 * <p>YAML設定例:</p>
 * <pre>
 * components:
 *   - type: CONE
 *     angle: 90.0
 *     range: 10.0
 *     max_targets: 5
 * </pre>
 *
 * <p>設定パラメータ:</p>
 * <ul>
 *   <li>angle: コーンの角度（度数法、デフォルト: 90.0）</li>
 *   <li>angle_per_level: レベルごとの角度増加量（デフォルト: 0.0）</li>
 *   <li>range: 範囲（デフォルト: 10.0）</li>
 *   <li>range_per_level: レベルごとの範囲増加量（デフォルト: 0.0）</li>
 *   <li>max_targets: 最大ターゲット数（デフォルト: 5）</li>
 *   <li>max_targets_per_level: レベルごとの最大ターゲット増加量（デフォルト: 0）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ConeTargetComponent extends TargetComponent {

    public ConeTargetComponent() {
        super("CONE");
    }

    @Override
    protected List<LivingEntity> selectTargets(LivingEntity caster, int level) {
        List<LivingEntity> targets = new ArrayList<>();

        if (caster == null || !caster.isValid()) {
            return targets;
        }

        double angle = getAngle(level, 90.0);
        double range = getRange(level, 10.0);
        int maxTargets = getMaxTargets(level, 5);

        // 近くのエンティティを取得
        List<LivingEntity> nearby = getNearbyEntities(caster, range);

        // 視線方向を取得
        Vector direction = caster.getEyeLocation().getDirection();
        Location casterLoc = caster.getEyeLocation();

        // コーン内のエンティティをフィルタ
        for (LivingEntity entity : nearby) {
            if (entity.equals(caster)) {
                continue;
            }

            Vector toEntity = entity.getLocation().toVector().subtract(casterLoc.toVector());
            double distance = toEntity.length();

            if (distance > range) {
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
