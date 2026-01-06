package com.example.rpgplugin.skill;

import org.bukkit.ChatColor;

/**
 * スキルタイプを表す列挙型
 *
 * <p>スキルをアクティブとパッシブに分類し、各タイプの表示名と色を管理します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: 各スキルタイプが単一の役割を担当</li>
 *   <li>DRY: 表示情報を一元管理</li>
 *   <li>KISS: シンプルなEnum構造</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public enum SkillType {

    /**
     * アクティブスキル
     *
     * <p>プレイヤーが手動で発動するスキルです。</p>
     * <p>クールダウンとMP消費があります。</p>
     */
    ACTIVE("active", "アクティブ", ChatColor.GOLD, "手動発動・クールダウンあり"),

    /**
     * パッシブスキル
     *
     * <p>常時発動しているスキルです。</p>
     * <p>特定条件で自動的に効果を発揮します。</p>
     */
    PASSIVE("passive", "パッシブ", ChatColor.GREEN, "常時発動・自動効果");

    private final String id;
    private final String displayName;
    private final ChatColor color;
    private final String description;

    /**
     * コンストラクタ
     *
     * @param id スキルタイプID
     * @param displayName 表示名
     * @param color チャットカラー
     * @param description 説明文
     */
    SkillType(String id, String displayName, ChatColor color, String description) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.description = description;
    }

    /**
     * スキルタイプIDを取得します
     *
     * @return スキルタイプID
     */
    public String getId() {
        return id;
    }

    /**
     * 表示名を取得します
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
     * @return カラー付き表示名
     */
    public String getColoredName() {
        return color + displayName;
    }

    /**
     * IDからSkillTypeを取得します
     *
     * <p>大文字小文字を区別しません。</p>
     *
     * @param id スキルタイプID
     * @return 対応するSkillType、見つからない場合はnull
     */
    public static SkillType fromId(String id) {
        if (id == null) {
            return null;
        }

        for (SkillType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return id + " (" + displayName + ")";
    }
}
