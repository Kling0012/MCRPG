package com.example.rpgplugin.player;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.stats.StatManager;
import com.example.rpgplugin.storage.models.PlayerData;
import com.example.rpgplugin.storage.repository.PlayerDataRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * プレイヤー管理クラス
 *
 * <p>オンラインプレイヤーを管理し、参加/退出イベントを処理します。
 * RPGPlayerのキャッシュを行い、データロード/セーブを担当します。</p>
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>ファサードパターン: プレイヤー管理への統一的インターフェース</li>
 *   <li>オブザーバーパターン: プレイヤー参加/退出イベントを監視</li>
 *   <li>キャッシュパターン: RPGPlayerをメモリ上にキャッシュ</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: プレイヤーライフサイクル管理に特化</li>
 *   <li>DRY: データアクセスロジックを一元管理</li>
 *   <li>KISS: シンプルなAPI設計</li>
 * </ul>
 *
 * <p>スレッド安全性:</p>
 * <ul>
 *   <li>ConcurrentHashMapを使用したスレッドセーフな実装</li>
 *   <li>外部同期なしで複数スレッドからアクセス可能</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 * @see RPGPlayer
 */
public class PlayerManager implements Listener {

    private final RPGPlugin plugin;
    private final Logger logger;
    private final PlayerDataRepository playerDataRepository;
    private final Map<UUID, RPGPlayer> onlinePlayers;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     * @param playerDataRepository プレイヤーデータリポジトリ
     */
    public PlayerManager(RPGPlugin plugin, PlayerDataRepository playerDataRepository) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.playerDataRepository = playerDataRepository;
        this.onlinePlayers = new ConcurrentHashMap<>();
    }

    // ==================== ライフサイクル ====================

    /**
     * プレイヤーマネージャーを初期化します
     *
     * <p>既にオンラインのプレイヤーをロードします。</p>
     */
    public void initialize() {
        logger.info("Initializing PlayerManager...");

        // 既にオンラインのプレイヤーをロード（リロード対策）
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                loadPlayer(player.getUniqueId());
                logger.fine("Loaded existing online player: " + player.getName());
            } catch (Exception e) {
                logger.warning("Failed to load player " + player.getName() + ": " + e.getMessage());
            }
        }

        logger.info("PlayerManager initialized. Online players: " + onlinePlayers.size());
    }

    /**
     * プレイヤーマネージャーをシャットダウンします
     *
     * <p>全オンラインプレイヤーのデータを保存します。</p>
     */
    public void shutdown() {
        logger.info("Shutting down PlayerManager...");

        int successCount = 0;
        int failCount = 0;

        for (RPGPlayer rpgPlayer : onlinePlayers.values()) {
            try {
                savePlayer(rpgPlayer.getUuid());
                successCount++;
            } catch (Exception e) {
                logger.warning("Failed to save player " + rpgPlayer.getUsername() + ": " + e.getMessage());
                failCount++;
            }
        }

        onlinePlayers.clear();

        logger.info("PlayerManager shutdown complete. Saved: " + successCount + ", Failed: " + failCount);
    }

    // ==================== イベントハンドラー ====================

    /**
     * プレイヤー参加イベント
     *
     * @param event プレイヤー参加イベント
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        try {
            loadPlayer(uuid);
            logger.fine("Player joined: " + player.getName());
        } catch (Exception e) {
            logger.severe("Failed to load player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * プレイヤー退出イベント
     *
     * @param event プレイヤー退出イベント
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        try {
            unloadPlayer(uuid);
            logger.fine("Player quit: " + player.getName());
        } catch (Exception e) {
            logger.severe("Failed to unload player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== プレイヤーロード/セーブ ====================

    /**
     * プレイヤーをロードします
     *
     * <p>データベースからプレイヤーデータをロードし、RPGPlayerを作成します。
     * データが存在しない場合は新規作成します。</p>
     *
     * @param uuid プレイヤーUUID
     * @return ロードされたRPGPlayer
     * @throws Exception ロードに失敗した場合
     */
    public RPGPlayer loadPlayer(UUID uuid) throws Exception {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }

        // 既にロード済みの場合は返す
        RPGPlayer existing = onlinePlayers.get(uuid);
        if (existing != null) {
            return existing;
        }

        // データベースからロード
        PlayerData playerData = playerDataRepository.findById(uuid.toString());
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) {
            throw new IllegalStateException("Player is not online: " + uuid);
        }

        // 新規プレイヤーの場合はデータを作成
        if (playerData == null) {
            playerData = new PlayerData(uuid, player.getName());
            playerDataRepository.save(playerData);
            logger.fine("Created new player data for: " + player.getName());
        } else {
            // ユーザー名を更新
            playerData.setUsername(player.getName());
            playerData.updateLastLogin();
        }

        // ステータスマネージャーを作成（初期値は10、手動配分ポイントは0）
        StatManager statManager = new StatManager(10, 0);

        // RPGPlayerを作成
        RPGPlayer rpgPlayer = new RPGPlayer(playerData, statManager);
        onlinePlayers.put(uuid, rpgPlayer);

        logger.fine("Loaded player: " + player.getName() + " (Class: " + playerData.getClassId() + ")");

        return rpgPlayer;
    }

    /**
     * プレイヤーをアンロードします
     *
     * <p>プレイヤーデータを保存し、キャッシュから削除します。</p>
     *
     * @param uuid プレイヤーUUID
     * @throws Exception アンロードに失敗した場合
     */
    public void unloadPlayer(UUID uuid) throws Exception {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }

        RPGPlayer rpgPlayer = onlinePlayers.remove(uuid);

        if (rpgPlayer != null) {
            savePlayer(uuid);

            // 通貨データもアンロード
            var currencyManager = plugin.getCurrencyManager();
            if (currencyManager != null) {
                currencyManager.unloadPlayerCurrency(uuid);
            }

            logger.fine("Unloaded player: " + rpgPlayer.getUsername());
        }
    }

    /**
     * プレイヤーデータを保存します
     *
     * @param uuid プレイヤーUUID
     * @throws Exception 保存に失敗した場合
     */
    public void savePlayer(UUID uuid) throws Exception {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }

        RPGPlayer rpgPlayer = onlinePlayers.get(uuid);

        if (rpgPlayer != null) {
            playerDataRepository.save(rpgPlayer.getPlayerData());
            logger.fine("Saved player data: " + rpgPlayer.getUsername());
        }
    }

    /**
     * 全プレイヤーデータを非同期に保存します
     *
     * <p>大量のプレイヤーデータを保存する場合に使用します。</p>
     */
    public void saveAllAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int successCount = 0;
            int failCount = 0;

            for (RPGPlayer rpgPlayer : onlinePlayers.values()) {
                try {
                    playerDataRepository.save(rpgPlayer.getPlayerData());
                    successCount++;
                } catch (Exception e) {
                    logger.warning("Failed to save player " + rpgPlayer.getUsername() + ": " + e.getMessage());
                    failCount++;
                }
            }

            logger.info("Async save complete. Saved: " + successCount + ", Failed: " + failCount);
        });
    }

    // ==================== プレイヤーアクセス ====================

    /**
     * 指定したUUIDのRPGPlayerを取得します
     *
     * @param uuid プレイヤーUUID
     * @return RPGPlayer、オフラインまたはロードされていない場合はnull
     */
    public RPGPlayer getRPGPlayer(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return onlinePlayers.get(uuid);
    }

    /**
     * 指定した名前のRPGPlayerを取得します
     *
     * @param name プレイヤー名
     * @return RPGPlayer、オフラインまたはロードされていない場合はnull
     */
    public RPGPlayer getRPGPlayer(String name) {
        if (name == null) {
            return null;
        }

        for (RPGPlayer rpgPlayer : onlinePlayers.values()) {
            if (rpgPlayer.getUsername().equalsIgnoreCase(name)) {
                return rpgPlayer;
            }
        }

        return null;
    }

    /**
     * オンラインのRPGPlayer数を取得します
     *
     * @return オンラインプレイヤー数
     */
    public int getOnlinePlayerCount() {
        return onlinePlayers.size();
    }

    /**
     * 全オンラインRPGPlayerを取得します
     *
     * @return オンラインRPGPlayerのマップ（コピー）
     */
    public Map<UUID, RPGPlayer> getOnlinePlayers() {
        return Map.copyOf(onlinePlayers);
    }

    /**
     * プレイヤーがオンラインか確認します
     *
     * @param uuid プレイヤーUUID
     * @return オンラインの場合はtrue
     */
    public boolean isOnline(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        return onlinePlayers.containsKey(uuid);
    }
}
