package com.example.rpgplugin.skill.target;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AreaShapeのテストクラス
 */
@DisplayName("AreaShape テスト")
class AreaShapeTest {

    @Nested
    @DisplayName("列挙値の存在確認")
    class EnumValuesTests {

        @Test
        @DisplayName("SINGLE: 列挙値が存在すること")
        void testSingleExists() {
            AreaShape shape = AreaShape.SINGLE;
            assertEquals("SINGLE", shape.name(), "SINGLEが存在すること");
        }

        @Test
        @DisplayName("CONE: 列挙値が存在すること")
        void testConeExists() {
            AreaShape shape = AreaShape.CONE;
            assertEquals("CONE", shape.name(), "CONEが存在すること");
        }

        @Test
        @DisplayName("RECT: 列挙値が存在すること")
        void testRectExists() {
            AreaShape shape = AreaShape.RECT;
            assertEquals("RECT", shape.name(), "RECTが存在すること");
        }

        @Test
        @DisplayName("CIRCLE: 列挙値が存在すること")
        void testCircleExists() {
            AreaShape shape = AreaShape.CIRCLE;
            assertEquals("CIRCLE", shape.name(), "CIRCLEが存在すること");
        }

        @Test
        @DisplayName("SPHERE: 列挙値が存在すること")
        void testSphereExists() {
            AreaShape shape = AreaShape.SPHERE;
            assertEquals("SPHERE", shape.name(), "SPHEREが存在すること");
        }

        @Test
        @DisplayName("values: 5個の列挙値が存在すること")
        void testValuesCount() {
            AreaShape[] values = AreaShape.values();
            assertEquals(5, values.length, "5個の列挙値が存在すること");
        }

        @Test
        @DisplayName("valueOf: 有効な文字列から列挙値が取得できること")
        void testValueOf_Valid() {
            assertEquals(AreaShape.SINGLE, AreaShape.valueOf("SINGLE"));
            assertEquals(AreaShape.CONE, AreaShape.valueOf("CONE"));
            assertEquals(AreaShape.RECT, AreaShape.valueOf("RECT"));
            assertEquals(AreaShape.CIRCLE, AreaShape.valueOf("CIRCLE"));
            assertEquals(AreaShape.SPHERE, AreaShape.valueOf("SPHERE"));
        }

        @Test
        @DisplayName("valueOf: 無効な文字列では例外がスローされること")
        void testValueOf_Invalid() {
            assertThrows(IllegalArgumentException.class, () -> AreaShape.valueOf("INVALID"),
                    "無効な文字列では例外がスローされること");
        }
    }

    @Nested
    @DisplayName("序数の確認")
    class OrdinalTests {

        @Test
        @DisplayName("全ての序数が一意であること")
        void testUniqueOrdinals() {
            AreaShape[] values = AreaShape.values();

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
            AreaShape[] values = AreaShape.values();

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
            AreaShape[] values = AreaShape.values();
            int caseCount = 0;

            for (AreaShape shape : values) {
                String result = switch (shape) {
                    case SINGLE -> { caseCount++; yield "single"; }
                    case CONE -> { caseCount++; yield "cone"; }
                    case RECT -> { caseCount++; yield "rect"; }
                    case CIRCLE -> { caseCount++; yield "circle"; }
                    case SPHERE -> { caseCount++; yield "sphere"; }
                };
                assertNotNull(result, "全ケースで結果が返されること");
            }

            assertEquals(values.length, caseCount, "全ケースが実行されること");
        }
    }

    @Nested
    @DisplayName("幾何学的形状分類")
    class GeometricCategoryTests {

        @Test
        @DisplayName("点形状: SINGLE")
        void testPointShape() {
            AreaShape shape = AreaShape.SINGLE;
            assertTrue(isPointShape(shape), "SINGLEは点形状");
            assertFalse(is2DShape(shape), "SINGLEは2D形状ではない");
            assertFalse(is3DShape(shape), "SINGLEは3D形状ではない");
        }

        @Test
        @DisplayName("2D形状: CONE, RECT, CIRCLE")
        void test2DShapes() {
            assertTrue(is2DShape(AreaShape.CONE), "CONEは2D形状");
            assertTrue(is2DShape(AreaShape.RECT), "RECTは2D形状");
            assertTrue(is2DShape(AreaShape.CIRCLE), "CIRCLEは2D形状");
        }

        @Test
        @DisplayName("3D形状: SPHERE")
        void test3DShapes() {
            assertTrue(is3DShape(AreaShape.SPHERE), "SPHEREは3D形状");
        }

        // ヘルパーメソッド
        private boolean isPointShape(AreaShape shape) {
            return shape == AreaShape.SINGLE;
        }

        private boolean is2DShape(AreaShape shape) {
            return switch (shape) {
                case CONE, RECT, CIRCLE -> true;
                default -> false;
            };
        }

        private boolean is3DShape(AreaShape shape) {
            return shape == AreaShape.SPHERE;
        }
    }

    @Nested
    @DisplayName("対応設定クラス")
    class ConfigClassMappingTests {

        @Test
        @DisplayName("SINGLE: SingleTargetConfigに対応")
        void testSingleMapping() {
            // SINGLEは単体ターゲットに使用
            AreaShape shape = AreaShape.SINGLE;
            assertEquals("SINGLE", shape.name());
        }

        @Test
        @DisplayName("CONE: ConeConfigに対応")
        void testConeMapping() {
            // CONEは扇状範囲設定に使用
            AreaShape shape = AreaShape.CONE;
            assertEquals("CONE", shape.name());
        }

        @Test
        @DisplayName("RECT: RectConfigに対応")
        void testRectMapping() {
            // RECTは四角形範囲設定に使用
            AreaShape shape = AreaShape.RECT;
            assertEquals("RECT", shape.name());
        }

        @Test
        @DisplayName("CIRCLE: CircleConfigに対応")
        void testCircleMapping() {
            // CIRCLEは円形範囲設定に使用
            AreaShape shape = AreaShape.CIRCLE;
            assertEquals("CIRCLE", shape.name());
        }

        @Test
        @DisplayName("SPHERE: 汎用range設定に対応")
        void testSphereMapping() {
            // SPHEREは球形範囲に使用
            AreaShape shape = AreaShape.SPHERE;
            assertEquals("SPHERE", shape.name());
        }
    }

    @Nested
    @DisplayName("fromIdメソッド")
    class FromIdTests {

        @Test
        @DisplayName("fromId: nullを渡した場合は空のOptionalが返されること")
        void testFromId_Null() {
            Optional<AreaShape> result = AreaShape.fromId(null);
            assertTrue(result.isEmpty(), "nullの場合は空のOptionalが返されること");
        }

        @Test
        @DisplayName("fromId: \"single\"でSINGLEが返されること")
        void testFromId_Single() {
            Optional<AreaShape> result = AreaShape.fromId("single");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(AreaShape.SINGLE, result.get(), "SINGLEが返されること");
        }

        @Test
        @DisplayName("fromId: \"cone\"でCONEが返されること")
        void testFromId_Cone() {
            Optional<AreaShape> result = AreaShape.fromId("cone");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(AreaShape.CONE, result.get(), "CONEが返されること");
        }

        @Test
        @DisplayName("fromId: \"rect\"でRECTが返されること")
        void testFromId_Rect() {
            Optional<AreaShape> result = AreaShape.fromId("rect");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(AreaShape.RECT, result.get(), "RECTが返されること");
        }

        @Test
        @DisplayName("fromId: \"circle\"でCIRCLEが返されること")
        void testFromId_Circle() {
            Optional<AreaShape> result = AreaShape.fromId("circle");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(AreaShape.CIRCLE, result.get(), "CIRCLEが返されること");
        }

        @Test
        @DisplayName("fromId: \"sphere\"でSPHEREが返されること")
        void testFromId_Sphere() {
            Optional<AreaShape> result = AreaShape.fromId("sphere");
            assertTrue(result.isPresent(), "Optionalが存在すること");
            assertEquals(AreaShape.SPHERE, result.get(), "SPHEREが返されること");
        }

        @Test
        @DisplayName("fromId: 大文字小文字を区別しないこと")
        void testFromId_CaseInsensitive() {
            assertTrue(AreaShape.fromId("SINGLE").isPresent());
            assertTrue(AreaShape.fromId("CONE").isPresent());
            assertTrue(AreaShape.fromId("RECT").isPresent());
            assertTrue(AreaShape.fromId("CIRCLE").isPresent());
            assertTrue(AreaShape.fromId("SPHERE").isPresent());
        }

        @Test
        @DisplayName("fromId: 無効なIDの場合は空のOptionalが返されること")
        void testFromId_Invalid() {
            Optional<AreaShape> result = AreaShape.fromId("invalid_id");
            assertTrue(result.isEmpty(), "無効なIDの場合は空のOptionalが返されること");
        }

        @Test
        @DisplayName("fromId: 空文字の場合は空のOptionalが返されること")
        void testFromId_Empty() {
            Optional<AreaShape> result = AreaShape.fromId("");
            assertTrue(result.isEmpty(), "空文字の場合は空のOptionalが返されること");
        }
    }

    @Nested
    @DisplayName("fromIdOrNullメソッド（レガシー）")
    class FromIdOrNullTests {

        @Test
        @DisplayName("fromIdOrNull: nullを渡した場合はnullが返されること")
        void testFromIdOrNull_Null() {
            AreaShape result = AreaShape.fromIdOrNull(null);
            assertNull(result, "nullの場合はnullが返されること");
        }

        @Test
        @DisplayName("fromIdOrNull: 有効なIDで対応するAreaShapeが返されること")
        void testFromIdOrNull_Valid() {
            assertEquals(AreaShape.SINGLE, AreaShape.fromIdOrNull("single"));
            assertEquals(AreaShape.CONE, AreaShape.fromIdOrNull("cone"));
            assertEquals(AreaShape.RECT, AreaShape.fromIdOrNull("rect"));
        }

        @Test
        @DisplayName("fromIdOrNull: 無効なIDの場合はnullが返されること")
        void testFromIdOrNull_Invalid() {
            AreaShape result = AreaShape.fromIdOrNull("invalid_id");
            assertNull(result, "無効なIDの場合はnullが返されること");
        }
    }

    @Nested
    @DisplayName("getIdメソッド")
    class GetIdTests {

        @Test
        @DisplayName("getId: SINGLEのIDが正しいこと")
        void testGetId_Single() {
            assertEquals("single", AreaShape.SINGLE.getId());
        }

        @Test
        @DisplayName("getId: CONEのIDが正しいこと")
        void testGetId_Cone() {
            assertEquals("cone", AreaShape.CONE.getId());
        }

        @Test
        @DisplayName("getId: RECTのIDが正しいこと")
        void testGetId_Rect() {
            assertEquals("rect", AreaShape.RECT.getId());
        }

        @Test
        @DisplayName("getId: CIRCLEのIDが正しいこと")
        void testGetId_Circle() {
            assertEquals("circle", AreaShape.CIRCLE.getId());
        }

        @Test
        @DisplayName("getId: SPHEREのIDが正しいこと")
        void testGetId_Sphere() {
            assertEquals("sphere", AreaShape.SPHERE.getId());
        }
    }

    @Nested
    @DisplayName("getDisplayNameメソッド")
    class GetDisplayNameTests {

        @Test
        @DisplayName("getDisplayName: SINGLEの表示名が正しいこと")
        void testGetDisplayName_Single() {
            assertEquals("単体", AreaShape.SINGLE.getDisplayName());
        }

        @Test
        @DisplayName("getDisplayName: CONEの表示名が正しいこと")
        void testGetDisplayName_Cone() {
            assertEquals("扇状", AreaShape.CONE.getDisplayName());
        }

        @Test
        @DisplayName("getDisplayName: RECTの表示名が正しいこと")
        void testGetDisplayName_Rect() {
            assertEquals("四角形", AreaShape.RECT.getDisplayName());
        }

        @Test
        @DisplayName("getDisplayName: CIRCLEの表示名が正しいこと")
        void testGetDisplayName_Circle() {
            assertEquals("円形", AreaShape.CIRCLE.getDisplayName());
        }

        @Test
        @DisplayName("getDisplayName: SPHEREの表示名が正しいこと")
        void testGetDisplayName_Sphere() {
            assertEquals("球形", AreaShape.SPHERE.getDisplayName());
        }
    }
}
