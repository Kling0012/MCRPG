package com.example.rpgplugin.integration;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.RPGPluginAPI;
import com.example.rpgplugin.api.bridge.SKriptBridge;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillType;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 外部API連携の結合テスト
 *
 * <p>テスト対象:</p>
 * <ul>
 *   <li>SKript → API → スキル発動</li>
 *   <li>コマンド経由のAPI呼び出し</li>
 *   <li>ターゲット指定スキル発動</li>
 *   <li>コストタイプ指定スキル発動</li>
 *   <li>範囲内エンティティ取得</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: 外部API連携のテストに専念</li>
 *   <li>現実的: SKriptからの呼び出しを模倣</li>
 *   <li>独立性: 各テストは独立して実行可能</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("外部API連携結合テスト")
class APIBridgeIntegrationTest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private Logger mockLogger;

    @Mock
    private CommandSender mockSender;

    @Mock
    private Player mockPlayer;

    @Mock
    private Player mockTargetPlayer;

    @Mock
    private RPGPlayer mockRpgPlayer;

    @Mock
    private RPGPluginAPI mockAPI;

    private MockedStatic<Bukkit> mockedBukkit;

    private SKriptBridge skriptBridge;
    private SkillManager skillManager;

    /**
     * 各テストの前に実行されるセットアップ処理
     */
    @BeforeEach
    void setUp() {
        // Bukkit静的メソッドのモック化
        mockedBukkit = mockStatic(Bukkit.class);

        // Pluginの基本設定
        when(mockPlugin.getLogger()).thenReturn(mockLogger);
        when(mockPlugin.getAPI()).thenReturn(mockAPI);

        // CommandSenderのモック設定
        when(mockSender.sendMessage(anyString())).thenAnswer(invocation -> {
            System.out.println("[Sender] " + invocation.getArgument(0));
            return null;
        });

        // プレイヤーのモック設定
        UUID playerUuid = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockPlayer.getName()).thenReturn("TestPlayer");

        UUID targetUuid = UUID.randomUUID();
        when(mockTargetPlayer.getUniqueId()).thenReturn(targetUuid);
        when(mockTargetPlayer.getName()).thenReturn("TargetPlayer");

        // RPGPlayerのモック設定
        when(mockRpgPlayer.getBukkitPlayer()).thenReturn(mockPlayer);
        when(mockRpgPlayer.getLevel()).thenReturn(10);

        // Bukkit.getPlayerのモック設定
        mockedBukkit.when(() -> Bukkit.getPlayer("TestPlayer")).thenReturn(mockPlayer);
        mockedBukkit.when(() -> Bukkit.getPlayer("TargetPlayer")).thenReturn(mockTargetPlayer);

        // SKriptBridgeとSkillManagerの初期化
        skriptBridge = new SKriptBridge(mockPlugin);
        skillManager = new SkillManager(mockPlugin);

        // APIのデフォルト振る舞い設定
        setupDefaultAPIBehavior();
    }

    /**
     * 各テストの後に実行されるクリーンアップ処理
     */
    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
        skillManager.clearAllSkills();
        skillManager.clearAllPlayerData();
    }

    /**
     * APIのデフォルト振る舞いを設定
     */
    private void setupDefaultAPIBehavior() {
        // レベル関連
        when(mockAPI.getLevel(mockPlayer)).thenReturn(10);
        doNothing().when(mockAPI).setLevel(mockPlayer, anyInt());

        // ステータス関連
        when(mockAPI.getStat(mockPlayer, any())).thenReturn(50);
        doNothing().when(mockAPI).setStat(mockPlayer, any(Stat.class), anyInt());

        // クラス関連
        when(mockAPI.getClassId(mockPlayer)).thenReturn("Warrior");
        when(mockAPI.setClass(mockPlayer, anyString())).thenReturn(true);
        when(mockAPI.upgradeClassRank(mockPlayer)).thenReturn(true);
        when(mockAPI.canUpgradeClass(mockPlayer)).thenReturn(true);

        // スキル関連
        when(mockAPI.hasSkill(mockPlayer, anyString())).thenReturn(true);
        when(mockAPI.unlockSkill(mockPlayer, anyString())).thenReturn(true);
        when(mockAPI.castSkill(mockPlayer, anyString())).thenReturn(true);
        when(mockAPI.getSkillLevel(mockPlayer, anyString())).thenReturn(1);

        // ゴールド関連
        when(mockAPI.getGoldBalance(mockPlayer)).thenReturn(1000.0);
        when(mockAPI.depositGold(mockPlayer, anyDouble())).thenReturn(true);
        when(mockAPI.withdrawGold(mockPlayer, anyDouble())).thenReturn(true);
        when(mockAPI.hasEnoughGold(mockPlayer, anyDouble())).thenReturn(true);

        // ダメージ計算
        when(mockAPI.calculateDamage(any(), any())).thenReturn(100.0);

        // ターゲット関連
        when(mockAPI.getLastTargetedEntity(mockPlayer)).thenReturn(java.util.Optional.empty());
        doNothing().when(mockAPI).setTargetedEntity(mockPlayer, any());

        // スキルトリガー
        when(mockAPI.castSkillAt(mockPlayer, anyString(), any())).thenReturn(true);
        when(mockAPI.castSkillWithCostType(mockPlayer, anyString(), any())).thenReturn(true);

        // 範囲エンティティ
        when(mockAPI.getEntitiesInArea(mockPlayer, anyString(), any(double[].class)))
                .thenReturn(java.util.List.of());
    }

    // ==================== シナリオ1: 基本API呼び出し ====================

    @Nested
    @DisplayName("シナリオ1: 基本API呼び出し")
    class Scenario1BasicAPICalls {

        @Test
        @DisplayName("シナリオ1-1: get_levelコマンドが正常に動作する")
        void test1_1_GetLevelCommand() {
            // Given: プレイヤーレベルが10
            when(mockAPI.getLevel(mockPlayer)).thenReturn(10);

            // When: get_levelコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"get_level", "TestPlayer"});

            // Then: 成功し、レベルが表示される
            assertThat(result).isTrue();
            verify(mockSender).sendMessage(contains("LV: 10"));
        }

        @Test
        @DisplayName("シナリオ1-2: set_levelコマンドが正常に動作する")
        void test1_2_SetLevelCommand() {
            // When: set_levelコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"set_level", "TestPlayer", "15"});

            // Then: 成功し、レベルが設定される
            assertThat(result).isTrue();
            verify(mockAPI).setLevel(mockPlayer, 15);
            verify(mockSender).sendMessage(contains("LVを 15 に設定"));
        }

        @Test
        @DisplayName("シナリオ1-3: get_statコマンドが正常に動作する")
        void test1_3_GetStatCommand() {
            // Given: STRが50
            when(mockAPI.getStat(mockPlayer, Stat.STRENGTH)).thenReturn(50);

            // When: get_statコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"get_stat", "TestPlayer", "STR"});

            // Then: 成功し、ステータスが表示される
            assertThat(result).isTrue();
            verify(mockAPI).getStat(mockPlayer, Stat.STRENGTH);
        }

        @Test
        @DisplayName("シナリオ1-4: set_statコマンドが正常に動作する")
        void test1_4_SetStatCommand() {
            // When: set_statコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"set_stat", "TestPlayer", "INT", "60"});

            // Then: 成功し、ステータスが設定される
            assertThat(result).isTrue();
            verify(mockAPI).setStat(mockPlayer, Stat.INTELLIGENCE, 60);
        }
    }

    // ==================== シナリオ2: スキル操作API ====================

    @Nested
    @DisplayName("シナリオ2: スキル操作API")
    class Scenario2SkillAPI {

        @Test
        @DisplayName("シナリオ2-1: has_skillコマンドが正常に動作する")
        void test2_1_HasSkillCommand() {
            // Given: プレイヤーがスキルを習得済み
            when(mockAPI.hasSkill(mockPlayer, "fireball")).thenReturn(true);

            // When: has_skillコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"has_skill", "TestPlayer", "fireball"});

            // Then: 成功し、習得状況が表示される
            assertThat(result).isTrue();
            verify(mockSender).sendMessage(contains("はい"));
        }

        @Test
        @DisplayName("シナリオ2-2: unlock_skillコマンドが正常に動作する")
        void test2_2_UnlockSkillCommand() {
            // Given: スキル習得が成功
            when(mockAPI.unlockSkill(mockPlayer, "fireball")).thenReturn(true);

            // When: unlock_skillコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"unlock_skill", "TestPlayer", "fireball"});

            // Then: 成功し、スキルが習得される
            assertThat(result).isTrue();
            verify(mockAPI).unlockSkill(mockPlayer, "fireball");
        }

        @Test
        @DisplayName("シナリオ2-3: cast_skillコマンドが正常に動作する")
        void test2_3_CastSkillCommand() {
            // Given: スキル発動が成功
            when(mockAPI.castSkill(mockPlayer, "fireball")).thenReturn(true);

            // When: cast_skillコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"cast_skill", "TestPlayer", "fireball"});

            // Then: 成功し、スキルが発動される
            assertThat(result).isTrue();
            verify(mockAPI).castSkill(mockPlayer, "fireball");
        }

        @Test
        @DisplayName("シナリオ2-4: get_skill_levelコマンドが正常に動作する")
        void test2_4_GetSkillLevelCommand() {
            // Given: スキルレベルが3
            when(mockAPI.getSkillLevel(mockPlayer, "fireball")).thenReturn(3);

            // When: get_skill_levelコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"get_skill_level", "TestPlayer", "fireball"});

            // Then: 成功し、レベルが表示される
            assertThat(result).isTrue();
            verify(mockSender).sendMessage(contains("スキルレベル: 3"));
        }
    }

    // ==================== シナリオ3: ターゲット指定スキル発動 ====================

    @Nested
    @DisplayName("シナリオ3: ターゲット指定スキル発動")
    class Scenario3TargetedSkillCast {

        @Test
        @DisplayName("シナリオ3-1: cast_atコマンドが自分をターゲットに動作する")
        void test3_1_CastAtSelfCommand() {
            // Given: ターゲット指定スキル発動が成功
            when(mockAPI.castSkillAt(mockPlayer, "heal", mockPlayer)).thenReturn(true);

            // When: cast_atコマンドを実行（ターゲット省略）
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"cast_at", "TestPlayer", "heal"});

            // Then: 成功し、スキルが発動される
            assertThat(result).isTrue();
            verify(mockAPI).castSkillAt(mockPlayer, "heal", mockPlayer);
        }

        @Test
        @DisplayName("シナリオ3-2: cast_atコマンドが別プレイヤーをターゲットに動作する")
        void test3_2_CastAtOtherPlayerCommand() {
            // Given: ターゲット指定スキル発動が成功
            when(mockAPI.castSkillAt(mockPlayer, "heal", mockTargetPlayer)).thenReturn(true);

            // When: cast_atコマンドを実行（ターゲット指定）
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"cast_at", "TestPlayer", "heal", "TargetPlayer"});

            // Then: 成功し、スキルが発動される
            assertThat(result).isTrue();
            verify(mockAPI).castSkillAt(mockPlayer, "heal", mockTargetPlayer);
        }

        @Test
        @DisplayName("シナリオ3-3: get_targetコマンドがターゲットを返す")
        void test3_3_GetTargetCommand() {
            // Given: 最後のターゲットが存在
            when(mockAPI.getLastTargetedEntity(mockPlayer))
                    .thenReturn(java.util.Optional.of(mockTargetPlayer));

            // When: get_targetコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"get_target", "TestPlayer"});

            // Then: 成功し、ターゲットが表示される
            assertThat(result).isTrue();
            verify(mockSender).sendMessage(contains("ターゲット"));
        }

        @Test
        @DisplayName("シナリオ3-4: set_targetコマンドがターゲットを設定する")
        void test3_4_SetTargetCommand() {
            // When: set_targetコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"set_target", "TestPlayer", "TargetPlayer"});

            // Then: 成功し、ターゲットが設定される
            assertThat(result).isTrue();
            verify(mockAPI).setTargetedEntity(mockPlayer, mockTargetPlayer);
        }
    }

    // ==================== シナリオ4: コストタイプ指定スキル発動 ====================

    @Nested
    @DisplayName("シナリオ4: コストタイプ指定スキル発動")
    class Scenario4CostTypeSkillCast {

        @Test
        @DisplayName("シナリオ4-1: cast_with_costコマンドがMANAで動作する")
        void test4_1_CastWithManaCostCommand() {
            // Given: MANAコストスキル発動が成功
            when(mockAPI.castSkillWithCostType(mockPlayer, "skill", SkillCostType.MANA))
                    .thenReturn(true);

            // When: cast_with_costコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"cast_with_cost", "TestPlayer", "skill", "mana"});

            // Then: 成功し、スキルが発動される
            assertThat(result).isTrue();
            verify(mockAPI).castSkillWithCostType(mockPlayer, "skill", SkillCostType.MANA);
        }

        @Test
        @DisplayName("シナリオ4-2: cast_with_costコマンドがHPで動作する")
        void test4_2_CastWithHPCostCommand() {
            // Given: HPコストスキル発動が成功
            when(mockAPI.castSkillWithCostType(mockPlayer, "blood_sacrifice", SkillCostType.HP))
                    .thenReturn(true);

            // When: cast_with_costコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"cast_with_cost", "TestPlayer", "blood_sacrifice", "hp"});

            // Then: 成功し、スキルが発動される
            assertThat(result).isTrue();
            verify(mockAPI).castSkillWithCostType(mockPlayer, "blood_sacrifice", SkillCostType.HP);
        }

        @Test
        @DisplayName("シナリオ4-3: 無効なコストタイプでエラーが返る")
        void test4_3_InvalidCostTypeCommand() {
            // When: 無効なコストタイプでcast_with_costコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"cast_with_cost", "TestPlayer", "skill", "invalid"});

            // Then: 失敗する
            assertThat(result).isFalse();
            verify(mockSender).sendMessage(contains("無効なコストタイプ"));
        }
    }

    // ==================== シナリオ5: 範囲内エンティティ取得 ====================

    @Nested
    @DisplayName("シナリオ5: 範囲内エンティティ取得")
    class Scenario5AreaEntityRetrieval {

        @Test
        @DisplayName("シナリオ5-1: get_entities_in_areaコマンドが円形範囲で動作する")
        void test5_1_GetEntitiesInCircleCommand() {
            // Given: 円形範囲内にエンティティがいる
            Entity mockEntity = mock(Entity.class);
            when(mockEntity.getType()).thenReturn(org.bukkit.entity.EntityType.ZOMBIE);
            when(mockAPI.getEntitiesInArea(mockPlayer, "circle", new double[]{5.0}))
                    .thenReturn(java.util.List.of(mockEntity));

            // When: get_entities_in_areaコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"get_entities_in_area", "TestPlayer", "circle", "5.0"});

            // Then: 成功し、エンティティ数が表示される
            assertThat(result).isTrue();
            verify(mockAPI).getEntitiesInArea(mockPlayer, "circle", new double[]{5.0});
            verify(mockSender).sendMessage(contains("範囲内のエンティティ: 1件"));
        }

        @Test
        @DisplayName("シナリオ5-2: get_entities_in_areaコマンドが扇状範囲で動作する")
        void test5_2_GetEntitiesInConeCommand() {
            // Given: 扇状範囲内にエンティティがいる
            Entity mockEntity = mock(Entity.class);
            when(mockEntity.getType()).thenReturn(org.bukkit.entity.EntityType.SKELETON);
            when(mockAPI.getEntitiesInArea(mockPlayer, "cone", new double[]{90.0, 10.0}))
                    .thenReturn(java.util.List.of(mockEntity));

            // When: get_entities_in_areaコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"get_entities_in_area", "TestPlayer", "cone", "90.0", "10.0"});

            // Then: 成功する
            assertThat(result).isTrue();
            verify(mockAPI).getEntitiesInArea(mockPlayer, "cone", new double[]{90.0, 10.0});
        }

        @Test
        @DisplayName("シナリオ5-3: 範囲内にエンティティがいない場合")
        void test5_3_NoEntitiesInAreaCommand() {
            // Given: 範囲内にエンティティがいない
            when(mockAPI.getEntitiesInArea(mockPlayer, "circle", new double[]{5.0}))
                    .thenReturn(java.util.List.of());

            // When: get_entities_in_areaコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"get_entities_in_area", "TestPlayer", "circle", "5.0"});

            // Then: 成功し、0件が表示される
            assertThat(result).isTrue();
            verify(mockSender).sendMessage(contains("範囲内のエンティティ: 0件"));
        }
    }

    // ==================== シナリオ6: エラーハンドリング ====================

    @Nested
    @DisplayName("シナリオ6: エラーハンドリング")
    class Scenario6ErrorHandling {

        @Test
        @DisplayName("シナリオ6-1: 存在しないプレイヤーでエラーが返る")
        void test6_1_NonExistentPlayerError() {
            // Given: プレイヤーが存在しない
            mockedBukkit.when(() -> Bukkit.getPlayer("NonExistent")).thenReturn(null);

            // When: 存在しないプレイヤーでコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"get_level", "NonExistent"});

            // Then: 失敗する
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("シナリオ6-2: 無効なアクションでヘルプが表示される")
        void test6_2_InvalidActionShowsHelp() {
            // When: 無効なアクションでコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"invalid_action"});

            // Then: 失敗し、ヘルプが表示される
            assertThat(result).isFalse();
            verify(mockSender).sendMessage(contains("RPGPlugin API"));
        }

        @Test
        @DisplayName("シナリオ6-3: 引数不足でエラーメッセージが表示される")
        void test6_3_MissingArgumentsError() {
            // When: 引数不足でコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"set_level", "TestPlayer"}); // レベル引数が不足

            // Then: 失敗し、使用法が表示される
            assertThat(result).isFalse();
            verify(mockSender).sendMessage(contains("使用法"));
        }

        @Test
        @DisplayName("シナリオ6-4: 無効な数値フォーマットでエラーが返る")
        void test6_4_InvalidNumberFormatError() {
            // When: 無効な数値でコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"set_level", "TestPlayer", "not_a_number"});

            // Then: 失敗し、エラーメッセージが表示される
            assertThat(result).isFalse();
            verify(mockSender).sendMessage(contains("整数で指定"));
        }
    }

    // ==================== シナリオ7: 経済操作API ====================

    @Nested
    @DisplayName("シナリオ7: 経済操作API")
    class Scenario7EconomyAPI {

        @Test
        @DisplayName("シナリオ7-1: get_goldコマンドが正常に動作する")
        void test7_1_GetGoldCommand() {
            // Given: ゴールド残高が1000
            when(mockAPI.getGoldBalance(mockPlayer)).thenReturn(1000.0);

            // When: get_goldコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"get_gold", "TestPlayer"});

            // Then: 成功し、残高が表示される
            assertThat(result).isTrue();
            verify(mockSender).sendMessage(contains("ゴールド:"));
            verify(mockSender).sendMessage(contains("1000"));
        }

        @Test
        @DisplayName("シナリオ7-2: give_goldコマンドが正常に動作する")
        void test7_2_GiveGoldCommand() {
            // Given: ゴールド付与が成功
            when(mockAPI.depositGold(mockPlayer, 500.0)).thenReturn(true);

            // When: give_goldコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"give_gold", "TestPlayer", "500"});

            // Then: 成功し、ゴールドが付与される
            assertThat(result).isTrue();
            verify(mockAPI).depositGold(mockPlayer, 500.0);
        }

        @Test
        @DisplayName("シナリオ7-3: has_goldコマンドが正常に動作する")
        void test7_3_HasGoldCommand() {
            // Given: 十分なゴールドを持っている
            when(mockAPI.hasEnoughGold(mockPlayer, 500.0)).thenReturn(true);

            // When: has_goldコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"has_gold", "TestPlayer", "500"});

            // Then: 成功し、所持確認が表示される
            assertThat(result).isTrue();
            verify(mockAPI).hasEnoughGold(mockPlayer, 500.0);
        }
    }

    // ==================== シナリオ8: ダメージ計算API ====================

    @Nested
    @DisplayName("シナリオ8: ダメージ計算API")
    class Scenario8DamageCalculationAPI {

        @Test
        @DisplayName("シナリオ8-1: calculate_damageコマンドが正常に動作する")
        void test8_1_CalculateDamageCommand() {
            // Given: ダメージ計算結果が100
            when(mockAPI.calculateDamage(mockPlayer, mockTargetPlayer)).thenReturn(100.0);

            // When: calculate_damageコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"calculate_damage", "TestPlayer", "TargetPlayer"});

            // Then: 成功し、ダメージが表示される
            assertThat(result).isTrue();
            verify(mockAPI).calculateDamage(mockPlayer, mockTargetPlayer);
            verify(mockSender).sendMessage(contains("計算ダメージ:"));
            verify(mockSender).sendMessage(contains("100"));
        }

        @Test
        @DisplayName("シナリオ8-2: ダメージ計算で小数値も扱える")
        void test8_2_DamageCalculationWithDecimals() {
            // Given: ダメージ計算結果が123.45
            when(mockAPI.calculateDamage(mockPlayer, mockTargetPlayer)).thenReturn(123.45);

            // When: calculate_damageコマンドを実行
            boolean result = skriptBridge.handleCall(mockSender,
                    new String[]{"calculate_damage", "TestPlayer", "TargetPlayer"});

            // Then: 成功し、小数値が表示される
            assertThat(result).isTrue();
            verify(mockSender).sendMessage(contains("123.45"));
        }
    }
}
