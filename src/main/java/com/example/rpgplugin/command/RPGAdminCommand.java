package com.example.rpgplugin.command;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.rpgclass.ClassLoader;
import com.example.rpgplugin.rpgclass.ClassManager;
import com.example.rpgplugin.skill.SkillLoader;
import com.example.rpgplugin.skill.SkillManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * RPG管理コマンド
 *
 * <p>管理者用のコマンドを提供します:</p>
 * <ul>
 *   <li>/rpgadmin reload classes - 職業クラスYAMLを再読み込み</li>
 *   <li>/rpgadmin reload skills - スキルYAMLを再読み込み</li>
 *   <li>/rpgadmin reload all - 全てのYAMLを再読み込み</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: 管理コマンドに特化</li>
 *   <li>KISS: シンプルなAPI設計</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class RPGAdminCommand implements CommandExecutor, TabCompleter {

    private final Logger logger;
    private final ClassManager classManager;
    private final SkillManager skillManager;
    private final ClassLoader classLoader;
    private final SkillLoader skillLoader;

    /**
     * コンストラクタ
     *
     * @param plugin        プラグインインスタンス
     * @param classManager  クラスマネージャー
     * @param skillManager  スキルマネージャー
     * @param classLoader   クラスローダー
     * @param skillLoader   スキルローダー
     */
    public RPGAdminCommand(RPGPlugin plugin, ClassManager classManager, SkillManager skillManager,
                          ClassLoader classLoader, SkillLoader skillLoader) {
        this.logger = plugin.getLogger();
        this.classManager = classManager;
        this.skillManager = skillManager;
        this.classLoader = classLoader;
        this.skillLoader = skillLoader;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 権限チェック
        if (!sender.hasPermission("rpgplugin.admin")) {
            sender.sendMessage("§cこのコマンドを実行する権限がありません。");
            return true;
        }

        // 引数なしの場合はヘルプを表示
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReload(sender, args);
                break;

            case "help":
                showHelp(sender);
                break;

            default:
                sender.sendMessage("§c不明なサブコマンド: " + subCommand);
                showHelp(sender);
                break;
        }

        return true;
    }

    /**
     * リロードコマンドを処理
     *
     * @param sender コマンド送信者
     * @param args   引数
     */
    private void handleReload(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c使用方法: /rpgadmin reload <classes|skills|all>");
            return;
        }

        String target = args[1].toLowerCase();

        switch (target) {
            case "classes":
                reloadClasses(sender);
                break;

            case "skills":
                reloadSkills(sender);
                break;

            case "all":
                reloadAll(sender);
                break;

            default:
                sender.sendMessage("§c不明なリロードターゲット: " + target);
                sender.sendMessage("§e使用可能なターゲット: classes, skills, all");
                break;
        }
    }

    /**
     * 職業クラスをリロード
     *
     * @param sender コマンド送信者
     */
    private void reloadClasses(CommandSender sender) {
        sender.sendMessage("§e職業クラスをリロード中...");

        try {
            // 新しいクラスを読み込み
            var newClasses = classLoader.loadAllClasses();

            // クリーンアップ付きでリロード
            ClassManager.ReloadResult result = classManager.reloadWithCleanup(newClasses);

            // 結果を送信
            sender.sendMessage("§a職業クラスをリロードしました:");
            sender.sendMessage("§7  - 読み込み: §f" + result.getLoadedClassCount() + " クラス");

            if (result.hasRemovedClasses()) {
                sender.sendMessage("§c  - 削除: §f" + result.getRemovedClasses().size() + " クラス §7(" +
                        String.join(", ", result.getRemovedClasses()) + ")");
                sender.sendMessage("§c  - 影響を受けたプレイヤー: §f" + result.getAffectedPlayerCount() + " 人");
            }

            logger.info("Classes reloaded by " + sender.getName() + ": " + result);

        } catch (Exception e) {
            sender.sendMessage("§c職業クラスのリロードに失敗しました: " + e.getMessage());
            logger.severe("Failed to reload classes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * スキルをリロード
     *
     * @param sender コマンド送信者
     */
    private void reloadSkills(CommandSender sender) {
        sender.sendMessage("§eスキルをリロード中...");

        try {
            // 新しいスキルを読み込み
            var newSkills = skillLoader.loadAllSkills();
            var newSkillMap = newSkills.stream()
                    .collect(Collectors.toMap(s -> s.getId(), s -> s));

            // クリーンアップ付きでリロード
            SkillManager.ReloadResult result = skillManager.reloadWithCleanup(newSkillMap);

            // 結果を送信
            sender.sendMessage("§aスキルをリロードしました:");
            sender.sendMessage("§7  - 読み込み: §f" + result.getLoadedSkillCount() + " スキル");

            if (result.hasRemovedSkills()) {
                sender.sendMessage("§c  - 削除: §f" + result.getRemovedSkills().size() + " スキル §7(" +
                        String.join(", ", result.getRemovedSkills()) + ")");
                sender.sendMessage("§c  - 影響を受けたプレイヤー: §f" + result.getAffectedPlayerCount() + " 人");
                sender.sendMessage("§c  - 削除されたスキルエントリ: §f" + result.getTotalSkillsRemoved() + " 件");
            }

            logger.info("Skills reloaded by " + sender.getName() + ": " + result);

        } catch (Exception e) {
            sender.sendMessage("§cスキルのリロードに失敗しました: " + e.getMessage());
            logger.severe("Failed to reload skills: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 全てをリロード
     *
     * @param sender コマンド送信者
     */
    private void reloadAll(CommandSender sender) {
        sender.sendMessage("§e全てのデータをリロード中...");

        try {
            // 職業クラスをリロード
            var newClasses = classLoader.loadAllClasses();
            ClassManager.ReloadResult classResult = classManager.reloadWithCleanup(newClasses);

            // スキルをリロード
            var newSkills = skillLoader.loadAllSkills();
            var newSkillMap = newSkills.stream()
                    .collect(Collectors.toMap(s -> s.getId(), s -> s));
            SkillManager.ReloadResult skillResult = skillManager.reloadWithCleanup(newSkillMap);

            // 結果を送信
            sender.sendMessage("§a全てのデータをリロードしました:");

            // クラス結果
            sender.sendMessage("§6【職業クラス】");
            sender.sendMessage("§7  - 読み込み: §f" + classResult.getLoadedClassCount() + " クラス");
            if (classResult.hasRemovedClasses()) {
                sender.sendMessage("§c  - 削除: §f" + classResult.getRemovedClasses().size() + " クラス");
                sender.sendMessage("§c  - 影響プレイヤー: §f" + classResult.getAffectedPlayerCount() + " 人");
            }

            // スキル結果
            sender.sendMessage("§6【スキル】");
            sender.sendMessage("§7  - 読み込み: §f" + skillResult.getLoadedSkillCount() + " スキル");
            if (skillResult.hasRemovedSkills()) {
                sender.sendMessage("§c  - 削除: §f" + skillResult.getRemovedSkills().size() + " スキル");
                sender.sendMessage("§c  - 影響プレイヤー: §f" + skillResult.getAffectedPlayerCount() + " 人");
            }

            logger.info("All data reloaded by " + sender.getName() +
                    ": Classes=" + classResult + ", Skills=" + skillResult);

        } catch (Exception e) {
            sender.sendMessage("§cリロードに失敗しました: " + e.getMessage());
            logger.severe("Failed to reload all: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ヘルプを表示
     *
     * @param sender コマンド送信者
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6=== RPG管理コマンド ヘルプ ===");
        sender.sendMessage("§e/rpgadmin reload classes §7- 職業クラスYAMLを再読み込み");
        sender.sendMessage("§e/rpgadmin reload skills §7- スキルYAMLを再読み込み");
        sender.sendMessage("§e/rpgadmin reload all §7- 全てのYAMLを再読み込み");
        sender.sendMessage("§e/rpgadmin help §7- このヘルプを表示");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 第一引数: サブコマンド
            completions.addAll(Arrays.asList("reload", "help"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reload")) {
            // 第二引数: リロードターゲット
            completions.addAll(Arrays.asList("classes", "skills", "all"));
        }

        // 入力された文字でフィルタリング
        String input = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
    }
}
