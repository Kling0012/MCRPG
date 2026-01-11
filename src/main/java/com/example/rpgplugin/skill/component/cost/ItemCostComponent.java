package com.example.rpgplugin.skill.component.cost;

import com.example.rpgplugin.skill.SkillCostType;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * アイテムを消費するコストコンポーネント
 *
 * <p>YAML設定例:</p>
 * <pre>
 * components:
 *   - type: ITEM
 *     item: DIAMOND
 *     amount: 1
 *     check_only: false
 * </pre>
 *
 * <p>設定パラメータ:</p>
 * <ul>
 *   <li>item: アイテムタイプ（Material名、必須）</li>
 *   <li>amount: 消費数量（デフォルト: 1）</li>
 *   <li>amount_per_level: レベルごとの数量増加量（デフォルト: 0）</li>
 *   <li>check_only: 消費せず所持チェックのみ行う（デフォルト: false）</li>
 *   <li>display_name: アイテムの表示名（オプション）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ItemCostComponent extends CostComponent {

    public ItemCostComponent() {
        super("ITEM");
    }

    @Override
    protected boolean consumeCost(LivingEntity caster, int level) {
        if (!(caster instanceof Player)) {
            return true; // プレイヤー以外はコスト不要
        }

        Player player = (Player) caster;
        PlayerInventory inventory = player.getInventory();

        // アイテムタイプを取得
        String itemStr = settings.getString("item", "DIAMOND");
        Material material = Material.matchMaterial(itemStr);

        if (material == null) {
            return false;
        }

        // 数量を計算
        int amount = settings.getInt("amount", 1);
        int perLevel = settings.getInt("amount_per_level", 0);
        int totalAmount = amount + (perLevel * (level - 1));

        if (totalAmount <= 0) {
            return true;
        }

        // 所持チェック
        int hasAmount = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                hasAmount += item.getAmount();
            }
        }

        if (hasAmount < totalAmount) {
            return false;
        }

        // チェックのみの場合はここで終了
        boolean checkOnly = settings.getBoolean("check_only", false);
        if (checkOnly) {
            return true;
        }

        // アイテムを消費
        int remaining = totalAmount;
        for (int i = 0; i < inventory.getContents().length && remaining > 0; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == material) {
                int stackAmount = item.getAmount();
                if (stackAmount <= remaining) {
                    remaining -= stackAmount;
                    inventory.setItem(i, null);
                } else {
                    item.setAmount(stackAmount - remaining);
                    remaining = 0;
                }
            }
        }

        return true;
    }

    @Override
    public SkillCostType getCostType() {
        return SkillCostType.MANA; // アイテムは独自タイプとして扱う
    }

    /**
     * 必要なアイテムを取得します
     *
     * @param level スキルレベル
     * @return アイテムスタック、見つからない場合はnull
     */
    public ItemStack getRequiredItem(int level) {
        String itemStr = settings.getString("item", "DIAMOND");
        Material material = Material.matchMaterial(itemStr);

        if (material == null) {
            return null;
        }

        int amount = settings.getInt("amount", 1);
        int perLevel = settings.getInt("amount_per_level", 0);
        int totalAmount = amount + (perLevel * (level - 1));

        return new ItemStack(material, Math.max(1, totalAmount));
    }
}
