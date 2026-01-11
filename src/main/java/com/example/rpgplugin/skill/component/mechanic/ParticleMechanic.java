package com.example.rpgplugin.skill.component.mechanic;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

/**
 * パーティクルメカニック
 * <p>ターゲットの位置にパーティクルを表示します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ParticleMechanic extends MechanicComponent {

    private static final String PARTICLE = "particle";
    private static final String COUNT = "count";
    private static final String OFFSET = "offset";
    private static final String SPEED = "speed";

    /**
     * コンストラクタ
     */
    public ParticleMechanic() {
        super("particle");
    }

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        ComponentSettings settings = getSettings();
        if (settings == null) {
            return false;
        }

        String particleName = settings.getString(PARTICLE, "FLAME").toUpperCase().replace(' ', '_');
        int count = settings.getInt(COUNT, 10);
        double offset = settings.getDouble(OFFSET, 0.5);
        double speed = settings.getDouble(SPEED, 0.1);

        try {
            Particle particle = Particle.valueOf(particleName);
            Location loc = target.getLocation().add(0, 1, 0);
            target.getWorld().spawnParticle(particle, loc, count, offset, offset, offset, speed);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
