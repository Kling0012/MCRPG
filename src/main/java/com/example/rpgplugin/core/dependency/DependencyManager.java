package com.example.rpgplugin.core.dependency;

import com.example.rpgplugin.RPGPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * 依存関係管理クラス
 *
 * <p>外部プラグインとの連携を管理し、依存関係のチェックと初期化を行います。
 * PlaceholderAPIとの連携を提供します。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class DependencyManager {

    private final JavaPlugin plugin;
    private final Logger logger;

    // 依存プラグインのフック
    private PlaceholderHook placeholderHook;

    // 依存状態
    private boolean placeholderApiAvailable = false;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public DependencyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * 全依存関係をチェックして初期化します
     *
     * @return 常にtrue（オプション依存のみのため）
     */
    public boolean setupDependencies() {
        logger.info("Checking plugin dependencies...");

        // PlaceholderAPIのチェック（オプション）
        if (checkPlaceholderAPI()) {
            placeholderApiAvailable = true;
            logger.info("PlaceholderAPI dependency found (optional).");
        } else {
            logger.info("PlaceholderAPI not found. Expansion features disabled.");
        }

        logger.info("Dependency check complete.");
        return true;
    }

    /**
     * PlaceholderAPIプラグインをチェックしてPlaceholderExpansionを登録します
     *
     * @return 利用可能な場合はtrue
     */
    private boolean checkPlaceholderAPI() {
        Plugin papiPlugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");

        if (papiPlugin == null || !papiPlugin.isEnabled()) {
            return false;
        }

        if (!(plugin instanceof RPGPlugin rpgPlugin)) {
            logger.warning("PlaceholderAPI hook requires RPGPlugin instance, skipping.");
            return false;
        }

        try {
            placeholderHook = new PlaceholderHook(rpgPlugin);
            boolean registered = placeholderHook.register();

            if (registered) {
                logger.info("PlaceholderAPI hooked successfully:");
                logger.info("  Version: " + papiPlugin.getPluginMeta().getVersion());
                logger.info("  Expansion: RPGPlugin v" + placeholderHook.getVersion());
                logger.info("  Identifier: %" + placeholderHook.getIdentifier() + "_<param>%");
                return true;
            } else {
                logger.warning("Failed to register PlaceholderExpansion");
                return false;
            }
        } catch (Exception e) {
            logger.warning("Failed to hook PlaceholderAPI: " + e.getMessage());
        }

        return false;
    }

    /**
     * PlaceholderHookを取得します
     *
     * @return PlaceholderHookインスタンス、利用不可能な場合はnull
     */
    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }

    /**
     * PlaceholderAPIが利用可能か確認します
     *
     * @return 利用可能な場合はtrue
     */
    public boolean isPlaceholderApiAvailable() {
        return placeholderApiAvailable;
    }

    /**
     * 依存関係の状態をログに出力します
     */
    public void logDependencyStatus() {
        logger.info("=== Dependency Status ===");
        logger.info("PlaceholderAPI: " + (placeholderApiAvailable ? "✓ Available (Optional)" : "✗ Not available (Optional)"));
        logger.info("========================");
    }

    /**
     * 依存関係をクリーンアップします
     */
    public void cleanup() {
        if (placeholderHook != null) {
            placeholderHook.unregister();
            placeholderHook = null;
        }

        placeholderApiAvailable = false;
        logger.info("Dependency manager cleaned up.");
    }
}
