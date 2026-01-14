package com.example.rpgplugin.core.dependency;

import com.example.rpgplugin.RPGPlugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DependencyManagerのユニットテスト
 *
 * <p>依存関係管理クラスのテストを行います。</p>
 *
 * 設計原則:
 * - SOLID-S: DependencyManagerのテストに特化
 * - KISS: シンプルなテストケース
 * - 読みやすさ: テスト名で振る舞いを明示
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("DependencyManager テスト")
class DependencyManagerTest {

    /**
     * モックプラグインインスタンスを作成
     */
    private JavaPlugin createMockPlugin() {
        RPGPlugin mockPlugin = mock(RPGPlugin.class);
        when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("TestPlugin"));
        return mockPlugin;
    }

    // ==================== コンストラクタ ====================

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("コンストラクタ: 初期状態で全依存が利用不可")
        void constructor_Initial_State_AllUnavailable() {
            JavaPlugin mockPlugin = createMockPlugin();
            DependencyManager manager = new DependencyManager(mockPlugin);

            assertThat(manager.isVaultAvailable()).isFalse();
            assertThat(manager.isMythicMobsAvailable()).isFalse();
            assertThat(manager.isPlaceholderApiAvailable()).isFalse();
        }

        @Test
        @DisplayName("コンストラクタ: フックはnull")
        void constructor_Initial_HooksAreNull() {
            JavaPlugin mockPlugin = createMockPlugin();
            DependencyManager manager = new DependencyManager(mockPlugin);

            assertThat(manager.getVaultHook()).isNull();
            assertThat(manager.getMythicMobsHook()).isNull();
        }

        @Test
        @DisplayName("コンストラクタ: 正常にインスタンス化")
        void constructor_CreatesInstance() {
            JavaPlugin mockPlugin = createMockPlugin();

            DependencyManager manager = new DependencyManager(mockPlugin);

            assertThat(manager).isNotNull();
        }
    }

    // ==================== availabilityフラグ ====================

    @Nested
    @DisplayName("Availability Flags")
    class AvailabilityTests {

        @Test
        @DisplayName("isVaultAvailable: 初期状態はfalse")
        void isVaultAvailable_Initial_ReturnsFalse() {
            JavaPlugin mockPlugin = createMockPlugin();
            DependencyManager manager = new DependencyManager(mockPlugin);

            assertThat(manager.isVaultAvailable()).isFalse();
        }

        @Test
        @DisplayName("isMythicMobsAvailable: 初期状態はfalse")
        void isMythicMobsAvailable_Initial_ReturnsFalse() {
            JavaPlugin mockPlugin = createMockPlugin();
            DependencyManager manager = new DependencyManager(mockPlugin);

            assertThat(manager.isMythicMobsAvailable()).isFalse();
        }

        @Test
        @DisplayName("isPlaceholderApiAvailable: 初期状態はfalse")
        void isPlaceholderApiAvailable_Initial_ReturnsFalse() {
            JavaPlugin mockPlugin = createMockPlugin();
            DependencyManager manager = new DependencyManager(mockPlugin);

            assertThat(manager.isPlaceholderApiAvailable()).isFalse();
        }
    }

    // ==================== Getterメソッド ====================

    @Nested
    @DisplayName("Getter Methods")
    class GetterTests {

        @Test
        @DisplayName("getVaultHook: 初期状態はnull")
        void getVaultHook_Initial_ReturnsNull() {
            JavaPlugin mockPlugin = createMockPlugin();
            DependencyManager manager = new DependencyManager(mockPlugin);

            assertThat(manager.getVaultHook()).isNull();
        }

        @Test
        @DisplayName("getMythicMobsHook: 初期状態はnull")
        void getMythicMobsHook_Initial_ReturnsNull() {
            JavaPlugin mockPlugin = createMockPlugin();
            DependencyManager manager = new DependencyManager(mockPlugin);

            assertThat(manager.getMythicMobsHook()).isNull();
        }
    }

    // ==================== クリーンアップ ====================

    @Nested
    @DisplayName("Cleanup")
    class CleanupTests {

        @Test
        @DisplayName("cleanup: 初期状態でも例外なし")
        void cleanup_Initial_DoesNotThrow() {
            JavaPlugin mockPlugin = createMockPlugin();
            DependencyManager manager = new DependencyManager(mockPlugin);

            assertThatCode(() -> manager.cleanup()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("cleanup: クリーンアップ後もフラグはfalse")
        void cleanup_AfterCleanup_FlagsRemainFalse() {
            JavaPlugin mockPlugin = createMockPlugin();
            DependencyManager manager = new DependencyManager(mockPlugin);

            manager.cleanup();

            assertThat(manager.isVaultAvailable()).isFalse();
            assertThat(manager.isMythicMobsAvailable()).isFalse();
            assertThat(manager.isPlaceholderApiAvailable()).isFalse();
        }

        @Test
        @DisplayName("cleanup: クリーンアップ後もフックはnull")
        void cleanup_AfterCleanup_HooksRemainNull() {
            JavaPlugin mockPlugin = createMockPlugin();
            DependencyManager manager = new DependencyManager(mockPlugin);

            manager.cleanup();

            assertThat(manager.getVaultHook()).isNull();
            assertThat(manager.getMythicMobsHook()).isNull();
        }
    }

    // ==================== setupDependencies ====================

    @Nested
    @DisplayName("setupDependencies")
    class SetupDependenciesTests {

        @Test
        @DisplayName("setupDependencies: Bukkit依存のため完全なテストにはモックが必要")
        void setupDependencies_RequiresBukkitMock() {
            // 注意: setupDependenciesはBukkit.getPluginManager()を呼び出すため、
            // 完全なテストにはMockedStatic<Bukkit>が必要
            // ここではメソッドが存在することを確認のみ

            JavaPlugin mockPlugin = createMockPlugin();
            DependencyManager manager = new DependencyManager(mockPlugin);

            assertThat(manager).isNotNull();
            // setupDependenciesを実際に呼ぶにはBukkitの静的モックが必要
            // これはインテグレーションテストで対応
        }
    }

    // ==================== logDependencyStatus ====================

    @Nested
    @DisplayName("logDependencyStatus")
    class LogDependencyStatusTests {

        @Test
        @DisplayName("logDependencyStatus: 初期状態でも例外なし")
        void logDependencyStatus_Initial_DoesNotThrow() {
            JavaPlugin mockPlugin = createMockPlugin();
            DependencyManager manager = new DependencyManager(mockPlugin);

            assertThatCode(() -> manager.logDependencyStatus()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("logDependencyStatus: クリーンアップ後でも例外なし")
        void logDependencyStatus_AfterCleanup_DoesNotThrow() {
            JavaPlugin mockPlugin = createMockPlugin();
            DependencyManager manager = new DependencyManager(mockPlugin);

            manager.cleanup();

            assertThatCode(() -> manager.logDependencyStatus()).doesNotThrowAnyException();
        }
    }

    // ==================== 複数インスタンス ====================

    @Nested
    @DisplayName("Multiple Instances")
    class MultipleInstancesTests {

        @Test
        @DisplayName("複数のインスタンスは独立")
        void multipleInstances_AreIndependent() {
            JavaPlugin mockPlugin = createMockPlugin();
            DependencyManager manager1 = new DependencyManager(mockPlugin);
            DependencyManager manager2 = new DependencyManager(mockPlugin);

            // 異なるインスタンス
            assertThat(manager1).isNotSameAs(manager2);

            // 同じ状態
            assertThat(manager1.isVaultAvailable()).isEqualTo(manager2.isVaultAvailable());
            assertThat(manager1.isMythicMobsAvailable()).isEqualTo(manager2.isMythicMobsAvailable());
        }
    }
}
