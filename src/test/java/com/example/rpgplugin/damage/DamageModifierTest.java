package com.example.rpgplugin.damage;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * DamageModifierのユニットテスト
 *
 * <p>ダメージ計算ロジックのテストを行います。</p>
 *
 * 設計原則:
 * - SOLID-S: ダメージ計算テストに特化
 * - KISS: シンプルなテストケース
 * - 読みやすさ: テスト名で振る舞いを明示
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("DamageModifier テスト")
class DamageModifierTest {

    // ==================== 物理ダメージ計算 ====================

    @Nested
    @DisplayName("物理ダメージ計算 (calculatePhysicalDamage)")
    class PhysicalDamageTests {

        @Test
        @DisplayName("基本ケース: STR0、倍率1.0")
        void testCalculatePhysicalDamage_BaseCase() {
            double result = DamageModifier.calculatePhysicalDamage(10.0, 0, 1.0);
            assertThat(result).isEqualTo(10.0);
        }

        @Test
        @DisplayName("STR補正あり")
        void testCalculatePhysicalDamage_WithStrength() {
            double result = DamageModifier.calculatePhysicalDamage(10.0, 50, 1.0);
            // 10 × (1 + 50/100) = 15
            assertThat(result).isEqualTo(15.0);
        }

        @Test
        @DisplayName("クラス倍率適用")
        void testCalculatePhysicalDamage_WithClassMultiplier() {
            double result = DamageModifier.calculatePhysicalDamage(10.0, 0, 2.0);
            assertThat(result).isEqualTo(20.0);
        }

        @Test
        @DisplayName("全補正適用")
        void testCalculatePhysicalDamage_AllModifiers() {
            double result = DamageModifier.calculatePhysicalDamage(10.0, 50, 2.0);
            // 10 × (1 + 50/100) × 2.0 = 30
            assertThat(result).isEqualTo(30.0);
        }

        @Test
        @DisplayName("負のSTR値でも計算可能")
        void testCalculatePhysicalDamage_NegativeStrength() {
            double result = DamageModifier.calculatePhysicalDamage(100.0, -50, 1.0);
            // 100 × (1 + (-50)/100) = 50
            assertThat(result).isEqualTo(50.0);
        }

        @Test
        @DisplayName("0ダメージは0のまま")
        void testCalculatePhysicalDamage_ZeroBaseDamage() {
            double result = DamageModifier.calculatePhysicalDamage(0.0, 100, 1.0);
            assertThat(result).isZero();
        }
    }

    // ==================== 魔法ダメージ計算 ====================

    @Nested
    @DisplayName("魔法ダメージ計算 (calculateMagicDamage)")
    class MagicDamageTests {

        @Test
        @DisplayName("基本ケース: INT0、倍率1.0")
        void testCalculateMagicDamage_BaseCase() {
            double result = DamageModifier.calculateMagicDamage(10.0, 0, 1.0);
            assertThat(result).isEqualTo(10.0);
        }

        @Test
        @DisplayName("INT補正あり")
        void testCalculateMagicDamage_WithIntelligence() {
            double result = DamageModifier.calculateMagicDamage(10.0, 50, 1.0);
            // 10 + (50 × 0.15) = 17.5
            assertThat(result).isEqualTo(17.5);
        }

        @Test
        @DisplayName("クラス倍率がINT補正に適用")
        void testCalculateMagicDamage_WithMultiplier() {
            double result = DamageModifier.calculateMagicDamage(100.0, 100, 2.0);
            // 100 + 100 × 0.15 × 2.0 = 130
            assertThat(result).isEqualTo(130.0);
        }

        @Test
        @DisplayName("負のINT値でダメージ減少")
        void testCalculateMagicDamage_NegativeInt() {
            double result = DamageModifier.calculateMagicDamage(100.0, -100, 1.0);
            // 100 + (-100) × 0.15 = 85
            assertThat(result).isEqualTo(85.0);
        }
    }

    // ==================== 防御カット計算 ====================

    @Nested
    @DisplayName("物理防御カット (calculateDefenseCut)")
    class DefenseCutTests {

        @Test
        @DisplayName("VIT0でダメージは減衰しない")
        void testCalculateDefenseCut_ZeroVit() {
            double result = DamageModifier.calculateDefenseCut(100.0, 0);
            assertThat(result).isEqualTo(100.0);
        }

        @Test
        @DisplayName("VIT50で約33%カット")
        void testCalculateDefenseCut_Vit50() {
            double result = DamageModifier.calculateDefenseCut(100.0, 50);
            // 防御率 = 50/(50+100) = 1/3, 結果 = 100 × 2/3 = 66.66...
            assertThat(result).isEqualTo(200.0 / 3, within(0.001));
        }

        @Test
        @DisplayName("高VITで大幅カット")
        void testCalculateDefenseCut_HighVit() {
            double result = DamageModifier.calculateDefenseCut(100.0, 200);
            // 防御率 = 200/300 = 2/3, 結果 = 100 × 1/3 = 33.33...
            assertThat(result).isEqualTo(100.0 / 3, within(0.001));
        }

        @Test
        @DisplayName("0ダメージは0のまま")
        void testCalculateDefenseCut_ZeroDamage() {
            double result = DamageModifier.calculateDefenseCut(0.0, 100);
            assertThat(result).isZero();
        }
    }

    // ==================== 魔法防御カット計算 ====================

    @Nested
    @DisplayName("魔法防御カット (calculateMagicDefenseCut)")
    class MagicDefenseCutTests {

        @Test
        @DisplayName("SPI0でダメージは減衰しない")
        void testCalculateMagicDefenseCut_ZeroSpi() {
            double result = DamageModifier.calculateMagicDefenseCut(100.0, 0);
            assertThat(result).isEqualTo(100.0);
        }

        @Test
        @DisplayName("SPI50で約33%カット")
        void testCalculateMagicDefenseCut_Spi50() {
            double result = DamageModifier.calculateMagicDefenseCut(100.0, 50);
            assertThat(result).isEqualTo(200.0 / 3, within(0.001));
        }

        @Test
        @DisplayName("高SPIで大幅カット")
        void testCalculateMagicDefenseCut_HighSpi() {
            double result = DamageModifier.calculateMagicDefenseCut(100.0, 200);
            assertThat(result).isEqualTo(100.0 / 3, within(0.001));
        }
    }

    // ==================== ダメージ丸め ====================

    @Nested
    @DisplayName("ダメージ丸め処理 (roundDamage)")
    class RoundDamageTests {

        @Test
        @DisplayName("正常値: 小数点以下切り捨て")
        void testRoundDamage_NormalValue() {
            int result = DamageModifier.roundDamage(10.6);
            assertThat(result).isEqualTo(10);
        }

        @Test
        @DisplayName("整数値はそのまま")
        void testRoundDamage_IntegerValue() {
            int result = DamageModifier.roundDamage(100.0);
            assertThat(result).isEqualTo(100);
        }

        @Test
        @DisplayName("0.5以下も切り捨て")
        void testRoundDamage_HalfOrLess() {
            int result = DamageModifier.roundDamage(10.5);
            assertThat(result).isEqualTo(10);
        }

        @Test
        @DisplayName("負の値は最低1保証")
        void testRoundDamage_NegativeValue() {
            int result = DamageModifier.roundDamage(-5.5);
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("1未満の正の値は1に（最低ダメージ保証）")
        void testRoundDamage_LessThanOne() {
            int result = DamageModifier.roundDamage(0.5);
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("0は1に（最低ダメージ保証）")
        void testRoundDamage_Zero() {
            int result = DamageModifier.roundDamage(0.0);
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("0.1も1に")
        void testRoundDamage_TinyPositive() {
            int result = DamageModifier.roundDamage(0.1);
            assertThat(result).isEqualTo(1);
        }
    }

    // ==================== 物理防御力計算 ====================

    @Nested
    @DisplayName("物理防御力計算 (calculatePhysicalDefense)")
    class PhysicalDefenseTests {

        @Test
        @DisplayName("VITから物理防御力を計算（半分）")
        void testCalculatePhysicalDefense() {
            double result = DamageModifier.calculatePhysicalDefense(100);
            assertThat(result).isEqualTo(50.0);
        }

        @Test
        @DisplayName("VIT0で防御力0")
        void testCalculatePhysicalDefense_ZeroVit() {
            double result = DamageModifier.calculatePhysicalDefense(0);
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("奇数のVITでも正しく計算")
        void testCalculatePhysicalDefense_OddVit() {
            double result = DamageModifier.calculatePhysicalDefense(55);
            assertThat(result).isEqualTo(27.5);
        }
    }

    // ==================== 魔法防御力計算 ====================

    @Nested
    @DisplayName("魔法防御力計算 (calculateMagicDefense)")
    class MagicDefenseTests {

        @Test
        @DisplayName("SPIから魔法防御力を計算（半分）")
        void testCalculateMagicDefense() {
            double result = DamageModifier.calculateMagicDefense(100);
            assertThat(result).isEqualTo(50.0);
        }

        @Test
        @DisplayName("SPI0で防御力0")
        void testCalculateMagicDefense_ZeroSpi() {
            double result = DamageModifier.calculateMagicDefense(0);
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("奇数のSPIでも正しく計算")
        void testCalculateMagicDefense_OddSpi() {
            double result = DamageModifier.calculateMagicDefense(77);
            assertThat(result).isEqualTo(38.5);
        }
    }

    // ==================== ダメージカット率計算 ====================

    @Nested
    @DisplayName("ダメージカット率計算 (calculateDamageCutRate)")
    class DamageCutRateTests {

        @Test
        @DisplayName("防御力0でカット率0")
        void testCalculateDamageCutRate_ZeroDefense() {
            double result = DamageModifier.calculateDamageCutRate(0.0);
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("防御力100でカット率50%")
        void testCalculateDamageCutRate_Defense100() {
            double result = DamageModifier.calculateDamageCutRate(100.0);
            // 100 / (100 + 100) = 0.5
            assertThat(result).isEqualTo(0.5);
        }

        @Test
        @DisplayName("高防御力で高カット率")
        void testCalculateDamageCutRate_HighDefense() {
            double result = DamageModifier.calculateDamageCutRate(300.0);
            // 300 / (300 + 100) = 0.75
            assertThat(result).isEqualTo(0.75);
        }

        @Test
        @DisplayName("カット率は100%未満に収まる")
        void testCalculateDamageCutRate_NeverReachesOne() {
            double result = DamageModifier.calculateDamageCutRate(10000.0);
            // 10000 / 10100 < 1.0
            assertThat(result).isStrictlyBetween(0.99, 1.0);
        }

        @Test
        @DisplayName("負の防御力で負のカット率")
        void testCalculateDamageCutRate_NegativeDefense() {
            double result = DamageModifier.calculateDamageCutRate(-50.0);
            // -50 / 50 = -1.0
            assertThat(result).isEqualTo(-1.0);
        }
    }

    // ==================== 組合せテスト ====================

    @Nested
    @DisplayName("複合計算フロー")
    class CombinedFlowTests {

        @Test
        @DisplayName("物理ダメージの完整計算フロー")
        void fullPhysicalDamageFlow() {
            // 攻撃側: STR100
            double baseDamage = DamageModifier.calculatePhysicalDamage(100.0, 100, 1.0);
            assertThat(baseDamage).isEqualTo(200.0);

            // 防御側: VIT50
            double afterDefense = DamageModifier.calculateDefenseCut(baseDamage, 50);
            assertThat(afterDefense).isCloseTo(133.33, within(0.01));

            // 整数丸め
            int finalDamage = DamageModifier.roundDamage(afterDefense);
            assertThat(finalDamage).isEqualTo(133);
        }

        @Test
        @DisplayName("魔法ダメージの完整計算フロー")
        void fullMagicDamageFlow() {
            // 攻撃側: INT100
            double baseDamage = DamageModifier.calculateMagicDamage(100.0, 100, 1.0);
            assertThat(baseDamage).isEqualTo(115.0);

            // 防御側: SPI50
            double afterDefense = DamageModifier.calculateMagicDefenseCut(baseDamage, 50);
            assertThat(afterDefense).isCloseTo(76.66, within(0.01));

            // 整数丸め
            int finalDamage = DamageModifier.roundDamage(afterDefense);
            assertThat(finalDamage).isEqualTo(76);
        }
    }
}
