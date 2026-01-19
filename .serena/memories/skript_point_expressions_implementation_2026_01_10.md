# Skript ポイント式実装レポート

## 日付
2026-01-10

## 作業ブランチ
`vk/5bc0-skript-rpg-skill`

## 実装内容

### 新規Expressions

#### 1. ExprRPGSkillPoint.java
- **パス**: `src/main/java/com/example/rpgplugin/api/skript/expressions/ExprRPGSkillPoint.java`
- **構文**: `rpg skill point of %player%`
- **戻り値**: `Number` (Integer)
- **Changer対応**: SET, ADD, REMOVE
- **説明**: スキルポイントの取得・変更

#### 2. ExprRPGAttrPoint.java
- **パス**: `src/main/java/com/example/rpgplugin/api/skript/expressions/ExprRPGAttrPoint.java`
- **構文**: `rpg attr[ibute] point of %player%`
- **戻り値**: `Number` (Integer)
- **Changer対応**: SET, ADD, REMOVE
- **説明**: 属性ポイント（ステータス配分ポイント）の取得・変更

### API変更

#### RPGPluginAPI.java 追加メソッド
```java
int getSkillPoints(Player player);
void setSkillPoints(Player player, int points);
int getAttrPoints(Player player);
void setAttrPoints(Player player, int points);
```

#### RPGPluginAPIImpl.java 追加実装
- `getSkillPoints()`: SkillManager.PlayerSkillDataから取得
- `setSkillPoints()`: SkillManager.PlayerSkillDataに設定
- `getAttrPoints()`: RPGPlayer.getAvailablePoints()から取得
- `setAttrPoints()`: RPGPlayer.setAvailablePoints()で設定

### 既存ファイル修正

#### RPGSkriptAddon.java
- `registerExpressions()` に2つのポイント式を追加
- クラスコメントの機能リストを更新
- 使用例にポイント操作のサンプルを追加

## Skript使用例

```skript
# 取得
set {_skillPoints} to rpg skill point of player
set {_attrPoints} to rpg attr point of player
send "スキルポイント: %{_skillPoints}%" to player
send "属性ポイント: %{_attrPoints}%" to player

# 追加
add 1 to rpg skill point of player
add 5 to rpg attr point of player

# 設定
set rpg skill point of player to 10
set rpg attr point of player to 5

# 削除
remove 1 from rpg skill point of player
remove 2 from rpg attr point of player

# 条件判定
if rpg skill point of player > 0:
    send "スキルポイントがあります！" to player

if rpg attr point of player >= 5:
    send "ステータスを振れます！" to player
```

## 技術詳細

### Changerパターン実装
- `acceptChange()`: SET, ADD, REMOVEモードを許可
- `change()`: 各モードに応じてAPIメソッドを呼び出し
- 値が負にならないよう `Math.max(0, ...)` で制御

### SimpleExpressionパターン
- 既存のExprRPGLevelと同様の構造
- ExpressionType.SIMPLEで登録（引数はPlayerのみ）
- RPGPlugin経由でAPIにアクセス

## 関連ファイル
- `src/main/java/com/example/rpgplugin/api/RPGPluginAPI.java` - APIインターフェース
- `src/main/java/com/example/rpgplugin/api/RPGPluginAPIImpl.java` - API実装
- `src/main/java/com/example/rpgplugin/api/skript/RPGSkriptAddon.java` - Skript登録
- `src/main/java/com/example/rpgplugin/player/RPGPlayer.java` - 属性ポイントデータソース
- `src/main/java/com/example/rpgplugin/skill/SkillManager.java` - スキルポイントデータソース
