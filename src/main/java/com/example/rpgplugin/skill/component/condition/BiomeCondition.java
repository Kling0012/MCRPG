package com.example.rpgplugin.skill.component.condition;

import org.bukkit.entity.LivingEntity;

import java.util.Locale;

/**
 * バイオーム条件コンポーネント
 * <p>ターゲットが特定のバイオームにいるかをチェックします</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class BiomeCondition extends ConditionComponent {

    private static final String BIOME = "biome";

    /**
     * コンストラクタ
     */
    public BiomeCondition() {
        super("biome");
    }

    @Override
    protected boolean test(LivingEntity caster, int level, LivingEntity target) {
        String required = getString(BIOME, "").toLowerCase(Locale.ENGLISH);
        if (required.isEmpty()) {
            return true;
        }

        String current = target.getLocation().getBlock().getBiome()
                .name().toLowerCase(Locale.ENGLISH);

        // 部分一致でチェック
        return current.contains(required);
    }
}
