package com.example.rpgplugin.rpgclass.requirements;

import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

/**
 * レベル要件を表すクラス
 */
public class LevelRequirement implements ClassRequirement {

    private final int requiredLevel;

    /**
     * コンストラクタ
     *
     * @param requiredLevel 必要レベル
     */
    public LevelRequirement(int requiredLevel) {
        this.requiredLevel = Math.max(1, requiredLevel);
    }

    @Override
    public boolean check(Player player) {
        if (player == null) {
            return false;
        }

        // Vanillaレベルを取得
        int playerLevel = player.getLevel();
        return playerLevel >= requiredLevel;
    }

    @Override
    public String getDescription() {
        return "レベル " + requiredLevel + " 以上";
    }

    @Override
    public String getType() {
        return "level";
    }

    /**
     * 必要レベルを取得
     *
     * @return 必要レベル
     */
    public int getRequiredLevel() {
        return requiredLevel;
    }

    /**
     * ConfigurationSectionからパース
     *
     * @param section 設定セクション
     * @return LevelRequirementインスタンス
     */
    public static LevelRequirement parse(ConfigurationSection section) {
        int level = section.getInt("value", 1);
        return new LevelRequirement(level);
    }
}
