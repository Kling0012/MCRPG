package com.example.rpgplugin.class.requirements;

import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

/**
 * ステータス要件を表すクラス
 */
public class StatRequirement implements ClassRequirement {

    private final Stat requiredStat;
    private final int requiredValue;
    private final PlayerManager playerManager;

    /**
     * コンストラクタ
     *
     * @param requiredStat   必要ステータス
     * @param requiredValue  必要値
     * @param playerManager  プレイヤーマネージャー
     */
    public StatRequirement(Stat requiredStat, int requiredValue, PlayerManager playerManager) {
        this.requiredStat = requiredStat;
        this.requiredValue = Math.max(0, requiredValue);
        this.playerManager = playerManager;
    }

    @Override
    public boolean check(Player player) {
        if (player == null) {
            return false;
        }

        return playerManager.getRPGPlayer(player.getUniqueId())
                .map(rpgPlayer -> rpgPlayer.getFinalStat(requiredStat) >= requiredValue)
                .orElse(false);
    }

    @Override
    public String getDescription() {
        return requiredStat.getDisplayName() + " が " + requiredValue + " 以上";
    }

    @Override
    public String getType() {
        return "stat";
    }

    /**
     * 必要ステータスを取得
     *
     * @return ステータス
     */
    public Stat getRequiredStat() {
        return requiredStat;
    }

    /**
     * 必要値を取得
     *
     * @return 必要値
     */
    public int getRequiredValue() {
        return requiredValue;
    }

    /**
     * ConfigurationSectionからパース
     *
     * @param section        設定セクション
     * @param playerManager  プレイヤーマネージャー
     * @return StatRequirementインスタンス
     */
    public static StatRequirement parse(ConfigurationSection section, PlayerManager playerManager) {
        String statName = section.getString("stat", "STRENGTH");
        int value = section.getInt("value", 0);

        try {
            Stat stat = Stat.valueOf(statName.toUpperCase());
            return new StatRequirement(stat, value, playerManager);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown stat type: " + statName, e);
        }
    }
}
