package com.example.rpgplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class RPGPlugin extends JavaPlugin {

    private static RPGPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("RPGPlugin has been enabled!");

        // コマンドハンドラーの登録
        getCommand("rpg").setExecutor(new RPGCommand());

        // リスナーの登録
        getServer().getPluginManager().registerEvents(new RPGListener(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("RPGPlugin has been disabled!");
    }

    public static RPGPlugin getInstance() {
        return instance;
    }
}
