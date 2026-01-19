package com.example.rpgplugin.core.module;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * モジュールインターフェース
 *
 * <p>プラグインの機能拡張のためのモジュールシステムの基底インターフェースです。
 * 各モジュールはこのインターフェースを実装し、ModuleManagerに登録されます。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SRP（単一責任の原則）: 各モジュールは単一の機能領域を担当</li>
 *   <li>OCP（開放閉鎖の原則）: モジュールの追加は可能だが、既存モジュールの修正は不要</li>
 *   <li>DIP（依存性逆転の原則）: 高水準モジュールはIModule抽象に依存</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public interface IModule {

    /**
     * モジュールを有効化します
     *
     * <p>このメソッドはプラグイン起動時またはModuleManager.enableAll()呼び出し時に実行されます。
     * モジュールの初期化処理、イベントリスナーの登録、コマンドの登録などを行います。</p>
     *
     * @param plugin プラグインインスタンス
     * @throws ModuleException モジュールの有効化に失敗した場合
     */
    void enable(JavaPlugin plugin) throws ModuleException;

    /**
     * モジュールを無効化します
     *
     * <p>このメソッドはプラグイン停止時またはModuleManager.disableAll()呼び出し時に実行されます。
     * リソースの解放、イベントリスナーの解除、データの保存などを行います。</p>
     */
    void disable();

    /**
     * モジュールをリロードします
     *
     * <p>このメソッドは実行中にモジュールの設定を再読み込みする際に呼び出されます。
     * キャッシュのクリア、設定ファイルの再読み込みなどを行います。</p>
     *
     * <p>デフォルト実装ではdisable() → enable()の順に実行します。
     * 必要に応じてオーバーライドして最適化してください。</p>
     *
     * @param plugin プラグインインスタンス
     * @throws ModuleException モジュールのリロードに失敗した場合
     */
    default void reload(JavaPlugin plugin) throws ModuleException {
        disable();
        enable(plugin);
    }

    /**
     * モジュール名を取得します
     *
     * <p>モジュール名は一意である必要があります。
     * ログ出力、デバッグ、管理画面などで使用されます。</p>
     *
     * @return モジュール名（nullまたは空文字列不可）
     */
    String getName();

    /**
     * モジュールバージョンを取得します
     *
     * @return バージョン文字列（例: "1.0.0"）
     */
    String getVersion();

    /**
     * モジュールの説明を取得します
     *
     * <p>デフォルト実装では空文字列を返します。
     * 必要に応じてオーバーライドしてください。</p>
     *
     * @return モジュールの説明
     */
    default String getDescription() {
        return "";
    }

    /**
     * モジュールが有効化されているか確認します
     *
     * <p>デフォルト実装ではfalseを返します。
     * 実装クラスで適切に状態管理を行ってください。</p>
     *
     * @return 有効化されている場合はtrue、それ以外はfalse
     */
    default boolean isEnabled() {
        return false;
    }

    /**
     * このモジュールが依存する他のモジュール名の配列を取得します
     *
     * <p>依存関係はModuleManagerによる起動順序の決定に使用されます。
     * 依存するモジュールが見つからない場合、このモジュールは有効化されません。</p>
     *
     * <p>デフォルト実装では空の配列を返します（依存関係なし）。</p>
     *
     * @return 依存モジュール名の配列
     */
    default String[] getDependencies() {
        return new String[0];
    }

    /**
     * モジュール例外
     *
     * <p>モジュールの有効化・リロード中に発生した例外をラップします。</p>
     */
    class ModuleException extends Exception {
        private static final long serialVersionUID = 1L;

        public ModuleException(String message) {
            super(message);
        }

        public ModuleException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
