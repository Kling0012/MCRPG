package com.example.rpgplugin.skill.target;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ターゲット選択クラスのテストクラス
 *
 * <p>Phase11-9: ターゲット選択の包括的なテスト覆盖</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ターゲット選択 テスト")
class TargetSelectorTest {

    @Mock
    private World world;

    @Mock
    private Player caster;

    private Location casterLocation;
    private UUID casterUuid;

    @BeforeEach
    void setUp() {
        casterUuid = UUID.randomUUID();
        when(caster.getUniqueId()).thenReturn(casterUuid);

        // プレイヤー位置: (0, 64, 0)、東方向を向く
        casterLocation = new Location(world, 0, 64, 0);
        casterLocation.setDirection(new Vector(1, 0, 0));
        when(caster.getLocation()).thenReturn(casterLocation);
        when(world.getUID()).thenReturn(UUID.randomUUID());
    }

    // ===== null/無効値テスト =====

    @Test
    @DisplayName("null設定で空リストを返す")
    void testNullConfig() {
        List<Entity> result = TargetSelector.selectTargets(
                caster, null, Collections.emptyList(), null
        );
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("nullプレイヤーで空リストを返す")
    void testNullCaster() {
        SkillTarget config = SkillTarget.createDefault();
        List<Entity> result = TargetSelector.selectTargets(
                null, config, Collections.emptyList(), null
        );
        assertTrue(result.isEmpty());
    }

    // ===== SELFタイプテスト =====

    @Test
    @DisplayName("SELFタイプ: プレイヤー自身が含まれる")
    void testSelfType() {
        SkillTarget config = new SkillTarget(
                TargetType.SELF, AreaShape.SINGLE, null, null, null, null
        );

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, Collections.emptyList(), null
        );

        assertEquals(1, result.size());
        assertTrue(result.contains(caster));
    }

    @Test
    @DisplayName("SELFタイプ: 候補エンティティが無視される")
    void testSelfTypeIgnoresCandidates() {
        SkillTarget config = new SkillTarget(
                TargetType.SELF, AreaShape.SINGLE, null, null, null, null
        );

        Entity other = createMockEntity(5, 64, 0);
        List<Entity> candidates = Arrays.asList(other);

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, candidates, null
        );

        assertEquals(1, result.size());
        assertTrue(result.contains(caster));
        assertFalse(result.contains(other));
    }

    // ===== EXTERNALタイプテスト =====

    @Test
    @DisplayName("EXTERNALタイプ: 外部ターゲットが含まれる")
    void testExternalType() {
        SkillTarget config = new SkillTarget(
                TargetType.EXTERNAL, AreaShape.SINGLE, null, null, null, null
        );

        Entity external = createMockEntity(5, 64, 0);

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, Collections.emptyList(), external
        );

        assertEquals(1, result.size());
        assertTrue(result.contains(external));
    }

    @Test
    @DisplayName("EXTERNALタイプ: null外部ターゲットで空リスト")
    void testExternalTypeWithNullTarget() {
        SkillTarget config = new SkillTarget(
                TargetType.EXTERNAL, AreaShape.SINGLE, null, null, null, null
        );

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, Collections.emptyList(), null
        );

        assertTrue(result.isEmpty());
    }

    // ===== NEAREST_HOSTILEタイプテスト =====

    @Test
    @DisplayName("NEAREST_HOSTILE: 最も近い敵対MOBを選択")
    void testNearestHostile() {
        SkillTarget config = new SkillTarget(
                TargetType.NEAREST_HOSTILE, AreaShape.SINGLE, null, null, null, null
        );

        Entity hostile1 = createMockEntity(3, 64, 0);  // 距離3
        Entity hostile2 = createMockEntity(5, 64, 0);  // 距離5
        Entity hostile3 = createMockEntity(2, 64, 0);  // 距離2（最も近い）

        List<Entity> candidates = Arrays.asList(hostile1, hostile2, hostile3);

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, candidates, null
        );

        assertEquals(1, result.size());
        assertTrue(result.contains(hostile3));
        assertFalse(result.contains(hostile1));
        assertFalse(result.contains(hostile2));
    }

    @Test
    @DisplayName("NEAREST_HOSTILE: プレイヤーは除外される")
    void testNearestHostileExcludesPlayers() {
        SkillTarget config = new SkillTarget(
                TargetType.NEAREST_HOSTILE, AreaShape.SINGLE, null, null, null, null
        );

        Player otherPlayer = mock(Player.class);
        when(otherPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        Location loc = new Location(world, 2, 64, 0);
        when(otherPlayer.getLocation()).thenReturn(loc);

        Entity hostile = createMockEntity(3, 64, 0);

        List<Entity> candidates = Arrays.asList(otherPlayer, hostile);

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, candidates, null
        );

        assertEquals(1, result.size());
        assertTrue(result.contains(hostile));
        assertFalse(result.contains(otherPlayer));
    }

    @Test
    @DisplayName("NEAREST_HOSTILE: targetSelf=trueで自身を選択")
    void testNearestHostileTargetSelf() {
        SkillTarget.SingleTargetConfig singleConfig = new SkillTarget.SingleTargetConfig(true, true);
        SkillTarget config = new SkillTarget(
                TargetType.NEAREST_HOSTILE, AreaShape.SINGLE, singleConfig, null, null, null
        );

        Entity hostile = createMockEntity(3, 64, 0);
        List<Entity> candidates = Arrays.asList(hostile);

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, candidates, null
        );

        assertEquals(1, result.size());
        assertTrue(result.contains(caster));
        assertFalse(result.contains(hostile));
    }

    @Test
    @DisplayName("NEAREST_HOSTILE: 候補が空の場合は空リスト")
    void testNearestHostileNoCandidates() {
        SkillTarget config = new SkillTarget(
                TargetType.NEAREST_HOSTILE, AreaShape.SINGLE, null, null, null, null
        );

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, Collections.emptyList(), null
        );

        assertTrue(result.isEmpty());
    }

    // ===== NEAREST_ENTITYタイプテスト =====

    @Test
    @DisplayName("NEAREST_ENTITY: 最も近いエンティティを選択（プレイヤー含む）")
    void testNearestEntity() {
        SkillTarget config = new SkillTarget(
                TargetType.NEAREST_ENTITY, AreaShape.SINGLE, null, null, null, null
        );

        Entity entity1 = createMockEntity(3, 64, 0);   // 距離3
        Entity entity2 = createMockEntity(5, 64, 0);   // 距離5
        Entity entity3 = createMockEntity(2, 64, 0);   // 距離2（最も近い）

        List<Entity> candidates = Arrays.asList(entity1, entity2, entity3);

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, candidates, null
        );

        assertEquals(1, result.size());
        assertTrue(result.contains(entity3));
    }

    @Test
    @DisplayName("NEAREST_ENTITY: キャスターは除外される")
    void testNearestEntityExcludesCaster() {
        SkillTarget config = new SkillTarget(
                TargetType.NEAREST_ENTITY, AreaShape.SINGLE, null, null, null, null
        );

        Entity entity = createMockEntity(2, 64, 0);
        List<Entity> candidates = Arrays.asList(entity);

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, candidates, null
        );

        assertEquals(1, result.size());
        assertTrue(result.contains(entity));
        assertFalse(result.contains(caster));
    }

    @Test
    @DisplayName("NEAREST_ENTITY: targetSelf=trueで自身を選択")
    void testNearestEntityTargetSelf() {
        SkillTarget.SingleTargetConfig singleConfig = new SkillTarget.SingleTargetConfig(true, true);
        SkillTarget config = new SkillTarget(
                TargetType.NEAREST_ENTITY, AreaShape.SINGLE, singleConfig, null, null, null
        );

        Entity entity = createMockEntity(2, 64, 0);
        List<Entity> candidates = Arrays.asList(entity);

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, candidates, null
        );

        assertEquals(1, result.size());
        assertTrue(result.contains(caster));
        assertFalse(result.contains(entity));
    }

    // ===== AREAタイプテスト =====

    @Test
    @DisplayName("AREA: 範囲内の全エンティティを選択")
    void testAreaType() {
        SkillTarget config = createAreaTarget(AreaShape.CIRCLE, 5.0);

        Entity inside1 = createMockEntity(2, 64, 0);
        Entity inside2 = createMockEntity(0, 64, 3);
        Entity outside = createMockEntity(6, 64, 0);

        List<Entity> candidates = Arrays.asList(inside1, inside2, outside);

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, candidates, null
        );

        assertEquals(2, result.size());
        assertTrue(result.contains(inside1));
        assertTrue(result.contains(inside2));
        assertFalse(result.contains(outside));
    }

    @Test
    @DisplayName("AREA: 扇状範囲のテスト")
    void testAreaConeType() {
        SkillTarget config = createAreaTarget(AreaShape.CONE, 90.0, 5.0);

        // 前方
        Entity front = createMockEntity(3, 64, 0);
        // 斜め前方（45度以内）
        Entity diagonal = createMockEntity(2, 64, 2);
        // 横方向（90度外）
        Entity side = createMockEntity(0, 64, 4);

        List<Entity> candidates = Arrays.asList(front, diagonal, side);

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, candidates, null
        );

        assertEquals(2, result.size());
        assertTrue(result.contains(front));
        assertTrue(result.contains(diagonal));
        assertFalse(result.contains(side));
    }

    @Test
    @DisplayName("AREA: 矩形範囲のテスト")
    void testAreaRectType() {
        SkillTarget config = createAreaTarget(AreaShape.RECT, 4.0, 6.0);

        // 前方直線
        Entity front = createMockEntity(3, 64, 0);
        // 範囲内の横方向
        Entity sideIn = createMockEntity(3, 64, 1);
        // 範囲外の横方向
        Entity sideOut = createMockEntity(3, 64, 3);
        // 後方
        Entity behind = createMockEntity(-1, 64, 0);

        List<Entity> candidates = Arrays.asList(front, sideIn, sideOut, behind);

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, candidates, null
        );

        assertEquals(2, result.size());
        assertTrue(result.contains(front));
        assertTrue(result.contains(sideIn));
        assertFalse(result.contains(sideOut));
        assertFalse(result.contains(behind));
    }

    @Test
    @DisplayName("AREA: 空候補リストで空リスト")
    void testAreaTypeNoCandidates() {
        SkillTarget config = createAreaTarget(AreaShape.CIRCLE, 5.0);

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, Collections.emptyList(), null
        );

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("AREA: 全エンティティが範囲外")
    void testAreaTypeAllOutside() {
        SkillTarget config = createAreaTarget(AreaShape.CIRCLE, 5.0);

        Entity far1 = createMockEntity(10, 64, 0);
        Entity far2 = createMockEntity(0, 64, 10);

        List<Entity> candidates = Arrays.asList(far1, far2);

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, candidates, null
        );

        assertTrue(result.isEmpty());
    }

    // ===== getNearbyEntitiesメソッドテスト =====

    @Test
    @DisplayName("getNearbyEntities: nullワールドで空リスト")
    void testGetNearbyEntitiesNullWorld() {
        Location nullWorldLoc = mock(Location.class);
        when(nullWorldLoc.getWorld()).thenReturn(null);

        List<Entity> result = TargetSelector.getNearbyEntities(nullWorldLoc, 5.0);

        assertTrue(result.isEmpty());
    }

    // ===== 実用的なシナリオテスト =====

    @Test
    @DisplayName("シナリオ: 近接攻撃スキル")
    void testMeleeAttackScenario() {
        // NEAREST_HOSTILE + SINGLE
        SkillTarget config = new SkillTarget(
                TargetType.NEAREST_HOSTILE, AreaShape.SINGLE, null, null, null, null
        );

        Entity nearbyEnemy = createMockEntity(1.5, 64, 0);
        Entity farEnemy = createMockEntity(5, 64, 0);

        List<Entity> candidates = Arrays.asList(nearbyEnemy, farEnemy);

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, candidates, null
        );

        assertEquals(1, result.size());
        assertTrue(result.contains(nearbyEnemy));
    }

    @Test
    @DisplayName("シナリオ: 範囲攻撃スキル")
    void testAoEAttackScenario() {
        // AREA + CIRCLE
        SkillTarget config = createAreaTarget(AreaShape.CIRCLE, 5.0);

        // 周囲の敵
        Entity n1 = createMockEntity(2, 64, 0);
        Entity n2 = createMockEntity(0, 64, 2);
        Entity n3 = createMockEntity(-2, 64, 0);
        Entity n4 = createMockEntity(0, 64, -2);

        // 範囲外の敵
        Entity far = createMockEntity(6, 64, 0);

        List<Entity> candidates = Arrays.asList(n1, n2, n3, n4, far);

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, candidates, null
        );

        assertEquals(4, result.size());
        assertTrue(result.contains(n1));
        assertTrue(result.contains(n2));
        assertTrue(result.contains(n3));
        assertTrue(result.contains(n4));
        assertFalse(result.contains(far));
    }

    @Test
    @DisplayName("シナリオ: 自己バフスキル")
    void testSelfBuffScenario() {
        SkillTarget config = new SkillTarget(
                TargetType.SELF, AreaShape.SINGLE, null, null, null, null
        );

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, Collections.emptyList(), null
        );

        assertEquals(1, result.size());
        assertEquals(caster, result.get(0));
    }

    @Test
    @DisplayName("シナリオ: 前方扇状範囲攻撃")
    void testFrontalConeAttackScenario() {
        SkillTarget config = createAreaTarget(AreaShape.CONE, 90.0, 5.0);

        // 前方扇状内
        Entity f1 = createMockEntity(3, 64, 0);
        Entity f2 = createMockEntity(2, 64, 2);
        Entity f3 = createMockEntity(2, 64, -2);

        // 範囲外
        Entity side = createMockEntity(0, 64, 4);
        Entity behind = createMockEntity(-3, 64, 0);

        List<Entity> candidates = Arrays.asList(f1, f2, f3, side, behind);

        List<Entity> result = TargetSelector.selectTargets(
                caster, config, candidates, null
        );

        assertEquals(3, result.size());
        assertTrue(result.contains(f1));
        assertTrue(result.contains(f2));
        assertTrue(result.contains(f3));
        assertFalse(result.contains(side));
        assertFalse(result.contains(behind));
    }

    // ===== ヘルパーメソッド =====

    private Entity createMockEntity(double x, double y, double z) {
        Entity entity = mock(Entity.class);
        Location loc = new Location(world, x, y, z);
        when(entity.getLocation()).thenReturn(loc);
        when(entity.getWorld()).thenReturn(world);
        when(entity.getUniqueId()).thenReturn(UUID.randomUUID());
        return entity;
    }

    private SkillTarget createAreaTarget(AreaShape shape, double radius) {
        return new SkillTarget(
                TargetType.AREA,
                shape,
                null,
                null,
                null,
                new SkillTarget.CircleConfig(radius)
        );
    }

    private SkillTarget createAreaTarget(AreaShape shape, double angle, double range) {
        return new SkillTarget(
                TargetType.AREA,
                shape,
                null,
                new SkillTarget.ConeConfig(angle, range),
                null,
                null
        );
    }

    private SkillTarget createAreaTarget(AreaShape shape, double width, double depth) {
        return new SkillTarget(
                TargetType.AREA,
                shape,
                null,
                null,
                new SkillTarget.RectConfig(width, depth),
                null
        );
    }
}
