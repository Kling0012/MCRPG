package com.example.rpgplugin.stats;

import org.bukkit.ChatColor;

/**
 * ステータス種別を表すEnum
 */
public enum Stat {
    STRENGTH("力", "STR", ChatColor.RED, "剣の攻撃力に影響"),
    INTELLIGENCE("知力", "INT", ChatColor.BLUE, "魔法の威力に影響"),
    SPIRIT("精神", "SPI", ChatColor.LIGHT_PURPLE, "MP回復・魔法防御に影響"),
    VITALITY("体力", "VIT", ChatColor.GREEN, "HP・防御力に影響"),
    DEXTERITY("器用さ", "DEX", ChatColor.AQUA, "命中率・クリティカルに影響");

    private final String displayName;
    private final String shortName;
    private final ChatColor color;
    private final String description;

    Stat(String displayName, String shortName, ChatColor color, String description) {
        this.displayName = displayName;
        this.shortName = shortName;
        this.color = color;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortName() {
        return shortName;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getDescription() {
        return description;
    }

    /**
     * カラー付き表示名を取得
     */
    public String getColoredName() {
        return color + displayName;
    }

    /**
     * カラー付き短縮名を取得
     */
    public String getColoredShortName() {
        return color + shortName;
    }
}
