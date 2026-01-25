package com.example.rpgplugin.api.skript.expressions;

import com.example.rpgplugin.api.skript.events.EvtRPGSkillCast.RPGSkillCastEvent;
import org.bukkit.entity.Player;
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
class ExprEventPlayerTest {

    private ExprEventPlayer expression;

    @Mock
    private RPGSkillCastEvent skillCastEvent;

    @Mock
    private Player player;

    @Mock
    private Event unknownEvent;

    @BeforeEach
    void setUp() {
        expression = new ExprEventPlayer();
    }

    @Test
    void testGetReturnType() {
        assertEquals(Player.class, expression.getReturnType());
    }

    @Test
    void testIsSingle() {
        assertTrue(expression.isSingle());
    }

    @Test
    void testToString() {
        assertEquals("event-player", expression.toString(null, false));
    }

    @Test
    void testInit() {
        assertTrue(expression.init(null, 0, null, null));
    }

    @Test
    void testGetWithSkillCastEvent() {
        when(skillCastEvent.getPlayer()).thenReturn(player);

        Player[] result = expression.get(skillCastEvent);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(player, result[0]);
    }

    @Test
    void testGetWithSkillCastEventNullPlayer() {
        when(skillCastEvent.getPlayer()).thenReturn(null);

        Player[] result = expression.get(skillCastEvent);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testGetWithUnknownEvent() {
        Player[] result = expression.get(unknownEvent);

        assertNotNull(result);
        assertEquals(0, result.length);
    }
}
