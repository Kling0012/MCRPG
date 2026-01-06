package com.example.rpgplugin;

import com.example.rpgplugin.core.dependency.DependencyManager;
import com.example.rpgplugin.core.module.ModuleManager;
import com.example.rpgplugin.storage.StorageManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * RPGプラグイン メインクラス
 *
 * <p>モジュールシステムと依存性管理を統合し、プラグインのライフサイクルを管理します。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class RPGPlugin extends JavaPlugin {

    private static RPGPlugin instance;
    private StorageManager storageManager;

    // マネージャー
    private DependencyManager dependencyManager;
    private ModuleManager moduleManager;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("========================================");
        getLogger().info(" RPGPlugin is starting...");
        getLogger().info(" Version: " + getDescription().getVersion());
        getLogger().info("========================================");

        try {
            // 依存関係のセットアップ
            if (!setupDependencies()) {
                getLogger().severe("Failed to setup dependencies. Plugin will be disabled.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            // ストレージシステムの初期化
            initializeStorage();

            // モジュールマネージャーの初期化
            setupModuleManager();

            // モジュールの有効化
            enableModules();

            // コマンドハンドラーの登録
            registerCommands();

            // リスナーの登録
            registerListeners();

            getLogger().info("========================================");
            getLogger().info(" RPGPlugin has been enabled successfully!");
            getLogger().info(" Loaded modules: " + moduleManager.getModuleCount());
            getLogger().info("========================================");

        } catch (Exception e) {
            getLogger().severe("Failed to enable RPGPlugin:");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("RPGPlugin is shutting down...");

        try {
            // モジュールの無効化
            if (moduleManager != null) {
                moduleManager.disableAll();
            }

            // ストレージシステムのシャットダウン
            if (storageManager != null) {
                storageManager.shutdown();
            }

            // 依存関係のクリーンアップ
            if (dependencyManager != null) {
                dependencyManager.cleanup();
            }

            getLogger().info("RPGPlugin has been disabled successfully!");

        } catch (Exception e) {
            getLogger().severe("Error during plugin shutdown:");
            e.printStackTrace();
        }
    }

    /**
     * ストレージシステムを初期化
     */
    private void initializeStorage() throws Exception {
        getLogger().info("Initializing storage system...");
        storageManager = new StorageManager(this);
        storageManager.initialize();
        getLogger().info("Storage system initialized!");
    }

    /**
     * 依存関係をセットアップします
     *
     * @return セットアップに成功した場合はtrue
     */
    private boolean setupDependencies() {
        dependencyManager = new DependencyManager(this);
        return dependencyManager.setupDependencies();
    }

    /**
     * モジュールマネージャーをセットアップします
     */
    private void setupModuleManager() {
        moduleManager = new ModuleManager(this);
        getLogger().info("ModuleManager initialized.");
    }

    /**
     * 全モジュールを有効化します
     */
    private void enableModules() {
        if (moduleManager == null) {
            getLogger().warning("ModuleManager is not initialized!");
            return;
        }

        int enabled = moduleManager.enableAll();
        getLogger().info("Enabled " + enabled + " modules.");
    }

    /**
     * コマンドを登録します
     */
    private void registerCommands() {
        try {
            getCommand("rpg").setExecutor(new RPGCommand());
            getLogger().info("Commands registered.");
        } catch (Exception e) {
            getLogger().warning("Failed to register commands: " + e.getMessage());
        }
    }

    /**
     * リスナーを登録します
     */
    private void registerListeners() {
        try {
            getServer().getPluginManager().registerEvents(new RPGListener(), this);
            getLogger().info("Listeners registered.");
        } catch (Exception e) {
            getLogger().warning("Failed to register listeners: " + e.getMessage());
        }
    }

    /**
     * プラグインインスタンスを取得します
     *
     * @return プラグインインスタンス
     */
    public static RPGPlugin getInstance() {
        return instance;
    }

    /**
     * ストレージマネージャーを取得
     *
     * @return ストレージマネージャー
     */
    public StorageManager getStorageManager() {
        return storageManager;
    }

    /**
     * 依存関係マネージャーを取得します
     *
     * @return DependencyManagerインスタンス
     */
    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    /**
     * モジュールマネージャーを取得します
     *
     * @return ModuleManagerインスタンス
     */
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    /**
     * プラグインをリロードします
     *
     * <p>実行中に設定を再読み込みする際に使用します。</p>
     */
    public void reloadPlugin() {
        getLogger().info("Reloading plugin...");

        try {
            // モジュールのリロード
            if (moduleManager != null) {
                moduleManager.reloadAll();
            }

            // 依存関係の状態をログ出力
            if (dependencyManager != null) {
                dependencyManager.logDependencyStatus();
            }

            getLogger().info("Plugin reloaded successfully!");

        } catch (Exception e) {
            getLogger().severe("Failed to reload plugin:");
            e.printStackTrace();
        }
    }
}
