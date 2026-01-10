package com.example.rpgplugin.api.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import com.example.rpgplugin.RPGPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * プレイヤーのステータスポイントを配分するSKript効果
 *
 * <p>構文:</p>
 * <pre>
 * add %number% [to] rpg stat[e] %string% of %player%
 * set rpg stat[e] %string% of %player% to %number%
 * remove %number% from rpg stat[e] %string% of %player%
 * </pre>
 *
 * <p>使用例:</p>
 * <pre>
 * # STRに5ポイント追加
 * add 5 to rpg stat "STR" of player
 *
 * # ステータス設定（管理用）
 * set rpg stat "VITALITY" of player to 100
 *
 * # ポイント配分
 * add 1 to rpg stat "DEX" of player
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class EffModifyRPGStat extends Effect {

    static {
        Skript.registerEffect(EffModifyRPGStat.class,
                "add %number% [to] rpg stat[e] %string% of %player%",
                "set rpg stat[e] %string% of %player% to %number%",
                "remove %number% from rpg stat[e] %string% of %player%"
        );
    }

    private enum Mode {
        ADD, SET, REMOVE
    }

    private Expression<Number> valueExpr;
    private Expression<String> statExpr;
    private Expression<Player> playerExpr;
    private Mode mode;

    @Override
    protected void execute(Event e) {
        Number value = valueExpr.getSingle(e);
        String statStr = statExpr.getSingle(e);
        Player player = playerExpr.getSingle(e);

        if (value == null || statStr == null || player == null) {
            return;
        }

        com.example.rpgplugin.stats.Stat stat = parseStat(statStr);
        if (stat == null) {
            SkriptLogger.error("Unknown stat type: " + statStr + ". Use STR, INT, SPI, VIT, or DEX.");
            return;
        }

        try {
            RPGPlugin plugin = RPGPlugin.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                SkriptLogger.error("RPGPlugin is not enabled");
                return;
            }

            com.example.rpgplugin.api.RPGPluginAPI api = plugin.getAPI();

            switch (mode) {
                case ADD:
                    int current = api.getStat(player, stat);
                    api.setStat(player, stat, current + value.intValue());
                    break;
                case SET:
                    api.setStat(player, stat, value.intValue());
                    break;
                case REMOVE:
                    int currentVal = api.getStat(player, stat);
                    api.setStat(player, stat, Math.max(0, currentVal - value.intValue()));
                    break;
            }
        } catch (Exception ex) {
            SkriptLogger.error("Error modifying RPG stat: " + ex.getMessage());
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return mode.name().toLowerCase() + " rpg stat " + statExpr.toString(e, debug) + " of " + playerExpr.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (matchedPattern == 0) {
            this.valueExpr = (Expression<Number>) exprs[0];
            this.statExpr = (Expression<String>) exprs[1];
            this.playerExpr = (Expression<Player>) exprs[2];
            this.mode = Mode.ADD;
        } else if (matchedPattern == 1) {
            this.statExpr = (Expression<String>) exprs[0];
            this.playerExpr = (Expression<Player>) exprs[1];
            this.valueExpr = (Expression<Number>) exprs[2];
            this.mode = Mode.SET;
        } else {
            this.valueExpr = (Expression<Number>) exprs[0];
            this.statExpr = (Expression<String>) exprs[1];
            this.playerExpr = (Expression<Player>) exprs[2];
            this.mode = Mode.REMOVE;
        }
        return true;
    }

    /**
     * Stat文字列を解析します
     */
    private com.example.rpgplugin.stats.Stat parseStat(String statStr) {
        if (statStr == null) {
            return null;
        }

        String upper = statStr.toUpperCase();

        switch (upper) {
            case "STR":
                return com.example.rpgplugin.stats.Stat.STRENGTH;
            case "INT":
                return com.example.rpgplugin.stats.Stat.INTELLIGENCE;
            case "SPI":
                return com.example.rpgplugin.stats.Stat.SPIRIT;
            case "VIT":
                return com.example.rpgplugin.stats.Stat.VITALITY;
            case "DEX":
                return com.example.rpgplugin.stats.Stat.DEXTERITY;
            default:
                try {
                    return com.example.rpgplugin.stats.Stat.valueOf(upper);
                } catch (IllegalArgumentException e) {
                    return null;
                }
        }
    }
}
