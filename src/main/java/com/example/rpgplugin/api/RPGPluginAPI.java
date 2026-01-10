package com.example.rpgplugin.api;

import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
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

    /**
     * スキルポイントを取得します
     *
     * @param player プレイヤー
     * @return 現在のスキルポイント
     */
    int getSkillPoints(Player player);

    /**
     * スキルポイントを設定します
     *
     * @param player プレイヤー
     * @param points 設定するポイント数
     */
    void setSkillPoints(Player player, int points);

    /**
     * 属性ポイント（ステータス配分ポイント）を取得します
     *
     * @param player プレイヤー
     * @return 現在の属性ポイント
     */
    int getAttrPoints(Player player);

    /**
     * 属性ポイント（ステータス配分ポイント）を設定します
     *
     * @param player プレイヤー
     * @param points 設定するポイント数
     */
    void setAttrPoints(Player player, int points);

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

    // ==================== ターゲット管理 ====================

    /**
     * 最後にターゲットしたエンティティを取得します
     *
     * @param player プレイヤー
     * @return ターゲットエンティティ、存在しない場合はempty
     */
    Optional<Entity> getLastTargetedEntity(Player player);

    /**
     * ターゲットエンティティを設定します
     *
     * @param player プレイヤー
     * @param entity ターゲットとするエンティティ（nullでクリア）
     */
    void setTargetedEntity(Player player, Entity entity);

    // ==================== スキルトリガー ====================

    /**
     * 指定ターゲットでスキルを発動します
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @param target ターゲットエンティティ
     * @return 成功した場合はtrue
     */
    boolean castSkillAt(Player player, String skillId, Entity target);

    /**
     * 指定コストタイプでスキルを発動します
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @param costType コストタイプ（MP/HP）
     * @return 成功した場合はtrue
     */
    boolean castSkillWithCostType(Player player, String skillId, SkillCostType costType);

    /**
     * 範囲内のエンティティを取得します
     *
     * @param player プレイヤー（中心位置）
     * @param shape 形状（"circle", "sphere", "cube"）
     * @param params パラメータ（半径など）
     * @return 範囲内のエンティティコレクション
     */
    Collection<Entity> getEntitiesInArea(Player player, String shape, double... params);
}
