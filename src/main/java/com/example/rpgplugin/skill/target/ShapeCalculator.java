package com.example.rpgplugin.skill.target;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * スキルエフェクトの範囲計算を行うクラス
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: 範囲計算に専念</li>
 *   <li>KISS: シンプルな幾何計算</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public final class ShapeCalculator {

    private ShapeCalculator() {
        // ユーティリティクラスのためインスタンス化禁止
    }

    /**
     * エンティティが範囲内に含まれるか判定します
     *
     * @param entity 判定対象エンティティ
     * @param origin 中心位置
     * @param direction 方向ベクトル（nullの場合は全方位判定）
     * @param shape 範囲形状
     * @param config ターゲット設定
     * @return 範囲内の場合はtrue
     */
    public static boolean isInRange(Entity entity, Location origin, Vector direction,
                                     AreaShape shape, SkillTarget config) {
        if (entity == null || origin == null || shape == null) {
            return false;
        }

        Location entityLoc = entity.getLocation();
        if (entityLoc == null) {
            return false;
        }

        if (origin.getWorld() == null || !origin.getWorld().equals(entityLoc.getWorld())) {
            return false;
        }

        switch (shape) {
            case SINGLE:
                return isInRangeSingle(entityLoc, origin);

            case CONE:
                return isInRangeCone(entityLoc, origin, direction, config.getCone());

            case RECT:
                return isInRangeRect(entityLoc, origin, direction, config.getRect());

            case CIRCLE:
                return isInRangeCircle(entityLoc, origin, config.getCircle());

            case SPHERE:
                return isInRangeSphere(entityLoc, origin, config.getSphereRadius());

            default:
                return false;
        }
    }

    /**
     * エンティティが単体ターゲットの範囲内に含まれるか判定します
     *
     * @param entityLoc エンティティ位置
     * @param origin 中心位置
     * @return 範囲内の場合はtrue
     */
    private static boolean isInRangeSingle(Location entityLoc, Location origin) {
        // 単体ターゲット: 接近判定（ブロック単位）
        double distance = entityLoc.distance(origin);
        return distance <= 1.5;
    }

    /**
     * エンティティが扇状範囲内に含まれるか判定します
     *
     * @param entityLoc エンティティ位置
     * @param origin 中心位置
     * @param direction 方向ベクトル
     * @param config 扇状設定
     * @return 範囲内の場合はtrue
     */
    private static boolean isInRangeCone(Location entityLoc, Location origin,
                                          Vector direction, SkillTarget.ConeConfig config) {
        if (config == null || direction == null) {
            return false;
        }

        double distance = entityLoc.distance(origin);
        if (distance > config.getRange() || distance < 0.001) {
            return false;
        }

        // エンティティへの方向ベクトル
        Vector toEntity = entityLoc.toVector().subtract(origin.toVector()).normalize();

        // 角度計算（acosの結果は0～PI）
        double angleRad = Math.acos(clamp(direction.dot(toEntity), -1.0, 1.0));
        double angleDeg = Math.toDegrees(angleRad);

        return angleDeg <= (config.getAngle() / 2.0);
    }

    /**
     * エンティティが四角形範囲内に含まれるか判定します
     *
     * @param entityLoc エンティティ位置
     * @param origin 中心位置
     * @param direction 方向ベクトル
     * @param config 四角形設定
     * @return 範囲内の場合はtrue
     */
    private static boolean isInRangeRect(Location entityLoc, Location origin,
                                          Vector direction, SkillTarget.RectConfig config) {
        if (config == null || direction == null) {
            return false;
        }

        // エンティティへのベクトル
        Vector toEntity = entityLoc.toVector().subtract(origin.toVector());

        // 方向ベクトルを正規化（水平方向のみ）
        Vector forward = direction.clone().setY(0).normalize();
        Vector right = new Vector(forward.getZ(), 0, -forward.getX()); // 右方向

        // 前後距離（奥行き）
        double forwardDist = toEntity.dot(forward);

        // 左右距離（幅）
        double rightDist = Math.abs(toEntity.dot(right));

        // 高さ差
        double heightDiff = Math.abs(toEntity.getY());

        // 判定: 前方、幅内、かつ一定高さ以内
        return forwardDist >= 0 && forwardDist <= config.getDepth()
                && rightDist <= (config.getWidth() / 2.0)
                && heightDiff <= 2.0;
    }

    /**
     * エンティティが円形範囲内に含まれるか判定します
     *
     * @param entityLoc エンティティ位置
     * @param origin 中心位置
     * @param config 円形設定
     * @return 範囲内の場合はtrue
     */
    private static boolean isInRangeCircle(Location entityLoc, Location origin,
                                            SkillTarget.CircleConfig config) {
        if (config == null) {
            return false;
        }

        double distanceSq = entityLoc.distanceSquared(origin);
        return distanceSq <= (config.getRadius() * config.getRadius());
    }

    /**
     * エンティティが球形範囲内に含まれるか判定します
     *
     * @param entityLoc エンティティ位置
     * @param origin 中心位置
     * @param radius 半径
     * @return 範囲内の場合はtrue
     */
    private static boolean isInRangeSphere(Location entityLoc, Location origin, double radius) {
        double distanceSq = entityLoc.distanceSquared(origin);
        return distanceSq <= (radius * radius);
    }

    /**
     * 範囲内のエンティティをフィルタリングします
     *
     * @param candidates 候補エンティティリスト
     * @param origin 中心位置
     * @param direction 方向ベクトル（nullの場合は全方位判定）
     * @param shape 範囲形状
     * @param config ターゲット設定
     * @return 範囲内のエンティティリスト
     */
    public static List<Entity> filterInRange(List<Entity> candidates, Location origin,
                                               Vector direction, AreaShape shape,
                                               SkillTarget config) {
        return candidates.stream()
                .filter(e -> isInRange(e, origin, direction, shape, config))
                .toList();
    }

    /**
     * 値を指定範囲内にクランプします
     *
     * @param value クランプ対象の値
     * @param min 最小値
     * @param max 最大値
     * @return クランプされた値
     */
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
