package com.example.rpgplugin.api.skript.conditions;

import ch.njol.skript.lang.Expression;
import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
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
@DisplayName("CondRPGStatAbove テスト")
class CondRPGStatAboveTest {

    @Mock
    private Expression<Player> playerExpr;

    @Mock
    private Expression<String> statNameExpr;

    @Mock
    private Expression<Number> valueExpr;

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

    private CondRPGStatAbove condition;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        condition = new CondRPGStatAbove();
        condition.init(new Expression[]{playerExpr, statNameExpr, valueExpr}, 0, null, null);
        playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(rpgPlayer.getStatManager()).thenReturn(statManager);
    }

    @Test
    @DisplayName("check: プレイヤーがnullの場合はfalse")
    void check_PlayerNull_ReturnsFalse() {
        when(playerExpr.getSingle(event)).thenReturn(null);
        when(statNameExpr.getSingle(event)).thenReturn("STR");
        when(valueExpr.getSingle(event)).thenReturn(10);

        assertFalse(condition.check(event));
    }

    @Test
    @DisplayName("check: ステータス名が不正ならfalse")
    void check_InvalidStat_ReturnsFalse() {
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(statNameExpr.getSingle(event)).thenReturn("UNKNOWN");
        when(valueExpr.getSingle(event)).thenReturn(10);

        assertFalse(condition.check(event));
    }

    @Test
    @DisplayName("check: 値がnullの場合はfalse")
    void check_ValueNull_ReturnsFalse() {
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(statNameExpr.getSingle(event)).thenReturn("STR");
        when(valueExpr.getSingle(event)).thenReturn(null);

        assertFalse(condition.check(event));
    }

    @Test
    @DisplayName("check: プラグイン取得失敗の場合はfalse")
    void check_PluginNull_ReturnsFalse() {
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(statNameExpr.getSingle(event)).thenReturn("STR");
        when(valueExpr.getSingle(event)).thenReturn(10);

        try (MockedStatic<RPGPlugin> mockedStatic = mockStatic(RPGPlugin.class)) {
            mockedStatic.when(RPGPlugin::getInstance).thenReturn(null);

            assertFalse(condition.check(event));
        }
    }

    @Test
    @DisplayName("check: ステータスが閾値以上ならtrue")
    void check_StatAbove_ReturnsTrue() {
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(statNameExpr.getSingle(event)).thenReturn("STR");
        when(valueExpr.getSingle(event)).thenReturn(10);

        try (MockedStatic<RPGPlugin> mockedStatic = mockStatic(RPGPlugin.class)) {
            mockedStatic.when(RPGPlugin::getInstance).thenReturn(plugin);
            when(plugin.getPlayerManager()).thenReturn(playerManager);
            when(playerManager.getRPGPlayer(playerId)).thenReturn(rpgPlayer);
            when(statManager.getFinalStat(Stat.STRENGTH)).thenReturn(15);

            assertTrue(condition.check(event));
        }
    }

    @Test
    @DisplayName("check: ステータス不足ならfalse (パターン2)")
    void check_StatBelow_ReturnsFalse_WithAltPattern() {
        CondRPGStatAbove altCondition = new CondRPGStatAbove();
        altCondition.init(new Expression[]{statNameExpr, playerExpr, valueExpr}, 1, null, null);

        when(playerExpr.getSingle(event)).thenReturn(player);
        when(statNameExpr.getSingle(event)).thenReturn("DEX");
        when(valueExpr.getSingle(event)).thenReturn(20);

        try (MockedStatic<RPGPlugin> mockedStatic = mockStatic(RPGPlugin.class)) {
            mockedStatic.when(RPGPlugin::getInstance).thenReturn(plugin);
            when(plugin.getPlayerManager()).thenReturn(playerManager);
            when(playerManager.getRPGPlayer(playerId)).thenReturn(rpgPlayer);
            when(statManager.getFinalStat(Stat.DEXTERITY)).thenReturn(10);

            assertFalse(altCondition.check(event));
        }
    }
}
