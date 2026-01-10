package com.example.rpgplugin.api.skript.expressions;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

/**
 * rpg skill level of player 式
 *
 * <p>プレイヤーのスキルレベルを取得します。</p>
 *
 * <p>構文:</p>
 * <pre>
 * [the] rpg skill level of %string% [from] %player%
 * [the] rpg skill level of %player%'s %string%
 * %player%'s rpg skill level for %string%
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.1
 */
public class ExprRPGSkillLevel extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprRPGSkillLevel.class, Number.class, ExpressionType.COMBINED,
                "[the] rpg skill level of %string% [from] %player%",
                "[the] rpg skill level of %player%'s %string%",
                "%player%'s rpg skill level for %string%"
        );
    }

    private Expression<String> skillId;
    private Expression<Player> player;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (matchedPattern == 0) {
            skillId = (Expression<String>) exprs[0];
            player = (Expression<Player>) exprs[1];
        } else {
            player = (Expression<Player>) exprs[0];
            skillId = (Expression<String>) exprs[1];
        }
        return true;
    }

    @Override
    protected Number[] get(Event e) {
        String skill = skillId.getSingle(e);
        Player p = player.getSingle(e);

        if (skill == null || p == null) {
            return new Number[0];
        }

        RPGPlugin plugin = RPGPlugin.getInstance();
        if (plugin == null) {
            return new Number[0];
        }

        PlayerManager pm = plugin.getPlayerManager();
        RPGPlayer rpgPlayer = pm.getRPGPlayer(p.getUniqueId());

        if (rpgPlayer == null) {
            return new Number[0];
        }

        if (!rpgPlayer.hasSkill(skill)) {
            return new Number[]{0};
        }

        return new Number[]{rpgPlayer.getSkillLevel(skill)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "rpg skill level of " + skillId.toString(e, debug) + " from " + player.toString(e, debug);
    }
}
