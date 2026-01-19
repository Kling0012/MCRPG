#!/bin/bash
##############################################################################
# Gitエイリアス設定 - 作業完了ワークフローのショートカット
#
# 使用方法:
#   source ./.scripts/aliases.sh
#
# 設定されるエイリアス:
#   git done        : ビルド → コミット → プッシュ → PR作成
#   git build       : Mavenビルドのみ
#   git artifact    : 成果物のパスを表示
##############################################################################

# エイリアス: 作業完了（全工程実行）
git config --local alias.done "!f() { bash ./.scripts/workflow-complete.sh \"$@\"; }; f"

# エイリアス: ビルドのみ
git config --local alias.build "!mvn clean package -DskipTests"

# エイリアス: 成果物パス表示
git config --local alias.artifact "!echo \"target/$(ls target/*.jar 2>/dev/null | grep -v 'javadoc\\|sources' | head -1 | xargs basename)\""

# エイリアス: ビルド + 成果物コピー
git config --local alias.deploy "!f() { mvn clean package -DskipTests && mkdir -p ../deploy && cp target/*.jar ../deploy/ && echo \"Deploy: ../deploy/$(ls target/*.jar | head -1 | xargs basename)\"; }; f"

echo "✅ Gitエイリアスを設定しました"
echo ""
echo "使用可能なコマンド:"
echo "  git done [タイトル] [説明]  : 作業完了ワークフロー実行"
echo "  git build                    : Mavenビルド実行"
echo "  git artifact                 : 成果物パス表示"
echo "  git deploy                   : ビルドして ../deploy/ にコピー"
