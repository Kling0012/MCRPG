package com.example.rpgplugin.skill.repository;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * プレイヤースキルサービス
 *
 * <p>プレイヤーの習得スキルデータを管理します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: プレイヤースキルデータ管理の単一責務</li>
 *   <li>Thread-Safety: ConcurrentHashMapによるスレッドセーフ</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class PlayerSkillService {

    private static final Logger LOGGER = Logger.getLogger(PlayerSkillService.class.getName());

    /** プレイヤーのスキルデータ */
    private final Map<UUID, PlayerSkillData> playerSkills;

    /**
     * コンストラクタ
     */
    public PlayerSkillService() {
        this.playerSkills = new ConcurrentHashMap<>();
    }

    /**
     * プレイヤーのスキルデータを取得します
     *
     * @param player プレイヤー
     * @return プレイヤーのスキルデータ
     */
    public PlayerSkillData getPlayerSkillData(Player player) {
        return playerSkills.computeIfAbsent(player.getUniqueId(), k -> new PlayerSkillData());
    }

    /**
     * プレイヤーのスキルデータを取得します
     *
     * @param uuid プレイヤーUUID
     * @return プレイヤーのスキルデータ
     */
    public PlayerSkillData getPlayerSkillData(UUID uuid) {
        return playerSkills.computeIfAbsent(uuid, k -> new PlayerSkillData());
    }

    /**
     * プレイヤーのスキルデータが存在するかチェックします
     *
     * @param uuid プレイヤーUUID
     * @return 存在する場合はtrue
     */
    public boolean hasPlayerSkillData(UUID uuid) {
        return playerSkills.containsKey(uuid);
    }

    /**
     * プレイヤーデータをアンロードします
     *
     * @param uuid プレイヤーUUID
     * @return アンロードされたデータ（nullの場合もあり）
     */
    public PlayerSkillData unloadPlayerData(UUID uuid) {
        return playerSkills.remove(uuid);
    }

    /**
     * 全プレイヤーデータをクリアします
     */
    public void clearAllPlayerData() {
        playerSkills.clear();
    }

    /**
     * 読み込まれているプレイヤーデータ数を返します
     *
     * @return プレイヤーデータ数
     */
    public int size() {
        return playerSkills.size();
    }

    /**
     * プレイヤーがスキルを習得しているかチェックします
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return 習得している場合はtrue
     */
    public boolean hasSkill(Player player, String skillId) {
        PlayerSkillData data = getPlayerSkillData(player);
        return data.hasSkill(skillId);
    }

    /**
     * プレイヤーがスキルを習得しているかチェックします
     *
     * @param uuid プレイヤーUUID
     * @param skillId スキルID
     * @return 習得している場合はtrue
     */
    public boolean hasSkill(UUID uuid, String skillId) {
        PlayerSkillData data = getPlayerSkillData(uuid);
        return data.hasSkill(skillId);
    }

    /**
     * プレイヤーのスキルレベルを取得します
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return スキルレベル（習得していない場合は0）
     */
    public int getSkillLevel(Player player, String skillId) {
        PlayerSkillData data = getPlayerSkillData(player);
        return data.getSkillLevel(skillId);
    }

    /**
     * プレイヤーのスキルレベルを取得します
     *
     * @param uuid プレイヤーUUID
     * @param skillId スキルID
     * @return スキルレベル（習得していない場合は0）
     */
    public int getSkillLevel(UUID uuid, String skillId) {
        PlayerSkillData data = getPlayerSkillData(uuid);
        return data.getSkillLevel(skillId);
    }

    /**
     * スキルを習得させます
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @param level レベル
     * @param maxLevel 最大レベル
     * @param displayName スキル表示名
     * @return 成功した場合はtrue
     */
    public boolean acquireSkill(Player player, String skillId, int level, int maxLevel, String displayName) {
        PlayerSkillData data = getPlayerSkillData(player);
        int currentLevel = data.getSkillLevel(skillId);

        if (currentLevel > 0 && level <= currentLevel) {
            player.sendMessage(Component.text("既により高いレベルを習得しています", NamedTextColor.RED));
            return false;
        }

        if (level > maxLevel) {
            player.sendMessage(Component.text("最大レベルを超えています: " + maxLevel, NamedTextColor.RED));
            return false;
        }

        data.setSkillLevel(skillId, level);
        player.sendMessage(Component.text("スキルを習得しました: " + displayName + " Lv." + level, NamedTextColor.GREEN));
        LOGGER.fine(() -> "Skill acquired: " + player.getUniqueId() + " -> " + skillId + " level " + level);
        return true;
    }

    /**
     * スキルレベルを上げます
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @param maxLevel 最大レベル
     * @param displayName スキル表示名
     * @return 成功した場合はtrue
     */
    public boolean upgradeSkill(Player player, String skillId, int maxLevel, String displayName) {
        PlayerSkillData data = getPlayerSkillData(player);
        int currentLevel = data.getSkillLevel(skillId);

        if (currentLevel == 0) {
            player.sendMessage(Component.text("まずスキルを習得してください", NamedTextColor.RED));
            return false;
        }

        if (currentLevel >= maxLevel) {
            player.sendMessage(Component.text("既に最大レベルに達しています", NamedTextColor.RED));
            return false;
        }

        data.setSkillLevel(skillId, currentLevel + 1);
        player.sendMessage(Component.text("スキルを強化しました: " + displayName + " Lv." + (currentLevel + 1), NamedTextColor.GREEN));
        LOGGER.fine(() -> "Skill upgraded: " + player.getUniqueId() + " -> " + skillId + " " + currentLevel + "->" + (currentLevel + 1));
        return true;
    }

    /**
     * 指定したスキルを解放しているプレイヤーからスキルデータを削除します
     *
     * @param removedSkillIds 削除されたスキルIDセット
     * @return 削除されたスキルデータの合計数
     */
    public int clearRemovedSkillsFromAllPlayers(Set<String> removedSkillIds) {
        int totalRemoved = 0;

        for (PlayerSkillData skillData : playerSkills.values()) {
            int removedCount = skillData.removeSkills(removedSkillIds);
            totalRemoved += removedCount;
        }

        return totalRemoved;
    }

    /**
     * プレイヤーのスキルデータをクリーンアップします
     *
     * @param removedSkillIds 削除されたスキルIDセット
     * @return クリーンアップサマリー
     */
    public CleanupSummary cleanupRemovedSkills(Set<String> removedSkillIds) {
        int affectedPlayers = 0;
        int totalSkillsRemoved = 0;

        for (Map.Entry<UUID, PlayerSkillData> entry : playerSkills.entrySet()) {
            int removedCount = entry.getValue().removeSkills(removedSkillIds);
            if (removedCount > 0) {
                affectedPlayers++;
                totalSkillsRemoved += removedCount;
            }
        }

        return new CleanupSummary(affectedPlayers, totalSkillsRemoved);
    }

    /**
     * 全プレイヤーのUUIDを取得します
     *
     * @return プレイヤーUUIDのセット
     */
    public Set<UUID> getAllPlayerUuids() {
        return new java.util.HashSet<>(playerSkills.keySet());
    }

    /**
     * クリーンアップサマリー
     */
    public static class CleanupSummary {
        private final int affectedPlayers;
        private final int totalSkillsRemoved;

        public CleanupSummary(int affectedPlayers, int totalSkillsRemoved) {
            this.affectedPlayers = affectedPlayers;
            this.totalSkillsRemoved = totalSkillsRemoved;
        }

        public int getAffectedPlayers() {
            return affectedPlayers;
        }

        public int getTotalSkillsRemoved() {
            return totalSkillsRemoved;
        }

        public boolean hasChanges() {
            return affectedPlayers > 0 || totalSkillsRemoved > 0;
        }

        @Override
        public String toString() {
            return "CleanupSummary{" +
                    "affectedPlayers=" + affectedPlayers +
                    ", totalSkillsRemoved=" + totalSkillsRemoved +
                    '}';
        }
    }

    /**
     * プレイヤーのスキルデータ
     */
    public static class PlayerSkillData {
        private final Map<String, Integer> acquiredSkills; // skillId -> level
        private final Map<String, Long> cooldowns; // skillId -> lastCastTime
        private int skillPoints;

        public PlayerSkillData() {
            this.acquiredSkills = new ConcurrentHashMap<>();
            this.cooldowns = new ConcurrentHashMap<>();
            this.skillPoints = 0;
        }

        public Map<String, Integer> getAcquiredSkills() {
            return new java.util.HashMap<>(acquiredSkills);
        }

        public int getSkillLevel(String skillId) {
            return acquiredSkills.getOrDefault(skillId, 0);
        }

        public boolean hasSkill(String skillId) {
            if (skillId == null || skillId.isEmpty()) {
                return false;
            }
            return acquiredSkills.containsKey(skillId) && acquiredSkills.get(skillId) > 0;
        }

        public void setSkillLevel(String skillId, int level) {
            if (level <= 0) {
                acquiredSkills.remove(skillId);
            } else {
                acquiredSkills.put(skillId, level);
            }
        }

        /**
         * スキルを削除します
         *
         * @param skillId スキルID
         */
        public void removeSkill(String skillId) {
            acquiredSkills.remove(skillId);
        }

        /**
         * 複数のスキルを削除します
         *
         * @param skillIds 削除するスキルIDセット
         * @return 削除された数
         */
        public int removeSkills(Set<String> skillIds) {
            int count = 0;
            for (String skillId : skillIds) {
                if (acquiredSkills.remove(skillId) != null) {
                    count++;
                }
            }
            return count;
        }

        public Map<String, Long> getCooldowns() {
            return new java.util.HashMap<>(cooldowns);
        }

        public long getLastCastTime(String skillId) {
            return cooldowns.getOrDefault(skillId, 0L);
        }

        public void setLastCastTime(String skillId, long time) {
            cooldowns.put(skillId, time);
        }

        /**
         * クールダウンを設定します
         *
         * @param skillId スキルID
         * @param time 最終発動時刻（エポックミリ秒）
         */
        public void setCooldown(String skillId, long time) {
            cooldowns.put(skillId, time);
        }

        public int getSkillPoints() {
            return skillPoints;
        }

        public void setSkillPoints(int points) {
            this.skillPoints = Math.max(0, points);
        }

        public void addSkillPoints(int points) {
            this.skillPoints += points;
        }

        public boolean useSkillPoint() {
            if (skillPoints > 0) {
                skillPoints--;
                return true;
            }
            return false;
        }
    }
}
