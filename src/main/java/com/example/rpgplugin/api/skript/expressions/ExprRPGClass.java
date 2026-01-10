package com.example.rpgplugin.api.skript.expressions;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

/**
 * rpg class of player 式
 *
 * <p>プレイヤーのクラスIDを取得します。</p>
 *
 * <p>構文:</p>
 * <pre>
 * [the] rpg class of %player%
 * [the] rpg class of %player%'s
 * %player%'s rpg class
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.1
 */
public class ExprRPGClass extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprRPGClass.class, String.class, ExpressionType.COMBINED,
                "[the] rpg class of %player%",
                "[the] rpg class of %player%'s",
                "%player%'s rpg class"
        );
    }

    private Expression<Player> player;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        player = (Expression<Player>) exprs[0];
        return true;
    }

    @Override
    protected String[] get(Event e) {
        Player p = player.getSingle(e);
        if (p == null) {
            return new String[0];
        }

        RPGPlugin plugin = RPGPlugin.getInstance();
        if (plugin == null) {
            return new String[0];
        }

        PlayerManager pm = plugin.getPlayerManager();
        RPGPlayer rpgPlayer = pm.getRPGPlayer(p.getUniqueId());

        if (rpgPlayer == null) {
            return new String[0];
        }

        String classId = rpgPlayer.getClassId();
        if (classId == null || classId.isEmpty()) {
            return new String[0];
        }

        return new String[]{classId};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "rpg class of " + player.toString(e, debug);
    }
}
