package com.example.rpgplugin.skill.component.trigger;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import java.util.Map;

/**
 * トリガーインターフェース
 * <p>スキルを自動発動させるイベントを定義します</p>
 *
 * <p>スキル発動後にリスナーを開始する設計です。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public interface Trigger<E extends Event> {

    /**
     * トリガーの一意識別キーを取得します
     *
     * @return トリガーキー（例: "CROUCH", "LAND", "DAMAGE"）
     */
    String getKey();

    /**
     * 対応するBukkitイベントクラスを取得します
     *
     * @return イベントクラス
     */
    Class<E> getEvent();

    /**
     * スキルを発動するべきか判定します
     *
     * @param event   イベント
     * @param level   スキルレベル
     * @param settings トリガー設定
     * @return 発動する場合はtrue
     */
    boolean shouldTrigger(E event, int level, TriggerSettings settings);

    /**
     * イベントからデータを抽出し、キャストデータに設定します
     *
     * @param event イベント
     * @param data   キャストデータマップ
     */
    void setValues(E event, Map<String, Object> data);

    /**
     * 発動者を取得します
     *
     * @param event イベント
     * @return 発動者
     */
    LivingEntity getCaster(E event);

    /**
     * ターゲットを取得します
     *
     * @param event    イベント
     * @param settings トリガー設定
     * @return ターゲット
     */
    LivingEntity getTarget(E event, TriggerSettings settings);
}
