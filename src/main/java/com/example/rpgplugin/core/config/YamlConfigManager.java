package com.example.rpgplugin.core.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * YAML設定マネージャー
 *
 * <p>複数のYAMLファイルを管理し、マージや再読み込み機能を提供します。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class YamlConfigManager {

    private final Plugin plugin;
    private final Logger logger;
    private final File dataFolder;
    private final ConfigLoader loader;

    // 設定ファイルのキャッシュ
    private final Map<String, FileConfiguration> configs;
    private final Map<String, File> configFiles;

    // リロードリスナー
    private final Map<String, List<Consumer<FileConfiguration>>> reloadListeners;

    /**
     * コンストラクタ
     *
     * @param plugin プラグイン
     */
    public YamlConfigManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dataFolder = plugin.getDataFolder();
        this.loader = new ConfigLoader(logger, dataFolder);
        this.configs = new ConcurrentHashMap<>();
        this.configFiles = new ConcurrentHashMap<>();
        this.reloadListeners = new ConcurrentHashMap<>();
    }

    /**
     * 設定ファイルを読み込みます
     *
     * @param name 設定名
     * @param path ファイルパス
     * @param defaultContent デフォルト内容
     * @return 成功した場合はtrue
     */
    public boolean loadConfig(@NotNull String name, @NotNull String path, @Nullable String defaultContent) {
        File file = new File(dataFolder, path);

        // デフォルトファイルの作成
        if (defaultContent != null && !file.exists()) {
            if (!loader.createDefaultFile(file, defaultContent)) {
                logger.warning("Failed to create default config: " + path);
                return false;
            }
        }

        // 設定を読み込み
        FileConfiguration config = loader.loadYaml(file);
        if (config == null) {
            logger.severe("Failed to load config: " + path);
            return false;
        }

        configs.put(name, config);
        configFiles.put(name, file);

        logger.info("Loaded config: " + name + " from " + path);
        return true;
    }

    /**
     * 設定ファイルを再読み込みします
     *
     * @param name 設定名
     * @return 成功した場合はtrue
     */
    public boolean reloadConfig(@NotNull String name) {
        File file = configFiles.get(name);
        if (file == null) {
            logger.warning("Config not found: " + name);
            return false;
        }

        FileConfiguration newConfig = loader.loadYaml(file);

        if (newConfig == null) {
            logger.severe("Failed to reload config: " + name);
            return false;
        }

        configs.put(name, newConfig);

        // リロードリスナーを通知
        notifyReloadListeners(name, newConfig);

        logger.info("Reloaded config: " + name);
        return true;
    }

    /**
     * すべての設定ファイルを再読み込みします
     *
     * @return 成功した数
     */
    public int reloadAll() {
        int success = 0;
        for (String name : configs.keySet()) {
            if (reloadConfig(name)) {
                success++;
            }
        }
        return success;
    }

    /**
     * 設定ファイルを保存します
     *
     * @param name 設定名
     * @return 成功した場合はtrue
     */
    public boolean saveConfig(@NotNull String name) {
        File file = configFiles.get(name);
        FileConfiguration config = configs.get(name);

        if (file == null || config == null) {
            logger.warning("Config not found: " + name);
            return false;
        }

        return loader.saveYaml(file, config);
    }

    /**
     * 複数の設定ファイルをマージします
     *
     * <p>後の設定が先の設定を上書きします。</p>
     *
     * @param targetName マージ先の設定名
     * @param sourceNames マージ元の設定名のリスト
     * @return 成功した場合はtrue
     */
    public boolean mergeConfigs(@NotNull String targetName, @NotNull List<String> sourceNames) {
        FileConfiguration target = configs.get(targetName);
        if (target == null) {
            logger.warning("Target config not found: " + targetName);
            return false;
        }

        for (String sourceName : sourceNames) {
            FileConfiguration source = configs.get(sourceName);
            if (source == null) {
                logger.warning("Source config not found: " + sourceName);
                continue;
            }

            mergeSections(target, source, "");
        }

        logger.info("Merged configs into: " + targetName);
        return true;
    }

    /**
     * 設定セクションをマージします
     *
     * @param target マージ先
     * @param source マージ元
     * @param path 現在のパス
     */
    private void mergeSections(@NotNull FileConfiguration target, @NotNull FileConfiguration source, @NotNull String path) {
        Set<String> keys = source.getKeys(false);

        for (String key : keys) {
            String fullPath = path.isEmpty() ? key : path + "." + key;
            Object sourceValue = source.get(key);

            if (sourceValue != null && sourceValue instanceof ConfigurationSection) {
                // ネストされたセクション
                if (target.contains(key)) {
                    mergeSections(target, source, fullPath);
                } else {
                    target.createSection(fullPath);
                }
            } else {
                // 値を設定
                target.set(fullPath, sourceValue);
            }
        }
    }

    /**
     * 設定値を取得します
     *
     * @param name 設定名
     * @param path パス
     * @return 設定値、存在しない場合はnull
     */
    @Nullable
    public Object get(@NotNull String name, @NotNull String path) {
        FileConfiguration config = configs.get(name);
        return config != null ? config.get(path) : null;
    }

    /**
     * 文字列設定値を取得します
     *
     * @param name 設定名
     * @param path パス
     * @param def デフォルト値
     * @return 設定値
     */
    @NotNull
    public String getString(@NotNull String name, @NotNull String path, @NotNull String def) {
        FileConfiguration config = configs.get(name);
        return config != null ? loader.getString(config, path, def) : def;
    }

    /**
     * 整数設定値を取得します
     *
     * @param name 設定名
     * @param path パス
     * @param def デフォルト値
     * @return 設定値
     */
    public int getInt(@NotNull String name, @NotNull String path, int def) {
        FileConfiguration config = configs.get(name);
        return config != null ? loader.getInt(config, path, def) : def;
    }

    /**
     * 実数設定値を取得します
     *
     * @param name 設定名
     * @param path パス
     * @param def デフォルト値
     * @return 設定値
     */
    public double getDouble(@NotNull String name, @NotNull String path, double def) {
        FileConfiguration config = configs.get(name);
        return config != null ? loader.getDouble(config, path, def) : def;
    }

    /**
     * 真偽値設定値を取得します
     *
     * @param name 設定名
     * @param path パス
     * @param def デフォルト値
     * @return 設定値
     */
    public boolean getBoolean(@NotNull String name, @NotNull String path, boolean def) {
        FileConfiguration config = configs.get(name);
        return config != null ? loader.getBoolean(config, path, def) : def;
    }

    /**
     * 文字列リスト設定値を取得します
     *
     * @param name 設定名
     * @param path パス
     * @return 設定値、存在しない場合は空リスト
     */
    @NotNull
    public List<String> getStringList(@NotNull String name, @NotNull String path) {
        FileConfiguration config = configs.get(name);
        return config != null ? loader.getStringList(config, path) : new ArrayList<>();
    }

    /**
     * 文字列マップ設定値を取得します
     *
     * @param name 設定名
     * @param path パス
     * @return 設定値、存在しない場合は空マップ
     */
    @NotNull
    public Map<String, String> getStringMap(@NotNull String name, @NotNull String path) {
        FileConfiguration config = configs.get(name);
        return config != null ? loader.getStringMap(config, path) : new HashMap<>();
    }

    /**
     * 設定セクションを取得します
     *
     * @param name 設定名
     * @param path パス
     * @return 設定セクション、存在しない場合はnull
     */
    @Nullable
    public ConfigurationSection getSection(@NotNull String name, @NotNull String path) {
        FileConfiguration config = configs.get(name);
        return config != null ? config.getConfigurationSection(path) : null;
    }

    /**
     * 設定値を設定します
     *
     * @param name 設定名
     * @param path パス
     * @param value 設定値
     */
    public void set(@NotNull String name, @NotNull String path, @Nullable Object value) {
        FileConfiguration config = configs.get(name);
        if (config != null) {
            config.set(path, value);
        }
    }

    /**
     * 設定が存在するかを確認します
     *
     * @param name 設定名
     * @param path パス
     * @return 存在する場合はtrue
     */
    public boolean contains(@NotNull String name, @NotNull String path) {
        FileConfiguration config = configs.get(name);
        return config != null && config.contains(path);
    }

    /**
     * すべての設定キーを取得します
     *
     * @param name 設定名
     * @param deep ネストされたキーも含めるか
     * @return 設定キーのセット
     */
    @NotNull
    public Set<String> getKeys(@NotNull String name, boolean deep) {
        FileConfiguration config = configs.get(name);
        return config != null ? config.getKeys(deep) : new HashSet<>();
    }

    /**
     * リロードリスナーを登録します
     *
     * @param name 設定名
     * @param listener リスナー
     */
    public void addReloadListener(@NotNull String name, @NotNull Consumer<FileConfiguration> listener) {
        reloadListeners.computeIfAbsent(name, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * リロードリスナーを通知します
     *
     * @param name 設定名
     * @param config 新しい設定
     */
    private void notifyReloadListeners(@NotNull String name, @NotNull FileConfiguration config) {
        List<Consumer<FileConfiguration>> listeners = reloadListeners.get(name);
        if (listeners != null) {
            for (Consumer<FileConfiguration> listener : listeners) {
                try {
                    listener.accept(config);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error in reload listener for: " + name, e);
                }
            }
        }
    }

    /**
     * 設定ファイルを取得します
     *
     * @param name 設定名
     * @return FileConfiguration、存在しない場合はnull
     */
    @Nullable
    public FileConfiguration getConfig(@NotNull String name) {
        return configs.get(name);
    }

    /**
     * 設定をアンロードします
     *
     * @param name 設定名
     */
    public void unloadConfig(@NotNull String name) {
        configs.remove(name);
        configFiles.remove(name);
        reloadListeners.remove(name);
        logger.info("Unloaded config: " + name);
    }

    /**
     * すべての設定をアンロードします
     */
    public void unloadAll() {
        configs.clear();
        configFiles.clear();
        reloadListeners.clear();
        logger.info("Unloaded all configs");
    }

    /**
     * 読み込まれた設定の数を取得します
     *
     * @return 設定数
     */
    public int getConfigCount() {
        return configs.size();
    }

    /**
     * 設定名のリストを取得します
     *
     * @return 設定名のリスト
     */
    @NotNull
    public Set<String> getConfigNames() {
        return configs.keySet();
    }

    /**
     * プラグインを取得します
     *
     * @return プラグイン
     */
    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * ロガーを取得します
     *
     * @return ロガー
     */
    @NotNull
    public Logger getLogger() {
        return logger;
    }

    /**
     * データフォルダを取得します
     *
     * @return データフォルダ
     */
    @NotNull
    public File getDataFolder() {
        return dataFolder;
    }

    /**
     * ConfigLoaderを取得します
     *
     * @return ConfigLoader
     */
    @NotNull
    public ConfigLoader getLoader() {
        return loader;
    }
}
