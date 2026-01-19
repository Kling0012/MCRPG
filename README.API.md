# Phase9: 外部API実装（SKript/Denizen/テンプレート）

## 概要

Phase9では、SKriptやDenizenなどの外部プラグインからアクセス可能なAPIを実装し、クラス・スキル作成用のテンプレートを提供します。

## 実装内容

### 1. メインAPI

#### `RPGPluginAPI.java` (インターフェース)
- **場所**: `src/main/java/com/example/rpgplugin/api/RPGPluginAPI.java`
- **機能**:
  - プレイヤーデータ取得（レベル、ステータス、クラス）
  - ステータス操作（設定、ポイント追加）
  - クラス操作（設定、ランクアップ、判定）
  - スキル操作（習得確認、習得、使用、レベル取得）
  - 経済操作（残高取得、入金、出金、転送）
  - ダメージ計算

#### `RPGPluginAPIImpl.java` (実装)
- **場所**: `src/main/java/com/example/rpgplugin/api/RPGPluginAPIImpl.java`
- **機能**: RPGPluginAPIインターフェースの実装

### 2. SKriptブリッジ

#### `SKriptBridge.java`
- **場所**: `src/main/java/com/example/rpgplugin/api/bridge/SKriptBridge.java`
- **機能**: SKriptからAPIへのアクセスを提供
- **使用方法**:
  ```skript
  execute player command "rpg api get_level %player%"
  execute player command "rpg api give_gold %player% 100"
  ```

### 3. Denizenブリッジ

#### `DenizenBridge.java`
- **場所**: `src/main/java/com/example/rpgplugin/api/bridge/DenizenBridge.java`
- **機能**: DenizenからAPIへのアクセスを提供
- **使用方法**:
  ```denizen
  # タグ置換
  define level <player.tag[rpg.level]>
  narrate "あなたのLV: %level%"

  # コマンド実行
  execute as_server "rpg api give_gold %player% 100"
  ```

### 4. APIコマンド

#### `APICommand.java`
- **場所**: `src/main/java/com/example/rpgplugin/api/command/APICommand.java`
- **機能**: コマンドラインからAPIにアクセス
- **使用方法**:
  ```bash
  /rpg api help
  /rpg api get_level Steve
  /rpg api give_gold Steve 100
  ```

### 5. クラステンプレート

#### テンプレートファイル
- **場所**: `src/main/resources/templates/classes/`
- **ファイル**:
  - `melee_template.yml` - 近接クラス（STR/VIT成長）
  - `ranged_template.yml` - 遠距離クラス（DEX/INT成長）
  - `magic_template.yml` - 魔法クラス（INT/SPI成長）
  - `tank_template.yml` - タンククラス（VIT/STR成長）

#### 使用方法
1. テンプレートファイルをコピーして `classes/` ディレクトリに配置
2. ID、名前、ステータス成長などをカスタマイズ
3. `/rpg reload` でリロード

### 6. スキルテンプレート

#### テンプレートファイル
- **場所**: `src/main/resources/templates/skills/`
- **ファイル**:
  - `active_skill_template.yml` - アクティブスキル
  - `passive_skill_template.yml` - パッシブスキル

#### 使用方法
1. テンプレートファイルをコピーして `skills/active/` または `skills/passive/` に配置
2. スキルID、効果、コストなどをカスタマイズ
3. `/rpg reload` でリロード

### 7. サンプルスクリプト

#### SKriptサンプル
- **場所**: `examples/skript/rpg_api_examples.sk`
- **内容**:
  - 基本的なAPI使用例
  - クエスト報酬システム
  - ショップシステム連携
  - PvP報酬システム
  - レベルアップボーナス

#### Denizenサンプル
- **場所**: `examples/denizen/rpg_api_examples.dsc`
- **内容**:
  - タグ置換の使用例
  - イベント連携
  - クエスト報酬システム
  - ショップシステム連携
  - PvP報酬システム
  - レベルアップボーナス

### 8. ドキュメント

#### APIドキュメント
- **場所**: `docs/API_DOCUMENTATION.md`
- **内容**:
  - セットアップ方法
  - SKript連携方法
  - Denizen連携方法
  - Java API使用方法
  - テンプレート使用方法
  - 使用例
  - トラブルシューティング

## 成果物

✅ **外部プラグインからアクセス可能なAPI**
- RPGPluginAPIインターフェース
- SKriptブリッジ
- Denizenブリッジ
- APIコマンド

✅ **SKriptサンプルスクリプト**
- 基本的なAPI使用例
- 実用的なシステム連携例

✅ **Denizenサンプルスクリプト**
- タグ置換使用例
- イベント連携例

✅ **クラス・スキル作成テンプレート**
- 4種類のクラステンプレート
- 2種類のスキルテンプレート

✅ **完全なドキュメント**
- API使用方法
- トラブルシューティング

## 使用例

### SKriptから使用

```skript
command /testrpg:
    trigger:
        # レベル取得
        execute player command "rpg api get_level %player%"

        # ゴールド付与
        execute player command "rpg api give_gold %player% 100"

        # スキル使用
        execute player command "rpg api cast_skill %player% fireball"
```

### Denizenから使用

```denizen
# タグ置換
define level <player.tag[rpg.level]>
narrate "あなたのLV: %level%"

# コマンド実行
execute as_server "rpg api give_gold %player% 100"
```

### Javaから使用

```java
RPGPlugin rpgPlugin = (RPGPlugin) Bukkit.getPluginManager().getPlugin("RPGPlugin");
RPGPluginAPI api = rpgPlugin.getAPI();

// レベル取得
int level = api.getLevel(player);

// ゴールド付与
api.depositGold(player, 100.0);

// スキル使用
api.castSkill(player, "fireball");
```

## テンプレートの使用

### クラステンプレート

1. `src/main/resources/templates/classes/melee_template.yml` をコピー
2. `classes/warrior_custom.yml` として保存
3. 内容を編集:
   ```yaml
   id: warrior_custom
   name: カスタム戦士
   display_name: "&cカスタム戦士"
   stat_growth:
     auto:
       strength: 3  # STR成長を増加
       vitality: 2  # VIT成長を増加
   ```
4. `/rpg reload` でリロード

### スキルテンプレート

1. `src/main/resources/templates/skills/active_skill_template.yml` をコピー
2. `skills/active/custom_skill.yml` として保存
3. 内容を編集:
   ```yaml
   name: "custom_skill"
   display_name: "&eカスタムスキル"
   damage_formula: "BASE_DAMAGE + (LEVEL * 10) + (STRENGTH * 3.0)"
   base_damage: 50
   ```
4. `/rpg reload` でリロード

## 今後の拡張可能性

- プレースホルダーAPI連携（PlaceholderAPI）
- REST API提供（外部アプリケーション連携）
- WebSocket連携（リアルタイム更新）
- データベース直接アクセスAPI
- より多くのSKript/Denizenタグ

## 関連ドキュメント

- [APIドキュメント](docs/API_DOCUMENTATION.md)
- [SKriptサンプル](examples/skript/rpg_api_examples.sk)
- [Denizenサンプル](examples/denizen/rpg_api_examples.dsc)

## ライセンス

MIT License
