# Phase14 スキルツリーGUI完成

## 更新日時
2026-01-08

## 概要
SkillMenuのTODOを解消し、スキル習得・強化の完全なGUI操作を実現した。

## 実装内容

### 1. checkCanAcquireメソッド実装 (SkillMenu.java:312-365)
**ファイル:** `src/main/java/com/example/rpgplugin/gui/menu/SkillMenu.java`

**実装した習得条件チェック:**
- スキルポイントが十分にあるかチェック
- 前提スキル（親スキル）を習得しているかチェック
- レベル要件・ステータス要件を満たしているかチェック（SkillTree.canAcquire経由）
- クラス要件を満たしているかチェック

### 2. スロットとスキルのマッピング実装
**追加フィールド:**
```java
private final Map<Integer, Skill> slotToSkillMap = new HashMap<>();
```

**更新メソッド:**
- `displaySkillNode()` - スキル表示時にスロットとスキルのマッピングを保存

### 3. handleSkillClickメソッド実装 (SkillMenu.java:359-377)
**処理内容:**
- スロットから該当するスキルを特定
- 習得済みの場合: `upgradeSkill()` を呼び出し
- 未習得の場合: `acquireSkill()` を呼び出し

### 4. acquireSkill/upgradeSkillメソッド追加 (SkillMenu.java:379-430)
**acquireSkill():**
- 習得条件を再チェック
- スキルポイントを消費
- SkillManager.acquireSkill() を呼び出して習得

**upgradeSkill():**
- 最大レベルチェック
- SkillManager.upgradeSkill() を呼び出して強化

### 5. メインメニューとの連携実装 (SkillMenu.java:323-366)
**戻るボタン動作:**
- StatMenu（ステータスメニュー）を開くように実装
- MainMenuクラスは存在しないため、StatMenuに戻る仕様とした

### 6. StatManagerのimport追加
- handleClick()でStatMenuを開くためimportを追加

## 関連ファイル
- `src/main/java/com/example/rpgplugin/gui/menu/SkillMenu.java` - メイン実装ファイル
- `src/main/java/com/example/rpgplugin/gui/menu/SkillMenuListener.java` - イベントリスナー
- `src/main/java/com/example/rpgplugin/gui/menu/StatMenu.java` - 戻る先のメニュー
- `src/main/java/com/example/rpgplugin/skill/SkillManager.java` - スキル習得・強化処理
- `src/main/java/com/example/rpgplugin/skill/SkillTree.java` - 習得要件チェック

## 解決したTODO
- ✅ `SkillMenu.java:313` - 習得条件チェックの実装
- ✅ `SkillMenu.java:333` - メインメニューとの連携
- ✅ `SkillMenu.java:361` - スロットとスキルのマッピング実装

## 受入条件達成状況
- ✅ 習得条件が正しくチェックされる
- ✅ 条件未達スキルが赤色表示（既存実装）
- ✅ クリックでスキル習得が可能
- ✅ スキルポイントが正しく消費される
- ✅ ステータスメニューに戻れる

## コンパイル結果
ビルド成功（エラーなし）

## 次のステップ（推奨）
- スキルツリーへのノード追加処理の実装（RPGCommand.java:300-301のTODO）
- スキル発動処理の実装（RPGCommand.java:355-356のTODO）
- クラス選択GUIとの連携強化
