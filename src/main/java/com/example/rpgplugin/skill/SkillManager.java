package com.example.rpgplugin.skill;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.evaluator.FormulaEvaluator;
import com.example.rpgplugin.skill.event.SkillEventListener;
import com.example.rpgplugin.skill.repository.PlayerSkillService;
import com.example.rpgplugin.skill.repository.SkillExecutor;
import com.example.rpgplugin.skill.repository.SkillRepository;
import com.example.rpgplugin.skill.result.SkillExecutionResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * スキル管理クラス（ファサード）
 *
 * <p>スキル関連の機能を統合して提供します。</p>
 * <p>内部処理は以下のクラスに委譲されます:</p>
 * <ul>
 *   <li>{@link SkillRepository} - スキルデータ管理</li>
 *   <li>{@link PlayerSkillService} - プレイヤースキルデータ管理</li>
 *   <li>{@link SkillExecutor} - スキル実行ロジック</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ファサードとしての調整役</li>
 *   <li>SOLID-O: プラグイン可能なアーキテクチャ</li>
 *   <li>Facade: シンプルなAPI、複雑な実装を隠蔽</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 2.0.0
 */
public class SkillManager implements SkillEventListener {

    private static final Logger LOGGER = Logger.getLogger(SkillManager.class.getName());

    private final RPGPlugin plugin;
    private final PlayerManager playerManager;
    private final SkillRepository skillRepository;
    private final PlayerSkillService playerSkillService;
    private final SkillExecutor skillExecutor;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     * @param playerManager プレイヤーマネージャー
     */
    public SkillManager(RPGPlugin plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.skillRepository = new SkillRepository();
        this.playerSkillService = new PlayerSkillService();
        this.skillExecutor = new SkillExecutor(skillRepository, playerSkillService, playerManager);
    }

    // ==================== スキル登録・取得 ====================

    /**
     * スキルを登録します
     *
     * <p>登録時にスキルツリーレジストリにも追加します。</p>
     *
     * @param skill 登録するスキル
     * @return 重複がある場合はfalse
     */
    public boolean registerSkill(Skill skill) {
        return skillRepository.registerSkill(skill);
    }

    /**
     * 既存のスキルを更新します
     *
     * <p>スキルが存在しない場合は登録します。</p>
     *
     * @param skill 更新するスキル
     * @return 成功した場合はtrue
     */
    public boolean updateSkill(Skill skill) {
        return skillRepository.updateSkill(skill);
    }

    /**
     * スキルを取得します
     *
     * @param skillId スキルID
     * @return スキル、見つからない場合はnull
     */
    public Skill getSkill(String skillId) {
        return skillRepository.getSkill(skillId);
    }

    /**
     * 全スキルを取得します
     *
     * @return 全スキルのマップ（コピー）
     */
    public Map<String, Skill> getAllSkills() {
        return skillRepository.getAllSkills();
    }

    /**
     * 全スキルIDを取得します
     *
     * @return スキルIDのセット
     */
    public Set<String> getAllSkillIds() {
        return skillRepository.getAllSkillIds();
    }

    /**
     * 指定されたクラスで使用可能なスキルを取得します
     *
     * @param classId クラスID
     * @return 使用可能なスキルリスト
     */
    public List<Skill> getSkillsForClass(String classId) {
        return skillRepository.getSkillsForClass(classId);
    }

    /**
     * スキルツリーレジストリを取得します
     *
     * @return スキルツリーレジストリ
     */
    public SkillTreeRegistry getTreeRegistry() {
        return skillRepository.getTreeRegistry();
    }

    /**
     * 指定されたクラスのスキルツリーを取得します
     *
     * @param classId クラスID
     * @return スキルツリー
     */
    public SkillTree getSkillTree(String classId) {
        return skillRepository.getSkillTree(classId);
    }

    /**
     * 全スキルデータをクリアします
     */
    public void clearAllSkills() {
        skillRepository.clearAllSkills();
    }

    // ==================== プレイヤースキルデータ ====================

    /**
     * プレイヤーのスキルデータを取得します
     *
     * @param player プレイヤー
     * @return プレイヤーのスキルデータ
     */
    public PlayerSkillData getPlayerSkillData(Player player) {
        PlayerSkillService.PlayerSkillData data = playerSkillService.getPlayerSkillData(player);
        return new PlayerSkillData(data);
    }

    /**
     * プレイヤーのスキルデータを取得します
     *
     * @param uuid プレイヤーUUID
     * @return プレイヤーのスキルデータ
     */
    public PlayerSkillData getPlayerSkillData(UUID uuid) {
        PlayerSkillService.PlayerSkillData data = playerSkillService.getPlayerSkillData(uuid);
        return new PlayerSkillData(data);
    }

    /**
     * プレイヤーがスキルを習得しているかチェックします
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return 習得している場合はtrue
     */
    public boolean hasSkill(Player player, String skillId) {
        return playerSkillService.hasSkill(player, skillId);
    }

    /**
     * プレイヤーのスキルレベルを取得します
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return スキルレベル（習得していない場合は0）
     */
    public int getSkillLevel(Player player, String skillId) {
        return playerSkillService.getSkillLevel(player, skillId);
    }

    /**
     * スキルを習得させます
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @param level レベル
     * @return 成功した場合はtrue
     */
    public boolean acquireSkill(Player player, String skillId, int level) {
        Skill skill = skillRepository.getSkill(skillId);
        if (skill == null) {
            player.sendMessage(Component.text("スキルが見つかりません: " + skillId, NamedTextColor.RED));
            return false;
        }
        return playerSkillService.acquireSkill(player, skillId, level, skill.getMaxLevel(), skill.getColoredDisplayName());
    }

    /**
     * スキルレベルを上げます
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return 成功した場合はtrue
     */
    public boolean upgradeSkill(Player player, String skillId) {
        Skill skill = skillRepository.getSkill(skillId);
        if (skill == null) {
            player.sendMessage(Component.text("スキルが見つかりません: " + skillId, NamedTextColor.RED));
            return false;
        }
        return playerSkillService.upgradeSkill(player, skillId, skill.getMaxLevel(), skill.getColoredDisplayName());
    }

    /**
     * プレイヤーデータをアンロードします
     *
     * @param uuid プレイヤーUUID
     */
    public void unloadPlayerData(UUID uuid) {
        playerSkillService.unloadPlayerData(uuid);
    }

    /**
     * 全プレイヤーデータをクリアします
     */
    public void clearAllPlayerData() {
        playerSkillService.clearAllPlayerData();
    }

    // ==================== スキル実行 ====================

    /**
     * スキルを実行します
     *
     * @param player 発動者
     * @param skillId スキルID
     * @param config 実行設定
     * @return 実行結果
     */
    public SkillExecutionResult executeSkill(Player player, String skillId, SkillExecutionConfig config) {
        return skillExecutor.executeSkill(player, skillId, config);
    }

    /**
     * スキルを実行します（デフォルト設定）
     *
     * @param player 発動者
     * @param skillId スキルID
     * @return 実行結果
     */
    public SkillExecutionResult executeSkill(Player player, String skillId) {
        return skillExecutor.executeSkill(player, skillId);
    }

    /**
     * ダメージを計算します
     *
     * @param skill スキル
     * @param rpgPlayer RPGプレイヤー
     * @param skillLevel スキルレベル
     * @return 計算されたダメージ
     */
    public double calculateDamage(Skill skill, RPGPlayer rpgPlayer, int skillLevel) {
        return skillExecutor.calculateDamage(skill, rpgPlayer, skillLevel);
    }

    /**
     * ダメージを計算します（カスタム変数付き）
     *
     * @param skill スキル
     * @param rpgPlayer RPGプレイヤー
     * @param skillLevel スキルレベル
     * @param customVariables カスタム変数
     * @return 計算されたダメージ
     */
    public double calculateDamage(Skill skill, RPGPlayer rpgPlayer, int skillLevel,
                                   Map<String, Double> customVariables) {
        return skillExecutor.calculateDamage(skill, rpgPlayer, skillLevel, customVariables);
    }

    /**
     * ダメージを計算します（数式エバリュエーター使用）
     *
     * @param formula ダメージ数式
     * @param rpgPlayer RPGプレイヤー
     * @param skillLevel スキルレベル
     * @return 計算されたダメージ
     */
    public double calculateDamageWithFormula(String formula, RPGPlayer rpgPlayer, int skillLevel) {
        return skillExecutor.calculateDamageWithFormula(formula, rpgPlayer, skillLevel);
    }

    /**
     * ダメージを計算します（数式エバリュエーター使用、カスタム変数付き）
     *
     * @param formula ダメージ数式
     * @param rpgPlayer RPGプレイヤー
     * @param skillLevel スキルレベル
     * @param customVariables カスタム変数
     * @return 計算されたダメージ
     */
    public double calculateDamageWithFormula(String formula, RPGPlayer rpgPlayer, int skillLevel,
                                              Map<String, Double> customVariables) {
        return skillExecutor.calculateDamageWithFormula(formula, rpgPlayer, skillLevel, customVariables);
    }

    /**
     * コストを消費します
     *
     * @param rpgPlayer RPGプレイヤー
     * @param cost コスト値
     * @param costType コストタイプ
     * @return 消費結果
     */
    public CostConsumptionResult consumeCost(RPGPlayer rpgPlayer, int cost, SkillCostType costType) {
        return new CostConsumptionResult(skillExecutor.consumeCost(rpgPlayer, cost, costType));
    }

    /**
     * クールダウンをチェックします
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return クールダウン中でない場合はtrue
     */
    public boolean checkCooldown(Player player, String skillId) {
        Skill skill = skillRepository.getSkill(skillId);
        if (skill == null) {
            return false;
        }
        return skillExecutor.checkCooldown(player, skillId, skill);
    }

    /**
     * ターゲットを選択します
     *
     * @param player 発動者
     * @param skill スキル
     * @param config 実行設定
     * @return ターゲットリスト
     */
    public List<LivingEntity> selectTargets(Player player, Skill skill, SkillExecutionConfig config) {
        return skillExecutor.selectTargets(player, skill, config);
    }

    /**
     * 効果を適用します
     *
     * @param target ターゲットエンティティ
     * @param damage ダメージ
     * @param skill スキル
     */
    public void applyEffect(LivingEntity target, double damage, Skill skill) {
        skillExecutor.applyEffect(target, damage, skill);
    }

    // ==================== ユーティリティ ====================

    /**
     * FormulaEvaluatorを取得します
     *
     * @return 数式エバリュエーター
     */
    public FormulaEvaluator getFormulaEvaluator() {
        return skillExecutor.getFormulaEvaluator();
    }

    /**
     * PlayerManagerを取得します
     *
     * @return プレイヤーマネージャー
     */
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * スキルをリロードし、削除されたスキルのプレイヤーデータをクリーンアップ
     *
     * <p>このメソッドは以下の処理を行います:</p>
     * <ul>
     *   <li>現在のスキルと新しいスキルを比較</li>
     *   <li>削除されたスキルを検出</li>
     *   <li>削除されたスキルを解放しているプレイヤーのスキルデータを削除</li>
     *   <li>新しいスキルマップを適用</li>
     *   <li>スキルツリーキャッシュを無効化</li>
     * </ul>
     *
     * @param newSkills 新しいスキルマップ
     * @return リロード結果（削除されたスキル数、影響を受けたプレイヤー数）
     */
    public ReloadResult reloadWithCleanup(Map<String, Skill> newSkills) {
        SkillRepository.ReloadResult repoResult = skillRepository.reloadSkills(newSkills);

        int affectedPlayers = 0;
        int totalSkillsRemoved = 0;

        if (!repoResult.getRemovedSkills().isEmpty()) {
            LOGGER.info(() -> "Detected " + repoResult.getRemovedSkills().size() + " removed skills: " + repoResult.getRemovedSkills());

            PlayerSkillService.CleanupSummary summary = playerSkillService.cleanupRemovedSkills(repoResult.getRemovedSkills());

            LOGGER.warning(() -> "Removed skills: " + repoResult.getRemovedSkills() +
                    " (affected " + summary.getAffectedPlayers() + " players, " + summary.getTotalSkillsRemoved() + " skill entries)");

            affectedPlayers = summary.getAffectedPlayers();
            totalSkillsRemoved = summary.getTotalSkillsRemoved();
        }

        return new ReloadResult(repoResult.getLoadedSkillCount(), repoResult.getRemovedSkills(),
                affectedPlayers, totalSkillsRemoved);
    }

    // ==================== SkillEventListener 実装 ====================

    @Override
    public void onSkillAcquired(UUID playerUuid, String skillId, int level) {
        PlayerSkillService.PlayerSkillData data = playerSkillService.getPlayerSkillData(playerUuid);
        data.setSkillLevel(skillId, level);
        LOGGER.fine(() -> "Skill acquired: " + playerUuid + " -> " + skillId + " level " + level);
    }

    @Override
    public void onSkillLevelUp(UUID playerUuid, String skillId, int newLevel, int previousLevel) {
        PlayerSkillService.PlayerSkillData data = playerSkillService.getPlayerSkillData(playerUuid);
        data.setSkillLevel(skillId, newLevel);
        LOGGER.fine(() -> "Skill level up: " + playerUuid + " -> " + skillId + " " + previousLevel + "->" + newLevel);
    }

    @Override
    public void onSkillExecuted(UUID playerUuid, String skillId, int level) {
        // クールダウン設定
        PlayerSkillService.PlayerSkillData data = playerSkillService.getPlayerSkillData(playerUuid);
        Skill skill = skillRepository.getSkill(skillId);
        if (skill != null) {
            data.setCooldown(skillId, System.currentTimeMillis());
        }
        LOGGER.fine(() -> "Skill executed: " + playerUuid + " -> " + skillId + " level " + level);
    }

    @Override
    public boolean canAcquireSkill(UUID playerUuid, String skillId) {
        Skill skill = skillRepository.getSkill(skillId);
        if (skill == null) {
            return false;
        }

        PlayerSkillService.PlayerSkillData data = playerSkillService.getPlayerSkillData(playerUuid);
        int currentLevel = data.getSkillLevel(skillId);

        // 既に最大レベルの場合は習得不可
        if (skill.getMaxLevel() > 0 && currentLevel >= skill.getMaxLevel()) {
            return false;
        }

        // 必要スキルのチェック
        for (String requiredSkill : skill.getRequiredSkills()) {
            if (data.getSkillLevel(requiredSkill) == 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int getSkillLevel(UUID playerUuid, String skillId) {
        return playerSkillService.getSkillLevel(playerUuid, skillId);
    }

    @Override
    public boolean hasSkill(UUID playerUuid, String skillId) {
        return playerSkillService.hasSkill(playerUuid, skillId);
    }

    /**
     * プレイヤーにスキルイベントリスナーを登録します
     *
     * <p>循環依存解消のため、RPGPlayerにこのインスタンスをイベントリスナーとして登録します。</p>
     *
     * @param rpgPlayer RPGプレイヤー
     */
    public void registerPlayerListener(RPGPlayer rpgPlayer) {
        if (rpgPlayer != null) {
            rpgPlayer.setSkillEventListener(this);
        }
    }

    // ==================== 内部クラス（後方互換性） ====================

    /**
     * プレイヤーのスキルデータ（ラッパー）
     *
     * <p>後方互換性のために提供されます。</p>
     */
    public static class PlayerSkillData {
        private final com.example.rpgplugin.skill.repository.PlayerSkillService.PlayerSkillData delegate;

        PlayerSkillData(com.example.rpgplugin.skill.repository.PlayerSkillService.PlayerSkillData delegate) {
            this.delegate = delegate;
        }

        public Map<String, Integer> getAcquiredSkills() {
            return delegate.getAcquiredSkills();
        }

        public int getSkillLevel(String skillId) {
            return delegate.getSkillLevel(skillId);
        }

        public boolean hasSkill(String skillId) {
            return delegate.hasSkill(skillId);
        }

        public void setSkillLevel(String skillId, int level) {
            delegate.setSkillLevel(skillId, level);
        }

        public void removeSkill(String skillId) {
            delegate.removeSkill(skillId);
        }

        public Map<String, Long> getCooldowns() {
            return delegate.getCooldowns();
        }

        public long getLastCastTime(String skillId) {
            return delegate.getLastCastTime(skillId);
        }

        public void setLastCastTime(String skillId, long time) {
            delegate.setLastCastTime(skillId, time);
        }

        public void setCooldown(String skillId, long time) {
            delegate.setCooldown(skillId, time);
        }

        public int getSkillPoints() {
            return delegate.getSkillPoints();
        }

        public void setSkillPoints(int points) {
            delegate.setSkillPoints(points);
        }

        public void addSkillPoints(int points) {
            delegate.addSkillPoints(points);
        }

        public boolean useSkillPoint() {
            return delegate.useSkillPoint();
        }
    }

    /**
     * コスト消費結果（後方互換性）
     */
    public static class CostConsumptionResult {
        private final SkillExecutor.CostConsumptionResult delegate;

        CostConsumptionResult(SkillExecutor.CostConsumptionResult delegate) {
            this.delegate = delegate;
        }

        public CostConsumptionResult(boolean success, String errorMessage, double consumedAmount) {
            this.delegate = new SkillExecutor.CostConsumptionResult(success, errorMessage, consumedAmount);
        }

        public boolean isSuccess() {
            return delegate.isSuccess();
        }

        public String getErrorMessage() {
            return delegate.getErrorMessage();
        }

        public double getConsumedAmount() {
            return delegate.getConsumedAmount();
        }
    }

    /**
     * スキルリロード結果
     */
    public static class ReloadResult {
        private final int loadedSkillCount;
        private final Set<String> removedSkills;
        private final int affectedPlayerCount;
        private final int totalSkillsRemoved;

        public ReloadResult(int loadedSkillCount, Set<String> removedSkills,
                           int affectedPlayerCount, int totalSkillsRemoved) {
            this.loadedSkillCount = loadedSkillCount;
            this.removedSkills = new HashSet<>(removedSkills);
            this.affectedPlayerCount = affectedPlayerCount;
            this.totalSkillsRemoved = totalSkillsRemoved;
        }

        public int getLoadedSkillCount() {
            return loadedSkillCount;
        }

        public Set<String> getRemovedSkills() {
            return new HashSet<>(removedSkills);
        }

        public int getAffectedPlayerCount() {
            return affectedPlayerCount;
        }

        public int getTotalSkillsRemoved() {
            return totalSkillsRemoved;
        }

        public boolean hasRemovedSkills() {
            return !removedSkills.isEmpty();
        }

        @Override
        public String toString() {
            return "ReloadResult{" +
                    "loaded=" + loadedSkillCount +
                    ", removed=" + removedSkills.size() +
                    ", affected=" + affectedPlayerCount +
                    ", totalSkillsRemoved=" + totalSkillsRemoved +
                    '}';
        }
    }
}
