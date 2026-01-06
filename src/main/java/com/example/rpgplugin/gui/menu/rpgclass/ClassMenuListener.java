package com.example.rpgplugin.gui.menu.rpgclass;

import com.example.rpgplugin.rpgclass.ClassManager;
import com.example.rpgplugin.rpgclass.ClassUpgrader;
import com.example.rpgplugin.rpgclass.RPGClass;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * クラスGUIイベントリスナー
 */
public class ClassMenuListener implements Listener {

    private final ClassMenu classMenu;
    private final ClassManager classManager;
    private final ClassUpgrader classUpgrader;

    public ClassMenuListener(ClassMenu classMenu, ClassManager classManager, ClassUpgrader classUpgrader) {
        this.classMenu = classMenu;
        this.classManager = classManager;
        this.classUpgrader = classUpgrader;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // クラス選択GUI
        if (title.equals("クラス選択")) {
            event.setCancelled(true);
            handleClassSelection(event, player);
        }
        // クラスアップGUI
        else if (title.equals("クラスアップ")) {
            event.setCancelled(true);
            handleClassUp(event, player);
        }
        // クラス情報GUI
        else if (title.startsWith("クラス情報: ")) {
            event.setCancelled(true);
            handleClassInfo(event, player);
        }
    }

    /**
     * クラス選択を処理
     */
    private void handleClassSelection(InventoryClickEvent event, Player player) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }

        int slot = event.getSlot();

        // クラススロット（10, 12, 14, 16, 19, 21...）
        if ((slot >= 10 && slot <= 16 && slot % 2 == 0) ||
                (slot >= 19 && slot <= 25 && slot % 2 == 1) ||
                (slot >= 28 && slot <= 34 && slot % 2 == 0)) {

            // アイテム名からクラスIDを取得（簡易実装）
            String displayName = clicked.getItemMeta().getDisplayName();

            // カラーコードを除去
            String cleanName = ChatColor.stripColor(displayName);

            // クラス名からクラスIDを検索
            for (RPGClass rpgClass : classManager.getInitialClasses()) {
                if (rpgClass.getName().equals(cleanName)) {
                    // 初期クラス設定
                    ClassUpgrader.ClassUpResult result = classUpgrader.setInitialClass(player, rpgClass.getId());

                    if (result.isSuccess()) {
                        player.sendMessage(ChatColor.GREEN + result.getMessage());
                        player.closeInventory();
                    } else {
                        player.sendMessage(ChatColor.RED + result.getMessage());
                    }
                    return;
                }
            }
        }
    }

    /**
     * クラスアップを処理
     */
    private void handleClassUp(InventoryClickEvent event, Player player) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }

        int slot = event.getSlot();

        // 戻るボタン
        if (slot == 45 && clicked.getType().name().contains("ARROW")) {
            player.closeInventory();
            return;
        }

        // クラススロット
        if ((slot >= 19 && slot <= 25) || (slot >= 28 && slot <= 34)) {
            String displayName = clicked.getItemMeta().getDisplayName();
            String cleanName = ChatColor.stripColor(displayName);

            // 現在のクラスを取得
            Optional<RPGClass> currentClassOpt = classManager.getPlayerClass(player);
            if (!currentClassOpt.isPresent()) {
                return;
            }

            RPGClass currentClass = currentClassOpt.get();

            // 次のランクを検索
            if (currentClass.hasNextRank()) {
                String nextClassId = currentClass.getNextRankClassId().get();
                Optional<RPGClass> nextClassOpt = classManager.getClass(nextClassId);

                if (nextClassOpt.isPresent()) {
                    RPGClass nextClass = nextClassOpt.get();
                    if (nextClass.getName().equals(cleanName)) {
                        executeClassUp(player, nextClassId);
                        return;
                    }
                }
            }

            // 分岐クラスを検索
            for (String altClassId : currentClass.getAlternativeRanks().keySet()) {
                Optional<RPGClass> altClassOpt = classManager.getClass(altClassId);
                if (altClassOpt.isPresent()) {
                    RPGClass altClass = altClassOpt.get();
                    if (altClass.getName().equals(cleanName)) {
                        executeClassUp(player, altClassId);
                        return;
                    }
                }
            }
        }
    }

    /**
     * クラスアップを実行
     */
    private void executeClassUp(Player player, String classId) {
        ClassUpgrader.ClassUpResult result = classUpgrader.executeClassUp(player, classId);

        if (result.isSuccess()) {
            player.sendMessage(ChatColor.GREEN + result.getMessage());
            player.closeInventory();
        } else {
            player.sendMessage(ChatColor.RED + result.getMessage());

            if (!result.isRankedUp() && !result.getFailedRequirements().isEmpty()) {
                player.sendMessage(ChatColor.RED + "【不足条件】");
                for (String req : result.getFailedRequirements()) {
                    player.sendMessage(ChatColor.RED + " - " + req);
                }
            }
        }
    }

    /**
     * クラス情報を処理
     */
    private void handleClassInfo(InventoryClickEvent event, Player player) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }

        int slot = event.getSlot();

        // 戻るボタン
        if (slot == 36 && clicked.getType().name().contains("ARROW")) {
            player.closeInventory();
            // クラス選択画面に戻る場合は再表示
            // classMenu.openInitialClassSelection(player);
        }
    }
}
