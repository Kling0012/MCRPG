package com.example.rpgplugin.skill.executor;

import com.example.rpgplugin.skill.Skill;
import org.bukkit.entity.Player;

/**
 * スキル実行エグゼキューターインターフェース
 *
 * <p>スキルの実行処理を定義します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキル実行の抽象化</li>
 *   <li>Strategy: 異なるスキルタイプの実行をStrategyパターンで管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public interface SkillExecutor {

    /**
     * スキルを実行します
     *
     * @param player 発動者
     * @param skill スキル
     * @param level スキルレベル
     * @return 成功した場合はtrue
     */
    boolean execute(Player player, Skill skill, int level);
}
