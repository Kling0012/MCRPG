# Claude Flow Hook エラー修正記録

## 日付
2026-01-15

## 問題
Claude Codeで以下のhookエラーが発生していました：
- `SessionStart:resume hook error`
- `UserPromptSubmit hook error`

## 原因
1. `daemon start --quiet`: `--quiet` オプションがグローバルオプションとして正しく機能していなかった
2. `route -t`: `-t` ショートオプションが存在せず、`--task` が必要
3. timeout値が短すぎた（5000ms）
4. 出力が適切にリダイレクトされていなかった

## 修正内容

### 1. npxコマンドに `-q` オプションを追加
```bash
# 修正前
npx @claude-flow/cli@latest daemon start --quiet 2>/dev/null

# 修正後
npx -q @claude-flow/cli@latest daemon start >/dev/null 2>&1
```

### 2. timeout値の調整
- PreToolUse/PostToolUse: 5000ms → 8000ms
- UserPromptSubmit: 5000ms → 10000ms
- SessionStart: 5000ms/10000ms → 15000ms
- Notification: 3000ms → 5000ms

### 3. routeコマンドのオプション修正
```bash
# 修正前
npx @claude-flow/cli@latest hooks route -t "$PROMPT"

# 修正後
npx -q @claude-flow/cli@latest hooks route --task "$PROMPT"
```

### 4. 出力の完全なリダイレクト
```bash
# 修正前
2>/dev/null

# 修正後
>/dev/null 2>&1
```

## 修正したhook一覧
- PreToolUse (Write/Edit/MultiEdit, Bash, Task)
- PostToolUse (Write/Edit/MultiEdit, Bash, Task)
- UserPromptSubmit
- SessionStart
- Notification
- statusLine

## 検証済みコマンド
```bash
# すべてOK
npx -q @claude-flow/cli@latest daemon start
npx -q @claude-flow/cli@latest hooks session-restore
npx -q @claude-flow/cli@latest hooks route --task "test"
```

## 作業ブランチ
main
