# スキル振り分けGUI実装レポート

## 作成日
2026-01-10

## 作業ブランチ
vk/1811-gui-gui

## 概要
スキルポイントを振るGUIを自動構成するシステムを実装した

## 実装ファイル

### 新規ファイル
1. **src/main/java/com/example/rpgplugin/gui/SkillTreeGUI.java**
   - スキルツリーGUIのメインクラス
   - YAMLから自動的にGUIを構成
   - 親子関係の階層表示
   - 習得可能/不可能な状態を視覚的に表示
   - 左クリック: スキル習得/レベルアップ（SP消費）
   - 右クリック: スキル解除/レベルダウン（SP返還）

2. **src/main/java/com/example/rpgplugin/gui/SkillTreeGUIListener.java**
   - InventoryClickEventでアイテム持ち出しを防止
   - スキルスロットのクリック処理

### 修正ファイル
1. **src/main/java/com/example/rpgplugin/skill/Skill.java**
   - `SkillTreeConfig`内部クラスに`icon`フィールドを追加
   - コンストラクタにiconパラメータを追加

2. **src/main/java/com/example/rpgplugin/skill/SkillManager.java**
   - `PlayerSkillData`クラスに`removeSkill()`メソッドを追加

3. **src/main/java/com/example/rpgplugin/skill/SkillLoader.java**
   - `parseSkillTreeConfig()`で`skill_tree.icon`を読み込むように修正
   - `skill_tree.icon`が優先されるようにiconMaterial設定を変更

4. **src/main/java/com/example/rpgplugin/RPGCommand.java**
   - `handleSkillCommand()`でGUIを開くように修正
   - `SkillTreeGUI`のインポートを追加

5. **src/main/java/com/example/rpgplugin/RPGPlugin.java**
   - `registerListeners()`で`SkillTreeGUIListener`を登録

### YAML設定追加
- **src/main/resources/skills/active/fireball.yml**: `skill_tree`設定追加
- **src/main/resources/skills/active/power_strike.yml**: `skill_tree`設定追加
- **src/main/resources/skills/passive/critical_mastery.yml**: `skill_tree`設定追加（parent: power_strike）

## GUI構成
- サイズ: 6行（54スロット）
- スキル表示: 2行目から5行目（スロット9-44）
- 上枠: ガラスパン
- 下枠: ガラスパン
- 中央上: スキルポイント表示（スロット4）
- 中央下: 閉じるボタン（スロット49）

## YAML設定項目
```yaml
skill_tree:
  parent: none  # 前提スキルID（noneで前提なし）
  cost: 1       # スキルポイント消費量
  icon: BLAZE_ROD  # GUIでの表示アイテム（Material名）
```

## 操作方法
- コマンド: `/rpg skill` でGUIを開く
- 左クリック: スキル習得/レベルアップ
- 右クリック: スキル解除/レベルダウン

## 表示内容
- スキル名（色付き）
- 現在レベル/最大レベル
- 習得コスト（SP）
- 説明文
- 前提スキル（習得済みか未習得かを表示）
- 習得可能状態

## 使用方法の例
```yaml
# skills/fireball.yml
skill_tree:
  parent: none  # 前提なし
  cost: 1       # 1SP消費
  icon: BLAZE_ROD

# skills/meteor.yml（上級スキル）
skill_tree:
  parent: fireball  # fireball習得後に解放
  cost: 2           # 2SP消費
  icon: FIRE_CHARGE
```

## 残タスク/今後の拡張
- PlayerDataにスキルポイントフィールドを正式に追加（現状はレベルベースの計算）
- スキルポイント獲得システムの実装
- GUIのドラッグ&ドロップでスキル配置カスタマイズ
- スキルツリーの可視化（Mermaid出力など）
