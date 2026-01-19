package com.example.rpgplugin.player.data;

import com.example.rpgplugin.storage.models.PlayerData;

import java.util.UUID;
import java.util.List;

/**
 * プレイヤーデータコンテナ
 *
 * <p>プレイヤーの永続化データへのアクセスを提供します。</p>
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>Data Container: データ保持に特化</li>
 *   <li>Value Object: 不変的なデータアクセス</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: データアクセスに特化</li>
 *   <li>DRY: データアクセスロジックを一元管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 2.0.0
 */
public class PlayerDataContainer {

    private final PlayerData playerData;

    /**
     * コンストラクタ
     *
     * @param playerData プレイヤーデータ
     */
    public PlayerDataContainer(PlayerData playerData) {
        this.playerData = playerData;
    }

    /**
     * プレイヤーデータを取得します
     *
     * @return プレイヤーデータ
     */
    public PlayerData getPlayerData() {
        return playerData;
    }

    /**
     * UUIDを取得します
     *
     * @return プレイヤーUUID
     */
    public UUID getUuid() {
        return playerData.getUuid();
    }

    /**
     * ユーザー名を取得します
     *
     * @return ユーザー名
     */
    public String getUsername() {
        return playerData.getUsername();
    }

    // ==================== クラス関連 ====================

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
    public List<String> getClassHistory() {
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

    // ==================== 時刻関連 ====================

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

    // ==================== MP/HPデータ同期 ====================

    /**
     * 最大MPを設定します
     *
     * @param maxMana 最大MP
     */
    public void setMaxMana(int maxMana) {
        playerData.setMaxMana(maxMana);
    }

    /**
     * 現在MPを設定します
     *
     * @param currentMana 現在MP
     */
    public void setCurrentMana(int currentMana) {
        playerData.setCurrentMana(currentMana);
    }

    /**
     * 最大HP修飾子を設定します
     *
     * @param maxHealth 最大HP修飾子
     */
    public void setMaxHealth(int maxHealth) {
        playerData.setMaxHealth(maxHealth);
    }

    /**
     * コストタイプを設定します
     *
     * @param costTypeId コストタイプID
     */
    public void setCostType(String costTypeId) {
        playerData.setCostType(costTypeId);
    }

    /**
     * 最大MPを取得します
     *
     * @return 最大MP
     */
    public int getMaxMana() {
        return playerData.getMaxMana();
    }

    /**
     * 現在MPを取得します
     *
     * @return 現在MP
     */
    public int getCurrentMana() {
        return playerData.getCurrentMana();
    }

    /**
     * 最大HP修飾子を取得します
     *
     * @return 最大HP修飾子
     */
    public int getMaxHealth() {
        return playerData.getMaxHealth();
    }

    /**
     * コストタイプIDを取得します
     *
     * @return コストタイプID
     */
    public String getCostTypeId() {
        return playerData.getCostType();
    }
}
