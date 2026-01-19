package com.example.rpgplugin.skill.target;

import java.util.logging.Logger;

/**
 * スキルターゲット設定のバリデーションを行うクラス
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ターゲット設定のバリデーションに専念</li>
 *   <li>KISS: シンプルな検証ロジック</li>
 *   <li>DRY: バリデーションロジックを一元管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public final class TargetValidator {

    private TargetValidator() {
        // ユーティリティクラスのためインスタンス化禁止
    }

    /**
     * スキルターゲット設定のバリデーション結果
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
    }

    /**
     * スキルターゲット設定をバリデートします
     *
     * @param target ターゲット設定
     * @param fileName ファイル名（ログ出力用）
     * @param logger ロガー
     * @return バリデーション結果
     */
    public static ValidationResult validate(SkillTarget target, String fileName, Logger logger) {
        if (target == null) {
            return ValidationResult.success();
        }

        // タイプと形状の組み合わせチェック
        ValidationResult typeShapeResult = validateTypeShapeCombination(target, fileName);
        if (!typeShapeResult.isValid()) {
            logger.warning(typeShapeResult.getErrorMessage());
            return typeShapeResult;
        }

        // 範囲パラメータのチェック
        ValidationResult rangeResult = validateRangeParameters(target, fileName);
        if (!rangeResult.isValid()) {
            logger.warning(rangeResult.getErrorMessage());
            return rangeResult;
        }

        // 必要な設定が存在するかチェック
        ValidationResult configResult = validateRequiredConfigs(target, fileName);
        if (!configResult.isValid()) {
            logger.warning(configResult.getErrorMessage());
            return configResult;
        }

        return ValidationResult.success();
    }

    /**
     * タイプと形状の組み合わせをバリデートします
     */
    private static ValidationResult validateTypeShapeCombination(SkillTarget target, String fileName) {
        TargetType type = target.getType();
        AreaShape shape = target.getAreaShape();

        // SINGLE形状は単体ターゲットタイプのみ
        if (shape == AreaShape.SINGLE) {
            if (type != TargetType.SELF
                    && type != TargetType.SELF_PLUS_ONE
                    && type != TargetType.NEAREST_HOSTILE
                    && type != TargetType.NEAREST_PLAYER
                    && type != TargetType.NEAREST_ENTITY
                    && type != TargetType.EXTERNAL) {
                return ValidationResult.failure(
                    String.format("[Config] Invalid type-shape combination: type=%s, shape=%s in %s",
                        type, shape, fileName));
            }
        }

        // CONE形状はCONEタイプまたはAREAタイプ
        if (shape == AreaShape.CONE && type != TargetType.CONE && type != TargetType.AREA_SELF && type != TargetType.AREA_OTHERS) {
            // 警告のみ、許容する
        }

        return ValidationResult.success();
    }

    /**
     * 範囲パラメータをバリデートします
     */
    private static ValidationResult validateRangeParameters(SkillTarget target, String fileName) {
        // 範囲値が正数かチェック
        Double range = target.getRawRange();
        if (range != null && range <= 0) {
            return ValidationResult.failure(
                String.format("[Config] range must be positive: %.2f in %s", range, fileName));
        }

        Double lineWidth = target.getRawLineWidth();
        if (lineWidth != null && lineWidth < 0) {
            return ValidationResult.failure(
                String.format("[Config] line_width must be non-negative: %.2f in %s", lineWidth, fileName));
        }

        Double coneAngle = target.getRawConeAngle();
        if (coneAngle != null && (coneAngle <= 0 || coneAngle > 360)) {
            return ValidationResult.failure(
                String.format("[Config] cone_angle must be in (0, 360]: %.2f in %s", coneAngle, fileName));
        }

        Double sphereRadius = target.getRawSphereRadius();
        if (sphereRadius != null && sphereRadius <= 0) {
            return ValidationResult.failure(
                String.format("[Config] sphere_radius must be positive: %.2f in %s", sphereRadius, fileName));
        }

        // ConeConfigのチェック
        if (target.getConeAsOptional().isPresent()) {
            SkillTarget.ConeConfig cone = target.getConeAsOptional().get();
            if (cone.getRange() <= 0) {
                return ValidationResult.failure(
                    String.format("[Config] cone.range must be positive: %.2f in %s", cone.getRange(), fileName));
            }
            if (cone.getAngle() <= 0 || cone.getAngle() > 360) {
                return ValidationResult.failure(
                    String.format("[Config] cone.angle must be in (0, 360]: %.2f in %s", cone.getAngle(), fileName));
            }
        }

        // CircleConfigのチェック
        if (target.getCircleAsOptional().isPresent()) {
            SkillTarget.CircleConfig circle = target.getCircleAsOptional().get();
            if (circle.getRadius() <= 0) {
                return ValidationResult.failure(
                    String.format("[Config] circle.radius must be positive: %.2f in %s", circle.getRadius(), fileName));
            }
        }

        // RectConfigのチェック
        if (target.getRectAsOptional().isPresent()) {
            SkillTarget.RectConfig rect = target.getRectAsOptional().get();
            if (rect.getWidth() <= 0 || rect.getDepth() <= 0) {
                return ValidationResult.failure(
                    String.format("[Config] rect dimensions must be positive: width=%.2f, depth=%.2f in %s",
                        rect.getWidth(), rect.getDepth(), fileName));
            }
        }

        return ValidationResult.success();
    }

    /**
     * 必要な設定が存在するかバリデートします
     */
    private static ValidationResult validateRequiredConfigs(SkillTarget target, String fileName) {
        AreaShape shape = target.getAreaShape();

        // CONE形状にはConeConfigが必要
        if (shape == AreaShape.CONE && target.getConeAsOptional().isEmpty()) {
            // 汎用パラメータがある場合は許容
            if (target.getRawRange() == null) {
                return ValidationResult.failure(
                    String.format("[Config] cone configuration is missing for CONE shape in %s", fileName));
            }
        }

        // RECT形状にはRectConfigが必要
        if (shape == AreaShape.RECT && target.getRectAsOptional().isEmpty()) {
            return ValidationResult.failure(
                String.format("[Config] rect configuration is missing for RECT shape in %s", fileName));
        }

        // CIRCLE形状にはCircleConfigが必要
        if (shape == AreaShape.CIRCLE && target.getCircleAsOptional().isEmpty()) {
            return ValidationResult.failure(
                String.format("[Config] circle configuration is missing for CIRCLE shape in %s", fileName));
        }

        return ValidationResult.success();
    }

    /**
     * maxTargetsの値をバリデートします
     *
     * @param maxTargets 最大ターゲット数
     * @param fileName ファイル名
     * @param logger ロガー
     * @return バリデートされた値（範囲外の場合はクリップ）
     */
    public static int validateMaxTargets(Integer maxTargets, String fileName, Logger logger) {
        if (maxTargets == null) {
            return Integer.MAX_VALUE;
        }

        if (maxTargets <= 0) {
            logger.warning(String.format(
                "[Config] max_targets must be positive: %d in %s, using unlimited", maxTargets, fileName));
            return Integer.MAX_VALUE;
        }

        if (maxTargets > 100) {
            logger.warning(String.format(
                "[Config] max_targets is suspiciously large: %d in %s", maxTargets, fileName));
        }

        return maxTargets;
    }
}
