package com.example.rpgplugin.damage;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.damage.config.DamageConfig;
import com.example.rpgplugin.damage.config.DamageConfigLoader;
import com.example.rpgplugin.damage.config.VariableScopeManager;
import com.example.rpgplugin.damage.config.YamlDamageCalculator;
import com.example.rpgplugin.damage.handlers.EntityDamageHandler;
import com.example.rpgplugin.damage.handlers.PlayerDamageHandler;
import com.example.rpgplugin.player.PlayerManager;
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
 * <p>パフォーマンス最適化:</p>
 * <ul>
 *   <li>ダメージ計算キャッシュの自動クリアタスク</li>
 *   <li>5秒ごとに期限切れキャッシュを削除</li>
 * </ul>
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

    // キャッシュクリアタスクID
    private int cacheClearTaskId = -1;

    // YAMLダメージ設定
    private DamageConfig damageConfig;
    private DamageConfigLoader configLoader;
    private VariableScopeManager scopeManager;
    private YamlDamageCalculator yamlCalculator;
    private boolean useYamlConfig = true;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     * @param playerManager プレイヤーマネージャー
     */
    public DamageManager(RPGPlugin plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.playerDamageHandler = new PlayerDamageHandler(playerManager, logger);
        this.entityDamageHandler = new EntityDamageHandler(playerManager, logger);
        this.enabled = true;

        // YAMLダメージ設定を初期化
        this.scopeManager = new VariableScopeManager();
        this.configLoader = new DamageConfigLoader(logger, plugin.getDataFolder());
        this.yamlCalculator = null; // loadConfig()で初期化
    }

    /**
     * ダメージマネージャーを初期化
     *
     * <p>キャッシュクリアタスクを開始し、YAML設定を読み込みます。</p>
     */
    public void initialize() {
        startCacheClearTask();
        loadDamageConfig();
        logger.info("DamageManager initialized with cache optimization and YAML config");
    }

    /**
     * YAMLダメージ設定を読み込みます
     */
    public void loadDamageConfig() {
        if (!useYamlConfig) {
            logger.info("YAML config is disabled, using default damage calculation");
            propagateCalculatorToHandlers();
            return;
        }

        // 設定ファイルが存在しない場合はデフォルトを作成
        if (!configLoader.configExists()) {
            logger.info("Damage config file not found, creating default config");
            configLoader.createDefaultConfig();
        }

        // 設定を読み込み
        damageConfig = configLoader.loadConfig();
        if (damageConfig != null) {
            yamlCalculator = new YamlDamageCalculator(damageConfig, scopeManager);
            propagateCalculatorToHandlers();
            logger.info("YAML damage config loaded successfully");
        } else {
            logger.warning("Failed to load YAML damage config, using defaults");
        }
    }

    /**
     * YAMLダメージ設定を再読み込みします
     */
    public void reloadDamageConfig() {
        damageConfig = configLoader.reloadConfig();
        if (damageConfig != null) {
            yamlCalculator = new YamlDamageCalculator(damageConfig, scopeManager);
            propagateCalculatorToHandlers();
            logger.info("YAML damage config reloaded successfully");
        } else {
            logger.warning("Failed to reload YAML damage config");
        }
    }

    /**
     * YAML計算機をハンドラーに伝播します
     */
    private void propagateCalculatorToHandlers() {
        playerDamageHandler.setYamlCalculator(yamlCalculator);
        entityDamageHandler.setYamlCalculator(yamlCalculator);
    }

    /**
     * キャッシュクリアタスクを開始
     *
     * <p>5秒ごとに期限切れのダメージ計算キャッシュをクリアします。</p>
     */
    private void startCacheClearTask() {
        long intervalTicks = 5 * 20L; // 5秒

        cacheClearTaskId = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                () -> {
                    playerDamageHandler.clearCache();
                    if (logger.isLoggable(java.util.logging.Level.FINE)) {
                        logger.fine("Damage cache cleared. Size: " + playerDamageHandler.getCacheSize());
                    }
                },
                intervalTicks,
                intervalTicks
        ).getTaskId();

        logger.info("Cache clear task started: interval=5s");
    }

    /**
     * ダメージマネージャーをシャットダウン
     *
     * <p>キャッシュクリアタスクを停止します。</p>
     */
    public void shutdown() {
        if (cacheClearTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(cacheClearTaskId);
            cacheClearTaskId = -1;
            logger.info("Cache clear task stopped");
        }
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
        // PvPは未サポートのため、バニラの動作に委ねる
        // 補正等の処理は行わない
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

    /**
     * YAMLダメージ計算機を取得します
     *
     * @return YAMLダメージ計算機、設定未読み込み時はnull
     */
    public YamlDamageCalculator getYamlCalculator() {
        return yamlCalculator;
    }

    /**
     * 変数スコープマネージャーを取得します
     *
     * @return 変数スコープマネージャー
     */
    public VariableScopeManager getScopeManager() {
        return scopeManager;
    }

    /**
     * ダメージ設定を取得します
     *
     * @return ダメージ設定、設定未読み込み時はnull
     */
    public DamageConfig getDamageConfig() {
        return damageConfig;
    }

    /**
     * YAML設定使用フラグを設定します
     *
     * @param useYamlConfig YAML設定を使用するかどうか
     */
    public void setUseYamlConfig(boolean useYamlConfig) {
        this.useYamlConfig = useYamlConfig;
        if (useYamlConfig) {
            loadDamageConfig();
        } else {
            yamlCalculator = null;
            propagateCalculatorToHandlers(); // nullを伝播してレガシー計算に戻す
        }
    }

    /**
     * YAML設定を使用しているかどうか
     *
     * @return YAML設定を使用している場合はtrue
     */
    public boolean isUseYamlConfig() {
        return useYamlConfig && yamlCalculator != null;
    }
}
