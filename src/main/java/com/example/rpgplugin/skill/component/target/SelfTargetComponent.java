package com.example.rpgplugin.skill.component.target;

import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 自分自身をターゲットにするコンポーネント
 *
 * <p>YAML設定例:</p>
 * <pre>
 * components:
 *   - type: SELF
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SelfTargetComponent extends TargetComponent {

    public SelfTargetComponent() {
        super("SELF");
    }

    @Override
    protected List<LivingEntity> selectTargets(LivingEntity caster, int level) {
        List<LivingEntity> targets = new ArrayList<>();
        if (caster != null && caster.isValid()) {
            targets.add(caster);
        }
        return targets;
    }
}
