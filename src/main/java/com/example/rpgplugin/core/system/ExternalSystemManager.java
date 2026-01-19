package com.example.rpgplugin.core.system;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.RPGPluginAPI;
import com.example.rpgplugin.api.RPGPluginAPIImpl;

/**
 * 外部システムの統合マネージャー（ファサード）
 *
 * <p>外部プラグイン連携とAPI公開を統合管理します。</p>
 *
 * <p>主な機能:</p>
 * <ul>
 *   <li>外部API公開（RPGPluginAPI）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExternalSystemManager {

    private final RPGPlugin plugin;

    // 外部API
    private final RPGPluginAPI api;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public ExternalSystemManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.api = new RPGPluginAPIImpl(plugin);
    }

    /**
     * 外部システムを初期化する
     */
    public void initialize() {
        plugin.getLogger().info("========================================");
        plugin.getLogger().info(" ExternalSystemManager: 初期化を開始します");
        plugin.getLogger().info("========================================");

        plugin.getLogger().info("[ExternalSystem] 外部APIを初期化中...");
        plugin.getLogger().info("[ExternalSystem] RPGPluginAPI initialized.");

        plugin.getLogger().info("========================================");
        plugin.getLogger().info(" ExternalSystemManager: 初期化が完了しました");
        plugin.getLogger().info("========================================");
    }

    /**
     * 外部システムをシャットダウンする
     */
    public void shutdown() {
        plugin.getLogger().info("[ExternalSystem] シャットダウンを開始します");
        plugin.getLogger().info("[ExternalSystem] シャットダウンが完了しました");
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
