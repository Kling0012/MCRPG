package com.example.rpgplugin.skill.component.mechanic;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

/**
 * 投射物発射メカニック
 * <p>ターゲットに向けて投射物を発射します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class LaunchMechanic extends MechanicComponent {

    private static final String PROJECTILE = "projectile";
    private static final String SPEED = "speed";
    private static final String SPREAD = "spread";

    /**
     * コンストラクタ
     */
    public LaunchMechanic() {
        super("launch");
    }

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        ComponentSettings settings = getSettings();
        if (settings == null) {
            return false;
        }

        String projectileName = settings.getString(PROJECTILE, "ARROW").toUpperCase();
        double speed = settings.getDouble(SPEED, 2.0);
        double spread = settings.getDouble(SPREAD, 0.1);

        try {
            Class<?> projectileClass = Class.forName("org.bukkit.entity." + projectileName);

            Location eyeLoc = caster.getEyeLocation();
            Vector direction = target.getLocation().add(0, 1, 0).subtract(eyeLoc).toVector();

            if (direction.length() > 0) {
                direction.normalize();
            }

            // 散布を追加
            direction.add(new Vector(
                    (Math.random() - 0.5) * spread,
                    (Math.random() - 0.5) * spread,
                    (Math.random() - 0.5) * spread
            )).normalize().multiply(speed);

            Projectile projectile = (Projectile) caster.getWorld().spawn(
                    eyeLoc.add(direction.clone().normalize().multiply(1)),
                    projectileClass.asSubclass(Projectile.class)
            );
            projectile.setShooter(caster);
            projectile.setVelocity(direction);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
