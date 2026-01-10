# RPGPlugin クイックスタートガイド

> **初心者開発者向け**
> **最終更新**: 2026-01-10
> **バージョン**: 1.0.0

---

このガイドでは、RPGPluginを活用して独自のRPG要素を追加したい初心者開発者向けに、すぐに使える内容を説明します。

## 目次

1. [はじめに](#はじめに)
2. [5分でわかる基本](#5分でわかる基本)
3. [PlaceholderAPIで表示する](#placeholderapiで表示する)
4. [次のステップ](#次のステップ)

---

## はじめに

### RPGPluginって何？

MinecraftサーバーにRPG要素（ステータス、スキル、クラス）を追加するプラグインです。

**できること:**
- プレイヤーにレベルとステータスを持たせる
- スキルを習得・発動させる
- クラス（職業）システムを追加
- PlaceholderAPIと連携

### 開発者向けの連携方法

| 方法 | 難易度 | 用途 | 例 |
|------|--------|------|-----|
| **PlaceholderAPI** | ★☆☆ | 情報表示 | スコアボード、チャット |
| **Java API** | ★★★ | 複雑な処理 | 独自プラグイン開発 |

初心者は **PlaceholderAPI** から始めるのがおすすめです。

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
```

**試してみよう:**

```
/papi parse %rpg_level%
```

プレイヤーのレベルが表示されればOKです！

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

## 次のステップ

### もっと学びたい場合

1. **[プレースホルダー](PLACEHOLDERS.md)** - 全プレースホルダー一覧
2. **[APIドキュメント](API_DOCUMENTATION.md)** - Java APIの詳細

### 応用例

| 作りたいもの | 参考セクション |
|-------------|---------------|
| スコアボード表示 | [PlaceholderAPIで表示する](#placeholderapiで表示する) |

### サポート

- **バグ報告**: GitHub Issues
- **ドキュメント**: `docs/` フォルダ

---

**Happy Coding! 🎮**
