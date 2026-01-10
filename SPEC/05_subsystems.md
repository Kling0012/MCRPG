# 各サブシステムの詳細設計

## 1. GUIシステム（重要）

### ステータス振りGUI
```
┌─────────────────────────────────┐
│         ステータス配分             │
├─────────────────────────────────┤
│  STR: 20  [+][-]  (自動+10)      │
│  INT: 15  [+][-]  (自動+5)       │
│  SPI: 10  [+][-]  (自動+5)       │
│  VIT: 25  [+][-]  (自動+10)      │
│  DEX: 12  [+][-]  (自動+2)       │
├─────────────────────────────────┤
│  残りポイント: 8                  │
├─────────────────────────────────┤
│          [確認して閉じる]          │
└─────────────────────────────────┘
```

### クリック操作
- 左クリック: +1ポイント
- 右クリック: -1ポイント
- Shift+左クリック: +10ポイント
- Shift+右クリック: -10ポイント

### スキルツリーGUI
```
┌─────────────────────────────────┐
│          スキルツリー              │
├─────────────────────────────────┤
│ [パワーストライク] ← [バッシュ]   │
│      ↓                           │
│  [ウェポンマスタリー]              │
├─────────────────────────────────┤
│  スキルポイント: 5                │
├─────────────────────────────────┤
│  習得可能スキル:                  │
│  [シールドバッシュ] [バトルクライ] │
└─────────────────────────────────┘
```

---

## 2. 外部API設計（重要）

### メインAPIインターフェース
```java
public interface RPGPluginAPI {
    // プレイヤーデータ取得
    RPGPlayer getRPGPlayer(Player player);
    int getLevel(Player player);
    int getStat(Player player, Stat stat);
    String getClassId(Player player);

    // ステータス操作
    void setStat(Player player, Stat stat, int baseValue);
    void addStatPoints(Player player, int points);

    // クラス操作
    void setClass(Player player, String classId);
    void upgradeClassRank(Player player);
    boolean canUpgradeClass(Player player);

    // スキル操作
    boolean hasSkill(Player player, String skillId);
    void unlockSkill(Player player, String skillId);
    void castSkill(Player player, String skillId);
    int getSkillLevel(Player player, String skillId);

    // ダメージ計算
    double calculateDamage(Player attacker, Entity target);
    double applyStatModifiers(Player player, double baseDamage, Stat stat);
}
```

---

## 3. 経験値システム（バニラ統合）

### バニラLV/EXP使用
```java
public class VanillaExpHandler {
    private final ExpDiminisher diminisher;

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        RPGClass rpgClass = plugin.getRPGPlayer(player).getRPGClass();

        // 減衰適用
        int originalAmount = event.getAmount();
        int diminishedAmount = diminisher.applyDiminish(player, rpgClass, originalAmount);

        if (diminishedAmount < originalAmount) {
            event.setAmount(diminishedAmount);
            player.sendMessage(String.format(
                "§e経験値減衰: %d → %d (%.0f%%)",
                originalAmount,
                diminishedAmount,
                rpgClass.getDiminishRate() * 100
            ));
        }
    }
}
```

### 減衰計算
```java
public class ExpDiminisher {
    public int applyDiminish(Player player, RPGClass rpgClass, int exp) {
        int level = player.getLevel();

        // 減衰開始レベル未満ならそのまま
        if (level < rpgClass.getDiminishStartLevel()) {
            return exp;
        }

        // 減衰率を適用
        double reductionRate = rpgClass.getDiminishRate();
        return (int) (exp * (1 - reductionRate));
    }
}
```
