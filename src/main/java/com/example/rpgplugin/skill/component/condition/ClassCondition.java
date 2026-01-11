package com.example.rpgplugin.skill.component.condition;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.component.ComponentSettings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * クラス条件
 * <p>ターゲットが特定のクラスの場合のみ効果を適用します</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ClassCondition extends ConditionComponent {

    private static final String CLASS = "class";
    private static final String EXACT = "exact";

    /**
     * コンストラクタ
     */
    public ClassCondition() {
        super("class");
    }

    @Override
    protected boolean test(LivingEntity caster, int level, LivingEntity target) {
        if (!(target instanceof Player)) {
            return false;
        }

        ComponentSettings settings = getSettings();
        if (settings == null) {
            return false;
        }

        String requiredClass = settings.getString(CLASS, "");
        boolean exact = settings.getBoolean(EXACT, false);

        RPGPlayer rpgPlayer = RPGPlugin.getInstance().getPlayerManager().getRPGPlayer(target.getUniqueId());
        if (rpgPlayer == null) {
            return false;
        }

        String currentClass = rpgPlayer.getClassId();

        if (currentClass == null) {
            return requiredClass.isEmpty();
        }

        if (exact) {
            return currentClass.equalsIgnoreCase(requiredClass);
        } else {
            // クラスツリーを考慮（将来実装）
            return currentClass.equalsIgnoreCase(requiredClass);
        }
    }
}
