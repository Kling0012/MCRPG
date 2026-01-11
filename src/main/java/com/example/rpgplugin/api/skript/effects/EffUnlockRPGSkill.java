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
 * unlock rpg skill 効果
 *
 * <p>プレイヤーにスキルを習得させます。</p>
 *
 * <p>構文:</p>
 * <pre>
 * unlock [the] rpg skill %string% [for] %player%
 * teach [the] rpg skill %string% to %player%
 * make %player% (learn|unlock) [the] rpg skill %string%
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.1
 */
public class EffUnlockRPGSkill extends Effect {

    static {
        Skript.registerEffect(EffUnlockRPGSkill.class,
                "unlock [the] rpg skill %string% [for] %player%",
                "teach [the] rpg skill %string% to %player%",
                "make %player% (learn|unlock) [the] rpg skill %string%"
        );
    }

    private Expression<String> skillId;
    private Expression<Player> player;

    @Override
    protected void execute(@NotNull Event e) {
        String skill = skillId.getSingle(e);
        Player p = player.getSingle(e);

        if (skill == null || p == null) {
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

        // スキル習得（レベル1）
        rpgPlayer.acquireSkill(skill, 1);
    }

    @Override
    public String toString(@NotNull Event e, boolean debug) {
        return "unlock rpg skill " + skillId.toString(e, debug) + " for " + player.toString(e, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (matchedPattern == 0 || matchedPattern == 1) {
            skillId = (Expression<String>) exprs[0];
            player = (Expression<Player>) exprs[1];
        } else {
            player = (Expression<Player>) exprs[0];
            skillId = (Expression<String>) exprs[1];
        }
        return true;
    }
}
