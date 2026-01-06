# Phase10-1: 経験値減衰システム 実装報告

## 実装概要

バニラ経験値システムを使用し、減衰設定とクラス別上限を実装しました。

## 実装内容

### 1. 新規ファイル作成

#### ExpDiminisher.java
**パス:** `src/main/java/com/example/rpgplugin/player/ExpDiminisher.java`

**機能:**
- 経験値減衰の適用ロジックを管理
- クラス別減衰設定の適用
- 除外条件の判定（プレイヤーキル、ボスモブ、イベント報酬）
- デバッグログ出力機能

**設計パターン:**
- ストラテジーパターン: クラスごとの減衰設定を適用
- ファサードパターン: 減衰計算への統一インターフェース

**主要メソッド:**
```java
// 経験値イベントに減衰を適用
public void applyDiminishment(PlayerExpChangeEvent event)

// 減衰後の経験値を計算
public int calculateDiminishedExp(Player player, int originalExp)

// 減衰率を取得
public double getDiminishRate(Player player)

// 減衰開始レベルを取得
public int getDiminishStartLevel(Player player)
```

### 2. 既存ファイルの修正

#### VanillaExpHandler.java
**変更内容:**
1. ExpDiminisherのフィールドを追加
2. コンストラクタにExpDiminisherパラメータを追加
3. `onPlayerExpChange()`メソッドで減衰を適用

```java
// 経験値減衰を適用
expDiminisher.applyDiminishment(event);
```

#### RPGPlugin.java
**変更内容:**
1. ExpDiminisherのインポート
2. ExpDiminisherフィールドの追加
3. `setupVanillaExpHandler()`メソッドでExpDiminisherを初期化
4. `getExpDiminisher()`ゲッターメソッドの追加

```java
// 経験値減衰マネージャーの初期化
expDiminisher = new ExpDiminisher(this, playerManager, classManager);

// バニラ経験値ハンドラーの初期化
vanillaExpHandler = new VanillaExpHandler(this, playerManager, expDiminisher);
```

### 3. 設定ファイルの更新

#### config.yml
**追加したセクション:**

```yaml
# 経験値減衰
exp_diminish:
  enabled: true
  start_level: 30
  reduction_rate: 0.5
  reduction_increment: 0.01

  # 減衰除外設定
  exemptions:
    player_kills: true
    boss_mobs: true
    event_rewards: true

# デバッグ設定
debug:
  log_exp_changes: false
  log_exp_diminish: false

level_up:
  show_title: true
  play_sound: true
```

### 4. クラス設定ファイル（既に実装済み）

すべての初期クラスに `exp_diminish` 設定が追加されています:

- **warrior.yml**: start_level: 30, reduction_rate: 0.5
- **mage.yml**: start_level: 30, reduction_rate: 0.5
- **archer.yml**: start_level: 30, reduction_rate: 0.5
- **shieldbearer.yml**: start_level: 30, reduction_rate: 0.5

## 既存実装の確認

以下の機能は既に実装されていました:

1. ✅ **RPGClass.ExpDiminish** - 減衰設定クラス（RPGClass.java:203-242行目）
2. ✅ **ExpDiminish.applyExp()** - 減衰計算メソッド（RPGClass.java:236-241行目）
3. ✅ **diminish_config.yml** - グローバル減衰設定
4. ✅ **ClassLoader** - exp_diminishセクションのパース処理（ClassLoader.java:169-174行目）

## アーキテクチャ

### 設計原則の適用

**SOLID原則:**
- **S (Single Responsibility):** ExpDiminisherは経験値減衰にのみ特化
- **O (Open/Closed):** クラス設定で減衰パラメータを拡張可能
- **D (Dependency Inversion):** RPGClass.ExpDiminishインターフェースに依存

**DRY (Don't Repeat Yourself):**
- 減衰ロジックをExpDiminisherに一元管理
- RPGClass.ExpDiminishで統一的な減衰計算

**KISS (Keep It Simple, Stupid):**
- シンプルな減衰計算式: `exp * (1 - reductionRate)`
- 明確な減衰フロー

### データフロー

```
PlayerExpChangeEvent
    ↓
VanillaExpHandler.onPlayerExpChange()
    ↓
ExpDiminisher.applyDiminishment()
    ↓
1. 減衰機能が有効か確認
2. 除外条件のチェック
3. クラス設定を取得
4. 減衰計算 (RPGClass.ExpDiminish.applyExp)
5. event.setAmount() で適用
```

## コンパイル確認

ExpDiminisherとVanillaExpHandlerに関連するコンパイルエラーはありません。
（既存のコンパイルエラーはPhase10-1の範囲外の既存の問題です）

## テスト項目

### 機能テスト
1. [ ] 減衰機能の有効/無効切り替え
2. [ ] レベル30未満では減衰なし
3. [ ] レベル30以上で減衰適用（デフォルト50%）
4. [ ] クラス別減衰設定の適用
5. [ ] 除外条件の動作確認

### 設定テスト
1. [ ] config.ymlのexp_diminish設定変更
2. [ ] debug.log_exp_diminishでのログ出力
3. [ ] クラスYAMLのexp_diminish設定変更
4. [ ] ホットリロードでの設定反映

### 統合テスト
1. [ ] プラグイン起動時の初期化
2. [ ] 経験値取得時の減衰適用
3. [ ] レベルアップ時の動作
4. [ ] 複数プレイヤー同時プレイ

## 今後の拡張案

1. **除外条件の実装**
   - プレイヤーキル判定
   - ボスモブ判定
   - イベント報酬判定

2. **高度な減衰アルゴリズム**
   - レベルごとの減衰率増加
   - モブ種類別減衰テーブル
   - 時間帯ボーナス

3. **UI/UX改善**
   - 減衰率表示コマンド
   - 次レベルまでの所要時間予測
   - 減衰なしの経験値ソースの表示

## 設計上の懸念点

1. **除外条件の未実装**
   - 現在は常にfalseを返すプレースホルダー
   - TODOコメントとしてマーク済み

2. **減衰テーブルの未活用**
   - diminish_config.ymlに減衰テーブル定義
   - 現在はクラス設定のみ使用

## まとめ

Phase10-1の経験値減衰システムの中核機能を実装しました。以下が完了しました:

✅ ExpDiminisherクラスの作成
✅ VanillaExpHandlerの統合
✅ RPGPluginの初期化ロジック修正
✅ 設定ファイルの更新
✅ すべての初期クラスにexp_diminish設定追加

**実装完了日:** 2026-01-06
**ステータス:** 実装完了、テスト待ち
**優先度:** 低
**見積時間:** 2-3日（完了）
