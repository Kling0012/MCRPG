package com.example.rpgplugin.skill.component.condition;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.entity.LivingEntity;

/**
 * ツール条件
 * <p>ターゲットが特定のツールを持っている場合のみ効果を適用します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ToolCondition extends ConditionComponent {

    private static final String MATERIAL = "material";
    private static final String HAND = "hand"; // main, off, any

    /**
     * コンストラクタ
     */
    public ToolCondition() {
        super("tool");
    }

    @Override
    protected boolean test(LivingEntity caster, int level, LivingEntity target) {
        if (!(target instanceof org.bukkit.entity.Player)) {
            return false;
        }

        ComponentSettings settings = getSettings();
        if (settings == null) {
            return true;
        }

        String material = settings.getString(MATERIAL, "").toUpperCase();
        String hand = settings.getString(HAND, "any").toLowerCase();

        if (material.isEmpty()) {
            return true;
        }

        org.bukkit.entity.Player player = (org.bukkit.entity.Player) target;
        boolean matches = false;

        if ("any".equals(hand) || "main".equals(hand)) {
            org.bukkit.inventory.ItemStack mainItem = player.getInventory().getItemInMainHand();
            if (mainItem != null && mainItem.getType().name().contains(material)) {
                matches = true;
            }
        }

        if (!matches && ("any".equals(hand) || "off".equals(hand))) {
            org.bukkit.inventory.ItemStack offItem = player.getInventory().getItemInOffHand();
            if (offItem != null && offItem.getType().name().contains(material)) {
                matches = true;
            }
        }

        return matches;
    }
}
