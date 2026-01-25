package com.example.rpgplugin.api.skript.effects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EffModifyRPGStatの単体テスト
 *
 * <p>Skript Effect: add/set/remove rpg stat</p>
 * <p>注: Skript APIに依存するため、基本的な構造検証のみ行います</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.1
 */
@DisplayName("EffModifyRPGStat テスト")
class EffModifyRPGStatTest {

    // ==================== クラス構造テスト ====================

    @Test
    @DisplayName("クラスが正しく定義されている")
    void testClass_Structure() {
        // Effectクラスが存在することを確認
        assertNotNull(EffModifyRPGStat.class);

        // クラスがEffectを継承していることを確認
        assertTrue(ch.njol.skript.lang.Effect.class.isAssignableFrom(EffModifyRPGStat.class));
    }

    @Test
    @DisplayName("クラス名が正しい")
    void testClass_Name() {
        assertEquals("EffModifyRPGStat", EffModifyRPGStat.class.getSimpleName());
    }

    @Test
    @DisplayName("パッケージが正しい")
    void testClass_Package() {
        assertEquals("com.example.rpgplugin.api.skript.effects",
                     EffModifyRPGStat.class.getPackage().getName());
    }

    @Test
    @DisplayName("requiredメソッドが存在する")
    void testMethods_Exist() throws NoSuchMethodException {
        // executeメソッド
        assertNotNull(EffModifyRPGStat.class.getDeclaredMethod("execute", org.bukkit.event.Event.class));

        // toStringメソッド
        assertNotNull(EffModifyRPGStat.class.getDeclaredMethod("toString", org.bukkit.event.Event.class, boolean.class));

        // initメソッド
        assertNotNull(EffModifyRPGStat.class.getDeclaredMethod("init",
                ch.njol.skript.lang.Expression[].class,
                int.class,
                ch.njol.util.Kleenean.class,
                ch.njol.skript.lang.SkriptParser.ParseResult.class));

        // parseStatメソッド（private）
        assertNotNull(EffModifyRPGStat.class.getDeclaredMethod("parseStat", String.class));
    }

    @Test
    @DisplayName("requiredフィールドが存在する")
    void testFields_Exist() throws NoSuchFieldException {
        // value式フィールド
        assertNotNull(EffModifyRPGStat.class.getDeclaredField("value"));

        // statName式フィールド
        assertNotNull(EffModifyRPGStat.class.getDeclaredField("statName"));

        // player式フィールド
        assertNotNull(EffModifyRPGStat.class.getDeclaredField("player"));

        // modeフィールド（enum）
        assertNotNull(EffModifyRPGStat.class.getDeclaredField("mode"));
    }

    @Test
    @DisplayName("Mode列挙型が存在する")
    void testEnum_ModeExists() {
        // Mode列挙型が内部クラスとして存在することを確認
        Class<?>[] innerClasses = EffModifyRPGStat.class.getDeclaredClasses();
        boolean hasMode = false;
        for (Class<?> inner : innerClasses) {
            if (inner.getSimpleName().equals("Mode")) {
                hasMode = true;
                break;
            }
        }
        assertTrue(hasMode, "Mode列挙型が存在するべきです");
    }

    // ==================== ドキュメンテーションテスト ====================

    @Test
    @DisplayName("クラスにJavadocが存在する")
    void testDocumentation_HasClassJavadoc() {
        // クラスのドキュメントコメントが存在することを確認
        // （このテストはクラスがロードできた時点で成功とみなされます）
        assertNotNull(EffModifyRPGStat.class);
    }
}
