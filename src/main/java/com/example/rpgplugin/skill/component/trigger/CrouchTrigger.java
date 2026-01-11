package com.example.rpgplugin.skill.component.trigger;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Map;

/**
 * スニークトリガー
 * <p>スニーク開始/終了時にスキルを発動します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class CrouchTrigger implements Trigger<PlayerToggleSneakEvent> {

    @Override
    public String getKey() {
        return "CROUCH";
    }

    @Override
    public Class<PlayerToggleSneakEvent> getEvent() {
        return PlayerToggleSneakEvent.class;
    }

    @Override
    public boolean shouldTrigger(PlayerToggleSneakEvent event, int level, TriggerSettings settings) {
        String type = settings.getString("type", "start crouching").toLowerCase();

        // スニークを開始した時か、終了した時か、両方か
        return type.equals("both")
                || (type.equals("start crouching") && event.isSneaking())
                || (type.equals("stop crouching") && !event.isSneaking());
    }

    @Override
    public void setValues(PlayerToggleSneakEvent event, Map<String, Object> data) {
        // 値を設定する必要なし
    }

    @Override
    public LivingEntity getCaster(PlayerToggleSneakEvent event) {
        return event.getPlayer();
    }

    @Override
    public LivingEntity getTarget(PlayerToggleSneakEvent event, TriggerSettings settings) {
        // キャスター自身がターゲット
        return event.getPlayer();
    }
}
