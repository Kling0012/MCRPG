package com.example.rpgplugin.skill.component.trigger;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;

/**
 * 物理ダメージ与えトリガー
 * <p>物理ダメージを与えた時にスキルを発動します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class PhysicalDealtTrigger implements Trigger<EntityDamageByEntityEvent> {

    @Override
    public String getKey() {
        return "PHYSICAL_DEALT";
    }

    @Override
    public Class<EntityDamageByEntityEvent> getEvent() {
        return EntityDamageByEntityEvent.class;
    }

    @Override
    public boolean shouldTrigger(EntityDamageByEntityEvent event, int level, TriggerSettings settings) {
        // ダメージを与えた側がプレイヤーの場合のみ発動
        return event.getDamager() instanceof LivingEntity
                && !(event.getDamager() instanceof org.bukkit.entity.Projectile);
    }

    @Override
    public void setValues(EntityDamageByEntityEvent event, Map<String, Object> data) {
        data.put("damage", event.getDamage());
        data.put("damage-cause", event.getCause().name());
    }

    @Override
    public LivingEntity getCaster(EntityDamageByEntityEvent event) {
        return event.getDamager() instanceof LivingEntity
                ? (LivingEntity) event.getDamager()
                : null;
    }

    @Override
    public LivingEntity getTarget(EntityDamageByEntityEvent event, TriggerSettings settings) {
        String targetMode = settings.getString("target", "self");
        if ("victim".equalsIgnoreCase(targetMode)) {
            return event.getEntity() instanceof LivingEntity
                    ? (LivingEntity) event.getEntity()
                    : null;
        }
        return getCaster(event);
    }
}
