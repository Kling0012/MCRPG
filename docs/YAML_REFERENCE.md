# スキルYAMLリファレンス

## 目次

1. [基本構造](#基本構造)
2. [フィールド詳細](#フィールド詳細)
3. [数式リファレンス](#数式リファレンス)
4. [ターゲット設定](#ターゲット設定)
5. [エフェクト設定](#エフェクト設定)
6. [完全スキーマ](#完全スキーマ)

---

## 基本構造

### 最小構成

```yaml
id: skill_id              # 必須: 一意のスキルID
name: スキル名            # 必須: スキル名
display_name: "&eスキル名" # 必須: 表示名（カラーコード対応）
type: normal              # 必須: normal（active/passiveは廃止）
max_level: 5              # 必須: 最大レベル
```

### 完全構成例

```yaml
# ========================================
# 基本情報
# ========================================
id: example_skill
name: サンプルスキル
display_name: "&e&lサンプルスキル"
type: normal              # active/passiveは廃止、normalのみ
description:
  - "&c説明文1行目"
  - "&e説明文2行目"
max_level: 10

# ========================================
# カスタム変数
# ========================================
variables:
  custom_var: 1.0

# ========================================
# コスト設定
# ========================================
cost:
  type: mana
  base: 10
  per_level: -1
  min: 0

# ========================================
# クールダウン設定
# ========================================
cooldown:
  base: 5.0
  per_level: -0.5
  min: 1.0

# ========================================
# ダメージ計算
# ========================================
damage:
  formula: "STR * 1.5 + Lv * 10"
  levels:
    5: "STR * 2.0 + Lv * 15"

# ========================================
# ターゲット設定
# ========================================
targeting:
  type: sphere
  sphere:
    radius: 5.0

# ========================================
# エフェクト設定
# ========================================
effects:
  cast:
    - type: sound
      sound: ENTITY_PLAYER_ATTACK_SWEEP
      volume: 1.0
      pitch: 1.0

# ========================================
# スキルツリー
# ========================================
skill_tree:
  parent: none
  unlock_requirements:
    - type: level
      value: 1
  cost: 1

# ========================================
# GUI設定
# ========================================
icon_material: DIAMOND_SWORD
available_classes:
  - warrior
  - knight
```

---

## フィールド詳細

### 基本情報フィールド

| フィールド | 型 | 必須 | 説明 | 例 |
|-----------|----|----|------|-----|
| `id` | String | ✓ | 一意のスキルID（小文字英数字と_） | `fireball` |
| `name` | String | ✓ | スキル名（日本語可） | `ファイアボール` |
| `display_name` | String | ✓ | GUI表示名（カラーコード対応） | `&cファイアボール` |
| `type` | String | ✓ | `normal`（active/passiveは廃止） | `normal` |
| `description` | List<String> | - | 説明文（複数行可） | 见上表 |
| `max_level` | Integer | ✓ | 最大スキルレベル | `10` |

### variables（カスタム変数）

数式内で使用する独自の変数を定義します。

```yaml
variables:
  str_scale: 1.8      # STR倍率
  int_scale: 2.5      # INT倍率
  base_damage: 50.0   # 基本ダメージ
  crit_mult: 1.5      # クリティカル倍率
```

**命名規則:**
- 小文字英数字とアンダースコアのみ
- 数値のみ可（整数・小数）
- 負の値も可能

### cost（コスト設定）

レベル依存のコスト設定を定義します。

```yaml
cost:
  type: mana          # コストタイプ: mana, hp, stamina
  base: 15            # 基本コスト
  per_level: -1       # レベルごとの増減
  min: 5              # 最小コスト
  max: 50             # 最大コスト（省略可）
```

| サブフィールド | 型 | 必須 | 説明 |
|---------------|----|----|------|
| `type` | String | - | `mana`, `hp`, `stamina`（デフォルト: mana） |
| `base` | Number | ✓ | レベル1でのコスト |
| `per_level` | Number | - | レベルごとの増減量（負で減少） |
| `min` | Number | - | 最小コスト（負で無制限） |
| `max` | Number | - | 最大コスト |

**計算式:** `cost = base + (level - 1) * per_level`

### cooldown（クールダウン設定）

レベル依存のクールダウン設定を定義します。

```yaml
cooldown:
  base: 8.0           # 基本クールダウン（秒）
  per_level: -0.5     # レベルごとの増減（秒）
  min: 2.0            # 最短クールダウン（秒）
```

| サブフィールド | 型 | 必須 | 説明 |
|---------------|----|----|------|
| `base` | Number | ✓ | レベル1でのクールダウン（秒） |
| `per_level` | Number | - | レベルごとの増減（秒） |
| `min` | Number | - | 最短クールダウン（秒） |

### damage（ダメージ計算）

数式によるダメージ計算を定義します。

```yaml
damage:
  formula: "STR * str_scale + Lv * 10 + base_damage"
  levels:
    5: "STR * str_scale * 1.5 + Lv * 15"
    10: "STR * str_scale * 2.0 + Lv * 20 + 100"
```

| サブフィールド | 型 | 必須 | 説明 |
|---------------|----|----|------|
| `formula` | String | ✓ | 基本ダメージ数式 |
| `levels` | Map<Integer, String> | - | レベル別数式オーバーライド |

---

## 数式リファレンス

### 組み込み変数

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

### 演算子

| 演算子 | 説明 | 例 | 結果 |
|--------|----|-----|-----|
| `+` | 加算 | `10 + 5` | `15` |
| `-` | 減算 | `10 - 5` | `5` |
| `*` | 乗算 | `10 * 5` | `50` |
| `/` | 除算 | `10 / 5` | `2.0` |
| `%` | 剰余 | `10 % 3` | `1` |
| `^` | 累乗 | `2 ^ 3` | `8` |

### 関数

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

### 条件演算子（三項演算子）

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
| `a \|\| b` | 論理和 | `Lv >= 10 \|\| STR > 50` |
| `!a` | 否定 | `!(HP_ratio > 0.5)` |

### 数式例

#### 基本的なダメージ計算

```yaml
variables:
  str_scale: 1.5
damage:
  formula: "STR * str_scale + Lv * 10 + 50"
```

#### クリティカル計算

```yaml
variables:
  base_crit: 0.1      # 基本クリティカル率10%
  dex_crit: 0.002     # DEX1ごとに+0.2%
  crit_mult: 2.0      # クリティカル倍率

damage:
  formula: "STR * 1.5 + Lv * 10"
  # レベル5でクリティカル実装
  levels:
    5: "(STR * 1.5 + Lv * 10) * (1 + min(base_crit + DEX * dex_crit, 1.0) * crit_mult)"
```

#### 低HPボーナス

```yaml
damage:
  formula: "STR * 2.0 + (HP_ratio < 0.3 ? 100 : 0)"
```

#### 複雑な条件分岐

```yaml
damage:
  formula: "STR * 1.5 + Lv * 10"
  levels:
    5: |
      STR * 1.5 + Lv * 10 +
      (HP_ratio < 0.25 ? 100 :
       HP_ratio < 0.5 ? 50 :
       HP_ratio < 0.75 ? 25 : 0)
```

---

## ターゲット設定

### ターゲットタイプ一覧

| タイプ | 説明 | YAML例 |
|--------|----|----|
| `self` | 自分自身 | `type: self` |
| `self_plus_one` | 自分 + 近くのエンティティ1体 | `type: self_plus_one` |
| `nearest_hostile` | 最も近い敵対的エンティティ | `type: nearest_hostile` |
| `nearest_player` | 最も近いプレイヤー | `type: nearest_player` |
| `nearest_entity` | 最も近いエンティティ | `type: nearest_entity` |
| `area_self` | 範囲内の全エンティティ（自分含む） | `type: area_self` |
| `area_others` | 範囲内の全エンティティ（自分除外） | `type: area_others` |
| `sphere` | 球形範囲 | `type: sphere` |
| `line` | 直線上のターゲット（SkillAPI参考） | `type: line` |
| `cone` | 扇状範囲のターゲット（SkillAPI参考） | `type: cone` |
| `looking` | 視線上のターゲット（SkillAPI参考） | `type: looking` |
| `external` | 外部から指定されたターゲット | `type: external` |

### 汎用ターゲット設定フィールド

| フィールド | 型 | 説明 | 適用ターゲットタイプ |
|-----------|----|----|-------------------|
| `range` | Double | 範囲（ブロック） | LINE, LOOKING, CONE |
| `line_width` | Double | 直線の幅 | LINE, LOOKING |
| `cone_angle` | Double | コーンの角度（度数法） | CONE |
| `sphere_radius` | Double | 球形の半径 | SPHERE |
| `filter` | String | エンティティフィルタ | 全タイプ |
| `max_targets` | Integer | 最大ターゲット数 | 範囲系全般 |
| `group` | String | グループフィルタ（敵味方） | 全タイプ |
| `wall` | Boolean | 壁を透過するか | 全タイプ |
| `random` | Boolean | ランダム順序で選択 | 範囲系全般 |
| `caster` | Boolean | キャスターを含める | 全タイプ |

### グループフィルタ（group）

| フィルタ値 | 説明 |
|-----------|----|
| `both` | 敵味方両方（デフォルト） |
| `enemy` | 敵対的エンティティのみ（PvPなしの場合: MOBのみ） |
| `ally` | 味方のみ（PvPなしの場合: プレイヤーのみ） |

> **注意**: 本プロジェクトはPvPを行わない設定のため、`enemy` はMOBのみ、`ally` はプレイヤーのみを対象とします。

### その他のターゲットオプション

| フィールド | 型 | デフォルト | 説明 |
|-----------|----|----------|------|
| `wall` | Boolean | `false` | `true`で壁を透過してターゲット選択 |
| `random` | Boolean | `false` | `true`で範囲内のターゲットをランダムに選択 |
| `caster` | Boolean | `false` | `true`でフィルタに関係なくキャスター自身を含める |

### ターゲット設定例

#### self（自分自身）

```yaml
targeting:
  type: self
```

#### sphere（球形範囲）

```yaml
targeting:
  type: sphere
  sphere_radius: 5.0        # 汎用フィールド
  # または
  sphere:
    radius: 5.0            # 従来の書き方（後方互換性）
  max_targets: 10          # 最大ターゲット数
```

#### line（直線範囲）

```yaml
targeting:
  type: line
  range: 15.0              # 範囲（ブロック）
  line_width: 2.0          # 直線の幅
  filter: hostile          # 敵対MOBのみ
```

#### cone（扇状範囲）

```yaml
targeting:
  type: cone
  range: 10.0              # 範囲（ブロック）
  cone_angle: 90.0         # 角度（度数法）
  # または
  cone:
    angle: 90.0            # 従来の書き方
    range: 10.0
  max_targets: 5
```

#### looking（視線範囲）

```yaml
targeting:
  type: looking
  range: 20.0              # 範囲（ブロック）
  line_width: 1.5          # 直線の幅
  filter: mobs             # MOBのみ
```

#### nearest_hostile（最も近い敵）

```yaml
targeting:
  type: nearest_hostile
  range: 8.0               # 探索範囲
```

#### area_self（自分を含む範囲）

```yaml
targeting:
  type: area_self
  sphere_radius: 6.0       # 範囲
  filter: players          # プレイヤーのみ
  max_targets: 8
```

#### 敵のみを対象（グループフィルタ）

```yaml
targeting:
  type: sphere
  sphere_radius: 8.0
  group: enemy             # MOBのみ（PvPなし環境）
  max_targets: 5
```

#### 味方のみを対象（ヒールスキル等）

```yaml
targeting:
  type: area_others
  sphere_radius: 10.0
  group: ally              # プレイヤーのみ（PvPなし環境）
  max_targets: 4
```

#### 壁を透過する範囲攻撃

```yaml
targeting:
  type: cone
  range: 15.0
  cone_angle: 60.0
  wall: true               # 壁を透過
  group: enemy
  max_targets: 10
```

#### ランダムにターゲットを選択

```yaml
targeting:
  type: sphere
  sphere_radius: 6.0
  random: true             # 範囲内からランダムに選択
  max_targets: 3
```

#### キャスターを含める

```yaml
targeting:
  type: area_others
  sphere_radius: 5.0
  caster: true             # フィルタに関係なくキャスターを含む
  group: enemy             # 敵 + キャスター
```

#### 複合フィルタ例

```yaml
targeting:
  type: cone
  range: 12.0
  cone_angle: 90.0
  group: enemy
  wall: false              # 壁で遮断
  random: true            # ランダムに選択
  max_targets: 5
  filter: hostile         # 敵対MOBのみ
```

### ターゲットフィルター

| フィルタ値 | 説明 |
|-----------|----|
| `all` | 全エンティティ（デフォルト） |
| `players` | プレイヤーのみ |
| `mobs` | MOBのみ |
| `hostile` | 敵対MOBのみ |

---

## エフェクト設定

### エフェクトタイプ

#### sound（サウンド）

```yaml
effects:
  cast:
    - type: sound
      sound: ENTITY_PLAYER_ATTACK_SWEEP  # Minecraftサウンド名
      volume: 1.0                        # 音量 0-2
      pitch: 1.0                         # ピッチ 0.5-2.0
```

#### particle（パーティクル）

```yaml
effects:
  cast:
    - type: particle
      particle: FLAME                    # パーティクルタイプ
      count: 20                          # 数量
      offset: 0.5                        # オフセット（ブロック）
```

主なパーティクルタイプ:
- `FLAME`, `SMOKE_LARGE`, `EXPLOSION_LARGE`
- `CRIT`, `SWEEP_ATTACK`, `TOTEM`
- `CLOUD`, `REDSTONE`, `LAVA`

### エフェクトタイミング

| タイミング | 説明 |
|-----------|------|
| `cast` | スキル発動時 |
| `hit` | ターゲットヒット時 |
| `impact` | 着弾時（投射物など） |
| `active` | 効果持続中 |
| `fade` | 効果終了時 |

### 複合エフェクト例

```yaml
effects:
  cast:
    - type: sound
      sound: ENTITY_BLAZE_SHOOT
      volume: 1.0
      pitch: 0.8
    - type: particle
      particle: FLAME
      count: 20

  impact:
    - type: sound
      sound: ENTITY_GENERIC_EXPLODE
      volume: 2.0
      pitch: 1.0
    - type: particle
      particle: EXPLOSION_LARGE
      count: 1

  duration:
    ticks: 60           # 3秒間（60tick）
    interval: 10        # 0.5秒ごと
    effects:
      - type: damage
        damage: 5.0
      - type: particle
        particle: SMOKE_LARGE
```

---

## バフ/デバフ設定

### buff_effects（バフ効果）

```yaml
buff_effects:
  duration:
    formula: "30 + Lv * 5"  # 効果時間（秒）

  effects:
    - type: movement_speed
      value: 0.2            # 20%上昇
      stack_type: add       # 加算/乗算

    - type: attack_speed
      value: 0.15           # 15%上昇
      stack_type: add

    - type: stat_boost
      stat: DEXTERITY
      value: 10             # DEX+10
      stack_type: add

    - type: stat_boost
      stat: STRENGTH
      value: 5              # STR+5
      stack_type: add
```

### debuffs（デバフ付与）

```yaml
debuffs:
  - type: burn
    duration: 5            # 5秒間燃焼
    damage_per_sec: 8      # 秒間ダメージ

  - type: slow
    duration: 3            # 3秒間減速
    slow_percent: 0.3      # 30%減速

  - type: poison
    duration: 10           # 10秒間毒
    damage_per_sec: 5
```

### additional_effects（追加効果）

```yaml
additional_effects:
  # ノックバック
  - type: knockback
    strength: 1.5          # ノックバック強度
    vertical: 0.3          # 垂直ノックバック

  # ライフスティール
  - type: lifesteal
    percent: 0.2           # 与ダメージの20%回復

  # スタン
  - type: stun
    duration: 2            # 2秒間スタン
```

---

## スキルツリー設定

### 基本構造

```yaml
skill_tree:
  parent: parent_skill_id  # 親スキルID（noneでルート）
  unlock_requirements:     # 習得要件
    - type: level
      value: 10
    - type: stat
      stat: STRENGTH
      value: 30
    - type: skill
      skill_id: basic_skill
      level: 5
  cost: 3                  # 習得コスト（スキルポイント）
```

### 要件タイプ

| タイプ | 説明 | サブフィールド |
|--------|------|---------------|
| `level` | プレイヤーレベル | `value` |
| `stat` | ステータス値 | `stat`, `value` |
| `skill` | スキル習得 | `skill_id`, `level` |
| `class` | クラス制限 | `classes` |

### 要件例

```yaml
skill_tree:
  parent: fireball_basic
  unlock_requirements:
    # レベル要件
    - type: level
      value: 15

    # ステータス要件
    - type: stat
      stat: INTELLIGENCE
      value: 40
    - type: stat
      stat: SPIRIT
      value: 30

    # スキル要件
    - type: skill
      skill_id: fireball_basic
      level: 5

    # クラス要件
    - type: class
      classes:
        - Mage
        - Wizard
        - Sorcerer

  cost: 5
```

---

## 使用条件

### usage_requirements

```yaml
usage_requirements:
  # HPが一定以上必要
  - type: min_hp_percent
    value: 10              # HPが10%以上必要

  - type: min_hp_value
    value: 20              # HPが20以上必要

  # MPが一定以上必要
  - type: min_mp_percent
    value: 25              # MPが25%以上必要

  # 特定の武器装着時のみ使用可
  - type: weapon_type
    types:
      - SWORD
      - AXE

  # 特定の状態でないと使用不可
  - type: not_condition
    conditions:
      - silenced
      - stunned
```

---

## GUI設定

### icon_material

Minecraftのアイテムマテリアルを指定します。

```yaml
icon_material: DIAMOND_SWORD
```

主なマテリアル:
- 武器: `DIAMOND_SWORD`, `IRON_AXE`, `BOW`
- 魔法: `BLAZE_ROD`, `FIRE_CHARGE`, `FEATHER`
- 消耗品: `POTION`, `GOLDEN_APPLE`
- その他: `DIAMOND`, `REDSTONE`, `ENDER_PEARL`

### available_classes

スキルを使用可能なクラスを制限します。

```yaml
available_classes:
  - Warrior
  - Knight
  - Samurai

# 全クラスで使用可能
available_classes:
  - All
```

---

## 完全スキーマ

```yaml
# ============================================
# スキルYAML完全スキーマ
# ============================================

# --- 基本情報 ---
id: string                   # 必須: スキルID
name: string                 # 必須: スキル名
display_name: string          # 必須: 表示名（カラーコード可）
type: string                 # 必須: normal（active/passiveは廃止）
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

# --- パッシブ効果（発動型スキル以外の場合） ---
# パッシブ効果はカスタムフィールドで定義
# crit_chance: "5 + Lv * 0.5"
# crit_damage: "150 + Lv * 5"
# magic_defense: "10 + Lv * 2"

# --- ターゲット設定 ---
targeting:                   # 必須
  type: string               # self|self_plus_one|nearest_hostile|nearest_player|
                             # nearest_entity|area_self|area_others|sphere|
                             # line|cone|looking|external
  # 汎用ターゲット設定（LINE, LOOKING, CONE, SPHERE用）
  range: number              # 範囲（ブロック）
  line_width: number         # 直線の幅（LINE, LOOKING用）
  cone_angle: number         # コーンの角度（度数法、CONE用）
  sphere_radius: number      # 球形の半径（SPHERE用）
  # 従来の範囲設定（後方互換性）
  cone:
    angle: number
    range: number
  sphere:
    radius: number
  rect:
    width: number
    depth: number
  circle:
    radius: number
  # その他
  filter: string             # all|players|mobs|hostile
  max_targets: integer       # 最大ターゲット数
  group: string              # both|enemy|ally（グループフィルタ）
  wall: boolean              # 壁を透過するか（デフォルト: false）
  random: boolean            # ランダム順序（デフォルト: false）
  caster: boolean            # キャスターを含める（デフォルト: false）

# --- エフェクト設定 ---
effects:                     # オプション
  cast:                      # 発動時
    - type: sound|particle
      # タイプごとのパラメータ
  hit:                       # ヒット時
    - ...
  impact:                    # 着弾時
    - ...
  duration:                  # 持続効果
    ticks: integer
    interval: integer
    effects:
      - ...

# --- デバフ付与 ---
debuffs:                     # オプション
  - type: burn|slow|poison|stun
    duration: number
    # タイプごとのパラメータ

# --- 追加効果 ---
additional_effects:          # オプション
  - type: knockback|lifesteal|stun
    # タイプごとのパラメータ

# --- 使用条件 ---
usage_requirements:          # オプション
  - type: min_hp_percent|min_hp_value|min_mp_percent|weapon_type
    value: number

# --- スキルツリー ---
skill_tree:                  # オプション
  parent: string             # 親スキルID
  unlock_requirements:       # 習得要件
    - type: level|stat|skill|class
      # タイプごとのパラメータ
  cost: integer              # 習得コスト

# --- GUI設定 ---
icon_material: string         # アイコンマテリアル
available_classes:           # 使用可能クラス
  - string
```
