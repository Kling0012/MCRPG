package com.example.rpgplugin.skill.component.cost;

import com.example.rpgplugin.skill.SkillCostType;
import org.bukkit.entity.LivingEntity;

/**
 * HPを消費するコストコンポーネント
 *
 * <p>YAML設定例:</p>
 * <pre>
 * components:
 *   - type: HP
 *     base: 5.0
 *     per_level: 1.0
 *     min: 1.0
 *     max: 50.0
 * </pre>
 *
 * <p>設定パラメータ:</p>
 * <ul>
 *   <li>base: 基本コスト（デフォルト: 5.0）</li>
 *   <li>per_level: レベルごとのコスト増加量（デフォルト: 0.0）</li>
 *   <li>min: 最小コスト（デフォルト: 1.0）</li>
 *   <li>max: 最大コスト（デフォルト: 無制限）</li>
 *   <li>allow_death: 死亡を許可するか（デフォルト: false）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class HpCostComponent extends CostComponent {

    public HpCostComponent() {
        super("HP");
    }

    @Override
    protected boolean consumeCost(LivingEntity caster, int level) {
        if (caster == null || !caster.isValid()) {
            return false;
        }

        double cost = calculateCost(level);

        if (cost <= 0) {
            return true;
        }

        double currentHealth = caster.getHealth();
        boolean allowDeath = settings.getBoolean("allow_death", false);

        // 死亡を許可しない場合、残りHPよりコストが大きい場合は失敗
        if (!allowDeath && currentHealth <= cost) {
            return false;
        }

        // HPを消費
        double newHealth = Math.max(0, currentHealth - cost);
        caster.setHealth(newHealth);

        return true;
    }

    @Override
    public SkillCostType getCostType() {
        return SkillCostType.HP;
    }
}
