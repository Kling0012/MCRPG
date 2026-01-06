package com.example.rpgplugin.trade.model;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * トレード参加者
 *
 * <p>トレードに参加するプレイヤーの情報と状態を管理します。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class TradeParty {

    private final UUID uuid;
    private final String username;
    private final TradeOffer offer;
    private Player player;
    private boolean ready;
    private long lastActionTime;

    /**
     * コンストラクタ
     *
     * @param player プレイヤー
     */
    public TradeParty(Player player) {
        this.uuid = player.getUniqueId();
        this.username = player.getName();
        this.player = player;
        this.offer = new TradeOffer(uuid);
        this.ready = false;
        this.lastActionTime = System.currentTimeMillis();
    }

    /**
     * プレイヤーを更新
     *
     * @param player プレイヤー
     */
    public void updatePlayer(Player player) {
        this.player = player;
        this.lastActionTime = System.currentTimeMillis();
    }

    /**
     * 準備完了状態を設定
     *
     * @param ready 準備完了状態
     */
    public void setReady(boolean ready) {
        this.ready = ready;
        this.lastActionTime = System.currentTimeMillis();
    }

    /**
     * UUIDを取得
     *
     * @return UUID
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * ユーザー名を取得
     *
     * @return ユーザー名
     */
    public String getUsername() {
        return username;
    }

    /**
     * プレイヤーを取得
     *
     * @return プレイヤー（オンラインの場合）
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * オファーを取得
     *
     * @return トレードオファー
     */
    public TradeOffer getOffer() {
        return offer;
    }

    /**
     * 準備完了状態を取得
     *
     * @return 準備完了状態
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * 最終アクション時刻を取得
     *
     * @return 最終アクション時刻（ミリ秒）
     */
    public long getLastActionTime() {
        return lastActionTime;
    }

    /**
     * プレイヤーがオンラインかチェック
     *
     * @return オンラインの場合true
     */
    public boolean isOnline() {
        return player != null && player.isOnline();
    }

    @Override
    public String toString() {
        return String.format("TradeParty{uuid=%s, name=%s, ready=%s, offer=%s}",
            uuid, username, ready, offer);
    }
}
