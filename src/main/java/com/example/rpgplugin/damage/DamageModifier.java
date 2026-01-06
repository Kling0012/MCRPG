package com.example.rpgplugin.damage;

import com.example.rpgplugin.stats.Stat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * ダメージ修正を適用するユーティリティクラス
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>DRY: ダメージ計算ロジックを一元管理</li>
 *   <li>KISS: シンプルで明快な計算式</li>
 *   <li>SOLID-S: 単一責務（ダメージ修正のみ）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class DamageModifier {

    /**
     * 物理ダメージを計算
     *
     * <p>計算式: 基本ダメージ × (1 + STR/100) × クリティカル倍率</p>
     *
     * @param baseDamage     基本ダメージ
     * @param strength       STR値
     * @param classMultiplier クラス倍率（オプション）
     * @param isCritical     クリティカルヒットかどうか
     * @param critMultiplier クリティカル倍率
     * @return 計算後の物理ダメージ
     */
    public static double calculatePhysicalDamage(
            double baseDamage,
            int strength,
            double classMultiplier,
            boolean isCritical,
            double critMultiplier) {

        // STRによるダメージボーナス
        double strMultiplier = 1.0 + (strength / 100.0);

        // 基本計算
        double damage = baseDamage * strMultiplier * classMultiplier;

        // クリティカル補正
        if (isCritical) {
            damage *= critMultiplier;
        }

        return damage;
    }

    /**
     * 魔法ダメージを計算
     *
     * <p>計算式: 基本ダメージ × (1 + INT/100) × クラス倍率</p>
     *
     * @param baseDamage      基本ダメージ
     * @param intelligence    INT値
     * @param classMultiplier クラス倍率（オプション）
     * @return 計算後の魔法ダメージ
     */
    public static double calculateMagicDamage(
            double baseDamage,
            int intelligence,
            double classMultiplier) {

        // INTによるダメージボーナス
        double intMultiplier = 1.0 + (intelligence / 100.0);

        // 基本計算
        return baseDamage * intMultiplier * classMultiplier;
    }

    /**
     * 防御カットを計算
     *
     * <p>計算式: ダメージ × (1 - VIT/(VIT+100))</p>
     *
     * @param damage 入力ダメージ
     * @param vitality VIT値
     * @return 防御カット後のダメージ
     */
    public static double calculateDefenseCut(double damage, int vitality) {
        // VITによる防御率
        double defenseRate = vitality / (vitality + 100.0);

        // 防御カット量
        double cutAmount = damage * defenseRate;

        // カット後ダメージ
        return damage - cutAmount;
    }

    /**
     * 魔法防御カットを計算
     *
     * <p>計算式: ダメージ × (1 - SPI/(SPI+100))</p>
     *
     * @param damage 入力ダメージ
     * @param spirit SPI値
     * @return 防御カット後のダメージ
     */
    public static double calculateMagicDefenseCut(double damage, int spirit) {
        // SPIによる魔法防御率
        double defenseRate = spirit / (spirit + 100.0);

        // 防御カット量
        double cutAmount = damage * defenseRate;

        // カット後ダメージ
        return damage - cutAmount;
    }

    /**
     * クリティカル率を計算
     *
     * <p>計算式: 基礎率(5%) + DEX/50</p>
     *
     * @param dexterity DEX値
     * @return クリティカル率（0.0〜1.0）
     */
    public static double calculateCriticalRate(int dexterity) {
        double baseRate = 0.05; // 基礎率5%
        double dexBonus = dexterity / 50.0;

        return Math.min(1.0, baseRate + dexBonus); // 最大100%
    }

    /**
     * クリティカル倍率を計算
     *
     * <p>計算式: 1.5 + DEX/200</p>
     *
     * @param dexterity DEX値
     * @return クリティカル倍率
     */
    public static double calculateCriticalMultiplier(int dexterity) {
        double baseMultiplier = 1.5;
        double dexBonus = dexterity / 200.0;

        return baseMultiplier + dexBonus;
    }

    /**
     * クリティカルヒットかどうかを判定
     *
     * @param dexterity DEX値
     * @return クリティカルヒットならtrue
     */
    public static boolean isCriticalHit(int dexterity) {
        double criticalRate = calculateCriticalRate(dexterity);
        return Math.random() < criticalRate;
    }

    /**
     * プレイヤーのステータスに基づいてクリティカル判定
     *
     * @param player プレイヤー
     * @param stats  プレイヤーのステータスマップ
     * @return クリティカルヒットならtrue
     */
    public static boolean isCriticalHit(Player player, Map<Stat, Integer> stats) {
        int dexterity = stats.getOrDefault(Stat.DEXTERITY, 0);
        return isCriticalHit(dexterity);
    }

    /**
     * ダメージを整数に丸める
     *
     * @param damage ダメージ値
     * @return 整数に丸められたダメージ（最低1）
     */
    public static int roundDamage(double damage) {
        int rounded = (int) Math.round(damage);
        return Math.max(1, rounded); // 最低ダメージは1
    }

    /**
     * 物理防御力を計算
     *
     * <p>計算式: VIT × 0.5</p>
     *
     * @param vitality VIT値
     * @return 物理防御力
     */
    public static double calculatePhysicalDefense(int vitality) {
        return vitality * 0.5;
    }

    /**
     * 魔法防御力を計算
     *
     * <p>計算式: SPI × 0.5</p>
     *
     * @param spirit SPI値
     * @return 魔法防御力
     */
    public static double calculateMagicDefense(int spirit) {
        return spirit * 0.5;
    }

    /**
     * 防御力からダメージカット率を計算
     *
     * <p>計算式: 防御力 / (防御力 + 100)</p>
     *
     * @param defense 防御力
     * @return ダメージカット率（0.0〜1.0）
     */
    public static double calculateDamageCutRate(double defense) {
        return defense / (defense + 100.0);
    }
}
