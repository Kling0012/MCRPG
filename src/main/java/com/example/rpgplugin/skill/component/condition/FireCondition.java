package com.example.rpgplugin.skill.component.condition;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.entity.LivingEntity;

/**
 * 炎上条件
 * <p>ターゲットが炎上している場合のみ効果を適用します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class FireCondition extends ConditionComponent {

    private static final String TICKS = "ticks"; // 最小炎上ティック数

    /**
     * コンストラクタ
     */
    public FireCondition() {
        super("fire");
    }

    @Override
    protected boolean test(LivingEntity caster, int level, LivingEntity target) {
        ComponentSettings settings = getSettings();
        if (settings == null) {
            return target.getFireTicks() > 0;
        }

        int minTicks = settings.getInt(TICKS, 0);
        return target.getFireTicks() > minTicks;
    }
}
