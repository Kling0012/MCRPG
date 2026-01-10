package com.example.rpgplugin.api.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.example.rpgplugin.api.skript.events.EvtRPGSkillCast;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * スキル発動イベントのスキルIDを取得するSKript式
 *
 * <p>構文:</p>
 * <pre>
 * [the] event-skill-id
 * [the] skill[-]id
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
 *     send "発動スキル: %event-skill-id%" to event-player
 *     if event-skill-id is "fireball":
 *         send "ファイアボール！" to event-player
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExprEventSkillId extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprEventSkillId.class, String.class,
				ExpressionType.SIMPLE,
				"[the] event-skill-id",
				"[the] skill[-]id"
		);
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event e) {
		if (!(e instanceof EvtRPGSkillCast.RPGSkillCastEvent)) {
			return new String[0];
		}

		EvtRPGSkillCast.RPGSkillCastEvent event = (EvtRPGSkillCast.RPGSkillCastEvent) e;
		String skillId = event.getSkillId();
		if (skillId == null) {
			return new String[0];
		}
		return new String[]{skillId};
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "event-skill-id";
	}
}
