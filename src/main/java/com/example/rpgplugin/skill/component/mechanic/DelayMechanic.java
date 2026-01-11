package com.example.rpgplugin.skill.component.mechanic;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * 遅延メカニック
 * <p>子コンポーネントの実行を遅延します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class DelayMechanic extends MechanicComponent {

    private static final String DELAY = "delay";
    private static final String TICKS = "ticks";

    private com.example.rpgplugin.RPGPlugin plugin;

    /**
     * コンストラクタ
     */
    public DelayMechanic() {
        super("delay");
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

        double delay = settings.getDouble(DELAY, 1.0);
        boolean useTicks = settings.getBoolean(TICKS, false);

        long delayTicks = useTicks ? (long) delay : (long) (delay * 20);

        // 子コンポーネントを遅延実行
        new BukkitRunnable() {
            @Override
            public void run() {
                executeChildren(caster, level, List.of(target));
            }
        }.runTaskLater(plugin, delayTicks);

        return true;
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        // 遅延メカニックは直ちにtrueを返し、子コンポーネントを非同期実行
        for (LivingEntity target : targets) {
            apply(caster, level, target);
        }
        return !targets.isEmpty();
    }
}
