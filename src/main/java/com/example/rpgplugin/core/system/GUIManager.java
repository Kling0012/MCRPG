package com.example.rpgplugin.core.system;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.gui.menu.SkillMenuListener;
import com.example.rpgplugin.gui.menu.rpgclass.ClassMenuListener;
import com.example.rpgplugin.trade.TradeMenuListener;
import com.example.rpgplugin.currency.CurrencyListener;
import com.example.rpgplugin.gui.menu.rpgclass.ClassMenu;
import com.example.rpgplugin.rpgclass.ClassManager;
import com.example.rpgplugin.rpgclass.ClassUpgrader;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.trade.TradeManager;
import com.example.rpgplugin.currency.CurrencyManager;

/**
 * GUIシステムの統合マネージャー（ファサード）
 *
 * <p>責務:</p>
 * <ul>
 * <li>スキルメニュー（SkillMenuListener）の管理</li>
 * <li>クラスメニュー（ClassMenuListener）の管理</li>
 * <li>トレードメニュー（TradeMenuListener）の管理</li>
 * <li>通貨GUI（CurrencyListener）の管理</li>
 * </ul>
 *
 * <p>Single Responsibility: GUI関連リスナーの統合管理</p>
 *
 * <p>初期化順序:</p>
 * <ol>
 * <li>各リスナーのインスタンス化</li>
 * <li>Bukkitイベントシステムに登録</li>
 * </ol>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class GUIManager {
    private final RPGPlugin plugin;
    private final GameSystemManager gameSystem;

    // GUI リスナー
    private final SkillMenuListener skillMenuListener;
    private final ClassMenuListener classMenuListener;
    private final TradeMenuListener tradeMenuListener;
    private final CurrencyListener currencyListener;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     * @param gameSystem ゲームシステムマネージャー
     */
    public GUIManager(RPGPlugin plugin, GameSystemManager gameSystem) {
        this.plugin = plugin;
        this.gameSystem = gameSystem;

        // 必要なコンポーネントをGameSystemManagerから取得
        ClassManager classManager = gameSystem.getClassManager();
        PlayerManager playerManager = gameSystem.getPlayerManager();
        TradeManager tradeManager = gameSystem.getTradeManager();
        CurrencyManager currencyManager = gameSystem.getCurrencyManager();

        // クラスメニュー関連のコンポーネントを作成
        ClassUpgrader classUpgrader = createClassUpgrader(classManager, playerManager);
        ClassMenu classMenu = createClassMenu(classManager, classUpgrader);

        // リスナーのインスタンス化
        this.skillMenuListener = new SkillMenuListener(plugin);
        this.classMenuListener = new ClassMenuListener(classMenu, classManager, classUpgrader);
        this.tradeMenuListener = new TradeMenuListener(tradeManager);
        this.currencyListener = new CurrencyListener(plugin, currencyManager);
    }

    /**
     * ClassUpgraderインスタンスを作成する
     *
     * @param classManager クラスマネージャー
     * @param playerManager プレイヤーマネージャー
     * @return ClassUpgraderインスタンス
     */
    private ClassUpgrader createClassUpgrader(ClassManager classManager, PlayerManager playerManager) {
        return new ClassUpgrader(classManager, playerManager);
    }

    /**
     * ClassMenuインスタンスを作成する
     *
     * @param classManager クラスマネージャー
     * @param classUpgrader クラスアップグレーダー
     * @return ClassMenuインスタンス
     */
    private ClassMenu createClassMenu(ClassManager classManager, ClassUpgrader classUpgrader) {
        return new ClassMenu(plugin, classManager, classUpgrader);
    }

    /**
     * GUIシステムを初期化する
     *
     * <p>Bukkitイベントシステムに全リスナーを登録します。</p>
     */
    public void initialize() {
        plugin.getLogger().info("========================================");
        plugin.getLogger().info(" GUIManager: 初期化を開始します");
        plugin.getLogger().info("========================================");

        // スキルメニューリスナーの登録
        plugin.getLogger().info("[GUISystem] スキルメニューリスナーを登録中...");
        plugin.getServer().getPluginManager().registerEvents(
            skillMenuListener, plugin
        );

        // クラスメニューリスナーの登録
        plugin.getLogger().info("[GUISystem] クラスメニューリスナーを登録中...");
        plugin.getServer().getPluginManager().registerEvents(
            classMenuListener, plugin
        );

        // トレードメニューリスナーの登録
        plugin.getLogger().info("[GUISystem] トレードメニューリスナーを登録中...");
        plugin.getServer().getPluginManager().registerEvents(
            tradeMenuListener, plugin
        );

        // 通貨リスナーの登録
        plugin.getLogger().info("[GUISystem] 通貨リスナーを登録中...");
        plugin.getServer().getPluginManager().registerEvents(
            currencyListener, plugin
        );

        plugin.getLogger().info("========================================");
        plugin.getLogger().info(" GUIManager: 初期化が完了しました");
        plugin.getLogger().info(" 登録したリスナー数: 4");
        plugin.getLogger().info("========================================");
    }

    /**
     * GUIシステムをシャットダウンする
     *
     * <p>Bukkitイベントシステムからのリスナー登録解除はプラグイン無効化時に
     * 自動的に行われるため、このメソッドではログ出力のみ行います。</p>
     */
    public void shutdown() {
        plugin.getLogger().info("[GUISystem] シャットダウンを開始します");
        plugin.getLogger().info("[GUISystem] リスナーの登録解除は自動的に行われます");
        plugin.getLogger().info("[GUISystem] シャットダウンが完了しました");
    }

    // Getter メソッド（後方互換性）

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
    public ClassMenuListener getClassMenuListener() {
        return classMenuListener;
    }

    /**
     * トレードメニューリスナーを取得します
     *
     * @return TradeMenuListenerインスタンス
     */
    public TradeMenuListener getTradeMenuListener() {
        return tradeMenuListener;
    }

    /**
     * 通貨リスナーを取得します
     *
     * @return CurrencyListenerインスタンス
     */
    public CurrencyListener getCurrencyListener() {
        return currencyListener;
    }
}
