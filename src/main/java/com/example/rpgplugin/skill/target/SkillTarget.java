package com.example.rpgplugin.skill.target;

import java.util.Objects;
import java.util.Optional;

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
    private final EntityTypeFilter entityTypeFilter;
    private final Integer maxTargets;

    // グループフィルタ（敵味方フィルタ、SkillAPI参考）
    private final TargetGroupFilter groupFilter;
    // 壁を通すかどうか（SkillAPI参考）
    private final boolean throughWall;
    // ランダム順序（SkillAPI参考）
    private final boolean randomOrder;
    // フィルタ無視でキャスターを含める（SkillAPI参考）
    private final boolean includeCaster;

    // 汎用ターゲット設定（LINE, LOOKING, CONE, SPHERE用）
    private final Double range;
    private final Double lineWidth;
    private final Double coneAngle;
    private final Double sphereRadius;

    /**
     * コンストラクタ
     *
     * @param type ターゲットタイプ
     * @param areaShape 範囲形状
     * @param singleTarget 単体ターゲット設定
     * @param cone 扇状範囲設定
     * @param rect 四角形範囲設定
     * @param circle 円形範囲設定
     * @param entityTypeFilter エンティティタイプフィルタ
     * @param maxTargets 最大ターゲット数（nullで制限なし）
     * @param groupFilter グループフィルタ（敵味方フィルタ）
     * @param throughWall 壁を通すかどうか
     * @param randomOrder ランダム順序
     * @param includeCaster フィルタ無視でキャスターを含める
     * @param range 汎用範囲（LINE, LOOKING用）
     * @param lineWidth 直線の幅（LINE, LOOKING用）
     * @param coneAngle コーンの角度（CONE用、度数法）
     * @param sphereRadius 球形の半径（SPHERE用）
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
     * レガシーコンストラクタ（後方互換性）
     * <p>注意: グループフィルタはBOTH（敵味方両方）がデフォルトです。</p>
     */
    public SkillTarget(TargetType type, AreaShape areaShape,
                       SingleTargetConfig singleTarget, ConeConfig cone,
                       RectConfig rect, CircleConfig circle) {
        this(type, areaShape, singleTarget, cone, rect, circle,
                EntityTypeFilter.ALL, null, TargetGroupFilter.BOTH, false, false, false,
                null, null, null, null);
    }

    /**
     * 汎用コンストラクタ（LINE, LOOKING, CONE, SPHERE用）
     */
    public SkillTarget(TargetType type, Double range, Double lineWidth,
                       Double coneAngle, Double sphereRadius,
                       EntityTypeFilter entityTypeFilter, Integer maxTargets) {
        this(type, AreaShape.SINGLE, null, null, null, null,
                entityTypeFilter, maxTargets, TargetGroupFilter.BOTH, false, false, false,
                range, lineWidth, coneAngle, sphereRadius);
    }

    /**
     * 拡張コンストラクタ（グループフィルタ、壁、ランダム対応）
     */
    public SkillTarget(TargetType type, AreaShape areaShape,
                       SingleTargetConfig singleTarget, ConeConfig cone,
                       RectConfig rect, CircleConfig circle,
                       EntityTypeFilter entityTypeFilter, Integer maxTargets,
                       TargetGroupFilter groupFilter, boolean throughWall,
                       boolean randomOrder, boolean includeCaster) {
        this(type, areaShape, singleTarget, cone, rect, circle,
                entityTypeFilter, maxTargets, groupFilter, throughWall, randomOrder, includeCaster,
                null, null, null, null);
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

    public EntityTypeFilter getEntityTypeFilter() {
        return entityTypeFilter;
    }

    public Integer getMaxTargets() {
        return maxTargets;
    }

    /**
     * 最大ターゲット数を取得します（制限なしの場合はInteger.MAX_VALUE）
     *
     * @return 最大ターゲット数
     */
    public int getMaxTargetsOrUnlimited() {
        return maxTargets != null ? maxTargets : Integer.MAX_VALUE;
    }

    // ==================== グループフィルタ・壁判定ゲッター ====================

    /**
     * グループフィルタを取得します
     *
     * @return グループフィルタ（敵味方フィルタ）
     */
    public TargetGroupFilter getGroupFilter() {
        return groupFilter;
    }

    /**
     * 壁を通すかどうかを取得します
     *
     * @return 壁を通す場合はtrue
     */
    public boolean isThroughWall() {
        return throughWall;
    }

    /**
     * ランダム順序でターゲットを選択するかを取得します
     *
     * @return ランダム順序の場合はtrue
     */
    public boolean isRandomOrder() {
        return randomOrder;
    }

    /**
     * フィルタ無視でキャスターを含めるかを取得します
     *
     * @return キャスターを含める場合はtrue
     */
    public boolean isIncludeCaster() {
        return includeCaster;
    }

    // ==================== 汎用ターゲット設定ゲッター ====================

    /**
     * 汎用範囲を取得します（LINE, LOOKING用）
     *
     * @return 範囲（未設定の場合はデフォルト値）
     */
    public double getRange() {
        if (range != null) {
            return range;
        }
        // ConeConfigから取得を試みる
        if (cone != null) {
            return cone.getRange();
        }
        return 10.0; // デフォルト値
    }

    /**
     * 直線の幅を取得します（LINE, LOOKING用）
     *
     * @return 幅（未設定の場合はデフォルト値）
     */
    public double getLineWidth() {
        return lineWidth != null ? lineWidth : 2.0; // デフォルト値
    }

    /**
     * コーンの角度を取得します（度数法）
     *
     * @return 角度（未設定の場合はConeConfigから取得、デフォルト60度）
     */
    public double getConeAngle() {
        if (coneAngle != null) {
            return coneAngle;
        }
        // ConeConfigから取得を試みる
        if (cone != null) {
            return cone.getAngle();
        }
        return 60.0; // デフォルト値
    }

    /**
     * 球形の半径を取得します（SPHERE用）
     *
     * @return 半径（未設定の場合はデフォルト値）
     */
    public double getSphereRadius() {
        if (sphereRadius != null) {
            return sphereRadius;
        }
        // CircleConfigから取得を試みる
        if (circle != null) {
            return circle.getRadius();
        }
        return 5.0; // デフォルト値
    }

    /**
     * 扇状範囲設定をOptionalで取得します
     *
     * @return 扇状範囲設定
     */
    public Optional<ConeConfig> getConeAsOptional() {
        return Optional.ofNullable(cone);
    }

    /**
     * 四角形範囲設定をOptionalで取得します
     *
     * @return 四角形範囲設定
     */
    public Optional<RectConfig> getRectAsOptional() {
        return Optional.ofNullable(rect);
    }

    /**
     * 円形範囲設定をOptionalで取得します
     *
     * @return 円形範囲設定
     */
    public Optional<CircleConfig> getCircleAsOptional() {
        return Optional.ofNullable(circle);
    }

    /**
     * 単体ターゲット設定をOptionalで取得します
     *
     * @return 単体ターゲット設定
     */
    public Optional<SingleTargetConfig> getSingleTargetAsOptional() {
        return Optional.ofNullable(singleTarget);
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
