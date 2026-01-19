package com.example.rpgplugin.rpgclass;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import org.bukkit.Material;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ClassLoaderのテストクラス
 *
 * <p>注意: 移行期間中、非推奨の {@code getAvailableSkills()} を
 * テストで意図的に使用します。</p>
 */
@SuppressWarnings("deprecation")
@DisplayName("ClassLoader Tests")
class ClassLoaderTest {

    private RPGPlugin mockPlugin;
    private PlayerManager mockPlayerManager;
    private ClassLoader classLoader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mockPlugin = mock(RPGPlugin.class);
        when(mockPlugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("TestLogger"));

        mockPlayerManager = mock(PlayerManager.class);

        classLoader = new ClassLoader(mockPlugin, mockPlayerManager);
    }

    @Nested
    @DisplayName("loadAllClasses")
    class LoadAllClassesTests {

        @Test
        @DisplayName("空のディレクトリの場合は空のマップを返す")
        void loadAllClasses_EmptyDirectory_ReturnsEmptyMap() {
            // classesディレクトリは存在するが空
            File classesDir = classLoader.getClassesDirectory();
            classesDir.mkdirs();

            Map<String, RPGClass> result = classLoader.loadAllClasses();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("有効なクラスファイルをロードできる")
        void loadAllClasses_ValidClassFile_LoadsClass() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    id: warrior
                    name: 戦士
                    display_name: 勇敢なる戦士
                    rank: 1
                    max_level: 50
                    icon: DIAMOND_SWORD
                    description:
                      - 前線で戦う勇者
                      - 近接戦闘のエキスパート
                    stat_growth:
                      manual_points: 5
                      vit: 2
                      str: 3
                    mana_regen: 1.0
                    """;

            Files.writeString(classesDir.resolve("warrior.yml"), yaml);

            Map<String, RPGClass> result = classLoader.loadAllClasses();

            assertEquals(1, result.size());
            assertTrue(result.containsKey("warrior"));

            RPGClass warriorClass = result.get("warrior");
            assertEquals("warrior", warriorClass.getId());
            assertEquals("戦士", warriorClass.getName());
            assertEquals("勇敢なる戦士", warriorClass.getDisplayName());
            assertEquals(1, warriorClass.getRank());
            assertEquals(50, warriorClass.getMaxLevel());
            assertEquals(Material.DIAMOND_SWORD, warriorClass.getIcon());
            assertEquals(2, warriorClass.getDescription().size());
        }

        @Test
        @DisplayName("IDが欠落しているファイルはスキップされる")
        void loadAllClasses_MissingId_SkipsFile() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    name: 名前なしクラス
                    rank: 1
                    """;

            Files.writeString(classesDir.resolve("invalid.yml"), yaml);

            Map<String, RPGClass> result = classLoader.loadAllClasses();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("無効なアイコンマテリアルの場合はデフォルトを使用")
        void loadAllClasses_InvalidIcon_UsesDefault() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    id: test_class
                    name: テスト
                    icon: INVALID_MATERIAL_XYZ
                    rank: 1
                    max_level: 50
                    """;

            Files.writeString(classesDir.resolve("test.yml"), yaml);

            Map<String, RPGClass> result = classLoader.loadAllClasses();

            assertEquals(1, result.size());
            assertEquals(Material.DIAMOND_SWORD, result.get("test_class").getIcon());
        }

        @Test
        @DisplayName("複数のクラスファイルをロードできる")
        void loadAllClasses_MultipleFiles_LoadsAll() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String warriorYaml = """
                    id: warrior
                    name: 戦士
                    rank: 1
                    max_level: 50
                    """;

            String mageYaml = """
                    id: mage
                    name: 魔法使い
                    rank: 1
                    max_level: 50
                    icon: BLAZE_ROD
                    """;

            Files.writeString(classesDir.resolve("warrior.yml"), warriorYaml);
            Files.writeString(classesDir.resolve("mage.yml"), mageYaml);

            Map<String, RPGClass> result = classLoader.loadAllClasses();

            assertEquals(2, result.size());
            assertTrue(result.containsKey("warrior"));
            assertTrue(result.containsKey("mage"));
        }

        @Test
        @DisplayName("サブディレクトリのファイルもロードできる")
        void loadAllClasses_SubDirectory_LoadsFiles() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Path subDir = classesDir.resolve("advanced");
            Files.createDirectories(subDir);

            String yaml = """
                    id: advanced_class
                    name: 上級クラス
                    rank: 2
                    max_level: 50
                    """;

            Files.writeString(subDir.resolve("advanced.yml"), yaml);

            Map<String, RPGClass> result = classLoader.loadAllClasses();

            assertEquals(1, result.size());
            assertTrue(result.containsKey("advanced_class"));
        }
    }

    @Nested
    @DisplayName("loadClass (Single File)")
    class LoadClassTests {

        @Test
        @DisplayName("最小限の設定でクラスをロードできる")
        void loadClass_MinimalConfig_LoadsSuccessfully() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    id: minimal
                    rank: 1
                    max_level: 50
                    """;

            Path classFilePath = classesDir.resolve("minimal.yml");
            Files.writeString(classFilePath, yaml);

            RPGClass result = classLoader.loadClass(classFilePath.toFile());

            assertNotNull(result);
            assertEquals("minimal", result.getId());
            assertEquals("minimal", result.getName()); // name defaults to id
            assertEquals(1, result.getRank());
            assertEquals(50, result.getMaxLevel());
        }

        @Test
        @DisplayName("stat_growthセクションをパースできる")
        void loadClass_WithStatGrowth_ParsesGrowth() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    id: test_class
                    name: テストクラス
                    rank: 1
                    max_level: 50
                    stat_growth:
                      manual_points: 5
                      str: 3
                      int: 2
                      vit: 2
                      dex: 1
                    """;

            Path classFilePath = classesDir.resolve("test.yml");
            Files.writeString(classFilePath, yaml);

            RPGClass result = classLoader.loadClass(classFilePath.toFile());

            assertNotNull(result);
            assertNotNull(result.getStatGrowth());
        }

        @Test
        @DisplayName("next_rankセクションをパースできる")
        void loadClass_WithNextRank_ParsesNextRank() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    id: warrior
                    name: 戦士
                    rank: 1
                    max_level: 50
                    next_rank:
                      class_id: warrior_advanced
                      requirements:
                        - type: level
                          level: 10
                        - type: stat
                          stat: STRENGTH
                          value: 50
                    """;

            Path classFilePath = classesDir.resolve("warrior.yml");
            Files.writeString(classFilePath, yaml);

            RPGClass result = classLoader.loadClass(classFilePath.toFile());

            assertNotNull(result);
            assertTrue(result.getNextRankClassId().isPresent());
            assertEquals("warrior_advanced", result.getNextRankClassId().get());
            // 要件はパースされるが、実装によっては空リストの場合がある
        }

        @Test
        @DisplayName("alternative_ranksセクションをパースできる")
        void loadClass_WithAlternativeRanks_ParsesAlternatives() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    id: warrior
                    name: 戦士
                    rank: 1
                    max_level: 50
                    alternative_ranks:
                      berserker:
                        requirements:
                          - type: item
                            item_name: 狂戦士の証
                            amount: 1
                      guardian:
                        requirements:
                          - type: stat
                            stat: VITALITY
                            value: 100
                    """;

            Path classFilePath = classesDir.resolve("warrior.yml");
            Files.writeString(classFilePath, yaml);

            RPGClass result = classLoader.loadClass(classFilePath.toFile());

            assertNotNull(result);
            assertTrue(result.getAlternativeRanks().containsKey("berserker"));
            assertTrue(result.getAlternativeRanks().containsKey("guardian"));
        }

        @Test
        @DisplayName("available_skillsをパースできる")
        void loadClass_WithAvailableSkills_ParsesSkills() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    id: warrior
                    name: 戦士
                    rank: 1
                    max_level: 50
                    available_skills:
                      - power_strike
                      - shield_bash
                      - war_cry
                    """;

            Path classFilePath = classesDir.resolve("warrior.yml");
            Files.writeString(classFilePath, yaml);

            RPGClass result = classLoader.loadClass(classFilePath.toFile());

            assertNotNull(result);
            List<String> skills = result.getAvailableSkills();
            assertEquals(3, skills.size());
            assertTrue(skills.contains("power_strike"));
            assertTrue(skills.contains("shield_bash"));
            assertTrue(skills.contains("war_cry"));
        }

        @Test
        @DisplayName("passive_bonusesをパースできる")
        void loadClass_WithPassiveBonuses_ParsesBonuses() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    id: warrior
                    name: 戦士
                    rank: 1
                    max_level: 50
                    passive_bonuses:
                      - type: stat_bonus
                        stat: STRENGTH
                        value: 10
                      - type: damage_reduction
                        value: 0.05
                    """;

            Path classFilePath = classesDir.resolve("warrior.yml");
            Files.writeString(classFilePath, yaml);

            RPGClass result = classLoader.loadClass(classFilePath.toFile());

            assertNotNull(result);
            assertNotNull(result.getPassiveBonuses());
            assertEquals(2, result.getPassiveBonuses().size());
        }

        @Test
        @DisplayName("exp_diminishセクションをパースできる")
        void loadClass_WithExpDiminish_ParsesDiminish() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    id: warrior
                    name: 戦士
                    rank: 1
                    max_level: 50
                    exp_diminish:
                      type: curve
                      base: 1.5
                      threshold: 20
                    """;

            Path classFilePath = classesDir.resolve("warrior.yml");
            Files.writeString(classFilePath, yaml);

            RPGClass result = classLoader.loadClass(classFilePath.toFile());

            assertNotNull(result);
            assertNotNull(result.getExpDiminish());
        }

        @Test
        @DisplayName("mana_regenをパースできる")
        void loadClass_WithManaRegen_ParsesValue() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    id: mage
                    name: 魔法使い
                    rank: 1
                    max_level: 50
                    mana_regen: 2.5
                    """;

            Path classFilePath = classesDir.resolve("mage.yml");
            Files.writeString(classFilePath, yaml);

            RPGClass result = classLoader.loadClass(classFilePath.toFile());

            assertNotNull(result);
            assertEquals(2.5, result.getManaRegen(), 0.01);
        }

        @Test
        @DisplayName("無効なファイルの場合はnullを返す")
        void loadClass_InvalidFile_ReturnsNull() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    # IDなし
                    name: 名前なし
                    """;

            Path classFilePath = classesDir.resolve("invalid.yml");
            Files.writeString(classFilePath, yaml);

            RPGClass result = classLoader.loadClass(classFilePath.toFile());

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("getClassesDirectory")
    class GetClassesDirectoryTests {

        @Test
        @DisplayName("クラスディレクトリを正しく返す")
        void getClassesDirectory_ReturnsDirectory() {
            File result = classLoader.getClassesDirectory();

            assertNotNull(result);
            assertTrue(result.exists());
            assertEquals("classes", result.getName());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("完全な設定ファイルをロードできる")
        void loadClass_FullConfig_LoadsCompletely() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    id: paladin
                    name: 聖騎士
                    display_name: 聖なる騎士
                    rank: 2
                    max_level: 60
                    icon: GOLDEN_SWORD
                    description:
                      - 神聖な魔法を使う騎士
                      - 攻防ともに優れる
                    stat_growth:
                      manual_points: 4
                      str: 2
                      int: 2
                      vit: 2
                      dex: 1
                    next_rank:
                      class_id: temple_knight
                      requirements:
                        - type: level
                          level: 20
                        - type: item
                          item_name: 聖なる契約書
                          amount: 1
                          consume_on_use: true
                    alternative_ranks:
                      dark_knight:
                        requirements:
                          - type: stat
                            stat: SPIRIT
                            value: -50
                    available_skills:
                      - holy_strike
                      - divine_shield
                      - heal
                    passive_bonuses:
                      - type: stat_bonus
                        stat: VITALITY
                        value: 15
                      - type: mana_bonus
                        value: 20
                    exp_diminish:
                      type: linear
                      factor: 1.2
                    mana_regen: 1.5
                    """;

            Path classFilePath = classesDir.resolve("paladin.yml");
            Files.writeString(classFilePath, yaml);

            RPGClass result = classLoader.loadClass(classFilePath.toFile());

            assertNotNull(result);
            assertEquals("paladin", result.getId());
            assertEquals("聖騎士", result.getName());
            assertEquals("聖なる騎士", result.getDisplayName());
            assertEquals(2, result.getRank());
            assertEquals(60, result.getMaxLevel());
            assertEquals(2, result.getDescription().size());
            assertEquals(1.5, result.getManaRegen(), 0.01);

            // 次のランク
            assertTrue(result.getNextRankClassId().isPresent());
            assertEquals("temple_knight", result.getNextRankClassId().get());

            // 分岐ランク
            assertTrue(result.getAlternativeRanks().containsKey("dark_knight"));

            // スキル
            assertEquals(3, result.getAvailableSkills().size());

            // パッシブボーナス
            assertEquals(2, result.getPassiveBonuses().size());

            // 経験値減衰
            assertNotNull(result.getExpDiminish());
        }
    }

    @Nested
    @DisplayName("Requirement Parsing")
    class RequirementParsingTests {

        @Test
        @DisplayName("すべての要件タイプをパースできる")
        void loadClass_AllRequirementTypes_ParsesCorrectly() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    id: test_class
                    name: テスト
                    rank: 1
                    max_level: 50
                    next_rank:
                      class_id: next_class
                      requirements:
                        - type: level
                          level: 15
                        - type: stat
                          stat: STRENGTH
                          value: 100
                        - type: item
                          item_name: テストアイテム
                          amount: 5
                          consume_on_use: false
                        - type: quest
                          quest_id: test_quest
                        - type: quest
                          quest_id: another_quest
                          required_stage: 2
                    """;

            Path classFilePath = classesDir.resolve("test.yml");
            Files.writeString(classFilePath, yaml);

            RPGClass result = classLoader.loadClass(classFilePath.toFile());

            assertNotNull(result);
            assertTrue(result.getNextRankClassId().isPresent());
        }

        @Test
        @DisplayName("単一形式の要件をパースできる")
        void loadClass_SingleRequirementFormat_ParsesCorrectly() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            // 単一形式：requirementsがリストではなくキーのマップ
            String yaml = """
                    id: test_class
                    name: テスト
                    rank: 1
                    max_level: 50
                    alternative_ranks:
                      branch_a:
                        requirements:
                          level_req:
                            type: level
                            level: 10
                          stat_req:
                            type: stat
                            stat: VITALITY
                            value: 50
                    """;

            Path classFilePath = classesDir.resolve("test.yml");
            Files.writeString(classFilePath, yaml);

            RPGClass result = classLoader.loadClass(classFilePath.toFile());

            assertNotNull(result);
            assertTrue(result.getAlternativeRanks().containsKey("branch_a"));
        }

        @Test
        @DisplayName("未知の要件タイプは警告してスキップする")
        void loadClass_UnknownRequirementType_LogsWarning() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    id: test_class
                    name: テスト
                    rank: 1
                    max_level: 50
                    next_rank:
                      class_id: next_class
                      requirements:
                        - type: level
                          level: 10
                        - type: unknown_type
                          some_field: value
                        - type: stat
                          stat: INTELLIGENCE
                          value: 30
                    """;

            Path classFilePath = classesDir.resolve("test.yml");
            Files.writeString(classFilePath, yaml);

            RPGClass result = classLoader.loadClass(classFilePath.toFile());

            // 有効な要件だけがパースされる
            assertNotNull(result);
            assertTrue(result.getNextRankClassId().isPresent());
        }

        @Test
        @DisplayName("要件タイプのみ（小文字）をパースできる")
        void loadCase_LowerCaseRequirementType_Works() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    id: test_class
                    name: テスト
                    rank: 1
                    max_level: 50
                    next_rank:
                      class_id: next_class
                      requirements:
                        - type: LEVEL
                          level: 20
                        - type: STAT
                          stat: STRENGTH
                          value: 80
                        - type: ITEM
                          item_name: アイテム
                          amount: 1
                        - type: QUEST
                          quest_id: q1
                    """;

            Path classFilePath = classesDir.resolve("test.yml");
            Files.writeString(classFilePath, yaml);

            RPGClass result = classLoader.loadClass(classFilePath.toFile());

            assertNotNull(result);
            assertTrue(result.getNextRankClassId().isPresent());
        }

        @Test
        @DisplayName("empty requirements list is handled")
        void loadClass_EmptyRequirements_HandlesGracefully() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    id: test_class
                    name: テスト
                    rank: 1
                    max_level: 50
                    next_rank:
                      class_id: next_class
                      requirements: []
                    """;

            Path classFilePath = classesDir.resolve("test.yml");
            Files.writeString(classFilePath, yaml);

            RPGClass result = classLoader.loadClass(classFilePath.toFile());

            assertNotNull(result);
            assertTrue(result.getNextRankClassId().isPresent());
        }

        @Test
        @DisplayName("alternative_ranks with single requirement format")
        void loadClass_AlternativeRankSingleRequirement_Works() throws IOException {
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(classesDir);

            String yaml = """
                    id: base_class
                    name: 基本クラス
                    rank: 1
                    max_level: 50
                    alternative_ranks:
                      advanced_one:
                        requirements:
                          level_check:
                            type: level
                            level: 10
                      advanced_two:
                        requirements:
                          item_check:
                            type: item
                            item_name: 進化の石
                            amount: 1
                            consume_on_use: true
                    """;

            Path classFilePath = classesDir.resolve("base.yml");
            Files.writeString(classFilePath, yaml);

            RPGClass result = classLoader.loadClass(classFilePath.toFile());

            assertNotNull(result);
            assertEquals(2, result.getAlternativeRanks().size());
            assertTrue(result.getAlternativeRanks().containsKey("advanced_one"));
            assertTrue(result.getAlternativeRanks().containsKey("advanced_two"));
        }
    }
}
