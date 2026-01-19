package com.example.rpgplugin.skill;

import com.example.rpgplugin.skill.component.SkillEffect;
import com.example.rpgplugin.skill.target.SkillTarget;

import java.util.ArrayList;
import java.util.List;

/**
 * スキルビルダー
 *
 * <p>Skillクラスのインスタンスを構築するBuilderパターン実装です。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>Builderパターン: 複雑なオブジェクトの構築をシンプルに</li>
 *   <li>メソッドチェーン: 流れるようなAPI</li>
 *   <li>不変オブジェクト: 構築後の変更を防止</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public final class SkillBuilder {

    private String id;
    private String name;
    private String displayName;
    private SkillType type;
    private List<String> description = new ArrayList<>();
    private int maxLevel;
    private double cooldown;
    private int manaCost;
    private LevelDependentParameter cooldownParameter;
    private LevelDependentParameter costParameter;
    private SkillCostType costType = SkillCostType.MANA;
    private Skill.DamageCalculation damage;
    private Skill.SkillTreeConfig skillTree;
    private String iconMaterial = "DIAMOND_SWORD";
    private List<String> availableClasses = new ArrayList<>();
    private List<Skill.VariableDefinition> variables = new ArrayList<>();
    private Skill.FormulaDamageConfig formulaDamage;
    private Skill.TargetingConfig targeting;
    private SkillTarget skillTarget;
    private SkillEffect componentEffect;

    /**
     * コンストラクタ
     */
    public SkillBuilder() {
    }

    /**
     * 必須フィールドを指定してビルダーを開始します
     *
     * @param id スキルID
     * @param name スキル名
     * @param type スキルタイプ
     * @return this
     */
    public static SkillBuilder create(String id, String name, SkillType type) {
        return new SkillBuilder()
                .id(id)
                .name(name)
                .displayName(name)
                .type(type);
    }

    // ==================== 基本プロパティ ====================

    /**
     * スキルIDを設定します
     *
     * @param id スキルID
     * @return this
     */
    public SkillBuilder id(String id) {
        this.id = id;
        return this;
    }

    /**
     * スキル名を設定します
     *
     * @param name スキル名
     * @return this
     */
    public SkillBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * 表示名を設定します
     *
     * @param displayName 表示名
     * @return this
     */
    public SkillBuilder displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * スキルタイプを設定します
     *
     * @param type スキルタイプ
     * @return this
     */
    public SkillBuilder type(SkillType type) {
        this.type = type;
        return this;
    }

    /**
     * 説明を設定します
     *
     * @param description 説明リスト
     * @return this
     */
    public SkillBuilder description(List<String> description) {
        this.description = description != null ? new ArrayList<>(description) : new ArrayList<>();
        return this;
    }

    /**
     * 説明を追加します
     *
     * @param line 説明行
     * @return this
     */
    public SkillBuilder addDescription(String line) {
        this.description.add(line);
        return this;
    }

    /**
     * 最大レベルを設定します
     *
     * @param maxLevel 最大レベル
     * @return this
     */
    public SkillBuilder maxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        return this;
    }

    // ==================== コスト・クールダウン ====================

    /**
     * クールダウンを設定します
     *
     * @param cooldown クールダウン（秒）
     * @return this
     */
    public SkillBuilder cooldown(double cooldown) {
        this.cooldown = cooldown;
        return this;
    }

    /**
     * レベル依存クールダウンパラメータを設定します
     *
     * @param cooldownParameter クールダウンパラメータ
     * @return this
     */
    public SkillBuilder cooldownParameter(LevelDependentParameter cooldownParameter) {
        this.cooldownParameter = cooldownParameter;
        return this;
    }

    /**
     * クールダウン設定を設定します
     *
     * @param cooldownConfig クールダウン設定
     * @return this
     */
    public SkillBuilder cooldownConfig(Skill.CooldownConfig cooldownConfig) {
        if (cooldownConfig != null) {
            this.cooldownParameter = cooldownConfig.getParameter();
        }
        return this;
    }

    /**
     * マナコストを設定します
     *
     * @param manaCost マナコスト
     * @return this
     */
    public SkillBuilder manaCost(int manaCost) {
        this.manaCost = manaCost;
        return this;
    }

    /**
     * レベル依存コストパラメータを設定します
     *
     * @param costParameter コストパラメータ
     * @return this
     */
    public SkillBuilder costParameter(LevelDependentParameter costParameter) {
        this.costParameter = costParameter;
        return this;
    }

    /**
     * コスト設定を設定します
     *
     * @param costConfig コスト設定
     * @return this
     */
    public SkillBuilder costConfig(Skill.CostConfig costConfig) {
        if (costConfig != null) {
            this.costParameter = costConfig.getParameter();
            this.costType = costConfig.getType();
        }
        return this;
    }

    /**
     * コストタイプを設定します
     *
     * @param costType コストタイプ
     * @return this
     */
    public SkillBuilder costType(SkillCostType costType) {
        this.costType = costType != null ? costType : SkillCostType.MANA;
        return this;
    }

    // ==================== ダメージ計算 ====================

    /**
     * ダメージ計算設定を設定します
     *
     * @param damage ダメージ計算設定
     * @return this
     */
    public SkillBuilder damage(Skill.DamageCalculation damage) {
        this.damage = damage;
        return this;
    }

    /**
     * ダメージ計算設定をビルダーで設定します
     *
     * @param base 基礎ダメージ
     * @param stat ステータス
     * @param multiplier 倍率
     * @param levelMultiplier レベル倍率
     * @return this
     */
    public SkillBuilder damage(double base, com.example.rpgplugin.stats.Stat stat, double multiplier, double levelMultiplier) {
        this.damage = new Skill.DamageCalculation(base, stat, multiplier, levelMultiplier);
        return this;
    }

    /**
     * 数式ダメージ設定を設定します
     *
     * @param formulaDamage 数式ダメージ設定
     * @return this
     */
    public SkillBuilder formulaDamage(Skill.FormulaDamageConfig formulaDamage) {
        this.formulaDamage = formulaDamage;
        return this;
    }

    // ==================== スキルツリー ====================

    /**
     * スキルツリー設定を設定します
     *
     * @param skillTree スキルツリー設定
     * @return this
     */
    public SkillBuilder skillTree(Skill.SkillTreeConfig skillTree) {
        this.skillTree = skillTree;
        return this;
    }

    // ==================== アイコン・クラス ====================

    /**
     * アイコン素材を設定します
     *
     * @param iconMaterial アイコン素材名
     * @return this
     */
    public SkillBuilder iconMaterial(String iconMaterial) {
        this.iconMaterial = iconMaterial != null ? iconMaterial : "DIAMOND_SWORD";
        return this;
    }

    /**
     * 利用可能なクラスを設定します
     *
     * @param availableClasses クラスIDリスト
     * @return this
     */
    public SkillBuilder availableClasses(List<String> availableClasses) {
        this.availableClasses = availableClasses != null ? new ArrayList<>(availableClasses) : new ArrayList<>();
        return this;
    }

    /**
     * 利用可能なクラスを追加します
     *
     * @param classId クラスID
     * @return this
     */
    public SkillBuilder addAvailableClass(String classId) {
        this.availableClasses.add(classId);
        return this;
    }

    // ==================== カスタム変数 ====================

    /**
     * カスタム変数定義を設定します
     *
     * @param variables カスタム変数定義リスト
     * @return this
     */
    public SkillBuilder variables(List<Skill.VariableDefinition> variables) {
        this.variables = variables != null ? new ArrayList<>(variables) : new ArrayList<>();
        return this;
    }

    /**
     * カスタム変数を追加します
     *
     * @param name 変数名
     * @param value 変数値
     * @return this
     */
    public SkillBuilder addVariable(String name, double value) {
        this.variables.add(new Skill.VariableDefinition(name, value));
        return this;
    }

    // ==================== ターゲット設定 ====================

    /**
     * ターゲット設定を設定します
     *
     * @param targeting ターゲット設定
     * @return this
     */
    public SkillBuilder targeting(Skill.TargetingConfig targeting) {
        this.targeting = targeting;
        return this;
    }

    /**
     * スキルターゲット設定を設定します
     *
     * @param skillTarget スキルターゲット設定
     * @return this
     */
    public SkillBuilder skillTarget(SkillTarget skillTarget) {
        this.skillTarget = skillTarget;
        return this;
    }

    // ==================== コンポーネント ====================

    /**
     * コンポーネントエフェクトを設定します
     *
     * @param componentEffect コンポーネントエフェクト
     * @return this
     */
    public SkillBuilder componentEffect(SkillEffect componentEffect) {
        this.componentEffect = componentEffect;
        return this;
    }

    // ==================== ビルド ====================

    /**
     * Skillインスタンスを構築します
     *
     * @return 構築されたSkillインスタンス
     * @throws IllegalStateException 必須フィールドが未設定の場合
     */
    public Skill build() {
        // 必須フィールドのバリデーション
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalStateException("id is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("name is required");
        }
        if (type == null) {
            throw new IllegalStateException("type is required");
        }

        // デフォルト値の設定
        if (displayName == null) {
            displayName = name;
        }
        if (maxLevel <= 0) {
            maxLevel = 5;
        }
        if (description == null) {
            description = new ArrayList<>();
        }
        if (availableClasses == null) {
            availableClasses = new ArrayList<>();
        }
        if (variables == null) {
            variables = new ArrayList<>();
        }

        // 新しいフルコンストラクタを使用してインスタンスを作成
        return new Skill(
                id, name, displayName, type, description, maxLevel,
                cooldown, manaCost,
                cooldownParameter, costParameter, costType,
                damage, skillTree, iconMaterial, availableClasses,
                variables, formulaDamage, targeting, skillTarget, componentEffect
        );
    }
}
