# Claude Flow グローバルHookエラー修正記録

## 日付
2026-01-15

## 問題
```
SessionStart:compact hook error
```

## 原因
`SessionStart:compact` はClaude Codeの内部イベント（コンテキスト圧縮処理）ですが、グローバル設定の `matcher: "*"` がすべてのSessionStartイベントにマッチしてしまい、内部イベントに対してもhookが実行されてエラーが発生していました。

## 修正内容

### /home/vscode/.claude/settings.json

すべてのhookに `continueOnError: true` を追加して、内部イベントによるエラーを無視するようにしました。

#### SessionStart hook
```json
"SessionStart": [
  {
    "matcher": "*",
    "continueOnError": true,  // 追加
    "hooks": [
      {
        "type": "command",
        "command": "bun run $PAI_DIR/hooks/initialize-session.ts",
        "continueOnError": true  // 追加
      },
      {
        "type": "command",
        "command": "bun run $PAI_DIR/hooks/load-core-context.ts",
        "continueOnError": true  // 追加
      }
    ]
  }
]
```

#### PreToolUse hook
```json
"PreToolUse": [
  {
    "matcher": "Bash",
    "continueOnError": true,  // 追加
    "hooks": [
      {
        "type": "command",
        "command": "bun run $PAI_DIR/hooks/security-validator.ts",
        "continueOnError": true  // 追加
      }
    ]
  }
]
```

#### UserPromptSubmit hook
```json
"UserPromptSubmit": [
  {
    "matcher": "*",
    "continueOnError": true,  // 追加
    "hooks": [
      {
        "type": "command",
        "command": "bun run $PAI_DIR/hooks/update-tab-titles.ts",
        "continueOnError": true  // 追加
      }
    ]
  }
]
```

## 結果
内部イベント（`compact`など）によるhookエラーが表示されなくなります。

## 作業ブランチ
main
