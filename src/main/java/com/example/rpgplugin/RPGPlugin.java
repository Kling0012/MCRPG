package com.example.rpgplugin;

import com.example.rpgplugin.core.config.ConfigWatcher;
import com.example.rpgplugin.core.config.YamlConfigManager;
import com.example.rpgplugin.core.dependency.DependencyManager;
import com.example.rpgplugin.core.module.ModuleManager;
import com.example.rpgplugin.gui.menu.StatMenuListener;
import com.example.rpgplugin.stats.StatManager;
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
    private YamlConfigManager configManager;
    private ConfigWatcher configWatcher;
    private StatManager statManager;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("========================================");
        getLogger().info(" RPGPlugin is starting...");
        getLogger().info(" Version: " + getDescription().getVersion());
        getLogger().info("========================================");

        try {
            // 設定マネージャーの初期化
            setupConfigManager();

            // ファイル監視の開始
            setupConfigWatcher();

            // 依存関係のセットアップ
            if (!setupDependencies()) {
                getLogger().severe("Failed to setup dependencies. Plugin will be disabled.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            // ストレージシステムの初期化
            initializeStorage();

            // ステータスシステムの初期化
            initializeStatManager();

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
            // ファイル監視の停止
            if (configWatcher != null) {
                configWatcher.stop();
            }

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

            // 設定のアンロード
            if (configManager != null) {
                configManager.unloadAll();
            }

            getLogger().info("RPGPlugin has been disabled successfully!");

        } catch (Exception e) {
            getLogger().severe("Error during plugin shutdown:");
            e.printStackTrace();
        }
    }

    /**
     * 設定マネージャーをセットアップします
     */
    private void setupConfigManager() {
        configManager = new YamlConfigManager(this);

        // メイン設定ファイルを読み込み
        boolean loaded = configManager.loadConfig(
            "main",
            "config.yml",
            null // デフォルトファイルはリソースからコピーされるためnull
        );

        if (loaded) {
            getLogger().info("Main config loaded successfully.");
        } else {
            getLogger().warning("Failed to load main config, using defaults.");
        }

        // デバッグログレベルを設定
        String logLevel = configManager.getString("main", "debug.log_level", "INFO");
        setLogLevel(logLevel);
    }

    /**
     * ファイル監視をセットアップします
     */
    private void setupConfigWatcher() {
        configWatcher = new ConfigWatcher(this, configManager);

        // 監視を開始
        if (!configWatcher.start()) {
            getLogger().warning("Failed to start ConfigWatcher");
            return;
        }

        // ホットリロード設定を確認
        boolean hotReloadClasses = configManager.getBoolean("main", "hot_reload.classes", true);
        boolean hotReloadSkills = configManager.getBoolean("main", "hot_reload.skills", true);
        boolean hotReloadExp = configManager.getBoolean("main", "hot_reload.exp_diminish", false);

        // クラス定義の監視
        if (hotReloadClasses) {
            configWatcher.watchDirectory("classes");
            configWatcher.enableAutoReload("classes", "classes");
        }

        // スキル定義の監視
        if (hotReloadSkills) {
            configWatcher.watchDirectory("skills");
            configWatcher.enableAutoReload("skills", "skills");
        }

        // 経験値減衰設定の監視
        if (hotReloadExp) {
            configWatcher.watchDirectory("exp");
            configWatcher.enableAutoReload("exp", "exp");
        }

        getLogger().info("ConfigWatcher initialized with " + configWatcher.getWatchedDirectoryCount() + " directories.");
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
     * ログレベルを設定します
     *
     * @param level ログレベル文字列
     */
    private void setLogLevel(String level) {
        try {
            java.util.logging.Level logLevel = java.util.logging.Level.parse(level.toUpperCase());
            getLogger().setLevel(logLevel);
        } catch (Exception e) {
            getLogger().warning("Invalid log level: " + level + ", using INFO");
        }
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
            getServer().getPluginManager().registerEvents(new StatMenuListener(this), this);
            getLogger().info("Listeners registered.");
        } catch (Exception e) {
            getLogger().warning("Failed to register listeners: " + e.getMessage());
        }
    }

    /**
     * ステータスマネージャーを初期化
     */
    private void initializeStatManager() {
        statManager = new StatManager(this);
        getLogger().info("StatManager initialized.");
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
     * 設定マネージャーを取得します
     *
     * @return YamlConfigManagerインスタンス
     */
    public YamlConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * ファイル監視を取得します
     *
     * @return ConfigWatcherインスタンス
     */
    public ConfigWatcher getConfigWatcher() {
        return configWatcher;
    }

    /**
     * ステータスマネージャーを取得します
     *
     * @return StatManagerインスタンス
     */
    public StatManager getStatManager() {
        return statManager;
    }

    /**
     * プラグインをリロードします
     *
     * <p>実行中に設定を再読み込みする際に使用します。</p>
     */
    public void reloadPlugin() {
        getLogger().info("Reloading plugin...");

        try {
            // 設定ファイルのリロード
            if (configManager != null) {
                int reloaded = configManager.reloadAll();
                getLogger().info("Reloaded " + reloaded + " config files.");
            }

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
