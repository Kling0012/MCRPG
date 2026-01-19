# スキルコンポーネント発動ロジック仕様書

**作成日**: 2026-01-17
**最終更新日**: 2026-01-18
**バージョン**: 1.6.0
**対象**: RPGPlugin スキルシステム

---

## ドキュメント変更履歴

| バージョン | 日付 | 変更内容 | 作成者/レビュアー |
|-----------|------|----------|------------------|
| 1.0.0 | 2026-01-17 | 初版作成 | Claude Code |
| 1.1.0 | 2026-01-18 | パラメータ名修正、数式構文追加、プレースホルダー一覧追加 | Claude Code (Codexレビュー反映) |
| 1.2.0 | 2026-01-18 | パラメータテーブル拡張、YAML例と数式構文の統一、単位の整合性修正 | Claude Code (iflow/geminiレビュー反映) |
| 1.3.0 | 2026-01-18 | ソースコード実装との照合によるパラメータ追加・修正 | Claude Code (実装照合レビュー) |
| 1.4.0 | 2026-01-18 | 全コンポーネント網羅による仕様拡張（MECHANIC 9件追加、CONDITION 9件追加、FILTER 2件追加） | Claude Code (全実装照合レビュー) |
| 1.5.0 | 2026-01-18 | デフォルト値・制約値の実装との整合性修正（delay/launch/time/armor） | Claude Code (再レビュー・パラメータ検証) |
| 1.6.0 | 2026-01-18 | TARGETコンポーネントにrelative_toパラメータ追加（ネストされたTARGET対応） | Claude Code (実装修正・仕様拡張) |

### v1.1.0での主な修正点

**P0（Critical）- パラメータ名の不一致修正:**
- health CONDITION: `min/max` → `min-value`/`max-value`
- health CONDITION: `type` パラメータを追加（比較タイプ指定）
- potion MECHANIC: `type` → `potion`
- potion MECHANIC: `duration` 単位を秒に修正（tick → 秒）
- potion MECHANIC: `ambient` パラメータを追加
- command MECHANIC: `as_op` → `type`（値は op/console/player）
- damage MECHANIC: 存在しない `stat_multiplier` を削除
- damage MECHANIC: `type`、`true-damage` パラメータを追加

**P1（High）- ドキュメント追加:**
- セクション8: 数式構文（Value Expression）を追加
  - スケーリング形式: `base + (level - 1) * scale`
  - 計算例と省略時の動作を記載
- セクション8.2: プレースホルダー一覧を追加
  - command MECHANIC: `{caster}`, `{target}`, `{level}`, `{x}`, `{y}`, `{z}`, `{world}`
  - message MECHANIC: `{player}`, `{target}`

### v1.2.0での主な修正点

**P0（Critical）- パラメータテーブル拡張:**
- セクション7.1-7.3: 全パラメータテーブルに「型」「必須」「デフォルト」「制約」列を追加
- TARGETコンポーネント: SELFを追加、各パラメータの型・制約を記載
- MECHANICコンポーネント: message MECHANICのパラメータを追加
- CONDITIONコンポーネント: mana, biome, classのパラメータを追加

**P0（Critical）- YAML例と数式構文の整合性修正:**
- セクション3.1、4.2、9.1-9.3: 古い形式 `value: "10 + level * 2"` を廃止
- 全YAML例を新しい `value-base`/`value-scale` 形式に統一
- fire MECHANIC: `duration: 100` (tick) → `seconds: 3` に修正

**P1（High）- 単位の整合性修正:**
- fire MECHANIC: 秒単位(`seconds`)を推奨、tick単位(`ticks`)を代替として記載
- potion MECHANIC: 秒単位で統一

### v1.3.0での主な修正点

**P0（Critical）- 実装との照合によるパラメータ追加・修正:**

TARGETコンポーネント:
- SINGLE: `range_per_level`, `select_nearest`, `target_self`, `hostile_only` を追加
- CONE: `angle_per_level`, `range_per_level`, `max_targets`, `max_targets_per_level` を追加
- SPHERE: `radius_per_level`, `max_targets`, `max_targets_per_level`, `include_caster` を追加
- SECTOR: デフォルト値を `angle: 60.0`, `range: 8.0` に修正、`angle_per_level`, `radius_per_level`, `max_targets`, `max_targets_per_level` を追加
- LINE: デフォルト値を `length: 15.0` に修正、`length_per_level`, `width_per_level`, `max_targets`, `max_targets_per_level` を追加
- NEAREST_HOSTILE: デフォルト値を `range: 15.0` に修正、`range_per_level` を追加
- AREA: `include_caster` を追加

MECHANICコンポーネント:
- push: パラメータ名 `velocity` → `speed` に変更、`vertical` パラメータを追加
- heal: `type` パラメータを追加（enum: value/percent/percent missing）

CONDITIONコンポーネント:
- chance: パラメータ名 `value` → `chance` に変更、デフォルト値を `50` に変更、範囲を `0-100`（百分率）に変更
- health: type enum に `difference percent` を追加
- class: `exact` パラメータを追加
- biome: 部分一致であることを明記

### v1.4.0での主な修正点

**P0（Critical）- 全コンポーネント網羅による仕様拡張:**

MECHANICコンポーネント（9件追加）:
- **speed**: 移動速度上昇効果を追加（`duration`, `amplifier`, `ambient`）
- **launch**: 投射物発射を追加（`projectile`, `speed`, `spread`）
- **sound**: サウンド再生を追加（`sound`, `volume`, `pitch`）
- **particle**: パーティクル表示を追加（`particle`, `count`, `offset`, `speed`）
- **cleanse**: ポーション効果解除を追加（`bad_only`, `potion`）
- **lightning**: 稲妻落としを追加（`damage`, `forward`, `right`）
- **explosion**: 爆発効果を追加（`power`, `fire`, `damage`）
- **delay**: 遅延実行を追加（`delay`, `ticks`）
- **channel**: チャネル詠唱を追加（`duration`, `ticks`）

CONDITIONコンポーネント（9件追加）:
- **armor**: 装備防具による条件判定（`material`, `slot`）
- **time**: 時刻による条件判定（`time`: day/night/dawn/dusk/noon/midnight）
- **fire**: 燃焼中による条件判定（`ticks`）
- **water**: 水中による条件判定（`depth`）
- **combat**: 戦闘状態による条件判定（`mode`, `seconds`）
- **potion**: ポーション効果有無による条件判定（`potion`, `min_level`）
- **status**: ステータス値による条件判定（`stat`, `min`, `max`, `percent`）
- **tool**: 手持ちツールによる条件判定（`material`, `hand`）
- **event**: イベント発火による条件判定（`event`, `duration`）

FILTERコンポーネント（2件追加）:
- **entity_type**: エンティティタイプによるフィルタリング（`type`: SELF/PLAYER/MOB/ALL）
- **group**: 敵味方グループによるフィルタリング（`group`: ENEMY/ALLY/BOTH）

その他:
- セクション2 コンポーネント一覧を完全な実装状態に更新
- セクション7.2-7.3-7.5のテーブルを拡張し全パラメータを網羅
- セクション9.3のYAML例で誤った `velocity` を `speed` に修正

### v1.5.0での主な修正点

**P0（Critical）- デフォルト値・制約値の実装との整合性修正:**

MECHANICコンポーネント:
- launch: `spread` デフォルト値を 0.0 → 0.1 に修正
- delay: `delay` デフォルト値を 20 → 1.0 に修正、型を int → double に変更、制約を拡張
- delay: パラメータ説明に「ticks=true時はtick単位」を追加

CONDITIONコンポーネント:
- time: 必須 → 任意 に修正、デフォルト値 "any" を追加
- time: 許約値に morning/midday/evening/any を追加（実装でサポートされるエイリアス）
- armor: `slot` 許約値を head/chest/legs/feet → boots/leggings/chestplate/helmet に修正（実装actual値）

### v1.6.0での主な修正点

**P0（Critical）- TARGETコンポーネントのrelative_toパラメータ追加:**

TARGETコンポーネント全種:
- `relative_to` パラメータを追加（デフォルト: "caster"）
- 許約値: "caster"（キャスター基準）/"target"（親TARGETで選択された最初のターゲット基準）
- ネストされたTARGETコンポーネントが、親で選択されたターゲットを基準に再選択可能に
- 使用例: 単体ターゲットにダメージ → その周囲の敵にも範囲ダメージ（連鎖攻撃）

**実装変更:**
- `TargetComponent.execute()`: relative_toパラメータに応じてselectTargetsメソッドを振り分け
- 全TARGET具象クラス: `selectTargets(LivingEntity caster, int level, List<LivingEntity> currentTargets)` オーバーロード実装
- `SelfTargetComponent` を除く全TARGETで、currentTargetsの最初のエンティティを基準に選択する機能を追加
- 後方互換性: relative_to未指定時は既存動作（caster基準）

---

## 1. 概要

本仕様書は、RPGPluginのスキルコンポーネントシステムのYAML定義から実際のスキル発動までの処理フロー、コンポーネント間の関係性、エラー条件、組み合わせ自由度について記述する。

---

## 2. コンポーネントタイプ一覧

| タイプ | 説明 | 登録済みコンポーネント |
|--------|------|---------------------|
| **TARGET** | ターゲットを選択 | SELF, SINGLE, CONE, SPHERE, SECTOR, AREA, LINE, NEAREST_HOSTILE |
| **MECHANIC** | 実際の効果を適用 | damage, heal, push, fire, potion, command, message, speed, launch, sound, particle, cleanse, lightning, explosion, delay, channel |
| **CONDITION** | ターゲットを条件でフィルタリング | health, chance, mana, biome, class, armor, time, fire, water, combat, potion, status, tool, event |
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
        value-base: 10   # レベル1で10ダメージ
        value-scale: 2   # レベルごとに+2ダメージ
        type: damage
      - type: fire   # MECHANIC
        seconds: 3      # 3秒間燃焼
      - type: chance # CONDITION
        chance: 50      # 50%の確率（百分率）
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
        value-base: 10   # レベル1で10ダメージ
        value-scale: 2   # レベルごとに+2ダメージ

# パターン2: 範囲ターゲット + 条件フィルタ + 効果
components:
  - type: AREA
    radius: 5
    components:
      - type: chance      # 条件: 50%の確率
        chance: 50        # 百分率で指定（0-100）
        components:
          - type: damage  # 条件満た時のみダメージ
            value-base: 20

# パターン3: 複数ターゲットセット
components:
  - type: SINGLE          # メインターゲット
    range: 20
    components:
      - type: damage
        value-base: 30
  - type: AREA            # 周囲にも範囲ダメージ
    radius: 3
    components:
      - type: damage
        value-base: 10

# パターン4: 連鎖攻撃（relative_to使用）
components:
  - type: SINGLE          # まず単体ターゲットを選択
    range: 20
    components:
      - type: damage      # メインターゲットにダメージ
        value-base: 30
      - type: AREA        # メインターゲットを基準に周囲の敵を検索
        radius: 5
        relative_to: target  # "target"指定で親TARGETの結果を基準にする
        components:
          - type: damage  # 周囲の敵にもダメージ
            value-base: 15
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

**TargetComponent.execute** (lines 43-65):
```java
public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
    // relative_to パラメータを取得
    String relativeTo = settings.getString("relative_to", "caster");

    List<LivingEntity> selectedTargets;

    if ("target".equals(relativeTo) && !targets.isEmpty()) {
        // 現在のターゲット（親TARGETで選択された最初のエンティティ）を基準に選択
        selectedTargets = selectTargets(caster, level, targets);
    } else {
        // casterを基準に選択（デフォルト、後方互換）
        selectedTargets = selectTargets(caster, level);
    }

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

| コンポーネント | パラメータ | 型 | 必須 | デフォルト | 制約 | 説明 |
|---------------|-----------|------|------|-----------|------|------|
| **SELF** | - | - | - | - | - | キャスター自身をターゲット（パラメータなし） |
| **SINGLE** | range | double | 任意 | 10.0 | 0.1-1000 | ターゲット選択範囲 |
| | range_per_level | double | 任意 | 0.0 | - | レベルごとの範囲増加量 |
| | select_nearest | boolean | 任意 | true | - | 最も近いターゲットを選択（false時ランダム） |
| | target_self | boolean | 任意 | false | - | 自分自身をターゲットにするか |
| | hostile_only | boolean | 任意 | true | - | 敵対的エンティティのみ対象にするか |
| | relative_to | String | 任意 | "caster" | caster/target | 基準エンティティ（ネスト時有効） |
| **CONE** | angle | double | 任意 | 90.0 | 1.0-360.0 | 扇状の角度（度数法） |
| | angle_per_level | double | 任意 | 0.0 | - | レベルごとの角度増加量 |
| | range | double | 任意 | 10.0 | 0.1-1000 | 範囲 |
| | range_per_level | double | 任意 | 0.0 | - | レベルごとの範囲増加量 |
| | max_targets | int | 任意 | 5 | 1-1000 | 最大ターゲット数 |
| | max_targets_per_level | int | 任意 | 0 | 0-100 | レベルごとの増加量 |
| | relative_to | String | 任意 | "caster" | caster/target | 基準エンティティ（ネスト時有効） |
| **SPHERE** | radius | double | 任意 | 5.0 | 0.1-500 | 半径 |
| | radius_per_level | double | 任意 | 0.0 | - | レベルごとの半径増加量 |
| | max_targets | int | 任意 | 10 | 1-1000 | 最大ターゲット数 |
| | max_targets_per_level | int | 任意 | 0 | 0-100 | レベルごとの増加量 |
| | include_caster | boolean | 任意 | false | - | キャスター自身を含めるか |
| | relative_to | String | 任意 | "caster" | caster/target | 基準エンティティ（ネスト時有効） |
| **SECTOR** | angle | double | 任意 | 60.0 | 1.0-360.0 | 扇形の角度（度数法） |
| | angle_per_level | double | 任意 | 0.0 | - | レベルごとの角度増加量 |
| | radius | double | 任意 | 8.0 | 0.1-1000 | 半径 |
| | radius_per_level | double | 任意 | 0.0 | - | レベルごとの半径増加量 |
| | max_targets | int | 任意 | 8 | 1-1000 | 最大ターゲット数 |
| | max_targets_per_level | int | 任意 | 0 | 0-100 | レベルごとの増加量 |
| | relative_to | String | 任意 | "caster" | caster/target | 基準エンティティ（ネスト時有効） |
| **AREA** | area_shape | String | 任意 | "CIRCLE" | CIRCLE/RECTANGLE | 形状 |
| | radius | double | 任意 | 10.0 | 0.1-500 | 半径（CIRCLE用） |
| | width | double | 任意 | 10.0 | 0.1-500 | 幅（RECTANGLE用） |
| | depth | double | 任意 | 10.0 | 0.1-500 | 奥行き（RECTANGLE用） |
| | max_targets | int | 任意 | 15 | 1-1000 | 最大ターゲット数 |
| | max_targets_per_level | int | 任意 | 0 | 0-100 | レベルごとの増加量 |
| | include_caster | boolean | 任意 | false | - | キャスター自身を含めるか |
| | relative_to | String | 任意 | "caster" | caster/target | 基準エンティティ（ネスト時有効） |
| **LINE** | length | double | 任意 | 15.0 | 0.1-500 | 長さ |
| | length_per_level | double | 任意 | 0.0 | - | レベルごとの長さ増加量 |
| | width | double | 任意 | 2.0 | 0.1-100 | 幅 |
| | width_per_level | double | 任意 | 0.0 | - | レベルごとの幅増加量 |
| | max_targets | int | 任意 | 5 | 1-1000 | 最大ターゲット数 |
| | max_targets_per_level | int | 任意 | 0 | 0-100 | レベルごとの増加量 |
| | relative_to | String | 任意 | "caster" | caster/target | 基準エンティティ（ネスト時有効） |
| **NEAREST_HOSTILE** | range | double | 任意 | 15.0 | 0.1-1000 | 検索範囲 |
| | range_per_level | double | 任意 | 0.0 | - | レベルごとの範囲増加量 |
| | relative_to | String | 任意 | "caster" | caster/target | 基準エンティティ（ネスト時有効） |

### 7.2 MECHANICコンポーネント

| コンポーネント | パラメータ | 型 | 必須 | デフォルト | 制約 | 説明 |
|---------------|-----------|------|------|-----------|------|------|
| **damage** | value-base | double | 任意 | 0 | - | 基本ダメージ量 |
| | value-scale | double | 任意 | 0 | - | レベルごとの増加量 |
| | type | String | 任意 | "damage" | damage/percent/multiplier/percent missing/percent left | ダメージタイプ |
| | true-damage | boolean | 任意 | false | - | 防具無視ダメージ |
| **heal** | value-base | double | 任意 | 0 | - | 基本回復量 |
| | value-scale | double | 任意 | 0 | - | レベルごとの増加量 |
| | type | String | 任意 | "value" | value/percent/percent missing | 回復タイプ |
| **push** | speed | double | 任意 | 1.0 | - | 水平方向の吹き飛ばし強さ |
| | vertical | double | 任意 | 0.3 | - | 垂直方向の吹き飛ばし強さ |
| **fire** | seconds | int | 任意 | 3 | 1-1000 | 燃焼時間（秒） |
| | ticks | int | 任意 | 60 | 1-20000 | 燃焼時間（tick、seconds未指定時） |
| **potion** | potion | String | 任意 | "SPEED" | PotionEffectType名 | ポーション効果 |
| | duration | double | 任意 | 3.0 | 0.1-100000 | 効果時間（秒） |
| | amplifier | int | 任意 | 0 | 0-15 | 強度 |
| | ambient | boolean | 任意 | true | - | アンビエントエフェクト |
| **command** | command | String | 必須 | - | - | 実行コマンド |
| | type | String | 任意 | "console" | op/console/player | 実行タイプ |
| **message** | text | String | 必須 | - | - | メッセージ本文 |
| | to-caster | boolean | 任意 | false | - | キャスターに送信 |
| | to-target | boolean | 任意 | false | - | ターゲットに送信 |
| **speed** | duration | double | 任意 | 3.0 | 0.1-100000 | 効果時間（秒） |
| | amplifier | int | 任意 | 0 | 0-15 | 速度強度 |
| | ambient | boolean | 任意 | true | - | アンビエントエフェクト |
| **launch** | projectile | String | 任意 | "arrow" | 投射物タイプ | 発射する投射物 |
| | speed | double | 任意 | 2.0 | 0.1-10.0 | 発射速度 |
| | spread | double | 任意 | 0.1 | 0.0-1.0 | 散布率 |
| **sound** | sound | String | 必須 | - | Sound名 | 再生するサウンド |
| | volume | float | 任意 | 1.0 | 0.0-2.0 | 音量 |
| | pitch | float | 任意 | 1.0 | 0.0-2.0 | ピッチ |
| **particle** | particle | String | 必須 | - | Particle型 | 表示するパーティクル |
| | count | int | 任意 | 10 | 1-1000 | パーティクル数 |
| | offset | double | 任意 | 0.5 | 0.0-10.0 | オフセット距離 |
| | speed | double | 任意 | 0.0 | 0.0-5.0 | パーティクル速度 |
| **cleanse** | bad_only | boolean | 任意 | true | - | 悪い効果のみ解除（true時） |
| | potion | String | 任意 | - | PotionEffectType名 | 解除する特定のポーション |
| **lightning** | damage | double | 任意 | 5.0 | - | ダメージ量 |
| | forward | double | 任意 | 0.0 | - | 前方オフセット |
| | right | double | 任意 | 0.0 | - | 右方オフセット |
| **explosion** | power | float | 任意 | 3.0 | - | 爆発力（TNT=4.0） |
| | fire | boolean | 任意 | false | - | 火災を発生させるか |
| | damage | boolean | 任意 | true | - | ダメージを与えるか |
| **delay** | delay | double | 任意 | 1.0 | 0-∞ | 遅延時間（秒、ticks=true時はtick単位） |
| | ticks | boolean | 任意 | false | - | tick単位で指定 |
| **channel** | duration | int | 任意 | 60 | 1-60000 | 詠唱時間（tick） |
| | ticks | boolean | 任意 | true | - | tick単位で指定 |

### 7.3 CONDITIONコンポーネント

| コンポーネント | パラメータ | 型 | 必須 | デフォルト | 制約 | 説明 |
|---------------|-----------|------|------|-----------|------|------|
| **health** | type | String | 任意 | "value" | value/percent/difference/difference percent | 比較タイプ |
| | min-value | double | 任意 | 0.0 | - | 最小HP値（または割合） |
| | max-value | double | 任意 | Double.MAX_VALUE | - | 最大HP値（または割合） |
| **chance** | chance | double | 任意 | 50 | 0-100 | 発動確率（百分率、例:50=50%） |
| **mana** | min-value | double | 任意 | 0 | 0-∞ | 最小MP値 |
| **biome** | biome | String | 任意 | - | Biome名（部分一致） | 必要バイオーム |
| **class** | class | String | 任意 | - | クラス名 | 必要クラス |
| | exact | boolean | 任意 | false | - | 完全一致でチェック（false時部分一致） |
| **armor** | material | String | 任意 | - | Material名（部分一致） | 装備素材 |
| | slot | String | 任意 | "any" | boots/leggings/chestplate/helmet/any | 装備スロット |
| **time** | time | String | 任意 | "any" | day/night/dawn/dusk/noon/midnight/morning/midday/evening/any | 時刻（anyは常時true） |
| **fire** | ticks | int | 任意 | 0 | 0-∞ | 最小燃焼tick数 |
| **water** | depth | int | 任意 | 1 | 1-∞ | 最小水深（ブロック数） |
| **combat** | mode | String | 任意 | "enter" | enter/exit/both | 戦闘状態モード |
| | seconds | int | 任意 | 5 | 1-∞ | 戦闘とみなす期間（秒） |
| **potion** | potion | String | 必須 | - | PotionEffectType名 | ポーション効果 |
| | min_level | int | 任意 | 1 | 1-255 | 最小強度 |
| **status** | stat | String | 任意 | "health" | health/max_health/food/air/exp/level | ステータス種別 |
| | min | double | 任意 | 0 | - | 最小値 |
| | max | double | 任意 | Double.MAX_VALUE | - | 最大値 |
| | percent | boolean | 任意 | false | - | パーセント指定 |
| **tool** | material | String | 任意 | - | Material名（部分一致） | ツール素材 |
| | hand | String | 任意 | "any" | main/off/any | 手の指定 |
| **event** | event | String | 必須 | - | PHYSICAL_DEALT/PHYSICAL_TAKEN/CROUCH/LAND/DEATH/KILL/LAUNCH/ENVIRONMENTAL | イベントタイプ |
| | duration | int | 任意 | 0 | 0-∞ | 有効期間（秒、0で永続） |

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
| **LAUNCH** | 投射物発射時 | 遅延 |
| **ENVIRONMENTAL** | 環境ダメージ時 | 遅延 |

### 7.5 FILTERコンポーネント

| コンポーネント | パラメータ | 型 | 必須 | デフォルト | 制約 | 説明 |
|---------------|-----------|------|------|-----------|------|------|
| **entity_type** | type | String | 任意 | "all" | SELF/PLAYER/MOB/ALL | エンティティタイプ |
| **group** | group | String | 任意 | "enemy" | ENEMY/ALLY/BOTH | グループ |

**entity_type の値**:
- `SELF`: キャスター自身のみ
- `PLAYER`: プレイヤーのみ
- `MOB`: Mobのみ（プレイヤー除外）
- `ALL`: 全て（フィルターなし）

**group の値**:
- `ENEMY`: 敵対的エンティティのみ（Mob、プレイヤーはPvP設定による）
- `ALLY`: 味方のみ（プレイヤー、キャスター自身）
- `BOTH`: 敵味方両方

---

## 8. 数式構文とプレースホルダー

### 8.1 数式構文（Value Expression）

スキルコンポーネントでは、数値パラメータにレベルに応じたスケーリングを適用できます。

#### 8.1.1 スケーリング形式

```
最終値 = base + (level - 1) * scale
```

| パラメータ | 説明 | 例 |
|-----------|------|---|
| `{key}-base` | 基本値（レベル1の時の値） | `value-base: 10` |
| `{key}-scale` | レベルごとの増加量 | `value-scale: 2` |

#### 8.1.2 計算例

| YAML設定 | レベル1 | レベル2 | レベル3 | レベル5 |
|----------|--------|--------|--------|--------|
| `value-base: 10`<br>`value-scale: 2` | 10 | 12 | 14 | 18 |
| `value-base: 100`<br>`value-scale: 10` | 100 | 110 | 120 | 140 |
| `value-base: 5`<br>`value-scale: 0.5` | 5 | 5.5 | 6 | 7 |

#### 8.1.3 省略時の動作

- `{-scale}` を省略した場合: スケーリングなし（常にbaseの値）
- `{-base}` を省略した場合: コンポーネントのデフォルト値を使用

```yaml
# 例：ダメージメカニック
components:
  - type: damage
    value-base: 10    # レベル1で10ダメージ
    value-scale: 2    # レベルごとに+2ダメージ
    type: damage
```

### 8.2 プレースホルダー一覧

一部のMECHANICコンポーネントでは、コマンドやメッセージ内でプレースホルダーを使用できます。

#### 8.2.1 コマンドメカニック（command）のプレースホルダー

| プレースホルダー | 型 | 説明 | 出力例 |
|-----------------|------|------|--------|
| `{caster}` | String | スキル発動者の名前 | `"Steve"` |
| `{target}` | String | ターゲットの名前 | `"Alex"` |
| `{level}` | int | スキルレベル | `"5"` |
| `{x}` | int | ターゲットのX座標 | `"123"` |
| `{y}` | int | ターゲットのY座標 | `"64"` |
| `{z}` | int | ターゲットのZ座標 | `"456"` |
| `{world}` | String | ワールド名 | `"world"` |

**使用例:**
```yaml
components:
  - type: command
    command: "title {target} title {\"text\":\"ダメージ！\",\"color\":\"red\"}"
    type: console
```

#### 8.2.2 メッセージメカニック（message）のプレースホルダー

| プレースホルダー | 型 | 説明 | 出力例 |
|-----------------|------|------|--------|
| `{player}` | String | スキル発動者の名前 | `"Steve"` |
| `{target}` | String | ターゲットの名前 | `"Alex"` |

**使用例:**
```yaml
components:
  - type: message
    text: "<gold>{player}</gold> が <green>{target}</green> にスキルを発動！"
    to-caster: true
    to-target: false
```

---

## 9. YAML例と対応する処理

### 9.1 基本的な火の玉スキル

```yaml
skill_id: "fireball"
display_name: "ファイアボール"
components:
  - type: SINGLE      # ターゲット選択
    range: 20.0
    components:
      - type: damage   # ダメージ適用
        value-base: 10    # レベル1で10ダメージ
        value-scale: 2    # レベルごとに+2ダメージ
        type: damage
      - type: fire     # 燃焼効果
        seconds: 3       # 3秒間燃焼（tick単位のticksも使用可）
```

**処理フロー**:
1. SINGLEコンポーネントが範囲20以内のターゲットを選択
2. 選択されたターゲットにdamageが適用
   - レベル1: 10ダメージ、レベル5: 18ダメージ
3. 選択されたターゲットにfireが適用（3秒間=60tick）

### 9.2 確率付きヒールスキル

```yaml
skill_id: "prayer"
display_name: "祈り"
components:
  - type: SELF        # 自分自身
    components:
      - type: chance   # 50%の確率
        chance: 50     # 百分率で指定（0-100）
        components:
          - type: heal # 成功時のみ回復
            value-base: 20   # レベル1で20回復
            value-scale: 5   # レベルごとに+5回復
```

**処理フロー**:
1. SELFコンポーネントがキャスター自身をターゲット
2. chanceコンポーネントが50%の確率でフィルタリング
3. 成功した場合のみhealが適用
   - レベル1: 20回復、レベル5: 40回復

### 9.3 範囲攻撃スキル

```yaml
skill_id: "whirlwind"
display_name: "旋風"
components:
  - type: AREA        # 範囲ターゲット
    radius: 5.0
    max_targets: 10
    components:
      - type: damage   # ダメージ
        value-base: 5     # レベル1で5ダメージ
        value-scale: 1.5  # レベルごとに+1.5ダメージ
        type: damage
      - type: push     # 吹き飛ばし
        speed: 2.0        # 吹き飛ばし強さ
```

**処理フロー**:
1. AREAコンポーネントが半径5以内の最大10体を選択
2. 選択されたターゲット全員にdamageを適用
   - レベル1: 5ダメージ、レベル5: 11ダメージ
3. damage成功後、選択されたターゲット全員にpushを適用

---

## 10. エディタ開発のためのガイドライン

### 10.1 YAML出力形式

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

### 10.2 バリデーション要件

| チェック項目 | 方法 |
|-----------|------|
| typeフィールドの存在 | 必須 |
| 有効なコンポーネントタイプ | ComponentRegistry.hasComponent() |
| 循環参照 | 深さカウント（最大50） |
| 必須パラメータ | 各コンポーネントの仕様に従う |

### 10.3 UI推奨事項

1. **ツリービュー**: 親子関係を視覚化
2. **ドラッグ&ドロップ**: コンポーネントの移動・親子変更
3. **リアルタイムバリデーション**: 不正な組み合わせを即座に通知
4. **プレビュー**: YAML出力のリアルタイム確認

---

## 11. 付録: ソースコード参照

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
