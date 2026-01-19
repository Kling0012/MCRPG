#!/bin/bash
##############################################################################
# 作業完了後ワークフロースクリプト（最適化版）
#
# 機能: 変更検出 → タイプ推測 → ビルド → コミット → プッシュ → PR作成
#
# 使用方法:
#   ./.scripts/workflow-complete.sh [タイトル] [説明] [オプション]
#
# オプション:
#   --dry-run         : シミュレーションのみ
#   --no-build        : ビルドスキップ
#   --no-pr           : PR作成スキップ
#   --non-interactive : 対話なしで自動実行（フック用）
#   --type <TYPE>     : コミットタイプ指定（feat/fix/refactor/docs/style/test/chore）
##############################################################################

set -e

# 色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# プロジェクトルート
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# デフォルト値
DRY_RUN=false
SKIP_BUILD=false
SKIP_PR=false
NON_INTERACTIVE=false
COMMIT_TYPE=""
TITLE=""
DESCRIPTION=""
BASE_BRANCH="main"
CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "HEAD")

# コミットタイプの定義
declare -A COMMIT_TYPES=(
    ["feat"]="新機能"
    ["fix"]="バグ修正"
    ["refactor"]="リファクタリング"
    ["docs"]="ドキュメント"
    ["style"]="フォーマット"
    ["test"]="テスト"
    ["chore"]="その他"
    ["perf"]="パフォーマンス"
    ["ci"]="CI/CD"
)

##############################################################################
# ログ関数
##############################################################################
log_info()  { echo -e "${BLUE}[INFO]${NC} $1" >&2; }
log_success() { echo -e "${GREEN}[OK]${NC} $1" >&2; }
log_warn()   { echo -e "${YELLOW}[WARN]${NC} $1" >&2; }
log_error()  { echo -e "${RED}[ERROR]${NC} $1" >&2; }
log_step()   { echo -e "\n${BLUE}=== $1 ===${NC}" >&2; }

##############################################################################
# 変更ファイルからコミットタイプを推測
##############################################################################
detect_commit_type() {
    if [[ -n "$COMMIT_TYPE" ]]; then
        echo "$COMMIT_TYPE"
        return
    fi

    # 変更ファイルのパスから推測
    local changed_files=$(git diff --cached --name-only --diff-filter=M 2>/dev/null | head -20)

    if echo "$changed_files" | grep -qE "\.md$|docs/|README"; then
        echo "docs"
    elif echo "$changed_files" | grep -qE "\.yml$|\.yaml$|\.github/|Dockerfile"; then
        echo "ci"
    elif echo "$changed_files" | grep -qE "test/|.*Test\.java"; then
        echo "test"
    elif echo "$changed_files" | grep -qiE "fix|bug|修正"; then
        echo "fix"
    elif echo "$changed_files" | grep -qiE "refactor|簡素|整理"; then
        echo "refactor"
    elif echo "$changed_files" | grep -qiE "perf|optim|最適"; then
        echo "perf"
    else
        echo "chore"
    fi
}

##############################################################################
# 変更の有無をチェック
##############################################################################
has_changes() {
    ! git diff --quiet || ! git diff --cached --quiet
}

##############################################################################
# ファイルが変わったか（新規・修正・削除）
##############################################################################
has_file_changes() {
    [[ $(git diff --name-only | wc -l) -gt 0 ]] || [[ $(git diff --cached --name-only | wc -l) -gt 0 ]]
}

##############################################################################
# メイン処理
##############################################################################

# オプション解析
while [[ $# -gt 0 ]]; do
    case $1 in
        --dry-run)         DRY_RUN=true; shift ;;
        --no-build)        SKIP_BUILD=true; shift ;;
        --no-pr)           SKIP_PR=true; shift ;;
        --non-interactive) NON_INTERACTIVE=true; shift ;;
        --type)            COMMIT_TYPE="$2"; shift 2 ;;
        *)
            if [[ -z "$TITLE" ]]; then
                TITLE="$1"
            elif [[ -z "$DESCRIPTION" ]]; then
                DESCRIPTION="$1"
            fi
            shift
            ;;
    esac
done

# 引数のデフォルト値
TITLE="${TITLE:-作業完了}"
DESCRIPTION="${DESCRIPTION:-作業完了による変更}"

log_info "🚀 ワークフロー開始"
log_info "ブランチ: $CURRENT_BRANCH"

# 1. 変更チェック
log_step "変更チェック"
if ! has_file_changes; then
    log_warn "変更がないためワークフローをスキップします"
    exit 0
fi
log_success "変更を検出しました"

# 2. ビルド（スキップ可能）
if [[ "$SKIP_BUILD" == false ]]; then
    log_step "Mavenビルド"
    if [[ "$DRY_RUN" == true ]]; then
        log_info "[DRY-RUN] mvn clean package -Dmaven.test.skip=true"
    else
        if mvn clean package -Dmaven.test.skip=true -q; then
            log_success "ビルド成功"
        else
            log_error "ビルド失敗"
            [[ "$NON_INTERACTIVE" == true ]] && exit 1
            read -p "続行しますか? (y/N): " -n 1 -r && [[ $REPLY =~ ^[Yy]$ ]] || exit 1
        fi
    fi
fi

# 3. コミットタイプ推測
TYPE=$(detect_commit_type)
log_info "コミットタイプ: $TYPE (${COMMIT_TYPES[$TYPE]})"

# 4. コミット
log_step "コミット作成"
git add -A

# 既にステージされているコミットがあるかチェック
if git diff --cached --quiet; then
    log_warn "ステージする変更がないためスキップします"
else
    COMMIT_MSG="${TYPE}: ${TITLE}

${DESCRIPTION}

---
Co-authored-by: Claude Code <claude@anthropic.com>"

    if [[ "$DRY_RUN" == true ]]; then
        log_info "[DRY-RUN] コミットメッセージ: $COMMIT_MSG"
    else
        git commit -m "$COMMIT_MSG"
        log_success "コミット作成: $(git log -1 --oneline)"
    fi
fi

# 5. プッシュ
log_step "プッシュ"
if [[ "$DRY_RUN" == true ]]; then
    log_info "[DRY-RUN] git push"
else
    if git rev-parse --verify "origin/$CURRENT_BRANCH" > /dev/null 2>&1; then
        git push
    else
        git push -u origin "$CURRENT_BRANCH"
    fi
    log_success "プッシュ完了"
fi

# 6. PR作成（スキップ可能）
if [[ "$SKIP_PR" == false ]]; then
    log_step "プルリクエスト"

    EXISTING_PR=$(gh pr list --head "$CURRENT_BRANCH" --json number --jq '.[0].number' 2>/dev/null || echo "")

    if [[ -n "$EXISTING_PR" ]]; then
        log_warn "PR #$EXISTING_PR が既に存在します"
        if [[ "$NON_INTERACTIVE" == true ]]; then
            log_info "非対話モードのためスキップします"
        else
            read -p "更新しますか? (y/N): " -n 1 -r && echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                gh pr edit "$EXISTING_PR" --title "$TITLE" --body "$DESCRIPTION"
                log_success "PRを更新しました"
            fi
        fi
    else
        if [[ "$DRY_RUN" == true ]]; then
            log_info "[DRY-RUN] gh pr create"
        else
            gh pr create --base "$BASE_BRANCH" --title "$TITLE" --body "$DESCRIPTION" 2>/dev/null && \
                log_success "PR作成完了" || \
                log_warn "PR作成をスキップしました（既に存在する可能性）"
        fi
    fi
fi

log_success "✅ ワークフロー完了"
