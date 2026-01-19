package com.example.rpgplugin.api.skript.conditions;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

/**
 * player's rpg class is 条件
 *
 * <p>プレイヤーのクラスが指定クラスかチェックします。</p>
 *
 * <p>構文:</p>
 * <pre>
 * %player%'s rpg class is %string%
 * %player% is [in] rpg class %string%
 * rpg class of %player% is %string%
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.1
 */
public class CondIsRPGClass extends Condition {

    static {
        Skript.registerCondition(CondIsRPGClass.class,
                "%player%'s rpg class is %string%",
                "%player% is [in] rpg class %string%",
                "rpg class of %player% is %string%"
        );
    }

    private Expression<Player> player;
    private Expression<String> classId;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        player = (Expression<Player>) exprs[0];
        classId = (Expression<String>) exprs[1];
        return true;
    }

    @Override
    public boolean check(@NotNull Event e) {
        Player p = player.getSingle(e);
        String targetClass = classId.getSingle(e);

        if (p == null || targetClass == null) {
            return false;
        }

        RPGPlugin plugin = RPGPlugin.getInstance();
        if (plugin == null) {
            return false;
        }

        PlayerManager pm = plugin.getPlayerManager();
        RPGPlayer rpgPlayer = pm.getRPGPlayer(p.getUniqueId());

        if (rpgPlayer == null) {
            return false;
        }

        String currentClass = rpgPlayer.getClassId();
        return targetClass.equalsIgnoreCase(currentClass);
    }

    @Override
    public String toString(@NotNull Event e, boolean debug) {
        return player.toString(e, debug) + "'s rpg class is " + classId.toString(e, debug);
    }
}
