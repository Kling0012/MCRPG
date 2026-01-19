package com.example.rpgplugin.player.manager;

import com.example.rpgplugin.player.data.PlayerDataContainer;
import com.example.rpgplugin.stats.ManaManager;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * プレイヤーステータスマネージャー
 *
 * <p>ステータス計算と管理を担当します。</p>
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>Compositeパターン: StatManagerとManaManagerを統合</li>
 *   <li>Facadeパターン: ステータス操作への統一インターフェース</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ステータス管理に特化</li>
 *   <li>DRY: 計算ロジックを一元管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 2.0.0
 */
public class PlayerStatManager {

    private final UUID uuid;
    private final PlayerDataContainer dataContainer;
    private final StatManager statManager;
    private final ManaManager manaManager;
    private final Supplier<Player> playerSupplier;

    /**
     * コンストラクタ
     *
     * @param uuid プレイヤーUUID
     * @param dataContainer データコンテナ
     * @param statManager ステータスマネージャー
     * @param manaManager マナマネージャー
     * @param playerSupplier プレイヤー取得サプライヤー
     */
    public PlayerStatManager(
            UUID uuid,
            PlayerDataContainer dataContainer,
            StatManager statManager,
            ManaManager manaManager,
            Supplier<Player> playerSupplier) {
        this.uuid = uuid;
        this.dataContainer = dataContainer;
        this.statManager = statManager;
        this.manaManager = manaManager;
        this.playerSupplier = playerSupplier;
    }

    // ==================== StatManager委譲 ====================

    /**
     * ステータスマネージャーを取得します
     *
     * @return ステータスマネージャー
     */
    public StatManager getStatManager() {
        return statManager;
    }

    /**
     * 基本ステータスを取得します
     *
     * @param stat ステータス種別
     * @return 基本値
     */
    public int getBaseStat(Stat stat) {
        return statManager.getBaseStat(stat);
    }

    /**
     * 基本ステータスを設定します
     *
     * @param stat ステータス種別
     * @param value 基本値
     */
    public void setBaseStat(Stat stat, int value) {
        statManager.setBaseStat(stat, value);
    }

    /**
     * 最終ステータスを取得します
     *
     * @param stat ステータス種別
     * @return 最終値
     */
    public int getFinalStat(Stat stat) {
        return statManager.getFinalStat(stat);
    }

    /**
     * 手動配分ポイントを取得します
     *
     * @return 手動配分ポイント
     */
    public int getAvailablePoints() {
        return statManager.getAvailablePoints();
    }

    /**
     * 手動配分ポイントを設定します
     *
     * @param points 手動配分ポイント
     */
    public void setAvailablePoints(int points) {
        statManager.setAvailablePoints(points);
    }

    /**
     * 手動配分ポイントを追加します
     *
     * @param amount 追加するポイント数
     */
    public void addAvailablePoints(int amount) {
        statManager.addAvailablePoints(amount);
    }

    /**
     * ステータスに手動配分ポイントを割り振ります
     *
     * @param stat ステータス種別
     * @param amount 割り振るポイント数
     * @return 割り振りに成功した場合はtrue
     */
    public boolean allocatePoint(Stat stat, int amount) {
        return statManager.allocatePoint(stat, amount);
    }

    /**
     * ステータス配分をリセットします
     *
     * @return 返却されたポイント数
     */
    public int resetAllocation() {
        return statManager.resetAllocation();
    }

    /**
     * ステータス情報をフォーマットして返します
     *
     * @return フォーマットされたステータス情報
     */
    public String formatStats() {
        return statManager.formatStats();
    }

    // ==================== ManaManager委譲 ====================

    /**
     * マナマネージャーを取得します
     *
     * @return マナマネージャー
     */
    public ManaManager getManaManager() {
        return manaManager;
    }

    /**
     * 最大HP修飾子を取得します
     *
     * @return 最大HP修飾子
     */
    public int getMaxHealthModifier() {
        return manaManager.getMaxHpModifier();
    }

    /**
     * 最大HP修飾子を設定します
     *
     * @param modifier 最大HP修飾子
     */
    public void setMaxHealthModifier(int modifier) {
        manaManager.setMaxHpModifier(modifier);
        dataContainer.setMaxHealth(modifier);
        // オンラインの場合はBukkitのHPも更新
        Player player = getPlayer();
        if (player != null) {
            double baseMaxHealth = 20.0;
            double newMaxHealth = baseMaxHealth + modifier;
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);
        }
    }

    /**
     * 最大MPを取得します
     *
     * @return 最大MP
     */
    public int getMaxMana() {
        return manaManager.getMaxMana();
    }

    /**
     * 最大MPを設定します
     *
     * @param maxMana 最大MP
     */
    public void setMaxMana(int maxMana) {
        manaManager.setMaxMana(maxMana);
        dataContainer.setMaxMana(maxMana);
    }

    /**
     * 現在MPを取得します
     *
     * @return 現在MP
     */
    public int getCurrentMana() {
        return manaManager.getCurrentMana();
    }

    /**
     * 現在MPを設定します
     *
     * @param currentMana 現在MP
     */
    public void setCurrentMana(int currentMana) {
        manaManager.setCurrentMana(currentMana);
        dataContainer.setCurrentMana(currentMana);
    }

    /**
     * MPを追加します
     *
     * @param amount 追加するMP量
     * @return 実際に追加されたMP量
     */
    public int addMana(int amount) {
        int actualAdd = manaManager.addMana(amount);
        dataContainer.setCurrentMana(manaManager.getCurrentMana());
        return actualAdd;
    }

    /**
     * MPを消費します
     *
     * @param amount 消費するMP量
     * @return 消費に成功した場合はtrue、MP不足の場合はfalse
     */
    public boolean consumeMana(int amount) {
        boolean success = manaManager.consumeMana(amount);
        if (success) {
            dataContainer.setCurrentMana(manaManager.getCurrentMana());
        }
        return success;
    }

    /**
     * MPが足りているか確認します
     *
     * @param amount 必要なMP量
     * @return MPが足りている場合はtrue
     */
    public boolean hasMana(int amount) {
        return manaManager.hasMana(amount);
    }

    /**
     * MPが満タンか確認します
     *
     * @return 満タンの場合はtrue
     */
    public boolean isFullMana() {
        return manaManager.isFullMana();
    }

    /**
     * MPが空か確認します
     *
     * @return 空の場合はtrue
     */
    public boolean isEmptyMana() {
        return manaManager.isEmptyMana();
    }

    /**
     * MPの割合を取得します
     *
     * @return MPの割合（0.0～1.0）
     */
    public double getManaRatio() {
        return manaManager.getManaRatio();
    }

    /**
     * MPを回復します
     *
     * <p>精神値（SPI）に基づいて回復量を計算します。</p>
     *
     * @param baseRegene 基礎回復量
     * @return 実際に回復したMP量
     */
    public int regenerateMana(double baseRegene) {
        int spiritValue = getFinalStat(Stat.SPIRIT);
        int actualRegen = manaManager.regenerateMana(spiritValue, baseRegene);
        dataContainer.setCurrentMana(manaManager.getCurrentMana());
        return actualRegen;
    }

    /**
     * コストタイプを取得します
     *
     * @return コストタイプ
     */
    public ManaManager.CostType getCostType() {
        return manaManager.getCostType();
    }

    /**
     * コストタイプを設定します
     *
     * @param costType コストタイプ
     */
    public void setCostType(ManaManager.CostType costType) {
        manaManager.setCostType(costType);
        dataContainer.setCostType(costType.getId());
    }

    /**
     * コストタイプがMPか確認します
     *
     * @return MP消費モードの場合はtrue
     */
    public boolean isManaCostType() {
        return manaManager.getCostType() == ManaManager.CostType.MANA;
    }

    /**
     * コストタイプを切り替えます
     *
     * @return 新しいコストタイプ
     */
    public ManaManager.CostType toggleCostType() {
        manaManager.toggleCostType();
        ManaManager.CostType newType = manaManager.getCostType();
        dataContainer.setCostType(newType.getId());
        return newType;
    }

    /**
     * スキル発動時のコストを消費します
     *
     * <p>現在のコストタイプに応じてMPまたはHPを消費します。</p>
     *
     * @param amount コスト量
     * @return 消費に成功した場合はtrue、リソース不足の場合はfalse
     */
    public boolean consumeSkillCost(int amount) {
        if (isManaCostType()) {
            return consumeMana(amount);
        } else {
            // HP消費モード
            Player player = getPlayer();
            if (player != null) {
                double currentHp = player.getHealth();
                if (currentHp <= amount) {
                    return false;
                }
                player.setHealth(currentHp - amount);
                return true;
            }
            return false;
        }
    }

    /**
     * MP情報をフォーマットして返します
     *
     * @return フォーマットされたMP情報
     */
    public String formatManaInfo() {
        return manaManager.formatManaInfo();
    }

    /**
     * ManaManagerの状態をPlayerDataに同期します
     *
     * <p>データベース保存前に呼び出して、メモリ上のMP状態をPlayerDataに反映します。</p>
     */
    public void syncManaToData() {
        dataContainer.setMaxMana(manaManager.getMaxMana());
        dataContainer.setCurrentMana(manaManager.getCurrentMana());
        dataContainer.setMaxHealth(manaManager.getMaxHpModifier());
        dataContainer.setCostType(manaManager.getCostType().getId());
    }

    // ==================== ユーティリティ ====================

    /**
     * Bukkitプレイヤーを取得します
     *
     * @return プレイヤー、オフラインの場合はnull
     */
    private Player getPlayer() {
        return playerSupplier.get();
    }

    /**
     * UUIDを取得します
     *
     * @return プレイヤーUUID
     */
    public UUID getUuid() {
        return uuid;
    }
}
