package com.example.rpgplugin.skill.target;

import java.util.Objects;
import java.util.Optional;

/**
 * スキルのターゲット設定を表す値オブジェクト（Value Object）
 *
 * <p>YAML設定からパースされ、ターゲット選択と範囲計算に使用されます。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>Value Objectパターン: 不変で等価性に基づくデータクラス</li>
 *   <li>SOLID-S: ターゲット設定の表現に専念</li>
 *   <li>バリデーションと計算はTargetValidatorとTargetCalculatorに委譲</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 2.0.0 - Value Objectへのリファクタリング
 */
public final class SkillTarget {

    private final TargetType type;
    private final AreaShape areaShape;
    private final SingleTargetConfig singleTarget;
    private final ConeConfig cone;
    private final RectConfig rect;
    private final CircleConfig circle;
    private final EntityTypeFilter entityTypeFilter;
    private final Integer maxTargets;
    private final TargetGroupFilter groupFilter;
    private final boolean throughWall;
    private final boolean randomOrder;
    private final boolean includeCaster;

    // 汎用ターゲット設定（LINE, LOOKING, CONE, SPHERE用）
    private final Double range;
    private final Double lineWidth;
    private final Double coneAngle;
    private final Double sphereRadius;

    /**
     * プライベートコンストラクタ
     * <p>ビルダーパターンを使用してインスタンスを作成してください。</p>
     */
    private SkillTarget(Builder builder) {
        this.type = builder.type != null ? builder.type : TargetType.NEAREST_HOSTILE;
        this.areaShape = builder.areaShape != null ? builder.areaShape : AreaShape.SINGLE;
        this.singleTarget = builder.singleTarget;
        this.cone = builder.cone;
        this.rect = builder.rect;
        this.circle = builder.circle;
        this.entityTypeFilter = builder.entityTypeFilter != null ? builder.entityTypeFilter : EntityTypeFilter.ALL;
        this.maxTargets = builder.maxTargets;
        this.groupFilter = builder.groupFilter != null ? builder.groupFilter : TargetGroupFilter.BOTH;
        this.throughWall = builder.throughWall;
        this.randomOrder = builder.randomOrder;
        this.includeCaster = builder.includeCaster;
        this.range = builder.range;
        this.lineWidth = builder.lineWidth;
        this.coneAngle = builder.coneAngle;
        this.sphereRadius = builder.sphereRadius;
    }

    /**
     * レガシーコンストラクタ（後方互換性）
     *
     * @param type ターゲットタイプ
     * @param areaShape 範囲形状
     * @param singleTarget 単体ターゲット設定
     * @param cone 扇状範囲設定
     * @param rect 四角形範囲設定
     * @param circle 円形範囲設定
     * @deprecated ビルダーの使用を推奨
     */
    @Deprecated
    public SkillTarget(TargetType type, AreaShape areaShape,
                       SingleTargetConfig singleTarget, ConeConfig cone,
                       RectConfig rect, CircleConfig circle) {
        this(type, areaShape, singleTarget, cone, rect, circle,
                EntityTypeFilter.ALL, null, TargetGroupFilter.BOTH,
                false, false, false, null, null, null, null);
    }

    /**
     * 全パラメータコンストラクタ（後方互換性）
     */
    public SkillTarget(TargetType type, AreaShape areaShape,
                       SingleTargetConfig singleTarget, ConeConfig cone,
                       RectConfig rect, CircleConfig circle,
                       EntityTypeFilter entityTypeFilter, Integer maxTargets,
                       TargetGroupFilter groupFilter, boolean throughWall,
                       boolean randomOrder, boolean includeCaster,
                       Double range, Double lineWidth, Double coneAngle, Double sphereRadius) {
        this.type = type != null ? type : TargetType.NEAREST_HOSTILE;
        this.areaShape = areaShape != null ? areaShape : AreaShape.SINGLE;
        this.singleTarget = singleTarget;
        this.cone = cone;
        this.rect = rect;
        this.circle = circle;
        this.entityTypeFilter = entityTypeFilter != null ? entityTypeFilter : EntityTypeFilter.ALL;
        this.maxTargets = maxTargets;
        this.groupFilter = groupFilter != null ? groupFilter : TargetGroupFilter.BOTH;
        this.throughWall = throughWall;
        this.randomOrder = randomOrder;
        this.includeCaster = includeCaster;
        this.range = range;
        this.lineWidth = lineWidth;
        this.coneAngle = coneAngle;
        this.sphereRadius = sphereRadius;
    }

    /**
     * デフォルト設定のインスタンスを作成します
     *
     * @return デフォルト設定のSkillTarget
     */
    public static SkillTarget createDefault() {
        return new Builder()
                .type(TargetType.NEAREST_HOSTILE)
                .areaShape(AreaShape.SINGLE)
                .singleTarget(new SingleTargetConfig(true, false))
                .build();
    }

    /**
     * ビルダーを取得します
     *
     * @return 新しいビルダーインスタンス
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== ゲッターメソッド ====================

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

    public EntityTypeFilter getEntityTypeFilter() {
        return entityTypeFilter;
    }

    public Integer getMaxTargets() {
        return maxTargets;
    }

    public TargetGroupFilter getGroupFilter() {
        return groupFilter;
    }

    public boolean isThroughWall() {
        return throughWall;
    }

    public boolean isRandomOrder() {
        return randomOrder;
    }

    public boolean isIncludeCaster() {
        return includeCaster;
    }

    // ==================== Raw値取得メソッド（計算なし） ====================

    /**
     * 汎用範囲パラメータを取得します（生値）
     * <p>計算済みの値が必要な場合はTargetCalculator.getEffectiveRange()を使用してください。</p>
     *
     * @return 範囲（未設定の場合はnull）
     */
    public Double getRawRange() {
        return range;
    }

    /**
     * 直線の幅パラメータを取得します（生値）
     * <p>計算済みの値が必要な場合はTargetCalculator.getEffectiveLineWidth()を使用してください。</p>
     *
     * @return 幅（未設定の場合はnull）
     */
    public Double getRawLineWidth() {
        return lineWidth;
    }

    /**
     * コーンの角度パラメータを取得します（生値）
     * <p>計算済みの値が必要な場合はTargetCalculator.getEffectiveConeAngle()を使用してください。</p>
     *
     * @return 角度（未設定の場合はnull）
     */
    public Double getRawConeAngle() {
        return coneAngle;
    }

    /**
     * 球形の半径パラメータを取得します（生値）
     * <p>計算済みの値が必要な場合はTargetCalculator.getEffectiveSphereRadius()を使用してください。</p>
     *
     * @return 半径（未設定の場合はnull）
     */
    public Double getRawSphereRadius() {
        return sphereRadius;
    }

    // ==================== 互換性のための計算メソッド（委譲） ====================

    /**
     * 最大ターゲット数を取得します（制限なしの場合はInteger.MAX_VALUE）
     * <p>内部的にはTargetCalculator.getEffectiveMaxTargets()に委譲しています。</p>
     *
     * @return 最大ターゲット数
     * @deprecated TargetCalculator.getEffectiveMaxTargets()の使用を推奨
     */
    @Deprecated
    public int getMaxTargetsOrUnlimited() {
        return TargetCalculator.getEffectiveMaxTargets(this);
    }

    /**
     * 有効な範囲を取得します
     * <p>内部的にはTargetCalculator.getEffectiveRange()に委譲しています。</p>
     *
     * @return 有効な範囲
     * @deprecated TargetCalculator.getEffectiveRange()の使用を推奨
     */
    @Deprecated
    public double getRange() {
        return TargetCalculator.getEffectiveRange(this);
    }

    /**
     * 有効な直線の幅を取得します
     * <p>内部的にはTargetCalculator.getEffectiveLineWidth()に委譲しています。</p>
     *
     * @return 有効な幅
     * @deprecated TargetCalculator.getEffectiveLineWidth()の使用を推奨
     */
    @Deprecated
    public double getLineWidth() {
        return TargetCalculator.getEffectiveLineWidth(this);
    }

    /**
     * 有効なコーンの角度を取得します（度数法）
     * <p>内部的にはTargetCalculator.getEffectiveConeAngle()に委譲しています。</p>
     *
     * @return 有効な角度
     * @deprecated TargetCalculator.getEffectiveConeAngle()の使用を推奨
     */
    @Deprecated
    public double getConeAngle() {
        return TargetCalculator.getEffectiveConeAngle(this);
    }

    /**
     * 有効な球形の半径を取得します
     * <p>内部的にはTargetCalculator.getEffectiveSphereRadius()に委譲しています。</p>
     *
     * @return 有効な半径
     * @deprecated TargetCalculator.getEffectiveSphereRadius()の使用を推奨
     */
    @Deprecated
    public double getSphereRadius() {
        return TargetCalculator.getEffectiveSphereRadius(this);
    }

    // ==================== Optional取得メソッド ====================

    public Optional<ConeConfig> getConeAsOptional() {
        return Optional.ofNullable(cone);
    }

    public Optional<RectConfig> getRectAsOptional() {
        return Optional.ofNullable(rect);
    }

    public Optional<CircleConfig> getCircleAsOptional() {
        return Optional.ofNullable(circle);
    }

    public Optional<SingleTargetConfig> getSingleTargetAsOptional() {
        return Optional.ofNullable(singleTarget);
    }

    // ==================== Value Objectとしての実装 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillTarget that = (SkillTarget) o;
        return throughWall == that.throughWall &&
                randomOrder == that.randomOrder &&
                includeCaster == that.includeCaster &&
                type == that.type &&
                areaShape == that.areaShape &&
                groupFilter == that.groupFilter &&
                entityTypeFilter == that.entityTypeFilter &&
                Objects.equals(singleTarget, that.singleTarget) &&
                Objects.equals(cone, that.cone) &&
                Objects.equals(rect, that.rect) &&
                Objects.equals(circle, that.circle) &&
                Objects.equals(maxTargets, that.maxTargets) &&
                Objects.equals(range, that.range) &&
                Objects.equals(lineWidth, that.lineWidth) &&
                Objects.equals(coneAngle, that.coneAngle) &&
                Objects.equals(sphereRadius, that.sphereRadius);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, areaShape, singleTarget, cone, rect, circle,
                entityTypeFilter, maxTargets, groupFilter, throughWall, randomOrder, includeCaster,
                range, lineWidth, coneAngle, sphereRadius);
    }

    @Override
    public String toString() {
        return "SkillTarget{" +
                "type=" + type +
                ", areaShape=" + areaShape +
                ", maxTargets=" + maxTargets +
                ", groupFilter=" + groupFilter +
                '}';
    }

    // ==================== ビルダークラス ====================

    /**
     * SkillTargetのビルダークラス
     */
    public static final class Builder {
        private TargetType type;
        private AreaShape areaShape;
        private SingleTargetConfig singleTarget;
        private ConeConfig cone;
        private RectConfig rect;
        private CircleConfig circle;
        private EntityTypeFilter entityTypeFilter;
        private Integer maxTargets;
        private TargetGroupFilter groupFilter;
        private boolean throughWall;
        private boolean randomOrder;
        private boolean includeCaster;
        private Double range;
        private Double lineWidth;
        private Double coneAngle;
        private Double sphereRadius;

        private Builder() {}

        public Builder type(TargetType type) {
            this.type = type;
            return this;
        }

        public Builder areaShape(AreaShape areaShape) {
            this.areaShape = areaShape;
            return this;
        }

        public Builder singleTarget(SingleTargetConfig singleTarget) {
            this.singleTarget = singleTarget;
            return this;
        }

        public Builder cone(ConeConfig cone) {
            this.cone = cone;
            return this;
        }

        public Builder rect(RectConfig rect) {
            this.rect = rect;
            return this;
        }

        public Builder circle(CircleConfig circle) {
            this.circle = circle;
            return this;
        }

        public Builder entityTypeFilter(EntityTypeFilter entityTypeFilter) {
            this.entityTypeFilter = entityTypeFilter;
            return this;
        }

        public Builder maxTargets(Integer maxTargets) {
            this.maxTargets = maxTargets;
            return this;
        }

        public Builder groupFilter(TargetGroupFilter groupFilter) {
            this.groupFilter = groupFilter;
            return this;
        }

        public Builder throughWall(boolean throughWall) {
            this.throughWall = throughWall;
            return this;
        }

        public Builder randomOrder(boolean randomOrder) {
            this.randomOrder = randomOrder;
            return this;
        }

        public Builder includeCaster(boolean includeCaster) {
            this.includeCaster = includeCaster;
            return this;
        }

        public Builder range(Double range) {
            this.range = range;
            return this;
        }

        public Builder lineWidth(Double lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        public Builder coneAngle(Double coneAngle) {
            this.coneAngle = coneAngle;
            return this;
        }

        public Builder sphereRadius(Double sphereRadius) {
            this.sphereRadius = sphereRadius;
            return this;
        }

        public SkillTarget build() {
            return new SkillTarget(this);
        }
    }

    // ==================== 内部クラス：設定値 ====================

    /**
     * 単体ターゲット設定（不変）
     */
    public static final class SingleTargetConfig {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SingleTargetConfig that = (SingleTargetConfig) o;
            return selectNearest == that.selectNearest && targetSelf == that.targetSelf;
        }

        @Override
        public int hashCode() {
            return Objects.hash(selectNearest, targetSelf);
        }
    }

    /**
     * 扇状範囲設定（不変）
     */
    public static final class ConeConfig {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConeConfig that = (ConeConfig) o;
            return Double.compare(that.angle, angle) == 0 && Double.compare(that.range, range) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(angle, range);
        }
    }

    /**
     * 四角形範囲設定（不変）
     */
    public static final class RectConfig {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RectConfig that = (RectConfig) o;
            return Double.compare(that.width, width) == 0 && Double.compare(that.depth, depth) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(width, depth);
        }
    }

    /**
     * 円形範囲設定（不変）
     */
    public static final class CircleConfig {
        private final double radius;

        public CircleConfig(double radius) {
            this.radius = radius;
        }

        public double getRadius() {
            return radius;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CircleConfig that = (CircleConfig) o;
            return Double.compare(that.radius, radius) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(radius);
        }
    }
}
