package com.example.rpgplugin.skill.repository;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PlayerSkillServiceの単体テスト
 *
 * <p>プレイヤースキルサービスの機能テスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("PlayerSkillService テスト")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerSkillServiceTest {

    @Mock
    private Player mockPlayer;

    private PlayerSkillService service;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        service = new PlayerSkillService();
        testUuid = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(testUuid);
    }

    // ==================== getPlayerSkillData テスト ====================

    @Test
    @DisplayName("getPlayerSkillData(Player): 新規データを作成")
    void testGetPlayerSkillData_Player_New() {
        PlayerSkillService.PlayerSkillData data = service.getPlayerSkillData(mockPlayer);

        assertNotNull(data, "データが作成されること");
        assertEquals(0, data.getSkillLevel("test_skill"), "初期スキルレベルは0");
    }

    @Test
    @DisplayName("getPlayerSkillData(Player): 既存データを返す")
    void testGetPlayerSkillData_Player_Existing() {
        PlayerSkillService.PlayerSkillData data1 = service.getPlayerSkillData(mockPlayer);
        data1.setSkillLevel("test_skill", 5);

        PlayerSkillService.PlayerSkillData data2 = service.getPlayerSkillData(mockPlayer);

        assertSame(data1, data2, "同じインスタンスが返されること");
        assertEquals(5, data2.getSkillLevel("test_skill"), "スキルレベルが保持されていること");
    }

    @Test
    @DisplayName("getPlayerSkillData(UUID): 新規データを作成")
    void testGetPlayerSkillData_Uuid_New() {
        PlayerSkillService.PlayerSkillData data = service.getPlayerSkillData(testUuid);

        assertNotNull(data, "データが作成されること");
        assertEquals(0, data.getSkillLevel("test_skill"), "初期スキルレベルは0");
    }

    @Test
    @DisplayName("getPlayerSkillData(UUID): 既存データを返す")
    void testGetPlayerSkillData_Uuid_Existing() {
        PlayerSkillService.PlayerSkillData data1 = service.getPlayerSkillData(testUuid);
        data1.setSkillLevel("test_skill", 3);

        PlayerSkillService.PlayerSkillData data2 = service.getPlayerSkillData(testUuid);

        assertSame(data1, data2, "同じインスタンスが返されること");
        assertEquals(3, data2.getSkillLevel("test_skill"), "スキルレベルが保持されていること");
    }

    // ==================== hasPlayerSkillData テスト ====================

    @Test
    @DisplayName("hasPlayerSkillData: データ作成後はtrue")
    void testHasPlayerSkillData_AfterCreate() {
        service.getPlayerSkillData(testUuid);
        assertTrue(service.hasPlayerSkillData(testUuid), "データ作成後はtrue");
    }

    @Test
    @DisplayName("hasPlayerSkillData: 未作成はfalse")
    void testHasPlayerSkillData_NotCreated() {
        assertFalse(service.hasPlayerSkillData(testUuid), "未作成時はfalse");
    }

    // ==================== unloadPlayerData テスト ====================

    @Test
    @DisplayName("unloadPlayerData: データを削除")
    void testUnloadPlayerData() {
        service.getPlayerSkillData(testUuid);
        PlayerSkillService.PlayerSkillData unloaded = service.unloadPlayerData(testUuid);

        assertNotNull(unloaded, "削除されたデータが返されること");
        assertFalse(service.hasPlayerSkillData(testUuid), "データが削除されていること");
    }

    @Test
    @DisplayName("unloadPlayerData: 未存在時はnull")
    void testUnloadPlayerData_NotExists() {
        PlayerSkillService.PlayerSkillData unloaded = service.unloadPlayerData(testUuid);
        assertNull(unloaded, "未存在時はnull");
    }

    // ==================== clearAllPlayerData テスト ====================

    @Test
    @DisplayName("clearAllPlayerData: 全データをクリア")
    void testClearAllPlayerData() {
        service.getPlayerSkillData(testUuid);
        service.getPlayerSkillData(UUID.randomUUID());
        assertEquals(2, service.size());

        service.clearAllPlayerData();

        assertEquals(0, service.size(), "全データがクリアされていること");
    }

    // ==================== size テスト ====================

    @Test
    @DisplayName("size: データ数を取得")
    void testSize() {
        assertEquals(0, service.size(), "初期状態は0");

        service.getPlayerSkillData(testUuid);
        assertEquals(1, service.size());

        service.getPlayerSkillData(UUID.randomUUID());
        assertEquals(2, service.size());
    }

    // ==================== hasSkill テスト ====================

    @Test
    @DisplayName("hasSkill(Player): 習得済みはtrue")
    void testHasSkill_Player_Acquired() {
        PlayerSkillService.PlayerSkillData data = service.getPlayerSkillData(mockPlayer);
        data.setSkillLevel("test_skill", 1);

        assertTrue(service.hasSkill(mockPlayer, "test_skill"), "習得済みはtrue");
    }

    @Test
    @DisplayName("hasSkill(Player): 未習得はfalse")
    void testHasSkill_Player_NotAcquired() {
        assertFalse(service.hasSkill(mockPlayer, "test_skill"), "未習得はfalse");
    }

    @Test
    @DisplayName("hasSkill(UUID): 習得済みはtrue")
    void testHasSkill_Uuid_Acquired() {
        PlayerSkillService.PlayerSkillData data = service.getPlayerSkillData(testUuid);
        data.setSkillLevel("test_skill", 1);

        assertTrue(service.hasSkill(testUuid, "test_skill"), "習得済みはtrue");
    }

    @Test
    @DisplayName("hasSkill(UUID): 未習得はfalse")
    void testHasSkill_Uuid_NotAcquired() {
        assertFalse(service.hasSkill(testUuid, "test_skill"), "未習得はfalse");
    }

    // ==================== getSkillLevel テスト ====================

    @Test
    @DisplayName("getSkillLevel(Player): 習得済みスキルのレベル")
    void testGetSkillLevel_Player_Acquired() {
        PlayerSkillService.PlayerSkillData data = service.getPlayerSkillData(mockPlayer);
        data.setSkillLevel("test_skill", 5);

        assertEquals(5, service.getSkillLevel(mockPlayer, "test_skill"), "正しいレベルが返されること");
    }

    @Test
    @DisplayName("getSkillLevel(Player): 未習得は0")
    void testGetSkillLevel_Player_NotAcquired() {
        assertEquals(0, service.getSkillLevel(mockPlayer, "test_skill"), "未習得は0");
    }

    @Test
    @DisplayName("getSkillLevel(UUID): 習得済みスキルのレベル")
    void testGetSkillLevel_Uuid_Acquired() {
        PlayerSkillService.PlayerSkillData data = service.getPlayerSkillData(testUuid);
        data.setSkillLevel("test_skill", 3);

        assertEquals(3, service.getSkillLevel(testUuid, "test_skill"), "正しいレベルが返されること");
    }

    @Test
    @DisplayName("getSkillLevel(UUID): 未習得は0")
    void testGetSkillLevel_Uuid_NotAcquired() {
        assertEquals(0, service.getSkillLevel(testUuid, "test_skill"), "未習得は0");
    }

    // ==================== acquireSkill テスト ====================

    @Test
    @DisplayName("acquireSkill: 新規習得")
    void testAcquireSkill_New() {
        boolean result = service.acquireSkill(mockPlayer, "test_skill", 1, 5, "テストスキル");

        assertTrue(result, "習得に成功すること");
        assertEquals(1, service.getSkillLevel(mockPlayer, "test_skill"), "スキルレベルが1であること");
        verify(mockPlayer).sendMessage(any(Component.class));
        // Adventure APIのComponent検証は複雑なため、メソッド呼び出しのみ検証
    }

    @Test
    @DisplayName("acquireSkill: より高いレベルで習得")
    void testAcquireSkill_HigherLevel() {
        PlayerSkillService.PlayerSkillData data = service.getPlayerSkillData(mockPlayer);
        data.setSkillLevel("test_skill", 2);

        boolean result = service.acquireSkill(mockPlayer, "test_skill", 5, 10, "テストスキル");

        assertTrue(result, "習得に成功すること");
        assertEquals(5, service.getSkillLevel(mockPlayer, "test_skill"), "スキルレベルが5であること");
    }

    @Test
    @DisplayName("acquireSkill: 低いレベルで習得は失敗")
    void testAcquireSkill_LowerLevel() {
        PlayerSkillService.PlayerSkillData data = service.getPlayerSkillData(mockPlayer);
        data.setSkillLevel("test_skill", 5);

        boolean result = service.acquireSkill(mockPlayer, "test_skill", 3, 10, "テストスキル");

        assertFalse(result, "習得に失敗すること");
        assertEquals(5, service.getSkillLevel(mockPlayer, "test_skill"), "レベルが変更されていないこと");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("acquireSkill: 同じレベルで習得は失敗")
    void testAcquireSkill_SameLevel() {
        PlayerSkillService.PlayerSkillData data = service.getPlayerSkillData(mockPlayer);
        data.setSkillLevel("test_skill", 3);

        boolean result = service.acquireSkill(mockPlayer, "test_skill", 3, 10, "テストスキル");

        assertFalse(result, "習得に失敗すること");
        assertEquals(3, service.getSkillLevel(mockPlayer, "test_skill"), "レベルが変更されていないこと");
    }

    @Test
    @DisplayName("acquireSkill: 最大レベル超過は失敗")
    void testAcquireSkill_ExceedsMaxLevel() {
        boolean result = service.acquireSkill(mockPlayer, "test_skill", 10, 5, "テストスキル");

        assertFalse(result, "習得に失敗すること");
        assertEquals(0, service.getSkillLevel(mockPlayer, "test_skill"), "習得されていないこと");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    // ==================== upgradeSkill テスト ====================

    @Test
    @DisplayName("upgradeSkill: スキル強化成功")
    void testUpgradeSkill_Success() {
        PlayerSkillService.PlayerSkillData data = service.getPlayerSkillData(mockPlayer);
        data.setSkillLevel("test_skill", 2);

        boolean result = service.upgradeSkill(mockPlayer, "test_skill", 5, "テストスキル");

        assertTrue(result, "強化に成功すること");
        assertEquals(3, service.getSkillLevel(mockPlayer, "test_skill"), "レベルが1上がっていること");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("upgradeSkill: 未習得は失敗")
    void testUpgradeSkill_NotAcquired() {
        boolean result = service.upgradeSkill(mockPlayer, "test_skill", 5, "テストスキル");

        assertFalse(result, "強化に失敗すること");
        assertEquals(0, service.getSkillLevel(mockPlayer, "test_skill"), "習得されていないこと");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("upgradeSkill: 最大レベル到達は失敗")
    void testUpgradeSkill_MaxLevelReached() {
        PlayerSkillService.PlayerSkillData data = service.getPlayerSkillData(mockPlayer);
        data.setSkillLevel("test_skill", 5);

        boolean result = service.upgradeSkill(mockPlayer, "test_skill", 5, "テストスキル");

        assertFalse(result, "強化に失敗すること");
        assertEquals(5, service.getSkillLevel(mockPlayer, "test_skill"), "レベルが変更されていないこと");
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    // ==================== clearRemovedSkillsFromAllPlayers テスト ====================

    @Test
    @DisplayName("clearRemovedSkillsFromAllPlayers: スキル削除")
    void testClearRemovedSkillsFromAllPlayers() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        service.getPlayerSkillData(player1).setSkillLevel("skill1", 1);
        service.getPlayerSkillData(player1).setSkillLevel("skill2", 1);
        service.getPlayerSkillData(player1).setSkillLevel("skill3", 1);
        service.getPlayerSkillData(player2).setSkillLevel("skill1", 1);
        service.getPlayerSkillData(player2).setSkillLevel("skill3", 1);

        int removed = service.clearRemovedSkillsFromAllPlayers(Set.of("skill1", "skill2"));

        assertEquals(3, removed, "3つのスキルが削除されること");
        assertFalse(service.hasSkill(player1, "skill1"), "player1のskill1が削除されている");
        assertFalse(service.hasSkill(player1, "skill2"), "player1のskill2が削除されている");
        assertTrue(service.hasSkill(player1, "skill3"), "player1のskill3は残っている");
        assertFalse(service.hasSkill(player2, "skill1"), "player2のskill1が削除されている");
        assertTrue(service.hasSkill(player2, "skill3"), "player2のskill3は残っている");
    }

    @Test
    @DisplayName("clearRemovedSkillsFromAllPlayers: 削除対象なし")
    void testClearRemovedSkillsFromAllPlayers_NoRemovals() {
        service.getPlayerSkillData(testUuid).setSkillLevel("skill1", 1);

        int removed = service.clearRemovedSkillsFromAllPlayers(Set.of("skill2"));

        assertEquals(0, removed, "削除数は0");
        assertTrue(service.hasSkill(testUuid, "skill1"), "skill1は残っている");
    }

    // ==================== cleanupRemovedSkills テスト ====================

    @Test
    @DisplayName("cleanupRemovedSkills: クリーンアップ実行")
    void testCleanupRemovedSkills() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        service.getPlayerSkillData(player1).setSkillLevel("removed1", 1);
        service.getPlayerSkillData(player2).setSkillLevel("removed1", 1);
        service.getPlayerSkillData(player2).setSkillLevel("removed2", 1);

        PlayerSkillService.CleanupSummary summary = service.cleanupRemovedSkills(Set.of("removed1", "removed2"));

        assertTrue(summary.hasChanges(), "変更があること");
        assertEquals(2, summary.getAffectedPlayers(), "2プレイヤーが影響を受けた");
        assertEquals(3, summary.getTotalSkillsRemoved(), "3つのスキルが削除された");
    }

    @Test
    @DisplayName("cleanupRemovedSkills: 変更なし")
    void testCleanupRemovedSkills_NoChanges() {
        service.getPlayerSkillData(testUuid).setSkillLevel("skill1", 1);

        PlayerSkillService.CleanupSummary summary = service.cleanupRemovedSkills(Set.of("removed"));

        assertFalse(summary.hasChanges(), "変更がないこと");
        assertEquals(0, summary.getAffectedPlayers(), "影響プレイヤーは0");
        assertEquals(0, summary.getTotalSkillsRemoved(), "削除スキルは0");
    }

    @Test
    @DisplayName("CleanupSummary: toString")
    void testCleanupSummary_ToString() {
        service.getPlayerSkillData(testUuid).setSkillLevel("removed", 1);
        PlayerSkillService.CleanupSummary summary = service.cleanupRemovedSkills(Set.of("removed"));

        String str = summary.toString();
        assertTrue(str.contains("affectedPlayers=1"));
        assertTrue(str.contains("totalSkillsRemoved=1"));
    }

    // ==================== getAllPlayerUuids テスト ====================

    @Test
    @DisplayName("getAllPlayerUuids: 全UUID取得")
    void testGetAllPlayerUuids() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();

        service.getPlayerSkillData(uuid1);
        service.getPlayerSkillData(uuid2);
        service.getPlayerSkillData(uuid3);

        Set<UUID> uuids = service.getAllPlayerUuids();

        assertEquals(3, uuids.size(), "3つのUUIDが取得できる");
        assertTrue(uuids.contains(uuid1));
        assertTrue(uuids.contains(uuid2));
        assertTrue(uuids.contains(uuid3));
    }

    @Test
    @DisplayName("getAllPlayerUuids: 変更不影响元")
    void testGetAllPlayerUuids_ModificationSafety() {
        service.getPlayerSkillData(testUuid);

        Set<UUID> uuids = service.getAllPlayerUuids();
        uuids.clear();

        assertEquals(1, service.size(), "取得したセットをクリアしても元には影響しない");
    }

    // ==================== PlayerSkillData テスト ====================

    @Test
    @DisplayName("PlayerSkillData: getAcquiredSkillsはコピー")
    void testPlayerSkillData_GetAcquiredSkills() {
        PlayerSkillService.PlayerSkillData data = new PlayerSkillService.PlayerSkillData();
        data.setSkillLevel("skill1", 1);
        data.setSkillLevel("skill2", 2);

        var skills = data.getAcquiredSkills();
        skills.clear();

        assertEquals(2, data.getAcquiredSkills().size(), "取得したマップをクリアしても元には影響しない");
    }

    @Test
    @DisplayName("PlayerSkillData: setSkillLevel(0以下)で削除")
    void testPlayerSkillData_SetSkillLevelZero() {
        PlayerSkillService.PlayerSkillData data = new PlayerSkillService.PlayerSkillData();
        data.setSkillLevel("skill1", 5);
        assertTrue(data.hasSkill("skill1"));

        data.setSkillLevel("skill1", 0);

        assertFalse(data.hasSkill("skill1"), "レベル0で削除されること");
    }

    @Test
    @DisplayName("PlayerSkillData: setSkillLevel(負)で削除")
    void testPlayerSkillData_SetSkillLevelNegative() {
        PlayerSkillService.PlayerSkillData data = new PlayerSkillService.PlayerSkillData();
        data.setSkillLevel("skill1", 5);
        assertTrue(data.hasSkill("skill1"));

        data.setSkillLevel("skill1", -1);

        assertFalse(data.hasSkill("skill1"), "負のレベルで削除されること");
    }

    @Test
    @DisplayName("PlayerSkillData: removeSkill")
    void testPlayerSkillData_RemoveSkill() {
        PlayerSkillService.PlayerSkillData data = new PlayerSkillService.PlayerSkillData();
        data.setSkillLevel("skill1", 1);
        data.setSkillLevel("skill2", 1);

        data.removeSkill("skill1");

        assertFalse(data.hasSkill("skill1"), "skill1が削除されている");
        assertTrue(data.hasSkill("skill2"), "skill2は残っている");
    }

    @Test
    @DisplayName("PlayerSkillData: removeSkills")
    void testPlayerSkillData_RemoveSkills() {
        PlayerSkillService.PlayerSkillData data = new PlayerSkillService.PlayerSkillData();
        data.setSkillLevel("skill1", 1);
        data.setSkillLevel("skill2", 1);
        data.setSkillLevel("skill3", 1);

        int removed = data.removeSkills(Set.of("skill1", "skill3"));

        assertEquals(2, removed, "2つ削除されている");
        assertFalse(data.hasSkill("skill1"));
        assertTrue(data.hasSkill("skill2"), "skill2は残っている");
        assertFalse(data.hasSkill("skill3"));
    }

    @Test
    @DisplayName("PlayerSkillData: getCooldownsはコピー")
    void testPlayerSkillData_GetCooldowns() {
        PlayerSkillService.PlayerSkillData data = new PlayerSkillService.PlayerSkillData();
        data.setCooldown("skill1", 1000L);

        var cooldowns = data.getCooldowns();
        cooldowns.clear();

        assertEquals(1000L, data.getLastCastTime("skill1"), "取得したマップをクリアしても元には影響しない");
    }

    @Test
    @DisplayName("PlayerSkillData: getLastCastTime")
    void testPlayerSkillData_GetLastCastTime() {
        PlayerSkillService.PlayerSkillData data = new PlayerSkillService.PlayerSkillData();
        data.setCooldown("skill1", 12345L);

        assertEquals(12345L, data.getLastCastTime("skill1"), "クールダウン時刻が取得できる");
        assertEquals(0L, data.getLastCastTime("unknown"), "未設定は0");
    }

    @Test
    @DisplayName("PlayerSkillData: setSkillPointsとaddSkillPoints")
    void testPlayerSkillData_SkillPoints() {
        PlayerSkillService.PlayerSkillData data = new PlayerSkillService.PlayerSkillData();

        data.setSkillPoints(10);
        assertEquals(10, data.getSkillPoints());

        data.addSkillPoints(5);
        assertEquals(15, data.getSkillPoints());
    }

    @Test
    @DisplayName("PlayerSkillData: setSkillPoints(負)は0に制限")
    void testPlayerSkillData_SetSkillPointsNegative() {
        PlayerSkillService.PlayerSkillData data = new PlayerSkillService.PlayerSkillData();
        data.setSkillPoints(10);

        data.setSkillPoints(-5);

        assertEquals(0, data.getSkillPoints(), "負の値は0に制限される");
    }

    @Test
    @DisplayName("PlayerSkillData: useSkillPoint")
    void testPlayerSkillData_UseSkillPoint() {
        PlayerSkillService.PlayerSkillData data = new PlayerSkillService.PlayerSkillData();
        data.setSkillPoints(3);

        assertTrue(data.useSkillPoint(), "使用成功");
        assertEquals(2, data.getSkillPoints());

        assertTrue(data.useSkillPoint());
        assertTrue(data.useSkillPoint());

        assertFalse(data.useSkillPoint(), "ポイント0で使用失敗");
        assertEquals(0, data.getSkillPoints());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("PlayerSkillData: 空スキルIDは習得していない")
    void testPlayerSkillData_HasSkill_EmptyOrInvalid(String skillId) {
        PlayerSkillService.PlayerSkillData data = new PlayerSkillService.PlayerSkillData();

        assertFalse(data.hasSkill(skillId), "空または無効なIDはfalse");
    }
}
