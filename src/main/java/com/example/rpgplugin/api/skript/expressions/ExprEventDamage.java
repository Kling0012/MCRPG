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
 * スキル発動イベントのダメージ値を取得するSKript式
 *
 * <p>注意: 現在の実装では、RPGSkillCastEventはダメージ値を直接含んでいません。
 * ダメージ計算は別途行われる必要があります。</p>
 *
 * <p>構文:</p>
 * <pre>
 * [the] event-damage
 * [the] skill[-]damage
 * </pre>
 *
 * <p>使用可能なイベント:</p>
 * <ul>
 *   <li>on rpg skill cast (将来実装予定)</li>
 *   <li>on rpg skill damage (将来実装予定)</li>
 * </ul>
 *
 * <p>使用例:</p>
 * <pre>
 * on rpg skill damage:
 *     send "%event-damage% ダメージを与えた！" to event-player
 *
 * on rpg skill damage:
 *     if event-damage > 100:
 *         send "クリティカルヒット！" to all players
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExprEventDamage extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprEventDamage.class, Number.class,
				ExpressionType.SIMPLE,
				"[the] event-damage",
				"[the] skill[-]damage"
		);
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Double.class;
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
	protected Number[] get(Event e) {
		// TODO: RPGSkillCastEventにダメージ値を追加するか、別のダメージイベントを作成する
		// 現時点では0を返す
		return new Number[]{0};
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "event-damage";
	}
}
