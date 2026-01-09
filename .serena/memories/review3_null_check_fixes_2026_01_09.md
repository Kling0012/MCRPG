# レビュー3: Skillシステム Part2 + Target - Nullチェック/例外処理修正

## 実施日
2026-01-09

## 対象ブランチ
vk/06ff-3-skill-part2-ta

## 修正したファイル

### 1. PassiveSkillExecutor.java
- コンストラクタにnullチェックとIllegalArgumentExceptionを追加
- PassiveEffectコンストラクタにskillのnullチェックを追加
- applyPassive()のチェーン呼び出しを安全化（skill.getDamage()とgetStatMultiplier()を分離）
- updatePassives()でskill.getId()のnullチェックを追加

### 2. TargetSelector.java
- selectTargets()でcandidatesのnullチェックと空リストのデフォルト値を設定
- caster.getLocation()の2重呼び出しを1回に統合
- selectNearestHostile/selectNearestEntity/selectAreaTargetsのStreamフィルタにe != nullチェックを追加
- findNearest()にe.getLocation()のnullチェックを追加

### 3. TargetType.java
- fromId()をOptional<TargetType>を返すように変更
- レガシー互換のためfromIdOrNull()を@Deprecatedで追加

### 4. SkillTarget.java
- Optionalを返すゲッターメソッドを追加
  - getConeAsOptional()
  - getRectAsOptional()
  - getCircleAsOptional()
  - getSingleTargetAsOptional()

### 5. AreaShape.java
- fromId()をOptional<AreaShape>を返すように変更
- レガシー互換のためfromIdOrNull()を@Deprecatedで追加

### 6. ShapeCalculator.java
- isInRange()にentityLocのnullチェックを追加
- origin.getWorld()のnullチェックを追加

### 7. VariableContext.java
- 両方のコンストラクタにrpgPlayerのnullチェックを追加
- setCustomVariable(String, String)にnameとvalueのnullチェックを追加
- 例外型をIllegalArgumentExceptionに統一

## 注意事項
- EntityTypeFilter.javaはプロジェクト内に存在しなかったため対象外

## 設計原則の適用
- SOLID-S: 各クラスの単一責務を維持
- 防御的プログラミング: nullチェックを早期実行
- Optionalパターン: nullを返すメソッドをOptional化
- 例外設計: IllegalArgumentExceptionで明示的なエラー通知
