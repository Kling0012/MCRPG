package com.example.rpgplugin.skill;

import com.example.rpgplugin.RPGPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * スキル管理クラス
 *
 * <p>全スキルの登録・取得、プレイヤーの習得スキル管理を行います。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキル管理に専念</li>
 *   <li>DRY: スキルアクセスロジックを一元管理</li>
 *   <li>Strategy: 異なるスキルタイプの実行をStrategyパターンで管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillManager {

    private final RPGPlugin plugin;
    private final Map<String, Skill> skills;
    private final Map<UUID, PlayerSkillData> playerSkills;

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
            return new HashMap<>(acquiredSkills);
        }

        public int getSkillLevel(String skillId) {
            return acquiredSkills.getOrDefault(skillId, 0);
        }

        public boolean hasSkill(String skillId) {
            return acquiredSkills.containsKey(skillId) && acquiredSkills.get(skillId) > 0;
        }

        public void setSkillLevel(String skillId, int level) {
            if (level <= 0) {
                acquiredSkills.remove(skillId);
            } else {
                acquiredSkills.put(skillId, level);
            }
        }

        public Map<String, Long> getCooldowns() {
            return new HashMap<>(cooldowns);
        }

        public long getLastCastTime(String skillId) {
            return cooldowns.getOrDefault(skillId, 0L);
        }

        public void setLastCastTime(String skillId, long time) {
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

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public SkillManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.skills = new ConcurrentHashMap<>();
        this.playerSkills = new ConcurrentHashMap<>();
    }

    /**
     * スキルを登録します
     *
     * @param skill 登録するスキル
     * @return 重複がある場合はfalse
     */
    public boolean registerSkill(Skill skill) {
        if (skills.containsKey(skill.getId())) {
            plugin.getLogger().warning("Skill already registered: " + skill.getId());
            return false;
        }
        skills.put(skill.getId(), skill);
        plugin.getLogger().info("Skill registered: " + skill.getId());
        return true;
    }

    /**
     * スキルを取得します
     *
     * @param skillId スキルID
     * @return スキル、見つからない場合はnull
     */
    public Skill getSkill(String skillId) {
        return skills.get(skillId);
    }

    /**
     * 全スキルを取得します
     *
     * @return 全スキルのマップ（コピー）
     */
    public Map<String, Skill> getAllSkills() {
        return new HashMap<>(skills);
    }

    /**
     * 指定されたクラスで使用可能なスキルを取得します
     *
     * @param classId クラスID
     * @return 使用可能なスキルリスト
     */
    public List<Skill> getSkillsForClass(String classId) {
        List<Skill> result = new ArrayList<>();
        for (Skill skill : skills.values()) {
            if (skill.isAvailableForClass(classId)) {
                result.add(skill);
            }
        }
        return result;
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
     * スキルを習得させます
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @param level レベル
     * @return 成功した場合はtrue
     */
    public boolean acquireSkill(Player player, String skillId, int level) {
        Skill skill = getSkill(skillId);
        if (skill == null) {
            player.sendMessage(ChatColor.RED + "スキルが見つかりません: " + skillId);
            return false;
        }

        PlayerSkillData data = getPlayerSkillData(player);
        int currentLevel = data.getSkillLevel(skillId);

        if (currentLevel > 0 && level <= currentLevel) {
            player.sendMessage(ChatColor.RED + "既に higher level を習得しています");
            return false;
        }

        if (level > skill.getMaxLevel()) {
            player.sendMessage(ChatColor.RED + "最大レベルを超えています: " + skill.getMaxLevel());
            return false;
        }

        data.setSkillLevel(skillId, level);
        player.sendMessage(ChatColor.GREEN + "スキルを習得しました: " + skill.getColoredDisplayName() + " Lv." + level);
        return true;
    }

    /**
     * スキルレベルを上げます
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return 成功した場合はtrue
     */
    public boolean upgradeSkill(Player player, String skillId) {
        Skill skill = getSkill(skillId);
        if (skill == null) {
            player.sendMessage(ChatColor.RED + "スキルが見つかりません: " + skillId);
            return false;
        }

        PlayerSkillData data = getPlayerSkillData(player);
        int currentLevel = data.getSkillLevel(skillId);

        if (currentLevel == 0) {
            player.sendMessage(ChatColor.RED + "まずスキルを習得してください");
            return false;
        }

        if (currentLevel >= skill.getMaxLevel()) {
            player.sendMessage(ChatColor.RED + "既に最大レベルに達しています");
            return false;
        }

        data.setSkillLevel(skillId, currentLevel + 1);
        player.sendMessage(ChatColor.GREEN + "スキルを強化しました: " + skill.getColoredDisplayName() + " Lv." + (currentLevel + 1));
        return true;
    }

    /**
     * クールダウンをチェックします
     *
     * @param player プレイヤー
     * @param skillId スキルID
     * @return クールダウン中でない場合はtrue
     */
    public boolean checkCooldown(Player player, String skillId) {
        Skill skill = getSkill(skillId);
        if (skill == null) {
            return false;
        }

        PlayerSkillData data = getPlayerSkillData(player);
        long lastCast = data.getLastCastTime(skillId);
        long currentTime = System.currentTimeMillis();
        long cooldownMs = (long) (skill.getCooldown() * 1000);

        if (currentTime - lastCast < cooldownMs) {
            long remainingSeconds = ((cooldownMs - (currentTime - lastCast)) / 1000) + 1;
            player.sendMessage(ChatColor.RED + "クールダウン中です: 残り " + remainingSeconds + " 秒");
            return false;
        }

        return true;
    }

    /**
     * プレイヤーデータをアンロードします
     *
     * @param uuid プレイヤーUUID
     */
    public void unloadPlayerData(UUID uuid) {
        playerSkills.remove(uuid);
    }

    /**
     * 全スキルデータをクリアします
     */
    public void clearAllSkills() {
        skills.clear();
    }

    /**
     * 全プレイヤーデータをクリアします
     */
    public void clearAllPlayerData() {
        playerSkills.clear();
    }
}
