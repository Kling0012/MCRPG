package com.example.rpgplugin.skill.calculator;

import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.skill.evaluator.FormulaDamageCalculator;
import com.example.rpgplugin.skill.evaluator.FormulaEvaluator;
import com.example.rpgplugin.skill.target.SkillTarget;
import com.example.rpgplugin.stats.Stat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * スキル計算ユーティリティ
 *
 * <p>Skillクラスから分離した計算ロジックを提供します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキル計算に専念</li>
 *   <li>DRY: 計算ロジックを一元管理</li>
 *   <li>KISS: シンプルな計算メソッド</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public final class SkillCalculator {

    private SkillCalculator() {
        // ユーティリティクラス: インスタンス化防止
    }

    // ==================== ダメージ計算 ====================

    /**
     * ダメージを計算します
     *
     * @param damage ダメージ計算設定
     * @param statValue ステータス値
     * @param skillLevel スキルレベル
     * @return 計算されたダメージ
     */
    public static double calculateDamage(Skill.DamageCalculation damage, double statValue, int skillLevel) {
        if (damage == null) {
            return 0.0;
        }
        return damage.getBase()
                + (statValue * damage.getMultiplierValue())
                + (skillLevel * damage.getLevelMultiplier());
    }

    /**
     * ステータスを考慮してダメージを計算します
     *
     * @param damage ダメージ計算設定
     * @param rpgPlayer RPGプレイヤー
     * @param skillLevel スキルレベル
     * @return 計算されたダメージ
     */
    public static double calculateDamage(Skill.DamageCalculation damage, RPGPlayer rpgPlayer, int skillLevel) {
        if (damage == null || damage.getStatMultiplier() == null) {
            return calculateDamage(damage, 0.0, skillLevel);
        }
        int statValue = rpgPlayer.getFinalStat(damage.getStatMultiplier());
        return calculateDamage(damage, statValue, skillLevel);
    }

    /**
     * 数式を使用してダメージを計算します
     *
     * @param formulaDamage 数式ダメージ設定
     * @param calculator 数式ダメージ計算機
     * @param rpgPlayer RPGプレイヤー
     * @param skillLevel スキルレベル
     * @return 計算されたダメージ、エラー時は0.0
     */
    public static double calculateFormulaDamage(
            Skill.FormulaDamageConfig formulaDamage,
            FormulaDamageCalculator calculator,
            RPGPlayer rpgPlayer,
            int skillLevel) {
        if (formulaDamage == null || calculator == null) {
            return 0.0;
        }

        // カスタム変数を設定
        if (calculator != null) {
            Map<String, Double> varMap = new HashMap<>();
            // カスタム変数があれば設定（呼び出し元で管理する想定）
            calculator.setCustomVariables(varMap);
        }

        try {
            return calculator.calculateDamage(rpgPlayer, skillLevel);
        } catch (Exception e) {
            return 0.0;
        }
    }

    // ==================== コスト計算 ====================

    /**
     * 指定レベルでのコストを計算します
     *
     * @param costParameter レベル依存コストパラメータ
     * @param manaCost フォールバックコスト値
     * @param level スキルレベル
     * @return コスト値
     */
    public static int calculateCost(
            com.example.rpgplugin.skill.LevelDependentParameter costParameter,
            int manaCost,
            int level) {
        if (costParameter != null) {
            return costParameter.getIntValue(level);
        }
        return manaCost;
    }

    /**
     * コスト設定からコストを計算します
     *
     * @param costConfig コスト設定
     * @param level スキルレベル
     * @return コスト値
     */
    public static int calculateCost(Skill.CostConfig costConfig, int level) {
        if (costConfig == null) {
            return 0;
        }
        return costConfig.getCost(level);
    }

    // ==================== クールダウン計算 ====================

    /**
     * 指定レベルでのクールダウンを計算します
     *
     * @param cooldownParameter レベル依存CDパラメータ
     * @param cooldown フォールバッククールダウン値
     * @param level スキルレベル
     * @return クールダウン（秒）
     */
    public static double calculateCooldown(
            com.example.rpgplugin.skill.LevelDependentParameter cooldownParameter,
            double cooldown,
            int level) {
        if (cooldownParameter != null) {
            return cooldownParameter.getValue(level);
        }
        return cooldown;
    }

    /**
     * クールダウン設定からクールダウンを計算します
     *
     * @param cooldownConfig クールダウン設定
     * @param level スキルレベル
     * @return クールダウン（秒）
     */
    public static double calculateCooldown(Skill.CooldownConfig cooldownConfig, int level) {
        if (cooldownConfig == null) {
            return 0.0;
        }
        return cooldownConfig.getCooldown(level);
    }

    // ==================== カスタム変数 ====================

    /**
     * カスタム変数をマップとして取得します
     *
     * @param variables カスタム変数定義リスト
     * @return 変数名と値のマップ
     */
    public static Map<String, Double> getVariableMap(List<Skill.VariableDefinition> variables) {
        Map<String, Double> map = new HashMap<>();
        if (variables != null) {
            for (Skill.VariableDefinition var : variables) {
                map.put(var.getName(), var.getValue());
            }
        }
        return map;
    }

    /**
     * カスタム変数が定義されているかチェックします
     *
     * @param variables カスタム変数定義リスト
     * @return カスタム変数がある場合はtrue
     */
    public static boolean hasVariables(List<Skill.VariableDefinition> variables) {
        return variables != null && !variables.isEmpty();
    }

    // ==================== 数式ダメージ計算機生成 ====================

    /**
     * 数式ダメージ計算機を作成します
     *
     * @param formulaDamage 数式ダメージ設定
     * @param evaluator 数式エバリュエーター
     * @return 数式ダメージ計算機、数式設定がない場合はnull
     */
    public static FormulaDamageCalculator createFormulaDamageCalculator(
            Skill.FormulaDamageConfig formulaDamage,
            FormulaEvaluator evaluator) {
        if (formulaDamage == null || evaluator == null) {
            return null;
        }

        String formula = formulaDamage.getFormula();
        Map<Integer, String> levelFormulas = formulaDamage.getLevelFormulas();

        return new FormulaDamageCalculator(evaluator, formula, levelFormulas, formula);
    }

    // ==================== コンポーネント関連の計算 ====================

    /**
     * 簡易数式を解析します
     *
     * <p>基本演算（+, -, *, /）と括弧に対応しています。</p>
     *
     * @param formula 数式
     * @return 計算結果
     */
    public static double parseSimpleFormula(String formula) {
        try {
            return new Object() {
                int pos = -1, ch;

                void nextChar() {
                    ch = (++pos < formula.length()) ? formula.charAt(pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (ch == ' ') nextChar();
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }

                double parse() {
                    nextChar();
                    double x = parseExpression();
                    if (pos < formula.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                    return x;
                }

                double parseExpression() {
                    double x = parseTerm();
                    for (;;) {
                        if (eat('+')) x += parseTerm();
                        else if (eat('-')) x -= parseTerm();
                        else return x;
                    }
                }

                double parseTerm() {
                    double x = parseFactor();
                    for (;;) {
                        if (eat('*')) x *= parseFactor();
                        else if (eat('/')) x /= parseFactor();
                        else return x;
                    }
                }

                double parseFactor() {
                    if (eat('+')) return parseFactor();
                    if (eat('-')) return -parseFactor();

                    double x;
                    int startPos = this.pos;
                    if (eat('(')) {
                        x = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        x = Double.parseDouble(formula.substring(startPos, this.pos));
                    } else {
                        throw new RuntimeException("Unexpected: " + (char) ch);
                    }

                    return x;
                }
            }.parse();
        } catch (Exception e) {
            try {
                return Double.parseDouble(formula);
            } catch (NumberFormatException ex) {
                return 0.0;
            }
        }
    }

    /**
     * レベル値を置換した数式を解析します
     *
     * @param formula 数式文字列
     * @param level スキルレベル
     * @return 計算結果
     */
    public static double parseFormulaWithLevel(String formula, int level) {
        String replaced = formula.replace("level", String.valueOf(level))
                .replace("Lv", String.valueOf(level));
        return parseSimpleFormula(replaced);
    }

    // ==================== ユーティリティ ====================

    /**
     * 指定されたクラスでこのスキルを使用可能かチェックします
     *
     * @param availableClasses 利用可能なクラスリスト
     * @param classId クラスID
     * @return 使用可能な場合はtrue
     */
    public static boolean isAvailableForClass(List<String> availableClasses, String classId) {
        if (availableClasses == null || availableClasses.isEmpty()) {
            return true;
        }
        return availableClasses.contains(classId);
    }

    /**
     * レガシーカラーコードをMiniMessage形式に変換します
     *
     * @param text 変換前のテキスト
     * @return MiniMessage形式のテキスト
     */
    public static String convertLegacyToMiniMessage(String text) {
        return text.replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&l", "<bold>")
                .replace("&o", "<italic>")
                .replace("&n", "<underline>")
                .replace("&m", "<strikethrough>")
                .replace("&k", "<obfuscated>")
                .replace("&r", "<reset>");
    }
}
