package com.example.rpgplugin.api.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleExpression;
import com.example.rpgplugin.api.skript.events.EvtRPGSkillCast.RPGSkillCastEvent;
import com.example.rpgplugin.skill.Skill;
import org.bukkit.event.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExprEventSkillTest {

    private SimpleExpression<Skill> expression;
    private MockedStatic<Skript> skriptMock;

    @Mock
    private RPGSkillCastEvent skillCastEvent;
    
    @Mock
    private Skill mockSkill;
    
    @Mock
    private Event unknownEvent;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        skriptMock = mockStatic(Skript.class);
        skriptMock.when(() -> Skript.registerExpression(any(), any(), any(), any())).thenAnswer(i -> null);
        skriptMock.when(() -> Skript.registerExpression(any(), any(), any(), any(), any())).thenAnswer(i -> null);

        Class<?> clazz = Class.forName("com.example.rpgplugin.api.skript.expressions.ExprEventSkill");
        expression = (SimpleExpression<Skill>) clazz.getDeclaredConstructor().newInstance();
    }
    
    @AfterEach
    void tearDown() {
        if (skriptMock != null) {
            skriptMock.close();
        }
    }

    @Test
    void testGetReturnType() {
        assertEquals(Skill.class, expression.getReturnType());
    }

    @Test
    void testIsSingle() {
        assertTrue(expression.isSingle());
    }

    @Test
    void testToString() {
        assertEquals("event-skill", expression.toString(null, false));
    }

    @Test
    void testInit() {
        assertTrue(expression.init(null, 0, null, null));
    }

    @Test
    void testGetWithSkillCastEvent() throws Exception {
        when(skillCastEvent.getSkill()).thenReturn(mockSkill);
        
        Method getMethod = expression.getClass().getDeclaredMethod("get", Event.class);
        getMethod.setAccessible(true);
        Skill[] result = (Skill[]) getMethod.invoke(expression, skillCastEvent);
        
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(mockSkill, result[0]);
    }
    
    @Test
    void testGetWithSkillCastEventNullSkill() throws Exception {
        when(skillCastEvent.getSkill()).thenReturn(null);
        
        Method getMethod = expression.getClass().getDeclaredMethod("get", Event.class);
        getMethod.setAccessible(true);
        Skill[] result = (Skill[]) getMethod.invoke(expression, skillCastEvent);
        
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testGetWithUnknownEvent() throws Exception {
        Method getMethod = expression.getClass().getDeclaredMethod("get", Event.class);
        getMethod.setAccessible(true);
        Skill[] result = (Skill[]) getMethod.invoke(expression, unknownEvent);
        
        assertNotNull(result);
        assertEquals(0, result.length);
    }
}
