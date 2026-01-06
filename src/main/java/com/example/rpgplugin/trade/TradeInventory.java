package com.example.rpgplugin.trade;

import com.example.rpgplugin.trade.model.TradeParty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * トレードGUI
 *
 * <p>2人のプレイヤー間のトレードを視覚化します。</p>
 *
 * <p>GUIレイアウト (54スロット):</p>
 * <pre>
 * ┌─────────────────────────────────────────┐
 * │         プレイヤー1 vs プレイヤー2         │  ← タイトル行
 * ├───────────────────┬─────────────────────┤
 * │   P1のアイテム      │   P2のアイテム       │  ← アイテムエリア
 * │   [スロット9個]     │   [スロット9個]     │     （行2, 行4）
 * │                   │                    │
 * ├───────────────────┴─────────────────────┤
 * │   P1: ゴールド100G    P2: ゴールド50G      │  ← ステータス行（行3）
 * ├─────────────────────────────────────────┤
 * │        [確認]  [キャンセル]               │  ← ボタン行（行6）
 * └─────────────────────────────────────────┘
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class TradeInventory implements InventoryHolder {

    private final Inventory inventory;
    private final TradeSession session;
    private final Player viewer;
    private final TradeParty viewerParty;
    private final TradeParty counterparty;

    // GUI定数
    private static final int INVENTORY_SIZE = 54;

    // スロット配置
    private static final int P1_ITEMS_START = 9;   // 行2の左端
    private static final int P1_ITEMS_END = 17;    // 行2の右端
    private static final int P2_ITEMS_START = 36;  // 行4の左端
    private static final int P2_ITEMS_END = 44;    // 行4の右端

    private static final int P1_STATUS_SLOT = 22;  // 行3の左寄り
    private static final int P2_STATUS_SLOT = 31;  // 行3の右寄り

    private static final int CONFIRM_SLOT = 49;    // 行6の中央
    private static final int CANCEL_SLOT = 45;     // 行6の左

    /**
     * コンストラクタ
     *
     * @param session トレードセッション
     * @param viewer 閲覧者（GUIを開くプレイヤー）
     */
    public TradeInventory(TradeSession session, Player viewer) {
        this.session = session;
        this.viewer = viewer;
        this.viewerParty = session.getParty(viewer.getUniqueId())
            .orElseThrow(() -> new IllegalArgumentException("Viewer is not part of this trade session"));
        this.counterparty = session.getCounterparty(viewer.getUniqueId())
            .orElseThrow(() -> new IllegalArgumentException("No counterparty found"));

        // タイトルを作成
        String title = createTitle();

        this.inventory = Bukkit.createInventory(this, INVENTORY_SIZE, title);

        initializeItems();
    }

    /**
     * GUIタイトルを作成
     *
     * @return タイトル
     */
    private String createTitle() {
        TradeParty p1 = session.getParty1();
        TradeParty p2 = session.getParty2();

        // 閲覧者に応じて表示順を調整
        if (viewerParty.getUuid().equals(p2.getUuid())) {
            // 閲覧者がP2の場合、順序を入れ替え
            return String.format("トレード: %s <-> %s", p2.getUsername(), p1.getUsername());
        } else {
            return String.format("トレード: %s <-> %s", p1.getUsername(), p2.getUsername());
        }
    }

    /**
     * GUIアイテムの初期化
     */
    private void initializeItems() {
        inventory.clear();

        // 背景を埋める
        fillBackground();

        // アイテムエリアを設定
        setupItemsArea();

        // ステータス表示を設定
        setupStatusDisplay();

        // ボタンを設定
        setupButtons();
    }

    /**
     * 背景をガラスパネルで埋める
     */
    private void fillBackground() {
        ItemStack glass = createGlassPane(ChatColor.GRAY, " ");
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            // 使用中のスロットはスキップ
            if (isUsedSlot(i)) {
                continue;
            }
            inventory.setItem(i, glass);
        }
    }

    /**
     * ガラスパーンを作成
     *
     * @param color 色
     * @param name 表示名
     * @return ガラスパーン
     */
    private ItemStack createGlassPane(ChatColor color, String name) {
        Material material = color == ChatColor.RED ? Material.RED_STAINED_GLASS_PANE :
                           color == ChatColor.GREEN ? Material.LIME_STAINED_GLASS_PANE :
                           color == ChatColor.YELLOW ? Material.YELLOW_STAINED_GLASS_PANE :
                           Material.GRAY_STAINED_GLASS_PANE;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * アイテムエリアを設定
     */
    private void setupItemsArea() {
        // 自分のアイテム
        for (int i = 0; i < 9; i++) {
            ItemStack item = viewerParty.getOffer().getItem(i);
            if (item == null) {
                item = createEmptySlot();
            }
            inventory.setItem(P1_ITEMS_START + i, item);
        }

        // 相手のアイテム
        for (int i = 0; i < 9; i++) {
            ItemStack item = counterparty.getOffer().getItem(i);
            if (item == null) {
                item = createEmptySlot();
            }
            inventory.setItem(P2_ITEMS_START + i, item);
        }
    }

    /**
     * 空スロットアイテムを作成
     *
     * @return 空スロット
     */
    private ItemStack createEmptySlot() {
        ItemStack item = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "空きスロット");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + "ここにアイテムをドラッグ＆ドロップ");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * ステータス表示を設定
     */
    private void setupStatusDisplay() {
        // 自分のステータス
        inventory.setItem(P1_STATUS_SLOT, createStatusItem(viewerParty, true));

        // 相手のステータス
        inventory.setItem(P2_STATUS_SLOT, createStatusItem(counterparty, false));
    }

    /**
     * ステータスアイテムを作成
     *
     * @param party トレード参加者
     * @param isViewer 自分かどうか
     * @return ステータスアイテム
     */
    private ItemStack createStatusItem(TradeParty party, boolean isViewer) {
        Material material = isViewer ? Material.GOLD_BLOCK : Material.GOLD_INGOT;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String name = party.getUsername();
        boolean ready = party.isReady();

        ChatColor nameColor = isViewer ? ChatColor.GREEN : ChatColor.YELLOW;
        ChatColor statusColor = ready ? ChatColor.GREEN : ChatColor.RED;

        meta.setDisplayName(nameColor + name);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "アイテム数: " + ChatColor.WHITE + party.getOffer().getItemSize());
        lore.add(ChatColor.GRAY + "ゴールド: " + ChatColor.GOLD + String.format("%.2fG", party.getOffer().getGoldAmount()));
        lore.add("");
        lore.add(statusColor + (ready ? "✓ 確認済み" : "未確認"));

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * ボタンを設定
     */
    private void setupButtons() {
        // 確認ボタン
        inventory.setItem(CONFIRM_SLOT, createConfirmButton());

        // キャンセルボタン
        inventory.setItem(CANCEL_SLOT, createCancelButton());
    }

    /**
     * 確認ボタンを作成
     *
     * @return 確認ボタン
     */
    private ItemStack createConfirmButton() {
        boolean ready = viewerParty.isReady();
        boolean counterpartyReady = counterparty.isReady();

        Material material;
        String name;
        List<String> lore = new ArrayList<>();

        if (ready && counterpartyReady) {
            material = Material.EMERALD_BLOCK;
            name = ChatColor.GREEN + "" + ChatColor.BOLD + "✓ トレード実行";
            lore.add(ChatColor.WHITE + "クリックでトレードを完了します");
        } else if (ready) {
            material = Material.LIME_STAINED_GLASS_PANE;
            name = ChatColor.YELLOW + "確認待ち...";
            lore.add(ChatColor.GRAY + "相手の確認をお待ちください");
        } else {
            material = Material.YELLOW_STAINED_GLASS_PANE;
            name = ChatColor.GREEN + "確認する";
            lore.add(ChatColor.WHITE + "クリックで確認します");
            lore.add("");
            lore.add(ChatColor.GRAY + "相手も確認するとトレード実行");
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * キャンセルボタンを作成
     *
     * @return キャンセルボタン
     */
    private ItemStack createCancelButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "キャンセル");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "クリックでトレードをキャンセル");
        lore.add("");
        lore.add(ChatColor.GRAY + "アイテムは返却されます");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * スロットが使用中かチェック
     *
     * @param slot スロット番号
     * @return 使用中の場合true
     */
    private boolean isUsedSlot(int slot) {
        return (slot >= P1_ITEMS_START && slot <= P1_ITEMS_END) ||
               (slot >= P2_ITEMS_START && slot <= P2_ITEMS_END) ||
               slot == P1_STATUS_SLOT ||
               slot == P2_STATUS_SLOT ||
               slot == CONFIRM_SLOT ||
               slot == CANCEL_SLOT;
    }

    /**
     * クリックイベントを処理
     *
     * @param slot スロット番号
     * @return 処理成功時true
     */
    public boolean handleClick(int slot) {
        // 確認ボタン
        if (slot == CONFIRM_SLOT) {
            boolean newState = session.toggleConfirmation(viewer);
            viewer.sendMessage(newState ? ChatColor.GREEN + "確認しました" : ChatColor.YELLOW + "確認を解除しました");
            // GUI更新はTradeMenuListenerで実施
            return true;
        }

        // キャンセルボタン
        if (slot == CANCEL_SLOT) {
            return true; // TradeManagerで処理
        }

        // アイテムスロットのクリックはTradeMenuListenerで処理
        return false;
    }

    /**
     * GUIを更新
     */
    public void refreshGUI() {
        // アイテムエリアを更新
        setupItemsArea();

        // ステータス表示を更新
        setupStatusDisplay();

        // ボタンを更新
        setupButtons();
    }

    /**
     * アイテムスロットかチェック
     *
     * @param slot スロット番号
     * @return アイテムスロットの場合true
     */
    public boolean isItemSlot(int slot) {
        return (slot >= P1_ITEMS_START && slot <= P1_ITEMS_END) ||
               (slot >= P2_ITEMS_START && slot <= P2_ITEMS_END);
    }

    /**
     * 自分のアイテムスロットかチェック
     *
     * @param slot スロット番号
     * @return 自分のアイテムスロットの場合true
     */
    public boolean isOwnItemSlot(int slot) {
        return slot >= P1_ITEMS_START && slot <= P1_ITEMS_END;
    }

    /**
     * アイテムスロットのインデックスを取得
     *
     * @param slot スロット番号
     * @return アイテムインデックス（0-8）、アイテムスロットでない場合は-1
     */
    public int getItemIndex(int slot) {
        if (slot >= P1_ITEMS_START && slot <= P1_ITEMS_END) {
            return slot - P1_ITEMS_START;
        } else if (slot >= P2_ITEMS_START && slot <= P2_ITEMS_END) {
            return slot - P2_ITEMS_START;
        }
        return -1;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * 閲覧者を取得
     *
     * @return 閲覧者
     */
    public Player getViewer() {
        return viewer;
    }

    /**
     * セッションを取得
     *
     * @return セッション
     */
    public TradeSession getSession() {
        return session;
    }

    /**
     * GUIを開く
     */
    public void open() {
        viewer.openInventory(inventory);
    }
}
