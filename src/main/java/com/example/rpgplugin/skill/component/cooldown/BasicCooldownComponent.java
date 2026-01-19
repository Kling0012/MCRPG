package com.example.rpgplugin.skill.component.cooldown;

import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * 基本クールダウンコンポーネント
 *
 * <p>スキル発動後のクールダウン時間を設定します。</p>
 *
 * <p>YAML設定例:</p>
 * <pre>
 * components:
 *   - type: COOLDOWN
 *     base: 5.0
 *     per_level: -0.5
 *     min: 1.0
 *     max: 10.0
 * </pre>
 *
 * <p>設定パラメータ:</p>
 * <ul>
 *   <li>base: 基本クールダウン（秒、デフォルト: 5.0）</li>
 *   <li>per_level: レベルごとのクールダウン変化量（デフォルト: 0.0）</li>
 *   <li>min: 最小クールダウン（秒、デフォルト: 0.0）</li>
 *   <li>max: 最大クールダウン（秒、デフォルト: 無制限）</li>
 * </ul>
 *
 * <p>使用例:</p>
 * <ul>
 *   <li>base: 5.0, per_level: 0.0 → 常に5秒</li>
 *   <li>base: 10.0, per_level: -1.0 → レベル1で10秒、レベル5で6秒</li>
 *   <li>base: 3.0, per_level: -0.5, min: 1.0 → レベル1で3秒、レベル5で1秒（最小1秒）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class BasicCooldownComponent extends CooldownComponent {

    public BasicCooldownComponent() {
        super("COOLDOWN");
    }

    /**
     * 基本クールダウンコンポーネントは特別な処理を行いません
     * クールダウン値は計算メソッドから取得できます
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        // 基本的に子コンポーネントのみ実行
        // クールダウン設定自体はSkillManager側で行われる
        return executeChildren(caster, level, targets);
    }
}
