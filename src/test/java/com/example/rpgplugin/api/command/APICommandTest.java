package com.example.rpgplugin.api.command;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * APICommandの単体テスト
 *
 * <p>/rpgapi コマンドの機能テスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("APICommand テスト")
@ExtendWith(MockitoExtension.class)
class APICommandTest {

    @Mock
    private CommandSender mockSender;

    @Mock
    private Player mockPlayer;

    @Mock
    private Command mockCommand;

    private APICommand apiCommand;

    @BeforeEach
    void setUp() {
        apiCommand = new APICommand();

        // プレイヤーモックの基本設定
        lenient().when(mockPlayer.getName()).thenReturn("TestPlayer");
        lenient().when(mockPlayer.hasPermission("rpgplugin.api")).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        // クリーンアップ
    }

    // ==================== onCommand 正常系テスト ====================

    @Test
    @DisplayName("onCommand: 引数なしでヘルプを表示")
    void testOnCommand_NoArgs_DisplaysHelp() {
        boolean result = apiCommand.onCommand(mockSender, mockCommand, "rpgapi", new String[]{});

        assertThat(result).isTrue();
        verify(mockSender, atLeastOnce()).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("onCommand: help引数でヘルプを表示")
    void testOnCommand_HelpArgument_DisplaysHelp() {
        boolean result = apiCommand.onCommand(mockSender, mockCommand, "rpgapi", new String[]{"help"});

        assertThat(result).isTrue();
        verify(mockSender, atLeastOnce()).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("onCommand: HELP大文字でもヘルプを表示")
    void testOnCommand_HelpUpperCase_DisplaysHelp() {
        boolean result = apiCommand.onCommand(mockSender, mockCommand, "rpgapi", new String[]{"HELP"});

        assertThat(result).isTrue();
        verify(mockSender, atLeastOnce()).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("onCommand: コンソールからヘルプ表示成功")
    void testOnCommand_Console_DisplaysHelp() {
        boolean result = apiCommand.onCommand(mockSender, mockCommand, "rpgapi", new String[]{});

        assertThat(result).isTrue();
        verify(mockSender, atLeastOnce()).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("onCommand: 権限を持つプレイヤーがヘルプを表示")
    void testOnCommand_PlayerWithPermission_DisplaysHelp() {
        when(mockPlayer.hasPermission("rpgplugin.api")).thenReturn(true);

        boolean result = apiCommand.onCommand(mockPlayer, mockCommand, "rpgapi", new String[]{});

        assertThat(result).isTrue();
        verify(mockPlayer, atLeastOnce()).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("onCommand: 不明な引数でもヘルプを表示")
    void testOnCommand_UnknownArgument_DisplaysHelp() {
        boolean result = apiCommand.onCommand(mockSender, mockCommand, "rpgapi", new String[]{"unknown"});

        assertThat(result).isTrue();
        verify(mockSender, atLeastOnce()).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("onCommand: 複数の引数でもヘルプを表示")
    void testOnCommand_MultipleArguments_DisplaysHelp() {
        boolean result = apiCommand.onCommand(mockSender, mockCommand, "rpgapi", new String[]{"arg1", "arg2"});

        assertThat(result).isTrue();
        verify(mockSender, atLeastOnce()).sendMessage(any(Component.class));
    }

    // ==================== onCommand 異常系テスト ====================

    @Test
    @DisplayName("onCommand: 権限がないプレイヤーはエラーメッセージ")
    void testOnCommand_PlayerWithoutPermission_ShowsError() {
        when(mockPlayer.hasPermission("rpgplugin.api")).thenReturn(false);

        boolean result = apiCommand.onCommand(mockPlayer, mockCommand, "rpgapi", new String[]{});

        assertThat(result).isTrue();
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    // ==================== onTabComplete テスト ====================

    @Test
    @DisplayName("onTabComplete: 空配列で空リストを返す")
    void testOnTabComplete_EmptyArgs_ReturnsEmpty() {
        List<String> completions = apiCommand.onTabComplete(mockSender, mockCommand, "rpgapi", new String[]{});

        assertThat(completions).isNotNull();
        assertThat(completions).isEmpty();
    }

    @Test
    @DisplayName("onTabComplete: 第一引数位置でhelpを補完")
    void testOnTabComplete_FirstArg_ReturnsHelp() {
        List<String> completions = apiCommand.onTabComplete(mockSender, mockCommand, "rpgapi", new String[]{""});

        assertThat(completions).isNotNull();
        assertThat(completions).hasSize(1);
        assertThat(completions).containsExactly("help");
    }

    @Test
    @DisplayName("onTabComplete: 第二引数以降は空リスト")
    void testOnTabComplete_SecondArg_ReturnsEmpty() {
        List<String> completions = apiCommand.onTabComplete(mockSender, mockCommand, "rpgapi", new String[]{"help", ""});

        assertThat(completions).isNotNull();
        assertThat(completions).isEmpty();
    }

    @Test
    @DisplayName("onTabComplete: 部分一致ヒントなし")
    void testOnTabComplete_PartialMatch_NoFiltering() {
        List<String> completions = apiCommand.onTabComplete(mockSender, mockCommand, "rpgapi", new String[]{"h"});

        assertThat(completions).isNotNull();
        assertThat(completions).hasSize(1);
        assertThat(completions).containsExactly("help");
    }

    @Test
    @DisplayName("onTabComplete: 複数引数で空リスト")
    void testOnTabComplete_MultipleArgs_ReturnsEmpty() {
        List<String> completions = apiCommand.onTabComplete(mockSender, mockCommand, "rpgapi", new String[]{"arg1", "arg2", "arg3"});

        assertThat(completions).isNotNull();
        assertThat(completions).isEmpty();
    }

    // ==================== sendHelp テスト（ヘルプメッセージ） ====================

    @Test
    @DisplayName("sendHelp: ヘルプメッセージが送信される")
    void testSendHelp_MessagesSent() {
        apiCommand.onCommand(mockSender, mockCommand, "rpgapi", new String[]{});

        // 複数行のヘルプメッセージが送信されることを確認
        verify(mockSender, atLeast(9)).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("sendHelp: プレイヤーにヘルプが送信される")
    void testSendHelp_ToPlayer_MessagesSent() {
        when(mockPlayer.hasPermission("rpgplugin.api")).thenReturn(true);

        apiCommand.onCommand(mockPlayer, mockCommand, "rpgapi", new String[]{"help"});

        verify(mockPlayer, atLeast(9)).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("sendHelp: コンソールにヘルプが送信される")
    void testSendHelp_ToConsole_MessagesSent() {
        apiCommand.onCommand(mockSender, mockCommand, "rpgapi", new String[]{});

        verify(mockSender, atLeast(9)).sendMessage(any(Component.class));
    }

    // ==================== 権限チェックテスト ====================

    @Test
    @DisplayName("権限チェック: rpgplugin.api権限がないとエラー")
    void testPermissionCheck_NoPermission_ShowsErrorMessage() {
        when(mockPlayer.hasPermission("rpgplugin.api")).thenReturn(false);

        apiCommand.onCommand(mockPlayer, mockCommand, "rpgapi", new String[]{});

        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("権限チェック: rpgplugin.api権限があるとヘルプ表示")
    void testPermissionCheck_WithPermission_DisplaysHelp() {
        when(mockPlayer.hasPermission("rpgplugin.api")).thenReturn(true);

        apiCommand.onCommand(mockPlayer, mockCommand, "rpgapi", new String[]{});

        verify(mockPlayer, atLeast(9)).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("権限チェック: コンソールは権限なしで実行可能")
    void testPermissionCheck_Console_NoPermissionRequired() {
        apiCommand.onCommand(mockSender, mockCommand, "rpgapi", new String[]{});

        verify(mockSender, atLeast(9)).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("権限チェック: help引数でも権限チェック実行")
    void testPermissionCheck_HelpArgument_ChecksPermission() {
        when(mockPlayer.hasPermission("rpgplugin.api")).thenReturn(false);

        apiCommand.onCommand(mockPlayer, mockCommand, "rpgapi", new String[]{"help"});

        verify(mockPlayer).sendMessage(any(Component.class));
    }
}
