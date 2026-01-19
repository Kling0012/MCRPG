package com.example.rpgplugin.model.skill;

import com.example.rpgplugin.stats.Stat;

/**
 * ダメージ計算設定
 *
 * <p>基本ダメージ、ステータス倍率、レベル倍率を設定します。</p>
 */
public class DamageCalculation {
    private final double base;
    private final Stat statMultiplier;
    private final double multiplierValue;
    private final double levelMultiplier;

    public DamageCalculation(double base, Stat statMultiplier, double multiplierValue, double levelMultiplier) {
        this.base = base;
        this.statMultiplier = statMultiplier;
        this.multiplierValue = multiplierValue;
        this.levelMultiplier = levelMultiplier;
    }

    public double getBase() {
        return base;
    }

    public Stat getStatMultiplier() {
        return statMultiplier;
    }

    public double getMultiplierValue() {
        return multiplierValue;
    }

    public double getLevelMultiplier() {
        return levelMultiplier;
    }

    /**
     * ダメージを計算します
     *
     * @param statValue ステータス値
     * @param skillLevel スキルレベル
     * @return 計算されたダメージ
     */
    public double calculateDamage(double statValue, int skillLevel) {
        return base + (statValue * multiplierValue) + (skillLevel * levelMultiplier);
    }
}
