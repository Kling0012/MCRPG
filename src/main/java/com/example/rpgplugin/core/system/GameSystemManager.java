package com.example.rpgplugin.core.system;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.stats.StatManager;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.rpgclass.ClassManager;
import com.example.rpgplugin.damage.DamageManager;
import com.example.rpgplugin.currency.CurrencyManager;
import com.example.rpgplugin.auction.AuctionManager;
import com.example.rpgplugin.trade.TradeManager;
import com.example.rpgplugin.player.exp.ExpManager;
import com.example.rpgplugin.core.system.CoreSystemManager;

/**
 * ゲームシステムの統合マネージャー（ファサード）
 *
 * 責務:
 * - プレイヤーデータ管理（PlayerManager）
 * - ステータスシステム（StatManager）
 * - スキルシステム（SkillManager）
 * - クラスシステム（ClassManager）
 * - ダメージシステム（DamageManager）
 * - 経済システム（CurrencyManager, AuctionManager, TradeManager）
 * - 経験値システム（ExpManager）
 *
 * Single Responsibility: ゲームプレイ関連機能の統合管理
 *
 * 初期化順序:
 * 1. PlayerManager（他のシステムがプレイヤーデータに依存）
 * 2. StatManager（ステータス管理）
 * 3. ClassManager（PlayerManagerに依存）
 * 4. SkillManager（スキル管理）
 * 5. DamageManager（ダメージ計算）
 * 6. ExpManager（経験値システム）
 * 7. CurrencyManager（通貨システム）
 * 8. TradeManager（トレードシステム）
 * 9. AuctionManager（オークションシステム）
 */
public class GameSystemManager {
    private final RPGPlugin plugin;
    private final CoreSystemManager coreSystem;

    // プレイヤーシステム
    private final PlayerManager playerManager;

    // ステータスシステム
    private final StatManager statManager;

    // クラスシステム
    private final ClassManager classManager;

    // スキルシステム
    private final SkillManager skillManager;
    private final com.example.rpgplugin.skill.config.SkillConfig skillConfig;
    private final com.example.rpgplugin.skill.executor.ActiveSkillExecutor activeSkillExecutor;
    private final com.example.rpgplugin.skill.executor.PassiveSkillExecutor passiveSkillExecutor;

    // ダメージシステム
    private final DamageManager damageManager;

    // 経験値システム
    private final ExpManager expManager;

    // 経済システム
    private final CurrencyManager currencyManager;
    private final TradeManager tradeManager;
    private final AuctionManager auctionManager;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     * @param coreSystem コアシステムマネージャー
     */
    public GameSystemManager(RPGPlugin plugin, CoreSystemManager coreSystem) {
        this.plugin = plugin;
        this.coreSystem = coreSystem;

        // 依存関係を考慮してインスタンス化
        // 1. PlayerManager（最初に初期化）
        this.playerManager = new PlayerManager(plugin, coreSystem.getStorageManager().getPlayerDataRepository());
        this.statManager = new StatManager();
        this.classManager = new ClassManager(playerManager);

        // 2. ExpManager（PlayerManagerとClassManagerに依存）
        this.expManager = new ExpManager(plugin, playerManager, classManager);

        // 3. その他のマネージャー
        this.skillManager = new SkillManager(plugin, playerManager);
        this.skillConfig = new com.example.rpgplugin.skill.config.SkillConfig(plugin, skillManager);
        this.activeSkillExecutor = new com.example.rpgplugin.skill.executor.ActiveSkillExecutor(plugin, skillManager, playerManager);
        this.passiveSkillExecutor = new com.example.rpgplugin.skill.executor.PassiveSkillExecutor(plugin, skillManager, playerManager);
        this.damageManager = new DamageManager(plugin);
        this.currencyManager = new CurrencyManager(
            coreSystem.getStorageManager().getPlayerCurrencyRepository(),
            plugin.getLogger()
        );
        this.tradeManager = new TradeManager(plugin);
        this.auctionManager = new AuctionManager(
            plugin.getLogger(),
            coreSystem.getStorageManager().getDatabaseManager()
        );
    }

    /**
     * ゲームシステムを初期化する
     *
     * @throws Exception 初期化に失敗した場合
     */
    public void initialize() throws Exception {
        plugin.getLogger().info("========================================");
        plugin.getLogger().info(" GameSystemManager: 初期化を開始します");
        plugin.getLogger().info("========================================");

        // 1. プレイヤーマネージャーの初期化
        plugin.getLogger().info("[GameSystem] プレイヤーマネージャーを初期化中...");
        playerManager.initialize();

        // 2. ステータスマネージャーの初期化
        plugin.getLogger().info("[GameSystem] ステータスマネージャーを初期化中...");
        // statManager は initialize() メソッドを持たない

        // 3. クラスマネージャーの初期化
        plugin.getLogger().info("[GameSystem] クラスマネージャーを初期化中...");
        // classManager は initialize() メソッドを持たない

        // 4. スキルマネージャーの初期化
        plugin.getLogger().info("[GameSystem] スキルマネージャーを初期化中...");
        // skillManager は initialize() メソッドを持たない

        // 5. ダメージマネージャーの初期化
        plugin.getLogger().info("[GameSystem] ダメージマネージャーを初期化中...");
        damageManager.initialize();

        // 6. 経験値システムの初期化
        plugin.getLogger().info("[GameSystem] 経験値システムを初期化中...");
        expManager.initialize();

        // 7. 通貨マネージャーの初期化
        plugin.getLogger().info("[GameSystem] 通貨マネージャーを初期化中...");
        // currencyManager は initialize() メソッドを持たない

        // 8. トレードマネージャーの初期化
        plugin.getLogger().info("[GameSystem] トレードマネージャーを初期化中...");
        // TradeHistoryRepositoryは直接インスタンス化
        com.example.rpgplugin.trade.repository.TradeHistoryRepository historyRepository =
            new com.example.rpgplugin.trade.repository.TradeHistoryRepository(
                coreSystem.getStorageManager().getDatabaseManager(),
                plugin.getLogger()
            );
        tradeManager.initialize(historyRepository);

        // 9. オークションマネージャーの初期化
        plugin.getLogger().info("[GameSystem] オークションマネージャーを初期化中...");
        auctionManager.loadActiveAuctions();

        plugin.getLogger().info("========================================");
        plugin.getLogger().info(" GameSystemManager: 初期化が完了しました");
        plugin.getLogger().info("========================================");
    }

    /**
     * ゲームシステムをシャットダウンする
     */
    public void shutdown() {
        plugin.getLogger().info("[GameSystem] シャットダウンを開始します");

        if (expManager != null) {
            expManager.shutdown();
        }

        // AuctionManagerにshutdownメソッドがないためスキップ
        // 必要に応じてAuctionManagerにshutdownメソッドを追加

        if (tradeManager != null) {
            // TradeManagerにshutdownメソッドがあるか確認
            // tradeManager.shutdown();
        }

        plugin.getLogger().info("[GameSystem] シャットダウンが完了しました");
    }

    // Getter メソッド（後方互換性）

    /**
     * プレイヤーマネージャーを取得する
     *
     * @return PlayerManager プレイヤーマネージャー
     */
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * ステータスマネージャーを取得する
     *
     * @return StatManager ステータスマネージャー
     */
    public StatManager getStatManager() {
        return statManager;
    }

    /**
     * クラスマネージャーを取得する
     *
     * @return ClassManager クラスマネージャー
     */
    public ClassManager getClassManager() {
        return classManager;
    }

    /**
     * スキルマネージャーを取得する
     *
     * @return SkillManager スキルマネージャー
     */
    public SkillManager getSkillManager() {
        return skillManager;
    }

    /**
     * スキル設定を取得する
     *
     * @return SkillConfig スキル設定
     */
    public com.example.rpgplugin.skill.config.SkillConfig getSkillConfig() {
        return skillConfig;
    }

    /**
     * アクティブスキルエグゼキューターを取得する
     *
     * @return ActiveSkillExecutor アクティブスキルエグゼキューター
     */
    public com.example.rpgplugin.skill.executor.ActiveSkillExecutor getActiveSkillExecutor() {
        return activeSkillExecutor;
    }

    /**
     * パッシブスキルエグゼキューターを取得する
     *
     * @return PassiveSkillExecutor パッシブスキルエグゼキューター
     */
    public com.example.rpgplugin.skill.executor.PassiveSkillExecutor getPassiveSkillExecutor() {
        return passiveSkillExecutor;
    }

    /**
     * ダメージマネージャーを取得する
     *
     * @return DamageManager ダメージマネージャー
     */
    public DamageManager getDamageManager() {
        return damageManager;
    }

    /**
     * 経験値マネージャーを取得する
     *
     * @return ExpManager 経験値マネージャー
     */
    public ExpManager getExpManager() {
        return expManager;
    }

    /**
     * 通貨マネージャーを取得する
     *
     * @return CurrencyManager 通貨マネージャー
     */
    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    /**
     * トレードマネージャーを取得する
     *
     * @return TradeManager トレードマネージャー
     */
    public TradeManager getTradeManager() {
        return tradeManager;
    }

    /**
     * オークションマネージャーを取得する
     *
     * @return AuctionManager オークションマネージャー
     */
    public AuctionManager getAuctionManager() {
        return auctionManager;
    }
}
