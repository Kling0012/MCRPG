package com.example.rpgplugin.api.skript.events;

import ch.njol.skript.Skript;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Skriptテスト用ブートストラップクラス。
 *
 * <p>このクラスはテスト環境でのSkriptの初期化を担当します。
 * 初期化オンデマンドホルダーイディオムを使用することで、スレッドセーフかつ遅延初期化を実現しています。</p>
 *
 * @see <a href="https://www.javaalmanac.com/effective-java/item-83-use-lazy-initialization-judiciously">Effective Java Item 83</a>
 */
final class SkriptTestBootstrap {

    private SkriptTestBootstrap() {
        // ユーティリティクラスのためインスタンス化を禁止
    }

    /**
     * Skriptの初期化を保証します。
     *
     * <p>このメソッドはHolderクラスを参照することで、
     * クラスロード時の静的初期化ブロックの実行をトリガーします。
     * Java言語仕様により静的初期化はスレッドセーフであることが保証されています。</p>
     */
    static void ensureInitialized() {
        // Holderクラスを参照することで静的初期化をトリガー
        Holder.initialize();
    }

    /**
     * 初期化オンデマンドホルダーイディオム。
     *
     * <p>静的初期化ブロックはHolderクラスが最初に参照されたときに1回だけ実行され、
     * Java言語仕様によりスレッドセーフであることが保証されています。</p>
     */
    private static final class Holder {

        static {
            try {
                initializeSkript();
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError("Failed to initialize Skript for tests: " + e.getMessage());
            }
        }

        /**
         * Skriptの初期化処理を実行します。
         */
        static void initialize() {
            // 静的初期化ブロックの実行を保証するダミーメソッド
        }

        /**
         * Skriptの初期化を実行します。
         */
        private static void initializeSkript() throws ReflectiveOperationException {
            // Skriptインスタンスの初期化
            Field instanceField = Skript.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            Object instance = instanceField.get(null);
            if (instance == null) {
                instance = allocateInstance(Skript.class);
                instanceField.set(null, instance);
            }
            setJavaPluginEnabled(instance, true);

            // Skript APIの初期化
            Field skriptField = Skript.class.getDeclaredField("skript");
            skriptField.setAccessible(true);
            if (skriptField.get(null) == null) {
                skriptField.set(null, org.skriptlang.skript.Skript.of(SkriptTestBootstrap.class, "test"));
            }

            // 不変Skriptインスタンスの初期化
            Field unmodifiableField = Skript.class.getDeclaredField("unmodifiableSkript");
            unmodifiableField.setAccessible(true);
            if (unmodifiableField.get(null) == null) {
                org.skriptlang.skript.Skript skriptInstance =
                        (org.skriptlang.skript.Skript) skriptField.get(null);
                unmodifiableField.set(null, skriptInstance.unmodifiableView());
            }

            // 登録受け入れフラグの設定
            Field acceptField = Skript.class.getDeclaredField("acceptRegistrations");
            acceptField.setAccessible(true);
            acceptField.setBoolean(null, true);
        }

        /**
         * リフレクションを使用してクラスのインスタンスを生成します。
         *
         * <p>このメソッドはsun.misc.Unsafeの代わりに標準Reflection APIを使用します。
         * JDK 9以降のモジュールシステムと互換性があります。</p>
         *
         * @param type インスタンスを生成するクラス
         * @return 生成されたインスタンス
         * @throws ReflectiveOperationException インスタンス生成に失敗した場合
         */
        private static Object allocateInstance(Class<?> type) throws ReflectiveOperationException {
            Constructor<?> ctor = type.getDeclaredConstructor();
            if (!ctor.canAccess(null)) {
                ctor.setAccessible(true);
            }
            return ctor.newInstance();
        }

        /**
         * JavaPluginの有効状態を設定します。
         *
         * @param instance Skriptインスタンス
         * @param enabled 有効にする場合はtrue
         * @throws ReflectiveOperationException フィールドアクセスに失敗した場合
         */
        private static void setJavaPluginEnabled(Object instance, boolean enabled) throws ReflectiveOperationException {
            Field enabledField = JavaPlugin.class.getDeclaredField("isEnabled");
            enabledField.setAccessible(true);
            enabledField.setBoolean(instance, enabled);
        }
    }
}
