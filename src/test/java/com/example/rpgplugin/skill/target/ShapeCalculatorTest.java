package com.example.rpgplugin.skill.target;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

/**
 * ShapeCalculatorのテストクラス
 */
@DisplayName("ShapeCalculator テスト")
@ExtendWith(MockitoExtension.class)
class ShapeCalculatorTest {

    @Mock
    private World mockWorld;

    @Mock
    private Entity mockEntity;

    @Mock
    private Entity mockEntity2;

    @Mock
    private Entity mockEntityFar;

    private Location origin;
    private Vector direction;

    @BeforeEach
    void setUp() {
        origin = new Location(mockWorld, 0, 64, 0);
        direction = new Vector(1, 0, 0); // X方向

        lenient().when(mockWorld.getName()).thenReturn("world");

        // 近いエンティティ
        lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 2, 64, 0));
        lenient().when(mockEntity.getWorld()).thenReturn(mockWorld);
        lenient().when(mockEntity.getUniqueId()).thenReturn(UUID.randomUUID());

        // 中距離エンティティ
        lenient().when(mockEntity2.getLocation()).thenReturn(new Location(mockWorld, 5, 64, 0));
        lenient().when(mockEntity2.getWorld()).thenReturn(mockWorld);
        lenient().when(mockEntity2.getUniqueId()).thenReturn(UUID.randomUUID());

        // 遠いエンティティ
        lenient().when(mockEntityFar.getLocation()).thenReturn(new Location(mockWorld, 20, 64, 0));
        lenient().when(mockEntityFar.getWorld()).thenReturn(mockWorld);
        lenient().when(mockEntityFar.getUniqueId()).thenReturn(UUID.randomUUID());
    }

    // ==================== 基本テスト ====================

    @Nested
    @DisplayName("基本動作")
    class BasicTests {

        @Test
        @DisplayName("isInRange: null entityの場合はfalse")
        void testIsInRange_NullEntity() {
            SkillTarget config = SkillTarget.createDefault();
            boolean result = ShapeCalculator.isInRange(null, origin, direction, AreaShape.SINGLE, config);

            assertFalse(result, "null entityの場合はfalse");
        }

        @Test
        @DisplayName("isInRange: null originの場合はfalse")
        void testIsInRange_NullOrigin() {
            SkillTarget config = SkillTarget.createDefault();
            boolean result = ShapeCalculator.isInRange(mockEntity, null, direction, AreaShape.SINGLE, config);

            assertFalse(result, "null originの場合はfalse");
        }

        @Test
        @DisplayName("isInRange: null shapeの場合はfalse")
        void testIsInRange_NullShape() {
            SkillTarget config = SkillTarget.createDefault();
            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, null, config);

            assertFalse(result, "null shapeの場合はfalse");
        }

        @Test
        @DisplayName("isInRange: entityのlocationがnullの場合はfalse")
        void testIsInRange_NullEntityLocation() {
            SkillTarget config = SkillTarget.createDefault();
            lenient().when(mockEntity.getLocation()).thenReturn(null);

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.SINGLE, config);

            assertFalse(result, "entityのlocationがnullの場合はfalse");
        }

        @Test
        @DisplayName("isInRange: 異なるワールドの場合はfalse")
        void testIsInRange_DifferentWorld() {
            World otherWorld = org.mockito.Mockito.mock(World.class);
            SkillTarget config = SkillTarget.createDefault();
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(otherWorld, 2, 64, 0));
            lenient().when(mockEntity.getWorld()).thenReturn(otherWorld);

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.SINGLE, config);

            assertFalse(result, "異なるワールドの場合はfalse");
        }

        @Test
        @DisplayName("isInRange: originのworldがnullの場合はfalse")
        void testIsInRange_NullOriginWorld() {
            Location originWithoutWorld = new Location(null, 0, 64, 0);
            SkillTarget config = SkillTarget.createDefault();

            boolean result = ShapeCalculator.isInRange(mockEntity, originWithoutWorld, direction, AreaShape.SINGLE, config);

            assertFalse(result, "originのworldがnullの場合はfalse");
        }
    }

    // ==================== SINGLE 形状テスト ====================

    @Nested
    @DisplayName("SINGLE 形状判定")
    class SingleShapeTests {

        @Test
        @DisplayName("SINGLE: 近接エンティティは範囲内")
        void testIsInRangeSingle_Nearby() {
            SkillTarget config = SkillTarget.createDefault();
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 1, 64, 0));

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.SINGLE, config);

            assertTrue(result, "近接エンティティは範囲内");
        }

        @Test
        @DisplayName("SINGLE: 距離1.5以内は範囲内")
        void testIsInRangeSingle_WithinDistance() {
            SkillTarget config = SkillTarget.createDefault();
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 1.5, 64, 0));

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.SINGLE, config);

            assertTrue(result, "距離1.5以内は範囲内");
        }

        @Test
        @DisplayName("SINGLE: 距離1.5以上は範囲外")
        void testIsInRangeSingle_OutOfRange() {
            SkillTarget config = SkillTarget.createDefault();
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 1.6, 64, 0));

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.SINGLE, config);

            assertFalse(result, "距離1.5以上は範囲外");
        }
    }

    // ==================== CONE 形状テスト ====================

    @Nested
    @DisplayName("CONE 形状判定")
    class ConeShapeTests {

        @Test
        @DisplayName("CONE: null configの場合はfalse")
        void testIsInRangeCone_NullConfig() {
            SkillTarget config = new SkillTarget(TargetType.CONE, AreaShape.CONE, null, null, null, null);

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.CONE, config);

            assertFalse(result, "null configの場合はfalse");
        }

        @Test
        @DisplayName("CONE: null directionの場合はfalse")
        void testIsInRangeCone_NullDirection() {
            SkillTarget.ConeConfig coneConfig = new SkillTarget.ConeConfig(45.0, 10.0);
            SkillTarget config = new SkillTarget(TargetType.CONE, AreaShape.CONE, null, coneConfig, null, null);

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, null, AreaShape.CONE, config);

            assertFalse(result, "null directionの場合はfalse");
        }

        @Test
        @DisplayName("CONE: 範囲内のエンティティは判定される")
        void testIsInRangeCone_WithinRange() {
            SkillTarget.ConeConfig coneConfig = new SkillTarget.ConeConfig(45.0, 10.0);
            SkillTarget config = new SkillTarget(TargetType.CONE, AreaShape.CONE, null, coneConfig, null, null);
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 5, 64, 0)); // 真正面

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.CONE, config);

            assertTrue(result, "範囲内のエンティティは判定される");
        }

        @Test
        @DisplayName("CONE: 範囲外のエンティティは判定されない")
        void testIsInRangeCone_OutOfRange() {
            SkillTarget.ConeConfig coneConfig = new SkillTarget.ConeConfig(45.0, 5.0);
            SkillTarget config = new SkillTarget(TargetType.CONE, AreaShape.CONE, null, coneConfig, null, null);
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 6, 64, 0)); // 範囲外

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.CONE, config);

            assertFalse(result, "範囲外のエンティティは判定されない");
        }

        @Test
        @DisplayName("CONE: 角度外のエンティティは判定されない")
        void testIsInRangeCone_OutOfAngle() {
            SkillTarget.ConeConfig coneConfig = new SkillTarget.ConeConfig(30.0, 10.0);
            SkillTarget config = new SkillTarget(TargetType.CONE, AreaShape.CONE, null, coneConfig, null, null);
            // 真横（90度）
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 0, 64, 5));

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.CONE, config);

            assertFalse(result, "角度外のエンティティは判定されない");
        }

        @Test
        @DisplayName("CONE: 近すぎるエンティティは除外される")
        void testIsInRangeCone_TooClose() {
            SkillTarget.ConeConfig coneConfig = new SkillTarget.ConeConfig(45.0, 10.0);
            SkillTarget config = new SkillTarget(TargetType.CONE, AreaShape.CONE, null, coneConfig, null, null);
            lenient().when(mockEntity.getLocation()).thenReturn(origin); // 同じ位置

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.CONE, config);

            assertFalse(result, "近すぎるエンティティは除外される");
        }
    }

    // ==================== RECT 形状テスト ====================

    @Nested
    @DisplayName("RECT 形状判定")
    class RectShapeTests {

        @Test
        @DisplayName("RECT: null configの場合はfalse")
        void testIsInRangeRect_NullConfig() {
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.RECT, null, null, null, null);

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.RECT, config);

            assertFalse(result, "null configの場合はfalse");
        }

        @Test
        @DisplayName("RECT: null directionの場合はfalse")
        void testIsInRangeRect_NullDirection() {
            SkillTarget.RectConfig rectConfig = new SkillTarget.RectConfig(5.0, 10.0);
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.RECT, null, null, rectConfig, null);

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, null, AreaShape.RECT, config);

            assertFalse(result, "null directionの場合はfalse");
        }

        @Test
        @DisplayName("RECT: 範囲内のエンティティは判定される")
        void testIsInRangeRect_WithinRange() {
            SkillTarget.RectConfig rectConfig = new SkillTarget.RectConfig(6.0, 10.0);
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.RECT, null, null, rectConfig, null);
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 3, 64, 0)); // 前方3ブロック

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.RECT, config);

            assertTrue(result, "範囲内のエンティティは判定される");
        }

        @Test
        @DisplayName("RECT: 幅外のエンティティは判定されない")
        void testIsInRangeRect_OutOfWidth() {
            SkillTarget.RectConfig rectConfig = new SkillTarget.RectConfig(4.0, 10.0);
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.RECT, null, null, rectConfig, null);
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 5, 64, 3)); // 横に外れている

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.RECT, config);

            assertFalse(result, "幅外のエンティティは判定されない");
        }

        @Test
        @DisplayName("RECT: 奥行き外のエンティティは判定されない")
        void testIsInRangeRect_OutOfDepth() {
            SkillTarget.RectConfig rectConfig = new SkillTarget.RectConfig(6.0, 5.0);
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.RECT, null, null, rectConfig, null);
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 6, 64, 0)); // 奥行き外

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.RECT, config);

            assertFalse(result, "奥行き外のエンティティは判定されない");
        }

        @Test
        @DisplayName("RECT: 後方のエンティティは判定されない")
        void testIsInRangeRect_Behind() {
            SkillTarget.RectConfig rectConfig = new SkillTarget.RectConfig(6.0, 10.0);
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.RECT, null, null, rectConfig, null);
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, -1, 64, 0)); // 後方

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.RECT, config);

            assertFalse(result, "後方のエンティティは判定されない");
        }
    }

    // ==================== CIRCLE 形状テスト ====================

    @Nested
    @DisplayName("CIRCLE 形状判定")
    class CircleShapeTests {

        @Test
        @DisplayName("CIRCLE: null configの場合はfalse")
        void testIsInRangeCircle_NullConfig() {
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.CIRCLE, null, null, null, null);

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.CIRCLE, config);

            assertFalse(result, "null configの場合はfalse");
        }

        @Test
        @DisplayName("CIRCLE: 半径内のエンティティは判定される")
        void testIsInRangeCircle_WithinRadius() {
            SkillTarget.CircleConfig circleConfig = new SkillTarget.CircleConfig(5.0);
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.CIRCLE, null, null, null, circleConfig);
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 3, 64, 0));

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.CIRCLE, config);

            assertTrue(result, "半径内のエンティティは判定される");
        }

        @Test
        @DisplayName("CIRCLE: 半径外のエンティティは判定されない")
        void testIsInRangeCircle_OutOfRadius() {
            SkillTarget.CircleConfig circleConfig = new SkillTarget.CircleConfig(5.0);
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.CIRCLE, null, null, null, circleConfig);
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 6, 64, 0));

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.CIRCLE, config);

            assertFalse(result, "半径外のエンティティは判定されない");
        }

        @Test
        @DisplayName("CIRCLE: 境界上のエンティティは判定される")
        void testIsInRangeCircle_OnBoundary() {
            SkillTarget.CircleConfig circleConfig = new SkillTarget.CircleConfig(5.0);
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.CIRCLE, null, null, null, circleConfig);
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 5, 64, 0));

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.CIRCLE, config);

            assertTrue(result, "境界上のエンティティは判定される");
        }
    }

    // ==================== filterInRange テスト ====================

    @Nested
    @DisplayName("filterInRange")
    class FilterInRangeTests {

        @Test
        @DisplayName("filterInRange: 範囲内のエンティティのみが返されること")
        void testFilterInRange() {
            SkillTarget.CircleConfig circleConfig = new SkillTarget.CircleConfig(5.0);
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.CIRCLE, null, null, null, circleConfig);

            // mockEntity: distance=2.0 (within radius 5.0)
            // mockEntity2: distance=5.0 (on boundary, within radius 5.0)
            // mockEntityFar: distance > 5.0 (outside radius)
            List<Entity> candidates = List.of(mockEntity, mockEntity2, mockEntityFar);
            List<Entity> result = ShapeCalculator.filterInRange(candidates, origin, direction, AreaShape.CIRCLE, config);

            assertEquals(2, result.size(), "範囲内と境界上のエンティティが返されること");
            assertTrue(result.contains(mockEntity), "近いエンティティが含まれること");
            assertTrue(result.contains(mockEntity2), "境界上のエンティティも含まれること");
        }

        @Test
        @DisplayName("filterInRange: 空リストの場合は空リストが返されること")
        void testFilterInRange_EmptyList() {
            SkillTarget config = SkillTarget.createDefault();

            List<Entity> result = ShapeCalculator.filterInRange(Collections.emptyList(), origin, direction, AreaShape.SINGLE, config);

            assertTrue(result.isEmpty(), "空リストの場合は空リストが返されること");
        }

        @Test
        @DisplayName("filterInRange: 全エンティティが範囲外の場合は空リストが返されること")
        void testFilterInRange_AllOutOfRange() {
            SkillTarget.CircleConfig circleConfig = new SkillTarget.CircleConfig(2.0);
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.CIRCLE, null, null, null, circleConfig);

            List<Entity> candidates = List.of(mockEntity2, mockEntityFar);
            List<Entity> result = ShapeCalculator.filterInRange(candidates, origin, direction, AreaShape.CIRCLE, config);

            assertTrue(result.isEmpty(), "全エンティティが範囲外の場合は空リストが返されること");
        }

        @Test
        @DisplayName("filterInRange: nullエンティティは除外されること")
        void testFilterInRange_WithNullEntity() {
            SkillTarget config = SkillTarget.createDefault();
            List<Entity> candidates = new ArrayList<>();
            candidates.add(null);
            candidates.add(mockEntity);

            List<Entity> result = ShapeCalculator.filterInRange(candidates, origin, direction, AreaShape.SINGLE, config);

            assertFalse(result.contains(null), "nullエンティティは除外されること");
        }
    }

    // ==================== 複雑なシナリオテスト ====================

    @Nested
    @DisplayName("複雑なシナリオ")
    class ComplexScenarioTests {

        @Test
        @DisplayName("複数形状: 異なる形状で正しく判定されること")
        void testMultipleShapes() {
            SkillTarget.CircleConfig circleConfig = new SkillTarget.CircleConfig(5.0);
            SkillTarget.ConeConfig coneConfig = new SkillTarget.ConeConfig(60.0, 10.0);
            SkillTarget.RectConfig rectConfig = new SkillTarget.RectConfig(6.0, 10.0);

            SkillTarget circleTarget = new SkillTarget(TargetType.AREA_SELF, AreaShape.CIRCLE, null, null, null, circleConfig);
            SkillTarget coneTarget = new SkillTarget(TargetType.CONE, AreaShape.CONE, null, coneConfig, null, null);
            SkillTarget rectTarget = new SkillTarget(TargetType.AREA_SELF, AreaShape.RECT, null, null, rectConfig, null);

            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 3, 64, 0));

            boolean circleResult = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.CIRCLE, circleTarget);
            boolean coneResult = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.CONE, coneTarget);
            boolean rectResult = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.RECT, rectTarget);

            assertTrue(circleResult, "CIRCLEで判定されること");
            assertTrue(coneResult, "CONEで判定されること");
            assertTrue(rectResult, "RECTで判定されること");
        }

        @Test
        @DisplayName("方向依存: CONEとRECTは方向に依存すること")
        void testDirectionDependent() {
            SkillTarget.ConeConfig coneConfig = new SkillTarget.ConeConfig(45.0, 10.0);
            SkillTarget coneTarget = new SkillTarget(TargetType.CONE, AreaShape.CONE, null, coneConfig, null, null);

            // 正面（範囲内）
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 5, 64, 0));
            boolean forwardResult = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.CONE, coneTarget);

            // 後方（範囲外）
            lenient().when(mockEntity2.getLocation()).thenReturn(new Location(mockWorld, -5, 64, 0));
            boolean backwardResult = ShapeCalculator.isInRange(mockEntity2, origin, direction, AreaShape.CONE, coneTarget);

            assertTrue(forwardResult, "正面は範囲内");
            assertFalse(backwardResult, "後方は範囲外");
        }

        @Test
        @DisplayName("方向非依存: CIRCLEは全方位判定")
        void testDirectionIndependent() {
            SkillTarget.CircleConfig circleConfig = new SkillTarget.CircleConfig(6.0);
            SkillTarget circleTarget = new SkillTarget(TargetType.AREA_SELF, AreaShape.CIRCLE, null, null, null, circleConfig);

            // 正面
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 4, 64, 0));
            boolean forwardResult = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.CIRCLE, circleTarget);

            // 横方向
            lenient().when(mockEntity2.getLocation()).thenReturn(new Location(mockWorld, 0, 64, 4));
            boolean sideResult = ShapeCalculator.isInRange(mockEntity2, origin, direction, AreaShape.CIRCLE, circleTarget);

            // 後方
            lenient().when(mockEntityFar.getLocation()).thenReturn(new Location(mockWorld, -4, 64, 0));
            boolean backwardResult = ShapeCalculator.isInRange(mockEntityFar, origin, direction, AreaShape.CIRCLE, circleTarget);

            assertTrue(forwardResult, "正面は範囲内");
            assertTrue(sideResult, "横方向も範囲内");
            assertTrue(backwardResult, "後方も範囲内");
        }
    }

    // ==================== 高さ考慮テスト ====================

    @Nested
    @DisplayName("高さ考慮")
    class HeightTests {

        @Test
        @DisplayName("RECT: 高さ差が2.0以下は範囲内")
        void testRect_WithinHeight() {
            SkillTarget.RectConfig rectConfig = new SkillTarget.RectConfig(6.0, 10.0);
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.RECT, null, null, rectConfig, null);
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 3, 65, 0)); // 1ブロック上

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.RECT, config);

            assertTrue(result, "高さ差1ブロックは範囲内");
        }

        @Test
        @DisplayName("RECT: 高さ差が2.0を超えると範囲外")
        void testRect_OutOfHeight() {
            SkillTarget.RectConfig rectConfig = new SkillTarget.RectConfig(6.0, 10.0);
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.RECT, null, null, rectConfig, null);
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 3, 67, 0)); // 3ブロック上

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.RECT, config);

            assertFalse(result, "高さ差3ブロックは範囲外");
        }

        @Test
        @DisplayName("RECT: 下方向の高さ差も考慮される")
        void testRect_BelowHeight() {
            SkillTarget.RectConfig rectConfig = new SkillTarget.RectConfig(6.0, 10.0);
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.RECT, null, null, rectConfig, null);
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 3, 62, 0)); // 2ブロック下

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.RECT, config);

            assertTrue(result, "下方向も考慮される");
        }
    }

    // ==================== 想定外の入力テスト ====================

    @Nested
    @DisplayName("想定外の入力")
    class EdgeCaseTests {

        @Test
        @DisplayName("SPHERE shape: 球形範囲判定が動作すること")
        void testIsInRange_SphereShape() {
            SkillTarget config = new SkillTarget(TargetType.SPHERE, AreaShape.SPHERE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null,
                    TargetGroupFilter.BOTH, false, false, false,
                    null, null, null, 10.0);

            // mockEntityは(2, 64, 0)で、原点(0, 64, 0)から距離2.0、半径10.0以内
            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.SPHERE, config);

            assertTrue(result, "SPHEREが実装され、範囲内のエンティティはtrueを返す");
        }

        @Test
        @DisplayName("負の距離: 計算が正しく動作すること")
        void testNegativeDistance() {
            SkillTarget.CircleConfig circleConfig = new SkillTarget.CircleConfig(5.0);
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.CIRCLE, null, null, null, circleConfig);
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, -3, 64, 0));

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, direction, AreaShape.CIRCLE, config);

            assertTrue(result, "負の方向でも距離計算が正しく動作する");
        }

        @Test
        @DisplayName("Y方向のみのベクトル: 正しく処理されること")
        void testVerticalDirection() {
            SkillTarget.CircleConfig circleConfig = new SkillTarget.CircleConfig(5.0);
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.CIRCLE, null, null, null, circleConfig);
            Vector verticalDirection = new Vector(0, 1, 0);
            lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 0, 65, 0));

            boolean result = ShapeCalculator.isInRange(mockEntity, origin, verticalDirection, AreaShape.CIRCLE, config);

            assertTrue(result, "Y方向のみのベクトルでも正しく処理される");
        }
    }
}
