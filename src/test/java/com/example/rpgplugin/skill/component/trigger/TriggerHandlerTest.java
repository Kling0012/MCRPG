package com.example.rpgplugin.skill.component.trigger;

import com.example.rpgplugin.skill.component.EffectComponent;
import com.example.rpgplugin.skill.component.SkillEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TriggerHandlerのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TriggerHandlerTest {

    @Mock
    private Trigger<Event> mockTrigger;
    @Mock
    private TriggerSettings mockSettings;
    @Mock
    private EffectComponent mockComponent;
    @Mock
    private SkillEffect mockSkill;
    @Mock
    private LivingEntity mockCaster;
    @Mock
    private LivingEntity mockTarget;
    @Mock
    private LivingEntity mockTarget2;
    @Mock
    private Event mockEvent;

    private TriggerHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TriggerHandler(
            "testSkill",
            mockTrigger,
            mockSettings,
            mockComponent,
            10
        );
    }

    // ========== コンストラクタとgetter テスト ==========

    @Nested
    @DisplayName("Constructor & Getters: コンストラクタとgetter")
    class ConstructorAndGettersTests {

        @Test
        @DisplayName("test: すべてのフィールドが正しく設定される")
        void testConstructor_AllFieldsSet() {
            assertThat(handler.getSkillId()).isEqualTo("testSkill");
            assertThat(handler.getTrigger()).isSameAs(mockTrigger);
            assertThat(handler.getSettings()).isSameAs(mockSettings);
            assertThat(handler.getRootComponent()).isSameAs(mockComponent);
            assertThat(handler.getDuration()).isEqualTo(10);
        }

        @Test
        @DisplayName("test: duration=0で無制限スキル")
        void testConstructor_ZeroDuration() {
            TriggerHandler unlimitedHandler = new TriggerHandler(
                "testSkill",
                mockTrigger,
                mockSettings,
                mockComponent,
                0
            );

            assertThat(unlimitedHandler.getDuration()).isEqualTo(0);
        }
    }

    // ========== handle() メソッドテスト ==========

    @Nested
    @DisplayName("handle: トリガー処理")
    class HandleTests {

        @Test
        @DisplayName("test: スキルが非アクティブの場合はfalse")
        void testHandle_SkillNotActive_ReturnsFalse() {
            when(mockTrigger.getCaster(mockEvent)).thenReturn(mockCaster);
            when(mockSkill.isActive(mockCaster)).thenReturn(false);

            boolean result = handler.handle(mockEvent, mockSkill);

            assertThat(result).isFalse();
            verify(mockComponent, never()).execute(any(), anyInt(), anyList());
        }

        @Test
        @DisplayName("test: shouldTriggerがfalseの場合はfalse")
        void testHandle_ShouldNotTrigger_ReturnsFalse() {
            when(mockTrigger.getCaster(mockEvent)).thenReturn(mockCaster);
            when(mockSkill.isActive(mockCaster)).thenReturn(true);
            when(mockSkill.getActiveLevel(mockCaster)).thenReturn(1);
            when(mockTrigger.shouldTrigger(mockEvent, 1, mockSettings)).thenReturn(false);

            boolean result = handler.handle(mockEvent, mockSkill);

            assertThat(result).isFalse();
            verify(mockComponent, never()).execute(any(), anyInt(), anyList());
        }

        @Test
        @DisplayName("test: 正常にトリガーが発動")
        void testHandle_SuccessfulTrigger() {
            when(mockTrigger.getCaster(mockEvent)).thenReturn(mockCaster);
            when(mockSkill.isActive(mockCaster)).thenReturn(true);
            when(mockSkill.getActiveLevel(mockCaster)).thenReturn(5);
            when(mockTrigger.shouldTrigger(mockEvent, 5, mockSettings)).thenReturn(true);
            when(mockTrigger.getTarget(mockEvent, mockSettings)).thenReturn(mockTarget);
            when(mockComponent.execute(eq(mockCaster), eq(5), anyList())).thenReturn(true);

            boolean result = handler.handle(mockEvent, mockSkill);

            assertThat(result).isTrue();
            verify(mockComponent).execute(eq(mockCaster), eq(5), anyList());
            verify(mockTrigger).setValues(eq(mockEvent), any());
        }

        @Test
        @DisplayName("test: コンポーネント実行が失敗した場合")
        void testHandle_ComponentExecutionFails() {
            when(mockTrigger.getCaster(mockEvent)).thenReturn(mockCaster);
            when(mockSkill.isActive(mockCaster)).thenReturn(true);
            when(mockSkill.getActiveLevel(mockCaster)).thenReturn(3);
            when(mockTrigger.shouldTrigger(mockEvent, 3, mockSettings)).thenReturn(true);
            when(mockTrigger.getTarget(mockEvent, mockSettings)).thenReturn(mockTarget);
            when(mockComponent.execute(eq(mockCaster), eq(3), anyList())).thenReturn(false);

            boolean result = handler.handle(mockEvent, mockSkill);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("test: setValuesが呼ばれることを確認")
        void testHandle_SetValuesCalled() {
            when(mockTrigger.getCaster(mockEvent)).thenReturn(mockCaster);
            when(mockSkill.isActive(mockCaster)).thenReturn(true);
            when(mockSkill.getActiveLevel(mockCaster)).thenReturn(1);
            when(mockTrigger.shouldTrigger(mockEvent, 1, mockSettings)).thenReturn(true);
            when(mockTrigger.getTarget(mockEvent, mockSettings)).thenReturn(mockTarget);
            when(mockComponent.execute(any(), anyInt(), anyList())).thenReturn(true);

            handler.handle(mockEvent, mockSkill);

            verify(mockTrigger).setValues(eq(mockEvent), any());
        }
    }

    // ========== handleImmediate() メソッドテスト ==========

    @Nested
    @DisplayName("handleImmediate: 即時実行")
    class HandleImmediateTests {

        @Test
        @DisplayName("test: ターゲット指定で即時実行")
        void testHandleImmediate_WithTarget() {
            when(mockComponent.execute(eq(mockCaster), eq(5), anyList())).thenReturn(true);

            boolean result = handler.handleImmediate(mockCaster, 5, mockTarget);

            assertThat(result).isTrue();
            verify(mockComponent).execute(eq(mockCaster), eq(5), anyList());
        }

        @Test
        @DisplayName("test: ターゲットnullの場合はcasterをターゲットに")
        void testHandleImmediate_NullTarget_UsesCaster() {
            when(mockComponent.execute(eq(mockCaster), eq(3), anyList())).thenReturn(true);

            boolean result = handler.handleImmediate(mockCaster, 3, null);

            assertThat(result).isTrue();
            verify(mockComponent).execute(eq(mockCaster), eq(3), anyList());
        }

        @Test
        @DisplayName("test: コンポーネント実行失敗")
        void testHandleImmediate_ComponentFails() {
            when(mockComponent.execute(mockCaster, 1, anyList())).thenReturn(false);

            boolean result = handler.handleImmediate(mockCaster, 1, mockTarget);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("test: レベル0でも動作")
        void testHandleImmediate_LevelZero() {
            when(mockComponent.execute(eq(mockCaster), eq(0), anyList())).thenReturn(true);

            boolean result = handler.handleImmediate(mockCaster, 0, mockTarget);

            assertThat(result).isTrue();
        }
    }

    // ========== 複数シナリオテスト ==========

    @Nested
    @DisplayName("Multiple Scenarios: 複数シナリオ")
    class MultipleScenarioTests {

        @Test
        @DisplayName("test: 異なるレベルでhandleを実行")
        void testHandle_DifferentLevels() {
            when(mockTrigger.getCaster(mockEvent)).thenReturn(mockCaster);
            when(mockSkill.isActive(mockCaster)).thenReturn(true);
            when(mockTrigger.getTarget(mockEvent, mockSettings)).thenReturn(mockTarget);
            when(mockComponent.execute(any(), anyInt(), anyList())).thenReturn(true);

            // レベル1
            when(mockSkill.getActiveLevel(mockCaster)).thenReturn(1);
            when(mockTrigger.shouldTrigger(mockEvent, 1, mockSettings)).thenReturn(true);
            assertThat(handler.handle(mockEvent, mockSkill)).isTrue();

            // レベル10
            when(mockSkill.getActiveLevel(mockCaster)).thenReturn(10);
            when(mockTrigger.shouldTrigger(mockEvent, 10, mockSettings)).thenReturn(true);
            assertThat(handler.handle(mockEvent, mockSkill)).isTrue();
        }

        @Test
        @DisplayName("test: 異なるターゲットでhandleを実行")
        void testHandle_DifferentTargets() {
            when(mockTrigger.getCaster(mockEvent)).thenReturn(mockCaster);
            when(mockSkill.isActive(mockCaster)).thenReturn(true);
            when(mockSkill.getActiveLevel(mockCaster)).thenReturn(5);

            // ターゲット1
            when(mockTrigger.shouldTrigger(mockEvent, 5, mockSettings)).thenReturn(true);
            when(mockTrigger.getTarget(mockEvent, mockSettings)).thenReturn(mockTarget);
            when(mockComponent.execute(eq(mockCaster), eq(5), anyList())).thenReturn(true);
            assertThat(handler.handle(mockEvent, mockSkill)).isTrue();

            // ターゲット2
            when(mockTrigger.getTarget(mockEvent, mockSettings)).thenReturn(mockTarget2);
            assertThat(handler.handle(mockEvent, mockSkill)).isTrue();
        }
    }
}
