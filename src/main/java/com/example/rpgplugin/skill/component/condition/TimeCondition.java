package com.example.rpgplugin.skill.component.condition;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.entity.LivingEntity;

/**
 * 時間条件
 * <p>特定の時間帯の場合のみ効果を適用します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class TimeCondition extends ConditionComponent {

    private static final String TIME = "time"; // day, night, dawn, dusk

    /**
     * コンストラクタ
     */
    public TimeCondition() {
        super("time");
    }

    @Override
    protected boolean test(LivingEntity caster, int level, LivingEntity target) {
        ComponentSettings settings = getSettings();
        if (settings == null) {
            return true;
        }

        String time = settings.getString(TIME, "any").toLowerCase();
        if ("any".equals(time)) {
            return true;
        }

        long worldTime = caster.getWorld().getTime();

        return switch (time) {
            case "day" -> worldTime >= 0 && worldTime < 12300;
            case "night" -> worldTime >= 12300 && worldTime < 24000;
            case "dawn", "morning" -> worldTime >= 0 && worldTime < 6000;
            case "noon", "midday" -> worldTime >= 6000 && worldTime < 12000;
            case "dusk", "evening" -> worldTime >= 12000 && worldTime < 14000;
            case "midnight" -> worldTime >= 18000 && worldTime < 19000;
            default -> true;
        };
    }
}
