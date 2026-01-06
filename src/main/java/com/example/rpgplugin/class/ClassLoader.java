package com.example.rpgplugin.class;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.class.growth.StatGrowth;
import com.example.rpgplugin.class.requirements.*;
import com.example.rpgplugin.player.PlayerManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * クラス設定ローダー
 * classes/ディレクトリからYAMLを読み込み、RPGClassインスタンスを生成
 */
public class ClassLoader {

    private final RPGPlugin plugin;
    private final Logger logger;
    private final File classesDirectory;
    private final PlayerManager playerManager;

    /**
     * コンストラクタ
     *
     * @param plugin        プラグインインスタンス
     * @param playerManager プレイヤーマネージャー
     */
    public ClassLoader(RPGPlugin plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.playerManager = playerManager;
        this.classesDirectory = new File(plugin.getDataFolder(), "classes");

        // ディレクトリが存在しない場合は作成
        if (!classesDirectory.exists()) {
            classesDirectory.mkdirs();
        }
    }

    /**
     * すべてのクラス設定をロード
     *
     * @return クラスID → RPGClassのマップ
     */
    public Map<String, RPGClass> loadAllClasses() {
        Map<String, RPGClass> classes = new HashMap<>();

        File[] files = classesDirectory.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".yml"));

        if (files == null || files.length == 0) {
            logger.warning("classes/ directory is empty or does not exist");
            return classes;
        }

        for (File file : files) {
            try {
                RPGClass rpgClass = loadClass(file);
                if (rpgClass != null) {
                    classes.put(rpgClass.getId(), rpgClass);
                    logger.info("Loaded class: " + rpgClass.getId() + " (" + rpgClass.getName() + ")");
                }
            } catch (Exception e) {
                logger.severe("Failed to load class from " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        logger.info("Loaded " + classes.size() + " classes");
        return classes;
    }

    /**
     * 単一のクラス設定をロード
     *
     * @param file YAMLファイル
     * @return RPGClassインスタンス（失敗時はnull）
     */
    public RPGClass loadClass(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // 基本情報の読み込み
        String id = config.getString("id");
        if (id == null || id.isEmpty()) {
            logger.warning("Class ID is missing in " + file.getName());
            return null;
        }

        RPGClass.Builder builder = new RPGClass.Builder(id);

        // 名前と表示名
        builder.setName(config.getString("name", id))
                .setDisplayName(config.getString("display_name", id));

        // 説明
        List<String> description = config.getStringList("description");
        if (!description.isEmpty()) {
            builder.setDescription(description);
        }

        // ランクと最大レベル
        builder.setRank(config.getInt("rank", 1))
                .setMaxLevel(config.getInt("max_level", 50));

        // アイコン
        String iconStr = config.getString("icon", "DIAMOND_SWORD");
        try {
            Material icon = Material.valueOf(iconStr.toUpperCase());
            builder.setIcon(icon);
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid icon material: " + iconStr + " for class " + id);
            builder.setIcon(Material.DIAMOND_SWORD);
        }

        // ステータス成長
        ConfigurationSection statGrowthSection = config.getConfigurationSection("stat_growth");
        if (statGrowthSection != null) {
            StatGrowth statGrowth = StatGrowth.parse(statGrowthSection);
            builder.setStatGrowth(statGrowth);
        } else {
            // デフォルト成長設定
            StatGrowth defaultGrowth = new StatGrowth.Builder()
                    .setManualPoints(3)
                    .build();
            builder.setStatGrowth(defaultGrowth);
        }

        // 次のランク（直線パターン）
        ConfigurationSection nextRankSection = config.getConfigurationSection("next_rank");
        if (nextRankSection != null) {
            String nextClassId = nextRankSection.getString("class_id");
            if (nextClassId != null && !nextClassId.isEmpty()) {
                builder.setNextRankClassId(nextClassId);

                // 要件の読み込み
                List<ClassRequirement> requirements = parseRequirements(
                        nextRankSection.getConfigurationSection("requirements"));
                requirements.forEach(builder::addNextRankRequirement);
            }
        }

        // 分岐パターン
        ConfigurationSection alternativeSection = config.getConfigurationSection("alternative_ranks");
        if (alternativeSection != null) {
            for (String classId : alternativeSection.getKeys(false)) {
                ConfigurationSection altClassSection = alternativeSection.getConfigurationSection(classId);
                List<ClassRequirement> requirements = parseRequirements(
                        altClassSection.getConfigurationSection("requirements"));
                builder.addAlternativeRank(classId, requirements);
            }
        }

        // 使用可能スキル
        List<String> skills = config.getStringList("available_skills");
        skills.forEach(builder::addAvailableSkill);

        // パッシブボーナス
        List<Map<String, Object>> passiveList = config.getMapList("passive_bonuses");
        for (Map<String, Object> passiveMap : passiveList) {
            ConfigurationSection passiveSection = createSectionFromMap(passiveMap);
            builder.addPassiveBonus(RPGClass.PassiveBonus.parse(passiveSection));
        }

        // 経験値減衰
        ConfigurationSection expDiminishSection = config.getConfigurationSection("exp_diminish");
        if (expDiminishSection != null) {
            RPGClass.ExpDiminish expDiminish = RPGClass.ExpDiminish.parse(expDiminishSection);
            builder.setExpDiminish(expDiminish);
        }

        return builder.build();
    }

    /**
     * 要件リストをパース
     *
     * @param section 要件設定セクション
     * @return 要件リスト
     */
    private List<ClassRequirement> parseRequirements(ConfigurationSection section) {
        List<ClassRequirement> requirements = new ArrayList<>();

        if (section == null) {
            return requirements;
        }

        List<Map<String, Object>> requirementList = section.getMapList("requirements");
        if (requirementList.isEmpty()) {
            // 単一の要件として処理
            for (String key : section.getKeys(false)) {
                ConfigurationSection reqSection = section.getConfigurationSection(key);
                if (reqSection != null) {
                    ClassRequirement req = parseRequirement(reqSection);
                    if (req != null) {
                        requirements.add(req);
                    }
                }
            }
        } else {
            // 複数要件として処理
            for (Map<String, Object> reqMap : requirementList) {
                ConfigurationSection reqSection = createSectionFromMap(reqMap);
                ClassRequirement req = parseRequirement(reqSection);
                if (req != null) {
                    requirements.add(req);
                }
            }
        }

        return requirements;
    }

    /**
     * 単一の要件をパース
     *
     * @param section 要件設定セクション
     * @return 要件インスタンス
     */
    private ClassRequirement parseRequirement(ConfigurationSection section) {
        String type = section.getString("type", "").toLowerCase();

        switch (type) {
            case "level":
                return LevelRequirement.parse(section, playerManager);

            case "stat":
                return StatRequirement.parse(section, playerManager);

            case "item":
                return ItemRequirement.parse(section);

            case "quest":
                return QuestRequirement.parse(section);

            default:
                logger.warning("Unknown requirement type: " + type);
                return null;
        }
    }

    /**
     * MapからConfigurationSectionを作成
     *
     * @param map データマップ
     * @return ConfigurationSection
     */
    private ConfigurationSection createSectionFromMap(Map<String, Object> map) {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        return config;
    }

    /**
     * クラスディレクトリを取得
     *
     * @return ディレクトリ
     */
    public File getClassesDirectory() {
        return classesDirectory;
    }
}
