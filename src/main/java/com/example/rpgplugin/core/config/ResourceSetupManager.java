package com.example.rpgplugin.core.config;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * リソースセットアップマネージャー
 *
 * <p>JAR内のテンプレートファイルをプラグインフォルダにコピーします。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: リソースのセットアップに専念</li>
 *   <li>DRY: コピーロジックを一元管理</li>
 *   <li>KISS: シンプルなファイル操作</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ResourceSetupManager {

    private final Plugin plugin;
    private final Logger logger;
    private final File dataFolder;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public ResourceSetupManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dataFolder = plugin.getDataFolder();
    }

    /**
     * 全てのリソースをセットアップします
     *
     * @return セットアップに成功した場合はtrue
     */
    public boolean setupAllResources() {
        logger.info("リソースのセットアップを開始します...");

        boolean success = true;

        // テンプレートディレクトリを作成
        success &= setupTemplateDirectory();

        // スキルディレクトリ構造を作成
        success &= setupSkillDirectories();

        // クラスディレクトリを作成
        success &= setupClassDirectories();

        // その他のディレクトリを作成
        success &= setupOtherDirectories();

        if (success) {
            logger.info("リソースのセットアップが完了しました");
        } else {
            logger.warning("リソースのセットアップ中に一部のエラーが発生しました");
        }

        return success;
    }

    /**
     * テンプレートディレクトリをセットアップします
     *
     * <p>JAR内のtemplates/ディレクトリをプラグインフォルダにコピーします。</p>
     *
     * @return 成功した場合はtrue
     */
    public boolean setupTemplateDirectory() {
        File templatesDir = new File(dataFolder, "templates");

        // 既に存在する場合はスキップ（設定で上書き可能にする）
        if (templatesDir.exists()) {
            logger.info("テンプレートディレクトリは既に存在します: " + templatesDir.getPath());
            return true;
        }

        logger.info("テンプレートディレクトリを作成します: " + templatesDir.getPath());
        if (!templatesDir.mkdirs()) {
            logger.warning("テンプレートディレクトリの作成に失敗しました");
            return false;
        }

        // サブディレクトリを作成
        File skillsTemplateDir = new File(templatesDir, "skills");
        File classesTemplateDir = new File(templatesDir, "classes");

        skillsTemplateDir.mkdirs();
        classesTemplateDir.mkdirs();

        // JAR内のテンプレートファイルをコピー
        boolean success = true;

        // スキルテンプレート（V5コンポーネント形式）
        // V5ではアクティブ/パッシブの区別はトリガー类型で表現するため、単一テンプレートを使用
        success &= copyResourceFromJar("templates/skills/skill_template.yml",
                new File(skillsTemplateDir, "skill_template.yml"));

        // クラステンプレート
        success &= copyResourceFromJar("templates/classes/melee_template.yml",
                new File(classesTemplateDir, "melee_template.yml"));
        success &= copyResourceFromJar("templates/classes/ranged_template.yml",
                new File(classesTemplateDir, "ranged_template.yml"));
        success &= copyResourceFromJar("templates/classes/magic_template.yml",
                new File(classesTemplateDir, "magic_template.yml"));
        success &= copyResourceFromJar("templates/classes/tank_template.yml",
                new File(classesTemplateDir, "tank_template.yml"));

        return success;
    }

    /**
     * スキルディレクトリ構造をセットアップします
     *
     * @return 成功した場合はtrue
     */
    public boolean setupSkillDirectories() {
        File skillsDir = new File(dataFolder, "skills");

        if (!skillsDir.exists()) {
            skillsDir.mkdirs();
            logger.info("スキルディレクトリを作成しました: " + skillsDir.getPath());
        }

        // アクティブスキルディレクトリ
        File activeDir = new File(skillsDir, "active");
        if (!activeDir.exists()) {
            activeDir.mkdirs();
        }

        // パッシブスキルディレクトリ
        File passiveDir = new File(skillsDir, "passive");
        if (!passiveDir.exists()) {
            passiveDir.mkdirs();
        }

        // READMEファイルを作成（初回のみ）
        File readme = new File(skillsDir, "README.txt");
        if (!readme.exists()) {
            try {
                createSkillsReadme(readme);
            } catch (IOException e) {
                logger.log(Level.WARNING, "READMEファイルの作成に失敗しました", e);
            }
        }

        return true;
    }

    /**
     * クラスディレクトリをセットアップします
     *
     * @return 成功した場合はtrue
     */
    public boolean setupClassDirectories() {
        File classesDir = new File(dataFolder, "classes");

        if (!classesDir.exists()) {
            classesDir.mkdirs();
            logger.info("クラスディレクトリを作成しました: " + classesDir.getPath());
        }

        // READMEファイルを作成（初回のみ）
        File readme = new File(classesDir, "README.txt");
        if (!readme.exists()) {
            try {
                createClassesReadme(readme);
            } catch (IOException e) {
                logger.log(Level.WARNING, "READMEファイルの作成に失敗しました", e);
            }
        }

        return true;
    }

    /**
     * その他のディレクトリをセットアップします
     *
     * @return 成功した場合はtrue
     */
    public boolean setupOtherDirectories() {
        List<String> directories = List.of(
                "mobs",
                "exp",
                "data"
        );

        boolean success = true;
        for (String dirName : directories) {
            File dir = new File(dataFolder, dirName);
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    logger.info("ディレクトリを作成しました: " + dirName);
                } else {
                    logger.warning("ディレクトリの作成に失敗しました: " + dirName);
                    success = false;
                }
            }
        }

        return success;
    }

    /**
     * JAR内のリソースをファイルにコピーします
     *
     * @param resourcePath JAR内のリソースパス
     * @param targetFile コピー先ファイル
     * @return 成功した場合はtrue
     */
    private boolean copyResourceFromJar(String resourcePath, File targetFile) {
        if (targetFile.exists()) {
            return true; // 既に存在する場合はスキップ
        }

        try (InputStream is = plugin.getResource(resourcePath)) {
            if (is == null) {
                logger.warning("リソースが見つかりません: " + resourcePath);
                return false;
            }

            // 親ディレクトリが存在しない場合は作成
            File parentDir = targetFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (FileOutputStream os = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }

            logger.info("リソースをコピーしました: " + resourcePath + " -> " + targetFile.getName());
            return true;

        } catch (IOException e) {
            logger.log(Level.WARNING, "リソースのコピーに失敗しました: " + resourcePath, e);
            return false;
        }
    }

    /**
     * スキルディレクトリのREADMEファイルを作成します
     *
     * @param file READMEファイル
     * @throws IOException 書き込みに失敗した場合
     */
    private void createSkillsReadme(File file) throws IOException {
        String content = """
                # スキルディレクトリ

                このディレクトリには、サーバーで使用するスキル定義ファイルを配置します。

                ## V5 コンポーネントシステム

                V5形式では、スキルは「コンポーネント」の組み合わせで定義されます。

                ### スキルタイプの違い（トリガーで表現）

                V5システムでは、アクティブ/パッシブの区別は**トリガー種類**で表現します：

                - **アクティブスキル**: `trigger: CAST` を使用（手動発動）
                  ```yaml
                  components:
                    - type: trigger
                      component_id: CAST
                      children:
                        - type: mechanic
                          component_id: DAMAGE
                  ```

                - **パッシブスキル**: イベントトリガーを使用（自動発動）
                  ```yaml
                  components:
                    - type: trigger
                      component_id: PHYSICAL_DEALT  # 攻撃時に発動
                      children:
                        - type: mechanic
                          component_id: DAMAGE
                  ```

                ### 利用可能なトリガー

                - `CAST`: スキル使用時に即時実行（アクティブスキル用）
                - `CROUCH`: スニーク時
                - `LAND`: 着地時
                - `DEATH`: 死亡時
                - `KILL`: エンティキル時
                - `PHYSICAL_DEALT`: 物理ダメージを与えた時
                - `PHYSICAL_TAKEN`: 物理ダメージを受けた時
                - `LAUNCH`: 投射物発射時
                - `ENVIRONMENTAL`: 環境ダメージ時

                ## ディレクトリ構造

                - `active/`  : アクティブスキルを配置（推奨、任意）
                - `passive/` : パッシブスキルを配置（推奨、任意）

                ※ どちらのディレクトリも読み込まれます。整理のための分類です。

                ## スキルの追加方法

                1. `templates/skills/skill_template.yml` をコピーします
                2. スキルID、表示名、コンポーネントを編集します
                3. このディレクトリ（active または passive）に配置します
                4. `/rpg reload` コマンドでリロードします

                ## ホットリロード

                config.yml で `hot_reload.skills: true` に設定されている場合、
                ファイルを保存すると自動的にリロードされます。

                ## テンプレートの場所

                テンプレートファイルは `plugins/MCRPG/templates/skills/skill_template.yml` にあります。
                """;

        java.nio.file.Files.writeString(file.toPath(), content);
    }

    /**
     * クラスディレクトリのREADMEファイルを作成します
     *
     * @param file READMEファイル
     * @throws IOException 書き込みに失敗した場合
     */
    private void createClassesReadme(File file) throws IOException {
        String content = """
                # クラスディレクトリ

                このディレクトリには、サーバーで使用するクラス定義ファイルを配置します。

                ## クラスの追加方法

                1. `templates/` ディレクトリにあるテンプレートをコピーします
                2. クラスID、表示名、ステータス成長などを編集します
                3. このディレクトリに配置します
                4. `/rpg reload` コマンドでリロードします

                ## ホットリロード

                config.yml で `hot_reload.classes: true` に設定されている場合、
                ファイルを保存すると自動的にリロードされます。

                ## テンプレートの場所

                テンプレートファイルは `plugins/MCRPG/templates/classes/` にあります。
                """;

        java.nio.file.Files.writeString(file.toPath(), content);
    }

    /**
     * セットアップ済みのディレクトリを取得します
     *
     * @return ディレクトリのリスト
     */
    @NotNull
    public List<File> getSetupDirectories() {
        List<File> directories = new ArrayList<>();

        directories.add(new File(dataFolder, "skills"));
        directories.add(new File(dataFolder, "skills/active"));
        directories.add(new File(dataFolder, "skills/passive"));
        directories.add(new File(dataFolder, "classes"));
        directories.add(new File(dataFolder, "templates"));
        directories.add(new File(dataFolder, "templates/skills"));
        directories.add(new File(dataFolder, "templates/classes"));
        directories.add(new File(dataFolder, "mobs"));
        directories.add(new File(dataFolder, "exp"));
        directories.add(new File(dataFolder, "data"));

        return directories;
    }
}
