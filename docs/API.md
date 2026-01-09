# RPGPlugin API リファレンス

> **作業ブランチ**: main
> **最終更新**: 2026-01-09
> **バージョン**: 1.0.0

---

## 目次

1. [概要](#概要)
2. [APIの取得](#apiの取得)
3. [プレイヤーデータ取得](#プレイヤーデータ取得)
4. [ステータス操作](#ステータス操作)
5. [クラス操作](#クラス操作)
6. [スキル操作](#スキル操作)
7. [経済操作](#経済操作)
8. [ダメージ計算](#ダメージ計算)
9. [ターゲット管理](#ターゲット管理)
10. [スキルトリガー](#スキルトリガー)

---

## 概要

RPGPlugin API は、SKript、Denizen等の外部プラグインからRPGシステムにアクセスするためのインターフェースを提供します。

### 設計原則

- **SOLID-O**: 機能拡張に対して開かれている
- **SOLID-I**: インターフェース分離の原則（最小限のメソッド群）
- **KISS**: シンプルで直感的なAPI

### 基本使用例

```java
// APIの取得
RPGPluginAPI api = RPGPlugin.getInstance().getAPI();

// レベル取得
int level = api.getLevel(player);

// スキル発動
api.castSkill(player, "fireball");
```

---

## APIの取得

### Javaからの場合

```java
// メインクラスから直接取得
RPGPluginAPI api = RPGPlugin.getInstance().getAPI();

// または実装クラスを直接取得
RPGPluginAPIImpl api = RPGPlugin.getInstance().getApiImpl();
```

### SKriptからの場合

```skript
# SKriptBridgeを経由してアクセス
set {_api} to rpg plugin api

# プレイヤーレベル取得
set {_level} to rpg level of player

# スキル発動
rpg cast "fireball" for player
```

#### SKriptアクション一覧

##### レベル操作

```skript
# レベル取得
execute player command "rpg api get_level %player%"

# レベル設定
execute player command "rpg api set_level %player% 20"
```

##### ステータス操作

```skript
# ステータス取得 (STR, INT, SPI, VIT, DEX)
execute player command "rpg api get_stat %player% STR"

# ステータス設定
execute player command "rpg api set_stat %player% STR 50"
```

##### クラス操作

```skript
# クラス取得
execute player command "rpg api get_class %player%"

# クラス設定
execute player command "rpg api set_class %player% warrior"

# クラスアップ
execute player command "rpg api upgrade_class %player%"

# クラスアップ可能か確認
execute player command "rpg api can_upgrade_class %player%"
```

##### スキル操作

```skript
# スキル習得確認
execute player command "rpg api has_skill %player% fireball"

# スキル習得
execute player command "rpg api unlock_skill %player% fireball"

# スキル使用
execute player command "rpg api cast_skill %player% fireball"

# スキルレベル取得
execute player command "rpg api get_skill_level %player% fireball"

# ターゲット指定スキル使用
execute player command "rpg api cast_at %player% fireball %target%"
```

##### 経済操作

```skript
# ゴールド残高取得
execute player command "rpg api get_gold %player%"

# ゴールド付与
execute player command "rpg api give_gold %player% 100"

# ゴールド剥奪
execute player command "rpg api take_gold %player% 50"

# ゴールド所持確認
execute player command "rpg api has_gold %player% 100"

# ゴールド転送
execute player command "rpg api transfer_gold %player% target_player 100"
```

##### ターゲット・範囲操作

```skript
# 範囲内のエンティティ取得
execute player command "rpg api get_entities_in_area %player% sphere 5"

# ターゲット設定
execute player command "rpg api set_target %player% %target%"
```

#### SKriptサンプル

##### クエスト報酬システム

```skript
command /questcomplete <text>:
    trigger:
        if {_text} is "tutorial":
            execute player command "rpg api give_gold %player% 100"
            execute player command "rpg api unlock_skill %player% power_strike"
            send "&aクエスト完了！100Gとスキルを獲得しました！"

command /dailyreward:
    trigger:
            execute player command "rpg api give_gold %player% 500"
            execute player command "rpg api set_stat %player% INT 100"
            send "&aデイリーボーナスを受け取りました！"
        else:
            send "&c今日は既に受け取り済みです。"
```

##### PvP報酬システム

```skript
on death:
    attacker is a player
    victim is a player
    execute player command "rpg api give_gold %attacker% 100"
    send "&a撃破報酬: 100Gを獲得！" to attacker
```

##### レベルボーナス

```skript
on levelup:
    if player's level is 10:
        execute player command "rpg api give_gold %player% 100"
        send "&aレベル10ボーナス: 100Gを獲得！"
    else if player's level is 20:
        execute player command "rpg api give_gold %player% 200"
        execute player command "rpg api unlock_skill %player% power_strike"
        send "&aレベル20ボーナス: 200Gとスキルを獲得！"
```

### Denizenからの場合

```
# DenizenBridgeを経由してアクセス
- ^rpg api [player] level
- ^rpg cast [player] skill:fireball
```

#### Denizenタグ置換

```denizen
# レベル取得
define level <player.tag[rpg.level]>
narrate "あなたのLV: %level%"

# ステータス取得
define str <player.tag[rpg.stat[STR]]>
narrate "STR: %str%"

# クラス取得
define class <player.tag[rpg.class]>
narrate "クラス: %class%"

# ゴールド取得
define gold <player.tag[rpg.gold]>
narrate "所持金: %gold% G"
```

#### Denizen利用可能なタグ

| タグ | 説明 | 例 |
|------|------|-----|
| `<player.tag[rpg.level]>` | レベル | `15` |
| `<player.tag[rpg.stat[<stat>]]>` | ステータス | `<player.tag[rpg.stat[STR]]>` |
| `<player.tag[rpg.stats]>` | 全ステータス | マップ形式 |
| `<player.tag[rpg.class]>` | クラスID | `"warrior"` |
| `<player.tag[rpg.class_rank]>` | クラスランク | `2` |
| `<player.tag[rpg.has_skill[<id>]]>` | スキル習得確認 | `"true"` または `"false"` |
| `<player.tag[rpg.skill_level[<id>]]>` | スキルレベル | `3` |
| `<player.tag[rpg.skill_count]>` | 習得済みスキル数 | `5` |
| `<player.tag[rpg.gold]>` | ゴールド残高 | `100.5` |
| `<player.tag[rpg.has_gold[<amount>]]>` | ゴールド所持確認 | `"true"` または `"false"` |
| `<player.tag[rpg.available_points]>` | 利用可能ステータスポイント | `10` |

#### Denizenコマンド実行

```denizen
# レベル設定
execute as_server "rpg api set_level %player% 20"

# クラス設定
execute as_server "rpg api set_class %player% warrior"

# ゴールド付与
execute as_server "rpg api give_gold %player% 100"
```

#### Denizenサンプル

##### クエスト報酬システム

```denizen
quest_complete_command:
  type: task
  script:
    - define questId <c.args.get[1]>
    - if %questId% == tutorial:
        - execute as_server "rpg api give_gold %player% 100"
        - execute as_server "rpg api unlock_skill %player% power_strike"
        - narrate "<green>クエスト完了！100Gとスキルを獲得しました！"
```

##### レベルアップボーナス

```denizen
level_up_bonus:
  type: world
  events:
    - player levels up
  script:
    - define level <context.new_level>
    - switch %level%:
        - case 10:
            - execute as_server "rpg api give_gold %player% 100"
            - narrate "<gray>レベル10ボーナス: 100Gを獲得！" context:%player%
        - case 20:
            - execute as_server "rpg api give_gold %player% 200"
            - execute as_server "rpg api unlock_skill %player% power_strike"
            - narrate "<gray>レベル20ボーナス: 200Gとスキルを獲得！" context:%player%
```

##### PvP報酬システム

```denizen
pvp_kill_reward:
  type: world
  events:
    - player killed by player
  script:
    # キラーに報酬
    - execute as_server "rpg api give_gold %context.killer% 100"
    - narrate "<green>プレイヤーを撃破！100Gを獲得！" context:%context.killer%

    # 被害者にペナルティ
    - define gold <context.victim.tag[rpg.gold]>
    - define penalty <math.mul[%gold%, 0.05]>
    - execute as_server "rpg api take_gold %context.victim% %penalty%"
```

---

## Phase11 新機能

### Phase11-4: ターゲットパッケージ

新しいターゲットタイプが追加されました：

| ターゲットタイプ | 説明 | パラメータ |
|-----------------|------|-----------|
| `cone` | コーン状範囲 | `angle`, `range` |
| `sphere` | 球形範囲 | `radius`, `max_targets` |
| `sector` | 扇形範囲 | `angle`, `radius` |
| `self` | 自分自身 | なし |
| `area` | 指定座標範囲 | `shape`, `radius` |

### Phase11-5: 外部API拡張

新しいAPIメソッドが追加されました：

```java
// スキル実行（ターゲット指定）
boolean castSkill(Player caster, String skillId, Entity target);

// スキル実行（座標指定）
boolean castSkillAtLocation(Player caster, String skillId, Location location);

// スキル実行（ターゲットリスト指定）
boolean castSkillWithTargets(Player caster, String skillId, List<Entity> targets);

// ターゲット取得
List<Entity> getTargetsInArea(Player caster, TargetType type, AreaShape shape);
```

---

## APIコマンド

### 基本構文

```bash
/rpg api <action> <args...>
```

### アクション一覧

| アクション | 説明 | 構文 |
|-----------|------|------|
| `get_level` | レベル取得 | `rpg api get_level <player>` |
| `set_level` | レベル設定 | `rpg api set_level <player> <level>` |
| `get_stat` | ステータス取得 | `rpg api get_stat <player> <stat>` |
| `set_stat` | ステータス設定 | `rpg api set_stat <player> <stat> <value>` |
| `get_class` | クラス取得 | `rpg api get_class <player>` |
| `set_class` | クラス設定 | `rpg api set_class <player> <class_id>` |
| `upgrade_class` | クラスアップ | `rpg api upgrade_class <player>` |
| `can_upgrade_class` | クラスアップ可否 | `rpg api can_upgrade_class <player>` |
| `has_skill` | スキル習得確認 | `rpg api has_skill <player> <skill_id>` |
| `unlock_skill` | スキル習得 | `rpg api unlock_skill <player> <skill_id>` |
| `cast_skill` | スキル発動 | `rpg api cast_skill <player> <skill_id>` |
| `get_skill_level` | スキルレベル取得 | `rpg api get_skill_level <player> <skill_id>` |
| `get_gold` | ゴールド取得 | `rpg api get_gold <player>` |
| `give_gold` | ゴールド付与 | `rpg api give_gold <player> <amount>` |
| `take_gold` | ゴールド剥奪 | `rpg api take_gold <player> <amount>` |
| `has_gold` | ゴールド所持確認 | `rpg api has_gold <player> <amount>` |
| `transfer_gold` | ゴールド転送 | `rpg api transfer_gold <from> <to> <amount>` |
| `calculate_damage` | ダメージ計算 | `rpg api calculate_damage <attacker> <target>` |
| `cast_at` | ターゲット指定スキル | `rpg api cast_at <player> <skill_id> <target>` |
| `get_entities_in_area` | 範囲エンティティ取得 | `rpg api get_entities_in_area <player> <shape> <params...>` |

---

## プレイヤーデータ取得

### getRPGPlayer

RPGPlayerを取得します。

```java
Optional<RPGPlayer> getRPGPlayer(Player player)
```

| パラメータ | 型 | 説明 |
|-----------|------|------|
| player | Player | Bukkitプレイヤー |

| 戻り値 | 説明 |
|--------|------|
| `Optional<RPGPlayer>` | RPGPlayer、存在しない場合はempty |

```java
api.getRPGPlayer(player).ifPresent(rpgPlayer -> {
    // RPGPlayer操作
    String classId = rpgPlayer.getClassId();
});
```

### getLevel

プレイヤーのバニラレベルを取得します。

```java
int getLevel(Player player)
```

```java
int level = api.getLevel(player);
```

### setLevel

プレイヤーのバニラレベルを設定します。

```java
void setLevel(Player player, int level)
```

```java
api.setLevel(player, 50);
```

### getStat

ステータスを取得します。

```java
int getStat(Player player, Stat stat)
```

| パラメータ | 型 | 説明 |
|-----------|------|------|
| player | Player | プレイヤー |
| stat | Stat | ステータス種別 (STRENGTH, INTELLIGENCE, SPIRIT, VITALITY, DEXTERITY) |

```java
int str = api.getStat(player, Stat.STRENGTH);
int intStat = api.getStat(player, Stat.INTELLIGENCE);
```

### getClassId

クラスIDを取得します。

```java
String getClassId(Player player)
```

| 戻り値 | 説明 |
|--------|------|
| String | クラスID、未設定の場合はnull |

```java
String classId = api.getClassId(player);
if (classId != null) {
    player.sendMessage("現在のクラス: " + classId);
}
```

---

## ステータス操作

### setStat

ステータスを設定します。

```java
void setStat(Player player, Stat stat, int baseValue)
```

```java
api.setStat(player, Stat.STRENGTH, 100);
```

### addStatPoints

手動配分ポイントを追加します。

```java
void addStatPoints(Player player, int points)
```

```java
// 5ポイント追加
api.addStatPoints(player, 5);
```

---

## クラス操作

### setClass

プレイヤーのクラスを設定します。

```java
boolean setClass(Player player, String classId)
```

| 戻り値 | 説明 |
|--------|------|
| boolean | 成功した場合はtrue |

```java
boolean success = api.setClass(player, "warrior");
if (success) {
    player.sendMessage("クラスを設定しました: Warrior");
}
```

### upgradeClassRank

クラスをランクアップします。

```java
boolean upgradeClassRank(Player player)
```

現在のクラスの上位クラスがある場合にランクアップします。

```java
boolean success = api.upgradeClassRank(player);
if (!success) {
    player.sendMessage("ランクアップできません");
}
```

### canUpgradeClass

クラスアップが可能かチェックします。

```java
boolean canUpgradeClass(Player player)
```

```java
if (api.canUpgradeClass(player)) {
    player.sendMessage("クラスアップ可能です！");
}
```

---

## スキル操作

### hasSkill

スキルを習得しているかチェックします。

```java
boolean hasSkill(Player player, String skillId)
```

```java
if (api.hasSkill(player, "fireball")) {
    player.sendMessage("火球術を習得しています");
}
```

### unlockSkill

スキルを習得させます。

```java
boolean unlockSkill(Player player, String skillId)
```

デフォルトでレベル1で習得します。

```java
api.unlockSkill(player, "fireball");
```

### castSkill

スキルを使用します。

```java
boolean castSkill(Player player, String skillId)
```

スキルを習得していない、またはクールダウン中の場合は失敗します。

```java
boolean success = api.castSkill(player, "fireball");
if (!success) {
    player.sendMessage("スキルの発動に失敗しました");
}
```

### getSkillLevel

スキルレベルを取得します。

```java
int getSkillLevel(Player player, String skillId)
```

習得していない場合は0を返します。

```java
int level = api.getSkillLevel(player, "fireball");
player.sendMessage("火球術レベル: " + level);
```

### getAcquiredSkills

プレイヤーの習得済みスキル一覧を取得します。

```java
Map<String, Integer> getAcquiredSkills(Player player)
```

習得済みスキルのマップ（スキルID -> レベル）を返します。

```java
Map<String, Integer> skills = api.getAcquiredSkills(player);
skills.forEach((skillId, level) -> {
    player.sendMessage(skillId + ": Lv." + level);
});
```

### getSkillsForClass

指定されたクラスで使用可能なスキル一覧を取得します。

```java
List<Skill> getSkillsForClass(String classId)
```

```java
List<Skill> skills = api.getSkillsForClass("warrior");
for (Skill skill : skills) {
    player.sendMessage(skill.getDisplayName());
}
```

---

## 経済操作

### getGoldBalance

ゴールド残高を取得します。

```java
double getGoldBalance(Player player)
```

```java
double balance = api.getGoldBalance(player);
player.sendMessage("所持金: " + balance + " G");
```

### depositGold

ゴールドを入金します。

```java
boolean depositGold(Player player, double amount)
```

```java
api.depositGold(player, 1000.0);
```

### withdrawGold

ゴールドを出金します。

```java
boolean withdrawGold(Player player, double amount)
```

残高が足りない場合は失敗します。

```java
boolean success = api.withdrawGold(player, 500.0);
if (!success) {
    player.sendMessage("ゴールドが足りません");
}
```

### hasEnoughGold

ゴールド残高が足りているかチェックします。

```java
boolean hasEnoughGold(Player player, double amount)
```

```java
if (api.hasEnoughGold(player, 1000.0)) {
    // 購入処理
}
```

### transferGold

プレイヤー間でゴールドを転送します。

```java
boolean transferGold(Player from, Player to, double amount)
```

```java
Player target = Bukkit.getPlayer("TargetPlayer");
if (target != null) {
    api.transferGold(player, target, 500.0);
}
```

---

## ダメージ計算

### calculateDamage

ダメージを計算します。

```java
double calculateDamage(Player attacker, Entity target)
```

攻撃者のステータスと武器に基づいてダメージを計算します。

```java
double damage = api.calculateDamage(player, target);
player.sendMessage("計算ダメージ: " + damage);
```

### applyStatModifiers

ステータス修正を適用したダメージを計算します。

```java
double applyStatModifiers(Player player, double baseDamage, Stat stat)
```

基本ダメージにステータス修正を適用します。

```java
double modifiedDamage = api.applyStatModifiers(player, 10.0, Stat.STRENGTH);
```

---

## ターゲット管理

### getLastTargetedEntity

最後にターゲットしたエンティティを取得します。

```java
Optional<Entity> getLastTargetedEntity(Player player)
```

```java
api.getLastTargetedEntity(player).ifPresent(target -> {
    player.sendMessage("ターゲット: " + target.getName());
});
```

### setTargetedEntity

ターゲットエンティティを設定します。

```java
void setTargetedEntity(Player player, Entity entity)
```

nullを渡すとクリアされます。

```java
api.setTargetedEntity(player, targetEntity);
```

---

## スキルトリガー

### castSkillAt

指定ターゲットでスキルを発動します。

```java
boolean castSkillAt(Player player, String skillId, Entity target)
```

```java
Entity target = /* ターゲット取得 */;
boolean success = api.castSkillAt(player, "fireball", target);
```

### castSkillWithCostType

指定コストタイプでスキルを発動します。

```java
boolean castSkillWithCostType(Player player, String skillId, SkillCostType costType)
```

costType は MP または HP を指定できます。

```java
// HPを消費して発動
api.castSkillWithCostType(player, "berserk", SkillCostType.HP);
```

### getEntitiesInArea

範囲内のエンティティを取得します。

```java
Collection<Entity> getEntitiesInArea(Player player, String shape, double... params)
```

| shape | 説明 | params |
|-------|------|--------|
| circle, sphere | 球形範囲 | 半径 |
| cube | 直方体範囲 | X, Y, Z |
| horizontal | 水平円形範囲 | 半径 |

```java
// 半径5ブロックの球形範囲
Collection<Entity> entities = api.getEntitiesInArea(player, "sphere", 5.0);

// 直方体範囲
Collection<Entity> entities = api.getEntitiesInArea(player, "cube", 10.0, 5.0, 10.0);

// 水平円形範囲
Collection<Entity> entities = api.getEntitiesInArea(player, "horizontal", 8.0);
```

---

## 関連項目

- [コマンドリファレンス](COMMANDS.md)
- [システム仕様書](SPEC.md)
- [チュートリアル](../TUTORIAL.md)
