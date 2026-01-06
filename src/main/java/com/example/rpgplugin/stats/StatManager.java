package com.example.rpgplugin.stats;

import com.example.rpgplugin.RPGPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * プレイヤーのステータスを管理するマネージャークラス
 */
public class StatManager {

    private final RPGPlugin plugin;
    private final Map<UUID, PlayerStats> playerStatsMap;

    public StatManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.playerStatsMap = new ConcurrentHashMap<>();
    }

    /**
     * プレイヤーのステータスデータを取得
     * 存在しない場合は新規作成
     */
    public PlayerStats getPlayerStats(Player player) {
        return playerStatsMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerStats());
    }

    /**
     * プレイヤーのステータスデータを取得
     */
    public PlayerStats getPlayerStats(UUID uuid) {
        return playerStatsMap.get(uuid);
    }

    /**
     * プレイヤーの合計ステータス値を取得
     */
    public int getStat(Player player, Stat stat) {
        PlayerStats stats = getPlayerStats(player);
        return stats != null ? stats.getTotalStat(stat) : 0;
    }

    /**
     * プレイヤーの残りポイントを取得
     */
    public int getAvailablePoints(Player player) {
        PlayerStats stats = getPlayerStats(player);
        return stats != null ? stats.getAvailablePoints() : 0;
    }

    /**
     * プレイヤーにポイントを追加（レベルアップ時など）
     */
    public void addPoints(Player player, int points) {
        PlayerStats stats = getPlayerStats(player);
        if (stats != null) {
            stats.addPoints(points);

            // 通知
            player.sendMessage(String.format("§a+%dポイント 獲得！ (残り: %d)",
                points, stats.getAvailablePoints()));
        }
    }

    /**
     * 自動ステータス配分を追加（レベルアップ時など）
     */
    public void addAutoPoints(Player player, Stat stat, int points) {
        PlayerStats stats = getPlayerStats(player);
        if (stats != null) {
            int currentAuto = stats.getAutoStat(stat);
            stats.setAutoStat(stat, currentAuto + points);

            // 通知
            player.sendMessage(String.format("§7%s: 自動+%d (合計: %d)",
                stat.getColoredShortName(), points, stats.getTotalStat(stat)));
        }
    }

    /**
     * プレイヤーのステータスデータをロード（データベースから）
     * TODO: Phase3で実装
     */
    public void loadStats(Player player) {
        // データベースからロード処理をここに実装
        // 現在は空のデータを作成
        getPlayerStats(player);
    }

    /**
     * プレイヤーのステータスデータを保存（データベースへ）
     * TODO: Phase3で実装
     */
    public void saveStats(Player player) {
        PlayerStats stats = getPlayerStats(player);
        if (stats != null) {
            // データベース保存処理をここに実装
            Bukkit.getLogger().info("Saving stats for " + player.getName());
        }
    }

    /**
     * プレイヤーのステータスデータをアンロード
     */
    public void unloadStats(Player player) {
        playerStatsMap.remove(player.getUniqueId());
    }

    /**
     * 全プレイヤーのステータスを保存
     */
    public void saveAllStats() {
        for (UUID uuid : playerStatsMap.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                saveStats(player);
            }
        }
    }
}
