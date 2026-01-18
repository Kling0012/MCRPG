# カバレッジ分析レポート 2026-01-15

## 全体サマリー

| 項目 | 値 |
|------|-----|
| 総ソースファイル数 | 197 |
| 総パッケージ数 | 49 |
| テストなしパッケージ | 21 |
| 完全カバレッジパッケージ | 14 |
| 部分的カバレッジパッケージ | 14 |

## 優先パッケージ別カバレッジ状況

### playerパッケージ (92% カバレッジ)
- `com.example.rpgplugin.player`: 100% (5/5 クラス)
- `com.example.rpgplugin.player.config`: 100% (1/1 クラス)
- `com.example.rpgplugin.player.exp`: **0%** - ExpManagerにテストが必要

### damageパッケージ (100% カバレッジ)
- `com.example.rpgplugin.damage`: 100% (3/3 クラス)
- `com.example.rpgplugin.damage.handlers`: 100% (2/2 クラス)
- `com.example.rpgplugin.damage.config`: 100% (5/5 クラス)

### skillパッケージ
- `com.example.rpgplugin.skill`: 91.7% (11/12) - SkillCostType
- `com.example.rpgplugin.skill.config`: **0%** - SkillConfig
- `com.example.rpgplugin.skill.target`: 100% (7/7)
- `com.example.rpgplugin.skill.evaluator`: 100% (4/4)
- `com.example.rpgplugin.skill.component`: 50% (3/6) - ComponentSettings, ComponentType, EffectComponent
- `com.example.rpgplugin.skill.component.target`: **11.1%** (1/9) - 8クラス未テスト
- `com.example.rpgplugin.skill.component.trigger`: 28.6% (4/14) - 10クラス未テスト
- `com.example.rpgplugin.skill.component.cost`: 20% (1/5) - 4クラス未テスト
- `com.example.rpgplugin.skill.component.mechanic`: 29.4% (5/17) - 12クラス未テスト
- `com.example.rpgplugin.skill.component.condition`: 13.3% (2/15) - 13クラス未テスト
- `com.example.rpgplugin.skill.component.placement`: **0%** (0/3) - 3クラス未テスト

### commandパッケージ (0% カバレッジ)
- `com.example.rpgplugin.command`: **0%** - RPGAdminCommandにテストが必要

### guiパッケージ (0% カバレッジ)
- `com.example.rpgplugin.gui`: **0%** - SkillTreeGUI, SkillTreeGUIListenerにテストが必要

### rpgclassパッケージ (93% カバレッジ)
- `com.example.rpgplugin.rpgclass`: 100% (4/4)
- `com.example.rpgplugin.rpgclass.requirements`: 80% (4/5) - ClassRequirement
- `com.example.rpgplugin.rpgclass.growth`: 100% (1/1)

## テストなしパッケージ (21パッケージ)

### 重要度高
- `com.example.rpgplugin.command` - RPGAdminCommand
- `com.example.rpgplugin.gui` - SkillTreeGUI, SkillTreeGUIListener
- `com.example.rpgplugin.player.exp` - ExpManager
- `com.example.rpgplugin.skill.component.placement` - 3クラス
- `com.example.rpgplugin.skill.component.mechanic` - 12クラス
- `com.example.rpgplugin.skill.component.condition` - 13クラス
- `com.example.rpgplugin.skill.component.target` - 8クラス
- `com.example.rpgplugin.skill.component.trigger` - 10クラス
- `com.example.rpgplugin.skill.component.cost` - 4クラス

### 重要度中
- `com.example.rpgplugin.api` - API関連 (2クラス)
- `com.example.rpgplugin.api.skript` - Skript統合 (27クラス)
- `com.example.rpgplugin.core.system` - コアシステム (3クラス)

### 重要度低
- `com.example.rpgplugin.storage` - ストレージ関連 (7クラス)

## 優先テスト追加リスト

### CRITICAL
- `ExpManager` - プレイヤー経験値管理の中核

### HIGH
- `RPGAdminCommand` - 管理者コマンド
- `SkillTreeGUI`, `SkillTreeGUIListener` - GUI操作
- TargetComponent系 (8クラス) - スキルターゲット選択
- MechanicComponent系 (12クラス) - スキルエフェクト実行
- ConditionComponent系 (13クラス) - スキル発動条件
- TriggerComponent系 (10クラス) - スキルトリガー
- CostComponent系 (4クラス) - スキルコスト

### MEDIUM
- `SkillConfig` - スキル設定
- `ComponentSettings`, `EffectComponent` - コンポーネント基底
- API統合クラス
- Skript統合クラス

### LOW
- ストレージ/データベース関連クラス

## 次のアクション推奨

1. **まずCRITICAL/HIGHのクラスからテストを追加**
2. **skill.componentパッケージの統合テスト強化**
3. **GUI/Commandのユニットテスト追加**
4. **API/Skript統合のテスト追加（中長期）**
