package com.example.rpgplugin.rpgclass;

import com.example.rpgplugin.rpgclass.requirements.ClassRequirement;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * クラス管理クラス
 * 全クラスの登録・取得・プレイヤーの現在のクラス管理を行う
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>ファサードパターン: クラスシステムへの統一的インターフェース</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: クラス管理に特化</li>
 *   <li>DRY: クラス操作ロジックを一元管理</li>
 *   <li>KISS: シンプルなAPI設計</li>
 * </ul>
 *
 * <p>スレッド安全性:</p>
 * <ul>
 *   <li>ConcurrentHashMapを使用したスレッドセーフな実装</li>
 * </ul>
 */
public class ClassManager {

    /** すべてのクラス */
    private final Map<String, RPGClass> classes;

    private final Logger logger;
    private final PlayerManager playerManager;

    /**
     * コンストラクタ
     *
     * @param playerManager プレイヤーマネージャー
     */
    public ClassManager(PlayerManager playerManager) {
        this.classes = new ConcurrentHashMap<>();
        this.logger = Logger.getLogger(ClassManager.class.getName());
        this.playerManager = playerManager;
    }

    /**
     * クラスを登録
     *
     * @param rpgClass クラス
     */
    public void registerClass(RPGClass rpgClass) {
        if (rpgClass == null) {
            throw new IllegalArgumentException("RPGClass cannot be null");
        }

        classes.put(rpgClass.getId(), rpgClass);
        logger.info("Registered class: " + rpgClass.getId());
    }

    /**
     * 複数のクラスを一括登録
     *
     * @param newClasses クラスマップ
     */
    public void registerAll(Map<String, RPGClass> newClasses) {
        newClasses.forEach((id, rpgClass) -> registerClass(rpgClass));
    }

    /**
     * クラスを取得
     *
     * @param classId クラスID
     * @return クラス（存在しない場合はempty）
     */
    public Optional<RPGClass> getClass(String classId) {
        return Optional.ofNullable(classes.get(classId));
    }

    /**
     * すべてのクラスを取得
     *
     * @return クラスリスト
     */
    public Collection<RPGClass> getAllClasses() {
        return new ArrayList<>(classes.values());
    }

    /**
     * 全クラスIDを取得します
     *
     * @return クラスIDのセット
     */
    public Set<String> getAllClassIds() {
        return new HashSet<>(classes.keySet());
    }

    /**
     * 指定ランクのクラスを取得
     *
     * @param rank ランク
     * @return クラスリスト
     */
    public List<RPGClass> getClassesByRank(int rank) {
        return classes.values().stream()
                .filter(c -> c.getRank() == rank)
                .collect(Collectors.toList());
    }

    /**
     * 初期クラス（Rank1）を取得
     *
     * @return 初期クラスリスト
     */
    public List<RPGClass> getInitialClasses() {
        return getClassesByRank(1);
    }

    /**
     * プレイヤーの現在のクラスを取得
     *
     * @param player プレイヤー
     * @return クラス（未設定の場合はempty）
     */
    public Optional<RPGClass> getPlayerClass(Player player) {
        if (player == null) {
            return Optional.empty();
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            return Optional.empty();
        }

        String classId = rpgPlayer.getClassId();
        if (classId == null) {
            return Optional.empty();
        }

        return getClass(classId);
    }

    /**
     * プレイヤーのクラスを設定
     *
     * @param player  プレイヤー
     * @param classId クラスID
     * @return 設定成功時はtrue
     */
    public boolean setPlayerClass(Player player, String classId) {
        if (player == null || classId == null) {
            return false;
        }

        if (!classes.containsKey(classId)) {
            logger.warning("Class not found: " + classId);
            return false;
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            logger.warning("RPGPlayer not found: " + player.getName());
            return false;
        }

        String oldClassId = rpgPlayer.getClassId();

        // 履歴に追加（永続化）
        if (oldClassId != null && !oldClassId.equals(classId)) {
            rpgPlayer.addClassToHistory(oldClassId);
        }

        rpgPlayer.setClassId(classId);
        logger.info("Set player class: " + player.getName() + " -> " + classId);

        return true;
    }

    /**
     * プレイヤーのクラスを解除
     *
     * @param player プレイヤー
     */
    public void clearPlayerClass(Player player) {
        if (player == null) {
            return;
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            return;
        }

        String oldClassId = rpgPlayer.getClassId();
        rpgPlayer.setClassId(null);

        if (oldClassId != null) {
            logger.info("Cleared player class: " + player.getName() + " (was " + oldClassId + ")");
        }
    }

    /**
     * プレイヤーのクラス履歴を取得
     *
     * @param player プレイヤー
     * @return 履歴リスト（コピー）
     */
    public List<String> getPlayerClassHistory(Player player) {
        if (player == null) {
            return new ArrayList<>();
        }

        RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
        if (rpgPlayer == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(rpgPlayer.getClassHistory());
    }

    /**
     * クラスアップが可能かチェック
     *
     * @param player    プレイヤー
     * @param targetId  目標クラスID
     * @return チェック結果
     */
    public ClassUpResult canUpgradeClass(Player player, String targetId) {
        if (player == null || targetId == null) {
            return new ClassUpResult(false, "プレイヤーまたはクラスIDが無効です", Collections.emptyList());
        }

        RPGClass targetClass = classes.get(targetId);
        if (targetClass == null) {
            return new ClassUpResult(false, "クラスが見つかりません: " + targetId, Collections.emptyList());
        }

        // 現在のクラスを取得
        Optional<RPGClass> currentClassOpt = getPlayerClass(player);
        if (!currentClassOpt.isPresent()) {
            // 初期クラスの場合は要件なし
            if (targetClass.getRank() == 1) {
                return new ClassUpResult(true, "初期クラスへ変更可能", Collections.emptyList());
            }
            return new ClassUpResult(false, "まずは初期クラスを選択してください", Collections.emptyList());
        }

        RPGClass currentClass = currentClassOpt.get();

        // 直線パターン
        if (currentClass.getNextRankClassId().map(id -> id.equals(targetId)).orElse(false)) {
            List<ClassRequirement> requirements = currentClass.getNextRankRequirements();
            return checkRequirements(player, requirements);
        }

        // 分岐パターン
        if (currentClass.getAlternativeRanks().containsKey(targetId)) {
            List<ClassRequirement> requirements = currentClass.getAlternativeRanks().get(targetId);
            return checkRequirements(player, requirements);
        }

        return new ClassUpResult(false, "このクラスに変更する条件を満たしていません", Collections.emptyList());
    }

    /**
     * 要件をチェック
     *
     * @param player      プレイヤー
     * @param requirements 要件リスト
     * @return チェック結果
     */
    private ClassUpResult checkRequirements(Player player, List<ClassRequirement> requirements) {
        List<String> failedRequirements = new ArrayList<>();

        for (ClassRequirement req : requirements) {
            if (!req.check(player)) {
                failedRequirements.add(req.getDescription());
            }
        }

        if (failedRequirements.isEmpty()) {
            return new ClassUpResult(true, "クラスアップ可能", Collections.emptyList());
        } else {
            return new ClassUpResult(false, "以下の条件を満たしていません", failedRequirements);
        }
    }

    /**
     * クラスをリロード
     *
     * @param newClasses 新しいクラスマップ
     */
    public void reload(Map<String, RPGClass> newClasses) {
        classes.clear();
        classes.putAll(newClasses);
        logger.info("Reloaded " + classes.size() + " classes");
    }

    /**
     * クラス数を取得
     *
     * @return クラス数
     */
    public int getClassCount() {
        return classes.size();
    }

    /**
     * クラスアップ結果
     */
    public static class ClassUpResult {
        private final boolean success;
        private final String message;
        private final List<String> failedRequirements;

        public ClassUpResult(boolean success, String message, List<String> failedRequirements) {
            this.success = success;
            this.message = message;
            this.failedRequirements = new ArrayList<>(failedRequirements);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public List<String> getFailedRequirements() {
            return new ArrayList<>(failedRequirements);
        }
    }
}
