# Development Commands

## ビルドとコンパイル
```bash
# プロジェクトをコンパイル
mvn compile

# JARをビルド
mvn package

# クリーンビルド
mvn clean package

# テストを実行（将来的に追加予定）
mvn test
```

## Git操作
```bash
# 現在のステータス確認
git status

# 変更をステージング
git add <files>

# コミット
git commit -m "message"

# ブランチ確認
git branch

# ログ確認
git log --oneline --graph
```

## ファイル検索
```bash
# ファイルを検索
find . -name "*.java"

# コンテンツ検索（ripgrep推奨）
rg "class DamageManager" src/

# grep使用（ripgrepがない場合）
grep -r "class DamageManager" src/
```

## プラグインテスト用サーバー
```bash
# Spigotサーバー起動（開発用）
cd /path/to/spigot/server
java -jar spigot-1.20.1.jar

# プラグイン配置
cp target/rpg-plugin-1.0.0.jar /path/to/spigot/server/plugins/
```

## データベース操作（SQLite）
```bash
# SQLiteデータベースに接続
sqlite3 plugins/RPGPlugin/data/database.db

# クエリ実行
sqlite3 plugins/RPGPlugin/data/database.db "SELECT * FROM player_stats;"
```

## ログ確認
```bash
# サーバーログを監視
tail -f logs/latest.log

# エラーログのみフィルタリング
tail -f logs/latest.log | grep -i error
```
