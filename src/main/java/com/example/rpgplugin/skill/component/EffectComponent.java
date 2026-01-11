package com.example.rpgplugin.skill.component;

import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

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
     * 子コンポーネントを取得します
     *
     * @return 子コンポーネントリスト
     */
    public List<EffectComponent> getChildren() {
        return children;
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
        double base = settings.getDouble(key + "-base", fallback);
        double scale = settings.getDouble(key + "-scale", 0);
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
        return settings.getDouble(key, fallback);
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "key='" + key + '\'' +
                ", type=" + getType() +
                ", children=" + children.size() +
                '}';
    }
}
