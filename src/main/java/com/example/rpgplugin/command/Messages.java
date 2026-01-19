package com.example.rpgplugin.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * RPGコマンドメッセージ定数
 *
 * <p>コマンド関連のメッセージを一元管理します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>DRY: メッセージ文字列の重複を排除</li>
 *   <li>変更容易性: メッセージ修正時に1箇所の変更で済む</li>
 *   <li>国際化対応: 将来的な多言語化への布石</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public final class Messages {

    private Messages() {
        // ユーティリティクラス
    }

    // ==================== 一般メッセージ ====================

    public static final String CONSOLE_ONLY = "このコマンドはプレイヤーのみ使用できます";
    public static final Component UNKNOWN_COMMAND = Component.text("不明なコマンドです", NamedTextColor.RED);
    public static final Component UNKNOWN_COMMAND_HINT = Component.text("/rpg help でヘルプを表示", NamedTextColor.GRAY);
    public static final Component NO_PERMISSION = Component.text("権限がありません", NamedTextColor.RED);

    // ==================== ヘルプメッセージ ====================

    public static final class Help {
        private Help() {
        }

        public static final Component TITLE = Component.text("           RPGPlugin ヘルプ", NamedTextColor.GOLD, TextDecoration.BOLD);
        public static final Component BORDER = Component.text("========================================", NamedTextColor.DARK_GRAY);

        public static final Component BASIC_COMMANDS_TITLE = Component.text("【基本コマンド】", NamedTextColor.YELLOW, TextDecoration.BOLD);
        public static final Component CLASS_COMMANDS_TITLE = Component.text("【クラスコマンド】", NamedTextColor.YELLOW, TextDecoration.BOLD);
        public static final Component SKILL_COMMANDS_TITLE = Component.text("【スキルコマンド】", NamedTextColor.YELLOW, TextDecoration.BOLD);
        public static final Component ADMIN_COMMANDS_TITLE = Component.text("【管理者コマンド】", NamedTextColor.RED, TextDecoration.BOLD);

        public static final String MAIN_MENU = "/rpg - メインメニューを表示";
        public static final String HELP = "/rpg help - このヘルプを表示";
        public static final String STATS = "/rpg stats - ステータスを表示";
        public static final String SKILL = "/rpg skill - スキル情報を表示";
        public static final String CLASS_INFO = "/rpg class - クラス情報を表示";
        public static final String CLASS_LIST = "/rpg class list - クラス一覧を表示";
        public static final String CLASS_CHANGE = "/rpg class change <クラスID> [level] - クラスを変更";
        public static final String CLASS_SELECT = "/rpg class <クラスID> - クラスを選択";
        public static final String CAST = "/rpg cast <スキルID> - スキルを発動";
        public static final String RELOAD = "/rpg reload - 設定をリロード";
        public static final String ADMIN_CLASS_CHANGE = "/rpg class change <player> <classId> [level] - 他プレイヤーのクラスを変更";

        public static final Component HINT = Component.text("ヒント: Tabキーでコマンド補完が利用できます", NamedTextColor.GRAY, TextDecoration.ITALIC);
    }

    // ==================== ステータスメッセージ ====================

    public static final class Stats {
        private Stats() {
        }

        public static final Component TITLE = Component.text("=== RPGPlugin PlaceholderAPI ===", NamedTextColor.YELLOW);
        public static final Component INFO = Component.text("ステータスは外部GUIプラグインで表示できます", NamedTextColor.GRAY);
        public static final Component MAIN_PLACEHOLDERS = Component.text("主なプレースホルダー:", NamedTextColor.WHITE);

        public static final String PLACEHOLDER_LEVEL = "  %rpg_level% - レベル";
        public static final String PLACEHOLDER_CLASS = "  %rpg_class% - クラス名";
        public static final String PLACEHOLDER_STR = "  %rpg_stat_strength% - STR";
        public static final String PLACEHOLDER_INT = "  %rpg_stat_intelligence% - INT";
        public static final String PLACEHOLDER_SPI = "  %rpg_stat_spirit% - SPI";
        public static final String PLACEHOLDER_VIT = "  %rpg_stat_vitality% - VIT";
        public static final String PLACEHOLDER_DEX = "  %rpg_stat_dexterity% - DEX";
        public static final String PLACEHOLDER_POINTS = "  %rpg_available_points% - 振り分け可能ポイント";
    }

    // ==================== スキルメッセージ ====================

    public static final class Skill {
        private Skill() {
        }

        public static final Component TITLE = Component.text("           スキルヘルプ", NamedTextColor.BLUE, TextDecoration.BOLD);
        public static final Component BORDER = Component.text("========================================", NamedTextColor.DARK_GRAY);

        public static final String CAST_USAGE = "使用法: /rpg cast <スキルID>";
        public static final String CAST_DESC = "/rpg cast <スキルID> - スキルを発動します";

        public static final Component CONDITION_TITLE = Component.text("発動条件:", NamedTextColor.WHITE);
        public static final Component CONDITION_ACQUIRED = Component.text("  • スキルを習得している必要があります", NamedTextColor.GRAY);
        public static final Component CONDITION_COOLDOWN = Component.text("  • クールダウン中ではない必要があります", NamedTextColor.GRAY);
        public static final Component CONDITION_MANA = Component.text("  • 十分なMPを持っている必要があります", NamedTextColor.GRAY);
    }

    // ==================== クラスメッセージ ====================

    public static final class ClassCmd {
        private ClassCmd() {
        }

        public static final String TITLE = "=== クラスシステム ===";
        public static final String USAGE = "使用法: /rpg class [list|change]";

        public static final Component CHANGE_USAGE = Component.text("使用法: /rpg class change <クラスID> [level]", NamedTextColor.RED);
        public static final Component CHANGE_ADMIN_USAGE = Component.text("使用法: /rpg class change <player> <classId> [level]", NamedTextColor.RED);

        public static final Component LIST_TITLE = Component.text("=== 利用可能なクラス ===", NamedTextColor.YELLOW);
        public static final Component USAGE_HINT = Component.text("使用法: /rpg class <クラスID>", NamedTextColor.GRAY);
        public static final String INFO_USAGE = "/rpg class info <クラスID> - クラス情報を表示";
        public static final String LIST = "/rpg class list - 利用可能なクラスを表示";
        public static final String CHANGE_USAGE_SHORT = "/rpg class change <クラスID> - クラスを変更";
        public static final String UPGRADE = "/rpg class upgrade - クラスをアップグレード";

        public static final Component NOT_FOUND = Component.text("クラスが見つかりません: ", NamedTextColor.RED);
        public static final Component CHECK_LIST = Component.text("使用法: /rpg class list でクラス一覧を確認", NamedTextColor.GRAY);
        public static final Component CHANGE_SUCCESS = Component.text("クラスを変更しました: ", NamedTextColor.GREEN);
        public static final Component CHANGE_FAILED = Component.text("クラスの変更に失敗しました", NamedTextColor.RED);

        public static final Component ADMIN_CHANGE_SUCCESS = Component.text(" のクラスを変更しました: ", NamedTextColor.GREEN);
        public static final Component ADMIN_CHANGE_NOTIFICATION = Component.text("クラスを変更されました: ", NamedTextColor.GREEN);
        public static final String LEVEL_FORMAT = "レベル: ";
    }

    // ==================== スキル発動メッセージ ====================

    public static final class Cast {
        private Cast() {
        }

        public static final Component SKILL_NOT_FOUND = Component.text("スキルが見つかりません: ", NamedTextColor.RED);
        public static final Component MANAGER_NOT_INITIALIZED = Component.text("スキルマネージャーが初期化されていません", NamedTextColor.RED);
        public static final Component EXECUTOR_NOT_INITIALIZED = Component.text("スキル実行システムが初期化されていません", NamedTextColor.RED);
        public static final Component NOT_ACQUIRED = Component.text("このスキルを習得していません: ", NamedTextColor.RED);
        public static final Component COOLDOWN = Component.text("クールダウン中です", NamedTextColor.RED);
        public static final Component CAST_FAILED = Component.text("スキルの発動に失敗しました: ", NamedTextColor.RED);
        public static final Component NUMBER_FORMAT_ERROR = Component.text("レベルは数値で指定してください", NamedTextColor.RED);
    }

    // ==================== リロードメッセージ ====================

    public static final class Reload {
        private Reload() {
        }

        public static final Component TITLE = Component.text("=== RPGPlugin リロード中 ===", NamedTextColor.YELLOW);
        public static final String SUCCESS_FORMAT = "リロード完了!(%dms)";
        public static final Component PLAYER_NOT_FOUND = Component.text("プレイヤーが見つかりません、またはオフラインです: ", NamedTextColor.RED);
        public static final String LEVEL_FORMAT = "レベル: ";
    }

    // ==================== GUIメッセージ ====================

    public static final class GUI {
        private GUI() {
        }

        public static final Component TITLE = Component.text("=== RPG Plugin ===", NamedTextColor.YELLOW);

        public static final String STATS = "/rpg stats - ステータスを表示";
        public static final String SKILL = "/rpg skill - スキル情報を表示";
        public static final String CLASS_LIST = "/rpg class list - クラス一覧を表示";
        public static final String CLASS_CHANGE = "/rpg class change <クラスID> [level] - クラスを変更";
        public static final String CAST_SKILL = "/rpg cast <スキルID> - スキルを発動";
        public static final String HELP_CMD = "/rpg help - ヘルプを表示";
        public static final String ADMIN_CLASS_CHANGE = "/rpg class change <player> <classId> [level] - 他プレイヤーのクラスを変更";
        public static final String ADMIN_RELOAD = "/rpg reload - 設定をリロード";

        public static final Component ERROR_TITLE = Component.text("=== スキルツリー ===", NamedTextColor.YELLOW);
        public static final Component NOT_FOUND_TITLE = Component.text("スキルツリーが見つかりません: ", NamedTextColor.RED);
        public static final Component NOT_AVAILABLE = Component.text("スキルツリーが利用できません", NamedTextColor.RED);
    }
}
