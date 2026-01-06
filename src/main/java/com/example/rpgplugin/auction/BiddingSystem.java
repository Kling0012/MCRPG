package com.example.rpgplugin.auction;

import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 入札システム
 * 入札ルールの適用と入札処理を担当
 */
public class BiddingSystem {

    /**
     * 入札を処理
     *
     * @param bidder 入札者
     * @param auction オークション
     * @param amount 入札額
     * @return 入札結果
     */
    public BidResult placeBid(Player bidder, Auction auction, double amount) {
        // ルール1: オークションが有効か確認
        if (!auction.isActive()) {
            return new BidResult(Auction.BidResult.FAIL_AUCTION_NOT_ACTIVE,
                    0, "このオークションは無効です");
        }

        if (auction.isExpired()) {
            return new BidResult(Auction.BidResult.FAIL_AUCTION_ENDED,
                    0, "このオークションは終了しました");
        }

        // ルール2: 自分自身のアイテムに入札できない
        if (auction.getSellerUuid().equals(bidder.getUniqueId())) {
            return new BidResult(Auction.BidResult.FAIL_SELF_BID,
                    0, "自分の出品アイテムに入札できません");
        }

        // ルール3: 最低価格以上であること
        if (amount < auction.getStartingPrice()) {
            return new BidResult(Auction.BidResult.FAIL_BELOW_STARTING,
                    auction.getStartingPrice(),
                    String.format("開始価格%.2f以上である必要があります", auction.getStartingPrice()));
        }

        // ルール4: 10%以上上乗せであること
        double minNextBid = auction.getMinimumNextBid();
        if (amount < minNextBid) {
            return new BidResult(Auction.BidResult.FAIL_TOO_LOW,
                    minNextBid,
                    String.format("現在の入札額の10%%以上上乗せする必要があります (最低: %.2f)", minNextBid));
        }

        // 入札確定
        auction.placeBid(bidder.getUniqueId(), amount);

        // ルール5: 入札期間延長（+5秒）
        // 注意: 実際の延長処理はAuctionManagerで行う
        LocalDateTime extendedExpiresAt = auction.getExpiresAt().plusSeconds(5);

        // 成功
        return new BidResult(Auction.BidResult.SUCCESS,
                amount,
                String.format("入札成功！%.2fで入札しました（有効期限+5秒延長）", amount),
                extendedExpiresAt);
    }

    /**
     * 入札結果クラス
     */
    public static class BidResult {
        private final Auction.BidResult result;
        private final double requiredAmount;
        private final String message;
        private final LocalDateTime extendedExpiresAt;

        public BidResult(Auction.BidResult result, double requiredAmount, String message) {
            this(result, requiredAmount, message, null);
        }

        public BidResult(Auction.BidResult result, double requiredAmount, String message,
                         LocalDateTime extendedExpiresAt) {
            this.result = result;
            this.requiredAmount = requiredAmount;
            this.message = message;
            this.extendedExpiresAt = extendedExpiresAt;
        }

        public boolean isSuccess() {
            return result == Auction.BidResult.SUCCESS;
        }

        public Auction.BidResult getResult() {
            return result;
        }

        public double getRequiredAmount() {
            return requiredAmount;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getExtendedExpiresAt() {
            return extendedExpiresAt;
        }
    }
}
