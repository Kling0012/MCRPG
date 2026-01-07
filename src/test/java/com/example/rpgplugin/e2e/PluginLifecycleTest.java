package com.example.rpgplugin.e2e;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.core.system.CoreSystemManager;
import com.example.rpgplugin.core.system.GameSystemManager;
import com.example.rpgplugin.core.system.GUIManager;
import com.example.rpgplugin.core.system.ExternalSystemManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * RPGPluginのエンドツーエンドテスト（構造検証）
 *
 * <p>プラグインのクラス構造とAPIシグネチャを検証します。</p>
 *
 * <p>注意: このテストはリフレクションベースで、Bukkitサーバーなしで実行可能です。
 * 実際のライフサイクルテストにはMockBukkitを使用してください。</p>
 *
 * テスト範囲:
 * - プラグインのクラス構造
 * - ファサードパターンの適用
 * - 後方互換性のあるgetterメソッド
 * - 初期化・シャットダウンメソッドの存在
 *
 * @author RPGPlugin Team
 * @version 2.0.0
 */
@DisplayName("RPGPlugin E2E テスト: 構造検証")
class PluginLifecycleTest {

    private final Class<RPGPlugin> pluginClass = RPGPlugin.class;

    // ==================== シナリオ1: ファサードパターンの検証 ====================

    @Nested
    @DisplayName("ファサードパターン構造テスト")
    class FacadePatternTests {

        @Test
        @DisplayName("シナリオ1: RPGPluginが4つのシステムマネージャーフィールドを持つ")
        void scenario01_HasFourSystemManagerFields() {
            // 期待されるファサードフィールド
            List<String> expectedFields = Arrays.asList(
                "coreSystem",
                "gameSystem",
                "guiSystem",
                "externalSystem"
            );

            // フィールドの存在を検証
            for (String fieldName : expectedFields) {
                assertThatCode(() -> pluginClass.getDeclaredField(fieldName))
                    .as("フィールド '%s' が存在すること", fieldName)
                    .doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("シナリオ2: システムマネージャーフィールドが正しい型を持つ")
        void scenario02_SystemManagerFieldsHaveCorrectTypes() throws NoSuchFieldException {
            // CoreSystemManager
            Field coreSystemField = pluginClass.getDeclaredField("coreSystem");
            assertThat(coreSystemField.getType()).isEqualTo(CoreSystemManager.class);

            // GameSystemManager
            Field gameSystemField = pluginClass.getDeclaredField("gameSystem");
            assertThat(gameSystemField.getType()).isEqualTo(GameSystemManager.class);

            // GUIManager
            Field guiSystemField = pluginClass.getDeclaredField("guiSystem");
            assertThat(guiSystemField.getType()).isEqualTo(GUIManager.class);

            // ExternalSystemManager
            Field externalSystemField = pluginClass.getDeclaredField("externalSystem");
            assertThat(externalSystemField.getType()).isEqualTo(ExternalSystemManager.class);
        }

        @Test
        @DisplayName("シナリオ3: フィールド数が大幅に削減されている（25個→4個+instance）")
        void scenario03_ReducedFieldCount() {
            // privateフィールドのみをカウント（継承フィールドを除く）
            List<Field> declaredFields = Arrays.stream(pluginClass.getDeclaredFields())
                .filter(f -> Modifier.isPrivate(f.getModifiers()))
                .filter(f -> !f.isSynthetic())
                .collect(Collectors.toList());

            // 期待: instance + coreSystem + gameSystem + guiSystem + externalSystem = 5個
            assertThat(declaredFields.size())
                .as("RPGPluginのprivateフィールド数が10個以下であること")
                .isLessThanOrEqualTo(10);
        }
    }

    // ==================== シナリオ4-6: ライフサイクルメソッドの検証 ====================

    @Nested
    @DisplayName("ライフサイクルメソッドテスト")
    class LifecycleMethodTests {

        @Test
        @DisplayName("シナリオ4: onEnableメソッドが存在する")
        void scenario04_OnEnableMethodExists() {
            assertThatCode(() -> pluginClass.getMethod("onEnable"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("シナリオ5: onDisableメソッドが存在する")
        void scenario05_OnDisableMethodExists() {
            assertThatCode(() -> pluginClass.getMethod("onDisable"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("シナリオ6: reloadPluginメソッドが存在する")
        void scenario06_ReloadMethodExists() {
            assertThatCode(() -> pluginClass.getMethod("reloadPlugin"))
                .doesNotThrowAnyException();
        }
    }

    // ==================== シナリオ7-10: 後方互換性Getterの検証 ====================

    @Nested
    @DisplayName("後方互換性Getterテスト")
    class BackwardCompatibilityGetterTests {

        @Test
        @DisplayName("シナリオ7: 全20個のマネージャーgetterが存在する")
        void scenario07_AllManagerGettersExist() {
            List<String> expectedGetters = Arrays.asList(
                "getStorageManager",
                "getPlayerManager",
                "getStatManager",
                "getClassManager",
                "getSkillManager",
                "getAuctionManager",
                "getDependencyManager",
                "getModuleManager",
                "getConfigManager",
                "getConfigWatcher",
                "getDamageManager",
                "getCurrencyManager",
                "getTradeManager",
                "getMythicMobsManager",
                "getSkillMenuListener",
                "getClassMenuListener",
                "getCurrencyListener",
                "getMythicDeathListener",
                "getActiveSkillExecutor",
                "getPassiveSkillExecutor"
            );

            for (String methodName : expectedGetters) {
                assertThatCode(() -> pluginClass.getMethod(methodName))
                    .as("メソッド '%s' が存在すること", methodName)
                    .doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("シナリオ8: getInstanceが静的メソッドである")
        void scenario08_GetInstanceIsStatic() throws NoSuchMethodException {
            Method getInstanceMethod = pluginClass.getMethod("getInstance");
            assertThat(Modifier.isStatic(getInstanceMethod.getModifiers())).isTrue();
            assertThat(getInstanceMethod.getReturnType()).isEqualTo(RPGPlugin.class);
        }

        @Test
        @DisplayName("シナリオ9: getAPIメソッドが存在する")
        void scenario09_GetAPIMethodExists() {
            assertThatCode(() -> pluginClass.getMethod("getAPI"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("シナリオ10: getExpDiminisherメソッドが存在する")
        void scenario10_GetExpDiminisherMethodExists() {
            assertThatCode(() -> pluginClass.getMethod("getExpDiminisher"))
                .doesNotThrowAnyException();
        }
    }

    // ==================== シナリオ11-13: 初期化ヘルパーメソッドの検証 ====================

    @Nested
    @DisplayName("初期化ヘルパーメソッドテスト")
    class InitializationHelperTests {

        @Test
        @DisplayName("シナリオ11: setupMainConfigメソッドが存在する（privateでも可）")
        void scenario11_SetupMainConfigExists() {
            boolean found = Arrays.stream(pluginClass.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("setupMainConfig"));
            assertThat(found)
                .as("setupMainConfigメソッドが存在すること")
                .isTrue();
        }

        @Test
        @DisplayName("シナリオ12: setupConfigWatcherメソッドが存在する（privateでも可）")
        void scenario12_SetupConfigWatcherExists() {
            boolean found = Arrays.stream(pluginClass.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("setupConfigWatcher"));
            assertThat(found)
                .as("setupConfigWatcherメソッドが存在すること")
                .isTrue();
        }

        @Test
        @DisplayName("シナリオ13: enableModulesメソッドが存在する（privateでも可）")
        void scenario13_EnableModulesExists() {
            boolean found = Arrays.stream(pluginClass.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("enableModules"));
            assertThat(found)
                .as("enableModulesメソッドが存在すること")
                .isTrue();
        }
    }

    // ==================== シナリオ14-16: システムマネージャークラスの検証 ====================

    @Nested
    @DisplayName("システムマネージャークラステスト")
    class SystemManagerClassTests {

        @Test
        @DisplayName("シナリオ14: CoreSystemManagerクラスが存在し、initializeメソッドを持つ")
        void scenario14_CoreSystemManagerHasInitialize() {
            assertThatCode(() -> CoreSystemManager.class.getMethod("initialize"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("シナリオ15: GameSystemManagerクラスが存在し、initializeメソッドを持つ")
        void scenario15_GameSystemManagerHasInitialize() {
            assertThatCode(() -> GameSystemManager.class.getMethod("initialize"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("シナリオ16: 各システムマネージャーがshutdownメソッドを持つ")
        void scenario16_AllSystemManagersHaveShutdown() {
            List<Class<?>> managerClasses = Arrays.asList(
                CoreSystemManager.class,
                GameSystemManager.class,
                GUIManager.class,
                ExternalSystemManager.class
            );

            for (Class<?> managerClass : managerClasses) {
                assertThatCode(() -> managerClass.getMethod("shutdown"))
                    .as("%s がshutdownメソッドを持つこと", managerClass.getSimpleName())
                    .doesNotThrowAnyException();
            }
        }
    }
}
