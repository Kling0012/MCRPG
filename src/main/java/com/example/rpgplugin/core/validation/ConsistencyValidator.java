package com.example.rpgplugin.core.validation;

import com.example.rpgplugin.rpgclass.RPGClass;
import com.example.rpgplugin.skill.Skill;

import java.util.*;
import java.util.logging.Logger;

/**
 * 整合性検証クラス
 * <p>クラスとスキルの整合性を検証・同期します</p>
 *
 * <p>設計方針:</p>
 * <ul>
 *   <li>情報源（Single Source of Truth）: {@code Skill.availableClasses}</li>
 *   <li>派生データ: {@code RPGClass.availableSkills} はスキル側から自動生成</li>
 *   <li>双方向リンクは非推奨: スキル起点の一方向管理に移行</li>
 * </ul>
 *
 * <p>検証内容:</p>
 * <ul>
 *   <li>スキルIDの実在確認</li>
 *   <li>クラスIDの実在確認</li>
 *   <li>RPGClass.availableSkills が Skill.availableClasses と一致しているか</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: 整合性検証に専念</li>
 *   <li>Single Source of Truth: スキル側を正とする</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 2.0.0 - 情報源をスキル側に変更
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
     * スキル起点でクラスのスキルリストを生成します
     * <p>{@code Skill.availableClasses} を情報源として、各クラスが持つべきスキルリストを生成します。</p>
     *
     * <p>使用例:</p>
     * <pre>{@code
     * Map<String, List<String>> classSkills = validator.syncSkillToClassLinks(classes, skills);
     * // classSkills = { "warrior": ["slash", "bash"], "mage": ["fireball", "frost"] }
     * }</pre>
     *
     * @param classes クラスマップ
     * @param skills スキルマップ（情報源）
     * @return クラスID → スキルIDリストのマップ（スキル側から生成）
     */
    public Map<String, List<String>> syncSkillToClassLinks(Map<String, RPGClass> classes,
                                                           Map<String, Skill> skills) {
        Map<String, List<String>> classSkillMap = new HashMap<>();

        // すべてのクラスを初期化
        for (String classId : classes.keySet()) {
            classSkillMap.put(classId, new ArrayList<>());
        }

        // スキル側の定義からクラス→スキルのマッピングを構築
        for (Map.Entry<String, Skill> entry : skills.entrySet()) {
            String skillId = entry.getKey();
            Skill skill = entry.getValue();
            List<String> availableClasses = skill.getAvailableClasses();

            if (availableClasses.isEmpty()) {
                // 全クラスで利用可能なスキル
                for (String classId : classes.keySet()) {
                    classSkillMap.get(classId).add(skillId);
                }
            } else {
                // 特定クラスのみ利用可能
                for (String classId : availableClasses) {
                    if (classes.containsKey(classId)) {
                        classSkillMap.get(classId).add(skillId);
                    } else {
                        logger.warning("Skill '" + skillId + "' references non-existent class '" + classId + "'");
                    }
                }
            }
        }

        return classSkillMap;
    }

    /**
     * スキル起点で整合性チェックを行います
     * <p>{@code RPGClass.availableSkills} が {@code Skill.availableClasses} から生成される内容と一致しているか検証します。</p>
     *
     * @param classes クラスマップ
     * @param skills スキルマップ（情報源）
     * @return 一致性のレポート
     */
    public SyncResult verifySkillSourcedConsistency(Map<String, RPGClass> classes,
                                                     Map<String, Skill> skills) {
        SyncResult result = new SyncResult();

        // スキル側から正しいクラス→スキルマップを生成
        Map<String, List<String>> expectedMap = syncSkillToClassLinks(classes, skills);

        // 各クラスの実際のスキルリストと比較
        for (Map.Entry<String, RPGClass> entry : classes.entrySet()) {
            String classId = entry.getKey();
            RPGClass rpgClass = entry.getValue();
            List<String> actualSkills = rpgClass.getAvailableSkills();
            List<String> expectedSkills = expectedMap.getOrDefault(classId, Collections.emptyList());

            // 余分なスキル（クラス側にあるが、スキル側で許可されていない）
            List<String> extraSkills = new ArrayList<>(actualSkills);
            extraSkills.removeAll(expectedSkills);
            if (!extraSkills.isEmpty()) {
                result.addInconsistency("Class '" + classId + "' has skills not listed in Skill.availableClasses: " + extraSkills);
            }

            // 足りないスキル（スキル側で許可されているが、クラス側にない）
            List<String> missingSkills = new ArrayList<>(expectedSkills);
            missingSkills.removeAll(actualSkills);
            if (!missingSkills.isEmpty()) {
                result.addInconsistency("Class '" + classId + "' is missing skills listed in Skill.availableClasses: " + missingSkills);
            }
        }

        return result;
    }

    /**
     * 整合性問題を自動修復します（非推奨）
     * <p>クラス側を基準としてスキル側を同期しようとしていましたが、
     * これは設計方針の変更により非推奨となりました。</p>
     *
     * <p>代わりに {@link #syncSkillToClassLinks(Map, Map)} を使用してください。
     * スキル側を情報源として、クラス側のスキルリストを生成します。</p>
     *
     * @param classes クラスマップ
     * @param skills スキルマップ（変更されません）
     * @return 修復されたスキルのマップ（新しいインスタンス）
     * @deprecated 情報源はスキル側（{@code Skill.availableClasses}）に変更されました。
     *             代わりに {@link #syncSkillToClassLinks(Map, Map)} を使用してください。
     */
    @Deprecated
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

    /**
     * スキル起点整合性チェックの結果
     * <p>{@link #verifySkillSourcedConsistency(Map, Map)} の結果を表します。</p>
     *
     * @since 2.0.0
     */
    public static class SyncResult {
        private final List<String> inconsistencies = new ArrayList<>();

        /**
         * 不整合を追加します
         *
         * @param inconsistency 不整合メッセージ
         */
        public void addInconsistency(String inconsistency) {
            inconsistencies.add(inconsistency);
        }

        /**
         * 整合しているか（不整合なし）
         *
         * @return 整合している場合はtrue
         */
        public boolean isConsistent() {
            return inconsistencies.isEmpty();
        }

        /**
         * 不整合リストを取得します
         *
         * @return 不整合リストのコピー
         */
        public List<String> getInconsistencies() {
            return new ArrayList<>(inconsistencies);
        }

        /**
         * 不整合件数を取得します
         *
         * @return 不整合件数
         */
        public int getInconsistencyCount() {
            return inconsistencies.size();
        }

        /**
         * 結果のサマリーを取得します
         *
         * @return サマリー文字列
         */
        public String getSummary() {
            return String.format("SyncResult{inconsistencies=%d, isConsistent=%s}",
                    inconsistencies.size(), isConsistent());
        }

        /**
         * 詳細なレポートを生成します
         *
         * @return レポート文字列
         */
        public String getDetailedReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Skill-Sourced Consistency Report ===\n");

            if (inconsistencies.isEmpty()) {
                sb.append("✓ All classes match Skill.availableClasses definitions\n");
            } else {
                sb.append("✗ Inconsistencies found (").append(inconsistencies.size()).append("):\n");
                for (String issue : inconsistencies) {
                    sb.append("  - ").append(issue).append("\n");
                }
                sb.append("\nSuggestion: Run syncSkillToClassLinks() to generate correct class skill lists\n");
            }

            sb.append("========================================\n");
            return sb.toString();
        }
    }
}
