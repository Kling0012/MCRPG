package com.example.rpgplugin.skill.component.trigger;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.skill.component.SkillEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.*;

/**
 * トリガーマネージャー
 * <p>スキル発動後にトリガーリスナーを登録・管理します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class TriggerManager implements Listener {

    private static TriggerManager instance;

    private final RPGPlugin plugin;
    private final Map<String, SkillEffect> skillEffects = new HashMap<>();
    private final Map<Integer, Map<String, ActiveTriggerData>> activeTriggers = new HashMap<>();

    private TriggerManager(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * インスタンスを取得します
     *
     * @return トリガーマネージャー
     */
    public static TriggerManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TriggerManager not initialized");
        }
        return instance;
    }

    /**
     * 初期化します
     *
     * @param plugin プラグイン
     * @return トリガーマネージャー
     */
    public static TriggerManager initialize(RPGPlugin plugin) {
        if (instance == null) {
            instance = new TriggerManager(plugin);
            plugin.getServer().getPluginManager().registerEvents(instance, plugin);
        }
        return instance;
    }

    /**
     * スキル効果を登録します
     *
     * @param skillId スキルID
     * @param effect  スキル効果
     */
    public void registerSkill(String skillId, SkillEffect effect) {
        skillEffects.put(skillId, effect);
    }

    /**
     * スキル効果を削除します
     *
     * @param skillId スキルID
     */
    public void unregisterSkill(String skillId) {
        skillEffects.remove(skillId);
        // 全てのアクティブトリガーをクリア
        for (Map<String, ActiveTriggerData> entityTriggers : activeTriggers.values()) {
            entityTriggers.remove(skillId);
        }
    }

    /**
     * スキルをアクティブ化します（トリガーを開始）
     *
     * @param skillId    スキルID
     * @param caster     発動者
     * @param level      スキルレベル
     * @param duration   効果時間（秒）、0で無制限
     * @param handlers   トリガーハンドラー
     */
    public void activateSkill(String skillId, LivingEntity caster, int level,
                              int duration, List<TriggerHandler> handlers) {
        int entityId = caster.getEntityId();

        // 全トリガーが期限切れであればアクティブデータを削除
        cleanupExpired(entityId);

        Map<String, ActiveTriggerData> entityTriggers = activeTriggers
                .computeIfAbsent(entityId, k -> new HashMap<>());

        ActiveTriggerData data = new ActiveTriggerData(duration, handlers);
        entityTriggers.put(skillId, data);

        // スキル効果をアクティブ化
        SkillEffect effect = skillEffects.get(skillId);
        if (effect != null) {
            effect.activate(caster, level, duration);
        }
    }

    /**
     * スキルを非アクティブ化します（トリガーを停止）
     *
     * @param skillId スキルID
     * @param caster  発動者
     */
    public void deactivateSkill(String skillId, LivingEntity caster) {
        int entityId = caster.getEntityId();
        Map<String, ActiveTriggerData> entityTriggers = activeTriggers.get(entityId);

        if (entityTriggers != null) {
            ActiveTriggerData data = entityTriggers.remove(skillId);
            if (data != null) {
                // コンポーネントのクリーンアップ
                for (TriggerHandler handler : data.handlers) {
                    handler.getRootComponent().cleanUp(caster);
                }
            }
        }

        // スキル効果を非アクティブ化
        SkillEffect effect = skillEffects.get(skillId);
        if (effect != null) {
            effect.deactivate(caster);
        }
    }

    /**
     * 期限切れのトリガーをクリーンアップします
     *
     * @param entityId エンティティID
     */
    private void cleanupExpired(int entityId) {
        Map<String, ActiveTriggerData> entityTriggers = activeTriggers.get(entityId);
        if (entityTriggers == null) {
            return;
        }

        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, ActiveTriggerData>> it = entityTriggers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ActiveTriggerData> entry = it.next();
            ActiveTriggerData data = entry.getValue();
            if (data.duration > 0 && data.expiryTime <= now) {
                // 期限切れ
                LivingEntity entity = getEntity(entityId);
                if (entity != null) {
                    for (TriggerHandler handler : data.handlers) {
                        handler.getRootComponent().cleanUp(entity);
                    }
                }
                it.remove();
            }
        }

        if (entityTriggers.isEmpty()) {
            activeTriggers.remove(entityId);
        }
    }

    /**
     * エンティティを取得します
     *
     * @param entityId エンティティID
     * @return エンティティ、存在しない場合はnull
     */
    private LivingEntity getEntity(int entityId) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getEntityId() == entityId) {
                return player;
            }
        }
        return null;
    }

    // ==================== イベントハンドラー ====================

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        int entityId = event.getPlayer().getEntityId();
        handleTrigger(event, entityId, "CROUCH");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            int entityId = event.getEntity().getKiller().getEntityId();
            handleTrigger(event, entityId, "KILL");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        LivingEntity entity = event.getEntity() instanceof LivingEntity
                ? (LivingEntity) event.getEntity()
                : null;
        if (entity == null) {
            return;
        }

        int entityId = entity.getEntityId();

        // 物理ダメージを受けた時
        handleTrigger(event, entityId, "PHYSICAL_TAKEN");

        // 物理ダメージを与えた時
        if (event.getDamager() instanceof LivingEntity) {
            int damagerId = event.getDamager().getEntityId();
            handleTrigger(event, damagerId, "PHYSICAL_DEALT");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        LivingEntity entity = event.getEntity() instanceof LivingEntity
                ? (LivingEntity) event.getEntity()
                : null;
        if (entity == null) {
            return;
        }

        int entityId = entity.getEntityId();

        // 着地トリガー（落下ダメージ時）
        if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL) {
            handleTrigger(event, entityId, "LAND");
        }

        // 環境ダメージトリガー（他エンティティからの攻撃を除く）
        if (!(event instanceof EntityDamageByEntityEvent)) {
            handleTrigger(event, entityId, "ENVIRONMENTAL");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof LivingEntity) {
            LivingEntity shooter = (LivingEntity) event.getEntity().getShooter();
            int entityId = shooter.getEntityId();
            handleTrigger(event, entityId, "LAUNCH");
        }
    }

    /**
     * トリガーを処理します
     *
     * @param event     イベント
     * @param entityId  エンティティID
     * @param triggerKey トリガーキー
     */
    private void handleTrigger(Event event, int entityId, String triggerKey) {
        Map<String, ActiveTriggerData> entityTriggers = activeTriggers.get(entityId);
        if (entityTriggers == null) {
            return;
        }

        for (ActiveTriggerData data : entityTriggers.values()) {
            for (TriggerHandler handler : data.handlers) {
                if (handler.getTrigger().getKey().equals(triggerKey)) {
                    LivingEntity caster = getEntity(entityId);
                    if (caster == null) {
                        continue;
                    }

                    SkillEffect effect = skillEffects.get(handler.getSkillId());
                    if (effect != null && effect.isActive(caster)) {
                        handler.handle(event, effect);
                    }
                }
            }
        }
    }

    /**
     * アクティブトリガーデータ
     */
    private static class ActiveTriggerData {
        final int duration;
        final long expiryTime;
        final List<TriggerHandler> handlers;

        ActiveTriggerData(int duration, List<TriggerHandler> handlers) {
            this.duration = duration;
            this.expiryTime = duration > 0 ? System.currentTimeMillis() + duration * 1000L : Long.MAX_VALUE;
            this.handlers = handlers;
        }
    }
}
