package com.example.rpgplugin.api;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.currency.CurrencyManager;
import com.example.rpgplugin.damage.DamageModifier;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.rpgclass.ClassManager;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.executor.ActiveSkillExecutor;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * RPGPlugin API 実装クラス
 *
 * <p>RPGPluginAPIインターフェースの実装を提供します。</p>
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>ファサードパターン: 複雑なシステムへのシンプルなアクセスを提供</li>
 *   <li>シングルトンパターン: プラグイン全体で1つのインスタンス</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-D: 実装の詳細に依存しない</li>
 *   <li>DRY: 共通処理を一元管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class RPGPluginAPIImpl implements RPGPluginAPI {

    private final RPGPlugin plugin;
    private final PlayerManager playerManager;
    private final ClassManager classManager;
    private final SkillManager skillManager;
    private final ActiveSkillExecutor activeSkillExecutor;
    private final CurrencyManager currencyManager;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public RPGPluginAPIImpl(RPGPlugin plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
        this.classManager = plugin.getClassManager();
        this.skillManager = plugin.getSkillManager();
        this.activeSkillExecutor = plugin.getActiveSkillExecutor();
        this.currencyManager = plugin.getCurrencyManager();
    }

    // ==================== プレイヤーデータ取得 ====================

    @Override
    public Optional<RPGPlayer> getRPGPlayer(Player player) {
        if (player == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(playerManager.getRPGPlayer(player.getUniqueId()));
    }

    @Override
    public int getLevel(Player player) {
        if (player == null) {
            return 0;
        }
        return player.getLevel();
    }

    @Override
    public void setLevel(Player player, int level) {
        if (player == null) {
            return;
        }
        player.setLevel(level);
    }

    @Override
    public int getStat(Player player, Stat stat) {
        if (player == null || stat == null) {
            return 0;
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            return 0;
        }

        return rpgPlayer.getFinalStat(stat);
    }

    @Override
    public String getClassId(Player player) {
        if (player == null) {
            return null;
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            return null;
        }

        return rpgPlayer.getClassId();
    }

    // ==================== ステータス操作 ====================

    @Override
    public void setStat(Player player, Stat stat, int baseValue) {
        if (player == null || stat == null) {
            return;
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            return;
        }

        rpgPlayer.setBaseStat(stat, baseValue);
    }

    @Override
    public void addStatPoints(Player player, int points) {
        if (player == null || points <= 0) {
            return;
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            return;
        }

        rpgPlayer.addAvailablePoints(points);
    }

    // ==================== クラス操作 ====================

    @Override
    public boolean setClass(Player player, String classId) {
        if (player == null || classId == null || classId.isEmpty()) {
            return false;
        }

        return classManager.setPlayerClass(player, classId);
    }

    @Override
    public boolean upgradeClassRank(Player player) {
        if (player == null) {
            return false;
        }

        String currentClassId = getClassId(player);
        if (currentClassId == null) {
            return false;
        }

        // 現在のクラスを取得
        var currentClassOpt = classManager.getClass(currentClassId);
        if (!currentClassOpt.isPresent()) {
            return false;
        }

        var currentClass = currentClassOpt.get();

        // 次のランククラスIDを取得
        var nextRankClassIdOpt = currentClass.getNextRankClassId();
        if (!nextRankClassIdOpt.isPresent()) {
            return false;
        }

        String nextRankClassId = nextRankClassIdOpt.get();

        // ランクアップ可能かチェック
        var result = classManager.canUpgradeClass(player, nextRankClassId);
        if (!result.isSuccess()) {
            return false;
        }

        // クラス設定
        return setClass(player, nextRankClassId);
    }

    @Override
    public boolean canUpgradeClass(Player player) {
        if (player == null) {
            return false;
        }

        String currentClassId = getClassId(player);
        if (currentClassId == null) {
            return false;
        }

        // 現在のクラスを取得
        var currentClassOpt = classManager.getClass(currentClassId);
        if (!currentClassOpt.isPresent()) {
            return false;
        }

        var currentClass = currentClassOpt.get();

        // 次のランククラスIDを取得
        var nextRankClassIdOpt = currentClass.getNextRankClassId();
        if (!nextRankClassIdOpt.isPresent()) {
            return false;
        }

        // ランクアップ可能かチェック
        var result = classManager.canUpgradeClass(player, nextRankClassIdOpt.get());
        return result.isSuccess();
    }

    // ==================== スキル操作 ====================

    @Override
    public boolean hasSkill(Player player, String skillId) {
        if (player == null || skillId == null || skillId.isEmpty()) {
            return false;
        }

        return skillManager.hasSkill(player, skillId);
    }

    @Override
    public boolean unlockSkill(Player player, String skillId) {
        if (player == null || skillId == null || skillId.isEmpty()) {
            return false;
        }

        // デフォルトでレベル1で習得
        return skillManager.acquireSkill(player, skillId, 1);
    }

    @Override
    public boolean castSkill(Player player, String skillId) {
        if (player == null || skillId == null || skillId.isEmpty()) {
            return false;
        }

        // スキル習得チェック
        if (!hasSkill(player, skillId)) {
            player.sendMessage("§cスキルを習得していません: " + skillId);
            return false;
        }

        // スキル取得
        Skill skill = skillManager.getSkill(skillId);
        if (skill == null) {
            player.sendMessage("§cスキルが見つかりません: " + skillId);
            return false;
        }

        // アクティブスキルのみ実行可能
        if (skill.getType() != com.example.rpgplugin.skill.SkillType.ACTIVE) {
            player.sendMessage("§cこのスキルはアクティブスキルではありません: " + skillId);
            return false;
        }

        // クールダウンチェック
        if (!skillManager.checkCooldown(player, skillId)) {
            return false;
        }

        // スキルレベルを取得
        int level = skillManager.getSkillLevel(player, skillId);

        // スキル実行
        return activeSkillExecutor.execute(player, skill, level);
    }

    @Override
    public int getSkillLevel(Player player, String skillId) {
        if (player == null || skillId == null || skillId.isEmpty()) {
            return 0;
        }

        return skillManager.getSkillLevel(player, skillId);
    }

    @Override
    public java.util.Map<String, Integer> getAcquiredSkills(Player player) {
        if (player == null) {
            return new java.util.HashMap<>();
        }

        var data = skillManager.getPlayerSkillData(player);
        return data.getAcquiredSkills();
    }

    @Override
    public List<Skill> getSkillsForClass(String classId) {
        if (classId == null || classId.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        return skillManager.getSkillsForClass(classId);
    }

    // ==================== 経済操作 ====================

    @Override
    public double getGoldBalance(Player player) {
        if (player == null) {
            return 0.0;
        }

        return currencyManager.getGoldBalance(player);
    }

    @Override
    public boolean depositGold(Player player, double amount) {
        if (player == null || amount <= 0) {
            return false;
        }

        return currencyManager.depositGold(player, amount);
    }

    @Override
    public boolean withdrawGold(Player player, double amount) {
        if (player == null || amount <= 0) {
            return false;
        }

        return currencyManager.withdrawGold(player, amount);
    }

    @Override
    public boolean hasEnoughGold(Player player, double amount) {
        if (player == null || amount < 0) {
            return false;
        }

        return currencyManager.hasEnoughGold(player, amount);
    }

    @Override
    public boolean transferGold(Player from, Player to, double amount) {
        if (from == null || to == null || amount <= 0) {
            return false;
        }

        return currencyManager.transferGold(from, to, amount);
    }

    // ==================== ダメージ計算 ====================

    @Override
    public double calculateDamage(Player attacker, Entity target) {
        if (attacker == null || target == null) {
            return 0.0;
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(attacker.getUniqueId());
        if (rpgPlayer == null) {
            return 1.0; // デフォルトダメージ
        }

        Map<Stat, Integer> stats = rpgPlayer.getStatManager().getAllFinalStats();
        int strength = stats.getOrDefault(Stat.STRENGTH, 0);
        int intelligence = stats.getOrDefault(Stat.INTELLIGENCE, 0);

        // 基本ダメージをステータスに基づいて計算
        double baseDamage = 1.0;
        double physicalDamage = DamageModifier.calculatePhysicalDamage(baseDamage, strength, 1.0, false, 1.5);
        double magicDamage = DamageModifier.calculateMagicDamage(baseDamage, intelligence, 1.0);

        // 物理と魔法の高い方を使用
        return Math.max(physicalDamage, magicDamage);
    }

    @Override
    public double applyStatModifiers(Player player, double baseDamage, Stat stat) {
        if (player == null || stat == null) {
            return baseDamage;
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            return baseDamage;
        }

        Map<Stat, Integer> stats = rpgPlayer.getStatManager().getAllFinalStats();
        int statValue = stats.getOrDefault(stat, 0);

        // ステータスに基づいてダメージを補正
        switch (stat) {
            case STRENGTH:
                return DamageModifier.calculatePhysicalDamage(baseDamage, statValue, 1.0, false, 1.5);
            case INTELLIGENCE:
                return DamageModifier.calculateMagicDamage(baseDamage, statValue, 1.0);
            default:
                return baseDamage;
        }
    }
}
