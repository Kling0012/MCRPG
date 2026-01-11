package com.example.rpgplugin.skill.component.mechanic;

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
                amount = base * target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue() / 100.0;
                break;
            case "percent missing":
                // 失ったHPに対する割合ダメージ
                double maxHp = target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                double missing = maxHp - target.getHealth();
                amount = base * missing / 100.0;
                break;
            case "percent left":
                // 現在HPに対する割合ダメージ
                amount = base * target.getHealth() / 100.0;
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
}
