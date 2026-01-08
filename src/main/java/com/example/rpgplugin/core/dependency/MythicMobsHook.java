package com.example.rpgplugin.core.dependency;

import io.lumine.mythic.api.MythicPlugin;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;

/**
 * MythicMobs連携クラス
 *
 * <p>MythicMobsプラグイン（最新版API）との連携を提供します。
 * モブのドロップ管理、スポナー制御、AI操作などの機能を提供します。</p>
 *
 * <p>対応バージョン: MythicMobs 5.0+ (Lumine Utilsベース)</p>
 *
 * <p>主な機能:</p>
 * <ul>
 *   <li>モブスポーン/デスポーン管理</li>
 *   <li>ドロップアイテム制御（倒した人のみ）</li>
 *   <li>モブステータス取得・変更</li>
 *   <li>モブターゲット操作</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class MythicMobsHook {

    private final JavaPlugin plugin;
    private MythicBukkit mythicBukkit;
    private MythicPlugin mythicPlugin;
    private boolean available = false;
    private String version;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public MythicMobsHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * MythicMobsのセットアップを行います
     *
     * <p>MythicMobsのインスタンスを取得し、利用可能か確認します。
     * 最新版API（io.lumine.mythic.*）を使用します。</p>
     *
     * @return セットアップに成功した場合はtrue
     */
    public boolean setup() {
        try {
            // MythicBukkitインスタンスの取得
            mythicBukkit = MythicBukkit.inst();
            if (mythicBukkit == null) {
                plugin.getLogger().warning("Failed to get MythicBukkit instance.");
                return false;
            }

            // バージョン情報の取得
            version = mythicBukkit.getVersion();
            available = true;

            plugin.getLogger().info("MythicMobs hooked successfully!");
            plugin.getLogger().info("Version: " + version);

            return true;

        } catch (NoClassDefFoundError e) {
            plugin.getLogger().warning("MythicMobs classes not found. Is MythicMobs installed?");
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook MythicMobs: " + e.getMessage());
            return false;
        }
    }

    /**
     * MythicMobsが利用可能か確認します
     *
     * @return 利用可能な場合はtrue
     */
    public boolean isAvailable() {
        return available && mythicBukkit != null && mythicPlugin != null;
    }

    /**
     * MythicBukkitインスタンスを取得します
     *
     * @return MythicBukkitインスタンス、利用不可能な場合はnull
     */
    public MythicBukkit getMythicBukkit() {
        return mythicBukkit;
    }

    /**
     * MythicPluginインスタンスを取得します
     *
     * @return MythicPluginインスタンス、利用不可能な場合はnull
     */
    public MythicPlugin getMythicPlugin() {
        return mythicPlugin;
    }

    /**
     * MythicMobsのバージョンを取得します
     *
     * @return バージョン文字列
     */
    public String getVersion() {
        return version != null ? version : "Unknown";
    }

    /**
     * エンティティがMythicMobかどうかを確認します
     *
     * @param entity チェックするエンティティ
     * @return MythicMobの場合はtrue
     */
    public boolean isMythicMob(Entity entity) {
        if (!isAvailable()) {
            return false;
        }

        try {
            return mythicBukkit.getMobManager()
                    .isActiveMob(BukkitAdapter.adapt(entity));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check if entity is MythicMob: " + e.getMessage());
            return false;
        }
    }

    /**
     * アクティブなMythicMobを取得します
     *
     * @param entity エンティティ
     * @return ActiveMobのOptional、MythicMobでない場合は空
     */
    public Optional<ActiveMob> getActiveMob(Entity entity) {
        if (!isAvailable()) {
            return Optional.empty();
        }

        try {
            // MythicMobs 5.6+でAPIが変更されたため、UUIDを直接取得
            UUID uuid = entity.getUniqueId();
            return mythicBukkit.getMobManager().getActiveMob(uuid);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get ActiveMob: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * MythicMobの内部IDを取得します
     *
     * @param entity エンティティ
     * @return 内部ID、MythicMobでない場合はnull
     */
    public String getMobTypeId(Entity entity) {
        Optional<ActiveMob> activeMob = getActiveMob(entity);
        return activeMob.map(mob -> mob.getMobType().toString()).orElse(null);
    }

    /**
     * MythicMobの表示名を取得します
     *
     * @param entity エンティティ
     * @return 表示名、MythicMobでない場合はnull
     */
    public String getMobDisplayName(Entity entity) {
        Optional<ActiveMob> activeMob = getActiveMob(entity);
        return activeMob.map(mob -> mob.getMobType().toString()).orElse(null);
    }

    /**
     * モブのレベルを取得します
     *
     * @param entity エンティティ
     * @return モブレベル、MythicMobでない場合は-1
     */
    public double getMobLevel(Entity entity) {
        Optional<ActiveMob> activeMob = getActiveMob(entity);
        return activeMob.map(ActiveMob::getLevel).orElse(-1.0);
    }

    /**
     * モブのHPを取得します
     *
     * @param entity エンティティ
     * @return 現在のHP、MythicMobでない場合は-1
     */
    public double getMobHealth(Entity entity) {
        Optional<ActiveMob> activeMob = getActiveMob(entity);
        return activeMob.map(mob -> mob.getEntity().getHealth()).orElse(-1.0);
    }

    /**
     * モブの最大HPを取得します
     *
     * @param entity エンティティ
     * @return 最大HP、MythicMobでない場合は-1
     */
    public double getMobMaxHealth(Entity entity) {
        Optional<ActiveMob> activeMob = getActiveMob(entity);
        return activeMob.map(mob -> mob.getEntity().getMaxHealth()).orElse(-1.0);
    }

    /**
     * モブをスポーンさせます
     *
     * @param mobTypeId モブタイプID
     * @param location スポーン位置
     * @param level モブレベル
     * @return スポーンしたActiveMobのOptional、失敗した場合は空
     */
    public Optional<ActiveMob> spawnMob(String mobTypeId, org.bukkit.Location location, double level) {
        if (!isAvailable()) {
            return Optional.empty();
        }

        try {
            Optional<MythicMob> mythicMob = mythicBukkit.getMobManager()
                    .getMythicMob(mobTypeId);

            if (mythicMob.isEmpty()) {
                plugin.getLogger().warning("MythicMob not found: " + mobTypeId);
                return Optional.empty();
            }

            ActiveMob spawnedMob = mythicMob.get().spawn(BukkitAdapter.adapt(location), level);
            return Optional.ofNullable(spawnedMob);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn MythicMob: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * モブのドロップを有効化/無効化します
     *
     * <p>倒した人のみドロップさせる機能の実装に使用します。</p>
     * <p>※ MythicMobs 5.6+でAPIが変更されたため、現在は未実装です</p>
     *
     * @param entity エンティティ
     * @param enabled trueで有効、falseで無効
     */
    public void setDropsEnabled(Entity entity, boolean enabled) {
        // MythicMobs 5.6+でドロップ制御APIが変更されたため未実装
        // ドロップ制御はMythicDeathListener側で実装済み
        plugin.getLogger().fine("setDropsEnabled is not implemented. Drop control is handled by MythicDeathListener.");
    }

    /**
     * モブのターゲットを設定します
     *
     * @param entity エンティティ
     * @param target ターゲットプレイヤー
     */
    public void setTarget(Entity entity, Player target) {
        Optional<ActiveMob> activeMob = getActiveMob(entity);
        activeMob.ifPresent(mob -> {
            mob.setTarget(BukkitAdapter.adapt(target));
        });
    }

    /**
     * モブを削除します
     *
     * @param entity エンティティ
     * @return 成功した場合はtrue
     */
    public boolean removeMob(Entity entity) {
        Optional<ActiveMob> activeMob = getActiveMob(entity);
        if (activeMob.isPresent()) {
            activeMob.get().remove();
            return true;
        }
        return false;
    }

    /**
     * フックをクリーンアップします
     *
     * <p>プラグイン無効化時に呼び出します。</p>
     */
    public void cleanup() {
        mythicBukkit = null;
        mythicPlugin = null;
        available = false;
        version = null;
    }
}
