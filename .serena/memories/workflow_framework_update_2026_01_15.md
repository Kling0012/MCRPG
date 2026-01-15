# 汎用共同作業フレームワーク アップデート記録

## 日付
2026-01-15

## アップデート内容

### 変更前
- 「チームレビュー共同作業ワークフロー」: レビュー専用の記述
- 特定の3エージェント（Gemini/Codex/iFlow）に固定
- チームレビューという単一用途

### 変更後
- 「汎用共同作業フレームワーク（Multi-Agent Collaboration）」
- 8種類のタスクタイプに対応
- タスクタイプ別エージェント構成表
- 複雑度によるエージェント数調整
- 3つの実装テンプレート（機能開発/バグ修正/リファクタ）

## 追加されたタスクタイプ

| タスクタイプ | エージェント構成 |
|------------|----------------|
| レビュー | researcher + system-architect + planner |
| 機能開発 | system-architect + coder + tester |
| バグ修正 | researcher + coder + reviewer |
| リファクタ | system-architect + coder + reviewer |
| パフォーマンス | performance-engineer + coder |
| セキュリティ | security-architect + security-auditor |
| ドキュメント | researcher + api-docs |
| デバッグ | debugger + coder |

## エージェント定義

| 名称 | subagent_type | 役割 |
|------|---------------|------|
| Gemini | researcher | 情報収集・分析 |
| Codex | system-architect | 設計・アーキテクチャ |
| iFlow | planner | プロセス・ワークフロー |
| Builder | coder | 実装 |
| QA | tester | テスト |
| Critic | reviewer | レビュー |
| Optimus | performance-engineer | パフォーマンス |
| Shield | security-architect | セキュリティ |
| Doc | api-docs | ドキュメント |
| Sherlock | debugger | デバッグ |

## ファイル
- `/workspaces/java/Minecraft-Java-Plaugin-forRPG/CLAUDE.md`
- セクション: `## 🤝 汎用共同作業フレームワーク（Multi-Agent Collaboration）`
- 行範囲: 668-916
