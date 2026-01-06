package com.example.rpgplugin.class.requirements;

import org.bukkit.entity.Player;

/**
 * クラス変更要件を表すインターフェース
 * レベル、ステータス、アイテム、クエストなどの条件を定義
 */
public interface ClassRequirement {

    /**
     * プレイヤーがこの要件を満たしているかチェック
     *
     * @param player チェック対象プレイヤー
     * @return 要件を満たしている場合はtrue
     */
    boolean check(Player player);

    /**
     * 要件の説明文を取得
     *
     * @return 説明文（プレイヤーに表示する用）
     */
    String getDescription();

    /**
     * 要件タイプを取得
     *
     * @return タイプ名
     */
    String getType();
}
