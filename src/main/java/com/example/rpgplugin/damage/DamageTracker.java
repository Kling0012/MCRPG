package com.example.rpgplugin.damage;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ダメージ追跡システム
 *
 * <p>Paper 1.20.6でEntityDeathEvent.getKiller()が削除されたため、
 * ダメージイベントを監視してキラーを特定します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ダメージ追跡に特化</li>
 *   <li>DRY: キラー特定ロジックを一元管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class DamageTracker implements Listener {

    /**
     * 最後にダメージを与えたプレイヤーを追跡
     * Key: エンティティのUUID
     * Value: 最後にダメージを与えたプレイヤーのUUID
     */
    private final Map<UUID, UUID> lastDamager = new ConcurrentHashMap<>();

    /**
     * エンティティが死亡したときに呼び出され、キラーを取得します
     *
     * @param event 死亡イベント
     * @return キラープレイヤー、特定できない場合はnull
     */
    public Player getKiller(EntityDeathEvent event) {
        return getKiller(event.getEntity());
    }

    /**
     * エンティティのキラーを取得します
     *
     * @param entity エンティティ
     * @return キラープレイヤー、特定できない場合はnull
     */
    public Player getKiller(Entity entity) {
        if (entity == null) {
            return null;
        }

        UUID killerUuid = lastDamager.get(entity.getUniqueId());
        if (killerUuid == null) {
            return null;
        }

        // エンティティのワールドからプレイヤーを取得
        // プレイヤーがオフラインの場合はnullが返される
        return entity.getServer().getPlayer(killerUuid);
    }

    /**
     * プレイヤーがエンティティのキラーであるか確認します
     *
     * @param entity エンティティ
     * @param player プレイヤー
     * @return プレイヤーがキラーの場合はtrue
     */
    public boolean isKiller(Entity entity, Player player) {
        if (entity == null || player == null) {
            return false;
        }

        UUID killerUuid = lastDamager.get(entity.getUniqueId());
        return killerUuid != null && killerUuid.equals(player.getUniqueId());
    }

    /**
     * エンティティのダメージ記録をクリアします
     *
     * @param entity エンティティ
     */
    public void clearDamageRecord(Entity entity) {
        if (entity != null) {
            lastDamager.remove(entity.getUniqueId());
        }
    }

    /**
     * ダメージイベントを監視して、最後のダメージ元を記録します
     *
     * @param event ダメージイベント
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        Entity damager = event.getDamager();

        // ダメージを受けたエンティティがプレイヤーの場合は追跡しない
        if (damaged instanceof Player) {
            return;
        }

        // ダメージを与えたのがプレイヤーの場合のみ記録
        Player player = null;
        if (damager instanceof Player) {
            player = (Player) damager;
        } else if (damager instanceof org.bukkit.entity.Projectile) {
            // 矢やトライデントなどの投射物
            org.bukkit.entity.Projectile projectile = (org.bukkit.entity.Projectile) damager;
            if (projectile.getShooter() instanceof Player) {
                player = (Player) projectile.getShooter();
            }
        } else if (damager instanceof org.bukkit.entity.AreaEffectCloud) {
            // 残留ポーション
            org.bukkit.entity.AreaEffectCloud cloud = (org.bukkit.entity.AreaEffectCloud) damager;
            if (cloud.getSource() instanceof Player) {
                player = (Player) cloud.getSource();
            }
        }

        if (player != null) {
            lastDamager.put(damaged.getUniqueId(), player.getUniqueId());
        }
    }

    /**
     * 死亡イベントで記録をクリアします
     *
     * @param event 死亡イベント
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        // 死亡したエンティティの記録をクリア（メモリリーク防止）
        clearDamageRecord(event.getEntity());
    }
}
