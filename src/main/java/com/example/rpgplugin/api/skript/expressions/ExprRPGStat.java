package com.example.rpgplugin.api.skript.expressions;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

/**
 * rpg stat of player 式
 *
 * <p>プレイヤーのステータスを取得します。</p>
 *
 * <p>構文:</p>
 * <pre>
 * [the] rpg stat[e] %string% of %player%
 * [the] rpg stat[e] %string% of %player%'s
 * %player%'s rpg stat[e] %string%
 * </pre>
 *
 * <p>ステータス名:</p>
 * <ul>
 *   <li>STR, STRENGTH - 筋力</li>
 *   <li>INT, INTELLIGENCE - 知力</li>
 *   <li>SPI, SPIRIT - 精神</li>
 *   <li>VIT, VITALITY - 生命力</li>
 *   <li>DEX, DEXTERITY - 器用さ</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.1
 */
public class ExprRPGStat extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprRPGStat.class, Number.class, ExpressionType.COMBINED,
                "[the] rpg stat[e] %string% of %player%",
                "[the] rpg stat[e] %string% of %player%'s",
                "%player%'s rpg stat[e] %string%"
        );
    }

    private Expression<String> statName;
    private Expression<Player> player;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        statName = (Expression<String>) exprs[0];
        player = (Expression<Player>) exprs[1];
        return true;
    }

    @Override
    protected Number[] get(Event e) {
        String name = statName.getSingle(e);
        Player p = player.getSingle(e);

        if (name == null || p == null) {
            return new Number[0];
        }

        Stat stat = parseStat(name);
        if (stat == null) {
            return new Number[0];
        }

        RPGPlugin plugin = RPGPlugin.getInstance();
        if (plugin == null) {
            return new Number[0];
        }

        PlayerManager pm = plugin.getPlayerManager();
        RPGPlayer rpgPlayer = pm.getRPGPlayer(p.getUniqueId());

        if (rpgPlayer == null) {
            return new Number[0];
        }

        return new Number[]{rpgPlayer.getStatManager().getFinalStat(stat)};
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
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "rpg stat " + statName.toString(e, debug) + " of " + player.toString(e, debug);
    }
}
