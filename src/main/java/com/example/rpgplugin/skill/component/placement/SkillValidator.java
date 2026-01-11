package com.example.rpgplugin.skill.component.placement;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * スキルYAMLバリデーター
 * <p>コンポーネントの配置ルールを検証します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillValidator {

    private final Logger logger;
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    public SkillValidator(Logger logger) {
        this.logger = logger;
    }

    /**
     * スキルYAMLを検証します
     *
     * @param skillId スキルID
     * @param components コンポーネントセクション
     * @return 検証に成功した場合はtrue
     */
    public boolean validate(String skillId, ConfigurationSection components) {
        errors.clear();
        warnings.clear();

        if (components == null) {
            return true; // コンポーネントなしは有効
        }

        List<?> componentList = components.getList("components");
        if (componentList == null || componentList.isEmpty()) {
            errors.add("[" + skillId + "] コンポーネントが空です");
            return false;
        }

        // ルートレベルのコンポーネントを検証
        Map<ComponentType, Integer> rootCountMap = new HashMap<>();
        for (Object obj : componentList) {
            if (!(obj instanceof ConfigurationSection)) {
                errors.add("[" + skillId + "] コンポーネントの形式が無効です");
                continue;
            }

            ConfigurationSection section = (ConfigurationSection) obj;
            ComponentType type = parseType(section);

            if (type == null) {
                errors.add("[" + skillId + "] コンポーネントタイプが指定されていません");
                continue;
            }

            // ルート配置可能か確認
            if (!PlacementRules.canBeRoot(type)) {
                errors.add("[" + skillId + "] ルートレベルに配置できないタイプです: " + type);
                continue;
            }

            // 最大数チェック
            int count = rootCountMap.getOrDefault(type, 0) + 1;
            int max = PlacementRules.getMaxCount(type);
            if (count > max) {
                errors.add("[" + skillId + "] " + type + " の配置数が上限を超えています: " + count + " > " + max);
            }
            rootCountMap.put(type, count);

            // 子コンポーネントを再帰的に検証
            validateChildren(skillId, section, type, 0);
        }

        // トリガーが必須
        if (rootCountMap.getOrDefault(ComponentType.TRIGGER, 0) == 0) {
            errors.add("[" + skillId + "] トリガー（CAST）が定義されていません");
        }

        // 結果を出力
        if (!errors.isEmpty()) {
            logger.warning("[" + skillId + "] スキルバリデーションエラー:");
            for (String error : errors) {
                logger.warning("  - " + error);
            }
        }

        if (!warnings.isEmpty()) {
            logger.info("[" + skillId + "] スキルバリデーション警告:");
            for (String warning : warnings) {
                logger.info("  - " + warning);
            }
        }

        return errors.isEmpty();
    }

    /**
     * 子コンポーネントを再帰的に検証します
     *
     * @param skillId スキルID
     * @param section コンポーネントセクション
     * @param parentType 親コンポーネントタイプ
     * @param depth 階層深度
     */
    private void validateChildren(String skillId, ConfigurationSection section, ComponentType parentType, int depth) {
        if (depth > 20) {
            errors.add("[" + skillId + "] コンポーネントの階層が深すぎます（最大20）");
            return;
        }

        // 子キーの判定（components または children）
        String childKey = section.contains("components") ? "components" : "children";
        if (!section.contains(childKey)) {
            return;
        }

        List<?> childList = section.getList(childKey);
        if (childList == null) {
            return;
        }

        for (Object obj : childList) {
            if (!(obj instanceof ConfigurationSection)) {
                continue;
            }

            ConfigurationSection childSection = (ConfigurationSection) obj;
            ComponentType childType = parseType(childSection);

            if (childType == null) {
                errors.add("[" + skillId + "] 子コンポーネントのタイプが指定されていません");
                continue;
            }

            // 親子関係の検証
            if (!PlacementRules.canPlace(parentType, childType)) {
                errors.add("[" + skillId + "] 無効な配置: " + parentType + " の子に " + childType + " は配置できません");
                continue;
            }

            // 再帰的に子を検証
            validateChildren(skillId, childSection, childType, depth + 1);
        }
    }

    /**
     * セクションからコンポーネントタイプを解析します
     *
     * @param section コンポーネントセクション
     * @return コンポーネントタイプ
     */
    private ComponentType parseType(ConfigurationSection section) {
        if (section.contains("trigger")) {
            return ComponentType.TRIGGER;
        } else if (section.contains("target")) {
            return ComponentType.TARGET;
        } else if (section.contains("filter")) {
            return ComponentType.FILTER;
        } else if (section.contains("condition")) {
            return ComponentType.CONDITION;
        } else if (section.contains("mechanic")) {
            return ComponentType.MECHANIC;
        } else if (section.contains("cost")) {
            return ComponentType.COST;
        } else if (section.contains("cooldown")) {
            return ComponentType.COOLDOWN;
        }
        return null;
    }

    /**
     * エラーリストを取得します
     *
     * @return エラーリストのコピー
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * 警告リストを取得します
     *
     * @return 警告リストのコピー
     */
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }

    /**
     * 検証結果が有効かどうか
     *
     * @return 有効な場合はtrue
     */
    public boolean isValid() {
        return errors.isEmpty();
    }
}
