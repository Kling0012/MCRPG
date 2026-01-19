package com.example.rpgplugin.player.exp;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.ExpDiminisher;
import com.example.rpgplugin.player.VanillaExpHandler;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.rpgclass.ClassManager;

/**
 * 経験値システムの統合マネージャー
 *
 * 責務:
 * - ExpDimisherとVanillaExpHandlerの統合管理
 * - 経験値システム全体の初期化とライフサイクル管理
 *
 * Single Responsibility: 経験値システム全体の管理
 */
public class ExpManager {
    private final RPGPlugin plugin;
    private final ExpDiminisher expDiminisher;
    private final VanillaExpHandler vanillaExpHandler;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     * @param playerManager プレイヤーマネージャー
     * @param classManager クラスマネージャー
     */
    public ExpManager(RPGPlugin plugin, PlayerManager playerManager, ClassManager classManager) {
        this.plugin = plugin;
        this.expDiminisher = new ExpDiminisher(plugin, playerManager, classManager);
        this.vanillaExpHandler = new VanillaExpHandler(plugin, playerManager, expDiminisher);
    }

    /**
     * 経験値システムを初期化する
     */
    public void initialize() {
        plugin.getLogger().info("[ExpManager] 経験値システムを初期化中...");
        plugin.getLogger().info("[ExpManager] 経験値システムの初期化が完了しました");
    }

    /**
     * 経験値ハンドラーをBukkitイベントシステムに登録します
     */
    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(vanillaExpHandler, plugin);
        plugin.getLogger().info("[ExpManager] VanillaExpHandlerを登録しました");
    }

    /**
     * 経験値システムをシャットダウンする
     */
    public void shutdown() {
        plugin.getLogger().info("[ExpManager] 経験値システムをシャットダウン中...");
        plugin.getLogger().info("[ExpManager] 経験値システムのシャットダウンが完了しました");
    }

    // Getter メソッド

    public ExpDiminisher getExpDiminisher() {
        return expDiminisher;
    }

    public VanillaExpHandler getVanillaExpHandler() {
        return vanillaExpHandler;
    }
}
