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
import com.example.rpgplugin.skill.Skill;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * スキル発動イベントのスキルオブジェクトを取得するSKript式
 *
 * <p>この式は主にskript-reflectと連携して使用します。</p>
 *
 * <p>構文:</p>
 * <pre>
 * [the] event-skill
 * [the] skill
 * </pre>
 *
 * <p>使用可能なイベント:</p>
 * <ul>
 *   <li>on rpg skill cast</li>
 * </ul>
 *
 * <p>使用例:</p>
 * <pre>
 * # skript-reflect と組み合わせた使用例
 * on rpg skill cast:
 *     set {_skill} to event-skill
 *     send "スキル名: %{_skill}.getName()%" to event-player
 *
 * # スキルオブジェクトのメソッド呼び出し
 * on rpg skill cast:
 *     set {_skill} to event-skill
 *     set {_name} to {_skill}.getName()
 *     set {_id} to {_skill}.getId()
 *     send "スキル: %{_name}% (ID: %{_id}%)" to event-player
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExprEventSkill extends SimpleExpression<Skill> {

	static {
		Skript.registerExpression(ExprEventSkill.class, Skill.class,
				ExpressionType.SIMPLE,
				"[the] event-skill",
				"[the] skill"
		);
	}

	@Override
	public Class<? extends Skill> getReturnType() {
		return Skill.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(EvtRPGSkillCast.RPGSkillCastEvent.class)) {
			SkriptLogger.error("The event-skill expression can only be used in a rpg skill cast event.");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Skill[] get(Event e) {
		if (!(e instanceof EvtRPGSkillCast.RPGSkillCastEvent)) {
			return new Skill[0];
		}

		EvtRPGSkillCast.RPGSkillCastEvent event = (EvtRPGSkillCast.RPGSkillCastEvent) e;
		Skill skill = event.getSkill();
		if (skill == null) {
			return new Skill[0];
		}
		return new Skill[]{skill};
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "event-skill";
	}
}
