package com.example.rpgplugin.skill.component.trigger;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;

/**
 * 物理ダメージ受けトリガー
 * <p>物理ダメージを受けた時にスキルを発動します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class PhysicalTakenTrigger implements Trigger<EntityDamageByEntityEvent> {

    @Override
    public String getKey() {
        return "PHYSICAL_TAKEN";
    }

    @Override
    public Class<EntityDamageByEntityEvent> getEvent() {
        return EntityDamageByEntityEvent.class;
    }

    @Override
    public boolean shouldTrigger(EntityDamageByEntityEvent event, int level, TriggerSettings settings) {
        // ダメージを受けた側がプレイヤーの場合のみ発動
        return event.getEntity() instanceof LivingEntity;
    }

    @Override
    public void setValues(EntityDamageByEntityEvent event, Map<String, Object> data) {
        data.put("damage", event.getDamage());
        data.put("damage-cause", event.getCause().name());
        data.put("attacker", event.getDamager());
    }

    @Override
    public LivingEntity getCaster(EntityDamageByEntityEvent event) {
        return event.getEntity() instanceof LivingEntity
                ? (LivingEntity) event.getEntity()
                : null;
    }

    @Override
    public LivingEntity getTarget(EntityDamageByEntityEvent event, TriggerSettings settings) {
        String targetMode = settings.getString("target", "self");
        if ("attacker".equalsIgnoreCase(targetMode)) {
            return event.getDamager() instanceof LivingEntity
                    ? (LivingEntity) event.getDamager()
                    : null;
        }
        return getCaster(event);
    }
}
