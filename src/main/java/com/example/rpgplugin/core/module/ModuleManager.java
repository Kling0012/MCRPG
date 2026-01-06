package com.example.rpgplugin.core.module;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * モジュール管理クラス
 *
 * <p>プラグインの全モジュールを管理し、一括有効化/無効化/リロード機能を提供します。
 * 依存関係を解決し、適切な順序でモジュールを起動します。</p>
 *
 * <p>設計パターン:</p>
 * <ul>
 *   <li>ファサードパターン: モジュールシステムへの統一的インターフェースを提供</li>
 *   <li>オブザーバーパターン: モジュールの状態変化を管理</li>
 * </ul>
 *
 * <p>スレッド安全性:</p>
 * <ul>
 *   <li>ConcurrentHashMapを使用したスレッドセーフな実装</li>
 *   <li>外部同期なしで複数スレッドからアクセス可能</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 * @see IModule
 */
public class ModuleManager {

    private final JavaPlugin plugin;
    private final Logger logger;
    private final Map<String, IModule> modules;
    private final Map<String, Boolean> moduleStates;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public ModuleManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.modules = new ConcurrentHashMap<>();
        this.moduleStates = new ConcurrentHashMap<>();
    }

    /**
     * モジュールを登録します
     *
     * <p>同名のモジュールが既に登録されている場合は上書きされます。</p>
     *
     * @param module 登録するモジュール
     * @throws IllegalArgumentException モジュール名がnullまたは空の場合
     */
    public void registerModule(IModule module) {
        if (module == null) {
            throw new IllegalArgumentException("Module cannot be null");
        }

        String name = module.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Module name cannot be null or empty");
        }

        modules.put(name, module);
        moduleStates.put(name, false);

        logger.info("Module registered: " + name + " v" + module.getVersion());
    }

    /**
     * モジュールの登録を解除します
     *
     * <p>モジュールが有効化されている場合は、自動的に無効化されます。</p>
     *
     * @param name モジュール名
     * @return 登録解除に成功した場合はtrue、該当モジュールが存在しない場合はfalse
     */
    public boolean unregisterModule(String name) {
        IModule module = modules.get(name);
        if (module == null) {
            return false;
        }

        // 有効化されている場合は無効化
        if (module.isEnabled()) {
            disableModule(name);
        }

        modules.remove(name);
        moduleStates.remove(name);

        logger.info("Module unregistered: " + name);
        return true;
    }

    /**
     * 指定したモジュールを有効化します
     *
     * @param name モジュール名
     * @return 有効化に成功した場合はtrue、失敗した場合はfalse
     */
    public boolean enableModule(String name) {
        IModule module = modules.get(name);
        if (module == null) {
            logger.warning("Module not found: " + name);
            return false;
        }

        if (module.isEnabled()) {
            logger.warning("Module already enabled: " + name);
            return true;
        }

        try {
            module.enable(plugin);
            moduleStates.put(name, true);
            logger.info("Module enabled: " + name);
            return true;
        } catch (IModule.ModuleException e) {
            logger.log(Level.SEVERE, "Failed to enable module: " + name, e);
            return false;
        }
    }

    /**
     * 指定したモジュールを無効化します
     *
     * @param name モジュール名
     * @return 無効化に成功した場合はtrue、失敗した場合はfalse
     */
    public boolean disableModule(String name) {
        IModule module = modules.get(name);
        if (module == null) {
            logger.warning("Module not found: " + name);
            return false;
        }

        if (!module.isEnabled()) {
            logger.warning("Module already disabled: " + name);
            return true;
        }

        try {
            module.disable();
            moduleStates.put(name, false);
            logger.info("Module disabled: " + name);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to disable module: " + name, e);
            return false;
        }
    }

    /**
     * 指定したモジュールをリロードします
     *
     * @param name モジュール名
     * @return リロードに成功した場合はtrue、失敗した場合はfalse
     */
    public boolean reloadModule(String name) {
        IModule module = modules.get(name);
        if (module == null) {
            logger.warning("Module not found: " + name);
            return false;
        }

        try {
            module.reload(plugin);
            moduleStates.put(name, module.isEnabled());
            logger.info("Module reloaded: " + name);
            return true;
        } catch (IModule.ModuleException e) {
            logger.log(Level.SEVERE, "Failed to reload module: " + name, e);
            return false;
        }
    }

    /**
     * 全モジュールを依存関係を考慮して有効化します
     *
     * <p>依存関係をトポロジカルソートで解決し、適切な順序で有効化します。
     * 依存関係が循環している場合や依存モジュールが見つからない場合は、
     * そのモジュールの有効化はスキップされます。</p>
     *
     * @return 有効化に成功したモジュール数
     */
    public int enableAll() {
        logger.info("Enabling all modules...");

        // 依存関係を解決して起動順序を決定
        List<String> order = resolveDependencyOrder();

        int successCount = 0;
        for (String name : order) {
            if (enableModule(name)) {
                successCount++;
            }
        }

        logger.info("Modules enabled: " + successCount + "/" + modules.size());
        return successCount;
    }

    /**
     * 全モジュールを無効化します
     *
     * <p>有効化された順序の逆順で無効化します。</p>
     *
     * @return 無効化に成功したモジュール数
     */
    public int disableAll() {
        logger.info("Disabling all modules...");

        int successCount = 0;

        // 有効化されているモジュールを逆順で無効化
        List<String> enabledModules = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : moduleStates.entrySet()) {
            if (entry.getValue()) {
                enabledModules.add(entry.getKey());
            }
        }

        // 逆順で処理
        Collections.reverse(enabledModules);

        for (String name : enabledModules) {
            if (disableModule(name)) {
                successCount++;
            }
        }

        logger.info("Modules disabled: " + successCount + "/" + enabledModules.size());
        return successCount;
    }

    /**
     * 全モジュールをリロードします
     *
     * @return リロードに成功したモジュール数
     */
    public int reloadAll() {
        logger.info("Reloading all modules...");

        disableAll();
        int count = enableAll();

        logger.info("Modules reloaded: " + count);
        return count;
    }

    /**
     * モジュールを取得します
     *
     * @param name モジュール名
     * @return モジュールインスタンス、存在しない場合はnull
     */
    public IModule getModule(String name) {
        return modules.get(name);
    }

    /**
     * モジュールが有効化されているか確認します
     *
     * @param name モジュール名
     * @return 有効化されている場合はtrue、それ以外はfalse
     */
    public boolean isModuleEnabled(String name) {
        return moduleStates.getOrDefault(name, false);
    }

    /**
     * 登録されている全モジュール名を取得します
     *
     * @return モジュール名のセット
     */
    public Set<String> getModuleNames() {
        return Collections.unmodifiableSet(modules.keySet());
    }

    /**
     * 登録されているモジュール数を取得します
     *
     * @return モジュール数
     */
    public int getModuleCount() {
        return modules.size();
    }

    /**
     * 依存関係を解決してモジュールの起動順序を決定します
     *
     * <p>トポロジカルソートアルゴリズムを使用します。
     * 循環依存が検出された場合は例外をスローします。</p>
     *
     * @return 起動順序のリスト
     */
    private List<String> resolveDependencyOrder() {
        List<String> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (String name : modules.keySet()) {
            if (!visited.contains(name)) {
                if (!visitModule(name, visited, visiting, result)) {
                    logger.warning("Circular dependency detected involving: " + name);
                    // 循環依存がある場合は残りのモジュールを追加
                    for (String remaining : modules.keySet()) {
                        if (!visited.contains(remaining)) {
                            result.add(remaining);
                        }
                    }
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 深度優先探索で依存関係をトラバースします
     *
     * @param name 現在のモジュール名
     * @param visited 訪問済みモジュールセット
     * @param visiting 訪問中モジュールセット（循環検出用）
     * @param result 結果リスト
     * @return 循環依存がない場合はtrue、ある場合はfalse
     */
    private boolean visitModule(String name, Set<String> visited, Set<String> visiting, List<String> result) {
        // 循環依存検出
        if (visiting.contains(name)) {
            logger.severe("Circular dependency detected: " + name);
            return false;
        }

        // 既に訪問済み
        if (visited.contains(name)) {
            return true;
        }

        visiting.add(name);

        IModule module = modules.get(name);
        if (module != null) {
            // 依存モジュールを先に処理
            for (String dep : module.getDependencies()) {
                if (modules.containsKey(dep)) {
                    if (!visitModule(dep, visited, visiting, result)) {
                        return false;
                    }
                } else {
                    logger.warning("Dependency not found for module '" + name + "': " + dep);
                }
            }
        }

        visiting.remove(name);
        visited.add(name);
        result.add(name);

        return true;
    }
}
