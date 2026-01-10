package com.example.rpgplugin.api.command;

import com.example.rpgplugin.RPGPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * APIコマンドクラス
 *
 * <p>Skript Reflectを使用した連携のためのヘルプ情報を提供します。</p>
 *
 * <p>コマンド: {@code /rpgapi help}</p>
 *
 * <p>Skript Reflect 2.6+対応</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.1
 */
public class APICommand implements CommandExecutor, TabCompleter {

    private final RPGPlugin plugin;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public APICommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 権限チェック - コンソールまたは権限を持つプレイヤーのみ
        if (sender instanceof Player player) {
            if (!player.hasPermission("rpgplugin.api")) {
                player.sendMessage(ChatColor.RED + "このコマンドは一般ユーザーは使用できません");
                return true;
            }
        }

        // 引数チェック
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        sendHelp(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("help");
        }

        return completions;
    }

    /**
     * ヘルプメッセージを送信します
     *
     * @param sender 送信先
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "===== RPGPlugin API (Skript Reflect) =====");
        sender.sendMessage(ChatColor.YELLOW + "Skript Reflectを使用して直接Javaメソッドを呼び出します。");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "基本構文:");
        sender.sendMessage(ChatColor.WHITE + "  set {_plugin} = RPGPlugin.getInstance()");
        sender.sendMessage(ChatColor.WHITE + "  set {_pm} = {_plugin}.getPlayerManager()");
        sender.sendMessage(ChatColor.WHITE + "  set {_rpgPlayer} = {_pm}.getRPGPlayer(player's uuid)");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "ドキュメント:");
        sender.sendMessage(ChatColor.WHITE + "  docs/SKRIPT_REFLECT.md");
        sender.sendMessage(ChatColor.GOLD + "===========================================");
    }
}
