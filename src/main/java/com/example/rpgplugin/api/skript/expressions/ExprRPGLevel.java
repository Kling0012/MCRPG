package com.example.rpgplugin.api.skript.expressions;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
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
 * rpg level of player 式
 *
 * <p>プレイヤーのRPGレベルを取得します。</p>
 *
 * <p>構文:</p>
 * <pre>
 * [the] rpg level of %player%
 * [the] rpg level of %player%'s
 * %player%'s rpg level
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.1
 */
public class ExprRPGLevel extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprRPGLevel.class, Number.class, ExpressionType.COMBINED,
                "[the] rpg level of %player%",
                "[the] rpg level of %player%'s",
                "%player%'s rpg level"
        );
    }

    private Expression<Player> player;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        player = (Expression<Player>) exprs[0];
        return true;
    }

    @Override
    protected Number[] get(Event e) {
        Player p = player.getSingle(e);
        if (p == null) {
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

        return new Number[]{rpgPlayer.getLevel()};
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
        return "rpg level of " + player.toString(e, debug);
    }
}
