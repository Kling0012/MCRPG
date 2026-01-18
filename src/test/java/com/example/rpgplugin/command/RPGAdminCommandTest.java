package com.example.rpgplugin.command;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.rpgclass.ClassLoader;
import com.example.rpgplugin.rpgclass.ClassManager;
import com.example.rpgplugin.rpgclass.RPGClass;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillLoader;
import com.example.rpgplugin.skill.SkillManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * RPGAdminCommandの単体テスト
 *
 * <p>RPG管理コマンドの機能テスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("RPGAdminCommand テスト")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RPGAdminCommandTest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private ClassManager mockClassManager;

    @Mock
    private SkillManager mockSkillManager;

    @Mock
    private ClassLoader mockClassLoader;

    @Mock
    private SkillLoader mockSkillLoader;

    @Mock
    private CommandSender mockSender;

    @Mock
    private Command mockCommand;

    @Mock
    private ConsoleCommandSender mockConsoleSender;

    private Logger logger;
    private RPGAdminCommand adminCommand;

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger("TestLogger");

        // プラグインモックの基本設定
        lenient().when(mockPlugin.getLogger()).thenReturn(logger);

        adminCommand = new RPGAdminCommand(
                mockPlugin,
                mockClassManager,
                mockSkillManager,
                mockClassLoader,
                mockSkillLoader
        );
    }

    // ==================== 権限チェックテスト ====================

    @Test
    @DisplayName("権限がない場合はエラーメッセージを返す")
    void testOnCommand_NoPermission() {
        when(mockSender.hasPermission("rpgplugin.admin")).thenReturn(false);

        boolean result = adminCommand.onCommand(mockSender, mockCommand, "rpgadmin", new String[]{});

        assertTrue(result);
        verify(mockSender).sendMessage("§cこのコマンドを実行する権限がありません。");
    }

    @Test
    @DisplayName("権限がある場合はコマンドを実行できる")
    void testOnCommand_WithPermission() {
        when(mockSender.hasPermission("rpgplugin.admin")).thenReturn(true);

        boolean result = adminCommand.onCommand(mockSender, mockCommand, "rpgadmin", new String[]{});

        assertTrue(result);
        // ヘルプが表示される
        verify(mockSender, atLeastOnce()).sendMessage(anyString());
    }

    // ==================== ヘルプコマンドテスト ====================

    @Test
    @DisplayName("引数なしでヘルプを表示")
    void testOnCommand_NoArgs_ShowsHelp() {
        when(mockSender.hasPermission("rpgplugin.admin")).thenReturn(true);

        adminCommand.onCommand(mockSender, mockCommand, "rpgadmin", new String[]{});

        verify(mockSender).sendMessage(contains("RPG管理コマンド ヘルプ"));
    }

    @Test
    @DisplayName("helpサブコマンドでヘルプを表示")
    void testOnCommand_HelpSubCommand_ShowsHelp() {
        when(mockSender.hasPermission("rpgplugin.admin")).thenReturn(true);

        adminCommand.onCommand(mockSender, mockCommand, "rpgadmin", new String[]{"help"});

        verify(mockSender).sendMessage(contains("RPG管理コマンド ヘルプ"));
    }

    // ==================== reload classes コマンドテスト ====================

    @Test
    @DisplayName("reload classes: 正常にリロードできる")
    void testReloadClasses_Success() {
        when(mockSender.hasPermission("rpgplugin.admin")).thenReturn(true);

        // モックの設定
        Map<String, RPGClass> classes = new HashMap<>();
        classes.put("warrior", new RPGClass.Builder("warrior").setDisplayName("ウォーリアー").build());
        classes.put("mage", new RPGClass.Builder("mage").setDisplayName("メイジ").build());
        when(mockClassLoader.loadAllClasses()).thenReturn(classes);

        ClassManager.ReloadResult reloadResult = mock(ClassManager.ReloadResult.class);
        when(reloadResult.getLoadedClassCount()).thenReturn(2);
        when(reloadResult.hasRemovedClasses()).thenReturn(false);
        when(mockClassManager.reloadWithCleanup(any(Map.class))).thenReturn(reloadResult);

        adminCommand.onCommand(mockSender, mockCommand, "rpgadmin", new String[]{"reload", "classes"});

        verify(mockClassLoader).loadAllClasses();
        verify(mockClassManager).reloadWithCleanup(any(Map.class));
        verify(mockSender).sendMessage(contains("職業クラスをリロードしました"));
        verify(mockSender).sendMessage(contains("読み込み: §f2 クラス"));
    }

    @Test
    @DisplayName("reload classes: 削除されたクラスがある場合は通知する")
    void testReloadClasses_WithRemovedClasses() {
        when(mockSender.hasPermission("rpgplugin.admin")).thenReturn(true);

        Map<String, RPGClass> classes = new HashMap<>();
        classes.put("warrior", new RPGClass.Builder("warrior").setDisplayName("ウォーリアー").build());
        when(mockClassLoader.loadAllClasses()).thenReturn(classes);

        ClassManager.ReloadResult reloadResult = mock(ClassManager.ReloadResult.class);
        when(reloadResult.getLoadedClassCount()).thenReturn(1);
        when(reloadResult.hasRemovedClasses()).thenReturn(true);
        when(reloadResult.getRemovedClasses()).thenReturn(new HashSet<>(Arrays.asList("mage", "archer")));
        when(reloadResult.getAffectedPlayerCount()).thenReturn(5);
        when(mockClassManager.reloadWithCleanup(any(Map.class))).thenReturn(reloadResult);

        adminCommand.onCommand(mockSender, mockCommand, "rpgadmin", new String[]{"reload", "classes"});

        verify(mockSender).sendMessage(contains("職業クラスをリロードしました"));
        verify(mockSender).sendMessage(contains("読み込み: §f1 クラス"));
        verify(mockSender).sendMessage(contains("削除: §f2 クラス"));
        verify(mockSender).sendMessage(contains("影響を受けたプレイヤー: §f5 人"));
    }

    @Test
    @DisplayName("reload classes: 例外が発生した場合はエラーメッセージを表示")
    void testReloadClasses_Exception() {
        when(mockSender.hasPermission("rpgplugin.admin")).thenReturn(true);

        when(mockClassLoader.loadAllClasses()).thenThrow(new RuntimeException("Load error"));

        adminCommand.onCommand(mockSender, mockCommand, "rpgadmin", new String[]{"reload", "classes"});

        verify(mockSender).sendMessage(contains("職業クラスのリロードに失敗しました"));
    }

    @Test
    @DisplayName("reload: 引数不足の場合は使用方法を表示")
    void testReload_MissingArgs() {
        when(mockSender.hasPermission("rpgplugin.admin")).thenReturn(true);

        adminCommand.onCommand(mockSender, mockCommand, "rpgadmin", new String[]{"reload"});

        verify(mockSender).sendMessage(contains("使用方法: /rpgadmin reload <classes|skills|all>"));
    }

    // ==================== reload skills コマンドテスト ====================

    @Test
    @DisplayName("reload skills: 正常にリロードできる")
    void testReloadSkills_Success() {
        when(mockSender.hasPermission("rpgplugin.admin")).thenReturn(true);

        Skill mockSkill1 = mock(Skill.class);
        Skill mockSkill2 = mock(Skill.class);
        when(mockSkill1.getId()).thenReturn("fireball");
        when(mockSkill2.getId()).thenReturn("icicle");

        List<Skill> skills = Arrays.asList(mockSkill1, mockSkill2);
        when(mockSkillLoader.loadAllSkills()).thenReturn(skills);

        SkillManager.ReloadResult reloadResult = mock(SkillManager.ReloadResult.class);
        when(reloadResult.getLoadedSkillCount()).thenReturn(2);
        when(reloadResult.hasRemovedSkills()).thenReturn(false);
        when(mockSkillManager.reloadWithCleanup(any(Map.class))).thenReturn(reloadResult);

        adminCommand.onCommand(mockSender, mockCommand, "rpgadmin", new String[]{"reload", "skills"});

        verify(mockSkillLoader).loadAllSkills();
        verify(mockSkillManager).reloadWithCleanup(any(Map.class));
        verify(mockSender).sendMessage(contains("スキルをリロードしました"));
        verify(mockSender).sendMessage(contains("読み込み: §f2 スキル"));
    }

    @Test
    @DisplayName("reload skills: 削除されたスキルがある場合は通知する")
    void testReloadSkills_WithRemovedSkills() {
        when(mockSender.hasPermission("rpgplugin.admin")).thenReturn(true);

        Skill mockSkill = mock(Skill.class);
        when(mockSkill.getId()).thenReturn("fireball");
        when(mockSkillLoader.loadAllSkills()).thenReturn(Arrays.asList(mockSkill));

        SkillManager.ReloadResult reloadResult = mock(SkillManager.ReloadResult.class);
        when(reloadResult.getLoadedSkillCount()).thenReturn(1);
        when(reloadResult.hasRemovedSkills()).thenReturn(true);
        when(reloadResult.getRemovedSkills()).thenReturn(new HashSet<>(Arrays.asList("icicle", "heal")));
        when(reloadResult.getAffectedPlayerCount()).thenReturn(3);
        when(reloadResult.getTotalSkillsRemoved()).thenReturn(7);
        when(mockSkillManager.reloadWithCleanup(any(Map.class))).thenReturn(reloadResult);

        adminCommand.onCommand(mockSender, mockCommand, "rpgadmin", new String[]{"reload", "skills"});

        verify(mockSender).sendMessage(contains("スキルをリロードしました"));
        verify(mockSender).sendMessage(contains("読み込み: §f1 スキル"));
        verify(mockSender).sendMessage(contains("削除: §f2 スキル"));
        verify(mockSender).sendMessage(contains("影響を受けたプレイヤー: §f3 人"));
        verify(mockSender).sendMessage(contains("削除されたスキルエントリ: §f7 件"));
    }

    @Test
    @DisplayName("reload skills: 例外が発生した場合はエラーメッセージを表示")
    void testReloadSkills_Exception() {
        when(mockSender.hasPermission("rpgplugin.admin")).thenReturn(true);

        when(mockSkillLoader.loadAllSkills()).thenThrow(new RuntimeException("Skill load error"));

        adminCommand.onCommand(mockSender, mockCommand, "rpgadmin", new String[]{"reload", "skills"});

        verify(mockSender).sendMessage(contains("スキルのリロードに失敗しました"));
    }

    // ==================== reload all コマンドテスト ====================

    @Test
    @DisplayName("reload all: 正常にリロードできる")
    void testReloadAll_Success() {
        when(mockSender.hasPermission("rpgplugin.admin")).thenReturn(true);

        // クラス関連のモック
        Map<String, RPGClass> classes = new HashMap<>();
        classes.put("warrior", new RPGClass.Builder("warrior").setDisplayName("ウォーリアー").build());
        when(mockClassLoader.loadAllClasses()).thenReturn(classes);

        ClassManager.ReloadResult classResult = mock(ClassManager.ReloadResult.class);
        when(classResult.getLoadedClassCount()).thenReturn(1);
        when(classResult.hasRemovedClasses()).thenReturn(false);
        when(mockClassManager.reloadWithCleanup(any(Map.class))).thenReturn(classResult);

        // スキル関連のモック
        Skill mockSkill = mock(Skill.class);
        when(mockSkill.getId()).thenReturn("fireball");
        when(mockSkillLoader.loadAllSkills()).thenReturn(Arrays.asList(mockSkill));

        SkillManager.ReloadResult skillResult = mock(SkillManager.ReloadResult.class);
        when(skillResult.getLoadedSkillCount()).thenReturn(1);
        when(skillResult.hasRemovedSkills()).thenReturn(false);
        when(mockSkillManager.reloadWithCleanup(any(Map.class))).thenReturn(skillResult);

        adminCommand.onCommand(mockSender, mockCommand, "rpgadmin", new String[]{"reload", "all"});

        verify(mockSender).sendMessage(contains("全てのデータをリロードしました"));
        verify(mockSender).sendMessage(contains("職業クラス】"));
        verify(mockSender).sendMessage(contains("スキル】"));
    }

    @Test
    @DisplayName("reload all: 例外が発生した場合はエラーメッセージを表示")
    void testReloadAll_Exception() {
        when(mockSender.hasPermission("rpgplugin.admin")).thenReturn(true);

        when(mockClassLoader.loadAllClasses()).thenThrow(new RuntimeException("Load error"));

        adminCommand.onCommand(mockSender, mockCommand, "rpgadmin", new String[]{"reload", "all"});

        verify(mockSender).sendMessage(contains("リロードに失敗しました"));
    }

    // ==================== 不明なサブコマンドテスト ====================

    @Test
    @DisplayName("不明なサブコマンドはエラーメッセージを表示")
    void testOnCommand_UnknownSubCommand() {
        when(mockSender.hasPermission("rpgplugin.admin")).thenReturn(true);

        adminCommand.onCommand(mockSender, mockCommand, "rpgadmin", new String[]{"unknown"});

        verify(mockSender).sendMessage(contains("不明なサブコマンド: unknown"));
    }

    @Test
    @DisplayName("不明なreloadターゲットはエラーメッセージを表示")
    void testReload_UnknownTarget() {
        when(mockSender.hasPermission("rpgplugin.admin")).thenReturn(true);

        adminCommand.onCommand(mockSender, mockCommand, "rpgadmin", new String[]{"reload", "unknown"});

        verify(mockSender).sendMessage(contains("不明なリロードターゲット: unknown"));
    }

    // ==================== Tab補完テスト ====================

    @Test
    @DisplayName("Tab補完: 第一引数でサブコマンドを補完")
    void testOnTabComplete_FirstArg() {
        List<String> completions = adminCommand.onTabComplete(mockSender, mockCommand, "rpgadmin", new String[]{""});

        assertEquals(2, completions.size());
        assertTrue(completions.contains("reload"));
        assertTrue(completions.contains("help"));
    }

    @Test
    @DisplayName("Tab補完: reloadサブコマンド後にターゲットを補完")
    void testOnTabComplete_AfterReload() {
        List<String> completions = adminCommand.onTabComplete(mockSender, mockCommand, "rpgadmin", new String[]{"reload", ""});

        assertEquals(3, completions.size());
        assertTrue(completions.contains("classes"));
        assertTrue(completions.contains("skills"));
        assertTrue(completions.contains("all"));
    }

    @Test
    @DisplayName("Tab補完: 入力フィルタリングが正しく動作する")
    void testOnTabComplete_WithInputFilter() {
        List<String> completions = adminCommand.onTabComplete(mockSender, mockCommand, "rpgadmin", new String[]{"re"});

        assertEquals(1, completions.size());
        assertTrue(completions.contains("reload"));
    }

    @Test
    @DisplayName("Tab補完: reload後にclで始まるターゲットを補完")
    void testOnTabComplete_AfterReloadWithFilter() {
        List<String> completions = adminCommand.onTabComplete(mockSender, mockCommand, "rpgadmin", new String[]{"reload", "cl"});

        assertEquals(1, completions.size());
        assertTrue(completions.contains("classes"));
    }

    @Test
    @DisplayName("Tab補完: 第三引数以降は空リスト")
    void testOnTabComplete_ThirdArg() {
        List<String> completions = adminCommand.onTabComplete(mockSender, mockCommand, "rpgadmin", new String[]{"reload", "classes", "extra"});

        assertTrue(completions.isEmpty());
    }

    @Test
    @DisplayName("Tab補完: 大文字小文字を区別しないフィルタリング")
    void testOnTabComplete_CaseInsensitiveFilter() {
        List<String> completions = adminCommand.onTabComplete(mockSender, mockCommand, "rpgadmin", new String[]{"RE"});

        assertEquals(1, completions.size());
        assertTrue(completions.contains("reload"));
    }
}
