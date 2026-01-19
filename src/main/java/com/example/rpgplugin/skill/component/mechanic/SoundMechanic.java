package com.example.rpgplugin.skill.component.mechanic;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

/**
 * サウンドメカニック
 * <p>ターゲットの位置でサウンドを再生します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SoundMechanic extends MechanicComponent {

    private static final String SOUND = "sound";
    private static final String VOLUME = "volume";
    private static final String PITCH = "pitch";

    /**
     * コンストラクタ
     */
    public SoundMechanic() {
        super("sound");
    }

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        ComponentSettings settings = getSettings();
        if (settings == null) {
            return false;
        }

        String soundName = settings.getString(SOUND, "").toUpperCase().replace(' ', '_');
        if (soundName.isEmpty()) {
            return false;
        }

        try {
            Sound sound = Sound.valueOf(soundName);
            float volume = (float) settings.getDouble(VOLUME, 1.0);
            float pitch = (float) settings.getDouble(PITCH, 1.0);

            volume = Math.max(0, volume);
            pitch = Math.min(2, Math.max(0.5f, pitch));

            target.getWorld().playSound(target.getLocation(), sound, volume, pitch);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
