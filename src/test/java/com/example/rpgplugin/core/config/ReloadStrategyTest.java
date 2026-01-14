package com.example.rpgplugin.core.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ReloadStrategyのユニットテスト
 *
 * <p>設定ファイルのリロード戦略列挙型のテストを行います。</p>
 *
 * 設計原則:
 * - SOLID-S: ReloadStrategyのテストに特化
 * - KISS: シンプルなテストケース
 * - 読みやすさ: テスト名で振る舞いを明示
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("ReloadStrategy テスト")
class ReloadStrategyTest {

    // ==================== 列挙値確認 ====================

    @Nested
    @DisplayName("Enum Values")
    class EnumValuesTests {

        @Test
        @DisplayName("全ての値が存在する")
        void values_ContainsAllStrategies() {
            ReloadStrategy[] values = ReloadStrategy.values();

            assertThat(values).hasSize(4);
            assertThat(values).containsExactly(
                ReloadStrategy.IMMEDIATE,
                ReloadStrategy.MANUAL,
                ReloadStrategy.INDIVIDUAL,
                ReloadStrategy.DELAYED
            );
        }

        @Test
        @DisplayName("valueOfが正しく動作する")
        void valueOf_WorksCorrectly() {
            assertThat(ReloadStrategy.valueOf("IMMEDIATE")).isEqualTo(ReloadStrategy.IMMEDIATE);
            assertThat(ReloadStrategy.valueOf("MANUAL")).isEqualTo(ReloadStrategy.MANUAL);
            assertThat(ReloadStrategy.valueOf("INDIVIDUAL")).isEqualTo(ReloadStrategy.INDIVIDUAL);
            assertThat(ReloadStrategy.valueOf("DELAYED")).isEqualTo(ReloadStrategy.DELAYED);
        }
    }

    // ==================== 即時リロード ====================

    @Nested
    @DisplayName("IMMEDIATE")
    class ImmediateTests {

        @Test
        @DisplayName("表示名が正しい")
        void immediate_DisplayName_IsCorrect() {
            assertThat(ReloadStrategy.IMMEDIATE.getDisplayName()).isEqualTo("即時リロード");
        }

        @Test
        @DisplayName("自動リロードが有効")
        void immediate_AutoReload_IsTrue() {
            assertThat(ReloadStrategy.IMMEDIATE.isAutoReload()).isTrue();
        }

        @Test
        @DisplayName("toStringが表示名を返す")
        void immediate_ToString_ReturnsDisplayName() {
            assertThat(ReloadStrategy.IMMEDIATE.toString()).isEqualTo("即時リロード");
        }
    }

    // ==================== 手動リロード ====================

    @Nested
    @DisplayName("MANUAL")
    class ManualTests {

        @Test
        @DisplayName("表示名が正しい")
        void manual_DisplayName_IsCorrect() {
            assertThat(ReloadStrategy.MANUAL.getDisplayName()).isEqualTo("手動リロード");
        }

        @Test
        @DisplayName("自動リロードが無効")
        void manual_AutoReload_IsFalse() {
            assertThat(ReloadStrategy.MANUAL.isAutoReload()).isFalse();
        }

        @Test
        @DisplayName("toStringが表示名を返す")
        void manual_ToString_ReturnsDisplayName() {
            assertThat(ReloadStrategy.MANUAL.toString()).isEqualTo("手動リロード");
        }
    }

    // ==================== 個別リロード ====================

    @Nested
    @DisplayName("INDIVIDUAL")
    class IndividualTests {

        @Test
        @DisplayName("表示名が正しい")
        void individual_DisplayName_IsCorrect() {
            assertThat(ReloadStrategy.INDIVIDUAL.getDisplayName()).isEqualTo("個別リロード");
        }

        @Test
        @DisplayName("自動リロードが無効")
        void individual_AutoReload_IsFalse() {
            assertThat(ReloadStrategy.INDIVIDUAL.isAutoReload()).isFalse();
        }

        @Test
        @DisplayName("toStringが表示名を返す")
        void individual_ToString_ReturnsDisplayName() {
            assertThat(ReloadStrategy.INDIVIDUAL.toString()).isEqualTo("個別リロード");
        }
    }

    // ==================== 遅延リロード ====================

    @Nested
    @DisplayName("DELAYED")
    class DelayedTests {

        @Test
        @DisplayName("表示名が正しい")
        void delayed_DisplayName_IsCorrect() {
            assertThat(ReloadStrategy.DELAYED.getDisplayName()).isEqualTo("遅延リロード");
        }

        @Test
        @DisplayName("自動リロードが無効")
        void delayed_AutoReload_IsFalse() {
            assertThat(ReloadStrategy.DELAYED.isAutoReload()).isFalse();
        }

        @Test
        @DisplayName("toStringが表示名を返す")
        void delayed_ToString_ReturnsDisplayName() {
            assertThat(ReloadStrategy.DELAYED.toString()).isEqualTo("遅延リロード");
        }
    }

    // ==================== 自動リロード ====================

    @Nested
    @DisplayName("Auto Reload Strategy")
    class AutoReloadStrategyTests {

        @Test
        @DisplayName("自動リロード戦略のみがtrueを返す")
        void autoReload_OnlyImmediate_IsTrue() {
            for (ReloadStrategy strategy : ReloadStrategy.values()) {
                if (strategy == ReloadStrategy.IMMEDIATE) {
                    assertThat(strategy.isAutoReload()).isTrue();
                } else {
                    assertThat(strategy.isAutoReload()).isFalse();
                }
            }
        }
    }
}
