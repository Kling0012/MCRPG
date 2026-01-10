# RPGPlugin クイックスタートガイド

> **初心者開発者向け**
> **最終更新**: 2026-01-09
> **バージョン**: 1.0.0

---

このガイドでは、RPGPluginを活用して独自のRPG要素を追加したい初心者開発者向けに、すぐに使える内容を説明します。

## 目次

1. [はじめに](#はじめに)
2. [5分でわかる基本](#5分でわかる基本)
3. [SKriptで始める](#skriptで始める)
4. [PlaceholderAPIで表示する](#placeholderapiで表示する)
5. [最初のクエストを作る](#最初のクエストを作る)
6. [次のステップ](#次のステップ)

---

## はじめに

### RPGPluginって何？

MinecraftサーバーにRPG要素（ステータス、スキル、クラス、経済）を追加するプラグインです。

**できること:**
- プレイヤーにレベルとステータスを持たせる
- スキルを習得・発動させる
- クラス（職業）システムを追加
- 独自の通貨（ゴールド）を管理
- SKriptやPlaceholderAPIと連携

### 開発者向けの3つの連携方法

| 方法 | 難易度 | 用途 | 例 |
|------|--------|------|-----|
| **PlaceholderAPI** | ★☆☆ | 情報表示 | スコアボード、チャット |
| **SKript** | ★☆☆ | イベント・条件 | クエスト、報酬 |
| **Java API** | ★★★ | 複雑な処理 | 独自プラグイン開発 |

初心者は **PlaceholderAPI** と **SKript** から始めるのがおすすめです。

---

## 5分でわかる基本

### 1. PlaceholderAPIで情報を表示する

**インストール:**
1. PlaceholderAPI をインストール
2. サーバー再起動

**すぐに使えるプレースホルダー:**

```
%rpg_level%        → レベル
%rpg_stat_STR%     → STR値
%rpg_stat_INT%     → INT値
%rpg_stat_SPI%     → SPI値
%rpg_stat_VIT%     → VIT値
%rpg_stat_DEX%     → DEX値
%rpg_class%        → クラスID
%rpg_class_name%   → クラス表示名
%rpg_gold%         → ゴールド
```

**試してみよう:**

```
/papi parse %rpg_level%
```

プレイヤーのレベルが表示されればOKです！

### 2. SKriptでイベントを作る

**必要なもの:**
- SKript プラグイン

**基本的な書き方:**

```skript# プレイヤーがログイン時のメッセージ
on join:
    set {_level} to rpg level of player
    send "ようこそ！Lv%{_level}%から始まります" to player
```

これだけで、プレイヤー参加時にRPGレベルを表示できます！

---

## SKriptで始める

### Step 1: 最初のスクリプト

`plugins/Skript/scripts/welcome.sk` を作成:

```skript# 新規プレイヤー歓迎スクリプト
on join:
    # 初回ログインかチェック
    if player has played before:
        set {_level} to rpg level of player
        set {_class} to rpg class of player
        send "&aおかえりなさい！" to player
        send "&7現在: Lv%{_level}% %{_class}%" to player
    else:
        send "&6はじめてのログインですね！" to player
        send "&e/rpg コマンドでステータスを確認できます" to player
```

### Step 2: レベルアップ時のイベント

```skript# レベルアップボーナス
on levelup:
    if player's level is 10:
        send "&e====================================" to player
        send "&6               レベル10達成！" to player
        send "&e====================================" to player
        send "&a報酬としてスキルポイントを5ポイント付与します" to player
        play sound "ENTITY_PLAYER_LEVELUP" with volume 1 and pitch 1 to player
    else if player's level is 20:
        send "&6レベル20達成！おめでとうございます！" to player
        play sound "ENTITY_PLAYER_LEVELUP" with volume 1 and pitch 1.5 to player
```

### Step 3: クラス別処理

```skript# クラス別の挨拶
on join:
    set {_class} to rpg class of player
    if {_class} is "warrior":
        send "&c勇敢なる戦士%player%よ、ようこそ！" to player
    else if {_class} is "mage":
        send "&b賢き魔法使い%player%よ、ようこそ！" to player
    else if {_class} is "archer":
        send "&a精准なる弓使い%player%よ、ようこそ！" to player
    else:
        send "&7冒険者%player%よ、ようこそ！" to player
```

### Step 4: スキル習得チェック

```skript# スキル習得意を確認するコマンド
command /myskills:
    trigger:
        send "&e========== 習得済みスキル ==========" to player

        # ファイアボール
        if player has rpg skill "fireball":
            set {_lv} to rpg skill level of "fireball" from player
            send "&cファイアボール &fLv%{_lv}%" to player

        # パワーストライク
        if player has rpg skill "power_strike":
            set {_lv} to rpg skill level of "power_strike" from player
            send "&6パワーストライク &fLv%{_lv}%" to player

        # ヒール
        if player has rpg skill "heal":
            set {_lv} to rpg skill level of "heal" from player
            send "&dヒール &fLv%{_lv}%" to player

        send "&e====================================" to player
```

---

## PlaceholderAPIで表示する

### スコアボードに表示

**FeatherBoardを使用する場合:**

1. `plugins/FeatherBoard/boards` に `rpg.txt` を作成

```yaml
rpg_status:
  title: "&c&lRPGステータス"
  rows:
    - "&e━━━━━━━━━━━━━━━━━━"
    - "&eプレイヤー: &f%player%"
    - ""
    - "&cレベル: &f%rpg_level%"
    - "&6クラス: &f%rpg_class_name%"
    - ""
    - "&cSTR: &f%rpg_stat_STR%"
    - "&aINT: &f%rpg_stat_INT%"
    - "&bSPI: &f%rpg_stat_SPI%"
    - "&dVIT: &f%rpg_stat_VIT%"
    - "&eDEX: &f%rpg_stat_DEX%"
    - ""
    - "&e所持金: &f%rpg_gold% G"
    - "&e━━━━━━━━━━━━━━━━━━"
```

2. `/fb reload` でリロード

### チャットフォーマット

**EssentialsX Chatの場合:**

```yaml
# plugins/Essentials/chat.yml
format: '&7[Lv%rpg_level%&7] &f%rpg_class_name% %display%&7: &f%message%'
```

これでチャットが次のように表示されます:
```
[Lv25] 戦士 Steve: こんにちは！
```

### TABリスト

**TABプラグインの場合:**

```yaml
# plugins/TAB/config.yml
  - playerline: "&eLv.%rpg_level% &7%player% | &6%rpg_class_name% | &c❤ %rpg_max_hp%"
```

---

## 最初のクエストを作る

### クエスト1: チュートリアル報酬

```skript# チュートリアル完了コマンド
command /questcomplete:
    trigger:
        send "&e========== クエスト完了 ==========" to player
        send "&aおめでとうございます！チュートリアル完了です" to player
        send "&e報酬を受け取りました:" to player
        send "&7- 100ゴールド" to player
        send "&7- スキル: パワーストライク" to player

        # 報酬を付与
        give 100 rpg gold to player
        unlock rpg skill "power_strike" for player

        # エフェクト
        play sound "ENTITY_PLAYER_LEVELUP" with volume 1 and pitch 1 to player
        loop 10 times:
            show "heart" particle 1 meter above player for player
            wait 5 ticks
```

### クエスト2: 毎日ログインボーナス

```skript# デイリーボーナスシステム
command /daily:
    trigger:
        if {daily::%player%::last} is not set:
            # 初回またはデータリセット後
            set {_daily::%player%::last} to now
            set {_daily::%player%::streak} to 0

        # 前回から24時間以上経過しているかチェック
        # （簡略版: 実際にはタイムスタンプ比較が必要）
        set {_can_claim} to true

        if {_can_claim} is true:
            # 連続ログイン日数を増やす
            add 1 to {daily::%player%::streak}
            set {_streak} to {daily::%player%::streak}

            # 報酬計算
            set {_gold} to 100 * {_streak}

            send "&a====================================" to player
            send "&6           デイリーボーナス" to player
            send "&a====================================" to player
            send "&e連続ログイン: &f%{_streak}%日目" to player
            send "&6報酬: &f%{_gold}% ゴールド" to player
            send "&a====================================" to player

            # 報酬付与
            give {_gold} rpg gold to player

            # ボーナス（3日ごと）
            if {_streak} is divisible by 3:
                send "&eボーナス: スキルポイント+3" to player
                # スキルポイントを追加する処理はAPI経由で

            play sound "ENTITY_EXPERIENCE_ORB_PICKUP" with volume 1 to player
        else:
            send "&cまだ受け取れません。明日また来てください！" to player
```

### クエスト3: 討練クエスト

```skript# 訓練クエスト
command /training:
    trigger:
        set {_str} to rpg stat "STR" of player

        if {_str} < 50:
            send "&c訓練: STRを50以上にしてください" to player
            send "&7現在のSTR: %{_str}%" to player
        else:
            send "&a訓練完了！" to player
            send "&7STRが50以上达到了ため、報酬を授与します" to player

            # 報酬
            give 500 rpg gold to player
            unlock rpg skill "heavy_strike" for player

            # ステータスボーナス
            add 5 to rpg stat "STR" of player
            send "&eSTR+5 のボーナスを獲得！" to player
```

---

## PvP報酬システム

```skript# プレイヤーキル報酬
on death:
    attacker is a player
    victim is a player

    # 攻撃者に報酬
    set {_reward} to 50
    give {_reward} rpg gold to attacker
    send "&a敵を撃破！%{_reward}%Gを獲得！" to attacker
    play sound "ENTITY_EXPERIENCE_ORB_PICKUP" with volume 0.5 to attacker

    # 被害者にペナルティ（少し失う）
    take 25 rpg gold from victim
    send "&c撃破されました。25Gを失いました" to victim
```

---

## スキル発動時のカスタムイベント

```skript# スキル発動時のエフェクト追加
on rpg skill cast:
    # スキルIDに応じて効果を変える
    if skill-id is "fireball":
        # ファイアボール発動時
        send "&cファイアボールを発動！" to player
        play sound "ENTITY_BLAZE_SHOOT" with volume 1 to player
        show "large smoke" particle 1 meter above player for player

    else if skill-id is "heal":
        # ヒール発動時
        send "&d回復魔法を発動！" to player
        play sound "ENTITY_EXPERIENCE_BOTTLE_THROW" with volume 1 to player
        show "heart" particle 1 meter above player for player

    else if skill-id is "power_strike":
        # パワーストライク発動時
        send "&6パワーストライク！" to player
        play sound "ENTITY_PLAYER_ATTACK_STRONG" with volume 1 to player

    # スキルレベルに応じたメッセージ
    if skill-level >= 5:
        send "&eスキルマスターの技！" to player
```

---

## 次のステップ

### もっと学びたい場合

1. **[SKript統合ガイド](SKRIPT_INTEGRATION.md)** - 全ての式・条件・効果・イベント
2. **[プレースホルダー](PLACEHOLDERS.md)** - 全プレースホルダー一覧
3. **[APIドキュメント](API_DOCUMENTATION.md)** - Java APIの詳細

### 応用例

| 作りたいもの | 参考セクション |
|-------------|---------------|
| クエストシステム | [最初のクエストを作る](#最初のクエストを作る) |
| スコアボード表示 | [PlaceholderAPIで表示する](#placeholderapiで表示する) |
| PvP報酬 | [PvP報酬システム](#pvp報酬システム) |
| スキルカスタマイズ | [スキル発動時のカスタムイベント](#スキル発動時のカスタムイベント) |

### サポート

- **バグ報告**: GitHub Issues
- **質問**: Discordサーバー（あれば）
- **ドキュメント**: `docs/` フォルダ

---

**Happy Coding! 🎮**
