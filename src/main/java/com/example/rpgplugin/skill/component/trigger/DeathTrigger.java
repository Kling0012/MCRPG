package com.example.rpgplugin.skill.component.trigger;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Map;

/**
 * 死亡トリガー
 * <p>エンティティが死亡した時にスキルを発動します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class DeathTrigger implements Trigger<EntityDeathEvent> {

    @Override
    public String getKey() {
        return "DEATH";
    }

    @Override
    public Class<EntityDeathEvent> getEvent() {
        return EntityDeathEvent.class;
    }

    @Override
    public boolean shouldTrigger(EntityDeathEvent event, int level, TriggerSettings settings) {
        boolean targetKiller = settings.getBoolean("killer", false);
        return !targetKiller || event.getEntity().getKiller() != null;
    }

    @Override
    public void setValues(EntityDeathEvent event, Map<String, Object> data) {
        // 値を設定する必要なし
    }

    @Override
    public LivingEntity getCaster(EntityDeathEvent event) {
        return event.getEntity();
    }

    @Override
    public LivingEntity getTarget(EntityDeathEvent event, TriggerSettings settings) {
        boolean targetKiller = settings.getBoolean("killer", false);
        return targetKiller ? event.getEntity().getKiller() : event.getEntity();
    }
}
