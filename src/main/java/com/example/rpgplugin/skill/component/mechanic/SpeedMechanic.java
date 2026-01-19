package com.example.rpgplugin.skill.component.mechanic;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 移動速度メカニック
 * <p>ターゲットの移動速度を変更します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SpeedMechanic extends MechanicComponent {

    private static final String DURATION = "duration";
    private static final String AMPLIFIER = "amplifier";
    private static final String AMBIENT = "ambient";

    /**
     * 秒からティックへの変換定数
     */
    private static final int TICKS_PER_SECOND = 20;

    /**
     * コンストラクタ
     */
    public SpeedMechanic() {
        super("speed");
    }

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        ComponentSettings settings = getSettings();
        if (settings == null) {
            return false;
        }

        double duration = settings.getDouble(DURATION, 3.0);
        int amplifier = settings.getInt(AMPLIFIER, 0);
        boolean ambient = settings.getBoolean(AMBIENT, true);

        int ticks = (int) (duration * TICKS_PER_SECOND);
        // Paper 1.20.6: boolean overrideパラメータは削除された
        target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, amplifier, ambient));
        return true;
    }
}
