package com.example.rpgplugin.damage.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * ダメージイベント設定
 *
 * <p>個々のダメージイベント（skill_damage, physical_attack等）の設定を保持します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: イベント設定の単一責務</li>
 *   <li>DRY: 共通処理は親クラスに集約</li>
 *   <li>KISS: シンプルなImmutable構造</li>
 * </ul>
 *
 * @version 1.0.0
 * @author RPGPlugin Team
 */
public class EventConfig {

    /** 説明 */
    private final String description;

    /** イベント用定数 */
    private final Map<String, Object> constants;

    /** ダメージ計算式 */
    private final String formula;

    /** フォールバック計算式 */
    private final String fallbackFormula;

    /** 最小ダメージ */
    private final double minDamage;

    /** 最大ダメージ（オプション） */
    private final Double maxDamage;

    /** クリティカルヒット設定 */
    private final CriticalConfig critical;

    /** 防御カット計算式（damage_takenイベント用） */
    private final String physicalCutFormula;

    /** 魔法防御カット計算式（damage_takenイベント用） */
    private final String magicCutFormula;

    /**
     * コンストラクタ
     */
    private EventConfig(
            String description,
            Map<String, Object> constants,
            String formula,
            String fallbackFormula,
            double minDamage,
            Double maxDamage,
            CriticalConfig critical,
            String physicalCutFormula,
            String magicCutFormula) {
        this.description = description;
        this.constants = constants != null
                ? Collections.unmodifiableMap(new HashMap<>(constants))
                : Collections.emptyMap();
        this.formula = formula;
        this.fallbackFormula = fallbackFormula;
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
        this.critical = critical;
        this.physicalCutFormula = physicalCutFormula;
        this.magicCutFormula = magicCutFormula;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getConstants() {
        return new HashMap<>(constants);
    }

    public Object getConstant(String key) {
        return constants.get(key);
    }

    public String getFormula() {
        return formula;
    }

    public String getFallbackFormula() {
        return fallbackFormula;
    }

    public double getMinDamage() {
        return minDamage;
    }

    public Optional<Double> getMaxDamage() {
        return Optional.ofNullable(maxDamage);
    }

    public Optional<CriticalConfig> getCritical() {
        return Optional.ofNullable(critical);
    }

    public Optional<String> getPhysicalCutFormula() {
        return Optional.ofNullable(physicalCutFormula);
    }

    public Optional<String> getMagicCutFormula() {
        return Optional.ofNullable(magicCutFormula);
    }

    /**
     * YAMLセクションから構築します
     *
     * @param section YAMLセクション
     * @return EventConfigインスタンス
     */
    public static EventConfig fromSection(ConfigurationSection section) {
        if (section == null) {
            throw new IllegalArgumentException("Section cannot be null");
        }

        String description = section.getString("description", "");
        String formula = section.getString("formula", "");
        String fallbackFormula = section.getString("constants.fallback_formula",
                section.getString("fallback_formula", ""));
        double minDamage = section.getDouble("min_damage", 1.0);
        Double maxDamage = section.contains("max_damage")
                ? section.getDouble("max_damage")
                : null;

        // 定数を読み込み
        Map<String, Object> constants = new HashMap<>();
        ConfigurationSection constantsSection = section.getConfigurationSection("constants");
        if (constantsSection != null) {
            for (String key : constantsSection.getKeys(false)) {
                constants.put(key, constantsSection.get(key));
            }
        }

        // クリティカル設定を読み込み
        CriticalConfig critical = null;
        ConfigurationSection criticalSection = section.getConfigurationSection("critical");
        if (criticalSection != null && criticalSection.getBoolean("enabled", false)) {
            critical = new CriticalConfig(
                    criticalSection.getString("chance_formula", "CRITICAL_HIT_CHANCE"),
                    criticalSection.getString("multiplier_formula", "CRITICAL_HIT_MULTIPLIER")
            );
        }

        // 防御カット計算式（damage_taken用）
        String physicalCutFormula = section.getString("physical_cut_formula");
        String magicCutFormula = section.getString("magic_cut_formula");

        return new EventConfig(
                description,
                constants,
                formula,
                fallbackFormula,
                minDamage,
                maxDamage,
                critical,
                physicalCutFormula,
                magicCutFormula
        );
    }

    /**
     * ビルダー
     */
    public static class Builder {
        private String description;
        private final Map<String, Object> constants = new HashMap<>();
        private String formula;
        private String fallbackFormula;
        private double minDamage = 1.0;
        private Double maxDamage;
        private CriticalConfig critical;
        private String physicalCutFormula;
        private String magicCutFormula;

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder addConstant(String key, Object value) {
            constants.put(key, value);
            return this;
        }

        public Builder addConstants(Map<String, Object> constants) {
            if (constants != null) {
                this.constants.putAll(constants);
            }
            return this;
        }

        public Builder formula(String formula) {
            this.formula = formula;
            return this;
        }

        public Builder fallbackFormula(String fallbackFormula) {
            this.fallbackFormula = fallbackFormula;
            return this;
        }

        public Builder minDamage(double minDamage) {
            this.minDamage = minDamage;
            return this;
        }

        public Builder maxDamage(Double maxDamage) {
            this.maxDamage = maxDamage;
            return this;
        }

        public Builder critical(CriticalConfig critical) {
            this.critical = critical;
            return this;
        }

        public Builder critical(String chanceFormula, String multiplierFormula) {
            this.critical = new CriticalConfig(chanceFormula, multiplierFormula);
            return this;
        }

        public Builder physicalCutFormula(String formula) {
            this.physicalCutFormula = formula;
            return this;
        }

        public Builder magicCutFormula(String formula) {
            this.magicCutFormula = formula;
            return this;
        }

        public EventConfig build() {
            return new EventConfig(
                    description,
                    constants,
                    formula,
                    fallbackFormula,
                    minDamage,
                    maxDamage,
                    critical,
                    physicalCutFormula,
                    magicCutFormula
            );
        }
    }

    /**
     * クリティカルヒット設定
     */
    public static class CriticalConfig {

        /** クリティカル発動率計算式 */
        private final String chanceFormula;

        /** クリティカル倍率計算式 */
        private final String multiplierFormula;

        /**
         * コンストラクタ
         *
         * @param chanceFormula クリティカル発動率計算式
         * @param multiplierFormula クリティカル倍率計算式
         */
        private CriticalConfig(String chanceFormula, String multiplierFormula) {
            this.chanceFormula = chanceFormula;
            this.multiplierFormula = multiplierFormula;
        }

        public String getChanceFormula() {
            return chanceFormula;
        }

        public String getMultiplierFormula() {
            return multiplierFormula;
        }
    }

    @Override
    public String toString() {
        return "EventConfig{" +
                "description='" + description + '\'' +
                ", formula='" + formula + '\'' +
                ", minDamage=" + minDamage +
                ", maxDamage=" + maxDamage +
                ", critical=" + (critical != null) +
                '}';
    }
}
