package com.example.rpgplugin.api.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.RPGPluginAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * プレイヤーのRPGステータスが指定値以上か判定するSKript条件
 *
 * <p>構文:</p>
 * <pre>
 * %player%'s rpg stat[e] %string% is [at least] %number%
 * rpg stat[e] %string% of %player% is [at least] %number%
 * </pre>
 *
 * <p>使用例:</p>
 * <pre>
 * if player's rpg stat "STR" is at least 50:
 *     send "STRが50以上です！"
 *
 * if rpg stat "VITALITY" of player is at least 100:
 *     send "体力が高いです！"
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class CondRPGStatAbove extends Condition {

    static {
        Skript.registerCondition(CondRPGStatAbove.class,
                "%player%'s rpg stat[e] %string% is [at least] %number%",
                "rpg stat[e] %string% of %player% is [at least] %number%"
        );
    }

    private Expression<Player> playerExpr;
    private Expression<String> statExpr;
    private Expression<Number> valueExpr;
    private boolean negate;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (matchedPattern == 0) {
            this.playerExpr = (Expression<Player>) exprs[0];
            this.statExpr = (Expression<String>) exprs[1];
            this.valueExpr = (Expression<Number>) exprs[2];
        } else {
            this.statExpr = (Expression<String>) exprs[0];
            this.playerExpr = (Expression<Player>) exprs[1];
            this.valueExpr = (Expression<Number>) exprs[2];
        }
        setNegated(parseResult.hasNegative);
        this.negate = parseResult.hasNegative;
        return true;
    }

    @Override
    public boolean check(Event e) {
        Player player = playerExpr.getSingle(e);
        String statStr = statExpr.getSingle(e);
        Number value = valueExpr.getSingle(e);

        if (player == null || statStr == null || value == null) {
            return negate;
        }

        com.example.rpgplugin.stats.Stat stat = parseStat(statStr);
        if (stat == null) {
            SkriptLogger.error("Unknown stat type: " + statStr);
            return negate;
        }

        try {
            RPGPlugin plugin = RPGPlugin.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                SkriptLogger.error("RPGPlugin is not enabled");
                return negate;
            }

            RPGPluginAPI api = plugin.getAPI();
            int statValue = api.getStat(player, stat);
            return (statValue >= value.intValue()) != negate;
        } catch (Exception ex) {
            SkriptLogger.error("Error checking RPG stat: " + ex.getMessage());
            return negate;
        }
    }

    /**
     * Stat文字列を解析します
     */
    private com.example.rpgplugin.stats.Stat parseStat(String statStr) {
        if (statStr == null) {
            return null;
        }

        String upper = statStr.toUpperCase();

        switch (upper) {
            case "STR":
                return com.example.rpgplugin.stats.Stat.STRENGTH;
            case "INT":
                return com.example.rpgplugin.stats.Stat.INTELLIGENCE;
            case "SPI":
                return com.example.rpgplugin.stats.Stat.SPIRIT;
            case "VIT":
                return com.example.rpgplugin.stats.Stat.VITALITY;
            case "DEX":
                return com.example.rpgplugin.stats.Stat.DEXTERITY;
            default:
                try {
                    return com.example.rpgplugin.stats.Stat.valueOf(upper);
                } catch (IllegalArgumentException e) {
                    return null;
                }
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return playerExpr.toString(e, debug) + "'s rpg stat " + statExpr.toString(e, debug) + " is at least " + valueExpr.toString(e, debug);
    }
}
