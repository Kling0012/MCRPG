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
 * RPGプレイヤーのレベルを取得するSKript式
 *
 * <p>構文:</p>
 * <pre>
 * [the] rpg level of %player%
 * [the] rpg level of %player%'s
 * %player%'s rpg level
 * </pre>
 *
 * <p>使用例:</p>
 * <pre>
 * set {_level} to rpg level of player
 * send "あなたのLV: %{_level}%" to player
 *
 * if rpg level of player > 50:
 *     send "レベル50以上です！"
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExprRPGLevel extends SimpleExpression<Number> {

    static {
        // 構文登録
        Skript.registerExpression(ExprRPGLevel.class, Number.class,
                ExpressionType.SIMPLE,
                "[the] rpg level of %player%",
                "[the] rpg level of %player%'s",
                "%player%'s rpg level"
        );
    }

    private Expression<Player> playerExpr;

    @Override
    public Class<? extends Number> getReturnType() {
        return Integer.class;
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
    protected Number[] get(Event e) {
        Player player = playerExpr.getSingle(e);
        if (player == null) {
            return new Integer[0];
        }

        try {
            RPGPlugin plugin = RPGPlugin.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                SkriptLogger.error("RPGPlugin is not enabled");
                return new Integer[0];
            }

            RPGPluginAPI api = plugin.getAPI();
            int level = api.getLevel(player);
            return new Number[]{level};
        } catch (Exception ex) {
            SkriptLogger.error("Error getting RPG level: " + ex.getMessage());
            return new Integer[0];
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "rpg level of " + playerExpr.toString(e, debug);
    }
}
