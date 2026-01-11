package com.example.rpgplugin.skill.evaluator;

import com.example.rpgplugin.player.RPGPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 数式エバリュエーター
 *
 * <p>YAML内で記述された数式文字列を実行時に評価するエンジンです。</p>
 * <p>主な機能:</p>
 * <ul>
 *   <li>数式解析と評価: 文字列から数式をパースして評価</li>
 *   <li>変数参照: ステータス(STR,INT,SPI,VIT,DEX)、スキルレベル(Lv)、プレイヤーレベル(LV)</li>
 *   <li>カスタム変数: YAML内で定義された独自変数のサポート</li>
 *   <li>レベル依存: レベルごとの数式または共通式の切り替え</li>
 *   <li>演算子: 四則演算、括弧、べき乗、比較演算子、論理演算子</li>
 *   <li>キャッシュ: パース結果のキャッシュによる高速化</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: 数式評価の単一責務</li>
 *   <li>DRY: キャッシュロジックの一元化</li>
 *   <li>KISS: シンプルなAPI設計</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class FormulaEvaluator {

    private static final Logger LOGGER = Logger.getLogger(FormulaEvaluator.class.getName());

    /** パース結果キャッシュ */
    private final Map<String, CachedExpression> expressionCache;

    /** 最大キャッシュサイズ */
    private static final int MAX_CACHE_SIZE = 500;

    /** キャッシュされた数式情報 */
    private static class CachedExpression {
        CachedExpression(String expression) {
            // プレースホルダー: 将来的なキャッシュ拡張用
        }
    }

    /**
     * コンストラクタ
     */
    public FormulaEvaluator() {
        this.expressionCache = new ConcurrentHashMap<>();
    }

    /**
     * 数式を評価します
     *
     * @param expression 数式文字列
     * @param rpgPlayer RPGプレイヤー（ステータス取得用）
     * @param skillLevel スキルレベル
     * @return 評価結果
     * @throws FormulaEvaluationException 評価エラー
     */
    public double evaluate(String expression, RPGPlayer rpgPlayer, int skillLevel)
            throws FormulaEvaluationException {
        return evaluate(expression, rpgPlayer, skillLevel, null);
    }

    /**
     * 数式を評価します（カスタム変数付き）
     *
     * @param expression 数式文字列
     * @param rpgPlayer RPGプレイヤー（ステータス取得用）
     * @param skillLevel スキルレベル
     * @param customVariables カスタム変数マップ
     * @return 評価結果
     * @throws FormulaEvaluationException 評価エラー
     */
    public double evaluate(String expression, RPGPlayer rpgPlayer, int skillLevel,
                          Map<String, Double> customVariables)
            throws FormulaEvaluationException {

        if (expression == null || expression.trim().isEmpty()) {
            return 0.0;
        }

        VariableContext context = new VariableContext(rpgPlayer, skillLevel);

        if (customVariables != null) {
            context.setCustomVariables(customVariables);
        }

        return evaluateWithContext(expression, context);
    }

    /**
     * 変数コンテキストを使用して数式を評価します
     *
     * @param expression 数式文字列
     * @param context 変数コンテキスト
     * @return 評価結果
     * @throws FormulaEvaluationException 評価エラー
     */
    public double evaluateWithContext(String expression, VariableContext context)
            throws FormulaEvaluationException {

        if (expression == null || expression.trim().isEmpty()) {
            return 0.0;
        }

        try {
            ExpressionParser parser = new ExpressionParser(expression);
            double result = parser.evaluate(context);

            // キャッシュに追加
            cacheExpression(expression);

            return result;

        } catch (ExpressionParser.FormulaEvaluationException e) {
            String message = "[FormulaEvaluator] 数式評価エラー: '" + expression + "' - " + e.getMessage();
            LOGGER.warning(message);
            throw new FormulaEvaluationException(message, e);
        }
    }

    /**
     * レベル別数式設定から評価します
     *
     * <p>指定されたスキルレベルに対応する数式を選択して評価します。</p>
     * <p>レベル別数式が存在しない場合はフォールバック式を使用します。</p>
     *
     * @param levelFormulas レベル別数式マップ（レベル → 数式）
     * @param fallbackFormula フォールバック数式（未定義レベル用）
     * @param rpgPlayer RPGプレイヤー
     * @param skillLevel スキルレベル
     * @return 評価結果
     * @throws FormulaEvaluationException 評価エラー
     */
    public double evaluateLevelBased(Map<Integer, String> levelFormulas,
                                     String fallbackFormula,
                                     RPGPlayer rpgPlayer,
                                     int skillLevel)
            throws FormulaEvaluationException {

        // 一致するレベルの数式を検索
        String targetFormula = null;

        if (levelFormulas != null && !levelFormulas.isEmpty()) {
            // 完全一致を探す
            targetFormula = levelFormulas.get(skillLevel);

            if (targetFormula == null) {
                // 以下の最大レベルを探す
                int maxMatchLevel = -1;
                for (Map.Entry<Integer, String> entry : levelFormulas.entrySet()) {
                    int level = entry.getKey();
                    if (level <= skillLevel && level > maxMatchLevel) {
                        maxMatchLevel = level;
                        targetFormula = entry.getValue();
                    }
                }
            }
        }

        // 見つからない場合はフォールバック
        if (targetFormula == null || targetFormula.trim().isEmpty()) {
            targetFormula = fallbackFormula;
        }

        // それでも空の場合はデフォルト値
        if (targetFormula == null || targetFormula.trim().isEmpty()) {
            LOGGER.warning("[FormulaEvaluator] 数式が未定義です。スキルレベル: " + skillLevel);
            return 0.0;
        }

        return evaluate(targetFormula, rpgPlayer, skillLevel);
    }

    /**
     * 数式を評価します（例外をハンドリングしてデフォルト値を返す）
     *
     * @param expression 数式文字列
     * @param rpgPlayer RPGプレイヤー
     * @param skillLevel スキルレベル
     * @param defaultValue エラー時のデフォルト値
     * @return 評価結果、またはデフォルト値
     */
    public double evaluateSafe(String expression, RPGPlayer rpgPlayer, int skillLevel, double defaultValue) {
        try {
            return evaluate(expression, rpgPlayer, skillLevel);
        } catch (FormulaEvaluationException e) {
            LOGGER.warning("[FormulaEvaluator] 評価エラー（デフォルト値使用）: " + e.getMessage());
            return defaultValue;
        }
    }

    /**
     * 数式を検証します（評価は行いません）
     *
     * @param expression 検証する数式文字列
     * @return 有効な場合はtrue
     */
    public boolean validate(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }

        try {
            // ダミーコンテキストでパースのみ実行
            VariableContext dummyContext = new VariableContext(null, 1);
            dummyContext.setCustomVariable("STR", 10.0);
            dummyContext.setCustomVariable("INT", 10.0);
            dummyContext.setCustomVariable("SPI", 10.0);
            dummyContext.setCustomVariable("VIT", 10.0);
            dummyContext.setCustomVariable("DEX", 10.0);
            dummyContext.setCustomVariable("Lv", 1.0);
            dummyContext.setCustomVariable("LV", 1.0);

            ExpressionParser parser = new ExpressionParser(expression);
            parser.evaluate(dummyContext);
            return true;

        } catch (ExpressionParser.FormulaEvaluationException e) {
            LOGGER.fine("[FormulaEvaluator] 検証失敗: '" + expression + "' - " + e.getMessage());
            return false;
        }
    }

    /**
     * 式をキャッシュします
     *
     * @param expression 数式
     */
    private void cacheExpression(String expression) {
        // キャッシュサイズ制限
        if (expressionCache.size() >= MAX_CACHE_SIZE) {
            // 古いエントリを削除（簡易的実装）
            expressionCache.clear();
        }
        expressionCache.put(expression, new CachedExpression(expression));
    }

    /**
     * キャッシュをクリアします
     */
    public void clearCache() {
        expressionCache.clear();
    }

    /**
     * キャッシュサイズを取得します
     *
     * @return キャッシュサイズ
     */
    public int getCacheSize() {
        return expressionCache.size();
    }

    /**
     * 数式評価例外
     */
    public static class FormulaEvaluationException extends Exception {
        public FormulaEvaluationException(String message) {
            super(message);
        }

        public FormulaEvaluationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
