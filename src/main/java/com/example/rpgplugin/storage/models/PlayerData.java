package com.example.rpgplugin.storage.models;

import java.util.UUID;

/**
 * プレイヤーデータモデル
 */
public class PlayerData {

    private final UUID uuid;
    private String username;
    private String classId;
    private int classRank;
    private String classHistory; // クラス履歴（JSONカンマ区切り: "class1,class2,class3"）
    private final long firstJoin;
    private long lastLogin;

    /**
     * コンストラクタ
     *
     * @param uuid プレイヤーUUID
     * @param username ユーザー名
     */
    public PlayerData(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.classId = null;
        this.classRank = 1;
        this.classHistory = null;
        this.firstJoin = System.currentTimeMillis();
        this.lastLogin = System.currentTimeMillis();
    }

    /**
     * データベースからロードするための完全コンストラクタ
     */
    public PlayerData(UUID uuid, String username, String classId, int classRank, String classHistory, long firstJoin, long lastLogin) {
        this.uuid = uuid;
        this.username = username;
        this.classId = classId;
        this.classRank = classRank;
        this.classHistory = classHistory;
        this.firstJoin = firstJoin;
        this.lastLogin = lastLogin;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public int getClassRank() {
        return classRank;
    }

    public void setClassRank(int classRank) {
        this.classRank = classRank;
    }

    public String getClassHistory() {
        return classHistory;
    }

    public void setClassHistory(String classHistory) {
        this.classHistory = classHistory;
    }

    /**
     * クラス履歴をリストとして取得
     *
     * @return クラスIDのリスト
     */
    public java.util.List<String> getClassHistoryList() {
        if (classHistory == null || classHistory.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return java.util.Arrays.asList(classHistory.split(","));
    }

    /**
     * クラス履歴をリストとして設定
     *
     * @param history クラスIDのリスト
     */
    public void setClassHistoryList(java.util.List<String> history) {
        if (history == null || history.isEmpty()) {
            this.classHistory = null;
        } else {
            this.classHistory = String.join(",", history);
        }
    }

    /**
     * クラス履歴に追加
     *
     * @param classId クラスID
     */
    public void addClassToHistory(String classId) {
        java.util.List<String> history = new java.util.ArrayList<>(getClassHistoryList());
        if (!history.contains(classId)) {
            history.add(classId);
            setClassHistoryList(history);
        }
    }

    public long getFirstJoin() {
        return firstJoin;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    /**
     * 最終ログイン時刻を現在時刻に更新
     */
    public void updateLastLogin() {
        this.lastLogin = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerData that = (PlayerData) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "uuid=" + uuid +
                ", username='" + username + '\'' +
                ", classId='" + classId + '\'' +
                ", classRank=" + classRank +
                ", classHistory='" + classHistory + '\'' +
                ", firstJoin=" + firstJoin +
                ", lastLogin=" + lastLogin +
                '}';
    }
}
