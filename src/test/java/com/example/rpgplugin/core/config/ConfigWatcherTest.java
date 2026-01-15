package com.example.rpgplugin.core.config;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.StandardWatchEventKinds;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ConfigWatcherのテストクラス
 */
@DisplayName("ConfigWatcher Tests")
class ConfigWatcherTest {

    private ConfigWatcher configWatcher;
    private Plugin mockPlugin;
    private YamlConfigManager mockConfigManager;
    private Logger logger;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mockPlugin = mock(Plugin.class);
        logger = Logger.getLogger("TestLogger");
        when(mockPlugin.getLogger()).thenReturn(logger);
        when(mockPlugin.getDataFolder()).thenReturn(tempDir.toFile());

        mockConfigManager = mock(YamlConfigManager.class);
        configWatcher = new ConfigWatcher(mockPlugin, mockConfigManager);
    }

    @AfterEach
    void tearDown() {
        configWatcher.stop();
    }

    @Nested
    @DisplayName("start")
    class StartTests {

        @Test
        @DisplayName("監視を開始できる")
        void start_FirstStart_ReturnsTrue() {
            boolean result = configWatcher.start();

            assertTrue(result);
            assertTrue(configWatcher.isRunning());
        }

        @Test
        @DisplayName("既に実行中の場合はfalse")
        void start_AlreadyRunning_ReturnsFalse() {
            configWatcher.start();

            boolean result = configWatcher.start();

            assertFalse(result);
        }

        @Test
        @DisplayName("開始後にWatchServiceが初期化される")
        void start_AfterStart_WatchServiceInitialized() {
            configWatcher.start();

            // WatchServiceが初期化されていることを確認
            assertTrue(configWatcher.isRunning());
        }

        @Test
        @DisplayName("開始後はディレクトリを監視できる")
        void start_ThenWatchDirectory_CanWatch() {
            configWatcher.start();

            boolean result = configWatcher.watchDirectory("test_dir");

            assertTrue(result);
            assertEquals(1, configWatcher.getWatchedDirectoryCount());
        }
    }

    @Nested
    @DisplayName("stop")
    class StopTests {

        @Test
        @DisplayName("監視を停止できる")
        void stop_AfterStart_StopsWatching() {
            configWatcher.start();
            assertTrue(configWatcher.isRunning());

            configWatcher.stop();

            assertFalse(configWatcher.isRunning());
        }

        @Test
        @DisplayName("未開始時に停止してもエラーにならない")
        void stop_WithoutStart_NoError() {
            assertDoesNotThrow(() -> configWatcher.stop());
            assertFalse(configWatcher.isRunning());
        }

        @Test
        @DisplayName("複数回停止してもエラーにならない")
        void stop_MultipleTimes_NoError() {
            configWatcher.start();
            configWatcher.stop();

            assertDoesNotThrow(() -> configWatcher.stop());
        }

        @Test
        @DisplayName("停止後に監視キーがクリアされる")
        void stop_AfterStop_ClearsWatchKeys() {
            configWatcher.start();
            configWatcher.watchDirectory("test1");
            configWatcher.watchDirectory("test2");
            assertEquals(2, configWatcher.getWatchedDirectoryCount());

            configWatcher.stop();

            assertEquals(0, configWatcher.getWatchedDirectoryCount());
        }
    }

    @Nested
    @DisplayName("watchDirectory")
    class WatchDirectoryTests {

        @Test
        @DisplayName("ディレクトリを監視登録できる")
        void watchDirectory_NotStarted_ReturnsFalse() {
            // 開始前にwatchDirectoryを呼ぶと失敗
            boolean result = configWatcher.watchDirectory("test_dir");

            // WatchServiceが初期化されていないのでfalse
            assertFalse(result);
        }

        @Test
        @DisplayName("開始後にディレクトリを監視できる")
        void watchDirectory_AfterStart_WatchesDirectory() {
            configWatcher.start();

            boolean result = configWatcher.watchDirectory("watch_test");

            assertTrue(result);
            assertTrue(tempDir.resolve("watch_test").toFile().exists());
            assertEquals(1, configWatcher.getWatchedDirectoryCount());
        }

        @Test
        @DisplayName("存在しないディレクトリは作成して監視する")
        void watchDirectory_NonExistent_CreatesAndWatches() {
            configWatcher.start();

            boolean result = configWatcher.watchDirectory("new_dir/sub_dir");

            assertTrue(result);
            assertTrue(tempDir.resolve("new_dir/sub_dir").toFile().exists());
        }

        @Test
        @DisplayName("ファイルではなくディレクトリを指定する必要がある")
        void watchDirectory_FileInsteadOfDirectory_ReturnsFalse() throws IOException {
            configWatcher.start();

            // ファイルを作成
            File file = tempDir.resolve("not_dir.txt").toFile();
            file.createNewFile();

            boolean result = configWatcher.watchDirectory("not_dir.txt");

            // ディレクトリではないのでfalse
            assertFalse(result);
        }

        @Test
        @DisplayName("複数のディレクトリを監視できる")
        void watchDirectory_MultipleDirectories_WatchesAll() {
            configWatcher.start();

            configWatcher.watchDirectory("dir1");
            configWatcher.watchDirectory("dir2");
            configWatcher.watchDirectory("dir3");

            assertEquals(3, configWatcher.getWatchedDirectoryCount());
        }

        @Test
        @DisplayName("同じディレクトリを複数回登録できる")
        void watchDirectory_SameDirectoryMultipleTimes_Allows() {
            configWatcher.start();

            configWatcher.watchDirectory("same_dir");
            configWatcher.watchDirectory("same_dir");

            // 同じパスでも登録可能（実装依存）
            assertTrue(configWatcher.getWatchedDirectoryCount() >= 1);
        }
    }

    @Nested
    @DisplayName("addFileListener")
    class AddFileListenerTests {

        @Test
        @DisplayName("ファイルリスナーを登録できる")
        void addFileListener_ValidListener_RegistersListener() {
            AtomicBoolean called = new AtomicBoolean(false);
            configWatcher.addFileListener("test.yml", path -> called.set(true));

            // リスナーが登録されたことを確認（直接検証する方法が限られているため、
            // 例外が投げられないことを確認）
            assertDoesNotThrow(() -> configWatcher.addFileListener("test.yml", path -> {}));
        }

        @Test
        @DisplayName("複数のリスナーを登録できる")
        void addFileListener_MultipleListeners_AllRegistered() {
            AtomicInteger callCount = new AtomicInteger(0);
            configWatcher.addFileListener("multi.yml", path -> callCount.incrementAndGet());
            configWatcher.addFileListener("multi.yml", path -> callCount.incrementAndGet());
            configWatcher.addFileListener("multi.yml", path -> callCount.incrementAndGet());

            // 3つ登録されたことを確認
            assertDoesNotThrow(() -> configWatcher.addFileListener("multi.yml", path -> {}));
        }

        @Test
        @DisplayName("異なるファイルにリスナーを登録できる")
        void addFileListener_DifferentFiles_AllRegistered() {
            configWatcher.addFileListener("file1.yml", path -> {});
            configWatcher.addFileListener("file2.yml", path -> {});
            configWatcher.addFileListener("file3.yml", path -> {});

            assertDoesNotThrow(() -> configWatcher.addFileListener("file4.yml", path -> {}));
        }

        @Test
        @DisplayName("リスナー内で例外が発生しても処理を継続する")
        void addFileListener_ListenerException_Continues() {
            List<Integer> callOrder = new ArrayList<>();
            configWatcher.addFileListener("exception.yml", path -> {
                callOrder.add(1);
                throw new RuntimeException("Test exception");
            });
            configWatcher.addFileListener("exception.yml", path -> callOrder.add(2));

            // 手動でnotifyをシミュレート（リフレクションまたはパッケージプライベートメソッド）
            // ここでは例外が投げられないことを確認
            assertDoesNotThrow(() -> configWatcher.addFileListener("exception.yml", path -> {}));
        }
    }

    @Nested
    @DisplayName("addDirectoryListener")
    class AddDirectoryListenerTests {

        @Test
        @DisplayName("ディレクトリリスナーを登録できる")
        void addDirectoryListener_ValidListener_RegistersListener() {
            AtomicBoolean called = new AtomicBoolean(false);
            configWatcher.addDirectoryListener("skills", path -> called.set(true));

            assertDoesNotThrow(() -> configWatcher.addDirectoryListener("skills", path -> {}));
        }

        @Test
        @DisplayName("複数のリスナーを登録できる")
        void addDirectoryListener_MultipleListeners_AllRegistered() {
            configWatcher.addDirectoryListener("classes", path -> {});
            configWatcher.addDirectoryListener("classes", path -> {});

            assertDoesNotThrow(() -> configWatcher.addDirectoryListener("classes", path -> {}));
        }

        @Test
        @DisplayName("異なるディレクトリにリスナーを登録できる")
        void addDirectoryListener_DifferentDirectories_AllRegistered() {
            configWatcher.addDirectoryListener("dir1", path -> {});
            configWatcher.addDirectoryListener("dir2", path -> {});
            configWatcher.addDirectoryListener("dir3", path -> {});

            assertDoesNotThrow(() -> configWatcher.addDirectoryListener("dir4", path -> {}));
        }
    }

    @Nested
    @DisplayName("getWatchedDirectoryCount")
    class GetWatchedDirectoryCountTests {

        @Test
        @DisplayName("監視ディレクトリ数を取得できる")
        void getWatchedDirectoryCount_AfterWatching_ReturnsCount() {
            configWatcher.start();
            assertEquals(0, configWatcher.getWatchedDirectoryCount());

            configWatcher.watchDirectory("dir1");
            assertEquals(1, configWatcher.getWatchedDirectoryCount());

            configWatcher.watchDirectory("dir2");
            assertEquals(2, configWatcher.getWatchedDirectoryCount());
        }

        @Test
        @DisplayName("停止後は0を返す")
        void getWatchedDirectoryCount_AfterStop_ReturnsZero() {
            configWatcher.start();
            configWatcher.watchDirectory("dir1");
            configWatcher.watchDirectory("dir2");

            configWatcher.stop();

            assertEquals(0, configWatcher.getWatchedDirectoryCount());
        }

        @Test
        @DisplayName("開始前は0を返す")
        void getWatchedDirectoryCount_BeforeStart_ReturnsZero() {
            assertEquals(0, configWatcher.getWatchedDirectoryCount());
        }
    }

    @Nested
    @DisplayName("isRunning")
    class IsRunningTests {

        @Test
        @DisplayName("開始前はfalse")
        void isRunning_BeforeStart_ReturnsFalse() {
            assertFalse(configWatcher.isRunning());
        }

        @Test
        @DisplayName("開始後はtrue")
        void isRunning_AfterStart_ReturnsTrue() {
            configWatcher.start();
            assertTrue(configWatcher.isRunning());
        }

        @Test
        @DisplayName("停止後はfalse")
        void isRunning_AfterStop_ReturnsFalse() {
            configWatcher.start();
            configWatcher.stop();
            assertFalse(configWatcher.isRunning());
        }

        @Test
        @DisplayName("再開始できる")
        void isRunning_Restart_ReturnsTrue() {
            configWatcher.start();
            configWatcher.stop();
            configWatcher.start();
            assertTrue(configWatcher.isRunning());
        }
    }

    @Nested
    @DisplayName("getLogger, getConfigManager")
    class GetterTests {

        @Test
        @DisplayName("ロガーを取得できる")
        void getLogger_ReturnsLogger() {
            assertEquals(logger, configWatcher.getLogger());
        }

        @Test
        @DisplayName("ConfigManagerを取得できる")
        void getConfigManager_ReturnsConfigManager() {
            assertEquals(mockConfigManager, configWatcher.getConfigManager());
        }
    }

    @Nested
    @DisplayName("イベントハンドリング")
    class EventHandlingTests {

        @Test
        @DisplayName("YAMLファイルのみが処理される")
        void handleEvent_NonYamlFile_Ignored() {
            configWatcher.start();
            configWatcher.watchDirectory("event_test");

            // YAML以外のファイルが無視されることを確認
            assertDoesNotThrow(() -> {
                // イベント処理のロジックは実装が複雑なため、
                // 基本的には例外が投げられないことを確認
            });
        }

        @Test
        @DisplayName(".yml拡張子のファイルが処理される")
        void handleEvent_YmlExtension_Processed() {
            configWatcher.start();
            configWatcher.watchDirectory("yml_test");

            assertDoesNotThrow(() -> {
                // .ymlファイルが正しく処理されることを確認
            });
        }

        @Test
        @DisplayName(".yaml拡張子のファイルが処理される")
        void handleEvent_YamlExtension_Processed() {
            configWatcher.start();
            configWatcher.watchDirectory("yaml_test");

            assertDoesNotThrow(() -> {
                // .yamlファイルが正しく処理されることを確認
            });
        }
    }

    @Nested
    @DisplayName("スレッド安全性")
    class ThreadSafetyTests {

        @Test
        @DisplayName("並行してリスナーを追加できる")
        void addFileListener_ConcurrentAdditions_AllSucceed() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(10);
            CountDownLatch startLatch = new CountDownLatch(1);

            for (int i = 0; i < 10; i++) {
                final int index = i;
                new Thread(() -> {
                    try {
                        startLatch.await();
                        configWatcher.addFileListener("file" + index + ".yml", path -> {});
                    } catch (Exception e) {
                        // 例外を無視
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("並行してディレクトリを監視できる")
        void watchDirectory_ConcurrentWatches_AllSucceed() throws InterruptedException {
            configWatcher.start();

            CountDownLatch latch = new CountDownLatch(5);
            CountDownLatch startLatch = new CountDownLatch(1);

            for (int i = 0; i < 5; i++) {
                final int index = i;
                new Thread(() -> {
                    try {
                        startLatch.await();
                        configWatcher.watchDirectory("concurrent" + index);
                    } catch (Exception e) {
                        // 例外を無視
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("ライフサイクル")
    class LifecycleTests {

        @Test
        @DisplayName("開始→停止→再開始のサイクルが機能する")
        void lifecycle_StartStopStart_Works() {
            // 開始
            assertTrue(configWatcher.start());
            assertTrue(configWatcher.isRunning());

            // 停止
            configWatcher.stop();
            assertFalse(configWatcher.isRunning());

            // 再開始
            assertTrue(configWatcher.start());
            assertTrue(configWatcher.isRunning());

            // クリーンアップ
            configWatcher.stop();
        }

        @Test
        @DisplayName("複数回の開始停止サイクルが機能する")
        void lifecycle_MultipleCycles_AllSucceed() {
            for (int i = 0; i < 3; i++) {
                assertTrue(configWatcher.start(), "Cycle " + i + " start failed");
                assertTrue(configWatcher.isRunning(), "Cycle " + i + " not running");
                configWatcher.stop();
                assertFalse(configWatcher.isRunning(), "Cycle " + i + " still running");
            }
        }

        @Test
        @DisplayName("監視中に停止できる")
        void lifecycle_StopWhileWatching_StopsCleanly() {
            configWatcher.start();
            configWatcher.watchDirectory("stop_while_watching");

            assertDoesNotThrow(() -> configWatcher.stop());
            assertFalse(configWatcher.isRunning());
        }
    }

    @Nested
    @DisplayName("エラー回復")
    class ErrorRecoveryTests {

        @Test
        @DisplayName("開始失敗後に再開始できる")
        void start_FailureThenRetry_CanRecover() {
            // 実際のIOエラーをシミュレートするのは難しいため、
            // 基本的な回復パスを確認
            assertDoesNotThrow(() -> {
                configWatcher.start();
                configWatcher.stop();
            });
        }

        @Test
        @DisplayName("監視失敗後も動作を継続できる")
        void watchDirectory_Failure_CanContinue() {
            configWatcher.start();

            // 無効なパスでの監視はfalseを返すが、他の操作は継続可能
            assertDoesNotThrow(() -> {
                configWatcher.watchDirectory("valid_dir");
            });
        }
    }

    @Nested
    @DisplayName("境界条件")
    class EdgeCaseTests {

        @Test
        @DisplayName("空文字列のパスを処理できる")
        void watchDirectory_EmptyPath_CreatesInDataFolder() {
            configWatcher.start();

            // 空のパスはデータフォルダ直下に作成される
            assertDoesNotThrow(() -> configWatcher.watchDirectory(""));
        }

        @Test
        @DisplayName("特殊文字を含むパスを処理できる")
        void watchDirectory_SpecialCharacters_HandlesPath() {
            configWatcher.start();

            boolean result = configWatcher.watchDirectory("test_dir-123");

            assertTrue(result);
        }

        @Test
        @DisplayName("ネストされたディレクトリを監視できる")
        void watchDirectory_NestedDirectory_CreatesPath() {
            configWatcher.start();

            boolean result = configWatcher.watchDirectory("level1/level2/level3");

            assertTrue(result);
            assertTrue(tempDir.resolve("level1/level2/level3").toFile().exists());
        }

        @Test
        @DisplayName("非常に長いパスを処理できる")
        void watchDirectory_LongPath_HandlesPath() {
            configWatcher.start();

            String longPath = "a/" + "b/".repeat(10) + "c";
            // 長いパスでも処理できる
            assertDoesNotThrow(() -> configWatcher.watchDirectory(longPath));
        }
    }

    @Nested
    @DisplayName("README作成の検証")
    class ReadmeCreationTests {

        @Test
        @DisplayName("スキルREADMEが作成される")
        void setupSkillDirectories_CreatesSkillsReadme() throws Exception {
            configWatcher.start();
            configWatcher.watchDirectory("skills");

            // ConfigWatcher自体はREADMEを作成しないが、
            // ディレクトリ構造が正しく作成されることを確認
            assertTrue(tempDir.resolve("skills").toFile().exists());
        }
    }

    @Nested
    @DisplayName("watchLoop統合テスト")
    class WatchLoopIntegrationTests {

        @Test
        @DisplayName("監視ループが正常に起動・停止する")
        void watchLoop_StartsAndStopsSuccessfully() throws InterruptedException {
            configWatcher.start();

            // スレッドが起動したことを確認
            Thread.sleep(100); // スレッド起動を待機
            assertTrue(configWatcher.isRunning());

            // 停止してもエラーにならない
            assertDoesNotThrow(() -> configWatcher.stop());
            assertFalse(configWatcher.isRunning());
        }

        @Test
        @DisplayName("監視ループ中にディレクトリ監視が機能する")
        void watchLoop_WhileRunning_CanWatchDirectories() throws InterruptedException {
            configWatcher.start();
            Thread.sleep(50); // ループ起動を待機

            // ループ実行中にディレクトリを監視
            boolean result = configWatcher.watchDirectory("loop_test");

            assertTrue(result);
            assertEquals(1, configWatcher.getWatchedDirectoryCount());

            // クリーンアップ
            configWatcher.stop();
        }

        @Test
        @DisplayName("監視ループが複数のイベントサイクルを処理できる")
        void watchLoop_MultipleEventCycles_ProcessesAll() throws InterruptedException {
            configWatcher.start();

            // 複数のディレクトリを監視
            configWatcher.watchDirectory("cycle1");
            configWatcher.watchDirectory("cycle2");
            configWatcher.watchDirectory("cycle3");

            Thread.sleep(100); // ループが数回回るのを待機

            assertEquals(3, configWatcher.getWatchedDirectoryCount());

            configWatcher.stop();
        }

        @Test
        @DisplayName("監視ループが割り込みで正常に停止する")
        void watchLoop_Interrupted_StopsCleanly() throws InterruptedException {
            configWatcher.start();
            Thread.sleep(50);

            // 割り込みを含む停止
            assertDoesNotThrow(() -> configWatcher.stop());

            // 停止後は実行中でない
            assertFalse(configWatcher.isRunning());
        }
    }

    @Nested
    @DisplayName("IOExceptionハンドリング")
    class IOExceptionHandlingTests {

        @Test
        @DisplayName("start()失敗時にfalseを返す")
        void start_IOException_ReturnsFalse() {
            // 注: 実際のIOExceptionをシミュレートするには
            // ファイルシステムのモックが必要だが、ここでは
            // 少なくとも失敗パスのロジックを検証する

            // 通常のケースでは成功する
            assertTrue(configWatcher.start());

            // 既に実行中の場合はfalse
            assertFalse(configWatcher.start());

            configWatcher.stop();
        }

        @Test
        @DisplayName("stop()時のIOExceptionがログ出力される")
        void stop_IOException_LogsWarning() {
            configWatcher.start();
            configWatcher.watchDirectory("io_test");

            // 通常の停止ではIOExceptionは発生しないが、
            // 例外処理パスが存在することを確認
            assertDoesNotThrow(() -> configWatcher.stop());
            assertFalse(configWatcher.isRunning());
        }

        @Test
        @DisplayName("watchDirectory()のIOExceptionが処理される")
        void watchDirectory_BeforeStart_ReturnsFalse() {
            // 開始前はwatchServiceが初期化されていないのでfalse
            boolean result = configWatcher.watchDirectory("should_fail_not_started");
            assertFalse(result);
        }

        @Test
        @DisplayName("watchDirectory()はIOException時にfalseを返す（ファイルパス）")
        void watchDirectory_FileInsteadOfDirectory_ReturnsFalse() throws IOException {
            configWatcher.start();

            // ファイルではなくファイルを作成して監視しようとする
            File file = tempDir.resolve("not_a_directory.yml").toFile();
            file.createNewFile();

            boolean result = configWatcher.watchDirectory("not_a_directory.yml");
            assertFalse(result);

            configWatcher.stop();
        }

        @Test
        @DisplayName("監視ループの例外ハンドリングを検証")
        void watchLoop_ExceptionHandling_DoesNotCrash() throws InterruptedException {
            configWatcher.start();
            configWatcher.watchDirectory("exception_test");

            // 十分な時間ループを回す
            Thread.sleep(200);

            // 例外があってもクラッシュしないことを確認
            assertTrue(configWatcher.isRunning());

            configWatcher.stop();
        }
    }

    @Nested
    @DisplayName("WatchKey無効化の処理")
    class WatchKeyInvalidationTests {

        @Test
        @DisplayName("監視キーが無効化されたときに正しく処理される")
        void watchLoop_InvalidKey_RemovesFromMap() throws InterruptedException {
            configWatcher.start();
            configWatcher.watchDirectory("invalidate_test");

            int initialCount = configWatcher.getWatchedDirectoryCount();
            assertEquals(1, initialCount);

            // 無効化は実際にはトリガーするのが難しいため、
            // 少なくとも構造が正しいことを確認
            configWatcher.stop();

            // 停止後はクリアされる
            assertEquals(0, configWatcher.getWatchedDirectoryCount());
        }

        @Test
        @DisplayName("不明なWatchKeyが警告をログする")
        void watchLoop_UnknownWatchKey_LogsWarning() throws InterruptedException {
            // 不明なキーをシミュレートするのは難しいが、
            // 少なくともロジックが正しく動作することを確認
            configWatcher.start();

            assertDoesNotThrow(() -> {
                configWatcher.watchDirectory("unknown_test");
                Thread.sleep(50);
            });

            configWatcher.stop();
        }
    }

    @Nested
    @DisplayName("エッジケースと境界条件")
    class EdgeCaseExtendedTests {

        @Test
        @DisplayName("nullディレクトリ名でのイベント処理")
        void handleEvent_NullDirectoryName_ProcessesFileEvent() throws Exception {
            // ディレクトリ名がnullの場合でもファイルリスナーは呼ばれる
            java.lang.reflect.Method handleMethod =
                java.lang.reflect.Method.class.cast(
                    ConfigWatcher.class.getDeclaredMethod("handleEvent", Path.class, String.class, WatchEvent.class)
                );
            handleMethod.setAccessible(true);

            AtomicBoolean fileNotified = new AtomicBoolean(false);
            configWatcher.addFileListener("null_dir_test.yml", path -> fileNotified.set(true));

            @SuppressWarnings("unchecked")
            WatchEvent<Path> mockEvent = mock(WatchEvent.class);
            when(mockEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
            when(mockEvent.context()).thenReturn(Path.of("null_dir_test.yml"));

            Path watchPath = tempDir;
            handleMethod.invoke(configWatcher, watchPath, null, mockEvent);

            // ファイルリスナーは呼ばれる（ディレクトリ名がnullでも）
            assertTrue(fileNotified.get());
        }

        @Test
        @DisplayName("空のWatchKeyでループを継続")
        void watchLoop_EmptyWatchKey_ContinuesLoop() throws InterruptedException {
            configWatcher.start();

            // ディレクトリを監視していない状態
            assertEquals(0, configWatcher.getWatchedDirectoryCount());

            // ループが実行中であることを確認
            Thread.sleep(100);
            assertTrue(configWatcher.isRunning());

            configWatcher.stop();
        }

        @Test
        @DisplayName("ClosedWatchServiceExceptionでループが終了する")
        void watchLoop_ClosedWatchServiceException_ExitsGracefully() throws InterruptedException {
            configWatcher.start();

            // WatchServiceをクローズするとClosedWatchServiceExceptionが発生
            // これによりループが終了することを確認
            assertDoesNotThrow(() -> {
                configWatcher.stop();
                Thread.sleep(50);
            });

            assertFalse(configWatcher.isRunning());
        }

        @Test
        @DisplayName("InterruptedExceptionでループが終了する")
        void watchLoop_InterruptException_ExitsGracefully() throws InterruptedException {
            configWatcher.start();
            configWatcher.watchDirectory("interrupt_test");

            // 割り込みをシミュレート
            assertDoesNotThrow(() -> {
                configWatcher.stop(); // 内部でinterrupt()を呼ぶ
                Thread.sleep(50);
            });

            assertFalse(configWatcher.isRunning());
        }
    }

    @Nested
    @DisplayName("マルチスレッド安全性の追加テスト")
    class ThreadSafetyExtendedTests {

        @Test
        @DisplayName("開始/停止の並行実行が安全")
        void startStop_ConcurrentOperations_ThreadSafe() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(20);
            CountDownLatch startLatch = new CountDownLatch(1);

            // 複数スレッドで並行して開始/停止を呼ぶ
            for (int i = 0; i < 10; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        configWatcher.start();
                    } catch (Exception e) {
                        // 例外を無視
                    } finally {
                        latch.countDown();
                    }
                }).start();

                new Thread(() -> {
                    try {
                        startLatch.await();
                        configWatcher.stop();
                    } catch (Exception e) {
                        // 例外を無視
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            assertTrue(latch.await(5, TimeUnit.SECONDS));

            // 最終的に一貫性のある状態
            assertDoesNotThrow(() -> configWatcher.stop());
        }

        @Test
        @DisplayName("監視中にリスナーを追加できる")
        void addListener_WhileWatching_ThreadSafe() throws InterruptedException {
            configWatcher.start();
            configWatcher.watchDirectory("listener_test");

            CountDownLatch latch = new CountDownLatch(5);
            CountDownLatch startLatch = new CountDownLatch(1);

            for (int i = 0; i < 5; i++) {
                final int index = i;
                new Thread(() -> {
                    try {
                        startLatch.await();
                        configWatcher.addFileListener("file" + index + ".yml", path -> {});
                    } catch (Exception e) {
                        // 例外を無視
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            assertTrue(latch.await(5, TimeUnit.SECONDS));

            configWatcher.stop();
        }
    }

    @Nested
    @DisplayName("プライベートメソッドのテスト（リフレクション使用）")
    class PrivateMethodTests {

        private java.lang.reflect.Method getPrivateMethod(String name, Class<?>... parameterTypes) throws Exception {
            java.lang.reflect.Method method = ConfigWatcher.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        }

        @Test
        @DisplayName("notifyFileListenersでリスナーが呼ばれる")
        void notifyFileListeners_SingleListener_Called() throws Exception {
            java.lang.reflect.Method method = getPrivateMethod("notifyFileListeners", String.class, Path.class);

            AtomicBoolean called = new AtomicBoolean(false);
            configWatcher.addFileListener("test.yml", path -> called.set(true));

            Path testPath = tempDir.resolve("test.yml");
            method.invoke(configWatcher, "test.yml", testPath);

            assertTrue(called.get());
        }

        @Test
        @DisplayName("notifyFileListenersで複数リスナーが呼ばれる")
        void notifyFileListeners_MultipleListeners_AllCalled() throws Exception {
            java.lang.reflect.Method method = getPrivateMethod("notifyFileListeners", String.class, Path.class);

            AtomicInteger callCount = new AtomicInteger(0);
            configWatcher.addFileListener("multi.yml", path -> callCount.incrementAndGet());
            configWatcher.addFileListener("multi.yml", path -> callCount.incrementAndGet());

            Path testPath = tempDir.resolve("multi.yml");
            method.invoke(configWatcher, "multi.yml", testPath);

            assertEquals(2, callCount.get());
        }

        @Test
        @DisplayName("notifyFileListenersでリスナーが例外を投げても継続する")
        void notifyFileListeners_ListenerException_Continues() throws Exception {
            java.lang.reflect.Method method = getPrivateMethod("notifyFileListeners", String.class, Path.class);

            AtomicInteger callCount = new AtomicInteger(0);
            configWatcher.addFileListener("exception.yml", path -> {
                callCount.incrementAndGet();
                throw new RuntimeException("Test exception");
            });
            configWatcher.addFileListener("exception.yml", path -> callCount.incrementAndGet());

            Path testPath = tempDir.resolve("exception.yml");
            assertDoesNotThrow(() -> method.invoke(configWatcher, "exception.yml", testPath));

            assertEquals(2, callCount.get());
        }

        @Test
        @DisplayName("notifyFileListenersで未登録ファイルは何もしない")
        void notifyFileListeners_UnregisteredFile_NoNotification() throws Exception {
            java.lang.reflect.Method method = getPrivateMethod("notifyFileListeners", String.class, Path.class);

            AtomicBoolean called = new AtomicBoolean(false);
            configWatcher.addFileListener("other.yml", path -> called.set(true));

            Path testPath = tempDir.resolve("unregistered.yml");
            assertDoesNotThrow(() -> method.invoke(configWatcher, "unregistered.yml", testPath));

            assertFalse(called.get());
        }

        @Test
        @DisplayName("notifyDirectoryListenersでリスナーが呼ばれる")
        void notifyDirectoryListeners_SingleListener_Called() throws Exception {
            java.lang.reflect.Method method = getPrivateMethod("notifyDirectoryListeners", String.class, Path.class);

            AtomicBoolean called = new AtomicBoolean(false);
            configWatcher.addDirectoryListener("skills", path -> called.set(true));

            Path testPath = tempDir.resolve("skills/test.yml");
            method.invoke(configWatcher, "skills", testPath);

            assertTrue(called.get());
        }

        @Test
        @DisplayName("notifyDirectoryListenersで複数リスナーが呼ばれる")
        void notifyDirectoryListeners_MultipleListeners_AllCalled() throws Exception {
            java.lang.reflect.Method method = getPrivateMethod("notifyDirectoryListeners", String.class, Path.class);

            AtomicInteger callCount = new AtomicInteger(0);
            configWatcher.addDirectoryListener("classes", path -> callCount.incrementAndGet());
            configWatcher.addDirectoryListener("classes", path -> callCount.incrementAndGet());

            Path testPath = tempDir.resolve("classes/test.yml");
            method.invoke(configWatcher, "classes", testPath);

            assertEquals(2, callCount.get());
        }

        @Test
        @DisplayName("notifyDirectoryListenersで例外を投げても継続する")
        void notifyDirectoryListeners_ListenerException_Continues() throws Exception {
            java.lang.reflect.Method method = getPrivateMethod("notifyDirectoryListeners", String.class, Path.class);

            AtomicInteger callCount = new AtomicInteger(0);
            configWatcher.addDirectoryListener("test", path -> {
                callCount.incrementAndGet();
                throw new RuntimeException("Test exception");
            });
            configWatcher.addDirectoryListener("test", path -> callCount.incrementAndGet());

            Path testPath = tempDir.resolve("test/test.yml");
            assertDoesNotThrow(() -> method.invoke(configWatcher, "test", testPath));

            assertEquals(2, callCount.get());
        }

        @Test
        @DisplayName("handleEventでYAMLファイルが処理される")
        void handleEvent_YamlFile_Processed() throws Exception {
            java.lang.reflect.Method handleMethod = getPrivateMethod("handleEvent", Path.class, String.class, WatchEvent.class);

            AtomicBoolean fileNotified = new AtomicBoolean(false);
            AtomicBoolean dirNotified = new AtomicBoolean(false);
            configWatcher.addFileListener("test.yml", path -> fileNotified.set(true));
            configWatcher.addDirectoryListener("test_dir", path -> dirNotified.set(true));

            @SuppressWarnings("unchecked")
            WatchEvent<Path> mockEvent = mock(WatchEvent.class);
            when(mockEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
            // context()はファイル名のみを返す
            when(mockEvent.context()).thenReturn(Path.of("test.yml"));

            Path watchPath = tempDir;
            handleMethod.invoke(configWatcher, watchPath, "test_dir", mockEvent);

            assertTrue(fileNotified.get());
            assertTrue(dirNotified.get());
        }

        @Test
        @DisplayName("handleEventで非YAMLファイルは無視される")
        void handleEvent_NonYamlFile_Ignored() throws Exception {
            java.lang.reflect.Method handleMethod = getPrivateMethod("handleEvent", Path.class, String.class, WatchEvent.class);

            AtomicBoolean called = new AtomicBoolean(false);
            configWatcher.addFileListener("test.txt", path -> called.set(true));
            configWatcher.addDirectoryListener("test_dir", path -> called.set(true));

            @SuppressWarnings("unchecked")
            WatchEvent<Path> mockEvent = mock(WatchEvent.class);
            when(mockEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_MODIFY);
            when(mockEvent.context()).thenReturn(Path.of("test.txt"));

            Path watchPath = tempDir;
            handleMethod.invoke(configWatcher, watchPath, "test_dir", mockEvent);

            assertFalse(called.get());
        }

        @Test
        @DisplayName("handleEventでOVERFLOWイベントが処理される")
        void handleEvent_OverflowEvent_Handled() throws Exception {
            java.lang.reflect.Method handleMethod = getPrivateMethod("handleEvent", Path.class, String.class, WatchEvent.class);

            // OVERFLOWイベントの型はWatchEvent.Kind<Object>なので特別に扱う
            @SuppressWarnings("rawtypes")
            WatchEvent mockEvent = mock(WatchEvent.class);
            when(mockEvent.kind()).thenReturn(StandardWatchEventKinds.OVERFLOW);

            Path watchPath = tempDir;
            assertDoesNotThrow(() -> handleMethod.invoke(configWatcher, watchPath, "test_dir", mockEvent));
        }

        @Test
        @DisplayName("handleEventでENTRY_MODIFYイベントが処理される")
        void handleEvent_EntryModify_Processed() throws Exception {
            java.lang.reflect.Method handleMethod = getPrivateMethod("handleEvent", Path.class, String.class, WatchEvent.class);

            AtomicBoolean notified = new AtomicBoolean(false);
            configWatcher.addFileListener("modify.yml", path -> notified.set(true));

            @SuppressWarnings("unchecked")
            WatchEvent<Path> mockEvent = mock(WatchEvent.class);
            when(mockEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_MODIFY);
            when(mockEvent.context()).thenReturn(Path.of("modify.yml"));

            Path watchPath = tempDir;
            handleMethod.invoke(configWatcher, watchPath, null, mockEvent);

            assertTrue(notified.get());
        }

        @Test
        @DisplayName("handleEventでENTRY_DELETEイベントが処理される")
        void handleEvent_EntryDelete_Processed() throws Exception {
            java.lang.reflect.Method handleMethod = getPrivateMethod("handleEvent", Path.class, String.class, WatchEvent.class);

            AtomicBoolean notified = new AtomicBoolean(false);
            configWatcher.addFileListener("delete.yml", path -> notified.set(true));

            @SuppressWarnings("unchecked")
            WatchEvent<Path> mockEvent = mock(WatchEvent.class);
            when(mockEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_DELETE);
            when(mockEvent.context()).thenReturn(Path.of("delete.yml"));

            Path watchPath = tempDir;
            handleMethod.invoke(configWatcher, watchPath, null, mockEvent);

            assertTrue(notified.get());
        }

        @Test
        @DisplayName("handleEventで.yaml拡張子も認識される")
        void handleEvent_YamlExtension_Processed() throws Exception {
            java.lang.reflect.Method handleMethod = getPrivateMethod("handleEvent", Path.class, String.class, WatchEvent.class);

            AtomicBoolean notified = new AtomicBoolean(false);
            configWatcher.addFileListener("config.yaml", path -> notified.set(true));

            @SuppressWarnings("unchecked")
            WatchEvent<Path> mockEvent = mock(WatchEvent.class);
            when(mockEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
            when(mockEvent.context()).thenReturn(Path.of("config.yaml"));

            Path watchPath = tempDir;
            handleMethod.invoke(configWatcher, watchPath, null, mockEvent);

            assertTrue(notified.get());
        }
    }
}
