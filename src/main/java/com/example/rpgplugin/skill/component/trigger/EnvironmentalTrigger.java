package com.example.rpgplugin.skill.component.trigger;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;

/**
 * 環境ダメージトリガー
 * <p>環境ダメージ（落下、炎、溺など）を受けた時にスキルを発動します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class EnvironmentalTrigger implements Trigger<EntityDamageEvent> {

    @Override
    public String getKey() {
        return "ENVIRONMENTAL";
    }

    @Override
    public Class<EntityDamageEvent> getEvent() {
        return EntityDamageEvent.class;
    }

    @Override
    public boolean shouldTrigger(EntityDamageEvent event, int level, TriggerSettings settings) {
        // エンティティダメージのみ（他のエンティティからの攻撃除外）
        if (event instanceof EntityDamageByEntityEvent) {
            return false;
        }

        // 特定のダメージ原因のみ発動する設定
        String type = settings.getString("type", "any").toLowerCase();
        if (!"any".equals(type)) {
            return event.getCause().name().toLowerCase().contains(type);
        }

        return event.getEntity() instanceof LivingEntity;
    }

    @Override
    public void setValues(EntityDamageEvent event, Map<String, Object> data) {
        data.put("damage", event.getDamage());
        data.put("cause", event.getCause().name());
    }

    @Override
    public LivingEntity getCaster(EntityDamageEvent event) {
        return event.getEntity() instanceof LivingEntity
                ? (LivingEntity) event.getEntity()
                : null;
    }

    @Override
    public LivingEntity getTarget(EntityDamageEvent event, TriggerSettings settings) {
        return getCaster(event);
    }
}
