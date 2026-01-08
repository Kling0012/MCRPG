package com.example.rpgplugin.skill;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * スキルYAMLファイルのレガシーフォーマットから新フォーマットへの移行ツール
 *
 * <p>Phase11-12: 既存スキルデータのマイグレーション</p>
 *
 * <h2>移行ルール</h2>
 * <ul>
 *   <li>{@code cooldown} 数値 → {@code cooldown: { base: 数値, per_level: 0.0, min: 数値 * 0.5 }}</li>
 *   <li>{@code mana_cost} 数値 → {@code cost: { type: mana, base: 数値, per_level: 0, min: 0 }}</li>
 *   <li>{@code damage: { base, stat_multiplier, level_multiplier }} → {@code damage: { formula: "数式" }}</li>
 *   <li>ターゲット設定なし → {@code targeting: { type: single }}</li>
 *   <li>カスタム変数を追加（レガシーダメージ変数から変換）</li>
 * </ul>
 *
 * @author Phase11-12 Development Team
 * @since 1.0.0
 */
public class SkillMigrationTool {

    private static final Logger LOGGER = Logger.getLogger(SkillMigrationTool.class.getName());

    private final File pluginDataFolder;
    private final File skillsDirectory;
    private final File legacyBackupDirectory;

    /**
     * コンストラクタ
     *
     * @param pluginDataFolder プラグインデータフォルダ
     */
    public SkillMigrationTool(File pluginDataFolder) {
        this.pluginDataFolder = pluginDataFolder;
        this.skillsDirectory = new File(pluginDataFolder, "skills");
        this.legacyBackupDirectory = new File(skillsDirectory, "legacy");
    }

    /**
     * 全スキルファイルを新フォーマットに移行します
     *
     * @return 移行結果レポート
     */
    public MigrationReport migrateAllSkills() {
        MigrationReport report = new MigrationReport();

        if (!skillsDirectory.exists()) {
            report.addError("スキルディレクトリが存在しません: " + skillsDirectory.getAbsolutePath());
            return report;
        }

        // バックアップディレクトリの作成
        createBackupDirectory();

        // 全YAMLファイルを収集
        List<File> skillFiles = collectSkillFiles(skillsDirectory);

        for (File skillFile : skillFiles) {
            try {
                migrateSkillFile(skillFile, report);
            } catch (Exception e) {
                report.addError("スキル移行失敗: " + skillFile.getName() + " - " + e.getMessage());
                LOGGER.log(Level.WARNING, "スキル移行失敗: " + skillFile.getName(), e);
            }
        }

        return report;
    }

    /**
     * 特定のスキルファイルのみを移行します
     *
     * @param skillFileName スキルファイル名
     * @return 移行結果
     */
    public boolean migrateSkill(String skillFileName) {
        File skillFile = findSkillFile(skillFileName);
        if (skillFile == null) {
            LOGGER.warning("スキルファイルが見つかりません: " + skillFileName);
            return false;
        }

        createBackupDirectory();
        MigrationReport report = new MigrationReport();

        try {
            return migrateSkillFile(skillFile, report);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "スキル移行失敗: " + skillFileName, e);
            return false;
        }
    }

    /**
     * 単一のスキルファイルを移行します
     *
     * @param skillFile スキルファイル
     * @param report    移行レポート
     * @return 移行成功時true
     */
    private boolean migrateSkillFile(File skillFile, MigrationReport report) {
        // 既に新フォーマットかチェック
        if (isNewFormat(skillFile)) {
            report.addSkipped(skillFile.getName());
            LOGGER.info("既に新フォーマット: " + skillFile.getName());
            return true;
        }

        // レガシー形式のバックアップ作成
        File backupFile = createBackup(skillFile);

        // YAMLを読み込み
        FileConfiguration config = YamlConfiguration.loadConfiguration(skillFile);

        // 新フォーマットへ変換
        convertToNewFormat(config, skillFile.getName());

        // 書き込み
        try {
            config.save(skillFile);
            report.addMigrated(skillFile.getName(), backupFile.getName());
            LOGGER.info("スキル移行完了: " + skillFile.getName());
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "スキル保存失敗: " + skillFile.getName(), e);
            report.addError("保存失敗: " + skillFile.getName());
            return false;
        }
    }

    /**
     * レガシーフォーマットから新フォーマットへ変換します
     *
     * @param config   設定セクション（変更されます）
     * @param fileName ファイル名（エラー表示用）
     */
    private void convertToNewFormat(FileConfiguration config, String fileName) {
        // cooldownの変換: 数値 → セクション形式
        if (config.contains("cooldown") && !config.isConfigurationSection("cooldown")) {
            double oldCooldown = config.getDouble("cooldown", 0.0);
            config.set("cooldown", null); // 古い値を削除

            // 新しいcooldownセクションを作成
            config.set("cooldown.base", oldCooldown);
            config.set("cooldown.per_level", 0.0);
            config.set("cooldown.min", Math.max(1.0, oldCooldown * 0.5));
        }

        // mana_costの変換: 数値 → costセクション形式
        if (config.contains("mana_cost") && !config.contains("cost")) {
            int oldManaCost = config.getInt("mana_cost", 0);
            config.set("mana_cost", null); // 古い値を削除

            // 新しいcostセクションを作成
            config.set("cost.type", "mana");
            config.set("cost.base", oldManaCost);
            config.set("cost.per_level", 0);
            config.set("cost.min", 0);
        }

        // damageセクションの変換: レガシー形式 → 数式形式
        if (config.contains("damage") && config.isConfigurationSection("damage")) {
            ConfigurationSection damageSection = config.getConfigurationSection("damage");

            // レガシー形式のチェック（formulaキーがない場合）
            if (!damageSection.contains("formula")) {
                convertLegacyDamageToFormula(damageSection, config, fileName);
            }
        }

        // targetingセクションの追加（まだない場合）
        if (!config.contains("targeting")) {
            String type = config.getString("type", "active");
            if ("active".equals(type)) {
                // アクティブスキルのデフォルトターゲット
                config.set("targeting.type", "single");
            }
        }

        // variablesセクションの生成（damageの数式で使用する変数）
        if (!config.contains("variables") && config.contains("damage")) {
            generateVariablesFromDamage(config);
        }
    }

    /**
     * レガシーダメージ形式を数式形式に変換します
     *
     * @param damageSection ダメージセクション
     * @param config        全体設定
     * @param fileName      ファイル名
     */
    private void convertLegacyDamageToFormula(ConfigurationSection damageSection,
                                               FileConfiguration config, String fileName) {
        double base = damageSection.getDouble("base", 0.0);
        double levelMultiplier = damageSection.getDouble("level_multiplier", 0.0);

        // stat_multiplierの処理
        String statName = null;
        double statMultiplier = 1.0;

        if (damageSection.contains("stat_multiplier")) {
            ConfigurationSection statSection = damageSection.getConfigurationSection("stat_multiplier");
            statName = statSection.getString("stat");
            statMultiplier = statSection.getDouble("multiplier", 1.0);
        }

        // 数式の構築
        StringBuilder formula = new StringBuilder();
        List<String> variables = new ArrayList<>();

        // ステータス倍率の部分
        if (statName != null) {
            String statShort = convertStatToShortName(statName);
            formula.append(statShort).append(" * ").append(statMultiplier);
            variables.add(statShort.toLowerCase() + "_mod: " + statMultiplier);
        }

        // レベル倍率の部分
        if (levelMultiplier > 0) {
            if (formula.length() > 0) {
                formula.append(" + ");
            }
            formula.append("Lv * ").append(levelMultiplier);
            variables.add("level_scale: " + levelMultiplier);
        }

        // 基礎値の部分
        if (base > 0) {
            if (formula.length() > 0) {
                formula.append(" + ");
            }
            formula.append(base);
            variables.add("base_damage: " + base);
        }

        // 新しい数式を設定
        String formulaString = formula.toString();
        damageSection.set("formula", formulaString);

        // レガシーフィールドの削除
        damageSection.set("base", null);
        damageSection.set("stat_multiplier", null);
        damageSection.set("level_multiplier", null);

        // variablesセクションの生成
        if (!variables.isEmpty()) {
            ConfigurationSection variablesSection = config.createSection("variables");
            for (String variable : variables) {
                String[] parts = variable.split(": ");
                if (parts.length == 2) {
                    try {
                        double value = Double.parseDouble(parts[1]);
                        variablesSection.set(parts[0], value);
                    } catch (NumberFormatException e) {
                        // 無視
                    }
                }
            }
        }

        LOGGER.info("ダメージ数式変換完了 (" + fileName + "): " + formulaString);
    }

    /**
     * damageセクションからvariablesを生成します
     *
     * @param config 全体設定
     */
    private void generateVariablesFromDamage(FileConfiguration config) {
        ConfigurationSection damageSection = config.getConfigurationSection("damage");
        if (damageSection == null || !damageSection.contains("formula")) {
            return;
        }

        String formula = damageSection.getString("formula", "");
        if (formula.isEmpty()) {
            return;
        }

        // 数式から変数名を抽出してデフォルト値を設定
        Map<String, Double> detectedVars = detectVariablesFromFormula(formula);

        if (!detectedVars.isEmpty() && !config.contains("variables")) {
            ConfigurationSection variablesSection = config.createSection("variables");
            for (Map.Entry<String, Double> entry : detectedVars.entrySet()) {
                variablesSection.set(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 数式から変数を検出します
     *
     * @param formula 数式文字列
     * @return 変数名とデフォルト値のマップ
     */
    private Map<String, Double> detectVariablesFromFormula(String formula) {
        Map<String, Double> variables = new HashMap<>();

        // 既知の変数パターンを検出
        String[] knownPatterns = {
                "_mod", "_scale", "_base", "base_", "multiplier"
        };

        // 数式を解析して小文字のアルファベット文字列を抽出
        String[] tokens = formula.split("[^a-zA-Z0-9_]");
        for (String token : tokens) {
            if (token.length() > 2) {
                for (String pattern : knownPatterns) {
                    if (token.toLowerCase().contains(pattern) && !token.equals("formula")) {
                        // 既知の変数パターンに一致
                        if (!variables.containsKey(token)) {
                            variables.put(token, 1.0); // デフォルト値
                        }
                        break;
                    }
                }
            }
        }

        return variables;
    }

    /**
     * ステータス名を短縮名に変換します
     *
     * @param statName ステータス名
     * @return 短縮名
     */
    private String convertStatToShortName(String statName) {
        if (statName == null) {
            return "STR";
        }

        switch (statName.toUpperCase()) {
            case "STRENGTH":
                return "STR";
            case "INTELLIGENCE":
            case "INTEL":
                return "INT";
            case "VITALITY":
            case "VIT":
                return "VIT";
            case "DEXTERITY":
            case "DEX":
                return "DEX";
            case "SPIRIT":
                return "SPI";
            case "LUCK":
            case "AGILITY":
            case "AGI":
                return statName.substring(0, 3).toUpperCase();
            default:
                // 短縮形を返す
                return statName.length() > 3 ? statName.substring(0, 3).toUpperCase() : statName.toUpperCase();
        }
    }

    /**
     * ファイルが新フォーマットかチェックします
     *
     * @param file YAMLファイル
     * @return 新フォーマットの場合true
     */
    private boolean isNewFormat(File file) {
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            // 新フォーマットの指標
            boolean hasCooldownSection = config.isConfigurationSection("cooldown");
            boolean hasCostSection = config.contains("cost");
            boolean hasDamageFormula = config.isConfigurationSection("damage")
                    && config.getConfigurationSection("damage").contains("formula");
            boolean hasTargeting = config.contains("targeting");
            boolean hasVariables = config.contains("variables");

            // いずれかの新フォーマット指標があれば新フォーマットと判定
            return hasCooldownSection || hasCostSection || hasDamageFormula
                    || hasTargeting || hasVariables;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * バックアップファイルを作成します
     *
     * @param originalFile 元ファイル
     * @return バックアップファイル
     */
    private File createBackup(File originalFile) {
        String fileName = originalFile.getName();
        File backupFile = new File(legacyBackupDirectory, fileName);

        try {
            Files.copy(originalFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("バックアップ作成: " + backupFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "バックアップ作成失敗: " + fileName, e);
        }

        return backupFile;
    }

    /**
     * バックアップディレクトリを作成します
     */
    private void createBackupDirectory() {
        if (!legacyBackupDirectory.exists()) {
            legacyBackupDirectory.mkdirs();
            LOGGER.info("バックアップディレクトリ作成: " + legacyBackupDirectory.getAbsolutePath());
        }
    }

    /**
     * 全スキルファイルを収集します
     *
     * @param directory スキルディレクトリ
     * @return YAMLファイルリスト
     */
    private List<File> collectSkillFiles(File directory) {
        List<File> files = new ArrayList<>();

        // サブディレクトリも含めて探索
        File[] subdirs = directory.listFiles(File::isDirectory);
        if (subdirs != null) {
            for (File subdir : subdirs) {
                files.addAll(collectSkillFiles(subdir));
            }
        }

        // カレントディレクトリのYAMLファイル
        File[] yamlFiles = directory.listFiles((dir, name) ->
                name.endsWith(".yml") || name.endsWith(".yaml"));
        if (yamlFiles != null) {
            Collections.addAll(files, yamlFiles);
        }

        return files;
    }

    /**
     * スキルファイルを検索します
     *
     * @param fileName ファイル名
     * @return ファイル、見つからない場合はnull
     */
    private File findSkillFile(String fileName) {
        List<File> allFiles = collectSkillFiles(skillsDirectory);
        return allFiles.stream()
                .filter(f -> f.getName().equals(fileName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 移行レポート
     */
    public static class MigrationReport {
        private final List<String> migratedFiles = new ArrayList<>();
        private final List<String> backupFiles = new ArrayList<>();
        private final List<String> skippedFiles = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();

        public void addMigrated(String fileName, String backupName) {
            migratedFiles.add(fileName);
            backupFiles.add(backupName);
        }

        public void addSkipped(String fileName) {
            skippedFiles.add(fileName);
        }

        public void addError(String error) {
            errors.add(error);
        }

        public List<String> getMigratedFiles() {
            return Collections.unmodifiableList(migratedFiles);
        }

        public List<String> getBackupFiles() {
            return Collections.unmodifiableList(backupFiles);
        }

        public List<String> getSkippedFiles() {
            return Collections.unmodifiableList(skippedFiles);
        }

        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        public int getMigratedCount() {
            return migratedFiles.size();
        }

        public int getSkippedCount() {
            return skippedFiles.size();
        }

        public int getErrorCount() {
            return errors.size();
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        /**
         * レポートを文字列として出力します
         *
         * @return レポート文字列
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== スキル移行レポート ===\n");
            sb.append("移行完了: ").append(getMigratedCount()).append("ファイル\n");
            sb.append("スキップ: ").append(getSkippedCount()).append("ファイル\n");
            sb.append("エラー: ").append(getErrorCount()).append("ファイル\n");

            if (!migratedFiles.isEmpty()) {
                sb.append("\n[移行完了ファイル]\n");
                for (String file : migratedFiles) {
                    sb.append("  - ").append(file).append("\n");
                }
            }

            if (!skippedFiles.isEmpty()) {
                sb.append("\n[スキップファイル]\n");
                for (String file : skippedFiles) {
                    sb.append("  - ").append(file).append("\n");
                }
            }

            if (!errors.isEmpty()) {
                sb.append("\n[エラー]\n");
                for (String error : errors) {
                    sb.append("  - ").append(error).append("\n");
                }
            }

            return sb.toString();
        }
    }

    /**
     * 移行をロールバックします
     *
     * @return ロールバック成功時true
     */
    public boolean rollback() {
        if (!legacyBackupDirectory.exists()) {
            LOGGER.warning("バックアップディレクトリが存在しません");
            return false;
        }

        File[] backupFiles = legacyBackupDirectory.listFiles((dir, name) ->
                name.endsWith(".yml") || name.endsWith(".yaml"));

        if (backupFiles == null || backupFiles.length == 0) {
            LOGGER.warning("バックアップファイルが存在しません");
            return false;
        }

        int restoredCount = 0;
        for (File backupFile : backupFiles) {
            // 元の場所を探す
            List<File> allFiles = collectSkillFiles(skillsDirectory);
            for (File targetFile : allFiles) {
                if (targetFile.getName().equals(backupFile.getName())) {
                    try {
                        Files.copy(backupFile.toPath(), targetFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                        restoredCount++;
                        LOGGER.info("ロールバック完了: " + targetFile.getName());
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "ロールバック失敗: " + targetFile.getName(), e);
                    }
                    break;
                }
            }
        }

        LOGGER.info("ロールバック完了: " + restoredCount + "ファイル");
        return restoredCount > 0;
    }
}
