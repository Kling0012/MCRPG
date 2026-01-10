# YAML更新時の削除処理とリロードコマンド実装

## 作成日
2026-01-10

## 作業ブランチ
vk/54a5-core-yaml

## 概要

職業クラス/スキルのYAML更新時に削除されたデータをDBから削除する処理を実装した。

## 実装内容

### 1. ClassManagerに差分検出と削除処理を追加

**ファイル**: `src/main/java/com/example/rpgplugin/rpgclass/ClassManager.java`

**追加メソッド**:
- `reloadWithCleanup(Map<String, RPGClass> newClasses)`: クラスをリロードし、削除されたクラスのプレイヤーデータをクリーンアップ
- `clearPlayersWithClass(String classId)`: 指定したクラスを使用している全プレイヤーのクラスを解除
- `ReloadResult` クラス: リロード結果を保持する

**動作**:
- 現在のクラスと新しいクラスを比較
- 削除されたクラスを検出
- 削除されたクラスを使用しているプレイヤーのクラスを解除（nullに設定）
- プレイヤーに通知メッセージを送信

### 2. SkillManagerに差分検出と削除処理を追加

**ファイル**: `src/main/java/com/example/rpgplugin/skill/SkillManager.java`

**追加メソッド**:
- `reloadWithCleanup(Map<String, Skill> newSkills)`: スキルをリロードし、削除されたスキルのプレイヤーデータをクリーンアップ
- `clearRemovedSkillsFromPlayers(Set<String> removedSkillIds)`: 指定したスキルを解放しているプレイヤーからスキルデータを削除
- `CleanupSummary` クラス: クリーンアップサマリー
- `ReloadResult` クラス: スキルリロード結果を保持する

**動作**:
- 現在のスキルと新しいスキルを比較
- 削除されたスキルを検出
- 削除されたスキルを解放しているプレイヤーのスキルデータを削除
- スキルツリーキャッシュを全て無効化
- プレイヤーに通知メッセージを送信

### 3. RPGAdminCommandリロードコマンドの実装

**ファイル**: `src/main/java/com/example/rpgplugin/command/RPGAdminCommand.java` (新規)

**コマンド**:
- `/rpgadmin reload classes` - 職業クラスYAMLを再読み込み
- `/rpgadmin reload skills` - スキルYAMLを再読み込み
- `/rpgadmin reload all` - 全てのYAMLを再読み込み

**機能**:
- 権限チェック (`rpgplugin.admin`)
- タブ補完対応
- 詳細な結果表示（読み込み数、削除数、影響を受けたプレイヤー数）

### 4. plugin.ymlの更新

**ファイル**: `src/main/resources/plugin.yml`

**追加**:
```yaml
rpgadmin:
  description: Admin RPG command for reloading
  usage: /rpgadmin <subcommand>
  aliases: [rpga, rpgadm]
  permission: rpgplugin.admin
```

### 5. RPGPluginの更新

**ファイル**: `src/main/java/com/example/rpgplugin/RPGPlugin.java`

**変更**:
- `registerCommands()`: RPGAdminCommandを登録
- `setupConfigWatcher()`: ホットリロード時にreloadWithCleanupを使用
- `reloadSkillsWithCleanup()`: 新しいヘルパーメソッド追加

### 6. GameSystemManagerの更新

**ファイル**: `src/main/java/com/example/rpgplugin/core/system/GameSystemManager.java`

**追加**:
- `classLoader` フィールド
- `skillLoader` フィールド
- `getClassLoader()` アクセサ
- `getSkillLoader()` アクセサ

## リロード結果クラス

### ClassManager.ReloadResult
- `loadedClassCount`: 読み込んだクラス数
- `removedClasses`: 削除されたクラスIDセット
- `affectedPlayerCount`: 影響を受けたプレイヤー数
- `hasRemovedClasses()`: 削除されたクラスがあるかどうか

### SkillManager.ReloadResult
- `loadedSkillCount`: 読み込んだスキル数
- `removedSkills`: 削除されたスキルIDセット
- `affectedPlayerCount`: 影響を受けたプレイヤー数
- `totalSkillsRemoved`: 削除されたスキルエントリ総数
- `hasRemovedSkills()`: 削除されたスキルがあるかどうか

## プレイヤーへの通知

削除されたデータがある場合、オンラインプレイヤーに以下のメッセージを送信:

- 職業削除: `§c[YAML更新] あなたの職業「{classId}」は削除されました。再度職業を選択してください。`
- スキル削除: `§c[YAML更新] {count}個のスキルが削除されました: {skillIds}`

## 設計原則

- **SOLID-S**: 各クラスが単一の責務を持つ
- **DRY**: リロードロジックを共通化
- **KISS**: シンプルなAPI設計

## 関連ファイル

### 新規ファイル
- `src/main/java/com/example/rpgplugin/command/RPGAdminCommand.java`

### 変更ファイル
- `src/main/java/com/example/rpgplugin/rpgclass/ClassManager.java`
- `src/main/java/com/example/rpgplugin/skill/SkillManager.java`
- `src/main/java/com/example/rpgplugin/RPGPlugin.java`
- `src/main/java/com/example/rpgplugin/core/system/GameSystemManager.java`
- `src/main/resources/plugin.yml`

## 使用方法

### コマンドでのリロード
```
/rpgadmin reload classes  # 職業クラスをリロード
/rpgadmin reload skills   # スキルをリロード
/rpgadmin reload all      # 全てをリロード
```

### ホットリロード
config.ymlで`hot_reload.classes`または`hot_reload.skills`が有効な場合、
ファイルの変更時に自動的にクリーンアップ付きリロードが行われる。
