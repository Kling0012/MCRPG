package com.example.rpgplugin.player;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;

/**
 * バニラ経験値ハンドラー
 *
 * <p>バニラのレベルアップシステムと統合し、レベルアップ時に自動ステータス配分と
 * 手動配分ポイントの付与を行います。また、経験値減衰システムも担当します。</p>
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>オブザーバーパターン: バニライベントを監視</li>
 *   <li>ストラテジーパターン: クラスごとの成長設定を適用</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: レベルアップ処理に特化</li>
 *   <li>DRY: レベルアップロジックを一元管理</li>
 *   <li>KISS: シンプルなイベント処理</li>
 * </ul>
 *
 * <p>レベルアップ処理:</p>
 * <ol>
 *   <li>自動配分: 各ステータス+2（クラス成長設定に基づく）</li>
 *   <li>手動配分ポイント: +3ポイント獲得</li>
 *   <li>通知メッセージ表示</li>
 * </ol>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class VanillaExpHandler implements Listener {

    private final RPGPlugin plugin;
    private final PlayerManager playerManager;
    private final ExpDiminisher expDiminisher;

    /**
     * 自動配分ポイント（デフォルト: 各ステータス+2）
     */
    private static final int AUTO_ALLOCATE_POINTS = 2;

    /**
     * 手動配分ポイント（デフォルト: +3ポイント）
     */
    private static final int MANUAL_ALLOCATE_POINTS = 3;

    /**
     * コンストラクタ
     *
     * @param plugin        プラグインインスタンス
     * @param playerManager プレイヤーマネージャー
     * @param expDiminisher 経験値減衰マネージャー
     */
    public VanillaExpHandler(RPGPlugin plugin, PlayerManager playerManager, ExpDiminisher expDiminisher) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.expDiminisher = expDiminisher;
    }

    /**
     * レベル変更イベント
     *
     * <p>プレイヤーがレベルアップした時に自動配分と手動配分ポイントの付与を行います。</p>
     *
     * @param event レベル変更イベント
     */
    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        int oldLevel = event.getOldLevel();
        int newLevel = event.getNewLevel();

        // レベルアップ時のみ処理
        if (newLevel <= oldLevel) {
            return;
        }

        Player player = event.getPlayer();
        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());

        if (rpgPlayer == null) {
            plugin.getLogger().warning("RPGPlayer not found for: " + player.getName());
            return;
        }

        try {
            handleLevelUp(rpgPlayer, newLevel);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to handle level up for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 経験値変更イベント
     *
     * <p>経験値の変化を監視し、減衰システムを適用します。</p>
     *
     * <p>処理フロー:</p>
     * <ol>
     *   <li>ExpDiminisherによる減衰計算と適用</li>
     *   <li>デバッグログ出力（設定時）</li>
     * </ol>
     *
     * @param event 経験値変更イベント
     */
    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        // 経験値減衰を適用
        expDiminisher.applyDiminishment(event);

        // デバッグログ出力
        if (plugin.getConfigManager().getBoolean("main", "debug.log_exp_changes", false)) {
            Player player = event.getPlayer();
            int amount = event.getAmount();
            plugin.getLogger().fine("Player " + player.getName() + " gained " + amount + " exp");
        }
    }

    /**
     * レベルアップ処理を実行します
     *
     * <p>処理内容:</p>
     * <ol>
     *   <li>自動配分: 各ステータス+2</li>
     *   <li>手動配分ポイント: +3</li>
     *   <li>メッセージ通知</li>
     * </ol>
     *
     * @param rpgPlayer RPGプレイヤー
     * @param newLevel 新しいレベル
     */
    private void handleLevelUp(RPGPlayer rpgPlayer, int newLevel) {
        Player player = rpgPlayer.getBukkitPlayer();
        if (player == null) {
            return;
        }

        // 1. 自動配分: 各ステータス+2
        for (Stat stat : Stat.values()) {
            int currentBase = rpgPlayer.getBaseStat(stat);
            rpgPlayer.setBaseStat(stat, currentBase + AUTO_ALLOCATE_POINTS);
        }

        // 2. 手動配分ポイント: +3
        rpgPlayer.addAvailablePoints(MANUAL_ALLOCATE_POINTS);

        // 3. StatManagerの総レベルを更新
        rpgPlayer.getStatManager().setTotalLevel(newLevel);

        // 4. メッセージ通知
        sendLevelUpMessage(rpgPlayer, newLevel);
    }

    /**
     * レベルアップメッセージを送信します
     *
     * @param rpgPlayer RPGプレイヤー
     * @param newLevel 新しいレベル
     */
    private void sendLevelUpMessage(RPGPlayer rpgPlayer, int newLevel) {
        Player player = rpgPlayer.getBukkitPlayer();
        if (player == null) {
            return;
        }

        // レベルアップメッセージ
        player.sendMessage("");
        player.sendMessage("§6§l========== レベルアップ! ==========");
        player.sendMessage("§eレベル " + (newLevel - 1) + " §7→ §eレベル " + newLevel);
        player.sendMessage("");
        player.sendMessage("§a自動配分: §f全ステータス +" + AUTO_ALLOCATE_POINTS);
        player.sendMessage("§b手動配分ポイント: §f+" + MANUAL_ALLOCATE_POINTS + " (残り: §e" + rpgPlayer.getAvailablePoints() + "§f)");
        player.sendMessage("§7§o/rpg stats §r§7でステータスを確認できます");
        player.sendMessage("§6§l================================");

        // タイトル表示（オプション）
        if (plugin.getConfigManager().getBoolean("main", "level_up.show_title", true)) {
            player.sendTitle(
                "§6§lLEVEL UP!",
                "§eレベル " + newLevel,
                10,  // フェードイン
                40,  // 表示時間
                10   // フェードアウト
            );
        }

        // サウンド効果（オプション）
        if (plugin.getConfigManager().getBoolean("main", "level_up.play_sound", true)) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }
}
