package com.example.rpgplugin.skill.component.condition;

import com.example.rpgplugin.skill.component.ComponentType;
import com.example.rpgplugin.skill.component.EffectComponent;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.stream.Collectors;

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
        // 条件を満たすターゲットのみをフィルタリング
        List<LivingEntity> filtered = targets.stream()
                .filter(t -> test(caster, level, t))
                .collect(Collectors.toList());

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
