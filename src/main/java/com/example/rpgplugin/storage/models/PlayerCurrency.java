package com.example.rpgplugin.storage.models;

import java.util.UUID;

/**
 * プレイヤー通貨データモデル
 */
public class PlayerCurrency {

    private final UUID uuid;
    private double goldBalance;
    private double totalEarned;
    private double totalSpent;

    /**
     * コンストラクタ
     *
     * @param uuid プレイヤーUUID
     */
    public PlayerCurrency(UUID uuid) {
        this.uuid = uuid;
        this.goldBalance = 0.0;
        this.totalEarned = 0.0;
        this.totalSpent = 0.0;
    }

    /**
     * データベースからロードするための完全コンストラクタ
     */
    public PlayerCurrency(UUID uuid, double goldBalance, double totalEarned, double totalSpent) {
        this.uuid = uuid;
        this.goldBalance = goldBalance;
        this.totalEarned = totalEarned;
        this.totalSpent = totalSpent;
    }

    public UUID getUuid() {
        return uuid;
    }

    public double getGoldBalance() {
        return goldBalance;
    }

    public void setGoldBalance(double goldBalance) {
        this.goldBalance = goldBalance;
    }

    public double getTotalEarned() {
        return totalEarned;
    }

    public void setTotalEarned(double totalEarned) {
        this.totalEarned = totalEarned;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    /**
     * ゴールドを加算
     *
     * @param amount 加算量
     */
    public void deposit(double amount) {
        this.goldBalance += amount;
        this.totalEarned += amount;
    }

    /**
     * ゴールドを減算
     *
     * @param amount 減算量
     * @return 残高が足りている場合はtrue
     */
    public boolean withdraw(double amount) {
        if (this.goldBalance < amount) {
            return false;
        }
        this.goldBalance -= amount;
        this.totalSpent += amount;
        return true;
    }

    /**
     * ゴールド残高が足りているか確認
     *
     * @param amount 必要な金額
     * @return 残高が足りている場合はtrue
     */
    public boolean hasEnough(double amount) {
        return this.goldBalance >= amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerCurrency that = (PlayerCurrency) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return "PlayerCurrency{" +
                "uuid=" + uuid +
                ", goldBalance=" + goldBalance +
                ", totalEarned=" + totalEarned +
                ", totalSpent=" + totalSpent +
                '}';
    }
}
