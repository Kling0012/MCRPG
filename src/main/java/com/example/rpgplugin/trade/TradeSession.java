package com.example.rpgplugin.trade;

import com.example.rpgplugin.trade.model.TradeOffer;
import com.example.rpgplugin.trade.model.TradeParty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * トレードセッション
 *
 * <p>2人のプレイヤー間のトレードを管理します。</p>
 *
 * <p>トレードフロー:</p>
 * <ol>
 *   <li>双方がアイテム/ゴールドを提示</li>
 *   <li>双方が確認ボタンをクリック</li>
 *   <li>トレード実行（アイテム/ゴールドの交換）</li>
 * </ol>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class TradeSession {

    private final String sessionId;
    private final TradeParty party1;
    private final TradeParty party2;
    private final long creationTime;
    private final Logger logger;
    private TradeState state;
    private long lastActivityTime;

    /**
     * トレード状態
     */
    public enum TradeState {
        /** ネゴシエーション中（アイテム/ゴールドの追加・削除可能） */
        NEGOTIATING,
        /** 確認待ち（一方が確認済み） */
        WAITING_CONFIRMATION,
        /** トレード完了 */
        COMPLETED,
        /** トレードキャンセル */
        CANCELLED,
        /** 期限切れ */
        EXPIRED
    }

    /**
     * コンストラクタ
     *
     * @param party1 プレイヤー1
     * @param party2 プレイヤー2
     * @param logger ロガー
     */
    public TradeSession(Player party1, Player party2, Logger logger) {
        this.sessionId = generateSessionId(party1.getUniqueId(), party2.getUniqueId());
        this.party1 = new TradeParty(party1);
        this.party2 = new TradeParty(party2);
        this.creationTime = System.currentTimeMillis();
        this.lastActivityTime = creationTime;
        this.state = TradeState.NEGOTIATING;
        this.logger = logger;
    }

    /**
     * セッションIDを生成
     *
     * @param uuid1 UUID1
     * @param uuid2 UUID2
     * @return セッションID
     */
    private static String generateSessionId(UUID uuid1, UUID uuid2) {
        // ソートして一意性を保証
        int comparison = uuid1.compareTo(uuid2);
        UUID first = comparison < 0 ? uuid1 : uuid2;
        UUID second = comparison < 0 ? uuid2 : uuid1;
        return first.toString().substring(0, 8) + "-" + second.toString().substring(0, 8);
    }

    /**
     * プレイヤーを取得
     *
     * @param uuid プレイヤーUUID
     * @return プレイヤー（存在しない場合は空）
     */
    public Optional<TradeParty> getParty(UUID uuid) {
        if (party1.getUuid().equals(uuid)) {
            return Optional.of(party1);
        } else if (party2.getUuid().equals(uuid)) {
            return Optional.of(party2);
        }
        return Optional.empty();
    }

    /**
     * 相手方を取得
     *
     * @param uuid 自分のUUID
     * @return 相手方（存在しない場合は空）
     */
    public Optional<TradeParty> getCounterparty(UUID uuid) {
        if (party1.getUuid().equals(uuid)) {
            return Optional.of(party2);
        } else if (party2.getUuid().equals(uuid)) {
            return Optional.of(party1);
        }
        return Optional.empty();
    }

    /**
     * アイテムを追加
     *
     * @param player プレイヤー
     * @param slot スロット番号
     * @param item アイテム
     * @return 追加成功時true
     */
    public boolean addItem(Player player, int slot, org.bukkit.inventory.ItemStack item) {
        if (state != TradeState.NEGOTIATING) {
            return false;
        }

        return getParty(player.getUniqueId())
            .map(party -> {
                boolean added = party.getOffer().addItem(item);
                if (added) {
                    updateActivity();
                    // 相手の確認をリセット
                    resetCounterpartyConfirmation(player.getUniqueId());
                }
                return added;
            })
            .orElse(false);
    }

    /**
     * アイテムを削除
     *
     * @param player プレイヤー
     * @param slot スロット番号
     * @return 削除成功時true
     */
    public boolean removeItem(Player player, int slot) {
        if (state != TradeState.NEGOTIATING) {
            return false;
        }

        return getParty(player.getUniqueId())
            .map(party -> {
                org.bukkit.inventory.ItemStack removed = party.getOffer().removeItem(slot);
                if (removed != null) {
                    updateActivity();
                    // 相手の確認をリセット
                    resetCounterpartyConfirmation(player.getUniqueId());
                    // アイテムを返却
                    player.getInventory().addItem(removed);
                    return true;
                }
                return false;
            })
            .orElse(false);
    }

    /**
     * ゴールド額を設定
     *
     * @param player プレイヤー
     * @param amount ゴールド額
     * @return 設定成功時true
     */
    public boolean setGold(Player player, double amount) {
        if (state != TradeState.NEGOTIATING) {
            return false;
        }

        return getParty(player.getUniqueId())
            .map(party -> {
                party.getOffer().setGoldAmount(amount);
                updateActivity();
                // 相手の確認をリセット
                resetCounterpartyConfirmation(player.getUniqueId());
                return true;
            })
            .orElse(false);
    }

    /**
     * 確認状態をトグル
     *
     * @param player プレイヤー
     * @return 新しい確認状態
     */
    public boolean toggleConfirmation(Player player) {
        return getParty(player.getUniqueId())
            .map(party -> {
                boolean newState = !party.isReady();
                party.setReady(newState);
                updateActivity();

                // 確認状態に応じてセッション状態を更新
                updateSessionState();

                return newState;
            })
            .orElse(false);
    }

    /**
     * セッション状態を更新
     */
    private void updateSessionState() {
        boolean p1Ready = party1.isReady();
        boolean p2Ready = party2.isReady();

        if (p1Ready && p2Ready) {
            state = TradeState.WAITING_CONFIRMATION;
        } else {
            state = TradeState.NEGOTIATING;
        }
    }

    /**
     * 相手の確認をリセット
     *
     * @param uuid 自分のUUID
     */
    private void resetCounterpartyConfirmation(UUID uuid) {
        getCounterparty(uuid).ifPresent(counterparty -> {
            counterparty.setReady(false);
            counterparty.getOffer().setConfirmed(false);
        });
        state = TradeState.NEGOTIATING;
    }

    /**
     * トレードを実行可能かチェック
     *
     * @return 実行可能な場合true
     */
    public boolean canExecute() {
        return state == TradeState.WAITING_CONFIRMATION
            && party1.isReady()
            && party2.isReady();
    }

    /**
     * トレードをキャンセル
     *
     * @param reason キャンセル理由
     */
    public void cancel(String reason) {
        this.state = TradeState.CANCELLED;
        logger.info(String.format("[Trade] Session %s cancelled: %s", sessionId, reason));
    }

    /**
     * トレードを完了
     */
    public void complete() {
        this.state = TradeState.COMPLETED;
        logger.info(String.format("[Trade] Session %s completed", sessionId));
    }

    /**
     * 有効期限切れをチェック
     *
     * @param timeoutMs タイムアウト時間（ミリ秒）
     * @return 期限切れの場合true
     */
    public boolean isExpired(long timeoutMs) {
        long elapsed = System.currentTimeMillis() - lastActivityTime;
        if (elapsed > timeoutMs) {
            state = TradeState.EXPIRED;
            return true;
        }
        return false;
    }

    /**
     * アクティビティ時間を更新
     */
    private void updateActivity() {
        this.lastActivityTime = System.currentTimeMillis();
    }

    /**
     * セッションIDを取得
     *
     * @return セッションID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * プレイヤー1を取得
     *
     * @return プレイヤー1
     */
    public TradeParty getParty1() {
        return party1;
    }

    /**
     * プレイヤー2を取得
     *
     * @return プレイヤー2
     */
    public TradeParty getParty2() {
        return party2;
    }

    /**
     * 現在の状態を取得
     *
     * @return 現在の状態
     */
    public TradeState getState() {
        return state;
    }

    /**
     * 作成時刻を取得
     *
     * @return 作成時刻（ミリ秒）
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * 両プレイヤーがオンラインかチェック
     *
     * @return 両方オンラインの場合true
     */
    public boolean areBothOnline() {
        return party1.isOnline() && party2.isOnline();
    }

    @Override
    public String toString() {
        return String.format("TradeSession{id=%s, state=%s, p1=%s, p2=%s}",
            sessionId, state, party1.getUsername(), party2.getUsername());
    }
}
