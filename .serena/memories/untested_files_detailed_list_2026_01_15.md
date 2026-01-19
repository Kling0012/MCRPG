# 0%カバレッジ対象ファイル詳細リスト

## commandパッケージ

### RPGAdminCommand.java
**行数:** 282行
**主要メソッド:**
- onCommand(): 64-95
- handleReload(): 103-129
- reloadClasses(): 136-163
- reloadSkills(): 170-200
- reloadAll(): 207-248
- showHelp(): 255-261
- onTabComplete(): 264-280

**テスト優先度:** P0
**推定テスト数:** 14テスト
**複雑度:** 中（引数解析、分岐）

## guiパッケージ

### SkillTreeGUI.java
**行数:** 632行
**主要メソッド:**
- コンストラクタ: 64-76
- open(): 95-106
- createInventory(): 113-130
- setupDecoration(): 137-157
- setupSkillItems(): 186-207
- createSkillItem(): 215-275
- acquireSkill(): 422-469
- refundSkill(): 477-517
- refreshGUI(): 522-534
- isSkillSlot(): 559-561
- getSkillIdFromSlot(): 569-583

**テスト優先度:** P0
**推定テスト数:** 20+テスト
**複雑度:** 高（GUIロジック、スキルポイント計算）

### SkillTreeGUIListener.java
**主要メソッド:**
- onInventoryClick(): クリック処理
- onInventoryClose(): クローズ処理

**テスト優先度:** P0
**推定テスト数:** 5テスト
**複雑度:** 中（イベント処理）

## player.expパッケージ

### ExpManager.java
**行数:** 70行
**主要メソッド:**
- コンストラクタ: 30-34
- initialize(): 39-42
- registerListeners(): 47-50
- shutdown(): 55-58
- getExpDiminisher(): 62-64
- getVanillaExpHandler(): 66-68

**テスト優先度:** P0
**推定テスト数:** 6テスト
**複雑度:** 低（ファサードパターン）

## apiパッケージ

### RPGPluginAPI.java
**インターフェース**
**主要メソッド:**
- getPlayerData()
- getClassData()
- getSkillData()
- modifyPlayerStat()
- castSkill()

**テスト優先度:** P1
**推定テスト数:** インターフェース検証のみ

### RPGPluginAPIImpl.java
**実装クラス**
**テスト優先度:** P1
**推定テスト数:** 8テスト

### APICommand.java
**コマンドクラス**
**テスト優先度:** P1
**推定テスト数:** 4テスト

## api.placeholderパッケージ

### RPGPlaceholderExpansion.java
**PlaceholderAPI統合**
**主要プレースホルダー:**
- %rpg_class%
- %rpg_level%
- %rpg_skill_points%
- %rpg_stat_xxx%

**テスト優先度:** P1
**推定テスト数:** 5+テスト

## api.skriptパッケージ（20+ファイル）

### Expressions（9ファイル）
- ExprEventPlayer, ExprEventSkill, ExprEventSkillId, etc.
**推定テスト数:** 9テスト

### Conditions（4ファイル）
- CondCanUpgradeRPGClass, CondHasRPGSkill, etc.
**推定テスト数:** 4テスト

### Effects（4ファイル）
- EffCastRPGSkill, EffSetRPGClass, etc.
**推定テスト数:** 4テスト

### Events（2ファイル）
- EvtRPGSkillCast, EvtRPGSkillDamage
**推定テスト数:** 2テスト

**テスト優先度:** P1
**総推定テスト数:** 19+テスト

## storageパッケージ

### StorageManager.java
**行数:** 未確認
**テスト優先度:** P2
**推定テスト数:** 5テスト

### DatabaseManager.java
**テスト優先度:** P2
**推定テスト数:** 6テスト

### ConnectionPool.java
**テスト優先度:** P2
**推定テスト数:** 5テスト

### SchemaManager.java
**テスト優先度:** P2
**推定テスト数:** 4テスト

### PlayerDataRepository.java
**テスト優先度:** P2
**推定テスト数:** 6テスト

### CacheRepository.java
**テスト優先度:** P2
**推定テスト数:** 4テスト

### migrations/*
**テスト優先度:** P2
**推定テスト数:** 4テスト

## 総計

| 優先度 | パッケージ | 推定テスト数 |
|--------|-----------|-------------|
| P0 | command, gui, player.exp | 45テスト |
| P1 | api, skript | 35+テスト |
| P2 | storage | 34テスト |
| **合計** | | **114+テスト** |

**現在テスト数:** 98
**目標テスト数:** 212+ (98 + 114)
**目標カバレッジ:** 75%以上
