package com.example.rpgplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RPGCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // コンソールからも実行可能なコマンド
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("rpg.admin")) {
                sender.sendMessage("§c権限がありません");
                return true;
            }

            long startTime = System.currentTimeMillis();

            sender.sendMessage("§e=== RPGPlugin リロード中 ===");
            RPGPlugin.getInstance().reloadPlugin();

            long duration = System.currentTimeMillis() - startTime;
            sender.sendMessage("§aリロード完了! (" + duration + "ms)");
            return true;
        }

        // プレイヤーのみのコマンド
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ使用できます");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§e=== RPG Plugin ===");
            player.sendMessage("§7/rpg stats - ステータスを表示");
            player.sendMessage("§7/rpg help - ヘルプを表示");
            if (player.hasPermission("rpg.admin")) {
                player.sendMessage("§7/rpg reload - 設定をリロード");
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "stats":
                player.sendMessage("§6=== ステータス ===");
                player.sendMessage("§7HP: §c100/100");
                player.sendMessage("§7MP: §b50/50");
                player.sendMessage("§7Level: §a1");
                player.sendMessage("§7EXP: §e0/100");
                break;

            case "help":
                player.sendMessage("§e=== ヘルプ ===");
                player.sendMessage("§7/rpg stats - ステータスを表示");
                player.sendMessage("§7/rpg help - ヘルプを表示");
                break;

            default:
                player.sendMessage("§c不明なコマンドです");
                break;
        }

        return true;
    }
}
