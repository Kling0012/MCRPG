package com.example.rpgplugin.skill.component.mechanic;

import org.bukkit.entity.LivingEntity;

/**
 * 燃焼メカニック
 * <p>ターゲットを炎上させます</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class FireMechanic extends MechanicComponent {

    private static final String TICKS = "ticks";
    private static final String SECONDS = "seconds";

    /**
     * コンストラクタ
     */
    public FireMechanic() {
        super("fire");
    }

    /**
     * 秒からティックへの変換定数
     */
    private static final int TICKS_PER_SECOND = 20;

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        int ticks;

        if (getSettings().has(SECONDS)) {
            int seconds = getInt(SECONDS, 3);
            ticks = seconds * TICKS_PER_SECOND;
        } else {
            ticks = getInt(TICKS, 60);
        }

        if (ticks <= 0) {
            return false;
        }

        target.setFireTicks(ticks);
        return true;
    }
}
