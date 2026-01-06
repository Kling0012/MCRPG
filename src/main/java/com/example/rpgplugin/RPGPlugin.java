package com.example.rpgplugin;

import com.example.rpgplugin.storage.StorageManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RPGPlugin extends JavaPlugin {

    private static RPGPlugin instance;
    private StorageManager storageManager;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("RPGPlugin has been enabled!");

        try {
            // ストレージシステムの初期化
            initializeStorage();

            // コマンドハンドラーの登録
            getCommand("rpg").setExecutor(new RPGCommand());

            // リスナーの登録
            getServer().getPluginManager().registerEvents(new RPGListener(), this);

            getLogger().info("RPGPlugin enabled successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to enable RPGPlugin: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("RPGPlugin has been disabled!");

        // ストレージシステムのシャットダウン
        if (storageManager != null) {
            storageManager.shutdown();
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
     * ストレージマネージャーを取得
     *
     * @return ストレージマネージャー
     */
    public StorageManager getStorageManager() {
        return storageManager;
    }

    public static RPGPlugin getInstance() {
        return instance;
    }
}
