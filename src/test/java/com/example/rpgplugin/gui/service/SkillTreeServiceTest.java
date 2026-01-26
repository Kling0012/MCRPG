package com.example.rpgplugin.gui.service;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillNode;
import com.example.rpgplugin.skill.SkillTree;
import com.example.rpgplugin.skill.SkillTreeRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SkillTreeServiceの単体テスト
 *
 * <p>スキルツリーGUIのビジネスロジックを検証します。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("SkillTreeService テスト")
@ExtendWith(MockitoExtension.class)
class SkillTreeServiceTest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private SkillManager mockSkillManager;

    @Mock
    private SkillTreeRegistry mockTreeRegistry;

    @Mock
    private SkillTree mockSkillTree;

    @Mock
    private Skill mockSkill;

    @Mock
    private SkillNode mockSkillNode;

    @Mock
    private Player mockPlayer;

    private SkillTreeService service;

    private static final String CLASS_ID = "warrior";
    private static final String SKILL_ID = "slash";
    private static final String SKILL_NAME = "スラッシュ";
    private static final String PARENT_SKILL_ID = "attack";

    @BeforeEach
    void setUp() {
        lenient().when(mockPlugin.getSkillManager()).thenReturn(mockSkillManager);
        lenient().when(mockSkillManager.getTreeRegistry()).thenReturn(mockTreeRegistry);
        lenient().when(mockTreeRegistry.getTree(CLASS_ID)).thenReturn(mockSkillTree);
        lenient().when(mockSkillManager.getSkill(SKILL_ID)).thenReturn(mockSkill);
        lenient().when(mockSkillManager.getSkill(PARENT_SKILL_ID)).thenReturn(mockSkill);
        lenient().when(mockSkillTree.getNode(SKILL_ID)).thenReturn(mockSkillNode);
        lenient().when(mockSkillTree.getNode(PARENT_SKILL_ID)).thenReturn(mockSkillNode);

        // デフォルト値を設定（個別テストで上書き可能）
        lenient().when(mockSkill.getMaxLevel()).thenReturn(5);
        lenient().when(mockSkill.getColoredDisplayName()).thenReturn(SKILL_NAME);

        service = new SkillTreeService(mockPlugin);
    }

    @AfterEach
    void tearDown() {
        reset(mockPlugin, mockSkillManager, mockTreeRegistry, mockSkillTree, mockSkill, mockSkillNode, mockPlayer);
    }

    // ==================== getSkillLevel テスト ====================

    @Test
    @DisplayName("getSkillLevel: SkillManagerに委譲")
    void testGetSkillLevel_DelegatesToSkillManager() {
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, SKILL_ID)).thenReturn(5);

        int result = service.getSkillLevel(mockPlayer, SKILL_ID);

        assertEquals(5, result, "SkillManagerの値が返される");
        verify(mockSkillManager).getSkillLevel(mockPlayer, SKILL_ID);
    }

    // ==================== getAvailableSkillPoints テスト ====================

    @Test
    @DisplayName("getAvailableSkillPoints: レベル1で未習得の場合、6ポイント")
    void testGetAvailableSkillPoints_Level1NoSkills() {
        lenient().when(mockPlayer.getLevel()).thenReturn(1);
        lenient().when(mockSkillManager.getSkillsForClass(CLASS_ID)).thenReturn(new ArrayList<>());
        lenient().when(mockSkillManager.getSkillLevel(any(Player.class), anyString())).thenReturn(0);

        int result = service.getAvailableSkillPoints(mockPlayer, CLASS_ID);

        assertEquals(6, result, "レベル1 + 5 = 6ポイント");
    }

    @Test
    @DisplayName("getAvailableSkillPoints: レベル10でスキルを習得済みの場合")
    void testGetAvailableSkillPoints_Level10WithSkills() {
        lenient().when(mockPlayer.getLevel()).thenReturn(10);

        // 習得済みスキルのモック
        Skill learnedSkill = createMockSkill("fireball");
        Skill anotherSkill = createMockSkill("shield");

        List<Skill> skills = List.of(learnedSkill, anotherSkill);
        when(mockSkillManager.getSkillsForClass(CLASS_ID)).thenReturn(skills);

        // 習得状況: fireball Lv2, shield Lv1
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, "fireball")).thenReturn(2);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, "shield")).thenReturn(1);

        // スキルツリーからコストを取得
        lenient().when(mockSkillTree.getCost("fireball")).thenReturn(3);
        lenient().when(mockSkillTree.getCost("shield")).thenReturn(2);

        int result = service.getAvailableSkillPoints(mockPlayer, CLASS_ID);

        // 基礎: 10 + 5 = 15
        // 使用: 3*2 + 2*1 = 8
        // 残り: 15 - 8 = 7
        assertEquals(7, result, "正しく計算される");
    }

    @Test
    @DisplayName("getAvailableSkillPoints: スキルツリーがない場合は0")
    void testGetAvailableSkillPoints_NoSkillTree() {
        lenient().when(mockPlayer.getLevel()).thenReturn(10);
        lenient().when(mockTreeRegistry.getTree(CLASS_ID)).thenReturn(null);

        int result = service.getAvailableSkillPoints(mockPlayer, CLASS_ID);

        assertEquals(0, result, "スキルツリーがない場合は0");
    }

    @Test
    @DisplayName("getAvailableSkillPoints: 使用ポイントが基礎ポイントを超える場合は0")
    void testGetAvailableSkillPoints_UsedExceedsBase() {
        lenient().when(mockPlayer.getLevel()).thenReturn(1);

        // 高コストスキルを習得済みとしてモック
        Skill expensiveSkill = createMockSkill("expensive");
        when(mockSkillManager.getSkillsForClass(CLASS_ID)).thenReturn(List.of(expensiveSkill));
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, "expensive")).thenReturn(1);
        lenient().when(mockSkillTree.getCost("expensive")).thenReturn(100);

        int result = service.getAvailableSkillPoints(mockPlayer, CLASS_ID);

        assertEquals(0, result, "負数にはならない");
    }

    // ==================== canAcquireSkill テスト ====================

    @Test
    @DisplayName("canAcquireSkill: スキルがない場合はfalse")
    void testCanAcquireSkill_NoSkill() {
        lenient().when(mockSkillManager.getSkill(SKILL_ID)).thenReturn(null);

        boolean result = service.canAcquireSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertFalse(result, "スキルがない場合はfalse");
    }

    @Test
    @DisplayName("canAcquireSkill: 最大レベルに達している場合はfalse")
    void testCanAcquireSkill_MaxLevelReached() {
        lenient().when(mockSkill.getMaxLevel()).thenReturn(5);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, SKILL_ID)).thenReturn(5);

        boolean result = service.canAcquireSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertFalse(result, "最大レベルの場合はfalse");
    }

    @Test
    @DisplayName("canAcquireSkill: 親スキル未習得の場合はfalse")
    void testCanAcquireSkill_ParentNotAcquired() {
        lenient().when(mockSkill.getMaxLevel()).thenReturn(5);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, SKILL_ID)).thenReturn(0);

        lenient().when(mockSkillNode.isRoot()).thenReturn(false);
        lenient().when(mockSkillTree.getParentSkillId(SKILL_ID)).thenReturn(PARENT_SKILL_ID);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, PARENT_SKILL_ID)).thenReturn(0);

        boolean result = service.canAcquireSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertFalse(result, "親スキル未習得の場合はfalse");
    }

    @Test
    @DisplayName("canAcquireSkill: 親スキル習得済みの場合はtrue")
    void testCanAcquireSkill_ParentAcquired() {
        lenient().when(mockSkill.getMaxLevel()).thenReturn(5);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, SKILL_ID)).thenReturn(0);

        lenient().when(mockSkillNode.isRoot()).thenReturn(false);
        lenient().when(mockSkillTree.getParentSkillId(SKILL_ID)).thenReturn(PARENT_SKILL_ID);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, PARENT_SKILL_ID)).thenReturn(1);

        boolean result = service.canAcquireSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertTrue(result, "親スキル習得済みの場合はtrue");
    }

    @Test
    @DisplayName("canAcquireSkill: ルートスキルの場合は親チェックなし")
    void testCanAcquireSkill_RootSkill() {
        lenient().when(mockSkill.getMaxLevel()).thenReturn(5);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, SKILL_ID)).thenReturn(0);

        lenient().when(mockSkillNode.isRoot()).thenReturn(true);

        boolean result = service.canAcquireSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertTrue(result, "ルートスキルの場合はtrue");
    }

    @Test
    @DisplayName("canAcquireSkill: スキルツリーがない場合はtrue")
    void testCanAcquireSkill_NoSkillTree() {
        lenient().when(mockSkill.getMaxLevel()).thenReturn(5);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, SKILL_ID)).thenReturn(0);
        lenient().when(mockTreeRegistry.getTree(CLASS_ID)).thenReturn(null);

        boolean result = service.canAcquireSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertTrue(result, "スキルツリーがない場合はtrue");
    }

    // ==================== acquireSkill テスト ====================

    @Test
    @DisplayName("acquireSkill: スキルがない場合は失敗")
    void testAcquireSkill_NoSkill() {
        lenient().when(mockSkillManager.getSkill(SKILL_ID)).thenReturn(null);

        var result = service.acquireSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertFalse(result.isSuccess(), "失敗する");
        assertTrue(result.getMessage().contains("見つかりません"), "エラーメッセージが正しい");
    }

    @Test
    @DisplayName("acquireSkill: スキルポイント不足の場合は失敗")
    void testAcquireSkill_NotEnoughPoints() {
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, SKILL_ID)).thenReturn(0);
        lenient().when(mockSkillTree.getCost(SKILL_ID)).thenReturn(10);
        lenient().when(mockPlayer.getLevel()).thenReturn(1); // 基礎ポイント6

        var result = service.acquireSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertFalse(result.isSuccess(), "失敗する");
        assertTrue(result.getMessage().contains("足りません"), "エラーメッセージが正しい");
    }

    @Test
    @DisplayName("acquireSkill: 習得条件を満たしていない場合は失敗")
    void testAcquireSkill_CannotAcquire() {
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, SKILL_ID)).thenReturn(0);
        when(mockSkillTree.getCost(SKILL_ID)).thenReturn(1);
        lenient().when(mockPlayer.getLevel()).thenReturn(10); // 十分なポイント
        lenient().when(mockSkillManager.getSkillsForClass(CLASS_ID)).thenReturn(new ArrayList<>());

        // canAcquireSkillがfalseを返すように設定
        lenient().when(mockSkillNode.isRoot()).thenReturn(false);
        lenient().when(mockSkillTree.getParentSkillId(SKILL_ID)).thenReturn(PARENT_SKILL_ID);
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, PARENT_SKILL_ID)).thenReturn(0);

        var result = service.acquireSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertFalse(result.isSuccess(), "失敗する");
        assertTrue(result.getMessage().contains("習得条件"), "エラーメッセージが正しい");
    }

    @Test
    @DisplayName("acquireSkill: 新規習得成功")
    void testAcquireSkill_NewAcquireSuccess() {
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, SKILL_ID)).thenReturn(0);
        lenient().when(mockSkillTree.getCost(SKILL_ID)).thenReturn(5);
        lenient().when(mockPlayer.getLevel()).thenReturn(10); // 基礎ポイント15
        lenient().when(mockSkillManager.getSkillsForClass(CLASS_ID)).thenReturn(new ArrayList<>());

        lenient().when(mockSkillNode.isRoot()).thenReturn(true);
        lenient().when(mockSkill.getColoredDisplayName()).thenReturn(SKILL_NAME); // Stringを返す

        lenient().when(mockSkillManager.acquireSkill(mockPlayer, SKILL_ID, 1)).thenReturn(true);

        var result = service.acquireSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertTrue(result.isSuccess(), "成功する");
        assertEquals(SKILL_NAME, result.getSkillName(), "スキル名が正しい");
        assertEquals(1, result.getNewLevel(), "レベル1");
        verify(mockSkillManager).acquireSkill(mockPlayer, SKILL_ID, 1);
    }

    @Test
    @DisplayName("acquireSkill: レベルアップ成功")
    void testAcquireSkill_LevelUpSuccess() {
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, SKILL_ID)).thenReturn(2);
        lenient().when(mockSkill.getMaxLevel()).thenReturn(5);
        lenient().when(mockSkillTree.getCost(SKILL_ID)).thenReturn(5);
        lenient().when(mockPlayer.getLevel()).thenReturn(20); // 基礎ポイント25
        lenient().when(mockSkillManager.getSkillsForClass(CLASS_ID)).thenReturn(new ArrayList<>());

        lenient().when(mockSkillNode.isRoot()).thenReturn(true);
        lenient().when(mockSkill.getColoredDisplayName()).thenReturn(SKILL_NAME); // Stringを返す

        lenient().when(mockSkillManager.upgradeSkill(mockPlayer, SKILL_ID)).thenReturn(true);

        var result = service.acquireSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertTrue(result.isSuccess(), "成功する");
        assertEquals(SKILL_NAME, result.getSkillName(), "スキル名が正しい");
        assertEquals(3, result.getNewLevel(), "レベル3");
        verify(mockSkillManager).upgradeSkill(mockPlayer, SKILL_ID);
    }

    @Test
    @DisplayName("acquireSkill: 最大レベルに達している場合は失敗")
    void testAcquireSkill_AlreadyMaxLevel() {
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, SKILL_ID)).thenReturn(5);
        lenient().when(mockSkill.getMaxLevel()).thenReturn(5);
        lenient().when(mockSkillTree.getCost(SKILL_ID)).thenReturn(1);
        lenient().when(mockPlayer.getLevel()).thenReturn(20);
        lenient().when(mockSkillManager.getSkillsForClass(CLASS_ID)).thenReturn(new ArrayList<>());

        lenient().when(mockSkillNode.isRoot()).thenReturn(true);

        var result = service.acquireSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertFalse(result.isSuccess(), "失敗する");
        // canAcquireSkillが先に最大レベルチェックを行うため「習得条件を満たしていません」になる
        assertTrue(result.getMessage().contains("習得条件"), "エラーメッセージが正しい");
        verify(mockSkillManager, never()).acquireSkill(any(Player.class), anyString(), anyInt());
        verify(mockSkillManager, never()).upgradeSkill(any(Player.class), anyString());
    }

    @Test
    @DisplayName("acquireSkill: acquireSkillが失敗した場合")
    void testAcquireSkill_AcquireFailed() {
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, SKILL_ID)).thenReturn(0);
        lenient().when(mockSkillTree.getCost(SKILL_ID)).thenReturn(5);
        lenient().when(mockPlayer.getLevel()).thenReturn(10);
        lenient().when(mockSkillManager.getSkillsForClass(CLASS_ID)).thenReturn(new ArrayList<>());

        lenient().when(mockSkillNode.isRoot()).thenReturn(true);

        lenient().when(mockSkillManager.acquireSkill(mockPlayer, SKILL_ID, 1)).thenReturn(false);

        var result = service.acquireSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertFalse(result.isSuccess(), "失敗する");
        assertTrue(result.getMessage().contains("習得に失敗"), "エラーメッセージが正しい");
    }

    // ==================== refundSkill テスト ====================

    @Test
    @DisplayName("refundSkill: スキルがない場合は失敗")
    void testRefundSkill_NoSkill() {
        lenient().when(mockSkillManager.getSkill(SKILL_ID)).thenReturn(null);

        var result = service.refundSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertFalse(result.isSuccess(), "失敗する");
        assertTrue(result.getMessage().contains("見つかりません"), "エラーメッセージが正しい");
    }

    @Test
    @DisplayName("refundSkill: 未習得の場合は失敗")
    void testRefundSkill_NotAcquired() {
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, SKILL_ID)).thenReturn(0);

        var result = service.refundSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertFalse(result.isSuccess(), "失敗する");
        assertTrue(result.getMessage().contains("習得していません"), "エラーメッセージが正しい");
    }

    @Test
    @DisplayName("refundSkill: 子スキルが習得されている場合は失敗")
    void testRefundSkill_HasChildSkills() {
        when(mockSkillManager.getSkillLevel(mockPlayer, SKILL_ID)).thenReturn(1);

        lenient().when(mockSkillNode.isLeaf()).thenReturn(false);

        // 子スキルモック
        Skill childSkill = createMockSkill("child_skill");
        SkillNode childNode = createMockSkillNode(childSkill);
        lenient().when(mockSkillNode.getChildren()).thenReturn(List.of(childNode));
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, "child_skill")).thenReturn(1);

        var result = service.refundSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertFalse(result.isSuccess(), "失敗する");
        assertTrue(result.getMessage().contains("子スキル"), "エラーメッセージが正しい");
    }

    @Test
    @DisplayName("refundSkill: レベル1を完全解除")
    void testRefundSkill_RemoveCompletely() {
        when(mockSkillManager.getSkillLevel(mockPlayer, SKILL_ID)).thenReturn(1);

        lenient().when(mockSkillNode.isLeaf()).thenReturn(true);
        lenient().when(mockSkill.getColoredDisplayName()).thenReturn(SKILL_NAME); // Stringを返す

        var playerSkillData = mock(com.example.rpgplugin.skill.SkillManager.PlayerSkillData.class);
        lenient().when(mockSkillManager.getPlayerSkillData(mockPlayer)).thenReturn(playerSkillData);

        var result = service.refundSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertTrue(result.isSuccess(), "成功する");
        assertEquals(SKILL_NAME, result.getSkillName(), "スキル名が正しい");
        assertEquals(0, result.getNewLevel(), "レベル0");
        verify(playerSkillData).removeSkill(SKILL_ID);
    }

    @Test
    @DisplayName("refundSkill: レベル2以上をレベルダウン")
    void testRefundSkill_LevelDown() {
        lenient().when(mockSkillManager.getSkillLevel(mockPlayer, SKILL_ID)).thenReturn(3);

        lenient().when(mockSkillNode.isLeaf()).thenReturn(true);
        lenient().when(mockSkill.getColoredDisplayName()).thenReturn(SKILL_NAME); // Stringを返す

        var playerSkillData = mock(com.example.rpgplugin.skill.SkillManager.PlayerSkillData.class);
        lenient().when(mockSkillManager.getPlayerSkillData(mockPlayer)).thenReturn(playerSkillData);

        var result = service.refundSkill(mockPlayer, SKILL_ID, CLASS_ID);

        assertTrue(result.isSuccess(), "成功する");
        assertEquals(SKILL_NAME, result.getSkillName(), "スキル名が正しい");
        assertEquals(2, result.getNewLevel(), "レベル2に低下");
        verify(playerSkillData).setSkillLevel(SKILL_ID, 2);
    }

    // ==================== SkillAcquireResult テスト ====================

    @Test
    @DisplayName("SkillAcquireResult: successファクトリメソッド")
    void testSkillAcquireResult_Success() {
        var result = SkillTreeService.SkillAcquireResult.success("テストスキル", 5);

        assertTrue(result.isSuccess(), "成功フラグ");
        assertNull(result.getMessage(), "メッセージはnull");
        assertEquals("テストスキル", result.getSkillName(), "スキル名");
        assertEquals(5, result.getNewLevel(), "新しいレベル");
    }

    @Test
    @DisplayName("SkillAcquireResult: failureファクトリメソッド")
    void testSkillAcquireResult_Failure() {
        var result = SkillTreeService.SkillAcquireResult.failure("エラーメッセージ");

        assertFalse(result.isSuccess(), "失敗フラグ");
        assertEquals("エラーメッセージ", result.getMessage(), "エラーメッセージ");
        assertNull(result.getSkillName(), "スキル名はnull");
        assertEquals(0, result.getNewLevel(), "レベル0");
    }

    @Test
    @DisplayName("SkillAcquireResult: sendMessageTo（成功時）")
    void testSkillAcquireResult_SendMessageTo_Success() {
        var result = SkillTreeService.SkillAcquireResult.success("テストスキル", 3);

        result.sendMessageTo(mockPlayer);

        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("SkillAcquireResult: sendMessageTo（失敗時）")
    void testSkillAcquireResult_SendMessageTo_Failure() {
        var result = SkillTreeService.SkillAcquireResult.failure("テストエラー");

        result.sendMessageTo(mockPlayer);

        verify(mockPlayer).sendMessage(any(Component.class));
    }

    // ==================== SkillRefundResult テスト ====================

    @Test
    @DisplayName("SkillRefundResult: successファクトリメソッド")
    void testSkillRefundResult_Success() {
        var result = SkillTreeService.SkillRefundResult.success("テストスキル", 2);

        assertTrue(result.isSuccess(), "成功フラグ");
        assertNull(result.getMessage(), "メッセージはnull");
        assertEquals("テストスキル", result.getSkillName(), "スキル名");
        assertEquals(2, result.getNewLevel(), "新しいレベル");
    }

    @Test
    @DisplayName("SkillRefundResult: failureファクトリメソッド")
    void testSkillRefundResult_Failure() {
        var result = SkillTreeService.SkillRefundResult.failure("エラーメッセージ");

        assertFalse(result.isSuccess(), "失敗フラグ");
        assertEquals("エラーメッセージ", result.getMessage(), "エラーメッセージ");
        assertNull(result.getSkillName(), "スキル名はnull");
        assertEquals(0, result.getNewLevel(), "レベル0");
    }

    @Test
    @DisplayName("SkillRefundResult: sendMessageTo（完全解除時）")
    void testSkillRefundResult_SendMessageTo_Remove() {
        var result = SkillTreeService.SkillRefundResult.success("テストスキル", 0);

        result.sendMessageTo(mockPlayer);

        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("SkillRefundResult: sendMessageTo（レベルダウン時）")
    void testSkillRefundResult_SendMessageTo_LevelDown() {
        var result = SkillTreeService.SkillRefundResult.success("テストスキル", 2);

        result.sendMessageTo(mockPlayer);

        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("SkillRefundResult: sendMessageTo（失敗時）")
    void testSkillRefundResult_SendMessageTo_Failure() {
        var result = SkillTreeService.SkillRefundResult.failure("テストエラー");

        result.sendMessageTo(mockPlayer);

        verify(mockPlayer).sendMessage(any(Component.class));
    }

    // ==================== ヘルパーメソッド ====================

    private Skill createMockSkill(String id) {
        Skill skill = mock(Skill.class);
        lenient().when(skill.getId()).thenReturn(id);
        return skill;
    }

    private SkillNode createMockSkillNode(Skill skill) {
        SkillNode node = mock(SkillNode.class);
        lenient().when(node.getSkill()).thenReturn(skill);
        return node;
    }
}
