package com.example.rpgplugin.damage;

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

    @Test
    @DisplayName("物理ダメージ計算: 基本ケース")
    void testCalculatePhysicalDamage_BaseCase() {
        // Given: 基本ダメージ10、STR0、クラス倍率1.0
        double baseDamage = 10.0;
        int strength = 0;
        double classMultiplier = 1.0;

        // When: 物理ダメージ計算
        double result = DamageModifier.calculatePhysicalDamage(
                baseDamage, strength, classMultiplier
        );

        // Then: 基本ダメージがそのまま返る
        assertThat(result).isEqualTo(10.0);
    }

    @Test
    @DisplayName("物理ダメージ計算: STR補正あり")
    void testCalculatePhysicalDamage_WithStrength() {
        // Given: 基本ダメージ10、STR50、クラス倍率1.0
        double baseDamage = 10.0;
        int strength = 50;
        double classMultiplier = 1.0;

        // When: 物理ダメージ計算
        double result = DamageModifier.calculatePhysicalDamage(
                baseDamage, strength, classMultiplier
        );

        // Then: STR補正が適用される（基本ダメージ × (1 + STR/100)）
        // 10 × (1 + 50/100) = 10 × 1.5 = 15
        assertThat(result).isEqualTo(15.0);
    }

    @Test
    @DisplayName("物理ダメージ計算: クラス倍率適用")
    void testCalculatePhysicalDamage_WithClassMultiplier() {
        // Given: 基本ダメージ10、STR0、クラス倍率2.0
        double baseDamage = 10.0;
        int strength = 0;
        double classMultiplier = 2.0;

        // When: 物理ダメージ計算
        double result = DamageModifier.calculatePhysicalDamage(
                baseDamage, strength, classMultiplier
        );

        // Then: クラス倍率が適用される
        // 10 × 2.0 = 20
        assertThat(result).isEqualTo(20.0);
    }

    @Test
    @DisplayName("物理ダメージ計算: 全補正適用")
    void testCalculatePhysicalDamage_AllModifiers() {
        // Given: 基本ダメージ10、STR50、クラス倍率2.0
        double baseDamage = 10.0;
        int strength = 50;
        double classMultiplier = 2.0;

        // When: 物理ダメージ計算
        double result = DamageModifier.calculatePhysicalDamage(
                baseDamage, strength, classMultiplier
        );

        // Then: 全補正が適用される
        // 10 × (1 + 50/100) × 2.0 = 10 × 1.5 × 2.0 = 30
        assertThat(result).isEqualTo(30.0);
    }

    @Test
    @DisplayName("魔法ダメージ計算: 基本ケース")
    void testCalculateMagicDamage_BaseCase() {
        // Given: 基本ダメージ10、INT0、クラス倍率1.0
        double baseDamage = 10.0;
        int intelligence = 0;
        double classMultiplier = 1.0;

        // When: 魔法ダメージ計算
        double result = DamageModifier.calculateMagicDamage(
                baseDamage, intelligence, classMultiplier
        );

        // Then: 基本ダメージがそのまま返る
        assertThat(result).isEqualTo(10.0);
    }

    @Test
    @DisplayName("魔法ダメージ計算: INT補正あり")
    void testCalculateMagicDamage_WithIntelligence() {
        // Given: 基本ダメージ10、INT50、クラス倍率1.0
        double baseDamage = 10.0;
        int intelligence = 50;
        double classMultiplier = 1.0;

        // When: 魔法ダメージ計算
        double result = DamageModifier.calculateMagicDamage(
                baseDamage, intelligence, classMultiplier
        );

        // Then: INT補正が適用される（基本ダメージ + INT * 0.15）
        // 10 + (50 * 0.15) = 17.5
        assertThat(result).isEqualTo(17.5);
    }

    @Test
    @DisplayName("ダメージ丸め処理: 正常値")
    void testRoundDamage_NormalValue() {
        // Given: ダメージ値10.6
        double damage = 10.6;

        // When: 整数に丸め
        int result = DamageModifier.roundDamage(damage);

        // Then: 小数点以下切り捨てで10になる
        assertThat(result).isEqualTo(10);
    }

    @Test
    @DisplayName("ダメージ丸め処理: 0.5以下")
    void testRoundDamage_HalfOrLess() {
        // Given: ダメージ値10.5
        double damage = 10.5;

        // When: 整数に丸め
        int result = DamageModifier.roundDamage(damage);

        // Then: 四捨五入されて10になる
        assertThat(result).isEqualTo(10);
    }

    @Test
    @DisplayName("ダメージ丸め処理: 負の値")
    void testRoundDamage_NegativeValue() {
        // Given: 負のダメージ値-5.5
        double damage = -5.5;

        // When: 整数に丸め
        int result = DamageModifier.roundDamage(damage);

        // Then: 0に丸められる（負のダメージは許容しない）
        assertThat(result).isEqualTo(0);
    }
}
