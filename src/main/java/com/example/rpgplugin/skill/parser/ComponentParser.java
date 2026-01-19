package com.example.rpgplugin.skill.parser;

import com.example.rpgplugin.skill.SkillType;
import com.example.rpgplugin.skill.component.*;
import com.example.rpgplugin.skill.component.trigger.ComponentRegistry;
import com.example.rpgplugin.skill.component.trigger.Trigger;
import com.example.rpgplugin.skill.component.trigger.TriggerHandler;
import com.example.rpgplugin.skill.component.trigger.TriggerManager;
import com.example.rpgplugin.skill.component.trigger.TriggerSettings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * コンポーネントパーサ
 *
 * <p>スキルのコンポーネント設定の解析を担当します。</p>
 */
public class ComponentParser {

    private final Logger logger;
    private final SkillValidator validator;

    public ComponentParser(Logger logger, SkillValidator validator) {
        this.logger = logger;
        this.validator = validator;
    }

    /**
     * コンポーネントを解析します
     *
     * @param config  設定
     * @param skillId スキルID
     * @return スキルエフェクト
     */
    public SkillEffect parse(FileConfiguration config, String skillId) {
        if (!config.contains("components")) {
            return null;
        }

        ConfigurationSection section = prepareSection(config);
        if (section == null) {
            return null;
        }

        validatePlacement(skillId, section);

        SkillEffect effect = new SkillEffect(skillId);
        parseTriggers(section, skillId, effect);
        parseComponentList(section, skillId, effect);

        return effect;
    }

    private ConfigurationSection prepareSection(FileConfiguration config) {
        if (config.isList("components")) {
            org.bukkit.configuration.MemoryConfiguration tempSection = new org.bukkit.configuration.MemoryConfiguration();
            tempSection.set("components", config.getList("components"));
            return tempSection;
        }
        return config.getConfigurationSection("components");
    }

    private void validatePlacement(String skillId, ConfigurationSection section) {
        com.example.rpgplugin.skill.component.placement.SkillValidator placementValidator =
                new com.example.rpgplugin.skill.component.placement.SkillValidator(logger);
        if (!placementValidator.validate(skillId, section)) {
            logger.warning("スキル " + skillId + " のコンポーネント配置バリデーションに失敗しました");
        }
    }

    private void parseTriggers(ConfigurationSection section, String skillId, SkillEffect effect) {
        List<?> triggerList = section.getList("triggers");
        if (triggerList == null) {
            return;
        }

        for (Object triggerObj : triggerList) {
            ConfigurationSection triggerSection = convertToSection(triggerObj);
            if (triggerSection == null) {
                continue;
            }

            TriggerHandler handler = parseTriggerComponent(triggerSection, skillId);
            if (handler != null) {
                TriggerManager.getInstance().registerSkill(skillId, effect);
            }
        }
    }

    private void parseComponentList(ConfigurationSection section, String skillId, SkillEffect effect) {
        List<?> componentList = section.getList("components");
        if (componentList == null) {
            return;
        }

        for (Object componentObj : componentList) {
            ConfigurationSection componentSection = convertToSection(componentObj);
            if (componentSection == null) {
                continue;
            }

            EffectComponent component = parseComponent(componentSection, skillId);
            if (component != null) {
                effect.addComponent(component);
            }
        }
    }

    private ConfigurationSection convertToSection(Object obj) {
        if (obj instanceof ConfigurationSection) {
            return (ConfigurationSection) obj;
        }
        if (obj instanceof Map) {
            org.bukkit.configuration.MemoryConfiguration memoryConfig = new org.bukkit.configuration.MemoryConfiguration();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                memoryConfig.set(entry.getKey(), entry.getValue());
            }
            return memoryConfig;
        }
        return null;
    }

    private TriggerHandler parseTriggerComponent(ConfigurationSection section, String skillId) {
        String key = section.getString("trigger");
        if (key == null) {
            logger.warning("トリガーキーが指定されていません: " + skillId);
            return null;
        }

        Trigger<?> trigger = ComponentRegistry.createTrigger(key);
        if (trigger == null) {
            logger.warning("不明なトリガー: " + key);
            return null;
        }

        TriggerSettings triggerSettings = new TriggerSettings();
        for (String settingKey : section.getKeys(false)) {
            if (isExcludedKey(settingKey)) {
                continue;
            }
            Object value = section.get(settingKey);
            if (value != null) {
                triggerSettings.put(settingKey, value);
            }
        }

        int duration = section.getInt("duration", 0);
        EffectComponent rootComponent = createRootComponent(section, skillId);

        return new TriggerHandler(skillId, trigger, triggerSettings, rootComponent, duration);
    }

    private EffectComponent createRootComponent(ConfigurationSection section, String skillId) {
        EffectComponent rootComponent = new EffectComponent("trigger_root") {
            @Override
            public ComponentType getType() {
                return ComponentType.MECHANIC;
            }

            @Override
            public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
                return executeChildren(caster, level, targets);
            }
        };

        if (!section.contains("components")) {
            return rootComponent;
        }

        List<?> childList = section.getList("components");
        if (childList == null) {
            return rootComponent;
        }

        for (Object childObj : childList) {
            ConfigurationSection childSection = convertToSection(childObj);
            if (childSection == null) {
                continue;
            }

            EffectComponent child = parseComponent(childSection, skillId);
            if (child != null) {
                rootComponent.addChild(child);
            }
        }

        return rootComponent;
    }

    /**
     * コンポーネントを解析します
     *
     * @param section 設定セクション
     * @param skillId スキルID
     * @return エフェクトコンポーネント
     */
    public EffectComponent parseComponent(ConfigurationSection section, String skillId) {
        ComponentTypeResult typeResult = determineComponentType(section, skillId);
        if (typeResult == null) {
            return null;
        }

        EffectComponent component = createComponent(typeResult.type, typeResult.key);
        if (component == null) {
            logger.warning("不明なコンポーネント: type=" + typeResult.type + ", key=" + typeResult.key + ", skillId=" + skillId);
            return null;
        }

        applySettings(component, section);
        parseChildren(component, section, skillId);

        return component;
    }

    private ComponentTypeResult determineComponentType(ConfigurationSection section, String skillId) {
        if (section.contains("condition")) {
            return new ComponentTypeResult("condition", section.getString("condition"));
        } else if (section.contains("filter")) {
            return new ComponentTypeResult("filter", section.getString("filter"));
        } else if (section.contains("mechanic")) {
            return new ComponentTypeResult("mechanic", section.getString("mechanic"));
        } else if (section.contains("target")) {
            return new ComponentTypeResult("target", section.getString("target"));
        } else if (section.contains("cost")) {
            return new ComponentTypeResult("cost", section.getString("cost"));
        } else if (section.contains("cooldown")) {
            return new ComponentTypeResult("cooldown", section.getString("cooldown"));
        }

        logger.warning("コンポーネントタイプが指定されていません: " + skillId);
        return null;
    }

    private EffectComponent createComponent(String type, String key) {
        return switch (type) {
            case "condition" -> ComponentRegistry.createCondition(key);
            case "filter" -> ComponentRegistry.createFilter(key);
            case "mechanic" -> ComponentRegistry.createMechanic(key);
            case "target" -> ComponentRegistry.createTarget(key);
            case "cost" -> ComponentRegistry.createCost(key);
            case "cooldown" -> ComponentRegistry.createCooldown(key);
            default -> null;
        };
    }

    private void applySettings(EffectComponent component, ConfigurationSection section) {
        ComponentSettings settings = new ComponentSettings();
        for (String key : section.getKeys(false)) {
            if (isExcludedKey(key)) {
                continue;
            }
            Object value = section.get(key);
            if (value != null) {
                settings.put(key, value);
            }
        }
        if (component.getSettings() != null) {
            component.getSettings().putAll(settings);
        }
    }

    private boolean isExcludedKey(String key) {
        return "trigger".equals(key) || "condition".equals(key) || "mechanic".equals(key)
                || "target".equals(key) || "cost".equals(key) || "cooldown".equals(key)
                || "components".equals(key) || "children".equals(key) || "skill_id".equals(key)
                || "filter".equals(key);
    }

    private void parseChildren(EffectComponent component, ConfigurationSection section, String skillId) {
        String childKey = section.contains("components") ? "components" : "children";
        if (!section.contains(childKey)) {
            return;
        }

        List<?> childList = section.getList(childKey);
        if (childList == null) {
            return;
        }

        for (Object childObj : childList) {
            ConfigurationSection childSection = convertToSection(childObj);
            if (childSection == null) {
                continue;
            }

            EffectComponent child = parseComponent(childSection, skillId);
            if (child != null) {
                component.addChild(child);
            }
        }
    }

    private static class ComponentTypeResult {
        final String type;
        final String key;

        ComponentTypeResult(String type, String key) {
            this.type = type;
            this.key = key;
        }
    }
}
