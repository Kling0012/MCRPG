package com.example.rpgplugin.trade.model;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * トレードオファー（トレード提案）
 *
 * <p>プレイヤーがトレードで提供するアイテムとゴールドを管理します。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class TradeOffer {

    private final UUID playerUuid;
    private final List<ItemStack> items;
    private double goldAmount;
    private boolean confirmed;

    /**
     * コンストラクタ
     *
     * @param playerUuid プレイヤーUUID
     */
    public TradeOffer(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.items = new ArrayList<>();
        this.goldAmount = 0.0;
        this.confirmed = false;
    }

    /**
     * アイテムを追加
     *
     * @param item 追加するアイテム
     * @return 追加成功時true
     */
    public boolean addItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        return items.add(item.clone());
    }

    /**
     * アイテムを削除
     *
     * @param slot スロット番号
     * @return 削除したアイテム（存在しない場合はnull）
     */
    public ItemStack removeItem(int slot) {
        if (slot < 0 || slot >= items.size()) {
            return null;
        }
        return items.remove(slot);
    }

    /**
     * アイテムをクリア
     */
    public void clearItems() {
        items.clear();
    }

    /**
     * ゴールド額を設定
     *
     * @param amount ゴールド額
     */
    public void setGoldAmount(double amount) {
        this.goldAmount = Math.max(0, amount);
    }

    /**
     * ゴールドを追加
     *
     * @param amount 追加額
     */
    public void addGold(double amount) {
        this.goldAmount += Math.max(0, amount);
    }

    /**
     * 確認状態を設定
     *
     * @param confirmed 確認状態
     */
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    /**
     * プレイヤーUUIDを取得
     *
     * @return プレイヤーUUID
     */
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    /**
     * アイテムリストを取得
     *
     * @return アイテムリスト（コピー）
     */
    public List<ItemStack> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * 指定スロットのアイテムを取得
     *
     * @param slot スロット番号
     * @return アイテム（存在しない場合はnull）
     */
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= items.size()) {
            return null;
        }
        return items.get(slot).clone();
    }

    /**
     * アイテム数を取得
     *
     * @return アイテム数
     */
    public int getItemSize() {
        return items.size();
    }

    /**
     * ゴールド額を取得
     *
     * @return ゴールド額
     */
    public double getGoldAmount() {
        return goldAmount;
    }

    /**
     * 確認状態を取得
     *
     * @return 確認状態
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * オファーが有効かチェック
     *
     * @return アイテムまたはゴールドが含まれている場合true
     */
    public boolean hasOffer() {
        return !items.isEmpty() || goldAmount > 0;
    }

    /**
     * オファーをリセット
     */
    public void reset() {
        items.clear();
        goldAmount = 0.0;
        confirmed = false;
    }

    @Override
    public String toString() {
        return String.format("TradeOffer{player=%s, items=%d, gold=%.2f, confirmed=%s}",
            playerUuid, items.size(), goldAmount, confirmed);
    }
}
