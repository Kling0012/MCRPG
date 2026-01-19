package com.example.rpgplugin.core.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Configuration.ValidationResultのユニットテスト
 *
 * <p>設定バリデーション結果クラスのテストを行います。</p>
 *
 * 設計原則:
 * - SOLID-S: ValidationResultのテストに特化
 * - KISS: シンプルなテストケース
 * - 読みやすさ: テスト名で振る舞いを明示
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("Configuration.ValidationResult テスト")
class ValidationResultTest {

    // ==================== コンストラクタ ====================

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("有効な結果を作成")
        void constructor_ValidResult_CreatesValid() {
            Configuration.ValidationResult result = new Configuration.ValidationResult(true, null);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("無効な結果を作成")
        void constructor_InvalidResult_CreatesInvalid() {
            Configuration.ValidationResult result = new Configuration.ValidationResult(false, "Error message");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorMessage()).isEqualTo("Error message");
        }
    }

    // ==================== ファクトリメソッド ====================

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("successで有効な結果を作成")
        void success_CreatesValidResult() {
            Configuration.ValidationResult result = Configuration.ValidationResult.success();

            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("failureで無効な結果を作成")
        void failure_CreatesInvalidResult() {
            Configuration.ValidationResult result = Configuration.ValidationResult.failure("Test error");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorMessage()).isEqualTo("Test error");
        }

        @Test
        @DisplayName("failureでnullメッセージを許容")
        void failure_NullMessage_CreatesWithNull() {
            Configuration.ValidationResult result = Configuration.ValidationResult.failure(null);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorMessage()).isNull();
        }
    }

    // ==================== メソッド動作 ====================

    @Nested
    @DisplayName("Method Behavior")
    class MethodBehaviorTests {

        @Test
        @DisplayName("isValidが有効状態を正しく返す")
        void isValid_ReturnsCorrectState() {
            Configuration.ValidationResult valid = Configuration.ValidationResult.success();
            Configuration.ValidationResult invalid = Configuration.ValidationResult.failure("Error");

            assertThat(valid.isValid()).isTrue();
            assertThat(invalid.isValid()).isFalse();
        }

        @Test
        @DisplayName("getErrorMessageがエラーメッセージを返す")
        void getErrorMessage_ReturnsErrorMessage() {
            Configuration.ValidationResult result = Configuration.ValidationResult.failure("Test error");

            assertThat(result.getErrorMessage()).isEqualTo("Test error");
        }

        @Test
        @DisplayName("有効な結果のgetErrorMessageはnull")
        void getErrorMessage_ValidResult_ReturnsNull() {
            Configuration.ValidationResult result = Configuration.ValidationResult.success();

            assertThat(result.getErrorMessage()).isNull();
        }
    }

    // ==================== 複数インスタンス ====================

    @Nested
    @DisplayName("Multiple Instances")
    class MultipleInstancesTests {

        @Test
        @DisplayName("複数の成功結果は独立している")
        void multipleSuccessResults_AreIndependent() {
            Configuration.ValidationResult result1 = Configuration.ValidationResult.success();
            Configuration.ValidationResult result2 = Configuration.ValidationResult.success();

            assertThat(result1.isValid()).isTrue();
            assertThat(result2.isValid()).isTrue();
            // 異なるインスタンス
            assertThat(result1).isNotSameAs(result2);
        }

        @Test
        @DisplayName("複数の失敗結果は独立している")
        void multipleFailureResults_AreIndependent() {
            Configuration.ValidationResult result1 = Configuration.ValidationResult.failure("Error 1");
            Configuration.ValidationResult result2 = Configuration.ValidationResult.failure("Error 2");

            assertThat(result1.isValid()).isFalse();
            assertThat(result2.isValid()).isFalse();
            assertThat(result1.getErrorMessage()).isEqualTo("Error 1");
            assertThat(result2.getErrorMessage()).isEqualTo("Error 2");
        }
    }

    // ==================== 空文字列メッセージ ====================

    @Nested
    @DisplayName("Empty String Message")
    class EmptyStringMessageTests {

        @Test
        @DisplayName("空文字列のエラーメッセージを許容")
        void failure_EmptyMessage_CreatesWithEmpty() {
            Configuration.ValidationResult result = Configuration.ValidationResult.failure("");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorMessage()).isEqualTo("");
        }
    }
}
