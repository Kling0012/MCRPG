package com.example.rpgplugin.api.bridge;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.RPGPluginAPI;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * SKriptブリッジクラス
 *
 * <p>SKriptからRPGプラグインのAPIにアクセスするためのブリッジです。</p>
 *
 * <p>SKript連携方法:</p>
 * <ul>
 *   <li>コマンド経由: {@code /rpg api <action> <args...>}</li>
 *   <li>直接メソッド呼び出し（JavaPlugin経由）</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: SKript連携に専念</li>
 *   <li>DRY: 既存APIを活用</li>
 *   <li>KISS: シンプルなコマンドベースAPI</li>
 * </ul>
 *
 * <p>使用例（SKript）:</p>
 * <pre>{@code
 * command /testrpg:
 *     trigger:
 *         execute player command "rpg api get_level %player%"
 * }</pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SKriptBridge {

    private final RPGPlugin plugin;
    private final RPGPluginAPI api;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public SKriptBridge(RPGPlugin plugin) {
        this.plugin = plugin;
        this.api = plugin.getAPI();
    }

    /**
     * SKriptからのコールを処理します
     *
     * <p>サポートされるアクション:</p>
     * <ul>
     *   <li>get_level &lt;player&gt; - レベル取得</li>
     *   <li>set_level &lt;player&gt; &lt;level&gt; - レベル設定</li>
     *   <li>get_stat &lt;player&gt; &lt;stat&gt; - ステータス取得</li>
     *   <li>set_stat &lt;player&gt; &lt;stat&gt; &lt;value&gt; - ステータス設定</li>
     *   <li>get_class &lt;player&gt; - クラスID取得</li>
     *   <li>set_class &lt;player&gt; &lt;classId&gt; - クラス設定</li>
     *   <li>upgrade_class &lt;player&gt; - クラスアップ</li>
     *   <li>can_upgrade_class &lt;player&gt; - クラスアップ可能か</li>
     *   <li>has_skill &lt;player&gt; &lt;skillId&gt; - スキル習得確認</li>
     *   <li>unlock_skill &lt;player&gt; &lt;skillId&gt; - スキル習得</li>
     *   <li>cast_skill &lt;player&gt; &lt;skillId&gt; - スキル使用</li>
     *   <li>get_skill_level &lt;player&gt; &lt;skillId&gt; - スキルレベル取得</li>
     *   <li>get_gold &lt;player&gt; - ゴールド残高取得</li>
     *   <li>give_gold &lt;player&gt; &lt;amount&gt; - ゴールド付与</li>
     *   <li>take_gold &lt;player&gt; &lt;amount&gt; - ゴールド剥奪</li>
     *   <li>has_gold &lt;player&gt; &lt;amount&gt; - ゴールド所持確認</li>
     *   <li>transfer_gold &lt;from&gt; &lt;to&gt; &lt;amount&gt; - ゴールド転送</li>
     *   <li>calculate_damage &lt;attacker&gt; &lt;target&gt; - ダメージ計算</li>
     *   <li>get_target &lt;player&gt; - 最後のターゲット取得</li>
     *   <li>set_target &lt;player&gt; &lt;entityId&gt; - ターゲット設定</li>
     *   <li>cast_at &lt;player&gt; &lt;skillId&gt; &lt;targetPlayer&gt; - 指定ターゲットでスキル発動</li>
     *   <li>cast_with_cost &lt;player&gt; &lt;skillId&gt; &lt;costType&gt; - コストタイプ指定で発動</li>
     *   <li>get_entities_in_area &lt;player&gt; &lt;shape&gt; &lt;params...&gt; - 範囲内エンティティ取得</li>
     * </ul>
     *
     * @param sender コマンド送信者
     * @param args 引数配列
     * @return 処理結果
     */
    public boolean handleCall(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return false;
        }

        String action = args[0].toLowerCase();
        String[] actionArgs = new String[args.length - 1];
        System.arraycopy(args, 1, actionArgs, 0, actionArgs.length);

        try {
            return processAction(sender, action, actionArgs);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "エラーが発生しました: " + e.getMessage());
            plugin.getLogger().warning("[SKriptBridge] Error processing action: " + action);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * アクションを処理します
     *
     * @param sender コマンド送信者
     * @param action アクション名
     * @param args 引数
     * @return 成功した場合はtrue
     */
    private boolean processAction(CommandSender sender, String action, String[] args) {
        Player targetPlayer = null;

        // 最初の引数がプレイヤー名の場合は取得
        if (args.length > 0) {
            targetPlayer = Bukkit.getPlayer(args[0]);
        }

        switch (action) {
            // ==================== レベル操作 ====================
            case "get_level":
                if (targetPlayer == null) {
                    sender.sendMessage(ChatColor.RED + "プレイヤーが指定されていません");
                    return false;
                }
                int level = api.getLevel(targetPlayer);
                sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " のLV: " + level);
                return true;

            case "set_level":
                if (targetPlayer == null || args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api set_level <player> <level>");
                    return false;
                }
                try {
                    int newLevel = Integer.parseInt(args[1]);
                    api.setLevel(targetPlayer, newLevel);
                    sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " のLVを " + newLevel + " に設定しました");
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "レベルは整数で指定してください");
                    return false;
                }

            // ==================== ステータス操作 ====================
            case "get_stat":
                if (targetPlayer == null || args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api get_stat <player> <stat>");
                    return false;
                }
                Stat stat = parseStat(args[1]);
                if (stat == null) {
                    sender.sendMessage(ChatColor.RED + "無効なステータス: " + args[1]);
                    return false;
                }
                int statValue = api.getStat(targetPlayer, stat);
                sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " の" + stat.getDisplayName() + ": " + statValue);
                return true;

            case "set_stat":
                if (targetPlayer == null || args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api set_stat <player> <stat> <value>");
                    return false;
                }
                Stat statToSet = parseStat(args[1]);
                if (statToSet == null) {
                    sender.sendMessage(ChatColor.RED + "無効なステータス: " + args[1]);
                    return false;
                }
                try {
                    int value = Integer.parseInt(args[2]);
                    api.setStat(targetPlayer, statToSet, value);
                    sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " の" + statToSet.getDisplayName() + "を " + value + " に設定しました");
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "値は整数で指定してください");
                    return false;
                }

            // ==================== クラス操作 ====================
            case "get_class":
                if (targetPlayer == null) {
                    sender.sendMessage(ChatColor.RED + "プレイヤーが指定されていません");
                    return false;
                }
                String classId = api.getClassId(targetPlayer);
                sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " のクラス: " + (classId != null ? classId : "なし"));
                return true;

            case "set_class":
                if (targetPlayer == null || args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api set_class <player> <classId>");
                    return false;
                }
                boolean setSuccess = api.setClass(targetPlayer, args[1]);
                if (setSuccess) {
                    sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " のクラスを " + args[1] + " に設定しました");
                } else {
                    sender.sendMessage(ChatColor.RED + "クラスの設定に失敗しました");
                }
                return setSuccess;

            case "upgrade_class":
                if (targetPlayer == null) {
                    sender.sendMessage(ChatColor.RED + "プレイヤーが指定されていません");
                    return false;
                }
                boolean upgradeSuccess = api.upgradeClassRank(targetPlayer);
                if (upgradeSuccess) {
                    sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " のクラスをランクアップしました");
                } else {
                    sender.sendMessage(ChatColor.RED + "クラスアップに失敗しました");
                }
                return upgradeSuccess;

            case "can_upgrade_class":
                if (targetPlayer == null) {
                    sender.sendMessage(ChatColor.RED + "プレイヤーが指定されていません");
                    return false;
                }
                boolean canUpgrade = api.canUpgradeClass(targetPlayer);
                sender.sendMessage(ChatColor.GREEN + "クラスアップ可能: " + (canUpgrade ? "はい" : "いいえ"));
                return true;

            // ==================== スキル操作 ====================
            case "has_skill":
                if (targetPlayer == null || args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api has_skill <player> <skillId>");
                    return false;
                }
                boolean hasSkill = api.hasSkill(targetPlayer, args[1]);
                sender.sendMessage(ChatColor.GREEN + "スキル習得: " + (hasSkill ? "はい" : "いいえ"));
                return true;

            case "unlock_skill":
                if (targetPlayer == null || args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api unlock_skill <player> <skillId>");
                    return false;
                }
                boolean unlockSuccess = api.unlockSkill(targetPlayer, args[1]);
                if (unlockSuccess) {
                    sender.sendMessage(ChatColor.GREEN + "スキル " + args[1] + " を習得させました");
                } else {
                    sender.sendMessage(ChatColor.RED + "スキルの習得に失敗しました");
                }
                return unlockSuccess;

            case "cast_skill":
                if (targetPlayer == null || args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api cast_skill <player> <skillId>");
                    return false;
                }
                boolean castSuccess = api.castSkill(targetPlayer, args[1]);
                if (castSuccess) {
                    sender.sendMessage(ChatColor.GREEN + "スキル " + args[1] + " を使用しました");
                } else {
                    sender.sendMessage(ChatColor.RED + "スキルの使用に失敗しました");
                }
                return castSuccess;

            case "get_skill_level":
                if (targetPlayer == null || args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api get_skill_level <player> <skillId>");
                    return false;
                }
                int skillLevel = api.getSkillLevel(targetPlayer, args[1]);
                sender.sendMessage(ChatColor.GREEN + "スキルレベル: " + skillLevel);
                return true;

            // ==================== 経済操作 ====================
            case "get_gold":
                if (targetPlayer == null) {
                    sender.sendMessage(ChatColor.RED + "プレイヤーが指定されていません");
                    return false;
                }
                double gold = api.getGoldBalance(targetPlayer);
                sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " のゴールド: " + String.format("%.2f", gold) + "G");
                return true;

            case "give_gold":
                if (targetPlayer == null || args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api give_gold <player> <amount>");
                    return false;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    boolean depositSuccess = api.depositGold(targetPlayer, amount);
                    if (depositSuccess) {
                        sender.sendMessage(ChatColor.GREEN + String.format("%.2f", amount) + "G を付与しました");
                    } else {
                        sender.sendMessage(ChatColor.RED + "ゴールドの付与に失敗しました");
                    }
                    return depositSuccess;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "金額は数値で指定してください");
                    return false;
                }

            case "take_gold":
                if (targetPlayer == null || args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api take_gold <player> <amount>");
                    return false;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    boolean withdrawSuccess = api.withdrawGold(targetPlayer, amount);
                    if (withdrawSuccess) {
                        sender.sendMessage(ChatColor.GREEN + String.format("%.2f", amount) + "G を剥奪しました");
                    } else {
                        sender.sendMessage(ChatColor.RED + "ゴールドが足りません");
                    }
                    return withdrawSuccess;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "金額は数値で指定してください");
                    return false;
                }

            case "has_gold":
                if (targetPlayer == null || args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api has_gold <player> <amount>");
                    return false;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    boolean hasEnough = api.hasEnoughGold(targetPlayer, amount);
                    sender.sendMessage(ChatColor.GREEN + "ゴールド所持: " + (hasEnough ? "はい" : "いいえ"));
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "金額は数値で指定してください");
                    return false;
                }

            case "transfer_gold":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api transfer_gold <from> <to> <amount>");
                    return false;
                }
                Player fromPlayer = Bukkit.getPlayer(args[0]);
                Player toPlayer = Bukkit.getPlayer(args[1]);
                if (fromPlayer == null || toPlayer == null) {
                    sender.sendMessage(ChatColor.RED + "プレイヤーが見つかりません");
                    return false;
                }
                try {
                    double amount = Double.parseDouble(args[2]);
                    boolean transferSuccess = api.transferGold(fromPlayer, toPlayer, amount);
                    if (transferSuccess) {
                        sender.sendMessage(ChatColor.GREEN + String.format("%.2f", amount) + "G を転送しました");
                    } else {
                        sender.sendMessage(ChatColor.RED + "転送に失敗しました");
                    }
                    return transferSuccess;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "金額は数値で指定してください");
                    return false;
                }

            // ==================== ダメージ計算 ====================
            case "calculate_damage":
                if (targetPlayer == null || args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api calculate_damage <attacker> <target>");
                    return false;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "ターゲットが見つかりません");
                    return false;
                }
                double damage = api.calculateDamage(targetPlayer, target);
                sender.sendMessage(ChatColor.GREEN + "計算ダメージ: " + String.format("%.2f", damage));
                return true;

            // ==================== ターゲット管理 ====================
            case "get_target":
                if (targetPlayer == null) {
                    sender.sendMessage(ChatColor.RED + "プレイヤーが指定されていません");
                    return false;
                }
                Optional<Entity> lastTarget = api.getLastTargetedEntity(targetPlayer);
                if (lastTarget.isPresent()) {
                    Entity entity = lastTarget.get();
                    String targetInfo = entity.getType().name();
                    if (entity instanceof Player) {
                        targetInfo = "Player:" + ((Player) entity).getName();
                    }
                    sender.sendMessage(ChatColor.GREEN + "ターゲット: " + targetInfo);
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "ターゲットは設定されていません");
                }
                return true;

            case "set_target":
                if (targetPlayer == null || args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api set_target <player> <entityId>");
                    return false;
                }
                try {
                    int entityId = Integer.parseInt(args[1]);
                    Entity targetEntity = null;
                    for (Entity e : targetPlayer.getWorld().getEntities()) {
                        if (e.getEntityId() == entityId) {
                            targetEntity = e;
                            break;
                        }
                    }
                    if (targetEntity != null) {
                        api.setTargetedEntity(targetPlayer, targetEntity);
                        sender.sendMessage(ChatColor.GREEN + "ターゲットを設定しました: " + targetEntity.getType().name());
                    } else {
                        sender.sendMessage(ChatColor.RED + "エンティティが見つかりません: ID=" + args[1]);
                        return false;
                    }
                } catch (NumberFormatException e) {
                    // プレイヤー名での指定も試みる
                    Player targetEntityByName = Bukkit.getPlayer(args[1]);
                    if (targetEntityByName != null) {
                        api.setTargetedEntity(targetPlayer, targetEntityByName);
                        sender.sendMessage(ChatColor.GREEN + "ターゲットを設定しました: " + targetEntityByName.getName());
                    } else {
                        sender.sendMessage(ChatColor.RED + "エンティティが見つかりません: " + args[1]);
                        return false;
                    }
                }
                return true;

            // ==================== スキルトリガー ====================
            case "cast_at":
                if (targetPlayer == null || args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api cast_at <player> <skillId> [targetPlayer]");
                    return false;
                }
                String skillIdToCast = args[1];
                Entity castTarget = targetPlayer; // デフォルトは自分

                if (args.length >= 3) {
                    Player targetEntity = Bukkit.getPlayer(args[2]);
                    if (targetEntity != null) {
                        castTarget = targetEntity;
                    }
                }

                boolean castAtSuccess = api.castSkillAt(targetPlayer, skillIdToCast, castTarget);
                if (castAtSuccess) {
                    sender.sendMessage(ChatColor.GREEN + "スキルを発動しました: " + skillIdToCast);
                } else {
                    sender.sendMessage(ChatColor.RED + "スキルの発動に失敗しました");
                }
                return castAtSuccess;

            case "cast_with_cost":
                if (targetPlayer == null || args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api cast_with_cost <player> <skillId> <costType>");
                    return false;
                }
                String skillIdWithCost = args[1];
                SkillCostType costType = SkillCostType.fromId(args[2]);
                if (costType == null) {
                    sender.sendMessage(ChatColor.RED + "無効なコストタイプ: " + args[2] + " (mana/hp)");
                    return false;
                }
                boolean castWithCostSuccess = api.castSkillWithCostType(targetPlayer, skillIdWithCost, costType);
                if (castWithCostSuccess) {
                    sender.sendMessage(ChatColor.GREEN + "スキルを発動しました: " + skillIdWithCost);
                } else {
                    sender.sendMessage(ChatColor.RED + "スキルの発動に失敗しました");
                }
                return castWithCostSuccess;

            case "get_entities_in_area":
                if (targetPlayer == null || args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用法: rpg api get_entities_in_area <player> <shape> <params...>");
                    return false;
                }
                String shape = args[1];
                double[] params = new double[args.length - 2];
                try {
                    for (int i = 0; i < params.length; i++) {
                        params[i] = Double.parseDouble(args[i + 2]);
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "パラメータは数値で指定してください");
                    return false;
                }
                var entities = api.getEntitiesInArea(targetPlayer, shape, params);
                sender.sendMessage(ChatColor.GREEN + "範囲内のエンティティ: " + entities.size() + "件");
                for (Entity e : entities) {
                    String info = e.getType().name();
                    if (e instanceof Player) {
                        info = "Player:" + ((Player) e).getName();
                    }
                    sender.sendMessage(ChatColor.WHITE + "  - " + info);
                }
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "不明なアクション: " + action);
                sendHelp(sender);
                return false;
        }
    }

    /**
     * Stat文字列を解析します
     *
     * @param statStr ステータス文字列
     * @return Stat、解析できない場合はnull
     */
    private Stat parseStat(String statStr) {
        if (statStr == null) {
            return null;
        }

        // 短縮名で判定
        Stat stat = Stat.fromShortName(statStr);
        if (stat != null) {
            return stat;
        }

        // 表示名で判定
        stat = Stat.fromDisplayName(statStr);
        if (stat != null) {
            return stat;
        }

        // 列挙名で判定
        try {
            return Stat.valueOf(statStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * ヘルプメッセージを送信します
     *
     * @param sender 送信先
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "===== RPGPlugin API for SKript =====");
        sender.sendMessage(ChatColor.YELLOW + "使用法: /rpg api <action> <args...>");
        sender.sendMessage(ChatColor.YELLOW + "アクション:");
        sender.sendMessage(ChatColor.WHITE + "  レベル: get_level, set_level");
        sender.sendMessage(ChatColor.WHITE + "  ステータス: get_stat, set_stat");
        sender.sendMessage(ChatColor.WHITE + "  クラス: get_class, set_class, upgrade_class, can_upgrade_class");
        sender.sendMessage(ChatColor.WHITE + "  スキル: has_skill, unlock_skill, cast_skill, get_skill_level");
        sender.sendMessage(ChatColor.WHITE + "  スキル拡張: cast_at, cast_with_cost");
        sender.sendMessage(ChatColor.WHITE + "  ターゲット: get_target, set_target");
        sender.sendMessage(ChatColor.WHITE + "  範囲: get_entities_in_area");
        sender.sendMessage(ChatColor.WHITE + "  経済: get_gold, give_gold, take_gold, has_gold, transfer_gold");
        sender.sendMessage(ChatColor.WHITE + "  ダメージ: calculate_damage");
        sender.sendMessage(ChatColor.GOLD + "================================");
    }
}
