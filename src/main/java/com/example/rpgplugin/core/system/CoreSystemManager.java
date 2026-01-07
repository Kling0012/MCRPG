package com.example.rpgplugin.core.system;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.core.config.YamlConfigManager;
import com.example.rpgplugin.core.dependency.DependencyManager;
import com.example.rpgplugin.core.module.ModuleManager;
import com.example.rpgplugin.core.config.ConfigWatcher;
import com.example.rpgplugin.storage.StorageManager;

/**
 * コアシステムの統合マネージャー（ファサード）
 *
 * <p>責務:</p>
 * <ul>
 *   <li>設定管理（YamlConfigManager, ConfigWatcher）</li>
 *   <li>依存関係管理（DependencyManager）</li>
 *   <li>モジュール管理（ModuleManager）</li>
 *   <li>データストレージ管理（StorageManager）</li>
 * </ul>
 *
 * <p>Single Responsibility: プラグインの基盤機能の統合管理</p>
 *
 * <p>初期化順序:</p>
 * <ol>
 *   <li>YamlConfigManager（他のシステムの設定に必要）</li>
 *   <li>DependencyManager（外部プラグインのチェック）</li>
 *   <li>StorageManager（データ永続化）</li>
 *   <li>ConfigWatcher（設定ファイルの監視）</li>
 *   <li>ModuleManager（モジュールの有効化）</li>
 * </ol>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class CoreSystemManager {
    private final RPGPlugin plugin;

    // 設定システム
    private final YamlConfigManager configManager;
    private final ConfigWatcher configWatcher;

    // 依存関係システム
    private final DependencyManager dependencyManager;

    // モジュールシステム
    private final ModuleManager moduleManager;

    // データストレージシステム
    private final StorageManager storageManager;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public CoreSystemManager(RPGPlugin plugin) {
        this.plugin = plugin;

        // 初期化順序を考慮してインスタンス化
        this.configManager = new YamlConfigManager(plugin);
        this.dependencyManager = new DependencyManager(plugin);
        this.storageManager = new StorageManager(plugin);
        this.configWatcher = new ConfigWatcher(plugin, configManager);
        this.moduleManager = new ModuleManager(plugin);
    }

    /**
     * コアシステムを初期化する
     *
     * @throws Exception 初期化に失敗した場合
     */
    public void initialize() throws Exception {
        plugin.getLogger().info("========================================");
        plugin.getLogger().info(" CoreSystemManager: 初期化を開始します");
        plugin.getLogger().info("========================================");

        // 1. 設定マネージャーの初期化
        plugin.getLogger().info("[CoreSystem] 設定マネージャーを初期化中...");
        // YamlConfigManager は initialize() メソッドを持たない

        // 2. 依存関係のセットアップ
        plugin.getLogger().info("[CoreSystem] 依存関係をチェック中...");
        if (!dependencyManager.setupDependencies()) {
            throw new IllegalStateException("必須の依存関係が見つかりません");
        }

        // 3. ストレージシステムの初期化
        plugin.getLogger().info("[CoreSystem] ストレージシステムを初期化中...");
        storageManager.initialize();

        // 4. 設定ファイル監視の開始
        plugin.getLogger().info("[CoreSystem] 設定ファイル監視を開始中...");
        if (!configWatcher.start()) {
            plugin.getLogger().warning("[CoreSystem] 設定ファイル監視の開始に失敗しました");
        }

        // 5. モジュールマネージャーの初期化
        plugin.getLogger().info("[CoreSystem] モジュールマネージャーを初期化中...");
        // ModuleManager は initialize() メソッドを持たない

        plugin.getLogger().info("========================================");
        plugin.getLogger().info(" CoreSystemManager: 初期化が完了しました");
        plugin.getLogger().info("========================================");
    }

    /**
     * コアシステムをシャットダウンする
     */
    public void shutdown() {
        plugin.getLogger().info("[CoreSystem] シャットダウンを開始します");

        // モジュールの無効化
        if (moduleManager != null) {
            moduleManager.disableAll();
        }

        // 設定監視の停止
        if (configWatcher != null) {
            configWatcher.stop();
        }

        // ストレージのクローズ
        if (storageManager != null) {
            storageManager.shutdown();
        }

        // 依存関係のクリーンアップ
        if (dependencyManager != null) {
            dependencyManager.cleanup();
        }

        plugin.getLogger().info("[CoreSystem] シャットダウンが完了しました");
    }

    // Getter メソッド（後方互換性）

    /**
     * 設定マネージャーを取得する
     *
     * @return YamlConfigManager インスタンス
     */
    public YamlConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * 設定監視クラスを取得する
     *
     * @return ConfigWatcher インスタンス
     */
    public ConfigWatcher getConfigWatcher() {
        return configWatcher;
    }

    /**
     * 依存関係マネージャーを取得する
     *
     * @return DependencyManager インスタンス
     */
    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    /**
     * モジュールマネージャーを取得する
     *
     * @return ModuleManager インスタンス
     */
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    /**
     * ストレージマネージャーを取得する
     *
     * @return StorageManager インスタンス
     */
    public StorageManager getStorageManager() {
        return storageManager;
    }
}
