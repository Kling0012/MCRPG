package com.example.rpgplugin.skill.component;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * コンポーネント設定クラス
 * <p>YAMLから読み込んだ設定を保持します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ComponentSettings {

    private final Map<String, Object> data = new HashMap<>();

    /**
     * 設定をロードします
     *
     * @param config 設定セクション
     */
    public void load(ConfigurationSection config) {
        if (config == null) {
            return;
        }
        for (String key : config.getKeys(false)) {
            Object value = config.get(key);
            if (value != null) {
                data.put(key, value);
            }
        }
    }

    /**
     * 設定が存在するか確認します
     *
     * @param key キー
     * @return 存在する場合はtrue
     */
    public boolean has(String key) {
        return data.containsKey(key);
    }

    /**
     * すべてのキーを取得します
     *
     * @return キーセット
     */
    public Set<String> getKeys() {
        return data.keySet();
    }

    /**
     * 文字列値を取得します
     *
     * @param key      キー
     * @param fallback デフォルト値
     * @return 設定値
     */
    public String getString(String key, String fallback) {
        Object value = data.get(key);
        if (value == null) {
            return fallback;
        }
        return value.toString();
    }

    /**
     * 整数値を取得します
     *
     * @param key      キー
     * @param fallback デフォルト値
     * @return 設定値
     */
    public int getInt(String key, int fallback) {
        Object value = data.get(key);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * 小数値を取得します
     *
     * @param key      キー
     * @param fallback デフォルト値
     * @return 設定値
     */
    public double getDouble(String key, double fallback) {
        Object value = data.get(key);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * 真偽値を取得します
     *
     * @param key      キー
     * @param fallback デフォルト値
     * @return 設定値
     */
    public boolean getBoolean(String key, boolean fallback) {
        Object value = data.get(key);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String str = value.toString().toLowerCase();
        return str.equals("true") || str.equals("yes") || str.equals("1");
    }

    /**
     * 値を設定します
     *
     * @param key   キー
     * @param value 値
     */
    public void set(String key, Object value) {
        data.put(key, value);
    }

    /**
     * 値を設定します（setメソッドのエイリアス）
     *
     * @param key   キー
     * @param value 値
     * @deprecated {@link #set(String, Object)}を使用してください
     */
    @Deprecated
    public void put(String key, Object value) {
        set(key, value);
    }

    /**
     * 複数の設定を追加します
     *
     * @param other 追加する設定
     */
    public void putAll(ComponentSettings other) {
        if (other != null) {
            data.putAll(other.data);
        }
    }

    /**
     * 設定をクリアします
     */
    public void clear() {
        data.clear();
    }
}
