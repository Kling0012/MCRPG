package com.example.rpgplugin.player;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.rpgclass.ClassManager;
import com.example.rpgplugin.rpgclass.RPGClass;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.MockedStatic;

import java.util.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * MP管理クラスのテストクラス
 *
 * <p>Phase11-9: MP管理の包括的なテスト覆盖</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MP管理 テスト")
class ManaManagerTest {

    @Mock
    private RPGPlugin plugin;

    @Mock
    private PlayerManager playerManager;

    @Mock
    private ClassManager classManager;

    @Mock
    private Logger logger;

    @Mock
    private BukkitScheduler scheduler;

    @Mock
    private BukkitTask task;

    private ManaManager manaManager;
    private UUID testUuid;
    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();

        when(plugin.getLogger()).thenReturn(logger);

        // Bukkit.getScheduler()のモック設定
        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

        when(scheduler.runTaskTimer(eq(plugin), any(Runnable.class), anyLong(), anyLong()))
                .thenReturn(task);
        when(task.isCancelled()).thenReturn(true);

        manaManager = new ManaManager(plugin, playerManager, classManager);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    // ===== コンストラクタテスト =====

    @Test
    @DisplayName("コンストラクタ: 正常に初期化される")
    void testConstructor() {
        assertNotNull(manaManager);
        assertEquals(1.0, ManaManager.getDefaultManaRegen(), 0.001);
    }

    // ===== start/stopテスト =====

    @Test
    @DisplayName("start: MP回復タスクが開始される")
    void testStart() {
        when(task.isCancelled()).thenReturn(false);

        manaManager.start();

        verify(scheduler).runTaskTimer(eq(plugin), any(Runnable.class), eq(20L), eq(20L));
        verify(logger).info("Mana regeneration task started");
    }

    @Test
    @DisplayName("start: 既に実行中の場合は警告がログされる")
    void testStartWhenAlreadyRunning() {
        when(task.isCancelled()).thenReturn(false);

        manaManager.start();
        manaManager.start(); // 二回目

        verify(logger).warning("Mana regeneration task is already running");
    }

    @Test
    @DisplayName("stop: MP回復タスクが停止される")
    void testStop() {
        when(task.isCancelled()).thenReturn(false);

        manaManager.start();
        manaManager.stop();

        verify(task).cancel();
        verify(logger).info("Mana regeneration task stopped");
    }

    @Test
    @DisplayName("stop: 既に停止している場合は何もしない")
    void testStopWhenAlreadyStopped() {
        // タスクが開始されていない状態
        manaManager.stop();

        verify(task, never()).cancel();
    }

    // ===== カスタム回復量テスト =====

    @Test
    @DisplayName("setCustomRegenRate: カスタム回復量が設定される")
    void testSetCustomRegenRate() {
        manaManager.setCustomRegenRate(testUuid, 2.5);

        assertEquals(2.5, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);
    }

    @Test
    @DisplayName("setCustomRegenRate: 0以下で設定解除される")
    void testSetCustomRegenRateZeroRemoves() {
        manaManager.setCustomRegenRate(testUuid, 2.5);
        assertEquals(2.5, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);

        manaManager.setCustomRegenRate(testUuid, 0);
        // デフォルト値に戻る
        assertNotEquals(2.5, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);
    }

    @Test
    @DisplayName("setCustomRegenRate: 負値で設定解除される")
    void testSetCustomRegenRateNegativeRemoves() {
        manaManager.setCustomRegenRate(testUuid, 2.5);
        manaManager.setCustomRegenRate(testUuid, -1.0);

        assertNotEquals(2.5, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);
    }

    @Test
    @DisplayName("clearCustomRegenRate: カスタム回復量が解除される")
    void testClearCustomRegenRate() {
        manaManager.setCustomRegenRate(testUuid, 2.5);
        assertEquals(2.5, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);

        manaManager.clearCustomRegenRate(testUuid);

        assertNotEquals(2.5, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);
    }

    // ===== 一時ボーナステスト =====

    @Test
    @DisplayName("addTemporaryBonus: ボーナスが加算される")
    void testAddTemporaryBonus() {
        // デフォルト1.0 + ボーナス0.5 = 1.5
        manaManager.addTemporaryBonus(testUuid, 0.5);

        assertEquals(1.5, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);
    }

    @Test
    @DisplayName("addTemporaryBonus: 複数回呼び出しで累積する")
    void testAddTemporaryBonusAccumulates() {
        manaManager.addTemporaryBonus(testUuid, 0.5);
        assertEquals(1.5, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);

        manaManager.addTemporaryBonus(testUuid, 0.3);
        assertEquals(1.8, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);
    }

    @Test
    @DisplayName("setTemporaryBonus: ボーナスが上書き設定される")
    void testSetTemporaryBonus() {
        manaManager.addTemporaryBonus(testUuid, 0.5);
        manaManager.setTemporaryBonus(testUuid, 1.0);

        assertEquals(2.0, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);
    }

    @Test
    @DisplayName("setTemporaryBonus: 0以下で解除される")
    void testSetTemporaryBonusZeroRemoves() {
        manaManager.addTemporaryBonus(testUuid, 0.5);
        assertEquals(1.5, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);

        manaManager.setTemporaryBonus(testUuid, 0);
        assertEquals(1.0, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);
    }

    @Test
    @DisplayName("clearTemporaryBonus: ボーナスが解除される")
    void testClearTemporaryBonus() {
        manaManager.addTemporaryBonus(testUuid, 0.5);
        assertEquals(1.5, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);

        manaManager.clearTemporaryBonus(testUuid);
        assertEquals(1.0, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);
    }

    // ===== カスタム回復量とボーナスの組み合わせテスト =====

    @Test
    @DisplayName("カスタム回復量とボーナスが合算される")
    void testCustomRegenWithBonus() {
        manaManager.setCustomRegenRate(testUuid, 2.0);
        manaManager.addTemporaryBonus(testUuid, 0.5);

        assertEquals(2.5, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);
    }

    @Test
    @DisplayName("カスタム回復量解除後もボーナスは維持される")
    void testBonusPersistsAfterCustomClear() {
        manaManager.setCustomRegenRate(testUuid, 2.0);
        manaManager.addTemporaryBonus(testUuid, 0.5);

        manaManager.clearCustomRegenRate(testUuid);

        assertEquals(1.5, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);
    }

    // ===== クラス回復量テスト =====

    @Test
    @DisplayName("クラス回復量が使用される")
    void testClassRegenRate() {
        UUID uuid = UUID.randomUUID();
        RPGClass rpgClass = mock(RPGClass.class);
        when(rpgClass.getManaRegen()).thenReturn(2.5);
        when(classManager.getClass("Mage")).thenReturn(Optional.of(rpgClass));

        RPGPlayer rpgPlayer = createMockRpgPlayer(uuid, "Mage");
        when(classManager.getClass("Mage")).thenReturn(Optional.of(rpgClass));

        // カスタム設定がない場合はクラス回復量が使用される
        double regen = manaManager.getCurrentRegenRate(rpgPlayer);
        assertEquals(2.5, regen, 0.001);
    }

    @Test
    @DisplayName("クラス未設定時はデフォルト回復量")
    void testDefaultRegenWhenNoClass() {
        UUID uuid = UUID.randomUUID();
        RPGPlayer rpgPlayer = createMockRpgPlayer(uuid, null);
        when(classManager.getClass(any())).thenReturn(Optional.empty());

        double regen = manaManager.getCurrentRegenRate(rpgPlayer);
        assertEquals(1.0, regen, 0.001);
    }

    // ===== skillRegenManaテスト =====

    @Test
    @DisplayName("skillRegenMana: 正常な回復")
    void testSkillRegenMana() {
        RPGPlayer rpgPlayer = createMockRpgPlayer(testUuid);
        when(rpgPlayer.regenerateMana(anyDouble())).thenReturn(10);

        int actual = manaManager.skillRegenMana(rpgPlayer, 15.5);

        assertEquals(10, actual);
        verify(rpgPlayer).regenerateMana(15.5);
    }

    @Test
    @DisplayName("skillRegenMana: nullプレイヤーで0を返す")
    void testSkillRegenManaNullPlayer() {
        int actual = manaManager.skillRegenMana(null, 10.0);

        assertEquals(0, actual);
    }

    @Test
    @DisplayName("skillRegenMana: 0以下の回復量で0を返す")
    void testSkillRegenManaZeroOrNegative() {
        RPGPlayer rpgPlayer = createMockRpgPlayer(testUuid);

        assertEquals(0, manaManager.skillRegenMana(rpgPlayer, 0));
        assertEquals(0, manaManager.skillRegenMana(rpgPlayer, -1.0));

        verify(rpgPlayer, never()).regenerateMana(anyDouble());
    }

    // ===== getCurrentRegenRateテスト =====

    @Test
    @DisplayName("getCurrentRegenRate: nullプレイヤーでデフォルト値")
    void testGetCurrentRegenRateNullPlayer() {
        double rate = manaManager.getCurrentRegenRate(null);

        assertEquals(1.0, rate, 0.001);
    }

    // ===== shutdownテスト =====

    @Test
    @DisplayName("shutdown: タスク停止とマップクリア")
    void testShutdown() {
        when(task.isCancelled()).thenReturn(false);

        manaManager.start();
        manaManager.setCustomRegenRate(testUuid, 2.0);
        manaManager.addTemporaryBonus(testUuid, 0.5);

        manaManager.shutdown();

        verify(task).cancel();
        verify(logger).info("ManaManager shut down");

        // シャットダウン後はデフォルト値
        assertEquals(1.0, manaManager.getCurrentRegenRate(createMockRpgPlayer(testUuid)), 0.001);
    }

    // ===== getDefaultManaRegenテスト =====

    @Test
    @DisplayName("getDefaultManaRegen: デフォルト値を返す")
    void testGetDefaultManaRegen() {
        assertEquals(1.0, ManaManager.getDefaultManaRegen(), 0.001);
    }

    // ===== 回復タスクの実行テスト =====

    @Test
    @DisplayName("回復タスク: オンラインプレイヤー全員に回復処理")
    void testRegenerationTaskExecution() {
        when(task.isCancelled()).thenReturn(false);
        when(scheduler.runTaskTimer(eq(plugin), any(Runnable.class), anyLong(), anyLong()))
                .thenAnswer(invocation -> {
                    Runnable task = invocation.getArgument(1);
                    task.run(); // タスクを即時実行
                    return mock(BukkitTask.class);
                });

        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        RPGPlayer player1 = createMockRpgPlayer(uuid1);
        RPGPlayer player2 = createMockRpgPlayer(uuid2);

        when(playerManager.getOnlinePlayers()).thenReturn(Map.of(
                uuid1, player1,
                uuid2, player2
        ));
        Player bukkitPlayer1 = mock(Player.class);
        Player bukkitPlayer2 = mock(Player.class);
        when(player1.getBukkitPlayer()).thenReturn(bukkitPlayer1);
        when(player2.getBukkitPlayer()).thenReturn(bukkitPlayer2);
        when(bukkitPlayer1.isOnline()).thenReturn(true);
        when(bukkitPlayer2.isOnline()).thenReturn(true);
        when(player1.isFullMana()).thenReturn(false);
        when(player2.isFullMana()).thenReturn(false);
        when(player1.getManaRatio()).thenReturn(0.3); // 50%未満
        when(player2.getManaRatio()).thenReturn(0.7); // 50%以上
        when(player1.regenerateMana(anyDouble())).thenReturn(1);
        when(player2.regenerateMana(anyDouble())).thenReturn(1);

        manaManager.start();

        // 両方のプレイヤーで回復が実行される
        verify(player1).regenerateMana(anyDouble());
        verify(player2).regenerateMana(anyDouble());
        // player1は50%未満なのでインジケーター表示
        verify(bukkitPlayer1).sendActionBar(any(Component.class));
        // player2は50%以上なのでインジケーター非表示
        verify(bukkitPlayer2, never()).sendActionBar(any(Component.class));
    }

    @Test
    @DisplayName("回復タスク: 最大MPのプレイヤーはスキップされる")
    void testRegenerationSkipsFullMana() {
        when(task.isCancelled()).thenReturn(false);
        when(scheduler.runTaskTimer(eq(plugin), any(Runnable.class), anyLong(), anyLong()))
                .thenAnswer(invocation -> {
                    Runnable task = invocation.getArgument(1);
                    task.run();
                    return mock(BukkitTask.class);
                });

        RPGPlayer player = createMockRpgPlayer(testUuid);
        Player bukkitPlayer = mock(Player.class);
        when(player.getBukkitPlayer()).thenReturn(bukkitPlayer);
        when(bukkitPlayer.isOnline()).thenReturn(true);
        when(player.isOnline()).thenReturn(true);
        when(player.isFullMana()).thenReturn(true);

        when(playerManager.getOnlinePlayers()).thenReturn(Map.of(testUuid, player));

        manaManager.start();

        verify(player, never()).regenerateMana(anyDouble());
    }

    @Test
    @DisplayName("回復タスク: オフラインプレイヤーはスキップされる")
    void testRegenerationSkipsOffline() {
        when(task.isCancelled()).thenReturn(false);
        when(scheduler.runTaskTimer(eq(plugin), any(Runnable.class), anyLong(), anyLong()))
                .thenAnswer(invocation -> {
                    Runnable task = invocation.getArgument(1);
                    task.run();
                    return mock(BukkitTask.class);
                });

        RPGPlayer player = createMockRpgPlayer(testUuid);
        Player bukkitPlayer = mock(Player.class);
        when(player.getBukkitPlayer()).thenReturn(bukkitPlayer);
        when(bukkitPlayer.isOnline()).thenReturn(false);
        when(player.isOnline()).thenReturn(false);

        when(playerManager.getOnlinePlayers()).thenReturn(Map.of(testUuid, player));

        manaManager.start();

        verify(player, never()).regenerateMana(anyDouble());
    }

    // ===== 回復インジケーターテスト =====

    @Test
    @DisplayName("回復インジケーター: MP50%未満で表示される")
    void testShowRegenIndicatorWhenLowMana() {
        when(task.isCancelled()).thenReturn(false);
        when(scheduler.runTaskTimer(eq(plugin), any(Runnable.class), anyLong(), anyLong()))
                .thenAnswer(invocation -> {
                    Runnable task = invocation.getArgument(1);
                    task.run();
                    return mock(BukkitTask.class);
                });

        RPGPlayer player = createMockRpgPlayer(testUuid);
        Player bukkitPlayer = mock(Player.class);
        when(player.getBukkitPlayer()).thenReturn(bukkitPlayer);
        when(bukkitPlayer.isOnline()).thenReturn(true);
        when(player.isOnline()).thenReturn(true);
        when(player.isFullMana()).thenReturn(false);
        when(player.getManaRatio()).thenReturn(0.3); // 50%未満
        when(player.regenerateMana(anyDouble())).thenReturn(1);

        when(playerManager.getOnlinePlayers()).thenReturn(Map.of(testUuid, player));

        manaManager.start();

        verify(bukkitPlayer).sendActionBar(any(Component.class));
    }

    @Test
    @DisplayName("回復インジケーター: MP50%以上で表示されない")
    void testNoRegenIndicatorWhenHighMana() {
        when(task.isCancelled()).thenReturn(false);
        when(scheduler.runTaskTimer(eq(plugin), any(Runnable.class), anyLong(), anyLong()))
                .thenAnswer(invocation -> {
                    Runnable task = invocation.getArgument(1);
                    task.run();
                    return mock(BukkitTask.class);
                });

        RPGPlayer player = createMockRpgPlayer(testUuid);
        Player bukkitPlayer = mock(Player.class);
        when(player.getBukkitPlayer()).thenReturn(bukkitPlayer);
        when(bukkitPlayer.isOnline()).thenReturn(true);
        when(player.isOnline()).thenReturn(true);
        when(player.isFullMana()).thenReturn(false);
        when(player.getManaRatio()).thenReturn(0.7); // 50%以上
        when(player.regenerateMana(anyDouble())).thenReturn(1);

        when(playerManager.getOnlinePlayers()).thenReturn(Map.of(testUuid, player));

        manaManager.start();

        verify(bukkitPlayer, never()).sendActionBar(any(Component.class));
    }

    // ===== ヘルパーメソッド =====

    private RPGPlayer createMockRpgPlayer(UUID uuid) {
        return createMockRpgPlayer(uuid, null);
    }

    private RPGPlayer createMockRpgPlayer(UUID uuid, String classId) {
        RPGPlayer rpgPlayer = mock(RPGPlayer.class);
        when(rpgPlayer.getUuid()).thenReturn(uuid);
        when(rpgPlayer.getClassId()).thenReturn(classId);
        when(rpgPlayer.isFullMana()).thenReturn(true); // デフォルトは満タン
        when(rpgPlayer.getManaRatio()).thenReturn(1.0);
        return rpgPlayer;
    }
}
