package com.example.rpgplugin.api.skript.effects;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

/**
 * add/set/remove rpg stat 効果
 *
 * <p>プレイヤーのステータスを変更します。</p>
 *
 * <p>構文:</p>
 * <pre>
 * add %number% [to] rpg stat[e] %string% of %player%
 * set rpg stat[e] %string% of %player% to %number%
 * remove %number% from rpg stat[e] %string% of %player%
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.1
 */
public class EffModifyRPGStat extends Effect {

    static {
        Skript.registerEffect(EffModifyRPGStat.class,
                "add %number% [to] rpg stat[e] %string% of %player%",
                "set rpg stat[e] %string% of %player% to %number%",
                "remove %number% from rpg stat[e] %string% of %player%"
        );
    }

    private enum Mode { ADD, SET, REMOVE }

    private Expression<Number> value;
    private Expression<String> statName;
    private Expression<Player> player;
    private Mode mode;

    @Override
    protected void execute(@NotNull Event e) {
        Number val = value.getSingle(e);
        String name = statName.getSingle(e);
        Player p = player.getSingle(e);

        if (val == null || name == null || p == null) {
            return;
        }

        Stat stat = parseStat(name);
        if (stat == null) {
            return;
        }

        RPGPlugin plugin = RPGPlugin.getInstance();
        if (plugin == null) {
            return;
        }

        PlayerManager pm = plugin.getPlayerManager();
        RPGPlayer rpgPlayer = pm.getRPGPlayer(p.getUniqueId());

        if (rpgPlayer == null) {
            return;
        }

        int amount = val.intValue();
        int currentStat = rpgPlayer.getStatManager().getBaseStat(stat);

        switch (mode) {
            case ADD -> rpgPlayer.setBaseStat(stat, currentStat + amount);
            case SET -> rpgPlayer.setBaseStat(stat, amount);
            case REMOVE -> rpgPlayer.setBaseStat(stat, Math.max(0, currentStat - amount));
        }
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
        return mode.toString().toLowerCase() + " rpg stat " + statName.toString(e, debug) + " of " + player.toString(e, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        mode = switch (matchedPattern) {
            case 0 -> Mode.ADD;
            case 1 -> Mode.SET;
            case 2 -> Mode.REMOVE;
            default -> Mode.SET;
        };

        if (matchedPattern == 1) {
            statName = (Expression<String>) exprs[0];
            player = (Expression<Player>) exprs[1];
            value = (Expression<Number>) exprs[2];
        } else {
            value = (Expression<Number>) exprs[0];
            statName = (Expression<String>) exprs[1];
            player = (Expression<Player>) exprs[2];
        }
        return true;
    }
}
