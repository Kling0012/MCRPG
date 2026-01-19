package com.example.rpgplugin.skill.component.mechanic;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * 爆発メカニック
 * <p>ターゲットの位置で爆発を起こします</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExplosionMechanic extends MechanicComponent {

    private static final String POWER = "power";
    private static final String FIRE = "fire";
    private static final String DAMAGE = "damage";

    /**
     * コンストラクタ
     */
    public ExplosionMechanic() {
        super("explosion");
    }

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        ComponentSettings settings = getSettings();
        if (settings == null) {
            return false;
        }

        double power = settings.getDouble(POWER, 3.0);
        boolean fire = settings.getBoolean(FIRE, false);
        boolean damage = settings.getBoolean(DAMAGE, true);

        Location loc = target.getLocation();
        target.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(),
                (float) power, fire, damage);
        return true;
    }
}
