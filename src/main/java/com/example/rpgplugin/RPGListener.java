package com.example.rpgplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class RPGListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            Player player = event.getPlayer();
            if (player != null) {
                player.sendMessage("§aようこそサーバーへ！§eRPGPlugin§aが有効です");
            }
        } catch (Exception e) {
            // イベント処理中の例外をキャッチし、サーバーにログ出力
            // プレイヤーの接続自体には影響しないようにする
            RPGPlugin.getInstance().getLogger().warning(
                "Error in onPlayerJoin: " + e.getMessage()
            );
        }
    }
}
