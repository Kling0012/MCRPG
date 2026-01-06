package com.example.rpgplugin.player;

import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import com.example.rpgplugin.storage.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * プレイヤーのRPGデータを管理するラッパークラス
 *
 * <p>BukkitプレイヤーとRPGシステムを橋渡しし、ステータス、クラス、レベルなどを管理します。</p>
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>ファサードパターン: プレイヤーRPG操作への統一的インターフェース</li>
 *   <li>Proxyパターン: PlayerDataとStatManagerへのアクセスを仲介</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: プレイヤーRPGデータ管理に特化</li>
 *   <li>DRY: データアクセスロジックを一元管理</li>
 * </ul>
 *
 * <p>スレッド安全性:</p>
 * <ul>
 *   <li>StatManagerはスレッドセーフ</li>
 *   <li>PlayerDataへのアクセスは同期が必要</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 * @see StatManager
 * @see PlayerData
 */
public class RPGPlayer {

    private final UUID uuid;
    private final String username;
    private final PlayerData playerData;
    private final StatManager statManager;
    private boolean isOnline;

    /**
     * コンストラクタ
     *
     * @param playerData プレイヤーデータ
     * @param statManager ステータスマネージャー
     */
    public RPGPlayer(PlayerData playerData, StatManager statManager) {
        this.uuid = playerData.getUuid();
        this.username = playerData.getUsername();
        this.playerData = playerData;
        this.statManager = statManager;
        this.isOnline = Bukkit.getPlayer(uuid) != null;
    }

    /**
     * UUIDを取得します
     *
     * @return プレイヤーUUID
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * ユーザー名を取得します
     *
     * @return ユーザー名
     */
    public String getUsername() {
        return username;
    }

    /**
     * Bukkitプレイヤーを取得します
     *
     * @return Bukkitプレイヤー、オフラインの場合はnull
     */
    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * オンラインか確認します
     *
     * @return オンラインの場合はtrue
     */
    public boolean isOnline() {
        Player player = Bukkit.getPlayer(uuid);
        this.isOnline = player != null && player.isOnline();
        return isOnline;
    }

    // ==================== PlayerData委譲 ====================

    /**
     * プレイヤーデータを取得します
     *
     * @return プレイヤーデータ
     */
    public PlayerData getPlayerData() {
        return playerData;
    }

    /**
     * クラスIDを取得します
     *
     * @return クラスID、未設定の場合はnull
     */
    public String getClassId() {
        return playerData.getClassId();
    }

    /**
     * クラスIDを設定します
     *
     * @param classId クラスID
     */
    public void setClassId(String classId) {
        playerData.setClassId(classId);
    }

    /**
     * クラスランクを取得します
     *
     * @return クラスランク
     */
    public int getClassRank() {
        return playerData.getClassRank();
    }

    /**
     * クラスランクを設定します
     *
     * @param rank クラスランク
     */
    public void setClassRank(int rank) {
        playerData.setClassRank(rank);
    }

    /**
     * クラス履歴を取得します
     *
     * @return クラスIDのリスト
     */
    public java.util.List<String> getClassHistory() {
        return playerData.getClassHistoryList();
    }

    /**
     * クラス履歴に追加します
     *
     * @param classId クラスID
     */
    public void addClassToHistory(String classId) {
        playerData.addClassToHistory(classId);
    }

    /**
     * クラス履歴をクリアします
     */
    public void clearClassHistory() {
        playerData.setClassHistory(null);
    }

    /**
     * 初回参加日時を取得します
     *
     * @return 初回参加日時（エポックミリ秒）
     */
    public long getFirstJoin() {
        return playerData.getFirstJoin();
    }

    /**
     * 最終ログイン日時を取得します
     *
     * @return 最終ログイン日時（エポックミリ秒）
     */
    public long getLastLogin() {
        return playerData.getLastLogin();
    }

    /**
     * 最終ログイン日時を更新します
     */
    public void updateLastLogin() {
        playerData.updateLastLogin();
    }

    // ==================== StatManager委譲 ====================

    /**
     * ステータスマネージャーを取得します
     *
     * @return ステータスマネージャー
     */
    public StatManager getStatManager() {
        return statManager;
    }

    /**
     * 基本ステータスを取得します
     *
     * @param stat ステータス種別
     * @return 基本値
     */
    public int getBaseStat(Stat stat) {
        return statManager.getBaseStat(stat);
    }

    /**
     * 基本ステータスを設定します
     *
     * @param stat ステータス種別
     * @param value 基本値
     */
    public void setBaseStat(Stat stat, int value) {
        statManager.setBaseStat(stat, value);
    }

    /**
     * 最終ステータスを取得します
     *
     * @param stat ステータス種別
     * @return 最終値
     */
    public int getFinalStat(Stat stat) {
        return statManager.getFinalStat(stat);
    }

    /**
     * 手動配分ポイントを取得します
     *
     * @return 手動配分ポイント
     */
    public int getAvailablePoints() {
        return statManager.getAvailablePoints();
    }

    /**
     * 手動配分ポイントを設定します
     *
     * @param points 手動配分ポイント
     */
    public void setAvailablePoints(int points) {
        statManager.setAvailablePoints(points);
    }

    /**
     * 手動配分ポイントを追加します
     *
     * @param amount 追加するポイント数
     */
    public void addAvailablePoints(int amount) {
        statManager.addAvailablePoints(amount);
    }

    /**
     * ステータスに手動配分ポイントを割り振ります
     *
     * @param stat ステータス種別
     * @param amount 割り振るポイント数
     * @return 割り振りに成功した場合はtrue
     */
    public boolean allocatePoint(Stat stat, int amount) {
        return statManager.allocatePoint(stat, amount);
    }

    /**
     * ステータス配分をリセットします
     *
     * @return 返却されたポイント数
     */
    public int resetAllocation() {
        return statManager.resetAllocation();
    }

    // ==================== ユーティリティ ====================

    /**
     * プレイヤーにメッセージを送信します
     *
     * <p>オンラインの場合のみ送信されます。</p>
     *
     * @param message メッセージ
     */
    public void sendMessage(String message) {
        if (isOnline()) {
            Player player = getBukkitPlayer();
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    /**
     * プレイヤーのバニラレベルを取得します
     *
     * @return バニラレベル、オフラインの場合は0
     */
    public int getVanillaLevel() {
        if (isOnline()) {
            Player player = getBukkitPlayer();
            if (player != null) {
                return player.getLevel();
            }
        }
        return 0;
    }

    /**
     * ステータス情報をフォーマットして返します
     *
     * @return フォーマットされたステータス情報
     */
    public String formatStats() {
        return statManager.formatStats();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RPGPlayer rpgPlayer = (RPGPlayer) o;
        return uuid.equals(rpgPlayer.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return "RPGPlayer{" +
                "uuid=" + uuid +
                ", username='" + username + '\'' +
                ", classId='" + getClassId() + '\'' +
                ", classRank=" + getClassRank() +
                ", isOnline=" + isOnline() +
                '}';
    }
}
