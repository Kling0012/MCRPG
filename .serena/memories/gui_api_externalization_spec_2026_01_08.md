# GUI操作の外部化・API化仕様

## 作成日
2026-01-08

## 作業ブランチ
vk/13de-gui

## 概要

Minecraft RPGプラグインにおける全てのGUI操作および内部操作をコマンドまたは外部API（SKript/Denizen）から呼び出せるようにするための仕様策定完了。

## 現在実装済みのGUI一覧

| GUI名 | クラス | 操作内容 |
|--------|--------|----------|
| ステータス配分GUI | `StatMenu` | ステータスポイントの追加/削除、保存/キャンセル |
| スキルツリーGUI | `SkillMenu` | スキル習得、スキル強化、ツリー表示 |
| クラス選択GUI | `ClassMenu` | 初期クラス選択、クラスアップ、クラス情報表示 |
| トレードGUI | `TradeInventory` | プレイヤー間トレード |

## 追加すべきAPIメソッド

### ステータス操作
- `boolean addStatPoint(Player player, Stat stat, int amount)`
- `boolean removeStatPoint(Player player, Stat stat, int amount)`
- `boolean resetStats(Player player)`
- `int getAvailableStatPoints(Player player)`

### スキル操作
- `boolean acquireSkill(Player player, String skillId)`
- `boolean upgradeSkill(Player player, String skillId)`
- `Optional<Skill> getSkill(String skillId)`
- `int getSkillPoints(Player player)`
- `boolean resetAllSkills(Player player, boolean refundPoints)`
- `boolean castSkillAt(Player player, String skillId, Entity target)`
- `boolean castSkillWithCostType(Player player, String skillId, SkillCostType costType)`

### クラス操作
- `boolean setInitialClass(Player player, String classId)`
- `Optional<RPGClass> getClass(String classId)`
- `List<RPGClass> getAllClasses()`
- `List<RPGClass> getInitialClasses()`
- `ClassUpgrader.ClassUpResult checkClassRequirements(Player player, String classId)`

### GUI操作
- `void openStatGUI(Player player)`
- `void openSkillGUI(Player player)`
- `void openClassGUI(Player player)`
- `void openClassInfoGUI(Player player, String classId)`

## 追加すべきコマンド

### ステータス
```
/rpg stats add <stat> [amount]
/rpg stats remove <stat> [amount]
/rpg stats set <stat> <value>
/rpg stats reset
/rpg stats available
```

### スキル
```
/rpg skill acquire <skillId>
/rpg skill upgrade <skillId>
/rpg skill info [skillId]
/rpg skill list
/rpg skill points
/rpg skill reset
```

### クラス
```
/rpg class set <classId>
/rpg class upgrade
/rpg class info [classId]
/rpg class list
/rpg class requirements <classId>
```

### GUI
```
/rpg gui stat
/rpg gui skill
/rpg gui class
/rpg gui class info <classId>
```

## 追加すべきSKript Bridgeアクション

```
rpg api add_stat <player> <stat> [amount]
rpg api remove_stat <player> <stat> [amount]
rpg api reset_stats <player>
rpg api get_available_points <player>

rpg api acquire_skill <player> <skillId>
rpg api upgrade_skill <player> <skillId>
rpg api get_skill_points <player>
rpg api reset_skills <player> [refund]
rpg api cast_at <player> <skillId> [targetPlayer]
rpg api cast_with_cost <player> <skillId> <costType>

rpg api set_initial_class <player> <classId>
rpg api get_class_info <classId>
rpg api list_classes
rpg api check_class_requirements <player> <classId>
```

## 関連ファイル

### SPECドキュメント
- `.spec-workflow/specs/gui-api-externalization-spec.md` - 仕様書
- `.spec-workflow/specs/gui-api-tech-stack.md` - 技術スタック

### 既存実装
- `src/main/java/com/example/rpgplugin/api/RPGPluginAPI.java` - APIインターフェース
- `src/main/java/com/example/rpgplugin/api/RPGPluginAPIImpl.java` - API実装
- `src/main/java/com/example/rpgplugin/api/bridge/SKriptBridge.java` - SKriptブリッジ
- `src/main/java/com/example/rpgplugin/gui/menu/StatMenu.java` - ステータスGUI
- `src/main/java/com/example/rpgplugin/gui/menu/SkillMenu.java` - スキルGUI
- `src/main/java/com/example/rpgplugin/gui/menu/rpgclass/ClassMenu.java` - クラスGUI

## 実装優先順位

### Phase 1: ステータス操作（高優先度）
1. `addStatPoint()` / `removeStatPoint()` API実装
2. SKriptBridge拡張
3. `/rpg stats add/remove` コマンド実装

### Phase 2: スキル操作（高優先度）
1. `acquireSkill()` / `upgradeSkill()` API実装
2. `castSkillAt()` / `castSkillWithCostType()` 実装
3. `/rpg skill acquire/upgrade` コマンド実装

### Phase 3: クラス操作（中優先度）
1. `setInitialClass()` / `checkClassRequirements()` 実装
2. `/rpg class set/info` コマンド実装

### Phase 4: GUI操作API（中優先度）
1. GUIオープンAPI実装
2. `/rpg gui` コマンド実装

## アーキテクチャ

```
外部インターフェース層
    (コマンド + SKriptBridge)
            │
            ▼
    APIファサード層
    (RPGPluginAPI拡張)
            │
            ▼
    ビジネスロジック層
    (StatManager, SkillManager, ClassManager)
            │
            ▼
    GUIレイヤー
    (StatMenu, SkillMenu, ClassMenu)
```

## 設計原則

1. **操作の外部化**: 全てのGUI操作はコマンドまたはAPIから実行可能にする
2. **SKript/Denizen対応**: 外部スクリプトから呼び出せるAPIを提供する
3. **権限管理**: 各操作に適切な権限チェックを設ける
4. **一貫性**: コマンド構文とAPI構文の一貫性を保つ
