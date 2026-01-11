package com.example.rpgplugin.skill.component.mechanic;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * チャネリングメカニック
 * <p>詠唱時間中に中断された場合の処理を行います</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ChannelMechanic extends MechanicComponent {

    private static final String DURATION = "duration";
    private static final String TICKS = "ticks";

    private static final Map<UUID, ChannelTask> activeChannels = new HashMap<>();
    private com.example.rpgplugin.RPGPlugin plugin;

    /**
     * コンストラクタ
     */
    public ChannelMechanic() {
        super("channel");
    }

    /**
     * プラグインを設定します
     *
     * @param plugin プラグインインスタンス
     */
    public void setPlugin(com.example.rpgplugin.RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        ComponentSettings settings = getSettings();
        if (settings == null || plugin == null) {
            return false;
        }

        double duration = settings.getDouble(DURATION, 2.0);
        boolean useTicks = settings.getBoolean(TICKS, false);

        long durationTicks = useTicks ? (long) duration : (long) (duration * 20);
        UUID targetId = target.getUniqueId();

        // 既存のチャネリングをキャンセル
        cancelChannel(targetId);

        // 新しいチャネリングを開始
        ChannelTask task = new ChannelTask(caster, level, target, durationTicks);
        activeChannels.put(targetId, task);
        task.runTaskLater(plugin, durationTicks);

        return true;
    }

    /**
     * チャネリングをキャンセルします
     *
     * @param targetId ターゲットのUUID
     */
    public static void cancelChannel(UUID targetId) {
        ChannelTask task = activeChannels.remove(targetId);
        if (task != null) {
            task.cancel();
            // 中断時の子コンポーネントを実行しない
        }
    }

    /**
     * チャネリングタスク
     */
    private class ChannelTask extends BukkitRunnable {
        private final LivingEntity caster;
        private final int level;
        private final LivingEntity target;

        ChannelTask(LivingEntity caster, int level, LivingEntity target, long durationTicks) {
            this.caster = caster;
            this.level = level;
            this.target = target;
        }

        @Override
        public void run() {
            activeChannels.remove(target.getUniqueId());
            // チャネリング完了時に子コンポーネントを実行
            executeChildren(caster, level, List.of(target));
        }
    }
}
