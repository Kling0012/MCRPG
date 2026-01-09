# RPGPlugin システム仕様書

> **作業ブランチ**: main
> **最終更新**: 2026-01-09
> **バージョン**: 1.0.0

---

## 目次

1. [概要](#概要)
2. [技術スタック](#技術スタック)
3. [アーキテクチャ](#アーキテクチャ)
4. [システム構成](#システム構成)
5. [データベース](#データベース)
6. [キャッシュ戦略](#キャッシュ戦略)
7. [外部連携](#外部連携)
8. [設定ファイル](#設定ファイル)
9. [ホットリロード](#ホットリロード)

---

## 概要

### プロジェクト情報

| 項目 | 値 |
|------|-----|
| プラグイン名 | MCRPG |
| 対応バージョン | Minecraft 1.20+ (Paper/Spigot) |
| 対応規模 | 50-150人規模のサーバー |
| パッケージ | com.example.rpgplugin |
| 言語 | Java 21 |

### 依存プラグイン

| プラグイン | バージョン | 必須 |
|-----------|----------|------|
| Vault | 1.7.1 | 必須 |
| MythicMobs | 5.6.2 | 必須 |
| PlaceholderAPI | 2.11.6 | オプション |

### コア機能

1. **ステータスシステム** - STR/INT/SPI/VIT/DEXの5種類
2. **ダメージシステム** - ステータス依存のダメージ計算
3. **クラスシステム** - 4初期クラス、ランク6まで
4. **スキルシステム** - アクティブ/パッシブ、数式エバリュエーター
5. **経済システム** - 独自通貨（ゴールド）、オークション
6. **GUIシステム** - ステータス、スキル、トレード
7. **MythicMobs連携** - ドロップ管理
8. **外部API** - SKript/Denizen連携

---

## 技術スタック

### ビルド

```xml
<!-- pom.xml -->
<maven.compiler.source>21</maven.compiler.source>
<maven.compiler.target>21</maven.compiler.target>

<dependencies>
    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>1.20.6-R0.1-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### データベース

- **永続化**: SQLite
- **コネクションプール**: 独自実装（最大10接続）
- **非同期保存**: サポート

---

## アーキテクチャ

### デザインパターン

| パターン | 用途 | 実装クラス |
|---------|------|-----------|
| Facade | 統一API | RPGPlugin.java |
| Strategy | スキル実行切り替え | ActiveSkillExecutor, PassiveSkillExecutor |
| Observer | イベント連携 | RPGListener, MythicDeathListener |
| Repository | データアクセス抽象化 | PlayerDataRepository, CacheRepository |
| Builder | 複雑オブジェクト構築 | Skill.builder() |
| Proxy | キャッシュ層 | 3層キャッシュ |

### パッケージ構造

```
com.example.rpgplugin
├── RPGPlugin.java              # メインクラス（ファサード）
├── RPGCommand.java             # コマンドハンドラー
├── RPGListener.java            # イベントリスナー
│
├── api/                        # 外部API
│   ├── RPGPluginAPI.java       # APIインターフェース
│   ├── RPGPluginAPIImpl.java   # API実装
│   └── bridge/                 # SKript/Denizenブリッジ
│
├── core/                       # コアシステム
│   ├── config/                 # 設定管理
│   │   ├── YamlConfigManager.java
│   │   ├── ConfigWatcher.java
│   │   └── ResourceSetupManager.java
│   ├── dependency/             # 依存関係管理
│   │   ├── DependencyManager.java
│   │   ├── VaultHook.java
│   │   ├── MythicMobsHook.java
│   │   └── PlaceholderHook.java
│   ├── module/                 # モジュール管理
│   │   ├── ModuleManager.java
│   │   └── IModule.java
│   └── system/                 # システムマネージャー
│       ├── CoreSystemManager.java
│       ├── GameSystemManager.java
│       ├── ExternalSystemManager.java
│       └── GUIManager.java
│
├── player/                     # プレイヤー管理
│   ├── PlayerManager.java
│   ├── RPGPlayer.java          # データモデル
│   ├── ManaManager.java
│   ├── ExpManager.java
│   ├── ExpDiminisher.java      # 経験値減衰
│   └── config/                 # プレイヤー設定
│
├── stats/                      # ステータスシステム
│   ├── StatManager.java
│   ├── Stat.java               # Enum: STR/INT/SPI/VIT/DEX
│   ├── StatModifier.java
│   ├── PlayerStats.java
│   ├── ManaManager.java
│   └── calculator/             # ステータス計算
│       └── StatCalculator.java
│
├── skill/                      # スキルシステム
│   ├── SkillManager.java
│   ├── SkillLoader.java
│   ├── SkillTreeRegistry.java
│   ├── Skill.java              # データモデル
│   ├── SkillType.java          # ACTIVE/PASSIVE
│   ├── SkillCostType.java      # MP/HP
│   ├── executor/               # スキル実行
│   │   ├── SkillExecutor.java
│   │   ├── ActiveSkillExecutor.java
│   │   └── PassiveSkillExecutor.java
│   ├── target/                 # ターゲット・範囲
│   │   ├── TargetSelector.java
│   │   ├── TargetType.java
│   │   ├── SkillTarget.java
│   │   ├── AreaShape.java
│   │   ├── EntityTypeFilter.java
│   │   └── ShapeCalculator.java
│   ├── evaluator/              # 数式エバリュエーター
│   │   ├── FormulaEvaluator.java
│   │   ├── ExpressionParser.java
│   │   ├── VariableContext.java
│   │   └── FormulaDamageCalculator.java
│   ├── LevelDependentParameter.java
│   └── config/                 # スキル設定
│
├── rpgclass/                   # クラスシステム
│   ├── ClassManager.java
│   ├── ClassLoader.java
│   ├── ClassUpgrader.java
│   ├── RPGClass.java           # データモデル
│   ├── requirements/           # クラス要件
│   │   ├── ClassRequirement.java
│   │   ├── LevelRequirement.java
│   │   ├── StatRequirement.java
│   │   ├── ItemRequirement.java
│   │   └── QuestRequirement.java
│   └── growth/                 # ステータス成長
│       └── StatGrowth.java
│
├── damage/                     # ダメージシステム
│   ├── DamageManager.java
│   ├── DamageModifier.java
│   ├── DamageTracker.java
│   └── handlers/
│       ├── PlayerDamageHandler.java
│       └── EntityDamageHandler.java
│
├── mythicmobs/                 # MythicMobs連携
│   ├── MythicMobsManager.java
│   ├── drop/
│   │   ├── DropHandler.java
│   │   ├── DropData.java
│   │   └── DropRepository.java
│   ├── listener/
│   │   └── MythicDeathListener.java
│   └── config/
│       └── MobDropConfig.java
│
├── currency/                   # 経済システム
│   ├── CurrencyManager.java
│   └── CurrencyListener.java
│
├── auction/                    # オークション
│   ├── AuctionManager.java
│   ├── AuctionCommand.java
│   ├── BiddingSystem.java
│   ├── Auction.java
│   └── AuctionListing.java
│
├── trade/                      # トレード
│   ├── TradeManager.java
│   ├── TradeSession.java
│   ├── TradeInventory.java
│   └── TradeMenuListener.java
│
├── gui/                        # GUIシステム
│   └── menu/
│       ├── StatMenu.java
│       ├── SkillMenu.java
│       ├── StatMenuListener.java
│       ├── SkillMenuListener.java
│       └── rpgclass/
│           └── ClassMenu.java
│
└── storage/                    # データ永続化
    ├── StorageManager.java
    ├── database/
    │   ├── DatabaseManager.java
    │   ├── ConnectionPool.java
    │   └── SchemaManager.java
    ├── repository/
    │   ├── IRepository.java
    │   ├── PlayerDataRepository.java
    │   ├── PlayerCurrencyRepository.java
    │   └── CacheRepository.java
    ├── migrations/
    │   └── Migration_v1_1_PlayerStats.java
    └── models/
        ├── PlayerData.java
        ├── PlayerCurrency.java
        └── Serializable.java
```

### 依存関係フロー

```
RPGPlugin (Main)
    ├─→ CoreSystemManager
    │       ├─→ ConfigWatcher
    │       ├─→ ResourceSetupManager
    │       └─→ DependencyManager
    │               ├─→ VaultHook
    │               ├─→ MythicMobsHook
    │               └─→ PlaceholderHook
    │
    ├─→ GameSystemManager
    │       ├─→ PlayerManager
    │       ├─→ StatManager
    │       ├─→ ClassManager
    │       ├─→ SkillManager
    │       ├─→ DamageManager
    │       ├─→ CurrencyManager
    │       ├─→ AuctionManager
    │       ├─→ TradeManager
    │       └─→ MythicMobsManager
    │
    ├─→ GUIManager
    └─→ ExternalSystemManager
            ├─→ RPGPluginAPIImpl
            ├─→ SKriptBridge
            └─→ DenizenBridge
```

---

## システム構成

### ステータスシステム

#### ステータス種別

| Stat | 表示名 | 色 | 影響 |
|------|--------|-----|------|
| STRENGTH | STR | 赤 | 物理攻撃力、物理防御力 |
| INTELLIGENCE | INT | 緑 | 魔法攻撃力、魔法防御力、最大MP |
| SPIRIT | SPI | 桃 | 回復力、MP回復 |
| VITALITY | VIT | 黄 | 最大HP、物理防御力 |
| DEXTERITY | DEX | 青 | 命中率、回避率、クリティカル率 |

#### ステータス計算式

```
物理攻撃力 = STR * 2 + 基本攻撃力
魔法攻撃力 = INT * 1.5 + 基本攻撃力
物理防御力 = VIT * 1.2 + DEX * 0.3 + 基本防御力
魔法防御力 = INT * 0.8 + SPI * 0.5 + 基本防御力
最大HP = VIT * 10 + 基本HP
最大MP = INT * 5 + SPI * 3 + 基本MP
命中率 = 75 + DEX * 0.2 (%)
回避率 = 10 + DEX * 0.15 (%)
クリティカル率 = 5 + DEX * 0.1 (%)
クリティカル倍率 = 1.5 + DEX * 0.01
MP回復/秒 = SPI * 0.1
```

### ダメージシステム

#### ダメージ計算フロー

```
1. 基本ダメージ取得 (武器/魔法)
2. ステータス補正適用
3. ダメージ修正適用 (DamageModifier)
4. 防御計算
5. 最終ダメージ決定
```

#### ダメージタイプ

| タイプ | 説明 |
|--------|------|
| PHYSICAL | 物理ダメージ（STR依存） |
| MAGIC | 魔法ダメージ（INT依存） |
| TRUE | 固定ダメージ（防御無視） |

### クラスシステム

#### 初期クラス

| クラスID | 表示名 | 説明 |
|---------|--------|------|
| warrior | Warrior | 前線で戦う近接戦闘のエキスパート |
| mage | Mage | 強力な魔法を操る遠距離攻撃要員 |
| ranger | Ranger | 精密な射撃を行う遠距離物理アタッカー |
| cleric | Cleric | 味方を支援する回復魔法の専門家 |

#### クラスランク

| ランク | 名称 | 要件例 |
|-------|------|---------|
| Rank 1 | 初期クラス | なし |
| Rank 2 | 上位クラス | Lv30以上 |
| Rank 3 | 詳細クラス | Lv60以上 |
| Rank 4 | マスター | Lv90以上 |
| Rank 5 | グランドマスター | Lv120以上 |
| Rank 6 | レジェンド | Lv150以上 |

### スキルシステム

#### スキルタイプ

| タイプ | 説明 |
|--------|------|
| ACTIVE | 手動発動スキル |
| PASSIVE | 常時効果スキル |

#### コストタイプ

| タイプ | 説明 |
|--------|------|
| MP | マナ消費 |
| HP | HP消費（血と魂のスキル） |
| STAMINA | スタミナ消費 |

#### Phase11-6: 数式ベースダメージ計算

##### 組み込み変数

| 変数 | 型 | 説明 | 例 |
|------|----|----|-----|
| `STR` | Double | プレイヤーの筋力値 | `50` |
| `INT` | Double | プレイヤーの知力値 | `40` |
| `SPI` | Double | プレイヤーの精神値 | `30` |
| `VIT` | Double | プレイヤーの体力値 | `45` |
| `DEX` | Double | プレイヤーの敏捷値 | `35` |
| `Lv` | Integer | 現在のスキルレベル | `5` |
| `HP` | Double | 現在のHP値 | `100` |
| `HP_max` | Double | 最大HP値 | `200` |
| `HP_ratio` | Double | HP比率（0-1） | `0.5` |
| `MP` | Double | 現在のMP値 | `50` |
| `MP_max` | Double | 最大MP値 | `100` |

##### 演算子

| 演算子 | 説明 | 例 | 結果 |
|--------|----|-----|-----|
| `+` | 加算 | `10 + 5` | `15` |
| `-` | 減算 | `10 - 5` | `5` |
| `*` | 乗算 | `10 * 5` | `50` |
| `/` | 除算 | `10 / 5` | `2.0` |
| `%` | 剰余 | `10 % 3` | `1` |
| `^` | 累乗 | `2 ^ 3` | `8` |

##### 関数

| 関数 | 説明 | 例 | 結果 |
|------|----|-----|-----|
| `floor(x)` | 切り捨て | `floor(3.7)` | `3.0` |
| `ceil(x)` | 切り上げ | `ceil(3.2)` | `4.0` |
| `round(x)` | 四捨五入 | `round(3.5)` | `4.0` |
| `abs(x)` | 絶対値 | `abs(-5)` | `5.0` |
| `min(a,b)` | 最小値 | `min(10, 5)` | `5.0` |
| `max(a,b)` | 最大値 | `max(10, 5)` | `10.0` |
| `sqrt(x)` | 平方根 | `sqrt(16)` | `4.0` |
| `pow(x,y)` | 累乗 | `pow(2, 3)` | `8.0` |

##### 条件演算子（三項演算子）

```yaml
# 構文: 条件 ? 真の値 : 偽の値
damage:
  formula: "STR * 2.0 + (HP_ratio < 0.5 ? 50 : 0)"
```

| 条件 | 説明 | 例 |
|------|----|-----|
| `a < b` | 小なり | `STR < 50` |
| `a <= b` | 以下 | `Lv <= 5` |
| `a > b` | 大なり | `HP > 50` |
| `a >= b` | 以上 | `Lv >= 10` |
| `a == b` | 等しい | `Lv == 5` |
| `a != b` | 等しくない | `Lv != 1` |
| `a && b` | 論理積 | `Lv >= 5 && HP > 50` |
| `a || b` | 論理和 | `Lv >= 10 || STR > 50` |
| `!a` | 否定 | `!(HP_ratio > 0.5)` |

##### 数式例

```yaml
# 基本ダメージ
variables:
  str_scale: 1.5
damage:
  formula: "STR * str_scale + Lv * 10 + 50"

# クリティカル計算
damage:
  formula: "(STR * 1.5 + Lv * 10) * (1 + min(DEX * 0.002, 0.5) * 2.0)"

# 低HPボーナス
damage:
  formula: "STR * 2.0 + (HP_ratio < 0.3 ? 100 : 0)"

# レベル別数式
damage:
  formula: "STR * 1.5 + Lv * 10"
  levels:
    5: "STR * 2.0 + Lv * 15"
    10: "STR * 2.5 + Lv * 20 + 100"
```

#### Phase11-4: ターゲットシステム

##### TargetType 列挙型

```java
public enum TargetType {
    SELF,           // 自分自身
    NEAREST_HOSTILE, // 最も近い敵対MOB
    NEAREST_ENTITY,  // 最も近いエンティティ
    SINGLE,          // 単体ターゲット
    AREA,            // エリア内全て
    CONE,            // コーン状範囲
    SPHERE,          // 球形範囲
    SECTOR,          // 扇形範囲
    EXTERNAL         // 外部指定（SKript等）
}
```

##### ターゲットタイプ詳細

| タイプ | 説明 | パラメータ |
|--------|------|-----------|
| `SELF` | 自分自身 | なし |
| `SINGLE` | 単体ターゲット | `range` |
| `CONE` | コーン状範囲 | `angle`, `range` |
| `SPHERE` | 球形範囲 | `radius`, `max_targets` |
| `SECTOR` | 扇形範囲 | `angle`, `radius`, `max_targets` |
| `AREA` | 指定座標範囲 | `shape`, `radius` |
| `EXTERNAL` | 外部指定 | - |

##### ターゲットフィルター

```yaml
targeting:
  type: sphere
  sphere:
    radius: 5.0
    filters:
    - type: hostile    # 敵対MOBのみ
    - type: not_self   # 自分を除外
    - type: alive      # 生きているエンティティのみ
```

#### スキルYAML完全スキーマ

```yaml
# --- 基本情報 ---
id: string                   # 必須: スキルID
name: string                 # 必須: スキル名
display_name: string          # 必須: 表示名（カラーコード可）
type: string                 # 必須: active|passive
description:                 # オプション: 説明文リスト
  - string
max_level: integer           # 必須: 最大レベル

# --- カスタム変数 ---
variables:                   # オプション
  var_name: number

# --- コスト設定 ---
cost:                        # オプション
  type: string               # mana|hp|stamina
  base: number               # 基本コスト
  per_level: number          # レベル増減
  min: number                # 最小値
  max: number                # 最大値

# --- クールダウン設定 ---
cooldown:                    # オプション
  base: number               # 基本クールダウン（秒）
  per_level: number          # レベル増減（秒）
  min: number                # 最短クールダウン（秒）

# --- ダメージ計算 ---
damage:                      # オプション（攻撃スキル）
  formula: string            # 基本数式
  levels:                    # レベル別数式
    level: string

# --- バフ効果 ---
buff_effects:                # オプション（バフスキル）
  duration:                  # 効果時間
    formula: string
  effects:                   # 効果リスト
    - type: movement_speed|attack_speed|stat_boost
      stat: string           # stat_boost時のステータス
      value: number
      stack_type: string     # add|multiply

# --- ターゲット設定 ---
targeting:                   # 必須
  type: string               # single|cone|sphere|sector|self|area
  cone:                      # coneの場合
    angle: number            # 角度（度）
    range: number            # 範囲
  sphere:                    # sphereの場合
    radius: number           # 半径
    max_targets: integer     # 最大ターゲット数
  sector:                    # sectorの場合
    angle: number            # 角度
    radius: number           # 半径
    max_targets: integer
  filters:                   # ターゲットフィルター
    - type: string

# --- エフェクト設定 ---
effects:                     # オプション
  cast:                      # 発動時
    - type: sound|particle
  hit:                       # ヒット時
    - ...
  impact:                    # 着弾時
    - ...

# --- デバフ付与 ---
debuffs:                     # オプション
  - type: burn|slow|poison|stun
    duration: number

# --- 追加効果 ---
additional_effects:          # オプション
  - type: knockback|lifesteal|stun

# --- スキルツリー ---
skill_tree:                  # オプション
  parent: string             # 親スキルID
  unlock_requirements:       # 習得要件
    - type: level|stat|skill|class
  cost: integer              # 習得コスト

# --- GUI設定 ---
icon_material: string         # アイコンマテリアル
available_classes:           # 使用可能クラス
  - string
```

### 経済システム

#### 通貨

| 項目 | 値 |
|------|-----|
| 通貨名 | ゴールド |
| 記号 | G |
| 最小単位 | 0.01 G |

#### オークション

| 項目 | 値 |
|------|-----|
| 手数料率 | 5% |
| 最低入札増 | 現在額の10% |
| 延長時間 | 入礼時に+5秒 |
| 出品期間 | 30-180秒 |

### 経験値減衰システム

#### 減衰計算

```
減衰開始レベル: 30 (デフォルト)
軽減率: 50% (デフォルト)

獲得経験値 = 基礎経験値 * (1 - 減衰率)

減衰率 = 基本減衰率 + (現在レベル - 開始レベル) * 増加量
例: Lv50の場合 = 0.5 + (50 - 30) * 0.01 = 0.7 (70%軽減)
```

#### 除外設定

| 条件 | デフォルト |
|------|----------|
| プレイヤーキル | 対象外 |
| ボスモブ | 対象外 |
| イベント報酬 | 対象外 |

---

## データベース

### スキーマ

#### player_data テーブル

| カラム | 型 | 説明 |
|--------|-----|------|
| uuid | TEXT | プレイヤーUUID (PK) |
| username | TEXT | プレイヤー名 |
| class_id | TEXT | クラスID |
| level | INTEGER | レベル |
| exp | REAL | 経験値 |
| strength | INTEGER | STR基本値 |
| intelligence | INTEGER | INT基本値 |
| spirit | INTEGER | SPI基本値 |
| vitality | INTEGER | VIT基本値 |
| dexterity | INTEGER | DEX基本値 |
| available_points | INTEGER | 手動配分ポイント |
| acquired_skills | TEXT | 習得スキル (JSON) |
| last_updated | INTEGER | 最終更新時刻 (Unix時間) |

#### player_currency テーブル

| カラム | 型 | 説明 |
|--------|-----|------|
| uuid | TEXT | プレイヤーUUID (PK) |
| balance | REAL | ゴールド残高 |
| last_updated | INTEGER | 最終更新時刻 |

### マイグレーション

- `v1_1_PlayerStats` - MP/HP、スキルコストタイプ対応

---

## キャッシュ戦略

### 3層キャッシュ

```
┌─────────────────────────────────────────────────────────┐
│                    アプリケーション                       │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│  L1 Cache: ConcurrentHashMap (オンラインプレイヤー)        │
│  - 稼働時間中のプレイヤーデータを保持                       │
│  - 即時アクセス、無効化なし                                │
└─────────────────────────────────────────────────────────┘
                          │ ミス
                          ▼
┌─────────────────────────────────────────────────────────┐
│  L2 Cache: Caffeine (LRUキャッシュ)                       │
│  - 最大サイズ: 2000エントリ (設定可能)                    │
│  - TTL: 10分 (アクセス時間ベース)                         │
│  - 最近アクセスしたオフラインプレイヤーを保持              │
└─────────────────────────────────────────────────────────┘
                          │ ミス
                          ▼
┌─────────────────────────────────────────────────────────┐
│  L3 Cache: SQLite (永続化)                               │
│  - ディスクベースのデータベース                           │
│  - 非同期保存 (5分間隔 or プレイヤー退出時)               │
└─────────────────────────────────────────────────────────┘
```

### キャッシュ設定

| 項目 | デフォルト値 | 説明 |
|------|-------------|------|
| `cache.l2_max_size` | 2000 | L2キャッシュ最大サイズ |
| `cache.l2_ttl_minutes` | 10 | L2キャッシュTTL |
| `cache.expire_after_access` | true | アクセス時間ベースTTL |
| `database.auto_save_interval` | 300 | 自動保存間隔(秒) |

---

## 外部連携

### MythicMobs連携

#### ドロップ設定

```yaml
# plugins/RPGPlugin/mobs/mob_drops.yml
mobs:
  BOSS_SKELETON:
    drops:
      - item: DIAMOND_SWORD
        chance: 0.1
        amount: 1
      - item: GOLD_INGOT
        chance: 0.5
        amount: 5-10
```

#### ドロップ保護

- 保護期間: 5分 (設定可能)
- ドロップ者のみ拾得可能

### PlaceholderAPI連携

#### プレースホルダー一覧

| プレースホルダー | 説明 |
|----------------|------|
| `%rpg_level%` | レベル |
| `%rpg_class%` | クラス名 |
| `%rpg_str%` | STR値 |
| `%rpg_int%` | INT値 |
| `%rpg_spi%` | SPI値 |
| `%rpg_vit%` | VIT値 |
| `%rpg_dex%` | DEX値 |
| `%rpg_gold%` | ゴールド残高 |
| `%rpg_hp%` | 現在HP |
| `%rpg_max_hp%` | 最大HP |
| `%rpg_mp%` | 現在MP |
| `%rpg_max_mp%` | 最大MP |

### SKript連携

#### イベント

```skript
on rpg skill cast:
    if skill-id is "fireball":
        send "ファイアボールが発動されました！"
        cancel event
```

#### アクション

```skript
# スキル発動
rpg cast "fireball" for player

# ステータス取得
set {_str} to rpg stat of player
set {_level} to rpg level of player

# ゴールド操作
add 100 to rpg gold of player
remove 50 from rpg gold of player
```

---

## 設定ファイル

### config.yml 構造

```yaml
# データベース設定
database:
  path: "data/database.db"
  pool_size: 10
  async_save: true
  auto_save_interval: 300

# キャッシュ設定
cache:
  l2_max_size: 2000
  l2_ttl_minutes: 10
  expire_after_access: true
  stats_logging_interval: 300
  hit_logging: false
  batch_save_size: 50

# MythicMobs連携
mythicmobs:
  enabled: true
  drop_expiration_minutes: 5
  drop_protection: true

# 経験値減衰
exp_diminish:
  enabled: true
  start_level: 30
  reduction_rate: 0.5
  reduction_increment: 0.01
  exemptions:
    player_kills: true
    boss_mobs: true
    event_rewards: true

# 経済システム
economy:
  currency_name: "ゴールド"
  currency_symbol: "G"
  auction_fee_rate: 0.05
  min_deposit: 1
  max_deposit: 1000000

# クラスシステム
classes:
  enabled: true
  change_cooldown: 86400
  reset_on_change:
    - exp
    - level

# スキルシステム
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

# デバッグ設定
debug:
  log_level: "INFO"
  verbose: false
  performance_logging: false
  log_exp_changes: false
  log_exp_diminish: false

# レベルアップ演出
level_up:
  show_title: true
  play_sound: true

# ホットリロード設定
hot_reload:
  classes: true
  skills: true
  exp_diminish: true
  mobs: true
  templates: false
```

---

## ホットリロード

### 対応ディレクトリ

| ディレクトリ | 設定キー | デフォルト |
|-------------|---------|----------|
| `classes/` | `hot_reload.classes` | true |
| `skills/` | `hot_reload.skills` | true |
| `exp/` | `hot_reload.exp_diminish` | true |
| `mobs/` | `hot_reload.mobs` | true |
| `templates/` | `hot_reload.templates` | false (ログのみ) |

### ディレクトリ構造

```
plugins/RPGPlugin/
├── config.yml
├── classes/
│   ├── README.txt
│   ├── warrior.yml
│   ├── mage.yml
│   └── ...
├── skills/
│   ├── README.txt
│   ├── active/
│   │   ├── power_strike.yml
│   │   └── ...
│   └── passive/
│       ├── critical_mastery.yml
│       └── ...
├── mobs/
│   └── mob_drops.yml
├── exp/
│   └── diminish_config.yml
├── data/
│   └── database.db
└── templates/
    ├── skills/
    │   ├── active_skill_template.yml
    │   └── passive_skill_template.yml
    └── classes/
        ├── melee_template.yml
        ├── ranged_template.yml
        ├── magic_template.yml
        └── tank_template.yml
```

### リロード方法

1. **コマンドリロード**: `/rpg reload`
2. **自動リロード**: ファイル保存時（`hot_reload.*: true`時）

---

## 関連項目

- [APIリファレンス](API.md)
- [コマンドリファレンス](COMMANDS.md)
- [チュートリアル](../TUTORIAL.md)
