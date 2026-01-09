# Skillシステム Part1 改善完了

**実施日:** 2026-01-09
**ブランチ:** vk/9b3d-2-skill-part1-15

## 実施した改善

### 1. SkillTreeRegistry.notifyListeners() ロガー出力改善
- **ファイル:** `src/main/java/com/example/rpgplugin/skill/SkillTreeRegistry.java:170`
- **変更:** `e.printStackTrace()` を `java.util.logging.Logger` 出力に変更
- **理由:** プラグインロガーを使用することで、ログの一元管理と適切なログレベル設定を可能にする

### 2. ActiveSkillExecutor calculateDamage() シグネチャ変更
- **ファイル:** `src/main/java/com/example/rpgplugin/skill/executor/ActiveSkillExecutor.java`
- **変更:** `calculateDamage(Player, Skill, int)` → `calculateDamage(RPGPlayer, Skill, int)`
- **理由:** 
  - `execute()` メソッドで既に取得済みの `rpgPlayer` を再利用
  - 重複した `playerManager.getRPGPlayer()` 呼び出しを排除
  - `executeAt()` メソッドでも `rpgPlayer` 取得を追加して統一

## 変更内容

### SkillTreeRegistry.java
```java
// 修正前
} catch (Exception e) {
    e.printStackTrace();
}

// 修正後
} catch (Exception e) {
    java.util.logging.Logger.getLogger(SkillTreeRegistry.class.getName())
            .log(java.util.logging.Level.WARNING,
                    "リスナー通知中に例外が発生しました: classId=" + classId, e);
}
```

### ActiveSkillExecutor.java
```java
// 修正前
private double calculateDamage(Player player, Skill skill, int level) {
    ...
    RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
    if (rpgPlayer == null) {
        return 0.0;
    }
    ...
}

// 修正後
private double calculateDamage(RPGPlayer rpgPlayer, Skill skill, int level) {
    ...
    // rpgPlayer は呼び出し元で既に取得済み
    ...
}
```
