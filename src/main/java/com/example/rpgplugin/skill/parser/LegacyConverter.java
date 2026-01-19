package com.example.rpgplugin.skill.parser;

import com.example.rpgplugin.skill.LevelDependentParameter;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.skill.SkillType;
import com.example.rpgplugin.model.skill.DamageCalculation;
import com.example.rpgplugin.model.skill.FormulaDamageConfig;
import com.example.rpgplugin.model.skill.SkillTreeConfig;
import com.example.rpgplugin.model.skill.TargetingConfig;
import com.example.rpgplugin.model.skill.UnlockRequirement;
import com.example.rpgplugin.model.skill.VariableDefinition;
import com.example.rpgplugin.skill.target.SkillTarget;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * レガシー形式変換コンバータ
 *
 * <p>レガシー形式のスキル設定を新しい形式に変換します。</p>
 */
public class LegacyConverter {

    private final Logger logger;
    private final SkillValidator validator;

    public LegacyConverter(Logger logger, SkillValidator validator) {
        this.logger = logger;
        this.validator = validator;
    }

    /**
     * スキルタイプを解析します
     *
     * @param typeStr  タイプ文字列
     * @param fileName ファイル名
     * @return スキルタイプ
     */
    public SkillType parseSkillType(String typeStr, String fileName) {
        SkillType type = SkillType.fromId(typeStr);
        if (type == null) {
            logger.warning("無効なスキルタイプ: " + typeStr + " (" + fileName + ")");
        }
        return type;
    }

    /**
     * クールダウンパラメータを解析します
     *
     * @param config   設定
     * @param fileName ファイル名
     * @return レベル依存パラメータ
     */
    public LevelDependentParameter parseCooldownParameter(FileConfiguration config, String fileName) {
        if (!config.contains("cooldown")) {
            return null;
        }
        Object cooldownObj = config.get("cooldown");
        if (!(cooldownObj instanceof ConfigurationSection)) {
            return null;
        }
        return LevelDependentParameterParser.parse(config.getConfigurationSection("cooldown"), fileName, "cooldown", validator, logger);
    }

    /**
     * コストパラメータを解析します
     *
     * @param config   設定
     * @param fileName ファイル名
     * @return レベル依存パラメータ
     */
    public LevelDependentParameter parseCostParameter(FileConfiguration config, String fileName) {
        if (!config.contains("cost")) {
            return null;
        }
        ConfigurationSection costSection = config.getConfigurationSection("cost");
        if (costSection == null) {
            return null;
        }
        return LevelDependentParameterParser.parse(costSection, fileName, "cost", validator, logger);
    }

    /**
     * コストタイプを解析します
     *
     * @param config   設定
     * @param fileName ファイル名
     * @return コストタイプ
     */
    public SkillCostType parseCostType(FileConfiguration config, String fileName) {
        if (!config.contains("cost")) {
            return SkillCostType.MANA;
        }
        ConfigurationSection costSection = config.getConfigurationSection("cost");
        if (costSection == null) {
            return SkillCostType.MANA;
        }
        String costTypeStr = costSection.getString("type", "mana");
        SkillCostType costType = SkillCostType.fromId(costTypeStr);
        if (costType == null) {
            logger.warning("無効なコストタイプ: " + costTypeStr + " (" + fileName + ")");
            return SkillCostType.MANA;
        }
        return costType;
    }

    /**
     * ダメージ計算設定を解析します
     *
     * @param config   設定
     * @param fileName ファイル名
     * @return ダメージ計算設定
     */
    public DamageCalculation parseDamageCalculation(FileConfiguration config, String fileName) {
        if (!config.contains("damage")) {
            return null;
        }
        ConfigurationSection damageSection = config.getConfigurationSection("damage");
        if (damageSection == null || damageSection.contains("formula")) {
            return null;
        }
        return DamageCalculationParser.parse(damageSection, fileName, logger);
    }

    /**
     * 数式ダメージ設定を解析します
     *
     * @param config   設定
     * @param fileName ファイル名
     * @return 数式ダメージ設定
     */
    public FormulaDamageConfig parseFormulaDamage(FileConfiguration config, String fileName) {
        if (!config.contains("damage")) {
            return null;
        }
        ConfigurationSection damageSection = config.getConfigurationSection("damage");
        if (damageSection == null) {
            return null;
        }
        return FormulaDamageParser.parse(damageSection, fileName, validator, logger);
    }

    /**
     * アイコン素材を解析します
     *
     * @param config    設定
     * @param skillTree スキルツリー設定
     * @return アイコン素材名
     */
    public String parseIconMaterial(FileConfiguration config, SkillTreeConfig skillTree) {
        if (skillTree != null && skillTree.getIcon() != null) {
            return skillTree.getIcon();
        }
        if (config.contains("icon_material")) {
            return config.getString("icon_material", "DIAMOND_SWORD");
        }
        return "DIAMOND_SWORD";
    }

    /**
     * 変数定義リストを解析します
     *
     * @param config 設定
     * @return 変数定義リスト
     */
    public List<VariableDefinition> parseVariables(FileConfiguration config) {
        return VariableParser.parse(config, logger);
    }

    /**
     * ターゲティング設定を解析します
     *
     * @param config   設定
     * @param fileName ファイル名
     * @return ターゲティング設定
     */
    public TargetingConfig parseTargeting(FileConfiguration config, String fileName) {
        return TargetingParser.parse(config, fileName, validator, logger);
    }

    /**
     * スキルターゲットを解析します
     *
     * @param config   設定
     * @param fileName ファイル名
     * @return スキルターゲット
     */
    public SkillTarget parseSkillTarget(FileConfiguration config, String fileName) {
        return SkillTargetParser.parse(config, fileName, validator, logger);
    }

    /**
     * スキルツリー設定を解析します
     *
     * @param config   設定
     * @param fileName ファイル名
     * @return スキルツリー設定
     */
    public SkillTreeConfig parseSkillTree(FileConfiguration config, String fileName) {
        return SkillTreeParser.parse(config, fileName, logger);
    }

    /**
     * レベル依存パラメータパーサ
     */
    private static class LevelDependentParameterParser {

        static LevelDependentParameter parse(ConfigurationSection section, String fileName, String paramName,
                                              SkillValidator validator, Logger logger) {
            if (section == null) {
                return null;
            }

            double base = section.getDouble("base", 0.0);
            double perLevel = section.getDouble("per_level", 0.0);
            Double minValue = section.contains("min") ? section.getDouble("min") : null;
            Double maxValue = section.contains("max") ? section.getDouble("max") : null;

            if (minValue != null && maxValue != null && minValue > maxValue) {
                logger.warning(paramName + ".min が " + paramName + ".max より大きいです (" + fileName + ")");
            }

            return new LevelDependentParameter(base, perLevel, minValue, maxValue);
        }
    }

    /**
     * 変数パーサ
     */
    private static class VariableParser {

        static List<VariableDefinition> parse(FileConfiguration config, Logger logger) {
            List<VariableDefinition> variables = new ArrayList<>();

            if (!config.contains("variables")) {
                return variables;
            }

            ConfigurationSection variablesSection = config.getConfigurationSection("variables");
            if (variablesSection == null) {
                return variables;
            }

            for (String key : variablesSection.getKeys(false)) {
                double value = variablesSection.getDouble(key, 0.0);
                variables.add(new VariableDefinition(key, value));
            }

            return variables;
        }
    }

    /**
     * ダメージ計算パーサ
     */
    private static class DamageCalculationParser {

        static DamageCalculation parse(ConfigurationSection section, String fileName, Logger logger) {
            if (section == null) {
                return null;
            }

            double base = section.getDouble("base", 0.0);

            if (!section.contains("stat_multiplier")) {
                return null;
            }

            ConfigurationSection statSection = section.getConfigurationSection("stat_multiplier");
            String statName = statSection != null ? statSection.getString("stat") : null;
            double multiplier = statSection != null ? statSection.getDouble("multiplier", 1.0) : 1.0;

            com.example.rpgplugin.stats.Stat stat = null;
            if (statName != null) {
                stat = com.example.rpgplugin.stats.Stat.fromShortName(statName);
                if (stat == null) {
                    stat = com.example.rpgplugin.stats.Stat.fromDisplayName(statName);
                }
            }

            double levelMultiplier = section.getDouble("level_multiplier", 0.0);

            return new DamageCalculation(base, stat, multiplier, levelMultiplier);
        }
    }

    /**
     * 数式ダメージパーサ
     */
    private static class FormulaDamageParser {

        static FormulaDamageConfig parse(ConfigurationSection section, String fileName,
                                               SkillValidator validator, Logger logger) {
            if (section == null) {
                return null;
            }

            String formula = section.getString("formula");
            if (formula == null || formula.trim().isEmpty()) {
                return null;
            }

            if (!validator.validateFormulaSyntax(formula)) {
                logger.warning("無効な数式です: " + formula + " (" + fileName + ")");
                return null;
            }

            Map<Integer, String> levelFormulas = parseLevelFormulas(section, fileName, validator, logger);

            return new FormulaDamageConfig(formula, levelFormulas);
        }

        private static Map<Integer, String> parseLevelFormulas(ConfigurationSection section, String fileName,
                                                               SkillValidator validator, Logger logger) {
            Map<Integer, String> levelFormulas = new HashMap<>();

            if (!section.contains("levels")) {
                return levelFormulas;
            }

            ConfigurationSection levelsSection = section.getConfigurationSection("levels");
            if (levelsSection == null) {
                return levelFormulas;
            }

            for (String levelKey : levelsSection.getKeys(false)) {
                try {
                    int level = Integer.parseInt(levelKey);
                    String levelFormula = levelsSection.getString(levelKey);
                    if (levelFormula != null && validator.validateFormulaSyntax(levelFormula)) {
                        levelFormulas.put(level, levelFormula);
                    }
                } catch (NumberFormatException e) {
                    logger.warning("無効なレベルキーです: " + levelKey + " (" + fileName + ")");
                }
            }

            return levelFormulas;
        }
    }

    /**
     * ターゲティングパーサ
     */
    private static class TargetingParser {

        static TargetingConfig parse(FileConfiguration config, String fileName,
                                           SkillValidator validator, Logger logger) {
            if (!config.contains("targeting")) {
                return null;
            }

            ConfigurationSection targetingSection = config.getConfigurationSection("targeting");
            if (targetingSection == null) {
                return null;
            }

            String type = targetingSection.getString("type", "single");
            TargetingConfig.TargetingParams params = parseParams(targetingSection, type, fileName, validator, logger);

            return new TargetingConfig(type, params);
        }

        private static TargetingConfig.TargetingParams parseParams(ConfigurationSection section, String type,
                                                                         String fileName, SkillValidator validator, Logger logger) {
            return switch (type.toLowerCase()) {
                case "cone" -> parseConeParams(section, fileName, validator, logger);
                case "sphere", "radius" -> parseSphereParams(section, fileName, validator, logger);
                case "sector" -> parseSectorParams(section, fileName, validator, logger);
                default -> null;
            };
        }

        private static TargetingConfig.ConeParams parseConeParams(ConfigurationSection section, String fileName,
                                                                         SkillValidator validator, Logger logger) {
            ConfigurationSection coneSection = section.getConfigurationSection("cone");
            if (coneSection == null) {
                coneSection = section;
            }

            double angle = coneSection.getDouble("angle", 90.0);
            double range = coneSection.getDouble("range", 5.0);

            validator.validateRange(angle, 1.0, 360.0, "targeting.cone.angle", fileName);
            validator.validateRange(range, 0.1, 100.0, "targeting.cone.range", fileName);

            return new TargetingConfig.ConeParams(angle, range);
        }

        private static TargetingConfig.SphereParams parseSphereParams(ConfigurationSection section, String fileName,
                                                                            SkillValidator validator, Logger logger) {
            ConfigurationSection sphereSection = section.getConfigurationSection("sphere");
            if (sphereSection == null) {
                sphereSection = section.getConfigurationSection("radius");
                if (sphereSection == null) {
                    sphereSection = section;
                }
            }

            double radius = sphereSection.getDouble("radius", sphereSection.getDouble("range", 5.0));
            validator.validateRange(radius, 0.1, 100.0, "targeting.sphere.radius", fileName);

            return new TargetingConfig.SphereParams(radius);
        }

        private static TargetingConfig.SectorParams parseSectorParams(ConfigurationSection section, String fileName,
                                                                             SkillValidator validator, Logger logger) {
            ConfigurationSection sectorSection = section.getConfigurationSection("sector");
            if (sectorSection == null) {
                sectorSection = section;
            }

            double angle = sectorSection.getDouble("angle", 90.0);
            double radius = sectorSection.getDouble("radius", 5.0);

            validator.validateRange(angle, 1.0, 360.0, "targeting.sector.angle", fileName);
            validator.validateRange(radius, 0.1, 100.0, "targeting.sector.radius", fileName);

            return new TargetingConfig.SectorParams(angle, radius);
        }
    }

    /**
     * スキルターゲットパーサ
     */
    private static class SkillTargetParser {

        static SkillTarget parse(FileConfiguration config, String fileName, SkillValidator validator, Logger logger) {
            if (!config.contains("target")) {
                return null;
            }

            ConfigurationSection targetSection = config.getConfigurationSection("target");
            if (targetSection == null) {
                return null;
            }

            var type = com.example.rpgplugin.skill.target.TargetType.fromId(targetSection.getString("type", "nearest_hostile"))
                    .orElse(com.example.rpgplugin.skill.target.TargetType.NEAREST_HOSTILE);

            var areaShape = com.example.rpgplugin.skill.target.AreaShape.fromId(targetSection.getString("area_shape", "single"))
                    .orElse(com.example.rpgplugin.skill.target.AreaShape.SINGLE);

            var singleTarget = parseSingleTarget(targetSection, fileName, logger);
            var cone = parseConeConfig(targetSection, fileName, logger);
            var rect = parseRectConfig(targetSection, fileName, logger);
            var circle = parseCircleConfig(targetSection, fileName, logger);

            TargetParams params = parseTargetParams(targetSection, fileName, logger);
            var entityTypeFilter = parseEntityTypeFilter(targetSection);
            Integer maxTargets = targetSection.contains("max_targets") ? targetSection.getInt("max_targets") : null;
            var groupFilter = parseTargetGroupFilter(targetSection);
            boolean throughWall = targetSection.getBoolean("wall", false);
            boolean randomOrder = targetSection.getBoolean("random", false);
            boolean includeCaster = targetSection.getBoolean("caster", false);

            return new SkillTarget(
                    type, areaShape, singleTarget, cone, rect, circle,
                    entityTypeFilter, maxTargets, groupFilter, throughWall, randomOrder, includeCaster,
                    params.range, params.lineWidth, params.coneAngle, params.sphereRadius);
        }

        private static SkillTarget.SingleTargetConfig parseSingleTarget(ConfigurationSection section, String fileName, Logger logger) {
            if (!section.contains("single")) {
                return null;
            }
            ConfigurationSection singleSection = section.getConfigurationSection("single");
            if (singleSection == null) {
                return null;
            }
            boolean selectNearest = singleSection.getBoolean("select_nearest", true);
            boolean targetSelf = singleSection.getBoolean("target_self", false);
            return new SkillTarget.SingleTargetConfig(selectNearest, targetSelf);
        }

        private static SkillTarget.ConeConfig parseConeConfig(ConfigurationSection section, String fileName, Logger logger) {
            if (!section.contains("cone")) {
                return null;
            }
            ConfigurationSection coneSection = section.getConfigurationSection("cone");
            if (coneSection == null) {
                return null;
            }
            double angle = coneSection.getDouble("angle", 90.0);
            double range = coneSection.getDouble("range", 5.0);
            return new SkillTarget.ConeConfig(angle, range);
        }

        private static SkillTarget.RectConfig parseRectConfig(ConfigurationSection section, String fileName, Logger logger) {
            if (!section.contains("rect")) {
                return null;
            }
            ConfigurationSection rectSection = section.getConfigurationSection("rect");
            if (rectSection == null) {
                return null;
            }
            double width = rectSection.getDouble("width", 3.0);
            double depth = rectSection.getDouble("depth", 10.0);
            return new SkillTarget.RectConfig(width, depth);
        }

        private static SkillTarget.CircleConfig parseCircleConfig(ConfigurationSection section, String fileName, Logger logger) {
            if (!section.contains("circle")) {
                return null;
            }
            ConfigurationSection circleSection = section.getConfigurationSection("circle");
            if (circleSection == null) {
                return null;
            }
            double radius = circleSection.getDouble("radius", 5.0);
            return new SkillTarget.CircleConfig(radius);
        }

        private static TargetParams parseTargetParams(ConfigurationSection section, String fileName, Logger logger) {
            Double range = null;
            Double lineWidth = null;
            Double coneAngle = null;
            Double sphereRadius = null;

            if (section.contains("range")) {
                range = section.getDouble("range");
            }
            if (section.contains("line_width")) {
                lineWidth = section.getDouble("line_width");
            }
            if (section.contains("cone_angle")) {
                coneAngle = section.getDouble("cone_angle");
            }
            if (section.contains("sphere_radius")) {
                sphereRadius = section.getDouble("sphere_radius");
            }

            if (sphereRadius == null && section.contains("sphere")) {
                ConfigurationSection sphereSection = section.getConfigurationSection("sphere");
                if (sphereSection != null) {
                    sphereRadius = sphereSection.getDouble("radius", 5.0);
                }
            }

            return new TargetParams(range, lineWidth, coneAngle, sphereRadius);
        }

        private static com.example.rpgplugin.skill.target.EntityTypeFilter parseEntityTypeFilter(ConfigurationSection section) {
            if (!section.contains("filter")) {
                return com.example.rpgplugin.skill.target.EntityTypeFilter.ALL;
            }
            String filterStr = section.getString("filter", "all").toLowerCase();
            return switch (filterStr) {
                case "player", "players" -> com.example.rpgplugin.skill.target.EntityTypeFilter.PLAYER_ONLY;
                case "mob", "mobs", "hostile" -> com.example.rpgplugin.skill.target.EntityTypeFilter.MOB_ONLY;
                default -> com.example.rpgplugin.skill.target.EntityTypeFilter.ALL;
            };
        }

        private static com.example.rpgplugin.skill.target.TargetGroupFilter parseTargetGroupFilter(ConfigurationSection section) {
            if (!section.contains("group")) {
                return com.example.rpgplugin.skill.target.TargetGroupFilter.BOTH;
            }
            return com.example.rpgplugin.skill.target.TargetGroupFilter.fromId(section.getString("group", "both").toLowerCase());
        }

        private static class TargetParams {
            final Double range;
            final Double lineWidth;
            final Double coneAngle;
            final Double sphereRadius;

            TargetParams(Double range, Double lineWidth, Double coneAngle, Double sphereRadius) {
                this.range = range;
                this.lineWidth = lineWidth;
                this.coneAngle = coneAngle;
                this.sphereRadius = sphereRadius;
            }
        }
    }

    /**
     * スキルツリーパーサ
     */
    private static class SkillTreeParser {

        static SkillTreeConfig parse(FileConfiguration config, String fileName, Logger logger) {
            if (!config.contains("skill_tree")) {
                return null;
            }

            ConfigurationSection section = config.getConfigurationSection("skill_tree");
            if (section == null) {
                return null;
            }

            String parent = section.getString("parent", "none");
            int cost = section.getInt("cost", 1);
            String icon = section.getString("icon", null);

            List<UnlockRequirement> requirements = parseUnlockRequirements(section, fileName, logger);

            return new SkillTreeConfig(parent, requirements, cost, icon);
        }

        private static List<UnlockRequirement> parseUnlockRequirements(ConfigurationSection section, String fileName, Logger logger) {
            List<UnlockRequirement> requirements = new ArrayList<>();

            if (!section.contains("unlock_requirements")) {
                return requirements;
            }

            List<?> requirementList = section.getList("unlock_requirements");
            if (requirementList == null) {
                return requirements;
            }

            for (Object reqObj : requirementList) {
                UnlockRequirement requirement = parseRequirement(reqObj, fileName, logger);
                if (requirement != null) {
                    requirements.add(requirement);
                }
            }

            return requirements;
        }

        private static UnlockRequirement parseRequirement(Object reqObj, String fileName, Logger logger) {
            if (reqObj instanceof ConfigurationSection) {
                return parseRequirementFromSection((ConfigurationSection) reqObj, fileName, logger);
            }
            if (reqObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> reqMap = (Map<String, Object>) reqObj;
                return parseRequirementFromMap(reqMap, fileName, logger);
            }
            return null;
        }

        private static UnlockRequirement parseRequirementFromSection(ConfigurationSection section, String fileName, Logger logger) {
            String type = section.getString("type");
            if (type == null) {
                logger.warning("unlock_requirements type is missing in " + fileName);
                return null;
            }

            com.example.rpgplugin.stats.Stat stat = null;
            if ("stat".equals(type)) {
                String statName = section.getString("stat");
                if (statName != null) {
                    stat = com.example.rpgplugin.stats.Stat.fromShortName(statName);
                    if (stat == null) {
                        stat = com.example.rpgplugin.stats.Stat.fromDisplayName(statName);
                    }
                }
            }

            double value = section.getDouble("value", 0.0);
            return new UnlockRequirement(type, stat, value);
        }

        private static UnlockRequirement parseRequirementFromMap(Map<String, Object> reqMap, String fileName, Logger logger) {
            if (reqMap == null) {
                return null;
            }

            Object typeObj = reqMap.get("type");
            if (typeObj == null) {
                logger.warning("unlock_requirements type is missing in " + fileName);
                return null;
            }

            String type = typeObj.toString();
            com.example.rpgplugin.stats.Stat stat = null;

            if ("stat".equals(type)) {
                Object statNameObj = reqMap.get("stat");
                if (statNameObj != null) {
                    String statName = statNameObj.toString();
                    stat = com.example.rpgplugin.stats.Stat.fromShortName(statName);
                    if (stat == null) {
                        stat = com.example.rpgplugin.stats.Stat.fromDisplayName(statName);
                    }
                }
            }

            double value = 0.0;
            Object valueObj = reqMap.get("value");
            if (valueObj instanceof Number) {
                value = ((Number) valueObj).doubleValue();
            }

            return new UnlockRequirement(type, stat, value);
        }
    }
}
