# スキルコンポーネント仕様書 レビュー結果

## レビュー日時
2026-01-18

## レビューアー
- iflow: 仕様書の構成とパラメータ名の不一致を指摘
- Codex: パラメータ名の不一致と数式構文/プレースホルダーの不足を指摘
- Opencode: 環境制限によりレビュー未完了

## 主な問題点

### P0（Critical）- パラメータ名の不一致

| コンポーネント | 仕様書（誤） | 実装（正） |
|---------------|------------|------------|
| health CONDITION | min, max | min-value, max-value |
| potion MECHANIC | type | potion |
| command MECHANIC | as_op | type |
| damage MECHANIC | stat_multiplier（存在しない） | 削除 |

### P1（High）- ドキュメント不足
- 数式構文の仕様が不明瞭
- プレースホルダー一覧が存在しない

## 実施した修正

### 1. パラメータ名修正
- health CONDITION: `min/max` → `min-value/max-value`
- health CONDITION: `type` パラメータ追加（value/percent/difference/difference percent）
- potion MECHANIC: `type` → `potion`
- potion MECHANIC: `duration` 単位修正（tick → 秒）
- potion MECHANIC: `ambient` パラメータ追加
- command MECHANIC: `as_op` → `type`（op/console/player）
- damage MECHANIC: `stat_multiplier` 削除、`type`/`true-damage` 追加

### 2. 数式構文セクション追加
- スケーリング形式: `base + (level - 1) * scale`
- パラメータ: `{key}-base`, `{key}-scale`
- 計算例と省略時の動作

### 3. プレースホルダー一覧追加
- command: `{caster}`, `{target}`, `{level}`, `{x}`, `{y}`, `{z}`, `{world}`
- message: `{player}`, `{target}`

## 参照ファイル
- `src/main/java/com/example/rpgplugin/skill/component/condition/HealthCondition.java`
- `src/main/java/com/example/rpgplugin/skill/component/mechanic/PotionMechanic.java`
- `src/main/java/com/example/rpgplugin/skill/component/mechanic/CommandMechanic.java`
- `src/main/java/com/example/rpgplugin/skill/component/mechanic/DamageMechanic.java`
- `src/main/java/com/example/rpgplugin/skill/component/EffectComponent.java`
