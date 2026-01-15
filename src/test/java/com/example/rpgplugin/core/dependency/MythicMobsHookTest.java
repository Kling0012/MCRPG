package com.example.rpgplugin.core.dependency;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MythicMobsHookのユニットテスト
 *
 * <p>MythicMobsプラグイン連携フックのテストを行います。</p>
 * <p>注意: MythicMobsライブラリがクラスパスにない場合、
 * テストの一部はスキップまたは失敗する可能性があります。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MythicMobsHook テスト")
class MythicMobsHookTest {

    @Mock
    private JavaPlugin mockPlugin;

    @Mock
    private Logger mockLogger;

    @Mock
    private Entity mockEntity;

    @Mock
    private LivingEntity mockLivingEntity;

    @Mock
    private Player mockPlayer;

    @Mock
    private Location mockLocation;

    private static final UUID TEST_UUID = UUID.randomUUID();

    private MythicMobsHook mythicMobsHook;

    @BeforeEach
    void setUp() {
        when(mockEntity.getUniqueId()).thenReturn(TEST_UUID);
        when(mockLivingEntity.getUniqueId()).thenReturn(TEST_UUID);
        when(mockPlugin.getLogger()).thenReturn(mockLogger);
    }

    // ==================== コンストラクタ・初期状態 テスト ====================

    @Nested
    @DisplayName("コンストラクタ・初期状態 テスト")
    class ConstructorTests {

        @Test
        @DisplayName("コンストラクタでインスタンスが生成される")
        void constructor_CreatesInstance() {
            mythicMobsHook = new MythicMobsHook(mockPlugin);

            assertThat(mythicMobsHook).isNotNull();
        }

        @Test
        @DisplayName("初期状態は利用不可能")
        void constructor_InitiallyNotAvailable() {
            mythicMobsHook = new MythicMobsHook(mockPlugin);

            assertThat(mythicMobsHook.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("初期状態でgetMythicBukkitはnullを返す")
        void constructor_GetMythicBukkitIsNull() {
            mythicMobsHook = new MythicMobsHook(mockPlugin);

            assertThat(mythicMobsHook.getMythicBukkit()).isNull();
        }

        @Test
        @DisplayName("初期状態でgetVersionはUnknownを返す")
        void constructor_GetVersionReturnsUnknown() {
            mythicMobsHook = new MythicMobsHook(mockPlugin);

            assertThat(mythicMobsHook.getVersion()).isEqualTo("Unknown");
        }

        @Test
        @DisplayName("初期状態でgetMythicPluginはnullを返す")
        void constructor_GetMythicPluginIsNull() {
            mythicMobsHook = new MythicMobsHook(mockPlugin);

            assertThat(mythicMobsHook.getMythicPlugin()).isNull();
        }
    }

    // ==================== 未セットアップ時のメソッド テスト ====================

    @Nested
    @DisplayName("未セットアップ時のメソッド テスト")
    class NotSetupTests {

        @BeforeEach
        void setUp() {
            mythicMobsHook = new MythicMobsHook(mockPlugin);
        }

        @Test
        @DisplayName("isMythicMobはfalseを返す")
        void isMythicMob_NotSetup_ReturnsFalse() {
            boolean result = mythicMobsHook.isMythicMob(mockEntity);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("getActiveMobは空を返す")
        void getActiveMob_NotSetup_ReturnsEmpty() {
            Optional<?> result = mythicMobsHook.getActiveMob(mockEntity);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("getMobTypeIdはnullを返す")
        void getMobTypeId_NotSetup_ReturnsNull() {
            String result = mythicMobsHook.getMobTypeId(mockEntity);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getMobDisplayNameはnullを返す")
        void getMobDisplayName_NotSetup_ReturnsNull() {
            String result = mythicMobsHook.getMobDisplayName(mockEntity);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getMobLevelは-1を返す")
        void getMobLevel_NotSetup_ReturnsMinusOne() {
            double result = mythicMobsHook.getMobLevel(mockEntity);

            assertThat(result).isEqualTo(-1.0);
        }

        @Test
        @DisplayName("getMobHealthは-1を返す")
        void getMobHealth_NotSetup_ReturnsMinusOne() {
            double result = mythicMobsHook.getMobHealth(mockEntity);

            assertThat(result).isEqualTo(-1.0);
        }

        @Test
        @DisplayName("getMobMaxHealthは-1を返す")
        void getMobMaxHealth_NotSetup_ReturnsMinusOne() {
            double result = mythicMobsHook.getMobMaxHealth(mockEntity);

            assertThat(result).isEqualTo(-1.0);
        }

        @Test
        @DisplayName("spawnMobは空を返す")
        void spawnMob_NotSetup_ReturnsEmpty() {
            Optional<?> result = mythicMobsHook.spawnMob("TestMob", mockLocation, 1.0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("setTargetは例外をスローしない")
        void setTarget_NotSetup_NoException() {
            assertThatCode(() -> mythicMobsHook.setTarget(mockEntity, mockPlayer))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("removeMobはfalseを返す")
        void removeMob_NotSetup_ReturnsFalse() {
            boolean result = mythicMobsHook.removeMob(mockEntity);

            assertThat(result).isFalse();
        }
    }

    // ==================== cleanup テスト ====================

    @Nested
    @DisplayName("cleanup テスト")
    class CleanupTests {

        @Test
        @DisplayName("cleanupでリソースが解放される")
        void cleanup_SetsFieldsToNull() {
            mythicMobsHook = new MythicMobsHook(mockPlugin);

            // セットアップを試みる（MythicMobsがない場合は失敗する）
            mythicMobsHook.setup();

            mythicMobsHook.cleanup();

            assertThat(mythicMobsHook.isAvailable()).isFalse();
            assertThat(mythicMobsHook.getMythicBukkit()).isNull();
            assertThat(mythicMobsHook.getMythicPlugin()).isNull();
            assertThat(mythicMobsHook.getVersion()).isEqualTo("Unknown");
        }

        @Test
        @DisplayName("cleanupは複数回呼んでも安全")
        void cleanup_MultipleCalls_Safe() {
            mythicMobsHook = new MythicMobsHook(mockPlugin);

            assertThatCode(() -> {
                mythicMobsHook.cleanup();
                mythicMobsHook.cleanup();
                mythicMobsHook.cleanup();
            }).doesNotThrowAnyException();
        }
    }

    // ==================== getMobMaxHealth 詳細テスト ====================

    @Nested
    @DisplayName("getMobMaxHealth 詳細テスト")
    class GetMobMaxHealthTests {

        @Test
        @DisplayName("EntityがLivingEntityでない場合-1を返す")
        void getMobMaxHealth_NotLivingEntity_ReturnsMinusOne() {
            mythicMobsHook = new MythicMobsHook(mockPlugin);

            // Entity mockはLivingEntityではない
            double result = mythicMobsHook.getMobMaxHealth(mockEntity);

            assertThat(result).isEqualTo(-1.0);
        }

        @Test
        @DisplayName("LivingEntityの場合の計算（未セットアップ）")
        void getMobMaxHealth_LivingEntity_NotSetup_ReturnsMinusOne() {
            mythicMobsHook = new MythicMobsHook(mockPlugin);

            double result = mythicMobsHook.getMobMaxHealth(mockLivingEntity);

            assertThat(result).isEqualTo(-1.0);
        }
    }

    // ==================== setDropsEnabled テスト ====================

    @Nested
    @DisplayName("setDropsEnabled テスト")
    class SetDropsEnabledTests {

        @BeforeEach
        void setUp() {
            mythicMobsHook = new MythicMobsHook(mockPlugin);
        }

        @Test
        @DisplayName("setDropsEnabledは実装されていないが例外をスローしない")
        void setDropsEnabled_NotImplemented_NoException() {
            assertThatCode(() -> mythicMobsHook.setDropsEnabled(mockEntity, true))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("setDropsEnabled(false)も安全")
        void setDropsEnabledFalse_Safe() {
            assertThatCode(() -> mythicMobsHook.setDropsEnabled(mockEntity, false))
                    .doesNotThrowAnyException();
        }
    }

    // ==================== setup テスト ====================

    @Nested
    @DisplayName("setup テスト")
    class SetupTests {

        @Test
        @DisplayName("MythicMobsがない環境ではfalseを返す")
        void setup_NoMythicMobs_ReturnsFalse() {
            mythicMobsHook = new MythicMobsHook(mockPlugin);

            // MythicMobsがない環境ではsetupはfalseを返す
            boolean result = mythicMobsHook.setup();

            assertThat(mythicMobsHook.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("setup失敗後もcleanupは安全")
        void setupFailed_CleanupSafe() {
            mythicMobsHook = new MythicMobsHook(mockPlugin);
            mythicMobsHook.setup();

            assertThatCode(() -> mythicMobsHook.cleanup())
                    .doesNotThrowAnyException();
        }
    }

    // ==================== Null安全 テスト ====================

    @Nested
    @DisplayName("Null安全 テスト")
    class NullSafetyTests {

        @BeforeEach
        void setUp() {
            mythicMobsHook = new MythicMobsHook(mockPlugin);
        }

        @Test
        @DisplayName("nullエンティティでisMythicMobはfalse")
        void isMythicMob_NullEntity_ReturnsFalse() {
            boolean result = mythicMobsHook.isMythicMob(null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("nullエンティティでgetActiveMobは空")
        void getActiveMob_NullEntity_ReturnsEmpty() {
            Optional<?> result = mythicMobsHook.getActiveMob(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("nullエンティティでgetMobTypeIdはnull")
        void getMobTypeId_NullEntity_ReturnsNull() {
            String result = mythicMobsHook.getMobTypeId(null);

            assertThat(result).isNull();
        }
    }
}
