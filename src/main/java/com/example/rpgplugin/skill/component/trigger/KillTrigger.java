package com.example.rpgplugin.skill.component.trigger;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Map;

/**
 * キルトリガー
 * <p>エンティティを倒した時にスキルを発動します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class KillTrigger implements Trigger<EntityDeathEvent> {

    @Override
    public String getKey() {
        return "KILL";
    }

    @Override
    public Class<EntityDeathEvent> getEvent() {
        return EntityDeathEvent.class;
    }

    @Override
    public boolean shouldTrigger(EntityDeathEvent event, int level, TriggerSettings settings) {
        // キラーが存在する場合のみ発動
        return event.getEntity().getKiller() != null;
    }

    @Override
    public void setValues(EntityDeathEvent event, Map<String, Object> data) {
        data.put("victim", event.getEntity());
        data.put("victim-type", event.getEntityType().name());
    }

    @Override
    public LivingEntity getCaster(EntityDeathEvent event) {
        return event.getEntity().getKiller();
    }

    @Override
    public LivingEntity getTarget(EntityDeathEvent event, TriggerSettings settings) {
        return event.getEntity().getKiller();
    }
}
