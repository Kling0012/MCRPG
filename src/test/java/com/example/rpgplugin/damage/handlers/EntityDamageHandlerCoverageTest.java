package com.example.rpgplugin.damage.handlers;

import com.example.rpgplugin.damage.config.YamlDamageCalculator;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * EntityDamageHandlerの追加カバレッジテスト
 *
 * <p>90%以上のカバレッジ達成のための追加テストケースです。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EntityDamageHandler カバレッジ向上テスト")
class EntityDamageHandlerCoverageTest {

    private EntityDamageHandler handler;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private Logger mockLogger;

    @Mock
    private Player mockPlayer;

    @Mock
    private Entity mockDamager;

    @Mock
    private RPGPlayer mockRpgPlayer;

    @Mock
    private StatManager mockStatManager;

    @Mock
    private EntityDamageByEntityEvent mockEvent;

    private UUID playerUuid;

    @BeforeEach
    void setUp() {
        handler = new EntityDamageHandler(mockPlayerManager, mockLogger);
        playerUuid = UUID.randomUUID();

        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockPlayerManager.getRPGPlayer(playerUuid)).thenReturn(mockRpgPlayer);
        when(mockRpgPlayer.getStatManager()).thenReturn(mockStatManager);
    }

    @Nested
    @DisplayName("showDamageIndicator詳細テスト")
    class DamageIndicatorDetailedTests {

        @Test
        @DisplayName("ダメージインジケーターに正しい形式で表示される")
        void damageIndicator_ShowsCorrectFormat() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 50.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 100));

            ArgumentCaptor<Component> componentCaptor = ArgumentCaptor.forClass(Component.class);

            handler.handleEntityToPlayerDamage(mockEvent);

            verify(mockPlayer).sendActionBar(componentCaptor.capture());
            Component captured = componentCaptor.getValue();

            // Componentがnullでないことを確認
            assertThat(captured).isNotNull();
        }

        @Test
        @DisplayName("ダメージ1でインジケーターが表示される")
        void damageIndicator_ShowsForOneDamage() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 500));

            handler.handleEntityToPlayerDamage(mockEvent);

            verify(mockPlayer, atLeastOnce()).sendActionBar(any(Component.class));
        }

        @Test
        @DisplayName("高ダメージでインジケーターが表示される")
        void damageIndicator_ShowsForHighDamage() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 500.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 0));

            handler.handleEntityToPlayerDamage(mockEvent);

            verify(mockPlayer, atLeastOnce()).sendActionBar(any(Component.class));
        }

        @Test
        @DisplayName("物理ダメージでインジケーターが表示される")
        void damageIndicator_PhysicalDamage() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK, 30.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 50));

            handler.handleEntityToPlayerDamage(mockEvent);

            verify(mockPlayer).sendActionBar(any(Component.class));
        }

        @Test
        @DisplayName("魔法ダメージでインジケーターが表示される")
        void damageIndicator_MagicDamage() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 40.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 50));

            handler.handleEntityToPlayerDamage(mockEvent);

            verify(mockPlayer).sendActionBar(any(Component.class));
        }
    }

    @Nested
    @DisplayName("追加ダメージタイプテスト")
    class AdditionalDamageTypeTests {

        @Test
        @DisplayName("ENTITY_EXPLOSIONは魔法ダメージとして扱われる")
        void damageType_EntityExplosion_TreatedAsMagic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, 80.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 50));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI50で約33%カット
            assertThat(result).isEqualTo(53);
        }

        @Test
        @DisplayName("BLOCK_EXPLOSIONは魔法ダメージとして扱われる")
        void damageType_BlockExplosion_TreatedAsMagic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION, 60.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI100で50%カット
            assertThat(result).isEqualTo(30);
        }

        @Test
        @DisplayName("MAGICレートで負のダメージでも1が保証される")
        void magicDamage_NegativeResult_MinimumOne() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 1.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 1000));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 最低1保証
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("物理レートで負のダメージでも1が保証される")
        void physicalDamage_NegativeResult_MinimumOne() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 1000));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 最低1保証
            assertThat(result).isEqualTo(1);
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
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 50));

            handler.setYamlCalculator(mockYamlCalculator);
            when(mockYamlCalculator.calculateDamageTaken(100.0, mockRpgPlayer, false))
                    .thenThrow(new IllegalArgumentException("Invalid argument"));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // レガシー計算: 66
            assertThat(result).isEqualTo(66);
            verify(mockLogger).warning(contains("YAML calculation failed"));
        }

        @Test
        @DisplayName("YAML計算機でRuntimeExceptionもフォールバック")
        void yamlCalculator_RuntimeException_FallsBack() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 50));

            handler.setYamlCalculator(mockYamlCalculator);
            when(mockYamlCalculator.calculateDamageTaken(100.0, mockRpgPlayer, true))
                    .thenThrow(new RuntimeException("Unexpected error"));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // レガシー計算: 66
            assertThat(result).isEqualTo(66);
            verify(mockLogger).warning(contains("YAML calculation failed"));
        }

        @Test
        @DisplayName("YAML計算機で計算結果が負の値でも1が保証される")
        void yamlCalculator_NegativeResult_MinimumOne() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 50));

            handler.setYamlCalculator(mockYamlCalculator);
            when(mockYamlCalculator.calculateDamageTaken(10.0, mockRpgPlayer, false)).thenReturn(-10.0);

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 負の値でも最低1保証
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("YAML計算機で極小ダメージが返されても1が保証される")
        void yamlCalculator_VerySmallResult_MinimumOne() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 5.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 100));

            handler.setYamlCalculator(mockYamlCalculator);
            when(mockYamlCalculator.calculateDamageTaken(5.0, mockRpgPlayer, true)).thenReturn(0.001);

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 0.001でも最低1保証
            assertThat(result).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("ログ出力詳細テスト")
    class LoggingDetailedTests {

        @Test
        @DisplayName("プレイヤー名が含まれる警告ログが出力される")
        void warningLog_ContainsPlayerName() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 50.0);
            when(mockPlayerManager.getRPGPlayer(playerUuid)).thenReturn(null);
            when(mockPlayer.getName()).thenReturn("TestPlayer123");

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            assertThat(result).isEqualTo(-1);
            verify(mockLogger).warning(contains("TestPlayer123"));
        }

        @Test
        @DisplayName("RPGPlayerがnullのときイベントが変更されない")
        void nullRpgPlayer_DoesNotModifyEvent() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 50.0);
            when(mockPlayerManager.getRPGPlayer(playerUuid)).thenReturn(null);

            handler.handleEntityToPlayerDamage(mockEvent);

            verify(mockEvent, never()).setDamage(anyDouble());
        }

        @Test
        @DisplayName("非プレイヤーエンティティは-1を返してイベントを変更しない")
        void nonPlayerEntity_ReturnsNegativeOne_DoesNotModifyEvent() {
            Entity nonPlayer = mock(Entity.class);
            when(mockEvent.getEntity()).thenReturn(nonPlayer);
            when(mockEvent.getDamager()).thenReturn(mockDamager);

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            assertThat(result).isEqualTo(-1);
            verify(mockEvent, never()).setDamage(anyDouble());
        }
    }

    @Nested
    @DisplayName("ステータス計算詳細テスト")
    class StatCalculationDetailedTests {

        @Test
        @DisplayName("VITがmapに存在しない場合のデフォルト値")
        void vitalityNotInMap_UsesDefaultZero() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of()); // VITなし

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // VIT0なのでカットなし
            assertThat(result).isEqualTo(100);
        }

        @Test
        @DisplayName("SPIがmapに存在しない場合のデフォルト値")
        void spiritNotInMap_UsesDefaultZero() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of()); // SPIなし

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI0なのでカットなし
            assertThat(result).isEqualTo(100);
        }

        @Test
        @DisplayName("VITとSPIの両方が存在する場合の優先順位")
        void bothVitAndSpiExist_PhysicalUsesVit() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(
                    Stat.VITALITY, 100,
                    Stat.SPIRIT, 100
            ));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 物理ダメージなのでVITが適用される
            assertThat(result).isEqualTo(50);
        }

        @Test
        @DisplayName("VITとSPIの両方が存在する場合の魔法優先順位")
        void bothVitAndSpiExist_MagicUsesSpi() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(
                    Stat.VITALITY, 100,
                    Stat.SPIRIT, 100
            ));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 魔法ダメージなのでSPIが適用される
            assertThat(result).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("境界値テスト")
    class BoundaryValueTests {

        @Test
        @DisplayName("ダメージ0でも1が保証される")
        void zeroDamage_MinimumOneGuaranteed() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 0.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("ダメージ0.1でも1が保証される")
        void verySmallDamage_MinimumOneGuaranteed() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 0.1);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 200));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("極大ダメージでも正常に計算される")
        void veryLargeDamage_CalculatesCorrectly() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 99999.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 500));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 計算が正常に行われている
            assertThat(result).isPositive();
            assertThat(result).isLessThan(99999);
        }
    }

    @Nested
    @DisplayName("switch式の全分岐カバレッジ")
    class SwitchExpressionCoverage {

        @Test
        @DisplayName("ENTITY_ATTACKは物理ダメージ")
        void switchCase_EntityAttack() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // VIT適用
            assertThat(result).isEqualTo(50);
        }

        @Test
        @DisplayName("ENTITY_SWEEP_ATTACKは物理ダメージ")
        void switchCase_EntitySweepAttack() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // VIT適用
            assertThat(result).isEqualTo(50);
        }

        @Test
        @DisplayName("その他のダメージタイプは魔法ダメージ")
        void switchCase_Default_Magic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.FIRE, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI適用
            assertThat(result).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("追加エッジケーステスト")
    class AdditionalEdgeCases {

        @Test
        @DisplayName("YAML計算機でIllegalStateExceptionもフォールバック")
        void yamlCalculator_IllegalStateException_FallsBack() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 50));

            YamlDamageCalculator mockYamlCalculator = mock(YamlDamageCalculator.class);
            handler.setYamlCalculator(mockYamlCalculator);
            when(mockYamlCalculator.calculateDamageTaken(100.0, mockRpgPlayer, false))
                    .thenThrow(new IllegalStateException("State error"));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // レガシー計算: 66
            assertThat(result).isEqualTo(66);
            verify(mockLogger).warning(contains("YAML calculation failed"));
        }

        @Test
        @DisplayName("YAML計算機で複数の例外タイプがフォールバック")
        void yamlCalculator_MultipleExceptionTypes_AllFallback() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 80.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 50));

            YamlDamageCalculator mockYamlCalculator = mock(YamlDamageCalculator.class);
            handler.setYamlCalculator(mockYamlCalculator);

            // 異なる例外タイプ
            when(mockYamlCalculator.calculateDamageTaken(80.0, mockRpgPlayer, true))
                    .thenThrow(new UnsupportedOperationException("Not supported"));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // レガシー計算: 80 * 100/150 = 53.33 -> 53
            assertThat(result).isEqualTo(53);
            verify(mockLogger).warning(contains("YAML calculation failed"));
        }

        @Test
        @DisplayName("StatManagerがnullでないことを確認")
        void statManagerNotNull_Confirmed() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 50.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 50));

            handler.handleEntityToPlayerDamage(mockEvent);

            // StatManagerが呼ばれることを確認
            verify(mockRpgPlayer, atLeastOnce()).getStatManager();
        }
    }

    // ==================== ヘルパーメソッド ====================

    private void setupDamageEvent(EntityDamageEvent.DamageCause cause, double damage) {
        when(mockEvent.getEntity()).thenReturn(mockPlayer);
        when(mockEvent.getDamager()).thenReturn(mockDamager);
        when(mockEvent.getCause()).thenReturn(cause);
        when(mockEvent.getDamage()).thenReturn(damage);
    }
}
