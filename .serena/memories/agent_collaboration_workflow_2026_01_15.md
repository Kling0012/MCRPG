# エージェントとの対話的共同作業ワークフロー 2026-01-15

## 解決した問題
- Codexのlandlock権限エラー → `-s danger-full-access` で解決
- 対話的I/Oができない → tmux + `--prompt-interactive` で解決

## 各エージェントの使用方法

### Codex（コーディング担当）
```bash
# 非対話モード（推奨）
codex exec -s danger-full-access --cd /workspaces/java/Minecraft-Java-Plaugin-forRPG "タスク"

# 対話モード
codex -s danger-full-access --cd /workspaces/java/Minecraft-Java-Plaugin-forRPG
```

### Gemini（情報収集担当）
```bash
# 非対話モード
gemini "タスク"

# 対話モード
tmux new-session -s gemini "gemini --prompt-interactive 'プロンプト'"
tmux attach -t gemini
```

### iFlow（プロセス担当）
```bash
# 非対話モード
iflow -p "タスク"

# 対話モード
tmux new-session -s iflow "iflow --prompt-interactive 'プロンプト'"
tmux attach -t iflow
```

## ラッパースクリプト
- `.sprint/bin/rpg-codex` - Codexラッパー
- `.sprint/bin/rpg-gemini` - Geminiラッパー（tmux使用）
- `.sprint/bin/rpg-iflow` - iFlowラッパー（tmux使用）
- `.sprint/bin/team-chat` - 統合チームチャット

## 認証状態
- Codex: 認証済み
- Gemini: 認証済み
- iFlow: ~/.iflow/settings.json にapiKey設定済み

## 制限事項
- Codex: sandboxモードが必要（danger-full-access推奨）
- Gemini: IDE拡張接続エラー警告あり（動作に支障なし）
- 対話モード: tmux経由でのみ可能
