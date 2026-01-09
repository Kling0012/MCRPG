package com.example.rpgplugin.trade;

import com.example.rpgplugin.trade.repository.TradeHistoryRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Type;
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

    // 申請時刻: requesterUuid -> timestamp
    private final Map<UUID, Long> requestTimestamps;

    // アクティブなトレードセッション: sessionId -> TradeSession
    private final Map<String, TradeSession> activeSessions;

    // プレイヤーUUID -> セッションIDのマップ（高速検索用）
    private final Map<UUID, String> playerToSession;

    // トレード履歴リポジトリ
    private TradeHistoryRepository historyRepository;

    // JSONシリアライズ用Gson
    private final Gson gson = new Gson();

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
        this.requestTimestamps = new ConcurrentHashMap<>();
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
        requestTimestamps.put(requesterUuid, System.currentTimeMillis());

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
        requestTimestamps.remove(requesterUuid);

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
        requestTimestamps.remove(requesterUuid);

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

        // インベントリ空き容量チェック
        if (!hasEnoughInventorySpace(session)) {
            cancelSession(session, "一方のプレイヤーのインベントリが満杯です");
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
     * インベントリ空き容量チェック
     *
     * @param session トレードセッション
     * @return 双方のインベントリに十分な空きがある場合true
     */
    private boolean hasEnoughInventorySpace(TradeSession session) {
        var p1 = session.getParty1();
        var p2 = session.getParty2();

        Player player1 = p1.getPlayer();
        Player player2 = p2.getPlayer();

        if (player1 == null || player2 == null) {
            return false;
        }

        // P1が受け取るアイテム数チェック
        int p1ReceiveCount = (int) p2.getOffer().getItems().stream().filter(i -> i != null && !i.getType().isAir()).count();
        int p1EmptySlots = (int) java.util.stream.IntStream.range(0, player1.getInventory().getSize())
                .filter(i -> player1.getInventory().getItem(i) == null ||
                           player1.getInventory().getItem(i).getType().isAir()).count();

        if (p1ReceiveCount > p1EmptySlots) {
            return false;
        }

        // P2が受け取るアイテム数チェック
        int p2ReceiveCount = (int) p1.getOffer().getItems().stream().filter(i -> i != null && !i.getType().isAir()).count();
        int p2EmptySlots = (int) java.util.stream.IntStream.range(0, player2.getInventory().getSize())
                .filter(i -> player2.getInventory().getItem(i) == null ||
                           player2.getInventory().getItem(i).getType().isAir()).count();

        return p2ReceiveCount <= p2EmptySlots;
    }

    /**
     * アイテムを交換
     *
     * @param session トレードセッション
     * @throws IllegalStateException インベントリが満杯でアイテムを追加できない場合
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
            HashMap<Integer, ItemStack> overflow = player2.getInventory().addItem(item);
            
            // インベントリに入りきらなかったアイテムをドロップ
            if (!overflow.isEmpty()) {
                for (ItemStack overflowItem : overflow.values()) {
                    player2.getWorld().dropItemNaturally(player2.getLocation(), overflowItem);
                    player2.sendMessage(ChatColor.YELLOW + "インベントリが満杯のため、アイテムが足元にドロップされました");
                }
            }
        }

        // P2のアイテムをP1に
        for (var item : p2.getOffer().getItems()) {
            player2.getInventory().removeItem(item);
            HashMap<Integer, ItemStack> overflow = player1.getInventory().addItem(item);
            
            // インベントリに入りきらなかったアイテムをドロップ
            if (!overflow.isEmpty()) {
                for (ItemStack overflowItem : overflow.values()) {
                    player1.getWorld().dropItemNaturally(player1.getLocation(), overflowItem);
                    player1.sendMessage(ChatColor.YELLOW + "インベントリが満杯のため、アイテムが足元にドロップされました");
                }
            }
        }
    }

    /**
 * ゴールドを交換
 *
 * @param session トレードセッション
 * @throws IllegalStateException ゴールド交換が失敗した場合
 */
private void exchangeGold(TradeSession session) {
    var p1 = session.getParty1();
    var p2 = session.getParty2();

    Player player1 = p1.getPlayer();
    Player player2 = p2.getPlayer();

    if (player1 == null || player2 == null) {
        throw new IllegalStateException("Both players must be online");
    }

    // CurrencyManagerを取得
    com.example.rpgplugin.currency.CurrencyManager currencyManager = plugin.getCurrencyManager();
    if (currencyManager == null) {
        String errorMsg = "CurrencyManager is not available, cannot execute trade with gold";
        logger.severe("[Trade] " + errorMsg);
        throw new IllegalStateException(errorMsg);
    }

    double gold1 = p1.getOffer().getGoldAmount();
    double gold2 = p2.getOffer().getGoldAmount();

    if (gold1 > 0 || gold2 > 0) {
        // P1のゴールドをP2に
        if (gold1 > 0) {
            if (!currencyManager.withdrawGold(player1, gold1)) {
                throw new IllegalStateException("Failed to withdraw gold from player1");
            }
            if (!currencyManager.depositGold(player2, gold1)) {
                // ロールバック
                currencyManager.depositGold(player1, gold1);
                throw new IllegalStateException("Failed to deposit gold to player2");
            }
            logger.info(String.format("[Trade] %s transferred %.2f gold to %s",
                player1.getName(), gold1, player2.getName()));
        }

        // P2のゴールドをP1に
        if (gold2 > 0) {
            if (!currencyManager.withdrawGold(player2, gold2)) {
                throw new IllegalStateException("Failed to withdraw gold from player2");
            }
            if (!currencyManager.depositGold(player1, gold2)) {
                // ロールバック
                currencyManager.depositGold(player2, gold2);
                throw new IllegalStateException("Failed to deposit gold to player1");
            }
            logger.info(String.format("[Trade] %s transferred %.2f gold to %s",
                player2.getName(), gold2, player1.getName()));
        }
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
        Iterator<Map.Entry<UUID, Long>> timestampIt = requestTimestamps.entrySet().iterator();
        while (timestampIt.hasNext()) {
            Map.Entry<UUID, Long> entry = timestampIt.next();
            UUID requesterUuid = entry.getKey();
            Long timestamp = entry.getValue();

            if (now - timestamp > REQUEST_TIMEOUT_MS) {
                // 申請を削除
                UUID targetUuid = pendingRequests.remove(requesterUuid);
                requestTimestamps.remove(requesterUuid);

                // 通知
                Player requester = Bukkit.getPlayer(requesterUuid);
                Player target = targetUuid != null ? Bukkit.getPlayer(targetUuid) : null;

                if (requester != null && requester.isOnline()) {
                    requester.sendMessage(ChatColor.YELLOW + "トレード申請が期限切れになりました");
                }
                if (target != null && target.isOnline()) {
                    target.sendMessage(ChatColor.YELLOW + "トレード申請が期限切れになりました");
                }

                logger.info(String.format("[Trade] Request expired: %s -> %s",
                    requester != null ? requester.getName() : "offline",
                    target != null ? target.getName() : "offline"));
            }
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
        String sessionId1 = playerToSession.get(uuid1);
        String sessionId2 = playerToSession.get(uuid2);

        if (sessionId1 != null) {
            TradeSession session1 = activeSessions.get(sessionId1);
            if (session1 != null) {
                cancelSession(session1, "新しいトレードが開始されました");
            }
        }

        if (sessionId2 != null) {
            TradeSession session2 = activeSessions.get(sessionId2);
            if (session2 != null) {
                cancelSession(session2, "新しいトレードが開始されました");
            }
        }
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
            try {
                // アイテムデータをJSONにシリアライズ
                Type itemListType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> p1ItemsData = new ArrayList<>();
                List<Map<String, Object>> p2ItemsData = new ArrayList<>();

                for (ItemStack item : session.getParty1().getOffer().getItems()) {
                    p1ItemsData.add(item.serialize());
                }

                for (ItemStack item : session.getParty2().getOffer().getItems()) {
                    p2ItemsData.add(item.serialize());
                }

                String p1ItemsJson = gson.toJson(p1ItemsData);
                String p2ItemsJson = gson.toJson(p2ItemsData);

                // トレード履歴レコードを作成
                TradeHistoryRepository.TradeHistoryRecord record =
                    new TradeHistoryRepository.TradeHistoryRecord(
                        session.getParty1().getUuid(),
                        session.getParty2().getUuid(),
                        p1ItemsJson,
                        p2ItemsJson,
                        session.getParty1().getOffer().getGoldAmount(),
                        session.getParty2().getOffer().getGoldAmount(),
                        System.currentTimeMillis() / 1000 // Unixタイムスタンプ（秒）
                    );

                // 保存
                boolean saved = historyRepository.save(record);
                if (saved) {
                    logger.info(String.format("[Trade] History saved for session %s", session.getSessionId()));
                } else {
                    logger.warning(String.format("[Trade] Failed to save history for session %s", session.getSessionId()));
                }
            } catch (Exception e) {
                logger.severe("Failed to save trade history: " + e.getMessage());
                e.printStackTrace();
            }
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
