package com.example.rpgplugin.skill;

import com.example.rpgplugin.model.skill.CooldownConfig;
import com.example.rpgplugin.model.skill.CostConfig;
import com.example.rpgplugin.model.skill.DamageCalculation;
import com.example.rpgplugin.model.skill.FormulaDamageConfig;
import com.example.rpgplugin.model.skill.SkillTreeConfig;
import com.example.rpgplugin.model.skill.TargetingConfig;
import com.example.rpgplugin.model.skill.UnlockRequirement;
import com.example.rpgplugin.model.skill.VariableDefinition;
import com.example.rpgplugin.stats.Stat;

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
    private final List<String> requiredSkills;

    // Phase11-6: 新YAMLフォーマット対応フィールド
    private final java.util.List<VariableDefinition> variables;
    private final FormulaDamageConfig formulaDamage;
    private final TargetingConfig targeting;

    // Phase11-4: スキルエフェクト範囲システム（targetパッケージ統合）
    private final com.example.rpgplugin.skill.target.SkillTarget skillTarget;

    // コンポーネントベーススキルシステム
    private final com.example.rpgplugin.skill.component.SkillEffect componentEffect;

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
                availableClasses, null, null, null, null, null);
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
                 com.example.rpgplugin.skill.target.SkillTarget skillTarget,
                 com.example.rpgplugin.skill.component.SkillEffect componentEffect) {
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
        this.requiredSkills = new ArrayList<>();  // デフォルトは空リスト
        // Phase11-6: 新フィールドの初期化
        this.variables = variables != null ? new ArrayList<>(variables) : new ArrayList<>();
        this.formulaDamage = formulaDamage;
        this.targeting = targeting;
        // Phase11-4: スキルターゲット設定の初期化
        this.skillTarget = skillTarget;
        // コンポーネントベーススキルシステム
        this.componentEffect = componentEffect;
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

    // ==================== Builderパターン ====================

    /**
     * SkillBuilderを作成します
     *
     * <p>Builderパターンを使用してSkillインスタンスを構築します。</p>
     *
     * @return 新しいSkillBuilderインスタンス
     */
    public static SkillBuilder builder() {
        return new SkillBuilder();
    }

    /**
     * 必須フィールドを指定してSkillBuilderを作成します
     *
     * @param id スキルID
     * @param name スキル名
     * @param type スキルタイプ
     * @return 必須フィールドが設定されたSkillBuilder
     */
    public static SkillBuilder builder(String id, String name, SkillType type) {
        return SkillBuilder.create(id, name, type);
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
        // legacy color codes (&) to MiniMessage format
        String displayName = this.displayName;
        if (displayName.contains("&")) {
            displayName = convertLegacyToMiniMessage(displayName);
        }
        return displayName;
    }

    /**
     * 旧形式のカラーコードをMiniMessage形式に変換します
     *
     * @param text 変換前のテキスト
     * @return MiniMessage形式のテキスト
     */
    private static String convertLegacyToMiniMessage(String text) {
        return text.replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&l", "<bold>")
                .replace("&o", "<italic>")
                .replace("&n", "<underline>")
                .replace("&m", "<strikethrough>")
                .replace("&k", "<obfuscated>")
                .replace("&r", "<reset>");
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
     * スキル習得に必要な前提スキルのリストを取得します
     *
     * @return 前提スキルIDのリスト（変更不可）
     */
    public List<String> getRequiredSkills() {
        return new ArrayList<>(requiredSkills);
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
     * コンポーネントベーススキルエフェクトを取得します
     *
     * @return スキルエフェクト、未設定の場合はnull
     */
    public com.example.rpgplugin.skill.component.SkillEffect getComponentEffect() {
        return componentEffect;
    }

    /**
     * コンポーネントベーススキルエフェクトが存在するかチェックします
     *
     * @return コンポーネントエフェクトがある場合はtrue
     */
    public boolean hasComponentEffect() {
        return componentEffect != null;
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

    // ==================== コンポーネントベース統合 ====================

    /**
     * コンポーネントツリーから指定されたキーのコンポーネントを検索します
     *
     * <p>再帰的に全てのコンポーネントを探索します。</p>
     *
     * @param key コンポーネントのキー（例: "damage", "cost", "cooldown"）
     * @return 見つかったコンポーネント、見つからない場合はnull
     * @since 1.5.0
     */
    public com.example.rpgplugin.skill.component.EffectComponent findComponentByKey(String key) {
        if (componentEffect == null) {
            return null;
        }
        return findComponentByKeyRecursive(key, componentEffect.getComponents());
    }

    /**
     * 再帰的にコンポーネントを検索します
     *
     * @param key  検索するキー
     * @param components コンポーネントリスト
     * @return 見つかったコンポーネント、見つからない場合はnull
     */
    private com.example.rpgplugin.skill.component.EffectComponent findComponentByKeyRecursive(
            String key, java.util.List<com.example.rpgplugin.skill.component.EffectComponent> components) {
        for (com.example.rpgplugin.skill.component.EffectComponent component : components) {
            // 現在のコンポーネントをチェック
            if (key.equals(component.getKey())) {
                return component;
            }
            // 子コンポーネントを再帰的に探索
            com.example.rpgplugin.skill.component.EffectComponent found =
                    findComponentByKeyRecursive(key, component.getChildren());
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * コンポーネントからダメージ値を取得します
     *
     * <p>damageコンポーネントの設定からダメージ値を計算します。</p>
     *
     * @param level スキルレベル
     * @return ダメージ値
     * @since 1.5.0
     */
    public double getDamageFromComponents(int level) {
        com.example.rpgplugin.skill.component.EffectComponent damageComponent =
                findComponentByKey("damage");

        if (damageComponent != null && damageComponent.getSettings() != null) {
            com.example.rpgplugin.skill.component.ComponentSettings settings = damageComponent.getSettings();

            if (settings.has("value")) {
                String valueStr = settings.getString("value", "0");
                try {
                    String formula = valueStr.replace("level", String.valueOf(level))
                                             .replace("Lv", String.valueOf(level));
                    return parseSimpleFormula(formula);
                } catch (Exception e) {
                    return settings.getDouble("value", 0.0);
                }
            }

            return settings.getDouble("value", 0.0);
        }

        return 0.0;
    }

    /**
     * コンポーネントからコスト値を取得します
     *
     * <p>costコンポーネントの設定からコスト値を計算します。</p>
     *
     * @param level スキルレベル
     * @return コスト値
     * @since 1.5.0
     */
    public int getCostFromComponents(int level) {
        com.example.rpgplugin.skill.component.EffectComponent costComponent =
                findComponentByKey("cost");

        if (costComponent != null && costComponent.getSettings() != null) {
            com.example.rpgplugin.skill.component.ComponentSettings settings = costComponent.getSettings();

            if (settings.has("value")) {
                String valueStr = settings.getString("value", "0");
                try {
                    String formula = valueStr.replace("level", String.valueOf(level))
                                             .replace("Lv", String.valueOf(level));
                    return (int) parseSimpleFormula(formula);
                } catch (Exception e) {
                    return settings.getInt("value", 0);
                }
            }

            return settings.getInt("value", 0);
        }

        return 0;
    }

    /**
     * コンポーネントからクールダウン値を取得します
     *
     * <p>cooldownコンポーネントの設定からクールダウン値を計算します。</p>
     *
     * @param level スキルレベル
     * @return クールダウン（秒）
     * @since 1.5.0
     */
    public double getCooldownFromComponents(int level) {
        com.example.rpgplugin.skill.component.EffectComponent cooldownComponent =
                findComponentByKey("cooldown");

        if (cooldownComponent != null && cooldownComponent.getSettings() != null) {
            com.example.rpgplugin.skill.component.ComponentSettings settings = cooldownComponent.getSettings();

            if (settings.has("value")) {
                String valueStr = settings.getString("value", "0");
                try {
                    String formula = valueStr.replace("level", String.valueOf(level))
                                             .replace("Lv", String.valueOf(level));
                    return parseSimpleFormula(formula);
                } catch (Exception e) {
                    return settings.getDouble("value", 0.0);
                }
            }

            return settings.getDouble("value", 0.0);
        }

        return 0.0;
    }

    /**
     * コンポーネントからターゲット設定を取得します
     *
     * <p>targetコンポーネントの設定を使用します。</p>
     *
     * @return ターゲット設定、見つからない場合はnull
     * @since 1.5.0
     */
    public com.example.rpgplugin.skill.target.SkillTarget getTargetFromComponents() {
        // targetコンポーネントを検索
        com.example.rpgplugin.skill.component.EffectComponent targetComponent =
                findComponentByKey("target");

        if (targetComponent != null && targetComponent.getSettings() != null) {
            com.example.rpgplugin.skill.component.ComponentSettings settings = targetComponent.getSettings();

            // 設定からSkillTargetを構築
            String typeStr = settings.getString("type", "SELF");
            String areaShapeStr = settings.getString("area_shape", "SPHERE");
            double range = settings.getDouble("range", 5.0);
            int maxTargets = settings.getInt("max_targets", 1);

            // TargetTypeに変換
            com.example.rpgplugin.skill.target.TargetType targetType;
            try {
                targetType = com.example.rpgplugin.skill.target.TargetType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                targetType = com.example.rpgplugin.skill.target.TargetType.NEAREST_HOSTILE;
            }

            // AreaShapeに変換
            com.example.rpgplugin.skill.target.AreaShape areaShape;
            try {
                areaShape = com.example.rpgplugin.skill.target.AreaShape.valueOf(areaShapeStr);
            } catch (IllegalArgumentException e) {
                areaShape = com.example.rpgplugin.skill.target.AreaShape.SPHERE;
            }

            // SingleTargetConfigを作成（単体ターゲットの場合）
            com.example.rpgplugin.skill.target.SkillTarget.SingleTargetConfig singleTarget = null;
            if (maxTargets == 1) {
                boolean selectNearest = settings.getBoolean("select_nearest", true);
                boolean targetSelf = settings.getBoolean("target_self", false);
                singleTarget = new com.example.rpgplugin.skill.target.SkillTarget.SingleTargetConfig(
                        selectNearest, targetSelf);
            }

            return new com.example.rpgplugin.skill.target.SkillTarget(
                    targetType,
                    areaShape,
                    singleTarget,
                    null, null, null,
                    com.example.rpgplugin.skill.target.EntityTypeFilter.ALL,
                    maxTargets,
                    com.example.rpgplugin.skill.target.TargetGroupFilter.BOTH,
                    false, false, false,
                    range, 0.0, 0.0, range
            );
        }

        return null;
    }

    /**
     * 簡易数式を解析します
     *
     * <p>基本演算（+, -, *, /）と括弧に対応しています。</p>
     *
     * @param formula 数式
     * @return 計算結果
     */
    private double parseSimpleFormula(String formula) {
        try {
            // 簡易パーサー: 演算子の優先順位を考慮
            return new Object() {
                int pos = -1, ch;

                void nextChar() {
                    ch = (++pos < formula.length()) ? formula.charAt(pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (ch == ' ') nextChar();
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }

                double parse() {
                    nextChar();
                    double x = parseExpression();
                    if (pos < formula.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                    return x;
                }

                double parseExpression() {
                    double x = parseTerm();
                    for (;;) {
                        if (eat('+')) x += parseTerm();
                        else if (eat('-')) x -= parseTerm();
                        else return x;
                    }
                }

                double parseTerm() {
                    double x = parseFactor();
                    for (;;) {
                        if (eat('*')) x *= parseFactor();
                        else if (eat('/')) x /= parseFactor();
                        else return x;
                    }
                }

                double parseFactor() {
                    if (eat('+')) return parseFactor();
                    if (eat('-')) return -parseFactor();

                    double x;
                    int startPos = this.pos;
                    if (eat('(')) {
                        x = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        x = Double.parseDouble(formula.substring(startPos, this.pos));
                    } else {
                        throw new RuntimeException("Unexpected: " + (char) ch);
                    }

                    return x;
                }
            }.parse();
        } catch (Exception e) {
            // パース失敗時は数値として試みる
            try {
                return Double.parseDouble(formula);
            } catch (NumberFormatException ex) {
                return 0.0;
            }
        }
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
