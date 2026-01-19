package com.example.rpgplugin.gui;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillNode;
import com.example.rpgplugin.skill.SkillTree;
import com.example.rpgplugin.skill.SkillTreeRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SkillTreeGUIの単体テスト
 *
 * <p>スキルツリーGUIの機能テスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("SkillTreeGUI テスト")
@ExtendWith(MockitoExtension.class)
class SkillTreeGUITest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private Player mockPlayer;

    @Mock
    private SkillManager mockSkillManager;

    @Mock
    private SkillTree mockSkillTree;

    @Mock
    private SkillTreeRegistry mockSkillTreeRegistry;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private RPGPlayer mockRpgPlayer;

    @Mock
    private Inventory mockInventory;

    @Mock
    private InventoryView mockInventoryView;

    @Mock
    private Skill mockSkill;

    @Mock
    private SkillNode mockSkillNode;

    @Mock
    private org.bukkit.Server mockServer;

    @Mock
    private org.bukkit.inventory.ItemFactory mockItemFactory;

    @Mock
    private ItemMeta mockItemMeta;

    @Mock
    private SkillManager.PlayerSkillData mockPlayerSkillData;

    private MockedStatic<Bukkit> mockedBukkit;
    private UUID playerUuid;
    private SkillTreeGUI gui;

    @BeforeEach
    void setUp() {
        // UUIDの設定
        playerUuid = UUID.randomUUID();

        // 基本設定
        lenient().when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        lenient().when(mockPlayer.getName()).thenReturn("TestPlayer");
        lenient().when(mockPlayer.getLevel()).thenReturn(10);

        // プラグイン設定
        lenient().when(mockPlugin.getSkillManager()).thenReturn(mockSkillManager);
        lenient().when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);
        lenient().when(mockPlugin.getServer()).thenReturn(mockServer);

        // SkillManager設定
        lenient().when(mockSkillManager.getTreeRegistry()).thenReturn(mockSkillTreeRegistry);
        lenient().when(mockSkillTreeRegistry.getTree(anyString())).thenReturn(mockSkillTree);
        lenient().when(mockSkillManager.getSkillLevel(eq(mockPlayer), anyString())).thenReturn(0);
        lenient().when(mockSkillManager.getSkillsForClass(anyString())).thenReturn(new ArrayList<>());
        lenient().when(mockSkillManager.getPlayerSkillData((Player) any())).thenReturn(mockPlayerSkillData);
        lenient().when(mockSkillManager.getPlayerSkillData((UUID) any())).thenReturn(mockPlayerSkillData);

        // PlayerManager設定
        lenient().when(mockPlayerManager.getRPGPlayer(playerUuid)).thenReturn(mockRpgPlayer);
        lenient().when(mockRpgPlayer.getClassId()).thenReturn("warrior");

        // SkillTree設定
        lenient().when(mockSkillTree.getAllNodes()).thenReturn(new HashMap<>());
        lenient().when(mockSkillTree.getCost(anyString())).thenReturn(1);
        lenient().when(mockSkillTree.getNode(anyString())).thenReturn(mockSkillNode);
        lenient().when(mockSkillTree.getParentSkillId(anyString())).thenReturn("none");

        // SkillNode設定
        lenient().when(mockSkillNode.getSkill()).thenReturn(mockSkill);
        lenient().when(mockSkillNode.isRoot()).thenReturn(true);
        lenient().when(mockSkillNode.isLeaf()).thenReturn(true);
        lenient().when(mockSkillNode.getChildren()).thenReturn(new ArrayList<>());

        // Skill設定
        lenient().when(mockSkill.getId()).thenReturn("test_skill");
        lenient().when(mockSkill.getDisplayName()).thenReturn("Test Skill");
        lenient().when(mockSkill.getColoredDisplayName()).thenReturn("<gold>Test Skill</gold>");
        lenient().when(mockSkill.getMaxLevel()).thenReturn(5);
        lenient().when(mockSkill.getIconMaterial()).thenReturn("DIAMOND_SWORD");
        lenient().when(mockSkill.getSkillTarget()).thenReturn(null);
        lenient().when(mockSkill.getDescription()).thenReturn(List.of("Test description"));

        // Bukkit設定
        mockedBukkit = mockStatic(Bukkit.class);
        lenient().when(Bukkit.getServer()).thenReturn(mockServer);
        lenient().when(Bukkit.getItemFactory()).thenReturn(mockItemFactory);
        lenient().when(mockItemFactory.getItemMeta(any(Material.class))).thenReturn(mockItemMeta);

        // Inventory設定
        lenient().when(mockServer.createInventory(any(), eq(54), any(Component.class)))
                .thenReturn(mockInventory);
        lenient().when(mockPlayer.openInventory((Inventory) any())).thenReturn(mockInventoryView);
        lenient().when(mockPlayer.getOpenInventory()).thenReturn(mockInventoryView);
        lenient().when(mockInventoryView.getTopInventory()).thenReturn(mockInventory);
        lenient().when(mockInventoryView.title()).thenReturn(Component.text("スキルツリー"));

        // GUIインスタンス生成（warriorクラスで初期化）
        gui = new SkillTreeGUI(mockPlugin, mockPlayer, "warrior");
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
        // テスト間でGUIインスタンスをクリーンアップ
        if (gui != null) {
            gui.close();
        }
    }

    // ==================== ヘルパーメソッド ====================

    private void setupSkillNodes(Map<String, SkillNode> nodes) {
        lenient().when(mockSkillTree.getAllNodes()).thenReturn(nodes);
        lenient().when(mockSkillTree.getNode(anyString())).thenAnswer(invocation -> {
            String skillId = invocation.getArgument(0);
            return nodes.values().stream()
                    .filter(node -> node.getSkill().getId().equals(skillId))
                    .findFirst()
                    .orElse(mockSkillNode);
        });
    }

    // ==================== コンストラクタと初期化 テスト ====================

    @Test
    @DisplayName("コンストラクタ: 正常な初期化")
    void testConstructor_ValidInitialization() {
        assertNotNull(gui, "GUIインスタンスが生成されるべき");
    }

    @Test
    @DisplayName("コンストラクタ: nullクラスIDの場合はデフォルトクラスを使用")
    void testConstructor_NullClassId_UsesDefault() {
        SkillTreeGUI guiWithNull = new SkillTreeGUI(mockPlugin, mockPlayer, null);
        assertNotNull(guiWithNull, "nullクラスIDでもGUIが生成されるべき");
        guiWithNull.close();
    }

    @Test
    @DisplayName("コンストラクタ: 存在しないクラスIDの場合はエラーメッセージ")
    void testConstructor_InvalidClassId_ShowErrorMessage() {
        lenient().when(mockSkillTreeRegistry.getTree("nonexistent")).thenReturn(null);

        SkillTreeGUI invalidGui = new SkillTreeGUI(mockPlugin, mockPlayer, "nonexistent");
        assertNotNull(invalidGui, "無効なクラスIDでもGUIインスタンスは生成される");
        // スキルツリーがnullの場合、open()時にエラーメッセージが表示される
        invalidGui.close();
    }

    @Test
    @DisplayName("getDefaultClassId: RPGPlayerが存在する場合はそのクラスIDを使用")
    void testGetDefaultClassId_WithRPGPlayer() {
        lenient().when(mockRpgPlayer.getClassId()).thenReturn("mage");

        SkillTreeGUI guiDefault = new SkillTreeGUI(mockPlugin, mockPlayer, null);
        assertNotNull(guiDefault, "デフォルトクラスIDでGUIが生成される");
        guiDefault.close();
    }

    // ==================== open() メソッド テスト ====================

    @Test
    @DisplayName("open: スキルツリーがnullの場合はエラーメッセージ")
    void testOpen_NullSkillTree_ShowErrorMessage() {
        lenient().when(mockSkillTreeRegistry.getTree("warrior")).thenReturn(null);
        SkillTreeGUI invalidGui = new SkillTreeGUI(mockPlugin, mockPlayer, "warrior");

        invalidGui.open();

        verify(mockPlayer, atLeastOnce()).sendMessage(any(Component.class));
        invalidGui.close();
    }

    @Test
    @DisplayName("open: 正常にGUIを開く")
    void testOpen_Success() {
        gui.open();

        verify(mockPlayer).openInventory(any(Inventory.class));
    }

    @Test
    @DisplayName("open: GUIインスタンスが登録される")
    void testOpen_RegistersGUIInstance() {
        gui.open();

        SkillTreeGUI retrieved = SkillTreeGUI.getOpenGUI(mockPlayer);
        assertNotNull(retrieved, "GUIインスタンスが登録されるべき");
        assertEquals(gui, retrieved, "登録されたGUIは同じインスタンスであるべき");
    }

    // ==================== createInventory() テスト ====================

    @Test
    @DisplayName("createInventory: 正常にインベントリが作成される")
    void testCreateInventory_Success() {
        gui.open();

        verify(mockServer).createInventory(isNull(), eq(54), any(Component.class));
    }

    @Test
    @DisplayName("createInventory: インベントリサイズが正しい")
    void testCreateInventory_CorrectSize() {
        gui.open();

        verify(mockServer).createInventory(isNull(), eq(54), any(Component.class));
    }

    // ==================== スキル習得（acquireSkill）テスト ====================

    @Test
    @DisplayName("acquireSkill: スキルがnullの場合はfalse")
    void testAcquireSkill_NullSkill_ReturnsFalse() {
        lenient().when(mockSkillManager.getSkill("invalid")).thenReturn(null);

        boolean result = gui.acquireSkill("invalid");

        assertFalse(result, "無効なスキルIDではfalseを返すべき");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("acquireSkill: スキルポイントが足りない場合はfalse")
    void testAcquireSkill_NotEnoughSkillPoints_ReturnsFalse() {
        lenient().when(mockSkillManager.getSkill("test_skill")).thenReturn(mockSkill);
        lenient().when(mockSkillTree.getCost("test_skill")).thenReturn(100);

        boolean result = gui.acquireSkill("test_skill");

        assertFalse(result, "スキルポイントが足りない場合はfalseを返すべき");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("acquireSkill: 習得条件を満たしていない場合はfalse")
    void testAcquireSkill_CannotAcquire_ReturnsFalse() {
        lenient().when(mockSkillManager.getSkill("test_skill")).thenReturn(mockSkill);
        lenient().when(mockSkillTree.getCost("test_skill")).thenReturn(1);
        lenient().when(mockSkillNode.isRoot()).thenReturn(false);
        lenient().when(mockSkillTree.getParentSkillId("test_skill")).thenReturn("parent_skill");
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, "parent_skill")).thenReturn(0);

        boolean result = gui.acquireSkill("test_skill");

        assertFalse(result, "習得条件を満たしていない場合はfalseを返すべき");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("acquireSkill: 新規習得成功")
    void testAcquireSkill_NewSkill_Success() {
        lenient().when(mockSkillManager.getSkill("test_skill")).thenReturn(mockSkill);
        lenient().when(mockSkillTree.getCost("test_skill")).thenReturn(1);
        lenient().when(mockSkillManager.acquireSkill(mockPlayer, "test_skill", 1)).thenReturn(true);

        boolean result = gui.acquireSkill("test_skill");

        assertTrue(result, "新規習得に成功すべき");
        verify(mockPlayer).sendMessage(any(Component.class));
        verify(mockSkillManager).acquireSkill(mockPlayer, "test_skill", 1);
    }

    @Test
    @DisplayName("acquireSkill: レベルアップ成功")
    void testAcquireSkill_LevelUp_Success() {
        lenient().when(mockSkillManager.getSkill("test_skill")).thenReturn(mockSkill);
        lenient().when(mockSkillTree.getCost("test_skill")).thenReturn(1);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, "test_skill")).thenReturn(1);
        lenient().when(mockSkillManager.upgradeSkill(mockPlayer, "test_skill")).thenReturn(true);

        boolean result = gui.acquireSkill("test_skill");

        assertTrue(result, "レベルアップに成功すべき");
        verify(mockSkillManager).upgradeSkill(mockPlayer, "test_skill");
    }

    @Test
    @DisplayName("acquireSkill: 最大レベル到達後はfalse")
    void testAcquireSkill_MaxLevel_ReturnsFalse() {
        lenient().when(mockSkillManager.getSkill("test_skill")).thenReturn(mockSkill);
        lenient().when(mockSkillTree.getCost("test_skill")).thenReturn(1);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, "test_skill")).thenReturn(5);

        boolean result = gui.acquireSkill("test_skill");

        assertFalse(result, "最大レベルに達している場合はfalseを返すべき");
        // canAcquireSkillがfalseを返すので、条件を満たしていませんメッセージ
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    // ==================== スキル解除（refundSkill）テスト ====================

    @Test
    @DisplayName("refundSkill: スキルがnullの場合はfalse")
    void testRefundSkill_NullSkill_ReturnsFalse() {
        lenient().when(mockSkillManager.getSkill("invalid")).thenReturn(null);

        boolean result = gui.refundSkill("invalid");

        assertFalse(result, "無効なスキルIDではfalseを返すべき");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("refundSkill: 未習得スキルの場合はfalse")
    void testRefundSkill_NotAcquired_ReturnsFalse() {
        lenient().when(mockSkillManager.getSkill("test_skill")).thenReturn(mockSkill);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, "test_skill")).thenReturn(0);

        boolean result = gui.refundSkill("test_skill");

        assertFalse(result, "未習得スキルではfalseを返すべき");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("refundSkill: 子スキルがある場合は解除不可")
    void testRefundSkill_HasChildSkill_ReturnsFalse() {
        lenient().when(mockSkillManager.getSkill("test_skill")).thenReturn(mockSkill);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, "test_skill")).thenReturn(1);
        lenient().when(mockSkillNode.isLeaf()).thenReturn(false);

        SkillNode childNode = mock(SkillNode.class);
        Skill childSkill = mock(Skill.class);
        lenient().when(childNode.getSkill()).thenReturn(childSkill);
        lenient().when(childSkill.getId()).thenReturn("child_skill");
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, "child_skill")).thenReturn(1);

        List<SkillNode> children = List.of(childNode);
        lenient().when(mockSkillNode.getChildren()).thenReturn(children);

        boolean result = gui.refundSkill("test_skill");

        assertFalse(result, "子スキルがある場合は解除不可");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("refundSkill: 完全削除成功")
    void testRefundSkill_RemoveCompletely_Success() {
        lenient().when(mockSkillManager.getSkill("test_skill")).thenReturn(mockSkill);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, "test_skill")).thenReturn(1);
        lenient().when(mockSkillManager.getPlayerSkillData(mockPlayer)).thenReturn(mockPlayerSkillData);

        boolean result = gui.refundSkill("test_skill");

        assertTrue(result, "スキル完全削除に成功すべき");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("refundSkill: レベルダウン成功")
    void testRefundSkill_LevelDown_Success() {
        lenient().when(mockSkillManager.getSkill("test_skill")).thenReturn(mockSkill);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, "test_skill")).thenReturn(3);
        lenient().when(mockSkillManager.getPlayerSkillData(mockPlayer)).thenReturn(mockPlayerSkillData);

        boolean result = gui.refundSkill("test_skill");

        assertTrue(result, "レベルダウンに成功すべき");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    // ==================== getSkillIdFromSlot() テスト ====================

    @Test
    @DisplayName("getSkillIdFromSlot: スキルスロット外の場合はnull")
    void testGetSkillIdFromSlot_InvalidSlot_ReturnsNull() {
        String skillId = gui.getSkillIdFromSlot(8); // スキル表示開始前

        assertNull(skillId, "スキルスロット外ではnullを返すべき");
    }

    @Test
    @DisplayName("getSkillIdFromSlot: 有効なスロットでスキルIDを返す")
    void testGetSkillIdFromSlot_ValidSlot_ReturnsSkillId() {
        Map<String, SkillNode> nodes = new HashMap<>();
        nodes.put("test_skill", mockSkillNode);
        setupSkillNodes(nodes);

        String skillId = gui.getSkillIdFromSlot(9);

        assertEquals("test_skill", skillId, "正しいスキルIDを返すべき");
    }

    @Test
    @DisplayName("getSkillIdFromSlot: 範囲外のインデックスの場合はnull")
    void testGetSkillIdFromSlot_OutOfRange_ReturnsNull() {
        Map<String, SkillNode> nodes = new HashMap<>();
        nodes.put("test_skill", mockSkillNode);
        setupSkillNodes(nodes);

        String skillId = gui.getSkillIdFromSlot(50); // 存在しないスロット

        assertNull(skillId, "範囲外ではnullを返すべき");
    }

    // ==================== isSkillSlot() テスト ====================

    @Test
    @DisplayName("isSkillSlot: スキルスロット範囲内")
    void testIsSkillSlot_ValidRange_ReturnsTrue() {
        assertTrue(SkillTreeGUI.isSkillSlot(9), "開始スロットは有効");
        assertTrue(SkillTreeGUI.isSkillSlot(44), "終了スロットは有効");
        assertTrue(SkillTreeGUI.isSkillSlot(25), "中間スロットは有効");
    }

    @Test
    @DisplayName("isSkillSlot: スキルスロット範囲外")
    void testIsSkillSlot_InvalidRange_ReturnsFalse() {
        assertFalse(SkillTreeGUI.isSkillSlot(8), "開始前は無効");
        assertFalse(SkillTreeGUI.isSkillSlot(45), "終了後は無効");
        assertFalse(SkillTreeGUI.isSkillSlot(0), "最初の行は無効");
        assertFalse(SkillTreeGUI.isSkillSlot(53), "最後の行は無効");
    }

    // ==================== refreshGUI() テスト ====================

    @Test
    @DisplayName("refreshGUI: GUIがリフレッシュされる")
    void testRefreshGUI_Success() {
        gui.refreshGUI();

        // インベントリがクリアされることを確認
        verify(mockInventory, atLeastOnce()).clear();
    }

    @Test
    @DisplayName("refreshGUI: プレイヤーがインベントリを開いていない場合は何もしない")
    void testRefreshGUI_NoInventoryOpen_DoesNothing() {
        lenient().when(mockPlayer.getOpenInventory()).thenReturn(null);

        assertDoesNotThrow(() -> gui.refreshGUI());
    }

    // ==================== getOpenGUI() テスト ====================

    @Test
    @DisplayName("getOpenGUI: 開いているGUIを取得")
    void testGetOpenGUI_ReturnsCorrectGUI() {
        gui.open();

        SkillTreeGUI retrieved = SkillTreeGUI.getOpenGUI(mockPlayer);

        assertNotNull(retrieved, "開いているGUIを取得できる");
        assertEquals(gui, retrieved, "同じインスタンスを返すべき");
    }

    @Test
    @DisplayName("getOpenGUI: GUIが開いていない場合はnull")
    void testGetOpenGUI_NotOpened_ReturnsNull() {
        SkillTreeGUI retrieved = SkillTreeGUI.getOpenGUI(mockPlayer);

        assertNull(retrieved, "開いていない場合はnullを返すべき");
    }

    // ==================== close() テスト ====================

    @Test
    @DisplayName("close: GUIインスタンスが削除される")
    void testClose_RemovesGUIInstance() {
        gui.open();
        gui.close();

        SkillTreeGUI retrieved = SkillTreeGUI.getOpenGUI(mockPlayer);
        assertNull(retrieved, "閉じた後は取得できない");
    }

    // ==================== 境界値テスト ====================

    @Test
    @DisplayName("境界値: 空のスキルリスト")
    void testBoundary_EmptySkillList() {
        lenient().when(mockSkillTree.getAllNodes()).thenReturn(new HashMap<>());

        gui.open();

        // インベントリが作成され、アイテムが設定されることを確認
        verify(mockInventory, atLeastOnce()).setItem(anyInt(), any(ItemStack.class));
    }

    @Test
    @DisplayName("境界値: 最大スキル数")
    void testBoundary_MaximumSkills() {
        // 36スキル（最大表示数）を作成
        Map<String, SkillNode> nodes = new HashMap<>();
        for (int i = 0; i < 36; i++) {
            Skill skill = mock(Skill.class);
            SkillNode node = mock(SkillNode.class);

            lenient().when(skill.getId()).thenReturn("skill_" + i);
            lenient().when(skill.getIconMaterial()).thenReturn("DIAMOND_SWORD");
            lenient().when(skill.getSkillTarget()).thenReturn(null);
            lenient().when(skill.getDisplayName()).thenReturn("Skill " + i);
            lenient().when(skill.getColoredDisplayName()).thenReturn("<gold>Skill " + i + "</gold>");
            lenient().when(skill.getDescription()).thenReturn(List.of("Description"));
            lenient().when(skill.getMaxLevel()).thenReturn(5);
            lenient().when(node.getSkill()).thenReturn(skill);
            lenient().when(node.isRoot()).thenReturn(true);

            nodes.put("skill_" + i, node);
        }
        setupSkillNodes(nodes);

        gui.open();

        // エラーなく処理されることを確認
        verify(mockInventory, atLeastOnce()).setItem(anyInt(), any(ItemStack.class));
    }

    @Test
    @DisplayName("境界値: スキルポイントが0の場合は習得不可")
    void testBoundary_ZeroSkillPoints() {
        // レベル0でコスト1のスキルを習得しようとする
        lenient().when(mockPlayer.getLevel()).thenReturn(0);
        lenient().when(mockSkillManager.getSkill("test_skill")).thenReturn(mockSkill);
        lenient().when(mockSkillTree.getCost("test_skill")).thenReturn(1);

        boolean result = gui.acquireSkill("test_skill");

        assertFalse(result, "スキルポイントが足りない場合は習得不可");
    }

    // ==================== 権限チェックテスト ====================

    @Test
    @DisplayName("権限: プレイヤーがnullの場合は適切に処理")
    void testPermission_NullPlayer() {
        assertThrows(NullPointerException.class, () -> {
            new SkillTreeGUI(mockPlugin, null, "warrior");
        });
    }

    // ==================== 複合テスト ====================

    @Test
    @DisplayName("複合: スキル習得後にGUIがリフレッシュされる")
    void testComplex_AcquireSkillRefreshesGUI() {
        lenient().when(mockSkillManager.getSkill("test_skill")).thenReturn(mockSkill);
        lenient().when(mockSkillTree.getCost("test_skill")).thenReturn(1);
        lenient().when(mockSkillManager.acquireSkill(mockPlayer, "test_skill", 1)).thenReturn(true);

        gui.acquireSkill("test_skill");

        // GUIがリフレッシュされることを確認
        verify(mockInventory, atLeastOnce()).clear();
    }

    @Test
    @DisplayName("複合: スキル解除後にGUIがリフレッシュされる")
    void testComplex_RefundSkillRefreshesGUI() {
        lenient().when(mockSkillManager.getSkill("test_skill")).thenReturn(mockSkill);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, "test_skill")).thenReturn(1);
        lenient().when(mockSkillManager.getPlayerSkillData(mockPlayer)).thenReturn(mockPlayerSkillData);

        gui.refundSkill("test_skill");

        // GUIがリフレッシュされることを確認
        verify(mockInventory, atLeastOnce()).clear();
    }

    @Test
    @DisplayName("複合: 親子関係のあるスキルツリー")
    void testComplex_ParentChildRelationship() {
        // 親スキル
        Skill parentSkill = mock(Skill.class);
        SkillNode parentNode = mock(SkillNode.class);
        lenient().when(parentSkill.getId()).thenReturn("parent_skill");
        lenient().when(parentNode.getSkill()).thenReturn(parentSkill);
        lenient().when(parentNode.isRoot()).thenReturn(true);

        // 子スキル
        Skill childSkill = mock(Skill.class);
        SkillNode childNode = mock(SkillNode.class);
        lenient().when(childSkill.getId()).thenReturn("child_skill");
        lenient().when(childNode.getSkill()).thenReturn(childSkill);
        lenient().when(childNode.isRoot()).thenReturn(false);

        Map<String, SkillNode> nodes = new HashMap<>();
        nodes.put("parent_skill", parentNode);
        nodes.put("child_skill", childNode);
        setupSkillNodes(nodes);

        // 親スキルが未習得の場合、子スキルは習得不可
        lenient().when(mockSkillTree.getParentSkillId("child_skill")).thenReturn("parent_skill");
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, "parent_skill")).thenReturn(0);
        lenient().when(mockSkillManager.getSkill("child_skill")).thenReturn(childSkill);
        lenient().when(mockSkillTree.getCost("child_skill")).thenReturn(1);

        boolean result = gui.acquireSkill("child_skill");

        assertFalse(result, "親スキル未習得の場合は子スキルを習得不可");
    }

    // ==================== エッジケーステスト ====================

    @Test
    @DisplayName("エッジケース: スキルコストが0")
    void testEdgeCase_ZeroCost() {
        lenient().when(mockSkillManager.getSkill("test_skill")).thenReturn(mockSkill);
        lenient().when(mockSkillTree.getCost("test_skill")).thenReturn(0);
        lenient().when(mockSkillManager.acquireSkill(mockPlayer, "test_skill", 1)).thenReturn(true);

        boolean result = gui.acquireSkill("test_skill");

        assertTrue(result, "コスト0でも習得可能");
    }

    @Test
    @DisplayName("エッジケース: スキル名が空文字列")
    void testEdgeCase_EmptySkillName() {
        lenient().when(mockSkillManager.getSkill("")).thenReturn(null);

        boolean result = gui.acquireSkill("");

        assertFalse(result, "空文字列のスキル名ではfalseを返す");
    }

    @Test
    @DisplayName("エッジケース: 最大レベルが1のスキル")
    void testEdgeCase_MaxLevelOne() {
        Skill skillMax1 = mock(Skill.class);
        lenient().when(skillMax1.getId()).thenReturn("test_skill");
        lenient().when(skillMax1.getMaxLevel()).thenReturn(1);
        lenient().when(skillMax1.getIconMaterial()).thenReturn("DIAMOND_SWORD");
        lenient().when(skillMax1.getSkillTarget()).thenReturn(null);

        lenient().when(mockSkillManager.getSkill("test_skill")).thenReturn(skillMax1);
        lenient().when(mockSkillTree.getCost("test_skill")).thenReturn(1);
        lenient().when(mockSkillManager.acquireSkill(mockPlayer, "test_skill", 1)).thenReturn(true);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, "test_skill")).thenReturn(0);

        // 習得
        assertTrue(gui.acquireSkill("test_skill"), "習得に成功");

        // 2回目の試行（最大レベル到達で失敗すべき）
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, "test_skill")).thenReturn(1);
        assertFalse(gui.acquireSkill("test_skill"), "最大レベル到達後は失敗");
    }
}
