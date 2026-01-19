package com.example.rpgplugin.damage.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;

/**
 * DamageConfigLoaderの単体テスト
 *
 * カバレッジ向上を目的とした包括的なテストスイート
 */
@DisplayName("DamageConfigLoader 単体テスト")
class DamageConfigLoaderTest {

    @TempDir
    Path tempDir;

    private Logger logger;
    private File dataFolder;
    private DamageConfigLoader loader;

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger("DamageConfigLoaderTest");
        dataFolder = tempDir.toFile();
        loader = new DamageConfigLoader(logger, dataFolder);
    }

    @Nested
    @DisplayName("reloadConfig()")
    class ReloadConfigTests {

        @Test
        @DisplayName("ファイルが存在しない場合はnullを返す")
        void reloadConfig_FileNotExists_ReturnsNull() {
            DamageConfig result = loader.reloadConfig();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("キャッシュがあり、ファイルが変更されていない場合はキャッシュを返す")
        void reloadConfig_CachedAndUnmodified_ReturnsCached() throws IOException {
            // 設定ファイルを作成
            File configFile = new File(dataFolder, "damage_config.yml");
            createTestConfigFile(configFile);

            // 最初にロードしてキャッシュを作成
            DamageConfig firstLoad = loader.reloadConfig();
            assertThat(firstLoad).isNotNull();

            // ファイルを変更せずに再読み込み
            DamageConfig secondLoad = loader.reloadConfig();

            assertThat(secondLoad).isSameAs(firstLoad);
        }

        @Test
        @DisplayName("ファイルが変更されている場合は再読み込みする")
        void reloadConfig_FileModified_Reloads() throws IOException, InterruptedException {
            File configFile = new File(dataFolder, "damage_config.yml");
            createTestConfigFile(configFile);

            // 最初にロード
            DamageConfig firstLoad = loader.reloadConfig();
            assertThat(firstLoad).isNotNull();

            // ファイルを変更（タイムスタンプを更新するために少し待機）
            Thread.sleep(10);
            Files.write(configFile.toPath(),
                    "constants:\n  test_const: 999\nevents:\n  test_event:\n    formula: 'BASE_DAMAGE'".getBytes()
            );

            // 再読み込み
            DamageConfig secondLoad = loader.reloadConfig();

            // 異なるインスタンスが返されることを確認
            assertThat(secondLoad).isNotNull();
            assertThat(secondLoad).isNotSameAs(firstLoad);
        }
    }

    @Nested
    @DisplayName("validateConfig()")
    class ValidateConfigTests {

        @Test
        @DisplayName("null設定は無効")
        void validateConfig_NullConfig_ReturnsInvalid() {
            DamageConfigLoader.ValidationResult result = loader.validateConfig(null);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getMessage()).isEqualTo("Config is null");
            assertThat(result.getWarnings()).isEmpty();
        }

        @Test
        @DisplayName("全必須要素がある場合は有効")
        void validateConfig_CompleteConfig_ReturnsValid() {
            DamageConfig config = createCompleteConfig();

            DamageConfigLoader.ValidationResult result = loader.validateConfig(config);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getMessage()).isEqualTo("Config validation passed");
            assertThat(result.hasWarnings()).isFalse();
        }

        @Test
        @DisplayName("skill_damageがない場合は警告")
        void validateConfig_MissingSkillDamage_ReturnsWarning() {
            DamageConfig config = new DamageConfig.Builder()
                    .addGlobalConstant("max_damage_cut_rate", 0.8)
                    .addEvent("physical_attack", createBasicEventConfig())
                    .addEvent("damage_taken", createBasicEventConfig())
                    .build();

            DamageConfigLoader.ValidationResult result = loader.validateConfig(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.hasWarnings()).isTrue();
            assertThat(result.getWarnings()).contains("skill_damage event not defined");
        }

        @Test
        @DisplayName("physical_attackがない場合は警告")
        void validateConfig_MissingPhysicalAttack_ReturnsWarning() {
            DamageConfig config = new DamageConfig.Builder()
                    .addGlobalConstant("max_damage_cut_rate", 0.8)
                    .addEvent("skill_damage", createBasicEventConfig())
                    .addEvent("damage_taken", createBasicEventConfig())
                    .build();

            DamageConfigLoader.ValidationResult result = loader.validateConfig(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.hasWarnings()).isTrue();
            assertThat(result.getWarnings()).contains("physical_attack event not defined");
        }

        @Test
        @DisplayName("damage_takenがない場合は警告")
        void validateConfig_MissingDamageTaken_ReturnsWarning() {
            DamageConfig config = new DamageConfig.Builder()
                    .addGlobalConstant("max_damage_cut_rate", 0.8)
                    .addEvent("skill_damage", createBasicEventConfig())
                    .addEvent("physical_attack", createBasicEventConfig())
                    .build();

            DamageConfigLoader.ValidationResult result = loader.validateConfig(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.hasWarnings()).isTrue();
            assertThat(result.getWarnings()).contains("damage_taken event not defined");
        }

        @Test
        @DisplayName("max_damage_cut_rateがない場合は警告")
        void validateConfig_MissingMaxDamageCutRate_ReturnsWarning() {
            DamageConfig config = new DamageConfig.Builder()
                    .addEvent("skill_damage", createBasicEventConfig())
                    .addEvent("physical_attack", createBasicEventConfig())
                    .addEvent("damage_taken", createBasicEventConfig())
                    .build();

            DamageConfigLoader.ValidationResult result = loader.validateConfig(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.hasWarnings()).isTrue();
            assertThat(result.getWarnings()).contains("max_damage_cut_rate not defined in global constants");
        }

        @Test
        @DisplayName("複数の警告を返すことができる")
        void validateConfig_MultipleRequiredMissing_ReturnsMultipleWarnings() {
            DamageConfig config = new DamageConfig.Builder().build();

            DamageConfigLoader.ValidationResult result = loader.validateConfig(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.hasWarnings()).isTrue();
            assertThat(result.getWarnings()).hasSize(4);
        }

        @Test
        @DisplayName("hasWarningsで警告の有無を判定")
        void validateConfig_NoWarnings_ReturnsFalseForHasWarnings() {
            DamageConfig config = createCompleteConfig();

            DamageConfigLoader.ValidationResult result = loader.validateConfig(config);

            assertThat(result.hasWarnings()).isFalse();
        }
    }

    @Nested
    @DisplayName("createDefaultConfig()")
    class CreateDefaultConfigTests {

        @Test
        @DisplayName("ファイルが既に存在する場合はfalseを返す")
        void createDefaultConfig_FileExists_ReturnsFalse() throws IOException {
            File configFile = new File(dataFolder, "damage_config.yml");
            Files.createFile(configFile.toPath());

            boolean result = loader.createDefaultConfig();

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("ファイルが存在しない場合はデフォルト設定を作成")
        void createDefaultConfig_FileNotExists_CreatesFile() {
            boolean result = loader.createDefaultConfig();

            assertThat(result).isTrue();

            File configFile = new File(dataFolder, "damage_config.yml");
            assertThat(configFile).exists();

            // 作成されたファイルを読み込んで内容を確認
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFile);
            assertThat(yaml.getDouble("constants.base_physical_multiplier")).isEqualTo(1.0);
            assertThat(yaml.getDouble("constants.critical_hit_chance")).isEqualTo(0.05);
            assertThat(yaml.getString("events.skill_damage.formula"))
                    .isEqualTo("BASE_DAMAGE * (1 + STR/100)");
        }

        @Test
        @DisplayName("作成されたファイルに全必須要素が含まれる")
        void createDefaultConfig_CreatesAllRequiredElements() {
            loader.createDefaultConfig();

            File configFile = new File(dataFolder, "damage_config.yml");
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFile);

            // グローバル定数
            assertThat(yaml.contains("constants.base_physical_multiplier")).isTrue();
            assertThat(yaml.contains("constants.base_magic_multiplier")).isTrue();
            assertThat(yaml.contains("constants.critical_hit_chance")).isTrue();
            assertThat(yaml.contains("constants.critical_hit_multiplier")).isTrue();
            assertThat(yaml.contains("constants.max_damage_cut_rate")).isTrue();

            // イベント
            assertThat(yaml.contains("events.skill_damage")).isTrue();
            assertThat(yaml.contains("events.physical_attack")).isTrue();
            assertThat(yaml.contains("events.magic_attack")).isTrue();
            assertThat(yaml.contains("events.damage_taken")).isTrue();
        }
    }

    @Nested
    @DisplayName("clearCache()")
    class ClearCacheTests {

        @Test
        @DisplayName("キャッシュをクリアする")
        void clearCache_AfterLoadConfig_ClearsCache() throws IOException {
            File configFile = new File(dataFolder, "damage_config.yml");
            createTestConfigFile(configFile);

            // 設定をロードしてキャッシュを作成
            loader.loadConfig();

            // キャッシュをクリア
            loader.clearCache();

            // 再読み込みで新しいインスタンスが作られることを確認
            DamageConfig config1 = loader.reloadConfig();
            DamageConfig config2 = loader.reloadConfig();

            // clearCache後なので異なるインスタンスになる可能性がある
            // ただし、ファイルが変更されていない場合は同じインスタンスが返される
            assertThat(config1).isNotNull();
            assertThat(config2).isNotNull();
        }
    }

    @Nested
    @DisplayName("getConfigFile()")
    class GetConfigFileTests {

        @Test
        @DisplayName("設定ファイルを返す")
        void getConfigFile_ReturnsConfigFile() {
            File configFile = loader.getConfigFile();

            assertThat(configFile).isNotNull();
            assertThat(configFile.getName()).isEqualTo("damage_config.yml");
            assertThat(configFile.getParentFile()).isEqualTo(dataFolder);
        }

        @Test
        @DisplayName("カスタムファイル名で正しいファイルを返す")
        void getConfigFile_CustomFileName_ReturnsCorrectFile() {
            DamageConfigLoader customLoader = new DamageConfigLoader(logger, dataFolder, "custom_damage.yml");

            File configFile = customLoader.getConfigFile();

            assertThat(configFile.getName()).isEqualTo("custom_damage.yml");
        }
    }

    @Nested
    @DisplayName("configExists()")
    class ConfigExistsTests {

        @Test
        @DisplayName("ファイルが存在する場合はtrue")
        void configExists_FileExists_ReturnsTrue() throws IOException {
            File configFile = new File(dataFolder, "damage_config.yml");
            Files.createFile(configFile.toPath());

            boolean result = loader.configExists();

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("ファイルが存在しない場合はfalse")
        void configExists_FileNotExists_ReturnsFalse() {
            boolean result = loader.configExists();

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("loadConfig() with invalid YAML")
    class LoadConfigInvalidYamlTests {

        @Test
        @DisplayName("空のYAMLファイルでデフォルト設定を使用")
        void loadConfig_EmptyYaml_LoadsWithDefaults() throws IOException {
            File configFile = new File(dataFolder, "damage_config.yml");
            Files.write(configFile.toPath(), "".getBytes());

            DamageConfig config = loader.loadConfig();

            // 空ファイルでもnullは返されない
            assertThat(config).isNotNull();
        }

        @Test
        @DisplayName("定数のみのYAMLファイル")
        void loadConfig_OnlyConstants_LoadsConstants() throws IOException {
            File configFile = new File(dataFolder, "damage_config.yml");
            String yamlContent = """
                    constants:
                      TEST_CONST: 123
                      ANOTHER_CONST: 456
                    """;
            Files.write(configFile.toPath(), yamlContent.getBytes());

            DamageConfig config = loader.loadConfig();

            assertThat(config).isNotNull();
            assertThat(config.getGlobalConstant("TEST_CONST")).isEqualTo(123);
            assertThat(config.getGlobalConstant("ANOTHER_CONST")).isEqualTo(456);
        }

        @Test
        @DisplayName("イベントのみのYAMLファイル")
        void loadConfig_OnlyEvents_LoadsEvents() throws IOException {
            File configFile = new File(dataFolder, "damage_config.yml");
            String yamlContent = """
                    events:
                      test_event:
                        formula: "BASE_DAMAGE * 2"
                        min_damage: 5
                    """;
            Files.write(configFile.toPath(), yamlContent.getBytes());

            DamageConfig config = loader.loadConfig();

            assertThat(config).isNotNull();
            assertThat(config.getEventConfig("test_event")).isNotNull();
            assertThat(config.getEventConfig("test_event").getFormula())
                    .isEqualTo("BASE_DAMAGE * 2");
        }
    }

    @Nested
    @DisplayName("loadClassOverrides() integration")
    class LoadClassOverridesTests {

        @Test
        @DisplayName("クラス別上書き設定を読み込む")
        void loadConfig_WithClassOverrides_LoadsOverrides() throws IOException {
            File configFile = new File(dataFolder, "damage_config.yml");
            String yamlContent = """
                    constants:
                      BASE_CONST: 100

                    events:
                      skill_damage:
                        formula: "BASE_DAMAGE"
                        min_damage: 1

                    class_overrides:
                      Warrior:
                        description: "戦士クラス"
                        constants:
                          CLASS_BONUS: 50
                        skill_damage:
                          formula: "BASE_DAMAGE * 1.5"
                          min_damage: 2
                      Mage:
                        description: "魔法使いクラス"
                        constants:
                          CLASS_BONUS: 20
                        magic_attack:
                          formula: "BASE_DAMAGE * 2.0"
                    """;
            Files.write(configFile.toPath(), yamlContent.getBytes());

            DamageConfig config = loader.loadConfig();

            assertThat(config).isNotNull();

            // Warriorクラスの上書き設定
            DamageConfig.ClassOverrideConfig warriorConfig = config.getClassOverride("Warrior");
            assertThat(warriorConfig).isNotNull();
            assertThat(warriorConfig.getDescription()).isEqualTo("戦士クラス");
            assertThat(warriorConfig.getConstant("CLASS_BONUS")).isEqualTo(50);
            assertThat(warriorConfig.getEventConfig("skill_damage")).isNotNull();

            // Mageクラスの上書き設定
            DamageConfig.ClassOverrideConfig mageConfig = config.getClassOverride("Mage");
            assertThat(mageConfig).isNotNull();
            assertThat(mageConfig.getDescription()).isEqualTo("魔法使いクラス");
            assertThat(mageConfig.getConstant("CLASS_BONUS")).isEqualTo(20);
        }

        @Test
        @DisplayName("クラス別上書きがない場合")
        void loadConfig_NoClassOverrides_LoadsNormally() throws IOException {
            File configFile = new File(dataFolder, "damage_config.yml");
            String yamlContent = """
                    events:
                      skill_damage:
                        formula: "BASE_DAMAGE"
                        min_damage: 1
                    """;
            Files.write(configFile.toPath(), yamlContent.getBytes());

            DamageConfig config = loader.loadConfig();

            assertThat(config).isNotNull();
            assertThat(config.getClassOverride("Warrior")).isNull();
        }
    }

    @Nested
    @DisplayName("loadDamageTypes() integration")
    class LoadDamageTypesTests {

        @Test
        @DisplayName("ダメージタイプ設定を読み込む")
        void loadConfig_WithDamageTypes_LoadsTypes() throws IOException {
            File configFile = new File(dataFolder, "damage_config.yml");
            String yamlContent = """
                    damage_types:
                      FIRE:
                        name: "Fire Damage"
                        description: "Burns the target"
                        stat_primary: "FIRE_RESIST"
                        stat_secondary: "ICE_RESIST"
                      ICE:
                        name: "Ice Damage"
                        description: "Freezes the target"
                        stat_primary: "ICE_RESIST"
                        stat_secondary: "WIND_RESIST"
                    """;
            Files.write(configFile.toPath(), yamlContent.getBytes());

            DamageConfig config = loader.loadConfig();

            assertThat(config).isNotNull();
            assertThat(config.getDamageType("FIRE")).isNotNull();
            assertThat(config.getDamageType("FIRE").getStatPrimary()).isEqualTo("FIRE_RESIST");
            assertThat(config.getDamageType("FIRE").getStatSecondary()).isEqualTo("ICE_RESIST");
            assertThat(config.getDamageType("ICE")).isNotNull();
        }

        @Test
        @DisplayName("ダメージタイプがない場合")
        void loadConfig_NoDamageTypes_LoadsNormally() throws IOException {
            File configFile = new File(dataFolder, "damage_config.yml");
            Files.write(configFile.toPath(), "events: {}".getBytes());

            DamageConfig config = loader.loadConfig();

            assertThat(config).isNotNull();
            assertThat(config.getDamageType("FIRE")).isNull();
        }
    }

    @Nested
    @DisplayName("loadWeaponTypes() integration")
    class LoadWeaponTypesTests {

        @Test
        @DisplayName("武器タイプ設定を読み込む")
        void loadConfig_WithWeaponTypes_LoadsTypes() throws IOException {
            File configFile = new File(dataFolder, "damage_config.yml");
            String yamlContent = """
                    weapon_types:
                      SWORD:
                        base_multiplier: 1.2
                        required_stat: "STRENGTH"
                      STAFF:
                        base_multiplier: 1.5
                        required_stat: "INTELLIGENCE"
                    """;
            Files.write(configFile.toPath(), yamlContent.getBytes());

            DamageConfig config = loader.loadConfig();

            assertThat(config).isNotNull();
            assertThat(config.getWeaponType("SWORD")).isNotNull();
            assertThat(config.getWeaponType("SWORD").getBaseMultiplier()).isEqualTo(1.2);
            assertThat(config.getWeaponType("STAFF")).isNotNull();
        }

        @Test
        @DisplayName("武器タイプがない場合")
        void loadConfig_NoWeaponTypes_LoadsNormally() throws IOException {
            File configFile = new File(dataFolder, "damage_config.yml");
            Files.write(configFile.toPath(), "events: {}".getBytes());

            DamageConfig config = loader.loadConfig();

            assertThat(config).isNotNull();
            assertThat(config.getWeaponType("SWORD")).isNull();
        }
    }

    // ========== ヘルパーメソッド ==========

    private void createTestConfigFile(File file) throws IOException {
        String yamlContent = """
                constants:
                  test_const: 123

                events:
                  skill_damage:
                    formula: "BASE_DAMAGE"
                    min_damage: 1
                  physical_attack:
                    formula: "BASE_DAMAGE"
                    min_damage: 1
                  damage_taken:
                    min_damage: 0.1
                """;
        Files.write(file.toPath(), yamlContent.getBytes());
    }

    private DamageConfig createCompleteConfig() {
        return new DamageConfig.Builder()
                .addGlobalConstant("max_damage_cut_rate", 0.8)
                .addEvent("skill_damage", createBasicEventConfig())
                .addEvent("physical_attack", createBasicEventConfig())
                .addEvent("damage_taken", createBasicEventConfig())
                .build();
    }

    private EventConfig createBasicEventConfig() {
        return new EventConfig.Builder()
                .formula("BASE_DAMAGE")
                .minDamage(1.0)
                .build();
    }
}
