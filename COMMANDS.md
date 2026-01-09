# RPGPlugin コマンドリスト

このドキュメントでは、RPGPluginで利用可能なすべてのコマンドとその使い方を説明します。

---

## 目次

1. [基本コマンド](#基本コマンド)
2. [ステータスコマンド](#ステータスコマンド)
3. [スキルコマンド](#スキルコマンド)
4. [クラスコマンド](#クラスコマンド)
5. [経済コマンド](#経済コマンド)
6. [オークションコマンド](#オークションコマンド)
7. [トレードコマンド](#トレードコマンド)
8. [管理者コマンド](#管理者コマンド)
9. [APIコマンド](#apiコマンド)

---

## 基本コマンド

### `/rpg`
メインRPGコマンドのエントリーポイントです。引数なしで実行するとメインメニューを表示します。

```
/rpg
```

**エイリアス**: `/rp`

### `/rpg help`
ヘルプメッセージを表示します。利用可能なすべてのコマンドの一覧が表示されます。

```
/rpg help
```

---

## ステータスコマンド

### `/rpg stats`
ステータスGUIを開きます。ステータスの確認、振り分けができます。

```
/rpg stats
```

**機能**:
- 現在のステータス値（STR/INT/SPI/VIT/DEX）の確認
- 手動配分ポイントの消費
- 戦闘ステータスの確認（攻撃力/防御力/命中/回避/クリティカル）

---

## スキルコマンド

### `/rpg skill` / `/rpg skills`
スキルツリーGUIを開きます。スキルの確認、習得ができます。

```
/rpg skill
/rpg skills
```

**機能**:
- 習得可能なスキルの確認
- スキルポイントの消費によるスキル習得
- スキルレベルの上昇

### `/rpg cast <スキルID>`
アクティブスキルを発動します。

```
/rpg cast fireball
```

**Tab補完**: スキルIDが補完されます

**条件**:
- スキルを習得している必要があります
- クールダウン中ではない必要があります
- 十分なMPを持っている必要があります

---

## クラスコマンド

### `/rpg class`
クラス選択GUIを開きます。

```
/rpg class
```

### `/rpg class list`
利用可能なクラス一覧を表示します。

```
/rpg class list
```

### `/rpg class <クラスID>`
指定したクラスに変更します。

```
/rpg class warrior
```

**Tab補完**: クラスIDが補完されます

**条件**:
- 初期クラスのみ選択可能
- クラスの変更には追加要件がある場合があります

---

## 経済コマンド

### `/rpg balance`
現在のゴールド残高を表示します。

```
/rpg balance
```

**表示内容**:
- 現在のゴールド残高
- 総獲得額
- 総消費額

---

## オークションコマンド

### `/rpg auction`
オークションコマンドのヘルプを表示します。

```
/rpg auction
```

### `/rpg auction list`
アクティブなオークション一覧を表示します。

```
/rpg auction list
```

**表示内容**:
- オークションID
- アイテム名
- 現在の入札額
- 残り時間

### `/rpg auction info <ID>`
指定したオークションの詳細情報を表示します。

```
/rpg auction info 1
```

**Tab補完**: アクティブなオークションIDが補完されます

### `/rpg auction bid <ID> <金額>`
オークションに入札します。

```
/rpg auction bid 1 150.0
```

**Tab補完**: オークションIDが補完されます

**入札ルール**:
- 開始価格以上である必要があります
- 現在の入札額の10%以上上乗せする必要があります
- 入札があると有効期限が+5秒延長されます

### `/rpg auction create <価格> <秒数>`
手持ちのアイテムを出品します。

```
/rpg auction create 100 60
```

**Tab補完**: 秒数（30/60/90/120/180）が補完されます

**引数**:
- 価格: 最低価格（0より大きい必要があります）
- 秒数: 出品期間（30-180秒）

### `/rpg auction cancel <ID>`
自分の出品をキャンセルします。

```
/rpg auction cancel 1
```

**Tab補完**: 自分の出品オークションIDが補完されます

---

## トレードコマンド

### `/rpg trade request <プレイヤー名>`
指定したプレイヤーにトレードを申請します。

```
/rpg trade request Steve
```

**Tab補完**: オンラインプレイヤー名が補完されます

### `/rpg trade accept`
保留中のトレード申請を承認します。

```
/rpg trade accept
```

### `/rpg trade deny`
保留中のトレード申請を拒否します。

```
/rpg trade deny
```

---

## 管理者コマンド

### `/rpg reload`
プラグインの設定をリロードします。

```
/rpg reload
```

**権限**: `rpg.admin`

**機能**:
- YAML設定ファイルの再読み込み
- スキル・クラス設定の更新
- データベース接続の再確認

---

## APIコマンド

### `/rpg api`
APIコマンドのヘルプを表示します。

```
/rpg api
```

### `/rpg api call <action> <args...>`
SKript/DenizenからAPIを呼び出します。

```
/rpg api call get_level Steve
/rpg api call give_gold Steve 100
```

**Tab補完**: アクション名が補完されます

**アクション一覧**:

| カテゴリ | アクション | 説明 |
|---------|-----------|------|
| レベル | `get_level` | プレイヤーのレベルを取得 |
| | `set_level` | プレイヤーのレベルを設定 |
| ステータス | `get_stat` | ステータス値を取得 |
| | `set_stat` | ステータス値を設定 |
| | `get_available_points` | 利用可能ステータスポイントを取得 |
| | `add_stat_point` | ステータスポイントを配分 |
| クラス | `get_class` | 現在のクラスを取得 |
| | `set_class` | クラスを設定 |
| | `try_change_class` | 条件チェック付きクラス変更 |
| | `can_change_class` | クラス変更可能か確認 |
| | `upgrade_class` | クラスをアップグレード |
| | `can_upgrade_class` | クラスアップグレード可能か確認 |
| スキル | `has_skill` | スキルを習得しているか確認 |
| | `unlock_skill` | スキルを習得 |
| | `unlock_skill_with_points` | スキルポイント消費でスキル習得 |
| | `cast_skill` | スキルを発動 |
| | `get_skill_level` | スキルレベルを取得 |
| | `get_skill_points` | スキルポイントを取得 |
| | `add_skill_points` | スキルポイントを付与 |
| 経済 | `get_gold` | ゴールド残高を取得 |
| | `give_gold` | ゴールドを付与 |
| | `take_gold` | ゴールドを剥奪 |
| | `has_gold` | ゴールドを持っているか確認 |
| | `transfer_gold` | ゴールドを移転 |
| ダメージ | `calculate_damage` | ダメージを計算 |

### `/rpg api <action> <args...>`
`call`を省略した形式でアクションを直接実行します。

```
/rpg api get_level Steve
```

---

## 権限

| 権限 | 説明 | デフォルト |
|-----|------|----------|
| `rpg.use` | RPGコマンドの使用 | 全プレイヤー |
| `rpg.admin` | 管理者コマンドの使用 | OPのみ |
| `rpgplugin.api` | APIコマンドの使用（Skript/Denizen専用） | なし（プレイヤーは使用不可） |

---

## Tab補完機能

すべてのコマンドでTabキーによる補完が利用可能です：

- **第一引数**: サブコマンドの補完
- **スキルID**: `/rpg cast` で習得可能なスキルID
- **クラスID**: `/rpg class` で利用可能なクラスID
- **オークションID**: アクティブなオークションのID
- **プレイヤー名**: `/rpg trade request` でオンラインプレイヤー名
- **ステータス**: APIコマンドでSTR/INT/SPI/VIT/DEX

---

## 使用例

### 初期セットアップ
```
/rpg class          # クラスを選択
/rpg stats          # ステータスを振り分ける
/rpg skill          # スキルを習得
```

### 日々のプレイ
```
/rpg cast fireball  # スキルを発動
/rpg balance        # 残高を確認
/rpg auction list   # オークションを確認
```

### トレード
```
/rpg trade request Steve  # トレード申請
# Steveが以下を実行:
/rpg trade accept         # トレード承認
```

---

*最終更新: 2026-01-08*
