package com.example.rpgplugin.skill.component.trigger;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.Map;

/**
 * 発射トリガー
 * <p>投射物を発射した時にスキルを発動します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class LaunchTrigger implements Trigger<ProjectileLaunchEvent> {

    @Override
    public String getKey() {
        return "LAUNCH";
    }

    @Override
    public Class<ProjectileLaunchEvent> getEvent() {
        return ProjectileLaunchEvent.class;
    }

    @Override
    public boolean shouldTrigger(ProjectileLaunchEvent event, int level, TriggerSettings settings) {
        // 発射者がプレイヤーの場合のみ発動
        return event.getEntity().getShooter() instanceof LivingEntity;
    }

    @Override
    public void setValues(ProjectileLaunchEvent event, Map<String, Object> data) {
        data.put("projectile-type", event.getEntityType().name());
        data.put("velocity", event.getEntity().getVelocity().length());
    }

    @Override
    public LivingEntity getCaster(ProjectileLaunchEvent event) {
        return event.getEntity().getShooter() instanceof LivingEntity
                ? (LivingEntity) event.getEntity().getShooter()
                : null;
    }

    @Override
    public LivingEntity getTarget(ProjectileLaunchEvent event, TriggerSettings settings) {
        return getCaster(event);
    }
}
