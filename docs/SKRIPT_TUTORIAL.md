# SKript 活用チュートリアル

> **初心者開発者向け**
> **最終更新**: 2026-01-10
> **バージョン**: 1.0.1

---

## ⚠️ 移行のお知らせ

RPGPlugin 1.0.1より、SKript連携は**Skript Reflect**アドオンを使用する方式に変更されました。

**詳細なチュートリアルは、[SKRIPT_REFLECT.md](SKRIPT_REFLECT.md)を参照してください。**

---

## 概要

RPGPluginとSkript Reflectを組み合わせて、独自のRPGシステムを作成するチュートリアルです。

## 目次

1. [セットアップ](#セットアップ)
2. [最初のスクリプト](#最初のスクリプト)
3. [プレイヤーデータを取得](#プレイヤーデータを取得)
4. [ステータス操作](#ステータス操作)
5. [スキル操作](#スキル操作)
6. [実践例](#実践例)

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

---

## 最初のスクリプト

`plugins/Skript/scripts/test.sk` を作成:

```skript
# 最初のスクリプト
command /hello:
    trigger:
        send "こんにちは、%player%！" to player

# RPGPluginにアクセス
command /rpgtest:
    trigger:
        set {_plugin} = RPGPlugin.getInstance()
        if {_plugin} is null:
            send "&cRPGPluginが見つかりません" to player
            stop
        send "&aRPGPluginに接続しました" to player
```

保存後、`/skript reload` でリロードし、`/rpgtest` を実行してみましょう。

---

## プレイヤーデータを取得

### 基本的なデータ取得

```skript
command /mystatus:
    trigger:
        set {_plugin} = RPGPlugin.getInstance()
        set {_pm} = {_plugin}.getPlayerManager()
        set {_rpgPlayer} = {_pm}.getRPGPlayer(player's uuid)

        if {_rpgPlayer} is null:
            send "&cRPGデータが読み込まれていません" to player
            stop

        # クラスID取得
        set {_classId} = {_rpgPlayer}.getClassId()

        # 利用可能ポイント
        set {_points} = {_rpgPlayer}.getAvailablePoints()

        # 表示
        send "&e========== ステータス ==========" to player
        send "&f名前: %player%" to player
        send "&fクラス: %{_classId} ? "未所属"%" to player
        send "&f振りポイント: %{_points}%" to player
        send "&e===============================" to player
```

---

## ステータス操作

### ステータスを取得・設定

```skript
command /getstat <text>:
    trigger:
        set {_plugin} = RPGPlugin.getInstance()
        set {_pm} = {_plugin}.getPlayerManager()
        set {_rpgPlayer} = {_pm}.getRPGPlayer(player's uuid)

        # ステータス名をStat列挙型に変換
        set {_statName} = arg-1
        if {_statName} is "STR":
            set {_stat} = java import "com.example.rpgplugin.stat.Stat".STRENGTH
        else if {_statName} is "INT":
            set {_stat} = java import "com.example.rpgplugin.stat.Stat".INTELLIGENCE
        else:
            send "&c無効なステータス名です" to player
            stop

        set {_value} = {_rpgPlayer}.getBaseStat({_stat})
        send "&e%arg-1%: %{_value}%" to player
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

---

## 実践例

より詳細な実践例は、[SKRIPT_REFLECT.md](SKRIPT_REFLECT.md)を参照してください。

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
```

---

**参照**: [SKRIPT_REFLECT.md](SKRIPT_REFLECT.md) - 完全なAPIリファレンスと使用例
