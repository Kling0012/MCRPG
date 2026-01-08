package com.example.rpgplugin.player;

import com.example.rpgplugin.stats.ManaManager;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import com.example.rpgplugin.storage.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Optional;
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
 * @see ManaManager
 * @see PlayerData
 */
public class RPGPlayer {

    private final UUID uuid;
    private final String username;
    private final PlayerData playerData;
    private final StatManager statManager;
    private final ManaManager manaManager;
    private boolean isOnline;

    // ==================== ターゲット管理 ====================
    /** 最後にターゲットしたエンティティ */
    private Entity lastTargetedEntity;

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

        // PlayerDataから値を取得してManaManagerを初期化
        ManaManager.CostType costType = ManaManager.CostType.fromId(playerData.getCostType());
        this.manaManager = new ManaManager(
            uuid,
            playerData.getMaxMana(),
            playerData.getCurrentMana(),
            playerData.getMaxHealth(),
            costType
        );

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

    // ==================== ManaManager委譲 ====================

    /**
     * マナマネージャーを取得します
     *
     * @return マナマネージャー
     */
    public ManaManager getManaManager() {
        return manaManager;
    }

    /**
     * 最大HP修飾子を取得します
     *
     * @return 最大HP修飾子
     */
    public int getMaxHealthModifier() {
        return manaManager.getMaxHpModifier();
    }

    /**
     * 最大HP修飾子を設定します
     *
     * @param modifier 最大HP修飾子
     */
    public void setMaxHealthModifier(int modifier) {
        manaManager.setMaxHpModifier(modifier);
        playerData.setMaxHealth(modifier);
        // オンラインの場合はBukkitのHPも更新
        if (isOnline()) {
            Player player = getBukkitPlayer();
            if (player != null) {
                double baseMaxHealth = 20.0;
                double newMaxHealth = baseMaxHealth + modifier;
                player.setMaxHealth(newMaxHealth);
            }
        }
    }

    /**
     * 最大MPを取得します
     *
     * @return 最大MP
     */
    public int getMaxMana() {
        return manaManager.getMaxMana();
    }

    /**
     * 最大MPを設定します
     *
     * @param maxMana 最大MP
     */
    public void setMaxMana(int maxMana) {
        manaManager.setMaxMana(maxMana);
        playerData.setMaxMana(maxMana);
    }

    /**
     * 現在MPを取得します
     *
     * @return 現在MP
     */
    public int getCurrentMana() {
        return manaManager.getCurrentMana();
    }

    /**
     * 現在MPを設定します
     *
     * @param currentMana 現在MP
     */
    public void setCurrentMana(int currentMana) {
        manaManager.setCurrentMana(currentMana);
        playerData.setCurrentMana(currentMana);
    }

    /**
     * MPを追加します
     *
     * @param amount 追加するMP量
     * @return 実際に追加されたMP量
     */
    public int addMana(int amount) {
        int actualAdd = manaManager.addMana(amount);
        playerData.setCurrentMana(manaManager.getCurrentMana());
        return actualAdd;
    }

    /**
     * MPを消費します
     *
     * @param amount 消費するMP量
     * @return 消費に成功した場合はtrue、MP不足の場合はfalse
     */
    public boolean consumeMana(int amount) {
        boolean success = manaManager.consumeMana(amount);
        if (success) {
            playerData.setCurrentMana(manaManager.getCurrentMana());
        }
        return success;
    }

    /**
     * MPが足りているか確認します
     *
     * @param amount 必要なMP量
     * @return MPが足りている場合はtrue
     */
    public boolean hasMana(int amount) {
        return manaManager.hasMana(amount);
    }

    /**
     * MPが満タンか確認します
     *
     * @return 満タンの場合はtrue
     */
    public boolean isFullMana() {
        return manaManager.isFullMana();
    }

    /**
     * MPが空か確認します
     *
     * @return 空の場合はtrue
     */
    public boolean isEmptyMana() {
        return manaManager.isEmptyMana();
    }

    /**
     * MPの割合を取得します
     *
     * @return MPの割合（0.0～1.0）
     */
    public double getManaRatio() {
        return manaManager.getManaRatio();
    }

    /**
     * MPを回復します
     *
     * <p>精神値（SPI）に基づいて回復量を計算します。</p>
     *
     * @param baseRegene 基礎回復量
     * @return 実際に回復したMP量
     */
    public int regenerateMana(double baseRegene) {
        int spiritValue = getFinalStat(Stat.SPIRIT);
        int actualRegen = manaManager.regenerateMana(spiritValue, baseRegene);
        playerData.setCurrentMana(manaManager.getCurrentMana());
        return actualRegen;
    }

    /**
     * コストタイプを取得します
     *
     * @return コストタイプ
     */
    public ManaManager.CostType getCostType() {
        return manaManager.getCostType();
    }

    /**
     * コストタイプを設定します
     *
     * @param costType コストタイプ
     */
    public void setCostType(ManaManager.CostType costType) {
        manaManager.setCostType(costType);
        playerData.setCostType(costType.getId());
    }

    /**
     * コストタイプがMPか確認します
     *
     * @return MP消費モードの場合はtrue
     */
    public boolean isManaCostType() {
        return manaManager.getCostType() == ManaManager.CostType.MANA;
    }

    /**
     * コストタイプを切り替えます
     *
     * @return 新しいコストタイプ
     */
    public ManaManager.CostType toggleCostType() {
        manaManager.toggleCostType();
        ManaManager.CostType newType = manaManager.getCostType();
        playerData.setCostType(newType.getId());
        return newType;
    }

    /**
     * スキル発動時のコストを消費します
     *
     * <p>現在のコストタイプに応じてMPまたはHPを消費します。</p>
     *
     * @param amount コスト量
     * @return 消費に成功した場合はtrue、リソース不足の場合はfalse
     */
    public boolean consumeSkillCost(int amount) {
        if (isManaCostType()) {
            return consumeMana(amount);
        } else {
            // HP消費モード
            if (isOnline()) {
                Player player = getBukkitPlayer();
                if (player != null) {
                    double currentHp = player.getHealth();
                    if (currentHp <= amount) {
                        return false;
                    }
                    player.setHealth(currentHp - amount);
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * MP情報をフォーマットして返します
     *
     * @return フォーマットされたMP情報
     */
    public String formatManaInfo() {
        return manaManager.formatManaInfo();
    }

    /**
     * ManaManagerの状態をPlayerDataに同期します
     *
     * <p>データベース保存前に呼び出して、メモリ上のMP状態をPlayerDataに反映します。</p>
     */
    public void syncManaToData() {
        playerData.setMaxMana(manaManager.getMaxMana());
        playerData.setCurrentMana(manaManager.getCurrentMana());
        playerData.setMaxHealth(manaManager.getMaxHpModifier());
        playerData.setCostType(manaManager.getCostType().getId());
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
     * プレイヤーのレベルを取得します
     *
     * <p>getVanillaLevel()のエイリアスです。スキルシステム等で使用されます。</p>
     *
     * @return レベル、オフラインの場合は0
     */
    public int getLevel() {
        return getVanillaLevel();
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

    // ==================== ターゲット管理 ====================

    /**
     * 最後にターゲットしたエンティティを取得します
     *
     * @return ターゲットエンティティ、存在しない場合はempty
     */
    public Optional<Entity> getLastTargetedEntity() {
        // エンティティが有効かチェック
        if (lastTargetedEntity != null && !lastTargetedEntity.isValid()) {
            lastTargetedEntity = null;
        }
        return Optional.ofNullable(lastTargetedEntity);
    }

    /**
     * ターゲットエンティティを設定します
     *
     * @param entity ターゲットとするエンティティ（nullでクリア）
     */
    public void setTargetedEntity(Entity entity) {
        this.lastTargetedEntity = entity;
    }

    /**
     * ターゲットをクリアします
     */
    public void clearTarget() {
        this.lastTargetedEntity = null;
    }

    // ==================== スキル関連 ====================

    /**
     * スキルマネージャーへの参照（オプション）
     *
     * <p>スキル関連メソッドを使用するために設定されます。</p>
     */
    private volatile com.example.rpgplugin.skill.SkillManager skillManager;

    /**
     * スキルマネージャーを設定します
     *
     * @param skillManager スキルマネージャー
     */
    public void setSkillManager(com.example.rpgplugin.skill.SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    /**
     * スキルを習得しているか確認します
     *
     * @param skillId スキルID
     * @return 習得している場合はtrue
     */
    public boolean hasSkill(String skillId) {
        if (skillManager == null) {
            return false;
        }
        org.bukkit.entity.Player player = getBukkitPlayer();
        if (player == null) {
            return false;
        }
        return skillManager.hasSkill(player, skillId);
    }

    /**
     * スキルレベルを取得します
     *
     * @param skillId スキルID
     * @return スキルレベル、習得していない場合は0
     */
    public int getSkillLevel(String skillId) {
        if (skillManager == null) {
            return 0;
        }
        org.bukkit.entity.Player player = getBukkitPlayer();
        if (player == null) {
            return 0;
        }
        return skillManager.getSkillLevel(player, skillId);
    }

    /**
     * スキルを習得します
     *
     * @param skillId スキルID
     * @return 習得に成功した場合はtrue
     */
    public boolean acquireSkill(String skillId) {
        return acquireSkill(skillId, 1);
    }

    /**
     * スキルを習得します
     *
     * @param skillId スキルID
     * @param level 習得するレベル
     * @return 習得に成功した場合はtrue
     */
    public boolean acquireSkill(String skillId, int level) {
        if (skillManager == null) {
            return false;
        }
        org.bukkit.entity.Player player = getBukkitPlayer();
        if (player == null) {
            return false;
        }
        return skillManager.acquireSkill(player, skillId, level);
    }

    /**
     * スキルをレベルアップします
     *
     * @param skillId スキルID
     * @return レベルアップに成功した場合はtrue
     */
    public boolean upgradeSkill(String skillId) {
        if (skillManager == null) {
            return false;
        }
        org.bukkit.entity.Player player = getBukkitPlayer();
        if (player == null) {
            return false;
        }
        return skillManager.upgradeSkill(player, skillId);
    }
}
