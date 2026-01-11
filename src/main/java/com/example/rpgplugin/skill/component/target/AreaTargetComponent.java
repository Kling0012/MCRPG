package com.example.rpgplugin.skill.component.target;

import com.example.rpgplugin.skill.target.AreaShape;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 指定座標範囲のターゲットを選択するコンポーネント
 *
 * <p>YAML設定例:</p>
 * <pre>
 * components:
 *   - type: AREA
 *     area_shape: CIRCLE
 *     radius: 10.0
 *     max_targets: 15
 * </pre>
 *
 * <p>設定パラメータ:</p>
 * <ul>
 *   <li>area_shape: 範囲形状（CIRCLE, RECTANGLE、デフォルト: CIRCLE）</li>
 *   <li>radius: 半径（CIRCLE用、デフォルト: 10.0）</li>
 *   <li>width: 幅（RECTANGLE用、デフォルト: 10.0）</li>
 *   <li>depth: 奥行き（RECTANGLE用、デフォルト: 10.0）</li>
 *   <li>max_targets: 最大ターゲット数（デフォルト: 15）</li>
 *   <li>max_targets_per_level: レベルごとの最大ターゲット増加量（デフォルト: 0）</li>
 *   <li>include_caster: キャスター自身を含めるか（デフォルト: false）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class AreaTargetComponent extends TargetComponent {

    public AreaTargetComponent() {
        super("AREA");
    }

    @Override
    protected List<LivingEntity> selectTargets(LivingEntity caster, int level) {
        List<LivingEntity> targets = new ArrayList<>();

        if (caster == null || !caster.isValid()) {
            return targets;
        }

        AreaShape shape = getAreaShape(AreaShape.CIRCLE);
        int maxTargets = getMaxTargets(level, 15);
        boolean includeCaster = settings.getBoolean("include_caster", false);

        double range;
        if (shape == AreaShape.CIRCLE || shape == AreaShape.SPHERE) {
            range = getRadius(level, 10.0);
        } else {
            // RECTANGLEの場合は大きい方の辺を使用
            double width = settings.getDouble("width", 10.0);
            double depth = settings.getDouble("depth", 10.0);
            range = Math.max(width, depth);
        }

        // 近くのエンティティを取得
        List<LivingEntity> nearby = getNearbyEntities(caster, range);

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
