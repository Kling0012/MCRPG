# 最終実装優先順位

## フェーズ1: コア基盤（1-2週間）【最優先】 ✅ **実装完了**

**優先度: 最高**

- [x] モジュールシステムと依存性管理
  - ModuleManager, IModuleインターフェース (`core/module/`)
  - DependencyManager（MythicMobs最新版チェック）(`core/dependency/`)
  - エラーハンドリングとログシステム

- [x] データ永続化レイヤー（50-150人対応）
  - DatabaseManager（SQLite接続プール）(`storage/database/`)
  - SchemaManager（自動テーブル作成）(`storage/database/`)
  - PlayerDataRepository（CRUD操作）(`storage/repository/`)
  - 3層キャッシュ実装（L1, L2, L3）(`storage/repository/CacheRepository.java`)

- [x] 設定管理システム
  - Configuration, ConfigLoader (`core/config/`)
  - YamlConfigManager (`core/config/`)
  - FileWatcher（ホットリロード）(`core/config/ConfigWatcher.java`)

**成果物**:
- ✅ プラグイン起動、SQLiteデータベース接続
- ✅ YAML設定ファイル読み込み
- ✅ MythicMobs連携準備完了
- ✅ キャッシュシステム動作

---

## フェーズ2: ステータスシステム（1週間） ✅ **実装完了**

**優先度: 高**

- [x] ステータス基盤
  - Stat Enum（STR, INT, SPI, VIT, DEX）(`stats/Stat.java`)
  - StatManager（EnumMap使用）(`stats/StatManager.java`)
  - RPGPlayer（プレイヤーラッパー）(`player/RPGPlayer.java`)
  - StatModifier（バフ/デバフ）(`stats/StatModifier.java`)

- [x] レベルアップシステム（バニラ統合）
  - バニラLV/EXP監視 (`player/VanillaExpHandler.java`)
  - 自動配分（+2）+ 手動配分（3ポイント）
  - レベルアップイベント

- [x] ダメージ計算基盤
  - StatCalculator（ステータス→パラメータ変換）(`stats/calculator/StatCalculator.java`)

- [x] ステータス振りGUI
  - StatMenu実装 (`gui/menu/StatMenu.java`)
  - 左クリック+、右クリック-
  - StatMenuListener (`gui/menu/StatMenuListener.java`)

**成果物**:
- ✅ バニラレベルアップで自動+2、手動3ポイント獲得
- ✅ `/rpg stats` コマンドでGUIオープン
- ✅ ダメージ計算基盤完成

---

## フェーズ3: ダメージシステム（1週間） ✅ **実装完了**

**優先度: 高**

- [x] ダメージイベント処理
  - DamageManager (`damage/DamageManager.java`)
  - EntityDamageEventハンドラー（HIGH優先度）(`damage/handlers/EntityDamageHandler.java`)
  - ダメージ倍率適用

- [x] ダメージ修正システム
  - DamageModifier（属性、クラス補正）(`damage/DamageModifier.java`)
  - クリティカル計算（DEX影響）
  - 防御力計算（VIT影響）
  - PlayerDamageHandler (`damage/handlers/PlayerDamageHandler.java`)

- [x] ダメージ表示
  - アクションバー表示
  - コンソールログ（デバッグ用）

**成果物**:
- ✅ ステータスに応じたダメージ変動
- ✅ プレイヤーへのダメージカット（VIT依存）
- ✅ ダメージ数値の可視化

---

## フェーズ4: クラスシステム（1-2週間） ✅ **実装完了**

**優先度: 高**

- [x] クラス基盤（重要）
  - RPGClassクラス (`rpgclass/RPGClass.java`)
  - ClassManager (`rpgclass/ClassManager.java`)
  - ClassLoader（外部YAML読み込み）(`rpgclass/ClassLoader.java`)
  - StatGrowth（自動+手動配分設定）(`rpgclass/growth/StatGrowth.java`)

- [x] クラスアップシステム（重要）
  - ClassUpgrader (`rpgclass/ClassUpgrader.java`)
  - 直線パターン対応
  - 分岐パターン対応
  - 条件付きクラスアップ（アイテム、クエスト、レベル、ステータス）
    - ClassRequirement (`rpgclass/requirements/ClassRequirement.java`)
    - LevelRequirement (`rpgclass/requirements/LevelRequirement.java`)
    - StatRequirement (`rpgclass/requirements/StatRequirement.java`)
    - ItemRequirement (`rpgclass/requirements/ItemRequirement.java`)
    - QuestRequirement (`rpgclass/requirements/QuestRequirement.java`)

- [x] GUI実装
  - ClassMenu（クラス選択GUI）(`gui/menu/rpgclass/ClassMenu.java`)
  - ClassMenuListener (`gui/menu/rpgclass/ClassMenuListener.java`)
  - InventoryHolderパターン

**成果物**:
- ✅ 4初期クラス実装（戦士、大盾使い、魔法使い、弓使い）
- ✅ 各クラスRank1-6のテンプレート用意
- ✅ `/rpg class` コマンドでクラス選択GUI
- ✅ `/rpg api upgrade_class` コマンドで外部からクラスアップ

---

## フェーズ5: スキルシステム（2-3週間） ✅ **実装完了**

**優先度: 中**

- [x] スキル基盤（重要）
  - SkillクラスとSkillManager (`skill/Skill.java`, `skill/SkillManager.java`)
  - SkillLoader（外部YAML）(`skill/SkillLoader.java`)
  - SkillTree（スキルツリー）(`skill/SkillTree.java`)
  - SkillNode (`skill/SkillNode.java`)
  - SkillType (`skill/SkillType.java`)
  - SkillConfig (`skill/config/SkillConfig.java`)

- [x] 共通スキルプール（重要）
  - 全スキル定義（YAML）
  - クラスで選択する仕組み

- [x] アクティブスキル
  - スキル発動システム
  - クールダウン管理
  - ATTR依存ダメージ計算
  - ActiveSkillExecutor (`skill/executor/ActiveSkillExecutor.java`)

- [x] パッシブスキル
  - 常時効果適用
  - 条件付き発動
  - PassiveSkillExecutor (`skill/executor/PassiveSkillExecutor.java`)

- [x] スキルGUI
  - SkillMenu (`gui/menu/SkillMenu.java`)
  - SkillMenuListener (`gui/menu/SkillMenuListener.java`)

**成果物**:
- ✅ 共通スキルプール実装（10-15種類）
- ✅ 各クラスで3-5個のスキルを選択
- ✅ `/rpg skill` コマンドでスキルツリーGUI
- ✅ `/rpg api cast_skill` コマンドで外部から発動

---

## フェーズ6: 経済・オークションシステム（1-2週間） ✅ **実装完了**

**優先度: 中**

- [x] 独自通貨システム（重要）
  - CurrencyManager (`currency/CurrencyManager.java`)
  - CurrencyListener (`currency/CurrencyListener.java`)
  - PlayerCurrency (`storage/models/PlayerCurrency.java`)
  - PlayerCurrencyRepository (`storage/repository/PlayerCurrencyRepository.java`)
  - 通貨入手方法実装

- [x] オークション（重要）
  - AuctionManager (`auction/AuctionManager.java`)
  - BiddingSystem（入札10%上乗せ、30-180秒、+5秒延長）(`auction/BiddingSystem.java`)
  - Auction (`auction/Auction.java`)
  - AuctionListing (`auction/AuctionListing.java`)
  - 手数料システム
  - AuctionCommand (`auction/AuctionCommand.java`)

- [x] GUI（オプション）
  - AuctionMenu（出品一覧）

**成果物**:
- ✅ 独自通貨「ゴールド」実装
- ✅ オークションシステム完全動作
- ✅ `/rpg auction` コマンド群

---

## フェーズ7: トレード・GUI（1週間） ✅ **実装完了**

**優先度: 中**

- [x] トレードシステム（重要）
  - TradeManager (`trade/TradeManager.java`)
  - TradeSession (`trade/TradeSession.java`)
  - TradeInventory（トレードGUI）(`trade/TradeInventory.java`)
  - TradeMenuListener (`trade/TradeMenuListener.java`)
  - TradeHistoryRepository (`trade/repository/TradeHistoryRepository.java`)
  - TradeOffer (`trade/model/TradeOffer.java`)
  - TradeParty (`trade/model/TradeParty.java`)

- [x] GUI追加
  - ステータス振りGUI（✅ 実装済み）
  - スキルツリーGUI（✅ 実装済み）
  - トレードGUI（✅ 実装済み）

**成果物**:
- ✅ プレイヤー間トレード実装
- ✅ `/rpg trade request <player>` コマンド
- ✅ トレードGUI完全動作

---

## フェーズ8: MythicMobs連携（1週間） ✅ **実装完了**

**優先度: 中**

- [x] MythicMobs Hook
  - MythicMobsManager (`mythicmobs/MythicMobsManager.java`)
  - MythicMobsHook (`core/dependency/MythicMobsHook.java`)
  - MythicMobs最新版API統合

- [x] ドロップシステム
  - DropHandler（倒した人のみ）(`mythicmobs/drop/DropHandler.java`)
  - DropData (`mythicmobs/drop/DropData.java`)
  - DropRepository (`mythicmobs/drop/DropRepository.java`)
  - MobDropConfig (`mythicmobs/config/MobDropConfig.java`)
  - MythicDeathListener (`mythicmobs/listener/MythicDeathListener.java`)
  - 独占ドロップ実装（NBTタグ）

**成果物**:
- ✅ MythicMobsドロップ制御
- ✅ ドロップアイテムの所有者限定取得（5分間）

---

## フェーズ9: 外部API実装（1週間） ✅ **実装完了**

**優先度: 高**

- [x] メインAPI（重要）
  - RPGPluginAPI (`api/RPGPluginAPI.java`)
  - RPGPluginAPIImpl (`api/RPGPluginAPIImpl.java`)
  - 全メソッド実装

- [x] SKriptブリッジ（重要）
  - SKriptBridge (`api/bridge/SKriptBridge.java`)
  - SKript用コマンド実装

- [x] Denizenブリッジ（重要）
  - DenizenBridge (`api/bridge/DenizenBridge.java`)
  - Denizenタグ実装

- [x] APIコマンド（重要）
  - APICommand (`api/command/APICommand.java`)

- [x] テンプレート用意（重要）
  - クラステンプレート（melee, ranged, magic）
  - スキルテンプレート（active, passive）

**成果物**:
- ✅ 外部プラグインからアクセス可能なAPI
- ✅ SKriptサンプルスクリプト
- ✅ Denizenサンプルスクリプト
- ✅ クラス・スキル作成テンプレート

---

## フェーズ10: 経験値減衰・最終調整（1週間） ✅ **実装完了**

**優先度: 低**

- [x] 経験値減衰（重要）
  - ExpDiminisher (`player/ExpDiminisher.java`)
  - VanillaExpHandler (`player/VanillaExpHandler.java`)
  - ExpManager (`player/exp/ExpManager.java`)
  - 減衰設定YAML

- [x] パフォーマンス最適化
  - クエリ最適化
  - キャッシュチューニング（Phase10-2で実装）
  - 非同期処理見直し（Phase10-2で実装）
  - ダメージ計算キャッシュ（Phase10-2で実装）

- [x] テスト・デバッグ
  - ユニットテスト（DamageModifierTest 実装済み）
  - 構造検証テスト（PluginLifecycleTest 実装済み）
  - ~~ロードテスト~~（未実装、今後対応）

- [x] ドキュメント
  - APIドキュメント (`docs/API_DOCUMENTATION.md`)
  - MockBukkit導入ガイド (`docs/MOCKBUKKIT_INTEGRATION_GUIDE.md`)
  - Phase10-2実装レポート (`docs/PHASE10-2_IMPLEMENTATION_REPORT.md`)
  - ~~Wiki作成~~（未実装）
  - ~~SKript/Denizen連携ガイド~~（一部実装）

**成果物**:
- ✅ 経験値減衰機能実装
- ✅ 全機能テスト完了（構造検証ベース）
- ✅ 基本ドキュメント整備
- ✅ リリース準備完了

---

## 最終マイルストーン

### フェーズ1-3完了時点（週4） ✅ **完了**
- ✅ コア基盤完成
- ✅ ステータスシステム動作
- ✅ ダメージシステム実装
- ✅ 基本的な戦闘システム完成

### フェーズ4-5完了時点（週9） ✅ **完了**
- ✅ クラスシステム完全動作（4クラス、Rank6）
- ✅ スキルシステム実装（共通スキルプール）
- ✅ GUIメニュー完成（ステータス、クラス、スキルツリー）
- ✅ 基本的なRPG体験可能

### フェーズ6-8完了時点（週11） ✅ **完了**
- ✅ 独自通貨システム
- ✅ オークション・トレードシステム
- ✅ MythicMobs連携

### フェーズ9-10完了時点（週12） ✅ **完了**
- ✅ 外部API完全実装（SKript/Denizen）
- ✅ 経験値減衰
- ✅ 全機能実装
- ✅ テスト完了（構造検証ベース）
- ✅ 基本ドキュメント整備
- ✅ リリース準備完了
