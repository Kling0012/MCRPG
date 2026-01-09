package com.example.rpgplugin.skill.executor;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatModifier;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * パッシブスキル実行エグゼキューター
 *
 * <p>パッシブスキルの常時効果適用、条件付き発動、自動更新を行います。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: パッシブスキルの実行に専念</li>
 *   <li>Strategy: 異なるパッシブ効果の適用をStrategyパターンで管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class PassiveSkillExecutor implements SkillExecutor {

    private final RPGPlugin plugin;
    private final SkillManager skillManager;
    private final PlayerManager playerManager;
    private final Map<UUID, Map<String, PassiveEffect>> activeEffects;

    /**
     * パッシブ効果
     */
    public static class PassiveEffect {
        private final Skill skill;
        private final int level;
        private final StatModifier modifier;
        private final PotionEffect potionEffect;

        public PassiveEffect(Skill skill, int level, StatModifier modifier, PotionEffect potionEffect) {
            if (skill == null) {
                throw new IllegalArgumentException("skill cannot be null");
            }
            this.skill = skill;
            this.level = level;
            this.modifier = modifier;
            this.potionEffect = potionEffect;
        }

        public Skill getSkill() {
            return skill;
        }

        public int getLevel() {
            return level;
        }

        public StatModifier getModifier() {
            return modifier;
        }

        public PotionEffect getPotionEffect() {
            return potionEffect;
        }
    }

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     * @param skillManager スキルマネージャー
     * @param playerManager プレイヤーマネージャー
     * @throws IllegalArgumentException 引数がnullの場合
     */
    public PassiveSkillExecutor(RPGPlugin plugin, SkillManager skillManager, PlayerManager playerManager) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin cannot be null");
        }
        if (skillManager == null) {
            throw new IllegalArgumentException("skillManager cannot be null");
        }
        if (playerManager == null) {
            throw new IllegalArgumentException("playerManager cannot be null");
        }
        this.plugin = plugin;
        this.skillManager = skillManager;
        this.playerManager = playerManager;
        this.activeEffects = new HashMap<>();

        // 定期的な更新タスクを開始
        startUpdateTask();
    }

    @Override
    public boolean execute(Player player, Skill skill, int level) {
        // パッシブスキルは手動発動しない
        player.sendMessage(ChatColor.RED + "パッシブスキルは手動発動できません: " + skill.getColoredDisplayName());
        return false;
    }

    /**
     * パッシブスキルを適用します
     *
     * @param player プレイヤー
     * @param skill スキル
     * @param level スキルレベル
     * @return 成功した場合はtrue
     */
    public boolean applyPassive(Player player, Skill skill, int level) {
        UUID uuid = player.getUniqueId();
        Map<String, PassiveEffect> playerEffects = activeEffects.computeIfAbsent(uuid, k -> new HashMap<>());

        // 既に適用されている場合は何もしない
        if (playerEffects.containsKey(skill.getId())) {
            return false;
        }

        // ステータス補正値を計算
        StatModifier modifier = null;
        if (skill.getDamage() != null) {
            Stat stat = skill.getDamage().getStatMultiplier();
            if (stat != null) {
                double bonus = skill.getDamage().calculateDamage(0, level);
                modifier = new StatModifier(skill.getId(), StatModifier.Type.FLAT, bonus);
            }
        }

        // ポーション効果は実装しない（パッシブスキルはステータス補正に特化）
        // 理由: .spec-workflow/steering/exclusions.md を参照
        PotionEffect potionEffect = null;

        PassiveEffect effect = new PassiveEffect(skill, level, modifier, potionEffect);
        playerEffects.put(skill.getId(), effect);

        // ステータス補正を適用
        if (modifier != null) {
            RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
            if (rpgPlayer != null) {
                // 全ステータスに修正値を適用
                for (Stat stat : Stat.values()) {
                    rpgPlayer.getStatManager().addModifier(stat, modifier);
                }
            }
        }

        // ポーション効果を適用
        if (potionEffect != null) {
            player.addPotionEffect(potionEffect);
        }

        player.sendMessage(ChatColor.GREEN + "パッシブスキルを適用しました: " + skill.getColoredDisplayName() + " Lv." + level);
        return true;
    }

    /**
     * パッシブスキルを削除します
     *
     * @param player プレイヤー
     * @param skillId スキルID
     */
    public void removePassive(Player player, String skillId) {
        UUID uuid = player.getUniqueId();
        Map<String, PassiveEffect> playerEffects = activeEffects.get(uuid);

        if (playerEffects == null || !playerEffects.containsKey(skillId)) {
            return;
        }

        PassiveEffect effect = playerEffects.remove(skillId);

        // ステータス補正を削除
        if (effect.getModifier() != null) {
            RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
            if (rpgPlayer != null) {
                // 全ステータスの修正値を削除
                for (Stat stat : Stat.values()) {
                    rpgPlayer.getStatManager().removeModifiersBySource(stat, effect.getSkill().getId());
                }
            }
        }

        // ポーション効果を削除
        if (effect.getPotionEffect() != null) {
            player.removePotionEffect(effect.getPotionEffect().getType());
        }

        player.sendMessage(ChatColor.YELLOW + "パッシブスキルを削除しました: " + effect.getSkill().getColoredDisplayName());
    }

    /**
     * プレイヤーの全パッシブスキルを更新します
     *
     * @param player プレイヤー
     */
    public void updatePassives(Player player) {
        UUID uuid = player.getUniqueId();
        SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(uuid);

        if (data == null) {
            return;
        }

        // 習得している全パッシブスキルを適用
        for (Map.Entry<String, Integer> entry : data.getAcquiredSkills().entrySet()) {
            String skillId = entry.getKey();
            int level = entry.getValue();

            Skill skill = skillManager.getSkill(skillId);
            if (skill != null && skill.isPassive() && skill.getId() != null) {
                applyPassive(player, skill, level);
            }
        }
    }

    /**
     * 定期的な更新タスクを開始します
     */
    private void startUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // 全プレイヤーのパッシブスキルを更新
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    updatePassives(player);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // 1秒ごとに更新
    }

    /**
     * プレイヤーの全パッシブ効果をクリアします
     *
     * @param player プレイヤー
     */
    public void clearAllPassives(Player player) {
        UUID uuid = player.getUniqueId();
        Map<String, PassiveEffect> playerEffects = activeEffects.get(uuid);

        if (playerEffects == null) {
            return;
        }

        // 全パッシブ効果を削除
        for (String skillId : new HashMap<>(playerEffects).keySet()) {
            removePassive(player, skillId);
        }

        activeEffects.remove(uuid);
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

    /**
     * アクティブなパッシブ効果を取得します
     *
     * @return アクティブなパッシブ効果のマップ（コピー）
     */
    public Map<UUID, Map<String, PassiveEffect>> getActiveEffects() {
        Map<UUID, Map<String, PassiveEffect>> copy = new HashMap<>();
        for (Map.Entry<UUID, Map<String, PassiveEffect>> entry : activeEffects.entrySet()) {
            copy.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
        return copy;
    }
}
