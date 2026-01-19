package com.example.rpgplugin.api.placeholder;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.RPGPluginAPI;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.rpgclass.RPGClass;
import com.example.rpgplugin.stats.Stat;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * RPGPluginのPlaceholderAPI拡張
 *
 * <p>PlaceholderAPIとの統合を提供します。</p>
 *
 * <p>サポートされるプレースホルダー:</p>
 * <ul>
 *   <li>{@code %rpg_level%} - プレイヤーレベル</li>
 *   <li>{@code %rpg_stat_STR%} - ステータス値 (STR/INT/SPI/VIT/DEX)</li>
 *   <li>{@code %rpg_class%} - 現在のクラスID</li>
 *   <li>{@code %rpg_class_name%} - クラス表示名</li>
 *   <li>{@code %rpg_class_rank%} - クラスランク</li>
 *   <li>{@code %rpg_skill_points%} - スキルポイント</li>
 *   <li>{@code %rpg_available_points%} - 利用可能ステータスポイント</li>
 *   <li>{@code %rpg_skill_level_<skill>%} - スキルレベル</li>
 *   <li>{@code %rpg_max_hp%} - 最大HP</li>
 *   <li>{@code %rpg_max_mana%} - 最大MP</li>
 *   <li>{@code %rpg_mana%} - 現在MP</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class RPGPlaceholderExpansion extends PlaceholderExpansion {

    private final RPGPlugin plugin;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public RPGPlaceholderExpansion(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "rpg";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return plugin.getPluginMeta().getAuthors().stream().findFirst().orElse("RPGPlugin Team");
    }

    @Override
    @NotNull
    public String getVersion() {
        String version = plugin.getPluginMeta().getVersion();
        return version != null ? version : "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (params == null) {
            return null;
        }

        UUID uuid = player.getUniqueId();
        RPGPluginAPI api = plugin.getAPI();

        // レベル関連
        if (params.equalsIgnoreCase("level")) {
            return String.valueOf(api.getLevel(player));
        }

        // マルチステータス（全て表示） - "stats"は "stat"のチェックより先に行う
        if (params.equals("stats")) {
            RPGPlayer rpgPlayer = plugin.getPlayerManager().getRPGPlayer(uuid);
            if (rpgPlayer != null) {
                return String.format("STR:%d INT:%d SPI:%d VIT:%d DEX:%d",
                        api.getStat(player, Stat.STRENGTH),
                        api.getStat(player, Stat.INTELLIGENCE),
                        api.getStat(player, Stat.SPIRIT),
                        api.getStat(player, Stat.VITALITY),
                        api.getStat(player, Stat.DEXTERITY)
                );
            }
            return "";
        }

        // ステータス関連
        if (params.startsWith("stat")) {
            String statStr = params.startsWith("stat_") ? params.substring(5).toUpperCase() : "";
            Stat stat = parseStat(statStr);
            if (stat != null) {
                return String.valueOf(api.getStat(player, stat));
            }
            return "0";
        }

        // クラス関連
        if (params.equals("class")) {
            String classId = api.getClassId(player);
            return classId != null ? classId : "None";
        }

        if (params.equals("class_name")) {
            RPGPlayer rpgPlayer = plugin.getPlayerManager().getRPGPlayer(uuid);
            if (rpgPlayer != null) {
                Optional<RPGClass> rpgClass = plugin.getClassManager().getPlayerClass(player);
                if (rpgClass.isPresent()) {
                    return rpgClass.get().getDisplayName();
                }
            }
            return "None";
        }

        if (params.equals("class_rank")) {
            RPGPlayer rpgPlayer = plugin.getPlayerManager().getRPGPlayer(uuid);
            if (rpgPlayer != null) {
                Optional<RPGClass> rpgClass = plugin.getClassManager().getPlayerClass(player);
                if (rpgClass.isPresent()) {
                    return String.valueOf(rpgClass.get().getRank());
                }
            }
            return "1";
        }

        // スキルポイント（利用可能ステータスポイントを使用）
        if (params.equals("skill_points")) {
            RPGPlayer rpgPlayer = plugin.getPlayerManager().getRPGPlayer(uuid);
            if (rpgPlayer != null) {
                return String.valueOf(rpgPlayer.getAvailablePoints());
            }
            return "0";
        }

        // 利用可能ステータスポイント
        if (params.equals("available_points")) {
            RPGPlayer rpgPlayer = plugin.getPlayerManager().getRPGPlayer(uuid);
            if (rpgPlayer != null) {
                return String.valueOf(rpgPlayer.getAvailablePoints());
            }
            return "0";
        }

        // スキルレベル
        if (params.startsWith("skill_level_")) {
            String skillId = params.substring(12);
            if (api.hasSkill(player, skillId)) {
                return String.valueOf(api.getSkillLevel(player, skillId));
            }
            return "0";
        }

        // HP/MP関連
        if (params.equals("max_hp") || params.equals("max_health")) {
            RPGPlayer rpgPlayer = plugin.getPlayerManager().getRPGPlayer(uuid);
            if (rpgPlayer != null && rpgPlayer.getPlayerData().getMaxHealth() > 0) {
                return String.valueOf(rpgPlayer.getPlayerData().getMaxHealth());
            }
            return "20";
        }

        if (params.equals("max_mana")) {
            RPGPlayer rpgPlayer = plugin.getPlayerManager().getRPGPlayer(uuid);
            if (rpgPlayer != null && rpgPlayer.getPlayerData().getMaxMana() > 0) {
                return String.valueOf(rpgPlayer.getPlayerData().getMaxMana());
            }
            return "100";
        }

        if (params.equals("mana")) {
            RPGPlayer rpgPlayer = plugin.getPlayerManager().getRPGPlayer(uuid);
            if (rpgPlayer != null) {
                return String.valueOf(rpgPlayer.getPlayerData().getCurrentMana());
            }
            return "100";
        }

        // 不明なリクエスト
        return null;
    }

    /**
     * Stat文字列を解析します
     *
     * @param statStr ステータス文字列
     * @return Stat、解析できない場合はnull
     */
    private Stat parseStat(String statStr) {
        if (statStr == null) {
            return null;
        }

        String upper = statStr.toUpperCase();

        switch (upper) {
            case "STR":
            case "STRENGTH":
                return Stat.STRENGTH;
            case "INT":
            case "INTELLIGENCE":
                return Stat.INTELLIGENCE;
            case "SPI":
            case "SPIRIT":
                return Stat.SPIRIT;
            case "VIT":
            case "VITALITY":
                return Stat.VITALITY;
            case "DEX":
            case "DEXTERITY":
                return Stat.DEXTERITY;
            default:
                return null;
        }
    }
}
