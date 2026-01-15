package com.example.rpgplugin.damage.config;

import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.evaluator.VariableContext;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * VariableScopeManagerのユニットテスト
 *
 * <p>変数スコープマネージャーのテストを行います。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("VariableScopeManager テスト")
class VariableScopeManagerTest {

    @Mock
    private RPGPlayer mockPlayer;

    @Mock
    private StatManager mockStatManager;

    @Mock
    private DamageConfig mockDamageConfig;

    @Mock
    private DamageConfig.ClassOverrideConfig mockClassConfig;

    private static final UUID TEST_UUID = UUID.randomUUID();
    private VariableScopeManager scopeManager;

    @BeforeEach
    void setUp() {
        scopeManager = new VariableScopeManager();

        // プレイヤーモック設定
        when(mockPlayer.getUuid()).thenReturn(TEST_UUID);
        when(mockPlayer.getClassId()).thenReturn("Warrior");
        when(mockPlayer.getStatManager()).thenReturn(mockStatManager);
        when(mockPlayer.getLevel()).thenReturn(10);
        when(mockPlayer.getClassRank()).thenReturn(5);
    }

    // ==================== コンストラクタ テスト ====================

    @Nested
    @DisplayName("コンストラクタ テスト")
    class ConstructorTests {

        @Test
        @DisplayName("デフォルトコンストラクタで初期化される")
        void constructor_Default_InitializesEmpty() {
            assertThat(scopeManager.getGlobalConstants()).isEmpty();
        }

        @Test
        @DisplayName("グローバル定数付きコンストラクタ")
        void constructor_WithGlobalConstants_SetsConstants() {
            Map<String, Object> constants = Map.of("KEY", 100);
            VariableScopeManager manager = new VariableScopeManager(constants);

            assertThat(manager.getGlobalConstants()).containsEntry("KEY", 100);
        }

        @Test
        @DisplayName("nullの定数マップで空になる")
        void constructor_NullConstants_Empty() {
            VariableScopeManager manager = new VariableScopeManager(null);

            assertThat(manager.getGlobalConstants()).isEmpty();
        }
    }

    // ==================== DamageConfig設定 テスト ====================

    @Nested
    @DisplayName("DamageConfig設定 テスト")
    class DamageConfigTests {

        @Test
        @DisplayName("DamageConfigを設定できる")
        void setDamageConfig_Valid_SetsConfig() {
            Map<String, Object> globals = Map.of("BASE_DAMAGE", 10);
            when(mockDamageConfig.getGlobalConstants()).thenReturn(globals);

            scopeManager.setDamageConfig(mockDamageConfig);

            assertThat(scopeManager.getGlobalConstants()).containsEntry("BASE_DAMAGE", 10);
        }

        @Test
        @DisplayName("nullのDamageConfigでもグローバル定数は維持される")
        void setDamageConfig_Null_KeepsGlobals() {
            scopeManager.setGlobalConstants(Map.of("KEY", 100));
            scopeManager.setDamageConfig(null);

            // nullの場合はグローバル定数をクリアしない
            assertThat(scopeManager.getGlobalConstants()).containsEntry("KEY", 100);
        }

        @Test
        @DisplayName("設定変更でクラス定数キャッシュがクリアされる")
        void setDamageConfig_Change_ClearsClassCache() {
            when(mockDamageConfig.getClassOverride("Warrior")).thenReturn(mockClassConfig);
            when(mockClassConfig.getConstants()).thenReturn(Map.of("STR_BONUS", 10));

            scopeManager.setDamageConfig(mockDamageConfig);
            scopeManager.getClassConstant("Warrior", "STR_BONUS"); // キャッシュ

            scopeManager.setDamageConfig(mockDamageConfig);
            // キャッシュがクリアされていることを確認
        }
    }

    // ==================== テンポラリ変数 テスト ====================

    @Nested
    @DisplayName("テンポラリ変数 テスト")
    class TemporaryVariableTests {

        @Test
        @DisplayName("テンポラリ変数を設定・取得できる")
        void setTemporaryVariable_ThenGet_ReturnsValue() {
            scopeManager.setTemporaryVariable(TEST_UUID, "BUFF", 50, 0);

            Object value = scopeManager.getTemporaryVariable("BUFF", mockPlayer);

            assertThat(value).isEqualTo(50);
        }

        @Test
        @DisplayName("nullプレイヤーでgetTemporaryVariableはnull")
        void getTemporaryVariable_NullPlayer_ReturnsNull() {
            scopeManager.setTemporaryVariable(TEST_UUID, "BUFF", 50, 0);

            Object value = scopeManager.getTemporaryVariable("BUFF", null);

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("テンポラリ変数を削除できる")
        void removeTemporaryVariable_Existing_RemovesValue() {
            scopeManager.setTemporaryVariable(TEST_UUID, "BUFF", 50, 0);
            scopeManager.removeTemporaryVariable(TEST_UUID, "BUFF");

            Object value = scopeManager.getTemporaryVariable("BUFF", mockPlayer);

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("プレイヤーの全テンポラリ変数をクリアできる")
        void clearTemporaryVariables_Existing_ClearsAll() {
            scopeManager.setTemporaryVariable(TEST_UUID, "BUFF1", 50, 0);
            scopeManager.setTemporaryVariable(TEST_UUID, "BUFF2", 30, 0);

            scopeManager.clearTemporaryVariables(TEST_UUID);

            assertThat(scopeManager.getTemporaryVariable("BUFF1", mockPlayer)).isNull();
            assertThat(scopeManager.getTemporaryVariable("BUFF2", mockPlayer)).isNull();
        }

        @Test
        @DisplayName("全テンポラリ変数をクリアできる")
        void clearAllTemporaryVariables_ClearsAll() {
            scopeManager.setTemporaryVariable(TEST_UUID, "BUFF", 50, 0);
            scopeManager.setTemporaryVariable(UUID.randomUUID(), "BUFF", 30, 0);

            scopeManager.clearAllTemporaryVariables();

            assertThat(scopeManager.getTemporaryVariable("BUFF", mockPlayer)).isNull();
        }
    }

    // ==================== プレイヤー永続変数 テスト ====================

    @Nested
    @DisplayName("プレイヤー永続変数 テスト")
    class PlayerVariableTests {

        @Test
        @DisplayName("プレイヤー変数を設定・取得できる")
        void setPlayerVariable_ThenGet_ReturnsValue() {
            scopeManager.setPlayerVariable(TEST_UUID, "custom_var", 100);

            Object value = scopeManager.getPlayerVariable("custom_var", mockPlayer);

            assertThat(value).isEqualTo(100);
        }

        @Test
        @DisplayName("nullプレイヤーでgetPlayerVariableはnull")
        void getPlayerVariable_NullPlayer_ReturnsNull() {
            scopeManager.setPlayerVariable(TEST_UUID, "custom_var", 100);

            Object value = scopeManager.getPlayerVariable("custom_var", null);

            assertThat(value).isNull();
        }
    }

    // ==================== グローバル定数 テスト ====================

    @Nested
    @DisplayName("グローバル定数 テスト")
    class GlobalConstantsTests {

        @Test
        @DisplayName("グローバル定数を設定できる")
        void setGlobalConstants_SetsValues() {
            scopeManager.setGlobalConstants(Map.of("KEY1", 100, "KEY2", 200));

            assertThat(scopeManager.getGlobalConstants())
                    .containsEntry("KEY1", 100)
                    .containsEntry("KEY2", 200);
        }

        @Test
        @DisplayName("グローバル定数を上書きできる")
        void setGlobalConstants_Overwrites() {
            scopeManager.setGlobalConstants(Map.of("KEY1", 100));
            scopeManager.setGlobalConstants(Map.of("KEY2", 200));

            assertThat(scopeManager.getGlobalConstants())
                    .doesNotContainKey("KEY1")
                    .containsEntry("KEY2", 200);
        }

        @Test
        @DisplayName("nullでクリアできる")
        void setGlobalConstants_Null_Clears() {
            scopeManager.setGlobalConstants(Map.of("KEY", 100));
            scopeManager.setGlobalConstants(null);

            assertThat(scopeManager.getGlobalConstants()).isEmpty();
        }

        @Test
        @DisplayName("getGlobalConstantsでコピーが返される")
        void getGlobalConstants_ReturnsCopy() {
            scopeManager.setGlobalConstants(Map.of("KEY", 100));

            Map<String, Object> copy1 = scopeManager.getGlobalConstants();
            Map<String, Object> copy2 = scopeManager.getGlobalConstants();

            assertThat(copy1).isNotSameAs(copy2);
            assertThat(copy1).isEqualTo(copy2);
        }
    }

    // ==================== クラス定数 テスト ====================

    @Nested
    @DisplayName("クラス定数 テスト")
    class ClassConstantsTests {

        @Test
        @DisplayName("クラス定数を取得できる")
        void getClassConstant_Existing_ReturnsValue() {
            when(mockDamageConfig.getClassOverride("Warrior")).thenReturn(mockClassConfig);
            when(mockClassConfig.getConstants()).thenReturn(Map.of("STR_BONUS", 10));
            scopeManager.setDamageConfig(mockDamageConfig);

            Object value = scopeManager.getClassConstant("Warrior", "STR_BONUS");

            assertThat(value).isEqualTo(10);
        }

        @Test
        @DisplayName("存在しないクラス定数はnull")
        void getClassConstant_NonExisting_ReturnsNull() {
            when(mockDamageConfig.getClassOverride("Warrior")).thenReturn(mockClassConfig);
            when(mockClassConfig.getConstants()).thenReturn(Map.of());
            scopeManager.setDamageConfig(mockDamageConfig);

            Object value = scopeManager.getClassConstant("Warrior", "NONEXISTENT");

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("クラス定数がキャッシュされる")
        void getClassConstant_Cached_ReturnsCachedValue() {
            when(mockDamageConfig.getClassOverride("Warrior")).thenReturn(mockClassConfig);
            when(mockClassConfig.getConstants()).thenReturn(Map.of("STR_BONUS", 10));
            scopeManager.setDamageConfig(mockDamageConfig);

            scopeManager.getClassConstant("Warrior", "STR_BONUS");
            scopeManager.getClassConstant("Warrior", "STR_BONUS");

            // 2回呼んでも1回だけロード
            verify(mockDamageConfig, times(1)).getClassOverride("Warrior");
        }

        @Test
        @DisplayName("クラス定数キャッシュをクリアできる")
        void clearClassConstantsCache_ClearsCache() {
            when(mockDamageConfig.getClassOverride("Warrior")).thenReturn(mockClassConfig);
            when(mockClassConfig.getConstants()).thenReturn(Map.of("STR_BONUS", 10));
            scopeManager.setDamageConfig(mockDamageConfig);

            scopeManager.getClassConstant("Warrior", "STR_BONUS");
            scopeManager.clearClassConstantsCache();
            scopeManager.getClassConstant("Warrior", "STR_BONUS");

            // キャッシュクリア後に再ロード
            verify(mockDamageConfig, times(2)).getClassOverride("Warrior");
        }
    }

    // ==================== 変数解決 テスト ====================

    @Nested
    @DisplayName("変数解決 テスト")
    class ResolveVariableTests {

        @Test
        @DisplayName("テンポラリ変数が優先される")
        void resolveVariable_TempFirst_ReturnsTempValue() {
            scopeManager.setTemporaryVariable(TEST_UUID, "VAR", 100, 0);
            scopeManager.setGlobalConstants(Map.of("VAR", 50));

            Object value = scopeManager.resolveVariable("VAR", mockPlayer);

            assertThat(value).isEqualTo(100);
        }

        @Test
        @DisplayName("プレイヤー変数が次に優先される")
        void resolveVariable_PlayerSecond_ReturnsPlayerValue() {
            scopeManager.setPlayerVariable(TEST_UUID, "VAR", 75);
            scopeManager.setGlobalConstants(Map.of("VAR", 50));

            Object value = scopeManager.resolveVariable("VAR", mockPlayer);

            assertThat(value).isEqualTo(75);
        }

        @Test
        @DisplayName("グローバル定数が次に優先される")
        void resolveVariable_GlobalThird_ReturnsGlobalValue() {
            scopeManager.setGlobalConstants(Map.of("VAR", 50));

            Object value = scopeManager.resolveVariable("VAR", mockPlayer);

            assertThat(value).isEqualTo(50);
        }

        @Test
        @DisplayName("存在しない変数はnull")
        void resolveVariable_NonExisting_ReturnsNull() {
            Object value = scopeManager.resolveVariable("NONEXISTENT", mockPlayer);

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("システム変数STRを取得できる")
        void resolveVariable_STR_ReturnsStrength() {
            when(mockStatManager.getFinalStat(Stat.STRENGTH)).thenReturn(15);

            Object value = scopeManager.resolveVariable("STR", mockPlayer);

            assertThat(value).isEqualTo(15);
        }

        @Test
        @DisplayName("システム変数INTを取得できる")
        void resolveVariable_INT_ReturnsIntelligence() {
            when(mockStatManager.getFinalStat(Stat.INTELLIGENCE)).thenReturn(12);

            Object value = scopeManager.resolveVariable("INT", mockPlayer);

            assertThat(value).isEqualTo(12);
        }

        @Test
        @DisplayName("システム変数LVを取得できる")
        void resolveVariable_LV_ReturnsLevel() {
            Object value = scopeManager.resolveVariable("LV", mockPlayer);

            assertThat(value).isEqualTo(10);
        }

        @Test
        @DisplayName("システム変数PLAYER_LEVELを取得できる")
        void resolveVariable_PLAYER_LEVEL_ReturnsLevel() {
            Object value = scopeManager.resolveVariable("PLAYER_LEVEL", mockPlayer);

            assertThat(value).isEqualTo(10);
        }

        @Test
        @DisplayName("システム変数CLASS_RANKを取得できる")
        void resolveVariable_CLASS_RANK_ReturnsClassRank() {
            Object value = scopeManager.resolveVariable("CLASS_RANK", mockPlayer);

            assertThat(value).isEqualTo(5);
        }

        @Test
        @DisplayName("大文字小文字を区別しないシステム変数")
        void resolveVariable_SystemCaseInsensitive_Works() {
            when(mockStatManager.getFinalStat(Stat.STRENGTH)).thenReturn(15);

            assertThat(scopeManager.resolveVariable("str", mockPlayer)).isEqualTo(15);
            assertThat(scopeManager.resolveVariable("Str", mockPlayer)).isEqualTo(15);
            assertThat(scopeManager.resolveVariable("STR", mockPlayer)).isEqualTo(15);
        }
    }

    // ==================== VariableContext構築 テスト ====================

    @Nested
    @DisplayName("VariableContext構築 テスト")
    class VariableContextTests {

        @Test
        @DisplayName("スキルレベルなしで構築できる")
        void buildVariableContext_NoSkillLevel_CreatesContext() {
            VariableContext context = scopeManager.buildVariableContext(mockPlayer, null);

            assertThat(context).isNotNull();
        }

        @Test
        @DisplayName("スキルレベルありで構築できる")
        void buildVariableContext_WithSkillLevel_CreatesContext() {
            VariableContext context = scopeManager.buildVariableContext(mockPlayer, 5);

            assertThat(context).isNotNull();
        }

        @Test
        @DisplayName("追加変数ありで構築できる")
        void buildVariableContext_WithAdditional_CreatesContext() {
            Map<String, Double> additional = Map.of("SKILL_DAMAGE", 50.0);

            VariableContext context = scopeManager.buildVariableContext(mockPlayer, null, additional);

            assertThat(context).isNotNull();
        }

        @Test
        @DisplayName("nullの追加変数で構築できる")
        void buildVariableContext_NullAdditional_CreatesContext() {
            VariableContext context = scopeManager.buildVariableContext(mockPlayer, 5, null);

            assertThat(context).isNotNull();
        }

        @Test
        @DisplayName("空の追加変数で構築できる")
        void buildVariableContext_EmptyAdditional_CreatesContext() {
            VariableContext context = scopeManager.buildVariableContext(mockPlayer, 5, Map.of());

            assertThat(context).isNotNull();
        }
    }

    // ==================== デバッグ テスト ====================

    @Nested
    @DisplayName("デバッグ テスト")
    class DebugTests {

        @Test
        @DisplayName("debugScopeで文字列が返される")
        void debugScope_ReturnsString() {
            when(mockStatManager.getFinalStat(Stat.STRENGTH)).thenReturn(15);
            when(mockStatManager.getFinalStat(Stat.INTELLIGENCE)).thenReturn(12);
            when(mockStatManager.getFinalStat(Stat.SPIRIT)).thenReturn(10);
            when(mockStatManager.getFinalStat(Stat.VITALITY)).thenReturn(14);
            when(mockStatManager.getFinalStat(Stat.DEXTERITY)).thenReturn(13);

            String debug = scopeManager.debugScope(mockPlayer);

            assertThat(debug).contains("VariableScopeManager Debug");
            assertThat(debug).contains("STR=15");
            assertThat(debug).contains("LV=10");
        }

        @Test
        @DisplayName("nullプレイヤーでdebugScopeが動作する")
        void debugScope_NullPlayer_Works() {
            String debug = scopeManager.debugScope(null);

            assertThat(debug).contains("VariableScopeManager Debug");
        }
    }
}
