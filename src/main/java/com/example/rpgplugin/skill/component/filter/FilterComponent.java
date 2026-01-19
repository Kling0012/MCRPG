package com.example.rpgplugin.skill.component.filter;

import com.example.rpgplugin.skill.component.ComponentType;
import com.example.rpgplugin.skill.component.EffectComponent;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * フィルターコンポーネントの基底クラス
 * <p>ターゲットを条件でフィルタリングします</p>
 *
 * <p>条件コンポーネント（ConditionComponent）との違い:</p>
 * <ul>
 *   <li>FilterComponent: ターゲットの属性（エンティティタイプ、グループ）でフィルタリング</li>
 *   <li>ConditionComponent: 状態（HP、MP、バイオーム等）でフィルタリング</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: フィルタリングに専念</li>
 *   <li>Strategy: 異なるフィルタロジックをStrategyパターンで管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public abstract class FilterComponent extends EffectComponent {

    /**
     * コンストラクタ
     *
     * @param key コンポーネントキー
     */
    protected FilterComponent(String key) {
        super(key);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.FILTER;
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        // フィルター条件を満たすターゲットのみを抽出
        List<LivingEntity> filtered = targets.stream()
                .filter(t -> test(caster, level, t))
                .collect(Collectors.toList());

        // 通過したターゲットがいれば、子コンポーネントを実行
        return !filtered.isEmpty() && executeChildren(caster, level, filtered);
    }

    /**
     * ターゲットがフィルター条件を満たすかテストします
     *
     * @param caster 発動者
     * @param level  スキルレベル
     * @param target テスト対象
     * @return 条件を満たす場合はtrue
     */
    protected abstract boolean test(LivingEntity caster, int level, LivingEntity target);
}
