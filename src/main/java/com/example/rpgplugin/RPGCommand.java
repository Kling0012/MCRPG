package com.example.rpgplugin;

import com.example.rpgplugin.command.Messages;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.gui.SkillTreeGUI;
import com.example.rpgplugin.skill.executor.ActiveSkillExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RPGコマンドハンドラー
 *
 * <p>/rpg コマンドの処理を行います。</p>
 *
 * <p>サブコマンド:</p>
 * <ul>
 *   <li>stats - ステータスを表示</li>
 *   <li>skill - スキル情報を表示</li>
 *   <li>cast - スキルを発動</li>
 *   <li>class - クラス管理</li>
 *   <li>help - ヘルプを表示</li>
 *   <li>reload - 設定をリロード（管理者のみ）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class RPGCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // コンソールからも実行可能なコマンド
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("rpg.admin")) {
                sender.sendMessage(Messages.NO_PERMISSION);
                return true;
            }

            long startTime = System.currentTimeMillis();

            sender.sendMessage(Messages.Reload.TITLE);
            RPGPlugin.getInstance().reloadPlugin();

            long duration = System.currentTimeMillis() - startTime;
            sender.sendMessage(Component.text(String.format(Messages.Reload.SUCCESS_FORMAT, duration), NamedTextColor.GREEN));
            return true;
        }

        // 管理者コマンド: /rpg class change <player> <classId> [level]
        if (args.length >= 5 && args[0].equalsIgnoreCase("class")
                && args[1].equalsIgnoreCase("change")
                && sender.hasPermission("rpg.admin.class.change")) {
            return handleAdminClassChangeCommand(sender, args);
        }

        // プレイヤーのみのコマンド
        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.CONSOLE_ONLY);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(Messages.GUI.TITLE);
            player.sendMessage(Component.text(Messages.GUI.STATS, NamedTextColor.GRAY));
            player.sendMessage(Component.text(Messages.GUI.SKILL, NamedTextColor.GRAY));
            player.sendMessage(Component.text(Messages.GUI.CLASS_LIST, NamedTextColor.GRAY));
            player.sendMessage(Component.text(Messages.GUI.CLASS_CHANGE, NamedTextColor.GRAY));
            player.sendMessage(Component.text(Messages.GUI.CAST_SKILL, NamedTextColor.GRAY));
            player.sendMessage(Component.text(Messages.GUI.HELP_CMD, NamedTextColor.GRAY));
            if (player.hasPermission("rpg.admin")) {
                player.sendMessage(Component.text(Messages.GUI.ADMIN_CLASS_CHANGE, NamedTextColor.GRAY));
                player.sendMessage(Component.text(Messages.GUI.ADMIN_RELOAD, NamedTextColor.GRAY));
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "stats":
                handleStatsCommand(player);
                break;

            case "skill":
            case "skills":
                handleSkillCommand(player);
                break;

            case "cast":
                handleCastCommand(player, args);
                break;

            case "class":
                handleClassCommand(player, args);
                break;

            case "help":
                if (args.length >= 2) {
                    showCategoryHelp(player, args[1]);
                } else {
                    showHelp(player);
                }
                break;

            default:
                player.sendMessage(Messages.UNKNOWN_COMMAND);
                player.sendMessage(Messages.UNKNOWN_COMMAND_HINT);
                break;
        }

        return true;
    }

    /**
     * ステータスコマンドを処理します
     *
     * <p>外部GUIプラグイン用のプレースホルダー案内を表示します。</p>
     *
     * @param player プレイヤー
     */
    private void handleStatsCommand(Player player) {
        // 外部GUIプラグイン用のプレースホルダー案内
        player.sendMessage(Messages.Stats.TITLE);
        player.sendMessage(Messages.Stats.INFO);
        player.sendMessage(Component.empty());
        player.sendMessage(Messages.Stats.MAIN_PLACEHOLDERS);
        player.sendMessage(Component.text(Messages.Stats.PLACEHOLDER_LEVEL, NamedTextColor.GRAY));
        player.sendMessage(Component.text(Messages.Stats.PLACEHOLDER_CLASS, NamedTextColor.GRAY));
        player.sendMessage(Component.text(Messages.Stats.PLACEHOLDER_STR, NamedTextColor.GRAY));
        player.sendMessage(Component.text(Messages.Stats.PLACEHOLDER_INT, NamedTextColor.GRAY));
        player.sendMessage(Component.text(Messages.Stats.PLACEHOLDER_SPI, NamedTextColor.GRAY));
        player.sendMessage(Component.text(Messages.Stats.PLACEHOLDER_VIT, NamedTextColor.GRAY));
        player.sendMessage(Component.text(Messages.Stats.PLACEHOLDER_DEX, NamedTextColor.GRAY));
        player.sendMessage(Component.text(Messages.Stats.PLACEHOLDER_POINTS, NamedTextColor.GRAY));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text(Messages.Help.HELP, NamedTextColor.GRAY));
    }

    /**
 * スキルコマンドを処理します
 *
 * <p>スキルツリーGUIを開きます。</p>
 *
 * @param player プレイヤー
 */
private void handleSkillCommand(Player player) {
    RPGPlugin plugin = RPGPlugin.getInstance();
    
    // プレイヤーのクラスを取得
    String classId = null;
    var rpgPlayer = plugin.getPlayerManager().getRPGPlayer(player.getUniqueId());
    if (rpgPlayer != null) {
        classId = rpgPlayer.getClassId();
    }
    
    // GUIを開く
    SkillTreeGUI gui = new SkillTreeGUI(plugin, player, classId);
    gui.open();
}

    /**
     * スキル発動コマンドを処理します
     *
     * @param player プレイヤー
     * @param args 引数
     */
    private void handleCastCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Messages.Skill.CAST_USAGE);
            return;
        }

        String skillId = args[1];
        SkillManager skillManager = RPGPlugin.getInstance().getSkillManager();
        if (skillManager == null) {
            player.sendMessage(Messages.Cast.MANAGER_NOT_INITIALIZED);
            return;
        }

        Skill skill = skillManager.getSkill(skillId);
        if (skill == null) {
            player.sendMessage(Messages.Cast.SKILL_NOT_FOUND.append(Component.text(skillId)));
            return;
        }

        // パッシブ/アクティブの区別を廃止したため、チェックを削除

        // 習得チェック
        int level = skillManager.getSkillLevel(player, skillId);
        if (level == 0) {
            player.sendMessage(Messages.Cast.NOT_ACQUIRED.append(Component.text(skill.getColoredDisplayName())));
            return;
        }

        // クールダウンチェック
        if (!skillManager.checkCooldown(player, skillId)) {
            player.sendMessage(Messages.Cast.COOLDOWN);
            return;
        }

        // スキル発動
        ActiveSkillExecutor executor = RPGPlugin.getInstance().getActiveSkillExecutor();
        if (executor == null) {
            player.sendMessage(Messages.Cast.EXECUTOR_NOT_INITIALIZED);
            return;
        }

        boolean success = executor.execute(player, skill, level);
        if (!success) {
            // 詳細なエラーメッセージは executor.execute() 内で送信される
            player.sendMessage(Messages.Cast.CAST_FAILED.append(Component.text(skill.getDisplayName())));
        }
    }

    /**
     * クラスコマンドを処理します
     *
     * <p>引数なし: クラス情報を表示</p>
     * <p>list: クラス一覧を表示</p>
     * <p>change <クラスID> [level]: クラスを変更</p>
     *
     * @param player プレイヤー
     * @param args 引数
     */
    private void handleClassCommand(Player player, String[] args) {
        com.example.rpgplugin.rpgclass.ClassManager clsManager = RPGPlugin.getInstance().getClassManager();
        if (clsManager == null) {
            player.sendMessage(Component.text("クラスマネージャーが初期化されていません", NamedTextColor.RED));
            return;
        }

        // 引数なし: クラス情報を表示
        if (args.length == 1) {
            player.sendMessage(Component.text(Messages.ClassCmd.TITLE, NamedTextColor.YELLOW));
            player.sendMessage(Component.text(Messages.ClassCmd.LIST, NamedTextColor.GRAY));
            player.sendMessage(Component.text(Messages.ClassCmd.CHANGE_USAGE_SHORT, NamedTextColor.GRAY));
            return;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "list":
                handleClassListCommand(player, clsManager);
                break;

            case "change":
                if (args.length < 3) {
                    player.sendMessage(Messages.ClassCmd.CHANGE_USAGE);
                    return;
                }
                String classId = args[2];
                int level = 0;
                if (args.length >= 4) {
                    try {
                        level = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(Messages.Cast.NUMBER_FORMAT_ERROR);
                        return;
                    }
                }
                handleClassChangeCommand(player, clsManager, classId, level);
                break;

            default:
                player.sendMessage(Component.text("不明なサブコマンド: " + subCommand, NamedTextColor.RED));
                player.sendMessage(Component.text(Messages.ClassCmd.USAGE, NamedTextColor.GRAY));
                break;
        }
    }

    /**
     * クラス一覧コマンドを処理します
     *
     * @param player プレイヤー
     * @param clsManager クラスマネージャー
     */
    private void handleClassListCommand(Player player, com.example.rpgplugin.rpgclass.ClassManager clsManager) {
        player.sendMessage(Component.text("=== 利用可能なクラス ===", NamedTextColor.YELLOW));

        // 初期クラス（Rank1）
        for (com.example.rpgplugin.rpgclass.RPGClass rpgClass : clsManager.getInitialClasses()) {
            player.sendMessage(Component.text(rpgClass.getDisplayName() + " - " + rpgClass.getDescription(), NamedTextColor.GOLD));
        }

        player.sendMessage(Component.text("使用法: /rpg class <クラスID>", NamedTextColor.GRAY));
    }

    /**
     * クラス変更コマンドを処理します
     *
     * <p>条件チェックを行わず、即座にクラスを変更します。</p>
     *
     * @param player    プレイヤー
     * @param clsManager クラスマネージャー
     * @param classId   クラスID
     * @param level     設定するレベル
     */
    private void handleClassChangeCommand(Player player, com.example.rpgplugin.rpgclass.ClassManager clsManager, String classId, int level) {
        // 権限チェック
        if (!player.hasPermission("rpg.admin.class.change")) {
            player.sendMessage(Messages.NO_PERMISSION);
            return;
        }

        // クラス存在確認
        if (!clsManager.getClass(classId).isPresent()) {
            player.sendMessage(Messages.ClassCmd.NOT_FOUND.append(Component.text(classId)));
            player.sendMessage(Messages.ClassCmd.CHECK_LIST);
            return;
        }

        // クラス変更
        boolean success = clsManager.changeClass(player, classId, level);
        if (success) {
            com.example.rpgplugin.rpgclass.RPGClass rpgClass = clsManager.getClass(classId).get();
            player.sendMessage(Messages.ClassCmd.CHANGE_SUCCESS.append(Component.text(rpgClass.getDisplayName())));
            player.sendMessage(Component.text(Messages.ClassCmd.LEVEL_FORMAT + Math.max(0, level), NamedTextColor.GRAY));
            // 説明文を送信
            for (String line : rpgClass.getDescription()) {
                player.sendMessage(Component.text(line, NamedTextColor.GRAY));
            }
        } else {
            player.sendMessage(Messages.ClassCmd.CHANGE_FAILED);
        }
    }


    /**
     * 管理者用クラス変更コマンドを処理します
     *
     * <p>/rpg class change <player> <classId> [level]</p>
     *
     * @param sender コマンド送信者
     * @param args 引数
     * @return 処理成功の場合はtrue
     */
    private boolean handleAdminClassChangeCommand(CommandSender sender, String[] args) {
        // 権限チェック
        if (!sender.hasPermission("rpg.admin.class.change")) {
            sender.sendMessage(Messages.NO_PERMISSION);
            return true;
        }

        // 引数チェック: /rpg class change <player> <classId> [level]
        // args[0]="class", args[1]="change", args[2]=<player>, args[3]=<classId>, args[4]=[level]
        if (args.length < 4) {
            sender.sendMessage(Messages.ClassCmd.CHANGE_ADMIN_USAGE);
            return true;
        }

        String targetPlayerName = args[2];
        String classId = args[3];
        int level = 0;

        if (args.length >= 5) {
            try {
                level = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Messages.Cast.NUMBER_FORMAT_ERROR);
                return true;
            }
        }

        // ターゲットプレイヤーを取得
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(Messages.Reload.PLAYER_NOT_FOUND.append(Component.text(targetPlayerName)));
            return true;
        }

        com.example.rpgplugin.rpgclass.ClassManager clsManager = RPGPlugin.getInstance().getClassManager();
        if (clsManager == null) {
            sender.sendMessage(Component.text("クラスマネージャーが初期化されていません", NamedTextColor.RED));
            return true;
        }

        // クラス存在確認
        if (!clsManager.getClass(classId).isPresent()) {
            sender.sendMessage(Messages.ClassCmd.NOT_FOUND.append(Component.text(classId)));
            return true;
        }

        // クラス変更
        boolean success = clsManager.changeClass(targetPlayer, classId, level);
        if (success) {
            com.example.rpgplugin.rpgclass.RPGClass rpgClass = clsManager.getClass(classId).get();
            sender.sendMessage(Component.text(targetPlayer.getName()).append(Messages.ClassCmd.ADMIN_CHANGE_SUCCESS).append(Component.text(rpgClass.getDisplayName())));
            targetPlayer.sendMessage(Messages.ClassCmd.ADMIN_CHANGE_NOTIFICATION.append(Component.text(rpgClass.getDisplayName())));
            targetPlayer.sendMessage(Component.text(Messages.ClassCmd.LEVEL_FORMAT + Math.max(0, level), NamedTextColor.GRAY));
        } else {
            sender.sendMessage(Messages.ClassCmd.CHANGE_FAILED);
        }

        return true;
    }

    /**
     * ヘルプを表示します
     *
     * @param player プレイヤー
     */
    private void showHelp(Player player) {
        player.sendMessage(Messages.Help.BORDER);
        player.sendMessage(Messages.Help.TITLE);
        player.sendMessage(Messages.Help.BORDER);
        player.sendMessage(Component.empty());

        // 基本コマンド
        player.sendMessage(Messages.Help.BASIC_COMMANDS_TITLE);
        player.sendMessage(Component.text(Messages.Help.MAIN_MENU, NamedTextColor.GRAY));
        player.sendMessage(Component.text(Messages.Help.HELP, NamedTextColor.GRAY));
        player.sendMessage(Component.text(Messages.Help.STATS, NamedTextColor.GRAY));
        player.sendMessage(Component.text(Messages.Help.SKILL, NamedTextColor.GRAY));
        player.sendMessage(Component.empty());

        // クラスコマンド
        player.sendMessage(Messages.Help.CLASS_COMMANDS_TITLE);
        player.sendMessage(Component.text(Messages.Help.CLASS_INFO, NamedTextColor.GRAY));
        player.sendMessage(Component.text(Messages.Help.CLASS_LIST, NamedTextColor.GRAY));
        player.sendMessage(Component.text(Messages.Help.CLASS_SELECT, NamedTextColor.GRAY));
        player.sendMessage(Component.empty());

        // スキルコマンド
        player.sendMessage(Messages.Help.SKILL_COMMANDS_TITLE);
        player.sendMessage(Component.text(Messages.Help.CAST, NamedTextColor.GRAY));
        player.sendMessage(Component.empty());

        // 管理者コマンド
        if (player.hasPermission("rpg.admin")) {
            player.sendMessage(Messages.Help.ADMIN_COMMANDS_TITLE);
            player.sendMessage(Component.text(Messages.Help.RELOAD, NamedTextColor.GRAY));
            player.sendMessage(Component.empty());
        }

        // ヒント
        player.sendMessage(Messages.Help.HINT);
        player.sendMessage(Messages.Help.BORDER);
    }

    /**
     * カテゴリ別ヘルプを表示します
     *
     * @param player プレイヤー
     * @param category カテゴリ
     */
    @SuppressWarnings("unused")
    private void showCategoryHelp(Player player, String category) {
        switch (category.toLowerCase()) {
            case "class":
            case "クラス":
                showClassHelp(player);
                break;
            case "skill":
            case "スキル":
                showSkillHelp(player);
                break;
            default:
                showHelp(player);
                break;
        }
    }

    /**
     * クラスヘルプを表示します
     *
     * @param player プレイヤー
     */
    private void showClassHelp(Player player) {
        player.sendMessage(Messages.Help.BORDER);
        player.sendMessage(Component.text("           クラスヘルプ", NamedTextColor.AQUA, TextDecoration.BOLD));
        player.sendMessage(Messages.Help.BORDER);
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text(Messages.ClassCmd.INFO_USAGE, NamedTextColor.GRAY));
        player.sendMessage(Component.text(Messages.ClassCmd.CHANGE_USAGE_SHORT, NamedTextColor.GRAY));
        player.sendMessage(Component.text(Messages.ClassCmd.UPGRADE, NamedTextColor.GRAY));
        player.sendMessage(Messages.Help.BORDER);
    }

    /**
     * スキルヘルプを表示します
     *
     * @param player プレイヤー
     */
    private void showSkillHelp(Player player) {
        player.sendMessage(Messages.Skill.BORDER);
        player.sendMessage(Messages.Skill.TITLE);
        player.sendMessage(Messages.Skill.BORDER);
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text(Messages.Skill.CAST_DESC, NamedTextColor.GRAY));
        player.sendMessage(Component.empty());
        player.sendMessage(Messages.Skill.CONDITION_TITLE);
        player.sendMessage(Messages.Skill.CONDITION_ACQUIRED);
        player.sendMessage(Messages.Skill.CONDITION_COOLDOWN);
        player.sendMessage(Messages.Skill.CONDITION_MANA);
        player.sendMessage(Messages.Skill.BORDER);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 第一引数: サブコマンド
            completions.addAll(getMainSubCommands(sender));
        } else if (args.length >= 2) {
            // 第二引数以降: サブコマンド固有の補完
            String subCommand = args[0].toLowerCase();
            completions.addAll(getSubCommandCompletions(sender, subCommand, args));
        }

        // 入力された文字列でフィルタリング
        String lastArg = args[args.length - 1];
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(lastArg.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * メインサブコマンドリストを取得します
     *
     * @param sender コマンド送信者
     * @return サブコマンドリスト
     */
    private List<String> getMainSubCommands(CommandSender sender) {
        List<String> commands = new ArrayList<>();
        commands.add("stats");
        commands.add("skill");
        commands.add("skills");
        commands.add("cast");
        commands.add("class");
        commands.add("help");
        if (sender.hasPermission("rpg.admin")) {
            commands.add("reload");
        }
        return commands;
    }

    /**
     * サブコマンド固有の補完候補を取得します
     *
     * @param sender コマンド送信者
     * @param subCommand サブコマンド
     * @param args 引数配列
     * @return 補完候補リスト
     */
    private List<String> getSubCommandCompletions(CommandSender sender, String subCommand, String[] args) {
        switch (subCommand) {
            case "cast":
                return getCastCompletions(args);

            case "class":
                return getClassCompletions(args);

            case "help":
                return getHelpCompletions(args);

            default:
                return Collections.emptyList();
        }
    }

    /**
     * castコマンドの補完候補を取得します
     *
     * @param args 引数配列
     * @return 補完候補リスト
     */
    private List<String> getCastCompletions(String[] args) {
        if (args.length == 2) {
            // スキルID補完
            SkillManager skillManager = RPGPlugin.getInstance().getSkillManager();
            if (skillManager != null) {
                return new ArrayList<>(skillManager.getAllSkillIds());
            }
        }
        return Collections.emptyList();
    }

    /**
     * classコマンドの補完候補を取得します
     *
     * @param args 引数配列
     * @return 補完候補リスト
     */
    private List<String> getClassCompletions(String[] args) {
        if (args.length == 2) {
            return Arrays.asList("list");
        } else if (args.length == 3) {
            // クラスID補完
            com.example.rpgplugin.rpgclass.ClassManager classManager = RPGPlugin.getInstance().getClassManager();
            if (classManager != null) {
                return new ArrayList<>(classManager.getAllClassIds());
            }
        }
        return Collections.emptyList();
    }

    /**
     * helpコマンドの補完候補を取得します
     *
     * @param args 引数配列
     * @return 補完候補リスト
     */
    private List<String> getHelpCompletions(String[] args) {
        if (args.length == 2) {
            // カテゴリ補完
            return Arrays.asList("class", "skill");
        }
        return Collections.emptyList();
    }
}
