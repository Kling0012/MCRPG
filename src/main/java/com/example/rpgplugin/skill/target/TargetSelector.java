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

        TargetType type = config.getType();
        Location origin = caster.getLocation();
        Vector direction = caster.getLocation().getDirection();

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
}
