package com.example.rpgplugin.skill.evaluator;

import java.util.ArrayList;
import java.util.List;

/**
 * 数式パーサー
 *
 * <p>文字列から数式をトークン化・解析し、評価可能な形式に変換します。</p>
 * <p>サポートされる演算子:</p>
 * <ul>
 *   <li>算術演算子: +, -, *, /, %, ^</li>
 *   <li>括弧: (, )</li>
 *   <li>比較演算子: &lt;, &lt;=, &gt;, &gt;=, ==, !=</li>
 *   <li>論理演算子: &amp;&amp;, ||, !</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: パース処理の単一責務</li>
 *   <li>KISS: 再帰的降下パーサーによるシンプルな実装</li>
 *   <li>DRY: トークン処理ロジックの一元化</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExpressionParser {

    /** トークンの種別 */
    private enum TokenType {
        /** 数値リテラル */
        NUMBER,
        /** 変数名 */
        VARIABLE,
        /** 加算 */
        PLUS,
        /** 減算 */
        MINUS,
        /** 乗算 */
        MULTIPLY,
        /** 除算 */
        DIVIDE,
        /** 剰余 */
        MODULO,
        /** べき乗 */
        POWER,
        /** 左括弧 */
        LEFT_PAREN,
        /** 右括弧 */
        RIGHT_PAREN,
        /** 小なり */
        LESS_THAN,
        /** 以下 */
        LESS_EQUAL,
        /** 大なり */
        GREATER_THAN,
        /** 以上 */
        GREATER_EQUAL,
        /** 等値 */
        EQUAL,
        /** 不等 */
        NOT_EQUAL,
        /** 論理積 */
        LOGICAL_AND,
        /** 論理和 */
        LOGICAL_OR,
        /** 論理否定 */
        LOGICAL_NOT,
        /** コンマ */
        COMMA,
        /** 終端 */
        EOF
    }

    /** トークン */
    private static class Token {
        final TokenType type;
        final String value;
        final int position;

        Token(TokenType type, String value, int position) {
            this.type = type;
            this.value = value;
            this.position = position;
        }

        @Override
        public String toString() {
            return "Token{" + type + "='" + value + '\'' + '}';
        }
    }

    /** 解析対象の数式 */
    private final String expression;

    /** 現在の位置 */
    private int position;

    /** トークンリスト */
    private final List<Token> tokens;

    /** 現在のトークンインデックス */
    private int tokenIndex;

    /**
     * コンストラクタ
     *
     * @param expression 数式文字列
     */
    public ExpressionParser(String expression) {
        this.expression = expression != null ? expression.trim() : "";
        this.position = 0;
        this.tokens = new ArrayList<>();
        this.tokenIndex = 0;
    }

    /**
     * 数式をパースして評価結果を取得します
     *
     * @param context 変数コンテキスト
     * @return 評価結果
     * @throws FormulaEvaluationException 評価エラー
     */
    public double evaluate(VariableContext context) throws FormulaEvaluationException {
        if (expression.isEmpty()) {
            return 0.0;
        }

        tokenize();
        tokenIndex = 0;

        double result = parseExpression(context);

        // EOFトークン以外が残っていないかチェック
        while (tokenIndex < tokens.size()) {
            Token token = tokens.get(tokenIndex);
            if (token.type != TokenType.EOF) {
                throw new FormulaEvaluationException(
                        "予期しないトークン: " + token.value + " (位置: " + token.position + ")");
            }
            tokenIndex++;
        }

        return result;
    }

    /**
     * トークン化を実行します
     *
     * @throws FormulaEvaluationException 字句解析エラー
     */
    private void tokenize() throws FormulaEvaluationException {
        tokens.clear();

        while (position < expression.length()) {
            char current = expression.charAt(position);

            // スキップ文字
            if (Character.isWhitespace(current)) {
                position++;
                continue;
            }

            // 数値
            if (Character.isDigit(current) || current == '.') {
                tokens.add(readNumber());
                continue;
            }

            // 変数名または識別子
            if (Character.isLetter(current) || current == '_') {
                tokens.add(readIdentifier());
                continue;
            }

            // 演算子・括弧
            switch (current) {
                case '+':
                    tokens.add(new Token(TokenType.PLUS, "+", position));
                    position++;
                    break;

                case '-':
                    tokens.add(new Token(TokenType.MINUS, "-", position));
                    position++;
                    break;

                case '*':
                    if (position + 1 < expression.length() && expression.charAt(position + 1) == '*') {
                        tokens.add(new Token(TokenType.POWER, "**", position));
                        position += 2;
                    } else if (position + 1 < expression.length() && expression.charAt(position + 1) == '/') {
                        // コメント
                        position += 2;
                        while (position < expression.length() && expression.charAt(position) != '\n') {
                            position++;
                        }
                    } else {
                        tokens.add(new Token(TokenType.MULTIPLY, "*", position));
                        position++;
                    }
                    break;

                case '/':
                    if (position + 1 < expression.length() && expression.charAt(position + 1) == '/') {
                        // コメント（行末まで）
                        position += 2;
                        while (position < expression.length() && expression.charAt(position) != '\n') {
                            position++;
                        }
                    } else {
                        tokens.add(new Token(TokenType.DIVIDE, "/", position));
                        position++;
                    }
                    break;

                case '%':
                    tokens.add(new Token(TokenType.MODULO, "%", position));
                    position++;
                    break;

                case '^':
                    tokens.add(new Token(TokenType.POWER, "^", position));
                    position++;
                    break;

                case '(':
                    tokens.add(new Token(TokenType.LEFT_PAREN, "(", position));
                    position++;
                    break;

                case ')':
                    tokens.add(new Token(TokenType.RIGHT_PAREN, ")", position));
                    position++;
                    break;

                case ',':
                    tokens.add(new Token(TokenType.COMMA, ",", position));
                    position++;
                    break;

                case '=':
                    if (position + 1 < expression.length() && expression.charAt(position + 1) == '=') {
                        tokens.add(new Token(TokenType.EQUAL, "==", position));
                        position += 2;
                    } else {
                        throw new FormulaEvaluationException("無効な演算子: '=' (位置: " + position + ")");
                    }
                    break;

                case '!':
                    if (position + 1 < expression.length() && expression.charAt(position + 1) == '=') {
                        tokens.add(new Token(TokenType.NOT_EQUAL, "!=", position));
                        position += 2;
                    } else {
                        tokens.add(new Token(TokenType.LOGICAL_NOT, "!", position));
                        position++;
                    }
                    break;

                case '<':
                    if (position + 1 < expression.length() && expression.charAt(position + 1) == '=') {
                        tokens.add(new Token(TokenType.LESS_EQUAL, "<=", position));
                        position += 2;
                    } else {
                        tokens.add(new Token(TokenType.LESS_THAN, "<", position));
                        position++;
                    }
                    break;

                case '>':
                    if (position + 1 < expression.length() && expression.charAt(position + 1) == '=') {
                        tokens.add(new Token(TokenType.GREATER_EQUAL, ">=", position));
                        position += 2;
                    } else {
                        tokens.add(new Token(TokenType.GREATER_THAN, ">", position));
                        position++;
                    }
                    break;

                case '&':
                    if (position + 1 < expression.length() && expression.charAt(position + 1) == '&') {
                        tokens.add(new Token(TokenType.LOGICAL_AND, "&&", position));
                        position += 2;
                    } else {
                        throw new FormulaEvaluationException("無効な演算子: '&' (位置: " + position + ")");
                    }
                    break;

                case '|':
                    if (position + 1 < expression.length() && expression.charAt(position + 1) == '|') {
                        tokens.add(new Token(TokenType.LOGICAL_OR, "||", position));
                        position += 2;
                    } else {
                        throw new FormulaEvaluationException("無効な演算子: '|' (位置: " + position + ")");
                    }
                    break;

                default:
                    throw new FormulaEvaluationException("不明な文字: '" + current + "' (位置: " + position + ")");
            }
        }

        tokens.add(new Token(TokenType.EOF, "", position));
    }

    /**
     * 数値トークンを読み込みます
     *
     * @return 数値トークン
     */
    private Token readNumber() {
        int start = position;
        boolean hasDecimal = false;

        while (position < expression.length()) {
            char c = expression.charAt(position);
            if (c == '.') {
                if (hasDecimal) {
                    break;
                }
                hasDecimal = true;
            } else if (!Character.isDigit(c)) {
                break;
            }
            position++;
        }

        return new Token(TokenType.NUMBER, expression.substring(start, position), start);
    }

    /**
     * 識別子トークンを読み込みます
     *
     * @return 識別子トークン
     */
    private Token readIdentifier() {
        int start = position;

        while (position < expression.length()) {
            char c = expression.charAt(position);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                break;
            }
            position++;
        }

        return new Token(TokenType.VARIABLE, expression.substring(start, position), start);
    }

    /**
     * 現在のトークンを取得します
     *
     * @return 現在のトークン
     */
    private Token currentToken() {
        if (tokenIndex < tokens.size()) {
            return tokens.get(tokenIndex);
        }
        return new Token(TokenType.EOF, "", -1);
    }

    /**
     * トークンを消費して進めます
     *
     * @return 消費前のトークン
     */
    private Token consumeToken() {
        Token token = currentToken();
        tokenIndex++;
        return token;
    }

    /**
     * 期待するトークンタイプを消費します
     *
     * @param expectedType 期待するトークンタイプ
     * @return 消費したトークン
     * @throws FormulaEvaluationException トークンタイプが一致しない場合
     */
    private Token expectToken(TokenType expectedType) throws FormulaEvaluationException {
        Token token = currentToken();
        if (token.type != expectedType) {
            throw new FormulaEvaluationException(
                    "トークンタイプ不一致: 期待=" + expectedType + ", 実際=" + token.type +
                    " (位置: " + token.position + ")");
        }
        return consumeToken();
    }

    // ===== 解析メソッド =====

    /**
     * 式を解析します（論理ORレベル）
     */
    private double parseExpression(VariableContext context) throws FormulaEvaluationException {
        return parseLogicalOr(context);
    }

    /**
     * 論理和を解析します
     */
    private double parseLogicalOr(VariableContext context) throws FormulaEvaluationException {
        double left = parseLogicalAnd(context);

        while (currentToken().type == TokenType.LOGICAL_OR) {
            consumeToken();
            double right = parseLogicalAnd(context);
            left = (isTruthy(left) || isTruthy(right)) ? 1.0 : 0.0;
        }

        return left;
    }

    /**
     * 論理積を解析します
     */
    private double parseLogicalAnd(VariableContext context) throws FormulaEvaluationException {
        double left = parseComparison(context);

        while (currentToken().type == TokenType.LOGICAL_AND) {
            consumeToken();
            double right = parseComparison(context);
            left = (isTruthy(left) && isTruthy(right)) ? 1.0 : 0.0;
        }

        return left;
    }

    /**
     * 比較を解析します
     */
    private double parseComparison(VariableContext context) throws FormulaEvaluationException {
        double left = parseAdditive(context);

        while (true) {
            Token op = currentToken();
            switch (op.type) {
                case LESS_THAN:
                    consumeToken();
                    left = (left < parseAdditive(context)) ? 1.0 : 0.0;
                    break;

                case LESS_EQUAL:
                    consumeToken();
                    left = (left <= parseAdditive(context)) ? 1.0 : 0.0;
                    break;

                case GREATER_THAN:
                    consumeToken();
                    left = (left > parseAdditive(context)) ? 1.0 : 0.0;
                    break;

                case GREATER_EQUAL:
                    consumeToken();
                    left = (left >= parseAdditive(context)) ? 1.0 : 0.0;
                    break;

                case EQUAL:
                    consumeToken();
                    left = (Math.abs(left - parseAdditive(context)) < 1e-9) ? 1.0 : 0.0;
                    break;

                case NOT_EQUAL:
                    consumeToken();
                    left = (Math.abs(left - parseAdditive(context)) >= 1e-9) ? 1.0 : 0.0;
                    break;

                default:
                    return left;
            }
        }
    }

    /**
     * 加算・減算を解析します
     */
    private double parseAdditive(VariableContext context) throws FormulaEvaluationException {
        double left = parseMultiplicative(context);

        while (true) {
            Token op = currentToken();
            switch (op.type) {
                case PLUS:
                    consumeToken();
                    left += parseMultiplicative(context);
                    break;

                case MINUS:
                    consumeToken();
                    left -= parseMultiplicative(context);
                    break;

                default:
                    return left;
            }
        }
    }

    /**
     * 乗算・除算・剰余を解析します
     */
    private double parseMultiplicative(VariableContext context) throws FormulaEvaluationException {
        double left = parsePower(context);

        while (true) {
            Token op = currentToken();
            switch (op.type) {
                case MULTIPLY:
                    consumeToken();
                    left *= parsePower(context);
                    break;

                case DIVIDE:
                    consumeToken();
                    double divisor = parsePower(context);
                    if (divisor == 0.0) {
                        throw new FormulaEvaluationException("ゼロ除算エラー (位置: " + op.position + ")");
                    }
                    left /= divisor;
                    break;

                case MODULO:
                    consumeToken();
                    double modulus = parsePower(context);
                    if (modulus == 0.0) {
                        throw new FormulaEvaluationException("ゼロ剰余エラー (位置: " + op.position + ")");
                    }
                    left %= modulus;
                    break;

                default:
                    return left;
            }
        }
    }

    /**
     * べき乗を解析します
     */
    private double parsePower(VariableContext context) throws FormulaEvaluationException {
        double base = parseUnary(context);

        if (currentToken().type == TokenType.POWER) {
            consumeToken();
            double exponent = parsePower(context); // 右結合
            return Math.pow(base, exponent);
        }

        return base;
    }

    /**
     * 単項演算子を解析します
     */
    private double parseUnary(VariableContext context) throws FormulaEvaluationException {
        Token op = currentToken();

        switch (op.type) {
            case PLUS:
                consumeToken();
                return parsePrimary(context);

            case MINUS:
                consumeToken();
                return -parsePrimary(context);

            case LOGICAL_NOT:
                consumeToken();
                return isTruthy(parseUnary(context)) ? 0.0 : 1.0;

            default:
                return parsePrimary(context);
        }
    }

    /**
     * 基本要素を解析します
     */
    private double parsePrimary(VariableContext context) throws FormulaEvaluationException {
        Token token = currentToken();

        switch (token.type) {
            case NUMBER: {
                consumeToken();
                try {
                    return Double.parseDouble(token.value);
                } catch (NumberFormatException e) {
                    throw new FormulaEvaluationException("数値パースエラー: " + token.value);
                }
            }

            case VARIABLE: {
                consumeToken();
                Double value = context.getVariable(token.value);
                if (value == null) {
                    throw new FormulaEvaluationException(
                            "未定義の変数: " + token.value + " (位置: " + token.position + ")");
                }
                return value;
            }

            case LEFT_PAREN: {
                consumeToken();
                double result = parseExpression(context);
                expectToken(TokenType.RIGHT_PAREN);
                return result;
            }

            case EOF:
                throw new FormulaEvaluationException("予期しない式の終了");

            default:
                throw new FormulaEvaluationException(
                        "予期しないトークン: " + token.value + " (位置: " + token.position + ")");
        }
    }

    /**
     * 値が真か判定します
     *
     * @param value 判定する値
     * @return 非ゼロの場合はtrue
     */
    private boolean isTruthy(double value) {
        return Math.abs(value) > 1e-9;
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
