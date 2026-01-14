package com.example.rpgplugin.core.validation;

import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * 入力バリデーションユーティリティクラス
 *
 * <p>UUID、数値範囲、文字列の検証を行います。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: バリデーションに専念</li>
 *   <li>KISS: シンプルな静的メソッド</li>
 *   <li>DRY: バリデーションロジックを一元管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public final class ValidationUtils {

    private static final Logger LOGGER = Logger.getLogger(ValidationUtils.class.getName());

    // UUIDの正規表現パターン
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    // プレイヤー名の正規表現（Minecraftの命名規則）
    private static final Pattern PLAYERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{1,16}$");

    // スキルID/クラスIDの正規表現
    private static final Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{1,64}$");

    private ValidationUtils() {
        // ユーティリティクラスのためインスタンス化禁止
    }

    // ==================== UUID 検証 ====================

    /**
     * UUIDが有効か検証します
     *
     * @param uuid 検証するUUID
     * @return 有効な場合true
     */
    public static boolean isValidUUID(UUID uuid) {
        return uuid != null;
    }

    /**
     * UUID文字列が有効か検証します
     *
     * @param uuidString UUID文字列
     * @return 有効な場合true
     */
    public static boolean isValidUUIDString(String uuidString) {
        if (uuidString == null || uuidString.isEmpty()) {
            return false;
        }
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * UUIDを安全にパースします
     *
     * @param uuidString UUID文字列
     * @return パースされたUUID。無効な場合はnull
     */
    public static UUID parseUUIDSafely(String uuidString) {
        if (uuidString == null || uuidString.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            LOGGER.warning(() -> "Invalid UUID format: " + uuidString);
            return null;
        }
    }

    // ==================== 数値範囲検証 ====================

    /**
     * レベル値が有効か検証します
     *
     * @param level レベル値
     * @return 1-99の範囲内の場合true
     */
    public static boolean isValidLevel(int level) {
        return level >= 1 && level <= 99;
    }

    /**
     * レベル値を有効範囲にクランプします
     *
     * @param level レベル値
     * @return 1-99の範囲に収めた値
     */
    public static int clampLevel(int level) {
        return Math.max(1, Math.min(99, level));
    }

    /**
     * ステータス値が有効か検証します
     *
     * @param stat ステータス値
     * @return 0-255の範囲内の場合true
     */
    public static boolean isValidStat(int stat) {
        return stat >= 0 && stat <= 255;
    }

    /**
     * ステータス値を有効範囲にクランプします
     *
     * @param stat ステータス値
     * @return 0-255の範囲に収めた値
     */
    public static int clampStat(int stat) {
        return Math.max(0, Math.min(255, stat));
    }

    /**
     * ポイント値が有効か検証します
     *
     * @param points ポイント値
     * @return 0以上の場合true
     */
    public static boolean isValidPoints(int points) {
        return points >= 0;
    }

    /**
     * マナ/HP値が有効か検証します
     *
     * @param value マナ/HP値
     * @return 0以上の場合true
     */
    public static boolean isValidHealthMana(int value) {
        return value >= 0;
    }

    /**
     * マナ/HP値を有効範囲にクランプします
     *
     * @param value マナ/HP値
     * @param max 最大値
     * @return 0-maxの範囲に収めた値
     */
    public static int clampHealthMana(int value, int max) {
        return Math.max(0, Math.min(max, value));
    }

    // ==================== 文字列検証 ====================

    /**
     * プレイヤー名が有効か検証します
     *
     * @param name プレイヤー名
     * @return 有効な場合true
     */
    public static boolean isValidPlayerName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return PLAYERNAME_PATTERN.matcher(name).matches();
    }

    /**
     * スキルID/クラスIDが有効か検証します
     *
     * @param id ID文字列
     * @return 有効な場合true
     */
    public static boolean isValidId(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        return ID_PATTERN.matcher(id).matches();
    }

    /**
     * 文字列が空でないか検証します
     *
     * @param str 文字列
     * @return nullまたは空文字列でない場合true
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    // ==================== 例外をスローする検証 ====================

    /**
     * UUIDが有効でない場合に例外をスローします
     *
     * @param uuid 検証するUUID
     * @throws IllegalArgumentException uuidがnullの場合
     */
    public static void requireValidUUID(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }
    }

    /**
     * UUID文字列が有効でない場合に例外をスローします
     *
     * @param uuidString UUID文字列
     * @throws IllegalArgumentException 無効なUUID形式の場合
     */
    public static void requireValidUUIDString(String uuidString) {
        if (!isValidUUIDString(uuidString)) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuidString);
        }
    }

    /**
     * レベル値が有効でない場合に例外をスローします
     *
     * @param level レベル値
     * @throwsIllegalArgumentException 無効なレベル値の場合
     */
    public static void requireValidLevel(int level) {
        if (!isValidLevel(level)) {
            throw new IllegalArgumentException("Level must be between 1 and 99: " + level);
        }
    }

    /**
     * ステータス値が有効でない場合に例外をスローします
     *
     * @param stat ステータス値
     * @param statName ステータス名（エラーメッセージ用）
     * @throws IllegalArgumentException 無効なステータス値の場合
     */
    public static void requireValidStat(int stat, String statName) {
        if (!isValidStat(stat)) {
            throw new IllegalArgumentException(
                statName + " must be between 0 and 255: " + stat
            );
        }
    }

    /**
     * 文字列が空の場合に例外をスローします
     *
     * @param str 文字列
     * @param paramName パラメータ名（エラーメッセージ用）
     * @throws IllegalArgumentException 空文字列の場合
     */
    public static void requireNotEmpty(String str, String paramName) {
        if (!isNotEmpty(str)) {
            throw new IllegalArgumentException(paramName + " cannot be null or empty");
        }
    }

    /**
     * 値が正の場合に例外をスローします
     *
     * @param value 検証する値
     * @param paramName パラメータ名（エラーメッセージ用）
     * @throws IllegalArgumentException 値が正の場合
     */
    public static void requireNonPositive(int value, String paramName) {
        if (value > 0) {
            throw new IllegalArgumentException(paramName + " must be 0 or negative: " + value);
        }
    }

    /**
     * 値が負でない場合に例外をスローします
     *
     * @param value 検証する値
     * @param paramName パラメータ名（エラーメッセージ用）
     * @throws IllegalArgumentException 値が負の場合
     */
    public static void requireNonNegative(int value, String paramName) {
        if (value < 0) {
            throw new IllegalArgumentException(paramName + " cannot be negative: " + value);
        }
    }
}
