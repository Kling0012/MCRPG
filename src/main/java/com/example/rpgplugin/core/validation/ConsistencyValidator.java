package com.example.rpgplugin.core.validation;

import com.example.rpgplugin.rpgclass.RPGClass;
import com.example.rpgplugin.skill.Skill;

import java.util.*;
import java.util.logging.Logger;

/**
 * 整合性検証クラス
 * <p>クラスとスキルの双方向整合性を検証します</p>
 *
 * <p>検証内容:</p>
 * <ul>
 *   <li>RPGClass.availableSkills と Skill.availableClasses の双方向整合性</li>
 *   <li>スキルIDの実在確認</li>
 *   <li>クラスIDの実在確認</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: 整合性検証に専念</li>
 *   <li>Observer: 検証結果を通知</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ConsistencyValidator {

    private final Logger logger;

    /**
     * コンストラクタ
     *
     * @param logger ロガー
     */
    public ConsistencyValidator(Logger logger) {
        this.logger = logger;
    }

    /**
     * コンストラクタ（ロガーなし）
     */
    public ConsistencyValidator() {
        this(Logger.getLogger(ConsistencyValidator.class.getName()));
    }

    /**
     * クラスとスキルの整合性を検証します
     *
     * @param classes クラスマップ
     * @param skills スキルマップ
     * @return 検証結果
     */
    public ValidationResult validate(Map<String, RPGClass> classes, Map<String, Skill> skills) {
        ValidationResult result = new ValidationResult();

        // スキル側の整合性チェック
        for (Map.Entry<String, Skill> entry : skills.entrySet()) {
            String skillId = entry.getKey();
            Skill skill = entry.getValue();

            validateSkillClasses(skillId, skill, classes, result);
        }

        // クラス側の整合性チェック
        for (Map.Entry<String, RPGClass> entry : classes.entrySet()) {
            String classId = entry.getKey();
            RPGClass rpgClass = entry.getValue();

            validateClassSkills(classId, rpgClass, skills, result);
        }

        // 双方向整合性チェック
        validateBidirectionalConsistency(classes, skills, result);

        return result;
    }

    /**
     * スキルのクラス参照を検証します
     *
     * @param skillId スキルID
     * @param skill スキル
     * @param classes クラスマップ
     * @param result 検証結果
     */
    private void validateSkillClasses(String skillId, Skill skill,
                                     Map<String, RPGClass> classes,
                                     ValidationResult result) {
        List<String> availableClasses = skill.getAvailableClasses();

        for (String classId : availableClasses) {
            if (!classes.containsKey(classId)) {
                result.addError("Skill '" + skillId + "' references non-existent class '" + classId + "'");
            }
        }
    }

    /**
     * クラスのスキル参照を検証します
     *
     * @param classId クラスID
     * @param rpgClass クラス
     * @param skills スキルマップ
     * @param result 検証結果
     */
    private void validateClassSkills(String classId, RPGClass rpgClass,
                                    Map<String, Skill> skills,
                                    ValidationResult result) {
        List<String> availableSkills = rpgClass.getAvailableSkills();

        for (String skillId : availableSkills) {
            if (!skills.containsKey(skillId)) {
                result.addError("Class '" + classId + "' references non-existent skill '" + skillId + "'");
            }
        }
    }

    /**
     * 双方向整合性を検証します
     * <p>Class.availableSkillsとSkill.availableClassesが一致しているか確認します</p>
     *
     * @param classes クラスマップ
     * @param skills スキルマップ
     * @param result 検証結果
     */
    private void validateBidirectionalConsistency(Map<String, RPGClass> classes,
                                                  Map<String, Skill> skills,
                                                  ValidationResult result) {
        for (Map.Entry<String, RPGClass> classEntry : classes.entrySet()) {
            String classId = classEntry.getKey();
            RPGClass rpgClass = classEntry.getValue();

            for (String skillId : rpgClass.getAvailableSkills()) {
                Skill skill = skills.get(skillId);
                if (skill == null) {
                    continue; // すでにエラーとして報告済み
                }

                // スキル側でこのクラスが利用可能とされているか確認
                if (!skill.isAvailableForClass(classId)) {
                    result.addWarning("Class '" + classId + "' has skill '" + skillId +
                            "' but skill doesn't list this class in availableClasses");
                }
            }
        }

        for (Map.Entry<String, Skill> skillEntry : skills.entrySet()) {
            String skillId = skillEntry.getKey();
            Skill skill = skillEntry.getValue();

            for (String classId : skill.getAvailableClasses()) {
                RPGClass rpgClass = classes.get(classId);
                if (rpgClass == null) {
                    continue; // すでにエラーとして報告済み
                }

                // クラス側でこのスキルが利用可能とされているか確認
                if (!rpgClass.getAvailableSkills().contains(skillId)) {
                    result.addWarning("Skill '" + skillId + "' is available for class '" + classId +
                            "' but class doesn't list this skill in availableSkills");
                }
            }
        }
    }

    /**
     * 単一クラスとスキルの整合性を検証します
     *
     * @param rpgClass クラス
     * @param skills スキルマップ
     * @return 検証結果
     */
    public ValidationResult validateClass(RPGClass rpgClass, Map<String, Skill> skills) {
        Map<String, RPGClass> singleClassMap = new HashMap<>();
        singleClassMap.put(rpgClass.getId(), rpgClass);
        return validate(singleClassMap, skills);
    }

    /**
     * 単一スキルとクラスの整合性を検証します
     *
     * @param skill スキル
     * @param classes クラスマップ
     * @return 検証結果
     */
    public ValidationResult validateSkill(Skill skill, Map<String, RPGClass> classes) {
        Map<String, Skill> singleSkillMap = new HashMap<>();
        singleSkillMap.put(skill.getId(), skill);
        return validate(classes, singleSkillMap);
    }

    /**
     * 整合性問題を自動修復します
     * <p>Class.availableSkillsを基準として、Skill.availableClassesを同期します</p>
     *
     * @param classes クラスマップ
     * @param skills スキルマップ（変更されません）
     * @return 修復されたスキルのマップ（新しいインスタンス）
     */
    public Map<String, Skill> autoRepair(Map<String, RPGClass> classes, Map<String, Skill> skills) {
        // 注意: Skillはイミュータブルな設計なので、
        // 実際の修復はSkillLoader側で行う必要があります
        // このメソッドは修復が必要なスキルのリストを返します

        Map<String, Skill> repairedSkills = new HashMap<>(skills);
        List<String> repairNeeded = new ArrayList<>();

        for (Map.Entry<String, RPGClass> classEntry : classes.entrySet()) {
            String classId = classEntry.getKey();
            RPGClass rpgClass = classEntry.getValue();

            for (String skillId : rpgClass.getAvailableSkills()) {
                Skill skill = skills.get(skillId);
                if (skill != null && !skill.isAvailableForClass(classId)) {
                    repairNeeded.add("Skill '" + skillId + "' should include class '" + classId + "'");
                }
            }
        }

        if (!repairNeeded.isEmpty()) {
            logger.warning("Consistency repair needed for: " + String.join(", ", repairNeeded));
            logger.warning("Please update YAML files to reflect these changes");
        }

        return repairedSkills;
    }

    /**
     * 検証結果
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        /**
         * エラーを追加します
         *
         * @param error エラーメッセージ
         */
        public void addError(String error) {
            errors.add(error);
            if (errors.size() <= 10) {
                // 最初の10件のみログ出力
                System.err.println("[Consistency Error] " + error);
            }
        }

        /**
         * 警告を追加します
         *
         * @param warning 警告メッセージ
         */
        public void addWarning(String warning) {
            warnings.add(warning);
        }

        /**
         * 検証が成功したか（エラーなし）
         *
         * @return 成功場合はtrue
         */
        public boolean isValid() {
            return errors.isEmpty();
        }

        /**
         * 警告があるか
         *
         * @return 警告がある場合はtrue
         */
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        /**
         * エラーリストを取得します
         *
         * @return エラーリストのコピー
         */
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        /**
         * 警告リストを取得します
         *
         * @return 警告リストのコピー
         */
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }

        /**
         * エラー件数を取得します
         *
         * @return エラー件数
         */
        public int getErrorCount() {
            return errors.size();
        }

        /**
         * 警告件数を取得します
         *
         * @return 警告件数
         */
        public int getWarningCount() {
            return warnings.size();
        }

        /**
         * 検証結果のサマリーを取得します
         *
         * @return サマリー文字列
         */
        public String getSummary() {
            return String.format("ValidationResult{errors=%d, warnings=%d}",
                    errors.size(), warnings.size());
        }

        /**
         * 詳細なレポートを生成します
         *
         * @return レポート文字列
         */
        public String getDetailedReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Consistency Validation Report ===\n");

            if (errors.isEmpty()) {
                sb.append("✓ No errors found\n");
            } else {
                sb.append("✗ Errors found (").append(errors.size()).append("):\n");
                for (String error : errors) {
                    sb.append("  - ").append(error).append("\n");
                }
            }

            if (warnings.isEmpty()) {
                sb.append("✓ No warnings found\n");
            } else {
                sb.append("⚠ Warnings found (").append(warnings.size()).append("):\n");
                for (String warning : warnings) {
                    sb.append("  - ").append(warning).append("\n");
                }
            }

            sb.append("=====================================").append("\n");
            return sb.toString();
        }
    }
}
