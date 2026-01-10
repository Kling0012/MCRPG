# SKript 活用チュートリアル

> **初心者開発者向け**
> **最終更新**: 2026-01-09
> **バージョン**: 1.0.0

---

RPGPluginとSKriptを組み合わせて、独自のRPGシステムを作成するチュートリアルです。

## 目次

1. [SKript基礎](#skript基礎)
2. [RPGPluginの式を使う](#rpgpluginの式を使う)
3. [条件で分岐させる](#条件で分岐させる)
4. [効果で変化させる](#効果で変化させる)
5. [イベントをフックする](#イベントをフックする)
6. [実践:クエストシステム](#実践クエストシステム)
7. [実践:ギルドシステム](#実践ギルドシステム)
8. [実践:ダンジョンシステム](#実践ダンジョンシステム)

---

## SKript基礎

### SKriptとは？

Minecraft内でイベント駆動のスクリプトを書くためのプラグインです。

**基本的な構造:**

```skript# イベント
on イベント名:
    # 条件
    if 条件:
        # 効果
        効果コマンド
```

### 最初のスクリプト

`plugins/Skript/scripts/test.sk` を作成:

```skript# 最初のスクリプト
command /hello:
    trigger:
        send "こんにちは、%player%！" to player
```

保存後、`/skript reload` でリロードし、`/hello` を実行してみましょう。

---

## RPGPluginの式を使う

### プレイヤーデータを取得

```skriptcommand /mystatus:
    trigger:
        # レベル取得
        set {_level} to rpg level of player

        # ステータス取得
        set {_str} to rpg stat "STR" of player
        set {_int} to rpg stat "INT" of player
        set {_vit} to rpg stat "VIT" of player

        # クラス取得
        set {_class} to rpg class of player

        # ゴールド取得
        set {_gold} to rpg gold of player

        # 表示
        send "&e========== ステータス ==========" to player
        send "&f名前: %player%" to player
        send "&fレベル: Lv.%{_level}%" to player
        send "&fクラス: %{_class}%" to player
        send ""
        send "&cSTR: %{_str}%" to player
        send "&aINT: %{_int}%" to player
        send "&dVIT: %{_vit}%" to player
        send ""
        send "&6所持金: %{_gold}% G" to player
        send "&e===============================" to player
```

### スキル情報を取得

```skriptcommand /checkskill <text>:
    trigger:
        # スキル習得チェック
        if player has rpg skill arg-1:
            set {_level} to rpg skill level of arg-1 from player
            send "&aスキル「%arg-1%」は習得済みです" to player
            send "&eスキルレベル: Lv.%{_level}%" to player
        else:
            send "&cスキル「%arg-1%」は未習得です" to player
```

### 全スキルを表示

```skriptcommand /allskills:
    trigger:
        send "&e========== 習得スキル ==========" to player

        # チェックするスキルリスト
        set {_skills} to "fireball", "ice_spike", "heal", "power_strike", "dash"

        loop {_skills::*}:
            set {_skill} to loop-value
            if player has rpg skill {_skill}:
                set {_level} to rpg skill level of {_skill} from player
                send "&a%{_skill}% &fLv.%{_level}%" to player
            else:
                send "&7%{_skill}% &c未習得" to player

        send "&e==================================" to player
```

---

## 条件で分岐させる

### ステータスチェック

```skript# STRが50以上の場合のみ実行できるコマンド
command /heavyattack:
    trigger:
        set {_str} to rpg stat "STR" of player

        if {_str} >= 50:
            send "&c重攻撃を発動！" to player
            play sound "ENTITY_PLAYER_ATTACK_STRONG" with volume 1 to player
        else:
            send "&c重攻撃にはSTR50以上が必要です" to player
            send "&7現在のSTR: %{_str}%" to player
```

### 複数条件の組み合わせ

```skript# 複数の条件をチェック
command /specialskill:
    trigger:
        set {_str} to rpg stat "STR" of player
        set {_int} to rpg stat "INT" of player
        set {_level} to rpg level of player

        # 全ての条件を満たす必要がある
        if {_level} >= 10 and {_str} >= 30 and {_int} >= 20:
            send "&a特殊スキルを発動できます！" to player
        else:
            send "&c条件を満たしていません" to player
            send "&7必要: Lv.10, STR30, INT20" to player
            send "&7現在: Lv.%{_level}%, STR%{_str}%, INT%{_int}%" to player
```

### クラス別処理

```skript# クラス別のメッセージ
command /classinfo:
    trigger:
        set {_class} to rpg class of player

        if {_class} is "warrior":
            send "&cあなたは戦士クラスです" to player
            send "&7- 特徴: 高いSTRとVIT" to player
            send "&7- 得意武器: 剣、斧" to player
        else if {_class} is "mage":
            send "&bあなたは魔法使いクラスです" to player
            send "&7- 特徴: 高いINTとSPI" to player
            send "&7- 得意武器: 杖" to player
        else if {_class} is "archer":
            send "&aあなたは弓使いクラスです" to player
            send "&7- 特徴: 高いDEX" to player
            send "&7- 得意武器: 弓" to player
        else:
            send "&7クラスを選択してください" to player
```

### ゴールド所持チェック

```skript# ゴールドを持っているかチェックして購入
command /buypotion:
    trigger:
        # 50G持っているかチェック
        if player has 50 rpg gold:
            # ゴールドを引く
            take 50 rpg gold from player

            # 回復
            heal player

            send "&aポーションを購入しました (-50G)" to player
            send "&eHPが回復しました！" to player
        else:
            send "&cポーションには50G必要です" to player
            send "&7現在の所持金: %rpg gold% G" to player
```

---

## 効果で変化させる

### ステータスを操作

```skript# 管理者用: ステータスを変更するコマンド
command /setstat <text> <number>:
    permission: rpg.admin
    trigger:
        # ステータスを設定
        set rpg stat "%arg-1%" of player to arg-2

        send "&a%arg-1% を %arg-2% に設定しました" to player

        # 確認
        set {_value} to rpg stat "%arg-1%" of player
        send "&7現在の値: %{_value}%" to player
```

### スキルを解放

```skript# スキルブックを使用した時にスキルを習得
command /learnskill <text>:
    trigger:
        # スキル習得チェック
        if player has rpg skill arg-1:
            send "&cこのスキルは既に習得済みです" to player
            stop

        # ゴールドチェック
        set {_cost} to 500
        if player has {_cost} rpg gold:
            # ゴールドを引く
            take {_cost} rpg gold from player

            # スキル習得
            unlock rpg skill arg-1 for player

            send "&aスキル「%arg-1%」を習得しました！(-%{_cost}%G)" to player
            play sound "ENTITY_EXPERIENCE_ORB_PICKUP" with volume 1 to player
        else:
            send "&cスキル習得には%{_cost}%G必要です" to player
```

### クラスを変更

```skript# クラス選択GUI（コマンド版）
command /chooseclass:
    trigger:
        open chest with 1 row named "&6クラス選択" to player

        # 戦士
        set {_item} to iron sword named "&c戦士" with lore "&7高い攻撃力と体力"||"&eクリックで選択"
        set slot 0 of player's current inventory to {_item}

        # 魔法使い
        set {_item} to blaze rod named "&b魔法使い" with lore "&7強力な魔法を使用"||"&eクリックで選択"
        set slot 1 of player's current inventory to {_item}

        # 弓使い
        set {_item} to bow named "&a弓使い" with lore "&7遠距離攻撃が得意"||"&eクリックで選択"
        set slot 2 of player's current inventory to {_item}

on inventory click:
    if inventory name of player's current inventory is "&6クラス選択":
        cancel event

        # クリックしたアイテムに応じてクラス設定
        if clicked slot is 0:
            set rpg class of player to "warrior"
            send "&c戦士クラスを選択しました！" to player
            close player's inventory

        else if clicked slot is 1:
            set rpg class of player to "mage"
            send "&b魔法使いクラスを選択しました！" to player
            close player's inventory

        else if clicked slot is 2:
            set rpg class of player to "archer"
            send "&a弓使いクラスを選択しました！" to player
            close player's inventory
```

---

## イベントをフックする

### ログインイベント

```skript# プレイヤーログイン時の処理
on join:
    delay 1 tick

    # RPGデータがロードされるのを待って処理
    set {_level} to rpg level of player
    set {_class} to rpg class of player

    # 全体メッセージ
    broadcast "&e%player% がログインしました (Lv.%{_level}% %{_class}%)"

    # 個別メッセージ
    send "&a====================================" to player
    send "&6       おかえりなさい！" to player
    send "&e====================================" to player
    send "&fレベル: &cLv.%{_level}%" to player
    send "&fクラス: &6%{_class}%" to player
    send "&f所持金: &e%rpg gold% G" to player
    send "&a====================================" to player
```

### レベルアップイベント

```skリプト# レベルアップ時の祝福
on levelup:
    # 特定のレベルで特殊処理
    if player's level is 5:
        send "&e====================================" to player
        send "&6        レベル5に到達！" to player
        send "&e====================================" to player
        send "&a報酬として100Gを獲得しました！" to player

        give 100 rpg gold to player
        play sound "ENTITY_PLAYER_LEVELUP" with volume 1 to player

    else if player's level is 10:
        send "&6★★★★★ レベル10達成 ★★★★★" to player
        send "&a新しいスキルが解放されます！" to player

        # クラスに応じてスキル解放
        if player's rpg class is "warrior":
            unlock rpg skill "shield_bash" for player
        else if player's rpg class is "mage":
            unlock rpg skill "fireball" for player
        else if player's rpg class is "archer":
            unlock rpg skill "multishot" for player

        give 200 rpg gold to player
        play sound "ENTITY_PLAYER_LEVELUP" with volume 1.5 to player
```

### スキル発動イベント

```skリプト# スキル発動時のカスタム処理
on rpg skill cast:
    # スキルIDに応じた処理
    if skill-id is "fireball":
        send "&c🔥 ファイアボール！" to player

        # ターゲットがいる場合
        if target is set:
            send "&eターゲット: %target%" to player

    else if skill-id is "heal":
        send "&d💖 ヒール！" to player
        show "heart" particle 1 meter above player

    else if skill-id is "dash":
        send "&e⚡ ダッシュ！" to player
        play sound "ENTITY_PARROT_FLY" with volume 0.5 to player

    # スキルレベルに応じたエフェクト
    if skill-level >= 5:
        send "&6【マスター】熟練された技！" to player

# 特定スキルのイベント
on rpg skill cast of "fireball":
    send "&cファイアボールが発動されました！" to player
    show "large smoke" particle 1 meter above player
```

### 死亡イベント

```skリプト# プレイヤー死亡時のペナルティ
on death:
    # プレイヤーの場合
    if victim is a player:
        # ゴールドを一部失う
        set {_gold} to rpg gold of victim
        set {_penalty} to {_gold} * 0.05  # 5%失う

        if {_penalty} > 0:
            take {_penalty} rpg gold from victim
            send "&c死亡しました。所持金の5%を失いました" to victim
            send "&7失った金額: %{_penalty}% G" to victim

    # プレイヤー同士の戦闘
    attacker is a player
    victim is a player:
        # キラーに報酬
        give 100 rpg gold to attacker
        send "&a敵プレイヤーを撃破！100Gを獲得！" to attacker
```

---

## 実践:クエストシステム

### クエスト管理システム

```skript# クエスト管理システム
# データ保存用変数: {quest::%player%::accepted::%quest%}

# クエスト受諾
command /quest accept <text>:
    trigger:
        if {quest::%player%::accepted::%arg-1%} is set:
            send "&cこのクエストは既に受諾済みです" to player
            stop

        # クエストを受諾
        set {quest::%player%::accepted::%arg-1%} to true
        set {quest::%player%::progress::%arg-1%} to 0

        send "&aクエスト「%arg-1%」を受諾しました！" to player
        send "&7/quest check %arg-1% で進捗を確認できます" to player

# クエスト進捗確認
command /quest check <text>:
    trigger:
        if {quest::%player%::accepted::%arg-1%} is not set:
            send "&cこのクエストは受諾していません" to player
            stop

        send "&e========== �エスト進捗 ==========" to player
        send "&6クエスト: %arg-1%" to player

        # クエスト別の処理
        if arg-1 is "slime_killer":
            # スライム討伐クエスト
            send "&7スライムを討伐してください" to player
            send "&7進捗: %{quest::%player%::progress::slime_killer%}/10" to player

        else if arg-1 is "stat_master":
            # ステータスマスタークエスト
            set {_str} to rpg stat "STR" of player
            if {_str} >= 50:
                send "&a達成済み！ /quest complete %arg-1% で報酬を受け取ってください" to player
            else:
                send "&7STRを50以上にしてください (現在: %{_str}%)" to player

        send "&e==================================" to player

# クエスト完了
command /quest complete <text>:
    trigger:
        if {quest::%player%::accepted::%arg-1%} is not set:
            send "&cこのクエストは受諾していません" to player
            stop

        # クエスト完了処理
        if arg-1 is "slime_killer":
            if {quest::%player%::progress::slime_killer%} >= 10:
                give 200 rpg gold to player
                send "&aクエスト完了！200Gを獲得しました！" to player

                # クエストデータを削除
                delete {quest::%player%::accepted::slime_killer}
                delete {quest::%player%::progress::slime_killer}
            else:
                send "&cまだ達成条件を満たしていません" to player

# スライム討伐進捗
on death of slime:
    attacker is a player

    if {quest::%player%::accepted::slime_killer} is true:
        add 1 to {quest::%player%::progress::slime_killer}
        send "&aスライムを討伐！(%{quest::%player%::progress::slime_killer%}/10)" to attacker

        if {quest::%player%::progress::slime_killer}} >= 10:
            send "&eクエスト達成！ /quest complete slime_killer で報酬を受け取ってください" to attacker
```

### デイリークエスト

```skリプト# デイリークエストシステム
# 変数: {daily::%player%::last::%date%}

command /daily:
    trigger:
        # 日付を取得
        set {_today} to now

        # 最後の受取日をチェック
        if {daily::%player%::last::%{_today}%} is true:
            send "&c今日は既に受け取り済みです" to player
            send "&7明日の午前0時にリセットされます" to player
            stop

        # 報酬を付与
        set {_streak} to {daily::%player%::streak}
        if {_streak} is not set:
            set {_streak} to 1
        else:
            add 1 to {_streak}

        set {_gold} to 100 * {_streak}

        send "&e====================================" to player
        send "&6           デイリーボーナス" to player
        send "&e====================================" to player
        send "&f連続ログイン: &a%{_streak}%日目" to player
        send "&f報酬: &6%{_gold}% G" to player
        send "&e====================================" to player

        # 報酬付与
        give {_gold} rpg gold to player

        # 3日ごとのボーナス
        if {_streak} is divisible by 3:
            send "&eボーナス: スキルポイント+3" to player
            # スキルポイント追加処理（API経由）

        # データ保存
        set {daily::%player%::last::%{_today}%} to true
        set {daily::%player%::streak} to {_streak}

        play sound "ENTITY_EXPERIENCE_ORB_PICKUP" with volume 1 to player
```

---

## 実践:ギルドシステム

```skript# ギルドシステム（簡易版）
# 変数: {guild::%player%}, {guild::%guild_name%::members::*}

# ギルド作成
command /guild create <text>:
    trigger:
        # 既にギルドに所属しているかチェック
        if {guild::%player%} is set:
            send "&c既にギルドに所属しています" to player
            stop

        # ギルド作成
        set {guild::%player%} to arg-1
        set {guild::%arg-1%::owner} to player

        send "&aギルド「%arg-1%」を作成しました！" to player
        send "&e/guild invite <プレイヤー> でメンバーを招待できます" to player

# ギルド招待
command /guild invite <player>:
    trigger:
        # ギルドオーナーかチェック
        set {_guild} to {guild::%player%}
        if {_guild} is not set:
            send "&cギルドに所属していません" to player
            stop

        if {guild::%{_guild}%::owner} is not player:
            send "&cギルドオーナーのみが招待できます" to player
            stop

        # 招待メッセージ
        send "&e%player% からギルド「%{_guild}%」への招待が届きました" to arg-1
        send "&7/guild join %{_guild}% で参加できます" to arg-1

        # 招待データ保存
        set {guild::%{_guild}%::invite::%arg-1%} to true

# ギルド参加
command /guild join <text>:
    trigger:
        # 招待されているかチェック
        if {guild::%arg-1%::invite::%player%} is not true:
            send "&c招待されていません" to player
            stop

        # ギルド参加
        set {guild::%player%} to arg-1
        add player to {guild::%arg-1%::members::*}

        # 招待削除
        delete {guild::%arg-1%::invite::%player%}

        send "&aギルド「%arg-1%」に参加しました！" to player

        # ギルドメンバーに通知
        loop {guild::%arg-1%::members::*}:
            send "&a%player% がギルドに参加しました！" to loop-value

# ギルド情報
command /guild info:
    trigger:
        set {_guild} to {guild::%player%}

        if {_guild} is not set:
            send "&cギルドに所属していません" to player
            stop

        send "&e========== ギルド情報 ==========" to player
        send "&6ギルド名: %{_guild}%" to player
        send "&eオーナー: %{guild::%{_guild}%::owner%" to player

        # メンバーリスト
        send "&aメンバー:" to player
        loop {guild::%{_guild}%::members::*}:
            send "&7- %loop-value%" to player

        send "&e==================================" to player

# ギルドチャット
command /guild chat <text>:
    aliases: /gc
    trigger:
        set {_guild} to {guild::%player%}

        if {_guild} is not set:
            send "&cギルドに所属していません" to player
            stop

        # ギルドメンバー全員に送信
        send "&6[ギルド] %player%: &f%arg-1%" to {guild::%{_guild}%::owner}
        loop {guild::%{_guild}%::members::*}:
            send "&6[ギルド] %player%: &f%arg-1%" to loop-value
```

---

## 実践:ダンジョンシステム

```skript# ダンジョンシステム
# 変数: {dungeon::%player%::in_dungeon}, {dungeon::%player%::level}

# ダンジョンに入る
command /dungeon enter:
    trigger:
        # レベルチェック
        set {_level} to rpg level of player
        if {_level} < 10:
            send "&cダンジョンに入るにはレベル10以上が必要です" to player
            stop

        # 既にダンジョン内かチェック
        if {dungeon::%player%::in_dungeon} is true:
            send "&c既にダンジョン内にいます" to player
            stop

        # ゴールドチェック
        if player has 100 rpg gold:
            take 100 rpg gold from player
        else:
            send "&cダンジョン入場には100G必要です" to player
            stop

        # ダンジョン入場
        set {dungeon::%player%::in_dungeon} to true
        set {dungeon::%player%::level} to 1

        send "&eダンジョンに入場しました！" to player
        send "&7/dungeon info で情報を確認できます" to player
        send "&7/dungeon leave で脱出できます" to player

        # ダンジョンスタート
        teleport player to {dungeon::spawn::level1}

# ダンジョン情報
command /dungeon info:
    trigger:
        if {dungeon::%player%::in_dungeon} is not true:
            send "&cダンジョン内にいません" to player
            stop

        set {_level} to {dungeon::%player%::level}

        send "&e========== ダンジョン情報 ==========" to player
        send "&f現在のフロア: &a%{_level}%" to player
        send "&f残りのフロア: &74" to player
        send ""
        send "&6ボーナス効果:" to player
        send "&7- 獲得経験値 2倍" to player
        send "&7- ドロップレートアップ" to player
        send "&e======================================" to player

# ダンジョン脱出
command /dungeon leave:
    trigger:
        if {dungeon::%player%::in_dungeon} is not true:
            send "&cダンジョン内にいません" to player
            stop

        # 脱出処理
        delete {dungeon::%player%::in_dungeon}
        delete {dungeon::%player%::level}

        teleport player to {spawn::main}

        send "&eダンジョンから脱出しました" to player
        play sound "ENTITY_ENDERMAN_TELEPORT" with volume 1 to player

# モブ討伐時のダンジョンイベント
on death:
    attacker is a player
    {dungeon::%attacker%::in_dungeon} is true

    # ダンジョン内では経験値ボーナス
    # （実際にはプラグイン側で処理）

    # ドロップボーナス
    send "&e【ダンジョンボーナス】追加の報酬を獲得！" to attacker
    give 50 rpg gold to attacker
```

---

## トラブルシューティング

### よくあるエラー

**Q: SKriptがRPGPluginの要素を認識しない**

A: 以下を確認してください：
1. RPGPluginがロードされているか
2. SKriptより先にRPGPluginをロードする
3. `/skript reload` を実行する

**Q: 式の値が正しく取得できない**

A: ステータス名やスキルIDが正しいか確認：
- ステータス: STR, INT, SPI, VIT, DEX
- スキルID: 小文字で正確に指定

**Q: 条件が正しく動作しない**

A: 値の型を確認してください：
- 数値の比較には `>=` `<=` を使用
- 文字列の比較には `is` を使用

---

## まとめ

このチュートリアルで学んだこと:

1. **式**: プレイヤーデータを取得
2. **条件**: 値に応じて分岐
3. **効果**: データを変更
4. **イベント**: ゲーム内イベントをフック

### 次のステップ

- **[クイックスタートガイド](QUICKSTART.md)** - 基礎から学ぶ
- **[SKript統合ガイド](SKRIPT_INTEGRATION.md)** - 全要素のリファレンス
- **[プレースホルダー](PLACEHOLDERS.md)** - 情報表示の方法

---

**Happy Scripting! 📜**
