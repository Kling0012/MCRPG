# Stats + Player パッケージ レビュー結果

## 日付
2026-01-09

## ブランチ
vk/15d7-7-stats-player-1

## レビュー対象ファイル
1. src/main/java/com/example/rpgplugin/stats/Stat.java
2. src/main/java/com/example/rpgplugin/stats/StatManager.java
3. src/main/java/com/example/rpgplugin/stats/StatModifier.java
4. src/main/java/com/example/rpgplugin/stats/PlayerStats.java
5. src/main/java/com/example/rpgplugin/stats/ManaManager.java
6. src/main/java/com/example/rpgplugin/stats/calculator/StatCalculator.java
7. src/main/java/com/example/rpgplugin/player/RPGPlayer.java
8. src/main/java/com/example/rpgplugin/player/PlayerManager.java
9. src/main/java/com/example/rpgplugin/player/ManaManager.java
10. src/main/java/com/example/rpgplugin/player/exp/ExpManager.java
11. src/main/java/com/example/rpgplugin/player/VanillaExpHandler.java

## 評価: A (90/100)

### 優れている点
- StatManager: stat, modifier, modifierIdの適切なnullチェック
- StatCalculator: StatManagerのnullチェック
- PlayerManager: UUIDのnullチェック（loadPlayer, savePlayer, unloadPlayer）
- RPGPlayer.hasSkill(): skillManagerとplayerのnull安全確認
- 境界値チェック: 回避率上限75%、クリティカル率上限50%
- イベントハンドラー: try-catchによる例外処理

### 改善推奨事項

#### PlayerStats.allocatePoint() - statのnullチェック不足
```java
// 現状 (src/main/java/com/example/rpgplugin/stats/PlayerStats.java:91)
public int allocatePoint(Stat stat, int points) {
    if (points <= 0) {
        return 0;
    }
    // statがnullの場合、getBaseStat(stat)でNullPointerException
    int currentValue = getBaseStat(stat);
    // ...
}

// 推奨修正
public int allocatePoint(Stat stat, int points) {
    if (stat == null) {
        throw new IllegalArgumentException("Stat cannot be null");
    }
    if (points <= 0) {
        return 0;
    }
    // ...
}
```

### チェック項目結果
- ステータス計算時のNullチェック: ✅ 良好
- プレイヤーデータ取得時の例外処理: ✅ 良好
- 経験値計算の境界チェック: ✅ 良好
