package com.example.rpgplugin.skill.component.condition;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * 防具条件
 * <p>ターゲットが特定の防具を装備している場合のみ効果を適用します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ArmorCondition extends ConditionComponent {

    private static final String MATERIAL = "material";
    private static final String SLOT = "slot"; // helmet, chestplate, leggings, boots

    /**
     * コンストラクタ
     */
    public ArmorCondition() {
        super("armor");
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

        org.bukkit.entity.Player player = (org.bukkit.entity.Player) target;
        String material = settings.getString(MATERIAL, "").toUpperCase();
        String slot = settings.getString(SLOT, "any").toLowerCase();

        if (material.isEmpty()) {
            return true;
        }

        ItemStack[] armorContents = player.getInventory().getArmorContents();
        boolean matches = false;

        for (int i = 0; i < armorContents.length; i++) {
            ItemStack item = armorContents[i];
            if (item == null) continue;

            String slotName = getSlotName(i);
            if (!"any".equals(slot) && !slotName.equals(slot)) {
                continue;
            }

            if (item.getType().name().contains(material)) {
                matches = true;
                break;
            }
        }

        return matches;
    }

    /**
     * スロットインデックスからスロット名を取得します
     *
     * @param index スロットインデックス
     * @return スロット名
     */
    private String getSlotName(int index) {
        return switch (index) {
            case 0 -> "boots";
            case 1 -> "leggings";
            case 2 -> "chestplate";
            case 3 -> "helmet";
            default -> "unknown";
        };
    }
}
