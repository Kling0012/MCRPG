package com.example.rpgplugin.skill.component.trigger;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import java.util.Map;

/**
 * キャストトリガー
 * <p>スキル使用時に即時実行されるトリガーです（始動点）</p>
 * <p>イベントを待機せず、スキル発動時に直ちに子コンポーネントを実行します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class CastTrigger implements Trigger<Event> {

    @Override
    public String getKey() {
        return "CAST";
    }

    @Override
    public Class<Event> getEvent() {
        return Event.class; // ダミー: イベントを使用しない
    }

    @Override
    public boolean shouldTrigger(Event event, int level, TriggerSettings settings) {
        // 常に発動（スキル使用時）
        return true;
    }

    @Override
    public void setValues(Event event, Map<String, Object> data) {
        // 設定する値はない
    }

    @Override
    public LivingEntity getCaster(Event event) {
        // 呼び出し元から設定される
        return null;
    }

    @Override
    public LivingEntity getTarget(Event event, TriggerSettings settings) {
        // ターゲットは呼び出し元から設定される
        return null;
    }

    /**
     * CASTトリガーはイベントリスナーを必要としない
     *
     * @return 常にtrue（イベント不要）
     */
    public boolean isEventless() {
        return true;
    }
}
