# SkillManager責務分離実装

## 概要
SkillManager（1131行）を3つのクラスに分離した。

## 新規クラス

### SkillRepository
- パッケージ: `com.example.rpgplugin.skill.repository`
- 責務: スキルデータの登録・取得・検索
- メソッド:
  - registerSkill(Skill)
  - getSkill(String)
  - getAllSkills()
  - getAllSkillIds()
  - getSkillsForClass(String)
  - getSkillTree(String)
  - getTreeRegistry()
  - reloadSkills(Map)
  - clearAllSkills()

### PlayerSkillService
- パッケージ: `com.example.rpgplugin.skill.repository`
- 責務: プレイヤーの習得スキルデータ管理
- メソッド:
  - getPlayerSkillData(Player/UUID)
  - hasSkill(Player/UUID, String)
  - getSkillLevel(Player/UUID, String)
  - acquireSkill(...)
  - upgradeSkill(...)
  - unloadPlayerData(UUID)
  - clearAllPlayerData()
  - cleanupRemovedSkills(Set)
- 内部クラス: PlayerSkillData, CleanupSummary

### SkillExecutor
- パッケージ: `com.example.rpgplugin.skill.repository`
- 責務: スキル実行ロジック
- メソッド:
  - executeSkill(Player, String, SkillExecutionConfig)
  - executeSkill(Player, String)
  - calculateDamage(Skill, RPGPlayer, int, Map)
  - calculateDamageWithFormula(...)
  - consumeCost(RPGPlayer, int, SkillCostType)
  - checkCooldown(Player, String, Skill)
  - selectTargets(Player, Skill, SkillExecutionConfig)
  - applyEffect(LivingEntity, double, Skill)
- 内部クラス: CostConsumptionResult

## その他の変更

### resultパッケージ
- `SkillExecutionResult` - 独立したクラスとして外出し

### SkillEventListener拡張
- executeSkill(Player, String) 追加
- executeSkill(Player, String, SkillExecutionConfig) 追加

### RPGPlayer拡張
- executeSkill(Player, String) 追加
- executeSkill(Player, String, SkillExecutionConfig) 追加

### EffCastRPGSkill修正
- SkillManager直接呼び出し → RPGPlayer経由に変更

## 次ステップ
- SkillManagerをファサードに書き換え
- テスト更新
- 動作確認
