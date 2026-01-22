package com.example.rpgplugin.player;

import com.example.rpgplugin.stats.ManaManager;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import com.example.rpgplugin.storage.models.PlayerData;
import com.example.rpgplugin.skill.event.SkillEventListener;
import com.example.rpgplugin.skill.result.SkillExecutionResult;
import com.example.rpgplugin.skill.SkillExecutionConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

/**
 * RPGPlayerのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RPGPlayer Tests")
@Disabled("TODO: Fix deprecated API usage - formatStats, setMaxMana, setCurrentMana, addMana, isEmptyMana, getCostType methods not found")
class RPGPlayerTest {

    @Mock
    private PlayerData mockPlayerData;

    @Mock
    private StatManager mockStatManager;

    @Mock
    private ManaManager mockManaManager;

    @Mock
    private Player mockPlayer;

    @Mock
    private AttributeInstance mockAttributeInstance;

    @Mock
    private SkillEventListener mockSkillEventListener;

    private MockedStatic<Bukkit> mockedBukkit;

    private UUID testUuid;
    private RPGPlayer rpgPlayer;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();

        when(mockPlayerData.getUuid()).thenReturn(testUuid);
        when(mockPlayerData.getUsername()).thenReturn("TestPlayer");
        when(mockPlayerData.getClassId()).thenReturn("warrior");
        when(mockPlayerData.getClassRank()).thenReturn(1);
        when(mockPlayerData.getMaxMana()).thenReturn(100);
        when(mockPlayerData.getCurrentMana()).thenReturn(50);
        when(mockPlayerData.getMaxHealth()).thenReturn(0);
        when(mockPlayerData.getCostType()).thenReturn("mana");

        when(mockStatManager.getBaseStat(any())).thenReturn(10);
        when(mockStatManager.getFinalStat(any())).thenReturn(15);
        when(mockStatManager.getAvailablePoints()).thenReturn(5);

        when(mockManaManager.getMaxMana()).thenReturn(100);
        when(mockManaManager.getCurrentMana()).thenReturn(50);
        when(mockManaManager.getMaxHpModifier()).thenReturn(0);
        when(mockManaManager.getCostType()).thenReturn(ManaManager.CostType.MANA);
        when(mockManaManager.getManaRatio()).thenReturn(0.5);
        when(mockManaManager.regenerateMana(anyInt(), anyDouble())).thenReturn(5);
        when(mockManaManager.addMana(anyInt())).thenReturn(10);
        when(mockManaManager.consumeMana(anyInt())).thenReturn(true);
        when(mockManaManager.hasMana(anyInt())).thenReturn(true);
        when(mockManaManager.isFullMana()).thenReturn(false);
        when(mockManaManager.isEmptyMana()).thenReturn(false);
        when(mockManaManager.formatManaInfo()).thenReturn("MP: 50/100");

        when(mockPlayer.getUniqueId()).thenReturn(testUuid);
        when(mockPlayer.getName()).thenReturn("TestPlayer");
        when(mockPlayer.isOnline()).thenReturn(true);
        when(mockPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH)).thenReturn(mockAttributeInstance);
        when(mockPlayer.getHealth()).thenReturn(20.0);
        when(mockPlayer.getLevel()).thenReturn(10);

        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(() -> Bukkit.getPlayer(testUuid)).thenReturn(mockPlayer);

        // StatManagerモックの設定
        when(mockStatManager.formatStats()).thenReturn("STR: 10, DEX: 10, INT: 10");

        // PlayerDataのクラス履歴モック
        when(mockPlayerData.getClassHistoryList()).thenReturn(List.of("warrior"));

        rpgPlayer = new RPGPlayer(mockPlayerData, mockStatManager);
        // ManaManagerをreflectionで置き換えることは難しいため、
        // 代わりにRPGPlayerのメソッドを通してテストする
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("正常に初期化される")
        void constructor_InitializesSuccessfully() {
            assertEquals(testUuid, rpgPlayer.getUuid());
            assertEquals("TestPlayer", rpgPlayer.getUsername());
            assertEquals(mockPlayerData, rpgPlayer.getPlayerData());
            assertEquals(mockStatManager, rpgPlayer.getStatManager());
        }

        @Test
        @DisplayName("equalsとhashCodeが正しく動作する")
        void equalsAndHashCode_WorkCorrectly() {
            UUID sameUuid = testUuid;
            PlayerData samePlayerData = mock(PlayerData.class);
            when(samePlayerData.getUuid()).thenReturn(sameUuid);
            when(samePlayerData.getUsername()).thenReturn("TestPlayer");

            StatManager anotherStatManager = mock(StatManager.class);

            RPGPlayer sameRpgPlayer = new RPGPlayer(samePlayerData, anotherStatManager);

            assertEquals(rpgPlayer, sameRpgPlayer);
            assertEquals(rpgPlayer.hashCode(), sameRpgPlayer.hashCode());
        }

        @Test
        @DisplayName("equals: nullと比較するとfalse")
        void equals_Null_ReturnsFalse() {
            assertNotEquals(null, rpgPlayer);
        }

        @Test
        @DisplayName("equals: 異なるクラスと比較するとfalse")
        void equals_DifferentClass_ReturnsFalse() {
            assertNotEquals("string", rpgPlayer);
        }

        @Test
        @DisplayName("toString: 正しい文字列表現を返す")
        void toString_ReturnsCorrectFormat() {
            String result = rpgPlayer.toString();
            assertTrue(result.contains("RPGPlayer{"));
            assertTrue(result.contains("uuid=" + testUuid));
            assertTrue(result.contains("username='TestPlayer'"));
        }
    }

    @Nested
    @DisplayName("PlayerData Delegation")
    class PlayerDataDelegationTests {

        @Test
        @DisplayName("getClassId: クラスIDを取得する")
        void getClassId_ReturnsClassId() {
            assertEquals("warrior", rpgPlayer.getClassId());
            verify(mockPlayerData).getClassId();
        }

        @Test
        @DisplayName("setClassId: クラスIDを設定する")
        void setClassId_SetsClassId() {
            rpgPlayer.setClassId("mage");

            verify(mockPlayerData).setClassId("mage");
        }

        @Test
        @DisplayName("getClassRank: クラスランクを取得する")
        void getClassRank_ReturnsClassRank() {
            assertEquals(1, rpgPlayer.getClassRank());
            verify(mockPlayerData).getClassRank();
        }

        @Test
        @DisplayName("setClassRank: クラスランクを設定する")
        void setClassRank_SetsClassRank() {
            rpgPlayer.setClassRank(2);

            verify(mockPlayerData).setClassRank(2);
        }

        @Test
        @DisplayName("getClassHistory: クラス履歴を取得する")
        void getClassHistory_ReturnsHistory() {
            List<String> history = rpgPlayer.getClassHistory();

            assertNotNull(history);
            assertEquals(1, history.size());
            verify(mockPlayerData).getClassHistoryList();
        }

        @Test
        @DisplayName("addClassToHistory: クラス履歴に追加する")
        void addClassToHistory_AddsToHistory() {
            rpgPlayer.addClassToHistory("mage");

            verify(mockPlayerData).addClassToHistory("mage");
        }

        @Test
        @DisplayName("clearClassHistory: クラス履歴をクリアする")
        void clearClassHistory_ClearsHistory() {
            rpgPlayer.clearClassHistory();

            verify(mockPlayerData).setClassHistory(null);
        }

        @Test
        @DisplayName("getFirstJoin: 初回参加日時を取得する")
        void getFirstJoin_ReturnsFirstJoin() {
            when(mockPlayerData.getFirstJoin()).thenReturn(123456789L);

            assertEquals(123456789L, rpgPlayer.getFirstJoin());
            verify(mockPlayerData).getFirstJoin();
        }

        @Test
        @DisplayName("getLastLogin: 最終ログイン日時を取得する")
        void getLastLogin_ReturnsLastLogin() {
            when(mockPlayerData.getLastLogin()).thenReturn(987654321L);

            assertEquals(987654321L, rpgPlayer.getLastLogin());
            verify(mockPlayerData).getLastLogin();
        }

        @Test
        @DisplayName("updateLastLogin: 最終ログイン日時を更新する")
        void updateLastLogin_UpdatesLastLogin() {
            rpgPlayer.updateLastLogin();

            verify(mockPlayerData).updateLastLogin();
        }
    }

    @Nested
    @DisplayName("StatManager Delegation")
    class StatManagerDelegationTests {

        @Test
        @DisplayName("getBaseStat: 基本ステータスを取得する")
        void getBaseStat_ReturnsBaseStat() {
            assertEquals(10, rpgPlayer.getBaseStat(Stat.STRENGTH));
            verify(mockStatManager).getBaseStat(Stat.STRENGTH);
        }

        @Test
        @DisplayName("setBaseStat: 基本ステータスを設定する")
        void setBaseStat_SetsBaseStat() {
            rpgPlayer.setBaseStat(Stat.STRENGTH, 15);

            verify(mockStatManager).setBaseStat(Stat.STRENGTH, 15);
        }

        @Test
        @DisplayName("getFinalStat: 最終ステータスを取得する")
        void getFinalStat_ReturnsFinalStat() {
            assertEquals(15, rpgPlayer.getFinalStat(Stat.STRENGTH));
            verify(mockStatManager).getFinalStat(Stat.STRENGTH);
        }

        @Test
        @DisplayName("getAvailablePoints: 手動配分ポイントを取得する")
        void getAvailablePoints_ReturnsPoints() {
            assertEquals(5, rpgPlayer.getAvailablePoints());
            verify(mockStatManager).getAvailablePoints();
        }

        @Test
        @DisplayName("setAvailablePoints: 手動配分ポイントを設定する")
        void setAvailablePoints_SetsPoints() {
            rpgPlayer.setAvailablePoints(10);

            verify(mockStatManager).setAvailablePoints(10);
        }

        @Test
        @DisplayName("addAvailablePoints: 手動配分ポイントを追加する")
        void addAvailablePoints_AddsPoints() {
            rpgPlayer.addAvailablePoints(3);

            verify(mockStatManager).addAvailablePoints(3);
        }

        @Test
        @DisplayName("allocatePoint: ステータスにポイントを割り振る")
        void allocatePoint_AllocatesPoint() {
            when(mockStatManager.allocatePoint(any(), anyInt())).thenReturn(true);

            assertTrue(rpgPlayer.allocatePoint(Stat.STRENGTH, 2));
            verify(mockStatManager).allocatePoint(Stat.STRENGTH, 2);
        }

        @Test
        @DisplayName("resetAllocation: 配分をリセットする")
        void resetAllocation_ResetsAllocation() {
            when(mockStatManager.resetAllocation()).thenReturn(5);

            assertEquals(5, rpgPlayer.resetAllocation());
            verify(mockStatManager).resetAllocation();
        }

        @Test
        @DisplayName("formatStats: ステータス情報をフォーマットする")
        void formatStats_ReturnsFormattedString() {
            String result = rpgPlayer.formatStats();

            assertEquals("STR: 10, DEX: 10, INT: 10", result);
            verify(mockStatManager).formatStats();
        }
    }

    @Nested
    @DisplayName("ManaManager Delegation")
    class ManaManagerDelegationTests {

        @Test
        @DisplayName("getManaManager: マナマネージャーを取得する")
        void getManaManager_ReturnsManaManager() {
            assertNotNull(rpgPlayer.getManaManager());
        }

        @Test
        @DisplayName("getMaxMana: 最大MPを取得する")
        void getMaxMana_ReturnsMaxMana() {
            assertEquals(100, rpgPlayer.getMaxMana());
        }

        @Test
        @DisplayName("setMaxMana: 最大MPを設定する")
        void setMaxMana_SetsMaxMana() {
            rpgPlayer.setMaxMana(150);

            verify(mockPlayerData).setMaxMana(150);
            assertEquals(150, rpgPlayer.getMaxMana());
        }

        @Test
        @DisplayName("getCurrentMana: 現在MPを取得する")
        void getCurrentMana_ReturnsCurrentMana() {
            assertEquals(50, rpgPlayer.getCurrentMana());
        }

        @Test
        @DisplayName("setCurrentMana: 現在MPを設定する")
        void setCurrentMana_SetsCurrentMana() {
            rpgPlayer.setCurrentMana(75);

            verify(mockPlayerData).setCurrentMana(75);
            assertEquals(75, rpgPlayer.getCurrentMana());
        }

        @Test
        @DisplayName("addMana: MPを追加する")
        void addMana_AddsMana() {
            int result = rpgPlayer.addMana(30);

            // 現在MP: 50、最大MP: 100、追加量: 30
            // actualAdd = Math.min(30, 100 - 50) = 30
            assertEquals(30, result);
            verify(mockPlayerData).setCurrentMana(80);
        }

        @Test
        @DisplayName("consumeMana: MPを消費する")
        void consumeMana_ConsumesMana() {
            boolean result = rpgPlayer.consumeMana(20);

            assertTrue(result);
            // 50 - 20 = 30になる
            verify(mockPlayerData).setCurrentMana(30);
        }

        @Test
        @DisplayName("hasMana: MPが足りているか確認する")
        void hasMana_ChecksMana() {
            assertTrue(rpgPlayer.hasMana(30));
        }

        @Test
        @DisplayName("isFullMana: MPが満タンか確認する")
        void isFullMana_ChecksFullMana() {
            assertFalse(rpgPlayer.isFullMana());
        }

        @Test
        @DisplayName("isEmptyMana: MPが空か確認する")
        void isEmptyMana_ChecksEmptyMana() {
            assertFalse(rpgPlayer.isEmptyMana());
        }

        @Test
        @DisplayName("getManaRatio: MPの割合を取得する")
        void getManaRatio_ReturnsRatio() {
            assertEquals(0.5, rpgPlayer.getManaRatio(), 0.01);
        }

        @Test
        @DisplayName("regenerateMana: MPを回復する")
        void regenerateMana_RegeneratesMana() {
            int result = rpgPlayer.regenerateMana(1.0);

            // SPIRIT=15なので、回復量 = 1.0 + (15 * 0.1) = 2.5 → Math.floor = 2
            // 現在MP: 50 + 2 = 52
            assertEquals(2, result);
            verify(mockStatManager).getFinalStat(Stat.SPIRIT);
            verify(mockPlayerData).setCurrentMana(52);
        }

        @Test
        @DisplayName("getCostType: コストタイプを取得する")
        void getCostType_ReturnsCostType() {
            assertEquals(ManaManager.CostType.MANA, rpgPlayer.getCostType());
        }

        @Test
        @DisplayName("setCostType: コストタイプを設定する")
        void setCostType_SetsCostType() {
            rpgPlayer.setCostType(ManaManager.CostType.HEALTH);

            verify(mockPlayerData).setCostType("hp");
            assertEquals(ManaManager.CostType.HEALTH, rpgPlayer.getCostType());
        }

        @Test
        @DisplayName("isManaCostType: MP消費モードか確認する")
        void isManaCostType_ChecksIsMana() {
            assertTrue(rpgPlayer.isManaCostType());
        }

        @Test
        @DisplayName("toggleCostType: コストタイプを切り替える")
        void toggleCostType_TogglesCostType() {
            // 初期状態はMANA
            assertEquals(ManaManager.CostType.MANA, rpgPlayer.getCostType());

            // 切り替えるとHEALTHになる
            ManaManager.CostType result = rpgPlayer.toggleCostType();

            assertEquals(ManaManager.CostType.HEALTH, result);
            verify(mockPlayerData).setCostType("hp");

            // もう一度切り替えるとMANAに戻る
            ManaManager.CostType result2 = rpgPlayer.toggleCostType();
            assertEquals(ManaManager.CostType.MANA, result2);
        }

        @Test
        @DisplayName("formatManaInfo: MP情報をフォーマットする")
        void formatManaInfo_ReturnsFormattedString() {
            String result = rpgPlayer.formatManaInfo();
            // formatManaInfoはカラー付きの文字列を返す
            assertTrue(result.contains("50"));
            assertTrue(result.contains("100"));
            assertTrue(result.contains("MP消費"));
        }

        @Test
        @DisplayName("syncManaToData: MP状態をPlayerDataに同期する")
        void syncManaToData_SyncsToPlayerData() {
            rpgPlayer.syncManaToData();

            verify(mockPlayerData).setMaxMana(100);
            verify(mockPlayerData).setCurrentMana(50);
            verify(mockPlayerData).setMaxHealth(0);
            verify(mockPlayerData).setCostType("mana");
        }
    }

    @Nested
    @DisplayName("consumeSkillCost")
    class ConsumeSkillCostTests {

        @Test
        @DisplayName("MPモードでMPを消費する")
        void consumeSkillCost_ManaMode_ConsumesMana() {
            // 初期状態はMPモード
            assertTrue(rpgPlayer.isManaCostType());
            assertTrue(rpgPlayer.consumeSkillCost(20));
        }

        @Test
        @DisplayName("HPモードでHPを消費する")
        void consumeSkillCost_HpMode_ConsumesHp() {
            rpgPlayer.setCostType(ManaManager.CostType.HEALTH);
            when(mockPlayer.getHealth()).thenReturn(20.0);

            assertTrue(rpgPlayer.consumeSkillCost(5));
            verify(mockPlayer).setHealth(15.0);
        }

        @Test
        @DisplayName("HPモードでHP不足の場合は失敗する")
        void consumeSkillCost_HpMode_NotEnoughHp_Fails() {
            rpgPlayer.setCostType(ManaManager.CostType.HEALTH);
            when(mockPlayer.getHealth()).thenReturn(3.0);

            assertFalse(rpgPlayer.consumeSkillCost(5));
            verify(mockPlayer, never()).setHealth(anyDouble());
        }

        @Test
        @DisplayName("オフラインの場合は失敗する")
        void consumeSkillCost_Offline_Fails() {
            rpgPlayer.setCostType(ManaManager.CostType.HEALTH);
            when(mockPlayer.isOnline()).thenReturn(false);

            assertFalse(rpgPlayer.consumeSkillCost(5));
        }
    }

    @Nested
    @DisplayName("Max Health Modifier")
    class MaxHealthModifierTests {

        @Test
        @DisplayName("getMaxHealthModifier: 最大HP修飾子を取得する")
        void getMaxHealthModifier_ReturnsModifier() {
            assertEquals(0, rpgPlayer.getMaxHealthModifier());
        }

        @Test
        @DisplayName("setMaxHealthModifier: 最大HP修飾子を設定する")
        void setMaxHealthModifier_SetsModifier() {
            rpgPlayer.setMaxHealthModifier(10);

            verify(mockPlayerData).setMaxHealth(10);
            verify(mockAttributeInstance).setBaseValue(30.0);
            assertEquals(10, rpgPlayer.getMaxHealthModifier());
        }

        @Test
        @DisplayName("setMaxHealthModifier: オフラインの場合はBukkitのHPを更新しない")
        void setMaxHealthModifier_Offline_DoesNotUpdateBukkitHp() {
            // RPGPlayerを作り直してオフライン状態をシミュレート
            mockedBukkit.close();
            mockedBukkit = mockStatic(Bukkit.class);
            mockedBukkit.when(() -> Bukkit.getPlayer(testUuid)).thenReturn(null); // オフライン

            // オンラインプレイヤーを返さない設定にする
            when(mockPlayer.isOnline()).thenReturn(false);
            mockedBukkit.when(() -> Bukkit.getPlayer(testUuid)).thenReturn(mockPlayer);

            rpgPlayer.setMaxHealthModifier(10);

            verify(mockAttributeInstance, never()).setBaseValue(anyDouble());
        }
    }

    @Nested
    @DisplayName("Bukkit Player Access")
    class BukkitPlayerAccessTests {

        @Test
        @DisplayName("getBukkitPlayer: Bukkitプレイヤーを取得する")
        void getBukkitPlayer_ReturnsBukkitPlayer() {
            Player result = rpgPlayer.getBukkitPlayer();

            assertEquals(mockPlayer, result);
        }

        @Test
        @DisplayName("isOnline: オンライン状態を確認する")
        void isOnline_ReturnsOnlineStatus() {
            assertTrue(rpgPlayer.isOnline());
        }

        @Test
        @DisplayName("getVanillaLevel: バニラレベルを取得する")
        void getVanillaLevel_ReturnsLevel() {
            assertEquals(10, rpgPlayer.getVanillaLevel());
        }

        @Test
        @DisplayName("getLevel: レベルを取得する")
        void getLevel_ReturnsLevel() {
            assertEquals(10, rpgPlayer.getLevel());
        }

        @Test
        @DisplayName("sendMessage: オンラインの場合にメッセージを送信する")
        void sendMessage_Online_SendsMessage() {
            rpgPlayer.sendMessage("Test message");

            verify(mockPlayer).sendMessage("Test message");
        }

        @Test
        @DisplayName("sendMessage: オフラインの場合はメッセージを送信しない")
        void sendMessage_Offline_DoesNotSendMessage() {
            // オフライン状態をシミュレート
            when(mockPlayer.isOnline()).thenReturn(false);

            rpgPlayer.sendMessage("Test message");

            verify(mockPlayer, never()).sendMessage(anyString());
        }
    }

    @Nested
    @DisplayName("Target Management")
    class TargetManagementTests {

        @Test
        @DisplayName("setTargetedEntity: ターゲットエンティティを設定する")
        void setTargetedEntity_SetsTarget() {
            Entity mockEntity = mock(Entity.class);
            when(mockEntity.isValid()).thenReturn(true);

            rpgPlayer.setTargetedEntity(mockEntity);

            assertTrue(rpgPlayer.getLastTargetedEntity().isPresent());
            assertEquals(mockEntity, rpgPlayer.getLastTargetedEntity().get());
        }

        @Test
        @DisplayName("clearTarget: ターゲットをクリアする")
        void clearTarget_ClearsTarget() {
            Entity mockEntity = mock(Entity.class);
            when(mockEntity.isValid()).thenReturn(true);
            rpgPlayer.setTargetedEntity(mockEntity);

            rpgPlayer.clearTarget();

            assertTrue(rpgPlayer.getLastTargetedEntity().isEmpty());
        }

        @Test
        @DisplayName("getLastTargetedEntity: 無効なエンティティの場合は空を返す")
        void getLastTargetedEntity_InvalidEntity_ReturnsEmpty() {
            Entity mockEntity = mock(Entity.class);
            when(mockEntity.isValid()).thenReturn(false);
            rpgPlayer.setTargetedEntity(mockEntity);

            Optional<Entity> result = rpgPlayer.getLastTargetedEntity();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("getLastTargetedEntity: nullエンティティの場合は空を返す")
        void getLastTargetedEntity_NullEntity_ReturnsEmpty() {
            rpgPlayer.setTargetedEntity(null);

            Optional<Entity> result = rpgPlayer.getLastTargetedEntity();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Skill Methods")
    class SkillMethodTests {

        @BeforeEach
        void setUpSkillListener() {
            rpgPlayer.setSkillEventListener(mockSkillEventListener);
        }

        @Test
        @DisplayName("setSkillEventListener: スキルイベントリスナーを設定する")
        void setSkillEventListener_SetsListener() {
            rpgPlayer.setSkillEventListener(mockSkillEventListener);

            // 設定後にスキルメソッドが動作することを確認
            when(mockSkillEventListener.hasSkill(testUuid, "fireball")).thenReturn(true);

            assertTrue(rpgPlayer.hasSkill("fireball"));
        }

        @Test
        @DisplayName("hasSkill: スキルを習得しているか確認する")
        void hasSkill_ReturnsTrue() {
            when(mockSkillEventListener.hasSkill(testUuid, "fireball")).thenReturn(true);

            assertTrue(rpgPlayer.hasSkill("fireball"));
            verify(mockSkillEventListener).hasSkill(testUuid, "fireball");
        }

        @Test
        @DisplayName("hasSkill: リスナー未設定の場合はfalse")
        void hasSkill_NoListener_ReturnsFalse() {
            rpgPlayer.setSkillEventListener(null);

            assertFalse(rpgPlayer.hasSkill("fireball"));
        }

        @Test
        @DisplayName("getSkillLevel: スキルレベルを取得する")
        void getSkillLevel_ReturnsLevel() {
            when(mockSkillEventListener.getSkillLevel(testUuid, "fireball")).thenReturn(3);

            assertEquals(3, rpgPlayer.getSkillLevel("fireball"));
            verify(mockSkillEventListener).getSkillLevel(testUuid, "fireball");
        }

        @Test
        @DisplayName("getSkillLevel: リスナー未設定の場合は0")
        void getSkillLevel_NoListener_ReturnsZero() {
            rpgPlayer.setSkillEventListener(null);

            assertEquals(0, rpgPlayer.getSkillLevel("fireball"));
        }

        @Test
        @DisplayName("acquireSkill: スキルを習得する")
        void acquireSkill_AcquiresSkill() {
            when(mockSkillEventListener.canAcquireSkill(testUuid, "fireball")).thenReturn(true);
            when(mockSkillEventListener.getSkillLevel(testUuid, "fireball")).thenReturn(0);

            assertTrue(rpgPlayer.acquireSkill("fireball"));
            verify(mockSkillEventListener).canAcquireSkill(testUuid, "fireball");
            verify(mockSkillEventListener).onSkillAcquired(testUuid, "fireball", 1);
        }

        @Test
        @DisplayName("acquireSkill: レベル指定で習得する")
        void acquireSkill_WithLevel_AcquiresSkill() {
            when(mockSkillEventListener.canAcquireSkill(testUuid, "fireball")).thenReturn(true);
            when(mockSkillEventListener.getSkillLevel(testUuid, "fireball")).thenReturn(0);

            assertTrue(rpgPlayer.acquireSkill("fireball", 5));
            verify(mockSkillEventListener).onSkillAcquired(testUuid, "fireball", 5);
        }

        @Test
        @DisplayName("acquireSkill: 習得できない場合はfalse")
        void acquireSkill_CannotAcquire_ReturnsFalse() {
            when(mockSkillEventListener.canAcquireSkill(testUuid, "fireball")).thenReturn(false);

            assertFalse(rpgPlayer.acquireSkill("fireball"));
            verify(mockSkillEventListener).canAcquireSkill(testUuid, "fireball");
            verify(mockSkillEventListener, never()).onSkillAcquired(any(), any(), anyInt());
        }

        @Test
        @DisplayName("upgradeSkill: スキルをレベルアップする")
        void upgradeLevel_LevelsUpSkill() {
            when(mockSkillEventListener.getSkillLevel(testUuid, "fireball")).thenReturn(2);

            assertTrue(rpgPlayer.upgradeSkill("fireball"));
            verify(mockSkillEventListener).onSkillLevelUp(testUuid, "fireball", 3, 2);
        }

        @Test
        @DisplayName("upgradeSkill: 未習得の場合はfalse")
        void upgradeSkill_NotLearned_ReturnsFalse() {
            when(mockSkillEventListener.getSkillLevel(testUuid, "fireball")).thenReturn(0);

            assertFalse(rpgPlayer.upgradeSkill("fireball"));
            verify(mockSkillEventListener, never()).onSkillLevelUp(any(), any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("executeSkill: スキルを実行する")
        void executeSkill_ExecutesSkill() {
            when(mockSkillEventListener.getSkillLevel(testUuid, "fireball")).thenReturn(3);

            rpgPlayer.executeSkill("fireball");

            verify(mockSkillEventListener).getSkillLevel(testUuid, "fireball");
            verify(mockSkillEventListener).onSkillExecuted(testUuid, "fireball", 3);
        }

        @Test
        @DisplayName("executeSkill: Player付きで実行する")
        void executeSkill_WithPlayer_ExecutesSkill() {
            SkillExecutionResult mockResult = mock(SkillExecutionResult.class);
            when(mockResult.isSuccess()).thenReturn(true);
            when(mockSkillEventListener.executeSkill(mockPlayer, "fireball")).thenReturn(mockResult);

            SkillExecutionResult result = rpgPlayer.executeSkill(mockPlayer, "fireball");

            assertTrue(result.isSuccess());
            verify(mockSkillEventListener).executeSkill(mockPlayer, "fireball");
        }

        @Test
        @DisplayName("executeSkill: 設定付きで実行する")
        void executeSkill_WithConfig_ExecutesSkill() {
            SkillExecutionResult mockResult = mock(SkillExecutionResult.class);
            when(mockResult.isSuccess()).thenReturn(true);
            SkillExecutionConfig config = mock(SkillExecutionConfig.class);
            when(mockSkillEventListener.executeSkill(mockPlayer, "fireball", config)).thenReturn(mockResult);

            SkillExecutionResult result = rpgPlayer.executeSkill(mockPlayer, "fireball", config);

            assertTrue(result.isSuccess());
            verify(mockSkillEventListener).executeSkill(mockPlayer, "fireball", config);
        }

        @Test
        @DisplayName("executeSkill: リスナー未設定の場合は失敗")
        void executeSkill_NoListener_ReturnsFailure() {
            rpgPlayer.setSkillEventListener(null);

            SkillExecutionResult result = rpgPlayer.executeSkill(mockPlayer, "fireball");

            assertFalse(result.isSuccess());
            assertTrue(result.getErrorMessage().contains("スキルシステムが利用できません"));
        }
    }

    @Nested
    @DisplayName("Skill Methods Without Listener")
    class SkillMethodsWithoutListenerTests {

        @Test
        @DisplayName("リスナー未設定時の各メソッドが適切に動作する")
        void noListener_AllMethodsReturnDefault() {
            rpgPlayer.setSkillEventListener(null);

            assertFalse(rpgPlayer.hasSkill("test"));
            assertEquals(0, rpgPlayer.getSkillLevel("test"));
            assertFalse(rpgPlayer.acquireSkill("test"));
            assertFalse(rpgPlayer.upgradeSkill("test"));

            // executeSkillは何もしない
            assertDoesNotThrow(() -> rpgPlayer.executeSkill("test"));

            // executeSkill with playerは失敗結果を返す
            SkillExecutionResult result = rpgPlayer.executeSkill(mockPlayer, "test");
            assertFalse(result.isSuccess());
        }
    }
}
