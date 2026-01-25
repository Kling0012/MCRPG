package com.example.rpgplugin.api.skript.conditions;

import ch.njol.skript.lang.Expression;
import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CondHasRPGSkill テスト")
class CondHasRPGSkillTest {

    @Mock
    private Expression<Player> playerExpr;

    @Mock
    private Expression<String> skillExpr;

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

    private CondHasRPGSkill condition;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        condition = new CondHasRPGSkill();
        condition.init(new Expression[]{playerExpr, skillExpr}, 0, null, null);
        playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
    }

    @Test
    @DisplayName("check: プレイヤーがnullの場合はfalse")
    void check_PlayerNull_ReturnsFalse() {
        when(playerExpr.getSingle(event)).thenReturn(null);
        when(skillExpr.getSingle(event)).thenReturn("fireball");

        assertFalse(condition.check(event));
    }

    @Test
    @DisplayName("check: スキル名がnullの場合はfalse")
    void check_SkillNull_ReturnsFalse() {
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(skillExpr.getSingle(event)).thenReturn(null);

        assertFalse(condition.check(event));
    }

    @Test
    @DisplayName("check: プラグイン取得失敗の場合はfalse")
    void check_PluginNull_ReturnsFalse() {
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(skillExpr.getSingle(event)).thenReturn("fireball");

        try (MockedStatic<RPGPlugin> mockedStatic = mockStatic(RPGPlugin.class)) {
            mockedStatic.when(RPGPlugin::getInstance).thenReturn(null);

            assertFalse(condition.check(event));
        }
    }

    @Test
    @DisplayName("check: RPGPlayer未登録の場合はfalse")
    void check_RpgPlayerNull_ReturnsFalse() {
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(skillExpr.getSingle(event)).thenReturn("fireball");

        try (MockedStatic<RPGPlugin> mockedStatic = mockStatic(RPGPlugin.class)) {
            mockedStatic.when(RPGPlugin::getInstance).thenReturn(plugin);
            when(plugin.getPlayerManager()).thenReturn(playerManager);
            when(playerManager.getRPGPlayer(playerId)).thenReturn(null);

            assertFalse(condition.check(event));
        }
    }

    @Test
    @DisplayName("check: スキル保持時はtrue")
    void check_HasSkill_ReturnsTrue() {
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(skillExpr.getSingle(event)).thenReturn("fireball");

        try (MockedStatic<RPGPlugin> mockedStatic = mockStatic(RPGPlugin.class)) {
            mockedStatic.when(RPGPlugin::getInstance).thenReturn(plugin);
            when(plugin.getPlayerManager()).thenReturn(playerManager);
            when(playerManager.getRPGPlayer(playerId)).thenReturn(rpgPlayer);
            when(rpgPlayer.hasSkill("fireball")).thenReturn(true);

            assertTrue(condition.check(event));
        }
    }

    @Test
    @DisplayName("check: スキル未保持時はfalse")
    void check_MissingSkill_ReturnsFalse() {
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(skillExpr.getSingle(event)).thenReturn("fireball");

        try (MockedStatic<RPGPlugin> mockedStatic = mockStatic(RPGPlugin.class)) {
            mockedStatic.when(RPGPlugin::getInstance).thenReturn(plugin);
            when(plugin.getPlayerManager()).thenReturn(playerManager);
            when(playerManager.getRPGPlayer(playerId)).thenReturn(rpgPlayer);
            when(rpgPlayer.hasSkill("fireball")).thenReturn(false);

            assertFalse(condition.check(event));
        }
    }
}
