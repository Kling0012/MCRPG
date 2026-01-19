package com.example.rpgplugin.skill;

/**
 * レベル依存パラメータ
 *
 * <p>スキルレベルに応じて変化するパラメータ（CD、マナコスト、ダメージ等）を表します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: レベル依存パラメータの表現に専念</li>
 *   <li>DRY: 計算ロジックを一元管理</li>
 *   <li>YAGNI: 必要な機能のみ実装</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class LevelDependentParameter {

    private final double base;
    private final double perLevel;
    private final Double minValue;
    private final Double maxValue;

    /**
     * コンストラクタ
     *
     * @param base 基本値（Lv1時の値）
     * @param perLevel レベル毎の変化量（増加:正、減少:負、変化なし:0）
     */
    public LevelDependentParameter(double base, double perLevel) {
        this(base, perLevel, null, null);
    }

    /**
     * コンストラクタ（最小値制限付き）
     *
     * @param base 基本値（Lv1時の値）
     * @param perLevel レベル毎の変化量（増加:正、減少:負、変化なし:0）
     * @param minValue 最小値（nullの場合は制限なし）
     */
    public LevelDependentParameter(double base, double perLevel, Double minValue) {
        this(base, perLevel, minValue, null);
    }

    /**
     * フルコンストラクタ
     *
     * @param base 基本値（Lv1時の値）
     * @param perLevel レベル毎の変化量（増加:正、減少:負、変化なし:0）
     * @param minValue 最小値（nullの場合は制限なし）
     * @param maxValue 最大値（nullの場合は制限なし）
     */
    public LevelDependentParameter(double base, double perLevel, Double minValue, Double maxValue) {
        this.base = base;
        this.perLevel = perLevel;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * 指定されたレベルでの値を計算します
     *
     * @param level スキルレベル（1から始まる）
     * @return 計算された値（minValue/maxValueの制限適用済み）
     * @throws IllegalArgumentException レベルが1未満の場合
     */
    public double getValue(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("スキルレベルは1以上である必要があります: " + level);
        }
        double value = base + (perLevel * (level - 1));
        if (minValue != null && value < minValue) {
            return minValue;
        }
        if (maxValue != null && value > maxValue) {
            return maxValue;
        }
        return value;
    }

    /**
     * 整数値として取得します
     *
     * @param level スキルレベル
     * @return 整数値（切り捨て）
     */
    public int getIntValue(int level) {
        return (int) Math.floor(getValue(level));
    }

    public double getBase() {
        return base;
    }

    public double getPerLevel() {
        return perLevel;
    }

    public Double getMinValue() {
        return minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    @Override
    public String toString() {
        return "LevelDependentParameter{" +
                "base=" + base +
                ", perLevel=" + perLevel +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                '}';
    }
}
