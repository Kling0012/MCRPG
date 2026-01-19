package com.example.rpgplugin.core.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * YamlConfigManagerのテストクラス
 */
@DisplayName("YamlConfigManager Tests")
class YamlConfigManagerTest {

    private YamlConfigManager configManager;
    private Plugin mockPlugin;
    private java.util.logging.Logger logger;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mockPlugin = mock(Plugin.class);
        logger = java.util.logging.Logger.getLogger("TestLogger");
        when(mockPlugin.getLogger()).thenReturn(logger);
        when(mockPlugin.getDataFolder()).thenReturn(tempDir.toFile());
        configManager = new YamlConfigManager(mockPlugin);
    }

    @Nested
    @DisplayName("loadConfig")
    class LoadConfigTests {

        @Test
        @DisplayName("設定ファイルを読み込める")
        void loadConfig_ExistingFile_LoadsSuccessfully() throws IOException {
            Path configPath = tempDir.resolve("config.yml");
            Files.writeString(configPath, "key: value");

            boolean result = configManager.loadConfig("test", "config.yml", null);

            assertTrue(result);
            assertEquals(1, configManager.getConfigCount());
            assertEquals("value", configManager.getString("test", "key", "default"));
        }

        @Test
        @DisplayName("デフォルト内容でファイルを作成して読み込める")
        void loadConfig_WithDefaultContent_CreatesFile() {
            String defaultContent = "default: content";

            boolean result = configManager.loadConfig("test", "new_config.yml", defaultContent);

            assertTrue(result);
            File createdFile = tempDir.resolve("new_config.yml").toFile();
            assertTrue(createdFile.exists());
            assertEquals("content", configManager.getString("test", "default", ""));
        }

        @Test
        @DisplayName("読み込み失敗時はfalseを返す")
        void loadConfig_LoadFailure_ReturnsFalse() {
            // 不正なYAMLで読み込み失敗をシミュレート
            boolean result = configManager.loadConfig("test", "nonexistent.yml", null);

            // ファイルが存在しない場合はfalse（デフォルト内容もnull）
            assertFalse(result);
        }

        @Test
        @DisplayName("複数の設定を読み込める")
        void loadConfig_MultipleConfigs_LoadsAll() throws IOException {
            Files.writeString(tempDir.resolve("config1.yml"), "value: 1");
            Files.writeString(tempDir.resolve("config2.yml"), "value: 2");

            boolean result1 = configManager.loadConfig("config1", "config1.yml", null);
            boolean result2 = configManager.loadConfig("config2", "config2.yml", null);

            assertTrue(result1);
            assertTrue(result2);
            assertEquals(2, configManager.getConfigCount());
        }
    }

    @Nested
    @DisplayName("reloadConfig")
    class ReloadConfigTests {

        @Test
        @DisplayName("設定を再読み込みできる")
        void reloadConfig_ExistingConfig_ReloadsSuccessfully() throws IOException {
            Path configPath = tempDir.resolve("reload_test.yml");
            Files.writeString(configPath, "value: original");

            configManager.loadConfig("reload", "reload_test.yml", null);
            assertEquals("original", configManager.getString("reload", "value", ""));

            // ファイルを更新
            Files.writeString(configPath, "value: updated");

            boolean result = configManager.reloadConfig("reload");

            assertTrue(result);
            assertEquals("updated", configManager.getString("reload", "value", ""));
        }

        @Test
        @DisplayName("存在しない設定の再読み込みはfalse")
        void reloadConfig_NonExistentConfig_ReturnsFalse() {
            boolean result = configManager.reloadConfig("nonexistent");

            assertFalse(result);
        }

        @Test
        @DisplayName("再読み込み時にリスナーが通知される")
        void reloadConfig_WithListener_NotifiesListener() throws IOException {
            Path configPath = tempDir.resolve("listener_test.yml");
            Files.writeString(configPath, "value: test");

            configManager.loadConfig("listener", "listener_test.yml", null);

            AtomicBoolean notified = new AtomicBoolean(false);
            configManager.addReloadListener("listener", config -> notified.set(true));

            Files.writeString(configPath, "value: updated");
            configManager.reloadConfig("listener");

            assertTrue(notified.get());
        }

        @Test
        @DisplayName("リスナー内で例外が発生しても他のリスナーは実行される")
        void reloadConfig_ListenerException_ContinuesNotification() throws IOException {
            Path configPath = tempDir.resolve("exception_test.yml");
            Files.writeString(configPath, "value: test");

            configManager.loadConfig("exception", "exception_test.yml", null);

            AtomicInteger callCount = new AtomicInteger(0);
            configManager.addReloadListener("exception", config -> {
                callCount.incrementAndGet();
                throw new RuntimeException("Test exception");
            });
            configManager.addReloadListener("exception", config -> callCount.incrementAndGet());

            Files.writeString(configPath, "value: updated");
            configManager.reloadConfig("exception");

            assertEquals(2, callCount.get());
        }
    }

    @Nested
    @DisplayName("reloadAll")
    class ReloadAllTests {

        @Test
        @DisplayName("全ての設定を再読み込みできる")
        void reloadAll_MultipleConfigs_ReloadsAll() throws IOException {
            Files.writeString(tempDir.resolve("all1.yml"), "value: 1");
            Files.writeString(tempDir.resolve("all2.yml"), "value: 2");
            Files.writeString(tempDir.resolve("all3.yml"), "value: 3");

            configManager.loadConfig("all1", "all1.yml", null);
            configManager.loadConfig("all2", "all2.yml", null);
            configManager.loadConfig("all3", "all3.yml", null);

            int result = configManager.reloadAll();

            assertEquals(3, result);
        }

        @Test
        @DisplayName("一部が失敗しても成功数を返す")
        void reloadAll_PartialFailure_ReturnsSuccessCount() throws IOException {
            Files.writeString(tempDir.resolve("success.yml"), "value: 1");

            configManager.loadConfig("success", "success.yml", null);
            configManager.loadConfig("fail", "nonexistent.yml", null);

            int result = configManager.reloadAll();

            assertEquals(1, result);
        }

        @Test
        @DisplayName("設定がない場合は0を返す")
        void reloadAll_NoConfigs_ReturnsZero() {
            int result = configManager.reloadAll();

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("saveConfig")
    class SaveConfigTests {

        @Test
        @DisplayName("設定を保存できる")
        void saveConfig_ExistingConfig_SavesSuccessfully() throws IOException {
            Path configPath = tempDir.resolve("save_test.yml");
            Files.writeString(configPath, "original: value");

            configManager.loadConfig("save", "save_test.yml", null);
            configManager.set("save", "new_key", "new_value");

            boolean result = configManager.saveConfig("save");

            assertTrue(result);

            // 読み込んで確認
            FileConfiguration saved = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(configPath.toFile());
            assertEquals("new_value", saved.getString("new_key"));
        }

        @Test
        @DisplayName("存在しない設定の保存はfalse")
        void saveConfig_NonExistentConfig_ReturnsFalse() {
            boolean result = configManager.saveConfig("nonexistent");

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("mergeConfigs")
    class MergeConfigsTests {

        @Test
        @DisplayName("複数の設定をマージできる")
        void mergeConfigs_MultipleSources_MergesSuccessfully() throws IOException {
            Files.writeString(tempDir.resolve("target.yml"), "key1: value1\nkey2: original");
            Files.writeString(tempDir.resolve("source1.yml"), "key2: updated\nkey3: new1");
            Files.writeString(tempDir.resolve("source2.yml"), "key4: new2");

            configManager.loadConfig("target", "target.yml", null);
            configManager.loadConfig("source1", "source1.yml", null);
            configManager.loadConfig("source2", "source2.yml", null);

            boolean result = configManager.mergeConfigs("target", List.of("source1", "source2"));

            assertTrue(result);
            assertEquals("value1", configManager.getString("target", "key1", ""));
            assertEquals("updated", configManager.getString("target", "key2", ""));
            assertEquals("new1", configManager.getString("target", "key3", ""));
            assertEquals("new2", configManager.getString("target", "key4", ""));
        }

        @Test
        @DisplayName("ターゲットが存在しない場合はfalse")
        void mergeConfigs_NonExistentTarget_ReturnsFalse() {
            boolean result = configManager.mergeConfigs("nonexistent", List.of("source1"));

            assertFalse(result);
        }

        @Test
        @DisplayName("ソースが存在しない場合はスキップして続行")
        void mergeConfigs_NonExistentSource_SkipsAndContinues() throws IOException {
            Files.writeString(tempDir.resolve("target.yml"), "key1: value1");

            configManager.loadConfig("target", "target.yml", null);

            boolean result = configManager.mergeConfigs("target", List.of("nonexistent"));

            assertTrue(result);
            assertEquals("value1", configManager.getString("target", "key1", ""));
        }
    }

    @Nested
    @DisplayName("get methods")
    class GetMethodTests {

        @Test
        @DisplayName("設定値を取得できる")
        void get_ExistingKey_ReturnsValue() throws IOException {
            Files.writeString(tempDir.resolve("get_test.yml"), "string: text\ninteger: 42");

            configManager.loadConfig("get", "get_test.yml", null);

            assertEquals("text", configManager.get("get", "string"));
            assertEquals(42, configManager.get("get", "integer"));
        }

        @Test
        @DisplayName("存在しないキーはnullを返す")
        void get_NonExistentKey_ReturnsNull() throws IOException {
            Files.writeString(tempDir.resolve("get_test.yml"), "key: value");

            configManager.loadConfig("get", "get_test.yml", null);

            assertNull(configManager.get("get", "nonexistent"));
        }

        @Test
        @DisplayName("文字列値を取得できる")
        void getString_ExistingKey_ReturnsValue() throws IOException {
            Files.writeString(tempDir.resolve("str_test.yml"), "name: test");

            configManager.loadConfig("str", "str_test.yml", null);

            assertEquals("test", configManager.getString("str", "name", "default"));
        }

        @Test
        @DisplayName("存在しない設定名はデフォルト値を返す")
        void getString_NonExistentConfig_ReturnsDefault() {
            String result = configManager.getString("nonexistent", "key", "default");

            assertEquals("default", result);
        }

        @Test
        @DisplayName("整数値を取得できる")
        void getInt_ExistingKey_ReturnsValue() throws IOException {
            Files.writeString(tempDir.resolve("int_test.yml"), "level: 10");

            configManager.loadConfig("int", "int_test.yml", null);

            assertEquals(10, configManager.getInt("int", "level", 0));
        }

        @Test
        @DisplayName("実数値を取得できる")
        void getDouble_ExistingKey_ReturnsValue() throws IOException {
            Files.writeString(tempDir.resolve("dbl_test.yml"), "rate: 0.5");

            configManager.loadConfig("dbl", "dbl_test.yml", null);

            assertEquals(0.5, configManager.getDouble("dbl", "rate", 0.0), 0.001);
        }

        @Test
        @DisplayName("真偽値を取得できる")
        void getBoolean_ExistingKey_ReturnsValue() throws IOException {
            Files.writeString(tempDir.resolve("bool_test.yml"), "enabled: true");

            configManager.loadConfig("bool", "bool_test.yml", null);

            assertTrue(configManager.getBoolean("bool", "enabled", false));
        }

        @Test
        @DisplayName("文字列リストを取得できる")
        void getStringList_ExistingKey_ReturnsList() throws IOException {
            Files.writeString(tempDir.resolve("list_test.yml"), "items:\n  - apple\n  - banana");

            configManager.loadConfig("list", "list_test.yml", null);

            List<String> result = configManager.getStringList("list", "items");

            assertEquals(2, result.size());
            assertTrue(result.contains("apple"));
            assertTrue(result.contains("banana"));
        }

        @Test
        @DisplayName("文字列マップを取得できる")
        void getStringMap_ExistingKey_ReturnsMap() throws IOException {
            Files.writeString(tempDir.resolve("map_test.yml"), "aliases:\n  key1: value1\n  key2: value2");

            configManager.loadConfig("map", "map_test.yml", null);

            var result = configManager.getStringMap("map", "aliases");

            assertEquals(2, result.size());
            assertEquals("value1", result.get("key1"));
            assertEquals("value2", result.get("key2"));
        }

        @Test
        @DisplayName("設定セクションを取得できる")
        void getSection_ExistingKey_ReturnsSection() throws IOException {
            Files.writeString(tempDir.resolve("sec_test.yml"), "section:\n  subkey: subvalue");

            configManager.loadConfig("sec", "sec_test.yml", null);

            ConfigurationSection result = configManager.getSection("sec", "section");

            assertNotNull(result);
            assertEquals("subvalue", result.getString("subkey"));
        }

        @Test
        @DisplayName("全てのキーを取得できる")
        void getKeys_ExistingConfig_ReturnsKeys() throws IOException {
            Files.writeString(tempDir.resolve("keys_test.yml"), "key1: value1\nkey2: value2");

            configManager.loadConfig("keys", "keys_test.yml", null);

            Set<String> keys = configManager.getKeys("keys", false);

            assertEquals(2, keys.size());
            assertTrue(keys.contains("key1"));
            assertTrue(keys.contains("key2"));
        }
    }

    @Nested
    @DisplayName("set method")
    class SetMethodTests {

        @Test
        @DisplayName("設定値を設定できる")
        void set_ExistingConfig_SetsValue() throws IOException {
            Files.writeString(tempDir.resolve("set_test.yml"), "original: value");

            configManager.loadConfig("set", "set_test.yml", null);
            configManager.set("set", "new_key", "new_value");

            assertEquals("new_value", configManager.getString("set", "new_key", ""));
        }

        @Test
        @DisplayName("既存のキーを上書きできる")
        void set_ExistingKey_OverwritesValue() throws IOException {
            Files.writeString(tempDir.resolve("overwrite.yml"), "key: old");

            configManager.loadConfig("overwrite", "overwrite.yml", null);
            configManager.set("overwrite", "key", "new");

            assertEquals("new", configManager.getString("overwrite", "key", ""));
        }
    }

    @Nested
    @DisplayName("contains")
    class ContainsTests {

        @Test
        @DisplayName("存在するキーはtrue")
        void contains_ExistingKey_ReturnsTrue() throws IOException {
            Files.writeString(tempDir.resolve("contains.yml"), "key: value");

            configManager.loadConfig("contains", "contains.yml", null);

            assertTrue(configManager.contains("contains", "key"));
        }

        @Test
        @DisplayName("存在しないキーはfalse")
        void contains_NonExistentKey_ReturnsFalse() throws IOException {
            Files.writeString(tempDir.resolve("contains.yml"), "key: value");

            configManager.loadConfig("contains", "contains.yml", null);

            assertFalse(configManager.contains("contains", "nonexistent"));
        }

        @Test
        @DisplayName("存在しない設定名はfalse")
        void contains_NonExistentConfig_ReturnsFalse() {
            assertFalse(configManager.contains("nonexistent", "key"));
        }
    }

    @Nested
    @DisplayName("addReloadListener")
    class AddReloadListenerTests {

        @Test
        @DisplayName("リロードリスナーを登録できる")
        void addReloadListener_ValidListener_RegistersListener() throws IOException {
            Files.writeString(tempDir.resolve("listener.yml"), "value: test");

            configManager.loadConfig("listener", "listener.yml", null);

            AtomicBoolean notified = new AtomicBoolean(false);
            configManager.addReloadListener("listener", config -> notified.set(true));

            Files.writeString(tempDir.resolve("listener.yml"), "value: updated");
            configManager.reloadConfig("listener");

            assertTrue(notified.get());
        }

        @Test
        @DisplayName("複数のリスナーを登録できる")
        void addReloadListener_MultipleListeners_AllNotified() throws IOException {
            Files.writeString(tempDir.resolve("multi.yml"), "value: test");

            configManager.loadConfig("multi", "multi.yml", null);

            AtomicInteger count = new AtomicInteger(0);
            configManager.addReloadListener("multi", config -> count.incrementAndGet());
            configManager.addReloadListener("multi", config -> count.incrementAndGet());

            Files.writeString(tempDir.resolve("multi.yml"), "value: updated");
            configManager.reloadConfig("multi");

            assertEquals(2, count.get());
        }
    }

    @Nested
    @DisplayName("unloadConfig")
    class UnloadConfigTests {

        @Test
        @DisplayName("設定をアンロードできる")
        void unloadConfig_ExistingConfig_RemovesConfig() throws IOException {
            Files.writeString(tempDir.resolve("unload.yml"), "value: test");

            configManager.loadConfig("unload", "unload.yml", null);
            assertEquals(1, configManager.getConfigCount());

            configManager.unloadConfig("unload");

            assertEquals(0, configManager.getConfigCount());
            assertNull(configManager.getConfig("unload"));
        }

        @Test
        @DisplayName("アンロード後にリスナーも削除される")
        void unloadConfig_WithListener_RemovesListener() throws IOException {
            Files.writeString(tempDir.resolve("unload_listener.yml"), "value: test");

            configManager.loadConfig("unload", "unload_listener.yml", null);
            configManager.addReloadListener("unload", config -> {});

            configManager.unloadConfig("unload");

            // 再読み込みしてもリスナーは実行されない（削除されている）
            Files.writeString(tempDir.resolve("unload_listener.yml"), "value: updated");
            boolean result = configManager.reloadConfig("unload");

            // 設定が存在しないのでfalseになる
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("unloadAll")
    class UnloadAllTests {

        @Test
        @DisplayName("全ての設定をアンロードできる")
        void unloadAll_MultipleConfigs_RemovesAll() throws IOException {
            Files.writeString(tempDir.resolve("all1.yml"), "value: 1");
            Files.writeString(tempDir.resolve("all2.yml"), "value: 2");

            configManager.loadConfig("all1", "all1.yml", null);
            configManager.loadConfig("all2", "all2.yml", null);
            assertEquals(2, configManager.getConfigCount());

            configManager.unloadAll();

            assertEquals(0, configManager.getConfigCount());
            assertTrue(configManager.getConfigNames().isEmpty());
        }
    }

    @Nested
    @DisplayName("getConfigCount, getConfigNames")
    class InfoMethodsTests {

        @Test
        @DisplayName("設定数を取得できる")
        void getConfigCount_MultipleConfigs_ReturnsCount() throws IOException {
            Files.writeString(tempDir.resolve("count1.yml"), "value: 1");
            Files.writeString(tempDir.resolve("count2.yml"), "value: 2");
            Files.writeString(tempDir.resolve("count3.yml"), "value: 3");

            configManager.loadConfig("count1", "count1.yml", null);
            configManager.loadConfig("count2", "count2.yml", null);
            configManager.loadConfig("count3", "count3.yml", null);

            assertEquals(3, configManager.getConfigCount());
        }

        @Test
        @DisplayName("設定名のセットを取得できる")
        void getConfigNames_MultipleConfigs_ReturnsNames() throws IOException {
            Files.writeString(tempDir.resolve("name1.yml"), "value: 1");
            Files.writeString(tempDir.resolve("name2.yml"), "value: 2");

            configManager.loadConfig("cfg1", "name1.yml", null);
            configManager.loadConfig("cfg2", "name2.yml", null);

            Set<String> names = configManager.getConfigNames();

            assertEquals(2, names.size());
            assertTrue(names.contains("cfg1"));
            assertTrue(names.contains("cfg2"));
        }
    }

    @Nested
    @DisplayName("getConfig")
    class GetConfigTests {

        @Test
        @DisplayName("FileConfigurationを取得できる")
        void getConfig_ExistingConfig_ReturnsConfig() throws IOException {
            Files.writeString(tempDir.resolve("get_cfg.yml"), "key: value");

            configManager.loadConfig("get_cfg", "get_cfg.yml", null);

            FileConfiguration result = configManager.getConfig("get_cfg");

            assertNotNull(result);
            assertEquals("value", result.getString("key"));
        }

        @Test
        @DisplayName("存在しない設定はnullを返す")
        void getConfig_NonExistentConfig_ReturnsNull() {
            FileConfiguration result = configManager.getConfig("nonexistent");

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("getters")
    class GetterTests {

        @Test
        @DisplayName("プラグインを取得できる")
        void getPlugin_ReturnsPlugin() {
            assertEquals(mockPlugin, configManager.getPlugin());
        }

        @Test
        @DisplayName("ロガーを取得できる")
        void getLogger_ReturnsLogger() {
            assertEquals(logger, configManager.getLogger());
        }

        @Test
        @DisplayName("データフォルダを取得できる")
        void getDataFolder_ReturnsDataFolder() {
            assertEquals(tempDir.toFile(), configManager.getDataFolder());
        }

        @Test
        @DisplayName("ConfigLoaderを取得できる")
        void getLoader_ReturnsLoader() {
            assertNotNull(configManager.getLoader());
            assertEquals(tempDir.toFile(), configManager.getLoader().getDataFolder());
        }
    }
}
