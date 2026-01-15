package com.example.rpgplugin.skill.component;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.component.trigger.TriggerManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ComponentEffectExecutorのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ComponentEffectExecutor: コンポーネント効果実行")
class ComponentEffectExecutorTest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private LivingEntity mockCaster;

    @Mock
    private LivingEntity mockTarget;

    @Mock
    private Player mockPlayer;

    @Mock
    private Skill mockSkill;

    @Mock
    private SkillEffect mockSkillEffect;

    @Mock
    private EffectComponent mockComponent;

    @Mock
    private TriggerManager mockTriggerManager;

    @BeforeEach
    void setUp() {
        when(mockCaster.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockTarget.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockPlayer.getName()).thenReturn("TestPlayer");
    }

    // ========== コンストラクタと基本機能 ==========

    @Nested
    @DisplayName("コンストラクタと基本機能")
    class BasicFunctionalityTests {

        private ComponentEffectExecutor executor;

        @BeforeEach
        void setUp() {
            try (MockedStatic<TriggerManager> mockedStatic = mockStatic(TriggerManager.class)) {
                mockedStatic.when(TriggerManager::getInstance).thenReturn(mockTriggerManager);
                executor = new ComponentEffectExecutor(mockPlugin);
            }
        }

        @Test
        @DisplayName("test: コンストラクタでプラグインを設定")
        void testConstructorWithPlugin() {
            try (MockedStatic<TriggerManager> mockedStatic = mockStatic(TriggerManager.class)) {
                mockedStatic.when(TriggerManager::getInstance).thenReturn(mockTriggerManager);
                ComponentEffectExecutor exec = new ComponentEffectExecutor(mockPlugin);
                assertNotNull(exec);
            }
        }

        @Test
        @DisplayName("test: getPluginで設定されたプラグインを返す")
        void testGetPlugin() {
            assertEquals(mockPlugin, executor.getPlugin());
        }

        @Test
        @DisplayName("test: getTriggerManagerでTriggerManagerを返す")
        void testGetTriggerManager() {
            assertEquals(mockTriggerManager, executor.getTriggerManager());
        }
    }

    // ========== executeメソッド（リストターゲット） ==========

    @Nested
    @DisplayName("executeメソッド（リストターゲット）")
    class ExecuteWithListTests {

        private ComponentEffectExecutor executor;

        @BeforeEach
        void setUp() {
            try (MockedStatic<TriggerManager> mockedStatic = mockStatic(TriggerManager.class)) {
                mockedStatic.when(TriggerManager::getInstance).thenReturn(mockTriggerManager);
                executor = new ComponentEffectExecutor(mockPlugin);
            }
        }

        @Test
        @DisplayName("test: スキルエフェクトがnullの場合はfalse")
        void testExecuteWithNullSkillEffect() {
            when(mockSkill.getComponentEffect()).thenReturn(null);

            boolean result = executor.execute(mockCaster, mockSkill, 1, List.of(mockTarget));

            assertFalse(result);
        }

        @Test
        @DisplayName("test: スキルエフェクトの実行が成功した場合はtrue")
        void testExecuteWithSuccessfulSkillEffect() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(mockCaster, 1, List.of(mockTarget))).thenReturn(true);

            boolean result = executor.execute(mockCaster, mockSkill, 1, List.of(mockTarget));

            assertTrue(result);
            verify(mockSkillEffect).execute(mockCaster, 1, List.of(mockTarget));
        }

        @Test
        @DisplayName("test: スキルエフェクトの実行が失敗した場合はfalse")
        void testExecuteWithFailedSkillEffect() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(mockCaster, 1, List.of(mockTarget))).thenReturn(false);

            boolean result = executor.execute(mockCaster, mockSkill, 1, List.of(mockTarget));

            assertFalse(result);
        }

        @Test
        @DisplayName("test: 実行中に例外が発生した場合はfalse")
        void testExecuteWithException() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(any(), anyInt(), any()))
                .thenThrow(new RuntimeException("Test exception"));

            boolean result = executor.execute(mockPlayer, mockSkill, 1, List.of(mockTarget));

            assertFalse(result);
        }

        @Test
        @DisplayName("test: 例外発生時にプレイヤーにエラーメッセージを送信")
        void testExecuteSendsErrorMessageOnException() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(any(), anyInt(), any()))
                .thenThrow(new RuntimeException("Test error"));

            executor.execute(mockPlayer, mockSkill, 1, List.of(mockTarget));

            verify(mockPlayer).sendMessage(any(Component.class));
        }

        @Test
        @DisplayName("test: プレイヤー以外の例外時はメッセージを送信しない")
        void testExecuteNonPlayerNoMessageOnException() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(any(), anyInt(), any()))
                .thenThrow(new RuntimeException("Test error"));

            executor.execute(mockCaster, mockSkill, 1, List.of(mockTarget));

            // mockCasterはPlayerではないのでsendMessageは呼ばれない
            verify(mockCaster, atMostOnce()).sendMessage(any(Component.class));
        }

        @Test
        @DisplayName("test: 空のターゲットリストでも実行")
        void testExecuteWithEmptyTargetList() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(mockCaster, 1, List.of())).thenReturn(true);

            boolean result = executor.execute(mockCaster, mockSkill, 1, List.of());

            assertTrue(result);
        }

        @Test
        @DisplayName("test: 複数ターゲットを渡せる")
        void testExecuteWithMultipleTargets() {
            List<LivingEntity> targets = List.of(mockTarget, mockCaster);
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(mockCaster, 1, targets)).thenReturn(true);

            boolean result = executor.execute(mockCaster, mockSkill, 1, targets);

            assertTrue(result);
        }
    }

    // ========== executeメソッド（単体ターゲット） ==========

    @Nested
    @DisplayName("executeメソッド（単体ターゲット）")
    class ExecuteWithSingleTargetTests {

        private ComponentEffectExecutor executor;

        @BeforeEach
        void setUp() {
            try (MockedStatic<TriggerManager> mockedStatic = mockStatic(TriggerManager.class)) {
                mockedStatic.when(TriggerManager::getInstance).thenReturn(mockTriggerManager);
                executor = new ComponentEffectExecutor(mockPlugin);
            }
        }

        @Test
        @DisplayName("test: 単体ターゲットで実行成功")
        void testExecuteWithSingleTarget() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(eq(mockCaster), eq(1), anyList())).thenReturn(true);

            boolean result = executor.execute(mockCaster, mockSkill, 1, mockTarget);

            assertTrue(result);
        }

        @Test
        @DisplayName("test: nullターゲットの場合は空リストで実行")
        void testExecuteWithNullTarget() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(eq(mockCaster), eq(1), argThat(list -> list != null && list.isEmpty()))).thenReturn(false);

            boolean result = executor.execute(mockCaster, mockSkill, 1, (LivingEntity) null);

            assertFalse(result);
        }
    }

    // ========== castWithTriggersメソッド ==========

    @Nested
    @DisplayName("castWithTriggersメソッド")
    class CastWithTriggersTests {

        private ComponentEffectExecutor executor;

        @BeforeEach
        void setUp() {
            try (MockedStatic<TriggerManager> mockedStatic = mockStatic(TriggerManager.class)) {
                mockedStatic.when(TriggerManager::getInstance).thenReturn(mockTriggerManager);
                executor = new ComponentEffectExecutor(mockPlugin);
            }
        }

        @Test
        @DisplayName("test: スキルエフェクトがnullの場合はfalse")
        void testCastWithTriggersNullSkillEffect() {
            when(mockSkill.getComponentEffect()).thenReturn(null);

            boolean result = executor.castWithTriggers(mockCaster, mockSkill, 1, 10);

            assertFalse(result);
        }

        @Test
        @DisplayName("test: 空のコンポーネントリストの場合はfalse")
        void testCastWithTriggersEmptyComponents() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.getComponents()).thenReturn(List.of());

            boolean result = executor.castWithTriggers(mockCaster, mockSkill, 1, 10);

            assertFalse(result);
        }

        @Test
        @DisplayName("test: トリガーではないコンポーネントの場合はfalse")
        void testCastWithNonTriggerComponents() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.getComponents()).thenReturn(List.of(mockComponent));
            when(mockComponent.getType()).thenReturn(ComponentType.MECHANIC);
            when(mockComponent.getChildren()).thenReturn(List.of());

            boolean result = executor.castWithTriggers(mockCaster, mockSkill, 1, 10);

            assertFalse(result);
            verify(mockTriggerManager, never()).activateSkill(any(), any(), anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("test: duration=0で無制限の効果時間")
        void testCastWithZeroDuration() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.getComponents()).thenReturn(List.of(mockComponent));
            when(mockComponent.getType()).thenReturn(ComponentType.MECHANIC);
            when(mockComponent.getChildren()).thenReturn(List.of());

            boolean result = executor.castWithTriggers(mockCaster, mockSkill, 1, 0);

            // トリガーがないのでfalse
            assertFalse(result);
        }
    }

    // ========== TriggerManager連携 ==========

    @Nested
    @DisplayName("TriggerManager連携")
    class TriggerManagerIntegrationTests {

        @Test
        @DisplayName("test: 複数のExecutorで同じTriggerManagerインスタンス")
        void testMultipleExecutorsShareTriggerManager() {
            try (MockedStatic<TriggerManager> mockedStatic = mockStatic(TriggerManager.class)) {
                mockedStatic.when(TriggerManager::getInstance).thenReturn(mockTriggerManager);
                ComponentEffectExecutor executor1 = new ComponentEffectExecutor(mockPlugin);
                ComponentEffectExecutor executor2 = new ComponentEffectExecutor(mockPlugin);

                // TriggerManager.getInstanceは常に同じインスタンスを返す
                assertSame(executor1.getTriggerManager(), executor2.getTriggerManager());
            }
        }

        @Test
        @DisplayName("test: 異なるプラグインインスタンスで異なるExecutor")
        void testDifferentPluginsHaveDifferentExecutors() {
            RPGPlugin mockPlugin2 = mock(RPGPlugin.class);
            try (MockedStatic<TriggerManager> mockedStatic = mockStatic(TriggerManager.class)) {
                mockedStatic.when(TriggerManager::getInstance).thenReturn(mockTriggerManager);
                ComponentEffectExecutor executor1 = new ComponentEffectExecutor(mockPlugin);
                ComponentEffectExecutor executor2 = new ComponentEffectExecutor(mockPlugin2);

                // プラグインインスタンスは異なる
                assertEquals(mockPlugin, executor1.getPlugin());
                assertEquals(mockPlugin2, executor2.getPlugin());
            }
        }
    }

    // ========== エッジケース ==========

    @Nested
    @DisplayName("エッジケース")
    class EdgeCaseTests {

        private ComponentEffectExecutor executor;

        @BeforeEach
        void setUp() {
            try (MockedStatic<TriggerManager> mockedStatic = mockStatic(TriggerManager.class)) {
                mockedStatic.when(TriggerManager::getInstance).thenReturn(mockTriggerManager);
                executor = new ComponentEffectExecutor(mockPlugin);
            }
        }

        @Test
        @DisplayName("test: レベル0でも実行")
        void testExecuteWithLevelZero() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(mockCaster, 0, List.of(mockTarget))).thenReturn(true);

            boolean result = executor.execute(mockCaster, mockSkill, 0, List.of(mockTarget));

            assertTrue(result);
            verify(mockSkillEffect).execute(mockCaster, 0, List.of(mockTarget));
        }

        @Test
        @DisplayName("test: 負のレベルでも実行")
        void testExecuteWithNegativeLevel() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(mockCaster, -1, List.of(mockTarget))).thenReturn(false);

            boolean result = executor.execute(mockCaster, mockSkill, -1, List.of(mockTarget));

            assertFalse(result);
        }

        @Test
        @DisplayName("test: 大きなレベル値でも実行")
        void testExecuteWithLargeLevel() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(mockCaster, 999, List.of(mockTarget))).thenReturn(true);

            boolean result = executor.execute(mockCaster, mockSkill, 999, List.of(mockTarget));

            assertTrue(result);
        }

        @Test
        @DisplayName("test: NullPointerExceptionが適切に処理される")
        void testExecuteWithNullPointerException() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(any(), anyInt(), any()))
                .thenThrow(new NullPointerException("Test NPE"));

            boolean result = executor.execute(mockPlayer, mockSkill, 1, List.of(mockTarget));

            assertFalse(result);
            verify(mockPlayer).sendMessage(any(Component.class));
        }

        @Test
        @DisplayName("test: RuntimeExceptionが適切に処理される")
        void testExecuteWithRuntimeException() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(any(), anyInt(), any()))
                .thenThrow(new IllegalStateException("Test exception"));

            boolean result = executor.execute(mockPlayer, mockSkill, 1, List.of(mockTarget));

            assertFalse(result);
            verify(mockPlayer).sendMessage(any(Component.class));
        }

        @Test
        @DisplayName("test: 異なるduration値でcastWithTriggers")
        void testCastWithDifferentDurations() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.getComponents()).thenReturn(List.of());

            // トリガーがないのでどのdurationでもfalse
            assertFalse(executor.castWithTriggers(mockCaster, mockSkill, 1, -1));
            assertFalse(executor.castWithTriggers(mockCaster, mockSkill, 1, 0));
            assertFalse(executor.castWithTriggers(mockCaster, mockSkill, 1, 10));
            assertFalse(executor.castWithTriggers(mockCaster, mockSkill, 1, 1000));
        }
    }

    // ========== メソッドオーバーロードの確認 ==========

    @Nested
    @DisplayName("メソッドオーバーロードの確認")
    class MethodOverloadTests {

        private ComponentEffectExecutor executor;

        @BeforeEach
        void setUp() {
            try (MockedStatic<TriggerManager> mockedStatic = mockStatic(TriggerManager.class)) {
                mockedStatic.when(TriggerManager::getInstance).thenReturn(mockTriggerManager);
                executor = new ComponentEffectExecutor(mockPlugin);
            }
        }

        @Test
        @DisplayName("test: 単体ターゲット版executeが内部でリスト版を呼ぶ")
        void testSingleTargetDelegatesToListVersion() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(eq(mockCaster), eq(5), anyList())).thenReturn(true);

            executor.execute(mockCaster, mockSkill, 5, mockTarget);

            // 単体ターゲットは1要素のリストに変換される
            verify(mockSkillEffect).execute(mockCaster, 5, List.of(mockTarget));
        }

        @Test
        @DisplayName("test: 両方のexecuteメソッドで同じ結果")
        void testBothExecuteMethodsSameResult() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(eq(mockCaster), eq(3), anyList())).thenReturn(true);

            boolean result1 = executor.execute(mockCaster, mockSkill, 3, List.of(mockTarget));
            boolean result2 = executor.execute(mockCaster, mockSkill, 3, mockTarget);

            assertEquals(result1, result2);
            assertTrue(result1);
            assertTrue(result2);
        }
    }

    // ========== 例外メッセージの検証 ==========

    @Nested
    @DisplayName("例外メッセージの検証")
    class ExceptionMessageTests {

        private ComponentEffectExecutor executor;

        @BeforeEach
        void setUp() {
            try (MockedStatic<TriggerManager> mockedStatic = mockStatic(TriggerManager.class)) {
                mockedStatic.when(TriggerManager::getInstance).thenReturn(mockTriggerManager);
                executor = new ComponentEffectExecutor(mockPlugin);
            }
        }

        @Test
        @DisplayName("test: エラーメッセージが日本語である")
        void testErrorMessageIsJapanese() {
            when(mockSkill.getComponentEffect()).thenReturn(mockSkillEffect);
            when(mockSkillEffect.execute(any(), anyInt(), any()))
                .thenThrow(new RuntimeException("Test error"));

            executor.execute(mockPlayer, mockSkill, 1, List.of(mockTarget));

            // エラーメッセージが送信されることを検証
            verify(mockPlayer).sendMessage(any(Component.class));
        }
    }
}
