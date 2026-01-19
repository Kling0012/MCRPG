# PlaceholderAPI プレースホルダーリファレンス

> **作業ブランチ**: main
> **最終更新**: 2026-01-09
> **バージョン**: 1.0.0

---

## 目次

1. [概要](#概要)
2. [セットアップ](#セットアップ)
3. [プレースホルダー一覧](#プレースホルダー一覧)
4. [使用例](#使用例)
5. [トラブルシューティング](#トラブルシューティング)

---

## 概要

RPGPluginはPlaceholderAPIと連携し、RPGシステムの情報をプラグイン全体で利用可能なプレースホルダーとして提供します。

### 主な機能

- **レベル系**: レベル情報
- **ステータス系**: 5種類のステータス値
- **クラス系**: クラスID、表示名、ランク
- **スキル系**: スキルポイント、スキルレベル
- **その他**: HP/MP関連

---

## セットアップ

### 必要プラグイン

1. **RPGPlugin** (必須)
2. **PlaceholderAPI 2.11.6+** (必須)

### 確認方法

サーバーコンソールに以下が表示されます：

```
[RPGPlugin] Setting up third-party integrations...
[RPGPlugin] PlaceholderAPI integration loaded successfully!
[RPGPlugin] Third-party integrations setup complete!
```

### PlaceholderAPIコマンド

プレースホルダーが正常に動作しているか確認：

```
/papi parse %rpg_level%
```

---

## プレースホルダー一覧

### 基本情報

| プレースホルダー | 説明 | 例 |
|-----------------|------|-----|
| `%rpg_level%` | プレイヤーレベル | `25` |

### ステータス

| プレースホルダー | 説明 | 例 |
|-----------------|------|-----|
| `%rpg_stat_STR%` | STR（力） | `75` |
| `%rpg_stat_STRENGTH%` | STR（完全形） | `75` |
| `%rpg_stat_INT%` | INT（知力） | `50` |
| `%rpg_stat_INTELLIGENCE%` | INT（完全形） | `50` |
| `%rpg_stat_SPI%` | SPI（精神） | `30` |
| `%rpg_stat_SPIRIT%` | SPI（完全形） | `30` |
| `%rpg_stat_VIT%` | VIT（体力） | `60` |
| `%rpg_stat_VITALITY%` | VIT（完全形） | `60` |
| `%rpg_stat_DEX%` | DEX（器用さ） | `45` |
| `%rpg_stat_DEXTERITY%` | DEX（完全形） | `45` |

### ステータス一括

| プレースホルダー | 説明 | 例 |
|-----------------|------|-----|
| `%rpg_stats%` | 全ステータス（フォーマット済み） | `STR:75 INT:50 SPI:30 VIT:60 DEX:45` |

### クラス

| プレースホルダー | 説明 | 例 |
|-----------------|------|-----|
| `%rpg_class%` | クラスID | `warrior` |
| `%rpg_class_name%` | クラス表示名 | `戦士` |
| `%rpg_class_rank%` | クラスランク | `2` |

### スキル

| プレースホルダー | 説明 | 例 |
|-----------------|------|-----|
| `%rpg_skill_points%` | スキルポイント | `5` |
| `%rpg_available_points%` | 利用可能ステータスポイント | `10` |
| `%rpg_skill_level_<skill>%` | スキルレベル | `%rpg_skill_level_fireball%` → `3` |

### HP/MP

| プレースホルダー | 説明 | 例 |
|-----------------|------|-----|
| `%rpg_max_hp%` | 最大HP | `250` |
| `%rpg_max_health%` | 最大HP（別名） | `250` |
| `%rpg_max_mana%` | 最大MP | `150` |
| `%rpg_mana%` | 現在MP | `120` |

---

## 使用例

### Scoreboard（スコアボード）

```yaml
# plugins/FeatherBoard/boards/rpg.txt
rpg_stats:
  title: "&c&lRPGステータス"
  rows:
    - "&eレベル: &f%rpg_level%"
    - ""
    - "&cSTR: &f%rpg_stat_STR%"
    - "&aINT: &f%rpg_stat_INT%"
    - "&bSPI: &f%rpg_stat_SPI%"
    - "&dVIT: &f%rpg_stat_VIT%"
    - "&eDEX: &f%rpg_stat_DEX%"
    - ""
    - "&6クラス: &f%rpg_class_name%"
    - "&6ランク: &f%rpg_class_rank%"
```

### ChatFormat（チャットフォーマット）

```yaml
# plugins/Essentials/chat.yml
format: '&7[%rpg_level%&7] %rpg_class_name% &f%display% %suffix%'
```

### TAB（タブリスト）

```yaml
# plugins/TAB/config.yml
  - playerline: "&e%rpg_level% &7%player%|&6%rpg_class_name%|&c❤ %rpg_max_hp%|&b✦ %rpg_max_mana%"
```

### BossBar

```yaml
# plugins/BossMessage/config.yml
messages:
  - text: "&eLv.%rpg_level% %rpg_class_name% | HP: %rpg_max_hp% | MP: %rpg_mana%/%rpg_max_mana%"
    condition: "%rpg_level% > 1"
```

### アニメーション（AnimatedScoreboard）

```xml
<text>
  <line id="level" interval="2">&eレベル: &f%rpg_level%</line>
  <line id="class" interval="2">&6クラス: &f%rpg_class_name%</line>
</text>
```

### DeluxeMenu（メニュー）

```yaml
# plugins/DeluxeMenus/menu.yml
menu_title: "&cRPGメニュー"
items:
  'stats':
    material: BOOK
    slot: 0
    display_name: "&eステータス"
    lore:
      - "&cSTR: %rpg_stat_STR%"
      - "&aINT: %rpg_stat_INT%"
      - "&bSPI: %rpg_stat_SPI%"
      - "&dVIT: %rpg_stat_VIT%"
      - "&eDEX: %rpg_stat_DEX%"
```

---

## トラブルシューティング

### よくある問題

#### Q: プレースホルダーが更新されない

A: 以下を確認してください：
1. PlaceholderAPIが最新版かどうか
2. `/papi ecloud refresh` を実行
3. サーバーを再起動

#### Q: プレースホルダーが `%rpg_level%` のまま表示される

A: 以下を確認してください：
1. PlaceholderAPIがインストールされているか
2. `/papi list` でRPGPlugin拡張が表示されるか
3. プレイヤーがオンラインかどうか

#### Q: 値が0やnullになる

A: 以下を確認してください：
1. プレイヤーがRPGシステムに登録されているか
2. 一度ログアウト/ログインしてみる
3. `/rpg info` でデータが存在するか確認

### デバッグコマンド

```
# プレースホルダーをパースしてテスト
/papi parse %rpg_level%
/papi parse %rpg_stat_STR%
/papi parse %rpg_class%

# 拡張機能のリストを表示
/papi list

# 拡張機能の情報を表示
/papi info rpg
```

---

## プラグイン別設定例

### FeatherBoard

```yaml
scoreboard:
  - rpg_info:
      title: "&c&l%player%"
      rows:
        - "&e━━━━━━━━━━━━━━"
        - "&eレベル: &f%rpg_level%"
        - "&6クラス: &f%rpg_class_name%"
        - ""
        - "&cSTR: &f%rpg_stat_STR%"
        - "&aINT: &f%rpg_stat_INT%"
        - "&bSPI: &f%rpg_stat_SPI%"
        - "&dVIT: &f%rpg_stat_VIT%"
        - "&eDEX: &f%rpg_stat_DEX%"
        - ""
        - "&e━━━━━━━━━━━━━━"
```

### TAB

```yaml
header-footer:
  header:
    - "&eRPG Server &7| &fVer 1.0"
    - "&eあなた: &f%rpg_class_name% &7Lv.%rpg_level%"
  footer:
    - "&aOnline: &f%online%"
```

### EssentialsX Chat

```yaml
chat:
  format: '&7[%rpg_level%&7] &f%rpg_class_name% %display%&7: &f%message%'
```

---

## サポート

バグ報告や機能リクエストは、GitHub Issuesまでお願いしてください。

---

## API ドキュメント

- [SKript統合ガイド](SKRIPT_INTEGRATION.md)
- [APIドキュメント](API_DOCUMENTATION.md)
- [YAMLリファレンス](YAML_REFERENCE.md)
