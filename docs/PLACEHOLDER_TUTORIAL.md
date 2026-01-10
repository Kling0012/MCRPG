# PlaceholderAPI 活用チュートリアル

> **初心者開発者向け**
> **最終更新**: 2026-01-09
> **バージョン**: 1.0.0

---

RPGPluginのプレースホルダーを使って、サーバー全体にRPG情報を表示するチュートリアルです。

## 目次

1. [PlaceholderAPI基礎](#placeholderapi基礎)
2. [基本のプレースホルダー](#基本のプレースホルダー)
3. [スコアボードに表示](#スコアボードに表示)
4. [チャットに表示](#チャットに表示)
5. [TABに表示](#tabに表示)
6. [実践:各種プラグイン設定](#実践各種プラグイン設定)
7. [トラブルシューティング](#トラブルシューティング)

---

## PlaceholderAPI基礎

### PlaceholderAPIとは？

Minecraftサーバーで情報を表示するためのプレースホルダー（変数のようなもの）を提供するプラグインです。

**基本的な使い方:**
- `%player%` → プレイヤー名
- `%health%` → 現在のHP
- `%world%` → 現在のワールド名

**RPGPluginの場合:**
- `%rpg_level%` → RPGレベル
- `%rpg_stat_STR%` → STR値
- など...

### インストールと確認

```
1. PlaceholderAPI.jar を plugins/ に配置
2. サーバー再起動
3. /papi version で確認
```

### プレースホルダーのテスト

```
/papi parse %rpg_level%
```

プレイヤーのRPGレベルが表示されればOK！

---

## 基本のプレースホルダー

### プレイヤー情報

| プレースホルダー | 説明 | 例 |
|-----------------|------|-----|
| `%rpg_level%` | レベル | `25` |

### ステータス

| プレースホルダー | 説明 | 例 |
|-----------------|------|-----|
| `%rpg_stat_STR%` | STR（力） | `75` |
| `%rpg_stat_INT%` | INT（知力） | `50` |
| `%rpg_stat_SPI%` | SPI（精神） | `30` |
| `%rpg_stat_VIT%` | VIT（体力） | `60` |
| `%rpg_stat_DEX%` | DEX（器用さ） | `45` |

### クラス情報

| プレースホルダー | 説明 | 例 |
|-----------------|------|-----|
| `%rpg_class%` | クラスID | `warrior` |
| `%rpg_class_name%` | クラス表示名 | `戦士` |
| `%rpg_class_rank%` | クラスランク | `2` |

### 経済情報

| プレースホルダー | 説明 | 例 |
|-----------------|------|-----|
| `%rpg_gold%` | ゴールド残高 | `1234.56` |

### スキル情報

| プレースホルダー | 説明 | 例 |
|-----------------|------|-----|
| `%rpg_skill_points%` | スキルポイント | `5` |
| `%rpg_available_points%` | 利用可能ステータスポイント | `10` |
| `%rpg_skill_level_fireball%` | 特定スキルのレベル | `3` |

### HP/MP情報

| プレースホルダー | 説明 | 例 |
|-----------------|------|-----|
| `%rpg_max_hp%` | 最大HP | `250` |
| `%rpg_max_mana%` | 最大MP | `150` |
| `%rpg_mana%` | 現在MP | `120` |

---

## スコアボードに表示

### FeatherBoard

**インストール:**
1. FeatherBoard をインストール
2. `plugins/FeatherBoard/boards/` に設定ファイルを作成

**基本設定:**

```yaml
# plugins/FeatherBoard/boards/rpg_stats.txt
rpg_stats:
  # タイトル（サイドバー）
  title: "&c&lRPGステータス"

  # 更新間隔（ティック）
  update-interval: 10

  # 表示行
  rows:
    - "&e━━━━━━━━━━━━━━━━━━"
    - "&eプレイヤー"
    - "&f%player%"
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
    - "&6所持金:"
    - "&e%rpg_gold% G"
    - "&e━━━━━━━━━━━━━━━━━━"
```

**リロード:**
```
/fb reload
```

### Scoreboard_revised

```yaml
# plugins/Scoreboard/config/display.yml
title: "&c&lRPG Stats"
rows:
  - "&e%player%"
  - ""
  - "&cLv%rpg_level% %rpg_class_name%"
  - ""
  - "&c❤ %rpg_max_hp%"
  - "&b✦ %rpg_max_mana%"
  - ""
  - "&6%rpg_gold% G"
```

### ASkyBlock / BSkyBlock

```yaml
# plugins/Scoreboard/config.yml
scoreboard:
  lines:
    - "&cRPG Stats"
    - " "
    - "&cLevel: &f%rpg_level%"
    - "&cSTR: &f%rpg_stat_STR%"
    - "&cINT: &f%rpg_stat_INT%"
    - "&cVIT: &f%rpg_stat_VIT%"
    - "&cDEX: &f%rpg_stat_DEX%"
    - " "
    - "&cGold: &f%rpg_gold%"
```

---

## チャットに表示

### EssentialsX Chat

**設定ファイル:**

```yaml
# plugins/Essentials/chat.yml

# グループチャットフォーマット
group-formats:
  default:
    format: '&7[Lv%rpg_level%&7] &f%rpg_class_name% %display%&7: &f%message%'
  admin:
    format: '&c[Admin][Lv%rpg_level%&c] &f%rpg_class_name% %display%&c: &f%message%'
```

**再読み込み:**
```
/ess reload
```

### CMI

```yaml
# plugins/CPI/config.yml

chat:
  format: "&7[Lv%rpg_level%&7] &f%rpg_class_name% %player%: &f%message%"
```

### UltimateChat

```gui-format
name: RPG Format
format: '&7[Lv%rpg_level%&7] &f%rpg_class_name% %player%&7: &f%message%'
```

---

## TABに表示

### TABプラグイン

**基本設定:**

```yaml
# plugins/TAB/config.yml

# プレイヤーリストのフォーマット
  - playerline: "&eLv.%rpg_level% &7%player% | &6%rpg_class_name%"

# カスタムタブで複雑な表示
per-world:
  world:
    - playerline: "&e%rpg_level% &7%player% &6%rpg_class_name% &c%rpg_stat_STR%STR &a%rpg_stat_INT%INT"
```

**ヘッダー・フッター:**

```yaml
# plugins/TAB/config.yml
header-footer:
  header:
    - "&e===================================="
    - "&6       RPG Server"
    - "&eあなた: %rpg_class_name% &7Lv.%rpg_level%"
    - "&e===================================="
  footer:
    - "&6所持金: %rpg_gold% G"
    - "&aOnline: &online%"
    - "&e===================================="
```

**リロード:**
```
/tab reload
```

---

## 実践:各種プラグイン設定

### DeluxeMenu - RPGメニュー

```yaml
# plugins/DeluxeMenus/rpg_menu.yml
menu_title: "&c&lRPGメニュー"
open_command: /rpgmenu
size: 45

items:
  'stats':
    material: DIAMOND_SWORD
    slot: 11
    display_name: "&eステータス"
    lore:
      - "&cSTR: %rpg_stat_STR%"
      - "&aINT: %rpg_stat_INT%"
      - "&bSPI: %rpg_stat_SPI%"
      - "&dVIT: %rpg_stat_VIT%"
      - "&eDEX: %rpg_stat_DEX%"
      - ""
      - "&eクリックで詳細を確認"
    left_click_commands:
      - "[player] /rpg stats"

  'class':
    material: GOLDEN_CHESTPLATE
    slot: 13
    display_name: "&6クラス"
    lore:
      - "&f現在: %rpg_class_name%"
      - "&fランク: %rpg_class_rank%"
      - ""
      - "&eクリックで詳細を確認"
    left_click_commands:
      - "[player] /rpg class"

  'skills':
    material: ENCHANTED_BOOK
    slot: 15
    display_name: "&bスキル"
    lore:
      - "&fスキルポイント: %rpg_skill_points%"
      - "&f残りポイント: %rpg_available_points%"
      - ""
      - "&eクリックで詳細を確認"
    left_click_commands:
      - "[player] /rpg skill"

  'gold':
    material: GOLD_INGOT
    slot: 20
    display_name: "&6所持金"
    lore:
      - "&f%rpg_gold% G"
      - ""
      - "&eクリックで取引を開く"
    left_click_commands:
      - "[player] /rpg balance"

  'info':
    material: BOOK
    slot: 22
    display_name: "&e情報"
    lore:
      - "&fレベル: &cLv%rpg_level%"
      - "&f最大HP: &c%rpg_max_hp%"
      - "&f最大MP: &b%rpg_max_mana%"
      - "&f現在MP: &b%rpg_mana%/%rpg_max_mana%"
```

### AnimatedScoreboard - アニメーション

```xml
<!-- plugins/AnimatedScoreboard/scores/rpg.xml -->
<scoreboard>
  <title>&c&lRPG Stats</title>
  <scroll>true</scroll>
  <interval>5</interval>

  <line>
    <text>&e━━━━━━━━━━━━━━━━━━</text>
  </line>
  <line>
    <text>&eプレイヤー: &f%player%</text>
  </line>
  <line>
    <text></text>
  </line>
  <line>
    <text>&cレベル: &fLv%rpg_level%</text>
  </line>
  <line>
    <text>&6クラス: &f%rpg_class_name%</text>
  </line>
  <line>
    <text></text>
  </line>
  <line>
    <text>&cSTR: &f%rpg_stat_STR%</text>
  </line>
  <line>
    <text>&aINT: &f%rpg_stat_INT%</text>
  </line>
  <line>
    <text>&bSPI: &f%rpg_stat_SPI%</text>
  </line>
  <line>
    <text>&dVIT: &f%rpg_stat_VIT%</text>
  </line>
  <line>
    <text>&eDEX: &f%rpg_stat_DEX%</text>
  </line>
  <line>
    <text></text>
  </line>
  <line>
    <text>&6所持金: &e%rpg_gold% G</text>
  </line>
  <line>
    <text>&e━━━━━━━━━━━━━━━━━━</text>
  </line>
</scoreboard>
```

### BossMessaging - BossBar表示

```yaml
# plugins/BossMessage/messages/rpg_status.yml
name: rpg_status
enabled: true
interval: 10
messages:
  - "&c&l%rpg_class_name% &f| &eLv.%rpg_level% &f| &6%rpg_gold%G"
  - "&cHP: %rpg_max_hp% &b| &bMP: %rpg_mana%/%rpg_max_mana%"
```

### HolographicDisplays - ホログラム

```yaml
# plugins/HolographicDisplays/ holograms/rpg_info.yml
test_hologram:
  - " &c&l========== RPGステータス =========="
  - " &eプレイヤー: &f%player%"
  - " &cレベル: &fLv.%rpg_level%"
  - " &6クラス: &f%rpg_class_name%"
  - ""
  - " &cSTR: &f%rpg_stat_STR%  &aINT: &f%rpg_stat_INT%"
  - " &bSPI: &f%rpg_stat_SPI%  &dVIT: &f%rpg_stat_VIT%"
  - " &eDEX: &f%rpg_stat_DEX%"
  - ""
  - " &6所持金: &f%rpg_gold% G"
```

### ChatFormat - チャット枠

```yaml
# plugins/ChatFormat/formats/rpg.yml
rpg_format:
  format: "&7[&cLv%rpg_level%&7] &f%rpg_class_name% &f%player%&7: &f%message%"
```

---

## 形式付き表示

### 数字のフォーマット

```
# カンマ区切り
/formatted_gold: $number_format{%rpg_gold%#,###}

# 小数点以下2桁
/formatted_gold: $number_format{%rpg_gold%#.00}

# カンマ + 小数点
/formatted_gold: $number_format{%rpg_gold%#,###.00}
```

例: `1234567.89` → `1,234,567.89`

---

## トラブルシューティング

### よくある問題

**Q: プレースホルダーが更新されない**

A: 以下を確認してください：
1. `/papi list` でRPGPlugin拡張が表示されるか
2. PlaceholderAPIが最新版か
3. `/papi ecloud refresh` を実行

**Q: 値が0やnullのまま**

A:
1. プレイヤーがログインしているか
2. `/rpg info` でデータが存在するか確認
3. 一度ログアウト/ログイン

**Q: 日本語が文字化けする**

A: YAMLファイルのエンコーディングを確認：
- UTF-8で保存されているか
- BOMなしUTF-8で保存

**Q: スコアボードが表示されない**

A:
1. スコアボードプラグインの優先度を確認
2. 他のスコアボードプラグインを無効化
3. `/fb toggle` でオン/オフ切り替え

### デバッグコマンド

```
# プレースホルダーをパースしてテスト
/papi parse %rpg_level%
/papi parse %rpg_stat_STR%
/papi parse %rpg_class%

# 拡張機能の一覧を表示
/papi list

# プレイヤーの情報を確認
/papi info %player%

# RPG拡張の情報を確認
/papi info rpg
```

---

## まとめ

このチュートリアルで学んだこと:

1. **基本プレースホルダー**: レベル、ステータス、クラス、ゴールド
2. **スコアボード**: FeatherBoardでRPG情報を表示
3. **チャット**: EssentialsX Chatでレベル・クラスを表示
4. **TAB**: プレイヤーリストに情報を追加
5. **各種プラグイン**: DeluxeMenu、AnimatedScoreboard、BossMessage

### 次のステップ

- **[クイックスタートガイド](QUICKSTART.md)** - 全体像を把握
- **[SKriptチュートリアル](SKRIPT_TUTORIAL.md)** - イベント処理を学ぶ
- **[プレースホルダー](PLACEHOLDERS.md)** - 全リファレンス

### サポート

- **PlaceholderAPI公式**: https://www.spigotmc.org/resources/placeholderapi.6245/
- ** FeatherBoard**: https://www.spigotmc.org/resources/featherboard.20831/
- **TAB**: https://www.spigotmc.org/resources/tab.57842/

---

**Happy Displaying! 📊**
