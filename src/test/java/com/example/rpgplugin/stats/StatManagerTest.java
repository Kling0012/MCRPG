package com.example.rpgplugin.stats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * StatManagerのユニットテスト
 *
 * <p>ステータス管理システムのテストを行います。</p>
 *
 * 設計原則:
 * - SOLID-S: StatManagerのテストに特化
 * - KISS: シンプルなテストケース
 * - 読みやすさ: テスト名で振る舞いを明示
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("StatManager テスト")
class StatManagerTest {

    private StatManager statManager;

    @BeforeEach
    void setUp() {
        statManager = new StatManager(10, 3);
    }

    // ==================== 基礎ステータスの設定・取得 ====================

    @Test
    @DisplayName("基礎ステータス取得: 初期値")
    void testGetBaseStat_InitialValue() {
        // When: 各ステータスの初期値を取得
        int strength = statManager.getBaseStat(Stat.STRENGTH);
        int intelligence = statManager.getBaseStat(Stat.INTELLIGENCE);
        int spirit = statManager.getBaseStat(Stat.SPIRIT);
        int vitality = statManager.getBaseStat(Stat.VITALITY);
        int dexterity = statManager.getBaseStat(Stat.DEXTERITY);

        // Then: 全て10で初期化される
        assertThat(strength).isEqualTo(10);
        assertThat(intelligence).isEqualTo(10);
        assertThat(spirit).isEqualTo(10);
        assertThat(vitality).isEqualTo(10);
        assertThat(dexterity).isEqualTo(10);
    }

    @Test
    @DisplayName("基礎ステータス設定: 正常値")
    void testSetBaseStat_ValidValue() {
        // When: STRを50に設定
        statManager.setBaseStat(Stat.STRENGTH, 50);

        // Then: 基礎ステータスが50になる
        assertThat(statManager.getBaseStat(Stat.STRENGTH)).isEqualTo(50);
    }

    @Test
    @DisplayName("基礎ステータス設定: 下限値(0)")
    void testSetBaseStat_MinValue() {
        // When: STRを0に設定
        statManager.setBaseStat(Stat.STRENGTH, 0);

        // Then: 基礎ステータスが0になる
        assertThat(statManager.getBaseStat(Stat.STRENGTH)).isEqualTo(0);
    }

    @Test
    @DisplayName("基礎ステータス設定: 上限値(255)")
    void testSetBaseStat_MaxValue() {
        // When: STRを255に設定
        statManager.setBaseStat(Stat.STRENGTH, 255);

        // Then: 基礎ステータスが255になる
        assertThat(statManager.getBaseStat(Stat.STRENGTH)).isEqualTo(255);
    }

    @Test
    @DisplayName("基礎ステータス設定: 負値は例外")
    void testSetBaseStat_NegativeValue() {
        // When: STRを-1に設定
        // Then: IllegalArgumentExceptionがスローされる
        assertThatThrownBy(() -> statManager.setBaseStat(Stat.STRENGTH, -1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Base stat value cannot be negative");
    }

    @Test
    @DisplayName("基礎ステータス設定: 上限値(255)は許容")
    void testSetBaseStat_ExceedsMax() {
        // When: STRを255に設定（最大値）
        // Then: 設定成功（255は許容される）
        statManager.setBaseStat(Stat.STRENGTH, 255);
        assertThat(statManager.getBaseStat(Stat.STRENGTH)).isEqualTo(255);
    }

    // ==================== 最終ステータス計算 ====================

    @Test
    @DisplayName("最終ステータス計算: 修正値なし")
    void testGetFinalStat_NoModifiers() {
        // Given: STRを20に設定
        statManager.setBaseStat(Stat.STRENGTH, 20);

        // When: 最終ステータスを取得
        int finalStat = statManager.getFinalStat(Stat.STRENGTH);

        // Then: 基礎ステータスと同じ値
        assertThat(finalStat).isEqualTo(20);
    }

    @Test
    @DisplayName("最終ステータス計算: FLAT修正のみ")
    void testGetFinalStat_FlatModifier() {
        // Given: 基礎STR20、FLAT+30の修正値
        statManager.setBaseStat(Stat.STRENGTH, 20);
        StatModifier flatModifier = new StatModifier(
            "TestSource",
            StatModifier.Type.FLAT,
            30.0
        );
        statManager.addModifier(Stat.STRENGTH, flatModifier);

        // When: 最終ステータスを取得
        int finalStat = statManager.getFinalStat(Stat.STRENGTH);

        // Then: 20 + 30 = 50
        assertThat(finalStat).isEqualTo(50);
    }

    @Test
    @DisplayName("最終ステータス計算: MULTIPLIER修正のみ")
    void testGetFinalStat_MultiplierModifier() {
        // Given: 基礎STR20、MULTIPLIER+50%の修正値
        statManager.setBaseStat(Stat.STRENGTH, 20);
        StatModifier multiplierModifier = new StatModifier(
            "TestSource",
            StatModifier.Type.MULTIPLIER,
            0.5
        );
        statManager.addModifier(Stat.STRENGTH, multiplierModifier);

        // When: 最終ステータスを取得
        int finalStat = statManager.getFinalStat(Stat.STRENGTH);

        // Then: 20 * 1.5 = 30
        assertThat(finalStat).isEqualTo(30);
    }

    @Test
    @DisplayName("最終ステータス計算: FINAL修正のみ")
    void testGetFinalStat_FinalModifier() {
        // Given: 基礎STR20、FINAL+15の修正値
        statManager.setBaseStat(Stat.STRENGTH, 20);
        StatModifier finalModifier = new StatModifier(
            "TestSource",
            StatModifier.Type.FINAL,
            15.0
        );
        statManager.addModifier(Stat.STRENGTH, finalModifier);

        // When: 最終ステータスを取得
        int finalStat = statManager.getFinalStat(Stat.STRENGTH);

        // Then: 20 + 15 = 35
        assertThat(finalStat).isEqualTo(35);
    }

    @Test
    @DisplayName("最終ステータス計算: 全修正タイプの組み合わせ")
    void testGetFinalStat_AllModifierTypes() {
        // Given: 基礎STR20、FLAT+30、MULTIPLIER+50%、FINAL+15
        statManager.setBaseStat(Stat.STRENGTH, 20);
        StatModifier flatModifier = new StatModifier(
            "TestSource1",
            StatModifier.Type.FLAT,
            30.0
        );
        StatModifier multiplierModifier = new StatModifier(
            "TestSource2",
            StatModifier.Type.MULTIPLIER,
            0.5
        );
        StatModifier finalModifier = new StatModifier(
            "TestSource3",
            StatModifier.Type.FINAL,
            15.0
        );

        statManager.addModifier(Stat.STRENGTH, flatModifier);
        statManager.addModifier(Stat.STRENGTH, multiplierModifier);
        statManager.addModifier(Stat.STRENGTH, finalModifier);

        // When: 最終ステータスを取得
        int finalStat = statManager.getFinalStat(Stat.STRENGTH);

        // Then: ((20 + 30) * 1.5) + 15 = 75 + 15 = 90
        assertThat(finalStat).isEqualTo(90);
    }

    // ==================== 修正値の追加・削除 ====================

    @Test
    @DisplayName("修正値追加: 正常追加")
    void testAddModifier_Valid() {
        // Given: FLAT+10の修正値
        StatModifier modifier = new StatModifier(
            "TestSource",
            StatModifier.Type.FLAT,
            10.0
        );

        // When: 修正値を追加
        statManager.addModifier(Stat.STRENGTH, modifier);

        // Then: 修正値が1つ存在
        assertThat(statManager.getModifiers(Stat.STRENGTH)).hasSize(1);
        assertThat(statManager.getModifiers(Stat.STRENGTH).get(0)).isEqualTo(modifier);
    }

    @Test
    @DisplayName("修正値追加: null statは例外")
    void testAddModifier_NullStat() {
        // Given: FLAT+10の修正値
        StatModifier modifier = new StatModifier(
            "TestSource",
            StatModifier.Type.FLAT,
            10.0
        );

        // When: null statで追加
        // Then: IllegalArgumentExceptionがスローされる
        assertThatThrownBy(() -> statManager.addModifier(null, modifier))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Stat cannot be null");
    }

    @Test
    @DisplayName("修正値追加: null modifierは例外")
    void testAddModifier_NullModifier() {
        // When: null modifierで追加
        // Then: IllegalArgumentExceptionがスローされる
        assertThatThrownBy(() -> statManager.addModifier(Stat.STRENGTH, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Modifier cannot be null");
    }

    @Test
    @DisplayName("修正値削除: ID指定")
    void testRemoveModifier_ById() {
        // Given: 修正値を2つ追加
        StatModifier modifier1 = new StatModifier(
            "TestSource",
            StatModifier.Type.FLAT,
            10.0
        );
        StatModifier modifier2 = new StatModifier(
            "TestSource2",
            StatModifier.Type.FLAT,
            20.0
        );
        statManager.addModifier(Stat.STRENGTH, modifier1);
        statManager.addModifier(Stat.STRENGTH, modifier2);

        // When: 1つを削除（IDで削除）
        UUID modifierId = statManager.getModifiers(Stat.STRENGTH).get(0).getId();
        boolean removed = statManager.removeModifier(Stat.STRENGTH, modifierId);

        // Then: 削除成功、残り1つ
        assertThat(removed).isTrue();
        assertThat(statManager.getModifiers(Stat.STRENGTH)).hasSize(1);
    }

    @Test
    @DisplayName("修正値削除: 存在しないID")
    void testRemoveModifier_NonExistent() {
        // When: 存在しないIDで削除
        boolean removed = statManager.removeModifier(Stat.STRENGTH, UUID.randomUUID());

        // Then: 削除失敗
        assertThat(removed).isFalse();
    }

    @Test
    @DisplayName("修正値削除: ソース指定")
    void testRemoveModifiersBySource() {
        // Given: 同一ソースの修正値を2つ追加
        StatModifier modifier1 = new StatModifier(
            "CommonSource",
            StatModifier.Type.FLAT,
            10.0
        );
        StatModifier modifier2 = new StatModifier(
            "CommonSource",
            StatModifier.Type.MULTIPLIER,
            0.5
        );
        StatModifier modifier3 = new StatModifier(
            "OtherSource",
            StatModifier.Type.FLAT,
            30.0
        );
        statManager.addModifier(Stat.STRENGTH, modifier1);
        statManager.addModifier(Stat.STRENGTH, modifier2);
        statManager.addModifier(Stat.STRENGTH, modifier3);

        // When: CommonSourceの修正値を全削除
        int removedCount = statManager.removeModifiersBySource(Stat.STRENGTH, "CommonSource");

        // Then: 2つ削除され、1つ残る
        assertThat(removedCount).isEqualTo(2);
        assertThat(statManager.getModifiers(Stat.STRENGTH)).hasSize(1);
        assertThat(statManager.getModifiers(Stat.STRENGTH).get(0).getSource()).isEqualTo("OtherSource");
    }

    // ==================== 有効期限切れ修正値の自動削除 ====================

    @Test
    @DisplayName("期限切れ修正値の削除: 期限切れあり")
    void testCleanupExpiredModifiers_HasExpired() {
        // Given: 期限切れの修正値（1秒で期限切れ）
        StatModifier expiredModifier = new StatModifier(
            "TestSource",
            StatModifier.Type.FLAT,
            10.0,
            1000
        );
        statManager.addModifier(Stat.STRENGTH, expiredModifier);

        // When: 2秒待機してクリーンアップ
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            fail("Sleep interrupted");
        }
        int removedCount = statManager.cleanupExpiredModifiers(Stat.STRENGTH);

        // Then: 1つ削除される
        assertThat(removedCount).isEqualTo(1);
        assertThat(statManager.getModifiers(Stat.STRENGTH)).isEmpty();
    }

    @Test
    @DisplayName("期限切れ修正値の削除: 永続修正値は残る")
    void testCleanupExpiredModifiers_PermanentRemains() {
        // Given: 永続修正値と期限付き修正値
        StatModifier permanentModifier = new StatModifier(
            "PermanentSource",
            StatModifier.Type.FLAT,
            10.0
        );
        StatModifier temporaryModifier = new StatModifier(
            "TemporarySource",
            StatModifier.Type.FLAT,
            20.0,
            1000
        );
        statManager.addModifier(Stat.STRENGTH, permanentModifier);
        statManager.addModifier(Stat.STRENGTH, temporaryModifier);

        // When: 2秒待機してクリーンアップ
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            fail("Sleep interrupted");
        }
        int removedCount = statManager.cleanupExpiredModifiers(Stat.STRENGTH);

        // Then: 1つ削除され、永続修正値は残る
        assertThat(removedCount).isEqualTo(1);
        assertThat(statManager.getModifiers(Stat.STRENGTH)).hasSize(1);
        assertThat(statManager.getModifiers(Stat.STRENGTH).get(0).getSource()).isEqualTo("PermanentSource");
    }

    @Test
    @DisplayName("期限切れ修正値の全クリーンアップ")
    void testCleanupAllExpiredModifiers() {
        // Given: 複数ステータスに期限付き修正値
        StatModifier strModifier = new StatModifier(
            "TestSource1",
            StatModifier.Type.FLAT,
            10.0,
            1000
        );
        StatModifier intModifier = new StatModifier(
            "TestSource2",
            StatModifier.Type.FLAT,
            10.0,
            1000
        );
        statManager.addModifier(Stat.STRENGTH, strModifier);
        statManager.addModifier(Stat.INTELLIGENCE, intModifier);

        // When: 2秒待機して全クリーンアップ
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            fail("Sleep interrupted");
        }
        int totalRemoved = statManager.cleanupAllExpiredModifiers();

        // Then: 2つ削除される
        assertThat(totalRemoved).isEqualTo(2);
        assertThat(statManager.getModifiers(Stat.STRENGTH)).isEmpty();
        assertThat(statManager.getModifiers(Stat.INTELLIGENCE)).isEmpty();
    }

    // ==================== ステータスポイント配分 ====================

    @Test
    @DisplayName("利用可能ポイント: 初期値")
    void testGetAvailablePoints_Initial() {
        // When: 初期ポイントを取得
        int points = statManager.getAvailablePoints();

        // Then: 3ポイント（初期配分用）
        assertThat(points).isEqualTo(3);
    }

    @Test
    @DisplayName("利用可能ポイント: 設定")
    void testSetAvailablePoints() {
        // When: 5ポイントに設定
        statManager.setAvailablePoints(5);

        // Then: 5ポイント
        assertThat(statManager.getAvailablePoints()).isEqualTo(5);
    }

    @Test
    @DisplayName("利用可能ポイント: 追加")
    void testAddAvailablePoints() {
        // When: 2ポイント追加
        statManager.addAvailablePoints(2);

        // Then: 3 + 2 = 5ポイント
        assertThat(statManager.getAvailablePoints()).isEqualTo(5);
    }

    @Test
    @DisplayName("ステータス配分: ポイント消費")
    void testAllocatePoint_ConsumesPoint() {
        // Given: 初期3ポイント
        int initialPoints = statManager.getAvailablePoints();

        // When: STRに1ポイント配分
        boolean allocated = statManager.allocatePoint(Stat.STRENGTH, 1);

        // Then: 配分成功、ポイント消費
        assertThat(allocated).isTrue();
        assertThat(statManager.getAvailablePoints()).isEqualTo(initialPoints - 1);
        assertThat(statManager.getBaseStat(Stat.STRENGTH)).isEqualTo(11); // 初期値10 + 1
    }

    @Test
    @DisplayName("ステータス配分: ポイント不足")
    void testAllocatePoint_NotEnoughPoints() {
        // Given: ポイント0
        statManager.setAvailablePoints(0);

        // When: STRに配分試行
        boolean allocated = statManager.allocatePoint(Stat.STRENGTH, 1);

        // Then: 配分失敗
        assertThat(allocated).isFalse();
        assertThat(statManager.getBaseStat(Stat.STRENGTH)).isEqualTo(10);
    }

    @Test
    @DisplayName("ステータス配分リセット: ポイント返却")
    void testResetAllocation() {
        // Given: STRを15に配分済み
        statManager.setBaseStat(Stat.STRENGTH, 15);
        statManager.setAvailablePoints(0);

        // When: リセット実行
        statManager.resetAllocation();

        // Then: 全ステータス10に、ポイント返却（15-10=5ポイント）
        assertThat(statManager.getBaseStat(Stat.STRENGTH)).isEqualTo(10);
        assertThat(statManager.getAvailablePoints()).isEqualTo(5);
    }

    // ==================== 総レベル管理 ====================

    @Test
    @DisplayName("総レベル: 初期値")
    void testGetTotalLevel_Initial() {
        // When: 初期レベルを取得
        int level = statManager.getTotalLevel();

        // Then: レベル1
        assertThat(level).isEqualTo(1);
    }

    @Test
    @DisplayName("総レベル: 設定")
    void testSetTotalLevel() {
        // When: レベル50に設定
        statManager.setTotalLevel(50);

        // Then: レベル50
        assertThat(statManager.getTotalLevel()).isEqualTo(50);
    }

    @Test
    @DisplayName("全ステータス取得: 基礎値")
    void testGetAllBaseStats() {
        // Given: 各ステータスに値を設定
        statManager.setBaseStat(Stat.STRENGTH, 20);
        statManager.setBaseStat(Stat.INTELLIGENCE, 30);
        statManager.setBaseStat(Stat.SPIRIT, 25);
        statManager.setBaseStat(Stat.VITALITY, 35);
        statManager.setBaseStat(Stat.DEXTERITY, 15);

        // When: 全基礎ステータスを取得
        var allStats = statManager.getAllBaseStats();

        // Then: 全ステータスがマッピングされている
        assertThat(allStats).hasSize(5);
        assertThat(allStats.get(Stat.STRENGTH)).isEqualTo(20);
        assertThat(allStats.get(Stat.INTELLIGENCE)).isEqualTo(30);
        assertThat(allStats.get(Stat.SPIRIT)).isEqualTo(25);
        assertThat(allStats.get(Stat.VITALITY)).isEqualTo(35);
        assertThat(allStats.get(Stat.DEXTERITY)).isEqualTo(15);
    }

    @Test
    @DisplayName("全ステータス取得: 最終値")
    void testGetAllFinalStats() {
        // Given: STRに修正値を追加
        statManager.setBaseStat(Stat.STRENGTH, 20);
        StatModifier modifier = new StatModifier(
            "TestSource",
            StatModifier.Type.FLAT,
            10.0
        );
        statManager.addModifier(Stat.STRENGTH, modifier);

        // When: 全最終ステータスを取得
        var allStats = statManager.getAllFinalStats();

        // Then: 修正適用済みの値
        assertThat(allStats.get(Stat.STRENGTH)).isEqualTo(30); // 20 + 10
        assertThat(allStats.get(Stat.INTELLIGENCE)).isEqualTo(10); // 初期値
    }

    // ==================== 修正値クリア ====================

    @Test
    @DisplayName("修正値クリア: 単一ステータス")
    void testClearModifiers() {
        // Given: STRに修正値を2つ追加
        statManager.addModifier(Stat.STRENGTH, new StatModifier(
            "TestSource1", StatModifier.Type.FLAT, 10.0
        ));
        statManager.addModifier(Stat.STRENGTH, new StatModifier(
            "TestSource2", StatModifier.Type.FLAT, 20.0
        ));

        // When: クリア実行
        statManager.clearModifiers(Stat.STRENGTH);

        // Then: 修正値が0になる
        assertThat(statManager.getModifiers(Stat.STRENGTH)).isEmpty();
    }

    @Test
    @DisplayName("修正値クリア: 全ステータス")
    void testClearAllModifiers() {
        // Given: 複数ステータスに修正値を追加
        statManager.addModifier(Stat.STRENGTH, new StatModifier(
            "TestSource1", StatModifier.Type.FLAT, 10.0
        ));
        statManager.addModifier(Stat.INTELLIGENCE, new StatModifier(
            "TestSource2", StatModifier.Type.FLAT, 10.0
        ));

        // When: 全クリア実行
        statManager.clearAllModifiers();

        // Then: 全修正値が0になる
        assertThat(statManager.getModifiers(Stat.STRENGTH)).isEmpty();
        assertThat(statManager.getModifiers(Stat.INTELLIGENCE)).isEmpty();
    }
}
