package com.example.rpgplugin.api.skript.events;

import ch.njol.skript.lang.Literal;
import com.example.rpgplugin.skill.Skill;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EvtRPGSkillDamage tests")
class EvtRPGSkillDamageTest {

    @Mock
    private Player player;

    @Mock
    private Skill skill;

    @Mock
    private Entity target;

    @BeforeAll
    static void setUpSkript() {
        SkriptTestBootstrap.ensureInitialized();
    }

    @Test
    @DisplayName("init without pattern keeps the default toString")
    void initWithoutPatternKeepsDefaultToString() {
        EvtRPGSkillDamage evt = new EvtRPGSkillDamage();
        assertTrue(evt.init(new Literal[0], 0, null));
        assertEquals("on rpg skill damage", evt.toString(mock(Event.class), false));
    }

    @Test
    @DisplayName("init with pattern uses the skill id in toString")
    void initWithPatternUsesSkillIdInToString() {
        EvtRPGSkillDamage evt = new EvtRPGSkillDamage();
        @SuppressWarnings("unchecked")
        Literal<String> literal = mock(Literal.class);
        when(literal.toString(any(Event.class), eq(false))).thenReturn("fireball");
        assertTrue(evt.init(new Literal[]{literal}, 1, null));
        assertEquals("on rpg skill damage of fireball", evt.toString(mock(Event.class), false));
    }

    @Test
    @DisplayName("check returns true when no skill id is provided")
    void checkReturnsTrueWhenSkillIdNull() {
        EvtRPGSkillDamage evt = new EvtRPGSkillDamage();
        assertTrue(evt.check(mock(Event.class)));
    }

    @Test
    @DisplayName("check returns true when literal returns null")
    void checkReturnsTrueWhenLiteralReturnsNull() {
        EvtRPGSkillDamage evt = new EvtRPGSkillDamage();
        @SuppressWarnings("unchecked")
        Literal<String> literal = mock(Literal.class);
        when(literal.getSingle(any(Event.class))).thenReturn(null);
        assertTrue(evt.init(new Literal[]{literal}, 1, null));
        assertTrue(evt.check(mock(Event.class)));
    }

    @Test
    @DisplayName("check returns true when skill id matches")
    void checkReturnsTrueWhenSkillIdMatches() {
        EvtRPGSkillDamage evt = new EvtRPGSkillDamage();
        @SuppressWarnings("unchecked")
        Literal<String> literal = mock(Literal.class);
        when(literal.getSingle(any(Event.class))).thenReturn("fireball");
        assertTrue(evt.init(new Literal[]{literal}, 1, null));

        Event event = new EvtRPGSkillDamage.RPGSkillDamageEvent(player, "fireball", skill, 3, target, 7.5);
        assertTrue(evt.check(event));
    }

    @Test
    @DisplayName("check returns false when skill id does not match")
    void checkReturnsFalseWhenSkillIdDoesNotMatch() {
        EvtRPGSkillDamage evt = new EvtRPGSkillDamage();
        @SuppressWarnings("unchecked")
        Literal<String> literal = mock(Literal.class);
        when(literal.getSingle(any(Event.class))).thenReturn("ice");
        assertTrue(evt.init(new Literal[]{literal}, 1, null));

        Event event = new EvtRPGSkillDamage.RPGSkillDamageEvent(player, "fireball", skill, 3, target, 7.5);
        assertFalse(evt.check(event));
    }

    @Test
    @DisplayName("check returns false for non skill damage events")
    void checkReturnsFalseForNonSkillDamageEvent() {
        EvtRPGSkillDamage evt = new EvtRPGSkillDamage();
        @SuppressWarnings("unchecked")
        Literal<String> literal = mock(Literal.class);
        when(literal.getSingle(any(Event.class))).thenReturn("fireball");
        assertTrue(evt.init(new Literal[]{literal}, 1, null));

        assertFalse(evt.check(mock(Event.class)));
    }

    @Test
    @DisplayName("event getters return provided values")
    void eventGettersReturnProvidedValues() {
        EvtRPGSkillDamage.RPGSkillDamageEvent event =
                new EvtRPGSkillDamage.RPGSkillDamageEvent(player, "fireball", skill, 4, target, 12.5);

        assertSame(player, event.getPlayer());
        assertEquals("fireball", event.getSkillId());
        assertSame(skill, event.getSkill());
        assertEquals(4, event.getSkillLevel());
        assertSame(target, event.getTarget());
        assertEquals(12.5, event.getDamage(), 0.0001);
    }

    @Test
    @DisplayName("handlers are shared across instances")
    void handlersAreSharedAcrossInstances() {
        EvtRPGSkillDamage.RPGSkillDamageEvent first =
                new EvtRPGSkillDamage.RPGSkillDamageEvent(player, "fireball", skill, 1, target, 1.0);
        EvtRPGSkillDamage.RPGSkillDamageEvent second =
                new EvtRPGSkillDamage.RPGSkillDamageEvent(player, "fireball", skill, 1, target, 1.0);

        assertSame(first.getHandlers(), second.getHandlers());
    }
}
