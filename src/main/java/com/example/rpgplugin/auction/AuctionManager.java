package com.example.rpgplugin.auction;

import com.example.rpgplugin.storage.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * オークション管理クラス
 * オークションのライフサイクルとデータアクセスを管理
 */
public class AuctionManager {

    private final Logger logger;
    private final DatabaseManager dbManager;
    private final BiddingSystem biddingSystem;
    private final Map<Integer, Auction> activeAuctions;
    private final Map<Integer, LocalDateTime> extendedExpirations;

    /**
     * コンストラクタ
     *
     * @param logger ロガー
     * @param dbManager データベースマネージャー
     */
    public AuctionManager(Logger logger, DatabaseManager dbManager) {
        this.logger = logger;
        this.dbManager = dbManager;
        this.biddingSystem = new BiddingSystem();
        this.activeAuctions = new ConcurrentHashMap<>();
        this.extendedExpirations = new ConcurrentHashMap<>();
    }

    /**
     * オークションを出品
     *
     * @param listing 出品データ
     * @return 作成されたオークション、失敗時null
     */
    public Auction createAuction(AuctionListing listing) {
        try {
            Connection conn = dbManager.getConnection();
            if (conn == null) {
                logger.severe("データベース接続の取得に失敗しました");
                return null;
            }
            try (Connection connection = conn) {
                // オークションを作成
                String sql = """
                    INSERT INTO auction_listings (
                        seller_uuid, seller_name, item_data, starting_price,
                        duration_seconds, created_at, expires_at, is_active
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

                try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, listing.getSellerUuid().toString());
                    stmt.setString(2, listing.getSellerName());
                    stmt.setString(3, serializeItemStack(listing.getItem()));
                    stmt.setDouble(4, listing.getStartingPrice());
                    stmt.setInt(5, listing.getDurationSeconds());
                    stmt.setLong(6, listing.getCreatedAt().atZone(ZoneId.systemDefault()).toEpochSecond());
                    stmt.setLong(7, listing.calculateExpiration().atZone(ZoneId.systemDefault()).toEpochSecond());
                    stmt.setBoolean(8, true);

                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows == 0) {
                        logger.severe("オークション作成に失敗: 行が挿入されませんでした");
                        return null;
                    }

                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int auctionId = generatedKeys.getInt(1);

                            Auction auction = new Auction(
                                    auctionId,
                                    listing.getSellerUuid(),
                                    listing.getSellerName(),
                                    listing.getItem(),
                                    listing.getStartingPrice(),
                                    listing.getCreatedAt(),
                                    listing.calculateExpiration()
                            );

                            activeAuctions.put(auctionId, auction);
                            logger.info(String.format("オークションを作成しました: ID=%d, 出品者=%s, 価格=%.2f",
                                    auctionId, listing.getSellerName(), listing.getStartingPrice()));

                            return auction;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("オークション作成中にエラー: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 入札を処理
     *
     * @param player 入札プレイヤー
     * @param auctionId オークションID
     * @param amount 入札額
     * @return 入札結果
     */
    public BiddingSystem.BidResult placeBid(Player player, int auctionId, double amount) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            return new BiddingSystem.BidResult(
                    Auction.BidResult.FAIL_AUCTION_NOT_ACTIVE,
                    0,
                    "オークションが見つかりません"
            );
        }

        // 入札処理
        BiddingSystem.BidResult result = biddingSystem.placeBid(player, auction, amount);

        if (result.isSuccess()) {
            // 入札をデータベースに記録
            saveBidToDatabase(auctionId, player.getUniqueId(), amount);

            // 有効期限延長を適用
            if (result.getExtendedExpiresAt() != null) {
                extendedExpirations.put(auctionId, result.getExtendedExpiresAt());
                updateExpirationInDatabase(auctionId, result.getExtendedExpiresAt());
            }

            logger.info(String.format("入札成功: プレイヤー=%s, オークションID=%d, 金額=%.2f",
                    player.getName(), auctionId, amount));
        }

        return result;
    }

    /**
     * すべてのアクティブなオークションを取得
     *
     * @return アクティブなオークションリスト
     */
    public List<Auction> getActiveAuctions() {
        return new ArrayList<>(activeAuctions.values());
    }

    /**
     * オークションIDで取得
     *
     * @param auctionId オークションID
     * @return オークション、存在しない場合はnull
     */
    public Auction getAuction(int auctionId) {
        return activeAuctions.get(auctionId);
    }

    /**
     * オークションをキャンセル
     *
     * @param auctionId オークションID
     * @param cancellerUuid キャンセル者UUID
     * @return 成功時true
     */
    public boolean cancelAuction(int auctionId, UUID cancellerUuid) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            return false;
        }

        // 出品者のみキャンセル可能
        if (!auction.getSellerUuid().equals(cancellerUuid)) {
            return false;
        }

        auction.setActive(false);
        updateAuctionStatusInDatabase(auctionId, false);
        activeAuctions.remove(auctionId);

        logger.info(String.format("オークションをキャンセル: ID=%d", auctionId));
        return true;
    }

    /**
     * 期限切れのオークションをチェックして処理
     */
    public void checkExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Integer> expiredIds = new ArrayList<>();

        for (Map.Entry<Integer, Auction> entry : activeAuctions.entrySet()) {
            int auctionId = entry.getKey();
            Auction auction = entry.getValue();

            // 延長された有効期限を使用
            LocalDateTime expiration = extendedExpirations.getOrDefault(auctionId, auction.getExpiresAt());

            if (now.isAfter(expiration)) {
                expiredIds.add(auctionId);
            }
        }

        // 期限切れオークションを処理
        for (int auctionId : expiredIds) {
            Auction auction = activeAuctions.get(auctionId);
            if (auction != null) {
                processExpiredAuction(auction);
                activeAuctions.remove(auctionId);
                extendedExpirations.remove(auctionId);
            }
        }
    }

    /**
     * 期限切れオークションを処理（勝者へのアイテム付与）
     *
     * @param auction オークション
     */
    private void processExpiredAuction(Auction auction) {
        auction.setActive(false);
        updateAuctionStatusInDatabase(auction.getId(), false);

        UUID winnerUuid = auction.getCurrentBidder();
        if (winnerUuid != null) {
            // 勝者にアイテムを付与
            Player winner = Bukkit.getPlayer(winnerUuid);
            if (winner != null && winner.isOnline()) {
                winner.getInventory().addItem(auction.getItem());
                winner.sendMessage("§a§lオークション勝利！§r§f" + auction.getItem().getType().name() + "を入手しました");
                logger.info(String.format("オークション終了: ID=%d, 勝者=%s, 金額=%.2f",
                        auction.getId(), winner.getName(), auction.getCurrentBid()));
            } else {
                // オフラインプレイヤーの場合、データベースに記録（TODO: 別途実装）
                logger.info(String.format("オークション終了（オフライン）: ID=%d, 勝者UUID=%s",
                        auction.getId(), winnerUuid));
            }
        } else {
            // 入札なしの場合、出品者に返還
            Player seller = Bukkit.getPlayer(auction.getSellerUuid());
            if (seller != null && seller.isOnline()) {
                seller.getInventory().addItem(auction.getItem());
                seller.sendMessage("§eオークション終了（入札なし）: アイテムを返還しました");
            }
        }
    }

    /**
     * データベースからオークションをロード
     */
    public void loadActiveAuctions() {
        String sql = """
            SELECT id, seller_uuid, seller_name, item_data, starting_price,
                   current_bid, current_bidder, duration_seconds,
                   created_at, expires_at, is_active
            FROM auction_listings
            WHERE is_active = true
            AND expires_at > strftime('%s', 'now')
        """;

        try {
            Connection conn = dbManager.getConnection();
            if (conn == null) {
                logger.severe("データベース接続の取得に失敗しました（オークションロード）");
                return;
            }
            try (Connection connection = conn;
                 PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    int id = rs.getInt("id");
                    UUID sellerUuid;
                    try {
                        sellerUuid = UUID.fromString(rs.getString("seller_uuid"));
                    } catch (IllegalArgumentException e) {
                        logger.severe("無効なseller_uuid: " + rs.getString("seller_uuid"));
                        continue;
                    }
                    String sellerName = rs.getString("seller_name");
                    ItemStack item = deserializeItemStack(rs.getString("item_data"));
                    double startingPrice = rs.getDouble("starting_price");
                    double currentBid = rs.getDouble("current_bid");
                    String bidderStr = rs.getString("current_bidder");

                    long createdAtEpoch = rs.getLong("created_at");
                    long expiresAtEpoch = rs.getLong("expires_at");

                    LocalDateTime createdAt = LocalDateTime.ofEpochSecond(createdAtEpoch, 0, ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now()));
                    LocalDateTime expiresAt = LocalDateTime.ofEpochSecond(expiresAtEpoch, 0, ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now()));

                    Auction auction = new Auction(id, sellerUuid, sellerName, item, startingPrice, createdAt, expiresAt);

                    if (bidderStr != null) {
                        try {
                            auction.placeBid(UUID.fromString(bidderStr), currentBid);
                        } catch (IllegalArgumentException e) {
                            logger.warning("無効なbidder_uuidをスキップ: " + bidderStr);
                        }
                    }

                    activeAuctions.put(id, auction);
                }

                logger.info("アクティブなオークションをロード: " + activeAuctions.size() + "件");
            }
        } catch (SQLException e) {
            logger.severe("オークションロード中にエラー: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ItemStackをシリアライズ
     *
     * @param item アイテム
     * @return シリアライズされた文字列
     */
    private String serializeItemStack(ItemStack item) {
        try {
            // BukkitのItemStackシリアライズ機能を使用
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            java.io.ByteArrayOutputStream byteOut = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream objectOut = new java.io.ObjectOutputStream(byteOut);

            // ItemStackをBase64エンコード
            java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(buf);
            oos.writeObject(item.serialize());
            oos.flush();

            return java.util.Base64.getEncoder().encodeToString(buf.toByteArray());
        } catch (Exception e) {
            logger.severe("ItemStackシリアライズ中にエラー: " + e.getMessage());
            return "";
        }
    }

    /**
     * ItemStackをデシリアライズ
     *
     * @param data シリアライズされた文字列
     * @return アイテム
     */
    private ItemStack deserializeItemStack(String data) {
        try {
            // Base64デコード
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(data);

            java.io.ByteArrayInputStream byteIn = new java.io.ByteArrayInputStream(decodedBytes);
            java.io.ObjectInputStream objectIn = new java.io.ObjectInputStream(byteIn);

            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> serialized = (java.util.Map<String, Object>) objectIn.readObject();

            return ItemStack.deserialize(serialized);
        } catch (Exception e) {
            logger.severe("ItemStackデシリアライズ中にエラー: " + e.getMessage());
            // エラー時はダミーアイテムを返す
            return new ItemStack(org.bukkit.Material.DIAMOND);
        }
    }

    /**
     * 入札をデータベースに保存
     */
    private void saveBidToDatabase(int auctionId, UUID bidderUuid, double amount) {
        dbManager.executeAsync(() -> {
            try {
                Connection conn = dbManager.getConnection();
                if (conn == null) {
                    logger.warning("データベース接続の取得に失敗しました（入札保存）");
                    return;
                }
                try (Connection connection = conn) {
                    String sql = """
                        UPDATE auction_listings
                        SET current_bid = ?, current_bidder = ?
                        WHERE id = ?
                    """;
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setDouble(1, amount);
                        stmt.setString(2, bidderUuid.toString());
                        stmt.setInt(3, auctionId);
                        stmt.executeUpdate();
                    }

                    // 入札履歴にも記録
                    String bidHistorySql = """
                        INSERT INTO auction_bids (auction_id, bidder_uuid, bid_amount)
                        VALUES (?, ?, ?)
                    """;
                    try (PreparedStatement stmt = connection.prepareStatement(bidHistorySql)) {
                        stmt.setInt(1, auctionId);
                        stmt.setString(2, bidderUuid.toString());
                        stmt.setDouble(3, amount);
                        stmt.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                logger.severe("入札保存中にエラー: " + e.getMessage());
            }
        });
    }

    /**
     * 有効期限をデータベースで更新
     */
    private void updateExpirationInDatabase(int auctionId, LocalDateTime newExpiration) {
        dbManager.executeAsync(() -> {
            try {
                Connection conn = dbManager.getConnection();
                if (conn == null) {
                    logger.warning("データベース接続の取得に失敗しました（有効期限更新）");
                    return;
                }
                try (Connection connection = conn) {
                    String sql = "UPDATE auction_listings SET expires_at = ? WHERE id = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setLong(1, newExpiration.atZone(ZoneId.systemDefault()).toEpochSecond());
                        stmt.setInt(2, auctionId);
                        stmt.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                logger.severe("有効期限更新中にエラー: " + e.getMessage());
            }
        });
    }

    /**
     * オークションステータスをデータベースで更新
     */
    private void updateAuctionStatusInDatabase(int auctionId, boolean isActive) {
        dbManager.executeAsync(() -> {
            try {
                Connection conn = dbManager.getConnection();
                if (conn == null) {
                    logger.warning("データベース接続の取得に失敗しました（ステータス更新）");
                    return;
                }
                try (Connection connection = conn) {
                    String sql = "UPDATE auction_listings SET is_active = ? WHERE id = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setBoolean(1, isActive);
                        stmt.setInt(2, auctionId);
                        stmt.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                logger.severe("ステータス更新中にエラー: " + e.getMessage());
            }
        });
    }
}
