package com.example.rpgplugin.core.dependency;

import com.example.rpgplugin.RPGPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

/**
 * DependencyManagerのユニットテスト
 *
 * <p>外部プラグイン依存関係管理クラスのテストを行います。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DependencyManager テスト")
class DependencyManagerTest {

    @Mock
    private JavaPlugin mockPlugin;

    @Mock
    private RPGPlugin mockRpgPlugin;

    @Mock
    private Logger mockLogger;

    @Mock
    private Server mockServer;

    @Mock
    private PluginManager mockPluginManager;

    @Mock
    private Plugin mockVaultPlugin;

    @Mock
    private Plugin mockMythicMobsPlugin;

    @Mock
    private Plugin mockPapiPlugin;

    private MockedStatic<Bukkit> mockedBukkit;
    private DependencyManager dependencyManager;

    @BeforeEach
    void setUp() {
        // Bukkitの静的メソッドをモック
        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::getServer).thenReturn(mockServer);
        // DependencyManagerはBukkit.getPluginManager()を直接呼び出す
        mockedBukkit.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
        when(mockServer.getPluginManager()).thenReturn(mockPluginManager);

        // RPGPluginモック設定
        when(mockRpgPlugin.getLogger()).thenReturn(mockLogger);

        // プラグインモック設定（RPGPluginとしても振る舞う）
        when(mockPlugin.getLogger()).thenReturn(mockLogger);

        // Vault plugin mock
        when(mockPluginManager.getPlugin("Vault")).thenReturn(mockVaultPlugin);
        when(mockVaultPlugin.isEnabled()).thenReturn(true);
        when(mockVaultPlugin.getName()).thenReturn("Vault");

        // MythicMobs plugin mock
        when(mockPluginManager.getPlugin("MythicMobs")).thenReturn(mockMythicMobsPlugin);
        when(mockMythicMobsPlugin.isEnabled()).thenReturn(true);
        when(mockMythicMobsPlugin.getName()).thenReturn("MythicMobs");

        // PlaceholderAPI plugin mock - null by default to avoid NoClassDefFoundError
        // Note: When PlaceholderAPI is mocked as enabled, checkPlaceholderAPI() tries to
        // instantiate PlaceholderHook which requires actual PlaceholderAPI classes
        when(mockPluginManager.getPlugin("PlaceholderAPI")).thenReturn(null);

        dependencyManager = new DependencyManager(mockRpgPlugin);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    // ==================== コンストラクタ テスト ====================

    @Nested
    @DisplayName("コンストラクタ テスト")
    class ConstructorTests {

        @Test
        @DisplayName("コンストラクタでインスタンスが生成される")
        void constructor_CreatesInstance() {
            assertThat(dependencyManager).isNotNull();
        }

        @Test
        @DisplayName("初期状態はPlaceholderAPIが利用不可能")
        void constructor_InitiallyNoDependencies() {
            assertThat(dependencyManager.isPlaceholderApiAvailable()).isFalse();
        }

        @Test
        @DisplayName("初期状態でゲッターはnullを返す")
        void constructor_GettersReturnNull() {
            assertThat(dependencyManager.getPlaceholderHook()).isNull();
        }
    }

    // ==================== setupDependencies テスト ====================

    @Nested
    @DisplayName("setupDependencies テスト")
    class SetupDependenciesTests {

        @Test
        @DisplayName("setupDependenciesは常にtrueを返す（オプション依存のみ）")
        void setupDependencies_AlwaysReturnsTrue() {
            boolean result = dependencyManager.setupDependencies();

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("PlaceholderAPIがない場合も成功")
        void setupDependencies_NoPapiStillSucceeds() {
            when(mockPluginManager.getPlugin("PlaceholderAPI")).thenReturn(null);

            boolean result = dependencyManager.setupDependencies();

            assertThat(result).isTrue();
        }
    }

    // ==================== 成功セットアップ テスト ====================
    // 注: Vault連携は削除されたため、PlaceholderAPIのみテスト

    @Nested
    @DisplayName("成功セットアップ テスト")
    class SuccessfulSetupTests {

        @Test
        @DisplayName("PlaceholderAPIがない場合でもsetupDependenciesは成功")
        void setupDependencies_NoPapi_Succeeds() {
            // PlaceholderAPIはnullに設定
            when(mockPluginManager.getPlugin("PlaceholderAPI")).thenReturn(null);

            dependencyManager.setupDependencies();

            // フックは作成されない
            assertThat(dependencyManager.getPlaceholderHook()).isNull();
            // でもsetupDependenciesは成功（オプション依存）
            assertThat(dependencyManager.isPlaceholderApiAvailable()).isFalse();
        }

        @Test
        @DisplayName("PlaceholderAPIのセットアップ失敗時のログ")
        void setupDependencies_PapiSetupFailed_LogsWarning() {
            dependencyManager.setupDependencies();

            verify(mockLogger, atLeastOnce()).info(contains("PlaceholderAPI"));
        }

        @Test
        @DisplayName("セットアップは常に成功（オプション依存のみ）")
        void setupDependencies_AlwaysSucceeds() {
            // VaultとMythicMobsは削除されたため、setupDependenciesは常にtrueを返す
            boolean result = dependencyManager.setupDependencies();

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("cleanupはフックがない場合も安全")
        void cleanup_NoHooks_NoException() {
            assertThatCode(() -> dependencyManager.cleanup())
                    .doesNotThrowAnyException();
        }
    }

    // ==================== isXxxAvailable テスト ====================

    @Nested
    @DisplayName("isXxxAvailable テスト")
    class IsAvailableTests {

        @Test
        @DisplayName("isPlaceholderApiAvailableの初期状態はfalse")
        void isPlaceholderApiAvailable_InitiallyFalse() {
            assertThat(dependencyManager.isPlaceholderApiAvailable()).isFalse();
        }
    }

    // ==================== logDependencyStatus テスト ====================

    @Nested
    @DisplayName("logDependencyStatus テスト")
    class LogDependencyStatusTests {

        @Test
        @DisplayName("logDependencyStatusでログが出力される")
        void logDependencyStatus_OutputsLog() {
            dependencyManager.logDependencyStatus();

            verify(mockLogger).info(contains("Dependency Status"));
            verify(mockLogger).info(contains("PlaceholderAPI:"));
        }

        @Test
        @DisplayName("logDependencyStatusはnull安全")
        void logDependencyStatus_NullSafe() {
            assertThatCode(() -> dependencyManager.logDependencyStatus())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("logDependencyStatusは複数回呼べる")
        void logDependencyStatus_MultipleCalls_Safe() {
            assertThatCode(() -> {
                dependencyManager.logDependencyStatus();
                dependencyManager.logDependencyStatus();
                dependencyManager.logDependencyStatus();
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("logDependencyStatusで正しいフォーマット")
        void logDependencyStatus_CorrectFormat() {
            dependencyManager.logDependencyStatus();

            verify(mockLogger, atLeastOnce()).info(contains("==="));
        }
    }

    // ==================== cleanup テスト ====================

    @Nested
    @DisplayName("cleanup テスト")
    class CleanupTests {

        @Test
        @DisplayName("cleanupでPlaceholderAPIフックがnullになる")
        void cleanup_SetsHooksToNull() {
            dependencyManager.cleanup();

            assertThat(dependencyManager.getPlaceholderHook()).isNull();
        }

        @Test
        @DisplayName("cleanup後はPlaceholderAPI利用不可フラグがfalse")
        void cleanup_AfterCleanup_Unavailable() {
            dependencyManager.cleanup();

            assertThat(dependencyManager.isPlaceholderApiAvailable()).isFalse();
        }

        @Test
        @DisplayName("cleanupは複数回呼べる")
        void cleanup_MultipleCalls_Safe() {
            assertThatCode(() -> {
                dependencyManager.cleanup();
                dependencyManager.cleanup();
                dependencyManager.cleanup();
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("cleanupでログが出力される")
        void cleanup_OutputsLog() {
            dependencyManager.cleanup();

            verify(mockLogger).info(contains("cleaned up"));
        }
    }

    // ==================== 統合テスト ====================

    @Nested
    @DisplayName("統合テスト")
    class IntegrationTests {

        @Test
        @DisplayName("ログ出力→クリーンアップの流れ")
        void fullFlow_LogThenCleanup() {
            // ログ出力
            assertThatCode(() -> dependencyManager.logDependencyStatus())
                    .doesNotThrowAnyException();

            // クリーンアップ
            dependencyManager.cleanup();
            assertThat(dependencyManager.getPlaceholderHook()).isNull();
        }

        @Test
        @DisplayName("複数回のセットアップ試行")
        void multipleSetupAttempts_Safe() {
            assertThatCode(() -> {
                dependencyManager.setupDependencies();
                dependencyManager.setupDependencies();
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("クリーンアップ後のセットアップ試行")
        void setupAfterCleanup_Safe() {
            dependencyManager.cleanup();

            assertThatCode(() -> dependencyManager.setupDependencies())
                    .doesNotThrowAnyException();
        }
    }

    // ==================== 複数インスタンス テスト ====================

    @Nested
    @DisplayName("複数インスタンス テスト")
    class MultipleInstancesTests {

        @Test
        @DisplayName("複数のインスタンスは独立")
        void multipleInstances_AreIndependent() {
            DependencyManager manager1 = new DependencyManager(mockPlugin);
            DependencyManager manager2 = new DependencyManager(mockPlugin);

            // 異なるインスタンス
            assertThat(manager1).isNotSameAs(manager2);

            // 同じ状態
            assertThat(manager1.isPlaceholderApiAvailable()).isEqualTo(manager2.isPlaceholderApiAvailable());
        }
    }

    // ==================== Null安全 テスト ====================

    @Nested
    @DisplayName("Null安全 テスト")
    class NullSafetyTests {

        @Test
        @DisplayName("全メソッドはnull安全")
        void allMethods_NullSafe() {
            assertThatCode(() -> {
                dependencyManager.cleanup();
                dependencyManager.logDependencyStatus();
                dependencyManager.getPlaceholderHook();
                dependencyManager.isPlaceholderApiAvailable();
            }).doesNotThrowAnyException();
        }
    }
}
