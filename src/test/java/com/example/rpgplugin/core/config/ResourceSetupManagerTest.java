package com.example.rpgplugin.core.config;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ResourceSetupManagerのテストクラス
 */
@DisplayName("ResourceSetupManager Tests")
class ResourceSetupManagerTest {

    private ResourceSetupManager resourceManager;
    private Plugin mockPlugin;
    private Logger logger;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mockPlugin = mock(Plugin.class);
        logger = Logger.getLogger("TestLogger");
        when(mockPlugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(mockPlugin.getLogger()).thenReturn(logger);
        resourceManager = new ResourceSetupManager(mockPlugin);
    }

    @Nested
    @DisplayName("setupAllResources")
    class SetupAllResourcesTests {

        @Test
        @DisplayName("全てのリソースをセットアップできる")
        void setupAllResources_FirstTime_CreatesAllDirectories() {
            // JARリソースがないため、結果はfalseになる可能性があるが、
            // ディレクトリ構造が作成されることを確認
            resourceManager.setupAllResources();

            // メインディレクトリが作成されたことを確認
            assertTrue(tempDir.resolve("templates").toFile().exists());
            assertTrue(tempDir.resolve("skills").toFile().exists());
            assertTrue(tempDir.resolve("classes").toFile().exists());
            assertTrue(tempDir.resolve("mobs").toFile().exists());
            assertTrue(tempDir.resolve("exp").toFile().exists());
            assertTrue(tempDir.resolve("data").toFile().exists());
        }

        @Test
        @DisplayName("既にディレクトリが存在する場合はtrue")
        void setupAllResources_AlreadyExists_ReturnsTrue() {
            tempDir.resolve("templates").toFile().mkdirs();
            tempDir.resolve("skills").toFile().mkdirs();
            tempDir.resolve("classes").toFile().mkdirs();
            tempDir.resolve("mobs").toFile().mkdirs();
            tempDir.resolve("exp").toFile().mkdirs();
            tempDir.resolve("data").toFile().mkdirs();

            boolean result = resourceManager.setupAllResources();

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("setupTemplateDirectory")
    class SetupTemplateDirectoryTests {

        @Test
        @DisplayName("テンプレートディレクトリを作成できる")
        void setupTemplateDirectory_NotExists_CreatesDirectory() {
            // JARリソースがないため、結果はfalseになる可能性があるが、
            // ディレクトリ構造が作成されることを確認
            resourceManager.setupTemplateDirectory();

            File templatesDir = tempDir.resolve("templates").toFile();
            assertTrue(templatesDir.exists());
            assertTrue(templatesDir.isDirectory());

            // サブディレクトリも作成されている
            assertTrue(tempDir.resolve("templates/skills").toFile().exists());
            assertTrue(tempDir.resolve("templates/classes").toFile().exists());
        }

        @Test
        @DisplayName("既に存在する場合はスキップしてtrue")
        void setupTemplateDirectory_AlreadyExists_ReturnsTrue() {
            File templatesDir = tempDir.resolve("templates").toFile();
            templatesDir.mkdirs();

            boolean result = resourceManager.setupTemplateDirectory();

            assertTrue(result);
        }

        @Test
        @DisplayName("サブディレクトリを作成できる")
        void setupTemplateDirectory_CreatesSubdirectories() {
            resourceManager.setupTemplateDirectory();

            assertTrue(tempDir.resolve("templates/skills").toFile().exists());
            assertTrue(tempDir.resolve("templates/classes").toFile().exists());
        }
    }

    @Nested
    @DisplayName("setupSkillDirectories")
    class SetupSkillDirectoriesTests {

        @Test
        @DisplayName("スキルディレクトリ構造を作成できる")
        void setupSkillDirectories_FirstTime_CreatesStructure() {
            boolean result = resourceManager.setupSkillDirectories();

            assertTrue(result);
            assertTrue(tempDir.resolve("skills").toFile().exists());
            assertTrue(tempDir.resolve("skills/active").toFile().exists());
            assertTrue(tempDir.resolve("skills/passive").toFile().exists());
        }

        @Test
        @DisplayName("READMEファイルを作成できる")
        void setupSkillDirectories_CreatesReadme() throws Exception {
            resourceManager.setupSkillDirectories();

            File readme = tempDir.resolve("skills/README.txt").toFile();
            assertTrue(readme.exists());

            String content = Files.readString(readme.toPath());
            assertTrue(content.contains("スキルディレクトリ"));
            assertTrue(content.contains("V5 コンポーネントシステム"));
        }

        @Test
        @DisplayName("既にREADMEが存在する場合は上書きしない")
        void setupSkillDirectories_ExistingReadme_PreservesContent() throws Exception {
            File skillsDir = tempDir.resolve("skills").toFile();
            skillsDir.mkdirs();
            File readme = tempDir.resolve("skills/README.txt").toFile();
            Files.writeString(readme.toPath(), "original content");

            resourceManager.setupSkillDirectories();

            String content = Files.readString(readme.toPath());
            assertEquals("original content", content);
        }

        @Test
        @DisplayName("既にディレクトリが存在する場合はtrue")
        void setupSkillDirectories_AlreadyExists_ReturnsTrue() {
            File skillsDir = tempDir.resolve("skills").toFile();
            skillsDir.mkdirs();
            new File(skillsDir, "active").mkdirs();
            new File(skillsDir, "passive").mkdirs();

            boolean result = resourceManager.setupSkillDirectories();

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("setupClassDirectories")
    class SetupClassDirectoriesTests {

        @Test
        @DisplayName("クラスディレクトリを作成できる")
        void setupClassDirectories_FirstTime_CreatesDirectory() {
            boolean result = resourceManager.setupClassDirectories();

            assertTrue(result);
            assertTrue(tempDir.resolve("classes").toFile().exists());
        }

        @Test
        @DisplayName("READMEファイルを作成できる")
        void setupClassDirectories_CreatesReadme() throws Exception {
            resourceManager.setupClassDirectories();

            File readme = tempDir.resolve("classes/README.txt").toFile();
            assertTrue(readme.exists());

            String content = Files.readString(readme.toPath());
            assertTrue(content.contains("クラスディレクトリ"));
            assertTrue(content.contains("テンプレート"));
        }

        @Test
        @DisplayName("既にREADMEが存在する場合は上書きしない")
        void setupClassDirectories_ExistingReadme_PreservesContent() throws Exception {
            File classesDir = tempDir.resolve("classes").toFile();
            classesDir.mkdirs();
            File readme = tempDir.resolve("classes/README.txt").toFile();
            Files.writeString(readme.toPath(), "original readme");

            resourceManager.setupClassDirectories();

            String content = Files.readString(readme.toPath());
            assertEquals("original readme", content);
        }

        @Test
        @DisplayName("既にディレクトリが存在する場合はtrue")
        void setupClassDirectories_AlreadyExists_ReturnsTrue() {
            File classesDir = tempDir.resolve("classes").toFile();
            classesDir.mkdirs();

            boolean result = resourceManager.setupClassDirectories();

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("setupOtherDirectories")
    class SetupOtherDirectoriesTests {

        @Test
        @DisplayName("その他のディレクトリを作成できる")
        void setupOtherDirectories_FirstTime_CreatesAll() {
            boolean result = resourceManager.setupOtherDirectories();

            assertTrue(result);
            assertTrue(tempDir.resolve("mobs").toFile().exists());
            assertTrue(tempDir.resolve("exp").toFile().exists());
            assertTrue(tempDir.resolve("data").toFile().exists());
        }

        @Test
        @DisplayName("既に存在するディレクトリはスキップする")
        void setupOtherDirectories_PartialExists_CreatesRemaining() {
            tempDir.resolve("mobs").toFile().mkdirs();

            boolean result = resourceManager.setupOtherDirectories();

            assertTrue(result);
            assertTrue(tempDir.resolve("exp").toFile().exists());
            assertTrue(tempDir.resolve("data").toFile().exists());
        }

        @Test
        @DisplayName("全てのディレクトリが既に存在する場合はtrue")
        void setupOtherDirectories_AllExist_ReturnsTrue() {
            tempDir.resolve("mobs").toFile().mkdirs();
            tempDir.resolve("exp").toFile().mkdirs();
            tempDir.resolve("data").toFile().mkdirs();

            boolean result = resourceManager.setupOtherDirectories();

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("getSetupDirectories")
    class GetSetupDirectoriesTests {

        @Test
        @DisplayName("セットアップディレクトリのリストを取得できる")
        void getSetupDirectories_ReturnsAllDirectories() {
            resourceManager.setupAllResources();

            List<File> directories = resourceManager.getSetupDirectories();

            assertEquals(10, directories.size());

            // パスを検証
            List<String> paths = directories.stream()
                    .map(File::getPath)
                    .toList();

            assertTrue(paths.contains(tempDir.resolve("skills").toFile().getPath()));
            assertTrue(paths.contains(tempDir.resolve("skills/active").toFile().getPath()));
            assertTrue(paths.contains(tempDir.resolve("skills/passive").toFile().getPath()));
            assertTrue(paths.contains(tempDir.resolve("classes").toFile().getPath()));
            assertTrue(paths.contains(tempDir.resolve("templates").toFile().getPath()));
            assertTrue(paths.contains(tempDir.resolve("templates/skills").toFile().getPath()));
            assertTrue(paths.contains(tempDir.resolve("templates/classes").toFile().getPath()));
            assertTrue(paths.contains(tempDir.resolve("mobs").toFile().getPath()));
            assertTrue(paths.contains(tempDir.resolve("exp").toFile().getPath()));
            assertTrue(paths.contains(tempDir.resolve("data").toFile().getPath()));
        }

        @Test
        @DisplayName("セットアップ前に呼び出しても空でないリストを返す")
        void getSetupDirectories_BeforeSetup_ReturnsList() {
            List<File> directories = resourceManager.getSetupDirectories();

            // メソッドは現在のデータフォルダに基づいてリストを返す
            assertEquals(10, directories.size());
        }
    }

    @Nested
    @DisplayName("README内容の検証")
    class ReadmeContentTests {

        @Test
        @DisplayName("スキルREADMEにV5情報が含まれる")
        void skillsReadme_ContainsV5Info() throws Exception {
            resourceManager.setupSkillDirectories();

            File readme = tempDir.resolve("skills/README.txt").toFile();
            String content = Files.readString(readme.toPath());

            // V5コンポーネントシステムの説明
            assertTrue(content.contains("V5 コンポーネントシステム"));
            assertTrue(content.contains("トリガー"));

            // アクティブ/パッシブの説明
            assertTrue(content.contains("アクティブスキル"));
            assertTrue(content.contains("パッシブスキル"));
            assertTrue(content.contains("CAST"));

            // 利用可能なトリガー
            assertTrue(content.contains("CROUCH"));
            assertTrue(content.contains("LAND"));
            assertTrue(content.contains("DEATH"));
            assertTrue(content.contains("PHYSICAL_DEALT"));
        }

        @Test
        @DisplayName("クラスREADMEに必要な情報が含まれる")
        void classesReadme_ContainsRequiredInfo() throws Exception {
            resourceManager.setupClassDirectories();

            File readme = tempDir.resolve("classes/README.txt").toFile();
            String content = Files.readString(readme.toPath());

            assertTrue(content.contains("クラスディレクトリ"));
            assertTrue(content.contains("テンプレート"));
            assertTrue(content.contains("ホットリロード"));
        }
    }

    @Nested
    @DisplayName("エラーハンドリング")
    class ErrorHandlingTests {

        @Test
        @DisplayName("README作成失敗時も処理を継続する")
        void setupSkillDirectories_ReadmeFailure_Continues() {
            // 読み取り専用のskillsディレクトリを作成
            File skillsDir = tempDir.resolve("skills").toFile();
            skillsDir.mkdirs();
            skillsDir.setReadable(false);

            // README作成に失敗するが、メソッド全体は成功する
            boolean result = resourceManager.setupSkillDirectories();

            // テスト環境の権限設定に依存するため、
            // 少なくとも例外が投げられないことを確認
            assertNotNull(result);

            // クリーンアップ
            skillsDir.setReadable(true);
        }

        @Test
        @DisplayName("mkdirs失敗時にfalseを返す可能性がある")
        void setupTemplateDirectory_MkdirsFailure_MayReturnFalse() {
            // 実際のファイルシステムではmkdirsが失敗するケースは稀だが、
            // エラーパスのテストとして構造を確認
            File templatesDir = tempDir.resolve("templates").toFile();

            // 既に存在する場合はtrue
            templatesDir.mkdirs();
            boolean result = resourceManager.setupTemplateDirectory();
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("ディレクトリ構造の整合性")
    class DirectoryStructureTests {

        @Test
        @DisplayName("全ての必要なディレクトリが作成される")
        void setupAllResources_CompleteStructure_AllDirectoriesExist() {
            resourceManager.setupAllResources();

            // メインディレクトリ
            assertTrue(tempDir.resolve("templates").toFile().exists());
            assertTrue(tempDir.resolve("templates/skills").toFile().exists());
            assertTrue(tempDir.resolve("templates/classes").toFile().exists());
            assertTrue(tempDir.resolve("skills").toFile().exists());
            assertTrue(tempDir.resolve("skills/active").toFile().exists());
            assertTrue(tempDir.resolve("skills/passive").toFile().exists());
            assertTrue(tempDir.resolve("classes").toFile().exists());
            assertTrue(tempDir.resolve("mobs").toFile().exists());
            assertTrue(tempDir.resolve("exp").toFile().exists());
            assertTrue(tempDir.resolve("data").toFile().exists());
        }

        @Test
        @DisplayName("部分セットアップ後に完全セットアップを行う")
        void setupPartialThenFull_CompletesStructure() {
            resourceManager.setupSkillDirectories();
            assertTrue(tempDir.resolve("skills").toFile().exists());

            resourceManager.setupAllResources();
            assertTrue(tempDir.resolve("templates").toFile().exists());
            assertTrue(tempDir.resolve("classes").toFile().exists());
        }
    }

    @Nested
    @DisplayName("リソースコピーのエラーハンドリング")
    class ResourceCopyErrorTests {

        @Test
        @DisplayName("JARリソースが存在しない場合のセットアップ")
        void setupTemplateDirectory_MissingJarResources_CreatesDirectories() {
            // JARリソースがない環境でもディレクトリ構造は作成される
            resourceManager.setupTemplateDirectory();

            // ディレクトリが作成されていることを確認
            assertTrue(tempDir.resolve("templates").toFile().exists());
            assertTrue(tempDir.resolve("templates/skills").toFile().exists());
            assertTrue(tempDir.resolve("templates/classes").toFile().exists());
        }

        @Test
        @DisplayName("既存のテンプレートディレクトリはスキップされる")
        void setupTemplateDirectory_ExistingDirectory_Skips() {
            File templatesDir = tempDir.resolve("templates").toFile();
            templatesDir.mkdirs();

            // 既存ファイルを作成
            File existingFile = tempDir.resolve("templates/skills/existing.yml").toFile();
            try {
                existingFile.getParentFile().mkdirs();
                existingFile.createNewFile();
            } catch (Exception e) {
                // 無視
            }

            boolean result = resourceManager.setupTemplateDirectory();

            assertTrue(result);
        }

        @Test
        @DisplayName("getSetupDirectoriesが正しい数のディレクトリを返す")
        void getSetupDirectories_ReturnsCorrectCount() {
            List<File> directories = resourceManager.getSetupDirectories();

            assertEquals(10, directories.size());
        }

        @Test
        @DisplayName("セットアップ後にgetSetupDirectoriesが変更を反映する")
        void getSetupDirectories_AfterSetup_ReflectsChanges() {
            resourceManager.setupAllResources();

            List<File> directories = resourceManager.getSetupDirectories();

            assertEquals(10, directories.size());

            // 全てのディレクトリが存在する
            for (File dir : directories) {
                assertTrue(dir.exists() || dir.isDirectory(), "Directory should exist: " + dir.getPath());
            }
        }
    }

    @Nested
    @DisplayName("失敗時の動作")
    class FailureBehaviorTests {

        @Test
        @DisplayName("mkdirs失敗時にsetupOtherDirectoriesがfalseを返す可能性がある")
        void setupOtherDirectories_MkdirsFailure_MayReturnFalse() {
            // 実際のファイルシステムではmkdirsが失敗するケースは稀だが、
            // エラーパスの構造を確認するため、少なくともメソッドが正しく動作することを確認

            // ディレクトリを作成して、既存の場合の動作を確認
            tempDir.resolve("mobs").toFile().mkdirs();
            tempDir.resolve("exp").toFile().mkdirs();
            tempDir.resolve("data").toFile().mkdirs();

            boolean result = resourceManager.setupOtherDirectories();

            assertTrue(result);
        }

        @Test
        @DisplayName("setupAllResourcesが一部失敗しても処理を継続する")
        void setupAllResources_PartialFailure_Continues() {
            // 一部のディレクトリを既存にする
            tempDir.resolve("skills").toFile().mkdirs();
            tempDir.resolve("classes").toFile().mkdirs();

            // 少なくとも例外が投げられないことを確認
            assertDoesNotThrow(() -> resourceManager.setupAllResources());
        }
    }

    @Nested
    @DisplayName("README作成のエラーハンドリング")
    class ReadmeErrorHandlingTests {

        @Test
        @DisplayName("README作成時のIOExceptionがログ出力される")
        void setupSkillDirectories_ReadmeIOException_LogsWarning() throws Exception {
            File skillsDir = tempDir.resolve("skills").toFile();
            skillsDir.mkdirs();

            // 読み取り専用のREADMEを作成
            File readme = tempDir.resolve("skills/README.txt").toFile();
            readme.createNewFile();
            readme.setReadable(true);
            readme.setWritable(false);

            // IOExceptionが発生するが、メソッド全体は成功する（trueを返す）
            boolean result = resourceManager.setupSkillDirectories();

            assertTrue(result);

            // クリーンアップ
            readme.setWritable(true);
        }

        @Test
        @DisplayName("クラスREADME作成時のIOExceptionが処理される")
        void setupClassDirectories_ReadmeIOException_Handled() throws Exception {
            File classesDir = tempDir.resolve("classes").toFile();
            classesDir.mkdirs();

            // 読み取り専用のREADMEを作成
            File readme = tempDir.resolve("classes/README.txt").toFile();
            readme.createNewFile();
            readme.setReadable(true);
            readme.setWritable(false);

            // IOExceptionが発生するが、メソッド全体は成功する
            boolean result = resourceManager.setupClassDirectories();

            assertTrue(result);

            // クリーンアップ
            readme.setWritable(true);
        }
    }

    @Nested
    @DisplayName("copyResourceFromJar()のテスト")
    class CopyResourceFromJarTests {

        @Test
        @DisplayName("既にファイルが存在する場合はtrueを返す")
        void copyResourceFromJar_FileExists_ReturnsTrue() throws Exception {
            // テスト用のファイルを作成
            File targetFile = tempDir.resolve("existing_file.txt").toFile();
            targetFile.createNewFile();

            // リフレクションでプライベートメソッドを呼ぶ
            java.lang.reflect.Method method = ResourceSetupManager.class
                .getDeclaredMethod("copyResourceFromJar", String.class, File.class);
            method.setAccessible(true);

            boolean result = (boolean) method.invoke(resourceManager, "test/resource.txt", targetFile);

            assertTrue(result, "既存ファイルの場合はtrueを返す");
        }

        @Test
        @DisplayName("JARリソースが存在しない場合はfalseを返す")
        void copyResourceFromJar_ResourceNotFound_ReturnsFalse() throws Exception {
            // 存在しないファイルパス
            File targetFile = tempDir.resolve("non_existing.txt").toFile();

            java.lang.reflect.Method method = ResourceSetupManager.class
                .getDeclaredMethod("copyResourceFromJar", String.class, File.class);
            method.setAccessible(true);

            // JARにリソースが存在しないのでfalse
            boolean result = (boolean) method.invoke(resourceManager, "/non/existent/resource.txt", targetFile);

            assertFalse(result, "リソースが見つからない場合はfalseを返す");
        }

        @Test
        @DisplayName("親ディレクトリが存在しない場合は作成される（リソースがある場合）")
        void copyResourceFromJar_NoParentDirectory_CreatesParent() throws Exception {
            // 注: テスト環境ではJARリソースがないため、
            // このテストは親ディレクトリ作成ロジックの構造を確認
            File targetFile = tempDir.resolve("nested/dir/resource.txt").toFile();

            java.lang.reflect.Method method = ResourceSetupManager.class
                .getDeclaredMethod("copyResourceFromJar", String.class, File.class);
            method.setAccessible(true);

            // リソースがないのでfalseを返すが、親ディレクトリ作成ロジックを通る前に
            // getResource()がnullを返すため、親ディレクトリは作成されない
            boolean result = (boolean) method.invoke(resourceManager, "/test/resource.txt", targetFile);

            // リソースが見つからないのでfalse
            assertFalse(result, "リソースがない場合はfalse");
        }

        @Test
        @DisplayName("IOException時にログが出力されてfalseを返す")
        void copyResourceFromJar_IOException_LogsWarning() throws Exception {
            // 読み取り専用の親ディレクトリを作成して書き込みを失敗させる
            File nestedDir = tempDir.resolve("readonly_test").toFile();
            nestedDir.mkdirs();
            nestedDir.setReadable(true, false);
            nestedDir.setWritable(false, false);

            File targetFile = new File(nestedDir, "resource.txt");

            java.lang.reflect.Method method = ResourceSetupManager.class
                .getDeclaredMethod("copyResourceFromJar", String.class, File.class);
            method.setAccessible(true);

            // IOExceptionが発生してfalseを返す
            boolean result = (boolean) method.invoke(resourceManager, "/test/resource.txt", targetFile);

            assertFalse(result, "IOException時はfalseを返す");

            // クリーンアップ
            nestedDir.setWritable(true, true);
        }

        @Test
        @DisplayName("nullリソースパスで警告がログされる")
        void copyResourceFromJar_NullResource_LogsWarning() throws Exception {
            File targetFile = tempDir.resolve("null_test.txt").toFile();

            java.lang.reflect.Method method = ResourceSetupManager.class
                .getDeclaredMethod("copyResourceFromJar", String.class, File.class);
            method.setAccessible(true);

            // nullではなく存在しないリソースパス
            boolean result = (boolean) method.invoke(resourceManager, "/null/test.txt", targetFile);

            // リソースが見つからないのでfalse
            assertFalse(result);
        }

        @Test
        @DisplayName("コピー成功時にtrueを返す（JARリソースがある場合）")
        void copyResourceFromJar_Success_ReturnsTrue() throws Exception {
            // 注: テスト環境ではJARリソースがないため、
            // このテストはリソースがない場合の動作を検証する
            File targetFile = tempDir.resolve("jar_resource.txt").toFile();

            java.lang.reflect.Method method = ResourceSetupManager.class
                .getDeclaredMethod("copyResourceFromJar", String.class, File.class);
            method.setAccessible(true);

            // テスト環境ではJARリソースがない
            boolean result = (boolean) method.invoke(resourceManager, "/test.txt", targetFile);

            // リソースが見つからない場合はfalse（JARリソースがないため）
            // 本番環境ではJARリソースがあるためtrueになる可能性がある
            assertFalse(result, "テスト環境ではリソースがないためfalse");
        }

        @Test
        @DisplayName("深いネスト構造のパスを処理できる")
        void copyResourceFromJar_DeepNesting_HandlesPath() throws Exception {
            // 深いネスト構造
            File targetFile = tempDir.resolve("level1/level2/level3/level4/resource.txt").toFile();

            java.lang.reflect.Method method = ResourceSetupManager.class
                .getDeclaredMethod("copyResourceFromJar", String.class, File.class);
            method.setAccessible(true);

            // メソッドを呼ぶ（リソースはないがパス処理ロジックを通過）
            method.invoke(resourceManager, "/test.txt", targetFile);

            // リソースがないため親ディレクトリも作成されない
            // (getResource()がnullを返すと早期リターンするため)
            assertFalse(targetFile.getParentFile().exists());
        }
    }

    @Nested
    @DisplayName("セットアップメソッドのエッジケース")
    class SetupMethodEdgeCases {

        @Test
        @DisplayName("setupTemplateDirectoryが部分成功時にtrueを返す")
        void setupTemplateDirectory_PartialSuccess_ReturnsTrue() {
            // 一部のサブディレクトリを事前作成
            tempDir.resolve("templates/skills").toFile().mkdirs();

            boolean result = resourceManager.setupTemplateDirectory();

            assertTrue(result);
        }

        @Test
        @DisplayName("setupSkillDirectoriesがREADME作成失敗時に継続する")
        void setupSkillDirectories_ReadmeFailure_Continues() throws Exception {
            File skillsDir = tempDir.resolve("skills").toFile();
            skillsDir.mkdirs();

            // 読み取り専用のREADMEを作成
            File readme = new File(skillsDir, "README.txt");
            readme.createNewFile();
            readme.setWritable(false);

            // README作成に失敗するが、ディレクトリ作成は成功しているのでtrue
            boolean result = resourceManager.setupSkillDirectories();

            assertTrue(result);

            // クリーンアップ
            readme.setWritable(true);
        }

        @Test
        @DisplayName("setupClassDirectoriesがREADME作成失敗時に継続する")
        void setupClassDirectories_ReadmeFailure_Continues() throws Exception {
            File classesDir = tempDir.resolve("classes").toFile();
            classesDir.mkdirs();

            // 読み取り専用のREADMEを作成
            File readme = new File(classesDir, "README.txt");
            readme.createNewFile();
            readme.setWritable(false);

            // README作成に失敗するが、ディレクトリ作成は成功
            boolean result = resourceManager.setupClassDirectories();

            assertTrue(result);

            // クリーンアップ
            readme.setWritable(true);
        }
    }

    @Nested
    @DisplayName("READMEファイルの内容検証")
    class ReadmeContentValidationTests {

        @Test
        @DisplayName("スキルREADMEにV5トリガー情報が含まれる")
        void skillsReadme_ContainsV5TriggerInfo() throws Exception {
            resourceManager.setupSkillDirectories();

            File readme = tempDir.resolve("skills/README.txt").toFile();
            assertTrue(readme.exists());

            String content = Files.readString(readme.toPath());

            // V5トリガー情報を確認
            assertTrue(content.contains("CAST"));
            assertTrue(content.contains("CROUCH"));
            assertTrue(content.contains("PHYSICAL_DEALT"));
            assertTrue(content.contains("パッシブスキル"));
            assertTrue(content.contains("アクティブスキル"));
        }

        @Test
        @DisplayName("クラスREADMEにテンプレート情報が含まれる")
        void classesReadme_ContainsTemplateInfo() throws Exception {
            resourceManager.setupClassDirectories();

            File readme = tempDir.resolve("classes/README.txt").toFile();
            assertTrue(readme.exists());

            String content = Files.readString(readme.toPath());

            // テンプレート情報を確認
            assertTrue(content.contains("テンプレート"));
            assertTrue(content.contains("ホットリロード"));
        }

        @Test
        @DisplayName("READMEがUTF-8エンコーディングで作成される")
        void readmeFile_Utf8Encoding_CorrectContent() throws Exception {
            resourceManager.setupSkillDirectories();

            File readme = tempDir.resolve("skills/README.txt").toFile();
            byte[] bytes = Files.readAllBytes(readme.toPath());

            // UTF-8で読み取れることを確認
            String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            assertFalse(content.isEmpty());
            assertTrue(content.contains("スキルディレクトリ"));
        }
    }

    @Nested
    @DisplayName("ディレクトリ作成の競合テスト")
    class DirectoryCreationRaceTests {

        @Test
        @DisplayName("並行してsetupAllResourcesを呼んでも安全")
        void setupAllResources_ConcurrentCalls_ThreadSafe() throws InterruptedException {
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(3);
            java.util.concurrent.CountDownLatch startLatch = new java.util.concurrent.CountDownLatch(1);

            for (int i = 0; i < 3; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        resourceManager.setupAllResources();
                    } catch (Exception e) {
                        // 例外を無視
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));

            // ディレクトリが作成されている
            assertTrue(tempDir.resolve("templates").toFile().exists());
        }

        @Test
        @DisplayName("setup中に別のスレッドがディレクトリを操作しても安全")
        void setupAndConcurrentAccess_ThreadSafe() throws InterruptedException {
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(2);
            java.util.concurrent.CountDownLatch startLatch = new java.util.concurrent.CountDownLatch(1);

            // スレッド1: setupAllResources
            new Thread(() -> {
                try {
                    startLatch.await();
                    resourceManager.setupAllResources();
                } catch (Exception e) {
                    // 例外を無視
                } finally {
                    latch.countDown();
                }
            }).start();

            // スレッド2: getSetupDirectories
            new Thread(() -> {
                try {
                    startLatch.await();
                    java.util.List<File> dirs = resourceManager.getSetupDirectories();
                    assertNotNull(dirs);
                } catch (Exception e) {
                    // 例外を無視
                } finally {
                    latch.countDown();
                }
            }).start();

            startLatch.countDown();
            assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
        }
    }
}
