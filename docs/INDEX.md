# RPGPlugin ドキュメント

> **Minecraft Java用RPGプラグイン**
> **バージョン**: 1.0.0
> **最終更新**: 2026-01-09
> **作業ブランチ**: main

---

## ドキュメント構成

| ドキュメント | 説明 | 対象 |
|-------------|------|------|
| **[クイックスタート](QUICKSTART.md)** | 5分で始める開発者向けガイド | 初心者開発者 |
| **[SKriptチュートリアル](SKRIPT_TUTORIAL.md)** | SKript活用チュートリアル | 初心者開発者 |
| **[PlaceholderAPIチュートリアル](PLACEHOLDER_TUTORIAL.md)** | プレースホルダー活用チュートリアル | 初心者開発者 |
| **[APIリファレンス](API.md)** | 外部APIの完全なリファレンス（Java/SKript/Denizen） | 開発者 |
| **[APIドキュメント](API_DOCUMENTATION.md)** | 詳細APIドキュメントとPhase11新機能 | 開発者 |
| **[SKript統合ガイド](SKRIPT_INTEGRATION.md)** | ネイティブSKript式・条件・効果・イベント | 開発者 |
| **[プレースホルダー](PLACEHOLDERS.md)** | PlaceholderAPI プレースホルダーリファレンス | 開発者 |
| **[コマンドリファレンス](COMMANDS.md)** | すべてのコマンドと使用方法 | 管理者・プレイヤー |
| **[システム仕様書](SPEC.md)** | システムアーキテクチャと仕様 | 開発者 |
| **[スキルシステムV2](SKILL_SYSTEM_V2.md)** | スキルシステムの全体像 | 開発者 |
| **[YAMLリファレンス](YAML_REFERENCE.md)** | スキル/クラスYAMLフォーマット | 開発者 |
| **[チュートリアル](../TUTORIAL.md)** | サーバー管理者・プレイヤー・開発者向けガイド | 全員 |

---

## クイックリファレンス

### コマンド一覧

| コマンド | 説明 |
|---------|------|
| `/rpg` | メインメニュー |
| `/rpg stats` | ステータスGUI |
| `/rpg skill` | スキルGUI |
| `/rpg cast <ID>` | スキル発動 |
| `/rpg class [ID]` | クラス管理 |
| `/rpg balance` | 残高確認 |
| `/rpg auction` | オークション |
| `/rpg trade` | トレード |
| `/rpg reload` | 設定リロード(OP) |

### API主要メソッド（Java）

| カテゴリ | メソッド | 説明 |
|---------|---------|------|
| プレイヤー | `getLevel(Player)` | レベル取得 |
| ステータス | `getStat(Player, Stat)` | ステータス取得 |
| クラス | `setClass(Player, String)` | クラス設定 |
| スキル | `castSkill(Player, String)` | スキル発動 |
| 経済 | `getGoldBalance(Player)` | 残高取得 |

### SKriptネイティブ要素

| カテゴリ | 構文 | 説明 |
|---------|------|------|
| 式 | `rpg level of %player%` | レベル取得 |
| 式 | `rpg stat[e] %string% of %player%` | ステータス取得 |
| 式 | `rpg class of %player%` | クラスID取得 |
| 式 | `rpg skill level of %string% [from] %player%` | スキルレベル取得 |
| 式 | `rpg gold of %player%` | ゴールド取得 |
| 条件 | `%player% has [the] rpg skill [named] %string%` | スキル習得確認 |
| 条件 | `%player% can upgrade [their] rpg class` | クラスアップ可否 |
| 条件 | `%player% has %number% rpg gold` | ゴールド所持確認 |
| 条件 | `%player%'s rpg stat[e] %string% is [at least] %number%` | ステータス比較 |
| 条件 | `%player%'s rpg class is %string%` | クラス確認 |
| 効果 | `give %number% rpg gold to %player%` | ゴールド付与 |
| 効果 | `take %number% rpg gold from %player%` | ゴールド剥奪 |
| 効果 | `unlock [the] rpg skill %string% [for] %player%` | スキル習得 |
| 効果 | `make %player% cast [the] rpg skill %string%` | スキル発動 |
| 効果 | `set [the] rpg class of %player% to %string%` | クラス設定 |
| 効果 | `add %number% [to] rpg stat[e] %string% of %player%` | ステータス追加 |
| イベント | `on rpg skill cast` | スキル発動イベント |
| イベント | `on rpg skill cast [of] %string%` | 特定スキル発動イベント |

### PlaceholderAPI プレースホルダー

| カテゴリ | プレースホルダー | 説明 |
|---------|-----------------|------|
| 基本 | `%rpg_level%` | レベル |
| ステータス | `%rpg_stat_STR%` | STR値 |
| ステータス | `%rpg_stat_INT%` | INT値 |
| ステータス | `%rpg_stat_SPI%` | SPI値 |
| ステータス | `%rpg_stat_VIT%` | VIT値 |
| ステータス | `%rpg_stat_DEX%` | DEX値 |
| ステータス | `%rpg_stats%` | 全ステータス（フォーマット済み） |
| クラス | `%rpg_class%` | クラスID |
| クラス | `%rpg_class_name%` | クラス表示名 |
| クラス | `%rpg_class_rank%` | クラスランク |
| 経済 | `%rpg_gold%` | ゴールド残高 |
| スキル | `%rpg_skill_points%` | スキルポイント |
| スキル | `%rpg_available_points%` | 利用可能ステータスポイント |
| スキル | `%rpg_skill_level_<skill>%` | スキルレベル |
| HP/MP | `%rpg_max_hp%` | 最大HP |
| HP/MP | `%rpg_max_mana%` | 最大MP |
| HP/MP | `%rpg_mana%` | 現在MP |

### APIコマンド一覧（SKript/Denizen）

| アクション | 説明 |
|-----------|------|
| `get_level` | レベル取得 |
| `set_level` | レベル設定 |
| `get_stat` | ステータス取得 |
| `set_stat` | ステータス設定 |
| `get_class` | クラス取得 |
| `set_class` | クラス設定 |
| `upgrade_class` | クラスアップ |
| `has_skill` | スキル習得確認 |
| `unlock_skill` | スキル習得 |
| `cast_skill` | スキル発動 |
| `get_skill_level` | スキルレベル取得 |
| `get_gold` | ゴールド取得 |
| `give_gold` | ゴールド付与 |
| `take_gold` | ゴールド剥奪 |
| `has_gold` | ゴールド所持確認 |
| `transfer_gold` | ゴールド転送 |
| `calculate_damage` | ダメージ計算 |
| `cast_at` | ターゲット指定スキル |
| `get_entities_in_area` | 範囲エンティティ取得 |

---

## Phase11 機能一覧

### Phase11-4: ターゲットパッケージ

| ターゲットタイプ | 説明 | パラメータ |
|-----------------|------|-----------|
| `cone` | コーン状範囲 | `angle`, `range` |
| `sphere` | 球形範囲 | `radius`, `max_targets` |
| `sector` | 扇形範囲 | `angle`, `radius` |
| `self` | 自分自身 | なし |
| `area` | 指定座標範囲 | `shape`, `radius` |

### Phase11-5: 外部API拡張

```java
// ターゲット指定スキル実行
boolean castSkill(Player caster, String skillId, Entity target);

// 座標指定スキル実行
boolean castSkillAtLocation(Player caster, String skillId, Location location);

// ターゲットリスト指定スキル実行
boolean castSkillWithTargets(Player caster, String skillId, List<Entity> targets);

// 範囲内ターゲット取得
List<Entity> getTargetsInArea(Player caster, TargetType type, AreaShape shape);
```

### Phase11-6: 数式ベースダメージ計算

#### 組み込み変数

| 変数 | 説明 | 例 |
|------|------|-----|
| `STR`, `INT`, `SPI`, `VIT`, `DEX` | ステータス値 | `50` |
| `Lv` | スキルレベル | `5` |
| `HP`, `HP_max`, `HP_ratio` | HP関連 | `100`, `200`, `0.5` |
| `MP`, `MP_max` | MP関連 | `50`, `100` |

#### 演算子・関数

| 種別 | 内容 |
|------|------|
| 演算子 | `+`, `-`, `*`, `/`, `%`, `^` |
| 関数 | `floor()`, `ceil()`, `round()`, `abs()`, `min()`, `max()`, `sqrt()`, `pow()` |
| 条件 | `a < b`, `a <= b`, `a > b`, `a >= b`, `a == b`, `a != b`, `a && b`, `a \|\| b`, `!a` |

---

## 索引

### あ行

| 項目 | 説明 | 関連 |
|------|------|------|
| アーキテクチャ | システム設計 | SPEC |
| API | 外部連携インターフェース | API |
| APIコマンド | SKript/Denizen連携コマンド | API |
| アクティブスキル | 手動発動スキル | SPEC |
| アイテム入札 | オークションシステム | COMMANDS |
| アクション | RPG APIコマンドの種類 | API |
| エンティティフィルター | ターゲット選択フィルター | SPEC |
| 演算子 | 数式の演算子 | SPEC |
| オークション | アイテム入札システム | COMMANDS |
| 依存関係 | プラグイン依存 | SPEC |
| オフラインプレイヤー | L2キャッシュ | SPEC |

### か行

| 項目 | 説明 | 関連 |
|------|------|------|
| キャッシュ | 3層キャッシュ戦略 | SPEC |
| クラス | RPGクラスシステム | COMMANDS, SPEC |
| クラスアップ | 上位クラスへの変更 | COMMANDS |
| クールダウン | スキルクールダウン | SPEC |
| 経験値減衰 | Lv30以上の経験値軽減 | SPEC |
| 権限 | コマンド権限 | COMMANDS |
| 関数 | 数式評価関数 | SPEC |
| コストタイプ | MP/HP/STAMINA | SPEC |

### さ行

| 項目 | 説明 | 関連 |
|------|------|------|
| スキル | アクティブ/パッシブ | COMMANDS, SPEC |
| スキルツリー | スキル習得ツリー | SPEC |
| スキルポイント | スキル習得に使用 | TUTORIAL |
| ステータス | STR/INT/SPI/VIT/DEX | COMMANDS, SPEC |
| ステータスポイント | レベルアップで付与 | TUTORIAL |
| 設定ファイル | config.yml | SPEC |
| 数式エバリュエーター | ダメージ計算式 | SPEC |

### た行

| 項目 | 説明 | 関連 |
|------|------|------|
| ダメージシステム | ステータス依存ダメージ | SPEC |
| ダメージタイプ | PHYSICAL/MAGIC/TRUE | SPEC |
| データベース | SQLite永続化 | SPEC |
| デバフ | 状態異常効果 | SPEC |
| デバッグモード | 詳細ログ出力 | TUTORIAL |
| トレード | プレイヤー間取引 | COMMANDS |
| Tab補完 | コマンド補完 | COMMANDS |
| ターゲットタイプ | cone/sphere/sector/self/area | SPEC |
| ターゲットフィルター | hostile/not_self/alive | SPEC |
| 条件演算子 | 三項演算子 | SPEC |
| テンプレート | スキル/クラス定義テンプレート | API_DOCUMENTATION |

### な行

| 項目 | 説明 | 関連 |
|------|------|------|
| ホットリロード | YAMLファイル自動読み込み | SPEC |
| ノックバック | 追加効果 | SPEC |

### は行

| 項目 | 説明 | 関連 |
|------|------|------|
| パッケージ構造 | ソースコード構成 | SPEC |
| バフ | ステータス強化効果 | SPEC |
| パッシブスキル | 常時効果スキル | SPEC |
| PlaceholderAPI | プレースホルダー連携 | SPEC, TUTORIAL |
| ビルドパターン | Facade/Strategy/Observer | SPEC |

### ま行

| 項目 | 説明 | 関連 |
|------|------|------|
| MythicMobs | モブドロップ連携 | SPEC |
| マイグレーション | データベース移行 | SPEC |
| 命中率 | DEX依存 | SPEC |

### や行

| 項目 | 説明 | 関連 |
|------|------|------|
| UUID | プレイヤー識別子 | SPEC |
| ユーティリティ関数 | floor/ceil/round/abs | SPEC |

### ら行

| 項目 | 説明 | 関連 |
|------|------|------|
| ライフスティール | 与ダメージ回復 | SPEC |
| リロード | 設定再読み込み | COMMANDS, SPEC |
| レベルアップ | ステータスポイント付与 | SPEC |
| レベル依存パラメータ | cost/cooldown/damage | SPEC |
| 連携 | SKript/Denizen連携 | API |

### わ行

| 項目 | 説明 | 関連 |
|------|------|------|
| ワールドポイント | 手動配分ポイント | TUTORIAL |

---

## クラス図（簡易版）

```
┌─────────────────────────────────────────────────────────────┐
│                        RPGPlugin                            │
│                      (Main / Facade)                        │
├─────────────────────────────────────────────────────────────┤
│  + getInstance(): RPGPlugin                                 │
│  + getAPI(): RPGPluginAPI                                   │
│  + getPlayerManager(): PlayerManager                        │
│  + getStatManager(): StatManager                            │
│  + getClassManager(): ClassManager                          │
│  + getSkillManager(): SkillManager                          │
│  + getCurrencyManager(): CurrencyManager                    │
└─────────────────────────────────────────────────────────────┘
                           │
           ┌───────────────┼───────────────┐
           ▼               ▼               ▼
    ┌──────────┐    ┌──────────┐    ┌──────────┐
    │ Player   │    │  Skill   │    │  Class   │
    │ Manager  │    │ Manager  │    │ Manager  │
    └──────────┘    └──────────┘    └──────────┘
         │               │               │
         ▼               ▼               ▼
    ┌──────────┐    ┌──────────┐    ┌──────────┐
    │ RPGPlayer │    │  Skill   │    │ RPGClass │
    └──────────┘    └──────────┘    └──────────┘
```

---

## システムフロー図

### ダメージ計算フロー

```
攻撃イベント
    │
    ▼
┌─────────────────┐
│ 攻撃者情報取得   │
│ - RPGPlayer     │
│ - ステータス    │
│ - 装備         │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 基本ダメージ計算 │
│ - 武器ダメージ  │
│ - ステータス補正│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ ダメージ修正     │
│ - スキル効果    │
│ - バフ/デバフ  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 防御計算        │
│ - 防御ステータス│
│ - 軽減率       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 最終ダメージ適用 │
└─────────────────┘
```

### スキル発動フロー

```
/rpg cast <skill_id>
    │
    ▼
┌─────────────────┐
│ スキル習得チェック│
│ - 習得済み?     │
│ - アクティブ?   │
└────────┬────────┘
         │ NO
         ├──────→ エラー終了
         │ YES
         ▼
┌─────────────────┐
│ クールダウンチェック│
│ - クールダウン中?│
└────────┬────────┘
         │ YES
         ├──────→ エラー終了
         │ NO
         ▼
┌─────────────────┐
│ コストチェック    │
│ - MP/HP足りる?  │
└────────┬────────┘
         │ NO
         ├──────→ エラー終了
         │ YES
         ▼
┌─────────────────┐
│ ターゲット選択    │
│ - 範囲計算      │
│ - エンティティ抽出│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ スキル効果適用    │
│ - ダメージ      │
│ - 回復         │
│ - バフ/デバフ  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ クールダウン開始  │
└─────────────────┘
```

---

## 用語集

| 用語 | 説明 |
|------|------|
| RPGPlayer | プラグイン内のプレイヤーデータモデル |
| ステータス | STR/INT/SPI/VIT/DEXの5種類 |
| アクティブスキル | 手動発動するスキル |
| パッシブスキル | 常時効果が発動するスキル |
| クラス | プレイヤーの役割（戦士、魔法使いなど） |
| ランクアップ | クラスの上位版への変更 |
| ゴールド | プラグイン内の通貨 |
| オークション | アイテム入札システム |
| ホットリロード | 再起動なしで設定を反映 |
| ダメージ修正 | ダメージに補正をかける仕組み |
| スキルツリー | スキルの習得条件関係 |
| 経験値減衰 | 高レベルでの経験値軽減 |
| 数式エバリュエーター | YAMLで数式によるダメージ計算 |
| ターゲットシステム | cone/sphere/sector等の範囲攻撃 |
| Phase11 | V2スキルシステムの機能群 |
| レベル依存パラメータ | スキルレベルで変化するcost/cooldown/damage |
| バフ/デバフ | ステータス強化・弱体化効果 |
| エンティティフィルター | ターゲット選択時のフィルタリング |

## 追加履歴

| 日付 | バージョン | 内容 |
|------|----------|------|
| 2026-01-09 | 1.0.0 | ドキュメント作成 |
| | | APIリファレンス作成 |
| | | コマンドリファレンス作成 |
| | | システム仕様書作成 |
| | | Phase11機能統合 |
| | | SKript/Denizen連携詳細追加 |
| | | 索引拡張 |
| 2026-01-09 | 1.1.0 | ネイティブSKript統合追加 |
| | | PlaceholderAPI統合追加 |
| | | SKRIPT_INTEGRATION.md作成 |
| | | PLACEHOLDERS.md作成 |
| | | INDEX更新 |
| 2026-01-09 | 1.2.0 | 初心者開発者向けガイド追加 |
| | | QUICKSTART.md作成 |
| | | SKRIPT_TUTORIAL.md作成 |
| | | PLACEHOLDER_TUTORIAL.md作成 |
| | | INDEX更新 |

---

## プロジェクト情報

- **リポジトリ**: [GitHub](https://github.com/your-repo)
- **ライセンス**: MIT
- **作者**: RPGPlugin Team
- **対応バージョン**: Minecraft 1.20+ (Paper/Spigot)

---

## 関連リンク

- [PaperMC](https://papermc.io/)
- [SpigotMC](https://www.spigotmc.org/)
- [MythicMobs](https://mythiccraft.io/index.php?resources/1/)
- [VaultAPI](https://www.spigotmc.org/resources/vault.34315/)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
