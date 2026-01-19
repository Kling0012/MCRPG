package com.example.rpgplugin.skill.result;

import java.util.Collections;
import java.util.Map;

/**
 * スキル実行結果
 *
 * <p>スキル実行の結果を保持します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: 実行結果の単一責務</li>
 *   <li>Immutability: 不変オブジェクト</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillExecutionResult {

    /** 成功フラグ */
    private final boolean success;

    /** エラーメッセージ（失敗時） */
    private final String errorMessage;

    /** 計算されたダメージ */
    private final double damage;

    /** ヒットしたターゲット数 */
    private final int targetsHit;

    /** 消費したコスト */
    private final double costConsumed;

    /** カスタムデータ */
    private final Map<String, Object> customData;

    /**
     * コンストラクタ
     *
     * @param success 成功フラグ
     * @param errorMessage エラーメッセージ
     * @param damage ダメージ
     * @param targetsHit ターゲット数
     * @param costConsumed 消費コスト
     * @param customData カスタムデータ
     */
    private SkillExecutionResult(boolean success, String errorMessage, double damage,
                                 int targetsHit, double costConsumed, Map<String, Object> customData) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.damage = damage;
        this.targetsHit = targetsHit;
        this.costConsumed = costConsumed;
        // 空マップの場合は Map.of() を直接使用（コピー不要）
        this.customData = (customData == null || customData.isEmpty()) 
                ? Map.of() 
                : Map.copyOf(customData);
    }

    /**
     * 成功結果を作成します
     *
     * @return 成功結果
     */
    public static SkillExecutionResult success() {
        return new SkillExecutionResult(true, null, 0.0, 0, 0.0, null);
    }

    /**
     * ダメージ付きの成功結果を作成します
     *
     * @param damage ダメージ
     * @param targetsHit ターゲット数
     * @return 成功結果
     */
    public static SkillExecutionResult successWithDamage(double damage, int targetsHit) {
        return new SkillExecutionResult(true, null, damage, targetsHit, 0.0, null);
    }

    /**
     * コスト消費付きの成功結果を作成します
     *
     * @param damage ダメージ
     * @param targetsHit ターゲット数
     * @param costConsumed 消費コスト
     * @return 成功結果
     */
    public static SkillExecutionResult successWithCost(double damage, int targetsHit, double costConsumed) {
        return new SkillExecutionResult(true, null, damage, targetsHit, costConsumed, null);
    }

    /**
     * 失敗結果を作成します
     *
     * @param errorMessage エラーメッセージ
     * @return 失敗結果
     */
    public static SkillExecutionResult failure(String errorMessage) {
        return new SkillExecutionResult(false, errorMessage, 0.0, 0, 0.0, null);
    }

    /**
     * 成功かどうかを返します
     *
     * @return 成功の場合はtrue
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * エラーメッセージを返します
     *
     * @return エラーメッセージ、成功時はnull
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * ダメージを返します
     *
     * @return ダメージ
     */
    public double getDamage() {
        return damage;
    }

    /**
     * ターゲット数を返します
     *
     * @return ターゲット数
     */
    public int getTargetsHit() {
        return targetsHit;
    }

    /**
     * 消費コストを返します
     *
     * @return 消費コスト
     */
    public double getCostConsumed() {
        return costConsumed;
    }

    /**
     * カスタムデータを取得します
     *
     * @param key キー
     * @return 値、存在しない場合はnull
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomData(String key) {
        return (T) customData.get(key);
    }

    /**
     * カスタムデータを取得します
     *
     * @param key キー
     * @param defaultValue デフォルト値
     * @return 値、存在しない場合はデフォルト値
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomData(String key, T defaultValue) {
        Object value = customData.get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * すべてのカスタムデータを取得します
     *
     * @return カスタムデータマップの読み取りビュー
     */
    public Map<String, Object> getAllCustomData() {
        return Collections.unmodifiableMap(customData);
    }

    /**
     * カスタムデータ付きのビルダーを作成します
     *
     * @return ビルダー
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        if (success) {
            return "SkillExecutionResult{success=" + success +
                    ", damage=" + damage +
                    ", targetsHit=" + targetsHit +
                    ", costConsumed=" + costConsumed + '}';
        } else {
            return "SkillExecutionResult{success=" + success +
                    ", errorMessage='" + errorMessage + '\'' + '}';
        }
    }

    /**
     * ビルダークラス
     */
    public static class Builder {
        private boolean success = true;
        private String errorMessage;
        private double damage;
        private int targetsHit;
        private double costConsumed;
        private Map<String, Object> customData = new java.util.HashMap<>();

        /**
         * 成功フラグを設定します
         *
         * @param success 成功フラグ
         * @return このビルダー
         */
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        /**
         * エラーメッセージを設定します
         *
         * @param errorMessage エラーメッセージ
         * @return このビルダー
         */
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            this.success = false;
            return this;
        }

        /**
         * ダメージを設定します
         *
         * @param damage ダメージ
         * @return このビルダー
         */
        public Builder damage(double damage) {
            this.damage = damage;
            return this;
        }

        /**
         * ターゲット数を設定します
         *
         * @param targetsHit ターゲット数
         * @return このビルダー
         */
        public Builder targetsHit(int targetsHit) {
            this.targetsHit = targetsHit;
            return this;
        }

        /**
         * 消費コストを設定します
         *
         * @param costConsumed 消費コスト
         * @return このビルダー
         */
        public Builder costConsumed(double costConsumed) {
            this.costConsumed = costConsumed;
            return this;
        }

        /**
         * カスタムデータを追加します
         *
         * @param key キー
         * @param value 値
         * @return このビルダー
         */
        public Builder addCustomData(String key, Object value) {
            this.customData.put(key, value);
            return this;
        }

        /**
         * カスタムデータを一括設定します
         *
         * @param data データマップ
         * @return このビルダー
         */
        public Builder customData(Map<String, Object> data) {
            if (data != null) {
                this.customData.putAll(data);
            }
            return this;
        }

        /**
         * 結果を構築します
         *
         * @return 構築された結果
         */
        public SkillExecutionResult build() {
            return new SkillExecutionResult(success, errorMessage, damage, targetsHit, costConsumed, customData);
        }
    }
}
