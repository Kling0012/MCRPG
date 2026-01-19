package com.example.rpgplugin.skill.component.mechanic;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

/**
 * ダメージメカニック
 * <p>ターゲットにダメージを与えます</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class DamageMechanic extends MechanicComponent {

    private static final String VALUE = "value";
    private static final String TYPE = "type";
    private static final String TRUE_DAMAGE = "true-damage";

    /**
     * パーセンテージ計算の分母定数
     */
    private static final double PERCENT_DIVISOR = 100.0;

    /**
     * コンストラクタ
     */
    public DamageMechanic() {
        super("damage");
    }

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        String type = getString(TYPE, "damage").toLowerCase();
        double base = parseValues(caster, VALUE, level, 10);
        boolean trueDamage = getBool(TRUE_DAMAGE, false);

        double amount;
        switch (type) {
            case "percent":
            case "multiplier":
                // 最大HPに対する割合ダメージ
                amount = base * getMaxHealth(target) / PERCENT_DIVISOR;
                break;
            case "percent missing":
                // 失ったHPに対する割合ダメージ
                amount = base * getMissingHealth(target) / PERCENT_DIVISOR;
                break;
            case "percent left":
                // 現在HPに対する割合ダメージ
                amount = base * target.getHealth() / PERCENT_DIVISOR;
                break;
            case "damage":
            default:
                // 固定ダメージ
                amount = base;
                break;
        }

        if (amount <= 0) {
            return false;
        }

        if (trueDamage) {
            // 無視ダメージ（防具無視）
            target.damage(amount);
        } else {
            // 通常ダメージ（発動者を攻撃者として設定）
            target.damage(amount, caster);
        }

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
