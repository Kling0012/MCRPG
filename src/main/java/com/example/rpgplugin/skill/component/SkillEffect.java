package com.example.rpgplugin.skill.component;

import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * スキル効果クラス
 * <p>コンポーネントツリーのルートとして機能します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillEffect {

    private final String skillId;
    private final List<EffectComponent> components = new ArrayList<>();
    private final Map<Integer, ActiveSkillData> activeSkills = new HashMap<>();

    /**
     * コンストラクタ
     *
     * @param skillId スキルID
     */
    public SkillEffect(String skillId) {
        this.skillId = skillId;
    }

    /**
     * スキルIDを取得します
     *
     * @return スキルID
     */
    public String getSkillId() {
        return skillId;
    }

    /**
     * コンポーネントを追加します
     *
     * @param component コンポーネント
     */
    public void addComponent(EffectComponent component) {
        if (component != null) {
            components.add(component);
            component.setSkill(this);
        }
    }

    /**
     * コンポーネントリストを取得します
     *
     * @return コンポーネントリスト
     */
    public List<EffectComponent> getComponents() {
        return components;
    }

    /**
     * スキルを実行します
     *
     * @param caster  発動者
     * @param level   スキルレベル
     * @param targets ターゲットリスト
     * @return 実行成功の場合はtrue
     */
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        boolean worked = false;
        for (EffectComponent component : components) {
            if (component.execute(caster, level, targets)) {
                worked = true;
            }
        }
        return worked;
    }

    /**
     * スキルをアクティブ化します（トリガー付きスキル用）
     *
     * @param caster 発動者
     * @param level  スキルレベル
     * @param duration 効果時間（秒）、0で無制限
     */
    public void activate(LivingEntity caster, int level, int duration) {
        int entityId = caster.getEntityId();
        activeSkills.put(entityId, new ActiveSkillData(level, duration));
    }

    /**
     * スキルを非アクティブ化します
     *
     * @param caster 発動者
     */
    public void deactivate(LivingEntity caster) {
        int entityId = caster.getEntityId();
        ActiveSkillData data = activeSkills.remove(entityId);
        if (data != null) {
            // クリーンアップ処理
            for (EffectComponent component : components) {
                component.cleanUp(caster);
            }
        }
    }

    /**
     * スキルがアクティブか確認します
     *
     * @param caster 発動者
     * @return アクティブな場合はtrue
     */
    public boolean isActive(LivingEntity caster) {
        ActiveSkillData data = activeSkills.get(caster.getEntityId());
        if (data == null) {
            return false;
        }
        // 期限切れチェック
        if (data.duration > 0 && System.currentTimeMillis() > data.expiryTime) {
            deactivate(caster);
            return false;
        }
        return true;
    }

    /**
     * アクティブスキルのレベルを取得します
     *
     * @param caster 発動者
     * @return スキルレベル、アクティブでない場合は-1
     */
    public int getActiveLevel(LivingEntity caster) {
        ActiveSkillData data = activeSkills.get(caster.getEntityId());
        if (data == null) {
            return -1;
        }
        if (data.duration > 0 && System.currentTimeMillis() > data.expiryTime) {
            deactivate(caster);
            return -1;
        }
        return data.level;
    }

    /**
     * 全てのアクティブスキルをクリアします
     */
    public void clearAllActive() {
        activeSkills.clear();
    }

    /**
     * キャストデータを取得します（コンポーネント間でデータ共有）
     *
     * @param caster 発動者
     * @return キャストデータマップ
     */
    public static Map<String, Object> getCastData(LivingEntity caster) {
        return CastDataManager.getCastData(caster);
    }

    /**
     * キャストデータをクリアします
     *
     * @param caster 発動者
     */
    public static void clearCastData(LivingEntity caster) {
        CastDataManager.clearCastData(caster);
    }

    /**
     * アクティブスキルデータ
     */
    private static class ActiveSkillData {
        final int level;
        final int duration;
        final long expiryTime;

        ActiveSkillData(int level, int duration) {
            this.level = level;
            this.duration = duration;
            this.expiryTime = duration > 0 ? System.currentTimeMillis() + duration * 1000L : Long.MAX_VALUE;
        }
    }

    /**
     * キャストデータマネージャー
     */
    private static class CastDataManager {
        private static final Map<Integer, Map<String, Object>> castDataMap = new HashMap<>();

        static Map<String, Object> getCastData(LivingEntity caster) {
            int entityId = caster.getEntityId();
            return castDataMap.computeIfAbsent(entityId, k -> new HashMap<>());
        }

        static void clearCastData(LivingEntity caster) {
            castDataMap.remove(caster.getEntityId());
        }
    }
}
