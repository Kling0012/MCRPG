# ホットリロード設計

## 最終設定ファイル構造

```
plugins/RPGPlugin/
├── config.yml                    # メイン設定
├── classes/                      # クラス定義（YAML）
│   ├── warrior.yml              # 戦士 Rank1
│   ├── warrior_rank2.yml        # 戦士 Rank2
│   ├── knight_rank2.yml         # 騎士 Rank2（分岐）
│   ├── shieldbearer.yml         # 大盾使い Rank1
│   ├── mage.yml                 # 魔法使い Rank1
│   ├── archer.yml               # 弓使い Rank1
│   └── templates/               # クラステンプレート
│       ├── melee_template.yml
│       └── ranged_template.yml
├── skills/                       # 共通スキルプール（YAML）
│   ├── active/
│   │   ├── power_strike.yml
│   │   ├── fireball.yml
│   │   ├── aimed_shot.yml
│   │   └── shield_bash.yml
│   └── passive/
│       ├── critical_mastery.yml
│       └── mana_regeneration.yml
├── exp/                          # 経験値設定
│   └── diminish_config.yml      # 減衰設定
├── mobs/                         # MythicMobs連携
│   └── mob_drops.yml
└── data/
    └── database.db              # SQLiteデータベース
```

## クラス設定YAML（重要）

```yaml
# classes/warrior.yml - 戦士 Rank1
id: warrior
name: 戦士
display_name: "&c戦士"
description: ["剣と盾で前線を守る近接クラス", "STRとVITが成長しやすい"]
rank: 1
max_level: 50
icon: DIAMOND_SWORD
icon_data: 0  # ダメージ値

# ステータス成長（レベルアップ時）
stat_growth:
  auto:                           # 自動配分
    strength: 2
    vitality: 1
  manual_points: 3                # 手動配分ポイント

# クラスアップ（直線パターン）
next_rank:
  class_id: warrior_rank2         # 次のランク
  requirements:
    - type: level
      value: 20
    - type: stat                  # ステータス要件
      stat: STRENGTH
      value: 50

# クラスアップ（分岐パターン） - Rank2以降
alternative_ranks:                # 複数の上位クラスへ
  - class_id: knight_rank2        # 騎士
    requirements:
      - type: level
        value: 20
      - type: item                # 特殊アイテム
        item: KNIGHT crest
  - class_id: berserker_rank2     # ベルセルク
    requirements:
      - type: level
        value: 20
      - type: quest               # クエスト完了
        quest: berserker_trial

# 使用可能スキル（共通スキルプールから選択）
available_skills:
  - power_strike
  - shield_bash
  - battle_cry

# 固有ボーナス
passive_bonuses:
  - type: damage_multiplier
    value: 1.1                    # +10% ダメージ
  - type: health_bonus
    value: 20                     # +20 HP

# 経験値減衰設定
exp_diminish:
  start_level: 30                 # LV30から減衰
  reduction_rate: 0.5             # 50%カット
```

## スキル設定YAML（重要）

```yaml
# skills/active/power_strike.yml
id: power_strike
name: パワーストライク
display_name: "&6パワーストライク"
type: active
description: ["強力な一撃を放つ", "STRに依存してダメージ増加"]
max_level: 5
cooldown: 8.0                     # 秒
mana_cost: 10

# ダメージ計算
damage:
  base: 50                        # 基本ダメージ
  stat_multiplier:                # ステータス依存
    stat: STRENGTH                # STR依存
    multiplier: 1.5               # STR100でダメージ+150
  level_multiplier: 10            # スキルLV1ごとに+10

# 効果
effects:
  - type: DAMAGE
    value: 50
    multiplier: intelligence      # INTでも補正可能

# スキルツリー設定
skill_tree:
  parent: none                    # 親スキル（なし）
  unlock_requirements:
    - type: level
      value: 5
    - type: stat
      stat: STRENGTH
      value: 20
  cost: 1                         # 習得に必要なスキルポイント
```

## 共通スキルプールの仕組み

```
全スキル定義（skills/ディレクトリ）
    ├─ power_strike.yml    (共通スキル)
    ├─ fireball.yml        (共通スキル)
    └─ shield_bash.yml     (共通スキル)
            ↓
クラス設定（available_skillsで選択）
    ├─ Warrior: [power_strike, shield_bash, battle_cry]
    ├─ Mage: [fireball, ice_spike, teleport]
    └─ Archer: [aimed_shot, multishot, trap]
            ↓
プレイヤーは自分のクラスのスキルのみ習得可能
```

## 経験値減衰設定

```yaml
# exp/diminish_config.yml
global_diminish:
  enabled: true
  start_level: 30                 # クラスごとに上書き可能
  reduction_rate: 0.5             # 50%カット（クラスごとに変更可）

# クラス別減衰設定（オプション）
class_specific:
  warrior:
    start_level: 25
    reduction_rate: 0.4           # 40%カット
  mage:
    start_level: 35
    reduction_rate: 0.6           # 60%カット
```

## FileWatcher実装

```java
public class ConfigWatcher implements Runnable {
    private final WatchService watchService;
    private final Map<WatchKey, Path> watchedDirectories;

    public void startWatching() {
        // classes/ と skills/ ディレクトリを監視
        watchDirectory(classesDir);
        watchDirectory(skillsDir);

        // 非同期で監視開始
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 0L, 20L);
    }

    @Override
    public void run() {
        WatchKey key = watchService.poll();
        if (key != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                Path changedFile = (Path) event.context();
                if (changedFile.toString().endsWith(".yml")) {
                    reloadConfig(changedFile);
                }
            }
            key.reset();
        }
    }
}
```

## リロード戦略

| 設定種別 | リロード方式 |
|---------|-------------|
| クラス・スキル設定 | 即時リロード（プレイヤーへの影響を最小限に） |
| メイン設定 | `/rpg reload` コマンドで手動リロード |
| 減衰設定 | `/rpg reload exp` で個別リロード |
| データベース | リロード不要（リアルタイム更新） |
