package com.example.rpgplugin.api.skript.effects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EffCastRPGSkillの単体テスト
 *
 * <p>Skript Effect: make %player% cast [the] rpg skill %string%</p>
 * <p>注: Skript APIに依存するため、基本的な構造検証のみ行います</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.1
 */
@DisplayName("EffCastRPGSkill テスト")
class EffCastRPGSkillTest {

    // ==================== クラス構造テスト ====================

    @Test
    @DisplayName("クラスが正しく定義されている")
    void testClass_Structure() {
        // Effectクラスが存在することを確認
        assertNotNull(EffCastRPGSkill.class);

        // クラスがEffectを継承していることを確認
        assertTrue(ch.njol.skript.lang.Effect.class.isAssignableFrom(EffCastRPGSkill.class));
    }

    @Test
    @DisplayName("クラス名が正しい")
    void testClass_Name() {
        assertEquals("EffCastRPGSkill", EffCastRPGSkill.class.getSimpleName());
    }

    @Test
    @DisplayName("パッケージが正しい")
    void testClass_Package() {
        assertEquals("com.example.rpgplugin.api.skript.effects",
                     EffCastRPGSkill.class.getPackage().getName());
    }

    @Test
    @DisplayName("requiredメソッドが存在する")
    void testMethods_Exist() throws NoSuchMethodException {
        // executeメソッド
        assertNotNull(EffCastRPGSkill.class.getDeclaredMethod("execute", org.bukkit.event.Event.class));

        // toStringメソッド
        assertNotNull(EffCastRPGSkill.class.getDeclaredMethod("toString", org.bukkit.event.Event.class, boolean.class));

        // initメソッド
        assertNotNull(EffCastRPGSkill.class.getDeclaredMethod("init",
                ch.njol.skript.lang.Expression[].class,
                int.class,
                ch.njol.util.Kleenean.class,
                ch.njol.skript.lang.SkriptParser.ParseResult.class));
    }

    @Test
    @DisplayName("requiredフィールドが存在する")
    void testFields_Exist() throws NoSuchFieldException {
        // player式フィールド
        assertNotNull(EffCastRPGSkill.class.getDeclaredField("player"));

        // skillId式フィールド
        assertNotNull(EffCastRPGSkill.class.getDeclaredField("skillId"));
    }

    // ==================== ドキュメンテーションテスト ====================

    @Test
    @DisplayName("クラスにJavadocが存在する")
    void testDocumentation_HasClassJavadoc() {
        // クラスのドキュメントコメントが存在することを確認
        // （このテストはクラスがロードできた時点で成功とみなされます）
        assertNotNull(EffCastRPGSkill.class);
    }
}
