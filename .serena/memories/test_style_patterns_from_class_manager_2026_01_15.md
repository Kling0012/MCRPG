# 既存テストスタイル分析

## テンプレート構造

```java
@DisplayName("クラス名 テスト")
@ExtendWith(MockitoExtension.class)
class TargetClassTest {

    @Mock
    private DependencyType mockDependency;

    private TargetClass target;

    @BeforeEach
    void setUp() {
        lenient().when(mockDependency.method()).thenReturn(value);
        target = new TargetClass(mockDependency);
    }

    @AfterEach
    void tearDown() {
        // クリーンアップ
    }

    @Test
    @DisplayName("メソッド: 正常系")
    void testMethod_Normal() {
        // Arrange, Act, Assert
    }

    @Test
    @DisplayName("メソッド: 異常系")
    void testMethod_Exception() {
        // テスト
    }
}
```

## 重要パターン

### 1. MockedStatic使用
- Bukkit.getOnlinePlayers() のモック
- @BeforeEachで作成、@AfterEachでclose

### 2. lenient()使用
- 全テストで使用されないスタブに適用
- 「Mockitoの不必要なスタブ検出」を回避

### 3. Optionalテスト
- assertTrue(result.isPresent())
- assertFalse(result.isPresent())

### 4. コレクション不変性テスト
- コピーが返されることを確認
- 返されたコレクションを変更しても元に影響しない

### 5. 検証パターン
- verify(mock).method()
- verify(mock, never()).method()
- verifyNoInteractions(mock)

## ClassManagerTestから学んだベストプラクティス

### テストケース網羅性
1. 正常系: 基本的な成功パス
2. null引数: すべてのpublicメソッド
3. 境界値: 0, MAX_VALUE
4. 重複/上書き: データの一意性
5. コピーの不変性: セッター/ゲッター
6. エラーハンドリング: 例外、未存在

### モック設定
- Player: UUID, Name, Level
- RPGPlayer: ClassId, ClassHistory
- Bukkit: getOnlinePlayers

### アサーション
- assertEquals(期待値, 実測値, メッセージ)
- assertTrue/assertFalse(条件, メッセージ)
- 日本語メッセージで説明を追加

### ネストされたクラスのテスト
- ClassUpResultなどの内部クラス
- ゲッター、toString、フィールドアクセス
