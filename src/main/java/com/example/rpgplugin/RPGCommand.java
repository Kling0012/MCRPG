package com.example.rpgplugin;

import com.example.rpgplugin.gui.menu.StatMenu;
import com.example.rpgplugin.stats.StatManager;
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
                // StatMenuを開く
                StatManager statManager = RPGPlugin.getInstance().getStatManager();
                if (statManager != null) {
                    StatMenu menu = new StatMenu(player, statManager);
                    menu.open();
                } else {
                    player.sendMessage("§cステータスシステムが有効ではありません");
                }
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
