# テストカバレッジ分析結果 (2026-01-15)

## テスト実行結果

- **総テスト数**: 3,967
- **成功**: 3,955
- **失敗**: 0
- **スキップ**: 12
- **エラー**: 0

## 全体カバレッジ

| メトリクス | カバレッジ |
|-----------|----------|
| 命令 (Instructions) | 70% |
| 分岐 (Branches) | 64% |
| 行数 (Lines) | 70% |
| メソッド (Methods) | 76% |
| クラス (Classes) | 81% |

## パッケージ別カバレッジ (傑出したパッケージ)

| パッケージ | 命令 | 分岐 |
|-----------|------|------|
| skill.result | 100% | 100% |
| rpgclass.growth | 100% | 100% |
| skill.component.cooldown | 100% | n/a |
| storage.models | 100% | 97% |
| skill.component.cost | 99% | 100% |
| skill.component.target | 98% | 80% |
| player.config | 98% | 93% |
| skill.component.filter | 97% | 87% |
| stats | 96% | 87% |
| skill.evaluator | 96% | 88% |
| skill.target | 95% | 85% |
| skill.repository | 95% | 89% |
| damage.config | 94% | 78% |
| skill.component.placement | 94% | 81% |
| core.module | 94% | 88% |
| rpgclass.requirements | 94% | 100% |
| core.validation | 93% | 85% |
| skill.component.trigger | 93% | 84% |
| player | 92% | 80% |
| rpgclass | 91% | 76% |
| skill.component.condition | 91% | 77% |

## パッケージ別カバレッジ (改善が必要)

| パッケージ | 命令 | 分岐 | 優先度 |
|-----------|------|------|--------|
| skill | 90% | 78% | 低 |
| core.config | 89% | 77% | 低 |
| skill.executor | 90% | 80% | 低 |
| skill.component.mechanic | 79% | 65% | 中 |
| skill.component | 80% | 69% | 中 |
| damage.handlers | 82% | 97% | 低 |
| damage | 85% | 77% | 低 |

## パッケージ別カバレッジ (0% - テスト未実装)

| パッケージ | 命令 | 説明 |
|-----------|------|------|
| com.example.rpgplugin | 0% | メインクラス |
| storage.database | 0% | データベース関連 |
| storage.repository | 0% | リポジトリパターン |
| gui | 0% | GUIコンポーネント |
| api.skript.expressions | 0% | Skript式 |
| api | 0% | パブリックAPI |
| api.skript.effects | 0% | Skriptエフェクト |
| core.system | 0% | コアシステム |
| command | 0% | コマンド処理 |
| api.skript.conditions | 0% | Skript条件 |
| api.placeholder | 0% | プレースホルダーAPI |
| storage | 0% | ストレージ関連 |

## スキップされたテスト

8つのテストが `@Disabled` アノテーションで無効化されています (MechanicComponentTest.java):
- 設定の上書き方法を修正する必要あり (6件)
- MessageMechanicの挙動を確認する必要あり (1件)
- CommandMechanicのモック設定を修正する必要あり (1件)

## 今後の改善推奨事項

1. **高優先度**: 0%カバレッジのパッケージにテストを追加
2. **中優先度**: mechanic (79% → 90%以上) の改善
3. **低優先度**: スキップされたテストの修正と再有効化
