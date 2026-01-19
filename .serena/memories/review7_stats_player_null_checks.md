# 【レビュー7】Stats + Player (9ファイル) - Nullチェック/例外処理レビュー

**レビュー日**: 2026-01-09
**ブランチ**: vk/fa4d-6-rpg-class-requ
**対象ファイル**: 9ファイル

## サマリー

| カテゴリ | 問題なし | 改善推奨 | 要修正 | 計 |
|---------|----------|----------|--------|-----|
| Nullチェック | 7 | 2 | 0 | 9 |
| 例外処理 | 8 | 1 | 0 | 9 |

**評価**: ✅ 良好 - 重大な問題なし

## 各ファイルの評価

### ✅ 優れた実装

1. **Stat.java (列挙型)**
   - fromShortName/fromDisplayNameでNullチェック完備
   - equalsIgnoreCase使用でNPE防止

2. **StatManager.java**
   - 全メソッドでStat Nullチェック + IllegalArgumentExceptionスロー
   - 負値チェック完備
   - Modifier/UUID/Source Nullチェック

3. **RPGPlayer.java**
   - Player Nullチェック安全
   - Optional<Entity>適切使用
   - skillManager Nullチェック

4. **PlayerManager.java**
   - UUID Nullチェック完備
   - 例外処理適切
   - 保存時Nullチェック

5. **ExpManager.java**
   - シンプルで安全

6. **ManaManager.java**
   - CostType.fromId Null安全
   - 値範囲チェック完備
   - ゼロ除算防止

7. **PlayerData.java**
   - costType/classHistory Nullチェック
   - 値範囲チェック

8. **StorageManager.java**
   - 初期化チェック完備
   - ConfigurationSection Nullチェック
   - 例外処理

### ⚠️ 改善推奨

1. **DropData.java**
   - 251-279行: deserializeメソッドでNumberFormatExceptionをキャッチ（現状: TODOコメント）

## 優れている点

- 一貫したNullチェック
- 防御的プログラミング（IllegalArgumentException）
- Optionalの適切な使用
- 値範囲チェック（Math.maxパターン）

## 優先度別改善タスク

| 優先度 | ファイル | 行 | 内容 |
|--------|----------|-----|------|
| 🟢 低 | DropData.java | 251-279 | deserializeでNumberFormatExceptionキャッチ |
