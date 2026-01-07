package com.example.rpgplugin.damage.handlers;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.damage.DamageModifier;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;
import java.util.logging.Logger;

/**
 * エンティティ→プレイヤーのダメージ計算ハンドラー
 *
 * <p>エンティティがプレイヤーに与えるダメージを計算・防御を適用します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: エンティティ→プレイヤーダメージのみ担当</li>
 *   <li>DRY: DamageModifierを活用</li>
 *   <li>KISS: シンプルなフロー</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class EntityDamageHandler {

    private final RPGPlugin plugin;
    private final PlayerManager playerManager;
    private final Logger logger;

    // デバッグログフラグ
    private final boolean DEBUG = false;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public EntityDamageHandler(RPGPlugin plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
        this.logger = plugin.getLogger();
    }

    /**
     * エンティティからプレイヤーへのダメージを計算・適用
     *
     * @param event ダメージイベント
     * @return 計算後のダメージ値、イベントをキャンセルする場合は-1
     */
    public double handleEntityToPlayerDamage(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();

        // プレイヤーでない場合は処理しない
        if (!(target instanceof Player)) {
            return -1;
        }

        Player player = (Player) target;

        // RPGPlayer取得
        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            logWarning("RPGPlayer not found for: " + player.getName());
            return -1;
        }

        // ステータス取得
        Map<Stat, Integer> stats = rpgPlayer.getStatManager().getAllFinalStats();

        // 基本ダメージ取得
        double baseDamage = event.getDamage();

        // ダメージタイプ判定
        EntityDamageEvent.DamageCause cause = event.getCause();
        boolean isPhysical = isPhysicalDamage(cause);

        // 防御計算
        double damageAfterDefense;
        if (isPhysical) {
            // 物理防御（VIT依存）
            int vitality = stats.getOrDefault(Stat.VITALITY, 0);
            damageAfterDefense = DamageModifier.calculateDefenseCut(baseDamage, vitality);
        } else {
            // 魔法防御（SPI依存）
            int spirit = stats.getOrDefault(Stat.SPIRIT, 0);
            damageAfterDefense = DamageModifier.calculateMagicDefenseCut(baseDamage, spirit);
        }

        // 整数に丸める
        int finalDamage = DamageModifier.roundDamage(damageAfterDefense);

        // ダメージ設定
        event.setDamage(finalDamage);

        // ログ出力
        if (DEBUG) {
            logDamage(event.getDamager(), player, baseDamage, finalDamage, stats);
        }

        // ダメージ数値表示
        showDamageIndicator(player, finalDamage);

        return finalDamage;
    }

    /**
     * 物理ダメージかどうかを判定
     *
     * @param cause ダメージ原因
     * @return 物理ダメージならtrue
     */
    private boolean isPhysicalDamage(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case ENTITY_ATTACK,
                 ENTITY_SWEEP_ATTACK -> true;
            default -> false;
        };
    }

    /**
     * ダメージ数値を表示
     *
     * @param player プレイヤー
     * @param damage ダメージ値
     */
    private void showDamageIndicator(Player player, int damage) {
        // アクションバーに表示
        String message = String.format("§c↗ %d", damage);
        player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(message)
        );
    }

    /**
     * ダメージログを出力
     *
     * @param damager      攻撃者
     * @param victim       被害者
     * @param baseDamage   基本ダメージ
     * @param finalDamage  最終ダメージ
     * @param stats        ステータス
     */
    private void logDamage(Entity damager, Player victim,
                          double baseDamage, int finalDamage,
                          Map<Stat, Integer> stats) {
        logger.info(String.format(
                "[Damage] %s->Player(%s): Base=%.1f, VIT=%d, SPI=%d, Final=%d",
                damager.getName(),
                victim.getName(),
                baseDamage,
                stats.getOrDefault(Stat.VITALITY, 0),
                stats.getOrDefault(Stat.SPIRIT, 0),
                finalDamage
        ));
    }

    /**
     * 警告ログを出力
     *
     * @param message メッセージ
     */
    private void logWarning(String message) {
        logger.warning("[EntityDamageHandler] " + message);
    }
}
