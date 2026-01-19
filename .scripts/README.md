# 作業完了後ワークフロー

## 概要

作業完了後に「ビルド → コミット → プッシュ → PR作成」を一括実行するワークフローです。

## セットアップ

### 1. Gitエイリアスの登録（推奨）

```bash
source ./.scripts/aliases.sh
```

### 2. 依存ツールの確認

- **Maven**: ビルドに使用
- **gh CLI**: PR作成に使用
- **Git**: バージョン管理

## 使用方法

### 基本的な使い方

```bash
# スクリプト直接実行
./.scripts/workflow-complete.sh "機能追加" "新しいXXX機能を実装"

# Gitエイリアス使用（セットアップ後）
git done "機能追加" "新しいXXX機能を実装"
```

### オプション

| オプション | 説明 |
|-----------|------|
| `--dry-run` | 実行せずにシミュレーションのみ |
| `--no-build` | ビルドをスキップ |
| `--no-pr` | PR作成をスキップ（コミットのみ） |

### 使用例

```bash
# 通常実行
git done "バグ修正" "NullPointerExceptionの修正"

# ビルドスキップ（コードのみ変更時）
git done --no-build "ドキュメント更新" "READMEの修正"

# PR作成スキップ（コミットまで）
git done --no-pr "WIP" "作業中"

# ドライラン（確認用）
git done --dry-run "テスト" "変更内容の確認"
```

## ワークフロー内容

1. **事前チェック**
   - Gitリポジトリ確認
   - 変更有無のチェック
   - gh CLI認証確認
   - Maven確認

2. **Mavenビルド**
   - `mvn clean package` 実行
   - 成果物を `target/` に生成

3. **コミット作成**
   - 変更ファイルをステージング
   - コミットメッセージでコミット

4. **プッシュ**
   - 現在のブランチをプッシュ

5. **プルリクエスト作成**
   - ベースブランチに対してPR作成
   - 既存PRがある場合は更新

## GitHub Actions連動

PRを作成すると自動で以下が実行されます：

- Mavenビルド検証
- テスト実行
- コード品質チェック（Checkstyle/SpotBugs）

設定ファイル: `.github/workflows/build-validate.yml`

## 自動実行設定

`.claude/settings.json` に `postTask` フックが設定されているため、Claude Codeでの作業完了時に自動実行されます。

```json
{
  "hooks": {
    "postTask": {
      "command": "bash ./.scripts/workflow-complete.sh ...",
      "enabled": true
    }
  }
}
```

## トラブルシューティング

### gh CLIの認証エラー

```bash
gh auth login
```

### ビルド失敗時の対処

```bash
# ビルドをスキップしてPR作成
git done --no-build "緊急対応" "hotfix"
```

### 既存PRの確認

```bash
gh pr list --head $(git branch --show-current)
```

## 成果物の場所

ビルド成功後のJARファイル:
```
target/mc-rpg-{version}.jar
```
