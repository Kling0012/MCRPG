package com.example.rpgplugin;

import com.example.rpgplugin.gui.menu.StatMenu;
import com.example.rpgplugin.gui.menu.SkillMenu;
import com.example.rpgplugin.gui.menu.SkillMenuListener;
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
 *   <li>stats - ステータスGUIを表示</li>
 *   <li>skill - スキルGUIを表示</li>
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
            showMainMenu(player);
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
                showMainMenu(player);
                break;
        }

        return true;
    }

    /**
     * メインメニューを表示します
     *
     * @param player プレイヤー
     */
    private void showMainMenu(Player player) {
        player.sendMessage(ChatColor.YELLOW + "=== RPG Plugin ===");
        player.sendMessage(ChatColor.GRAY + "/rpg stats - ステータスGUIを表示");
        player.sendMessage(ChatColor.GRAY + "/rpg skill - スキルGUIを表示");
        player.sendMessage(ChatColor.GRAY + "/rpg class - クラスGUIを表示");
        player.sendMessage(ChatColor.GRAY + "/rpg auction - オークションシステム");
        player.sendMessage(ChatColor.GRAY + "/rpg trade - トレードを管理");
        player.sendMessage(ChatColor.GRAY + "/rpg cast <スキルID> - スキルを発動");
        player.sendMessage(ChatColor.GRAY + "/rpg balance - 残高を確認");
        player.sendMessage(ChatColor.GRAY + "/rpg help - ヘルプを表示");
        if (player.hasPermission("rpg.admin")) {
            player.sendMessage(ChatColor.GRAY + "/rpg reload - 設定をリロード");
        }
    }

    /**
     * ステータスコマンドを処理します
     *
     * <p>StatMenu（GUI）を開きます。</p>
     * <p>GUIを開けない場合はテキストベースのステータス表示にフォールバックします。</p>
     *
     * @param player プレイヤー
     */
    private void handleStatsCommand(Player player) {
        StatManager statManager = RPGPlugin.getInstance().getStatManager();
        PlayerManager playerManager = RPGPlugin.getInstance().getPlayerManager();

        // StatMenuを開く
        if (statManager != null && playerManager != null) {
            StatMenu menu = new StatMenu(player, statManager, playerManager);
            menu.open();
        } else {
            // フォールバック: テキストベースのステータス表示
            handleStatsTextFallback(player);
        }
    }

    /**
     * ステータスコマンドのフォールバック処理
     *
     * <p>GUIが使用できない場合のテキストベース表示です。</p>
     *
     * @param player プレイヤー
     */
    private void handleStatsTextFallback(Player player) {
        PlayerManager playerManager = RPGPlugin.getInstance().getPlayerManager();
        if (playerManager == null) {
            player.sendMessage(ChatColor.RED + "プレイヤーマネージャーが初期化されていません");
            return;
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            player.sendMessage(ChatColor.RED + "プレイヤーデータがロードされていません");
            return;
        }

        // ステータス情報を表示
        sendStatsMessage(rpgPlayer);
    }

    /**
     * ステータス情報を送信します
     *
     * @param rpgPlayer RPGプレイヤー
     */
    private void sendStatsMessage(RPGPlayer rpgPlayer) {
        Player player = rpgPlayer.getBukkitPlayer();
        if (player == null) {
            return;
        }

        // ヘッダー
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "========== ステータス ==========");
        player.sendMessage(ChatColor.YELLOW + "プレイヤー: " + ChatColor.WHITE + rpgPlayer.getUsername());
        player.sendMessage("");

        // レベル情報
        int level = rpgPlayer.getVanillaLevel();
        player.sendMessage(ChatColor.GREEN + "レベル: " + ChatColor.WHITE + level);
        player.sendMessage("");

        // HP/MP情報
        int maxHp = StatCalculator.calculateMaxHp(rpgPlayer.getStatManager());
        int maxMp = StatCalculator.calculateMaxMp(rpgPlayer.getStatManager());
        double mpRegen = StatCalculator.calculateMpRegen(rpgPlayer.getStatManager());

        player.sendMessage(ChatColor.RED + "HP: " + ChatColor.WHITE + maxHp + " / " + maxHp);
        player.sendMessage(ChatColor.BLUE + "MP: " + ChatColor.WHITE + maxMp + " / " + maxMp + ChatColor.GRAY + " (+" + String.format("%.1f", mpRegen) + "/秒)");
        player.sendMessage("");

        // ステータス情報
        player.sendMessage(ChatColor.GOLD + "----- 基本ステータス -----");

        for (Stat stat : Stat.values()) {
            int baseValue = rpgPlayer.getBaseStat(stat);
            int finalValue = rpgPlayer.getFinalStat(stat);

            // 基本値と最終値が異なる場合は色分け
            ChatColor valueColor = (baseValue == finalValue) ? ChatColor.WHITE : ChatColor.AQUA;

            player.sendMessage(String.format("%s%s§r: %s%d §7(基本: %d)",
                    stat.getColor(),
                    stat.getDisplayName(),
                    valueColor,
                    finalValue,
                    baseValue));
        }

        // 手動配分ポイント
        int availablePoints = rpgPlayer.getAvailablePoints();
        if (availablePoints > 0) {
            player.sendMessage("");
            player.sendMessage(ChatColor.LIGHT_PURPLE + "手動配分ポイント: " + ChatColor.WHITE + availablePoints);
            player.sendMessage(ChatColor.GRAY + "GUIで配分してください");
        }

        // 攻撃/防御情報
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "----- 戦闘ステータス -----");
        player.sendMessage(ChatColor.RED + "物理攻撃力: " + ChatColor.WHITE + StatCalculator.calculatePhysicalAttack(rpgPlayer.getStatManager()));
        player.sendMessage(ChatColor.BLUE + "魔法攻撃力: " + ChatColor.WHITE + StatCalculator.calculateMagicAttack(rpgPlayer.getStatManager()));
        player.sendMessage(ChatColor.DARK_GRAY + "物理防御力: " + ChatColor.WHITE + StatCalculator.calculatePhysicalDefense(rpgPlayer.getStatManager()));
        player.sendMessage(ChatColor.DARK_AQUA + "魔法防御力: " + ChatColor.WHITE + StatCalculator.calculateMagicDefense(rpgPlayer.getStatManager()));
        player.sendMessage("");

        // 命中/回避/クリティカル
        double hitRate = StatCalculator.calculateHitRate(rpgPlayer.getStatManager());
        double dodgeRate = StatCalculator.calculateDodgeRate(rpgPlayer.getStatManager());
        double critRate = StatCalculator.calculateCriticalRate(rpgPlayer.getStatManager());
        double critDamage = StatCalculator.calculateCriticalDamage(rpgPlayer.getStatManager());

        player.sendMessage(ChatColor.GOLD + "----- 命中 & 回避 -----");
        player.sendMessage(String.format(ChatColor.GRAY + "命中率: " + ChatColor.WHITE + "%.1f%%", hitRate));
        player.sendMessage(String.format(ChatColor.GRAY + "回避率: " + ChatColor.WHITE + "%.1f%%", dodgeRate));
        player.sendMessage(String.format(ChatColor.GRAY + "クリティカル率: " + ChatColor.WHITE + "%.1f%%", critRate));
        player.sendMessage(String.format(ChatColor.GRAY + "クリティカル倍率: " + ChatColor.WHITE + "%.2fx", critDamage));

        // フッター
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "================================");
        player.sendMessage("");
    }

    /**
 * スキルコマンドを処理します
 *
 * <p>SkillMenu（GUI）を開きます。</p>
 * <p>Phase14: SkillTreeRegistryを使用して自動的にスキルツリーを構築します。</p>
 *
 * @param player プレイヤー
 */
private void handleSkillCommand(Player player) {
    SkillManager skillManager = RPGPlugin.getInstance().getSkillManager();
    if (skillManager == null) {
        player.sendMessage(ChatColor.RED + "スキルマネージャーが初期化されていません");
        return;
    }

    PlayerManager playerManager = RPGPlugin.getInstance().getPlayerManager();
    if (playerManager == null) {
        player.sendMessage(ChatColor.RED + "プレイヤーマネージャーが初期化されていません");
        return;
    }

    RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
    if (rpgPlayer == null) {
        player.sendMessage(ChatColor.RED + "プレイヤーデータがロードされていません");
        return;
    }

    try {
        // Phase14: 新しいコンストラクタを使用（自動更新対応）
        // SkillTreeRegistryから自動的にスキルツリーを取得
        SkillMenu menu = new SkillMenu(RPGPlugin.getInstance(), player);

        // リスナーに登録
        SkillMenuListener listener = RPGPlugin.getInstance().getSkillMenuListener();
        if (listener != null) {
            listener.registerMenu(player, menu);
        }

        menu.open();
    } catch (Exception e) {
        player.sendMessage(ChatColor.RED + "スキルメニューを開けませんでした");
        RPGPlugin.getInstance().getLogger().warning(
            "Failed to open skill menu for " + player.getName() + ": " + e.getMessage()
        );
    }
}

    /**
     * キャストコマンドを処理します
     *
     * <p>スキルを発動します。</p>
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
     * <p>引数なし: GUIを開く</p>
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

        // 引数なし: GUIを開く
        if (args.length == 1) {
            openClassMenu(player);
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
     * クラスGUIを開きます
     *
     * @param player プレイヤー
     */
    private void openClassMenu(Player player) {
        com.example.rpgplugin.rpgclass.ClassManager clsManager = RPGPlugin.getInstance().getClassManager();

        if (clsManager == null) {
            player.sendMessage(ChatColor.RED + "クラスシステムが初期化されていません");
            return;
        }

        try {
            // 初期クラス選択GUIを開く（ClassUpgraderは不要）
            player.sendMessage(ChatColor.YELLOW + "クラス選択GUIは現在開発中です");
            player.sendMessage(ChatColor.GRAY + "利用可能なクラス: " + clsManager.getInitialClasses().size());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "エラーが発生しました: " + e.getMessage());
            e.printStackTrace();
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
        player.sendMessage(ChatColor.WHITE + "/rpg stats" + ChatColor.GRAY + " - ステータスGUIを表示");
        player.sendMessage(ChatColor.WHITE + "/rpg skill" + ChatColor.GRAY + " - スキルGUIを表示");
        player.sendMessage(ChatColor.WHITE + "/rpg balance" + ChatColor.GRAY + " - 残高を確認");
        player.sendMessage("");

        // クラスコマンド
        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "【クラスコマンド】");
        player.sendMessage(ChatColor.WHITE + "/rpg class" + ChatColor.GRAY + " - クラスGUIを表示");
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
            case "スキル":
                showSkillHelp(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "不明なカテゴリです: " + category);
                player.sendMessage(ChatColor.GRAY + "使用法: /rpg help [category]");
                break;
        }
    }

    /**
     * クラスコマンドのヘルプを表示します
     *
     * @param player プレイヤー
     */
    private void showClassHelp(Player player) {
        player.sendMessage(ChatColor.YELLOW + "=== クラスコマンドヘルプ ===");
        player.sendMessage(ChatColor.GOLD + "/rpg class" + ChatColor.GRAY + " - クラス選択GUIを開きます");
        player.sendMessage(ChatColor.GOLD + "/rpg class list" + ChatColor.GRAY + " - 利用可能なクラスを表示します");
        player.sendMessage(ChatColor.GOLD + "/rpg class <クラスID>" + ChatColor.GRAY + " - 指定したクラスに変更します");
        player.sendMessage("");
        player.sendMessage(ChatColor.WHITE + "利用可能なクラスID:");
        com.example.rpgplugin.rpgclass.ClassManager classManager = RPGPlugin.getInstance().getClassManager();
        if (classManager != null) {
            for (com.example.rpgplugin.rpgclass.RPGClass rpgClass : classManager.getInitialClasses()) {
                player.sendMessage(ChatColor.GRAY + "  - " + rpgClass.getId() + ": " + rpgClass.getDisplayName());
            }
        }
    }

    /**
     * オークションコマンドのヘルプを表示します
     *
     * @param player プレイヤー
     */
    private void showAuctionHelp(Player player) {
        player.sendMessage(ChatColor.YELLOW + "=== オークションコマンドヘルプ ===");
        player.sendMessage(ChatColor.GOLD + "/rpg auction list" + ChatColor.GRAY + " - アクティブなオークション一覧");
        player.sendMessage(ChatColor.GOLD + "/rpg auction info <ID>" + ChatColor.GRAY + " - オークション詳細を表示");
        player.sendMessage(ChatColor.GOLD + "/rpg auction bid <ID> <金額>" + ChatColor.GRAY + " - 入札する");
        player.sendMessage(ChatColor.GOLD + "/rpg auction create <価格> <秒数>" + ChatColor.GRAY + " - 手持ちアイテムを出品");
        player.sendMessage(ChatColor.GOLD + "/rpg auction cancel <ID>" + ChatColor.GRAY + " - 自分の出品をキャンセル");
        player.sendMessage("");
        player.sendMessage(ChatColor.WHITE + "入札ルール:");
        player.sendMessage(ChatColor.GRAY + "  • 開始価格以上である必要があります");
        player.sendMessage(ChatColor.GRAY + "  • 現在の入札額の10%以上上乗せする必要があります");
        player.sendMessage(ChatColor.GRAY + "  • 入札があると有効期限が+5秒延長されます");
        player.sendMessage(ChatColor.GRAY + "  • 出品期間は30-180秒です");
    }

    /**
     * スキルコマンドのヘルプを表示します
     *
     * @param player プレイヤー
     */
    private void showSkillHelp(Player player) {
        player.sendMessage(ChatColor.YELLOW + "=== スキルコマンドヘルプ ===");
        player.sendMessage(ChatColor.GOLD + "/rpg skill" + ChatColor.GRAY + " - スキルGUIを開きます");
        player.sendMessage(ChatColor.GOLD + "/rpg cast <スキルID>" + ChatColor.GRAY + " - スキルを発動します");
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
