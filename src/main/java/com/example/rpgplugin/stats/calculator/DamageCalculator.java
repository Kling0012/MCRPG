package com.example.rpgplugin.stats.calculator;

import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;

import java.util.Random;

/**
 * ダメージ計算クラス
 *
 * <p>ステータス値に基づいて物理ダメージ、魔法ダメージ、クリティカルダメージを計算します。</p>
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>ユーティリティクラス: 静的メソッドのみ提供</li>
 *   <li>ストラテジーパターン: ダメージ計算式をカプセル化</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ダメージ計算に特化</li>
 *   <li>DRY: 計算ロジックを一元管理</li>
 *   <li>KISS: シンプルな計算式</li>
 * </ul>
 *
 * <p>計算式:</p>
 * <ul>
 *   <li>物理ダメージ = (STR × 2 + DEX - 物理防御) × 武器倍率 × ランダム変動[0.9-1.1]</li>
 *   <li>魔法ダメージ = (INT × 2 + SPI - 魔法防御) × 魔法倍率 × ランダム変動[0.9-1.1]</li>
 *   <li>クリティカルダメージ = 通常ダメージ × クリティカル倍率</li>
 * </ul>
 *
 * <p>スレッド安全性:</p>
 * <ul>
 *   <li>ThreadLocalのRandomを使用したスレッドセーフな乱数生成</li>
 *   <li>外部同期なしで複数スレッドからアクセス可能</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 * @see StatCalculator
 */
public final class DamageCalculator {

    // ==================== 定数 ====================

    /**
     * ダメージ変動幅（±10%）
     */
    public static final double DAMAGE_VARIATION = 0.1;

    /**
     * 最小ダメージ係数（0.9倍）
     */
    public static final double MIN_DAMAGE_MULTIPLIER = 1.0 - DAMAGE_VARIATION;

    /**
     * 最大ダメージ係数（1.1倍）
     */
    public static final double MAX_DAMAGE_MULTIPLIER = 1.0 + DAMAGE_VARIATION;

    /**
     * 最終ダメージ係数（防御減衰後の最低ダメージ割合）
     */
    public static final double MIN_DAMAGE_PERCENTAGE = 0.1;

    // ==================== ThreadSafe乱数生成 ====================

    /**
     * スレッドセーフな乱数生成器
     */
    private static final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::new);

    // ==================== コンストラクタ ====================

    /**
     * プライベートコンストラクタ
     *
     * <p>ユーティリティクラスのためインスタンス化を防止します。</p>
     */
    private DamageCalculator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== 物理ダメージ計算 ====================

    /**
     * 物理ダメージを計算します
     *
     * <p>計算式:</p>
     * <pre>
     * baseDamage = 攻撃力 - 防御力
     * finalDamage = max(baseDamage × 武器倍率 × ランダム[0.9-1.1], baseDamage × 0.1)
     * </pre>
     *
     * @param attackerStatManager 攻撃者のステータスマネージャー
     * @param defenderStatManager 防御者のステータスマネージャー
     * @param weaponMultiplier 武器倍率（1.0で無補正）
     * @return 物理ダメージ
     */
    public static int calculatePhysicalDamage(
            StatManager attackerStatManager,
            StatManager defenderStatManager,
            double weaponMultiplier) {

        if (attackerStatManager == null || defenderStatManager == null) {
            throw new IllegalArgumentException("StatManager cannot be null");
        }
        if (weaponMultiplier < 0.0) {
            throw new IllegalArgumentException("Weapon multiplier cannot be negative: " + weaponMultiplier);
        }

        // 攻撃力と防御力を取得
        int attack = StatCalculator.calculatePhysicalAttack(attackerStatManager);
        int defense = StatCalculator.calculatePhysicalDefense(defenderStatManager);

        // 基本ダメージ計算
        double baseDamage = attack - defense;

        // ダメージが負の場合は最低ダメージを適用
        if (baseDamage < 0) {
            baseDamage = 0;
        }

        // 武器倍率を適用
        double weaponDamage = baseDamage * weaponMultiplier;

        // ランダム変動を適用
        double randomMultiplier = getRandomDamageMultiplier();
        double finalDamage = weaponDamage * randomMultiplier;

        // 最低ダメージ保証（基本ダメージの10%）
        double minDamage = baseDamage * MIN_DAMAGE_PERCENTAGE;
        finalDamage = Math.max(minDamage, finalDamage);

        return (int) Math.floor(finalDamage);
    }

    /**
     * 物理ダメージを計算します（武器倍率1.0）
     *
     * @param attackerStatManager 攻撃者のステータスマネージャー
     * @param defenderStatManager 防御者のステータスマネージャー
     * @return 物理ダメージ
     */
    public static int calculatePhysicalDamage(
            StatManager attackerStatManager,
            StatManager defenderStatManager) {

        return calculatePhysicalDamage(attackerStatManager, defenderStatManager, 1.0);
    }

    // ==================== 魔法ダメージ計算 ====================

    /**
     * 魔法ダメージを計算します
     *
     * <p>計算式:</p>
     * <pre>
     * baseDamage = 魔法攻撃力 - 魔法防御力
     * finalDamage = max(baseDamage × 魔法倍率 × ランダム[0.9-1.1], baseDamage × 0.1)
     * </pre>
     *
     * @param attackerStatManager 攻撃者のステータスマネージャー
     * @param defenderStatManager 防御者のステータスマネージャー
     * @param spellMultiplier 魔法倍率（1.0で無補正）
     * @return 魔法ダメージ
     */
    public static int calculateMagicDamage(
            StatManager attackerStatManager,
            StatManager defenderStatManager,
            double spellMultiplier) {

        if (attackerStatManager == null || defenderStatManager == null) {
            throw new IllegalArgumentException("StatManager cannot be null");
        }
        if (spellMultiplier < 0.0) {
            throw new IllegalArgumentException("Spell multiplier cannot be negative: " + spellMultiplier);
        }

        // 魔法攻撃力と魔法防御力を取得
        int attack = StatCalculator.calculateMagicAttack(attackerStatManager);
        int defense = StatCalculator.calculateMagicDefense(defenderStatManager);

        // 基本ダメージ計算
        double baseDamage = attack - defense;

        // ダメージが負の場合は最低ダメージを適用
        if (baseDamage < 0) {
            baseDamage = 0;
        }

        // 魔法倍率を適用
        double spellDamage = baseDamage * spellMultiplier;

        // ランダム変動を適用
        double randomMultiplier = getRandomDamageMultiplier();
        double finalDamage = spellDamage * randomMultiplier;

        // 最低ダメージ保証（基本ダメージの10%）
        double minDamage = baseDamage * MIN_DAMAGE_PERCENTAGE;
        finalDamage = Math.max(minDamage, finalDamage);

        return (int) Math.floor(finalDamage);
    }

    /**
     * 魔法ダメージを計算します（魔法倍率1.0）
     *
     * @param attackerStatManager 攻撃者のステータスマネージャー
     * @param defenderStatManager 防御者のステータスマネージャー
     * @return 魔法ダメージ
     */
    public static int calculateMagicDamage(
            StatManager attackerStatManager,
            StatManager defenderStatManager) {

        return calculateMagicDamage(attackerStatManager, defenderStatManager, 1.0);
    }

    // ==================== クリティカル処理 ====================

    /**
     * クリティカルが発生するか判定します
     *
     * <p>DEXに基づいてクリティカル率を計算し、乱数判定を行います。</p>
     *
     * @param attackerStatManager 攻撃者のステータスマネージャー
     * @return クリティカルが発生した場合はtrue
     */
    public static boolean isCriticalHit(StatManager attackerStatManager) {
        if (attackerStatManager == null) {
            throw new IllegalArgumentException("StatManager cannot be null");
        }

        double critRate = StatCalculator.calculateCriticalRate(attackerStatManager);
        double roll = RANDOM.get().nextDouble() * 100.0;

        return roll < critRate;
    }

    /**
     * クリティカルダメージを計算します
     *
     * <p>通常ダメージにクリティカル倍率を適用します。</p>
     *
     * @param normalDamage 通常ダメージ
     * @param attackerStatManager 攻撃者のステータスマネージャー
     * @return クリティカルダメージ
     */
    public static int calculateCriticalDamage(int normalDamage, StatManager attackerStatManager) {
        if (attackerStatManager == null) {
            throw new IllegalArgumentException("StatManager cannot be null");
        }

        if (normalDamage <= 0) {
            return 0;
        }

        double critMultiplier = StatCalculator.calculateCriticalDamage(attackerStatManager);
        double critDamage = normalDamage * critMultiplier;

        return (int) Math.floor(critDamage);
    }

    // ==================== 命中/回避判定 ====================

    /**
     * 攻撃が命中するか判定します
     *
     * <p>攻撃者の命中率と防御者の回避率を考慮して判定します。</p>
     *
     * @param attackerStatManager 攻撃者のステータスマネージャー
     * @param defenderStatManager 防御者のステータスマネージャー
     * @return 命中した場合はtrue
     */
    public static boolean isHit(StatManager attackerStatManager, StatManager defenderStatManager) {
        if (attackerStatManager == null || defenderStatManager == null) {
            throw new IllegalArgumentException("StatManager cannot be null");
        }

        double hitRate = StatCalculator.calculateHitRate(attackerStatManager);
        double dodgeRate = StatCalculator.calculateDodgeRate(defenderStatManager);

        // 実質命中率 = 命中率 - 回避率
        double actualHitRate = hitRate - dodgeRate;

        // 最低命中率は5%
        actualHitRate = Math.max(5.0, actualHitRate);

        double roll = RANDOM.get().nextDouble() * 100.0;

        return roll < actualHitRate;
    }

    // ==================== ユーティリティ ====================

    /**
     * ランダムダメージ倍率を生成します
     *
     * <p>0.9〜1.1の範囲でランダムな倍率を返します。</p>
     *
     * @return ランダムダメージ倍率
     */
    private static double getRandomDamageMultiplier() {
        Random random = RANDOM.get();
        return MIN_DAMAGE_MULTIPLIER + (random.nextDouble() * (MAX_DAMAGE_MULTIPLIER - MIN_DAMAGE_MULTIPLIER));
    }

    /**
     * ダメージ計算結果を表すクラス
     */
    public static class DamageResult {
        private final int damage;
        private final boolean isCritical;
        private final boolean isDodged;
        private final boolean isMissed;

        /**
         * コンストラクタ
         *
         * @param damage ダメージ値
         * @param isCritical クリティカルかどうか
         * @param isDodged 回避されたかどうか
         * @param isMissed 外れたかどうか
         */
        public DamageResult(int damage, boolean isCritical, boolean isDodged, boolean isMissed) {
            this.damage = damage;
            this.isCritical = isCritical;
            this.isDodged = isDodged;
            this.isMissed = isMissed;
        }

        /**
         * ダメージ値を取得します
         *
         * @return ダメージ値
         */
        public int getDamage() {
            return damage;
        }

        /**
         * クリティカルか確認します
         *
         * @return クリティカルの場合はtrue
         */
        public boolean isCritical() {
            return isCritical;
        }

        /**
         * 回避されたか確認します
         *
         * @return 回避された場合はtrue
         */
        public boolean isDodged() {
            return isDodged;
        }

        /**
         * 外れたか確認します
         *
         * @return 外れた場合はtrue
         */
        public boolean isMissed() {
            return isMissed;
        }

        @Override
        public String toString() {
            if (isMissed) {
                return "Miss";
            } else if (isDodged) {
                return "Dodged";
            } else if (isCritical) {
                return "Critical: " + damage;
            } else {
                return "Damage: " + damage;
            }
        }
    }

    /**
     * 完全なダメージ計算を実行します
     *
     * <p>命中判定、回避判定、クリティカル判定、ダメージ計算を一括で行います。</p>
     *
     * @param attackerStatManager 攻撃者のステータスマネージャー
     * @param defenderStatManager 防御者のステータスマネージャー
     * @param weaponMultiplier 武器倍率
     * @param isMagicAttack 魔法攻撃かどうか
     * @return ダメージ計算結果
     */
    public static DamageResult calculateFullDamage(
            StatManager attackerStatManager,
            StatManager defenderStatManager,
            double weaponMultiplier,
            boolean isMagicAttack) {

        // 命中判定
        if (!isHit(attackerStatManager, defenderStatManager)) {
            return new DamageResult(0, false, false, true);
        }

        // 回避判定
        double dodgeRate = StatCalculator.calculateDodgeRate(defenderStatManager);
        double dodgeRoll = RANDOM.get().nextDouble() * 100.0;

        if (dodgeRoll < dodgeRate) {
            return new DamageResult(0, false, true, false);
        }

        // ダメージ計算
        int damage;
        if (isMagicAttack) {
            damage = calculateMagicDamage(attackerStatManager, defenderStatManager, weaponMultiplier);
        } else {
            damage = calculatePhysicalDamage(attackerStatManager, defenderStatManager, weaponMultiplier);
        }

        // クリティカル判定
        boolean isCritical = isCriticalHit(attackerStatManager);
        if (isCritical) {
            damage = calculateCriticalDamage(damage, attackerStatManager);
        }

        return new DamageResult(damage, isCritical, false, false);
    }
}
