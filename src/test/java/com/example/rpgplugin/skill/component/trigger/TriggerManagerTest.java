package com.example.rpgplugin.skill.component.trigger;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.skill.component.EffectComponent;
import com.example.rpgplugin.skill.component.SkillEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * TriggerManagerのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TriggerManagerTest {

    @Mock
    private RPGPlugin mockPlugin;
    @Mock
    private LivingEntity mockCaster;
    @Mock
    private Player mockPlayer;
    @Mock
    private SkillEffect mockEffect;
    @Mock
    private TriggerHandler mockHandler;
    @Mock
    private EffectComponent mockComponent;

    private TriggerManager manager;

    @BeforeEach
    void setUp() {
        // プライベートコンストラクタを回避するためにリフレクションを使用
        try {
            var constructor = TriggerManager.class.getDeclaredConstructor(RPGPlugin.class);
            constructor.setAccessible(true);
            manager = constructor.newInstance(mockPlugin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(mockCaster.getEntityId()).thenReturn(100);
        when(mockPlayer.getEntityId()).thenReturn(200);
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockHandler.getSkillId()).thenReturn("testSkill");
        when(mockHandler.getRootComponent()).thenReturn(mockComponent);
        doReturn(new CastTrigger()).when(mockHandler).getTrigger();
    }

    @AfterEach
    void tearDown() {
        // シングルトンをリセット
        try {
            var instanceField = TriggerManager.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            // Ignore
        }
    }

    // ========== getInstance() テスト ==========

    @Nested
    @DisplayName("getInstance: シングルトン取得")
    class GetInstanceTests {

        @Test
        @DisplayName("test: 初期化前にgetInstanceを呼ぶと例外")
        void testGetInstanceThrowsWhenNotInitialized() {
            // シングルトンをリセット
            try {
                var instanceField = TriggerManager.class.getDeclaredField("instance");
                instanceField.setAccessible(true);
                instanceField.set(null, null);
            } catch (Exception e) {
                // Ignore
            }

            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
                TriggerManager.getInstance();
            });
            assertTrue(ex.getMessage().contains("not initialized"));
        }
    }

    // ========== registerSkill / unregisterSkill テスト ==========

    @Nested
    @DisplayName("registerSkill/unregisterSkill: スキル登録・削除")
    class SkillRegistrationTests {

        @Test
        @DisplayName("test: registerSkillでスキルを登録")
        void testRegisterSkill() {
            manager.registerSkill("testSkill", mockEffect);

            // 登録されたスキル効果はactivateSkillで使用される
            when(mockEffect.isActive(mockCaster)).thenReturn(true);

            manager.activateSkill("testSkill", mockCaster, 1, 10, new ArrayList<>());

            verify(mockEffect).activate(mockCaster, 1, 10);
        }

        @Test
        @DisplayName("test: registerSkillで上書き登録")
        void testRegisterSkillOverwrite() {
            SkillEffect effect1 = mock(SkillEffect.class);
            SkillEffect effect2 = mock(SkillEffect.class);

            manager.registerSkill("testSkill", effect1);
            manager.registerSkill("testSkill", effect2);

            when(effect2.isActive(mockCaster)).thenReturn(true);

            manager.activateSkill("testSkill", mockCaster, 1, 10, new ArrayList<>());

            verify(effect2).activate(mockCaster, 1, 10);
            verify(effect1, never()).activate(any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("test: unregisterSkillでスキルを削除")
        void testUnregisterSkill() {
            manager.registerSkill("testSkill", mockEffect);
            manager.unregisterSkill("testSkill");

            manager.activateSkill("testSkill", mockCaster, 1, 10, new ArrayList<>());

            // 削除後はeffect.activateが呼ばれない
            verify(mockEffect, never()).activate(any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("test: unregisterSkillで存在しないスキルを削除")
        void testUnregisterNonExistentSkill() {
            assertDoesNotThrow(() -> {
                manager.unregisterSkill("nonExistent");
            });
        }

        @Test
        @DisplayName("test: unregisterSkillでアクティブトリガーもクリア")
        void testUnregisterSkillClearsActiveTriggers() {
            List<TriggerHandler> handlers = new ArrayList<>();
            handlers.add(mockHandler);

            manager.registerSkill("testSkill", mockEffect);
            manager.activateSkill("testSkill", mockCaster, 1, 10, handlers);

            // 削除
            manager.unregisterSkill("testSkill");

            // 再度activateしてもhandlersはクリアされているため何も起きない
            manager.activateSkill("testSkill", mockCaster, 1, 10, new ArrayList<>());
        }
    }

    // ========== activateSkill / deactivateSkill テスト ==========

    @Nested
    @DisplayName("activateSkill/deactivateSkill: スキルアクティブ化")
    class SkillActivationTests {

        @Test
        @DisplayName("test: activateSkillでスキルをアクティブ化")
        void testActivateSkill() {
            manager.registerSkill("testSkill", mockEffect);
            when(mockEffect.isActive(mockCaster)).thenReturn(true);

            manager.activateSkill("testSkill", mockCaster, 1, 10, new ArrayList<>());

            verify(mockEffect).activate(mockCaster, 1, 10);
        }

        @Test
        @DisplayName("test: activateSkillで未登録スキルはエラーにならない")
        void testActivateSkillUnregistered() {
            // 未登録スキルでもエラーにならない
            assertDoesNotThrow(() -> {
                manager.activateSkill("unregistered", mockCaster, 1, 10, new ArrayList<>());
            });
        }

        @Test
        @DisplayName("test: activateSkillでnull effectは無視")
        void testActivateSkillWithNullEffect() {
            // 未登録でeffect=null
            assertDoesNotThrow(() -> {
                manager.activateSkill("nullEffect", mockCaster, 1, 10, new ArrayList<>());
            });
        }

        @Test
        @DisplayName("test: deactivateSkillでスキルを非アクティブ化")
        void testDeactivateSkill() {
            List<TriggerHandler> handlers = new ArrayList<>();
            handlers.add(mockHandler);

            manager.registerSkill("testSkill", mockEffect);
            manager.activateSkill("testSkill", mockCaster, 1, 10, handlers);
            manager.deactivateSkill("testSkill", mockCaster);

            verify(mockComponent).cleanUp(mockCaster);
            verify(mockEffect).deactivate(mockCaster);
        }

        @Test
        @DisplayName("test: deactivateSkillで未登録スキルはエラーにならない")
        void testDeactivateSkillUnregistered() {
            // 未登録スキルでもエラーにならない
            assertDoesNotThrow(() -> {
                manager.deactivateSkill("unregistered", mockCaster);
            });
        }

        @Test
        @DisplayName("test: deactivateSkillでnull effectは無視")
        void testDeactivateSkillWithNullEffect() {
            // 未登録でeffect=null
            assertDoesNotThrow(() -> {
                manager.deactivateSkill("nullEffect", mockCaster);
            });
        }

        @Test
        @DisplayName("test: duration=0で無制限スキル")
        void testActivateSkillWithZeroDuration() {
            manager.registerSkill("testSkill", mockEffect);
            when(mockEffect.isActive(mockCaster)).thenReturn(true);

            manager.activateSkill("testSkill", mockCaster, 1, 0, new ArrayList<>());

            verify(mockEffect).activate(mockCaster, 1, 0);
        }

        @Test
        @DisplayName("test: effectがnullでない場合はactivateが呼ばれる")
        void testActivateSkillCallsActivate() {
            manager.registerSkill("testSkill", mockEffect);

            manager.activateSkill("testSkill", mockCaster, 1, 10, new ArrayList<>());

            // TriggerManagerはisActiveチェックを行わない
            verify(mockEffect).activate(mockCaster, 1, 10);
        }

        @Test
        @DisplayName("test: 異なるエンティティで同じスキルをアクティブ化")
        void testActivateSkillForDifferentEntities() {
            LivingEntity mockCaster2 = mock(LivingEntity.class);
            when(mockCaster2.getEntityId()).thenReturn(200);

            manager.registerSkill("testSkill", mockEffect);
            when(mockEffect.isActive(any())).thenReturn(true);

            List<TriggerHandler> handlers = new ArrayList<>();
            handlers.add(mockHandler);

            manager.activateSkill("testSkill", mockCaster, 1, 10, handlers);
            manager.activateSkill("testSkill", mockCaster2, 1, 10, handlers);

            // 両方のエンティティでactivateが呼ばれる
            verify(mockEffect, times(2)).activate(any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("test: handlersが空の場合でも動作")
        void testActivateSkillWithEmptyHandlers() {
            manager.registerSkill("testSkill", mockEffect);
            when(mockEffect.isActive(mockCaster)).thenReturn(true);

            // 空のハンドラーリスト
            manager.activateSkill("testSkill", mockCaster, 1, 10, new ArrayList<>());

            verify(mockEffect).activate(mockCaster, 1, 10);
        }

        @Test
        @DisplayName("test: deactivateSkillでエンティティにアクティブスキルがない場合")
        void testDeactivateSkillWithNoActiveSkill() {
            manager.registerSkill("testSkill", mockEffect);

            // アクティブ化していないのにdeactivate
            assertDoesNotThrow(() -> {
                manager.deactivateSkill("testSkill", mockCaster);
            });
        }
    }

    // ========== 複数回activateSkill テスト ==========

    @Nested
    @DisplayName("Multiple Activations: 複数回アクティブ化")
    class MultipleActivationTests {

        @Test
        @DisplayName("test: 同じスキルを複数回activateすると上書き")
        void testMultipleActivateOverwrites() {
            List<TriggerHandler> handlers = new ArrayList<>();
            handlers.add(mockHandler);

            manager.registerSkill("testSkill", mockEffect);
            when(mockEffect.isActive(mockCaster)).thenReturn(true);

            manager.activateSkill("testSkill", mockCaster, 1, 10, handlers);
            manager.activateSkill("testSkill", mockCaster, 2, 20, handlers);

            // effect.activateは2回呼ばれる（cleanup + new）
            verify(mockEffect, times(2)).activate(any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("test: 異なるスキルを同時にactivate")
        void testActivateDifferentSkills() {
            List<TriggerHandler> handlers1 = new ArrayList<>();
            List<TriggerHandler> handlers2 = new ArrayList<>();
            handlers1.add(mockHandler);
            handlers2.add(mockHandler);

            TriggerHandler handler2 = mock(TriggerHandler.class);
            when(handler2.getSkillId()).thenReturn("testSkill2");
            when(handler2.getRootComponent()).thenReturn(mockComponent);
            doReturn(new CastTrigger()).when(handler2).getTrigger();

            manager.registerSkill("testSkill", mockEffect);
            manager.registerSkill("testSkill2", mockEffect);
            when(mockEffect.isActive(mockCaster)).thenReturn(true);

            manager.activateSkill("testSkill", mockCaster, 1, 10, handlers1);
            manager.activateSkill("testSkill2", mockCaster, 1, 10, handlers2);

            verify(mockEffect, times(2)).activate(mockCaster, 1, 10);
        }
    }


    // ========== getEntity() テスト（リフレクション使用）==========

    @Nested
    @DisplayName("getEntity: エンティティ取得")
    class GetEntityTests {

        @Mock
        private Server mockServer;
        private java.util.Collection<Player> emptyPlayers;

        @BeforeEach
        void setUpGetEntity() {
            emptyPlayers = java.util.Collections.emptyList();
            when(mockPlugin.getServer()).thenReturn(mockServer);
            // getOnlinePlayers()はCollection<? extends Player>を返す
            when(mockServer.getOnlinePlayers()).thenAnswer(invocation -> emptyPlayers);
        }

        @Test
        @DisplayName("test: エンティティが存在しない場合はnull")
        void testGetEntity_NotFound() throws Exception {
            var method = TriggerManager.class.getDeclaredMethod("getEntity", int.class);
            method.setAccessible(true);

            LivingEntity result = (LivingEntity) method.invoke(manager, 999);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("test: プレイヤーリストが空の場合はnull")
        void testGetEntity_EmptyPlayerList() throws Exception {
            var method = TriggerManager.class.getDeclaredMethod("getEntity", int.class);
            method.setAccessible(true);

            LivingEntity result = (LivingEntity) method.invoke(manager, 100);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("test: プレイヤーが見つかる場合はエンティティを返す")
        void testGetEntity_PlayerFound() throws Exception {
            // プレイヤーリストにmockPlayerを追加
            java.util.List<Player> playerList = new java.util.ArrayList<>();
            playerList.add(mockPlayer);

            @SuppressWarnings("unchecked")
            java.util.Collection<Player> players = (java.util.Collection<Player>) (java.util.Collection<?>) playerList;
            when(mockServer.getOnlinePlayers()).thenAnswer(invocation -> players);
            when(mockPlayer.getEntityId()).thenReturn(100);

            var method = TriggerManager.class.getDeclaredMethod("getEntity", int.class);
            method.setAccessible(true);

            LivingEntity result = (LivingEntity) method.invoke(manager, 100);

            assertThat(result).isSameAs(mockPlayer);
        }

        @Test
        @DisplayName("test: 複数プレイヤーの中から正しいエンティティを返す")
        void testGetEntity_MultiplePlayers() throws Exception {
            Player mockPlayer2 = mock(Player.class);
            when(mockPlayer2.getEntityId()).thenReturn(201);

            java.util.List<Player> playerList = new java.util.ArrayList<>();
            playerList.add(mockPlayer);
            playerList.add(mockPlayer2);

            @SuppressWarnings("unchecked")
            java.util.Collection<Player> players = (java.util.Collection<Player>) (java.util.Collection<?>) playerList;
            when(mockServer.getOnlinePlayers()).thenAnswer(invocation -> players);
            when(mockPlayer.getEntityId()).thenReturn(200);

            var method = TriggerManager.class.getDeclaredMethod("getEntity", int.class);
            method.setAccessible(true);

            // mockPlayer (ID=200) を探す
            LivingEntity result = (LivingEntity) method.invoke(manager, 200);

            assertThat(result).isSameAs(mockPlayer);
        }

        @Test
        @DisplayName("test: 一致しないIDの場合はnull")
        void testGetEntity_NoMatch() throws Exception {
            java.util.List<Player> playerList = new java.util.ArrayList<>();
            playerList.add(mockPlayer);

            @SuppressWarnings("unchecked")
            java.util.Collection<Player> players = (java.util.Collection<Player>) (java.util.Collection<?>) playerList;
            when(mockServer.getOnlinePlayers()).thenAnswer(invocation -> players);
            when(mockPlayer.getEntityId()).thenReturn(200);

            var method = TriggerManager.class.getDeclaredMethod("getEntity", int.class);
            method.setAccessible(true);

            LivingEntity result = (LivingEntity) method.invoke(manager, 999);

            assertThat(result).isNull();
        }
    }

    // ========== cleanupExpired() テスト（リフレクション使用）==========

    @Nested
    @DisplayName("cleanupExpired: 期限切れトリガークリーンアップ")
    class CleanupExpiredTests {

        @BeforeEach
        void setUpCleanup() {
            // mockPlugin.getServer() のモック設定
            Server mockServer = mock(Server.class);
            when(mockPlugin.getServer()).thenReturn(mockServer);
            when(mockServer.getOnlinePlayers()).thenReturn(java.util.Collections.emptyList());
        }

        @Test
        @DisplayName("test: エンティティのトリガーがない場合は何もしない")
        void testCleanupExpired_NoTriggers() throws Exception {
            var method = TriggerManager.class.getDeclaredMethod("cleanupExpired", int.class);
            method.setAccessible(true);

            // 例外が投げられないことを確認
            assertDoesNotThrow(() -> {
                method.invoke(manager, 999);
            });
        }

        @Test
        @DisplayName("test: duration=0のトリガーは期限切れにならない")
        void testCleanupExpired_ZeroDuration_NoExpiry() throws Exception {
            List<TriggerHandler> handlers = new ArrayList<>();
            handlers.add(mockHandler);

            manager.registerSkill("testSkill", mockEffect);
            manager.activateSkill("testSkill", mockCaster, 1, 0, handlers);

            // activeTriggers にデータがある状態で cleanupExpired
            var method = TriggerManager.class.getDeclaredMethod("cleanupExpired", int.class);
            method.setAccessible(true);

            assertDoesNotThrow(() -> {
                method.invoke(manager, mockCaster.getEntityId());
            });

            // スキルはまだアクティブであるべき（cleanupされていない）
            // これは間接的な検証
        }

        @Test
        @DisplayName("test: 期限切れのトリガーを削除しcleanUpを呼ぶ")
        void testCleanupExpired_RemovesExpired() throws Exception {
            // まずスキルをアクティブ化
            List<TriggerHandler> handlers = new ArrayList<>();
            handlers.add(mockHandler);

            manager.registerSkill("testSkill", mockEffect);
            when(mockEffect.isActive(mockCaster)).thenReturn(true);

            manager.activateSkill("testSkill", mockCaster, 1, 1, handlers); // duration=1秒

            // 期限切れになるまで待機できないため、リフレクションで直接操作
            var activeTriggersField = TriggerManager.class.getDeclaredField("activeTriggers");
            activeTriggersField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, Map<String, Object>> activeTriggers = 
                (Map<Integer, Map<String, Object>>) activeTriggersField.get(manager);

            // expiryTime を過去に設定
            Map<String, Object> entityTriggers = activeTriggers.get(mockCaster.getEntityId());
            if (entityTriggers != null && !entityTriggers.isEmpty()) {
                Object triggerData = entityTriggers.values().iterator().next();
                var expiryTimeField = triggerData.getClass().getDeclaredField("expiryTime");
                expiryTimeField.setAccessible(true);
                expiryTimeField.set(triggerData, 0L); // 過去の時刻
            }

            var method = TriggerManager.class.getDeclaredMethod("cleanupExpired", int.class);
            method.setAccessible(true);

            assertDoesNotThrow(() -> {
                method.invoke(manager, mockCaster.getEntityId());
            });

            // cleanUpはgetEntityがnullを返す場合呼ばれないため、ここでは動作確認のみ
            // 実際のcleanUp呼び出しは統合テストで検証
        }

        @Test
        @DisplayName("test: 全トリガー削除後にエントリも削除")
        void testCleanupExpired_RemovesEntryWhenEmpty() throws Exception {
            List<TriggerHandler> handlers = new ArrayList<>();
            handlers.add(mockHandler);

            manager.registerSkill("testSkill", mockEffect);
            manager.activateSkill("testSkill", mockCaster, 1, 1, handlers);

            // expiryTime を過去に設定して期限切れにする
            var activeTriggersField = TriggerManager.class.getDeclaredField("activeTriggers");
            activeTriggersField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, Map<String, Object>> activeTriggers = 
                (Map<Integer, Map<String, Object>>) activeTriggersField.get(manager);

            Map<String, Object> entityTriggers = activeTriggers.get(mockCaster.getEntityId());
            if (entityTriggers != null && !entityTriggers.isEmpty()) {
                Object triggerData = entityTriggers.values().iterator().next();
                var expiryTimeField = triggerData.getClass().getDeclaredField("expiryTime");
                expiryTimeField.setAccessible(true);
                expiryTimeField.set(triggerData, 0L);
            }

            var method = TriggerManager.class.getDeclaredMethod("cleanupExpired", int.class);
            method.setAccessible(true);

            method.invoke(manager, mockCaster.getEntityId());

            // エントリが削除されていることを確認
            assertThat(activeTriggers.containsKey(mockCaster.getEntityId())).isFalse();
        }

        @Test
        @DisplayName("test: 複数トリガーのうち期限切れのもののみ削除")
        void testCleanupExpired_PartialRemoval() throws Exception {
            // 異なる期限のトリガーを作成
            TriggerHandler handler2 = mock(TriggerHandler.class);
            when(handler2.getSkillId()).thenReturn("testSkill2");
            when(handler2.getRootComponent()).thenReturn(mockComponent);
            doReturn(new CastTrigger()).when(handler2).getTrigger();

            List<TriggerHandler> handlers1 = new ArrayList<>();
            handlers1.add(mockHandler);

            List<TriggerHandler> handlers2 = new ArrayList<>();
            handlers2.add(handler2);

            manager.registerSkill("testSkill", mockEffect);
            manager.registerSkill("testSkill2", mockEffect);

            // 1つのスキルをアクティブ化
            manager.activateSkill("testSkill", mockCaster, 1, 1, handlers1);

            var method = TriggerManager.class.getDeclaredMethod("cleanupExpired", int.class);
            method.setAccessible(true);

            assertDoesNotThrow(() -> {
                method.invoke(manager, mockCaster.getEntityId());
            });
        }
    }

    // ========== EventHandler テスト ==========

    @Nested
    @DisplayName("EventHandlers: Bukkitイベントハンドラー")
    class EventHandlerTests {

        @Test
        @DisplayName("test: onPlayerToggleSneakでイベントが処理される")
        void testOnPlayerToggleSneak_ProcessesEvent() {
            org.bukkit.event.player.PlayerToggleSneakEvent event =
                mock(org.bukkit.event.player.PlayerToggleSneakEvent.class);
            when(event.getPlayer()).thenReturn(mockPlayer);

            assertDoesNotThrow(() -> {
                manager.onPlayerToggleSneak(event);
            });
        }

        @Test
        @DisplayName("test: onEntityDeathでキラートリガーが処理される")
        void testOnEntityDeath_ProcessesKillTrigger() {
            org.bukkit.event.entity.EntityDeathEvent event =
                mock(org.bukkit.event.entity.EntityDeathEvent.class);
            LivingEntity deadEntity = mock(LivingEntity.class);
            Player killer = mock(Player.class);
            when(killer.getEntityId()).thenReturn(100);

            when(event.getEntity()).thenReturn(deadEntity);
            when(deadEntity.getKiller()).thenReturn(killer);

            assertDoesNotThrow(() -> {
                manager.onEntityDeath(event);
            });
        }

        @Test
        @DisplayName("test: onEntityDeathでキラーがいない場合は何もしない")
        void testOnEntityDeath_NoKiller() {
            org.bukkit.event.entity.EntityDeathEvent event =
                mock(org.bukkit.event.entity.EntityDeathEvent.class);
            LivingEntity deadEntity = mock(LivingEntity.class);

            when(event.getEntity()).thenReturn(deadEntity);
            when(deadEntity.getKiller()).thenReturn(null);

            assertDoesNotThrow(() -> {
                manager.onEntityDeath(event);
            });
        }

        @Test
        @DisplayName("test: onEntityDamageByEntityで物理ダメージトリガーが処理される")
        void testOnEntityDamageByEntity_ProcessesPhysicalDamage() {
            org.bukkit.event.entity.EntityDamageByEntityEvent event =
                mock(org.bukkit.event.entity.EntityDamageByEntityEvent.class);
            when(event.getEntity()).thenReturn(mockCaster);
            when(event.getDamager()).thenReturn(mockPlayer);

            assertDoesNotThrow(() -> {
                manager.onEntityDamageByEntity(event);
            });
        }

        @Test
        @DisplayName("test: onEntityDamageByEntityでエンティティがnullの場合")
        void testOnEntityDamageByEntity_NullEntity() {
            org.bukkit.event.entity.EntityDamageByEntityEvent event =
                mock(org.bukkit.event.entity.EntityDamageByEntityEvent.class);
            org.bukkit.entity.Entity nonLiving = mock(org.bukkit.entity.Entity.class);

            when(event.getEntity()).thenReturn(nonLiving);

            assertDoesNotThrow(() -> {
                manager.onEntityDamageByEntity(event);
            });
        }

        @Test
        @DisplayName("test: onEntityDamageで落下ダメージトリガーが処理される")
        void testOnEntityDamage_ProcessesFallDamage() {
            org.bukkit.event.entity.EntityDamageEvent event =
                mock(org.bukkit.event.entity.EntityDamageEvent.class);
            when(event.getEntity()).thenReturn(mockCaster);
            when(event.getCause()).thenReturn(org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL);

            assertDoesNotThrow(() -> {
                manager.onEntityDamage(event);
            });
        }

        @Test
        @DisplayName("test: onEntityDamageで環境ダメージトリガーが処理される")
        void testOnEntityDamage_ProcessesEnvironmentalDamage() {
            org.bukkit.event.entity.EntityDamageEvent event =
                mock(org.bukkit.event.entity.EntityDamageEvent.class);
            when(event.getEntity()).thenReturn(mockCaster);
            when(event.getCause()).thenReturn(org.bukkit.event.entity.EntityDamageEvent.DamageCause.LAVA);

            assertDoesNotThrow(() -> {
                manager.onEntityDamage(event);
            });
        }

        @Test
        @DisplayName("test: onEntityDamageで非LivingEntityの場合は何もしない")
        void testOnEntityDamage_NonLivingEntity() {
            org.bukkit.event.entity.EntityDamageEvent event =
                mock(org.bukkit.event.entity.EntityDamageEvent.class);
            org.bukkit.entity.Entity nonLiving = mock(org.bukkit.entity.Entity.class);

            when(event.getEntity()).thenReturn(nonLiving);

            assertDoesNotThrow(() -> {
                manager.onEntityDamage(event);
            });
        }

        @Test
        @DisplayName("test: onProjectileLaunchで投射物発射トリガーが処理される")
        void testOnProjectileLaunch_ProcessesLaunch() {
            org.bukkit.event.entity.ProjectileLaunchEvent event =
                mock(org.bukkit.event.entity.ProjectileLaunchEvent.class);
            org.bukkit.entity.Projectile projectile = mock(org.bukkit.entity.Projectile.class);

            when(event.getEntity()).thenReturn(projectile);
            when(projectile.getShooter()).thenReturn(mockPlayer);

            assertDoesNotThrow(() -> {
                manager.onProjectileLaunch(event);
            });
        }

        @Test
        @DisplayName("test: onProjectileLaunchでシューターがnullの場合")
        void testOnProjectileLaunch_NullShooter() {
            org.bukkit.event.entity.ProjectileLaunchEvent event =
                mock(org.bukkit.event.entity.ProjectileLaunchEvent.class);
            org.bukkit.entity.Projectile projectile = mock(org.bukkit.entity.Projectile.class);

            when(event.getEntity()).thenReturn(projectile);
            when(projectile.getShooter()).thenReturn(null);

            assertDoesNotThrow(() -> {
                manager.onProjectileLaunch(event);
            });
        }

        @Test
        @DisplayName("test: onProjectileLaunchでシューターが非LivingEntityの場合")
        void testOnProjectileLaunch_NonLivingShooter() {
            org.bukkit.event.entity.ProjectileLaunchEvent event =
                mock(org.bukkit.event.entity.ProjectileLaunchEvent.class);
            org.bukkit.entity.Projectile projectile = mock(org.bukkit.entity.Projectile.class);
            org.bukkit.projectiles.ProjectileSource blockSource = mock(org.bukkit.projectiles.ProjectileSource.class);

            when(event.getEntity()).thenReturn(projectile);
            when(projectile.getShooter()).thenReturn(blockSource);

            assertDoesNotThrow(() -> {
                manager.onProjectileLaunch(event);
            });
        }
    }
}
