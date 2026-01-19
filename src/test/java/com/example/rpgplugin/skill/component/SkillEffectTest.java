package com.example.rpgplugin.skill.component;

import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SkillEffectのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SkillEffect: スキル効果")
class SkillEffectTest {

    @Mock
    private LivingEntity mockCaster;

    @Mock
    private LivingEntity mockTarget;

    @Mock
    private EffectComponent mockComponent1;

    @Mock
    private EffectComponent mockComponent2;

    @BeforeEach
    void setUp() {
        when(mockCaster.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockCaster.getEntityId()).thenReturn(100);
        when(mockTarget.getEntityId()).thenReturn(200);
    }

    // ========== コンストラクタと基本機能 ==========

    @Nested
    @DisplayName("コンストラクタと基本機能")
    class BasicFunctionalityTests {

        @Test
        @DisplayName("test: コンストラクタでスキルIDを設定")
        void testConstructorWithSkillId() {
            SkillEffect skillEffect = new SkillEffect("test_skill");
            assertEquals("test_skill", skillEffect.getSkillId());
        }

        @Test
        @DisplayName("test: getSkillIdは設定されたスキルIDを返す")
        void testGetSkillId() {
            SkillEffect skillEffect = new SkillEffect("fireball");
            assertEquals("fireball", skillEffect.getSkillId());
        }

        @Test
        @DisplayName("test: 初期状態のコンポーネントリストは空")
        void testInitialComponentsListIsEmpty() {
            SkillEffect skillEffect = new SkillEffect("test");
            assertTrue(skillEffect.getComponents().isEmpty());
        }

        @Test
        @DisplayName("test: clearAllActiveですべてのアクティブスキルをクリア")
        void testClearAllActive() {
            SkillEffect skillEffect = new SkillEffect("test");
            skillEffect.activate(mockCaster, 1, 10);
            skillEffect.activate(mockTarget, 1, 10);
            assertTrue(skillEffect.isActive(mockCaster));
            assertTrue(skillEffect.isActive(mockTarget));

            skillEffect.clearAllActive();

            assertFalse(skillEffect.isActive(mockCaster));
            assertFalse(skillEffect.isActive(mockTarget));
        }
    }

    // ========== コンポーネント管理 ==========

    @Nested
    @DisplayName("コンポーネント管理")
    class ComponentManagementTests {

        private SkillEffect skillEffect;

        @BeforeEach
        void setUp() {
            skillEffect = new SkillEffect("test_skill");
        }

        @Test
        @DisplayName("test: コンポーネントを追加できる")
        void testAddComponent() {
            skillEffect.addComponent(mockComponent1);
            assertEquals(1, skillEffect.getComponents().size());
            assertTrue(skillEffect.getComponents().contains(mockComponent1));
        }

        @Test
        @DisplayName("test: 複数のコンポーネントを追加できる")
        void testAddMultipleComponents() {
            skillEffect.addComponent(mockComponent1);
            skillEffect.addComponent(mockComponent2);
            assertEquals(2, skillEffect.getComponents().size());
        }

        @Test
        @DisplayName("test: nullを追加してもエラーにならない")
        void testAddNullComponent() {
            skillEffect.addComponent(null);
            assertTrue(skillEffect.getComponents().isEmpty());
        }

        @Test
        @DisplayName("test: getComponentsでコンポーネントリストを取得")
        void testGetComponents() {
            skillEffect.addComponent(mockComponent1);
            skillEffect.addComponent(mockComponent2);
            List<EffectComponent> components = skillEffect.getComponents();
            assertEquals(2, components.size());
            assertTrue(components.contains(mockComponent1));
            assertTrue(components.contains(mockComponent2));
        }

        @Test
        @DisplayName("test: コンポーネント追加時にスキルが設定される")
        void testSkillSetOnComponent() {
            skillEffect.addComponent(mockComponent1);
            verify(mockComponent1).setSkill(skillEffect);
        }

        @Test
        @DisplayName("test: null追加時はスキルが設定されない")
        void testNoSkillSetOnNullComponent() {
            skillEffect.addComponent(null);
            verify(mockComponent1, never()).setSkill(any());
        }
    }

    // ========== スキル実行 ==========

    @Nested
    @DisplayName("スキル実行")
    class ExecutionTests {

        private SkillEffect skillEffect;

        @BeforeEach
        void setUp() {
            skillEffect = new SkillEffect("test_skill");
        }

        @Test
        @DisplayName("test: コンポーネントが空の場合は実行失敗")
        void testExecuteWithNoComponents() {
            boolean result = skillEffect.execute(mockCaster, 1, List.of(mockTarget));
            assertFalse(result);
        }

        @Test
        @DisplayName("test: 全コンポーネントが失敗した場合は実行失敗")
        void testExecuteWithAllFailedComponents() {
            when(mockComponent1.execute(mockCaster, 1, List.of(mockTarget))).thenReturn(false);
            when(mockComponent2.execute(mockCaster, 1, List.of(mockTarget))).thenReturn(false);
            skillEffect.addComponent(mockComponent1);
            skillEffect.addComponent(mockComponent2);

            boolean result = skillEffect.execute(mockCaster, 1, List.of(mockTarget));

            assertFalse(result);
        }

        @Test
        @DisplayName("test: 1つのコンポーネントが成功すれば実行成功")
        void testExecuteWithOneSuccess() {
            when(mockComponent1.execute(mockCaster, 1, List.of(mockTarget))).thenReturn(false);
            when(mockComponent2.execute(mockCaster, 1, List.of(mockTarget))).thenReturn(true);
            skillEffect.addComponent(mockComponent1);
            skillEffect.addComponent(mockComponent2);

            boolean result = skillEffect.execute(mockCaster, 1, List.of(mockTarget));

            assertTrue(result);
        }

        @Test
        @DisplayName("test: すべてのコンポーネントが実行される")
        void testAllComponentsExecuted() {
            when(mockComponent1.execute(mockCaster, 1, List.of(mockTarget))).thenReturn(true);
            when(mockComponent2.execute(mockCaster, 1, List.of(mockTarget))).thenReturn(true);
            skillEffect.addComponent(mockComponent1);
            skillEffect.addComponent(mockComponent2);

            skillEffect.execute(mockCaster, 1, List.of(mockTarget));

            verify(mockComponent1).execute(mockCaster, 1, List.of(mockTarget));
            verify(mockComponent2).execute(mockCaster, 1, List.of(mockTarget));
        }

        @Test
        @DisplayName("test: 複数ターゲットを渡せる")
        void testExecuteWithMultipleTargets() {
            when(mockComponent1.execute(eq(mockCaster), eq(1), anyList())).thenReturn(true);
            skillEffect.addComponent(mockComponent1);

            boolean result = skillEffect.execute(mockCaster, 1, List.of(mockTarget, mockCaster));

            assertTrue(result);
            verify(mockComponent1).execute(eq(mockCaster), eq(1), anyList());
        }
    }

    // ========== アクティブスキル管理 ==========

    @Nested
    @DisplayName("アクティブスキル管理")
    class ActiveSkillManagementTests {

        private SkillEffect skillEffect;

        @BeforeEach
        void setUp() {
            skillEffect = new SkillEffect("test_skill");
        }

        @Test
        @DisplayName("test: activateでスキルをアクティブ化")
        void testActivateSkill() {
            skillEffect.activate(mockCaster, 5, 30);
            assertTrue(skillEffect.isActive(mockCaster));
        }

        @Test
        @DisplayName("test: アクティブ化していないスキルはisActiveでfalse")
        void testInactiveSkillReturnsFalse() {
            assertFalse(skillEffect.isActive(mockCaster));
        }

        @Test
        @DisplayName("test: activateしたレベルを取得できる")
        void testGetActiveLevel() {
            skillEffect.activate(mockCaster, 7, 0);
            assertEquals(7, skillEffect.getActiveLevel(mockCaster));
        }

        @Test
        @DisplayName("test: アクティブ化していない場合はgetActiveLevelで-1")
        void testGetActiveLevelForInactiveSkill() {
            assertEquals(-1, skillEffect.getActiveLevel(mockCaster));
        }

        @Test
        @DisplayName("test: deactivateでスキルを非アクティブ化")
        void testDeactivateSkill() {
            skillEffect.activate(mockCaster, 1, 10);
            assertTrue(skillEffect.isActive(mockCaster));

            skillEffect.deactivate(mockCaster);

            assertFalse(skillEffect.isActive(mockCaster));
        }

        @Test
        @DisplayName("test: deactivate時にコンポーネントのcleanUpが呼ばれる")
        void testDeactivateCallsCleanup() {
            skillEffect.addComponent(mockComponent1);
            skillEffect.activate(mockCaster, 1, 10);

            skillEffect.deactivate(mockCaster);

            verify(mockComponent1).cleanUp(mockCaster);
        }

        @Test
        @DisplayName("test: 存在しないスキルのdeactivateはエラーにならない")
        void testDeactivateNonExistentSkill() {
            assertDoesNotThrow(() -> skillEffect.deactivate(mockCaster));
            assertFalse(skillEffect.isActive(mockCaster));
        }

        @Test
        @DisplayName("test: 複数のエンティティのスキルを管理できる")
        void testManageMultipleEntities() {
            skillEffect.activate(mockCaster, 1, 10);
            skillEffect.activate(mockTarget, 2, 20);

            assertTrue(skillEffect.isActive(mockCaster));
            assertTrue(skillEffect.isActive(mockTarget));
            assertEquals(1, skillEffect.getActiveLevel(mockCaster));
            assertEquals(2, skillEffect.getActiveLevel(mockTarget));
        }

        @Test
        @DisplayName("test: 別々のエンティティをdeactivateできる")
        void testDeactivateSpecificEntity() {
            skillEffect.activate(mockCaster, 1, 10);
            skillEffect.activate(mockTarget, 1, 10);

            skillEffect.deactivate(mockCaster);

            assertFalse(skillEffect.isActive(mockCaster));
            assertTrue(skillEffect.isActive(mockTarget));
        }
    }

    // ========== キャストデータ管理 ==========

    @Nested
    @DisplayName("キャストデータ管理")
    class CastDataManagementTests {

        @BeforeEach
        void setUp() {
            // 既存のキャストデータをクリア
            SkillEffect.clearCastData(mockCaster);
            SkillEffect.clearCastData(mockTarget);
        }

        @Test
        @DisplayName("test: getCastDataで空のマップを取得")
        void testGetCastDataReturnsEmptyMap() {
            var data = SkillEffect.getCastData(mockCaster);
            assertNotNull(data);
            assertTrue(data.isEmpty());
        }

        @Test
        @DisplayName("test: 同じエンティティで同じマップを取得")
        void testGetCastDataReturnsSameMap() {
            var data1 = SkillEffect.getCastData(mockCaster);
            var data2 = SkillEffect.getCastData(mockCaster);

            assertSame(data1, data2);
        }

        @Test
        @DisplayName("test: キャストデータを保存して取得できる")
        void testStoreAndRetrieveCastData() {
            var data = SkillEffect.getCastData(mockCaster);
            data.put("key1", "value1");
            data.put("key2", 123);

            var retrieved = SkillEffect.getCastData(mockCaster);

            assertEquals("value1", retrieved.get("key1"));
            assertEquals(123, retrieved.get("key2"));
        }

        @Test
        @DisplayName("test: clearCastDataでデータをクリア")
        void testClearCastData() {
            var data = SkillEffect.getCastData(mockCaster);
            data.put("test", "value");
            assertEquals(1, data.size());

            SkillEffect.clearCastData(mockCaster);

            var newData = SkillEffect.getCastData(mockCaster);
            assertTrue(newData.isEmpty());
            assertNotSame(data, newData);
        }

        @Test
        @DisplayName("test: 異なるエンティティのデータは分離されている")
        void testDifferentEntitiesHaveSeparateData() {
            var data1 = SkillEffect.getCastData(mockCaster);
            var data2 = SkillEffect.getCastData(mockTarget);

            data1.put("shared", "value1");
            data2.put("shared", "value2");

            assertEquals("value1", data1.get("shared"));
            assertEquals("value2", data2.get("shared"));
            assertNotSame(data1, data2);
        }
    }

    // ========== 有効期限チェック ==========

    @Nested
    @DisplayName("有効期限チェック")
    class ExpiryTests {

        private SkillEffect skillEffect;

        @BeforeEach
        void setUp() {
            skillEffect = new SkillEffect("test_skill");
        }

        @Test
        @DisplayName("test: duration=0の場合は期限切れしない")
        void testZeroDurationNoExpiry() {
            skillEffect.activate(mockCaster, 1, 0);

            // 直後は有効
            assertTrue(skillEffect.isActive(mockCaster));
            assertEquals(1, skillEffect.getActiveLevel(mockCaster));
        }

        @Test
        @DisplayName("test: 正のdurationで期限切れが発生する")
        void testPositiveDurationExpiry() throws InterruptedException {
            // 1秒の有効期限を設定
            skillEffect.activate(mockCaster, 1, 1);

            // 直後は有効
            assertTrue(skillEffect.isActive(mockCaster));

            // 1.1秒待機（有効期限切れ）
            Thread.sleep(1100);

            assertFalse(skillEffect.isActive(mockCaster));
            assertEquals(-1, skillEffect.getActiveLevel(mockCaster));
        }

        @Test
        @DisplayName("test: 期限切れ後は自動的にdeactivateされる")
        void testAutoDeactivateAfterExpiry() throws InterruptedException {
            skillEffect.addComponent(mockComponent1);
            skillEffect.activate(mockCaster, 1, 1);

            Thread.sleep(1100);

            // isActiveを呼ぶと内部でdeactivateが呼ばれる
            skillEffect.isActive(mockCaster);

            verify(mockComponent1).cleanUp(mockCaster);
        }
    }

    // ========== 複数のスキルエンティティ ==========

    @Nested
    @DisplayName("複数のスキルエンティティ")
    class MultipleSkillEntitiesTests {

        private SkillEffect skillEffect1;
        private SkillEffect skillEffect2;

        @BeforeEach
        void setUp() {
            skillEffect1 = new SkillEffect("skill1");
            skillEffect2 = new SkillEffect("skill2");
        }

        @Test
        @DisplayName("test: 異なるスキルは独立してアクティブ化できる")
        void testDifferentSkillsIndependent() {
            skillEffect1.activate(mockCaster, 1, 10);
            skillEffect2.activate(mockTarget, 1, 10);

            assertTrue(skillEffect1.isActive(mockCaster));
            assertFalse(skillEffect1.isActive(mockTarget));
            assertFalse(skillEffect2.isActive(mockCaster));
            assertTrue(skillEffect2.isActive(mockTarget));
        }
    }
}
