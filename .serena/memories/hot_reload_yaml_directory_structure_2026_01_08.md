# ホットリロード可能YAMLファイルのディレクトリ構造とテンプレートセットアップ

## 作成日
2026-01-08

## 作業ブランチ
vk/bea0-

## 概要

プラグインフォルダにホットリロード可能なYAMLファイル用のディレクトリ構造とテンプレートを自動生成する機能を実装しました。

## 実装内容

### 1. 新規クラス: ResourceSetupManager

**ファイル**: `src/main/java/com/example/rpgplugin/core/config/ResourceSetupManager.java`

**機能**:
- プラグイン初期化時に必要なディレクトリ構造を自動生成
- JAR内のテンプレートファイルをプラグインフォルダにコピー
- READMEファイルの自動生成

**メソッド**:
- `setupAllResources()`: 全リソースのセットアップを実行
- `setupTemplateDirectory()`: templates/ディレクトリとテンプレートファイルを作成
- `setupSkillDirectories()`: skills/active/, skills/passive/ を作成
- `setupClassDirectories()`: classes/ を作成
- `setupOtherDirectories()`: mobs/, exp/, data/ を作成

### 2. CoreSystemManagerの更新

**ファイル**: `src/main/java/com/example/rpgplugin/core/system/CoreSystemManager.java`

**変更点**:
- `ResourceSetupManager`フィールドを追加
- `initialize()`メソッドでリソースセットアップを実行
- `getResourceSetupManager()`アクセサメソッドを追加

### 3. config.ymlの更新

**ファイル**: `src/main/resources/config.yml`

**追加項目**:
```yaml
hot_reload:
  # テンプレートファイルのホットリロードを有効化
  # テンプレートファイルの編集を監視するかどうか
  templates: false
```

### 4. RPGPlugin.javaの更新

**ファイル**: `src/main/java/com/example/rpgplugin/RPGPlugin.java`

**変更点**:
- `setupConfigWatcher()`メソッドにtemplates監視機能を追加
- `getResourceSetupManager()`アクセサメソッドを追加

## 生成されるディレクトリ構造

```
plugins/RPGPlugin/
├── config.yml                        # メイン設定
├── classes/                          # クラス定義（ホットリロード対象）
│   ├── README.txt                    # 使用方法説明
│   ├── warrior.yml
│   ├── mage.yml
│   └── ...
├── skills/                           # スキル定義（ホットリロード対象）
│   ├── README.txt                    # 使用方法説明
│   ├── active/                       # アクティブスキル
│   │   ├── power_strike.yml
│   │   └── ...
│   └── passive/                      # パッシブスキル
│       ├── critical_mastery.yml
│       └── ...
├── mobs/                             # モブ設定
│   └── mob_drops.yml
├── exp/                              # 経験値設定
│   └── diminish_config.yml
├── data/                             # データベース等
└── templates/                        # ★新規：外部編集用テンプレート
    ├── skills/                       # スキルテンプレート
    │   ├── active_skill_template.yml
    │   └── passive_skill_template.yml
    └── classes/                      # クラステンプレート
        ├── melee_template.yml
        ├── ranged_template.yml
        ├── magic_template.yml
        └── tank_template.yml
```

## ホットリロード設定

| ディレクトリ | 設定キー | デフォルト値 | 説明 |
|-------------|---------|-------------|------|
| classes/ | hot_reload.classes | true | クラス定義の自動リロード |
| skills/ | hot_reload.skills | true | スキル定義の自動リロード |
| exp/ | hot_reload.exp_diminish | true | 経験値減衰設定の自動リロード |
| templates/ | hot_reload.templates | false | テンプレートの監視（ログ出力のみ） |

## 使用方法

### スキルの追加

1. `plugins/RPGPlugin/templates/skills/active_skill_template.yml` をコピー
2. スキルID、表示名、パラメータを編集
3. `plugins/RPGPlugin/skills/active/` に配置
4. `/rpg reload` またはファイル保存で自動リロード

### クラスの追加

1. `plugins/RPGPlugin/templates/classes/melee_template.yml` をコピー
2. クラスID、表示名、ステータス成長などを編集
3. `plugins/RPGPlugin/classes/` に配置
4. `/rpg reload` またはファイル保存で自動リロード

## 関連ファイル

### 新規ファイル
- `src/main/java/com/example/rpgplugin/core/config/ResourceSetupManager.java`

### 変更ファイル
- `src/main/java/com/example/rpgplugin/core/system/CoreSystemManager.java`
- `src/main/java/com/example/rpgplugin/RPGPlugin.java`
- `src/main/resources/config.yml`

### 既存テンプレート（JAR内）
- `src/main/resources/templates/skills/active_skill_template.yml`
- `src/main/resources/templates/skills/passive_skill_template.yml`
- `src/main/resources/templates/classes/melee_template.yml`
- `src/main/resources/templates/classes/ranged_template.yml`
- `src/main/resources/templates/classes/magic_template.yml`
- `src/main/resources/templates/classes/tank_template.yml`
