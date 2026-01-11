package com.example.rpgplugin.skill.component.cooldown;

import com.example.rpgplugin.skill.component.ComponentType;
import com.example.rpgplugin.skill.component.EffectComponent;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * クールダウンコンポーネントの基底クラス
 * <p>スキル発動後のクールダウンを管理します</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: クールダウン管理に専念</li>
 *   <li>Strategy: 異なるクールダウンタイプをStrategyパターンで管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public abstract class CooldownComponent extends EffectComponent {

    /**
     * コンストラクタ
     *
     * @param key コンポーネントキー
     */
    protected CooldownComponent(String key) {
        super(key);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.COOLDOWN;
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        // クールダウンは実行時に設定されるものではなく、
        // スキル発動後に適用されるものなので、
        // ここでは子コンポーネントを実行するのみ
        return executeChildren(caster, level, targets);
    }

    /**
     * クールダウン時間を計算します
     *
     * @param level スキルレベル
     * @return クールダウン時間（秒）
     */
    protected double calculateCooldown(int level) {
        double base = settings.getDouble("base", 0.0);
        double perLevel = settings.getDouble("per_level", 0.0);

        double cooldown = base + (perLevel * (level - 1));

        // 最小値を適用
        double min = settings.getDouble("min", 0.0);
        cooldown = Math.max(cooldown, min);

        // 最大値を適用
        double max = settings.getDouble("max", Double.MAX_VALUE);
        cooldown = Math.min(cooldown, max);

        return cooldown;
    }

    /**
     * クールダウン時間（ミリ秒）を取得します
     *
     * @param level スキルレベル
     * @return クールダウン時間（ミリ秒）
     */
    public long getCooldownMillis(int level) {
        return (long) (calculateCooldown(level) * 1000);
    }

    /**
     * クールダウン時間（秒）を取得します
     *
     * @param level スキルレベル
     * @return クールダウン時間（秒）
     */
    public double getCooldownSeconds(int level) {
        return calculateCooldown(level);
    }
}
