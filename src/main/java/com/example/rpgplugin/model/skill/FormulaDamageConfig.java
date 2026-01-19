package com.example.rpgplugin.model.skill;

import java.util.HashMap;
import java.util.Map;

/**
 * 数式ダメージ設定
 *
 * <p>数式を使用したダメージ計算設定を表します。</p>
 *
 * <p>YAML例:</p>
 * <pre>
 * damage:
 *   formula: "STR * str_scale + (Lv * 5) + base_mod * 10"
 * </pre>
 */
public class FormulaDamageConfig {
    private final String formula;
    private final Map<Integer, String> levelFormulas;

    public FormulaDamageConfig(String formula, Map<Integer, String> levelFormulas) {
        this.formula = formula;
        this.levelFormulas = levelFormulas != null ? levelFormulas : new HashMap<>();
    }

    public String getFormula() {
        return formula;
    }

    public Map<Integer, String> getLevelFormulas() {
        return new HashMap<>(levelFormulas);
    }

    /**
     * 指定レベルの数式を取得します
     *
     * @param level スキルレベル
     * @return 数式、レベル別定義がない場合は基本数式
     */
    public String getFormula(int level) {
        return levelFormulas.getOrDefault(level, formula);
    }

    /**
     * レベル別数式が定義されているかチェックします
     *
     * @return レベル別数式が存在する場合はtrue
     */
    public boolean hasLevelFormulas() {
        return !levelFormulas.isEmpty();
    }
}
