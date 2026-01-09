package com.example.rpgplugin.api.command;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.bridge.SKriptBridge;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * APIコマンドクラス
 *
 * <p>SKript/Denizenから使用するためのAPIコマンドを提供します。</p>
 *
 * <p>コマンド: {@code /rpg api <action> <args...>}</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: APIコマンド処理に専念</li>
 *   <li>DRY: SKriptBridgeに委譲</li>
 *   <li>KISS: シンプルなコマンド構造</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class APICommand implements CommandExecutor, TabCompleter {

    private final RPGPlugin plugin;
    private final SKriptBridge skriptBridge;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public APICommand(RPGPlugin plugin) {
        this.plugin = plugin;
        this.skriptBridge = new SKriptBridge(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 権限チェック - コンソールまたは権限を持つプレイヤーのみ
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("rpgplugin.api")) {
                player.sendMessage(ChatColor.RED + "このコマンドは一般ユーザーは使用できません");
                player.sendMessage(ChatColor.GRAY + "このAPIはSkript/Denizenからのみ使用されます");
                return true;
            }
        }
        // コンソールからの実行は常に許可（Skript/Denizenが "execute as_server" で実行）

        // 引数チェック
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        // サブコマンド処理
        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (subCommand) {
            case "help":
                sendHelp(sender);
                return true;

            case "call":
                // SKript/Denizenからのコールを処理
                if (subArgs.length == 0) {
                    sender.sendMessage(ChatColor.RED + "使用法: /rpg api call <action> <args...>");
                    return false;
                }
                return skriptBridge.handleCall(sender, subArgs);

            default:
                // "call"を省略した形式もサポート
                return skriptBridge.handleCall(sender, args);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 第一引数: サブコマンドまたはアクション
            completions.add("help");
            completions.add("call");
            completions.addAll(getActionList());
        } else if (args.length >= 2) {
            // 第二引数以降: アクション固有の補完
            String firstArg = args[0].toLowerCase();
            if (firstArg.equals("call") && args.length >= 3) {
                // "call"の場合は次の引数がアクション
                String action = args[1].toLowerCase();
                completions.addAll(getActionSpecificCompletions(action, args));
            } else if (!firstArg.equals("help")) {
                // "call"を省略した場合
                completions.addAll(getActionSpecificCompletions(firstArg, args));
            }
        }

        // 入力された文字列でフィルタリング
        String lastArg = args[args.length - 1];
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(lastArg.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * アクションリストを取得します
     *
     * @return アクションリスト
     */
    private List<String> getActionList() {
        return Arrays.asList(
                "get_level", "set_level",
                "get_stat", "set_stat", "get_available_points", "add_stat_point",
                "get_class", "set_class", "try_change_class", "can_change_class", "upgrade_class", "can_upgrade_class",
                "has_skill", "unlock_skill", "unlock_skill_with_points", "cast_skill", "get_skill_level",
                "get_skill_points", "add_skill_points",
                "get_gold", "give_gold", "take_gold", "has_gold", "transfer_gold",
                "calculate_damage"
        );
    }

    /**
     * アクション固有の補完候補を取得します
     *
     * @param action アクション名
     * @param args 引数配列
     * @return 補完候補リスト
     */
    private List<String> completions = new ArrayList<>();

    private List<String> getActionSpecificCompletions(String action, String[] args) {
        completions.clear();

        switch (action) {
            case "get_stat":
            case "set_stat":
            case "add_stat_point":
                if (args.length == 2 || (args.length == 3 && args[0].equals("call"))) {
                    // ステータス補完
                    completions.addAll(Arrays.asList("STR", "INT", "SPI", "VIT", "DEX"));
                }
                break;

            case "try_change_class":
            case "can_change_class":
                if (args.length == 2 || (args.length == 3 && args[0].equals("call"))) {
                    // クラスID補完
                    if (plugin.getClassManager() != null) {
                        completions.addAll(plugin.getClassManager().getAllClassIds());
                    }
                }
                break;

            case "has_skill":
            case "unlock_skill":
            case "unlock_skill_with_points":
            case "cast_skill":
            case "get_skill_level":
                // スキルIDはプラグインから取得可能だが、今回は空リスト
                break;

            default:
                // プレイヤー名補完（デフォルト）
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    completions.add(player.getName());
                }
                break;
        }

        return completions;
    }

    /**
     * ヘルプメッセージを送信します
     *
     * @param sender 送信先
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "===== RPGPlugin API Command =====");
        sender.sendMessage(ChatColor.YELLOW + "使用法: /rpg api <subcommand> <args...>");
        sender.sendMessage(ChatColor.YELLOW + "サブコマンド:");
        sender.sendMessage(ChatColor.WHITE + "  help - ヘルプを表示");
        sender.sendMessage(ChatColor.WHITE + "  call <action> <args...> - アクションを実行");
        sender.sendMessage(ChatColor.WHITE + "  <action> <args...> - アクションを直接実行（call省略）");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "アクション一覧:");
        sender.sendMessage(ChatColor.WHITE + "  レベル: get_level, set_level");
        sender.sendMessage(ChatColor.WHITE + "  ステータス: get_stat, set_stat, get_available_points, add_stat_point");
        sender.sendMessage(ChatColor.WHITE + "  クラス: get_class, set_class, try_change_class, can_change_class, upgrade_class, can_upgrade_class");
        sender.sendMessage(ChatColor.WHITE + "  スキル: has_skill, unlock_skill, unlock_skill_with_points, cast_skill, get_skill_level");
        sender.sendMessage(ChatColor.WHITE + "  スキル管理: get_skill_points, add_skill_points");
        sender.sendMessage(ChatColor.WHITE + "  経済: get_gold, give_gold, take_gold, has_gold, transfer_gold");
        sender.sendMessage(ChatColor.WHITE + "  ダメージ: calculate_damage");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "使用例:");
        sender.sendMessage(ChatColor.WHITE + "  /rpg api get_level Steve");
        sender.sendMessage(ChatColor.WHITE + "  /rpg api give_gold Steve 100");
        sender.sendMessage(ChatColor.WHITE + "  /rpg api cast_skill Steve fireball");
        sender.sendMessage(ChatColor.GOLD + "================================");
    }
}
