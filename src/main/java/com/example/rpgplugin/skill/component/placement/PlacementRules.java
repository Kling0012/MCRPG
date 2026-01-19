package com.example.rpgplugin.skill.component.placement;

import java.util.EnumSet;
import java.util.Set;

/**
 * コンポーネント配置ルール
 * <p>各コンポーネントがどこに配置できるかを定義します</p>
 *
 * <p>ルールの階層構造:</p>
 * <pre>
 * Skill (ルート)
 * └── TRIGGER (CAST) [1個のみ]
 *     ├── COST [0-1個]
 *     ├── COOLDOWN [0-1個]
 *     └── TARGET [1個以上]
 *         ├── FILTER [0-N個]
 *         ├── CONDITION [0-N個]
 *         │   └── 子コンポーネント
 *         └── MECHANIC [1個以上]
 *             ├── CONDITION [0-N個]
 *             ├── TARGET [0-N個]
 *             └── MECHANIC [0-N個]
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public final class PlacementRules {

    private PlacementRules() {
    }

    /**
     * コンポーネントが配置可能な親タイプ
     */
    private static final EnumSet<ComponentType>[] ALLOWED_PARENTS;

    /**
     * コンポーネントが持てる子タイプ
     */
    private static final EnumSet<ComponentType>[] ALLOWED_CHILDREN;

    /**
     * コンポーネントの最大配置数（ルート単位）
     */
    private static final int[] MAX_COUNT;

    static {
        @SuppressWarnings("unchecked")
        EnumSet<ComponentType>[] allowedParents = new EnumSet[ComponentType.values().length];
        @SuppressWarnings("unchecked")
        EnumSet<ComponentType>[] allowedChildren = new EnumSet[ComponentType.values().length];
        int[] maxCount = new int[ComponentType.values().length];

        // TRIGGER (CAST)
        allowedParents[ComponentType.TRIGGER.ordinal()] = EnumSet.noneOf(ComponentType.class);
        allowedChildren[ComponentType.TRIGGER.ordinal()] = EnumSet.of(
                ComponentType.COST,
                ComponentType.COOLDOWN,
                ComponentType.TARGET
        );
        maxCount[ComponentType.TRIGGER.ordinal()] = 1;

        // COST
        allowedParents[ComponentType.COST.ordinal()] = EnumSet.of(ComponentType.TRIGGER);
        allowedChildren[ComponentType.COST.ordinal()] = EnumSet.noneOf(ComponentType.class);
        maxCount[ComponentType.COST.ordinal()] = 1;

        // COOLDOWN
        allowedParents[ComponentType.COOLDOWN.ordinal()] = EnumSet.of(ComponentType.TRIGGER);
        allowedChildren[ComponentType.COOLDOWN.ordinal()] = EnumSet.noneOf(ComponentType.class);
        maxCount[ComponentType.COOLDOWN.ordinal()] = 1;

        // TARGET
        allowedParents[ComponentType.TARGET.ordinal()] = EnumSet.of(
                ComponentType.TRIGGER,
                ComponentType.CONDITION,
                ComponentType.FILTER,
                ComponentType.MECHANIC
        );
        allowedChildren[ComponentType.TARGET.ordinal()] = EnumSet.of(
                ComponentType.FILTER,
                ComponentType.CONDITION,
                ComponentType.MECHANIC,
                ComponentType.TARGET
        );
        maxCount[ComponentType.TARGET.ordinal()] = Integer.MAX_VALUE;

        // FILTER
        allowedParents[ComponentType.FILTER.ordinal()] = EnumSet.of(
                ComponentType.TARGET,
                ComponentType.CONDITION,
                ComponentType.FILTER,
                ComponentType.MECHANIC
        );
        allowedChildren[ComponentType.FILTER.ordinal()] = EnumSet.of(
                ComponentType.FILTER,
                ComponentType.CONDITION,
                ComponentType.MECHANIC,
                ComponentType.TARGET
        );
        maxCount[ComponentType.FILTER.ordinal()] = Integer.MAX_VALUE;

        // CONDITION
        allowedParents[ComponentType.CONDITION.ordinal()] = EnumSet.of(
                ComponentType.TARGET,
                ComponentType.FILTER,
                ComponentType.MECHANIC,
                ComponentType.CONDITION
        );
        allowedChildren[ComponentType.CONDITION.ordinal()] = EnumSet.of(
                ComponentType.CONDITION,
                ComponentType.MECHANIC,
                ComponentType.TARGET
        );
        maxCount[ComponentType.CONDITION.ordinal()] = Integer.MAX_VALUE;

        // MECHANIC
        allowedParents[ComponentType.MECHANIC.ordinal()] = EnumSet.of(
                ComponentType.TARGET,
                ComponentType.FILTER,
                ComponentType.CONDITION,
                ComponentType.MECHANIC
        );
        allowedChildren[ComponentType.MECHANIC.ordinal()] = EnumSet.of(
                ComponentType.MECHANIC,
                ComponentType.TARGET,
                ComponentType.CONDITION
        );
        maxCount[ComponentType.MECHANIC.ordinal()] = Integer.MAX_VALUE;

        ALLOWED_PARENTS = allowedParents;
        ALLOWED_CHILDREN = allowedChildren;
        MAX_COUNT = maxCount;
    }

    /**
     * 指定されたタイプの親になれるコンポーネントタイプを取得します
     *
     * @param type コンポーネントタイプ
     * @return 親になれるタイプのセット
     */
    public static Set<ComponentType> getAllowedParents(ComponentType type) {
        return ALLOWED_PARENTS[type.ordinal()];
    }

    /**
     * 指定されたタイプに配置できる子コンポーネントタイプを取得します
     *
     * @param type コンポーネントタイプ
     * @return 配置できる子タイプのセット
     */
    public static Set<ComponentType> getAllowedChildren(ComponentType type) {
        return ALLOWED_CHILDREN[type.ordinal()];
    }

    /**
     * 指定されたタイプの最大配置数を取得します
     *
     * @param type コンポーネントタイプ
     * @return 最大配置数
     */
    public static int getMaxCount(ComponentType type) {
        return MAX_COUNT[type.ordinal()];
    }

    /**
     * 親コンポーネントに子コンポーネントを配置できるか検証します
     *
     * @param parent 親コンポーネントタイプ
     * @param child 子コンポーネントタイプ
     * @return 配置可能な場合はtrue
     */
    public static boolean canPlace(ComponentType parent, ComponentType child) {
        return ALLOWED_CHILDREN[parent.ordinal()].contains(child);
    }

    /**
     * 指定されたコンポーネントタイプがルートに配置できるか検証します
     *
     * @param type コンポーネントタイプ
     * @return ルートに配置可能な場合はtrue
     */
    public static boolean canBeRoot(ComponentType type) {
        return type == ComponentType.TRIGGER;
    }

    /**
     * 配置ルールのサマリーを取得します
     *
     * @return サマリー文字列
     */
    public static String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Component Placement Rules ===\n");

        for (ComponentType type : ComponentType.values()) {
            sb.append("\n[").append(type.name()).append("]\n");
            sb.append("  Can be parent of: ").append(getAllowedChildren(type)).append("\n");
            sb.append("  Can be child of: ").append(getAllowedParents(type)).append("\n");
            sb.append("  Max count: ").append(getMaxCount(type)).append("\n");
        }

        return sb.toString();
    }
}
