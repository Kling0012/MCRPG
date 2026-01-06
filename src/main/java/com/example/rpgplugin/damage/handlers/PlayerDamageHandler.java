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
 * プレイヤー→エンティティのダメージ計算ハンドラー
 *
 * <p>プレイヤーがエンティティに与えるダメージを計算・適用します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: プレイヤー→エンティティダメージのみ担当</li>
 *   <li>DRY: DamageModifierを活用</li>
 *   <li>KISS: シンプルなフロー</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class PlayerDamageHandler {

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
    public PlayerDamageHandler(RPGPlugin plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
        this.logger = plugin.getLogger();
    }

    /**
     * プレイヤーからエンティティへのダメージを計算・適用
     *
     * @param event ダメージイベント
     * @return 計算後のダメージ値、イベントをキャンセルする場合は-1
     */
    public double handlePlayerToEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity target = event.getEntity();

        // プレイヤーでない場合は処理しない
        if (!(damager instanceof Player)) {
            return -1;
        }

        Player player = (Player) damager;

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

        // クリティカル判定
        boolean isCritical = DamageModifier.isCriticalHit(player, stats);
        int dexterity = stats.getOrDefault(Stat.DEXTERITY, 0);
        double critMultiplier = DamageModifier.calculateCriticalMultiplier(dexterity);

        // クラス倍率（将来的に実装、現在は1.0固定）
        double classMultiplier = 1.0;

        // ダメージ計算
        double calculatedDamage;
        if (isPhysical) {
            // 物理ダメージ計算
            int strength = stats.getOrDefault(Stat.STRENGTH, 0);
            calculatedDamage = DamageModifier.calculatePhysicalDamage(
                    baseDamage,
                    strength,
                    classMultiplier,
                    isCritical,
                    critMultiplier
            );
        } else {
            // 魔法ダメージ計算
            int intelligence = stats.getOrDefault(Stat.INTELLIGENCE, 0);
            calculatedDamage = DamageModifier.calculateMagicDamage(
                    baseDamage,
                    intelligence,
                    classMultiplier
            );
        }

        // 整数に丸める
        int finalDamage = DamageModifier.roundDamage(calculatedDamage);

        // ダメージ設定
        event.setDamage(finalDamage);

        // ログ出力
        if (DEBUG) {
            logDamage(player, target, baseDamage, finalDamage, isCritical, stats);
        }

        // クリティカルエフェクト（将来的に実装）
        if (isCritical) {
            showCriticalEffect(player, target);
        }

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
     * クリティカルエフェクトを表示
     *
     * @param player 攻撃者
     * @param target ターゲット
     */
    private void showCriticalEffect(Player player, Entity target) {
        // ターゲットの頭上にパーティクル表示
        target.getWorld().spawnParticle(
                org.bukkit.Particle.FLAME,
                target.getLocation().add(0, 2, 0),
                10,
                0.5, 0.5, 0.5,
                0.05
        );

        // プレイヤーにメッセージ送信
        player.sendMessage("§6✨ クリティカルヒット！ ✨");
    }

    /**
     * ダメージログを出力
     *
     * @param attacker      攻撃者
     * @param target        ターゲット
     * @param baseDamage    基本ダメージ
     * @param finalDamage   最終ダメージ
     * @param isCritical    クリティカルかどうか
     * @param stats         ステータス
     */
    private void logDamage(Player attacker, Entity target,
                          double baseDamage, int finalDamage,
                          boolean isCritical, Map<Stat, Integer> stats) {
        String critStr = isCritical ? " [CRITICAL!]" : "";
        logger.info(String.format(
                "[Damage] %s->%s: Base=%.1f, STR=%d, INT=%d, Final=%d%s",
                attacker.getName(),
                target.getName(),
                baseDamage,
                stats.getOrDefault(Stat.STRENGTH, 0),
                stats.getOrDefault(Stat.INTELLIGENCE, 0),
                finalDamage,
                critStr
        ));
    }

    /**
     * 警告ログを出力
     *
     * @param message メッセージ
     */
    private void logWarning(String message) {
        logger.warning("[PlayerDamageHandler] " + message);
    }
}
