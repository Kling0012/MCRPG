# SKript 連携ガイド

> **作業ブランチ**: main
> **最終更新**: 2026-01-10
> **バージョン**: 1.0.1

---

## ⚠️ 移行のお知らせ

RPGPlugin 1.0.1より、SKript連携は**Skript Reflect**アドオンを使用する方式に変更されました。

従来のネイティブSKript構文（`rpg level of player` 等）は廃止されました。

**新しい連携方法については、[SKRIPT_REFLECT.md](SKRIPT_REFLECT.md)を参照してください。**

---

## 概要

RPGPluginは**Skript Reflect**アドオンを使用して、Skriptスクリプトから直接Javaメソッドを呼び出すことができます。

---

## 目次

1. [概要](#概要)
2. [セットアップ](#セットアップ)
3. [式（Expressions）](#式expressions)
4. [条件（Conditions）](#条件conditions)
5. [効果（Effects）](#効果effects)
6. [イベント（Events）](#イベントevents)
7. [使用例](#使用例)
8. [トラブルシューティング](#トラブルシューティング)

---

## 概要

RPGPluginはSKriptと完全に統合されており、ネイティブのSKript構文でRPGシステムにアクセスできます。コマンド実行の手間がなく、直感的に記述できます。

### 主な機能

- **4つの式**: Level, Stat, Class, Skill の取得
- **4つの条件**: スキル習得、クラスアップ、ステータス比較、クラス確認
- **4つの効果**: スキル解放/発動、クラス設定、ステータス変更
- **1つのイベント**: スキル発動イベント

### 特徴

- **skript-reflect対応**: Skillオブジェクトを直接アクセス可能
- **タイプセーフ**: 数値はNumber型、文字列はString型で返却
- **エラーハンドリング**: 不正な値に対してはデフォルト値を返却

---

## セットアップ

### 必要プラグイン

1. **RPGPlugin** (必須)
2. **SKript 2.8.4+** (必須)
3. **skript-reflect 0.5.0+** (オプション - Skillオブジェクトアクセス用)

### 確認方法

サーバーコンソールに以下が表示されます：

```
[RPGPlugin] Setting up third-party integrations...
[RPGPlugin] Skript integration loaded successfully!
[RPGPlugin] Third-party integrations setup complete!
```

---

## 式（Expressions）

### rpg level

プレイヤーのRPGレベルを取得します。

```skript
set {_level} to rpg level of player
send "あなたのレベル: %{_level}%" to player
```

**構文パターン:**
- `[the] rpg level of %player%`
- `[the] rpg level of %player%'s`
- `%player%'s rpg level`

**戻り値:** `Number`

### rpg stat

プレイヤーのステータスを取得します。

```skript
set {_str} to rpg stat "STR" of player
set {_int} to rpg stat "INTELLIGENCE" of player
set {_spi} to rpg stat "SPI" of player
set {_vit} to rpg stat "VITALITY" of player
set {_dex} to rpg stat "DEX" of player
```

**構文パターン:**
- `[the] rpg stat[e] %string% of %player%`
- `[the] rpg stat[e] %string% of %player%'s`
- `%player%'s rpg stat[e] %string%`

**ステータス名:**
| 短縮形 | 完全形 |
|--------|--------|
| STR | STRENGTH |
| INT | INTELLIGENCE |
| SPI | SPIRIT |
| VIT | VITALITY |
| DEX | DEXTERITY |

**戻り値:** `Number`

### rpg class

プレイヤーのクラスIDを取得します。

```skript
set {_class} to rpg class of player
if {_class} is "warrior":
    send "あなたは戦士クラスです" to player
```

**構文パターン:**
- `[the] rpg class of %player%`
- `[the] rpg class of %player%'s`
- `%player%'s rpg class`

**戻り値:** `String`

### rpg skill level

プレイヤーのスキルレベルを取得します。

```skript
set {_fireball_level} to rpg skill level of "fireball" from player
if {_fireball_level} > 0:
    send "ファイアボール LV%{_fireball_level}%" to player
```

**構文パターン:**
- `[the] rpg skill level of %string% [from] %player%`
- `[the] rpg skill level of %player%'s %string%`
- `%player%'s rpg skill level for %string%`

**戻り値:** `Number` (習得していない場合は0)

---

## 条件（Conditions）

### player has rpg skill

プレイヤーがスキルを習得しているかチェックします。

```skript
if player has rpg skill "fireball":
    send "ファイアボールを習得済み" to player
```

**構文パターン:**
- `%player% has [the] rpg skill [named] %string%`
- `%player%'s rpg skill[s] (contain|includes) %string%`

### player can upgrade rpg class

プレイヤーがクラスアップ可能かチェックします。

```skript
if player can upgrade rpg class:
    send "クラスアップ可能です！" to player
```

**構文パターン:**
- `%player% can upgrade [their] rpg class`
- `%player% is able to upgrade [their] rpg class`

### player's rpg stat is at least

プレイヤーのステータスが指定値以上かチェックします。

```skript
if player's rpg stat "STR" is at least 50:
    send "STRが50以上です" to player
```

**構文パターン:**
- `%player%'s rpg stat[e] %string% is [at least] %number%`
- `rpg stat[e] %string% of %player% is [at least] %number%`

### player's rpg class is

プレイヤーのクラスが指定クラスかチェックします。

```skript
if player's rpg class is "warrior":
    send "戦士クラスです" to player
```

**構文パターン:**
- `%player%'s rpg class is %string%`
- `%player% is [in] rpg class %string%`
- `rpg class of %player% is %string%`

---

## 効果（Effects）

### unlock rpg skill

プレイヤーにスキルを習得させます。

```skript
unlock rpg skill "power_strike" for player
```

**構文パターン:**
- `unlock [the] rpg skill %string% [for] %player%`
- `teach [the] rpg skill %string% to %player%`
- `make %player% (learn|unlock) [the] rpg skill %string%`

### make player cast rpg skill

プレイヤーにスキルを発動させます。

```skript
make player cast rpg skill "fireball"
```

**構文パターン:**
- `make %player% cast [the] rpg skill %string%`
- `force %player% to cast [the] rpg skill %string%`
- `cast [the] rpg skill %string% [for] %player%`

### set rpg class

プレイヤーのクラスを設定します。

```skript
set rpg class of player to "warrior"
```

**構文パターン:**
- `set [the] rpg class of %player% to %string%`
- `make %player% [a]n rpg class %string%`
- `change %player%'s rpg class to %string%`

### add/set/remove rpg stat

プレイヤーのステータスを変更します。

```skript
# STRに5ポイント追加
add 5 to rpg stat "STR" of player

# STRを100に設定
set rpg stat "STR" of player to 100

# STRから10ポイント削除
remove 10 from rpg stat "STR" of player
```

**構文パターン:**
- `add %number% [to] rpg stat[e] %string% of %player%`
- `set rpg stat[e] %string% of %player% to %number%`
- `remove %number% from rpg stat[e] %string% of %player%`

---

## イベント（Events）

### on rpg skill cast

スキル発動時にトリガーされます。

```skript
on rpg skill cast:
    send "スキル %skill-id% が発動しました！" to player
    send "スキルレベル: %skill-level%" to player
```

#### 特定スキルのみ

```skript
on rpg skill cast of "fireball":
    send "ファイアボール発動！" to player
```

#### イベント値

| イベント値 | 型 | 説明 |
|-----------|------|------|
| `player` | Player | スキルを発動したプレイヤー |
| `caster` | Player | 発動者（playerの別名） |
| `skill-id` | String | スキルID |
| `skill` | Skill | スキルオブジェクト（skript-reflect使用時） |
| `skill-level` | Integer | スキルレベル |
| `target` | Entity | ターゲットエンティティ（存在する場合） |
| `damage` | Number | 与えるダメージ（計算済みの場合） |

**構文パターン:**
- `on rpg skill cast`
- `on rpg skill cast [of] %string%`

---

## 使用例

### クエスト報酬システム

```skript
command /questcomplete <text>:
    trigger:
        if arg-1 is "tutorial":
            unlock rpg skill "power_strike" for player
            send "&aクエスト完了！スキルを獲得しました！" to player
```

### レベルボーナス

```skript
on levelup:
    if player's level is 10:
        send "&aレベル10ボーナス！" to player
    else if player's level is 20:
        unlock rpg skill "power_strike" for player
        send "&aレベル20ボーナス: スキルを獲得！" to player
```

### PvP報酬システム

```skript
on death:
    attacker is a player
    victim is a player
    send "&a撃破報酬を獲得！" to attacker
```

### スキル発動時の追加効果

```skript
on rpg skill cast:
    if skill-id is "heal":
        heal player by 5
    if skill-id is "power_strike":
        play sound "ENTITY_PLAYER_ATTACK_STRONG" with volume 1 and pitch 1 to player
```

### クラス別処理

```skript
command /classbonus:
    trigger:
        if player's rpg class is "warrior":
            add 10 to rpg stat "VIT" of player
            send "&a戦士ボーナス: VIT+10" to player
        else if player's rpg class is "mage":
            add 10 to rpg stat "INT" of player
            send "&a魔法使いボーナス: INT+10" to player
```

### スキル習得チェック

```skript
command /checkskill <text>:
    trigger:
        if player has rpg skill arg-1:
            set {_level} to rpg skill level of arg-1 from player
            send "&aスキル %{arg-1}% は習得済みです (LV%{_level}%)" to player
        else:
            send "&cスキル %{arg-1}% は未習得です" to player
```

---

## トラブルシューティング

### よくある問題

#### Q: SKript要素が認識されない

A: 以下を確認してください：
1. SKriptがインストールされているか
2. RPGPluginが正常にロードされているか
3. サーバーを再起動してみる

#### Q: 式がエラーになる

A: ステータス名やスキルIDが正しいか確認してください：
- ステータス: STR, INT, SPI, VIT, DEX (または完全形)
- スキルID: 小文字で正確に指定

#### Q: イベントが発動しない

A: スキルが正常に発動しているか確認してください。コマンドからスキルを発動してテストできます。

#### Q: skillオブジェクトにアクセスできない

A: skript-reflectがインストールされているか確認してください。

---

## サポート

バグ報告や機能リクエストは、GitHub Issuesまでお願いしてください。
