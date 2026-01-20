package com.example.rpgplugin.model.skill;

/**
 * カスタム変数定義
 *
 * <p>YAMLのvariablesセクションで定義されるカスタム変数を表します。</p>
 *
 * <p>YAML例:</p>
 * <pre>
 * variables:
 *   base_mod: 1.0
 *   str_scale: 1.5
 * </pre>
 */
public class VariableDefinition {
    private final String name;
    private final double value;

    public VariableDefinition(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }
}
