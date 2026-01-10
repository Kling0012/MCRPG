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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * スキル発動イベントのスキルレベルを取得するSKript式
 *
 * <p>構文:</p>
 * <pre>
 * [the] event-skill[-]level
 * [the] skill[-]level
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
 *     send "LV%event-skill-level% %event-skill-id% が発動！" to event-player
 *
 * on rpg skill cast:
 *     if event-skill-level >= 5:
 *         send "上級スキルが発動しました！" to event-player
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExprEventSkillLevel extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprEventSkillLevel.class, Number.class,
				ExpressionType.SIMPLE,
				"[the] event-skill[-]level",
				"[the] skill[-]level"
		);
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Integer.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(EvtRPGSkillCast.RPGSkillCastEvent.class)) {
			SkriptLogger.error("The event-skill-level expression can only be used in a rpg skill cast event.");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Number[] get(Event e) {
		if (!(e instanceof EvtRPGSkillCast.RPGSkillCastEvent)) {
			return new Number[0];
		}

		EvtRPGSkillCast.RPGSkillCastEvent event = (EvtRPGSkillCast.RPGSkillCastEvent) e;
		int skillLevel = event.getSkillLevel();
		return new Number[]{skillLevel};
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "event-skill-level";
	}
}
