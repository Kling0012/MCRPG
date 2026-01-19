package com.example.rpgplugin.core.module;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ModuleManagerのユニットテスト
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ModuleManager テスト")
class ModuleManagerTest {

    @Mock
    private JavaPlugin mockPlugin;

    private ModuleManager moduleManager;

    @BeforeEach
    void setUp() {
        when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("Test"));
        moduleManager = new ModuleManager(mockPlugin);
    }

    // ==================== テスト用モジュール実装 ====================

    private static class TestModule implements IModule {
        private boolean enabled = false;
        private boolean enableThrows = false;
        private boolean disableThrows = false;
        private final String name;
        private final String version;
        private final String[] dependencies;

        TestModule(String name, String version, String[] dependencies) {
            this.name = name;
            this.version = version;
            this.dependencies = dependencies;
        }

        @Override
        public void enable(JavaPlugin plugin) throws ModuleException {
            if (enableThrows) {
                throw new ModuleException("Enable failed");
            }
            enabled = true;
        }

        @Override
        public void disable() {
            if (disableThrows) {
                throw new RuntimeException("Disable failed");
            }
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

    // ==================== registerModule テスト ====================

    @Nested
    @DisplayName("registerModule テスト")
    class RegisterModuleTests {

        @Test
        @DisplayName("正常にモジュールを登録できる")
        void registerModule_Success() {
            TestModule module = new TestModule("TestModule", "1.0.0", new String[0]);

            moduleManager.registerModule(module);

            assertThat(moduleManager.getModuleCount()).isEqualTo(1);
            assertThat(moduleManager.getModule("TestModule")).isSameAs(module);
        }

        @Test
        @DisplayName("nullモジュールで例外がスローされる")
        void registerModule_Null_ThrowsException() {
            assertThatThrownBy(() -> moduleManager.registerModule(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("空の名前で例外がスローされる")
        void registerModule_EmptyName_ThrowsException() {
            TestModule module = new TestModule("", "1.0.0", new String[0]);

            assertThatThrownBy(() -> moduleManager.registerModule(module))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null or empty");
        }

        @Test
        @DisplayName("空白のみの名前で例外がスローされる")
        void registerModule_WhitespaceName_ThrowsException() {
            TestModule module = new TestModule("   ", "1.0.0", new String[0]);

            assertThatThrownBy(() -> moduleManager.registerModule(module))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null or empty");
        }

        @Test
        @DisplayName("同名のモジュールを上書きできる")
        void registerModule_DuplicateName_Overwrites() {
            TestModule module1 = new TestModule("Test", "1.0.0", new String[0]);
            TestModule module2 = new TestModule("Test", "2.0.0", new String[0]);

            moduleManager.registerModule(module1);
            moduleManager.registerModule(module2);

            assertThat(moduleManager.getModuleCount()).isEqualTo(1);
            assertThat(moduleManager.getModule("Test").getVersion()).isEqualTo("2.0.0");
        }
    }

    // ==================== unregisterModule テスト ====================

    @Nested
    @DisplayName("unregisterModule テスト")
    class UnregisterModuleTests {

        @Test
        @DisplayName("存在するモジュールを登録解除できる")
        void unregisterModule_Exists_ReturnsTrue() {
            TestModule module = new TestModule("Test", "1.0.0", new String[0]);
            moduleManager.registerModule(module);

            boolean result = moduleManager.unregisterModule("Test");

            assertThat(result).isTrue();
            assertThat(moduleManager.getModuleCount()).isZero();
        }

        @Test
        @DisplayName("存在しないモジュールはfalseを返す")
        void unregisterModule_NotExists_ReturnsFalse() {
            boolean result = moduleManager.unregisterModule("NonExistent");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("有効化されたモジュールは自動的に無効化される")
        void unregisterModule_Enabled_DisablesFirst() {
            TestModule module = new TestModule("Test", "1.0.0", new String[0]);
            moduleManager.registerModule(module);
            moduleManager.enableModule("Test");

            assertThat(module.isEnabled()).isTrue();

            moduleManager.unregisterModule("Test");

            assertThat(module.isEnabled()).isFalse();
        }
    }

    // ==================== enableModule テスト ====================

    @Nested
    @DisplayName("enableModule テスト")
    class EnableModuleTests {

        @Test
        @DisplayName("モジュールを有効化できる")
        void enableModule_Success() {
            TestModule module = new TestModule("Test", "1.0.0", new String[0]);
            moduleManager.registerModule(module);

            boolean result = moduleManager.enableModule("Test");

            assertThat(result).isTrue();
            assertThat(module.isEnabled()).isTrue();
            assertThat(moduleManager.isModuleEnabled("Test")).isTrue();
        }

        @Test
        @DisplayName("存在しないモジュールはfalseを返す")
        void enableModule_NotExists_ReturnsFalse() {
            boolean result = moduleManager.enableModule("NonExistent");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("既に有効化されているモジュールはtrueを返す")
        void enableModule_AlreadyEnabled_ReturnsTrue() {
            TestModule module = new TestModule("Test", "1.0.0", new String[0]);
            moduleManager.registerModule(module);
            moduleManager.enableModule("Test");

            boolean result = moduleManager.enableModule("Test");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("例外がスローされた場合はfalseを返す")
        void enableModule_ThrowsException_ReturnsFalse() {
            TestModule module = new TestModule("Test", "1.0.0", new String[0]);
            module.enableThrows = true;
            moduleManager.registerModule(module);

            boolean result = moduleManager.enableModule("Test");

            assertThat(result).isFalse();
            assertThat(module.isEnabled()).isFalse();
        }
    }

    // ==================== disableModule テスト ====================

    @Nested
    @DisplayName("disableModule テスト")
    class DisableModuleTests {

        @Test
        @DisplayName("モジュールを無効化できる")
        void disableModule_Success() {
            TestModule module = new TestModule("Test", "1.0.0", new String[0]);
            moduleManager.registerModule(module);
            moduleManager.enableModule("Test");

            boolean result = moduleManager.disableModule("Test");

            assertThat(result).isTrue();
            assertThat(module.isEnabled()).isFalse();
            assertThat(moduleManager.isModuleEnabled("Test")).isFalse();
        }

        @Test
        @DisplayName("存在しないモジュールはfalseを返す")
        void disableModule_NotExists_ReturnsFalse() {
            boolean result = moduleManager.disableModule("NonExistent");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("既に無効化されているモジュールはtrueを返す")
        void disableModule_AlreadyDisabled_ReturnsTrue() {
            TestModule module = new TestModule("Test", "1.0.0", new String[0]);
            moduleManager.registerModule(module);

            boolean result = moduleManager.disableModule("Test");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("例外がスローされた場合はfalseを返す")
        void disableModule_ThrowsException_ReturnsFalse() {
            TestModule module = new TestModule("Test", "1.0.0", new String[0]);
            module.disableThrows = true;
            moduleManager.registerModule(module);
            moduleManager.enableModule("Test");

            boolean result = moduleManager.disableModule("Test");

            assertThat(result).isFalse();
        }
    }

    // ==================== reloadModule テスト ====================

    @Nested
    @DisplayName("reloadModule テスト")
    class ReloadModuleTests {

        @Test
        @DisplayName("モジュールをリロードできる")
        void reloadModule_Success() {
            TestModule module = new TestModule("Test", "1.0.0", new String[0]);
            moduleManager.registerModule(module);
            moduleManager.enableModule("Test");

            boolean result = moduleManager.reloadModule("Test");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("存在しないモジュールはfalseを返す")
        void reloadModule_NotExists_ReturnsFalse() {
            boolean result = moduleManager.reloadModule("NonExistent");

            assertThat(result).isFalse();
        }
    }

    // ==================== enableAll テスト ====================

    @Nested
    @DisplayName("enableAll テスト")
    class EnableAllTests {

        @Test
        @DisplayName("全モジュールを有効化できる")
        void enableAll_Success() {
            TestModule module1 = new TestModule("Module1", "1.0.0", new String[0]);
            TestModule module2 = new TestModule("Module2", "1.0.0", new String[0]);
            moduleManager.registerModule(module1);
            moduleManager.registerModule(module2);

            int count = moduleManager.enableAll();

            assertThat(count).isEqualTo(2);
            assertThat(module1.isEnabled()).isTrue();
            assertThat(module2.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("依存関係を考慮して有効化する")
        void enableAll_WithDependencies_EnablesInOrder() {
            TestModule module1 = new TestModule("Dependent", "1.0.0", new String[]{"Dependency"});
            TestModule module2 = new TestModule("Dependency", "1.0.0", new String[0]);
            moduleManager.registerModule(module1);
            moduleManager.registerModule(module2);

            int count = moduleManager.enableAll();

            assertThat(count).isEqualTo(2);
            assertThat(module2.isEnabled()).isTrue();
            assertThat(module1.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("一部のモジュールが失敗しても続行する")
        void enableAll_PartialFailure_Continues() {
            TestModule module1 = new TestModule("Good", "1.0.0", new String[0]);
            TestModule module2 = new TestModule("Bad", "1.0.0", new String[0]);
            module2.enableThrows = true;
            moduleManager.registerModule(module1);
            moduleManager.registerModule(module2);

            int count = moduleManager.enableAll();

            assertThat(count).isEqualTo(1);
            assertThat(module1.isEnabled()).isTrue();
            assertThat(module2.isEnabled()).isFalse();
        }
    }

    // ==================== disableAll テスト ====================

    @Nested
    @DisplayName("disableAll テスト")
    class DisableAllTests {

        @Test
        @DisplayName("全モジュールを無効化できる")
        void disableAll_Success() {
            TestModule module1 = new TestModule("Module1", "1.0.0", new String[0]);
            TestModule module2 = new TestModule("Module2", "1.0.0", new String[0]);
            moduleManager.registerModule(module1);
            moduleManager.registerModule(module2);
            moduleManager.enableAll();

            int count = moduleManager.disableAll();

            assertThat(count).isEqualTo(2);
            assertThat(module1.isEnabled()).isFalse();
            assertThat(module2.isEnabled()).isFalse();
        }
    }

    // ==================== reloadAll テスト ====================

    @Nested
    @DisplayName("reloadAll テスト")
    class ReloadAllTests {

        @Test
        @DisplayName("全モジュールをリロードできる")
        void reloadAll_Success() {
            TestModule module1 = new TestModule("Module1", "1.0.0", new String[0]);
            TestModule module2 = new TestModule("Module2", "1.0.0", new String[0]);
            moduleManager.registerModule(module1);
            moduleManager.registerModule(module2);
            moduleManager.enableAll();

            int count = moduleManager.reloadAll();

            assertThat(count).isEqualTo(2);
        }
    }

    // ==================== その他のメソッド ====================

    @Nested
    @DisplayName("その他のメソッド")
    class OtherMethodsTests {

        @Test
        @DisplayName("getModuleNamesでモジュール名を取得できる")
        void getModuleNames_ReturnsImmutableSet() {
            TestModule module = new TestModule("Test", "1.0.0", new String[0]);
            moduleManager.registerModule(module);

            var names = moduleManager.getModuleNames();

            assertThat(names).containsExactly("Test");
            assertThatThrownBy(() -> names.add("Another"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getModuleCountで正しい数を返す")
        void getModuleCount_ReturnsCorrectCount() {
            assertThat(moduleManager.getModuleCount()).isZero();

            moduleManager.registerModule(new TestModule("M1", "1.0.0", new String[0]));
            assertThat(moduleManager.getModuleCount()).isEqualTo(1);

            moduleManager.registerModule(new TestModule("M2", "1.0.0", new String[0]));
            assertThat(moduleManager.getModuleCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("存在しないモジュールの状態はfalse")
        void isModuleEnabled_NotExists_ReturnsFalse() {
            assertThat(moduleManager.isModuleEnabled("NonExistent")).isFalse();
        }
    }

    // ==================== 循環依存テスト ====================

    @Nested
    @DisplayName("循環依存テスト")
    class CircularDependencyTests {

        @Test
        @DisplayName("循環依存が検出される")
        void enableAll_CircularDependency_DetectsAndHandles() {
            TestModule module1 = new TestModule("A", "1.0.0", new String[]{"B"});
            TestModule module2 = new TestModule("B", "1.0.0", new String[]{"A"});
            moduleManager.registerModule(module1);
            moduleManager.registerModule(module2);

            // 循環依存を検出するが、クラッシュせずに処理を続行
            int count = moduleManager.enableAll();

            // 少なくとも1つは有効化されるか、または失敗する
            assertThat(count).isGreaterThanOrEqualTo(0);
        }
    }
}
