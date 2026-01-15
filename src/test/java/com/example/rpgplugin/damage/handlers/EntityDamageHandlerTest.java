package com.example.rpgplugin.damage.handlers;

import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import net.kyori.adventure.text.Component;
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

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * EntityDamageHandlerのユニットテスト
 *
 * <p>エンティティ→プレイヤーのダメージ計算ハンドラーをテストします。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EntityDamageHandler テスト")
class EntityDamageHandlerTest {

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

    // ==================== handleEntityToPlayerDamage テスト ====================

    @Nested
    @DisplayName("handleEntityToPlayerDamage テスト")
    class HandleEntityToPlayerDamageTests {

        @Test
        @DisplayName("ターゲットがプレイヤーでない場合は-1を返す")
        void handleEntityToPlayerDamage_NonPlayerTarget_ReturnsNegativeOne() {
            Entity nonPlayer = mock(Entity.class);
            when(mockEvent.getEntity()).thenReturn(nonPlayer);
            when(mockEvent.getDamager()).thenReturn(mockDamager);

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            assertThat(result).isEqualTo(-1);
            verify(mockEvent, never()).setDamage(anyDouble());
        }

        @Test
        @DisplayName("RPGPlayerが見つからない場合は-1を返す")
        void handleEntityToPlayerDamage_RpgPlayerNotFound_ReturnsNegativeOne() {
            when(mockEvent.getEntity()).thenReturn(mockPlayer);
            when(mockEvent.getDamager()).thenReturn(mockDamager);
            when(mockPlayerManager.getRPGPlayer(playerUuid)).thenReturn(null);

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            assertThat(result).isEqualTo(-1);
            verify(mockEvent, never()).setDamage(anyDouble());
        }

        @Test
        @DisplayName("物理防御: VIT0でダメージは変化しない")
        void handleEntityToPlayerDamage_PhysicalDefense_Vit0() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 0));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            assertThat(result).isEqualTo(100);
            verify(mockEvent).setDamage(100);
        }

        @Test
        @DisplayName("物理防御: VIT50で約33%カット")
        void handleEntityToPlayerDamage_PhysicalDefense_Vit50() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 50));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 100 × (1 - 50/150) = 66.66... → 66
            assertThat(result).isEqualTo(66);
            verify(mockEvent).setDamage(66);
        }

        @Test
        @DisplayName("物理防御: VIT100で50%カット")
        void handleEntityToPlayerDamage_PhysicalDefense_Vit100() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 100 × (1 - 100/200) = 50
            assertThat(result).isEqualTo(50);
            verify(mockEvent).setDamage(50);
        }

        @Test
        @DisplayName("物理防御: 高VITで大幅カット")
        void handleEntityToPlayerDamage_PhysicalDefense_HighVit() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 200));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 100 × (1 - 200/300) = 33.33... → 33
            assertThat(result).isEqualTo(33);
            verify(mockEvent).setDamage(33);
        }

        @Test
        @DisplayName("物理防御: 範囲攻撃")
        void handleEntityToPlayerDamage_PhysicalDefense_SweepAttack() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK, 20.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 20 × 0.5 = 10
            assertThat(result).isEqualTo(10);
            verify(mockEvent).setDamage(10);
        }

        @Test
        @DisplayName("魔法防御: SPI0でダメージは変化しない")
        void handleEntityToPlayerDamage_MagicDefense_Spi0() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 0));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            assertThat(result).isEqualTo(100);
            verify(mockEvent).setDamage(100);
        }

        @Test
        @DisplayName("魔法防御: SPI50で約33%カット")
        void handleEntityToPlayerDamage_MagicDefense_Spi50() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 50));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 100 × (1 - 50/150) = 66.66... → 66
            assertThat(result).isEqualTo(66);
            verify(mockEvent).setDamage(66);
        }

        @Test
        @DisplayName("魔法防御: SPI100で50%カット")
        void handleEntityToPlayerDamage_MagicDefense_Spi100() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 100 × 0.5 = 50
            assertThat(result).isEqualTo(50);
            verify(mockEvent).setDamage(50);
        }

        @Test
        @DisplayName("魔法防御: 高SPIで大幅カット")
        void handleEntityToPlayerDamage_MagicDefense_HighSpi() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 200));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 100 × (1 - 200/300) = 33.33... → 33
            assertThat(result).isEqualTo(33);
            verify(mockEvent).setDamage(33);
        }

        @Test
        @DisplayName("ステータスが空の場合は基本ダメージのまま")
        void handleEntityToPlayerDamage_EmptyStats_UsesBaseDamage() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 15.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of());

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            assertThat(result).isEqualTo(15);
            verify(mockEvent).setDamage(15);
        }

        @Test
        @DisplayName("ダメージインジケーターを送信する")
        void handleEntityToPlayerDamage_SendsActionBar() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 0));

            handler.handleEntityToPlayerDamage(mockEvent);

            verify(mockPlayer).sendActionBar(any(Component.class));
        }
    }

    // ==================== 物理vs魔法ダメージ判定テスト ====================

    @Nested
    @DisplayName("ダメージタイプ判定テスト")
    class DamageTypeTests {

        @Test
        @DisplayName("ENTITY_ATTACKは物理ダメージ")
        void damageType_EntityAttack_IsPhysical() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 50));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // VITが適用される
            assertThat(result).isEqualTo(66);
        }

        @Test
        @DisplayName("ENTITY_SWEEP_ATTACKは物理ダメージ")
        void damageType_SweepAttack_IsPhysical() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // VITが適用される
            assertThat(result).isEqualTo(50);
        }

        @Test
        @DisplayName("MAGICは魔法ダメージ")
        void damageType_Magic_IsMagical() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPIが適用される
            assertThat(result).isEqualTo(50);
        }

        @Test
        @DisplayName("他のダメージタイプは魔法防御")
        void damageType_Other_IsMagicalDefense() {
            setupDamageEvent(EntityDamageEvent.DamageCause.FIRE, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPIが適用される
            assertThat(result).isEqualTo(50);
        }
    }

    // ==================== 複合テスト ====================

    @Nested
    @DisplayName("複合シナリオテスト")
    class CombinedScenarioTests {

        @Test
        @DisplayName("全防御ステータス補正を含む計算")
        void fullCalculation_WithAllStats() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(
                    Stat.VITALITY, 100,
                    Stat.SPIRIT, 50
            ));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 物理ダメージ: VIT100で50%カット
            assertThat(result).isEqualTo(50);
        }

        @Test
        @DisplayName("負のステータス値でも計算可能")
        void negativeStats_StillCalculates() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, -50));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 100 × (1 - (-50)/50) = 200
            assertThat(result).isEqualTo(200);
        }

        @Test
        @DisplayName("最低ダメージ1が保証される")
        void lowDamage_MinimumOneGuaranteed() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 200));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // ほぼ0になるが最低1保証
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("高防御で大幅ダメージ軽減")
        void highDefense_MassiveDamageReduction() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1000.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 500));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 1000 × (1 - 500/600) = 166.66... → 166
            assertThat(result).isEqualTo(166);
        }
    }

    // ==================== 全ダメージタイプテスト ====================

    @Nested
    @DisplayName("全ダメージタイプ カバレッジテスト")
    class AllDamageTypesCoverageTests {

        @Test
        @DisplayName("ENTITY_ATTACKは物理ダメージ（VIT適用）")
        void damageType_EntityAttack_AppliesVitality() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // VIT100で50%カット
            assertThat(result).isEqualTo(50);
        }

        @Test
        @DisplayName("ENTITY_SWEEP_ATTACKは物理ダメージ（範囲攻撃補正）")
        void damageType_SweepAttack_AppliesVitality() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // VIT100で50%カット
            assertThat(result).isEqualTo(50);
        }

        @Test
        @DisplayName("MAGICは魔法ダメージ（SPI適用）")
        void damageType_Magic_AppliesSpirit() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI100で50%カット
            assertThat(result).isEqualTo(50);
        }

        @Test
        @DisplayName("FIREは魔法ダメージ（SPI適用）")
        void damageType_Fire_AppliesSpirit() {
            setupDamageEvent(EntityDamageEvent.DamageCause.FIRE, 80.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 50));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI50で約33%カット
            assertThat(result).isEqualTo(53);
        }

        @Test
        @DisplayName("THORNSは魔法ダメージ（SPI適用）")
        void damageType_Thorns_AppliesSpirit() {
            setupDamageEvent(EntityDamageEvent.DamageCause.THORNS, 60.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 75));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI75: 60 * 100/175 = 34.28... -> 34
            assertThat(result).isEqualTo(34);
        }

        @Test
        @DisplayName("POISONは魔法ダメージ（SPI適用）")
        void damageType_Poison_AppliesSpirit() {
            setupDamageEvent(EntityDamageEvent.DamageCause.POISON, 40.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 25));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI25: 40 * 100/125 = 32
            assertThat(result).isEqualTo(32);
        }

        @Test
        @DisplayName("WITHERは魔法ダメージ（SPI適用）")
        void damageType_Wither_AppliesSpirit() {
            setupDamageEvent(EntityDamageEvent.DamageCause.WITHER, 30.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 0));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI0でカットなし
            assertThat(result).isEqualTo(30);
        }

        @Test
        @DisplayName("PROJECTILEは魔法ダメージ（SPI適用）")
        void damageType_Projectile_AppliesSpirit() {
            setupDamageEvent(EntityDamageEvent.DamageCause.PROJECTILE, 50.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI100で50%カット
            assertThat(result).isEqualTo(25);
        }

        @Test
        @DisplayName("FALLは魔法ダメージ（SPI適用）")
        void damageType_Fall_AppliesSpirit() {
            setupDamageEvent(EntityDamageEvent.DamageCause.FALL, 70.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 50));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI50: 70 * 100/150 = 46.66... -> 46
            assertThat(result).isEqualTo(46);
        }

        @Test
        @DisplayName("LIGHTNINGは魔法ダメージ（SPI適用）")
        void damageType_Lightning_AppliesSpirit() {
            setupDamageEvent(EntityDamageEvent.DamageCause.LIGHTNING, 90.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 80));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI80: 90 * 100/180 = 50
            assertThat(result).isEqualTo(50);
        }
    }

    // ==================== その他ダメージタイプテスト ====================

    @Nested
    @DisplayName("特殊ダメージタイプテスト")
    class SpecialDamageTypeTests {

        @Test
        @DisplayName("VOIDは魔法ダメージとして扱われる")
        void damageType_Void_TreatedAsMagic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.VOID, 50.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI100で50%カット
            assertThat(result).isEqualTo(25);
        }

        @Test
        @DisplayName("SUICIDEは魔法ダメージとして扱われる")
        void damageType_Suicide_TreatedAsMagic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.SUICIDE, 100.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 0));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI0でカットなし
            assertThat(result).isEqualTo(100);
        }

        @Test
        @DisplayName("HOT_FLOORは魔法ダメージとして扱われる")
        void damageType_HotFloor_TreatedAsMagic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.HOT_FLOOR, 25.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 30));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI30: 25 * 100/130 = 19.23... -> 19
            assertThat(result).isEqualTo(19);
        }

        @Test
        @DisplayName("LAVAは魔法ダメージとして扱われる")
        void damageType_Lava_TreatedAsMagic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.LAVA, 40.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 60));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI60: 40 * 100/160 = 25
            assertThat(result).isEqualTo(25);
        }

        @Test
        @DisplayName("CONTACTは魔法ダメージとして扱われる")
        void damageType_Contact_TreatedAsMagic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.CONTACT, 35.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 40));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI40: 35 * 100/140 = 25
            assertThat(result).isEqualTo(25);
        }

        @Test
        @DisplayName("DRAGON_BREATHは魔法ダメージとして扱われる")
        void damageType_DragonBreath_TreatedAsMagic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.DRAGON_BREATH, 80.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 90));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI90: 80 * 100/190 = 42.1... -> 42
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("CUSTOMは魔法ダメージとして扱われる")
        void damageType_Custom_TreatedAsMagic() {
            setupDamageEvent(EntityDamageEvent.DamageCause.CUSTOM, 60.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 70));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // SPI70: 60 * 100/170 = 35.29... -> 35
            assertThat(result).isEqualTo(35);
        }

    }

    // ==================== エッジケーステスト ====================

    @Nested
    @DisplayName("エッジケーステスト")
    class EdgeCaseTests {

        @Test
        @DisplayName("ゼロダメージでも計算が正常")
        void zeroDamage_CalculatesCorrectly() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 0.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 100));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 0カットでも最低1保証
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("極小ダメージでも計算が正常")
        void verySmallDamage_CalculatesCorrectly() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 0.5);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 200));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // 最低1保証
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("極大ダメージでも計算が正常")
        void veryLargeDamage_CalculatesCorrectly() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10000.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 1000));

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            // VIT1000で約91%カット
            assertThat(result).isEqualTo(909);
        }

        @Test
        @DisplayName("null RPGPlayerでログ警告が出力される")
        void nullRpgPlayer_LogsWarning() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 50.0);
            when(mockPlayerManager.getRPGPlayer(playerUuid)).thenReturn(null);

            double result = handler.handleEntityToPlayerDamage(mockEvent);

            assertThat(result).isEqualTo(-1);
            verify(mockLogger).warning(contains("RPGPlayer not found"));
        }

    }

    // ==================== ダメージ数値表示テスト ====================

    @Nested
    @DisplayName("ダメージ数値表示テスト")
    class DamageIndicatorTests {

        @Test
        @DisplayName("ダメージ1で表示される")
        void damageOfOne_ShowsCorrectIndicator() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 50.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 100));

            handler.handleEntityToPlayerDamage(mockEvent);

            verify(mockPlayer).sendActionBar(any(Component.class));
        }

        @Test
        @DisplayName("高ダメージでも表示される")
        void highDamage_ShowsCorrectIndicator() {
            setupDamageEvent(EntityDamageEvent.DamageCause.MAGIC, 500.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.SPIRIT, 50));

            handler.handleEntityToPlayerDamage(mockEvent);

            verify(mockPlayer).sendActionBar(any(Component.class));
        }

        @Test
        @DisplayName("カット後ダメージ1でも表示される")
        void cutToOne_ShowsIndicator() {
            setupDamageEvent(EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1.0);
            when(mockStatManager.getAllFinalStats()).thenReturn(Map.of(Stat.VITALITY, 500));

            handler.handleEntityToPlayerDamage(mockEvent);

            verify(mockPlayer).sendActionBar(any(Component.class));
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
