package com.example.rpgplugin.skill.component;

import com.example.rpgplugin.skill.component.trigger.ComponentRegistry;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.logging.Logger;

/**
 * コンポーネントローダー
 * <p>YAML設定からコンポーネントツリーを構築します</p>
 *
 * <p>YAMLフォーマット:</p>
 * <pre>
 * components:
 *   - type: DAMAGE
 *     value: "10 + level * 2"
 *     stat_multiplier: STRENGTH
 *     multiplier: 1.5
 *   - type: FIRE
 *     duration: 100
 *   - type: CONDITION
 *     type: health
 *     min: 0.5
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ComponentLoader {

    private final Logger logger;

    /**
     * コンストラクタ
     *
     * @param logger ロガー
     */
    public ComponentLoader(Logger logger) {
        this.logger = logger;
    }

    /**
     * コンポーネントリストをロードします
     *
     * @param componentsSection コンポーネント設定セクション
     * @return コンポーネントリスト
     */
    public List<EffectComponent> loadComponents(ConfigurationSection componentsSection) {
        List<EffectComponent> components = new ArrayList<>();

        if (componentsSection == null) {
            return components;
        }

        // リスト形式のコンポーネント定義
        List<?> componentList = componentsSection.getList("components");
        if (componentList != null && !componentList.isEmpty()) {
            return loadComponentsFromList(componentList);
        }

        // 単一セクションとしてのコンポーネント定義
        for (String key : componentsSection.getKeys(false)) {
            ConfigurationSection section = componentsSection.getConfigurationSection(key);
            if (section == null) {
                // 単純な値の場合
                Object value = componentsSection.get(key);
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) value;
                    EffectComponent component = loadComponentFromMap(key, map);
                    if (component != null) {
                        components.add(component);
                    }
                }
            } else {
                EffectComponent component = loadComponent(key, section);
                if (component != null) {
                    components.add(component);
                }
            }
        }

        return components;
    }

    /**
     * リストからコンポーネントをロードします
     *
     * @param list コンポーネント定義リスト
     * @return コンポーネントリスト
     */
    private List<EffectComponent> loadComponentsFromList(List<?> list) {
        List<EffectComponent> components = new ArrayList<>();

        for (Object item : list) {
            EffectComponent component = null;

            if (item instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) item;
                String type = getString(map, "type", "");
                component = loadComponentFromMap(type, map);
            } else if (item instanceof String) {
                // 文字列のみの場合はタイプとして扱う
                component = ComponentRegistry.createComponent((String) item);
            }

            if (component != null) {
                components.add(component);
            }
        }

        return components;
    }

    /**
     * マップからコンポーネントをロードします
     *
     * @param type コンポーネントタイプ
     * @param map 設定マップ
     * @return コンポーネントインスタンス、見つからない場合はnull
     */
    @SuppressWarnings("unchecked")
    private EffectComponent loadComponentFromMap(String type, Map<String, Object> map) {
        EffectComponent component = ComponentRegistry.createComponent(type);
        if (component == null) {
            logger.warning("Unknown component type: " + type);
            return null;
        }

        // 設定をロード
        ComponentSettings settings = component.getSettings();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if ("type".equals(entry.getKey())) {
                continue; // typeはスキップ
            }
            settings.put(entry.getKey(), entry.getValue());
        }

        // 子コンポーネントをロード
        Object children = map.get("components");
        if (children instanceof List) {
            List<?> childList = (List<?>) children;
            for (Object child : childList) {
                if (child instanceof Map) {
                    Map<String, Object> childMap = (Map<String, Object>) child;
                    String childType = getString(childMap, "type", "");
                    EffectComponent childComponent = loadComponentFromMap(childType, childMap);
                    if (childComponent != null) {
                        component.addChild(childComponent);
                    }
                }
            }
        }

        return component;
    }

    /**
     * 設定セクションからコンポーネントをロードします
     *
     * @param type コンポーネントタイプ
     * @param section 設定セクション
     * @return コンポーネントインスタンス、見つからない場合はnull
     */
    private EffectComponent loadComponent(String type, ConfigurationSection section) {
        EffectComponent component = ComponentRegistry.createComponent(type);
        if (component == null) {
            logger.warning("Unknown component type: " + type);
            return null;
        }

        // 設定をロード
        ComponentSettings settings = component.getSettings();
        settings.load(section);

        // 子コンポーネントをロード
        List<?> childList = section.getList("components");
        if (childList != null && !childList.isEmpty()) {
            for (Object child : childList) {
                if (child instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> childMap = (Map<String, Object>) child;
                    String childType = getString(childMap, "type", "");
                    EffectComponent childComponent = loadComponentFromMap(childType, childMap);
                    if (childComponent != null) {
                        component.addChild(childComponent);
                    }
                }
            }
        }

        return component;
    }

    /**
     * スキルエフェクトをロードします
     *
     * @param skillId スキルID
     * @param componentsSection コンポーネント設定セクション
     * @return スキルエフェクト
     */
    public SkillEffect loadSkillEffect(String skillId, ConfigurationSection componentsSection) {
        SkillEffect skillEffect = new SkillEffect(skillId);

        if (componentsSection == null) {
            return skillEffect;
        }

        List<EffectComponent> components = loadComponents(componentsSection);
        for (EffectComponent component : components) {
            skillEffect.addComponent(component);
        }

        return skillEffect;
    }

    /**
     * マップから文字列値を取得します
     *
     * @param map マップ
     * @param key キー
     * @param fallback デフォルト値
     * @return 文字列値
     */
    private String getString(Map<String, Object> map, String key, String fallback) {
        Object value = map.get(key);
        return value != null ? value.toString() : fallback;
    }

    /**
     * コンポーネント定義を検証します
     *
     * @param componentsSection コンポーネント設定セクション
     * @return 検証結果
     */
    public ValidationResult validate(ConfigurationSection componentsSection) {
        ValidationResult result = new ValidationResult();

        if (componentsSection == null) {
            return result; // 空は有効
        }

        validateRecursive(componentsSection, result, new ArrayList<>());
        return result;
    }

    /**
     * 再帰的にコンポーネント定義を検証します
     *
     * @param section 設定セクション
     * @param result 検証結果
     * @param path パス（循環検出用）
     */
    @SuppressWarnings("unchecked")
    private void validateRecursive(ConfigurationSection section, ValidationResult result, List<String> path) {
        List<?> componentList = section.getList("components");
        if (componentList == null || componentList.isEmpty()) {
            return;
        }

        for (Object item : componentList) {
            if (!(item instanceof Map)) {
                continue;
            }

            Map<String, Object> map = (Map<String, Object>) item;
            String type = getString(map, "type", "");

            if (type.isEmpty()) {
                result.addError("Component missing 'type' field at path: " + path);
                continue;
            }

            if (!ComponentRegistry.hasComponent(type)) {
                result.addWarning("Unknown component type: " + type + " at path: " + path);
            }

            // 子コンポーネントを検証
            Object children = map.get("components");
            if (children instanceof List) {
                List<?> childList = (List<?>) children;
                List<String> newPath = new ArrayList<>(path);
                newPath.add(type);

                if (newPath.size() > 50) {
                    result.addError("Component tree too deep (possible circular reference) at path: " + path);
                    return;
                }

                for (Object child : childList) {
                    if (child instanceof Map) {
                        validateRecursive(createSectionFromMap((Map<String, Object>) child), result, newPath);
                    }
                }
            }
        }
    }

    /**
     * マップから設定セクションを作成します
     *
     * @param map マップ
     * @return 設定セクション
     */
    private ConfigurationSection createSectionFromMap(Map<String, Object> map) {
        org.bukkit.configuration.MemorySection section = new org.bukkit.configuration.MemorySection() {
            @Override
            public Map<String, Object> getValues(boolean deep) {
                return new HashMap<>(map);
            }
        };
        return section;
    }

    /**
     * 検証結果
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        public void addError(String error) {
            errors.add(error);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public String getSummary() {
            return "ValidationResult{errors=" + errors.size() + ", warnings=" + warnings.size() + "}";
        }
    }
}
