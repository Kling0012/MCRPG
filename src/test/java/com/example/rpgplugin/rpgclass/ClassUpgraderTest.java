package com.example.rpgplugin.rpgclass;

import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.rpgclass.requirements.ClassRequirement;
import com.example.rpgplugin.rpgclass.requirements.ItemRequirement;
import com.example.rpgplugin.storage.models.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

/**
 * ClassUpgraderのユニットテスト
 *
 * <p>クラスアップシステムの包括的なテストを行います。</p>
 *
 * 設計原則:
 * - SOLID-S: ClassUpgraderのテストに特化
 * - KISS: シンプルなテストケース
 * - 読みやすさ: テスト名で振る舞いを明示
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ClassUpgrader テスト")
class ClassUpgraderTest {

    @Mock
    private ClassManager mockClassManager;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private Player mockPlayer;

    @Mock
    private RPGPlayer mockRpgPlayer;

    @Mock
    private PlayerData mockPlayerData;

    private ClassUpgrader classUpgrader;
    private static UUID playerUuid;
    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeAll
    static void beforeAll() {
        playerUuid = UUID.randomUUID();
    }

    @BeforeEach
    void setUp() {
        // Bukkit静的メソッドのモック
        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::isPrimaryThread).thenReturn(true);

        // ItemFactoryのモック（ItemStack.setItemMetaで必要）
        org.bukkit.inventory.ItemFactory mockItemFactory = mock(org.bukkit.inventory.ItemFactory.class);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(mockItemFactory);
        when(mockItemFactory.isApplicable(any(ItemMeta.class), any(Material.class))).thenReturn(true);

        // Player setup
        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockPlayer.getName()).thenReturn("TestPlayer");

        // PlayerManager setup
        when(mockPlayerManager.getRPGPlayer(any(UUID.class))).thenReturn(mockRpgPlayer);
        when(mockRpgPlayer.getPlayerData()).thenReturn(mockPlayerData);

        classUpgrader = new ClassUpgrader(mockClassManager, mockPlayerManager);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    // ==================== コンストラクタ ====================

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("コンストラクタ: 正常にインスタンス化")
        void constructor_CreatesInstance() {
            ClassUpgrader upgrader = new ClassUpgrader(mockClassManager, mockPlayerManager);

            assertThat(upgrader).isNotNull();
        }

        @Test
        @DisplayName("コンストラクタ: nullのClassManagerも受け入れる")
        void constructor_NullClassManager_AcceptsNull() {
            // コンストラクタはnullチェックを行わない
            ClassUpgrader upgrader = new ClassUpgrader(null, mockPlayerManager);

            assertThat(upgrader).isNotNull();
        }

        @Test
        @DisplayName("コンストラクタ: nullのPlayerManagerも受け入れる")
        void constructor_NullPlayerManager_AcceptsNull() {
            // コンストラクタはnullチェックを行わない
            ClassUpgrader upgrader = new ClassUpgrader(mockClassManager, null);

            assertThat(upgrader).isNotNull();
        }
    }

    // ==================== executeClassUp ====================

    @Nested
    @DisplayName("executeClassUp")
    class ExecuteClassUpTests {

        @Test
        @DisplayName("メインスレッド外で実行: 失敗を返す")
        void executeClassUp_NotMainThread_ReturnsFailure() {
            mockedBukkit.when(Bukkit::isPrimaryThread).thenReturn(false);

            ClassUpgrader.ClassUpResult result = classUpgrader.executeClassUp(mockPlayer, "warrior_advanced");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).contains("メインスレッド");
        }

        @Test
        @DisplayName("要件チェック失敗: 失敗を返す")
        void executeClassUp_RequirementCheckFailed_ReturnsFailure() {
            when(mockClassManager.canUpgradeClass(eq(mockPlayer), anyString()))
                    .thenReturn(new ClassManager.ClassUpResult(false, "レベルが不足しています", List.of("レベル不足")));

            ClassUpgrader.ClassUpResult result = classUpgrader.executeClassUp(mockPlayer, "warrior_advanced");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).contains("レベルが不足しています");
        }

        @Test
        @DisplayName("クラスが見つからない: 失敗を返す")
        void executeClassUp_ClassNotFound_ReturnsFailure() {
            when(mockClassManager.canUpgradeClass(eq(mockPlayer), anyString()))
                    .thenReturn(new ClassManager.ClassUpResult(true, "", new ArrayList<>()));
            when(mockClassManager.getClass(anyString())).thenReturn(Optional.empty());

            ClassUpgrader.ClassUpResult result = classUpgrader.executeClassUp(mockPlayer, "unknown_class");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).contains("見つかりません");
        }

        @Test
        @DisplayName("クラス設定失敗: 失敗を返す")
        void executeClassUp_SetPlayerClassFails_ReturnsFailure() {
            RPGClass targetClass = new RPGClass.Builder("warrior_advanced")
                    .setName("熟練戦士")
                    .setDisplayName("熟練戦士")
                    .setRank(2)
                    .build();

            when(mockClassManager.canUpgradeClass(eq(mockPlayer), anyString()))
                    .thenReturn(new ClassManager.ClassUpResult(true, "", new ArrayList<>()));
            when(mockClassManager.getClass("warrior_advanced")).thenReturn(Optional.of(targetClass));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.empty());
            when(mockClassManager.setPlayerClass(eq(mockPlayer), anyString())).thenReturn(false);

            ClassUpgrader.ClassUpResult result = classUpgrader.executeClassUp(mockPlayer, "warrior_advanced");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).contains("設定に失敗しました");
        }

        @Test
        @DisplayName("成功: 現在クラスなしで直接変更")
        void executeClassUp_NoCurrentClass_Success() {
            RPGClass targetClass = new RPGClass.Builder("warrior_advanced")
                    .setName("熟練戦士")
                    .setDisplayName("熟練戦士")
                    .setRank(2)
                    .build();

            when(mockClassManager.canUpgradeClass(eq(mockPlayer), anyString()))
                    .thenReturn(new ClassManager.ClassUpResult(true, "", new ArrayList<>()));
            when(mockClassManager.getClass("warrior_advanced")).thenReturn(Optional.of(targetClass));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.empty());
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("warrior_advanced"))).thenReturn(true);

            ClassUpgrader.ClassUpResult result = classUpgrader.executeClassUp(mockPlayer, "warrior_advanced");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("熟練戦士");
            assertThat(result.isRankedUp()).isTrue();

            // PlayerDataが更新されたことを確認
            verify(mockPlayerData).setClassId("warrior_advanced");
            verify(mockPlayerData).setClassRank(2);
        }

        @Test
        @DisplayName("直線パターン: 次のランクへアップ")
        void executeClassUp_LinearPattern_NextRank() {
            RPGClass currentClass = new RPGClass.Builder("warrior")
                    .setName("戦士")
                    .setDisplayName("戦士")
                    .setRank(1)
                    .setNextRankClassId("warrior_advanced")
                    .build();

            RPGClass targetClass = new RPGClass.Builder("warrior_advanced")
                    .setName("熟練戦士")
                    .setDisplayName("熟練戦士")
                    .setRank(2)
                    .build();

            when(mockClassManager.canUpgradeClass(eq(mockPlayer), anyString()))
                    .thenReturn(new ClassManager.ClassUpResult(true, "", new ArrayList<>()));
            when(mockClassManager.getClass("warrior_advanced")).thenReturn(Optional.of(targetClass));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.of(currentClass));
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("warrior_advanced"))).thenReturn(true);

            ClassUpgrader.ClassUpResult result = classUpgrader.executeClassUp(mockPlayer, "warrior_advanced");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("熟練戦士");
        }

        @Test
        @DisplayName("直線パターン: 現在クラスと目標が一致しない")
        void executeClassUp_LinearPattern_NotNextRank() {
            RPGClass currentClass = new RPGClass.Builder("warrior")
                    .setName("戦士")
                    .setDisplayName("戦士")
                    .setRank(1)
                    .setNextRankClassId("some_other_class")
                    .build();

            RPGClass targetClass = new RPGClass.Builder("warrior_advanced")
                    .setName("熟練戦士")
                    .setDisplayName("熟練戦士")
                    .setRank(2)
                    .build();

            when(mockClassManager.canUpgradeClass(eq(mockPlayer), anyString()))
                    .thenReturn(new ClassManager.ClassUpResult(true, "", new ArrayList<>()));
            when(mockClassManager.getClass("warrior_advanced")).thenReturn(Optional.of(targetClass));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.of(currentClass));
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("warrior_advanced"))).thenReturn(true);

            ClassUpgrader.ClassUpResult result = classUpgrader.executeClassUp(mockPlayer, "warrior_advanced");

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("分岐パターン: 別ランクへアップ")
        void executeClassUp_BranchPattern_AlternativeRank() {
            List<ClassRequirement> requirements = new ArrayList<>();

            RPGClass currentClass = new RPGClass.Builder("warrior")
                    .setName("戦士")
                    .setDisplayName("戦士")
                    .setRank(1)
                    .addAlternativeRank("berserker", requirements)
                    .build();

            RPGClass targetClass = new RPGClass.Builder("berserker")
                    .setName("バーサーカー")
                    .setDisplayName("バーサーカー")
                    .setRank(2)
                    .build();

            when(mockClassManager.canUpgradeClass(eq(mockPlayer), anyString()))
                    .thenReturn(new ClassManager.ClassUpResult(true, "", new ArrayList<>()));
            when(mockClassManager.getClass("berserker")).thenReturn(Optional.of(targetClass));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.of(currentClass));
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("berserker"))).thenReturn(true);

            ClassUpgrader.ClassUpResult result = classUpgrader.executeClassUp(mockPlayer, "berserker");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("バーサーカー");
        }

        @Test
        @DisplayName("RPGPlayerがnull: 成功（PlayerData更新スキップ）")
        void executeClassUp_RpgPlayerNull_Success() {
            when(mockPlayerManager.getRPGPlayer(any(UUID.class))).thenReturn(null);

            RPGClass targetClass = new RPGClass.Builder("warrior_advanced")
                    .setName("熟練戦士")
                    .setDisplayName("熟練戦士")
                    .setRank(2)
                    .build();

            when(mockClassManager.canUpgradeClass(eq(mockPlayer), anyString()))
                    .thenReturn(new ClassManager.ClassUpResult(true, "", new ArrayList<>()));
            when(mockClassManager.getClass("warrior_advanced")).thenReturn(Optional.of(targetClass));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.empty());
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("warrior_advanced"))).thenReturn(true);

            ClassUpgrader.ClassUpResult result = classUpgrader.executeClassUp(mockPlayer, "warrior_advanced");

            assertThat(result.isSuccess()).isTrue();
            // PlayerData更新はスキップされる
            verify(mockPlayerData, never()).setClassId(anyString());
        }
    }

    // ==================== setInitialClass ====================

    @Nested
    @DisplayName("setInitialClass")
    class SetInitialClassTests {

        @Test
        @DisplayName("クラスが見つからない: 失敗を返す")
        void setInitialClass_ClassNotFound_ReturnsFailure() {
            when(mockClassManager.getClass(anyString())).thenReturn(Optional.empty());

            ClassUpgrader.ClassUpResult result = classUpgrader.setInitialClass(mockPlayer, "unknown");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).contains("見つかりません");
        }

        @Test
        @DisplayName("Rank2以上: 失敗を返す")
        void setInitialClass_NotRank1_ReturnsFailure() {
            RPGClass rank2Class = new RPGClass.Builder("warrior_advanced")
                    .setName("熟練戦士")
                    .setRank(2)
                    .build();

            when(mockClassManager.getClass("warrior_advanced")).thenReturn(Optional.of(rank2Class));

            ClassUpgrader.ClassUpResult result = classUpgrader.setInitialClass(mockPlayer, "warrior_advanced");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).contains("Rank1");
        }

        @Test
        @DisplayName("既にクラス設定済み: 失敗を返す")
        void setInitialClass_AlreadyHasClass_ReturnsFailure() {
            RPGClass rank1Class = new RPGClass.Builder("warrior")
                    .setName("戦士")
                    .setDisplayName("戦士")
                    .setRank(1)
                    .build();

            RPGClass currentClass = new RPGClass.Builder("novice")
                    .setName("初心者")
                    .setDisplayName("初心者")
                    .setRank(1)
                    .build();

            when(mockClassManager.getClass("warrior")).thenReturn(Optional.of(rank1Class));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.of(currentClass));

            ClassUpgrader.ClassUpResult result = classUpgrader.setInitialClass(mockPlayer, "warrior");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).contains("既にクラスが設定されています");
        }

        @Test
        @DisplayName("クラス設定失敗: 失敗を返す")
        void setInitialClass_SetPlayerClassFails_ReturnsFailure() {
            RPGClass rank1Class = new RPGClass.Builder("warrior")
                    .setName("戦士")
                    .setDisplayName("戦士")
                    .setRank(1)
                    .build();

            when(mockClassManager.getClass("warrior")).thenReturn(Optional.of(rank1Class));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.empty());
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("warrior"))).thenReturn(false);

            ClassUpgrader.ClassUpResult result = classUpgrader.setInitialClass(mockPlayer, "warrior");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).contains("設定に失敗しました");
        }

        @Test
        @DisplayName("成功: 初期クラスを設定")
        void setInitialClass_Success_ReturnsSuccess() {
            RPGClass rank1Class = new RPGClass.Builder("warrior")
                    .setName("戦士")
                    .setDisplayName("戦士")
                    .setRank(1)
                    .build();

            when(mockClassManager.getClass("warrior")).thenReturn(Optional.of(rank1Class));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.empty());
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("warrior"))).thenReturn(true);

            ClassUpgrader.ClassUpResult result = classUpgrader.setInitialClass(mockPlayer, "warrior");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("戦士");
            assertThat(result.isRankedUp()).isFalse();

            // PlayerDataが更新されたことを確認
            verify(mockPlayerData).setClassId("warrior");
            verify(mockPlayerData).setClassRank(1);
        }

        @Test
        @DisplayName("RPGPlayerがnull: 成功（PlayerData更新スキップ）")
        void setInitialClass_RpgPlayerNull_Success() {
            when(mockPlayerManager.getRPGPlayer(any(UUID.class))).thenReturn(null);

            RPGClass rank1Class = new RPGClass.Builder("warrior")
                    .setName("戦士")
                    .setDisplayName("戦士")
                    .setRank(1)
                    .build();

            when(mockClassManager.getClass("warrior")).thenReturn(Optional.of(rank1Class));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.empty());
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("warrior"))).thenReturn(true);

            ClassUpgrader.ClassUpResult result = classUpgrader.setInitialClass(mockPlayer, "warrior");

            assertThat(result.isSuccess()).isTrue();
            // PlayerData更新はスキップされる
            verify(mockPlayerData, never()).setClassId(anyString());
        }
    }

    // ==================== ClassUpResult ====================

    @Nested
    @DisplayName("ClassUpResult")
    class ClassUpResultTests {

        @Test
        @DisplayName("成功時の結果を正しく保持する")
        void classUpResult_Success_HoldsValues() {
            ClassUpgrader.ClassUpResult result = new ClassUpgrader.ClassUpResult(true, "成功しました", true);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("成功しました");
            assertThat(result.isRankedUp()).isTrue();
        }

        @Test
        @DisplayName("失敗時の結果を正しく保持する")
        void classUpResult_Failure_HoldsValues() {
            ClassUpgrader.ClassUpResult result = new ClassUpgrader.ClassUpResult(false, "失敗しました", false);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).isEqualTo("失敗しました");
            assertThat(result.isRankedUp()).isFalse();
        }

        @Test
        @DisplayName("成功でもランクアップなしの結果を保持できる")
        void classUpResult_SuccessWithoutRank_HoldsValues() {
            ClassUpgrader.ClassUpResult result = new ClassUpgrader.ClassUpResult(true, "初期クラスを設定", false);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("初期クラスを設定");
            assertThat(result.isRankedUp()).isFalse();
        }

        @Test
        @DisplayName("成功でランクアップありの結果を保持できる")
        void classUpResult_SuccessWithRank_HoldsValues() {
            ClassUpgrader.ClassUpResult result = new ClassUpgrader.ClassUpResult(true, "クラスアップ！", true);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("クラスアップ！");
            assertThat(result.isRankedUp()).isTrue();
        }
    }

    // ==================== Integration Tests ====================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("クラスチェーン: 直線パターンを検証")
        void integration_LinearChain_Verifies() {
            RPGClass baseClass = new RPGClass.Builder("warrior")
                    .setName("戦士")
                    .setRank(1)
                    .setNextRankClassId("warrior_advanced")
                    .build();

            assertThat(baseClass.getNextRankClassId()).isPresent();
            assertThat(baseClass.getNextRankClassId().get()).isEqualTo("warrior_advanced");
        }

        @Test
        @DisplayName("クラスチェーン: 分岐パターンを検証")
        void integration_BranchPattern_Verifies() {
            List<ClassRequirement> reqs = new ArrayList<>();

            RPGClass baseClass = new RPGClass.Builder("warrior")
                    .setName("戦士")
                    .setRank(1)
                    .addAlternativeRank("berserker", reqs)
                    .addAlternativeRank("guardian", reqs)
                    .build();

            assertThat(baseClass.getAlternativeRanks()).hasSize(2);
            assertThat(baseClass.getAlternativeRanks()).containsKey("berserker");
            assertThat(baseClass.getAlternativeRanks()).containsKey("guardian");
        }

        @Test
        @DisplayName("全ランクアップフロー: 初期→直線→分岐")
        void integration_FullFlow_Verifies() {
            // 初期クラス
            RPGClass initialClass = new RPGClass.Builder("warrior")
                    .setName("戦士")
                    .setDisplayName("戦士")
                    .setRank(1)
                    .setNextRankClassId("warrior_advanced")
                    .build();

            // 次のランク
            RPGClass advancedClass = new RPGClass.Builder("warrior_advanced")
                    .setName("熟練戦士")
                    .setDisplayName("熟練戦士")
                    .setRank(2)
                    .addAlternativeRank("berserker", new ArrayList<>())
                    .addAlternativeRank("guardian", new ArrayList<>())
                    .build();

            // 初期クラス設定
            when(mockClassManager.getClass("warrior")).thenReturn(Optional.of(initialClass));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.empty());
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("warrior"))).thenReturn(true);

            ClassUpgrader.ClassUpResult initResult = classUpgrader.setInitialClass(mockPlayer, "warrior");
            assertThat(initResult.isSuccess()).isTrue();

            // 直線パターンでアップ
            when(mockClassManager.getClass("warrior_advanced")).thenReturn(Optional.of(advancedClass));
            when(mockClassManager.canUpgradeClass(eq(mockPlayer), anyString()))
                    .thenReturn(new ClassManager.ClassUpResult(true, "", new ArrayList<>()));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.of(initialClass));
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("warrior_advanced"))).thenReturn(true);

            ClassUpgrader.ClassUpResult upResult = classUpgrader.executeClassUp(mockPlayer, "warrior_advanced");
            assertThat(upResult.isSuccess()).isTrue();
            assertThat(upResult.isRankedUp()).isTrue();
        }

        @Test
        @DisplayName("直線パターン: アイテム消費が必要なアップグレード")
        void integration_LinearWithItemConsumption_Success() {
            // ItemRequirementを作成
            ItemRequirement itemReq = new ItemRequirement("魔法の石", 1, true);

            // 初期クラス
            RPGClass initialClass = new RPGClass.Builder("warrior")
                    .setName("戦士")
                    .setDisplayName("戦士")
                    .setRank(1)
                    .setNextRankClassId("warrior_advanced")
                    .addNextRankRequirement(itemReq)
                    .build();

            // 次のランク
            RPGClass advancedClass = new RPGClass.Builder("warrior_advanced")
                    .setName("熟練戦士")
                    .setDisplayName("熟練戦士")
                    .setRank(2)
                    .build();

            // 初期クラス設定
            when(mockClassManager.getClass("warrior")).thenReturn(Optional.of(initialClass));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.empty());
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("warrior"))).thenReturn(true);

            ClassUpgrader.ClassUpResult initResult = classUpgrader.setInitialClass(mockPlayer, "warrior");
            assertThat(initResult.isSuccess()).isTrue();

            // 直線パターンでアップ（アイテム消費が必要）
            when(mockClassManager.getClass("warrior_advanced")).thenReturn(Optional.of(advancedClass));
            when(mockClassManager.canUpgradeClass(eq(mockPlayer), anyString()))
                    .thenReturn(new ClassManager.ClassUpResult(true, "", new ArrayList<>()));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.of(initialClass));
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("warrior_advanced"))).thenReturn(true);

            // インベントリに魔法の石を設定
            ItemStack magicStone = mock(ItemStack.class);
            ItemMeta meta = mock(ItemMeta.class);
            when(meta.hasDisplayName()).thenReturn(true);
            Component displayNameComponent = Component.text("魔法の石");
            when(meta.displayName()).thenReturn(displayNameComponent);
            when(magicStone.getItemMeta()).thenReturn(meta);
            when(magicStone.hasItemMeta()).thenReturn(true);
            when(magicStone.getAmount()).thenReturn(1);

            org.bukkit.inventory.PlayerInventory mockInventory = mock(org.bukkit.inventory.PlayerInventory.class);
            when(mockInventory.getContents()).thenReturn(new ItemStack[]{magicStone});
            when(mockPlayer.getInventory()).thenReturn(mockInventory);

            ClassUpgrader.ClassUpResult upResult = classUpgrader.executeClassUp(mockPlayer, "warrior_advanced");
            assertThat(upResult.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("分岐パターン: アイテム消費が必要なアップグレード")
        void integration_BranchWithItemConsumption_Success() {
            // ItemRequirementを作成
            List<ClassRequirement> requirements = new ArrayList<>();
            requirements.add(new ItemRequirement("狂戦士の証", 1, true));

            // 初期クラス
            RPGClass initialClass = new RPGClass.Builder("warrior")
                    .setName("戦士")
                    .setDisplayName("戦士")
                    .setRank(1)
                    .addAlternativeRank("berserker", requirements)
                    .build();

            // 分岐先クラス
            RPGClass berserkerClass = new RPGClass.Builder("berserker")
                    .setName("バーサーカー")
                    .setDisplayName("バーサーカー")
                    .setRank(2)
                    .build();

            // 初期クラス設定
            when(mockClassManager.getClass("warrior")).thenReturn(Optional.of(initialClass));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.empty());
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("warrior"))).thenReturn(true);

            ClassUpgrader.ClassUpResult initResult = classUpgrader.setInitialClass(mockPlayer, "warrior");
            assertThat(initResult.isSuccess()).isTrue();

            // 分岐パターンでアップ（アイテム消費が必要）
            when(mockClassManager.getClass("berserker")).thenReturn(Optional.of(berserkerClass));
            when(mockClassManager.canUpgradeClass(eq(mockPlayer), anyString()))
                    .thenReturn(new ClassManager.ClassUpResult(true, "", new ArrayList<>()));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.of(initialClass));
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("berserker"))).thenReturn(true);

            // インベントリに狂戦士の証を設定
            ItemStack proof = mock(ItemStack.class);
            ItemMeta meta = mock(ItemMeta.class);
            when(meta.hasDisplayName()).thenReturn(true);
            Component displayNameComponent = Component.text("狂戦士の証");
            when(meta.displayName()).thenReturn(displayNameComponent);
            when(proof.getItemMeta()).thenReturn(meta);
            when(proof.hasItemMeta()).thenReturn(true);
            when(proof.getAmount()).thenReturn(1);

            org.bukkit.inventory.PlayerInventory mockInventory = mock(org.bukkit.inventory.PlayerInventory.class);
            when(mockInventory.getContents()).thenReturn(new ItemStack[]{proof});
            when(mockPlayer.getInventory()).thenReturn(mockInventory);

            ClassUpgrader.ClassUpResult upResult = classUpgrader.executeClassUp(mockPlayer, "berserker");
            assertThat(upResult.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("アイテム不足: アップグレード失敗")
        void integration_ItemInsufficient_Failure() {
            // ItemRequirementを作成
            ItemRequirement itemReq = new ItemRequirement("魔法の石", 1, true);

            // 初期クラス
            RPGClass initialClass = new RPGClass.Builder("warrior")
                    .setName("戦士")
                    .setDisplayName("戦士")
                    .setRank(1)
                    .setNextRankClassId("warrior_advanced")
                    .addNextRankRequirement(itemReq)
                    .build();

            // 次のランク
            RPGClass advancedClass = new RPGClass.Builder("warrior_advanced")
                    .setName("熟練戦士")
                    .setDisplayName("熟練戦士")
                    .setRank(2)
                    .build();

            when(mockClassManager.getClass("warrior_advanced")).thenReturn(Optional.of(advancedClass));
            when(mockClassManager.canUpgradeClass(eq(mockPlayer), anyString()))
                    .thenReturn(new ClassManager.ClassUpResult(true, "", new ArrayList<>()));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.of(initialClass));
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("warrior_advanced"))).thenReturn(true);

            // インベントリは空
            org.bukkit.inventory.PlayerInventory mockInventory = mock(org.bukkit.inventory.PlayerInventory.class);
            when(mockInventory.getContents()).thenReturn(new ItemStack[]{});
            when(mockPlayer.getInventory()).thenReturn(mockInventory);

            ClassUpgrader.ClassUpResult upResult = classUpgrader.executeClassUp(mockPlayer, "warrior_advanced");
            assertThat(upResult.isSuccess()).isFalse();
            assertThat(upResult.getMessage()).contains("消費に失敗");
        }

        @Test
        @DisplayName("アイテム消費: 複数スタックからの消費")
        void integration_ConsumeFromMultipleStacks_Success() {
            // ItemRequirement（3個必要）
            ItemRequirement itemReq = new ItemRequirement("魔法の石", 3, true);

            // 初期クラス
            RPGClass initialClass = new RPGClass.Builder("warrior")
                    .setName("戦士")
                    .setDisplayName("戦士")
                    .setRank(1)
                    .setNextRankClassId("warrior_advanced")
                    .addNextRankRequirement(itemReq)
                    .build();

            // 次のランク
            RPGClass advancedClass = new RPGClass.Builder("warrior_advanced")
                    .setName("熟練戦士")
                    .setDisplayName("熟練戦士")
                    .setRank(2)
                    .build();

            when(mockClassManager.getClass("warrior_advanced")).thenReturn(Optional.of(advancedClass));
            when(mockClassManager.canUpgradeClass(eq(mockPlayer), anyString()))
                    .thenReturn(new ClassManager.ClassUpResult(true, "", new ArrayList<>()));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.of(initialClass));
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("warrior_advanced"))).thenReturn(true);

            // 複数スタックに分かれたアイテム
            ItemStack stack1 = mock(ItemStack.class);
            ItemMeta meta1 = mock(ItemMeta.class);
            when(meta1.hasDisplayName()).thenReturn(true);
            Component displayNameComponent1 = Component.text("魔法の石");
            when(meta1.displayName()).thenReturn(displayNameComponent1);
            when(stack1.getItemMeta()).thenReturn(meta1);
            when(stack1.hasItemMeta()).thenReturn(true);
            when(stack1.getAmount()).thenReturn(2);

            ItemStack stack2 = mock(ItemStack.class);
            ItemMeta meta2 = mock(ItemMeta.class);
            when(meta2.hasDisplayName()).thenReturn(true);
            Component displayNameComponent2 = Component.text("魔法の石");
            when(meta2.displayName()).thenReturn(displayNameComponent2);
            when(stack2.getItemMeta()).thenReturn(meta2);
            when(stack2.hasItemMeta()).thenReturn(true);
            when(stack2.getAmount()).thenReturn(2);

            org.bukkit.inventory.PlayerInventory mockInventory = mock(org.bukkit.inventory.PlayerInventory.class);
            when(mockInventory.getContents()).thenReturn(new ItemStack[]{stack1, stack2});
            when(mockPlayer.getInventory()).thenReturn(mockInventory);

            ClassUpgrader.ClassUpResult upResult = classUpgrader.executeClassUp(mockPlayer, "warrior_advanced");
            assertThat(upResult.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("アイテム消費: 部分的消費（スタック残り）")
        void integration_PartialConsumption_Success() {
            // ItemRequirement（1個必要）
            ItemRequirement itemReq = new ItemRequirement("魔法の石", 1, true);

            // 初期クラス
            RPGClass initialClass = new RPGClass.Builder("warrior")
                    .setName("戦士")
                    .setDisplayName("戦士")
                    .setRank(1)
                    .setNextRankClassId("warrior_advanced")
                    .addNextRankRequirement(itemReq)
                    .build();

            // 次のランク
            RPGClass advancedClass = new RPGClass.Builder("warrior_advanced")
                    .setName("熟練戦士")
                    .setDisplayName("熟練戦士")
                    .setRank(2)
                    .build();

            when(mockClassManager.getClass("warrior_advanced")).thenReturn(Optional.of(advancedClass));
            when(mockClassManager.canUpgradeClass(eq(mockPlayer), anyString()))
                    .thenReturn(new ClassManager.ClassUpResult(true, "", new ArrayList<>()));
            when(mockClassManager.getPlayerClass(mockPlayer)).thenReturn(Optional.of(initialClass));
            when(mockClassManager.setPlayerClass(eq(mockPlayer), eq("warrior_advanced"))).thenReturn(true);

            // 64個スタックから1個消費
            ItemStack fullStack = mock(ItemStack.class);
            ItemMeta meta = mock(ItemMeta.class);
            when(meta.hasDisplayName()).thenReturn(true);
            Component displayNameComponent = Component.text("魔法の石");
            when(meta.displayName()).thenReturn(displayNameComponent);
            when(fullStack.getItemMeta()).thenReturn(meta);
            when(fullStack.hasItemMeta()).thenReturn(true);
            when(fullStack.getAmount()).thenReturn(64);
            doNothing().when(fullStack).setAmount(anyInt());

            org.bukkit.inventory.PlayerInventory mockInventory = mock(org.bukkit.inventory.PlayerInventory.class);
            when(mockInventory.getContents()).thenReturn(new ItemStack[]{fullStack});
            when(mockPlayer.getInventory()).thenReturn(mockInventory);

            ClassUpgrader.ClassUpResult upResult = classUpgrader.executeClassUp(mockPlayer, "warrior_advanced");
            assertThat(upResult.isSuccess()).isTrue();
            // 残りのスタックが63個に設定されることを確認
            verify(fullStack).setAmount(63);
        }
    }
}
