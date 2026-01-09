# RPGPlugin コマンドリファレンス

> **作業ブランチ**: main
> **最終更新**: 2026-01-09
> **バージョン**: 1.0.0

---

## 目次

1. [概要](#概要)
2. [基本コマンド](#基本コマンド)
3. [ステータスコマンド](#ステータスコマンド)
4. [スキルコマンド](#スキルコマンド)
5. [クラ スコマンド](#クラスコマンド)
6. [オークションコマンド](#オークションコマンド)
7. [トレードコマンド](#トレードコマンド)
8. [管理者コマンド](#管理者コマンド)
9. [権限](#権限)
10. [Tab補完](#tab補完)

---

## 概要

RPGPluginのコマンドはすべて `/rpg` から始まります。エイリアスとして `/rp` も使用可能です。

### 基本構文

```
/rpg <サブコマンド> [引数...]
```

### コマンド一覧（クイックリファレンス）

| コマンド | 説明 | 権限 |
|---------|------|------|
| `/rpg` | メインメニューを表示 | `rpg.use` |
| `/rpg stats` | ステータスGUIを表示 | `rpg.use` |
| `/rpg skill` | スキルGUIを表示 | `rpg.use` |
| `/rpg cast <スキルID>` | スキルを発動 | `rpg.use` |
| `/rpg class [ID|list]` | クラス管理 | `rpg.use` |
| `/rpg balance` | 残高を確認 | `rpg.use` |
| `/rpg auction ...` | オークション操作 | `rpg.use` |
| `/rpg trade ...` | トレード操作 | `rpg.use` |
| `/rpg help [カテゴリ]` | ヘルプを表示 | `rpg.use` |
| `/rpg reload` | 設定をリロード | `rpg.admin` |

---

## 基本コマンド

### /rpg

メインメニューを表示します。

**構文**: `/rpg`

**権限**: `rpg.use`

```
=== RPG Plugin ===
/rpg stats - ステータスGUIを表示
/rpg skill - スキルGUIを表示
/rpg class - クラスGUIを表示
/rpg auction - オークションシステム
/rpg trade - トレードを管理
/rpg cast <スキルID> - スキルを発動
/rpg balance - 残高を確認
/rpg help - ヘルプを表示
```

### /rpg help

ヘルプを表示します。

**構文**: `/rpg help [カテゴリ]`

**権限**: `rpg.use`

**カテゴリ**:
- `class` - クラスコマンドのヘルプ
- `auction` - オークションコマンドのヘルプ
- `trade` - トレードコマンドのヘルプ
- `skill` - スキルコマンドのヘルプ

```
========================================
           RPGPlugin ヘルプ
========================================

【基本コマンド】
/rpg - メインメニューを表示
/rpg help - このヘルプを表示
/rpg stats - ステータスGUIを表示
/rpg skill - スキルGUIを表示
/rpg balance - 残高を確認

【クラスコマンド】
/rpg class - クラスGUIを表示
/rpg class list - クラス一覧を表示
/rpg class <クラスID> - クラスを選択
...
```

---

## ステータスコマンド

### /rpg stats

ステータスGUIを表示します。

**構文**: `/rpg stats`

**権限**: `rpg.use`

GUIが使用できない場合は、テキストベースのステータス表示にフォールバックします。

**テキスト表示例**:
```
========== ステータス ==========
プレイヤー: Steve

レベル: 50

HP: 1500 / 1500
MP: 800 / 800 (+8.0/秒)

----- 基本ステータス -----
§c§lSTR§r: 120 (基本: 80)
§a§lINT§r: 65 (基本: 50)
§d§lSPI§r: 70 (基本: 55)
§c§lVIT§r: 100 (基本: 75)
§e§lDEX§r: 85 (基本: 60)

----- 戦闘ステータス -----
物理攻撃力: 240
魔法攻撃力: 130
物理防御力: 150
魔法防御力: 95

----- 命中 & 回避 -----
命中率: 85.0%
回避率: 25.0%
クリティカル率: 15.0%
クリティカル倍率: 2.00x
================================
```

---

## スキルコマンド

### /rpg skill

スキルGUIを表示します。

**構文**: `/rpg skill` または `/rpg skills`

**権限**: `rpg.use`

スキルツリーが自動的に構築され、習得可能なスキルが表示されます。

### /rpg cast

スキルを発動します。

**構文**: `/rpg cast <スキルID>`

**権限**: `rpg.use`

**発動条件**:
- スキルを習得している必要があります
- クールダウン中ではない必要があります
- 十分なMP/HPを持っている必要があります

```
使用法: /rpg cast <スキルID>

例:
/rpg cast fireball
/rpg cast power_strike
/rpg cast heal
```

---

## クラスコマンド

### /rpg class

クラス管理を行います。

**構文**:
- `/rpg class` - クラスGUIを表示
- `/rpg class list` - クラス一覧を表示
- `/rpg class <クラスID>` - クラスを選択

**権限**: `rpg.use`

#### クラス一覧表示

```
=== 利用可能なクラス ===
§6§lWarrior§r - 前線で戦う近接戦闘のエキスパート
§6§lMage§r - 強力な魔法を操る遠距離攻撃要員
§6§lRanger§r - 精密な射撃を行う遠距離物理アタッカー
§6§lCleric§r - 味方を支援する回復魔法の専門家

使用法: /rpg class <クラスID>
```

#### クラス選択

```
使用法: /rpg class <クラスID>

例:
/rpg class warrior
/rpg class mage
```

**注意**: 既にクラスを選択している場合は、変更できません（クラス変更機能は実装中です）。

---

## オークションコマンド

### /rpg auction

オークションシステムを操作します。

**構文**: `/rpg auction <サブコマンド> [引数...]`

**権限**: `rpg.use`

#### サブコマンド一覧

| サブコマンド | 構文 | 説明 |
|-------------|------|------|
| list | `/rpg auction list` | アクティブなオークション一覧 |
| info | `/rpg auction info <ID>` | オークション詳細を表示 |
| bid | `/rpg auction bid <ID> <金額>` | 入札する |
| create | `/rpg auction create <価格> <秒数>` | 手持ちアイテムを出品 |
| cancel | `/rpg auction cancel <ID>` | 自分の出品をキャンセル |

#### /rpg auction list

アクティブなオークション一覧を表示します。

```
=== アクティブなオークション (3) ===
[1] DIAMOND_SWORD 1000.00 - §61000.00§r §e残り45s
[2] GOLDEN_APPLE (入札あり: §6500.00§r) §e残り120s
[3] IRON_SWORD 100.00 §e残り30s
```

#### /rpg auction info

オークションの詳細情報を表示します。

```
=== オークション詳細 #1 ===
アイテム: DIAMOND_SWORD
出品者: Steve
開始価格: 1000.00
現在の入札額: 1200.00
最終入札者: Alex
有効期限: 45秒
最低次回入札額: 1320.00
```

#### /rpg auction bid

オークションに入札します。

**入札ルール**:
- 開始価格以上である必要があります
- 現在の入札額の10%以上上乗せする必要があります
- 入札があると有効期限が+5秒延長されます

```
使用法: /rpg auction bid <オークションID> <金額>

例:
/rpg auction bid 1 1500
```

#### /rpg auction create

手持ちのアイテムを出品します。

**出品期間**: 30-180秒

```
使用法: /rpg auction create <開始価格> <秒数(30-180)>

手に持っているアイテムを出品します

例:
/rpg auction create 1000 60
```

#### /rpg auction cancel

自分の出品をキャンセルします。

```
使用法: /rpg auction cancel <オークションID>

例:
/rpg auction cancel 1
```

---

## トレードコマンド

### /rpg trade

プレイヤー間トレードを操作します。

**構文**: `/rpg trade <サブコマンド> [引数...]`

**権限**: `rpg.use`

#### サブコマンド一覧

| サブコマンド | 構文 | 説明 |
|-------------|------|------|
| request | `/rpg trade request <プレイヤー名>` | トレードを申請 |
| accept | `/rpg trade accept` | トレードを承認 |
| deny | `/rpg trade deny` | トレードを拒否 |

#### /rpg trade request

他プレイヤーにトレードを申請します。

```
使用法: /rpg trade request <プレイヤー名>

例:
/rpg trade request Steve
```

#### /rpg trade accept

トレード申請を承認します。

```
/rpg trade accept
```

#### /rpg trade deny

トレード申請を拒否します。

```
/rpg trade deny
```

---

## 管理者コマンド

### /rpg reload

プラグインの設定をリロードします。

**構文**: `/rpg reload`

**権限**: `rpg.admin` (デフォルト: OPのみ)

```
=== RPGPlugin リロード中 ===
リロード完了! (245ms)
```

**リロード対象**:
- `config.yml` - メイン設定ファイル
- `classes/` - クラス定義（`hot_reload.classes: true`時）
- `skills/` - スキル定義（`hot_reload.skills: true`時）
- `exp/` - 経験値減衰設定（`hot_reload.exp_diminish: true`時）
- `mobs/` - モブドロップ設定（`hot_reload.mobs: true`時）

---

## 権限

### 権限ノード一覧

| 権限 | デフォルト | 説明 |
|------|----------|------|
| `rpg.use` | `true` | 基本的なRPGコマンドの使用 |
| `rpg.admin` | `op` | 管理者コマンドの使用 |
| `rpgplugin.api` | `false` | RPG APIの使用（Skript/Denizen専用、プレイヤーは使用不可） |

### plugin.yml 設定

```yaml
permissions:
  rpg.use:
    description: Allows using RPG commands
    default: true
  rpg.admin:
    description: Allows administrative RPG commands
    default: op
    children:
      rpg.use: true
```

---

## Tab補完

### サブコマンド補完

```
/rpg [Tab]
→ stats, skill, skills, cast, class, balance, auction, trade, help, reload
```

### スキルID補完

```
/rpg cast [Tab]
→ fireball, power_strike, heal, ... (習得済みスキル)
```

### クラスID補完

```
/rpg class [Tab]
→ list, warrior, mage, ranger, cleric, ...
```

### オークション補完

```
/rpg auction [Tab]
→ list, bid, create, cancel, info

/rpg auction bid [Tab]
→ 1, 2, 3, ... (アクティブなオークションID)

/rpg auction create [Tab]
→ <価格>

/rpg auction create 1000 [Tab]
→ 30, 60, 90, 120, 180
```

### トレード補完

```
/rpg trade [Tab]
→ request, accept, deny

/rpg trade request [Tab]
→ Steve, Alex, ... (オンラインプレイヤー名)
```

---

## 関連項目

- [APIリファレンス](API.md)
- [システム仕様書](SPEC.md)
- [チュートリアル](../TUTORIAL.md)
