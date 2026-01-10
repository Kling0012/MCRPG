package com.example.rpgplugin;

import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillTree;
import com.example.rpgplugin.gui.SkillTreeGUI;
import com.example.rpgplugin.skill.executor.ActiveSkillExecutor;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
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
                sender.sendMessage(ChatColor.RED + "権限がありません");
                return true;
            }

            long startTime = System.currentTimeMillis();

            sender.sendMessage(ChatColor.YELLOW + "=== RPGPlugin リロード中 ===");
            RPGPlugin.getInstance().reloadPlugin();

            long duration = System.currentTimeMillis() - startTime;
            sender.sendMessage(ChatColor.GREEN + "リロード完了!(" + duration + "ms)");
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
            sender.sendMessage("このコマンドはプレイヤーのみ使用できます");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "=== RPG Plugin ===");
            player.sendMessage(ChatColor.GRAY + "/rpg stats - ステータスを表示");
            player.sendMessage(ChatColor.GRAY + "/rpg skill - スキル情報を表示");
            player.sendMessage(ChatColor.GRAY + "/rpg class list - クラス一覧を表示");
            player.sendMessage(ChatColor.GRAY + "/rpg class change <クラスID> [level] - クラスを変更");
            player.sendMessage(ChatColor.GRAY + "/rpg cast <スキルID> - スキルを発動");
            player.sendMessage(ChatColor.GRAY + "/rpg help - ヘルプを表示");
            if (player.hasPermission("rpg.admin")) {
                player.sendMessage(ChatColor.GRAY + "/rpg class change <player> <classId> [level] - 他プレイヤーのクラスを変更");
                player.sendMessage(ChatColor.GRAY + "/rpg reload - 設定をリロード");
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
                player.sendMessage(ChatColor.RED + "不明なコマンドです");
                player.sendMessage(ChatColor.GRAY + "/rpg help でヘルプを表示");
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
        player.sendMessage(ChatColor.YELLOW + "=== RPGPlugin PlaceholderAPI ===");
        player.sendMessage(ChatColor.GRAY + "ステータスは外部GUIプラグインで表示できます");
        player.sendMessage("");
        player.sendMessage(ChatColor.WHITE + "主なプレースホルダー:");
        player.sendMessage(ChatColor.GRAY + "  %rpg_level% - レベル");
        player.sendMessage(ChatColor.GRAY + "  %rpg_class% - クラス名");
        player.sendMessage(ChatColor.GRAY + "  %rpg_stat_strength% - STR");
        player.sendMessage(ChatColor.GRAY + "  %rpg_stat_intelligence% - INT");
        player.sendMessage(ChatColor.GRAY + "  %rpg_stat_spirit% - SPI");
        player.sendMessage(ChatColor.GRAY + "  %rpg_stat_vitality% - VIT");
        player.sendMessage(ChatColor.GRAY + "  %rpg_stat_dexterity% - DEX");
        player.sendMessage(ChatColor.GRAY + "  %rpg_available_points% - 振り分け可能ポイント");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "/rpg help で全コマンドを表示");
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
    var rpgPlayer = plugin.getPlayerManager().getRPGPlayer(player);
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
            player.sendMessage(ChatColor.RED + "使用法: /rpg cast <スキルID>");
            return;
        }

        String skillId = args[1];
        SkillManager skillManager = RPGPlugin.getInstance().getSkillManager();
        if (skillManager == null) {
            player.sendMessage(ChatColor.RED + "スキルマネージャーが初期化されていません");
            return;
        }

        Skill skill = skillManager.getSkill(skillId);
        if (skill == null) {
            player.sendMessage(ChatColor.RED + "スキルが見つかりません: " + skillId);
            return;
        }

        if (!skill.isActive()) {
            player.sendMessage(ChatColor.RED + "このスキルは手動発動できません: " + skill.getColoredDisplayName());
            return;
        }

        // 習得チェック
        int level = skillManager.getSkillLevel(player, skillId);
        if (level == 0) {
            player.sendMessage(ChatColor.RED + "このスキルを習得していません: " + skill.getColoredDisplayName());
            return;
        }

        // クールダウンチェック
        if (!skillManager.checkCooldown(player, skillId)) {
            player.sendMessage(ChatColor.RED + "クールダウン中です");
            return;
        }

        // スキル発動
        ActiveSkillExecutor executor = RPGPlugin.getInstance().getActiveSkillExecutor();
        if (executor == null) {
            player.sendMessage(ChatColor.RED + "スキル実行システムが初期化されていません");
            return;
        }

        boolean success = executor.execute(player, skill, level);
        if (!success) {
            // 詳細なエラーメッセージは executor.execute() 内で送信される
            player.sendMessage(ChatColor.RED + "スキルの発動に失敗しました: " + skill.getColoredDisplayName());
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
            player.sendMessage(ChatColor.RED + "クラスマネージャーが初期化されていません");
            return;
        }

        // 引数なし: クラス情報を表示
        if (args.length == 1) {
            player.sendMessage(ChatColor.YELLOW + "=== クラスシステム ===");
            player.sendMessage(ChatColor.GRAY + "/rpg class list - 利用可能なクラス一覧");
            player.sendMessage(ChatColor.GRAY + "/rpg class change <クラスID> [level] - クラスを変更");
            return;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "list":
                handleClassListCommand(player, clsManager);
                break;

            case "change":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "使用法: /rpg class change <クラスID> [level]");
                    return;
                }
                String classId = args[2];
                int level = 0;
                if (args.length >= 4) {
                    try {
                        level = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "レベルは数値で指定してください");
                        return;
                    }
                }
                handleClassChangeCommand(player, clsManager, classId, level);
                break;

            default:
                player.sendMessage(ChatColor.RED + "不明なサブコマンド: " + subCommand);
                player.sendMessage(ChatColor.GRAY + "使用法: /rpg class [list|change]");
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
        player.sendMessage(ChatColor.YELLOW + "=== 利用可能なクラス ===");

        // 初期クラス（Rank1）
        for (com.example.rpgplugin.rpgclass.RPGClass rpgClass : clsManager.getInitialClasses()) {
            player.sendMessage(String.format("%s%s§r - %s",
                ChatColor.GOLD,
                rpgClass.getDisplayName(),
                rpgClass.getDescription()
            ));
        }

        player.sendMessage(ChatColor.GRAY + "使用法: /rpg class <クラスID>");
    }

    /**
     * クラス設定コマンドを処理します
     *
     * @param player プレイヤー
     * @param clsManager クラスマネージャー
     * @param classId クラスID
     */
    private void handleClassSetCommand(Player player, com.example.rpgplugin.rpgclass.ClassManager clsManager, String classId) {
        // クラス存在確認
        if (!clsManager.getClass(classId).isPresent()) {
            player.sendMessage(ChatColor.RED + "クラスが見つかりません: " + classId);
            player.sendMessage(ChatColor.GRAY + "使用法: /rpg class list でクラス一覧を確認");
            return;
        }

        // 現在のクラスを確認
        if (clsManager.getPlayerClass(player).isPresent()) {
            player.sendMessage(ChatColor.RED + "既にクラスを選択しています");
            player.sendMessage(ChatColor.GRAY + "クラスの変更は現在実装中です");
            return;
        }

        // クラス設定
        boolean success = clsManager.setPlayerClass(player, classId);
        if (success) {
            com.example.rpgplugin.rpgclass.RPGClass rpgClass = clsManager.getClass(classId).get();
            player.sendMessage(ChatColor.GREEN + "クラスを設定しました: " + rpgClass.getDisplayName());
            // 説明文を送信
            for (String line : rpgClass.getDescription()) {
                player.sendMessage(ChatColor.GRAY + line);
            }
        } else {
            player.sendMessage(ChatColor.RED + "クラスの設定に失敗しました");
        }
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
            player.sendMessage(ChatColor.RED + "このコマンドを実行する権限がありません");
            return;
        }

        // クラス存在確認
        if (!clsManager.getClass(classId).isPresent()) {
            player.sendMessage(ChatColor.RED + "クラスが見つかりません: " + classId);
            player.sendMessage(ChatColor.GRAY + "使用法: /rpg class list でクラス一覧を確認");
            return;
        }

        // クラス変更
        boolean success = clsManager.changeClass(player, classId, level);
        if (success) {
            com.example.rpgplugin.rpgclass.RPGClass rpgClass = clsManager.getClass(classId).get();
            player.sendMessage(ChatColor.GREEN + "クラスを変更しました: " + rpgClass.getDisplayName());
            player.sendMessage(ChatColor.GRAY + "レベル: " + Math.max(0, level));
            // 説明文を送信
            for (String line : rpgClass.getDescription()) {
                player.sendMessage(ChatColor.GRAY + line);
            }
        } else {
            player.sendMessage(ChatColor.RED + "クラスの変更に失敗しました");
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
            sender.sendMessage(ChatColor.RED + "このコマンドを実行する権限がありません");
            return true;
        }

        // 引数チェック: /rpg class change <player> <classId> [level]
        // args[0]="class", args[1]="change", args[2]=<player>, args[3]=<classId>, args[4]=[level]
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "使用法: /rpg class change <player> <classId> [level]");
            return true;
        }

        String targetPlayerName = args[2];
        String classId = args[3];
        int level = 0;

        if (args.length >= 5) {
            try {
                level = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "レベルは数値で指定してください");
                return true;
            }
        }

        // ターゲットプレイヤーを取得
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(ChatColor.RED + "プレイヤーが見つかりません、またはオフラインです: " + targetPlayerName);
            return true;
        }

        com.example.rpgplugin.rpgclass.ClassManager clsManager = RPGPlugin.getInstance().getClassManager();
        if (clsManager == null) {
            sender.sendMessage(ChatColor.RED + "クラスマネージャーが初期化されていません");
            return true;
        }

        // クラス存在確認
        if (!clsManager.getClass(classId).isPresent()) {
            sender.sendMessage(ChatColor.RED + "クラスが見つかりません: " + classId);
            return true;
        }

        // クラス変更
        boolean success = clsManager.changeClass(targetPlayer, classId, level);
        if (success) {
            com.example.rpgplugin.rpgclass.RPGClass rpgClass = clsManager.getClass(classId).get();
            sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " のクラスを変更しました: " + rpgClass.getDisplayName());
            targetPlayer.sendMessage(ChatColor.GREEN + "クラスを変更されました: " + rpgClass.getDisplayName());
            targetPlayer.sendMessage(ChatColor.GRAY + "レベル: " + Math.max(0, level));
        } else {
            sender.sendMessage(ChatColor.RED + "クラスの変更に失敗しました");
        }

        return true;
    }

    /**
     * ヘルプを表示します
     *
     * @param player プレイヤー
     */
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.DARK_GRAY + "========================================");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "           RPGPlugin ヘルプ");
        player.sendMessage(ChatColor.DARK_GRAY + "========================================");
        player.sendMessage("");

        // 基本コマンド
        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "【基本コマンド】");
        player.sendMessage(ChatColor.WHITE + "/rpg" + ChatColor.GRAY + " - メインメニューを表示");
        player.sendMessage(ChatColor.WHITE + "/rpg help" + ChatColor.GRAY + " - このヘルプを表示");
        player.sendMessage(ChatColor.WHITE + "/rpg stats" + ChatColor.GRAY + " - ステータスを表示");
        player.sendMessage(ChatColor.WHITE + "/rpg skill" + ChatColor.GRAY + " - スキル情報を表示");
        player.sendMessage("");

        // クラスコマンド
        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "【クラスコマンド】");
        player.sendMessage(ChatColor.WHITE + "/rpg class" + ChatColor.GRAY + " - クラス情報を表示");
        player.sendMessage(ChatColor.WHITE + "/rpg class list" + ChatColor.GRAY + " - クラス一覧を表示");
        player.sendMessage(ChatColor.WHITE + "/rpg class <クラスID>" + ChatColor.GRAY + " - クラスを選択");
        player.sendMessage("");

        // スキルコマンド
        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "【スキルコマンド】");
        player.sendMessage(ChatColor.WHITE + "/rpg cast <スキルID>" + ChatColor.GRAY + " - スキルを発動");
        player.sendMessage("");

        // 管理者コマンド
        if (player.hasPermission("rpg.admin")) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "【管理者コマンド】");
            player.sendMessage(ChatColor.WHITE + "/rpg reload" + ChatColor.GRAY + " - 設定をリロード");
            player.sendMessage("");
        }

        // ヒント
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "ヒント: Tabキーでコマンド補完が利用できます");
        player.sendMessage(ChatColor.DARK_GRAY + "========================================");
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
        player.sendMessage(ChatColor.DARK_GRAY + "========================================");
        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "           クラスヘルプ");
        player.sendMessage(ChatColor.DARK_GRAY + "========================================");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "/rpg class list" + ChatColor.GRAY + " - 利用可能なクラスを表示");
        player.sendMessage(ChatColor.GOLD + "/rpg class info <クラスID>" + ChatColor.GRAY + " - クラス情報を表示");
        player.sendMessage(ChatColor.GOLD + "/rpg class change <クラスID>" + ChatColor.GRAY + " - クラスを変更");
        player.sendMessage(ChatColor.GOLD + "/rpg class upgrade" + ChatColor.GRAY + " - クラスをアップグレード");
        player.sendMessage(ChatColor.DARK_GRAY + "========================================");
    }

    /**
     * スキルヘルプを表示します
     *
     * @param player プレイヤー
     */
    private void showSkillHelp(Player player) {
        player.sendMessage(ChatColor.DARK_GRAY + "========================================");
        player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "           スキルヘルプ");
        player.sendMessage(ChatColor.DARK_GRAY + "========================================");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "/rpg cast <スキルID>" + ChatColor.GRAY + " - スキルを発動します");
        player.sendMessage("");
        player.sendMessage(ChatColor.WHITE + "発動条件:");
        player.sendMessage(ChatColor.GRAY + "  • スキルを習得している必要があります");
        player.sendMessage(ChatColor.GRAY + "  • クールダウン中ではない必要があります");
        player.sendMessage(ChatColor.GRAY + "  • 十分なMPを持っている必要があります");
        player.sendMessage(ChatColor.DARK_GRAY + "========================================");
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
