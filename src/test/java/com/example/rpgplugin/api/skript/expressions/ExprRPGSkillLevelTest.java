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
class ExprRPGSkillLevelTest {

    private ExprRPGSkillLevel expression;

    @Mock
    private Expression<String> skillExpr;

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
        expression = new ExprRPGSkillLevel();
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
    void testInitPatternZeroToString() {
        when(skillExpr.toString(any(), anyBoolean())).thenReturn("skill");
        when(playerExpr.toString(any(), anyBoolean())).thenReturn("player");

        assertTrue(expression.init(new Expression[]{skillExpr, playerExpr}, 0, null, null));
        assertEquals("rpg skill level of skill from player", expression.toString(event, true));
    }

    @Test
    void testInitPatternOneToString() {
        when(skillExpr.toString(any(), anyBoolean())).thenReturn("skill");
        when(playerExpr.toString(any(), anyBoolean())).thenReturn("player");

        assertTrue(expression.init(new Expression[]{playerExpr, skillExpr}, 1, null, null));
        assertEquals("rpg skill level of skill from player", expression.toString(event, true));
    }

    @Test
    void testGetWithNullSkillId() {
        expression.init(new Expression[]{skillExpr, playerExpr}, 0, null, null);
        when(skillExpr.getSingle(event)).thenReturn(null);
        when(playerExpr.getSingle(event)).thenReturn(player);

        Number[] result = expression.get(event);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testGetWithNoSkillReturnsZero() {
        UUID uuid = UUID.randomUUID();
        expression.init(new Expression[]{skillExpr, playerExpr}, 0, null, null);
        when(skillExpr.getSingle(event)).thenReturn("fireball");
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(player.getUniqueId()).thenReturn(uuid);
        when(plugin.getPlayerManager()).thenReturn(playerManager);
        when(playerManager.getRPGPlayer(uuid)).thenReturn(rpgPlayer);
        when(rpgPlayer.hasSkill("fireball")).thenReturn(false);

        try (MockedStatic<RPGPlugin> mocked = mockStatic(RPGPlugin.class)) {
            mocked.when(RPGPlugin::getInstance).thenReturn(plugin);

            Number[] result = expression.get(event);

            assertNotNull(result);
            assertEquals(1, result.length);
            assertEquals(0, result[0]);
        }
    }

    @Test
    void testGetWithSkillReturnsLevel() {
        UUID uuid = UUID.randomUUID();
        expression.init(new Expression[]{skillExpr, playerExpr}, 0, null, null);
        when(skillExpr.getSingle(event)).thenReturn("fireball");
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(player.getUniqueId()).thenReturn(uuid);
        when(plugin.getPlayerManager()).thenReturn(playerManager);
        when(playerManager.getRPGPlayer(uuid)).thenReturn(rpgPlayer);
        when(rpgPlayer.hasSkill("fireball")).thenReturn(true);
        when(rpgPlayer.getSkillLevel("fireball")).thenReturn(4);

        try (MockedStatic<RPGPlugin> mocked = mockStatic(RPGPlugin.class)) {
            mocked.when(RPGPlugin::getInstance).thenReturn(plugin);

            Number[] result = expression.get(event);

            assertNotNull(result);
            assertEquals(1, result.length);
            assertEquals(4, result[0]);
        }
    }
}
