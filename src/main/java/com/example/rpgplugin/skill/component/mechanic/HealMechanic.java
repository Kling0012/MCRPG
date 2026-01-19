package com.example.rpgplugin.skill.component.mechanic;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

/**
 * 回復メカニック
 * <p>ターゲットのHPを回復します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class HealMechanic extends MechanicComponent {

    private static final String VALUE = "value";
    private static final String TYPE = "type";

    /**
     * パーセンテージ計算の分母定数
     */
    private static final double PERCENT_DIVISOR = 100.0;

    /**
     * コンストラクタ
     */
    public HealMechanic() {
        super("heal");
    }

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        String type = getString(TYPE, "value").toLowerCase();
        double base = parseValues(caster, VALUE, level, 10);

        double maxHp = getMaxHealth(target);
        double amount;
        switch (type) {
            case "percent":
                // 最大HPに対する割合回復
                amount = base * maxHp / PERCENT_DIVISOR;
                break;
            case "percent missing":
                // 失ったHPに対する割合回復
                double missing = getMissingHealth(target);
                amount = base * missing / PERCENT_DIVISOR;
                break;
            case "value":
            default:
                // 固定値回復
                amount = base;
                break;
        }

        if (amount <= 0) {
            return false;
        }

        double newHealth = Math.min(target.getHealth() + amount, maxHp);
        target.setHealth(newHealth);

        return true;
    }

    /**
     * ターゲットの最大HPを取得します
     */
    private double getMaxHealth(LivingEntity target) {
        return target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    }

    /**
     * ターゲットの失ったHPを取得します
     */
    private double getMissingHealth(LivingEntity target) {
        return getMaxHealth(target) - target.getHealth();
    }
}
