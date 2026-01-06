package com.example.rpgplugin.auction;

import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * オークション出品データクラス
 * 新規出品時に使用するデータ構造
 */
public class AuctionListing {

    private final UUID sellerUuid;
    private final String sellerName;
    private final ItemStack item;
    private final double startingPrice;
    private final int durationSeconds;
    private final LocalDateTime createdAt;

    /**
     * コンストラクタ
     *
     * @param sellerUuid 出品者UUID
     * @param sellerName 出品者名
     * @param item アイテム
     * @param startingPrice 開始価格
     * @param durationSeconds 継続時間（秒）
     */
    public AuctionListing(UUID sellerUuid, String sellerName, ItemStack item,
                          double startingPrice, int durationSeconds) {
        if (startingPrice <= 0) {
            throw new IllegalArgumentException("開始価格は0より大きい必要があります");
        }
        if (durationSeconds < 30 || durationSeconds > 180) {
            throw new IllegalArgumentException("継続時間は30-180秒である必要があります");
        }
        if (item == null) {
            throw new IllegalArgumentException("アイテムはnullであってはなりません");
        }

        this.sellerUuid = sellerUuid;
        this.sellerName = sellerName;
        this.item = item.clone();
        this.startingPrice = startingPrice;
        this.durationSeconds = durationSeconds;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 有効期限を計算
     *
     * @return 有効期限
     */
    public LocalDateTime calculateExpiration() {
        return createdAt.plusSeconds(durationSeconds);
    }

    // Getters
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

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
