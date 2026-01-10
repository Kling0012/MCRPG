package com.example.rpgplugin.api.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.RPGPluginAPI;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * RPGプレイヤーのステータスを取得するSKript式
 *
 * <p>構文:</p>
 * <pre>
 * [the] rpg stat %string% of %player%
 * [the] rpg stat %string% of %player%'s
 * %player%'s rpg stat %string%
 * </pre>
 *
 * <p>サポートされるステータス:</p>
 * <ul>
 *   <li>STR, STRENGTH - 攻撃力</li>
 *   <li>INT, INTELLIGENCE - 知力</li>
 *   <li>SPI, SPIRIT - 精神力</li>
 *   <li>VIT, VITALITY - 体力</li>
 *   <li>DEX, DEXTERITY - 器用さ</li>
 * </ul>
 *
 * <p>使用例:</p>
 * <pre>
 * set {_str} to rpg stat "STR" of player
 * send "STR: %{_str}%" to player
 *
 * set {_vit} to rpg stat "VITALITY" of player
 * if {_vit} > 50:
 *     send "体力が高いです！"
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExprRPGStat extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprRPGStat.class, Number.class,
                ExpressionType.COMBINED,
                "[the] rpg stat[e] %string% of %player%",
                "[the] rpg stat[e] %string% of %player%'s",
                "%player%'s rpg stat[e] %string%"
        );
    }

    private Expression<String> statExpr;
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
        this.statExpr = (Expression<String>) exprs[0];
        this.playerExpr = (Expression<Player>) exprs[1];
        return true;
    }

    @Override
    @Nullable
    protected Number[] get(Event e) {
        String statStr = statExpr.getSingle(e);
        Player player = playerExpr.getSingle(e);

        if (statStr == null || player == null) {
            return new Integer[0];
        }

        Stat stat = parseStat(statStr);
        if (stat == null) {
            SkriptLogger.error("Unknown stat type: " + statStr + ". Use STR, INT, SPI, VIT, or DEX.");
            return new Integer[0];
        }

        try {
            RPGPlugin plugin = RPGPlugin.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                SkriptLogger.error("RPGPlugin is not enabled");
                return new Integer[0];
            }

            RPGPluginAPI api = plugin.getAPI();
            int statValue = api.getStat(player, stat);
            return new Number[]{statValue};
        } catch (Exception ex) {
            SkriptLogger.error("Error getting RPG stat: " + ex.getMessage());
            return new Integer[0];
        }
    }

    /**
     * Stat文字列を解析します
     *
     * @param statStr ステータス文字列
     * @return Stat、解析できない場合はnull
     */
    private Stat parseStat(String statStr) {
        if (statStr == null) {
            return null;
        }

        String upper = statStr.toUpperCase();

        // 短縮名
        switch (upper) {
            case "STR":
                return Stat.STRENGTH;
            case "INT":
                return Stat.INTELLIGENCE;
            case "SPI":
                return Stat.SPIRIT;
            case "VIT":
                return Stat.VITALITY;
            case "DEX":
                return Stat.DEXTERITY;
            default:
                // 完全名で試行
                try {
                    return Stat.valueOf(upper);
                } catch (IllegalArgumentException e) {
                    return null;
                }
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "rpg stat " + statExpr.toString(e, debug) + " of " + playerExpr.toString(e, debug);
    }
}
