package com.example.rpgplugin.api.skript.expressions;

import ch.njol.skript.lang.Expression;
import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExprRPGLevelTest {

    private ExprRPGLevel expression;

    @Mock
    private Expression<Player> playerExpr;

    @Mock
    private Player player;

    @Mock
    private Event event;

    @Mock
    private RPGPlugin plugin;

    @Mock
    private PlayerManager playerManager;

    @Mock
    private RPGPlayer rpgPlayer;

    @BeforeEach
    void setUp() {
        expression = new ExprRPGLevel();
    }

    @Test
    void testGetReturnType() {
        assertEquals(Number.class, expression.getReturnType());
    }

    @Test
    void testIsSingle() {
        assertTrue(expression.isSingle());
    }

    @Test
    void testInitAndToString() {
        when(playerExpr.toString(any(), anyBoolean())).thenReturn("player");

        assertTrue(expression.init(new Expression[]{playerExpr}, 0, null, null));
        assertEquals("rpg level of player", expression.toString(event, true));
    }

    @Test
    void testGetWithNullPlayer() {
        expression.init(new Expression[]{playerExpr}, 0, null, null);
        when(playerExpr.getSingle(event)).thenReturn(null);

        Number[] result = expression.get(event);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testGetWithNullPlugin() {
        expression.init(new Expression[]{playerExpr}, 0, null, null);
        when(playerExpr.getSingle(event)).thenReturn(player);

        try (MockedStatic<RPGPlugin> mocked = mockStatic(RPGPlugin.class)) {
            mocked.when(RPGPlugin::getInstance).thenReturn(null);

            Number[] result = expression.get(event);

            assertNotNull(result);
            assertEquals(0, result.length);
        }
    }

    @Test
    void testGetWithNullRpgPlayer() {
        UUID uuid = UUID.randomUUID();
        expression.init(new Expression[]{playerExpr}, 0, null, null);
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(player.getUniqueId()).thenReturn(uuid);
        when(plugin.getPlayerManager()).thenReturn(playerManager);
        when(playerManager.getRPGPlayer(uuid)).thenReturn(null);

        try (MockedStatic<RPGPlugin> mocked = mockStatic(RPGPlugin.class)) {
            mocked.when(RPGPlugin::getInstance).thenReturn(plugin);

            Number[] result = expression.get(event);

            assertNotNull(result);
            assertEquals(0, result.length);
        }
    }

    @Test
    void testGetWithValidPlayerLevel() {
        UUID uuid = UUID.randomUUID();
        expression.init(new Expression[]{playerExpr}, 0, null, null);
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(player.getUniqueId()).thenReturn(uuid);
        when(plugin.getPlayerManager()).thenReturn(playerManager);
        when(playerManager.getRPGPlayer(uuid)).thenReturn(rpgPlayer);
        when(rpgPlayer.getLevel()).thenReturn(12);

        try (MockedStatic<RPGPlugin> mocked = mockStatic(RPGPlugin.class)) {
            mocked.when(RPGPlugin::getInstance).thenReturn(plugin);

            Number[] result = expression.get(event);

            assertNotNull(result);
            assertEquals(1, result.length);
            assertEquals(12, result[0]);
        }
    }
}
