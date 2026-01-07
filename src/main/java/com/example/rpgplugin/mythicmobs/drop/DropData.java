package com.example.rpgplugin.mythicmobs.drop;

import com.example.rpgplugin.storage.models.Serializable;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * MythicMobsドロップデータモデル
 *
 * <p>MythicMobsがドロップしたアイテムの情報を保持します。
 * 独占ドロップと期限管理機能を提供します。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class DropData implements Serializable {

    private int id;
    private UUID playerUuid;
    private String mobId;
    private String itemData;
    private long droppedAt;
    private boolean claimed;
    private Long expiresAt;

    /**
     * デフォルトコンストラクタ
     */
    public DropData() {
        this.droppedAt = System.currentTimeMillis() / 1000;
        this.claimed = false;
    }

    /**
     * コンストラクタ
     *
     * @param playerUuid プレイヤーUUID
     * @param mobId モブID
     * @param itemData アイテムデータ（Base64シリアライズ済み）
     * @param expirationSeconds 独占有効期限（秒）
     */
    public DropData(UUID playerUuid, String mobId, String itemData, long expirationSeconds) {
        this();
        this.playerUuid = playerUuid;
        this.mobId = mobId;
        this.itemData = itemData;
        if (expirationSeconds > 0) {
            this.expiresAt = (System.currentTimeMillis() / 1000) + expirationSeconds;
        }
    }

    /**
     * IDを取得します
     *
     * @return データベース上のID
     */
    public int getId() {
        return id;
    }

    /**
     * IDを設定します
     *
     * @param id データベース上のID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * プレイヤーUUIDを取得します
     *
     * @return プレイヤーUUID
     */
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    /**
     * プレイヤーUUIDを設定します
     *
     * @param playerUuid プレイヤーUUID
     */
    public void setPlayerUuid(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    /**
     * モブIDを取得します
     *
     * @return モブID
     */
    public String getMobId() {
        return mobId;
    }

    /**
     * モブIDを設定します
     *
     * @param mobId モブID
     */
    public void setMobId(String mobId) {
        this.mobId = mobId;
    }

    /**
     * アイテムデータを取得します
     *
     * @return Base64シリアライズされたアイテムデータ
     */
    public String getItemData() {
        return itemData;
    }

    /**
     * アイテムデータを設定します
     *
     * @param itemData Base64シリアライズされたアイテムデータ
     */
    public void setItemData(String itemData) {
        this.itemData = itemData;
    }

    /**
     * ドロップ日時を取得します
     *
     * @return ドロップ日時（Unixタイムスタンプ、秒）
     */
    public long getDroppedAt() {
        return droppedAt;
    }

    /**
     * ドロップ日時を設定します
     *
     * @param droppedAt ドロップ日時（Unixタイムスタンプ、秒）
     */
    public void setDroppedAt(long droppedAt) {
        this.droppedAt = droppedAt;
    }

    /**
     * ドロップが既に回収されたか確認します
     *
     * @return 回収済みの場合はtrue
     */
    public boolean isClaimed() {
        return claimed;
    }

    /**
     * 回収状態を設定します
     *
     * @param claimed 回収済みの場合はtrue
     */
    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }

    /**
     * 期限切れ日時を取得します
     *
     * @return 期限切れ日時（Unixタイムスタンプ、秒）、期限なしの場合はnull
     */
    public Long getExpiresAt() {
        return expiresAt;
    }

    /**
     * 期限切れ日時を設定します
     *
     * @param expiresAt 期限切れ日時（Unixタイムスタンプ、秒）、期限なしの場合はnull
     */
    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * ドロップが期限切れか確認します
     *
     * @return 期限切れの場合はtrue、期限なしまたは有効期限内の場合はfalse
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis() / 1000;
        return currentTime > expiresAt;
    }

    /**
     * ドロップが指定したプレイヤーに取得可能か確認します
     *
     * @param playerUuid チェックするプレイヤーUUID
     * @return 取得可能な場合はtrue
     */
    public boolean canPickup(UUID playerUuid) {
        // 既に回収済みの場合は取得不可
        if (claimed) {
            return false;
        }

        // 期限切れの場合は誰でも取得可能
        if (isExpired()) {
            return true;
        }

        // 有効期限内は所有者のみ取得可能
        return this.playerUuid.equals(playerUuid);
    }

    /**
     * 残り有効期限（秒）を取得します
     *
     * @return 残り秒数、期限なしまたは期限切れの場合は0
     */
    public long getRemainingSeconds() {
        if (expiresAt == null) {
            return 0;
        }
        long currentTime = System.currentTimeMillis() / 1000;
        long remaining = expiresAt - currentTime;
        return Math.max(0, remaining);
    }

    @Override
    public String toString() {
        return "DropData{" +
                "id=" + id +
                ", playerUuid=" + playerUuid +
                ", mobId='" + mobId + '\'' +
                ", droppedAt=" + droppedAt +
                ", claimed=" + claimed +
                ", expiresAt=" + expiresAt +
                ", expired=" + isExpired() +
                '}';
    }

    @Override
    public String serialize() {
        // JSON形式でシリアライズ
        return String.format(
            "{\"id\":%d,\"playerUuid\":\"%s\",\"mobId\":\"%s\",\"itemData\":\"%s\",\"droppedAt\":%d,\"claimed\":%b,\"expiresAt\":%s}",
            id, playerUuid, mobId, itemData, droppedAt, claimed,
            expiresAt != null ? expiresAt : "null"
        );
    }

    @Override
    public void deserialize(String data) {
        // JSON形式からデシリアライズ（簡易実装）
        if (data == null || data.isEmpty()) {
            return;
        }
        // TODO: 完全なJSONパースの実装
        // 暫定的に、各フィールドを抽出
        String[] parts = data.split(",");
        for (String part : parts) {
            String[] keyValue = part.split(":");
            if (keyValue.length != 2) continue;

            String key = keyValue[0].replaceAll("[\"{}]", "").trim();
            String value = keyValue[1].replaceAll("[\"{}]", "").trim();

            switch (key) {
                case "id" -> id = Integer.parseInt(value);
                case "playerUuid" -> playerUuid = UUID.fromString(value);
                case "mobId" -> mobId = value;
                case "itemData" -> itemData = value;
                case "droppedAt" -> droppedAt = Long.parseLong(value);
                case "claimed" -> claimed = Boolean.parseBoolean(value);
                case "expiresAt" -> {
                    if (!value.equals("null")) {
                        expiresAt = Long.parseLong(value);
                    }
                }
            }
        }
    }
}
