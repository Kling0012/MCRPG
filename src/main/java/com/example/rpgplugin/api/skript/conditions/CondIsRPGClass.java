package com.example.rpgplugin.api.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import com.example.rpgplugin.RPGPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * プレイヤーが特定のRPGクラスであるか判定するSKript条件
 *
 * <p>構文:</p>
 * <pre>
 * %player%'s rpg class is %string%
 * %player% is rpg class %string%
 * rpg class of %player% is %string%
 * </pre>
 *
 * <p>使用例:</p>
 * <pre>
 * if player's rpg class is "warrior":
 *     send "あなたは戦士です！"
 *
 * if player is rpg class "mage":
 *     give player diamond sword
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class CondIsRPGClass extends Condition {

    static {
        Skript.registerCondition(CondIsRPGClass.class,
                "%player%'s rpg class is %string%",
                "%player% is [in] rpg class %string%",
                "rpg class of %player% is %string%"
        );
    }

    private Expression<Player> playerExpr;
    private Expression<String> classExpr;
    private boolean negate;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (matchedPattern == 0) {
            this.playerExpr = (Expression<Player>) exprs[0];
            this.classExpr = (Expression<String>) exprs[1];
        } else if (matchedPattern == 1) {
            this.playerExpr = (Expression<Player>) exprs[0];
            this.classExpr = (Expression<String>) exprs[1];
        } else {
            this.playerExpr = (Expression<Player>) exprs[0];
            this.classExpr = (Expression<String>) exprs[1];
        }
        this.negate = parseResult.hasNegative;
        return true;
    }

    @Override
    public boolean check(Event e) {
        Player player = playerExpr.getSingle(e);
        String classId = classExpr.getSingle(e);

        if (player == null || classId == null) {
            return negate;
        }

        try {
            RPGPlugin plugin = RPGPlugin.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                SkriptLogger.error("RPGPlugin is not enabled");
                return negate;
            }

            String playerClass = plugin.getAPI().getClassId(player);
            boolean matches = classId.equalsIgnoreCase(playerClass);
            return matches != negate;
        } catch (Exception ex) {
            SkriptLogger.error("Error checking RPG class: " + ex.getMessage());
            return negate;
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return playerExpr.toString(e, debug) + "'s rpg class is " + classExpr.toString(e, debug);
    }
}
