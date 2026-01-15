package com.example.rpgplugin.damage;

import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DamageTrackerのユニットテスト
 *
 * <p>Paper 1.20.6でEntityDeathEvent.getKiller()が削除されたため、
 * ダメージイベントを監視してキラーを特定する機能をテストします。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DamageTracker テスト")
class DamageTrackerTest {

    private DamageTracker damageTracker;

    @Mock
    private Server mockServer;

    @Mock
    private Player mockPlayer;

    @Mock
    private Player mockKiller;

    @Mock
    private LivingEntity mockLivingEntity;

    @Mock
    private Projectile mockProjectile;

    @Mock
    private org.bukkit.entity.AreaEffectCloud mockCloud;

    private UUID playerUuid;
    private UUID entityUuid;
    private UUID killerUuid;

    @BeforeEach
    void setUp() {
        damageTracker = new DamageTracker();
        playerUuid = UUID.randomUUID();
        entityUuid = UUID.randomUUID();
        killerUuid = UUID.randomUUID();

        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockLivingEntity.getUniqueId()).thenReturn(entityUuid);
        when(mockKiller.getUniqueId()).thenReturn(killerUuid);
        when(mockLivingEntity.getServer()).thenReturn(mockServer);
    }

    // ==================== getKiller(Entity) テスト ====================

    @Nested
    @DisplayName("getKiller(Entity) テスト")
    class GetKillerByEntityTests {

        @Test
        @DisplayName("nullエンティティの場合はnullを返す")
        void getKiller_NullEntity_ReturnsNull() {
            Player result = damageTracker.getKiller((Entity) null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("ダメージ記録がない場合はnullを返す")
        void getKiller_NoDamageRecord_ReturnsNull() {
            Player result = damageTracker.getKiller(mockLivingEntity);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("記録されたキラーがオンラインの場合はそのプレイヤーを返す")
        void getKiller_KillerOnline_ReturnsKiller() {
            when(mockServer.getPlayer(killerUuid)).thenReturn(mockKiller);

            setLastDamager(entityUuid, killerUuid);

            Player result = damageTracker.getKiller(mockLivingEntity);
            assertThat(result).isSameAs(mockKiller);
        }

        @Test
        @DisplayName("記録されたキラーがオフラインの場合はnullを返す")
        void getKiller_KillerOffline_ReturnsNull() {
            when(mockServer.getPlayer(killerUuid)).thenReturn(null);

            setLastDamager(entityUuid, killerUuid);

            Player result = damageTracker.getKiller(mockLivingEntity);
            assertThat(result).isNull();
        }
    }

    // ==================== getKiller(EntityDeathEvent) テスト ====================

    @Nested
    @DisplayName("getKiller(EntityDeathEvent) テスト")
    class GetKillerByEventTests {

        @Mock
        private EntityDeathEvent mockEvent;

        @Test
        @DisplayName("イベントからエンティティを取得してキラーを返す")
        void getKiller_FromEvent_ReturnsKiller() {
            when(mockEvent.getEntity()).thenReturn(mockLivingEntity);
            when(mockServer.getPlayer(killerUuid)).thenReturn(mockKiller);

            setLastDamager(entityUuid, killerUuid);

            Player result = damageTracker.getKiller(mockEvent);
            assertThat(result).isSameAs(mockKiller);
        }

        @Test
        @DisplayName("イベントのエンティティがnullの場合はnullを返す")
        void getKiller_NullEventEntity_ReturnsNull() {
            when(mockEvent.getEntity()).thenReturn(null);

            Player result = damageTracker.getKiller(mockEvent);
            assertThat(result).isNull();
        }
    }

    // ==================== isKiller テスト ====================

    @Nested
    @DisplayName("isKiller テスト")
    class IsKillerTests {

        @Test
        @DisplayName("エンティティがnullの場合はfalse")
        void isKiller_NullEntity_ReturnsFalse() {
            boolean result = damageTracker.isKiller(null, mockPlayer);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("プレイヤーがnullの場合はfalse")
        void isKiller_NullPlayer_ReturnsFalse() {
            boolean result = damageTracker.isKiller(mockLivingEntity, null);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("両方nullの場合はfalse")
        void isKiller_BothNull_ReturnsFalse() {
            boolean result = damageTracker.isKiller(null, null);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("記録されたキラーと一致する場合はtrue")
        void isKiller_MatchingKiller_ReturnsTrue() {
            when(mockPlayer.getUniqueId()).thenReturn(killerUuid);

            setLastDamager(entityUuid, killerUuid);

            boolean result = damageTracker.isKiller(mockLivingEntity, mockPlayer);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("記録されたキラーと異なる場合はfalse")
        void isKiller_DifferentKiller_ReturnsFalse() {
            UUID differentUuid = UUID.randomUUID();
            when(mockPlayer.getUniqueId()).thenReturn(differentUuid);

            setLastDamager(entityUuid, killerUuid);

            boolean result = damageTracker.isKiller(mockLivingEntity, mockPlayer);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("ダメージ記録がない場合はfalse")
        void isKiller_NoRecord_ReturnsFalse() {
            when(mockPlayer.getUniqueId()).thenReturn(killerUuid);

            boolean result = damageTracker.isKiller(mockLivingEntity, mockPlayer);
            assertThat(result).isFalse();
        }
    }

    // ==================== clearDamageRecord テスト ====================

    @Nested
    @DisplayName("clearDamageRecord テスト")
    class ClearDamageRecordTests {

        @Test
        @DisplayName("nullエンティティの場合は例外を投げない")
        void clearDamageRecord_NullEntity_NoException() {
            assertThatCode(() -> damageTracker.clearDamageRecord(null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("記録をクリアできる")
        void clearDamageRecord_ValidEntity_ClearsRecord() {
            setLastDamager(entityUuid, killerUuid);

            damageTracker.clearDamageRecord(mockLivingEntity);

            when(mockServer.getPlayer(killerUuid)).thenReturn(mockKiller);
            Player result = damageTracker.getKiller(mockLivingEntity);
            assertThat(result).isNull(); // クリアされたのでnull
        }
    }

    // ==================== onEntityDamageByEntity テスト ====================

    @Nested
    @DisplayName("onEntityDamageByEntity イベントハンドラーテスト")
    class OnEntityDamageByEntityTests {

        @Mock
        private EntityDamageByEntityEvent mockEvent;

        @Test
        @DisplayName("プレイヤーがダメージを受けた場合は記録しない")
        void onEntityDamageByEntity_DamagedIsPlayer_NoRecord() {
            when(mockEvent.getEntity()).thenReturn(mockPlayer);
            when(mockEvent.getDamager()).thenReturn(mockLivingEntity);

            damageTracker.onEntityDamageByEntity(mockEvent);

            when(mockServer.getPlayer(any(UUID.class))).thenReturn(mockKiller);
            Player result = damageTracker.getKiller(mockPlayer);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("プレイヤーが直接攻撃した場合に記録する")
        void onEntityDamageByEntity_PlayerDirectAttack_RecordsKiller() {
            when(mockEvent.getEntity()).thenReturn(mockLivingEntity);
            when(mockEvent.getDamager()).thenReturn(mockKiller);

            damageTracker.onEntityDamageByEntity(mockEvent);

            when(mockServer.getPlayer(killerUuid)).thenReturn(mockKiller);
            Player result = damageTracker.getKiller(mockLivingEntity);
            assertThat(result).isSameAs(mockKiller);
        }

        @Test
        @DisplayName("投射物の発射元がプレイヤーの場合に記録する")
        void onEntityDamageByEntity_ProjectileFromPlayer_RecordsKiller() {
            when(mockEvent.getEntity()).thenReturn(mockLivingEntity);
            when(mockEvent.getDamager()).thenReturn(mockProjectile);
            when(mockProjectile.getShooter()).thenReturn((ProjectileSource) mockKiller);

            damageTracker.onEntityDamageByEntity(mockEvent);

            when(mockServer.getPlayer(killerUuid)).thenReturn(mockKiller);
            Player result = damageTracker.getKiller(mockLivingEntity);
            assertThat(result).isSameAs(mockKiller);
        }

        @Test
        @DisplayName("投射物の発射元がプレイヤーでない場合は記録しない")
        void onEntityDamageByEntity_ProjectileFromNonPlayer_NoRecord() {
            ProjectileSource nonPlayer = mock(ProjectileSource.class);
            when(mockEvent.getEntity()).thenReturn(mockLivingEntity);
            when(mockEvent.getDamager()).thenReturn(mockProjectile);
            when(mockProjectile.getShooter()).thenReturn(nonPlayer);

            damageTracker.onEntityDamageByEntity(mockEvent);

            Player result = damageTracker.getKiller(mockLivingEntity);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("投射物の発射元がnullの場合は記録しない")
        void onEntityDamageByEntity_ProjectileNoShooter_NoRecord() {
            when(mockEvent.getEntity()).thenReturn(mockLivingEntity);
            when(mockEvent.getDamager()).thenReturn(mockProjectile);
            when(mockProjectile.getShooter()).thenReturn(null);

            damageTracker.onEntityDamageByEntity(mockEvent);

            Player result = damageTracker.getKiller(mockLivingEntity);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("残留ポーションのソースがプレイヤーの場合に記録する")
        void onEntityDamageByEntity_CloudFromPlayer_RecordsKiller() {
            when(mockEvent.getEntity()).thenReturn(mockLivingEntity);
            when(mockEvent.getDamager()).thenReturn(mockCloud);
            when(mockCloud.getSource()).thenReturn((ProjectileSource) mockKiller);

            damageTracker.onEntityDamageByEntity(mockEvent);

            when(mockServer.getPlayer(killerUuid)).thenReturn(mockKiller);
            Player result = damageTracker.getKiller(mockLivingEntity);
            assertThat(result).isSameAs(mockKiller);
        }

        @Test
        @DisplayName("残留ポーションのソースがプレイヤーでない場合は記録しない")
        void onEntityDamageByEntity_CloudFromNonPlayer_NoRecord() {
            ProjectileSource nonPlayer = mock(ProjectileSource.class);
            when(mockEvent.getEntity()).thenReturn(mockLivingEntity);
            when(mockEvent.getDamager()).thenReturn(mockCloud);
            when(mockCloud.getSource()).thenReturn(nonPlayer);

            damageTracker.onEntityDamageByEntity(mockEvent);

            Player result = damageTracker.getKiller(mockLivingEntity);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("後の攻撃でキラーを上書きする")
        void onEntityDamageByEntity_MultipleAttackers_OverwritesKiller() {
            UUID firstKillerUuid = UUID.randomUUID();
            UUID secondKillerUuid = UUID.randomUUID();
            Player firstKiller = mock(Player.class);
            Player secondKiller = mock(Player.class);

            when(firstKiller.getUniqueId()).thenReturn(firstKillerUuid);
            when(secondKiller.getUniqueId()).thenReturn(secondKillerUuid);

            when(mockEvent.getEntity()).thenReturn(mockLivingEntity);

            // 最初の攻撃者
            when(mockEvent.getDamager()).thenReturn(firstKiller);
            damageTracker.onEntityDamageByEntity(mockEvent);

            // 2人目の攻撃者（上書き）
            when(mockEvent.getDamager()).thenReturn(secondKiller);
            damageTracker.onEntityDamageByEntity(mockEvent);

            when(mockServer.getPlayer(firstKillerUuid)).thenReturn(firstKiller);
            when(mockServer.getPlayer(secondKillerUuid)).thenReturn(secondKiller);

            boolean isFirstKiller = damageTracker.isKiller(mockLivingEntity, firstKiller);
            boolean isSecondKiller = damageTracker.isKiller(mockLivingEntity, secondKiller);

            assertThat(isFirstKiller).isFalse();
            assertThat(isSecondKiller).isTrue();
        }
    }

    // ==================== onEntityDeath テスト ====================

    @Nested
    @DisplayName("onEntityDeath イベントハンドラーテスト")
    class OnEntityDeathTests {

        @Mock
        private EntityDeathEvent mockEvent;

        @Test
        @DisplayName("死亡時にダメージ記録をクリアする")
        void onEntityDeath_ClearsRecord() {
            when(mockEvent.getEntity()).thenReturn(mockLivingEntity);
            setLastDamager(entityUuid, killerUuid);

            damageTracker.onEntityDeath(mockEvent);

            when(mockServer.getPlayer(killerUuid)).thenReturn(mockKiller);
            Player result = damageTracker.getKiller(mockLivingEntity);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("死亡イベントで例外を投げない")
        void onEntityDeath_NoException() {
            when(mockEvent.getEntity()).thenReturn(mockLivingEntity);

            assertThatCode(() -> damageTracker.onEntityDeath(mockEvent))
                    .doesNotThrowAnyException();
        }
    }

    // ==================== 複合テスト ====================

    @Nested
    @DisplayName("複合シナリオテスト")
    class CombinedScenarioTests {

        @Mock
        private EntityDamageByEntityEvent mockDamageEvent;

        @Mock
        private EntityDeathEvent mockDeathEvent;

        @Test
        @DisplayName("ダメージ→死亡の正常フロー")
        void damageThenDeath_NormalFlow() {
            // セットアップ
            when(mockDamageEvent.getEntity()).thenReturn(mockLivingEntity);
            when(mockDamageEvent.getDamager()).thenReturn(mockKiller);
            when(mockDeathEvent.getEntity()).thenReturn(mockLivingEntity);
            when(mockServer.getPlayer(killerUuid)).thenReturn(mockKiller);

            // ダメージイベント
            damageTracker.onEntityDamageByEntity(mockDamageEvent);
            Player killerAfterDamage = damageTracker.getKiller(mockLivingEntity);
            assertThat(killerAfterDamage).isSameAs(mockKiller);

            // 死亡イベント
            damageTracker.onEntityDeath(mockDeathEvent);
            Player killerAfterDeath = damageTracker.getKiller(mockLivingEntity);
            assertThat(killerAfterDeath).isNull();
        }

        @Test
        @DisplayName("複数エンティティの個別追跡")
        void multipleEntities_TrackedSeparately() {
            LivingEntity entity1 = mock(LivingEntity.class);
            LivingEntity entity2 = mock(LivingEntity.class);
            UUID uuid1 = UUID.randomUUID();
            UUID uuid2 = UUID.randomUUID();

            when(entity1.getUniqueId()).thenReturn(uuid1);
            when(entity2.getUniqueId()).thenReturn(uuid2);
            when(entity1.getServer()).thenReturn(mockServer);
            when(entity2.getServer()).thenReturn(mockServer);

            Player killer1 = mock(Player.class);
            Player killer2 = mock(Player.class);
            UUID killer1Uuid = UUID.randomUUID();
            UUID killer2Uuid = UUID.randomUUID();

            when(killer1.getUniqueId()).thenReturn(killer1Uuid);
            when(killer2.getUniqueId()).thenReturn(killer2Uuid);

            setLastDamager(uuid1, killer1Uuid);
            setLastDamager(uuid2, killer2Uuid);

            // UUIDを明示的にキャストして呼び出し
            when(mockServer.getPlayer(killer1Uuid)).thenReturn(killer1);
            when(mockServer.getPlayer(killer2Uuid)).thenReturn(killer2);

            assertThat(damageTracker.isKiller(entity1, killer1)).isTrue();
            assertThat(damageTracker.isKiller(entity2, killer2)).isTrue();
            assertThat(damageTracker.isKiller(entity1, killer2)).isFalse();
            assertThat(damageTracker.isKiller(entity2, killer1)).isFalse();
        }
    }

    // ==================== ヘルパーメソッド ====================

    /**
     * リフレクションでlastDamagerマップに値を設定する
     */
    private void setLastDamager(UUID entityUuid, UUID killerUuid) {
        try {
            var field = DamageTracker.class.getDeclaredField("lastDamager");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            var map = (java.util.Map<UUID, UUID>) field.get(damageTracker);
            map.put(entityUuid, killerUuid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
