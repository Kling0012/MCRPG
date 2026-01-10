# Critical Files（実装開始時に重要）

## 既存ファイル

### メインクラス
- **ファイル**: `src/main/java/com/example/rpgplugin/RPGPlugin.java`
- **説明**: プラグインのエントリーポイント、onEnable/onDisable

### コマンド構造
- **ファイル**: `src/main/java/com/example/rpgplugin/RPGCommand.java`
- **説明**: 既存のコマンドハンドラー構造

### Maven依存関係
- **ファイル**: `pom.xml`
- **説明**: Maven依存関係追加（MythicMobs, Vault等）

### プラグイン設定
- **ファイル**: `src/main/resources/plugin.yml`
- **説明**: コマンド・権限・softdepend定義

## 新規作成ファイル（優先順位順）

### 1. コア基盤
| ファイル | 説明 |
|---------|------|
| `core/module/ModuleManager.java` | モジュール管理システム |
| `storage/database/DatabaseManager.java` | SQLite接続プール |
| `storage/repository/PlayerDataRepository.java` | CRUD操作 |

### 2. ステータス
| ファイル | 説明 |
|---------|------|
| `stats/Stat.java` | Enum: STR, INT, SPI, VIT, DEX |
| `stats/StatManager.java` | EnumMap使用した管理 |
| `player/RPGPlayer.java` | プレイヤーラッパー |

### 3. クラス
| ファイル | 説明 |
|---------|------|
| `class/RPGClass.java` | クラス基底 |
| `class/ClassLoader.java` | 外部YAML読み込み |
| `class/ClassUpgrader.java` | クラスアップ処理 |

### 4. スキル
| ファイル | 説明 |
|---------|------|
| `skill/Skill.java` | 共通スキル基底 |
| `skill/SkillLoader.java` | 外部YAML読み込み |
| `skill/SkillTree.java` | スキルツリー構造 |

### 5. GUI
| ファイル | 説明 |
|---------|------|
| `gui/menu/StatMenu.java` | ステータス振りGUI |
| `gui/menu/SkillTreeMenu.java` | スキルツリーGUI |
| `gui/menu/TradeInventory.java` | トレードGUI |

### 6. API
| ファイル | 説明 |
|---------|------|
| `api/RPGPluginAPI.java` | メインAPIインターフェース |
| `api/SKriptBridge.java` | SKript連携ブリッジ |
| `api/DenizenBridge.java` | Denizen連携ブリッジ |

## フェーズ11追加ファイル

### 数式エバリュエーター
| ファイル | 説明 |
|---------|------|
| `skill/evaluator/FormulaEvaluator.java` | 数式評価エンジン |
| `skill/evaluator/VariableContext.java` | 変数コンテキスト |
| `skill/evaluator/ExpressionParser.java` | 式パーサー |

### ターゲット・範囲
| ファイル | 説明 |
|---------|------|
| `skill/target/TargetType.java` | ターゲット種別Enum |
| `skill/target/TargetSelector.java` | ターゲット選択ロジック |
| `skill/target/AreaShape.java` | 範囲形状Enum |
| `skill/target/ShapeCalculator.java` | 幾何計算 |

### MP/HP拡張
| ファイル | 説明 |
|---------|------|
| `player/ManaManager.java` | MP管理システム |
| `storage/migrations/Migration_v1_1_PlayerStats.java` | MP/HP拡張マイグレーション |
