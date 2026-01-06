package com.example.rpgplugin.stats.calculator;

import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;

/**
 * ステータス計算クラス
 *
 * <p>ステータス値をHP、MP、攻撃力などの実際のパラメータに変換します。</p>
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>ユーティリティクラス: 静的メソッドのみ提供</li>
 *   <li>ストラテジーパターン: 計算式をカプセル化</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ステータス計算に特化</li>
 *   <li>DRY: 計算ロジックを一元管理</li>
 *   <li>KISS: シンプルな計算式</li>
 * </ul>
 *
 * <p>計算式:</p>
 * <ul>
 *   <li>HP = VIT × 10 + レベル × 5</li>
 *   <li>MP = SPI × 5 + INT × 3 + レベル × 2</li>
 *   <li>物理攻撃力 = STR × 2 + DEX</li>
 *   <li>魔法攻撃力 = INT × 2 + SPI</li>
 *   <li>物理防御力 = VIT × 1.5</li>
 *   <li>魔法防御力 = SPI × 1.5</li>
 *   <li>命中率 = DEX × 0.5 + レベル × 2</li>
 *   <li>回避率 = DEX × 0.3</li>
 *   <li>クリティカル率 = DEX × 0.2</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public final class StatCalculator {

    // ==================== 定数 ====================

    /**
     * HP計算係数（VITに対する乗数）
     */
    public static final double HP_VIT_MULTIPLIER = 10.0;

    /**
     * HP計算係数（レベルに対する乗数）
     */
    public static final double HP_LEVEL_MULTIPLIER = 5.0;

    /**
     * MP計算係数（SPIに対する乗数）
     */
    public static final double MP_SPI_MULTIPLIER = 5.0;

    /**
     * MP計算係数（INTに対する乗数）
     */
    public static final double MP_INT_MULTIPLIER = 3.0;

    /**
     * MP計算係数（レベルに対する乗数）
     */
    public static final double MP_LEVEL_MULTIPLIER = 2.0;

    /**
     * 物理攻撃力計算係数（STRに対する乗数）
     */
    public static final double PHYSICAL_ATK_STR_MULTIPLIER = 2.0;

    /**
     * 物理攻撃力計算係数（DEXに対する乗数）
     */
    public static final double PHYSICAL_ATK_DEX_MULTIPLIER = 1.0;

    /**
     * 魔法攻撃力計算係数（INTに対する乗数）
     */
    public static final double MAGIC_ATK_INT_MULTIPLIER = 2.0;

    /**
     * 魔法攻撃力計算係数（SPIに対する乗数）
     */
    public static final double MAGIC_ATK_SPI_MULTIPLIER = 1.0;

    /**
     * 物理防御力計算係数（VITに対する乗数）
     */
    public static final double PHYSICAL_DEF_VIT_MULTIPLIER = 1.5;

    /**
     * 魔法防御力計算係数（SPIに対する乗数）
     */
    public static final double MAGIC_DEF_SPI_MULTIPLIER = 1.5;

    /**
     * 命中率計算係数（DEXに対する乗数）
     */
    public static final double HIT_RATE_DEX_MULTIPLIER = 0.5;

    /**
     * 命中率計算係数（レベルに対する乗数）
     */
    public static final double HIT_RATE_LEVEL_MULTIPLIER = 2.0;

    /**
     * 回避率計算係数（DEXに対する乗数）
     */
    public static final double DODGE_RATE_DEX_MULTIPLIER = 0.3;

    /**
     * クリティカル率計算係数（DEXに対する乗数）
     */
    public static final double CRIT_RATE_DEX_MULTIPLIER = 0.2;

    /**
     * 最大回避率（%）
     */
    public static final double MAX_DODGE_RATE = 75.0;

    /**
     * 最大クリティカル率（%）
     */
    public static final double MAX_CRIT_RATE = 50.0;

    // ==================== コンストラクタ ====================

    /**
     * プライベートコンストラクタ
     *
     * <p>ユーティリティクラスのためインスタンス化を防止します。</p>
     */
    private StatCalculator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== HP/MP計算 ====================

    /**
     * 最大HPを計算します
     *
     * <p>計算式: VIT × 10 + レベル × 5</p>
     *
     * @param statManager ステータスマネージャー
     * @return 最大HP
     */
    public static int calculateMaxHp(StatManager statManager) {
        if (statManager == null) {
            throw new IllegalArgumentException("StatManager cannot be null");
        }

        int vit = statManager.getFinalStat(Stat.VITALITY);
        int level = statManager.getTotalLevel();

        double maxHp = (vit * HP_VIT_MULTIPLIER) + (level * HP_LEVEL_MULTIPLIER);
        return (int) Math.floor(maxHp);
    }

    /**
     * 最大MPを計算します
     *
     * <p>計算式: SPI × 5 + INT × 3 + レベル × 2</p>
     *
     * @param statManager ステータスマネージャー
     * @return 最大MP
     */
    public static int calculateMaxMp(StatManager statManager) {
        if (statManager == null) {
            throw new IllegalArgumentException("StatManager cannot be null");
        }

        int spi = statManager.getFinalStat(Stat.SPIRIT);
        int intVal = statManager.getFinalStat(Stat.INTELLIGENCE);
        int level = statManager.getTotalLevel();

        double maxMp = (spi * MP_SPI_MULTIPLIER) + (intVal * MP_INT_MULTIPLIER) + (level * MP_LEVEL_MULTIPLIER);
        return (int) Math.floor(maxMp);
    }

    /**
     * MP自然回復量を計算します
     *
     * <p>計算式: SPI × 0.1 + 1（秒間回復量）</p>
     *
     * @param statManager ステータスマネージャー
     * @return 秒間MP回復量
     */
    public static double calculateMpRegen(StatManager statManager) {
        if (statManager == null) {
            throw new IllegalArgumentException("StatManager cannot be null");
        }

        int spi = statManager.getFinalStat(Stat.SPIRIT);
        return (spi * 0.1) + 1.0;
    }

    // ==================== 攻撃力計算 ====================

    /**
     * 物理攻撃力を計算します
     *
     * <p>計算式: STR × 2 + DEX</p>
     *
     * @param statManager ステータスマネージャー
     * @return 物理攻撃力
     */
    public static int calculatePhysicalAttack(StatManager statManager) {
        if (statManager == null) {
            throw new IllegalArgumentException("StatManager cannot be null");
        }

        int str = statManager.getFinalStat(Stat.STRENGTH);
        int dex = statManager.getFinalStat(Stat.DEXTERITY);

        double atk = (str * PHYSICAL_ATK_STR_MULTIPLIER) + (dex * PHYSICAL_ATK_DEX_MULTIPLIER);
        return (int) Math.floor(atk);
    }

    /**
     * 魔法攻撃力を計算します
     *
     * <p>計算式: INT × 2 + SPI</p>
     *
     * @param statManager ステータスマネージャー
     * @return 魔法攻撃力
     */
    public static int calculateMagicAttack(StatManager statManager) {
        if (statManager == null) {
            throw new IllegalArgumentException("StatManager cannot be null");
        }

        int intVal = statManager.getFinalStat(Stat.INTELLIGENCE);
        int spi = statManager.getFinalStat(Stat.SPIRIT);

        double atk = (intVal * MAGIC_ATK_INT_MULTIPLIER) + (spi * MAGIC_ATK_SPI_MULTIPLIER);
        return (int) Math.floor(atk);
    }

    // ==================== 防御力計算 ====================

    /**
     * 物理防御力を計算します
     *
     * <p>計算式: VIT × 1.5</p>
     *
     * @param statManager ステータスマネージャー
     * @return 物理防御力
     */
    public static int calculatePhysicalDefense(StatManager statManager) {
        if (statManager == null) {
            throw new IllegalArgumentException("StatManager cannot be null");
        }

        int vit = statManager.getFinalStat(Stat.VITALITY);
        double def = vit * PHYSICAL_DEF_VIT_MULTIPLIER;
        return (int) Math.floor(def);
    }

    /**
     * 魔法防御力を計算します
     *
     * <p>計算式: SPI × 1.5</p>
     *
     * @param statManager ステータスマネージャー
     * @return 魔法防御力
     */
    public static int calculateMagicDefense(StatManager statManager) {
        if (statManager == null) {
            throw new IllegalArgumentException("StatManager cannot be null");
        }

        int spi = statManager.getFinalStat(Stat.SPIRIT);
        double def = spi * MAGIC_DEF_SPI_MULTIPLIER;
        return (int) Math.floor(def);
    }

    // ==================== 命中/回避/クリティカル計算 ====================

    /**
     * 命中率を計算します
     *
     * <p>計算式: DEX × 0.5 + レベル × 2（基本命中率85%に加算）</p>
     *
     * @param statManager ステータスマネージャー
     * @return 命中率（%）[0-100]
     */
    public static double calculateHitRate(StatManager statManager) {
        if (statManager == null) {
            throw new IllegalArgumentException("StatManager cannot be null");
        }

        int dex = statManager.getFinalStat(Stat.DEXTERITY);
        int level = statManager.getTotalLevel();

        double baseHitRate = 85.0; // 基本命中率
        double bonus = (dex * HIT_RATE_DEX_MULTIPLIER) + (level * HIT_RATE_LEVEL_MULTIPLIER);

        return Math.min(100.0, baseHitRate + bonus);
    }

    /**
     * 回避率を計算します
     *
     * <p>計算式: DEX × 0.3（最大75%）</p>
     *
     * @param statManager ステータスマネージャー
     * @return 回避率（%）[0-75]
     */
    public static double calculateDodgeRate(StatManager statManager) {
        if (statManager == null) {
            throw new IllegalArgumentException("StatManager cannot be null");
        }

        int dex = statManager.getFinalStat(Stat.DEXTERITY);
        double dodge = dex * DODGE_RATE_DEX_MULTIPLIER;
        return Math.min(MAX_DODGE_RATE, dodge);
    }

    /**
     * クリティカル率を計算します
     *
     * <p>計算式: DEX × 0.2（最大50%）</p>
     *
     * @param statManager ステータスマネージャー
     * @return クリティカル率（%）[0-50]
     */
    public static double calculateCriticalRate(StatManager statManager) {
        if (statManager == null) {
            throw new IllegalArgumentException("StatManager cannot be null");
        }

        int dex = statManager.getFinalStat(Stat.DEXTERITY);
        double crit = dex * CRIT_RATE_DEX_MULTIPLIER;
        return Math.min(MAX_CRIT_RATE, crit);
    }

    /**
     * クリティカルダメージ倍率を計算します
     *
     * <p>クリティカル時のダメージ倍率を返します。
     * 基本は1.5倍、DEXに応じて増加します。</p>
     *
     * @param statManager ステータスマネージャー
     * @return クリティカルダメージ倍率
     */
    public static double calculateCriticalDamage(StatManager statManager) {
        if (statManager == null) {
            throw new IllegalArgumentException("StatManager cannot be null");
        }

        int dex = statManager.getFinalStat(Stat.DEXTERITY);
        // 基本倍率1.5 + DEXの0.1%
        double multiplier = 1.5 + (dex * 0.001);
        return multiplier;
    }
}
