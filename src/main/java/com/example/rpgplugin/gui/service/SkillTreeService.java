package com.example.rpgplugin.gui.service;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillNode;
import com.example.rpgplugin.skill.SkillTree;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * スキルツリーサービス
 *
 * <p>スキルツリーGUIのビジネスロジックを担当します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキル操作ビジネスロジックに専念</li>
 *   <li>DRY: スキル習得/解放ロジックを一元管理</li>
 *   <li>KISS: シンプルで明確なAPI</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillTreeService {

    private final RPGPlugin plugin;
    private final SkillManager skillManager;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public SkillTreeService(RPGPlugin plugin) {
        this.plugin = plugin;
        this.skillManager = plugin.getSkillManager();
    }

    /**
     * スキルレベルを取得します
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return スキルレベル
     */
    public int getSkillLevel(Player player, String skillId) {
        return skillManager.getSkillLevel(player, skillId);
    }

    /**
     * 利用可能なスキルポイントを計算します
     *
     * @param player プレイヤー
     * @param classId クラスID
     * @return スキルポイント
     */
    public int getAvailableSkillPoints(Player player, String classId) {
        int playerLevel = player.getLevel();

        // 習得済みスキルの総コストを計算
        int usedPoints = 0;
        SkillTree skillTree = skillManager.getTreeRegistry().getTree(classId);
        if (skillTree == null) {
            return 0;
        }

        for (Skill skill : skillManager.getSkillsForClass(classId)) {
            int level = getSkillLevel(player, skill.getId());
            if (level > 0) {
                int cost = skillTree.getCost(skill.getId());
                usedPoints += cost * level;
            }
        }

        // 基礎スキルポイント（レベル * 1 + 5）
        int basePoints = playerLevel + 5;

        return Math.max(0, basePoints - usedPoints);
    }

    /**
     * スキルを習得可能かチェックします
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @param classId クラスID
     * @return 習得可能な場合はtrue
     */
    public boolean canAcquireSkill(Player player, String skillId, String classId) {
        Skill skill = skillManager.getSkill(skillId);
        if (skill == null) {
            return false;
        }

        int currentLevel = getSkillLevel(player, skillId);

        // 最大レベルチェック
        if (currentLevel >= skill.getMaxLevel()) {
            return false;
        }

        // 親スキルチェック
        SkillTree skillTree = skillManager.getTreeRegistry().getTree(classId);
        if (skillTree != null) {
            SkillNode node = skillTree.getNode(skillId);
            if (node != null && !node.isRoot()) {
                String parentId = skillTree.getParentSkillId(skillId);
                if (parentId != null && !"none".equalsIgnoreCase(parentId)) {
                    int parentLevel = getSkillLevel(player, parentId);
                    if (parentLevel == 0) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * スキルを習得します
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @param classId クラスID
     * @return 習得結果
     */
    public SkillAcquireResult acquireSkill(Player player, String skillId, String classId) {
        Skill skill = skillManager.getSkill(skillId);
        if (skill == null) {
            return SkillAcquireResult.failure("スキルが見つかりません: " + skillId);
        }

        int currentLevel = getSkillLevel(player, skillId);
        int availablePoints = getAvailableSkillPoints(player, classId);

        SkillTree skillTree = skillManager.getTreeRegistry().getTree(classId);
        int cost = skillTree != null ? skillTree.getCost(skillId) : 1;

        // スキルポイントチェック
        if (availablePoints < cost) {
            return SkillAcquireResult.failure("スキルポイントが足りません（必要: " + cost + "、所持: " + availablePoints + "）");
        }

        // 習得条件チェック
        if (!canAcquireSkill(player, skillId, classId)) {
            return SkillAcquireResult.failure("習得条件を満たしていません");
        }

        // 習得処理
        if (currentLevel == 0) {
            // 新規習得
            if (skillManager.acquireSkill(player, skillId, 1)) {
                return SkillAcquireResult.success(skill.getColoredDisplayName(), 1);
            }
        } else {
            // レベルアップ
            if (currentLevel >= skill.getMaxLevel()) {
                return SkillAcquireResult.failure("既に最大レベルに達しています");
            }
            if (skillManager.upgradeSkill(player, skillId)) {
                return SkillAcquireResult.success(skill.getColoredDisplayName(), currentLevel + 1);
            }
        }

        return SkillAcquireResult.failure("習得に失敗しました");
    }

    /**
     * スキルを解除します
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @param classId クラスID
     * @return 解除結果
     */
    public SkillRefundResult refundSkill(Player player, String skillId, String classId) {
        Skill skill = skillManager.getSkill(skillId);
        if (skill == null) {
            return SkillRefundResult.failure("スキルが見つかりません: " + skillId);
        }

        int currentLevel = getSkillLevel(player, skillId);
        if (currentLevel == 0) {
            return SkillRefundResult.failure("このスキルは習得していません");
        }

        // 子スキルチェック
        SkillTree skillTree = skillManager.getTreeRegistry().getTree(classId);
        if (skillTree != null) {
            SkillNode node = skillTree.getNode(skillId);
            if (node != null && !node.isLeaf()) {
                for (SkillNode child : node.getChildren()) {
                    if (getSkillLevel(player, child.getSkill().getId()) > 0) {
                        return SkillRefundResult.failure("子スキルを先に解除してください");
                    }
                }
            }
        }

        // 解除処理
        int newLevel;
        if (currentLevel == 1) {
            // 完全に削除
            skillManager.getPlayerSkillData(player).removeSkill(skillId);
            newLevel = 0;
        } else {
            // レベルダウン
            skillManager.getPlayerSkillData(player).setSkillLevel(skillId, currentLevel - 1);
            newLevel = currentLevel - 1;
        }

        return SkillRefundResult.success(skill.getColoredDisplayName(), newLevel);
    }

    /**
     * スキル習得結果
     */
    public static class SkillAcquireResult {
        private final boolean success;
        private final String message;
        private final String skillName;
        private final int newLevel;

        private SkillAcquireResult(boolean success, String message, String skillName, int newLevel) {
            this.success = success;
            this.message = message;
            this.skillName = skillName;
            this.newLevel = newLevel;
        }

        public static SkillAcquireResult success(String skillName, int newLevel) {
            return new SkillAcquireResult(true, null, skillName, newLevel);
        }

        public static SkillAcquireResult failure(String message) {
            return new SkillAcquireResult(false, message, null, 0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getSkillName() {
            return skillName;
        }

        public int getNewLevel() {
            return newLevel;
        }

        public void sendMessageTo(Player player) {
            if (success) {
                player.sendMessage(Component.text("スキルを習得しました: " + skillName, NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text(message, NamedTextColor.RED));
            }
        }
    }

    /**
     * スキル解除結果
     */
    public static class SkillRefundResult {
        private final boolean success;
        private final String message;
        private final String skillName;
        private final int newLevel;

        private SkillRefundResult(boolean success, String message, String skillName, int newLevel) {
            this.success = success;
            this.message = message;
            this.skillName = skillName;
            this.newLevel = newLevel;
        }

        public static SkillRefundResult success(String skillName, int newLevel) {
            return new SkillRefundResult(true, null, skillName, newLevel);
        }

        public static SkillRefundResult failure(String message) {
            return new SkillRefundResult(false, message, null, 0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getSkillName() {
            return skillName;
        }

        public int getNewLevel() {
            return newLevel;
        }

        public void sendMessageTo(Player player) {
            if (success) {
                if (newLevel == 0) {
                    player.sendMessage(Component.text("スキルを解除しました: " + skillName, NamedTextColor.YELLOW));
                } else {
                    player.sendMessage(Component.text("スキルレベルを下げました: " + skillName + " Lv." + newLevel, NamedTextColor.YELLOW));
                }
            } else {
                player.sendMessage(Component.text(message, NamedTextColor.RED));
            }
        }
    }
}
