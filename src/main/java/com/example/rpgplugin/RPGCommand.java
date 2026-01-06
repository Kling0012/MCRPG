package com.example.rpgplugin;

import com.example.rpgplugin.gui.menu.SkillMenu;
import com.example.rpgplugin.gui.menu.SkillMenuListener;
import com.example.rpgplugin.gui.menu.StatMenu;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillTree;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import com.example.rpgplugin.stats.calculator.StatCalculator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
 *   <li>help - ヘルプを表示</li>
 *   <li>reload - 設定をリロード（管理者のみ）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class RPGCommand implements CommandExecutor {

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

            case "help":
                showHelp(player);
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
        player.sendMessage(ChatColor.GRAY + "/rpg cast <スキルID> - スキルを発動");
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

        // StatMenuを開く
        if (statManager != null) {
            StatMenu menu = new StatMenu(player, statManager);
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
     * ヘルプを表示します
     *
     * @param player プレイヤー
     */
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.YELLOW + "=== ヘルプ ===");
        player.sendMessage(ChatColor.GRAY + "/rpg stats - ステータスGUIを表示");
        player.sendMessage(ChatColor.GRAY + "/rpg skill - スキルGUIを表示");
        player.sendMessage(ChatColor.GRAY + "/rpg cast <スキルID> - スキルを発動");
        player.sendMessage(ChatColor.GRAY + "/rpg help - ヘルプを表示");
        if (player.hasPermission("rpg.admin")) {
            player.sendMessage(ChatColor.GRAY + "/rpg reload - 設定をリロード");
        }
    }

    /**
     * スキルコマンドを処理します
     *
     * <p>SkillMenu（GUI）を開きます。</p>
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

        RPGPlayer rpgPlayer = playerManager.getPlayer(player);
        if (rpgPlayer == null) {
            player.sendMessage(ChatColor.RED + "プレイヤーデータがロードされていません");
            return;
        }

        // スキルツリーを作成
        // TODO: クラスごとのスキルツリー設定を読み込む
        String classId = rpgPlayer.getClassId();
        SkillTree skillTree = new SkillTree(classId);

        // TODO: スキルツリーにノードを追加
        // 現時点では空のツリーを作成

        // SkillMenuを開く
        SkillMenu menu = new SkillMenu(RPGPlugin.getInstance(), player, skillTree);

        // リスナーに登録
        SkillMenuListener listener = RPGPlugin.getInstance().getSkillMenuListener();
        if (listener != null) {
            listener.registerMenu(player, menu);
        }

        menu.open();
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

        // スキル発動
        // TODO: ActiveSkillExecutorを使用した発動処理
        player.sendMessage(ChatColor.YELLOW + "スキル発動処理を実装中: " + skill.getColoredDisplayName());
    }
}
