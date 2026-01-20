package com.example.rpgplugin.model.skill;

import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.skill.LevelDependentParameter;

/**
 * コスト設定
 *
 * <p>レベル依存のコスト設定を表します。</p>
 *
 * <p>YAML例:</p>
 * <pre>
 * cost:
 *   type: mana
 *   base: 10
 *   per_level: -1
 *   min: 0
 * </pre>
 */
public class CostConfig {
    private final SkillCostType type;
    private final LevelDependentParameter parameter;

    public CostConfig(SkillCostType type, LevelDependentParameter parameter) {
        this.type = type != null ? type : SkillCostType.MANA;
        this.parameter = parameter;
    }

    public SkillCostType getType() {
        return type;
    }

    public LevelDependentParameter getParameter() {
        return parameter;
    }

    /**
     * 指定レベルでのコストを取得します
     *
     * @param level スキルレベル
     * @return コスト値
     */
    public int getCost(int level) {
        if (parameter != null) {
            return parameter.getIntValue(level);
        }
        return 0;
    }
}
