package com.example.rpgplugin.api.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.RPGPluginAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * RPGプレイヤーの属性ポイント（ステータス配分ポイント）を取得・変更するSKript式
 *
 * <p>構文:</p>
 * <pre>
 * [the] rpg attr[ibute] point of %player%
 * [the] rpg attr[ibute] point of %player%'s
 * %player%'s rpg attr[ibute] point
 * </pre>
 *
 * <p>変更操作:</p>
 * <pre>
 * add 1 to rpg attr point of player
 * set rpg attr point of player to 5
 * remove 1 from rpg attr point of player
 * </pre>
 *
 * <p>使用例:</p>
 * <pre>
 * # 取得
 * set {_points} to rpg attr point of player
 * send "属性ポイント: %{_points}%" to player
 *
 * # 追加
 * add 1 to rpg attr point of player
 *
 * # 設定
 * set rpg attr point of player to 10
 *
 * # 削除
 * remove 1 from rpg attr point of player
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExprRPGAttrPoint extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprRPGAttrPoint.class, Number.class,
                ExpressionType.SIMPLE,
                "[the] rpg attr[ibute] point of %player%",
                "[the] rpg attr[ibute] point of %player%'s",
                "%player%'s rpg attr[ibute] point"
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
                SkriptLogger.error("RPGPlugin is not enabled");
                return new Integer[0];
            }

            RPGPluginAPI api = plugin.getAPI();
            int points = api.getAttrPoints(player);
            return new Number[]{points};
        } catch (Exception ex) {
            SkriptLogger.error("Error getting RPG attr point: " + ex.getMessage());
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
                SkriptLogger.error("RPGPlugin is not enabled");
                return;
            }

            RPGPluginAPI api = plugin.getAPI();
            int current = api.getAttrPoints(player);

            switch (mode) {
                case SET:
                    api.setAttrPoints(player, value);
                    break;
                case ADD:
                    api.setAttrPoints(player, current + value);
                    break;
                case REMOVE:
                    api.setAttrPoints(player, Math.max(0, current - value));
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            SkriptLogger.error("Error changing RPG attr point: " + ex.getMessage());
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "rpg attr point of " + playerExpr.toString(e, debug);
    }
}
