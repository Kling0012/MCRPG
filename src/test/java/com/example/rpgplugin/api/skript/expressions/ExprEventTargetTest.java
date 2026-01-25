package com.example.rpgplugin.api.skript.expressions;

import com.example.rpgplugin.api.skript.events.EvtRPGSkillCast.RPGSkillCastEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExprEventTargetTest {

    private ExprEventTarget expression;

    @Mock
    private RPGSkillCastEvent skillCastEvent;

    @Mock
    private Entity target;

    @Mock
    private Event unknownEvent;

    @BeforeEach
    void setUp() {
        expression = new ExprEventTarget();
    }

    @Test
    void testGetReturnType() {
        assertEquals(Entity.class, expression.getReturnType());
    }

    @Test
    void testIsSingle() {
        assertTrue(expression.isSingle());
    }

    @Test
    void testToString() {
        assertEquals("event-target", expression.toString(null, false));
    }

    @Test
    void testInit() {
        assertTrue(expression.init(null, 0, null, null));
    }

    @Test
    void testGetWithSkillCastEvent() {
        when(skillCastEvent.getTarget()).thenReturn(target);

        Entity[] result = expression.get(skillCastEvent);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(target, result[0]);
    }

    @Test
    void testGetWithSkillCastEventNullTarget() {
        when(skillCastEvent.getTarget()).thenReturn(null);

        Entity[] result = expression.get(skillCastEvent);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testGetWithUnknownEvent() {
        Entity[] result = expression.get(unknownEvent);

        assertNotNull(result);
        assertEquals(0, result.length);
    }
}
