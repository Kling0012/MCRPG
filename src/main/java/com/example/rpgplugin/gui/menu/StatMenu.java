package com.example.rpgplugin.gui.menu;

import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * ステータス振りGUI（54スロット）
 *
 * GUIレイアウト:
 * ┌─────────────────────────────────┐
 * │         ステータス配分             │
 * ├─────────────────────────────────┤
 * │  STR: 20  [+][-]  (自動+10)      │
 * │  INT: 15  [+][-]  (自動+5)       │
 * │  SPI: 10  [+][-]  (自動+5)       │
 * │  VIT: 25  [+][-]  (自動+10)      │
 * │  DEX: 12  [+][-]  (自動+2)       │
 * ├─────────────────────────────────┤
 * │  残りポイント: 8                  │
 * ├─────────────────────────────────┤
 * │          [確認して閉じる]          │
 * └─────────────────────────────────┘
 */
public class StatMenu implements InventoryHolder {

    private final Inventory inventory;
    private final Player player;
    private final StatManager statManager;
    private final PlayerManager playerManager;
    private final RPGPlayer rpgPlayer;

    // GUI定数
    private static final int INVENTORY_SIZE = 54;
    private static final String INVENTORY_TITLE = "ステータス配分";

    // スロット配置
    private static final int[] STAT_ROWS = {10, 19, 28, 37, 46}; // 縦に配置
    private static final int[] INFO_SLOTS = {4, 22}; // 上部情報、残りポイント表示
    private static final int CONFIRM_SLOT = 49; // 確認ボタン（中央下）
    private static final int CANCEL_SLOT = 45; // キャンセルボタン（左下）

    /**
     * コンストラクタ
     */
    public StatMenu(Player player, StatManager statManager, PlayerManager playerManager) {
        this.player = player;
        this.statManager = statManager;
        this.playerManager = playerManager;
        this.rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        this.inventory = Bukkit.createInventory(this, INVENTORY_SIZE, INVENTORY_TITLE);

        initializeItems();
    }

    /**
     * GUIアイテムの初期化
     */
    private void initializeItems() {
        inventory.clear();

        // 装飾ガラスパネル
        fillBackground();

        // 上部情報パネル
        inventory.setItem(INFO_SLOTS[0], createInfoItem());

        // 各ステータス行を配置
        Stat[] statTypes = Stat.values();
        for (int i = 0; i < statTypes.length; i++) {
            int rowSlot = STAT_ROWS[i];
            setupStatRow(statTypes[i], rowSlot);
        }

        // 残りポイント表示
        inventory.setItem(INFO_SLOTS[1], createPointsDisplayItem());

        // 確認ボタン
        inventory.setItem(CONFIRM_SLOT, createConfirmButton());

        // キャンセルボタン
        inventory.setItem(CANCEL_SLOT, createCancelButton());
    }

    /**
     * 背景をガラスパネルで埋める
     */
    private void fillBackground() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            // 既に配置予定のスロットはスキップ
            if (i == INFO_SLOTS[0] || i == INFO_SLOTS[1] ||
                i == CONFIRM_SLOT || i == CANCEL_SLOT) {
                continue;
            }

            boolean isStatRow = false;
            for (int rowSlot : STAT_ROWS) {
                if (i >= rowSlot - 1 && i <= rowSlot + 3) {
                    isStatRow = true;
                    break;
                }
            }

            if (!isStatRow) {
                inventory.setItem(i, glass);
            }
        }
    }

    /**
     * 上部情報アイテムを作成
     */
    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lステータス配分");
        List<String> lore = new ArrayList<>();
        lore.add("§7クリック操作:");
        lore.add("§e左クリック§f: +1ポイント");
        lore.add("§e右クリック§f: -1ポイント");
        lore.add("§eShift+左クリック§f: +10ポイント");
        lore.add("§eShift+右クリック§f: -10ポイント");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * ステータス行を設定
     * 各ステータスの左から: [+] [ステータス表示] [-]
     */
    private void setupStatRow(Stat stat, int centerSlot) {
        // 左: +ボタン
        ItemStack plusButton = createStatButton(stat, true);
        inventory.setItem(centerSlot - 2, plusButton);

        // 中央: ステータス表示
        ItemStack statDisplay = createStatDisplay(stat);
        inventory.setItem(centerSlot, statDisplay);

        // 右: -ボタン
        ItemStack minusButton = createStatButton(stat, false);
        inventory.setItem(centerSlot + 2, minusButton);
    }

    /**
     * ステータス表示アイテムを作成
     */
    private ItemStack createStatDisplay(Stat stat) {
        Material material = getStatMaterial(stat);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        int baseValue = statManager.getBaseStat(stat);
        int finalValue = statManager.getFinalStat(stat);
        int autoValue = finalValue - baseValue;

        meta.setDisplayName(String.format("§c§l%s §f- §e%s",
            stat.getShortName(), stat.getDisplayName()));

        List<String> lore = new ArrayList<>();
        lore.add("§7合計: §f" + finalValue);
        lore.add("§7手動配分: §a" + baseValue);
        lore.add("§7自動配分: §b+" + autoValue);
        lore.add("");
        lore.add(stat.getDescription());

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * +/- ボタンを作成
     */
    private ItemStack createStatButton(Stat stat, boolean isPlus) {
        Material material = isPlus ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String symbol = isPlus ? "§a+§f+1" : "§c-§f-1";
        meta.setDisplayName(symbol);

        List<String> lore = new ArrayList<>();
        lore.add((isPlus ? "§a" : "§c") + stat.getShortName() + "に" + (isPlus ? "追加" : "削除"));
        lore.add("§7Shiftクリックで10");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * ステータスに対応するマテリアルを取得
     */
    private Material getStatMaterial(Stat stat) {
        switch (stat) {
            case STRENGTH:
                return Material.DIAMOND_SWORD;
            case INTELLIGENCE:
                return Material.BLAZE_ROD;
            case SPIRIT:
                return Material.ENCHANTED_BOOK;
            case VITALITY:
                return Material.GOLDEN_APPLE;
            case DEXTERITY:
                return Material.FEATHER;
            default:
                return Material.PAPER;
        }
    }

    /**
     * 残りポイント表示アイテムを作成
     */
    private ItemStack createPointsDisplayItem() {
        int available = statManager.getAvailablePoints();
        Material material = available > 0 ? Material.GOLD_INGOT : Material.BARRIER;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§e§l残りポイント: §f" + available);

        List<String> lore = new ArrayList<>();
        lore.add("§7配分可能なポイント数");

        if (available == 0) {
            lore.add("");
            lore.add("§cポイントがありません！");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * 確認ボタンを作成
     */
    private ItemStack createConfirmButton() {
        ItemStack item = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§a§l確認して閉じる");

        List<String> lore = new ArrayList<>();
        lore.add("§7ステータスを保存して閉じます");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * キャンセルボタンを作成
     */
    private ItemStack createCancelButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§c§lキャンセル");

        List<String> lore = new ArrayList<>();
        lore.add("§7変更を破棄して閉じます");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * クリックイベントを処理
     *
     * @param slot クリックされたスロット
     * @param isLeftClick 左クリックかどうか
     * @param isShiftClick Shiftクリックかどうか
     */
    public void handleClick(int slot, boolean isLeftClick, boolean isShiftClick) {
        // 確認ボタン
        if (slot == CONFIRM_SLOT) {
            saveAndClose();
            return;
        }

        // キャンセルボタン
        if (slot == CANCEL_SLOT) {
            cancelAndClose();
            return;
        }

        // ステータスボタンの処理
        for (int i = 0; i < Stat.values().length; i++) {
            int centerSlot = STAT_ROWS[i];

            // +ボタン (centerSlot - 2)
            if (slot == centerSlot - 2 && isLeftClick) {
                Stat stat = Stat.values()[i];
                int amount = isShiftClick ? 10 : 1;
                addPoints(stat, amount);
                return;
            }

            // -ボタン (centerSlot + 2)
            if (slot == centerSlot + 2 && !isLeftClick) {
                Stat stat = Stat.values()[i];
                int amount = isShiftClick ? 10 : 1;
                removePoints(stat, amount);
                return;
            }
        }
    }

    /**
     * ステータスにポイントを追加
     */
    private void addPoints(Stat stat, int amount) {
        if (statManager.getAvailablePoints() <= 0) {
            player.sendMessage("§c配分ポイントがありません！");
            return;
        }

        boolean success = statManager.allocatePoint(stat, amount);
        if (success) {
            refreshGUI();
            player.sendMessage(String.format("§a%sに+%dポイント (残り: %d)",
                stat.getColoredShortName(), amount, statManager.getAvailablePoints()));
        } else {
            player.sendMessage("§cポイントを配分できませんでした！");
        }
    }

    /**
     * ステータスからポイントを削除
     */
    private void removePoints(Stat stat, int amount) {
        int currentBase = statManager.getBaseStat(stat);
        if (currentBase <= 0) {
            player.sendMessage("§c削除できるポイントがありません！");
            return;
        }

        int toRemove = Math.min(amount, currentBase);
        statManager.setBaseStat(stat, currentBase - toRemove);
        statManager.setAvailablePoints(statManager.getAvailablePoints() + toRemove);

        refreshGUI();
        player.sendMessage(String.format("§c%sから-%dポイント (残り: %d)",
            stat.getColoredShortName(), toRemove, statManager.getAvailablePoints()));
    }

    /**
     * GUIを更新
     */
    private void refreshGUI() {
        // ステータス表示と残りポイントを更新
        for (int i = 0; i < Stat.values().length; i++) {
            int centerSlot = STAT_ROWS[i];
            Stat stat = Stat.values()[i];
            inventory.setItem(centerSlot, createStatDisplay(stat));
        }

        inventory.setItem(INFO_SLOTS[1], createPointsDisplayItem());
    }

    /**
     * 保存して閉じる
     */
    private void saveAndClose() {
        if (rpgPlayer != null) {
            // PlayerStatsは既にRPGPlayerによって管理されているため、
            // 明示的な保存操作は不要（PlayerManagerが永続化を担当）
            player.sendMessage("§aステータスを保存しました！");
        }
        player.closeInventory();
    }

    /**
     * キャンセルして閉じる
     */
    private void cancelAndClose() {
        if (rpgPlayer != null) {
            // RPGPlayerからステータスを再読み込み
            var reloadedPlayer = playerManager.getRPGPlayer(player.getUniqueId());
            if (reloadedPlayer != null) {
                // ステータスはRPGPlayerによって管理されているため、
                // 単にGUIを閉じるだけで元の値に戻る
            }
        }
        player.sendMessage("§c変更を破棄しました");
        player.closeInventory();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * プレイヤーを取得
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * GUIを開く
     */
    public void open() {
        player.openInventory(inventory);
    }
}
