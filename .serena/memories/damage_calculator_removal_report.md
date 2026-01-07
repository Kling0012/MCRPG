# DamageCalculator 削除レポート

## 実施日
2026-01-07

## 削除理由
1. **未使用コード**: 参照数 0（外部からのインポートなし）
2. **機能重複**: DamageModifier とダメージ計算機能が重複
3. **保守性向上**: 未使用コードの削除はコードベースの簡素化に貢献
4. **YAGNI原則**: 現状不要な機能（命中判定、ランダム倍率）を含んでいた

## 削除対象
- **ファイル**: `src/main/java/com/example/rpgplugin/stats/calculator/DamageCalculator.java`
- **行数**: 422行
- **機能**:
  - `calculatePhysicalDamage()` - 物理ダメージ計算
  - `calculateMagicDamage()` - 魔法ダメージ計算
  - `isCriticalHit()` - クリティカル判定
  - `calculateCriticalDamage()` - クリティカルダメージ
  - `isHit()` - 命中判定（DamageModifier にない機能）
  - `getRandomDamageMultiplier()` - ランダム倍率（DamageModifier にない機能）
  - `calculateFullDamage()` - 統合ダメージ計算
  - `DamageResult` - ダメージ結果クラス

## 代替クラス
**DamageModifier.java** (`src/main/java/com/example/rpgplugin/damage/DamageModifier.java`)
- 現在稼働中のダメージ計算クラス
- Phase10-2 で修正・テスト済み
- 実装済み機能:
  - 物理ダメージ計算
  - 魔法ダメージ計算
  - 防御カット計算
  - クリティカル判定/倍率
  - ダメージ丸め処理

## 影響範囲確認
```bash
# 参照検索結果
rg "DamageCalculator" --type java src/
→ 結果: 自分のファイル内のみ（外部参照なし）
```

## 検証結果
### コンパイル
```bash
mvn clean compile
→ BUILD SUCCESS
```

### テスト
```bash
mvn test
→ Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
→ BUILD SUCCESS
```

## 機能比較

| 機能 | DamageCalculator | DamageModifier | 備考 |
|------|------------------|----------------|------|
| 物理ダメージ計算 | ✓ | ✓ | DamageModifier を使用 |
| 魔法ダメージ計算 | ✓ | ✓ | DamageModifier を使用 |
| クリティカル判定 | ✓ | ✓ | DamageModifier を使用 |
| 防御カット計算 | ✗ | ✓ | DamageModifier のみ実装 |
| 命中判定 | ✓ | ✗ | 未使用のため削除 |
| ランダム倍率 | ✓ | ✗ | 未使用のため削除 |
| 統合メソッド | ✓ | ✗ | 未使用のため削除 |

## 将来の拡張
以下の機能が必要になった場合、DamageModifier に追加可能:
1. 命中判定システム
2. ランダムダメージ変動システム
3. 統合ダメージ計算メソッド

## 原則適用
- **KISS**: 1つのダメージ計算クラスに集約
- **YAGNI**: 未使用機能を削除
- **DRY**: 機能重複を排除

## 結論
DamageCalculator の削除は安全に実施でき、システムに悪影響を与えていない。