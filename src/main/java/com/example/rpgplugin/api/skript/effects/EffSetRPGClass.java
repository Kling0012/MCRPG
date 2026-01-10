package com.example.rpgplugin.api.skript.effects;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

/**
 * プレイヤーのRPGクラスを設定するSKript効果
 *
 * <p>構文:</p>
 * <pre>
 * set [the] rpg class of %player% to %string%
 * make %player% [a]n rpg class %string%
 * change %player%'s rpg class to %string%
 * set [the] rpg class of %player% to %string% at level %number%
 * make %player% [a]n rpg class %string% at level %number%
 * change %player%'s rpg class to %string% at level %number%
 * </pre>
 *
 * <p>使用例:</p>
 * <pre>
 * # クラス選択GUIで選択したクラスを設定（レベル0）
 * set rpg class of player to "warrior"
 *
 * # レベル指定してクラスを変更
 * set rpg class of player to "mage" at level 10
 *
 * # コマンドでクラス変更
 * command /setclass <class>:
 *     trigger:
 *         set rpg class of player to arg-1
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.1.0
 */
public class EffSetRPGClass extends Effect {

    static {
        Skript.registerEffect(EffSetRPGClass.class,
                "set [the] rpg class of %player% to %string%",
                "make %player% [a]n rpg class %string%",
                "change %player%'s rpg class to %string%",
                "set [the] rpg class of %player% to %string% at level %number%",
                "make %player% [a]n rpg class %string% at level %number%",
                "change %player%'s rpg class to %string% at level %number%"
        );
    }

    private Expression<Player> playerExpr;
    private Expression<String> classExpr;
    private Expression<Number> levelExpr;
    private boolean hasLevel;

    @Override
    protected void execute(Event e) {
        Player player = playerExpr.getSingle(e);
        String classId = classExpr.getSingle(e);

        if (player == null || classId == null) {
            return;
        }

        int level = 0;
        if (hasLevel && levelExpr != null) {
            Number levelNum = levelExpr.getSingle(e);
            if (levelNum != null) {
                level = levelNum.intValue();
            }
        }

        try {
            RPGPlugin plugin = RPGPlugin.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                SkriptLogger.error("RPGPlugin is not enabled");
                return;
            }

            boolean success;
            if (hasLevel) {
                success = plugin.getAPI().changeClass(player, classId, level);
            } else {
                success = plugin.getAPI().setClass(player, classId);
            }

            if (!success) {
                SkriptLogger.warning("Failed to set class " + classId + " for " + player.getName());
            }
        } catch (Exception ex) {
            SkriptLogger.error("Error setting RPG class: " + ex.getMessage());
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        if (hasLevel) {
            return "set rpg class of " + playerExpr.toString(e, debug) + " to " + classExpr.toString(e, debug) + " at level " + (levelExpr != null ? levelExpr.toString(e, debug) : "0");
        }
        return "set rpg class of " + playerExpr.toString(e, debug) + " to " + classExpr.toString(e, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.playerExpr = (Expression<Player>) exprs[0];
        this.classExpr = (Expression<String>) exprs[1];
        // パターン3,4,5 は level を含む
        this.hasLevel = matchedPattern >= 3;
        if (hasLevel) {
            this.levelExpr = (Expression<Number>) exprs[2];
        } else {
            this.levelExpr = null;
        }
        return true;
    }
}
