package com.example.rpgplugin.core.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * YAML設定ローダー
 *
 * <p>YAMLファイルの読み込み・解析・型変換を行います。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ConfigLoader {

    private final Logger logger;
    private final File dataFolder;

    /**
     * コンストラクタ
     *
     * @param logger ロガー
     * @param dataFolder データフォルダ
     */
    public ConfigLoader(@NotNull Logger logger, @NotNull File dataFolder) {
        this.logger = logger;
        this.dataFolder = dataFolder;
    }

    /**
     * YAMLファイルを読み込みます
     *
     * @param file ファイル
     * @return FileConfiguration、失敗した場合はnull
     */
    @Nullable
    public FileConfiguration loadYaml(@NotNull File file) {
        if (!file.exists()) {
            logger.warning("File does not exist: " + file.getPath());
            return null;
        }

        try {
            return YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load YAML file: " + file.getPath(), e);
            return null;
        }
    }

    /**
     * YAMLファイルを読み込みます（パス指定）
     *
     * @param path ファイルパス
     * @return FileConfiguration、失敗した場合はnull
     */
    @Nullable
    public FileConfiguration loadYaml(@NotNull String path) {
        File file = new File(dataFolder, path);
        return loadYaml(file);
    }

    /**
     * 設定を保存します
     *
     * @param file ファイル
     * @param config FileConfiguration
     * @return 成功した場合はtrue
     */
    public boolean saveYaml(@NotNull File file, @NotNull FileConfiguration config) {
        try {
            config.save(file);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save YAML file: " + file.getPath(), e);
            return false;
        }
    }

    /**
     * 設定を保存します（パス指定）
     *
     * @param path ファイルパス
     * @param config FileConfiguration
     * @return 成功した場合はtrue
     */
    public boolean saveYaml(@NotNull String path, @NotNull FileConfiguration config) {
        File file = new File(dataFolder, path);
        return saveYaml(file, config);
    }

    /**
     * デフォルト設定ファイルを作成します
     *
     * @param file ファイル
     * @param defaultContent デフォルト内容
     * @return 成功した場合はtrue
     */
    public boolean createDefaultFile(@NotNull File file, @NotNull String defaultContent) {
        if (file.exists()) {
            return true;
        }

        try {
            // 親ディレクトリが存在しない場合は作成
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            Files.writeString(file.toPath(), defaultContent);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create default file: " + file.getPath(), e);
            return false;
        }
    }

    /**
     * デフォルト設定ファイルを作成します（パス指定）
     *
     * @param path ファイルパス
     * @param defaultContent デフォルト内容
     * @return 成功した場合はtrue
     */
    public boolean createDefaultFile(@NotNull String path, @NotNull String defaultContent) {
        File file = new File(dataFolder, path);
        return createDefaultFile(file, defaultContent);
    }

    /**
     * 文字列値を取得します
     *
     * @param config 設定
     * @param path パス
     * @param def デフォルト値
     * @return 設定値
     */
    @NotNull
    public String getString(@NotNull FileConfiguration config, @NotNull String path, @NotNull String def) {
        if (config.contains(path)) {
            return config.getString(path, def);
        }
        return def;
    }

    /**
     * 整数値を取得します
     *
     * @param config 設定
     * @param path パス
     * @param def デフォルト値
     * @return 設定値
     */
    public int getInt(@NotNull FileConfiguration config, @NotNull String path, int def) {
        if (config.contains(path)) {
            return config.getInt(path, def);
        }
        return def;
    }

    /**
     * 長整数値を取得します
     *
     * @param config 設定
     * @param path パス
     * @param def デフォルト値
     * @return 設定値
     */
    public long getLong(@NotNull FileConfiguration config, @NotNull String path, long def) {
        if (config.contains(path)) {
            return config.getLong(path, def);
        }
        return def;
    }

    /**
     * 実数値を取得します
     *
     * @param config 設定
     * @param path パス
     * @param def デフォルト値
     * @return 設定値
     */
    public double getDouble(@NotNull FileConfiguration config, @NotNull String path, double def) {
        if (config.contains(path)) {
            return config.getDouble(path, def);
        }
        return def;
    }

    /**
     * 真偽値を取得します
     *
     * @param config 設定
     * @param path パス
     * @param def デフォルト値
     * @return 設定値
     */
    public boolean getBoolean(@NotNull FileConfiguration config, @NotNull String path, boolean def) {
        if (config.contains(path)) {
            return config.getBoolean(path, def);
        }
        return def;
    }

    /**
     * 文字列リストを取得します
     *
     * @param config 設定
     * @param path パス
     * @return 設定値、存在しない場合は空リスト
     */
    @NotNull
    public List<String> getStringList(@NotNull FileConfiguration config, @NotNull String path) {
        if (config.contains(path)) {
            return config.getStringList(path);
        }
        return new ArrayList<>();
    }

    /**
     * 整数リストを取得します
     *
     * @param config 設定
     * @param path パス
     * @return 設定値、存在しない場合は空リスト
     */
    @NotNull
    public List<Integer> getIntegerList(@NotNull FileConfiguration config, @NotNull String path) {
        if (config.contains(path)) {
            return config.getIntegerList(path);
        }
        return new ArrayList<>();
    }

    /**
     * 文字列マップを取得します
     *
     * <p>YAMLのネストされたセクションを文字列マップに変換します。</p>
     *
     * @param config 設定
     * @param path パス
     * @return 設定値、存在しない場合は空マップ
     */
    @NotNull
    public Map<String, String> getStringMap(@NotNull FileConfiguration config, @NotNull String path) {
        Map<String, String> map = new HashMap<>();

        if (!config.contains(path)) {
            return map;
        }

        var section = config.getConfigurationSection(path);
        if (section == null) {
            return map;
        }

        for (String key : section.getKeys(false)) {
            String value = section.getString(key);
            if (value != null) {
                map.put(key, value);
            }
        }

        return map;
    }

    /**
     * ディレクトリ内のすべてのYAMLファイルを取得します
     *
     * @param directory ディレクトリパス
     * @param recursive 再帰的に検索するか
     * @return YAMLファイルのリスト
     */
    @NotNull
    public List<File> getYamlFiles(@NotNull String directory, boolean recursive) {
        List<File> files = new ArrayList<>();
        File dir = new File(dataFolder, directory);

        if (!dir.exists() || !dir.isDirectory()) {
            return files;
        }

        File[] dirFiles = dir.listFiles((d, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (dirFiles != null) {
            for (File file : dirFiles) {
                if (file.isFile()) {
                    files.add(file);
                }
            }
        }

        if (recursive) {
            File[] subDirs = dir.listFiles(File::isDirectory);
            if (subDirs != null) {
                for (File subDir : subDirs) {
                    files.addAll(getYamlFiles(directory + "/" + subDir.getName(), true));
                }
            }
        }

        return files;
    }

    /**
     * 設定値の型を検証します
     *
     * @param config 設定
     * @param path パス
     * @param expectedType 期待される型
     * @return 正しい型の場合はtrue
     */
    public boolean validateType(@NotNull FileConfiguration config, @NotNull String path, @NotNull Class<?> expectedType) {
        if (!config.contains(path)) {
            return false;
        }

        Object value = config.get(path);
        return expectedType.isInstance(value);
    }

    /**
     * 必須設定値を検証します
     *
     * @param config 設定
     * @param paths 必須パスのリスト
     * @return すべての必須値が存在する場合はtrue
     */
    public boolean validateRequired(@NotNull FileConfiguration config, @NotNull List<String> paths) {
        for (String path : paths) {
            if (!config.contains(path)) {
                logger.warning("Missing required configuration: " + path);
                return false;
            }
        }
        return true;
    }

    /**
     * 範囲検証を行います
     *
     * @param value 値
     * @param min 最小値
     * @param max 最大値
     * @return 範囲内の場合はtrue
     */
    public boolean validateRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * 範囲検証を行います
     *
     * @param value 値
     * @param min 最小値
     * @param max 最大値
     * @return 範囲内の場合はtrue
     */
    public boolean validateRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * 範囲検証を行い、範囲外の場合に警告ログを出力します
     *
     * @param value 値
     * @param min 最小値
     * @param max 最大値
     * @param fieldName フィールド名（エラーメッセージ用）
     * @param fileName ファイル名（エラーメッセージ用）
     * @return 範囲内の場合はtrue
     */
    public boolean validateRange(double value, double min, double max, String fieldName, String fileName) {
        boolean inRange = value >= min && value <= max;
        if (!inRange) {
            logger.warning(String.format(
                "[Config] %s in %s: %.2f is out of range [%.2f, %.2f]",
                fieldName, fileName, value, min, max
            ));
        }
        return inRange;
    }

    /**
     * 範囲検証を行い、範囲外の場合に警告ログを出力します
     *
     * @param value 値
     * @param min 最小値
     * @param max 最大値
     * @param fieldName フィールド名（エラーメッセージ用）
     * @param fileName ファイル名（エラーメッセージ用）
     * @return 範囲内の場合はtrue
     */
    public boolean validateRange(int value, int min, int max, String fieldName, String fileName) {
        boolean inRange = value >= min && value <= max;
        if (!inRange) {
            logger.warning(String.format(
                "[Config] %s in %s: %d is out of range [%d, %d]",
                fieldName, fileName, value, min, max
            ));
        }
        return inRange;
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
}
