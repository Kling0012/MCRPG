package com.example.rpgplugin.skill;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SkillTypeのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SkillType: スキルタイプ列挙型")
class SkillTypeTest {

    // ========== ゲッターメソッド テスト ==========

    @Nested
    @DisplayName("Getter Methods: ゲッターメソッド")
    class GetterMethodTests {

        @Test
        @DisplayName("test: getIdはnormalを返す")
        void testGetId() {
            assertEquals("normal", SkillType.NORMAL.getId());
        }

        @Test
        @DisplayName("test: getDisplayNameはスキルを返す")
        void testGetDisplayName() {
            assertEquals("スキル", SkillType.NORMAL.getDisplayName());
        }

        @Test
        @DisplayName("test: getColorTagは<gold>を返す")
        void testGetColorTag() {
            assertEquals("<gold>", SkillType.NORMAL.getColorTag());
        }

        @Test
        @DisplayName("test: getDescriptionを返す")
        void testGetDescription() {
            assertEquals("通常スキル", SkillType.NORMAL.getDescription());
        }

        @Test
        @DisplayName("test: getColoredNameはカラータグ付きの表示名を返す")
        void testGetColoredName() {
            assertEquals("<gold>スキル", SkillType.NORMAL.getColoredName());
        }
    }

    // ========== fromId() テスト ==========

    @Nested
    @DisplayName("fromId: IDからSkillTypeを取得")
    class FromIdTests {

        @Test
        @DisplayName("test: nullを渡すとnullを返す")
        void testFromIdWithNull() {
            assertNull(SkillType.fromId(null));
        }

        @Test
        @DisplayName("test: normalでNORMALを返す")
        void testFromIdWithNormal() {
            assertSame(SkillType.NORMAL, SkillType.fromId("normal"));
        }

        @Test
        @DisplayName("test: NORMAL（大文字）でもNORMALを返す")
        void testFromIdWithNormalUpperCase() {
            assertSame(SkillType.NORMAL, SkillType.fromId("NORMAL"));
        }

        @Test
        @DisplayName("test: Normal（キャメルケース）でもNORMALを返す")
        void testFromIdWithNormalCamelCase() {
            assertSame(SkillType.NORMAL, SkillType.fromId("Normal"));
        }

        @Test
        @DisplayName("test: activeは互換性のためNORMALを返す")
        void testFromIdWithActive() {
            assertSame(SkillType.NORMAL, SkillType.fromId("active"));
        }

        @Test
        @DisplayName("test: ACTIVE（大文字）でもNORMALを返す")
        void testFromIdWithActiveUpperCase() {
            assertSame(SkillType.NORMAL, SkillType.fromId("ACTIVE"));
        }

        @Test
        @DisplayName("test: passiveは互換性のためNORMALを返す")
        void testFromIdWithPassive() {
            assertSame(SkillType.NORMAL, SkillType.fromId("passive"));
        }

        @Test
        @DisplayName("test: PASSIVE（大文字）でもNORMALを返す")
        void testFromIdWithPassiveUpperCase() {
            assertSame(SkillType.NORMAL, SkillType.fromId("PASSIVE"));
        }

        @Test
        @DisplayName("test: 存在しないIDはnullを返す")
        void testFromIdWithInvalidId() {
            assertNull(SkillType.fromId("invalid"));
            assertNull(SkillType.fromId(""));
        }
    }

    // ========== toString() テスト ==========

    @Nested
    @DisplayName("toString: 文字列表現")
    class ToStringTests {

        @Test
        @DisplayName("test: toStringはIDと表示名を返す")
        void testToString() {
            assertEquals("normal (スキル)", SkillType.NORMAL.toString());
        }
    }

    // ========== values() テスト ==========

    @Nested
    @DisplayName("values: 全値の取得")
    class ValuesTests {

        @Test
        @DisplayName("test: valuesはNORMALのみを含む")
        void testValues() {
            SkillType[] values = SkillType.values();
            assertEquals(1, values.length);
            assertSame(SkillType.NORMAL, values[0]);
        }
    }

    // ========== valueOf() テスト ==========

    @Nested
    @DisplayName("valueOf: 名前から取得")
    class ValueOfTests {

        @Test
        @DisplayName("test: valueOfでNORMALを取得")
        void testValueOf() {
            assertSame(SkillType.NORMAL, SkillType.valueOf("NORMAL"));
        }

        @Test
        @DisplayName("test: valueOfで無効な名前は例外")
        void testValueOfWithInvalidName() {
            assertThrows(IllegalArgumentException.class, () -> {
                SkillType.valueOf("INVALID");
            });
        }
    }
}
