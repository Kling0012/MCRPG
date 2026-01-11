# コンポーネントシステム V5 リファレンス

## 目次

1. [概要](#概要)
2. [アーキテクチャ](#アーキテクチャ)
3. [配置ルール](#配置ルール)
4. [トリガー (Triggers)](#トリガー-triggers)
5. [ターゲット (Targets)](#ターゲット-targets)
6. [フィルター (Filters)](#フィルター-filters)
7. [条件 (Conditions)](#条件-conditions)
8. [メカニック (Mechanics)](#メカニック-mechanics)
9. [コスト (Costs)](#コスト-costs)
10. [クールダウン (Cooldowns)](#クールダウン-cooldowns)
11. [YAML構造](#yaml構造)
12. [実装例](#実装例)

---

## 概要

V5コンポーネントシステムは、スキルを「コンポーネント」の組み合わせとして定義する柔軟なシステムです。

### 特徴

- **ツリー構造**: コンポーネントは親子関係で構成され、条件分岐や複雑なロジックを表現
- **イベント駆動**: トリガーイベントに応じてコンポーネントツリーを実行
- **即時実行**: CASTトリガーにより、イベント待機なしの即時発動が可能
- **拡張性**: 新しいコンポーネントタイプを容易に追加

---

## アーキテクチャ

### コンポーネントタイプ

```
┌─────────────────────────────────────────────────────────────┐
│                    コンポーネントタイプ                       │
├─────────────────────────────────────────────────────────────┤
│  Trigger  (トリガー)   : 発動タイミングを定義                │
│  Target   (ターゲット) : 効果対象を定義                      │
│  Filter   (フィルター) : ターゲット属性でフィルタリング        │
│  Condition(条件)      : 状態でフィルタリング                  │
│  Mechanic (メカニック) : 実際の効果を定義                    │
│  Cost     (コスト)    : 消費リソースを定義                   │
│  Cooldown (クールダウン): 発動間隔を定義                     │
└─────────────────────────────────────────────────────────────┘
```

### コンポーネントツリー

```
Trigger (CAST)
  ├─ Cost (MANA)
  ├─ Cooldown (COOLDOWN)
  └─ Target (SPHERE)
      ├─ Filter (ENTITY_TYPE: PLAYER)
      ├─ Filter (GROUP: ENEMY)
      ├─ Condition (health > 50%)
      │   └─ Mechanic (DAMAGE: 100)
      └─ Mechanic (DAMAGE: 50)
```

---

## 配置ルール

### 基本ルール

コンポーネントには配置可能な場所が定められています。`SkillValidator`によりYAML読み込み時に検証されます。

| 親コンポーネント | 配置可能な子コンポーネント |
|------------------|---------------------------|
| **TRIGGER** | COST, COOLDOWN, TARGET |
| **TARGET** | FILTER, CONDITION, MECHANIC, TARGET |
| **FILTER** | FILTER, CONDITION, MECHANIC, TARGET |
| **CONDITION** | CONDITION, MECHANIC, TARGET |
| **MECHANIC** | MECHANIC, TARGET, CONDITION |
| **COST** | なし |
| **COOLDOWN** | なし |

### 配置制限

- **TRIGGER**: ルートレベルに1個のみ配置可能
- **COST**: TRIGGERの子として0-1個
- **COOLDOWN**: TRIGGERの子として0-1個
- **他**: 無制限（ネスト可能）

---

## トリガー (Triggers)

トリガーはスキルが発動するタイミングを定義します。

### 実装済みトリガー

| ID | 説明 | イベント | パラメータ |
|----|------|---------|-----------|
| **CAST** | スキル使用時に即時実行（始動点） | なし（即時） | `cooldown` |
| **CROUCH** | スニーク時 | PlayerToggleSneakEvent | `type` |
| **LAND** | 着地時（落下ダメージ） | EntityDamageEvent | `duration` |
| **DEATH** | 死亡時 | PlayerDeathEvent | `duration` |
| **KILL** | エンティティを倒した時 | EntityDeathEvent | `duration` |
| **PHYSICAL_DEALT** | 物理ダメージを与えた時 | EntityDamageByEntityEvent | `duration` |
| **PHYSICAL_TAKEN** | 物理ダメージを受けた時 | EntityDamageByEntityEvent | `duration` |
| **LAUNCH** | 投射物発射時 | ProjectileLaunchEvent | `duration` |
| **ENVIRONMENTAL** | 環境ダメージ時 | EntityDamageEvent | `type`, `duration` |

### YAML例

```yaml
components:
  - type: trigger
    component_id: CAST
    settings:
      cooldown: 5.0
    children:
      - type: mechanic
        component_id: DAMAGE
        settings:
          value: "10 + LEVEL * 2"
```

---

## ターゲット (Targets)

ターゲットは効果の対象を定義します。

### ターゲットタイプ

| ID | 説明 | パラメータ |
|----|------|-----------|
| **SELF** | 自分自身 | なし |
| **SINGLE** | 単体ターゲット | `range`, `max_targets` |
| **CONE** | コーン状範囲 | `angle`, `range`, `max_targets` |
| **SPHERE** | 球形範囲 | `radius`, `max_targets` |
| **SECTOR** | 扇形範囲 | `angle`, `radius`, `max_targets` |
| **AREA** | 指定座標範囲 | `shape`, `radius` |
| **LINE** | 直線状範囲 | `length`, `width` |
| **NEAREST_HOSTILE** | 最寄りの敵 | `range` |

### YAML例

```yaml
components:
  - type: trigger
    component_id: CAST
    children:
      - type: target
        component_id: SPHERE
        settings:
          radius: 5.0
          max_targets: 10
        children:
          - type: mechanic
            component_id: DAMAGE
            settings:
              value: "20 + LEVEL * 3"
```

---

## フィルター (Filters)

フィルターはターゲットを属性（エンティティタイプ、グループ）で絞り込みます。

### FilterとConditionの違い

| 種類 | 目的 | 例 |
|------|------|-----|
| **Filter** | エンティティの属性でフィルタリング | プレイヤーのみ、Mobのみ、敵のみ |
| **Condition** | 状態・状況でフィルタリング | HPが50%以上、夜間、ポーション効果中 |

### フィルタータイプ

| ID | 説明 | パラメータ | 値 |
|----|------|-----------|---|
| **entity_type** | エンティティタイプで絞り込み | `type` | `SELF`, `PLAYER`, `MOB`, `ALL` |
| **group** | 敵味方グループで絞り込み | `group` | `ENEMY`, `ALLY`, `BOTH` |

### YAML例

```yaml
components:
  - type: trigger
    component_id: CAST
    children:
      # 球形範囲のターゲット取得
      - type: target
        component_id: SPHERE
        settings:
          radius: 10
        children:
          # プレイヤーのみに絞り込み
          - type: filter
            component_id: ENTITY_TYPE
            settings:
              type: PLAYER
          # 敵対的エンティティのみに絞り込み
          - type: filter
            component_id: GROUP
            settings:
              group: ENEMY
          # ダメージ適用
          - type: mechanic
            component_id: DAMAGE
            settings:
              value: "20 + LEVEL * 3"
```

### 複数フィルターの組み合わせ

複数のフィルターを連続させると、**AND条件**として機能します。

```yaml
# プレイヤー AND 敵対 = 敵対的プレイヤーのみ（PvP有効時）
- type: filter
  component_id: ENTITY_TYPE
  settings:
    type: PLAYER
  children:
    - type: filter
      component_id: GROUP
      settings:
        group: ENEMY
      children:
        - type: mechanic
          component_id: DAMAGE
```

---

## 条件 (Conditions)

条件はコンポーネントの実行可否を判定します。

### 実装済み条件

| ID | 説明 | パラメータ |
|----|------|-----------|
| **health** | HP条件 | `min`, `max`, `percent` |
| **chance** | 確率 | `chance` (0.0-1.0) |
| **mana** | MP条件 | `min`, `max` |
| **biome** | バイオーム | `biome` |
| **class** | クラス条件 | `class`, `exact` |
| **time** | 時間帯 | `time` (day/night/dawn/dusk/noon/midnight) |
| **armor** | 防具条件 | `material`, `slot` |
| **fire** | 炎上状態 | `ticks` |
| **water** | 水中状態 | `depth` |
| **combat** | 戦闘状態 | `mode`, `seconds` |
| **potion** | ポーション効果 | `potion`, `min_level` |
| **status** | ステータス値 | `stat`, `min`, `max`, `percent` |
| **tool** | ツール条件 | `material`, `hand` |

### YAML例

```yaml
components:
  - type: trigger
    component_id: CAST
    children:
      - type: condition
        component_id: health
        settings:
          max: 0.5  # HPが50%以下の時
        children:
          - type: mechanic
            component_id: DAMAGE
            settings:
              value: "50 + LEVEL * 5"  # 追加ダメージ
```

---

## メカニック (Mechanics)

メカニックは実際のゲーム効果を定義します。

### 実装済みメカニック

| ID | 説明 | パラメータ |
|----|------|-----------|
| **damage** | ダメージ | `value`, `stat`, `multiplier` |
| **heal** | 回復 | `value` |
| **push** | ノックバック | `speed`, `vertical` |
| **fire** | 炎上 | `duration` |
| **message** | メッセージ | `text` |
| **potion** | ポーション効果 | `potion`, `duration`, `amplifier`, `ambient` |
| **lightning** | 稲妻 | `damage`, `forward`, `right` |
| **sound** | サウンド | `sound`, `volume`, `pitch` |
| **command** | コマンド実行 | `command`, `type` (op/console/player) |
| **explosion** | 爆発 | `power`, `fire`, `damage` |
| **speed** | 移動速度 | `duration`, `amplifier`, `ambient` |
| **particle** | パーティクル | `particle`, `count`, `offset`, `speed` |
| **launch** | 投射物発射 | `projectile`, `speed`, `spread` |
| **delay** | 遅延実行 | `delay`, `ticks` |
| **cleanse** | ポーション解除 | `bad_only`, `potion` |
| **channel** | 詠唱中 | `duration`, `ticks` |

### YAML例

```yaml
components:
  - type: trigger
    component_id: CAST
    children:
      - type: mechanic
        component_id: POTION
        settings:
          potion: SPEED
          duration: "10 + LEVEL * 2"
          amplifier: 1
          ambient: false
```

---

## コスト (Costs)

コストはスキル発動時に消費するリソースを定義します。

| ID | 説明 | パラメータ |
|----|------|-----------|
| **MANA** | MP消費 | `base`, `per_level`, `min`, `max` |
| **HP** | HP消費 | `base`, `per_level`, `min`, `max` |
| **STAMINA** | スタミナ消費 | `base`, `per_level` |
| **ITEM** | アイテム消費 | `item`, `amount` |

---

## クールダウン (Cooldowns)

クールダウンはスキルの発動間隔を定義します。

| ID | 説明 | パラメータ |
|----|------|-----------|
| **COOLDOWN** | クールダウン | `base`, `per_level`, `min` |

---

## YAML構造

### 基本構造

```yaml
# ============================================
# 基本情報
# ============================================
id: example_skill
name: サンプルスキル
display_name: "&eサンプルスキル"
type: normal
description:
  - "&aスキルの説明"
max_level: 5

# カスタム変数（数式で使用可能）
variables:
  base_value: 10.0
  scale_factor: 1.5

# ============================================
# コンポーネント定義
# ============================================
components:
  # トリガー
  - type: trigger
    component_id: CAST
    settings:
      cooldown: 5.0

    # 子コンポーネント（トリガー発火時の効果）
    children:
      # ターゲット
      - type: target
        component_id: SINGLE
        settings:
          range: 10.0

        # コスト
        - type: cost
          component_id: MANA
          settings:
            base: 20
            per_level: 2

        # メカニック
        - type: mechanic
          component_id: DAMAGE
          settings:
            value: "10 + LEVEL * 2"
            stat: INTELLIGENCE
            multiplier: 1.0
```

---

## 実装例

### 単純な攻撃スキル

```yaml
id: power_strike
name: パワーストライク
display_name: "&6パワーストライク"
type: normal
max_level: 5

components:
  - type: trigger
    component_id: CAST
    settings:
      cooldown: 8.0
    children:
      - type: cost
        component_id: MANA
        settings:
          base: 15
          min: 5
      - type: mechanic
        component_id: DAMAGE
        settings:
          value: "30 + LEVEL * 10"
          stat: STRENGTH
          multiplier: 1.5
      - type: mechanic
        component_id: PUSH
        settings:
          speed: 1.5
          vertical: 0.3
```

### 条件付きスキル

```yaml
id: berserker_rage
name: バーサーカーレイジ
display_name: "&cバーサーカーレイジ"
type: normal
max_level: 3

components:
  - type: trigger
    component_id: CAST
    children:
      # HPが30%以下の時のみ使用可能
      - type: condition
        component_id: health
        settings:
          max: 0.3
        children:
          - type: mechanic
            component_id: DAMAGE
            settings:
              value: "50 + LEVEL * 20"
          - type: mechanic
            component_id: SPEED
            settings:
              duration: "5 + LEVEL * 2"
              amplifier: 2
```

### 範囲攻撃スキル

```yaml
id: flame_burst
name: フレイムバースト
display_name: "&cフレイムバースト"
type: normal
max_level: 5

components:
  - type: trigger
    component_id: CAST
    children:
      - type: target
        component_id: SPHERE
        settings:
          radius: "3.0 + LEVEL * 0.5"
          max_targets: "5 + LEVEL"
        children:
          - type: mechanic
            component_id: FIRE
            settings:
              duration: "3 + LEVEL"
          - type: mechanic
            component_id: DAMAGE
            settings:
              value: "15 + LEVEL * 5"
```

### トリガースキル

```yaml
id: counter_attack
name: カウンターアタック
display_name: "&eカウンターアタック"
type: normal
max_level: 3

components:
  - type: trigger
    component_id: PHYSICAL_TAKEN
    settings:
      duration: 10
    children:
      - type: condition
        component_id: chance
        settings:
          chance: 0.3  # 30%の確率
        children:
          - type: mechanic
            component_id: DAMAGE
            settings:
              value: "20 + LEVEL * 10"
```

---

## 拡張

### 新しいコンポーネントの追加

1. **EffectComponent**を継承したクラスを作成
2. **ComponentRegistry**に登録
3. YAMLテンプレートとエディタを更新

#### トリガーの実装例

```java
public class MyTrigger implements Trigger<MyEvent> {
    @Override
    public String getKey() {
        return "MY_TRIGGER";
    }

    @Override
    public Class<MyEvent> getEventClass() {
        return MyEvent.class;
    }

    @Override
    public LivingEntity getCaster(MyEvent event) {
        return event.getPlayer();
    }

    @Override
    public boolean shouldTrigger(MyEvent event, int level, TriggerSettings settings) {
        return true;
    }
}
```

#### メカニックの実装例

```java
public class MyMechanic extends MechanicComponent {
    public MyMechanic() {
        super("my_mechanic");
    }

    @Override
    protected boolean apply(LivingEntity caster, int level, LivingEntity target) {
        // 効果の実装
        return true;
    }
}
```

---

## トラブルシューティング

### よくある問題

#### Q: コンポーネントが実行されない

A:
1. トリガーが正しく登録されているか確認
2. 条件が満たされているか確認
3. 子コンポーネントの`children`構造が正しいか確認

#### Q: 数式がエラーになる

A:
1. 変数名が正しく定義されているか確認
2. 括弧の対応が正しいか確認
3. `LEVEL`変数は正しく使用されているか確認

---

## 関連ドキュメント

- [YAMLリファレンス](YAML_REFERENCE.md) - 詳細なYAML仕様
- [スキルシステムV2](SKILL_SYSTEM_V2.md) - システム全体のアーキテクチャ
- [APIドキュメント](API_DOCUMENTATION.md) - Java/SKript API
