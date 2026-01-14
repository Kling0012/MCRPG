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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
}
