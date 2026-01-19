package com.example.rpgplugin.skill.result;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SkillExecutionResultの単体テスト
 *
 * <p>スキル実行結果クラスの不変性、ファクトリメソッド、ビルダーのテスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("SkillExecutionResult テスト")
class SkillExecutionResultTest {

    // ==================== ファクトリメソッド テスト ====================

    @Test
    @DisplayName("success(): 成功結果を作成")
    void testSuccess() {
        SkillExecutionResult result = SkillExecutionResult.success();

        assertTrue(result.isSuccess(), "成功フラグがtrueであること");
        assertNull(result.getErrorMessage(), "エラーメッセージがnullであること");
        assertEquals(0.0, result.getDamage(), 0.001, "ダメージが0であること");
        assertEquals(0, result.getTargetsHit(), "ターゲット数が0であること");
        assertEquals(0.0, result.getCostConsumed(), 0.001, "消費コストが0であること");
        assertTrue(result.getAllCustomData().isEmpty(), "カスタムデータが空であること");
    }

    @Test
    @DisplayName("successWithDamage(): ダメージ付き成功結果を作成")
    void testSuccessWithDamage() {
        SkillExecutionResult result = SkillExecutionResult.successWithDamage(100.5, 3);

        assertTrue(result.isSuccess(), "成功フラグがtrueであること");
        assertEquals(100.5, result.getDamage(), 0.001, "ダメージが正しいこと");
        assertEquals(3, result.getTargetsHit(), "ターゲット数が正しいこと");
        assertEquals(0.0, result.getCostConsumed(), 0.001, "消費コストが0であること");
    }

    @Test
    @DisplayName("successWithDamage(): ゼロダメージ")
    void testSuccessWithDamage_Zero() {
        SkillExecutionResult result = SkillExecutionResult.successWithDamage(0.0, 0);

        assertTrue(result.isSuccess());
        assertEquals(0.0, result.getDamage(), 0.001);
        assertEquals(0, result.getTargetsHit());
    }

    @Test
    @DisplayName("successWithDamage(): 負のダメージ")
    void testSuccessWithDamage_Negative() {
        SkillExecutionResult result = SkillExecutionResult.successWithDamage(-10.0, 1);

        assertTrue(result.isSuccess());
        assertEquals(-10.0, result.getDamage(), 0.001, "負のダメージも許容すること");
    }

    @Test
    @DisplayName("successWithCost(): コスト消費付き成功結果を作成")
    void testSuccessWithCost() {
        SkillExecutionResult result = SkillExecutionResult.successWithCost(50.0, 2, 25.0);

        assertTrue(result.isSuccess(), "成功フラグがtrueであること");
        assertEquals(50.0, result.getDamage(), 0.001, "ダメージが正しいこと");
        assertEquals(2, result.getTargetsHit(), "ターゲット数が正しいこと");
        assertEquals(25.0, result.getCostConsumed(), 0.001, "消費コストが正しいこと");
    }

    @Test
    @DisplayName("failure(): 失敗結果を作成")
    void testFailure() {
        SkillExecutionResult result = SkillExecutionResult.failure("Mana不足");

        assertFalse(result.isSuccess(), "成功フラグがfalseであること");
        assertEquals("Mana不足", result.getErrorMessage(), "エラーメッセージが正しいこと");
        assertEquals(0.0, result.getDamage(), 0.001, "ダメージが0であること");
        assertEquals(0, result.getTargetsHit(), "ターゲット数が0であること");
        assertEquals(0.0, result.getCostConsumed(), 0.001, "消費コストが0であること");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("failure(): 空白エラーメッセージ")
    void testFailure_EmptyMessage(String errorMessage) {
        SkillExecutionResult result = SkillExecutionResult.failure(errorMessage);

        assertFalse(result.isSuccess());
        assertEquals(errorMessage, result.getErrorMessage());
    }

    // ==================== ビルダー テスト ====================

    @Test
    @DisplayName("Builder: デフォルト値で成功結果を作成")
    void testBuilder_DefaultSuccess() {
        SkillExecutionResult result = SkillExecutionResult.builder().build();

        assertTrue(result.isSuccess(), "デフォルトで成功であること");
        assertNull(result.getErrorMessage());
        assertEquals(0.0, result.getDamage(), 0.001);
        assertEquals(0, result.getTargetsHit());
        assertEquals(0.0, result.getCostConsumed(), 0.001);
        assertTrue(result.getAllCustomData().isEmpty());
    }

    @Test
    @DisplayName("Builder: 全フィールドを設定")
    void testBuilder_AllFields() {
        SkillExecutionResult result = SkillExecutionResult.builder()
                .success(true)
                .damage(150.0)
                .targetsHit(5)
                .costConsumed(30.0)
                .addCustomData("critical", true)
                .addCustomData("element", "FIRE")
                .build();

        assertTrue(result.isSuccess());
        assertEquals(150.0, result.getDamage(), 0.001);
        assertEquals(5, result.getTargetsHit());
        assertEquals(30.0, result.getCostConsumed(), 0.001);
        assertEquals(true, result.getCustomData("critical"));
        assertEquals("FIRE", result.getCustomData("element"));
    }

    @Test
    @DisplayName("Builder: errorMessage()でsuccessがfalseになる")
    void testBuilder_ErrorMessageSetsFailure() {
        SkillExecutionResult result = SkillExecutionResult.builder()
                .errorMessage("クールダウン中")
                .build();

        assertFalse(result.isSuccess(), "errorMessage設定で失敗になること");
        assertEquals("クールダウン中", result.getErrorMessage());
    }

    @Test
    @DisplayName("Builder: success(false)とerrorMessageの組み合わせ")
    void testBuilder_SuccessFalseAndMessage() {
        SkillExecutionResult result = SkillExecutionResult.builder()
                .success(false)
                .errorMessage("ターゲット不在")
                .build();

        assertFalse(result.isSuccess());
        assertEquals("ターゲット不在", result.getErrorMessage());
    }

    @Test
    @DisplayName("Builder: カスタムデータを一括設定")
    void testBuilder_CustomDataBatch() {
        Map<String, Object> data = Map.of(
                "key1", "value1",
                "key2", 42,
                "key3", true
        );

        SkillExecutionResult result = SkillExecutionResult.builder()
                .customData(data)
                .build();

        assertEquals("value1", result.getCustomData("key1"));
        assertEquals(42, (Integer) result.getCustomData("key2"));
        assertEquals(true, (Boolean) result.getCustomData("key3"));
    }

    @Test
    @DisplayName("Builder: メソッドチェーン")
    void testBuilder_MethodChaining() {
        SkillExecutionResult result = SkillExecutionResult.builder()
                .damage(100.0)
                .targetsHit(3)
                .costConsumed(20.0)
                .addCustomData("combo", true)
                .build();

        assertTrue(result.isSuccess());
        assertEquals(100.0, result.getDamage(), 0.001);
        assertEquals(3, result.getTargetsHit());
        assertEquals(20.0, result.getCostConsumed(), 0.001);
        assertEquals(true, result.getCustomData("combo"));
    }

    @Test
    @DisplayName("Builder: nullのcustomDataは無視")
    void testBuilder_NullCustomData() {
        SkillExecutionResult result = SkillExecutionResult.builder()
                .customData(null)
                .build();

        assertTrue(result.getAllCustomData().isEmpty());
    }

    // ==================== カスタムデータ テスト ====================

    @Test
    @DisplayName("getCustomData(): 値を取得")
    void testGetCustomData() {
        SkillExecutionResult result = SkillExecutionResult.builder()
                .addCustomData("string", "test")
                .addCustomData("integer", 123)
                .addCustomData("double", 45.6)
                .addCustomData("boolean", true)
                .build();

        assertEquals("test", result.getCustomData("string"));
        assertEquals(123, (Integer) result.getCustomData("integer"));
        assertEquals(45.6, result.getCustomData("double"), 0.001);
        assertEquals(true, (Boolean) result.getCustomData("boolean"));
    }

    @Test
    @DisplayName("getCustomData(): 存在しないキーはnull")
    void testGetCustomData_NotExist() {
        SkillExecutionResult result = SkillExecutionResult.success();

        assertNull(result.getCustomData("nonexistent"));
    }

    @Test
    @DisplayName("getCustomData(key, default): デフォルト値")
    void testGetCustomData_WithDefault() {
        SkillExecutionResult result = SkillExecutionResult.builder()
                .addCustomData("existing", "value")
                .build();

        assertEquals("value", result.getCustomData("existing", "default"));
        assertEquals("default", result.getCustomData("nonexistent", "default"));
    }

    @Test
    @DisplayName("getAllCustomData(): 読み取りビューを返す")
    void testGetAllCustomData() {
        SkillExecutionResult result = SkillExecutionResult.builder()
                .addCustomData("key1", "value1")
                .addCustomData("key2", "value2")
                .build();

        Map<String, Object> customData = result.getAllCustomData();

        assertEquals(2, customData.size());
        assertEquals("value1", customData.get("key1"));
        assertEquals("value2", customData.get("key2"));

        // 読み取りビューの確認（変更不可能）
        assertThrows(UnsupportedOperationException.class, () -> {
            customData.put("key3", "value3");
        });
    }

    // ==================== toString テスト ====================

    @Test
    @DisplayName("toString(): 成功時の文字列表現")
    void testToString_Success() {
        SkillExecutionResult result = SkillExecutionResult.successWithDamage(100.0, 3);

        String str = result.toString();

        assertTrue(str.contains("success=true"));
        assertTrue(str.contains("damage=100.0"));
        assertTrue(str.contains("targetsHit=3"));
        assertFalse(str.contains("errorMessage"));
    }

    @Test
    @DisplayName("toString(): 失敗時の文字列表現")
    void testToString_Failure() {
        SkillExecutionResult result = SkillExecutionResult.failure("テストエラー");

        String str = result.toString();

        assertTrue(str.contains("success=false"));
        assertTrue(str.contains("errorMessage='テストエラー'"));
        assertFalse(str.contains("damage"));
    }

    // ==================== 不変性 テスト ====================

    @Test
    @DisplayName("不変性: ファクトリメソッドで作成した結果は同じ値を返す")
    void testImmutability_FactoryMethods() {
        SkillExecutionResult result = SkillExecutionResult.successWithCost(50.0, 2, 25.0);

        assertEquals(50.0, result.getDamage(), 0.001);
        assertEquals(50.0, result.getDamage(), 0.001, "複数回呼び出しても同じ値");
        assertEquals(2, (int) result.getTargetsHit());
        assertEquals(2, (int) result.getTargetsHit(), "複数回呼び出しても同じ値");
    }

    @Test
    @DisplayName("不変性: カスタムデータのコピー防御")
    void testImmutability_CustomDataCopy() {
        Map<String, Object> originalData = new java.util.HashMap<>();
        originalData.put("key", "value");

        SkillExecutionResult result = SkillExecutionResult.builder()
                .customData(originalData)
                .build();

        // 元のマップを変更
        originalData.put("key", "modified");
        originalData.put("newKey", "newValue");

        // 結果は影響を受けない
        assertEquals("value", result.getCustomData("key"));
        assertNull(result.getCustomData("newKey"));
    }

    // ==================== エッジケース テスト ====================

    @Test
    @DisplayName("エッジケース: 最大値")
    void testEdgeCase_MaxValues() {
        SkillExecutionResult result = SkillExecutionResult.builder()
                .damage(Double.MAX_VALUE)
                .targetsHit(Integer.MAX_VALUE)
                .costConsumed(Double.MAX_VALUE)
                .build();

        assertEquals(Double.MAX_VALUE, result.getDamage(), 0.0);
        assertEquals(Integer.MAX_VALUE, result.getTargetsHit());
        assertEquals(Double.MAX_VALUE, result.getCostConsumed(), 0.0);
    }

    @Test
    @DisplayName("エッジケース: 最小値")
    void testEdgeCase_MinValues() {
        SkillExecutionResult result = SkillExecutionResult.builder()
                .damage(Double.MIN_VALUE)
                .targetsHit(Integer.MIN_VALUE)
                .costConsumed(Double.MIN_VALUE)
                .build();

        assertEquals(Double.MIN_VALUE, result.getDamage(), 0.0);
        assertEquals(Integer.MIN_VALUE, result.getTargetsHit());
        assertEquals(Double.MIN_VALUE, result.getCostConsumed(), 0.0);
    }

    @Test
    @DisplayName("エッジケース: NaNダメージ")
    void testEdgeCase_NaN() {
        SkillExecutionResult result = SkillExecutionResult.builder()
                .damage(Double.NaN)
                .build();

        assertTrue(Double.isNaN(result.getDamage()));
    }

    @Test
    @DisplayName("エッジケース: Infinity")
    void testEdgeCase_Infinity() {
        SkillExecutionResult result = SkillExecutionResult.builder()
                .damage(Double.POSITIVE_INFINITY)
                .costConsumed(Double.NEGATIVE_INFINITY)
                .build();

        assertTrue(Double.isInfinite(result.getDamage()));
        assertTrue(Double.isInfinite(result.getCostConsumed()));
    }
}
