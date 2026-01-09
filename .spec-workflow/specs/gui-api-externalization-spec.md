# GUI操作の外部化・API化仕様書

## 作成日
2026-01-08

## 作業ブランチ
vk/13de-gui

## 概要

本仕様は、Minecraft RPGプラグインにおける全てのGUI操作および内部操作をコマンドまたは外部APIから呼び出せるようにするための設計仕様である。

## 背景

現在のシステムでは、以下のGUIが実装されているが、これらはGUIクリック操作のみで実行可能であり、コマンドや外部API（SKript/Denizen）から直接呼び出す手段が限られている。

### 現在実装済みのGUI一覧

| GUI名 | クラス | 操作内容 |
|--------|--------|----------|
| ステータス配分GUI | `StatMenu` | ステータスポイントの追加/削除、保存/キャンセル |
| スキルツリーGUI | `SkillMenu` | スキル習得、スキル強化、ツリー表示 |
| クラス選択GUI | `ClassMenu` | 初期クラス選択、クラスアップ、クラス情報表示 |
| トレードGUI | `TradeInventory` | プレイヤー間トレード |

### 既存の外部API

既に `RPGPluginAPI` インターフェースと `SKriptBridge` が実装されているが、GUI操作に対応する以下の機能が不足している。

## 方針

### 基本原則

1. **操作の外部化**: 全てのGUI操作はコマンドまたはAPIから実行可能にする
2. **SKript/Denizen対応**: 外部スクリプトから呼び出せるAPIを提供する
3. **権限管理**: 各操作に適切な権限チェックを設ける
4. **一貫性**: コマンド構文とAPI構文の一貫性を保つ

## 設計

### 1. ステータス操作の外部化

#### 1.1 コマンド拡張

```bash
# 既存
/rpg stats              # ステータスGUIを開く

# 新規追加
/rpg stats add <stat> [amount]         # ステータスにポイント追加
/rpg stats remove <stat> [amount]      # ステータスからポイント削除
/rpg stats set <stat> <value>          # ステータス値を直接設定
/rpg stats reset                       # ステータス配分をリセット
/rpg stats available                   # 利用可能ポイントを表示
```

#### 1.2 API拡張

`RPGPluginAPI` に以下のメソッドを追加:

```java
/**
 * ステータスにポイントを追加する
 * @param player プレイヤー
 * @param stat ステータス種別
 * @param amount 追加量（デフォルト: 1）
 * @return 成功した場合はtrue
 */
boolean addStatPoint(Player player, Stat stat, int amount);

/**
 * ステータスからポイントを削除する
 * @param player プレイヤー
 * @param stat ステータス種別
 * @param amount 削除量（デフォルト: 1）
 * @return 成功した場合はtrue
 */
boolean removeStatPoint(Player player, Stat stat, int amount);

/**
 * ステータス配分をリセットする
 * @param player プレイヤー
 * @return 成功した場合はtrue
 */
boolean resetStats(Player player);

/**
 * 利用可能なステータスポイントを取得する
 * @param player プレイヤー
 * @return 利用可能ポイント数
 */
int getAvailableStatPoints(Player player);
```

#### 1.3 SKript Bridge拡張

```
# SKriptから呼び出し可能なアクション
rpg api add_stat <player> <stat> [amount]
rpg api remove_stat <player> <stat> [amount]
rpg api reset_stats <player>
rpg api get_available_points <player>
```

### 2. スキル操作の外部化

#### 2.1 コマンド拡張

```bash
# 既存
/rpg skill              # スキルGUIを開く
/rpg cast <skillId>     # スキル発動

# 新規追加
/rpg skill acquire <skillId>           # スキル習得
/rpg skill upgrade <skillId>           # スキル強化
/rpg skill info [skillId]              # スキル情報表示
/rpg skill list                        # 習得済みスキル一覧
/rpg skill points                      # スキルポイント表示
/rpg skill reset                       # スキルリセット（ポイント返還）
```

#### 2.2 API拡張

`RPGPluginAPI` に以下のメソッドを追加/拡張:

```java
/**
 * スキルを習得する（ポイント消費あり）
 * @param player プレイヤー
 * @param skillId スキルID
 * @return 成功した場合はtrue
 */
boolean acquireSkill(Player player, String skillId);

/**
 * スキルを強化する
 * @param player プレイヤー
 * @param skillId スキルID
 * @return 成功した場合はtrue
 */
boolean upgradeSkill(Player player, String skillId);

/**
 * スキル情報を取得する
 * @param skillId スキルID
 * @return スキル情報、存在しない場合はempty
 */
Optional<Skill> getSkill(String skillId);

/**
 * スキルポイントを取得する
 * @param player プレイヤー
 * @return 現在のスキルポイント
 */
int getSkillPoints(Player player);

/**
 * 全スキルをリセットする
 * @param player プレイヤー
 * @param refundPoints ポイントを返還するか
 * @return 成功した場合はtrue
 */
boolean resetAllSkills(Player player, boolean refundPoints);

/**
 * 指定スキルの発動を試みる
 * @param player プレイヤー
 * @param skillId スキルID
 * @param target ターゲット（省略時は自分）
 * @return 成功した場合はtrue
 */
boolean castSkillAt(Player player, String skillId, Entity target);

/**
 * コストタイプを指定してスキル発動
 * @param player プレイヤー
 * @param skillId スキルID
 * @param costType コストタイプ（MANA/HP）
 * @return 成功した場合はtrue
 */
boolean castSkillWithCostType(Player player, String skillId, SkillCostType costType);
```

#### 2.3 SKript Bridge拡張

```
# 新規アクション
rpg api acquire_skill <player> <skillId>
rpg api upgrade_skill <player> <skillId>
rpg api get_skill_points <player>
rpg api reset_skills <player> [refund]
rpg api cast_at <player> <skillId> [targetPlayer]
rpg api cast_with_cost <player> <skillId> <costType>
```

### 3. クラス操作の外部化

#### 3.1 コマンド拡張

```bash
# 既存
/rpg class              # クラスGUIを開く

# 新規追加
/rpg class set <classId>                # クラス設定
/rpg class upgrade                      # クラスアップ
/rpg class info [classId]               # クラス情報表示
/rpg class list                         # 利用可能クラス一覧
/rpg class requirements <classId>       # クラス習得要件表示
```

#### 3.2 API拡張

`RPGPluginAPI` に以下のメソッドを追加:

```java
/**
 * 初期クラスを設定する
 * @param player プレイヤー
 * @param classId クラスID
 * @return 成功した場合はtrue
 */
boolean setInitialClass(Player player, String classId);

/**
 * クラス情報を取得する
 * @param classId クラスID
 * @return クラス情報、存在しない場合はempty
 */
Optional<RPGClass> getClass(String classId);

/**
 * 全クラス一覧を取得する
 * @return 全クラスのリスト
 */
List<RPGClass> getAllClasses();

/**
 * 初期クラス一覧を取得する
 * @return 初期クラス（Rank1）のリスト
 */
List<RPGClass> getInitialClasses();

/**
 * クラス習得要件をチェックする
 * @param player プレイヤー
 * @param classId クラスID
 * @return チェック結果
 */
ClassUpgrader.ClassUpResult checkClassRequirements(Player player, String classId);
```

#### 3.3 SKript Bridge拡張

```
# 新規アクション
rpg api set_initial_class <player> <classId>
rpg api get_class_info <classId>
rpg api list_classes
rpg api check_class_requirements <player> <classId>
```

### 4. GUI操作の外部化

#### 4.1 GUIコマンド

```bash
# GUIを直接開くコマンド
/rpg gui stat                 # ステータスGUIを開く
/rpg gui skill                # スキルGUIを開く
/rpg gui class                # クラスGUIを開く
/rpg gui class info <classId> # クラス情報GUIを開く
```

#### 4.2 GUI API

`RPGPluginAPI` に以下のメソッドを追加:

```java
/**
 * ステータスGUIを開く
 * @param player プレイヤー
 */
void openStatGUI(Player player);

/**
 * スキルGUIを開く
 * @param player プレイヤー
 */
void openSkillGUI(Player player);

/**
 * クラス選択GUIを開く
 * @param player プレイヤー
 */
void openClassGUI(Player player);

/**
 * クラス情報GUIを開く
 * @param player プレイヤー
 * @param classId クラスID
 */
void openClassInfoGUI(Player player, String classId);
```

## 既存コンポーネントの再利用

### 既存の利用可能なコンポーネント

| コンポーネント | 用途 |
|----------------|------|
| `RPGPluginAPI` | APIインターフェース本体 |
| `SKriptBridge` | SKript/Denizenブリッジ |
| `StatManager` | ステータス管理 |
| `SkillManager` | スキル管理 |
| `ClassManager` | クラス管理 |
| `ClassUpgrader` | クラスアップ処理 |
| `StatMenu` | ステータスGUI |
| `SkillMenu` | スキルGUI |
| `ClassMenu` | クラスGUI |

### 統合ポイント

- **RPGCommand**: 新しいサブコマンドのハンドラーを追加
- **APICommand**: SKriptBridgeに新しいアクションを追加
- **RPGPluginAPIImpl**: 新しいAPIメソッドの実装

## アーキテクチャ

```
┌─────────────────────────────────────────────────────────────────────┐
│                        外部インターフェース層                       │
├─────────────────────────────────────────────────────────────────────┤
│  コマンドレイヤー          │           APIブリッジレイヤー          │
│  ┌──────────────┐          │           ┌──────────────┐            │
│  │ RPGCommand   │          │           │SKriptBridge  │            │
│  │ - stats      │          │           │ - add_stat   │            │
│  │ - skill      │  ◄───────┼───────────│ - acquire... │            │
│  │ - class      │          │           │ - cast_...   │            │
│  │ - gui        │          │           └──────────────┘            │
│  └──────────────┘          │                                      │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                            APIレイヤー                              │
├─────────────────────────────────────────────────────────────────────┤
│  ┌────────────────────────────────────────────────────────────┐    │
│  │                    RPGPluginAPI                            │    │
│  │  - addStatPoint(), removeStatPoint(), resetStats()        │    │
│  │  - acquireSkill(), upgradeSkill(), castSkillAt()          │    │
│  │  - setInitialClass(), checkClassRequirements()           │    │
│  │  - openStatGUI(), openSkillGUI(), openClassGUI()         │    │
│  └────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         ビジネスロジック層                           │
├─────────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │
│  │ StatManager  │  │ SkillManager │  │ ClassManager │             │
│  └──────────────┘  └──────────────┘  │ ClassUpgrader│             │
│                                       └──────────────┘             │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                            GUIレイヤー                               │
├─────────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │
│  │  StatMenu    │  │  SkillMenu   │  │  ClassMenu   │             │
│  └──────────────┘  └──────────────┘  └──────────────┘             │
└─────────────────────────────────────────────────────────────────────┘
```

## データモデル

### APIレスポンス構造

```java
/**
 * API操作の結果を表す
 */
public class APIResult {
    private final boolean success;
    private final String message;
    private final Object data;

    public static APIResult success(String message, Object data) {
        return new APIResult(true, message, data);
    }

    public static APIResult failure(String message) {
        return new APIResult(false, message, null);
    }

    // getters...
}
```

### スキル情報DTO

```java
/**
 * スキル情報の転送用オブジェクト
 */
public class SkillInfoDTO {
    private final String id;
    private final String displayName;
    private final String type;
    private final int maxLevel;
    private final int currentLevel;
    private final int manaCost;
    private final int cooldown;
    private final List<String> description;
    private final boolean canAcquire;
    private final List<String> requirements;

    // getters...
}
```

## エラーハンドリング

### エラーシナリオ

| シナリオ | ハンドリング | ユーザーへの表示 |
|----------|--------------|------------------|
| ポイント不足 | 操作をキャンセル | "ポイントが足りません" |
| スキル未習得 | スキル習得を試みる | "スキルを習得していません" |
| 要件未達 | 要件を表示 | "習得条件を満たしていません" |
| 権限不足 | エラーを返す | "権限がありません" |
| プレイヤー不在 | エラーを返す | "プレイヤーが見つかりません" |
| 無効な引数 | エラーを返す | "無効な引数です" |

## テスト戦略

### ユニットテスト

- APIメソッドの個別テスト
- エッジケースのテスト（ポイント不足、権限なし等）
- モックを使用したマネージャーのテスト

### 統合テスト

- コマンドからAPIまでのフローテスト
- SKriptBridge経由での呼び出しテスト
- GUI操作との整合性テスト

### E2Eテスト

- 実際のゲーム内での動作確認
- 複数の操作を組み合わせたシナリオ

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

## 権限設計

| 権限 | 説明 | デフォルト |
|------|------|------------|
| `rpg.command.stats` | ステータスコマンド使用 | 全プレイヤー |
| `rpg.command.skill` | スキルコマンド使用 | 全プレイヤー |
| `rpg.command.class` | クラスコマンド使用 | 全プレイヤー |
| `rpg.command.gui` | GUIコマンド使用 | 全プレイヤー |
| `rpg.api.modify` | APIでのデータ変更 | 管理者のみ |
| `rpg.api.gui` | APIでのGUI操作 | 全プレイヤー |

## 将来の拡張

- WebSocketベースのGUI操作通知
- REST API endpoint（外部ツール連携用）
- イベントベースのGUI状態同期
- バッチ操作API（複数プレイヤーへの操作）

## 関連ドキュメント

- [Phase14 実装レポート](memory:phase14_implementation_report)
- [スキルツリー自動更新システム](memory:skill_tree_auto_update_system)
- [プロジェクト概要](memory:project_overview)

## 更新履歴

| 日付 | バージョン | 変更内容 |
|------|-----------|----------|
| 2026-01-08 | 1.0.0 | 初版作成 |
