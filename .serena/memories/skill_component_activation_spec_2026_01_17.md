# スキルコンポーネント発動ロジック仕様

**作成日**: 2026-01-17
**ブランチ**: main

## コンポーネントタイプ一覧

| タイプ | 説明 | 登録済みコンポーネント |
|--------|------|---------------------|
| TARGET | ターゲットを選択 | SELF, SINGLE, CONE, SPHERE, SECTOR, AREA, LINE, NEAREST_HOSTILE |
| MECHANIC | 実際の効果を適用 | damage, heal, push, fire, message, potion, lightning, sound, command, explosion, speed, particle, launch, delay, cleanse, channel |
| CONDITION | ターゲットを条件でフィルタリング | health, chance, mana, biome, class, time, armor, fire, water, combat, potion, status, tool, event |
| FILTER | ターゲット属性でフィルタリング | entity_type, group |
| TRIGGER | スキル発動のトリガー | CAST, CROUCH, LAND, DEATH, KILL, PHYSICAL_DEALT, PHYSICAL_TAKEN, LAUNCH, ENVIRONMENTAL |
| COST | スキル発動のコスト | MANA, HP, STAMINA, ITEM |
| COOLDOWN | スキルのクールダウン | COOLDOWN |

## 処理フロー

```
SkillManager.executeSkill()
  └─> SkillExecutor.executeSkill()
      └─> コスト消費チェック
      └─> クールダウンチェック
      └─> ComponentEffectExecutor.castWithTriggers()
          └─> collectTriggerHandlers()
          └─> CASTトリガー: 即時実行
          └─> 他トリガー: TriggerManager.activateSkill()
```

## コンポーネント実行パターン

**TARGET**: selectTargets() → executeChildren(selected)
- ターゲットを選択してリストを置換
- 選択されたターゲットで子を実行

**MECHANIC**: for each target → apply() → executeChildren()
- 全ターゲットに効果を適用
- 効果適用成功時のみ子を実行

**CONDITION**: filter(targets) → executeChildren(filtered)
- 条件を満たすターゲットのみフィルタリング
- フィルタ後のターゲットで子を実行

## 親子関係のルール

| 親 | 可能な子 |
|----|---------|
| TARGET | MECHANIC, CONDITION, FILTER |
| MECHANIC | MECHANIC, CONDITION, FILTER |
| CONDITION | MECHANIC, CONDITION, FILTER |
| FILTER | MECHANIC, CONDITION, FILTER |
| TRIGGER | 全て |
| COST | 子を持たない（ルートのみ） |
| COOLDOWN | 子を持たない（ルートのみ） |

## エラー条件

| 条件 | 挙動 |
|-------|------|
| typeフィールド欠落 | コンポーネントスキップ |
| 不明なコンポーネントタイプ | コンポーネントスキップ |
| 循環参照（深さ50以上） | ロード失敗 |
| ターゲットなし | falseを返し、子は実行されない |
| コスト不足 | スキル発動キャンセル |
| クールダウン中 | スキル発動キャンセル |

## YAML基本構造

```yaml
skill_id: "fireball"
components:
  - type: SINGLE
    range: 20.0
    components:
      - type: damage
        value: "10 + level * 2"
      - type: fire
        duration: 100
      - type: chance
        value: 0.5
        components:
          - type: lightning
```

## 重要なソースファイル

- SkillEffect.java: コンポーネントツリーのルート
- EffectComponent.java: 基底クラス（executeChildrenメソッド）
- TargetComponent.java: selectTargets()実装
- MechanicComponent.java: apply()実装
- ConditionComponent.java: test()によるフィルタリング
- ComponentRegistry.java: 全コンポーネントの登録
- ComponentLoader.java: YAMLロードとバリデーション
