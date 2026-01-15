package com.example.rpgplugin.core.dependency;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.logging.Logger;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * VaultHookのユニットテスト
 *
 * <p>Vaultプラグイン連携フックのテストを行います。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("VaultHook テスト")
class VaultHookTest {

    @Mock
    private JavaPlugin mockPlugin;

    @Mock
    private Logger mockLogger;

    @Mock
    private Server mockServer;

    @Mock
    private PluginManager mockPluginManager;

    @Mock
    private Plugin mockVaultPlugin;

    @Mock
    private Economy mockEconomy;

    @Mock
    private Chat mockChat;

    @Mock
    private Permission mockPermission;

    @Mock
    private RegisteredServiceProvider<Economy> mockEconomyRsp;

    @Mock
    private RegisteredServiceProvider<Chat> mockChatRsp;

    @Mock
    private RegisteredServiceProvider<Permission> mockPermissionRsp;

    @Mock
    private OfflinePlayer mockPlayer;

    @Mock
    private org.bukkit.plugin.ServicesManager mockServicesManager;

    private MockedStatic<Bukkit> mockedBukkit;
    private VaultHook vaultHook;
    private static final UUID TEST_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // Bukkitの静的メソッドをモック
        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::getServer).thenReturn(mockServer);
        mockedBukkit.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
        mockedBukkit.when(Bukkit::getServicesManager).thenReturn(mockServicesManager);
        mockedBukkit.when(() -> Bukkit.getOfflinePlayerIfCached(anyString())).thenReturn(mockPlayer);

        when(mockServer.getPluginManager()).thenReturn(mockPluginManager);
        when(mockServer.getServicesManager()).thenReturn(mockServicesManager);

        when(mockPlugin.getLogger()).thenReturn(mockLogger);
        when(mockPlugin.getServer()).thenReturn(mockServer);

        // OfflinePlayer mock setup
        when(mockPlayer.getUniqueId()).thenReturn(TEST_UUID);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    // ==================== コンストラクタテスト ====================

    @Nested
    @DisplayName("コンストラクタテスト")
    class ConstructorTests {

        @Test
        @DisplayName("コンストラクタでインスタンスが生成される")
        void constructor_CreatesInstance() {
            vaultHook = new VaultHook(mockPlugin);

            assertThat(vaultHook).isNotNull();
        }
    }

    // ==================== setup テスト ====================

    @Nested
    @DisplayName("setup テスト")
    class SetupTests {

        @Test
        @DisplayName("Vaultプラグインが無効な場合はfalseを返す")
        void setup_VaultNotEnabled_ReturnsFalse() {
            when(mockPluginManager.getPlugin("Vault")).thenReturn(null);

            vaultHook = new VaultHook(mockPlugin);
            boolean result = vaultHook.setup();

            assertThat(result).isFalse();
            verify(mockLogger).warning(contains("not enabled"));
        }

        @Test
        @DisplayName("全サービスのセットアップに成功するとtrueを返す")
        void setup_AllServicesAvailable_ReturnsTrue() {
            mockServicesManager(true, true, true);

            vaultHook = new VaultHook(mockPlugin);
            boolean result = vaultHook.setup();

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("経済システムのみ利用可能な場合trueを返す")
        void setup_OnlyEconomyAvailable_ReturnsTrue() {
            mockServicesManager(true, false, false);

            vaultHook = new VaultHook(mockPlugin);
            boolean result = vaultHook.setup();

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("全サービスが利用不可能な場合falseを返す")
        void setup_NoServicesAvailable_ReturnsFalse() {
            mockServicesManager(false, false, false);

            vaultHook = new VaultHook(mockPlugin);
            boolean result = vaultHook.setup();

            assertThat(result).isFalse();
        }
    }

    // ==================== ゲッター テスト ====================

    @Nested
    @DisplayName("ゲッター テスト")
    class GetterTests {

        @BeforeEach
        void setUp() {
            mockServicesManager(true, true, true);
            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();
        }

        @Test
        @DisplayName("getEconomyでEconomyインスタンスが取得できる")
        void getEconomy_ReturnsEconomy() {
            assertThat(vaultHook.getEconomy()).isSameAs(mockEconomy);
        }

        @Test
        @DisplayName("getChatでChatインスタンスが取得できる")
        void getChat_ReturnsChat() {
            assertThat(vaultHook.getChat()).isSameAs(mockChat);
        }

        @Test
        @DisplayName("getPermissionでPermissionインスタンスが取得できる")
        void getPermission_ReturnsPermission() {
            assertThat(vaultHook.getPermission()).isSameAs(mockPermission);
        }
    }

    // ==================== hasXxx テスト ====================

    @Nested
    @DisplayName("hasXxx テスト")
    class HasTests {

        @Test
        @DisplayName("hasEconomyはEconomyが利用可能な場合true")
        void hasEconomy_WhenAvailable_ReturnsTrue() {
            mockServicesManager(true, false, false);
            when(mockEconomy.isEnabled()).thenReturn(true);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            assertThat(vaultHook.hasEconomy()).isTrue();
        }

        @Test
        @DisplayName("hasEconomyはEconomyが無効な場合false")
        void hasEconomy_WhenDisabled_ReturnsFalse() {
            mockServicesManager(true, false, false);
            when(mockEconomy.isEnabled()).thenReturn(false);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            assertThat(vaultHook.hasEconomy()).isFalse();
        }

        @Test
        @DisplayName("hasChatはChatが利用可能な場合true")
        void hasChat_WhenAvailable_ReturnsTrue() {
            mockServicesManager(false, true, false);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            assertThat(vaultHook.hasChat()).isTrue();
        }

        @Test
        @DisplayName("hasChatはChatが利用不可能な場合false")
        void hasChat_WhenNotAvailable_ReturnsFalse() {
            mockServicesManager(false, false, false);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            assertThat(vaultHook.hasChat()).isFalse();
        }

        @Test
        @DisplayName("hasPermissionはPermissionが利用可能な場合true")
        void hasPermission_WhenAvailable_ReturnsTrue() {
            mockServicesManager(false, false, true);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            assertThat(vaultHook.hasPermission()).isTrue();
        }

        @Test
        @DisplayName("hasPermissionはPermissionが利用不可能な場合false")
        void hasPermission_WhenNotAvailable_ReturnsFalse() {
            mockServicesManager(false, false, false);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            assertThat(vaultHook.hasPermission()).isFalse();
        }
    }

    // ==================== 経済操作メソッド テスト ====================

    @Nested
    @DisplayName("経済操作メソッド テスト")
    class EconomyOperationTests {

        @BeforeEach
        void setUp() {
            mockServicesManager(true, false, false);
            when(mockEconomy.isEnabled()).thenReturn(true);
        }

        @Test
        @DisplayName("getBalanceで残高を取得できる")
        void getBalance_WhenEconomyAvailable_ReturnsBalance() {
            when(mockEconomy.getBalance(mockPlayer)).thenReturn(100.0);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            double balance = vaultHook.getBalance("player");

            assertThat(balance).isEqualTo(100.0);
        }

        @Test
        @DisplayName("getBalanceはEconomyが無効な場合0を返す")
        void getBalance_WhenEconomyNotAvailable_ReturnsZero() {
            when(mockEconomy.isEnabled()).thenReturn(false);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            double balance = vaultHook.getBalance("player");

            assertThat(balance).isEqualTo(0);
        }

        @Test
        @DisplayName("getBalanceはプレイヤーがキャッシュにない場合0を返す")
        void getBalance_PlayerNotCached_ReturnsZero() {
            when(Bukkit.getOfflinePlayerIfCached(anyString())).thenReturn(null);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            double balance = vaultHook.getBalance("unknown_player");

            assertThat(balance).isEqualTo(0);
        }

        @Test
        @DisplayName("setBalanceで残高を設定できる")
        void setBalance_Successful_SetsBalance() {
            when(mockEconomy.getBalance(mockPlayer)).thenReturn(50.0);
            var responseMock = mock(net.milkbowl.vault.economy.EconomyResponse.class);
            when(responseMock.transactionSuccess()).thenReturn(true);
            when(mockEconomy.depositPlayer(mockPlayer, 50.0)).thenReturn(responseMock);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            boolean result = vaultHook.setBalance("player", 100.0);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("setBalanceはEconomyが無効な場合falseを返す")
        void setBalance_EconomyNotAvailable_ReturnsFalse() {
            when(mockEconomy.isEnabled()).thenReturn(false);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            boolean result = vaultHook.setBalance("player", 100.0);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("depositで入金できる")
        void deposit_Successful_ReturnsTrue() {
            var responseMock = mock(net.milkbowl.vault.economy.EconomyResponse.class);
            when(responseMock.transactionSuccess()).thenReturn(true);
            when(mockEconomy.depositPlayer(mockPlayer, 50.0)).thenReturn(responseMock);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            boolean result = vaultHook.deposit("player", 50.0);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("withdrawで出金できる")
        void withdraw_Successful_ReturnsTrue() {
            var responseMock = mock(net.milkbowl.vault.economy.EconomyResponse.class);
            when(responseMock.transactionSuccess()).thenReturn(true);
            when(mockEconomy.withdrawPlayer(mockPlayer, 30.0)).thenReturn(responseMock);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            boolean result = vaultHook.withdraw("player", 30.0);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("hasで残高確認ができる")
        void has_SufficientBalance_ReturnsTrue() {
            when(mockEconomy.has(mockPlayer, 50.0)).thenReturn(true);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            boolean result = vaultHook.has("player", 50.0);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("hasは残高不足の場合falseを返す")
        void has_InsufficientBalance_ReturnsFalse() {
            when(mockEconomy.has(mockPlayer, 50.0)).thenReturn(false);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            boolean result = vaultHook.has("player", 50.0);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("hasはEconomyが無効な場合falseを返す")
        void has_EconomyNotAvailable_ReturnsFalse() {
            when(mockEconomy.isEnabled()).thenReturn(false);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            boolean result = vaultHook.has("player", 50.0);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("setBalanceは現在より高い金額でdepositを呼ぶ")
        void setBalance_AmountHigher_DepositsDifference() {
            when(mockEconomy.getBalance(mockPlayer)).thenReturn(50.0);
            var responseMock = mock(net.milkbowl.vault.economy.EconomyResponse.class);
            when(responseMock.transactionSuccess()).thenReturn(true);
            when(mockEconomy.depositPlayer(mockPlayer, 50.0)).thenReturn(responseMock);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            boolean result = vaultHook.setBalance("player", 100.0);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("setBalanceは現在より低い金額でwithdrawを呼ぶ")
        void setBalance_AmountLower_WithdrawsDifference() {
            when(mockEconomy.getBalance(mockPlayer)).thenReturn(100.0);
            var responseMock = mock(net.milkbowl.vault.economy.EconomyResponse.class);
            when(responseMock.transactionSuccess()).thenReturn(true);
            when(mockEconomy.withdrawPlayer(mockPlayer, 50.0)).thenReturn(responseMock);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            boolean result = vaultHook.setBalance("player", 50.0);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("setBalanceは同じ金額でtrueを返す")
        void setBalance_SameAmount_ReturnsTrue() {
            when(mockEconomy.getBalance(mockPlayer)).thenReturn(100.0);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            boolean result = vaultHook.setBalance("player", 100.0);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("depositはトランザクション失敗でfalseを返す")
        void deposit_TransactionFails_ReturnsFalse() {
            var responseMock = mock(net.milkbowl.vault.economy.EconomyResponse.class);
            when(responseMock.transactionSuccess()).thenReturn(false);
            when(mockEconomy.depositPlayer(mockPlayer, 50.0)).thenReturn(responseMock);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            boolean result = vaultHook.deposit("player", 50.0);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("withdrawはトランザクション失敗でfalseを返す")
        void withdraw_TransactionFails_ReturnsFalse() {
            var responseMock = mock(net.milkbowl.vault.economy.EconomyResponse.class);
            when(responseMock.transactionSuccess()).thenReturn(false);
            when(mockEconomy.withdrawPlayer(mockPlayer, 30.0)).thenReturn(responseMock);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            boolean result = vaultHook.withdraw("player", 30.0);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("depositはプレイヤーがキャッシュにない場合falseを返す")
        void deposit_PlayerNotCached_ReturnsFalse() {
            when(Bukkit.getOfflinePlayerIfCached(anyString())).thenReturn(null);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            boolean result = vaultHook.deposit("unknown_player", 50.0);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("withdrawはプレイヤーがキャッシュにない場合falseを返す")
        void withdraw_PlayerNotCached_ReturnsFalse() {
            when(Bukkit.getOfflinePlayerIfCached(anyString())).thenReturn(null);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            boolean result = vaultHook.withdraw("unknown_player", 30.0);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("hasはプレイヤーがキャッシュにない場合falseを返す")
        void has_PlayerNotCached_ReturnsFalse() {
            when(Bukkit.getOfflinePlayerIfCached(anyString())).thenReturn(null);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            boolean result = vaultHook.has("unknown_player", 50.0);

            assertThat(result).isFalse();
        }
    }

    // ==================== cleanup テスト ====================

    @Nested
    @DisplayName("cleanup テスト")
    class CleanupTests {

        @Test
        @DisplayName("cleanupで全サービスがnullになる")
        void cleanup_SetsAllServicesToNull() {
            mockServicesManager(true, true, true);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            assertThat(vaultHook.getEconomy()).isNotNull();

            vaultHook.cleanup();

            assertThat(vaultHook.getEconomy()).isNull();
            assertThat(vaultHook.getChat()).isNull();
            assertThat(vaultHook.getPermission()).isNull();
        }

        @Test
        @DisplayName("cleanup後hasXxxはfalseを返す")
        void cleanup_AfterCleanup_ReturnsFalse() {
            mockServicesManager(true, true, true);

            vaultHook = new VaultHook(mockPlugin);
            vaultHook.setup();

            vaultHook.cleanup();

            assertThat(vaultHook.hasEconomy()).isFalse();
            assertThat(vaultHook.hasChat()).isFalse();
            assertThat(vaultHook.hasPermission()).isFalse();
        }
    }

    // ==================== ヘルパーメソッド ====================

    private void mockServicesManager(boolean economy, boolean chat, boolean permission) {
        when(mockPluginManager.getPlugin("Vault")).thenReturn(mockVaultPlugin);
        when(mockVaultPlugin.isEnabled()).thenReturn(true);

        // ServicesManagerモック設定
        if (economy) {
            when(mockEconomy.isEnabled()).thenReturn(true);
            when(mockEconomyRsp.getProvider()).thenReturn(mockEconomy);
            when(mockServicesManager.getRegistration(Economy.class)).thenReturn(mockEconomyRsp);
        }

        if (chat) {
            when(mockChatRsp.getProvider()).thenReturn(mockChat);
            when(mockServicesManager.getRegistration(Chat.class)).thenReturn(mockChatRsp);
        }

        if (permission) {
            when(mockPermissionRsp.getProvider()).thenReturn(mockPermission);
            when(mockServicesManager.getRegistration(Permission.class)).thenReturn(mockPermissionRsp);
        }

        // Serverモック設定 - 既存のmockServerを使用
        when(mockServer.getPluginManager()).thenReturn(mockPluginManager);
        when(mockServer.getServicesManager()).thenReturn(mockServicesManager);
        when(mockPlugin.getServer()).thenReturn(mockServer);

        // EconomyResponse mock
        net.milkbowl.vault.economy.EconomyResponse responseMock = mock(net.milkbowl.vault.economy.EconomyResponse.class);
        when(responseMock.transactionSuccess()).thenReturn(true);
        when(mockEconomy.depositPlayer(any(OfflinePlayer.class), anyDouble())).thenReturn(responseMock);
        when(mockEconomy.withdrawPlayer(any(OfflinePlayer.class), anyDouble())).thenReturn(responseMock);
    }
}
