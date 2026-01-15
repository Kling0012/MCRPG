package com.example.rpgplugin.skill.component.trigger;

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

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * TriggerSettingsのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TriggerSettingsTest {

    private TriggerSettings settings;

    @Mock
    private ConfigurationSection mockConfig;

    @BeforeEach
    void setUp() {
        settings = new TriggerSettings();
    }

    // ========== load() テスト ==========

    @Nested
    @DisplayName("load: 設定のロード")
    class LoadTests {

        @Test
        @DisplayName("test: null configでは何もしない")
        void testLoad_NullConfig_DoesNothing() {
            settings.load(null);

            assertThat(settings.getKeys()).isEmpty();
        }

        @Test
        @DisplayName("test: 空のconfigをロード")
        void testLoad_EmptyConfig() {
            when(mockConfig.getKeys(false)).thenReturn(Set.of());

            settings.load(mockConfig);

            assertThat(settings.getKeys()).isEmpty();
        }

        @Test
        @DisplayName("test: 単一の値をロード")
        void testLoad_SingleValue() {
            when(mockConfig.getKeys(false)).thenReturn(Set.of("key1"));
            when(mockConfig.get("key1")).thenReturn("value1");

            settings.load(mockConfig);

            assertThat(settings.getString("key1", null)).isEqualTo("value1");
        }

        @Test
        @DisplayName("test: 複数の値をロード")
        void testLoad_MultipleValues() {
            when(mockConfig.getKeys(false)).thenReturn(Set.of("key1", "key2", "key3"));
            when(mockConfig.get("key1")).thenReturn("value1");
            when(mockConfig.get("key2")).thenReturn(42);
            when(mockConfig.get("key3")).thenReturn(true);

            settings.load(mockConfig);

            assertThat(settings.getString("key1", null)).isEqualTo("value1");
            assertThat(settings.getInt("key2", 0)).isEqualTo(42);
            assertThat(settings.getBoolean("key3", false)).isTrue();
        }

        @Test
        @DisplayName("test: null値はスキップされる")
        void testLoad_NullValues_Skipped() {
            when(mockConfig.getKeys(false)).thenReturn(Set.of("key1", "key2"));
            when(mockConfig.get("key1")).thenReturn("value1");
            when(mockConfig.get("key2")).thenReturn(null);

            settings.load(mockConfig);

            assertThat(settings.has("key1")).isTrue();
            assertThat(settings.has("key2")).isFalse();
        }
    }

    // ========== has/getKeys() テスト ==========

    @Nested
    @DisplayName("has/getKeys: キー確認")
    class KeyTests {

        @Test
        @DisplayName("test: 存在するキーを確認")
        void testHas_ExistingKey() {
            settings.set("testKey", "testValue");

            assertThat(settings.has("testKey")).isTrue();
        }

        @Test
        @DisplayName("test: 存在しないキーを確認")
        void testHas_NonExistingKey() {
            assertThat(settings.has("nonExisting")).isFalse();
        }

        @Test
        @DisplayName("test: すべてのキーを取得")
        void testGetKeys() {
            settings.set("key1", "value1");
            settings.set("key2", "value2");
            settings.set("key3", "value3");

            Set<String> keys = settings.getKeys();

            assertThat(keys).containsExactlyInAnyOrder("key1", "key2", "key3");
        }
    }

    // ========== getString() テスト ==========

    @Nested
    @DisplayName("getString: 文字列値取得")
    class GetStringTests {

        @Test
        @DisplayName("test: 存在するキーで文字列を取得")
        void testGetString_ExistingKey() {
            settings.set("key", "stringValue");

            assertThat(settings.getString("key", "default")).isEqualTo("stringValue");
        }

        @Test
        @DisplayName("test: 存在しないキーでデフォルト値を取得")
        void testGetString_NonExistingKey() {
            assertThat(settings.getString("nonExisting", "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("test: 数値も文字列として取得")
        void testGetString_NumberValue() {
            settings.set("key", 123);

            assertThat(settings.getString("key", "default")).isEqualTo("123");
        }

        @Test
        @DisplayName("test: 真偽値も文字列として取得")
        void testGetString_BooleanValue() {
            settings.set("key", true);

            assertThat(settings.getString("key", "default")).isEqualTo("true");
        }
    }

    // ========== getInt() テスト ==========

    @Nested
    @DisplayName("getInt: 整数値取得")
    class GetIntTests {

        @Test
        @DisplayName("test: 存在する整数値を取得")
        void testGetInt_ExistingInt() {
            settings.set("key", 42);

            assertThat(settings.getInt("key", 0)).isEqualTo(42);
        }

        @Test
        @DisplayName("test: Number型として整数値を取得")
        void testGetInt_NumberValue() {
            settings.set("key", 42.5);

            assertThat(settings.getInt("key", 0)).isEqualTo(42);
        }

        @Test
        @DisplayName("test: Long型でも整数値として取得")
        void testGetInt_LongValue() {
            settings.set("key", 42L);

            assertThat(settings.getInt("key", 0)).isEqualTo(42);
        }

        @Test
        @DisplayName("test: 文字列を整数にパース")
        void testGetInt_StringValue() {
            settings.set("key", "42");

            assertThat(settings.getInt("key", 0)).isEqualTo(42);
        }

        @Test
        @DisplayName("test: 無効な文字列ではデフォルト値")
        void testGetInt_InvalidString() {
            settings.set("key", "invalid");

            assertThat(settings.getInt("key", 99)).isEqualTo(99);
        }

        @Test
        @DisplayName("test: nullではデフォルト値")
        void testGetInt_NullValue() {
            assertThat(settings.getInt("nonExisting", 99)).isEqualTo(99);
        }

        @Test
        @DisplayName("test: 小数文字列ではデフォルト値（NumberFormatException）")
        void testGetInt_DecimalString() {
            settings.set("key", "42.5");

            assertThat(settings.getInt("key", 99)).isEqualTo(99);
        }
    }

    // ========== getDouble() テスト ==========

    @Nested
    @DisplayName("getDouble: 小数値取得")
    class GetDoubleTests {

        @Test
        @DisplayName("test: 存在する小数値を取得")
        void testGetDouble_ExistingDouble() {
            settings.set("key", 42.5);

            assertThat(settings.getDouble("key", 0.0)).isEqualTo(42.5);
        }

        @Test
        @DisplayName("test: Number型として小数値を取得")
        void testGetDouble_NumberValue() {
            settings.set("key", 42);

            assertThat(settings.getDouble("key", 0.0)).isEqualTo(42.0);
        }

        @Test
        @DisplayName("test: Integer型でも小数値として取得")
        void testGetDouble_IntegerValue() {
            settings.set("key", 42);

            assertThat(settings.getDouble("key", 0.0)).isEqualTo(42.0);
        }

        @Test
        @DisplayName("test: 文字列を小数にパース")
        void testGetDouble_StringValue() {
            settings.set("key", "42.5");

            assertThat(settings.getDouble("key", 0.0)).isEqualTo(42.5);
        }

        @Test
        @DisplayName("test: 無効な文字列ではデフォルト値")
        void testGetDouble_InvalidString() {
            settings.set("key", "invalid");

            assertThat(settings.getDouble("key", 99.9)).isEqualTo(99.9);
        }

        @Test
        @DisplayName("test: nullではデフォルト値")
        void testGetDouble_NullValue() {
            assertThat(settings.getDouble("nonExisting", 99.9)).isEqualTo(99.9);
        }
    }

    // ========== getBoolean() テスト ==========

    @Nested
    @DisplayName("getBoolean: 真偽値取得")
    class GetBooleanTests {

        @Test
        @DisplayName("test: trueを取得")
        void testGetBoolean_True() {
            settings.set("key", true);

            assertThat(settings.getBoolean("key", false)).isTrue();
        }

        @Test
        @DisplayName("test: falseを取得")
        void testGetBoolean_False() {
            settings.set("key", false);

            assertThat(settings.getBoolean("key", true)).isFalse();
        }

        @Test
        @DisplayName("test: 文字列'true'をtrueとして取得")
        void testGetBoolean_StringTrue() {
            settings.set("key", "true");

            assertThat(settings.getBoolean("key", false)).isTrue();
        }

        @Test
        @DisplayName("test: 文字列'TRUE'をtrueとして取得（大文字小文字区別なし）")
        void testGetBoolean_StringTrueUpperCase() {
            settings.set("key", "TRUE");

            assertThat(settings.getBoolean("key", false)).isTrue();
        }

        @Test
        @DisplayName("test: 文字列'yes'をtrueとして取得")
        void testGetBoolean_StringYes() {
            settings.set("key", "yes");

            assertThat(settings.getBoolean("key", false)).isTrue();
        }

        @Test
        @DisplayName("test: 文字列'1'をtrueとして取得")
        void testGetBoolean_StringOne() {
            settings.set("key", "1");

            assertThat(settings.getBoolean("key", false)).isTrue();
        }

        @Test
        @DisplayName("test: 他の文字列はfalse")
        void testGetBoolean_OtherString() {
            settings.set("key", "no");

            assertThat(settings.getBoolean("key", true)).isFalse();
        }

        @Test
        @DisplayName("test: nullではデフォルト値")
        void testGetBoolean_NullValue() {
            assertThat(settings.getBoolean("nonExisting", true)).isTrue();
        }
    }

    // ========== set/put/putAll/clear() テスト ==========

    @Nested
    @DisplayName("set/put/putAll/clear: 値の設定")
    class MutationTests {

        @Test
        @DisplayName("test: setで値を設定")
        void testSet() {
            settings.set("key", "value");

            assertThat(settings.getString("key", null)).isEqualTo("value");
        }

        @Test
        @DisplayName("test: putで値を設定")
        void testPut() {
            settings.put("key", "value");

            assertThat(settings.getString("key", null)).isEqualTo("value");
        }

        @Test
        @DisplayName("test: putAllで複数の値をマージ")
        void testPutAll() {
            settings.set("key1", "value1");

            TriggerSettings other = new TriggerSettings();
            other.set("key2", "value2");
            other.set("key3", "value3");

            settings.putAll(other);

            assertThat(settings.has("key1")).isTrue();
            assertThat(settings.has("key2")).isTrue();
            assertThat(settings.has("key3")).isTrue();
        }

        @Test
        @DisplayName("test: putAllでnullは無視")
        void testPutAll_Null() {
            settings.set("key1", "value1");

            settings.putAll(null);

            assertThat(settings.has("key1")).isTrue();
        }

        @Test
        @DisplayName("test: clearで全設定をクリア")
        void testClear() {
            settings.set("key1", "value1");
            settings.set("key2", "value2");

            settings.clear();

            assertThat(settings.getKeys()).isEmpty();
        }

        @Test
        @DisplayName("test: 同じキーで上書き")
        void testOverwrite() {
            settings.set("key", "value1");
            settings.set("key", "value2");

            assertThat(settings.getString("key", null)).isEqualTo("value2");
        }
    }

    // ========== 型混合テスト ==========

    @Nested
    @DisplayName("Mixed Types: 混合型テスト")
    class MixedTypesTests {

        @Test
        @DisplayName("test: 異なる型の値を混在して保存")
        void testMixedTypes() {
            settings.set("stringKey", "stringValue");
            settings.set("intKey", 42);
            settings.set("doubleKey", 3.14);
            settings.set("boolKey", true);

            assertThat(settings.getString("stringKey", null)).isEqualTo("stringValue");
            assertThat(settings.getInt("intKey", 0)).isEqualTo(42);
            assertThat(settings.getDouble("doubleKey", 0.0)).isEqualTo(3.14);
            assertThat(settings.getBoolean("boolKey", false)).isTrue();
        }
    }
}
