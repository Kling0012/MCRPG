package com.example.rpgplugin.skill.component.condition;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Material;

/**
 * 水中条件
 * <p>ターゲットが水中にいる場合のみ効果を適用します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class WaterCondition extends ConditionComponent {

    private static final String DEPTH = "depth"; // 最小水深

    /**
     * コンストラクタ
     */
    public WaterCondition() {
        super("water");
    }

    @Override
    protected boolean test(LivingEntity caster, int level, LivingEntity target) {
        ComponentSettings settings = getSettings();
        if (settings == null) {
            return target.isInWater();
        }

        int minDepth = settings.getInt(DEPTH, 0);

        if (!target.isInWater()) {
            return false;
        }

        // 水深を確認
        int depth = 0;
        Material current = target.getLocation().getBlock().getType();
        while (current == Material.WATER || current == Material.SEAGRASS
                || current == Material.TALL_SEAGRASS || current == Material.KELP) {
            depth++;
            current = target.getLocation().add(0, depth, 0).getBlock().getType();
        }

        return depth >= minDepth;
    }
}
