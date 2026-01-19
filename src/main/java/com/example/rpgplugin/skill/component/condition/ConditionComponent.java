package com.example.rpgplugin.skill.component.condition;

import com.example.rpgplugin.skill.component.ComponentType;
import com.example.rpgplugin.skill.component.EffectComponent;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 条件コンポーネントの基底クラス
 * <p>ターゲットを条件でフィルタリングします</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public abstract class ConditionComponent extends EffectComponent {

    /**
     * コンストラクタ
     *
     * @param key コンポーネントキー
     */
    protected ConditionComponent(String key) {
        super(key);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.CONDITION;
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        // 条件を満たすターゲットのみをフィルタリング（Streamよりループの方が高速）
        List<LivingEntity> filtered = new ArrayList<>(targets.size());
        for (LivingEntity target : targets) {
            if (test(caster, level, target)) {
                filtered.add(target);
            }
        }

        // 通過したターゲットがいれば、子コンポーネントを実行
        return !filtered.isEmpty() && executeChildren(caster, level, filtered);
    }

    /**
     * ターゲットが条件を満たすかテストします
     *
     * @param caster 発動者
     * @param level  スキルレベル
     * @param target テスト対象
     * @return 条件を満たす場合はtrue
     */
    protected abstract boolean test(LivingEntity caster, int level, LivingEntity target);
}
