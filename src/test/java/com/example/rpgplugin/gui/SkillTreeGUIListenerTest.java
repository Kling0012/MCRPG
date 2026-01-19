package com.example.rpgplugin.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
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

/**
 * SkillTreeGUIListenerの単体テスト
 *
 * <p>スキルツリーGUIイベントリスナーの機能テスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("SkillTreeGUIListener テスト")
@ExtendWith(MockitoExtension.class)
class SkillTreeGUIListenerTest {

    @Mock
    private Player mockPlayer;

    @Mock
    private Inventory mockInventory;

    @Mock
    private InventoryView mockInventoryView;

    @Mock
    private InventoryClickEvent mockClickEvent;

    @Mock
    private InventoryCloseEvent mockCloseEvent;

    @Mock
    private SkillTreeGUI mockSkillTreeGUI;

    private MockedStatic<SkillTreeGUI> mockedSkillTreeGUI;
    private MockedStatic<Bukkit> mockedBukkit;
    private SkillTreeGUIListener listener;

    @BeforeEach
    void setUp() {
        listener = new SkillTreeGUIListener();

        // SkillTreeGUIの静的モック設定
        mockedSkillTreeGUI = mockStatic(SkillTreeGUI.class);

        // Bukkitのモック設定
        mockedBukkit = mockStatic(Bukkit.class);

        // 基本設定
        lenient().when(mockPlayer.getUniqueId()).thenReturn(java.util.UUID.randomUUID());
        lenient().when(mockPlayer.getName()).thenReturn("TestPlayer");

        // InventoryClickEvent基本設定
        lenient().when(mockClickEvent.getWhoClicked()).thenReturn(mockPlayer);
        lenient().when(mockClickEvent.getClickedInventory()).thenReturn(mockInventory);
        lenient().when(mockClickEvent.getView()).thenReturn(mockInventoryView);
        lenient().when(mockClickEvent.getSlot()).thenReturn(10);
        lenient().when(mockClickEvent.isRightClick()).thenReturn(false);
        lenient().when(mockClickEvent.isShiftClick()).thenReturn(false);

        // InventoryCloseEvent基本設定
        lenient().when(mockCloseEvent.getPlayer()).thenReturn(mockPlayer);
        lenient().when(mockCloseEvent.getView()).thenReturn(mockInventoryView);

        // InventoryView基本設定
        lenient().when(mockInventoryView.getTopInventory()).thenReturn(mockInventory);

        // タイトル設定
        lenient().when(mockInventoryView.title()).thenReturn(Component.text("スキルツリー"));
    }

    @AfterEach
    void tearDown() {
        if (mockedSkillTreeGUI != null) {
            mockedSkillTreeGUI.close();
        }
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    // ==================== InventoryClickEvent テスト ====================

    @Test
    @DisplayName("onInventoryClick: プレイヤー以外は無視")
    void testOnInventoryClick_NotPlayer() {
        // プレイヤー以外をシミュレート（nullを返す）
        when(mockClickEvent.getWhoClicked()).thenReturn(null);

        // nullの場合は例外が投げられず、単に無視される
        assertDoesNotThrow(() -> listener.onInventoryClick(mockClickEvent));

        // クリックがキャンセルされないことを確認
        verify(mockClickEvent, never()).setCancelled(true);
    }

    @Test
    @DisplayName("onInventoryClick: スキルツリーGUI以外は無視")
    void testOnInventoryClick_NotSkillTreeGUI() {
        when(mockInventoryView.title()).thenReturn(Component.text("Other Inventory"));

        listener.onInventoryClick(mockClickEvent);

        verify(mockClickEvent, never()).setCancelled(true);
    }

    @Test
    @DisplayName("onInventoryClick: スキルツリーGUIではクリックをキャンセル")
    void testOnInventoryClick_CancelsClick() {
        listener.onInventoryClick(mockClickEvent);

        verify(mockClickEvent).setCancelled(true);
    }

    @Test
    @DisplayName("onInventoryClick: トップインベントリ以外は無視")
    void testOnInventoryClick_NotTopInventory() {
        Inventory mockTopInventory = mock(Inventory.class);
        when(mockClickEvent.getClickedInventory()).thenReturn(mockInventory);
        when(mockInventoryView.getTopInventory()).thenReturn(mockTopInventory);
        // 異なるインベントリオブジェクトを返す

        listener.onInventoryClick(mockClickEvent);

        // キャンセルはされるが、以降の処理は行われない
        verify(mockClickEvent).setCancelled(true);
        verify(mockSkillTreeGUI, never()).acquireSkill(anyString());
    }

    @Test
    @DisplayName("onInventoryClick: GUIがnullの場合はインベントリを閉じる")
    void testOnInventoryClick_NullGUI() {
        mockedSkillTreeGUI.when(() -> SkillTreeGUI.getOpenGUI(mockPlayer)).thenReturn(null);

        listener.onInventoryClick(mockClickEvent);

        verify(mockPlayer).closeInventory();
    }

    @Test
    @DisplayName("onInventoryClick: スキルスロットの左クリックで習得")
    void testOnInventoryClick_LeftClickSkillSlot() {
        mockedSkillTreeGUI.when(() -> SkillTreeGUI.getOpenGUI(mockPlayer)).thenReturn(mockSkillTreeGUI);
        mockedSkillTreeGUI.when(() -> SkillTreeGUI.isSkillSlot(10)).thenReturn(true);
        when(mockSkillTreeGUI.getSkillIdFromSlot(10)).thenReturn("fireball");
        when(mockSkillTreeGUI.acquireSkill("fireball")).thenReturn(true);

        listener.onInventoryClick(mockClickEvent);

        verify(mockSkillTreeGUI).acquireSkill("fireball");
    }

    @Test
    @DisplayName("onInventoryClick: スキルスロットの右クリックで解除")
    void testOnInventoryClick_RightClickSkillSlot() {
        mockedSkillTreeGUI.when(() -> SkillTreeGUI.getOpenGUI(mockPlayer)).thenReturn(mockSkillTreeGUI);
        mockedSkillTreeGUI.when(() -> SkillTreeGUI.isSkillSlot(10)).thenReturn(true);
        when(mockClickEvent.isRightClick()).thenReturn(true);
        when(mockSkillTreeGUI.getSkillIdFromSlot(10)).thenReturn("fireball");
        when(mockSkillTreeGUI.refundSkill("fireball")).thenReturn(true);

        listener.onInventoryClick(mockClickEvent);

        verify(mockSkillTreeGUI).refundSkill("fireball");
    }

    @Test
    @DisplayName("onInventoryClick: シフトクリックも右クリック扱い")
    void testOnInventoryClick_ShiftClickSkillSlot() {
        mockedSkillTreeGUI.when(() -> SkillTreeGUI.getOpenGUI(mockPlayer)).thenReturn(mockSkillTreeGUI);
        mockedSkillTreeGUI.when(() -> SkillTreeGUI.isSkillSlot(10)).thenReturn(true);
        when(mockClickEvent.isShiftClick()).thenReturn(true);
        when(mockSkillTreeGUI.getSkillIdFromSlot(10)).thenReturn("fireball");
        when(mockSkillTreeGUI.refundSkill("fireball")).thenReturn(true);

        listener.onInventoryClick(mockClickEvent);

        verify(mockSkillTreeGUI).refundSkill("fireball");
    }

    @Test
    @DisplayName("onInventoryClick: 閉じるボタン(スロット49)でインベントリを閉じる")
    void testOnInventoryClick_CloseButton() {
        mockedSkillTreeGUI.when(() -> SkillTreeGUI.getOpenGUI(mockPlayer)).thenReturn(mockSkillTreeGUI);
        when(mockClickEvent.getSlot()).thenReturn(49);

        listener.onInventoryClick(mockClickEvent);

        verify(mockPlayer).closeInventory();
    }

    @Test
    @DisplayName("onInventoryClick: スキルIDがnullの場合は何もしない")
    void testOnInventoryClick_NullSkillId() {
        mockedSkillTreeGUI.when(() -> SkillTreeGUI.getOpenGUI(mockPlayer)).thenReturn(mockSkillTreeGUI);
        mockedSkillTreeGUI.when(() -> SkillTreeGUI.isSkillSlot(10)).thenReturn(true);
        when(mockSkillTreeGUI.getSkillIdFromSlot(10)).thenReturn(null);

        listener.onInventoryClick(mockClickEvent);

        verify(mockSkillTreeGUI, never()).acquireSkill(anyString());
        verify(mockSkillTreeGUI, never()).refundSkill(anyString());
    }

    @Test
    @DisplayName("onInventoryClick: スキルスロット外の場合は何もしない")
    void testOnInventoryClick_NotSkillSlot() {
        mockedSkillTreeGUI.when(() -> SkillTreeGUI.getOpenGUI(mockPlayer)).thenReturn(mockSkillTreeGUI);
        mockedSkillTreeGUI.when(() -> SkillTreeGUI.isSkillSlot(10)).thenReturn(false);

        listener.onInventoryClick(mockClickEvent);

        verify(mockSkillTreeGUI, never()).acquireSkill(anyString());
        verify(mockSkillTreeGUI, never()).refundSkill(anyString());
    }

    // ==================== InventoryCloseEvent テスト ====================

    @Test
    @DisplayName("onInventoryClose: プレイヤー以外は無視")
    void testOnInventoryClose_NotPlayer() {
        // プレイヤー以外をシミュレート（nullを返す）
        when(mockCloseEvent.getPlayer()).thenReturn(null);

        // nullの場合は例外が投げられず、単に無視される
        assertDoesNotThrow(() -> listener.onInventoryClose(mockCloseEvent));

        // GUIのclose()が呼ばれないことを確認
        verify(mockSkillTreeGUI, never()).close();
    }

    @Test
    @DisplayName("onInventoryClose: スキルツリーGUI以外は無視")
    void testOnInventoryClose_NotSkillTreeGUI() {
        when(mockInventoryView.title()).thenReturn(Component.text("Other Inventory"));

        listener.onInventoryClose(mockCloseEvent);

        verify(mockSkillTreeGUI, never()).close();
    }

    @Test
    @DisplayName("onInventoryClose: スキルツリーGUIが閉じられたらGUIをクリーンアップ")
    void testOnInventoryClose_CleanupGUI() {
        mockedSkillTreeGUI.when(() -> SkillTreeGUI.getOpenGUI(mockPlayer)).thenReturn(mockSkillTreeGUI);

        listener.onInventoryClose(mockCloseEvent);

        verify(mockSkillTreeGUI).close();
    }

    @Test
    @DisplayName("onInventoryClose: GUIがnullでも例外が投げられない")
    void testOnInventoryClose_NullGUI() {
        mockedSkillTreeGUI.when(() -> SkillTreeGUI.getOpenGUI(mockPlayer)).thenReturn(null);

        assertDoesNotThrow(() -> listener.onInventoryClose(mockCloseEvent));
    }
}
