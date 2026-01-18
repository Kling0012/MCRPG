# スキルコンポーネント発動ロジック仕様書

**作成日**: 2026-01-17
**バージョン**: 1.0.0
**対象**: RPGPlugin スキルシステム

---

## 1. 概要

本仕様書は、RPGPluginのスキルコンポーネントシステムのYAML定義から実際のスキル発動までの処理フロー、コンポーネント間の関係性、エラー条件、組み合わせ自由度について記述する。

---

## 2. コンポーネントタイプ一覧

| タイプ | 説明 | 登録済みコンポーネント |
|--------|------|---------------------|
| **TARGET** | ターゲットを選択 | SELF, SINGLE, CONE, SPHERE, SECTOR, AREA, LINE, NEAREST_HOSTILE |
| **MECHANIC** | 実際の効果を適用 | damage, heal, push, fire, message, potion, lightning, sound, command, explosion, speed, particle, launch, delay, cleanse, channel |
| **CONDITION** | ターゲットを条件でフィルタリング | health, chance, mana, biome, class, time, armor, fire, water, combat, potion, status, tool, event |
| **FILTER** | ターゲット属性でフィルタリング | entity_type, group |
| **TRIGGER** | スキル発動のトリガー | CAST, CROUCH, LAND, DEATH, KILL, PHYSICAL_DEALT, PHYSICAL_TAKEN, LAUNCH, ENVIRONMENTAL |
| **COST** | スキル発動のコスト | MANA, HP, STAMINA, ITEM |
| **COOLDOWN** | スキルのクールダウン | COOLDOWN |

---

## 3. YAML構造と処理フローのマッピング

### 3.1 基本YAML構造

```yaml
skill_id: "fireball"
components:
  - type: SINGLE  # TARGETコンポーネント
    key: "main_target"
    range: 20.0
    components:   # 子コンポーネント（ツリー構造）
      - type: damage  # MECHANIC
        value: "10 + level * 2"
      - type: fire   # MECHANIC
        duration: 100
      - type: chance # CONDITION
        value: 0.5
        components:
          - type: lightning  # 条件満た時のみ実行
```

### 3.2 処理フロー

```
┌─────────────────────────────────────────────────────────────────┐
│  スキル発動フロー                                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. SkillManager.executeSkill()                                 │
│     └─> SkillExecutor.executeSkill()                            │
│         └─> コスト消費チェック                                  │
│         └─> クールダウンチェック                                │
│         └─> ComponentEffectExecutor.castWithTriggers()          │
│                                                                  │
│  2. ComponentEffectExecutor.castWithTriggers()                  │
│     ├─> collectTriggerHandlers()  - トリガー収集                │
│     ├─> CASTトリガー: 即時実行                                  │
│     └─> 他トリガー: TriggerManager.activateSkill()              │
│                                                                  │
│  3. SkillEffect.execute(caster, level, targets)                 │
│     └─> components.forEach(c -> c.execute())                    │
│                                                                  │
│  4. 各コンポーネントのexecute()パターン:                        │
│                                                                  │
│     TARGET: selectTargets() → executeChildren(selected)        │
│     ├─> ターゲットを選択してリストを置換                        │
│     └─> 選択されたターゲットで子を実行                          │
│                                                                  │
│     MECHANIC: for each target → apply() → executeChildren()     │
│     ├─> 全ターゲットに効果を適用                                │
│     └─> 効果適用成功時のみ子を実行                              │
│                                                                  │
│     CONDITION: filter(targets) → executeChildren(filtered)     │
│     ├─> 条件を満たすターゲットのみフィルタリング                │
│     └─> フィルタ後のターゲットで子を実行                        │
│                                                                  │
│     FILTER: similar to CONDITION                                │
│                                                                  │
│     TRIGGER: TriggerManagerに登録（即時実行はCASTのみ）         │
│                                                                  │
│     COST/COOLDOWN: スキル実行前にチェック                       │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 3.3 実行順序のルール

1. **ルートレベルのコンポーネント**はYAML記述順に実行
2. **親コンポーネント**は**子コンポーネント**より先に実行
3. **TARGET**はターゲット選択後に子を実行
4. **CONDITION**はフィルタリング後に子を実行（通過したターゲットがない場合、子は実行されない）
5. **MECHANIC**は効果適用成功時のみ子を実行

---

## 4. コンポーネント間の関係性

### 4.1 親子関係のルール

| 親コンポーネント | 可能な子コンポーネント | 制約 |
|-----------------|---------------------|------|
| **TARGET** | MECHANIC, CONDITION, FILTER | ターゲット選択後の効果実行 |
| **MECHANIC** | MECHANIC, CONDITION, FILTER | 効果適用成功時に実行 |
| **CONDITION** | MECHANIC, CONDITION, FILTER | 条件満た時のみ実行 |
| **FILTER** | MECHANIC, CONDITION, FILTER | フィルタ後のターゲットに実行 |
| **TRIGGER** | 全て | トリガー発火時の子実行 |
| **COST** | 子を持たない（ルートレベルのみ） | - |
| **COOLDOWN** | 子を持たない（ルートレベルのみ） | - |

### 4.2 ツリー構造のパターン

```yaml
# パターン1: 単一ターゲット + 効果
components:
  - type: SINGLE
    range: 10
    components:
      - type: damage
        value: "10 + level * 2"

# パターン2: 範囲ターゲット + 条件フィルタ + 効果
components:
  - type: AREA
    radius: 5
    components:
      - type: chance      # 条件: 50%の確率
        value: 0.5
        components:
          - type: damage  # 条件満た時のみダメージ
            value: 20

# パターン3: 複数ターゲットセット
components:
  - type: SINGLE          # メインターゲット
    range: 20
    components:
      - type: damage
        value: 30
  - type: AREA            # 周囲にも範囲ダメージ
    radius: 3
    components:
      - type: damage
        value: 10
```

---

## 5. エラー条件とエラーパターン

### 5.1 ロード時エラー（ComponentLoader.validate）

| エラー条件 | エラーメッセージ | 影響 |
|-----------|-----------------|------|
| `type`フィールド欠落 | "Component missing 'type' field" | コンポーネントがスキップ |
| 不明なコンポーネントタイプ | "Unknown component type: {type}" | コンポーネントがスキップ |
| 循環参照（深さ50以上） | "Component tree too deep" | ロード失敗 |

### 5.2 実行時エラー

| エラー条件 | 挙動 | 回復方法 |
|-----------|------|----------|
| ターゲットがいない | TARGETがfalseを返し、子は実行されない | - |
| コスト不足 | スキル発動キャンセル | - |
| クールダウン中 | スキル発動キャンセル | - |
| 無効なエンティティ | ターゲットリストから除外 | - |
| 必要パラメータ欠落 | デフォルト値使用 | ComponentSettingsのfallback |

### 5.3 エラーハンドリング実装

**SkillEffect.execute** (lines 70-78):
```java
public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
    boolean worked = false;
    for (EffectComponent component : components) {
        if (component.execute(caster, level, targets)) {
            worked = true;
        }
    }
    return worked;  // いずれかが成功すればtrue
}
```

**TargetComponent.execute** (lines 43-53):
```java
public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
    List<LivingEntity> selectedTargets = selectTargets(caster, level);
    if (selectedTargets.isEmpty()) {
        return false;  // ターゲットなし = 失敗
    }
    return executeChildren(caster, level, selectedTargets);
}
```

---

## 6. コンポーネントの組み合わせ自由度

### 6.1 自由な組み合わせが可能なケース

| 組み合わせ | 例 |
|-----------|---|
| TARGET → MECHANIC | SINGLE → damage |
| TARGET → CONDITION → MECHANIC | SINGLE → chance → damage |
| TARGET → MECHANIC → MECHANIC | SINGLE → fire → damage |
| TARGET → CONDITION → CONDITION → MECHANIC | SINGLE → health → mana → heal |
| 複数の独立ターゲットツリー | SINGLEセット + AREAセット |

### 6.2 制約のある組み合わせ

| 制約 | 説明 |
|------|------|
| COSTはルートレベルのみ | 他コンポーネントの子にはなれない |
| COOLDOWNはルートレベルのみ | 他コンポーネントの子にはなれない |
| TRIGGERの効果はトリガー発火時のみ | CASTは即時、他は遅延実行 |
| TARGETは子のターゲットを置換 | 子コンポーネントは親が選択したターゲットを使用 |

### 6.3 注意点

1. **複数のTARGET**: 同じレベルに複数のTARGETがある場合、それぞれ独立して実行される
2. **CONDITIONの連鎖**: 全てのCONDITIONを満たす必要がある（AND条件）
3. **MECHANICの連鎖**: 前のMECHANICが成功した場合のみ次が実行される

---

## 7. 各コンポーネントの詳細仕様

### 7.1 TARGETコンポーネント

| コンポーネント | パラメータ | デフォルト | 説明 |
|---------------|-----------|-----------|------|
| **SINGLE** | range | 10.0 | ターゲット選択範囲 |
| | ray_trace | false | 視線判定 |
| **CONE** | angle | 90 | 扇状の角度 |
| | range | 10.0 | 範囲 |
| **SPHERE** | radius | 5.0 | 半径 |
| **SECTOR** | angle | 90 | 角度 |
| | range | 10.0 | 範囲 |
| **AREA** | area_shape | CIRCLE | 形状（CIRCLE/RECTANGLE） |
| | radius | 10.0 | 半径 |
| | width | 10.0 | 幅（RECTANGLE用） |
| | depth | 10.0 | 奥行き（RECTANGLE用） |
| | max_targets | 15 | 最大ターゲット数 |
| | max_targets_per_level | 0 | レベルごとの増加量 |
| **LINE** | length | 10.0 | 長さ |
| | width | 2.0 | 幅 |
| **NEAREST_HOSTILE** | range | 10.0 | 検索範囲 |

### 7.2 MECHANICコンポーネント

| コンポーネント | パラメータ | デフォルト | 説明 |
|---------------|-----------|-----------|------|
| **damage** | value | "0" | ダメージ数式 |
| | stat_multiplier | - | ステータス倍率 |
| | multiplier | 1.0 | 固定倍率 |
| **heal** | value | "0" | 回復量数式 |
| **push** | velocity | 1.0 | 吹き飛ばし強さ |
| **fire** | duration | 100 | 燃焼時間（tick） |
| **potion** | type | - | ポーション効果 |
| | duration | 100 | 効果時間 |
| | amplifier | 0 | 強度 |
| **command** | command | - | 実行コマンド |
| | as_op | false | OP権限で実行 |
| **explosion** | power | 3.0 | 爆発力 |
| **delay** | delay | 20 | 遅延時間（tick） |

### 7.3 CONDITIONコンポーネント

| コンポーネント | パラメータ | デフォルト | 説明 |
|---------------|-----------|-----------|------|
| **health** | min | 0.0 | 最小HP割合 |
| | max | 1.0 | 最大HP割合 |
| **chance** | value | 0.5 | 発動確率 |
| **mana** | min | 0 | 最小MP |
| **biome** | biome | - | 必要バイオーム |
| **class** | class | - | 必要クラス |

### 7.4 TRIGGERコンポーネント

| トリガー | タイミング | 即時/遅延 |
|---------|---------|-----------|
| **CAST** | スキル使用時 | 即時 |
| **CROUCH** | スニーク時 | 遅延 |
| **LAND** | 着地時 | 遅延 |
| **DEATH** | 死亡時 | 遅延 |
| **KILL** | キル時 | 遅延 |
| **PHYSICAL_DEALT** | 物理ダメージ与えた時 | 遅延 |
| **PHYSICAL_TAKEN** | 物理ダメージ受けた時 | 遅延 |

---

## 8. YAML例と対応する処理

### 8.1 基本的な火の玉スキル

```yaml
skill_id: "fireball"
display_name: "ファイアボール"
components:
  - type: SINGLE      # ターゲット選択
    range: 20.0
    components:
      - type: damage   # ダメージ適用
        value: "10 + level * 2"
      - type: fire     # 燃焼効果
        duration: 100
```

**処理フロー**:
1. SINGLEコンポーネントが範囲20以内のターゲットを選択
2. 選択されたターゲットにdamageが適用
3. 選択されたターゲットにfireが適用

### 8.2 確率付きヒールスキル

```yaml
skill_id: "prayer"
display_name: "祈り"
components:
  - type: SELF        # 自分自身
    components:
      - type: chance   # 50%の確率
        value: 0.5
        components:
          - type: heal # 成功時のみ回復
            value: "20 + level * 5"
```

**処理フロー**:
1. SELFコンポーネントがキャスター自身をターゲット
2. chanceコンポーネントが50%の確率でフィルタリング
3. 成功した場合のみhealが適用

### 8.3 範囲攻撃スキル

```yaml
skill_id: "whirlwind"
display_name: "旋風"
components:
  - type: AREA        # 範囲ターゲット
    radius: 5.0
    max_targets: 10
    components:
      - type: damage   # ダメージ
        value: "5 + level * 1.5"
      - type: push     # 吹き飛ばし
        velocity: 2.0
```

**処理フロー**:
1. AREAコンポーネントが半径5以内の最大10体を選択
2. 選択されたターゲット全員にdamageを適用
3. damage成功後、選択されたターゲット全員にpushを適用

---

## 9. エディタ開発のためのガイドライン

### 9.1 YAML出力形式

エディタは以下の形式でYAMLを出力する必要がある:

```yaml
skill_id: "example"
display_name: "スキル名"
components:
  - type: COMPONENT_TYPE
    key: "unique_key"
    param1: value1
    param2: value2
    components:
      - type: CHILD_TYPE
        param1: value1
```

### 9.2 バリデーション要件

| チェック項目 | 方法 |
|-----------|------|
| typeフィールドの存在 | 必須 |
| 有効なコンポーネントタイプ | ComponentRegistry.hasComponent() |
| 循環参照 | 深さカウント（最大50） |
| 必須パラメータ | 各コンポーネントの仕様に従う |

### 9.3 UI推奨事項

1. **ツリービュー**: 親子関係を視覚化
2. **ドラッグ&ドロップ**: コンポーネントの移動・親子変更
3. **リアルタイムバリデーション**: 不正な組み合わせを即座に通知
4. **プレビュー**: YAML出力のリアルタイム確認

---

## 10. 付録: ソースコード参照

| クラス | 役割 | ファイル |
|-------|------|----------|
| SkillEffect | コンポーネントツリーのルート | skill/component/SkillEffect.java |
| EffectComponent | コンポーネント基底クラス | skill/component/EffectComponent.java |
| TargetComponent | ターゲット選択基底 | skill/component/target/TargetComponent.java |
| MechanicComponent | 効果適用基底 | skill/component/mechanic/MechanicComponent.java |
| ConditionComponent | 条件フィルタ基底 | skill/component/condition/ConditionComponent.java |
| ComponentRegistry | コンポーネント管理 | skill/component/trigger/ComponentRegistry.java |
| ComponentLoader | YAMLからのロード | skill/component/ComponentLoader.java |
| ComponentEffectExecutor | コンポーネント実行 | skill/component/ComponentEffectExecutor.java |
| SkillManager | スキル管理ファサード | skill/SkillManager.java |

---

*この仕様書はエディタ開発のために作成されました。*
