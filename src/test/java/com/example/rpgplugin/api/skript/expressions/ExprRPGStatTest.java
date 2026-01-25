package com.example.rpgplugin.api.skript.expressions;

import ch.njol.skript.lang.Expression;
import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
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
class ExprRPGStatTest {

    private ExprRPGStat expression;

    @Mock
    private Expression<String> statExpr;

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

    @Mock
    private StatManager statManager;

    @BeforeEach
    void setUp() {
        expression = new ExprRPGStat();
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
        when(statExpr.toString(any(), anyBoolean())).thenReturn("str");
        when(playerExpr.toString(any(), anyBoolean())).thenReturn("player");

        assertTrue(expression.init(new Expression[]{statExpr, playerExpr}, 0, null, null));
        assertEquals("rpg stat str of player", expression.toString(event, true));
    }

    @Test
    void testGetWithInvalidStatName() {
        expression.init(new Expression[]{statExpr, playerExpr}, 0, null, null);
        when(statExpr.getSingle(event)).thenReturn("unknown");
        when(playerExpr.getSingle(event)).thenReturn(player);

        Number[] result = expression.get(event);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testGetWithNullPlugin() {
        expression.init(new Expression[]{statExpr, playerExpr}, 0, null, null);
        when(statExpr.getSingle(event)).thenReturn("STR");
        when(playerExpr.getSingle(event)).thenReturn(player);

        try (MockedStatic<RPGPlugin> mocked = mockStatic(RPGPlugin.class)) {
            mocked.when(RPGPlugin::getInstance).thenReturn(null);

            Number[] result = expression.get(event);

            assertNotNull(result);
            assertEquals(0, result.length);
        }
    }

    @Test
    void testGetWithValidStat() {
        UUID uuid = UUID.randomUUID();
        expression.init(new Expression[]{statExpr, playerExpr}, 0, null, null);
        when(statExpr.getSingle(event)).thenReturn("str");
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(player.getUniqueId()).thenReturn(uuid);
        when(plugin.getPlayerManager()).thenReturn(playerManager);
        when(playerManager.getRPGPlayer(uuid)).thenReturn(rpgPlayer);
        when(rpgPlayer.getStatManager()).thenReturn(statManager);
        when(statManager.getFinalStat(Stat.STRENGTH)).thenReturn(42);

        try (MockedStatic<RPGPlugin> mocked = mockStatic(RPGPlugin.class)) {
            mocked.when(RPGPlugin::getInstance).thenReturn(plugin);

            Number[] result = expression.get(event);

            assertNotNull(result);
            assertEquals(1, result.length);
            assertEquals(42, result[0]);
        }
    }
}
