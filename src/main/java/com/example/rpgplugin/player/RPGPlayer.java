package com.example.rpgplugin.player;

import com.example.rpgplugin.player.data.PlayerDataContainer;
import com.example.rpgplugin.player.manager.PlayerSkillManager;
import com.example.rpgplugin.player.manager.PlayerStatManager;
import com.example.rpgplugin.skill.event.SkillEventListener;
import com.example.rpgplugin.stats.ManaManager;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import com.example.rpgplugin.storage.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * プレイヤーのRPGデータを管理するファサードクラス
 *
 * <p>BukkitプレイヤーとRPGシステムを橋渡しし、各マネージャーへの統一的アクセスを提供します。</p>
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>ファサードパターン: 複雑なサブシステムへの簡潔なインターフェース</li>
 *   <li>委譲パターン: 各マネージャーに処理を委譲</li>
 *   <li>Compositeパターン: 複数のマネージャーを統合</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ファサードとして統一インターフェース提供</li>
 *   <li>DRY: 重複する委譲ロジックを削除</li>
 *   <li>KISS: シンプルなAPI設計</li>
 * </ul>
 *
 * <p>スレッド安全性:</p>
 * <ul>
 *   <li>StatManagerはスレッドセーフ</li>
 *   <li>PlayerDataへのアクセスは同期が必要</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 2.0.0
 * @see PlayerDataContainer
 * @see PlayerStatManager
 * @see PlayerSkillManager
 */
public class RPGPlayer {

    private final UUID uuid;
    private final String username;
    private final PlayerDataContainer dataContainer;
    private final PlayerStatManager statManager;
    private final PlayerSkillManager skillManager;
    private final EntityTargetManager targetManager;

    /**
     * コンストラクタ
     *
     * @param playerData プレイヤーデータ
     * @param statManager ステータスマネージャー
     */
    public RPGPlayer(PlayerData playerData, StatManager statManager) {
        this.uuid = playerData.getUuid();
        this.username = playerData.getUsername();

        // サブシステムを初期化
        this.dataContainer = new PlayerDataContainer(playerData);

        // ManaManagerを初期化
        ManaManager.CostType costType = ManaManager.CostType.fromId(playerData.getCostType());
        ManaManager manaManager = new ManaManager(
            uuid,
            playerData.getMaxMana(),
            playerData.getCurrentMana(),
            playerData.getMaxHealth(),
            costType
        );

        // PlayerStatManagerを初期化
        this.statManager = new PlayerStatManager(
            uuid,
            this.dataContainer,
            statManager,
            manaManager,
            () -> Bukkit.getPlayer(uuid)
        );

        // PlayerSkillManagerを初期化
        this.skillManager = new PlayerSkillManager(uuid);

        // ターゲットマネージャーを初期化
        this.targetManager = new EntityTargetManager();
    }

    // ==================== 基本情報 ====================

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
        return player != null && player.isOnline();
    }

    // ==================== PlayerDataContainer委譲 ====================

    /**
     * プレイヤーデータを取得します
     *
     * @return プレイヤーデータ
     */
    public PlayerData getPlayerData() {
        return dataContainer.getPlayerData();
    }

    /**
     * データコンテナを取得します
     *
     * @return データコンテナ
     */
    public PlayerDataContainer getDataContainer() {
        return dataContainer;
    }

    /**
     * クラスIDを取得します
     *
     * @return クラスID、未設定の場合はnull
     */
    public String getClassId() {
        return dataContainer.getClassId();
    }

    /**
     * クラスIDを設定します
     *
     * @param classId クラスID
     */
    public void setClassId(String classId) {
        dataContainer.setClassId(classId);
    }

    /**
     * クラスランクを取得します
     *
     * @return クラスランク
     */
    public int getClassRank() {
        return dataContainer.getClassRank();
    }

    /**
     * クラスランクを設定します
     *
     * @param rank クラスランク
     */
    public void setClassRank(int rank) {
        dataContainer.setClassRank(rank);
    }

    /**
     * クラス履歴を取得します
     *
     * @return クラスIDのリスト
     */
    public List<String> getClassHistory() {
        return dataContainer.getClassHistory();
    }

    /**
     * クラス履歴に追加します
     *
     * @param classId クラスID
     */
    public void addClassToHistory(String classId) {
        dataContainer.addClassToHistory(classId);
    }

    /**
     * クラス履歴をクリアします
     */
    public void clearClassHistory() {
        dataContainer.clearClassHistory();
    }

    /**
     * 初回参加日時を取得します
     *
     * @return 初回参加日時（エポックミリ秒）
     */
    public long getFirstJoin() {
        return dataContainer.getFirstJoin();
    }

    /**
     * 最終ログイン日時を取得します
     *
     * @return 最終ログイン日時（エポックミリ秒）
     */
    public long getLastLogin() {
        return dataContainer.getLastLogin();
    }

    /**
     * 最終ログイン日時を更新します
     */
    public void updateLastLogin() {
        dataContainer.updateLastLogin();
    }

    // ==================== PlayerStatManager委譲 ====================

    /**
     * ステータスマネージャーを取得します
     *
     * @return ステータスマネージャー
     */
    public StatManager getStatManager() {
        return statManager.getStatManager();
    }

    /**
     * ステータスマネージャーを取得します
     *
     * @return プレイヤーステータスマネージャー
     */
    public PlayerStatManager getPlayerStatManager() {
        return statManager;
    }

    /**
     * マナマネージャーを取得します
     *
     * @return マナマネージャー
     */
    public ManaManager getManaManager() {
        return statManager.getManaManager();
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

    // ==================== Mana管理メソッド ====================

    /**
     * 最大HP修飾子を取得します
     *
     * @return 最大HP修飾子
     */
    public int getMaxHealthModifier() {
        return statManager.getMaxHealthModifier();
    }

    /**
     * 最大HP修飾子を設定します
     *
     * @param modifier 最大HP修飾子
     */
    public void setMaxHealthModifier(int modifier) {
        statManager.setMaxHealthModifier(modifier);
    }

    /**
     * 最大MPを取得します
     *
     * @return 最大MP
     */
    public int getMaxMana() {
        return statManager.getMaxMana();
    }

    /**
     * 最大MPを設定します
     *
     * @param maxMana 最大MP
     */
    public void setMaxMana(int maxMana) {
        statManager.setMaxMana(maxMana);
    }

    /**
     * 現在MPを取得します
     *
     * @return 現在MP
     */
    public int getCurrentMana() {
        return statManager.getCurrentMana();
    }

    /**
     * 現在MPを設定します
     *
     * @param currentMana 現在MP
     */
    public void setCurrentMana(int currentMana) {
        statManager.setCurrentMana(currentMana);
    }

    /**
     * MPを追加します
     *
     * @param amount 追加するMP量
     * @return 実際に追加されたMP量
     */
    public int addMana(int amount) {
        return statManager.addMana(amount);
    }

    /**
     * MPを消費します
     *
     * @param amount 消費するMP量
     * @return 消費に成功した場合はtrue、MP不足の場合はfalse
     */
    public boolean consumeMana(int amount) {
        return statManager.consumeMana(amount);
    }

    /**
     * MPが足りているか確認します
     *
     * @param amount 必要なMP量
     * @return MPが足りている場合はtrue
     */
    public boolean hasMana(int amount) {
        return statManager.hasMana(amount);
    }

    /**
     * MPが満タンか確認します
     *
     * @return 満タンの場合はtrue
     */
    public boolean isFullMana() {
        return statManager.isFullMana();
    }

    /**
     * MPが空か確認します
     *
     * @return 空の場合はtrue
     */
    public boolean isEmptyMana() {
        return statManager.isEmptyMana();
    }

    /**
     * MPの割合を取得します
     *
     * @return MPの割合（0.0～1.0）
     */
    public double getManaRatio() {
        return statManager.getManaRatio();
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
        return statManager.regenerateMana(baseRegene);
    }

    /**
     * コストタイプを取得します
     *
     * @return コストタイプ
     */
    public ManaManager.CostType getCostType() {
        return statManager.getCostType();
    }

    /**
     * コストタイプを設定します
     *
     * @param costType コストタイプ
     */
    public void setCostType(ManaManager.CostType costType) {
        statManager.setCostType(costType);
    }

    /**
     * コストタイプがMPか確認します
     *
     * @return MP消費モードの場合はtrue
     */
    public boolean isManaCostType() {
        return statManager.isManaCostType();
    }

    /**
     * コストタイプを切り替えます
     *
     * @return 新しいコストタイプ
     */
    public ManaManager.CostType toggleCostType() {
        return statManager.toggleCostType();
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
        return statManager.consumeSkillCost(amount);
    }

    /**
     * MP情報をフォーマットして返します
     *
     * @return フォーマットされたMP情報
     */
    public String formatManaInfo() {
        return statManager.formatManaInfo();
    }

    /**
     * ManaManagerの状態をPlayerDataに同期します
     *
     * <p>データベース保存前に呼び出して、メモリ上のMP状態をPlayerDataに反映します。</p>
     */
    public void syncManaToData() {
        statManager.syncManaToData();
    }

    /**
     * ステータス情報をフォーマットして返します
     *
     * @return フォーマットされたステータス情報
     */
    public String formatStats() {
        return statManager.formatStats();
    }

    // ==================== PlayerSkillManager委譲 ====================

    /**
     * スキルマネージャーを取得します
     *
     * @return プレイヤースキルマネージャー
     */
    public PlayerSkillManager getPlayerSkillManager() {
        return skillManager;
    }

    /**
     * スキルイベントリスナーを設定します
     *
     * @param listener スキルイベントリスナー
     */
    public void setSkillEventListener(SkillEventListener listener) {
        skillManager.setSkillEventListener(listener);
    }

    /**
     * スキルを習得しているか確認します
     *
     * @param skillId スキルID
     * @return 習得している場合はtrue
     */
    public boolean hasSkill(String skillId) {
        return skillManager.hasSkill(skillId);
    }

    /**
     * スキルレベルを取得します
     *
     * @param skillId スキルID
     * @return スキルレベル、習得していない場合は0
     */
    public int getSkillLevel(String skillId) {
        return skillManager.getSkillLevel(skillId);
    }

    /**
     * スキルを習得します
     *
     * @param skillId スキルID
     * @return 習得に成功した場合はtrue
     */
    public boolean acquireSkill(String skillId) {
        return skillManager.acquireSkill(skillId);
    }

    /**
     * スキルを習得します
     *
     * <p>イベントを発行し、リスナーが実際の習得処理を行います。</p>
     *
     * @param skillId スキルID
     * @param level 習得するレベル
     * @return 習得に成功した場合はtrue
     */
    public boolean acquireSkill(String skillId, int level) {
        return skillManager.acquireSkill(skillId, level);
    }

    /**
     * スキルをレベルアップします
     *
     * @param skillId スキルID
     * @return レベルアップに成功した場合はtrue
     */
    public boolean upgradeSkill(String skillId) {
        return skillManager.upgradeSkill(skillId);
    }

    /**
     * スキルを実行します
     *
     * @param skillId スキルID
     */
    public void executeSkill(String skillId) {
        skillManager.executeSkill(skillId);
    }

    /**
     * スキルを実行します（Playerパラメータ付き）
     *
     * <p>Skript等の外部システムからスキルを実行する場合に使用します。</p>
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return 実行結果
     */
    public com.example.rpgplugin.skill.result.SkillExecutionResult executeSkill(Player player, String skillId) {
        return skillManager.executeSkill(player, skillId);
    }

    /**
     * スキルを実行します（設定付き）
     *
     * <p>Skript等の外部システムからスキルを実行する場合に使用します。</p>
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @param config 実行設定
     * @return 実行結果
     */
    public com.example.rpgplugin.skill.result.SkillExecutionResult executeSkill(
            Player player,
            String skillId,
            com.example.rpgplugin.skill.SkillExecutionConfig config) {
        return skillManager.executeSkill(player, skillId, config);
    }

    // ==================== ターゲット管理 ====================

    /**
     * 最後にターゲットしたエンティティを取得します
     *
     * @return ターゲットエンティティ、存在しない場合はempty
     */
    public Optional<Entity> getLastTargetedEntity() {
        return targetManager.getLastTargetedEntity();
    }

    /**
     * ターゲットエンティティを設定します
     *
     * @param entity ターゲットとするエンティティ（nullでクリア）
     */
    public void setTargetedEntity(Entity entity) {
        targetManager.setTargetedEntity(entity);
    }

    /**
     * ターゲットをクリアします
     */
    public void clearTarget() {
        targetManager.clearTarget();
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

    // ==================== 内部クラス ====================

    /**
     * エンティティターゲット管理クラス
     *
     * <p>ターゲットエンティティの管理を担当します。</p>
     */
    private static class EntityTargetManager {

        private Entity lastTargetedEntity;

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
    }
}
