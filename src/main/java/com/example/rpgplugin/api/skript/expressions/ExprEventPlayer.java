package com.example.rpgplugin.api.skript.expressions;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import com.example.rpgplugin.api.skript.events.EvtRPGSkillCast;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * スキル発動イベントのプレイヤーを取得するSKript式
 *
 * <p>構文:</p>
 * <pre>
 * [the] event-player
 * [the] event-caster
 * </pre>
 *
 * <p>使用可能なイベント:</p>
 * <ul>
 *   <li>on rpg skill cast</li>
 * </ul>
 *
 * <p>使用例:</p>
 * <pre>
 * on rpg skill cast:
 *     send "スキル発動者: %event-player%" to event-player
 *     set {_caster} to event-caster
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExprEventPlayer extends SimpleExpression<Player> {

	static {
		Skript.registerExpression(ExprEventPlayer.class, Player.class,
				ExpressionType.SIMPLE,
				"[the] event-player",
				"[the] event-caster"
		);
	}

	@Override
	public Class<? extends Player> getReturnType() {
		return Player.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(EvtRPGSkillCast.RPGSkillCastEvent.class)) {
			SkriptLogger.error("The event-player expression can only be used in a rpg skill cast event.");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Player[] get(Event e) {
		if (!(e instanceof EvtRPGSkillCast.RPGSkillCastEvent)) {
			return new Player[0];
		}

		EvtRPGSkillCast.RPGSkillCastEvent event = (EvtRPGSkillCast.RPGSkillCastEvent) e;
		Player player = event.getPlayer();
		if (player == null) {
			return new Player[0];
		}
		return new Player[]{player};
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "event-player";
	}
}
