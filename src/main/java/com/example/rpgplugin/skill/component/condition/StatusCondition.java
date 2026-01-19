package com.example.rpgplugin.skill.component.condition;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

/**
 * ステータス条件
 * <p>ターゲットのステータス値に基づいて条件判定します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class StatusCondition extends ConditionComponent {

    private static final String STAT = "stat"; // health, max_health, food, air, exp
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String PERCENT = "percent"; // パーセント指定

    /**
     * コンストラクタ
     */
    public StatusCondition() {
        super("status");
    }

    @Override
    protected boolean test(LivingEntity caster, int level, LivingEntity target) {
        ComponentSettings settings = getSettings();
        if (settings == null) {
            return true;
        }

        String stat = settings.getString(STAT, "health").toLowerCase();
        double min = settings.getDouble(MIN, 0);
        double max = settings.getDouble(MAX, Double.MAX_VALUE);
        boolean percent = settings.getBoolean(PERCENT, false);

        double value = getStatValue(target, stat, percent);

        return value >= min && value <= max;
    }

    /**
     * ステータス値を取得します
     *
     * @param target  ターゲット
     * @param stat    ステータス名
     * @param percent パーセント指定
     * @return ステータス値
     */
    private double getStatValue(LivingEntity target, String stat, boolean percent) {
        AttributeInstance maxHealthAttr = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;

        return switch (stat) {
            case "health" -> percent ? (target.getHealth() / maxHealth) * 100 : target.getHealth();
            case "max_health" -> maxHealth;
            case "food" -> target instanceof org.bukkit.entity.Player player ? player.getFoodLevel() : 0;
            case "air" -> target.getRemainingAir();
            case "exp" -> target instanceof org.bukkit.entity.Player player ? player.getExp() * 100 : 0;
            case "level" -> target instanceof org.bukkit.entity.Player player ? player.getLevel() : 0;
            default -> target.getHealth();
        };
    }
}
