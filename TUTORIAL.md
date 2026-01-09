# RPGPlugin 利用チュートリアル

Minecraft Java Edition 用RPGプラグインの完全ガイド

---

## 目次

1. [はじめに](#はじめに)
2. [サーバー管理者向けガイド](#サーバー管理者向けガイド)
3. [プレイヤー向けガイド](#プレイヤー向けガイド)
4. [SKript/Denizen開発者向けガイド](#skriptdenizen開発者向けガイド)
5. [トラブルシューティング](#トラブルシューティング)

---

## はじめに

### RPGPluginとは？

RPGPluginはMinecraftサーバーに本格的なRPG要素を追加するプラグインです。

#### 主な機能

| 機能 | 説明 |
|------|------|
| **ステータスシステム** | STR/INT/SPI/VIT/DEX の5種類のステータス |
| **クラスシステム** | 4つの初期クラスから選択、ランクアップ可能 |
| **スキルシステム** | YAMLベースの柔軟なスキル定義、数式ダメージ計算 |
| **経済システム** | 独自のゴールド通貨、オークション機能 |
| **GUI** | ステータス配分、スキル習得、クラス選択の直感的なGUI |
| **外部API** | SKript/Denizenとの連携対応 |

### 対応環境

| 項目 | 要件 |
|------|------|
| **Minecraftバージョン** | 1.20+ (Paper/Spigot) |
| **Javaバージョン** | 21 |
| **必須プラグイン** | Vault, MythicMobs |
| **推奨プラグイン** | PlaceholderAPI |

---

## サーバー管理者向けガイド

### インストール手順

#### 1. 必須プラグインの準備

以下のプラグインを先にインストールしてください：

```bash
plugins/
├── Vault.jar
├── MythicMobs.jar
└── (オプション) PlaceholderAPI.jar
```

#### 2. RPGPluginのインストール

1. `rpg-plugin-1.0.0.jar` を `plugins/` フォルダに配置
2. サーバーを再起動
3. `plugins/RPGPlugin/` フォルダが自動生成されます

```
plugins/RPGPlugin/
├── classes/           # クラス定義YAML
├── skills/            # スキル定義YAML
│   ├── active/       # アクティブスキル
│   └── passive/      # パッシブスキル
├── config.yml        # メイン設定ファイル
└── data/             # プレイヤーデータ
```

#### 3. 基本設定

`config.yml` を編集してサーバーに合わせて設定します：

```yaml
# データベース設定
database:
  path: "data/database.db"
  pool_size: 10
  auto_save_interval: 300  # 5分ごとに自動保存

# キャッシュ設定（50-150人規模推奨）
cache:
  l2_max_size: 2000
  l2_ttl_minutes: 10
  expire_after_access: true

# MythicMobs連携
mythicmobs:
  enabled: true
  drop_expiration_minutes: 5
  drop_protection: true  # ドロップ保護

# 経験値減衰
exp_diminish:
  enabled: true
  start_level: 30
  reduction_rate: 0.5
```

### 権限設定

#### 基本権限

| 権限 | 説明 | デフォルト |
|------|------|----------|
| `rpg.use` | 基本的なRPGコマンド使用 | 全プレイヤー |
| `rpg.admin` | 管理者コマンド | OPのみ |

#### LuckPerms設定例

```yaml
groups:
  default:
    permissions:
      - rpg.use
  admin:
    permissions:
      - rpg.admin
```

### 管理者コマンド

```bash
# 設定をリロード
/rpg reload

# プレイヤーのステータスを設定
/rpg admin setstat <player> <stat> <value>

# プレイヤーのレベルを設定
/rpg admin setlevel <player> <level>

# ゴールドを付与
/rpg admin givegold <player> <amount>

# クラスを設定
/rpg admin setclass <player> <class_id>

# スキルを習得させる
/rpg admin unlockskill <player> <skill_id>
```

### クラスのカスタマイズ

#### クラス定義YAMLの作成

`classes/` ディレクトリに新しいYAMLファイルを作成します：

```yaml
# custom_warrior.yml
id: custom_warrior
name: カスタム戦士
display_name: "&cカスタム戦士"
description:
  - "&f近接戦闘の専門家"
  - "&c高いHPと攻撃力が特徴"

rank: 1
max_level: 50
icon: IRON_SWORD

# ステータス成長
stat_growth:
  auto:              # レベルアップ時の自動成長
    strength: 2
    vitality: 1
  manual_points: 3   # 手動配分可能ポイント

# 次のランク（クラスアップ）
next_rank:
  class_id: advanced_warrior
  requirements:
    - type: level
      value: 20
    - type: stat
      stat: STRENGTH
      value: 50

# 使用可能スキル
available_skills:
  - power_strike
  - shield_bash
  - war_cry

# パッシブボーナス
passive_bonuses:
  - type: damage_multiplier
    value: 1.1
  - type: health_bonus
    value: 20
```

### スキルのカスタマイズ

#### アクティブスキルの作成

`skills/active/` ディレクトリにYAMLファイルを作成します：

```yaml
# fire_burst.yml
id: fire_burst
name: ファイアバースト
display_name: "&cファイアバースト"
type: active
description:
  - "&f前方に火の玉を放つ"
  - "&c範囲内の敵にダメージ"

max_level: 5

# コスト設定
cost:
  type: mana
  base: 20
  per_level: -2
  min: 10

# クールダウン
cooldown:
  base: 10.0
  per_level: -1.0
  min: 5.0

# ダメージ計算（数式使用可能）
damage:
  formula: "INT * 2.0 + Lv * 15 + 50"
  levels:
    5: "INT * 2.5 + Lv * 20 + 100"  # Lv5でダメージ上昇

# ターゲット設定
targeting:
  type: cone          # コーン状範囲
  cone:
    angle: 60         # 60度
    range: 8.0        # 範囲8ブロック
  max_targets: 5      # 最大5体

# 習得条件
requirements:
  level: 5
  class:
    - mage
    - battlemage

# 前提スキル
prerequisite:
  skill_id: fireball
  level: 3

# スキルツリー設定
skill_tree:
  parent: fireball
  cost: 5            # 習得に必要なスキルポイント
  requirements:
    - type: level
      value: 10
    - type: stat
      stat: INTELLIGENCE
      value: 30
```

#### 数式で使用可能な変数

| 変数 | 説明 | 例 |
|------|------|-----|
| `STR` | 筋力 | 50 |
| `INT` | 知力 | 40 |
| `SPI` | 精神 | 35 |
| `VIT` | 生命力 | 45 |
| `DEX` | 器用さ | 30 |
| `Lv` | スキルレベル | 3 |
| `HP`, `HP_max`, `HP_ratio` | HP関連 | 100, 200, 0.5 |
| `MP`, `MP_max` | MP関連 | 50, 100 |

### ホットリロード機能

設定ファイルを編集後、コマンドでリロード可能です：

```bash
# 全設定をリロード
/rpg reload

# クラス定義のみリロード
/rpg reload classes

# スキル定義のみリロード
/rpg reload skills
```

---

## プレイヤー向けガイド

### 最初のステップ

#### 1. クラスを選択する

サーバーに初めてログインすると、クラス選択GUIが表示されます。

| クラス | 特徴 | 成長傾向 |
|--------|------|----------|
| **戦士** | 近接戦闘、高HP | STR/VIT |
| **魔法使い** | 魔法攻撃、広範囲 | INT/SPI |
| **盗賊** | 高い回避率、クリティカル | DEX/STR |
| **聖職者** | 回復、サポート | SPI/VIT |

#### 2. ステータスを振る

`/rpg stat` コマンドでステータスGUIを開きます。

```
レベルアップ時に +3ポイント 獲得
ステータスをクリックで +1
保存ボタンで確定
キャンセルで元に戻す
```

| ステータス | 効果 |
|-----------|------|
| **STR（筋力）** | 近接ダメージ上昇 |
| **INT（知力）** | 魔法ダメージ上昇、MP増加 |
| **SPI（精神）** | 回復量上昇、MP回復 |
| **VIT（生命力）** | 最大HP上昇、防御力 |
| **DEX（器用さ）** | クリティカル率、回避率 |

#### 3. スキルを習得する

`/rpg skill` コマンドでスキルツリーGUIを開きます。

```
スキルポイントを消費してスキルを習得
左クリックで習得/強化
右クリックで詳細表示
```

### 基本コマンド

```bash
# ステータスGUIを開く
/rpg stat

# スキルGUIを開く
/rpg skill

# クラスGUIを開く
/rpg class

# 自分の情報を表示
/rpg info

# クラス情報を表示
/rpg class info

# クラスアップ（要件を満たしている場合）
/rpg class upgrade
```

### 経済システム

#### ゴールドの取得

- モブ討伐
- クエスト完了
- プレイヤー間取引
- オークション

#### ゴールドの使用

```bash
# 残高を確認
/rpg gold

# 他プレイヤーに送金
/rpg pay <player> <amount>
```

### レベルアップ

- モブを倒して経験値を獲得
- 経験値が満タンになると自動レベルアップ
- レベルアップ時：
  - ステータス +3ポイント
  - スキルポイント +1

### クラスアップ

特定のレベルとステータス要件を満たすと、上位クラスにアップできます。

```bash
# クラスアップ要件を確認
/rpg class requirements

# クラスアップ実行
/rpg class upgrade
```

#### クラスアップ例

戦士 → ナイト → パラディン
- ナイト: LV20, STR50
- パラディン: LV40, STR80, SPI50

---

## SKript/Denizen開発者向けガイド

### SKript連携

#### 基本構文

```skript
# APIコマンドの実行
execute player command "rpg api <action> <args...>"
```

#### アクション一覧

##### レベル操作

```skript
# レベル取得
execute player command "rpg api get_level %player%"

# レベル設定
execute player command "rpg api set_level %player% 20"
```

##### ステータス操作

```skript
# ステータス取得 (STR, INT, SPI, VIT, DEX)
execute player command "rpg api get_stat %player% STR"

# ステータス設定
execute player command "rpg api set_stat %player% STR 50"
```

##### クラス操作

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

##### スキル操作

```skript
# スキル習得確認
execute player command "rpg api has_skill %player% fireball"

# スキル習得
execute player command "rpg api unlock_skill %player% fireball"

# スキル使用
execute player command "rpg api cast_skill %player% fireball"

# スキルレベル取得
execute player command "rpg api get_skill_level %player% fireball"

# ターゲット指定スキル使用
execute player command "rpg api cast_at %player% fireball %target%"
```

##### 経済操作

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

##### ターゲット・範囲操作

```skript
# 範囲内のエンティティ取得
execute player command "rpg api get_entities_in_area %player% sphere 5"

# ターゲット設定
execute player command "rpg api set_target %player% %target%"
```

#### SKriptサンプル

##### クエスト報酬システム

```skript
command /questcomplete <text>:
    trigger:
        if {_text} is "tutorial":
            execute player command "rpg api give_gold %player% 100"
            execute player command "rpg api unlock_skill %player% power_strike"
            send "&aクエスト完了！100Gとスキルを獲得しました！"

command /dailyreward:
    trigger:
            execute player command "rpg api give_gold %player% 500"
            execute player command "rpg api set_stat %player% INT 100"
            send "&aデイリーボーナスを受け取りました！"
        else:
            send "&c今日は既に受け取り済みです。"
```

##### PvP報酬システム

```skript
on death:
    attacker is a player
    victim is a player
    execute player command "rpg api give_gold %attacker% 100"
    send "&a撃破報酬: 100Gを獲得！" to attacker
```

##### レベルボーナス

```skript
on levelup:
    if player's level is 10:
        execute player command "rpg api give_gold %player% 100"
        send "&aレベル10ボーナス: 100Gを獲得！"
    else if player's level is 20:
        execute player command "rpg api give_gold %player% 200"
        execute player command "rpg api unlock_skill %player% power_strike"
        send "&aレベル20ボーナス: 200Gとスキルを獲得！"
```

### PlaceholderAPI連携

#### 使用可能なプレースホルダー

```yaml
# プレイヤー情報
%rpgplugin_level%          # レベル
%rpgplugin_class%          # クラス名
%rpgplugin_class_rank%     # クラスランク

# ステータス
%rpgplugin_str%            # 筋力
%rpgplugin_int%            # 知力
%rpgplugin_spi%            # 精神
%rpgplugin_vit%            # 生命力
%rpgplugin_dex%            # 器用さ

# HP/MP
%rpgplugin_hp%             # 現在HP
%rpgplugin_hp_max%         # 最大HP
%rpgplugin_mp%             # 現在MP
%rpgplugin_mp_max%         # 最大MP

# スキル
%rpgplugin_skill_points%   # スキルポイント
%rpgplugin_stat_points%    # ステータスポイント
%rpgplugin_skill_count%    # 習得スキル数

# 経済
%rpgplugin_gold%           # ゴールド残高
```

#### 使用例

```yaml
# Scoreboard設定
scoreboard:
  title: "&c&lRPG Status"
  rows:
    - "&7Level: &f%rpgplugin_level%"
    - "&7Class: &f%rpgplugin_class%"
    - "&7HP: &a%rpgplugin_hp%&7/&a%rpgplugin_hp_max%"
    - "&7MP: &b%rpgplugin_mp%&7/&b%rpgplugin_mp_max%"
    - "&7Gold: &e%rpgplugin_gold%G"
```

### Java API

#### 基本的な使用方法

```java
// API取得
RPGPlugin rpgPlugin = (RPGPlugin) Bukkit.getPluginManager().getPlugin("RPGPlugin");
RPGPluginAPI api = rpgPlugin.getAPI();

// RPGPlayer取得
Optional<RPGPlayer> rpgPlayer = api.getRPGPlayer(player);
```

#### 主要メソッド

```java
// レベル操作
int level = api.getLevel(player);
api.setLevel(player, 20);

// ステータス操作
int str = api.getStat(player, Stat.STRENGTH);
api.setStat(player, Stat.STRENGTH, 50);

// クラス操作
String classId = api.getClassId(player);
api.setClass(player, "warrior");
boolean canUpgrade = api.canUpgradeClass(player);

// スキル操作
boolean hasSkill = api.hasSkill(player, "fireball");
boolean unlocked = api.unlockSkill(player, "fireball");
boolean casted = api.castSkill(player, "fireball");
int skillLevel = api.getSkillLevel(player, "fireball");

// 経済操作
double gold = api.getGoldBalance(player);
api.depositGold(player, 100.0);
boolean withdrawn = api.withdrawGold(player, 50.0);
boolean transferred = api.transferGold(player1, player2, 100.0);

// ダメージ計算
double damage = api.calculateDamage(attacker, target);
```

---

## トラブルシューティング

### よくある問題

#### Q: プラグインが動作しない

**A**: 以下を確認してください：

1. 必須プラグインがインストールされているか
   - Vault
   - MythicMobs

2. Javaバージョンが21であるか

3. エラーログを確認
   ```
   logs/latest.log
   ```

#### Q: コマンドが実行できない

**A**: 権限を確認してください。

```
/rpg info  # 基本権限の確認
```

#### Q: スキルが習得できない

**A**: 以下を確認してください：

1. スキルポイントが足りているか
2. 前提スキルを習得しているか
3. レベル要件を満たしているか
4. クラスが正しいか

#### Q: データが保存されない

**A**: `config.yml` のデータベース設定を確認してください。

```yaml
database:
  auto_save_interval: 300  # 自動保存間隔
  async_save: true         # 非同期保存
```

### デバッグモード

```yaml
# config.yml
debug:
  log_level: "FINE"        # 詳細ログ
  verbose: true            # 冗長ログ
  performance_logging: true # パフォーマンスログ
```

### サポート

バグ報告や機能リクエストは、GitHub Issuesまでお願いします。

---

*最終更新: 2026-01-08*
*バージョン: 1.0.0*
