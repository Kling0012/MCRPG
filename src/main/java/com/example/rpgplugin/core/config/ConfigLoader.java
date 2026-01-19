package com.example.rpgplugin.core.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
     * ロガーを取得します
     *
     * @return ロガー
     */
    protected Logger getLogger() {
        return logger;
    }

    /**
     * データフォルダを取得します
     *
     * @return データフォルダ
     */
    protected File getDataFolder() {
        return dataFolder;
    }

    // ============================================================
    // ファイル操作メソッド
    // ============================================================

    /**
     * YAMLファイルを読み込みます
     *
     * @param file ファイル
     * @return FileConfiguration、失敗した場合はnull
     */
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
    public FileConfiguration loadYaml(@NotNull String path) {
        return loadYaml(new File(dataFolder, path));
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
        return saveYaml(new File(dataFolder, path), config);
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
        return createDefaultFile(new File(dataFolder, path), defaultContent);
    }

    // ============================================================
    // ジェネリック値取得メソッド（Template Methodパターン）
    // ============================================================

    /**
     * 設定値を取得するジェネリックメソッド
     *
     * @param config 設定
     * @param path パス
     * @param defaultValue デフォルト値
     * @param getter 値を取得する関数
     * @param <T> 戻り値の型
     * @return 設定値
     */
    private <T> T getValue(@NotNull FileConfiguration config,
                           @NotNull String path,
                           @NotNull T defaultValue,
                           @NotNull Function<String, T> getter) {
        if (config.contains(path)) {
            return getter.apply(path);
        }
        return defaultValue;
    }

    /**
     * リスト値を取得するジェネリックメソッド
     *
     * @param config 設定
     * @param path パス
     * @param getter 値を取得する関数
     * @param <T> リスト要素の型
     * @return 設定値、存在しない場合は空リスト
     */
    @NotNull
    private <T> List<T> getList(@NotNull FileConfiguration config,
                                @NotNull String path,
                                @NotNull Function<String, List<T>> getter) {
        if (config.contains(path)) {
            return getter.apply(path);
        }
        return new ArrayList<>();
    }

    // ============================================================
    // 公開ゲッターメソッド（互換性維持）
    // ============================================================

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
        return getValue(config, path, def, p -> config.getString(p, def));
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
        return getValue(config, path, def, p -> config.getInt(p, def));
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
        return getValue(config, path, def, p -> config.getLong(p, def));
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
        return getValue(config, path, def, p -> config.getDouble(p, def));
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
        return getValue(config, path, def, p -> config.getBoolean(p, def));
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
        return getList(config, path, config::getStringList);
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
        return getList(config, path, config::getIntegerList);
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
        if (!config.contains(path)) {
            return new HashMap<>();
        }

        var section = config.getConfigurationSection(path);
        if (section == null) {
            return new HashMap<>();
        }

        Map<String, String> map = new HashMap<>();
        for (String key : section.getKeys(false)) {
            String value = section.getString(key);
            if (value != null) {
                map.put(key, value);
            }
        }
        return map;
    }

    // ============================================================
    // ファイル検索メソッド
    // ============================================================

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

    // ============================================================
    // バリデーションメソッド（ジェネリック化）
    // ============================================================

    /**
     * 範囲検証を行うジェネリックメソッド
     *
     * @param value 値
     * @param min 最小値
     * @param max 最大値
     * @param comparator 比較関数
     * @param <T> 比較可能な型
     * @return 範囲内の場合はtrue
     */
    private <T extends Comparable<T>> boolean validateRange(@NotNull T value,
                                                             @NotNull T min,
                                                             @NotNull T max) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    /**
     * 範囲検証を行い、範囲外の場合に警告ログを出力するジェネリックメソッド
     *
     * @param value 値
     * @param min 最小値
     * @param max 最大値
     * @param fieldName フィールド名（エラーメッセージ用）
     * @param fileName ファイル名（エラーメッセージ用）
     * @param valueFormatter 値のフォーマット関数
     * @param <T> 比較可能な型
     * @return 範囲内の場合はtrue
     */
    private <T extends Comparable<T>> boolean validateRangeWithLog(@NotNull T value,
                                                                   @NotNull T min,
                                                                   @NotNull T max,
                                                                   @NotNull String fieldName,
                                                                   @NotNull String fileName,
                                                                   @NotNull Function<T, String> valueFormatter) {
        boolean inRange = validateRange(value, min, max);
        if (!inRange) {
            logger.warning(String.format(
                "[Config] %s in %s: %s is out of range [%s, %s]",
                fieldName, fileName, valueFormatter.apply(value), valueFormatter.apply(min), valueFormatter.apply(max)
            ));
        }
        return inRange;
    }

    /**
     * 範囲検証を行います（int）
     *
     * @param value 値
     * @param min 最小値
     * @param max 最大値
     * @return 範囲内の場合はtrue
     */
    public boolean validateRange(int value, int min, int max) {
        return validateRange(value, min, max);
    }

    /**
     * 範囲検証を行います（double）
     *
     * @param value 値
     * @param min 最小値
     * @param max 最大値
     * @return 範囲内の場合はtrue
     */
    public boolean validateRange(double value, double min, double max) {
        return validateRange(value, min, max);
    }

    /**
     * 範囲検証を行い、範囲外の場合に警告ログを出力します（double）
     *
     * @param value 値
     * @param min 最小値
     * @param max 最大値
     * @param fieldName フィールド名（エラーメッセージ用）
     * @param fileName ファイル名（エラーメッセージ用）
     * @return 範囲内の場合はtrue
     */
    public boolean validateRange(double value, double min, double max, String fieldName, String fileName) {
        return validateRangeWithLog(value, min, max, fieldName, fileName, v -> String.format("%.2f", v));
    }

    /**
     * 範囲検証を行い、範囲外の場合に警告ログを出力します（int）
     *
     * @param value 値
     * @param min 最小値
     * @param max 最大値
     * @param fieldName フィールド名（エラーメッセージ用）
     * @param fileName ファイル名（エラーメッセージ用）
     * @return 範囲内の場合はtrue
     */
    public boolean validateRange(int value, int min, int max, String fieldName, String fileName) {
        return validateRangeWithLog(value, min, max, fieldName, fileName, String::valueOf);
    }

    // ============================================================
    // その他のバリデーションメソッド
    // ============================================================

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
        return expectedType.isInstance(config.get(path));
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
}
