# Mockito UnnecessaryStubbingException 分析

## 現状の問題
- 27個のUnnecessaryStubbingExceptionが発生
- 主にPluginMeta関連のスタブbingが不要と判定されている
- @BeforeEachメソッドで統一したモック設定が原因

## 解決策の検討
1. **lenient()アノテーションの適用** - 一部解決したが完全ではない
2. **@Mockアノテーションの使用** - テストクラスレベルで定義
3. **Mockito設定の変更** - strictnessの調整
4. **個別テストメソッドでのモック設定** - 必要なもののみを設定

## 方針
- @Mockアノテーションを使ってテストクラスレベルでモックを定義
- テスト実行時にのみ必要なスタブbingを個別に設定
- 不要なスタブbingを削除してクリーンなテストコードにする