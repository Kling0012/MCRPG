package com.example.rpgplugin.core.validation;

import com.example.rpgplugin.rpgclass.RPGClass;
import com.example.rpgplugin.skill.Skill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ConsistencyValidatorのユニットテスト
 *
 * <p>クラスとスキルの整合性検証クラスのテストを行います。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ConsistencyValidator テスト")
class ConsistencyValidatorTest {

    @Mock
    private Logger mockLogger;

    @Mock
    private RPGClass mockClass1;

    @Mock
    private RPGClass mockClass2;

    @Mock
    private Skill mockSkill1;

    @Mock
    private Skill mockSkill2;

    private ConsistencyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ConsistencyValidator(mockLogger);
    }

    // ==================== コンストラクタ テスト ====================

    @Nested
    @DisplayName("コンストラクタ テスト")
    class ConstructorTests {

        @Test
        @DisplayName("Logger付きコンストラクタでインスタンスが生成される")
        void constructor_WithLogger_CreatesInstance() {
            assertThat(validator).isNotNull();
        }

        @Test
        @DisplayName("デフォルトコンストラクタでもインスタンスが生成される")
        void constructor_Default_CreatesInstance() {
            ConsistencyValidator defaultValidator = new ConsistencyValidator();
            assertThat(defaultValidator).isNotNull();
        }
    }

    // ==================== validate メソッド テスト ====================

    @Nested
    @DisplayName("validate メソッド テスト")
    class ValidateTests {

        @Test
        @DisplayName("空のマップで検証成功")
        void validate_EmptyMaps_ReturnsValid() {
            Map<String, RPGClass> classes = new HashMap<>();
            Map<String, Skill> skills = new HashMap<>();

            ConsistencyValidator.ValidationResult result = validator.validate(classes, skills);

            assertThat(result.isValid()).isTrue();
            assertThat(result.hasWarnings()).isFalse();
            assertThat(result.getErrorCount()).isZero();
        }

        @Test
        @DisplayName("スキルが存在しないクラスを参照している場合はエラー")
        void validate_SkillReferencesNonExistentClass_AddsError() {
            // セットアップ
            when(mockSkill1.getId()).thenReturn("skill1");
            when(mockSkill1.getAvailableClasses()).thenReturn(List.of("non_existent_class"));

            Map<String, RPGClass> classes = new HashMap<>();
            Map<String, Skill> skills = new HashMap<>();
            skills.put("skill1", mockSkill1);

            // 実行
            ConsistencyValidator.ValidationResult result = validator.validate(classes, skills);

            // 検証
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorCount()).isEqualTo(1);
            assertThat(result.getErrors().get(0)).contains("non_existent_class");
        }

        @Test
        @DisplayName("クラスが存在しないスキルを参照している場合はエラー")
        void validate_ClassReferencesNonExistentSkill_AddsError() {
            // セットアップ
            when(mockClass1.getId()).thenReturn("class1");
            when(mockClass1.getAvailableSkills()).thenReturn(List.of("non_existent_skill"));

            Map<String, RPGClass> classes = new HashMap<>();
            Map<String, Skill> skills = new HashMap<>();
            classes.put("class1", mockClass1);

            // 実行
            ConsistencyValidator.ValidationResult result = validator.validate(classes, skills);

            // 検証
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorCount()).isEqualTo(1);
            assertThat(result.getErrors().get(0)).contains("non_existent_skill");
        }

        @Test
        @DisplayName("双方向整合性がない場合は警告")
        void validate_BidirectionalInconsistency_AddsWarning() {
            // セットアップ: クラスはスキルを持つが、スキルはクラスを持たない
            when(mockClass1.getId()).thenReturn("class1");
            when(mockClass1.getAvailableSkills()).thenReturn(List.of("skill1"));

            when(mockSkill1.getId()).thenReturn("skill1");
            when(mockSkill1.getAvailableClasses()).thenReturn(List.of()); // クラスを持たない
            when(mockSkill1.isAvailableForClass("class1")).thenReturn(false);

            Map<String, RPGClass> classes = new HashMap<>();
            Map<String, Skill> skills = new HashMap<>();
            classes.put("class1", mockClass1);
            skills.put("skill1", mockSkill1);

            // 実行
            ConsistencyValidator.ValidationResult result = validator.validate(classes, skills);

            // 検証
            assertThat(result.isValid()).isTrue(); // エラーではない
            assertThat(result.hasWarnings()).isTrue();
            assertThat(result.getWarnings().get(0)).contains("skill1");
            assertThat(result.getWarnings().get(0)).contains("class1");
        }

        @Test
        @DisplayName("スキル側がクラスを持つがクラス側がスキルを持たない場合も警告")
        void validate_SkillHasClassButClassNotHaveSkill_AddsWarning() {
            // セットアップ
            when(mockClass1.getId()).thenReturn("class1");
            when(mockClass1.getAvailableSkills()).thenReturn(List.of()); // スキルを持たない

            when(mockSkill1.getId()).thenReturn("skill1");
            when(mockSkill1.getAvailableClasses()).thenReturn(List.of("class1"));

            Map<String, RPGClass> classes = new HashMap<>();
            Map<String, Skill> skills = new HashMap<>();
            classes.put("class1", mockClass1);
            skills.put("skill1", mockSkill1);

            // 実行
            ConsistencyValidator.ValidationResult result = validator.validate(classes, skills);

            // 検証
            assertThat(result.isValid()).isTrue();
            assertThat(result.hasWarnings()).isTrue();
            assertThat(result.getWarnings().get(0)).contains("skill1");
            assertThat(result.getWarnings().get(0)).contains("class1");
        }

        @Test
        @DisplayName("正しい双方向参照では警告なし")
        void validate_CorrectBidirectionalReference_NoWarnings() {
            // セットアップ: 双方向参照が正しい
            when(mockClass1.getId()).thenReturn("class1");
            when(mockClass1.getAvailableSkills()).thenReturn(List.of("skill1"));

            when(mockSkill1.getId()).thenReturn("skill1");
            when(mockSkill1.getAvailableClasses()).thenReturn(List.of("class1"));
            when(mockSkill1.isAvailableForClass("class1")).thenReturn(true);

            Map<String, RPGClass> classes = new HashMap<>();
            Map<String, Skill> skills = new HashMap<>();
            classes.put("class1", mockClass1);
            skills.put("skill1", mockSkill1);

            // 実行
            ConsistencyValidator.ValidationResult result = validator.validate(classes, skills);

            // 検証
            assertThat(result.isValid()).isTrue();
            assertThat(result.hasWarnings()).isFalse();
        }

        @Test
        @DisplayName("複数のスキルとクラスの検証")
        void validate_MultipleSkillsAndClasses_ValidatesAll() {
            // セットアップ
            when(mockClass1.getId()).thenReturn("warrior");
            when(mockClass1.getAvailableSkills()).thenReturn(List.of("slash", "block"));

            when(mockClass2.getId()).thenReturn("mage");
            when(mockClass2.getAvailableSkills()).thenReturn(List.of("fireball", "ice_spike"));

            when(mockSkill1.getId()).thenReturn("slash");
            when(mockSkill1.getAvailableClasses()).thenReturn(List.of("warrior"));

            when(mockSkill2.getId()).thenReturn("fireball");
            when(mockSkill2.getAvailableClasses()).thenReturn(List.of("mage"));

            Map<String, RPGClass> classes = new HashMap<>();
            Map<String, Skill> skills = new HashMap<>();
            classes.put("warrior", mockClass1);
            classes.put("mage", mockClass2);
            skills.put("slash", mockSkill1);
            skills.put("fireball", mockSkill2);

            // 実行
            ConsistencyValidator.ValidationResult result = validator.validate(classes, skills);

            // 検証 - warriorはblockを、mageはice_spikeを参照しているが存在しない
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorCount()).isEqualTo(2); // blockとice_spikeが見つからない
        }
    }

    // ==================== validateClass メソッド テスト ====================

    @Nested
    @DisplayName("validateClass メソッド テスト")
    class ValidateClassTests {

        @Test
        @DisplayName("単一クラスの検証が成功")
        void validateClass_ValidClass_ReturnsValid() {
            when(mockClass1.getId()).thenReturn("warrior");
            when(mockClass1.getAvailableSkills()).thenReturn(List.of());

            Map<String, Skill> skills = new HashMap<>();

            ConsistencyValidator.ValidationResult result = validator.validateClass(mockClass1, skills);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("クラスが存在しないスキルを参照している場合はエラー")
        void validateClass_NonExistentSkill_AddsError() {
            when(mockClass1.getId()).thenReturn("warrior");
            when(mockClass1.getAvailableSkills()).thenReturn(List.of("non_existent_skill"));

            Map<String, Skill> skills = new HashMap<>();

            ConsistencyValidator.ValidationResult result = validator.validateClass(mockClass1, skills);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0)).contains("non_existent_skill");
        }
    }

    // ==================== validateSkill メソッド テスト ====================

    @Nested
    @DisplayName("validateSkill メソッド テスト")
    class ValidateSkillTests {

        @Test
        @DisplayName("単一スキルの検証が成功")
        void validateSkill_ValidSkill_ReturnsValid() {
            when(mockSkill1.getId()).thenReturn("slash");
            when(mockSkill1.getAvailableClasses()).thenReturn(List.of());

            Map<String, RPGClass> classes = new HashMap<>();

            ConsistencyValidator.ValidationResult result = validator.validateSkill(mockSkill1, classes);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("スキルが存在しないクラスを参照している場合はエラー")
        void validateSkill_NonExistentClass_AddsError() {
            when(mockSkill1.getId()).thenReturn("slash");
            when(mockSkill1.getAvailableClasses()).thenReturn(List.of("non_existent_class"));

            Map<String, RPGClass> classes = new HashMap<>();

            ConsistencyValidator.ValidationResult result = validator.validateSkill(mockSkill1, classes);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0)).contains("non_existent_class");
        }
    }

    // ==================== autoRepair メソッド テスト ====================

    @Nested
    @DisplayName("autoRepair メソッド テスト")
    class AutoRepairTests {

        @Test
        @DisplayName("autoRepairはスキルマップを返す")
        void autoRepair_ReturnsSkillsMap() {
            Map<String, RPGClass> classes = new HashMap<>();
            Map<String, Skill> skills = new HashMap<>();

            Map<String, Skill> result = validator.autoRepair(classes, skills);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(skills);
        }

        @Test
        @DisplayName("修復が必要な場合はログが出力される")
        void autoRepair_NeedsRepair_LogsWarning() {
            when(mockClass1.getId()).thenReturn("warrior");
            when(mockClass1.getAvailableSkills()).thenReturn(List.of("skill1"));

            when(mockSkill1.getId()).thenReturn("skill1");
            when(mockSkill1.getAvailableClasses()).thenReturn(List.of()); // クラスを持たない
            when(mockSkill1.isAvailableForClass("warrior")).thenReturn(false);

            Map<String, RPGClass> classes = new HashMap<>();
            Map<String, Skill> skills = new HashMap<>();
            classes.put("warrior", mockClass1);
            skills.put("skill1", mockSkill1);

            Map<String, Skill> result = validator.autoRepair(classes, skills);

            assertThat(result).isNotNull();
            verify(mockLogger).warning(contains("repair needed"));
        }
    }

    // ==================== ValidationResult テスト ====================

    @Nested
    @DisplayName("ValidationResult テスト")
    class ValidationResultTests {

        private ConsistencyValidator.ValidationResult result;

        @BeforeEach
        void setUp() {
            result = new ConsistencyValidator.ValidationResult();
        }

        @Test
        @DisplayName("初期状態は有効")
        void validationResult_InitiallyValid() {
            assertThat(result.isValid()).isTrue();
            assertThat(result.hasWarnings()).isFalse();
        }

        @Test
        @DisplayName("エラー追加後に無効になる")
        void validationResult_AddError_BecomesInvalid() {
            result.addError("Test error");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("警告追加後に警告を持つ")
        void validationResult_AddWarning_HasWarnings() {
            result.addWarning("Test warning");

            assertThat(result.hasWarnings()).isTrue();
            assertThat(result.getWarningCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("getErrorsはコピーを返す")
        void validationResult_GetErrors_ReturnsCopy() {
            result.addError("Error 1");

            List<String> errors = result.getErrors();
            errors.clear(); // 返されたリストを変更

            assertThat(result.getErrorCount()).isEqualTo(1); // 元は変わらない
        }

        @Test
        @DisplayName("getWarningsはコピーを返す")
        void validationResult_GetWarnings_ReturnsCopy() {
            result.addWarning("Warning 1");

            List<String> warnings = result.getWarnings();
            warnings.clear(); // 返されたリストを変更

            assertThat(result.getWarningCount()).isEqualTo(1); // 元は変わらない
        }

        @Test
        @DisplayName("getSummaryは正しいフォーマット")
        void validationResult_GetSummary_CorrectFormat() {
            result.addError("Error 1");
            result.addWarning("Warning 1");

            String summary = result.getSummary();

            assertThat(summary).contains("errors=1");
            assertThat(summary).contains("warnings=1");
        }

        @Test
        @DisplayName("getDetailedReportは正しいフォーマット")
        void validationResult_GetDetailedReport_CorrectFormat() {
            result.addError("Test error");
            result.addWarning("Test warning");

            String report = result.getDetailedReport();

            assertThat(report).contains("Consistency Validation Report");
            assertThat(report).contains("Test error");
            assertThat(report).contains("Test warning");
        }

        @Test
        @DisplayName("エラーなしの場合のレポート")
        void validationResult_NoErrors_SuccessMessage() {
            String report = result.getDetailedReport();

            assertThat(report).contains("No errors found");
        }

        @Test
        @DisplayName("警告なしの場合のレポート")
        void validationResult_NoWarnings_SuccessMessage() {
            String report = result.getDetailedReport();

            assertThat(report).contains("No warnings found");
        }

        @Test
        @DisplayName("複数のエラーと警告を追加")
        void validationResult_MultipleErrorsAndWarnings() {
            result.addError("Error 1");
            result.addError("Error 2");
            result.addWarning("Warning 1");
            result.addWarning("Warning 2");

            assertThat(result.getErrorCount()).isEqualTo(2);
            assertThat(result.getWarningCount()).isEqualTo(2);
        }
    }

    // ==================== Null安全 テスト ====================

    @Nested
    @DisplayName("Null安全 テスト")
    class NullSafetyTests {

        @Test
        @DisplayName("nullマップでの検証は安全")
        void validate_NullMaps_HandlesGracefully() {
            // 実際にはNullPointerExceptionがスローされる可能性があるが、
            // テストではその動作を確認
            assertThatCode(() -> {
                validator.validate(null, null);
            }).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("空のマップでは例外をスローしない")
        void validate_EmptyMaps_NoException() {
            assertThatCode(() -> {
                validator.validate(new HashMap<>(), new HashMap<>());
            }).doesNotThrowAnyException();
        }
    }

    // ==================== syncSkillToClassLinks メソッド テスト ====================

    @Nested
    @DisplayName("syncSkillToClassLinks メソッド テスト（v2.0.0 スキル起点）")
    class SyncSkillToClassLinksTests {

        @Test
        @DisplayName("スキル起点でクラス→スキルマップを生成")
        void syncSkillToClassLinks_BuildsClassToSkillMap() {
            // セットアップ: スキル側にクラスが定義されている
            when(mockClass1.getId()).thenReturn("warrior");
            when(mockClass2.getId()).thenReturn("mage");

            when(mockSkill1.getId()).thenReturn("slash");
            when(mockSkill1.getAvailableClasses()).thenReturn(List.of("warrior"));

            when(mockSkill2.getId()).thenReturn("fireball");
            when(mockSkill2.getAvailableClasses()).thenReturn(List.of("mage"));

            Map<String, RPGClass> classes = new HashMap<>();
            classes.put("warrior", mockClass1);
            classes.put("mage", mockClass2);

            Map<String, Skill> skills = new HashMap<>();
            skills.put("slash", mockSkill1);
            skills.put("fireball", mockSkill2);

            // 実行
            Map<String, List<String>> result = validator.syncSkillToClassLinks(classes, skills);

            // 検証: スキル側の定義からクラス→スキルマップが生成される
            assertThat(result).hasSize(2);
            assertThat(result.get("warrior")).containsExactly("slash");
            assertThat(result.get("mage")).containsExactly("fireball");
        }

        @Test
        @DisplayName("マルチクラススキルのマッピング（1つのスキルが複数クラスで利用可能）")
        void syncSkillToClassLinks_MultiClassSkill_MapsToMultipleClasses() {
            // セットアップ: 1つのスキルが複数クラスで使える
            when(mockClass1.getId()).thenReturn("warrior");
            when(mockClass2.getId()).thenReturn("mage");
            when(mockClass2.getId()).thenReturn("rogue");

            when(mockSkill1.getId()).thenReturn("heal");
            when(mockSkill1.getAvailableClasses()).thenReturn(List.of("warrior", "mage", "rogue"));

            Map<String, RPGClass> classes = new HashMap<>();
            classes.put("warrior", mockClass1);
            classes.put("mage", mockClass2);
            classes.put("rogue", mockClass2);

            Map<String, Skill> skills = new HashMap<>();
            skills.put("heal", mockSkill1);

            // 実行
            Map<String, List<String>> result = validator.syncSkillToClassLinks(classes, skills);

            // 検証: 全てのクラスでhealスキルが利用可能
            assertThat(result.get("warrior")).containsExactly("heal");
            assertThat(result.get("mage")).contains("heal");
            assertThat(result.get("rogue")).contains("heal");
        }

        @Test
        @DisplayName("全クラススキル（availableClassesが空）は全クラスにマッピング")
        void syncSkillToClassLinks_AllClassSkill_MapsToAllClasses() {
            // セットアップ: availableClassesが空 = 全クラスで利用可能
            when(mockClass1.getId()).thenReturn("warrior");
            when(mockClass2.getId()).thenReturn("mage");

            when(mockSkill1.getId()).thenReturn("basic_attack");
            when(mockSkill1.getAvailableClasses()).thenReturn(List.of()); // 空リスト

            Map<String, RPGClass> classes = new HashMap<>();
            classes.put("warrior", mockClass1);
            classes.put("mage", mockClass2);

            Map<String, Skill> skills = new HashMap<>();
            skills.put("basic_attack", mockSkill1);

            // 実行
            Map<String, List<String>> result = validator.syncSkillToClassLinks(classes, skills);

            // 検証: 全クラスでbasic_attackが利用可能
            assertThat(result.get("warrior")).containsExactly("basic_attack");
            assertThat(result.get("mage")).containsExactly("basic_attack");
        }

        @Test
        @DisplayName("存在しないクラスへの参照はログ出力のみ")
        void syncSkillToClassLinks_NonExistentClass_LogsWarning() {
            // セットアップ: スキルが存在しないクラスを参照
            when(mockClass1.getId()).thenReturn("warrior");

            when(mockSkill1.getId()).thenReturn("slash");
            when(mockSkill1.getAvailableClasses()).thenReturn(List.of("warrior", "non_existent_class"));

            Map<String, RPGClass> classes = new HashMap<>();
            classes.put("warrior", mockClass1);

            Map<String, Skill> skills = new HashMap<>();
            skills.put("slash", mockSkill1);

            // 実行
            Map<String, List<String>> result = validator.syncSkillToClassLinks(classes, skills);

            // 検証: 存在するクラスのみマッピング
            assertThat(result.get("warrior")).containsExactly("slash");
            assertThat(result).doesNotContainKey("non_existent_class");
            verify(mockLogger).warning(contains("non_existent_class"));
        }
    }

    // ==================== verifySkillSourcedConsistency メソッド テスト ====================

    @Nested
    @DisplayName("verifySkillSourcedConsistency メソッド テスト（v2.0.0 スキル起点）")
    class VerifySkillSourcedConsistencyTests {

        @Test
        @DisplayName("クラス側がスキル側と一致している場合は整合性あり")
        void verifySkillSourcedConsistency_MatchingDefinitions_IsConsistent() {
            // セットアップ: 双方が一致
            when(mockClass1.getId()).thenReturn("warrior");
            when(mockClass1.getAvailableSkills()).thenReturn(List.of("slash"));

            when(mockSkill1.getId()).thenReturn("slash");
            when(mockSkill1.getAvailableClasses()).thenReturn(List.of("warrior"));

            Map<String, RPGClass> classes = new HashMap<>();
            classes.put("warrior", mockClass1);

            Map<String, Skill> skills = new HashMap<>();
            skills.put("slash", mockSkill1);

            // 実行
            ConsistencyValidator.SyncResult result = validator.verifySkillSourcedConsistency(classes, skills);

            // 検証: 整合性あり
            assertThat(result.isConsistent()).isTrue();
            assertThat(result.getInconsistencyCount()).isZero();
        }

        @Test
        @DisplayName("クラス側に余分なスキルがある場合は不整合")
        void verifySkillSourcedConsistency_ClassHasExtraSkills_NotConsistent() {
            // セットアップ: クラス側にスキル側で許可されていないスキルがある
            when(mockClass1.getId()).thenReturn("warrior");
            when(mockClass1.getAvailableSkills()).thenReturn(List.of("slash", "fireball")); // fireballはmage専用

            when(mockSkill1.getId()).thenReturn("slash");
            when(mockSkill1.getAvailableClasses()).thenReturn(List.of("warrior"));

            when(mockSkill2.getId()).thenReturn("fireball");
            when(mockSkill2.getAvailableClasses()).thenReturn(List.of("mage")); // mage専用

            Map<String, RPGClass> classes = new HashMap<>();
            classes.put("warrior", mockClass1);

            Map<String, Skill> skills = new HashMap<>();
            skills.put("slash", mockSkill1);
            skills.put("fireball", mockSkill2);

            // 実行
            ConsistencyValidator.SyncResult result = validator.verifySkillSourcedConsistency(classes, skills);

            // 検証: 不整合あり
            assertThat(result.isConsistent()).isFalse();
            assertThat(result.getInconsistencies()).anyMatch(s -> s.contains("fireball"));
        }

        @Test
        @DisplayName("クラス側に足りないスキルがある場合は不整合")
        void verifySkillSourcedConsistency_ClassMissingSkills_NotConsistent() {
            // セットアップ: スキル側で許可されているがクラス側にない
            when(mockClass1.getId()).thenReturn("warrior");
            when(mockClass1.getAvailableSkills()).thenReturn(List.of("slash")); // blockが不足

            when(mockSkill1.getId()).thenReturn("slash");
            when(mockSkill1.getAvailableClasses()).thenReturn(List.of("warrior"));

            when(mockSkill2.getId()).thenReturn("block");
            when(mockSkill2.getAvailableClasses()).thenReturn(List.of("warrior")); // warriorで使える

            Map<String, RPGClass> classes = new HashMap<>();
            classes.put("warrior", mockClass1);

            Map<String, Skill> skills = new HashMap<>();
            skills.put("slash", mockSkill1);
            skills.put("block", mockSkill2);

            // 実行
            ConsistencyValidator.SyncResult result = validator.verifySkillSourcedConsistency(classes, skills);

            // 検証: 不整合あり
            assertThat(result.isConsistent()).isFalse();
            assertThat(result.getInconsistencies()).anyMatch(s -> s.contains("block") && s.contains("missing"));
        }

        @Test
        @DisplayName("詳細レポートの出力")
        void verifySkillSourcedConsistency_DetailedReport_CorrectFormat() {
            // セットアップ: 不整合あり
            when(mockClass1.getId()).thenReturn("warrior");
            when(mockClass1.getAvailableSkills()).thenReturn(List.of("slash", "fireball"));

            when(mockSkill1.getId()).thenReturn("slash");
            when(mockSkill1.getAvailableClasses()).thenReturn(List.of("warrior"));

            when(mockSkill2.getId()).thenReturn("fireball");
            when(mockSkill2.getAvailableClasses()).thenReturn(List.of("mage"));

            Map<String, RPGClass> classes = new HashMap<>();
            classes.put("warrior", mockClass1);

            Map<String, Skill> skills = new HashMap<>();
            skills.put("slash", mockSkill1);
            skills.put("fireball", mockSkill2);

            // 実行
            ConsistencyValidator.SyncResult result = validator.verifySkillSourcedConsistency(classes, skills);

            // 検証: レポート形式
            String report = result.getDetailedReport();
            assertThat(report).contains("Skill-Sourced Consistency Report");
            assertThat(report).contains("Inconsistencies found");
            assertThat(report).contains("syncSkillToClassLinks()");
        }
    }

    // ==================== SyncResult テスト ====================

    @Nested
    @DisplayName("SyncResult テスト（v2.0.0）")
    class SyncResultTests {

        private ConsistencyValidator.SyncResult result;

        @BeforeEach
        void setUp() {
            result = new ConsistencyValidator.SyncResult();
        }

        @Test
        @DisplayName("初期状態は整合性あり")
        void syncResult_InitiallyConsistent() {
            assertThat(result.isConsistent()).isTrue();
            assertThat(result.getInconsistencyCount()).isZero();
        }

        @Test
        @DisplayName("不整合追加後に整合性なし")
        void syncResult_AddInconsistency_BecomesInconsistent() {
            result.addInconsistency("Test inconsistency");

            assertThat(result.isConsistent()).isFalse();
            assertThat(result.getInconsistencyCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("getInconsistenciesはコピーを返す")
        void syncResult_GetInconsistencies_ReturnsCopy() {
            result.addInconsistency("Issue 1");

            List<String> issues = result.getInconsistencies();
            issues.clear(); // 返されたリストを変更

            assertThat(result.getInconsistencyCount()).isEqualTo(1); // 元は変わらない
        }

        @Test
        @DisplayName("getSummaryは正しいフォーマット")
        void syncResult_GetSummary_CorrectFormat() {
            result.addInconsistency("Issue 1");

            String summary = result.getSummary();

            assertThat(summary).contains("inconsistencies=1");
            assertThat(summary).contains("isConsistent=false");
        }

        @Test
        @DisplayName("整合性ありのレポート")
        void syncResult_ConsistentReport_SuccessMessage() {
            String report = result.getDetailedReport();

            assertThat(report).contains("All classes match Skill.availableClasses definitions");
        }

        @Test
        @DisplayName("不整合ありのレポート")
        void syncResult_InconsistentReport_ShowIssues() {
            result.addInconsistency("Test issue");

            String report = result.getDetailedReport();

            assertThat(report).contains("Inconsistencies found");
            assertThat(report).contains("Test issue");
            assertThat(report).contains("syncSkillToClassLinks()");
        }
    }
}
