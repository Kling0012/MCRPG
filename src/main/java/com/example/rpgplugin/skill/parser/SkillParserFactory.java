package com.example.rpgplugin.skill.parser;

import com.example.rpgplugin.model.skill.VariableDefinition;
import com.example.rpgplugin.skill.Skill;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.logging.Logger;

/**
 * スキルパーサファクトリ
 *
 * <p>スキルパースに必要なコンポーネントを統合し、一貫したパース処理を提供します。</p>
 */
public class SkillParserFactory {

    private final Logger logger;
    private final SkillValidator validator;
    private final ComponentParser componentParser;
    private final LegacyConverter legacyConverter;

    /**
     * ファクトリを初期化します
     *
     * @param logger ロガー
     */
    public SkillParserFactory(Logger logger) {
        this.logger = logger;
        this.validator = new SkillValidator(logger);
        this.componentParser = new ComponentParser(logger, validator);
        this.legacyConverter = new LegacyConverter(logger, validator);
    }

    /**
     * スキル設定をパースします
     *
     * @param config   設定
     * @param fileName ファイル名
     * @return スキル
     */
    public Skill parseSkill(FileConfiguration config, String fileName) {
        // 必須フィールドのバリデーション
        if (!validator.validateRequiredFields(config, fileName)) {
            return null;
        }

        String id = config.getString("id");
        String name = config.getString("name");
        String displayName = config.getString("display_name", name);

        // スキルタイプ解析
        var type = legacyConverter.parseSkillType(config.getString("type"), fileName);
        if (type == null) {
            return null;
        }

        // 基本設定
        List<String> description = config.getStringList("description");
        int maxLevel = config.getInt("max_level", 5);
        validator.validateRange(maxLevel, 1, 100, "max_level", fileName);

        // レベル依存パラメータ
        var cooldownParameter = legacyConverter.parseCooldownParameter(config, fileName);
        var costParameter = legacyConverter.parseCostParameter(config, fileName);
        var costType = legacyConverter.parseCostType(config, fileName);

        // ダメージ計算
        var damage = legacyConverter.parseDamageCalculation(config, fileName);
        var formulaDamage = legacyConverter.parseFormulaDamage(config, fileName);

        // ターゲット設定
        var targeting = legacyConverter.parseTargeting(config, fileName);
        var skillTarget = legacyConverter.parseSkillTarget(config, fileName);

        // スキルツリー
        var skillTree = legacyConverter.parseSkillTree(config, fileName);

        // アイコン素材
        String iconMaterial = legacyConverter.parseIconMaterial(config, skillTree);

        // 利用可能なクラス
        List<String> availableClasses = config.getStringList("available_classes");

        // フォールバック値
        double cooldownFallback = config.getDouble("cooldown", 0.0);
        int manaCostFallback = config.getInt("mana_cost", 0);

        // カスタム変数
        List<VariableDefinition> variables = legacyConverter.parseVariables(config);

        // コンポーネントベーススキル
        var componentEffect = componentParser.parse(config, id);

        return new Skill(id, name, displayName, type, description, maxLevel,
                cooldownFallback, manaCostFallback,
                cooldownParameter, costParameter, costType,
                damage, skillTree, iconMaterial, availableClasses,
                variables, formulaDamage, targeting, skillTarget, componentEffect);
    }

    /**
     * バリデータを取得します
     *
     * @return バリデータ
     */
    public SkillValidator getValidator() {
        return validator;
    }

    /**
     * コンポーネントパーサを取得します
     *
     * @return コンポーネントパーサ
     */
    public ComponentParser getComponentParser() {
        return componentParser;
    }

    /**
     * レガシーコンバータを取得します
     *
     * @return レガシーコンバータ
     */
    public LegacyConverter getLegacyConverter() {
        return legacyConverter;
    }
}
