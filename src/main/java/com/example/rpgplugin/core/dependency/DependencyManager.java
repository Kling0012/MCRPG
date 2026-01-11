package com.example.rpgplugin.core.dependency;

import com.example.rpgplugin.RPGPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 依存関係管理クラス
 *
 * <p>外部プラグインとの連携を管理し、依存関係のチェックと初期化を行います。
 * Vault、MythicMobs、PlaceholderAPIなどとの連携を提供します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>DIP（依存性逆転の原則）: 具体的なフック実装ではなく抽象に依存</li>
 *   <li>ファサードパターン: 外部プラグイン連携への統一的インターフェース</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class DependencyManager {

    private final JavaPlugin plugin;
    private final Logger logger;

    // 依存プラグインのフック
    private VaultHook vaultHook;
    private MythicMobsHook mythicMobsHook;
    private PlaceholderHook placeholderHook;

    // 依存状態
    private boolean vaultAvailable = false;
    private boolean mythicMobsAvailable = false;
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
     * <p>必須依存（Vault, MythicMobs）が欠けている場合はfalseを返します。
     * オプション依存（PlaceholderAPI）は警告のみを出力します。</p>
     *
     * @return 全必須依存が利用可能な場合はtrue、それ以外はfalse
     */
    public boolean setupDependencies() {
        logger.info("Checking plugin dependencies...");

        List<String> missingDependencies = new ArrayList<>();

        // Vaultのチェック（必須）
        if (checkVault()) {
            vaultAvailable = true;
            logger.info("Vault dependency found and hooked.");
        } else {
            missingDependencies.add("Vault");
        }

        // MythicMobsのチェック（必須）
        if (checkMythicMobs()) {
            mythicMobsAvailable = true;
            logger.info("MythicMobs dependency found and hooked.");
        } else {
            missingDependencies.add("MythicMobs");
        }

        // PlaceholderAPIのチェック（オプション）
        if (checkPlaceholderAPI()) {
            placeholderApiAvailable = true;
            logger.info("PlaceholderAPI dependency found (optional).");
        } else {
            logger.info("PlaceholderAPI not found. Expansion features disabled.");
        }

        // 必須依存のチェック
        if (!missingDependencies.isEmpty()) {
            logger.severe("========================================");
            logger.severe("MISSING REQUIRED DEPENDENCIES!");
            logger.severe("The following plugins are required:");
            for (String dep : missingDependencies) {
                logger.severe("  - " + dep);
            }
            logger.severe("========================================");
            logger.severe("");
            logger.severe("Please install the required plugins and restart the server.");
            logger.severe("Download links:");
            logger.severe("  Vault: https://www.spigotmc.org/resources/vault.34315/");
            logger.severe("  MythicMobs: https://www.spigotmc.org/resources/mythicmobs.5702/");
            logger.severe("========================================");

            return false;
        }

        logger.info("All required dependencies are available!");
        return true;
    }

    /**
     * Vaultプラグインをチェックしてフックします
     *
     * @return 利用可能な場合はtrue
     */
    private boolean checkVault() {
        Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");

        if (vaultPlugin == null || !vaultPlugin.isEnabled()) {
            return false;
        }

        try {
            vaultHook = new VaultHook(plugin);
            if (vaultHook.setup()) {
                logger.info("Vault hooked successfully:");
                logger.info("  Economy: " + (vaultHook.hasEconomy() ? "Available" : "Not available"));
                logger.info("  Chat: " + (vaultHook.hasChat() ? "Available" : "Not available"));
                logger.info("  Permission: " + (vaultHook.hasPermission() ? "Available" : "Not available"));
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to hook Vault", e);
        }

        return false;
    }

    /**
     * MythicMobsプラグインをチェックしてフックします
     *
     * @return 利用可能な場合はtrue
     */
    private boolean checkMythicMobs() {
        Plugin mythicMobsPlugin = Bukkit.getPluginManager().getPlugin("MythicMobs");

        if (mythicMobsPlugin == null || !mythicMobsPlugin.isEnabled()) {
            return false;
        }

        try {
            mythicMobsHook = new MythicMobsHook(plugin);
            if (mythicMobsHook.setup()) {
                String version = mythicMobsHook.getVersion();
                logger.info("MythicMobs hooked successfully (version: " + version + ")");
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to hook MythicMobs", e);
        }

        return false;
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

        // RPGPluginにキャストできるか確認
        if (!(plugin instanceof RPGPlugin rpgPlugin)) {
            logger.warning("PlaceholderAPI hook requires RPGPlugin instance, skipping.");
            return false;
        }

        try {
            // PlaceholderExpansionを登録
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
            logger.log(Level.WARNING, "Failed to hook PlaceholderAPI", e);
        }

        return false;
    }

    /**
     * Vaultフックを取得します
     *
     * @return VaultHookインスタンス、利用不可能な場合はnull
     */
    public VaultHook getVaultHook() {
        return vaultHook;
    }

    /**
     * MythicMobsフックを取得します
     *
     * @return MythicMobsHookインスタンス、利用不可能な場合はnull
     */
    public MythicMobsHook getMythicMobsHook() {
        return mythicMobsHook;
    }

    /**
     * Vaultが利用可能か確認します
     *
     * @return 利用可能な場合はtrue
     */
    public boolean isVaultAvailable() {
        return vaultAvailable;
    }

    /**
     * MythicMobsが利用可能か確認します
     *
     * @return 利用可能な場合はtrue
     */
    public boolean isMythicMobsAvailable() {
        return mythicMobsAvailable;
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
     *
     * <p>デバッグや状態確認のために使用します。</p>
     */
    public void logDependencyStatus() {
        logger.info("=== Dependency Status ===");
        logger.info("Vault: " + (vaultAvailable ? "✓ Available" : "✗ Not available"));
        logger.info("MythicMobs: " + (mythicMobsAvailable ? "✓ Available" : "✗ Not available"));
        logger.info("PlaceholderAPI: " + (placeholderApiAvailable ? "✓ Available (Optional)" : "✗ Not available (Optional)"));
        logger.info("========================");
    }

    /**
     * 依存関係をクリーンアップします
     *
     * <p>プラグイン無効化時に呼び出します。</p>
     */
    public void cleanup() {
        if (vaultHook != null) {
            vaultHook.cleanup();
            vaultHook = null;
        }

        if (mythicMobsHook != null) {
            mythicMobsHook.cleanup();
            mythicMobsHook = null;
        }

        if (placeholderHook != null) {
            placeholderHook.unregister();
            placeholderHook = null;
        }

        vaultAvailable = false;
        mythicMobsAvailable = false;
        placeholderApiAvailable = false;

        logger.info("Dependency manager cleaned up.");
    }
}
