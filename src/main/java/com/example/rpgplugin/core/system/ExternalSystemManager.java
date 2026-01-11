package com.example.rpgplugin.core.system;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.RPGPluginAPI;
import com.example.rpgplugin.api.RPGPluginAPIImpl;
import com.example.rpgplugin.core.dependency.MythicMobsHook;
import com.example.rpgplugin.mythicmobs.MythicMobsManager;
import com.example.rpgplugin.mythicmobs.listener.MythicDeathListener;
import com.example.rpgplugin.storage.database.ConnectionPool;

/**
 * 外部システムの統合マネージャー（ファサード）
 *
 * <p>外部プラグイン連携とAPI公開を統合管理します。</p>
 *
 * <p>主な機能:</p>
 * <ul>
 *   <li>MythicMobs連携（MythicMobsManager, MythicDeathListener）</li>
 *   <li>外部API公開（RPGPluginAPI）</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>ファサードパターン: 外部連携への統一インターフェース</li>
 *   <li>単一責任: 外部プラグイン連携とAPI管理のみ担当</li>
 *   <li>グレースフルデグラデーション: 依存プラグインがなくても動作</li>
 * </ul>
 *
 * <p>初期化順序:</p>
 * <ol>
 *   <li>MythicMobsManager（MythicMobsプラグインとの連携）</li>
 *   <li>MythicDeathListener（MythicMobsの Deathイベント監視）</li>
 *   <li>RPGPluginAPI（外部プラグイン向けAPI）</li>
 * </ol>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExternalSystemManager {

    private final RPGPlugin plugin;

    // MythicMobs連携
    private final MythicMobsManager mythicMobsManager;
    private final MythicDeathListener mythicDeathListener;

    // 外部API
    private final RPGPluginAPI api;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     * @param connectionPool データベースコネクションプール
     */
    public ExternalSystemManager(RPGPlugin plugin, ConnectionPool connectionPool) {
        this.plugin = plugin;

        // MythicMobs連携の初期化
        MythicMobsHook mythicMobsHook = new MythicMobsHook(plugin);
        this.mythicMobsManager = new MythicMobsManager(
            plugin,
            mythicMobsHook,
            connectionPool
        );
        this.mythicDeathListener = new MythicDeathListener(mythicMobsManager);

        // 外部APIの初期化
        this.api = new RPGPluginAPIImpl(plugin);
    }

    /**
     * 外部システムを初期化する
     *
     * @throws Exception 初期化に失敗した場合
     */
    public void initialize() throws Exception {
        plugin.getLogger().info("========================================");
        plugin.getLogger().info(" ExternalSystemManager: 初期化を開始します");
        plugin.getLogger().info("========================================");

        // MythicMobsManagerの初期化
        plugin.getLogger().info("[ExternalSystem] MythicMobs連携を初期化中...");
        boolean mythicMobsEnabled = mythicMobsManager.initialize();

        if (mythicMobsEnabled) {
            plugin.getLogger().info("[ExternalSystem] MythicMobs連携が有効です");

            // MythicDeathListenerの登録
            plugin.getLogger().info("[ExternalSystem] MythicDeathListenerを登録中...");
            plugin.getServer().getPluginManager().registerEvents(
                mythicDeathListener, plugin
            );
        } else {
            plugin.getLogger().info("[ExternalSystem] MythicMobs連携は無効です（プラグインが見つかりません）");
        }

        // 外部APIの初期化
        plugin.getLogger().info("[ExternalSystem] 外部APIを初期化中...");
        // RPGPluginAPIImpl は initialize() メソッドを持たない

        plugin.getLogger().info("========================================");
        plugin.getLogger().info(" ExternalSystemManager: 初期化が完了しました");
        plugin.getLogger().info("========================================");
    }

    /**
     * 外部システムをシャットダウンする
     */
    public void shutdown() {
        plugin.getLogger().info("[ExternalSystem] シャットダウンを開始します");

        mythicMobsManager.cleanup();

        plugin.getLogger().info("[ExternalSystem] シャットダウンが完了しました");
    }

    // ==================== Getter メソッド（後方互換性） ====================

    /**
     * MythicMobsManagerを取得します
     *
     * @return MythicMobsManager
     */
    public MythicMobsManager getMythicMobsManager() {
        return mythicMobsManager;
    }

    /**
     * MythicDeathListenerを取得します
     *
     * @return MythicDeathListener
     */
    public MythicDeathListener getMythicDeathListener() {
        return mythicDeathListener;
    }

    /**
     * RPGPluginAPIを取得します
     *
     * @return RPGPluginAPI
     */
    public RPGPluginAPI getAPI() {
        return api;
    }
}
