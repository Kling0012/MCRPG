# Minecraft RPG Plugin - Project Overview

## プロジェクト目的
Minecraft 1.20.1 (Spigot) 用の本格的RPGプラグイン。50-150人規模のサーバー向けに設計された、包括的なRPGシステムを提供する。

## 技術スタック
- **Javaバージョン**: 17
- **ビルドツール**: Maven
- ** Minecraftバージョン**: 1.20.1 (Spigot API)
- **データベース**: SQLite + 3層キャッシュ（L1: ConcurrentHashMap, L2: Caffeine, L3: SQLite）
- **外部連携**: MythicMobs 5.6.2, Vault API 1.7.1

## 主要機能
1. **ダメージシステム**: 全イベントキャッチ、ステータス倍率、ダメージカット
2. **ステータスシステム**: STR/INT/SPI/VIT/DEX、レベルアップ時自動+2/手動3ポイント
3. **クラスシステム**: 4初期クラス、ランク6まで、直線/分岐対応
4. **スキルシステム**: 共通スキルプール、クラス選択、スキルツリーGUI
5. **経済システム**: 独自通貨（ゴールド）、オークション（入札10%上乗せ）
6. **GUI**: ステータス振り、スキル取得、トレード
7. **MythicMobs連携**: ドロップ管理（倒した人のみ）
8. **外部API**: SKript/Denizen からアクセス可能

## パッケージ構造
```
com.example.rpgplugin
├── RPGPlugin.java              # メインクラス
├── core/                       # コアシステム（config, dependency, module）
├── storage/                    # データ永続化（SQLite + Cache）
├── player/                     # プレイヤー管理
├── stats/                      # ステータスシステム（Phase2実装済み）
├── gui/                        # GUIシステム（Phase2実装済み）
└── [他]: class/, skill/, damage/, economy/, etc.
```

## 設計パターン
- **ファサード**: 各サブシステムの統一API
- **ストラテジー**: スキル実行、ダメージ計算の切り替え
- **オブザーバー**: イベント駆動のシステム連携
- **リポジトリ**: データアクセスの抽象化
- **ビルダー**: 複雑なオブジェクト構築
- **プロキシ**: キャッシュ層の実装

## 開発状況
- **Phase1**: コア基盤（完了）
- **Phase2**: ステータスシステム + GUI（完了）
- **Phase3**: ダメージシステム（完了）
- **Phase11**: スキルシステム拡張（進行中）
  - Phase11-2: レベル依存パラメータ（完了）
  - Phase11-4: スキルエフェクト範囲システム/targetパッケージ（完了）
  - Phase11-5: 外部API拡張（完了）
  - Phase11-6: 新YAMLフォーマット対応（完了）
  - Phase11-7: SkillManager更新（完了）
  - Phase11-8: データベーススキーマ更新（進行中）（現在実装中）
