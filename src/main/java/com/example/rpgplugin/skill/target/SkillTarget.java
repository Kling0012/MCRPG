package com.example.rpgplugin.skill.target;

import java.util.Objects;

/**
 * スキルのターゲット設定を表すクラス
 *
 * <p>YAML設定からパースされ、ターゲット選択と範囲計算に使用されます。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ターゲット設定の表現に専念</li>
 *   <li>DRY: 不変クラスで安全にデータを保持</li>
 *   <li>YAGNI: 必要な設定のみ保持</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillTarget {

    private final TargetType type;
    private final AreaShape areaShape;
    private final SingleTargetConfig singleTarget;
    private final ConeConfig cone;
    private final RectConfig rect;
    private final CircleConfig circle;

    /**
     * コンストラクタ
     *
     * @param type ターゲットタイプ
     * @param areaShape 範囲形状
     * @param singleTarget 単体ターゲット設定
     * @param cone 扇状範囲設定
     * @param rect 四角形範囲設定
     * @param circle 円形範囲設定
     */
    public SkillTarget(TargetType type, AreaShape areaShape,
                       SingleTargetConfig singleTarget, ConeConfig cone,
                       RectConfig rect, CircleConfig circle) {
        this.type = type != null ? type : TargetType.NEAREST_HOSTILE;
        this.areaShape = areaShape != null ? areaShape : AreaShape.SINGLE;
        this.singleTarget = singleTarget;
        this.cone = cone;
        this.rect = rect;
        this.circle = circle;
    }

    /**
     * デフォルト設定のインスタンスを作成します
     *
     * @return デフォルト設定のSkillTarget
     */
    public static SkillTarget createDefault() {
        return new SkillTarget(
                TargetType.NEAREST_HOSTILE,
                AreaShape.SINGLE,
                new SingleTargetConfig(true, false),
                null,
                null,
                null
        );
    }

    public TargetType getType() {
        return type;
    }

    public AreaShape getAreaShape() {
        return areaShape;
    }

    public SingleTargetConfig getSingleTarget() {
        return singleTarget;
    }

    public ConeConfig getCone() {
        return cone;
    }

    public RectConfig getRect() {
        return rect;
    }

    public CircleConfig getCircle() {
        return circle;
    }

    /**
     * 単体ターゲット設定
     */
    public static class SingleTargetConfig {
        private final boolean selectNearest;
        private final boolean targetSelf;

        public SingleTargetConfig(boolean selectNearest, boolean targetSelf) {
            this.selectNearest = selectNearest;
            this.targetSelf = targetSelf;
        }

        public boolean isSelectNearest() {
            return selectNearest;
        }

        public boolean isTargetSelf() {
            return targetSelf;
        }
    }

    /**
     * 扇状範囲設定
     */
    public static class ConeConfig {
        private final double angle;
        private final double range;

        public ConeConfig(double angle, double range) {
            this.angle = angle;
            this.range = range;
        }

        public double getAngle() {
            return angle;
        }

        public double getRange() {
            return range;
        }
    }

    /**
     * 四角形範囲設定
     */
    public static class RectConfig {
        private final double width;
        private final double depth;

        public RectConfig(double width, double depth) {
            this.width = width;
            this.depth = depth;
        }

        public double getWidth() {
            return width;
        }

        public double getDepth() {
            return depth;
        }
    }

    /**
     * 円形範囲設定
     */
    public static class CircleConfig {
        private final double radius;

        public CircleConfig(double radius) {
            this.radius = radius;
        }

        public double getRadius() {
            return radius;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillTarget that = (SkillTarget) o;
        return type == that.type && areaShape == that.areaShape;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, areaShape);
    }

    @Override
    public String toString() {
        return "SkillTarget{" +
                "type=" + type +
                ", areaShape=" + areaShape +
                '}';
    }
}
