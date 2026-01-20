package com.example.rpgplugin.core.system;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.stats.StatManager;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.rpgclass.ClassManager;
import com.example.rpgplugin.damage.DamageManager;
import com.example.rpgplugin.player.exp.ExpManager;
import com.example.rpgplugin.core.validation.ConsistencyValidator;

import java.util.Map;

/**
 * ゲームシステムの統合マネージャー（ファサード）
 *
 * 責務:
 * - プレイヤーデータ管理（PlayerManager）
 * - ステータスシステム（StatManager）
 * - スキルシステム（SkillManager）
 * - クラスシステム（ClassManager）
 * - ダメージシステム（DamageManager）
 * - 経験値システム（ExpManager）
 *
 * Single Responsibility: ゲームプレイ関連機能の統合管理
 *
 * 初期化順序:
 * 1. PlayerManager（他のシステムがプレイヤーデータに依存）
 * 2. StatManager（ステータス管理）
 * 3. ClassManager（PlayerManagerに依存）
 * 4. SkillManager（スキル管理）
 * 5. DamageManager（ダメージ計算）
 * 6. ExpManager（経験値システム）
 */
public class GameSystemManager {
    private final RPGPlugin plugin;

    // プレイヤーシステム
    private final PlayerManager playerManager;

    // ステータスシステム
    private final StatManager statManager;

    // クラスシステム
    private final ClassManager classManager;

    // スキルシステム
    private final SkillManager skillManager;
    private final com.example.rpgplugin.model.skill.SkillConfig skillConfig;
    private final com.example.rpgplugin.skill.executor.ActiveSkillExecutor activeSkillExecutor;
    private final com.example.rpgplugin.skill.executor.PassiveSkillExecutor passiveSkillExecutor;

    // ダメージシステム
    private final DamageManager damageManager;

    // 経験値システム
    private final ExpManager expManager;

    // ローダー（リロード用）
    private final com.example.rpgplugin.rpgclass.ClassLoader classLoader;
    private final com.example.rpgplugin.skill.SkillLoader skillLoader;

    // 整合性検証
    private final ConsistencyValidator consistencyValidator;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     * @param coreSystem コアシステムマネージャー
     */
    public GameSystemManager(RPGPlugin plugin, CoreSystemManager coreSystem) {
        this.plugin = plugin;

        // 依存関係を考慮してインスタンス化
        // 1. PlayerManager（最初に初期化）
        this.playerManager = new PlayerManager(plugin, coreSystem.getStorageManager().getPlayerDataRepository());
        this.statManager = new StatManager();
        this.classManager = new ClassManager(playerManager);

        // 2. ExpManager（PlayerManagerとClassManagerに依存）
        this.expManager = new ExpManager(plugin, playerManager, classManager);

        // 3. その他のマネージャー
        this.skillManager = new SkillManager(plugin, playerManager);
        this.skillConfig = new com.example.rpgplugin.model.skill.SkillConfig(plugin, skillManager);
        this.activeSkillExecutor = new com.example.rpgplugin.skill.executor.ActiveSkillExecutor(plugin, skillManager, playerManager);
        this.passiveSkillExecutor = new com.example.rpgplugin.skill.executor.PassiveSkillExecutor(plugin, skillManager, playerManager);
        // DamageManagerはPlayerManagerに依存するため、初期化順序を考慮して渡す
        this.damageManager = new DamageManager(plugin, playerManager);

        // 4. ローダー（リロード用）
        this.classLoader = new com.example.rpgplugin.rpgclass.ClassLoader(plugin, playerManager);
        this.skillLoader = new com.example.rpgplugin.skill.SkillLoader(plugin);

        // 5. 整合性検証
        this.consistencyValidator = new ConsistencyValidator(plugin.getLogger());
    }

    /**
     * ゲームシステムを初期化する
     *
     * @throws Exception 初期化に失敗した場合
     */
    public void initialize() throws Exception {
        plugin.getLogger().info("========================================");
        plugin.getLogger().info(" GameSystemManager: 初期化を開始します");
        plugin.getLogger().info("========================================");

        // 1. プレイヤーマネージャーの初期化
        plugin.getLogger().info("[GameSystem] プレイヤーマネージャーを初期化中...");
        playerManager.initialize();

        // 2. ステータスマネージャーの初期化
        plugin.getLogger().info("[GameSystem] ステータスマネージャーを初期化中...");
        // statManager は initialize() メソッドを持たない

        // 3. クラスマネージャーの初期化
        plugin.getLogger().info("[GameSystem] クラスマネージャーを初期化中...");
        // classManager は initialize() メソッドを持たない

        // 4. スキルマネージャーの初期化
        plugin.getLogger().info("[GameSystem] スキルマネージャーを初期化中...");
        // skillManager は initialize() メソッドを持たない

        // 5. ダメージマネージャーの初期化
        plugin.getLogger().info("[GameSystem] ダメージマネージャーを初期化中...");
        damageManager.initialize();

        // 6. 経験値システムの初期化
        plugin.getLogger().info("[GameSystem] 経験値システムを初期化中...");
        expManager.initialize();

        plugin.getLogger().info("========================================");
        plugin.getLogger().info(" GameSystemManager: 初期化が完了しました");
        plugin.getLogger().info("========================================");
    }

    /**
     * ゲームシステムをシャットダウンする
     */
    public void shutdown() {
        plugin.getLogger().info("[GameSystem] シャットダウンを開始します");
        expManager.shutdown();
        plugin.getLogger().info("[GameSystem] シャットダウンが完了しました");
    }

    // Getter メソッド（後方互換性）

    /**
     * プレイヤーマネージャーを取得する
     *
     * @return PlayerManager プレイヤーマネージャー
     */
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * ステータスマネージャーを取得する
     *
     * @return StatManager ステータスマネージャー
     */
    public StatManager getStatManager() {
        return statManager;
    }

    /**
     * クラスマネージャーを取得する
     *
     * @return ClassManager クラスマネージャー
     */
    public ClassManager getClassManager() {
        return classManager;
    }

    /**
     * スキルマネージャーを取得する
     *
     * @return SkillManager スキルマネージャー
     */
    public SkillManager getSkillManager() {
        return skillManager;
    }

    /**
     * スキル設定を取得する
     *
     * @return SkillConfig スキル設定
     */
    public com.example.rpgplugin.model.skill.SkillConfig getSkillConfig() {
        return skillConfig;
    }

    /**
     * アクティブスキルエグゼキューターを取得する
     *
     * @return ActiveSkillExecutor アクティブスキルエグゼキューター
     */
    public com.example.rpgplugin.skill.executor.ActiveSkillExecutor getActiveSkillExecutor() {
        return activeSkillExecutor;
    }

    /**
     * パッシブスキルエグゼキューターを取得する
     *
     * @return PassiveSkillExecutor パッシブスキルエグゼキューター
     */
    public com.example.rpgplugin.skill.executor.PassiveSkillExecutor getPassiveSkillExecutor() {
        return passiveSkillExecutor;
    }

    /**
     * ダメージマネージャーを取得する
     *
     * @return DamageManager ダメージマネージャー
     */
    public DamageManager getDamageManager() {
        return damageManager;
    }

    /**
     * 経験値マネージャーを取得する
     *
     * @return ExpManager 経験値マネージャー
     */
    public ExpManager getExpManager() {
        return expManager;
    }

    /**
     * クラスローダーを取得する
     *
     * @return ClassLoader クラスローダー
     */
    public com.example.rpgplugin.rpgclass.ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * スキルローダーを取得する
     *
     * @return SkillLoader スキルローダー
     */
    public com.example.rpgplugin.skill.SkillLoader getSkillLoader() {
        return skillLoader;
    }

    /**
     * 整合性検証クラスを取得します
     *
     * @return ConsistencyValidator 整合性検証クラス
     */
    public ConsistencyValidator getConsistencyValidator() {
        return consistencyValidator;
    }

    /**
     * クラスとスキルの整合性を検証します
     *
     * <p>以下を検証します:</p>
     * <ul>
     *   <li>Class.availableSkillsとSkill.availableClassesの双方向整合性</li>
     *   <li>存在しないスキル/クラスの参照チェック</li>
     * </ul>
     *
     * @return 検証結果
     */
    public ConsistencyValidator.ValidationResult validateConsistency() {
        Map<String, com.example.rpgplugin.rpgclass.RPGClass> classes = new java.util.HashMap<>();
        classManager.getAllClasses().forEach(c -> classes.put(c.getId(), c));

        Map<String, com.example.rpgplugin.skill.Skill> skills = skillManager.getAllSkills();

        return consistencyValidator.validate(classes, skills);
    }
}
