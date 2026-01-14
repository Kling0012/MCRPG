package com.example.rpgplugin.skill.target;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TargetTypeのテストクラス
 */
@DisplayName("TargetType テスト")
class TargetTypeTest {

    @Nested
    @DisplayName("列挙値の存在確認")
    class EnumValuesTests {

        @Test
        @DisplayName("SELF: 列挙値が存在すること")
        void testSelfExists() {
            TargetType type = TargetType.SELF;
            assertEquals("SELF", type.name(), "SELFが存在すること");
        }

        @Test
        @DisplayName("SELF_PLUS_ONE: 列挙値が存在すること")
        void testSelfPlusOneExists() {
            TargetType type = TargetType.SELF_PLUS_ONE;
            assertEquals("SELF_PLUS_ONE", type.name(), "SELF_PLUS_ONEが存在すること");
        }

        @Test
        @DisplayName("NEAREST_HOSTILE: 列挙値が存在すること")
        void testNearestHostileExists() {
            TargetType type = TargetType.NEAREST_HOSTILE;
            assertEquals("NEAREST_HOSTILE", type.name(), "NEAREST_HOSTILEが存在すること");
        }

        @Test
        @DisplayName("NEAREST_PLAYER: 列挙値が存在すること")
        void testNearestPlayerExists() {
            TargetType type = TargetType.NEAREST_PLAYER;
            assertEquals("NEAREST_PLAYER", type.name(), "NEAREST_PLAYERが存在すること");
        }

        @Test
        @DisplayName("NEAREST_ENTITY: 列挙値が存在すること")
        void testNearestEntityExists() {
            TargetType type = TargetType.NEAREST_ENTITY;
            assertEquals("NEAREST_ENTITY", type.name(), "NEAREST_ENTITYが存在すること");
        }

        @Test
        @DisplayName("AREA_SELF: 列挙値が存在すること")
        void testAreaSelfExists() {
            TargetType type = TargetType.AREA_SELF;
            assertEquals("AREA_SELF", type.name(), "AREA_SELFが存在すること");
        }

        @Test
        @DisplayName("AREA_OTHERS: 列挙値が存在すること")
        void testAreaOthersExists() {
            TargetType type = TargetType.AREA_OTHERS;
            assertEquals("AREA_OTHERS", type.name(), "AREA_OTHERSが存在すること");
        }

        @Test
        @DisplayName("EXTERNAL: 列挙値が存在すること")
        void testExternalExists() {
            TargetType type = TargetType.EXTERNAL;
            assertEquals("EXTERNAL", type.name(), "EXTERNALが存在すること");
        }

        @Test
        @DisplayName("LINE: 列挙値が存在すること")
        void testLineExists() {
            TargetType type = TargetType.LINE;
            assertEquals("LINE", type.name(), "LINEが存在すること");
        }

        @Test
        @DisplayName("CONE: 列挙値が存在すること")
        void testConeExists() {
            TargetType type = TargetType.CONE;
            assertEquals("CONE", type.name(), "CONEが存在すること");
        }

        @Test
        @DisplayName("LOOKING: 列挙値が存在すること")
        void testLookingExists() {
            TargetType type = TargetType.LOOKING;
            assertEquals("LOOKING", type.name(), "LOOKINGが存在すること");
        }

        @Test
        @DisplayName("SPHERE: 列挙値が存在すること")
        void testSphereExists() {
            TargetType type = TargetType.SPHERE;
            assertEquals("SPHERE", type.name(), "SPHEREが存在すること");
        }

        @Test
        @DisplayName("values: 12個の列挙値が存在すること")
        void testValuesCount() {
            TargetType[] values = TargetType.values();
            assertEquals(12, values.length, "12個の列挙値が存在すること");
        }

        @Test
        @DisplayName("valueOf: 有効な文字列から列挙値が取得できること")
        void testValueOf_Valid() {
            assertEquals(TargetType.SELF, TargetType.valueOf("SELF"));
            assertEquals(TargetType.LINE, TargetType.valueOf("LINE"));
            assertEquals(TargetType.CONE, TargetType.valueOf("CONE"));
        }

        @Test
        @DisplayName("valueOf: 無効な文字列では例外がスローされること")
        void testValueOf_Invalid() {
            assertThrows(IllegalArgumentException.class, () -> TargetType.valueOf("INVALID"),
                    "無効な文字列では例外がスローされること");
        }
    }

    @Nested
    @DisplayName("序数の確認")
    class OrdinalTests {

        @Test
        @DisplayName("全ての序数が一意であること")
        void testUniqueOrdinals() {
            TargetType[] values = TargetType.values();

            for (int i = 0; i < values.length; i++) {
                for (int j = i + 1; j < values.length; j++) {
                    assertNotEquals(values[i].ordinal(), values[j].ordinal(),
                            String.format("%sと%sの序数が異なること", values[i], values[j]));
                }
            }
        }

        @Test
        @DisplayName("序数が0から始まる連番であること")
        void testSequentialOrdinals() {
            TargetType[] values = TargetType.values();

            for (int i = 0; i < values.length; i++) {
                assertEquals(i, values[i].ordinal(),
                        String.format("%sの序数が%dであること", values[i], i));
            }
        }
    }

    @Nested
    @DisplayName("switch文での使用")
    class SwitchUsageTests {

        @Test
        @DisplayName("switch: 全ケースで正しく分岐されること")
        void testSwitchCoverage() {
            TargetType[] values = TargetType.values();
            int caseCount = 0;

            for (TargetType type : values) {
                String result = switch (type) {
                    case SELF -> { caseCount++; yield "self"; }
                    case SELF_PLUS_ONE -> { caseCount++; yield "self_plus_one"; }
                    case NEAREST_HOSTILE -> { caseCount++; yield "nearest_hostile"; }
                    case NEAREST_PLAYER -> { caseCount++; yield "nearest_player"; }
                    case NEAREST_ENTITY -> { caseCount++; yield "nearest_entity"; }
                    case AREA_SELF -> { caseCount++; yield "area_self"; }
                    case AREA_OTHERS -> { caseCount++; yield "area_others"; }
                    case EXTERNAL -> { caseCount++; yield "external"; }
                    case LINE -> { caseCount++; yield "line"; }
                    case CONE -> { caseCount++; yield "cone"; }
                    case LOOKING -> { caseCount++; yield "looking"; }
                    case SPHERE -> { caseCount++; yield "sphere"; }
                };
                assertNotNull(result, "全ケースで結果が返されること");
            }

            assertEquals(values.length, caseCount, "全ケースが実行されること");
        }
    }

    @Nested
    @DisplayName("セマンティック分類")
    class SemanticCategoryTests {

        @Test
        @DisplayName("単体ターゲットタイプ: SELF")
        void testSingleTarget_Self() {
            assertTrue(isSingleTarget(TargetType.SELF), "SELFは単体ターゲット");
        }

        @Test
        @DisplayName("単体ターゲットタイプ: SELF_PLUS_ONE")
        void testSingleTarget_SelfPlusOne() {
            assertTrue(isSingleTarget(TargetType.SELF_PLUS_ONE), "SELF_PLUS_ONEは単体ターゲット");
        }

        @Test
        @DisplayName("単体ターゲットタイプ: NEAREST_HOSTILE")
        void testSingleTarget_NearestHostile() {
            assertTrue(isSingleTarget(TargetType.NEAREST_HOSTILE), "NEAREST_HOSTILEは単体ターゲット");
        }

        @Test
        @DisplayName("単体ターゲットタイプ: NEAREST_PLAYER")
        void testSingleTarget_NearestPlayer() {
            assertTrue(isSingleTarget(TargetType.NEAREST_PLAYER), "NEAREST_PLAYERは単体ターゲット");
        }

        @Test
        @DisplayName("単体ターゲットタイプ: NEAREST_ENTITY")
        void testSingleTarget_NearestEntity() {
            assertTrue(isSingleTarget(TargetType.NEAREST_ENTITY), "NEAREST_ENTITYは単体ターゲット");
        }

        @Test
        @DisplayName("範囲ターゲットタイプ: AREA_SELF")
        void testAreaTarget_AreaSelf() {
            assertTrue(isAreaTarget(TargetType.AREA_SELF), "AREA_SELFは範囲ターゲット");
        }

        @Test
        @DisplayName("範囲ターゲットタイプ: AREA_OTHERS")
        void testAreaTarget_AreaOthers() {
            assertTrue(isAreaTarget(TargetType.AREA_OTHERS), "AREA_OTHERSは範囲ターゲット");
        }

        @Test
        @DisplayName("範囲ターゲットタイプ: LINE")
        void testAreaTarget_Line() {
            assertTrue(isAreaTarget(TargetType.LINE), "LINEは範囲ターゲット");
        }

        @Test
        @DisplayName("範囲ターゲットタイプ: CONE")
        void testAreaTarget_Cone() {
            assertTrue(isAreaTarget(TargetType.CONE), "CONEは範囲ターゲット");
        }

        @Test
        @DisplayName("範囲ターゲットタイプ: LOOKING")
        void testAreaTarget_Looking() {
            assertTrue(isAreaTarget(TargetType.LOOKING), "LOOKINGは範囲ターゲット");
        }

        @Test
        @DisplayName("範囲ターゲットタイプ: SPHERE")
        void testAreaTarget_Sphere() {
            assertTrue(isAreaTarget(TargetType.SPHERE), "SPHEREは範囲ターゲット");
        }

        @Test
        @DisplayName("特殊ターゲットタイプ: EXTERNAL")
        void testSpecialTarget_External() {
            TargetType type = TargetType.EXTERNAL;
            assertNotNull(type, "EXTERNALは特殊ターゲットタイプ");
        }

        // ヘルパーメソッド
        private boolean isSingleTarget(TargetType type) {
            return switch (type) {
                case SELF, SELF_PLUS_ONE, NEAREST_HOSTILE, NEAREST_PLAYER, NEAREST_ENTITY -> true;
                default -> false;
            };
        }

        private boolean isAreaTarget(TargetType type) {
            return switch (type) {
                case AREA_SELF, AREA_OTHERS, LINE, CONE, LOOKING, SPHERE -> true;
                default -> false;
            };
        }
    }

    @Nested
    @DisplayName("fromIdメソッド")
    class FromIdTests {

        @Test
        @DisplayName("fromId: nullを渡した場合は空のOptionalが返されること")
        void testFromId_Null() {
            Optional<TargetType> result = TargetType.fromId(null);
            assertTrue(result.isEmpty(), "nullの場合は空のOptionalが返されること");
        }

        @Test
        @DisplayName("fromId: \"self\"でSELFが返されること")
        void testFromId_Self() {
            Optional<TargetType> result = TargetType.fromId("self");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(TargetType.SELF, result.get(), "SELFが返されること");
        }

        @Test
        @DisplayName("fromId: \"self_plus_one\"でSELF_PLUS_ONEが返されること")
        void testFromId_SelfPlusOne() {
            Optional<TargetType> result = TargetType.fromId("self_plus_one");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(TargetType.SELF_PLUS_ONE, result.get(), "SELF_PLUS_ONEが返されること");
        }

        @Test
        @DisplayName("fromId: \"nearest_hostile\"でNEAREST_HOSTILEが返されること")
        void testFromId_NearestHostile() {
            Optional<TargetType> result = TargetType.fromId("nearest_hostile");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(TargetType.NEAREST_HOSTILE, result.get(), "NEAREST_HOSTILEが返されること");
        }

        @Test
        @DisplayName("fromId: \"nearest_player\"でNEAREST_PLAYERが返されること")
        void testFromId_NearestPlayer() {
            Optional<TargetType> result = TargetType.fromId("nearest_player");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(TargetType.NEAREST_PLAYER, result.get(), "NEAREST_PLAYERが返されること");
        }

        @Test
        @DisplayName("fromId: \"nearest_entity\"でNEAREST_ENTITYが返されること")
        void testFromId_NearestEntity() {
            Optional<TargetType> result = TargetType.fromId("nearest_entity");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(TargetType.NEAREST_ENTITY, result.get(), "NEAREST_ENTITYが返されること");
        }

        @Test
        @DisplayName("fromId: \"area_self\"でAREA_SELFが返されること")
        void testFromId_AreaSelf() {
            Optional<TargetType> result = TargetType.fromId("area_self");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(TargetType.AREA_SELF, result.get(), "AREA_SELFが返されること");
        }

        @Test
        @DisplayName("fromId: \"area_others\"でAREA_OTHERSが返されること")
        void testFromId_AreaOthers() {
            Optional<TargetType> result = TargetType.fromId("area_others");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(TargetType.AREA_OTHERS, result.get(), "AREA_OTHERSが返されること");
        }

        @Test
        @DisplayName("fromId: \"external\"でEXTERNALが返されること")
        void testFromId_External() {
            Optional<TargetType> result = TargetType.fromId("external");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(TargetType.EXTERNAL, result.get(), "EXTERNALが返されること");
        }

        @Test
        @DisplayName("fromId: \"line\"でLINEが返されること")
        void testFromId_Line() {
            Optional<TargetType> result = TargetType.fromId("line");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(TargetType.LINE, result.get(), "LINEが返されること");
        }

        @Test
        @DisplayName("fromId: \"cone\"でCONEが返されること")
        void testFromId_Cone() {
            Optional<TargetType> result = TargetType.fromId("cone");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(TargetType.CONE, result.get(), "CONEが返されること");
        }

        @Test
        @DisplayName("fromId: \"looking\"でLOOKINGが返されること")
        void testFromId_Looking() {
            Optional<TargetType> result = TargetType.fromId("looking");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(TargetType.LOOKING, result.get(), "LOOKINGが返されること");
        }

        @Test
        @DisplayName("fromId: \"sphere\"でSPHEREが返されること")
        void testFromId_Sphere() {
            Optional<TargetType> result = TargetType.fromId("sphere");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(TargetType.SPHERE, result.get(), "SPHEREが返されること");
        }

        @Test
        @DisplayName("fromId: 大文字小文字を区別しないこと")
        void testFromId_CaseInsensitive() {
            assertTrue(TargetType.fromId("SELF").isPresent());
            assertTrue(TargetType.fromId("LINE").isPresent());
            assertTrue(TargetType.fromId("CONE").isPresent());
        }

        @Test
        @DisplayName("fromId: 無効なIDの場合は空のOptionalが返されること")
        void testFromId_Invalid() {
            Optional<TargetType> result = TargetType.fromId("invalid_id");
            assertTrue(result.isEmpty(), "無効なIDの場合は空のOptionalが返されること");
        }

        @Test
        @DisplayName("fromId: 空文字の場合は空のOptionalが返されること")
        void testFromId_Empty() {
            Optional<TargetType> result = TargetType.fromId("");
            assertTrue(result.isEmpty(), "空文字の場合は空のOptionalが返されること");
        }
    }

    @Nested
    @DisplayName("fromIdOrNullメソッド（レガシー）")
    class FromIdOrNullTests {

        @Test
        @DisplayName("fromIdOrNull: nullを渡した場合はnullが返されること")
        void testFromIdOrNull_Null() {
            TargetType result = TargetType.fromIdOrNull(null);
            assertNull(result, "nullの場合はnullが返されること");
        }

        @Test
        @DisplayName("fromIdOrNull: 有効なIDで対応するTargetTypeが返されること")
        void testFromIdOrNull_Valid() {
            assertEquals(TargetType.SELF, TargetType.fromIdOrNull("self"));
            assertEquals(TargetType.LINE, TargetType.fromIdOrNull("line"));
            assertEquals(TargetType.CONE, TargetType.fromIdOrNull("cone"));
        }

        @Test
        @DisplayName("fromIdOrNull: 無効なIDの場合はnullが返されること")
        void testFromIdOrNull_Invalid() {
            TargetType result = TargetType.fromIdOrNull("invalid_id");
            assertNull(result, "無効なIDの場合はnullが返されること");
        }
    }

    @Nested
    @DisplayName("getIdメソッド")
    class GetIdTests {

        @Test
        @DisplayName("getId: 全てのターゲットタイプのIDが正しいこと")
        void testGetId_All() {
            assertEquals("self", TargetType.SELF.getId());
            assertEquals("self_plus_one", TargetType.SELF_PLUS_ONE.getId());
            assertEquals("nearest_hostile", TargetType.NEAREST_HOSTILE.getId());
            assertEquals("nearest_player", TargetType.NEAREST_PLAYER.getId());
            assertEquals("nearest_entity", TargetType.NEAREST_ENTITY.getId());
            assertEquals("area_self", TargetType.AREA_SELF.getId());
            assertEquals("area_others", TargetType.AREA_OTHERS.getId());
            assertEquals("external", TargetType.EXTERNAL.getId());
            assertEquals("line", TargetType.LINE.getId());
            assertEquals("cone", TargetType.CONE.getId());
            assertEquals("looking", TargetType.LOOKING.getId());
            assertEquals("sphere", TargetType.SPHERE.getId());
        }
    }

    @Nested
    @DisplayName("getDisplayNameメソッド")
    class GetDisplayNameTests {

        @Test
        @DisplayName("getDisplayName: 全てのターゲットタイプの表示名が正しいこと")
        void testGetDisplayName_All() {
            assertEquals("自分自身", TargetType.SELF.getDisplayName());
            assertEquals("自分と他人一人", TargetType.SELF_PLUS_ONE.getDisplayName());
            assertEquals("最も近いMob", TargetType.NEAREST_HOSTILE.getDisplayName());
            assertEquals("最も近いプレイヤー", TargetType.NEAREST_PLAYER.getDisplayName());
            assertEquals("最も近いエンティティ", TargetType.NEAREST_ENTITY.getDisplayName());
            assertEquals("自分を含む範囲", TargetType.AREA_SELF.getDisplayName());
            assertEquals("自分を含まない範囲", TargetType.AREA_OTHERS.getDisplayName());
            assertEquals("外部指定", TargetType.EXTERNAL.getDisplayName());
            assertEquals("直線", TargetType.LINE.getDisplayName());
            assertEquals("扇状", TargetType.CONE.getDisplayName());
            assertEquals("視線上", TargetType.LOOKING.getDisplayName());
            assertEquals("球形範囲", TargetType.SPHERE.getDisplayName());
        }
    }

    @Nested
    @DisplayName("isAreaTypeメソッド")
    class IsAreaTypeTests {

        @Test
        @DisplayName("isAreaType: 範囲系タイプはtrue")
        void testIsAreaType_AreaTypes() {
            assertTrue(TargetType.AREA_SELF.isAreaType(), "AREA_SELFは範囲系");
            assertTrue(TargetType.AREA_OTHERS.isAreaType(), "AREA_OTHERSは範囲系");
            assertTrue(TargetType.CONE.isAreaType(), "CONEは範囲系");
            assertTrue(TargetType.SPHERE.isAreaType(), "SPHEREは範囲系");
        }

        @Test
        @DisplayName("isAreaType: 単体系タイプはfalse")
        void testIsAreaType_SingleTypes() {
            assertFalse(TargetType.SELF.isAreaType(), "SELFは範囲系ではない");
            assertFalse(TargetType.SELF_PLUS_ONE.isAreaType(), "SELF_PLUS_ONEは範囲系ではない");
            assertFalse(TargetType.NEAREST_HOSTILE.isAreaType(), "NEAREST_HOSTILEは範囲系ではない");
            assertFalse(TargetType.NEAREST_PLAYER.isAreaType(), "NEAREST_PLAYERは範囲系ではない");
            assertFalse(TargetType.NEAREST_ENTITY.isAreaType(), "NEAREST_ENTITYは範囲系ではない");
        }

        @Test
        @DisplayName("isAreaType: LINEはfalse（範囲系として分類されない）")
        void testIsAreaType_Line() {
            assertFalse(TargetType.LINE.isAreaType(), "LINEは範囲系ではない");
        }

        @Test
        @DisplayName("isAreaType: LOOKINGはfalse（範囲系として分類されない）")
        void testIsAreaType_Looking() {
            assertFalse(TargetType.LOOKING.isAreaType(), "LOOKINGは範囲系ではない");
        }

        @Test
        @DisplayName("isAreaType: EXTERNALはfalse（範囲系として分類されない）")
        void testIsAreaType_External() {
            assertFalse(TargetType.EXTERNAL.isAreaType(), "EXTERNALは範囲系ではない");
        }
    }

    @Nested
    @DisplayName("isSingleTypeメソッド")
    class IsSingleTypeTests {

        @Test
        @DisplayName("isSingleType: 単体ターゲットタイプはtrue")
        void testIsSingleType_SingleTypes() {
            assertTrue(TargetType.SELF.isSingleType(), "SELFは単体ターゲット");
            assertTrue(TargetType.SELF_PLUS_ONE.isSingleType(), "SELF_PLUS_ONEは単体ターゲット");
            assertTrue(TargetType.NEAREST_HOSTILE.isSingleType(), "NEAREST_HOSTILEは単体ターゲット");
            assertTrue(TargetType.NEAREST_PLAYER.isSingleType(), "NEAREST_PLAYERは単体ターゲット");
            assertTrue(TargetType.NEAREST_ENTITY.isSingleType(), "NEAREST_ENTITYは単体ターゲット");
        }

        @Test
        @DisplayName("isSingleType: 範囲ターゲットタイプはfalse")
        void testIsSingleType_AreaTypes() {
            assertFalse(TargetType.AREA_SELF.isSingleType(), "AREA_SELFは単体ターゲットではない");
            assertFalse(TargetType.AREA_OTHERS.isSingleType(), "AREA_OTHERSは単体ターゲットではない");
            assertFalse(TargetType.CONE.isSingleType(), "CONEは単体ターゲットではない");
            assertFalse(TargetType.SPHERE.isSingleType(), "SPHEREは単体ターゲットではない");
        }

        @Test
        @DisplayName("isSingleType: LINEはfalse")
        void testIsSingleType_Line() {
            assertFalse(TargetType.LINE.isSingleType(), "LINEは単体ターゲットではない");
        }

        @Test
        @DisplayName("isSingleType: LOOKINGはfalse")
        void testIsSingleType_Looking() {
            assertFalse(TargetType.LOOKING.isSingleType(), "LOOKINGは単体ターゲットではない");
        }

        @Test
        @DisplayName("isSingleType: EXTERNALはfalse")
        void testIsSingleType_External() {
            assertFalse(TargetType.EXTERNAL.isSingleType(), "EXTERNALは単体ターゲットではない");
        }
    }

    @Nested
    @DisplayName("isMultiTargetTypeメソッド")
    class IsMultiTargetTypeTests {

        @Test
        @DisplayName("isMultiTargetType: 複数ターゲットタイプはtrue")
        void testIsMultiTargetType_MultiTypes() {
            assertTrue(TargetType.SELF_PLUS_ONE.isMultiTargetType(), "SELF_PLUS_ONEは複数ターゲット");
            assertTrue(TargetType.AREA_SELF.isMultiTargetType(), "AREA_SELFは複数ターゲット");
            assertTrue(TargetType.AREA_OTHERS.isMultiTargetType(), "AREA_OTHERSは複数ターゲット");
        }

        @Test
        @DisplayName("isMultiTargetType: 単体ターゲットタイプはfalse")
        void testIsMultiTargetType_SingleTypes() {
            assertFalse(TargetType.SELF.isMultiTargetType(), "SELFは複数ターゲットではない");
            assertFalse(TargetType.NEAREST_HOSTILE.isMultiTargetType(), "NEAREST_HOSTILEは複数ターゲットではない");
            assertFalse(TargetType.NEAREST_PLAYER.isMultiTargetType(), "NEAREST_PLAYERは複数ターゲットではない");
            assertFalse(TargetType.NEAREST_ENTITY.isMultiTargetType(), "NEAREST_ENTITYは複数ターゲットではない");
        }

        @Test
        @DisplayName("isMultiTargetType: 形状タイプはfalse")
        void testIsMultiTargetType_ShapeTypes() {
            assertFalse(TargetType.LINE.isMultiTargetType(), "LINEは複数ターゲットではない");
            assertFalse(TargetType.CONE.isMultiTargetType(), "CONEは複数ターゲットではない");
            assertFalse(TargetType.SPHERE.isMultiTargetType(), "SPHEREは複数ターゲットではない");
            assertFalse(TargetType.LOOKING.isMultiTargetType(), "LOOKINGは複数ターゲットではない");
        }

        @Test
        @DisplayName("isMultiTargetType: EXTERNALはfalse")
        void testIsMultiTargetType_External() {
            assertFalse(TargetType.EXTERNAL.isMultiTargetType(), "EXTERNALは複数ターゲットではない");
        }
    }
}
