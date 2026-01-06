package com.example.rpgplugin.gui.menu;

import com.example.rpgplugin.RPGPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * StatMenuのクリックイベントを処理するリスナー
 */
public class StatMenuListener implements Listener {

    private final RPGPlugin plugin;

    public StatMenuListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // StatMenu以外は無視
        if (!(holder instanceof StatMenu)) {
            return;
        }

        StatMenu menu = (StatMenu) holder;
        Player player = menu.getPlayer();

        // プレイヤー以外のクリックはキャンセル
        if (event.getWhoClicked() != player) {
            event.setCancelled(true);
            return;
        }

        // イベントをキャンセル（アイテムの移動を防止）
        event.setCancelled(true);

        // クリックされたスロットを取得
        int slot = event.getRawSlot();

        // インベントリ外のクリックは無視
        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }

        // クリック情報を取得
        boolean isLeftClick = event.isLeftClick();
        boolean isShiftClick = event.isShiftClick();

        // メニューにクリックを委譲
        menu.handleClick(slot, isLeftClick, isShiftClick);
    }
}
