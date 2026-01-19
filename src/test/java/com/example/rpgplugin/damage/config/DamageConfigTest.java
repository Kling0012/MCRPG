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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DamageConfigのユニットテスト
 *
 * <p>ダメージYAML設定モデルのテストを行います。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DamageConfig テスト")
class DamageConfigTest {

    // ==================== Builder テスト ====================

    @Nested
    @DisplayName("Builder テスト")
    class BuilderTests {

        @Mock
        private ConfigurationSection mockSection;

        private DamageConfig.Builder builder;

        @BeforeEach
        void setUp() {
            builder = new DamageConfig.Builder();
        }

        @Test
        @DisplayName("空の設定を構築できる")
        void build_Empty_HasEmptyMaps() {
            DamageConfig config = builder.build();

            assertThat(config.getGlobalConstants()).isEmpty();
            assertThat(config.getAllEvents()).isEmpty();
        }

        @Test
        @DisplayName("グローバル定数を追加できる")
        void addGlobalConstant_Single_AddsToMap() {
            DamageConfig config = builder
                    .addGlobalConstant("KEY", 100)
                    .build();

            assertThat(config.getGlobalConstant("KEY")).isEqualTo(100);
        }

        @Test
        @DisplayName("グローバル定数をマップで追加できる")
        void addGlobalConstants_Map_AddsAllEntries() {
            Map<String, Object> constants = Map.of(
                    "KEY1", 100,
                    "KEY2", 200
            );

            DamageConfig config = builder
                    .addGlobalConstants(constants)
                    .build();

            assertThat(config.getGlobalConstant("KEY1")).isEqualTo(100);
            assertThat(config.getGlobalConstant("KEY2")).isEqualTo(200);
        }

        @Test
        @DisplayName("nullの定数マップで例外がスローされない")
        void addGlobalConstants_Null_DoesNothing() {
            DamageConfig config = builder
                    .addGlobalConstants(null)
                    .build();

            assertThat(config.getGlobalConstants()).isEmpty();
        }

        @Test
        @DisplayName("イベント設定を追加できる")
        void addEvent_Single_AddsToMap() {
            EventConfig eventConfig = new EventConfig.Builder()
                    .description("Test event")
                    .build();

            DamageConfig config = builder
                    .addEvent("skill_damage", eventConfig)
                    .build();

            assertThat(config.getEventConfig("skill_damage")).isSameAs(eventConfig);
        }

        @Test
        @DisplayName("クラス上書きを追加できる")
        void addClassOverride_Single_AddsToMap() {
            DamageConfig.ClassOverrideConfig classConfig =
                    new DamageConfig.ClassOverrideConfig.Builder()
                            .description("Warrior overrides")
                            .build();

            DamageConfig config = builder
                    .addClassOverride("Warrior", classConfig)
                    .build();

            assertThat(config.getClassOverride("Warrior")).isSameAs(classConfig);
        }

        @Test
        @DisplayName("ダメージタイプを追加できる")
        void addDamageType_Single_AddsToMap() {
            // fromSectionで作成
            when(mockSection.getString("name", "")).thenReturn("PHYSICAL");
            when(mockSection.getString("description", "")).thenReturn("Physical damage");
            when(mockSection.getString("stat_primary", "STR")).thenReturn("STR");
            when(mockSection.getString("stat_secondary", "DEX")).thenReturn("DEX");
            DamageConfig.DamageTypeConfig typeConfig = DamageConfig.DamageTypeConfig.fromSection(mockSection);

            DamageConfig config = builder
                    .addDamageType("PHYSICAL", typeConfig)
                    .build();

            assertThat(config.getDamageType("PHYSICAL")).isSameAs(typeConfig);
        }

        @Test
        @DisplayName("武器タイプを追加できる")
        void addWeaponType_Single_AddsToMap() {
            // fromSectionで作成
            when(mockSection.getString("name", "")).thenReturn("SWORD");
            when(mockSection.getDouble("base_multiplier", 1.0)).thenReturn(1.5);
            DamageConfig.WeaponTypeConfig weaponConfig = DamageConfig.WeaponTypeConfig.fromSection(mockSection);

            DamageConfig config = builder
                    .addWeaponType("SWORD", weaponConfig)
                    .build();

            assertThat(config.getWeaponType("SWORD")).isSameAs(weaponConfig);
        }
    }

    // ==================== ClassOverrideConfig.Builder テスト ====================

    @Nested
    @DisplayName("ClassOverrideConfig.Builder テスト")
    class ClassOverrideConfigBuilderTests {

        private DamageConfig.ClassOverrideConfig.Builder builder;

        @BeforeEach
        void setUp() {
            builder = new DamageConfig.ClassOverrideConfig.Builder();
        }

        @Test
        @DisplayName("デフォルト値で構築できる")
        void build_Defaults_HasEmptyMaps() {
            DamageConfig.ClassOverrideConfig config = builder.build();

            assertThat(config.getDescription()).isNull();
            assertThat(config.getConstants()).isEmpty();
            assertThat(config.getAllEventOverrides()).isEmpty();
        }

        @Test
        @DisplayName("全フィールドを設定して構築できる")
        void build_AllFields_SetsAllValues() {
            EventConfig eventConfig = new EventConfig.Builder()
                    .description("Override event")
                    .build();

            DamageConfig.ClassOverrideConfig config = builder
                    .description("Warrior class")
                    .addConstant("STR_BONUS", 10)
                    .addEventOverride("skill_damage", eventConfig)
                    .build();

            assertThat(config.getDescription()).isEqualTo("Warrior class");
            assertThat(config.getConstant("STR_BONUS")).isEqualTo(10);
            assertThat(config.getEventConfig("skill_damage")).isSameAs(eventConfig);
        }

        @Test
        @DisplayName("定数をマップで追加できる")
        void addConstants_Map_AddsAllEntries() {
            Map<String, Object> constants = Map.of(
                    "KEY1", 100,
                    "KEY2", 200
            );

            DamageConfig.ClassOverrideConfig config = builder
                    .addConstants(constants)
                    .build();

            assertThat(config.getConstant("KEY1")).isEqualTo(100);
            assertThat(config.getConstant("KEY2")).isEqualTo(200);
        }

        @Test
        @DisplayName("nullの定数マップで例外がスローされない")
        void addConstants_Null_DoesNothing() {
            DamageConfig.ClassOverrideConfig config = builder
                    .addConstants(null)
                    .build();

            assertThat(config.getConstants()).isEmpty();
        }
    }

    // ==================== DamageTypeConfig テスト ====================

    @Nested
    @DisplayName("DamageTypeConfig テスト")
    class DamageTypeConfigTests {

        @Mock
        private ConfigurationSection mockSection;

        @Test
        @DisplayName("fromSectionでYAMLセクションから構築できる")
        void fromSection_Valid_SetsAllValues() {
            when(mockSection.getString("name", "")).thenReturn("PHYSICAL");
            when(mockSection.getString("description", "")).thenReturn("Physical damage");
            when(mockSection.getString("stat_primary", "STR")).thenReturn("STR");
            when(mockSection.getString("stat_secondary", "DEX")).thenReturn("DEX");

            DamageConfig.DamageTypeConfig config = DamageConfig.DamageTypeConfig.fromSection(mockSection);

            assertThat(config.getName()).isEqualTo("PHYSICAL");
            assertThat(config.getDescription()).isEqualTo("Physical damage");
            assertThat(config.getStatPrimary()).isEqualTo("STR");
            assertThat(config.getStatSecondary()).isEqualTo("DEX");
        }

        @Test
        @DisplayName("fromSectionでnullセクションは例外")
        void fromSection_Null_ThrowsException() {
            assertThatThrownBy(() -> DamageConfig.DamageTypeConfig.fromSection(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Section cannot be null");
        }

        @Test
        @DisplayName("fromSectionでデフォルト値が使用される")
        void fromSection_MissingValues_UsesDefaults() {
            when(mockSection.getString("name", "")).thenReturn("");
            when(mockSection.getString("description", "")).thenReturn("");
            when(mockSection.getString("stat_primary", "STR")).thenReturn("STR");
            when(mockSection.getString("stat_secondary", "DEX")).thenReturn("DEX");

            DamageConfig.DamageTypeConfig config = DamageConfig.DamageTypeConfig.fromSection(mockSection);

            assertThat(config.getName()).isEmpty();
            assertThat(config.getDescription()).isEmpty();
            assertThat(config.getStatPrimary()).isEqualTo("STR");
            assertThat(config.getStatSecondary()).isEqualTo("DEX");
        }
    }

    // ==================== WeaponTypeConfig テスト ====================

    @Nested
    @DisplayName("WeaponTypeConfig テスト")
    class WeaponTypeConfigTests {

        @Mock
        private ConfigurationSection mockSection;

        @Test
        @DisplayName("fromSectionでYAMLセクションから構築できる")
        void fromSection_Valid_SetsAllValues() {
            when(mockSection.getString("name", "")).thenReturn("SWORD");
            when(mockSection.getDouble("base_multiplier", 1.0)).thenReturn(1.5);

            DamageConfig.WeaponTypeConfig config = DamageConfig.WeaponTypeConfig.fromSection(mockSection);

            assertThat(config.getName()).isEqualTo("SWORD");
            assertThat(config.getBaseMultiplier()).isEqualTo(1.5);
        }

        @Test
        @DisplayName("fromSectionでnullセクションは例外")
        void fromSection_Null_ThrowsException() {
            assertThatThrownBy(() -> DamageConfig.WeaponTypeConfig.fromSection(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Section cannot be null");
        }

        @Test
        @DisplayName("fromSectionでデフォルト値が使用される")
        void fromSection_MissingValues_UsesDefaults() {
            when(mockSection.getString("name", "")).thenReturn("");
            when(mockSection.getDouble("base_multiplier", 1.0)).thenReturn(1.0);

            DamageConfig.WeaponTypeConfig config = DamageConfig.WeaponTypeConfig.fromSection(mockSection);

            assertThat(config.getName()).isEmpty();
            assertThat(config.getBaseMultiplier()).isEqualTo(1.0);
        }
    }

    // ==================== ClassOverrideConfig テスト ====================

    @Nested
    @DisplayName("ClassOverrideConfig テスト")
    class ClassOverrideConfigTests {

        @Test
        @DisplayName("getConstantsでコピーが返される")
        void getConstants_ReturnsCopy() {
            DamageConfig.ClassOverrideConfig config = new DamageConfig.ClassOverrideConfig.Builder()
                    .addConstant("KEY", 100)
                    .build();

            Map<String, Object> constants1 = config.getConstants();
            Map<String, Object> constants2 = config.getConstants();

            assertThat(constants1).isNotSameAs(constants2);
            assertThat(constants1).isEqualTo(constants2);
        }

        @Test
        @DisplayName("getConstantで値を取得できる")
        void getConstant_Existing_ReturnsValue() {
            DamageConfig.ClassOverrideConfig config = new DamageConfig.ClassOverrideConfig.Builder()
                    .addConstant("KEY", 100)
                    .build();

            assertThat(config.getConstant("KEY")).isEqualTo(100);
        }

        @Test
        @DisplayName("getConstantで存在しないキーはnull")
        void getConstant_NonExisting_ReturnsNull() {
            DamageConfig.ClassOverrideConfig config = new DamageConfig.ClassOverrideConfig.Builder()
                    .build();

            assertThat(config.getConstant("NONEXISTENT")).isNull();
        }

        @Test
        @DisplayName("getAllEventOverridesでコピーが返される")
        void getAllEventOverrides_ReturnsCopy() {
            EventConfig eventConfig = new EventConfig.Builder().build();
            DamageConfig.ClassOverrideConfig config = new DamageConfig.ClassOverrideConfig.Builder()
                    .addEventOverride("skill_damage", eventConfig)
                    .build();

            Map<String, EventConfig> overrides1 = config.getAllEventOverrides();
            Map<String, EventConfig> overrides2 = config.getAllEventOverrides();

            assertThat(overrides1).isNotSameAs(overrides2);
            assertThat(overrides1).isEqualTo(overrides2);
        }

        @Test
        @DisplayName("getEventConfigで値を取得できる")
        void getEventConfig_Existing_ReturnsValue() {
            EventConfig eventConfig = new EventConfig.Builder()
                    .description("Override")
                    .build();

            DamageConfig.ClassOverrideConfig config = new DamageConfig.ClassOverrideConfig.Builder()
                    .addEventOverride("skill_damage", eventConfig)
                    .build();

            assertThat(config.getEventConfig("skill_damage")).isSameAs(eventConfig);
        }
    }

    // ==================== DamageConfig メソッド テスト ====================

    @Nested
    @DisplayName("DamageConfig メソッド テスト")
    class DamageConfigMethodsTests {

        private DamageConfig config;
        private EventConfig defaultEvent;
        private EventConfig warriorEvent;
        private DamageConfig.ClassOverrideConfig warriorOverride;

        @BeforeEach
        void setUp() {
            defaultEvent = new EventConfig.Builder()
                    .description("Default skill damage")
                    .formula("STR * 2")
                    .build();

            warriorEvent = new EventConfig.Builder()
                    .description("Warrior skill damage")
                    .formula("STR * 3")
                    .build();

            warriorOverride = new DamageConfig.ClassOverrideConfig.Builder()
                    .description("Warrior overrides")
                    .addConstant("STR_BONUS", 10)
                    .addEventOverride("skill_damage", warriorEvent)
                    .build();

            config = new DamageConfig.Builder()
                    .addGlobalConstant("BASE_DAMAGE", 10)
                    .addEvent("skill_damage", defaultEvent)
                    .addClassOverride("Warrior", warriorOverride)
                    .build();
        }

        @Test
        @DisplayName("getGlobalConstantsでコピーが返される")
        void getGlobalConstants_ReturnsCopy() {
            Map<String, Object> constants1 = config.getGlobalConstants();
            Map<String, Object> constants2 = config.getGlobalConstants();

            assertThat(constants1).isNotSameAs(constants2);
            assertThat(constants1).isEqualTo(constants2);
        }

        @Test
        @DisplayName("getAllEventsでコピーが返される")
        void getAllEvents_ReturnsCopy() {
            Map<String, EventConfig> events1 = config.getAllEvents();
            Map<String, EventConfig> events2 = config.getAllEvents();

            assertThat(events1).isNotSameAs(events2);
            assertThat(events1).isEqualTo(events2);
        }

        @Test
        @DisplayName("getEventConfigForClassでクラス上書きが優先される")
        void getEventConfigForClass_OverrideExists_ReturnsOverride() {
            EventConfig result = config.getEventConfigForClass("Warrior", "skill_damage");

            assertThat(result).isSameAs(warriorEvent);
        }

        @Test
        @DisplayName("getEventConfigForClassで上書きがない場合はデフォルト")
        void getEventConfigForClass_NoOverride_ReturnsDefault() {
            EventConfig result = config.getEventConfigForClass("Mage", "skill_damage");

            assertThat(result).isSameAs(defaultEvent);
        }

        @Test
        @DisplayName("getEventConfigForClassでイベントがない場合はnull")
        void getEventConfigForClass_NoEvent_ReturnsNull() {
            EventConfig result = config.getEventConfigForClass("Warrior", "nonexistent");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("存在しないクラスのオーバーライドはnull")
        void getClassOverride_NonExisting_ReturnsNull() {
            assertThat(config.getClassOverride("Mage")).isNull();
        }
    }
}
