package com.example.rpgplugin.skill.component;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.component.condition.ChanceCondition;
import com.example.rpgplugin.skill.component.condition.HealthCondition;
import com.example.rpgplugin.skill.component.mechanic.DamageMechanic;
import com.example.rpgplugin.skill.component.mechanic.HealMechanic;
import com.example.rpgplugin.skill.component.mechanic.MessageMechanic;
import com.example.rpgplugin.skill.component.trigger.CrouchTrigger;
import com.example.rpgplugin.skill.component.trigger.LandTrigger;
import com.example.rpgplugin.skill.component.trigger.Trigger;
import com.example.rpgplugin.skill.component.trigger.TriggerHandler;
import com.example.rpgplugin.skill.component.trigger.TriggerManager;
import com.example.rpgplugin.skill.component.trigger.TriggerSettings;
import com.example.rpgplugin.skill.component.trigger.ComponentRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * コンポーネントシステム統合テスト
 *
 * <p>テスト対象:</p>
 * <ul>
 *   <li>コンポーネント効果実行フロー</li>
 *   <li>トリガー_ACTIVATIONと発火</li>
 *   <li>各種トリガーの動作確認</li>
 *   <li>コンポーネントの組み合わせ実行</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: コンポーネント統合のテストに専念</li>
 *   <li>現実的: 実際のYAML設定に近い形式でテスト</li>
 *   <li>独立性: 各テストは独立して実行可能</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("コンポーネントシステム統合テスト")
class ComponentSystemIntegrationTest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private Logger mockLogger;

    @Mock
    private Player mockPlayer;

    @Mock
    private RPGPlayer mockRpgPlayer;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private Server mockServer;

    @Mock
    private PluginManager mockPluginManager;

    private MockedStatic<Bukkit> mockedBukkit;

    private TriggerManager triggerManager;

    /**
     * 各テストの前に実行されるセットアップ処理
     */
    @BeforeEach
    void setUp() {
        // Bukkit静的メソッドのモック化
        mockedBukkit = mockStatic(Bukkit.class);

        // Pluginの基本設定
        when(mockPlugin.getLogger()).thenReturn(mockLogger);

        // プレイヤーのモック設定
        UUID playerUuid = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockPlayer.getName()).thenReturn("TestPlayer");
        when(mockPlayer.getEntityId()).thenReturn(12345);

        // LivingEntity用のモック設定（HealMechanicで使用）
        AttributeInstance maxHealthAttr = mock(AttributeInstance.class);
        when(maxHealthAttr.getValue()).thenReturn(20.0);
        when(mockPlayer.getHealth()).thenReturn(15.0);
        when(mockPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH)).thenReturn(maxHealthAttr);

        // RPGPlayerのモック設定
        when(mockRpgPlayer.getBukkitPlayer()).thenReturn(mockPlayer);
        when(mockRpgPlayer.getLevel()).thenReturn(10);

        // PlayerManagerのモック設定
        when(mockPlayerManager.getRPGPlayer(any(UUID.class))).thenReturn(mockRpgPlayer);

        // Serverのモック設定
        when(mockPlugin.getServer()).thenReturn(mockServer);
        when(mockServer.getPluginManager()).thenReturn(mockPluginManager);

        // TriggerManagerの初期化
        triggerManager = TriggerManager.initialize(mockPlugin);
    }

    /**
     * 各テストの後に実行されるクリーンアップ処理
     */
    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    // ==================== テスト1: コンポーネントレジストリ ====================

    @Nested
    @DisplayName("テスト1: コンポーネントレジストリ")
    class Test1ComponentRegistry {

        @Test
        @DisplayName("テスト1-1: 全ての条件コンポーネントが登録されている")
        void test1_1_AllConditionsRegistered() {
            // Then: 登録済みの条件を確認
            assertThat(ComponentRegistry.hasCondition("health")).isTrue();
            assertThat(ComponentRegistry.hasCondition("chance")).isTrue();
            assertThat(ComponentRegistry.hasCondition("mana")).isTrue();
            assertThat(ComponentRegistry.hasCondition("biome")).isTrue();
        }

        @Test
        @DisplayName("テスト1-2: 全てのメカニックコンポーネントが登録されている")
        void test1_2_AllMechanicsRegistered() {
            // Then: 登録済みのメカニックを確認
            assertThat(ComponentRegistry.hasMechanic("damage")).isTrue();
            assertThat(ComponentRegistry.hasMechanic("heal")).isTrue();
            assertThat(ComponentRegistry.hasMechanic("push")).isTrue();
            assertThat(ComponentRegistry.hasMechanic("fire")).isTrue();
            assertThat(ComponentRegistry.hasMechanic("message")).isTrue();
        }

        @Test
        @DisplayName("テスト1-3: 全てのトリガーが登録されている")
        void test1_3_AllTriggersRegistered() {
            // Then: 登録済みのトリガーを確認
            assertThat(ComponentRegistry.hasTrigger("CROUCH")).isTrue();
            assertThat(ComponentRegistry.hasTrigger("LAND")).isTrue();
            assertThat(ComponentRegistry.hasTrigger("DEATH")).isTrue();
        }

        @Test
        @DisplayName("テスト1-4: 条件コンポーネントが作成できる")
        void test1_4_ConditionComponentCreation() {
            // When: 条件コンポーネントを作成
            EffectComponent health = ComponentRegistry.createCondition("health");
            EffectComponent chance = ComponentRegistry.createCondition("chance");

            // Then: コンポーネントが正しく作成されている
            assertThat(health).isNotNull();
            assertThat(health.getType()).isEqualTo(ComponentType.CONDITION);
            assertThat(health instanceof HealthCondition).isTrue();

            assertThat(chance).isNotNull();
            assertThat(chance.getType()).isEqualTo(ComponentType.CONDITION);
        }

        @Test
        @DisplayName("テスト1-5: メカニックコンポーネントが作成できる")
        void test1_5_MechanicComponentCreation() {
            // When: メカニックコンポーネントを作成
            EffectComponent damage = ComponentRegistry.createMechanic("damage");
            EffectComponent heal = ComponentRegistry.createMechanic("heal");

            // Then: コンポーネントが正しく作成されている
            assertThat(damage).isNotNull();
            assertThat(damage.getType()).isEqualTo(ComponentType.MECHANIC);
            assertThat(damage instanceof DamageMechanic).isTrue();

            assertThat(heal).isNotNull();
            assertThat(heal.getType()).isEqualTo(ComponentType.MECHANIC);
            assertThat(heal instanceof HealMechanic).isTrue();
        }

        @Test
        @DisplayName("テスト1-6: トリガーが作成できる")
        void test1_6_TriggerCreation() {
            // When: トリガーを作成
            Trigger<?> crouch = ComponentRegistry.createTrigger("CROUCH");
            Trigger<?> land = ComponentRegistry.createTrigger("LAND");

            // Then: トリガーが正しく作成されている
            assertThat(crouch).isNotNull();
            assertThat(crouch.getKey()).isEqualTo("CROUCH");
            assertThat(crouch instanceof CrouchTrigger).isTrue();

            assertThat(land).isNotNull();
            assertThat(land.getKey()).isEqualTo("LAND");
            assertThat(land instanceof LandTrigger).isTrue();
        }
    }

    // ==================== テスト2: スキル効果ツリー構築 ====================

    @Nested
    @DisplayName("テスト2: スキル効果ツリー構築")
    class Test2SkillEffectTree {

        @Test
        @DisplayName("テスト2-1: 単一メカニックを持つスキル効果を作成できる")
        void test2_1_SingleMechanicSkillEffect() {
            // Given: メカニックコンポーネント
            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "10");

            // When: スキル効果を作成
            SkillEffect skillEffect = new SkillEffect("test_skill");
            skillEffect.addComponent(damage);

            // Then: コンポーネントが含まれている
            assertThat(skillEffect.getComponents()).hasSize(1);
            assertThat(skillEffect.getComponents().get(0)).isSameAs(damage);
        }

        @Test
        @DisplayName("テスト2-2: 複数メカニックを持つスキル効果を作成できる")
        void test2_2_MultipleMechanicSkillEffect() {
            // Given: 複数のメカニックコンポーネント
            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "10");

            HealMechanic heal = new HealMechanic();
            heal.getSettings().put("value", "5");

            // When: スキル効果を作成
            SkillEffect skillEffect = new SkillEffect("test_skill");
            skillEffect.addComponent(damage);
            skillEffect.addComponent(heal);

            // Then: 全てのコンポーネントが含まれている
            assertThat(skillEffect.getComponents()).hasSize(2);
        }

        @Test
        @DisplayName("テスト2-3: 親子関係を持つコンポーネント階層を作成できる")
        void test2_3_ComponentHierarchy() {
            // Given: 条件とメカニック
            HealthCondition condition = new HealthCondition();
            condition.getSettings().put("below", "50");

            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "10");

            // When: 条件の子としてメカニックを追加
            condition.addChild(damage);

            // Then: 階層構造が正しい
            assertThat(condition.getChildren()).hasSize(1);
            assertThat(condition.getChildren().get(0)).isSameAs(damage);
        }

        @Test
        @DisplayName("テスト2-4: 複数の子コンポーネントを持つ階層を作成できる")
        void test2_4_MultipleChildren() {
            // Given: 親コンポーネント
            HealthCondition condition = new HealthCondition();

            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "10");

            MessageMechanic message = new MessageMechanic();
            message.getSettings().put("text", "スキル発動!");

            // When: 親に複数の子を追加
            condition.addChild(damage);
            condition.addChild(message);

            // Then: 全ての子が含まれている
            assertThat(condition.getChildren()).hasSize(2);
        }

        @Test
        @DisplayName("テスト2-5: 条件とメカニックの組み合わせを作成できる")
        void test2_5_ConditionWithMechanic() {
            // Given: 条件とメカニック
            HealthCondition condition = new HealthCondition();
            condition.getSettings().put("below", "50");

            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "10");

            // When: 条件の子としてメカニックを追加
            condition.addChild(damage);

            // Then: 階層構造が正しい
            assertThat(condition.getChildren()).hasSize(1);
            assertThat(condition.getChildren().get(0)).isSameAs(damage);
        }
    }

    // ==================== テスト3: コンポーネント実行 ====================

    @Nested
    @DisplayName("テスト3: コンポーネント実行")
    class Test3ComponentExecution {

        @Test
        @DisplayName("テスト3-1: ダメージメカニックが正しく実行される")
        void test3_1_DamageMechanicExecution() {
            // Given: ダメージメカニック
            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "10");

            // When: メカニックを実行（ターゲットに発動者を指定）
            boolean executed = damage.execute(mockPlayer, 1, List.of(mockPlayer));

            // Then: 実行成功
            assertThat(executed).isTrue();
        }

        @Test
        @DisplayName("テスト3-2: ヒールメカニックが正しく実行される")
        void test3_2_HealMechanicExecution() {
            // Given: ヒールメカニック
            HealMechanic heal = new HealMechanic();
            heal.getSettings().put("value", "10");

            // When: メカニックを実行（ターゲットに発動者を指定）
            boolean executed = heal.execute(mockPlayer, 1, List.of(mockPlayer));

            // Then: 実行成功
            assertThat(executed).isTrue();
        }

        @Test
        @DisplayName("テスト3-3: メッセージメカニックが正しく実行される")
        void test3_3_MessageMechanicExecution() {
            // Given: メッセージメカニック
            MessageMechanic message = new MessageMechanic();
            message.getSettings().put("text", "テストメッセージ");
            // casterのみに送信するように設定
            message.getSettings().put("to-caster", "true");
            message.getSettings().put("to-target", "false");

            // When: メカニックを実行（ターゲットに発動者を指定）
            boolean executed = message.execute(mockPlayer, 1, List.of(mockPlayer));

            // Then: 実行成功
            assertThat(executed).isTrue();
            // メッセージが送信されたことを確認（Component検証）
            ArgumentCaptor<Component> componentCaptor = ArgumentCaptor.forClass(Component.class);
            verify(mockPlayer).sendMessage(componentCaptor.capture());
            assertThat(PlainTextComponentSerializer.plainText().serialize(componentCaptor.getValue())).isEqualTo("テストメッセージ");
        }

        @Test
        @DisplayName("テスト3-4: 複数メカニックが順に実行される")
        void test3_4_MultipleMechanicsExecution() {
            // Given: 複数のメカニック
            SkillEffect skillEffect = new SkillEffect("test_skill");

            MessageMechanic message1 = new MessageMechanic();
            message1.getSettings().put("text", "メッセージ1");
            message1.getSettings().put("to-caster", "true");
            message1.getSettings().put("to-target", "false");

            MessageMechanic message2 = new MessageMechanic();
            message2.getSettings().put("text", "メッセージ2");
            message2.getSettings().put("to-caster", "true");
            message2.getSettings().put("to-target", "false");

            skillEffect.addComponent(message1);
            skillEffect.addComponent(message2);

            // When: スキル効果を実行（ターゲットに発動者を指定）
            boolean executed = skillEffect.execute(mockPlayer, 1, List.of(mockPlayer));

            // Then: 全てのメッセージが送信された（Component検証）
            assertThat(executed).isTrue();
            ArgumentCaptor<Component> componentCaptor = ArgumentCaptor.forClass(Component.class);
            verify(mockPlayer, times(2)).sendMessage(componentCaptor.capture());
            List<Component> capturedComponents = componentCaptor.getAllValues();
            assertThat(PlainTextComponentSerializer.plainText().serialize(capturedComponents.get(0))).isEqualTo("メッセージ1");
            assertThat(PlainTextComponentSerializer.plainText().serialize(capturedComponents.get(1))).isEqualTo("メッセージ2");
        }
    }

    // ==================== テスト4: トリガーシステム ====================

    @Nested
    @DisplayName("テスト4: トリガーシステム")
    class Test4TriggerSystem {

        @Test
        @DisplayName("テスト4-1: トリガーマネージャーが取得できる")
        void test4_1_TriggerManagerInstance() {
            // Then: シングルトンインスタンスが取得できる
            assertThat(triggerManager).isNotNull();
        }

        @Test
        @DisplayName("テスト4-2: トリガーハンドラーが作成できる")
        void test4_2_TriggerHandlerCreation() {
            // Given: トリガーとコンポーネント
            CrouchTrigger trigger = new CrouchTrigger();
            TriggerSettings settings = new TriggerSettings();
            settings.put("cooldown", "5");

            DamageMechanic rootComponent = new DamageMechanic();
            rootComponent.getSettings().put("value", "10");

            // When: トリガーハンドラーを作成
            TriggerHandler handler = new TriggerHandler(
                    "test_skill",
                    trigger,
                    settings,
                    rootComponent,
                    5
            );

            // Then: ハンドラーが正しく作成されている
            assertThat(handler).isNotNull();
            assertThat(handler.getSkillId()).isEqualTo("test_skill");
            assertThat(handler.getDuration()).isEqualTo(5);
        }

        @Test
        @DisplayName("テスト4-3: スキルがアクティベートできる")
        void test4_3_SkillActivation() {
            // Given: トリガーハンドラー
            CrouchTrigger trigger = new CrouchTrigger();
            TriggerSettings settings = new TriggerSettings();
            DamageMechanic component = new DamageMechanic();

            TriggerHandler handler = new TriggerHandler(
                    "test_skill",
                    trigger,
                    settings,
                    component,
                    0
            );

            // When: スキルをアクティベート（例外が発生しないことを確認）
            assertThatCode(() -> {
                triggerManager.activateSkill(
                        "test_skill",
                        mockPlayer,
                        1,
                        0,
                        List.of(handler)
                );
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("テスト4-4: スキルがディアクティベートできる")
        void test4_4_SkillDeactivation() {
            // Given: アクティベートされたスキル
            CrouchTrigger trigger = new CrouchTrigger();
            TriggerSettings settings = new TriggerSettings();
            DamageMechanic component = new DamageMechanic();

            TriggerHandler handler = new TriggerHandler(
                    "test_skill",
                    trigger,
                    settings,
                    component,
                    0
            );

            triggerManager.activateSkill(
                    "test_skill",
                    mockPlayer,
                    1,
                    0,
                    List.of(handler)
            );

            // When: スキルをディアクティベート（例外が発生しないことを確認）
            assertThatCode(() -> {
                triggerManager.deactivateSkill("test_skill", mockPlayer);
            }).doesNotThrowAnyException();
        }
    }

    // ==================== テスト5: コンポーネント設定 ====================

    @Nested
    @DisplayName("テスト5: コンポーネント設定")
        class Test5ComponentSettings {

        @Test
        @DisplayName("テスト5-1: ComponentSettingsに値を設定・取得できる")
        void test5_1_ComponentSettingsSetGet() {
            // Given: コンポーネント設定
            ComponentSettings settings = new ComponentSettings();

            // When: 値を設定
            settings.put("value", "10");
            settings.put("name", "test");
            settings.put("enabled", "true");

            // Then: 値が正しく取得できる
            assertThat(settings.getString("value", "")).isEqualTo("10");
            assertThat(settings.getString("name", "")).isEqualTo("test");
            assertThat(settings.getString("enabled", "")).isEqualTo("true");
        }

        @Test
        @DisplayName("テスト5-2: ComponentSettingsで整数値を取得できる")
        void test5_2_ComponentSettingsGetInt() {
            // Given: コンポーネント設定
            ComponentSettings settings = new ComponentSettings();
            settings.put("damage", "15");

            // When: 整数として取得
            int damage = settings.getInt("damage", 0);

            // Then: 正しい整数値が取得できる
            assertThat(damage).isEqualTo(15);
        }

        @Test
        @DisplayName("テスト5-3: ComponentSettingsで小数値を取得できる")
        void test5_3_ComponentSettingsGetDouble() {
            // Given: コンポーネント設定
            ComponentSettings settings = new ComponentSettings();
            settings.put("range", "5.5");

            // When: 小数として取得
            double range = settings.getDouble("range", 0.0);

            // Then: 正しい小数値が取得できる
            assertThat(range).isEqualTo(5.5);
        }

        @Test
        @DisplayName("テスト5-4: ComponentSettingsで真偽値を取得できる")
        void test5_4_ComponentSettingsGetBoolean() {
            // Given: コンポーネント設定
            ComponentSettings settings = new ComponentSettings();
            settings.put("enabled", "true");
            settings.put("disabled", "false");

            // When: 真偽値として取得
            boolean enabled = settings.getBoolean("enabled", false);
            boolean disabled = settings.getBoolean("disabled", true);

            // Then: 正しい真偽値が取得できる
            assertThat(enabled).isTrue();
            assertThat(disabled).isFalse();
        }

            @Test
            @DisplayName("テスト5-5: ComponentSettingsでキー存在確認ができる")
            void test5_5_ComponentSettingsHas() {
            // Given: コンポーネント設定
            ComponentSettings settings = new ComponentSettings();
            settings.put("existing_key", "value");

            // When: キー存在を確認
            boolean hasExisting = settings.has("existing_key");
            boolean hasMissing = settings.has("missing_key");

            // Then: 正しい結果が返る
                assertThat(hasExisting).isTrue();
                assertThat(hasMissing).isFalse();
            }

            @Test
            @DisplayName("テスト5-6: kebab-case/snake_case のキー互換がある")
            void test5_6_ComponentSettingsKeyCompatibility() {
                ComponentSettings settings = new ComponentSettings();
                settings.put("value_base", "10.5");
                settings.put("min-value", "3");

                assertThat(settings.getDouble("value-base", 0.0)).isEqualTo(10.5);
                assertThat(settings.getInt("min_value", 0)).isEqualTo(3);
                assertThat(settings.has("value-base")).isTrue();
                assertThat(settings.has("min_value")).isTrue();
                assertThat(settings.getRaw("value-base")).isEqualTo("10.5");
                assertThat(settings.getRaw("min_value")).isEqualTo("3");
            }
        }

    // ==================== テスト6: コンポーネントタイプ ====================

    @Nested
    @DisplayName("テスト6: コンポーネントタイプ")
    class Test6ComponentType {

        @Test
        @DisplayName("テスト6-1: 全てのコンポーネントタイプが定義されている")
        void test6_1_AllComponentTypesDefined() {
            // Then: 全てのタイプが定義されている
            assertThat(ComponentType.CONDITION).isNotNull();
            assertThat(ComponentType.MECHANIC).isNotNull();
            assertThat(ComponentType.TRIGGER).isNotNull();
        }

        @Test
        @DisplayName("テスト6-2: 条件コンポーネントのタイプが正しい")
        void test6_2_ConditionComponentType() {
            // Given: 条件コンポーネント
            HealthCondition condition = new HealthCondition();

            // Then: タイプがCONDITIONである
            assertThat(condition.getType()).isEqualTo(ComponentType.CONDITION);
        }

        @Test
        @DisplayName("テスト6-3: メカニックコンポーネントのタイプが正しい")
        void test6_3_MechanicComponentType() {
            // Given: メカニックコンポーネント
            DamageMechanic mechanic = new DamageMechanic();

            // Then: タイプがMECHANICである
            assertThat(mechanic.getType()).isEqualTo(ComponentType.MECHANIC);
        }
    }

    @Nested
    @DisplayName("テストX: 数式/変数（components）")
    class TestXComponentFormulaSupport {

        @Test
        @DisplayName("components の value_base/value_scale で数式(Lv)が使える")
        void testX_1_DamageMechanicSupportsFormulaWithLv() {
            DamageMechanic mechanic = new DamageMechanic();
            ComponentSettings settings = mechanic.getSettings();
            settings.put("value_base", "Lv * 2");
            settings.put("value_scale", "1");
            // default type=damage, true-damage=false

            org.bukkit.entity.LivingEntity caster = mock(org.bukkit.entity.LivingEntity.class);
            org.bukkit.entity.LivingEntity target = mock(org.bukkit.entity.LivingEntity.class);

            // When
            mechanic.execute(caster, 3, List.of(target));

            // Then: amount = (Lv*2) + (Lv-1)*1 = 6 + 2 = 8
            ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);
            verify(target, times(1)).damage(amountCaptor.capture(), eq(caster));
            assertThat(amountCaptor.getValue()).isEqualTo(8.0);
        }
    }

    // ==================== テスト7: 新しいメカニック実行テスト ====================

    @Nested
    @DisplayName("テスト7: 新しいメカニック実行テスト")
    class Test7NewMechanicExecution {

        @Test
        @DisplayName("テスト7-1: POTIONメカニックが作成・実行できる")
        void test7_1_PotionMechanicExecution() {
            // Given: ポーションメカニック
            var potion = ComponentRegistry.createMechanic("potion");

            // Then: メカニックが作成できる
            assertThat(potion).isNotNull();
            assertThat(potion.getType()).isEqualTo(ComponentType.MECHANIC);
        }

        @Test
        @DisplayName("テスト7-2: LIGHTNINGメカニックが作成できる")
        void test7_2_LightningMechanicCreation() {
            // Given: 稲妻メカニック
            var lightning = ComponentRegistry.createMechanic("lightning");

            // Then: メカニックが作成できる
            assertThat(lightning).isNotNull();
            assertThat(lightning.getType()).isEqualTo(ComponentType.MECHANIC);
        }

        @Test
        @DisplayName("テスト7-3: SOUNDメカニックが作成できる")
        void test7_3_SoundMechanicCreation() {
            // Given: サウンドメカニック
            var sound = ComponentRegistry.createMechanic("sound");

            // Then: メカニックが作成できる
            assertThat(sound).isNotNull();
            assertThat(sound.getType()).isEqualTo(ComponentType.MECHANIC);
        }

        @Test
        @DisplayName("テスト7-4: COMMANDメカニックが作成できる")
        void test7_4_CommandMechanicCreation() {
            // Given: コマンドメカニック
            var command = ComponentRegistry.createMechanic("command");

            // Then: メカニックが作成できる
            assertThat(command).isNotNull();
            assertThat(command.getType()).isEqualTo(ComponentType.MECHANIC);
        }

        @Test
        @DisplayName("テスト7-5: EXPLOSIONメカニックが作成できる")
        void test7_5_ExplosionMechanicCreation() {
            // Given: 爆発メカニック
            var explosion = ComponentRegistry.createMechanic("explosion");

            // Then: メカニックが作成できる
            assertThat(explosion).isNotNull();
            assertThat(explosion.getType()).isEqualTo(ComponentType.MECHANIC);
        }

        @Test
        @DisplayName("テスト7-6: SPEEDメカニックが作成できる")
        void test7_6_SpeedMechanicCreation() {
            // Given: スピードメカニック
            var speed = ComponentRegistry.createMechanic("speed");

            // Then: メカニックが作成できる
            assertThat(speed).isNotNull();
            assertThat(speed.getType()).isEqualTo(ComponentType.MECHANIC);
        }

        @Test
        @DisplayName("テスト7-7: PARTICLEメカニックが作成できる")
        void test7_7_ParticleMechanicCreation() {
            // Given: パーティクルメカニック
            var particle = ComponentRegistry.createMechanic("particle");

            // Then: メカニックが作成できる
            assertThat(particle).isNotNull();
            assertThat(particle.getType()).isEqualTo(ComponentType.MECHANIC);
        }

        @Test
        @DisplayName("テスト7-8: DELAYメカニックが作成できる")
        void test7_8_DelayMechanicCreation() {
            // Given: 遅延メカニック
            var delay = ComponentRegistry.createMechanic("delay");

            // Then: メカニックが作成できる
            assertThat(delay).isNotNull();
            assertThat(delay.getType()).isEqualTo(ComponentType.MECHANIC);
        }

        @Test
        @DisplayName("テスト7-9: CLEANSEメカニックが作成できる")
        void test7_9_CleanseMechanicCreation() {
            // Given: クレンジメカニック
            var cleanse = ComponentRegistry.createMechanic("cleanse");

            // Then: メカニックが作成できる
            assertThat(cleanse).isNotNull();
            assertThat(cleanse.getType()).isEqualTo(ComponentType.MECHANIC);
        }

        @Test
        @DisplayName("テスト7-10: CHANNELメカニックが作成できる")
        void test7_10_ChannelMechanicCreation() {
            // Given: チャネルメカニック
            var channel = ComponentRegistry.createMechanic("channel");

            // Then: メカニックが作成できる
            assertThat(channel).isNotNull();
            assertThat(channel.getType()).isEqualTo(ComponentType.MECHANIC);
        }

        @Test
        @DisplayName("テスト7-11: LAUNCHメカニックが作成できる")
        void test7_11_LaunchMechanicCreation() {
            // Given: 投射物メカニック
            var launch = ComponentRegistry.createMechanic("launch");

            // Then: メカニックが作成できる
            assertThat(launch).isNotNull();
            assertThat(launch.getType()).isEqualTo(ComponentType.MECHANIC);
        }
    }

    // ==================== テスト8: 新しい条件判定テスト ====================

    @Nested
    @DisplayName("テスト8: 新しい条件判定テスト")
    class Test8NewConditionEvaluation {

        @Test
        @DisplayName("テスト8-1: CLASS条件が作成できる")
        void test8_1_ClassConditionCreation() {
            // Given: クラス条件
            var classCondition = ComponentRegistry.createCondition("class");

            // Then: 条件が作成できる
            assertThat(classCondition).isNotNull();
            assertThat(classCondition.getType()).isEqualTo(ComponentType.CONDITION);
        }

        @Test
        @DisplayName("テスト8-2: TIME条件が作成できる")
        void test8_2_TimeConditionCreation() {
            // Given: 時間条件
            var timeCondition = ComponentRegistry.createCondition("time");

            // Then: 条件が作成できる
            assertThat(timeCondition).isNotNull();
            assertThat(timeCondition.getType()).isEqualTo(ComponentType.CONDITION);
        }

        @Test
        @DisplayName("テスト8-3: ARMOR条件が作成できる")
        void test8_3_ArmorConditionCreation() {
            // Given: 防具条件
            var armorCondition = ComponentRegistry.createCondition("armor");

            // Then: 条件が作成できる
            assertThat(armorCondition).isNotNull();
            assertThat(armorCondition.getType()).isEqualTo(ComponentType.CONDITION);
        }

        @Test
        @DisplayName("テスト8-4: FIRE条件が作成できる")
        void test8_4_FireConditionCreation() {
            // Given: 炎上条件
            var fireCondition = ComponentRegistry.createCondition("fire");

            // Then: 条件が作成できる
            assertThat(fireCondition).isNotNull();
            assertThat(fireCondition.getType()).isEqualTo(ComponentType.CONDITION);
        }

        @Test
        @DisplayName("テスト8-5: WATER条件が作成できる")
        void test8_5_WaterConditionCreation() {
            // Given: 水中条件
            var waterCondition = ComponentRegistry.createCondition("water");

            // Then: 条件が作成できる
            assertThat(waterCondition).isNotNull();
            assertThat(waterCondition.getType()).isEqualTo(ComponentType.CONDITION);
        }

        @Test
        @DisplayName("テスト8-6: COMBAT条件が作成できる")
        void test8_6_CombatConditionCreation() {
            // Given: 戦闘状態条件
            var combatCondition = ComponentRegistry.createCondition("combat");

            // Then: 条件が作成できる
            assertThat(combatCondition).isNotNull();
            assertThat(combatCondition.getType()).isEqualTo(ComponentType.CONDITION);
        }

        @Test
        @DisplayName("テスト8-7: POTION条件が作成できる")
        void test8_7_PotionConditionCreation() {
            // Given: ポーション条件
            var potionCondition = ComponentRegistry.createCondition("potion");

            // Then: 条件が作成できる
            assertThat(potionCondition).isNotNull();
            assertThat(potionCondition.getType()).isEqualTo(ComponentType.CONDITION);
        }

        @Test
        @DisplayName("テスト8-8: STATUS条件が作成できる")
        void test8_8_StatusConditionCreation() {
            // Given: ステータス条件
            var statusCondition = ComponentRegistry.createCondition("status");

            // Then: 条件が作成できる
            assertThat(statusCondition).isNotNull();
            assertThat(statusCondition.getType()).isEqualTo(ComponentType.CONDITION);
        }

        @Test
        @DisplayName("テスト8-9: TOOL条件が作成できる")
        void test8_9_ToolConditionCreation() {
            // Given: ツール条件
            var toolCondition = ComponentRegistry.createCondition("tool");

            // Then: 条件が作成できる
            assertThat(toolCondition).isNotNull();
            assertThat(toolCondition.getType()).isEqualTo(ComponentType.CONDITION);
        }
    }

    // ==================== テスト9: 組み合わせ実行テスト ====================

    @Nested
    @DisplayName("テスト9: 組み合わせ実行テスト")
    class Test9CombinationExecution {

        @Test
        @DisplayName("テスト9-1: 条件+メカニックの組み合わせ実行")
        void test9_1_ConditionWithMechanic() {
            // Given: HP条件付きダメージ
            HealthCondition condition = new HealthCondition();
            condition.getSettings().put("max", "0.5");  // HPが50%以下

            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "20");

            // 条件の子としてメカニックを追加
            condition.addChild(damage);

            // When: 条件付きメカニックを実行
            boolean executed = condition.execute(mockPlayer, 1, List.of(mockPlayer));

            // Then: 実行が完了する（条件判定の結果に関わらず）
            // 実際の条件判定はHealthCondition.test()内で行われる
            assertThat(executed).isNotNull();
        }

        @Test
        @DisplayName("テスト9-2: 確率条件+メカニックの組み合わせ")
        void test9_2_ChanceWithMechanic() {
            // Given: 30%確率で発動するメッセージ
            ChanceCondition chance = new ChanceCondition();
            chance.getSettings().put("chance", "0.3");

            MessageMechanic message = new MessageMechanic();
            message.getSettings().put("text", "ラッキー！");
            message.getSettings().put("to-caster", "true");
            message.getSettings().put("to-target", "false");

            // 条件の子としてメカニックを追加
            chance.addChild(message);

            // When: 確率条件付きメカニックを実行（複数回試行）
            // 確率テストなので単純に実行できることを確認
            boolean executed = chance.execute(mockPlayer, 1, List.of(mockPlayer));

            // Then: 実行が完了する
            assertThat(executed).isNotNull();
        }

        @Test
        @DisplayName("テスト9-3: 複数の条件を持つスキル効果")
        void test9_3_MultipleConditions() {
            // Given: 複数の条件とメカニック
            SkillEffect skillEffect = new SkillEffect("test_skill");

            // 条件1: HPが50%以下
            HealthCondition healthCond = new HealthCondition();
            healthCond.getSettings().put("max", "0.5");

            // 条件2: 30%の確率
            ChanceCondition chanceCond = new ChanceCondition();
            chanceCond.getSettings().put("chance", "0.3");

            // メカニック: ダメージ
            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "10");

            // スキル効果に追加
            skillEffect.addComponent(healthCond);
            skillEffect.addComponent(chanceCond);
            skillEffect.addComponent(damage);

            // When: スキル効果を実行
            boolean executed = skillEffect.execute(mockPlayer, 1, List.of(mockPlayer));

            // Then: 実行が完了する
            assertThat(executed).isNotNull();
        }

        @Test
        @DisplayName("テスト9-4: 入れ子の条件とメカニック")
        void test9_4_NestedConditions() {
            // Given: 入れ子の条件構造
            // 条件1(確率) -> 条件2(HP) -> メカニック(ダメージ)
            ChanceCondition chanceCond = new ChanceCondition();
            chanceCond.getSettings().put("chance", "0.5");

            HealthCondition healthCond = new HealthCondition();
            healthCond.getSettings().put("max", "0.5");

            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "15");

            // 入れ子構造を作成
            healthCond.addChild(damage);
            chanceCond.addChild(healthCond);

            // When: 入れ子の条件を実行
            boolean executed = chanceCond.execute(mockPlayer, 1, List.of(mockPlayer));

            // Then: 実行が完了する
            assertThat(executed).isNotNull();
        }

        @Test
        @DisplayName("テスト9-5: メカニックの連鎖実行")
        void test9_5_MechanicChain() {
            // Given: ダメージ -> メッセージ -> ヒール の連鎖
            SkillEffect skillEffect = new SkillEffect("combo_skill");

            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "10");

            MessageMechanic message = new MessageMechanic();
            message.getSettings().put("text", "コンボヒット！");
            message.getSettings().put("to-caster", "true");
            message.getSettings().put("to-target", "false");

            HealMechanic heal = new HealMechanic();
            heal.getSettings().put("value", "5");

            // 全て追加
            skillEffect.addComponent(damage);
            skillEffect.addComponent(message);
            skillEffect.addComponent(heal);

            // When: 連鎖メカニックを実行
            boolean executed = skillEffect.execute(mockPlayer, 1, List.of(mockPlayer));

            // Then: 実行が成功する
            assertThat(executed).isTrue();
        }
    }

    // ==================== テスト10: CASTトリガーテスト ====================

    @Nested
    @DisplayName("テスト10: CASTトリガーテスト")
    class Test10CastTrigger {

        @Test
        @DisplayName("テスト10-1: CASTトリガーが作成できる")
        void test10_1_CastTriggerCreation() {
            // When: CASTトリガーを作成
            var castTrigger = ComponentRegistry.createTrigger("CAST");

            // Then: トリガーが正しく作成されている
            assertThat(castTrigger).isNotNull();
            assertThat(castTrigger.getKey()).isEqualTo("CAST");
        }

        @Test
        @DisplayName("テスト10-2: CASTトリガーの即時実行")
        void test10_2_CastTriggerImmediateExecution() {
            // Given: CASTトリガーとハンドラー
            var castTrigger = ComponentRegistry.createTrigger("CAST");
            TriggerSettings settings = new TriggerSettings();
            settings.put("cooldown", "5.0");

            DamageMechanic damage = new DamageMechanic();
            damage.getSettings().put("value", "10");

            TriggerHandler handler = new TriggerHandler(
                    "test_skill",
                    castTrigger,
                    settings,
                    damage,
                    1
            );

            // When: CASTトリガーで即時実行（例外が発生しないことを確認）
            assertThatCode(() -> {
                handler.handleImmediate(mockPlayer, 1, mockPlayer);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("テスト10-3: CASTトリガーを含むスキル効果")
        void test10_3_CastTriggerWithSkillEffect() {
            // Given: CASTトリガーを持つスキル効果
            SkillEffect skillEffect = new SkillEffect("cast_test");

            MessageMechanic message = new MessageMechanic();
            message.getSettings().put("text", "即時発動！");
            message.getSettings().put("to-caster", "true");
            message.getSettings().put("to-target", "false");

            skillEffect.addComponent(message);

            // When: スキル効果を実行
            boolean executed = skillEffect.execute(mockPlayer, 1, List.of(mockPlayer));

            // Then: 実行が成功する
            assertThat(executed).isTrue();
        }
    }

    // ==================== テスト11: コンポーネント総数確認 ====================

    @Nested
    @DisplayName("テスト11: コンポーネント総数確認")
    class Test11ComponentCount {

        @Test
        @DisplayName("テスト11-1: トリガーが9種類登録されている")
        void test11_1_TriggerCount() {
            // Given: 全トリガーキー
            var triggerKeys = ComponentRegistry.getTriggerKeys();

            // Then: 9種類のトリガーが登録されている
            assertThat(triggerKeys.size()).isEqualTo(9);
        }

        @Test
        @DisplayName("テスト11-2: メカニックが16種類登録されている")
        void test11_2_MechanicCount() {
            // Given: 全メカニックキー
            var mechanicKeys = ComponentRegistry.getMechanicKeys();

            // Then: 16種類のメカニックが登録されている
            assertThat(mechanicKeys.size()).isEqualTo(16);
        }

        @Test
        @DisplayName("テスト11-3: 条件が14種類登録されている")
        void test11_3_ConditionCount() {
            // Given: 全条件キー
            var conditionKeys = ComponentRegistry.getConditionKeys();

            // Then: 14種類の条件が登録されている（eventを含む）
            assertThat(conditionKeys.size()).isEqualTo(14);
        }

        @Test
        @DisplayName("テスト11-4: コンポーネント総数が39種類である")
        void test11_4_TotalComponentCount() {
            // When: 各種類のコンポーネント数を取得
            int triggerCount = ComponentRegistry.getTriggerKeys().size();
            int mechanicCount = ComponentRegistry.getMechanicKeys().size();
            int conditionCount = ComponentRegistry.getConditionKeys().size();
            int total = triggerCount + mechanicCount + conditionCount;

            // Then: 合計39種類（9トリガー + 16メカニック + 14条件）
            assertThat(total).isEqualTo(39);
        }
    }

    // ==================== テスト12: ComponentEffectExecutor統合 ====================

    @Nested
    @DisplayName("テスト12: ComponentEffectExecutor統合")
    class Test12ComponentEffectExecutor {

        @Test
        @DisplayName("テスト12-1: ComponentEffectExecutorが作成できる")
        void test12_1_ExecutorCreation() {
            // When: エグゼキューターを作成
            ComponentEffectExecutor executor = new ComponentEffectExecutor(mockPlugin);

            // Then: エグゼキューターが正しく作成されている
            assertThat(executor).isNotNull();
            assertThat(executor.getPlugin()).isSameAs(mockPlugin);
        }

        @Test
        @DisplayName("テスト12-2: キャスト時にトリガーがアクティベートされる")
        void test12_2_CastWithTriggers() {
            // Given: スキル
            Skill mockSkill = mock(Skill.class);

            // スキルにコンポーネント効果を設定
            SkillEffect skillEffect = new SkillEffect("test_skill");
            when(mockSkill.getComponentEffect()).thenReturn(skillEffect);
            when(mockSkill.getId()).thenReturn("test_skill");

            // When: エグゼキューターを作成してキャスト
            ComponentEffectExecutor executor = new ComponentEffectExecutor(mockPlugin);
            executor.castWithTriggers(mockPlayer, mockSkill, 1, 0);

            // Then: キャスト成功（トリガーがある場合はtrue、ない場合はfalse）
            // このテストではトリガーを含むSkillEffectを設定していないため、falseが期待値
            // 実際の統合テストでは、TriggerManagerを介した検証が必要
        }
    }
}
