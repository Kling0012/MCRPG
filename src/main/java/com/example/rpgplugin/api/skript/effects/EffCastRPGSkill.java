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
 * cast rpg skill 効果
 *
 * <p>プレイヤーにスキルを発動させます。</p>
 *
 * <p>構文:</p>
 * <pre>
 * make %player% cast [the] rpg skill %string%
 * force %player% to cast [the] rpg skill %string%
 * cast [the] rpg skill %string% [for] %player%
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.1
 */
public class EffCastRPGSkill extends Effect {

    static {
        Skript.registerEffect(EffCastRPGSkill.class,
                "make %player% cast [the] rpg skill %string%",
                "force %player% to cast [the] rpg skill %string%",
                "cast [the] rpg skill %string% [for] %player%"
        );
    }

    private Expression<Player> player;
    private Expression<String> skillId;

    @Override
    protected void execute(@NotNull Event e) {
        Player p = player.getSingle(e);
        String skill = skillId.getSingle(e);

        if (p == null || skill == null) {
            return;
        }

        RPGPlugin plugin = RPGPlugin.getInstance();
        if (plugin == null) {
            return;
        }

        PlayerManager pm = plugin.getPlayerManager();
        RPGPlayer rpgPlayer = pm.getRPGPlayer(p.getUniqueId());

        if (rpgPlayer == null) {
            return;
        }

        // スキル発動
        rpgPlayer.executeSkill(p, skill);
    }

    @Override
    public String toString(@NotNull Event e, boolean debug) {
        return "make " + player.toString(e, debug) + " cast rpg skill " + skillId.toString(e, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (matchedPattern == 2) {
            skillId = (Expression<String>) exprs[0];
            player = (Expression<Player>) exprs[1];
        } else {
            player = (Expression<Player>) exprs[0];
            skillId = (Expression<String>) exprs[1];
        }
        return true;
    }
}
