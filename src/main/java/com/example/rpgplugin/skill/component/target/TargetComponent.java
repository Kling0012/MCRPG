package com.example.rpgplugin.skill.component.target;

import com.example.rpgplugin.skill.component.ComponentType;
import com.example.rpgplugin.skill.component.EffectComponent;
import com.example.rpgplugin.skill.target.TargetType;
import com.example.rpgplugin.skill.target.AreaShape;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * ターゲットコンポーネントの基底クラス
 * <p>スキルのターゲットを選択します</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ターゲット選択に専念</li>
 *   <li>Strategy: 異なるターゲット選択ロジックをStrategyパターンで管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public abstract class TargetComponent extends EffectComponent {

    /**
     * コンストラクタ
     *
     * @param key コンポーネントキー
     */
    protected TargetComponent(String key) {
        super(key);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.TARGET;
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        // ターゲットを選択して置き換え
        List<LivingEntity> selectedTargets = selectTargets(caster, level);

        if (selectedTargets.isEmpty()) {
            return false;
        }

        // 選択されたターゲットで子コンポーネントを実行
        return executeChildren(caster, level, selectedTargets);
    }

    /**
     * ターゲットを選択します
     *
     * @param caster 発動者
     * @param level  スキルレベル
     * @return 選択されたターゲットリスト
     */
    protected abstract List<LivingEntity> selectTargets(LivingEntity caster, int level);

    /**
     * 設定からTargetTypeを取得します
     *
     * @param defaultValue デフォルト値
     * @return TargetType
     */
    protected TargetType getTargetType(TargetType defaultValue) {
        String typeStr = settings.getString("type", defaultValue.getId());
        try {
            return TargetType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /**
     * 設定からAreaShapeを取得します
     *
     * @param defaultValue デフォルト値
     * @return AreaShape
     */
    protected AreaShape getAreaShape(AreaShape defaultValue) {
        String shapeStr = settings.getString("area_shape", defaultValue.name());
        try {
            return AreaShape.valueOf(shapeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /**
     * 範囲を取得します
     *
     * @param level スキルレベル
     * @param defaultValue デフォルト値
     * @return 範囲
     */
    protected double getRange(int level, double defaultValue) {
        double base = settings.getDouble("range", defaultValue);
        double perLevel = settings.getDouble("range_per_level", 0.0);
        return base + (perLevel * (level - 1));
    }

    /**
     * 最大ターゲット数を取得します
     *
     * @param level スキルレベル
     * @param defaultValue デフォルト値
     * @return 最大ターゲット数
     */
    protected int getMaxTargets(int level, int defaultValue) {
        int base = settings.getInt("max_targets", defaultValue);
        int perLevel = settings.getInt("max_targets_per_level", 0);
        return base + (perLevel * (level - 1));
    }

    /**
     * 角度を取得します（コーン/扇状用）
     *
     * @param level スキルレベル
     * @param defaultValue デフォルト値
     * @return 角度（度数法）
     */
    protected double getAngle(int level, double defaultValue) {
        double base = settings.getDouble("angle", defaultValue);
        double perLevel = settings.getDouble("angle_per_level", 0.0);
        return base + (perLevel * (level - 1));
    }

    /**
     * 半径を取得します（球形/円形用）
     *
     * @param level スキルレベル
     * @param defaultValue デフォルト値
     * @return 半径
     */
    protected double getRadius(int level, double defaultValue) {
        double base = settings.getDouble("radius", defaultValue);
        double perLevel = settings.getDouble("radius_per_level", 0.0);
        return base + (perLevel * (level - 1));
    }

    /**
     * 長さを取得します（直線用）
     *
     * @param level スキルレベル
     * @param defaultValue デフォルト値
     * @return 長さ
     */
    protected double getLength(int level, double defaultValue) {
        double base = settings.getDouble("length", defaultValue);
        double perLevel = settings.getDouble("length_per_level", 0.0);
        return base + (perLevel * (level - 1));
    }

    /**
     * 幅を取得します（直線用）
     *
     * @param level スキルレベル
     * @param defaultValue デフォルト値
     * @return 幅
     */
    protected double getWidth(int level, double defaultValue) {
        double base = settings.getDouble("width", defaultValue);
        double perLevel = settings.getDouble("width_per_level", 0.0);
        return base + (perLevel * (level - 1));
    }

    /**
     * 近くのエンティティを取得します
     *
     * @param caster 発動者
     * @param range 範囲
     * @return 近くのエンティティリスト
     */
    protected List<LivingEntity> getNearbyEntities(LivingEntity caster, double range) {
        List<LivingEntity> entities = new ArrayList<>();
        if (caster == null || !caster.isValid()) {
            return entities;
        }

        for (org.bukkit.entity.Entity entity : caster.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity && entity.isValid()) {
                entities.add((LivingEntity) entity);
            }
        }

        return entities;
    }

    /**
     * 敵対的エンティティをフィルタリングします
     *
     * @param caster 発動者
     * @param entities エンティティリスト
     * @return 敵対的エンティティリスト
     */
    protected List<LivingEntity> filterHostile(LivingEntity caster, List<LivingEntity> entities) {
        List<LivingEntity> hostile = new ArrayList<>();

        for (LivingEntity entity : entities) {
            // プレイヤーは敵対的とみなさない（PvP設定による）
            if (entity instanceof Player) {
                continue;
            }
            // その他のMobは敵対的とみなす
            hostile.add(entity);
        }

        return hostile;
    }

    /**
     * 最も近いエンティティを取得します
     *
     * @param caster 基準エンティティ
     * @param entities 検索対象エンティティリスト
     * @return 最も近いエンティティ、見つからない場合はnull
     */
    protected LivingEntity getNearestEntity(LivingEntity caster, List<LivingEntity> entities) {
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            if (!entity.isValid() || entity.equals(caster)) {
                continue;
            }

            double distance = caster.getLocation().distance(entity.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = entity;
            }
        }

        return nearest;
    }

    /**
     * 指定された数だけターゲットを制限します
     *
     * @param targets ターゲットリスト
     * @param maxTargets 最大ターゲット数
     * @return 制限されたターゲットリスト
     */
    protected List<LivingEntity> limitTargets(List<LivingEntity> targets, int maxTargets) {
        if (targets.size() <= maxTargets) {
            return targets;
        }

        // 距離順にソートして近いものから取得
        List<LivingEntity> sorted = new ArrayList<>(targets);
        sorted.sort((a, b) -> {
            // 簡易的にリスト順（実際は距離ソートが望ましい）
            return 0;
        });

        return sorted.subList(0, maxTargets);
    }
}
