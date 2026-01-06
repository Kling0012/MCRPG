package com.example.rpgplugin.core.config;

import org.jetbrains.annotations.NotNull;

/**
 * リロード戦略列挙型
 *
 * <p>設定ファイルのリロード方法を定義します。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public enum ReloadStrategy {

    /**
     * 即時リロード
     *
     * <p>ファイル変更時に自動的にリロードします。</p>
     * <p>クラス定義・スキル定義など、頻繁に変更される設定に適しています。</p>
     */
    IMMEDIATE("即時リロード", true),

    /**
     * 手動リロード
     *
     * <p>コマンド実行時のみリロードします。</p>
     * <p>メイン設定など、重要な設定に適しています。</p>
     */
    MANUAL("手動リロード", false),

    /**
     * 個別リロード
     *
     * <p>個別のファイルのみリロードします。</p>
     * <p>経験値減衰設定など、独立した設定に適しています。</p>
     */
    INDIVIDUAL("個別リロード", false),

    /**
     * 遅延リロード
     *
     * <p>変更後一定時間経過後にリロードします。</p>
     * <p>複数の変更をまとめて適用する場合に適しています。</p>
     */
    DELAYED("遅延リロード", false);

    private final String displayName;
    private final boolean autoReload;

    /**
     * コンストラクタ
     *
     * @param displayName 表示名
     * @param autoReload 自動リロードするか
     */
    ReloadStrategy(@NotNull String displayName, boolean autoReload) {
        this.displayName = displayName;
        this.autoReload = autoReload;
    }

    /**
     * 表示名を取得します
     *
     * @return 表示名
     */
    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 自動リロードするかを確認します
     *
     * @return 自動リロードする場合はtrue
     */
    public boolean isAutoReload() {
        return autoReload;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
