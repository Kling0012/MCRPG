package com.example.rpgplugin.skill;

import org.bukkit.entity.Entity;
import org.bukkit.Location;

import java.util.Map;

/**
 * スキル実行設定
 *
 * <p>スキル実行時のオプションを設定します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: 実行設定の表現に専念</li>
 *   <li>Builder: 柔軟な設定構築</li>
 *   <li>Immutable: 不変オブジェクト</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillExecutionConfig {

    /** ターゲットエンティティ（nullの場合は自動選択） */
    private final Entity targetEntity;

    /** ターゲットロケーション（nullの場合はプレイヤー位置） */
    private final Location targetLocation;

    /** コストタイプ（nullの場合はスキル設定を使用） */
    private final SkillCostType costType;

    /** カスタム変数（数式評価用） */
    private final Map<String, Double> customVariables;

    /** 範囲設定（nullの場合はスキル設定を使用） */
    private final RangeConfig rangeConfig;

    /** コストを消費するか */
    private final boolean consumeCost;

    /** クールダウンを適用するか */
    private final boolean applyCooldown;

    /** ダメージを適用するか */
    private final boolean applyDamage;

    /**
     * 範囲設定
     */
    public static class RangeConfig {
        private final double rangeX;
        private final double rangeY;
        private final double rangeZ;
        private final boolean spherical;
        private final int maxTargets;

        /**
         * コンストラクタ
         *
         * @param rangeX X方向範囲
         * @param rangeY Y方向範囲
         * @param rangeZ Z方向範囲
         * @param spherical 球形範囲かどうか
         * @param maxTargets 最大ターゲット数（0以下で無制限）
         */
        public RangeConfig(double rangeX, double rangeY, double rangeZ,
                           boolean spherical, int maxTargets) {
            this.rangeX = rangeX;
            this.rangeY = rangeY;
            this.rangeZ = rangeZ;
            this.spherical = spherical;
            this.maxTargets = maxTargets;
        }

        /**
         * 直方体範囲を作成します
         *
         * @param rangeX X方向範囲
         * @param rangeY Y方向範囲
         * @param rangeZ Z方向範囲
         * @return 範囲設定
         */
        public static RangeConfig box(double rangeX, double rangeY, double rangeZ) {
            return new RangeConfig(rangeX, rangeY, rangeZ, false, 0);
        }

        /**
         * 球形範囲を作成します
         *
         * @param radius 半径
         * @return 範囲設定
         */
        public static RangeConfig sphere(double radius) {
            return new RangeConfig(radius, radius, radius, true, 0);
        }

        /**
         * ターゲット数制限付きの球形範囲を作成します
         *
         * @param radius 半径
         * @param maxTargets 最大ターゲット数
         * @return 範囲設定
         */
        public static RangeConfig sphereWithLimit(double radius, int maxTargets) {
            return new RangeConfig(radius, radius, radius, true, maxTargets);
        }

        public double getRangeX() {
            return rangeX;
        }

        public double getRangeY() {
            return rangeY;
        }

        public double getRangeZ() {
            return rangeZ;
        }

        public boolean isSpherical() {
            return spherical;
        }

        public int getMaxTargets() {
            return maxTargets;
        }
    }

    /**
     * コンストラクタ
     *
     * @param targetEntity ターゲットエンティティ
     * @param targetLocation ターゲットロケーション
     * @param costType コストタイプ
     * @param customVariables カスタム変数
     * @param rangeConfig 範囲設定
     * @param consumeCost コスト消費フラグ
     * @param applyCooldown クールダウン適用フラグ
     * @param applyDamage ダメージ適用フラグ
     */
    private SkillExecutionConfig(Entity targetEntity, Location targetLocation,
                                  SkillCostType costType, Map<String, Double> customVariables,
                                  RangeConfig rangeConfig, boolean consumeCost,
                                  boolean applyCooldown, boolean applyDamage) {
        this.targetEntity = targetEntity;
        this.targetLocation = targetLocation;
        this.costType = costType;
        this.customVariables = customVariables;
        this.rangeConfig = rangeConfig;
        this.consumeCost = consumeCost;
        this.applyCooldown = applyCooldown;
        this.applyDamage = applyDamage;
    }

    /**
     * デフォルト設定を作成します
     *
     * @return デフォルト設定
     */
    public static SkillExecutionConfig createDefault() {
        return new Builder().build();
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }

    public Location getTargetLocation() {
        return targetLocation;
    }

    public SkillCostType getCostType() {
        return costType;
    }

    public Map<String, Double> getCustomVariables() {
        return customVariables;
    }

    public RangeConfig getRangeConfig() {
        return rangeConfig;
    }

    public boolean shouldConsumeCost() {
        return consumeCost;
    }

    public boolean shouldApplyCooldown() {
        return applyCooldown;
    }

    public boolean shouldApplyDamage() {
        return applyDamage;
    }

    /**
     * ビルダークラス
     */
    public static class Builder {
        private Entity targetEntity;
        private Location targetLocation;
        private SkillCostType costType;
        private Map<String, Double> customVariables;
        private RangeConfig rangeConfig;
        private boolean consumeCost = true;
        private boolean applyCooldown = true;
        private boolean applyDamage = true;

        /**
         * ターゲットエンティティを設定します
         *
         * @param entity ターゲットエンティティ
         * @return このビルダー
         */
        public Builder targetEntity(Entity entity) {
            this.targetEntity = entity;
            return this;
        }

        /**
         * ターゲットロケーションを設定します
         *
         * @param location ターゲットロケーション
         * @return このビルダー
         */
        public Builder targetLocation(Location location) {
            this.targetLocation = location;
            return this;
        }

        /**
         * コストタイプを設定します
         *
         * @param costType コストタイプ
         * @return このビルダー
         */
        public Builder costType(SkillCostType costType) {
            this.costType = costType;
            return this;
        }

        /**
         * カスタム変数を設定します
         *
         * @param variables カスタム変数マップ
         * @return このビルダー
         */
        public Builder customVariables(Map<String, Double> variables) {
            this.customVariables = variables;
            return this;
        }

        /**
         * 範囲設定を設定します
         *
         * @param rangeConfig 範囲設定
         * @return このビルダー
         */
        public Builder rangeConfig(RangeConfig rangeConfig) {
            this.rangeConfig = rangeConfig;
            return this;
        }

        /**
         * コスト消費の有無を設定します
         *
         * @param consume コストを消費する場合はtrue
         * @return このビルダー
         */
        public Builder consumeCost(boolean consume) {
            this.consumeCost = consume;
            return this;
        }

        /**
         * クールダウン適用の有無を設定します
         *
         * @param apply クールダウンを適用する場合はtrue
         * @return このビルダー
         */
        public Builder applyCooldown(boolean apply) {
            this.applyCooldown = apply;
            return this;
        }

        /**
         * ダメージ適用の有無を設定します
         *
         * @param apply ダメージを適用する場合はtrue
         * @return このビルダー
         */
        public Builder applyDamage(boolean apply) {
            this.applyDamage = apply;
            return this;
        }

        /**
         * 設定を構築します
         *
         * @return 構築された設定
         */
        public SkillExecutionConfig build() {
            return new SkillExecutionConfig(
                    targetEntity,
                    targetLocation,
                    costType,
                    customVariables,
                    rangeConfig,
                    consumeCost,
                    applyCooldown,
                    applyDamage
            );
        }
    }
}
