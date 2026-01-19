package com.example.rpgplugin.player.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 経験値減衰設定
 *
 * <p>exp/diminish_config.ymlから読み込まれる設定を管理します。</p>
 *
 * <p>設定項目:</p>
 * <ul>
 *   <li>diminish_table: レベル範囲ごとの減衰率</li>
 *   <li>mob_exp_table: モブごとの基礎経験値と最大レベル</li>
 *   <li>exemptions: 減衰除外条件</li>
 *   <li>formula: 減衰計算式</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class DiminishConfig {

    private final Logger logger;

    // 減衰テーブル: レベル範囲 -> 減衰率
    private final Map<LevelRange, Double> diminishTable = new HashMap<>();

    // モブ経験値テーブル: モブID -> モブ経験値設定
    private final Map<String, MobExpConfig> mobExpTable = new HashMap<>();

    // 除外設定
    private boolean playerKillsExempt = true;
    private boolean bossMobsExempt = true;
    private boolean eventRewardsExempt = true;

    // 計算式
    private String formula = "BASE_EXP * (1.0 - REDUCTION_RATE)";

    /**
     * レベル範囲クラス
     */
    public static class LevelRange {
        private final int minLevel;
        private final Integer maxLevel;

        public LevelRange(int minLevel, Integer maxLevel) {
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
        }

        public boolean contains(int level) {
            if (maxLevel == null) {
                return level >= minLevel;
            }
            return level >= minLevel && level <= maxLevel;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LevelRange that = (LevelRange) o;
            if (minLevel != that.minLevel) return false;
            return maxLevel != null ? maxLevel.equals(that.maxLevel) : that.maxLevel == null;
        }

        @Override
        public int hashCode() {
            int result = minLevel;
            result = 31 * result + (maxLevel != null ? maxLevel.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return maxLevel == null ? (minLevel + "+") : (minLevel + "-" + maxLevel);
        }
    }

    /**
     * モブ経験値設定
     */
    public static class MobExpConfig {
        private final String mobId;
        private final int baseExp;
        private final int maxLevel;

        public MobExpConfig(String mobId, int baseExp, int maxLevel) {
            this.mobId = mobId;
            this.baseExp = baseExp;
            this.maxLevel = maxLevel;
        }

        public String getMobId() {
            return mobId;
        }

        public int getBaseExp() {
            return baseExp;
        }

        public int getMaxLevel() {
            return maxLevel;
        }
    }

    /**
     * コンストラクタ
     *
     * @param logger ロガー
     */
    public DiminishConfig(Logger logger) {
        this.logger = logger;
    }

    /**
     * 設定を読み込みます
     *
     * @param config 設定
     * @return 成功した場合はtrue
     */
    public boolean load(@NotNull FileConfiguration config) {
        try {
            // 減衰テーブルを読み込み
            loadDiminishTable(config);

            // モブ経験値テーブルを読み込み
            loadMobExpTable(config);

            // 除外設定を読み込み
            loadExemptions(config);

            // 計算式を読み込み
            loadFormula(config);

            logger.info("DiminishConfig loaded: " + diminishTable.size() + " level ranges, "
                    + mobExpTable.size() + " mob configs");
            return true;

        } catch (Exception e) {
            logger.warning("Failed to load DiminishConfig: " + e.getMessage());
            return false;
        }
    }

    /**
     * 減衰テーブルを読み込みます
     */
    private void loadDiminishTable(FileConfiguration config) {
        diminishTable.clear();

        List<?> table = config.getList("diminish_table");
        if (table == null || table.isEmpty()) {
            // デフォルト値を設定
            logger.info("No diminish_table found, using defaults");
            setDefaultDiminishTable();
            return;
        }

        for (Object entryObj : table) {
            if (!(entryObj instanceof Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> entry = (Map<String, Object>) entryObj;

            Object rangeObj = entry.get("level_range");
            Object rateObj = entry.get("reduction_rate");

            if (rangeObj == null || rateObj == null) {
                continue;
            }

            String rangeStr = rangeObj.toString();
            LevelRange range = parseLevelRange(rangeStr);
            double rate = ((Number) rateObj).doubleValue();

            if (range != null && rate >= 0.0 && rate <= 1.0) {
                diminishTable.put(range, rate);
            }
        }
    }

    /**
     * レベル範囲を解析します
     */
    private LevelRange parseLevelRange(String rangeStr) {
        if (rangeStr == null || rangeStr.isEmpty()) {
            return null;
        }

        try {
            if (rangeStr.contains("+")) {
                // "70+" 形式
                int min = Integer.parseInt(rangeStr.replace("+", ""));
                return new LevelRange(min, null);
            } else if (rangeStr.contains("-")) {
                // "30-39" 形式
                String[] parts = rangeStr.split("-");
                int min = Integer.parseInt(parts[0].trim());
                int max = Integer.parseInt(parts[1].trim());
                return new LevelRange(min, max);
            }
        } catch (NumberFormatException e) {
            logger.warning("Invalid level range: " + rangeStr);
        }

        return null;
    }

    /**
     * デフォルトの減衰テーブルを設定します
     */
    private void setDefaultDiminishTable() {
        diminishTable.put(new LevelRange(30, 39), 0.5);
        diminishTable.put(new LevelRange(40, 49), 0.6);
        diminishTable.put(new LevelRange(50, 59), 0.7);
        diminishTable.put(new LevelRange(60, 69), 0.8);
        diminishTable.put(new LevelRange(70, null), 0.9);
    }

    /**
     * モブ経験値テーブルを読み込みます
     */
    private void loadMobExpTable(FileConfiguration config) {
        mobExpTable.clear();

        var mobSection = config.getConfigurationSection("mob_exp_table");
        if (mobSection == null) {
            return;
        }

        for (String mobId : mobSection.getKeys(false)) {
            var mobConfig = mobSection.getConfigurationSection(mobId);
            if (mobConfig == null) {
                continue;
            }

            int baseExp = mobConfig.getInt("base_exp", 20);
            int maxLevel = mobConfig.getInt("max_level", 69);

            mobExpTable.put(mobId.toLowerCase(), new MobExpConfig(mobId, baseExp, maxLevel));
        }
    }

    /**
     * 除外設定を読み込みます
     */
    private void loadExemptions(FileConfiguration config) {
        var exemptSection = config.getConfigurationSection("exemptions");
        if (exemptSection == null) {
            return;
        }

        playerKillsExempt = exemptSection.getBoolean("player_kills", true);
        bossMobsExempt = exemptSection.getBoolean("boss_mobs", true);
        eventRewardsExempt = exemptSection.getBoolean("event_rewards", true);
    }

    /**
     * 計算式を読み込みます
     */
    private void loadFormula(FileConfiguration config) {
        formula = config.getString("formula", "BASE_EXP * (1.0 - REDUCTION_RATE)");
    }

    /**
     * 指定レベルの減衰率を取得します
     *
     * @param level レベル
     * @return 減衰率（0.0-1.0）、該当する範囲がない場合は0.0
     */
    public double getReductionRate(int level) {
        for (Map.Entry<LevelRange, Double> entry : diminishTable.entrySet()) {
            if (entry.getKey().contains(level)) {
                return entry.getValue();
            }
        }
        return 0.0;
    }

    /**
     * 減衰開始レベルを取得します
     *
     * @return 開始レベル、設定がない場合はInteger.MAX_VALUE
     */
    public int getStartLevel() {
        int minStart = Integer.MAX_VALUE;
        for (LevelRange range : diminishTable.keySet()) {
            if (range.minLevel < minStart) {
                minStart = range.minLevel;
            }
        }
        return minStart == Integer.MAX_VALUE ? 30 : minStart;
    }

    /**
     * モブ経験値設定を取得します
     *
     * @param mobId モブID
     * @return モブ経験値設定、ない場合はnull
     */
    @Nullable
    public MobExpConfig getMobExpConfig(String mobId) {
        return mobExpTable.get(mobId.toLowerCase());
    }

    /**
     * モブの基礎経験値を取得します
     *
     * @param mobId     モブID
     * @param defaultExp デフォルト経験値
     * @return 基礎経験値
     */
    public int getMobBaseExp(String mobId, int defaultExp) {
        MobExpConfig config = getMobExpConfig(mobId);
        return config != null ? config.getBaseExp() : defaultExp;
    }

    /**
     * モブの最大レベルを取得します
     *
     * @param mobId        モブID
     * @param defaultMaxLv デフォルト最大レベル
     * @return 最大レベル
     */
    public int getMobMaxLevel(String mobId, int defaultMaxLv) {
        MobExpConfig config = getMobExpConfig(mobId);
        return config != null ? config.getMaxLevel() : defaultMaxLv;
    }

    /**
     * 除外設定を取得します
     */
    public boolean isPlayerKillsExempt() {
        return playerKillsExempt;
    }

    public boolean isBossMobsExempt() {
        return bossMobsExempt;
    }

    public boolean isEventRewardsExempt() {
        return eventRewardsExempt;
    }

    /**
     * 計算式を取得します
     */
    public String getFormula() {
        return formula;
    }
}
