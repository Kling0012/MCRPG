package com.example.rpgplugin.api.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.RPGPluginAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;


/**
 * RPGプレイヤーのスキルポイントを取得・変更するSKript式
 *
 * <p>構文:</p>
 * <pre>
 * [the] rpg skill point of %player%
 * [the] rpg skill point of %player%'s
 * %player%'s rpg skill point
 * </pre>
 *
 * <p>変更操作:</p>
 * <pre>
 * add 1 to rpg skill point of player
 * set rpg skill point of player to 5
 * remove 1 from rpg skill point of player
 * </pre>
 *
 * <p>使用例:</p>
 * <pre>
 * # 取得
 * set {_points} to rpg skill point of player
 * send "スキルポイント: %{_points}%" to player
 *
 * # 追加
 * add 1 to rpg skill point of player
 *
 * # 設定
 * set rpg skill point of player to 10
 *
 * # 削除
 * remove 1 from rpg skill point of player
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExprRPGSkillPoint extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprRPGSkillPoint.class, Number.class,
                ExpressionType.SIMPLE,
                "[the] rpg skill point of %player%",
                "[the] rpg skill point of %player%'s",
                "%player%'s rpg skill point"
        );
    }

    private Expression<Player> playerExpr;

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
        this.playerExpr = (Expression<Player>) exprs[0];
        return true;
    }

    @Override
    @Nullable
    protected Number[] get(Event e) {
        Player player = playerExpr.getSingle(e);
        if (player == null) {
            return new Integer[0];
        }

        try {
            RPGPlugin plugin = RPGPlugin.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                return new Integer[0];
            }

            RPGPluginAPI api = plugin.getAPI();
            int points = api.getSkillPoints(player);
            return new Number[]{points};
        } catch (Exception ex) {
            return new Integer[0];
        }
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(Changer.ChangeMode mode) {
        if (mode == Changer.ChangeMode.SET ||
            mode == Changer.ChangeMode.ADD ||
            mode == Changer.ChangeMode.REMOVE) {
            return new Class[]{Number.class};
        }
        return null;
    }

    @Override
    public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
        if (delta == null || delta.length == 0 || delta[0] == null) {
            return;
        }

        Player player = playerExpr.getSingle(e);
        if (player == null) {
            return;
        }

        int value = ((Number) delta[0]).intValue();

        try {
            RPGPlugin plugin = RPGPlugin.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                return;
            }

            RPGPluginAPI api = plugin.getAPI();
            int current = api.getSkillPoints(player);

            switch (mode) {
                case SET:
                    api.setSkillPoints(player, value);
                    break;
                case ADD:
                    api.setSkillPoints(player, current + value);
                    break;
                case REMOVE:
                    api.setSkillPoints(player, Math.max(0, current - value));
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            // Silently fail
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "rpg skill point of " + playerExpr.toString(e, debug);
    }
}
