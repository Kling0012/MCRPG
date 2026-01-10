package com.example.rpgplugin.gui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * スキルツリーGUIのイベントリスナー
 * <p>
 * GUI内でのクリック操作を処理し、アイテムの持ち出しを防止します。
 * </p>
 */
public class SkillTreeGUIListener implements Listener {

    /**
     * インベントリクリックイベント
     * <p>
     * スキルツリーGUI内での操作を処理し、アイテムの持ち出しを防止します。
     * </p>
     *
     * @param event イベント
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        String title = event.getView().getTitle();

        // スキルツリーGUI以外は無視
        if (!"スキルツリー".equals(title)) {
            return;
        }

        // クリックをキャンセルしてアイテムの持ち出しを防止
        event.setCancelled(true);

        // トップインベントリでのクリックのみ処理
        if (inventory == null || !event.getView().getTopInventory().equals(inventory)) {
            return;
        }

        SkillTreeGUI gui = SkillTreeGUI.getOpenGUI(player);
        if (gui == null) {
            player.closeInventory();
            return;
        }

        int slot = event.getSlot();
        boolean isRightClick = event.isRightClick() || event.isShiftClick();

        // スキルスロットのクリック処理
        if (SkillTreeGUI.isSkillSlot(slot)) {
            String skillId = gui.getSkillIdFromSlot(slot);
            if (skillId != null) {
                handleSkillClick(gui, skillId, isRightClick);
            }
        } else if (slot == 49) {
            // 閉じるボタン
            player.closeInventory();
        }
    }

    /**
     * スキルクリック処理
     *
     * @param gui          GUIインスタンス
     * @param skillId      スキルID
     * @param isRightClick 右クリックかどうか
     */
    private void handleSkillClick(SkillTreeGUI gui, String skillId, boolean isRightClick) {
        if (isRightClick) {
            // 右クリック: 解除/レベルダウン
            gui.refundSkill(skillId);
        } else {
            // 左クリック: 習得/レベルアップ
            gui.acquireSkill(skillId);
        }
    }

    /**
     * インベントリクローズイベント
     * <p>
     * GUIが閉じられた際に後処理を行います。
     * </p>
     *
     * @param event イベント
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        // スキルツリーGUIが閉じられた場合
        if ("スキルツリー".equals(title)) {
            SkillTreeGUI gui = SkillTreeGUI.getOpenGUI(player);
            if (gui != null) {
                gui.close();
            }
        }
    }
}
