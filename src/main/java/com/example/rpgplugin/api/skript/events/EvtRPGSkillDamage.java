package com.example.rpgplugin.api.skript.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import com.example.rpgplugin.skill.Skill;
import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;

/**
 * on rpg skill damage イベント
 *
 * <p>スキルでダメージを与えた時にトリガーされます。</p>
 *
 * <p>構文:</p>
 * <pre>
 * on rpg skill damage:
 *     set {_p} to event-player
 *     set {_skill} to event-skill
 *     set {_dmg} to event-damage
 *     set {_target} to event-target
 *
 * on rpg skill damage [of] %string%:
 *     # 特定スキルのみ
 * </pre>
 *
 * <p>イベント式（イベント内で使用可能）:</p>
 * <ul>
 *   <li>event-player / event-caster - スキルを発動したプレイヤー</li>
 *   <li>event-skill-id - スキルID（文字列）</li>
 *   <li>event-skill - スキルオブジェクト</li>
 *   <li>event-skill-level - スキルレベル（数値）</li>
 *   <li>event-target - ダメージを受けたエンティティ</li>
 *   <li>event-damage - 与えたダメージ（数値）</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.1
 */
public class EvtRPGSkillDamage extends SkriptEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    static {
        Skript.registerEvent("RPGSkillDamage", EvtRPGSkillDamage.class, RPGSkillDamageEvent.class,
                "on rpg skill damage",
                "on rpg skill damage [of] %string%"
        );

        // イベント式の登録は別途 ExprEventXXX クラスで行います
    }

    private Literal<String> skillId;

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
        if (matchedPattern == 1) {
            skillId = (Literal<String>) args[0];
        }
        return true;
    }

    @Override
    public boolean check(@NotNull Event e) {
        if (skillId == null) {
            return true;
        }
        String targetSkill = skillId.getSingle(e);
        if (targetSkill == null) {
            return true;
        }
        if (e instanceof RPGSkillDamageEvent) {
            return targetSkill.equals(((RPGSkillDamageEvent) e).getSkillId());
        }
        return false;
    }

    @Override
    public String toString(@NotNull Event e, boolean debug) {
        if (skillId != null) {
            return "on rpg skill damage of " + skillId.toString(e, debug);
        }
        return "on rpg skill damage";
    }

    /**
     * RPGスキルダメージイベント
     */
    public static class RPGSkillDamageEvent extends Event {

        private final Player player;
        private final String skillId;
        private final Skill skill;
        private final int skillLevel;
        private final Entity target;
        private final double damage;

        public RPGSkillDamageEvent(Player player, String skillId, Skill skill, int skillLevel, Entity target, double damage) {
            this.player = player;
            this.skillId = skillId;
            this.skill = skill;
            this.skillLevel = skillLevel;
            this.target = target;
            this.damage = damage;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public Player getPlayer() {
            return player;
        }

        public String getSkillId() {
            return skillId;
        }

        public Skill getSkill() {
            return skill;
        }

        public int getSkillLevel() {
            return skillLevel;
        }

        public Entity getTarget() {
            return target;
        }

        public double getDamage() {
            return damage;
        }
    }
}
