package com.example.rpgplugin.currency;

import com.example.rpgplugin.storage.models.PlayerCurrency;
import com.example.rpgplugin.storage.repository.PlayerCurrencyRepository;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 通貨マネージャー
 * 全プレイヤーの通貨管理とキャッシュを担当
 */
public class CurrencyManager {

    private final PlayerCurrencyRepository repository;
    private final Logger logger;

    // キャッシュ: UUID -> PlayerCurrency
    private final ConcurrentHashMap<UUID, PlayerCurrency> currencyCache;

    public CurrencyManager(PlayerCurrencyRepository repository, Logger logger) {
        this.repository = repository;
        this.logger = logger;
        this.currencyCache = new ConcurrentHashMap<>();
    }

    /**
     * プレイヤーの通貨データをロード
     *
     * @param uuid プレイヤーUUID
     * @return 通貨データ
     */
    public PlayerCurrency loadPlayerCurrency(UUID uuid) {
        // キャッシュに存在する場合はそれを返す
        if (currencyCache.containsKey(uuid)) {
            return currencyCache.get(uuid);
        }

        try {
            // データベースからロードまたは新規作成
            PlayerCurrency currency = repository.getOrCreate(uuid);
            currencyCache.put(uuid, currency);
            logger.info("[Currency] Loaded currency for player: " + uuid);
            return currency;
        } catch (SQLException e) {
            logger.severe("[Currency] Failed to load player currency: " + e.getMessage());
            e.printStackTrace();
            // フォールバック: 新規作成
            PlayerCurrency currency = new PlayerCurrency(uuid);
            currencyCache.put(uuid, currency);
            return currency;
        }
    }

    /**
     * プレイヤーの通貨データをアンロード
     *
     * @param uuid プレイヤーUUID
     */
    public void unloadPlayerCurrency(UUID uuid) {
        PlayerCurrency currency = currencyCache.remove(uuid);
        if (currency != null) {
            repository.saveAsync(currency);
            logger.info("[Currency] Unloaded currency for player: " + uuid);
        }
    }

    /**
     * 全プレイヤーの通貨データを保存
     */
    public void saveAll() {
        logger.info("[Currency] Saving all player currencies...");
        currencyCache.forEach((uuid, currency) -> {
            try {
                repository.save(currency);
            } catch (SQLException e) {
                logger.severe("[Currency] Failed to save currency for " + uuid + ": " + e.getMessage());
            }
        });
        logger.info("[Currency] Saved " + currencyCache.size() + " player currencies");
    }

    /**
     * ゴールド残高を取得
     *
     * @param player プレイヤー
     * @return ゴールド残高
     */
    public double getGoldBalance(Player player) {
        PlayerCurrency currency = currencyCache.get(player.getUniqueId());
        if (currency == null) {
            currency = loadPlayerCurrency(player.getUniqueId());
        }
        return currency.getGoldBalance();
    }

    /**
 * ゴールドを入金
 *
 * @param player プレイヤー
 * @param amount 入金額
 * @return 成功した場合はtrue
 */
public boolean depositGold(Player player, double amount) {
    if (amount <= 0) {
        return false;
    }

    PlayerCurrency currency = currencyCache.get(player.getUniqueId());
    if (currency == null) {
        try {
            currency = loadPlayerCurrency(player.getUniqueId());
        } catch (Exception e) {
            logger.severe("[Currency] Failed to load currency for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }

    // 二重チェック（loadPlayerCurrencyのフォールバック後も安全確保）
    if (currency == null) {
        logger.severe("[Currency] Currency is null after loading for " + player.getName());
        return false;
    }

    currency.deposit(amount);
    repository.saveAsync(currency);

    logger.fine("[Currency] Deposited " + amount + "G to " + player.getName());
    return true;
}

    /**
 * ゴールドを出金
 *
 * @param player プレイヤー
 * @param amount 出金額
 * @return 成功した場合はtrue
 */
public boolean withdrawGold(Player player, double amount) {
    if (amount <= 0) {
        return false;
    }

    PlayerCurrency currency = currencyCache.get(player.getUniqueId());
    if (currency == null) {
        try {
            currency = loadPlayerCurrency(player.getUniqueId());
        } catch (Exception e) {
            logger.severe("[Currency] Failed to load currency for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }

    // 二重チェック（loadPlayerCurrencyのフォールバック後も安全確保）
    if (currency == null) {
        logger.severe("[Currency] Currency is null after loading for " + player.getName());
        return false;
    }

    if (currency.withdraw(amount)) {
        repository.saveAsync(currency);
        logger.fine("[Currency] Withdrew " + amount + "G from " + player.getName());
        return true;
    }

    logger.fine("[Currency] Failed to withdraw " + amount + "G from " + player.getName() + " (insufficient balance)");
    return false;
}

    /**
     * ゴールド残高が足りているか確認
     *
     * @param player プレイヤー
     * @param amount 必要な金額
     * @return 残高が足りている場合はtrue
     */
    public boolean hasEnoughGold(Player player, double amount) {
        PlayerCurrency currency = currencyCache.get(player.getUniqueId());
        if (currency == null) {
            currency = loadPlayerCurrency(player.getUniqueId());
        }
        return currency.hasEnough(amount);
    }

    /**
 * ゴールドを転送
 *
 * @param from 送金元プレイヤー
 * @param to 送金先プレイヤー
 * @param amount 送金額
 * @return 成功した場合はtrue
 */
public boolean transferGold(Player from, Player to, double amount) {
    if (amount <= 0) {
        return false;
    }

    PlayerCurrency fromCurrency = currencyCache.get(from.getUniqueId());
    if (fromCurrency == null) {
        try {
            fromCurrency = loadPlayerCurrency(from.getUniqueId());
        } catch (Exception e) {
            logger.severe("[Currency] Failed to load currency for " + from.getName() + ": " + e.getMessage());
            return false;
        }
    }

    // 二重チェック
    if (fromCurrency == null) {
        logger.severe("[Currency] fromCurrency is null after loading for " + from.getName());
        return false;
    }

    PlayerCurrency toCurrency = currencyCache.get(to.getUniqueId());
    if (toCurrency == null) {
        try {
            toCurrency = loadPlayerCurrency(to.getUniqueId());
        } catch (Exception e) {
            logger.severe("[Currency] Failed to load currency for " + to.getName() + ": " + e.getMessage());
            return false;
        }
    }

    // 二重チェック
    if (toCurrency == null) {
        logger.severe("[Currency] toCurrency is null after loading for " + to.getName());
        return false;
    }

    // 残高チェック
    if (!fromCurrency.hasEnough(amount)) {
        logger.fine("[Currency] Transfer failed: " + from.getName() + " has insufficient balance");
        return false;
    }

    // 転送実行
    fromCurrency.withdraw(amount);
    toCurrency.deposit(amount);

    // 非同期保存
    repository.saveAsync(fromCurrency);
    repository.saveAsync(toCurrency);

    logger.info("[Currency] Transferred " + amount + "G from " + from.getName() + " to " + to.getName());
    return true;
}

    /**
     * キャッシュ済みの通貨データを取得
     *
     * @param uuid プレイヤーUUID
     * @return 通貨データ（キャッシュに存在しない場合はnull）
     */
    public PlayerCurrency getCachedCurrency(UUID uuid) {
        return currencyCache.get(uuid);
    }

    /**
     * キャッシュサイズを取得
     *
     * @return キャッシュされているプレイヤー数
     */
    public int getCacheSize() {
        return currencyCache.size();
    }

    /**
     * 通貨統計情報をログ出力
     */
    public void logStatistics() {
        try {
            long totalPlayers = repository.count();
            double totalGold = currencyCache.values().stream()
                    .mapToDouble(PlayerCurrency::getGoldBalance)
                    .sum();
            double totalEarned = currencyCache.values().stream()
                    .mapToDouble(PlayerCurrency::getTotalEarned)
                    .sum();
            double totalSpent = currencyCache.values().stream()
                    .mapToDouble(PlayerCurrency::getTotalSpent)
                    .sum();

            logger.info("[Currency] Statistics:");
            logger.info("  - Total Players: " + totalPlayers);
            logger.info("  - Cached Players: " + currencyCache.size());
            logger.info("  - Total Gold in Circulation: " + String.format("%.2f", totalGold) + "G");
            logger.info("  - Total Earned: " + String.format("%.2f", totalEarned) + "G");
            logger.info("  - Total Spent: " + String.format("%.2f", totalSpent) + "G");
        } catch (SQLException e) {
            logger.warning("[Currency] Failed to get statistics: " + e.getMessage());
        }
    }
}
