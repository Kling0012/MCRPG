package com.example.rpgplugin.skill.repository;

import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.skill.SkillExecutionConfig;
import com.example.rpgplugin.skill.evaluator.FormulaEvaluator;
import com.example.rpgplugin.skill.target.TargetSelector;
import com.example.rpgplugin.skill.result.SkillExecutionResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * スキル実行クラス
 *
 * <p>スキルの実行ロジックを担当します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキル実行の単一責務</li>
 *   <li>Strategy: 異なるスキルタイプの実行を柔軟に対応</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillExecutor {

    private static final Logger LOGGER = Logger.getLogger(SkillExecutor.class.getName());

    private final SkillRepository skillRepository;
    private final PlayerSkillService playerSkillService;
    private final PlayerManager playerManager;
    private final FormulaEvaluator formulaEvaluator;

    /**
     * コンストラクタ
     *
     * @param skillRepository スキルリポジトリ
     * @param playerSkillService プレイヤースキルサービス
     * @param playerManager プレイヤーマネージャー
     */
    public SkillExecutor(SkillRepository skillRepository,
                         PlayerSkillService playerSkillService,
                         PlayerManager playerManager) {
        this.skillRepository = skillRepository;
        this.playerSkillService = playerSkillService;
        this.playerManager = playerManager;
        this.formulaEvaluator = new FormulaEvaluator();
    }

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
        Skill skill = skillRepository.getSkill(skillId);
        if (skill == null) {
            return SkillExecutionResult.failure("スキルが見つかりません: " + skillId);
        }

        // 習得チェック
        if (!playerSkillService.hasSkill(player, skillId)) {
            return SkillExecutionResult.failure("スキルを習得していません: " + skill.getColoredDisplayName());
        }

        // レベル取得
        int level = playerSkillService.getSkillLevel(player, skillId);
        if (level <= 0) {
            return SkillExecutionResult.failure("スキルレベルが0です");
        }

        // クールダウンチェック
        if (config.shouldApplyCooldown() && !checkCooldown(player, skillId, skill)) {
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
            PlayerSkillService.PlayerSkillData data = playerSkillService.getPlayerSkillData(player);
            data.setLastCastTime(skillId, System.currentTimeMillis());
        }

        // 成功メッセージ
        if (targetsHit > 0) {
            player.sendMessage(Component.text("スキルを発動しました: " + skill.getColoredDisplayName() + " Lv." + level
                    + " (ダメージ: " + String.format("%.1f", damage) + ", ターゲット: " + targetsHit + ")", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("スキルを発動しました: " + skill.getColoredDisplayName() + " Lv." + level, NamedTextColor.GREEN));
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
            LOGGER.warning(() -> "[SkillExecutor] ダメージ数式評価エラー: " + e.getMessage());
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
     * クールダウンをチェックします
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @param skill スキル
     * @return クールダウン中でない場合はtrue
     */
    public boolean checkCooldown(Player player, String skillId, Skill skill) {
        PlayerSkillService.PlayerSkillData data = playerSkillService.getPlayerSkillData(player);
        long lastCast = data.getLastCastTime(skillId);
        long currentTime = System.currentTimeMillis();
        long cooldownMs = (long) (skill.getCooldown() * 1000);

        if (currentTime - lastCast < cooldownMs) {
            long remainingSeconds = ((cooldownMs - (currentTime - lastCast)) / 1000) + 1;
            player.sendMessage(Component.text("クールダウン中です: 残り " + remainingSeconds + " 秒", NamedTextColor.RED));
            return false;
        }

        return true;
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
        // SkillTargetが設定されている場合は、TargetSelectorを使用
        if (skill.hasSkillTarget()) {
            return selectTargetsWithSkillTarget(player, skill, config);
        }

        // レガシー: 従来のターゲット選択ロジック
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
     * SkillTargetを使用してターゲットを選択します
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
     * 候補エンティティを収集します
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
     * スキルターゲット設定から検索半径を取得します
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
}
