package com.example.rpgplugin.api.bridge;

import com.example.rpgplugin.skill.Skill;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Objects;

/**
 * スキル発動カスタムイベント
 *
 * <p>SKript等の外部プラグインからスキル発動を検知するためのカスタムイベントです。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキルイベントの通知に専念</li>
 *   <li>Observer: イベント駆動で外部システムと連携</li>
 * </ul>
 *
 * <p>SKript使用例:</p>
 * <pre>{@code
 * on rpg skill cast:
 *     if skill-id is "fireball":
 *         send "ファイアボールが発動しました！" to player
 *         send "ターゲット: %target%" to player
 * }</pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SKriptSkillEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player caster;
    private final String skillId;
    private final Skill skill;
    private final int skillLevel;
    private final Entity target;
    private final boolean cancelled;

    /**
     * コンストラクタ
     *
     * @param caster 発動プレイヤー（null不可）
     * @param skillId スキルID（null不可）
     * @param skill スキルインスタンス（null不可）
     * @param skillLevel スキルレベル
     * @param target ターゲットエンティティ（存在しない場合はnull）
     * @throws IllegalArgumentException caster、skillId、またはskillがnullの場合
     */
    public SKriptSkillEvent(Player caster, String skillId, Skill skill, int skillLevel, Entity target) {
        this.caster = Objects.requireNonNull(caster, "Caster cannot be null");
        this.skillId = Objects.requireNonNull(skillId, "Skill ID cannot be null");
        this.skill = Objects.requireNonNull(skill, "Skill cannot be null");
        this.skillLevel = skillLevel;
        this.target = target;
        this.cancelled = false;
    }

    /**
     * 非同期版コンストラクタ
     *
     * @param caster 発動プレイヤー（null不可）
     * @param skillId スキルID（null不可）
     * @param skill スキルインスタンス（null不可）
     * @param skillLevel スキルレベル
     * @param target ターゲットエンティティ（存在しない場合はnull）
     * @param async 非同期かどうか
     * @throws IllegalArgumentException caster、skillId、またはskillがnullの場合
     */
    public SKriptSkillEvent(Player caster, String skillId, Skill skill, int skillLevel, Entity target, boolean async) {
        super(async);
        this.caster = Objects.requireNonNull(caster, "Caster cannot be null");
        this.skillId = Objects.requireNonNull(skillId, "Skill ID cannot be null");
        this.skill = Objects.requireNonNull(skill, "Skill cannot be null");
        this.skillLevel = skillLevel;
        this.target = target;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * HandlerListを取得します
     *
     * @return HandlerList
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    // ==================== Getters ====================

    /**
     * 発動プレイヤーを取得します
     *
     * @return 発動プレイヤー
     */
    public Player getCaster() {
        return caster;
    }

    /**
     * スキルIDを取得します
     *
     * @return スキルID
     */
    public String getSkillId() {
        return skillId;
    }

    /**
     * スキルインスタンスを取得します
     *
     * @return スキルインスタンス
     */
    public Skill getSkill() {
        return skill;
    }

    /**
     * スキルレベルを取得します
     *
     * @return スキルレベル
     */
    public int getSkillLevel() {
        return skillLevel;
    }

    /**
     * ターゲットエンティティを取得します
     *
     * @return ターゲットエンティティ、存在しない場合はnull
     */
    public Entity getTarget() {
        return target;
    }

    /**
     * キャンセルされたか取得します
     *
     * @return キャンセルされた場合はtrue
     */
    public boolean isCancelled() {
        return cancelled;
    }
}
