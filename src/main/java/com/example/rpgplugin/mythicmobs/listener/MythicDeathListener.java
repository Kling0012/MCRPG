package com.example.rpgplugin.mythicmobs.listener;

import com.example.rpgplugin.mythicmobs.MythicMobsManager;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * MythicMobs死亡イベントリスナー
 *
 * <p>MythicMobの死亡を監視し、ドロップ処理を実行します。
 * また、ピックアップ制御も担当します。</p>
 *
 * <p>主な機能:</p>
 * <ul>
 *   <li>MythicMob死亡イベント監視</li>
 *   <li>ドロップアイテムの所有者判定</li>
 *   <li>ピックアップ制限（独占ドロップ）</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>単一責任: イベント処理のみ担当</li>
 *   <li>High優先度: 他のプラグインより先に処理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class MythicDeathListener implements Listener {

    private final MythicMobsManager mythicMobsManager;

    /**
     * コンストラクタ
     *
     * @param mythicMobsManager MythicMobsマネージャー
     */
    public MythicDeathListener(MythicMobsManager mythicMobsManager) {
        this.mythicMobsManager = mythicMobsManager;
    }

    /**
     * MythicMob死亡イベントハンドラー
     *
     * <p>MythicMobが死亡した際にドロップ処理を実行します。</p>
     *
     * @param event MythicMob死亡イベント
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        if (!mythicMobsManager.isAvailable()) {
            return;
        }

        // キラー（倒したプレイヤー）を取得
        Entity killer = event.getKiller();

        if (!(killer instanceof Player player)) {
            // プレイヤーが倒した場合のみ処理
            return;
        }

        // 死亡したエンティティを取得
        Entity entity = event.getEntity();

        // ドロップ処理を実行
        mythicMobsManager.handleMobDeath(player, entity);

        // MythicMobsデフォルトのドロップを無効化
        // 注: この処理は必要に応じて調整してください
        // event.setDrops(false); // オプション
    }

    /**
     * プレイヤーアイテムピックアップイベントハンドラー
     *
     * <p>独占ドロップの所有者チェックを行います。</p>
     *
     * @param event プレイヤーピックアップイベント
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (!mythicMobsManager.isAvailable()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();

        // ピックアップ可能か確認
        if (!mythicMobsManager.getDropHandler().canPickup(player, item)) {
            event.setCancelled(true);
            player.sendMessage(org.bukkit.ChatColor.RED + "このアイテムは拾えません！");
        }
    }

    /**
     * インベントリピックアップイベントハンドラー
     *
     * <p>ホッパー等のインベントリによるピックアップを制限します。</p>
     *
     * @param event インベントリピックアップイベント
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (!mythicMobsManager.isAvailable()) {
            return;
        }

        ItemStack item = event.getItem().getItemStack();

        // 独占ドロップの場合はインベントリピックアップを禁止
        if (!mythicMobsManager.getDropHandler().isExpired(item)) {
            event.setCancelled(true);
        }
    }

    /**
     * MythicMobsスタンデーションが提供する death イベントの代替ハンドラー
     *
     * <p>MythicMobDeathEventが firing されない場合のフォールバック処理。</p>
     *
     * @param event エンティティ死亡イベント
     */
    /*
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent event) {
        if (!mythicMobsManager.isAvailable()) {
            return;
        }

        Entity entity = event.getEntity();

        // MythicMobか確認
        if (!mythicMobsManager.isMythicMob(entity)) {
            return;
        }

        // 既にMythicMobDeathEventで処理済みの場合はスキップ
        // 注: このロジックは必要に応じて調整してください

        Player killer = entity.getKiller();
        if (killer == null) {
            return;
        }

        // ドロップ処理を実行
        mythicMobsManager.handleMobDeath(killer, entity);

        // デフォルトドロップをクリア（オプション）
        // event.getDrops().clear();
    }
    */
}
