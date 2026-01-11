package com.example.rpgplugin.skill.component.condition;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

/**
 * MP条件コンポーネント
 * <p>発動者のMPに基づいてフィルタリングします</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ManaCondition extends ConditionComponent {

    private static final String MIN = "min-value";

    /**
     * コンストラクタ
     */
    public ManaCondition() {
        super("mana");
    }

    @Override
    protected boolean test(LivingEntity caster, int level, LivingEntity target) {
        // キャスターのMPをチェック（ターゲットは無視）
        if (!(caster instanceof Player)) {
            return true;
        }

        PlayerManager playerManager = RPGPlugin.getInstance().getPlayerManager();
        if (playerManager == null) {
            return true;
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(caster.getUniqueId());
        if (rpgPlayer == null) {
            return true;
        }

        double minMana = parseValues(caster, MIN, level, 0);
        return rpgPlayer.getCurrentMana() >= minMana;
    }
}
