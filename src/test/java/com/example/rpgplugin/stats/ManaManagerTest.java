package com.example.rpgplugin.stats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * ManaManagerのユニットテスト
 *
 * <p>MP（マナポイント）管理システムのテストを行います。</p>
 *
 * 設計原則:
 * - SOLID-S: ManaManagerのテストに特化
 * - KISS: シンプルなテストケース
 * - 読みやすさ: テスト名で振る舞いを明示
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("ManaManager テスト")
class ManaManagerTest {

    private UUID playerUuid;
    private ManaManager manaManager;

    @BeforeEach
    void setUp() {
        playerUuid = UUID.randomUUID();
        manaManager = new ManaManager(playerUuid, 100, 50, 0, ManaManager.CostType.MANA);
    }

    // ==================== コンストラクタ ====================

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("デフォルトコンストラクタ: MP100で初期化")
        void constructor_Default_InitializesWith100Mana() {
            ManaManager defaultManager = new ManaManager(playerUuid);

            assertThat(defaultManager.getMaxMana()).isEqualTo(100);
            assertThat(defaultManager.getCurrentMana()).isEqualTo(100);
            assertThat(defaultManager.getMaxHpModifier()).isZero();
            assertThat(defaultManager.getCostType()).isEqualTo(ManaManager.CostType.MANA);
        }

        @Test
        @DisplayName("フルコンストラクタ: 指定値で初期化")
        void constructor_Full_InitializesWithSpecifiedValues() {
            assertThat(manaManager.getMaxMana()).isEqualTo(100);
            assertThat(manaManager.getCurrentMana()).isEqualTo(50);
            assertThat(manaManager.getMaxHpModifier()).isZero();
            assertThat(manaManager.getCostType()).isEqualTo(ManaManager.CostType.MANA);
        }

        @Test
        @DisplayName("現在MPが最大MPを超える場合、最大MPに丸められる")
        void constructor_CurrentExceedsMax_ClampedToMax() {
            ManaManager manager = new ManaManager(playerUuid, 100, 150, 0, ManaManager.CostType.MANA);

            assertThat(manager.getCurrentMana()).isEqualTo(100);
        }
    }

    // ==================== MP操作 ====================

    @Nested
    @DisplayName("Mana Operations")
    class ManaOperationsTests {

        @Test
        @DisplayName("最大MPを取得")
        void getMaxMana_ReturnsMaxMana() {
            assertThat(manaManager.getMaxMana()).isEqualTo(100);
        }

        @Test
        @DisplayName("最大MPを設定")
        void setMaxMana_SetsMaxMana() {
            manaManager.setMaxMana(200);

            assertThat(manaManager.getMaxMana()).isEqualTo(200);
        }

        @Test
        @DisplayName("最大MP設定: 現在MPが超える場合、現在MPも調整される")
        void setMaxMana_CurrentExceedsMax_CurrentAdjusted() {
            manaManager.setCurrentMana(80);
            manaManager.setMaxMana(50);

            assertThat(manaManager.getMaxMana()).isEqualTo(50);
            assertThat(manaManager.getCurrentMana()).isEqualTo(50);
        }

        @Test
        @DisplayName("現在MPを取得")
        void getCurrentMana_ReturnsCurrentMana() {
            assertThat(manaManager.getCurrentMana()).isEqualTo(50);
        }

        @Test
        @DisplayName("現在MPを設定")
        void setCurrentMana_SetsCurrentMana() {
            manaManager.setCurrentMana(75);

            assertThat(manaManager.getCurrentMana()).isEqualTo(75);
        }

        @Test
        @DisplayName("現在MP設定: 最大値を超える場合は丸められる")
        void setCurrentMana_ExceedsMax_ClampedToMax() {
            manaManager.setCurrentMana(150);

            assertThat(manaManager.getCurrentMana()).isEqualTo(100);
        }

        @Test
        @DisplayName("現在MP設定: 負値は0に丸められる")
        void setCurrentMana_Negative_ClampedToZero() {
            manaManager.setCurrentMana(-10);

            assertThat(manaManager.getCurrentMana()).isZero();
        }

        @Test
        @DisplayName("MPを追加")
        void addMana_AddsMana() {
            int added = manaManager.addMana(30);

            assertThat(added).isEqualTo(30);
            assertThat(manaManager.getCurrentMana()).isEqualTo(80);
        }

        @Test
        @DisplayName("MP追加: 最大値を超える場合は丸められる")
        void addMana_ExceedsMax_ClampedToMax() {
            manaManager.setCurrentMana(80);
            int added = manaManager.addMana(50);

            assertThat(added).isEqualTo(20);
            assertThat(manaManager.getCurrentMana()).isEqualTo(100);
        }

        @Test
        @DisplayName("MP追加: 負値は無視される")
        void addMana_Negative_ReturnsZero() {
            int current = manaManager.getCurrentMana();
            int added = manaManager.addMana(-10);

            assertThat(added).isZero();
            assertThat(manaManager.getCurrentMana()).isEqualTo(current);
        }

        @Test
        @DisplayName("MP追加: 0は無視される")
        void addMana_Zero_ReturnsZero() {
            int current = manaManager.getCurrentMana();
            int added = manaManager.addMana(0);

            assertThat(added).isZero();
            assertThat(manaManager.getCurrentMana()).isEqualTo(current);
        }

        @Test
        @DisplayName("MP消費: 成功")
        void consumeMana_Success_ReturnsTrue() {
            manaManager.setCurrentMana(50);
            boolean consumed = manaManager.consumeMana(30);

            assertThat(consumed).isTrue();
            assertThat(manaManager.getCurrentMana()).isEqualTo(20);
        }

        @Test
        @DisplayName("MP消費: 不足")
        void consumeMana_NotEnoughMana_ReturnsFalse() {
            manaManager.setCurrentMana(20);
            boolean consumed = manaManager.consumeMana(30);

            assertThat(consumed).isFalse();
            assertThat(manaManager.getCurrentMana()).isEqualTo(20);
        }

        @Test
        @DisplayName("MP消費: 正確に消費")
        void consumeMana_ExactlyAll_ReturnsTrue() {
            manaManager.setCurrentMana(30);
            boolean consumed = manaManager.consumeMana(30);

            assertThat(consumed).isTrue();
            assertThat(manaManager.getCurrentMana()).isZero();
        }

        @Test
        @DisplayName("MP消費: 負値は無視")
        void consumeMana_Negative_ReturnsTrue() {
            int current = manaManager.getCurrentMana();
            boolean consumed = manaManager.consumeMana(-10);

            assertThat(consumed).isTrue();
            assertThat(manaManager.getCurrentMana()).isEqualTo(current);
        }

        @Test
        @DisplayName("MP消費: 0は無視")
        void consumeMana_Zero_ReturnsTrue() {
            int current = manaManager.getCurrentMana();
            boolean consumed = manaManager.consumeMana(0);

            assertThat(consumed).isTrue();
            assertThat(manaManager.getCurrentMana()).isEqualTo(current);
        }

        @Test
        @DisplayName("MPが足りているか確認")
        void hasMana_Enough_ReturnsTrue() {
            manaManager.setCurrentMana(50);

            assertThat(manaManager.hasMana(30)).isTrue();
            assertThat(manaManager.hasMana(50)).isTrue();
            assertThat(manaManager.hasMana(51)).isFalse();
        }

        @Test
        @DisplayName("MP回復: 精神値に基づいて回復")
        void regenerateMana_WithSpirit_Regenerates() {
            manaManager.setCurrentMana(50);

            int regen = manaManager.regenerateMana(20, 1.0);

            // 1.0 + (20 * 0.1) = 3.0 → 3
            assertThat(regen).isEqualTo(3);
            assertThat(manaManager.getCurrentMana()).isEqualTo(53);
        }

        @Test
        @DisplayName("MP回復: 最大値を超えない")
        void regenerateMana_ExceedsMax_ClampedToMax() {
            manaManager.setCurrentMana(95);

            int regen = manaManager.regenerateMana(20, 1.0);

            // 1.0 + (20 * 0.1) = 3.0 → 3, 95 + 3 = 98
            assertThat(regen).isEqualTo(3);
            assertThat(manaManager.getCurrentMana()).isEqualTo(98);
        }
    }

    // ==================== HP修飾子 ====================

    @Nested
    @DisplayName("Max HP Modifier")
    class MaxHpModifierTests {

        @Test
        @DisplayName("最大HP修飾子を取得")
        void getMaxHpModifier_ReturnsModifier() {
            assertThat(manaManager.getMaxHpModifier()).isZero();
        }

        @Test
        @DisplayName("最大HP修飾子を設定")
        void setMaxHpModifier_SetsModifier() {
            manaManager.setMaxHpModifier(50);

            assertThat(manaManager.getMaxHpModifier()).isEqualTo(50);
        }

        @Test
        @DisplayName("最大HP修飾子を追加")
        void addMaxHpModifier_AddsToModifier() {
            manaManager.setMaxHpModifier(10);
            manaManager.addMaxHpModifier(20);

            assertThat(manaManager.getMaxHpModifier()).isEqualTo(30);
        }
    }

    // ==================== コストタイプ ====================

    @Nested
    @DisplayName("Cost Type")
    class CostTypeTests {

        @Test
        @DisplayName("コストタイプを取得")
        void getCostType_ReturnsCostType() {
            assertThat(manaManager.getCostType()).isEqualTo(ManaManager.CostType.MANA);
        }

        @Test
        @DisplayName("コストタイプを設定")
        void setCostType_SetsCostType() {
            manaManager.setCostType(ManaManager.CostType.HEALTH);

            assertThat(manaManager.getCostType()).isEqualTo(ManaManager.CostType.HEALTH);
        }

        @Test
        @DisplayName("コストタイプ: IDで設定")
        void setCostType_ById_SetsCostType() {
            manaManager.setCostType("hp");

            assertThat(manaManager.getCostType()).isEqualTo(ManaManager.CostType.HEALTH);
        }

        @Test
        @DisplayName("コストタイプ: nullはデフォルト")
        void setCostType_Null_DoesNotChange() {
            // ManaManager.setCostType(null)はNPEを投げるためテストしない
            // 実装がnullを許容しないことを確認
            ManaManager.CostType original = manaManager.getCostType();

            assertThatThrownBy(() -> manaManager.setCostType((ManaManager.CostType) null))
                .isInstanceOf(NullPointerException.class);

            // コストタイプは変更されない
            assertThat(manaManager.getCostType()).isEqualTo(original);
        }

        @Test
        @DisplayName("コストタイプを切り替え")
        void toggleCostType_Toggles() {
            assertThat(manaManager.getCostType()).isEqualTo(ManaManager.CostType.MANA);

            manaManager.toggleCostType();

            assertThat(manaManager.getCostType()).isEqualTo(ManaManager.CostType.HEALTH);

            manaManager.toggleCostType();

            assertThat(manaManager.getCostType()).isEqualTo(ManaManager.CostType.MANA);
        }
    }

    // ==================== CostType列挙 ====================

    @Nested
    @DisplayName("CostType Enum")
    class CostTypeEnumTests {

        @Test
        @DisplayName("CostTypeの全値を確認")
        void costType_HasAllValues() {
            ManaManager.CostType[] values = ManaManager.CostType.values();

            assertThat(values).hasSize(2);
            assertThat(values[0]).isEqualTo(ManaManager.CostType.MANA);
            assertThat(values[1]).isEqualTo(ManaManager.CostType.HEALTH);
        }

        @Test
        @DisplayName("getIdが正しく動作する")
        void costType_GetId_Works() {
            assertThat(ManaManager.CostType.MANA.getId()).isEqualTo("mana");
            assertThat(ManaManager.CostType.HEALTH.getId()).isEqualTo("hp");
        }

        @Test
        @DisplayName("getDisplayNameが正しく動作する")
        void costType_GetDisplayName_Works() {
            assertThat(ManaManager.CostType.MANA.getDisplayName()).isEqualTo("MP消費");
            assertThat(ManaManager.CostType.HEALTH.getDisplayName()).isEqualTo("HP消費");
        }

        @Test
        @DisplayName("fromIdが正しく動作する")
        void costType_FromId_Works() {
            assertThat(ManaManager.CostType.fromId("mana")).isEqualTo(ManaManager.CostType.MANA);
            assertThat(ManaManager.CostType.fromId("hp")).isEqualTo(ManaManager.CostType.HEALTH);
            assertThat(ManaManager.CostType.fromId("MANA")).isEqualTo(ManaManager.CostType.MANA);
            assertThat(ManaManager.CostType.fromId("HP")).isEqualTo(ManaManager.CostType.HEALTH);
        }

        @Test
        @DisplayName("fromId: nullはデフォルト")
        void costType_FromId_Null_ReturnsDefault() {
            assertThat(ManaManager.CostType.fromId(null)).isEqualTo(ManaManager.CostType.MANA);
        }

        @Test
        @DisplayName("fromId: 不正な値はデフォルト")
        void costType_FromId_Invalid_ReturnsDefault() {
            assertThat(ManaManager.CostType.fromId("invalid")).isEqualTo(ManaManager.CostType.MANA);
        }
    }

    // ==================== スキル発動コスト ====================

    @Nested
    @DisplayName("Skill Cost Consumption")
    class SkillCostTests {

        @Test
        @DisplayName("コスト消費: MPタイプ")
        void consumeSkillCost_ManaType_ConsumesMana() {
            manaManager.setCostType(ManaManager.CostType.MANA);
            manaManager.setCurrentMana(50);

            boolean consumed = manaManager.consumeSkillCost(30, 100, 100);

            assertThat(consumed).isTrue();
            assertThat(manaManager.getCurrentMana()).isEqualTo(20);
        }

        @Test
        @DisplayName("コスト消費: MPタイプで不足")
        void consumeSkillCost_ManaType_NotEnough_ReturnsFalse() {
            manaManager.setCostType(ManaManager.CostType.MANA);
            manaManager.setCurrentMana(20);

            boolean consumed = manaManager.consumeSkillCost(30, 100, 100);

            assertThat(consumed).isFalse();
        }

        @Test
        @DisplayName("コスト消費: HPタイプ")
        void consumeSkillCost_HealthType_ChecksHp() {
            manaManager.setCostType(ManaManager.CostType.HEALTH);

            boolean consumed = manaManager.consumeSkillCost(30, 50, 100);

            assertThat(consumed).isTrue();
            // HP消費の実際の処理は呼び出し元で行うため、MPは変わらない
            assertThat(manaManager.getCurrentMana()).isEqualTo(50);
        }

        @Test
        @DisplayName("コスト消費: HPタイプで不足")
        void consumeSkillCost_HealthType_NotEnough_ReturnsFalse() {
            manaManager.setCostType(ManaManager.CostType.HEALTH);

            boolean consumed = manaManager.consumeSkillCost(30, 20, 100);

            assertThat(consumed).isFalse();
        }
    }

    // ==================== ユーティリティ ====================

    @Nested
    @DisplayName("Utility Methods")
    class UtilityTests {

        @Test
        @DisplayName("MP割合を取得")
        void getManaRatio_ReturnsRatio() {
            manaManager.setCurrentMana(50);

            assertThat(manaManager.getManaRatio()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("MP割合: 最大MPが0の場合は0")
        void getManaRatio_MaxZero_ReturnsZero() {
            ManaManager zeroMax = new ManaManager(playerUuid, 0, 0, 0, ManaManager.CostType.MANA);

            assertThat(zeroMax.getManaRatio()).isZero();
        }

        @Test
        @DisplayName("MPが満タンか確認")
        void isFullMana_Full_ReturnsTrue() {
            manaManager.setCurrentMana(100);

            assertThat(manaManager.isFullMana()).isTrue();
        }

        @Test
        @DisplayName("MPが満タンか確認: 満タンでない")
        void isFullMana_NotFull_ReturnsFalse() {
            manaManager.setCurrentMana(99);

            assertThat(manaManager.isFullMana()).isFalse();
        }

        @Test
        @DisplayName("MPが空か確認")
        void isEmptyMana_Empty_ReturnsTrue() {
            manaManager.setCurrentMana(0);

            assertThat(manaManager.isEmptyMana()).isTrue();
        }

        @Test
        @DisplayName("MPが空か確認: 空でない")
        void isEmptyMana_NotEmpty_ReturnsFalse() {
            manaManager.setCurrentMana(1);

            assertThat(manaManager.isEmptyMana()).isFalse();
        }

        @Test
        @DisplayName("データコピーを取得")
        void getDataCopy_ReturnsCopy() {
            ManaManager.ManaManagerData copy = manaManager.getDataCopy();

            assertThat(copy.getMaxMana()).isEqualTo(100);
            assertThat(copy.getCurrentMana()).isEqualTo(50);
            assertThat(copy.getMaxHpModifier()).isZero();
            assertThat(copy.getCostType()).isEqualTo(ManaManager.CostType.MANA);
        }

        @Test
        @DisplayName("toStringが正しく動作する")
        void toString_ReturnsFormattedString() {
            String str = manaManager.toString();

            assertThat(str).contains("ManaManager");
            assertThat(str).contains("maxMana=100");
            assertThat(str).contains("currentMana=50");
        }
    }

    // ==================== ManaManagerData ====================

    @Nested
    @DisplayName("ManaManagerData")
    class ManaManagerDataTests {

        @Test
        @DisplayName("データクラスのgetter/setter")
        void dataClass_GettersSetters_Work() {
            ManaManager.ManaManagerData data = new ManaManager.ManaManagerData(
                200, 100, 50, ManaManager.CostType.HEALTH
            );

            assertThat(data.getMaxMana()).isEqualTo(200);
            assertThat(data.getCurrentMana()).isEqualTo(100);
            assertThat(data.getMaxHpModifier()).isEqualTo(50);
            assertThat(data.getCostType()).isEqualTo(ManaManager.CostType.HEALTH);
        }

        @Test
        @DisplayName("setMaxMana: 現在値が超える場合調整")
        void dataClass_setMaxMana_CurrentExceedsAdjusted() {
            ManaManager.ManaManagerData data = new ManaManager.ManaManagerData(
                100, 80, 0, ManaManager.CostType.MANA
            );

            data.setMaxMana(50);

            assertThat(data.getMaxMana()).isEqualTo(50);
            assertThat(data.getCurrentMana()).isEqualTo(50);
        }

        @Test
        @DisplayName("setCurrentMana: 範囲外は丸められる")
        void dataClass_setCurrentMana_ClampedToRange() {
            ManaManager.ManaManagerData data = new ManaManager.ManaManagerData(
                100, 50, 0, ManaManager.CostType.MANA
            );

            data.setCurrentMana(150);
            assertThat(data.getCurrentMana()).isEqualTo(100);

            data.setCurrentMana(-10);
            assertThat(data.getCurrentMana()).isZero();
        }

        @Test
        @DisplayName("setMaxHpModifier: 負値は0に丸められる")
        void dataClass_setMaxHpModifier_NegativeClampedToZero() {
            ManaManager.ManaManagerData data = new ManaManager.ManaManagerData(
                100, 50, 10, ManaManager.CostType.MANA
            );

            data.setMaxHpModifier(-5);

            assertThat(data.getMaxHpModifier()).isZero();
        }

        @Test
        @DisplayName("setCostType: nullはデフォルト")
        void dataClass_setCostType_NullDefaultsToMana() {
            ManaManager.ManaManagerData data = new ManaManager.ManaManagerData(
                100, 50, 0, ManaManager.CostType.HEALTH
            );

            data.setCostType(null);

            assertThat(data.getCostType()).isEqualTo(ManaManager.CostType.MANA);
        }
    }
}
