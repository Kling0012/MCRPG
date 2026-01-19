# RPGPlugin 利用チュートリアル

Minecraft Java Edition 用RPGプラグインの完全ガイド

---

## 目次

1. [はじめに](#はじめに)
2. [サーバー管理者向けガイド](#サーバー管理者向けガイド)
3. [プレイヤー向けガイド](#プレイヤー向けガイド)
4. [Java開発者向けガイド](#java開発者向けガイド)
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
| **外部API** | PlaceholderAPIとの連携対応 |

### 対応環境

| 項目 | 要件 |
|------|------|
| **Minecraftバージョン** | 1.20+ (Paper/Spigot) |
| **Javaバージョン** | 21 |
| **推奨プラグイン** | PlaceholderAPI, MythicMobs |

---

## サーバー管理者向けガイド

### インストール手順

#### 1. 推奨プラグインの準備

以下のプラグインをインストールしてください：

```bash
plugins/
├── PlaceholderAPI.jar    # 推奨
└── (オプション) MythicMobs.jar
```

#### 2. RPGPluginのインストール

1. `mc-rpg-1.0.1.jar` を `plugins/` フォルダに配置
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
```

---

## プレイヤー向けガイド

### 最初のステップ

#### 1. クラスを選択する

サーバーに初めてログインすると、クラスを選択できます。

| クラス | 特徴 | 成長傾向 |
|--------|------|----------|
| **戦士** | 近接戦闘、高HP | STR/VIT |
| **大盾使い** | 防御特化 | VIT/STR |
| **魔法使い** | 魔法攻撃、広範囲 | INT/SPI |
| **弓使い** | 遠距離攻撃 | DEX/STR |

#### 2. ステータスを振る

レベルアップ時にステータスポイントが付与されます。

| ステータス | 効果 |
|-----------|------|
| **STR（筋力）** | 近接ダメージ上昇 |
| **INT（知力）** | 魔法ダメージ上昇、MP増加 |
| **SPI（精神）** | 回復量上昇、MP回復 |
| **VIT（生命力）** | 最大HP上昇、防御力 |
| **DEX（器用さ）** | クリティカル率、回避率 |

### 基本コマンド

```bash
# ステータス情報を表示
/rpg stats

# スキル情報を表示
/rpg skill

# クラス一覧を表示
/rpg class list

# クラスを選択
/rpg class <クラスID>

# スキルを発動
/rpg cast <スキルID>
```

### レベルアップ

- モブを倒して経験値を獲得
- 経験値が満タンになると自動レベルアップ
- レベルアップ時：
  - ステータス +3ポイント
  - スキルポイント +1

### クラスアップ

特定のレベルとステータス要件を満たすと、上位クラスにアップできます。

---

## Java開発者向けガイド

### 基本的な使用方法

```java
// API取得
RPGPlugin rpgPlugin = (RPGPlugin) Bukkit.getPluginManager().getPlugin("RPGPlugin");
RPGPluginAPI api = rpgPlugin.getAPI();

// RPGPlayer取得
Optional<RPGPlayer> rpgPlayer = api.getRPGPlayer(player);
```

### 主要メソッド

```java
// レベル操作
int level = api.getLevel(player);

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

// ダメージ計算
double damage = api.calculateDamage(attacker, target);
```

---

## トラブルシューティング

### よくある問題

#### Q: プラグインが動作しない

**A**: 以下を確認してください：

1. Javaバージョンが21であるか

2. エラーログを確認
   ```
   logs/latest.log
   ```

#### Q: コマンドが実行できない

**A**: 権限を確認してください。

```
/rpg stats  # 基本権限の確認
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

*最終更新: 2026-01-10*
*バージョン: 1.0.0*
