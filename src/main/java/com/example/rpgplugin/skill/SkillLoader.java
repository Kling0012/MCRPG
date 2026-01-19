package com.example.rpgplugin.skill;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.core.config.ConfigLoader;
import com.example.rpgplugin.skill.parser.SkillParserFactory;
import org.bukkit.configuration.file.FileConfiguration;

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
 *   <li>ファサードパターン: 複雑なパース処理を専用クラスに委譲</li>
 *   <li>KISS: シンプルなインターフェース</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 3.0.0 - リファクタリング: 専用パーサークラスへの分割
 */
public class SkillLoader extends ConfigLoader {

    private final RPGPlugin plugin;
    private final SkillParserFactory parserFactory;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public SkillLoader(RPGPlugin plugin) {
        super(plugin.getLogger(), plugin.getDataFolder());
        this.plugin = plugin;
        this.parserFactory = new SkillParserFactory(getLogger());
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
            Skill skill = loadSkill(file);
            if (skill != null) {
                skills.add(skill);
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
            return parserFactory.parseSkill(config, file.getName());
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "スキルのパースに失敗しました: " + file.getName(), e);
            return null;
        }
    }

    /**
     * スキルディレクトリを取得します
     *
     * @return スキルディレクトリ
     */
    public File getSkillsDirectory() {
        return new File(plugin.getDataFolder(), "skills");
    }

    /**
     * パーサファクトリを取得します
     *
     * @return パーサファクトリ
     */
    public SkillParserFactory getParserFactory() {
        return parserFactory;
    }
}
