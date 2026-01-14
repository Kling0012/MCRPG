package com.example.rpgplugin.core.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ValidationUtilsの単体テスト
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("ValidationUtils テスト")
class ValidationUtilsTest {

    // ==================== UUID 検証 ====================

    @Test
    @DisplayName("UUID検証: 有効なUUID")
    void testIsValidUUID_Valid() {
        UUID uuid = UUID.randomUUID();
        assertTrue(ValidationUtils.isValidUUID(uuid));
    }

    @Test
    @DisplayName("UUID検証: nullは無効")
    void testIsValidUUID_Null() {
        assertFalse(ValidationUtils.isValidUUID(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "123e4567-e89b-12d3-a456-426614174000",
        "00000000-0000-0000-0000-000000000000",
        "ffffffff-ffff-ffff-ffff-ffffffffffff"
    })
    @DisplayName("UUID文字列検証: 有効な形式")
    void testIsValidUUIDString_Valid(String uuidString) {
        assertTrue(ValidationUtils.isValidUUIDString(uuidString));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid",
        "123e4567-e89b-12d3-a456",
        "not-a-uuid",
        "123e4567-e89b-12d3-a456-42661417400x"  // x at end
    })
    @NullAndEmptySource
    @DisplayName("UUID文字列検証: 無効な形式")
    void testIsValidUUIDString_Invalid(String uuidString) {
        assertFalse(ValidationUtils.isValidUUIDString(uuidString));
    }

    @Test
    @DisplayName("UUID安全パース: 有効な形式")
    void testParseUUIDSafely_Valid() {
        String uuidString = "123e4567-e89b-12d3-a456-426614174000";
        UUID result = ValidationUtils.parseUUIDSafely(uuidString);
        assertNotNull(result);
        assertEquals(uuidString, result.toString());
    }

    @Test
    @DisplayName("UUID安全パース: 無効な形式はnullを返す")
    void testParseUUIDSafely_Invalid() {
        assertNull(ValidationUtils.parseUUIDSafely("invalid"));
    }

    // ==================== 数値範囲検証 ====================

    @ParameterizedTest
    @ValueSource(ints = {1, 50, 99})
    @DisplayName("レベル検証: 有効範囲")
    void testIsValidLevel_Valid(int level) {
        assertTrue(ValidationUtils.isValidLevel(level));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 100, 999})
    @DisplayName("レベル検証: 無効範囲")
    void testIsValidLevel_Invalid(int level) {
        assertFalse(ValidationUtils.isValidLevel(level));
    }

    @Test
    @DisplayName("レベルクランプ: 範囲内に収める")
    void testClampLevel() {
        assertEquals(1, ValidationUtils.clampLevel(0));
        assertEquals(1, ValidationUtils.clampLevel(-10));
        assertEquals(50, ValidationUtils.clampLevel(50));
        assertEquals(99, ValidationUtils.clampLevel(100));
        assertEquals(99, ValidationUtils.clampLevel(999));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 128, 255})
    @DisplayName("ステータス検証: 有効範囲")
    void testIsValidStat_Valid(int stat) {
        assertTrue(ValidationUtils.isValidStat(stat));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 256, 999})
    @DisplayName("ステータス検証: 無効範囲")
    void testIsValidStat_Invalid(int stat) {
        assertFalse(ValidationUtils.isValidStat(stat));
    }

    @Test
    @DisplayName("ステータスクランプ: 範囲内に収める")
    void testClampStat() {
        assertEquals(0, ValidationUtils.clampStat(-10));
        assertEquals(100, ValidationUtils.clampStat(100));
        assertEquals(255, ValidationUtils.clampStat(300));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 100, 999})
    @DisplayName("ポイント検証: 0以上は有効")
    void testIsValidPoints_Valid(int points) {
        assertTrue(ValidationUtils.isValidPoints(points));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100})
    @DisplayName("ポイント検証: 負数は無効")
    void testIsValidPoints_Invalid(int points) {
        assertFalse(ValidationUtils.isValidPoints(points));
    }

    // ==================== 文字列検証 ====================

    @ParameterizedTest
    @ValueSource(strings = {"Player", "Test_123", "a", "PlayerName123"})
    @DisplayName("プレイヤー名検証: 有効な名前")
    void testIsValidPlayerName_Valid(String name) {
        assertTrue(ValidationUtils.isValidPlayerName(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Player Name", "Player@Name", "-Player", "verylongplayernameexceeding16"})
    @NullAndEmptySource
    @DisplayName("プレイヤー名検証: 無効な名前")
    void testIsValidPlayerName_Invalid(String name) {
        assertFalse(ValidationUtils.isValidPlayerName(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"skill_id", "Skill123", "test_skill", "a", "A"})
    @DisplayName("ID検証: 有効なID")
    void testIsValidId_Valid(String id) {
        assertTrue(ValidationUtils.isValidId(id));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "skill-id", "skill.id", "skill id", "-skill"})
    @NullAndEmptySource
    @DisplayName("ID検証: 無効なID")
    void testIsValidId_Invalid(String id) {
        assertFalse(ValidationUtils.isValidId(id));
    }

    @Test
    @DisplayName("空文字検証: 有効な文字列")
    void testIsNotEmpty_Valid() {
        assertTrue(ValidationUtils.isNotEmpty("test"));
        assertTrue(ValidationUtils.isNotEmpty(" test "));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("空文字検証: nullまたは空は無効")
    void testIsNotEmpty_Invalid(String str) {
        assertFalse(ValidationUtils.isNotEmpty(str));
    }

    @Test
    @DisplayName("空文字検証: 空白のみは無効")
    void testIsNotEmpty_WhitespaceOnly() {
        assertFalse(ValidationUtils.isNotEmpty("   "));
    }

    // ==================== 例外をスローする検証 ====================

    @Test
    @DisplayName("requireValidUUID: 有効なUUIDは通過")
    void testRequireValidUUID_Valid() {
        UUID uuid = UUID.randomUUID();
        assertDoesNotThrow(() -> ValidationUtils.requireValidUUID(uuid));
    }

    @Test
    @DisplayName("requireValidUUID: nullは例外")
    void testRequireValidUUID_Null() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireValidUUID(null));
    }

    @Test
    @DisplayName("requireValidLevel: 有効なレベルは通過")
    void testRequireValidLevel_Valid() {
        assertDoesNotThrow(() -> ValidationUtils.requireValidLevel(50));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 100})
    @DisplayName("requireValidLevel: 無効なレベルは例外")
    void testRequireValidLevel_Invalid(int level) {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireValidLevel(level));
    }

    @Test
    @DisplayName("requireNotEmpty: 有効な文字列は通過")
    void testRequireNotEmpty_Valid() {
        assertDoesNotThrow(() -> ValidationUtils.isNotEmpty("test"));
    }

    @Test
    @DisplayName("requireNotEmpty: nullは例外")
    void testRequireNotEmpty_Null() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNotEmpty(null, "testParam"));
    }

    @Test
    @DisplayName("requireNonNegative: 有効な値は通過")
    void testRequireNonNegative_Valid() {
        assertDoesNotThrow(() -> ValidationUtils.requireNonNegative(0, "test"));
        assertDoesNotThrow(() -> ValidationUtils.requireNonNegative(100, "test"));
    }

    @Test
    @DisplayName("requireNonNegative: 負数は例外")
    void testRequireNonNegative_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNonNegative(-1, "test"));
    }
}
