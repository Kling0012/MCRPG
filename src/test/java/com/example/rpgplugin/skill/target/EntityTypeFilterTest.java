package com.example.rpgplugin.skill.target;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EntityTypeFilterのテストクラス
 */
@DisplayName("EntityTypeFilter テスト")
class EntityTypeFilterTest {

    @Nested
    @DisplayName("列挙値の存在確認")
    class EnumValuesTests {

        @Test
        @DisplayName("PLAYER_ONLY: 列挙値が存在すること")
        void testPlayerOnlyExists() {
            EntityTypeFilter filter = EntityTypeFilter.PLAYER_ONLY;
            assertEquals("PLAYER_ONLY", filter.name(), "PLAYER_ONLYが存在すること");
        }

        @Test
        @DisplayName("MOB_ONLY: 列挙値が存在すること")
        void testMobOnlyExists() {
            EntityTypeFilter filter = EntityTypeFilter.MOB_ONLY;
            assertEquals("MOB_ONLY", filter.name(), "MOB_ONLYが存在すること");
        }

        @Test
        @DisplayName("ALL: 列挙値が存在すること")
        void testAllExists() {
            EntityTypeFilter filter = EntityTypeFilter.ALL;
            assertEquals("ALL", filter.name(), "ALLが存在すること");
        }

        @Test
        @DisplayName("values: 3つの列挙値が存在すること")
        void testValuesCount() {
            EntityTypeFilter[] values = EntityTypeFilter.values();
            assertEquals(3, values.length, "3つの列挙値が存在すること");
        }

        @Test
        @DisplayName("valueOf: 有効な文字列から列挙値が取得できること")
        void testValueOf_Valid() {
            assertEquals(EntityTypeFilter.PLAYER_ONLY, EntityTypeFilter.valueOf("PLAYER_ONLY"));
            assertEquals(EntityTypeFilter.MOB_ONLY, EntityTypeFilter.valueOf("MOB_ONLY"));
            assertEquals(EntityTypeFilter.ALL, EntityTypeFilter.valueOf("ALL"));
        }

        @Test
        @DisplayName("valueOf: 無効な文字列では例外がスローされること")
        void testValueOf_Invalid() {
            assertThrows(IllegalArgumentException.class, () -> EntityTypeFilter.valueOf("INVALID"),
                    "無効な文字列では例外がスローされること");
        }
    }

    @Nested
    @DisplayName("序数の確認")
    class OrdinalTests {

        @Test
        @DisplayName("序数が一意であること")
        void testUniqueOrdinals() {
            int ordinal1 = EntityTypeFilter.PLAYER_ONLY.ordinal();
            int ordinal2 = EntityTypeFilter.MOB_ONLY.ordinal();
            int ordinal3 = EntityTypeFilter.ALL.ordinal();

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
            EntityTypeFilter[] values = EntityTypeFilter.values();

            for (EntityTypeFilter filter : values) {
                String result = switch (filter) {
                    case PLAYER_ONLY -> "player";
                    case MOB_ONLY -> "mob";
                    case ALL -> "all";
                };
                assertNotNull(result, "全ケースで結果が返されること");
            }
        }
    }

    @Nested
    @DisplayName("fromIdメソッド")
    class FromIdTests {

        @Test
        @DisplayName("fromId: nullを渡した場合はALLが返されること")
        void testFromId_Null() {
            EntityTypeFilter result = EntityTypeFilter.fromId(null);
            assertEquals(EntityTypeFilter.ALL, result, "nullの場合はALLが返されること");
        }

        @Test
        @DisplayName("fromId: \"player_only\"でPLAYER_ONLYが返されること")
        void testFromId_PlayerOnly() {
            EntityTypeFilter result = EntityTypeFilter.fromId("player_only");
            assertEquals(EntityTypeFilter.PLAYER_ONLY, result, "PLAYER_ONLYが返されること");
        }

        @Test
        @DisplayName("fromId: \"mob_only\"でMOB_ONLYが返されること")
        void testFromId_MobOnly() {
            EntityTypeFilter result = EntityTypeFilter.fromId("mob_only");
            assertEquals(EntityTypeFilter.MOB_ONLY, result, "MOB_ONLYが返されること");
        }

        @Test
        @DisplayName("fromId: \"all\"でALLが返されること")
        void testFromId_All() {
            EntityTypeFilter result = EntityTypeFilter.fromId("all");
            assertEquals(EntityTypeFilter.ALL, result, "ALLが返されること");
        }

        @Test
        @DisplayName("fromId: 大文字小文字を区別しないこと")
        void testFromId_CaseInsensitive() {
            assertEquals(EntityTypeFilter.PLAYER_ONLY, EntityTypeFilter.fromId("PLAYER_ONLY"));
            assertEquals(EntityTypeFilter.PLAYER_ONLY, EntityTypeFilter.fromId("Player_Only"));
            assertEquals(EntityTypeFilter.MOB_ONLY, EntityTypeFilter.fromId("MOB_ONLY"));
            assertEquals(EntityTypeFilter.ALL, EntityTypeFilter.fromId("ALL"));
        }

        @Test
        @DisplayName("fromId: 無効なIDの場合はALLが返されること")
        void testFromId_Invalid() {
            EntityTypeFilter result = EntityTypeFilter.fromId("invalid_id");
            assertEquals(EntityTypeFilter.ALL, result, "無効なIDの場合はALLが返されること");
        }

        @Test
        @DisplayName("fromId: 空文字の場合はALLが返されること")
        void testFromId_Empty() {
            EntityTypeFilter result = EntityTypeFilter.fromId("");
            assertEquals(EntityTypeFilter.ALL, result, "空文字の場合はALLが返されること");
        }
    }

    @Nested
    @DisplayName("getIdメソッド")
    class GetIdTests {

        @Test
        @DisplayName("getId: PLAYER_ONLYのIDが正しいこと")
        void testGetId_PlayerOnly() {
            assertEquals("player_only", EntityTypeFilter.PLAYER_ONLY.getId());
        }

        @Test
        @DisplayName("getId: MOB_ONLYのIDが正しいこと")
        void testGetId_MobOnly() {
            assertEquals("mob_only", EntityTypeFilter.MOB_ONLY.getId());
        }

        @Test
        @DisplayName("getId: ALLのIDが正しいこと")
        void testGetId_All() {
            assertEquals("all", EntityTypeFilter.ALL.getId());
        }
    }

    @Nested
    @DisplayName("getDisplayNameメソッド")
    class GetDisplayNameTests {

        @Test
        @DisplayName("getDisplayName: PLAYER_ONLYの表示名が正しいこと")
        void testGetDisplayName_PlayerOnly() {
            assertEquals("プレイヤーのみ", EntityTypeFilter.PLAYER_ONLY.getDisplayName());
        }

        @Test
        @DisplayName("getDisplayName: MOB_ONLYの表示名が正しいこと")
        void testGetDisplayName_MobOnly() {
            assertEquals("Mobのみ", EntityTypeFilter.MOB_ONLY.getDisplayName());
        }

        @Test
        @DisplayName("getDisplayName: ALLの表示名が正しいこと")
        void testGetDisplayName_All() {
            assertEquals("全て", EntityTypeFilter.ALL.getDisplayName());
        }
    }
}
