package com.example.rpgplugin.damage;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.damage.handlers.EntityDamageHandler;
import com.example.rpgplugin.damage.handlers.PlayerDamageHandler;
import com.example.rpgplugin.player.PlayerManager;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DamageManagerのユニットテスト
 *
 * <p>ダメージシステム統括マネージャーのテストを行います。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DamageManager テスト")
class DamageManagerTest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private Server mockServer;

    @Mock
    private BukkitScheduler mockScheduler;

    @Mock
    private BukkitTask mockTask;

    @Mock
    private Logger mockLogger;

    private DamageManager damageManager;

    @BeforeEach
    void setUp() {
        when(mockPlugin.getServer()).thenReturn(mockServer);
        when(mockServer.getScheduler()).thenReturn(mockScheduler);
        when(mockPlugin.getLogger()).thenReturn(mockLogger);
        when(mockScheduler.runTaskTimer(eq(mockPlugin), any(Runnable.class), anyLong(), anyLong()))
                .thenReturn(mockTask);
        when(mockTask.getTaskId()).thenReturn(1);
    }

    // ==================== 初期化・終了テスト ====================

    @Nested
    @DisplayName("初期化・終了テスト")
    class LifecycleTests {

        @Test
        @DisplayName("コンストラクタで有効状態になる")
        void constructor_InitializesAsEnabled() {
            damageManager = new DamageManager(mockPlugin, mockPlayerManager);

            assertThat(damageManager.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("initializeでキャッシュクリアタスクが開始される")
        void initialize_StartsCacheClearTask() {
            damageManager = new DamageManager(mockPlugin, mockPlayerManager);

            damageManager.initialize();

            verify(mockScheduler).runTaskTimer(eq(mockPlugin), any(Runnable.class), eq(100L), eq(100L));
            verify(mockLogger).info(contains("Cache clear task started"));
        }

        @Test
        @DisplayName("shutdownでタスクが停止される")
        void shutdown_StopsTask() {
            damageManager = new DamageManager(mockPlugin, mockPlayerManager);
            damageManager.initialize();

            damageManager.shutdown();

            verify(mockScheduler).cancelTask(1);
            verify(mockLogger).info(contains("Cache clear task stopped"));
        }

        @Test
        @DisplayName("初期化前にshutdownしても例外が発生しない")
        void shutdown_BeforeInitialize_NoException() {
            damageManager = new DamageManager(mockPlugin, mockPlayerManager);

            assertThatCode(() -> damageManager.shutdown())
                    .doesNotThrowAnyException();
        }
    }

    // ==================== 有効化・無効化テスト ====================

    @Nested
    @DisplayName("有効化・無効化テスト")
    class EnableDisableTests {

        @BeforeEach
        void setUp() {
            damageManager = new DamageManager(mockPlugin, mockPlayerManager);
        }

        @Test
        @DisplayName("disableで無効状態になる")
        void disable_SetsDisabled() {
            damageManager.disable();

            assertThat(damageManager.isEnabled()).isFalse();
            verify(mockLogger).info(contains("無効化"));
        }

        @Test
        @DisplayName("enableで有効状態になる")
        void enable_SetsEnabled() {
            damageManager.disable();
            damageManager.enable();

            assertThat(damageManager.isEnabled()).isTrue();
            verify(mockLogger).info(contains("有効化"));
        }

        @Test
        @DisplayName("無効状態でdisableしてもログが出ない")
        void disable_WhenDisabled_NoLog() {
            damageManager.disable();
            reset(mockLogger);

            damageManager.disable();

            verify(mockLogger, never()).info(anyString());
        }

        @Test
        @DisplayName("有効状態でenableしてもログが出ない")
        void enable_WhenEnabled_NoLog() {
            reset(mockLogger);

            damageManager.enable();

            verify(mockLogger, never()).info(anyString());
        }
    }

    // ==================== ハンドラー取得テスト ====================

    @Nested
    @DisplayName("ハンドラー取得テスト")
    class HandlerGetterTests {

        @BeforeEach
        void setUp() {
            damageManager = new DamageManager(mockPlugin, mockPlayerManager);
        }

        @Test
        @DisplayName("PlayerDamageHandlerを取得できる")
        void getPlayerDamageHandler_ReturnsHandler() {
            PlayerDamageHandler handler = damageManager.getPlayerDamageHandler();

            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("EntityDamageHandlerを取得できる")
        void getEntityDamageHandler_ReturnsHandler() {
            EntityDamageHandler handler = damageManager.getEntityDamageHandler();

            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("同じインスタンスが返される")
        void getHandlers_SameInstance() {
            PlayerDamageHandler handler1 = damageManager.getPlayerDamageHandler();
            PlayerDamageHandler handler2 = damageManager.getPlayerDamageHandler();

            assertThat(handler1).isSameAs(handler2);
        }
    }

    // ==================== イベントハンドラーテスト ====================

    @Nested
    @DisplayName("onEntityDamageByEntity イベントハンドラーテスト")
    class OnEntityDamageByEntityTests {

        @Mock
        private EntityDamageByEntityEvent mockEvent;

        @Mock
        private Player mockPlayer;

        @Mock
        private Entity mockEntity;

        @BeforeEach
        void setUp() {
            damageManager = new DamageManager(mockPlugin, mockPlayerManager);
            when(mockEvent.getDamager()).thenReturn(mockPlayer);
            when(mockEvent.getEntity()).thenReturn(mockEntity);
            // instanceof はスタブ化できない - 実際のinstanceofチェックに任せる
        }

        @Test
        @DisplayName("無効状態では処理しない")
        void onEntityDamageByEntity_Disabled_NoProcessing() {
            damageManager.disable();

            damageManager.onEntityDamageByEntity(mockEvent);

            verify(mockEvent, never()).setCancelled(true);
        }

        @Test
        @DisplayName("プレイヤーが関与しない場合は処理しない")
        void onEntityDamageByEntity_NoPlayerInvolved_NoProcessing() {
            Entity nonPlayer1 = mock(Entity.class);
            Entity nonPlayer2 = mock(Entity.class);
            when(mockEvent.getDamager()).thenReturn(nonPlayer1);
            when(mockEvent.getEntity()).thenReturn(nonPlayer2);
            // instanceof はスタブ化できない - Entity mock は instanceof Player で false

            damageManager.onEntityDamageByEntity(mockEvent);

            verify(mockEvent, never()).setCancelled(true);
        }

        @Test
        @DisplayName("プレイヤー→エンティティの場合は処理する")
        void onEntityDamageByEntity_PlayerToEntity_Processes() {
            damageManager.onEntityDamageByEntity(mockEvent);

            // ハンドラーが呼ばれること（実装次第でキャンセルされる可能性あり）
            verify(mockEvent, atLeastOnce()).getDamager();
        }

        @Test
        @DisplayName("プレイヤー→プレイヤーは処理しない（PvP未サポート）")
        void onEntityDamageByEntity_PlayerToPlayer_NoProcessing() {
            // Entity mock は instanceof Player で false になるため、このテストは Player mock に変更
            Player mockPlayerTarget = mock(Player.class);
            when(mockEvent.getEntity()).thenReturn(mockPlayerTarget);

            damageManager.onEntityDamageByEntity(mockEvent);

            // PvPはバニラの動作に委ねるためキャンセルされない
            verify(mockEvent, never()).setCancelled(true);
        }
    }

    // ==================== onEntityDamage テスト ====================

    @Nested
    @DisplayName("onEntityDamage イベントハンドラーテスト")
    class OnEntityDamageTests {

        @Mock
        private EntityDamageEvent mockEvent;

        @Mock
        private Player mockPlayer;

        @Mock
        private Entity mockEntity;

        @BeforeEach
        void setUp() {
            damageManager = new DamageManager(mockPlugin, mockPlayerManager);
        }

        @Test
        @DisplayName("無効状態では処理しない")
        void onEntityDamage_Disabled_NoProcessing() {
            damageManager.disable();
            when(mockEvent.getEntity()).thenReturn(mockPlayer);

            damageManager.onEntityDamage(mockEvent);

            // 環境ダメージは未実装なので何もしない
            verify(mockEvent, never()).setCancelled(true);
        }

        @Test
        @DisplayName("プレイヤー以外は処理しない")
        void onEntityDamage_NonPlayer_NoProcessing() {
            when(mockEvent.getEntity()).thenReturn(mockEntity);

            damageManager.onEntityDamage(mockEvent);

            verify(mockEvent, never()).setCancelled(true);
        }

        @Test
        @DisplayName("VOIDダメージは補正しない")
        void onEntityDamage_VoidDamage_NoModification() {
            when(mockEvent.getEntity()).thenReturn(mockPlayer);
            when(mockEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.VOID);

            damageManager.onEntityDamage(mockEvent);

            // 即死系は補な正なし
            verify(mockEvent, never()).setDamage(anyDouble());
        }

        @Test
        @DisplayName("SUICIDEダメージは補正しない")
        void onEntityDamage_SuicideDamage_NoModification() {
            when(mockEvent.getEntity()).thenReturn(mockPlayer);
            when(mockEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.SUICIDE);

            damageManager.onEntityDamage(mockEvent);

            verify(mockEvent, never()).setDamage(anyDouble());
        }

        @Test
        @DisplayName("KILLダメージは補正しない")
        void onEntityDamage_KillDamage_NoModification() {
            when(mockEvent.getEntity()).thenReturn(mockPlayer);
            when(mockEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.KILL);

            damageManager.onEntityDamage(mockEvent);

            verify(mockEvent, never()).setDamage(anyDouble());
        }

        @Test
        @DisplayName("その他のダメージタイプは将来的に実装")
        void onEntityDamage_OtherDamage_ForFutureImplementation() {
            when(mockEvent.getEntity()).thenReturn(mockPlayer);
            when(mockEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);

            damageManager.onEntityDamage(mockEvent);

            // 現在は実装されていないため何もしない
            verify(mockEvent, never()).setDamage(anyDouble());
        }
    }

    // ==================== 統合テスト ====================

    @Nested
    @DisplayName("統合テスト")
    class IntegrationTests {

        @Test
        @DisplayName("ライフサイクル全体")
        void fullLifecycle_InitialState() {
            damageManager = new DamageManager(mockPlugin, mockPlayerManager);

            // 初期状態は有効
            assertThat(damageManager.isEnabled()).isTrue();

            // 初期化
            damageManager.initialize();
            assertThat(damageManager.isEnabled()).isTrue();

            // 無効化
            damageManager.disable();
            assertThat(damageManager.isEnabled()).isFalse();

            // 有効化
            damageManager.enable();
            assertThat(damageManager.isEnabled()).isTrue();

            // 終了
            damageManager.shutdown();
            assertThat(damageManager.isEnabled()).isTrue(); // shutdownはタスク停止のみ
        }
    }
}
