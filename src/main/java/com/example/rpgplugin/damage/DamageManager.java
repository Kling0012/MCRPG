package com.example.rpgplugin.damage;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.damage.handlers.EntityDamageHandler;
import com.example.rpgplugin.damage.handlers.PlayerDamageHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.logging.Logger;

/**
 * ダメージシステム統括マネージャー
 *
 * <p>全ダメージイベントをキャッチし、ステータスに応じたダメージ計算を行います。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ダメージイベント処理のみ担当</li>
 *   <li>DRY: 各ハンドラーに委譲</li>
 *   <li>OCP: 拡張可能（将来的に他ダメージタイプ追加）</li>
 * </ul>
 *
 * <p>優先度: HIGH（他プラグインより先にダメージ計算を完了させる）</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class DamageManager implements Listener {

    private final RPGPlugin plugin;
    private final PlayerDamageHandler playerDamageHandler;
    private final EntityDamageHandler entityDamageHandler;
    private final Logger logger;

    // 有効フラグ
    private boolean enabled;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public DamageManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.playerDamageHandler = new PlayerDamageHandler(plugin);
        this.entityDamageHandler = new EntityDamageHandler(plugin);
        this.enabled = true;
    }

    /**
     * プレイヤーが関係するダメージイベントを処理
     *
     * <p>EventPriority.HIGHで他プラグインより先にダメージ計算を適用します。</p>
     *
     * @param event エンティティダメージイベント
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // 無効な場合は処理しない
        if (!enabled) {
            return;
        }

        Entity damager = event.getDamager();
        Entity target = event.getEntity();

        // プレイヤーが関与しない場合は処理しない
        if (!(damager instanceof Player) && !(target instanceof Player)) {
            return;
        }

        // プレイヤー→エンティティ
        if (damager instanceof Player && !(target instanceof Player)) {
            double damage = playerDamageHandler.handlePlayerToEntityDamage(event);

            // ダメージ計算失敗時はキャンセル
            if (damage < 0) {
                event.setCancelled(true);
            }
            return;
        }

        // エンティティ→プレイヤー
        if (!(damager instanceof Player) && target instanceof Player) {
            double damage = entityDamageHandler.handleEntityToPlayerDamage(event);

            // ダメージ計算失敗時はキャンセル
            if (damage < 0) {
                event.setCancelled(true);
            }
            return;
        }

        // プレイヤー→プレイヤー（PvP）
        // 現時点では通常ダメージを適用
        // 将来的にはPvP補正を実装予定
    }

    /**
     * その他のダメージイベントを処理（将来的に実装）
     *
     * @param event エンティティダメージイベント
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        // 無効な場合は処理しない
        if (!enabled) {
            return;
        }

        Entity target = event.getEntity();

        // プレイヤー以外は処理しない
        if (!(target instanceof Player)) {
            return;
        }

        // 環境ダメージ（落下、火傷、毒など）への対応
        // 将来的に実装
        Player player = (Player) target;

        // 特定のダメージタイプには補正を適用しない
        switch (event.getCause()) {
            case VOID:
            case SUICIDE:
            case KILL:
                // 即死系は補正なし
                return;
            default:
                // 他のダメージタイプは将来的に対応
                break;
        }
    }

    /**
     * ダメージシステムを有効化
     */
    public void enable() {
        if (!enabled) {
            this.enabled = true;
            logger.info("[DamageManager] ダメージシステムを有効化しました");
        }
    }

    /**
     * ダメージシステムを無効化
     */
    public void disable() {
        if (enabled) {
            this.enabled = false;
            logger.info("[DamageManager] ダメージシステムを無効化しました");
        }
    }

    /**
     * ダメージシステムが有効かどうか
     *
     * @return 有効ならtrue
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * プレイヤーダメージハンドラーを取得
     *
     * @return プレイヤーダメージハンドラー
     */
    public PlayerDamageHandler getPlayerDamageHandler() {
        return playerDamageHandler;
    }

    /**
     * エンティティダメージハンドラーを取得
     *
     * @return エンティティダメージハンドラー
     */
    public EntityDamageHandler getEntityDamageHandler() {
        return entityDamageHandler;
    }
}
