package com.example.rpgplugin.skill.component.mechanic;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * コマンドメカニック
 * <p>ターゲット（プレイヤー）に対してコマンドを実行します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class CommandMechanic extends MechanicComponent {

    private static final String COMMAND = "command";
    private static final String TYPE = "type"; // op, console, player

    /**
     * コンストラクタ
     */
    public CommandMechanic() {
        super("command");
    }

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        ComponentSettings settings = getSettings();
        if (settings == null) {
            return false;
        }

        String command = settings.getString(COMMAND, "");
        if (command.isEmpty()) {
            return false;
        }

        // プレイヤーのみ対象
        if (!(target instanceof Player)) {
            return false;
        }

        Player player = (Player) target;
        String type = settings.getString(TYPE, "console").toLowerCase();

        // プレースホルダー置換
        command = replacePlaceholders(command, caster, player, level);

        switch (type) {
            case "op":
                boolean wasOp = player.isOp();
                try {
                    player.setOp(true);
                    return Bukkit.dispatchCommand(player, command);
                } finally {
                    player.setOp(wasOp);
                }
            case "player":
                return Bukkit.dispatchCommand(player, command);
            case "console":
            default:
                return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    /**
     * プレースホルダーを置換します
     *
     * @param command コマンド文字列
     * @param caster  発動者
     * @param target  ターゲット
     * @param level   スキルレベル
     * @return 置換後のコマンド文字列
     */
    private String replacePlaceholders(String command, LivingEntity caster, LivingEntity target, int level) {
        return command
                .replace("{caster}", caster.getName())
                .replace("{target}", target.getName())
                .replace("{level}", String.valueOf(level))
                .replace("{x}", String.valueOf(target.getLocation().getBlockX()))
                .replace("{y}", String.valueOf(target.getLocation().getBlockY()))
                .replace("{z}", String.valueOf(target.getLocation().getBlockZ()))
                .replace("{world}", target.getWorld().getName());
    }
}
