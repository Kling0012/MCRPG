package com.example.rpgplugin.skill.component.cost;

import com.example.rpgplugin.skill.SkillCostType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * スタミナを消費するコストコンポーネント
 *
 * <p>注意: 現在の実装ではスタミナシステムは未実装のため、
 * 常に成功を返します。将来の拡張用に用意されています。</p>
 *
 * <p>YAML設定例:</p>
 * <pre>
 * components:
 *   - type: STAMINA
 *     base: 5.0
 *     per_level: 1.0
 *     min: 0.0
 * </pre>
 *
 * <p>設定パラメータ:</p>
 * <ul>
 *   <li>base: 基本コスト（デフォルト: 5.0）</li>
 *   <li>per_level: レベルごとのコスト増加量（デフォルト: 0.0）</li>
 *   <li>min: 最小コスト（デフォルト: 0.0）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class StaminaCostComponent extends CostComponent {

    public StaminaCostComponent() {
        super("STAMINA");
    }

    @Override
    protected boolean consumeCost(LivingEntity caster, int level) {
        if (!(caster instanceof Player)) {
            return true; // プレイヤー以外はコスト不要
        }

        double cost = calculateCost(level);

        if (cost <= 0) {
            return true;
        }

        // TODO: スタミナシステム実装時に消費処理を追加
        // 現在は常に成功を返す
        return true;
    }

    @Override
    public SkillCostType getCostType() {
        // スタミナは独自のコストタイプとして扱う
        return SkillCostType.MANA; // 暫定的にMANAを使用
    }
}
