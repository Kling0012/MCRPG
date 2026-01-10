package com.example.rpgplugin.skill.target;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 範囲計算クラスのテストクラス
 *
 * <p>Phase11-9: 範囲計算の包括的なテスト覆盖</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("範囲計算 テスト")
class ShapeCalculatorTest {

    @Mock
    private World world;

    private Location origin;
    private Vector direction;

    @BeforeEach
    void setUp() {
        when(world.getUID()).thenReturn(UUID.randomUUID());

        // 原点 (0, 64, 0)
        origin = new Location(world, 0, 64, 0);

        // X方向を向く（東）
        direction = new Vector(1, 0, 0);
    }

    // ===== null/無効値テスト =====

    @Test
    @DisplayName("nullエンティティでfalseを返す")
    void testNullEntity() {
        boolean result = ShapeCalculator.isInRange(
                null, origin, direction, AreaShape.CIRCLE, createCircleConfig(5.0)
        );
        assertFalse(result);
    }

    @Test
    @DisplayName("null原点でfalseを返す")
    void testNullOrigin() {
        Entity entity = createEntityAt(3, 64, 0);

        boolean result = ShapeCalculator.isInRange(
                entity, null, direction, AreaShape.CIRCLE, createCircleConfig(5.0)
        );
        assertFalse(result);
    }

    @Test
    @DisplayName("null形状でfalseを返す")
    void testNullShape() {
        Entity entity = createEntityAt(3, 64, 0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, null, createCircleConfig(5.0)
        );
        assertFalse(result);
    }

    // ===== SINGLE（単体）テスト =====

    @Test
    @DisplayName("単体範囲内：接近したエンティティは含まれる")
    void testSingleInRange() {
        Entity entity = createEntityAt(0.5, 64, 0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.SINGLE, null
        );
        assertTrue(result);
    }

    @Test
    @DisplayName("単体範囲外：離れたエンティティは除外される")
    void testSingleOutOfRange() {
        Entity entity = createEntityAt(2, 64, 0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.SINGLE, null
        );
        assertFalse(result);
    }

    // ===== CIRCLE（円形）テスト =====

    @Test
    @DisplayName("円形範囲内：中心に近いエンティティは含まれる")
    void testCircleInRange() {
        Entity entity = createEntityAt(3, 64, 0);
        SkillTarget config = createCircleTarget(5.0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.CIRCLE, config
        );
        assertTrue(result);
    }

    @Test
    @DisplayName("円形範囲外：遠いエンティティは除外される")
    void testCircleOutOfRange() {
        Entity entity = createEntityAt(6, 64, 0);
        SkillTarget config = createCircleTarget(5.0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.CIRCLE, config
        );
        assertFalse(result);
    }

    @Test
    @DisplayName("円形境界：境界上のエンティティは含まれる")
    void testCircleOnBoundary() {
        Entity entity = createEntityAt(5, 64, 0);
        SkillTarget config = createCircleTarget(5.0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.CIRCLE, config
        );
        assertTrue(result);
    }

    @Test
    @DisplayName("円形3D：高さ違いのエンティティも判定される")
    void testCircle3D() {
        Entity entity = createEntityAt(3, 66, 0); // Y+2
        SkillTarget config = createCircleTarget(5.0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.CIRCLE, config
        );
        // 距離 = sqrt(3^2 + 2^2) = sqrt(13) ≈ 3.6 < 5
        assertTrue(result);
    }

    // ===== CONE（扇状）テスト =====

    @Test
    @DisplayName("扇状範囲内：前方のエンティティは含まれる")
    void testConeInFront() {
        Entity entity = createEntityAt(3, 64, 0);
        SkillTarget config = createConeTarget(90.0, 5.0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.CONE, config
        );
        assertTrue(result);
    }

    @Test
    @DisplayName("扇状範囲外：後方のエンティティは除外される")
    void testConeBehind() {
        Entity entity = createEntityAt(-3, 64, 0);
        SkillTarget config = createConeTarget(90.0, 5.0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.CONE, config
        );
        assertFalse(result);
    }

    @Test
    @DisplayName("扇状角度外：側方のエンティティは除外される")
    void testConeOutsideAngle() {
        // Z方向（北）のエンティティ：90度の角度外
        Entity entity = createEntityAt(0, 64, 3);
        SkillTarget config = createConeTarget(45.0, 5.0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.CONE, config
        );
        assertFalse(result);
    }

    @Test
    @DisplayName("扇状範囲外：遠いエンティティは除外される")
    void testConeOutOfRange() {
        Entity entity = createEntityAt(6, 64, 0);
        SkillTarget config = createConeTarget(90.0, 5.0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.CONE, config
        );
        assertFalse(result);
    }

    @Test
    @DisplayName("扇状広角：180度で半円がカバーされる")
    void testConeWideAngle() {
        // 斜め45度方向のエンティティ
        Entity entity = createEntityAt(3, 64, 3);
        SkillTarget config = createConeTarget(180.0, 5.0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.CONE, config
        );
        assertTrue(result);
    }

    // ===== RECT（四角形）テスト =====

    @Test
    @DisplayName("四角形範囲内：前方のエンティティは含まれる")
    void testRectInFront() {
        Entity entity = createEntityAt(3, 64, 0);
        SkillTarget config = createRectTarget(6.0, 4.0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.RECT, config
        );
        assertTrue(result);
    }

    @Test
    @DisplayName("四角形範囲外：後方のエンティティは除外される")
    void testRectBehind() {
        Entity entity = createEntityAt(-1, 64, 0);
        SkillTarget config = createRectTarget(6.0, 4.0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.RECT, config
        );
        assertFalse(result);
    }

    @Test
    @DisplayName("四角形範囲内：幅内の側方エンティティは含まれる")
    void testRectWithinWidth() {
        Entity entity = createEntityAt(3, 64, 1.5); // 右に1.5ブロック
        SkillTarget config = createRectTarget(6.0, 4.0); // 幅4.0

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.RECT, config
        );
        assertTrue(result);
    }

    @Test
    @DisplayName("四角形範囲外：幅外の側方エンティティは除外される")
    void testRectOutsideWidth() {
        Entity entity = createEntityAt(3, 64, 3); // 右に3ブロック
        SkillTarget config = createRectTarget(6.0, 4.0); // 幅4.0（半分が2.0）

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.RECT, config
        );
        assertFalse(result);
    }

    @Test
    @DisplayName("四角形範囲外：奥行き超過のエンティティは除外される")
    void testRectOutsideDepth() {
        Entity entity = createEntityAt(7, 64, 0); // 奥行6.0を超える
        SkillTarget config = createRectTarget(6.0, 4.0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.RECT, config
        );
        assertFalse(result);
    }

    @Test
    @DisplayName("四角形範囲内：高さ許容範囲内のエンティティ")
    void testRectWithinHeight() {
        Entity entity = createEntityAt(3, 65.5, 0); // Y+1.5（2.0以内）
        SkillTarget config = createRectTarget(6.0, 4.0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.RECT, config
        );
        assertTrue(result);
    }

    @Test
    @DisplayName("四角形範囲外：高さ許容範囲外のエンティティ")
    void testRectOutsideHeight() {
        Entity entity = createEntityAt(3, 67, 0); // Y+3（2.0超過）
        SkillTarget config = createRectTarget(6.0, 4.0);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.RECT, config
        );
        assertFalse(result);
    }

    // ===== 異なるワールドテスト =====

    @Test
    @DisplayName("異なるワールドのエンティティは除外される")
    void testDifferentWorld() {
        World otherWorld = mock(World.class);
        when(otherWorld.getUID()).thenReturn(UUID.randomUUID());

        Location otherOrigin = new Location(otherWorld, 0, 64, 0);
        Entity entity = createEntityAt(3, 64, 0, otherWorld);

        boolean result = ShapeCalculator.isInRange(
                entity, origin, direction, AreaShape.CIRCLE, createCircleTarget(5.0)
        );
        assertFalse(result);
    }

    // ===== filterInRangeメソッドテスト =====

    @Test
    @DisplayName("filterInRange: 複数エンティティのフィルタリング")
    void testFilterInRange() {
        Entity inside1 = createEntityAt(2, 64, 0);  // 円形内
        Entity inside2 = createEntityAt(0, 64, 2);  // 円形内
        Entity outside1 = createEntityAt(6, 64, 0); // 円形外
        Entity outside2 = createEntityAt(0, 64, 6); // 円形外

        List<Entity> candidates = Arrays.asList(inside1, inside2, outside1, outside2);
        SkillTarget config = createCircleTarget(5.0);

        List<Entity> result = ShapeCalculator.filterInRange(
                candidates, origin, direction, AreaShape.CIRCLE, config
        );

        assertEquals(2, result.size());
        assertTrue(result.contains(inside1));
        assertTrue(result.contains(inside2));
        assertFalse(result.contains(outside1));
        assertFalse(result.contains(outside2));
    }

    @Test
    @DisplayName("filterInRange: 空リストの処理")
    void testFilterInRangeEmptyList() {
        List<Entity> candidates = new ArrayList<>();
        SkillTarget config = createCircleTarget(5.0);

        List<Entity> result = ShapeCalculator.filterInRange(
                candidates, origin, direction, AreaShape.CIRCLE, config
        );

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("filterInRange: 全エンティティが範囲外")
    void testFilterInRangeAllOutside() {
        Entity far1 = createEntityAt(10, 64, 0);
        Entity far2 = createEntityAt(0, 64, 10);

        List<Entity> candidates = Arrays.asList(far1, far2);
        SkillTarget config = createCircleTarget(5.0);

        List<Entity> result = ShapeCalculator.filterInRange(
                candidates, origin, direction, AreaShape.CIRCLE, config
        );

        assertTrue(result.isEmpty());
    }

    // ===== 実用的なシナリオテスト =====

    @Test
    @DisplayName("前方扇状攻撃スキルシナリオ")
    void testForwardConeAttackScenario() {
        // プレイヤーの前方90度、距離5ブロックの扇状範囲
        SkillTarget config = createConeTarget(90.0, 5.0);

        // 前方のターゲット
        Entity front = createEntityAt(3, 64, 0);
        assertTrue(ShapeCalculator.isInRange(front, origin, direction, AreaShape.CONE, config));

        // 斜め前方のターゲット
        Entity diagonal = createEntityAt(2, 64, 2);
        assertTrue(ShapeCalculator.isInRange(diagonal, origin, direction, AreaShape.CONE, config));

        // 横方向のターゲット（90度境界）
        Entity side = createEntityAt(0, 64, 3);
        assertFalse(ShapeCalculator.isInRange(side, origin, direction, AreaShape.CONE, config));

        // 後方のターゲット
        Entity behind = createEntityAt(-3, 64, 0);
        assertFalse(ShapeCalculator.isInRange(behind, origin, direction, AreaShape.CONE, config));
    }

    @Test
    @DisplayName("周囲範囲攻撃スキルシナリオ")
    void testAoEAttackScenario() {
        // 周囲半径5ブロックの円形範囲
        SkillTarget config = createCircleTarget(5.0);

        // 全方向のターゲット
        Entity north = createEntityAt(0, 64, -3);
        Entity south = createEntityAt(0, 64, 3);
        Entity east = createEntityAt(3, 64, 0);
        Entity west = createEntityAt(-3, 64, 0);

        assertTrue(ShapeCalculator.isInRange(north, origin, direction, AreaShape.CIRCLE, config));
        assertTrue(ShapeCalculator.isInRange(south, origin, direction, AreaShape.CIRCLE, config));
        assertTrue(ShapeCalculator.isInRange(east, origin, direction, AreaShape.CIRCLE, config));
        assertTrue(ShapeCalculator.isInRange(west, origin, direction, AreaShape.CIRCLE, config));

        // 範囲外
        Entity far = createEntityAt(6, 64, 0);
        assertFalse(ShapeCalculator.isInRange(far, origin, direction, AreaShape.CIRCLE, config));
    }

    @Test
    @DisplayName("直線貫通攻撃スキルシナリオ")
    void testLinearAttackScenario() {
        // 前方6ブロック、幅2ブロックの矩形範囲
        SkillTarget config = createRectTarget(6.0, 2.0);

        // 直線上のターゲット
        Entity frontNear = createEntityAt(2, 64, 0);
        Entity frontMid = createEntityAt(4, 64, 0);
        Entity frontFar = createEntityAt(5.5, 64, 0);

        assertTrue(ShapeCalculator.isInRange(frontNear, origin, direction, AreaShape.RECT, config));
        assertTrue(ShapeCalculator.isInRange(frontMid, origin, direction, AreaShape.RECT, config));
        assertTrue(ShapeCalculator.isInRange(frontFar, origin, direction, AreaShape.RECT, config));

        // 僅かにずれたターゲット（幅外）
        Entity slightlyOff = createEntityAt(3, 64, 1.1);
        assertFalse(ShapeCalculator.isInRange(slightlyOff, origin, direction, AreaShape.RECT, config));
    }

    // ===== ヘルパーメソッド =====

    private Entity createEntityAt(double x, double y, double z) {
        return createEntityAt(x, y, z, world);
    }

    private Entity createEntityAt(double x, double y, double z, World entityWorld) {
        Entity entity = mock(Entity.class);
        Location loc = new Location(entityWorld, x, y, z);
        when(entity.getLocation()).thenReturn(loc);
        when(entity.getWorld()).thenReturn(entityWorld);
        return entity;
    }

    private SkillTarget createCircleTarget(double radius) {
        return new SkillTarget(
                TargetType.AREA,
                AreaShape.CIRCLE,
                null,
                null,
                null,
                new SkillTarget.CircleConfig(radius)
        );
    }

    private SkillTarget createCircleConfig(double radius) {
        return createCircleTarget(radius);
    }

    private SkillTarget createConeTarget(double angle, double range) {
        return new SkillTarget(
                TargetType.AREA,
                AreaShape.CONE,
                null,
                new SkillTarget.ConeConfig(angle, range),
                null,
                null
        );
    }

    private SkillTarget createRectTarget(double width, double depth) {
        return new SkillTarget(
                TargetType.AREA,
                AreaShape.RECT,
                null,
                null,
                new SkillTarget.RectConfig(width, depth),
                null
        );
    }
}
