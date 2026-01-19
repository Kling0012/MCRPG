package com.example.rpgplugin.skill.component.mechanic;

import com.example.rpgplugin.skill.component.ComponentType;
import com.example.rpgplugin.skill.component.EffectComponent;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * メカニックコンポーネントの基底クラス
 * <p>実際のゲーム効果を適用します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public abstract class MechanicComponent extends EffectComponent {

    /**
     * コンストラクタ
     *
     * @param key コンポーネントキー
     */
    protected MechanicComponent(String key) {
        super(key);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.MECHANIC;
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.isEmpty()) {
            return false;
        }

        // 全ターゲットに効果を適用
        boolean worked = false;
        for (LivingEntity target : targets) {
            if (target.isDead()) {
                continue;
            }
            if (apply(caster, level, target)) {
                worked = true;
            }
        }

        // 効果適用後に子コンポーネントを実行
        if (worked) {
            executeChildren(caster, level, targets);
        }

        return worked;
    }

    /**
     * 単一ターゲットに効果を適用します
     *
     * @param caster 発動者
     * @param level  スキルレベル
     * @param target 対象
     * @return 適用成功の場合はtrue
     */
    protected abstract boolean apply(LivingEntity caster, int level, LivingEntity target);
}
