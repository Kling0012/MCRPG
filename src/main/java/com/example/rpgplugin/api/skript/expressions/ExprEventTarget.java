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
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * スキル発動イベントのターゲットを取得するSKript式
 *
 * <p>構文:</p>
 * <pre>
 * [the] event-target
 * [the] skill[-]target
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
 *     if event-target is set:
 *         send "ターゲット: %event-target%" to event-player
 *         send "ターゲットのHP: %{event-target}'s health%" to event-player
 *
 * on rpg skill cast:
 *     if event-target is a player:
 *         send "プレイヤーに対してスキル発動！" to event-player
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExprEventTarget extends SimpleExpression<Entity> {

	static {
		Skript.registerExpression(ExprEventTarget.class, Entity.class,
				ExpressionType.SIMPLE,
				"[the] event-target",
				"[the] skill[-]target"
		);
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(EvtRPGSkillCast.RPGSkillCastEvent.class)) {
			SkriptLogger.error("The event-target expression can only be used in a rpg skill cast event.");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Entity[] get(Event e) {
		if (!(e instanceof EvtRPGSkillCast.RPGSkillCastEvent)) {
			return new Entity[0];
		}

		EvtRPGSkillCast.RPGSkillCastEvent event = (EvtRPGSkillCast.RPGSkillCastEvent) e;
		Entity target = event.getTarget();
		if (target == null) {
			return new Entity[0];
		}
		return new Entity[]{target};
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "event-target";
	}
}
