package com.example.rpgplugin.skill.component.mechanic;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/**
 * 稲妻メカニック
 * <p>ターゲットの位置に稲妻を落とします</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class LightningMechanic extends MechanicComponent {

    private static final String FORWARD = "forward";
    private static final String RIGHT = "right";
    private static final String DAMAGE = "damage";

    private static final Vector UP = new Vector(0, 1, 0);

    /**
     * コンストラクタ
     */
    public LightningMechanic() {
        super("lightning");
    }

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        ComponentSettings settings = getSettings();
        if (settings == null) {
            return false;
        }

        boolean damage = settings.getBoolean(DAMAGE, true);
        double forward = settings.getDouble(FORWARD, 0);
        double right = settings.getDouble(RIGHT, 0);

        Vector dir = target.getLocation().getDirection().setY(0).normalize();
        Vector nor = dir.clone().crossProduct(UP);
        Location loc = target.getLocation().add(dir.multiply(forward).add(nor.multiply(right)));

        if (damage) {
            target.getWorld().strikeLightning(loc);
        } else {
            target.getWorld().strikeLightningEffect(loc);
        }
        return true;
    }
}
