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
@DisplayName("CondIsRPGClass テスト")
class CondIsRPGClassTest {

    @Mock
    private Expression<Player> playerExpr;

    @Mock
    private Expression<String> classExpr;

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

    private CondIsRPGClass condition;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        condition = new CondIsRPGClass();
        condition.init(new Expression[]{playerExpr, classExpr}, 0, null, null);
        playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
    }

    @Test
    @DisplayName("check: プレイヤーがnullの場合はfalse")
    void check_PlayerNull_ReturnsFalse() {
        when(playerExpr.getSingle(event)).thenReturn(null);
        when(classExpr.getSingle(event)).thenReturn("warrior");

        assertFalse(condition.check(event));
    }

    @Test
    @DisplayName("check: クラス名がnullの場合はfalse")
    void check_ClassNull_ReturnsFalse() {
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(classExpr.getSingle(event)).thenReturn(null);

        assertFalse(condition.check(event));
    }

    @Test
    @DisplayName("check: プラグイン取得失敗の場合はfalse")
    void check_PluginNull_ReturnsFalse() {
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(classExpr.getSingle(event)).thenReturn("warrior");

        try (MockedStatic<RPGPlugin> mockedStatic = mockStatic(RPGPlugin.class)) {
            mockedStatic.when(RPGPlugin::getInstance).thenReturn(null);

            assertFalse(condition.check(event));
        }
    }

    @Test
    @DisplayName("check: RPGPlayer未登録の場合はfalse")
    void check_RpgPlayerNull_ReturnsFalse() {
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(classExpr.getSingle(event)).thenReturn("warrior");

        try (MockedStatic<RPGPlugin> mockedStatic = mockStatic(RPGPlugin.class)) {
            mockedStatic.when(RPGPlugin::getInstance).thenReturn(plugin);
            when(plugin.getPlayerManager()).thenReturn(playerManager);
            when(playerManager.getRPGPlayer(playerId)).thenReturn(null);

            assertFalse(condition.check(event));
        }
    }

    @Test
    @DisplayName("check: クラス一致ならtrue (大文字小文字無視)")
    void check_ClassMatch_ReturnsTrue() {
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(classExpr.getSingle(event)).thenReturn("warrior");

        try (MockedStatic<RPGPlugin> mockedStatic = mockStatic(RPGPlugin.class)) {
            mockedStatic.when(RPGPlugin::getInstance).thenReturn(plugin);
            when(plugin.getPlayerManager()).thenReturn(playerManager);
            when(playerManager.getRPGPlayer(playerId)).thenReturn(rpgPlayer);
            when(rpgPlayer.getClassId()).thenReturn("Warrior");

            assertTrue(condition.check(event));
        }
    }

    @Test
    @DisplayName("check: クラス不一致ならfalse")
    void check_ClassMismatch_ReturnsFalse() {
        when(playerExpr.getSingle(event)).thenReturn(player);
        when(classExpr.getSingle(event)).thenReturn("warrior");

        try (MockedStatic<RPGPlugin> mockedStatic = mockStatic(RPGPlugin.class)) {
            mockedStatic.when(RPGPlugin::getInstance).thenReturn(plugin);
            when(plugin.getPlayerManager()).thenReturn(playerManager);
            when(playerManager.getRPGPlayer(playerId)).thenReturn(rpgPlayer);
            when(rpgPlayer.getClassId()).thenReturn("mage");

            assertFalse(condition.check(event));
        }
    }
}
