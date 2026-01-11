# Minecraft RPGプラグイン - 設計書（実装完了版）

## プロジェクト概要

**対象サーバースケール**: 50-150人未満
**Minecraftバージョン**: 1.20.6 (Paper 1.20.6)
**Javaバージョン**: 21
**外部連携**: MythicMobs, PlaceholderAPI, Skript (オプション)

### 実装済み核心機能
1. **ダメージシステム**: 全イベントキャッチ、ステータス倍率、ダメージカット
2. **ステータスシステム**: STR/INT/SPI/VIT/DEX、レベルアップ時「自動+2 / 手動配分3ポイント」
3. **クラスシステム**: 4初期クラス（戦士、大盾使い、魔法使い、弓使い）、ランク6までクラスアップ、直線/分岐両対応
4. **スキルシステム**: V5コンポーネントベース、共通スキルプール、数式ベースダメージ計算
5. **ストレージ**: SQLite + 3層キャッシュ（50-150人対応）
6. **MythicMobs連携**: ドロップ管理（倒した人のみ）
7. **外部API**: PlaceholderAPI、Skript連携、Java API
8. **バニラ経験値**: 減衰設定、各クラスで上限設定

### 未実装（スコープ外）
- 経済システム（独自通貨）
- オークションシステム
- トレードシステム
- ステータス振りGUI
- PvPシステム

---

## アーキテクチャ設計

### パッケージ構成（実装済み）
```
com.example.rpgplugin
├── RPGPlugin.java              # メインクラス
├── RPGCommand.java             # メインコマンド
├── RPGListener.java            # イベントリスナー
├── api/                        # 外部API
│   ├── RPGPluginAPI.java       # APIインターフェース
│   ├── RPGPluginAPIImpl.java   # API実装
│   ├── command/
│   │   └── APICommand.java
│   ├── placeholder/
│   │   └── RPGPlaceholderExpansion.java
│   └── skript/                 # Skript連携
│       ├── RPGSkriptAddon.java
│       ├── conditions/
│       ├── effects/
│       ├── events/
│       └── expressions/
├── command/                    # コマンドシステム
├── core/                       # コアシステム
│   ├── config/                 # 設定管理・ホットリロード
│   │   ├── Configuration.java
│   │   ├── ConfigLoader.java
│   │   ├── ConfigWatcher.java
│   │   ├── ReloadStrategy.java
│   │   ├── ResourceSetupManager.java
│   │   └── YamlConfigManager.java
│   ├── dependency/             # MythicMobs連携
│   │   ├── DependencyManager.java
│   │   └── MythicMobsHook.java
│   ├── module/                 # モジュールシステム
│   │   ├── IModule.java
│   │   └── ModuleManager.java
│   ├── system/
│   └── validation/
├── damage/                     # ダメージシステム
│   ├── DamageManager.java
│   ├── DamageModifier.java
│   ├── DamageTracker.java
│   └── handlers/
│       ├── EntityDamageHandler.java
│       └── PlayerDamageHandler.java
├── gui/                        # GUIシステム
│   ├── SkillTreeGUI.java
│   └── SkillTreeGUIListener.java
├── mythicmobs/                 # MythicMobs連携
│   ├── MythicMobsManager.java
│   ├── config/
│   │   └── MobDropConfig.java
│   ├── drop/
│   │   ├── DropData.java
│   │   ├── DropHandler.java
│   │   └── DropRepository.java
│   └── listener/
│       └── MythicDeathListener.java
├── player/                     # プレイヤー管理
│   ├── PlayerManager.java
│   ├── RPGPlayer.java
│   ├── ManaManager.java
│   ├── VanillaExpHandler.java
│   ├── ExpDiminisher.java
│   ├── config/
│   │   └── DiminishConfig.java
│   └── exp/
│       └── ExpManager.java
├── rpgclass/                   # クラスシステム
│   ├── RPGClass.java
│   ├── ClassManager.java
│   ├── ClassLoader.java
│   ├── ClassUpgrader.java
│   ├── growth/
│   │   └── StatGrowth.java
│   └── requirements/
│       ├── ClassRequirement.java
│       ├── ItemRequirement.java
│       ├── LevelRequirement.java
│       ├── QuestRequirement.java
│       └── StatRequirement.java
├── skill/                      # スキルシステム
│   ├── Skill.java
│   ├── SkillManager.java
│   ├── SkillLoader.java
│   ├── SkillTree.java
│   ├── SkillNode.java
│   ├── SkillType.java
│   ├── SkillCostType.java
│   ├── SkillTreeRegistry.java
│   ├── SkillMigrationTool.java
│   ├── LevelDependentParameter.java
│   ├── component/              # V5コンポーネントシステム
│   │   ├── EffectComponent.java
│   │   ├── SkillEffect.java
│   │   ├── ComponentLoader.java
│   │   ├── ComponentSettings.java
│   │   ├── ComponentEffectExecutor.java
│   │   ├── condition/          # 14種類
│   │   ├── cooldown/           # 1種類
│   │   ├── cost/               # 4種類
│   │   ├── filter/             # 2種類
│   │   ├── mechanic/           # 16種類
│   │   ├── placement/
│   │   ├── target/             # 8種類
│   │   └── trigger/            # 9種類
│   ├── config/
│   │   └── SkillConfig.java
│   ├── evaluator/              # 数式エバリュエーター
│   │   ├── FormulaEvaluator.java
│   │   ├── ExpressionParser.java
│   │   ├── FormulaDamageCalculator.java
│   │   └── VariableContext.java
│   ├── executor/               # スキル実行
│   │   ├── SkillExecutor.java
│   │   ├── ActiveSkillExecutor.java
│   │   └── PassiveSkillExecutor.java
│   └── target/                 # ターゲットシステム
│       ├── TargetType.java
│       ├── TargetSelector.java
│       ├── AreaShape.java
│       ├── ShapeCalculator.java
│       ├── SkillTarget.java
│       ├── EntityTypeFilter.java
│       └── TargetGroupFilter.java
├── stats/                      # ステータスシステム
│   ├── Stat.java               # Enum: STR, INT, SPI, VIT, DEX
│   ├── StatManager.java
│   ├── StatModifier.java
│   └── calculator/
│       └── StatCalculator.java
└── storage/                    # データ永続化
    ├── StorageManager.java
    ├── database/
    │   ├── DatabaseManager.java
    │   ├── ConnectionPool.java
    │   └── SchemaManager.java
    ├── migrations/
    ├── models/
    └── repository/
```

### 設計パターン
- **ファサード**: RPGPlugin, RPGPluginAPI
- **ストラテジー**: スキル実行、ダメージ計算
- **オブザーバー**: イベント駆動のシステム連携
- **リポジトリ**: データアクセスの抽象化
- **コンポジット**: V5コンポーネントツリー

---

## データベース設計

### データ保存方式: SQLite + 3層キャッシュ + YAML設定

| データ種別 | 保存方式 | 理由 |
|-----------|----------|------|
| プレイヤーデータ | SQLite | 50-150人対応、ACID保証 |
| クラス・スキル設定 | YAML | ホットリロード、人間可読 |
| キャッシュ層 | ConcurrentHashMap + Caffeine | 高速アクセス |

### キャッシュ戦略（3層構成）
- **L1キャッシュ**: オンラインプレイヤー全データ（ConcurrentHashMap）
- **L2キャッシュ**: 高頻度アクセスデータ（Caffeine、最大1000エントリ、5分TTL）
- **L3**: SQLiteデータベース

---

## V5 コンポーネントシステム

### コンポーネント一覧

| タイプ | 数 | 例 |
|--------|----|----|
| **Trigger** | 9 | CAST, CROUCH, LAND, DEATH, KILL, PHYSICAL_DEALT, PHYSICAL_TAKEN, LAUNCH, ENVIRONMENTAL |
| **Mechanic** | 16 | damage, heal, push, fire, message, potion, lightning, sound, command, explosion, speed, particle, launch, delay, cleanse, channel |
| **Condition** | 14 | health, chance, mana, biome, class, time, armor, fire, water, combat, potion, status, tool, event |
| **Target** | 8 | SELF, SINGLE, CONE, SPHERE, SECTOR, AREA, LINE, NEAREST_HOSTILE |
| **Cost** | 4 | MANA, HP, STAMINA, ITEM |
| **Cooldown** | 1 | COOLDOWN |
| **Filter** | 2 | entity_type, group |

### スキルYAML例
```yaml
id: fireball
name: "ファイアボール"
display_name: "&cファイアボール"
type: active
max_level: 5

cost:
  type: mana
  base: 20
  per_level: -2
  min: 10

cooldown:
  base: 10.0
  per_level: -1.0
  min: 5.0

variables:
  int_scale: 2.0

damage:
  formula: "50 + INT * int_scale + Lv * 15"

targeting:
  type: cone
  angle: 45
  range: 8.0

components:
  - type: trigger
    trigger: CAST
    children:
      - type: target
        target: CONE
        settings:
          angle: 45
          range: 8.0
        children:
          - type: mechanic
            mechanic: damage
            settings:
              amount: "50 + INT * 2 + Lv * 15"
          - type: mechanic
            mechanic: fire
            settings:
              duration: 60
```

---

## 外部API

### PlaceholderAPI

| プレースホルダー | 説明 |
|----------------|------|
| `%rpg_level%` | レベル |
| `%rpg_stat_STR%` | STR値 |
| `%rpg_stat_INT%` | INT値 |
| `%rpg_stat_SPI%` | SPI値 |
| `%rpg_stat_VIT%` | VIT値 |
| `%rpg_stat_DEX%` | DEX値 |
| `%rpg_available_points%` | 利用可能ステータスポイント |
| `%rpg_class%` | クラスID |
| `%rpg_class_name%` | クラス表示名 |
| `%rpg_class_rank%` | クラスランク |
| `%rpg_max_hp%` | 最大HP |
| `%rpg_max_mana%` | 最大MP |
| `%rpg_mana%` | 現在MP |
| `%rpg_skill_level_<skill>%` | スキルレベル |

### Java API
```java
public interface RPGPluginAPI {
    RPGPlayer getRPGPlayer(Player player);
    int getLevel(Player player);
    int getStat(Player player, Stat stat);
    String getClassId(Player player);
    void setStat(Player player, Stat stat, int baseValue);
    void addStatPoints(Player player, int points);
    void setClass(Player player, String classId);
    boolean hasSkill(Player player, String skillId);
    void unlockSkill(Player player, String skillId);
    void castSkill(Player player, String skillId);
    int getSkillLevel(Player player, String skillId);
}
```

### Skript連携
```skript
# イベント
on rpg skill cast:
on rpg skill damage:

# 式
rpg level of player
rpg stat STR of player
rpg class of player
rpg skill level of "fireball" of player

# エフェクト
make player cast rpg skill "fireball"
set rpg class of player to "warrior"
unlock rpg skill "power_strike" for player

# 条件
player has rpg skill "fireball"
player is rpg class "warrior"
rpg stat STR of player >= 50
```

---

## コマンド

| コマンド | 説明 | 権限 |
|---------|------|------|
| `/rpg` | メインメニュー | なし |
| `/rpg stats` | ステータス表示 | なし |
| `/rpg skill` | スキル情報表示 | なし |
| `/rpg cast <ID>` | スキル発動 | なし |
| `/rpg class list` | クラス一覧 | なし |
| `/rpg class <ID>` | クラス選択 | なし |
| `/rpg reload` | 設定リロード | rpg.admin |

---

## 実装状況サマリー

| フェーズ | 名称 | ステータス |
|---------|------|-----------|
| Phase 1 | コア基盤 | ✅ 完了 |
| Phase 2 | ステータスシステム | ✅ 完了 |
| Phase 3 | ダメージシステム | ✅ 完了 |
| Phase 4 | クラスシステム | ✅ 完了 |
| Phase 5 | スキルシステム | ✅ 完了 |
| Phase 6 | 経済・オークション | ⏭️ スキップ |
| Phase 7 | トレード・GUI | ⏭️ スキップ |
| Phase 8 | MythicMobs連携 | ✅ 完了 |
| Phase 9 | 外部API | ✅ 完了 |
| Phase 10 | 経験値減衰・最終調整 | ✅ 完了 |
| Phase 11 | スキルシステム改善 | ✅ 完了 |

**全コアフェーズ完了 - プロダクションレディ状態**

---

## テスト

| テストスイート | テスト数 | 説明 |
|---------------|---------|------|
| FormulaEvaluatorTest | 43 | 数式エバリュエーター |
| StatManagerTest | 32 | ステータス管理 |
| ManaManagerTest | 29 | マナ管理 |
| ComponentSystemIntegrationTest | 61 | コンポーネント統合 |
| SkillLoaderTest | 16 | スキルローダー |
| 統合テスト群 | 50+ | ターゲティング、レベルアップ等 |
| **合計** | **334** | 全テストパス |

---

## Critical Files

### メインクラス
- `RPGPlugin.java` - メインクラス
- `RPGCommand.java` - コマンドハンドラ
- `pom.xml` - Maven依存関係
- `plugin.yml` - プラグイン設定

### 設定ファイル
- `src/main/resources/config.yml` - メイン設定
- `skills/` - スキル定義YAML
- `classes/` - クラス定義YAML
