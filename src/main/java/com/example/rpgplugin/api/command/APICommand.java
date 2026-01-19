package com.example.rpgplugin.api.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
 * @version 1.0.2
 */
public class APICommand implements CommandExecutor, TabCompleter {

    /**
     * コンストラクタ
     */
    public APICommand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 権限チェック - コンソールまたは権限を持つプレイヤーのみ
        if (sender instanceof Player player) {
            if (!player.hasPermission("rpgplugin.api")) {
                sender.sendMessage(Component.text("このコマンドは一般ユーザーは使用できません", NamedTextColor.RED));
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
        sender.sendMessage(Component.text("===== RPGPlugin API (Skript Reflect) =====", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Skript Reflectを使用して直接Javaメソッドを呼び出します。", NamedTextColor.YELLOW));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("基本構文:", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("  set {_plugin} = RPGPlugin.getInstance()", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("  set {_pm} = {_plugin}.getPlayerManager()", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("  set {_rpgPlayer} = {_pm}.getRPGPlayer(player's uuid)", NamedTextColor.WHITE));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("ドキュメント:", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("  docs/SKRIPT_REFLECT.md", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("===========================================", NamedTextColor.GOLD));
    }
}
