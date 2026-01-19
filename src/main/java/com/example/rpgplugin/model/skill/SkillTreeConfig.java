package com.example.rpgplugin.model.skill;

import java.util.ArrayList;
import java.util.List;

/**
 * スキルツリー設定
 *
 * <p>親スキル、習得要件、習得コスト、アイコンを管理します。</p>
 */
public class SkillTreeConfig {
    private final String parent;
    private final List<UnlockRequirement> unlockRequirements;
    private final int cost;
    private final String icon;

    public SkillTreeConfig(String parent, List<UnlockRequirement> unlockRequirements, int cost, String icon) {
        this.parent = parent;
        this.unlockRequirements = unlockRequirements != null ? unlockRequirements : new ArrayList<>();
        this.cost = cost;
        this.icon = icon;
    }

    public String getParent() {
        return parent;
    }

    public List<UnlockRequirement> getUnlockRequirements() {
        return unlockRequirements;
    }

    public int getCost() {
        return cost;
    }

    public String getIcon() {
        return icon;
    }
}
