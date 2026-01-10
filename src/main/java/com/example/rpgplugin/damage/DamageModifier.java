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
     * <p>計算式: 基本ダメージ × (1 + STR/100) × クラス倍率</p>
     *
     * @param baseDamage     基本ダメージ
     * @param strength       STR値
     * @param classMultiplier クラス倍率（オプション）
     * @return 計算後の物理ダメージ
     */
    public static double calculatePhysicalDamage(
            double baseDamage,
            int strength,
            double classMultiplier) {

        // STRによるダメージボーナス
        double strMultiplier = 1.0 + (strength / 100.0);

        // 基本計算
        return baseDamage * strMultiplier * classMultiplier;
    }

    /**
     * 魔法ダメージを計算
     *
     * <p>計算式: 基本ダメージ + INT * 0.15 * クラス倍率</p>
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

        // INTによるダメージボーナス（加算式）
        double intBonus = intelligence * 0.15 * classMultiplier;

        // 基本計算
        return baseDamage + intBonus;
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
     * ダメージを整数に丸める
     *
     * @param damage ダメージ値
     * @return 整数に丸められたダメージ（負の値は0、最低1）
     */
    public static int roundDamage(double damage) {
        // 負の値は0に
        if (damage < 0) {
            return 0;
        }

        // 小数点以下を切り捨て（Math.floorではなくintキャスト）
        int rounded = (int) damage;

        // 最低ダメージは1
        return Math.max(1, rounded);
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
