package com.example.rpgplugin.skill.executor;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

/**
 * アクティブスキル実行エグゼキューター
 *
 * <p>アクティブスキルの発動処理を行います。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: アクティブスキルの実行に専念</li>
 *   <li>Strategy: 異なるスキル効果の実行をStrategyパターンで管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ActiveSkillExecutor implements SkillExecutor {

    private final RPGPlugin plugin;
    private final SkillManager skillManager;
    private final PlayerManager playerManager;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     * @param skillManager スキルマネージャー
     * @param playerManager プレイヤーマネージャー
     */
    public ActiveSkillExecutor(RPGPlugin plugin, SkillManager skillManager, PlayerManager playerManager) {
        this.plugin = plugin;
        this.skillManager = skillManager;
        this.playerManager = playerManager;
    }

    @Override
    public boolean execute(Player player, Skill skill, int level) {
        // クールダウンチェック
        if (!skillManager.checkCooldown(player, skill.getId())) {
            return false;
        }

        // MP消費チェック（実装必要）
        // TODO: MPシステム実装後に有効化

        // ダメージ計算
        if (skill.getDamage() != null) {
            double damage = calculateDamage(player, skill, level);
            applyDamage(player, damage);
        }

        // クールダウン設定
        SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(player);
        data.setLastCastTime(skill.getId(), System.currentTimeMillis());

        // メッセージ送信
        player.sendMessage(ChatColor.GREEN + "スキルを発動しました: " + skill.getColoredDisplayName() + " Lv." + level);

        return true;
    }

    /**
     * ダメージを計算します
     *
     * @param player プレイヤー
     * @param skill スキル
     * @param level スキルレベル
     * @return 計算されたダメージ
     */
    private double calculateDamage(Player player, Skill skill, int level) {
        Skill.DamageCalculation damageConfig = skill.getDamage();
        if (damageConfig == null) {
            return 0.0;
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            return 0.0;
        }

        // ステータス値を取得
        Stat stat = damageConfig.getStatMultiplier();
        double statValue = 0.0;
        if (stat != null) {
            statValue = rpgPlayer.getStatManager().getFinalStat(stat);
        }

        // ダメージ計算
        return damageConfig.calculateDamage(statValue, level);
    }

    /**
     * ダメージを適用します
     *
     * @param player プレイヤー
     * @param damage ダメージ
     */
    private void applyDamage(Player player, double damage) {
        // ターゲット選択
        Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(
                player.getLocation(), 5, 5, 5);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // 敵対的Mobのみにダメージを与える
                if (isEnemy(livingEntity)) {
                    livingEntity.damage(damage, player);
                }
            }
        }
    }

    /**
     * エンティティが敵対的かチェックします
     *
     * @param entity エンティティ
     * @return 敵対的場合はtrue
     */
    private boolean isEnemy(LivingEntity entity) {
        // プレイヤーではない場合
        if (entity instanceof Player) {
            return false;
        }

        // その他のMobは敵対的とみなす
        // MythicMobs連携はドロップ管理機能のみ実装済み
        // 敵対判定ではMythicMobsとバニラMOBを区別しない
        return true;
    }

    /**
     * ポーション効果を付与します
     *
     * @param player プレイヤー
     * @param effectType ポーション効果タイプ
     * @param duration 効果時間（ティック）
     * @param amplifier レベル
     */
    private void applyPotionEffect(Player player, PotionEffectType effectType, int duration, int amplifier) {
        PotionEffect effect = new PotionEffect(effectType, duration, amplifier);
        player.addPotionEffect(effect);
    }

    /**
     * RPGPluginを取得します
     *
     * @return プラグインインスタンス
     */
    public RPGPlugin getPlugin() {
        return plugin;
    }

    /**
     * SkillManagerを取得します
     *
     * @return スキルマネージャー
     */
    public SkillManager getSkillManager() {
        return skillManager;
    }

    /**
     * PlayerManagerを取得します
     *
     * @return プレイヤーマネージャー
     */
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    // ==================== ターゲット指定発動 ====================

    /**
     * 指定ターゲットでスキルを発動します
     *
     * @param player プレイヤー
     * @param skill スキル
     * @param level スキルレベル
     * @param target ターゲットエンティティ
     * @return 成功した場合はtrue
     */
    public boolean executeAt(Player player, Skill skill, int level, Entity target) {
        if (player == null || skill == null || target == null) {
            return false;
        }

        // ターゲットが有効かチェック
        if (!target.isValid()) {
            player.sendMessage(ChatColor.RED + "ターゲットが無効です");
            return false;
        }

        // クールダウンチェック
        if (!skillManager.checkCooldown(player, skill.getId())) {
            return false;
        }

        // ダメージ計算と適用
        if (skill.getDamage() != null && target instanceof LivingEntity) {
            double damage = calculateDamage(player, skill, level);
            LivingEntity livingTarget = (LivingEntity) target;

            // 敵対的かチェック
            if (isEnemy(livingTarget)) {
                livingTarget.damage(damage, player);
                player.sendMessage(ChatColor.GREEN + "スキルを発動しました: " + skill.getColoredDisplayName() + " Lv." + level);
            } else {
                player.sendMessage(ChatColor.RED + "ターゲットは敵対的ではありません");
                return false;
            }
        } else {
            // ダメージなしのスキル（バフ等）
            player.sendMessage(ChatColor.GREEN + "スキルを発動しました: " + skill.getColoredDisplayName() + " Lv." + level);
        }

        // クールダウン設定
        SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(player);
        data.setLastCastTime(skill.getId(), System.currentTimeMillis());

        return true;
    }

    /**
     * 指定コストタイプでスキルを発動します
     *
     * @param player プレイヤー
     * @param skill スキル
     * @param level スキルレベル
     * @param costType コストタイプ
     * @return 成功した場合はtrue
     */
    public boolean executeWithCostType(Player player, Skill skill, int level, SkillCostType costType) {
        if (player == null || skill == null || costType == null) {
            return false;
        }

        // クールダウンチェック
        if (!skillManager.checkCooldown(player, skill.getId())) {
            return false;
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            return false;
        }

        // コスト消費チェック（指定されたコストタイプを使用）
        double cost = skill.getCost(level);
        if (cost > 0) {
            boolean canAfford = false;
            switch (costType) {
                case MANA:
                    // TODO: マナシステム実装後に有効化
                    canAfford = true; // 暫定：常にtrue
                    break;
                case HP:
                    canAfford = rpgPlayer.getBukkitPlayer() != null
                            && rpgPlayer.getBukkitPlayer().getHealth() > cost;
                    break;
            }

            if (!canAfford) {
                player.sendMessage(ChatColor.RED + costType.getDisplayName() + "が足りません");
                return false;
            }
        }

        // ダメージ計算
        if (skill.getDamage() != null) {
            double damage = calculateDamage(player, skill, level);
            applyDamage(player, damage);
        }

        // 指定コストタイプで消費
        if (cost > 0) {
            switch (costType) {
                case MANA:
                    // TODO: マナシステム実装後に有効化
                    break;
                case HP:
                    if (rpgPlayer.getBukkitPlayer() != null) {
                        rpgPlayer.getBukkitPlayer().setHealth(
                                rpgPlayer.getBukkitPlayer().getHealth() - cost);
                    }
                    break;
            }
        }

        // クールダウン設定
        SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(player);
        data.setLastCastTime(skill.getId(), System.currentTimeMillis());

        // メッセージ送信
        player.sendMessage(ChatColor.GREEN + "スキルを発動しました: " + skill.getColoredDisplayName() + " Lv." + level
                + ChatColor.GRAY + " (" + costType.getDisplayName() + "消費)");

        return true;
    }
}
