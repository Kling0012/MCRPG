# スキルコンポーネント発動テスト計画

**作成日**: 2026-01-17
**目的**: 仕様書の内容を実装と照合して検証

---

## テストケース一覧

### TC001: 基本的なSINGLE + damage

```yaml
skill_id: "test_basic_damage"
display_name: "テスト: 基本ダメージ"
components:
  - type: SINGLE
    range: 10.0
    components:
      - type: damage
        value: 10
```

**期待結果**:
- SINGLEコンポーネントがターゲットを選択
- 選択されたターゲットに10ダメージ

### TC002: TARGET → MECHANIC → MECHANIC (連鎖)

```yaml
skill_id: "test_chain_mechanics"
display_name: "テスト: 連鎖メカニック"
components:
  - type: SELF
    components:
      - type: heal
        value: 5
      - type: message
        message: "回復しました"
```

**期待結果**:
- まずhealが実行される
- 成功後messageが実行される

### TC003: TARGET → CONDITION → MECHANIC

```yaml
skill_id: "test_condition_filter"
display_name: "テスト: 条件フィルタ"
components:
  - type: AREA
    radius: 5.0
    components:
      - type: chance
        value: 0.5
        components:
          - type: damage
            value: 20
```

**期待結果**:
- AREAで範囲内のエンティティを取得
- chanceで50%のエンティティのみフィルタリング
- フィルタされたエンティティのみダメージ

### TC004: 複数の独立ターゲットツリー

```yaml
skill_id: "test_multiple_targets"
display_name: "テスト: 複数ターゲット"
components:
  - type: SELF
    components:
      - type: heal
        value: 10
  - type: SINGLE
    range: 10.0
    components:
      - type: damage
        value: 5
```

**期待結果**:
- SELFで自分を回復
- SINGLEでターゲットにダメージ
- 2つのツリーは独立して実行

### TC005: 入れ子のCONDITION

```yaml
skill_id: "test_nested_conditions"
display_name: "テスト: 入れ子条件"
components:
  - type: SINGLE
    range: 10.0
    components:
      - type: health
        min: 0.5
        components:
          - type: mana
            min: 10
            components:
              - type: heal
                value: 20
```

**期待結果**:
- HP50%以上のターゲットのみ
- かつMP10以上のターゲットのみ
- 両方満たす場合のみ回復

### TC006: 不明なコンポーネントタイプ（エラー確認）

```yaml
skill_id: "test_invalid_component"
display_name: "テスト: 不明なコンポーネント"
components:
  - type: INVALID_TYPE
```

**期待結果**:
- ロード時に警告ログ
- コンポーネントはスキップ

### TC007: typeフィールド欠落（エラー確認）

```yaml
skill_id: "test_missing_type"
display_name: "テスト: type欠落"
components:
  - key: "no_type"
    value: 10
```

**期待結果**:
- バリデーションでエラー
- ロード失敗またはスキップ

### TC008: TRIGGERを使用したスキル

```yaml
skill_id: "test_trigger_skill"
display_name: "テスト: トリガースキル"
components:
  - type: PHYSICAL_TAKEN
    duration: 10
    components:
      - type: damage
        value: 5
        multiplier: 1.5
```

**期待結果**:
- 物理ダメージを受けた時に発動
- 10秒間アクティブ
- カウンターダメージを適用

---

## テスト実施方法

1. テスト用YAMLを `skills/test/` ディレクトリに配置
2. `/skill reload` コマンドで再読み込み
3. `/skill cast <skill_id>` で発動
4. 挙動を確認

## テスト結果記録

| TC ID | 日時 | 結果 | 備考 |
|-------|------|------|------|
| TC001 | - | 未実施 | - |
| TC002 | - | 未実施 | - |
| TC003 | - | 未実施 | - |
| TC004 | - | 未実施 | - |
| TC005 | - | 未実施 | - |
| TC006 | - | 未実施 | - |
| TC007 | - | 未実施 | - |
| TC008 | - | 未実施 | - |
