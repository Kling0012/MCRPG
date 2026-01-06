package com.example.rpgplugin.class.requirements;

import com.example.rpgplugin.player.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

/**
 * レベル要件を表すクラス
 */
public class LevelRequirement implements ClassRequirement {

    private final int requiredLevel;
    private final PlayerManager playerManager;

    /**
     * コンストラクタ
     *
     * @param requiredLevel 必要レベル
     * @param playerManager プレイヤーマネージャー
     */
    public LevelRequirement(int requiredLevel, PlayerManager playerManager) {
        this.requiredLevel = Math.max(1, requiredLevel);
        this.playerManager = playerManager;
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
     * @param section        設定セクション
     * @param playerManager  プレイヤーマネージャー
     * @return LevelRequirementインスタンス
     */
    public static LevelRequirement parse(ConfigurationSection section, PlayerManager playerManager) {
        int level = section.getInt("value", 1);
        return new LevelRequirement(level, playerManager);
    }
}
