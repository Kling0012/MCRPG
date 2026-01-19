package com.example.rpgplugin.skill.component.mechanic;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Registry;
import org.bukkit.NamespacedKey;

/**
 * クリーンメカニック
 * <p>ターゲットのポーション効果を解除します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class CleanseMechanic extends MechanicComponent {

    private static final String BAD_ONLY = "bad_only";
    private static final String POTION = "potion";

    /**
     * コンストラクタ
     */
    public CleanseMechanic() {
        super("cleanse");
    }

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        ComponentSettings settings = getSettings();
        if (settings == null) {
            return false;
        }

        boolean badOnly = settings.getBoolean(BAD_ONLY, true);
        String specificPotion = settings.getString(POTION, null);

        if (specificPotion != null && !specificPotion.isEmpty()) {
            // 特定のポーションのみ解除
            PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(
                    NamespacedKey.minecraft(specificPotion.toLowerCase().replace(' ', '_'))
            );
            if (type != null) {
                target.removePotionEffect(type);
                return true;
            }
            return false;
        }

        // 全てのポーション効果を解除
        boolean worked = false;
        for (PotionEffectType type : Registry.POTION_EFFECT_TYPE) {
            if (type == null) continue;

            if (badOnly) {
                // 負の効果のみ解除
                if (isNegativeEffect(type)) {
                    target.removePotionEffect(type);
                    worked = true;
                }
            } else {
                target.removePotionEffect(type);
                worked = true;
            }
        }
        return worked;
    }

    /**
     * 負のポーション効果か判定します
     *
     * @param type ポーション効果タイプ
     * @return 負の効果の場合はtrue
     */
    private boolean isNegativeEffect(PotionEffectType type) {
        if (type == null) return false;
        // 一般的に負の効果とされるもの
        // Minecraft 1.20.1以降のAPIに対応
        return type.getKey().equals(org.bukkit.NamespacedKey.minecraft("slow"))
                || type.getKey().equals(org.bukkit.NamespacedKey.minecraft("poison"))
                || type.getKey().equals(org.bukkit.NamespacedKey.minecraft("wither"))
                || type.getKey().equals(org.bukkit.NamespacedKey.minecraft("weakness"))
                || type.getKey().equals(org.bukkit.NamespacedKey.minecraft("blindness"))
                || type.getKey().equals(org.bukkit.NamespacedKey.minecraft("hunger"))
                || type.getKey().equals(org.bukkit.NamespacedKey.minecraft("darkness"))
                || type.getKey().equals(org.bukkit.NamespacedKey.minecraft("levitation"))  // 場合による
                || type.getKey().equals(org.bukkit.NamespacedKey.minecraft("bad_omen"))
                || type.getKey().equals(org.bukkit.NamespacedKey.minecraft("unluck"));
    }
}
