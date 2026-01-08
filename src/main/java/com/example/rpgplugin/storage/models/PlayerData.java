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

    // ==================== MP/HP関連フィールド ====================

    private int maxHealth;        // 最大HP修飾子
    private int maxMana;          // 最大MP
    private int currentMana;      // 現在MP
    private String costType;      // コストタイプ（"mana" or "hp"）

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
        // MP/HP関連フィールドの初期化
        this.maxHealth = 20;
        this.maxMana = 100;
        this.currentMana = 100;
        this.costType = "mana";
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
        // MP/HP関連フィールドのデフォルト値（旧データ互換）
        this.maxHealth = 20;
        this.maxMana = 100;
        this.currentMana = 100;
        this.costType = "mana";
    }

    /**
     * 全フィールドを含む完全コンストラクタ
     *
     * @param uuid プレイヤーUUID
     * @param username ユーザー名
     * @param classId クラスID
     * @param classRank クラスランク
     * @param classHistory クラス履歴
     * @param firstJoin 初回参加日時
     * @param lastLogin 最終ログイン日時
     * @param maxHealth 最大HP修飾子
     * @param maxMana 最大MP
     * @param currentMana 現在MP
     * @param costType コストタイプ
     */
    public PlayerData(UUID uuid, String username, String classId, int classRank, String classHistory,
                      long firstJoin, long lastLogin, int maxHealth, int maxMana, int currentMana, String costType) {
        this.uuid = uuid;
        this.username = username;
        this.classId = classId;
        this.classRank = classRank;
        this.classHistory = classHistory;
        this.firstJoin = firstJoin;
        this.lastLogin = lastLogin;
        this.maxHealth = maxHealth;
        this.maxMana = maxMana;
        this.currentMana = currentMana;
        this.costType = costType != null ? costType : "mana";
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

    // ==================== MP/HP関連アクセサ ====================

    /**
     * 最大HP修飾子を取得します
     *
     * @return 最大HP修飾子
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * 最大HP修飾子を設定します
     *
     * @param maxHealth 最大HP修飾子
     */
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = Math.max(0, maxHealth);
    }

    /**
     * 最大MPを取得します
     *
     * @return 最大MP
     */
    public int getMaxMana() {
        return maxMana;
    }

    /**
     * 最大MPを設定します
     *
     * @param maxMana 最大MP
     */
    public void setMaxMana(int maxMana) {
        this.maxMana = Math.max(0, maxMana);
        // 現在MPが最大MPを超える場合は調整
        if (this.currentMana > this.maxMana) {
            this.currentMana = this.maxMana;
        }
    }

    /**
     * 現在MPを取得します
     *
     * @return 現在MP
     */
    public int getCurrentMana() {
        return currentMana;
    }

    /**
     * 現在MPを設定します
     *
     * @param currentMana 現在MP
     */
    public void setCurrentMana(int currentMana) {
        this.currentMana = Math.min(Math.max(0, currentMana), maxMana);
    }

    /**
     * MPを追加します
     *
     * @param amount 追加するMP量
     * @return 実際に追加されたMP量
     */
    public int addMana(int amount) {
        if (amount <= 0) {
            return 0;
        }
        int current = getCurrentMana();
        int max = getMaxMana();
        int actualAdd = Math.min(amount, max - current);
        setCurrentMana(current + actualAdd);
        return actualAdd;
    }

    /**
     * MPを消費します
     *
     * @param amount 消費するMP量
     * @return 消費に成功した場合はtrue、MP不足の場合はfalse
     */
    public boolean consumeMana(int amount) {
        if (amount <= 0) {
            return true;
        }
        int current = getCurrentMana();
        if (current < amount) {
            return false;
        }
        setCurrentMana(current - amount);
        return true;
    }

    /**
     * コストタイプを取得します
     *
     * @return コストタイプ（"mana" or "hp"）
     */
    public String getCostType() {
        return costType;
    }

    /**
     * コストタイプを設定します
     *
     * @param costType コストタイプ（"mana" or "hp"）
     */
    public void setCostType(String costType) {
        this.costType = (costType != null && (costType.equalsIgnoreCase("hp") || costType.equalsIgnoreCase("health")))
                ? "hp" : "mana";
    }

    /**
     * コストタイプがMPか確認します
     *
     * @return MP消費モードの場合はtrue
     */
    public boolean isManaCostType() {
        return "mana".equalsIgnoreCase(costType);
    }

    /**
     * コストタイプを切り替えます
     */
    public void toggleCostType() {
        this.costType = isManaCostType() ? "hp" : "mana";
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
                ", maxHealth=" + maxHealth +
                ", maxMana=" + maxMana +
                ", currentMana=" + currentMana +
                ", costType='" + costType + '\'' +
                '}';
    }
}
