package com.example.rpgplugin.skill.component.mechanic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * メッセージメカニック
 * <p>ターゲットにメッセージを送信します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class MessageMechanic extends MechanicComponent {

    private static final String TEXT = "text";
    private static final String TO_CASTER = "to-caster";
    private static final String TO_TARGET = "to-target";

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    /**
     * コンストラクタ
     */
    public MessageMechanic() {
        super("message");
    }

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        String text = getString(TEXT, "");
        if (text.isEmpty()) {
            return false;
        }

        // MiniMessageでカラーコードを変換（& → < >）
        // 旧形式の & カラーコードをMiniMessage形式に変換
        text = convertLegacyToMiniMessage(text);

        // プレースホルダーを置換
        text = text.replace("{player}", caster.getName())
                .replace("{target}", target.getName());

        Component component = MINI_MESSAGE.deserialize(text);

        boolean toCaster = getBool(TO_CASTER, false);
        boolean toTarget = getBool(TO_TARGET, false);

        // デフォルトは両方に送信
        if (!toCaster && !toTarget) {
            toCaster = true;
            toTarget = true;
        }

        if (toCaster && caster instanceof Player) {
            ((Player) caster).sendMessage(component);
        }

        if (toTarget && target instanceof Player) {
            ((Player) target).sendMessage(component);
        }

        return true;
    }

    /**
     * 旧形式のカラーコードをMiniMessage形式に変換します
     *
     * @param text 変換前のテキスト
     * @return MiniMessage形式のテキスト
     */
    private String convertLegacyToMiniMessage(String text) {
        // 旧形式のカラーコード（&0-&f, &a-&f等）をMiniMessage形式に変換
        if (text.contains("&")) {
            text = text.replace("&0", "<black>")
                    .replace("&1", "<dark_blue>")
                    .replace("&2", "<dark_green>")
                    .replace("&3", "<dark_aqua>")
                    .replace("&4", "<dark_red>")
                    .replace("&5", "<dark_purple>")
                    .replace("&6", "<gold>")
                    .replace("&7", "<gray>")
                    .replace("&8", "<dark_gray>")
                    .replace("&9", "<blue>")
                    .replace("&a", "<green>")
                    .replace("&b", "<aqua>")
                    .replace("&c", "<red>")
                    .replace("&d", "<light_purple>")
                    .replace("&e", "<yellow>")
                    .replace("&f", "<white>")
                    .replace("&l", "<bold>")
                    .replace("&o", "<italic>")
                    .replace("&n", "<underline>")
                    .replace("&m", "<strikethrough>")
                    .replace("&k", "<obfuscated>")
                    .replace("&r", "<reset>");
        }
        return text;
    }
}
