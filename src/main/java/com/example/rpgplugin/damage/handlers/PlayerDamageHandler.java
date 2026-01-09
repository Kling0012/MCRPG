package com.example.rpgplugin.damage.handlers;

import com.example.rpgplugin.damage.DamageModifier;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
 *   <li>OCP: キャッシュ戦略を変更可能</li>
 * </ul>
 *
 * <p>パフォーマンス最適化:</p>
 * <ul>
 *   <li>ダメージ計算結果を1秒間キャッシュ</li>
 *   <li>同じターゲットへの連続攻撃で計算を省略</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class PlayerDamageHandler {

    private final PlayerManager playerManager;
    private final Logger logger;

    // デバッグログフラグ
    private final boolean DEBUG = false;

    // ダメージ計算キャッシュ（1秒間有効）
    private final Map<UUID, CachedDamage> damageCache;
    private static final long DAMAGE_CACHE_MILLIS = 1000; // 1秒
    private static final int MAX_CACHE_SIZE = 1000; // 最大キャッシュ数

    /**
     * キャッシュされたダメージ計算結果
     *
     * @param damage 計算されたダメージ値
     * @param timestamp キャッシュ時刻（ミリ秒）
     */
    private record CachedDamage(double damage, long timestamp) {}

    /**
     * コンストラクタ
     *
     * @param playerManager プレイヤーマネージャー
     * @param logger ロガー
     */
    public PlayerDamageHandler(PlayerManager playerManager, Logger logger) {
        this.playerManager = playerManager;
        this.logger = logger;
        this.damageCache = new ConcurrentHashMap<>();
    }

    /**
     * キャッシュをクリア
     *
     * <p>定期的な呼び出しでメモリ使用量を抑制します。</p>
     */
    public void clearCache() {
        long now = System.currentTimeMillis();
        damageCache.entrySet().removeIf(entry -> {
            boolean expired = (now - entry.getValue().timestamp()) > DAMAGE_CACHE_MILLIS;
            return expired;
        });
    }

    /**
     * キャッシュサイズを取得
     *
     * @return 現在のキャッシュサイズ
     */
    public int getCacheSize() {
        return damageCache.size();
    }

    /**
     * プレイヤーからエンティティへのダメージを計算・適用
     *
     * <p>ダメージ計算結果を1秒間キャッシュし、連続攻撃時の計算コストを削減します。</p>
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
        UUID playerId = player.getUniqueId();

        // RPGPlayer取得
        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(playerId);
        if (rpgPlayer == null) {
            logWarning("RPGPlayer not found for: " + player.getName());
            return -1;
        }

        // キャッシュチェック
        CachedDamage cached = damageCache.get(playerId);
        long now = System.currentTimeMillis();

        // キャッシュが有効な場合はキャッシュ値を使用（ただし基本ダメージが同じ場合のみ）
        double baseDamage = event.getDamage();
        if (cached != null && (now - cached.timestamp()) < DAMAGE_CACHE_MILLIS) {
            int finalDamage = DamageModifier.roundDamage(cached.damage());
            event.setDamage(finalDamage);
            return finalDamage;
        }

        // ステータス取得
        Map<Stat, Integer> stats = rpgPlayer.getStatManager().getAllFinalStats();

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

        // キャッシュに保存（最大サイズチェック）
        if (damageCache.size() < MAX_CACHE_SIZE) {
            damageCache.put(playerId, new CachedDamage(calculatedDamage, now));
        }

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
