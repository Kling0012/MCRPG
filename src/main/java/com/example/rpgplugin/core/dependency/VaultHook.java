package com.example.rpgplugin.core.dependency;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Vault連携クラス
 *
 * <p>Vaultプラグインとの連携を提供します。
 * 経済システム、チャット、パーミッション機能へのアクセスを提供します。</p>
 *
 * <p>機能:</p>
 * <ul>
 *   <li>経済システム: 通貨の管理、取引処理</li>
 *   <li>チャット: チャットフォーマット、プレフィックス/サフィックス</li>
 *   <li>パーミッション: 権限の確認・管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class VaultHook {

    private final JavaPlugin plugin;

    private Economy economy;
    private Chat chat;
    private Permission permission;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public VaultHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Vaultのセットアップを行います
     *
     * <p>経済、チャット、パーミッションサービスの初期化を試みます。
     * 少なくとも1つのサービスが利用可能な場合にtrueを返します。</p>
     *
     * @return セットアップに成功した場合はtrue
     */
    public boolean setup() {
        // Vaultプラグインの存在確認
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault is not enabled!");
            return false;
        }

        boolean success = false;

        // 経済システムのセットアップ
        if (setupEconomy()) {
            plugin.getLogger().info("Vault Economy hooked successfully.");
            success = true;
        } else {
            plugin.getLogger().warning("Vault Economy is not available.");
        }

        // チャットのセットアップ
        if (setupChat()) {
            plugin.getLogger().info("Vault Chat hooked successfully.");
            success = true;
        } else {
            plugin.getLogger().info("Vault Chat is not available.");
        }

        // パーミッションのセットアップ
        if (setupPermission()) {
            plugin.getLogger().info("Vault Permission hooked successfully.");
            success = true;
        } else {
            plugin.getLogger().info("Vault Permission is not available.");
        }

        return success;
    }

    /**
     * 経済システムをセットアップします
     *
     * @return 成功した場合はtrue
     */
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager()
                .getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null && economy.isEnabled();
    }

    /**
     * チャットをセットアップします
     *
     * @return 成功した場合はtrue
     */
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = Bukkit.getServicesManager()
                .getRegistration(Chat.class);

        if (rsp == null) {
            return false;
        }

        chat = rsp.getProvider();
        return chat != null;
    }

    /**
     * パーミッションをセットアップします
     *
     * @return 成功した場合はtrue
     */
    private boolean setupPermission() {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager()
                .getRegistration(Permission.class);

        if (rsp == null) {
            return false;
        }

        permission = rsp.getProvider();
        return permission != null;
    }

    /**
     * 経済システムを取得します
     *
     * @return Economyインスタンス、利用不可能な場合はnull
     */
    public Economy getEconomy() {
        return economy;
    }

    /**
     * チャットを取得します
     *
     * @return Chatインスタンス、利用不可能な場合はnull
     */
    public Chat getChat() {
        return chat;
    }

    /**
     * パーミッションを取得します
     *
     * @return Permissionインスタンス、利用不可能な場合はnull
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * 経済システムが利用可能か確認します
     *
     * @return 利用可能な場合はtrue
     */
    public boolean hasEconomy() {
        return economy != null && economy.isEnabled();
    }

    /**
     * チャットが利用可能か確認します
     *
     * @return 利用可能な場合はtrue
     */
    public boolean hasChat() {
        return chat != null;
    }

    /**
     * パーミッションが利用可能か確認します
     *
     * @return 利用可能な場合はtrue
     */
    public boolean hasPermission() {
        return permission != null;
    }

    /**
     * 便利メソッド: プレイヤーの残高を取得します
     *
     * @param playerName プレイヤー名
     * @return 残高、経済システムが利用不可能な場合は0
     */
    public double getBalance(String playerName) {
        if (!hasEconomy()) {
            return 0;
        }
        return economy.getBalance(playerName);
    }

    /**
     * 便利メソッド: プレイヤーの残高を設定します
     *
     * @param playerName プレイヤー名
     * @param amount 金額
     * @return 成功した場合はtrue
     */
    public boolean setBalance(String playerName, double amount) {
        if (!hasEconomy()) {
            return false;
        }

        double current = economy.getBalance(playerName);
        if (amount > current) {
            return economy.depositPlayer(playerName, amount - current)
                    .transactionSuccess();
        } else if (amount < current) {
            return economy.withdrawPlayer(playerName, current - amount)
                    .transactionSuccess();
        }

        return true;
    }

    /**
     * 便利メソッド: プレイヤーに通貨を追加します
     *
     * @param playerName プレイヤー名
     * @param amount 金額
     * @return 成功した場合はtrue
     */
    public boolean deposit(String playerName, double amount) {
        if (!hasEconomy()) {
            return false;
        }
        return economy.depositPlayer(playerName, amount).transactionSuccess();
    }

    /**
     * 便利メソッド: プレイヤーから通貨を引き出します
     *
     * @param playerName プレイヤー名
     * @param amount 金額
     * @return 成功した場合はtrue
     */
    public boolean withdraw(String playerName, double amount) {
        if (!hasEconomy()) {
            return false;
        }
        return economy.withdrawPlayer(playerName, amount).transactionSuccess();
    }

    /**
     * 便利メソッド: プレイヤーが指定金額を持っているか確認します
     *
     * @param playerName プレイヤー名
     * @param amount 金額
     * @return 持っている場合はtrue
     */
    public boolean has(String playerName, double amount) {
        if (!hasEconomy()) {
            return false;
        }
        return economy.has(playerName, amount);
    }

    /**
     * フックをクリーンアップします
     *
     * <p>プラグイン無効化時に呼び出します。</p>
     */
    public void cleanup() {
        economy = null;
        chat = null;
        permission = null;
    }
}
