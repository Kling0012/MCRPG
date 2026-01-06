package com.example.rpgplugin.gui.menu.class;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.class.ClassManager;
import com.example.rpgplugin.class.ClassUpgrader;
import com.example.rpgplugin.class.RPGClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Optional;

/**
 * クラス選択GUI
 * 初期クラス選択、クラスアップ、クラス情報表示
 */
public class ClassMenu {

    private final RPGPlugin plugin;
    private final ClassManager classManager;
    private final ClassUpgrader classUpgrader;

    /** GUIタイトル */
    private static final String TITLE = "クラス選択";

    /** GUIサイズ */
    private static final int SIZE = 54;

    public ClassMenu(RPGPlugin plugin, ClassManager classManager, ClassUpgrader classUpgrader) {
        this.plugin = plugin;
        this.classManager = classManager;
        this.classUpgrader = classUpgrader;
    }

    /**
     * 初期クラス選択GUIを開く
     *
     * @param player プレイヤー
     */
    public void openInitialClassSelection(Player player) {
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);

        // 装飾枠
        fillBorders(inv);

        // 初期クラス（Rank1）を取得
        List<RPGClass> initialClasses = classManager.getInitialClasses();

        // クラスを表示
        int slot = 10;
        for (RPGClass rpgClass : initialClasses) {
            if (slot >= 35) {
                break;
            }

            ItemStack classItem = createClassItem(rpgClass, true);
            inv.setItem(slot, classItem);
            slot += 2;

            if (slot % 9 == 8) {
                slot += 2;
            }
        }

        // 説明アイテム
        ItemStack infoItem = createInfoItem();
        inv.setItem(49, infoItem);

        player.openInventory(inv);
    }

    /**
     * クラスアップGUIを開く
     *
     * @param player プレイヤー
     */
    public void openClassUpMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, SIZE, "クラスアップ");

        // 装飾枠
        fillBorders(inv);

        // 現在のクラスを取得
        Optional<RPGClass> currentClassOpt = classManager.getPlayerClass(player);
        if (!currentClassOpt.isPresent()) {
            player.sendMessage(ChatColor.RED + "クラスが設定されていません");
            return;
        }

        RPGClass currentClass = currentClassOpt.get();

        // 現在のクラスを表示
        ItemStack currentItem = createCurrentClassItem(currentClass);
        inv.setItem(4, currentItem);

        // 次のランクのクラスを表示
        int slot = 19;
        if (currentClass.hasNextRank()) {
            String nextClassId = currentClass.getNextRankClassId().get();
            Optional<RPGClass> nextClassOpt = classManager.getClass(nextClassId);

            if (nextClassOpt.isPresent()) {
                RPGClass nextClass = nextClassOpt.get();
                ClassManager.ClassUpResult result = classManager.canUpgradeClass(player, nextClassId);

                ItemStack nextItem = createClassUpItem(nextClass, result);
                inv.setItem(slot, nextItem);
                slot += 2;
            }
        }

        // 分岐クラスを表示
        for (String altClassId : currentClass.getAlternativeRanks().keySet()) {
            if (slot >= 34) {
                break;
            }

            Optional<RPGClass> altClassOpt = classManager.getClass(altClassId);
            if (altClassOpt.isPresent()) {
                RPGClass altClass = altClassOpt.get();
                ClassManager.ClassUpResult result = classManager.canUpgradeClass(player, altClassId);

                ItemStack altItem = createClassUpItem(altClass, result);
                inv.setItem(slot, altItem);
                slot += 2;
            }
        }

        // 戻るボタン
        ItemStack backButton = createBackButton();
        inv.setItem(45, backButton);

        player.openInventory(inv);
    }

    /**
     * クラス情報GUIを開く
     *
     * @param player  プレイヤー
     * @param classId クラスID
     */
    public void openClassInfo(Player player, String classId) {
        Optional<RPGClass> classOpt = classManager.getClass(classId);
        if (!classOpt.isPresent()) {
            player.sendMessage(ChatColor.RED + "クラスが見つかりません: " + classId);
            return;
        }

        RPGClass rpgClass = classOpt.get();
        Inventory inv = Bukkit.createInventory(null, 45, "クラス情報: " + rpgClass.getName());

        // クラスアイテム
        ItemStack classItem = createDetailedClassItem(rpgClass);
        inv.setItem(4, classItem);

        // 装飾
        fillBorders(inv);

        // 戻るボタン
        ItemStack backButton = createBackButton();
        inv.setItem(36, backButton);

        player.openInventory(inv);
    }

    // ========== Item Creation Methods ==========

    /**
     * クラスアイテムを作成（選択用）
     */
    private ItemStack createClassItem(RPGClass rpgClass, boolean selectable) {
        ItemStack item = new ItemStack(rpgClass.getIcon());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', rpgClass.getDisplayName()));

            // 説明文
            List<String> lore = new java.util.ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "ランク: " + ChatColor.GOLD + "Rank " + rpgClass.getRank());
            lore.add(ChatColor.GRAY + "最大レベル: " + ChatColor.WHITE + rpgClass.getMaxLevel());
            lore.add("");

            // 説明
            for (String desc : rpgClass.getDescription()) {
                lore.add(ChatColor.translateAlternateColorCodes('&', desc));
            }

            lore.add("");
            lore.add(ChatColor.GOLD + "【ステータス成長】");
            lore.add(ChatColor.GRAY + "自動配分: " + getAutoGrowthString(rpgClass));
            lore.add(ChatColor.GRAY + "手動配分: " + ChatColor.WHITE + "+" +
                    rpgClass.getStatGrowth().getManualPoints() + "ポイント");

            lore.add("");
            if (selectable) {
                lore.add(ChatColor.GREEN + "クリックで選択");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 現在のクラスアイテムを作成
     */
    private ItemStack createCurrentClassItem(RPGClass rpgClass) {
        ItemStack item = new ItemStack(rpgClass.getIcon());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', rpgClass.getDisplayName()));

            List<String> lore = new java.util.ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GREEN + "現在のクラス");
            lore.add(ChatColor.GRAY + "ランク: " + ChatColor.GOLD + "Rank " + rpgClass.getRank());
            lore.add("");
            lore.add(ChatColor.GRAY + "クラスアップするには下のクラスをクリック");

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * クラスアップアイテムを作成
     */
    private ItemStack createClassUpItem(RPGClass rpgClass, ClassManager.ClassUpResult result) {
        ItemStack item = new ItemStack(result.isSuccess() ? Material.GREEN_WOOL : Material.RED_WOOL);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', rpgClass.getDisplayName()));

            List<String> lore = new java.util.ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "ランク: " + ChatColor.GOLD + "Rank " + rpgClass.getRank());
            lore.add("");

            if (result.isSuccess()) {
                lore.add(ChatColor.GREEN + "✓ クラスアップ可能");
                lore.add(ChatColor.GRAY + "クリックでクラスアップ");
            } else {
                lore.add(ChatColor.RED + "✗ クラスアップ条件を満たしていません");
                lore.add("");
                lore.add(ChatColor.RED + "【不足している条件】");
                for (String req : result.getFailedRequirements()) {
                    lore.add(ChatColor.RED + " - " + req);
                }
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 詳細クラスアイテムを作成
     */
    private ItemStack createDetailedClassItem(RPGClass rpgClass) {
        ItemStack item = new ItemStack(rpgClass.getIcon());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', rpgClass.getDisplayName()));

            List<String> lore = new java.util.ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "ランク: " + ChatColor.GOLD + "Rank " + rpgClass.getRank());
            lore.add(ChatColor.GRAY + "最大レベル: " + ChatColor.WHITE + rpgClass.getMaxLevel());
            lore.add("");

            // 説明
            for (String desc : rpgClass.getDescription()) {
                lore.add(ChatColor.translateAlternateColorCodes('&', desc));
            }

            lore.add("");
            lore.add(ChatColor.GOLD + "【ステータス成長】");
            lore.add(getAutoGrowthString(rpgClass));
            lore.add(ChatColor.GRAY + "手動配分: " + ChatColor.WHITE + "+" +
                    rpgClass.getStatGrowth().getManualPoints() + "ポイント");

            // スキル
            if (!rpgClass.getAvailableSkills().isEmpty()) {
                lore.add("");
                lore.add(ChatColor.GOLD + "【使用可能スキル】");
                for (String skill : rpgClass.getAvailableSkills()) {
                    lore.add(ChatColor.GRAY + " - " + ChatColor.WHITE + skill);
                }
            }

            // パッシブボーナス
            if (!rpgClass.getPassiveBonuses().isEmpty()) {
                lore.add("");
                lore.add(ChatColor.GOLD + "【パッシブボーナス】");
                for (RPGClass.PassiveBonus bonus : rpgClass.getPassiveBonuses()) {
                    lore.add(ChatColor.GRAY + " - " + bonus.getType() + ": " + ChatColor.WHITE +
                            formatBonusValue(bonus.getValue()));
                }
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 自動成長文字列を生成
     */
    private String getAutoGrowthString(RPGClass rpgClass) {
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.GRAY).append("自動配分: ");

        boolean first = true;
        for (var entry : rpgClass.getStatGrowth().getAllAutoGrowth().entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(ChatColor.WHITE).append("+").append(entry.getValue()).append(" ")
                    .append(ChatColor.AQUA).append(entry.getKey().name());
            first = false;
        }

        return sb.toString();
    }

    /**
     * ボーナス値をフォーマット
     */
    private String formatBonusValue(double value) {
        if (value >= 1.0) {
            return String.format("+%.0f", value);
        } else {
            return String.format("+%.1f%%", value * 100);
        }
    }

    /**
     * 説明アイテムを作成
     */
    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "クラスについて");

            List<String> lore = new java.util.ArrayList<>();
            lore.add("");
            lore.add(ChatColor.WHITE + "クラスを選択すると、そのクラスに応じた");
            lore.add(ChatColor.WHITE + "ステータス成長ボーナスが得られます。");
            lore.add("");
            lore.add(ChatColor.WHITE + "レベル20でクラスアップが可能になり、");
            lore.add(ChatColor.WHITE + "さらに強力なクラスに進化できます。");
            lore.add("");
            lore.add(ChatColor.GRAY + "最初の選択は慎重に！");

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 戻るボタンを作成
     */
    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "← 戻る");

            List<String> lore = new java.util.ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "クリックで戻る");

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 枠を埋める
     */
    private void fillBorders(Inventory inv) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
        }

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, glass);
            inv.setItem(i + 45, glass);
        }

        for (int i = 0; i < 54; i += 9) {
            inv.setItem(i, glass);
            inv.setItem(i + 8, glass);
        }
    }
}
