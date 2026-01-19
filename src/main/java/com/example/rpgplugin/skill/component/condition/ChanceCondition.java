package com.example.rpgplugin.skill.component.condition;

import org.bukkit.entity.LivingEntity;

import java.util.Random;

/**
 * 確率条件コンポーネント
 * <p>指定された確率でターゲットを通過させます</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ChanceCondition extends ConditionComponent {

    private static final String CHANCE = "chance";
    private static final Random RANDOM = new Random();

    /**
     * コンストラクタ
     */
    public ChanceCondition() {
        super("chance");
    }

    @Override
    protected boolean test(LivingEntity caster, int level, LivingEntity target) {
        double chance = parseValues(caster, CHANCE, level, 50) / 100.0;
        return RANDOM.nextDouble() < chance;
    }
}
