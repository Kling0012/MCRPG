package com.example.rpgplugin.auction;

import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * オークションデータクラス
 * 単一のオークションアイテムを表現
 */
public class Auction {

    private final int id;
    private final UUID sellerUuid;
    private final String sellerName;
    private final ItemStack item;
    private final double startingPrice;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private double currentBid;
    private UUID currentBidder;
    private LocalDateTime lastBidTime;
    private boolean isActive;

    /**
     * コンストラクタ
     *
     * @param id オークションID
     * @param sellerUuid 出品者UUID
     * @param sellerName 出品者名
     * @param item アイテム
     * @param startingPrice 開始価格
     * @param createdAt 作成日時
     * @param expiresAt 有効期限
     */
    public Auction(int id, UUID sellerUuid, String sellerName, ItemStack item,
                   double startingPrice, LocalDateTime createdAt, LocalDateTime expiresAt) {
        if (item == null) {
            throw new IllegalArgumentException("アイテムはnullであってはなりません");
        }
        this.id = id;
        this.sellerUuid = sellerUuid;
        this.sellerName = sellerName;
        this.item = item.clone();
        this.startingPrice = startingPrice;
        this.currentBid = startingPrice;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.isActive = true;
    }

    /**
     * 入札を配置
     *
     * @param bidderUuid 入札者UUID
     * @param amount 入札額
     */
    public void placeBid(UUID bidderUuid, double amount) {
        this.currentBid = amount;
        this.currentBidder = bidderUuid;
        this.lastBidTime = LocalDateTime.now();
    }

    /**
     * オークション期間を延長
     *
     * @param seconds 延長秒数
     */
    public void extendDuration(int seconds) {
        // 現在の有効期限から延長
        // 注意: このメソッドは不変オブジェクトパターンを考慮し、
        // 実際の延長処理はAuctionManagerで行う
    }

    /**
     * 最低次回入札額を計算（現在の入札額の110%）
     *
     * @return 最低次回入札額
     */
    public double getMinimumNextBid() {
        return currentBid * 1.1;
    }

    /**
     * 入札が有効か確認
     *
     * @param amount 入札額
     * @return 有効な場合true
     */
    public boolean isValidBidAmount(double amount) {
        // 開始価格以上であること
        if (amount < startingPrice) {
            return false;
        }

        // 現在の入札額の10%以上上乗せであること
        double minNextBid = getMinimumNextBid();
        return amount >= minNextBid;
    }

    /**
     * オークションが期限切れか確認
     *
     * @return 期限切れの場合true
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // Getters
    public int getId() {
        return id;
    }

    public UUID getSellerUuid() {
        return sellerUuid;
    }

    public String getSellerName() {
        return sellerName;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public double getCurrentBid() {
        return currentBid;
    }

    public UUID getCurrentBidder() {
        return currentBidder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getLastBidTime() {
        return lastBidTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Bid結果列挙型
     */
    public enum BidResult {
        SUCCESS,
        FAIL_BELOW_STARTING,
        FAIL_TOO_LOW,
        FAIL_AUCTION_ENDED,
        FAIL_AUCTION_NOT_ACTIVE,
        FAIL_SELF_BID
    }
}
