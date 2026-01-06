package com.example.rpgplugin.gui.menu;

import com.example.rpgplugin.RPGPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * スキルメニューリスナー
 *
 * <p>スキルGUIのクリックイベントを処理します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: イベント処理に専念</li>
 *   <li>DRY: GUI管理ロジックを一元管理</li>
 *   <li>KISS: シンプルなイベントハンドリング</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillMenuListener implements Listener {

    private final RPGPlugin plugin;
    private final Map<UUID, SkillMenu> openMenus;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public SkillMenuListener(RPGPlugin plugin) {
        this.plugin = plugin;
        this.openMenus = new HashMap<>();
    }

    /**
     * インベントリクリックイベントを処理します
     *
     * @param event イベント
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();
        SkillMenu menu = openMenus.get(uuid);

        if (menu == null) {
            return;
        }

        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getView().getTopInventory();

        // 自分のGUIでない場合は無視
        if (topInventory != menu.getInventory()) {
            return;
        }

        // クリックをキャンセル
        event.setCancelled(true);

        // 上部インベントリのクリックのみ処理
        if (clickedInventory == null || clickedInventory != topInventory) {
            return;
        }

        // クリックイベントを処理
        boolean handled = menu.handleClick(event.getSlot());

        if (handled) {
            // GUIをリフレッシュ
            menu.refreshGUI();
        }
    }

    /**
     * インベントリクローズイベントを処理します
     *
     * @param event イベント
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();

        // メニューを削除
        openMenus.remove(uuid);
    }

    /**
     * メニューを登録します
     *
     * @param player プレイヤー
     * @param menu スキルメニュー
     */
    public void registerMenu(Player player, SkillMenu menu) {
        openMenus.put(player.getUniqueId(), menu);
    }

    /**
     * メニューを削除します
     *
     * @param player プレイヤー
     */
    public void unregisterMenu(Player player) {
        openMenus.remove(player.getUniqueId());
    }

    /**
     * プレイヤーがメニューを開いているかチェックします
     *
     * @param player プレイヤー
     * @return 開いている場合はtrue
     */
    public boolean hasOpenMenu(Player player) {
        return openMenus.containsKey(player.getUniqueId());
    }

    /**
     * プレイヤーのメニューを取得します
     *
     * @param player プレイヤー
     * @return スキルメニュー、開いていない場合はnull
     */
    public SkillMenu getMenu(Player player) {
        return openMenus.get(player.getUniqueId());
    }

    /**
     * 全メニューをクリアします
     */
    public void clearAllMenus() {
        openMenus.clear();
    }

    /**
     * 開いているメニューの数を取得します
     *
     * @return メニュー数
     */
    public int getOpenMenuCount() {
        return openMenus.size();
    }
}
