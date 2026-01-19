package com.example.rpgplugin.api.skript.conditions;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

/**
 * player's rpg stat is at least 条件
 *
 * <p>プレイヤーのステータスが指定値以上かチェックします。</p>
 *
 * <p>構文:</p>
 * <pre>
 * %player%'s rpg stat[e] %string% is [at least] %number%
 * rpg stat[e] %string% of %player% is [at least] %number%
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.1
 */
public class CondRPGStatAbove extends Condition {

    static {
        Skript.registerCondition(CondRPGStatAbove.class,
                "%player%'s rpg stat[e] %string% is [at least] %number%",
                "rpg stat[e] %string% of %player% is [at least] %number%"
        );
    }

    private Expression<Player> player;
    private Expression<String> statName;
    private Expression<Number> value;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (matchedPattern == 0) {
            player = (Expression<Player>) exprs[0];
            statName = (Expression<String>) exprs[1];
            value = (Expression<Number>) exprs[2];
        } else {
            statName = (Expression<String>) exprs[0];
            player = (Expression<Player>) exprs[1];
            value = (Expression<Number>) exprs[2];
        }
        return true;
    }

    @Override
    public boolean check(@NotNull Event e) {
        Player p = player.getSingle(e);
        String name = statName.getSingle(e);
        Number val = value.getSingle(e);

        if (p == null || name == null || val == null) {
            return false;
        }

        Stat stat = parseStat(name);
        if (stat == null) {
            return false;
        }

        RPGPlugin plugin = RPGPlugin.getInstance();
        if (plugin == null) {
            return false;
        }

        PlayerManager pm = plugin.getPlayerManager();
        RPGPlayer rpgPlayer = pm.getRPGPlayer(p.getUniqueId());

        if (rpgPlayer == null) {
            return false;
        }

        int statValue = rpgPlayer.getStatManager().getFinalStat(stat);
        return statValue >= val.intValue();
    }

    /**
     * ステータス名をStat列挙型に変換
     */
    private Stat parseStat(String name) {
        return switch (name.toUpperCase()) {
            case "STR", "STRENGTH" -> Stat.STRENGTH;
            case "INT", "INTELLIGENCE" -> Stat.INTELLIGENCE;
            case "SPI", "SPIRIT" -> Stat.SPIRIT;
            case "VIT", "VITALITY" -> Stat.VITALITY;
            case "DEX", "DEXTERITY" -> Stat.DEXTERITY;
            default -> null;
        };
    }

    @Override
    public String toString(@NotNull Event e, boolean debug) {
        return player.toString(e, debug) + "'s rpg stat " + statName.toString(e, debug) + " is at least " + value.toString(e, debug);
    }
}
