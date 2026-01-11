package com.example.rpgplugin.skill.executor;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.skript.events.EvtRPGSkillCast.RPGSkillCastEvent;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.target.SkillTarget;
import com.example.rpgplugin.skill.target.TargetSelector;
import com.example.rpgplugin.stats.Stat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

/**
 * アクティブスキル実行エグゼキューター
 *
 * <p>アクティブスキルの発動処理を行います。</p>
 *
 * <p>効果がないスキル（発動のみ）も許可します。</p>
 * <ul>
 *   <li>コスト消費: MP/HPを消費</li>
 *   <li>クールダウン: クールダウンを設定</li>
 *   <li>メッセージ: 発動成功メッセージを表示</li>
 * </ul>
 * <p>これは以下の用途で使用できます:</p>
 * <ul>
 *   <li>プレースホルダーとしてのスキル</li>
 *   <li>他システム（クエスト等）から発火されるスキル</li>
 *   <li>コスメティックなスキル</li>
 * </ul>
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
    private final com.example.rpgplugin.skill.component.ComponentEffectExecutor componentExecutor;

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
        this.componentExecutor = new com.example.rpgplugin.skill.component.ComponentEffectExecutor(plugin);
    }

    @Override
    public boolean execute(Player player, Skill skill, int level) {
        // クールダウンチェック
        if (!skillManager.checkCooldown(player, skill.getId())) {
            return false;
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            return false;
        }

        // コスト消費チェック（コンポーネントベース）
        int cost = skill.getCostFromComponents(level);
        if (cost > 0) {
            if (!rpgPlayer.consumeSkillCost(cost)) {
                if (rpgPlayer.isManaCostType()) {
                    player.sendMessage(Component.text("MPが足りません", NamedTextColor.RED));
                } else {
                    player.sendMessage(Component.text("HPが足りません", NamedTextColor.RED));
                }
                return false;
            }
        }

        // ターゲットを取得
        Collection<LivingEntity> targets = getTargets(player, skill);

        // ダメージ計算と適用（コンポーネントベース）
        double damage = 0.0;
        Entity firstTarget = null;
        com.example.rpgplugin.skill.component.EffectComponent damageComponent =
                skill.findComponentByKey("damage");
        if (damageComponent != null) {
            damage = calculateDamageFromComponents(rpgPlayer, skill, level, damageComponent);

            for (LivingEntity target : targets) {
                if (isEnemy(target)) {
                    target.damage(damage, player);
                    if (firstTarget == null) {
                        firstTarget = target;
                    }
                }
            }
        }

        // Skriptイベント発火
        try {
            org.bukkit.plugin.PluginManager pm = Bukkit.getPluginManager();
            if (pm != null) {
                pm.callEvent(new RPGSkillCastEvent(player, skill.getId(), skill, level, firstTarget, damage));
            }
        } catch (Exception ignored) {
            // テスト環境などでPluginManagerが利用できない場合は無視
        }

        // クールダウン設定
        SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(player);
        data.setLastCastTime(skill.getId(), System.currentTimeMillis());

        // コンポーネント効果を実行（トリガー発火）
        if (skill.getComponentEffect() != null) {
            componentExecutor.castWithTriggers(player, skill, level, 0);
        }

        return true;
    }

    /**
     * ターゲットを取得します
     *
     * <p>コンポーネントベースのターゲット設定を使用します。</p>
     *
     * @param player プレイヤー
     * @param skill スキル
     * @return ターゲットエンティティのコレクション
     */
    private Collection<LivingEntity> getTargets(Player player, Skill skill) {
        SkillTarget skillTarget = skill.getTargetFromComponents();

        if (skillTarget != null) {
            List<Entity> candidates = TargetSelector.getNearbyEntities(
                    player.getLocation(), skillTarget.getRange());
            List<Entity> selected = TargetSelector.selectTargets(player, skillTarget, candidates, null);

            Collection<LivingEntity> targets = new java.util.ArrayList<>();
            for (Entity entity : selected) {
                if (entity instanceof LivingEntity) {
                    targets.add((LivingEntity) entity);
                }
            }
            return targets;
        }

        // デフォルト: プレイヤーのみ
        Collection<LivingEntity> targets = new java.util.ArrayList<>();
        targets.add(player);
        return targets;
    }

    /**
     * コンポーネントからダメージを計算します
     *
     * <p>ダメージコンポーネントの設定からダメージ値を計算します。</p>
     *
     * @param rpgPlayer RPGプレイヤー
     * @param skill スキル
     * @param level スキルレベル
     * @param damageComponent ダメージコンポーネント
     * @return 計算されたダメージ
     */
    private double calculateDamageFromComponents(RPGPlayer rpgPlayer, Skill skill, int level,
                                                   com.example.rpgplugin.skill.component.EffectComponent damageComponent) {
        if (damageComponent == null || damageComponent.getSettings() == null) {
            return 0.0;
        }

        com.example.rpgplugin.skill.component.ComponentSettings settings = damageComponent.getSettings();

        // valueパラメータを取得
        double baseDamage = 0.0;
        if (settings.has("value")) {
            String valueStr = settings.getString("value", "0");

            // 数式をパース（レベル変数を置換）
            try {
                String formula = valueStr.replace("level", String.valueOf(level))
                                         .replace("Lv", String.valueOf(level));

                // ステータス変数も置換（例: strength, intelligenceなど）
                if (formula.contains("strength") || formula.contains("intel")) {
                    formula = replaceStatVariables(formula, rpgPlayer);
                }

                // 簡易数式評価
                baseDamage = parseFormula(formula);
            } catch (Exception e) {
                baseDamage = settings.getDouble("value", 0.0);
            }
        } else {
            baseDamage = settings.getDouble("value", 0.0);
        }

        // stat_multiplierがある場合はステータス倍率を適用
        if (settings.has("stat_multiplier")) {
            String statName = settings.getString("stat_multiplier", "");
            double multiplier = settings.getDouble("multiplier", 1.0);

            try {
                Stat stat = Stat.fromShortName(statName);
                if (stat == null) {
                    stat = Stat.fromDisplayName(statName);
                }

                if (stat != null) {
                    double statValue = rpgPlayer.getStatManager().getFinalStat(stat);
                    baseDamage += statValue * multiplier;
                }
            } catch (Exception e) {
                // ステータス解釈エラーは無視
            }
        }

        // level_multiplierがある場合はレベル倍率を適用
        if (settings.has("level_multiplier")) {
            double levelMultiplier = settings.getDouble("level_multiplier", 0.0);
            baseDamage += level * levelMultiplier;
        }

        return baseDamage;
    }

    /**
     * 数式内のステータス変数を置換します
     *
     * @param formula 数式
     * @param rpgPlayer RPGプレイヤー
     * @return 置換後の数式
     */
    private String replaceStatVariables(String formula, RPGPlayer rpgPlayer) {
        // ステータス名の置換マップ
        java.util.Map<String, Stat> statMap = new java.util.HashMap<>();
        statMap.put("strength", Stat.STRENGTH);
        statMap.put("str", Stat.STRENGTH);
        statMap.put("intelligence", Stat.INTELLIGENCE);
        statMap.put("intel", Stat.INTELLIGENCE);
        statMap.put("int", Stat.INTELLIGENCE);
        statMap.put("spirit", Stat.SPIRIT);
        statMap.put("spr", Stat.SPIRIT);
        statMap.put("dexterity", Stat.DEXTERITY);
        statMap.put("dex", Stat.DEXTERITY);
        statMap.put("vitality", Stat.VITALITY);
        statMap.put("vit", Stat.VITALITY);

        String result = formula;
        for (java.util.Map.Entry<String, Stat> entry : statMap.entrySet()) {
            String placeholder = entry.getKey();
            Stat stat = entry.getValue();
            double statValue = rpgPlayer.getStatManager().getFinalStat(stat);
            result = result.replaceAll("\\b" + placeholder + "\\b", String.valueOf((int) statValue));
        }

        return result;
    }

    /**
     * 簡易数式を解析します
     *
     * @param formula 数式
     * @return 計算結果
     */
    private double parseFormula(String formula) {
        try {
            // 簡易パーサー: 演算子の優先順位を考慮
            return new Object() {
                int pos = -1, ch;

                void nextChar() {
                    ch = (++pos < formula.length()) ? formula.charAt(pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (ch == ' ') nextChar();
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }

                double parse() {
                    nextChar();
                    double x = parseExpression();
                    if (pos < formula.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                    return x;
                }

                double parseExpression() {
                    double x = parseTerm();
                    for (;;) {
                        if (eat('+')) x += parseTerm();
                        else if (eat('-')) x -= parseTerm();
                        else return x;
                    }
                }

                double parseTerm() {
                    double x = parseFactor();
                    for (;;) {
                        if (eat('*')) x *= parseFactor();
                        else if (eat('/')) x /= parseFactor();
                        else return x;
                    }
                }

                double parseFactor() {
                    if (eat('+')) return parseFactor();
                    if (eat('-')) return -parseFactor();

                    double x;
                    int startPos = this.pos;
                    if (eat('(')) {
                        x = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        x = Double.parseDouble(formula.substring(startPos, this.pos));
                    } else {
                        throw new RuntimeException("Unexpected: " + (char) ch);
                    }

                    return x;
                }
            }.parse();
        } catch (Exception e) {
            // パース失敗時は数値として試みる
            try {
                return Double.parseDouble(formula);
            } catch (NumberFormatException ex) {
                return 0.0;
            }
        }
    }

    /**
     * ダメージを計算します
     *
     * @param rpgPlayer RPGプレイヤー
     * @param skill スキル
     * @param level スキルレベル
     * @return 計算されたダメージ
     */
    private double calculateDamage(RPGPlayer rpgPlayer, Skill skill, int level) {
        Skill.DamageCalculation damageConfig = skill.getDamage();
        if (damageConfig == null) {
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
     * エンティティが敵対的かチェックします
     *
     * @param entity エンティティ
     * @return 敵対的の場合はtrue
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

        // RPGプレイヤー取得
        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            return false;
        }

        // ターゲットが有効かチェック
        if (!target.isValid()) {
            player.sendMessage(Component.text("ターゲットが無効です", NamedTextColor.RED));
            return false;
        }

        // クールダウンチェック
        if (!skillManager.checkCooldown(player, skill.getId())) {
            return false;
        }

        // ダメージ計算
        double damage = 0.0;
        if (skill.getDamage() != null && target instanceof LivingEntity) {
            damage = calculateDamage(rpgPlayer, skill, level);
            LivingEntity livingTarget = (LivingEntity) target;

            // 敵対的かチェック
            if (isEnemy(livingTarget)) {
                livingTarget.damage(damage, player);
            } else {
                player.sendMessage(Component.text("ターゲットは敵対的ではありません", NamedTextColor.RED));
                return false;
            }
        }

        // Skriptイベント発火
        try {
            org.bukkit.plugin.PluginManager pm = Bukkit.getPluginManager();
            if (pm != null) {
                pm.callEvent(new RPGSkillCastEvent(player, skill.getId(), skill, level, target, damage));
            }
        } catch (Exception ignored) {
            // テスト環境などでPluginManagerが利用できない場合は無視
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
                    canAfford = rpgPlayer.hasMana((int) cost);
                    break;
                case HP:
                    canAfford = rpgPlayer.getBukkitPlayer() != null
                            && rpgPlayer.getBukkitPlayer().getHealth() > cost;
                    break;
            }

            if (!canAfford) {
                player.sendMessage(Component.text(costType.getDisplayName() + "が足りません", NamedTextColor.RED));
                return false;
            }
        }

        // ターゲットを取得とダメージ適用
        Collection<LivingEntity> targets = getTargets(player, skill);

        // ダメージ計算と適用
        if (skill.getDamage() != null) {
            double damage = calculateDamage(rpgPlayer, skill, level);

            for (LivingEntity target : targets) {
                if (isEnemy(target)) {
                    target.damage(damage, player);
                }
            }
        }

        // 指定コストタイプで消費
        if (cost > 0) {
            boolean consumed = false;
            switch (costType) {
                case MANA:
                    consumed = rpgPlayer.consumeMana((int) cost);
                    break;
                case HP:
                    if (rpgPlayer.getBukkitPlayer() != null) {
                        rpgPlayer.getBukkitPlayer().setHealth(
                                rpgPlayer.getBukkitPlayer().getHealth() - cost);
                        consumed = true;
                    }
                    break;
            }

            // 消費失敗時は処理を中断
            if (!consumed && costType == SkillCostType.MANA) {
                return false;
            }
        }

        // クールダウン設定
        SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(player);
        data.setLastCastTime(skill.getId(), System.currentTimeMillis());

        return true;
    }
}
