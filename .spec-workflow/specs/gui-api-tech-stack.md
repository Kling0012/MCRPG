# Technology Stack - GUI/API外部化拡張

## プロジェクトタイプ
Minecraft Spigotプラグイン（Java 21）

## GUI/API外部化のための技術仕様

### コア技術

#### プライマリ言語
- **言語**: Java 21
- **ランタイム**: Spigot 1.20.1 API
- **ビルドツール**: Maven

### 既存の依存関係（再利用）

| ライブラリ | 用途 | バージョン |
|-----------|------|----------|
| Spigot API | コアプラグインAPI | 1.20.1 |
| MythicMobs | カスタムモブ連携 | 5.6.2 |
| Vault API | 経済システム連携 | 1.7.1 |
| PlaceholderAPI | プレースホルダー連携 | 最新版 |

### 新規追加コンポーネント

#### GUI/API外部化レイヤー

```
src/main/java/com/example/rpgplugin/
├── api/
│   ├── dto/                    # データ転送用オブジェクト
│   │   ├── APIResult.java
│   │   ├── SkillInfoDTO.java
│   │   └── ClassInfoDTO.java
│   ├── gui/                    # GUI制御API
│   │   └── GUIController.java
│   └── command/                # 既存
│       └── APICommand.java
└── command/
    └── gui/                    # GUIコマンド
        ├── StatGUICommand.java
        ├── SkillGUICommand.java
        └── ClassGUICommand.java
```

### アプリケーションアーキテクチャ

#### レイヤー構造

```
┌─────────────────────────────────────────────────────────────────────┐
│                         プレゼンテーション層                        │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐   │
│  │   コマンド層      │  │    GUI層         │  │   APIブリッジ    │   │
│  │  - RPGCommand    │  │  - StatMenu      │  │  - SKriptBridge  │   │
│  │  - *GUICommand   │  │  - SkillMenu     │  │  - DenizenBridge │   │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          APIファサード層                            │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │                    RPGPluginAPI                             │    │
│  │  + プレイヤーデータ取得                                       │    │
│  │  + ステータス操作（新規: add/remove/reset）                 │    │
│  │  + スキル操作（新規: acquire/upgrade/castAt）               │    │
│  │  + クラス操作（新規: setInitial/checkRequirements）          │    │
│  │  + GUI操作（新規: openStatGUI/openSkillGUI...）             │    │
│  └────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        ビジネスロジック層                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │
│  │ StatManager  │  │ SkillManager │  │ ClassManager │             │
│  │              │  │              │  │ ClassUpgrader│             │
│  └──────────────┘  └──────────────┘  └──────────────┘             │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                           データアクセス層                           │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │                    PlayerManager                            │    │
│  │  - RPGPlayerのロード/保存                                    │    │
│  │  - 3層キャッシュ（ConcurrentHashMap → Caffeine → SQLite）    │    │
│  └────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

### データストレージ

#### 既存ストレージ（再利用）
- **プライマリ**: SQLiteデータベース
- **キャッシュL1**: ConcurrentHashMap（オンラインプレイヤー）
- **キャッシュL2**: Caffeine（最近アクセスしたプレイヤー）
- **キャッシュL3**: SQLite（永続化）

#### データフォーマット
- **設定**: YAML（config.yml, skills/*.yml, classes/*.yml）
- **プレイヤーデータ**: SQLite
- **キャッシュ**: Javaオブジェクト（メモリ内）

### 外部統合

#### 既存API（拡張）

| API | 現在の機能 | 追加機能 |
|-----|-----------|----------|
| SKript | `rpg api call` 経由で基本操作 | GUI操作、ステータス操作 |
| Denizen | プレースホルダー提供 | アクションフック |
| PlaceholderAPI | `%rpg_*%` プレースホルダー | 新規プレースホルダー |

#### プロトコル
- **コマンド**: Bukkit Command API
- **SKript連携**: コマンドブリッジ経由
- **イベント**: Bukkit Event System

### 開発環境

#### ビルド・開発ツール
```bash
# コンパイル
mvn compile

# パッケージング
mvn package

# テスト（将来的に追加）
mvn test
```

#### コード品質ツール
- **静的解析**: Eclipse JDT（IDE内蔵）
- **フォーマット**: SPD4（SPDev Plugin Standards）
- **タブ幅**: 4スペース相当
- **コメント**: 日本語

### 技術要件と制約

#### パフォーマンス要件
- **コマンド応答**: 100ms以内
- **GUIオープン**: 200ms以内
- **API呼び出し**: 50ms以内
- **キャッシュヒット率**: 90%以上

#### 互換性要件
- **Minecraft**: 1.20.1のみ
- **Java**: 21（OpenJDK Microsoft）
- **Spigot**: 1.20.1 Roaming
- **MythicMobs**: 5.6.2以上

### セキュリティとコンプライアンス

#### 権限設計

```yaml
permissions:
  rpg.command.stats:
    description: "ステータスコマンドの使用を許可"
    default: true
  rpg.command.skill:
    description: "スキルコマンドの使用を許可"
    default: true
  rpg.command.class:
    description: "クラスコマンドの使用を許可"
    default: true
  rpg.command.gui:
    description: "GUIコマンドの使用を許可"
    default: true
  rpg.api.modify:
    description: "APIでのデータ変更を許可"
    default: op
  rpg.api.gui:
    description: "APIでのGUI操作を許可"
    default: true
```

#### 入力検証
- 全てのユーザー入力を検証
- ステータス値: 0-255の範囲
- スキルID: 既存スキルのみ
- クラスID: 既存クラスのみ
- 数値: 整数チェック、範囲チェック

### スケーラビリティと信頼性

#### 予想負荷
- **同時接続**: 50-150プレイヤー
- **API呼び出し**: 10-50回/秒
- **GUI操作**: 5-20回/秒

#### 可用性要件
- **稼働時間**: 99.9%（メンテナンス除く）
- **データ損失防止**: トランザクション処理

### 技術決定と根拠

#### 決定ログ

| 決定事項 | 根拠 | 代替案 |
|----------|------|--------|
| コマンドベースのAPI | SKript/Denizenとの互換性 | REST API（外部ツールのみ） |
| DTOパターン | データカプセル化 | 直接オブジェクト返却（密結合） |
| ファサードパターン | シンプルなAPI、テスト容易性 | 直接マネージャーアクセス |
| 既存RPGPluginAPI拡張 | 下位互換性 | 新API作成（破壊的変更） |

### 既知の制限

| 制限事項 | 影響 | 将来の対応 |
|----------|------|-----------|
| GUI操作は同期のみ | 大量操作時にラグ | 非同期GUI API |
| SKriptブリッジ経由のみ | 直接Java呼び出し不可 | 直接Java API公開 |
| 単一サーバーのみ | マルチサーバー連携不可 | データベース共有 |

## 実装ガイドライン

### コマンド実装パターン

```java
/**
 * ステータス操作コマンド
 */
public class StatCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                            String label, String[] args) {
        // 1. 権限チェック
        if (!sender.hasPermission("rpg.command.stats")) {
            sender.sendMessage(ChatColor.RED + "権限がありません");
            return false;
        }

        // 2. プレイヤーチェック
        if (!(sender instanceof Player)) {
            sender.sendMessage("プレイヤーのみ使用可能です");
            return false;
        }

        Player player = (Player) sender;

        // 3. サブコマンド処理
        if (args.length == 0) {
            // GUIを開く
            RPGPlugin.getInstance().getAPI().openStatGUI(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                return handleAdd(player, args);
            case "remove":
                return handleRemove(player, args);
            // ...
        }
    }

    private boolean handleAdd(Player player, String[] args) {
        // 引数チェック
        // バリデーション
        // API呼び出し
        // 結果返却
    }
}
```

### API実装パターン

```java
/**
 * RPGPluginAPIImplの拡張例
 */
@Override
public boolean addStatPoint(Player player, Stat stat, int amount) {
    // 1. 入力検証
    if (player == null || stat == null || amount <= 0) {
        return false;
    }

    // 2. プレイヤーデータ取得
    RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
    if (rpgPlayer == null) {
        return false;
    }

    // 3. ビジネスロジック実行
    StatManager statManager = rpgPlayer.getStatManager();
    int available = statManager.getAvailablePoints();

    if (available < amount) {
        player.sendMessage("§cポイントが足りません");
        return false;
    }

    // 4. 変更適用
    boolean success = statManager.allocatePoint(stat, amount);
    if (success) {
        player.sendMessage(String.format("§a%sに+%dポイント",
            stat.getColoredShortName(), amount));
    }

    return success;
}
```

## 関連ドキュメント

- [GUI/API外部化仕様](.spec-workflow/specs/gui-api-externalization-spec.md)
- [設計テンプレート](.spec-workflow/templates/design-template.md)
- [プロジェクト構造](.spec-workflow/templates/structure-template.md)
