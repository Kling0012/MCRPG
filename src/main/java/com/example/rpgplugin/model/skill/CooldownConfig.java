package com.example.rpgplugin.model.skill;

import com.example.rpgplugin.skill.LevelDependentParameter;

/**
 * クールダウン設定
 *
 * <p>レベル依存のクールダウン設定を表します。</p>
 *
 * <p>YAML例:</p>
 * <pre>
 * cooldown:
 *   base: 5.0
 *   per_level: -0.5
 *   min: 1.0
 * </pre>
 */
public class CooldownConfig {
    private final LevelDependentParameter parameter;

    public CooldownConfig(LevelDependentParameter parameter) {
        this.parameter = parameter;
    }

    public LevelDependentParameter getParameter() {
        return parameter;
    }

    /**
     * 指定レベルでのクールダウンを取得します
     *
     * @param level スキルレベル
     * @return クールダウン（秒）
     */
    public double getCooldown(int level) {
        if (parameter != null) {
            return parameter.getValue(level);
        }
        return 0.0;
    }
}
