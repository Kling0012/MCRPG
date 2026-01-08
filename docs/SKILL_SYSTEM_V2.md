# スキルシステム V2 ドキュメント

## 目次

1. [概要](#概要)
2. [アーキテクチャ](#アーキテクチャ)
3. [新機能](#新機能)
4. [移行ガイド](#移行ガイド)
5. [ベストプラクティス](#ベストプラクティス)

---

## 概要

RPGPlugin スキルシステム V2は、YAMLベースの柔軟なスキル定義システムを提供します。

### 主な特徴

- **数式ベースのダメージ計算**: カスタム数式で複雑な計算を記述可能
- **レベル依存パラメータ**: コスト、クールダウン、効果をレベルに応じて変化
- **複数コストタイプ**: MP、HP、スタミナなどのリソース消費に対応
- **柔軟なターゲティング**: 単体、コーン、球形、扇形など多様な範囲攻撃
- **バフ/デバフシステム**: ステータス強化や弱体化効果
- **スキルツリー**: 前提条件を設定したスキル習得ツリー

---

## アーキテクチャ

### コアコンポーネント

```
┌─────────────────────────────────────────────────────────────┐
│                      SkillManager                           │
│  - スキル登録・取得                                          │
│  - プレイヤースキルデータ管理                                │
│  - スキル実行の Orchestrator                                 │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  SkillLoader │  │SkillExecutor │  │ SkillTree    │
│  - YAML読込  │  │  - 実行ロジック│  │  - ツリー構造│
└──────────────┘  └──────────────┘  └──────────────┘
        │                   │
        ▼                   ▼
┌──────────────┐  ┌──────────────────────────────────┐
│   Skill      │  │    FormulaDamageCalculator       │
│  - データ    │  │  - 数式評価                      │
│  - 設定      │  │  - 変数管理                      │
└──────────────┘  └──────────────────────────────────┘
        │
        ▼
┌──────────────────────────────────────────┐
│          Target System                   │
│  - TargetType                            │
│  - TargetSelector                        │
│  - AreaShape (Phase11-4)                 │
└──────────────────────────────────────────┘
```

### 設計原則

| 原則 | 適用 |
|------|------|
| **SOLID-S** | 各クラスは単一責務を持つ（Skill: データ、Calculator: 計算、Executor: 実行） |
| **SOLID-O** | 新しいTargetTypeやEffectTypeを追加可能 |
| **DRY** | 数式評価ロジックはFormulaEvaluatorに集約 |
| **KISS** | YAML形式で直感的にスキルを定義可能 |
| **YAGNI** | 必要な機能のみ実装、過剰な抽象化を回避 |

---

## 新機能

### Phase11-4: ターゲットパッケージ（スキルエフェクト範囲システム）

#### TargetType 列挙型

```java
public enum TargetType {
    SELF,           // 自分自身
    NEAREST_HOSTILE, // 最も近い敵対MOB
    NEAREST_ENTITY,  // 最も近いエンティティ
    AREA,           // エリア内全て
    CONE,           // コーン状範囲
    SPHERE,         // 球形範囲
    SECTOR,         // 扇形範囲
    EXTERNAL        // 外部指定（SKript等）
}
```

#### AreaShape と ShapeCalculator

```java
// エリア形状の定義
public interface AreaShape {
    boolean isInShape(Location target, Location center, Vector direction);
    Collection<Entity> getEntitiesInShape(Location center, Vector direction);
}

// 形状計算ロジック
public class ShapeCalculator {
    public static List<Entity> getEntitiesInCone(Location center, Vector direction, double angle, double range);
    public static List<Entity> getEntitiesInSphere(Location center, double radius);
    public static List<Entity> getEntitiesInSector(Location center, Vector direction, double angle, double radius);
}
```

### Phase11-5: 外部API拡張（ターゲット・SKript連携）

#### RPGPluginAPI

```java
public interface RPGPluginAPI {
    // プレイヤーデータ
    Optional<RPGPlayer> getRPGPlayer(Player player);

    // スキル実行（外部ターゲット指定対応）
    boolean castSkill(Player caster, String skillId, Entity target);
    boolean castSkillAtLocation(Player caster, String skillId, Location location);

    // ターゲット取得
    List<Entity> getTargetsInArea(Player caster, TargetType type, AreaShape shape);
}
```

#### SKriptBridge

```skript
# スキル実行イベント
on rpg skill cast:
    if skill-id is "fireball":
        send "&cファイアボールを発射！"
        cancel event  # デフォルト動作をキャンセルしてカスタム処理

# カスタムターゲット指定
command /customfireball:
    trigger:
        make player cast "fireball" at targeted entity
```

### Phase11-6: 数式ベースダメージ計算

#### 変数定義

YAMLでカスタム変数を定義可能：

```yaml
variables:
  str_scale: 1.8
  level_bonus: 5.0
  base_mod: 1.0
```

#### 数式構文

| 構文 | 説明 | 例 |
|------|------|-----|
| `STR`, `INT`, `SPI`, `VIT`, `DEX` | プレイヤーステータス | `STR * 1.5` |
| `Lv` | スキルレベル | `Lv * 10` |
| `+`, `-`, `*`, `/`, `%`, `^` | 基本演算子 | `STR + Lv * 5` |
| `floor()`, `ceil()`, `round()` | 丸め関数 | `floor(STR * 1.2)` |
| `abs()`, `min()`, `max()` | ユーティリティ関数 | `min(x, 100)` |
| カスタム変数名 | variablesで定義した変数 | `base_mod * 10` |

#### レベル別数式

特定レベルで計算式をオーバーライド：

```yaml
damage:
  formula: "STR * str_scale + Lv * 10"
  levels:
    5: "STR * str_scale * 1.5 + Lv * 15"
    10: "(STR + INT) * 2.0 + Lv * 20"
```

### Phase11-8: データベーススキーマ更新

#### スキルデータ永続化

```sql
-- プレイヤースキルデータ
CREATE TABLE player_skills (
    uuid VARCHAR(36) PRIMARY KEY,
    player_id BIGINT,
    skill_data TEXT,  -- JSON形式でスキル習得状況を保存
    last_updated TIMESTAMP
);

-- スキル定義キャッシュ
CREATE TABLE skill_definitions (
    skill_id VARCHAR(64) PRIMARY KEY,
    definition TEXT,  -- YAML形式のスキル定義
    version INT,
    checksum VARCHAR(32)
);
```

---

## 移行ガイド

### 旧フォーマットから新フォーマットへの移行

#### 1. 基本スキルの移行

**旧フォーマット:**
```yaml
id: power_strike
name: パワーストライク
display_name: "&6パワーストライク"
type: active
max_level: 5
cooldown: 8.0
mana_cost: 10

damage:
  base: 50.0
  stat_multiplier:
    stat: STRENGTH
    multiplier: 1.5
  level_multiplier: 10.0
```

**新フォーマット:**
```yaml
id: power_strike
name: パワーストライク
display_name: "&6パワーストライク"
type: active
max_level: 5

# レベル依存パラメータ
cost:
  type: mana
  base: 10

cooldown:
  base: 8.0

# 数式形式
variables:
  str_scale: 1.5

damage:
  formula: "50 + STR * str_scale + Lv * 10"
```

#### 2. 変換ルール

| 旧フィールド | 新フィールド | 変換式 |
|-------------|-------------|--------|
| `mana_cost` | `cost.base` | `cost.type: mana` |
| `cooldown` | `cooldown.base` | そのまま |
| `damage.base` | 数式の定数項 | `base` |
| `damage.stat_multiplier.multiplier` | variables | `X_scale` 変数 |
| `damage.level_multiplier` | 数式の係数 | `* Lv` |

#### 3. 自動変換スクリプト

```java
// SkillConverter.java
public class SkillConverter {
    public static String convertToV2(String oldYaml) {
        // 旧YAMLをパース
        YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldYaml);

        // 新形式に変換
        Map<String, Object> newConfig = new HashMap<>();

        // 基本フィールドはそのまま
        newConfig.put("id", oldConfig.getString("id"));
        // ... 他のフィールド

        // ダメージ変換
        if (oldConfig.contains("damage")) {
            Map<String, Object> damage = new HashMap<>();
            double base = oldConfig.getDouble("damage.base", 0);
            double multiplier = oldConfig.getDouble("damage.stat_multiplier.multiplier", 0);
            double levelMult = oldConfig.getDouble("damage.level_multiplier", 0);

            String formula = String.format("%.1f + STR * %.2f + Lv * %.1f",
                base, multiplier, levelMult);
            damage.put("formula", formula);
            newConfig.put("damage", damage);
        }

        return YamlConfiguration.dump(newConfig);
    }
}
```

---

## ベストプラクティス

### スキルバランス設計

#### ダメージ計算のガイドライン

| スキルタイプ | 基本ダメージ | ステータス倍率 | レベル倍率 |
|-------------|-------------|---------------|-----------|
| 単体攻撃 | 30-50 | 1.5-2.0 | 5-10 |
| 範囲攻撃 | 20-40 | 1.0-1.5 | 3-8 |
| ヘビー攻撃 | 50-80 | 2.0-3.0 | 10-15 |
| バフ/デバフ | - | - | - |

#### コストバランス

```yaml
# 良い例: レベルと共にバランスが取れている
cost:
  type: mana
  base: 15        # 基本コスト
  per_level: -1   # レベルで低下（使いやすくなる）
  min: 5          # 最低コスト

# 注意: レベルと共に上昇しすぎる例
cost:
  type: mana
  base: 20
  per_level: 10   # Lv10で120消費は高すぎる
```

### 数式記述のコツ

```yaml
# 良い例: 変数を使用して調整しやすい
variables:
  str_scale: 1.8
  level_bonus: 5.0
damage:
  formula: "STR * str_scale + Lv * level_bonus + 50"

# 避けるべき例: マジックナンバーが多い
damage:
  formula: "STR * 1.832 + Lv * 4.875 + 50.25"
```

### ターゲティング選択

| シチュエーション | 推奨ターゲット | 理由 |
|-----------------|---------------|------|
| 近接単体攻撃 | `single` | 前方1体のみ |
| ワイド攻撃 | `cone` | 前方範囲 |
| 全体攻撃 | `sphere` | 周囲全て |
| 遠距離魔法 | `sector` | 狭い範囲に集中 |

---

## API使用例

### Java API

```java
// スキル実行
RPGPluginAPI api = plugin.getAPI();
api.castSkill(player, "fireball");

// 外部ターゲット指定
Entity target = getTargetEntity();
api.castSkill(player, "fireball", target);

// カスタムターゲットリスト
List<Entity> customTargets = getCustomTargets();
api.castSkillWithTargets(player, "wind_slash", customTargets);
```

### SKript連携

```skript
# スキル使用時にカスタム処理
on rpg skill cast:
    if skill-id is "blood_weapon":
        # HPチェック
        if player's health is less than 10:
            send "&cHPが足りません！"
            cancel event
        else:
            send "&4血の力で攻撃！"

# スキルレベル取得時の処理
on rpg skill level up:
    send "&a%skill-id% がレベル %new-level% になりました！"
```

---

## トラブルシューティング

### よくある問題

#### Q: 数式がエラーになる

A: 以下を確認してください：
1. 変数名が正しく定義されているか
2. 括弧の対応が正しいか
3. 使用していない関数がないか

#### Q: ターゲットが正しく選択されない

A:
1. `targeting.type` が正しいか確認
2. 範囲パラメータ（radius, range）が適切か確認
3. `max_targets` 制限を確認

#### Q: レベルアップで効果が変わらない

A:
1. `damage.levels` にレベル別定義があるか確認
2. `per_level` 値が0でないか確認

---

## リファレンス

- [YAMLリファレンス](YAML_REFERENCE.md) - 詳細なYAML仕様
- [APIドキュメント](API_DOCUMENTATION.md) - Java/SKript API
- [サンプルスキル](../skills/active/) - 実装例
