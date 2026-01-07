package com.example.rpgplugin.skill.evaluator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数式エバリュエーターのテストクラス
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("数式エバリュエーター テスト")
class FormulaEvaluatorTest {

    private FormulaEvaluator evaluator;
    private VariableContext context;

    @BeforeEach
    void setUp() {
        evaluator = new FormulaEvaluator();
        context = new VariableContext(null, 5);
    }

    // ===== 基本的な算術演算テスト =====

    @Test
    @DisplayName("加算テスト")
    void testAddition() throws Exception {
        context.setCustomVariable("a", 10.0);
        context.setCustomVariable("b", 20.0);
        double result = evaluator.evaluateWithContext("a + b", context);
        assertEquals(30.0, result, 0.001);
    }

    @Test
    @DisplayName("減算テスト")
    void testSubtraction() throws Exception {
        context.setCustomVariable("a", 20.0);
        context.setCustomVariable("b", 10.0);
        double result = evaluator.evaluateWithContext("a - b", context);
        assertEquals(10.0, result, 0.001);
    }

    @Test
    @DisplayName("乗算テスト")
    void testMultiplication() throws Exception {
        context.setCustomVariable("a", 5.0);
        context.setCustomVariable("b", 4.0);
        double result = evaluator.evaluateWithContext("a * b", context);
        assertEquals(20.0, result, 0.001);
    }

    @Test
    @DisplayName("除算テスト")
    void testDivision() throws Exception {
        context.setCustomVariable("a", 20.0);
        context.setCustomVariable("b", 4.0);
        double result = evaluator.evaluateWithContext("a / b", context);
        assertEquals(5.0, result, 0.001);
    }

    @Test
    @DisplayName("剰余テスト")
    void testModulo() throws Exception {
        context.setCustomVariable("a", 17.0);
        context.setCustomVariable("b", 5.0);
        double result = evaluator.evaluateWithContext("a % b", context);
        assertEquals(2.0, result, 0.001);
    }

    @Test
    @DisplayName("べき乗テスト")
    void testPower() throws Exception {
        context.setCustomVariable("a", 2.0);
        context.setCustomVariable("b", 3.0);
        double result = evaluator.evaluateWithContext("a ^ b", context);
        assertEquals(8.0, result, 0.001);
    }

    // ===== 複合式テスト =====

    @Test
    @DisplayName("複合式テスト")
    void testComplexExpression() throws Exception {
        context.setCustomVariable("STR", 50.0);
        context.setCustomVariable("DEX", 30.0);
        context.setCustomVariable("Lv", 5.0);

        double result = evaluator.evaluateWithContext("STR * DEX + (Lv * 5)", context);
        assertEquals(1500.0 + 25.0, result, 0.001);
    }

    @Test
    @DisplayName("括弧の優先順位テスト")
    void testParentheses() throws Exception {
        double result1 = evaluator.evaluateWithContext("2 + 3 * 4", context);
        assertEquals(14.0, result1, 0.001);

        double result2 = evaluator.evaluateWithContext("(2 + 3) * 4", context);
        assertEquals(20.0, result2, 0.001);
    }

    @Test
    @DisplayName("演算子の結合順序テスト")
    void testOperatorPrecedence() throws Exception {
        double result = evaluator.evaluateWithContext("2 + 3 * 4 - 6 / 2", context);
        assertEquals(2.0 + 12.0 - 3.0, result, 0.001);
    }

    // ===== 予約変数テスト =====

    @Test
    @DisplayName("スキルレベル変数テスト")
    void testSkillLevelVariable() throws Exception {
        context.setSkillLevel(10);
        double result = evaluator.evaluateWithContext("Lv * 5", context);
        assertEquals(50.0, result, 0.001);
    }

    // ===== 比較演算子テスト =====

    @Test
    @DisplayName("等値演算子テスト")
    void testEqualOperator() throws Exception {
        double result = evaluator.evaluateWithContext("5 == 5", context);
        assertEquals(1.0, result, 0.001);

        double result2 = evaluator.evaluateWithContext("5 == 3", context);
        assertEquals(0.0, result2, 0.001);
    }

    @Test
    @DisplayName("不等演算子テスト")
    void testNotEqualOperator() throws Exception {
        double result = evaluator.evaluateWithContext("5 != 3", context);
        assertEquals(1.0, result, 0.001);

        double result2 = evaluator.evaluateWithContext("5 != 5", context);
        assertEquals(0.0, result2, 0.001);
    }

    @Test
    @DisplayName("大小比較演算子テスト")
    void testComparisonOperators() throws Exception {
        assertEquals(1.0, evaluator.evaluateWithContext("5 > 3", context), 0.001);
        assertEquals(0.0, evaluator.evaluateWithContext("3 > 5", context), 0.001);
        assertEquals(1.0, evaluator.evaluateWithContext("3 < 5", context), 0.001);
        assertEquals(0.0, evaluator.evaluateWithContext("5 < 3", context), 0.001);
        assertEquals(1.0, evaluator.evaluateWithContext("5 >= 5", context), 0.001);
        assertEquals(1.0, evaluator.evaluateWithContext("5 <= 5", context), 0.001);
    }

    // ===== 論理演算子テスト =====

    @Test
    @DisplayName("論理AND演算子テスト")
    void testLogicalAnd() throws Exception {
        assertEquals(1.0, evaluator.evaluateWithContext("1 && 1", context), 0.001);
        assertEquals(0.0, evaluator.evaluateWithContext("1 && 0", context), 0.001);
        assertEquals(0.0, evaluator.evaluateWithContext("0 && 1", context), 0.001);
        assertEquals(0.0, evaluator.evaluateWithContext("0 && 0", context), 0.001);
    }

    @Test
    @DisplayName("論理OR演算子テスト")
    void testLogicalOr() throws Exception {
        assertEquals(1.0, evaluator.evaluateWithContext("1 || 1", context), 0.001);
        assertEquals(1.0, evaluator.evaluateWithContext("1 || 0", context), 0.001);
        assertEquals(1.0, evaluator.evaluateWithContext("0 || 1", context), 0.001);
        assertEquals(0.0, evaluator.evaluateWithContext("0 || 0", context), 0.001);
    }

    @Test
    @DisplayName("論理NOT演算子テスト")
    void testLogicalNot() throws Exception {
        assertEquals(0.0, evaluator.evaluateWithContext("!1", context), 0.001);
        assertEquals(1.0, evaluator.evaluateWithContext("!0", context), 0.001);
    }

    // ===== エラーハンドリングテスト =====

    @Test
    @DisplayName("未定義変数エラーテスト")
    void testUndefinedVariable() {
        assertThrows(FormulaEvaluator.FormulaEvaluationException.class,
                () -> evaluator.evaluateWithContext("undefined_var + 1", context));
    }

    @Test
    @DisplayName("ゼロ除算エラーテスト")
    void testDivisionByZero() {
        assertThrows(FormulaEvaluator.FormulaEvaluationException.class,
                () -> evaluator.evaluateWithContext("10 / 0", context));
    }

    @Test
    @DisplayName("空文字列テスト")
    void testEmptyString() throws Exception {
        double result = evaluator.evaluateWithContext("", context);
        assertEquals(0.0, result, 0.001);

        double result2 = evaluator.evaluateWithContext("   ", context);
        assertEquals(0.0, result2, 0.001);
    }

    @Test
    @DisplayName("安全評価テスト")
    void testEvaluateSafe() {
        double result = evaluator.evaluateSafe("2 + 3 * 4", null, 1, -1.0);
        assertEquals(14.0, result, 0.001);

        // エラー時のデフォルト値
        double result2 = evaluator.evaluateSafe("undefined + 1", null, 1, -999.0);
        assertEquals(-999.0, result2, 0.001);
    }

    // ===== レベル別数式テスト =====

    @Test
    @DisplayName("レベル別数式テスト")
    void testLevelBasedFormulas() throws Exception {
        Map<Integer, String> levelFormulas = new HashMap<>();
        levelFormulas.put(1, "10");
        levelFormulas.put(3, "20");
        levelFormulas.put(5, "30");
        String fallback = "100";

        // レベル1 → 直接一致
        double result1 = evaluator.evaluateLevelBased(levelFormulas, fallback, null, 1);
        assertEquals(10.0, result1, 0.001);

        // レベル2 → 最大の以下レベル（レベル1）
        double result2 = evaluator.evaluateLevelBased(levelFormulas, fallback, null, 2);
        assertEquals(10.0, result2, 0.001);

        // レベル5 → 直接一致
        double result5 = evaluator.evaluateLevelBased(levelFormulas, fallback, null, 5);
        assertEquals(30.0, result5, 0.001);

        // レベル10 → 最大の以下レベル（レベル5）
        double result10 = evaluator.evaluateLevelBased(levelFormulas, fallback, null, 10);
        assertEquals(30.0, result10, 0.001);
    }

    // ===== 単項演算子テスト =====

    @Test
    @DisplayName("単項マイナステスト")
    void testUnaryMinus() throws Exception {
        double result = evaluator.evaluateWithContext("-5", context);
        assertEquals(-5.0, result, 0.001);

        double result2 = evaluator.evaluateWithContext("- (3 + 2)", context);
        assertEquals(-5.0, result2, 0.001);
    }

    @Test
    @DisplayName("単項プラスステスト")
    void testUnaryPlus() throws Exception {
        double result = evaluator.evaluateWithContext("+5", context);
        assertEquals(5.0, result, 0.001);
    }

    // ===== 数値リテラルテスト =====

    @Test
    @DisplayName("整数リテラルテスト")
    void testIntegerLiteral() throws Exception {
        double result = evaluator.evaluateWithContext("42", context);
        assertEquals(42.0, result, 0.001);
    }

    @Test
    @DisplayName("小数リテラルテスト")
    void testDecimalLiteral() throws Exception {
        double result = evaluator.evaluateWithContext("3.14", context);
        assertEquals(3.14, result, 0.001);

        double result2 = evaluator.evaluateWithContext("0.5", context);
        assertEquals(0.5, result2, 0.001);
    }

    // ===== 検証テスト =====

    @Test
    @DisplayName("数式検証テスト")
    void testValidation() {
        assertTrue(evaluator.validate("STR * DEX + Lv * 5"));
        assertTrue(evaluator.validate("(10 + 20) * 2"));
        assertFalse(evaluator.validate("undefined_var + 1"));
        assertFalse(evaluator.validate(""));
        assertFalse(evaluator.validate(null));
    }

    // ===== キャッシュテスト =====

    @Test
    @DisplayName("キャッシュ機能テスト")
    void testCache() {
        assertEquals(0, evaluator.getCacheSize());

        try {
            evaluator.evaluate("10 + 20", null, 1);
            assertTrue(evaluator.getCacheSize() > 0);
        } catch (Exception e) {
            fail("評価で例外が発生: " + e.getMessage());
        }

        evaluator.clearCache();
        assertEquals(0, evaluator.getCacheSize());
    }

    // ===== 実用的なダメージ計算テスト =====

    @Test
    @DisplayName("実用的なダメージ計算テスト")
    void testPracticalDamageCalculation() throws Exception {
        // STR=50, DEX=30, Lv=5 のプレイヤー
        context.setCustomVariable("STR", 50.0);
        context.setCustomVariable("DEX", 30.0);
        context.setCustomVariable("Lv", 5.0);

        // 基本的なダメージ式
        double result1 = evaluator.evaluateWithContext("STR * 2 + DEX + Lv * 10", context);
        assertEquals(100.0 + 30.0 + 50.0, result1, 0.001);

        // クリティカル補正を含む式
        context.setCustomVariable("is_critical", 1.0);
        double result2 = evaluator.evaluateWithContext("(STR * 2 + DEX) * (1 + is_critical * 0.5)", context);
        assertEquals(130.0 * 1.5, result2, 0.001);

        // 条件分岐的なダメージ計算
        double result3 = evaluator.evaluateWithContext("(Lv >= 5) * 100 + (Lv < 5) * 50", context);
        assertEquals(100.0, result3, 0.001);
    }
}
