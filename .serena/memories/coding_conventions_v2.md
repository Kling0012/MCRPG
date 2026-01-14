# RPGPlugin コーディング規約 v2.0

> 最終更新: 2025-01-13  
> 適用: Java 21, Maven, Paper API 1.20.6

---

## 1. 基本規約

| 項目 | 規定 | 備考 |
|------|------|------|
| **インデント** | スペース4つ | タブ禁止（Java標準） |
| **エンコーディング** | UTF-8 | BOMなし |
| **行長** | 最大120文字 | 推奨100文字 |
| **ファイル末尾** | 改行あり | |
| **中括弧** | K&Rスタイル | 開き中括弧は行末 |

```java
// 正しい例
public class Example {
    public void method() {
        if (condition) {
            doSomething();
        }
    }
}
```

---

## 2. 命名規則

| 種別 | 規則 | 例 |
|------|------|------|
| **クラス** | PascalCase | `SkillManager`, `StatManager` |
| **インターフェース** | PascalCase | `IRepository`, `Executable` |
| **メソッド** | camelCase | `calculateDamage()`, `getBaseStat()` |
| **変数** | camelCase | `baseDamage`, `finalDamage` |
| **定数** | UPPER_SNAKE_CASE | `DEFAULT_CRITICAL_RATE` |
| **Enum定数** | UPPER_SNAKE_CASE | `STRENGTH`, `INTELLIGENCE` |
| **パッケージ** | 小文字 | `com.example.rpgplugin.skill` |
| **privateフィールド** | camelCase | `playerManager`, `formulaEvaluator` |
| **ローカル変数** | camelCase | `result`, `skillId` |

### 命名ガイドライン

```java
// クラス: 名詞、機能を表す
public class DamageManager { }
public interface SkillExecutor { }

// メソッド: 動詞、何をするか
public void calculateDamage() { }
public int getFinalStat() { }
public boolean hasSkill() { }

// boolean: is/has/can/should prefixes
public boolean isActive() { }
public boolean hasPermission() { }

// コレクション: 複数形
private final Map<String, Skill> skills;
private final List<StatModifier> modifiers;
```

---

## 3. ドキュメント規約

### 3.1 Javadoc（必須）

```java
/**
 * スキル管理クラス
 *
 * <p>全スキルの登録・取得、プレイヤーの習得スキル管理を行います。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: スキル管理に専念</li>
 *   <li>DRY: スキルアクセスロジックを一元管理</li>
 *   <li>Strategy: 異なるスキルタイプの実行をStrategyパターンで管理</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 * @see Skill
 * @see SkillExecutor
 */
public class SkillManager {
    
    /**
     * 指定したスキルを取得します
     *
     * @param skillId スキルID
     * @return スキルインスタンス。存在しない場合はnull
     * @throws IllegalArgumentException skillIdがnullの場合
     */
    public Skill getSkill(String skillId) {
        // ...
    }
}
```

### 3.2 コメント規約

- **言語**: 日本語
- **パブリックAPI**: Javadoc必須
- **複雑なロジック**: 説明コメントを追加
- **TODO/FIXME**: `// TODO: 説明` 形式

```java
// ==================== セクション区切り ====================

// 単一行コメント：説明を簡潔に
if (stat == null) {
    throw new IllegalArgumentException("Stat cannot be null");
}

// 複雑なロジックには説明を追加
// 期限切れの修正値を削除（スレッド安全性のためイテレータ使用）
cleanupExpiredModifiers(stat);
```

---

## 4. アノテーション規約

| アノテーション | 使用箇所 |
|--------------|----------|
| `@Override` | オーバーライドメソッド（必須） |
| `@EventHandler` | Bukkitイベントハンドラー（必須） |
| `@FunctionalInterface` | 関数型インターフェース |
| `@NotNull` / `@Nullable` | Null許容を明示 |
| `@Deprecated` | 非推奨API |

```java
@Override
public void onEnable() {
    // ...
}

@EventHandler(priority = EventPriority.HIGH)
public void onPlayerJoin(PlayerJoinEvent event) {
    // ...
}
```

---

## 5. エラーハンドリング規約

### 5.1 例外処理

```java
// パラメータ検証
public void setBaseStat(Stat stat, int value) {
    if (stat == null) {
        throw new IllegalArgumentException("Stat cannot be null");
    }
    if (value < 0) {
        throw new IllegalArgumentException("Base stat value cannot be negative: " + value);
    }
    baseStats.put(stat, value);
}

// オプショナルの活用
public Optional<Skill> getSkill(String skillId) {
    return Optional.ofNullable(skills.get(skillId));
}
```

### 5.2 Null安全

```java
// 優先: Optionalを使用
public Optional<RPGPlayer> getPlayer(UUID uuid) {
    return Optional.ofNullable(players.get(uuid));
}

// 次善: nullチェック
public void processSkill(String skillId) {
    if (skillId == null || skillId.isEmpty()) {
        return;
    }
    // ...
}
```

---

## 6. スレッドセーフティ規約

| 目的 | 推奨クラス |
|------|-----------|
| スレッドセーフMap | `ConcurrentHashMap` |
| スレッドセーフList | `CopyOnWriteArrayList` |
| スレッドセーフSet | `ConcurrentHashMap.newKeySet()` |
| カウンター | `AtomicInteger`, `AtomicLong` |
| 参照 | `AtomicReference` |

```java
// スレッドセーフなコレクション初期化
private final Map<Stat, Integer> baseStats = new ConcurrentHashMap<>();
private final List<StatModifier> modifiers = new CopyOnWriteArrayList<>();
private final AtomicInteger availablePoints = new AtomicInteger(0);

// 非同期処理
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
    // データベース操作など
});

// 同期処理
Bukkit.getScheduler().runTask(plugin, () -> {
    // メインスレッドで実行
});
```

---

## 7. Spigot/Bukkit 固有規約

### 7.1 イベント処理

```java
// イベント優先度の使い分け
@EventHandler(priority = EventPriority.LOWEST)      // キャンセル目的
@EventHandler(priority = EventPriority.LOW)         // 早期処理
@EventHandler(priority = EventPriority.NORMAL)      // 標準
@EventHandler(priority = EventPriority.HIGH)        // 重要な変更
@EventHandler(priority = EventPriority.HIGHEST)     // 変更の監視
@EventHandler(priority = EventPriority.MONITOR)     // ログ目的のみ

// イベントキャンセル
event.setCancelled(true);
```

### 7.2 スケジューラー使い分け

```java
// 即時実行（メインスレッド）
Bukkit.getScheduler().runTask(plugin, () -> { });

// 遅延実行
Bukkit.getScheduler().runTaskLater(plugin, () -> { }, ticks);

// 定期実行
Bukkit.getScheduler().runTaskTimer(plugin, () -> { }, delay, period);

// 非同期
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> { });
```

---

## 8. ログ出力規約

| レベル | 使用場面 | 例 |
|--------|----------|-----|
| `SEVERE` | エラー、致命的問題 | `logger.severe("Failed to save player data")` |
| `WARNING` | 警告、回復可能問題 | `logger.warning("Config file not found, using defaults")` |
| `INFO` | 一般情報 | `logger.info("RPGPlugin enabled successfully")` |
| `FINE` | デバッグ情報 | `logger.fine("Skill cast: " + skillId)` |

```java
// システムプレフィックス付き
logger.info("[Damage] Player->Zombie: Base=100, STR=50, Final=150");
logger.warning("[Skill] Cooldown not ready: " + skillId);
logger.severe("[Database] Connection failed: " + e.getMessage());
```

---

## 9. 設計原則（SOLID + KISS + DRY + YAGNI）

### SOLID

- **S (Single Responsibility)**: クラスは1つの責務のみ持つ
- **O (Open/Closed)**: 拡張には開か、修正には閉じる
- **L (Liskov Substitution)**: 子クラスは親クラスを置換可能
- **I (Interface Segregation)**: インターフェースは最小限に
- **D (Dependency Inversion)**: 具象クラスでなく抽象に依存

### その他

- **KISS**: シンプルに保つ
- **DRY**: 重複を排除
- **YAGNI**: 必要な機能のみ実装

---

## 10. デザインパターン使用ガイド

| パターン | 使用箇所 |
|----------|----------|
| **ファサード** | RPGPlugin.java（統合API） |
| **ストラテジー** | スキル実行、ダメージ計算の切り替え |
| **オブザーバー** | イベント駆動システム連携 |
| **リポジトリ** | データアクセス抽象化 |
| **ビルダー** | 複雑なオブジェクト構築 |
| **プロキシ** | キャッシュ層（3層: L1/L2/L3） |
| **シングルトン** | マネージャークラス（慎重に） |
| **ファクトリー** | スキル、エフェクト生成 |

---

## 11. インポート規約

```java
// 1. 標準ライブラリ（アルファベット順）
import java.util.List;
import java.util.Map;

// 2. サードパーティ（アルファベット順）
import com.google.common.collect.ImmutableList;

// 3. プロジェクト内（アルファベット順）
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.stats.Stat;

// 4. staticインポート（最後）
import static java.util.Objects.requireNonNull;
```

---

## 12. フォーマットチェック

### Mavenで確認

```bash
# ビルドとテスト
mvn clean build

# カバレッジレポート
mvn jacoco:report

# レポート確認
cat target/site/jacoco/index.html
```

### カバレッジ基準

- **ラインカバレッジ**: 60%以上必須
- **テスト規約**: JUnit 5 + Mockito + AssertJ

---

## 付録: チェックリスト

- [ ] インデントはスペース4つ
- [ ] クラス/メソッドにJavadocがある
- [ ] @Overrideが適切に使われている
- [ ] nullチェックがある
- [ ] スレッドセーフなコレクションを使っている
- [ ] ログレベルが適切
- [ ] 命名規則に従っている
- [ ] 設計原則（SOLID）を守っている
