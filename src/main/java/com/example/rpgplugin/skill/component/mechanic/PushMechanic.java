package com.example.rpgplugin.skill.component.mechanic;

import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/**
 * ノックバックメカニック
 * <p>ターゲットを吹き飛ばします</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class PushMechanic extends MechanicComponent {

    private static final String SPEED = "speed";
    private static final String VERTICAL = "vertical";

    /**
     * コンストラクタ
     */
    public PushMechanic() {
        super("push");
    }

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        double speed = parseValues(caster, SPEED, level, 1.0);
        double vertical = getNum(VERTICAL, 0.3);

        if (speed <= 0) {
            return false;
        }

        // キャスターからターゲットへの方向ベクトル
        Vector direction = target.getLocation().toVector()
                .subtract(caster.getLocation().toVector())
                .normalize();

        // 水平方向のノックバック
        Vector velocity = direction.multiply(speed);
        // 垂直方向のノックバック
        velocity.setY(vertical);

        target.setVelocity(velocity);

        return true;
    }
}
