package com.example.rpgplugin.skill;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.skill.evaluator.FormulaDamageCalculator;
import com.example.rpgplugin.skill.evaluator.FormulaEvaluator;
import com.example.rpgplugin.skill.target.ShapeCalculator;
import com.example.rpgplugin.skill.target.TargetSelector;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * スキル管理クラス
 *
 * <p>全スキルの登録・取得、プレイヤーの習得スキル管理を行います。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキル管理に専念</li>
 *   <li>DRY: スキルアクセスロジックを一元管理</li>
 *   <li>Strategy: 異なるスキルタイプの実行をStrategyパターンで管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillManager {

    private static final Logger LOGGER = Logger.getLogger(SkillManager.class.getName());

    private final RPGPlugin plugin;
    private final Map<String, Skill> skills;
    private final Map<UUID, PlayerSkillData> playerSkills;
    private final PlayerManager playerManager;
    private final FormulaEvaluator formulaEvaluator;

    /**
     * スキル実行結果
     */
    public static class SkillExecutionResult {
        private final boolean success;
        private final String errorMessage;
        private final double damage;
        private final int targetsHit;
        private final double costConsumed;

        private SkillExecutionResult(boolean success, String errorMessage,
                                      double damage, int targetsHit, double costConsumed) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.damage = damage;
            this.targetsHit = targetsHit;
            this.costConsumed = costConsumed;
        }

        /**
         * 成功結果を作成します
         */
        public static SkillExecutionResult success() {
            return new SkillExecutionResult(true, null, 0, 0, 0);
        }

        /**
         * ダメージ付きの成功結果を作成します
         */
        public static SkillExecutionResult successWithDamage(double damage, int targetsHit) {
            return new SkillExecutionResult(true, null, damage, targetsHit, 0);
        }

        /**
         * コスト消費付きの成功結果を作成します
         */
        public static SkillExecutionResult successWithCost(double damage, int targetsHit, double costConsumed) {
            return new SkillExecutionResult(true, null, damage, targetsHit, costConsumed);
        }

        /**
         * 失敗結果を作成します
         */
        public static SkillExecutionResult failure(String errorMessage) {
            return new SkillExecutionResult(false, errorMessage, 0, 0, 0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public double getDamage() {
            return damage;
        }

        public int getTargetsHit() {
            return targetsHit;
        }

        public double getCostConsumed() {
            return costConsumed;
        }
    }

    /**
     * プレイヤーのスキルデータ
     */
    public static class PlayerSkillData {
        private final Map<String, Integer> acquiredSkills; // skillId -> level
        private final Map<String, Long> cooldowns; // skillId -> lastCastTime
        private int skillPoints;

        public PlayerSkillData() {
            this.acquiredSkills = new ConcurrentHashMap<>();
            this.cooldowns = new ConcurrentHashMap<>();
            this.skillPoints = 0;
        }

        public Map<String, Integer> getAcquiredSkills() {
            return new HashMap<>(acquiredSkills);
        }

        public int getSkillLevel(String skillId) {
            return acquiredSkills.getOrDefault(skillId, 0);
        }

        public boolean hasSkill(String skillId) {
            return acquiredSkills.containsKey(skillId) && acquiredSkills.get(skillId) > 0;
        }

        public void setSkillLevel(String skillId, int level) {
            if (level <= 0) {
                acquiredSkills.remove(skillId);
            } else {
                acquiredSkills.put(skillId, level);
            }
        }

        /**
         * スキルを削除します
         *
         * @param skillId スキルID
         */
        public void removeSkill(String skillId) {
            acquiredSkills.remove(skillId);
        }

        public Map<String, Long> getCooldowns() {
            return new HashMap<>(cooldowns);
        }

        public long getLastCastTime(String skillId) {
            return cooldowns.getOrDefault(skillId, 0L);
        }

        public void setLastCastTime(String skillId, long time) {
            cooldowns.put(skillId, time);
        }

        public int getSkillPoints() {
            return skillPoints;
        }

        public void setSkillPoints(int points) {
            this.skillPoints = Math.max(0, points);
        }

        public void addSkillPoints(int points) {
            this.skillPoints += points;
        }

        public boolean useSkillPoint() {
            if (skillPoints > 0) {
                skillPoints--;
                return true;
            }
            return false;
        }
    }

    /** スキルツリーレジストリ（Phase14: 自動更新対応） */
    private final SkillTreeRegistry treeRegistry;

    /**
 * コンストラクタ
 *
 * @param plugin プラグインインスタンス
 * @param playerManager プレイヤーマネージャー
 */
public SkillManager(RPGPlugin plugin, PlayerManager playerManager) {
    this.plugin = plugin;
    this.playerManager = playerManager;
    this.skills = new ConcurrentHashMap<>();
    this.playerSkills = new ConcurrentHashMap<>();
    this.formulaEvaluator = new FormulaEvaluator();
    this.treeRegistry = new SkillTreeRegistry();
}

    /**
 * スキルを登録します
 *
 * <p>登録時にスキルツリーレジストリにも追加し、ツリーの自動更新を有効にします。</p>
 *
 * @param skill 登録するスキル
 * @return 重複がある場合はfalse
 */
public boolean registerSkill(Skill skill) {
    if (skill == null || skill.getId() == null) {
        plugin.getLogger().warning("Cannot register null skill or skill with null ID");
        return false;
    }
    if (skills.containsKey(skill.getId())) {
        plugin.getLogger().warning("Skill already registered: " + skill.getId());
        return false;
    }
    skills.put(skill.getId(), skill);
    plugin.getLogger().info("Skill registered: " + skill.getId());

    // スキルツリーレジストリにも登録（ツリー自動更新対応）
    treeRegistry.registerSkill(skill);

    return true;
}

    /**
     * スキルツリーレジストリを取得します（Phase14: 自動更新対応）
     *
     * @return スキルツリーレジストリ
     */
    public SkillTreeRegistry getTreeRegistry() {
        return treeRegistry;
    }

    /**
     * 指定されたクラスのスキルツリーを取得します（Phase14: 自動更新対応）
     *
     * @param classId クラスID
     * @return スキルツリー
     */
    public SkillTree getSkillTree(String classId) {
        return treeRegistry.getTree(classId);
    }

    /**
     * スキルを取得します
     *
     * @param skillId スキルID
     * @return スキル、見つからない場合はnull
     */
    public Skill getSkill(String skillId) {
        return skills.get(skillId);
    }

    /**
     * 全スキルを取得します
     *
     * @return 全スキルのマップ（コピー）
     */
    public Map<String, Skill> getAllSkills() {
        return new HashMap<>(skills);
    }

    /**
     * 全スキルIDを取得します
     *
     * @return スキルIDのセット
     */
    public Set<String> getAllSkillIds() {
        return new HashSet<>(skills.keySet());
    }

    /**
     * 指定されたクラスで使用可能なスキルを取得します
     *
     * @param classId クラスID
     * @return 使用可能なスキルリスト
     */
    public List<Skill> getSkillsForClass(String classId) {
        List<Skill> result = new ArrayList<>();
        for (Skill skill : skills.values()) {
            if (skill.isAvailableForClass(classId)) {
                result.add(skill);
            }
        }
        return result;
    }

    /**
     * プレイヤーのスキルデータを取得します
     *
     * @param player プレイヤー
     * @return プレイヤーのスキルデータ
     */
    public PlayerSkillData getPlayerSkillData(Player player) {
        return playerSkills.computeIfAbsent(player.getUniqueId(), k -> new PlayerSkillData());
    }

    /**
     * プレイヤーのスキルデータを取得します
     *
     * @param uuid プレイヤーUUID
     * @return プレイヤーのスキルデータ
     */
    public PlayerSkillData getPlayerSkillData(UUID uuid) {
        return playerSkills.computeIfAbsent(uuid, k -> new PlayerSkillData());
    }

    /**
     * プレイヤーがスキルを習得しているかチェックします
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return 習得している場合はtrue
     */
    public boolean hasSkill(Player player, String skillId) {
        PlayerSkillData data = getPlayerSkillData(player);
        return data.hasSkill(skillId);
    }

    /**
     * プレイヤーのスキルレベルを取得します
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return スキルレベル（習得していない場合は0）
     */
    public int getSkillLevel(Player player, String skillId) {
        PlayerSkillData data = getPlayerSkillData(player);
        return data.getSkillLevel(skillId);
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
        Skill skill = getSkill(skillId);
        if (skill == null) {
            player.sendMessage(ChatColor.RED + "スキルが見つかりません: " + skillId);
            return false;
        }

        PlayerSkillData data = getPlayerSkillData(player);
        int currentLevel = data.getSkillLevel(skillId);

        if (currentLevel > 0 && level <= currentLevel) {
            player.sendMessage(ChatColor.RED + "既に higher level を習得しています");
            return false;
        }

        if (level > skill.getMaxLevel()) {
            player.sendMessage(ChatColor.RED + "最大レベルを超えています: " + skill.getMaxLevel());
            return false;
        }

        data.setSkillLevel(skillId, level);
        player.sendMessage(ChatColor.GREEN + "スキルを習得しました: " + skill.getColoredDisplayName() + " Lv." + level);
        return true;
    }

    /**
     * スキルレベルを上げます
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return 成功した場合はtrue
     */
    public boolean upgradeSkill(Player player, String skillId) {
        Skill skill = getSkill(skillId);
        if (skill == null) {
            player.sendMessage(ChatColor.RED + "スキルが見つかりません: " + skillId);
            return false;
        }

        PlayerSkillData data = getPlayerSkillData(player);
        int currentLevel = data.getSkillLevel(skillId);

        if (currentLevel == 0) {
            player.sendMessage(ChatColor.RED + "まずスキルを習得してください");
            return false;
        }

        if (currentLevel >= skill.getMaxLevel()) {
            player.sendMessage(ChatColor.RED + "既に最大レベルに達しています");
            return false;
        }

        data.setSkillLevel(skillId, currentLevel + 1);
        player.sendMessage(ChatColor.GREEN + "スキルを強化しました: " + skill.getColoredDisplayName() + " Lv." + (currentLevel + 1));
        return true;
    }

    /**
     * クールダウンをチェックします
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return クールダウン中でない場合はtrue
     */
    public boolean checkCooldown(Player player, String skillId) {
        Skill skill = getSkill(skillId);
        if (skill == null) {
            return false;
        }

        PlayerSkillData data = getPlayerSkillData(player);
        long lastCast = data.getLastCastTime(skillId);
        long currentTime = System.currentTimeMillis();
        long cooldownMs = (long) (skill.getCooldown() * 1000);

        if (currentTime - lastCast < cooldownMs) {
            long remainingSeconds = ((cooldownMs - (currentTime - lastCast)) / 1000) + 1;
            player.sendMessage(ChatColor.RED + "クールダウン中です: 残り " + remainingSeconds + " 秒");
            return false;
        }

        return true;
    }

    /**
     * プレイヤーデータをアンロードします
     *
     * @param uuid プレイヤーUUID
     */
    public void unloadPlayerData(UUID uuid) {
        playerSkills.remove(uuid);
    }

    /**
     * 全スキルデータをクリアします
     */
    public void clearAllSkills() {
        skills.clear();
    }

    /**
     * 全プレイヤーデータをクリアします
     */
    public void clearAllPlayerData() {
        playerSkills.clear();
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
        // スキル取得
        Skill skill = getSkill(skillId);
        if (skill == null) {
            return SkillExecutionResult.failure("スキルが見つかりません: " + skillId);
        }

        // 習得チェック
        if (!hasSkill(player, skillId)) {
            return SkillExecutionResult.failure("スキルを習得していません: " + skill.getColoredDisplayName());
        }

        // レベル取得
        int level = getSkillLevel(player, skillId);
        if (level <= 0) {
            return SkillExecutionResult.failure("スキルレベルが0です");
        }

        // アクティブスキルチェック
        if (!skill.isActive()) {
            return SkillExecutionResult.failure("このスキルはアクティブスキルではありません");
        }

        // クールダウンチェック
        if (config.shouldApplyCooldown() && !checkCooldown(player, skillId)) {
            return SkillExecutionResult.failure("クールダウン中です");
        }

        // RPGプレイヤー取得
        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            return SkillExecutionResult.failure("プレイヤーデータが読み込まれていません");
        }

        // コスト消費
        double costConsumed = 0.0;
        if (config.shouldConsumeCost()) {
            SkillCostType costType = config.getCostType() != null ? config.getCostType() : skill.getCostType();
            int cost = skill.getCost(level);

            CostConsumptionResult costResult = consumeCost(rpgPlayer, cost, costType);
            if (!costResult.isSuccess()) {
                return SkillExecutionResult.failure(costResult.getErrorMessage());
            }
            costConsumed = costResult.getConsumedAmount();
        }

        // ダメージ計算
        double damage = 0.0;
        if (config.shouldApplyDamage() && skill.getDamage() != null) {
            damage = calculateDamage(skill, rpgPlayer, level, config.getCustomVariables());
        }

        // ターゲット取得と効果適用
        int targetsHit = 0;
        if (config.shouldApplyDamage()) {
            List<LivingEntity> targets = selectTargets(player, skill, config);
            for (LivingEntity target : targets) {
                applyEffect(target, damage, skill);
                targetsHit++;
            }
        }

        // クールダウン設定
        if (config.shouldApplyCooldown()) {
            PlayerSkillData data = getPlayerSkillData(player);
            data.setLastCastTime(skillId, System.currentTimeMillis());
        }

        // 成功メッセージ
        if (targetsHit > 0) {
            player.sendMessage(ChatColor.GREEN + "スキルを発動しました: " + skill.getColoredDisplayName() + " Lv." + level
                    + ChatColor.GRAY + " (ダメージ: " + String.format("%.1f", damage) + ", ターゲット: " + targetsHit + ")");
        } else {
            player.sendMessage(ChatColor.GREEN + "スキルを発動しました: " + skill.getColoredDisplayName() + " Lv." + level);
        }

        return SkillExecutionResult.successWithCost(damage, targetsHit, costConsumed);
    }

    /**
     * スキルを実行します（デフォルト設定）
     *
     * @param player 発動者
     * @param skillId スキルID
     * @return 実行結果
     */
    public SkillExecutionResult executeSkill(Player player, String skillId) {
        return executeSkill(player, skillId, SkillExecutionConfig.createDefault());
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
        return calculateDamage(skill, rpgPlayer, skillLevel, null);
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
        Skill.DamageCalculation damageConfig = skill.getDamage();
        if (damageConfig == null) {
            return 0.0;
        }

        // レガシー計算方式（後方互換性維持）
        // ステータス値を取得
        var stat = damageConfig.getStatMultiplier();
        double statValue = 0.0;
        if (stat != null) {
            statValue = rpgPlayer.getStatManager().getFinalStat(stat);
        }

        // 既存の計算方式を使用
        return damageConfig.calculateDamage(statValue, skillLevel);
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
        return calculateDamageWithFormula(formula, rpgPlayer, skillLevel, null);
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
        if (formula == null || formula.trim().isEmpty()) {
            return 0.0;
        }

        try {
            return formulaEvaluator.evaluate(formula, rpgPlayer, skillLevel, customVariables);
        } catch (FormulaEvaluator.FormulaEvaluationException e) {
            LOGGER.warning("[SkillManager] ダメージ数式評価エラー: " + e.getMessage());
            return 0.0;
        }
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
        Player player = rpgPlayer.getBukkitPlayer();
        if (player == null) {
            return new CostConsumptionResult(false, "プレイヤーがオンラインではありません", 0);
        }

        switch (costType) {
            case MANA:
                // MP消費
                if (!rpgPlayer.hasMana(cost)) {
                    return new CostConsumptionResult(false, "MPが不足しています", 0);
                }
                rpgPlayer.consumeMana(cost);
                return new CostConsumptionResult(true, null, cost);

            case HP:
                // HP消費
                double currentHealth = player.getHealth();
                if (currentHealth <= cost) {
                    return new CostConsumptionResult(false, "HPが不足しています", 0);
                }
                player.setHealth(currentHealth - cost);
                return new CostConsumptionResult(true, null, cost);

            default:
                return new CostConsumptionResult(false, "未知のコストタイプ: " + costType, 0);
        }
    }

    /**
     * コスト消費結果
     */
    public static class CostConsumptionResult {
        private final boolean success;
        private final String errorMessage;
        private final double consumedAmount;

        public CostConsumptionResult(boolean success, String errorMessage, double consumedAmount) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.consumedAmount = consumedAmount;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public double getConsumedAmount() {
            return consumedAmount;
        }
    }

    /**
     * ターゲットを選択します
     *
     * <p>Phase11-4統合: SkillTargetが設定されている場合はTargetSelectorを使用します。</p>
     *
     * @param player 発動者
     * @param skill スキル
     * @param config 実行設定
     * @return ターゲットリスト
     */
    public List<LivingEntity> selectTargets(Player player, Skill skill, SkillExecutionConfig config) {
        // Phase11-4: SkillTargetが設定されている場合は、TargetSelectorを使用
        if (skill.hasSkillTarget()) {
            return selectTargetsWithSkillTarget(player, skill, config);
        }

        // レガシー/Phase11-6: 従来のターゲット選択ロジック
        List<LivingEntity> targets = new ArrayList<>();

        // 単体ターゲット指定
        if (config.getTargetEntity() instanceof LivingEntity) {
            targets.add((LivingEntity) config.getTargetEntity());
            return targets;
        }

        // 範囲ターゲット選択
        Location center = config.getTargetLocation() != null ? config.getTargetLocation() : player.getLocation();
        SkillExecutionConfig.RangeConfig rangeConfig = config.getRangeConfig();

        if (rangeConfig == null) {
            // デフォルト範囲（球形、半径5ブロック）
            rangeConfig = SkillExecutionConfig.RangeConfig.sphere(5.0);
        }

        Collection<Entity> nearbyEntities;
        if (rangeConfig.isSpherical()) {
            double radius = rangeConfig.getRangeX();
            nearbyEntities = player.getWorld().getNearbyEntities(center, radius, radius, radius);
        } else {
            nearbyEntities = player.getWorld().getNearbyEntities(
                    center, rangeConfig.getRangeX(), rangeConfig.getRangeY(), rangeConfig.getRangeZ());
        }

        // LivingEntityのみを抽出
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // 敵対的エンティティのみをターゲット
                if (isValidTarget(livingEntity, player)) {
                    targets.add(livingEntity);

                    // 最大ターゲット数制限
                    if (rangeConfig.getMaxTargets() > 0 && targets.size() >= rangeConfig.getMaxTargets()) {
                        break;
                    }
                }
            }
        }

        return targets;
    }

    /**
     * Phase11-4: SkillTargetを使用してターゲットを選択します
     *
     * @param player 発動者
     * @param skill スキル
     * @param config 実行設定
     * @return ターゲットリスト
     */
    private List<LivingEntity> selectTargetsWithSkillTarget(Player player, Skill skill,
                                                            SkillExecutionConfig config) {
        com.example.rpgplugin.skill.target.SkillTarget skillTarget = skill.getSkillTarget();
        if (skillTarget == null) {
            return new ArrayList<>();
        }

        Location origin = player.getLocation();
        Vector direction = player.getLocation().getDirection();

        // 候補エンティティを収集
        List<Entity> candidates = getCandidateEntities(player, skillTarget);

        // TargetSelectorでターゲットを選択
        List<Entity> selectedEntities = TargetSelector.selectTargets(
                player, skillTarget, candidates,
                config.getTargetEntity());

        // LivingEntityのみを抽出して返す
        return selectedEntities.stream()
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .filter(e -> isValidTarget((LivingEntity) e, player))
                .collect(Collectors.toList());
    }

    /**
     * Phase11-4: 候補エンティティを収集します
     *
     * @param player 発動者
     * @param skillTarget スキルターゲット設定
     * @return 候補エンティティリスト
     */
    private List<Entity> getCandidateEntities(Player player,
                                             com.example.rpgplugin.skill.target.SkillTarget skillTarget) {
        Location origin = player.getLocation();

        // 範囲設定に基づいて検索半径を決定
        double searchRadius = getSearchRadius(skillTarget);

        return new ArrayList<>(TargetSelector.getNearbyEntities(origin, searchRadius));
    }

    /**
     * Phase11-4: スキルターゲット設定から検索半径を取得します
     *
     * @param skillTarget スキルターゲット設定
     * @return 検索半径
     */
    private double getSearchRadius(com.example.rpgplugin.skill.target.SkillTarget skillTarget) {
        // 範囲設定から最大値を取得
        if (skillTarget.getCircle() != null) {
            return skillTarget.getCircle().getRadius();
        }
        if (skillTarget.getCone() != null) {
            return skillTarget.getCone().getRange();
        }
        if (skillTarget.getRect() != null) {
            return Math.max(skillTarget.getRect().getWidth(), skillTarget.getRect().getDepth());
        }
        // デフォルト検索範囲
        return 10.0;
    }

    /**
     * エンティティが有効なターゲットかチェックします
     *
     * @param entity チェック対象エンティティ
     * @param caster 発動者
     * @return 有効なターゲットの場合はtrue
     */
    private boolean isValidTarget(LivingEntity entity, Player caster) {
        // プレイヤーはターゲットにしない（PvE環境のためPvPは未サポート）
        if (entity instanceof Player) {
            return false;
        }

        // その他のMobはターゲットとする
        // MythicMobs連携はドロップ管理機能のみ実装済み
        // スキルターゲット判定ではMythicMobsとバニラMOBを区別しない
        return true;
    }

    /**
     * 効果を適用します
     *
     * @param target ターゲットエンティティ
     * @param damage ダメージ
     * @param skill スキル
     */
    public void applyEffect(LivingEntity target, double damage, Skill skill) {
        if (damage > 0) {
            target.damage(damage);
        }

        // TODO: 他の効果（デバフ、ノックバック等）の適用
    }

    /**
     * FormulaEvaluatorを取得します
     *
     * @return 数式エバリュエーター
     */
    public FormulaEvaluator getFormulaEvaluator() {
        return formulaEvaluator;
    }

    /**
     * PlayerManagerを取得します
     *
     * @return プレイヤーマネージャー
     */
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
