package com.example.rpgplugin.model.skill;

/**
 * ターゲット設定
 *
 * <p>スキルのターゲット範囲設定を表します。</p>
 *
 * <p>YAML例:</p>
 * <pre>
 * targeting:
 *   type: cone
 *   cone:
 *     angle: 90
 *     range: 5.0
 * </pre>
 */
public class TargetingConfig {
    private final String type;
    private final TargetingParams params;

    public TargetingConfig(String type, TargetingParams params) {
        this.type = type != null ? type : "single";
        this.params = params;
    }

    public String getType() {
        return type;
    }

    public TargetingParams getParams() {
        return params;
    }

    /**
     * ターゲットパラメータの基底クラス
     */
    public static class TargetingParams {
        private final double range;

        public TargetingParams(double range) {
            this.range = range;
        }

        public double getRange() {
            return range;
        }
    }

    /**
     * コーン型範囲パラメータ
     */
    public static class ConeParams extends TargetingParams {
        private final double angle;

        public ConeParams(double angle, double range) {
            super(range);
            this.angle = angle;
        }

        public double getAngle() {
            return angle;
        }
    }

    /**
     * 球形範囲パラメータ
     */
    public static class SphereParams extends TargetingParams {
        private final double radius;

        public SphereParams(double radius) {
            super(radius);
            this.radius = radius;
        }

        public double getRadius() {
            return radius;
        }
    }

    /**
     * 扇形パラメータ
     */
    public static class SectorParams extends TargetingParams {
        private final double angle;
        private final double radius;

        public SectorParams(double angle, double radius) {
            super(radius);
            this.angle = angle;
            this.radius = radius;
        }

        public double getAngle() {
            return angle;
        }

        public double getRadius() {
            return radius;
        }
    }
}
