package com.example.rpgplugin.model.skill;

import com.example.rpgplugin.stats.Stat;

/**
 * 習得要件
 *
 * <p>スキル習得に必要なステータス要件を表します。</p>
 */
public class UnlockRequirement {
    private final String type;
    private final Stat stat;
    private final double value;

    public UnlockRequirement(String type, Stat stat, double value) {
        this.type = type;
        this.stat = stat;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public Stat getStat() {
        return stat;
    }

    public double getValue() {
        return value;
    }
}
