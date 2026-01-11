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

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        int ticks;
        String secondsKey = SECONDS; // Use the constant directly

        if (getSettings().has(secondsKey)) {
            int seconds = getInt(SECONDS, 3);
            ticks = seconds * 20;
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
