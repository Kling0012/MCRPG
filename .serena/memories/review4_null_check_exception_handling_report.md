# レビュー4: Nullチェック/例外処理 レビュー報告

**日付:** 2026-01-09
**ブランチ:** vk/12e7-4-skill-evaluato
**対象:** Skill Evaluator + MythicMobs Hook (8ファイル)

## 対象ファイル
1. `src/main/java/com/example/rpgplugin/skill/evaluator/ExpressionParser.java`
2. `src/main/java/com/example/rpgplugin/skill/evaluator/FormulaEvaluator.java`
3. `src/main/java/com/example/rpgplugin/skill/evaluator/FormulaDamageCalculator.java`
4. `src/main/java/com/example/rpgplugin/core/dependency/MythicMobsHook.java`
5. `src/main/java/com/example/rpgplugin/core/dependency/PlaceholderHook.java`
6. `src/main/java/com/example/rpgplugin/mythicmobs/MythicMobsManager.java`
7. `src/main/java/com/example/rpgplugin/mythicmobs/config/MobDropConfig.java`
8. `src/main/java/com/example/rpgplugin/mythicmobs/drop/DropRepository.java`

---

## ✅ 良好な実装

### ExpressionParser.java
- **独自例外クラス**: `FormulaEvaluationException` を定義し、適切なエラーメッセージと位置情報を提供
- **ゼロ除算チェック**: `parseMultiplicative()` で除数・剰余のゼロチェックを実行
- **未定義変数チェック**: `parsePrimary()` で変数の未定義チェック
- **数値パース例外処理**: `Double.parseDouble()` を try-catch でラップ
- **EOF検証**: 式評価後にトークンが残っていないかチェック

### FormulaEvaluator.java
- **Null/Emptyチェック**: `evaluate()` メソッドで数式のnull・空文字チェック
- **Safe評価メソッド**: `evaluateSafe()` で例外をキャッチしデフォルト値を返す
- **検証メソッド**: `validate()` でダミーコンテキストを使用した構文検証
- **例外チェーン**: `ExpressionParser.FormulaEvaluationException` をラップして再スロー

### FormulaDamageCalculator.java
- **例外ハンドリング**: `calculateDamage()` で `FormulaEvaluationException` をキャッチしログ出力
- **数式未定義チェック**: 数式がnullまたは空の場合にログ出力して0.0を返却

### MythicMobsHook.java
- **可用性チェック**: `isAvailable()` で `available`、`mythicBukkit`、`mythicPlugin` の3重チェック
- **Optional使用**: `getActiveMob()` などで `Optional<ActiveMob>` を使用
- **例外ハンドリング**: `setup()` で `NoClassDefFoundError` と汎用 `Exception` をキャッチ
- **null安全なアクセス**: 各メソッドで `isAvailable()` チェック後にAPI呼び出し

### PlaceholderHook.java
- **Nullチェック**: `onRequest()` で `offlinePlayer` と `hasPlayedBefore()` をチェック
- **RPGPlayer nullチェック**: `rpgPlayer` がnullの場合は空文字を返却
- **BukkitPlayer nullチェック**: `bukkitPlayer` がnullの場合に適切なデフォルト値を返却
- **例外処理**: `Stat.fromShortName()` の `IllegalArgumentException` をキャッチ

### MythicMobsManager.java
- **Null安全な委譲**: 全メソッドで `mythicMobsHook` のメソッドに委譲する際、nullチェックは委譲先に任せる
- **初期化チェック**: `initialize()` で `mythicMobsHook.isAvailable()` を確認
- **Nullチェック**: `handleMobDeath()` で `mobId` がnullの場合に早期リターン

### MobDropConfig.java
- **Nullチェック**: `loadFromConfig()` で `section` と `mobsSection` のnullチェック
- **例外ハンドリング**: `loadFromConfig()` で各モブ設定の読み込みをtry-catchで囲み、失敗時にログ出力して継続
- **Material検証**: `Material.matchMaterial()` がnullを返す場合にログ出力してスキップ

### DropRepository.java
- **SQL例外**: 全メソッドで `throws SQLException` を宣言
- **try-with-resources**: PreparedStatement/ResultSet を適切にクローズ
- **ResultSet Null処理**: `mapResultSetToDrop()` で `rs.wasNull()` を使用して `expires_at` のnullを判定

---

## ⚠️ 問題点

### 1. MythicMobsHook.java - isAvailable() メソッドの論理問題

**場所:** `src/main/java/com/example/rpgplugin/core/dependency/MythicMobsHook.java:85-92`

```java
public boolean isAvailable() {
    return available && mythicBukkit != null && mythicPlugin != null;
}
```

**問題:** 
- `setup()` メソッドで `mythicPlugin` が設定されていない（nullのまま）
- そのため `isAvailable()` は常にfalseを返す

**影響:** 
- MythicMobs機能が正しく動作しない可能性

**推奨修正:**
```java
public boolean isAvailable() {
    return available && mythicBukkit != null;  // mythicPluginは使用していないため削除
}
```

または `setup()` メソッドで `mythicPlugin` を適切に設定する。

### 2. MobDropConfig.java - materialName のNullチェック不足

**場所:** `src/main/java/com/example/rpgplugin/mythicmobs/config/MobDropConfig.java:254`

```java
String materialName = dropSection.getString("item");
// ...
Material material = Material.matchMaterial(materialName);
if (material == null) {
    logger.warning("Invalid material: " + materialName);
    continue;
}
```

**問題:**
- `getString()` がnullを返す可能性がある
- `Material.matchMaterial(null)` はnullを返すが、ログメッセージが"Invalid material: null"となる

**推奨修正:**
```java
String materialName = dropSection.getString("item");
if (materialName == null || materialName.isEmpty()) {
    logger.warning("Missing material name in drop config");
    continue;
}
Material material = Material.matchMaterial(materialName);
if (material == null) {
    logger.warning("Invalid material: " + materialName);
    continue;
}
```

### 3. DropRepository.java - mapResultSetToDrop() でのUUID例外処理不足

**場所:** `src/main/java/com/example/rpgplugin/mythicmobs/drop/DropRepository.java:302-312`

```java
private DropData mapResultSetToDrop(ResultSet rs) throws SQLException {
    DropData drop = new DropData();
    drop.setId(rs.getInt("id"));
    drop.setPlayerUuid(UUID.fromString(rs.getString("player_uuid")));  // IllegalArgumentExceptionの可能性
    // ...
}
```

**問題:**
- `UUID.fromString()` が `IllegalArgumentException` をスローする可能性がある
- データベース内の無効なUUID文字列により実行時例外が発生

**推奨修正:**
```java
private DropData mapResultSetToDrop(ResultSet rs) throws SQLException {
    DropData drop = new DropData();
    drop.setId(rs.getInt("id"));
    
    String uuidStr = rs.getString("player_uuid");
    try {
        drop.setPlayerUuid(UUID.fromString(uuidStr));
    } catch (IllegalArgumentException e) {
        throw new SQLException("Invalid UUID format in database: " + uuidStr, e);
    }
    // ...
}
```

### 4. PlaceholderHook.java - getAuthor() での潜在的なNull

**場所:** `src/main/java/com/example/rpgplugin/core/dependency/PlaceholderHook.java:51-55`

```java
@Override
@NotNull
public String getAuthor() {
    return String.join(", ", plugin.getDescription().getAuthors());
}
```

**問題:**
- `plugin.getDescription().getAuthors()` が空リストまたはnullを返す可能性
- 空リストの場合は空文字が返る（@NotNullには違反しない）
- `getDescription()` がnullを返す可能性は低いが、考慮が必要

**推奨修正:**
```java
@Override
@NotNull
public String getAuthor() {
    List<String> authors = plugin.getDescription().getAuthors();
    return authors == null || authors.isEmpty() ? "Unknown" : String.join(", ", authors);
}
```

---

## 📊 まとめ

| ファイル | 状態 | 問題数 |
|---------|------|--------|
| ExpressionParser.java | ✅ 良好 | 0 |
| FormulaEvaluator.java | ✅ 良好 | 0 |
| FormulaDamageCalculator.java | ✅ 良好 | 0 |
| MythicMobsHook.java | ⚠️ 要修正 | 1 |
| PlaceholderHook.java | ⚠️ 軽微 | 1 |
| MythicMobsManager.java | ✅ 良好 | 0 |
| MobDropConfig.java | ⚠️ 要修正 | 1 |
| DropRepository.java | ⚠️ 要修正 | 1 |

**全体的な評価:** B+
- 例外処理とNullチェックの大部分が適切に実装されている
- しかし `MythicMobsHook.isAvailable()` のロジック問題は機能に影響する可能性がある
- `DropRepository.mapResultSetToDrop()` のUUID例外処理はデータ整合性问题を引き起こす可能性がある
