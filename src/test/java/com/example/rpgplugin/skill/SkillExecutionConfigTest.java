package com.example.rpgplugin.skill;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SkillExecutionConfigの単体テスト
 *
 * <p>スキル実行設定クラスの機能テスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("SkillExecutionConfig テスト")
@ExtendWith(MockitoExtension.class)
class SkillExecutionConfigTest {

    @Mock
    private Entity mockEntity;

    @Mock
    private World mockWorld;

    @Nested
    @DisplayName("RangeConfig 内部クラス")
    class RangeConfigTests {

        @Test
        @DisplayName("コンストラクタ: 正しく初期化されること")
        void testConstructor() {
            SkillExecutionConfig.RangeConfig config =
                    new SkillExecutionConfig.RangeConfig(10.0, 5.0, 15.0, false, 5);

            assertEquals(10.0, config.getRangeX(), 0.001, "rangeXが設定されること");
            assertEquals(5.0, config.getRangeY(), 0.001, "rangeYが設定されること");
            assertEquals(15.0, config.getRangeZ(), 0.001, "rangeZが設定されること");
            assertFalse(config.isSpherical(), "sphericalがfalseであること");
            assertEquals(5, config.getMaxTargets(), "maxTargetsが設定されること");
        }

        @Test
        @DisplayName("box: 直方体範囲を作成できること")
        void testBox() {
            SkillExecutionConfig.RangeConfig config = SkillExecutionConfig.RangeConfig.box(10.0, 5.0, 15.0);

            assertEquals(10.0, config.getRangeX(), 0.001, "rangeXが設定されること");
            assertEquals(5.0, config.getRangeY(), 0.001, "rangeYが設定されること");
            assertEquals(15.0, config.getRangeZ(), 0.001, "rangeZが設定されること");
            assertFalse(config.isSpherical(), "直方体であること");
            assertEquals(0, config.getMaxTargets(), "maxTargetsが0であること");
        }

        @Test
        @DisplayName("sphere: 球形範囲を作成できること")
        void testSphere() {
            SkillExecutionConfig.RangeConfig config = SkillExecutionConfig.RangeConfig.sphere(10.0);

            assertEquals(10.0, config.getRangeX(), 0.001, "rangeXが半径と同じであること");
            assertEquals(10.0, config.getRangeY(), 0.001, "rangeYが半径と同じであること");
            assertEquals(10.0, config.getRangeZ(), 0.001, "rangeZが半径と同じであること");
            assertTrue(config.isSpherical(), "球形であること");
            assertEquals(0, config.getMaxTargets(), "maxTargetsが0であること");
        }

        @Test
        @DisplayName("sphereWithLimit: ターゲット数制限付きの球形範囲を作成できること")
        void testSphereWithLimit() {
            SkillExecutionConfig.RangeConfig config = SkillExecutionConfig.RangeConfig.sphereWithLimit(10.0, 5);

            assertTrue(config.isSpherical(), "球形であること");
            assertEquals(5, config.getMaxTargets(), "maxTargetsが設定されること");
        }
    }

    @Nested
    @DisplayName("Builder 内部クラス")
    class BuilderTests {

        @Test
        @DisplayName("build: デフォルト設定が構築できること")
        void testBuild_Default() {
            SkillExecutionConfig.Builder builder = new SkillExecutionConfig.Builder();
            SkillExecutionConfig config = builder.build();

            assertNotNull(config, "設定が構築されること");
            assertTrue(config.shouldConsumeCost(), "デフォルトでコスト消費が有効であること");
            assertTrue(config.shouldApplyCooldown(), "デフォルトでクールダウン適用が有効であること");
            assertTrue(config.shouldApplyDamage(), "デフォルトでダメージ適用が有効であること");
            assertNull(config.getTargetEntity(), "ターゲットエンティティはnullであること");
            assertNull(config.getTargetLocation(), "ターゲットロケーションはnullであること");
            assertNull(config.getCostType(), "コストタイプはnullであること");
            assertNull(config.getCustomVariables(), "カスタム変数はnullであること");
            assertNull(config.getRangeConfig(), "範囲設定はnullであること");
        }

        @Test
        @DisplayName("build: 全設定を構築できること")
        void testBuild_AllSettings() {
            Location location = new Location(mockWorld, 100, 64, 200);
            Map<String, Double> variables = Map.of("bonus", 5.0);
            SkillExecutionConfig.RangeConfig rangeConfig = SkillExecutionConfig.RangeConfig.box(10.0, 10.0, 10.0);

            SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                    .targetEntity(mockEntity)
                    .targetLocation(location)
                    .costType(SkillCostType.HP)
                    .customVariables(variables)
                    .rangeConfig(rangeConfig)
                    .consumeCost(false)
                    .applyCooldown(false)
                    .applyDamage(false)
                    .build();

            assertEquals(mockEntity, config.getTargetEntity(), "ターゲットエンティティが設定されること");
            assertEquals(location, config.getTargetLocation(), "ターゲットロケーションが設定されること");
            assertEquals(SkillCostType.HP, config.getCostType(), "コストタイプが設定されること");
            assertEquals(variables, config.getCustomVariables(), "カスタム変数が設定されること");
            assertEquals(rangeConfig, config.getRangeConfig(), "範囲設定が設定されること");
            assertFalse(config.shouldConsumeCost(), "コスト消費が無効であること");
            assertFalse(config.shouldApplyCooldown(), "クールダウン適用が無効であること");
            assertFalse(config.shouldApplyDamage(), "ダメージ適用が無効であること");
        }

        @Test
        @DisplayName("targetEntity: メソッドチェーンが動作すること")
        void testChainedSetting() {
            SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                    .targetEntity(mockEntity)
                    .build();

            assertEquals(mockEntity, config.getTargetEntity(), "ターゲットが設定されること");
        }

        @Test
        @DisplayName("consumeCost: falseを設定できること")
        void testConsumeCost_False() {
            SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                    .consumeCost(false)
                    .build();

            assertFalse(config.shouldConsumeCost(), "コスト消費が無効であること");
        }

        @Test
        @DisplayName("applyCooldown: falseを設定できること")
        void testApplyCooldown_False() {
            SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                    .applyCooldown(false)
                    .build();

            assertFalse(config.shouldApplyCooldown(), "クールダウン適用が無効であること");
        }

        @Test
        @DisplayName("applyDamage: falseを設定できること")
        void testApplyDamage_False() {
            SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                    .applyDamage(false)
                    .build();

            assertFalse(config.shouldApplyDamage(), "ダメージ適用が無効であること");
        }
    }

    @Nested
    @DisplayName("SkillExecutionConfig メソッド")
    class ConfigTests {

        @Test
        @DisplayName("createDefault: デフォルト設定が作成できること")
        void testCreateDefault() {
            SkillExecutionConfig config = SkillExecutionConfig.createDefault();

            assertNotNull(config, "デフォルト設定が作成されること");
        }

        @Test
        @DisplayName("getter: 各フィールドが取得できること")
        void testGetters() {
            Location location = new Location(mockWorld, 0, 64, 0);
            Map<String, Double> variables = Map.of("test", 1.0);
            SkillExecutionConfig.RangeConfig rangeConfig = SkillExecutionConfig.RangeConfig.sphere(5.0);

            SkillExecutionConfig config = new SkillExecutionConfig.Builder()
                    .targetEntity(mockEntity)
                    .targetLocation(location)
                    .costType(SkillCostType.HP)
                    .customVariables(variables)
                    .rangeConfig(rangeConfig)
                    .build();

            assertEquals(mockEntity, config.getTargetEntity());
            assertEquals(location, config.getTargetLocation());
            assertEquals(SkillCostType.HP, config.getCostType());
            assertEquals(variables, config.getCustomVariables());
            assertEquals(rangeConfig, config.getRangeConfig());
        }
    }
}
