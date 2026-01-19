package com.example.rpgplugin.skill.target;

/**
 * スキルターゲット設定に関する計算を行うクラス
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ターゲット設定の計算に専念</li>
 *   <li>KISS: シンプルな計算ロジック</li>
 *   <li>DRY: 計算ロジックを一元管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public final class TargetCalculator {

    // デフォルト値
    private static final double DEFAULT_RANGE = 10.0;
    private static final double DEFAULT_LINE_WIDTH = 2.0;
    private static final double DEFAULT_CONE_ANGLE = 60.0;
    private static final double DEFAULT_SPHERE_RADIUS = 5.0;
    private static final int DEFAULT_SEARCH_RANGE = 5;

    private TargetCalculator() {
        // ユーティリティクラスのためインスタンス化禁止
    }

    /**
     * 有効な範囲（range）を取得します
     *
     * <p>優先順位:</p>
     * <ol>
     *   <li>汎用rangeパラメータ</li>
     *   <li>ConeConfigのrange</li>
     *   <li>デフォルト値(10.0)</li>
     * </ol>
     *
     * @param target ターゲット設定
     * @return 有効な範囲
     */
    public static double getEffectiveRange(SkillTarget target) {
        if (target == null) {
            return DEFAULT_RANGE;
        }

        // 汎用rangeパラメータを優先
        Double range = target.getRawRange();
        if (range != null) {
            return range;
        }

        // ConeConfigから取得
        if (target.getConeAsOptional().isPresent()) {
            return target.getConeAsOptional().get().getRange();
        }

        return DEFAULT_RANGE;
    }

    /**
     * 有効な直線幅（lineWidth）を取得します
     *
     * @param target ターゲット設定
     * @return 有効な直線幅
     */
    public static double getEffectiveLineWidth(SkillTarget target) {
        if (target == null) {
            return DEFAULT_LINE_WIDTH;
        }

        Double lineWidth = target.getRawLineWidth();
        return lineWidth != null ? lineWidth : DEFAULT_LINE_WIDTH;
    }

    /**
     * 有効なコーン角度（coneAngle）を取得します
     *
     * <p>優先順位:</p>
     * <ol>
     *   <li>汎用coneAngleパラメータ</li>
     *   <li>ConeConfigのangle</li>
     *   <li>デフォルト値(60.0)</li>
     * </ol>
     *
     * @param target ターゲット設定
     * @return 有効なコーン角度（度数法）
     */
    public static double getEffectiveConeAngle(SkillTarget target) {
        if (target == null) {
            return DEFAULT_CONE_ANGLE;
        }

        // 汎用coneAngleパラメータを優先
        Double coneAngle = target.getRawConeAngle();
        if (coneAngle != null) {
            return coneAngle;
        }

        // ConeConfigから取得
        if (target.getConeAsOptional().isPresent()) {
            return target.getConeAsOptional().get().getAngle();
        }

        return DEFAULT_CONE_ANGLE;
    }

    /**
     * 有効な球形半径（sphereRadius）を取得します
     *
     * <p>優先順位:</p>
     * <ol>
     *   <li>汎用sphereRadiusパラメータ</li>
     *   <li>CircleConfigのradius</li>
     *   <li>デフォルト値(5.0)</li>
     * </ol>
     *
     * @param target ターゲット設定
     * @return 有効な球形半径
     */
    public static double getEffectiveSphereRadius(SkillTarget target) {
        if (target == null) {
            return DEFAULT_SPHERE_RADIUS;
        }

        // 汎用sphereRadiusパラメータを優先
        Double sphereRadius = target.getRawSphereRadius();
        if (sphereRadius != null) {
            return sphereRadius;
        }

        // CircleConfigから取得
        if (target.getCircleAsOptional().isPresent()) {
            return target.getCircleAsOptional().get().getRadius();
        }

        return DEFAULT_SPHERE_RADIUS;
    }

    /**
     * 有効な最大ターゲット数を取得します
     *
     * @param target ターゲット設定
     * @return 最大ターゲット数（制限なしの場合はInteger.MAX_VALUE）
     */
    public static int getEffectiveMaxTargets(SkillTarget target) {
        if (target == null) {
            return Integer.MAX_VALUE;
        }

        Integer maxTargets = target.getMaxTargets();
        return maxTargets != null ? maxTargets : Integer.MAX_VALUE;
    }

    /**
     * 単体ターゲットのデフォルト探索範囲を取得します
     *
     * @return 探索範囲（ブロック単位）
     */
    public static int getSingleTargetSearchRange() {
        return DEFAULT_SEARCH_RANGE;
    }

    /**
     * ターゲットがコーン範囲内に含まれるか判定するための角度（ラジアン）を計算します
     *
     * @param target ターゲット設定
     * @return 角度（ラジアン）
     */
    public static double getConeAngleRadians(SkillTarget target) {
        return Math.toRadians(getEffectiveConeAngle(target));
    }

    /**
     * ターゲット設定が範囲攻撃かどうかを判定します
     *
     * @param target ターゲット設定
     * @return 範囲攻撃の場合はtrue
     */
    public static boolean isAreaTarget(SkillTarget target) {
        if (target == null) {
            return false;
        }

        TargetType type = target.getType();
        return type == TargetType.AREA_SELF
                || type == TargetType.AREA_OTHERS
                || type == TargetType.CONE
                || type == TargetType.LINE
                || type == TargetType.LOOKING
                || type == TargetType.SPHERE;
    }

    /**
     * ターゲット設定が単体攻撃かどうかを判定します
     *
     * @param target ターゲット設定
     * @return 単体攻撃の場合はtrue
     */
    public static boolean isSingleTarget(SkillTarget target) {
        if (target == null) {
            return true;
        }

        TargetType type = target.getType();
        return type == TargetType.SELF
                || type == TargetType.SELF_PLUS_ONE
                || type == TargetType.NEAREST_HOSTILE
                || type == TargetType.NEAREST_PLAYER
                || type == TargetType.NEAREST_ENTITY
                || type == TargetType.EXTERNAL;
    }
}
