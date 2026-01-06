package com.example.rpgplugin.rpgclass;

import com.example.rpgplugin.rpgclass.requirements.ItemRequirement;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.storage.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * クラスアップ実行クラス
 * 直線/分岐パターン対応、要件チェック、アイテム消費、履歴管理を行う
 */
public class ClassUpgrader {

    private final ClassManager classManager;
    private final PlayerManager playerManager;
    private final Logger logger;

    /**
     * コンストラクタ
     *
     * @param classManager   クラスマネージャー
     * @param playerManager  プレイヤーマネージャー
     */
    public ClassUpgrader(ClassManager classManager, PlayerManager playerManager) {
        this.classManager = classManager;
        this.playerManager = playerManager;
        this.logger = Logger.getLogger(ClassUpgrader.class.getName());
    }

    /**
     * クラスアップを実行
     *
     * @param player   プレイヤー
     * @param targetId 目標クラスID
     * @return 実行結果
     */
    public ClassUpResult executeClassUp(Player player, String targetId) {
        // 非同期でデータベース操作を避けるため、同期処理
        if (!Bukkit.isPrimaryThread()) {
            return new ClassUpResult(false, "クラスアップはメインスレッドから実行してください", false);
        }

        // 要件チェック
        ClassManager.ClassUpResult checkResult = classManager.canUpgradeClass(player, targetId);
        if (!checkResult.isSuccess()) {
            return new ClassUpResult(false, checkResult.getMessage(), false);
        }

        // クラス取得
        Optional<RPGClass> targetClassOpt = classManager.getClass(targetId);
        if (!targetClassOpt.isPresent()) {
            return new ClassUpResult(false, "クラスが見つかりません: " + targetId, false);
        }

        RPGClass targetClass = targetClassOpt.get();

        // アイテム消費処理
        Optional<RPGClass> currentClassOpt = classManager.getPlayerClass(player);
        if (currentClassOpt.isPresent()) {
            RPGClass currentClass = currentClassOpt.get();

            // 直線パターン
            if (currentClass.getNextRankClassId().map(id -> id.equals(targetId)).orElse(false)) {
                boolean consumed = consumeRequiredItems(player, currentClass.getNextRankRequirements());
                if (!consumed) {
                    return new ClassUpResult(false, "必要アイテムの消費に失敗しました", false);
                }
            }
            // 分岐パターン
            else if (currentClass.getAlternativeRanks().containsKey(targetId)) {
                boolean consumed = consumeRequiredItems(player,
                        currentClass.getAlternativeRanks().get(targetId));
                if (!consumed) {
                    return new ClassUpResult(false, "必要アイテムの消費に失敗しました", false);
                }
            }
        }

        // クラス変更実行
        boolean success = classManager.setPlayerClass(player, targetId);
        if (!success) {
            return new ClassUpResult(false, "クラスの設定に失敗しました", false);
        }

        // PlayerDataの更新
        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer != null) {
            PlayerData data = rpgPlayer.getPlayerData();
            data.setClassId(targetId);
            data.setClassRank(targetClass.getRank());

            // データベースに保存（PlayerManagerを通じて自動保存されるため、明示的な保存は不要）
        }

        // 成功ログ
        logger.info("Class up successful: " + player.getName() + " -> " + targetClass.getName() +
                " (Rank " + targetClass.getRank() + ")");

        return new ClassUpResult(true,
                "クラスを「" + targetClass.getDisplayName() + "」に変更しました！",
                true);
    }

    /**
     * 必要アイテムを消費
     *
     * @param player       プレイヤー
     * @param requirements 要件リスト
     * @return 消費成功時はtrue
     */
    private boolean consumeRequiredItems(Player player, java.util.List<com.example.rpgplugin.rpgclass.requirements.ClassRequirement> requirements) {
        for (com.example.rpgplugin.rpgclass.requirements.ClassRequirement req : requirements) {
            if (req instanceof ItemRequirement) {
                ItemRequirement itemReq = (ItemRequirement) req;
                if (itemReq.isConsumeOnUse()) {
                    boolean consumed = consumeItem(player, itemReq.getItemName(),
                            itemReq.getRequiredAmount());
                    if (!consumed) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * アイテムを消費
     *
     * @param player  プレイヤー
     * @param itemName アイテム名
     * @param amount   個数
     * @return 消費成功時はtrue
     */
    private boolean consumeItem(Player player, String itemName, int amount) {
        int remaining = amount;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                String displayName = item.getItemMeta().getDisplayName();
                if (displayName != null && displayName.equals(itemName)) {
                    int stackAmount = item.getAmount();
                    if (stackAmount <= remaining) {
                        player.getInventory().remove(item);
                        remaining -= stackAmount;
                    } else {
                        item.setAmount(stackAmount - remaining);
                        remaining = 0;
                    }

                    if (remaining <= 0) {
                        break;
                    }
                }
            }
        }

        return remaining <= 0;
    }

    /**
     * 初期クラスを設定
     *
     * @param player  プレイヤー
     * @param classId クラスID
     * @return 実行結果
     */
    public ClassUpResult setInitialClass(Player player, String classId) {
        // Rank1のクラスかチェック
        Optional<RPGClass> classOpt = classManager.getClass(classId);
        if (!classOpt.isPresent()) {
            return new ClassUpResult(false, "クラスが見つかりません: " + classId, false);
        }

        RPGClass rpgClass = classOpt.get();
        if (rpgClass.getRank() != 1) {
            return new ClassUpResult(false, "初期クラスはRank1のクラスを選択してください", false);
        }

        // まだクラス未設定かチェック
        if (classManager.getPlayerClass(player).isPresent()) {
            return new ClassUpResult(false, "既にクラスが設定されています", false);
        }

        // クラス設定
        boolean success = classManager.setPlayerClass(player, classId);
        if (!success) {
            return new ClassUpResult(false, "クラスの設定に失敗しました", false);
        }

        // PlayerDataの更新
        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer != null) {
            PlayerData data = rpgPlayer.getPlayerData();
            data.setClassId(classId);
            data.setClassRank(1);

            // データベースに保存（PlayerManagerを通じて自動保存されるため、明示的な保存は不要）
        }

        logger.info("Initial class set: " + player.getName() + " -> " + rpgClass.getName());

        return new ClassUpResult(true,
                "初期クラスを「" + rpgClass.getDisplayName() + "」に設定しました！",
                false);
    }

    /**
     * クラスアップ結果
     */
    public static class ClassUpResult {
        private final boolean success;
        private final String message;
        private final boolean rankedUp;

        public ClassUpResult(boolean success, String message, boolean rankedUp) {
            this.success = success;
            this.message = message;
            this.rankedUp = rankedUp;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public boolean isRankedUp() {
            return rankedUp;
        }
    }
}
