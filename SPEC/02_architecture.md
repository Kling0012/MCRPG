# 最終アーキテクチャ設計

## パッケージ構成（確定版）

```
com.example.rpgplugin
├── RPGPlugin.java              # メインクラス
├── core/                       # コアシステム
│   ├── config/                 # 設定管理・ホットリロード
│   │   ├── Configuration.java
│   │   ├── ConfigLoader.java
│   │   └── YamlConfigManager.java
│   ├── dependency/             # Vault, MythicMobs連携
│   │   ├── DependencyManager.java
│   │   ├── VaultHook.java
│   │   └── MythicMobsHook.java
│   └── module/                 # モジュールシステム
│       ├── IModule.java
│       └── ModuleManager.java
├── storage/                    # データ永続化
│   ├── StorageManager.java
│   ├── database/
│   │   ├── DatabaseManager.java
│   │   ├── ConnectionPool.java
│   │   └── SchemaManager.java
│   ├── repository/
│   │   ├── IRepository.java
│   │   ├── PlayerDataRepository.java
│   │   └── CacheRepository.java
│   └── models/
│       ├── PlayerData.java
│       └── Serializable.java
├── player/
│   ├── PlayerManager.java
│   ├── RPGPlayer.java
│   └── cache/
│       └── PlayerCache.java
├── stats/                      # ステータスシステム
│   ├── Stat.java               # Enum: STR, INT, SPI, VIT, DEX
│   ├── StatManager.java
│   ├── StatModifier.java
│   └── calculator/
│       ├── DamageCalculator.java
│       └── StatCalculator.java
├── class/                      # クラスシステム（重要）
│   ├── RPGClass.java           # クラス基底
│   ├── ClassManager.java
│   ├── ClassLoader.java        # YAMLローダー
│   ├── ClassUpgrader.java      # クラスアップ処理
│   ├── requirements/
│   │   └── ClassRequirement.java
│   └── growth/                 # レベルアップ成長設定
│       └── StatGrowth.java
├── skill/                      # スキルシステム（重要）
│   ├── Skill.java              # 共通スキル基底
│   ├── SkillManager.java
│   ├── SkillLoader.java        # YAMLローダー
│   ├── SkillTree.java          # スキルツリー
│   ├── executor/
│   │   ├── SkillExecutor.java
│   │   ├── ActiveSkillExecutor.java
│   │   └── PassiveSkillExecutor.java
│   └── config/
│       └── SkillConfig.java
├── damage/                     # ダメージシステム
│   ├── DamageManager.java
│   ├── DamageModifier.java
│   └── handlers/
│       ├── PlayerDamageHandler.java
│       └── EntityDamageHandler.java
├── gui/                        # GUIシステム
│   ├── GUIManager.java
│   ├── inventory/
│   │   ├── CustomInventory.java
│   │   └── InventoryHolder.java
│   ├── menu/
│   │   ├── StatMenu.java       # ステータス振りGUI
│   │   ├── ClassMenu.java
│   │   └── SkillTreeMenu.java  # スキルツリーGUI
│   └── components/
│       ├── GUIItem.java
│       └── ClickAction.java
├── mythicmobs/                 # MythicMobs連携
│   ├── MythicMobsManager.java
│   ├── mob/
│   │   ├── MythicMob.java
│   │   └── DropHandler.java
│   └── listener/
│       └── MythicDeathListener.java
├── api/                        # 外部API（重要）
│   ├── RPGPluginAPI.java       # メインAPI
│   ├── placeholder/            # PlaceholderAPI連携
│   │   └── RPGPlaceholderExpansion.java
│   ├── skript/                 # SKriptネイティブ統合
│   │   ├── RPGSkriptAddon.java # 登録クラス
│   │   ├── expressions/        # SKript式
│   │   │   ├── ExprRPGLevel.java
│   │   │   ├── ExprRPGStat.java
│   │   │   ├── ExprRPGClass.java
│   │   │   └── ExprRPGSkill.java
│   │   ├── conditions/         # SKript条件
│   │   │   ├── CondHasRPGSkill.java
│   │   │   ├── CondCanUpgradeRPGClass.java
│   │   │   ├── CondRPGStatAbove.java
│   │   │   └── CondIsRPGClass.java
│   │   ├── effects/            # SKript効果
│   │   │   ├── EffUnlockRPGSkill.java
│   │   │   ├── EffCastRPGSkill.java
│   │   │   ├── EffSetRPGClass.java
│   │   │   └── EffModifyRPGStat.java
│   │   └── events/             # SKriptイベント
│   │       └── EvtRPGSkillCast.java
│   ├── bridge/                 # ブリッジ（コマンドベース）
│   │   ├── SKriptBridge.java
│   │   ├── SKriptSkillEvent.java
│   │   └── DenizenBridge.java
│   └── RPGPluginAPI.java       # メインAPI
├── exp/                        # 経験値システム（重要）
│   ├── ExpManager.java
│   ├── ExpDiminisher.java      # 減衰計算
│   └── VanillaExpHandler.java  # バニラEXP連携
└── command/                    # コマンドシステム
    ├── CommandFramework.java
    ├── BaseCommand.java
    └── commands/
        ├── StatsCommand.java
        ├── ClassCommand.java
        ├── SkillCommand.java
        ├── AdminCommand.java
        └── APICommand.java      # 外部スクリプト用コマンド
```

## 設計パターン

| パターン | 用途 |
|---------|------|
| **ファサード** | 各サブシステムの統一API |
| **ストラテジー** | スキル実行、ダメージ計算の切り替え |
| **オブザーバー** | イベント駆動のシステム連携 |
| **リポジトリ** | データアクセスの抽象化 |
| **ビルダー** | 複雑なオブジェクト構築 |
| **プロキシ** | キャッシュ層の実装 |
