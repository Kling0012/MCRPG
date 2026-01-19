package com.example.rpgplugin.stats;

import java.util.UUID;

/**
 * ステータス修正値を表すクラス
 *
 * <p>バフ、デバフ、装備補正などの一時的・永続的なステータス修正を管理します。</p>
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>Value Object: 不変オブジェクトとして設計</li>
 *   <li>DRY: 修正ロジックを一元管理</li>
 * </ul>
 *
 * <p>スレッド安全性:</p>
 * <ul>
 *   <li>不変オブジェクトのためスレッドセーフ</li>
 *   <li>外部同期なしで複数スレッドからアクセス可能</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class StatModifier {

    /**
     * 修正値のタイプ
     */
    public enum Type {
        /**
         * 加算修正（基本値に加算）
         *
         * <p>例: 装備でSTR+10</p>
         */
        FLAT,

        /**
         * 乗算修正（基本値に乗算）
         *
         * <p>例: バフでSTRが1.2倍</p>
         */
        MULTIPLIER,

        /**
         * 最終修正（最終値に加算）
         *
         * <p>例: パッシブスキルで最終STR+5</p>
         */
        FINAL
    }

    private final UUID id;
    private final String source;
    private final Type type;
    private final double value;
    private final long duration;
    private final long creationTime;

    /**
     * 永続的な修正値を作成します
     *
     * <p>永続的な修正値は時間経過で消えません。</p>
     *
     * @param source 修正元（例: "iron_sword", "strength_buff"）
     * @param type 修正タイプ
     * @param value 修正値
     */
    public StatModifier(String source, Type type, double value) {
        this(source, type, value, -1);
    }

    /**
     * 一時的な修正値を作成します
     *
     * <p>指定した時間（ミリ秒）が経過すると自動的に無効化されます。</p>
     *
     * @param source 修正元
     * @param type 修正タイプ
     * @param value 修正値
     * @param duration 有効期間（ミリ秒）。-1で永続
     */
    public StatModifier(String source, Type type, double value, long duration) {
        this.id = UUID.randomUUID();
        this.source = source;
        this.type = type;
        this.value = value;
        this.duration = duration;
        this.creationTime = System.currentTimeMillis();
    }

    /**
     * 修正元を取得します
     *
     * @return 修正元
     */
    public String getSource() {
        return source;
    }

    /**
     * 修正タイプを取得します
     *
     * @return 修正タイプ
     */
    public Type getType() {
        return type;
    }

    /**
     * 修正値を取得します
     *
     * @return 修正値
     */
    public double getValue() {
        return value;
    }

    /**
     * 修正値のIDを取得します
     *
     * @return 一意のID
     */
    public UUID getId() {
        return id;
    }

    /**
     * 有効期間を取得します
     *
     * @return 有効期間（ミリ秒）。-1は永続
     */
    public long getDuration() {
        return duration;
    }

    /**
     * 作成時刻を取得します
     *
     * @return 作成時刻（エポックミリ秒）
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * 有効期限を取得します
     *
     * @return 有効期限（エポックミリ秒）。永続の場合はLong.MAX_VALUE
     */
    public long getExpiryTime() {
        if (duration == -1) {
            return Long.MAX_VALUE;
        }
        return creationTime + duration;
    }

    /**
     * 残り有効時間を取得します
     *
     * @return 残り時間（ミリ秒）。永続の場合は-1
     */
    public long getRemainingTime() {
        if (duration == -1) {
            return -1;
        }

        long remaining = getExpiryTime() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /**
     * この修正値が有効期限切れか確認します
     *
     * @return 期限切れの場合はtrue、有効な場合はfalse
     */
    public boolean isExpired() {
        if (duration == -1) {
            return false;
        }
        return System.currentTimeMillis() > getExpiryTime();
    }

    /**
     * 永続的な修正値か確認します
     *
     * @return 永続的な場合はtrue、一時的な場合はfalse
     */
    public boolean isPermanent() {
        return duration == -1;
    }

    /**
     * 指定した基本値に対する修正結果を計算します
     *
     * <p>計算順序:</p>
     * <ol>
     *   <li>FLAT修正を加算</li>
     *   <li>MULTIPLIER修正を乗算</li>
     *   <li>FINAL修正を加算</li>
     * </ol>
     *
     * @param baseValue 基本値
     * @return 修正後の値
     */
    public double applyTo(double baseValue) {
        switch (type) {
            case FLAT:
                return baseValue + value;
            case MULTIPLIER:
                return baseValue * (1.0 + value);
            case FINAL:
                return baseValue + value;
            default:
                return baseValue;
        }
    }

    /**
     * 新しい値を持つコピーを作成します
     *
     * <p>元のオブジェクトは変更されません。</p>
     *
     * @param newValue 新しい値
     * @return コピーされた修正値
     */
    public StatModifier withValue(double newValue) {
        return new StatModifier(source, type, newValue, duration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatModifier that = (StatModifier) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        String durationStr = isPermanent() ? "永続" : (getRemainingTime() / 1000) + "秒";
        String valueStr = type == Type.MULTIPLIER ? (value >= 0 ? "+" : "") + (value * 100) + "%"
                                            : (value >= 0 ? "+" : "") + value;

        return source + ": " + valueStr + " (" + type + ", " + durationStr + ")";
    }
}
