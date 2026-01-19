package com.example.rpgplugin.skill.component.trigger;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;

/**
 * 着地トリガー
 * <p>プレイヤーが落下ダメージを受けた時にスキルを発動します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class LandTrigger implements Trigger<EntityDamageEvent> {

    @Override
    public String getKey() {
        return "LAND";
    }

    @Override
    public Class<EntityDamageEvent> getEvent() {
        return EntityDamageEvent.class;
    }

    @Override
    public boolean shouldTrigger(EntityDamageEvent event, int level, TriggerSettings settings) {
        // 落下ダメージのみ
        return event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL;
    }

    @Override
    public void setValues(EntityDamageEvent event, Map<String, Object> data) {
        data.put("damage", event.getDamage());
    }

    @Override
    public LivingEntity getCaster(EntityDamageEvent event) {
        return (LivingEntity) event.getEntity();
    }

    @Override
    public LivingEntity getTarget(EntityDamageEvent event, TriggerSettings settings) {
        // キャスター自身がターゲット
        return (LivingEntity) event.getEntity();
    }
}
