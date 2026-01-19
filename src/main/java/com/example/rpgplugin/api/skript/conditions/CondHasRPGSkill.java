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
 * player has rpg skill 条件
 *
 * <p>プレイヤーがスキルを習得しているかチェックします。</p>
 *
 * <p>構文:</p>
 * <pre>
 * %player% has [the] rpg skill [named] %string%
 * %player%'s rpg skill[s] (contain|includes) %string%
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.1
 */
public class CondHasRPGSkill extends Condition {

    static {
        Skript.registerCondition(CondHasRPGSkill.class,
                "%player% has [the] rpg skill [named] %string%",
                "%player%'s rpg skill[s] (contain|includes) %string%"
        );
    }

    private Expression<Player> player;
    private Expression<String> skillId;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        player = (Expression<Player>) exprs[0];
        skillId = (Expression<String>) exprs[1];
        return true;
    }

    @Override
    public boolean check(@NotNull Event e) {
        Player p = player.getSingle(e);
        String skill = skillId.getSingle(e);

        if (p == null || skill == null) {
            return false;
        }

        RPGPlugin plugin = RPGPlugin.getInstance();
        if (plugin == null) {
            return false;
        }

        PlayerManager pm = plugin.getPlayerManager();
        RPGPlayer rpgPlayer = pm.getRPGPlayer(p.getUniqueId());

        return rpgPlayer != null && rpgPlayer.hasSkill(skill);
    }

    @Override
    public String toString(@NotNull Event e, boolean debug) {
        return player.toString(e, debug) + " has rpg skill " + skillId.toString(e, debug);
    }
}
