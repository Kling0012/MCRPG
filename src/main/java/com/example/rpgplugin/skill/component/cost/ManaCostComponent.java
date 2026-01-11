package com.example.rpgplugin.skill.component.cost;

import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.skill.SkillCostType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * マナを消費するコストコンポーネント
 *
 * <p>YAML設定例:</p>
 * <pre>
 * components:
 *   - type: MANA
 *     base: 10.0
 *     per_level: 2.0
 *     min: 0.0
 *     max: 100.0
 * </pre>
 *
 * <p>設定パラメータ:</p>
 * <ul>
 *   <li>base: 基本コスト（デフォルト: 10.0）</li>
 *   <li>per_level: レベルごとのコスト増加量（デフォルト: 0.0）</li>
 *   <li>min: 最小コスト（デフォルト: 0.0）</li>
 *   <li>max: 最大コスト（デフォルト: 無制限）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ManaCostComponent extends CostComponent {

    private final RPGPlugin plugin;

    public ManaCostComponent(RPGPlugin plugin) {
        super("MANA");
        this.plugin = plugin;
    }

    public ManaCostComponent() {
        this(null);
    }

    @Override
    protected boolean consumeCost(LivingEntity caster, int level) {
        if (!(caster instanceof Player)) {
            return true; // プレイヤー以外はコスト不要
        }

        Player player = (Player) caster;
        double cost = calculateCost(level);

        if (cost <= 0) {
            return true;
        }

        // PlayerManagerからRPGPlayerを取得
        PlayerManager playerManager = plugin != null
                ? plugin.getPlayerManager()
                : null;

        if (playerManager == null) {
            return true; // テスト環境などではスキップ
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            return false;
        }

        // MPを消費
        int manaCost = (int) Math.ceil(cost);
        if (rpgPlayer.hasMana(manaCost)) {
            return rpgPlayer.consumeMana(manaCost);
        }

        return false;
    }

    @Override
    public SkillCostType getCostType() {
        return SkillCostType.MANA;
    }
}
