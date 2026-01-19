#!/bin/bash
##############################################################################
# 作業完了後ワークフロースクリプト
#
# 機能: ビルド → 成果物生成 → コミット → PR作成 を一連実行
#
# 使用方法:
#   ./.scripts/workflow-complete.sh [タイトル] [説明]
#   例: ./scripts/workflow-complete.sh "機能追加" "新しいXXX機能を実装"
#
# オプション:
#   --dry-run    : 実行せずにシミュレーションのみ
#   --no-build   : ビルドをスキップ
#   --no-pr      : PR作成をスキップ（コミットのみ）
##############################################################################

set -e

# 色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# プロジェクトルートディレクトリ
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# デフォルト値
DRY_RUN=false
SKIP_BUILD=false
SKIP_PR=false
TITLE="${1:-作業完了}"
DESCRIPTION="${2:-作業完了による変更}"
BASE_BRANCH="main"
CURRENT_BRANCH=$(git branch --show-current)

# オプション解析
while [[ $# -gt 0 ]]; do
    case $1 in
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --no-build)
            SKIP_BUILD=true
            shift
            ;;
        --no-pr)
            SKIP_PR=true
            shift
            ;;
        *)
            if [[ -z "$TITLE_PARSED" ]]; then
                TITLE="$1"
                TITLE_PARSED=true
            elif [[ -z "$DESCRIPTION_PARSED" ]]; then
                DESCRIPTION="$1"
                DESCRIPTION_PARSED=true
            fi
            shift
            ;;
    esac
done

##############################################################################
# ログ関数
##############################################################################
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "\n${BLUE}=== $1 ===${NC}"
}

##############################################################################
# 事前チェック
##############################################################################
pre_check() {
    log_step "事前チェック"

    # Gitリポジトリチェック
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        log_error "Gitリポジトリではありません"
        exit 1
    fi

    # 変更があるかチェック
    if git diff --quiet && git diff --cached --quiet; then
        log_warn "コミット可能な変更がありません"
        read -p "続行しますか? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 0
        fi
    fi

    # gh CLIチェック
    if ! command -v gh &> /dev/null; then
        log_error "gh CLIがインストールされていません"
        log_info "インストール: https://cli.github.com/"
        exit 1
    fi

    # 認証チェック
    if ! gh auth status &> /dev/null; then
        log_error "gh CLIの認証が必要です"
        log_info "実行: gh auth login"
        exit 1
    fi

    # Mavenチェック
    if ! command -v mvn &> /dev/null; then
        log_error "Mavenがインストールされていません"
        exit 1
    fi

    # ベースブランチチェック
    if ! git rev-parse --verify "$BASE_BRANCH" > /dev/null 2>&1; then
        log_warn "ベースブランチ '$BASE_BRANCH' が存在しません"
        BASE_BRANCH=$(git remote show origin | grep "HEAD branch" | cut -d: -f2 | xargs)
        log_info "ベースブランチを '$BASE_BRANCH' に設定"
    fi

    log_success "事前チェック完了"
}

##############################################################################
# ビルド実行
##############################################################################
do_build() {
    if [[ "$SKIP_BUILD" == true ]]; then
        log_warn "ビルドをスキップします"
        return 0
    fi

    log_step "Mavenビルド実行"

    if [[ "$DRY_RUN" == true ]]; then
        log_info "[DRY-RUN] mvn clean package -DskipTests"
        return 0
    fi

    # ビルド実行
    if mvn clean package -DskipTests; then
        log_success "ビルド成功"

        # 成果物のパス
        ARTIFACT="$PROJECT_ROOT/target/$(ls target/*.jar 2>/dev/null | grep -v 'javadoc\|sources' | head -1 | xargs basename)"
        if [[ -f "target/$ARTIFACT" ]]; then
            log_info "成果物: target/$ARTIFACT"
            ls -lh "target/$ARTIFACT"
        fi
    else
        log_error "ビルド失敗"
        read -p "続行しますか? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
}

##############################################################################
# コミット作成
##############################################################################
do_commit() {
    log_step "コミット作成"

    # ステージされていない変更を表示
    log_info "変更内容:"
    git status --short

    if [[ "$DRY_RUN" == true ]]; then
        log_info "[DRY-RUN] git add . && git commit"
        return 0
    fi

    # 変更をステージング
    git add .

    # コミットメッセージ生成
    COMMIT_MSG="$TITLE

$DESCRIPTION

---
Co-authored-by: Claude Code <claude@anthropic.com>"
    # コミット実行
    if git commit -m "$COMMIT_MSG"; then
        log_success "コミット作成成功"
        git log -1 --oneline
    else
        log_warn "コミット作成失敗（変更がありません）"
    fi
}

##############################################################################
# プッシュ実行
##############################################################################
do_push() {
    log_step "プッシュ実行"

    if [[ "$DRY_RUN" == true ]]; then
        log_info "[DRY-RUN] git push -u origin $CURRENT_BRANCH"
        return 0
    fi

    # リモートブランチが存在するかチェック
    if git rev-parse --verify "origin/$CURRENT_BRANCH" > /dev/null 2>&1; then
        log_info "既存のブランチにプッシュ"
        git push
    else
        log_info "新しいブランチをプッシュ"
        git push -u origin "$CURRENT_BRANCH"
    fi

    log_success "プッシュ完了"
}

##############################################################################
# PR作成
##############################################################################
do_pr() {
    if [[ "$SKIP_PR" == true ]]; then
        log_warn "PR作成をスキップします"
        return 0
    fi

    log_step "プルリクエスト作成"

    if [[ "$DRY_RUN" == true ]]; then
        log_info "[DRY-RUN] gh pr create --base $BASE_BRANCH --title '$TITLE' --body '$DESCRIPTION'"
        return 0
    fi

    # 既存PRチェック
    EXISTING_PR=$(gh pr list --head "$CURRENT_BRANCH" --json number --jq '.[0].number' 2>/dev/null || echo "")

    if [[ -n "$EXISTING_PR" ]]; then
        log_warn "PR #$EXISTING_PR が既に存在します"
        read -p "既存PRを更新しますか? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            gh pr edit "$EXISTING_PR" --title "$TITLE" --body "$DESCRIPTION"
            log_success "PR #$EXISTING_PR を更新しました"
        fi
        return 0
    fi

    # PR作成
    PR_URL=$(gh pr create \
        --base "$BASE_BRANCH" \
        --title "$TITLE" \
        --body "$DESCRIPTION" \
        --label "auto-generated" 2>&1)

    if [[ $? -eq 0 ]]; then
        log_success "PR作成完了"
        log_info "$PR_URL"
    else
        log_error "PR作成失敗: $PR_URL"
        return 1
    fi
}

##############################################################################
# メイン処理
##############################################################################
main() {
    log_info "🚀 作業完了後ワークフロー開始"
    log_info "プロジェクト: $PROJECT_ROOT"
    log_info "現在のブランチ: $CURRENT_BRANCH"
    log_info "タイトル: $TITLE"

    pre_check
    do_build
    do_commit
    do_push
    do_pr

    log_success "✅ ワークフロー完了"
}

# 実行
main "$@"
