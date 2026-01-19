package com.example.rpgplugin.command;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.RPGCommand;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.rpgclass.ClassManager;
import com.example.rpgplugin.rpgclass.RPGClass;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillTreeRegistry;
import com.example.rpgplugin.skill.executor.ActiveSkillExecutor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RPGCommandの単体テスト
 *
 * <p>/rpg コマンドの機能テスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("RPGCommand テスト")
@ExtendWith(MockitoExtension.class)
class RPGCommandTest {

    @Mock
    private CommandSender mockSender;

    @Mock
    private Command mockCommand;

    @Mock
    private Player mockPlayer;

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private SkillManager mockSkillManager;

    @Mock
    private SkillTreeRegistry mockSkillTreeRegistry;

    @Mock
    private ActiveSkillExecutor mockActiveSkillExecutor;

    @Mock
    private ClassManager mockClassManager;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private RPGPlayer mockRpgPlayer;

    private MockedStatic<Bukkit> mockedBukkit;
    private MockedStatic<RPGPlugin> mockedPlugin;
    private RPGCommand rpgCommand;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();

        // Bukkitのモック設定
        mockedBukkit = mockStatic(Bukkit.class);

        // RPGPluginの静的モック設定
        mockedPlugin = mockStatic(RPGPlugin.class);
        mockedPlugin.when(RPGPlugin::getInstance).thenReturn(mockPlugin);

        // プラグイン基本設定
        lenient().when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("TestLogger"));
        lenient().when(mockPlugin.getSkillManager()).thenReturn(mockSkillManager);
        lenient().when(mockPlugin.getActiveSkillExecutor()).thenReturn(mockActiveSkillExecutor);
        lenient().when(mockPlugin.getClassManager()).thenReturn(mockClassManager);
        lenient().when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);

        // プレイヤーモックの基本設定
        lenient().when(mockPlayer.getUniqueId()).thenReturn(testUuid);
        lenient().when(mockPlayer.getName()).thenReturn("TestPlayer");
        lenient().when(mockPlayer.hasPermission(anyString())).thenReturn(true);
        lenient().when(mockPlayer.getLevel()).thenReturn(10);

        // RPGPlayerモック設定
        lenient().when(mockPlayerManager.getRPGPlayer(testUuid)).thenReturn(mockRpgPlayer);
        lenient().when(mockRpgPlayer.getClassId()).thenReturn("warrior");

        // ClassManagerモック設定
        RPGClass testClass = new RPGClass.Builder("warrior")
                .setDisplayName("ウォーリアー")
                .setDescription(Arrays.asList("前線で戦う勇者"))
                .build();
        lenient().when(mockClassManager.getClass("warrior")).thenReturn(Optional.of(testClass));
        lenient().when(mockClassManager.getInitialClasses()).thenReturn(Arrays.asList(testClass));
        lenient().when(mockClassManager.getAllClassIds()).thenReturn(new HashSet<>(Arrays.asList("warrior", "mage")));

        // SkillManagerモック設定
        lenient().when(mockSkillManager.getAllSkillIds()).thenReturn(new HashSet<>(Arrays.asList("fireball", "icicle", "heal")));
        lenient().when(mockSkillManager.getSkillLevel(eq(mockPlayer), anyString())).thenReturn(1);
        lenient().when(mockSkillManager.getTreeRegistry()).thenReturn(mockSkillTreeRegistry);

        rpgCommand = new RPGCommand();
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
        if (mockedPlugin != null) {
            mockedPlugin.close();
        }
    }

    // ==================== 引数なし・ヘルプテスト ====================

    @Test
    @DisplayName("引数なしでプレイヤーがコマンドを実行するとメニューを表示")
    void testOnCommand_NoArgs_Player() {
        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{});

        assertTrue(result);
        verify(mockPlayer, atLeastOnce()).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("コンソールからプレイヤー専用コマンドを実行するとエラー")
    void testOnCommand_Console_PlayerOnlyCommand() {
        boolean result = rpgCommand.onCommand(mockSender, mockCommand, "rpg", new String[]{"stats"});

        assertTrue(result);
        verify(mockSender).sendMessage("このコマンドはプレイヤーのみ使用できます");
    }

    @Test
    @DisplayName("helpサブコマンドでヘルプを表示")
    void testOnCommand_Help() {
        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"help"});

        assertTrue(result);
        verify(mockPlayer, atLeastOnce()).sendMessage(any(Component.class));
    }

    // ==================== reload コマンドテスト ====================

    @Test
    @DisplayName("reload: 権限がない場合はエラー")
    void testOnCommand_Reload_NoPermission() {
        lenient().when(mockSender.hasPermission("rpg.admin")).thenReturn(false);

        boolean result = rpgCommand.onCommand(mockSender, mockCommand, "rpg", new String[]{"reload"});

        assertTrue(result);
        verify(mockSender).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("reload: 権限がある場合はリロード実行")
    void testOnCommand_Reload_WithPermission() {
        doNothing().when(mockPlugin).reloadPlugin();

        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"reload"});

        assertTrue(result);
        verify(mockPlugin).reloadPlugin();
        verify(mockPlayer, atLeastOnce()).sendMessage(any(Component.class));
    }

    // ==================== stats コマンドテスト ====================

    @Test
    @DisplayName("stats: プレースホルダー案内を表示")
    void testOnCommand_Stats() {
        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"stats"});

        assertTrue(result);
        verify(mockPlayer, atLeastOnce()).sendMessage(any(Component.class));
    }

    // ==================== skill コマンドテスト ====================

    @Test
    @DisplayName("skill: スキルツリーGUIを開く")
    void testOnCommand_Skill() {
        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"skill"});

        assertTrue(result);
        verify(mockPlayerManager).getRPGPlayer(testUuid);
    }

    @Test
    @DisplayName("skills: skillのエイリアスとして機能する")
    void testOnCommand_Skills_Alias() {
        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"skills"});

        assertTrue(result);
        verify(mockPlayerManager).getRPGPlayer(testUuid);
    }

    // ==================== cast コマンドテスト ====================

    @Test
    @DisplayName("cast: 引数不足でエラー")
    void testOnCommand_Cast_MissingArgs() {
        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"cast"});

        assertTrue(result);
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("cast: 存在しないスキルでエラー")
    void testOnCommand_Cast_UnknownSkill() {
        when(mockSkillManager.getSkill("unknown")).thenReturn(null);

        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"cast", "unknown"});

        assertTrue(result);
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("cast: 未習得スキルでエラー")
    void testOnCommand_Cast_NotAcquired() {
        Skill mockSkill = mock(Skill.class);
        lenient().when(mockSkill.getId()).thenReturn("fireball");
        lenient().when(mockSkill.getColoredDisplayName()).thenReturn("ファイアボール");
        when(mockSkillManager.getSkill("fireball")).thenReturn(mockSkill);
        when(mockSkillManager.getSkillLevel(mockPlayer, "fireball")).thenReturn(0);

        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"cast", "fireball"});

        assertTrue(result);
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("cast: クールダウン中はエラー")
    void testOnCommand_Cast_OnCooldown() {
        Skill mockSkill = mock(Skill.class);
        lenient().when(mockSkill.getId()).thenReturn("fireball");
        lenient().when(mockSkill.getColoredDisplayName()).thenReturn("ファイアボール");
        when(mockSkillManager.getSkill("fireball")).thenReturn(mockSkill);
        when(mockSkillManager.getSkillLevel(mockPlayer, "fireball")).thenReturn(1);
        when(mockSkillManager.checkCooldown(mockPlayer, "fireball")).thenReturn(false);

        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"cast", "fireball"});

        assertTrue(result);
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("cast: 正常発動")
    void testOnCommand_Cast_Success() {
        Skill mockSkill = mock(Skill.class);
        lenient().when(mockSkill.getId()).thenReturn("fireball");
        lenient().when(mockSkill.getColoredDisplayName()).thenReturn("ファイアボール");
        when(mockSkillManager.getSkill("fireball")).thenReturn(mockSkill);
        when(mockSkillManager.getSkillLevel(mockPlayer, "fireball")).thenReturn(1);
        when(mockSkillManager.checkCooldown(mockPlayer, "fireball")).thenReturn(true);
        when(mockActiveSkillExecutor.execute(mockPlayer, mockSkill, 1)).thenReturn(true);

        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"cast", "fireball"});

        assertTrue(result);
        verify(mockActiveSkillExecutor).execute(mockPlayer, mockSkill, 1);
    }

    @Test
    @DisplayName("cast: スキル発動失敗時にエラーメッセージ")
    void testOnCommand_Cast_ExecuteFailed() {
        Skill mockSkill = mock(Skill.class);
        lenient().when(mockSkill.getId()).thenReturn("fireball");
        lenient().when(mockSkill.getColoredDisplayName()).thenReturn("ファイアボール");
        when(mockSkillManager.getSkill("fireball")).thenReturn(mockSkill);
        when(mockSkillManager.getSkillLevel(mockPlayer, "fireball")).thenReturn(1);
        when(mockSkillManager.checkCooldown(mockPlayer, "fireball")).thenReturn(true);
        when(mockActiveSkillExecutor.execute(mockPlayer, mockSkill, 1)).thenReturn(false);

        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"cast", "fireball"});

        assertTrue(result);
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    // ==================== class コマンドテスト ====================

    @Test
    @DisplayName("class: 引数なしでクラス情報を表示")
    void testOnCommand_Class_NoArgs() {
        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"class"});

        assertTrue(result);
        verify(mockPlayer, atLeastOnce()).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("class list: クラス一覧を表示")
    void testOnCommand_Class_List() {
        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"class", "list"});

        assertTrue(result);
        verify(mockClassManager).getInitialClasses();
    }

    @Test
    @DisplayName("class change: 引数不足でエラー")
    void testOnCommand_ClassChange_MissingArgs() {
        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"class", "change"});

        assertTrue(result);
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("class change: 権限がない場合はエラー")
    void testOnCommand_ClassChange_NoPermission() {
        lenient().when(mockPlayer.hasPermission("rpg.admin.class.change")).thenReturn(false);

        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"class", "change", "warrior"});

        assertTrue(result);
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("class change: レベル引数が不正な場合はエラー")
    void testOnCommand_ClassChange_InvalidLevel() {
        lenient().when(mockPlayer.hasPermission("rpg.admin.class.change")).thenReturn(true);

        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"class", "change", "warrior", "invalid"});

        assertTrue(result);
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("class change: 正常にクラス変更")
    void testOnCommand_ClassChange_Success() {
        lenient().when(mockPlayer.hasPermission("rpg.admin.class.change")).thenReturn(true);
        when(mockClassManager.changeClass(mockPlayer, "warrior", 0)).thenReturn(true);

        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"class", "change", "warrior"});

        assertTrue(result);
        verify(mockClassManager).changeClass(mockPlayer, "warrior", 0);
    }

    @Test
    @DisplayName("class change: 指定レベルでクラス変更")
    void testOnCommand_ClassChange_WithLevel() {
        lenient().when(mockPlayer.hasPermission("rpg.admin.class.change")).thenReturn(true);
        when(mockClassManager.changeClass(mockPlayer, "warrior", 20)).thenReturn(true);

        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"class", "change", "warrior", "20"});

        assertTrue(result);
        verify(mockClassManager).changeClass(mockPlayer, "warrior", 20);
    }

    @Test
    @DisplayName("class change: 存在しないクラスでエラー")
    void testOnCommand_ClassChange_UnknownClass() {
        lenient().when(mockPlayer.hasPermission("rpg.admin.class.change")).thenReturn(true);
        when(mockClassManager.getClass("unknown")).thenReturn(Optional.empty());

        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"class", "change", "unknown"});

        assertTrue(result);
        verify(mockPlayer, atLeastOnce()).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("class change: 失敗時にエラーメッセージ")
    void testOnCommand_ClassChange_Failed() {
        lenient().when(mockPlayer.hasPermission("rpg.admin.class.change")).thenReturn(true);
        when(mockClassManager.changeClass(mockPlayer, "warrior", 0)).thenReturn(false);

        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"class", "change", "warrior"});

        assertTrue(result);
        verify(mockPlayer).sendMessage(any(Component.class));
    }

    // ==================== 管理者用クラス変更コマンドテスト ====================

    @Test
    @DisplayName("admin class change: 権限がない場合はプレイヤーのみエラー")
    void testAdminClassChange_NoPermission() {
        lenient().when(mockSender.hasPermission("rpg.admin.class.change")).thenReturn(false);

        // 管理者コマンドは args.length >= 5 が必要だが、権限がない場合は
        // handleAdminClassChangeCommand が呼ばれず、プレイヤーのみチェックに引っかかる
        boolean result = rpgCommand.onCommand(mockSender, mockCommand, "rpg",
                new String[]{"class", "change", "targetPlayer", "warrior", "0"});

        assertTrue(result);
        verify(mockSender).sendMessage("このコマンドはプレイヤーのみ使用できます");
    }

    @Test
    @DisplayName("admin class change: 引数不足でエラー（プレイヤーのみ）")
    void testAdminClassChange_MissingArgs() {
        lenient().when(mockSender.hasPermission("rpg.admin.class.change")).thenReturn(true);

        // args.length < 5 の場合は管理者用コマンドではなくプレイヤーのみコマンドとして扱われる
        // したがって「プレイヤーのみ使用できます」エラーになる
        boolean result = rpgCommand.onCommand(mockSender, mockCommand, "rpg",
                new String[]{"class", "change", "targetPlayer"});

        assertTrue(result);
        verify(mockSender).sendMessage("このコマンドはプレイヤーのみ使用できます");
    }

    @Test
    @DisplayName("admin class change: オフラインプレイヤーでエラー")
    void testAdminClassChange_OfflinePlayer() {
        lenient().when(mockSender.hasPermission("rpg.admin.class.change")).thenReturn(true);
        mockedBukkit.when(() -> Bukkit.getPlayer("offlinePlayer")).thenReturn(null);

        // args.length >= 5 にする
        boolean result = rpgCommand.onCommand(mockSender, mockCommand, "rpg",
                new String[]{"class", "change", "offlinePlayer", "warrior", "0"});

        assertTrue(result);
        verify(mockSender).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("admin class change: 正常に他プレイヤーのクラス変更")
    void testAdminClassChange_Success() {
        lenient().when(mockSender.hasPermission("rpg.admin.class.change")).thenReturn(true);
        mockedBukkit.when(() -> Bukkit.getPlayer("targetPlayer")).thenReturn(mockPlayer);
        lenient().when(mockPlayer.isOnline()).thenReturn(true);
        when(mockClassManager.changeClass(mockPlayer, "warrior", 0)).thenReturn(true);

        // args.length >= 5 にする
        boolean result = rpgCommand.onCommand(mockSender, mockCommand, "rpg",
                new String[]{"class", "change", "targetPlayer", "warrior", "0"});

        assertTrue(result);
        verify(mockClassManager).changeClass(mockPlayer, "warrior", 0);
    }

    // ==================== 不明なコマンドテスト ====================

    @Test
    @DisplayName("不明なサブコマンドでエラー")
    void testOnCommand_UnknownSubCommand() {
        boolean result = rpgCommand.onCommand(mockPlayer, mockCommand, "rpg", new String[]{"unknown"});

        assertTrue(result);
        verify(mockPlayer, atLeastOnce()).sendMessage(any(Component.class));
    }

    // ==================== Tab補完テスト ====================

    @Test
    @DisplayName("Tab補完: 第一引数でサブコマンド補完")
    void testOnTabComplete_FirstArg() {
        List<String> completions = rpgCommand.onTabComplete(mockPlayer, mockCommand, "rpg", new String[]{""});

        assertTrue(completions.contains("stats"));
        assertTrue(completions.contains("skill"));
        assertTrue(completions.contains("cast"));
        assertTrue(completions.contains("class"));
        assertTrue(completions.contains("help"));
    }

    @Test
    @DisplayName("Tab補完: 管理者権限でreloadを含む")
    void testOnTabComplete_FirstArg_WithAdminPermission() {
        List<String> completions = rpgCommand.onTabComplete(mockPlayer, mockCommand, "rpg", new String[]{""});

        assertTrue(completions.contains("reload"));
    }

    @Test
    @DisplayName("Tab補完: cast後にスキルID補完")
    void testOnTabComplete_AfterCast() {
        List<String> completions = rpgCommand.onTabComplete(mockPlayer, mockCommand, "rpg", new String[]{"cast", ""});

        assertTrue(completions.contains("fireball"));
        assertTrue(completions.contains("icicle"));
        assertTrue(completions.contains("heal"));
    }

    @Test
    @DisplayName("Tab補完: class後にサブコマンド補完")
    void testOnTabComplete_AfterClass() {
        List<String> completions = rpgCommand.onTabComplete(mockPlayer, mockCommand, "rpg", new String[]{"class", ""});

        assertTrue(completions.contains("list"));
    }

    @Test
    @DisplayName("Tab補完: class change後にクラスID補完")
    void testOnTabComplete_AfterClassChange() {
        List<String> completions = rpgCommand.onTabComplete(mockPlayer, mockCommand, "rpg", new String[]{"class", "change", ""});

        assertTrue(completions.contains("warrior"));
        assertTrue(completions.contains("mage"));
    }

    @Test
    @DisplayName("Tab補完: help後にカテゴリ補完")
    void testOnTabComplete_AfterHelp() {
        List<String> completions = rpgCommand.onTabComplete(mockPlayer, mockCommand, "rpg", new String[]{"help", ""});

        assertTrue(completions.contains("class"));
        assertTrue(completions.contains("skill"));
    }

    @Test
    @DisplayName("Tab補完: フィルタリングが正しく動作")
    void testOnTabComplete_WithFilter() {
        List<String> completions = rpgCommand.onTabComplete(mockPlayer, mockCommand, "rpg", new String[]{"s"});

        assertTrue(completions.contains("stats"));
        assertTrue(completions.contains("skill"));
        assertTrue(completions.contains("skills"));
    }
}
