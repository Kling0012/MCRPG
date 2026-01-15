package com.example.rpgplugin.core.dependency;

import com.example.rpgplugin.RPGPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
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

    @Mock
    private RPGPlugin mockRpgPlugin;

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

        // プラグインモック設定
        when(mockPlugin.getLogger()).thenReturn(mockLogger);

        // Vault plugin mock
        when(mockPluginManager.getPlugin("Vault")).thenReturn(mockVaultPlugin);
        when(mockVaultPlugin.isEnabled()).thenReturn(true);
        when(mockVaultPlugin.getName()).thenReturn("Vault");

        // MythicMobs plugin mock
        when(mockPluginManager.getPlugin("MythicMobs")).thenReturn(mockMythicMobsPlugin);
        when(mockMythicMobsPlugin.isEnabled()).thenReturn(true);
        when(mockMythicMobsPlugin.getName()).thenReturn("MythicMobs");

        // PlaceholderAPI plugin mock
        when(mockPluginManager.getPlugin("PlaceholderAPI")).thenReturn(mockPapiPlugin);
        when(mockPapiPlugin.isEnabled()).thenReturn(true);
        when(mockPapiPlugin.getName()).thenReturn("PlaceholderAPI");

        // PlaceholderAPI plugin meta mock
        var papiMeta = mock(io.papermc.paper.plugin.configuration.PluginMeta.class);
        when(mockPapiPlugin.getPluginMeta()).thenReturn(papiMeta);
        when(papiMeta.getVersion()).thenReturn("2.11.3");

        dependencyManager = new DependencyManager(mockPlugin);
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
        @DisplayName("初期状態は全依存が利用不可能")
        void constructor_InitiallyNoDependencies() {
            assertThat(dependencyManager.isVaultAvailable()).isFalse();
            assertThat(dependencyManager.isMythicMobsAvailable()).isFalse();
            assertThat(dependencyManager.isPlaceholderApiAvailable()).isFalse();
        }

        @Test
        @DisplayName("初期状態でゲッターはnullを返す")
        void constructor_GettersReturnNull() {
            assertThat(dependencyManager.getVaultHook()).isNull();
            assertThat(dependencyManager.getMythicMobsHook()).isNull();
        }
    }

    // ==================== setupDependencies テスト ====================

    @Nested
    @DisplayName("setupDependencies テスト")
    class SetupDependenciesTests {

        @Test
        @DisplayName("Vaultがない場合はfalseを返す")
        void setupDependencies_NoVault_ReturnsFalse() {
            when(mockPluginManager.getPlugin("Vault")).thenReturn(null);

            boolean result = dependencyManager.setupDependencies();

            assertThat(result).isFalse();
            verify(mockLogger).severe(contains("MISSING REQUIRED DEPENDENCIES"));
        }

        @Test
        @DisplayName("Vaultが無効な場合はfalseを返す")
        void setupDependencies_VaultDisabled_ReturnsFalse() {
            when(mockVaultPlugin.isEnabled()).thenReturn(false);

            boolean result = dependencyManager.setupDependencies();

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("MythicMobsがない場合はfalseを返す")
        void setupDependencies_NoMythicMobs_ReturnsFalse() {
            when(mockPluginManager.getPlugin("MythicMobs")).thenReturn(null);

            boolean result = dependencyManager.setupDependencies();

            assertThat(result).isFalse();
            verify(mockLogger, atLeastOnce()).severe(contains("MythicMobs"));
        }

        @Test
        @DisplayName("全必須依存がない場合は両方ログに出力される")
        void setupDependencies_NoRequiredDependencies_LogsBoth() {
            when(mockPluginManager.getPlugin("Vault")).thenReturn(null);
            when(mockPluginManager.getPlugin("MythicMobs")).thenReturn(null);

            dependencyManager.setupDependencies();

            // ログが少なくとも1回呼ばれることを確認
            verify(mockLogger, atLeastOnce()).severe(contains("Vault"));
            verify(mockLogger, atLeastOnce()).severe(contains("MythicMobs"));
        }

        @Test
        @DisplayName("PlaceholderAPIはオプションなのでなくても成功する可能性がある")
        void setupDependencies_NoPapi_CanStillSucceed() {
            // VaultとMythicMobsのセットアップを試みるが、実際には失敗する可能性がある
            // ここではPAPIがない場合でもチェックが続くことを確認
            when(mockPluginManager.getPlugin("PlaceholderAPI")).thenReturn(null);

            dependencyManager.setupDependencies();

            verify(mockLogger).info(contains("PlaceholderAPI not found"));
        }

        @Test
        @DisplayName("ダウンロードリンクがログに含まれる")
        void setupDependencies_MissingDependencies_ContainsDownloadLinks() {
            when(mockPluginManager.getPlugin("Vault")).thenReturn(null);
            when(mockPluginManager.getPlugin("MythicMobs")).thenReturn(null);

            dependencyManager.setupDependencies();

            verify(mockLogger, atLeastOnce()).severe(contains("spigotmc.org"));
        }
    }

    // ==================== 成功セットアップ テスト ====================

    @Nested
    @DisplayName("成功セットアップ テスト")
    class SuccessfulSetupTests {

        @Mock
        private org.bukkit.plugin.ServicesManager mockServicesManager;

        @Mock
        private RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> mockEconomyRsp;

        @Mock
        private net.milkbowl.vault.economy.Economy mockEconomy;

        @BeforeEach
        void setUp() {
            mockedBukkit.when(Bukkit::getServicesManager).thenReturn(mockServicesManager);
            when(mockServer.getServicesManager()).thenReturn(mockServicesManager);

            // Economyサービスのモック
            when(mockEconomy.isEnabled()).thenReturn(true);
            when(mockEconomyRsp.getProvider()).thenReturn(mockEconomy);
            when(mockServicesManager.getRegistration(net.milkbowl.vault.economy.Economy.class)).thenReturn(mockEconomyRsp);

            // Vaultプラグインは有効
            when(mockPluginManager.getPlugin("Vault")).thenReturn(mockVaultPlugin);
            when(mockVaultPlugin.isEnabled()).thenReturn(true);

            // Plugin.getServer()がnullにならないように
            when(mockPlugin.getServer()).thenReturn(mockServer);
        }

        @Test
        @DisplayName("Vaultが正常にセットアップされると利用可能になる")
        void setupDependencies_VaultSetup_Success_BecomesAvailable() {
            boolean result = dependencyManager.setupDependencies();

            // MythicMobsがないので全体としては失敗する
            assertThat(result).isFalse();
            // しかしVaultフックは作成されている
            assertThat(dependencyManager.getVaultHook()).isNotNull();
        }

        @Test
        @DisplayName("セットアップ成功時に適切なログが出力される")
        void setupDependencies_VaultSetup_LogsSuccess() {
            dependencyManager.setupDependencies();

            verify(mockLogger, atLeastOnce()).info(contains("Vault"));
        }

        @Test
        @DisplayName("PlaceholderAPIのセットアップ失敗時のログ")
        void setupDependencies_PapiSetupFailed_LogsWarning() {
            dependencyManager.setupDependencies();

            verify(mockLogger, atLeastOnce()).info(contains("PlaceholderAPI"));
        }

        @Test
        @DisplayName("セットアップに失敗してもフックがnull安全")
        void setupDependencies_FailedSetup_NullSafe() {
            when(mockPluginManager.getPlugin("Vault")).thenReturn(null);

            assertThatCode(() -> dependencyManager.setupDependencies())
                    .doesNotThrowAnyException();
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
        @DisplayName("isVaultAvailableの初期状態はfalse")
        void isVaultAvailable_InitiallyFalse() {
            assertThat(dependencyManager.isVaultAvailable()).isFalse();
        }

        @Test
        @DisplayName("isMythicMobsAvailableの初期状態はfalse")
        void isMythicMobsAvailable_InitiallyFalse() {
            assertThat(dependencyManager.isMythicMobsAvailable()).isFalse();
        }

        @Test
        @DisplayName("isPlaceholderApiAvailableの初期状態はfalse")
        void isPlaceholderApiAvailable_InitiallyFalse() {
            assertThat(dependencyManager.isPlaceholderApiAvailable()).isFalse();
        }
    }

    // ==================== ゲッター テスト ====================

    @Nested
    @DisplayName("ゲッター テスト")
    class GetterTests {

        @Test
        @DisplayName("getVaultHookは初期状態null")
        void getVaultHook_InitiallyNull() {
            assertThat(dependencyManager.getVaultHook()).isNull();
        }

        @Test
        @DisplayName("getMythicMobsHookは初期状態null")
        void getMythicMobsHook_InitiallyNull() {
            assertThat(dependencyManager.getMythicMobsHook()).isNull();
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
            verify(mockLogger).info(contains("Vault:"));
            verify(mockLogger).info(contains("MythicMobs:"));
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
        @DisplayName("cleanupで全フックがnullになる")
        void cleanup_SetsHooksToNull() {
            dependencyManager.cleanup();

            assertThat(dependencyManager.getVaultHook()).isNull();
            assertThat(dependencyManager.getMythicMobsHook()).isNull();
        }

        @Test
        @DisplayName("cleanup後は全利用不可フラグがfalse")
        void cleanup_AfterCleanup_AllUnavailable() {
            dependencyManager.cleanup();

            assertThat(dependencyManager.isVaultAvailable()).isFalse();
            assertThat(dependencyManager.isMythicMobsAvailable()).isFalse();
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
            assertThat(dependencyManager.getVaultHook()).isNull();
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
            assertThat(manager1.isVaultAvailable()).isEqualTo(manager2.isVaultAvailable());
            assertThat(manager1.isMythicMobsAvailable()).isEqualTo(manager2.isMythicMobsAvailable());
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
                dependencyManager.getVaultHook();
                dependencyManager.getMythicMobsHook();
                dependencyManager.isVaultAvailable();
                dependencyManager.isMythicMobsAvailable();
                dependencyManager.isPlaceholderApiAvailable();
            }).doesNotThrowAnyException();
        }
    }
}
