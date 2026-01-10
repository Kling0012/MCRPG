package com.example.rpgplugin.api.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.RPGPluginAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * プレイヤーがRPGスキルを発動するSKript効果
 *
 * <p>構文:</p>
 * <pre>
 * make %player% cast [the] rpg skill %skill%
 * force %player% to cast [the] rpg skill %skill%
 * cast [the] rpg skill %skill% [for] %player%
 * </pre>
 *
 * <p>使用例:</p>
 * <pre>
 * # スキル発動
 * make player cast rpg skill "fireball"
 *
 * # 特定条件下でスキル発動
 * on rightclick with blaze rod:
 *     make player cast rpg skill "fireball"
 *
 * # ターゲット指定スキル発動
 * make player cast rpg skill "heal" at target
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class EffCastRPGSkill extends Effect {

    static {
        Skript.registerEffect(EffCastRPGSkill.class,
                "make %player% cast [the] rpg skill %string%",
                "force %player% to cast [the] rpg skill %string%",
                "cast [the] rpg skill %string% [for] %player%"
        );
    }

    private Expression<Player> playerExpr;
    private Expression<String> skillExpr;

    @Override
    protected void execute(Event e) {
        Player player = playerExpr.getSingle(e);
        String skillId = skillExpr.getSingle(e);

        if (player == null || skillId == null) {
            return;
        }

        try {
            RPGPlugin plugin = RPGPlugin.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                SkriptLogger.error("RPGPlugin is not enabled");
                return;
            }

            RPGPluginAPI api = plugin.getAPI();
            boolean success = api.castSkill(player, skillId);

            if (!success) {
                SkriptLogger.warning("Failed to cast skill " + skillId + " for " + player.getName());
            }
        } catch (Exception ex) {
            SkriptLogger.error("Error casting RPG skill: " + ex.getMessage());
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "cast rpg skill " + skillExpr.toString(e, debug) + " for " + playerExpr.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (matchedPattern == 0 || matchedPattern == 1) {
            this.playerExpr = (Expression<Player>) exprs[0];
            this.skillExpr = (Expression<String>) exprs[1];
        } else {
            this.skillExpr = (Expression<String>) exprs[0];
            this.playerExpr = (Expression<Player>) exprs[1];
        }
        return true;
    }
}
