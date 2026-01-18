# CLIワークフロー書き換え (2026-01-15)

## 変更概要

CLAUDE.mdのアジャイル型チーム共同作業フレームワークを、Task toolのサブエージェント起動から、Bash経由の非対話的CLI実行に書き換え。

## 変更箇所

### 1. フェーズ2: スプリント実行（Execution）
- `Task({ subagent_type: ... })` → `codex exec "..."` / `iflow -p "..."` / `gemini "..."`
- バックグラウンド実行（`&`）とログ出力（`.sprint/outputs/*.log`）を追加

### 2. フェーズ3: スプリントレビュー（Review）
- `TaskOutput({ task_id: ... })` → `cat .sprint/outputs/*.log`

### 3. 実装テンプレート1〜4
- テンプレート1（機能開発）: Bash並列実行パターンに変更
- テンプレート2（バグ修正）: Bash並列実行パターンに変更
- テンプレート3（コードレビュー）: Bash並列実行パターンに変更
- テンプレート4（フルスプリント）: Bash並列実行パターンに変更

### 4. SWARM EXECUTION RULES
- 8ルールをCLIベースの並列実行に完全書き換え
- ログファイル読み取りパターンを追加

### 5. その他セクション
- GOLDEN RULE: Task tool → CLI Agent Execution
- ABSOLUTE RULES: Task tool → CLI AGENTS
- Auto-Start Swarm Protocol: 完全書き換え
- Claude Code vs CLI Tools: CLIエージェント実行を追加

## 実行パターン

```bash
# 標準的な並列実行
mkdir -p .sprint/outputs
codex exec "タスク" > .sprint/outputs/codex.log 2>&1 &
iflow -p "タスク" > .sprint/outputs/iflow.log 2>&1 &
gemini "タスク" > .sprint/outputs/gemini.log 2>&1 &
```

## エージェント対応

| エージェント | CLIコマンド | 役割 |
|------------|------------|------|
| Codex | `codex exec "..."` | 実装・設計 |
| iFlow | `iflow -p "..."` | プロセス・振り分け |
| Gemini | `gemini "..."` | 情報収集・外部調査 |
