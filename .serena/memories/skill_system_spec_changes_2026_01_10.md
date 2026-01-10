# スキルシステム仕様変更（2026-01-10）

## 変更概要
パッシブスキル概念の削除と、レベルアップ時のスキルポイント付与を実施した。

## 変更内容

### 1. SkillType.java
- `ACTIVE`、`PASSIVE`の2種類を廃止し、`NORMAL`のみに統一
- `fromId()`メソッドで`active`/`passive`のYAML値を`NORMAL`にマッピング（後方互換性）
- ファイル: `src/main/java/com/example/rpgplugin/skill/SkillType.java`

### 2. Skill.java
- `isActive()`メソッドを削除
- `isPassive()`メソッドを削除
- ファイル: `src/main/java/com/example/rpgplugin/skill/Skill.java`

### 3. 使用箇所の修正
- `RPGCommand.java`: `isActive()`チェックを削除
- `SkillManager.java`: `isActive()`チェックを削除
- `PassiveSkillExecutor.java`: `isPassive()`チェックを削除
- `SkillLoaderTest.java`: テストコードから`isActive()`/`isPassive()`呼び出しを削除

### 4. VanillaExpHandler.java
- レベルアップ時に1スキルポイントを付与する処理を追加
- レベルアップメッセージにスキルポイント表示を追加
- ファイル: `src/main/java/com/example/rpgplugin/player/VanillaExpHandler.java`

## YAML互換性
既存のYAMLファイルで`type: active`または`type: passive`が指定されている場合、`SkillType.NORMAL`として扱われる。

## レベルアップ処理の変更
- 自動配分: 各ステータス+2
- 手動配分ポイント: +3
- **スキルポイント: +1**（新規追加）
