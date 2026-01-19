package com.example.rpgplugin.damage.config;

import org.bukkit.configuration.ConfigurationSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * EventConfigのユニットテスト
 *
 * <p>ダメージイベント設定モデルのテストを行います。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EventConfig テスト")
class EventConfigTest {

    @Mock
    private ConfigurationSection mockSection;

    @Mock
    private ConfigurationSection mockConstantsSection;

    @Mock
    private ConfigurationSection mockCriticalSection;

    // ==================== Builder テスト ====================

    @Nested
    @DisplayName("Builder テスト")
    class BuilderTests {

        private EventConfig.Builder builder;

        @BeforeEach
        void setUp() {
            builder = new EventConfig.Builder();
        }

        @Test
        @DisplayName("デフォルト値で構築できる")
        void build_Defaults_HasDefaultValues() {
            EventConfig config = builder.build();

            assertThat(config.getDescription()).isNull();
            assertThat(config.getFormula()).isNull();
            assertThat(config.getFallbackFormula()).isNull();
            assertThat(config.getMinDamage()).isEqualTo(1.0);
            assertThat(config.getMaxDamage()).isEmpty();
            assertThat(config.getCritical()).isEmpty();
            assertThat(config.getPhysicalCutFormula()).isEmpty();
            assertThat(config.getMagicCutFormula()).isEmpty();
        }

        @Test
        @DisplayName("全フィールドを設定して構築できる")
        void build_AllFields_SetsAllValues() {
            EventConfig config = builder
                    .description("Test event")
                    .addConstant("KEY", 100)
                    .formula("STR * 2")
                    .fallbackFormula("STR")
                    .minDamage(5.0)
                    .maxDamage(100.0)
                    .critical("chance", "multiplier")
                    .physicalCutFormula("DEF / 2")
                    .magicCutFormula("MDEF / 2")
                    .build();

            assertThat(config.getDescription()).isEqualTo("Test event");
            assertThat(config.getFormula()).isEqualTo("STR * 2");
            assertThat(config.getFallbackFormula()).isEqualTo("STR");
            assertThat(config.getMinDamage()).isEqualTo(5.0);
            assertThat(config.getMaxDamage()).hasValue(100.0);
            assertThat(config.getCritical()).hasValueSatisfying(c -> {
                assertThat(c.getChanceFormula()).isEqualTo("chance");
                assertThat(c.getMultiplierFormula()).isEqualTo("multiplier");
            });
            assertThat(config.getPhysicalCutFormula()).hasValue("DEF / 2");
            assertThat(config.getMagicCutFormula()).hasValue("MDEF / 2");
        }

        @Test
        @DisplayName("定数をマップで追加できる")
        void addConstants_Map_AddsAllEntries() {
            Map<String, Object> constants = Map.of(
                    "KEY1", 100,
                    "KEY2", 200
            );

            EventConfig config = builder
                    .addConstants(constants)
                    .build();

            assertThat(config.getConstant("KEY1")).isEqualTo(100);
            assertThat(config.getConstant("KEY2")).isEqualTo(200);
        }

        @Test
        @DisplayName("nullの定数マップで例外がスローされない")
        void addConstants_Null_DoesNothing() {
            EventConfig config = builder
                    .addConstants(null)
                    .build();

            assertThat(config.getConstants()).isEmpty();
        }

        @Test
        @DisplayName("criticalを2つの引数で設定できる")
        void critical_TwoArgs_CreatesCriticalConfig() {
            EventConfig config = builder
                    .critical("CHANCE_FORMULA", "MULT_FORMULA")
                    .build();

            assertThat(config.getCritical()).hasValueSatisfying(c -> {
                assertThat(c.getChanceFormula()).isEqualTo("CHANCE_FORMULA");
                assertThat(c.getMultiplierFormula()).isEqualTo("MULT_FORMULA");
            });
        }

        @Test
        @DisplayName("maxDamageにnullを設定できる")
        void maxDamage_Null_SetsToNull() {
            EventConfig config = builder
                    .maxDamage(null)
                    .build();

            assertThat(config.getMaxDamage()).isEmpty();
        }
    }

    // ==================== CriticalConfig テスト ====================

    @Nested
    @DisplayName("CriticalConfig テスト")
    class CriticalConfigTests {

        @Test
        @DisplayName("CriticalConfigが構築できる")
        void criticalConfig_Construction_Works() {
            // Builderを通してCriticalConfigを取得
            EventConfig config = new EventConfig.Builder()
                    .critical("CHANCE_FORMULA", "MULT_FORMULA")
                    .build();

            assertThat(config.getCritical()).hasValueSatisfying(c -> {
                assertThat(c.getChanceFormula()).isEqualTo("CHANCE_FORMULA");
                assertThat(c.getMultiplierFormula()).isEqualTo("MULT_FORMULA");
            });
        }
    }

    // ==================== fromSection テスト ====================

    @Nested
    @DisplayName("fromSection テスト")
    class FromSectionTests {

        @BeforeEach
        void setUp() {
            when(mockSection.getString("description", "")).thenReturn("Test event");
            when(mockSection.getString("formula", "")).thenReturn("STR * 2");
            when(mockSection.getString("constants.fallback_formula", "")).thenReturn("STR");
            when(mockSection.getString("fallback_formula", "")).thenReturn("STR");
            when(mockSection.getDouble("min_damage", 1.0)).thenReturn(5.0);
            when(mockSection.contains("max_damage")).thenReturn(true);
            when(mockSection.getDouble("max_damage")).thenReturn(100.0);
            when(mockSection.getString("physical_cut_formula")).thenReturn("DEF / 2");
            when(mockSection.getString("magic_cut_formula")).thenReturn("MDEF / 2");
        }

        @Test
        @DisplayName("YAMLセクションから構築できる")
        void fromSection_Valid_SetsAllValues() {
            EventConfig config = EventConfig.fromSection(mockSection);

            assertThat(config.getDescription()).isEqualTo("Test event");
            assertThat(config.getFormula()).isEqualTo("STR * 2");
            assertThat(config.getMinDamage()).isEqualTo(5.0);
            assertThat(config.getMaxDamage()).hasValue(100.0);
            assertThat(config.getPhysicalCutFormula()).hasValue("DEF / 2");
            assertThat(config.getMagicCutFormula()).hasValue("MDEF / 2");
        }

        @Test
        @DisplayName("nullセクションで例外がスローされる")
        void fromSection_Null_ThrowsException() {
            assertThatThrownBy(() -> EventConfig.fromSection(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Section cannot be null");
        }

        @Test
        @DisplayName("maxDamageが存在しない場合は空になる")
        void fromSection_NoMaxDamage_Empty() {
            when(mockSection.contains("max_damage")).thenReturn(false);

            EventConfig config = EventConfig.fromSection(mockSection);

            assertThat(config.getMaxDamage()).isEmpty();
        }

        @Test
        @DisplayName("constantsセクションから定数を読み込める")
        void fromSection_Constants_LoadsConstants() {
            when(mockSection.getConfigurationSection("constants")).thenReturn(mockConstantsSection);
            when(mockConstantsSection.getKeys(false)).thenReturn(java.util.Set.of("KEY1", "KEY2"));
            when(mockConstantsSection.get("KEY1")).thenReturn(100);
            when(mockConstantsSection.get("KEY2")).thenReturn(200);

            EventConfig config = EventConfig.fromSection(mockSection);

            assertThat(config.getConstant("KEY1")).isEqualTo(100);
            assertThat(config.getConstant("KEY2")).isEqualTo(200);
        }

        @Test
        @DisplayName("enabled=falseのクリティカル設定は読み込まれない")
        void fromSection_CriticalDisabled_NoCriticalConfig() {
            when(mockSection.getConfigurationSection("critical")).thenReturn(mockCriticalSection);
            when(mockCriticalSection.getBoolean("enabled", false)).thenReturn(false);

            EventConfig config = EventConfig.fromSection(mockSection);

            assertThat(config.getCritical()).isEmpty();
        }

        @Test
        @DisplayName("enabled=trueのクリティカル設定は読み込まれる")
        void fromSection_CriticalEnabled_LoadsCriticalConfig() {
            when(mockSection.getConfigurationSection("critical")).thenReturn(mockCriticalSection);
            when(mockCriticalSection.getBoolean("enabled", false)).thenReturn(true);
            when(mockCriticalSection.getString("chance_formula", "CRITICAL_HIT_CHANCE"))
                    .thenReturn("CUSTOM_CHANCE");
            when(mockCriticalSection.getString("multiplier_formula", "CRITICAL_HIT_MULTIPLIER"))
                    .thenReturn("CUSTOM_MULT");

            EventConfig config = EventConfig.fromSection(mockSection);

            assertThat(config.getCritical()).hasValueSatisfying(c -> {
                assertThat(c.getChanceFormula()).isEqualTo("CUSTOM_CHANCE");
                assertThat(c.getMultiplierFormula()).isEqualTo("CUSTOM_MULT");
            });
        }

        @Test
        @DisplayName("クリティカルセクションがnullの場合は読み込まれない")
        void fromSection_NoCriticalSection_NoCriticalConfig() {
            when(mockSection.getConfigurationSection("critical")).thenReturn(null);

            EventConfig config = EventConfig.fromSection(mockSection);

            assertThat(config.getCritical()).isEmpty();
        }
    }

    // ==================== ゲッター テスト ====================

    @Nested
    @DisplayName("ゲッター テスト")
    class AccessorTests {

        private EventConfig config;

        @BeforeEach
        void setUp() {
            config = new EventConfig.Builder()
                    .description("Test")
                    .addConstant("KEY", 100)
                    .formula("STR")
                    .fallbackFormula("STR * 0.5")
                    .minDamage(1.0)
                    .maxDamage(10.0)
                    .critical("chance", "multiplier")
                    .physicalCutFormula("DEF / 2")
                    .magicCutFormula("MDEF / 2")
                    .build();
        }

        @Test
        @DisplayName("getConstantsでコピーが返される")
        void getConstants_ReturnsCopy() {
            Map<String, Object> constants1 = config.getConstants();
            Map<String, Object> constants2 = config.getConstants();

            assertThat(constants1).isNotSameAs(constants2);
            assertThat(constants1).isEqualTo(constants2);
        }

        @Test
        @DisplayName("getConstantで値を取得できる")
        void getConstant_Existing_ReturnsValue() {
            assertThat(config.getConstant("KEY")).isEqualTo(100);
        }

        @Test
        @DisplayName("getConstantで存在しないキーはnull")
        void getConstant_NonExisting_ReturnsNull() {
            assertThat(config.getConstant("NONEXISTENT")).isNull();
        }

        @Test
        @DisplayName("getMaxDamageでOptionalが返される")
        void getMaxDamage_ReturnsOptional() {
            assertThat(config.getMaxDamage()).isPresent();
        }

        @Test
        @DisplayName("getCriticalでOptionalが返される")
        void getCritical_ReturnsOptional() {
            assertThat(config.getCritical()).isPresent();
        }

        @Test
        @DisplayName("getPhysicalCutFormulaでOptionalが返される")
        void getPhysicalCutFormula_ReturnsOptional() {
            assertThat(config.getPhysicalCutFormula()).isPresent();
        }

        @Test
        @DisplayName("getMagicCutFormulaでOptionalが返される")
        void getMagicCutFormula_ReturnsOptional() {
            assertThat(config.getMagicCutFormula()).isPresent();
        }
    }

    // ==================== toString テスト ====================

    @Nested
    @DisplayName("toString テスト")
    class ToStringTests {

        @Test
        @DisplayName("toStringに主要フィールドが含まれる")
        void toString_ContainsMajorFields() {
            EventConfig config = new EventConfig.Builder()
                    .description("Test event")
                    .formula("STR * 2")
                    .minDamage(5.0)
                    .maxDamage(100.0)
                    .critical("chance", "multiplier")
                    .build();

            String str = config.toString();
            assertThat(str).contains("description='Test event'");
            assertThat(str).contains("formula='STR * 2'");
            assertThat(str).contains("minDamage=5.0");
            assertThat(str).contains("maxDamage=100.0");
            assertThat(str).contains("critical=true");
        }

        @Test
        @DisplayName("critical=nullの場合もtoStringが動作する")
        void toString_NoCritical_Works() {
            EventConfig config = new EventConfig.Builder()
                    .description("Test")
                    .build();

            String str = config.toString();
            assertThat(str).contains("critical=false");
        }
    }
}
