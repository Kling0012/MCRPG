# MockBukkit 導入ガイド

## 概要

MockBukkitは、Bukkitプラグインの完全なライフサイクルテストを可能にするモックフレームワークです。現在のテストはリフレクションベースの構造検証に限定されていますが、MockBukkitを導入することで実際のプラグイン動作をテストできます。

## 現状のテスト制限

現在の `PluginLifecycleTest` はリフレクションベースで以下を検証しています：
- クラス構造
- フィールドの存在と型
- メソッドシグネチャ

**テストできないこと：**
- 実際の `onEnable()` / `onDisable()` 動作
- イベントリスナーの登録確認
- コマンド実行のテスト
- プレイヤーインタラクション

## MockBukkit 導入手順

### 1. 依存関係の追加

`pom.xml` に以下を追加：

```xml
<repositories>
    <!-- 既存のリポジトリに追加 -->
    <repository>
        <id>papermc</id>
        <url>https://repo.papermc.io/repository/maven-public/</url>
    </repository>
</repositories>

<dependencies>
    <!-- 既存の依存関係に追加 -->
    <dependency>
        <groupId>com.github.seeseemelk</groupId>
        <artifactId>MockBukkit-v1.21</artifactId>
        <version>3.93.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2. テストクラスの書き換え

```java
import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.example.rpgplugin.RPGPlugin;
import org.junit.jupiter.api.*;

@DisplayName("RPGPlugin 統合テスト（MockBukkit使用）")
class RPGPluginIntegrationTest {

    private static ServerMock server;
    private static RPGPlugin plugin;

    @BeforeAll
    static void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RPGPlugin.class);
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("プラグインが正常に有効化される")
    void testPluginEnabled() {
        assertThat(plugin.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("全マネージャーが初期化される")
    void testAllManagersInitialized() {
        assertThat(plugin.getStorageManager()).isNotNull();
        assertThat(plugin.getPlayerManager()).isNotNull();
        assertThat(plugin.getStatManager()).isNotNull();
        assertThat(plugin.getClassManager()).isNotNull();
        assertThat(plugin.getSkillManager()).isNotNull();
    }

    @Test
    @DisplayName("プレイヤー参加時にPlayerManagerが処理する")
    void testPlayerJoin() {
        PlayerMock player = server.addPlayer();

        // プレイヤーがPlayerManagerに登録されていることを確認
        assertThat(plugin.getPlayerManager().isPlayerLoaded(player.getUniqueId())).isTrue();
    }

    @Test
    @DisplayName("/rpg コマンドが登録されている")
    void testRpgCommandRegistered() {
        PluginCommand command = plugin.getCommand("rpg");
        assertThat(command).isNotNull();
        assertThat(command.getExecutor()).isNotNull();
    }
}
```

### 3. ファサードシステムのテスト

```java
@Nested
@DisplayName("ファサードシステムテスト")
class FacadeSystemTests {

    @Test
    @DisplayName("CoreSystemManagerが正しく初期化される")
    void testCoreSystemManagerInitialized() {
        assertThat(plugin.getStorageManager()).isNotNull();
        assertThat(plugin.getDependencyManager()).isNotNull();
        assertThat(plugin.getModuleManager()).isNotNull();
        assertThat(plugin.getConfigManager()).isNotNull();
    }

    @Test
    @DisplayName("GameSystemManagerが正しく初期化される")
    void testGameSystemManagerInitialized() {
        assertThat(plugin.getPlayerManager()).isNotNull();
        assertThat(plugin.getStatManager()).isNotNull();
        assertThat(plugin.getSkillManager()).isNotNull();
        assertThat(plugin.getClassManager()).isNotNull();
        assertThat(plugin.getDamageManager()).isNotNull();
    }

    @Test
    @DisplayName("GUIManagerが正しく初期化される")
    void testGUIManagerInitialized() {
        assertThat(plugin.getSkillMenuListener()).isNotNull();
        assertThat(plugin.getClassMenuListener()).isNotNull();
        assertThat(plugin.getCurrencyListener()).isNotNull();
    }

    @Test
    @DisplayName("ExternalSystemManagerが正しく初期化される")
    void testExternalSystemManagerInitialized() {
        assertThat(plugin.getAPI()).isNotNull();
        // MythicMobsは依存関係があるため、nullの可能性あり
    }
}
```

## 注意事項

### バージョン互換性

- MockBukkitのバージョンはMinecraftバージョンに依存します
- 現在のSpigot API `1.21-R0.1-SNAPSHOT` に合わせて `MockBukkit-v1.21` を使用
- Minecraftバージョンを更新する場合はMockBukkitも更新が必要

### 制限事項

1. **外部プラグイン依存**: MythicMobs, Vaultなどはモックが必要
2. **データベース**: SQLiteは実際のファイルを作成するため、テスト用DBが必要
3. **非同期処理**: `runTaskAsynchronously` はテスト環境では同期的に実行される

### 推奨テスト構造

```
src/test/java/
├── com/example/rpgplugin/
│   ├── e2e/                    # 現在のリフレクションテスト
│   │   └── PluginLifecycleTest.java
│   ├── integration/            # MockBukkit使用テスト
│   │   ├── PluginIntegrationTest.java
│   │   └── SystemIntegrationTest.java
│   ├── unit/                   # 単体テスト
│   │   ├── DamageModifierTest.java
│   │   └── StatManagerTest.java
│   └── refactoring/            # リファクタリング検証
│       └── BackwardCompatibilityTest.java
```

## 導入の優先度

| 優先度 | 理由 |
|--------|------|
| **中** | 現在のリフレクションテストで構造は検証できている |
| **高** (将来) | プラグインの複雑化に伴い、実際の動作テストが必要になる |

## 参考リンク

- [MockBukkit GitHub](https://github.com/MockBukkit/MockBukkit)
- [MockBukkit ドキュメント](https://mockbukkit.readthedocs.io/)
- [Paper API](https://docs.papermc.io/paper/dev/api)
