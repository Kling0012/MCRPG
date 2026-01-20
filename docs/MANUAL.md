# MCRPG プラグイン マニュアル

## 目次

1. [インストール](#1-インストール)
2. [初期設定](#2-初期設定)
3. [プレイヤーコマンド](#3-プレイヤーコマンド)
4. [管理コマンド](#4-管理コマンド)
5. [クラス作成](#5-クラス作成)
6. [スキル作成](#6-スキル作成)
7. [Skript連携](#7-skript連携)
8. [PlaceholderAPI連携](#8-placeholderapi連携)

---

## 1. インストール

### 1.1 動作環境

| 項目 | 必要仕様 |
|------|----------|
| Minecraftバージョン | 1.20.6 以上 |
| Javaバージョン | 17 以上 |
| 必須プラグイン | なし |
| 任意プラグイン | PlaceholderAPI, Skript |

### 1.2 インストール手順

1. `MCRPG-{version}.jar` を `plugins/` ディレクトリに配置
2. サーバーを起動
3. `plugins/MCRPG/` ディレクトリが自動生成される
4. サーバーを停止

### 1.3 依存プラグインの導入

#### PlaceholderAPI 連携

1. PlaceholderAPI をインストール
2. サーバーを起動
3. `/papi ecloud download MCRPG` を実行
4. `/papi reload` を実行

#### Skript 連携

1. Skript をインストール
2. サーバーを起動
3. `plugins/MCRPG/skript/` 以下のスクリプトが自動読み込み

---

## 2. 初期設定

### 2.1 基本設定ファイル

```
plugins/MCRPG/
├── config.yml              # メイン設定
├── damage_config.yml       # ダメージ計算設定
├── classes/                # クラス定義ディレクトリ
│   ├── warrior.yml
│   ├── mage.yml
│   └── ...
└── skills/                 # スキル定義ディレクトリ
    ├── active/
    │   ├── power_strike.yml
    │   └── ...
    └── passive/
        ├── critical_mastery.yml
        └── ...
```

### 2.2 データベース設定

**config.yml の `database` セクション**

```yaml
database:
  path: "data/database.db"      # データベースファイルのパス
  pool_size: 10                 # コネクションプールサイズ
  async_save: true              # 非同期保存を有効化
  auto_save_interval: 300       # 自動保存間隔（秒）
```

### 2.3 キャッシュ設定

**config.yml の `cache` セクション**

| サイズ | 推奨値 |
|--------|--------|
| 50人サーバー | l2_max_size: 500-1000 |
| 150人サーバー | l2_max_size: 1500-2250 |

```yaml
cache:
  l2_max_size: 2000            # L2キャッシュの最大サイズ
  l2_ttl_minutes: 10           # キャッシュのTTL（分）
  expire_after_access: true    # アクセス時間ベースのTTL
```

### 2.4 経験値減衰設定

```yaml
exp_diminish:
  enabled: true                # 減衰システムを有効化
  start_level: 30              # 減衰開始レベル
  reduction_rate: 0.5          # 軽減率（0.0-1.0）
  exemptions:
    player_kills: true         # プレイヤーキルは減衰対象外
    boss_mobs: true            # ボスモブは減衰対象外
    event_rewards: true        # イベント報酬は減衰対象外
```

### 2.5 ホットリロード設定

```yaml
hot_reload:
  classes: true        # クラス定義のホットリロード
  skills: true         # スキル定義のホットリロード
  exp_diminish: true   # 減衰設定のホットリロード
  mobs: true           # モブドロップ設定のホットリロード
  templates: false     # テンプレートファイルのホットリロード
```

---

## 3. プレイヤーコマンド

### 3.1 基本コマンド

| コマンド | 説明 |
|----------|------|
| `/rpg` | メニューやGUIを開く |
| `/rpg class` | クラス選択GUIを開く |
| `/rpg stats` | ステータス確認 |
| `/rpg skills` | スキルツリーGUIを開く |
| `/rpg info` | 現在のクラス情報を表示 |

### 3.2 スキルコマンド

| コマンド | 説明 |
|----------|------|
| `/rpg cast <スキルID>` | スキルを発動 |
| `/rpg unlock <スキルID>` | スキルをアンロック |
| `/rpg skills` | スキルツリーを開く |

### 3.3 クラスコマンド

| コマンド | 説明 |
|----------|------|
| `/rpg class [クラスID]` | クラスを選択/変更 |
| `/rpg profession` | 職業を選択 |
| `/rpg info` | 現在のクラス情報を表示 |

### 3.4 ステータスコマンド

| コマンド | 説明 |
|----------|------|
| `/rpg stats` | ステータスを表示 |
| `/rpg stat <ステータス> <値>` | ステータスポイントを振る |

---

## 4. 管理コマンド

### 4.1 基本管理コマンド

| コマンド | 説明 | 権限 |
|----------|------|------|
| `/rpgadmin reload` | 設定をリロード | `rpgplugin.admin` |
| `/rpgadmin save` | データを強制保存 | `rpgplugin.admin` |
| `/rpgadmin reset <プレイヤー>` | プレイヤーデータをリセット | `rpgplugin.admin` |
| `/rpgadmin giveexp <プレイヤー> <量>` | 経験値を付与 | `rpgplugin.admin` |
| `/rpgadmin setlevel <プレイヤー> <レベル>` | レベルを設定 | `rpgplugin.admin` |

### 4.2 デバッグコマンド

| コマンド | 説明 |
|----------|------|
| `/rpgadmin debug <プレイヤー>` | プレイヤーのデバッグ情報を表示 |
| `/rpgadmin cache stats` | キャッシュ統計を表示 |
| `/rpgadmin cache clear` | キャッシュをクリア |

---

## 5. クラス作成

### 5.1 クラス定義ファイルの作成

1. `plugins/MCRPG/classes/` ディレクトリに `<class_id>.yml` を作成
2. 以下の基本構造を記述

### 5.2 クラス基本設定

```yaml
# 基本情報
id: warrior                      # クラスID（英数字小文字）
name: 戦士                       # クラス名
display_name: "&c戦士"           # 表示名（カラーコード使用可）
prefix: "&c[戦士]"               # プレフィックス
description:
  - "&f剣と盾で前線を守る近接クラス"
  - "&eSTRとVITが成長しやすい"

# クラス設定
group: class                     # クラスグループ
max_level: 50                    # 最大レベル
icon: IRON_SWORD                 # アイコン
```

### 5.3 ステータス設定

```yaml
# HP/MP設定
health:
  base: 100                      # レベル1の時のHP
  scale: 10                      # レベル毎のHP増加量

mana:
  base: 50                       # レベル1の時のMP
  scale: 5                       # レベル毎のMP増加量

# ステータス成長
stat_growth:
  auto:
    strength: 2                  # レベルアップ時の自動上昇
    vitality: 1
  manual_points: 3               # 手動振り分けポイント
```

### 5.4 パッシブボーナス設定

```yaml
passive_bonuses:
  - type: physical_damage        # ボーナスタイプ
    formula: "10 + Lv * 2"       # 計算式（Lv:レベル変数）
  - type: defense
    formula: "5 + Lv"
```

**使用可能なボーナスタイプ**

| タイプ | 説明 |
|--------|------|
| physical_damage | 物理ダメージ |
| magical_damage | 魔法ダメージ |
| defense | 防御力 |
| max_health | 最大HP |
| max_mana | 最大MP |

### 5.5 経験値設定

```yaml
# 経験値獲得方法（ビットフィールド）
exp_source: 43                   # MOB(1) + BLOCK_BREAK(2) + CRAFT(8) + QUEST(32)

# 経験値減衰
exp_diminish:
  start_level: 30                # 減衰開始レベル
  reduction_rate: 0.5            # 軽減率
```

**exp_source の値**

| 値 | 説明 |
|----|------|
| 1 | MOB討伐 |
| 2 | ブロック破壊 |
| 4 | 採掘 |
| 8 | クラフト |
| 16 | アイテム使用 |
| 32 | クエスト完了 |

### 5.6 スキル紐付け

```yaml
available_skills:
  - power_strike                 # スキルID
  - shield_bash
  - battle_cry
  - iron_skin
  - execute
```

### 5.7 クラス変更要件

```yaml
requirements:
  - type: level                  # 要件タイプ
    value: 10                    # 必要レベル
  - type: stat                   # ステータス要件
    stat: strength               # 対象ステータス
    value: 20                    # 必要値
  - type: item                   # アイテム要件
    material: DIAMOND_SWORD      # アイテム種類
    amount: 1                    # 必要数
    consume: true                # 消費するか
  - type: class                  # クラス要件
    value: warrior               # 必要クラス
  - type: quest                  # クエスト要件
    quest_id: "warrior_trial"    # クエストID
```

---

## 6. スキル作成

### 6.1 スキル定義ファイルの作成

1. `plugins/MCRPG/skills/active/` または `passive/` に `<skill_id>.yml` を作成
2. 以下の基本構造を記述

### 6.2 スキル基本設定

```yaml
# 基本情報
id: power_strike                 # スキルID
name: パワーストライク           # スキル名
type: normal                     # スキルタイプ
display_name: "&cパワーストライク"
description:
  - "&f強力な一撃を放つ"
  - "&e敵にノックバックを与える"

# レベル設定
max_level: 5                     # 最大レベル
icon_material: DIAMOND_SWORD     # アイコン
```

**スキルタイプ**

| タイプ | 説明 |
|--------|------|
| normal | 通常スキル |
| toggle | スイッチ式スキル |
| passive | パッシブスキル |
| channeling | チャネリングスキル |

### 6.3 コスト設定

```yaml
cost:
  type: mana                     # コストタイプ
  base: 10                       # 基本コスト
  per_level: 1                   # レベル毎の増加量
  min: 5                         # 最小コスト
  max: 30                        # 最大コスト
```

**コストタイプ**

| タイプ | 説明 |
|--------|------|
| mana | MP消費 |
| health | HP消費 |
| stamina | スタミナ消費 |
| hunger | 空腹度消費 |
| item | アイテム消費 |

### 6.4 クールダウン設定

```yaml
cooldown:
  base: 8.0                      # 基本クールダウン（秒）
  per_level: -0.3                # レベル毎の変化量
  min: 5.0                       # 最小クールダウン
```

### 6.5 ダメージ計算設定

```yaml
damage:
  formula: "BASE_DAMAGE + (Lv * 5) + (STR * 1.5)"  # ダメージ計算式
  base: 20.0                     # 基本ダメージ
  stat: STRENGTH                 # 補正ステータス
```

**使用可能な変数**

| 変数 | 説明 |
|------|------|
| BASE_DAMAGE | 基本ダメージ |
| Lv | スキルレベル |
| STR | 力ステータス |
| DEX | 敏捷ステータス |
| INT | 知力ステータス |
| VIT | 生命力ステータス |

### 6.6 ターゲット設定

```yaml
targeting:
  type: nearest_hostile          # ターゲットタイプ
  range: 3.0                     # 範囲
```

**ターゲットタイプ**

| タイプ | 説明 |
|--------|------|
| nearest_hostile | 最も近い敵対的エンティティ |
| nearest_entity | 最も近いエンティティ |
| area | 範囲内の全エンティティ |
| self | 自分自身 |
| single | 単体ターゲット |

### 6.7 スキルツリー設定

```yaml
skill_tree:
  parent: none                    # 親スキルID
  unlock_requirements:
    - type: level                # アンロック条件
      value: 1                   # 必要レベル
    - type: class
      value: warrior             # 必要クラス
    - type: skill                # 親スキル要件
      value: power_strike        # 必要スキルID
      level: 3                   # 必要スキルレベル
  cost: 1                        # アンロックコスト（スキルポイント）
```

### 6.8 特殊効果設定

```yaml
# ノックバック
knockback: "0.5 + Lv * 0.1"      # ノックバック力

# スロウ効果
slow:
  duration: 3.0                  # 効果時間（秒）
  amplifier: 2                   # 効果強度

# 周囲効果
area:
  radius: 5.0                    # 半径
  center_at: target              # 中心位置（target/self）

# DoT（ダメージ-over-time）
dot:
  duration: 5.0                  # 効果時間（秒）
  interval: 1.0                  # ダメージ間隔（秒）
  damage: "Lv * 2"               # ダメージ量
```

---

## 7. Skript連携

### 7.1 Skriptアドオンの有効化

1. Skriptプラグインをインストール
2. `plugins/MCRPG/skript/` に Skriptファイルを配置
3. `/skript reload all` を実行

### 7.2 条件式（Conditions）

| 条件 | 説明 |
|------|------|
| `player has rpg skill "skill_id"` | 指定スキルを持っている |
| `player's rpg class is "class_id"` | 指定クラスである |
| `player's rpg stat %stat% is greater than %number%` | ステータスが指定値以上 |
| `player can upgrade rpg class` | クラスアップ可能 |

### 7.3 エフェクト（Effects）

| エフェクト | 説明 |
|-----------|------|
| `make player cast rpg skill "skill_id"` | スキルを発動 |
| `unlock rpg skill "skill_id" for player` | スキルをアンロック |
| `set player's rpg class to "class_id"` | クラスを設定 |
| `modify player's rpg stat %stat% by %number%` | ステータスを変更 |

### 7.4 イベント（Events）

| イベント | 説明 |
|---------|------|
| `on rpg skill cast` | スキル発動時 |
| `on rpg skill damage` | スキルダメージ発生時 |

### 7.5 式（Expressions）

| 式 | 説明 |
|----|------|
| `[rpg] class of %player%` | プレイヤーのクラス |
| `[rpg] level of %player%` | プレイヤーのレベル |
| `[rpg] stat %stat% of %player%` | 指定ステータスの値 |
| `[rpg] skill level of %skill% for %player%` | スキルレベル |

### 7.6 Skriptサンプル

```skript
on rpg skill cast:
    if skill-id of event is "fireball":
        send "&cファイアボールを発射！" to player
        set {combo::%player%::uuid} to 0

on rpg skill damage:
    if skill-id of event is "power_strike":
        set {_damage} to event-damage
        send "&c%{_damage}%のダメージを与えた！" to player

command /rpgtest:
    trigger:
        if player's rpg class is "warrior":
            send "&cあなたは戦士です"
            send "&eSTR: %player's rpg stat strength%"
        else:
            send "&7戦士ではありません"
```

---

## 8. PlaceholderAPI連携

### 8.1 プレースホルダー一覧

| プレースホルダー | 説明 |
|-----------------|------|
| `%mcrpg_class%` | クラス名 |
| `%mcrpg_level%` | レベル |
| `%mcrpg_exp%` | 現在の経験値 |
| `%mcrpg_exp_needed%` | レベルアップに必要な経験値 |
| `%mcrpg_exp_bar%` | 経験値バー |
| `%mcrpg_stat_strength%` | 力ステータス |
| `%mcrpg_stat_dexterity%` | 敏捷ステータス |
| `%mcrpg_stat_intelligence%` | 知力ステータス |
| `%mcrpg_stat_vitality%` | 生命力ステータス |
| `%mcrpg_stat_points%` | 振り分け可能ポイント |
| `%mcrpg_mana%` | 現在のMP |
| `%mcrpg_mana_max%` | 最大MP |
| `%mcrpg_mana_bar%` | MPバー |
| `%mcrpg_health%` | 現在のHP |
| `%mcrpg_health_max%` | 最大HP |
| `%mcrpg_health_bar%` | HPバー |

### 8.2 Scoreboard連携例

```
/scoreboard objectives add rpgstats dummy
/scoreboard objectives setdisplay sidebar rpgstats
/scoreboard players add "&cクラス" rpgstats
/scoreboard players add "&f%mcrpg_class%" rpgstats
/scoreboard players add "&aレベル" rpgstats
/scoreboard players add "&f%mcrpg_level%" rpgstats
```

### 8.3 Bossbar連携例

```
/bossbar main rpg_info add
/bossbar main rpg_info set color white
/bossbar main rpg_info set style progress
/bossbar main rpg_info set title "&aHP: %mcrpg_health_bar% &b| MP: %mcrpg_mana_bar%"
```

---

## 9. トラブルシューティング

### 9.1 よくある問題

| 問題 | 解決策 |
|------|--------|
| プラグインが読み込まれない | Javaバージョンが17以上であることを確認 |
| スキルが発動しない | `/rpgadmin reload` を実行、config.ymlを確認 |
| データが保存されない | データベースファイルの権限を確認 |
| クラスが選択できない | `classes.enabled: true` を確認 |

### 9.2 デバッグモード

```yaml
debug:
  log_level: "FINE"              # 詳細ログを有効化
  verbose: true                  # さらに詳細なログ
  performance_logging: true      # パフォーマンスログ
  log_exp_changes: true          # 経験値変化のログ
  log_exp_diminish: true         # 経験値減衰のログ
```

### 9.3 キャッシュ関連

| コマンド | 説明 |
|----------|------|
| `/rpgadmin cache stats` | キャッシュ統計を表示 |
| `/rpgadmin cache clear` | キャッシュをクリア |
| `/rpgadmin save` | データを強制保存 |

---

## 10. 設定ファイルリファレンス

### 10.1 config.yml 完全構造

```yaml
database:
  path: "data/database.db"
  pool_size: 10
  async_save: true
  auto_save_interval: 300

cache:
  l2_max_size: 2000
  l2_ttl_minutes: 10
  expire_after_access: true
  stats_logging_interval: 300
  hit_logging: false
  batch_save_size: 50

exp_diminish:
  enabled: true
  start_level: 30
  reduction_rate: 0.5
  reduction_increment: 0.01
  exemptions:
    player_kills: true
    boss_mobs: true
    event_rewards: true

classes:
  enabled: true
  change_cooldown: 86400
  reset_on_change:
    - exp
    - level

skills:
  enabled: true
  show_cooldown: true
  mana_cost: true
  messages:
    show_cast_success: true
    show_mana_consume: true
    show_hp_consume: true
    show_mana_insufficient: true
    show_hp_insufficient: true

debug:
  log_level: "INFO"
  verbose: false
  performance_logging: false
  log_exp_changes: false
  log_exp_diminish: false

level_up:
  show_title: true
  play_sound: true

hot_reload:
  classes: true
  skills: true
  exp_diminish: true
  mobs: true
  templates: false

damage:
  enabled: true
  use_external_config: true
  config_file: "damage_config.yml"
  log_final_damage: false
```

### 10.2 damage_config.yml 完全構造

```yaml
events:
  EntityDamageEvent:
    priority: HIGHEST
    handlers:
      - type: player
        enabled: true
        base_damage: 1.0
        stat_modifiers:
          - stat: STRENGTH
            multiplier: 0.5
            operation: ADD
        final_modifiers:
          - type: flat
            value: 0.0
            operation: ADD
          - type: multiplier
            value: 1.0
            operation: MULTIPLY

variables:
  global:
    BASE_DAMAGE: 10.0
    CRITICAL_MULTIPLIER: 1.5
  per_player:
    combo_count: 0
    last_damage_time: 0
```

---

*最終更新: 2025-01-20*
