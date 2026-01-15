package com.example.rpgplugin.storage.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * PlayerDataのユニットテスト
 *
 * <p>プレイヤーデータモデルのテストを行います。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-I: モデルテストに特化</li>
 *   <li>KISS: シンプルで明快なテスト</li>
 *   <li>網羅性: 全メソッドをカバレッジ</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("PlayerData テスト")
class PlayerDataTest {

    private static final UUID TEST_UUID = UUID.randomUUID();
    private static final String TEST_USERNAME = "TestPlayer";
    private static final String TEST_CLASS_ID = "Warrior";

    private PlayerData playerData;

    @BeforeEach
    void setUp() {
        playerData = new PlayerData(TEST_UUID, TEST_USERNAME);
    }

    // ==================== コンストラクタ テスト ====================

    @Nested
    @DisplayName("コンストラクタ テスト")
    class ConstructorTests {

        @Test
        @DisplayName("基本コンストラクタで初期値が正しく設定される")
        void constructor_Basic_SetsCorrectDefaults() {
            assertThat(playerData.getUuid()).isEqualTo(TEST_UUID);
            assertThat(playerData.getUsername()).isEqualTo(TEST_USERNAME);
            assertThat(playerData.getClassId()).isNull();
            assertThat(playerData.getClassRank()).isEqualTo(1);
            assertThat(playerData.getClassHistory()).isNull();
            assertThat(playerData.getFirstJoin()).isPositive();
            assertThat(playerData.getLastLogin()).isPositive();
            assertThat(playerData.getMaxHealth()).isEqualTo(20);
            assertThat(playerData.getMaxMana()).isEqualTo(100);
            assertThat(playerData.getCurrentMana()).isEqualTo(100);
            assertThat(playerData.getCostType()).isEqualTo("mana");
        }

        @Test
        @DisplayName("データベースロード用コンストラクタで全フィールドが設定される")
        void constructor_DatabaseLoad_SetsAllFields() {
            long firstJoin = System.currentTimeMillis() - 1000000;
            long lastLogin = System.currentTimeMillis();

            PlayerData data = new PlayerData(
                    TEST_UUID,
                    TEST_USERNAME,
                    TEST_CLASS_ID,
                    5,
                    "Class1,Class2",
                    firstJoin,
                    lastLogin
            );

            assertThat(data.getUuid()).isEqualTo(TEST_UUID);
            assertThat(data.getUsername()).isEqualTo(TEST_USERNAME);
            assertThat(data.getClassId()).isEqualTo(TEST_CLASS_ID);
            assertThat(data.getClassRank()).isEqualTo(5);
            assertThat(data.getClassHistory()).isEqualTo("Class1,Class2");
            assertThat(data.getFirstJoin()).isEqualTo(firstJoin);
            assertThat(data.getLastLogin()).isEqualTo(lastLogin);
            // MP/HPはデフォルト値
            assertThat(data.getMaxHealth()).isEqualTo(20);
            assertThat(data.getMaxMana()).isEqualTo(100);
        }

        @Test
        @DisplayName("完全コンストラクタで全フィールドが設定される")
        void constructor_Full_SetsAllFields() {
            long firstJoin = System.currentTimeMillis() - 1000000;
            long lastLogin = System.currentTimeMillis();

            PlayerData data = new PlayerData(
                    TEST_UUID,
                    TEST_USERNAME,
                    TEST_CLASS_ID,
                    5,
                    "Class1,Class2",
                    firstJoin,
                    lastLogin,
                    25,
                    150,
                    120,
                    "hp"
            );

            assertThat(data.getUuid()).isEqualTo(TEST_UUID);
            assertThat(data.getClassId()).isEqualTo(TEST_CLASS_ID);
            assertThat(data.getClassRank()).isEqualTo(5);
            assertThat(data.getClassHistory()).isEqualTo("Class1,Class2");
            assertThat(data.getMaxHealth()).isEqualTo(25);
            assertThat(data.getMaxMana()).isEqualTo(150);
            assertThat(data.getCurrentMana()).isEqualTo(120);
            assertThat(data.getCostType()).isEqualTo("hp");
        }

        @Test
        @DisplayName("costTypeにnullを渡すとmanaになる")
        void constructor_NullCostType_DefaultsToMana() {
            PlayerData data = new PlayerData(
                    TEST_UUID,
                    TEST_USERNAME,
                    null,
                    1,
                    null,
                    0,
                    0,
                    20,
                    100,
                    100,
                    null
            );

            assertThat(data.getCostType()).isEqualTo("mana");
        }
    }

    // ==================== ゲッター/セッター テスト ====================

    @Nested
    @DisplayName("ゲッター/セッター テスト")
    class AccessorTests {

        @Test
        @DisplayName("usernameを設定・取得できる")
        void username_SetAndGet() {
            playerData.setUsername("NewName");
            assertThat(playerData.getUsername()).isEqualTo("NewName");
        }

        @Test
        @DisplayName("classIdを設定・取得できる")
        void classId_SetAndGet() {
            playerData.setClassId(TEST_CLASS_ID);
            assertThat(playerData.getClassId()).isEqualTo(TEST_CLASS_ID);
        }

        @Test
        @DisplayName("classRankを設定・取得できる")
        void classRank_SetAndGet() {
            playerData.setClassRank(10);
            assertThat(playerData.getClassRank()).isEqualTo(10);
        }

        @Test
        @DisplayName("classHistoryを設定・取得できる")
        void classHistory_SetAndGet() {
            playerData.setClassHistory("Class1,Class2,Class3");
            assertThat(playerData.getClassHistory()).isEqualTo("Class1,Class2,Class3");
        }

        @Test
        @DisplayName("lastLoginを設定・取得できる")
        void lastLogin_SetAndGet() {
            long newTime = System.currentTimeMillis() + 10000;
            playerData.setLastLogin(newTime);
            assertThat(playerData.getLastLogin()).isEqualTo(newTime);
        }

        @Test
        @DisplayName("updateLastLoginで現在時刻に更新される")
        void updateLastLogin_UpdatesToCurrentTime() {
            long before = playerData.getLastLogin();
            try {
                Thread.sleep(2); // 確実に時間が進むように待機
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            playerData.updateLastLogin();
            assertThat(playerData.getLastLogin()).isGreaterThan(before);
        }
    }

    // ==================== クラス履歴 テスト ====================

    @Nested
    @DisplayName("クラス履歴 テスト")
    class ClassHistoryTests {

        @Test
        @DisplayName("空の履歴で空リストが返される")
        void getClassHistoryList_Empty_ReturnsEmptyList() {
            List<String> history = playerData.getClassHistoryList();
            assertThat(history).isEmpty();
        }

        @Test
        @DisplayName("nullの履歴で空リストが返される")
        void getClassHistoryList_Null_ReturnsEmptyList() {
            playerData.setClassHistory(null);
            List<String> history = playerData.getClassHistoryList();
            assertThat(history).isEmpty();
        }

        @Test
        @DisplayName("履歴文字列からリストに変換される")
        void getClassHistoryList_Valid_ReturnsList() {
            playerData.setClassHistory("Warrior,Mage,Rogue");
            List<String> history = playerData.getClassHistoryList();
            assertThat(history).containsExactly("Warrior", "Mage", "Rogue");
        }

        @Test
        @DisplayName("リストから履歴文字列に変換される")
        void setClassHistoryList_Valid_SetsString() {
            playerData.setClassHistoryList(List.of("Warrior", "Mage", "Rogue"));
            assertThat(playerData.getClassHistory()).isEqualTo("Warrior,Mage,Rogue");
        }

        @Test
        @DisplayName("空リストを設定するとnullになる")
        void setClassHistoryList_Empty_SetsNull() {
            playerData.setClassHistoryList(List.of());
            assertThat(playerData.getClassHistory()).isNull();
        }

        @Test
        @DisplayName("nullを設定するとnullになる")
        void setClassHistoryList_Null_SetsNull() {
            playerData.setClassHistory("Warrior,Mage");
            playerData.setClassHistoryList(null);
            assertThat(playerData.getClassHistory()).isNull();
        }

        @Test
        @DisplayName("クラス履歴に追加できる")
        void addClassToHistory_NewClass_AddsToList() {
            playerData.addClassToHistory("Warrior");
            playerData.addClassToHistory("Mage");
            assertThat(playerData.getClassHistoryList()).containsExactly("Warrior", "Mage");
        }

        @Test
        @DisplayName("既存のクラスは重複して追加されない")
        void addClassToHistory_ExistingClass_DoesNotDuplicate() {
            playerData.addClassToHistory("Warrior");
            playerData.addClassToHistory("Mage");
            playerData.addClassToHistory("Warrior");
            assertThat(playerData.getClassHistoryList()).containsExactly("Warrior", "Mage");
        }
    }

    // ==================== MP/HP テスト ====================

    @Nested
    @DisplayName("MP/HP テスト")
    class ManaHealthTests {

        @Test
        @DisplayName("maxHealthを設定・取得できる")
        void maxHealth_SetAndGet() {
            playerData.setMaxHealth(30);
            assertThat(playerData.getMaxHealth()).isEqualTo(30);
        }

        @Test
        @DisplayName("maxHealthに負の値を設定すると0になる")
        void maxHealth_NegativeValue_BecomesZero() {
            playerData.setMaxHealth(-10);
            assertThat(playerData.getMaxHealth()).isEqualTo(0);
        }

        @Test
        @DisplayName("maxManaを設定・取得できる")
        void maxMana_SetAndGet() {
            playerData.setMaxMana(200);
            assertThat(playerData.getMaxMana()).isEqualTo(200);
        }

        @Test
        @DisplayName("maxManaを下げるとcurrentManaも調整される")
        void maxMana_Lower_AdjustsCurrentMana() {
            playerData.setCurrentMana(80);
            playerData.setMaxMana(50);
            assertThat(playerData.getCurrentMana()).isEqualTo(50);
        }

        @Test
        @DisplayName("maxManaに負の値を設定すると0になる")
        void maxMana_NegativeValue_BecomesZero() {
            playerData.setMaxMana(-10);
            assertThat(playerData.getMaxMana()).isEqualTo(0);
            assertThat(playerData.getCurrentMana()).isEqualTo(0);
        }

        @Test
        @DisplayName("currentManaを設定・取得できる")
        void currentMana_SetAndGet() {
            playerData.setCurrentMana(50);
            assertThat(playerData.getCurrentMana()).isEqualTo(50);
        }

        @Test
        @DisplayName("currentManaはmaxManaを超えない")
        void currentMana_ExceedsMax_ClampedToMax() {
            playerData.setCurrentMana(150);
            assertThat(playerData.getCurrentMana()).isEqualTo(100); // maxManaのデフォルト
        }

        @Test
        @DisplayName("currentManaに負の値を設定すると0になる")
        void currentMana_NegativeValue_BecomesZero() {
            playerData.setCurrentMana(-10);
            assertThat(playerData.getCurrentMana()).isEqualTo(0);
        }

        @Test
        @DisplayName("addManaでMPが増加する")
        void addMana_PositiveAmount_IncreasesMana() {
            playerData.setCurrentMana(50);
            int added = playerData.addMana(30);
            assertThat(added).isEqualTo(30);
            assertThat(playerData.getCurrentMana()).isEqualTo(80);
        }

        @Test
        @DisplayName("addManaで最大MPを超えない")
        void addMana_ExceedsMax_ClampsToMax() {
            playerData.setCurrentMana(80);
            int added = playerData.addMana(50);
            assertThat(added).isEqualTo(20); // 100 - 80 = 20
            assertThat(playerData.getCurrentMana()).isEqualTo(100);
        }

        @Test
        @DisplayName("addManaに0以下を渡すと何もしない")
        void addMana_ZeroOrNegative_DoesNothing() {
            playerData.setCurrentMana(50);
            assertThat(playerData.addMana(0)).isEqualTo(0);
            assertThat(playerData.addMana(-10)).isEqualTo(0);
            assertThat(playerData.getCurrentMana()).isEqualTo(50);
        }

        @Test
        @DisplayName("consumeManaでMPを消費できる")
        void consumeMana_SufficientAmount_ReturnsTrue() {
            playerData.setCurrentMana(80);
            boolean result = playerData.consumeMana(30);
            assertThat(result).isTrue();
            assertThat(playerData.getCurrentMana()).isEqualTo(50);
        }

        @Test
        @DisplayName("consumeManaでMP不足時は消費されない")
        void consumeMana_Insufficient_ReturnsFalse() {
            playerData.setCurrentMana(20);
            boolean result = playerData.consumeMana(30);
            assertThat(result).isFalse();
            assertThat(playerData.getCurrentMana()).isEqualTo(20);
        }

        @Test
        @DisplayName("consumeManaに0を渡すと成功する")
        void consumeMana_Zero_ReturnsTrue() {
            playerData.setCurrentMana(50);
            boolean result = playerData.consumeMana(0);
            assertThat(result).isTrue();
            assertThat(playerData.getCurrentMana()).isEqualTo(50);
        }

        @Test
        @DisplayName("consumeManaに負の値を渡すと成功する")
        void consumeMana_Negative_ReturnsTrue() {
            playerData.setCurrentMana(50);
            boolean result = playerData.consumeMana(-10);
            assertThat(result).isTrue();
            assertThat(playerData.getCurrentMana()).isEqualTo(50);
        }
    }

    // ==================== コストタイプ テスト ====================

    @Nested
    @DisplayName("コストタイプ テスト")
    class CostTypeTests {

        @Test
        @DisplayName("コストタイプを設定・取得できる")
        void costType_SetAndGet() {
            playerData.setCostType("hp");
            assertThat(playerData.getCostType()).isEqualTo("hp");
        }

        @Test
        @DisplayName("manaは有効なコストタイプ")
        void setCostType_Mana_SetsMana() {
            playerData.setCostType("mana");
            assertThat(playerData.getCostType()).isEqualTo("mana");
        }

        @Test
        @DisplayName("hpは有効なコストタイプ")
        void setCostType_Hp_SetsHp() {
            playerData.setCostType("hp");
            assertThat(playerData.getCostType()).isEqualTo("hp");
        }

        @Test
        @DisplayName("healthもhpとして認識される")
        void setCostType_Health_SetsHp() {
            playerData.setCostType("health");
            assertThat(playerData.getCostType()).isEqualTo("hp");
        }

        @Test
        @DisplayName("無効なコストタイプはmanaになる")
        void setCostType_Invalid_DefaultsToMana() {
            playerData.setCostType("invalid");
            assertThat(playerData.getCostType()).isEqualTo("mana");
        }

        @Test
        @DisplayName("nullのコストタイプはmanaになる")
        void setCostType_Null_DefaultsToMana() {
            playerData.setCostType(null);
            assertThat(playerData.getCostType()).isEqualTo("mana");
        }

        @Test
        @DisplayName("大文字小文字を区別しない")
        void setCostType_CaseInsensitive_Works() {
            playerData.setCostType("HP");
            assertThat(playerData.getCostType()).isEqualTo("hp");
            playerData.setCostType("MANA");
            assertThat(playerData.getCostType()).isEqualTo("mana");
        }

        @Test
        @DisplayName("isManaCostTypeで判定できる")
        void isManaCostType_Mana_ReturnsTrue() {
            playerData.setCostType("mana");
            assertThat(playerData.isManaCostType()).isTrue();
        }

        @Test
        @DisplayName("isManaCostTypeでhpを判定できる")
        void isManaCostType_Hp_ReturnsFalse() {
            playerData.setCostType("hp");
            assertThat(playerData.isManaCostType()).isFalse();
        }

        @Test
        @DisplayName("toggleCostTypeで切り替わる")
        void toggleCostType_ManaToHp_Switches() {
            assertThat(playerData.isManaCostType()).isTrue();
            playerData.toggleCostType();
            assertThat(playerData.isManaCostType()).isFalse();
            playerData.toggleCostType();
            assertThat(playerData.isManaCostType()).isTrue();
        }
    }

    // ==================== equals/hashCode/toString テスト ====================

    @Nested
    @DisplayName("equals/hashCode/toString テスト")
    class ObjectMethodsTests {

        @Test
        @DisplayName("同じUUIDでequalsがtrueになる")
        void equals_SameUuid_ReturnsTrue() {
            PlayerData data1 = new PlayerData(TEST_UUID, "Player1");
            PlayerData data2 = new PlayerData(TEST_UUID, "Player2");
            assertThat(data1).isEqualTo(data2);
        }

        @Test
        @DisplayName("異なるUUIDでequalsがfalseになる")
        void equals_DifferentUuid_ReturnsFalse() {
            PlayerData data1 = new PlayerData(UUID.randomUUID(), "Player");
            PlayerData data2 = new PlayerData(UUID.randomUUID(), "Player");
            assertThat(data1).isNotEqualTo(data2);
        }

        @Test
        @DisplayName("自分自身とequalsがtrueになる")
        void equals_SameInstance_ReturnsTrue() {
            assertThat(playerData).isEqualTo(playerData);
        }

        @Test
        @DisplayName("nullとequalsがfalseになる")
        void equals_Null_ReturnsFalse() {
            assertThat(playerData).isNotEqualTo(null);
        }

        @Test
        @DisplayName("異なるクラスとequalsがfalseになる")
        void equals_DifferentClass_ReturnsFalse() {
            assertThat(playerData).isNotEqualTo("string");
        }

        @Test
        @DisplayName("同じUUIDでhashCodeが等しい")
        void hashCode_SameUuid_ReturnsSameValue() {
            PlayerData data1 = new PlayerData(TEST_UUID, "Player1");
            PlayerData data2 = new PlayerData(TEST_UUID, "Player2");
            assertThat(data1.hashCode()).isEqualTo(data2.hashCode());
        }

        @Test
        @DisplayName("toStringに全フィールドが含まれる")
        void toString_ContainsAllFields() {
            String str = playerData.toString();
            assertThat(str).contains("uuid=" + TEST_UUID);
            assertThat(str).contains("username='" + TEST_USERNAME + "'");
            assertThat(str).contains("maxHealth=20");
            assertThat(str).contains("maxMana=100");
            assertThat(str).contains("currentMana=100");
            assertThat(str).contains("costType='mana'");
        }
    }
}
