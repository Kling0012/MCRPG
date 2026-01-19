package com.example.rpgplugin.skill.component.mechanic;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Registry;
import org.bukkit.NamespacedKey;

/**
 * ポーションメカニック
 * <p>ターゲットにポーション効果を付与します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class PotionMechanic extends MechanicComponent {

    private static final String POTION = "potion";
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
    public PotionMechanic() {
        super("potion");
    }

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        ComponentSettings settings = getSettings();
        if (settings == null) {
            return false;
        }

        String potionName = settings.getString(POTION, "SPEED").toUpperCase().replace(' ', '_');
        double duration = settings.getDouble(DURATION, 3.0);
        int amplifier = settings.getInt(AMPLIFIER, 0);
        boolean ambient = settings.getBoolean(AMBIENT, true);

        try {
            // Registry APIを使用してPotionEffectTypeを取得
            PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(
                    NamespacedKey.minecraft(potionName.toLowerCase())
            );
            if (type == null) {
                return false;
            }

            int ticks = (int) (duration * TICKS_PER_SECOND);
            // Paper 1.20.6: boolean overrideパラメータは削除された
            target.addPotionEffect(new PotionEffect(type, ticks, amplifier, ambient));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
