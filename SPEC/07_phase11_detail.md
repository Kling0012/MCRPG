# フェーズ11: スキルシステム改善（進行中）

## 概要
既存のスキルシステムを大幅に拡張し、YAML側で完全に挙動を制御可能にする。

## 新規要件

### 1. 数式エバリュエーター
- **目的**: YAML内で記述された数式文字列を実行時に評価
- **サポートする変数**:
  - ステータス: STR, INT, SPI, VIT, DEX
  - スキルレベル: Lv
  - プレイヤーレベル: LV (新規)
  - カスタム変数: YAML内で定義可能
- **演算子**: 四則演算、括弧、べき乗
- **例**: `"STR * DEX + (Lv * 5)"`, `"base_damage * STR"`

### 2. レベル依存パラメータ
- **クールダウン**: レベルアップごとの変化量（増減両対応）
- **マナコスト/HPコスト**: レベルアップごとの変化量
- **ダメージ**: レベルごとの個別定義、または共通式（フォールバック）

### 3. MPステータスとPlayer拡張
- **MP（マナポイント）**: 新規ステータス追加
- **HP総量**: 変更可能にする
- **コストタイプ**: MP消費 or HP消費を選択可能
- **切替**: 外部トリガーで変更可能

### 4. エフェクト範囲システム
- **範囲タイプ**:
  - 単体（最も近いMOB）
  - 前方扇状（角度指定）
  - 前方四角形（幅・奥行き指定）
  - 周囲円形（半径指定）
- **ターゲット**: 自分自身、最も近い敵対MOB、範囲内全エンティティ
- **外部トリガー**: ターゲットを外部から指定可能

### 5. 外部API拡張
- ターゲット取得/設定API
- 指定ターゲットでスキル発動
- コストタイプ指定発動
- 範囲内エンティティ取得
- SKript連携: `Skill Targeted` 構文対応

## 新YAMLフォーマット

```yaml
# スキル定義例（新フォーマット）
id: power_strike_v2
name: パワーストライク改
display_name: "&6パワーストライク改"
type: active
max_level: 5

# カスタム変数（数式内で使用可能）
variables:
  str_multiplier: 1.5
  base_damage: 50.0

# コスト（レベルで変化、MP/HP選択可能）
cost:
  type: mana           # mana or hp
  base: 10
  per_level: -1        # レベル毎に-1消費
  min: 0               # 最低コスト

# クールダウン（レベルで短縮）
cooldown:
  base: 5.0
  per_level: -0.5
  min: 1.0

# ダメージ（数式で記述）
damage:
  formula: "base_damage + STR * str_multiplier + (Lv * 10)"
  # レベル別定義も可能
  level_formulas:
    3: "base_damage * 1.5 + STR * str_multiplier"

# ターゲット・範囲
targeting:
  type: cone            # single, cone, rect, circle
  cone:
    angle: 90           # 扇状の角度
    range: 5.0
  select_nearest: true  # 範囲内で最も近いMOB優先

# スキルツリー設定
skill_tree:
  parent: none
  unlock_requirements:
    - type: level
      value: 5
    - type: stat
      stat: STRENGTH
      value: 20
  cost: 1

icon_material: IRON_SWORD
available_classes:
  - Warrior
  - Knight
```

## 実装ファイル構成

```
com.example.rpgplugin
├── skill/
│   ├── evaluator/                    # 数式エバリュエーター
│   │   ├── FormulaEvaluator.java     # 新規: 数式評価
│   │   ├── VariableContext.java      # 新規: 変数コンテキスト
│   │   └── ExpressionParser.java     # 新規: 式パーサー
│   ├── target/                       # ターゲット選択
│   │   ├── TargetType.java           # 新規 enum: ターゲット種別
│   │   ├── TargetSelector.java       # 新規: ターゲット選択ロジック
│   │   ├── AreaShape.java            # 新規 enum: 範囲形状
│   │   └── ShapeCalculator.java      # 新規: 幾何計算
│   ├── LevelDependentParameter.java  # 新規: レベル依存パラメータ
│   ├── SkillCostType.java            # 新規 enum: MANA, HP
│   ├── SkillExecutionConfig.java     # 新規: 実行時オプション
│   ├── Skill.java                    # 修正: 内部クラス拡張
│   ├── SkillLoader.java              # 修正: 新フォーマット対応
│   └── SkillManager.java             # 修正: 実行ロジック更新
├── player/
│   ├── ManaManager.java              # 新規: MP管理
│   └── RPGPlayer.java                # 修正: MPアクセサ追加
├── storage/
│   ├── models/
│   │   └── PlayerData.java           # 修正: maxMana, currentMana, maxHealth
│   └── migrations/
│       └── Migration_v1_1_PlayerStats.java  # 新規: マイグレーション
└── api/
    ├── RPGPluginAPI.java             # 修正: ターゲット系メソッド追加
    └── bridge/
        └── SKriptBridge.java         # 修正: 新規アクション追加
```

## データベーススキーマ変更

```sql
-- プレイヤーデータにMP/HP拡張を追加
ALTER TABLE player_data ADD COLUMN max_health INTEGER DEFAULT 20;
ALTER TABLE player_data ADD COLUMN max_mana INTEGER DEFAULT 100;
ALTER TABLE player_data ADD COLUMN current_mana INTEGER DEFAULT 100;
```

## タスクリスト

| タスク | 内容 |
|--------|------|
| Phase11-1 | 数式エバリュエーター実装（カスタム変数・レベル依存対応） |
| Phase11-2 | レベル依存パラメータシステム（CD/マナ/HPコスト） |
| Phase11-3 | MPステータス追加とPlayerクラス拡張（HP/MP消費切替） |
| Phase11-4 | スキルエフェクト範囲システム（扇状/四角/円/単体） |
| Phase11-5 | 外部API拡張（ターゲット・SKript連携） |
| Phase11-6 | YAMLローダー拡張と新スキル定義フォーマット実装 |
| Phase11-7 | SkillManagerのスキル実行ロジック更新（数式・範囲・コスト対応） |
| Phase11-8 | データベーススキーマ更新とマイグレーション（MP/HP拡張） |
| Phase11-9 | ユニットテスト実装（数式・範囲・ローダー等） |
| Phase11-10 | 結合テストとインテグレーションテスト実装 |
| Phase11-11 | サンプルスキルYAML作成とドキュメント更新 |
| Phase11-12 | 既存スキルデータのマイグレーションと変換ツール実装 |

## SKript連携例

```skript
# ターゲット取得
set {_target} to rpg api get_target %player%

# 指定ターゲットでスキル発動
execute player command "rpg api cast_at fireball %{_target}%"

# 範囲内エンティティ取得
execute player command "rpg api get_entities_in_area circle 5.0 %player%"

# コストタイプ指定発動
execute player command "rpg api cast_with_cost blood_weapon hp"
```
