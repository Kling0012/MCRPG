package com.example.rpgplugin.skill.config;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillLoader;
import com.example.rpgplugin.skill.SkillManager;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

/**
 * スキル設定管理クラス
 *
 * <p>スキルYAMLファイルのロード、リロード、バリデーションを管理します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキル設定の管理に専念</li>
 *   <li>DRY: 設定ロードロジックを一元管理</li>
 *   <li>OCP: ホットリロード機能で拡張可能</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillConfig {

    private final RPGPlugin plugin;
    private final SkillManager skillManager;
    private final SkillLoader loader;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     * @param skillManager スキルマネージャー
     */
    public SkillConfig(RPGPlugin plugin, SkillManager skillManager) {
        this.plugin = plugin;
        this.skillManager = skillManager;
        this.loader = new SkillLoader(plugin);
    }

    /**
     * 全スキルを読み込みます
     *
     * @return 読み込んだスキル数
     */
    public int loadSkills() {
        plugin.getLogger().info("スキルの読み込みを開始します...");

        // 既存のスキルをクリア
        skillManager.clearAllSkills();

        // スキルを読み込み
        List<Skill> skills = loader.loadAllSkills();

        // スキルマネージャーに登録
        int successCount = 0;
        for (Skill skill : skills) {
            if (skill != null && skillManager.registerSkill(skill)) {
                successCount++;
            }
        }

        plugin.getLogger().info(successCount + " 個のスキルを登録しました");
        return successCount;
    }

    /**
     * スキルをリロードします
     *
     * @return リロードしたスキル数
     */
    public int reloadSkills() {
        plugin.getLogger().info("スキルのリロードを開始します...");
        int count = loadSkills();
        plugin.getLogger().info("スキルのリロードが完了しました");
        return count;
    }

    /**
     * 単一のスキルファイルをリロードします
     *
     * @param fileName ファイル名
     * @return 成功した場合はtrue
     */
    public boolean reloadSkill(String fileName) {
        File skillsDir = loader.getSkillsDirectory();
        File file = new File(skillsDir, fileName);

        if (!file.exists()) {
            plugin.getLogger().warning("スキルファイルが見つかりません: " + fileName);
            return false;
        }

        try {
            Skill skill = loader.loadSkill(file);
            if (skill == null) {
                return false;
            }

            // 既存のスキルを一度削除して再登録
            skillManager.clearAllSkills();
            loadSkills();

            plugin.getLogger().info("スキルをリロードしました: " + skill.getId());
            return true;

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "スキルのリロードに失敗しました: " + fileName, e);
            return false;
        }
    }

    /**
     * スキルローダーを取得します
     *
     * @return スキルローダー
     */
    public SkillLoader getLoader() {
        return loader;
    }

    /**
     * スキル設定が有効かチェックします
     *
     * @return 有効な場合はtrue
     */
    public boolean isValid() {
        File skillsDir = loader.getSkillsDirectory();
        return skillsDir.exists() && skillsDir.isDirectory();
    }
}
