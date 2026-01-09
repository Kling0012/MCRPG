package com.example.rpgplugin.player;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.rpgclass.ClassManager;
import com.example.rpgplugin.rpgclass.RPGClass;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpChangeEvent;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * 経験値減衰マネージャー
 *
 * <p>プレイヤーのレベルとクラス設定に基づいて、経験値取得量に減衰を適用します。</p>
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>ストラテジーパターン: クラスごとの減衰設定を適用</li>
 *   <li>ファサードパターン: 減衰計算への統一インターフェース</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: 経験値減衰に特化</li>
 *   <li>DRY: 減衰ロジックを一元管理</li>
 *   <li>KISS: シンプルな計算ロジック</li>
 * </ul>
 *
 * <p>減衰計算フロー:</p>
 * <ol>
 *   <li>減衰機能が有効か確認</li>
 *   <li>除外条件（プレイヤーキル、ボス、イベント報酬）をチェック</li>
 *   <li>プレイヤーのクラス設定を取得</li>
 *   <li>レベルとクラス設定に基づいて減衰率を計算</li>
 *   <li>減衰適用後の経験値を返却</li>
 * </ol>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ExpDiminisher {

    private final RPGPlugin plugin;
    private final PlayerManager playerManager;
    private final ClassManager classManager;
    private final Logger logger;

    /**
     * 減衰除外キー
     */
    public static final String EXEMPTION_PLAYER_KILL = "player_kill";
    public static final String EXEMPTION_BOSS_MOB = "boss_mob";
    public static final String EXEMPTION_EVENT_REWARD = "event_reward";

    /**
     * コンストラクタ
     *
     * @param plugin        プラグインインスタンス
     * @param playerManager プレイヤーマネージャー
     * @param classManager  クラスマネージャー
     */
    public ExpDiminisher(RPGPlugin plugin, PlayerManager playerManager, ClassManager classManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.classManager = classManager;
        this.logger = plugin.getLogger();
    }

    /**
     * 経験値イベントに減衰を適用します
     *
     * <p>PlayerExpChangeEventを監視し、必要に応じて経験値を減衰させます。</p>
     *
     * @param event 経験値変更イベント
     */
    public void applyDiminishment(PlayerExpChangeEvent event) {
        // 減衰機能が有効か確認
        if (!isDiminishmentEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        int originalExp = event.getAmount();

        // 除外条件のチェック
        if (isExempted(player, event)) {
            logDiminishment(player, originalExp, originalExp, 0.0, "Exempted");
            return;
        }

        // 減衰適用
        int diminishedExp = calculateDiminishedExp(player, originalExp);

        // イベントの経験値を変更
        if (diminishedExp != originalExp) {
            event.setAmount(diminishedExp);
        }

        // ログ出力（デバッグ用）
        if (plugin.getConfigManager().getBoolean("main", "debug.log_exp_diminish", false)) {
            double reductionRate = originalExp > 0 ? 1.0 - (double) diminishedExp / originalExp : 0.0;
            logDiminishment(player, originalExp, diminishedExp, reductionRate, "Applied");
        }
    }

    /**
     * 減衰後の経験値を計算します
     *
     * @param player       プレイヤー
     * @param originalExp  元の経験値
     * @return 減衰後の経験値
     */
    public int calculateDiminishedExp(Player player, int originalExp) {
        if (originalExp <= 0) {
            return originalExp;
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            logger.warning("RPGPlayer not found for: " + player.getName());
            return originalExp;
        }

        // クラス設定を取得
        Optional<RPGClass> classOpt = classManager.getPlayerClass(player);
        if (!classOpt.isPresent()) {
            // クラス未設定の場合は減衰なし
            return originalExp;
        }

        RPGClass rpgClass = classOpt.get();
        int level = player.getLevel();

        // クラス固有の減衰設定を適用
        RPGClass.ExpDiminish expDiminish = rpgClass.getExpDiminish();
        long diminishedExp = expDiminish.applyExp(originalExp, level);

        return (int) diminishedExp;
    }

    /**
     * 減衰機能が有効か確認します
     *
     * @return 有効な場合はtrue
     */
    private boolean isDiminishmentEnabled() {
        return plugin.getConfigManager().getBoolean("main", "exp_diminish.enabled", true);
    }

    /**
     * 除外条件に該当するか確認します
     *
     * @param player プレイヤー
     * @param event  経験値イベント
     * @return 除外される場合はtrue
     */
    private boolean isExempted(Player player, PlayerExpChangeEvent event) {
        // 設定から除外条件を読み込み
        boolean playerKillsExempt = plugin.getConfigManager()
                .getBoolean("main", "exp_diminish.exemptions.player_kills", true);
        boolean bossMobsExempt = plugin.getConfigManager()
                .getBoolean("main", "exp_diminish.exemptions.boss_mobs", true);
        boolean eventRewardsExempt = plugin.getConfigManager()
                .getBoolean("main", "exp_diminish.exemptions.event_rewards", true);

        // TODO: 実際の除外判定ロジックを実装
        // 例: プレイヤーキル、ボスモブ、イベント報酬などの判定

        return false;
    }

    /**
     * 減衰ログを出力します
     *
     * @param player        プレイヤー
     * @param originalExp   元の経験値
     * @param diminishedExp 減衰後経験値
     * @param reductionRate 減衰率
     * @param status        ステータス
     */
    private void logDiminishment(Player player, int originalExp, int diminishedExp,
                                  double reductionRate, String status) {
        logger.fine(String.format(
                "[ExpDiminish] %s (Lv%d): %d -> %d (%.1f%% reduction) [%s]",
                player.getName(),
                player.getLevel(),
                originalExp,
                diminishedExp,
                reductionRate * 100,
                status
        ));
    }

    /**
     * 減衰率を取得します
     *
     * @param player プレイヤー
     * @return 減衰率（0.0-1.0）
     */
    public double getDiminishRate(Player player) {
        Optional<RPGClass> classOpt = classManager.getPlayerClass(player);
        if (!classOpt.isPresent()) {
            return 0.0;
        }

        RPGClass rpgClass = classOpt.get();
        RPGClass.ExpDiminish expDiminish = rpgClass.getExpDiminish();
        if (expDiminish == null) {
            return 0.0;
        }

        int level = player.getLevel();
        if (level < expDiminish.getStartLevel()) {
            return 0.0;
        }

        return expDiminish.getReductionRate();
    }

    /**
     * 減衰開始レベルを取得します
     *
     * @param player プレイヤー
     * @return 開始レベル
     */
    public int getDiminishStartLevel(Player player) {
        Optional<RPGClass> classOpt = classManager.getPlayerClass(player);
        if (!classOpt.isPresent()) {
            return Integer.MAX_VALUE;
        }

        RPGClass rpgClass = classOpt.get();
        RPGClass.ExpDiminish expDiminish = rpgClass.getExpDiminish();
        if (expDiminish == null) {
            return Integer.MAX_VALUE;
        }

        return expDiminish.getStartLevel();
    }
}
