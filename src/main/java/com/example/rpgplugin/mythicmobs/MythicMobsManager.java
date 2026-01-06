package com.example.rpgplugin.mythicmobs;

import com.example.rpgplugin.core.dependency.MythicMobsHook;
import com.example.rpgplugin.mythicmobs.config.MobDropConfig;
import com.example.rpgplugin.mythicmobs.drop.DropHandler;
import com.example.rpgplugin.storage.database.ConnectionPool;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Logger;

/**
 * MythicMobs統合管理クラス
 *
 * <p>MythicMobsプラグインとの連携を統合管理します。
 * 既存のMythicMobsHookをラップし、ドロップ管理機能を提供します。</p>
 *
 * <p>主な機能:</p>
 * <ul>
 *   <li>モブ情報の取得・管理</li>
 *   <li>ドロップ処理の統括</li>
 *   <li>モスポーン制御</li>
 *   <li>設定管理</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>ファサードパターン: MythicMobs機能への統一インターフェース</li>
 *   <li>単一責任: MythicMobs連携の調整のみ担当</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class MythicMobsManager {

    private final JavaPlugin plugin;
    private final MythicMobsHook mythicMobsHook;
    private final DropHandler dropHandler;
    private final Logger logger;

    // ドロップ設定
    private Map<String, MobDropConfig.MobDrop> dropConfigs;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     * @param mythicMobsHook MythicMobsフック
     * @param connectionPool データベースコネクションプール
     */
    public MythicMobsManager(JavaPlugin plugin, MythicMobsHook mythicMobsHook, ConnectionPool connectionPool) {
        this.plugin = plugin;
        this.mythicMobsHook = mythicMobsHook;
        this.dropHandler = new DropHandler(connectionPool, logger);
        this.logger = plugin.getLogger();
        this.dropConfigs = new HashMap<>();
    }

    /**
     * マネージャーを初期化します
     *
     * @return 初期化に成功した場合はtrue
     */
    public boolean initialize() {
        if (!mythicMobsHook.isAvailable()) {
            logger.warning("MythicMobs is not available. Drop management disabled.");
            return false;
        }

        logger.info("MythicMobs Manager initialized successfully");
        logger.info("MythicMobs version: " + mythicMobsHook.getVersion());
        return true;
    }

    /**
     * ドロップ設定を読み込みます
     *
     * @param mobDropConfigs モブドロップ設定リスト
     */
    public void loadDropConfigs(List<MobDropConfig.MobDrop> mobDropConfigs) {
        dropConfigs.clear();

        for (MobDropConfig.MobDrop mobDrop : mobDropConfigs) {
            dropConfigs.put(mobDrop.getMobId(), mobDrop);
        }

        logger.info("Loaded " + dropConfigs.size() + " mob drop configurations");
    }

    /**
     * モブドロップ処理を実行します
     *
     * @param player 倒したプレイヤー
     * @param entity 死亡したエンティティ
     */
    public void handleMobDeath(Player player, Entity entity) {
        if (!mythicMobsHook.isAvailable()) {
            return;
        }

        // MythicMobかどうか確認
        if (!mythicMobsHook.isMythicMob(entity)) {
            return;
        }

        // モブIDを取得
        String mobId = mythicMobsHook.getMobTypeId(entity);
        if (mobId == null) {
            logger.fine("Could not get mob ID for entity: " + entity.getUniqueId());
            return;
        }

        // ドロップ設定を取得
        MobDropConfig.MobDrop mobDrop = dropConfigs.get(mobId);
        if (mobDrop == null) {
            logger.fine("No drop config found for mob: " + mobId);
            return;
        }

        // ドロップ処理を実行
        Location deathLocation = entity.getLocation();
        List<MobDropConfig.DropItem> droppedItems = mobDrop.processDrops();

        if (!droppedItems.isEmpty()) {
            dropHandler.processDrops(player, mobId, deathLocation, droppedItems);
        } else {
            logger.fine("No items dropped from mob: " + mobId);
        }
    }

    /**
     * エンティティがMythicMobか確認します
     *
     * @param entity エンティティ
     * @return MythicMobの場合はtrue
     */
    public boolean isMythicMob(Entity entity) {
        return mythicMobsHook.isMythicMob(entity);
    }

    /**
     * アクティブなMythicMobを取得します
     *
     * @param entity エンティティ
     * @return ActiveMobのOptional
     */
    public Optional<ActiveMob> getActiveMob(Entity entity) {
        return mythicMobsHook.getActiveMob(entity);
    }

    /**
     * モブIDを取得します
     *
     * @param entity エンティティ
     * @return モブID、MythicMobでない場合はnull
     */
    public String getMobId(Entity entity) {
        return mythicMobsHook.getMobTypeId(entity);
    }

    /**
     * モブの表示名を取得します
     *
     * @param entity エンティティ
     * @return 表示名
     */
    public String getMobDisplayName(Entity entity) {
        return mythicMobsHook.getMobDisplayName(entity);
    }

    /**
     * モブのレベルを取得します
     *
     * @param entity エンティティ
     * @return モブレベル
     */
    public double getMobLevel(Entity entity) {
        return mythicMobsHook.getMobLevel(entity);
    }

    /**
     * モブをスポーンさせます
     *
     * @param mobTypeId モブタイプID
     * @param location スポーン位置
     * @param level モブレベル
     * @return スポーンしたActiveMobのOptional
     */
    public Optional<ActiveMob> spawnMob(String mobTypeId, Location location, double level) {
        return mythicMobsHook.spawnMob(mobTypeId, location, level);
    }

    /**
     * モブのドロップを有効化/無効化します
     *
     * @param entity エンティティ
     * @param enabled trueで有効、falseで無効
     */
    public void setDropsEnabled(Entity entity, boolean enabled) {
        mythicMobsHook.setDropsEnabled(entity, enabled);
    }

    /**
     * モブを削除します
     *
     * @param entity エンティティ
     * @return 成功した場合はtrue
     */
    public boolean removeMob(Entity entity) {
        return mythicMobsHook.removeMob(entity);
    }

    /**
     * モブのターゲットを設定します
     *
     * @param entity エンティティ
     * @param target ターゲットプレイヤー
     */
    public void setTarget(Entity entity, Player target) {
        mythicMobsHook.setTarget(entity, target);
    }

    /**
     * ドロップハンドラーを取得します
     *
     * @return ドロップハンドラー
     */
    public DropHandler getDropHandler() {
        return dropHandler;
    }

    /**
     * MythicMobsが利用可能か確認します
     *
     * @return 利用可能な場合はtrue
     */
    public boolean isAvailable() {
        return mythicMobsHook.isAvailable();
    }

    /**
     * MythicMobsのバージョンを取得します
     *
     * @return バージョン文字列
     */
    public String getVersion() {
        return mythicMobsHook.getVersion();
    }

    /**
     * 期限切れドロップのクリーンアップを行います
     */
    public void cleanupExpiredDrops() {
        dropHandler.cleanupExpiredDrops();
    }

    /**
     * マネージャーをクリーンアップします
     *
     * <p>プラグイン無効化時に呼び出します。</p>
     */
    public void cleanup() {
        dropConfigs.clear();
        logger.info("MythicMobs Manager cleaned up");
    }

    /**
     * ドロップ設定を取得します
     *
     * @param mobId モブID
     * @return モブドロップ設定のOptional
     */
    public Optional<MobDropConfig.MobDrop> getDropConfig(String mobId) {
        return Optional.ofNullable(dropConfigs.get(mobId));
    }

    /**
     * 全ドロップ設定を取得します
     *
     * @return モブドロップ設定マップ
     */
    public Map<String, MobDropConfig.MobDrop> getAllDropConfigs() {
        return Collections.unmodifiableMap(dropConfigs);
    }

    /**
     * MythicMobsフックを取得します
     *
     * @return MythicMobsフック
     */
    public MythicMobsHook getHook() {
        return mythicMobsHook;
    }
}
