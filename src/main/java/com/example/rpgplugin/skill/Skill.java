package com.example.rpgplugin.skill;

import com.example.rpgplugin.stats.Stat;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * スキルデータモデル
 *
 * <p>スキルの基本情報、ダメージ計算式、習得要件などを管理します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキルデータの表現に専念</li>
 *   <li>DRY: 計算ロジックを一元管理</li>
 *   <li>YAGNI: 必要な機能のみ実装</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class Skill {

    private final String id;
    private final String name;
    private final String displayName;
    private final SkillType type;
    private final List<String> description;
    private final int maxLevel;
    private final double cooldown;
    private final int manaCost;
    // レベル依存パラメータ（Phase11-2で追加）
    private final LevelDependentParameter cooldownParameter;
    private final LevelDependentParameter costParameter;
    private final SkillCostType costType;
    private final DamageCalculation damage;
    private final SkillTreeConfig skillTree;
    private final String iconMaterial;
    private final List<String> availableClasses;

    // Phase11-6: 新YAMLフォーマット対応フィールド
    private final java.util.List<VariableDefinition> variables;
    private final FormulaDamageConfig formulaDamage;
    private final TargetingConfig targeting;

    // Phase11-4: スキルエフェクト範囲システム（targetパッケージ統合）
    private final com.example.rpgplugin.skill.target.SkillTarget skillTarget;

    /**
     * ダメージ計算設定
     */
    public static class DamageCalculation {
        private final double base;
        private final Stat statMultiplier;
        private final double multiplierValue;
        private final double levelMultiplier;

        public DamageCalculation(double base, Stat statMultiplier, double multiplierValue, double levelMultiplier) {
            this.base = base;
            this.statMultiplier = statMultiplier;
            this.multiplierValue = multiplierValue;
            this.levelMultiplier = levelMultiplier;
        }

        public double getBase() {
            return base;
        }

        public Stat getStatMultiplier() {
            return statMultiplier;
        }

        public double getMultiplierValue() {
            return multiplierValue;
        }

        public double getLevelMultiplier() {
            return levelMultiplier;
        }

        /**
         * ダメージを計算します
         *
         * @param statValue ステータス値
         * @param skillLevel スキルレベル
         * @return 計算されたダメージ
         */
        public double calculateDamage(double statValue, int skillLevel) {
            return base + (statValue * multiplierValue) + (skillLevel * levelMultiplier);
        }
    }

    /**
     * スキルツリー設定
     */
    public static class SkillTreeConfig {
        private final String parent;
        private final List<UnlockRequirement> unlockRequirements;
        private final int cost;

        public SkillTreeConfig(String parent, List<UnlockRequirement> unlockRequirements, int cost) {
            this.parent = parent;
            this.unlockRequirements = unlockRequirements != null ? unlockRequirements : new ArrayList<>();
            this.cost = cost;
        }

        public String getParent() {
            return parent;
        }

        public List<UnlockRequirement> getUnlockRequirements() {
            return unlockRequirements;
        }

        public int getCost() {
            return cost;
        }
    }

    /**
     * 習得要件
     */
    public static class UnlockRequirement {
        private final String type;
        private final Stat stat;
        private final double value;

        public UnlockRequirement(String type, Stat stat, double value) {
            this.type = type;
            this.stat = stat;
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public Stat getStat() {
            return stat;
        }

        public double getValue() {
            return value;
        }
    }

    /**
     * カスタム変数定義
     * 
     * <p>YAMLのvariablesセクションで定義されるカスタム変数を表します。</p>
     * 
     * <p>YAML例:</p>
     * <pre>
     * variables:
     *   base_mod: 1.0
     *   str_scale: 1.5
     * </pre>
     */
    public static class VariableDefinition {
        private final String name;
        private final double value;
        
        public VariableDefinition(String name, double value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() {
            return name;
        }
        
        public double getValue() {
            return value;
        }
    }

    /**
     * コスト設定
     * 
     * <p>レベル依存のコスト設定を表します。</p>
     * 
     * <p>YAML例:</p>
     * <pre>
     * cost:
     *   type: mana
     *   base: 10
     *   per_level: -1
     *   min: 0
     * </pre>
     */
    public static class CostConfig {
        private final SkillCostType type;
        private final LevelDependentParameter parameter;
        
        public CostConfig(SkillCostType type, LevelDependentParameter parameter) {
            this.type = type != null ? type : SkillCostType.MANA;
            this.parameter = parameter;
        }
        
        public SkillCostType getType() {
            return type;
        }
        
        public LevelDependentParameter getParameter() {
            return parameter;
        }
        
        /**
         * 指定レベルでのコストを取得します
         *
         * @param level スキルレベル
         * @return コスト値
         */
        public int getCost(int level) {
            if (parameter != null) {
                return parameter.getIntValue(level);
            }
            return 0;
        }
    }

    /**
     * クールダウン設定
     * 
     * <p>レベル依存のクールダウン設定を表します。</p>
     * 
     * <p>YAML例:</p>
     * <pre>
     * cooldown:
     *   base: 5.0
     *   per_level: -0.5
     *   min: 1.0
     * </pre>
     */
    public static class CooldownConfig {
        private final LevelDependentParameter parameter;
        
        public CooldownConfig(LevelDependentParameter parameter) {
            this.parameter = parameter;
        }
        
        public LevelDependentParameter getParameter() {
            return parameter;
        }
        
        /**
         * 指定レベルでのクールダウンを取得します
         *
         * @param level スキルレベル
         * @return クールダウン（秒）
         */
        public double getCooldown(int level) {
            if (parameter != null) {
                return parameter.getValue(level);
            }
            return 0.0;
        }
    }

    /**
     * ターゲット設定
     * 
     * <p>スキルのターゲット範囲設定を表します。</p>
     * 
     * <p>YAML例:</p>
     * <pre>
     * targeting:
     *   type: cone
     *   cone:
     *     angle: 90
     *     range: 5.0
     * </pre>
     */
    public static class TargetingConfig {
        private final String type;
        private final TargetingParams params;
        
        public TargetingConfig(String type, TargetingParams params) {
            this.type = type != null ? type : "single";
            this.params = params;
        }
        
        public String getType() {
            return type;
        }
        
        public TargetingParams getParams() {
            return params;
        }
        
        /**
         * ターゲットパラメータの基底クラス
         */
        public static class TargetingParams {
            private final double range;
            
            public TargetingParams(double range) {
                this.range = range;
            }
            
            public double getRange() {
                return range;
            }
        }
        
        /**
         * コーン型範囲パラメータ
         */
        public static class ConeParams extends TargetingParams {
            private final double angle;
            
            public ConeParams(double angle, double range) {
                super(range);
                this.angle = angle;
            }
            
            public double getAngle() {
                return angle;
            }
        }
        
        /**
         * 球形範囲パラメータ
         */
        public static class SphereParams extends TargetingParams {
            private final double radius;
            
            public SphereParams(double radius) {
                super(radius);
                this.radius = radius;
            }
            
            public double getRadius() {
                return radius;
            }
        }
        
        /**
         * 扇形パラメータ
         */
        public static class SectorParams extends TargetingParams {
            private final double angle;
            private final double radius;
            
            public SectorParams(double angle, double radius) {
                super(radius);
                this.angle = angle;
                this.radius = radius;
            }
            
            public double getAngle() {
                return angle;
            }
            
            public double getRadius() {
                return radius;
            }
        }
    }

    /**
     * 数式ダメージ設定
     * 
     * <p>数式を使用したダメージ計算設定を表します。</p>
     * 
     * <p>YAML例:</p>
     * <pre>
     * damage:
     *   formula: "STR * str_scale + (Lv * 5) + base_mod * 10"
     * </pre>
     */
    public static class FormulaDamageConfig {
        private final String formula;
        private final java.util.Map<Integer, String> levelFormulas;
        
        public FormulaDamageConfig(String formula, java.util.Map<Integer, String> levelFormulas) {
            this.formula = formula;
            this.levelFormulas = levelFormulas != null ? levelFormulas : new java.util.HashMap<>();
        }
        
        public String getFormula() {
            return formula;
        }
        
        public java.util.Map<Integer, String> getLevelFormulas() {
            return new java.util.HashMap<>(levelFormulas);
        }
        
        /**
         * 指定レベルの数式を取得します
         *
         * @param level スキルレベル
         * @return 数式、レベル別定義がない場合は基本数式
         */
        public String getFormula(int level) {
            return levelFormulas.getOrDefault(level, formula);
        }
        
        /**
         * レベル別数式が定義されているかチェックします
         *
         * @return レベル別数式が存在する場合はtrue
         */
        public boolean hasLevelFormulas() {
            return !levelFormulas.isEmpty();
        }
    }

    /**
     * コンストラクタ（レベル依存パラメータ対応版）
     *
     * @param id スキルID
     * @param name スキル名
     * @param displayName 表示名（カラーコード対応）
     * @param type スキルタイプ
     * @param description 説明リスト
     * @param maxLevel 最大レベル
     * @param cooldown クールダウン（秒）、レベル依存パラメータ未使用時のフォールバック値
     * @param manaCost 消費MP、レベル依存パラメータ未使用時のフォールバック値
     * @param cooldownParameter レベル依存CDパラメータ（null可能）
     * @param costParameter レベル依存コストパラメータ（null可能）
     * @param costType コストタイプ（MANA/HP）
     * @param damage ダメージ計算設定
     * @param skillTree スキルツリー設定
     * @param iconMaterial アイコン素材
     * @param availableClasses 利用可能なクラスリスト
     */
    public Skill(String id, String name, String displayName, SkillType type, List<String> description,
                 int maxLevel, double cooldown, int manaCost,
                 LevelDependentParameter cooldownParameter, LevelDependentParameter costParameter,
                 SkillCostType costType, DamageCalculation damage,
                 SkillTreeConfig skillTree, String iconMaterial, List<String> availableClasses) {
        this(id, name, displayName, type, description, maxLevel, cooldown, manaCost,
                cooldownParameter, costParameter, costType, damage, skillTree, iconMaterial,
                availableClasses, null, null, null, null);
    }

    /**
     * コンストラクタ（Phase11-6+11-4統合: 新YAMLフォーマット完全対応版）
     *
     * @param id スキルID
     * @param name スキル名
     * @param displayName 表示名（カラーコード対応）
     * @param type スキルタイプ
     * @param description 説明リスト
     * @param maxLevel 最大レベル
     * @param cooldown クールダウン（秒）、レベル依存パラメータ未使用時のフォールバック値
     * @param manaCost 消費MP、レベル依存パラメータ未使用時のフォールバック値
     * @param cooldownParameter レベル依存CDパラメータ（null可能）
     * @param costParameter レベル依存コストパラメータ（null可能）
     * @param costType コストタイプ（MANA/HP）
     * @param damage ダメージ計算設定
     * @param skillTree スキルツリー設定
     * @param iconMaterial アイコン素材
     * @param availableClasses 利用可能なクラスリスト
     * @param variables カスタム変数定義リスト（Phase11-6）
     * @param formulaDamage 数式ダメージ設定（Phase11-6）
     * @param targeting ターゲット設定（Phase11-6 TargetingConfig）
     * @param skillTarget スキルターゲット設定（Phase11-4 SkillTarget）
     */
    public Skill(String id, String name, String displayName, SkillType type, List<String> description,
                 int maxLevel, double cooldown, int manaCost,
                 LevelDependentParameter cooldownParameter, LevelDependentParameter costParameter,
                 SkillCostType costType, DamageCalculation damage,
                 SkillTreeConfig skillTree, String iconMaterial, List<String> availableClasses,
                 java.util.List<VariableDefinition> variables, FormulaDamageConfig formulaDamage,
                 TargetingConfig targeting,
                 com.example.rpgplugin.skill.target.SkillTarget skillTarget) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.description = description != null ? new ArrayList<>(description) : new ArrayList<>();
        this.maxLevel = maxLevel;
        this.cooldown = cooldown;
        this.manaCost = manaCost;
        this.cooldownParameter = cooldownParameter;
        this.costParameter = costParameter;
        this.costType = costType != null ? costType : SkillCostType.MANA;
        this.damage = damage;
        this.skillTree = skillTree;
        this.iconMaterial = iconMaterial != null ? iconMaterial : "DIAMOND_SWORD";
        this.availableClasses = availableClasses != null ? new ArrayList<>(availableClasses) : new ArrayList<>();
        // Phase11-6: 新フィールドの初期化
        this.variables = variables != null ? new ArrayList<>(variables) : new ArrayList<>();
        this.formulaDamage = formulaDamage;
        this.targeting = targeting;
        // Phase11-4: スキルターゲット設定の初期化
        this.skillTarget = skillTarget;
    }

    /**
     * レガシーコンストラクタ（後方互換性維持）
     *
     * @param id スキルID
     * @param name スキル名
     * @param displayName 表示名（カラーコード対応）
     * @param type スキルタイプ
     * @param description 説明リスト
     * @param maxLevel 最大レベル
     * @param cooldown クールダウン（秒）
     * @param manaCost 消費MP
     * @param damage ダメージ計算設定
     * @param skillTree スキルツリー設定
     * @param iconMaterial アイコン素材
     * @param availableClasses 利用可能なクラスリスト
     */
    public Skill(String id, String name, String displayName, SkillType type, List<String> description,
                 int maxLevel, double cooldown, int manaCost, DamageCalculation damage,
                 SkillTreeConfig skillTree, String iconMaterial, List<String> availableClasses) {
        this(id, name, displayName, type, description, maxLevel, cooldown, manaCost,
                null, null, SkillCostType.MANA, damage, skillTree, iconMaterial, availableClasses);
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColoredDisplayName() {
        return ChatColor.translateAlternateColorCodes('&', displayName);
    }

    public SkillType getType() {
        return type;
    }

    public List<String> getDescription() {
        return new ArrayList<>(description);
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public double getCooldown() {
        return cooldown;
    }

    /**
     * 指定レベルでのクールダウンを取得します
     *
     * @param level スキルレベル
     * @return クールダウン（秒）
     */
    public double getCooldown(int level) {
        if (cooldownParameter != null) {
            return cooldownParameter.getValue(level);
        }
        return cooldown;
    }

    public int getManaCost() {
        return manaCost;
    }

    /**
     * 指定レベルでのコストを取得します
     *
     * @param level スキルレベル
     * @return コスト値
     */
    public int getCost(int level) {
        if (costParameter != null) {
            return costParameter.getIntValue(level);
        }
        return manaCost;
    }

    /**
     * コストタイプを取得します
     *
     * @return コストタイプ（MANA/HP）
     */
    public SkillCostType getCostType() {
        return costType;
    }

    /**
     * レベル依存CDパラメータを取得します
     *
     * @return CDパラメータ、未設定の場合はnull
     */
    public LevelDependentParameter getCooldownParameter() {
        return cooldownParameter;
    }

    /**
     * レベル依存コストパラメータを取得します
     *
     * @return コストパラメータ、未設定の場合はnull
     */
    public LevelDependentParameter getCostParameter() {
        return costParameter;
    }

    public DamageCalculation getDamage() {
        return damage;
    }

    public SkillTreeConfig getSkillTree() {
        return skillTree;
    }

    public String getIconMaterial() {
        return iconMaterial;
    }

    public List<String> getAvailableClasses() {
        return new ArrayList<>(availableClasses);
    }

    /**
     * 指定されたクラスでこのスキルを使用可能かチェックします
     *
     * @param classId クラスID
     * @return 使用可能な場合はtrue
     */
    public boolean isAvailableForClass(String classId) {
        if (availableClasses.isEmpty()) {
            return true; // 空リストは全クラスで利用可能
        }
        return availableClasses.contains(classId);
    }

    /**

     * カスタム変数定義リストを取得します
     *
     * @return カスタム変数定義リスト
     */
    public java.util.List<VariableDefinition> getVariables() {
        return new ArrayList<>(variables);
    }

    /**
     * 数式ダメージ設定を取得します
     *
     * @return 数式ダメージ設定、未設定の場合はnull
     */
    public FormulaDamageConfig getFormulaDamage() {
        return formulaDamage;
    }

    /**
     * ターゲット設定を取得します
     *
     * @return ターゲット設定、未設定の場合はnull
     */
    public TargetingConfig getTargeting() {
        return targeting;
    }

    /**
     * 数式ダメージ設定が存在するかチェックします
     *
     * @return 数式ダメージ設定がある場合はtrue
     */
    public boolean hasFormulaDamage() {
        return formulaDamage != null;
    }

    /**
     * ターゲット設定が存在するかチェックします
     *
     * @return ターゲット設定がある場合はtrue
     */
    public boolean hasTargeting() {
        return targeting != null;
    }

    /**
     * カスタム変数が定義されているかチェックします
     *
     * @return カスタム変数がある場合はtrue
     */
    public boolean hasVariables() {
        return variables != null && !variables.isEmpty();
    }

    /**
     * スキルターゲット設定を取得します（Phase11-4）
     *
     * @return スキルターゲット設定、未設定の場合はnull
     */
    public com.example.rpgplugin.skill.target.SkillTarget getSkillTarget() {
        return skillTarget;
    }

    /**
     * スキルターゲット設定が存在するかチェックします（Phase11-4）
     *
     * @return スキルターゲット設定がある場合はtrue
     */
    public boolean hasSkillTarget() {
        return skillTarget != null;
    }

    /**
     * 数式を使用してダメージを計算します
     *
     * <p>このメソッドはFormulaDamageCalculatorと連携して動作します。</p>
     *
     * @param calculator 数式ダメージ計算機
     * @param rpgPlayer RPGプレイヤー
     * @param skillLevel スキルレベル
     * @return 計算されたダメージ、エラー時は0.0
     */
    public double calculateFormulaDamage(com.example.rpgplugin.skill.evaluator.FormulaDamageCalculator calculator,
                                         com.example.rpgplugin.player.RPGPlayer rpgPlayer, int skillLevel) {
        if (formulaDamage == null || calculator == null) {
            return 0.0;
        }

        // カスタム変数を設定
        if (hasVariables()) {
            java.util.Map<String, Double> varMap = new java.util.HashMap<>();
            for (VariableDefinition var : variables) {
                varMap.put(var.getName(), var.getValue());
            }
            calculator.setCustomVariables(varMap);
        }

        try {
            return calculator.calculateDamage(rpgPlayer, skillLevel);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 数式ダメージ計算機を作成します
     *
     * @param evaluator 数式エバリュエーター
     * @return 数式ダメージ計算機、数式設定がない場合はnull
     */
    public com.example.rpgplugin.skill.evaluator.FormulaDamageCalculator createFormulaDamageCalculator(
            com.example.rpgplugin.skill.evaluator.FormulaEvaluator evaluator) {
        if (formulaDamage == null || evaluator == null) {
            return null;
        }
        
        String formula = formulaDamage.getFormula();
        java.util.Map<Integer, String> levelFormulas = formulaDamage.getLevelFormulas();
        
        return new com.example.rpgplugin.skill.evaluator.FormulaDamageCalculator(
                evaluator, formula, levelFormulas, formula);
    }

    /**
     * カスタム変数をマップとして取得します
     *
     * @return 変数名と値のマップ
     */
    public java.util.Map<String, Double> getVariableMap() {
        java.util.Map<String, Double> map = new java.util.HashMap<>();
        if (variables != null) {
            for (VariableDefinition var : variables) {
                map.put(var.getName(), var.getValue());
            }
        }
        return map;
    }

    @Override
    public String toString() {
        return "Skill{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", maxLevel=" + maxLevel +
                '}';
    }
}
