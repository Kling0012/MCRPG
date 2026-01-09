package com.example.rpgplugin.api.bridge;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.RPGPluginAPI;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;

/**
 * Denizenブリッジクラス
 *
 * <p>DenizenからRPGプラグインのAPIにアクセスするためのブリッジです。</p>
 *
 * <p>Denizen連携方法:</p>
 * <ul>
 *   <li>タグ置換: {@code <player.tag[rpg.level]>}</li>
 *   <li>コマンド経由: {@code /rpg api <action> <args...>}</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: Denizen連携に専念</li>
 *   <li>DRY: 既存APIを活用</li>
 *   <li>KISS: シンプルなタグベースAPI</li>
 * </ul>
 *
 * <p>使用例（Denizen）:</p>
 * <pre>{@code
 * my_script:
 *   type: task
 *   script:
 *     - define level <player.tag[rpg.level]>
 *     - narrate "あなたのLV: %level%"
 *     - execute as_server "rpg api upgrade_class %player%"
 * }</pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class DenizenBridge {

    private final RPGPlugin plugin;
    private final RPGPluginAPI api;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public DenizenBridge(RPGPlugin plugin) {
        this.plugin = plugin;
        this.api = plugin.getAPI();
    }

    // ==================== タグ置換用メソッド ====================

    /**
     * レベルを取得します
     *
     * <p>Denizenタグ: {@code <player.tag[rpg.level]>}</p>
     *
     * @param player プレイヤー（null不可）
     * @return レベル
     * @throws IllegalArgumentException playerがnullの場合
     */
    public int getLevel(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return api.getLevel(player);
    }

    /**
     * ステータスを取得します
     *
     * <p>Denizenタグ: {@code <player.tag[rpg.stat[<stat>]>}</p>
     *
     * <p>ステータス指定: STR, INT, SPI, VIT, DEX</p>
     *
     * @param player プレイヤー（null不可）
     * @param statName ステータス名
     * @return ステータス値
     * @throws IllegalArgumentException playerがnullの場合
     */
    public int getStat(Player player, String statName) {
        Objects.requireNonNull(player, "Player cannot be null");
        Stat stat = parseStat(statName);
        if (stat == null) {
            return 0;
        }
        return api.getStat(player, stat);
    }

    /**
     * 全ステータスを取得します
     *
     * <p>Denizenタグ: {@code <player.tag[rpg.stats]>}</p>
     *
     * @param player プレイヤー（null不可）
     * @return ステータスマップ（Stat -> 値）
     * @throws IllegalArgumentException playerがnullの場合
     */
    public Map<Stat, Integer> getAllStats(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        var rpgPlayerOpt = api.getRPGPlayer(player);
        if (!rpgPlayerOpt.isPresent()) {
            return Map.of();
        }
        RPGPlayer rpgPlayer = rpgPlayerOpt.get();
        return rpgPlayer.getStatManager().getAllFinalStats();
    }

    /**
     * クラスIDを取得します
     *
     * <p>Denizenタグ: {@code <player.tag[rpg.class]>}</p>
     *
     * @param player プレイヤー（null不可）
     * @return クラスID、未設定の場合は"none"
     * @throws IllegalArgumentException playerがnullの場合
     */
    public String getClassId(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        String classId = api.getClassId(player);
        return classId != null ? classId : "none";
    }

    /**
     * クラスランクを取得します
     *
     * <p>Denizenタグ: {@code <player.tag[rpg.class_rank]>}</p>
     *
     * @param player プレイヤー（null不可）
     * @return クラスランク
     * @throws IllegalArgumentException playerがnullの場合
     */
    public int getClassRank(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        var rpgPlayerOpt = api.getRPGPlayer(player);
        if (!rpgPlayerOpt.isPresent()) {
            return 0;
        }
        return rpgPlayerOpt.get().getClassRank();
    }

    /**
     * スキル習得確認をします
     *
     * <p>Denizenタグ: {@code <player.tag[rpg.has_skill[<skillId>]>}</p>
     *
     * @param player プレイヤー（null不可）
     * @param skillId スキルID
     * @return 習得している場合は"true"、それ以外は"false"
     * @throws IllegalArgumentException playerがnullの場合
     */
    public String hasSkill(Player player, String skillId) {
        Objects.requireNonNull(player, "Player cannot be null");
        return String.valueOf(api.hasSkill(player, skillId));
    }

    /**
     * スキルレベルを取得します
     *
     * <p>Denizenタグ: {@code <player.tag[rpg.skill_level[<skillId>]>}</p>
     *
     * @param player プレイヤー（null不可）
     * @param skillId スキルID
     * @return スキルレベル
     * @throws IllegalArgumentException playerがnullの場合
     */
    public int getSkillLevel(Player player, String skillId) {
        Objects.requireNonNull(player, "Player cannot be null");
        return api.getSkillLevel(player, skillId);
    }

    /**
     * 習得済みスキル数を取得します
     *
     * <p>Denizenタグ: {@code <player.tag[rpg.skill_count]>}</p>
     *
     * @param player プレイヤー（null不可）
     * @return 習得済みスキル数
     * @throws IllegalArgumentException playerがnullの場合
     */
    public int getSkillCount(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return api.getAcquiredSkills(player).size();
    }

    /**
     * ゴールド残高を取得します
     *
     * <p>Denizenタグ: {@code <player.tag[rpg.gold]>}</p>
     *
     * @param player プレイヤー（null不可）
     * @return ゴールド残高
     * @throws IllegalArgumentException playerがnullの場合
     */
    public double getGoldBalance(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return api.getGoldBalance(player);
    }

    /**
     * ゴールド所持確認をします
     *
     * <p>Denizenタグ: {@code <player.tag[rpg.has_gold[<amount>]>}</p>
     *
     * @param player プレイヤー（null不可）
     * @param amount 金額
     * @return 足りている場合は"true"、それ以外は"false"
     * @throws IllegalArgumentException playerがnullの場合
     */
    public String hasEnoughGold(Player player, double amount) {
        Objects.requireNonNull(player, "Player cannot be null");
        return String.valueOf(api.hasEnoughGold(player, amount));
    }

    /**
     * 利用可能なステータスポイントを取得します
     *
     * <p>Denizenタグ: {@code <player.tag[rpg.available_points]>}</p>
     *
     * @param player プレイヤー（null不可）
     * @return 利用可能なポイント数
     * @throws IllegalArgumentException playerがnullの場合
     */
    public int getAvailablePoints(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        var rpgPlayerOpt = api.getRPGPlayer(player);
        if (!rpgPlayerOpt.isPresent()) {
            return 0;
        }
        return rpgPlayerOpt.get().getAvailablePoints();
    }

    // ==================== アクション実行用メソッド ====================

    /**
     * レベルを設定します
     *
     * @param player プレイヤー（null不可）
     * @param level レベル
     * @return 成功した場合はtrue
     * @throws IllegalArgumentException playerがnullの場合
     */
    public boolean setLevel(Player player, int level) {
        Objects.requireNonNull(player, "Player cannot be null");
        api.setLevel(player, level);
        return true;
    }

    /**
     * ステータスを設定します
     *
     * @param player プレイヤー（null不可）
     * @param statName ステータス名
     * @param value 値
     * @return 成功した場合はtrue
     * @throws IllegalArgumentException playerがnullの場合
     */
    public boolean setStat(Player player, String statName, int value) {
        Objects.requireNonNull(player, "Player cannot be null");
        Stat stat = parseStat(statName);
        if (stat == null) {
            return false;
        }
        api.setStat(player, stat, value);
        return true;
    }

    /**
     * ステータスポイントを追加します
     *
     * @param player プレイヤー（null不可）
     * @param points 追加するポイント数
     * @return 成功した場合はtrue
     * @throws IllegalArgumentException playerがnullの場合
     */
    public boolean addStatPoints(Player player, int points) {
        Objects.requireNonNull(player, "Player cannot be null");
        api.addStatPoints(player, points);
        return true;
    }

    /**
     * クラスを設定します
     *
     * @param player プレイヤー（null不可）
     * @param classId クラスID
     * @return 成功した場合はtrue
     * @throws IllegalArgumentException playerがnullの場合
     */
    public boolean setClass(Player player, String classId) {
        Objects.requireNonNull(player, "Player cannot be null");
        return api.setClass(player, classId);
    }

    /**
     * クラスをランクアップします
     *
     * @param player プレイヤー（null不可）
     * @return 成功した場合はtrue
     * @throws IllegalArgumentException playerがnullの場合
     */
    public boolean upgradeClassRank(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return api.upgradeClassRank(player);
    }

    /**
     * クラスアップが可能かチェックします
     *
     * @param player プレイヤー（null不可）
     * @return 可能な場合はtrue
     * @throws IllegalArgumentException playerがnullの場合
     */
    public boolean canUpgradeClass(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return api.canUpgradeClass(player);
    }

    /**
     * スキルを習得させます
     *
     * @param player プレイヤー（null不可）
     * @param skillId スキルID
     * @return 成功した場合はtrue
     * @throws IllegalArgumentException playerがnullの場合
     */
    public boolean unlockSkill(Player player, String skillId) {
        Objects.requireNonNull(player, "Player cannot be null");
        return api.unlockSkill(player, skillId);
    }

    /**
     * スキルを使用します
     *
     * @param player プレイヤー（null不可）
     * @param skillId スキルID
     * @return 成功した場合はtrue
     * @throws IllegalArgumentException playerがnullの場合
     */
    public boolean castSkill(Player player, String skillId) {
        Objects.requireNonNull(player, "Player cannot be null");
        return api.castSkill(player, skillId);
    }

    /**
     * ゴールドを入金します
     *
     * @param player プレイヤー（null不可）
     * @param amount 入金額
     * @return 成功した場合はtrue
     * @throws IllegalArgumentException playerがnullの場合
     */
    public boolean depositGold(Player player, double amount) {
        Objects.requireNonNull(player, "Player cannot be null");
        return api.depositGold(player, amount);
    }

    /**
     * ゴールドを出金します
     *
     * @param player プレイヤー（null不可）
     * @param amount 出金額
     * @return 成功した場合はtrue
     * @throws IllegalArgumentException playerがnullの場合
     */
    public boolean withdrawGold(Player player, double amount) {
        Objects.requireNonNull(player, "Player cannot be null");
        return api.withdrawGold(player, amount);
    }

    /**
     * プレイヤー間でゴールドを転送します
     *
     * @param from 送金元プレイヤー（null不可）
     * @param to 送金先プレイヤー（null不可）
     * @param amount 送金額
     * @return 成功した場合はtrue
     * @throws IllegalArgumentException fromまたはtoがnullの場合
     */
    public boolean transferGold(Player from, Player to, double amount) {
        Objects.requireNonNull(from, "Source player cannot be null");
        Objects.requireNonNull(to, "Target player cannot be null");
        return api.transferGold(from, to, amount);
    }

    /**
     * ダメージを計算します
     *
     * @param attacker 攻撃者（null不可）
     * @param targetName ターゲット名
     * @return 計算されたダメージ
     * @throws IllegalArgumentException attackerがnullの場合
     */
    public double calculateDamageByName(Player attacker, String targetName) {
        Objects.requireNonNull(attacker, "Attacker cannot be null");
        var target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            return 0.0;
        }
        return api.calculateDamage(attacker, target);
    }

    // ==================== ユーティリティ ====================

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
     * 利用可能なタグ一覧を取得します
     *
     * @return タグ一覧の説明
     */
    public static String getAvailableTags() {
        return """
                ===== Denizen Tags for RPGPlugin =====

                Player Tags:
                - <player.tag[rpg.level]> - レベル取得
                - <player.tag[rpg.stat[<stat>]]> - ステータス取得 (STR/INT/SPI/VIT/DEX)
                - <player.tag[rpg.stats]> - 全ステータス取得
                - <player.tag[rpg.class]> - クラスID取得
                - <player.tag[rpg.class_rank]> - クラスランク取得
                - <player.tag[rpg.has_skill[<skillId>]]> - スキル習得確認
                - <player.tag[rpg.skill_level[<skillId>]]> - スキルレベル取得
                - <player.tag[rpg.skill_count]> - 習得済みスキル数
                - <player.tag[rpg.gold]> - ゴールド残高取得
                - <player.tag[rpg.has_gold[<amount>]]> - ゴールド所持確認
                - <player.tag[rpg.available_points]> - 利用可能ステータスポイント

                Commands:
                - execute as_server "rpg api <action> <args...>"

                Example:
                my_script:
                  type: task
                  script:
                    - define level <player.tag[rpg.level]>
                    - narrate "あなたのLV: %level%"
                    - execute as_server "rpg api give_gold %player% 100"
                """;
    }
}
