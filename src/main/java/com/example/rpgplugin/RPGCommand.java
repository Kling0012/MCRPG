package com.example.rpgplugin;

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
                sender.sendMessage(Component.text("権限がありません", NamedTextColor.RED));
                return true;
            }

            long startTime = System.currentTimeMillis();

            sender.sendMessage(Component.text("=== RPGPlugin リロード中 ===", NamedTextColor.YELLOW));
            RPGPlugin.getInstance().reloadPlugin();

            long duration = System.currentTimeMillis() - startTime;
            sender.sendMessage(Component.text("リロード完了!(" + duration + "ms)", NamedTextColor.GREEN));
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
            player.sendMessage(Component.text("=== RPG Plugin ===", NamedTextColor.YELLOW));
            player.sendMessage(Component.text("/rpg stats - ステータスを表示", NamedTextColor.GRAY));
            player.sendMessage(Component.text("/rpg skill - スキル情報を表示", NamedTextColor.GRAY));
            player.sendMessage(Component.text("/rpg class list - クラス一覧を表示", NamedTextColor.GRAY));
            player.sendMessage(Component.text("/rpg class change <クラスID> [level] - クラスを変更", NamedTextColor.GRAY));
            player.sendMessage(Component.text("/rpg cast <スキルID> - スキルを発動", NamedTextColor.GRAY));
            player.sendMessage(Component.text("/rpg help - ヘルプを表示", NamedTextColor.GRAY));
            if (player.hasPermission("rpg.admin")) {
                player.sendMessage(Component.text("/rpg class change <player> <classId> [level] - 他プレイヤーのクラスを変更", NamedTextColor.GRAY));
                player.sendMessage(Component.text("/rpg reload - 設定をリロード", NamedTextColor.GRAY));
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
                player.sendMessage(Component.text("不明なコマンドです", NamedTextColor.RED));
                player.sendMessage(Component.text("/rpg help でヘルプを表示", NamedTextColor.GRAY));
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
        player.sendMessage(Component.text("=== RPGPlugin PlaceholderAPI ===", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("ステータスは外部GUIプラグインで表示できます", NamedTextColor.GRAY));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("主なプレースホルダー:", NamedTextColor.WHITE));
        player.sendMessage(Component.text("  %rpg_level% - レベル", NamedTextColor.GRAY));
        player.sendMessage(Component.text("  %rpg_class% - クラス名", NamedTextColor.GRAY));
        player.sendMessage(Component.text("  %rpg_stat_strength% - STR", NamedTextColor.GRAY));
        player.sendMessage(Component.text("  %rpg_stat_intelligence% - INT", NamedTextColor.GRAY));
        player.sendMessage(Component.text("  %rpg_stat_spirit% - SPI", NamedTextColor.GRAY));
        player.sendMessage(Component.text("  %rpg_stat_vitality% - VIT", NamedTextColor.GRAY));
        player.sendMessage(Component.text("  %rpg_stat_dexterity% - DEX", NamedTextColor.GRAY));
        player.sendMessage(Component.text("  %rpg_available_points% - 振り分け可能ポイント", NamedTextColor.GRAY));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("/rpg help で全コマンドを表示", NamedTextColor.GRAY));
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
            player.sendMessage(Component.text("使用法: /rpg cast <スキルID>", NamedTextColor.RED));
            return;
        }

        String skillId = args[1];
        SkillManager skillManager = RPGPlugin.getInstance().getSkillManager();
        if (skillManager == null) {
            player.sendMessage(Component.text("スキルマネージャーが初期化されていません", NamedTextColor.RED));
            return;
        }

        Skill skill = skillManager.getSkill(skillId);
        if (skill == null) {
            player.sendMessage(Component.text("スキルが見つかりません: " + skillId, NamedTextColor.RED));
            return;
        }

        // パッシブ/アクティブの区別を廃止したため、チェックを削除

        // 習得チェック
        int level = skillManager.getSkillLevel(player, skillId);
        if (level == 0) {
            player.sendMessage(Component.text("このスキルを習得していません: " + skill.getColoredDisplayName(), NamedTextColor.RED));
            return;
        }

        // クールダウンチェック
        if (!skillManager.checkCooldown(player, skillId)) {
            player.sendMessage(Component.text("クールダウン中です", NamedTextColor.RED));
            return;
        }

        // スキル発動
        ActiveSkillExecutor executor = RPGPlugin.getInstance().getActiveSkillExecutor();
        if (executor == null) {
            player.sendMessage(Component.text("スキル実行システムが初期化されていません", NamedTextColor.RED));
            return;
        }

        boolean success = executor.execute(player, skill, level);
        if (!success) {
            // 詳細なエラーメッセージは executor.execute() 内で送信される
            player.sendMessage(Component.text("スキルの発動に失敗しました: " + skill.getColoredDisplayName(), NamedTextColor.RED));
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
            player.sendMessage(Component.text("=== クラスシステム ===", NamedTextColor.YELLOW));
            player.sendMessage(Component.text("/rpg class list - 利用可能なクラス一覧", NamedTextColor.GRAY));
            player.sendMessage(Component.text("/rpg class change <クラスID> [level] - クラスを変更", NamedTextColor.GRAY));
            return;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "list":
                handleClassListCommand(player, clsManager);
                break;

            case "change":
                if (args.length < 3) {
                    player.sendMessage(Component.text("使用法: /rpg class change <クラスID> [level]", NamedTextColor.RED));
                    return;
                }
                String classId = args[2];
                int level = 0;
                if (args.length >= 4) {
                    try {
                        level = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(Component.text("レベルは数値で指定してください", NamedTextColor.RED));
                        return;
                    }
                }
                handleClassChangeCommand(player, clsManager, classId, level);
                break;

            default:
                player.sendMessage(Component.text("不明なサブコマンド: " + subCommand, NamedTextColor.RED));
                player.sendMessage(Component.text("使用法: /rpg class [list|change]", NamedTextColor.GRAY));
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
            player.sendMessage(Component.text("このコマンドを実行する権限がありません", NamedTextColor.RED));
            return;
        }

        // クラス存在確認
        if (!clsManager.getClass(classId).isPresent()) {
            player.sendMessage(Component.text("クラスが見つかりません: " + classId, NamedTextColor.RED));
            player.sendMessage(Component.text("使用法: /rpg class list でクラス一覧を確認", NamedTextColor.GRAY));
            return;
        }

        // クラス変更
        boolean success = clsManager.changeClass(player, classId, level);
        if (success) {
            com.example.rpgplugin.rpgclass.RPGClass rpgClass = clsManager.getClass(classId).get();
            player.sendMessage(Component.text("クラスを変更しました: " + rpgClass.getDisplayName(), NamedTextColor.GREEN));
            player.sendMessage(Component.text("レベル: " + Math.max(0, level), NamedTextColor.GRAY));
            // 説明文を送信
            for (String line : rpgClass.getDescription()) {
                player.sendMessage(Component.text(line, NamedTextColor.GRAY));
            }
        } else {
            player.sendMessage(Component.text("クラスの変更に失敗しました", NamedTextColor.RED));
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
            sender.sendMessage(Component.text("このコマンドを実行する権限がありません", NamedTextColor.RED));
            return true;
        }

        // 引数チェック: /rpg class change <player> <classId> [level]
        // args[0]="class", args[1]="change", args[2]=<player>, args[3]=<classId>, args[4]=[level]
        if (args.length < 4) {
            sender.sendMessage(Component.text("使用法: /rpg class change <player> <classId> [level]", NamedTextColor.RED));
            return true;
        }

        String targetPlayerName = args[2];
        String classId = args[3];
        int level = 0;

        if (args.length >= 5) {
            try {
                level = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("レベルは数値で指定してください", NamedTextColor.RED));
                return true;
            }
        }

        // ターゲットプレイヤーを取得
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(Component.text("プレイヤーが見つかりません、またはオフラインです: " + targetPlayerName, NamedTextColor.RED));
            return true;
        }

        com.example.rpgplugin.rpgclass.ClassManager clsManager = RPGPlugin.getInstance().getClassManager();
        if (clsManager == null) {
            sender.sendMessage(Component.text("クラスマネージャーが初期化されていません", NamedTextColor.RED));
            return true;
        }

        // クラス存在確認
        if (!clsManager.getClass(classId).isPresent()) {
            sender.sendMessage(Component.text("クラスが見つかりません: " + classId, NamedTextColor.RED));
            return true;
        }

        // クラス変更
        boolean success = clsManager.changeClass(targetPlayer, classId, level);
        if (success) {
            com.example.rpgplugin.rpgclass.RPGClass rpgClass = clsManager.getClass(classId).get();
            sender.sendMessage(Component.text(targetPlayer.getName() + " のクラスを変更しました: " + rpgClass.getDisplayName(), NamedTextColor.GREEN));
            targetPlayer.sendMessage(Component.text("クラスを変更されました: " + rpgClass.getDisplayName(), NamedTextColor.GREEN));
            targetPlayer.sendMessage(Component.text("レベル: " + Math.max(0, level), NamedTextColor.GRAY));
        } else {
            sender.sendMessage(Component.text("クラスの変更に失敗しました", NamedTextColor.RED));
        }

        return true;
    }

    /**
     * ヘルプを表示します
     *
     * @param player プレイヤー
     */
    private void showHelp(Player player) {
        player.sendMessage(Component.text("========================================", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.text("           RPGPlugin ヘルプ", NamedTextColor.GOLD, TextDecoration.BOLD));
        player.sendMessage(Component.text("========================================", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.empty());

        // 基本コマンド
        player.sendMessage(Component.text("【基本コマンド】", NamedTextColor.YELLOW, TextDecoration.BOLD));
        player.sendMessage(Component.text("/rpg - メインメニューを表示", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/rpg help - このヘルプを表示", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/rpg stats - ステータスを表示", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/rpg skill - スキル情報を表示", NamedTextColor.GRAY));
        player.sendMessage(Component.empty());

        // クラスコマンド
        player.sendMessage(Component.text("【クラスコマンド】", NamedTextColor.YELLOW, TextDecoration.BOLD));
        player.sendMessage(Component.text("/rpg class - クラス情報を表示", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/rpg class list - クラス一覧を表示", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/rpg class <クラスID> - クラスを選択", NamedTextColor.GRAY));
        player.sendMessage(Component.empty());

        // スキルコマンド
        player.sendMessage(Component.text("【スキルコマンド】", NamedTextColor.YELLOW, TextDecoration.BOLD));
        player.sendMessage(Component.text("/rpg cast <スキルID> - スキルを発動", NamedTextColor.GRAY));
        player.sendMessage(Component.empty());

        // 管理者コマンド
        if (player.hasPermission("rpg.admin")) {
            player.sendMessage(Component.text("【管理者コマンド】", NamedTextColor.RED, TextDecoration.BOLD));
            player.sendMessage(Component.text("/rpg reload - 設定をリロード", NamedTextColor.GRAY));
            player.sendMessage(Component.empty());
        }

        // ヒント
        player.sendMessage(Component.text("ヒント: Tabキーでコマンド補完が利用できます", NamedTextColor.GRAY, TextDecoration.ITALIC));
        player.sendMessage(Component.text("========================================", NamedTextColor.DARK_GRAY));
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
        player.sendMessage(Component.text("========================================", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.text("           クラスヘルプ", NamedTextColor.AQUA, TextDecoration.BOLD));
        player.sendMessage(Component.text("========================================", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("/rpg class list - 利用可能なクラスを表示", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/rpg class info <クラスID> - クラス情報を表示", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/rpg class change <クラスID> - クラスを変更", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/rpg class upgrade - クラスをアップグレード", NamedTextColor.GRAY));
        player.sendMessage(Component.text("========================================", NamedTextColor.DARK_GRAY));
    }

    /**
     * スキルヘルプを表示します
     *
     * @param player プレイヤー
     */
    private void showSkillHelp(Player player) {
        player.sendMessage(Component.text("========================================", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.text("           スキルヘルプ", NamedTextColor.BLUE, TextDecoration.BOLD));
        player.sendMessage(Component.text("========================================", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("/rpg cast <スキルID> - スキルを発動します", NamedTextColor.GRAY));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("発動条件:", NamedTextColor.WHITE));
        player.sendMessage(Component.text("  • スキルを習得している必要があります", NamedTextColor.GRAY));
        player.sendMessage(Component.text("  • クールダウン中ではない必要があります", NamedTextColor.GRAY));
        player.sendMessage(Component.text("  • 十分なMPを持っている必要があります", NamedTextColor.GRAY));
        player.sendMessage(Component.text("========================================", NamedTextColor.DARK_GRAY));
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
