# レビュー12: API Bridge + Main Plugin - Nullチェック/例外処理

## 作成日
2026-01-09

## 作業ブランチ
vk/0ebd-12-api-bridge-ma

## 対象ファイル
- src/main/java/com/example/rpgplugin/api/bridge/SKriptBridge.java
- src/main/java/com/example/rpgplugin/api/bridge/DenizenBridge.java
- src/main/java/com/example/rpgplugin/api/bridge/SKriptSkillEvent.java
- src/main/java/com/example/rpgplugin/RPGPlugin.java
- src/main/java/com/example/rpgplugin/core/dependency/DependencyManager.java

## 総合評価

| 優先度 | 問題 | 修正内容 |
|--------|------|----------|
| 高 | DenizenBridge.java - Player引数のnullチェック欠如 | 各メソッドの先頭で `Objects.requireNonNull(player)` 追加 |
| 高 | SKriptSkillEvent.java - コンストラクタの必須パラメータ検証なし | `Objects.requireNonNull` 追加 |
| 中 | SKriptBridge.java - `action` 引数のnullチェック | `handleCall` でactionのnullチェック追加 |
| 中 | RPGPlugin.java - `instance` 設定タイミング | 成功時のみ設定するよう変更 |

## 各ファイルの詳細

### 1. SKriptBridge.java
- ✅ `handleCall` 例外処理あり
- ✅ Player nullチェックあり
- ✅ 数値パース例外処理あり
- ⚠️ `action` 引数のnullチェック推奨

### 2. DenizenBridge.java
- ✅ `parseStat()` 結果のnullチェックあり
- ⚠️ **Player引数のnullチェック欠如（重要）**
- ⚠️ `api`フィールドのnull保証なし

### 3. SKriptSkillEvent.java
- ✅ フィールド定義適切
- ⚠️ **コンストラクタの必須パラメータ検証なし（重要）**

### 4. RPGPlugin.java
- ✅ `onEnable`/`onDisable` 例外処理あり
- ✅ システムマネージャnullチェックあり
- ⚠️ `instance` 設定タイミング改善推奨

### 5. DependencyManager.java
- ✅ 依存関係チェック適切
- ✅ `cleanup` でnullチェックあり
- ✅ 例外処理適切

## 結論
全体として例外処理は適切に実装されているが、外部API（Denizen）とイベントクラスでnull安全性強化が推奨される。
