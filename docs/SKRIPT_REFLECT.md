# Skript Reflect 連携ガイド

> **作業ブランチ**: main
> **最終更新**: 2026-01-10
> **バージョン**: 1.0.1

---

## 目次

1. [概要](#概要)
2. [セットアップ](#セットアップ)
3. [基本操作](#基本操作)
4. [ステータス操作](#ステータス操作)
5. [クラス操作](#クラス操作)
6. [スキル操作](#スキル操作)
7. [マナ/HP操作](#マナhp操作)
8. [実践例](#実践例)
9. [APIリファレンス](#apiリファレンス)

---

## 概要

RPGPluginは**Skript Reflect**アドオンを使用して、Skriptスクリプトから直接Javaメソッドを呼び出すことができます。これにより、独自のSKript構文を定義するよりも柔軟で高速な連携が可能です。

### 特徴

- **直接Javaメソッド呼び出し**: リフレクションで直接プラグインAPIにアクセス
- **タイプセーフ**: Javaの型システムをそのまま利用
- **完全なAPIアクセス**: プラグインの全機能にアクセス可能
- **カスタム構文不要**: Skriptの標準構文で記述可能

---

## セットアップ

### 必要プラグイン

1. **RPGPlugin** (必須)
2. **Skript** (必須)
3. **Skript Reflect** (必須)

### インストール

```bash
# サーバーのpluginsディレクトリに配置
plugins/
├── RPGPlugin.jar
├── Skript.jar
└── skript-reflect.jar
```

### 確認方法

サーバー起動時に以下が表示されます：

```
[RPGPlugin] Skript detected - SKriptBridge command available via /rpgapi
```

---

## 基本操作

### プラグインインスタンスの取得

まず、プラグインのインスタンスを取得します：

```skript
# プラグインインスタンスを取得
set {_plugin} to plugin "RPGPlugin"

# PlayerManagerを取得
set {_playerManager} = {_plugin}.getPlayerManager()

# プレイヤーを取得
set {_rpgPlayer} = {_playerManager}.getRPGPlayer(player's uuid)
```

### ショートカット（静的メソッド）

RPGPluginは静的アクセサーを提供しています：

```skript
# プラグインインスタンスを取得
set {_plugin} = RPGPlugin.getInstance()

# 各マネージャーにアクセス
set {_pm} = {_plugin}.getPlayerManager()
set {_sm} = {_plugin}.getStatManager()
set {_cm} = {_plugin}.getClassManager()
set {_skm} = {_plugin}.getSkillManager()
```

---

## ステータス操作

### ステータスを取得

```skript
command /rpgstats:
    trigger:
        set {_plugin} = RPGPlugin.getInstance()
        set {_pm} = {_plugin}.getPlayerManager()
        set {_rpgPlayer} = {_pm}.getRPGPlayer(player's uuid)

        if {_rpgPlayer} is null:
            send "&cRPGデータが読み込まれていません" to player
            stop

        # Stat列挙型を取得
        set {_str} = {_rpgPlayer}.getBaseStat(java import "com.example.rpgplugin.stat.Stat".STRENGTH)
        set {_int} = {_rpgPlayer}.getBaseStat(java import "com.example.rpgplugin.stat.Stat".INTELLIGENCE)
        set {_vit} = {_rpgPlayer}.getBaseStat(java import "com.example.rpgplugin.stat.Stat".VITALITY)
        set {_dex} = {_rpgPlayer}.getBaseStat(java import "com.example.rpgplugin.stat.Stat".DEXTERITY)
        set {_spi} = {_rpgPlayer}.getBaseStat(java import "com.example.rpgplugin.stat.Stat".SPIRIT)

        send "&e========== ステータス ==========" to player
        send "&cSTR: %{_str}%" to player
        send "&aINT: %{_int}%" to player
        send "&bSPI: %{_spi}%" to player
        send "&dVIT: %{_vit}%" to player
        send "&eDEX: %{_dex}%" to player
        send "&e===============================" to player
```

### ステータスを設定

```skript
command /setstat <text> <number>:
    permission: rpg.admin
    trigger:
        set {_plugin} = RPGPlugin.getInstance()
        set {_pm} = {_plugin}.getPlayerManager()
        set {_rpgPlayer} = {_pm}.getRPGPlayer(player's uuid)

        # ステータス名をStat列挙型に変換
        set {_statName} = arg-1
        if {_statName} is "STR" or "STRENGTH":
            set {_stat} = java import "com.example.rpgplugin.stat.Stat".STRENGTH
        else if {_statName} is "INT" or "INTELLIGENCE":
            set {_stat} = java import "com.example.rpgplugin.stat.Stat".INTELLIGENCE
        else if {_statName} is "VIT" or "VITALITY":
            set {_stat} = java import "com.example.rpgplugin.stat.Stat".VITALITY
        else if {_statName} is "DEX" or "DEXTERITY":
            set {_stat} = java import "com.example.rpgplugin.stat.Stat".DEXTERITY
        else if {_statName} is "SPI" or "SPIRIT":
            set {_stat} = java import "com.example.rpgplugin.stat.Stat".SPIRIT
        else:
            send "&c無効なステータス名です" to player
            stop

        {_rpgPlayer}.setBaseStat({_stat}, arg-2)
        send "&a%arg-1% を %{arg-2}% に設定しました" to player
```

### 振りポイントを操作

```skript
# 利用可能ポイントを取得
set {_points} = {_rpgPlayer}.getAvailablePoints()

# ポイントを追加
{_rpgPlayer}.addAvailablePoints(5)

# ポイントを振る
set {_stat} = java import "com.example.rpgplugin.stat.Stat".STRENGTH
{_rpgPlayer}.allocatePoint({_stat})
```

---

## クラス操作

### クラス情報を取得

```skript
command /myclass:
    trigger:
        set {_plugin} = RPGPlugin.getInstance()
        set {_pm} = {_plugin}.getPlayerManager()
        set {_rpgPlayer} = {_pm}.getRPGPlayer(player's uuid)

        set {_classId} = {_rpgPlayer}.getClassId()
        set {_classRank} = {_rpgPlayer}.getClassRank()

        send "&e========== クラス情報 ==========" to player
        if {_classId} is set:
            send "&fクラス: %{_classId}%" to player
            send "&fランク: Rank %{_classRank}%" to player
        else:
            send "&cクラス未所属です" to player
        send "&e================================" to player
```

### クラスを設定

```skript
command /setclass <text>:
    trigger:
        set {_plugin} = RPGPlugin.getInstance()
        set {_cm} = {_plugin}.getClassManager()

        # クラスを設定
        set {_success} = {_cm}.setPlayerClass(player, arg-1)

        if {_success}:
            send "&aクラスを「%arg-1%」に設定しました" to player
        else:
            send "&cクラスの設定に失敗しました。クラスが存在しない可能性があります。" to player
```

### クラスアップ条件チェック

```skript
command /checkupgrade <text>:
    trigger:
        set {_plugin} = RPGPlugin.getInstance()
        set {_cm} = {_plugin}.getClassManager()

        # アップグレード可能かチェック
        set {_result} = {_cm}.canUpgradeClass(player, arg-1)

        if {_result}.canUpgrade():
            send "&a%arg-1% にクラスアップ可能です" to player
        else:
            send "&cクラスアップできません: %{_result}.getMessage()%" to player
```

---

## スキル操作

### スキル習得状況を確認

```skript
command /hasskill <text>:
    trigger:
        set {_plugin} = RPGPlugin.getInstance()
        set {_pm} = {_plugin}.getPlayerManager()
        set {_rpgPlayer} = {_pm}.getRPGPlayer(player's uuid)

        # スキル習得チェック
        set {_hasSkill} = {_rpgPlayer}.hasSkill(arg-1)

        if {_hasSkill}:
            set {_level} = {_rpgPlayer}.getSkillLevel(arg-1)
            send "&aスキル「%arg-1%」は習得済みです (Lv.%{_level}%)" to player
        else:
            send "&cスキル「%arg-1%」は未習得です" to player
```

### スキルを習得させる

```skript
command /unlockskill <text> [<number>]:
    permission: rpg.admin
    trigger:
        set {_plugin} = RPGPlugin.getInstance()
        set {_pm} = {_plugin}.getPlayerManager()
        set {_rpgPlayer} = {_pm}.getRPGPlayer(player's uuid)

        # レベル指定（デフォルト1）
        set {_level} = arg-2 ? 1

        # スキル習得
        set {_success} = {_rpgPlayer}.acquireSkill(arg-1, {_level})

        if {_success}:
            send "&aスキル「%arg-1%」をLv.%{_level}% で習得しました" to player
        else:
            send "&cスキルの習得に失敗しました" to player
```

### スキルを発動

```skript
command /castsk <text>:
    trigger:
        set {_plugin} = RPGPlugin.getInstance()
        set {_skm} = {_plugin}.getSkillManager()

        # スキル発動
        set {_result} = {_skm}.executeSkill(player, arg-1)

        if {_result}.isSuccess():
            send "&aスキル「%arg-1%」を発動しました" to player
        else:
            send "&cスキル発動失敗: %{_result}.getErrorMessage()%" to player
```

---

## マナ/HP操作

### マナ操作

```skript
command /mana:
    trigger:
        set {_plugin} = RPGPlugin.getInstance()
        set {_pm} = {_plugin}.getPlayerManager()
        set {_rpgPlayer} = {_pm}.getRPGPlayer(player's uuid)

        # マナ情報を取得
        set {_current} = {_rpgPlayer}.getCurrentMana()
        set {_max} = {_rpgPlayer}.getMaxMana()
        set {_ratio} = {_rpgPlayer}.getManaRatio()

        send "&b========== マナ ==========" to player
        send "&f現在: %{_current}%/%{_max}%" to player
        send "&f割合: %{_ratio}%%%" to player

        # 状態チェック
        if {_rpgPlayer}.isFullMana():
            send "&aマナは満タンです" to player
        else if {_rpgPlayer}.isEmptyMana():
            send "&cマナが空です" to player

        # マナ回復
        {_rpgPlayer}.regenerateMana(10)
        send "&e10マナ回復しました" to player
        send "&b=========================" to player
```

### マナを消費

```skript
# マナ消費のチェック
set {_rpgPlayer} = {_pm}.getRPGPlayer(player's uuid)

if {_rpgPlayer}.hasMana(50):
    {_rpgPlayer}.consumeMana(50)
    send "&a50マナ消費しました" to player
else:
    send "&cマナが足りません" to player
```

### 最大HP/MPの取得

```skript
command /maxhp:
    trigger:
        set {_plugin} = RPGPlugin.getInstance()
        set {_pm} = {_plugin}.getPlayerManager()
        set {_rpgPlayer} = {_pm}.getRPGPlayer(player's uuid)

        # VITから最大HPを計算（VIT × 10）
        set {_vit} = {_rpgPlayer}.getFinalStat(java import "com.example.rpgplugin.stat.Stat".VITALITY)
        set {_maxHp} = {_vit} * 10

        send "&e最大HP: %{_maxHp}%" to player
```

---

## 実践例

### クエスト報酬システム

```skript
# クエスト完了時に報酬を与える
command /questcomplete <text>:
    trigger:
        set {_plugin} = RPGPlugin.getInstance()
        set {_pm} = {_plugin}.getPlayerManager()
        set {_rpgPlayer} = {_pm}.getRPGPlayer(player's uuid)

        # クエストIDに応じて報酬を分岐
        if arg-1 is "tutorial":
            # レベル報酬
            send "&aチュートリアル完了！経験値を獲得しました" to player
            player.giveExpLevels(5)

        else if arg-1 is "skill_master":
            # スキル習得報酬
            {_rpgPlayer}.acquireSkill("fireball", 1)
            {_rpgPlayer}.acquireSkill("ice_spike", 1)
            send "&aスキルマスクエスト完了！新規スキルを習得しました" to player

        else if arg-1 is "stat_boost":
            # ステータスボーナス
            set {_strStat} = java import "com.example.rpgplugin.stat.Stat".STRENGTH
            set {_currentStr} = {_rpgPlayer}.getBaseStat({_strStat})
            {_rpgPlayer}.setBaseStat({_strStat}, {_currentStr} + 10)
            send "&aステータスボーナス獲得！STR +10" to player
```

### ギルドボーナスシステム

```skript
# ギルド加入時にボーナスを与える
command /joinguild <text>:
    trigger:
        set {_plugin} = RPGPlugin.getInstance()
        set {_pm} = {_plugin}.getPlayerManager()
        set {_rpgPlayer} = {_pm}.getRPGPlayer(player's uuid)

        # ギルドに応じてボーナスを適用
        if arg-1 is "warriors":
            # 戦士ギルド: STR +5
            set {_stat} = java import "com.example.rpgplugin.stat.Stat".STRENGTH
            add 5 to {_rpgPlayer}.getBaseStat({_stat})
            send "&a戦士ギルドに加入しました！STR +5" to player

        else if arg-1 is "mages":
            # 魔術師ギルド: INT +5, スキル習得
            set {_stat} = java import "com.example.rpgplugin.stat.Stat".INTELLIGENCE
            add 5 to {_rpgPlayer}.getBaseStat({_stat})
            {_rpgPlayer}.acquireSkill("mana_boost", 1)
            send "&a魔術師ギルドに加入しました！INT +5, マナブースト習得" to player

        else if arg-1 is "rogues":
            # 盗賊ギルド: DEX +5
            set {_stat} = java import "com.example.rpgplugin.stat.Stat".DEXTERITY
            add 5 to {_rpgPlayer}.getBaseStat({_stat})
            send "&a盗賊ギルドに加入しました！DEX +5" to player
```

### ダンジョンクリア報酬

```skript
# ダンジョンクリア時の報酬
on death of zombie:
    # ダンジョン内でのキルかチェック（ワールド名など）
    if player's world's name contains "dungeon":
        set {_plugin} = RPGPlugin.getInstance()
        set {_pm} = {_plugin}.getPlayerManager()
        set {_rpgPlayer} = {_pm}.getRPGPlayer(player's uuid)

        # ランダムなステータスボーナス
        set {_bonus} = random integer between 1 and 3

        if {_bonus} is 1:
            set {_stat} = java import "com.example.rpgplugin.stat.Stat".STRENGTH
            set {_name} = "STR"
        else if {_bonus} is 2:
            set {_stat} = java import "com.example.rpgplugin.stat.Stat".INTELLIGENCE
            set {_name} = "INT"
        else:
            set {_stat} = java import "com.example.rpgplugin.stat.Stat".VITALITY
            set {_name} = "VIT"

        set {_current} = {_rpgPlayer}.getBaseStat({_stat})
        {_rpgPlayer}.setBaseStat({_stat}, {_current} + 1)

        send "&aダンジョンボーナス! %{_name}% +1" to player
```

---

## APIリファレンス

### RPGPlugin（メインクラス）

| メソッド | 説明 | 戻り値 |
|---------|------|--------|
| `getInstance()` | プラグインインスタンスを取得 | `RPGPlugin` |
| `getPlayerManager()` | プレイヤーマネージャーを取得 | `PlayerManager` |
| `getStatManager()` | ステータスマネージャーを取得 | `StatManager` |
| `getClassManager()` | クラスマネージャーを取得 | `ClassManager` |
| `getSkillManager()` | スキルマネージャーを取得 | `SkillManager` |
| `getSkillConfig()` | スキル設定を取得 | `SkillConfig` |
| `getActiveSkillExecutor()` | アクティブスキル実行器 | `ActiveSkillExecutor` |
| `getMythicMobsManager()` | MythicMobs管理 | `MythicMobsManager` |

### PlayerManager

| メソッド | 説明 | 戻り値 |
|---------|------|--------|
| `getRPGPlayer(UUID)` | UUIDからRPGPlayer取得 | `Optional<RPGPlayer>` |
| `getRPGPlayer(Player)` | PlayerからRPGPlayer取得 | `Optional<RPGPlayer>` |
| `isOnline(UUID)` | プレイヤーがオンラインか | `boolean` |
| `getOnlinePlayerCount()` | オンラインプレイヤー数 | `int` |

### RPGPlayer

| メソッド | 説明 | 戻り値 |
|---------|------|--------|
| `getUuid()` | UUID取得 | `UUID` |
| `getUsername()` | ユーザー名取得 | `String` |
| `getBukkitPlayer()` | Bukkitプレイヤー取得 | `Player` |
| `getClassId()` | クラスID取得 | `String` |
| `setClassId(String)` | クラスID設定 | `void` |
| `getClassRank()` | クラスランク取得 | `int` |
| `getBaseStat(Stat)` | 基礎ステータス取得 | `int` |
| `setBaseStat(Stat, int)` | 基礎ステータス設定 | `void` |
| `getFinalStat(Stat)` | 最終ステータス取得 | `double` |
| `getAvailablePoints()` | 振りポイント取得 | `int` |
| `setAvailablePoints(int)` | 振りポイント設定 | `void` |
| `addAvailablePoints(int)` | 振りポイント追加 | `void` |
| `allocatePoint(Stat)` | ポイントを振る | `void` |
| `getMaxMana()` | 最大マナ取得 | `int` |
| `setMaxMana(int)` | 最大マナ設定 | `void` |
| `getCurrentMana()` | 現在のマナ取得 | `int` |
| `setCurrentMana(int)` | マナ設定 | `void` |
| `addMana(int)` | マナ追加 | `void` |
| `consumeMana(int)` | マナ消費 | `boolean` |
| `hasMana(int)` | マナが十分か | `boolean` |
| `isFullMana()` | マナ満タンか | `boolean` |
| `isEmptyMana()` | マナ空か | `boolean` |
| `getManaRatio()` | マナ割合取得 | `double` |
| `regenerateMana(int)` | マナ回復 | `void` |
| `hasSkill(String)` | スキル習得済みか | `boolean` |
| `getSkillLevel(String)` | スキルレベル取得 | `int` |
| `acquireSkill(String, int)` | スキル習得 | `boolean` |
| `upgradeSkill(String)` | スキルレベルアップ | `boolean` |

### Stat列挙型

| 定数 | 説明 |
|------|------|
| `Stat.STRENGTH` | 攻撃力 |
| `Stat.INTELLIGENCE` | 魔力 |
| `Stat.SPIRIT` | 精神 |
| `Stat.VITALITY` | 体力 |
| `Stat.DEXTERITY` | 器用さ |

---

## トラブルシューティング

### よくあるエラー

#### 「plugin "RPGPlugin" is null」になる

**原因**: プラグインが正常に起動していないか、Skript Reflectがインストールされていません。

**解決策**:
1. プラグインが正常に起動しているか確認
2. Skript Reflectがインストールされているか確認

#### 「getRPGPlayer returned null」になる

**原因**: プレイヤーデータが読み込まれていません。

**解決策**:
1. プレイヤーが一度ログインしているか確認
2. データベース接続を確認

### デバッグコマンド

```skript
command /rpgdebug:
    trigger:
        set {_plugin} = plugin "RPGPlugin"
        if {_plugin} is null:
            send "&cRPGPluginが見つかりません" to player
        else:
            send "&aRPGPluginは正常に動作しています" to player

        # 各マネージャーのチェック
        set {_pm} = {_plugin}.getPlayerManager()
        send "&eオンラインプレイヤー数: %{_pm}.getOnlinePlayerCount()%" to player
```

---

**参考リンク**:
- [Skript Reflect 公式ドキュメント](https://tpgamesnl.gitbook.io/skript-reflect/)
- [Skript Discord](https://discord.gg/skript)
