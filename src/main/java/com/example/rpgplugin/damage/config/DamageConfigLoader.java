package com.example.rpgplugin.damage.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * ダメージYAML設定ローダー
 *
 * <p>damage_config.ymlから設定を読み込み、DamageConfigを構築します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: YAML読み込みの単一責務</li>
 *   <li>DRY: 共通パースロジックの一元化</li>
 *   <li>KISS: シンプルなロードフロー</li>
 * </ul>
 *
 * @version 1.0.0
 * @author RPGPlugin Team
 */
public class DamageConfigLoader {

    private final Logger logger;
    private final File dataFolder;
    private final String configFileName;

    /** キャッシュされた設定 */
    private DamageConfig cachedConfig;

    /** 設定ファイルの最終更新時刻 */
    private long lastModified;

    /**
     * コンストラクタ
     *
     * @param logger ロガー
     * @param dataFolder プラグインデータフォルダ
     * @param configFileName 設定ファイル名（デフォルト: "damage_config.yml"）
     */
    public DamageConfigLoader(Logger logger, File dataFolder, String configFileName) {
        this.logger = logger;
        this.dataFolder = dataFolder;
        this.configFileName = configFileName != null ? configFileName : "damage_config.yml";
    }

    /**
     * コンストラクタ（デフォルトファイル名）
     *
     * @param logger ロガー
     * @param dataFolder プラグインデータフォルダ
     */
    public DamageConfigLoader(Logger logger, File dataFolder) {
        this(logger, dataFolder, "damage_config.yml");
    }

    /**
     * 設定を読み込みます
     *
     * @return DamageConfigインスタンス、失敗時はnull
     */
    public DamageConfig loadConfig() {
        File configFile = new File(dataFolder, configFileName);

        if (!configFile.exists()) {
            logger.warning("Damage config file not found: " + configFile.getPath());
            return null;
        }

        // キャッシュチェック
        if (cachedConfig != null && configFile.lastModified() == lastModified) {
            return cachedConfig;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFile);

        try {
            DamageConfig.Builder builder = new DamageConfig.Builder();

            // グローバル定数を読み込み
            loadGlobalConstants(yaml, builder);

            // イベント設定を読み込み
            loadEvents(yaml, builder);

            // クラス別上書きを読み込み
            loadClassOverrides(yaml, builder);

            // ダメージタイプを読み込み
            loadDamageTypes(yaml, builder);

            // 武器タイプを読み込み
            loadWeaponTypes(yaml, builder);

            DamageConfig config = builder.build();
            cachedConfig = config;
            lastModified = configFile.lastModified();

            logger.info("Damage config loaded successfully from " + configFileName);
            return config;

        } catch (Exception e) {
            logger.severe("Failed to load damage config: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 設定を再読み込みします
     *
     * @return 再読み込みされた設定、変更がない場合は現在のキャッシュ
     */
    public DamageConfig reloadConfig() {
        File configFile = new File(dataFolder, configFileName);

        if (!configFile.exists()) {
            logger.warning("Damage config file not found: " + configFile.getPath());
            return null;
        }

        // ファイルが変更されている場合のみ再読み込み
        if (cachedConfig != null && configFile.lastModified() == lastModified) {
            return cachedConfig;
        }

        cachedConfig = null;
        return loadConfig();
    }

    /**
     * グローバル定数を読み込みます
     */
    private void loadGlobalConstants(YamlConfiguration yaml, DamageConfig.Builder builder) {
        ConfigurationSection constantsSection = yaml.getConfigurationSection("constants");
        if (constantsSection != null) {
            Map<String, Object> constants = new HashMap<>();
            for (String key : constantsSection.getKeys(false)) {
                constants.put(key, constantsSection.get(key));
            }
            builder.addGlobalConstants(constants);
            logger.fine("Loaded " + constants.size() + " global constants");
        }
    }

    /**
     * イベント設定を読み込みます
     */
    private void loadEvents(YamlConfiguration yaml, DamageConfig.Builder builder) {
        ConfigurationSection eventsSection = yaml.getConfigurationSection("events");
        if (eventsSection != null) {
            for (String eventName : eventsSection.getKeys(false)) {
                ConfigurationSection eventSection = eventsSection.getConfigurationSection(eventName);
                if (eventSection != null) {
                    EventConfig eventConfig = EventConfig.fromSection(eventSection);
                    builder.addEvent(eventName, eventConfig);
                    logger.fine("Loaded event config: " + eventName);
                }
            }
        }
    }

    /**
     * クラス別上書き設定を読み込みます
     */
    private void loadClassOverrides(YamlConfiguration yaml, DamageConfig.Builder builder) {
        ConfigurationSection classSection = yaml.getConfigurationSection("class_overrides");
        if (classSection != null) {
            for (String className : classSection.getKeys(false)) {
                ConfigurationSection classConfigSection = classSection.getConfigurationSection(className);
                if (classConfigSection != null) {
                    DamageConfig.ClassOverrideConfig.Builder classBuilder =
                            new DamageConfig.ClassOverrideConfig.Builder();

                    // 説明
                    classBuilder.description(classConfigSection.getString("description", ""));

                    // クラス定数
                    ConfigurationSection constantsSection = classConfigSection.getConfigurationSection("constants");
                    if (constantsSection != null) {
                        Map<String, Object> constants = new HashMap<>();
                        for (String key : constantsSection.getKeys(false)) {
                            constants.put(key, constantsSection.get(key));
                        }
                        classBuilder.addConstants(constants);
                    }

                    // イベント上書き
                    Set<String> eventKeys = new HashSet<>(classConfigSection.getKeys(false));
                    eventKeys.remove("description");
                    eventKeys.remove("constants");
                    for (String eventKey : eventKeys) {
                        if (classConfigSection.isConfigurationSection(eventKey)) {
                            ConfigurationSection eventSection = classConfigSection.getConfigurationSection(eventKey);
                            EventConfig eventConfig = EventConfig.fromSection(eventSection);
                            classBuilder.addEventOverride(eventKey, eventConfig);
                        }
                    }

                    DamageConfig.ClassOverrideConfig classOverride = classBuilder.build();
                    builder.addClassOverride(className, classOverride);
                    logger.fine("Loaded class override: " + className);
                }
            }
        }
    }

    /**
     * ダメージタイプ設定を読み込みます
     */
    private void loadDamageTypes(YamlConfiguration yaml, DamageConfig.Builder builder) {
        ConfigurationSection typesSection = yaml.getConfigurationSection("damage_types");
        if (typesSection != null) {
            for (String typeName : typesSection.getKeys(false)) {
                ConfigurationSection typeSection = typesSection.getConfigurationSection(typeName);
                if (typeSection != null) {
                    DamageConfig.DamageTypeConfig typeConfig =
                            DamageConfig.DamageTypeConfig.fromSection(typeSection);
                    builder.addDamageType(typeName, typeConfig);
                    logger.fine("Loaded damage type: " + typeName);
                }
            }
        }
    }

    /**
     * 武器タイプ設定を読み込みます
     */
    private void loadWeaponTypes(YamlConfiguration yaml, DamageConfig.Builder builder) {
        ConfigurationSection weaponsSection = yaml.getConfigurationSection("weapon_types");
        if (weaponsSection != null) {
            for (String weaponName : weaponsSection.getKeys(false)) {
                ConfigurationSection weaponSection = weaponsSection.getConfigurationSection(weaponName);
                if (weaponSection != null) {
                    DamageConfig.WeaponTypeConfig weaponConfig =
                            DamageConfig.WeaponTypeConfig.fromSection(weaponSection);
                    builder.addWeaponType(weaponName, weaponConfig);
                    logger.fine("Loaded weapon type: " + weaponName);
                }
            }
        }
    }

    /**
     * 設定を検証します
     *
     * @param config 検証する設定
     * @return 検証結果
     */
    public ValidationResult validateConfig(DamageConfig config) {
        if (config == null) {
            return new ValidationResult(false, "Config is null", Collections.emptyList());
        }

        List<String> warnings = new ArrayList<>();

        // 必須イベントを確認
        if (config.getEventConfig("skill_damage") == null) {
            warnings.add("skill_damage event not defined");
        }
        if (config.getEventConfig("physical_attack") == null) {
            warnings.add("physical_attack event not defined");
        }
        if (config.getEventConfig("damage_taken") == null) {
            warnings.add("damage_taken event not defined");
        }

        // グローバル定数を確認
        if (config.getGlobalConstant("max_damage_cut_rate") == null) {
            warnings.add("max_damage_cut_rate not defined in global constants");
        }

        boolean isValid = warnings.isEmpty();
        String message = isValid ? "Config validation passed" : "Config validation completed with warnings";

        return new ValidationResult(isValid, message, warnings);
    }

    /**
     * 設定ファイルを作成します（デフォルト設定）
     *
     * @return 作成成功時はtrue
     */
    public boolean createDefaultConfig() {
        File configFile = new File(dataFolder, configFileName);

        if (configFile.exists()) {
            logger.info("Damage config file already exists: " + configFile.getPath());
            return false;
        }

        try {
            // デフォルト設定を作成
            YamlConfiguration yaml = new YamlConfiguration();

            // グローバル定数
            yaml.set("constants.base_physical_multiplier", 1.0);
            yaml.set("constants.base_magic_multiplier", 1.0);
            yaml.set("constants.critical_hit_chance", 0.05);
            yaml.set("constants.critical_hit_multiplier", 2.0);
            yaml.set("constants.max_damage_cut_rate", 0.8);

            // スキルダメージイベント
            yaml.set("events.skill_damage.description", "スキルによるダメージ計算");
            yaml.set("events.skill_damage.formula", "BASE_DAMAGE * (1 + STR/100)");
            yaml.set("events.skill_damage.min_damage", 1);

            // 物理攻撃イベント
            yaml.set("events.physical_attack.description", "通常攻撃（物理）のダメージ計算");
            yaml.set("events.physical_attack.formula", "BASE_DAMAGE * (1 + STR/100)");
            yaml.set("events.physical_attack.min_damage", 1);

            // 魔法攻撃イベント
            yaml.set("events.magic_attack.description", "通常攻撃（魔法）のダメージ計算");
            yaml.set("events.magic_attack.formula", "BASE_DAMAGE + INT * 0.5");
            yaml.set("events.magic_attack.min_damage", 1);

            // 被ダメージイベント
            yaml.set("events.damage_taken.description", "受けるダメージの軽減計算");
            yaml.set("events.damage_taken.physical_cut_formula", "DAMAGE * (1 - VIT / (VIT + 100))");
            yaml.set("events.damage_taken.magic_cut_formula", "DAMAGE * (1 - SPI / (SPI + 100))");
            yaml.set("events.damage_taken.min_damage", 0.1);

            yaml.save(configFile);
            logger.info("Created default damage config: " + configFile.getPath());
            return true;

        } catch (IOException e) {
            logger.severe("Failed to create default damage config: " + e.getMessage());
            return false;
        }
    }

    /**
     * キャッシュされた設定をクリアします
     */
    public void clearCache() {
        cachedConfig = null;
        lastModified = 0;
    }

    /**
     * 設定ファイルの存在を確認します
     *
     * @return ファイルが存在する場合はtrue
     */
    public boolean configExists() {
        return new File(dataFolder, configFileName).exists();
    }

    /**
     * 設定ファイルを取得します
     *
     * @return 設定ファイル
     */
    public File getConfigFile() {
        return new File(dataFolder, configFileName);
    }

    /**
     * 検証結果
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final List<String> warnings;

        public ValidationResult(boolean valid, String message, List<String> warnings) {
            this.valid = valid;
            this.message = message;
            this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }
}
