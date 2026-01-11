package com.example.rpgplugin.skill.component;

/**
 * コンポーネントタイプ
 * <p>SkillAPIのコンポーネントシステムを参考に実装</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public enum ComponentType {

    /**
     * 条件コンポーネント - ターゲットをフィルタリング
     */
    CONDITION,

    /**
     * メカニックコンポーネント - 実際の効果を適用
     */
    MECHANIC,

    /**
     * フィルターコンポーネント - ターゲット属性でフィルタリング
     */
    FILTER,

    /**
     * ターゲットコンポーネント - ターゲットを選択
     */
    TARGET,

    /**
     * トリガーコンポーネント - スキル発動のトリガー
     */
    TRIGGER,

    /**
     * コストコンポーネント - スキル発動のコスト（MP/HP消費）
     */
    COST,

    /**
     * クールダウンコンポーネント - スキルのクールダウン時間
     */
    COOLDOWN
}
