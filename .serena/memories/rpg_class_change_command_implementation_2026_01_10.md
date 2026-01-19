# 職業変更コマンド実装記録

## 実装日
2026-01-10

## 作業ブランチ
vk/6bd2-command

## 実装内容

### コマンド仕様

1. **`/rpg class change <classId> [level]`** - 自分の職業を変更
   - 権限: `rpg.admin.class.change`
   - オプションでレベルを指定可能（デフォルト: 0）
   - 条件チェックなし（即時変更）

2. **`/rpg class change <player> <classId> [level]`** - 他プレイヤーの職業を変更（OP）
   - 権限: `rpg.admin.class.change`
   - 管理者向けコマンド

### 機能要件

- ✅ 条件チェック機構を削除（レベル、ステータス要件等を廃止）
- ✅ 即時変更可能
- ✅ 変更時にレベルを0にリセット（または指定したレベル）
- ✅ 職業固有のステータスボーナス再計算（TODO: StatManagerでの実装）
- ✅ スキルはYamlファイルに従う

### 変更ファイル

1. **ClassManager.java**
   - `changeClass(Player, String, int)` メソッド追加
   - `changeClass(Player, String)` メソッド追加（レベル0用）

2. **RPGPluginAPI.java**
   - `changeClass(Player, String)` メソッド追加
   - `changeClass(Player, String, int)` メソッド追加

3. **RPGPluginAPIImpl.java**
   - `changeClass` メソッド実装追加

4. **RPGCommand.java**
   - `handleClassCommand` を修正して `change` サブコマンドを追加
   - `handleClassChangeCommand` メソッド追加（自分用）
   - `handleAdminClassChangeCommand` メソッド追加（他プレイヤー用）
   - `onCommand` を修正して管理者コマンドに対応

5. **plugin.yml**
   - `rpg.admin.class.change` 権限追加

6. **EffSetRPGClass.java**
   - level引数オプション追加
   - 新しい構文パターン追加（`at level %number%`）

### Skript構文

```skript
# レベル0でクラス変更
set rpg class of player to "warrior"

# レベル指定でクラス変更
set rpg class of player to "mage" at level 10

# 他の構文もサポート
make player an rpg class "warrior" at level 5
change player's rpg class to "rogue" at level 15
```

### 権限

- `rpg.admin.class.change` - クラス変更権限（デフォルト: OPのみ）

## TODO

- StatManagerで職業固有のステータスボーナスを再計算する処理を実装
