package com.example.rpgplugin.skill.component.trigger;

import com.example.rpgplugin.skill.component.EffectComponent;
import com.example.rpgplugin.skill.component.SkillEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import java.util.List;

/**
 * トリガーハンドラー
 * <p>スキル発動後にトリガーリスナーを登録・管理します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class TriggerHandler {

    private final String skillId;
    private final Trigger<?> trigger;
    private final TriggerSettings settings;
    private final EffectComponent rootComponent;
    private final int duration;

    /**
     * コンストラクタ
     *
     * @param skillId        スキルID
     * @param trigger         トリガー
     * @param settings        トリガー設定
     * @param rootComponent   ルートコンポーネント
     * @param duration        効果時間（秒）、0で無制限
     */
    public TriggerHandler(String skillId, Trigger<?> trigger,
                          TriggerSettings settings, EffectComponent rootComponent, int duration) {
        this.skillId = skillId;
        this.trigger = trigger;
        this.settings = settings;
        this.rootComponent = rootComponent;
        this.duration = duration;
    }

    /**
     * トリガーを処理します
     *
     * @param event イベント
     * @param skill スキル効果
     * @return 処理成功の場合はtrue
     */
    @SuppressWarnings("unchecked")
    public boolean handle(Event event, SkillEffect skill) {
        Trigger<Event> t = (Trigger<Event>) trigger;
        LivingEntity caster = t.getCaster(event);

        // スキルがアクティブか確認
        if (!skill.isActive(caster)) {
            return false;
        }

        int level = skill.getActiveLevel(caster);
        if (!t.shouldTrigger(event, level, settings)) {
            return false;
        }

        LivingEntity target = t.getTarget(event, settings);

        // データを設定
        t.setValues(event, SkillEffect.getCastData(caster));

        // コンポーネントを実行
        return rootComponent.execute(caster, level, List.of(target));
    }

/**
     * CASTトリガーを即時実行します（イベント不要）
     *
     * @param caster 発動者
     * @param level  スキルレベル
     * @param target ターゲット
     * @return 処理成功の場合はtrue
     */
    public boolean handleImmediate(LivingEntity caster, int level, LivingEntity target) {
        // CASTトリガーは常に発動
        // コンポーネントを実行
        return rootComponent.execute(caster, level, target != null ? List.of(target) : List.of(caster));
    }

    /**
     * トリガーを取得します
     *
     * @return トリガー
     */
    public Trigger<?> getTrigger() {
        return trigger;
    }

    /**
     * トリガー設定を取得します
     *
     * @return 設定
     */
    public TriggerSettings getSettings() {
        return settings;
    }

    /**
     * ルートコンポーネントを取得します
     *
     * @return ルートコンポーネント
     */
    public EffectComponent getRootComponent() {
        return rootComponent;
    }

    /**
     * 効果時間を取得します
     *
     * @return 効果時間（秒）
     */
    public int getDuration() {
        return duration;
    }

    /**
     * スキルIDを取得します
     *
     * @return スキルID
     */
    public String getSkillId() {
        return skillId;
    }
}
