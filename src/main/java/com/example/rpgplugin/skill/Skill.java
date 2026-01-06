package com.example.rpgplugin.skill;

import com.example.rpgplugin.stats.Stat;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * スキルデータモデル
 *
 * <p>スキルの基本情報、ダメージ計算式、習得要件などを管理します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキルデータの表現に専念</li>
 *   <li>DRY: 計算ロジックを一元管理</li>
 *   <li>YAGNI: 必要な機能のみ実装</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class Skill {

    private final String id;
    private final String name;
    private final String displayName;
    private final SkillType type;
    private final List<String> description;
    private final int maxLevel;
    private final double cooldown;
    private final int manaCost;
    private final DamageCalculation damage;
    private final SkillTreeConfig skillTree;
    private final String iconMaterial;
    private final List<String> availableClasses;

    /**
     * ダメージ計算設定
     */
    public static class DamageCalculation {
        private final double base;
        private final Stat statMultiplier;
        private final double multiplierValue;
        private final double levelMultiplier;

        public DamageCalculation(double base, Stat statMultiplier, double multiplierValue, double levelMultiplier) {
            this.base = base;
            this.statMultiplier = statMultiplier;
            this.multiplierValue = multiplierValue;
            this.levelMultiplier = levelMultiplier;
        }

        public double getBase() {
            return base;
        }

        public Stat getStatMultiplier() {
            return statMultiplier;
        }

        public double getMultiplierValue() {
            return multiplierValue;
        }

        public double getLevelMultiplier() {
            return levelMultiplier;
        }

        /**
         * ダメージを計算します
         *
         * @param statValue ステータス値
         * @param skillLevel スキルレベル
         * @return 計算されたダメージ
         */
        public double calculateDamage(double statValue, int skillLevel) {
            return base + (statValue * multiplierValue) + (skillLevel * levelMultiplier);
        }
    }

    /**
     * スキルツリー設定
     */
    public static class SkillTreeConfig {
        private final String parent;
        private final List<UnlockRequirement> unlockRequirements;
        private final int cost;

        public SkillTreeConfig(String parent, List<UnlockRequirement> unlockRequirements, int cost) {
            this.parent = parent;
            this.unlockRequirements = unlockRequirements != null ? unlockRequirements : new ArrayList<>();
            this.cost = cost;
        }

        public String getParent() {
            return parent;
        }

        public List<UnlockRequirement> getUnlockRequirements() {
            return unlockRequirements;
        }

        public int getCost() {
            return cost;
        }
    }

    /**
     * 習得要件
     */
    public static class UnlockRequirement {
        private final String type;
        private final Stat stat;
        private final double value;

        public UnlockRequirement(String type, Stat stat, double value) {
            this.type = type;
            this.stat = stat;
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public Stat getStat() {
            return stat;
        }

        public double getValue() {
            return value;
        }
    }

    /**
     * コンストラクタ
     *
     * @param id スキルID
     * @param name スキル名
     * @param displayName 表示名（カラーコード対応）
     * @param type スキルタイプ
     * @param description 説明リスト
     * @param maxLevel 最大レベル
     * @param cooldown クールダウン（秒）
     * @param manaCost 消費MP
     * @param damage ダメージ計算設定
     * @param skillTree スキルツリー設定
     * @param iconMaterial アイコン素材
     * @param availableClasses 利用可能なクラスリスト
     */
    public Skill(String id, String name, String displayName, SkillType type, List<String> description,
                 int maxLevel, double cooldown, int manaCost, DamageCalculation damage,
                 SkillTreeConfig skillTree, String iconMaterial, List<String> availableClasses) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.description = description != null ? new ArrayList<>(description) : new ArrayList<>();
        this.maxLevel = maxLevel;
        this.cooldown = cooldown;
        this.manaCost = manaCost;
        this.damage = damage;
        this.skillTree = skillTree;
        this.iconMaterial = iconMaterial != null ? iconMaterial : "DIAMOND_SWORD";
        this.availableClasses = availableClasses != null ? new ArrayList<>(availableClasses) : new ArrayList<>();
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColoredDisplayName() {
        return ChatColor.translateAlternateColorCodes('&', displayName);
    }

    public SkillType getType() {
        return type;
    }

    public List<String> getDescription() {
        return new ArrayList<>(description);
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public double getCooldown() {
        return cooldown;
    }

    public int getManaCost() {
        return manaCost;
    }

    public DamageCalculation getDamage() {
        return damage;
    }

    public SkillTreeConfig getSkillTree() {
        return skillTree;
    }

    public String getIconMaterial() {
        return iconMaterial;
    }

    public List<String> getAvailableClasses() {
        return new ArrayList<>(availableClasses);
    }

    /**
     * 指定されたクラスでこのスキルを使用可能かチェックします
     *
     * @param classId クラスID
     * @return 使用可能な場合はtrue
     */
    public boolean isAvailableForClass(String classId) {
        if (availableClasses.isEmpty()) {
            return true; // 空リストは全クラスで利用可能
        }
        return availableClasses.contains(classId);
    }

    /**
     * アクティブスキルかチェックします
     *
     * @return アクティブスキルの場合はtrue
     */
    public boolean isActive() {
        return type == SkillType.ACTIVE;
    }

    /**
     * パッシブスキルかチェックします
     *
     * @return パッシブスキルの場合はtrue
     */
    public boolean isPassive() {
        return type == SkillType.PASSIVE;
    }

    @Override
    public String toString() {
        return "Skill{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", maxLevel=" + maxLevel +
                '}';
    }
}
