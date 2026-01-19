package com.example.rpgplugin.skill;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * レベル依存パラメータのテストクラス
 *
 * <p>Phase11-9: レベル依存パラメータの包括的なテスト覆盖</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("レベル依存パラメータ テスト")
class LevelDependentParameterTest {

    // ===== 基本的な値取得テスト =====

    @Test
    @DisplayName("基本値のみのテスト")
    void testBaseOnly() {
        LevelDependentParameter ldp = new LevelDependentParameter(10.0, 0.0);

        assertEquals(10.0, ldp.getValue(1), 0.001);
        assertEquals(10.0, ldp.getValue(5), 0.001);
        assertEquals(10.0, ldp.getValue(10), 0.001);
    }

    @Test
    @DisplayName("レベル毎増加テスト")
    void testPerLevelIncrease() {
        LevelDependentParameter ldp = new LevelDependentParameter(10.0, 2.0);

        assertEquals(10.0, ldp.getValue(1), 0.001);
        assertEquals(12.0, ldp.getValue(2), 0.001);
        assertEquals(14.0, ldp.getValue(3), 0.001);
        assertEquals(20.0, ldp.getValue(6), 0.001);
        assertEquals(28.0, ldp.getValue(10), 0.001);
    }

    @Test
    @DisplayName("レベル毎減少テスト（CD減少パターン）")
    void testPerLevelDecrease() {
        LevelDependentParameter ldp = new LevelDependentParameter(10.0, -1.0);

        assertEquals(10.0, ldp.getValue(1), 0.001);
        assertEquals(9.0, ldp.getValue(2), 0.001);
        assertEquals(8.0, ldp.getValue(3), 0.001);
        assertEquals(5.0, ldp.getValue(6), 0.001);
        assertEquals(1.0, ldp.getValue(10), 0.001);
    }

    // ===== 最小値制限テスト =====

    @Test
    @DisplayName("最小値制限テスト（減少パターン）")
    void testMinValueDecrease() {
        LevelDependentParameter ldp = new LevelDependentParameter(10.0, -1.0, 5.0);

        assertEquals(10.0, ldp.getValue(1), 0.001);
        assertEquals(9.0, ldp.getValue(2), 0.001);
        assertEquals(8.0, ldp.getValue(3), 0.001);
        assertEquals(7.0, ldp.getValue(4), 0.001);
        assertEquals(6.0, ldp.getValue(5), 0.001);
        assertEquals(5.0, ldp.getValue(6), 0.001); // 最小値到達
        assertEquals(5.0, ldp.getValue(10), 0.001); // その後は最小値固定
    }

    @Test
    @DisplayName("最小値制限テスト（増加パターンでの下限）")
    void testMinValueIncrease() {
        LevelDependentParameter ldp = new LevelDependentParameter(5.0, 1.0, 8.0);

        // 基本値が最小値を下回る場合
        assertEquals(8.0, ldp.getValue(1), 0.001); // 最小値適用
        assertEquals(8.0, ldp.getValue(2), 0.001);
        assertEquals(8.0, ldp.getValue(3), 0.001);
        assertEquals(8.0, ldp.getValue(4), 0.001);
        assertEquals(9.0, ldp.getValue(5), 0.001); // 計算値が最小値を超える
    }

    // ===== 最大値制限テスト =====

    @Test
    @DisplayName("最大値制限テスト（コスト増加パターン）")
    void testMaxValueIncrease() {
        LevelDependentParameter ldp = new LevelDependentParameter(10.0, 5.0, null, 30.0);

        assertEquals(10.0, ldp.getValue(1), 0.001);
        assertEquals(15.0, ldp.getValue(2), 0.001);
        assertEquals(20.0, ldp.getValue(3), 0.001);
        assertEquals(25.0, ldp.getValue(4), 0.001);
        assertEquals(30.0, ldp.getValue(5), 0.001); // 最大値到達
        assertEquals(30.0, ldp.getValue(10), 0.001); // その後は最大値固定
    }

    // ===== 最小値・最大値両方制限テスト =====

    @Test
    @DisplayName("最小値・最大値両方制限テスト")
    void testMinAndMaxValue() {
        LevelDependentParameter ldp = new LevelDependentParameter(15.0, 1.0, 10.0, 20.0);

        assertEquals(15.0, ldp.getValue(1), 0.001);
        assertEquals(16.0, ldp.getValue(2), 0.001);
        assertEquals(17.0, ldp.getValue(3), 0.001);
        assertEquals(18.0, ldp.getValue(4), 0.001);
        assertEquals(19.0, ldp.getValue(5), 0.001);
        assertEquals(20.0, ldp.getValue(6), 0.001); // 最大値到達
        assertEquals(20.0, ldp.getValue(10), 0.001);
    }

    @Test
    @DisplayName("範囲外の基本値がmin/maxでクランプされるテスト")
    void testBaseValueOutsideRange() {
        // 基本値が最大値を超えている場合
        LevelDependentParameter ldp1 = new LevelDependentParameter(100.0, 5.0, 50.0, 70.0);
        assertEquals(70.0, ldp1.getValue(1), 0.001);

        // 基本値が最小値を下回っている場合
        LevelDependentParameter ldp2 = new LevelDependentParameter(5.0, -1.0, 10.0, 20.0);
        assertEquals(10.0, ldp2.getValue(1), 0.001);
    }

    // ===== 整数値取得テスト =====

    @Test
    @DisplayName("整数値取得テスト（切り捨て）")
    void testGetIntValue() {
        LevelDependentParameter ldp = new LevelDependentParameter(10.5, 1.3);

        // 小数点以下は切り捨て
        assertEquals(10, ldp.getIntValue(1));
        assertEquals(11, ldp.getIntValue(2)); // 10.5 + 1.3 = 11.8 -> 11
        assertEquals(13, ldp.getIntValue(3)); // 10.5 + 2.6 = 13.1 -> 13
    }

    // ===== ゲッターメソッドテスト =====

    @Test
    @DisplayName("ゲッターメソッドテスト")
    void testGetters() {
        LevelDependentParameter ldp = new LevelDependentParameter(
                10.0, 2.0, 5.0, 30.0
        );

        assertEquals(10.0, ldp.getBase(), 0.001);
        assertEquals(2.0, ldp.getPerLevel(), 0.001);
        assertEquals(5.0, ldp.getMinValue(), 0.001);
        assertEquals(30.0, ldp.getMaxValue(), 0.001);
    }

    @Test
    @DisplayName("null制限値のテスト")
    void testNullLimitValues() {
        LevelDependentParameter ldp = new LevelDependentParameter(
                10.0, 2.0, null, null
        );

        assertNull(ldp.getMinValue());
        assertNull(ldp.getMaxValue());
    }

    // ===== 例外テスト =====

    @Test
    @DisplayName("無効なレベル（0以下）で例外が発生すること")
    void testInvalidLevelThrowsException() {
        LevelDependentParameter ldp = new LevelDependentParameter(10.0, 2.0);

        assertThrows(IllegalArgumentException.class, () -> ldp.getValue(0));
        assertThrows(IllegalArgumentException.class, () -> ldp.getValue(-1));
        assertThrows(IllegalArgumentException.class, () -> ldp.getValue(-10));
    }

    @Test
    @DisplayName("有効な最小レベル（1）で例外が発生しないこと")
    void testValidMinimumLevel() {
        LevelDependentParameter ldp = new LevelDependentParameter(10.0, 2.0);

        assertDoesNotThrow(() -> ldp.getValue(1));
        assertEquals(10.0, ldp.getValue(1), 0.001);
    }

    // ===== toStringテスト =====

    @Test
    @DisplayName("toStringテスト")
    void testToString() {
        LevelDependentParameter ldp = new LevelDependentParameter(
                10.0, 2.0, 5.0, 30.0
        );

        String str = ldp.toString();
        assertNotNull(str);
        assertTrue(str.contains("base=10.0"));
        assertTrue(str.contains("perLevel=2.0"));
        assertTrue(str.contains("minValue=5.0"));
        assertTrue(str.contains("maxValue=30.0"));
    }

    // ===== 実用的なシナリオテスト =====

    @Test
    @DisplayName("クールダウン減少シナリオテスト")
    void testCooldownDecreaseScenario() {
        // 初期CD: 10秒、レベル毎-1秒、最小3秒
        LevelDependentParameter cooldown = new LevelDependentParameter(10.0, -1.0, 3.0);

        assertEquals(10.0, cooldown.getValue(1), "Lv1: 10秒");
        assertEquals(9.0, cooldown.getValue(2), "Lv2: 9秒");
        assertEquals(8.0, cooldown.getValue(3), "Lv3: 8秒");
        assertEquals(7.0, cooldown.getValue(4), "Lv4: 7秒");
        assertEquals(6.0, cooldown.getValue(5), "Lv5: 6秒");
        assertEquals(5.0, cooldown.getValue(6), "Lv6: 5秒");
        assertEquals(4.0, cooldown.getValue(7), "Lv7: 4秒");
        assertEquals(3.0, cooldown.getValue(8), "Lv8: 3秒（最小到達）");
        assertEquals(3.0, cooldown.getValue(9), "Lv9: 3秒（最小維持）");
        assertEquals(3.0, cooldown.getValue(10), "Lv10: 3秒（最小維持）");
    }

    @Test
    @DisplayName("マナコスト増加シナリオテスト")
    void testManaCostIncreaseScenario() {
        // 初期コスト: 10、レベル毎+2、最大30
        LevelDependentParameter cost = new LevelDependentParameter(10.0, 2.0, null, 30.0);

        assertEquals(10, cost.getIntValue(1), "Lv1: 10 MP");
        assertEquals(12, cost.getIntValue(2), "Lv2: 12 MP");
        assertEquals(14, cost.getIntValue(3), "Lv3: 14 MP");
        assertEquals(16, cost.getIntValue(4), "Lv4: 16 MP");
        assertEquals(18, cost.getIntValue(5), "Lv5: 18 MP");
        assertEquals(20, cost.getIntValue(6), "Lv6: 20 MP");
        assertEquals(22, cost.getIntValue(7), "Lv7: 22 MP");
        assertEquals(24, cost.getIntValue(8), "Lv8: 24 MP");
        assertEquals(26, cost.getIntValue(9), "Lv9: 26 MP");
        assertEquals(28, cost.getIntValue(10), "Lv10: 28 MP");
        assertEquals(30, cost.getIntValue(11), "Lv11: 30 MP（最大到達）");
        assertEquals(30, cost.getIntValue(15), "Lv15: 30 MP（最大維持）");
    }

    // ===== パラメータ化テスト =====

    @ParameterizedTest
    @CsvSource({
            "1, 10.0",
            "2, 12.0",
            "5, 18.0",
            "10, 28.0"
    })
    @DisplayName("パラメータ化テスト：増加パターン")
    void testIncreasePattern(int level, double expected) {
        LevelDependentParameter ldp = new LevelDependentParameter(10.0, 2.0);
        assertEquals(expected, ldp.getValue(level), 0.001);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 10.0",
            "2, 8.0",
            "5, 2.0",
            "10, -8.0"
    })
    @DisplayName("パラメータ化テスト：減少パターン（制限なし）")
    void testDecreasePatternNoLimit(int level, double expected) {
        LevelDependentParameter ldp = new LevelDependentParameter(10.0, -2.0);
        assertEquals(expected, ldp.getValue(level), 0.001);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 50, 100})
    @DisplayName("パラメータ化テスト：高いレベル値での動作")
    void testHighLevels(int level) {
        LevelDependentParameter ldp = new LevelDependentParameter(100.0, 10.0, null, 500.0);
        double expected = Math.min(100.0 + 10.0 * (level - 1), 500.0);
        assertEquals(expected, ldp.getValue(level), 0.001);
    }

    // ===== 境界値テスト =====

    @Test
    @DisplayName("境界値テスト：最小値境界")
    void testMinBoundary() {
        LevelDependentParameter ldp = new LevelDependentParameter(10.0, -1.0, 5.0);

        // 境界直前
        assertEquals(6.0, ldp.getValue(5), 0.001);
        // 境界到達
        assertEquals(5.0, ldp.getValue(6), 0.001);
        // 境界越え
        assertEquals(5.0, ldp.getValue(7), 0.001);
    }

    @Test
    @DisplayName("境界値テスト：最大値境界")
    void testMaxBoundary() {
        LevelDependentParameter ldp = new LevelDependentParameter(10.0, 5.0, null, 25.0);

        // 境界直前
        assertEquals(20.0, ldp.getValue(3), 0.001);
        // 境界到達
        assertEquals(25.0, ldp.getValue(4), 0.001);
        // 境界越え
        assertEquals(25.0, ldp.getValue(5), 0.001);
    }

    // ===== 特殊ケーステスト =====

    @Test
    @DisplayName("ゼロ変化量テスト")
    void testZeroPerLevel() {
        LevelDependentParameter ldp = new LevelDependentParameter(15.0, 0.0);

        for (int level = 1; level <= 20; level++) {
            assertEquals(15.0, ldp.getValue(level), 0.001,
                    "レベル" + level + "で値は変わらないはず");
        }
    }

    @Test
    @DisplayName("小数値テスト")
    void testDecimalValues() {
        LevelDependentParameter ldp = new LevelDependentParameter(
                10.5, -0.75, 5.25, 15.75
        );

        assertEquals(10.5, ldp.getValue(1), 0.001);
        assertEquals(9.75, ldp.getValue(2), 0.001);
        assertEquals(9.0, ldp.getValue(3), 0.001);
        assertEquals(8.25, ldp.getValue(4), 0.001);
        assertEquals(7.5, ldp.getValue(5), 0.001);
        assertEquals(6.75, ldp.getValue(6), 0.001);
        assertEquals(6.0, ldp.getValue(7), 0.001);
        assertEquals(5.25, ldp.getValue(8), 0.001); // 最小値到達
    }

    @Test
    @DisplayName("負の基本値テスト")
    void testNegativeBaseValue() {
        LevelDependentParameter ldp = new LevelDependentParameter(-10.0, 2.0, -15.0, 5.0);

        assertEquals(-10.0, ldp.getValue(1), 0.001);
        assertEquals(-8.0, ldp.getValue(2), 0.001);
        assertEquals(-6.0, ldp.getValue(3), 0.001);
        assertEquals(-4.0, ldp.getValue(4), 0.001);
        assertEquals(-2.0, ldp.getValue(5), 0.001);
        assertEquals(0.0, ldp.getValue(6), 0.001);
        assertEquals(2.0, ldp.getValue(7), 0.001);
        assertEquals(4.0, ldp.getValue(8), 0.001);
        assertEquals(5.0, ldp.getValue(9), 0.001); // 最大値到達
    }
}
