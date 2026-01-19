# レビュー10: Trade + Currency (9ファイル) - Nullチェック/例外処理

## 作業日時
2026-01-09

## 作業ブランチ
vk/5c5f-10-trade-currenc

## レビュー対象ファイル
- src/main/java/com/example/rpgplugin/trade/TradeManager.java
- src/main/java/com/example/rpgplugin/trade/TradeSession.java
- src/main/java/com/example/rpgplugin/trade/TradeInventory.java
- src/main/java/com/example/rpgplugin/trade/TradeMenuListener.java
- src/main/java/com/example/rpgplugin/trade/model/TradeOffer.java
- src/main/java/com/example/rpgplugin/trade/model/TradeParty.java
- src/main/java/com/example/rpgplugin/trade/repository/TradeHistoryRepository.java
- src/main/java/com/example/rpgplugin/currency/CurrencyManager.java
- src/main/java/com/example/rpgplugin/currency/CurrencyListener.java
- src/main/java/com/example/rpgplugin/core/module/IModule.java

## 重要な問題点

### 高優先度

1. **TradeManager.java:exchangeGold()** - CurrencyManagerがnullの場合、ゴールド交換がスキップされるがセッションは完了状態になる
   - 推奨: 例外をスローしてトレードを中止

2. **CurrencyManager.java:depositGold/withdrawGold/transferGold()** - loadPlayerCurrency()例外後のnullチェック不足
   - 推奨: 例外ハンドリングとnullチェックを追加

### 中優先度

3. **TradeParty.java:getPlayer()** - nullを返す可能性があるがJavaDocに明記されていない
   - 推奨: JavaDocに明記またはOptional化

### 良好な実装（参考）

- TradeOffer.addItem() - 引数のnullチェック
- TradeParty.isOnline() - フィールドのnullチェック
- TradeHistoryRepository.save() - try-with-resourcesと例外処理
- TradeInventoryコンストラクタ - orElseThrowによる明示的例外

## 評価サマリー

| ファイル | 状態 |
|---------|------|
| TradeManager.java | ⚠️ 要改善 |
| TradeSession.java | ✅ 良好 |
| TradeInventory.java | ✅ 良好 |
| TradeMenuListener.java | ✅ 良好 |
| TradeOffer.java | ✅ 良好 |
| TradeParty.java | ⚠️ 要改善 |
| TradeHistoryRepository.java | ✅ 良好 |
| CurrencyManager.java | ⚠️ 要改善 |
| CurrencyListener.java | ✅ 良好 |
| IModule.java | ✅ 良好 |
