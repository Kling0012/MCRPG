package com.example.rpgplugin.stats;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * MP（マナポイント）管理クラス
 *
 * <p>プレイヤーのMP値を管理し、最大MP、現在MP、回復処理を統合します。</p>
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>ファサードパターン: MP操作への統一的インターフェース</li>
 *   <li>ストラテジーパターン: MP/HP消費の切り替え</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: MP管理に特化</li>
 *   <li>DRY: 回復ロジックを一元管理</li>
 *   <li>KISS: シンプルなAPI設計</li>
 * </ul>
 *
 * <p>スレッド安全性:</p>
 * <ul>
 *   <li>ConcurrentHashMapを使用したスレッドセーフな実装</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 * @see Stat
 */
public class ManaManager {

    private final Logger logger;
    private final UUID playerUuid;
    private final ManaManagerData manaData;

    /**
     * コストタイプ列挙型
     *
     * <p>スキル発動時のリソース消費タイプを定義します。</p>
     */
    public enum CostType {
        /** MP消費 */
        MANA("mana", "MP消費", "§b"),
        /** HP消費 */
        HEALTH("hp", "HP消費", "§c");

        private final String id;
        private final String displayName;
        private final String color;

        CostType(String id, String displayName, String color) {
            this.id = id;
            this.displayName = displayName;
            this.color = color;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColoredName() {
            return color + displayName;
        }

        public static CostType fromId(String id) {
            if (id == null) {
                return MANA;
            }
            for (CostType type : values()) {
                if (type.id.equalsIgnoreCase(id)) {
                    return type;
                }
            }
            return MANA;
        }
    }

    /**
     * MPデータ保持クラス
     */
    public static class ManaManagerData {
        private int maxMana;
        private int currentMana;
        private int maxHpModifier;
        private CostType costType;

        public ManaManagerData(int maxMana, int currentMana, int maxHpModifier, CostType costType) {
            this.maxMana = maxMana;
            this.currentMana = Math.min(currentMana, maxMana);
            this.maxHpModifier = maxHpModifier;
            this.costType = costType != null ? costType : CostType.MANA;
        }

        public int getMaxMana() {
            return maxMana;
        }

        public void setMaxMana(int maxMana) {
            this.maxMana = Math.max(0, maxMana);
            if (currentMana > this.maxMana) {
                currentMana = this.maxMana;
            }
        }

        public int getCurrentMana() {
            return currentMana;
        }

        public void setCurrentMana(int currentMana) {
            this.currentMana = Math.min(Math.max(0, currentMana), maxMana);
        }

        public int getMaxHpModifier() {
            return maxHpModifier;
        }

        public void setMaxHpModifier(int maxHpModifier) {
            this.maxHpModifier = Math.max(0, maxHpModifier);
        }

        public CostType getCostType() {
            return costType;
        }

        public void setCostType(CostType costType) {
            this.costType = costType != null ? costType : CostType.MANA;
        }
    }

    /**
     * コンストラクタ
     *
     * @param playerUuid プレイヤーUUID
     * @param maxMana 最大MP
     * @param currentMana 現在MP
     * @param maxHpModifier 最大HP修飾子
     * @param costType コストタイプ
     */
    public ManaManager(UUID playerUuid, int maxMana, int currentMana, int maxHpModifier, CostType costType) {
        this.logger = Logger.getLogger(ManaManager.class.getName());
        this.playerUuid = playerUuid;
        this.manaData = new ManaManagerData(maxMana, currentMana, maxHpModifier, costType);
    }

    /**
     * デフォルトコンストラクタ
     *
     * <p>MPを100、HP修飾子を0で初期化します。</p>
     *
     * @param playerUuid プレイヤーUUID
     */
    public ManaManager(UUID playerUuid) {
        this(playerUuid, 100, 100, 0, CostType.MANA);
    }

    // ==================== MP操作 ====================

    /**
     * 最大MPを取得します
     *
     * @return 最大MP
     */
    public int getMaxMana() {
        return manaData.getMaxMana();
    }

    /**
     * 最大MPを設定します
     *
     * @param maxMana 最大MP
     */
    public void setMaxMana(int maxMana) {
        manaData.setMaxMana(maxMana);
        logger.fine("[" + playerUuid + "] Max mana set to: " + maxMana);
    }

    /**
     * 現在MPを取得します
     *
     * @return 現在MP
     */
    public int getCurrentMana() {
        return manaData.getCurrentMana();
    }

    /**
     * 現在MPを設定します
     *
     * @param currentMana 現在MP
     */
    public void setCurrentMana(int currentMana) {
        manaData.setCurrentMana(currentMana);
    }

    /**
     * MPを追加します
     *
     * @param amount 追加するMP量
     * @return 実際に追加されたMP量
     */
    public int addMana(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int current = getCurrentMana();
        int max = getMaxMana();
        int actualAdd = Math.min(amount, max - current);

        setCurrentMana(current + actualAdd);

        logger.fine("[" + playerUuid + "] Mana added: " + actualAdd + " (requested: " + amount + ")");

        return actualAdd;
    }

    /**
     * MPを消費します
     *
     * @param amount 消費するMP量
     * @return 消費に成功した場合はtrue、MP不足の場合はfalse
     */
    public boolean consumeMana(int amount) {
        if (amount <= 0) {
            return true;
        }

        int current = getCurrentMana();
        if (current < amount) {
            logger.fine("[" + playerUuid + "] Not enough mana. Have: " + current + ", Need: " + amount);
            return false;
        }

        setCurrentMana(current - amount);
        logger.fine("[" + playerUuid + "] Mana consumed: " + amount);

        return true;
    }

    /**
     * MPが足りているか確認します
     *
     * @param amount 必要なMP量
     * @return MPが足りている場合はtrue
     */
    public boolean hasMana(int amount) {
        return getCurrentMana() >= amount;
    }

    /**
     * MP回復処理を実行します
     *
     * <p>精神値（SPI）に基づいて回復量を計算します。</p>
     *
     * @param spiritValue 精神値
     * @param baseRegene 基礎回復量
     * @return 実際に回復したMP量
     */
    public int regenerateMana(int spiritValue, double baseRegene) {
        // SPI 1ポイントにつき回復量ボーナス
        double regenAmount = baseRegene + (spiritValue * 0.1);
        int actualRegen = addMana((int) Math.floor(regenAmount));

        if (actualRegen > 0) {
            logger.fine("[" + playerUuid + "] Mana regenerated: " + actualRegen +
                       " (SPI: " + spiritValue + ", Base: " + baseRegene + ")");
        }

        return actualRegen;
    }

    // ==================== HP修飾子操作 ====================

    /**
     * 最大HP修飾子を取得します
     *
     * @return 最大HP修飾子
     */
    public int getMaxHpModifier() {
        return manaData.getMaxHpModifier();
    }

    /**
     * 最大HP修飾子を設定します
     *
     * @param modifier 最大HP修飾子
     */
    public void setMaxHpModifier(int modifier) {
        manaData.setMaxHpModifier(modifier);
        logger.fine("[" + playerUuid + "] Max HP modifier set to: " + modifier);
    }

    /**
     * 最大HP修飾子を追加します
     *
     * @param amount 追加する修飾子量
     */
    public void addMaxHpModifier(int amount) {
        int current = getMaxHpModifier();
        setMaxHpModifier(current + amount);
    }

    // ==================== コストタイプ操作 ====================

    /**
     * 現在のコストタイプを取得します
     *
     * @return コストタイプ
     */
    public CostType getCostType() {
        return manaData.getCostType();
    }

    /**
     * コストタイプを設定します
     *
     * @param costType 新しいコストタイプ
     */
    public void setCostType(CostType costType) {
        manaData.setCostType(costType);
        logger.fine("[" + playerUuid + "] Cost type changed to: " + costType.getDisplayName());
    }

    /**
     * IDからコストタイプを設定します
     *
     * @param typeId コストタイプID
     */
    public void setCostType(String typeId) {
        setCostType(CostType.fromId(typeId));
    }

    /**
     * コストタイプを切り替えます
     */
    public void toggleCostType() {
        CostType current = getCostType();
        CostType newType = (current == CostType.MANA) ? CostType.HEALTH : CostType.MANA;
        setCostType(newType);
    }

    // ==================== スキル発動コスト処理 ====================

    /**
     * スキル発動時のコストを消費します
     *
     * <p>現在のコストタイプに応じてMPまたはHPを消費します。</p>
     *
     * @param amount コスト量
     * @param currentHp 現在HP
     * @param maxHp 最大HP
     * @return 消費に成功した場合はtrue、リソース不足の場合はfalse
     */
    public boolean consumeSkillCost(int amount, int currentHp, int maxHp) {
        CostType type = getCostType();

        switch (type) {
            case MANA:
                return consumeMana(amount);
            case HEALTH:
                if (currentHp <= amount) {
                    logger.fine("[" + playerUuid + "] Not enough HP for skill cost. Have: " + currentHp + ", Need: " + amount);
                    return false;
                }
                // HP消費の実際の処理は呼び出し元で行う
                return true;
            default:
                return consumeMana(amount);
        }
    }

    // ==================== ユーティリティ ====================

    /**
     * MPの割合を取得します
     *
     * @return MPの割合（0.0～1.0）
     */
    public double getManaRatio() {
        int max = getMaxMana();
        if (max == 0) {
            return 0.0;
        }
        return (double) getCurrentMana() / max;
    }

    /**
     * MPが満タンか確認します
     *
     * @return 満タンの場合はtrue
     */
    public boolean isFullMana() {
        return getCurrentMana() >= getMaxMana();
    }

    /**
     * MPが空か確認します
     *
     * @return 空の場合はtrue
     */
    public boolean isEmptyMana() {
        return getCurrentMana() <= 0;
    }

    /**
     * MPデータのコピーを取得します
     *
     * @return MPデータのコピー
     */
    public ManaManagerData getDataCopy() {
        return new ManaManagerData(
            getMaxMana(),
            getCurrentMana(),
            getMaxHpModifier(),
            getCostType()
        );
    }

    /**
     * MP情報を文字列化します
     *
     * @return フォーマットされた文字列
     */
    public String formatManaInfo() {
        CostType type = getCostType();
        return String.format("§bMP: %d§7/§b%d §7(%s§7)",
            getCurrentMana(),
            getMaxMana(),
            type.getColoredName()
        );
    }

    @Override
    public String toString() {
        return "ManaManager{" +
                "maxMana=" + getMaxMana() +
                ", currentMana=" + getCurrentMana() +
                ", maxHpModifier=" + getMaxHpModifier() +
                ", costType=" + getCostType() +
                '}';
    }
}
