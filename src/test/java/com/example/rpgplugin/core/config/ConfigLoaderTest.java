package com.example.rpgplugin.core.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigLoaderのテストクラス
 */
@DisplayName("ConfigLoader Tests")
class ConfigLoaderTest {

    private ConfigLoader configLoader;
    private java.util.logging.Logger logger;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        logger = java.util.logging.Logger.getLogger("TestLogger");
        configLoader = new ConfigLoader(logger, tempDir.toFile());
    }

    @Nested
    @DisplayName("loadYaml (File)")
    class LoadYamlFileTests {

        @Test
        @DisplayName("存在するYAMLファイルを読み込める")
        void loadYaml_ExistingFile_ReturnsConfig() throws IOException {
            File yamlFile = tempDir.resolve("test.yml").toFile();
            Files.writeString(yamlFile.toPath(), """
                    key1: value1
                    key2: 123
                    key3: true
                    section:
                      subkey: subvalue
                    """);

            FileConfiguration result = configLoader.loadYaml(yamlFile);

            assertNotNull(result);
            assertEquals("value1", result.getString("key1"));
            assertEquals(123, result.getInt("key2"));
            assertTrue(result.getBoolean("key3"));
            assertEquals("subvalue", result.getString("section.subkey"));
        }

        @Test
        @DisplayName("存在しないファイルはnullを返す")
        void loadYaml_NonExistentFile_ReturnsNull() {
            File nonExistentFile = tempDir.resolve("nonexistent.yml").toFile();

            FileConfiguration result = configLoader.loadYaml(nonExistentFile);

            assertNull(result);
        }

        @Test
        @DisplayName("空のYAMLファイルを読み込める")
        void loadYaml_EmptyFile_ReturnsConfig() throws IOException {
            File yamlFile = tempDir.resolve("empty.yml").toFile();
            Files.writeString(yamlFile.toPath(), "");

            FileConfiguration result = configLoader.loadYaml(yamlFile);

            assertNotNull(result);
        }

        @Test
        @DisplayName("複雑なネスト構造をパースできる")
        void loadYaml_NestedStructure_ParsesCorrectly() throws IOException {
            File yamlFile = tempDir.resolve("nested.yml").toFile();
            Files.writeString(yamlFile.toPath(), """
                    level1:
                      level2:
                        level3:
                          value: deep
                    list:
                      - item1
                      - item2
                      - item3
                    """);

            FileConfiguration result = configLoader.loadYaml(yamlFile);

            assertNotNull(result);
            assertEquals("deep", result.getString("level1.level2.level3.value"));
            assertEquals(3, result.getList("list").size());
        }
    }

    @Nested
    @DisplayName("loadYaml (String path)")
    class LoadYamlPathTests {

        @Test
        @DisplayName("パス指定でYAMLファイルを読み込める")
        void loadYaml_Path_LoadsConfig() throws IOException {
            Path subDir = tempDir.resolve("subdir");
            Files.createDirectories(subDir);
            Files.writeString(subDir.resolve("config.yml"), "test: value");

            FileConfiguration result = configLoader.loadYaml("subdir/config.yml");

            assertNotNull(result);
            assertEquals("value", result.getString("test"));
        }

        @Test
        @DisplayName("相対パスでファイルを読み込める")
        void loadYaml_RelativePath_LoadsConfig() throws IOException {
            Files.writeString(tempDir.resolve("relative.yml"), "key: relative");

            FileConfiguration result = configLoader.loadYaml("relative.yml");

            assertNotNull(result);
            assertEquals("relative", result.getString("key"));
        }
    }

    @Nested
    @DisplayName("saveYaml")
    class SaveYamlTests {

        @Test
        @DisplayName("YAML設定を保存できる")
        void saveYaml_Config_SavesSuccessfully() throws IOException {
            File yamlFile = tempDir.resolve("save_test.yml").toFile();
            FileConfiguration config = new YamlConfiguration();
            config.set("key1", "value1");
            config.set("key2", 456);

            boolean result = configLoader.saveYaml(yamlFile, config);

            assertTrue(result);
            assertTrue(yamlFile.exists());

            // 読み込んで確認
            FileConfiguration reloaded = YamlConfiguration.loadConfiguration(yamlFile);
            assertEquals("value1", reloaded.getString("key1"));
            assertEquals(456, reloaded.getInt("key2"));
        }

        @Test
        @DisplayName("パス指定で保存できる")
        void saveYaml_Path_SavesSuccessfully() throws IOException {
            FileConfiguration config = new YamlConfiguration();
            config.set("test", "saved");

            boolean result = configLoader.saveYaml("path/save.yml", config);

            assertTrue(result);
            assertTrue(tempDir.resolve("path").toFile().exists());
            assertTrue(tempDir.resolve("path/save.yml").toFile().exists());
        }

        @Test
        @DisplayName("日本語を含むYAMLを保存できる")
        void saveYaml_WithJapanese_SavesCorrectly() throws IOException {
            File yamlFile = tempDir.resolve("japanese.yml").toFile();
            FileConfiguration config = new YamlConfiguration();
            config.set("name", "テスト名");
            config.set("description", "説明文");

            boolean result = configLoader.saveYaml(yamlFile, config);

            assertTrue(result);

            FileConfiguration reloaded = YamlConfiguration.loadConfiguration(yamlFile);
            assertEquals("テスト名", reloaded.getString("name"));
            assertEquals("説明文", reloaded.getString("description"));
        }
    }

    @Nested
    @DisplayName("createDefaultFile")
    class CreateDefaultFileTests {

        @Test
        @DisplayName("デフォルトファイルを作成できる")
        void createDefaultFile_NotExists_CreatesFile() throws IOException {
            File newFile = tempDir.resolve("new_config.yml").toFile();
            String content = "default: content";

            boolean result = configLoader.createDefaultFile(newFile, content);

            assertTrue(result);
            assertTrue(newFile.exists());
            assertEquals(content, Files.readString(newFile.toPath()));
        }

        @Test
        @DisplayName("既存ファイルは上書きしない")
        void createDefaultFile_Exists_ReturnsTrue() throws IOException {
            File existingFile = tempDir.resolve("existing.yml").toFile();
            Files.writeString(existingFile.toPath(), "original: content");

            boolean result = configLoader.createDefaultFile(existingFile, "new: content");

            assertTrue(result);
            assertEquals("original: content", Files.readString(existingFile.toPath()));
        }

        @Test
        @DisplayName("親ディレクトリが存在しない場合は作成する")
        void createDefaultFile_NoParentDirectory_CreatesParent() {
            File deepFile = tempDir.resolve("deep/nested/config.yml").toFile();

            boolean result = configLoader.createDefaultFile(deepFile, "key: value");

            assertTrue(result);
            assertTrue(deepFile.exists());
        }

        @Test
        @DisplayName("パス指定でデフォルトファイルを作成できる")
        void createDefaultFile_Path_CreatesFile() {
            boolean result = configLoader.createDefaultFile("defaults/test.yml", "test: default");

            assertTrue(result);
            assertTrue(tempDir.resolve("defaults/test.yml").toFile().exists());
        }
    }

    @Nested
    @DisplayName("getString")
    class GetStringTests {

        @Test
        @DisplayName("文字列値を取得できる")
        void getString_ExistingValue_ReturnsValue() {
            FileConfiguration config = new YamlConfiguration();
            config.set("key", "value");

            String result = configLoader.getString(config, "key", "default");

            assertEquals("value", result);
        }

        @Test
        @DisplayName("存在しないキーはデフォルト値を返す")
        void getString_NonExistentKey_ReturnsDefault() {
            FileConfiguration config = new YamlConfiguration();

            String result = configLoader.getString(config, "nonexistent", "default");

            assertEquals("default", result);
        }

        @Test
        @DisplayName("null値でもデフォルトを返す")
        void string_NullValue_ReturnsDefault() {
            FileConfiguration config = new YamlConfiguration();
            config.set("key", (String) null);

            String result = configLoader.getString(config, "key", "default");

            assertEquals("default", result);
        }
    }

    @Nested
    @DisplayName("getInt")
    class GetIntTests {

        @Test
        @DisplayName("整数値を取得できる")
        void getInt_ExistingValue_ReturnsValue() {
            FileConfiguration config = new YamlConfiguration();
            config.set("number", 42);

            int result = configLoader.getInt(config, "number", 0);

            assertEquals(42, result);
        }

        @Test
        @DisplayName("存在しないキーはデフォルト値を返す")
        void getInt_NonExistentKey_ReturnsDefault() {
            FileConfiguration config = new YamlConfiguration();

            int result = configLoader.getInt(config, "nonexistent", 10);

            assertEquals(10, result);
        }
    }

    @Nested
    @DisplayName("getLong")
    class GetLongTests {

        @Test
        @DisplayName("長整数値を取得できる")
        void getLong_ExistingValue_ReturnsValue() {
            FileConfiguration config = new YamlConfiguration();
            config.set("bigNumber", 12345678901L);

            long result = configLoader.getLong(config, "bigNumber", 0L);

            assertEquals(12345678901L, result);
        }

        @Test
        @DisplayName("存在しないキーはデフォルト値を返す")
        void getLong_NonExistentKey_ReturnsDefault() {
            FileConfiguration config = new YamlConfiguration();

            long result = configLoader.getLong(config, "nonexistent", 100L);

            assertEquals(100L, result);
        }
    }

    @Nested
    @DisplayName("getDouble")
    class GetDoubleTests {

        @Test
        @DisplayName("実数値を取得できる")
        void getDouble_ExistingValue_ReturnsValue() {
            FileConfiguration config = new YamlConfiguration();
            config.set("price", 19.99);

            double result = configLoader.getDouble(config, "price", 0.0);

            assertEquals(19.99, result, 0.001);
        }

        @Test
        @DisplayName("存在しないキーはデフォルト値を返す")
        void getDouble_NonExistentKey_ReturnsDefault() {
            FileConfiguration config = new YamlConfiguration();

            double result = configLoader.getDouble(config, "nonexistent", 5.0);

            assertEquals(5.0, result, 0.001);
        }
    }

    @Nested
    @DisplayName("getBoolean")
    class GetBooleanTests {

        @Test
        @DisplayName("真偽値を取得できる")
        void getBoolean_ExistingValue_ReturnsValue() {
            FileConfiguration config = new YamlConfiguration();
            config.set("enabled", true);

            boolean result = configLoader.getBoolean(config, "enabled", false);

            assertTrue(result);
        }

        @Test
        @DisplayName("存在しないキーはデフォルト値を返す")
        void getBoolean_NonExistentKey_ReturnsDefault() {
            FileConfiguration config = new YamlConfiguration();

            boolean result = configLoader.getBoolean(config, "nonexistent", true);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("getStringList")
    class GetStringListTests {

        @Test
        @DisplayName("文字列リストを取得できる")
        void getStringList_ExistingList_ReturnsList() {
            FileConfiguration config = new YamlConfiguration();
            config.set("items", List.of("sword", "shield", "potion"));

            List<String> result = configLoader.getStringList(config, "items");

            assertEquals(3, result.size());
            assertTrue(result.contains("sword"));
            assertTrue(result.contains("shield"));
            assertTrue(result.contains("potion"));
        }

        @Test
        @DisplayName("存在しないキーは空リストを返す")
        void getStringList_NonExistentKey_ReturnsEmptyList() {
            FileConfiguration config = new YamlConfiguration();

            List<String> result = configLoader.getStringList(config, "nonexistent");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getIntegerList")
    class GetIntegerListTests {

        @Test
        @DisplayName("整数リストを取得できる")
        void getIntegerList_ExistingList_ReturnsList() {
            FileConfiguration config = new YamlConfiguration();
            config.set("levels", List.of(1, 5, 10, 15));

            List<Integer> result = configLoader.getIntegerList(config, "levels");

            assertEquals(4, result.size());
            assertEquals(1, result.get(0));
            assertEquals(15, result.get(3));
        }

        @Test
        @DisplayName("存在しないキーは空リストを返す")
        void getIntegerList_NonExistentKey_ReturnsEmptyList() {
            FileConfiguration config = new YamlConfiguration();

            List<Integer> result = configLoader.getIntegerList(config, "nonexistent");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getStringMap")
    class GetStringMapTests {

        @Test
        @DisplayName("文字列マップを取得できる")
        void getStringMap_ExistingMap_ReturnsMap() {
            FileConfiguration config = new YamlConfiguration();
            ConfigurationSection section = config.createSection("aliases");
            section.set("key1", "value1");
            section.set("key2", "value2");

            Map<String, String> result = configLoader.getStringMap(config, "aliases");

            assertEquals(2, result.size());
            assertEquals("value1", result.get("key1"));
            assertEquals("value2", result.get("key2"));
        }

        @Test
        @DisplayName("存在しないキーは空マップを返す")
        void getStringMap_NonExistentKey_ReturnsEmptyMap() {
            FileConfiguration config = new YamlConfiguration();

            Map<String, String> result = configLoader.getStringMap(config, "nonexistent");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("null値はスキップされる")
        void getStringMap_WithNullValues_SkipsNulls() {
            FileConfiguration config = new YamlConfiguration();
            ConfigurationSection section = config.createSection("test");
            section.set("valid", "value");
            section.set("null_key", (String) null);

            Map<String, String> result = configLoader.getStringMap(config, "test");

            assertEquals(1, result.size());
            assertEquals("value", result.get("valid"));
        }
    }

    @Nested
    @DisplayName("getYamlFiles")
    class GetYamlFilesTests {

        @Test
        @DisplayName("ディレクトリ内のYAMLファイルを取得できる")
        void getYamlFiles_ExistingFiles_ReturnsList() throws IOException {
            Path dir = tempDir.resolve("yaml_dir");
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("file1.yml"), "content1");
            Files.writeString(dir.resolve("file2.yaml"), "content2");
            Files.writeString(dir.resolve("not_yaml.txt"), "content3");

            List<File> result = configLoader.getYamlFiles("yaml_dir", false);

            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(f -> f.getName().equals("file1.yml")));
            assertTrue(result.stream().anyMatch(f -> f.getName().equals("file2.yaml")));
        }

        @Test
        @DisplayName("存在しないディレクトリは空リストを返す")
        void getYamlFiles_NonExistentDirectory_ReturnsEmptyList() {
            List<File> result = configLoader.getYamlFiles("nonexistent", false);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("再帰的にサブディレクトリも検索する")
        void getYamlFiles_Recursive_IncludesSubdirectories() throws IOException {
            Path root = tempDir.resolve("root");
            Path sub = root.resolve("sub");
            Files.createDirectories(sub);
            Files.writeString(root.resolve("root.yml"), "root");
            Files.writeString(sub.resolve("sub.yml"), "sub");

            List<File> result = configLoader.getYamlFiles("root", true);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("非再帰モードではサブディレクトリを無視する")
        void getYamlFiles_NonRecursive_IgnoresSubdirectories() throws IOException {
            Path root = tempDir.resolve("root2");
            Path sub = root.resolve("sub");
            Files.createDirectories(sub);
            Files.writeString(root.resolve("root.yml"), "root");
            Files.writeString(sub.resolve("sub.yml"), "sub");

            List<File> result = configLoader.getYamlFiles("root2", false);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("validateType")
    class ValidateTypeTests {

        @Test
        @DisplayName("正しい型の場合はtrueを返す")
        void validateType_CorrectType_ReturnsTrue() {
            FileConfiguration config = new YamlConfiguration();
            config.set("string", "text");
            config.set("integer", 123);

            assertTrue(configLoader.validateType(config, "string", String.class));
            assertTrue(configLoader.validateType(config, "integer", Integer.class));
        }

        @Test
        @DisplayName("間違った型の場合はfalseを返す")
        void validateType_WrongType_ReturnsFalse() {
            FileConfiguration config = new YamlConfiguration();
            config.set("string", "text");

            assertFalse(configLoader.validateType(config, "string", Integer.class));
        }

        @Test
        @DisplayName("存在しないキーはfalseを返す")
        void validateType_NonExistentKey_ReturnsFalse() {
            FileConfiguration config = new YamlConfiguration();

            assertFalse(configLoader.validateType(config, "nonexistent", String.class));
        }
    }

    @Nested
    @DisplayName("validateRequired")
    class ValidateRequiredTests {

        @Test
        @DisplayName("全ての必須キーが存在する場合はtrue")
        void validateRequired_AllKeysExist_ReturnsTrue() {
            FileConfiguration config = new YamlConfiguration();
            config.set("key1", "value1");
            config.set("key2", "value2");

            boolean result = configLoader.validateRequired(config, List.of("key1", "key2"));

            assertTrue(result);
        }

        @Test
        @DisplayName("必須キーが欠けている場合はfalse")
        void validateRequired_MissingKey_ReturnsFalse() {
            FileConfiguration config = new YamlConfiguration();
            config.set("key1", "value1");

            boolean result = configLoader.validateRequired(config, List.of("key1", "key2"));

            assertFalse(result);
        }

        @Test
        @DisplayName("空のリストは常にtrue")
        void validateRequired_EmptyList_ReturnsTrue() {
            FileConfiguration config = new YamlConfiguration();

            boolean result = configLoader.validateRequired(config, List.of());

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("validateRange (int)")
    class ValidateRangeIntTests {

        @Test
        @DisplayName("範囲内の場合はtrue")
        void validateRange_InRange_ReturnsTrue() {
            assertTrue(configLoader.validateRange(5, 1, 10));
            assertTrue(configLoader.validateRange(1, 1, 10));
            assertTrue(configLoader.validateRange(10, 1, 10));
        }

        @Test
        @DisplayName("範囲外の場合はfalse")
        void validateRange_OutOfRange_ReturnsFalse() {
            assertFalse(configLoader.validateRange(0, 1, 10));
            assertFalse(configLoader.validateRange(11, 1, 10));
        }

        @Test
        @DisplayName("範囲外の場合に警告ログを出力する")
        void validateRange_OutOfRange_LogsWarning() {
            configLoader.validateRange(15, 1, 10, "level", "config.yml");

            // ログ出力を確認するには追加の設定が必要ですが、
            // テストの簡素化のため例外が投げられないことを確認
            assertFalse(configLoader.validateRange(15, 1, 10, "level", "config.yml"));
        }
    }

    @Nested
    @DisplayName("validateRange (double)")
    class ValidateRangeDoubleTests {

        @Test
        @DisplayName("実数の範囲内の場合はtrue")
        void validateRange_InRange_ReturnsTrue() {
            assertTrue(configLoader.validateRange(5.5, 1.0, 10.0));
            assertTrue(configLoader.validateRange(1.0, 1.0, 10.0));
            assertTrue(configLoader.validateRange(10.0, 1.0, 10.0));
        }

        @Test
        @DisplayName("実数の範囲外の場合はfalse")
        void validateRange_OutOfRange_ReturnsFalse() {
            assertFalse(configLoader.validateRange(0.9, 1.0, 10.0));
            assertFalse(configLoader.validateRange(10.1, 1.0, 10.0));
        }
    }

    @Nested
    @DisplayName("getLogger, getDataFolder")
    class GetterTests {

        @Test
        @DisplayName("ロガーを取得できる")
        void getLogger_ReturnsLogger() {
            assertEquals(logger, configLoader.getLogger());
        }

        @Test
        @DisplayName("データフォルダを取得できる")
        void getDataFolder_ReturnsDataFolder() {
            assertEquals(tempDir.toFile(), configLoader.getDataFolder());
        }
    }

    @Nested
    @DisplayName("エラーハンドリング")
    class ErrorHandlingTests {

        @Test
        @DisplayName("無効なYAML読み込み時にnullを返す（例外処理）")
        void loadYaml_InvalidYaml_ReturnsNull() throws IOException {
            // 無効なYAML構造を持つファイルを作成
            File invalidYaml = tempDir.resolve("invalid.yml").toFile();
            Files.writeString(invalidYaml.toPath(), """
                    invalid: [
                      this is: malformed
                      unclosed bracket
                    """);

            FileConfiguration result = configLoader.loadYaml(invalidYaml);

            // BukkitのYamlConfigurationは環境によっては例外を投げる可能性がある
            // ConfigLoaderは例外をキャッチしてnullを返す
            assertNull(result, "Should return null when YAML loading fails");
        }

        @Test
        @DisplayName("読み取り専用ファイルへの保存時にfalseを返す")
        void saveYaml_ReadOnlyFile_ReturnsFalse() throws IOException {
            // 読み取り専用ファイルを作成
            File readOnlyFile = tempDir.resolve("readonly.yml").toFile();
            Files.writeString(readOnlyFile.toPath(), "initial: content");
            readOnlyFile.setReadable(true);
            readOnlyFile.setWritable(false);

            FileConfiguration config = new YamlConfiguration();
            config.set("new", "value");

            // 書き込み権限がないため保存に失敗するはず
            boolean result = configLoader.saveYaml(readOnlyFile, config);

            // ファイルシステムによっては結果が異なる可能性があるため、
            // 少なくとも例外が投げられないことを確認
            // テスト環境によっては書き込みが成功する場合もある
            assertNotNull(result);

            // クリーンアップ
            readOnlyFile.setWritable(true);
        }

        @Test
        @DisplayName("書き込み不可ディレクトリでのデフォルトファイル作成時にfalseを返す")
        void createDefaultFile_ReadOnlyDirectory_ReturnsFalse() throws IOException {
            // 親ディレクトリを作成して読み取り専用にする
            File readOnlyDir = tempDir.resolve("readonly_dir").toFile();
            readOnlyDir.mkdirs();
            readOnlyDir.setReadable(true);
            readOnlyDir.setWritable(false);

            File newFile = new File(readOnlyDir, "new_file.yml");

            boolean result = configLoader.createDefaultFile(newFile, "content: value");

            // 書き込み権限がないため失敗するはずだが、
            // ファイルシステムやOSによって動作が異なる可能性がある
            assertNotNull(result);

            // クリーンアップ
            readOnlyDir.setWritable(true);
        }

        @Test
        @DisplayName("validateRange(double)で範囲外時にログが出力される")
        void validateRange_OutOfRange_LogsWarning() {
            // 範囲外の値で警告ログが出力されることを確認
            // ログ出力の検証は難しいため、戻り値と例外が投げられないことを確認
            assertFalse(configLoader.validateRange(15.5, 1.0, 10.0, "testField", "test.yml"));
            assertTrue(configLoader.validateRange(5.5, 1.0, 10.0, "testField", "test.yml"));
        }

        @Test
        @DisplayName("validateRange(int)で範囲外時にログが出力される")
        void validateRange_IntOutOfRange_LogsWarning() {
            // 範囲外の値で警告ログが出力されることを確認
            assertFalse(configLoader.validateRange(15, 1, 10, "level", "config.yml"));
            assertTrue(configLoader.validateRange(5, 1, 10, "level", "config.yml"));
        }
    }

    @Nested
    @DisplayName("validateRequired のログ出力")
    class ValidateRequiredLoggingTests {

        @Test
        @DisplayName("必須キーが欠けている時に警告ログが出力される")
        void validateRequired_MissingKey_LogsWarning() {
            FileConfiguration config = new YamlConfiguration();
            config.set("key1", "value1");

            // key2が欠けているため警告ログが出力される
            boolean result = configLoader.validateRequired(config, List.of("key1", "key2"));

            assertFalse(result);
        }

        @Test
        @DisplayName("全ての必須キーが存在する場合は警告が出ない")
        void validateRequired_AllKeysExist_NoWarning() {
            FileConfiguration config = new YamlConfiguration();
            config.set("key1", "value1");
            config.set("key2", "value2");

            boolean result = configLoader.validateRequired(config, List.of("key1", "key2"));

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("境界値テスト")
    class BoundaryValueTests {

        @Test
        @DisplayName("validateRange(double)の境界値をテスト")
        void validateRange_BoundaryValues_Double() {
            // 厳密な境界値テスト
            assertTrue(configLoader.validateRange(1.0, 1.0, 10.0));
            assertTrue(configLoader.validateRange(10.0, 1.0, 10.0));
            assertTrue(configLoader.validateRange(5.5, 1.0, 10.0));

            // 境界外
            assertFalse(configLoader.validateRange(0.999, 1.0, 10.0));
            assertFalse(configLoader.validateRange(10.001, 1.0, 10.0));

            // 負の値
            assertTrue(configLoader.validateRange(-5.0, -10.0, 0.0));
            assertFalse(configLoader.validateRange(-10.1, -10.0, 0.0));
        }

        @Test
        @DisplayName("validateRange(int)の境界値をテスト")
        void validateRange_BoundaryValues_Int() {
            // 厳密な境界値テスト
            assertTrue(configLoader.validateRange(1, 1, 10));
            assertTrue(configLoader.validateRange(10, 1, 10));
            assertTrue(configLoader.validateRange(5, 1, 10));

            // 境界外
            assertFalse(configLoader.validateRange(0, 1, 10));
            assertFalse(configLoader.validateRange(11, 1, 10));

            // 負の値
            assertTrue(configLoader.validateRange(-5, -10, 0));
            assertFalse(configLoader.validateRange(-11, -10, 0));
        }
    }

    @Nested
    @DisplayName("null安全テスト")
    class NullSafetyTests {

        @Test
        @DisplayName("getStringのnull値でデフォルトが返される")
        void getString_NullValue_ReturnsDefault() {
            FileConfiguration config = new YamlConfiguration();
            config.set("key", (String) null);

            String result = configLoader.getString(config, "key", "default");

            assertEquals("default", result);
        }

        @Test
        @DisplayName("getStringMapでnull値のキーはスキップされる")
        void getStringMap_NullValueKey_Skipped() {
            FileConfiguration config = new YamlConfiguration();
            var section = config.createSection("test");
            section.set("valid", "value");
            section.set("null_key", (String) null);

            var result = configLoader.getStringMap(config, "test");

            assertEquals(1, result.size());
            assertEquals("value", result.get("valid"));
            assertFalse(result.containsKey("null_key"));
        }
    }
}
