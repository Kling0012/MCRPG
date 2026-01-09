package com.example.rpgplugin.stats;

import java.util.EnumMap;
import java.util.Map;

/**
 * プレイヤーのステータスデータを管理するクラス
 * 手動配分・自動配分・残りポイントを保持
 */
public class PlayerStats {

    // 手動配分されたステータス値
    private final Map<Stat, Integer> baseStats;

    // 自動配分されたステータス値
    private final Map<Stat, Integer> autoStats;

    // 未使用の配分ポイント
    private int availablePoints;

    /**
     * コンストラクタ
     */
    public PlayerStats() {
        this.baseStats = new EnumMap<>(Stat.class);
        this.autoStats = new EnumMap<>(Stat.class);
        this.availablePoints = 0;

        // 初期化
        for (Stat stat : Stat.values()) {
            baseStats.put(stat, 0);
            autoStats.put(stat, 0);
        }
    }

    /**
     * 手動配分値を取得
     */
    public int getBaseStat(Stat stat) {
        return baseStats.getOrDefault(stat, 0);
    }

    /**
     * 手動配分値を設定
     */
    public void setBaseStat(Stat stat, int value) {
        baseStats.put(stat, Math.max(0, value));
    }

    /**
     * 自動配分値を取得
     */
    public int getAutoStat(Stat stat) {
        return autoStats.getOrDefault(stat, 0);
    }

    /**
     * 自動配分値を設定
     */
    public void setAutoStat(Stat stat, int value) {
        autoStats.put(stat, Math.max(0, value));
    }

    /**
     * 合計ステータス値（手動 + 自動）を取得
     */
    public int getTotalStat(Stat stat) {
        return getBaseStat(stat) + getAutoStat(stat);
    }

    /**
     * 残りポイントを取得
     */
    public int getAvailablePoints() {
        return availablePoints;
    }

    /**
     * 残りポイントを設定
     */
    public void setAvailablePoints(int points) {
        this.availablePoints = Math.max(0, points);
    }

    /**
     * ポイントを追加
     */
    public void addPoints(int points) {
        this.availablePoints += Math.max(0, points);
    }

    /**
	 * ステータスにポイントを配分
	 *
	 * @param stat  対象ステータス
	 * @param points 配分ポイント数
	 * @return 実際に配分されたポイント数（残りポイント不足の場合は少なくなる）
	 * @throws IllegalArgumentException statがnullの場合
	 */
	public int allocatePoint(Stat stat, int points) {
		if (stat == null) {
			throw new IllegalArgumentException("Stat cannot be null");
		}
		if (points <= 0) {
			return 0;
		}

		// 残りポイントを考慮
		int actualPoints = Math.min(points, availablePoints);
		if (actualPoints <= 0) {
			return 0;
		}

		// ポイント配分
		int currentValue = getBaseStat(stat);
		setBaseStat(stat, currentValue + actualPoints);
		availablePoints -= actualPoints;

		return actualPoints;
	}

    /**
	 * ステータスからポイントを削除
	 *
	 * @param stat  対象ステータス
	 * @param points 削除ポイント数
	 * @return 実際に削除されたポイント数（現在値不足の場合は少なくなる）
	 * @throws IllegalArgumentException statがnullの場合
	 */
	public int deallocatePoint(Stat stat, int points) {
		if (stat == null) {
			throw new IllegalArgumentException("Stat cannot be null");
		}
		if (points <= 0) {
			return 0;
		}

		int currentValue = getBaseStat(stat);
		int actualPoints = Math.min(points, currentValue);

		if (actualPoints <= 0) {
			return 0;
		}

		// ポイント削除
		setBaseStat(stat, currentValue - actualPoints);
		availablePoints += actualPoints;

		return actualPoints;
	}

    /**
     * すべての手動配分ステータスを取得
     */
    public Map<Stat, Integer> getAllBaseStats() {
        return new EnumMap<>(baseStats);
    }

    /**
     * すべての自動配分ステータスを取得
     */
    public Map<Stat, Integer> getAllAutoStats() {
        return new EnumMap<>(autoStats);
    }

    /**
     * すべての合計ステータスを取得
     */
    public Map<Stat, Integer> getAllTotalStats() {
        Map<Stat, Integer> totalStats = new EnumMap<>(Stat.class);
        for (Stat stat : Stat.values()) {
            totalStats.put(stat, getTotalStat(stat));
        }
        return totalStats;
    }

    /**
     * 配分済みの手動ポイント総数を取得
     */
    public int getTotalAllocatedPoints() {
        return baseStats.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * 自動配分ポイント総数を取得
     */
    public int getTotalAutoPoints() {
        return autoStats.values().stream().mapToInt(Integer::intValue).sum();
    }
}
