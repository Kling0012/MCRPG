package com.example.rpgplugin.skill.component.trigger;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * トリガーコンポーネントのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class TriggerComponentTest {

    @Mock
    private Player mockPlayer;
    @Mock
    private Player mockKiller;
    @Mock
    private LivingEntity mockEntity;

    private TriggerSettings settings;

    @BeforeEach
    void setUp() {
        settings = new TriggerSettings();
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockEntity.getUniqueId()).thenReturn(UUID.randomUUID());
    }

    // ========== CastTrigger テスト ==========

    @Nested
    @DisplayName("CastTrigger: スキル使用時トリガー")
    class CastTriggerTests {
        private CastTrigger trigger;

        @BeforeEach
        void setUp() {
            trigger = new CastTrigger();
        }

        @Test
        @DisplayName("test: getKeyはCASTを返す")
        void testGetKey() {
            assertEquals("CAST", trigger.getKey());
        }

        @Test
        @DisplayName("test: getEventはEvent.classを返す")
        void testGetEvent() {
            assertEquals(org.bukkit.event.Event.class, trigger.getEvent());
        }

        @Test
        @DisplayName("test: shouldTriggerは常にtrue")
        void testShouldTriggerAlwaysTrue() {
            assertTrue(trigger.shouldTrigger(null, 1, settings));
        }

        @Test
        @DisplayName("test: getCasterはnullを返す")
        void testGetCasterReturnsNull() {
            assertNull(trigger.getCaster(null));
        }

        @Test
        @DisplayName("test: getTargetはnullを返す")
        void testGetTargetReturnsNull() {
            assertNull(trigger.getTarget(null, settings));
        }

        @Test
        @DisplayName("test: isEventlessはtrue")
        void testIsEventless() {
            assertTrue(trigger.isEventless());
        }
    }

    // ========== CrouchTrigger テスト ==========

    @Nested
    @DisplayName("CrouchTrigger: スニークトリガー")
    class CrouchTriggerTests {
        private CrouchTrigger trigger;
        private PlayerToggleSneakEvent event;

        @BeforeEach
        void setUp() {
            trigger = new CrouchTrigger();
            event = mock(PlayerToggleSneakEvent.class);
        }

        @Test
        @DisplayName("test: getKeyはCROUCHを返す")
        void testGetKey() {
            assertEquals("CROUCH", trigger.getKey());
        }

        @Test
        @DisplayName("test: start crouching時に発動")
        void testStartCrouching() {
            when(event.isSneaking()).thenReturn(true);
            settings.set("type", "start crouching");
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: stop crouching時に発動")
        void testStopCrouching() {
            when(event.isSneaking()).thenReturn(false);
            settings.set("type", "stop crouching");
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: both時に発動")
        void testBoth() {
            settings.set("type", "both");
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: start条件が不一致の場合は不发動")
        void testStartConditionMismatch() {
            when(event.isSneaking()).thenReturn(false);
            settings.set("type", "start crouching");
            assertFalse(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: デフォルト設定")
        void testDefaultSettings() {
            when(event.isSneaking()).thenReturn(true);
            // デフォルトは"start crouching"
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: getCasterはプレイヤーを返す")
        void testGetCaster() {
            when(event.getPlayer()).thenReturn(mockPlayer);
            assertEquals(mockPlayer, trigger.getCaster(event));
        }

        @Test
        @DisplayName("test: getTargetはプレイヤーを返す")
        void testGetTarget() {
            when(event.getPlayer()).thenReturn(mockPlayer);
            assertEquals(mockPlayer, trigger.getTarget(event, settings));
        }
    }

    // ========== DeathTrigger テスト ==========

    @Nested
    @DisplayName("DeathTrigger: 死亡トリガー")
    class DeathTriggerTests {
        private DeathTrigger trigger;
        private EntityDeathEvent event;

        @BeforeEach
        void setUp() {
            trigger = new DeathTrigger();
            event = mock(EntityDeathEvent.class);
            when(event.getEntity()).thenReturn(mockEntity);
        }

        @Test
        @DisplayName("test: getKeyはDEATHを返す")
        void testGetKey() {
            assertEquals("DEATH", trigger.getKey());
        }

        @Test
        @DisplayName("test: killer=false時は常に発動")
        void testAlwaysTriggersWithoutKiller() {
            settings.set("killer", false);
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: killer=trueでキラーがいる場合に発動")
        void testTriggersWithKiller() {
            when(mockEntity.getKiller()).thenReturn(mockKiller);
            settings.set("killer", true);
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: killer=trueでキラーがいない場合は不发動")
        void testNoTriggerWithoutKiller() {
            when(mockEntity.getKiller()).thenReturn(null);
            settings.set("killer", true);
            assertFalse(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: getCasterは死亡エンティティを返す")
        void testGetCaster() {
            assertEquals(mockEntity, trigger.getCaster(event));
        }

        @Test
        @DisplayName("test: getTargetはkiller=falseで死亡エンティティを返す")
        void testGetTargetWithoutKiller() {
            settings.set("killer", false);
            assertEquals(mockEntity, trigger.getTarget(event, settings));
        }

        @Test
        @DisplayName("test: getTargetはkiller=trueでキラーを返す")
        void testGetTargetWithKiller() {
            when(mockEntity.getKiller()).thenReturn(mockKiller);
            settings.set("killer", true);
            assertEquals(mockKiller, trigger.getTarget(event, settings));
        }
    }

    // ========== LandTrigger テスト ==========

    @Nested
    @DisplayName("LandTrigger: 着地トリガー")
    class LandTriggerTests {
        private com.example.rpgplugin.skill.component.trigger.LandTrigger trigger;
        private EntityDamageEvent event;
        private EntityDamageEvent nonFallEvent;

        @BeforeEach
        void setUp() {
            trigger = new com.example.rpgplugin.skill.component.trigger.LandTrigger();
            event = mock(EntityDamageEvent.class);
            nonFallEvent = mock(EntityDamageEvent.class);
            when(event.getEntity()).thenReturn(mockEntity);
            when(nonFallEvent.getEntity()).thenReturn(mockEntity);
        }

        @Test
        @DisplayName("test: getKeyはLANDを返す")
        void testGetKey() {
            assertEquals("LAND", trigger.getKey());
        }

        @Test
        @DisplayName("test: getEventはEntityDamageEvent.classを返す")
        void testGetEvent() {
            assertEquals(EntityDamageEvent.class, trigger.getEvent());
        }

        @Test
        @DisplayName("test: FALLダメージ時に発動")
        void testShouldTriggerWithFallDamage() {
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: 非FALLダメージ時は不发動")
        void testShouldNotTriggerWithNonFallDamage() {
            when(nonFallEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.ENTITY_ATTACK);
            assertFalse(trigger.shouldTrigger(nonFallEvent, 1, settings));
        }

        @Test
        @DisplayName("test: FIREダメージ時は不发動")
        void testShouldNotTriggerWithFireDamage() {
            when(nonFallEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FIRE);
            assertFalse(trigger.shouldTrigger(nonFallEvent, 1, settings));
        }

        @Test
        @DisplayName("test: setValuesでダメージを設定")
        void testSetValues() {
            when(event.getDamage()).thenReturn(5.0);
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            trigger.setValues(event, data);
            assertEquals(5.0, data.get("damage"));
        }

        @Test
        @DisplayName("test: getCasterはダメージを受けたエンティティ")
        void testGetCaster() {
            when(event.getEntity()).thenReturn(mockEntity);
            assertEquals(mockEntity, trigger.getCaster(event));
        }

        @Test
        @DisplayName("test: getTargetはダメージを受けたエンティティ")
        void testGetTarget() {
            when(event.getEntity()).thenReturn(mockEntity);
            assertEquals(mockEntity, trigger.getTarget(event, settings));
        }
    }

    // ========== KillTrigger テスト ==========

    @Nested
    @DisplayName("KillTrigger: キルトリガー")
    class KillTriggerTests {
        private com.example.rpgplugin.skill.component.trigger.KillTrigger trigger;
        private EntityDeathEvent event;

        @BeforeEach
        void setUp() {
            trigger = new com.example.rpgplugin.skill.component.trigger.KillTrigger();
            event = mock(EntityDeathEvent.class);
            when(event.getEntity()).thenReturn(mockEntity);
        }

        @Test
        @DisplayName("test: getKeyはKILLを返す")
        void testGetKey() {
            assertEquals("KILL", trigger.getKey());
        }

        @Test
        @DisplayName("test: getEventはEntityDeathEvent.classを返す")
        void testGetEvent() {
            assertEquals(EntityDeathEvent.class, trigger.getEvent());
        }

        @Test
        @DisplayName("test: キラーがいる場合に発動")
        void testShouldTriggerWithKiller() {
            when(mockEntity.getKiller()).thenReturn(mockPlayer);
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: キラーがいない場合は不发動")
        void testShouldNotTriggerWithoutKiller() {
            when(mockEntity.getKiller()).thenReturn(null);
            assertFalse(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: setValuesでvictimとvictim-typeを設定")
        void testSetValues() {
            when(event.getEntityType()).thenReturn(org.bukkit.entity.EntityType.ZOMBIE);
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            trigger.setValues(event, data);
            assertEquals(mockEntity, data.get("victim"));
            assertEquals("ZOMBIE", data.get("victim-type"));
        }

        @Test
        @DisplayName("test: getCasterはキラーを返す")
        void testGetCaster() {
            when(mockEntity.getKiller()).thenReturn(mockPlayer);
            assertEquals(mockPlayer, trigger.getCaster(event));
        }

        @Test
        @DisplayName("test: キラーがいない場合getCasterはnull")
        void testNoKillerReturnsNull() {
            when(mockEntity.getKiller()).thenReturn(null);
            assertNull(trigger.getCaster(event));
        }

        @Test
        @DisplayName("test: getTargetはキラーを返す")
        void testGetTarget() {
            when(mockEntity.getKiller()).thenReturn(mockPlayer);
            assertEquals(mockPlayer, trigger.getTarget(event, settings));
        }

        @Test
        @DisplayName("test: キラーがいない場合getTargetはnull")
        void testGetTargetWithoutKiller() {
            when(mockEntity.getKiller()).thenReturn(null);
            assertNull(trigger.getTarget(event, settings));
        }
    }

    // ========== LaunchTrigger テスト ==========

    @Nested
    @DisplayName("LaunchTrigger: 発射トリガー")
    class LaunchTriggerTests {
        private com.example.rpgplugin.skill.component.trigger.LaunchTrigger trigger;
        private ProjectileLaunchEvent event;
        private org.bukkit.entity.Projectile mockProjectile;

        @BeforeEach
        void setUp() {
            trigger = new com.example.rpgplugin.skill.component.trigger.LaunchTrigger();
            event = mock(ProjectileLaunchEvent.class);
            mockProjectile = mock(org.bukkit.entity.Projectile.class);
            when(event.getEntity()).thenReturn(mockProjectile);
        }

        @Test
        @DisplayName("test: getKeyはLAUNCHを返す")
        void testGetKey() {
            assertEquals("LAUNCH", trigger.getKey());
        }

        @Test
        @DisplayName("test: getEventはProjectileLaunchEvent.classを返す")
        void testGetEvent() {
            assertEquals(ProjectileLaunchEvent.class, trigger.getEvent());
        }

        @Test
        @DisplayName("test: LivingEntityが発射した場合に発動")
        void testShouldTriggerWithLivingEntityShooter() {
            when(mockProjectile.getShooter()).thenReturn(mockEntity);
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: Playerが発射した場合に発動")
        void testShouldTriggerWithPlayerShooter() {
            when(mockProjectile.getShooter()).thenReturn(mockPlayer);
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: LivingEntityでない発射者の場合は不发動")
        void testShouldNotTriggerWithNonLivingEntityShooter() {
            ProjectileSource mockBlock = mock(ProjectileSource.class);
            when(mockProjectile.getShooter()).thenReturn(mockBlock);
            assertFalse(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: 発射者がnullの場合は不发動")
        void testShouldNotTriggerWithNullShooter() {
            when(mockProjectile.getShooter()).thenReturn(null);
            assertFalse(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: setValuesでprojectile-typeとvelocityを設定")
        void testSetValues() {
            when(event.getEntityType()).thenReturn(org.bukkit.entity.EntityType.ARROW);
            org.bukkit.util.Vector velocity = new org.bukkit.util.Vector(1.0, 0.0, 0.0);
            when(mockProjectile.getVelocity()).thenReturn(velocity);
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            trigger.setValues(event, data);
            assertEquals("ARROW", data.get("projectile-type"));
            assertEquals(1.0, data.get("velocity"));
        }

        @Test
        @DisplayName("test: getCasterはLivingEntity発射者を返す")
        void testGetCasterWithLivingEntity() {
            when(mockProjectile.getShooter()).thenReturn(mockEntity);
            assertEquals(mockEntity, trigger.getCaster(event));
        }

        @Test
        @DisplayName("test: 発射者がLivingEntityでない場合getCasterはnull")
        void testGetCasterWithNonLivingEntity() {
            ProjectileSource mockBlock = mock(ProjectileSource.class);
            when(mockProjectile.getShooter()).thenReturn(mockBlock);
            assertNull(trigger.getCaster(event));
        }

        @Test
        @DisplayName("test: getTargetは発射者と同じ")
        void testGetTarget() {
            when(mockProjectile.getShooter()).thenReturn(mockEntity);
            assertEquals(mockEntity, trigger.getTarget(event, settings));
        }

        @Test
        @DisplayName("test: 発射者がnullの場合getTargetはnull")
        void testGetTargetWithNullShooter() {
            when(mockProjectile.getShooter()).thenReturn(null);
            assertNull(trigger.getTarget(event, settings));
        }
    }

    // ========== EnvironmentalTrigger テスト ==========

    @Nested
    @DisplayName("EnvironmentalTrigger: 環境トリガー")
    class EnvironmentalTriggerTests {
        private com.example.rpgplugin.skill.component.trigger.EnvironmentalTrigger trigger;
        private EntityDamageEvent event;
        private EntityDamageByEntityEvent damageByEntityEvent;

        @BeforeEach
        void setUp() {
            trigger = new com.example.rpgplugin.skill.component.trigger.EnvironmentalTrigger();
            event = mock(EntityDamageEvent.class);
            damageByEntityEvent = mock(EntityDamageByEntityEvent.class);
            when(event.getEntity()).thenReturn(mockEntity);
            when(damageByEntityEvent.getEntity()).thenReturn(mockEntity);
        }

        @Test
        @DisplayName("test: getKeyはENVIRONMENTALを返す")
        void testGetKey() {
            assertEquals("ENVIRONMENTAL", trigger.getKey());
        }

        @Test
        @DisplayName("test: getEventはEntityDamageEvent.classを返す")
        void testGetEvent() {
            assertEquals(EntityDamageEvent.class, trigger.getEvent());
        }

        @Test
        @DisplayName("test: EntityDamageByEntityEventは不发動")
        void testShouldNotTriggerWithEntityDamageByEntityEvent() {
            assertFalse(trigger.shouldTrigger(damageByEntityEvent, 1, settings));
        }

        @Test
        @DisplayName("test: LivingEntityの環境ダメージで発動")
        void testShouldTriggerWithLivingEntityEnvironmentalDamage() {
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: type=fallでFALLダメージ時に発動")
        void testShouldTriggerWithTypeFall() {
            settings.set("type", "fall");
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: type=fireでFIREダメージ時に発動")
        void testShouldTriggerWithTypeFire() {
            settings.set("type", "fire");
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.FIRE);
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: type=fallでFIREダメージ時は不发動")
        void testShouldNotTriggerWithTypeFallFireDamage() {
            settings.set("type", "fall");
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.FIRE);
            assertFalse(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: type=anyですべての環境ダメージで発動")
        void testShouldTriggerWithAnyType() {
            settings.set("type", "any");
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.LAVA);
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: typeの大文字小文字は区別されない")
        void testTypeCaseInsensitive() {
            settings.set("type", "FIRE");
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.FIRE);
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: setValuesでdamageとcauseを設定")
        void testSetValues() {
            when(event.getDamage()).thenReturn(3.5);
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.DROWNING);
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            trigger.setValues(event, data);
            assertEquals(3.5, data.get("damage"));
            assertEquals("DROWNING", data.get("cause"));
        }

        @Test
        @DisplayName("test: getCasterはLivingEntityを返す")
        void testGetCasterWithLivingEntity() {
            when(event.getEntity()).thenReturn(mockEntity);
            assertEquals(mockEntity, trigger.getCaster(event));
        }

        @Test
        @DisplayName("test: エンティティがLivingEntityでない場合getCasterはnull")
        void testGetCasterWithNonLivingEntity() {
            Entity nonLiving = mock(Entity.class);
            when(event.getEntity()).thenReturn(nonLiving);
            assertNull(trigger.getCaster(event));
        }

        @Test
        @DisplayName("test: getTargetはキャスターと同じ")
        void testGetTarget() {
            when(event.getEntity()).thenReturn(mockEntity);
            assertEquals(mockEntity, trigger.getTarget(event, settings));
        }

        @Test
        @DisplayName("test: 部分文字列マッチでtype判定")
        void testTypePartialMatch() {
            settings.set("type", "fir");
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.FIRE);
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }
    }

    // ========== PhysicalDealtTrigger テスト ==========

    @Nested
    @DisplayName("PhysicalDealtTrigger: 物理ダメージ与えた")
    class PhysicalDealtTriggerTests {
        private com.example.rpgplugin.skill.component.trigger.PhysicalDealtTrigger trigger;
        private EntityDamageByEntityEvent event;
        private org.bukkit.entity.Projectile mockProjectile;

        @BeforeEach
        void setUp() {
            trigger = new com.example.rpgplugin.skill.component.trigger.PhysicalDealtTrigger();
            event = mock(EntityDamageByEntityEvent.class);
            mockProjectile = mock(org.bukkit.entity.Projectile.class);
            when(event.getDamager()).thenReturn(mockEntity);
        }

        @Test
        @DisplayName("test: getKeyはPHYSICAL_DEALTを返す")
        void testGetKey() {
            assertEquals("PHYSICAL_DEALT", trigger.getKey());
        }

        @Test
        @DisplayName("test: getEventはEntityDamageByEntityEvent.classを返す")
        void testGetEvent() {
            assertEquals(EntityDamageByEntityEvent.class, trigger.getEvent());
        }

        @Test
        @DisplayName("test: LivingEntityがダメージを与えた場合に発動")
        void testShouldTriggerWithLivingEntity() {
            when(event.getDamager()).thenReturn(mockEntity);
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: Projectileがダメージを与えた場合は不发動")
        void testShouldNotTriggerWithProjectile() {
            when(event.getDamager()).thenReturn(mockProjectile);
            assertFalse(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: setValuesでdamageとdamage-causeを設定")
        void testSetValues() {
            when(event.getDamage()).thenReturn(10.5);
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.ENTITY_ATTACK);
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            trigger.setValues(event, data);
            assertEquals(10.5, data.get("damage"));
            assertEquals("ENTITY_ATTACK", data.get("damage-cause"));
        }

        @Test
        @DisplayName("test: getCasterはダメージを与えたエンティティ")
        void testGetCaster() {
            assertEquals(mockEntity, trigger.getCaster(event));
        }

        @Test
        @DisplayName("test: getTargetはtarget=victimでダメージを受けたエンティティ")
        void testGetTargetVictim() {
            settings.set("target", "victim");
            when(event.getEntity()).thenReturn(mockEntity);
            assertEquals(mockEntity, trigger.getTarget(event, settings));
        }

        @Test
        @DisplayName("test: getTargetはtarget=victimで非LivingEntityの場合はnull")
        void testGetTargetVictimNonLiving() {
            settings.set("target", "victim");
            Entity nonLiving = mock(Entity.class);
            when(event.getEntity()).thenReturn(nonLiving);
            assertNull(trigger.getTarget(event, settings));
        }

        @Test
        @DisplayName("test: getTargetはtarget=selfでダメージを与えたエンティティ")
        void testGetTargetSelf() {
            assertEquals(mockEntity, trigger.getTarget(event, settings));
        }
    }

    // ========== PhysicalTakenTrigger テスト ==========

    @Nested
    @DisplayName("PhysicalTakenTrigger: 物理ダメージ受けた")
    class PhysicalTakenTriggerTests {
        private com.example.rpgplugin.skill.component.trigger.PhysicalTakenTrigger trigger;
        private EntityDamageByEntityEvent event;

        @BeforeEach
        void setUp() {
            trigger = new com.example.rpgplugin.skill.component.trigger.PhysicalTakenTrigger();
            event = mock(EntityDamageByEntityEvent.class);
            when(event.getEntity()).thenReturn(mockEntity);
        }

        @Test
        @DisplayName("test: getKeyはPHYSICAL_TAKENを返す")
        void testGetKey() {
            assertEquals("PHYSICAL_TAKEN", trigger.getKey());
        }

        @Test
        @DisplayName("test: getEventはEntityDamageByEntityEvent.classを返す")
        void testGetEvent() {
            assertEquals(EntityDamageByEntityEvent.class, trigger.getEvent());
        }

        @Test
        @DisplayName("test: LivingEntityがダメージを受けた場合に発動")
        void testShouldTriggerWithLivingEntity() {
            when(event.getEntity()).thenReturn(mockEntity);
            assertTrue(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: 非LivingEntityがダメージを受けた場合は不发動")
        void testShouldNotTriggerWithNonLivingEntity() {
            Entity nonLiving = mock(Entity.class);
            when(event.getEntity()).thenReturn(nonLiving);
            assertFalse(trigger.shouldTrigger(event, 1, settings));
        }

        @Test
        @DisplayName("test: setValuesでdamage、damage-cause、attackerを設定")
        void testSetValues() {
            when(event.getDamage()).thenReturn(8.5);
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.ENTITY_ATTACK);
            when(event.getDamager()).thenReturn(mockPlayer);
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            trigger.setValues(event, data);
            assertEquals(8.5, data.get("damage"));
            assertEquals("ENTITY_ATTACK", data.get("damage-cause"));
            assertEquals(mockPlayer, data.get("attacker"));
        }

        @Test
        @DisplayName("test: getCasterはダメージを受けたエンティティ")
        void testGetCaster() {
            assertEquals(mockEntity, trigger.getCaster(event));
        }

        @Test
        @DisplayName("test: 非LivingEntityがダメージを受けた場合getCasterはnull")
        void testGetCasterNonLivingEntity() {
            Entity nonLiving = mock(Entity.class);
            when(event.getEntity()).thenReturn(nonLiving);
            assertNull(trigger.getCaster(event));
        }

        @Test
        @DisplayName("test: getTargetはtarget=attackerでダメージを与えたエンティティ")
        void testGetTargetAttacker() {
            settings.set("target", "attacker");
            when(event.getDamager()).thenReturn(mockPlayer);
            assertEquals(mockPlayer, trigger.getTarget(event, settings));
        }

        @Test
        @DisplayName("test: getTargetはtarget=attackerで非LivingEntityの場合はnull")
        void testGetTargetAttackerNonLiving() {
            settings.set("target", "attacker");
            Entity nonLiving = mock(Entity.class);
            when(event.getDamager()).thenReturn(nonLiving);
            assertNull(trigger.getTarget(event, settings));
        }

        @Test
        @DisplayName("test: getTargetはtarget=selfでダメージを受けたエンティティ")
        void testGetTargetSelf() {
            assertEquals(mockEntity, trigger.getTarget(event, settings));
        }

        @Test
        @DisplayName("test: target設定の大文字小文字は区別されない")
        void testTargetCaseInsensitive() {
            settings.set("target", "ATTACKER");
            when(event.getDamager()).thenReturn(mockPlayer);
            assertEquals(mockPlayer, trigger.getTarget(event, settings));
        }
    }

    // ========== TriggerSettings テスト ==========

    @Nested
    @DisplayName("TriggerSettings: トリガー設定")
    class TriggerSettingsTests {

        @Test
        @DisplayName("test: getStringでデフォルト値")
        void testGetStringDefault() {
            assertEquals("default", settings.getString("key", "default"));
        }

        @Test
        @DisplayName("test: getStringで設定値")
        void testGetStringValue() {
            settings.set("key", "value");
            assertEquals("value", settings.getString("key", "default"));
        }

        @Test
        @DisplayName("test: getBooleanでデフォルト値")
        void testGetBooleanDefault() {
            assertFalse(settings.getBoolean("key", false));
            assertTrue(settings.getBoolean("key", true));
        }

        @Test
        @DisplayName("test: getBooleanで設定値")
        void testGetBooleanValue() {
            settings.set("key", true);
            assertTrue(settings.getBoolean("key", false));
        }

        @Test
        @DisplayName("test: getDoubleでデフォルト値")
        void testGetDoubleDefault() {
            assertEquals(10.0, settings.getDouble("key", 10.0));
        }

        @Test
        @DisplayName("test: getDoubleで設定値")
        void testGetDoubleValue() {
            settings.set("key", "5.5");
            assertEquals(5.5, settings.getDouble("key", 10.0));
        }

        @Test
        @DisplayName("test: getIntでデフォルト値")
        void testGetIntDefault() {
            assertEquals(5, settings.getInt("key", 5));
        }

        @Test
        @DisplayName("test: getIntで設定値")
        void testGetIntValue() {
            settings.set("key", "10");
            assertEquals(10, settings.getInt("key", 5));
        }

        @Test
        @DisplayName("test: hasで存在確認")
        void testHas() {
            assertFalse(settings.has("key"));
            settings.set("key", "value");
            assertTrue(settings.has("key"));
        }

        @Test
        @DisplayName("test: setとputで値を設定")
        void testSetAndPut() {
            settings.set("key1", "value1");
            assertEquals("value1", settings.getString("key1", null));
            settings.put("key2", "value2");
            assertEquals("value2", settings.getString("key2", null));
        }

        @Test
        @DisplayName("test: clearでクリア")
        void testClear() {
            settings.set("key", "value");
            assertTrue(settings.has("key"));
            settings.clear();
            assertFalse(settings.has("key"));
        }

        @Test
        @DisplayName("test: getKeysでキーセット取得")
        void testGetKeys() {
            settings.set("key1", "value1");
            settings.set("key2", "value2");
            assertEquals(2, settings.getKeys().size());
            assertTrue(settings.getKeys().contains("key1"));
            assertTrue(settings.getKeys().contains("key2"));
        }
    }

    // ========== ComponentRegistry テスト ==========

    @Nested
    @DisplayName("ComponentRegistry: コンポーネント登録")
    class ComponentRegistryTests {

        @Test
        @DisplayName("test: CASTトリガーを取得")
        void testGetCastTrigger() {
            var trigger = com.example.rpgplugin.skill.component.trigger.ComponentRegistry.createTrigger("CAST");
            assertNotNull(trigger);
            assertTrue(trigger instanceof CastTrigger);
            assertEquals("CAST", trigger.getKey());
        }

        @Test
        @DisplayName("test: CROUCHトリガーを取得")
        void testGetCrouchTrigger() {
            var trigger = com.example.rpgplugin.skill.component.trigger.ComponentRegistry.createTrigger("CROUCH");
            assertNotNull(trigger);
            assertTrue(trigger instanceof CrouchTrigger);
        }

        @Test
        @DisplayName("test: DEATHトリガーを取得")
        void testGetDeathTrigger() {
            var trigger = com.example.rpgplugin.skill.component.trigger.ComponentRegistry.createTrigger("DEATH");
            assertNotNull(trigger);
            assertTrue(trigger instanceof DeathTrigger);
        }

        @Test
        @DisplayName("test: LANDトリガーを取得")
        void testGetLandTrigger() {
            var trigger = com.example.rpgplugin.skill.component.trigger.ComponentRegistry.createTrigger("LAND");
            assertNotNull(trigger);
            assertTrue(trigger instanceof com.example.rpgplugin.skill.component.trigger.LandTrigger);
        }

        @Test
        @DisplayName("test: KILLトリガーを取得")
        void testGetKillTrigger() {
            var trigger = com.example.rpgplugin.skill.component.trigger.ComponentRegistry.createTrigger("KILL");
            assertNotNull(trigger);
            assertTrue(trigger instanceof com.example.rpgplugin.skill.component.trigger.KillTrigger);
        }

        @Test
        @DisplayName("test: 存在しないトリガーはnull")
        void testGetNonExistentTrigger() {
            var trigger = com.example.rpgplugin.skill.component.trigger.ComponentRegistry.createTrigger("NONEXISTENT");
            assertNull(trigger);
        }

        @Test
        @DisplayName("test: hasTriggerで存在確認")
        void testHasTrigger() {
            assertTrue(com.example.rpgplugin.skill.component.trigger.ComponentRegistry.hasTrigger("CAST"));
            assertFalse(com.example.rpgplugin.skill.component.trigger.ComponentRegistry.hasTrigger("NONEXISTENT"));
        }

        @Test
        @DisplayName("test: getTriggerKeysでキー一覧取得")
        void testGetTriggerKeys() {
            var keys = com.example.rpgplugin.skill.component.trigger.ComponentRegistry.getTriggerKeys();
            assertTrue(keys.contains("CAST"));
            assertTrue(keys.contains("CROUCH"));
            assertTrue(keys.contains("DEATH"));
        }
    }
}
