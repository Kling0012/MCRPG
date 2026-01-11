package com.example.rpgplugin.skill.component.target;

import org.bukkit.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * 直線状範囲のターゲットを選択するコンポーネント
 *
 * <p>YAML設定例:</p>
 * <pre>
 * components:
 *   - type: LINE
 *     length: 15.0
 *     width: 2.0
 *     max_targets: 5
 * </pre>
 *
 * <p>設定パラメータ:</p>
 * <ul>
 *   <li>length: 直線の長さ（デフォルト: 15.0）</li>
 *   <li>length_per_level: レベルごとの長さ増加量（デフォルト: 0.0）</li>
 *   <li>width: 直線の幅（デフォルト: 2.0）</li>
 *   <li>width_per_level: レベルごとの幅増加量（デフォルト: 0.0）</li>
 *   <li>max_targets: 最大ターゲット数（デフォルト: 5）</li>
 *   <li>max_targets_per_level: レベルごとの最大ターゲット増加量（デフォルト: 0）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class LineTargetComponent extends TargetComponent {

    public LineTargetComponent() {
        super("LINE");
    }

    @Override
    protected List<LivingEntity> selectTargets(LivingEntity caster, int level) {
        List<LivingEntity> targets = new ArrayList<>();

        if (caster == null || !caster.isValid()) {
            return targets;
        }

        double length = getLength(level, 15.0);
        double width = getWidth(level, 2.0);
        int maxTargets = getMaxTargets(level, 5);

        // 視線方向に基づいて直線上のエンティティを検索
        Location casterLoc = caster.getEyeLocation();
        Vector direction = casterLoc.getDirection().normalize();

        // 範囲内のエンティティを取得（長さ+幅の余裕を持つ）
        double searchRange = length + width;
        List<LivingEntity> nearby = getNearbyEntities(caster, searchRange);

        for (LivingEntity entity : nearby) {
            if (entity.equals(caster)) {
                continue;
            }

            // キャスターからエンティティへのベクトル
            Vector toEntity = entity.getLocation().toVector().subtract(casterLoc.toVector());

            // 視線方向への投影
            double projection = toEntity.dot(direction);

            // 直線の長さ範囲内かチェック
            if (projection < 0 || projection > length) {
                continue;
            }

            // 直線からの距離を計算
            Vector projectedPoint = direction.clone().multiply(projection);
            Vector perpendicular = toEntity.clone().subtract(projectedPoint);
            double distanceFromLine = perpendicular.length();

            // 幅の範囲内かチェック
            if (distanceFromLine <= width) {
                targets.add(entity);
            }
        }

        // 距離順にソート
        targets.sort((a, b) -> {
            double distA = caster.getLocation().distance(a.getLocation());
            double distB = caster.getLocation().distance(b.getLocation());
            return Double.compare(distA, distB);
        });

        // 最大ターゲット数で制限
        return limitTargets(targets, maxTargets);
    }
}
