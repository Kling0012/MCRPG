# 各サブシステムの詳細設計

## 1. PlaceholderAPI連携

### プレースホルダー一覧

| プレースホルダー | 説明 |
|----------------|------|
| `%rpg_level%` | レベル |
| `%rpg_stat_STR%` | STR値 |
| `%rpg_stat_INT%` | INT値 |
| `%rpg_stat_SPI%` | SPI値 |
| `%rpg_stat_VIT%` | VIT値 |
| `%rpg_stat_DEX%` | DEX値 |
| `%rpg_available_points%` | 利用可能ステータスポイント |
| `%rpg_class%` | クラスID |
| `%rpg_class_name%` | クラス表示名 |
| `%rpg_class_rank%` | クラスランク |
| `%rpg_max_hp%` | 最大HP |
| `%rpg_max_mana%` | 最大MP |
| `%rpg_mana%` | 現在MP |
| `%rpg_skill_level_<skill>%` | スキルレベル |

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
