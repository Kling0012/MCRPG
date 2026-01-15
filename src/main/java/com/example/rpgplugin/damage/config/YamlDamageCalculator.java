package com.example.rpgplugin.damage.config;

import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.evaluator.ExpressionParser;
import com.example.rpgplugin.skill.evaluator.VariableContext;

import java.util.*;

/**
 * YAMLベースダメージ計算機
 *
 * <p>DamageConfigを使用してダメージを計算します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: YAML設定に基づくダメージ計算の単一責務</li>
 *   <li>DRY: VariableContext/ExpressionParserを活用</li>
 *   <li>KISS: シンプルな計算フロー</li>
 * </ul>
 *
 * @version 1.0.0
 * @author RPGPlugin Team
 */
public class YamlDamageCalculator {

    /** ダメージ設定 */
    private final DamageConfig damageConfig;

    /** 変数スコープマネージャー */
    private final VariableScopeManager scopeManager;

    /**
     * コンストラクタ
     *
     * @param damageConfig ダメージ設定
     * @param scopeManager 変数スコープマネージャー
     */
    public YamlDamageCalculator(DamageConfig damageConfig, VariableScopeManager scopeManager) {
        this.damageConfig = damageConfig;
        this.scopeManager = scopeManager;
        if (scopeManager != null) {
            scopeManager.setDamageConfig(damageConfig);
        }
    }

    /**
     * スキルダメージを計算します
     *
     * @param baseDamage 基本ダメージ
     * @param attacker 攻撃者
     * @param skillLevel スキルレベル
     * @return 計算されたダメージ
     */
    public double calculateSkillDamage(double baseDamage, RPGPlayer attacker, int skillLevel) {
        return calculateSkillDamage(baseDamage, attacker, skillLevel, null);
    }

    /**
     * スキルダメージを計算します（追加変数付き）
     *
     * @param baseDamage 基本ダメージ
     * @param attacker 攻撃者
     * @param skillLevel スキルレベル
     * @param additionalVariables 追加変数
     * @return 計算されたダメージ
     */
    public double calculateSkillDamage(
            double baseDamage,
            RPGPlayer attacker,
            int skillLevel,
            Map<String, Double> additionalVariables) {

        // クラス別設定を取得
        String className = attacker != null ? attacker.getClassId() : null;
        EventConfig config = className != null
                ? damageConfig.getEventConfigForClass(className, "skill_damage")
                : damageConfig.getEventConfig("skill_damage");

        if (config == null) {
            // デフォルト計算
            return baseDamage;
        }

        return calculateDamage(baseDamage, attacker, skillLevel, config, additionalVariables);
    }

    /**
     * 物理攻撃ダメージを計算します
     *
     * @param baseDamage 基本ダメージ
     * @param attacker 攻撃者
     * @return 計算されたダメージ
     */
    public double calculatePhysicalAttack(double baseDamage, RPGPlayer attacker) {
        String className = attacker != null ? attacker.getClassId() : null;
        EventConfig config = className != null
                ? damageConfig.getEventConfigForClass(className, "physical_attack")
                : damageConfig.getEventConfig("physical_attack");

        if (config == null) {
            return baseDamage;
        }

        return calculateDamage(baseDamage, attacker, null, config, null);
    }

    /**
     * 魔法攻撃ダメージを計算します
     *
     * @param baseDamage 基本ダメージ
     * @param attacker 攻撃者
     * @return 計算されたダメージ
     */
    public double calculateMagicAttack(double baseDamage, RPGPlayer attacker) {
        String className = attacker != null ? attacker.getClassId() : null;
        EventConfig config = className != null
                ? damageConfig.getEventConfigForClass(className, "magic_attack")
                : damageConfig.getEventConfig("magic_attack");

        if (config == null) {
            return baseDamage;
        }

        return calculateDamage(baseDamage, attacker, null, config, null);
    }

    /**
     * 被ダメージ軽減後のダメージを計算します
     *
     * @param damage 元ダメージ
     * @param target 対象プレイヤー
     * @param isMagic 魔法ダメージかどうか
     * @return 軽減後のダメージ
     */
    public double calculateDamageTaken(double damage, RPGPlayer target, boolean isMagic) {
        EventConfig config = damageConfig.getEventConfig("damage_taken");

        if (config == null) {
            return damage;
        }

        // 防御カット計算
        String cutFormula = isMagic
                ? config.getMagicCutFormula().orElse(null)
                : config.getPhysicalCutFormula().orElse(null);

        if (cutFormula != null) {
            double cutDamage = evaluateFormula(cutFormula, damage, target, null, null);
            return Math.max(cutDamage, config.getMinDamage());
        }

        return damage;
    }

    /**
     * クリティカルヒット判定を行います
     *
     * @param attacker 攻撃者
     * @param className クラス名
     * @return クリティカルヒットの場合はtrue
     */
    public boolean isCriticalHit(RPGPlayer attacker, String className) {
        EventConfig config = className != null
                ? damageConfig.getEventConfigForClass(className, "skill_damage")
                : damageConfig.getEventConfig("skill_damage");

        if (config == null) {
            return false;
        }

        Optional<EventConfig.CriticalConfig> criticalOpt = config.getCritical();
        if (criticalOpt.isEmpty()) {
            return false;
        }

        EventConfig.CriticalConfig critical = criticalOpt.get();
        double chance = evaluateNumericFormula(critical.getChanceFormula(), attacker, null);
        return Math.random() < chance;
    }

    /**
     * クリティカル倍率を計算します
     *
     * @param attacker 攻撃者
     * @param className クラス名
     * @return クリティカル倍率
     */
    public double getCriticalMultiplier(RPGPlayer attacker, String className) {
        EventConfig config = className != null
                ? damageConfig.getEventConfigForClass(className, "skill_damage")
                : damageConfig.getEventConfig("skill_damage");

        if (config == null) {
            return 2.0;
        }

        Optional<EventConfig.CriticalConfig> criticalOpt = config.getCritical();
        if (criticalOpt.isEmpty()) {
            return 2.0;
        }

        EventConfig.CriticalConfig critical = criticalOpt.get();
        return evaluateNumericFormula(critical.getMultiplierFormula(), attacker, null);
    }

    /**
     * ダメージ計算の共通処理
     */
    private double calculateDamage(
            double baseDamage,
            RPGPlayer attacker,
            Integer skillLevel,
            EventConfig config,
            Map<String, Double> additionalVariables) {

        String formula = config.getFormula();
        if (formula == null || formula.isEmpty()) {
            formula = config.getFallbackFormula();
        }

        if (formula == null || formula.isEmpty()) {
            return baseDamage;
        }

        double damage = evaluateFormula(formula, baseDamage, attacker, skillLevel, additionalVariables);

        // 最小/最大値の適用
        damage = Math.max(damage, config.getMinDamage());
        if (config.getMaxDamage().isPresent()) {
            damage = Math.min(damage, config.getMaxDamage().get());
        }

        return damage;
    }

    /**
     * 数式を評価します（BASE_DAMAGE変数付き）
     */
    private double evaluateFormula(
            String formula,
            double baseDamage,
            RPGPlayer player,
            Integer skillLevel,
            Map<String, Double> additionalVariables) {

        try {
            // VariableContextを構築
            VariableContext context = scopeManager != null && player != null
                    ? scopeManager.buildVariableContext(player, skillLevel)
                    : new VariableContext(player);

            // 追加変数を設定
            Map<String, Double> vars = new HashMap<>();
            vars.put("BASE_DAMAGE", baseDamage);
            vars.put("DAMAGE", baseDamage);
            if (additionalVariables != null) {
                vars.putAll(additionalVariables);
            }
            context.setCustomVariables(vars);

            // 数式を解析・評価
            ExpressionParser parser = new ExpressionParser(formula);
            Object result = parser.evaluate(context);

            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }

            // Booleanの場合は1.0または0.0に変換
            if (result instanceof Boolean) {
                return ((Boolean) result) ? 1.0 : 0.0;
            }

            return baseDamage;

        } catch (Exception e) {
            // 数式評価エラー時は基本ダメージを返す
            return baseDamage;
        }
    }

    /**
     * 数値数式を評価します（BASE_DAMAGEなし）
     */
    private double evaluateNumericFormula(String formula, RPGPlayer player, Integer skillLevel) {
        try {
            VariableContext context = scopeManager != null && player != null
                    ? scopeManager.buildVariableContext(player, skillLevel)
                    : new VariableContext(player);

            ExpressionParser parser = new ExpressionParser(formula);
            Object result = parser.evaluate(context);

            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }

            if (result instanceof Boolean) {
                return ((Boolean) result) ? 1.0 : 0.0;
            }

            return 0.0;

        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * ダメージ設定を取得します
     *
     * @return ダメージ設定
     */
    public DamageConfig getDamageConfig() {
        return damageConfig;
    }

    /**
     * 変数スコープマネージャーを取得します
     *
     * @return 変数スコープマネージャー
     */
    public VariableScopeManager getScopeManager() {
        return scopeManager;
    }

    /**
     * クリティカルヒット計算結果
     */
    public static class CriticalResult {
        private final boolean isCritical;
        private final double multiplier;
        private final double finalDamage;

        public CriticalResult(boolean isCritical, double multiplier, double finalDamage) {
            this.isCritical = isCritical;
            this.multiplier = multiplier;
            this.finalDamage = finalDamage;
        }

        public boolean isCritical() {
            return isCritical;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public double getFinalDamage() {
            return finalDamage;
        }
    }

    /**
     * クリティカルヒットを含むダメージ計算
     *
     * @param baseDamage 基本ダメージ
     * @param attacker 攻撃者
     * @param skillLevel スキルレベル
     * @return クリティカルヒット計算結果
     */
    public CriticalResult calculateWithCritical(
            double baseDamage,
            RPGPlayer attacker,
            int skillLevel) {

        String className = attacker != null ? attacker.getClassId() : null;

        // 通常ダメージ計算
        double damage = calculateSkillDamage(baseDamage, attacker, skillLevel);

        // クリティカル判定
        boolean isCritical = isCriticalHit(attacker, className);
        double multiplier = 1.0;

        if (isCritical) {
            multiplier = getCriticalMultiplier(attacker, className);
            damage *= multiplier;
        }

        return new CriticalResult(isCritical, multiplier, damage);
    }

    /**
     * 計算結果（詳細）
     */
    public static class DamageResult {
        private final double baseDamage;
        private final double finalDamage;
        private final double minDamage;
        private final double maxDamage;
        private final String formula;
        private final Map<String, Object> variables;
        private final boolean isCritical;
        private final double criticalMultiplier;

        public DamageResult(
                double baseDamage,
                double finalDamage,
                double minDamage,
                Double maxDamage,
                String formula,
                Map<String, Object> variables,
                boolean isCritical,
                double criticalMultiplier) {
            this.baseDamage = baseDamage;
            this.finalDamage = finalDamage;
            this.minDamage = minDamage;
            this.maxDamage = maxDamage != null ? maxDamage : Double.MAX_VALUE;
            this.formula = formula;
            this.variables = variables != null ? variables : Collections.emptyMap();
            this.isCritical = isCritical;
            this.criticalMultiplier = criticalMultiplier;
        }

        public double getBaseDamage() {
            return baseDamage;
        }

        public double getFinalDamage() {
            return finalDamage;
        }

        public double getMinDamage() {
            return minDamage;
        }

        public double getMaxDamage() {
            return maxDamage;
        }

        public String getFormula() {
            return formula;
        }

        public Map<String, Object> getVariables() {
            return new HashMap<>(variables);
        }

        public boolean isCritical() {
            return isCritical;
        }

        public double getCriticalMultiplier() {
            return criticalMultiplier;
        }
    }

    /**
     * 詳細ダメージ計算（デバッグ用）
     */
    public DamageResult calculateDetailed(
            double baseDamage,
            RPGPlayer attacker,
            int skillLevel,
            String eventType) {

        String className = attacker != null ? attacker.getClassId() : null;
        EventConfig config = className != null
                ? damageConfig.getEventConfigForClass(className, eventType)
                : damageConfig.getEventConfig(eventType);

        if (config == null) {
            return new DamageResult(baseDamage, baseDamage, 1, null, "BASE_DAMAGE", Collections.emptyMap(), false, 1.0);
        }

        String formula = config.getFormula();
        Map<String, Object> vars = new HashMap<>();
        vars.put("BASE_DAMAGE", baseDamage);

        // ステータス値を収集
        if (attacker != null) {
            vars.put("STR", attacker.getStatManager().getFinalStat(com.example.rpgplugin.stats.Stat.STRENGTH));
            vars.put("INT", attacker.getStatManager().getFinalStat(com.example.rpgplugin.stats.Stat.INTELLIGENCE));
            vars.put("SPI", attacker.getStatManager().getFinalStat(com.example.rpgplugin.stats.Stat.SPIRIT));
            vars.put("VIT", attacker.getStatManager().getFinalStat(com.example.rpgplugin.stats.Stat.VITALITY));
            vars.put("DEX", attacker.getStatManager().getFinalStat(com.example.rpgplugin.stats.Stat.DEXTERITY));
            vars.put("LV", attacker.getLevel());
            vars.put("CLASS_RANK", attacker.getClassRank());
        }

        // クリティカル判定
        boolean isCritical = isCriticalHit(attacker, className);
        double critMult = 1.0;
        if (isCritical) {
            critMult = getCriticalMultiplier(attacker, className);
        }

        double damage = calculateSkillDamage(baseDamage, attacker, skillLevel);
        if (isCritical) {
            damage *= critMult;
        }

        return new DamageResult(
                baseDamage,
                damage,
                config.getMinDamage(),
                config.getMaxDamage().orElse(null),
                formula,
                vars,
                isCritical,
                critMult
        );
    }
}
