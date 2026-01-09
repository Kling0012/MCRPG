# 【レビュー6】RPG Class Requirements + GUI Part1 - Nullチェック/例外処理レビュー

**レビュー日**: 2026-01-09
**ブランチ**: vk/fa4d-6-rpg-class-requ
**対象ファイル**: 11ファイル

## サマリー

| カテゴリ | 問題なし | 改善推奨 | 要修正 | 計 |
|---------|----------|----------|--------|-----|
| Nullチェック | 7 | 4 | 0 | 11 |
| 例外処理 | 8 | 3 | 0 | 11 |

**評価**: ✅ 良好 - 重大な問題なし

## 各ファイルの評価

### ✅ 良好な実装

1. **ItemRequirement.java**
   - Player, ItemStack, ItemMeta, displayNameの全てにNullチェックあり
   - 改善: ItemMetaを変数に格納して二重呼出し回避

2. **StatRequirement.java**
   - Player, RPGPlayerのNullチェック完備
   - 値検証（Math.max）で負値防止
   - parse()での例外処理適切

3. **LevelRequirement.java**
   - Player Nullチェックあり
   - 値検証（Math.max(1, ...)）で1以上保証

4. **SkillMenuListener.java**
   - instanceof, Menu Null, Inventory Nullチェック完備

5. **StatMenuListener.java**
   - InventoryHolder, instanceof, スロット範囲チェックあり

6. **ClassMenu.java**
   - ItemMeta Nullチェック徹底
   - Optionalの適切な使用
   - 空リストチェックあり

7. **ClassMenuListener.java**
   - ItemStack, ItemMeta Nullチェックあり
   - Optional使用適切

### ⚠️ 改善推奨

1. **QuestRequirement.java**
   - 56行: `externalPlugin.equalsIgnoreCase()` → `"BetonQuest".equalsIgnoreCase(externalPlugin)`
   - 81-82行: 汎用的なcatchではなく具体的な例外型 + ログ出力

2. **SkillMenu.java**
   - 72行: コンストラクタでplayerのNullチェック追加
   - 76-78行: rpgPlayer/skillTreeのNullチェック追加
   - 182行: PlayerSkillDataのNullチェック追加

3. **StatMenu.java**
   - 61行: rpgPlayerのNullチェック追加

## 優れている点

- Player引数に対するNullチェックが一貫して実装
- Optionalの適切な使用
- ItemMetaの安全な使用（hasItemMeta()チェック後）

## 優先度別改善タスク

| 優先度 | ファイル | 行 | 内容 |
|--------|----------|-----|------|
| 🔴 高 | QuestRequirement.java | 56 | equalsIgnoreCaseの安全な呼出し |
| 🟡 中 | SkillMenu.java | 72-78 | コンストラクタNullチェック |
| 🟡 中 | StatMenu.java | 61 | rpgPlayer Nullチェック |
| 🟢 低 | QuestRequirement.java | 81-82 | 例外ログ出力 |
