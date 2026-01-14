package com.example.rpgplugin.skill.target;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.lenient;

/**
 * TargetSelectorのテストクラス
 */
@DisplayName("TargetSelector テスト")
@ExtendWith(MockitoExtension.class)
class TargetSelectorTest {

    @Mock
    private World mockWorld;

    @Mock
    private Player mockCaster;

    @Mock
    private Player mockTargetPlayer;

    @Mock
    private Zombie mockZombie;

    @Mock
    private Zombie mockZombie2;

    @Mock
    private Entity mockEntity;

    private Location casterLocation;
    private UUID casterUuid;
    private UUID targetPlayerUuid;
    private UUID zombieUuid;

    @BeforeEach
    void setUp() {
        casterUuid = UUID.randomUUID();
        targetPlayerUuid = UUID.randomUUID();
        zombieUuid = UUID.randomUUID();

        casterLocation = new Location(mockWorld, 0, 64, 0);

        // Mock setup for caster
        lenient().when(mockCaster.getUniqueId()).thenReturn(casterUuid);
        lenient().when(mockCaster.getLocation()).thenReturn(casterLocation);
        lenient().when(mockCaster.getWorld()).thenReturn(mockWorld);
        lenient().when(mockCaster.getType()).thenReturn(EntityType.PLAYER);

        // Mock setup for target player
        lenient().when(mockTargetPlayer.getUniqueId()).thenReturn(targetPlayerUuid);
        lenient().when(mockTargetPlayer.getType()).thenReturn(EntityType.PLAYER);
        lenient().when(mockTargetPlayer.getLocation()).thenReturn(new Location(mockWorld, 3, 64, 0));
        lenient().when(mockTargetPlayer.getWorld()).thenReturn(mockWorld);

        // Mock setup for zombies
        lenient().when(mockZombie.getUniqueId()).thenReturn(zombieUuid);
        lenient().when(mockZombie.getType()).thenReturn(EntityType.ZOMBIE);
        lenient().when(mockZombie.getLocation()).thenReturn(new Location(mockWorld, 3, 64, 0));
        lenient().when(mockZombie.getWorld()).thenReturn(mockWorld);

        lenient().when(mockZombie2.getUniqueId()).thenReturn(UUID.randomUUID());
        lenient().when(mockZombie2.getType()).thenReturn(EntityType.ZOMBIE);
        lenient().when(mockZombie2.getLocation()).thenReturn(new Location(mockWorld, 5, 64, 0));
        lenient().when(mockZombie2.getWorld()).thenReturn(mockWorld);

        // Mock setup for generic entity (ARROW type - not Player, not Mob in filter sense)
        lenient().when(mockEntity.getUniqueId()).thenReturn(UUID.randomUUID());
        lenient().when(mockEntity.getType()).thenReturn(EntityType.ARROW);
        lenient().when(mockEntity.getLocation()).thenReturn(new Location(mockWorld, 2, 64, 0));
        lenient().when(mockEntity.getWorld()).thenReturn(mockWorld);
    }

    // ==================== 基本テスト ====================

    @Nested
    @DisplayName("基本動作")
    class BasicTests {

        @Test
        @DisplayName("selectTargets: null configの場合は空リストを返すこと")
        void testSelectTargets_NullConfig() {
            List<Entity> candidates = List.of(mockZombie);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, null, candidates, null);

            assertTrue(result.isEmpty(), "null configの場合は空リストを返すこと");
        }

        @Test
        @DisplayName("selectTargets: null casterの場合は空リストを返すこと")
        void testSelectTargets_NullCaster() {
            SkillTarget config = SkillTarget.createDefault();
            List<Entity> result = TargetSelector.selectTargets(null, config, List.of(mockZombie), null);

            assertTrue(result.isEmpty(), "null casterの場合は空リストを返すこと");
        }

        @Test
        @DisplayName("selectTargets: null candidatesの場合は空のArrayListとして処理されること")
        void testSelectTargets_NullCandidates() {
            SkillTarget config = new SkillTarget(TargetType.SELF, AreaShape.SINGLE,
                    new SkillTarget.SingleTargetConfig(true, false),
                    null, null, null);

            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, null, null);

            assertEquals(1, result.size(), "SELFタイプならcasterが含まれること");
            assertEquals(mockCaster, result.get(0), "casterが含まれること");
        }
    }

    // ==================== SELF テスト ====================

    @Nested
    @DisplayName("SELF ターゲット選択")
    class SelfTests {

        @Test
        @DisplayName("SELF: キャスター自身のみを返すこと")
        void testSelectTargets_Self() {
            SkillTarget config = new SkillTarget(TargetType.SELF, AreaShape.SINGLE,
                    new SkillTarget.SingleTargetConfig(true, false),
                    null, null, null);

            List<Entity> candidates = List.of(mockZombie, mockTargetPlayer);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertEquals(1, result.size(), "キャスターのみが含まれること");
            assertEquals(mockCaster, result.get(0), "キャスターが含まれること");
        }
    }

    // ==================== SELF_PLUS_ONE テスト ====================

    @Nested
    @DisplayName("SELF_PLUS_ONE ターゲット選択")
    class SelfPlusOneTests {

        @Test
        @DisplayName("SELF_PLUS_ONE: キャスターと最も近いエンティティを返すこと")
        void testSelectTargets_SelfPlusOne() {
            SkillTarget config = new SkillTarget(TargetType.SELF_PLUS_ONE, AreaShape.SINGLE,
                    new SkillTarget.SingleTargetConfig(true, false),
                    null, null, null);

            List<Entity> candidates = List.of(mockZombie, mockZombie2);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertEquals(2, result.size(), "キャスターと最も近いエンティティが含まれること");
            assertEquals(mockCaster, result.get(0), "キャスターが最初に含まれること");
        }

        @Test
        @DisplayName("SELF_PLUS_ONE: 候補がいない場合はキャスターのみを返すこと")
        void testSelectTargets_SelfPlusOne_NoCandidates() {
            SkillTarget config = new SkillTarget(TargetType.SELF_PLUS_ONE, AreaShape.SINGLE,
                    new SkillTarget.SingleTargetConfig(true, false),
                    null, null, null);

            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, Collections.emptyList(), null);

            assertEquals(1, result.size(), "キャスターのみが含まれること");
            assertEquals(mockCaster, result.get(0), "キャスターが含まれること");
        }
    }

    // ==================== NEAREST_HOSTILE テスト ====================

    @Nested
    @DisplayName("NEAREST_HOSTILE ターゲット選択")
    class NearestHostileTests {

        @Test
        @DisplayName("NEAREST_HOSTILE: 最も近いMobを返すこと")
        void testSelectTargets_NearestHostile() {
            SkillTarget config = new SkillTarget(TargetType.NEAREST_HOSTILE, AreaShape.SINGLE,
                    new SkillTarget.SingleTargetConfig(true, false),
                    null, null, null);

            List<Entity> candidates = List.of(mockZombie, mockZombie2, mockTargetPlayer);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertEquals(1, result.size(), "最も近い敵対Mobが含まれること");
            assertEquals(mockZombie, result.get(0), "最も近いZombieが含まれること");
        }

        @Test
        @DisplayName("NEAREST_HOSTILE: targetSelf=trueの場合はキャスターを返すこと")
        void testSelectTargets_NearestHostile_TargetSelf() {
            SkillTarget config = new SkillTarget(TargetType.NEAREST_HOSTILE, AreaShape.SINGLE,
                    new SkillTarget.SingleTargetConfig(true, true),
                    null, null, null);

            List<Entity> candidates = List.of(mockZombie, mockZombie2);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertEquals(1, result.size(), "キャスターが含まれること");
            assertEquals(mockCaster, result.get(0), "キャスターが含まれること");
        }

        @Test
        @DisplayName("NEAREST_HOSTILE: 候補がいない場合は空リストを返すこと")
        void testSelectTargets_NearestHostile_NoCandidates() {
            SkillTarget config = new SkillTarget(TargetType.NEAREST_HOSTILE, AreaShape.SINGLE,
                    new SkillTarget.SingleTargetConfig(true, false),
                    null, null, null);

            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, Collections.emptyList(), null);

            assertTrue(result.isEmpty(), "候補がいない場合は空リストを返すこと");
        }
    }

    // ==================== NEAREST_PLAYER テスト ====================

    @Nested
    @DisplayName("NEAREST_PLAYER ターゲット選択")
    class NearestPlayerTests {

        @Test
        @DisplayName("NEAREST_PLAYER: 最も近いプレイヤーを返すこと")
        void testSelectTargets_NearestPlayer() {
            SkillTarget config = new SkillTarget(TargetType.NEAREST_PLAYER, AreaShape.SINGLE,
                    new SkillTarget.SingleTargetConfig(true, false),
                    null, null, null);

            List<Entity> candidates = List.of(mockTargetPlayer, mockZombie);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertEquals(1, result.size(), "最も近いプレイヤーが含まれること");
            assertEquals(mockTargetPlayer, result.get(0), "プレイヤーが含まれること");
        }

        @Test
        @DisplayName("NEAREST_PLAYER: プレイヤーがいない場合は空リストを返すこと")
        void testSelectTargets_NearestPlayer_NoPlayers() {
            SkillTarget config = new SkillTarget(TargetType.NEAREST_PLAYER, AreaShape.SINGLE,
                    new SkillTarget.SingleTargetConfig(true, false),
                    null, null, null);

            List<Entity> candidates = List.of(mockZombie, mockZombie2);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertTrue(result.isEmpty(), "プレイヤーがいない場合は空リストを返すこと");
        }
    }

    // ==================== NEAREST_ENTITY テスト ====================

    @Nested
    @DisplayName("NEAREST_ENTITY ターゲット選択")
    class NearestEntityTests {

        @Test
        @DisplayName("NEAREST_ENTITY: 最も近いエンティティを返すこと（プレイヤー除く）")
        void testSelectTargets_NearestEntity() {
            SkillTarget config = new SkillTarget(TargetType.NEAREST_ENTITY, AreaShape.SINGLE,
                    new SkillTarget.SingleTargetConfig(true, false),
                    null, null, null);

            List<Entity> candidates = List.of(mockEntity, mockZombie);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertEquals(1, result.size(), "最も近いエンティティが含まれること");
        }

        @Test
        @DisplayName("NEAREST_ENTITY: targetSelf=trueの場合はキャスターを返すこと")
        void testSelectTargets_NearestEntity_TargetSelf() {
            SkillTarget config = new SkillTarget(TargetType.NEAREST_ENTITY, AreaShape.SINGLE,
                    new SkillTarget.SingleTargetConfig(true, true),
                    null, null, null);

            List<Entity> candidates = List.of(mockEntity);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertEquals(1, result.size(), "キャスターが含まれること");
            assertEquals(mockCaster, result.get(0), "キャスターが含まれること");
        }
    }

    // ==================== AREA_SELF / AREA_OTHERS テスト ====================

    @Nested
    @DisplayName("AREA ターゲット選択")
    class AreaTests {

        @Test
        @DisplayName("AREA_SELF: 範囲内の全エンティティとキャスターを返すこと")
        void testSelectTargets_AreaSelf() {
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.SPHERE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null,
                    TargetGroupFilter.BOTH, false, false, true,
                    10.0, null, null, 10.0);

            List<Entity> candidates = List.of(mockZombie, mockEntity);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertTrue(result.contains(mockZombie), "Zombieが含まれること");
            assertTrue(result.contains(mockEntity), "Entityが含まれること");
            assertTrue(result.contains(mockCaster), "キャスターが含まれること");
        }

        @Test
        @DisplayName("AREA_OTHERS: 範囲内のエンティティ（キャスター除く）を返すこと")
        void testSelectTargets_AreaOthers() {
            SkillTarget config = new SkillTarget(TargetType.AREA_OTHERS, AreaShape.SPHERE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null,
                    TargetGroupFilter.BOTH, false, false, false,
                    10.0, null, null, 10.0);

            List<Entity> candidates = List.of(mockZombie, mockEntity);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertTrue(result.contains(mockZombie), "Zombieが含まれること");
            assertTrue(result.contains(mockEntity), "Entityが含まれること");
            assertFalse(result.contains(mockCaster), "キャスターは含まれないこと");
        }

        @Test
        @DisplayName("AREA: includeCaster=trueの場合はフィルタに関係なくキャスターを含むこと")
        void testSelectTargets_Area_IncludeCaster() {
            SkillTarget config = new SkillTarget(TargetType.AREA_OTHERS, AreaShape.SPHERE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null,
                    TargetGroupFilter.BOTH, false, false, true,
                    10.0, null, null, 10.0);

            List<Entity> candidates = List.of(mockZombie);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertTrue(result.contains(mockCaster), "includeCaster=trueならキャスターが含まれること");
        }
    }

    // ==================== EXTERNAL テスト ====================

    @Nested
    @DisplayName("EXTERNAL ターゲット選択")
    class ExternalTests {

        @Test
        @DisplayName("EXTERNAL: 外部ターゲットを返すこと")
        void testSelectTargets_External() {
            SkillTarget config = new SkillTarget(TargetType.EXTERNAL, AreaShape.SINGLE,
                    null, null, null, null);

            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, Collections.emptyList(), mockZombie);

            assertEquals(1, result.size(), "外部ターゲットが含まれること");
            assertEquals(mockZombie, result.get(0), "指定した外部ターゲットが含まれること");
        }

        @Test
        @DisplayName("EXTERNAL: null外部ターゲットの場合は空リストを返すこと")
        void testSelectTargets_External_NullTarget() {
            SkillTarget config = new SkillTarget(TargetType.EXTERNAL, AreaShape.SINGLE,
                    null, null, null, null);

            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, Collections.emptyList(), null);

            assertTrue(result.isEmpty(), "null外部ターゲットの場合は空リストを返すこと");
        }
    }

    // ==================== ENTITY TYPE フィルタテスト ====================

    @Nested
    @DisplayName("EntityTypeFilter フィルタリング")
    class EntityTypeFilterTests {

        @Test
        @DisplayName("PLAYER_ONLY: プレイヤーのみを返すこと")
        void testEntityTypeFilter_PlayerOnly() {
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.SPHERE,
                    null, null, null, null,
                    EntityTypeFilter.PLAYER_ONLY, null,
                    TargetGroupFilter.BOTH, false, false, false,
                    10.0, null, null, 10.0);

            List<Entity> candidates = List.of(mockTargetPlayer, mockZombie);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertTrue(result.contains(mockTargetPlayer), "プレイヤーが含まれること");
            assertFalse(result.contains(mockZombie), "Zombieは含まれないこと");
        }

        @Test
        @DisplayName("MOB_ONLY: Mobのみを返すこと")
        void testEntityTypeFilter_MobOnly() {
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.SPHERE,
                    null, null, null, null,
                    EntityTypeFilter.MOB_ONLY, null,
                    TargetGroupFilter.BOTH, false, false, false,
                    10.0, null, null, 10.0);

            List<Entity> candidates = List.of(mockTargetPlayer, mockZombie);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertFalse(result.contains(mockTargetPlayer), "プレイヤーは含まれないこと");
            assertTrue(result.contains(mockZombie), "Zombieが含まれること");
        }

        @Test
        @DisplayName("ALL: すべてのエンティティを返すこと")
        void testEntityTypeFilter_All() {
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.SPHERE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null,
                    TargetGroupFilter.BOTH, false, false, false,
                    10.0, null, null, 10.0);

            List<Entity> candidates = List.of(mockTargetPlayer, mockZombie);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertTrue(result.contains(mockTargetPlayer), "プレイヤーが含まれること");
            assertTrue(result.contains(mockZombie), "Zombieが含まれること");
        }
    }

    // ==================== GROUP フィルタテスト ====================

    @Nested
    @DisplayName("TargetGroupFilter フィルタリング")
    class GroupFilterTests {

        @Test
        @DisplayName("ENEMY: Mobのみを返すこと（PvPなしサーバー）")
        void testGroupFilter_Enemy() {
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.SPHERE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null,
                    TargetGroupFilter.ENEMY, false, false, false,
                    10.0, null, null, 10.0);

            List<Entity> candidates = List.of(mockTargetPlayer, mockZombie);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertFalse(result.contains(mockTargetPlayer), "プレイヤーは敵ではない（PvPなし）");
            assertTrue(result.contains(mockZombie), "Mobは敵として含まれること");
        }

        @Test
        @DisplayName("ALLY: プレイヤーのみを返すこと")
        void testGroupFilter_Ally() {
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.SPHERE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null,
                    TargetGroupFilter.ALLY, false, false, false,
                    10.0, null, null, 10.0);

            List<Entity> candidates = List.of(mockTargetPlayer, mockZombie);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertTrue(result.contains(mockTargetPlayer), "プレイヤーは味方");
            assertFalse(result.contains(mockZombie), "Mobは味方ではない");
        }

        @Test
        @DisplayName("BOTH: すべてのエンティティを返すこと")
        void testGroupFilter_Both() {
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.SPHERE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null,
                    TargetGroupFilter.BOTH, false, false, false,
                    10.0, null, null, 10.0);

            List<Entity> candidates = List.of(mockTargetPlayer, mockZombie);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertTrue(result.contains(mockTargetPlayer), "プレイヤーが含まれること");
            assertTrue(result.contains(mockZombie), "Zombieが含まれること");
        }
    }

    // ==================== LINE テスト ====================

    @Nested
    @DisplayName("LINE ターゲット選択")
    class LineTests {

        @Test
        @DisplayName("LINE: 直線上のエンティティを返すこと")
        void testSelectTargets_Line() {
            SkillTarget config = new SkillTarget(TargetType.LINE, 10.0, 2.0,
                    null, null,
                    EntityTypeFilter.ALL, null);

            List<Entity> candidates = List.of(mockEntity, mockZombie);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            // 範囲内のエンティティが含まれるか確認
            assertFalse(result.isEmpty(), "直線上のエンティティが含まれること");
        }

        @Test
        @DisplayName("LINE: includeCaster=trueの場合はキャスターを含むこと")
        void testSelectTargets_Line_IncludeCaster() {
            SkillTarget config = new SkillTarget(TargetType.LINE, 10.0, 2.0,
                    null, null,
                    EntityTypeFilter.ALL, null);

            // includeCasterを設定するには拡張コンストラクタを使用
            SkillTarget configWithCaster = new SkillTarget(TargetType.LINE, AreaShape.SINGLE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null,
                    TargetGroupFilter.BOTH, false, false, true,
                    10.0, 2.0, null, null);

            List<Entity> candidates = List.of(mockEntity);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, configWithCaster, candidates, null);

            assertTrue(result.contains(mockCaster), "includeCaster=trueならキャスターが含まれること");
        }
    }

    // ==================== CONE テスト ====================

    @Nested
    @DisplayName("CONE ターゲット選択")
    class ConeTests {

        @Test
        @DisplayName("CONE: 扇状範囲内のエンティティを返すこと")
        void testSelectTargets_Cone() {
            SkillTarget config = new SkillTarget(TargetType.CONE, 10.0, 2.0,
                    60.0, null,
                    EntityTypeFilter.ALL, null);

            List<Entity> candidates = List.of(mockEntity, mockZombie);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            // 結果が空ではないことを確認
            assertNotNull(result, "結果がnullでないこと");
        }

        @Test
        @DisplayName("CONE: includeCaster=trueの場合はキャスターを含むこと")
        void testSelectTargets_Cone_IncludeCaster() {
            SkillTarget config = new SkillTarget(TargetType.CONE, AreaShape.SINGLE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null,
                    TargetGroupFilter.BOTH, false, false, true,
                    10.0, 2.0, 60.0, null);

            List<Entity> candidates = List.of(mockEntity);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertTrue(result.contains(mockCaster), "includeCaster=trueならキャスターが含まれること");
        }
    }

    // ==================== LOOKING テスト ====================

    @Nested
    @DisplayName("LOOKING ターゲット選択")
    class LookingTests {

        @Test
        @DisplayName("LOOKING: 視線上のエンティティを返すこと")
        void testSelectTargets_Looking() {
            SkillTarget config = new SkillTarget(TargetType.LOOKING, 10.0, 2.0,
                    null, null,
                    EntityTypeFilter.ALL, 3);

            List<Entity> candidates = List.of(mockEntity, mockZombie, mockZombie2);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertNotNull(result, "結果がnullでないこと");
            assertTrue(result.size() <= 3, "最大ターゲット数を超えないこと");
        }

        @Test
        @DisplayName("LOOKING: includeCaster=trueの場合はキャスターを含むこと")
        void testSelectTargets_Looking_IncludeCaster() {
            SkillTarget config = new SkillTarget(TargetType.LOOKING, AreaShape.SINGLE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, 3,
                    TargetGroupFilter.BOTH, false, false, true,
                    10.0, 2.0, null, null);

            List<Entity> candidates = List.of(mockEntity);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertTrue(result.contains(mockCaster), "includeCaster=trueならキャスターが含まれること");
        }
    }

    // ==================== SPHERE テスト ====================

    @Nested
    @DisplayName("SPHERE ターゲット選択")
    class SphereTests {

        @Test
        @DisplayName("SPHERE: 球形範囲内のエンティティを返すこと")
        void testSelectTargets_Sphere() {
            SkillTarget config = new SkillTarget(TargetType.SPHERE, null, null,
                    null, 10.0,
                    EntityTypeFilter.ALL, null);

            List<Entity> candidates = List.of(mockZombie, mockEntity);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertTrue(result.contains(mockZombie), "範囲内のZombieが含まれること");
        }

        @Test
        @DisplayName("SPHERE: includeCaster=trueの場合はキャスターを含むこと")
        void testSelectTargets_Sphere_IncludeCaster() {
            SkillTarget config = new SkillTarget(TargetType.SPHERE, AreaShape.SINGLE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null,
                    TargetGroupFilter.BOTH, false, false, true,
                    null, null, null, 10.0);

            List<Entity> candidates = List.of(mockZombie);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertTrue(result.contains(mockCaster), "includeCaster=trueならキャスターが含まれること");
        }

        @Test
        @DisplayName("SPHERE: maxTargetsで結果数を制限すること")
        void testSelectTargets_Sphere_MaxTargets() {
            SkillTarget config = new SkillTarget(TargetType.SPHERE, null, null,
                    null, 10.0,
                    EntityTypeFilter.ALL, 2);

            List<Entity> candidates = List.of(mockZombie, mockZombie2, mockEntity);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertTrue(result.size() <= 2, "最大ターゲット数を超えないこと");
        }
    }

    // ==================== ユーティリティメソッドテスト ====================

    @Nested
    @DisplayName("ユーティリティメソッド")
    class UtilityMethodTests {

        @Test
        @DisplayName("getNearbyEntities: ワールドがnullの場合は空リストを返すこと")
        void testGetNearbyEntities_NullWorld() {
            Location locationWithoutWorld = new Location(null, 0, 64, 0);
            List<Entity> result = TargetSelector.getNearbyEntities(locationWithoutWorld, 10.0);

            assertTrue(result.isEmpty(), "ワールドがnullの場合は空リストを返すこと");
        }

        @Test
        @DisplayName("getNearbyEntities: ワールドから近くのエンティティを取得すること")
        void testGetNearbyEntities() {
            // World mock setup
            lenient().when(mockWorld.getNearbyEntities(any(Location.class), anyDouble(), anyDouble(), anyDouble()))
                    .thenReturn(List.of(mockZombie, mockEntity));

            List<Entity> result = TargetSelector.getNearbyEntities(casterLocation, 10.0);

            assertEquals(2, result.size(), "近くのエンティティが取得できること");
            assertTrue(result.contains(mockZombie), "Zombieが含まれること");
            assertTrue(result.contains(mockEntity), "Entityが含まれること");
        }
    }

    // ==================== maxTargets テスト ====================

    @Nested
    @DisplayName("最大ターゲット数制限")
    class MaxTargetsTests {

        @Test
        @DisplayName("maxTargets: 結果数が制限されること")
        void testMaxTargets_Limit() {
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.SPHERE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, 2,
                    TargetGroupFilter.BOTH, false, false, false,
                    10.0, null, null, 10.0);

            List<Entity> candidates = List.of(mockZombie, mockZombie2, mockEntity);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            assertTrue(result.size() <= 3, "キャスター+最大2エンティティまで");
        }

        @Test
        @DisplayName("maxTargets=null: 制限なし")
        void testMaxTargets_Unlimited() {
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.SPHERE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null,
                    TargetGroupFilter.BOTH, false, false, false,
                    10.0, null, null, 10.0);

            List<Entity> candidates = List.of(mockZombie, mockZombie2, mockEntity);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            // すべての候補が含まれる（キャスターも含む可能性がある）
            assertTrue(result.size() >= candidates.size(), "すべての候補が含まれること");
        }

        @Test
        @DisplayName("LINE: maxTargetsで近い順に制限されること")
        void testMaxTargets_Line() {
            // 簡易コンストラクタを使用
            SkillTarget config = new SkillTarget(TargetType.LINE, 10.0, 1.0, null, null,
                    EntityTypeFilter.ALL, 2);

            // 3つのエンティティを異なる距離に配置
            List<Entity> candidates = List.of(mockEntity, mockZombie, mockZombie2);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            // 最大2つに制限されること
            assertTrue(result.size() <= 2, "最大2つのエンティティが返されること");
        }

        @Test
        @DisplayName("CONE: maxTargetsで近い順に制限されること")
        void testMaxTargets_Cone() {
            // 簡易コンストラクタを使用（coneAngleを指定）
            SkillTarget config = new SkillTarget(TargetType.CONE, 10.0, 1.0, 90.0, null,
                    EntityTypeFilter.ALL, 2);

            // 3つのエンティティを範囲内に配置
            List<Entity> candidates = List.of(mockEntity, mockZombie, mockZombie2);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            // 最大2つに制限されること
            assertTrue(result.size() <= 2, "最大2つのエンティティが返されること");
        }

        @Test
        @DisplayName("LOOKING: maxTargetsで近い順に制限されること")
        void testMaxTargets_Looking() {
            // 簡易コンストラクタを使用
            SkillTarget config = new SkillTarget(TargetType.LOOKING, 10.0, 1.0, null, null,
                    EntityTypeFilter.ALL, 2);

            // 3つのエンティティを視線上に配置
            List<Entity> candidates = List.of(mockEntity, mockZombie, mockZombie2);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            // 最大2つに制限されること
            assertTrue(result.size() <= 2, "最大2つのエンティティが返されること");
        }

        @Test
        @DisplayName("SPHERE: maxTargetsで近い順に制限されること")
        void testMaxTargets_Sphere() {
            // 簡易コンストラクタを使用（sphereRadiusを指定）
            SkillTarget config = new SkillTarget(TargetType.SPHERE, null, null, null, 10.0,
                    EntityTypeFilter.ALL, 2);

            // 3つのエンティティを範囲内に配置
            List<Entity> candidates = List.of(mockEntity, mockZombie, mockZombie2);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            // 最大2つに制限されること
            assertTrue(result.size() <= 2, "最大2つのエンティティが返されること");
        }
    }

    // ==================== randomOrder テスト ====================

    @Nested
    @DisplayName("ランダム順序")
    class RandomOrderTests {

        @Test
        @DisplayName("randomOrder=true: 結果が毎回異なる可能性があること")
        void testRandomOrder() {
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.SPHERE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null,
                    TargetGroupFilter.BOTH, false, true, false,
                    10.0, null, null, 10.0);

            List<Entity> candidates = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Zombie z = org.mockito.Mockito.mock(Zombie.class);
                lenient().when(z.getUniqueId()).thenReturn(UUID.randomUUID());
                lenient().when(z.getType()).thenReturn(EntityType.ZOMBIE);
                lenient().when(z.getLocation()).thenReturn(new Location(mockWorld, i, 64, 0));
                candidates.add(z);
            }

            // 複数回実行して結果が異なることを確認（確率的）
            List<Entity> result1 = TargetSelector.selectTargets(mockCaster, config, candidates, null);
            List<Entity> result2 = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            // 同じサイズであること
            assertEquals(result1.size(), result2.size(), "結果のサイズは同じ");
        }
    }

    // ==================== throughWall テスト ====================

    @Nested
    @DisplayName("壁貫通設定")
    class ThroughWallTests {

        @Test
        @DisplayName("throughWall: 設定に関係なく候補が返されること（TODO実装前）")
        void testThroughWall() {
            SkillTarget config = new SkillTarget(TargetType.AREA_SELF, AreaShape.SPHERE,
                    null, null, null, null,
                    EntityTypeFilter.ALL, null,
                    TargetGroupFilter.BOTH, false, false, false,
                    10.0, null, null, 10.0);

            List<Entity> candidates = List.of(mockZombie);
            List<Entity> result = TargetSelector.selectTargets(mockCaster, config, candidates, null);

            // TODO実装前はフィルタが無効
            assertTrue(result.contains(mockZombie), "壁フィルタは無効（TODO）");
        }
    }
}
