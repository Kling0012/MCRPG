package com.example.rpgplugin.damage.config;

import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.evaluator.VariableContext;
import com.example.rpgplugin.stats.Stat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 変数スコープマネージャー
 *
 * <p>動的スコープシステムを実装します。スコープの優先順位は以下の通り:</p>
 * <ol>
 *   <li>テンポラリ変数（一時的バフ/デバフ）</li>
 *   <li>プレイヤー変数（プレイヤー固有のカスタム変数）</li>
 *   <li>クラス変数（クラス別上書き定数）</li>
 *   <li>グローバル変数（グローバル定数）</li>
 *   <li>システム変数（STR, INT等のステータス）</li>
 * </ol>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: 変数スコープ管理の単一責務</li>
 *   <li>DRY: 変数解決ロジックの一元化</li>
 *   <li>KISS: シンプルなチェイン構造</li>
 * </ul>
 *
 * @version 1.0.0
 * @author RPGPlugin Team
 */
public class VariableScopeManager {

    /** テンポラリ変数ストレージ (プレイヤーUUID -> 変数マップ) */
    private final Map<java.util.UUID, Map<String, Object>> temporaryVariables;

    /** プレイヤー永続変数ストレージ (プレイヤーUUID -> 変数マップ) */
    private final Map<java.util.UUID, Map<String, Object>> playerVariables;

    /** グローバル定数 */
    private final Map<String, Object> globalConstants;

    /** クラス定数キャッシュ (クラス名 -> 定数マップ) */
    private final Map<String, Map<String, Object>> classConstantsCache;

    /** DamageConfigへの参照 */
    private DamageConfig damageConfig;

    /**
     * コンストラクタ
     */
    public VariableScopeManager() {
        this.temporaryVariables = new ConcurrentHashMap<>();
        this.playerVariables = new ConcurrentHashMap<>();
        this.globalConstants = new ConcurrentHashMap<>();
        this.classConstantsCache = new ConcurrentHashMap<>();
    }

    /**
     * コンストラクタ（初期グローバル定数付き）
     *
     * @param globalConstants 初期グローバル定数
     */
    public VariableScopeManager(Map<String, Object> globalConstants) {
        this();
        if (globalConstants != null) {
            this.globalConstants.putAll(globalConstants);
        }
    }

    /**
     * DamageConfigを設定します
     *
     * @param damageConfig ダメージ設定
     */
    public void setDamageConfig(DamageConfig damageConfig) {
        this.damageConfig = damageConfig;
        // グローバル定数を更新
        if (damageConfig != null) {
            this.globalConstants.clear();
            this.globalConstants.putAll(damageConfig.getGlobalConstants());
        }
        // クラス定数キャッシュをクリア
        this.classConstantsCache.clear();
    }

    /**
     * 変数を解決します
     *
     * <p>スコープ順に変数を検索し、最初に見つかった値を返します。</p>
     *
     * @param variableName 変数名
     * @param rpgPlayer RPGプレイヤー
     * @return 変数値、見つからない場合はnull
     */
    public Object resolveVariable(String variableName, RPGPlayer rpgPlayer) {
        // 1. テンポラリ変数を確認
        Object value = getTemporaryVariable(variableName, rpgPlayer);
        if (value != null) {
            return value;
        }

        // 2. プレイヤー永続変数を確認
        value = getPlayerVariable(variableName, rpgPlayer);
        if (value != null) {
            return value;
        }

        // 3. クラス変数を確認
        if (rpgPlayer != null && rpgPlayer.getClassId() != null) {
            String className = rpgPlayer.getClassId();
            value = getClassConstant(className, variableName);
            if (value != null) {
                return value;
            }
        }

        // 4. グローバル定数を確認
        value = globalConstants.get(variableName);
        if (value != null) {
            return value;
        }

        // 5. システム変数（ステータス）を確認
        if (rpgPlayer != null) {
            value = getSystemVariable(variableName, rpgPlayer);
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    /**
     * テンポラリ変数を設定します
     *
     * @param playerId プレイヤーUUID
     * @param variableName 変数名
     * @param value 値
     * @param durationTicks 持続時間（ティック）、0以下で永続
     */
    public void setTemporaryVariable(java.util.UUID playerId, String variableName, Object value, long durationTicks) {
        temporaryVariables.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .put(variableName, value);

        if (durationTicks > 0) {
            // 期限付きテンポラリ変数のスケジュール
            scheduleTemporaryVariableRemoval(playerId, variableName, durationTicks);
        }
    }

    /**
     * テンポラリ変数を取得します
     *
     * @param variableName 変数名
     * @param rpgPlayer RPGプレイヤー
     * @return 変数値、存在しない場合はnull
     */
    public Object getTemporaryVariable(String variableName, RPGPlayer rpgPlayer) {
        if (rpgPlayer == null) {
            return null;
        }
        Map<String, Object> playerTemps = temporaryVariables.get(rpgPlayer.getUuid());
        return playerTemps != null ? playerTemps.get(variableName) : null;
    }

    /**
     * テンポラリ変数を削除します
     *
     * @param playerId プレイヤーUUID
     * @param variableName 変数名
     */
    public void removeTemporaryVariable(java.util.UUID playerId, String variableName) {
        Map<String, Object> playerTemps = temporaryVariables.get(playerId);
        if (playerTemps != null) {
            playerTemps.remove(variableName);
            if (playerTemps.isEmpty()) {
                temporaryVariables.remove(playerId);
            }
        }
    }

    /**
     * プレイヤーの全テンポラリ変数を削除します
     *
     * @param playerId プレイヤーUUID
     */
    public void clearTemporaryVariables(java.util.UUID playerId) {
        temporaryVariables.remove(playerId);
    }

    /**
     * プレイヤー永続変数を設定します
     *
     * @param playerId プレイヤーUUID
     * @param variableName 変数名
     * @param value 値
     */
    public void setPlayerVariable(java.util.UUID playerId, String variableName, Object value) {
        playerVariables.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .put(variableName, value);
    }

    /**
     * プレイヤー永続変数を取得します
     *
     * @param variableName 変数名
     * @param rpgPlayer RPGプレイヤー
     * @return 変数値、存在しない場合はnull
     */
    public Object getPlayerVariable(String variableName, RPGPlayer rpgPlayer) {
        if (rpgPlayer == null) {
            return null;
        }
        Map<String, Object> playerVars = playerVariables.get(rpgPlayer.getUuid());
        return playerVars != null ? playerVars.get(variableName) : null;
    }

    /**
     * クラス定数を取得します
     *
     * @param className クラス名
     * @param constantName 定数名
     * @return 定数値、存在しない場合はnull
     */
    public Object getClassConstant(String className, String constantName) {
        Map<String, Object> classConsts = classConstantsCache.computeIfAbsent(className, this::loadClassConstants);
        return classConsts.get(constantName);
    }

    /**
     * クラス定数をロードします
     *
     * @param className クラス名
     * @return クラス定数マップ
     */
    private Map<String, Object> loadClassConstants(String className) {
        if (damageConfig != null) {
            DamageConfig.ClassOverrideConfig classOverride = damageConfig.getClassOverride(className);
            if (classOverride != null) {
                return new HashMap<>(classOverride.getConstants());
            }
        }
        return Collections.emptyMap();
    }

    /**
     * システム変数（ステータス）を取得します
     *
     * @param variableName 変数名
     * @param rpgPlayer RPGプレイヤー
     * @return 変数値、存在しない場合はnull
     */
    private Object getSystemVariable(String variableName, RPGPlayer rpgPlayer) {
        return switch (variableName.toUpperCase()) {
            case "STR" -> rpgPlayer.getStatManager().getFinalStat(Stat.STRENGTH);
            case "INT" -> rpgPlayer.getStatManager().getFinalStat(Stat.INTELLIGENCE);
            case "SPI" -> rpgPlayer.getStatManager().getFinalStat(Stat.SPIRIT);
            case "VIT" -> rpgPlayer.getStatManager().getFinalStat(Stat.VITALITY);
            case "DEX" -> rpgPlayer.getStatManager().getFinalStat(Stat.DEXTERITY);
            case "LV", "PLAYER_LEVEL" -> rpgPlayer.getLevel();
            case "CLASS_RANK" -> rpgPlayer.getClassRank();
            default -> null;
        };
    }

    /**
     * VariableContextを構築します
     *
     * <p>数式評価用の完全な変数コンテキストを作成します。</p>
     *
     * @param rpgPlayer RPGプレイヤー
     * @param skillLevel スキルレベル（オプション）
     * @param additionalVariables 追加変数（オプション）
     * @return VariableContextインスタンス
     */
    public VariableContext buildVariableContext(
            RPGPlayer rpgPlayer,
            Integer skillLevel,
            Map<String, Double> additionalVariables) {

        VariableContext context = skillLevel != null
                ? new VariableContext(rpgPlayer, skillLevel)
                : new VariableContext(rpgPlayer);

        // 追加変数を設定
        if (additionalVariables != null && !additionalVariables.isEmpty()) {
            context.setCustomVariables(additionalVariables);
        }

        return context;
    }

    /**
     * VariableContextを構築します（追加変数なし）
     *
     * @param rpgPlayer RPGプレイヤー
     * @param skillLevel スキルレベル（オプション）
     * @return VariableContextインスタンス
     */
    public VariableContext buildVariableContext(RPGPlayer rpgPlayer, Integer skillLevel) {
        return buildVariableContext(rpgPlayer, skillLevel, null);
    }

    /**
     * テンポラリ変数削除をスケジュールします
     *
     * @param playerId プレイヤーUUID
     * @param variableName 変数名
     * @param durationTicks 持続時間（ティック）
     */
    private void scheduleTemporaryVariableRemoval(java.util.UUID playerId, String variableName, long durationTicks) {
        // Note: 実際のスケジューリングは呼び出し元で行う必要があります
        // ここではインターフェースのみ提供
        // RPGPluginのスケジューラーを使用してください
    }

    /**
     * クラス定数キャッシュをクリアします
     *
     * <p>ダメージ設定がリロードされた場合に呼び出してください。</p>
     */
    public void clearClassConstantsCache() {
        classConstantsCache.clear();
    }

    /**
     * 全プレイヤーのテンポラリ変数をクリアします
     */
    public void clearAllTemporaryVariables() {
        temporaryVariables.clear();
    }

    /**
     * グローバル定数を設定します
     *
     * @param constants 定数マップ
     */
    public void setGlobalConstants(Map<String, Object> constants) {
        globalConstants.clear();
        if (constants != null) {
            globalConstants.putAll(constants);
        }
    }

    /**
     * グローバル定数を取得します
     *
     * @return グローバル定数マップ（コピー）
     */
    public Map<String, Object> getGlobalConstants() {
        return new HashMap<>(globalConstants);
    }

    /**
     * 現在のスコープ状態を文字列で返します（デバッグ用）
     *
     * @param rpgPlayer RPGプレイヤー
     * @return スコープ状態の文字列表現
     */
    public String debugScope(RPGPlayer rpgPlayer) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== VariableScopeManager Debug ===\n");

        if (rpgPlayer != null) {
            java.util.UUID playerId = rpgPlayer.getUuid();

            // テンポラリ変数
            Map<String, Object> temps = temporaryVariables.get(playerId);
            sb.append("Temporary Variables: ").append(temps != null ? temps : "{}").append("\n");

            // プレイヤー永続変数
            Map<String, Object> playerVars = playerVariables.get(playerId);
            sb.append("Player Variables: ").append(playerVars != null ? playerVars : "{}").append("\n");

            // クラス定数
            if (rpgPlayer.getClassId() != null) {
                String className = rpgPlayer.getClassId();
                Map<String, Object> classConsts = classConstantsCache.get(className);
                sb.append("Class Constants (").append(className).append("): ")
                        .append(classConsts != null ? classConsts : "{}").append("\n");
            }

            // システム変数
            sb.append("System Variables: {");
            sb.append("STR=").append(rpgPlayer.getStatManager().getFinalStat(Stat.STRENGTH)).append(", ");
            sb.append("INT=").append(rpgPlayer.getStatManager().getFinalStat(Stat.INTELLIGENCE)).append(", ");
            sb.append("SPI=").append(rpgPlayer.getStatManager().getFinalStat(Stat.SPIRIT)).append(", ");
            sb.append("VIT=").append(rpgPlayer.getStatManager().getFinalStat(Stat.VITALITY)).append(", ");
            sb.append("DEX=").append(rpgPlayer.getStatManager().getFinalStat(Stat.DEXTERITY)).append(", ");
            sb.append("LV=").append(rpgPlayer.getLevel());
            sb.append("}\n");
        }

        // グローバル定数
        sb.append("Global Constants: ").append(globalConstants);

        return sb.toString();
    }
}
