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
 * プレイヤーがRPGスキルを習得しているか判定するSKript条件
 *
 * <p>構文:</p>
 * <pre>
 * %player% has [the] rpg skill %skill%
 * %player% has [the] rpg skill [named] %skill%
 * %player%'s rpg skill[s] (contain|includes) %skill%
 * </pre>
 *
 * <p>使用例:</p>
 * <pre>
 * if player has rpg skill "fireball":
 *     send "ファイアボールを習得済みです！"
 *
 * if player has rpg skill named "power_strike":
 *     execute player command "cast power_strike"
 *
 * if player's rpg skills includes "heal":
 *     send "ヒールが使用可能です"
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class CondHasRPGSkill extends Condition {

    static {
        Skript.registerCondition(CondHasRPGSkill.class,
                "%player% has [the] rpg skill [named] %string%",
                "%player%'s rpg skill[s] (contain|includes) %string%"
        );
    }

    private Expression<Player> playerExpr;
    private Expression<String> skillExpr;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.playerExpr = (Expression<Player>) exprs[0];
        this.skillExpr = (Expression<String>) exprs[1];
        return true;
    }

    @Override
    public boolean check(Event e) {
        Player player = playerExpr.getSingle(e);
        String skillId = skillExpr.getSingle(e);

        if (player == null || skillId == null) {
            return false;
        }

        try {
            RPGPlugin plugin = RPGPlugin.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                SkriptLogger.error("RPGPlugin is not enabled");
                return false;
            }

            RPGPluginAPI api = plugin.getAPI();
            return api.hasSkill(player, skillId);
        } catch (Exception ex) {
            SkriptLogger.error("Error checking RPG skill: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return playerExpr.toString(e, debug) + " has rpg skill " + skillExpr.toString(e, debug);
    }
}
