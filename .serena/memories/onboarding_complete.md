# Serena Onboarding Complete - Minecraft RPG Plugin

## プロジェクトの概要
- **目的**: Minecraft 1.20.6 (Paper) 用の本格的RPGプラグイン
- **対象サーバースケール**: 50-150人規模
- **開発状況**: 全フェーズ完了（Phase 1-10）、プロダクションレディ

## 技術スタック
- **Javaバージョン**: 21
- **ビルドツール**: Maven
- **Minecraftバージョン**: 1.20.6 (Paper API)
- **データベース**: SQLite + 3層キャッシュ（L1: ConcurrentHashMap, L2: Caffeine, L3: SQLite）
- **外部連携**: MythicMobs 5.6.2, Vault API 1.7.1
- **テスト**: JUnit 5, Mockito, AssertJ, JaCoCo

## 主要機能
1. ダメージシステム（全イベントキャッチ、ステータス倍率、ダメージカット）
2. ステータスシステム（STR/INT/SPI/VIT/DEX、レベルアップ時自動+2/手動3ポイント）
3. クラスシステム（4初期クラス、ランク6まで、直線/分岐対応）
4. スキルシステム（共通スキルプール、クラス選択、スキルツリーGUI）
5. 経済システム（独自通貨「ゴールド」、オークション）
6. GUIシステム（ステータス振り、スキル取得、トレード）
7. MythicMobs連携（ドロップ管理）
8. 外部API（SKript/Denizen からアクセス可能）
9. 経験値システム（バニラLV/EXP使用、減衰設定）

## パッケージ構成
```
com.example.rpgplugin
├── RPGPlugin.java              # メインクラス
├── core/                       # コアシステム（config, dependency, module）
├── storage/                    # データ永続化（SQLite + 3層キャッシュ）
├── player/                     # プレイヤー管理
├── stats/                      # ステータスシステム
├── rpgclass/                   # クラスシステム
├── skill/                      # スキルシステム
├── damage/                     # ダメージシステム
├── currency/                   # 経済システム
├── auction/                    # オークションシステム
├── trade/                      # トレードシステム
├── gui/                        # GUIシステム
├── mythicmobs/                 # MythicMobs連携
├── api/                        # 外部API
└── command/                    # コマンドシステム
```

## 設計パターン
- **ファサード**: 各サブシステムの統一API
- **ストラテジー**: スキル実行、ダメージ計算の切り替え
- **オブザーバー**: イベント駆動のシステム連携
- **リポジトリ**: データアクセスの抽象化
- **ビルダー**: 複雑なオブジェクト構築
- **プロキシ**: キャッシュ層の実装

## 開発コマンド

### ビルドとテスト
```bash
# プロジェクトをコンパイル
mvn compile

# JARをビルド
mvn package

# クリーンビルド
mvn clean package

# テストを実行
mvn test

# 特定のテストクラスを実行
mvn test -Dtest=ClassName

# 特定のテストメソッドを実行
mvn test -Dtest=ClassName#testMethod

# カバレッジレポート生成
mvn test
# レポート: target/site/jacoco/index.html
```

### Git操作
```bash
# 現在のステータス確認
git status

# 変更をステージング
git add <files>

# コミット
git commit -m "message"

# ログ確認
git log --oneline --graph
```

### ファイル検索
```bash
# ファイルを検索
find . -name "*.java"

# コンテンツ検索（ripgrep推奨）
rg "class DamageManager" src/

# grep使用（ripgrepがない場合）
grep -r "class DamageManager" src/
```

### プラグインテスト用サーバー
```bash
# Spigotサーバー起動
java -jar spigot-1.20.6.jar

# プラグイン配置
cp target/rpg-plugin-1.0.0.jar /path/to/spigot/server/plugins/
```

## コードスタイルと規約

### 命名規則
- **クラス名**: PascalCase（例: `DamageManager`, `StatManager`）
- **メソッド名**: camelCase（例: `calculateDamage`, `getBaseStat`）
- **変数名**: camelCase（例: `baseDamage`, `finalDamage`）
- **定数名**: UPPER_SNAKE_CASE（例: `DEFAULT_CRITICAL_RATE`）
- **Enum定数**: UPPER_SNAKE_CASE（例: `STRENGTH`, `INTELLIGENCE`）

### コーディング規約
- **インデント**: 4スペース
- **ファイルエンコーディング**: UTF-8
- **行長**: 最大120文字（推奨）
- **インポート**: アルファベット順、staticインポートは最後

### ドキュメント
- **Javadoc**: パブリッククラス/メソッドには必須
- **コメント**: 複雑なロジックには説明コメントを追加
- **言語**: コード内のコメントは日本語で記述

### ログ出力
- **レベル**: SEVERE（エラー）, WARNING（警告）, INFO（一般情報）, FINE（デバッグ）
- **フォーマット**: `[SystemName] Message format`
- **例**: `logger.info("[Damage] Player->Zombie: Base=100, STR=50, Final=150")`

### エラーハンドリング
- **例外**: 適切な例外クラスを使用
- **nullチェック**: 必要な箇所で実施
- **バリデーション**: 入力値の検証を実施

### スレッドセーフティ
- **非同期処理**: Bukkit.getScheduler().runTaskAsynchronously() 使用
- **同期**: ConcurrentHashMap, CopyOnWriteArrayList 使用
- **ロック**: 必要に応じてsynchronizedまたはReentrantLock 使用

### Spigot/Bukkit 固有
- **イベント優先度**: 適切なEventPriority設定（LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR）
- **イベントキャンセル**: event.setCancelled(true) 使用
- **スケジューラー**: runTask, runTaskLater, runTaskTimer 適切に使い分け

## 開発ガイドライン
1. 既存のコードを常に確認し、パターンを再利用する
2. モジュールシステムを利用して機能を分離する
3. キャッシュ戦略（L1, L2, L3）を考慮して実装する
4. YAML設定をホットリロード可能にする
5. すべての公開メソッドにJavadocを追加する
6. 非同期処理を適切に使用してパフォーマンスを最適化する

## タスク完了時のチェックリスト
- [ ] コードがコンパイルされる（`mvn compile`）
- [ ] テストが通る（`mvn test`）
- [ ] JaCoCoカバレッジが60%以上であること
- [ ] Javadocが適切に記述されている
- [ ] コードスタイル規約に従っている
- [ ] 非同期処理が適切に使用されている

## 参考ドキュメント
- 詳細な設計書: `spec.md`
- APIドキュメント: `docs/API_DOCUMENTATION.md`
- MockBukkit導入ガイド: `docs/MOCKBUKKIT_INTEGRATION_GUIDE.md`
- Phase10-2実装レポート: `docs/PHASE10-2_IMPLEMENTATION_REPORT.md`
