package com.example.rpgplugin.skill;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.core.config.ConfigLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * スキルYAMLローダー
 *
 * <p>skills/ディレクトリからYAMLファイルを読み込み、スキルデータをパースします。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキルYAMLのロードに専念</li>
 *   <li>DRY: バリデーションロジックを再利用</li>
 *   <li>KISS: シンプルなパース処理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillLoader extends ConfigLoader {

    private final RPGPlugin plugin;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public SkillLoader(RPGPlugin plugin) {
        super(plugin.getLogger(), plugin.getDataFolder());
        this.plugin = plugin;
    }

    /**
     * スキルディレクトリから全スキルを読み込みます
     *
     * @return 読み込んだスキルのリスト
     */
    public List<Skill> loadAllSkills() {
        List<Skill> skills = new ArrayList<>();
        File skillsDir = new File(plugin.getDataFolder(), "skills");

        if (!skillsDir.exists()) {
            skillsDir.mkdirs();
            plugin.getLogger().info("スキルディレクトリを作成しました: " + skillsDir.getPath());
            return skills;
        }

        List<File> yamlFiles = getYamlFiles("skills", true);
        if (yamlFiles.isEmpty()) {
            plugin.getLogger().warning("スキルファイルが見つかりません: " + skillsDir.getPath());
            return skills;
        }

        for (File file : yamlFiles) {
            try {
                Skill skill = loadSkill(file);
                if (skill != null) {
                    skills.add(skill);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "スキルの読み込みに失敗しました: " + file.getName(), e);
            }
        }

        plugin.getLogger().info(skills.size() + " 個のスキルを読み込みました");
        return skills;
    }

    /**
     * 単一のスキルファイルを読み込みます
     *
     * @param file YAMLファイル
     * @return パースされたスキル、失敗した場合はnull
     */
    public Skill loadSkill(File file) {
        FileConfiguration config = loadYaml(file);
        if (config == null) {
            return null;
        }

        try {
            // 必須フィールドのバリデーション
            if (!validateRequired(config, List.of("id", "name", "type"))) {
                getLogger().warning("必須フィールドが不足しています: " + file.getName());
                return null;
            }

            String id = config.getString("id");
            String name = config.getString("name");
            String displayName = config.getString("display_name", name);
            String typeStr = config.getString("type");

            SkillType type = SkillType.fromId(typeStr);
            if (type == null) {
                getLogger().warning("無効なスキルタイプ: " + typeStr + " (" + file.getName() + ")");
                return null;
            }

            // 説明
            List<String> description = config.getStringList("description");

            // レベルとコスト
            int maxLevel = config.getInt("max_level", 5);
            validateRange(maxLevel, 1, 100, "max_level", file.getName());

            // Phase11-6: カスタム変数のパース
            List<Skill.VariableDefinition> variables = parseVariables(config, file.getName());

            // レベル依存パラメータのパース（Phase11-2で追加）
            LevelDependentParameter cooldownParameter = null;
            LevelDependentParameter costParameter = null;
            SkillCostType costType = SkillCostType.MANA;

            if (config.contains("cooldown")) {
                Object cooldownObj = config.get("cooldown");
                if (cooldownObj instanceof ConfigurationSection) {
                    // cooldownセクション形式: { base: 5.0, per_level: -0.5, min: 1.0 }
                    cooldownParameter = parseLevelDependentParameter(
                            config.getConfigurationSection("cooldown"), file.getName(), "cooldown");
                }
                // 数値形式の場合はフォールバック値として後で取得
            }

            // コスト関連のパース
            if (config.contains("cost")) {
                // 新しいcostセクション形式
                ConfigurationSection costSection = config.getConfigurationSection("cost");
                if (costSection != null) {
                    costParameter = parseLevelDependentParameter(costSection, file.getName(), "cost");
                    String costTypeStr = costSection.getString("type", "mana");
                    costType = SkillCostType.fromId(costTypeStr);
                    if (costType == null) {
                        getLogger().warning("無効なコストタイプ: " + costTypeStr + " (" + file.getName() + ")");
                        costType = SkillCostType.MANA;
                    }
                }
            } else if (config.contains("mana_cost")) {
                // レガシーマナコスト形式（後方互換性）
                int manaCost = config.getInt("mana_cost", 0);
                validateRange(manaCost, 0, 1000, "mana_cost", file.getName());
            }

            // ダメージ計算（レガシー形式）
            Skill.DamageCalculation damage = null;
            Skill.FormulaDamageConfig formulaDamage = null;
            
            if (config.contains("damage")) {
                ConfigurationSection damageSection = config.getConfigurationSection("damage");
                
                // Phase11-6: 数式形式のチェック
                if (damageSection.contains("formula")) {
                    formulaDamage = parseFormulaDamage(damageSection, file.getName());
                } else {
                    // レガシー形式のダメージ計算
                    damage = parseDamageCalculation(damageSection, file.getName());
                }
            }

            // Phase11-6: ターゲット設定のパース
            Skill.TargetingConfig targeting = parseTargeting(config, file.getName());

            // Phase11-4: スキルターゲット設定のパース（targetパッケージ統合）
            com.example.rpgplugin.skill.target.SkillTarget skillTarget = parseSkillTarget(config, file.getName());

            // スキルツリー
            Skill.SkillTreeConfig skillTree = null;
            if (config.contains("skill_tree")) {
                skillTree = parseSkillTreeConfig(config.getConfigurationSection("skill_tree"), file.getName());
            }

            // アイコン素材（skill_tree.iconが優先、なければicon_material）
            String iconMaterial = "DIAMOND_SWORD";
            if (skillTree != null && skillTree.getIcon() != null) {
                iconMaterial = skillTree.getIcon();
            } else if (config.contains("icon_material")) {
                iconMaterial = config.getString("icon_material", "DIAMOND_SWORD");
            }

            // 利用可能なクラス
            List<String> availableClasses = config.getStringList("available_classes");

            // フォールバック値（レベル依存パラメータ未使用時）
            double cooldownFallback = config.getDouble("cooldown", 0.0);
            int manaCostFallback = config.getInt("mana_cost", 0);

            // Phase11-6+11-4統合: 新コンストラクタを使用して全設定を渡す
            return new Skill(id, name, displayName, type, description, maxLevel,
                    cooldownFallback, manaCostFallback,
                    cooldownParameter, costParameter, costType,
                    damage, skillTree, iconMaterial, availableClasses,
                    variables, formulaDamage, targeting, skillTarget);

        } catch (Exception e) {
            getLogger().log(Level.WARNING, "スキルのパースに失敗しました: " + file.getName(), e);
            return null;
        }
    }

    /**
     * ダメージ計算設定をパースします
     *
     * @param section コンフィグセクション
     * @param fileName ファイル名（エラー表示用）
     * @return ダメージ計算設定
     */
    private Skill.DamageCalculation parseDamageCalculation(ConfigurationSection section, String fileName) {
        if (section == null) {
            return null;
        }

        double base = section.getDouble("base", 0.0);
        validateRange(base, 0.0, 100000.0, "damage.base", fileName);

        // ステータス倍率
        Skill.DamageCalculation damage = null;
        if (section.contains("stat_multiplier")) {
            ConfigurationSection statSection = section.getConfigurationSection("stat_multiplier");
            String statName = statSection.getString("stat");
            double multiplier = statSection.getDouble("multiplier", 1.0);

            com.example.rpgplugin.stats.Stat stat = null;
            if (statName != null) {
                stat = com.example.rpgplugin.stats.Stat.fromShortName(statName);
                if (stat == null) {
                    stat = com.example.rpgplugin.stats.Stat.fromDisplayName(statName);
                }
            }

            double levelMultiplier = section.getDouble("level_multiplier", 0.0);
            validateRange(levelMultiplier, 0.0, 10000.0, "damage.level_multiplier", fileName);

            damage = new Skill.DamageCalculation(base, stat, multiplier, levelMultiplier);
        }

        return damage;
    }

    /**
     * スキルツリー設定をパースします
     *
     * @param section コンフィグセクション
     * @param fileName ファイル名（エラー表示用）
     * @return スキルツリー設定
     */
    private Skill.SkillTreeConfig parseSkillTreeConfig(ConfigurationSection section, String fileName) {
        if (section == null) {
            return null;
        }

        String parent = section.getString("parent", "none");
        int cost = section.getInt("cost", 1);
        validateRange(cost, 0, 100, "skill_tree.cost", fileName);

        // GUI表示用アイコン
        String icon = section.getString("icon", null);

        // 習得要件
        List<Skill.UnlockRequirement> requirements = new ArrayList<>();
        if (section.contains("unlock_requirements")) {
            List<?> requirementList = section.getList("unlock_requirements");
            if (requirementList != null) {
                for (Object reqObj : requirementList) {
                    if (reqObj instanceof ConfigurationSection) {
                        ConfigurationSection reqSection = (ConfigurationSection) reqObj;
                        Skill.UnlockRequirement requirement = parseUnlockRequirement(reqSection, fileName);
                        if (requirement != null) {
                            requirements.add(requirement);
                        }
                    }
                }
            }
        }

        return new Skill.SkillTreeConfig(parent, requirements, cost, icon);
    }

    /**
     * 習得要件をパースします
     *
     * @param section コンフィグセクション
     * @param fileName ファイル名（エラー表示用）
     * @return 習得要件
     */
    private Skill.UnlockRequirement parseUnlockRequirement(ConfigurationSection section, String fileName) {
        String type = section.getString("type");
        if (type == null) {
            getLogger().warning("unlock_requirements type is missing in " + fileName);
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

        return new Skill.UnlockRequirement(type, stat, value);
    }

    /**
     * レベル依存パラメータをパースします
     *
     * <p>サポートされるYAML形式:</p>
     * <pre>
     * # 最小値制限付き
     * cooldown:
     *   base: 5.0
     *   per_level: -0.5
     *   min: 1.0
     *
     * # 最小値・最大値制限付き
     * cost:
     *   base: 10
     *   per_level: 2
     *   min: 5
     *   max: 50
     * </pre>
     *
     * @param section コンフィグセクション
     * @param fileName ファイル名（エラー表示用）
     * @param paramName パラメータ名（エラー表示用）
     * @return レベル依存パラメータ
     */
    private LevelDependentParameter parseLevelDependentParameter(
            ConfigurationSection section, String fileName, String paramName) {
        if (section == null) {
            return null;
        }

        double base = section.getDouble("base", 0.0);
        double perLevel = section.getDouble("per_level", 0.0);

        // オプションパラメータ
        Double minValue = section.contains("min") ? section.getDouble("min") : null;
        Double maxValue = section.contains("max") ? section.getDouble("max") : null;

        // バリデーション
        if (minValue != null && maxValue != null && minValue > maxValue) {
            getLogger().warning(paramName + ".min が " + paramName + ".max より大きいです (" + fileName + ")");
        }

        return new LevelDependentParameter(base, perLevel, minValue, maxValue);
    }

/**
     * カスタム変数定義をパースします
     *
     * <p>YAML例:</p>
     * <pre>
     * variables:
     *   base_mod: 1.0
     *   str_scale: 1.5
     * </pre>
     *
     * @param config コンフィグ
     * @param fileName ファイル名（エラー表示用）
     * @return カスタム変数定義リスト
     */
    private List<Skill.VariableDefinition> parseVariables(ConfigurationSection config, String fileName) {
        List<Skill.VariableDefinition> variables = new ArrayList<>();
        
        if (!config.contains("variables")) {
            return variables;
        }
        
        ConfigurationSection variablesSection = config.getConfigurationSection("variables");
        if (variablesSection == null) {
            return variables;
        }
        
        for (String key : variablesSection.getKeys(false)) {
            double value = variablesSection.getDouble(key, 0.0);
            variables.add(new Skill.VariableDefinition(key, value));
        }
        
        return variables;
    }

    /**
     * 数式ダメージ設定をパースします
     *
     * <p>YAML例:</p>
     * <pre>
     * damage:
     *   formula: "STR * str_scale + (Lv * 5) + base_mod * 10"
     * </pre>
     *
     * <p>またはレベル別定義:</p>
     * <pre>
     * damage:
     *   formula: "STR * 2"
     *   levels:
     *     1: "STR * 2"
     *     5: "STR * 3"
     *     10: "STR * 5"
     * </pre>
     *
     * @param section ダメージセクション
     * @param fileName ファイル名（エラー表示用）
     * @return 数式ダメージ設定、数式がない場合はnull
     */
    private Skill.FormulaDamageConfig parseFormulaDamage(ConfigurationSection section, String fileName) {
        if (section == null) {
            return null;
        }
        
        // formulaキーのチェック
        String formula = section.getString("formula");
        if (formula == null || formula.trim().isEmpty()) {
            return null;
        }
        
        // 数式のバリデーション
        if (!validateFormulaSyntax(formula)) {
            getLogger().warning("無効な数式です: " + formula + " (" + fileName + ")");
            return null;
        }
        
        // レベル別数式のパース
        java.util.Map<Integer, String> levelFormulas = new java.util.HashMap<>();
        if (section.contains("levels")) {
            ConfigurationSection levelsSection = section.getConfigurationSection("levels");
            if (levelsSection != null) {
                for (String levelKey : levelsSection.getKeys(false)) {
                    try {
                        int level = Integer.parseInt(levelKey);
                        String levelFormula = levelsSection.getString(levelKey);
                        if (levelFormula != null && validateFormulaSyntax(levelFormula)) {
                            levelFormulas.put(level, levelFormula);
                        }
                    } catch (NumberFormatException e) {
                        getLogger().warning("無効なレベルキーです: " + levelKey + " (" + fileName + ")");
                    }
                }
            }
        }
        
        return new Skill.FormulaDamageConfig(formula, levelFormulas);
    }

    /**
     * ターゲット設定をパースします
     *
     * <p>YAML例:</p>
     * <pre>
     * targeting:
     *   type: cone
     *   cone:
     *     angle: 90
     *     range: 5.0
     * </pre>
     *
     * @param config コンフィグ
     * @param fileName ファイル名（エラー表示用）
     * @return ターゲット設定、未設定の場合はnull
     */
    private Skill.TargetingConfig parseTargeting(ConfigurationSection config, String fileName) {
        if (!config.contains("targeting")) {
            return null;
        }
        
        ConfigurationSection targetingSection = config.getConfigurationSection("targeting");
        if (targetingSection == null) {
            return null;
        }
        
        String type = targetingSection.getString("type", "single");
        Skill.TargetingConfig.TargetingParams params = null;
        
        switch (type.toLowerCase()) {
            case "cone":
                params = parseConeParams(targetingSection, fileName);
                break;
            case "sphere":
            case "radius":
                params = parseSphereParams(targetingSection, fileName);
                break;
            case "sector":
                params = parseSectorParams(targetingSection, fileName);
                break;
            case "single":
            default:
                // デフォルトは単体ターゲット（パラメータなし）
                break;
        }
        
        return new Skill.TargetingConfig(type, params);
    }

    /**
     * コーン型パラメータをパースします
     */
    private Skill.TargetingConfig.ConeParams parseConeParams(ConfigurationSection section, String fileName) {
        ConfigurationSection coneSection = section.getConfigurationSection("cone");
        if (coneSection == null) {
            coneSection = section;
        }
        
        double angle = coneSection.getDouble("angle", 90.0);
        double range = coneSection.getDouble("range", 5.0);
        
        validateRange(angle, 1.0, 360.0, "targeting.cone.angle", fileName);
        validateRange(range, 0.1, 100.0, "targeting.cone.range", fileName);
        
        return new Skill.TargetingConfig.ConeParams(angle, range);
    }

    /**
     * 球形パラメータをパースします
     */
    private Skill.TargetingConfig.SphereParams parseSphereParams(ConfigurationSection section, String fileName) {
        ConfigurationSection sphereSection = section.getConfigurationSection("sphere");
        if (sphereSection == null) {
            sphereSection = section.getConfigurationSection("radius");
            if (sphereSection == null) {
                sphereSection = section;
            }
        }
        
        double radius = sphereSection.getDouble("radius", sphereSection.getDouble("range", 5.0));
        
        validateRange(radius, 0.1, 100.0, "targeting.sphere.radius", fileName);
        
        return new Skill.TargetingConfig.SphereParams(radius);
    }

    /**
     * 扇形パラメータをパースします
     */
    private Skill.TargetingConfig.SectorParams parseSectorParams(ConfigurationSection section, String fileName) {
        ConfigurationSection sectorSection = section.getConfigurationSection("sector");
        if (sectorSection == null) {
            sectorSection = section;
        }
        
        double angle = sectorSection.getDouble("angle", 90.0);
        double radius = sectorSection.getDouble("radius", 5.0);
        
        validateRange(angle, 1.0, 360.0, "targeting.sector.angle", fileName);
        validateRange(radius, 0.1, 100.0, "targeting.sector.radius", fileName);
        
        return new Skill.TargetingConfig.SectorParams(angle, radius);
    }

    /**
     * 数式の構文をバリデーションします
     *
     * <p>チェック内容:</p>
     * <ul>
     *   <li>空でないこと</li>
     *   <li>危険な文字列を含まないこと</li>
     *   <li>基本的な括弧の整合性</li>
     * </ul>
     *
     * @param formula 数式文字列
     * @return 有効な場合はtrue
     */
    private boolean validateFormulaSyntax(String formula) {
        if (formula == null || formula.trim().isEmpty()) {
            return false;
        }
        
        // 括弧の整合性チェック
        int bracketCount = 0;
        for (char c : formula.toCharArray()) {
            if (c == '(') bracketCount++;
            if (c == ')') bracketCount--;
            if (bracketCount < 0) return false;
        }
        if (bracketCount != 0) return false;
        
        // 危険なパターンをチェック（簡易的なセキュリティチェック）
        String dangerousPattern = "(eval|exec|runtime|process|system|cmd)";
        if (formula.toLowerCase().matches(".*" + dangerousPattern + ".*")) {
            return false;
        }
        
        return true;
    }

    /**
     * スキルターゲット設定をパースします（Phase11-4統合）
     *
     * <p>YAML例:</p>
     * <pre>
     * target:
     *   type: nearest_hostile  # self, nearest_hostile, nearest_entity, area
     *   area_shape: cone       # single, cone, rect, circle
     *   single:
     *     select_nearest: true
     *     target_self: false
     *   cone:
     *     angle: 90
     *     range: 5.0
     *   rect:
     *     width: 3.0
     *     depth: 10.0
     *   circle:
     *     radius: 5.0
     * </pre>
     *
     * @param config コンフィグ
     * @param fileName ファイル名（エラー表示用）
     * @return スキルターゲット設定、未設定の場合はnull
     */
    private com.example.rpgplugin.skill.target.SkillTarget parseSkillTarget(
            ConfigurationSection config, String fileName) {

        if (!config.contains("target")) {
            return null;
        }

        ConfigurationSection targetSection = config.getConfigurationSection("target");
        if (targetSection == null) {
            return null;
        }

        // ターゲットタイプのパース
        String typeStr = targetSection.getString("type", "nearest_hostile");
        com.example.rpgplugin.skill.target.TargetType type =
                com.example.rpgplugin.skill.target.TargetType.fromId(typeStr)
                .orElse(com.example.rpgplugin.skill.target.TargetType.NEAREST_HOSTILE);
        if (type == null) {
            getLogger().warning("無効なtarget.type: " + typeStr + " (" + fileName + ")");
            type = com.example.rpgplugin.skill.target.TargetType.NEAREST_HOSTILE;
        }

        // 範囲形状のパース
        String shapeStr = targetSection.getString("area_shape", "single");
        com.example.rpgplugin.skill.target.AreaShape areaShape =
                com.example.rpgplugin.skill.target.AreaShape.fromId(shapeStr)
                .orElse(com.example.rpgplugin.skill.target.AreaShape.SINGLE);
        if (areaShape == null) {
            getLogger().warning("無効なtarget.area_shape: " + shapeStr + " (" + fileName + ")");
            areaShape = com.example.rpgplugin.skill.target.AreaShape.SINGLE;
        }

        // 単体ターゲット設定
        com.example.rpgplugin.skill.target.SkillTarget.SingleTargetConfig singleTarget = null;
        if (targetSection.contains("single")) {
            ConfigurationSection singleSection = targetSection.getConfigurationSection("single");
            if (singleSection != null) {
                boolean selectNearest = singleSection.getBoolean("select_nearest", true);
                boolean targetSelf = singleSection.getBoolean("target_self", false);
                singleTarget = new com.example.rpgplugin.skill.target.SkillTarget.SingleTargetConfig(
                        selectNearest, targetSelf);
            }
        }

        // 扇状設定
        com.example.rpgplugin.skill.target.SkillTarget.ConeConfig cone = null;
        if (targetSection.contains("cone")) {
            ConfigurationSection coneSection = targetSection.getConfigurationSection("cone");
            if (coneSection != null) {
                double angle = coneSection.getDouble("angle", 90.0);
                double range = coneSection.getDouble("range", 5.0);
                validateRange(angle, 1.0, 360.0, "target.cone.angle", fileName);
                validateRange(range, 0.1, 100.0, "target.cone.range", fileName);
                cone = new com.example.rpgplugin.skill.target.SkillTarget.ConeConfig(angle, range);
            }
        }

        // 四角形設定
        com.example.rpgplugin.skill.target.SkillTarget.RectConfig rect = null;
        if (targetSection.contains("rect")) {
            ConfigurationSection rectSection = targetSection.getConfigurationSection("rect");
            if (rectSection != null) {
                double width = rectSection.getDouble("width", 3.0);
                double depth = rectSection.getDouble("depth", 10.0);
                validateRange(width, 0.1, 100.0, "target.rect.width", fileName);
                validateRange(depth, 0.1, 100.0, "target.rect.depth", fileName);
                rect = new com.example.rpgplugin.skill.target.SkillTarget.RectConfig(width, depth);
            }
        }

        // 円形設定
        com.example.rpgplugin.skill.target.SkillTarget.CircleConfig circle = null;
        if (targetSection.contains("circle")) {
            ConfigurationSection circleSection = targetSection.getConfigurationSection("circle");
            if (circleSection != null) {
                double radius = circleSection.getDouble("radius", 5.0);
                validateRange(radius, 0.1, 100.0, "target.circle.radius", fileName);
                circle = new com.example.rpgplugin.skill.target.SkillTarget.CircleConfig(radius);
            }
        }

        // 汎用ターゲット設定（LINE, LOOKING, CONE, SPHERE用）
        Double range = null;
        Double lineWidth = null;
        Double coneAngle = null;
        Double sphereRadius = null;

        if (targetSection.contains("range")) {
            range = targetSection.getDouble("range");
            validateRange(range, 0.1, 100.0, "target.range", fileName);
        }

        if (targetSection.contains("line_width")) {
            lineWidth = targetSection.getDouble("line_width");
            validateRange(lineWidth, 0.1, 50.0, "target.line_width", fileName);
        }

        if (targetSection.contains("cone_angle")) {
            coneAngle = targetSection.getDouble("cone_angle");
            validateRange(coneAngle, 1.0, 360.0, "target.cone_angle", fileName);
        }

        if (targetSection.contains("sphere_radius")) {
            sphereRadius = targetSection.getDouble("sphere_radius");
            validateRange(sphereRadius, 0.1, 100.0, "target.sphere_radius", fileName);
        }

        // sphereセクションからsphere_radiusを取得
        if (targetSection.contains("sphere")) {
            ConfigurationSection sphereSection = targetSection.getConfigurationSection("sphere");
            if (sphereSection != null && sphereRadius == null) {
                sphereRadius = sphereSection.getDouble("radius", 5.0);
                validateRange(sphereRadius, 0.1, 100.0, "target.sphere.radius", fileName);
            }
        }

        // EntityTypeFilterのパース
        com.example.rpgplugin.skill.target.EntityTypeFilter entityTypeFilter =
                com.example.rpgplugin.skill.target.EntityTypeFilter.ALL;
        if (targetSection.contains("filter")) {
            String filterStr = targetSection.getString("filter", "all").toLowerCase();
            switch (filterStr) {
                case "player", "players" -> entityTypeFilter =
                        com.example.rpgplugin.skill.target.EntityTypeFilter.PLAYER_ONLY;
                case "mob", "mobs", "hostile" -> entityTypeFilter =
                        com.example.rpgplugin.skill.target.EntityTypeFilter.MOB_ONLY;
                default -> entityTypeFilter =
                        com.example.rpgplugin.skill.target.EntityTypeFilter.ALL;
            }
        }

        // 最大ターゲット数
        Integer maxTargets = null;
        if (targetSection.contains("max_targets")) {
            maxTargets = targetSection.getInt("max_targets");
        }

        return new com.example.rpgplugin.skill.target.SkillTarget(
                type, areaShape, singleTarget, cone, rect, circle,
                entityTypeFilter, maxTargets, range, lineWidth, coneAngle, sphereRadius);
    }

    /**
     * スキルディレクトリを取得します
     *
     * @return スキルディレクトリ
     */
    public File getSkillsDirectory() {
        return new File(plugin.getDataFolder(), "skills");
    }
}
