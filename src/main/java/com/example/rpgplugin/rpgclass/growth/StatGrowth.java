package com.example.rpgplugin.rpgclass.growth;

import com.example.rpgplugin.stats.Stat;
import org.bukkit.configuration.ConfigurationSection;

import java.util.EnumMap;
import java.util.Map;

/**
 * ステータス成長設定を管理するクラス
 * レベルアップ時の自動配分と手動配分ポイントを定義
 */
public class StatGrowth {

    /** 自動配分されるステータス値 */
    private final Map<Stat, Integer> autoGrowth;

    /** レベルアップ時に付与される手動配分ポイント */
    private final int manualPoints;

    /**
     * コンストラクタ
     *
     * @param autoGrowth   自動成長マップ
     * @param manualPoints 手動配分ポイント
     */
    private StatGrowth(Map<Stat, Integer> autoGrowth, int manualPoints) {
        this.autoGrowth = new EnumMap<>(autoGrowth);
        this.manualPoints = Math.max(0, manualPoints);
    }

    /**
     * 自動成長値を取得
     *
     * @param stat ステータス
     * @return 成長値（存在しない場合は0）
     */
    public int getAutoGrowth(Stat stat) {
        return autoGrowth.getOrDefault(stat, 0);
    }

    /**
     * すべての自動成長値を取得
     *
     * @return 自動成長マップのコピー
     */
    public Map<Stat, Integer> getAllAutoGrowth() {
        return new EnumMap<>(autoGrowth);
    }

    /**
     * 手動配分ポイントを取得
     *
     * @return ポイント数
     */
    public int getManualPoints() {
        return manualPoints;
    }

    /**
     * ConfigurationSectionからStatGrowthをパース
     *
     * @param section 設定セクション
     * @return StatGrowthインスタンス
     * @throws IllegalArgumentException 設定が不正な場合
     */
    public static StatGrowth parse(ConfigurationSection section) {
        if (section == null) {
            throw new IllegalArgumentException("stat_growth section cannot be null");
        }

        Map<Stat, Integer> autoGrowth = new EnumMap<>(Stat.class);

        // 自動配分の読み込み
        ConfigurationSection autoSection = section.getConfigurationSection("auto");
        if (autoSection != null) {
            for (String statName : autoSection.getKeys(false)) {
                try {
                    Stat stat = Stat.valueOf(statName.toUpperCase());
                    int value = autoSection.getInt(statName, 0);
                    if (value > 0) {
                        autoGrowth.put(stat, value);
                    }
                } catch (IllegalArgumentException e) {
                    // 不明なステータスは警告してスキップ
                    java.util.logging.Logger.getLogger("StatGrowth")
                            .warning("Unknown stat type: " + statName);
                }
            }
        }

        // 手動配分ポイントの読み込み
        int manualPoints = section.getInt("manual_points", 0);

        return new StatGrowth(autoGrowth, manualPoints);
    }

    /**
     * ビルダークラス
     */
    public static class Builder {
        private final Map<Stat, Integer> autoGrowth;
        private int manualPoints;

        public Builder() {
            this.autoGrowth = new EnumMap<>(Stat.class);
            this.manualPoints = 0;
        }

        /**
         * 自動成長を設定
         *
         * @param stat  対象ステータス
         * @param value 成長値
         * @return ビルダー
         */
        public Builder setAutoGrowth(Stat stat, int value) {
            if (value > 0) {
                autoGrowth.put(stat, value);
            }
            return this;
        }

        /**
         * 手動配分ポイントを設定
         *
         * @param points ポイント数
         * @return ビルダー
         */
        public Builder setManualPoints(int points) {
            this.manualPoints = Math.max(0, points);
            return this;
        }

        /**
         * StatGrowthを構築
         *
         * @return StatGrowthインスタンス
         */
        public StatGrowth build() {
            return new StatGrowth(autoGrowth, manualPoints);
        }
    }

    @Override
    public String toString() {
        return "StatGrowth{" +
                "autoGrowth=" + autoGrowth +
                ", manualPoints=" + manualPoints +
                '}';
    }
}
