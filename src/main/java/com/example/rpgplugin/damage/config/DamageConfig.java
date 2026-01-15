package com.example.rpgplugin.damage.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * ダメージYAML設定モデル
 *
 * <p>damage_config.ymlから読み込んだ設定を保持します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: 設定データの保持のみ担当</li>
 *   <li>DRY: 変数解決ロジックはVariableScopeManagerに委譲</li>
 *   <li>KISS: シンプルなImmutable構造</li>
 * </ul>
 *
 * @version 1.0.0
 * @author RPGPlugin Team
 */
public class DamageConfig {

    /** グローバル定数 */
    private final Map<String, Object> globalConstants;

    /** イベント設定マップ (イベント名 -> EventConfig) */
    private final Map<String, EventConfig> events;

    /** クラス別上書き設定 (クラス名 -> クラス設定) */
    private final Map<String, ClassOverrideConfig> classOverrides;

    /** ダメージタイプ設定 */
    private final Map<String, DamageTypeConfig> damageTypes;

    /** 武器タイプ設定 */
    private final Map<String, WeaponTypeConfig> weaponTypes;

    /**
     * コンストラクタ
     *
     * @param globalConstants グローバル定数
     * @param events イベント設定
     * @param classOverrides クラス別上書き
     * @param damageTypes ダメージタイプ
     * @param weaponTypes 武器タイプ
     */
    private DamageConfig(
            Map<String, Object> globalConstants,
            Map<String, EventConfig> events,
            Map<String, ClassOverrideConfig> classOverrides,
            Map<String, DamageTypeConfig> damageTypes,
            Map<String, WeaponTypeConfig> weaponTypes) {
        this.globalConstants = Collections.unmodifiableMap(new HashMap<>(globalConstants));
        this.events = Collections.unmodifiableMap(new HashMap<>(events));
        this.classOverrides = Collections.unmodifiableMap(new HashMap<>(classOverrides));
        this.damageTypes = Collections.unmodifiableMap(new HashMap<>(damageTypes));
        this.weaponTypes = Collections.unmodifiableMap(new HashMap<>(weaponTypes));
    }

    /**
     * グローバル定数を取得します
     *
     * @return グローバル定数マップ（コピー）
     */
    public Map<String, Object> getGlobalConstants() {
        return new HashMap<>(globalConstants);
    }

    /**
     * グローバル定数値を取得します
     *
     * @param key 定数キー
     * @return 定数値、存在しない場合はnull
     */
    public Object getGlobalConstant(String key) {
        return globalConstants.get(key);
    }

    /**
     * イベント設定を取得します
     *
     * @param eventName イベント名
     * @return イベント設定、存在しない場合はnull
     */
    public EventConfig getEventConfig(String eventName) {
        return events.get(eventName);
    }

    /**
     * 全イベント設定を取得します
     *
     * @return イベント設定マップ（コピー）
     */
    public Map<String, EventConfig> getAllEvents() {
        return new HashMap<>(events);
    }

    /**
     * クラス別上書き設定を取得します
     *
     * @param className クラス名
     * @return クラス設定、存在しない場合はnull
     */
    public ClassOverrideConfig getClassOverride(String className) {
        return classOverrides.get(className);
    }

    /**
     * クラス用のイベント設定を取得します
     *
     * <p>クラス別上書きが存在する場合はそれを返し、なければデフォルトのイベント設定を返します。</p>
     *
     * @param className クラス名
     * @param eventName イベント名
     * @return イベント設定、存在しない場合はnull
     */
    public EventConfig getEventConfigForClass(String className, String eventName) {
        ClassOverrideConfig classOverride = classOverrides.get(className);
        if (classOverride != null) {
            EventConfig overridden = classOverride.getEventConfig(eventName);
            if (overridden != null) {
                return overridden;
            }
        }
        return events.get(eventName);
    }

    /**
     * ダメージタイプ設定を取得します
     *
     * @param damageType ダメージタイプ
     * @return ダメージタイプ設定、存在しない場合はnull
     */
    public DamageTypeConfig getDamageType(String damageType) {
        return damageTypes.get(damageType);
    }

    /**
     * 武器タイプ設定を取得します
     *
     * @param weaponType 武器タイプ
     * @return 武器タイプ設定、存在しない場合はnull
     */
    public WeaponTypeConfig getWeaponType(String weaponType) {
        return weaponTypes.get(weaponType);
    }

    /**
     * ビルダー
     */
    public static class Builder {
        private final Map<String, Object> globalConstants = new HashMap<>();
        private final Map<String, EventConfig> events = new HashMap<>();
        private final Map<String, ClassOverrideConfig> classOverrides = new HashMap<>();
        private final Map<String, DamageTypeConfig> damageTypes = new HashMap<>();
        private final Map<String, WeaponTypeConfig> weaponTypes = new HashMap<>();

        /**
         * グローバル定数を追加します
         *
         * @param key キー
         * @param value 値
         * @return this
         */
        public Builder addGlobalConstant(String key, Object value) {
            globalConstants.put(key, value);
            return this;
        }

        /**
         * グローバル定数をまとめて追加します
         *
         * @param constants 定数マップ
         * @return this
         */
        public Builder addGlobalConstants(Map<String, Object> constants) {
            if (constants != null) {
                globalConstants.putAll(constants);
            }
            return this;
        }

        /**
         * イベント設定を追加します
         *
         * @param name イベント名
         * @param config イベント設定
         * @return this
         */
        public Builder addEvent(String name, EventConfig config) {
            events.put(name, config);
            return this;
        }

        /**
         * クラス別上書きを追加します
         *
         * @param className クラス名
         * @param config クラス設定
         * @return this
         */
        public Builder addClassOverride(String className, ClassOverrideConfig config) {
            classOverrides.put(className, config);
            return this;
        }

        /**
         * ダメージタイプを追加します
         *
         * @param name ダメージタイプ名
         * @param config ダメージタイプ設定
         * @return this
         */
        public Builder addDamageType(String name, DamageTypeConfig config) {
            damageTypes.put(name, config);
            return this;
        }

        /**
         * 武器タイプを追加します
         *
         * @param name 武器タイプ名
         * @param config 武器タイプ設定
         * @return this
         */
        public Builder addWeaponType(String name, WeaponTypeConfig config) {
            weaponTypes.put(name, config);
            return this;
        }

        /**
         * DamageConfigを構築します
         *
         * @return DamageConfigインスタンス
         */
        public DamageConfig build() {
            return new DamageConfig(
                    globalConstants,
                    events,
                    classOverrides,
                    damageTypes,
                    weaponTypes
            );
        }
    }

    /**
     * クラス別上書き設定
     */
    public static class ClassOverrideConfig {

        /** 説明 */
        private final String description;

        /** クラス定数 */
        private final Map<String, Object> constants;

        /** イベント上書き設定 */
        private final Map<String, EventConfig> eventOverrides;

        /**
         * コンストラクタ
         *
         * @param description 説明
         * @param constants クラス定数
         * @param eventOverrides イベント上書き
         */
        private ClassOverrideConfig(
                String description,
                Map<String, Object> constants,
                Map<String, EventConfig> eventOverrides) {
            this.description = description;
            this.constants = Collections.unmodifiableMap(new HashMap<>(constants));
            this.eventOverrides = Collections.unmodifiableMap(new HashMap<>(eventOverrides));
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

        public EventConfig getEventConfig(String eventName) {
            return eventOverrides.get(eventName);
        }

        public Map<String, EventConfig> getAllEventOverrides() {
            return new HashMap<>(eventOverrides);
        }

        /**
         * ビルダー
         */
        public static class Builder {
            private String description;
            private final Map<String, Object> constants = new HashMap<>();
            private final Map<String, EventConfig> eventOverrides = new HashMap<>();

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

            public Builder addEventOverride(String eventName, EventConfig config) {
                eventOverrides.put(eventName, config);
                return this;
            }

            public ClassOverrideConfig build() {
                return new ClassOverrideConfig(description, constants, eventOverrides);
            }
        }
    }

    /**
     * ダメージタイプ設定
     */
    public static class DamageTypeConfig {

        private final String name;
        private final String description;
        private final String statPrimary;
        private final String statSecondary;

        private DamageTypeConfig(String name, String description, String statPrimary, String statSecondary) {
            this.name = name;
            this.description = description;
            this.statPrimary = statPrimary;
            this.statSecondary = statSecondary;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getStatPrimary() {
            return statPrimary;
        }

        public String getStatSecondary() {
            return statSecondary;
        }

        /**
         * YAMLセクションから構築します
         */
        public static DamageTypeConfig fromSection(ConfigurationSection section) {
            if (section == null) {
                throw new IllegalArgumentException("Section cannot be null");
            }
            return new DamageTypeConfig(
                    section.getString("name", ""),
                    section.getString("description", ""),
                    section.getString("stat_primary", "STR"),
                    section.getString("stat_secondary", "DEX")
            );
        }
    }

    /**
     * 武器タイプ設定
     */
    public static class WeaponTypeConfig {

        private final String name;
        private final double baseMultiplier;

        private WeaponTypeConfig(String name, double baseMultiplier) {
            this.name = name;
            this.baseMultiplier = baseMultiplier;
        }

        public String getName() {
            return name;
        }

        public double getBaseMultiplier() {
            return baseMultiplier;
        }

        /**
         * YAMLセクションから構築します
         */
        public static WeaponTypeConfig fromSection(ConfigurationSection section) {
            if (section == null) {
                throw new IllegalArgumentException("Section cannot be null");
            }
            return new WeaponTypeConfig(
                    section.getString("name", ""),
                    section.getDouble("base_multiplier", 1.0)
            );
        }
    }
}
