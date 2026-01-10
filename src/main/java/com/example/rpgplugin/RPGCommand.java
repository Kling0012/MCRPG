package com.example.rpgplugin;

import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillTree;
import com.example.rpgplugin.skill.executor.ActiveSkillExecutor;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import com.example.rpgplugin.stats.calculator.StatCalculator;
import org.bukkit.Bukkit;
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
 *   <li>balance - 残高確認</li>
 *   <li>auction - オークションシステム</li>
 *   <li>trade - トレード管理</li>
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
            sender.sendMessage(ChatColor.GREEN + "リロード完了! (" + duration + "ms)");
            return true;
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
            player.sendMessage(ChatColor.GRAY + "/rpg auction - オークションシステム");
            player.sendMessage(ChatColor.GRAY + "/rpg trade - トレードを管理");
            player.sendMessage(ChatColor.GRAY + "/rpg cast <スキルID> - スキルを発動");
            player.sendMessage(ChatColor.GRAY + "/rpg balance - 残高を確認");
            player.sendMessage(ChatColor.GRAY + "/rpg help - ヘルプを表示");
            if (player.hasPermission("rpg.admin")) {
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

            case "balance":
                handleBalanceCommand(player);
                break;

            case "auction":
                handleAuctionCommand(player, args);
                break;

            case "trade":
                handleTradeCommand(player, args);
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
    player.sendMessage(ChatColor.GRAY + "  %rpgplugin_level% - レベル");
    player.sendMessage(ChatColor.GRAY + "  %rpgplugin_class% - クラス名");
    player.sendMessage(ChatColor.GRAY + "  %rpgplugin_stat_strength% - STR");
    player.sendMessage(ChatColor.GRAY + "  %rpgplugin_stat_intelligence% - INT");
    player.sendMessage(ChatColor.GRAY + "  %rpgplugin_stat_spirit% - SPI");
    player.sendMessage(ChatColor.GRAY + "  %rpgplugin_stat_vitality% - VIT");
    player.sendMessage(ChatColor.GRAY + "  %rpgplugin_stat_dexterity% - DEX");
    player.sendMessage(ChatColor.GRAY + "  %rpgplugin_available_points% - 振り分け可能ポイント");
    player.sendMessage("");
    player.sendMessage(ChatColor.GRAY + "/rpg help で全コマンドを表示");
}



    /**
 * スキルコマンドを処理します
 *
 * <p>テキストベースでスキル情報を表示します。</p>
 *
 * @param player プレイヤー
 */
priv/**
 * スキルコマンドを処理します
 *
 * <p>テキストベースでスキル情報を表示します。</p>
 *
 * @param player プレイヤー
 */
/**
 * スキルコマンドを処理します
 *
 * <p>外部GUIプラグイン用のプレースホルダー案内を表示します。</p>
 *
 * @param player プレイヤー
 */
private void handleSkillCommand(Player player) {
    // 外部GUIプラグイン用のプレースホルダー案内
    player.sendMessage(ChatColor.YELLOW + "=== RPGPlugin スキル ===");
    player.sendMessage(ChatColor.GRAY + "スキルは外部GUIプラグインで表示できます");
    player.sendMessage("");
    player.sendMessage(ChatColor.WHITE + "主なプレースホルダー:");
    player.sendMessage(ChatColor.GRAY + "  %rpgplugin_skill_count% - 習得スキル数");
    player.sendMessage(ChatColor.GRAY + "  %rpgplugin_skill_level_<スキル名>% - スキルレベル");
    player.sendMessage(ChatColor.GRAY + "  %rpgplugin_has_skill_<スキル名>% - スキル所持確認");
    player.sendMessage("");
    player.sendMessage(ChatColor.WHITE + "/rpg cast <スキルID> でスキルを発動");
    player.sendMessage(ChatColor.GRAY + "/rpg help で全コマンドを表示");
}       for (var entry : skills.entrySet()) {
            String skillId = entry.getKey();
            Integer level = entry.getValue();
            player.sendMessage(ChatColor.WHITE + " - " + skillId + " (Lv." + level + ")");
        }
        player.sendMessage(ChatColor.GRAY + "/rpg cast <スキルID> でスキルを発動");
    }
}    * @param player プレイヤー
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

        // パッシブ/アクティブの区別を廃止したため、チェックを削除

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
     * <p>&lt;クラスID&gt;: クラスを変更</p>
     * <p>list: クラス一覧を表示</p>
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
            player.sendMessage(ChatColor.GRAY + "/rpg class set <クラスID> - クラスを変更");
            return;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "list":
                handleClassListCommand(player, clsManager);
                break;

            default:
                // クラスIDとして解釈
                handleClassSetCommand(player, clsManager, subCommand);
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
     * 残高コマンドを処理します
     *
     * <p>現在のゴールド残高を表示します。</p>
     *
     * @param player プレイヤー
     */
    private void handleBalanceCommand(Player player) {
        com.example.rpgplugin.currency.CurrencyManager currencyManager = RPGPlugin.getInstance().getCurrencyManager();
        if (currencyManager == null) {
            player.sendMessage(ChatColor.RED + "通貨システムが初期化されていません");
            return;
        }

        double balance = currencyManager.getGoldBalance(player);

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "========== 残高情報 ==========");
        player.sendMessage(ChatColor.YELLOW + "プレイヤー: " + ChatColor.WHITE + player.getName());
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "----- 所持金 -----");
        player.sendMessage(ChatColor.YELLOW + "ゴールド: " + ChatColor.WHITE + String.format("%.2f", balance) + " G");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "================================");
        player.sendMessage("");
    }

    /**
     * オークションコマンドを処理します
     *
     * @param player プレイヤー
     * @param args 引数
     */
    private void handleAuctionCommand(Player player, String[] args) {
        com.example.rpgplugin.auction.AuctionManager auctionManager = RPGPlugin.getInstance().getAuctionManager();
        if (auctionManager == null) {
            player.sendMessage(ChatColor.RED + "オークションマネージャーが初期化されていません");
            return;
        }

        // AuctionCommandに委譲
        com.example.rpgplugin.auction.AuctionCommand auctionCommand =
                new com.example.rpgplugin.auction.AuctionCommand(auctionManager);
        auctionCommand.onCommand(player, null, "auction", args);
    }

    /**
     * トレードコマンドを処理します
     *
     * <p>サブコマンド:</p>
     * <ul>
     *   <li>request &lt;player&gt; - トレード申請</li>
     *   <li>accept - トレード承認</li>
     *   <li>deny - トレード拒否</li>
     * </ul>
     *
     * @param player プレイヤー
     * @param args 引数
     */
    private void handleTradeCommand(Player player, String[] args) {
        // TradeManagerの取得
        com.example.rpgplugin.trade.TradeManager tradeManager = RPGPlugin.getInstance().getTradeManager();
        if (tradeManager == null) {
            player.sendMessage(ChatColor.RED + "トレードシステムが利用できません");
            return;
        }

        // サブコマンドチェック
        if (args.length < 2) {
            showTradeHelp(player);
            return;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "request":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "使用法: /rpg trade request <プレイヤー名>");
                    return;
                }
                handleTradeRequest(player, args[2], tradeManager);
                break;

            case "accept":
                tradeManager.acceptRequest(player);
                break;

            case "deny":
                tradeManager.denyRequest(player);
                break;

            default:
                player.sendMessage(ChatColor.RED + "不明なサブコマンドです: " + subCommand);
                showTradeHelp(player);
                break;
        }
    }

    /**
     * トレード申請を処理
     *
     * @param requester 申請者
     * @param targetName 相手のプレイヤー名
     * @param tradeManager トレードマネージャー
     */
    private void handleTradeRequest(Player requester, String targetName, com.example.rpgplugin.trade.TradeManager tradeManager) {
        // 相手を検索
        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            requester.sendMessage(ChatColor.RED + "プレイヤーが見つかりません: " + targetName);
            return;
        }

        // トレード申請
        tradeManager.sendRequest(requester, target);
    }

    /**
     * トレードヘルプを表示
     *
     * @param player プレイヤー
     */
    private void showTradeHelp(Player player) {
        player.sendMessage(ChatColor.YELLOW + "=== トレードコマンド ===");
        player.sendMessage(ChatColor.GRAY + "/rpg trade request <プレイヤー名> - トレードを申請");
        player.sendMessage(ChatColor.GRAY + "/rpg trade accept - トレードを承認");
        player.sendMessage(ChatColor.GRAY + "/rpg trade deny - トレードを拒否");
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
        player.sendMessage(ChatColor.WHITE + "/rpg balance" + ChatColor.GRAY + " - 残高を確認");
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

        // オークションコマンド
        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "【オークションコマンド】");
        player.sendMessage(ChatColor.WHITE + "/rpg auction list" + ChatColor.GRAY + " - オークション一覧");
        player.sendMessage(ChatColor.WHITE + "/rpg auction info <ID>" + ChatColor.GRAY + " - 詳細表示");
        player.sendMessage(ChatColor.WHITE + "/rpg auction bid <ID> <金額>" + ChatColor.GRAY + " - 入札");
        player.sendMessage(ChatColor.WHITE + "/rpg auction create <価格> <秒数>" + ChatColor.GRAY + " - 出品");
        player.sendMessage(ChatColor.WHITE + "/rpg auction cancel <ID>" + ChatColor.GRAY + " - キャンセル");
        player.sendMessage("");

        // トレードコマンド
        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "【トレードコマンド】");
        player.sendMessage(ChatColor.WHITE + "/rpg trade request <プレイヤー名>" + ChatColor.GRAY + " - トレード申請");
        player.sendMessage(ChatColor.WHITE + "/rpg trade accept" + ChatColor.GRAY + " - トレード承認");
        player.sendMessage(ChatColor.WHITE + "/rpg trade deny" + ChatColor.GRAY + " - トレード拒否");
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
            case "auction":
            case "オークション":
                showAuctionHelp(player);
                break;
            case "trade":
            case "トレード":
                showTradeHelp(player);
                break;
            case "skill":
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
    player.sendMessage(ChatColor.WHITE + "/rpg" + ChatColor.GRAY + " - プレースホルダー案内を表示");
    player.sendMessage(ChatColor.WHITE + "/rpg help" + ChatColor.GRAY + " - このヘルプを表示");
    player.sendMessage(ChatColor.WHITE + "/rpg stats" + ChatColor.GRAY + " - ステータスプレースホルダー案内");
    player.sendMessage(ChatColor.WHITE + "/rpg skill" + ChatColor.GRAY + " - スキルプレースホルダー案内");
    player.sendMessage(ChatColor.WHITE + "/rpg balance" + ChatColor.GRAY + " - 残高を確認");
    player.sendMessage("");

    // クラスコマンド
    player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "【クラスコマンド】");
    player.sendMessage(ChatColor.WHITE + "/rpg class list" + ChatColor.GRAY + " - クラス一覧を表示");
    player.sendMessage(ChatColor.WHITE + "/rpg class <クラスID>" + ChatColor.GRAY + " - クラスを選択");
    player.sendMessage("");

    // スキルコマンド
    player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "【スキルコマンド】");
    player.sendMessage(ChatColor.WHITE + "/rpg cast <スキルID>" + ChatColor.GRAY + " - スキルを発動");
    player.sendMessage("");

    // PlaceholderAPI
    player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "【PlaceholderAPI】");
    player.sendMessage(ChatColor.GRAY + "外部GUIプラグインで以下のプレースホルダーが使用できます:");
    player.sendMessage(ChatColor.WHITE + "  %rpgplugin_level% - レベル");
    player.sendMessage(ChatColor.WHITE + "  %rpgplugin_class% - クラス名");
    player.sendMessage(ChatColor.WHITE + "  %rpgplugin_stat_strength% - STR");
    player.sendMessage(ChatColor.WHITE + "  %rpgplugin_available_points% - 振り分け可能ポイント");
    player.sendMessage("");

    // オークションコマンド
    player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "【オークションコマンド】");
    player.sendMessage(ChatColor.WHITE + "/rpg auction list" + ChatColor.GRAY + " - オークション一覧");
    player.sendMessage(ChatColor.WHITE + "/rpg auction info <ID>" + ChatColor.GRAY + " - 詳細表示");
    player.sendMessage(ChatColor.WHITE + "/rpg auction bid <ID> <金額>" + ChatColor.GRAY + " - 入札");
    player.sendMessage("");

    // トレードコマンド
    player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "【トレードコマンド】");
    player.sendMessage(ChatColor.WHITE + "/rpg trade request <プレイヤー名>" + ChatColor.GRAY + " - トレード申請");
    player.sendMessage(ChatColor.WHITE + "/rpg trade accept" + ChatColor.GRAY + " - トレード承認");
    player.sendMessage(ChatColor.WHITE + "/rpg trade deny" + ChatColor.GRAY + " - トレード拒否");
    player.sendMessage("");

    // 管理者コマンド
    if (player.hasPermission("rpg.admin")) {
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "【管理者コマンド】");
        player.sendMessage(ChatColor.WHITE + "/rpg reload" + ChatColor.GRAY + " - 設定をリロード");
        player.sendMessage("");
    }

    player.sendMessage(ChatColor.DARK_GRAY + "========================================");
}   player.sendMessage(ChatColor.GOLD + "/rpg cast <スキルID>" + ChatColor.GRAY + " - スキルを発動します");
        player.sendMessage("");
        player.sendMessage(ChatColor.WHITE + "発動条件:");
        player.sendMessage(ChatColor.GRAY + "  • スキルを習得している必要があります");
        player.sendMessage(ChatColor.GRAY + "  • クールダウン中ではない必要があります");
        player.sendMessage(ChatColor.GRAY + "  • 十分なMPを持っている必要があります");
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
        commands.add("balance");
        commands.add("auction");
        commands.add("trade");
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

            case "auction":
                return getAuctionCompletions(args);

            case "trade":
                return getTradeCompletions(args);

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
     * auctionコマンドの補完候補を取得します
     *
     * @param args 引数配列
     * @return 補完候補リスト
     */
    private List<String> getAuctionCompletions(String[] args) {
        if (args.length == 2) {
            return Arrays.asList("list", "bid", "create", "cancel", "info");
        } else if (args.length >= 3) {
            String auctionSubCommand = args[1].toLowerCase();
            if (auctionSubCommand.equals("bid") || auctionSubCommand.equals("cancel") || auctionSubCommand.equals("info")) {
                // オークションID補完（アクティブなオークションのID）
                com.example.rpgplugin.auction.AuctionManager auctionManager = RPGPlugin.getInstance().getAuctionManager();
                if (auctionManager != null) {
                    return auctionManager.getActiveAuctions().stream()
                            .map(auction -> String.valueOf(auction.getId()))
                            .collect(Collectors.toList());
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * tradeコマンドの補完候補を取得します
     *
     * @param args 引数配列
     * @return 補完候補リスト
     */
    private List<String> getTradeCompletions(String[] args) {
        if (args.length == 2) {
            return Arrays.asList("request", "accept", "deny");
        } else if (args.length == 3) {
            String tradeSubCommand = args[1].toLowerCase();
            if (tradeSubCommand.equals("request")) {
                // プレイヤー名補完
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
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
            return Arrays.asList("class", "auction", "trade", "skill");
        }
        return Collections.emptyList();
    }
}
