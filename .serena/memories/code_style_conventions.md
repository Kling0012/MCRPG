# Code Style and Conventions

## 命名規則
- **クラス名**: PascalCase (例: `DamageManager`, `StatManager`)
- **メソッド名**: camelCase (例: `calculateDamage`, `getBaseStat`)
- **変数名**: camelCase (例: `baseDamage`, `finalDamage`)
- **定数名**: UPPER_SNAKE_CASE (例: `DEFAULT_CRITICAL_RATE`)
- **Enum定数**: UPPER_SNAKE_CASE (例: `STRENGTH`, `INTELLIGENCE`)

## コーディング規約
- **インデント**: 4スペース
- **ファイルエンコーディング**: UTF-8
- **行長**: 最大120文字（推奨）
- **インポート**: アルファベット順、staticインポートは最後

## アノテーション
- **@EventHandler**: 全てのイベントハンドラーに必須
- **Override**: オーバーライドメソッドに必須
- **FunctionalInterface**: 関数型インターフェースに使用

## ドキュメント
- **Javadoc**: パブリッククラス/メソッドには必須
- **コメント**: 複雑なロジックには説明コメントを追加
- **言語**: コード内のコメントは日本語で記述

## ログ出力
- **レベル**: SEVERE（エラー）, WARNING（警告）, INFO（一般情報）, FINE（デバッグ）
- **フォーマット**: `[SystemName] Message format`
- **例**: `logger.info("[Damage] Player->Zombie: Base=100, STR=50, Final=150")`

## エラーハンドリング
- **例外**: 適切な例外クラスを使用
- ** nullチェック**: 必要な箇所で実施
- **バリデーション**: 入力値の検証を実施

## スレッドセーフティ
- **非同期処理**: Bukkit.getScheduler().runTaskAsynchronously() 使用
- **同期**: ConcurrentHashMap, CopyOnWriteArrayList 使用
- **ロック**: 必要に応じてsynchronizedまたはReentrantLock 使用

## Spigot/Bukkit 固有
- **イベント優先度**: 適切なEventPriority設定（LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR）
- **イベントキャンセル**: event.setCancelled(true) 使用
- **スケジューラー**: runTask, runTaskLater, runTaskTimer 適切に使い分け
