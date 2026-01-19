package com.example.rpgplugin.skill.component.cost;

import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.skill.component.ComponentType;
import com.example.rpgplugin.skill.component.EffectComponent;
import org.bukkit.entity.LivingEntity;
import java.util.List;

/**
 * コストコンポーネントの基底クラス
 * <p>スキル発動に必要なコストを計算・消費します</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: コスト消費に専念</li>
 *   <li>Strategy: 異なるコストタイプの消費ロジックをStrategyパターンで管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public abstract class CostComponent extends EffectComponent {

    /**
     * コンストラクタ
     *
     * @param key コンポーネントキー
     */
    protected CostComponent(String key) {
        super(key);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.COST;
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        // コストを消費
        if (!consumeCost(caster, level)) {
            return false;
        }

        // 子コンポーネントを実行
        return executeChildren(caster, level, targets);
    }

    /**
     * コストを消費します
     *
     * @param caster 発動者
     * @param level  スキルレベル
     * @return 消費成功の場合はtrue
     */
    protected abstract boolean consumeCost(LivingEntity caster, int level);

    /**
     * コストを計算します
     *
     * @param level スキルレベル
     * @return コスト値
     */
    protected double calculateCost(int level) {
        double base = settings.getDouble("base", 0.0);
        double perLevel = settings.getDouble("per_level", 0.0);

        double cost = base + (perLevel * (level - 1));

        // 最小値を適用
        double min = settings.getDouble("min", 0.0);
        cost = Math.max(cost, min);

        // 最大値を適用
        double max = settings.getDouble("max", Double.MAX_VALUE);
        cost = Math.min(cost, max);

        return cost;
    }

    /**
     * コストタイプを取得します
     *
     * @return コストタイプ
     */
    public SkillCostType getCostType() {
        return SkillCostType.MANA; // デフォルトはMANA
    }
}
