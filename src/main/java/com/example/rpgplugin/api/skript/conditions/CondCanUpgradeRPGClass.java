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
 * プレイヤーがRPGクラスアップ可能か判定するSKript条件
 *
 * <p>構文:</p>
 * <pre>
 * %player% can upgrade [their] rpg class
 * %player% can upgrade [the] rpg class
 * %player% is able to upgrade rpg class
 * </pre>
 *
 * <p>使用例:</p>
 * <pre>
 * if player can upgrade rpg class:
 *     send "クラスアップ可能です！"
 *     execute player command "rpg api upgrade_class"
 * else:
 *     send "まだクラスアップできません"
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class CondCanUpgradeRPGClass extends Condition {

    static {
        Skript.registerCondition(CondCanUpgradeRPGClass.class,
                "%player% can upgrade [their] rpg class",
                "%player% is able to upgrade [their] rpg class"
        );
    }

    private Expression<Player> playerExpr;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.playerExpr = (Expression<Player>) exprs[0];
        return true;
    }

    @Override
    public boolean check(Event e) {
        Player player = playerExpr.getSingle(e);

        if (player == null) {
            return false;
        }

        try {
            RPGPlugin plugin = RPGPlugin.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                SkriptLogger.error("RPGPlugin is not enabled");
                return false;
            }

            RPGPluginAPI api = plugin.getAPI();
            return api.canUpgradeClass(player);
        } catch (Exception ex) {
            SkriptLogger.error("Error checking class upgrade possibility: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return playerExpr.toString(e, debug) + " can upgrade rpg class";
    }
}
