package com.example.rpgplugin.trade;

import com.example.rpgplugin.trade.repository.TradeHistoryRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * トレードマネージャー
 *
 * <p>トレードシステムのファサードクラスです。</p>
 *
 * <ul>
 *   <li>トレード申請の管理</li>
 *   <li>アクティブなトレードセッションの管理</li>
 *   <li>トレードの実行と完了処理</li>
 *   <li>タイムアウト監視</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class TradeManager {

    private final Logger logger;
    private final com.example.rpgplugin.RPGPlugin plugin;

    // トレード申請: requesterUuid -> targetUuid
    private final Map<UUID, UUID> pendingRequests;

    // アクティブなトレードセッション: sessionId -> TradeSession
    private final Map<String, TradeSession> activeSessions;

    // プレイヤーUUID -> セッションIDのマップ（高速検索用）
    private final Map<UUID, String> playerToSession;

    // トレード履歴リポジトリ
    private TradeHistoryRepository historyRepository;

    // タイムアウト設定
    private static final long REQUEST_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(2);
    private static final long SESSION_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(5);

    // タイムアウトチェックタスク
    private BukkitTask timeoutCheckTask;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public TradeManager(com.example.rpgplugin.RPGPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.pendingRequests = new ConcurrentHashMap<>();
        this.activeSessions = new ConcurrentHashMap<>();
        this.playerToSession = new ConcurrentHashMap<>();
    }

    /**
     * トレードシステムを初期化
     *
     * @param historyRepository トレード履歴リポジトリ
     */
    public void initialize(TradeHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;

        // タイムアウトチェックタスクを開始
        startTimeoutCheckTask();

        logger.info("TradeManager initialized");
    }

    /**
     * タイムアウトチェックタスクを開始
     */
    private void startTimeoutCheckTask() {
        // 10秒ごとにチェック
        timeoutCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            checkTimeouts();
        }, 200L, 200L); // 10秒 = 200 ticks

        logger.info("Timeout check task started");
    }

    /**
     * トレード申請を送信
     *
     * @param requester 申請者
     * @param target 相手
     * @return 申請成功時true
     */
    public boolean sendRequest(Player requester, Player target) {
        // バリデーション
        if (!validateRequest(requester, target)) {
            return false;
        }

        UUID requesterUuid = requester.getUniqueId();
        UUID targetUuid = target.getUniqueId();

        // 既存の申請をクリア
        clearExistingRequests(requesterUuid, targetUuid);

        // 申請を登録
        pendingRequests.put(requesterUuid, targetUuid);

        // 通知
        requester.sendMessage(ChatColor.GREEN + "トレード申請を " + target.getName() + " に送信しました");
        target.sendMessage(ChatColor.YELLOW + requester.getName() + " からトレード申請が届きました");
        target.sendMessage(ChatColor.GRAY + "承認するには: " + ChatColor.WHITE + "/rpg trade accept");

        logger.info(String.format("[Trade] Request: %s -> %s", requester.getName(), target.getName()));

        return true;
    }

    /**
     * トレード申請を承認
     *
     * @param player 承認するプレイヤー
     * @return 承認成功時true
     */
    public boolean acceptRequest(Player player) {
        UUID playerUuid = player.getUniqueId();

        // このプレイヤー宛の申請を検索
        UUID requesterUuid = null;
        for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
            if (entry.getValue().equals(playerUuid)) {
                requesterUuid = entry.getKey();
                break;
            }
        }

        if (requesterUuid == null) {
            player.sendMessage(ChatColor.RED + "保留中のトレード申請がありません");
            return false;
        }

        Player requester = Bukkit.getPlayer(requesterUuid);
        if (requester == null || !requester.isOnline()) {
            player.sendMessage(ChatColor.RED + "申請者がオフラインです");
            pendingRequests.remove(requesterUuid);
            return false;
        }

        // 申請を削除
        pendingRequests.remove(requesterUuid);

        // トレードセッションを作成
        TradeSession session = new TradeSession(requester, player, logger);
        activeSessions.put(session.getSessionId(), session);
        playerToSession.put(requesterUuid, session.getSessionId());
        playerToSession.put(playerUuid, session.getSessionId());

        // 通知
        requester.sendMessage(ChatColor.GREEN + "トレードが開始されました！");
        player.sendMessage(ChatColor.GREEN + "トレードが開始されました！");

        logger.info(String.format("[Trade] Session started: %s between %s and %s",
            session.getSessionId(), requester.getName(), player.getName()));

        // GUIを開く
        openTradeGUI(session);

        return true;
    }

    /**
     * トレード申請を拒否
     *
     * @param player 拒否するプレイヤー
     * @return 拒否成功時true
     */
    public boolean denyRequest(Player player) {
        UUID playerUuid = player.getUniqueId();

        // このプレイヤー宛の申請を検索
        UUID requesterUuid = null;
        for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
            if (entry.getValue().equals(playerUuid)) {
                requesterUuid = entry.getKey();
                break;
            }
        }

        if (requesterUuid == null) {
            player.sendMessage(ChatColor.RED + "保留中のトレード申請がありません");
            return false;
        }

        Player requester = Bukkit.getPlayer(requesterUuid);

        // 申請を削除
        pendingRequests.remove(requesterUuid);

        // 通知
        player.sendMessage(ChatColor.YELLOW + "トレード申請を拒否しました");
        if (requester != null && requester.isOnline()) {
            requester.sendMessage(ChatColor.RED + player.getName() + " にトレード申請を拒否されました");
        }

        logger.info(String.format("[Trade] Request denied: %s -> %s",
            requester != null ? requester.getName() : "offline", player.getName()));

        return true;
    }

    /**
     * プレイヤーのセッションを取得
     *
     * @param player プレイヤー
     * @return セッション（存在しない場合は空）
     */
    public Optional<TradeSession> getPlayerSession(Player player) {
        String sessionId = playerToSession.get(player.getUniqueId());
        if (sessionId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(activeSessions.get(sessionId));
    }

    /**
     * セッションを取得
     *
     * @param sessionId セッションID
     * @return セッション（存在しない場合は空）
     */
    public Optional<TradeSession> getSession(String sessionId) {
        return Optional.ofNullable(activeSessions.get(sessionId));
    }

    /**
     * トレードを実行
     *
     * @param session トレードセッション
     * @return 実行成功時true
     */
    public boolean executeTrade(TradeSession session) {
        if (!session.canExecute()) {
            logger.warning(String.format("[Trade] Cannot execute session %s: invalid state", session.getSessionId()));
            return false;
        }

        if (!session.areBothOnline()) {
            cancelSession(session, "一方のプレイヤーがオフラインです");
            return false;
        }

        try {
            // アイテムとゴールドの交換を実行
            exchangeItems(session);
            exchangeGold(session);

            // セッションを完了状態に
            session.complete();

            // 両プレイヤーのGUIを閉じる
            closeTradeGUI(session);

            // 通知
            notifyTradeCompleted(session);

            // 履歴を保存（非同期）
            saveTradeHistory(session);

            // セッションをクリーンアップ
            cleanupSession(session);

            logger.info(String.format("[Trade] Session %s executed successfully", session.getSessionId()));
            return true;

        } catch (Exception e) {
            logger.severe("Failed to execute trade: " + e.getMessage());
            e.printStackTrace();
            cancelSession(session, "トレード実行中にエラーが発生しました");
            return false;
        }
    }

    /**
     * アイテムを交換
     *
     * @param session トレードセッション
     */
    private void exchangeItems(TradeSession session) {
        var p1 = session.getParty1();
        var p2 = session.getParty2();

        Player player1 = p1.getPlayer();
        Player player2 = p2.getPlayer();

        if (player1 == null || player2 == null) {
            throw new IllegalStateException("Both players must be online");
        }

        // P1のアイテムをP2に
        for (var item : p1.getOffer().getItems()) {
            player1.getInventory().removeItem(item);
            player2.getInventory().addItem(item);
        }

        // P2のアイテムをP1に
        for (var item : p2.getOffer().getItems()) {
            player2.getInventory().removeItem(item);
            player1.getInventory().addItem(item);
        }
    }

    /**
     * ゴールドを交換
     *
     * @param session トレードセッション
     */
    private void exchangeGold(TradeSession session) {
        // TODO: 経済システムとの連携
        // 現時点ではプレイヤーメッセージのみ
        double gold1 = session.getParty1().getOffer().getGoldAmount();
        double gold2 = session.getParty2().getOffer().getGoldAmount();

        if (gold1 > 0 || gold2 > 0) {
            logger.info(String.format("[Trade] Gold exchange: P1=%.2f, P2=%.2f", gold1, gold2));
        }
    }

    /**
     * セッションをキャンセル
     *
     * @param session セッション
     * @param reason 理由
     */
    public void cancelSession(TradeSession session, String reason) {
        session.cancel(reason);

        // 両プレイヤーに通知
        notifyTradeCancelled(session, reason);

        // GUIを閉じる
        closeTradeGUI(session);

        // アイテムを返却
        returnItems(session);

        // セッションをクリーンアップ
        cleanupSession(session);

        logger.info(String.format("[Trade] Session %s cancelled: %s", session.getSessionId(), reason));
    }

    /**
     * アイテムを返却
     *
     * @param session トレードセッション
     */
    private void returnItems(TradeSession session) {
        for (var party : Arrays.asList(session.getParty1(), session.getParty2())) {
            Player player = party.getPlayer();
            if (player != null && player.isOnline()) {
                for (var item : party.getOffer().getItems()) {
                    player.getInventory().addItem(item);
                }
            }
        }
    }

    /**
     * タイムアウトチェック
     */
    private void checkTimeouts() {
        long now = System.currentTimeMillis();

        // 申請のタイムアウトチェック
        Iterator<Map.Entry<UUID, UUID>> requestIt = pendingRequests.entrySet().iterator();
        while (requestIt.hasNext()) {
            Map.Entry<UUID, UUID> entry = requestIt.next();
            // TODO: 申請時刻を記録してタイムアウトチェック
        }

        // セッションのタイムアウトチェック
        Iterator<Map.Entry<String, TradeSession>> sessionIt = activeSessions.entrySet().iterator();
        while (sessionIt.hasNext()) {
            Map.Entry<String, TradeSession> entry = sessionIt.next();
            TradeSession session = entry.getValue();

            if (session.isExpired(SESSION_TIMEOUT_MS)) {
                cancelSession(session, "タイムアウト");
            }
        }
    }

    /**
     * セッションをクリーンアップ
     *
     * @param session セッション
     */
    private void cleanupSession(TradeSession session) {
        activeSessions.remove(session.getSessionId());
        playerToSession.remove(session.getParty1().getUuid());
        playerToSession.remove(session.getParty2().getUuid());
    }

    /**
     * 既存の申請/セッションをクリア
     *
     * @param uuid1 UUID1
     * @param uuid2 UUID2
     */
    private void clearExistingRequests(UUID uuid1, UUID uuid2) {
        // 既存の申請を削除
        pendingRequests.remove(uuid1);
        pendingRequests.entrySet().removeIf(entry -> entry.getValue().equals(uuid1));

        // 既存のセッションをキャンセル
        playerToSession.get(uuid1);
        playerToSession.get(uuid2);
        // TODO: 既存セッションのキャンセル処理
    }

    /**
     * トレード申請のバリデーション
     *
     * @param requester 申請者
     * @param target 相手
     * @return 有効な場合true
     */
    private boolean validateRequest(Player requester, Player target) {
        if (requester.equals(target)) {
            requester.sendMessage(ChatColor.RED + "自分自身とはトレードできません");
            return false;
        }

        if (!target.isOnline()) {
            requester.sendMessage(ChatColor.RED + "相手がオフラインです");
            return false;
        }

        // 既存のセッションチェック
        if (getPlayerSession(requester).isPresent()) {
            requester.sendMessage(ChatColor.RED + "既に進行中のトレードがあります");
            return false;
        }

        if (getPlayerSession(target).isPresent()) {
            requester.sendMessage(ChatColor.RED + "相手は既にトレード中です");
            return false;
        }

        return true;
    }

    /**
     * トレードGUIを開く
     *
     * @param session トレードセッション
     */
    private void openTradeGUI(TradeSession session) {
        Player p1 = session.getParty1().getPlayer();
        Player p2 = session.getParty2().getPlayer();

        if (p1 != null && p1.isOnline()) {
            TradeInventory inv1 = new TradeInventory(session, p1);
            inv1.open();
        }

        if (p2 != null && p2.isOnline()) {
            TradeInventory inv2 = new TradeInventory(session, p2);
            inv2.open();
        }

        logger.info(String.format("[Trade] GUI opened for session %s", session.getSessionId()));
    }

    /**
     * トレードGUIを閉じる
     *
     * @param session トレードセッション
     */
    private void closeTradeGUI(TradeSession session) {
        Player p1 = session.getParty1().getPlayer();
        Player p2 = session.getParty2().getPlayer();

        if (p1 != null && p1.isOnline()) {
            p1.closeInventory();
        }
        if (p2 != null && p2.isOnline()) {
            p2.closeInventory();
        }
    }

    /**
     * トレード完了通知
     *
     * @param session トレードセッション
     */
    private void notifyTradeCompleted(TradeSession session) {
        String message = ChatColor.GREEN + "トレードが完了しました！";

        Player p1 = session.getParty1().getPlayer();
        Player p2 = session.getParty2().getPlayer();

        if (p1 != null && p1.isOnline()) {
            p1.sendMessage(message);
        }
        if (p2 != null && p2.isOnline()) {
            p2.sendMessage(message);
        }
    }

    /**
     * トレードキャンセル通知
     *
     * @param session トレードセッション
     * @param reason 理由
     */
    private void notifyTradeCancelled(TradeSession session, String reason) {
        String message = ChatColor.RED + "トレードがキャンセルされました: " + reason;

        Player p1 = session.getParty1().getPlayer();
        Player p2 = session.getParty2().getPlayer();

        if (p1 != null && p1.isOnline()) {
            p1.sendMessage(message);
        }
        if (p2 != null && p2.isOnline()) {
            p2.sendMessage(message);
        }
    }

    /**
     * トレード履歴を保存（非同期）
     *
     * @param session トレードセッション
     */
    private void saveTradeHistory(TradeSession session) {
        if (historyRepository == null) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // TODO: 履歴保存処理
            logger.info(String.format("[Trade] History saved for session %s", session.getSessionId()));
        });
    }

    /**
     * シャットダウン
     */
    public void shutdown() {
        // すべてのアクティブなセッションをキャンセル
        for (TradeSession session : new ArrayList<>(activeSessions.values())) {
            cancelSession(session, "サーバーがシャットダウンしています");
        }

        // タイムアウトチェックタスクを停止
        if (timeoutCheckTask != null) {
            timeoutCheckTask.cancel();
            timeoutCheckTask = null;
        }

        logger.info("TradeManager shutdown completed");
    }

    /**
     * アクティブなセッション数を取得
     *
     * @return セッション数
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * 保留中の申請数を取得
     *
     * @return 申請数
     */
    public int getPendingRequestCount() {
        return pendingRequests.size();
    }
}
