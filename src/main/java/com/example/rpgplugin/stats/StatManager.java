package com.example.rpgplugin.stats;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * ステータス管理クラス
 *
 * <p>プレイヤーのステータス値を管理し、基本値、手動配分ポイント、修正値を統合します。</p>
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>ファサードパターン: ステータス操作への統一的インターフェース</li>
 *   <li>ビルダーパターン: 複雑なステータス計算の構築</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ステータス管理に特化</li>
 *   <li>DRY: 計算ロジックを一元管理</li>
 *   <li>KISS: シンプルなAPI設計</li>
 * </ul>
 *
 * <p>スレッド安全性:</p>
 * <ul>
 *   <li>ConcurrentHashMapを使用したスレッドセーフな実装</li>
 *   <li>外部同期なしで複数スレッドからアクセス可能</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 * @see Stat
 * @see StatModifier
 */
public class StatManager {

    private final Logger logger;
    private final Map<Stat, Integer> baseStats;
    private final Map<Stat, List<StatModifier>> modifiers;
    private final AtomicInteger availablePoints;
    private final AtomicInteger totalLevel;

    /**
     * デフォルトコンストラクタ
     *
     * <p>全ステータスを10、手動配分ポイントを0で初期化します。</p>
     */
    public StatManager() {
        this(10, 0);
    }

    /**
     * コンストラクタ
     *
     * @param initialBaseValue 初期基本値（全ステータス共通）
     * @param initialAvailablePoints 初期手動配分ポイント
     */
    public StatManager(int initialBaseValue, int initialAvailablePoints) {
        this.logger = Logger.getLogger(StatManager.class.getName());
        this.baseStats = new ConcurrentHashMap<>();
        this.modifiers = new ConcurrentHashMap<>();
        this.availablePoints = new AtomicInteger(initialAvailablePoints);
        this.totalLevel = new AtomicInteger(1);

        // 初期化
        for (Stat stat : Stat.values()) {
            baseStats.put(stat, initialBaseValue);
            modifiers.put(stat, new CopyOnWriteArrayList<>());
        }
    }

    // ==================== 基本値操作 ====================

    /**
     * 指定したステータスの基本値を取得します
     *
     * @param stat ステータス種別
     * @return 基本値
     * @throws IllegalArgumentException statがnullの場合
     */
    public int getBaseStat(Stat stat) {
        if (stat == null) {
            throw new IllegalArgumentException("Stat cannot be null");
        }
        return baseStats.getOrDefault(stat, 0);
    }

    /**
     * 指定したステータスの基本値を設定します
     *
     * @param stat ステータス種別
     * @param value 新しい基本値
     * @throws IllegalArgumentException statがnull、またはvalueが負の場合
     */
    public void setBaseStat(Stat stat, int value) {
        if (stat == null) {
            throw new IllegalArgumentException("Stat cannot be null");
        }
        if (value < 0) {
            throw new IllegalArgumentException("Base stat value cannot be negative: " + value);
        }
        baseStats.put(stat, value);
    }

    /**
     * 全基本値を取得します
     *
     * @return 基本値のマップ（コピー）
     */
    public Map<Stat, Integer> getAllBaseStats() {
        return new EnumMap<>(baseStats);
    }

    // ==================== 最終値計算 ====================

    /**
     * 指定したステータスの最終値を計算します
     *
     * <p>計算式:</p>
     * <pre>
     * finalValue = ((baseValue + flatModifiers) * multiplierModifiers) + finalModifiers
     * </pre>
     *
     * @param stat ステータス種別
     * @return 最終値（小数点以下切り捨て）
     */
    public int getFinalStat(Stat stat) {
        if (stat == null) {
            throw new IllegalArgumentException("Stat cannot be null");
        }

        int baseValue = getBaseStat(stat);
        List<StatModifier> statModifiers = modifiers.get(stat);

        // 期限切れの修正値を削除
        cleanupExpiredModifiers(stat);

        double result = baseValue;

        // FLAT修正を適用
        for (StatModifier modifier : statModifiers) {
            if (modifier.getType() == StatModifier.Type.FLAT && !modifier.isExpired()) {
                result = modifier.applyTo(result);
            }
        }

        // MULTIPLIER修正を適用
        for (StatModifier modifier : statModifiers) {
            if (modifier.getType() == StatModifier.Type.MULTIPLIER && !modifier.isExpired()) {
                result = modifier.applyTo(result);
            }
        }

        // FINAL修正を適用
        for (StatModifier modifier : statModifiers) {
            if (modifier.getType() == StatModifier.Type.FINAL && !modifier.isExpired()) {
                result = modifier.applyTo(result);
            }
        }

        return (int) Math.floor(result);
    }

    /**
     * 全最終値を取得します
     *
     * @return 最終値のマップ（コピー）
     */
    public Map<Stat, Integer> getAllFinalStats() {
        Map<Stat, Integer> result = new EnumMap<>(Stat.class);
        for (Stat stat : Stat.values()) {
            result.put(stat, getFinalStat(stat));
        }
        return result;
    }

    // ==================== 手動配分 ====================

    /**
     * 手動配分ポイントを取得します
     *
     * @return 現在の手動配分ポイント
     */
    public int getAvailablePoints() {
        return availablePoints.get();
    }

    /**
     * 手動配分ポイントを設定します
     *
     * @param points 新しい手動配分ポイント
     * @throws IllegalArgumentException pointsが負の場合
     */
    public void setAvailablePoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Available points cannot be negative: " + points);
        }
        this.availablePoints.set(points);
    }

    /**
     * 手動配分ポイントを追加します
     *
     * @param amount 追加するポイント数
     * @throws IllegalArgumentException amountが負の場合
     */
    public void addAvailablePoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + amount);
        }
        this.availablePoints.addAndGet(amount);
    }

    /**
     * ステータスに手動配分ポイントを割り振ります
     *
     * @param stat ステータス種別
     * @param amount 割り振るポイント数
     * @return 割り振りに成功した場合はtrue、失敗した場合はfalse
     */
    public boolean allocatePoint(Stat stat, int amount) {
        if (stat == null) {
            throw new IllegalArgumentException("Stat cannot be null");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive: " + amount);
        }
        if (availablePoints.get() < amount) {
            logger.fine("Not enough available points. Have: " + availablePoints.get() + ", Need: " + amount);
            return false;
        }

        int currentValue = getBaseStat(stat);
        setBaseStat(stat, currentValue + amount);
        availablePoints.addAndGet(-amount);

        logger.fine("Allocated " + amount + " points to " + stat.getDisplayName() +
                   ". New value: " + getBaseStat(stat) + ", Remaining points: " + availablePoints);

        return true;
    }

    /**
     * ステータス配分をリセットします
     *
     * <p>全ステータスを10に戻し、使用したポイントを返却します。</p>
     *
     * @return 返却されたポイント数
     */
    public int resetAllocation() {
        int totalUsed = 0;
        final int defaultValue = 10;

        for (Stat stat : Stat.values()) {
            int currentValue = getBaseStat(stat);
            if (currentValue > defaultValue) {
                totalUsed += (currentValue - defaultValue);
                setBaseStat(stat, defaultValue);
            }
        }

        availablePoints.addAndGet(totalUsed);

        logger.fine("Reset stat allocation. Returned " + totalUsed + " points.");

        return totalUsed;
    }

    // ==================== 修正値管理 ====================

    /**
     * 修正値を追加します
     *
     * @param stat ステータス種別
     * @param modifier 修正値
     * @throws IllegalArgumentException statまたはmodifierがnullの場合
     */
    public void addModifier(Stat stat, StatModifier modifier) {
        if (stat == null) {
            throw new IllegalArgumentException("Stat cannot be null");
        }
        if (modifier == null) {
            throw new IllegalArgumentException("Modifier cannot be null");
        }

        List<StatModifier> statModifiers = modifiers.get(stat);
        statModifiers.add(modifier);

        logger.fine("Added modifier to " + stat.getDisplayName() + ": " + modifier.toString());
    }

    /**
     * 修正値を削除します
     *
     * @param stat ステータス種別
     * @param modifierId 修正値ID
     * @return 削除に成功した場合はtrue、失敗した場合はfalse
     */
    public boolean removeModifier(Stat stat, UUID modifierId) {
        if (stat == null) {
            throw new IllegalArgumentException("Stat cannot be null");
        }
        if (modifierId == null) {
            throw new IllegalArgumentException("Modifier ID cannot be null");
        }

        List<StatModifier> statModifiers = modifiers.get(stat);
        boolean removed = statModifiers.removeIf(modifier -> modifier.getId().equals(modifierId));

        if (removed) {
            logger.fine("Removed modifier from " + stat.getDisplayName() + ": " + modifierId);
        }

        return removed;
    }

    /**
     * ソースを指定して修正値を削除します
     *
     * @param stat ステータス種別
     * @param source 修正元
     * @return 削除した修正値の数
     */
    public int removeModifiersBySource(Stat stat, String source) {
        if (stat == null) {
            throw new IllegalArgumentException("Stat cannot be null");
        }
        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }

        List<StatModifier> statModifiers = modifiers.get(stat);
        int beforeSize = statModifiers.size();
        statModifiers.removeIf(modifier -> modifier.getSource().equals(source));
        int removedCount = beforeSize - statModifiers.size();

        if (removedCount > 0) {
            logger.fine("Removed " + removedCount + " modifiers from " + stat.getDisplayName() + " with source: " + source);
        }

        return removedCount;
    }

    /**
     * 指定したステータスの修正値を全てクリアします
     *
     * @param stat ステータス種別
     */
    public void clearModifiers(Stat stat) {
        if (stat == null) {
            throw new IllegalArgumentException("Stat cannot be null");
        }

        List<StatModifier> statModifiers = modifiers.get(stat);
        int count = statModifiers.size();
        statModifiers.clear();

        logger.fine("Cleared " + count + " modifiers from " + stat.getDisplayName());
    }

    /**
     * 全ステータスの修正値を全てクリアします
     */
    public void clearAllModifiers() {
        int totalCleared = 0;
        for (Stat stat : Stat.values()) {
            totalCleared += modifiers.get(stat).size();
            modifiers.get(stat).clear();
        }

        logger.fine("Cleared " + totalCleared + " modifiers from all stats.");
    }

    /**
     * 期限切れの修正値を削除します
     *
     * @param stat ステータス種別
     * @return 削除した数
     */
    public int cleanupExpiredModifiers(Stat stat) {
        if (stat == null) {
            throw new IllegalArgumentException("Stat cannot be null");
        }

        List<StatModifier> statModifiers = modifiers.get(stat);
        int beforeSize = statModifiers.size();
        statModifiers.removeIf(StatModifier::isExpired);
        int removedCount = beforeSize - statModifiers.size();

        if (removedCount > 0) {
            logger.fine("Cleaned up " + removedCount + " expired modifiers from " + stat.getDisplayName());
        }

        return removedCount;
    }

    /**
     * 全ステータスの期限切れ修正値を削除します
     *
     * @return 削除した合計数
     */
    public int cleanupAllExpiredModifiers() {
        int totalCleared = 0;
        for (Stat stat : Stat.values()) {
            totalCleared += cleanupExpiredModifiers(stat);
        }
        return totalCleared;
    }

    /**
     * 指定したステータスの修正値リストを取得します
     *
     * @param stat ステータス種別
     * @return 修正値リスト（コピー）
     */
    public List<StatModifier> getModifiers(Stat stat) {
        if (stat == null) {
            throw new IllegalArgumentException("Stat cannot be null");
        }

        List<StatModifier> statModifiers = modifiers.get(stat);
        return new ArrayList<>(statModifiers);
    }

    // ==================== レベル管理 ====================

    /**
     * 総レベルを取得します
     *
     * @return 総レベル
     */
    public int getTotalLevel() {
        return totalLevel.get();
    }

    /**
     * 総レベルを設定します
     *
     * @param level 新しいレベル
     * @throws IllegalArgumentException levelが1未満の場合
     */
    public void setTotalLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("Level cannot be less than 1: " + level);
        }
        this.totalLevel.set(level);
    }

    // ==================== ユーティリティ ====================

    /**
     * ステータス情報を文字列化します
     *
     * @return フォーマットされた文字列
     */
    public String formatStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== ステータス =====\n");
        sb.append("レベル: ").append(totalLevel).append("\n");
        sb.append("手動配分ポイント: ").append(availablePoints).append("\n");
        sb.append("--------------------\n");

        for (Stat stat : Stat.values()) {
            int base = getBaseStat(stat);
            int finalValue = getFinalStat(stat);
            sb.append(String.format("%s%s§r: %d (基本: %d)",
                    stat.getColor(),
                    stat.getDisplayName(),
                    finalValue,
                    base));

            // 修正値がある場合は表示
            List<StatModifier> statModifiers = modifiers.get(stat);
            if (!statModifiers.isEmpty()) {
                List<String> modifierStrings = statModifiers.stream()
                    .filter(m -> !m.isExpired())
                    .map(m -> "[" + m.getSource() + ": " + m.getValue() + "]")
                    .collect(Collectors.toList());
                if (!modifierStrings.isEmpty()) {
                    sb.append(" ").append(String.join(" ", modifierStrings));
                }
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "StatManager{" +
                "totalLevel=" + totalLevel +
                ", availablePoints=" + availablePoints +
                ", baseStats=" + baseStats +
                '}';
    }
}
