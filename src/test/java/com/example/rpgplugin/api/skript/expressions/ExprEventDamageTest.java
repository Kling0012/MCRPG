package com.example.rpgplugin.api.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.SimpleExpression;
import com.example.rpgplugin.api.skript.events.EvtRPGSkillCast.RPGSkillCastEvent;
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
class ExprEventDamageTest {

    private SimpleExpression<Number> expression;
    private MockedStatic<Skript> skriptMock;

    @Mock
    private RPGSkillCastEvent skillCastEvent;
    
    @Mock
    private Event unknownEvent;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        // 1. モックを先に作成
        skriptMock = mockStatic(Skript.class);
        skriptMock.when(() -> Skript.registerExpression(any(), any(), any(), any())).thenAnswer(i -> null);
        skriptMock.when(() -> Skript.registerExpression(any(), any(), any(), any(), any())).thenAnswer(i -> null);

        // 2. リフレクションでクラスをロード＆インスタンス化
        // これにより、static initializerがこのタイミングで実行され、モックが効くはず
        Class<?> clazz = Class.forName("com.example.rpgplugin.api.skript.expressions.ExprEventDamage");
        expression = (SimpleExpression<Number>) clazz.getDeclaredConstructor().newInstance();
    }
    
    @AfterEach
    void tearDown() {
        if (skriptMock != null) {
            skriptMock.close();
        }
    }

    @Test
    void testGetReturnType() {
        assertEquals(Double.class, expression.getReturnType());
    }

    @Test
    void testIsSingle() {
        assertTrue(expression.isSingle());
    }

    @Test
    void testToString() {
        assertEquals("event-damage", expression.toString(null, false));
    }

    @Test
    void testInit() {
        assertTrue(expression.init(null, 0, null, null));
    }

    @Test
    void testGetWithSkillCastEvent() throws Exception {
        when(skillCastEvent.getDamage()).thenReturn(50.0);
        
        // getメソッドはprotectedなのでリフレクションで呼ぶか、
        // SimpleExpressionが継承しているgetSingleなどを呼ぶ
        // SimpleExpression.get(Event)はprotectedだが、getArray(Event)やgetSingle(Event)はpublic
        // しかしgetSingleの実装によっては内部でget(Event)を呼ぶ
        
        // protectedメソッドをテストするためにリフレクションを使用
        Method getMethod = expression.getClass().getDeclaredMethod("get", Event.class);
        getMethod.setAccessible(true);
        Number[] result = (Number[]) getMethod.invoke(expression, skillCastEvent);
        
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(50.0, result[0]);
    }

    @Test
    void testGetWithUnknownEvent() throws Exception {
        Method getMethod = expression.getClass().getDeclaredMethod("get", Event.class);
        getMethod.setAccessible(true);
        Number[] result = (Number[]) getMethod.invoke(expression, unknownEvent);
        
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(0, result[0]); 
    }
}
