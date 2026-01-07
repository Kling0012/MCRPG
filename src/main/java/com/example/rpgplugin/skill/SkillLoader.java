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

        List<File> yamlFiles = getYamlFiles("skills", false);
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
            validateRequired(config, List.of("id", "name", "type"));

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

            // ダメージ計算
            Skill.DamageCalculation damage = null;
            if (config.contains("damage")) {
                damage = parseDamageCalculation(config.getConfigurationSection("damage"), file.getName());
            }

            // スキルツリー
            Skill.SkillTreeConfig skillTree = null;
            if (config.contains("skill_tree")) {
                skillTree = parseSkillTreeConfig(config.getConfigurationSection("skill_tree"), file.getName());
            }

            // アイコン素材
            String iconMaterial = config.getString("icon_material", "DIAMOND_SWORD");

            // 利用可能なクラス
            List<String> availableClasses = config.getStringList("available_classes");

            // フォールバック値（レベル依存パラメータ未使用時）
            double cooldownFallback = config.getDouble("cooldown", 0.0);
            int manaCostFallback = config.getInt("mana_cost", 0);

            return new Skill(id, name, displayName, type, description, maxLevel,
                    cooldownFallback, manaCostFallback,
                    cooldownParameter, costParameter, costType,
                    damage, skillTree, iconMaterial, availableClasses);

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

        return new Skill.SkillTreeConfig(parent, requirements, cost);
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
     * スキルディレクトリを取得します
     *
     * @return スキルディレクトリ
     */
    public File getSkillsDirectory() {
        return new File(plugin.getDataFolder(), "skills");
    }
}
