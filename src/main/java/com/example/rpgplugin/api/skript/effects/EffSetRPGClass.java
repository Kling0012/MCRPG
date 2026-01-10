package com.example.rpgplugin.api.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import com.example.rpgplugin.RPGPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * プレイヤーのRPGクラスを設定するSKript効果
 *
 * <p>構文:</p>
 * <pre>
 * set [the] rpg class of %player% to %string%
 * make %player% [a]n rpg class %string%
 * change %player%'s rpg class to %string%
 * </pre>
 *
 * <p>使用例:</p>
 * <pre>
 * # クラス選択GUIで選択したクラスを設定
 * set rpg class of player to "warrior"
 *
 * # コマンドでクラス変更
 * command /setclass <class>:
 *     trigger:
 *         set rpg class of player to arg-1
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class EffSetRPGClass extends Effect {

    static {
        Skript.registerEffect(EffSetRPGClass.class,
                "set [the] rpg class of %player% to %string%",
                "make %player% [a]n rpg class %string%",
                "change %player%'s rpg class to %string%"
        );
    }

    private Expression<Player> playerExpr;
    private Expression<String> classExpr;

    @Override
    protected void execute(Event e) {
        Player player = playerExpr.getSingle(e);
        String classId = classExpr.getSingle(e);

        if (player == null || classId == null) {
            return;
        }

        try {
            RPGPlugin plugin = RPGPlugin.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                SkriptLogger.error("RPGPlugin is not enabled");
                return;
            }

            boolean success = plugin.getAPI().setClass(player, classId);

            if (!success) {
                SkriptLogger.warning("Failed to set class " + classId + " for " + player.getName());
            }
        } catch (Exception ex) {
            SkriptLogger.error("Error setting RPG class: " + ex.getMessage());
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "set rpg class of " + playerExpr.toString(e, debug) + " to " + classExpr.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (matchedPattern == 0) {
            this.playerExpr = (Expression<Player>) exprs[0];
            this.classExpr = (Expression<String>) exprs[1];
        } else {
            this.playerExpr = (Expression<Player>) exprs[0];
            this.classExpr = (Expression<String>) exprs[1];
        }
        return true;
    }
}
