package com.example.rpgplugin.api.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.SkriptLogger;
import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.skript.conditions.*;
import com.example.rpgplugin.api.skript.effects.*;
import com.example.rpgplugin.api.skript.events.EvtRPGSkillCast;
import com.example.rpgplugin.api.skript.expressions.*;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SKriptアドオン登録クラス
 *
 * <p>SKriptとの統合を管理します。</p>
 *
 * <p>このクラスはRPGPluginがロードされた際、SKriptが利用可能な場合に自動的に
 * 全ての式・条件・効果・イベントを登録します。</p>
 *
 * <p>登録される機能:</p>
 * <ul>
 *   <li>式: rpg level, rpg stat, rpg class, rpg skill, rpg skill point, rpg attr point</li>
 *   <li>条件: has rpg skill, can upgrade rpg class, rpg stat above, is rpg class</li>
 *   <li>効果: unlock rpg skill, cast rpg skill, set rpg class, modify rpg stat</li>
 *   <li>イベント: on rpg skill cast</li>
 * </ul>
 *
 * <p>使用例:</p>
 * <pre>
 * # 式の使用
 * set {_level} to rpg level of player
 * set {_skillPoints} to rpg skill point of player
 * set {_attrPoints} to rpg attr point of player
 *
 * # 条件の使用
 * if player has rpg skill "fireball":
 *     send "ファイアボール習得済み"
 *
 * # 効果の使用
 * unlock rpg skill "power_strike" for player
 * make player cast rpg skill "fireball"
 *
 * # ポイント操作
 * add 1 to rpg skill point of player
 * set rpg attr point of player to 5
 *
 * # イベントの使用
 * on rpg skill cast:
 *     send "スキル %skill-id% 発動！" to player
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class RPGSkriptAddon {

    private final RPGPlugin plugin;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public RPGSkriptAddon(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * SKript要素を登録します
     *
     * <p>このメソッドはRPGPluginのonEnable内で呼び出されます。</p>
     */
    public void registerElements() {
        if (plugin.getServer().getPluginManager().getPlugin("Skript") == null) {
            plugin.getLogger().info("Skript is not installed. Skipping Skript integration.");
            return;
        }

        try {
            // 式の登録
            registerExpressions();

            // 条件の登録
            registerConditions();

            // 効果の登録
            registerEffects();

            // イベントの登録
            registerEvents();

            plugin.getLogger().info("Skript integration loaded successfully!");
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            plugin.getLogger().warning("Skript version mismatch or not fully loaded. Some features may not work.");
            SkriptLogger.warn("RPGPlugin: Skript integration partially loaded due to version mismatch.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load Skript integration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 式（Expression）を登録します
     */
    private void registerExpressions() {
        // ExprRPGLevel - rpg level of %player%
        Skript.registerExpression(ExprRPGLevel.class, Number.class,
                ExpressionType.SIMPLE,
                "[the] rpg level of %player%",
                "[the] rpg level of %player%'s",
                "%player%'s rpg level"
        );

        // ExprRPGStat - rpg stat %string% of %player%
        Skript.registerExpression(ExprRPGStat.class, Number.class,
                ExpressionType.COMBINED,
                "[the] rpg stat[e] %string% of %player%",
                "[the] rpg stat[e] %string% of %player%'s",
                "%player%'s rpg stat[e] %string%"
        );

        // ExprRPGClass - rpg class of %player%
        Skript.registerExpression(ExprRPGClass.class, String.class,
                ExpressionType.SIMPLE,
                "[the] rpg class of %player%",
                "[the] rpg class of %player%'s",
                "%player%'s rpg class"
        );

        // ExprRPGSkill - rpg skill level of %string% from %player%
        Skript.registerExpression(ExprRPGSkill.class, Number.class,
                ExpressionType.COMBINED,
                "[the] rpg skill level of %string% [from] %player%",
                "[the] rpg skill level of %player%'s %string%",
                "%player%'s rpg skill level for %string%"
        );

        // ExprRPGSkillPoint - rpg skill point of %player%
        Skript.registerExpression(ExprRPGSkillPoint.class, Number.class,
                ExpressionType.SIMPLE,
                "[the] rpg skill point of %player%",
                "[the] rpg skill point of %player%'s",
                "%player%'s rpg skill point"
        );

        // ExprRPGAttrPoint - rpg attr[ibute] point of %player%
        Skript.registerExpression(ExprRPGAttrPoint.class, Number.class,
                ExpressionType.SIMPLE,
                "[the] rpg attr[ibute] point of %player%",
                "[the] rpg attr[ibute] point of %player%'s",
                "%player%'s rpg attr[ibute] point"
        );
    }

    /**
     * 条件（Condition）を登録します
     */
    private void registerConditions() {
        // CondHasRPGSkill - %player% has rpg skill %skill%
        Skript.registerCondition(CondHasRPGSkill.class,
                "%player% has [the] rpg skill [named] %string%",
                "%player%'s rpg skill[s] (contain|includes) %string%"
        );

        // CondCanUpgradeRPGClass - %player% can upgrade rpg class
        Skript.registerCondition(CondCanUpgradeRPGClass.class,
                "%player% can upgrade [their] rpg class",
                "%player% is able to upgrade [their] rpg class"
        );

        // CondRPGStatAbove - %player%'s rpg stat %string% is at least %number%
        Skript.registerCondition(CondRPGStatAbove.class,
                "%player%'s rpg stat[e] %string% is [at least] %number%",
                "rpg stat[e] %string% of %player% is [at least] %number%"
        );

        // CondIsRPGClass - %player%'s rpg class is %string%
        Skript.registerCondition(CondIsRPGClass.class,
                "%player%'s rpg class is %string%",
                "%player% is [in] rpg class %string%",
                "rpg class of %player% is %string%"
        );
    }

    /**
     * 効果（Effect）を登録します
     */
    private void registerEffects() {
        // EffUnlockRPGSkill - unlock rpg skill %skill% for %player%
        Skript.registerEffect(EffUnlockRPGSkill.class,
                "unlock [the] rpg skill %string% [for] %player%",
                "teach [the] rpg skill %string% to %player%",
                "make %player% (learn|unlock) [the] rpg skill %string%"
        );

        // EffCastRPGSkill - make %player% cast rpg skill %skill%
        Skript.registerEffect(EffCastRPGSkill.class,
                "make %player% cast [the] rpg skill %string%",
                "force %player% to cast [the] rpg skill %string%",
                "cast [the] rpg skill %string% [for] %player%"
        );

        // EffSetRPGClass - set rpg class of %player% to %string%
        Skript.registerEffect(EffSetRPGClass.class,
                "set [the] rpg class of %player% to %string%",
                "make %player% [a]n rpg class %string%",
                "change %player%'s rpg class to %string%"
        );

        // EffModifyRPGStat - add/set/remove rpg stat
        Skript.registerEffect(EffModifyRPGStat.class,
                "add %number% [to] rpg stat[e] %string% of %player%",
                "set rpg stat[e] %string% of %player% to %number%",
                "remove %number% from rpg stat[e] %string% of %player%"
        );
    }

    /**
     * イベント（Event）を登録します
     */
    private void registerEvents() {
        // EvtRPGSkillCast - on rpg skill cast
        Skript.registerEvent("RPG Skill Cast", EvtRPGSkillCast.class,
                com.example.rpgplugin.api.bridge.SKriptSkillEvent.class,
                "on rpg skill cast",
                "on rpg skill cast [of] %string%"
        );
    }
}
