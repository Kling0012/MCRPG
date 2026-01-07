package com.example.rpgplugin.player;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.rpgclass.ClassManager;
import com.example.rpgplugin.rpgclass.RPGClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * MP管理クラス
 *
 * <p>主な機能：</p>
 * <ul>
 *   <li>毎秒MP自然回復の処理</li>
 *   <li>スキルによる特殊回復の外部制御</li>
 *   <li>クラスごとの回復量設定に基づく回復処理</li>
 * </ul>
 */
public class ManaManager {

    private final RPGPlugin plugin;
    private final Logger logger;
    private final PlayerManager playerManager;
    private final ClassManager classManager;

    /** MP回復タスク */
    private BukkitTask regenerationTask;

    /** カスタム回復量上書き（プレイヤーUUID -> 回復量/秒） */
    private final Map<UUID, Double> customRegenRates;

    /** 一時回復ボーナス（プレイヤーUUID -> ボーナス量/秒） */
    private final Map<UUID, Double> temporaryBonus;

    /** デフォルトMP回復量/秒 */
    private static final double DEFAULT_MANA_REGEN = 1.0;

    /** 回復タスクの実行間隔（tick） */
    private static final long REGEN_TASK_INTERVAL = 20L; // 1秒

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     * @param playerManager プレイヤーマネージャー
     * @param classManager クラスマネージャー
     */
    public ManaManager(RPGPlugin plugin, PlayerManager playerManager, ClassManager classManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.playerManager = playerManager;
        this.classManager = classManager;
        this.customRegenRates = new HashMap<>();
        this.temporaryBonus = new HashMap<>();
    }

    /**
     * MP回復処理を開始します
     */
    public void start() {
        if (regenerationTask != null && !regenerationTask.isCancelled()) {
            logger.warning("Mana regeneration task is already running");
            return;
        }

        regenerationTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (RPGPlayer rpgPlayer : playerManager.getOnlinePlayers()) {
                try {
                    regenerateMana(rpgPlayer);
                } catch (Exception e) {
                    logger.warning("Failed to regenerate mana for player: " + rpgPlayer.getUsername());
                    e.printStackTrace();
                }
            }
        }, REGEN_TASK_INTERVAL, REGEN_TASK_INTERVAL);

        logger.info("Mana regeneration task started");
    }

    /**
     * MP回復処理を停止します
     */
    public void stop() {
        if (regenerationTask != null && !regenerationTask.isCancelled()) {
            regenerationTask.cancel();
            regenerationTask = null;
            logger.info("Mana regeneration task stopped");
        }
    }

    /**
     * プレイヤーのMPを回復します
     *
     * @param rpgPlayer RPGプレイヤー
     */
    private void regenerateMana(RPGPlayer rpgPlayer) {
        Player player = rpgPlayer.getBukkitPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }

        // 最大MPに達している場合は回復しない
        if (rpgPlayer.isFullMana()) {
            return;
        }

        // 回復量の計算
        double regenAmount = calculateRegenAmount(rpgPlayer);
        if (regenAmount <= 0) {
            return;
        }

        // 回復実行
        int actualRegen = rpgPlayer.regenerateMana(regenAmount);

        // 回復量が0でない場合はアクションバーで表示（オプション）
        if (actualRegen > 0 && shouldShowRegenIndicator(rpgPlayer)) {
            showRegenIndicator(player, actualRegen);
        }
    }

    /**
     * 回復量を計算します
     *
     * @param rpgPlayer RPGプレイヤー
     * @return 回復量/秒
     */
    private double calculateRegenAmount(RPGPlayer rpgPlayer) {
        UUID uuid = rpgPlayer.getUuid();

        // カスタム回復量が設定されている場合はそれを使用
        if (customRegenRates.containsKey(uuid)) {
            double custom = customRegenRates.get(uuid);
            double bonus = temporaryBonus.getOrDefault(uuid, 0.0);
            return custom + bonus;
        }

        // クラスごとの回復量を取得
        double classRegen = DEFAULT_MANA_REGEN;
        if (classManager != null) {
            String classId = rpgPlayer.getClassId();
            if (classId != null) {
                var classOpt = classManager.getClass(classId);
                if (classOpt.isPresent()) {
                    RPGClass rpgClass = classOpt.get();
                    classRegen = rpgClass.getManaRegen();
                }
            }
        }

        // 一時ボーナスを加算
        double bonus = temporaryBonus.getOrDefault(uuid, 0.0);
        return classRegen + bonus;
    }

    /**
     * 回復インジケーターを表示するかどうか
     *
     * @param rpgPlayer RPGプレイヤー
     * @return 表示する場合はtrue
     */
    private boolean shouldShowRegenIndicator(RPGPlayer rpgPlayer) {
        // MPが50%未満の場合のみ表示
        return rpgPlayer.getManaRatio() < 0.5;
    }

    /**
     * 回復インジケーターを表示します
     *
     * @param player Bukkitプレイヤー
     * @param amount 回復量
     */
    private void showRegenIndicator(Player player, int amount) {
        // 簡易的なアクションバーメッセージ
        player.sendActionBar("§b+" + amount + " MP");
    }

    // ==================== 外部制御メソッド ====================

    /**
     * プレイヤーのMP回復量を上書き設定します
     *
     * <p>この設定は{@link #clearCustomRegenRate(UUID)}が呼ばれるまで継続します。</p>
     *
     * @param uuid プレイヤーUUID
     * @param regenAmount 回復量/秒（0以下で設定解除）
     */
    public void setCustomRegenRate(UUID uuid, double regenAmount) {
        if (regenAmount <= 0) {
            customRegenRates.remove(uuid);
        } else {
            customRegenRates.put(uuid, regenAmount);
        }
    }

    /**
     * プレイヤーのカスタム回復量設定を解除します
     *
     * @param uuid プレイヤーUUID
     */
    public void clearCustomRegenRate(UUID uuid) {
        customRegenRates.remove(uuid);
    }

    /**
     * 一時的なMP回復ボーナスを設定します
     *
     * <p>このボーナスはクラス設定やカスタム設定に加算されます。</p>
     *
     * @param uuid プレイヤーUUID
     * @param bonus ボーナス量/秒
     */
    public void addTemporaryBonus(UUID uuid, double bonus) {
        temporaryBonus.merge(uuid, bonus, Double::sum);
    }

    /**
     * 一時的なMP回復ボーナスを設定します（上書き）
     *
     * @param uuid プレイヤーUUID
     * @param bonus ボーナス量/秒（0以下で設定解除）
     */
    public void setTemporaryBonus(UUID uuid, double bonus) {
        if (bonus <= 0) {
            temporaryBonus.remove(uuid);
        } else {
            temporaryBonus.put(uuid, bonus);
        }
    }

    /**
     * 一時的なMP回復ボーナスを解除します
     *
     * @param uuid プレイヤーUUID
     */
    public void clearTemporaryBonus(UUID uuid) {
        temporaryBonus.remove(uuid);
    }

    /**
     * スキルによる特殊回復を実行します
     *
     * <p>即時回復を行います。毎秒回復とは別に処理されます。</p>
     *
     * @param rpgPlayer RPGプレイヤー
     * @param amount 回復量
     * @return 実際に回復した量
     */
    public int skillRegenMana(RPGPlayer rpgPlayer, double amount) {
        if (rpgPlayer == null || amount <= 0) {
            return 0;
        }
        return rpgPlayer.regenerateMana(amount);
    }

    /**
     * プレイヤーの現在のMP回復量を取得します
     *
     * @param rpgPlayer RPGプレイヤー
     * @return 現在の回復量/秒
     */
    public double getCurrentRegenRate(RPGPlayer rpgPlayer) {
        if (rpgPlayer == null) {
            return DEFAULT_MANA_REGEN;
        }
        return calculateRegenAmount(rpgPlayer);
    }

    /**
     * デフォルトMP回復量を取得します
     *
     * @return デフォルト回復量/秒
     */
    public static double getDefaultManaRegen() {
        return DEFAULT_MANA_REGEN;
    }

    /**
     * プラグイン無効化時のクリーンアップ処理
     */
    public void shutdown() {
        stop();
        customRegenRates.clear();
        temporaryBonus.clear();
        logger.info("ManaManager shut down");
    }
}
