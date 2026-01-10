package com.example.rpgplugin.skill.target;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * スキルのターゲット選択を行うクラス
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ターゲット選択に専念</li>
 *   <li>KISS: シンプルな選択ロジック</li>
 *   <li>DRY: 範囲計算はShapeCalculatorに委譲</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public final class TargetSelector {

    private TargetSelector() {
        // ユーティリティクラスのためインスタンス化禁止
    }

    /**
     * ターゲットを選択します
     *
     * @param caster 発動者
     * @param config ターゲット設定
     * @param candidates 候補エンティティリスト
     * @param externalTarget 外部から指定されたターゲット（EXTERNALタイプ時のみ使用）
     * @return 選択されたターゲットリスト
     */
    public static List<Entity> selectTargets(Player caster, SkillTarget config,
                                              List<Entity> candidates, Entity externalTarget) {
        List<Entity> result = new ArrayList<>();

        if (config == null || caster == null) {
            return result;
        }

        // candidatesのnull安全
        if (candidates == null) {
            candidates = new ArrayList<>();
        }

        TargetType type = config.getType();
        Location casterLocation = caster.getLocation();
        Location origin = casterLocation;
        Vector direction = casterLocation.getDirection();

        switch (type) {
            case SELF:
                result.add(caster);
                break;

            case SELF_PLUS_ONE:
                selectSelfPlusOne(caster, config, candidates, origin, direction, result);
                break;

            case NEAREST_HOSTILE:
                selectNearestHostile(caster, config, candidates, origin, direction, result);
                break;

            case NEAREST_PLAYER:
                selectNearestPlayer(caster, config, candidates, origin, direction, result);
                break;

            case NEAREST_ENTITY:
                selectNearestEntity(caster, config, candidates, origin, direction, result);
                break;

            case AREA_SELF:
                selectAreaTargets(caster, config, candidates, origin, direction, result, true);
                break;

            case AREA_OTHERS:
                selectAreaTargets(caster, config, candidates, origin, direction, result, false);
                break;

            case EXTERNAL:
                if (externalTarget != null) {
                    result.add(externalTarget);
                }
                break;

            case LINE:
                selectLineTargets(caster, config, candidates, origin, direction, result);
                break;

            case CONE:
                selectConeTargets(caster, config, candidates, origin, direction, result);
                break;

            case LOOKING:
                selectLookingTargets(caster, config, candidates, origin, direction, result);
                break;

            case SPHERE:
                selectSphereTargets(caster, config, candidates, origin, result);
                break;

            default:
                break;
        }

        return result;
    }

    /**
     * 自分と最も近いターゲット一人を選択します
     *
     * @param caster 発動者
     * @param config ターゲット設定
     * @param candidates 候補エンティティリスト
     * @param origin 中心位置
     * @param direction 方向ベクトル
     * @param result 結果を追加するリスト
     */
    private static void selectSelfPlusOne(Player caster, SkillTarget config,
                                          List<Entity> candidates, Location origin,
                                          Vector direction, List<Entity> result) {
        // 自分を追加
        result.add(caster);

        // EntityTypeFilterに基づいて候補をフィルタリング
        List<Entity> filtered = filterByEntityType(candidates, config.getEntityTypeFilter(), caster);
        filtered = filtered.stream()
                .filter(e -> isInRange(e, origin, direction, config))
                .toList();

        if (filtered.isEmpty()) {
            return;
        }

        // 最も近いエンティティを一人追加
        findNearest(origin, filtered).ifPresent(result::add);
    }

    /**
     * 最も近い敵対MOBを選択します
     *
     * @param caster 発動者
     * @param config ターゲット設定
     * @param candidates 候補エンティティリスト
     * @param origin 中心位置
     * @param direction 方向ベクトル
     * @param result 結果を追加するリスト
     */
    private static void selectNearestHostile(Player caster, SkillTarget config,
                                              List<Entity> candidates, Location origin,
                                              Vector direction, List<Entity> result) {
        // 自分をターゲットにする設定の場合
        if (config.getSingleTarget() != null && config.getSingleTarget().isTargetSelf()) {
            result.add(caster);
            return;
        }

        // Mobのみフィルタリング
        List<Entity> mobs = filterByEntityType(candidates, EntityTypeFilter.MOB_ONLY, caster);
        mobs = mobs.stream()
                .filter(e -> e != null)
                .filter(e -> isInRange(e, origin, direction, config))
                .toList();

        if (mobs.isEmpty()) {
            return;
        }

        // 最も近いエンティティを選択
        findNearest(origin, mobs).ifPresent(result::add);
    }

    /**
     * 最も近いプレイヤーを選択します
     *
     * @param caster 発動者
     * @param config ターゲット設定
     * @param candidates 候補エンティティリスト
     * @param origin 中心位置
     * @param direction 方向ベクトル
     * @param result 結果を追加するリスト
     */
    private static void selectNearestPlayer(Player caster, SkillTarget config,
                                             List<Entity> candidates, Location origin,
                                             Vector direction, List<Entity> result) {
        // 自分をターゲットにする設定の場合
        if (config.getSingleTarget() != null && config.getSingleTarget().isTargetSelf()) {
            result.add(caster);
            return;
        }

        // プレイヤーのみフィルタリング
        List<Entity> players = filterByEntityType(candidates, EntityTypeFilter.PLAYER_ONLY, caster);
        players = players.stream()
                .filter(e -> isInRange(e, origin, direction, config))
                .toList();

        if (players.isEmpty()) {
            return;
        }

        // 最も近いプレイヤーを選択
        findNearest(origin, players).ifPresent(result::add);
    }

    /**
     * 最も近いエンティティを選択します
     *
     * @param caster 発動者
     * @param config ターゲット設定
     * @param candidates 候補エンティティリスト
     * @param origin 中心位置
     * @param direction 方向ベクトル
     * @param result 結果を追加するリスト
     */
    private static void selectNearestEntity(Player caster, SkillTarget config,
                                             List<Entity> candidates, Location origin,
                                             Vector direction, List<Entity> result) {
        // 自分をターゲットにする設定の場合
        if (config.getSingleTarget() != null && config.getSingleTarget().isTargetSelf()) {
            result.add(caster);
            return;
        }

        // EntityTypeFilterに基づいて候補をフィルタリング
        List<Entity> filtered = filterByEntityType(candidates, config.getEntityTypeFilter(), caster);
        filtered = filtered.stream()
                .filter(e -> e != null)
                .filter(e -> !e.getUniqueId().equals(caster.getUniqueId()))
                .filter(e -> isInRange(e, origin, direction, config))
                .toList();

        if (filtered.isEmpty()) {
            return;
        }

        findNearest(origin, filtered).ifPresent(result::add);
    }

    /**
     * 範囲内の全ターゲットを選択します
     *
     * @param caster 発動者
     * @param config ターゲット設定
     * @param candidates 候補エンティティリスト
     * @param origin 中心位置
     * @param direction 方向ベクトル
     * @param result 結果を追加するリスト
     * @param includeSelf 自分を含めるかどうか
     */
    private static void selectAreaTargets(Player caster, SkillTarget config,
                                          List<Entity> candidates, Location origin,
                                          Vector direction, List<Entity> result,
                                          boolean includeSelf) {
        // EntityTypeFilterに基づいて候補をフィルタリング
        List<Entity> filtered = filterByEntityType(candidates, config.getEntityTypeFilter(), caster);

        // 範囲内のエンティティを選択
        List<Entity> inRange = filtered.stream()
                .filter(e -> e != null)
                .filter(e -> isInRange(e, origin, direction, config))
                .filter(e -> includeSelf || !e.getUniqueId().equals(caster.getUniqueId()))
                .toList();

        // 最大ターゲット数を適用
        int maxTargets = config.getMaxTargetsOrUnlimited();
        if (inRange.size() > maxTargets) {
            // 距離の順にソートして最大数に制限
            inRange = inRange.stream()
                    .sorted(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(origin)))
                    .limit(maxTargets)
                    .toList();
        }

        result.addAll(inRange);
    }

    /**
     * エンティティが範囲内か判定します
     *
     * @param entity エンティティ
     * @param origin 中心位置
     * @param direction 方向ベクトル
     * @param config ターゲット設定
     * @return 範囲内の場合はtrue
     */
    private static boolean isInRange(Entity entity, Location origin,
                                      Vector direction, SkillTarget config) {
        AreaShape shape = config.getAreaShape();

        // SINGLEの場合は簡易判定
        if (shape == AreaShape.SINGLE) {
            double distance = entity.getLocation().distance(origin);
            return distance <= 5.0; // デフォルト探索範囲
        }

        return ShapeCalculator.isInRange(entity, origin, direction, shape, config);
    }

    /**
     * 指定位置から最も近いエンティティを見つけます
     *
     * @param origin 基準位置
     * @param entities 検索対象エンティティリスト
     * @return 最も近いエンティティ（存在する場合）
     */
    private static Optional<Entity> findNearest(Location origin, List<Entity> entities) {
        return entities.stream()
                .filter(e -> e != null && e.getLocation() != null)
                .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(origin)));
    }

    /**
     * 近くのエンティティを取得します
     *
     * @param origin 中心位置
     * @param radius 検索半径
     * @return 近くのエンティティリスト
     */
    public static List<Entity> getNearbyEntities(Location origin, double radius) {
        if (origin.getWorld() == null) {
            return List.of();
        }
        return new ArrayList<>(origin.getWorld().getNearbyEntities(origin, radius, radius, radius));
    }

    /**
     * エンティティタイプフィルタで候補をフィルタリングします
     *
     * @param candidates 候補エンティティリスト
     * @param filter エンティティタイプフィルタ
     * @param caster 発動者（自分除外用）
     * @return フィルタリングされたエンティティリスト
     */
    private static List<Entity> filterByEntityType(List<Entity> candidates,
                                                     EntityTypeFilter filter,
                                                     Player caster) {
        return candidates.stream()
                .filter(e -> matchesFilter(e, filter))
                .toList();
    }

    /**
     * エンティティがフィルタ条件に一致するか判定します
     *
     * @param entity エンティティ
     * @param filter エンティティタイプフィルタ
     * @return 一致する場合はtrue
     */
    private static boolean matchesFilter(Entity entity, EntityTypeFilter filter) {
        return switch (filter) {
            case PLAYER_ONLY -> entity instanceof Player;
            case MOB_ONLY -> !(entity instanceof Player);
            case ALL -> true;
        };
    }

    // ==================== 新規ターゲット選択メソッド ====================

    /**
     * 直線上のターゲットを選択します（SkillAPI参考）
     *
     * @param caster 発動者
     * @param config ターゲット設定
     * @param candidates 候補エンティティリスト
     * @param origin 中心位置
     * @param direction 方向ベクトル
     * @param result 結果を追加するリスト
     */
    private static void selectLineTargets(Player caster, SkillTarget config,
                                            List<Entity> candidates, Location origin,
                                            Vector direction, List<Entity> result) {
        List<Entity> filtered = filterByEntityType(candidates, config.getEntityTypeFilter(), caster);

        // 直線の範囲を設定
        double range = config.getRange();
        double width = config.getLineWidth();

        for (Entity entity : filtered) {
            if (entity == null || entity.getLocation() == null) {
                continue;
            }

            // 直線上か判定
            if (isOnLine(entity.getLocation(), origin, direction, range, width)) {
                result.add(entity);
            }
        }

        // 最大ターゲット数を適用
        int maxTargets = config.getMaxTargetsOrUnlimited();
        if (result.size() > maxTargets) {
            result.subList(0, maxTargets);
        }
    }

    /**
     * 扇状範囲のターゲットを選択します（SkillAPI参考）
     *
     * @param caster 発動者
     * @param config ターゲット設定
     * @param candidates 候補エンティティリスト
     * @param origin 中心位置
     * @param direction 方向ベクトル
     * @param result 結果を追加するリスト
     */
    private static void selectConeTargets(Player caster, SkillTarget config,
                                            List<Entity> candidates, Location origin,
                                            Vector direction, List<Entity> result) {
        List<Entity> filtered = filterByEntityType(candidates, config.getEntityTypeFilter(), caster);

        // コーンの範囲を設定
        double range = config.getRange();
        double angle = config.getConeAngle();

        for (Entity entity : filtered) {
            if (entity == null || entity.getLocation() == null) {
                continue;
            }

            // コーン内か判定
            if (isInCone(entity.getLocation(), origin, direction, range, angle)) {
                result.add(entity);
            }
        }

        // 最大ターゲット数を適用
        int maxTargets = config.getMaxTargetsOrUnlimited();
        if (result.size() > maxTargets) {
            result.subList(0, maxTargets);
        }
    }

    /**
     * 視線上のターゲットを選択します（SkillAPI参考）
     *
     * @param caster 発動者
     * @param config ターゲット設定
     * @param candidates 候補エンティティリスト
     * @param origin 中心位置
     * @param direction 方向ベクトル
     * @param result 結果を追加するリスト
     */
    private static void selectLookingTargets(Player caster, SkillTarget config,
                                              List<Entity> candidates, Location origin,
                                              Vector direction, List<Entity> result) {
        List<Entity> filtered = filterByEntityType(candidates, config.getEntityTypeFilter(), caster);

        // 視線の範囲を設定
        double range = config.getRange();
        double width = config.getLineWidth();

        // 視線上のエンティティを距離順に取得
        List<Entity> lookingTargets = new ArrayList<>();
        for (Entity entity : filtered) {
            if (entity == null || entity.getLocation() == null) {
                continue;
            }

            if (isOnLine(entity.getLocation(), origin, direction, range, width)) {
                lookingTargets.add(entity);
            }
        }

        // 距離でソート
        lookingTargets.sort(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(origin)));

        // 最大ターゲット数を適用
        int maxTargets = config.getMaxTargetsOrUnlimited();
        for (int i = 0; i < Math.min(lookingTargets.size(), maxTargets); i++) {
            result.add(lookingTargets.get(i));
        }
    }

    /**
     * 球形範囲のターゲットを選択します（SkillAPI参考）
     *
     * @param caster 発動者
     * @param config ターゲット設定
     * @param candidates 候補エンティティリスト
     * @param origin 中心位置
     * @param result 結果を追加するリスト
     */
    private static void selectSphereTargets(Player caster, SkillTarget config,
                                             List<Entity> candidates, Location origin,
                                             List<Entity> result) {
        List<Entity> filtered = filterByEntityType(candidates, config.getEntityTypeFilter(), caster);

        // 球形の範囲を設定
        double radius = config.getSphereRadius();

        for (Entity entity : filtered) {
            if (entity == null || entity.getLocation() == null) {
                continue;
            }

            // 球形内か判定
            if (entity.getLocation().distance(origin) <= radius) {
                result.add(entity);
            }
        }

        // 最大ターゲット数を適用
        int maxTargets = config.getMaxTargetsOrUnlimited();
        if (result.size() > maxTargets) {
            result.subList(0, maxTargets);
        }
    }

    /**
     * エンティティが直線上にあるか判定します
     *
     * @param location エンティティの位置
     * @param origin 始点
     * @param direction 方向ベクトル
     * @param range 範囲
     * @param width 幅
     * @return 直線上の場合はtrue
     */
    private static boolean isOnLine(Location location, Location origin,
                                    Vector direction, double range, double width) {
        // 始点からの距離チェック
        double distance = location.distance(origin);
        if (distance > range || distance < 0.5) {
            return false;
        }

        // 直線からのずれを計算
        Vector toEntity = location.toVector().subtract(origin.toVector());
        double projection = toEntity.dot(direction.normalize());

        if (projection < 0 || projection > range) {
            return false;
        }

        Vector closestPoint = origin.toVector().add(direction.normalize().multiply(projection));
        double perpendicularDistance = location.toVector().subtract(closestPoint).length();

        return perpendicularDistance <= width;
    }

    /**
     * エンティティがコーン内にあるか判定します
     *
     * @param location エンティティの位置
     * @param origin 始点
     * @param direction 方向ベクトル
     * @param range 範囲
     * @param angle 角度（度数法）
     * @return コーン内の場合はtrue
     */
    private static boolean isInCone(Location location, Location origin,
                                     Vector direction, double range, double angle) {
        // 始点からの距離チェック
        double distance = location.distance(origin);
        if (distance > range || distance < 0.5) {
            return false;
        }

        // 角度チェック
        Vector toEntity = location.toVector().subtract(origin.toVector());
        double dotProduct = toEntity.normalize().dot(direction.normalize());
        double angleRad = Math.toRadians(angle);

        return dotProduct >= Math.cos(angleRad / 2.0);
    }
}
