package com.example.rpgplugin.skill.component.condition;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Registry;
import org.bukkit.NamespacedKey;

/**
 * ポーション条件
 * <p>ターゲットが特定のポーション効果を受けている場合のみ効果を適用します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class PotionCondition extends ConditionComponent {

    private static final String POTION = "potion";
    private static final String MIN_LEVEL = "min_level";

    /**
     * コンストラクタ
     */
    public PotionCondition() {
        super("potion");
    }

    @Override
    protected boolean test(LivingEntity caster, int level, LivingEntity target) {
        ComponentSettings settings = getSettings();
        if (settings == null) {
            return false;
        }

        String potionName = settings.getString(POTION, "");
        if (potionName.isEmpty()) {
            return false;
        }

        // Registry APIを使用してPotionEffectTypeを取得
        PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(
                NamespacedKey.minecraft(potionName.toLowerCase().replace(' ', '_'))
        );
        if (type == null) {
            return false;
        }

        if (!target.hasPotionEffect(type)) {
            return false;
        }

        int minLevel = settings.getInt(MIN_LEVEL, 0);
        if (minLevel > 0) {
            org.bukkit.potion.PotionEffect effect = target.getPotionEffect(type);
            if (effect == null || effect.getAmplifier() < minLevel - 1) {
                return false;
            }
        }

        return true;
    }
}
