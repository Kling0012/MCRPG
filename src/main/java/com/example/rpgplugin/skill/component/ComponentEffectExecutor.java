package com.example.rpgplugin.skill.component;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.component.trigger.Trigger;
import com.example.rpgplugin.skill.component.trigger.TriggerHandler;
import com.example.rpgplugin.skill.component.trigger.TriggerManager;
import com.example.rpgplugin.skill.component.trigger.TriggerSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Logger;

/**
 * コンポーネント効果実行クラス
 * <p>EffectComponentのツリーを実行します</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: コンポーネント実行に専念</li>
 *   <li>Strategy: 異なるコンポーネントタイプの実行を委譲</li>
 *   <li>Composite: 子コンポーネントの再帰的実行</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ComponentEffectExecutor {

    private static final Logger LOGGER = Logger.getLogger(ComponentEffectExecutor.class.getName());

    private final RPGPlugin plugin;
    private final TriggerManager triggerManager;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public ComponentEffectExecutor(RPGPlugin plugin) {
        this.plugin = plugin;
        this.triggerManager = TriggerManager.getInstance();
    }

    /**
     * コンポーネント効果を実行します
     *
     * @param caster   発動者
     * @param skill    スキル
     * @param level    スキルレベル
     * @param targets  ターゲットリスト
     * @return 実行成功の場合はtrue
     */
    public boolean execute(LivingEntity caster, Skill skill, int level, List<LivingEntity> targets) {
        SkillEffect skillEffect = skill.getComponentEffect();
        if (skillEffect == null) {
            return false;
        }

        try {
            // SkillEffect経由でコンポーネントを実行
            return skillEffect.execute(caster, level, targets);
        } catch (Exception e) {
            LOGGER.warning("[ComponentEffectExecutor] コンポーネント実行エラー: " + e.getMessage());
            if (caster instanceof Player) {
                ((Player) caster).sendMessage(Component.text("スキル効果の実行中にエラーが発生しました", NamedTextColor.RED));
            }
            return false;
        }
    }

    /**
     * コンポーネント効果を実行します（単体ターゲット）
     *
     * @param caster  発動者
     * @param skill   スキル
     * @param level   スキルレベル
     * @param target  ターゲット
     * @return 実行成功の場合はtrue
     */
    public boolean execute(LivingEntity caster, Skill skill, int level, LivingEntity target) {
        List<LivingEntity> targets = new ArrayList<>();
        if (target != null) {
            targets.add(target);
        }
        return execute(caster, skill, level, targets);
    }

    /**
     * スキルをキャストし、トリガーをアクティブ化します
     *
     * <p>設計要件: "トリガーはcastコマンドを発動してからリスナーを始める"</p>
     * <p>効果はトリガー発火時にのみ実行されます</p>
     *
     * @param caster   発動者
     * @param skill    スキル
     * @param level    スキルレベル
     * @param duration 効果時間（秒）、0で無制限
     * @return 実行成功の場合はtrue
     */
    public boolean castWithTriggers(LivingEntity caster, Skill skill, int level, int duration) {
        // トリガーを持つコンポーネントを検索
        List<TriggerHandler> triggerHandlers = collectTriggerHandlers(skill, level);

        if (triggerHandlers.isEmpty()) {
            return false;
        }

        boolean hasCastTrigger = false;
        List<TriggerHandler> delayedHandlers = new ArrayList<>();

        // CASTトリガーは即時実行、それ以外は遅延実行
        for (TriggerHandler handler : triggerHandlers) {
            if ("CAST".equals(handler.getTrigger().getKey())) {
                // CASTトリガーは即時実行
                handler.handleImmediate(caster, level, caster);
                hasCastTrigger = true;
            } else {
                delayedHandlers.add(handler);
            }
        }

        // 他のトリガーをアクティブ化（効果はトリガー発火時に実行される）
        if (!delayedHandlers.isEmpty()) {
            triggerManager.activateSkill(skill.getId(), caster, level, duration, delayedHandlers);
        }

        return hasCastTrigger || !delayedHandlers.isEmpty();
    }

    /**
     * トリガーハンドラーを収集します
     *
     * @param skill スキル
     * @param level スキルレベル
     * @return トリガーハンドラーリスト
     */
    private List<TriggerHandler> collectTriggerHandlers(Skill skill, int level) {
        List<TriggerHandler> handlers = new ArrayList<>();
        SkillEffect skillEffect = skill.getComponentEffect();

        if (skillEffect != null) {
            for (EffectComponent component : skillEffect.getComponents()) {
                collectTriggerHandlersRecursive(component, handlers, skill, level);
            }
        }

        return handlers;
    }

    /**
     * 再帰的にトリガーハンドラーを収集します
     *
     * @param component コンポーネント
     * @param handlers  ハンドラーリスト
     * @param skill     スキル
     * @param level     スキルレベル
     */

    private void collectTriggerHandlersRecursive(EffectComponent component,
                                                 List<TriggerHandler> handlers,
                                                 Skill skill,
                                                 int level) {
        if (component == null) {
            return;
        }

        // このコンポーネントがトリガーの場合
        if (component.getType() == ComponentType.TRIGGER && component instanceof Trigger<?>) {
            Trigger<?> trigger = (Trigger<?>) component;

            // トリガー設定を作成（ComponentSettingsのデータをコピー）
            TriggerSettings settings = new TriggerSettings();
            ComponentSettings componentSettings = component.getSettings();
            if (componentSettings != null) {
                // ComponentSettingsのデータをTriggerSettingsにコピー
                for (String key : componentSettings.getKeys()) {
                    Object value = getSettingValue(componentSettings, key);
                    if (value != null) {
                        settings.put(key, value);
                    }
                }
            }

            // デフォルトの効果時間を取得（設定にdurationがある場合は使用）
            int duration = settings.getInt("duration", 0);

            // トリガーハンドラーを作成
            TriggerHandler handler = new TriggerHandler(
                    skill.getId(),
                    trigger,
                    settings,
                    component,
                    duration
            );
            handlers.add(handler);
        }

        // 子コンポーネントを再帰的に検索
        for (EffectComponent child : component.getChildren()) {
            collectTriggerHandlersRecursive(child, handlers, skill, level);
        }
    }

    /**
     * ComponentSettingsから値を取得します
     *
     * @param settings 設定
     * @param key      キー
     * @return 値
     */
    private Object getSettingValue(ComponentSettings settings, String key) {
        // 文字列、整数、小数、真偽値の順で試す
        if (settings.has(key)) {
            String strValue = settings.getString(key, null);
            if (strValue != null) {
                // 数値 possibly parse
                try {
                    if (strValue.contains(".")) {
                        return Double.parseDouble(strValue);
                    } else {
                        return Integer.parseInt(strValue);
                    }
                } catch (NumberFormatException e) {
                    // 文字列として返す
                    return strValue;
                }
            }
        }
        return null;
    }

    /**
     * RPGPluginを取得します
     *
     * @return プラグインインスタンス
     */
    public RPGPlugin getPlugin() {
        return plugin;
    }

    /**
     * TriggerManagerを取得します
     *
     * @return トリガーマネージャー
     */
    public TriggerManager getTriggerManager() {
        return triggerManager;
    }
}
