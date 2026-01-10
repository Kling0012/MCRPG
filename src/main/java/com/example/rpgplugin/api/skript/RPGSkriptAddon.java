package com.example.rpgplugin.api.skript;

import com.example.rpgplugin.RPGPlugin;

/**
 * RPGPlugin Skript連携メインクラス
 *
 * <p>Skriptのネイティブ構文でRPGPluginにアクセスするための要素を登録します。</p>
 *
 * <p>登録される要素:</p>
 * <ul>
 *   <li>Expressions: rpg level, rpg stat, rpg class, rpg skill level, rpg skill point, rpg attr point</li>
 *   <li>Event Expressions: event-player, event-skill, event-skill-id, event-skill-level, event-target, event-damage</li>
 *   <li>Conditions: has rpg skill, can upgrade rpg class, rpg stat is at least, rpg class is</li>
 *   <li>Effects: unlock rpg skill, cast rpg skill, set rpg class, add/set/remove rpg stat</li>
 *   <li>Events: on rpg skill cast, on rpg skill damage</li>
 * </ul>
 *
 * <p>使用例:</p>
 * <pre>
 * # Expressions
 * set {_lvl} to rpg level of player
 * set {_str} to rpg stat "str" of player
 * set {_class} to rpg class of player
 * set {_skilllvl} to rpg skill level "fireball" of player
 * set {_skillPoints} to rpg skill point of player
 * set {_attrPoints} to rpg attr point of player
 *
 * # Conditions
 * if player has rpg skill "fireball":
 *     send "You have fireball skill!"
 *
 * if player can upgrade rpg class:
 *     send "You can upgrade your class!"
 *
 * if rpg stat "str" of player is at least 50:
 *     send "Your strength is 50 or higher!"
 *
 * if rpg class of player is "warrior":
 *     send "You are a warrior!"
 *
 * # Effects
 * unlock rpg skill "fireball" for player
 * make player cast rpg skill "fireball"
 * set rpg class of player to "mage"
 * add 5 to rpg stat "str" of player
 * set rpg stat "int" of player to 20
 * remove 3 from rpg stat "dex" of player
 *
 * # Point operations
 * add 1 to rpg skill point of player
 * set rpg attr point of player to 5
 *
 * # Events
 * on rpg skill cast:
 *     send "Skill cast!" to event-player
 *     send "Skill ID: %event-skill-id%" to event-player
 *     send "Skill Level: %event-skill-level%" to event-player
 *     if event-target is set:
 *         send "Target: %event-target%" to event-player
 *
 * on rpg skill damage:
 *     send "Dealt %event-damage% damage!" to event-player
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.1
 */
public class RPGSkriptAddon {

    private final RPGPlugin plugin;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public RPGSkriptAddon(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Skript要素を登録します
     *
     * <p>Skriptがロードされている場合のみ登録を行います。</p>
     */
    public void registerElements() {
        if (!isSkriptLoaded()) {
            plugin.getLogger().info("Skript is not loaded. Skript integration disabled.");
            return;
        }

        try {
            // Expressions (auto-registered via static initialization)
            Class.forName("com.example.rpgplugin.api.skript.expressions.ExprRPGLevel");
            Class.forName("com.example.rpgplugin.api.skript.expressions.ExprRPGStat");
            Class.forName("com.example.rpgplugin.api.skript.expressions.ExprRPGClass");
            Class.forName("com.example.rpgplugin.api.skript.expressions.ExprRPGSkillLevel");
            Class.forName("com.example.rpgplugin.api.skript.expressions.ExprRPGSkillPoint");
            Class.forName("com.example.rpgplugin.api.skript.expressions.ExprRPGAttrPoint");

            // Event Expressions (auto-registered via static initialization)
            Class.forName("com.example.rpgplugin.api.skript.expressions.ExprEventPlayer");
            Class.forName("com.example.rpgplugin.api.skript.expressions.ExprEventSkill");
            Class.forName("com.example.rpgplugin.api.skript.expressions.ExprEventSkillId");
            Class.forName("com.example.rpgplugin.api.skript.expressions.ExprEventSkillLevel");
            Class.forName("com.example.rpgplugin.api.skript.expressions.ExprEventTarget");
            Class.forName("com.example.rpgplugin.api.skript.expressions.ExprEventDamage");

            // Conditions
            Class.forName("com.example.rpgplugin.api.skript.conditions.CondHasRPGSkill");
            Class.forName("com.example.rpgplugin.api.skript.conditions.CondCanUpgradeRPGClass");
            Class.forName("com.example.rpgplugin.api.skript.conditions.CondRPGStatAbove");
            Class.forName("com.example.rpgplugin.api.skript.conditions.CondIsRPGClass");

            // Effects
            Class.forName("com.example.rpgplugin.api.skript.effects.EffUnlockRPGSkill");
            Class.forName("com.example.rpgplugin.api.skript.effects.EffCastRPGSkill");
            Class.forName("com.example.rpgplugin.api.skript.effects.EffSetRPGClass");
            Class.forName("com.example.rpgplugin.api.skript.effects.EffModifyRPGStat");

            // Events
            Class.forName("com.example.rpgplugin.api.skript.events.EvtRPGSkillCast");
            Class.forName("com.example.rpgplugin.api.skript.events.EvtRPGSkillDamage");

            plugin.getLogger().info("Skript integration loaded successfully!");
            plugin.getLogger().info("Registered: 12 expressions, 4 conditions, 4 effects, 2 events");

        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Failed to load Skript elements: " + e.getMessage());
        }
    }

    /**
     * Skriptがロードされているかチェック
     *
     * @return Skriptがロードされている場合true
     */
    private boolean isSkriptLoaded() {
        try {
            Class.forName("ch.njol.skript.Skript");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
