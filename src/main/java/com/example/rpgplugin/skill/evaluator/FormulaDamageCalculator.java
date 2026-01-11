package com.example.rpgplugin.skill.evaluator;

import com.example.rpgplugin.player.RPGPlayer;

import java.util.Map;
import java.util.logging.Logger;

/**
 * 数式ベースダメージ計算機
 *
 * <p>数式エバリュエーターを使用してダメージを計算します。</p>
 * <p>既存の {@code Skill.DamageCalculation} の機能を拡張・置換します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ダメージ計算の単一責務</li>
 *   <li>SOLID-O: 数式による柔軟な拡張</li>
 *   <li>KISS: シンプルなAPI</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class FormulaDamageCalculator {

    private static final Logger LOGGER = Logger.getLogger(FormulaDamageCalculator.class.getName());

    /** 数式エバリュエーター */
    private final FormulaEvaluator evaluator;

    /** ダメージ数式 */
    private final String damageFormula;

    /** レベル別ダメージ数式 */
    private final Map<Integer, String> levelFormulas;

    /** フォールバック数式 */
    private final String fallbackFormula;

    /** カスタム変数 */
    private final Map<String, Double> customVariables;

    /**
     * コンストラクタ
     *
     * @param evaluator 数式エバリュエーター
     * @param damageFormula ダメージ数式
     */
    public FormulaDamageCalculator(FormulaEvaluator evaluator, String damageFormula) {
        this(evaluator, damageFormula, null, null);
    }

    /**
     * コンストラクタ（レベル別対応）
     *
     * @param evaluator 数式エバリュエーター
     * @param damageFormula ダメージ数式
     * @param levelFormulas レベル別数式
     * @param fallbackFormula フォールバック数式
     */
    public FormulaDamageCalculator(FormulaEvaluator evaluator,
                                   String damageFormula,
                                   Map<Integer, String> levelFormulas,
                                   String fallbackFormula) {
        this.evaluator = evaluator;
        this.damageFormula = damageFormula;
        this.levelFormulas = levelFormulas;
        this.fallbackFormula = fallbackFormula;
        this.customVariables = new java.util.concurrent.ConcurrentHashMap<>();
    }

    /**
     * カスタム変数を設定します
     *
     * @param variables 変数マップ
     */
    public void setCustomVariables(Map<String, Double> variables) {
        if (variables != null) {
            customVariables.putAll(variables);
        }
    }

    /**
     * カスタム変数を追加します
     *
     * @param name 変数名
     * @param value 変数値
     */
    public void addCustomVariable(String name, double value) {
        customVariables.put(name, value);
    }

    /**
     * ダメージを計算します
     *
     * @param rpgPlayer RPGプレイヤー
     * @param skillLevel スキルレベル
     * @return 計算されたダメージ
     */
    public double calculateDamage(RPGPlayer rpgPlayer, int skillLevel) {
        try {
            // レベル別数式を使用する場合
            if (levelFormulas != null && !levelFormulas.isEmpty()) {
                return evaluator.evaluateLevelBased(
                        levelFormulas,
                        fallbackFormula != null ? fallbackFormula : damageFormula,
                        rpgPlayer,
                        skillLevel
                );
            }

            // 通常の数式を使用
            String formula = (damageFormula != null && !damageFormula.isEmpty())
                    ? damageFormula
                    : fallbackFormula;

            if (formula == null || formula.isEmpty()) {
                LOGGER.warning("[FormulaDamageCalculator] 数式が未定義です");
                return 0.0;
            }

            return evaluator.evaluate(formula, rpgPlayer, skillLevel, customVariables);

        } catch (FormulaEvaluator.FormulaEvaluationException e) {
            LOGGER.warning("[FormulaDamageCalculator] ダメージ計算エラー: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * 指定ステータスを使用してダメージを計算します（レガシー互換）
     *
     * <p>既存の {@code Skill.DamageCalculation.calculateDamage()} と互換性を持たせます。</p>
     *
     * @param statValue ステータス値
     * @param skillLevel スキルレベル
     * @return 計算されたダメージ
     */
    public double calculateDamage(double statValue, int skillLevel) {
        // レガシー互換: 固定式 base + (stat * multiplier) + (level * levelMultiplier)
        // 数式が設定されている場合は数式を優先
        if (damageFormula != null && !damageFormula.isEmpty()) {
            // ダミーコンテキストで評価（ステータス値をカスタム変数として設定）
            VariableContext context = new VariableContext(null, skillLevel);
            context.setCustomVariable("stat", statValue);
            context.setCustomVariable("STR", statValue);
            context.setCustomVariable("INT", statValue);
            context.setCustomVariable("SPI", statValue);
            context.setCustomVariable("VIT", statValue);
            context.setCustomVariable("DEX", statValue);

            try {
                return evaluator.evaluateWithContext(damageFormula, context);
            } catch (FormulaEvaluator.FormulaEvaluationException e) {
                LOGGER.warning("[FormulaDamageCalculator] 評価エラー: " + e.getMessage());
            }
        }

        // デフォルト計算
        return statValue * skillLevel;
    }

    /**
     * ダメージ数式を取得します
     *
     * @return ダメージ数式
     */
    public String getDamageFormula() {
        return damageFormula;
    }

    /**
     * レベル別数式を取得します
     *
     * @return レベル別数式マップのコピー
     */
    public Map<Integer, String> getLevelFormulas() {
        return levelFormulas != null ? Map.copyOf(levelFormulas) : Map.of();
    }

    /**
     * フォールバック数式を取得します
     *
     * @return フォールバック数式
     */
    public String getFallbackFormula() {
        return fallbackFormula;
    }

    /**
     * ビルダークラス
     */
    public static class Builder {
        private final FormulaEvaluator evaluator;
        private String damageFormula;
        private Map<Integer, String> levelFormulas;
        private String fallbackFormula;
        private Map<String, Double> customVariables = new java.util.concurrent.ConcurrentHashMap<>();

        /**
         * コンストラクタ
         *
         * @param evaluator 数式エバリュエーター
         */
        public Builder(FormulaEvaluator evaluator) {
            this.evaluator = evaluator;
        }

        /**
         * ダメージ数式を設定します
         *
         * @param formula 数式
         * @return このビルダー
         */
        public Builder damageFormula(String formula) {
            this.damageFormula = formula;
            return this;
        }

        /**
         * レベル別数式を設定します
         *
         * @param formulas レベル別数式マップ
         * @return このビルダー
         */
        public Builder levelFormulas(Map<Integer, String> formulas) {
            this.levelFormulas = formulas;
            return this;
        }

        /**
         * フォールバック数式を設定します
         *
         * @param formula フォールバック数式
         * @return このビルダー
         */
        public Builder fallbackFormula(String formula) {
            this.fallbackFormula = formula;
            return this;
        }

        /**
         * カスタム変数を設定します
         *
         * @param variables 変数マップ
         * @return このビルダー
         */
        public Builder customVariables(Map<String, Double> variables) {
            if (variables != null) {
                this.customVariables.putAll(variables);
            }
            return this;
        }

        /**
         * カスタム変数を追加します
         *
         * @param name 変数名
         * @param value 変数値
         * @return このビルダー
         */
        public Builder addVariable(String name, double value) {
            this.customVariables.put(name, value);
            return this;
        }

        /**
         * 計算機を構築します
         *
         * @return 構築された計算機
         */
        public FormulaDamageCalculator build() {
            FormulaDamageCalculator calculator = new FormulaDamageCalculator(
                    evaluator,
                    damageFormula,
                    levelFormulas,
                    fallbackFormula
            );
            calculator.setCustomVariables(customVariables);
            return calculator;
        }
    }
}
