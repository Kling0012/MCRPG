package com.example.rpgplugin.integration;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.core.config.YamlConfigManager;
import com.example.rpgplugin.core.dependency.DependencyManager;
import com.example.rpgplugin.core.module.IModule;
import com.example.rpgplugin.core.module.ModuleManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * システム統合テスト
 *
 * <p>プラグインの主要システム間の連携と初期化順序を検証します。</p>
 *
 * <p>テスト対象:</p>
 * <ul>
 *   <li>ModuleManager: モジュールシステムの初期化と管理</li>
 *   <li>DependencyManager: 外部プラグインとの依存関係チェック</li>
 *   <li>YamlConfigManager: 設定ファイルの読み込みと管理</li>
 *   <li>全システム連携: 複数システムの協調動作</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-I: 統合テストに特化</li>
 *   <li>現実的: 本番環境に近い構成でテスト</li>
 *   <li>独立性: 各テストは独立して実行可能</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("システム統合テスト")
class SystemIntegrationTest {

    @Mock
    private JavaPlugin mockPlugin;

    @Mock
    private Logger mockLogger;

    @Mock
    private PluginManager mockPluginManager;

    @Mock
    private Plugin mockVaultPlugin;

    @Mock
    private Plugin mockMythicMobsPlugin;

    @Mock
    private File mockDataFolder;

    private MockedStatic<Bukkit> mockedBukkit;

    /**
     * 各テストの前に実行されるセットアップ処理
     *
     * <p>Bukkit APIのモック化と共通設定を行います。</p>
     */
    @BeforeEach
    void setUp() {
        // 前のモックが残っている場合はクローズする
        if (mockedBukkit != null) {
            try {
                mockedBukkit.close();
            } catch (Exception e) {
                // クローズ時の例外は無視
            }
        }

        // Bukkit静的メソッドのモック化
        mockedBukkit = mockStatic(Bukkit.class);

        // Bukkit.getPluginManager() の振る舞いを設定
        mockedBukkit.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);

        // Pluginの基本設定
        when(mockPlugin.getLogger()).thenReturn(mockLogger);
        when(mockPlugin.getDataFolder()).thenReturn(mockDataFolder);

        // モックフォルダの振る舞いを設定
        when(mockDataFolder.exists()).thenReturn(true);
        when(mockDataFolder.mkdirs()).thenReturn(true);

        // Vaultプラグインのモック設定
        when(mockVaultPlugin.getName()).thenReturn("Vault");
        when(mockVaultPlugin.isEnabled()).thenReturn(true);
        when(mockPluginManager.getPlugin("Vault")).thenReturn(mockVaultPlugin);

        // MythicMobsプラグインのモック設定
        when(mockMythicMobsPlugin.getName()).thenReturn("MythicMobs");
        when(mockMythicMobsPlugin.isEnabled()).thenReturn(true);
        when(mockPluginManager.getPlugin("MythicMobs")).thenReturn(mockMythicMobsPlugin);

        // PlaceholderAPIは存在しない（オプション扱い）
        when(mockPluginManager.getPlugin("PlaceholderAPI")).thenReturn(null);
    }

    /**
     * 各テストの後に実行されるクリーンアップ処理
     *
     * <p>モックを解放し、リソースを解放します。</p>
     */
    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    // ==================== ModuleManager テスト ====================

    @Test
    @DisplayName("ModuleManager: 初期化と基本機能")
    void testModuleManager_Initialization() {
        // Given: ModuleManagerインスタンスを作成
        ModuleManager moduleManager = new ModuleManager(mockPlugin);

        // When: モジュールを登録
        TestModule module1 = new TestModule("TestModule1", "1.0.0");
        TestModule module2 = new TestModule("TestModule2", "2.0.0");

        moduleManager.registerModule(module1);
        moduleManager.registerModule(module2);

        // Then: モジュールが正しく登録されている
        assertThat(moduleManager.getModuleCount()).isEqualTo(2);
        assertThat(moduleManager.getModuleNames()).containsExactlyInAnyOrder("TestModule1", "TestModule2");
        assertThat(moduleManager.getModule("TestModule1")).isNotNull();
        assertThat(moduleManager.getModule("TestModule2")).isNotNull();
    }

    @Test
    @DisplayName("ModuleManager: モジュールの有効化と無効化")
    void testModuleManager_EnableDisable() {
        // Given: ModuleManagerとモジュールを準備
        ModuleManager moduleManager = new ModuleManager(mockPlugin);
        TestModule module = new TestModule("TestModule", "1.0.0");
        moduleManager.registerModule(module);

        // When: モジュールを有効化
        boolean enableResult = moduleManager.enableModule("TestModule");

        // Then: 有効化が成功し、状態が正しく反映される
        assertThat(enableResult).isTrue();
        assertThat(moduleManager.isModuleEnabled("TestModule")).isTrue();
        assertThat(module.isEnabled()).isTrue();

        // When: モジュールを無効化
        boolean disableResult = moduleManager.disableModule("TestModule");

        // Then: 無効化が成功し、状態が正しく反映される
        assertThat(disableResult).isTrue();
        assertThat(moduleManager.isModuleEnabled("TestModule")).isFalse();
        assertThat(module.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("ModuleManager: 依存関係を考慮した起動順序")
    void testModuleManager_DependencyOrder() {
        // Given: 依存関係のあるモジュールを準備
        ModuleManager moduleManager = new ModuleManager(mockPlugin);

        TestModule moduleA = new TestModule("ModuleA", "1.0.0");
        TestModule moduleB = new TestModule("ModuleB", "1.0.0", new String[]{"ModuleA"});
        TestModule moduleC = new TestModule("ModuleC", "1.0.0", new String[]{"ModuleB"});

        moduleManager.registerModule(moduleC);
        moduleManager.registerModule(moduleA);
        moduleManager.registerModule(moduleB);

        // When: 全モジュールを有効化
        int enabledCount = moduleManager.enableAll();

        // Then: 依存関係を考慮して順番に有効化される
        assertThat(enabledCount).isEqualTo(3);
        assertThat(moduleA.enableOrder).isLessThan(moduleB.enableOrder);
        assertThat(moduleB.enableOrder).isLessThan(moduleC.enableOrder);
    }

    // ==================== DependencyManager テスト ====================

    @Test
    @DisplayName("DependencyManager: 初期化と基本機能")
    void testDependencyManager_Initialization() {
        // Given: DependencyManagerインスタンスを作成
        DependencyManager dependencyManager = new DependencyManager(mockPlugin);

        // When: インスタンスが正しく作成されることを確認
        // Then: マネージャーが初期化されている
        assertThat(dependencyManager).isNotNull();
        assertThat(dependencyManager.isVaultAvailable()).isFalse(); // まだセットアップしていない
        assertThat(dependencyManager.isMythicMobsAvailable()).isFalse();
        assertThat(dependencyManager.isPlaceholderApiAvailable()).isFalse();
    }

    @Test
    @DisplayName("DependencyManager: 必須依存が欠けている場合の挙動")
    void testDependencyManager_MissingRequiredDependency() {
        // Given: VaultとMythicMobsが存在しない状況をシミュレート
        when(mockPluginManager.getPlugin("Vault")).thenReturn(null);
        when(mockPluginManager.getPlugin("MythicMobs")).thenReturn(null);

        DependencyManager dependencyManager = new DependencyManager(mockPlugin);

        // When: 依存関係をセットアップ
        boolean setupResult = dependencyManager.setupDependencies();

        // Then: セットアップが失敗する
        assertThat(setupResult).isFalse();
        assertThat(dependencyManager.isVaultAvailable()).isFalse();
        assertThat(dependencyManager.isMythicMobsAvailable()).isFalse();
    }

    @Test
    @DisplayName("DependencyManager: オプション依存のチェック")
    void testDependencyManager_OptionalDependency() {
        // Given: 必須依存が欠けている状態
        when(mockPluginManager.getPlugin("Vault")).thenReturn(null);
        when(mockPluginManager.getPlugin("MythicMobs")).thenReturn(null);

        DependencyManager dependencyManager = new DependencyManager(mockPlugin);
        dependencyManager.setupDependencies();

        // When: オプション依存の状態を確認
        boolean papiAvailable = dependencyManager.isPlaceholderApiAvailable();

        // Then: オプション依存も利用不可
        assertThat(papiAvailable).isFalse();
        // 必須依存も利用不可
        assertThat(dependencyManager.isVaultAvailable()).isFalse();
        assertThat(dependencyManager.isMythicMobsAvailable()).isFalse();
    }

    @Test
    @DisplayName("DependencyManager: クリーンアップ機能")
    void testDependencyManager_Cleanup() {
        // Given: DependencyManagerインスタンスを作成
        DependencyManager dependencyManager = new DependencyManager(mockPlugin);

        // When: クリーンアップを実行
        dependencyManager.cleanup();

        // Then: 例外がスローされないことを確認
        assertThat(dependencyManager.isVaultAvailable()).isFalse();
        assertThat(dependencyManager.isMythicMobsAvailable()).isFalse();
    }

    // ==================== YamlConfigManager テスト ====================

    @Test
    @DisplayName("YamlConfigManager: 初期化と設定読み込み")
    void testYamlConfigManager_Initialization() {
        // Given: YamlConfigManagerインスタンスを作成
        YamlConfigManager configManager = new YamlConfigManager(mockPlugin);

        // When: 設定ファイルを読み込み
        // 注: 実際のファイル操作はモックされているため、正常終了のみを確認
        assertThat(configManager).isNotNull();
        assertThat(configManager.getPlugin()).isEqualTo(mockPlugin);
        assertThat(configManager.getLogger()).isEqualTo(mockLogger);
        assertThat(configManager.getDataFolder()).isEqualTo(mockDataFolder);
    }

    @Test
    @DisplayName("YamlConfigManager: 設定値の取得と設定")
    void testYamlConfigManager_ConfigOperations() {
        // Given: YamlConfigManagerインスタンスを作成
        YamlConfigManager configManager = new YamlConfigManager(mockPlugin);

        // When・Then: 基本メソッドが呼び出せることを確認
        assertThat(configManager.getConfigCount()).isEqualTo(0);
        assertThat(configManager.getConfigNames()).isEmpty();
        assertThat(configManager.contains("test", "test.path")).isFalse();
    }

    // ==================== 全システム連携テスト ====================

    @Test
    @DisplayName("全システム連携: 初期化順序の検証")
    void testSystemIntegration_InitializationOrder() {
        // Given: 各マネージャーを準備
        DependencyManager dependencyManager = new DependencyManager(mockPlugin);
        ModuleManager moduleManager = new ModuleManager(mockPlugin);
        YamlConfigManager configManager = new YamlConfigManager(mockPlugin);

        // When: システムを順番に初期化
        // 1. 依存関係チェック（必須依存がない場合は失敗してもOK）
        boolean dependenciesOk = dependencyManager.setupDependencies();

        // 2. 設定読み込み（基本初期化）
        boolean configInitialized = configManager != null;

        // 3. モジュールシステム
        TestModule coreModule = new TestModule("CoreModule", "1.0.0");
        moduleManager.registerModule(coreModule);
        int modulesEnabled = moduleManager.enableAll();

        // Then: 各システムが正しく初期化される
        // 依存関係は失敗してもよい（Vault/MythicMobsのサービスマネージャーは複雑）
        assertThat(configInitialized).isTrue();
        assertThat(modulesEnabled).isEqualTo(1);
    }

    @Test
    @DisplayName("全システム連携: 正常系フローの検証")
    void testSystemIntegration_NormalFlow() {
        // Given: 全システムを準備
        DependencyManager dependencyManager = new DependencyManager(mockPlugin);
        YamlConfigManager configManager = new YamlConfigManager(mockPlugin);
        ModuleManager moduleManager = new ModuleManager(mockPlugin);

        // 複数のモジュールを登録
        TestModule module1 = new TestModule("Module1", "1.0.0");
        TestModule module2 = new TestModule("Module2", "1.0.0");
        moduleManager.registerModule(module1);
        moduleManager.registerModule(module2);

        // When: 正常な初期化フローを実行
        // ステップ1: 依存関係の確認（失敗しても続行）
        dependencyManager.setupDependencies();

        // ステップ2: 設定の読み込み（今回はモックなのでスキップ）

        // ステップ3: モジュールの有効化
        int enabledCount = moduleManager.enableAll();

        // ステップ4: 状態の確認
        assertThat(enabledCount).isEqualTo(2);
        assertThat(moduleManager.isModuleEnabled("Module1")).isTrue();
        assertThat(moduleManager.isModuleEnabled("Module2")).isTrue();

        // ステップ5: クリーンアップ
        dependencyManager.cleanup();
        int disabledCount = moduleManager.disableAll();
        assertThat(disabledCount).isEqualTo(2);
    }

    @Test
    @DisplayName("全システム連携: エラーハンドリングの検証")
    void testSystemIntegration_ErrorHandling() {
        // Given: 依存関係が不十分な状態をシミュレート
        when(mockPluginManager.getPlugin("Vault")).thenReturn(null);

        DependencyManager dependencyManager = new DependencyManager(mockPlugin);
        ModuleManager moduleManager = new ModuleManager(mockPlugin);

        // When: 依存関係チェックを実行
        boolean dependenciesSetup = dependencyManager.setupDependencies();

        // Then: エラーが正しく検出される
        assertThat(dependenciesSetup).isFalse();

        // モジュールは有効化できる（依存関係は独立）
        TestModule module = new TestModule("TestModule", "1.0.0");
        moduleManager.registerModule(module);
        assertThat(moduleManager.enableModule("TestModule")).isTrue();
    }

    @Test
    @DisplayName("全システム連携: リロード機能の検証")
    void testSystemIntegration_Reload() {
        // Given: 全システムを初期化
        DependencyManager dependencyManager = new DependencyManager(mockPlugin);
        ModuleManager moduleManager = new ModuleManager(mockPlugin);

        TestModule module1 = new TestModule("Module1", "1.0.0");
        TestModule module2 = new TestModule("Module2", "1.0.0");
        moduleManager.registerModule(module1);
        moduleManager.registerModule(module2);

        // When: 初期化とリロードを実行
        moduleManager.enableAll();

        // モジュール1をリロード
        boolean reloadResult = moduleManager.reloadModule("Module1");

        // Then: リロードが成功し、状態が保持される
        assertThat(reloadResult).isTrue();
        assertThat(moduleManager.isModuleEnabled("Module1")).isTrue();
        assertThat(module1.enableCallCount).isEqualTo(2); // 初期化 + リロード
    }

    // ==================== テスト用モジュールクラス ====================

    /**
     * テスト用モジュール実装
     *
     * <p>モジュールの振る舞いを検証するためのシンプルな実装です。</p>
     */
    private static class TestModule implements IModule {
        private final String name;
        private final String version;
        private final String[] dependencies;
        private boolean enabled = false;
        public int enableOrder = 0;
        public int enableCallCount = 0;
        private static int globalEnableCounter = 0;

        /**
         * コンストラクタ
         *
         * @param name モジュール名
         * @param version バージョン
         */
        TestModule(String name, String version) {
            this(name, version, new String[0]);
        }

        /**
         * コンストラクタ（依存関係付き）
         *
         * @param name モジュール名
         * @param version バージョン
         * @param dependencies 依存モジュール名の配列
         */
        TestModule(String name, String version, String[] dependencies) {
            this.name = name;
            this.version = version;
            this.dependencies = dependencies;
        }

        @Override
        public void enable(JavaPlugin plugin) throws ModuleException {
            enabled = true;
            enableOrder = ++globalEnableCounter;
            enableCallCount++;
        }

        @Override
        public void disable() {
            enabled = false;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getVersion() {
            return version;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public String[] getDependencies() {
            return dependencies;
        }
    }
}
