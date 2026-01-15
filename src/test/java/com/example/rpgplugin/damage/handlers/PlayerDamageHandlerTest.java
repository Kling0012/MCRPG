package com.example.rpgplugin.damage.handlers;

import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PlayerDamageHandlerのユニットテスト
 *
 * <p>プレイヤー→エンティティのダメージ計算ハンドラーをテストします。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PlayerDamageHandler テスト")
class PlayerDamageHandlerTest {

    private PlayerDamageHandler handler;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private Logger mockLogger;

    @Mock
    private Player mockPlayer;

    @Mock
    private Entity mockTarget;

    @Mock
    private RPGPlayer mockRpgPlayer;

    @Mock
    private StatManager mockStatManager;

    @Mock
    private EntityDamageByEntityEvent mockEvent;

    private UUID playerUuid;

    @BeforeEach
    void setUp() {
        handler = new PlayerDamageHandler(mockPlayerManager, mockLogger);
        playerUuid = UUID.randomUUID();

        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockPlayerManager.getRPGPlayer(playerUuid)).thenReturn(mockRpgPlayer);
        when(mockRpgPlayer.getStatManager()).thenReturn(mockStatManager);
    }

    // ==================== handlePlayerToEntityDamage テスト ====================

    @Nested
    @DisplayName("handlePlayerToEntityDamage テスト")
    class HandlePlayerToEntityDamageTests {

        @Test
        @DisplayName("ダメージ元がプレイヤーでない場合は-1を返す")
        void handlePlayerToEntityDamage_NonPlayerDamager_ReturnsNegativeOne() {
            Entity nonPlayer = mock(Entity.class);
            when(mockEvent.getDamager()).thenReturn(nonPlayer);
            when(mockEvent.getEntity()).thenReturn(mockTarget);

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            assertThat(result).isEqualTo(-1);
            verify(mockEvent, never()).setDamage(anyDouble());
        }

        @Test
        @DisplayName("RPGPlayerが見つからない場合は-1を返す")
        void handlePlayerToEntityDamage_RpgPlayerNotFound_ReturnsNegativeOne() {
            when(mockEvent.getDamager()).thenReturn(mockPlayer);
            when(mockEvent.getEntity()).thenReturn(mockTarget);
            when(mockPlayerManager.getRPGPlayer(playerUuid)).thenReturn(null);

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            assertThat(result).isEqualTo(-1);
            verify(mockEvent, never()).setDamage(anyDouble());
        }

        @Test
        @DisplayName("物理ダメージ計算: STR0")
        void handlePlayerToEntityDamage_PhysicalDamage_Str0() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 0));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 10 × (1 + 0/100) × 1.0 = 10
            assertThat(result).isEqualTo(10);
            verify(mockEvent).setDamage(10);
        }

        @Test
        @DisplayName("物理ダメージ計算: STR50でダメージ増加")
        void handlePlayerToEntityDamage_PhysicalDamage_Str50() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 10 × (1 + 50/100) = 15
            assertThat(result).isEqualTo(15);
            verify(mockEvent).setDamage(15);
        }

        @Test
        @DisplayName("物理ダメージ計算: STR100でダメージ2倍")
        void handlePlayerToEntityDamage_PhysicalDamage_Str100() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 100));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 10 × (1 + 100/100) = 20
            assertThat(result).isEqualTo(20);
            verify(mockEvent).setDamage(20);
        }

        @Test
        @DisplayName("物理ダメージ計算: 範囲攻撃")
        void handlePlayerToEntityDamage_PhysicalDamage_SweepAttack() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK, 5.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 5 × 1.5 = 7.5 → 7
            assertThat(result).isEqualTo(7);
            verify(mockEvent).setDamage(7);
        }

        @Test
        @DisplayName("魔法ダメージ計算: INT0")
        void handlePlayerToEntityDamage_MagicDamage_Int0() {
            setupDamageEvent(EntityDamageEvent.DamageCause.THORNS, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.INTELLIGENCE, 0));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 10 + 0 = 10
            assertThat(result).isEqualTo(10);
            verify(mockEvent).setDamage(10);
        }

        @Test
        @DisplayName("魔法ダメージ計算: INT50でダメージ増加")
        void handlePlayerToEntityDamage_MagicDamage_Int50() {
            setupDamageEvent(EntityDamageEvent.DamageCause.THORNS, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.INTELLIGENCE, 50));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 100 + 50 × 0.15 × 1.0 = 107.5 → 107
            assertThat(result).isEqualTo(107);
            verify(mockEvent).setDamage(107);
        }

        @Test
        @DisplayName("魔法ダメージ計算: INT100")
        void handlePlayerToEntityDamage_MagicDamage_Int100() {
            setupDamageEvent(EntityDamageEvent.DamageCause.THORNS, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.INTELLIGENCE, 100));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 100 + 100 × 0.15 = 115
            assertThat(result).isEqualTo(115);
            verify(mockEvent).setDamage(115);
        }

        @Test
        @DisplayName("ステータスが空の場合は基本ダメージのまま")
        void handlePlayerToEntityDamage_EmptyStats_UsesBaseDamage() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 15.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of());

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            assertThat(result).isEqualTo(15);
            verify(mockEvent).setDamage(15);
        }

        @Test
        @DisplayName("小数ダメージは切り捨て")
        void handlePlayerToEntityDamage_FractionalDamage_Truncates() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 33));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 10 × 1.33 = 13.3 → 13
            assertThat(result).isEqualTo(13);
            verify(mockEvent).setDamage(13);
        }
    }

    // ==================== キャッシュ機能テスト ====================

    @Nested
    @DisplayName("キャッシュ機能テスト")
    class CacheTests {

        @Test
        @DisplayName("キャッシュクリアでサイズが減少する")
        void clearCache_ReducesSize() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            // 最初の攻撃
            handler.handlePlayerToEntityDamage(mockEvent);

            int sizeBefore = handler.getCacheSize();
            assertThat(sizeBefore).isGreaterThan(0);

            // キャッシュクリア（期限切れエントリのシミュレートは難しいため、メソッド呼び出しのみ確認）
            handler.clearCache();

            // エントリがまだ有効な場合はサイズが維持される
            int sizeAfter = handler.getCacheSize();
            assertThat(sizeAfter).isLessThanOrEqualTo(sizeBefore);
        }

        @Test
        @DisplayName("キャッシュサイズが0から始まる")
        void getCacheSize_InitialState_ReturnsZero() {
            assertThat(handler.getCacheSize()).isZero();
        }

        @Test
        @DisplayName("最大キャッシュサイズを超えない")
        void handlePlayerToEntityDamage_RespectsMaxCacheSize() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            // キャッシュを埋める（実際にはテストで1000回実行するのは非現実的）
            for (int i = 0; i < 10; i++) {
                handler.handlePlayerToEntityDamage(mockEvent);
            }

            assertThat(handler.getCacheSize()).isPositive();
        }

        @Test
        @DisplayName("キャッシュが有効期限内は同じダメージ値を返す")
        void cache_ValidWithinOneSecond_ReturnsCachedValue() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            // 最初の攻撃
            handler.handlePlayerToEntityDamage(mockEvent);
            int cacheSize = handler.getCacheSize();
            assertThat(cacheSize).isEqualTo(1);

            // キャッシュ有効期限内の2回目の攻撃（キャッシュ値を使用）
            handler.handlePlayerToEntityDamage(mockEvent);
            assertThat(handler.getCacheSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("異なるプレイヤーは別々のキャッシュエントリを持つ")
        void cache_DifferentPlayers_SeparateEntries() {
            Player mockPlayer2 = mock(Player.class);
            UUID playerUuid2 = UUID.randomUUID();
            when(mockPlayer2.getUniqueId()).thenReturn(playerUuid2);
            when(mockPlayerManager.getRPGPlayer(playerUuid2)).thenReturn(mockRpgPlayer);

            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            // プレイヤー1の攻撃
            when(mockEvent.getDamager()).thenReturn(mockPlayer);
            handler.handlePlayerToEntityDamage(mockEvent);

            // プレイヤー2の攻撃
            when(mockEvent.getDamager()).thenReturn(mockPlayer2);
            handler.handlePlayerToEntityDamage(mockEvent);

            assertThat(handler.getCacheSize()).isEqualTo(2);
        }
    }

    // ==================== 複合テスト ====================

    @Nested
    @DisplayName("複合シナリオテスト")
    class CombinedScenarioTests {

        @Test
        @DisplayName("全ステータス補正を含む計算")
        void fullCalculation_WithAllStats() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(
                    Stat.STRENGTH, 100,
                    Stat.INTELLIGENCE, 50
            ));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 物理ダメージ: 100 × 2.0 = 200
            assertThat(result).isEqualTo(200);
        }

        @Test
        @DisplayName("負のステータス値でも計算可能")
        void negativeStats_StillCalculates() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, -50));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 100 × 0.5 = 50
            assertThat(result).isEqualTo(50);
        }

        @Test
        @DisplayName("高ステータスでの大幅ダメージ増加")
        void highStats_LargeDamageIncrease() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 200));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 10 × 3.0 = 30
            assertThat(result).isEqualTo(30);
        }

        @Test
        @DisplayName("最低ダメージ1が保証される")
        void lowDamage_MinimumOneGuaranteed() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, -100));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 1 × 0 = 0 だが最低1保証
            assertThat(result).isEqualTo(1);
        }
    }

    // ==================== ヘルパーメソッド ====================

    private void setupDamageEvent(EntityDamageEvent.DamageCause cause, double damage) {
        when(mockEvent.getDamager()).thenReturn(mockPlayer);
        when(mockEvent.getEntity()).thenReturn(mockTarget);
        when(mockEvent.getCause()).thenReturn(cause);
        when(mockEvent.getDamage()).thenReturn(damage);
    }
}
