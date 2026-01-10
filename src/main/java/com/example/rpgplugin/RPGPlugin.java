package com.example.rpgplugin;

import com.example.rpgplugin.api.command.APICommand;
import com.example.rpgplugin.core.config.ConfigWatcher;
import com.example.rpgplugin.core.config.YamlConfigManager;
import com.example.rpgplugin.core.dependency.DependencyManager;
import com.example.rpgplugin.core.module.ModuleManager;
import com.example.rpgplugin.core.system.CoreSystemManager;
import com.example.rpgplugin.core.system.GameSystemManager;
import com.example.rpgplugin.core.system.ExternalSystemManager;
import com.example.rpgplugin.mythicmobs.MythicMobsManager;
import com.example.rpgplugin.mythicmobs.config.MobDropConfig;
import com.example.rpgplugin.mythicmobs.listener.MythicDeathListener;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.ExpDiminisher;
import com.example.rpgplugin.player.VanillaExpHandler;
import com.example.rpgplugin.rpgclass.ClassLoader;
import com.example.rpgplugin.rpgclass.RPGClass;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.config.SkillConfig;
import com.example.rpgplugin.skill.executor.ActiveSkillExecutor;
import com.example.rpgplugin.skill.executor.PassiveSkillExecutor;
import com.example.rpgplugin.stats.StatManager;
import com.example.rpgplugin.storage.StorageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * RPGプラグイン メインクラス
 *
 * <p>モジュールシステムと依存性管理を統合し、プラグインのライフサイクルを管理します。</p>
 *
 * <p>ファサードパターンを採用し、4つのシステムマネージャーで全機能を統合管理します。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class RPGPlugin extends JavaPlugin {

    private static RPGPlugin instance;

    // ファサードシステム
    private CoreSystemManager coreSystem;
    private GameSystemManager gameSystem;
    private ExternalSystemManager externalSystem;

    // PlaceholderAPI連携
    private com.example.rpgplugin.api.placeholder.RPGPlaceholderExpansion placeholderExpansion;

    @Override
    public void onEnable() {
        getLogger().info("========================================");
        getLogger().info(" RPGPlugin is starting...");
        getLogger().info(" Version: " + getDescription().getVersion());
        getLogger().info("========================================");

        try {
            // 1. コアシステムの初期化
            coreSystem = new CoreSystemManager(this);
            coreSystem.initialize();

            // メイン設定ファイルを読み込み
            setupMainConfig();

            // ファイル監視の設定
            setupConfigWatcher();

            // モジュールの有効化
            enableModules();

            // 2. ゲームシステムの初期化
            gameSystem = new GameSystemManager(this, coreSystem);
            gameSystem.initialize();

            // ゲームシステムの追加初期化
            setupGameSystemExtensions();

            // 3. 外部システムの初期化
            externalSystem = new ExternalSystemManager(this, coreSystem.getStorageManager().getDatabaseManager().getConnectionPool());
            externalSystem.initialize();

            // 外部システムの追加初期化
            setupExternalSystemExtensions();

            // 4. PlaceholderAPI連携
            setupIntegrations();

            // 5. コマンド・リスナー登録
            registerCommands();
            registerListeners();

            // 初期化成功後にinstanceを設定
            instance = this;

            getLogger().info("========================================");
            getLogger().info(" RPGPlugin has been enabled successfully!");
            getLogger().info(" Loaded modules: " + coreSystem.getModuleManager().getModuleCount());
            getLogger().info("========================================");

        } catch (Exception e) {
            getLogger().severe("Failed to enable RPGPlugin:");
            e.printStackTrace();
            // 初期化失敗時はinstanceを設定せず、プラグインを無効化
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("========================================");
        getLogger().info(" RPGPlugin is shutting down...");
        getLogger().info("========================================");

        try {
            // 外部システムのシャットダウン
            if (externalSystem != null) {
                externalSystem.shutdown();
            }

            // ゲームシステムのシャットダウン
            if (gameSystem != null) {
                gameSystem.shutdown();
            }

            // コアシステムのシャットダウン
            if (coreSystem != null) {
                coreSystem.shutdown();
            }

            getLogger().info("========================================");
            getLogger().info(" RPGPlugin has been disabled successfully!");
            getLogger().info("========================================");

        } catch (Exception e) {
            getLogger().severe("Error during plugin shutdown:");
            e.printStackTrace();
        } finally {
            // プラグイン無効化時にinstanceをクリア
            instance = null;
        }
    }

    // ====== 設定とヘルパーメソッド ======

    /**
     * メイン設定ファイルをセットアップします
     */
    private void setupMainConfig() {
        YamlConfigManager configManager = coreSystem.getConfigManager();

        // デフォルト設定ファイルが存在しない場合はJARからコピー
        java.io.File configFile = new java.io.File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
            getLogger().info("Created default config.yml from JAR.");
        }

        // メイン設定ファイルを読み込み
        boolean loaded = configManager.loadConfig(
            "main",
            "config.yml",
            null
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
        ConfigWatcher configWatcher = coreSystem.getConfigWatcher();

        // ホットリロード設定を確認
        YamlConfigManager configManager = coreSystem.getConfigManager();
        boolean hotReloadClasses = configManager.getBoolean("main", "hot_reload.classes", true);
        boolean hotReloadSkills = configManager.getBoolean("main", "hot_reload.skills", true);
        boolean hotReloadExp = configManager.getBoolean("main", "hot_reload.exp_diminish", true);
        boolean hotReloadMobs = configManager.getBoolean("main", "hot_reload.mobs", true);
        boolean hotReloadTemplates = configManager.getBoolean("main", "hot_reload.templates", false);

        // クラス定義の監視
        if (hotReloadClasses) {
            configWatcher.watchDirectory("classes");
            configWatcher.addDirectoryListener("classes", path -> {
                try {
                    getLogger().info("[HotReload] Class file modified: " + path.getFileName());
                    com.example.rpgplugin.rpgclass.ClassLoader classLoader = gameSystem.getClassLoader();
                    Map<String, RPGClass> classes = classLoader.loadAllClasses();
                    ClassManager.ReloadResult result = gameSystem.getClassManager().reloadWithCleanup(classes);
                    getLogger().info("[HotReload] Reloaded " + result.getLoadedClassCount() + " classes." +
                        (result.hasRemovedClasses() ? " Removed: " + result.getRemovedClasses().size() : ""));
                } catch (Exception e) {
                    getLogger().warning("[HotReload] Failed to reload classes: " + e.getMessage());
                }
            });
        }

        // スキル定義の監視
        if (hotReloadSkills) {
            // スキルディレクトリ（サブディレクトリも監視）
            configWatcher.watchDirectory("skills/active");
            configWatcher.watchDirectory("skills/passive");

            configWatcher.addDirectoryListener("skills/active", path -> {
                reloadSkillsWithCleanup();
            });
            configWatcher.addDirectoryListener("skills/passive", path -> {
                reloadSkillsWithCleanup();
            });
        }

        // 経験値減衰設定の監視
        if (hotReloadExp) {
            configWatcher.watchDirectory("exp");
            configWatcher.addDirectoryListener("exp", path -> {
                try {
                    if (path.getFileName().toString().equals("diminish_config.yml")) {
                        getLogger().info("[HotReload] Exp config modified");
                        getExpDiminisher().loadConfig();
                        getLogger().info("[HotReload] Reloaded diminish_config.yml");
                    }
                } catch (Exception e) {
                    getLogger().warning("[HotReload] Failed to reload exp config: " + e.getMessage());
                }
            });
        }

        // モブドロップ設定の監視
        if (hotReloadMobs && coreSystem.getDependencyManager().isMythicMobsAvailable()) {
            configWatcher.watchDirectory("mobs");
            configWatcher.addDirectoryListener("mobs", path -> {
                try {
                    if (path.getFileName().toString().equals("mob_drops.yml")) {
                        getLogger().info("[HotReload] Mob drops config modified");
                        loadMobDropConfigs();
                        getLogger().info("[HotReload] Reloaded mob_drops.yml");
                    }
                } catch (Exception e) {
                    getLogger().warning("[HotReload] Failed to reload mob drops: " + e.getMessage());
                }
            });
        }

        // テンプレートファイルの監視（ログ出力のみ）
        if (hotReloadTemplates) {
            configWatcher.watchDirectory("templates");
            configWatcher.addDirectoryListener("templates", path -> {
                getLogger().info("[HotReload] Template file modified: " + path.getFileName() + ". Copy template to use as new skill/class.");
            });
        }

        getLogger().info("ConfigWatcher initialized with " + configWatcher.getWatchedDirectoryCount() + " directories.");
    }

    /**
     * スキルをリロードするヘルパーメソッド（旧互換）
     */
    private void reloadSkills() {
        try {
            SkillConfig skillConfig = gameSystem.getSkillConfig();
            int count = skillConfig.reloadSkills();
            getLogger().info("[HotReload] Reloaded " + count + " skills.");
        } catch (Exception e) {
            getLogger().warning("[HotReload] Failed to reload skills: " + e.getMessage());
        }
    }

    /**
     * スキルをクリーンアップ付きでリロードするヘルパーメソッド
     */
    private void reloadSkillsWithCleanup() {
        try {
            com.example.rpgplugin.skill.SkillLoader skillLoader = gameSystem.getSkillLoader();
            var newSkills = skillLoader.loadAllSkills();
            var newSkillMap = newSkills.stream()
                    .collect(Collectors.toMap(s -> s.getId(), s -> s));
            SkillManager.ReloadResult result = gameSystem.getSkillManager().reloadWithCleanup(newSkillMap);
            getLogger().info("[HotReload] Reloaded " + result.getLoadedSkillCount() + " skills." +
                    (result.hasRemovedSkills() ? " Removed: " + result.getRemovedSkills().size() : ""));
        } catch (Exception e) {
            getLogger().warning("[HotReload] Failed to reload skills: " + e.getMessage());
        }
    }

    /**
     * 全モジュールを有効化します
     */
    private void enableModules() {
        ModuleManager moduleManager = coreSystem.getModuleManager();
        int enabled = moduleManager.enableAll();
        getLogger().info("Enabled " + enabled + " modules.");
    }

    /**
     * ゲームシステムの追加初期化を行います
     */
    private void setupGameSystemExtensions() {
        getLogger().info("Setting up game system extensions...");

        // クラス設定を読み込み
        com.example.rpgplugin.rpgclass.ClassLoader clsLoader =
            new com.example.rpgplugin.rpgclass.ClassLoader(this, gameSystem.getPlayerManager());
        Map<String, com.example.rpgplugin.rpgclass.RPGClass> classes = clsLoader.loadAllClasses();
        gameSystem.getClassManager().registerAll(classes);
        getLogger().info("Loaded " + classes.size() + " classes");

        // スキル設定を読み込み
        SkillConfig skillConfig = new SkillConfig(this, gameSystem.getSkillManager());
        int skillCount = skillConfig.loadSkills();
        getLogger().info("Loaded " + skillCount + " skills");

        // 経験値ハンドラーを登録
        gameSystem.getExpManager().registerListeners();

        // ダメージマネージャーを登録
        getServer().getPluginManager().registerEvents(gameSystem.getDamageManager(), this);

        // プレイヤーマネージャーを登録
        getServer().getPluginManager().registerEvents(gameSystem.getPlayerManager(), this);

        // 自動保存タスクを開始
        startAutoSaveTask();

        getLogger().info("Game system extensions setup complete!");
    }

    /**
     * 外部システムの追加初期化を行います
     */
    private void setupExternalSystemExtensions() {
        getLogger().info("Setting up external system extensions...");

        // MythicMobsが利用可能な場合のみ追加設定
        if (coreSystem.getDependencyManager().isMythicMobsAvailable()) {
            // ドロップ設定を読み込み
            loadMobDropConfigs();

            // 期限切れドロップクリーニングタスクを開始
            startDropCleanupTask();

            getLogger().info("MythicMobs extensions setup complete!");
        } else {
            getLogger().info("MythicMobs not available, skipping extensions.");
        }
    }

    /**
     * 外部プラグインとの統合をセットアップします
     */
    private void setupIntegrations() {
        getLogger().info("Setting up third-party integrations...");

        // PlaceholderAPI連携
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                placeholderExpansion = new com.example.rpgplugin.api.placeholder.RPGPlaceholderExpansion(this);
                placeholderExpansion.register();
                getLogger().info("PlaceholderAPI integration loaded successfully!");
            } catch (Exception e) {
                getLogger().warning("Failed to load PlaceholderAPI integration: " + e.getMessage());
            }
        } else {
            getLogger().info("PlaceholderAPI not installed, skipping PlaceholderAPI integration.");
        }

        // Skript連携
if (getServer().getPluginManager().getPlugin("Skript") != null) {
    getLogger().info("Skript detected - use Skript Reflect for direct API access. See /rpgapi");
} else {
    getLogger().info("Skript not installed.");
}

        getLogger().info("Third-party integrations setup complete!");
    }

    /**
     * モブドロップ設定を読み込み
     */
    private void loadMobDropConfigs() {
        getLogger().info("Loading mob drop configurations...");

        // mob_drops.ymlを保存（存在しない場合）
        saveResource("mobs/mob_drops.yml", false);

        // 設定ファイルを読み込み
        FileConfiguration dropConfig = coreSystem.getConfigManager().getConfig("mob_drops");
        if (dropConfig == null) {
            // 手動で読み込み
            try {
                dropConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                        new java.io.File(getDataFolder(), "mobs/mob_drops.yml")
                );
            } catch (Exception e) {
                getLogger().warning("Failed to load mob_drops.yml: " + e.getMessage());
                return;
            }
        }

        // MobDropConfigで設定を解析
        MobDropConfig configLoader = new MobDropConfig(getLogger());
        var mobDrops = configLoader.loadFromConfig(dropConfig);

        // マネージャーに設定をロード
        externalSystem.getMythicMobsManager().loadDropConfigs(mobDrops);

        getLogger().info("Loaded " + mobDrops.size() + " mob drop configurations");
    }

    /**
     * ドロップクリーニングタスクを開始
     */
    private void startDropCleanupTask() {
        // 10分ごとに期限切れドロップをクリーニング
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            MythicMobsManager mythicMobsManager = externalSystem.getMythicMobsManager();
            if (mythicMobsManager != null) {
                mythicMobsManager.cleanupExpiredDrops();
            }
        }, 10L * 60L * 20L, 10L * 60L * 20L); // 10分 = 12000 ticks

        getLogger().info("Drop cleanup task started (runs every 10 minutes)");
    }

    /**
     * 自動保存タスクを開始
     */
    private void startAutoSaveTask() {
        // 設定から自動保存間隔を取得（秒）
        int autoSaveInterval = getConfig().getInt("database.auto_save_interval", 300);

        if (autoSaveInterval > 0) {
            long intervalTicks = autoSaveInterval * 20L; // 秒をティックに変換

            getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                try {
                    gameSystem.getPlayerManager().saveAllAsync();
                } catch (Exception e) {
                    getLogger().warning("[AutoSave] Failed to save player data: " + e.getMessage());
                }
            }, intervalTicks, intervalTicks);

            getLogger().info("Auto-save task started: interval=" + autoSaveInterval + "s");
        } else {
            getLogger().info("Auto-save is disabled (auto_save_interval=0)");
        }
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
     * コマンドを登録します
     */
    private void registerCommands() {
        try {
            // メインRPGコマンド
            RPGCommand rpgCommand = new RPGCommand();
            getCommand("rpg").setExecutor(rpgCommand);
            getCommand("rpg").setTabCompleter(rpgCommand);

            // APIコマンド（Skript Reflectヘルプ用）
            APICommand apiCommand = new APICommand(this);
            PluginCommand apiCmd = getCommand("rpgapi");
            if (apiCmd != null) {
                apiCmd.setExecutor(apiCommand);
                apiCmd.setTabCompleter(apiCommand);
                getLogger().info("API command registered for Skript Reflect.");
            } else {
                getLogger().warning("API command 'rpgapi' not found in plugin.yml");
            }

            // RPG管理コマンド
            com.example.rpgplugin.command.RPGAdminCommand adminCommand =
                new com.example.rpgplugin.command.RPGAdminCommand(
                    this,
                    gameSystem.getClassManager(),
                    gameSystem.getSkillManager(),
                    gameSystem.getClassLoader(),
                    gameSystem.getSkillLoader()
                );
            getCommand("rpgadmin").setExecutor(adminCommand);
            getCommand("rpgadmin").setTabCompleter(adminCommand);

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
        getServer().getPluginManager().registerEvents(new com.example.rpgplugin.gui.SkillTreeGUIListener(), this);
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

    // ====== コアシステム ======

    /**
     * ストレージマネージャーを取得します
     *
     * @return ストレージマネージャー
     */
    public StorageManager getStorageManager() {
        return coreSystem.getStorageManager();
    }

    /**
     * 依存関係マネージャーを取得します
     *
     * @return DependencyManagerインスタンス
     */
    public DependencyManager getDependencyManager() {
        return coreSystem.getDependencyManager();
    }

    /**
     * モジュールマネージャーを取得します
     *
     * @return ModuleManagerインスタンス
     */
    public ModuleManager getModuleManager() {
        return coreSystem.getModuleManager();
    }

    /**
     * 設定マネージャーを取得します
     *
     * @return YamlConfigManagerインスタンス
     */
    public YamlConfigManager getConfigManager() {
        return coreSystem.getConfigManager();
    }

    /**
     * ファイル監視を取得します
     *
     * @return ConfigWatcherインスタンス
     */
    public ConfigWatcher getConfigWatcher() {
        return coreSystem.getConfigWatcher();
    }

    // ====== ゲームシステム ======

    /**
     * プレイヤーマネージャーを取得します
     *
     * @return プレイヤーマネージャー
     */
    public PlayerManager getPlayerManager() {
        return gameSystem.getPlayerManager();
    }

    /**
     * ステータスマネージャーを取得します
     *
     * @return ステータスマネージャー
     */
    public StatManager getStatManager() {
        return gameSystem.getStatManager();
    }

    /**
     * クラスマネージャーを取得します
     *
     * @return クラスマネージャー
     */
    public com.example.rpgplugin.rpgclass.ClassManager getClassManager() {
        return gameSystem.getClassManager();
    }

    /**
     * スキルマネージャーを取得します
     *
     * @return スキルマネージャー
     */
    public SkillManager getSkillManager() {
        return gameSystem.getSkillManager();
    }

    /**
     * ダメージマネージャーを取得します
     *
     * @return DamageManagerインスタンス
     */
    public com.example.rpgplugin.damage.DamageManager getDamageManager() {
        return gameSystem.getDamageManager();
    }

    /**
     * 経験値減衰マネージャーを取得します
     *
     * @return ExpDiminisherインスタンス
     */
    public ExpDiminisher getExpDiminisher() {
        return gameSystem.getExpManager().getExpDiminisher();
    }

    /**
     * バニラ経験値ハンドラーを取得します
     *
     * @return VanillaExpHandlerインスタンス
     */
    public VanillaExpHandler getVanillaExpHandler() {
        return gameSystem.getExpManager().getVanillaExpHandler();
    }

    // ====== スキル関連（特別なアクセサ） ======

    /**
     * スキル設定を取得します
     *
     * @return SkillConfigインスタンス
     */
    public SkillConfig getSkillConfig() {
        return gameSystem.getSkillConfig();
    }

    /**
     * アクティブスキルエグゼキューターを取得します
     *
     * @return ActiveSkillExecutorインスタンス
     */
    public ActiveSkillExecutor getActiveSkillExecutor() {
        return gameSystem.getActiveSkillExecutor();
    }

    /**
     * パッシブスキルエグゼキューターを取得します
     *
     * @return PassiveSkillExecutorインスタンス
     */
    public PassiveSkillExecutor getPassiveSkillExecutor() {
        return gameSystem.getPassiveSkillExecutor();
    }

    // ====== 外部システム ======

    /**
     * MythicMobsマネージャーを取得します
     *
     * @return MythicMobsManagerインスタンス
     */
    public MythicMobsManager getMythicMobsManager() {
        return externalSystem.getMythicMobsManager();
    }

    /**
     * MythicMobsデスリスナーを取得します
     *
     * @return MythicDeathListenerインスタンス
     */
    public MythicDeathListener getMythicDeathListener() {
        return externalSystem.getMythicDeathListener();
    }

    /**
     * APIを取得します
     *
     * @return RPGPluginAPIインスタンス
     */
    public com.example.rpgplugin.api.RPGPluginAPI getAPI() {
        return externalSystem.getAPI();
    }

    // ====== その他 ======

    /**
     * プラグインをリロードします
     *
     * <p>実行中に設定を再読み込みする際に使用します。</p>
     */
    public void reloadPlugin() {
        getLogger().info("Reloading plugin...");

        try {
            // 設定ファイルのリロード
            YamlConfigManager configManager = coreSystem.getConfigManager();
            int reloaded = configManager.reloadAll();
            getLogger().info("Reloaded " + reloaded + " config files.");

            // スキル設定のリロード
            SkillConfig skillConfig = gameSystem.getSkillConfig();
            int reloadedSkills = skillConfig.reloadSkills();
            getLogger().info("Reloaded " + reloadedSkills + " skills.");

            // モジュールのリロード
            ModuleManager moduleManager = coreSystem.getModuleManager();
            moduleManager.reloadAll();

            // 依存関係の状態をログ出力
            DependencyManager dependencyManager = coreSystem.getDependencyManager();
            dependencyManager.logDependencyStatus();

            getLogger().info("Plugin reloaded successfully!");

        } catch (Exception e) {
            getLogger().severe("Failed to reload plugin:");
            e.printStackTrace();
        }
    }
}
