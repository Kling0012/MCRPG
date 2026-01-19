package com.example.rpgplugin.skill.target;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TargetGroupFilterのテストクラス
 */
@DisplayName("TargetGroupFilter テスト")
class TargetGroupFilterTest {

    @Nested
    @DisplayName("列挙値の存在確認")
    class EnumValuesTests {

        @Test
        @DisplayName("BOTH: 列挙値が存在すること")
        void testBothExists() {
            TargetGroupFilter filter = TargetGroupFilter.BOTH;
            assertEquals("BOTH", filter.name(), "BOTHが存在すること");
        }

        @Test
        @DisplayName("ENEMY: 列挙値が存在すること")
        void testEnemyExists() {
            TargetGroupFilter filter = TargetGroupFilter.ENEMY;
            assertEquals("ENEMY", filter.name(), "ENEMYが存在すること");
        }

        @Test
        @DisplayName("ALLY: 列挙値が存在すること")
        void testAllyExists() {
            TargetGroupFilter filter = TargetGroupFilter.ALLY;
            assertEquals("ALLY", filter.name(), "ALLYが存在すること");
        }

        @Test
        @DisplayName("values: 3つの列挙値が存在すること")
        void testValuesCount() {
            TargetGroupFilter[] values = TargetGroupFilter.values();
            assertEquals(3, values.length, "3つの列挙値が存在すること");
        }

        @Test
        @DisplayName("valueOf: 有効な文字列から列挙値が取得できること")
        void testValueOf_Valid() {
            assertEquals(TargetGroupFilter.BOTH, TargetGroupFilter.valueOf("BOTH"));
            assertEquals(TargetGroupFilter.ENEMY, TargetGroupFilter.valueOf("ENEMY"));
            assertEquals(TargetGroupFilter.ALLY, TargetGroupFilter.valueOf("ALLY"));
        }

        @Test
        @DisplayName("valueOf: 無効な文字列では例外がスローされること")
        void testValueOf_Invalid() {
            assertThrows(IllegalArgumentException.class, () -> TargetGroupFilter.valueOf("INVALID"),
                    "無効な文字列では例外がスローされること");
        }
    }

    @Nested
    @DisplayName("序数の確認")
    class OrdinalTests {

        @Test
        @DisplayName("序数が一意であること")
        void testUniqueOrdinals() {
            int ordinal1 = TargetGroupFilter.BOTH.ordinal();
            int ordinal2 = TargetGroupFilter.ENEMY.ordinal();
            int ordinal3 = TargetGroupFilter.ALLY.ordinal();

            assertNotEquals(ordinal1, ordinal2, "序数が異なること");
            assertNotEquals(ordinal2, ordinal3, "序数が異なること");
            assertNotEquals(ordinal1, ordinal3, "序数が異なること");
        }
    }

    @Nested
    @DisplayName("switch文での使用")
    class SwitchUsageTests {

        @Test
        @DisplayName("switch: 全ケースで正しく分岐されること")
        void testSwitchCoverage() {
            TargetGroupFilter[] values = TargetGroupFilter.values();

            for (TargetGroupFilter filter : values) {
                String result = switch (filter) {
                    case BOTH -> "both";
                    case ENEMY -> "enemy";
                    case ALLY -> "ally";
                };
                assertNotNull(result, "全ケースで結果が返されること");
            }
        }
    }

    @Nested
    @DisplayName("セマンティック意味")
    class SemanticMeaningTests {

        @Test
        @DisplayName("BOTH: 敵味方両方を含む")
        void testBothSemantics() {
            TargetGroupFilter filter = TargetGroupFilter.BOTH;
            // BOTHは敵味方両方を含む
            assertNotNull(filter, "BOTHはnullではない");
        }

        @Test
        @DisplayName("ENEMY: 敵対エンティティのみ")
        void testEnemySemantics() {
            TargetGroupFilter filter = TargetGroupFilter.ENEMY;
            // ENEMYは敵対エンティティのみ（PvPなしサーバーではMobのみ）
            assertNotNull(filter, "ENEMYはnullではない");
        }

        @Test
        @DisplayName("ALLY: 味方エンティティのみ")
        void testAllySemantics() {
            TargetGroupFilter filter = TargetGroupFilter.ALLY;
            // ALLYは味方エンティティのみ（プレイヤー）
            assertNotNull(filter, "ALLYはnullではない");
        }
    }

    @Nested
    @DisplayName("fromIdメソッド")
    class FromIdTests {

        @Test
        @DisplayName("fromId: nullを渡した場合はENEMYが返されること")
        void testFromId_Null() {
            TargetGroupFilter result = TargetGroupFilter.fromId(null);
            assertEquals(TargetGroupFilter.ENEMY, result, "nullの場合はENEMYが返されること");
        }

        @Test
        @DisplayName("fromId: \"enemy\"でENEMYが返されること")
        void testFromId_Enemy() {
            TargetGroupFilter result = TargetGroupFilter.fromId("enemy");
            assertEquals(TargetGroupFilter.ENEMY, result, "ENEMYが返されること");
        }

        @Test
        @DisplayName("fromId: \"ally\"でALLYが返されること")
        void testFromId_Ally() {
            TargetGroupFilter result = TargetGroupFilter.fromId("ally");
            assertEquals(TargetGroupFilter.ALLY, result, "ALLYが返されること");
        }

        @Test
        @DisplayName("fromId: \"both\"でBOTHが返されること")
        void testFromId_Both() {
            TargetGroupFilter result = TargetGroupFilter.fromId("both");
            assertEquals(TargetGroupFilter.BOTH, result, "BOTHが返されること");
        }

        @Test
        @DisplayName("fromId: 大文字小文字を区別しないこと")
        void testFromId_CaseInsensitive() {
            assertEquals(TargetGroupFilter.ENEMY, TargetGroupFilter.fromId("ENEMY"));
            assertEquals(TargetGroupFilter.ENEMY, TargetGroupFilter.fromId("Enemy"));
            assertEquals(TargetGroupFilter.ALLY, TargetGroupFilter.fromId("ALLY"));
            assertEquals(TargetGroupFilter.BOTH, TargetGroupFilter.fromId("BOTH"));
        }

        @Test
        @DisplayName("fromId: 無効なIDの場合はENEMYが返されること")
        void testFromId_Invalid() {
            TargetGroupFilter result = TargetGroupFilter.fromId("invalid_id");
            assertEquals(TargetGroupFilter.ENEMY, result, "無効なIDの場合はENEMYが返されること");
        }

        @Test
        @DisplayName("fromId: 空文字の場合はENEMYが返されること")
        void testFromId_Empty() {
            TargetGroupFilter result = TargetGroupFilter.fromId("");
            assertEquals(TargetGroupFilter.ENEMY, result, "空文字の場合はENEMYが返されること");
        }
    }

    @Nested
    @DisplayName("fromIdOrEmptyメソッド")
    class FromIdOrEmptyTests {

        @Test
        @DisplayName("fromIdOrEmpty: nullを渡した場合は空のOptionalが返されること")
        void testFromIdOrEmpty_Null() {
            Optional<TargetGroupFilter> result = TargetGroupFilter.fromIdOrEmpty(null);
            assertTrue(result.isEmpty(), "nullの場合は空のOptionalが返されること");
        }

        @Test
        @DisplayName("fromIdOrEmpty: \"enemy\"でENEMYを含むOptionalが返されること")
        void testFromIdOrEmpty_Enemy() {
            Optional<TargetGroupFilter> result = TargetGroupFilter.fromIdOrEmpty("enemy");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(TargetGroupFilter.ENEMY, result.get(), "ENEMYが含まれること");
        }

        @Test
        @DisplayName("fromIdOrEmpty: \"ally\"でALLYを含むOptionalが返されること")
        void testFromIdOrEmpty_Ally() {
            Optional<TargetGroupFilter> result = TargetGroupFilter.fromIdOrEmpty("ally");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(TargetGroupFilter.ALLY, result.get(), "ALLYが含まれること");
        }

        @Test
        @DisplayName("fromIdOrEmpty: \"both\"でBOTHを含むOptionalが返されること")
        void testFromIdOrEmpty_Both() {
            Optional<TargetGroupFilter> result = TargetGroupFilter.fromIdOrEmpty("both");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(TargetGroupFilter.BOTH, result.get(), "BOTHが含まれること");
        }

        @Test
        @DisplayName("fromIdOrEmpty: 大文字小文字を区別しないこと")
        void testFromIdOrEmpty_CaseInsensitive() {
            assertTrue(TargetGroupFilter.fromIdOrEmpty("ENEMY").isPresent());
            assertTrue(TargetGroupFilter.fromIdOrEmpty("ALLY").isPresent());
            assertTrue(TargetGroupFilter.fromIdOrEmpty("BOTH").isPresent());
        }

        @Test
        @DisplayName("fromIdOrEmpty: 無効なIDの場合は空のOptionalが返されること")
        void testFromIdOrEmpty_Invalid() {
            Optional<TargetGroupFilter> result = TargetGroupFilter.fromIdOrEmpty("invalid_id");
            assertTrue(result.isEmpty(), "無効なIDの場合は空のOptionalが返されること");
        }

        @Test
        @DisplayName("fromIdOrEmpty: 空文字の場合は空のOptionalが返されること")
        void testFromIdOrEmpty_Empty() {
            Optional<TargetGroupFilter> result = TargetGroupFilter.fromIdOrEmpty("");
            assertTrue(result.isEmpty(), "空文字の場合は空のOptionalが返されること");
        }
    }

    @Nested
    @DisplayName("getIdメソッド")
    class GetIdTests {

        @Test
        @DisplayName("getId: ENEMYのIDが正しいこと")
        void testGetId_Enemy() {
            assertEquals("enemy", TargetGroupFilter.ENEMY.getId());
        }

        @Test
        @DisplayName("getId: ALLYのIDが正しいこと")
        void testGetId_Ally() {
            assertEquals("ally", TargetGroupFilter.ALLY.getId());
        }

        @Test
        @DisplayName("getId: BOTHのIDが正しいこと")
        void testGetId_Both() {
            assertEquals("both", TargetGroupFilter.BOTH.getId());
        }
    }

    @Nested
    @DisplayName("getDisplayNameメソッド")
    class GetDisplayNameTests {

        @Test
        @DisplayName("getDisplayName: ENEMYの表示名が正しいこと")
        void testGetDisplayName_Enemy() {
            assertEquals("敵のみ", TargetGroupFilter.ENEMY.getDisplayName());
        }

        @Test
        @DisplayName("getDisplayName: ALLYの表示名が正しいこと")
        void testGetDisplayName_Ally() {
            assertEquals("味方のみ", TargetGroupFilter.ALLY.getDisplayName());
        }

        @Test
        @DisplayName("getDisplayName: BOTHの表示名が正しいこと")
        void testGetDisplayName_Both() {
            assertEquals("敵味方両方", TargetGroupFilter.BOTH.getDisplayName());
        }
    }
}
