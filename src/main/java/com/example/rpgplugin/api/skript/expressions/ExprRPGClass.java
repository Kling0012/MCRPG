package com.example.rpgplugin.api.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.RPGPluginAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * RPGプレイヤーのクラスを取得するSKript式
 *
 * <p>構文:</p>
 * <pre>
 * [the] rpg class of %player%
 * [the] rpg class of %player%'s
 * %player%'s rpg class
 * </pre>
 *
 * <p>使用例:</p>
 * <pre>
 * set {_class} to rpg class of player
 * send "クラス: %{_class}%" to player
 *
 * if rpg class of player is "warrior":
 *     send "あなたは戦士です！"
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExprRPGClass extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprRPGClass.class, String.class,
                ExpressionType.SIMPLE,
                "[the] rpg class of %player%",
                "[the] rpg class of %player%'s",
                "%player%'s rpg class"
        );
    }

    private Expression<Player> playerExpr;

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.playerExpr = (Expression<Player>) exprs[0];
        return true;
    }

    @Override
    @Nullable
    protected String[] get(Event e) {
        Player player = playerExpr.getSingle(e);
        if (player == null) {
            return new String[0];
        }

        try {
            RPGPlugin plugin = RPGPlugin.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                SkriptLogger.error("RPGPlugin is not enabled");
                return new String[0];
            }

            RPGPluginAPI api = plugin.getAPI();
            String classId = api.getClassId(player);
            if (classId == null) {
                return new String[]{"None"};
            }
            return new String[]{classId};
        } catch (Exception ex) {
            SkriptLogger.error("Error getting RPG class: " + ex.getMessage());
            return new String[0];
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "rpg class of " + playerExpr.toString(e, debug);
    }
}
