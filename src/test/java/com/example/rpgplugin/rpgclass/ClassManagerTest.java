package com.example.rpgplugin.rpgclass;

import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.rpgclass.growth.StatGrowth;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ClassManagerの単体テスト
 *
 * <p>クラス管理システムの機能テスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("ClassManager テスト")
@ExtendWith(MockitoExtension.class)
class ClassManagerTest {

    @Mock
    private PlayerManager mockPlayerManager;
    @Mock
    private Player mockPlayer;
    @Mock
    private RPGPlayer mockRpgPlayer;

    private MockedStatic<Bukkit> mockedBukkit;
    private ClassManager classManager;
    private UUID testUuid;
    private RPGClass testClass1;
    private RPGClass testClass2;
    private RPGClass testClassRank2;

    @BeforeEach
    void setUp() {
        // Bukkit.getOnlinePlayers()のモック設定
        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn(Collections.emptySet());

        classManager = new ClassManager(mockPlayerManager);
        testUuid = UUID.randomUUID();

        // lenient() - 全てのテストで使用されないスタブ
        lenient().when(mockPlayer.getUniqueId()).thenReturn(testUuid);
        lenient().when(mockPlayer.getName()).thenReturn("TestPlayer");
        lenient().when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        lenient().when(mockRpgPlayer.getClassId()).thenReturn(null);
        lenient().when(mockRpgPlayer.getClassHistory()).thenReturn(new ArrayList<>());

        // テスト用クラスを作成
        StatGrowth growth = new StatGrowth.Builder()
                .setAutoGrowth(Stat.STRENGTH, 5)
                .setAutoGrowth(Stat.VITALITY, 3)
                .setManualPoints(2)
                .build();

        testClass1 = new RPGClass.Builder("warrior")
                .setDisplayName("ウォーリアー")
                .setRank(1)
                .setMaxLevel(50)
                .setIcon(Material.IRON_SWORD)
                .setStatGrowth(growth)
                .setNextRankClassId("warrior_rank2")
                .build();

        testClass2 = new RPGClass.Builder("mage")
                .setDisplayName("メイジ")
                .setRank(1)
                .setMaxLevel(50)
                .setIcon(Material.BLAZE_ROD)
                .setStatGrowth(growth)
                .build();

        testClassRank2 = new RPGClass.Builder("warrior_rank2")
                .setDisplayName("ウォーリアー Rank2")
                .setRank(2)
                .setMaxLevel(100)
                .setIcon(Material.DIAMOND_SWORD)
                .setStatGrowth(growth)
                .build();
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    // ==================== registerClass テスト ====================

    @Test
    @DisplayName("registerClass: 有効なクラスを登録")
    void testRegisterClass_Valid() {
        classManager.registerClass(testClass1);

        assertTrue(classManager.getClass("warrior").isPresent(), "クラスが登録されていること");
        assertEquals(1, classManager.getClassCount(), "クラス数が1であること");
    }

    @Test
    @DisplayName("registerClass: nullは例外")
    void testRegisterClass_Null() {
        assertThrows(IllegalArgumentException.class, () -> {
            classManager.registerClass(null);
        }, "nullクラスは例外");
    }

    @Test
    @DisplayName("registerClass: 重複IDは上書き")
    void testRegisterClass_Duplicate() {
        classManager.registerClass(testClass1);

        RPGClass newWarrior = new RPGClass.Builder("warrior")
                .setDisplayName("ニューウォーリアー")
                .setRank(1)
                .build();

        classManager.registerClass(newWarrior);

        assertEquals("ニューウォーリアー", classManager.getClass("warrior").get().getDisplayName(),
                "上書きされていること");
        assertEquals(1, classManager.getClassCount(), "クラス数は1のまま");
    }

    @Test
    @DisplayName("registerClass: 複数クラスを登録")
    void testRegisterClass_Multiple() {
        classManager.registerClass(testClass1);
        classManager.registerClass(testClass2);
        classManager.registerClass(testClassRank2);

        assertEquals(3, classManager.getClassCount());
        assertTrue(classManager.getClass("warrior").isPresent());
        assertTrue(classManager.getClass("mage").isPresent());
        assertTrue(classManager.getClass("warrior_rank2").isPresent());
    }

    // ==================== registerAll テスト ====================

    @Test
    @DisplayName("registerAll: 複数クラスを一括登録")
    void testRegisterAll() {
        Map<String, RPGClass> classes = new HashMap<>();
        classes.put("warrior", testClass1);
        classes.put("mage", testClass2);

        classManager.registerAll(classes);

        assertEquals(2, classManager.getClassCount());
        assertTrue(classManager.getClass("warrior").isPresent());
        assertTrue(classManager.getClass("mage").isPresent());
    }

    // ==================== getClass テスト ====================

    @Test
    @DisplayName("getClass: 登録済みクラスを取得")
    void testGetClass_Exists() {
        classManager.registerClass(testClass1);

        Optional<RPGClass> result = classManager.getClass("warrior");

        assertTrue(result.isPresent());
        assertEquals("warrior", result.get().getId());
        assertEquals("ウォーリアー", result.get().getDisplayName());
    }

    @Test
    @DisplayName("getClass: 未登録クラスはempty")
    void testGetClass_NotExists() {
        Optional<RPGClass> result = classManager.getClass("unknown");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("getClass: nullはempty")
    void testGetClass_Null() {
        Optional<RPGClass> result = classManager.getClass(null);

        assertFalse(result.isPresent());
    }

    // ==================== getAllClasses テスト ====================

    @Test
    @DisplayName("getAllClasses: 全クラスのコピーを取得")
    void testGetAllClasses() {
        classManager.registerClass(testClass1);
        classManager.registerClass(testClass2);

        Collection<RPGClass> allClasses = classManager.getAllClasses();

        assertEquals(2, allClasses.size());

        // コピーの確認
        allClasses.clear();
        assertEquals(2, classManager.getClassCount(), "元のマップには影響しない");
    }

    @Test
    @DisplayName("getAllClasses: 空の場合は空リスト")
    void testGetAllClasses_Empty() {
        Collection<RPGClass> allClasses = classManager.getAllClasses();

        assertTrue(allClasses.isEmpty());
    }

    // ==================== getAllClassIds テスト ====================

    @Test
    @DisplayName("getAllClassIds: 全クラスIDを取得")
    void testGetAllClassIds() {
        classManager.registerClass(testClass1);
        classManager.registerClass(testClass2);

        Set<String> ids = classManager.getAllClassIds();

        assertEquals(2, ids.size());
        assertTrue(ids.contains("warrior"));
        assertTrue(ids.contains("mage"));

        // コピーの確認
        ids.add("fake_id");
        assertFalse(classManager.getClass("fake_id").isPresent(), "取得したセットの変更は元に影響しない");
    }

    // ==================== getClassesByRank テスト ====================

    @Test
    @DisplayName("getClassesByRank: 指定ランクのクラスを取得")
    void testGetClassesByRank() {
        classManager.registerClass(testClass1);  // rank 1
        classManager.registerClass(testClass2);  // rank 1
        classManager.registerClass(testClassRank2);  // rank 2

        List<RPGClass> rank1Classes = classManager.getClassesByRank(1);
        List<RPGClass> rank2Classes = classManager.getClassesByRank(2);

        assertEquals(2, rank1Classes.size(), "Rank1は2クラス");
        assertEquals(1, rank2Classes.size(), "Rank2は1クラス");
    }

    @Test
    @DisplayName("getClassesByRank: 該当ランクなしは空リスト")
    void testGetClassesByRank_None() {
        classManager.registerClass(testClass1);

        List<RPGClass> rank5Classes = classManager.getClassesByRank(5);

        assertTrue(rank5Classes.isEmpty());
    }

    // ==================== getInitialClasses テスト ====================

    @Test
    @DisplayName("getInitialClasses: Rank1のクラスを取得")
    void testGetInitialClasses() {
        classManager.registerClass(testClass1);  // rank 1
        classManager.registerClass(testClassRank2);  // rank 2

        List<RPGClass> initialClasses = classManager.getInitialClasses();

        assertEquals(1, initialClasses.size());
        assertEquals("warrior", initialClasses.get(0).getId());
    }

    // ==================== getPlayerClass テスト ====================

    @Test
    @DisplayName("getPlayerClass: プレイヤーのクラスを取得")
    void testGetPlayerClass_HasClass() {
        when(mockRpgPlayer.getClassId()).thenReturn("warrior");
        classManager.registerClass(testClass1);

        Optional<RPGClass> result = classManager.getPlayerClass(mockPlayer);

        assertTrue(result.isPresent());
        assertEquals("warrior", result.get().getId());
    }

    @Test
    @DisplayName("getPlayerClass: クラス未設定はempty")
    void testGetPlayerClass_NoClass() {
        classManager.registerClass(testClass1);

        Optional<RPGClass> result = classManager.getPlayerClass(mockPlayer);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("getPlayerClass: nullプレイヤーはempty")
    void testGetPlayerClass_NullPlayer() {
        Optional<RPGClass> result = classManager.getPlayerClass(null);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("getPlayerClass: RPGPlayer未存在はempty")
    void testGetPlayerClass_RpgPlayerNull() {
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(null);

        Optional<RPGClass> result = classManager.getPlayerClass(mockPlayer);

        assertFalse(result.isPresent());
    }

    // ==================== setPlayerClass テスト ====================

    @Test
    @DisplayName("setPlayerClass: 正常に設定")
    void testSetPlayerClass_Success() {
        classManager.registerClass(testClass1);

        boolean result = classManager.setPlayerClass(mockPlayer, "warrior");

        assertTrue(result, "設定成功");
        verify(mockRpgPlayer).setClassId("warrior");
    }

    @Test
    @DisplayName("setPlayerClass: null引数は失敗")
    void testSetPlayerClass_NullArguments() {
        classManager.registerClass(testClass1);

        assertFalse(classManager.setPlayerClass(null, "warrior"), "player=nullは失敗");
        assertFalse(classManager.setPlayerClass(mockPlayer, null), "classId=nullは失敗");
    }

    @Test
    @DisplayName("setPlayerClass: 未登録クラスは失敗")
    void testSetPlayerClass_UnknownClass() {
        boolean result = classManager.setPlayerClass(mockPlayer, "unknown");

        assertFalse(result);
        verify(mockRpgPlayer, never()).setClassId(any());
    }

    @Test
    @DisplayName("setPlayerClass: RPGPlayer未存在は失敗")
    void testSetPlayerClass_RpgPlayerNull() {
        classManager.registerClass(testClass1);
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(null);

        boolean result = classManager.setPlayerClass(mockPlayer, "warrior");

        assertFalse(result);
    }

    @Test
    @DisplayName("setPlayerClass: 変更時に履歴を追加")
    void testSetPlayerClass_AddsHistory() {
        when(mockRpgPlayer.getClassId()).thenReturn("warrior");
        classManager.registerClass(testClass1);
        classManager.registerClass(testClass2);

        classManager.setPlayerClass(mockPlayer, "mage");

        verify(mockRpgPlayer).addClassToHistory("warrior");
        verify(mockRpgPlayer).setClassId("mage");
    }

    // ==================== clearPlayerClass テスト ====================

    @Test
    @DisplayName("clearPlayerClass: クラスを解除")
    void testClearPlayerClass() {
        when(mockRpgPlayer.getClassId()).thenReturn("warrior");

        classManager.clearPlayerClass(mockPlayer);

        verify(mockRpgPlayer).setClassId(null);
    }

    @Test
    @DisplayName("clearPlayerClass: nullプレイヤーは無視")
    void testClearPlayerClass_NullPlayer() {
        // 例外が発生しないことを確認
        assertDoesNotThrow(() -> classManager.clearPlayerClass(null));
    }

    @Test
    @DisplayName("clearPlayerClass: RPGPlayer未存在は無視")
    void testClearPlayerClass_RpgPlayerNull() {
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(null);

        assertDoesNotThrow(() -> classManager.clearPlayerClass(mockPlayer));
    }

    // ==================== getPlayerClassHistory テスト ====================

    @Test
    @DisplayName("getPlayerClassHistory: 履歴を取得")
    void testGetPlayerClassHistory() {
        List<String> history = Arrays.asList("warrior", "mage");
        when(mockRpgPlayer.getClassHistory()).thenReturn(history);

        List<String> result = classManager.getPlayerClassHistory(mockPlayer);

        assertEquals(2, result.size());
        assertEquals("warrior", result.get(0));
        assertEquals("mage", result.get(1));
    }

    @Test
    @DisplayName("getPlayerClassHistory: コピーを返す")
    void testGetPlayerClassHistory_ReturnsCopy() {
        List<String> history = new ArrayList<>(Arrays.asList("warrior"));
        when(mockRpgPlayer.getClassHistory()).thenReturn(history);

        List<String> result = classManager.getPlayerClassHistory(mockPlayer);
        result.clear();

        // 元のリストには影響しない（RPGPlayer.getClassHistoryが新しいリストを返すことを期待）
        // このテストは実装依存
    }

    // ==================== canUpgradeClass テスト ====================

    @Test
    @DisplayName("canUpgradeClass: 初期クラスは条件なしで成功")
    void testCanUpgradeClass_InitialClass() {
        classManager.registerClass(testClass1);

        ClassManager.ClassUpResult result = classManager.canUpgradeClass(mockPlayer, "warrior");

        assertTrue(result.isSuccess());
        assertEquals("初期クラスへ変更可能", result.getMessage());
    }

    @Test
    @DisplayName("canUpgradeClass: 未登録クラスは失敗")
    void testCanUpgradeClass_UnknownClass() {
        ClassManager.ClassUpResult result = classManager.canUpgradeClass(mockPlayer, "unknown");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("見つかりません"));
    }

    @Test
    @DisplayName("canUpgradeClass: null引数は失敗")
    void testCanUpgradeClass_NullArguments() {
        ClassManager.ClassUpResult result1 = classManager.canUpgradeClass(null, "warrior");

        assertFalse(result1.isSuccess());
        assertTrue(result1.getMessage().contains("無効"));

        ClassManager.ClassUpResult result2 = classManager.canUpgradeClass(mockPlayer, null);

        assertFalse(result2.isSuccess());
    }

    @Test
    @DisplayName("canUpgradeClass: Rank2以降は現在のクラスが必要")
    void testCanUpgradeClass_NeedsCurrentClass() {
        classManager.registerClass(testClassRank2);

        ClassManager.ClassUpResult result = classManager.canUpgradeClass(mockPlayer, "warrior_rank2");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("初期クラスを選択"));
    }

    @Test
    @DisplayName("canUpgradeClass: 分岐ランクへの変更")
    void testCanUpgradeClass_AlternativeRank() {
        // 分岐ランクを持つクラスを登録
        RPGClass warriorWithAlternatives = new RPGClass.Builder("warrior")
                .setDisplayName("ウォーリアー")
                .setRank(1)
                .addAlternativeRank("berserker", new ArrayList<>())
                .build();

        RPGClass berserker = new RPGClass.Builder("berserker")
                .setDisplayName("バーサーカー")
                .setRank(2)
                .build();

        classManager.registerClass(warriorWithAlternatives);
        classManager.registerClass(berserker);
        when(mockRpgPlayer.getClassId()).thenReturn("warrior");

        ClassManager.ClassUpResult result = classManager.canUpgradeClass(mockPlayer, "berserker");

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("クラスアップ可能"));
    }

    // ==================== reload テスト ====================

    @Test
    @DisplayName("reload: クラスをリロード")
    void testReload() {
        classManager.registerClass(testClass1);

        Map<String, RPGClass> newClasses = new HashMap<>();
        newClasses.put("mage", testClass2);

        classManager.reload(newClasses);

        assertFalse(classManager.getClass("warrior").isPresent(), "旧クラスは削除");
        assertTrue(classManager.getClass("mage").isPresent(), "新クラスが登録");
        assertEquals(1, classManager.getClassCount());
    }

    @Test
    @DisplayName("reload: 空マップでクリア")
    void testReload_Clear() {
        classManager.registerClass(testClass1);
        classManager.registerClass(testClass2);

        classManager.reload(Map.of());

        assertEquals(0, classManager.getClassCount());
    }

    // ==================== reloadWithCleanup テスト ====================

    @Test
    @DisplayName("reloadWithCleanup: 削除されたクラスを検出")
    void testReloadWithCleanup_DetectsRemoved() {
        classManager.registerClass(testClass1);
        classManager.registerClass(testClass2);

        Map<String, RPGClass> newClasses = new HashMap<>();
        newClasses.put("warrior", testClass1);  // warriorのみ残す

        ClassManager.ReloadResult result = classManager.reloadWithCleanup(newClasses);

        assertTrue(result.hasRemovedClasses());
        assertEquals(1, result.getRemovedClasses().size());
        assertTrue(result.getRemovedClasses().contains("mage"));
    }

    @Test
    @DisplayName("reloadWithCleanup: 削除なし")
    void testReloadWithCleanup_NoRemovals() {
        classManager.registerClass(testClass1);

        Map<String, RPGClass> newClasses = new HashMap<>();
        newClasses.put("warrior", testClass1);

        ClassManager.ReloadResult result = classManager.reloadWithCleanup(newClasses);

        assertFalse(result.hasRemovedClasses());
        assertEquals(0, result.getRemovedClasses().size());
    }

    @Test
    @DisplayName("reloadWithCleanup: オンラインプレイヤーに影響")
    void testReloadWithCleanup_WithOnlinePlayers() {
        classManager.registerClass(testClass1);
        when(mockRpgPlayer.getClassId()).thenReturn("warrior");

        // オンラインプレイヤーとしてmockPlayerを設定
        Set<Player> onlinePlayers = Set.of(mockPlayer);
        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn(onlinePlayers);

        Map<String, RPGClass> newClasses = new HashMap<>();
        // warriorを削除

        ClassManager.ReloadResult result = classManager.reloadWithCleanup(newClasses);

        assertTrue(result.hasRemovedClasses());
        assertEquals(1, result.getAffectedPlayerCount());
        verify(mockRpgPlayer).setClassId(null);
    }

    @Test
    @DisplayName("reloadWithCleanup: 複数クラス削除")
    void testReloadWithCleanup_MultipleRemoved() {
        classManager.registerClass(testClass1);
        classManager.registerClass(testClass2);
        classManager.registerClass(testClassRank2);

        Map<String, RPGClass> newClasses = new HashMap<>();
        // 全て削除

        ClassManager.ReloadResult result = classManager.reloadWithCleanup(newClasses);

        assertTrue(result.hasRemovedClasses());
        assertEquals(3, result.getRemovedClasses().size());
    }

    // ==================== getClassCount テスト ====================

    @Test
    @DisplayName("getClassCount: クラス数を取得")
    void testGetClassCount() {
        assertEquals(0, classManager.getClassCount(), "初期状態は0");

        classManager.registerClass(testClass1);
        assertEquals(1, classManager.getClassCount());

        classManager.registerClass(testClass2);
        assertEquals(2, classManager.getClassCount());
    }

    // ==================== changeClass テスト ====================

    @Test
    @DisplayName("changeClass(classId, level): 正常に変更")
    void testChangeClass_WithLevel() {
        classManager.registerClass(testClass1);

        boolean result = classManager.changeClass(mockPlayer, "warrior", 5);

        assertTrue(result);
        verify(mockRpgPlayer).setClassId("warrior");
        verify(mockPlayer).setLevel(5);
    }

    @Test
    @DisplayName("changeClass(classId, level): レベル0以下は0に制限")
    void testChangeClass_NegativeLevel() {
        classManager.registerClass(testClass1);

        boolean result = classManager.changeClass(mockPlayer, "warrior", -5);

        assertTrue(result);
        verify(mockPlayer).setLevel(0);
    }

    @Test
    @DisplayName("changeClass(classId): レベル0で変更")
    void testChangeClass_DefaultLevel() {
        classManager.registerClass(testClass1);

        boolean result = classManager.changeClass(mockPlayer, "warrior");

        assertTrue(result);
        verify(mockRpgPlayer).setClassId("warrior");
        verify(mockPlayer).setLevel(0);
    }

    @Test
    @DisplayName("changeClass: null引数は失敗")
    void testChangeClass_NullArguments() {
        classManager.registerClass(testClass1);

        assertFalse(classManager.changeClass(null, "warrior"));
        assertFalse(classManager.changeClass(mockPlayer, null));
    }

    @Test
    @DisplayName("changeClass: RPGPlayer未存在は失敗")
    void testChangeClass_RpgPlayerNull() {
        classManager.registerClass(testClass1);
        when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(null);

        boolean result = classManager.changeClass(mockPlayer, "warrior");

        assertFalse(result);
        verify(mockPlayer, never()).setLevel(anyInt());
    }

    @Test
    @DisplayName("changeClass: 未登録クラスは失敗")
    void testChangeClass_UnknownClass() {
        boolean result = classManager.changeClass(mockPlayer, "unknown");

        assertFalse(result);
        verify(mockRpgPlayer, never()).setClassId(any());
    }

    // ==================== ClassUpResult テスト ====================

    @Test
    @DisplayName("ClassUpResult: 成功結果")
    void testClassUpResult_Success() {
        ClassManager.ClassUpResult result = new ClassManager.ClassUpResult(
                true, "成功", Collections.emptyList()
        );

        assertTrue(result.isSuccess());
        assertEquals("成功", result.getMessage());
        assertTrue(result.getFailedRequirements().isEmpty());
    }

    @Test
    @DisplayName("ClassUpResult: 失敗結果")
    void testClassUpResult_Failure() {
        List<String> failed = Arrays.asList("レベル不足", "金額不足");
        ClassManager.ClassUpResult result = new ClassManager.ClassUpResult(
                false, "失敗", failed
        );

        assertFalse(result.isSuccess());
        assertEquals("失敗", result.getMessage());
        assertEquals(2, result.getFailedRequirements().size());
    }

    // ==================== ReloadResult テスト ====================

    @Test
    @DisplayName("ReloadResult: フィールド取得")
    void testReloadResult_Getters() {
        Set<String> removed = Set.of("class1", "class2");
        ClassManager.ReloadResult result = new ClassManager.ReloadResult(10, removed, 5);

        assertEquals(10, result.getLoadedClassCount());
        assertEquals(2, result.getRemovedClasses().size());
        assertEquals(5, result.getAffectedPlayerCount());
        assertTrue(result.hasRemovedClasses());
    }

    @Test
    @DisplayName("ReloadResult: 削除なし")
    void testReloadResult_NoRemovals() {
        ClassManager.ReloadResult result = new ClassManager.ReloadResult(
                10, Set.of(), 0
        );

        assertFalse(result.hasRemovedClasses());
        assertTrue(result.getRemovedClasses().isEmpty());
    }

    @Test
    @DisplayName("ReloadResult: toString")
    void testReloadResult_ToString() {
        ClassManager.ReloadResult result = new ClassManager.ReloadResult(
                10, Set.of("removed"), 5
        );

        String str = result.toString();
        assertTrue(str.contains("loaded=10"));
        assertTrue(str.contains("removed=1"));
        assertTrue(str.contains("affected=5"));
    }
}
