# テスト戦略概要

## 作成日
2026-01-15

## プロジェクト概要
- 総ソース: 197ファイル
- 既存テスト: 98ファイル
- カバレッジ: 約49.7%

## 0%カバレッジ未テストパッケージ（優先順位付き）

### P0（最優先）- コア機能
1. **command** (1ファイル)
   - RPGAdminCommand: 管理コマンド

2. **gui** (2ファイル)
   - SkillTreeGUI: スキルツリーGUI
   - SkillTreeGUIListener: GUIイベント

3. **player.exp** (1ファイル)
   - ExpManager: 経験値統合

### P1（重要）- API統合
4. **api** (3ファイル)
   - RPGPluginAPI, Impl, APICommand

5. **api.placeholder** (1ファイル)
   - RPGPlaceholderExpansion

6. **api.skript** (20+ファイル)
   - Skript統合: Expressions, Conditions, Effects, Events

### P2（中優先）- データ永続化
7. **storage** (9ファイル)
   - StorageManager, DatabaseManager, ConnectionPool
   - SchemaManager, PlayerDataRepository, CacheRepository
   - migrations/*

## テスト実装フェーズ

### Phase 1: P0 コア機能（Week 1）
- RPGAdminCommand: 14テスト項目
- SkillTreeGUI: 20+テスト項目
- SkillTreeGUIListener: 5テスト項目
- ExpManager: 6テスト項目

### Phase 2: P1 API統合（Week 2）
- RPGPluginAPI/Impl: 8テスト項目
- APICommand: 4テスト項目
- RPGPlaceholderExpansion: 5テスト項目
- Skript統合: 20+テスト項目

### Phase 3: P2 データ永続化（Week 3）
- StorageManager: 5テスト項目
- DatabaseManager: 6テスト項目
- ConnectionPool: 5テスト項目
- SchemaManager: 4テスト項目
- PlayerDataRepository: 6テスト項目
- CacheRepository: 4テスト項目
- Migrations: 4テスト項目

## 目標カバレッジ
- 全体: 75%以上
- コア機能: 90%以上
- API層: 80%以上
- データ層: 85%以上

## 使用ツール
- JUnit 5
- Mockito
- AssertJ

## ベストプラクティス
- 日本語DisplayName
- 正常系/異常系/境界値テスト
- モック活用
- ネストされたテストクラス
