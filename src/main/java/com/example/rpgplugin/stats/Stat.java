package com.example.rpgplugin.stats;

import org.bukkit.ChatColor;

/**
 * ステータス種別を表す列挙型
 *
 * <p>5つの主要ステータス（STR/INT/SPI/VIT/DEX）を定義し、
 * 各ステータスの表示名、説明、色を管理します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: 各ステータスが単一の役割を担当</li>
 *   <li>DRY: 表示情報を一元管理</li>
 *   <li>KISS: シンプルなEnum構造</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public enum Stat {

    /**
     * 筋力（STR）
     *
     * <p>物理攻撃力に影響します。</p>
     * <p>近距離武器のダメージ計算に使用されます。</p>
     */
    STRENGTH("STR", "筋力", ChatColor.RED, "物理攻撃力に影響"),

    /**
     * 知力（INT）
     *
     * <p>魔法攻撃力に影響します。</p>
     * <p>魔法スキルのダメージ計算に使用されます。</p>
     */
    INTELLIGENCE("INT", "知力", ChatColor.BLUE, "魔法攻撃力に影響"),

    /**
     * 精神（SPI）
     *
     * <p>MP回復・魔法防御に影響します。</p>
     * <p>MPの自然回復速度と魔法耐性に使用されます。</p>
     */
    SPIRIT("SPI", "精神", ChatColor.AQUA, "MP回復・魔法防御に影響"),

    /**
     * 生命力（VIT）
     *
     * <p>HP・防御力に影響します。</p>
     * <p>最大HPと物理防御力に使用されます。</p>
     */
    VITALITY("VIT", "生命力", ChatColor.GREEN, "HP・防御力に影響"),

    /**
     * 器用さ（DEX）
     *
     * <p>命中率・クリティカルに影響します。</p>
     * <p>攻撃の命中率とクリティカル率に使用されます。</p>
     */
    DEXTERITY("DEX", "器用さ", ChatColor.YELLOW, "命中率・クリティカルに影響");

    private final String shortName;
    private final String displayName;
    private final ChatColor color;
    private final String description;

    /**
     * コンストラクタ
     *
     * @param shortName 短縮名（3文字）
     * @param displayName 表示名
     * @param color チャットカラー
     * @param description 説明文
     */
    Stat(String shortName, String displayName, ChatColor color, String description) {
        this.shortName = shortName;
        this.displayName = displayName;
        this.color = color;
        this.description = description;
    }

    /**
     * 短縮名を取得します
     *
     * <p>例: "STR", "INT"</p>
     *
     * @return 短縮名
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * 表示名を取得します
     *
     * <p>例: "筋力", "知力"</p>
     *
     * @return 表示名
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * チャットカラーを取得します
     *
     * @return チャットカラー
     */
    public ChatColor getColor() {
        return color;
    }

    /**
     * 説明文を取得します
     *
     * @return 説明文
     */
    public String getDescription() {
        return description;
    }

    /**
     * カラー付きの表示名を取得します
     *
     * <p>例: "§c筋力"</p>
     *
     * @return カラー付き表示名
     */
    public String getColoredName() {
        return color + displayName;
    }

    /**
     * カラー付きの短縮名を取得します
     *
     * <p>例: "§cSTR"</p>
     *
     * @return カラー付き短縮名
     */
    public String getColoredShortName() {
        return color + shortName;
    }

    /**
     * 短縮名からStatを取得します
     *
     * <p>大文字小文字を区別しません。</p>
     *
     * @param shortName 短縮名
     * @return 対応するStat、見つからない場合はnull
     */
    public static Stat fromShortName(String shortName) {
        if (shortName == null) {
            return null;
        }

        for (Stat stat : values()) {
            if (stat.shortName.equalsIgnoreCase(shortName)) {
                return stat;
            }
        }
        return null;
    }

    /**
     * 表示名からStatを取得します
     *
     * <p>大文字小文字を区別しません。</p>
     *
     * @param displayName 表示名
     * @return 対応するStat、見つからない場合はnull
     */
    public static Stat fromDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }

        for (Stat stat : values()) {
            if (stat.displayName.equalsIgnoreCase(displayName)) {
                return stat;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return shortName + " (" + displayName + ")";
    }
}
