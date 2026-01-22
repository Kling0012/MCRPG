package com.example.rpgplugin.skill.component;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.evaluator.FormulaEvaluator;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * スキル効果コンポーネントの基底クラス
 * <p>SkillAPIのEffectComponentを参考に実装</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: コンポーネントの実行に専念</li>
 *   <li>O: 子コンポーネントを追加可能</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public abstract class EffectComponent {

    private static final Logger LOGGER = Logger.getLogger(EffectComponent.class.getName());
    private static final FormulaEvaluator FORMULA_EVALUATOR = new FormulaEvaluator();

    /**
     * 子コンポーネントリスト
     */
    protected final List<EffectComponent> children = new ArrayList<>();

    /**
     * コンポーネントの識別キー
     */
    protected String key;

    /**
     * コンポーネント設定
     */
    protected ComponentSettings settings = new ComponentSettings();

    /**
     * 親スキル
     */
    protected SkillEffect skill;

    /**
     * コンストラクタ
     *
     * @param key コンポーネントキー
     */
    protected EffectComponent(String key) {
        this.key = key;
    }

    /**
     * コンポーネントキーを取得します
     *
     * @return コンポーネントキー
     */
    public String getKey() {
        return key;
    }

    /**
     * コンポーネントタイプを取得します
     *
     * @return コンポーネントタイプ
     */
    public abstract ComponentType getType();

    /**
     * 設定を取得します
     *
     * @return 設定
     */
    public ComponentSettings getSettings() {
        return settings;
    }

    /**
     * スキルを設定します
     *
     * @param skill スキル
     */
    public void setSkill(SkillEffect skill) {
        this.skill = skill;
    }

    /**
     * 子コンポーネントを追加します
     *
     * @param child 子コンポーネント
     */
    public void addChild(EffectComponent child) {
        if (child != null) {
            children.add(child);
            child.setSkill(this.skill);
        }
    }

    /**
     * 子コンポーネントを取得します（不変ビュー）
     *
     * @return 子コンポーネントリストの不変ビュー
     */
    public List<EffectComponent> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * 子コンポーネントを実行します
     *
     * @param caster  発動者
     * @param level   スキルレベル
     * @param targets ターゲットリスト
     * @return 実行成功の場合はtrue
     */
    protected boolean executeChildren(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.isEmpty()) {
            return false;
        }

        boolean worked = false;
        for (EffectComponent child : children) {
            boolean passed = child.execute(caster, level, targets);
            worked = passed || worked;
        }
        return worked;
    }

    /**
     * コンポーネントを実行します
     *
     * @param caster  発動者
     * @param level   スキルレベル
     * @param targets ターゲットリスト
     * @return 実行成功の場合はtrue
     */
    public abstract boolean execute(LivingEntity caster, int level, List<LivingEntity> targets);

    /**
     * クリーンアップ処理を行います
     *
     * @param caster 発動者
     */
    public void cleanUp(LivingEntity caster) {
        for (EffectComponent child : children) {
            child.cleanUp(caster);
        }
    }

    /**
     * 数値をパースします
     * <p>レベルに応じたスケーリングを適用します</p>
     *
     * @param caster   発動者
     * @param key      設定キー
     * @param level    スキルレベル
     * @param fallback デフォルト値
     * @return パースされた数値
     */
    protected double parseValues(LivingEntity caster, String key, int level, double fallback) {
        double base = resolveNumber(caster, key + "-base", level, fallback);
        double scale = resolveNumber(caster, key + "-scale", level, 0);
        return base + (level - 1) * scale;
    }

    /**
     * 数値設定を取得します
     *
     * @param key      設定キー
     * @param fallback デフォルト値
     * @return 設定値
     */
    protected double getNum(String key, double fallback) {
        return resolveNumber(null, key, 1, fallback);
    }

    /**
     * 数値設定を取得します（式/変数対応）
     *
     * <p>ComponentSettings が数値ではなく文字列を保持している場合、数式として評価を試みます。</p>
     * <p>利用可能変数: STR/INT/SPI/VIT/DEX, Lv, LV, skill YAML の variables</p>
     */
    protected double getNum(LivingEntity caster, String key, int skillLevel, double fallback) {
        return resolveNumber(caster, key, skillLevel, fallback);
    }

    /**
     * 整数値を取得します
     *
     * @param key      設定キー
     * @param fallback デフォルト値
     * @return 設定値
     */
    protected int getInt(String key, int fallback) {
        return settings.getInt(key, fallback);
    }

    /**
     * 真偽値を取得します
     *
     * @param key      設定キー
     * @param fallback デフォルト値
     * @return 設定値
     */
    protected boolean getBool(String key, boolean fallback) {
        return settings.getBoolean(key, fallback);
    }

    /**
     * 文字列を取得します
     *
     * @param key      設定キー
     * @param fallback デフォルト値
     * @return 設定値
     */
    protected String getString(String key, String fallback) {
        return settings.getString(key, fallback);
    }

    private double resolveNumber(LivingEntity caster, String key, int skillLevel, double fallback) {
        if (settings == null) {
            return fallback;
        }
        Object raw = settings.getRaw(key);
        if (raw == null) {
            return fallback;
        }
        if (raw instanceof Number) {
            return ((Number) raw).doubleValue();
        }

        String str = raw.toString().trim();
        if (str.isEmpty()) {
            return fallback;
        }

        // Fast path: plain numeric string
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException ignored) {
            // try formula evaluation
        }

        try {
            RPGPlayer rpgPlayer = null;
            RPGPlugin plugin = RPGPlugin.getInstance();
            if (plugin != null && caster instanceof Player) {
                PlayerManager pm = plugin.getPlayerManager();
                if (pm != null) {
                    rpgPlayer = pm.getRPGPlayer(((Player) caster).getUniqueId());
                }
            }

            Map<String, Double> variables = new HashMap<>();
            if (plugin != null && skill != null) {
                Skill skillObj = plugin.getSkillManager().getSkill(skill.getSkillId());
                if (skillObj != null) {
                    variables.putAll(skillObj.getVariableMap());
                }
            }

            return FORMULA_EVALUATOR.evaluate(str, rpgPlayer, skillLevel, variables);
        } catch (FormulaEvaluator.FormulaEvaluationException e) {
            LOGGER.log(Level.WARNING, "[EffectComponent] 数式評価エラー: key=" + key + ", formula=" + str + ", error=" + e.getMessage(), e);
            return fallback;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[EffectComponent] 予期しないエラー: key=" + key + ", formula=" + str, e);
            return fallback;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "key='" + key + '\'' +
                ", type=" + getType() +
                ", children=" + children.size() +
                '}';
    }
}
