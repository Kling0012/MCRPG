package com.example.rpgplugin.stats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * PlayerStatsのユニットテスト
 *
 * <p>プレイヤーのステータスデータ管理システムのテストを行います。</p>
 *
 * 設計原則:
 * - SOLID-S: PlayerStatsのテストに特化
 * - KISS: シンプルなテストケース
 * - 読みやすさ: テスト名で振る舞いを明示
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("PlayerStats テスト")
class PlayerStatsTest {

    private PlayerStats playerStats;

    @BeforeEach
    void setUp() {
        playerStats = new PlayerStats();
    }

    // ==================== コンストラクタ ====================

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("全ステータスが0で初期化される")
        void constructor_InitializesWithZeros() {
            // Then: 全ステータスが0
            for (Stat stat : Stat.values()) {
                assertThat(playerStats.getBaseStat(stat)).isZero();
                assertThat(playerStats.getAutoStat(stat)).isZero();
            }
            assertThat(playerStats.getAvailablePoints()).isZero();
        }
    }

    // ==================== 手動配分ステータス ====================

    @Nested
    @DisplayName("BaseStat Operations")
    class BaseStatTests {

        @Test
        @DisplayName("手動配分値を取得: 初期値は0")
        void getBaseStat_InitialValueIsZero() {
            assertThat(playerStats.getBaseStat(Stat.STRENGTH)).isZero();
        }

        @Test
        @DisplayName("手動配分値を設定")
        void setBaseStat_SetsValue() {
            playerStats.setBaseStat(Stat.STRENGTH, 50);

            assertThat(playerStats.getBaseStat(Stat.STRENGTH)).isEqualTo(50);
        }

        @Test
        @DisplayName("手動配分値設定: 負値は0に丸められる")
        void setBaseStat_NegativeValue_ClampedToZero() {
            playerStats.setBaseStat(Stat.STRENGTH, -10);

            assertThat(playerStats.getBaseStat(Stat.STRENGTH)).isZero();
        }

        @Test
        @DisplayName("全手動配分値を取得")
        void getAllBaseStats_ReturnsAllStats() {
            playerStats.setBaseStat(Stat.STRENGTH, 20);
            playerStats.setBaseStat(Stat.INTELLIGENCE, 30);
            playerStats.setBaseStat(Stat.SPIRIT, 25);

            Map<Stat, Integer> stats = playerStats.getAllBaseStats();

            assertThat(stats).hasSize(5);
            assertThat(stats.get(Stat.STRENGTH)).isEqualTo(20);
            assertThat(stats.get(Stat.INTELLIGENCE)).isEqualTo(30);
            assertThat(stats.get(Stat.SPIRIT)).isEqualTo(25);
            assertThat(stats.get(Stat.VITALITY)).isZero();
            assertThat(stats.get(Stat.DEXTERITY)).isZero();
        }
    }

    // ==================== 自動配分ステータス ====================

    @Nested
    @DisplayName("AutoStat Operations")
    class AutoStatTests {

        @Test
        @DisplayName("自動配分値を取得: 初期値は0")
        void getAutoStat_InitialValueIsZero() {
            assertThat(playerStats.getAutoStat(Stat.STRENGTH)).isZero();
        }

        @Test
        @DisplayName("自動配分値を設定")
        void setAutoStat_SetsValue() {
            playerStats.setAutoStat(Stat.STRENGTH, 30);

            assertThat(playerStats.getAutoStat(Stat.STRENGTH)).isEqualTo(30);
        }

        @Test
        @DisplayName("自動配分値設定: 負値は0に丸められる")
        void setAutoStat_NegativeValue_ClampedToZero() {
            playerStats.setAutoStat(Stat.STRENGTH, -10);

            assertThat(playerStats.getAutoStat(Stat.STRENGTH)).isZero();
        }

        @Test
        @DisplayName("全自動配分値を取得")
        void getAllAutoStats_ReturnsAllStats() {
            playerStats.setAutoStat(Stat.STRENGTH, 10);
            playerStats.setAutoStat(Stat.VITALITY, 15);

            Map<Stat, Integer> stats = playerStats.getAllAutoStats();

            assertThat(stats).hasSize(5);
            assertThat(stats.get(Stat.STRENGTH)).isEqualTo(10);
            assertThat(stats.get(Stat.VITALITY)).isEqualTo(15);
        }

        @Test
        @DisplayName("自動配分ポイント総数を取得")
        void getTotalAutoPoints_ReturnsSum() {
            playerStats.setAutoStat(Stat.STRENGTH, 10);
            playerStats.setAutoStat(Stat.INTELLIGENCE, 5);
            playerStats.setAutoStat(Stat.VITALITY, 15);

            assertThat(playerStats.getTotalAutoPoints()).isEqualTo(30);
        }
    }

    // ==================== 合計ステータス ====================

    @Nested
    @DisplayName("TotalStat Operations")
    class TotalStatTests {

        @Test
        @DisplayName("合計ステータス: 手動+自動")
        void getTotalStat_ReturnsSum() {
            playerStats.setBaseStat(Stat.STRENGTH, 20);
            playerStats.setAutoStat(Stat.STRENGTH, 10);

            assertThat(playerStats.getTotalStat(Stat.STRENGTH)).isEqualTo(30);
        }

        @Test
        @DisplayName("合計ステータス: 片方のみ")
        void getTotalStat_OnlyOneType() {
            playerStats.setBaseStat(Stat.STRENGTH, 15);

            assertThat(playerStats.getTotalStat(Stat.STRENGTH)).isEqualTo(15);
        }

        @Test
        @DisplayName("全合計ステータスを取得")
        void getAllTotalStats_ReturnsAllStats() {
            playerStats.setBaseStat(Stat.STRENGTH, 20);
            playerStats.setAutoStat(Stat.STRENGTH, 10);
            playerStats.setBaseStat(Stat.INTELLIGENCE, 15);

            Map<Stat, Integer> stats = playerStats.getAllTotalStats();

            assertThat(stats.get(Stat.STRENGTH)).isEqualTo(30);
            assertThat(stats.get(Stat.INTELLIGENCE)).isEqualTo(15);
            assertThat(stats.get(Stat.VITALITY)).isZero();
        }
    }

    // ==================== ポイント管理 ====================

    @Nested
    @DisplayName("Available Points Operations")
    class AvailablePointsTests {

        @Test
        @DisplayName("残りポイント: 初期値は0")
        void getAvailablePoints_InitialValueIsZero() {
            assertThat(playerStats.getAvailablePoints()).isZero();
        }

        @Test
        @DisplayName("残りポイントを設定")
        void setAvailablePoints_SetsValue() {
            playerStats.setAvailablePoints(10);

            assertThat(playerStats.getAvailablePoints()).isEqualTo(10);
        }

        @Test
        @DisplayName("残りポイント設定: 負値は0に丸められる")
        void setAvailablePoints_NegativeValue_ClampedToZero() {
            playerStats.setAvailablePoints(-5);

            assertThat(playerStats.getAvailablePoints()).isZero();
        }

        @Test
        @DisplayName("ポイントを追加")
        void addPoints_AddsToAvailable() {
            playerStats.setAvailablePoints(5);
            playerStats.addPoints(10);

            assertThat(playerStats.getAvailablePoints()).isEqualTo(15);
        }

        @Test
        @DisplayName("ポイント追加: 負値は加算されない")
        void addPoints_NegativeValue_NotAdded() {
            playerStats.setAvailablePoints(5);
            playerStats.addPoints(-10);

            assertThat(playerStats.getAvailablePoints()).isEqualTo(5);
        }

        @Test
        @DisplayName("ポイント追加: 0は加算されない")
        void addPoints_Zero_NotAdded() {
            playerStats.setAvailablePoints(5);
            playerStats.addPoints(0);

            assertThat(playerStats.getAvailablePoints()).isEqualTo(5);
        }
    }

    // ==================== ポイント配分 ====================

    @Nested
    @DisplayName("Point Allocation")
    class AllocationTests {

        @Test
        @DisplayName("ステータスにポイントを配分")
        void allocatePoint_AllocatesSuccessfully() {
            playerStats.setAvailablePoints(10);
            int allocated = playerStats.allocatePoint(Stat.STRENGTH, 5);

            assertThat(allocated).isEqualTo(5);
            assertThat(playerStats.getBaseStat(Stat.STRENGTH)).isEqualTo(5);
            assertThat(playerStats.getAvailablePoints()).isEqualTo(5);
        }

        @Test
        @DisplayName("ポイント配分: ポイント不足の場合は全て使う")
        void allocatePoint_NotEnoughPoints_UsesAllAvailable() {
            playerStats.setAvailablePoints(3);
            int allocated = playerStats.allocatePoint(Stat.STRENGTH, 10);

            assertThat(allocated).isEqualTo(3);
            assertThat(playerStats.getBaseStat(Stat.STRENGTH)).isEqualTo(3);
            assertThat(playerStats.getAvailablePoints()).isZero();
        }

        @Test
        @DisplayName("ポイント配分: ポイントが0の場合は配分されない")
        void allocatePoint_NoPoints_ReturnsZero() {
            int allocated = playerStats.allocatePoint(Stat.STRENGTH, 5);

            assertThat(allocated).isZero();
            assertThat(playerStats.getBaseStat(Stat.STRENGTH)).isZero();
        }

        @Test
        @DisplayName("ポイント配分: 負値は無視される")
        void allocatePoint_NegativeAmount_ReturnsZero() {
            playerStats.setAvailablePoints(10);
            int allocated = playerStats.allocatePoint(Stat.STRENGTH, -5);

            assertThat(allocated).isZero();
            assertThat(playerStats.getAvailablePoints()).isEqualTo(10);
        }

        @Test
        @DisplayName("ポイント配分: 0は無視される")
        void allocatePoint_ZeroAmount_ReturnsZero() {
            playerStats.setAvailablePoints(10);
            int allocated = playerStats.allocatePoint(Stat.STRENGTH, 0);

            assertThat(allocated).isZero();
            assertThat(playerStats.getAvailablePoints()).isEqualTo(10);
        }

        @Test
        @DisplayName("ポイント配分: null statは例外")
        void allocatePoint_NullStat_ThrowsException() {
            playerStats.setAvailablePoints(10);

            assertThatThrownBy(() -> playerStats.allocatePoint(null, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stat cannot be null");
        }

        @Test
        @DisplayName("ポイント配分: 既存値に加算される")
        void allocatePoint_AddsToExisting() {
            playerStats.setAvailablePoints(10);
            playerStats.setBaseStat(Stat.STRENGTH, 5);

            int allocated = playerStats.allocatePoint(Stat.STRENGTH, 3);

            assertThat(allocated).isEqualTo(3);
            assertThat(playerStats.getBaseStat(Stat.STRENGTH)).isEqualTo(8);
        }
    }

    // ==================== ポイント削除 ====================

    @Nested
    @DisplayName("Point Deallocation")
    class DeallocationTests {

        @Test
        @DisplayName("ステータスからポイントを削除")
        void deallocatePoint_DeallocatesSuccessfully() {
            playerStats.setBaseStat(Stat.STRENGTH, 20);
            int deallocated = playerStats.deallocatePoint(Stat.STRENGTH, 5);

            assertThat(deallocated).isEqualTo(5);
            assertThat(playerStats.getBaseStat(Stat.STRENGTH)).isEqualTo(15);
            assertThat(playerStats.getAvailablePoints()).isEqualTo(5);
        }

        @Test
        @DisplayName("ポイント削除: 値不足の場合は全て削除")
        void deallocatePoint_NotEnoughValue_RemovesAll() {
            playerStats.setBaseStat(Stat.STRENGTH, 3);
            int deallocated = playerStats.deallocatePoint(Stat.STRENGTH, 10);

            assertThat(deallocated).isEqualTo(3);
            assertThat(playerStats.getBaseStat(Stat.STRENGTH)).isZero();
            assertThat(playerStats.getAvailablePoints()).isEqualTo(3);
        }

        @Test
        @DisplayName("ポイント削除: 値が0の場合は何もしない")
        void deallocatePoint_NoValue_ReturnsZero() {
            int deallocated = playerStats.deallocatePoint(Stat.STRENGTH, 5);

            assertThat(deallocated).isZero();
            assertThat(playerStats.getAvailablePoints()).isZero();
        }

        @Test
        @DisplayName("ポイント削除: 負値は無視される")
        void deallocatePoint_NegativeAmount_ReturnsZero() {
            playerStats.setBaseStat(Stat.STRENGTH, 10);
            int deallocated = playerStats.deallocatePoint(Stat.STRENGTH, -5);

            assertThat(deallocated).isZero();
            assertThat(playerStats.getBaseStat(Stat.STRENGTH)).isEqualTo(10);
        }

        @Test
        @DisplayName("ポイント削除: 0は無視される")
        void deallocatePoint_ZeroAmount_ReturnsZero() {
            playerStats.setBaseStat(Stat.STRENGTH, 10);
            int deallocated = playerStats.deallocatePoint(Stat.STRENGTH, 0);

            assertThat(deallocated).isZero();
        }

        @Test
        @DisplayName("ポイント削除: null statは例外")
        void deallocatePoint_NullStat_ThrowsException() {
            assertThatThrownBy(() -> playerStats.deallocatePoint(null, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stat cannot be null");
        }
    }

    // ==================== 総計ポイント ====================

    @Nested
    @DisplayName("Total Points")
    class TotalPointsTests {

        @Test
        @DisplayName("配分済みポイント総数を取得")
        void getTotalAllocatedPoints_ReturnsSum() {
            playerStats.setBaseStat(Stat.STRENGTH, 20);
            playerStats.setBaseStat(Stat.INTELLIGENCE, 15);
            playerStats.setBaseStat(Stat.VITALITY, 10);

            assertThat(playerStats.getTotalAllocatedPoints()).isEqualTo(45);
        }

        @Test
        @DisplayName("配分済みポイント総数: 全て0の場合")
        void getTotalAllocatedPoints_AllZero_ReturnsZero() {
            assertThat(playerStats.getTotalAllocatedPoints()).isZero();
        }
    }

    // ==================== コピー防御 ====================

    @Nested
    @DisplayName("Defensive Copying")
    class DefensiveCopyTests {

        @Test
        @DisplayName("getAllBaseStats: 返されたマップの変更は元に影響しない")
        void getAllBaseStats_ReturnsDefensiveCopy() {
            playerStats.setBaseStat(Stat.STRENGTH, 20);

            Map<Stat, Integer> stats = playerStats.getAllBaseStats();
            stats.put(Stat.STRENGTH, 999);

            assertThat(playerStats.getBaseStat(Stat.STRENGTH)).isEqualTo(20);
        }

        @Test
        @DisplayName("getAllAutoStats: 返されたマップの変更は元に影響しない")
        void getAllAutoStats_ReturnsDefensiveCopy() {
            playerStats.setAutoStat(Stat.STRENGTH, 10);

            Map<Stat, Integer> stats = playerStats.getAllAutoStats();
            stats.put(Stat.STRENGTH, 999);

            assertThat(playerStats.getAutoStat(Stat.STRENGTH)).isEqualTo(10);
        }

        @Test
        @DisplayName("getAllTotalStats: 返されたマップの変更は元に影響しない")
        void getAllTotalStats_ReturnsDefensiveCopy() {
            playerStats.setBaseStat(Stat.STRENGTH, 20);

            Map<Stat, Integer> stats = playerStats.getAllTotalStats();
            stats.put(Stat.STRENGTH, 999);

            assertThat(playerStats.getTotalStat(Stat.STRENGTH)).isEqualTo(20);
        }
    }

    // ==================== 全ステータス列挙 ====================

    @Nested
    @DisplayName("All Stat Values")
    class AllStatValuesTests {

        @Test
        @DisplayName("全ステータス種別が正しく扱われる")
        void allStats_AreHandledCorrectly() {
            for (Stat stat : Stat.values()) {
                playerStats.setBaseStat(stat, 10);
                playerStats.setAutoStat(stat, 5);

                assertThat(playerStats.getTotalStat(stat)).isEqualTo(15);
            }
        }
    }
}
