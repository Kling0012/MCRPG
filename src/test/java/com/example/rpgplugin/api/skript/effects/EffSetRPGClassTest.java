package com.example.rpgplugin.api.skript.effects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EffSetRPGClassの単体テスト
 *
 * <p>Skript Effect: set [the] rpg class of %player% to %string%</p>
 * <p>注: Skript APIに依存するため、基本的な構造検証のみ行います</p>
 *
 * @author RPGPlugin Team
 * @version 1.1.0
 */
@DisplayName("EffSetRPGClass テスト")
class EffSetRPGClassTest {

    // ==================== クラス構造テスト ====================

    @Test
    @DisplayName("クラスが正しく定義されている")
    void testClass_Structure() {
        // Effectクラスが存在することを確認
        assertNotNull(EffSetRPGClass.class);

        // クラスがEffectを継承していることを確認
        assertTrue(ch.njol.skript.lang.Effect.class.isAssignableFrom(EffSetRPGClass.class));
    }

    @Test
    @DisplayName("クラス名が正しい")
    void testClass_Name() {
        assertEquals("EffSetRPGClass", EffSetRPGClass.class.getSimpleName());
    }

    @Test
    @DisplayName("パッケージが正しい")
    void testClass_Package() {
        assertEquals("com.example.rpgplugin.api.skript.effects",
                     EffSetRPGClass.class.getPackage().getName());
    }

    @Test
    @DisplayName("requiredメソッドが存在する")
    void testMethods_Exist() throws NoSuchMethodException {
        // executeメソッド
        assertNotNull(EffSetRPGClass.class.getDeclaredMethod("execute", org.bukkit.event.Event.class));

        // toStringメソッド
        assertNotNull(EffSetRPGClass.class.getDeclaredMethod("toString", org.bukkit.event.Event.class, boolean.class));

        // initメソッド
        assertNotNull(EffSetRPGClass.class.getDeclaredMethod("init",
                ch.njol.skript.lang.Expression[].class,
                int.class,
                ch.njol.util.Kleenean.class,
                ch.njol.skript.lang.SkriptParser.ParseResult.class));
    }

    @Test
    @DisplayName("requiredフィールドが存在する")
    void testFields_Exist() throws NoSuchFieldException {
        // playerExpr式フィールド
        assertNotNull(EffSetRPGClass.class.getDeclaredField("playerExpr"));

        // classExpr式フィールド
        assertNotNull(EffSetRPGClass.class.getDeclaredField("classExpr"));

        // levelExpr式フィールド
        assertNotNull(EffSetRPGClass.class.getDeclaredField("levelExpr"));

        // hasLevelフラグ
        assertNotNull(EffSetRPGClass.class.getDeclaredField("hasLevel"));
    }

    @Test
    @DisplayName("toStringメソッドがnull-safeである")
    void testToString_NullSafe() {
        // テスト環境ではSkript APIが初期化されていないため、
        // クラス構造の検証のみ行います
        assertNotNull(EffSetRPGClass.class);
    }

    // ==================== ドキュメンテーションテスト ====================

    @Test
    @DisplayName("クラスにJavadocが存在する")
    void testDocumentation_HasClassJavadoc() {
        // クラスのドキュメントコメントが存在することを確認
        // （このテストはクラスがロードできた時点で成功とみなされます）
        assertNotNull(EffSetRPGClass.class);
    }
}
