# RPGPlugin API ドキュメント

## 目次

1. [概要](#概要)
2. [セットアップ](#セットアップ)
3. [SKript連携](#skript連携)
4. [Denizen連携](#denizen連携)
5. [Java API](#java-api)
6. [テンプレート使用方法](#テンプレート使用方法)
7. [使用例](#使用例)

---

## 概要

RPGPluginは、SKriptやDenizenなどの外部プラグインからアクセス可能なAPIを提供しています。これにより、独自のクエストシステム、ショップ、ギルド機能などをRPGシステムと連携させることができます。

### 主な機能

- **プレイヤーデータアクセス**: レベル、ステータス、クラス、スキル情報の取得・設定
- **経済システム**: ゴールドの付与、剥奪、転送
- **スキルシステム**: スキル習得、使用、レベル取得
- **クラスシステム**: クラス設定、ランクアップ
- **ダメージ計算**: ステータスに基づくダメージ計算

---

## セットアップ

### 1. プラグインのインストール

以下のプラグインをインストールしてください：

1. **RPGPlugin** (必須)
2. **SKript** または **Denizen** (オプション)
3. **Vault** (必須)
4. **MythicMobs** (必須)

### 2. 権限の設定

APIを使用するには、以下の権限が必要です：

```yaml
rpgplugin.api: true
```

### 3. APIコマンド

基本コマンド形式：

```bash
/rpg api <action> <args...>
```

---

## SKript連携

### 基本的な使用方法

SKriptからAPIを使用するには、コマンドを実行します：

```skript
command /testrpg:
    trigger:
        execute player command "rpg api get_level %player%"
```

### アクション一覧

#### レベル操作

```skript
# レベル取得
execute player command "rpg api get_level %player%"

# レベル設定
execute player command "rpg api set_level %player% 20"
```

#### ステータス操作

```skript
# ステータス取得 (STR, INT, SPI, VIT, DEX)
execute player command "rpg api get_stat %player% STR"

# ステータス設定
execute player command "rpg api set_stat %player% STR 50"
```

#### クラス操作

```skript
# クラス取得
execute player command "rpg api get_class %player%"

# クラス設定
execute player command "rpg api set_class %player% warrior"

# クラスアップ
execute player command "rpg api upgrade_class %player%"

# クラスアップ可能か確認
execute player command "rpg api can_upgrade_class %player%"
```

#### スキル操作

```skript
# スキル習得確認
execute player command "rpg api has_skill %player% fireball"

# スキル習得
execute player command "rpg api unlock_skill %player% fireball"

# スキル使用
execute player command "rpg api cast_skill %player% fireball"

# スキルレベル取得
execute player command "rpg api get_skill_level %player% fireball"
```

#### 経済操作

```skript
# ゴールド残高取得
execute player command "rpg api get_gold %player%"

# ゴールド付与
execute player command "rpg api give_gold %player% 100"

# ゴールド剥奪
execute player command "rpg api take_gold %player% 50"

# ゴールド所持確認
execute player command "rpg api has_gold %player% 100"

# ゴールド転送
execute player command "rpg api transfer_gold %player% target_player 100"
```

#### ダメージ計算

```skript
# ダメージ計算
execute player command "rpg api calculate_damage %player% target_entity"
```

### 完全なSKriptサンプル

サンプルファイル: `examples/skript/rpg_api_examples.sk`

---

## Denizen連携

### 基本的な使用方法

Denizenからは、タグ置換とコマンドの両方を使用できます。

#### タグ置換

```denizen
# レベル取得
define level <player.tag[rpg.level]>
narrate "あなたのLV: %level%"

# ステータス取得
define str <player.tag[rpg.stat[STR]]>
narrate "STR: %str%"

# クラス取得
define class <player.tag[rpg.class]>
narrate "クラス: %class%"

# ゴールド取得
define gold <player.tag[rpg.gold]>
narrate "所持金: %gold% G"
```

#### 利用可能なタグ

| タグ | 説明 | 例 |
|------|------|-----|
| `<player.tag[rpg.level]>` | レベル | `15` |
| `<player.tag[rpg.stat[<stat>]]>` | ステータス | `<player.tag[rpg.stat[STR]]>` |
| `<player.tag[rpg.stats]>` | 全ステータス | マップ形式 |
| `<player.tag[rpg.class]>` | クラスID | `"warrior"` |
| `<player.tag[rpg.class_rank]>` | クラスランク | `2` |
| `<player.tag[rpg.has_skill[<id>]]>` | スキル習得確認 | `"true"` または `"false"` |
| `<player.tag[rpg.skill_level[<id>]]>` | スキルレベル | `3` |
| `<player.tag[rpg.skill_count]>` | 習得済みスキル数 | `5` |
| `<player.tag[rpg.gold]>` | ゴールド残高 | `100.5` |
| `<player.tag[rpg.has_gold[<amount>]]>` | ゴールド所持確認 | `"true"` または `"false"` |
| `<player.tag[rpg.available_points]>` | 利用可能ステータスポイント | `10` |

#### コマンド実行

```denizen
# レベル設定
execute as_server "rpg api set_level %player% 20"

# クラス設定
execute as_server "rpg api set_class %player% warrior"

# ゴールド付与
execute as_server "rpg api give_gold %player% 100"
```

### 完全なDenizenサンプル

サンプルファイル: `examples/denizen/rpg_api_examples.dsc`

---

## Java API

### 基本的な使用方法

Javaプラグインから直接RPGPlugin APIを使用できます。

```java
// API取得
RPGPlugin rpgPlugin = (RPGPlugin) Bukkit.getPluginManager().getPlugin("RPGPlugin");
RPGPluginAPI api = rpgPlugin.getAPI();

// レベル取得
int level = api.getLevel(player);

// ゴールド付与
api.depositGold(player, 100.0);

// スキル使用
api.castSkill(player, "fireball");
```

### APIメソッド一覧

#### プレイヤーデータ

```java
// RPGPlayer取得
Optional<RPGPlayer> getRPGPlayer(Player player);

// レベル
int getLevel(Player player);
void setLevel(Player player, int level);

// ステータス
int getStat(Player player, Stat stat);
void setStat(Player player, Stat stat, int baseValue);

// クラス
String getClassId(Player player);
boolean setClass(Player player, String classId);
boolean upgradeClassRank(Player player);
boolean canUpgradeClass(Player player);
```

#### スキル

```java
boolean hasSkill(Player player, String skillId);
boolean unlockSkill(Player player, String skillId);
boolean castSkill(Player player, String skillId);
int getSkillLevel(Player player, String skillId);
Map<String, Integer> getAcquiredSkills(Player player);
List<Skill> getSkillsForClass(String classId);
```

#### 経済

```java
double getGoldBalance(Player player);
boolean depositGold(Player player, double amount);
boolean withdrawGold(Player player, double amount);
boolean hasEnoughGold(Player player, double amount);
boolean transferGold(Player from, Player to, double amount);
```

#### ダメージ

```java
double calculateDamage(Player attacker, Entity target);
double applyStatModifiers(Player player, double baseDamage, Stat stat);
```

---

## テンプレート使用方法

### クラステンプレート

テンプレートファイル: `src/main/resources/templates/classes/`

#### 使用手順

1. テンプレートファイルをコピー
2. `classes/` ディレクトリに配置
3. 内容をカスタマイズ
4. `/rpg reload` でリロード

#### 利用可能なテンプレート

- `melee_template.yml` - 近接クラス（STR/VIT成長）
- `ranged_template.yml` - 遠距離クラス（DEX/INT成長）
- `magic_template.yml` - 魔法クラス（INT/SPI成長）
- `tank_template.yml` - タンククラス（VIT/STR成長）

#### テンプレート構造

```yaml
id: template_melee              # クラスID（一意）
name: テンプレート近接クラス     # クラス名
display_name: "&cクラス名"       # 表示名
description:                    # 説明
  - "&f説明1"
  - "&f説明2"

rank: 1                         # ランク
max_level: 50                   # 最大レベル
icon: IRON_SWORD                # GUIアイコン

stat_growth:                    # ステータス成長
  auto:                         # 自動成長
    strength: 2
    vitality: 1
  manual_points: 3              # 手動配分ポイント

next_rank:                      # 次のランク
  class_id: next_class_id
  requirements:                 # ランクアップ要件
    - type: level
      value: 20
    - type: stat
      stat: STRENGTH
      value: 50

available_skills:               # 使用可能スキル
  - power_strike
  - shield_bash

passive_bonuses:                # パッシブボーナス
  - type: damage_multiplier
    value: 1.1
  - type: health_bonus
    value: 20
```

### スキルテンプレート

テンプレートファイル: `src/main/resources/templates/skills/`

#### 利用可能なテンプレート

- `active_skill_template.yml` - アクティブスキル
- `passive_skill_template.yml` - パッシブスキル

#### アクティブスキルテンプレート構造

```yaml
name: "template_active_skill"    # スキルID
display_name: "&eスキル名"       # 表示名
type: "ACTIVE"                   # タイプ
description: "説明"               # 説明

max_level: 5                     # 最大レベル
mana_cost: 20                    # マナコスト
cooldown: 10                     # クールダウン（秒）
cast_time: 1                     # キャスト時間（秒）

damage_formula: "BASE_DAMAGE + (LEVEL * 5) + (STRENGTH * 1.5)"
base_damage: 20                  # 基本ダメージ

levels:                          # レベルごとの効果
  1:
    damage_multiplier: 1.0
    range: 5
    effect_duration: 3
  # ... 他のレベル

requirements:                    # 必要条件
  level: 1
  class: ["warrior", "knight"]

unlock_cost: 100                 # 習得コスト（ゴールド）
```

#### パッシブスキルテンプレート構造

```yaml
name: "template_passive_skill"   # スキルID
display_name: "&eスキル名"       # 表示名
type: "PASSIVE"                  # タイプ
description: "説明"               # 説明

max_level: 10                    # 最大レベル
base_bonus: 5                    # 基本ボーナス

levels:                          # レベルごとの効果
  1:
    bonus_value: 5
    multiplier: 1.0
  # ... 他のレベル

requirements:                    # 必要条件
  level: 5
  class: ["warrior"]

unlock_cost: 100                 # 習得コスト（ゴールド）
effect_type: "bonus"             # 効果タイプ
```

---

## 使用例

### 例1: クエスト報酬システム

#### SKript版

```skript
command /questcomplete <text>:
    trigger:
        if {_text} is "tutorial":
            execute player command "rpg api give_gold %player% 100"
            execute player command "rpg api unlock_skill %player% power_strike"
            send "&aクエスト完了！100Gとスキルを獲得しました！"
```

#### Denizen版

```denizen
quest_complete_command:
  type: task
  script:
    - define questId <c.args.get[1]>
    - if %questId% == tutorial:
        - execute as_server "rpg api give_gold %player% 100"
        - execute as_server "rpg api unlock_skill %player% power_strike"
        - narrate "<green>クエスト完了！100Gとスキルを獲得しました！"
```

### 例2: レベルアップボーナス

#### Denizen版

```denizen
level_up_bonus:
  type: world
  events:
    - player levels up
  script:
    - define level <context.new_level>
    - switch %level%:
        - case 10:
            - execute as_server "rpg api give_gold %player% 100"
            - narrate "<gray>レベル10ボーナス: 100Gを獲得！" context:%player%
        - case 20:
            - execute as_server "rpg api give_gold %player% 200"
            - execute as_server "rpg api unlock_skill %player% power_strike"
            - narrate "<gray>レベル20ボーナス: 200Gとスキルを獲得！" context:%player%
```

### 例3: ショップシステム

#### SKript版

```skript
command /shop buy <text>:
    trigger:
        if {_text} is "hp":
            # ゴールド確認
            execute player command "rpg api has_gold %player% 50"
            # 購入処理
            execute player command "rpg api take_gold %player% 50"
            heal player
            send "&aHP回復ポーションを使用しました！( -50G)"
```

### 例4: PvP報酬システム

#### Denizen版

```denizen
pvp_kill_reward:
  type: world
  events:
    - player killed by player
  script:
    # キラーに報酬
    - execute as_server "rpg api give_gold %context.killer% 100"
    - narrate "<green>プレイヤーを撃破！100Gを獲得！" context:%context.killer%

    # 被害者にペナルティ
    - define gold <context.victim.tag[rpg.gold]>
    - define penalty <math.mul[%gold%, 0.05]>
    - execute as_server "rpg api take_gold %context.victim% %penalty%"
```

---

## トラブルシューティング

### よくある問題

#### Q: コマンドが動作しない

A: 以下を確認してください：
1. 権限 `rpgplugin.api` が設定されているか
2. RPGPluginが正常に動作しているか
3. 引数の形式が正しいか

#### Q: SKriptから値を取得したい

A: 現在のバージョンでは、コマンドの実行結果を直接取得することはできません。プレイヤーにメッセージを表示するか、タグを使用してください。

#### Q: Denizenのタグが動作しない

A: Denizenのバージョンを確認してください。タグ機能は最新バージョンでサポートされています。

---

## サポート

バグ報告や機能リクエストは、GitHub Issuesまでお願いします。

---

## ライセンス

このプロジェクトはMITライセンスの下で提供されています。
