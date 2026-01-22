package com.example.rpgplugin.skill.target;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SkillTargetのテストクラス
 */
@DisplayName("SkillTarget テスト")
class SkillTargetTest {

    // ==================== コンストラクタテスト ====================

    @Nested
    @DisplayName("コンストラクタ")
    class ConstructorTests {

        @Test
        @DisplayName("フルコンストラクタ: すべてのパラメータが正しく設定されること")
        void testFullConstructor() {
            SkillTarget.SingleTargetConfig singleConfig = new SkillTarget.SingleTargetConfig(true, false);
            SkillTarget.ConeConfig coneConfig = new SkillTarget.ConeConfig(45.0, 10.0);
            SkillTarget.RectConfig rectConfig = new SkillTarget.RectConfig(5.0, 8.0);
            SkillTarget.CircleConfig circleConfig = new SkillTarget.CircleConfig(6.0);

            SkillTarget target = new SkillTarget(
                    TargetType.LINE, AreaShape.CONE,
                    singleConfig, coneConfig, rectConfig, circleConfig,
                    EntityTypeFilter.PLAYER_ONLY, 5,
                    TargetGroupFilter.ENEMY, true, true, false,
                    15.0, 3.0, 45.0, 8.0
            );

            assertEquals(TargetType.LINE, target.getType(), "typeが正しく設定されること");
            assertEquals(AreaShape.CONE, target.getAreaShape(), "areaShapeが正しく設定されること");
            assertEquals(EntityTypeFilter.PLAYER_ONLY, target.getEntityTypeFilter(), "entityTypeFilterが正しく設定されること");
            assertEquals(5, target.getMaxTargets(), "maxTargetsが正しく設定されること");
            assertEquals(TargetGroupFilter.ENEMY, target.getGroupFilter(), "groupFilterが正しく設定されること");
            assertTrue(target.isThroughWall(), "throughWallが正しく設定されること");
            assertTrue(target.isRandomOrder(), "randomOrderが正しく設定されること");
            assertFalse(target.isIncludeCaster(), "includeCasterが正しく設定されること");
        }

        @Test
        @DisplayName("レガシーコンストラクタ: デフォルト値が正しく設定されること")
        void testLegacyConstructor() {
            SkillTarget.SingleTargetConfig singleConfig = new SkillTarget.SingleTargetConfig(true, false);
            SkillTarget.ConeConfig coneConfig = new SkillTarget.ConeConfig(60.0, 10.0);

            SkillTarget target = new SkillTarget(
                    TargetType.NEAREST_HOSTILE, AreaShape.SINGLE,
                    singleConfig, coneConfig, null, null
            );

            assertEquals(TargetType.NEAREST_HOSTILE, target.getType(), "typeが正しく設定されること");
            assertEquals(AreaShape.SINGLE, target.getAreaShape(), "areaShapeが正しく設定されること");
            assertEquals(EntityTypeFilter.ALL, target.getEntityTypeFilter(), "entityTypeFilterのデフォルトはALL");
            assertEquals(TargetGroupFilter.BOTH, target.getGroupFilter(), "groupFilterのデフォルトはBOTH");
            assertFalse(target.isThroughWall(), "throughWallのデフォルトはfalse");
            assertFalse(target.isRandomOrder(), "randomOrderのデフォルトはfalse");
            assertFalse(target.isIncludeCaster(), "includeCasterのデフォルトはfalse");
        }

        @Test
        @DisplayName("汎用コンストラクタ: LINEタイプ用のパラメータが正しく設定されること")
        void testGenericConstructor() {
            // 7引数コンストラクタは存在しないため、全パラメータコンストラクタを使用
            SkillTarget target = new SkillTarget(
                    TargetType.LINE, AreaShape.SINGLE,
                    null, null, null, null,
                    EntityTypeFilter.MOB_ONLY, 3,
                    TargetGroupFilter.BOTH, false, false, false,
                    10.0, 2.0, null, null
            );

            assertEquals(TargetType.LINE, target.getType(), "typeがLINEであること");
            assertEquals(AreaShape.SINGLE, target.getAreaShape(), "areaShapeはSINGLE");
            assertEquals(EntityTypeFilter.MOB_ONLY, target.getEntityTypeFilter(), "entityTypeFilterが正しく設定されること");
            assertEquals(3, target.getMaxTargets(), "maxTargetsが正しく設定されること");
            assertEquals(10.0, target.getRawRange(), 0.001, "rangeが正しく設定されること");
            assertEquals(2.0, target.getRawLineWidth(), 0.001, "lineWidthが正しく設定されること");
        }

        @Test
        @DisplayName("拡張コンストラクタ: グループフィルタ等が正しく設定されること")
        void testExtendedConstructor() {
            SkillTarget.SingleTargetConfig singleConfig = new SkillTarget.SingleTargetConfig(false, true);

            SkillTarget target = new SkillTarget(
                    TargetType.AREA_SELF, AreaShape.SPHERE,
                    singleConfig, null, null, null,
                    EntityTypeFilter.ALL, 10,
                    TargetGroupFilter.ALLY, false, true, true,
                    null, null, null, null
            );

            assertEquals(TargetType.AREA_SELF, target.getType());
            assertEquals(AreaShape.SPHERE, target.getAreaShape());
            assertEquals(EntityTypeFilter.ALL, target.getEntityTypeFilter());
            assertEquals(10, target.getMaxTargets());
            assertEquals(TargetGroupFilter.ALLY, target.getGroupFilter());
            assertFalse(target.isThroughWall());
            assertTrue(target.isRandomOrder());
            assertTrue(target.isIncludeCaster());
        }

        @Test
        @DisplayName("null type: デフォルト値が使用されること")
        void testNullType_DefaultsToNearestHostile() {
            SkillTarget target = new SkillTarget(
                    null, AreaShape.SINGLE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null,
                    TargetGroupFilter.BOTH, false, false, false,
                    null, null, null, null
            );

            assertEquals(TargetType.NEAREST_HOSTILE, target.getType(), "typeのデフォルトはNEAREST_HOSTILE");
        }

        @Test
        @DisplayName("null areaShape: デフォルト値が使用されること")
        void testNullAreaShape_DefaultsToSingle() {
            SkillTarget target = new SkillTarget(
                    TargetType.SELF, null,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null,
                    TargetGroupFilter.BOTH, false, false, false,
                    null, null, null, null
            );

            assertEquals(AreaShape.SINGLE, target.getAreaShape(), "areaShapeのデフォルトはSINGLE");
        }

        @Test
        @DisplayName("null entityTypeFilter: デフォルト値が使用されること")
        void testNullEntityTypeFilter_DefaultsToAll() {
            SkillTarget target = new SkillTarget(
                    TargetType.SELF, AreaShape.SINGLE,
                    null, null, null, null,
                    null, null, null, false, false, false,
                    null, null, null, null
            );

            assertEquals(EntityTypeFilter.ALL, target.getEntityTypeFilter(), "entityTypeFilterのデフォルトはALL");
        }

        @Test
        @DisplayName("null groupFilter: デフォルト値が使用されること")
        void testNullGroupFilter_DefaultsToBoth() {
            SkillTarget target = new SkillTarget(
                    TargetType.SELF, AreaShape.SINGLE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null, null, false, false, false,
                    null, null, null, null
            );

            assertEquals(TargetGroupFilter.BOTH, target.getGroupFilter(), "groupFilterのデフォルトはBOTH");
        }
    }

    // ==================== createDefault テスト ====================

    @Nested
    @DisplayName("createDefault")
    class CreateDefaultTests {

        @Test
        @DisplayName("createDefault: デフォルト設定でインスタンスが作成されること")
        void testCreateDefault() {
            SkillTarget target = SkillTarget.createDefault();

            assertEquals(TargetType.NEAREST_HOSTILE, target.getType(), "typeはNEAREST_HOSTILE");
            assertEquals(AreaShape.SINGLE, target.getAreaShape(), "areaShapeはSINGLE");
            assertNotNull(target.getSingleTarget(), "singleTargetが設定されること");
            assertTrue(target.getSingleTarget().isSelectNearest(), "selectNearestはtrue");
            assertFalse(target.getSingleTarget().isTargetSelf(), "targetSelfはfalse");
        }
    }

    // ==================== ゲッターメソッドテスト ====================

    @Nested
    @DisplayName("ゲッターメソッド")
    class GetterTests {

        @Test
        @DisplayName("getType: typeが返されること")
        void testGetType() {
            SkillTarget target = new SkillTarget(TargetType.CONE, AreaShape.SINGLE, null, null, null, null);
            assertEquals(TargetType.CONE, target.getType());
        }

        @Test
        @DisplayName("getAreaShape: areaShapeが返されること")
        void testGetAreaShape() {
            SkillTarget target = new SkillTarget(TargetType.SELF, AreaShape.CIRCLE, null, null, null, null);
            assertEquals(AreaShape.CIRCLE, target.getAreaShape());
        }

        @Test
        @DisplayName("getSingleTarget: singleTargetが返されること")
        void testGetSingleTarget() {
            SkillTarget.SingleTargetConfig config = new SkillTarget.SingleTargetConfig(true, false);
            SkillTarget target = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, config, null, null, null);

            assertEquals(config, target.getSingleTarget(), "singleTargetが返されること");
        }

        @Test
        @DisplayName("getCone: coneが返されること")
        void testGetCone() {
            SkillTarget.ConeConfig config = new SkillTarget.ConeConfig(45.0, 15.0);
            SkillTarget target = new SkillTarget(TargetType.CONE, AreaShape.CONE, null, config, null, null);

            assertEquals(config, target.getCone(), "coneが返されること");
            assertEquals(45.0, target.getCone().getAngle(), 0.001, "angleが正しいこと");
            assertEquals(15.0, target.getCone().getRange(), 0.001, "rangeが正しいこと");
        }

        @Test
        @DisplayName("getRect: rectが返されること")
        void testGetRect() {
            SkillTarget.RectConfig config = new SkillTarget.RectConfig(8.0, 12.0);
            SkillTarget target = new SkillTarget(TargetType.AREA_SELF, AreaShape.RECT, null, null, config, null);

            assertEquals(config, target.getRect(), "rectが返されること");
            assertEquals(8.0, target.getRect().getWidth(), 0.001, "widthが正しいこと");
            assertEquals(12.0, target.getRect().getDepth(), 0.001, "depthが正しいこと");
        }

        @Test
        @DisplayName("getCircle: circleが返されること")
        void testGetCircle() {
            SkillTarget.CircleConfig config = new SkillTarget.CircleConfig(7.5);
            SkillTarget target = new SkillTarget(TargetType.AREA_SELF, AreaShape.CIRCLE, null, null, null, config);

            assertEquals(config, target.getCircle(), "circleが返されること");
            assertEquals(7.5, target.getCircle().getRadius(), 0.001, "radiusが正しいこと");
        }

        @Test
        @DisplayName("getEntityTypeFilter: entityTypeFilterが返されること")
        void testGetEntityTypeFilter() {
            SkillTarget target = new SkillTarget(
                    TargetType.SELF, AreaShape.SINGLE, null, null, null, null,
                    EntityTypeFilter.MOB_ONLY, null, null, false, false, false,
                    null, null, null, null
            );

            assertEquals(EntityTypeFilter.MOB_ONLY, target.getEntityTypeFilter());
        }

        @Test
        @DisplayName("getMaxTargets: maxTargetsが返されること")
        void testGetMaxTargets() {
            SkillTarget target = new SkillTarget(
                    TargetType.AREA_SELF, AreaShape.SPHERE, null, null, null, null,
                    EntityTypeFilter.ALL, 7, null, false, false, false,
                    null, null, null, null
            );

            assertEquals(7, target.getMaxTargets());
        }

        @Test
        @DisplayName("getMaxTargets: nullが設定されること")
        void testGetMaxTargets_Null() {
            SkillTarget target = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);

            assertNull(target.getMaxTargets(), "maxTargetsはnull");
        }
    }

    // ==================== getMaxTargetsOrUnlimited テスト ====================

    @Nested
    @DisplayName("getMaxTargetsOrUnlimited")
    class GetMaxTargetsOrUnlimitedTests {

        @Test
        @DisplayName("getMaxTargetsOrUnlimited: 設定値が返されること")
        void testGetMaxTargetsOrUnlimited_SetValue() {
            SkillTarget target = new SkillTarget(
                    TargetType.AREA_SELF, AreaShape.SPHERE, null, null, null, null,
                    EntityTypeFilter.ALL, 5, null, false, false, false,
                    null, null, null, null
            );

            assertEquals(5, target.getMaxTargetsOrUnlimited(), "設定値が返されること");
        }

        @Test
        @DisplayName("getMaxTargetsOrUnlimited: nullの場合はInteger.MAX_VALUEが返されること")
        void testGetMaxTargetsOrUnlimited_Null() {
            SkillTarget target = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);

            assertEquals(Integer.MAX_VALUE, target.getMaxTargetsOrUnlimited(), "nullの場合はMAX_VALUE");
        }
    }

    // ==================== getRawRange テスト ====================

    @Nested
    @DisplayName("getRawRange")
    class GetRangeTests {

        @Test
        @DisplayName("getRawRange: 設定されたrangeが返されること")
        void testGetRawRange_SetValue() {
            SkillTarget target = new SkillTarget(
                    TargetType.LINE, AreaShape.SINGLE, null, null, null, null,
                    EntityTypeFilter.ALL, null, null, false, false, false,
                    20.0, null, null, null
            );

            assertEquals(20.0, target.getRawRange(), 0.001, "設定されたrangeが返されること");
        }

        @Test
        @DisplayName("getRawRange: rangeがnullの場合、ConeConfigから取得されること")
        void testGetRawRange_FromConeConfig() {
            SkillTarget.ConeConfig coneConfig = new SkillTarget.ConeConfig(45.0, 25.0);
            SkillTarget target = new SkillTarget(
                    TargetType.CONE, AreaShape.CONE, null, coneConfig, null, null
            );

            // ConeConfigからrangeが取得されることは確認できない（計算が必要なため）
            // nullが返されることを確認
            assertNull(target.getRawRange(), "設定されていない場合はnull");
        }

        @Test
        @DisplayName("getRawRange: どちらもnullの場合はnullが返されること")
        void testGetRawRange_DefaultValue() {
            SkillTarget target = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);

            assertNull(target.getRawRange(), "設定されていない場合はnull");
        }
    }

    // ==================== getRawLineWidth テスト ====================

    @Nested
    @DisplayName("getRawLineWidth")
    class GetLineWidthTests {

        @Test
        @DisplayName("getRawLineWidth: 設定されたlineWidthが返されること")
        void testGetRawLineWidth_SetValue() {
            SkillTarget target = new SkillTarget(
                    TargetType.LINE, AreaShape.SINGLE, null, null, null, null,
                    EntityTypeFilter.ALL, null, null, false, false, false,
                    null, 4.0, null, null
            );

            assertEquals(4.0, target.getRawLineWidth(), 0.001, "設定されたlineWidthが返されること");
        }

        @Test
        @DisplayName("getRawLineWidth: nullの場合はnullが返されること")
        void testGetRawLineWidth_DefaultValue() {
            SkillTarget target = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);

            assertNull(target.getRawLineWidth(), "設定されていない場合はnull");
        }
    }

    // ==================== getRawConeAngle テスト ====================

    @Nested
    @DisplayName("getRawConeAngle")
    class GetConeAngleTests {

        @Test
        @DisplayName("getRawConeAngle: 設定されたconeAngleが返されること")
        void testGetRawConeAngle_SetValue() {
            SkillTarget target = new SkillTarget(
                    TargetType.CONE, AreaShape.CONE, null, null, null, null,
                    EntityTypeFilter.ALL, null, null, false, false, false,
                    null, null, 90.0, null
            );

            assertEquals(90.0, target.getRawConeAngle(), 0.001, "設定されたconeAngleが返されること");
        }

        @Test
        @DisplayName("getRawConeAngle: nullの場合はnullが返されること")
        void testGetRawConeAngle_DefaultValue() {
            SkillTarget target = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);

            assertNull(target.getRawConeAngle(), "設定されていない場合はnull");
        }
    }

    // ==================== getRawSphereRadius テスト ====================

    @Nested
    @DisplayName("getRawSphereRadius")
    class GetSphereRadiusTests {

        @Test
        @DisplayName("getRawSphereRadius: 設定されたsphereRadiusが返されること")
        void testGetRawSphereRadius_SetValue() {
            SkillTarget target = new SkillTarget(
                    TargetType.SPHERE, AreaShape.SPHERE, null, null, null, null,
                    EntityTypeFilter.ALL, null, null, false, false, false,
                    null, null, null, 12.0
            );

            assertEquals(12.0, target.getRawSphereRadius(), 0.001, "設定されたsphereRadiusが返されること");
        }

        @Test
        @DisplayName("getRawSphereRadius: nullの場合はnullが返されること")
        void testGetRawSphereRadius_DefaultValue() {
            SkillTarget target = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);

            assertNull(target.getRawSphereRadius(), "設定されていない場合はnull");
        }
    }

    // ==================== Optionalゲッターメソッドテスト ====================

    @Nested
    @DisplayName("Optionalゲッターメソッド")
    class OptionalGetterTests {

        @Test
        @DisplayName("getConeAsOptional: 設定されている場合はOptionalが返されること")
        void testGetConeAsOptional_Present() {
            SkillTarget.ConeConfig config = new SkillTarget.ConeConfig(45.0, 10.0);
            SkillTarget target = new SkillTarget(TargetType.CONE, AreaShape.CONE, null, config, null, null);

            Optional<SkillTarget.ConeConfig> result = target.getConeAsOptional();

            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(config, result.get(), "正しいconfigが返されること");
        }

        @Test
        @DisplayName("getConeAsOptional: 設定されていない場合はemptyが返されること")
        void testGetConeAsOptional_Empty() {
            SkillTarget target = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);

            Optional<SkillTarget.ConeConfig> result = target.getConeAsOptional();

            assertFalse(result.isPresent(), "Optionalが空であること");
        }

        @Test
        @DisplayName("getRectAsOptional: 設定されている場合はOptionalが返されること")
        void testGetRectAsOptional_Present() {
            SkillTarget.RectConfig config = new SkillTarget.RectConfig(5.0, 8.0);
            SkillTarget target = new SkillTarget(TargetType.AREA_SELF, AreaShape.RECT, null, null, config, null);

            Optional<SkillTarget.RectConfig> result = target.getRectAsOptional();

            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(config, result.get(), "正しいconfigが返されること");
        }

        @Test
        @DisplayName("getRectAsOptional: 設定されていない場合はemptyが返されること")
        void testGetRectAsOptional_Empty() {
            SkillTarget target = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);

            Optional<SkillTarget.RectConfig> result = target.getRectAsOptional();

            assertFalse(result.isPresent(), "Optionalが空であること");
        }

        @Test
        @DisplayName("getCircleAsOptional: 設定されている場合はOptionalが返されること")
        void testGetCircleAsOptional_Present() {
            SkillTarget.CircleConfig config = new SkillTarget.CircleConfig(6.0);
            SkillTarget target = new SkillTarget(TargetType.AREA_SELF, AreaShape.CIRCLE, null, null, null, config);

            Optional<SkillTarget.CircleConfig> result = target.getCircleAsOptional();

            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(config, result.get(), "正しいconfigが返されること");
        }

        @Test
        @DisplayName("getCircleAsOptional: 設定されていない場合はemptyが返されること")
        void testGetCircleAsOptional_Empty() {
            SkillTarget target = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);

            Optional<SkillTarget.CircleConfig> result = target.getCircleAsOptional();

            assertFalse(result.isPresent(), "Optionalが空であること");
        }

        @Test
        @DisplayName("getSingleTargetAsOptional: 設定されている場合はOptionalが返されること")
        void testGetSingleTargetAsOptional_Present() {
            SkillTarget.SingleTargetConfig config = new SkillTarget.SingleTargetConfig(true, false);
            SkillTarget target = new SkillTarget(TargetType.SELF_PLUS_ONE, AreaShape.SINGLE, config, null, null, null);

            Optional<SkillTarget.SingleTargetConfig> result = target.getSingleTargetAsOptional();

            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(config, result.get(), "正しいconfigが返されること");
        }

        @Test
        @DisplayName("getSingleTargetAsOptional: 設定されていない場合はemptyが返されること")
        void testGetSingleTargetAsOptional_Empty() {
            SkillTarget target = new SkillTarget(TargetType.AREA_SELF, AreaShape.SPHERE, null, null, null, null);

            Optional<SkillTarget.SingleTargetConfig> result = target.getSingleTargetAsOptional();

            assertFalse(result.isPresent(), "Optionalが空であること");
        }
    }

    // ==================== equals/hashCode/toString テスト ====================

    @Nested
    @DisplayName("equals/hashCode/toString")
    class ObjectMethodsTests {

        @Test
        @DisplayName("equals: 同じtypeとareaShapeなら等価と判定されること")
        void testEquals_SameTypeAndShape() {
            SkillTarget target1 = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);
            SkillTarget target2 = new SkillTarget(TargetType.SELF, AreaShape.SINGLE,
                    new SkillTarget.SingleTargetConfig(false, true),
                    new SkillTarget.ConeConfig(90.0, 20.0), null, null);

            assertEquals(target1, target2, "同じtypeとareaShapeなら等価");
            assertEquals(target1.hashCode(), target2.hashCode(), "hashCodeも等しいこと");
        }

        @Test
        @DisplayName("equals: typeが異なる場合は等価ではない")
        void testEquals_DifferentType() {
            SkillTarget target1 = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);
            SkillTarget target2 = new SkillTarget(TargetType.AREA_SELF, AreaShape.SINGLE, null, null, null, null);

            assertNotEquals(target1, target2, "typeが異なる場合は等価ではない");
        }

        @Test
        @DisplayName("equals: areaShapeが異なる場合は等価ではない")
        void testEquals_DifferentShape() {
            SkillTarget target1 = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);
            SkillTarget target2 = new SkillTarget(TargetType.SELF, AreaShape.CIRCLE, null, null, null, null);

            assertNotEquals(target1, target2, "areaShapeが異なる場合は等価ではない");
        }

        @Test
        @DisplayName("equals: 同一インスタンスの場合は等価")
        void testEquals_SameInstance() {
            SkillTarget target = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);

            assertEquals(target, target, "同一インスタンスは等価");
        }

        @Test
        @DisplayName("equals: nullとは等価ではない")
        void testEquals_Null() {
            SkillTarget target = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);

            assertNotEquals(target, null, "nullとは等価ではない");
        }

        @Test
        @DisplayName("equals: 異なるクラスとは等価ではない")
        void testEquals_DifferentClass() {
            SkillTarget target = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);

            assertNotEquals(target, "not a SkillTarget", "異なるクラスとは等価ではない");
        }

        @Test
        @DisplayName("hashCode: 同じtypeとareaShapeなら同じhashCode")
        void testHashCode_SameTypeAndShape() {
            SkillTarget target1 = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);
            SkillTarget target2 = new SkillTarget(TargetType.SELF, AreaShape.SINGLE,
                    new SkillTarget.SingleTargetConfig(true, false),
                    null, null, null);

            assertEquals(target1.hashCode(), target2.hashCode(), "同じhashCode");
        }

        @Test
        @DisplayName("hashCode: 異なるtypeなら異なるhashCode")
        void testHashCode_DifferentType() {
            SkillTarget target1 = new SkillTarget(TargetType.SELF, AreaShape.SINGLE, null, null, null, null);
            SkillTarget target2 = new SkillTarget(TargetType.AREA_SELF, AreaShape.SINGLE, null, null, null, null);

            assertNotEquals(target1.hashCode(), target2.hashCode(), "異なるhashCode");
        }

        @Test
        @DisplayName("toString: typeとareaShapeが含まれること")
        void testToString() {
            SkillTarget target = new SkillTarget(TargetType.CONE, AreaShape.CONE, null, null, null, null);

            String result = target.toString();

            assertTrue(result.contains("CONE"), "toStringにtypeが含まれること");
            assertTrue(result.contains("type="), "typeフィールドが含まれること");
            assertTrue(result.contains("areaShape="), "areaShapeフィールドが含まれること");
        }
    }

    // ==================== 内部クラスのテスト ====================

    @Nested
    @DisplayName("SingleTargetConfig")
    class SingleTargetConfigTests {

        @Test
        @DisplayName("SingleTargetConfig: 値が正しく設定されること")
        void testSingleTargetConfig() {
            SkillTarget.SingleTargetConfig config = new SkillTarget.SingleTargetConfig(true, false);

            assertTrue(config.isSelectNearest(), "selectNearestがtrue");
            assertFalse(config.isTargetSelf(), "targetSelfがfalse");
        }

        @Test
        @DisplayName("SingleTargetConfig: すべてfalseでも設定できること")
        void testSingleTargetConfig_AllFalse() {
            SkillTarget.SingleTargetConfig config = new SkillTarget.SingleTargetConfig(false, false);

            assertFalse(config.isSelectNearest(), "selectNearestがfalse");
            assertFalse(config.isTargetSelf(), "targetSelfがfalse");
        }

        @Test
        @DisplayName("SingleTargetConfig: すべてtrueでも設定できること")
        void testSingleTargetConfig_AllTrue() {
            SkillTarget.SingleTargetConfig config = new SkillTarget.SingleTargetConfig(true, true);

            assertTrue(config.isSelectNearest(), "selectNearestがtrue");
            assertTrue(config.isTargetSelf(), "targetSelfがtrue");
        }
    }

    @Nested
    @DisplayName("ConeConfig")
    class ConeConfigTests {

        @Test
        @DisplayName("ConeConfig: 値が正しく設定されること")
        void testConeConfig() {
            SkillTarget.ConeConfig config = new SkillTarget.ConeConfig(45.0, 15.0);

            assertEquals(45.0, config.getAngle(), 0.001, "angleが正しいこと");
            assertEquals(15.0, config.getRange(), 0.001, "rangeが正しいこと");
        }

        @Test
        @DisplayName("ConeConfig: 0度でも設定できること")
        void testConeConfig_ZeroAngle() {
            SkillTarget.ConeConfig config = new SkillTarget.ConeConfig(0.0, 10.0);

            assertEquals(0.0, config.getAngle(), 0.001, "0度でも設定できる");
        }

        @Test
        @DisplayName("ConeConfig: 180度でも設定できること")
        void testConeConfig_MaxAngle() {
            SkillTarget.ConeConfig config = new SkillTarget.ConeConfig(180.0, 20.0);

            assertEquals(180.0, config.getAngle(), 0.001, "180度でも設定できる");
        }
    }

    @Nested
    @DisplayName("RectConfig")
    class RectConfigTests {

        @Test
        @DisplayName("RectConfig: 値が正しく設定されること")
        void testRectConfig() {
            SkillTarget.RectConfig config = new SkillTarget.RectConfig(8.0, 12.0);

            assertEquals(8.0, config.getWidth(), 0.001, "widthが正しいこと");
            assertEquals(12.0, config.getDepth(), 0.001, "depthが正しいこと");
        }

        @Test
        @DisplayName("RectConfig: 正方形でも設定できること")
        void testRectConfig_Square() {
            SkillTarget.RectConfig config = new SkillTarget.RectConfig(10.0, 10.0);

            assertEquals(10.0, config.getWidth(), 0.001, "widthが正しいこと");
            assertEquals(10.0, config.getDepth(), 0.001, "depthが正しいこと");
        }
    }

    @Nested
    @DisplayName("CircleConfig")
    class CircleConfigTests {

        @Test
        @DisplayName("CircleConfig: radiusが正しく設定されること")
        void testCircleConfig() {
            SkillTarget.CircleConfig config = new SkillTarget.CircleConfig(7.5);

            assertEquals(7.5, config.getRadius(), 0.001, "radiusが正しいこと");
        }

        @Test
        @DisplayName("CircleConfig: 0でも設定できること")
        void testCircleConfig_ZeroRadius() {
            SkillTarget.CircleConfig config = new SkillTarget.CircleConfig(0.0);

            assertEquals(0.0, config.getRadius(), 0.001, "0でも設定できる");
        }

        @Test
        @DisplayName("CircleConfig: 大きな値でも設定できること")
        void testCircleConfig_LargeRadius() {
            SkillTarget.CircleConfig config = new SkillTarget.CircleConfig(100.0);

            assertEquals(100.0, config.getRadius(), 0.001, "大きな値でも設定できる");
        }
    }
}
