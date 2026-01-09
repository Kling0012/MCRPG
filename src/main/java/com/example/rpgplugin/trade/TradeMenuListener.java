package com.example.rpgplugin.trade;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * トレードGUIのイベントリスナー
 *
 * <p>トレードインベントリでのクリック、ドラッグ、クローズイベントを処理します。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class TradeMenuListener implements Listener {

    private final TradeManager tradeManager;

    /**
     * コンストラクタ
     *
     * @param tradeManager トレードマネージャー
     */
    public TradeMenuListener(TradeManager tradeManager) {
        this.tradeManager = tradeManager;
    }

    /**
     * インベントリクリックイベントを処理
     *
     * @param event イベント
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // TradeInventory以外は無視
        if (!(holder instanceof TradeInventory)) {
            return;
        }

        TradeInventory tradeInv = (TradeInventory) holder;
        Player player = tradeInv.getViewer();

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

        // クリックを処理
        handleClick(tradeInv, event, slot);
    }

    /**
     * クリック処理を委譲
     *
     * @param tradeInv トレードインベントリ
     * @param event クリックイベント
     * @param slot スロット番号
     */
    private void handleClick(TradeInventory tradeInv, InventoryClickEvent event, int slot) {
        // ボタンクリック
        if (handleButtonClick(tradeInv, slot)) {
            return;
        }

        // アイテムスロットクリック
        handleItemSlotClick(tradeInv, event, slot);
    }

    /**
     * ボタンクリックを処理
     *
     * @param tradeInv トレードインベントリ
     * @param slot スロット番号
     * @return 処理した場合true
     */
    private boolean handleButtonClick(TradeInventory tradeInv, int slot) {
        TradeSession session = tradeInv.getSession();

        // キャンセルボタン（スロット45）
        if (slot == 45) {
            tradeManager.cancelSession(session, "キャンセルされました");
            return true;
        }

        // 確認ボタン（スロット49）
        if (slot == 49) {
            if (tradeInv.handleClick(slot)) {
                // 双方のGUIを更新
                updateBothGUIs(session);

                // 双方が確認済みの場合、トレードを実行
                if (session.canExecute()) {
                    tradeManager.executeTrade(session);
                }
            }
            return true;
        }

        return false;
    }

    /**
     * アイテムスロットクリックを処理
     *
     * @param tradeInv トレードインベントリ
     * @param event クリックイベント
     * @param slot スロット番号
     */
    private void handleItemSlotClick(TradeInventory tradeInv, InventoryClickEvent event, int slot) {
        // アイテムスロットでなければ無視
        if (!tradeInv.isItemSlot(slot)) {
            return;
        }

        // 相手のスロットは操作不可
        if (!tradeInv.isOwnItemSlot(slot)) {
            tradeInv.getViewer().sendMessage(org.bukkit.ChatColor.RED + "相手のアイテムは操作できません");
            return;
        }

        Player player = tradeInv.getViewer();
        TradeSession session = tradeInv.getSession();
        int itemIndex = tradeInv.getItemIndex(slot);

        // シフトクリック：アイテムを追加
        if (event.isShiftClick() && event.getCurrentItem() != null) {
            ItemStack cursorItem = event.getCurrentItem();
            if (addItemToTrade(session, player, itemIndex, cursorItem)) {
                // プレイヤーのインベントリからアイテムを削除
                event.getView().setCursor(null);
            }
        }
        // 左クリック：アイテムを削除
        else if (event.isLeftClick()) {
            removeItemFromTrade(session, player, itemIndex);
        }
        // 右クリック：半分を削除（スタック可能なアイテムの場合）
        else if (event.isRightClick()) {
            removeHalfItemFromTrade(session, player, itemIndex);
        }
    }

    /**
     * トレードにアイテムを追加
     *
     * @param session トレードセッション
     * @param player プレイヤー
     * @param slot スロット番号
     * @param item 追加するアイテム
     * @return 追加成功時true
     */
    private boolean addItemToTrade(TradeSession session, Player player, int slot, ItemStack item) {
        if (session == null || item == null) {
            return false;
        }

        // セッションにアイテムを追加
        boolean added = session.addItem(player, slot, item);

        if (added) {
            player.sendMessage(org.bukkit.ChatColor.GREEN + "アイテムを追加しました");
            // 双方のGUIを更新
            updateBothGUIs(session);
        }

        return added;
    }

    /**
     * トレードからアイテムを削除
     *
     * @param session トレードセッション
     * @param player プレイヤー
     * @param slot スロット番号
     */
    private void removeItemFromTrade(TradeSession session, Player player, int slot) {
        if (session == null) {
            return;
        }

        boolean removed = session.removeItem(player, slot);

        if (removed) {
            player.sendMessage(org.bukkit.ChatColor.YELLOW + "アイテムを削除しました");
            // 双方のGUIを更新
            updateBothGUIs(session);
        }
    }

    /**
     * トレードからアイテムの半分を削除
     *
     * @param session トレードセッション
     * @param player プレイヤー
     * @param slot スロット番号
     */
    private void removeHalfItemFromTrade(TradeSession session, Player player, int slot) {
        if (session == null) {
            return;
        }

        var party = session.getParty(player.getUniqueId());
        if (!party.isPresent()) {
            return;
        }

        ItemStack item = party.get().getOffer().getItem(slot);
        if (item == null) {
            return;
        }

        int amount = item.getAmount();
        if (amount <= 1) {
            // 1個の場合は全削除
            removeItemFromTrade(session, player, slot);
            return;
        }

        // 半分を削除
        int halfAmount = amount / 2;
        session.removeItem(player, slot);

        // 残りを再追加
        item.setAmount(amount - halfAmount);
        session.addItem(player, slot, item);

        player.sendMessage(org.bukkit.ChatColor.YELLOW + "アイテムの半分を削除しました");
        // 双方のGUIを更新
        updateBothGUIs(session);
    }

    /**
     * 両プレイヤーのGUIを更新
     *
     * @param session トレードセッション
     */
    private void updateBothGUIs(TradeSession session) {
        var p1 = session.getParty1().getPlayer();
        var p2 = session.getParty2().getPlayer();

        if (p1 != null && p1.isOnline() && p1.getOpenInventory().getTopInventory().getHolder() instanceof TradeInventory) {
            TradeInventory inv1 = (TradeInventory) p1.getOpenInventory().getTopInventory().getHolder();
            inv1.refreshGUI();
        }

        if (p2 != null && p2.isOnline() && p2.getOpenInventory().getTopInventory().getHolder() instanceof TradeInventory) {
            TradeInventory inv2 = (TradeInventory) p2.getOpenInventory().getTopInventory().getHolder();
            inv2.refreshGUI();
        }
    }

    /**
     * インベントリクローズイベントを処理
     *
     * @param event イベント
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // TradeInventory以外は無視
        if (!(holder instanceof TradeInventory)) {
            return;
        }

        TradeInventory tradeInv = (TradeInventory) holder;
        TradeSession session = tradeInv.getSession();

        // トレードが未完了の場合、キャンセル扱い
        if (session.getState() != TradeSession.TradeState.COMPLETED) {
            // 相手がオンラインでGUIを開いている場合のみキャンセル
            if (session.areBothOnline()) {
                // TODO: 相手にも通知
            }

            // トレードをキャンセル（ただしアイテム返却は自動）
            tradeManager.cancelSession(session, "インベントリが閉じられました");
        }
    }

    /**
     * インベントリドラッグイベントを処理
     *
     * @param event イベント
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // TradeInventory以外は無視
        if (!(holder instanceof TradeInventory)) {
            return;
        }

        TradeInventory tradeInv = (TradeInventory) holder;
        Player player = tradeInv.getViewer();

        // プレイヤー以外のドラッグはキャンセル
        if (event.getWhoClicked() != player) {
            event.setCancelled(true);
            return;
        }

        TradeSession session = tradeInv.getSession();

        // 自分のアイテムスロットへのドラッグのみ許可
        for (Integer slot : event.getInventorySlots()) {
            if (!tradeInv.isOwnItemSlot(slot)) {
                event.setCancelled(true);
                player.sendMessage(org.bukkit.ChatColor.RED + "自分のアイテムスロットにのみドラッグできます");
                return;
            }
        }

        // ドラッグされたアイテムを取得
        ItemStack draggedItem = event.getOldCursor();
        if (draggedItem == null || draggedItem.getType().isAir()) {
            event.setCancelled(true);
            return;
        }

        // 最初のスロットにアイテムを追加
        int firstSlot = event.getInventorySlots().iterator().next();
        int itemIndex = tradeInv.getItemIndex(firstSlot);

        if (addItemToTrade(session, player, itemIndex, draggedItem)) {
            // カーソルをクリア
            event.getView().setCursor(null);
        }

        event.setCancelled(true);
    }
}
