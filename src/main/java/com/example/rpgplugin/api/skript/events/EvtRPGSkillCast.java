package com.example.rpgplugin.api.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.registrations.EventValues;
import com.example.rpgplugin.skill.Skill;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * RPGスキル発動イベント
 *
 * <p>SKriptスクリプトでRPGスキル発動を検知するためのカスタムイベントです。</p>
 *
 * <p>構文:</p>
 * <pre>
 * on rpg skill cast:
 * on rpg skill cast of %skill%:
 * </pre>
 *
 * <p>使用可能なイベント値:</p>
 * <ul>
 *   <li>player (Player) - スキルを発動したプレイヤー</li>
 *   <li>caster (Player) - 発動者（playerの別名）</li>
 *   <li>skill-id (String) - スキルID</li>
 *   <li>skill (Skill) - スキルオブジェクト（skript-reflect使用時）</li>
 *   <li>skill-level (Integer) - スキルレベル</li>
 *   <li>target (Entity) - ターゲットエンティティ（存在する場合）</li>
 *   <li>damage (Number) - 与えるダメージ（計算済みの場合）</li>
 * </ul>
 *
 * <p>使用例:</p>
 * <pre>
 * on rpg skill cast:
 *     send "スキル %skill-id% が発動しました！" to player
 *     if target is set:
 *         send "ターゲット: %target%" to player
 *
 * on rpg skill cast of "fireball":
 *     send "ファイアボール発動！" to player
 *     send "LV%skill-level% ファイアボール" to player
 *
 * # 特定スキル発動時に追加効果
 * on rpg skill cast:
 *     if skill-id is "heal":
 *         heal player by 5
 *     if skill-id is "power_strike":
 *         play sound "ENTITY_PLAYER_ATTACK_STRONG" with volume 1 and pitch 1 to player
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class EvtRPGSkillCast extends SkriptEventInfo {

    static {
        Skript.registerEvent("RPG Skill Cast", EvtRPGSkillCast.class,
                com.example.rpgplugin.api.bridge.SKriptSkillEvent.class,
                "on rpg skill cast",
                "on rpg skill cast [of] %string%"
        );

        // イベント値の登録
        EventValues.registerEventValue(com.example.rpgplugin.api.bridge.SKriptSkillEvent.class,
                Player.class, new org.bukkit.event.Event.Value<>(),
                org.bukkit.event.Event.ValueTime.TIME_PAST);

        EventValues.registerEventValue(com.example.rpgplugin.api.bridge.SKriptSkillEvent.class,
                Player.class, new org.bukkit.event.Event.Value<>("caster"),
                org.bukkit.event.Event.ValueTime.TIME_PAST);

        EventValues.registerEventValue(com.example.rpgplugin.api.bridge.SKriptSkillEvent.class,
                String.class, new org.bukkit.event.Event.Value<>("skill-id"),
                org.bukkit.event.Event.ValueTime.TIME_PAST);

        EventValues.registerEventValue(com.example.rpgplugin.api.bridge.SKriptSkillEvent.class,
                Integer.class, new org.bukkit.event.Event.Value<>("skill-level"),
                org.bukkit.event.Event.ValueTime.TIME_PAST);

        EventValues.registerEventValue(com.example.rpgplugin.api.bridge.SKriptSkillEvent.class,
                Entity.class, new org.bukkit.event.Event.Value<>("target"),
                org.bukkit.event.Event.ValueTime.TIME_PAST);

        EventValues.registerEventValue(com.example.rpgplugin.api.bridge.SKriptSkillEvent.class,
                Number.class, new org.bukkit.event.Event.Value<>("damage"),
                org.bukkit.event.Event.ValueTime.TIME_PAST);

        // skript-reflectを使用する場合のSkillオブジェクトクト
        try {
            EventValues.registerEventValue(com.example.rpgplugin.api.bridge.SKriptSkillEvent.class,
                    Skill.class, new org.bukkit.event.Event.Value<>("skill"),
                    org.bukkit.event.Event.ValueTime.TIME_PAST);
        } catch (Exception ignored) {
            // Skriptがロードされていない場合
        }
    }

    private Literal<String> skillIdExpr;

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
        if (matchedPattern == 1) {
            this.skillIdExpr = (Literal<String>) args[0];
        }
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (skillIdExpr == null) {
            return true;
        }

        if (!(e instanceof com.example.rpgplugin.api.bridge.SKriptSkillEvent)) {
            return false;
        }

        com.example.rpgplugin.api.bridge.SKriptSkillEvent event = (com.example.rpgplugin.api.bridge.SKriptSkillEvent) e;
        String skillId = skillIdExpr.getSingle(e);

        return skillId != null && skillId.equalsIgnoreCase(event.getSkillId());
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        if (skillIdExpr != null) {
            return "on rpg skill cast of " + skillIdExpr.toString(e, debug);
        }
        return "on rpg skill cast";
    }
}
