package com.example.rpgplugin;

import com.example.rpgplugin.core.config.ConfigWatcher;
import com.example.rpgplugin.core.config.YamlConfigManager;
import com.example.rpgplugin.core.dependency.DependencyManager;
import com.example.rpgplugin.core.module.ModuleManager;
import com.example.rpgplugin.gui.menu.SkillMenuListener;
import com.example.rpgplugin.mythicmobs.MythicMobsManager;
import com.example.rpgplugin.mythicmobs.config.MobDropConfig;
import com.example.rpgplugin.mythicmobs.listener.MythicDeathListener;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.VanillaExpHandler;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.config.SkillConfig;
import com.example.rpgplugin.skill.executor.ActiveSkillExecutor;
import com.example.rpgplugin.skill.executor.PassiveSkillExecutor;
import com.example.rpgplugin.stats.StatManager;
import com.example.rpgplugin.storage.StorageManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

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
    private PlayerManager playerManager;
    private YamlConfigManager configManager;
    private ConfigWatcher configWatcher;
    private StatManager statManager;
    private SkillManager skillManager;
    private com.example.rpgplugin.rpgclass.ClassManager classManager;
    private com.example.rpgplugin.damage.DamageManager damageManager;
    private com.example.rpgplugin.auction.AuctionManager auctionManager;
    private SkillConfig skillConfig;
    private ActiveSkillExecutor activeSkillExecutor;
    private PassiveSkillExecutor passiveSkillExecutor;
    private com.example.rpgplugin.currency.CurrencyManager currencyManager;
    private com.example.rpgplugin.currency.CurrencyListener currencyListener;
    private com.example.rpgplugin.trade.TradeManager tradeManager;
    private MythicMobsManager mythicMobsManager;
    private MythicDeathListener mythicDeathListener;
    private com.example.rpgplugin.api.RPGPluginAPI api;

    // リスナー
    private VanillaExpHandler vanillaExpHandler;
    private SkillMenuListener skillMenuListener;
    private com.example.rpgplugin.gui.menu.rpgclass.ClassMenuListener classMenuListener;
    private com.example.rpgplugin.trade.TradeMenuListener tradeMenuListener;

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

            // プレイヤーマネージャーの初期化
            setupPlayerManager();

            // ステータスシステムの初期化
            initializeStatManager();

            // クラスシステムの初期化
            initializeClassManager();

            // スキルシステムの初期化
            initializeSkillSystem();

            // 通貨システムの初期化
            initializeCurrencySystem();

            // トレードシステムの初期化
            initializeTradeSystem();

            // MythicMobsシステムの初期化
            initializeMythicMobsSystem();

            // モジュールマネージャーの初期化
            setupModuleManager();

            // モジュールの有効化
            enableModules();

            // オークションシステムの初期化
            initializeAuctionSystem();

            // APIの初期化
            initializeAPI();

            // バニラ経験値ハンドラーの登録
            setupVanillaExpHandler();

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

            // プレイヤーマネージャーのシャットダウン
            if (playerManager != null) {
                playerManager.shutdown();
            }

            // トレードマネージャーのシャットダウン
            if (tradeManager != null) {
                tradeManager.shutdown();
            }

            // MythicMobsマネージャーのクリーンアップ
            if (mythicMobsManager != null) {
                mythicMobsManager.cleanup();
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
     * プレイヤーマネージャーをセットアップします
     */
    private void setupPlayerManager() {
        getLogger().info("Initializing PlayerManager...");
        playerManager = new PlayerManager(this, storageManager.getPlayerDataRepository());
        playerManager.initialize();
        getServer().getPluginManager().registerEvents(playerManager, this);
        getLogger().info("PlayerManager initialized!");
    }

    /**
     * ステータスシステムを初期化
     */
    private void initializeStatManager() {
        getLogger().info("Initializing StatManager...");
        statManager = new StatManager();
        getLogger().info("StatManager initialized!");
    }

    /**
     * クラスシステムを初期化
     */
    private void initializeClassManager() {
        getLogger().info("Initializing ClassManager...");
        classManager = new com.example.rpgplugin.rpgclass.ClassManager(playerManager);

        // クラス設定を読み込み
        com.example.rpgplugin.rpgclass.ClassLoader clsLoader = new com.example.rpgplugin.rpgclass.ClassLoader(this, playerManager);
        Map<String, com.example.rpgplugin.rpgclass.RPGClass> classes = clsLoader.loadAllClasses();
        getLogger().info("Loaded " + classes.size() + " classes");

        // クラスメニューリスナーはClassMenuごとに動的に作成されるため、
        // ここではグローバルリスナーを登録しない

        getLogger().info("ClassManager initialized!");
    }

    /**
     * スキルシステムを初期化
     */
    private void initializeSkillSystem() {
        getLogger().info("Initializing SkillSystem...");

        // スキルマネージャー
        skillManager = new SkillManager(this);

        // スキル設定
        skillConfig = new SkillConfig(this, skillManager);
        int skillCount = skillConfig.loadSkills();
        getLogger().info("Loaded " + skillCount + " skills");

        // アクティブスキルエグゼキューター
        activeSkillExecutor = new ActiveSkillExecutor(this, skillManager, playerManager);

        // パッシブスキルエグゼキューター
        passiveSkillExecutor = new PassiveSkillExecutor(this, skillManager, playerManager);

        // スキルメニューリスナー
        skillMenuListener = new SkillMenuListener(this);
        getServer().getPluginManager().registerEvents(skillMenuListener, this);

        getLogger().info("SkillSystem initialized!");
    }

    /**
     * 通貨システムを初期化
     */
    private void initializeCurrencySystem() {
        getLogger().info("Initializing CurrencySystem...");

        // 通貨マネージャー
        currencyManager = new com.example.rpgplugin.currency.CurrencyManager(
                storageManager.getPlayerCurrencyRepository(),
                getLogger()
        );

        // 通貨リスナー
        currencyListener = new com.example.rpgplugin.currency.CurrencyListener(this, currencyManager);
        getServer().getPluginManager().registerEvents(currencyListener, this);

        getLogger().info("CurrencySystem initialized!");
    }

    /**
     * バニラ経験値ハンドラーをセットアップします
     */
    private void setupVanillaExpHandler() {
        getLogger().info("Initializing VanillaExpHandler...");
        vanillaExpHandler = new VanillaExpHandler(this, playerManager);
        getServer().getPluginManager().registerEvents(vanillaExpHandler, this);
        getLogger().info("VanillaExpHandler initialized!");
    }

    /**
     * トレードシステムを初期化
     */
    private void initializeTradeSystem() {
        getLogger().info("Initializing TradeSystem...");

        // トレードマネージャー
        tradeManager = new com.example.rpgplugin.trade.TradeManager(this);

        // トレード履歴リポジトリ
        com.example.rpgplugin.trade.repository.TradeHistoryRepository historyRepository =
            new com.example.rpgplugin.trade.repository.TradeHistoryRepository(
                storageManager.getDatabaseManager(),
                getLogger()
            );

        // トレードマネージャーを初期化
        tradeManager.initialize(historyRepository);

        // トレードメニューリスナー
        tradeMenuListener = new com.example.rpgplugin.trade.TradeMenuListener(tradeManager);
        getServer().getPluginManager().registerEvents(tradeMenuListener, this);

        getLogger().info("TradeSystem initialized!");
    }

    /**
     * ダメージシステムを初期化
     */
    private void initializeDamageManager() {
        getLogger().info("Initializing DamageManager...");
        damageManager = new com.example.rpgplugin.damage.DamageManager(this);
        getServer().getPluginManager().registerEvents(damageManager, this);
        getLogger().info("DamageManager initialized!");
    }

    /**
     * MythicMobsシステムを初期化
     */
    private void initializeMythicMobsSystem() {
        if (!dependencyManager.isMythicMobsAvailable()) {
            getLogger().warning("MythicMobs not available. Skipping MythicMobs system initialization.");
            return;
        }

        getLogger().info("Initializing MythicMobs System...");

        // TODO: MythicMobsManager初期化 - ConnectionPoolとDatabaseManagerの統合が必要
        // 現在、MythicMobsシステムは一時的に無効化されています
        getLogger().warning("MythicMobs system is temporarily disabled pending ConnectionPool integration");

        /*
        // MythicMobsマネージャー
        mythicMobsManager = new MythicMobsManager(
                this,
                dependencyManager.getMythicMobsHook(),
                storageManager.getDatabaseManager()
        );

        // マネージャーを初期化
        if (!mythicMobsManager.initialize()) {
            getLogger().warning("Failed to initialize MythicMobsManager");
            return;
        }

        // ドロップ設定を読み込み
        loadMobDropConfigs();
        */

        /*
        // MythicMobsデスリスナー
        mythicDeathListener = new MythicDeathListener(mythicMobsManager);
        getServer().getPluginManager().registerEvents(mythicDeathListener, this);
        */

        // 期限切れドロップクリーニングタスクを開始
        // startDropCleanupTask();

        getLogger().info("MythicMobs System initialized!");
    }

    /**
     * モブドロップ設定を読み込み
     */
    private void loadMobDropConfigs() {
        getLogger().info("Loading mob drop configurations...");

        // mob_drops.ymlを保存（存在しない場合）
        saveResource("mobs/mob_drops.yml", false);

        // 設定ファイルを読み込み
        FileConfiguration dropConfig = getConfigManager().getConfig("mob_drops");
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
        mythicMobsManager.loadDropConfigs(mobDrops);

        getLogger().info("Loaded " + mobDrops.size() + " mob drop configurations");
    }

    /**
     * ドロップクリーニングタスクを開始
     */
    private void startDropCleanupTask() {
        // 10分ごとに期限切れドロップをクリーニング
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (mythicMobsManager != null) {
                mythicMobsManager.cleanupExpiredDrops();
            }
        }, 10L * 60L * 20L, 10L * 60L * 20L); // 10分 = 12000 ticks

        getLogger().info("Drop cleanup task started (runs every 10 minutes)");
    }

    /**
     * オークションシステムを初期化
     */
    private void initializeAuctionSystem() {
        getLogger().info("Initializing AuctionManager...");
        auctionManager = new com.example.rpgplugin.auction.AuctionManager(
                getLogger(),
                storageManager.getDatabaseManager()
        );

        // アクティブなオークションをロード
        auctionManager.loadActiveAuctions();

        // 入札延長タスクスケジューラーを開始
        startAuctionExpirationTask();

        getLogger().info("AuctionManager initialized!");
    }

    /**
     * 入札延長タスクスケジューラーを開始
     */
    private void startAuctionExpirationTask() {
        // 5秒ごとに期限切れオークションをチェック
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (auctionManager != null) {
                auctionManager.checkExpiredAuctions();
            }
        }, 5L * 20L, 5L * 20L); // 5秒 = 100 ticks (20 ticks/sec)

        getLogger().info("Auction expiration checker started (runs every 5 seconds)");
    }

    /**
     * APIを初期化
     */
    private void initializeAPI() {
        getLogger().info("Initializing API...");
        api = new com.example.rpgplugin.api.RPGPluginAPIImpl(this);
        getLogger().info("API initialized!");
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
     * プレイヤーマネージャーを取得
     *
     * @return プレイヤーマネージャー
     */
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * ステータスマネージャーを取得
     *
     * @return ステータスマネージャー
     */
    public StatManager getStatManager() {
        return statManager;
    }

    /**
     * クラスマネージャーを取得
     *
     * @return クラスマネージャー
     */
    public com.example.rpgplugin.rpgclass.ClassManager getClassManager() {
        return classManager;
    }

    /**
     * スキルマネージャーを取得
     *
     * @return スキルマネージャー
     */
    public SkillManager getSkillManager() {
        return skillManager;
    }

    /**
     * オークションマネージャーを取得
     *
     * @return オークションマネージャー
     */
    public com.example.rpgplugin.auction.AuctionManager getAuctionManager() {
        return auctionManager;
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
     * ダメージマネージャーを取得します
     *
     * @return DamageManagerインスタンス
     */
    public com.example.rpgplugin.damage.DamageManager getDamageManager() {
        return damageManager;
    }

    /**
     * スキルメニューリスナーを取得します
     *
     * @return SkillMenuListenerインスタンス
     */
    public SkillMenuListener getSkillMenuListener() {
        return skillMenuListener;
    }

    /**
     * クラスメニューリスナーを取得します
     *
     * @return ClassMenuListenerインスタンス
     */
    public com.example.rpgplugin.gui.menu.rpgclass.ClassMenuListener getClassMenuListener() {
        return classMenuListener;
    }

    /**
     * スキル設定を取得します
     *
     * @return SkillConfigインスタンス
     */
    public SkillConfig getSkillConfig() {
        return skillConfig;
    }

    /**
     * アクティブスキルエグゼキューターを取得します
     *
     * @return ActiveSkillExecutorインスタンス
     */
    public ActiveSkillExecutor getActiveSkillExecutor() {
        return activeSkillExecutor;
    }

    /**
     * パッシブスキルエグゼキューターを取得します
     *
     * @return PassiveSkillExecutorインスタンス
     */
    public PassiveSkillExecutor getPassiveSkillExecutor() {
        return passiveSkillExecutor;
    }

    /**
     * 通貨マネージャーを取得します
     *
     * @return CurrencyManagerインスタンス
     */
    public com.example.rpgplugin.currency.CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    /**
     * 通貨リスナーを取得します
     *
     * @return CurrencyListenerインスタンス
     */
    public com.example.rpgplugin.currency.CurrencyListener getCurrencyListener() {
        return currencyListener;
    }

    /**
     * トレードマネージャーを取得します
     *
     * @return TradeManagerインスタンス
     */
    public com.example.rpgplugin.trade.TradeManager getTradeManager() {
        return tradeManager;
    }

    /**
     * MythicMobsマネージャーを取得します
     *
     * @return MythicMobsManagerインスタンス
     */
    public MythicMobsManager getMythicMobsManager() {
        return mythicMobsManager;
    }

    /**
     * MythicMobsデスリスナーを取得します
     *
     * @return MythicDeathListenerインスタンス
     */
    public MythicDeathListener getMythicDeathListener() {
        return mythicDeathListener;
    }

    /**
     * APIを取得します
     *
     * @return RPGPluginAPIインスタンス
     */
    public com.example.rpgplugin.api.RPGPluginAPI getAPI() {
        return api;
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

            // スキル設定のリロード
            if (skillConfig != null) {
                int reloadedSkills = skillConfig.reloadSkills();
                getLogger().info("Reloaded " + reloadedSkills + " skills.");
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
