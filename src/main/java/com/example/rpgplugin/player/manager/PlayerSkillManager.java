package com.example.rpgplugin.player.manager;

import com.example.rpgplugin.skill.event.SkillEventListener;
import com.example.rpgplugin.skill.result.SkillExecutionResult;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * プレイヤースキルマネージャー
 *
 * <p>スキル操作と管理を担当します。</p>
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>Proxyパターン: SkillEventListenerへのアクセスを仲介</li>
 *   <li>オブザーバーパターン: スキルイベントを通知</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキル管理に特化</li>
 *   <li>依存性逆転: インターフェースへの依存</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 2.0.0
 */
public class PlayerSkillManager {

    private final UUID uuid;
    private final AtomicReference<SkillEventListener> skillEventListener;

    /**
     * コンストラクタ
     *
     * @param uuid プレイヤーUUID
     */
    public PlayerSkillManager(UUID uuid) {
        this.uuid = uuid;
        this.skillEventListener = new AtomicReference<>();
    }

    /**
     * UUIDを取得します
     *
     * @return プレイヤーUUID
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * スキルイベントリスナーを設定します
     *
     * @param listener スキルイベントリスナー
     */
    public void setSkillEventListener(SkillEventListener listener) {
        this.skillEventListener.set(listener);
    }

    /**
     * スキルイベントリスナーを取得します
     *
     * @return スキルイベントリスナー
     */
    public Optional<SkillEventListener> getSkillEventListener() {
        return Optional.ofNullable(skillEventListener.get());
    }

    // ==================== スキル照会 ====================

    /**
     * スキルを習得しているか確認します
     *
     * @param skillId スキルID
     * @return 習得している場合はtrue
     */
    public boolean hasSkill(String skillId) {
        SkillEventListener listener = skillEventListener.get();
        if (listener == null) {
            return false;
        }
        return listener.hasSkill(uuid, skillId);
    }

    /**
     * スキルレベルを取得します
     *
     * @param skillId スキルID
     * @return スキルレベル、習得していない場合は0
     */
    public int getSkillLevel(String skillId) {
        SkillEventListener listener = skillEventListener.get();
        if (listener == null) {
            return 0;
        }
        return listener.getSkillLevel(uuid, skillId);
    }

    // ==================== スキル操作 ====================

    /**
     * スキルを習得します
     *
     * @param skillId スキルID
     * @return 習得に成功した場合はtrue
     */
    public boolean acquireSkill(String skillId) {
        return acquireSkill(skillId, 1);
    }

    /**
     * スキルを習得します
     *
     * <p>イベントを発行し、リスナーが実際の習得処理を行います。</p>
     *
     * @param skillId スキルID
     * @param level 習得するレベル
     * @return 習得に成功した場合はtrue
     */
    public boolean acquireSkill(String skillId, int level) {
        SkillEventListener listener = skillEventListener.get();
        if (listener == null) {
            return false;
        }
        if (!listener.canAcquireSkill(uuid, skillId)) {
            return false;
        }
        // イベント発行
        int previousLevel = listener.getSkillLevel(uuid, skillId);
        if (previousLevel == 0) {
            listener.onSkillAcquired(uuid, skillId, level);
        } else {
            listener.onSkillLevelUp(uuid, skillId, level, previousLevel);
        }
        return true;
    }

    /**
     * スキルをレベルアップします
     *
     * @param skillId スキルID
     * @return レベルアップに成功した場合はtrue
     */
    public boolean upgradeSkill(String skillId) {
        SkillEventListener listener = skillEventListener.get();
        if (listener == null) {
            return false;
        }
        int currentLevel = listener.getSkillLevel(uuid, skillId);
        if (currentLevel == 0) {
            return false;
        }
        int newLevel = currentLevel + 1;
        listener.onSkillLevelUp(uuid, skillId, newLevel, currentLevel);
        return true;
    }

    /**
     * スキルを実行します
     *
     * @param skillId スキルID
     */
    public void executeSkill(String skillId) {
        SkillEventListener listener = skillEventListener.get();
        if (listener == null) {
            return;
        }
        int level = listener.getSkillLevel(uuid, skillId);
        listener.onSkillExecuted(uuid, skillId, level);
    }

    /**
     * スキルを実行します（Playerパラメータ付き）
     *
     * <p>Skript等の外部システムからスキルを実行する場合に使用します。</p>
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return 実行結果
     */
    public SkillExecutionResult executeSkill(Player player, String skillId) {
        SkillEventListener listener = skillEventListener.get();
        if (listener == null) {
            return SkillExecutionResult.failure("スキルシステムが利用できません");
        }
        return listener.executeSkill(player, skillId);
    }

    /**
     * スキルを実行します（設定付き）
     *
     * <p>Skript等の外部システムからスキルを実行する場合に使用します。</p>
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @param config 実行設定
     * @return 実行結果
     */
    public SkillExecutionResult executeSkill(
            Player player,
            String skillId,
            com.example.rpgplugin.skill.SkillExecutionConfig config) {
        SkillEventListener listener = skillEventListener.get();
        if (listener == null) {
            return SkillExecutionResult.failure("スキルシステムが利用できません");
        }
        return listener.executeSkill(player, skillId, config);
    }

    /**
     * スキル習得判定リクエスト
     *
     * @param skillId スキルID
     * @return 習得可能な場合true
     */
    public boolean canAcquireSkill(String skillId) {
        SkillEventListener listener = skillEventListener.get();
        if (listener == null) {
            return false;
        }
        return listener.canAcquireSkill(uuid, skillId);
    }
}
