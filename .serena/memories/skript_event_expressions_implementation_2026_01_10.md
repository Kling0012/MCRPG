# Skript イベント式実装レポート

## 日付
2026-01-10

## 作業ブランチ
`vk/ab46-skript-main-even`

## 実装内容

### 新規ファイル作成

#### 1. ExprEventPlayer.java
- **パス**: `src/main/java/com/example/rpgplugin/api/skript/expressions/ExprEventPlayer.java`
- **構文**: `event-player`, `event-caster`
- **戻り値**: `Player`
- **説明**: `on rpg skill cast:` イベント内でスキル発動プレイヤーを取得

#### 2. ExprEventSkill.java
- **パス**: `src/main/java/com/example/rpgplugin/api/skript/expressions/ExprEventSkill.java`
- **構文**: `event-skill`, `skill`
- **戻り値**: `Skill` (skript-reflect用)
- **説明**: スキルオブジェクトを取得

#### 3. ExprEventSkillId.java
- **パス**: `src/main/java/com/example/rpgplugin/api/skript/expressions/ExprEventSkillId.java`
- **構文**: `event-skill-id`, `skill-id`
- **戻り値**: `String`
- **説明**: スキルIDを取得

#### 4. ExprEventSkillLevel.java
- **パス**: `src/main/java/com/example/rpgplugin/api/skript/expressions/ExprEventSkillLevel.java`
- **構文**: `event-skill-level`, `skill-level`
- **戻り値**: `Number` (Integer)
- **説明**: スキルレベルを取得

#### 5. ExprEventTarget.java
- **パス**: `src/main/java/com/example/rpgplugin/api/skript/expressions/ExprEventTarget.java`
- **構文**: `event-target`, `skill-target`
- **戻り値**: `Entity`
- **説明**: ターゲットエンティティを取得

#### 6. ExprEventDamage.java
- **パス**: `src/main/java/com/example/rpgplugin/api/skript/expressions/ExprEventDamage.java`
- **構文**: `event-damage`, `skill-damage`
- **戻り値**: `Number` (Double)
- **説明**: ダメージ値を取得（現在は未実装、将来対応）

### 既存ファイル修正

#### RPGSkriptAddon.java
- **パス**: `src/main/java/com/example/rpgplugin/api/skript/RPGSkriptAddon.java`
- **変更内容**:
  - `registerExpressions()` メソッドに6つのイベント式を追加
  - クラスコメントの機能リストを更新
  - 使用例にイベント式のサンプルを追加

## Skript使用例

```skript
on rpg skill cast:
    send "スキル発動！" to event-player
    send "スキルID: %event-skill-id%" to event-player
    send "スキルレベル: %event-skill-level%" to event-player
    
    if event-target is set:
        send "ターゲット: %event-target%" to event-player
    
    # skript-reflect でスキルオブジェクトを使用
    set {_skill} to event-skill
    send "スキル名: %{_skill}.getName()%" to event-player

# 特定スキルの条件分岐
on rpg skill cast:
    if event-skill-id is "heal":
        heal event-player by 5
    else if event-skill-id is "fireball":
        play sound "ENTITY_GENERIC_EXPLODE" with volume 1 to event-player
```

## 技術詳細

### SimpleExpressionパターン
- すべてのイベント式は `SimpleExpression<T>` を継承
- `ExpressionType.SIMPLE` で登録（引数なし）
- `ScriptLoader.isCurrentEvent()` でイベントコンテキストを検証

### SKriptSkillEvent統合
- 既存の `com.example.rpgplugin.api.bridge.SKriptSkillEvent` から値を取得
- 各フィールドのgetterメソッドを呼び出して値を返却

### 将来の拡張
- `event-damage` の実装には、`SKriptSkillEvent` へのダメージフィールド追加か、
  別途ダメージイベントクラスの作成が必要

## 関連ファイル
- `src/main/java/com/example/rpgplugin/api/bridge/SKriptSkillEvent.java` - 元イベントクラス
- `src/main/java/com/example/rpgplugin/api/skript/events/EvtRPGSkillCast.java` - Skriptイベント定義
