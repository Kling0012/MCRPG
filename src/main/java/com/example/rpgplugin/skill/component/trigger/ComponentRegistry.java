package com.example.rpgplugin.skill.component.trigger;

import com.example.rpgplugin.skill.component.EffectComponent;
import com.example.rpgplugin.skill.component.condition.BiomeCondition;
import com.example.rpgplugin.skill.component.condition.ChanceCondition;
import com.example.rpgplugin.skill.component.condition.EventCondition;
import com.example.rpgplugin.skill.component.condition.HealthCondition;
import com.example.rpgplugin.skill.component.condition.ManaCondition;
import com.example.rpgplugin.skill.component.filter.*;
import com.example.rpgplugin.skill.component.mechanic.*;
import com.example.rpgplugin.skill.component.condition.*;
import com.example.rpgplugin.skill.component.target.*;
import com.example.rpgplugin.skill.component.cost.*;
import com.example.rpgplugin.skill.component.cooldown.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * コンポーネントレジストリ
 * <p>全てのコンポーネントを管理します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ComponentRegistry {

    private static final Map<String, Supplier<? extends EffectComponent>> conditions = new HashMap<>();
    private static final Map<String, Supplier<? extends EffectComponent>> mechanics = new HashMap<>();
    private static final Map<String, Supplier<? extends Trigger<?>>> triggers = new HashMap<>();
    private static final Map<String, Supplier<? extends EffectComponent>> targets = new HashMap<>();
    private static final Map<String, Supplier<? extends EffectComponent>> costs = new HashMap<>();
    private static final Map<String, Supplier<? extends EffectComponent>> cooldowns = new HashMap<>();
    private static final Map<String, Supplier<? extends EffectComponent>> filters = new HashMap<>();

    static {
        // ========== 条件コンポーネント ==========
        registerCondition("health", HealthCondition::new);
        registerCondition("chance", ChanceCondition::new);
        registerCondition("mana", ManaCondition::new);
        registerCondition("biome", BiomeCondition::new);
        registerCondition("class", ClassCondition::new);
        registerCondition("time", TimeCondition::new);
        registerCondition("armor", ArmorCondition::new);
        registerCondition("fire", FireCondition::new);
        registerCondition("water", WaterCondition::new);
        registerCondition("combat", CombatCondition::new);
        registerCondition("potion", PotionCondition::new);
        registerCondition("status", StatusCondition::new);
        registerCondition("tool", ToolCondition::new);
        registerCondition("event", EventCondition::new);

        // ========== フィルターコンポーネント ==========
        registerFilter("entity_type", EntityTypeFilter::new);
        registerFilter("group", TargetGroupFilterComponent::new);

        // ========== メカニックコンポーネント ==========
        registerMechanic("damage", DamageMechanic::new);
        registerMechanic("heal", HealMechanic::new);
        registerMechanic("push", PushMechanic::new);
        registerMechanic("fire", FireMechanic::new);
        registerMechanic("message", MessageMechanic::new);
        registerMechanic("potion", PotionMechanic::new);
        registerMechanic("lightning", LightningMechanic::new);
        registerMechanic("sound", SoundMechanic::new);
        registerMechanic("command", CommandMechanic::new);
        registerMechanic("explosion", ExplosionMechanic::new);
        registerMechanic("speed", SpeedMechanic::new);
        registerMechanic("particle", ParticleMechanic::new);
        registerMechanic("launch", LaunchMechanic::new);
        registerMechanic("delay", DelayMechanic::new);
        registerMechanic("cleanse", CleanseMechanic::new);
        registerMechanic("channel", ChannelMechanic::new);

        // ========== トリガー ==========
        registerTrigger("CAST", CastTrigger::new);
        registerTrigger("CROUCH", CrouchTrigger::new);
        registerTrigger("LAND", LandTrigger::new);
        registerTrigger("DEATH", DeathTrigger::new);
        registerTrigger("KILL", KillTrigger::new);
        registerTrigger("PHYSICAL_DEALT", PhysicalDealtTrigger::new);
        registerTrigger("PHYSICAL_TAKEN", PhysicalTakenTrigger::new);
        registerTrigger("LAUNCH", LaunchTrigger::new);
        registerTrigger("ENVIRONMENTAL", EnvironmentalTrigger::new);

        // ========== ターゲットコンポーネント ==========
        registerTarget("SELF", SelfTargetComponent::new);
        registerTarget("SINGLE", SingleTargetComponent::new);
        registerTarget("CONE", ConeTargetComponent::new);
        registerTarget("SPHERE", SphereTargetComponent::new);
        registerTarget("SECTOR", SectorTargetComponent::new);
        registerTarget("AREA", AreaTargetComponent::new);
        registerTarget("LINE", LineTargetComponent::new);
        registerTarget("NEAREST_HOSTILE", NearestHostileTargetComponent::new);

        // ========== コストコンポーネント ==========
        registerCost("MANA", ManaCostComponent::new);
        registerCost("HP", HpCostComponent::new);
        registerCost("STAMINA", StaminaCostComponent::new);
        registerCost("ITEM", ItemCostComponent::new);

        // ========== クールダウンコンポーネント ==========
        registerCooldown("COOLDOWN", BasicCooldownComponent::new);
    }

    /**
     * 条件コンポーネントを登録します
     *
     * @param key  キー
     * @param supplier サプライヤー
     */
    public static void registerCondition(String key, Supplier<? extends EffectComponent> supplier) {
        conditions.put(key.toLowerCase(), supplier);
    }

    /**
     * メカニックコンポーネントを登録します
     *
     * @param key  キー
     * @param supplier サプライヤー
     */
    public static void registerMechanic(String key, Supplier<? extends EffectComponent> supplier) {
        mechanics.put(key.toLowerCase(), supplier);
    }

    /**
     * トリガーを登録します
     *
     * @param key  キー
     * @param supplier サプライヤー
     */
    public static void registerTrigger(String key, Supplier<? extends Trigger<?>> supplier) {
        triggers.put(key.toUpperCase(), supplier);
    }

    /**
     * 条件コンポーネントを作成します
     *
     * @param key キー
     * @return コンポーネント、見つからない場合はnull
     */
    public static EffectComponent createCondition(String key) {
        Supplier<? extends EffectComponent> supplier = conditions.get(key.toLowerCase());
        return supplier != null ? supplier.get() : null;
    }

    /**
     * メカニックコンポーネントを作成します
     *
     * @param key キー
     * @return コンポーネント、見つからない場合はnull
     */
    public static EffectComponent createMechanic(String key) {
        Supplier<? extends EffectComponent> supplier = mechanics.get(key.toLowerCase());
        return supplier != null ? supplier.get() : null;
    }

    /**
     * トリガーを作成します
     *
     * @param key キー
     * @return トリガー、見つからない場合はnull
     */
    @SuppressWarnings("unchecked")
    public static <E extends org.bukkit.event.Event> Trigger<E> createTrigger(String key, Class<E> eventClass) {
        Supplier<? extends Trigger<?>> supplier = triggers.get(key.toUpperCase());
        if (supplier != null) {
            Trigger<?> trigger = supplier.get();
            if (trigger != null && eventClass.isInstance(trigger)) {
                return (Trigger<E>) trigger;
            }
        }
        return null;
    }

    /**
     * トリガーを作成します（簡易版）
     *
     * @param key キー
     * @return トリガー、見つからない場合はnull
     */
    public static Trigger<?> createTrigger(String key) {
        Supplier<? extends Trigger<?>> supplier = triggers.get(key.toUpperCase());
        return supplier != null ? supplier.get() : null;
    }

    /**
     * 条件コンポーネントが存在するか確認します
     *
     * @param key キー
     * @return 存在する場合はtrue
     */
    public static boolean hasCondition(String key) {
        return conditions.containsKey(key.toLowerCase());
    }

    /**
     * メカニックコンポーネントが存在するか確認します
     *
     * @param key キー
     * @return 存在する場合はtrue
     */
    public static boolean hasMechanic(String key) {
        return mechanics.containsKey(key.toLowerCase());
    }

    /**
     * トリガーが存在するか確認します
     *
     * @param key キー
     * @return 存在する場合はtrue
     */
    public static boolean hasTrigger(String key) {
        return triggers.containsKey(key.toUpperCase());
    }

    // ========== ターゲットコンポーネントメソッド ==========

    /**
     * ターゲットコンポーネントを登録します
     *
     * @param key  キー
     * @param supplier サプライヤー
     */
    public static void registerTarget(String key, Supplier<? extends EffectComponent> supplier) {
        targets.put(key.toUpperCase(), supplier);
    }

    /**
     * ターゲットコンポーネントを作成します
     *
     * @param key キー
     * @return コンポーネント、見つからない場合はnull
     */
    public static EffectComponent createTarget(String key) {
        Supplier<? extends EffectComponent> supplier = targets.get(key.toUpperCase());
        return supplier != null ? supplier.get() : null;
    }

    /**
     * ターゲットコンポーネントが存在するか確認します
     *
     * @param key キー
     * @return 存在する場合はtrue
     */
    public static boolean hasTarget(String key) {
        return targets.containsKey(key.toUpperCase());
    }

    /**
     * すべてのターゲットキーを取得します
     *
     * @return キーセット
     */
    public static java.util.Set<String> getTargetKeys() {
        return targets.keySet();
    }

    // ========== コストコンポーネントメソッド ==========

    /**
     * コストコンポーネントを登録します
     *
     * @param key  キー
     * @param supplier サプライヤー
     */
    public static void registerCost(String key, Supplier<? extends EffectComponent> supplier) {
        costs.put(key.toUpperCase(), supplier);
    }

    /**
     * コストコンポーネントを作成します
     *
     * @param key キー
     * @return コンポーネント、見つからない場合はnull
     */
    public static EffectComponent createCost(String key) {
        Supplier<? extends EffectComponent> supplier = costs.get(key.toUpperCase());
        return supplier != null ? supplier.get() : null;
    }

    /**
     * コストコンポーネントが存在するか確認します
     *
     * @param key キー
     * @return 存在する場合はtrue
     */
    public static boolean hasCost(String key) {
        return costs.containsKey(key.toUpperCase());
    }

    /**
     * すべてのコストキーを取得します
     *
     * @return キーセット
     */
    public static java.util.Set<String> getCostKeys() {
        return costs.keySet();
    }

    // ========== クールダウンコンポーネントメソッド ==========

    /**
     * クールダウンコンポーネントを登録します
     *
     * @param key  キー
     * @param supplier サプライヤー
     */
    public static void registerCooldown(String key, Supplier<? extends EffectComponent> supplier) {
        cooldowns.put(key.toUpperCase(), supplier);
    }

    /**
     * クールダウンコンポーネントを作成します
     *
     * @param key キー
     * @return コンポーネント、見つからない場合はnull
     */
    public static EffectComponent createCooldown(String key) {
        Supplier<? extends EffectComponent> supplier = cooldowns.get(key.toUpperCase());
        return supplier != null ? supplier.get() : null;
    }

    /**
     * クールダウンコンポーネントが存在するか確認します
     *
     * @param key キー
     * @return 存在する場合はtrue
     */
    public static boolean hasCooldown(String key) {
        return cooldowns.containsKey(key.toUpperCase());
    }

    /**
     * すべてのクールダウンキーを取得します
     *
     * @return キーセット
     */
    public static java.util.Set<String> getCooldownKeys() {
        return cooldowns.keySet();
    }

    // ========== フィルターコンポーネントメソッド ==========

    /**
     * フィルターコンポーネントを登録します
     *
     * @param key  キー
     * @param supplier サプライヤー
     */
    public static void registerFilter(String key, Supplier<? extends EffectComponent> supplier) {
        filters.put(key.toUpperCase(), supplier);
    }

    /**
     * フィルターコンポーネントを作成します
     *
     * @param key キー
     * @return コンポーネント、見つからない場合はnull
     */
    public static EffectComponent createFilter(String key) {
        Supplier<? extends EffectComponent> supplier = filters.get(key.toUpperCase());
        return supplier != null ? supplier.get() : null;
    }

    /**
     * フィルターコンポーネントが存在するか確認します
     *
     * @param key キー
     * @return 存在する場合はtrue
     */
    public static boolean hasFilter(String key) {
        return filters.containsKey(key.toUpperCase());
    }

    /**
     * すべてのフィルターキーを取得します
     *
     * @return キーセット
     */
    public static java.util.Set<String> getFilterKeys() {
        return filters.keySet();
    }

    // ========== 総合メソッド ==========

    /**
     * コンポーネントを作成します（タイプ自動判定）
     *
     * @param key キー
     * @return コンポーネント、見つからない場合はnull
     */
    public static EffectComponent createComponent(String key) {
        // Condition, Mechanic, Filter, Target, Cost, Cooldown の順で検索
        EffectComponent component = createCondition(key);
        if (component != null) return component;

        component = createMechanic(key);
        if (component != null) return component;

        component = createFilter(key);
        if (component != null) return component;

        component = createTarget(key);
        if (component != null) return component;

        component = createCost(key);
        if (component != null) return component;

        return createCooldown(key);
    }

    /**
     * コンポーネントが存在するか確認します（全タイプ検索）
     *
     * @param key キー
     * @return いずれかのタイプに存在する場合はtrue
     */
    public static boolean hasComponent(String key) {
        return hasCondition(key) || hasMechanic(key) || hasTarget(key) || hasCost(key) || hasCooldown(key);
    }

    /**
     * すべての条件キーを取得します
     *
     * @return キーセット
     */
    public static java.util.Set<String> getConditionKeys() {
        return conditions.keySet();
    }

    /**
     * すべてのメカニックキーを取得します
     *
     * @return キーセット
     */
    public static java.util.Set<String> getMechanicKeys() {
        return mechanics.keySet();
    }

    /**
     * すべてのトリガーキーを取得します
     *
     * @return キーセット
     */
    public static java.util.Set<String> getTriggerKeys() {
        return triggers.keySet();
    }
}
