package com.example.rpgplugin.skill.event;

import com.example.rpgplugin.skill.result.SkillExecutionResult;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * スキルイベントリスナーインターフェース
 *
 * <p>スキル関連のイベントを通知するためのリスナーです。</p>
 * <p>このインターフェースにより、RPGPlayerとSkillManagerの循環依存を解消します。</p>
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>オブザーバーパターン: スキルイベントを通知</li>
 *   <li>依存性逆転: インターフェースへの依存で結合度を低下</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public interface SkillEventListener {

    /**
     * スキル習得イベント
     *
     * @param playerUuid プレイヤーUUID
     * @param skillId スキルID
     * @param level スキルレベル
     */
    void onSkillAcquired(UUID playerUuid, String skillId, int level);

    /**
     * スキルレベルアップイベント
     *
     * @param playerUuid プレイヤーUUID
     * @param skillId スキルID
     * @param newLevel 新しいレベル
     * @param previousLevel 前のレベル
     */
    void onSkillLevelUp(UUID playerUuid, String skillId, int newLevel, int previousLevel);

    /**
     * スキル実行イベント
     *
     * @param playerUuid プレイヤーUUID
     * @param skillId スキルID
     * @param level スキルレベル
     */
    void onSkillExecuted(UUID playerUuid, String skillId, int level);

    /**
     * スキルを実行します
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return 実行結果
     */
    SkillExecutionResult executeSkill(Player player, String skillId);

    /**
     * スキルを実行します（設定付き）
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @param config 実行設定
     * @return 実行結果
     */
    SkillExecutionResult executeSkill(Player player, String skillId, com.example.rpgplugin.skill.SkillExecutionConfig config);

    /**
     * スキル習得判定リクエスト
     *
     * @param playerUuid プレイヤーUUID
     * @param skillId スキルID
     * @return 習得可能な場合true
     */
    boolean canAcquireSkill(UUID playerUuid, String skillId);

    /**
     * スキルレベル判定リクエスト
     *
     * @param playerUuid プレイヤーUUID
     * @param skillId スキルID
     * @return 現在のレベル（習得していない場合は0）
     */
    int getSkillLevel(UUID playerUuid, String skillId);

    /**
     * スキル習得判定リクエスト
     *
     * @param playerUuid プレイヤーUUID
     * @param skillId スキルID
     * @return 習得している場合true
     */
    boolean hasSkill(UUID playerUuid, String skillId);
}
