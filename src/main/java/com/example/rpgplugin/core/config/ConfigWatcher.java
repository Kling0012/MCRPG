package com.example.rpgplugin.core.config;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 設定ファイル監視クラス
 *
 * <p>WatchService APIを使用してファイルの変更を監視し、ホットリロードを実現します。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class ConfigWatcher {

    private final Plugin plugin;
    private final Logger logger;
    private final YamlConfigManager configManager;
    private final Map<WatchKey, Path> watchKeys;
    private final Map<WatchKey, String> directoryNames; // ディレクトリ名のマッピング
    private final Map<String, List<Consumer<Path>>> fileListeners;
    private final Map<String, List<Consumer<Path>>> directoryListeners;

    private WatchService watchService;
    private Thread watchThread;
    private volatile boolean running;

    /**
     * コンストラクタ
     *
     * @param plugin プラグイン
     * @param configManager 設定マネージャー
     */
    public ConfigWatcher(@NotNull Plugin plugin, @NotNull YamlConfigManager configManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.configManager = configManager;
        this.watchKeys = new ConcurrentHashMap<>();
        this.directoryNames = new ConcurrentHashMap<>();
        this.fileListeners = new ConcurrentHashMap<>();
        this.directoryListeners = new ConcurrentHashMap<>();
    }

    /**
     * 監視を開始します
     *
     * @return 成功した場合はtrue
     */
    public boolean start() {
        if (running) {
            logger.warning("ConfigWatcher is already running");
            return false;
        }

        try {
            watchService = FileSystems.getDefault().newWatchService();
            running = true;

            // 監視スレッドを起動
            watchThread = new Thread(this::watchLoop, "ConfigWatcherThread");
            watchThread.setDaemon(true);
            watchThread.start();

            logger.info("ConfigWatcher started");
            return true;

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to start ConfigWatcher", e);
            return false;
        }
    }

    /**
     * 監視を停止します
     */
    public void stop() {
        if (!running) {
            return;
        }

        running = false;

        // 監視スレッドを中断
        if (watchThread != null) {
            watchThread.interrupt();
        }

        // WatchServiceをクローズ
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to close WatchService", e);
            }
        }

        // 監視キーをクリア
        watchKeys.clear();
        directoryNames.clear();

        logger.info("ConfigWatcher stopped");
    }

    /**
     * ディレクトリを監視します
     *
     * @param directoryPath ディレクトリパス
     * @return 成功した場合はtrue
     */
    public boolean watchDirectory(@NotNull String directoryPath) {
        if (watchService == null) {
            logger.warning("WatchService is not initialized");
            return false;
        }

        File directory = new File(plugin.getDataFolder(), directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        if (!directory.isDirectory()) {
            logger.warning("Not a directory: " + directoryPath);
            return false;
        }

        try {
            Path path = directory.toPath();
            WatchKey key = path.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
            );

            watchKeys.put(key, path);
            directoryNames.put(key, directoryPath);
            logger.info("Watching directory: " + directoryPath);
            return true;

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to watch directory: " + directoryPath, e);
            return false;
        }
    }

    /**
     * ファイル監視リスナーを登録します
     *
     * @param fileName ファイル名
     * @param listener リスナー
     */
    public void addFileListener(@NotNull String fileName, @NotNull Consumer<Path> listener) {
        fileListeners.computeIfAbsent(fileName, k -> new ArrayList<>()).add(listener);
    }

    /**
     * ディレクトリ監視リスナーを登録します
     *
     * <p>指定したディレクトリ内のYAMLファイルが変更されたときに通知されます。</p>
     *
     * @param directoryPath ディレクトリパス
     * @param listener リスナー
     */
    public void addDirectoryListener(@NotNull String directoryPath, @NotNull Consumer<Path> listener) {
        directoryListeners.computeIfAbsent(directoryPath, k -> new ArrayList<>()).add(listener);
    }

    /**
     * ファイル変更を通知します
     *
     * @param fileName ファイル名
     * @param path ファイルパス
     */
    private void notifyFileListeners(@NotNull String fileName, @NotNull Path path) {
        List<Consumer<Path>> listeners = fileListeners.get(fileName);
        if (listeners != null) {
            for (Consumer<Path> listener : listeners) {
                try {
                    listener.accept(path);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error in file listener for: " + fileName, e);
                }
            }
        }
    }

    /**
     * ディレクトリ変更を通知します
     *
     * @param directoryPath ディレクトリパス
     * @param path ファイルパス
     */
    private void notifyDirectoryListeners(@NotNull String directoryPath, @NotNull Path path) {
        List<Consumer<Path>> listeners = directoryListeners.get(directoryPath);
        if (listeners != null) {
            for (Consumer<Path> listener : listeners) {
                try {
                    listener.accept(path);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error in directory listener for: " + directoryPath, e);
                }
            }
        }
    }

    /**
     * 監視ループ
     */
    private void watchLoop() {
        logger.info("ConfigWatcher loop started");

        while (running) {
            try {
                // イベントを待機（1秒タイムアウト）
                WatchKey key = watchService.poll(1, java.util.concurrent.TimeUnit.SECONDS);

                if (key == null) {
                    continue;
                }

                Path watchPath = watchKeys.get(key);
                String directoryName = directoryNames.get(key);
                if (watchPath == null) {
                    logger.warning("Unknown watch key");
                    key.reset();
                    continue;
                }

                // イベントを処理
                for (WatchEvent<?> event : key.pollEvents()) {
                    handleEvent(watchPath, directoryName, event);
                }

                // キーをリセット
                boolean valid = key.reset();
                if (!valid) {
                    logger.warning("Watch key no longer valid: " + watchPath);
                    watchKeys.remove(key);
                    directoryNames.remove(key);
                }

            } catch (InterruptedException e) {
                // スレッドが中断された場合
                Thread.currentThread().interrupt();
                break;
            } catch (ClosedWatchServiceException e) {
                // WatchServiceがクローズされた場合
                break;
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error in watch loop", e);
            }
        }

        logger.info("ConfigWatcher loop stopped");
    }

    /**
     * ファイルイベントを処理します
     *
     * @param watchPath 監視パス
     * @param directoryName ディレクトリ名
     * @param event イベント
     */
    private void handleEvent(@NotNull Path watchPath, String directoryName, @NotNull WatchEvent<?> event) {
        WatchEvent.Kind<?> kind = event.kind();

        if (kind == StandardWatchEventKinds.OVERFLOW) {
            logger.warning("WatchEvent overflow detected");
            return;
        }

        @SuppressWarnings("unchecked")
        WatchEvent<Path> ev = (WatchEvent<Path>) event;
        Path fileName = ev.context();
        Path fullPath = watchPath.resolve(fileName);

        // YAMLファイルのみ処理
        if (!fileName.toString().endsWith(".yml") && !fileName.toString().endsWith(".yaml")) {
            return;
        }

        // ファイルリスナーに通知
        notifyFileListeners(fileName.toString(), fullPath);

        // ディレクトリリスナーに通知
        if (directoryName != null) {
            notifyDirectoryListeners(directoryName, fullPath);
        }

        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            logger.info("File created: " + fullPath);

        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            // 重複通知を防ぐための遅延処理（既に通知済みなので何もしない）

        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            logger.info("File deleted: " + fullPath);
        }
    }

    /**
     * 監視中のディレクトリ数を取得します
     *
     * @return ディレクトリ数
     */
    public int getWatchedDirectoryCount() {
        return watchKeys.size();
    }

    /**
     * 実行中かを確認します
     *
     * @return 実行中の場合はtrue
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Loggerを取得します
     *
     * @return ロガー
     */
    @NotNull
    public Logger getLogger() {
        return logger;
    }

    /**
     * ConfigManagerを取得します
     *
     * @return 設定マネージャー
     */
    @NotNull
    public YamlConfigManager getConfigManager() {
        return configManager;
    }
}
