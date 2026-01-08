# Phase13: MP/HPシステムのGUI統合 実装完了

## 実装日
2026-01-08

## 目的
ActiveSkillExecutorのTODOを解消し、ManaManagerとの完全な連携を実現する。

## 実装内容

### 1. config.ymlにメッセージ設定を追加
`src/main/resources/config.yml`に以下の設定を追加：

```yaml
skills:
  messages:
    show_cast_success: true
    show_mana_consume: true
    show_hp_consume: true
    show_mana_insufficient: true
    show_hp_insufficient: true
```

### 2. ActiveSkillExecutor.execute() メソッド修正
- TODOコメントを削除し、MP消費チェックを有効化
- `RPGPlayer.consumeSkillCost()`経由でMP/HP消費を統合
- 設定に応じたメッセージ表示を実装

### 3. ActiveSkillExecutor.executeWithCostType() メソッド修正
- MANAケースのTODOを削除し実装
- `RPGPlayer.hasMana()`でチェック
- `RPGPlayer.consumeMana()`で消費
- 設定に応じたメッセージ表示を実装

### 4. ヘルパーメソッド追加
- `isShowMessage(String key)`: config.ymlからメッセージ表示設定を取得

## 関連ファイル
- `src/main/resources/config.yml` - メッセージ設定追加
- `src/main/java/com/example/rpgplugin/skill/executor/ActiveSkillExecutor.java` - メイン実装

## 既存連携
- `RPGPlayer.consumeMana(int)` - MP消費
- `RPGPlayer.hasMana(int)` - MPチェック
- `RPGPlayer.consumeSkillCost(int)` - コストタイプに応じた消費
- `RPGPlayer.isManaCostType()` - 現在のコストタイプ判定

## 受入条件達成状況
- [x] MP消費チェックが正常に動作
- [x] MP不足時にスキル発動が失敗
- [x] MP消費時にプレイヤーに通知（設定可能）
- [x] HP消費モードでも正常に動作
- [x] 既存テストが全件パス

## 設定項目
| キー | デフォルト | 説明 |
|------|------------|------|
| skills.messages.show_cast_success | true | スキル発動成功メッセージ |
| skills.messages.show_mana_consume | true | MP消費メッセージ |
| skills.messages.show_hp_consume | true | HP消費メッセージ |
| skills.messages.show_mana_insufficient | true | MP不足メッセージ |
| skills.messages.show_hp_insufficient | true | HP不足メッセージ |

## 解消されたTODO
- `ActiveSkillExecutor.java:60` - MP消費チェックの有効化
- `ActiveSkillExecutor.java:270` - MANAコスト消費処理の実装
- `ActiveSkillExecutor.java:295` - マナシステム実装後の有効化
