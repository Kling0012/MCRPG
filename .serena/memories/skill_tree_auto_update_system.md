# スキルツリー自動更新システム設計

## 更新日時
2026-01-08

## 概要
新しいクラスやスキルを追加した際にGUIを自動更新できる仕組みを設計・実装した。

## アーキテクチャ

### コンポーネント構成図
```
┌─────────────────────────────────────────────────────────────────┐
│                         SkillManager                             │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │              SkillTreeRegistry (新規)                     │  │
│  │  - クラスごとのスキルツリーキャッシュ                       │  │
│  │  - スキル登録時の自動ツリー更新                             │  │
│  │  - GUI更新リスナー管理                                      │  │
│  └───────────────────────────────────────────────────────────┘  │
│                            ↓                                    │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │              SkillTreeBuilder (新規)                      │  │
│  │  - スキルリストからのツリー構築                            │  │
│  │  - 親子関係の自動解決                                       │  │
│  │  - 循環参照検出                                             │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                            ↓
        ┌──────────────────────────────────────────┐
        │              SkillMenu                    │
        │  - 自動更新リスナー登録                    │
        │  - ツリー更新時のGUIリフレッシュ            │
        └──────────────────────────────────────────┘
```

## 新規ファイル

### 1. SkillTreeRegistry.java
**パッケージ:** `com.example.rpgplugin.skill`

**機能:**
- クラスIDごとのスキルツリーキャッシュ管理
- スキル登録時の自動ツリー更新
- GUI更新リスナー通知機能（Observerパターン）
- スキルIDから所属クラスのマッピング

**主要メソッド:**
- `getTree(String classId)`: スキルツリー取得（キャッシュヒット時は即座に返却）
- `registerSkill(Skill skill)`: スキル登録とツリー自動更新
- `invalidateTree(String classId)`: 特定クラスのキャッシュ無効化
- `addListener(TreeUpdateListener)`: GUI更新リスナー登録

**インターフェース:**
```java
@FunctionalInterface
public interface TreeUpdateListener {
    void onTreeUpdated(String classId);
}
```

### 2. SkillTreeBuilder.java
**パッケージ:** `com.example.rpgplugin.skill`

**機能:**
- スキルリストからのツリー構築（Builderパターン）
- 親子関係の自動解決（parentフィールドベース）
- 循環参照検出
- バリデーション機能

**主要メソッド:**
- `addSkill(Skill skill)`: スキル追加（メソッドチェーン対応）
- `build()`: スキルツリー構築
- `hasCycle(...)`: 循環参照検出
- `forClass(String classId, Map<String, Skill> allSkills, Logger logger)`: クラスフィルタリング済みビルダー生成

## 修正ファイル

### 1. SkillManager.java
**変更内容:**
- `treeRegistry`フィールド追加
- `registerSkill()`メソッドでRegistryへの登録を追加
- `getTreeRegistry()`, `getSkillTree(String classId)`メソッド追加

### 2. SkillMenu.java
**変更内容:**
- 新コンストラクタ追加: `SkillMenu(RPGPlugin plugin, Player player, boolean autoRefresh)`
- `SkillMenu(RPGPlugin plugin, Player player)`: 自動更新有効版
- `onTreeUpdated(String classId)`: ツリー更新時のコールバック
- Registryから自動的にスキルツリーを取得

### 3. RPGCommand.java
**変更内容:**
- `handleSkillCommand()`メソッドを簡素化
- 空のツリー作成処理を削除
- SkillMenuの新コンストラクタを使用

## 自動更新フロー

```
1. 新しいスキルYAMLファイルを追加
   ↓
2. SkillLoader.loadAllSkills() でスキル読み込み
   ↓
3. SkillManager.registerSkill() でスキル登録
   ↓
4. SkillTreeRegistry.registerSkill() でRegistryにも登録
   ↓
5. 該当クラスのツリーキャッシュを無効化 (invalidateTree)
   ↓
6. 登録済みリスナーに通知 (onTreeUpdated)
   ↓
7. SkillMenu.onTreeUpdated() でGUIをリフレッシュ
   ↓
8. プレイヤーがGUIを開いている場合は即座に更新
```

## 使用方法

### スキル追加時
```yaml
# skills/fireball.yml
id: fireball
name: "ファイアボール"
display_name: "&cファイアボール"
type: active
max_level: 10

skill_tree:
  parent: none
  cost: 1

available_classes: []  # 空リスト = 全クラスで利用可能
```

### プログラムからスキル追加
```java
Skill skill = new Skill(...);
skillManager.registerSkill(skill);
// 自動的にツリーが更新され、GUIもリフレッシュされる
```

## 利点

1. **YAML追加のみでGUI更新**: スキルファイルを追加するだけで自動的にGUIに反映
2. **キャッシュによる高速化**: 同じクラスの2回目以降はキャッシュを使用
3. **通知機能**: 開いているGUIを持つプレイヤー全員に更新を通知
4. **循環参照検出**: 無限ループを防止
5. **クラスフィルタリング**: クラス限定スキルに対応

## バリデーション

- 循環参照の検出
- 親スキル不在の警告
- 空ツリーの検出

## 今後の拡張

- ホットリロード対応（/rpg reload でスキルを再読み込み）
- GUIのドラッグ&ドロップでスキル配置をカスタマイズ
- スキルツリーの可視化（Mermaid出力など）
