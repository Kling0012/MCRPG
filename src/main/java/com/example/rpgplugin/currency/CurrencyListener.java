package com.example.rpgplugin.currency;

import com.example.rpgplugin.RPGPlugin;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Optional;
import java.util.Random;

/**
 * 通貨システムリスナー
 * モブ討伐時のゴールドドロップを処理
 */
public class CurrencyListener implements Listener {

    private final RPGPlugin plugin;
    private final CurrencyManager currencyManager;
    private final Random random;

    // デフォルトドロップ設定
    private static final double DEFAULT_DROP_CHANCE = 0.3; // 30%
    private static final double DEFAULT_MIN_GOLD = 1.0;
    private static final double DEFAULT_MAX_GOLD = 10.0;

    // MythicMobsボーナス倍率
    private static final double MYTHICMOB_BONUS_MULTIPLIER = 2.0;

    public CurrencyListener(RPGPlugin plugin, CurrencyManager currencyManager) {
        this.plugin = plugin;
        this.currencyManager = currencyManager;
        this.random = new Random();
    }

    /**
     * エンティティ死亡イベントを処理
     * モブ討伐時にゴールドをドロップ
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        // Paper 1.20.6: getKiller()は削除されたため、ダメージイベントからプレイヤーを特定
        // TODO: ダメージ追跡システムを実装して、キラーを特定できるようにする
        // 現在は一時的に無効化
        return;

        /*
        Player killer = entity.getKiller();

        // プレイヤーに倒されたか確認
        if (killer == null) {
            return;
        }

        // プレイヤー自身の死亡は無視
        if (entity instanceof Player) {
            return;
        }

        // ゴールド計算
        double goldDrop = calculateGoldDrop(entity);

        if (goldDrop <= 0) {
            return;
        }

        // ゴールドを付与
        boolean success = currencyManager.depositGold(killer, goldDrop);

        if (success) {
            // メッセージを送信
            sendGoldEarnMessage(killer, entity, goldDrop);

            plugin.getLogger().fine("[Currency] " + killer.getName() + " earned " + goldDrop + "G from " + entity.getType().name());
        }
        */
    }

    /**
     * ゴールドドロップ量を計算
     *
     * @param entity 倒されたエンティティ
     * @return ゴールドドロップ量
     */
    private double calculateGoldDrop(Entity entity) {
        // ドロップチャック判定
        if (random.nextDouble() > DEFAULT_DROP_CHANCE) {
            return 0;
        }

        // MythicMobsか確認
        boolean isMythicMob = isMythicMob(entity);

        // 基本ドロップ量計算
        double baseDrop = DEFAULT_MIN_GOLD + (DEFAULT_MAX_GOLD - DEFAULT_MIN_GOLD) * random.nextDouble();

        // エンティティタイプによるボーナス
        double typeBonus = getTypeBonus(entity.getType());

        // MythicMobsボーナス
        double finalDrop = baseDrop * typeBonus;
        if (isMythicMob) {
            finalDrop *= MYTHICMOB_BONUS_MULTIPLIER;
        }

        return finalDrop;
    }

    /**
     * エンティティタイプのボーナス倍率を取得
     *
     * @param type エンティティタイプ
     * @return ボーナス倍率
     */
    private double getTypeBonus(EntityType type) {
        return switch (type) {
            // ボス系モブ
            case ENDER_DRAGON, WITHER, ELDER_GUARDIAN -> 10.0;

            // 強力なモブ
            case WARDEN, RAVAGER, PIGLIN_BRUTE -> 5.0;

            // 準ボス級
            case GUARDIAN -> 3.0;

            // 敵対的モブ
            case ZOMBIE, SKELETON, SPIDER, CREEPER, ENDERMAN,
                 WITCH, SLIME, PHANTOM, DROWNED, HUSK, STRAY,
                 CAVE_SPIDER, SILVERFISH, BLAZE, GHAST, MAGMA_CUBE,
                 SHULKER, BREEZE -> 2.0;

            // 中立モブ
            case IRON_GOLEM, SNOW_GOLEM, POLAR_BEAR, LLAMA,
                 TRADER_LLAMA, WOLF, FOX, OCELOT, CAT,
                 PANDA, BEE -> 1.5;

            // 受動的モブ
            default -> 1.0;
        };
    }

    /**
     * MythicMobsか確認
     *
     * @param entity エンティティ
     * @return MythicMobsの場合はtrue
     */
    private boolean isMythicMob(Entity entity) {
        // MythicMobsが有効か確認
        if (!plugin.getDependencyManager().isMythicMobsAvailable()) {
            return false;
        }

        try {
            Optional<ActiveMob> activeMob = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId());
            return activeMob.isPresent();
        } catch (Exception e) {
            plugin.getLogger().fine("[Currency] Failed to check MythicMob: " + e.getMessage());
            return false;
        }
    }

    /**
     * ゴールド獲得メッセージを送信
     *
     * @param player プレイヤー
     * @param entity 倒されたエンティティ
     * @param amount 獲得ゴールド
     */
    private void sendGoldEarnMessage(Player player, Entity entity, double amount) {
        boolean isMythicMob = isMythicMob(entity);

        if (isMythicMob) {
            player.sendMessage(ChatColor.GOLD + "【ゴールド獲得】 " + ChatColor.YELLOW + String.format("%.2f", amount) + " G" +
                    ChatColor.GRAY + " (MythicMobボーナス: x" + MYTHICMOB_BONUS_MULTIPLIER + ")");
        } else if (amount >= 50.0) {
            player.sendMessage(ChatColor.GOLD + "【ゴールド獲得】 " + ChatColor.YELLOW + String.format("%.2f", amount) + " G" +
                    ChatColor.GRAY + " (" + entity.getName() + ")");
        }
    }
}
