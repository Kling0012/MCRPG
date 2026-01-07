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
        // Given: 基本ダメージ10、STR0、クラス倍率1.0、非クリティカル
        double baseDamage = 10.0;
        int strength = 0;
        double classMultiplier = 1.0;
        boolean isCritical = false;
        double critMultiplier = 1.5;

        // When: 物理ダメージ計算
        double result = DamageModifier.calculatePhysicalDamage(
                baseDamage, strength, classMultiplier, isCritical, critMultiplier
        );

        // Then: 基本ダメージがそのまま返る
        assertThat(result).isEqualTo(10.0);
    }

    @Test
    @DisplayName("物理ダメージ計算: STR補正あり")
    void testCalculatePhysicalDamage_WithStrength() {
        // Given: 基本ダメージ10、STR50、クラス倍率1.0、非クリティカル
        double baseDamage = 10.0;
        int strength = 50;
        double classMultiplier = 1.0;
        boolean isCritical = false;
        double critMultiplier = 1.5;

        // When: 物理ダメージ計算
        double result = DamageModifier.calculatePhysicalDamage(
                baseDamage, strength, classMultiplier, isCritical, critMultiplier
        );

        // Then: STR補正が適用される（基本ダメージ + STR * 0.1）
        // 10 + (50 * 0.1) = 15
        assertThat(result).isEqualTo(15.0);
    }

    @Test
    @DisplayName("物理ダメージ計算: クリティカルヒット")
    void testCalculatePhysicalDamage_CriticalHit() {
        // Given: 基本ダメージ10、STR0、クラス倍率1.0、クリティカル
        double baseDamage = 10.0;
        int strength = 0;
        double classMultiplier = 1.0;
        boolean isCritical = true;
        double critMultiplier = 1.5;

        // When: 物理ダメージ計算
        double result = DamageModifier.calculatePhysicalDamage(
                baseDamage, strength, classMultiplier, isCritical, critMultiplier
        );

        // Then: クリティカル倍率が適用される
        // 10 * 1.5 = 15
        assertThat(result).isEqualTo(15.0);
    }

    @Test
    @DisplayName("物理ダメージ計算: 全補正適用")
    void testCalculatePhysicalDamage_AllModifiers() {
        // Given: 基本ダメージ10、STR50、クラス倍率2.0、クリティカル
        double baseDamage = 10.0;
        int strength = 50;
        double classMultiplier = 2.0;
        boolean isCritical = true;
        double critMultiplier = 1.5;

        // When: 物理ダメージ計算
        double result = DamageModifier.calculatePhysicalDamage(
                baseDamage, strength, classMultiplier, isCritical, critMultiplier
        );

        // Then: 全補正が適用される
        // (10 + (50 * 0.1)) * 2.0 * 1.5 = 45
        assertThat(result).isEqualTo(45.0);
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

        // Then: 四捨五入されて11になる
        assertThat(result).isEqualTo(11);
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

    @Test
    @DisplayName("クリティカル倍率計算: DEX0")
    void testCalculateCriticalMultiplier_ZeroDexterity() {
        // Given: DEX0
        int dexterity = 0;

        // When: クリティカル倍率計算
        double result = DamageModifier.calculateCriticalMultiplier(dexterity);

        // Then: 基本倍率1.5
        assertThat(result).isEqualTo(1.5);
    }

    @Test
    @DisplayName("クリティカル倍率計算: DEX50")
    void testCalculateCriticalMultiplier_FiftyDexterity() {
        // Given: DEX50
        int dexterity = 50;

        // When: クリティカル倍率計算
        double result = DamageModifier.calculateCriticalMultiplier(dexterity);

        // Then: 基本倍率 + (DEX * 0.002) = 1.5 + 0.1 = 1.6
        assertThat(result).isEqualTo(1.6);
    }
}
