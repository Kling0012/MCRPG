package com.example.rpgplugin.skill.component.trigger;

import com.example.rpgplugin.skill.component.EffectComponent;
import com.example.rpgplugin.skill.component.condition.HealthCondition;
import com.example.rpgplugin.skill.component.cost.ManaCostComponent;
import com.example.rpgplugin.skill.component.cooldown.BasicCooldownComponent;
import com.example.rpgplugin.skill.component.filter.EntityTypeFilter;
import com.example.rpgplugin.skill.component.mechanic.DamageMechanic;
import com.example.rpgplugin.skill.component.target.SelfTargetComponent;
import org.bukkit.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ComponentRegistryのテストクラス
 */
class ComponentRegistryTest {

    // ========== createCondition() テスト ==========

    @Nested
    @DisplayName("createCondition: 条件コンポーネント作成")
    class CreateConditionTests {

        @Test
        @DisplayName("test: 登録済みの条件を作成")
        void testCreateCondition_Registered() {
            EffectComponent component = ComponentRegistry.createCondition("health");

            assertThat(component).isNotNull();
        }

        @Test
        @DisplayName("test: 大文字小文字を区別しない")
        void testCreateCondition_CaseInsensitive() {
            EffectComponent component1 = ComponentRegistry.createCondition("HEALTH");
            EffectComponent component2 = ComponentRegistry.createCondition("Health");
            EffectComponent component3 = ComponentRegistry.createCondition("health");

            assertThat(component1).isNotNull();
            assertThat(component2).isNotNull();
            assertThat(component3).isNotNull();
        }

        @Test
        @DisplayName("test: 未登録の条件ではnull")
        void testCreateCondition_NotRegistered() {
            EffectComponent component = ComponentRegistry.createCondition("nonexistent");

            assertThat(component).isNull();
        }

        @Test
        @DisplayName("test: 全条件タイプを確認")
        void testCreateCondition_AllTypes() {
            String[] conditions = {"health", "chance", "mana", "biome", "class", "time",
                                   "armor", "fire", "water", "combat", "potion", "status", "tool", "event"};

            for (String condition : conditions) {
                assertThat(ComponentRegistry.createCondition(condition))
                    .as("Condition '%s' should be registered", condition)
                    .isNotNull();
            }
        }
    }

    // ========== createMechanic() テスト ==========

    @Nested
    @DisplayName("createMechanic: メカニックコンポーネント作成")
    class CreateMechanicTests {

        @Test
        @DisplayName("test: 登録済みのメカニックを作成")
        void testCreateMechanic_Registered() {
            EffectComponent component = ComponentRegistry.createMechanic("damage");

            assertThat(component).isNotNull();
        }

        @Test
        @DisplayName("test: 大文字小文字を区別しない")
        void testCreateMechanic_CaseInsensitive() {
            EffectComponent component1 = ComponentRegistry.createMechanic("DAMAGE");
            EffectComponent component2 = ComponentRegistry.createMechanic("Damage");

            assertThat(component1).isNotNull();
            assertThat(component2).isNotNull();
        }

        @Test
        @DisplayName("test: 未登録のメカニックではnull")
        void testCreateMechanic_NotRegistered() {
            EffectComponent component = ComponentRegistry.createMechanic("nonexistent");

            assertThat(component).isNull();
        }

        @Test
        @DisplayName("test: 全メカニックタイプを確認")
        void testCreateMechanic_AllTypes() {
            String[] mechanics = {"damage", "heal", "push", "fire", "message", "potion",
                                  "lightning", "sound", "command", "explosion", "speed",
                                  "particle", "launch", "delay", "cleanse", "channel"};

            for (String mechanic : mechanics) {
                assertThat(ComponentRegistry.createMechanic(mechanic))
                    .as("Mechanic '%s' should be registered", mechanic)
                    .isNotNull();
            }
        }
    }

    // ========== createTrigger() テスト ==========

    @Nested
    @DisplayName("createTrigger: トリガー作成")
    class CreateTriggerTests {

        @Test
        @DisplayName("test: 登録済みのトリガーを作成（簡易版）")
        void testCreateSimpleTrigger_Registered() {
            Trigger<?> trigger = ComponentRegistry.createTrigger("CAST");

            assertThat(trigger).isNotNull();
        }

        @Test
        @DisplayName("test: 大文字小文字を区別しない（簡易版）")
        void testCreateSimpleTrigger_CaseInsensitive() {
            Trigger<?> trigger1 = ComponentRegistry.createTrigger("CAST");
            Trigger<?> trigger2 = ComponentRegistry.createTrigger("cast");

            // ComponentRegistryは大文字変換を行うためcase-insensitive
            assertThat(trigger1).isNotNull();
            assertThat(trigger2).isNotNull(); // 同じトリガーが返される
        }

        @Test
        @DisplayName("test: 未登録のトリガーではnull")
        void testCreateSimpleTrigger_NotRegistered() {
            Trigger<?> trigger = ComponentRegistry.createTrigger("nonexistent");

            assertThat(trigger).isNull();
        }

        @Test
        @DisplayName("test: 全トリガータイプを確認")
        void testCreateTrigger_AllTypes() {
            String[] triggers = {"CAST", "CROUCH", "LAND", "DEATH", "KILL",
                                 "PHYSICAL_DEALT", "PHYSICAL_TAKEN", "LAUNCH", "ENVIRONMENTAL"};

            for (String trigger : triggers) {
                assertThat(ComponentRegistry.createTrigger(trigger))
                    .as("Trigger '%s' should be registered", trigger)
                    .isNotNull();
            }
        }

        @Test
        @DisplayName("test: 型指定でトリガーを作成")
        void testCreateTrigger_WithEventClass() {
            // CAST trigger should work with Event class
            Trigger<Event> trigger = ComponentRegistry.createTrigger("CAST", Event.class);

            assertThat(trigger).isNotNull();
        }

        @Test
        @DisplayName("test: 型指定で未登録のトリガーはnull")
        void testCreateTrigger_WithEventClass_NotRegistered() {
            Trigger<Event> trigger = ComponentRegistry.createTrigger("nonexistent", Event.class);

            assertThat(trigger).isNull();
        }
    }

    // ========== createTarget() テスト ==========

    @Nested
    @DisplayName("createTarget: ターゲットコンポーネント作成")
    class CreateTargetTests {

        @Test
        @DisplayName("test: 登録済みのターゲットを作成")
        void testCreateTarget_Registered() {
            EffectComponent component = ComponentRegistry.createTarget("SELF");

            assertThat(component).isNotNull();
        }

        @Test
        @DisplayName("test: 大文字小文字を区別しない")
        void testCreateTarget_CaseInsensitive() {
            EffectComponent component1 = ComponentRegistry.createTarget("SELF");
            EffectComponent component2 = ComponentRegistry.createTarget("self");

            // ComponentRegistryは大文字変換を行うためcase-insensitive
            assertThat(component1).isNotNull();
            assertThat(component2).isNotNull(); // 同じコンポーネントが返される
        }

        @Test
        @DisplayName("test: 未登録のターゲットではnull")
        void testCreateTarget_NotRegistered() {
            EffectComponent component = ComponentRegistry.createTarget("nonexistent");

            assertThat(component).isNull();
        }

        @Test
        @DisplayName("test: 全ターゲットタイプを確認")
        void testCreateTarget_AllTypes() {
            String[] targets = {"SELF", "SINGLE", "CONE", "SPHERE", "SECTOR",
                                "AREA", "LINE", "NEAREST_HOSTILE"};

            for (String target : targets) {
                assertThat(ComponentRegistry.createTarget(target))
                    .as("Target '%s' should be registered", target)
                    .isNotNull();
            }
        }
    }

    // ========== createCost() テスト ==========

    @Nested
    @DisplayName("createCost: コストコンポーネント作成")
    class CreateCostTests {

        @Test
        @DisplayName("test: 登録済みのコストを作成")
        void testCreateCost_Registered() {
            EffectComponent component = ComponentRegistry.createCost("MANA");

            assertThat(component).isNotNull();
        }

        @Test
        @DisplayName("test: 大文字小文字を区別しない")
        void testCreateCost_CaseInsensitive() {
            EffectComponent component1 = ComponentRegistry.createCost("MANA");
            EffectComponent component2 = ComponentRegistry.createCost("mana");

            // ComponentRegistryは大文字変換を行うためcase-insensitive
            assertThat(component1).isNotNull();
            assertThat(component2).isNotNull(); // 同じコンポーネントが返される
        }

        @Test
        @DisplayName("test: 全コストタイプを確認")
        void testCreateCost_AllTypes() {
            String[] costs = {"MANA", "HP", "STAMINA", "ITEM"};

            for (String cost : costs) {
                assertThat(ComponentRegistry.createCost(cost))
                    .as("Cost '%s' should be registered", cost)
                    .isNotNull();
            }
        }
    }

    // ========== createCooldown() テスト ==========

    @Nested
    @DisplayName("createCooldown: クールダウンコンポーネント作成")
    class CreateCooldownTests {

        @Test
        @DisplayName("test: 登録済みのクールダウンを作成")
        void testCreateCooldown_Registered() {
            EffectComponent component = ComponentRegistry.createCooldown("COOLDOWN");

            assertThat(component).isNotNull();
        }

        @Test
        @DisplayName("test: 大文字小文字を区別しない")
        void testCreateCooldown_CaseInsensitive() {
            EffectComponent component1 = ComponentRegistry.createCooldown("COOLDOWN");
            EffectComponent component2 = ComponentRegistry.createCooldown("cooldown");

            // ComponentRegistryは大文字変換を行うためcase-insensitive
            assertThat(component1).isNotNull();
            assertThat(component2).isNotNull(); // 同じコンポーネントが返される
        }
    }

    // ========== createFilter() テスト ==========

    @Nested
    @DisplayName("createFilter: フィルターコンポーネント作成")
    class CreateFilterTests {

        @Test
        @DisplayName("test: 登録済みのフィルターを作成")
        void testCreateFilter_Registered() {
            EffectComponent component = ComponentRegistry.createFilter("entity_type");

            assertThat(component).isNotNull();
        }

        @Test
        @DisplayName("test: 大文字小文字を区別しない")
        void testCreateFilter_CaseInsensitive() {
            EffectComponent component1 = ComponentRegistry.createFilter("ENTITY_TYPE");
            EffectComponent component2 = ComponentRegistry.createFilter("entity_type");

            // ComponentRegistryは大文字変換を行うためcase-insensitive
            assertThat(component1).isNotNull();
            assertThat(component2).isNotNull(); // 同じコンポーネントが返される
            assertThat(component1.getClass()).isEqualTo(component2.getClass());
        }

        @Test
        @DisplayName("test: 未登録のフィルターではnull")
        void testCreateFilter_NotRegistered() {
            EffectComponent component = ComponentRegistry.createFilter("nonexistent");

            assertThat(component).isNull();
        }

        @Test
        @DisplayName("test: 全フィルタータイプを確認")
        void testCreateFilter_AllTypes() {
            String[] filters = {"ENTITY_TYPE", "GROUP"};

            for (String filter : filters) {
                assertThat(ComponentRegistry.createFilter(filter))
                    .as("Filter '%s' should be registered", filter)
                    .isNotNull();
            }
        }
    }

    // ========== hasComponent() テスト ==========

    @Nested
    @DisplayName("hasComponent: コンポーネント存在確認")
    class HasComponentTests {

        @Test
        @DisplayName("test: 登録済みの条件を確認")
        void testHasComponent_Condition() {
            assertThat(ComponentRegistry.hasComponent("health")).isTrue();
        }

        @Test
        @DisplayName("test: 登録済みのメカニックを確認")
        void testHasComponent_Mechanic() {
            assertThat(ComponentRegistry.hasComponent("damage")).isTrue();
        }

        @Test
        @DisplayName("test: 登録済みのターゲットを確認")
        void testHasComponent_Target() {
            assertThat(ComponentRegistry.hasComponent("SELF")).isTrue();
        }

        @Test
        @DisplayName("test: 登録済みのコストを確認")
        void testHasComponent_Cost() {
            assertThat(ComponentRegistry.hasComponent("MANA")).isTrue();
        }

        @Test
        @DisplayName("test: 登録済みのクールダウンを確認")
        void testHasComponent_Cooldown() {
            assertThat(ComponentRegistry.hasComponent("COOLDOWN")).isTrue();
        }

        @Test
        @DisplayName("test: 未登録のコンポーネント")
        void testHasComponent_NotRegistered() {
            assertThat(ComponentRegistry.hasComponent("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("test: フィルターも確認できる（条件として）")
        void testHasComponent_Filter() {
            // フィルターは条件として登録されている
            assertThat(ComponentRegistry.hasComponent("entity_type")).isTrue();
        }
    }

    // ========== hasCondition/hasMechanic/hasTrigger/hasTarget/hasCost/hasCooldown/hasFilter テスト ==========

    @Nested
    @DisplayName("hasXxx: 個別存在確認メソッド")
    class HasMethodTests {

        @Test
        @DisplayName("test: hasCondition")
        void testHasCondition() {
            assertThat(ComponentRegistry.hasCondition("health")).isTrue();
            assertThat(ComponentRegistry.hasCondition("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("test: hasMechanic")
        void testHasMechanic() {
            assertThat(ComponentRegistry.hasMechanic("damage")).isTrue();
            assertThat(ComponentRegistry.hasMechanic("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("test: hasTrigger")
        void testHasTrigger() {
            // ComponentRegistryは大文字変換を行うためcase-insensitive
            assertThat(ComponentRegistry.hasTrigger("CAST")).isTrue();
            assertThat(ComponentRegistry.hasTrigger("cast")).isTrue(); // case-insensitive
            assertThat(ComponentRegistry.hasTrigger("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("test: hasTarget")
        void testHasTarget() {
            // ComponentRegistryは大文字変換を行うためcase-insensitive
            assertThat(ComponentRegistry.hasTarget("SELF")).isTrue();
            assertThat(ComponentRegistry.hasTarget("self")).isTrue(); // case-insensitive
            assertThat(ComponentRegistry.hasTarget("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("test: hasCost")
        void testHasCost() {
            assertThat(ComponentRegistry.hasCost("MANA")).isTrue();
            assertThat(ComponentRegistry.hasCost("mana")).isFalse(); // case-sensitive
            assertThat(ComponentRegistry.hasCost("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("test: hasCooldown")
        void testHasCooldown() {
            assertThat(ComponentRegistry.hasCooldown("COOLDOWN")).isTrue();
            assertThat(ComponentRegistry.hasCooldown("cooldown")).isFalse(); // case-sensitive
            assertThat(ComponentRegistry.hasCooldown("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("test: hasFilter")
        void testHasFilter() {
            // ComponentRegistryは大文字変換を行うためcase-insensitive
            assertThat(ComponentRegistry.hasFilter("ENTITY_TYPE")).isTrue();
            assertThat(ComponentRegistry.hasFilter("entity_type")).isTrue(); // case-insensitive
            assertThat(ComponentRegistry.hasFilter("nonexistent")).isFalse();
        }
    }

    // ========== getKeys() テスト ==========

    @Nested
    @DisplayName("getKeys: キーセット取得")
    class GetKeysTests {

        @Test
        @DisplayName("test: getConditionKeys")
        void testGetConditionKeys() {
            assertThat(ComponentRegistry.getConditionKeys()).isNotEmpty();
            assertThat(ComponentRegistry.getConditionKeys()).contains("health", "chance", "mana");
        }

        @Test
        @DisplayName("test: getMechanicKeys")
        void testGetMechanicKeys() {
            assertThat(ComponentRegistry.getMechanicKeys()).isNotEmpty();
            assertThat(ComponentRegistry.getMechanicKeys()).contains("damage", "heal", "push");
        }

        @Test
        @DisplayName("test: getTriggerKeys")
        void testGetTriggerKeys() {
            assertThat(ComponentRegistry.getTriggerKeys()).isNotEmpty();
            assertThat(ComponentRegistry.getTriggerKeys()).contains("CAST", "CROUCH", "LAND");
        }

        @Test
        @DisplayName("test: getTargetKeys")
        void testGetTargetKeys() {
            assertThat(ComponentRegistry.getTargetKeys()).isNotEmpty();
            assertThat(ComponentRegistry.getTargetKeys()).contains("SELF", "SINGLE", "CONE");
        }

        @Test
        @DisplayName("test: getCostKeys")
        void testGetCostKeys() {
            assertThat(ComponentRegistry.getCostKeys()).isNotEmpty();
            assertThat(ComponentRegistry.getCostKeys()).contains("MANA", "HP", "STAMINA", "ITEM");
        }

        @Test
        @DisplayName("test: getCooldownKeys")
        void testGetCooldownKeys() {
            assertThat(ComponentRegistry.getCooldownKeys()).isNotEmpty();
            assertThat(ComponentRegistry.getCooldownKeys()).contains("COOLDOWN");
        }

        @Test
        @DisplayName("test: getFilterKeys")
        void testGetFilterKeys() {
            assertThat(ComponentRegistry.getFilterKeys()).isNotEmpty();
            assertThat(ComponentRegistry.getFilterKeys()).contains("ENTITY_TYPE", "GROUP");
        }
    }

    // ========== createComponent() テスト ==========

    @Nested
    @DisplayName("createComponent: 総合コンポーネント作成")
    class CreateComponentTests {

        @Test
        @DisplayName("test: 条件から作成")
        void testCreateComponent_FromCondition() {
            EffectComponent component = ComponentRegistry.createComponent("health");

            assertThat(component).isNotNull();
        }

        @Test
        @DisplayName("test: メカニックから作成")
        void testCreateComponent_FromMechanic() {
            EffectComponent component = ComponentRegistry.createComponent("damage");

            assertThat(component).isNotNull();
        }

        @Test
        @DisplayName("test: フィルターから作成")
        void testCreateComponent_FromFilter() {
            EffectComponent component = ComponentRegistry.createComponent("entity_type");

            assertThat(component).isNotNull();
        }

        @Test
        @DisplayName("test: ターゲットから作成")
        void testCreateComponent_FromTarget() {
            EffectComponent component = ComponentRegistry.createComponent("SELF");

            assertThat(component).isNotNull();
        }

        @Test
        @DisplayName("test: コストから作成")
        void testCreateComponent_FromCost() {
            EffectComponent component = ComponentRegistry.createComponent("MANA");

            assertThat(component).isNotNull();
        }

        @Test
        @DisplayName("test: クールダウンから作成")
        void testCreateComponent_FromCooldown() {
            EffectComponent component = ComponentRegistry.createComponent("COOLDOWN");

            assertThat(component).isNotNull();
        }

        @Test
        @DisplayName("test: 未登録のコンポーネントではnull")
        void testCreateComponent_NotRegistered() {
            EffectComponent component = ComponentRegistry.createComponent("nonexistent");

            assertThat(component).isNull();
        }
    }

    // ========== registerXxx() テスト ==========

    @Nested
    @DisplayName("register: 動的登録")
    class RegisterTests {

        @Test
        @DisplayName("test: 新しい条件を登録して作成")
        void testRegisterCondition() {
            // 未登録
            assertThat(ComponentRegistry.hasCondition("custom_condition")).isFalse();

            // 登録
            ComponentRegistry.registerCondition("custom_condition", HealthCondition::new);

            // 登録済み
            assertThat(ComponentRegistry.hasCondition("custom_condition")).isTrue();
            assertThat(ComponentRegistry.createCondition("custom_condition")).isNotNull();
        }

        @Test
        @DisplayName("test: 新しいメカニックを登録して作成")
        void testRegisterMechanic() {
            assertThat(ComponentRegistry.hasMechanic("custom_mechanic")).isFalse();

            ComponentRegistry.registerMechanic("custom_mechanic", DamageMechanic::new);

            assertThat(ComponentRegistry.hasMechanic("custom_mechanic")).isTrue();
            assertThat(ComponentRegistry.createMechanic("custom_mechanic")).isNotNull();
        }

        @Test
        @DisplayName("test: 新しいトリガーを登録して作成")
        void testRegisterTrigger() {
            assertThat(ComponentRegistry.hasTrigger("CUSTOM_TRIGGER")).isFalse();

            ComponentRegistry.registerTrigger("CUSTOM_TRIGGER", CastTrigger::new);

            assertThat(ComponentRegistry.hasTrigger("CUSTOM_TRIGGER")).isTrue();
            assertThat(ComponentRegistry.createTrigger("CUSTOM_TRIGGER")).isNotNull();
        }

        @Test
        @DisplayName("test: 新しいターゲットを登録して作成")
        void testRegisterTarget() {
            assertThat(ComponentRegistry.hasTarget("CUSTOM_TARGET")).isFalse();

            ComponentRegistry.registerTarget("CUSTOM_TARGET", SelfTargetComponent::new);

            assertThat(ComponentRegistry.hasTarget("CUSTOM_TARGET")).isTrue();
            assertThat(ComponentRegistry.createTarget("CUSTOM_TARGET")).isNotNull();
        }

        @Test
        @DisplayName("test: 新しいコストを登録して作成")
        void testRegisterCost() {
            assertThat(ComponentRegistry.hasCost("CUSTOM_COST")).isFalse();

            ComponentRegistry.registerCost("CUSTOM_COST", ManaCostComponent::new);

            assertThat(ComponentRegistry.hasCost("CUSTOM_COST")).isTrue();
            assertThat(ComponentRegistry.createCost("CUSTOM_COST")).isNotNull();
        }

        @Test
        @DisplayName("test: 新しいクールダウンを登録して作成")
        void testRegisterCooldown() {
            assertThat(ComponentRegistry.hasCooldown("CUSTOM_CD")).isFalse();

            ComponentRegistry.registerCooldown("CUSTOM_CD", BasicCooldownComponent::new);

            assertThat(ComponentRegistry.hasCooldown("CUSTOM_CD")).isTrue();
            assertThat(ComponentRegistry.createCooldown("CUSTOM_CD")).isNotNull();
        }

        @Test
        @DisplayName("test: 新しいフィルターを登録して作成")
        void testRegisterFilter() {
            assertThat(ComponentRegistry.hasFilter("CUSTOM_FILTER")).isFalse();

            ComponentRegistry.registerFilter("CUSTOM_FILTER", EntityTypeFilter::new);

            assertThat(ComponentRegistry.hasFilter("CUSTOM_FILTER")).isTrue();
            assertThat(ComponentRegistry.createFilter("CUSTOM_FILTER")).isNotNull();
        }
    }
}
