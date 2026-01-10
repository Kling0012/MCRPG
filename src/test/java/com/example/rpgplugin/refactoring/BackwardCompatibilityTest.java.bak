package com.example.rpgplugin.refactoring;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.RPGPluginAPI;
import com.example.rpgplugin.core.config.ConfigWatcher;
import com.example.rpgplugin.core.config.YamlConfigManager;
import com.example.rpgplugin.core.dependency.DependencyManager;
import com.example.rpgplugin.core.module.ModuleManager;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.ExpDiminisher;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.config.SkillConfig;
import com.example.rpgplugin.skill.executor.ActiveSkillExecutor;
import com.example.rpgplugin.skill.executor.PassiveSkillExecutor;
import com.example.rpgplugin.stats.StatManager;
import com.example.rpgplugin.storage.StorageManager;
import com.example.rpgplugin.auction.AuctionManager;
import com.example.rpgplugin.currency.CurrencyManager;
import com.example.rpgplugin.currency.CurrencyListener;
import com.example.rpgplugin.damage.DamageManager;
import com.example.rpgplugin.gui.menu.SkillMenuListener;
import com.example.rpgplugin.gui.menu.rpgclass.ClassMenuListener;
import com.example.rpgplugin.mythicmobs.MythicMobsManager;
import com.example.rpgplugin.mythicmobs.listener.MythicDeathListener;
import com.example.rpgplugin.rpgclass.ClassManager;
import com.example.rpgplugin.trade.TradeManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 後方互換性テスト
 *
 * <p>リファクタリング後もAPIシグネチャが変更されていないことを検証します。</p>
 * <p>外部プラグインからのアクセスに影響がないことを保証します。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("後方互換性テスト")
class BackwardCompatibilityTest {

    /**
     * 全てのgetterメソッドが存在し、nullでないことを検証します
     */
    @Test
    @DisplayName("全てのgetterメソッドが正しく定義されていること")
    void testAllGettersExist() {
        Class<RPGPlugin> pluginClass = RPGPlugin.class;

        // 静的メソッド getInstance()
        assertGetterExists(pluginClass, "getInstance", RPGPlugin.class);

        // マネージャー getter (20個)
        assertGetterExists(pluginClass, "getStorageManager", StorageManager.class);
        assertGetterExists(pluginClass, "getPlayerManager", PlayerManager.class);
        assertGetterExists(pluginClass, "getStatManager", StatManager.class);
        assertGetterExists(pluginClass, "getClassManager", ClassManager.class);
        assertGetterExists(pluginClass, "getSkillManager", SkillManager.class);
        assertGetterExists(pluginClass, "getAuctionManager", AuctionManager.class);
        assertGetterExists(pluginClass, "getDependencyManager", DependencyManager.class);
        assertGetterExists(pluginClass, "getModuleManager", ModuleManager.class);
        assertGetterExists(pluginClass, "getConfigManager", YamlConfigManager.class);
        assertGetterExists(pluginClass, "getConfigWatcher", ConfigWatcher.class);
        assertGetterExists(pluginClass, "getDamageManager", DamageManager.class);
        assertGetterExists(pluginClass, "getCurrencyManager", CurrencyManager.class);
        assertGetterExists(pluginClass, "getTradeManager", TradeManager.class);
        assertGetterExists(pluginClass, "getMythicMobsManager", MythicMobsManager.class);

        // リスナー getter (4個)
        assertGetterExists(pluginClass, "getSkillMenuListener", SkillMenuListener.class);
        assertGetterExists(pluginClass, "getClassMenuListener", ClassMenuListener.class);
        assertGetterExists(pluginClass, "getCurrencyListener", CurrencyListener.class);
        assertGetterExists(pluginClass, "getMythicDeathListener", MythicDeathListener.class);

        // エグゼキューター getter (2個)
        assertGetterExists(pluginClass, "getActiveSkillExecutor", ActiveSkillExecutor.class);
        assertGetterExists(pluginClass, "getPassiveSkillExecutor", PassiveSkillExecutor.class);

        // 設定・API・その他 (3個)
        assertGetterExists(pluginClass, "getSkillConfig", SkillConfig.class);
        assertGetterExists(pluginClass, "getAPI", RPGPluginAPI.class);
        assertGetterExists(pluginClass, "getExpDiminisher", ExpDiminisher.class);
    }

    /**
     * APIのシグネチャが変更されていないことを検証します
     */
    @Test
    @DisplayName("APIシグネチャが後方互換性を保っていること")
    void testAPISignatureBackwardCompatibility() {
        Class<RPGPlugin> pluginClass = RPGPlugin.class;

        // getInstance()はstaticで、引数なし、RPGPluginを返す
        Method getInstanceMethod = assertMethodSignature(
                pluginClass,
                "getInstance",
                RPGPlugin.class,
                true,
                new Class<?>[]{}
        );

        // 全てのインスタンスメソッドgetterは、引数なし、非static
        String[] instanceGetters = {
                "getStorageManager", "getPlayerManager", "getStatManager",
                "getClassManager", "getSkillManager", "getAuctionManager",
                "getDependencyManager", "getModuleManager", "getConfigManager",
                "getConfigWatcher", "getDamageManager", "getSkillMenuListener",
                "getClassMenuListener", "getSkillConfig", "getActiveSkillExecutor",
                "getPassiveSkillExecutor", "getCurrencyManager", "getCurrencyListener",
                "getTradeManager", "getMythicMobsManager", "getMythicDeathListener",
                "getAPI", "getExpDiminisher"
        };

        for (String getterName : instanceGetters) {
            assertMethodSignature(
                    pluginClass,
                    getterName,
                    null,  // 戻り値の型はgetter名によって異なるためチェックしない
                    false, // 非static
                    new Class<?>[]{} // 引数なし
            );
        }
    }

    /**
     * 外部プラグインからのアクセスパターンをシミュレートします
     */
    @Test
    @DisplayName("外部プラグインからのAPIアクセスパターンが動作すること")
    void testExternalPluginAccessPatterns() {
        // パターン1: 静的アクセス
        // RPGPlugin plugin = RPGPlugin.getInstance();
        assertStaticMethodAccessible(RPGPlugin.class, "getInstance");

        // パターン2: マネージャー取得
        assertInstanceMethodAccessible(RPGPlugin.class, "getStorageManager");
        assertInstanceMethodAccessible(RPGPlugin.class, "getPlayerManager");
        assertInstanceMethodAccessible(RPGPlugin.class, "getStatManager");
        assertInstanceMethodAccessible(RPGPlugin.class, "getClassManager");
        assertInstanceMethodAccessible(RPGPlugin.class, "getSkillManager");
        assertInstanceMethodAccessible(RPGPlugin.class, "getAuctionManager");
        assertInstanceMethodAccessible(RPGPlugin.class, "getDependencyManager");
        assertInstanceMethodAccessible(RPGPlugin.class, "getModuleManager");

        // パターン3: 設定・コンフィグ取得
        assertInstanceMethodAccessible(RPGPlugin.class, "getConfigManager");
        assertInstanceMethodAccessible(RPGPlugin.class, "getConfigWatcher");

        // パターン4: リスナー取得
        assertInstanceMethodAccessible(RPGPlugin.class, "getSkillMenuListener");
        assertInstanceMethodAccessible(RPGPlugin.class, "getClassMenuListener");
        assertInstanceMethodAccessible(RPGPlugin.class, "getCurrencyListener");
        assertInstanceMethodAccessible(RPGPlugin.class, "getMythicDeathListener");

        // パターン5: スキル関連
        assertInstanceMethodAccessible(RPGPlugin.class, "getSkillConfig");
        assertInstanceMethodAccessible(RPGPlugin.class, "getActiveSkillExecutor");
        assertInstanceMethodAccessible(RPGPlugin.class, "getPassiveSkillExecutor");

        // パターン6: その他システム
        assertInstanceMethodAccessible(RPGPlugin.class, "getCurrencyManager");
        assertInstanceMethodAccessible(RPGPlugin.class, "getTradeManager");
        assertInstanceMethodAccessible(RPGPlugin.class, "getMythicMobsManager");
        assertInstanceMethodAccessible(RPGPlugin.class, "getDamageManager");
        assertInstanceMethodAccessible(RPGPlugin.class, "getAPI");
        assertInstanceMethodAccessible(RPGPlugin.class, "getExpDiminisher");
    }

    /**
     * メソッドが存在し、正しいシグネチャを持っていることをアサートします
     *
     * @param clazz クラス
     * @param methodName メソッド名
     * @param expectedReturnType 期待される戻り値の型（nullの場合はチェックしない）
     * @param isStatic staticメソッドかどうか
     * @param parameterTypes パラメータの型
     * @return メソッドオブジェクト
     */
    private Method assertMethodSignature(
            Class<?> clazz,
            String methodName,
            Class<?> expectedReturnType,
            boolean isStatic,
            Class<?>[] parameterTypes) {

        try {
            Method method = clazz.getMethod(methodName, parameterTypes);

            // static修飾子のチェック
            if (isStatic) {
                assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers()))
                        .as("メソッド %s はstaticであるべきです", methodName)
                        .isTrue();
            } else {
                assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers()))
                        .as("メソッド %s はstaticでないべきです", methodName)
                        .isFalse();
            }

            // 戻り値の型チェック（expectedReturnTypeがnullの場合はスキップ）
            if (expectedReturnType != null) {
                assertThat(method.getReturnType())
                        .as("メソッド %s の戻り値の型が正しくありません", methodName)
                        .isEqualTo(expectedReturnType);
            }

            // パブリックメソッドであることを確認
            assertThat(java.lang.reflect.Modifier.isPublic(method.getModifiers()))
                    .as("メソッド %s はpublicであるべきです", methodName)
                    .isTrue();

            return method;

        } catch (NoSuchMethodException e) {
            throw new AssertionError(
                    String.format("メソッド %s が見つかりません: %s",
                            methodName,
                            e.getMessage()),
                    e
            );
        }
    }

    /**
     * Getterメソッドが存在することをアサートします
     *
     * @param clazz クラス
     * @param methodName メソッド名
     * @param expectedReturnType 期待される戻り値の型
     */
    private void assertGetterExists(Class<?> clazz, String methodName, Class<?> expectedReturnType) {
        try {
            Method method = clazz.getMethod(methodName);
            assertNotNull(method, String.format("メソッド %s が存在しません", methodName));
            assertThat(method.getReturnType())
                    .as("メソッド %s の戻り値の型が正しくありません", methodName)
                    .isEqualTo(expectedReturnType);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(
                    String.format("メソッド %s が見つかりません: %s",
                            methodName,
                            e.getMessage()),
                    e
            );
        }
    }

    /**
     * staticメソッドがアクセス可能であることをアサートします
     *
     * @param clazz クラス
     * @param methodName メソッド名
     */
    private void assertStaticMethodAccessible(Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getMethod(methodName);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers()))
                    .as("メソッド %s はstaticであるべきです", methodName)
                    .isTrue();
            assertThat(java.lang.reflect.Modifier.isPublic(method.getModifiers()))
                    .as("メソッド %s はpublicであるべきです", methodName)
                    .isTrue();
        } catch (NoSuchMethodException e) {
            throw new AssertionError(
                    String.format("staticメソッド %s が見つかりません: %s",
                            methodName,
                            e.getMessage()),
                    e
            );
        }
    }

    /**
     * インスタンスメソッドがアクセス可能であることをアサートします
     *
     * @param clazz クラス
     * @param methodName メソッド名
     */
    private void assertInstanceMethodAccessible(Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getMethod(methodName);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers()))
                    .as("メソッド %s はstaticでないべきです", methodName)
                    .isFalse();
            assertThat(java.lang.reflect.Modifier.isPublic(method.getModifiers()))
                    .as("メソッド %s はpublicであるべきです", methodName)
                    .isTrue();
        } catch (NoSuchMethodException e) {
            throw new AssertionError(
                    String.format("インスタンスメソッド %s が見つかりません: %s",
                            methodName,
                            e.getMessage()),
                    e
            );
        }
    }
}
