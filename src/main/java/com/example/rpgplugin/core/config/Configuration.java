package com.example.rpgplugin.core.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 設定インターフェース
 *
 * <p>設定値の取得・設定・バリデーションを行うための基底インターフェースです。</p>
 *
 * @param <T> 設定の型
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public interface Configuration<T extends Configuration<T>> {

    /**
     * 設定をリロードします
     *
     * @throws Exception リロードに失敗した場合
     */
    void reload() throws Exception;

    /**
     * 設定を保存します
     *
     * @throws Exception 保存に失敗した場合
     */
    void save() throws Exception;

    /**
     * 設定値を取得します
     *
     * @param path 設定パス（ドット区切り）
     * @return 設定値、存在しない場合はnull
     */
    @Nullable
    Object get(@NotNull String path);

    /**
     * 文字列設定値を取得します
     *
     * @param path 設定パス
     * @param def デフォルト値
     * @return 設定値
     */
    @NotNull
    String getString(@NotNull String path, @NotNull String def);

    /**
     * 整数設定値を取得します
     *
     * @param path 設定パス
     * @param def デフォルト値
     * @return 設定値
     */
    int getInt(@NotNull String path, int def);

    /**
     * 長整数設定値を取得します
     *
     * @param path 設定パス
     * @param def デフォルト値
     * @return 設定値
     */
    long getLong(@NotNull String path, long def);

    /**
     * 実数設定値を取得します
     *
     * @param path 設定パス
     * @param def デフォルト値
     * @return 設定値
     */
    double getDouble(@NotNull String path, double def);

    /**
     * 真偽値設定値を取得します
     *
     * @param path 設定パス
     * @param def デフォルト値
     * @return 設定値
     */
    boolean getBoolean(@NotNull String path, boolean def);

    /**
     * リスト設定値を取得します
     *
     * @param path 設定パス
     * @return 設定値、存在しない場合は空リスト
     */
    @NotNull
    List<String> getStringList(@NotNull String path);

    /**
     * 整数リスト設定値を取得します
     *
     * @param path 設定パス
     * @return 設定値、存在しない場合は空リスト
     */
    @NotNull
    List<Integer> getIntegerList(@NotNull String path);

    /**
     * 文字列マップ設定値を取得します
     *
     * @param path 設定パス
     * @return 設定値、存在しない場合は空マップ
     */
    @NotNull
    Map<String, String> getStringMap(@NotNull String path);

    /**
     * 設定セクションを取得します
     *
     * @param path 設定パス
     * @return 設定セクション、存在しない場合はnull
     */
    @Nullable
    ConfigurationSection getSection(@NotNull String path);

    /**
     * 設定値を設定します
     *
     * @param path 設定パス
     * @param value 設定値
     */
    void set(@NotNull String path, @Nullable Object value);

    /**
     * 設定が存在するかを確認します
     *
     * @param path 設定パス
     * @return 存在する場合はtrue
     */
    boolean contains(@NotNull String path);

    /**
     * すべての設定キーを取得します
     *
     * @param deep ネストされたキーも含めるか
     * @return 設定キーのセット
     */
    @NotNull
    Set<String> getKeys(boolean deep);

    /**
     * 設定をバリデートします
     *
     * @return バリデーション結果
     */
    @NotNull
    ValidationResult validate();

    /**
     * デフォルト値を設定します
     */
    void setDefaults();

    /**
     * 設定ファイルのパスを取得します
     *
     * @return ファイルパス
     */
    @NotNull
    String getFilePath();

    /**
     * 設定名を取得します
     *
     * @return 設定名
     */
    @NotNull
    String getName();

    /**
     * バリデーション結果クラス
     */
    class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
