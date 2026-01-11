package com.example.rpgplugin.skill.component.condition;

import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.entity.LivingEntity;

/**
 * 戦闘条件
 * <p>ターゲットが戦闘状態（ダメージを与えられた/受けた）の場合のみ効果を適用します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class CombatCondition extends ConditionComponent {

    private static final String MODE = "mode"; // attacking, defending, any
    private static final String SECONDS = "seconds"; // 戦闘状態とみなす秒数

    private static final int COMBAT_TIMEOUT = 10; // デフォルト10秒

    /**
     * コンストラクタ
     */
    public CombatCondition() {
        super("combat");
    }

    @Override
    protected boolean test(LivingEntity caster, int level, LivingEntity target) {
        ComponentSettings settings = getSettings();
        if (settings == null) {
            return isInCombat(target);
        }

        String mode = settings.getString(MODE, "any").toLowerCase();
        int seconds = settings.getInt(SECONDS, COMBAT_TIMEOUT);

        if ("any".equals(mode)) {
            return isInCombat(target, seconds);
        } else if ("attacking".equals(mode)) {
            return hasAttackedRecently(target, seconds);
        } else if ("defending".equals(mode)) {
            return wasDamagedRecently(target, seconds);
        }

        return isInCombat(target, seconds);
    }

    /**
     * エンティティが戦闘状態か確認します
     *
     * @param entity エンティティ
     * @return 戦闘状態の場合はtrue
     */
    private boolean isInCombat(LivingEntity entity) {
        return isInCombat(entity, COMBAT_TIMEOUT);
    }

    /**
     * エンティティが戦闘状態か確認します
     *
     * @param entity エンティティ
     * @param seconds 戦闘状態とみなす秒数
     * @return 戦闘状態の場合はtrue
     */
    private boolean isInCombat(LivingEntity entity, int seconds) {
        long noDamageTicks = entity.getNoDamageTicks();
        return noDamageTicks < (20 * seconds);
    }

    /**
     * エンティティが直前に攻撃したか確認します
     *
     * @param entity エンティティ
     * @param seconds 秒数
     * @return 攻撃した場合はtrue
     */
    private boolean hasAttackedRecently(LivingEntity entity, int seconds) {
        // RPGPluginのデータで追跡するか、簡易実装
        return entity.getNoDamageTicks() < (20 * seconds);
    }

    /**
     * エンティティが直後にダメージを受けたか確認します
     *
     * @param entity エンティティ
     * @param seconds 秒数
     * @return ダメージを受けた場合はtrue
     */
    private boolean wasDamagedRecently(LivingEntity entity, int seconds) {
        return entity.getNoDamageTicks() < (20 * seconds);
    }
}
