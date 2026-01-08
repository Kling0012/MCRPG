# Phase11 スキルシステム拡張 - 最新仕様

## 更新日時
2026-01-08

## 概要
Phase11はスキルシステムの包括的な拡張を行うフェーズ。現在Phase11-4～Phase11-8が統合されている。

## Phase11-2: レベル依存パラメータ
- `LevelDependentParameter` クラス追加
- クールダウン、コストがスキルレベルに応じて変化
- YAML形式: `{ base: 5.0, per_level: -0.5, min: 1.0, max: 10.0 }`

## Phase11-4: スキルエフェクト範囲システム（targetパッケージ）
- `target/` パッケージ追加
- `SkillTarget` クラス: ターゲット選択設定
- `TargetSelector` クラス: ターゲット選択ロジック
- `ShapeCalculator` クラス: 範囲形状計算
- サポート対象: `SINGLE`, `CONE`, `RECT`, `CIRCLE`
- ターゲットタイプ: `SELF`, `NEAREST_HOSTILE`, `NEAREST_ENTITY`, `AREA`

## Phase11-5: 外部API拡張
- ターゲット・SKript連携
- 外部プラグインからスキルターゲット機能にアクセス可能

## Phase11-6: 新YAMLフォーマット対応
### カスタム変数
```yaml
variables:
  base_mod: 1.0
  str_scale: 1.5
```

### 数式ダメージ
```yaml
damage:
  formula: "STR * str_scale + (Lv * 5) + base_mod * 10"
  levels:
    1: "STR * 2"
    5: "STR * 3"
    10: "STR * 5"
```

### ターゲット設定
```yaml
targeting:
  type: cone
  cone:
    angle: 90
    range: 5.0
```

### targetパッケージ統合
```yaml
target:
  type: nearest_hostile
  area_shape: cone
  cone:
    angle: 90
    range: 5.0
```

## Phase11-7: SkillManager更新
- `SkillExecutionResult` クラス追加
- `executeSkill()` メソッド拡張
- `selectTargetsWithSkillTarget()` メソッド追加（Phase11-4統合）
- `CostConsumptionResult` クラス追加

## Phase11-8: データベーススキーマ更新
- マイグレーションシステム実装
- スキルデータ永続化対応

## パッケージ構造
```
com.example.rpgplugin.skill/
├── Skill.java              # メインデータモデル（Phase11-6+11-4統合済み）
├── SkillManager.java       # スキル管理・実行（Phase11-7更新済み）
├── SkillLoader.java        # YAMLローダー（Phase11-6+11-4対応済み）
├── SkillType.java          # ACTIVE/PASSIVE
├── SkillCostType.java      # MANA/HP
├── SkillExecutionConfig.java
├── SkillTree.java
├── SkillNode.java
├── LevelDependentParameter.java  # Phase11-2
├── config/                 # 設定関連
├── target/                 # Phase11-4: ターゲット・範囲システム
│   ├── SkillTarget.java
│   ├── TargetSelector.java
│   ├── TargetType.java
│   ├── AreaShape.java
│   └── ShapeCalculator.java
├── evaluator/              # Phase11-6: 数式エバリュエーター
│   ├── FormulaEvaluator.java
│   └── FormulaDamageCalculator.java
└── executor/               # スキル実行
```

## コストタイプ
- `MANA`: MP消費（TODO: MPシステム実装後に対応）
- `HP`: HP消費

## YAML例（完全版）
```yaml
id: fireball
name: "ファイアボール"
display_name: "&cファイアボール"
type: active
max_level: 10

# レガシーマナコスト（後方互換性）
mana_cost: 10

# 新しいコスト形式
cost:
  type: mana
  base: 10
  per_level: -1
  min: 0

# クールダウン
cooldown:
  base: 5.0
  per_level: -0.5
  min: 1.0

# カスタム変数
variables:
  base_mod: 1.0
  str_scale: 1.5

# ダメージ（数式形式）
damage:
  formula: "STR * str_scale + (Lv * 5) + base_mod * 10"

# Phase11-6 ターゲット設定
targeting:
  type: cone
  cone:
    angle: 90
    range: 5.0

# Phase11-4 targetパッケージ統合
target:
  type: nearest_hostile
  area_shape: cone
  cone:
    angle: 90
    range: 5.0

skill_tree:
  parent: none
  cost: 1

icon_material: BLAZE_ROD
available_classes: []
```

## 現在の変更中ファイル（git status）
- src/main/java/com/example/rpgplugin/skill/Skill.java
- src/main/java/com/example/rpgplugin/skill/SkillLoader.java
- src/main/java/com/example/rpgplugin/skill/SkillManager.java

## 次のステップ
- Phase11-8 データベーススキーマ更新とマイグレーションの完全実装
- MPシステムとの統合（コスト消費の完全実装）
- PvP設定の実装
- MythicMobs連携の詳細判定
