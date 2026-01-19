package com.example.rpgplugin.api.skript.conditions;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

/**
 * player can upgrade rpg class 条件
 *
 * <p>プレイヤーがクラスアップ可能かチェックします。</p>
 *
 * <p>構文:</p>
 * <pre>
 * %player% can upgrade [their] rpg class
 * %player% is able to upgrade [their] rpg class
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.1
 */
public class CondCanUpgradeRPGClass extends Condition {

    static {
        Skript.registerCondition(CondCanUpgradeRPGClass.class,
                "%player% can upgrade [their] rpg class",
                "%player% is able to upgrade [their] rpg class"
        );
    }

    private Expression<Player> player;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        player = (Expression<Player>) exprs[0];
        return true;
    }

    @Override
    public boolean check(@NotNull Event e) {
        Player p = player.getSingle(e);
        if (p == null) {
            return false;
        }

        RPGPlugin plugin = RPGPlugin.getInstance();
        if (plugin == null) {
            return false;
        }

        PlayerManager pm = plugin.getPlayerManager();
        RPGPlayer rpgPlayer = pm.getRPGPlayer(p.getUniqueId());

        if (rpgPlayer == null) {
            return false;
        }

        // クラスアップ可能判定（レベルが次のランクの要件を満たすか）
        // 現在の実装では、常にtrueを返す（将来的にクラスシステムと連携）
        return rpgPlayer.getClassId() != null && !rpgPlayer.getClassId().isEmpty();
    }

    @Override
    public String toString(@NotNull Event e, boolean debug) {
        return player.toString(e, debug) + " can upgrade rpg class";
    }
}
