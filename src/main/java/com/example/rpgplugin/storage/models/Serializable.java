package com.example.rpgplugin.storage.models;

/**
 * シリアライズ可能なデータを表すインターフェース
 */
public interface Serializable {

    /**
     * データをシリアライズして文字列として返す
     *
     * @return シリアライズされたデータ
     */
    String serialize();

    /**
     * シリアライズされたデータをデシリアライズする
     *
     * @param data シリアライズされたデータ
     */
    void deserialize(String data);
}
