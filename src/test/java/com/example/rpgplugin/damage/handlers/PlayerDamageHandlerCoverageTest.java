package com.example.rpgplugin.damage.handlers;

import com.example.rpgplugin.damage.config.YamlDamageCalculator;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PlayerDamageHandlerの追加カバレッジテスト
 *
 * <p>90%以上のカバレッジ達成のための追加テストケースです。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PlayerDamageHandler カバレッジ向上テスト")
class PlayerDamageHandlerCoverageTest {

    private PlayerDamageHandler handler;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private Logger mockLogger;

    @Mock
    private Player mockPlayer;

    @Mock
    private Entity mockTarget;

    @Mock
    private RPGPlayer mockRpgPlayer;

    @Mock
    private StatManager mockStatManager;

    @Mock
    private EntityDamageByEntityEvent mockEvent;

    private UUID playerUuid;

    @BeforeEach
    void setUp() {
        handler = new PlayerDamageHandler(mockPlayerManager, mockLogger);
        playerUuid = UUID.randomUUID();

        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockPlayerManager.getRPGPlayer(playerUuid)).thenReturn(mockRpgPlayer);
        when(mockRpgPlayer.getStatManager()).thenReturn(mockStatManager);
    }

    @Nested
    @DisplayName("キャッシュ機能詳細テスト")
    class CacheDetailedTests {

        @Test
        @DisplayName("キャッシュ有効期限切れで再計算される")
        void cacheExpired_Recalculates() throws Exception {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            // 1回目の攻撃
            handler.handlePlayerToEntityDamage(mockEvent);
            assertThat(handler.getCacheSize()).isEqualTo(1);

            // キャッシュを直接期限切れにする
            setCacheEntryTimestamp(playerUuid, System.currentTimeMillis() - 2000);

            // 2回目の攻撃（キャッシュ期限切れなので再計算）
            handler.handlePlayerToEntityDamage(mockEvent);

            // キャッシュサイズは1のまま（上書き）
            assertThat(handler.getCacheSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("キャッシュ有効期限内で同じ値が使用される")
        void cacheValid_UsesSameValue() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            // 1回目
            double result1 = handler.handlePlayerToEntityDamage(mockEvent);

            // 2回目（キャッシュ使用）
            double result2 = handler.handlePlayerToEntityDamage(mockEvent);

            // 同じ値である
            assertThat(result1).isEqualTo(result2);
            assertThat(result1).isEqualTo(15);
        }

        @Test
        @DisplayName("同じプレイヤーの連続攻撃でキャッシュが上書きされる")
        void samePlayer_OverwritesCacheEntry() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            // 1回目の攻撃
            handler.handlePlayerToEntityDamage(mockEvent);

            // キャッシュサイズは1
            assertThat(handler.getCacheSize()).isEqualTo(1);

            // 2回目の攻撃（キャッシュ使用）
            handler.handlePlayerToEntityDamage(mockEvent);

            // キャッシュサイズは1のまま（上書き）
            assertThat(handler.getCacheSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("異なるプレイヤーでキャッシュが分離される")
        void differentPlayers_SeparateCaches() {
            Player mockPlayer2 = mock(Player.class);
            UUID playerUuid2 = UUID.randomUUID();
            when(mockPlayer2.getUniqueId()).thenReturn(playerUuid2);
            when(mockPlayerManager.getRPGPlayer(playerUuid2)).thenReturn(mockRpgPlayer);

            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            // プレイヤー1の攻撃
            when(mockEvent.getDamager()).thenReturn(mockPlayer);
            handler.handlePlayerToEntityDamage(mockEvent);

            // プレイヤー2の攻撃
            when(mockEvent.getDamager()).thenReturn(mockPlayer2);
            handler.handlePlayerToEntityDamage(mockEvent);

            // 別々のキャッシュエントリ
            assertThat(handler.getCacheSize()).isEqualTo(2);
        }

        @Test
        @DisplayName("clearCacheで期限切れエントリのみ削除される")
        void clearCache_RemovesOnlyExpiredEntries() throws Exception {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            // エントリを作成
            handler.handlePlayerToEntityDamage(mockEvent);

            // 有効なエントリを期限切れに変更
            setCacheEntryTimestamp(playerUuid, System.currentTimeMillis() - 2000);

            // clearCache実行
            handler.clearCache();

            // キャッシュが空になる
            assertThat(handler.getCacheSize()).isEqualTo(0);
        }

        @Test
        @DisplayName("clearCacheで有効なエントリは維持される")
        void clearCache_KeepsValidEntries() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            // エントリを作成
            handler.handlePlayerToEntityDamage(mockEvent);
            int sizeBefore = handler.getCacheSize();

            // clearCache実行（エントリは有効なまま）
            handler.clearCache();

            // サイズが維持される
            assertThat(handler.getCacheSize()).isEqualTo(sizeBefore);
        }
    }

    @Nested
    @DisplayName("追加ダメージタイプテスト")
    class AdditionalDamageTypeTests {

        @Test
        @DisplayName("ENTITY_EXPLOSIONは魔法ダメージ")
        void damageType_EntityExplosion_IsMagic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.INTELLIGENCE, 50));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // INT魔法ダメージ: 100 + 50 * 0.15 = 107.5 -> 107
            assertThat(result).isEqualTo(107);
        }

        @Test
        @DisplayName("BLOCK_EXPLOSIONは魔法ダメージ")
        void damageType_BlockExplosion_IsMagic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION, 80.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.INTELLIGENCE, 100));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // INT魔法ダメージ: 80 + 100 * 0.15 = 95
            assertThat(result).isEqualTo(95);
        }

        @Test
        @DisplayName("FIREは魔法ダメージ")
        void damageType_Fire_IsMagic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.FIRE, 60.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.INTELLIGENCE, 75));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // INT魔法ダメージ: 60 + 75 * 0.15 = 71.25 -> 71
            assertThat(result).isEqualTo(71);
        }

        @Test
        @DisplayName("FALLは魔法ダメージ")
        void damageType_Fall_IsMagic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.FALL, 40.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.INTELLIGENCE, 25));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // INT魔法ダメージ: 40 + 25 * 0.15 = 43.75 -> 43
            assertThat(result).isEqualTo(43);
        }

        @Test
        @DisplayName("POISONは魔法ダメージ")
        void damageType_Poison_IsMagic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.POISON, 30.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.INTELLIGENCE, 0));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // INT0なので基本ダメージそのまま
            assertThat(result).isEqualTo(30);
        }

        @Test
        @DisplayName("LIGHTNINGは魔法ダメージ")
        void damageType_Lightning_IsMagic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.LIGHTNING, 50.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.INTELLIGENCE, 200));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // INT魔法ダメージ: 50 + 200 * 0.15 = 80
            assertThat(result).isEqualTo(80);
        }

        @Test
        @DisplayName("VOIDは魔法ダメージ")
        void damageType_Void_IsMagic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.VOID, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.INTELLIGENCE, 50));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // INT魔法ダメージ
            assertThat(result).isEqualTo(107);
        }
    }

    @Nested
    @DisplayName("YAML Calculator追加エッジケース")
    class YamlCalculatorAdditionalEdgeCases {

        @Mock
        private YamlDamageCalculator mockYamlCalculator;

        @Test
        @DisplayName("YAML計算機でIllegalArgumentExceptionもフォールバック")
        void yamlCalculator_IllegalArgumentException_FallsBack() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            handler.setYamlCalculator(mockYamlCalculator);
            when(mockYamlCalculator.calculatePhysicalAttack(10.0, mockRpgPlayer))
                    .thenThrow(new IllegalArgumentException("Invalid param"));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // レガシー計算: 15
            assertThat(result).isEqualTo(15);
            verify(mockLogger).warning(contains("YAML calculation failed"));
        }

        @Test
        @DisplayName("YAML計算機でRuntimeExceptionもフォールバック")
        void yamlCalculator_RuntimeException_FallsBack() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.INTELLIGENCE, 50));

            handler.setYamlCalculator(mockYamlCalculator);
            when(mockYamlCalculator.calculateMagicAttack(100.0, mockRpgPlayer))
                    .thenThrow(new RuntimeException("Calculation error"));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // レガシー計算: 107
            assertThat(result).isEqualTo(107);
            verify(mockLogger).warning(contains("YAML calculation failed"));
        }

        @Test
        @DisplayName("YAML計算機で負の結果でも1が保証される")
        void yamlCalculator_NegativeResult_MinimumOne() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            handler.setYamlCalculator(mockYamlCalculator);
            when(mockYamlCalculator.calculatePhysicalAttack(10.0, mockRpgPlayer)).thenReturn(-5.0);

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 負の値でも最低1保証
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("YAML計算機で0.001のような極小値でも1が保証される")
        void yamlCalculator_VerySmallResult_MinimumOne() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 50.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.INTELLIGENCE, 50));

            handler.setYamlCalculator(mockYamlCalculator);
            when(mockYamlCalculator.calculateMagicAttack(50.0, mockRpgPlayer)).thenReturn(0.001);

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 極小値でも最低1保証
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("YAML計算機使用時もキャッシュが機能する")
        void yamlCalculator_CacheStillWorks() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            handler.setYamlCalculator(mockYamlCalculator);
            when(mockYamlCalculator.calculatePhysicalAttack(10.0, mockRpgPlayer)).thenReturn(25.0);

            // 1回目の攻撃
            handler.handlePlayerToEntityDamage(mockEvent);

            // キャッシュにエントリがある
            assertThat(handler.getCacheSize()).isEqualTo(1);

            // 2回目はYAML計算機が呼ばれない（キャッシュ使用）
            handler.handlePlayerToEntityDamage(mockEvent);

            verify(mockYamlCalculator, times(1)).calculatePhysicalAttack(10.0, mockRpgPlayer);
        }
    }

    @Nested
    @DisplayName("ステータス計算詳細テスト")
    class StatCalculationDetailedTests {

        @Test
        @DisplayName("STRがmapに存在しない場合のデフォルト値")
        void strengthNotInMap_UsesDefaultZero() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of()); // STRなし

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // STR0なので基本ダメージそのまま
            assertThat(result).isEqualTo(100);
        }

        @Test
        @DisplayName("INTがmapに存在しない場合のデフォルト値")
        void intelligenceNotInMap_UsesDefaultZero() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of()); // INTなし

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // INT0なので基本ダメージそのまま
            assertThat(result).isEqualTo(100);
        }

        @Test
        @DisplayName("STRとINTの両方が存在する場合の物理優先")
        void bothStrAndIntExist_PhysicalUsesStr() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(
                    Stat.STRENGTH, 100,
                    Stat.INTELLIGENCE, 100
            ));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 物理ダメージなのでSTRが適用
            assertThat(result).isEqualTo(200);
        }

        @Test
        @DisplayName("STRとINTの両方が存在する場合の魔法優先")
        void bothStrAndIntExist_MagicUsesInt() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(
                    Stat.STRENGTH, 100,
                    Stat.INTELLIGENCE, 100
            ));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 魔法ダメージなのでINTが適用: 100 + 100 * 0.15 = 115
            assertThat(result).isEqualTo(115);
        }
    }

    @Nested
    @DisplayName("境界値テスト")
    class BoundaryValueTests {

        @Test
        @DisplayName("ダメージ0でも1が保証される")
        void zeroDamage_MinimumOneGuaranteed() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 0.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, -100));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("ダメージ0.1でも1が保証される")
        void verySmallDamage_MinimumOneGuaranteed() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 0.1);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.INTELLIGENCE, 0));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("極大ダメージでも正常に計算される")
        void veryLargeDamage_CalculatesCorrectly() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 99999.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 200));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 計算が正常に行われている
            assertThat(result).isPositive();
            assertThat(result).isGreaterThan(99999);
        }

        @Test
        @DisplayName("STR-200でダメージが減少しても最低1保証")
        void veryNegativeStrength_MinimumOneGuaranteed() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, -200));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 10 × (1 + (-200)/100) = -10 だが最低1保証
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("INT-100でも最低1保証")
        void veryNegativeIntelligence_MinimumOneGuaranteed() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.INTELLIGENCE, -100));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // 10 + (-100) * 0.15 = -5 だが最低1保証
            assertThat(result).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("switch式の全分岐カバレッジ")
    class SwitchExpressionCoverage {

        @Test
        @DisplayName("ENTITY_ATTACKは物理ダメージ")
        void switchCase_EntityAttack() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 100));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // STR適用: 100 × 2 = 200
            assertThat(result).isEqualTo(200);
        }

        @Test
        @DisplayName("ENTITY_SWEEP_ATTACKは物理ダメージ")
        void switchCase_EntitySweepAttack() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 100));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // STR適用: 100 × 2 = 200
            assertThat(result).isEqualTo(200);
        }

        @Test
        @DisplayName("その他のダメージタイプは魔法ダメージ")
        void switchCase_Default_Magic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.FIRE, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.INTELLIGENCE, 100));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // INT適用: 100 + 100 × 0.15 = 115
            assertThat(result).isEqualTo(115);
        }
    }

    @Nested
    @DisplayName("ログ出力詳細テスト")
    class LoggingDetailedTests {

        @Test
        @DisplayName("プレイヤー名が含まれる警告ログ")
        void warningLog_ContainsPlayerName() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockPlayerManager.getRPGPlayer(playerUuid)).thenReturn(null);
            when(mockPlayer.getName()).thenReturn("TestPlayer456");

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            assertThat(result).isEqualTo(-1);
            verify(mockLogger).warning(contains("TestPlayer456"));
        }

        @Test
        @DisplayName("非プレイヤーダメージ元は-1を返す")
        void nonPlayerDamager_ReturnsNegativeOne() {
            Entity nonPlayer = mock(Entity.class);
            when(mockEvent.getDamager()).thenReturn(nonPlayer);
            when(mockEvent.getEntity()).thenReturn(mockTarget);

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            assertThat(result).isEqualTo(-1);
            verify(mockEvent, never()).setDamage(anyDouble());
        }
    }

    @Nested
    @DisplayName("追加エッジケーステスト")
    class AdditionalEdgeCases {

        @Test
        @DisplayName("YAML計算機でIllegalStateExceptionもフォールバック")
        void yamlCalculator_IllegalStateException_FallsBack() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            YamlDamageCalculator mockYamlCalculator = mock(YamlDamageCalculator.class);
            handler.setYamlCalculator(mockYamlCalculator);
            when(mockYamlCalculator.calculatePhysicalAttack(10.0, mockRpgPlayer))
                    .thenThrow(new IllegalStateException("State error"));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // レガシー計算: 15
            assertThat(result).isEqualTo(15);
            verify(mockLogger).warning(contains("YAML calculation failed"));
        }

        @Test
        @DisplayName("YAML計算機でUnsupportedOperationExceptionもフォールバック")
        void yamlCalculator_UnsupportedOperationException_FallsBack() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 80.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.INTELLIGENCE, 50));

            YamlDamageCalculator mockYamlCalculator = mock(YamlDamageCalculator.class);
            handler.setYamlCalculator(mockYamlCalculator);
            when(mockYamlCalculator.calculateMagicAttack(80.0, mockRpgPlayer))
                    .thenThrow(new UnsupportedOperationException("Not supported"));

            double result = handler.handlePlayerToEntityDamage(mockEvent);

            // レガシー計算: 80 + 50 * 0.15 = 87.5 -> 87
            assertThat(result).isEqualTo(87);
            verify(mockLogger).warning(contains("YAML calculation failed"));
        }

        @Test
        @DisplayName("StatManagerがnullでないことを確認")
        void statManagerNotNull_Confirmed() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 50.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.STRENGTH, 50));

            handler.handlePlayerToEntityDamage(mockEvent);

            // StatManagerが呼ばれることを確認
            verify(mockRpgPlayer, atLeastOnce()).getStatManager();
        }
    }

    // ==================== ヘルパーメソッド ====================

    private void setupDamageEvent(EntityDamageEvent.DamageCause cause, double damage) {
        when(mockEvent.getDamager()).thenReturn(mockPlayer);
        when(mockEvent.getEntity()).thenReturn(mockTarget);
        when(mockEvent.getCause()).thenReturn(cause);
        when(mockEvent.getDamage()).thenReturn(damage);
    }

    /**
     * テスト用にキャッシュエントリのタイムスタンプを設定
     */
    private void setCacheEntryTimestamp(UUID playerId, long timestamp) throws Exception {
        Field cacheField = PlayerDamageHandler.class.getDeclaredField("damageCache");
        cacheField.setAccessible(true);

        @SuppressWarnings("unchecked")
        var cache = (Map<UUID, Object>) cacheField.get(handler);

        // 既存のエントリを取得
        Object existing = cache.get(playerId);
        if (existing != null) {
            // recordのコンストラクタをリフレクションで取得
            Class<?> cachedDamageClass = existing.getClass();
            var constructor = cachedDamageClass.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            Object newEntry = constructor.newInstance(
                    cachedDamageClass.getMethod("damage").invoke(existing), timestamp);

            cache.put(playerId, newEntry);
        }
    }

}
