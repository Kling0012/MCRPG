# YAML Editor Integration Feature - 2026/01/14

## Created File
`tools/yaml-editor/integrated-editor.html` (1,837 lines)

## 機能概要

### 3タブインターフェース
1. **スキルタブ (⚡)**: スキル情報、利用可能クラス、スキルツリー編集
2. **クラスタブ (🛡️)**: クラス情報、ステータス成長、要件編集
3. **整合性タブ (✓)**: 双方向参照の整合性検証

### スキル管理機能
- 基本情報: id, name, displayName, type, maxLevel
- availableClassesマルチセレクト（クラスへのリンク）
- スキルツリー: 親スキル、解放要件（level/stat）、コスト
- 必須スキルリスト

### クラス管理機能
- 基本情報: id, name, displayName, rank, maxLevel
- ステータス成長設定（str, vit, dex, int, wis）
- 要件ビルダー（level, stat, item, quest）
- availableSkillsマルチセレクト（スキルへのリンク）

### 整合性チェック機能
```javascript
// ConsistencyValidator.javaパターンに基づく実装
// Class.availableSkills ↔ Skill.availableClasses

チェック項目:
1. クラスが参照するスキルが存在するか
2. スキルが参照するクラスが存在するか
3. 双方向整合性: クラスがスキルを持つ ↔ スキルがクラスを持つ
```

### データ永続化
- LocalStorageによる自動保存
- Import/Export JSON機能
- YAMLプレビュー（スキル・クラス両方）

## 使用方法
ブラウザで `tools/yaml-editor/integrated-editor.html` を開いて使用

## 連携仕様
- Skill.availableClasses: スキルが利用可能なクラスIDのリスト
- Class.availableSkills: クラスが利用可能なスキルIDのリスト
- 整合性スコア: エラー0警告0で100%、エラーありで25%、警告のみで75%
