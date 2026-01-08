# Phase15 PvP設定とMythicMobs詳細判定 - 分析結果

## 更新日時
2026-01-08

## 結論
- **PvP設定**: 未サポート（PvE環境に特化）
- **MythicMobs詳細判定**: 不要（ドロップ管理機能のみ実装済み）

## 修正ファイル

### 1. SkillManager.java:788-798
- 変更前: `// TODO: PvP設定に基づいた判定`
- 変更後: `// PvE環境のためPvPは未サポート`
- 変更前: `// TODO: MythicMobs等との連携で詳細な判定を行う`
- 変更後: `// MythicMobs連携はドロップ管理機能のみ実装済み`

### 2. DamageManager.java:155-157
- 変更前: `// 将来的にはPvP補正を実装予定`
- 変更後: `// PvPは未サポートのため、バニラの動作に委ねる`

### 3. ActiveSkillExecutor.java:143-145
- 変更前: `// TODO: MythicMobs等との連携で詳細な判定を行う`
- 変更後: `// MythicMobs連携はドロップ管理機能のみ実装済み`

### 4. MythicMobsHook.java:257-260
- 変更前: `// TODO: MythicMobs 5.6+でAPIを調査して実装`
- 変更後: `// ドロップ制御はMythicDeathListener側で実装済み`

### 5. spec.md
- 新規追加: 「スコープ外の機能」セクション
  - PvP未サポートの明記
  - 仕様の明確化

## MythicMobs連携の現状
- **実装済み**: ドロップ管理（MythicMobsManager, DropHandler, MythicDeathListener）
- **メソッド利用可能**: `isMythicMob()`, `getMobId()`, `getMobLevel()`, etc.
- **スキルターゲット判定**: MythicMobsとバニラMOBを区別する必要はない

## PvP未サポートの理由
1. プロジェクトはPvE（Player vs Environment）に特化
2. spec.mdにPvP機能の記述がない
3. プレイヤー間の攻撃はバニラの動作に委ねる
