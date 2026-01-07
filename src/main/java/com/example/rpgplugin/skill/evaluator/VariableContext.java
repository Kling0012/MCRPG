package com.example.rpgplugin.skill.evaluator;

import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 数式評価用の変数コンテキスト
 *
 * <p>数式内で使用可能な変数を管理します。</p>
 * <p>サポートされる変数:</p>
 * <ul>
 *   <li>ステータス: STR, INT, SPI, VIT, DEX</li>
 *   <li>スキルレベル: Lv</li>
 *   <li>プレイヤーレベル: LV</li>
 *   <li>カスタム変数: YAML等で定義された独自変数</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: 変数管理の単一責務</li>
 *   <li>DRY: 変数参照ロジックの一元化</li>
 *   <li>KISS: シンプルなMapベースの構造</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class VariableContext {

    private static final Logger LOGGER = Logger.getLogger(VariableContext.class.getName());

    /** スキルレベル変数名 */
    public static final String SKILL_LEVEL = "Lv";

    /** プレイヤーレベル変数名 */
    public static final String PLAYER_LEVEL = "LV";

    /** カスタム変数を格納するMap */
    private final Map<String, Double> customVariables;

    /** RPGプレイヤーインスタンス（ステータス取得用） */
    private final RPGPlayer rpgPlayer;

    /** 現在のスキルレベル */
    private int skillLevel;

    /**
     * コンストラクタ
     *
     * @param rpgPlayer RPGプレイヤー（ステータス取得用）
     */
    public VariableContext(RPGPlayer rpgPlayer) {
        this.rpgPlayer = rpgPlayer;
        this.customVariables = new ConcurrentHashMap<>();
        this.skillLevel = 1;
    }

    /**
     * コンストラクタ（スキルレベル指定）
     *
     * @param rpgPlayer RPGプレイヤー（ステータス取得用）
     * @param skillLevel スキルレベル
     */
    public VariableContext(RPGPlayer rpgPlayer, int skillLevel) {
        this.rpgPlayer = rpgPlayer;
        this.customVariables = new ConcurrentHashMap<>();
        this.skillLevel = skillLevel;
    }

    /**
     * 変数値を取得します
     *
     * <p>優先順位:</p>
     * <ol>
     *   <li>カスタム変数</li>
     *   <li>予約変数（Lv, LV）</li>
     *   <li>ステータス変数（STR, INT, SPI, VIT, DEX）</li>
     * </ol>
     *
     * @param variableName 変数名
     * @return 変数値、見つからない場合はnull
     */
    public Double getVariable(String variableName) {
        if (variableName == null || variableName.isEmpty()) {
            return null;
        }

        // カスタム変数を優先
        Double customValue = customVariables.get(variableName);
        if (customValue != null) {
            return customValue;
        }

        // スキルレベル
        if (SKILL_LEVEL.equals(variableName)) {
            return (double) skillLevel;
        }

        // プレイヤーレベル
        if (PLAYER_LEVEL.equals(variableName)) {
            if (rpgPlayer != null) {
                return (double) rpgPlayer.getVanillaLevel();
            }
            return 1.0;
        }

        // ステータス変数
        if (rpgPlayer != null) {
            Stat stat = Stat.fromShortName(variableName);
            if (stat != null) {
                StatManager statManager = rpgPlayer.getStatManager();
                if (statManager != null) {
                    return (double) statManager.getFinalStat(stat);
                }
            }
        }

        return null;
    }

    /**
     * カスタム変数を設定します
     *
     * @param name 変数名
     * @param value 変数値
     */
    public void setCustomVariable(String name, double value) {
        customVariables.put(name, value);
    }

    /**
     * カスタム変数を一括設定します
     *
     * @param variables 変数マップ
     */
    public void setCustomVariables(Map<String, Double> variables) {
        if (variables != null) {
            customVariables.putAll(variables);
        }
    }

    /**
     * カスタム変数を設定します（数値パース版）
     *
     * @param name 変数名
     * @param value 文字列値（数値にパース可能であること）
     * @throws NumberFormatException 数値パースに失敗した場合
     */
    public void setCustomVariable(String name, String value) {
        double parsedValue = parseNumericString(value);
        customVariables.put(name, parsedValue);
    }

    /**
     * スキルレベルを設定します
     *
     * @param skillLevel スキルレベル
     */
    public void setSkillLevel(int skillLevel) {
        this.skillLevel = Math.max(1, skillLevel);
    }

    /**
     * スキルレベルを取得します
     *
     * @return スキルレベル
     */
    public int getSkillLevel() {
        return skillLevel;
    }

    /**
     * RPGプレイヤーを取得します
     *
     * @return RPGプレイヤー
     */
    public RPGPlayer getRPGPlayer() {
        return rpgPlayer;
    }

    /**
     * カスタム変数マップを取得します
     *
     * @return カスタム変数マップのコピー
     */
    public Map<String, Double> getCustomVariables() {
        return Map.copyOf(customVariables);
    }

    /**
     * カスタム変数をクリアします
     */
    public void clearCustomVariables() {
        customVariables.clear();
    }

    /**
     * 数値文字列をパースします
     *
     * <p>整数および小数形式に対応します。</p>
     *
     * @param value 数値文字列
     * @return パースされた数値
     * @throws NumberFormatException パースに失敗した場合
     */
    private double parseNumericString(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            LOGGER.warning("[VariableContext] 数値パース失敗: " + value);
            throw e;
        }
    }

    /**
     * 変数が存在するか確認します
     *
     * @param variableName 変数名
     * @return 存在する場合はtrue
     */
    public boolean hasVariable(String variableName) {
        return getVariable(variableName) != null;
    }

    @Override
    public String toString() {
        return "VariableContext{" +
                "skillLevel=" + skillLevel +
                ", customVariables=" + customVariables +
                ", rpgPlayer=" + (rpgPlayer != null ? rpgPlayer.getUsername() : "null") +
                '}';
    }
}
