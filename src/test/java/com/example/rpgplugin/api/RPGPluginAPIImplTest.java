package com.example.rpgplugin.api;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.damage.DamageModifier;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.rpgclass.ClassManager;
import com.example.rpgplugin.rpgclass.RPGClass;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.executor.ActiveSkillExecutor;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RPGPluginAPIImpl の単体テストクラス
 *
 * <p>テスト戦略:</p>
 * <ul>
 *   <li>正常系: 有効なプレイヤー、有効なデータ</li>
 *   <li>異常系: null、空文字列、未登録プレイヤー</li>
 *   <li>境界値: 負数、0、最大値</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RPGPluginAPIImpl テスト")
class RPGPluginAPIImplTest {

    @Mock
    private RPGPlugin plugin;

    @Mock
    private java.util.logging.Logger logger;

    @Mock
    private PlayerManager playerManager;

    @Mock
    private ClassManager classManager;

    @Mock
    private SkillManager skillManager;

    @Mock
    private ActiveSkillExecutor activeSkillExecutor;

    @Mock
    private Player player;

    @Mock
    private RPGPlayer rpgPlayer;

    @Mock
    private World world;

    @Mock
    private Location location;

    @Mock
    private Entity entity;

    @Mock
    private StatManager statManager;

    private RPGPluginAPIImpl api;
    private UUID playerUuid;

    @BeforeEach
    void setUp() {
        // Loggerモックの設定（warningメソッドでNPEを防ぐ）
        doNothing().when(logger).warning(anyString());

        // プラグインモックの設定
        lenient().when(plugin.getLogger()).thenReturn(logger);
        lenient().when(plugin.getPlayerManager()).thenReturn(playerManager);
        lenient().when(plugin.getClassManager()).thenReturn(classManager);
        lenient().when(plugin.getSkillManager()).thenReturn(skillManager);
        lenient().when(plugin.getActiveSkillExecutor()).thenReturn(activeSkillExecutor);

        // プレイヤーモックの基本設定
        playerUuid = UUID.randomUUID();
        lenient().when(player.getUniqueId()).thenReturn(playerUuid);
        lenient().when(player.getWorld()).thenReturn(world);
        lenient().when(player.getLocation()).thenReturn(location);
        doNothing().when(player).sendMessage(anyString());

        // RPGPlayerモックの基本設定
        lenient().when(rpgPlayer.getStatManager()).thenReturn(statManager);
        lenient().when(statManager.getAllFinalStats()).thenReturn(new EnumMap<>(Stat.class));

        // APIインスタンス生成
        api = new RPGPluginAPIImpl(plugin);
    }

    // ==================== プレイヤーデータ取得 ====================

    @Nested
    @DisplayName("getRPGPlayer: プレイヤーデータ取得")
    class GetRPGPlayerTests {

        @Test
        @DisplayName("正常系: 有効なプレイヤーでRPGPlayerを取得")
        void testGetRPGPlayer_ValidPlayer_ReturnsRPGPlayer() {
            // Given
            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);

            // When
            Optional<RPGPlayer> result = api.getRPGPlayer(player);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(rpgPlayer);
            verify(playerManager).getRPGPlayer(playerUuid);
        }

        @Test
        @DisplayName("異常系: nullプレイヤーで空を返す")
        void testGetRPGPlayer_NullPlayer_ReturnsEmpty() {
            // When
            Optional<RPGPlayer> result = api.getRPGPlayer(null);

            // Then
            assertThat(result).isEmpty();
            verify(playerManager, never()).getRPGPlayer(any(UUID.class));
        }

        @Test
        @DisplayName("異常系: 未登録プレイヤーで空を返す")
        void testGetRPGPlayer_UnregisteredPlayer_ReturnsEmpty() {
            // Given
            when(playerManager.getRPGPlayer(playerUuid)).thenReturn(null);

            // When
            Optional<RPGPlayer> result = api.getRPGPlayer(player);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getLevel/setLevel: レベル操作")
    class LevelTests {

        @Test
        @DisplayName("正常系: レベルを取得")
        void testGetLevel_ValidPlayer_ReturnsLevel() {
            // Given
            when(player.getLevel()).thenReturn(10);

            // When
            int level = api.getLevel(player);

            // Then
            assertThat(level).isEqualTo(10);
        }

        @Test
        @DisplayName("正常系: レベルを設定")
        void testSetLevel_ValidPlayer_SetsLevel() {
            // When
            api.setLevel(player, 20);

            // Then
            verify(player).setLevel(20);
        }

        @Test
        @DisplayName("異常系: nullプレイヤーで0を返す")
        void testGetLevel_NullPlayer_ReturnsZero() {
            // When
            int level = api.getLevel(null);

            // Then
            assertThat(level).isZero();
        }

        @Test
        @DisplayName("異常系: nullプレイヤーで設定をスキップ")
        void testSetLevel_NullPlayer_DoesNothing() {
            // When
            api.setLevel(null, 20);

            // Then
            verify(player, never()).setLevel(anyInt());
        }

        @Test
        @DisplayName("境界値: 0を設定")
        void testSetLevel_Zero_SetsZero() {
            // When
            api.setLevel(player, 0);

            // Then
            verify(player).setLevel(0);
        }

        @Test
        @DisplayName("境界値: 負数を設定")
        void testSetLevel_Negative_SetsNegative() {
            // When
            api.setLevel(player, -1);

            // Then
            verify(player).setLevel(-1);
        }
    }

    @Nested
    @DisplayName("getStat/setStat: ステータス操作")
    class StatTests {

        @Test
        @DisplayName("正常系: ステータスを取得")
        void testGetStat_ValidPlayer_ReturnsStat() {
            // Given
            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);
            when(rpgPlayer.getFinalStat(Stat.STRENGTH)).thenReturn(15);

            // When
            int stat = api.getStat(player, Stat.STRENGTH);

            // Then
            assertThat(stat).isEqualTo(15);
        }

        @Test
        @DisplayName("正常系: ステータスを設定")
        void testSetStat_ValidPlayer_SetsStat() {
            // Given
            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);

            // When
            api.setStat(player, Stat.STRENGTH, 20);

            // Then
            verify(rpgPlayer).setBaseStat(Stat.STRENGTH, 20);
        }

        @Test
        @DisplayName("異常系: nullプレイヤーで0を返す")
        void testGetStat_NullPlayer_ReturnsZero() {
            // When
            int stat = api.getStat(null, Stat.STRENGTH);

            // Then
            assertThat(stat).isZero();
        }

        @Test
        @DisplayName("異常系: nullステータスで0を返す")
        void testGetStat_NullStat_ReturnsZero() {
            // When
            int stat = api.getStat(player, null);

            // Then
            assertThat(stat).isZero();
        }

        @Test
        @DisplayName("異常系: 未登録プレイヤーで0を返す")
        void testGetStat_UnregisteredPlayer_ReturnsZero() {
            // Given
            when(playerManager.getRPGPlayer(playerUuid)).thenReturn(null);

            // When
            int stat = api.getStat(player, Stat.STRENGTH);

            // Then
            assertThat(stat).isZero();
        }

        @Test
        @DisplayName("異常系: nullプレイヤーで設定をスキップ")
        void testSetStat_NullPlayer_DoesNothing() {
            // When
            api.setStat(null, Stat.STRENGTH, 20);

            // Then
            verify(rpgPlayer, never()).setBaseStat(any(), anyInt());
        }

        @Test
        @DisplayName("異常系: nullステータスで設定をスキップ")
        void testSetStat_NullStat_DoesNothing() {
            // Given
            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);

            // When
            api.setStat(player, null, 20);

            // Then
            verify(rpgPlayer, never()).setBaseStat(any(), anyInt());
        }

        @Test
        @DisplayName("境界値: 0を設定")
        void testSetStat_Zero_SetsZero() {
            // Given
            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);

            // When
            api.setStat(player, Stat.INTELLIGENCE, 0);

            // Then
            verify(rpgPlayer).setBaseStat(Stat.INTELLIGENCE, 0);
        }

        @Test
        @DisplayName("境界値: 負数を設定")
        void testSetStat_Negative_SetsNegative() {
            // Given
            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);

            // When
            api.setStat(player, Stat.DEXTERITY, -5);

            // Then
            verify(rpgPlayer).setBaseStat(Stat.DEXTERITY, -5);
        }
    }

    @Nested
    @DisplayName("getClassId: クラスID取得")
    class GetClassIdTests {

        @Test
        @DisplayName("正常系: クラスIDを取得")
        void testGetClassId_ValidPlayer_ReturnsClassId() {
            // Given
            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);
            when(rpgPlayer.getClassId()).thenReturn("warrior");

            // When
            String classId = api.getClassId(player);

            // Then
            assertThat(classId).isEqualTo("warrior");
        }

        @Test
        @DisplayName("異常系: nullプレイヤーでnullを返す")
        void testGetClassId_NullPlayer_ReturnsNull() {
            // When
            String classId = api.getClassId(null);

            // Then
            assertThat(classId).isNull();
        }

        @Test
        @DisplayName("異常系: 未登録プレイヤーでnullを返す")
        void testGetClassId_UnregisteredPlayer_ReturnsNull() {
            // Given
            when(playerManager.getRPGPlayer(playerUuid)).thenReturn(null);

            // When
            String classId = api.getClassId(player);

            // Then
            assertThat(classId).isNull();
        }
    }

    // ==================== ステータス操作 ====================

    @Nested
    @DisplayName("addStatPoints: ステータスポイント追加")
    class AddStatPointsTests {

        @Test
        @DisplayName("正常系: ポイントを追加")
        void testAddStatPoints_ValidPlayer_AddsPoints() {
            // Given
            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);

            // When
            api.addStatPoints(player, 5);

            // Then
            verify(rpgPlayer).addAvailablePoints(5);
        }

        @Test
        @DisplayName("異常系: nullプレイヤーで追加をスキップ")
        void testAddStatPoints_NullPlayer_DoesNothing() {
            // When
            api.addStatPoints(null, 5);

            // Then
            verify(rpgPlayer, never()).addAvailablePoints(anyInt());
        }

        @Test
        @DisplayName("異常系: 未登録プレイヤーで追加をスキップ")
        void testAddStatPoints_UnregisteredPlayer_DoesNothing() {
            // Given
            when(playerManager.getRPGPlayer(playerUuid)).thenReturn(null);

            // When
            api.addStatPoints(player, 5);

            // Then
            verify(rpgPlayer, never()).addAvailablePoints(anyInt());
        }

        @Test
        @DisplayName("異常系: 0以下のポイントで追加をスキップ")
        void testAddStatPoints_ZeroOrNegative_DoesNothing() {
            // Given
            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);

            // When
            api.addStatPoints(player, 0);
            api.addStatPoints(player, -1);

            // Then
            verify(rpgPlayer, never()).addAvailablePoints(anyInt());
        }
    }

    @Nested
    @DisplayName("getSkillPoints/setSkillPoints: スキルポイント操作")
    class SkillPointsTests {

        @Test
        @DisplayName("正常系: スキルポイントを取得")
        void testGetSkillPoints_ValidPlayer_ReturnsPoints() {
            // Given
            var skillData = mock(SkillManager.PlayerSkillData.class);
            when(skillData.getSkillPoints()).thenReturn(10);
            when(skillManager.getPlayerSkillData((org.bukkit.entity.Player)player)).thenReturn(skillData);

            // When
            int points = api.getSkillPoints(player);

            // Then
            assertThat(points).isEqualTo(10);
        }

        @Test
        @DisplayName("正常系: スキルポイントを設定")
        void testSetSkillPoints_ValidPlayer_SetsPoints() {
            // Given
            var skillData = mock(SkillManager.PlayerSkillData.class);
            when(skillManager.getPlayerSkillData((org.bukkit.entity.Player)player)).thenReturn(skillData);

            // When
            api.setSkillPoints(player, 15);

            // Then
            verify(skillData).setSkillPoints(15);
        }

        @Test
        @DisplayName("異常系: nullプレイヤーで0を返す")
        void testGetSkillPoints_NullPlayer_ReturnsZero() {
            // When
            int points = api.getSkillPoints(null);

            // Then
            assertThat(points).isZero();
        }

        @Test
        @DisplayName("異常系: nullプレイヤーで設定をスキップ")
        void testSetSkillPoints_NullPlayer_DoesNothing() {
            // When
            api.setSkillPoints(null, 15);

            // Then
            verify(skillManager, never()).getPlayerSkillData(any(Player.class));
        }

        @Test
        @DisplayName("境界値: 負数を設定すると0になる")
        void testSetSkillPoints_Negative_SetsToZero() {
            // Given
            var skillData = mock(SkillManager.PlayerSkillData.class);
            when(skillManager.getPlayerSkillData((org.bukkit.entity.Player)player)).thenReturn(skillData);

            // When
            api.setSkillPoints(player, -5);

            // Then
            verify(skillData).setSkillPoints(0);
        }
    }

    @Nested
    @DisplayName("getAttrPoints/setAttrPoints: 属性ポイント操作")
    class AttrPointsTests {

        @Test
        @DisplayName("正常系: 属性ポイントを取得")
        void testGetAttrPoints_ValidPlayer_ReturnsPoints() {
            // Given
            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);
            when(rpgPlayer.getAvailablePoints()).thenReturn(8);

            // When
            int points = api.getAttrPoints(player);

            // Then
            assertThat(points).isEqualTo(8);
        }

        @Test
        @DisplayName("正常系: 属性ポイントを設定")
        void testSetAttrPoints_ValidPlayer_SetsPoints() {
            // Given
            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);

            // When
            api.setAttrPoints(player, 12);

            // Then
            verify(rpgPlayer).setAvailablePoints(12);
        }

        @Test
        @DisplayName("異常系: nullプレイヤーで0を返す")
        void testGetAttrPoints_NullPlayer_ReturnsZero() {
            // When
            int points = api.getAttrPoints(null);

            // Then
            assertThat(points).isZero();
        }

        @Test
        @DisplayName("異常系: 未登録プレイヤーで0を返す")
        void testGetAttrPoints_UnregisteredPlayer_ReturnsZero() {
            // Given
            when(playerManager.getRPGPlayer(playerUuid)).thenReturn(null);

            // When
            int points = api.getAttrPoints(player);

            // Then
            assertThat(points).isZero();
        }

        @Test
        @DisplayName("異常系: nullプレイヤーで設定をスキップ")
        void testSetAttrPoints_NullPlayer_DoesNothing() {
            // When
            api.setAttrPoints(null, 12);

            // Then
            verify(rpgPlayer, never()).setAvailablePoints(anyInt());
        }

        @Test
        @DisplayName("境界値: 負数を設定すると0になる")
        void testSetAttrPoints_Negative_SetsToZero() {
            // Given
            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);

            // When
            api.setAttrPoints(player, -3);

            // Then
            verify(rpgPlayer).setAvailablePoints(0);
        }
    }

    // ==================== クラス操作 ====================

    @Nested
    @DisplayName("setClass: クラス設定")
    class SetClassTests {

        @Test
        @DisplayName("正常系: クラスを設定")
        void testSetClass_ValidPlayerAndClassId_ReturnsTrue() {
            // Given
            when(classManager.setPlayerClass(player, "warrior")).thenReturn(true);

            // When
            boolean result = api.setClass(player, "warrior");

            // Then
            assertThat(result).isTrue();
            verify(classManager).setPlayerClass(player, "warrior");
        }

        @Test
        @DisplayName("異常系: nullプレイヤーでfalseを返す")
        void testSetClass_NullPlayer_ReturnsFalse() {
            // When
            boolean result = api.setClass(null, "warrior");

            // Then
            assertThat(result).isFalse();
            verify(classManager, never()).setPlayerClass(any(), any());
        }

        @Test
        @DisplayName("異常系: nullクラスIDでfalseを返す")
        void testSetClass_NullClassId_ReturnsFalse() {
            // When
            boolean result = api.setClass(player, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: 空文字列でfalseを返す")
        void testSetClass_EmptyClassId_ReturnsFalse() {
            // When
            boolean result = api.setClass(player, "");

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("changeClass: クラス変更")
    class ChangeClassTests {

        @Test
        @DisplayName("正常系: デフォルトレベルでクラスを変更")
        void testChangeClass_ValidPlayerAndClassId_ReturnsTrue() {
            // Given
            when(classManager.changeClass(player, "mage", 0)).thenReturn(true);

            // When
            boolean result = api.changeClass(player, "mage");

            // Then
            assertThat(result).isTrue();
            verify(classManager).changeClass(player, "mage", 0);
        }

        @Test
        @DisplayName("正常系: レベル指定でクラスを変更")
        void testChangeClass_WithLevel_ReturnsTrue() {
            // Given
            when(classManager.changeClass(player, "mage", 10)).thenReturn(true);

            // When
            boolean result = api.changeClass(player, "mage", 10);

            // Then
            assertThat(result).isTrue();
            verify(classManager).changeClass(player, "mage", 10);
        }

        @Test
        @DisplayName("異常系: nullプレイヤーでfalseを返す")
        void testChangeClass_NullPlayer_ReturnsFalse() {
            // When
            boolean result = api.changeClass(null, "mage");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: nullクラスIDでfalseを返す")
        void testChangeClass_NullClassId_ReturnsFalse() {
            // When
            boolean result = api.changeClass(player, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: 空文字列でfalseを返す")
        void testChangeClass_EmptyClassId_ReturnsFalse() {
            // When
            boolean result = api.changeClass(player, "");

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("upgradeClassRank: クラスランクアップ")
    class UpgradeClassRankTests {

        @Test
        @DisplayName("正常系: クラスをランクアップ")
        void testUpgradeClassRank_ValidPlayer_ReturnsTrue() {
            // Given
            RPGClass currentClass = mock(RPGClass.class);
            when(currentClass.getNextRankClassId()).thenReturn(Optional.of("warrior_advanced"));

            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);
            when(rpgPlayer.getClassId()).thenReturn("warrior");
            when(classManager.getClass("warrior")).thenReturn(Optional.of(currentClass));
            when(classManager.canUpgradeClass(player, "warrior_advanced"))
                    .thenReturn(new ClassManager.ClassUpResult(true, "成功", Collections.emptyList()));
            when(classManager.setPlayerClass(player, "warrior_advanced")).thenReturn(true);

            // When
            boolean result = api.upgradeClassRank(player);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("異常系: nullプレイヤーでfalseを返す")
        void testUpgradeClassRank_NullPlayer_ReturnsFalse() {
            // When
            boolean result = api.upgradeClassRank(null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: 未登録プレイヤーでfalseを返す")
        void testUpgradeClassRank_UnregisteredPlayer_ReturnsFalse() {
            // Given
            when(playerManager.getRPGPlayer(playerUuid)).thenReturn(null);

            // When
            boolean result = api.upgradeClassRank(player);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: クラス未設定でfalseを返す")
        void testUpgradeClassRank_NoClass_ReturnsFalse() {
            // Given
            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);
            when(rpgPlayer.getClassId()).thenReturn(null);

            // When
            boolean result = api.upgradeClassRank(player);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: 次のランクがない場合falseを返す")
        void testUpgradeClassRank_NoNextRank_ReturnsFalse() {
            // Given
            RPGClass currentClass = mock(RPGClass.class);
            when(currentClass.getNextRankClassId()).thenReturn(Optional.empty());

            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);
            when(rpgPlayer.getClassId()).thenReturn("warrior");
            when(classManager.getClass("warrior")).thenReturn(Optional.of(currentClass));

            // When
            boolean result = api.upgradeClassRank(player);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("canUpgradeClass: ランクアップ可能チェック")
    class CanUpgradeClassTests {

        @Test
        @DisplayName("正常系: ランクアップ可能")
        void testCanUpgradeClass_ValidPlayer_ReturnsTrue() {
            // Given
            RPGClass currentClass = mock(RPGClass.class);
            when(currentClass.getNextRankClassId()).thenReturn(Optional.of("warrior_advanced"));

            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);
            when(rpgPlayer.getClassId()).thenReturn("warrior");
            when(classManager.getClass("warrior")).thenReturn(Optional.of(currentClass));
            when(classManager.canUpgradeClass(player, "warrior_advanced"))
                    .thenReturn(new ClassManager.ClassUpResult(true, "成功", Collections.emptyList()));

            // When
            boolean result = api.canUpgradeClass(player);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("異常系: nullプレイヤーでfalseを返す")
        void testCanUpgradeClass_NullPlayer_ReturnsFalse() {
            // When
            boolean result = api.canUpgradeClass(null);

            // Then
            assertThat(result).isFalse();
        }
    }

    // ==================== スキル操作 ====================

    @Nested
    @DisplayName("hasSkill: スキル習得チェック")
    class HasSkillTests {

        @Test
        @DisplayName("正常系: スキルを習得している")
        void testHasSkill_ValidPlayer_ReturnsTrue() {
            // Given
            when(skillManager.hasSkill(player, "fireball")).thenReturn(true);

            // When
            boolean result = api.hasSkill(player, "fireball");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("異常系: nullプレイヤーでfalseを返す")
        void testHasSkill_NullPlayer_ReturnsFalse() {
            // When
            boolean result = api.hasSkill(null, "fireball");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: nullスキルIDでfalseを返す")
        void testHasSkill_NullSkillId_ReturnsFalse() {
            // When
            boolean result = api.hasSkill(player, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: 空文字列でfalseを返す")
        void testHasSkill_EmptySkillId_ReturnsFalse() {
            // When
            boolean result = api.hasSkill(player, "");

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("unlockSkill: スキル習得")
    class UnlockSkillTests {

        @Test
        @DisplayName("正常系: スキルを習得")
        void testUnlockSkill_ValidPlayer_ReturnsTrue() {
            // Given
            when(skillManager.acquireSkill(player, "fireball", 1)).thenReturn(true);

            // When
            boolean result = api.unlockSkill(player, "fireball");

            // Then
            assertThat(result).isTrue();
            verify(skillManager).acquireSkill(player, "fireball", 1);
        }

        @Test
        @DisplayName("異常系: nullプレイヤーでfalseを返す")
        void testUnlockSkill_NullPlayer_ReturnsFalse() {
            // When
            boolean result = api.unlockSkill(null, "fireball");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: nullスキルIDでfalseを返す")
        void testUnlockSkill_NullSkillId_ReturnsFalse() {
            // When
            boolean result = api.unlockSkill(player, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: 空文字列でfalseを返す")
        void testUnlockSkill_EmptySkillId_ReturnsFalse() {
            // When
            boolean result = api.unlockSkill(player, "");

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("castSkill: スキル発動")
    class CastSkillTests {

        @Test
        @DisplayName("正常系: スキルを発動")
        void testCastSkill_ValidPlayerAndSkill_ReturnsTrue() {
            // Given
            Skill skill = mock(Skill.class);
            when(skillManager.hasSkill(player, "fireball")).thenReturn(true);
            when(skillManager.getSkill("fireball")).thenReturn(skill);
            when(skillManager.checkCooldown(player, "fireball")).thenReturn(true);
            when(skillManager.getSkillLevel(player, "fireball")).thenReturn(1);
            when(activeSkillExecutor.execute(player, skill, 1)).thenReturn(true);

            // When
            boolean result = api.castSkill(player, "fireball");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("異常系: nullプレイヤーでfalseを返す")
        void testCastSkill_NullPlayer_ReturnsFalse() {
            // When
            boolean result = api.castSkill(null, "fireball");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: nullスキルIDでfalseを返す")
        void testCastSkill_NullSkillId_ReturnsFalse() {
            // When
            boolean result = api.castSkill(player, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: 未習得スキルでfalseを返す")
        void testCastSkill_UnlearnedSkill_ReturnsFalse() {
            // Given
            when(skillManager.hasSkill(player, "fireball")).thenReturn(false);

            // When
            boolean result = api.castSkill(player, "fireball");

            // Then
            assertThat(result).isFalse();
            verify(player).sendMessage(contains("スキルを習得していません"));
        }

        @Test
        @DisplayName("異常系: スキルが見つからない場合falseを返す")
        void testCastSkill_SkillNotFound_ReturnsFalse() {
            // Given
            when(skillManager.hasSkill(player, "fireball")).thenReturn(true);
            when(skillManager.getSkill("fireball")).thenReturn(null);

            // When
            boolean result = api.castSkill(player, "fireball");

            // Then
            assertThat(result).isFalse();
            verify(player).sendMessage(contains("スキルが見つかりません"));
        }
    }

    @Nested
    @DisplayName("getSkillLevel: スキルレベル取得")
    class GetSkillLevelTests {

        @Test
        @DisplayName("正常系: スキルレベルを取得")
        void testGetSkillLevel_ValidPlayer_ReturnsLevel() {
            // Given
            when(skillManager.getSkillLevel(player, "fireball")).thenReturn(5);

            // When
            int level = api.getSkillLevel(player, "fireball");

            // Then
            assertThat(level).isEqualTo(5);
        }

        @Test
        @DisplayName("異常系: nullプレイヤーで0を返す")
        void testGetSkillLevel_NullPlayer_ReturnsZero() {
            // When
            int level = api.getSkillLevel(null, "fireball");

            // Then
            assertThat(level).isZero();
        }

        @Test
        @DisplayName("異常系: nullスキルIDで0を返す")
        void testGetSkillLevel_NullSkillId_ReturnsZero() {
            // When
            int level = api.getSkillLevel(player, null);

            // Then
            assertThat(level).isZero();
        }

        @Test
        @DisplayName("異常系: 空文字列で0を返す")
        void testGetSkillLevel_EmptySkillId_ReturnsZero() {
            // When
            int level = api.getSkillLevel(player, "");

            // Then
            assertThat(level).isZero();
        }
    }

    @Nested
    @DisplayName("getAcquiredSkills: 習得スキル一覧取得")
    class GetAcquiredSkillsTests {

        @Test
        @DisplayName("正常系: 習得スキル一覧を取得")
        void testGetAcquiredSkills_ValidPlayer_ReturnsSkills() {
            // Given
            var skillData = mock(SkillManager.PlayerSkillData.class);
            Map<String, Integer> skills = new HashMap<>();
            skills.put("fireball", 3);
            skills.put("icespike", 2);
            when(skillData.getAcquiredSkills()).thenReturn(skills);
            when(skillManager.getPlayerSkillData((org.bukkit.entity.Player)player)).thenReturn(skillData);

            // When
            Map<String, Integer> result = api.getAcquiredSkills(player);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsEntry("fireball", 3);
            assertThat(result).containsEntry("icespike", 2);
        }

        @Test
        @DisplayName("異常系: nullプレイヤーで空マップを返す")
        void testGetAcquiredSkills_NullPlayer_ReturnsEmptyMap() {
            // When
            Map<String, Integer> result = api.getAcquiredSkills(null);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSkillsForClass: クラススキル一覧取得")
    class GetSkillsForClassTests {

        @Test
        @DisplayName("正常系: クラススキル一覧を取得")
        void testGetSkillsForClass_ValidClassId_ReturnsSkills() {
            // Given
            List<Skill> skills = Arrays.asList(mock(Skill.class), mock(Skill.class));
            when(skillManager.getSkillsForClass("warrior")).thenReturn(skills);

            // When
            List<Skill> result = api.getSkillsForClass("warrior");

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("異常系: nullクラスIDで空リストを返す")
        void testGetSkillsForClass_NullClassId_ReturnsEmptyList() {
            // When
            List<Skill> result = api.getSkillsForClass(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("異常系: 空文字列で空リストを返す")
        void testGetSkillsForClass_EmptyClassId_ReturnsEmptyList() {
            // When
            List<Skill> result = api.getSkillsForClass("");

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ==================== ダメージ計算 ====================

    @Nested
    @DisplayName("calculateDamage: ダメージ計算")
    class CalculateDamageTests {

        @Test
        @DisplayName("正常系: 物理ダメージを計算")
        void testCalculateDamage_ValidPlayer_ReturnsPhysicalDamage() {
            // Given
            Map<Stat, Integer> stats = new EnumMap<>(Stat.class);
            stats.put(Stat.STRENGTH, 20);
            stats.put(Stat.INTELLIGENCE, 5);

            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);
            when(statManager.getAllFinalStats()).thenReturn(stats);

            // When
            double damage = api.calculateDamage(player, entity);

            // Then
            assertThat(damage).isGreaterThan(0);
        }

        @Test
        @DisplayName("正常系: 魔法ダメージを計算")
        void testCalculateDamage_ValidPlayer_ReturnsMagicDamage() {
            // Given
            Map<Stat, Integer> stats = new EnumMap<>(Stat.class);
            stats.put(Stat.STRENGTH, 5);
            stats.put(Stat.INTELLIGENCE, 25);

            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);
            when(statManager.getAllFinalStats()).thenReturn(stats);

            // When
            double damage = api.calculateDamage(player, entity);

            // Then
            assertThat(damage).isGreaterThan(0);
        }

        @Test
        @DisplayName("異常系: null攻撃者で0を返す")
        void testCalculateDamage_NullAttacker_ReturnsZero() {
            // When
            double damage = api.calculateDamage(null, entity);

            // Then
            assertThat(damage).isZero();
        }

        @Test
        @DisplayName("異常系: nullターゲットで0を返す")
        void testCalculateDamage_NullTarget_ReturnsZero() {
            // When
            double damage = api.calculateDamage(player, null);

            // Then
            assertThat(damage).isZero();
        }

        @Test
        @DisplayName("異常系: 未登録プレイヤーでデフォルトダメージを返す")
        void testCalculateDamage_UnregisteredPlayer_ReturnsDefaultDamage() {
            // Given
            when(playerManager.getRPGPlayer(playerUuid)).thenReturn(null);

            // When
            double damage = api.calculateDamage(player, entity);

            // Then
            assertThat(damage).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("applyStatModifiers: ステータス補正適用")
    class ApplyStatModifiersTests {

        @Test
        @DisplayName("正常系: 筋力で物理ダメージを補正")
        void testApplyStatModifiers_Strength_AppliesPhysicalDamage() {
            // Given
            Map<Stat, Integer> stats = new EnumMap<>(Stat.class);
            stats.put(Stat.STRENGTH, 20);

            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);
            when(statManager.getAllFinalStats()).thenReturn(stats);

            // When
            double result = api.applyStatModifiers(player, 10.0, Stat.STRENGTH);

            // Then
            assertThat(result).isGreaterThan(10.0);
        }

        @Test
        @DisplayName("正常系: 知力で魔法ダメージを補正")
        void testApplyStatModifiers_Intelligence_AppliesMagicDamage() {
            // Given
            Map<Stat, Integer> stats = new EnumMap<>(Stat.class);
            stats.put(Stat.INTELLIGENCE, 25);

            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);
            when(statManager.getAllFinalStats()).thenReturn(stats);

            // When
            double result = api.applyStatModifiers(player, 10.0, Stat.INTELLIGENCE);

            // Then
            assertThat(result).isGreaterThan(10.0);
        }

        @Test
        @DisplayName("異常系: nullプレイヤーで基本ダメージを返す")
        void testApplyStatModifiers_NullPlayer_ReturnsBaseDamage() {
            // When
            double result = api.applyStatModifiers(null, 10.0, Stat.STRENGTH);

            // Then
            assertThat(result).isEqualTo(10.0);
        }

        @Test
        @DisplayName("異常系: nullステータスで基本ダメージを返す")
        void testApplyStatModifiers_NullStat_ReturnsBaseDamage() {
            // When
            double result = api.applyStatModifiers(player, 10.0, null);

            // Then
            assertThat(result).isEqualTo(10.0);
        }

        @Test
        @DisplayName("異常系: 未登録プレイヤーで基本ダメージを返す")
        void testApplyStatModifiers_UnregisteredPlayer_ReturnsBaseDamage() {
            // Given
            when(playerManager.getRPGPlayer(playerUuid)).thenReturn(null);

            // When
            double result = api.applyStatModifiers(player, 10.0, Stat.STRENGTH);

            // Then
            assertThat(result).isEqualTo(10.0);
        }
    }

    // ==================== ターゲット管理 ====================

    @Nested
    @DisplayName("getLastTargetedEntity: 最後のターゲット取得")
    class GetLastTargetedEntityTests {

        @Test
        @DisplayName("正常系: ターゲットを取得")
        void testGetLastTargetedEntity_ValidPlayer_ReturnsTarget() {
            // Given
            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);
            when(rpgPlayer.getLastTargetedEntity()).thenReturn(Optional.of(entity));

            // When
            Optional<Entity> result = api.getLastTargetedEntity(player);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(entity);
        }

        @Test
        @DisplayName("異常系: nullプレイヤーで空を返す")
        void testGetLastTargetedEntity_NullPlayer_ReturnsEmpty() {
            // When
            Optional<Entity> result = api.getLastTargetedEntity(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("異常系: 未登録プレイヤーで空を返す")
        void testGetLastTargetedEntity_UnregisteredPlayer_ReturnsEmpty() {
            // Given
            when(playerManager.getRPGPlayer(playerUuid)).thenReturn(null);

            // When
            Optional<Entity> result = api.getLastTargetedEntity(player);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("setTargetedEntity: ターゲット設定")
    class SetTargetedEntityTests {

        @Test
        @DisplayName("正常系: ターゲットを設定")
        void testSetTargetedEntity_ValidPlayer_SetsTarget() {
            // Given
            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);

            // When
            api.setTargetedEntity(player, entity);

            // Then
            verify(rpgPlayer).setTargetedEntity(entity);
        }

        @Test
        @DisplayName("異常系: nullプレイヤーで設定をスキップ")
        void testSetTargetedEntity_NullPlayer_DoesNothing() {
            // When
            api.setTargetedEntity(null, entity);

            // Then
            verify(rpgPlayer, never()).setTargetedEntity(any());
        }

        @Test
        @DisplayName("異常系: 未登録プレイヤーで設定をスキップ")
        void testSetTargetedEntity_UnregisteredPlayer_DoesNothing() {
            // Given
            when(playerManager.getRPGPlayer(playerUuid)).thenReturn(null);

            // When
            api.setTargetedEntity(player, entity);

            // Then
            verify(rpgPlayer, never()).setTargetedEntity(any());
        }
    }

    // ==================== スキルトリガー ====================

    @Nested
    @DisplayName("castSkillAt: ターゲット指定スキル発動")
    class CastSkillAtTests {

        @Test
        @DisplayName("正常系: ターゲット指定でスキル発動")
        void testCastSkillAt_ValidPlayerAndTarget_ReturnsTrue() {
            // Given
            Skill skill = mock(Skill.class);
            when(skillManager.hasSkill(player, "fireball")).thenReturn(true);
            when(skillManager.getSkill("fireball")).thenReturn(skill);
            when(skillManager.checkCooldown(player, "fireball")).thenReturn(true);
            when(skillManager.getSkillLevel(player, "fireball")).thenReturn(1);
            when(playerManager.getRPGPlayer((java.util.UUID)playerUuid)).thenReturn(rpgPlayer);
            when(activeSkillExecutor.executeAt(player, skill, 1, entity)).thenReturn(true);

            // When
            boolean result = api.castSkillAt(player, "fireball", entity);

            // Then
            assertThat(result).isTrue();
            verify(rpgPlayer).setTargetedEntity(entity);
        }

        @Test
        @DisplayName("異常系: nullプレイヤーでfalseを返す")
        void testCastSkillAt_NullPlayer_ReturnsFalse() {
            // When
            boolean result = api.castSkillAt(null, "fireball", entity);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: nullスキルIDでfalseを返す")
        void testCastSkillAt_NullSkillId_ReturnsFalse() {
            // When
            boolean result = api.castSkillAt(player, null, entity);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: nullターゲットでfalseを返す")
        void testCastSkillAt_NullTarget_ReturnsFalse() {
            // When
            boolean result = api.castSkillAt(player, "fireball", null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: 未習得スキルでfalseを返す")
        void testCastSkillAt_UnlearnedSkill_ReturnsFalse() {
            // Given
            when(skillManager.hasSkill(player, "fireball")).thenReturn(false);

            // When
            boolean result = api.castSkillAt(player, "fireball", entity);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("castSkillWithCostType: コストタイプ指定スキル発動")
    class CastSkillWithCostTypeTests {

        @Test
        @DisplayName("正常系: コストタイプ指定でスキル発動")
        void testCastSkillWithCostType_ValidPlayerAndCostType_ReturnsTrue() {
            // Given
            Skill skill = mock(Skill.class);
            when(skillManager.hasSkill(player, "fireball")).thenReturn(true);
            when(skillManager.getSkill("fireball")).thenReturn(skill);
            when(skillManager.checkCooldown(player, "fireball")).thenReturn(true);
            when(skillManager.getSkillLevel(player, "fireball")).thenReturn(1);
            when(activeSkillExecutor.executeWithCostType(player, skill, 1, SkillCostType.MANA))
                    .thenReturn(true);

            // When
            boolean result = api.castSkillWithCostType(player, "fireball", SkillCostType.MANA);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("異常系: nullプレイヤーでfalseを返す")
        void testCastSkillWithCostType_NullPlayer_ReturnsFalse() {
            // When
            boolean result = api.castSkillWithCostType(null, "fireball", SkillCostType.MANA);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: nullスキルIDでfalseを返す")
        void testCastSkillWithCostType_NullSkillId_ReturnsFalse() {
            // When
            boolean result = api.castSkillWithCostType(player, null, SkillCostType.MANA);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: nullコストタイプでfalseを返す")
        void testCastSkillWithCostType_NullCostType_ReturnsFalse() {
            // When
            boolean result = api.castSkillWithCostType(player, "fireball", null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("異常系: 未習得スキルでfalseを返す")
        void testCastSkillWithCostType_UnlearnedSkill_ReturnsFalse() {
            // Given
            when(skillManager.hasSkill(player, "fireball")).thenReturn(false);

            // When
            boolean result = api.castSkillWithCostType(player, "fireball", SkillCostType.MANA);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getEntitiesInArea: 範囲内エンティティ取得")
    class GetEntitiesInAreaTests {

        @Test
        @DisplayName("正常系: 球形範囲でエンティティを取得")
        void testGetEntitiesInArea_SphereShape_ReturnsEntities() {
            // Given
            List<Entity> entities = Arrays.asList(entity);
            when(world.getNearbyEntities(any(), anyDouble(), anyDouble(), anyDouble()))
                    .thenReturn(entities);

            // When
            Collection<Entity> result = api.getEntitiesInArea(player, "sphere", 5.0);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result).contains(entity);
        }

        @Test
        @DisplayName("正常系: 立方体範囲でエンティティを取得")
        void testGetEntitiesInArea_CubeShape_ReturnsEntities() {
            // Given
            List<Entity> entities = Arrays.asList(entity);
            when(world.getNearbyEntities(any(), anyDouble(), anyDouble(), anyDouble()))
                    .thenReturn(entities);

            // When
            Collection<Entity> result = api.getEntitiesInArea(player, "cube", 5.0, 3.0, 4.0);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("正常系: 水平円形範囲でエンティティを取得")
        void testGetEntitiesInArea_HorizontalShape_ReturnsEntities() {
            // Given
            List<Entity> entities = Arrays.asList(entity);
            when(world.getNearbyEntities(any(), anyDouble(), anyDouble(), anyDouble()))
                    .thenReturn(entities);

            // When
            Collection<Entity> result = api.getEntitiesInArea(player, "horizontal", 5.0);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("異常系: nullプレイヤーで空リストを返す")
        void testGetEntitiesInArea_NullPlayer_ReturnsEmptyList() {
            // When
            Collection<Entity> result = api.getEntitiesInArea(null, "sphere", 5.0);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("異常系: nullシェイプで空リストを返す")
        void testGetEntitiesInArea_NullShape_ReturnsEmptyList() {
            // When
            Collection<Entity> result = api.getEntitiesInArea(player, null, 5.0);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("異常系: nullパラメータで空リストを返す")
        void testGetEntitiesInArea_NullParams_ReturnsEmptyList() {
            // When
            Collection<Entity> result = api.getEntitiesInArea(player, "sphere", (double[]) null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("異常系: 空パラメータで空リストを返す")
        void testGetEntitiesInArea_EmptyParams_ReturnsEmptyList() {
            // When
            Collection<Entity> result = api.getEntitiesInArea(player, "sphere");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("異常系: 不明なシェイプで空リストを返す")
        void testGetEntitiesInArea_UnknownShape_ReturnsEmptyList() {
            // When
            Collection<Entity> result = api.getEntitiesInArea(player, "unknown", 5.0);

            // Then
            assertThat(result).isEmpty();
        }
    }
}
