package com.example.rpgplugin.skill.evaluator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExpressionParserのテストクラス
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("ExpressionParser テスト")
class ExpressionParserTest {

    private VariableContext context;

    @BeforeEach
    void setUp() {
        context = new VariableContext(null, 5);
    }

    // ===== トークン化のテスト =====

    @Nested
    @DisplayName("トークン化のテスト")
    class TokenizationTests {

        @Test
        @DisplayName("単一演算子のトークン化")
        void testSingleOperatorTokenization() throws Exception {
            ExpressionParser parser = new ExpressionParser("1+2");
            double result = parser.evaluate(context);
            assertEquals(3.0, result, 0.001);
        }

        @Test
        @DisplayName("べき乗演算子(**)のトークン化")
        void testPowerOperatorTokenization() throws Exception {
            ExpressionParser parser = new ExpressionParser("2**3");
            double result = parser.evaluate(context);
            assertEquals(8.0, result, 0.001);
        }

        @Test
        @DisplayName("べき乗演算子(^)のトークン化")
        void testCaretPowerOperatorTokenization() throws Exception {
            ExpressionParser parser = new ExpressionParser("2^3");
            double result = parser.evaluate(context);
            assertEquals(8.0, result, 0.001);
        }

        @Test
        @DisplayName("比較演算子のトークン化")
        void testComparisonOperatorsTokenization() throws Exception {
            ExpressionParser parser = new ExpressionParser("5 >= 3");
            double result = parser.evaluate(context);
            assertEquals(1.0, result, 0.001);
        }

        @Test
        @DisplayName("等値演算子(==)のトークン化")
        void testEqualOperatorTokenization() throws Exception {
            ExpressionParser parser = new ExpressionParser("5 == 5");
            double result = parser.evaluate(context);
            assertEquals(1.0, result, 0.001);
        }

        @Test
        @DisplayName("不等演算子(!=)のトークン化")
        void testNotEqualOperatorTokenization() throws Exception {
            ExpressionParser parser = new ExpressionParser("5 != 3");
            double result = parser.evaluate(context);
            assertEquals(1.0, result, 0.001);
        }

        @Test
        @DisplayName("論理AND演算子のトークン化")
        void testLogicalAndTokenization() throws Exception {
            ExpressionParser parser = new ExpressionParser("1 && 1");
            double result = parser.evaluate(context);
            assertEquals(1.0, result, 0.001);
        }

        @Test
        @DisplayName("論理OR演算子のトークン化")
        void testLogicalOrTokenization() throws Exception {
            ExpressionParser parser = new ExpressionParser("0 || 1");
            double result = parser.evaluate(context);
            assertEquals(1.0, result, 0.001);
        }

        @Test
        @DisplayName("論理NOT演算子のトークン化")
        void testLogicalNotTokenization() throws Exception {
            ExpressionParser parser = new ExpressionParser("!0");
            double result = parser.evaluate(context);
            assertEquals(1.0, result, 0.001);
        }
    }

    // ===== エラーハンドリングのテスト =====

    @Nested
    @DisplayName("エラーハンドリングのテスト")
    class ErrorHandlingTests {

        @Test
        @DisplayName("無効な演算子(=)で例外")
        void testInvalidOperatorEquals() {
            ExpressionParser parser = new ExpressionParser("5 = 3");

            Exception exception = assertThrows(
                    ExpressionParser.FormulaEvaluationException.class,
                    () -> parser.evaluate(context)
            );
            assertTrue(exception.getMessage().contains("無効な演算子"));
            assertTrue(exception.getMessage().contains("="));
        }

        @Test
        @DisplayName("無効な演算子(&)で例外")
        void testInvalidOperatorAmpersand() {
            ExpressionParser parser = new ExpressionParser("5 & 3");

            Exception exception = assertThrows(
                    ExpressionParser.FormulaEvaluationException.class,
                    () -> parser.evaluate(context)
            );
            assertTrue(exception.getMessage().contains("無効な演算子"));
            assertTrue(exception.getMessage().contains("&"));
        }

        @Test
        @DisplayName("無効な演算子(|)で例外")
        void testInvalidOperatorPipe() {
            ExpressionParser parser = new ExpressionParser("5 | 3");

            Exception exception = assertThrows(
                    ExpressionParser.FormulaEvaluationException.class,
                    () -> parser.evaluate(context)
            );
            assertTrue(exception.getMessage().contains("無効な演算子"));
            assertTrue(exception.getMessage().contains("|"));
        }

        @Test
        @DisplayName("不明な文字で例外")
        void testUnknownCharacter() {
            ExpressionParser parser = new ExpressionParser("5 @ 3");

            Exception exception = assertThrows(
                    ExpressionParser.FormulaEvaluationException.class,
                    () -> parser.evaluate(context)
            );
            assertTrue(exception.getMessage().contains("不明な文字"));
            assertTrue(exception.getMessage().contains("@"));
        }

        @Test
        @DisplayName("未終了括弧で例外")
        void testUnclosedParenthesis() {
            ExpressionParser parser = new ExpressionParser("(5 + 3");

            // エラーが発生することを確認
            assertThrows(
                    ExpressionParser.FormulaEvaluationException.class,
                    () -> parser.evaluate(context)
            );
        }

        @Test
        @DisplayName("余分な閉じ括弧で例外")
        void testExtraClosingParenthesis() {
            ExpressionParser parser = new ExpressionParser("5 + 3)");

            Exception exception = assertThrows(
                    ExpressionParser.FormulaEvaluationException.class,
                    () -> parser.evaluate(context)
            );
            assertTrue(exception.getMessage().contains("予期しないトークン"));
        }

        @Test
        @DisplayName("予期しないトークンエラー")
        void testUnexpectedToken() {
            ExpressionParser parser = new ExpressionParser("5 + )");

            Exception exception = assertThrows(
                    ExpressionParser.FormulaEvaluationException.class,
                    () -> parser.evaluate(context)
            );
            assertTrue(exception.getMessage().contains("トークンタイプ不一致") ||
                       exception.getMessage().contains("予期しない"));
        }
    }

    // ===== コメントのテスト =====

    @Nested
    @DisplayName("コメント処理のテスト")
    class CommentTests {

        @Test
        @DisplayName("単一行コメント(//)の処理")
        void testSingleLineComment() throws Exception {
            ExpressionParser parser = new ExpressionParser("5 + 3 // これはコメント\n + 2");
            double result = parser.evaluate(context);
            assertEquals(10.0, result, 0.001);
        }

        @Test
        @DisplayName("行末までのコメント")
        void testCommentUntilEndOfLine() throws Exception {
            ExpressionParser parser = new ExpressionParser("5 + 3 // add 2");
            double result = parser.evaluate(context);
            assertEquals(8.0, result, 0.001);
        }

        @Test
        @DisplayName("複数行コメント")
        void testMultipleLinesWithComments() throws Exception {
            ExpressionParser parser = new ExpressionParser(
                    "1 + 2 // first comment\n" +
                    "+ 3 // second comment\n" +
                    "+ 4"
            );
            double result = parser.evaluate(context);
            assertEquals(10.0, result, 0.001);
        }
    }

    // ===== 数値リテラルのテスト =====

    @Nested
    @DisplayName("数値リテラルのテスト")
    class NumericLiteralTests {

        @Test
        @DisplayName("整数リテラル")
        void testIntegerLiteral() throws Exception {
            ExpressionParser parser = new ExpressionParser("42");
            double result = parser.evaluate(context);
            assertEquals(42.0, result, 0.001);
        }

        @Test
        @DisplayName("小数リテラル")
        void testDecimalLiteral() throws Exception {
            ExpressionParser parser = new ExpressionParser("3.14");
            double result = parser.evaluate(context);
            assertEquals(3.14, result, 0.001);
        }

        @Test
        @DisplayName("先頭ドットの小数")
        void testLeadingDotDecimal() throws Exception {
            ExpressionParser parser = new ExpressionParser(".5");
            double result = parser.evaluate(context);
            assertEquals(0.5, result, 0.001);
        }

        @Test
        @DisplayName("末尾ドットの数値")
        void testTrailingDotNumber() throws Exception {
            ExpressionParser parser = new ExpressionParser("5.");
            double result = parser.evaluate(context);
            assertEquals(5.0, result, 0.001);
        }

        @Test
        @DisplayName("複数の小数点はエラー")
        void testMultipleDecimalsError() {
            // "5.5.5"は最初の5.5まで読み込み、その後.5が別トークンとしてエラー
            ExpressionParser parser = new ExpressionParser("5.5.5");
            assertThrows(
                    ExpressionParser.FormulaEvaluationException.class,
                    () -> parser.evaluate(context)
            );
        }

        @Test
        @DisplayName("0のみの数値")
        void testZeroLiteral() throws Exception {
            ExpressionParser parser = new ExpressionParser("0");
            double result = parser.evaluate(context);
            assertEquals(0.0, result, 0.001);
        }

        @Test
        @DisplayName("大きな整数")
        void testLargeInteger() throws Exception {
            ExpressionParser parser = new ExpressionParser("123456789");
            double result = parser.evaluate(context);
            assertEquals(123456789.0, result, 0.001);
        }
    }

    // ===== 変数名のテスト =====

    @Nested
    @DisplayName("変数名のテスト")
    class VariableNameTests {

        @Test
        @DisplayName("単純な変数名")
        void testSimpleVariableName() throws Exception {
            ExpressionParser parser = new ExpressionParser("x");
            context.setCustomVariable("x", 10.0);
            double result = parser.evaluate(context);
            assertEquals(10.0, result, 0.001);
        }

        @Test
        @DisplayName("アンダースコア始まりの変数名")
        void testUnderscoreVariableName() throws Exception {
            ExpressionParser parser = new ExpressionParser("_private");
            context.setCustomVariable("_private", 5.0);
            double result = parser.evaluate(context);
            assertEquals(5.0, result, 0.001);
        }

        @Test
        @DisplayName("数字を含む変数名")
        void testVariableNameWithNumbers() throws Exception {
            ExpressionParser parser = new ExpressionParser("var123");
            context.setCustomVariable("var123", 7.0);
            double result = parser.evaluate(context);
            assertEquals(7.0, result, 0.001);
        }

        @Test
        @DisplayName("キャメルケース変数名")
        void testCamelCaseVariableName() throws Exception {
            ExpressionParser parser = new ExpressionParser("myVariable");
            context.setCustomVariable("myVariable", 3.0);
            double result = parser.evaluate(context);
            assertEquals(3.0, result, 0.001);
        }

        @Test
        @DisplayName("スネークケース変数名")
        void testSnakeCaseVariableName() throws Exception {
            ExpressionParser parser = new ExpressionParser("my_variable");
            context.setCustomVariable("my_variable", 9.0);
            double result = parser.evaluate(context);
            assertEquals(9.0, result, 0.001);
        }
    }

    // ===== 空白文字のテスト =====

    @Nested
    @DisplayName("空白文字処理のテスト")
    class WhitespaceTests {

        @Test
        @DisplayName("スペースのみの式")
        void testSpacesOnly() throws Exception {
            ExpressionParser parser = new ExpressionParser("   ");
            double result = parser.evaluate(context);
            assertEquals(0.0, result, 0.001);
        }

        @Test
        @DisplayName("タブ文字を含む式")
        void testTabsInExpression() throws Exception {
            ExpressionParser parser = new ExpressionParser("1\t+\t2");
            double result = parser.evaluate(context);
            assertEquals(3.0, result, 0.001);
        }

        @Test
        @DisplayName("改行を含む式")
        void testNewlinesInExpression() throws Exception {
            ExpressionParser parser = new ExpressionParser("1\n+\n2");
            double result = parser.evaluate(context);
            assertEquals(3.0, result, 0.001);
        }

        @Test
        @DisplayName("混在する空白文字")
        void testMixedWhitespace() throws Exception {
            ExpressionParser parser = new ExpressionParser("1 \t\n+\r 2");
            double result = parser.evaluate(context);
            assertEquals(3.0, result, 0.001);
        }
    }

    // ===== 複雑な式のテスト =====

    @Nested
    @DisplayName("複雑な式のテスト")
    class ComplexExpressionTests {

        @Test
        @DisplayName("深くネストした括弧")
        void testDeeplyNestedParentheses() throws Exception {
            ExpressionParser parser = new ExpressionParser("(((1 + 2) * 3) - 4)");
            double result = parser.evaluate(context);
            assertEquals(5.0, result, 0.001);
        }

        @Test
        @DisplayName("複数の比較演算子")
        void testMultipleComparisons() throws Exception {
            ExpressionParser parser = new ExpressionParser("5 > 3 && 3 < 10");
            double result = parser.evaluate(context);
            assertEquals(1.0, result, 0.001);
        }

        @Test
        @DisplayName("混合した論理演算子")
        void testMixedLogicalOperators() throws Exception {
            ExpressionParser parser = new ExpressionParser("1 || 0 && 1");
            double result = parser.evaluate(context);
            // ||より&&の優先順位が高い: 1 || (0 && 1) = 1 || 0 = 1
            assertEquals(1.0, result, 0.001);
        }

        @Test
        @DisplayName("べき乗の連続")
        void testConsecutivePowers() throws Exception {
            ExpressionParser parser = new ExpressionParser("2^3^2");
            double result = parser.evaluate(context);
            // 右結合: 2^(3^2) = 2^9 = 512
            assertEquals(512.0, result, 0.001);
        }

        @Test
        @DisplayName("単項演算子の連続（非サポート）")
        void testConsecutiveUnaryOperators() {
            ExpressionParser parser = new ExpressionParser("-+5");

            // 連続する単項演算子はサポートされていない
            assertThrows(
                    ExpressionParser.FormulaEvaluationException.class,
                    () -> parser.evaluate(context)
            );
        }

        @Test
        @DisplayName("単項NOTと比較")
        void testUnaryNotWithComparison() throws Exception {
            ExpressionParser parser = new ExpressionParser("!(5 > 3)");
            double result = parser.evaluate(context);
            assertEquals(0.0, result, 0.001);
        }
    }

    // ===== 境界値テスト =====

    @Nested
    @DisplayName("境界値のテスト")
    class BoundaryValueTests {

        @Test
        @DisplayName("ゼロ除算のチェック")
        void testDivisionByZeroCheck() {
            ExpressionParser parser = new ExpressionParser("10 / 0");
            assertThrows(
                    ExpressionParser.FormulaEvaluationException.class,
                    () -> parser.evaluate(context)
            );
        }

        @Test
        @DisplayName("ゼロでの剰余")
        void testModuloByZero() {
            ExpressionParser parser = new ExpressionParser("10 % 0");
            assertThrows(
                    ExpressionParser.FormulaEvaluationException.class,
                    () -> parser.evaluate(context)
            );
        }

        @Test
        @DisplayName("未定義変数へのアクセス")
        void testUndefinedVariableAccess() {
            ExpressionParser parser = new ExpressionParser("undefined_var + 1");
            assertThrows(
                    ExpressionParser.FormulaEvaluationException.class,
                    () -> parser.evaluate(context)
            );
        }
    }

    // ===== null・空文字列のテスト =====

    @Nested
    @DisplayName("null・空文字列のテスト")
    class NullEmptyTests {

        @Test
        @DisplayName("null式で0を返す")
        void testNullExpression() throws Exception {
            ExpressionParser parser = new ExpressionParser(null);
            double result = parser.evaluate(context);
            assertEquals(0.0, result, 0.001);
        }

        @Test
        @DisplayName("空文字列で0を返す")
        void testEmptyExpression() throws Exception {
            ExpressionParser parser = new ExpressionParser("");
            double result = parser.evaluate(context);
            assertEquals(0.0, result, 0.001);
        }

        @Test
        @DisplayName("空白のみで0を返す")
        void testWhitespaceOnlyExpression() throws Exception {
            ExpressionParser parser = new ExpressionParser("   \t\n   ");
            double result = parser.evaluate(context);
            assertEquals(0.0, result, 0.001);
        }

        @Test
        @DisplayName("trim後の空文字列")
        void testTrimmedEmptyExpression() throws Exception {
            ExpressionParser parser = new ExpressionParser("   ");
            double result = parser.evaluate(context);
            assertEquals(0.0, result, 0.001);
        }
    }

    // ===== 特殊演算子組み合わせのテスト =====

    @Nested
    @DisplayName("特殊演算子組み合わせのテスト")
    class SpecialOperatorCombinationTests {

        @Test
        @DisplayName("べき乗と単項マイナス")
        void testPowerWithUnaryMinus() throws Exception {
            ExpressionParser parser = new ExpressionParser("-2^2");
            double result = parser.evaluate(context);
            // パーサーの実装では(-2)^2 = 4
            assertEquals(4.0, result, 0.001);
        }

        @Test
        @DisplayName("括弧付きべき乗と単項マイナス")
        void testPowerWithUnaryMinusInParentheses() throws Exception {
            ExpressionParser parser = new ExpressionParser("(-2)^2");
            double result = parser.evaluate(context);
            // (-2)^2 = 4
            assertEquals(4.0, result, 0.001);
        }

        @Test
        @DisplayName("括弧でべき乗の優先順位を変更")
        void testPowerPriorityWithParens() throws Exception {
            ExpressionParser parser = new ExpressionParser("-(2^2)");
            double result = parser.evaluate(context);
            // -(2^2) = -4
            assertEquals(-4.0, result, 0.001);
        }

        @Test
        @DisplayName("カンマ区切り（将来の関数用）")
        void testCommaSeparator() throws Exception {
            ExpressionParser parser = new ExpressionParser("1, 2");

            // カンマはトークン化されるが、使用されない
            // エラーまたは予期しないトークンとして処理される
            assertThrows(
                    ExpressionParser.FormulaEvaluationException.class,
                    () -> parser.evaluate(context)
            );
        }
    }

    // ===== 予約変数のテスト =====

    @Nested
    @DisplayName("予約変数のテスト")
    class ReservedVariableTests {

        @Test
        @DisplayName("Lv変数の使用")
        void testLvVariable() throws Exception {
            ExpressionParser parser = new ExpressionParser("Lv * 10");
            double result = parser.evaluate(context);
            assertEquals(50.0, result, 0.001);
        }

        @Test
        @DisplayName("LV変数の使用")
        void testLVCapitalVariable() throws Exception {
            ExpressionParser parser = new ExpressionParser("LV + 1");
            double result = parser.evaluate(context);
            // nullプレイヤーなのでデフォルト1
            assertEquals(2.0, result, 0.001);
        }

        @Test
        @DisplayName("カスタム変数でLvを上書き")
        void testOverrideLvWithCustomVariable() throws Exception {
            ExpressionParser parser = new ExpressionParser("Lv * 10");
            context.setCustomVariable("Lv", 100.0);
            double result = parser.evaluate(context);
            assertEquals(1000.0, result, 0.001);
        }
    }
}
