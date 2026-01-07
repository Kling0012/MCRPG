package com.example.rpgplugin.api;

import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

/**
 * RPGPlugin API メインインターフェース
 *
 * <p>SKript、Denizen等の外部プラグインからRPGシステムにアクセスするためのAPIです。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-O: 機能拡張に対して開かれている</li>
 *   <li>SOLID-I: インターフェース分離の原則（最小限のメソッド群）</li>
 *   <li>KISS: シンプルで直感的なAPI</li>
 * </ul>
 *
 * <p>使用例:</p>
 * <pre>{@code
 * RPGPluginAPI api = RPGPlugin.getInstance().getAPI();
 * int level = api.getLevel(player);
 * api.castSkill(player, "fireball");
 * }</pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 * @see RPGPlayer
 * @see Stat
 */
public interface RPGPluginAPI {

    // ==================== プレイヤーデータ取得 ====================

    /**
     * RPGPlayerを取得します
     *
     * @param player Bukkitプレイヤー
     * @return RPGPlayer、存在しない場合はempty
     */
    Optional<RPGPlayer> getRPGPlayer(Player player);

    /**
     * プレイヤーのバニラレベルを取得します
     *
     * @param player プレイヤー
     * @return バニラレベル
     */
    int getLevel(Player player);

    /**
     * プレイヤーのバニラレベルを設定します
     *
     * @param player プレイヤー
     * @param level レベル
     */
    void setLevel(Player player, int level);

    /**
     * ステータスを取得します
     *
     * @param player プレイヤー
     * @param stat ステータス種別
     * @return ステータス値
     */
    int getStat(Player player, Stat stat);

    /**
     * クラスIDを取得します
     *
     * @param player プレイヤー
     * @return クラスID、未設定の場合はnull
     */
    String getClassId(Player player);

    // ==================== ステータス操作 ====================

    /**
     * ステータスを設定します
     *
     * <p>基本ステータス値を設定します。</p>
     *
     * @param player プレイヤー
     * @param stat ステータス種別
     * @param baseValue 基本値
     */
    void setStat(Player player, Stat stat, int baseValue);

    /**
     * 手動配分ポイントを追加します
     *
     * @param player プレイヤー
     * @param points 追加するポイント数
     */
    void addStatPoints(Player player, int points);

    // ==================== クラス操作 ====================

    /**
     * プレイヤーのクラスを設定します
     *
     * @param player プレイヤー
     * @param classId クラスID
     * @return 成功した場合はtrue
     */
    boolean setClass(Player player, String classId);

    /**
     * クラスをランクアップします
     *
     * <p>現在のクラスの上位クラスがある場合にランクアップします。</p>
     *
     * @param player プレイヤー
     * @return 成功した場合はtrue
     */
    boolean upgradeClassRank(Player player);

    /**
     * クラスアップが可能かチェックします
     *
     * @param player プレイヤー
     * @return 可能な場合はtrue
     */
    boolean canUpgradeClass(Player player);

    // ==================== スキル操作 ====================

    /**
     * スキルを習得しているかチェックします
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return 習得している場合はtrue
     */
    boolean hasSkill(Player player, String skillId);

    /**
     * スキルを習得させます
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return 成功した場合はtrue
     */
    boolean unlockSkill(Player player, String skillId);

    /**
     * スキルを使用します
     *
     * <p>スキルを習得していない、またはクールダウン中の場合は失敗します。</p>
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return 成功した場合はtrue
     */
    boolean castSkill(Player player, String skillId);

    /**
     * スキルレベルを取得します
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return スキルレベル（習得していない場合は0）
     */
    int getSkillLevel(Player player, String skillId);

    /**
     * プレイヤーの習得済みスキル一覧を取得します
     *
     * @param player プレイヤー
     * @return 習得済みスキルのマップ（スキルID -> レベル）
     */
    java.util.Map<String, Integer> getAcquiredSkills(Player player);

    /**
     * 指定されたクラスで使用可能なスキル一覧を取得します
     *
     * @param classId クラスID
     * @return 使用可能なスキルリスト
     */
    List<Skill> getSkillsForClass(String classId);

    // ==================== 経済操作 ====================

    /**
     * ゴールド残高を取得します
     *
     * @param player プレイヤー
     * @return ゴールド残高
     */
    double getGoldBalance(Player player);

    /**
     * ゴールドを入金します
     *
     * @param player プレイヤー
     * @param amount 入金額
     * @return 成功した場合はtrue
     */
    boolean depositGold(Player player, double amount);

    /**
     * ゴールドを出金します
     *
     * <p>残高が足りない場合は失敗します。</p>
     *
     * @param player プレイヤー
     * @param amount 出金額
     * @return 成功した場合はtrue
     */
    boolean withdrawGold(Player player, double amount);

    /**
     * ゴールド残高が足りているかチェックします
     *
     * @param player プレイヤー
     * @param amount 必要な金額
     * @return 足りている場合はtrue
     */
    boolean hasEnoughGold(Player player, double amount);

    /**
     * プレイヤー間でゴールドを転送します
     *
     * @param from 送金元プレイヤー
     * @param to 送金先プレイヤー
     * @param amount 送金額
     * @return 成功した場合はtrue
     */
    boolean transferGold(Player from, Player to, double amount);

    // ==================== ダメージ計算 ====================

    /**
     * ダメージを計算します
     *
     * <p>攻撃者のステータスと武器に基づいてダメージを計算します。</p>
     *
     * @param attacker 攻撃者
     * @param target ターゲット
     * @return 計算されたダメージ
     */
    double calculateDamage(Player attacker, Entity target);

    /**
     * ステータス修正を適用したダメージを計算します
     *
     * <p>基本ダメージにステータス修正を適用します。</p>
     *
     * @param player プレイヤー
     * @param baseDamage 基本ダメージ
     * @param stat 使用するステータス
     * @return 修正後のダメージ
     */
    double applyStatModifiers(Player player, double baseDamage, Stat stat);
}
