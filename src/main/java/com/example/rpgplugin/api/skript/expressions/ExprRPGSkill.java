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
 * RPGプレイヤーのスキルレベルを取得するSKript式
 *
 * <p>構文:</p>
 * <pre>
 * [the] rpg skill level of %skill% [from] %player%
 * [the] rpg skill level of %player%'s %skill%
 * %player%'s rpg skill level for %skill%
 * </pre>
 *
 * <p>使用例:</p>
 * <pre>
 * set {_skillLevel} to rpg skill level of "fireball" from player
 * send "ファイアボールLV: %{_skillLevel}%" to player
 *
 * if rpg skill level of player's "power_strike" > 3:
 *     send "パワーストライクが強化されています！"
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExprRPGSkill extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprRPGSkill.class, Number.class,
                ExpressionType.COMBINED,
                "[the] rpg skill level of %string% [from] %player%",
                "[the] rpg skill level of %player%'s %string%",
                "%player%'s rpg skill level for %string%"
        );
    }

    private Expression<String> skillExpr;
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
        // パターンによって引数順が異なる
        if (matchedPattern == 0) {
            this.skillExpr = (Expression<String>) exprs[0];
            this.playerExpr = (Expression<Player>) exprs[1];
        } else {
            this.playerExpr = (Expression<Player>) exprs[0];
            this.skillExpr = (Expression<String>) exprs[1];
        }
        return true;
    }

    @Override
    @Nullable
    protected Number[] get(Event e) {
        String skillId = skillExpr.getSingle(e);
        Player player = playerExpr.getSingle(e);

        if (skillId == null || player == null) {
            return new Integer[0];
        }

        try {
            RPGPlugin plugin = RPGPlugin.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                SkriptLogger.error("RPGPlugin is not enabled");
                return new Integer[0];
            }

            RPGPluginAPI api = plugin.getAPI();
            int level = api.getSkillLevel(player, skillId);
            return new Number[]{level};
        } catch (Exception ex) {
            SkriptLogger.error("Error getting RPG skill level: " + ex.getMessage());
            return new Integer[0];
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "rpg skill level of " + skillExpr.toString(e, debug) + " from " + playerExpr.toString(e, debug);
    }
}
