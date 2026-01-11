package com.example.rpgplugin.skill.component.condition;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.entity.LivingEntity;

/**
 * イベント条件コンポーネント
 * <p>特定のイベントが発生した時のみ子コンポーネントを実行します</p>
 *
 * <p>設計変更: CAST以外のトリガーはこのイベント条件として扱います</p>
 *
 * <p>YAML設定例:</p>
 * <pre>
 * - type: condition
 *   component_id: EVENT
 *   settings:
 *     event: PHYSICAL_DEALT
 *     duration: 10  # 登録後10秒間有効
 * </pre>
 *
 * <p>サポートするイベント:</p>
 * <ul>
 *   <li>PHYSICAL_DEALT - 物理ダメージを与えた時</li>
 *   <li>PHYSICAL_TAKEN - 物理ダメージを受けた時</li>
 *   <li>CROUCH - スニーク時</li>
 *   <li>LAND - 着地時（落下ダメージ）</li>
 *   <li>DEATH - 死亡時</li>
 *   <li>KILL - エンティティを倒した時</li>
 *   <li>LAUNCH - 投射物発射時</li>
 *   <li>ENVIRONMENTAL - 環境ダメージ時</li>
 * </ul>
 *
 * <p>注意: この条件はTriggerHandlerによってイベント駆動で評価されます。
 * 通常のtest()メソッドは常にfalseを返します。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class EventCondition extends ConditionComponent {

    // サポートするイベントタイプ
    public enum EventType {
        PHYSICAL_DEALT("PhysicalDealtTrigger"),
        PHYSICAL_TAKEN("PhysicalTakenTrigger"),
        CROUCH("CrouchTrigger"),
        LAND("LandTrigger"),
        DEATH("DeathTrigger"),
        KILL("KillTrigger"),
        LAUNCH("LaunchTrigger"),
        ENVIRONMENTAL("EnvironmentalTrigger");

        private final String triggerClass;

        EventType(String triggerClass) {
            this.triggerClass = triggerClass;
        }

        public String getTriggerClass() {
            return "com.example.rpgplugin.skill.component.trigger." + triggerClass;
        }
    }

    /**
     * イベントタイプ
     */
    private EventType eventType;

    /**
     * 有効期間（秒）、0以下で永続
     */
    private int duration;

    public EventCondition() {
        super("event");
    }

    @Override
    protected boolean test(LivingEntity caster, int level, LivingEntity target) {
        // イベント条件の評価は、TriggerHandler 側で行われます
        // ここでは通常の条件評価としては常にfalseを返し、
        // イベント駆動でのみ子コンポーネントを実行します
        return false;
    }

    /**
     * イベントタイプを設定します
     *
     * @param eventType イベントタイプ
     */
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    /**
     * イベントタイプを取得します
     *
     * @return イベントタイプ
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     * 有効期間を設定します
     *
     * @param duration 有効期間（秒）、0以下で永続
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * 有効期間を取得します
     *
     * @return 有効期間（秒）
     */
    public int getDuration() {
        return duration;
    }

    /**
     * 設定からイベントタイプを読み込みます
     */
    public void loadSettings() {
        String eventStr = getString("event", "");
        if (!eventStr.isEmpty()) {
            this.eventType = parseEventType(eventStr);
        }
        this.duration = getInt("duration", 0);
    }

    /**
     * イベントタイプを解析します
     *
     * @param eventStr イベント文字列
     * @return イベントタイプ、見つからない場合はnull
     */
    public static EventType parseEventType(String eventStr) {
        if (eventStr == null) {
            return null;
        }
        try {
            return EventType.valueOf(eventStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 設定の妥当性を検証します
     *
     * @param settings 設定
     * @throws IllegalArgumentException 設定が無効な場合
     */
    public static void validateConfig(ComponentSettings settings) {
        String eventStr = settings.getString("event", "");
        if (eventStr.isEmpty()) {
            throw new IllegalArgumentException("event パラメータが必須です");
        }

        EventType eventType = parseEventType(eventStr);
        if (eventType == null) {
            throw new IllegalArgumentException("不明なイベントタイプです: " + eventStr);
        }
    }

    @Override
    public String toString() {
        return "EventCondition{" +
                "eventType=" + eventType +
                ", duration=" + duration +
                ", children=" + getChildren().size() +
                '}';
    }
}
