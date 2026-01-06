package com.example.rpgplugin.rpgclass.requirements;

import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * アイテム要件を表すクラス
 * 特定のアイテムの所持を条件とする
 */
public class ItemRequirement implements ClassRequirement {

    private final String itemName;
    private final int requiredAmount;
    private final boolean consumeOnUse;

    /**
     * コンストラクタ
     *
     * @param itemName       アイテム名（識別用）
     * @param requiredAmount 必要個数
     * @param consumeOnUse   クラス変更時に消費するか
     */
    public ItemRequirement(String itemName, int requiredAmount, boolean consumeOnUse) {
        this.itemName = itemName;
        this.requiredAmount = Math.max(1, requiredAmount);
        this.consumeOnUse = consumeOnUse;
    }

    @Override
    public boolean check(Player player) {
        if (player == null) {
            return false;
        }

        // インベントリ内のアイテムをカウント
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                String displayName = item.getItemMeta().getDisplayName();
                if (displayName != null && displayName.equals(itemName)) {
                    count += item.getAmount();
                }
            }
        }

        return count >= requiredAmount;
    }

    @Override
    public String getDescription() {
        return (consumeOnUse ? "消費:" : "所持:") + itemName + " x" + requiredAmount;
    }

    @Override
    public String getType() {
        return "item";
    }

    /**
     * アイテム名を取得
     *
     * @return アイテム名
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * 必要個数を取得
     *
     * @return 個数
     */
    public int getRequiredAmount() {
        return requiredAmount;
    }

    /**
     * 使用時に消費するか
     *
     * @return 消費する場合はtrue
     */
    public boolean isConsumeOnUse() {
        return consumeOnUse;
    }

    /**
     * ConfigurationSectionからパース
     *
     * @param section 設定セクション
     * @return ItemRequirementインスタンス
     */
    public static ItemRequirement parse(ConfigurationSection section) {
        String item = section.getString("item", "UNKNOWN");
        int amount = section.getInt("amount", 1);
        boolean consume = section.getBoolean("consume", false);

        return new ItemRequirement(item, amount, consume);
    }
}
