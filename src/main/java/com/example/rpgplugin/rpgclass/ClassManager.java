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
     * クラスをリロードし、削除されたクラスのプレイヤーデータをクリーンアップ
     *
     * <p>このメソッドは以下の処理を行います:</p>
     * <ul>
     *   <li>現在のクラスと新しいクラスを比較</li>
     *   <li>削除されたクラスを検出</li>
     *   <li>削除されたクラスを使用しているプレイヤーのクラスを解除</li>
     *   <li>新しいクラスマップを適用</li>
     * </ul>
     *
     * @param newClasses 新しいクラスマップ
     * @return リロード結果（削除されたクラス数、影響を受けたプレイヤー数）
     */
    public ReloadResult reloadWithCleanup(Map<String, RPGClass> newClasses) {
        Set<String> oldClassIds = new HashSet<>(classes.keySet());
        Set<String> newClassIds = new HashSet<>(newClasses.keySet());

        // 削除されたクラスを検出
        Set<String> removedClasses = new HashSet<>(oldClassIds);
        removedClasses.removeAll(newClassIds);

        int affectedPlayers = 0;

        // 削除されたクラスを使用しているプレイヤーをクリーンアップ
        if (!removedClasses.isEmpty()) {
            logger.info("Detected " + removedClasses.size() + " removed classes: " + removedClasses);

            for (String removedClass : removedClasses) {
                affectedPlayers += clearPlayersWithClass(removedClass);
                logger.warning("Removed class: " + removedClass + " (affected " + affectedPlayers + " players)");
            }
        }

        // 新しいクラスマップを適用
        classes.clear();
        classes.putAll(newClasses);

        logger.info("Reloaded " + classes.size() + " classes (removed: " + removedClasses.size() + ", affected players: " + affectedPlayers + ")");

        return new ReloadResult(newClasses.size(), removedClasses, affectedPlayers);
    }

    /**
     * 指定したクラスを使用している全プレイヤーのクラスを解除
     *
     * @param classId クラスID
     * @return 影響を受けたプレイヤー数
     */
    private int clearPlayersWithClass(String classId) {
        int count = 0;

        // オンラインプレイヤーを確認
        for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            RPGPlayer rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());
            if (rpgPlayer != null && classId.equals(rpgPlayer.getClassId())) {
                rpgPlayer.setClassId(null);
                player.sendMessage("§c[YAML更新] あなたの職業「" + classId + "」は削除されました。再度職業を選択してください。");
                count++;
            }
        }

        return count;
    }

    /**
     * リロード結果
     */
    public static class ReloadResult {
        private final int loadedClassCount;
        private final Set<String> removedClasses;
        private final int affectedPlayerCount;

        public ReloadResult(int loadedClassCount, Set<String> removedClasses, int affectedPlayerCount) {
            this.loadedClassCount = loadedClassCount;
            this.removedClasses = new HashSet<>(removedClasses);
            this.affectedPlayerCount = affectedPlayerCount;
        }

        public int getLoadedClassCount() {
            return loadedClassCount;
        }

        public Set<String> getRemovedClasses() {
            return new HashSet<>(removedClasses);
        }

        public int getAffectedPlayerCount() {
            return affectedPlayerCount;
        }

        public boolean hasRemovedClasses() {
            return !removedClasses.isEmpty();
        }

        @Override
        public String toString() {
            return "ReloadResult{" +
                    "loaded=" + loadedClassCount +
                    ", removed=" + removedClasses.size() +
                    ", affected=" + affectedPlayerCount +
                    '}';
        }
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
     * プレイヤーのクラスを変更します
     *
     * <p>条件チェックを行わず、即座にクラスを変更します。
     * 変更時にレベルを0にリセットし、職業固有のステータスボーナスを再計算します。</p>
     *
     * @param player  プレイヤー
     * @param classId 新しいクラスID
     * @param level   設定するレベル（0以下の場合は0）
     *return 成功した場合はtrue
     */
    public boolean changeClass(Player player, String classId, int level) {
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

        // クラスIDを設定
        rpgPlayer.setClassId(classId);

        // レベルを設定（0以下の場合は0）
        int newLevel = Math.max(0, level);
        player.setLevel(newLevel);

        // ステータスボーナスを再計算
        // TODO: StatManagerで職業固有のステータスボーナスを再計算する処理を実装
        // 現在は基本実装のみ

        logger.info("[ClassManager] Changed player class: " + player.getName() +
                    " from " + (oldClassId != null ? oldClassId : "none") +
                    " to " + classId + " (level: " + newLevel + ")");

        return true;
    }

    /**
     * プレイヤーのクラスを変更します（レベル0）
     *
     * <p>レベルを0にリセットしてクラスを変更します。</p>
     *
     * @param player  プレイヤー
     * @param classId 新しいクラスID
     * @return 成功した場合はtrue
     */
    public boolean changeClass(Player player, String classId) {
        return changeClass(player, classId, 0);
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
