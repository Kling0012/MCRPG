package com.example.rpgplugin.class;

import com.example.rpgplugin.class.growth.StatGrowth;
import com.example.rpgplugin.class.requirements.ClassRequirement;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * クラスデータを表すクラス
 * 4初期クラス（戦士、大盾使い、魔法使い、弓使い）と
 * Rank6までのクラスアップシステム（直線/分岐対応）を管理
 */
public class RPGClass {

    /** クラスID */
    private final String id;

    /** クラス名 */
    private final String name;

    /** 表示名（カラーコード対応） */
    private final String displayName;

    /** 説明文リスト */
    private final List<String> description;

    /** ランク（1-6） */
    private final int rank;

    /** 最大レベル */
    private final int maxLevel;

    /** アイコン */
    private final Material icon;

    /** ステータス成長設定 */
    private final StatGrowth statGrowth;

    /** 次のランクのクラスID（直線パターン） */
    private String nextRankClassId;

    /** 次のランクへの要件（直線パターン） */
    private final List<ClassRequirement> nextRankRequirements;

    /** 分岐パターンの代替クラス（Rank2以降） */
    private final Map<String, List<ClassRequirement>> alternativeRanks;

    /** 使用可能スキルIDリスト */
    private final List<String> availableSkills;

    /** パッシブボーナス */
    private final List<PassiveBonus> passiveBonuses;

    /** 経験値減衰設定 */
    private final ExpDiminish expDiminish;

    /**
     * コンストラクタ
     */
    private RPGClass(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.displayName = builder.displayName;
        this.description = new ArrayList<>(builder.description);
        this.rank = builder.rank;
        this.maxLevel = builder.maxLevel;
        this.icon = builder.icon;
        this.statGrowth = builder.statGrowth;
        this.nextRankClassId = builder.nextRankClassId;
        this.nextRankRequirements = new ArrayList<>(builder.nextRankRequirements);
        this.alternativeRanks = new HashMap<>(builder.alternativeRanks);
        this.availableSkills = new ArrayList<>(builder.availableSkills);
        this.passiveBonuses = new ArrayList<>(builder.passiveBonuses);
        this.expDiminish = builder.expDiminish;
    }

    // ========== Getters ==========

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getDescription() {
        return new ArrayList<>(description);
    }

    public int getRank() {
        return rank;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public Material getIcon() {
        return icon;
    }

    public StatGrowth getStatGrowth() {
        return statGrowth;
    }

    public Optional<String> getNextRankClassId() {
        return Optional.ofNullable(nextRankClassId);
    }

    public List<ClassRequirement> getNextRankRequirements() {
        return new ArrayList<>(nextRankRequirements);
    }

    public Map<String, List<ClassRequirement>> getAlternativeRanks() {
        return new HashMap<>(alternativeRanks);
    }

    public List<String> getAvailableSkills() {
        return new ArrayList<>(availableSkills);
    }

    public List<PassiveBonus> getPassiveBonuses() {
        return new ArrayList<>(passiveBonuses);
    }

    public ExpDiminish getExpDiminish() {
        return expDiminish;
    }

    // ========== Public Methods ==========

    /**
     * 次のランクが存在するか
     *
     * @return 次のランクがある場合はtrue
     */
    public boolean hasNextRank() {
        return nextRankClassId != null && !nextRankClassId.isEmpty();
    }

    /**
     * 分岐パターンが存在するか
     *
     * @return 分岐がある場合はtrue
     */
    public boolean hasAlternativeRanks() {
        return !alternativeRanks.isEmpty();
    }

    /**
     * アイテムスタックとしてのアイコンを取得
     *
     * @return アイテムスタック
     */
    public ItemStack getIconItem() {
        return new ItemStack(icon);
    }

    // ========== Inner Classes ==========

    /**
     * パッシブボーナス
     */
    public static class PassiveBonus {
        private final String type;
        private final double value;

        public PassiveBonus(String type, double value) {
            this.type = type;
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public double getValue() {
            return value;
        }

        /**
         * ConfigurationSectionからパース
         */
        public static PassiveBonus parse(ConfigurationSection section) {
            String type = section.getString("type", "unknown");
            double value = section.getDouble("value", 0);
            return new PassiveBonus(type, value);
        }
    }

    /**
     * 経験値減衰設定
     */
    public static class ExpDiminish {
        private final int startLevel;
        private final double reductionRate;

        public ExpDiminish(int startLevel, double reductionRate) {
            this.startLevel = Math.max(0, startLevel);
            this.reductionRate = Math.max(0, Math.min(1, reductionRate));
        }

        public int getStartLevel() {
            return startLevel;
        }

        public double getReductionRate() {
            return reductionRate;
        }

        /**
         * ConfigurationSectionからパース
         */
        public static ExpDiminish parse(ConfigurationSection section) {
            int start = section.getInt("start_level", 30);
            double rate = section.getDouble("reduction_rate", 0.5);
            return new ExpDiminish(start, rate);
        }

        /**
         * 減衰を適用した経験値を計算
         *
         * @param baseExp 基本経験値
         * @param level   レベル
         * @return 適用後経験値
         */
        public long applyExp(long baseExp, int level) {
            if (level < startLevel) {
                return baseExp;
            }
            return (long) (baseExp * (1 - reductionRate));
        }
    }

    /**
     * ビルダークラス
     */
    public static class Builder {
        private String id;
        private String name;
        private String displayName;
        private List<String> description = new ArrayList<>();
        private int rank = 1;
        private int maxLevel = 50;
        private Material icon = Material.DIAMOND_SWORD;
        private StatGrowth statGrowth;
        private String nextRankClassId;
        private List<ClassRequirement> nextRankRequirements = new ArrayList<>();
        private Map<String, List<ClassRequirement>> alternativeRanks = new HashMap<>();
        private List<String> availableSkills = new ArrayList<>();
        private List<PassiveBonus> passiveBonuses = new ArrayList<>();
        private ExpDiminish expDiminish = new ExpDiminish(30, 0.5);

        public Builder(String id) {
            this.id = id;
            this.name = id;
            this.displayName = id;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder setDescription(List<String> description) {
            this.description = new ArrayList<>(description);
            return this;
        }

        public Builder setRank(int rank) {
            this.rank = Math.max(1, Math.min(6, rank));
            return this;
        }

        public Builder setMaxLevel(int maxLevel) {
            this.maxLevel = Math.max(1, maxLevel);
            return this;
        }

        public Builder setIcon(Material icon) {
            this.icon = icon;
            return this;
        }

        public Builder setStatGrowth(StatGrowth statGrowth) {
            this.statGrowth = statGrowth;
            return this;
        }

        public Builder setNextRankClassId(String nextRankClassId) {
            this.nextRankClassId = nextRankClassId;
            return this;
        }

        public Builder addNextRankRequirement(ClassRequirement requirement) {
            this.nextRankRequirements.add(requirement);
            return this;
        }

        public Builder addAlternativeRank(String classId, List<ClassRequirement> requirements) {
            this.alternativeRanks.put(classId, requirements);
            return this;
        }

        public Builder addAvailableSkill(String skillId) {
            this.availableSkills.add(skillId);
            return this;
        }

        public Builder addPassiveBonus(PassiveBonus bonus) {
            this.passiveBonuses.add(bonus);
            return this;
        }

        public Builder setExpDiminish(ExpDiminish expDiminish) {
            this.expDiminish = expDiminish;
            return this;
        }

        public RPGClass build() {
            return new RPGClass(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RPGClass rpgClass = (RPGClass) o;
        return id.equals(rpgClass.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "RPGClass{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", rank=" + rank +
                ", maxLevel=" + maxLevel +
                '}';
    }
}
