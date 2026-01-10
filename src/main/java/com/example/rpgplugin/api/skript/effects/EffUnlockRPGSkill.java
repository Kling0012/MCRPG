package com.example.rpgplugin.api.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
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
 * プレイヤーにRPGスキルを習得させるSKript効果
 *
 * <p>構文:</p>
 * <pre>
 * unlock [the] rpg skill %skill% [for] %player%
 * teach [the] rpg skill %skill% to %player%
 * make %player% (learn|unlock) [the] rpg skill %skill%
 * </pre>
 *
 * <p>使用例:</p>
 * <pre>
 * # クエスト報酬としてスキルを習得
 * unlock rpg skill "fireball" for player
 *
 * # 特定条件下でスキル解放
 * on join:
 *     if player has permission "vip":
 *         unlock rpg skill "vip_heal" for player
 *
 * # コマンドでスキル習得
 * command /teachskill <player>:
 *     trigger:
 *         unlock rpg skill "power_strike" for arg-1
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class EffUnlockRPGSkill extends Effect {

    static {
        Skript.registerEffect(EffUnlockRPGSkill.class,
                "unlock [the] rpg skill %string% [for] %player%",
                "teach [the] rpg skill %string% to %player%",
                "make %player% (learn|unlock) [the] rpg skill %string%"
        );
    }

    private Expression<String> skillExpr;
    private Expression<Player> playerExpr;

    @Override
    protected void execute(Event e) {
        String skillId = skillExpr.getSingle(e);
        Player player = playerExpr.getSingle(e);

        if (skillId == null || player == null) {
            return;
        }

        try {
            RPGPlugin plugin = RPGPlugin.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                SkriptLogger.error("RPGPlugin is not enabled");
                return;
            }

            RPGPluginAPI api = plugin.getAPI();
            boolean success = api.unlockSkill(player, skillId);

            if (!success) {
                SkriptLogger.warning("Failed to unlock skill " + skillId + " for " + player.getName());
            }
        } catch (Exception ex) {
            SkriptLogger.error("Error unlocking RPG skill: " + ex.getMessage());
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "unlock rpg skill " + skillExpr.toString(e, debug) + " for " + playerExpr.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (matchedPattern == 0 || matchedPattern == 1) {
            this.skillExpr = (Expression<String>) exprs[0];
            this.playerExpr = (Expression<Player>) exprs[1];
        } else {
            this.playerExpr = (Expression<Player>) exprs[0];
            this.skillExpr = (Expression<String>) exprs[1];
        }
        return true;
    }
}
