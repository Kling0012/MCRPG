package com.example.rpgplugin.skill.parser;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Logger;

/**
 * スキル設定バリデータ
 *
 * <p>スキルYAML設定のバリデーションを担当します。</p>
 */
public class SkillValidator {

    private final Logger logger;

    public SkillValidator(Logger logger) {
        this.logger = logger;
    }

    /**
     * 必須フィールドのバリデーション
     *
     * @param config   設定セクション
     * @param fileName ファイル名
     * @return バリデーション結果
     */
    public boolean validateRequiredFields(FileConfiguration config, String fileName) {
        if (!config.contains("id") || !config.contains("name") || !config.contains("type")) {
            logger.warning("必須フィールドが不足しています: " + fileName);
            return false;
        }
        return true;
    }

    /**
     * 整数範囲バリデーション
     *
     * @param value    検証値
     * @param min      最小値
     * @param max      最大値
     * @param fieldName フィールド名
     * @param fileName ファイル名
     */
    public void validateRange(int value, int min, int max, String fieldName, String fileName) {
        if (value < min || value > max) {
            logger.warning(String.format(
                "[Config] %s in %s: %d is out of range [%d, %d]",
                fieldName, fileName, value, min, max
            ));
        }
    }

    /**
     * 小数範囲バリデーション
     *
     * @param value    検証値
     * @param min      最小値
     * @param max      最大値
     * @param fieldName フィールド名
     * @param fileName ファイル名
     */
    public void validateRange(double value, double min, double max, String fieldName, String fileName) {
        if (value < min || value > max) {
            logger.warning(String.format(
                "[Config] %s in %s: %.2f is out of range [%.2f, %.2f]",
                fieldName, fileName, value, min, max
            ));
        }
    }

    /**
     * 数式構文バリデーション
     *
     * @param formula 検証する数式
     * @return バリデーション結果
     */
    public boolean validateFormulaSyntax(String formula) {
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

        // 危険なパターンチェック
        String dangerousPattern = "(eval|exec|runtime|process|system|cmd)";
        if (formula.toLowerCase().matches(".*" + dangerousPattern + ".*")) {
            return false;
        }

        return true;
    }
}
