package com.example.rpgplugin.skill.component.condition;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

/**
 * HP条件コンポーネント
 * <p>ターゲットのHPに基づいてフィルタリングします</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class HealthCondition extends ConditionComponent {

    private static final String TYPE = "type";
    private static final String MIN = "min-value";
    private static final String MAX = "max-value";

    /**
     * コンストラクタ
     */
    public HealthCondition() {
        super("health");
    }

    @Override
    protected boolean test(LivingEntity caster, int level, LivingEntity target) {
        String type = getString(TYPE, "value").toLowerCase();
        double min = parseValues(caster, MIN, level, 0);
        double max = parseValues(caster, MAX, level, Double.MAX_VALUE);

        double value;
        switch (type) {
            case "percent":
                AttributeInstance maxHealthAttr = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;
                value = (target.getHealth() / maxHealth) * 100;
                break;
            case "difference":
                value = target.getHealth() - caster.getHealth();
                break;
            case "difference percent":
                double casterHp = caster.getHealth();
                if (casterHp == 0) {
                    return false;
                }
                value = ((target.getHealth() - casterHp) / casterHp) * 100;
                break;
            case "value":
            default:
                value = target.getHealth();
                break;
        }

        return value >= min && value <= max;
    }
}
