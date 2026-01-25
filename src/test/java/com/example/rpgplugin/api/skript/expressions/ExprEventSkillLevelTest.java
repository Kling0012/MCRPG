package com.example.rpgplugin.api.skript.expressions;

import com.example.rpgplugin.api.skript.events.EvtRPGSkillCast.RPGSkillCastEvent;
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
class ExprEventSkillLevelTest {

    private ExprEventSkillLevel expression;

    @Mock
    private RPGSkillCastEvent skillCastEvent;

    @Mock
    private Event unknownEvent;

    @BeforeEach
    void setUp() {
        expression = new ExprEventSkillLevel();
    }

    @Test
    void testGetReturnType() {
        assertEquals(Integer.class, expression.getReturnType());
    }

    @Test
    void testIsSingle() {
        assertTrue(expression.isSingle());
    }

    @Test
    void testToString() {
        assertEquals("event-skill-level", expression.toString(null, false));
    }

    @Test
    void testInit() {
        assertTrue(expression.init(null, 0, null, null));
    }

    @Test
    void testGetWithSkillCastEvent() {
        when(skillCastEvent.getSkillLevel()).thenReturn(3);

        Number[] result = expression.get(skillCastEvent);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(3, result[0]);
    }

    @Test
    void testGetWithUnknownEvent() {
        Number[] result = expression.get(unknownEvent);

        assertNotNull(result);
        assertEquals(0, result.length);
    }
}
