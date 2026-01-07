package com.example.rpgplugin.skill.evaluator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 変数コンテキストのテストクラス
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("変数コンテキスト テスト")
class VariableContextTest {

    private VariableContext context;

    @BeforeEach
    void setUp() {
        context = new VariableContext(null, 5);
    }

    // ===== カスタム変数テスト =====

    @Test
    @DisplayName("カスタム変数設定・取得テスト")
    void testCustomVariable() {
        context.setCustomVariable("test_var", 42.0);
        Double result = context.getVariable("test_var");

        assertNotNull(result);
        assertEquals(42.0, result, 0.001);
    }

    @Test
    @DisplayName("カスタム変数一括設定テスト")
    void testSetCustomVariables() {
        Map<String, Double> vars = Map.of(
                "var1", 10.0,
                "var2", 20.0,
                "var3", 30.0
        );
        context.setCustomVariables(vars);

        assertEquals(10.0, context.getVariable("var1"), 0.001);
        assertEquals(20.0, context.getVariable("var2"), 0.001);
        assertEquals(30.0, context.getVariable("var3"), 0.001);
    }

    @Test
    @DisplayName("カスタム変数文字列設定テスト")
    void testSetCustomVariableFromString() {
        context.setCustomVariable("var1", "123.45");

        assertEquals(123.45, context.getVariable("var1"), 0.001);
    }

    @Test
    @DisplayName("カスタム変数上書きテスト")
    void testCustomVariableOverride() {
        context.setCustomVariable("var", 10.0);
        assertEquals(10.0, context.getVariable("var"), 0.001);

        context.setCustomVariable("var", 20.0);
        assertEquals(20.0, context.getVariable("var"), 0.001);
    }

    // ===== 予約変数テスト =====

    @Test
    @DisplayName("スキルレベル変数テスト")
    void testSkillLevelVariable() {
        assertEquals(5.0, context.getVariable(VariableContext.SKILL_LEVEL), 0.001);

        context.setSkillLevel(10);
        assertEquals(10.0, context.getVariable(VariableContext.SKILL_LEVEL), 0.001);
    }

    @Test
    @DisplayName("スキルレベル境界テスト")
    void testSkillLevelBoundary() {
        context.setSkillLevel(0);
        assertEquals(1.0, context.getVariable(VariableContext.SKILL_LEVEL), 0.001);

        context.setSkillLevel(-5);
        assertEquals(1.0, context.getVariable(VariableContext.SKILL_LEVEL), 0.001);
    }

    // ===== プレイヤーレベル変数テスト =====

    @Test
    @DisplayName("プレイヤーレベル変数テスト（nullプレイヤー）")
    void testPlayerLevelVariableWithNullPlayer() {
        Double result = context.getVariable(VariableContext.PLAYER_LEVEL);
        assertNotNull(result);
        assertEquals(1.0, result, 0.001);
    }

    // ===== カスタム変数優先順位テスト =====

    @Test
    @DisplayName("カスタム変数優先テスト")
    void testCustomVariablePriority() {
        context.setCustomVariable("Lv", 999.0);

        // カスタム変数が優先される
        Double result = context.getVariable("Lv");
        assertEquals(999.0, result, 0.001);
    }

    // ===== 変数存在確認テスト =====

    @Test
    @DisplayName("変数存在確認テスト")
    void testHasVariable() {
        assertFalse(context.hasVariable("nonexistent"));

        context.setCustomVariable("existent", 10.0);
        assertTrue(context.hasVariable("existent"));
    }

    // ===== カスタム変数クリアテスト =====

    @Test
    @DisplayName("カスタム変数クリアテスト")
    void testClearCustomVariables() {
        context.setCustomVariable("var1", 10.0);
        context.setCustomVariable("var2", 20.0);

        assertTrue(context.hasVariable("var1"));
        assertTrue(context.hasVariable("var2"));

        context.clearCustomVariables();

        assertFalse(context.hasVariable("var1"));
        assertFalse(context.hasVariable("var2"));

        // 予約変数は影響を受けない
        assertTrue(context.hasVariable(VariableContext.SKILL_LEVEL));
    }

    // ===== カスタム変数マップ取得テスト =====

    @Test
    @DisplayName("カスタム変数マップ取得テスト")
    void testGetCustomVariables() {
        context.setCustomVariable("var1", 10.0);
        context.setCustomVariable("var2", 20.0);

        Map<String, Double> vars = context.getCustomVariables();

        assertEquals(2, vars.size());
        assertTrue(vars.containsKey("var1"));
        assertTrue(vars.containsKey("var2"));

        // 返されたマップは不変（コピー）
        assertThrows(UnsupportedOperationException.class, () -> vars.put("var3", 30.0));
    }

    // ===== スキルレベル取得テスト =====

    @Test
    @DisplayName("スキルレベル取得テスト")
    void testGetSkillLevel() {
        context.setSkillLevel(15);
        assertEquals(15, context.getSkillLevel());
    }

    // ===== RPGプレイヤー取得テスト =====

    @Test
    @DisplayName("RPGプレイヤー取得テスト")
    void testGetRPGPlayer() {
        VariableContext contextWithPlayer = new VariableContext(null, 1);
        assertNull(contextWithPlayer.getRPGPlayer());
    }

    // ===== toStringテスト =====

    @Test
    @DisplayName("toStringテスト")
    void testToString() {
        String str = context.toString();
        assertNotNull(str);
        assertTrue(str.contains("skillLevel=5"));
        assertTrue(str.contains("customVariables={}"));
        assertTrue(str.contains("rpgPlayer=null"));
    }

    // ===== null・空文字処理テスト =====

    @Test
    @DisplayName("null変数名テスト")
    void testNullVariableName() {
        Double result = context.getVariable(null);
        assertNull(result);
    }

    @Test
    @DisplayName("空文字変数名テスト")
    void testEmptyVariableName() {
        Double result = context.getVariable("");
        assertNull(result);
    }

    // ===== 数値文字列パース例外テスト =====

    @Test
    @DisplayName("無効な数値文字列パーステスト")
    void testInvalidNumericStringParse() {
        assertThrows(NumberFormatException.class, () -> {
            context.setCustomVariable("var", "not_a_number");
        });
    }
}
