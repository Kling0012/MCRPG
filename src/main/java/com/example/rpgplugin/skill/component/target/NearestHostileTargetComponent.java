package com.example.rpgplugin.skill.component.target;

import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 最寄りの敵対的エンティティをターゲットにするコンポーネント
 *
 * <p>YAML設定例:</p>
 * <pre>
 * components:
 *   - type: NEAREST_HOSTILE
 *     range: 15.0
 * </pre>
 *
 * <p>設定パラメータ:</p>
 * <ul>
 *   <li>range: 検索範囲（デフォルト: 15.0）</li>
 *   <li>range_per_level: レベルごとの範囲増加量（デフォルト: 0.0）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class NearestHostileTargetComponent extends TargetComponent {

    public NearestHostileTargetComponent() {
        super("NEAREST_HOSTILE");
    }

    @Override
    protected List<LivingEntity> selectTargets(LivingEntity caster, int level) {
        List<LivingEntity> targets = new ArrayList<>();

        if (caster == null || !caster.isValid()) {
            return targets;
        }

        double range = getRange(level, 15.0);

        // 近くのエンティティを取得
        List<LivingEntity> nearby = getNearbyEntities(caster, range);

        // 敵対的エンティティにフィルタ
        List<LivingEntity> hostile = filterHostile(caster, nearby);

        if (hostile.isEmpty()) {
            return targets;
        }

        // 最も近い敵対的エンティティを選択
        LivingEntity nearest = getNearestEntity(caster, hostile);
        if (nearest != null) {
            targets.add(nearest);
        }

        return targets;
    }
}
